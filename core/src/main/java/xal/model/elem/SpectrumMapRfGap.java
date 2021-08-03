/*
 *  SpectrumMapRfGap.java
 *
 *  Created on October 22, 2002, 1:58 PM
 */
package xal.model.elem;

import java.io.PrintWriter;

import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.sync.IRfCavityCell;
import xal.model.elem.sync.IRfGap;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.RfGap;
import xal.tools.beam.EnergyVector;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.beam.em.AxialFieldSpectrum;
import xal.tools.beam.optics.AcceleratingRfGap;
import xal.tools.beam.optics.AcceleratingRfGap.LOC;
import xal.tools.math.fnc.IRealFunction;

/**
 * <p>
 * Represents the action of an ideal RF gap. Gap is modeled as a thin element
 * whose accelerating action is given by the Panofsky formula. </p>
 * <p>
 * </p>
 * <p>
 * The gap provides acceleration to the propagation probe as well as
 * longitudinal focusing and radial defocusing. These mechanisms are implemented
 * according to that provided by an ideal gap where the effects can be described
 * analytically.
 * </p>
 * <p>
 * <h4>CKA NOTES:</h4>
 * <br/>
 * &middot; The gap length <em>L</em> should not be used in any time or phase
 * calculations since it is absorbed by adjacent drift spaces. (So I believe.)
 * For example, drift time &Delta;<em>t</em> should probably not include the
 * term &Delta;<em>t</em> &prop; &omega;<em>L</em>/2.
 * <br/>
 * <br/>
 * &middot; The <em>phase correction</em> parameter &Delta;&phi; I believe is
 * the change in probe phase due to propagation from the first gap in the
 * cavity. Thus, the first gap has a correction of &Delta;&phi; and the probe
 * phase there &phi;<sub>0</sub> is simply the cavity phase
 * &phi;<sub><em>cav</em></sub>.
 * <br/>
 * <br/>
 * &middot; It is essential that the probe has the correct phase coming into
 * this element. If this is a first gap (see <code>{@link #isFirstGap()}</code>)
 * then that phase must be the phase of the cavity, given by
 * <code>{@link #getPhase()}</code>. Thus, we must set the probe phase to the
 * phase of the cavity at these special gaps. (This is unfortunately necessary
 * since we cannot implement RF cavity elements with the current lattice
 * generator.) In the Element/Algorithm/Probe architecture elements do not
 * modify probes. Thus, although it's a kluge no matter which way you do it, the
 * probe phase should be reset by the algorithm.
 * <br/>
 * <br/>
 * &middot; There are provisions for both an offset of gap electrical center
 * with geometric center and for the Fourier sine transit time factor
 * <em>S</em>. And both values are used. This creates a potential inconsistency
 * since the sine transit time factor can account for any shifts in the field
 * center (i.e., the Fourier sine and cosine transforms together can represent
 * <strong>any</strong> continuous function.) I'm not sure if the provided
 * <em>S</em>(&beta;) is taken at the geometric center or the electrical center
 * (there it would probably be zero). If at the geometric center you are
 * probably shifting everything right back to the geometric center by using the
 * offset.
 * <br/>
 * <br/>
 * &middot; The above condition is localized in the method
 * <code>{@link #compGapPhaseAndEnergyGain(IProbe)}</code> which determines when
 * to add the phase change due to the gap offset. It is added into the phase
 * change when the phase and energy are computed directly by the Panofsky method
 * (i.e., method <code>{@link #compGapPhaseAndEnergyGainDirect(IProbe)}</code>).
 * When the <em>S</em>(&beta;) transit time factor is used to compute the phase
 * and energy gain (i.e., method
 * <code>{@link #compGapPhaseAndEnergyGainIndirect(IProbe)}</code>) the change
 * in phase due to gap offset is not added.
 * <br/>
 * <br/>
 * &middot; The above method
 * <code>{@link #compGapPhaseAndEnergyGain(IProbe)}</code> also consolidates the
 * two different methods of computing the phase and energy gains through the
 * gap. One method is the direct method based upon the Panofsky equation (and
 * where the phase change is 0 through the gap). This method is used when the
 * <kbd>useRfGapPhaseCalculation</kbd> flag in the probe's algorithm object is
 * set to <code>false</code>. If it is set to <code>true</code> then the
 * indirect, iterative (and presumably more accurate) method is used to solve a
 * transcendental equation for the phase and energy changes through the gap.
 * </p>
 *
 * @author Christopher K. Allen
 * @since November 22, 2005
 * @version Nov 23, 2014
 * <br/>Jan 16, 2015
 * <br/>July 29, 2015
 */
public class SpectrumMapRfGap extends ThinElement implements IRfGap, IRfCavityCell {

    /*
     *  Global Constants
     */
    /**
     * the string type identifier for all SpectrumMapRfGap objects
     */
    public static final String TYPE = "SpectrumMapRfGap";

    //
    // DataAdaptor Data Tags
    //
    /**
     * Parameters for XAL MODEL LATTICE dtd
     */
    public static final String PARAM_ETL = "ETL";

    /**
     * Description of the Field
     */
    public static final String PARAM_PHASE = "Phase";

    /**
     * Description of the Field
     */
    public static final String PARAM_FREQ = "Frequency";

    //
    // Numeric Constants
    //
    /**
     * The number 2&pi;
     */
    public static final double DBL_2PI = 2.0 * Math.PI;

    /**
     * Error tolerance in the iterative search for phase change through RF gap
     */
    private static final double DBL_PHASECALC_CNVERR = 1.0e-12;

    /**
     * Maximum number of allowable iterations in the phase change search
     */
    private static final int INT_PHASECALC_MAXITER = 50;

    private double synchronousPhase;
    private double energyGain;

    @Override
    public void computeSynchronousPhaseAndEnergyGain(IProbe probe) {
        synchronousPhase = compGapEntrancePhase(probe);
        energyGain = energyGain(probe);
    }

    @Override
    public double getSynchronousPhase() {
        return synchronousPhase;
    }

    @Override
    public double getEnergyGain() {
        return energyGain;
    }


    /*
     * Internal Data Structures
     */
    /**
     * Enumeration specifying the method of phase and energy gain calculation.
     * The gap may be switched between the given simulation modes to achieve
     * differing objectives. Specially we can simulate a "perfect" gap in order
     * to facilitate design calculations, or simulate various degrees of
     * deviation from design parameters to observe operating effects.
     *
     *
     * @author Christopher K. Allen
     * @since Feb 5, 2015
     */
    public enum PHASECALC {

        /**
         * Design Phase: Use gap design phase and design transit time factor to
         * compute energy.
         */
        DESIGN,
        /**
         * Dynamic Phase: Use <strong>probe</strong> phase with cavity spatial
         * fields (i.e., mode structure) and gap offset for phase, design
         * transit time factor <em>T</em>(&beta;) for energy calculation.
         */
        DYNPHASE,
        /**
         * Dynamic Energy: Use dynamic phase plus iterative algorithm for energy
         * including both <em>S</em>(&beta;) and <em>T</em>(&beta;) transit time
         * factors accounting for gap offset.
         */
        DYNENERGY;
    }

    /**
     * Represents the longitudinal conjugate variables of phase and energy.
     *
     * @author Christopher K. Allen
     * @since Nov 28, 2014
     */
    private static class EnergyVariables {

        /**
         * particle phase
         */
        private double phi;

        /**
         * particle energy
         */
        private double W;

        /**
         * Zero argument constructor
         */
        @SuppressWarnings("unused")
        public EnergyVariables() {
            this(0.0, 0.0);
        }

        /**
         * Initializing Constructor
         */
        public EnergyVariables(double phi, double W) {
            this.phi = phi;
            this.W = W;
        }

        /**
         * @see java.lang.Object#toString()
         *
         * @since Feb 5, 2015 by Christopher K. Allen
         */
        @Override
        public String toString() {
            return "(phi,W)=(" + phi + ", " + W + ')';
        }

    }

    /*
     * Global Attributes
     */
 /*
     *  Local Attributes
     */
    //
    // Operating Parameters
    //
    /**
     * ETL product of gap
     */
    private double dblETL = 0.0;

    /**
     * phase delay of gap w.r.t. the synchronous particle
     */
    private double dblPhase = 0.0;

    /**
     * the on axis accelerating field (V)
     */
    private double dblFieldE0 = 0.;

    /**
     * operating frequency of the gap
     */
    private double dblFreq = 0.0;

    //
    // Geometric Properties
    //
    /**
     * the separation of the gap center from the cell center (m)
     */
    private double gapOffset = 0.;

    /**
     * <del>the accelerating cell length</del> No, this is the effective length
     * of the gap
     */
    private double dblGapLength = 0.;

    /**
     * flag indicating that this gap is in the leading cell of an RF cavity
     */
    private boolean bolStartCell = false;

    /**
     * flag indicating that this gap is in the end cell of an RF cavity
     */
    private boolean bolEndCell = false;

    /**
     * axial field spectrum defining this gap
     */
    private AxialFieldSpectrum spcGapFlds;

    /**
     * RF gap acceleration model used to compute phase jump and energy gain
     */
    private AcceleratingRfGap gapAcclMdl;

    //
    // Computation Properties
    //
    /**
     * phase and energy computation method
     */
    private PHASECALC enmPhsCalcMth = PHASECALC.DYNENERGY;

    //
    // Parent RF Cavity Properties
    // 
    /**
     * = 0 if the gap is part of a 0 mode cavity structure (e.g. DTL), = 1/2 if
     * the gap is part of a pi/2 mode cavity structure = 1 if the gap is part of
     * a pi mode cavity (e.g. Super-conducting)
     */
    private double dblCavModeConst = 0.;

    /**
     * The index of the cavity cell (within the parent cavity) containing this
     * gap.
     */
    private int indCell = 0;

    /*
     * Initialization
     */
    /**
     * Creates a new instance of SpectrumMapRfGap
     *
     * @param strId instance identifier of element
     * @param dblETL field/transit time/length factor for gap (in
     * <strong>volts</strong> )
     * @param dblPhase operating phase of gap (in <strong>radians</strong> )
     * @param dblFreq operating RF frequency of gap (in <strong>Hertz</strong> )
     */
    public SpectrumMapRfGap(String strId, double dblETL, double dblPhase, double dblFreq) {
        super(TYPE, strId);

        this.setETL(dblETL);
        this.setPhase(dblPhase);
        this.setFrequency(dblFreq);
    }

    /**
     * JavaBean constructor - creates a new uninitialized instance of
     * SpectrumMapRfGap <strong>
     * BE CAREFUL</strong>
     */
    public SpectrumMapRfGap() {
        super(TYPE);
    }


    /*
     * Attribute Query
     */
    /**
     * <del>Returns the cell length (m)</del>
     *
     * <p>
     * <strong>CKA</strong> This method never returned the <em>cavity cell</em>
     * length. It always returned the length of the gap within the cell.
     * Fortunately everywhere this method was used it was used in that context.
     * Thus I have changed the name from <code>getCellLength</code> to
     * <code>getGapLength</code>. I have also renamed the corresponding class
     * variable.
     * </p>
     * <p>
     * Of course the gap is being modeled as a thin element and has no length
     * proper. The length <em>L</em> given here is simply the value that
     * produces the appropriate acceleration potential <em>V</em><sub>0</sub> =
     * <em>E</em><sub>0</sub>L when using a hard-edge model.
     * </p>
     * <p>
     * <h4>CKA NOTES:</h4>
     * &middot; I believe this is the length of the overall gap cell structure,
     * not just the gap itself.
     * <br/>
     * &middot; Specifically, it is the distance from one gap center to the next
     * in an accelerating structure.
     * <br/>
     * &middot; I'm not sure if the electric field maximum is averaged over this
     * quantity, I suppose it should be.
     * </p>
     *
     * @return The dblGapLength value
     *
     * @version Jan 15, 2015
     */
    public double getGapLength() {
        return dblGapLength;
    }

    /**
     * Return the displacement of the "effective gap" (i.e., the gap model) from
     * the true center of the actual RF gap center. The true center would be the
     * value provided to the SMF accelerator hierarchy.
     *
     * @return the difference between the RF gap true center and effective
     * center (in meters)
     *
     * @author Christopher K. Allen
     * @since Nov 17, 2014
     */
    public double getGapOffset() {
        return this.gapOffset;
    }


    /*
     * Operations
     */
    /**
     * Compute the wavelength of the RF.
     *
     * @return RF wavelength in <strong>meters</strong>
     */
    public double wavelengthRF() {

        // Compute the RF wavelength
        double c = IElement.LIGHT_SPEED;
        double f = getFrequency();
        double lambda = c / f;

        return lambda;
    }

    /**
     * Compute and return the mid-gap normalized velocity for the given probe.
     *
     * <p>
     * <h4>NOTE:</h4>
     * - Because of the state-dependent nature of the energy calculations (this
     * needs to be fixed), this function will only work correctly if the
     * function energyGain() is consistent.
     * <br/>
     * - CKA: I believe this has been fixed.
     * </p>
     *
     * @param probe probe containing energy information
     *
     * @return average or "mid-gap" velocity in units of <strong>c</strong>
     *
     * @see SpectrumMapRfGap#energyGain(IProbe)
     */
    public double compMidGapBeta(IProbe probe) {

        EnergyVector varMidGap = this.compGapPhaseAndEnergyGain(probe);

        double Er = probe.getSpeciesRestEnergy();
        double W_mid = varMidGap.getEnergy();

        double b_mid = RelativisticParameterConverter.computeBetaFromEnergies(W_mid, Er);

        return b_mid;
    }

    /**
     * <p>
     * Provided for legacy calculations. This method computes the phase of the
     * gap necessary to account for the cavity mode field distribution. For
     * example, if the phase of the probe at the gap is &phi;<sub>0</sub> and
     * the mode amplitude factor for cell/gap <em>n</em> is
     * <em>A<sub>n</sub></em>, then the energy gain
     * &Delta;<em>W<sub>n</sub></em> is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;<em>W<sub>n</sub></em> = <em>qA<sub>n</sub>ETL</em>
     * cos &phi;<sub>0</sub> .
     * <br/>
     * <br/>
     * Combining the <em>A<sub>n</sub></em> and cos &phi;<sub>0</sub> we get
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;<em>W<sub>n</sub></em> = <em>qETL</em> cos
     * (&phi;<sub>0</sub> + <em>nq</em>&pi;) ,
     * <br/>
     * <br/>
     * where <em>q</em> is the cavity mode constant (see
     * <code>{@link #getCavityModeConstant()}</code>). This method returns the
     * cosine argument of the later expression.
     * </p>
     *
     * @param probe probe being propagated through gap
     *
     * @return the combined phase &phi;<sub>0</sub> + <em>nq</em>&pi; combining
     * the propagation time of the probe and the spatial structure of the field
     *
     * @since Jan 15, 2015 by Christopher K. Allen
     */
    public double compEffectivePhaseAtGap(IProbe probe) {
        final int n = this.getCavityCellIndex();
        final double q = this.getCavityModeConstant();

        final double phi_cav = n * q * Math.PI;
        final double phi_prb = this.compGapEntrancePhase(probe);
        final double phi_eff = phi_prb + phi_cav % DBL_2PI;

        return phi_eff;
    }

    /**
     * <p>
     * Get the transverse focusing constant for a particular probe. The focusing
     * constant is used in the construction of the transfer matrix for the RF
     * gap. A gap provides longitudinal focusing and transverse defocusing as
     * well as a gain in beam energy. This focusing constant describes the
     * effect in the transverse direction, which is defocusing when the gap is
     * accelerator and, therefore, typically negative.
     * </p>
     * <p>
     * The value represents the thin lens focusing constant for an ideal RF gap
     * (this is the inverse of the focal length). To compute the focusing action
     * for the lens we must include beam energy, which is changing through the
     * gap. We use the value of beta for which the beam has received half the
     * total energy gain.
     * </p>
     *
     * @param probe beam energy and particle charge are taken from the probe
     * @return (de)focusing constant (<strong>in radians/meter</strong> )
     */
    public double compTransFocusing(IProbe probe) {

        // TODO - this is the full energy and phase gain
        EnergyVariables varMidGap = this.compMidGapPhaseAndEnergy(probe);
        double W_mid = varMidGap.W;
        double phi_mid = varMidGap.phi;

        double c = IElement.LIGHT_SPEED;

        double Q = Math.abs(probe.getSpeciesCharge());
        double Er = probe.getSpeciesRestEnergy();

        double g_mid = W_mid / Er + 1.0;
        double b_mid = RelativisticParameterConverter.computeBetaFromEnergies(W_mid, Er);
        double bg_mid = b_mid * g_mid;

        double E = this.getE0();
        double f = this.getFrequency();
        double k_mid = RelativisticParameterConverter.computeWavenumberFromBeta(b_mid, f);
        double T_mid = this.spcGapFlds.Tz(k_mid);
        double L = this.getLength();

        double A = this.compCavModeFieldCoeff();
        double ETL = E * T_mid * L;

        double kr = (Math.PI * f / c) * Q * A * ETL * Math.sin(-phi_mid) / (Er * bg_mid * bg_mid);

        return kr;
    }

    /**
     * <p>
     * Get the longitudinal focusing constant for a particular probe. The
     * focusing constant is used in the construction of the transfer matrix for
     * the RF gap. A gap provides longitudinal focusing and transverse
     * defocusing as well as a gain in beam energy. This focusing constant
     * describes the effect in the longitudinal direction, which is focusing
     * and, therefore, positive.
     * </p>
     * <p>
     * The value represents the thin lens focusing constant for an ideal RF gap
     * (this is the inverse of the focal length). To compute the focusing action
     * for the lens we must include beam energy, which is changing through the
     * gap. We use the value of beta for which the beam has received half the
     * total energy gain.
     * </p>
     *
     * @param probe beam energy and particle charge are taken from the probe
     * @return (de)focusing constant (<strong>in radians/meter</strong> )
     */
    public double compLongFocusing(IProbe probe) {

        EnergyVariables varMidGap = this.compMidGapPhaseAndEnergy(probe);

        double Wbar = varMidGap.W;
        double Er = probe.getSpeciesRestEnergy();
        double gbar = Wbar / Er + 1.0;

        double kr = this.compTransFocusing(probe);
        double kz = -2.0 * kr * gbar * gbar;

        return kz;
    }

    /*
     *  IRfGap Interface
     */
    /**
     * Return the ETL product of the gap, where E is the longitudinal electric
     * field, T is the transit time factor, and L is the gap length.
     *
     * @return the ETL product of the gap (in <bold>volts</bold> ).
     */
    @Override
    public double getETL() {
        return dblETL;
    }

    /**
     * Return the RF phase delay of the gap with respect to the synchronous
     * particle.
     *
     * @return phase delay w.r.t. synchronous particle (in <bold>radians</bold>
     * ).
     */
    @Override
    public double getPhase() {
        return dblPhase;
    }

    /**
     * Get the operating frequency of the RF gap.
     *
     * @return frequency of RF gap (in <bold>Hertz</bold> )
     */
    @Override
    public double getFrequency() {
        return dblFreq;
    }

    /**
     * Set the ETL product of the RF gap where E is the longitudinal electric
     * field of the gap, T is the transit time factor of the gap, L is the
     * length of the gap.
     * <p>
     *
     * The maximum energy gain from the gap is given by qETL where q is the
     * charge (in coulombs) of the species particle.
     *
     * @param dblETL ETL product of gap (in <bold>volts</bold> ).
     */
    @Override
    public void setETL(double dblETL) {
        this.dblETL = dblETL;
    }

    /**
     * Set the phase delay of the RF in gap with respect to the synchronous
     * particle. The actual energy gain from the gap is given by qETLcos(dblPhi)
     * where dbkPhi is the phase delay.
     *
     * @param dblPhase phase delay of the RF w.r.t. synchronous particle (in
     * <bold>radians</bold> ).
     */
    @Override
    public void setPhase(double dblPhase) {
        this.dblPhase = dblPhase;
    }

    /**
     * Set the operating frequency of the RF gap.
     *
     * @param dblFreq frequency of RF gap (in <bold>Hertz</bold> )
     */
    @Override
    public void setFrequency(double dblFreq) {
        this.dblFreq = dblFreq;
    }

    /**
     * Set the on accelerating field E - the on axis field (V/m)
     *
     * @param E The new E0 value
     */
    @Override
    public void setE0(double E) {
        dblFieldE0 = E;
        this.resetGapPotential();
    }

    /**
     * Get the on accelerating field (V/m)
     *
     * @return The e0 value
     */
    @Override
    public double getE0() {
        return dblFieldE0;
    }

    /*
     * IRfCavityCell Interface
     */

 /*
     * Attribute Query
     */
    /**
     * return whether this gap is the initial gap of a cavity
     *
     * @return The firstGap value
     */
    @Override
    public boolean isFirstGap() {
        return bolStartCell;
    }

    /**
     *
     * @see xal.model.elem.sync.IRfCavityCell#setCavityCellIndex(int)
     *
     * @since Jan 8, 2015 by Christopher K. Allen
     */
    @Override
    public void setCavityCellIndex(int indCell) {
        this.indCell = indCell;
    }

    /**
     *
     * @see xal.model.elem.sync.IRfCavityCell#setCavityModeConstant(double)
     *
     * @since Jan 8, 2015 by Christopher K. Allen
     */
    @Override
    public void setCavityModeConstant(double dblCavModeConst) {
        this.dblCavModeConst = dblCavModeConst;
    }

    /**
     *
     * @see xal.model.elem.sync.IRfCavityCell#getCavityCellIndex()
     *
     * @since Jan 8, 2015 by Christopher K. Allen
     */
    @Override
    public int getCavityCellIndex() {
        return this.indCell;
    }

    /**
     * <p>
     * Returns the structure mode <strong>number</strong> <em>q</em> for the
     * cavity in which this gap belongs. Here the structure mode number is
     * defined in terms of the fractional phase advance between cells in units
     * of &pi;. To make this explicit
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>q</em> = 0 &nbsp; &nbsp; &rAarr; 0 mode
     * <br/>
     * &nbsp; &nbsp; <em>q</em> = 1/2 &rArr; &pi;/2 mode
     * <br/>
     * &nbsp; &nbsp; <em>q</em> = 1 &nbsp; &nbsp; &rAarr; &pi; mode
     * <br/>
     * <br/>
     * Thus, a cavity mode constant of <em>q</em> = 1/2 indicates a &pi;/2 phase
     * advance between adjacent cells and a corresponding cell amplitude
     * function <em>A<sub>n</sub></em> of
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>A<sub>n</sub></em> = cos(<em>nq</em>&pi;)
     * <br/>
     * <br/>
     * where <em>n</em> is the index of the cell within the coupled cavity.
     * </p>
     *
     * @return the cavity mode constant for the cell containing this gap
     *
     * @see <em>RF Linear Accelerators</em>, Thomas P. Wangler (Wiley, 2008).
     *
     * @author Christopher K. Allen
     * @since Nov 20, 2014
     */
    @Override
    public double getCavityModeConstant() {
        return this.dblCavModeConst;
    }

    /**
     * Returns flag indicating whether or not this gap is in the initial or
     * terminal cell in a string of cells within an RF cavity.
     *
     * @return     <code>true</code> if this gap is in a cavity cell at either end
     * of a cavity cell bank, <code>false</code> otherwise
     *
     * @since Jan 23, 2015 by Christopher K. Allen
     */
    @Override
    public boolean isEndCell() {
        return this.bolEndCell;
    }

    /**
     *
     * @see xal.model.elem.sync.IRfCavityCell#isFirstCell()
     *
     * @since Jan 23, 2015 by Christopher K. Allen
     */
    @Override
    public boolean isFirstCell() {
        return this.bolStartCell;
    }


    /*
     *  IElement Interface
     */
    /**
     * Returns the time taken for the probe to propagate through element.
     * <br/>
     * <br/>
     * TODO Need to correct this after computing
     * (&Delta;&phi;,&Delta;<em>W</em>)
     *
     * @param probe propagating probe
     *
     * @return The time taken to propagate through gap including phase shift
     * &delta;&phi; and any gap offset &Delta;<em>l</em> (asymmetric drifting at
     * initial and final energies)
     */
    @Override
    public double elapsedTime(IProbe probe) {

        // Get the phase jump across the gap (if any)
        EnergyVector varGain = this.compGapPhaseAndEnergyGain(probe);

        double d_phi = varGain.getPhase();

        // Compute the time necessary for a smooth propagation through that phase jump
        double f = this.getFrequency();
        double w = DBL_2PI * f;
        double dT = d_phi / w;

        return dT;
    }

    /**
     * Compute the energy gain of the RF gap for a probe including the effects
     * of calculating the phase advance.
     *
     * @param probe uses the particle species charge
     *
     * @return energy gain for this probe (<strong>in electron-volts</strong> )
     */
    @Override
    public double energyGain(IProbe probe) {
        double dW = this.compGapPhaseAndEnergyGain(probe).getEnergy();
        return dW;
    }

    /**
     * Compute and return the longitudinal phase advance (w.r.t. the RF) for the
     * given probe while propagating through this element.
     *
     * @see
     * xal.model.elem.ThinElement#longitudinalPhaseAdvance(xal.model.IProbe)
     *
     * @author Christopher K. Allen
     * @since Nov 23, 2014
     */
    @Override
    public double longitudinalPhaseAdvance(IProbe probe) {

        // We trick the algorithm into resetting the probe's phase to the phase
        //  of this gap, which is the klystron phase of this cavity
        if (this.isFirstGap()) {
            double phi0 = this.getPhase();
            double phi = probe.getLongitinalPhase();
            double dphi = this.compGapPhaseAndEnergyGain(probe).getPhase();

            double phi_reset = -phi + phi0 + dphi;

            return phi_reset;

            // We're just a plain ole gap, advance the probe phase by the phase gain
        } else {

            double dphi = this.compGapPhaseAndEnergyGain(probe).getPhase();

            return dphi;
        }
    }

    /**
     * Compute the transfer map for an ideal RF gap.
     *
     * @param probe compute transfer map using parameters from this probe
     *
     * @return transfer map for the probe
     *
     * @exception ModelException this should not occur
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws ModelException {
        // Get probe parameters at initial energy
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();
        double bi = probe.getBeta();
        double gi = probe.getGamma();

        // Determine the current energy gain and focusing constants for the gap
        // the following section is to calculate the phase of the beam at each gap, rather than use hardwired phases.
        // update the energy gain first:
        double dW = this.compGapPhaseAndEnergyGain(probe).getEnergy();
        double kz = this.compLongFocusing(probe);
        double kt = this.compTransFocusing(probe);

        // Compute final energy parameters
        double Wf = Wi + dW;
        double gf = Wf / Er + 1.0;
        double bf = Math.sqrt(1.0 - 1.0 / (gf * gf));

        // Compute component block matrices then full transfer matrix
        double arrTranX[][] = new double[][]{{1.0, 0.0}, {kt / (bf * gf), bi * gi / (bf * gf)}};
        double arrTranY[][] = new double[][]{{1.0, 0.0}, {kt / (bf * gf), bi * gi / (bf * gf)}};

        // CKA - Corrected 7/14/2010
        //  Additional factor gbar^2 in the longitudinal focusing term 
        double arrLong[][] = new double[][]{{1.0, 0.0}, {kz / (bf * gf * gf * gf), gi * gi * gi * bi / (gf * gf * gf * bf)}};

        PhaseMatrix matPhi = new PhaseMatrix();

        matPhi.setElem(6, 6, 1.0);
        matPhi.setSubMatrix(0, 1, 0, 1, arrTranX);
        matPhi.setSubMatrix(2, 3, 2, 3, arrTranY);
        matPhi.setSubMatrix(4, 5, 4, 5, arrLong);

        // Jan 2019 - Natalia Milas
        // apply alignment and rotation errors
        matPhi = applyErrors(matPhi, getGapLength());

        return new PhaseMap(matPhi);
    }

    /*
     * IComponent Interface
     */
    /**
     * Conversion method to be provided by the user
     *
     * <p>
     * <h4>NOTES - CKA</h4>
     * &middot; The accelerating gap potential is set for a unit charge. It is
     * later updated for whatever charge the probe carries when energy and phase
     * calculations are made. See {@link #compGapPhaseAndEnergyGain(IProbe)}
     * where {@link #gapAcclMdl} is modified.
     * <br/>
     * <br/>
     * &middot; Someday we really need to remove this dependency with SMF.
     * </p>
     *
     * @param latticeElement the SMF node to convert
     *
     * @see #compGapPhaseAndEnergyGain(IProbe)
     */
    @Override
    public void initializeFrom(LatticeElement element) {
        super.initializeFrom(element);

        RfGap rfgap = (RfGap) element.getHardwareNode();

        // Initialize the RF gap properties
        this.bolStartCell = rfgap.isFirstGap();
        this.bolEndCell = rfgap.isEndCell();
        this.dblGapLength = rfgap.getGapLength();
        this.gapOffset = rfgap.getGapOffset();

        // Initialize the RF cavity properties
        this.dblCavModeConst = rfgap.getStructureMode();

        // the SMF object uses kV (stupid)
        this.dblFieldE0 = rfgap.getGapDfltAmp() * 1.0e3;
        // the SMF object uses MHz (stupid)
        this.dblFreq = rfgap.getGapDfltFrequency() * 1.0e6;

        // Create the defining axial field spectrum object
        IRealFunction fitTTFPrime = rfgap.getTTFPrimeFit();
        IRealFunction fitTTF = rfgap.getTTFFit();
        IRealFunction fitSTFPrime = rfgap.getSPrimeFit();
        IRealFunction fitSTF = rfgap.getSFit();

        this.spcGapFlds = new AxialFieldSpectrum(this.dblFreq, this.gapOffset, fitTTF, fitTTFPrime, fitSTF, fitSTFPrime);

        // Create the accelerating gap model
        //  The accelerating gap potential is set for a unit charge.  It is later updated
        //  for whatever charge the probe carries when energy and phase calculations are made.
        double E0 = this.getE0();
        double A = this.compCavModeFieldCoeff();
        double L = this.getGapLength();
        // This is for a unit charge 
        double V0 = A * E0 * L;

        this.gapAcclMdl = new AcceleratingRfGap(this.dblFreq, V0, this.spcGapFlds);
    }

    /*
     *  Object Overrides
     */
    /**
     *
     * @see xal.model.elem.Element#toString()
     *
     * @since Jan 22, 2015 by Christopher K. Allen
     */
    @Override
    public String toString() {
        StringBuilder bufOut = new StringBuilder();

        bufOut.append(super.toString());

        bufOut.append("  Gap ETL product    : ").append(this.getETL());
        bufOut.append('\n');
        bufOut.append("  Gap phase shift    : ").append(this.getPhase());
        bufOut.append('\n');

        bufOut.append("  RF frequency       : ").append(this.getFrequency());
        bufOut.append('\n');

        bufOut.append("  Axial field dblFieldE0     : ").append(this.getE0());
        bufOut.append('\n');

        bufOut.append("  Gap offset         : ").append(this.getGapOffset());
        bufOut.append('\n');

        return bufOut.toString();
    }

    /**
     * Dump current state and content to output stream.
     *
     * @param os output stream object
     */
    @Override
    public void print(PrintWriter os) {
        super.print(os);

        os.println("  Gap ETL product    : " + this.getETL());
        os.println("  Gap phase shift    : " + this.getPhase());
        os.println("  RF frequency       : " + this.getFrequency());
        os.println("  Axial field dblFieldE0     : " + this.getE0());
    }

    /*
     *  Support Methods
     */
    /**
     * <p>
     * Computes and returns the electric field coefficient associated with the
     * RF cavity cell containing this gap. Specifically, if this gap belongs to
     * the <em>n<sup>th</sup></em> cell of the cavity (index origin 0) and the
     * cavity operates in the <em>q</em>&pi;-mode where <em>q</em> is the cavity
     * mode constant, then the field coefficient <em>A<sub>n</sub></em> is
     * defined
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>A<sub>n</sub></em> &#8796; cos(<em>nq</em>&pi;) ,
     * <br/>
     * <br/>
     * so that the electric field <em>E<sub>n</sub></em>(<em>z</em>,<em>t</em>)
     * at cell <em>n</em>
     * is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <em>E<sub>n</sub></em>(<em>z</em>,<em>t</em>) =
     * <em>A<sub>n</sub></em> <em>E</em><sub>0</sub>(<em>z</em>)
     * cos(&omega;<em>t</em> + &phi;<sub>0</sub>) ,
     * <br/>
     * <br/>
     * where <em>E</em><sub>0</sub>(<em>z</em>) is the axial field profile of
     * each cell.
     * </p>
     *
     * @return the cavity field coefficient <em>A<sub>n</sub></em> for this gap
     *
     * @since Jan 12, 2015 by Christopher K. Allen
     */
    private double compCavModeFieldCoeff() {
        final int n = this.getCavityCellIndex();
        final double q = this.getCavityModeConstant();

        final double A = Math.cos(n * q * Math.PI);

        return A;
    }

    /**
     * <p>
     * Resets the total potential gain across the gap. This is the integral of
     * the axial electric field <em>E<sub>z</sub></em>(<em>z</em>) through the
     * entire gap region.
     * </p>
     * <p>
     * This method needs to be called whenever the gap length or gap potential
     * are changed. The effects is to change the magnitude of the spectral maps
     * that define the gap fields.
     * </p>
     *
     * @since Oct 16, 2015, Christopher K. Allen
     */
    private void resetGapPotential() {

        // Create the accelerating gap model
        //  The accelerating gap potential is set for a unit charge.  It is later updated
        //  for whatever charge the probe carries when energy and phase calculations are made.
        double E0 = this.getE0();
        double A = this.compCavModeFieldCoeff();
        double L = this.getGapLength();
        // This is for a unit charge 
        double V0 = A * E0 * L;

        this.gapAcclMdl = new AcceleratingRfGap(this.dblFreq, V0, this.spcGapFlds);
    }

    /**
     * <p>
     * Computes and returns the phase at the position of the gap's
     * <strong>electrical entrance</strong>. The longitudinal phase of the given
     * probe is assumed to be at the gap's geometric entrance as it arrives, a
     * correction is necessary if the geometric and electrical centers are
     * offset.
     * </p>
     * <p>
     * <h4>NOTE</h4>
     * &middot; It is essential that the probe has the correct phase. If this is
     * the first gap then that phase must be the phase of the cavity, given by
     * <code>{@link #getPhase()}</code>. Thus, we must set the probe phase to
     * the phase of the cavity at these special gaps. (This is unfortunately
     * necessary since we cannot implement RF cavity elements with the current
     * lattice generator.) In the Element/Algorithm/Probe architecture elements
     * do not modify probes. Thus, although it's a kluge no matter which way you
     * do it, the probe phase should be reset by the algorithm.
     * <br/>
     * <br/>
     * &middot; To get the phase at the middle of the gap (again, electrical
     * middle) you can call
     * <code>{@link #compGapPhaseAndEnergyImpulses(IProbe)}</code> and add
     * <strong>half</strong> that value to this method's returned value.
     * </p>
     *
     * @param probe probe containing phase information
     *
     * @return the probe phase corrected as necessary for any gap offset
     *
     * @since Nov 26, 2014, Christopher K. Allen
     */
    private double compGapEntrancePhase(IProbe probe) {

        // Get the phase of the probe at the gap geometric center
        double phi0 = this.isFirstGap() ? this.getPhase() : probe.getLongitinalPhase();

        double bi = probe.getBeta();
        double dl = this.getGapOffset();
        double dphi = this.compDriftingPhaseAdvance(bi, dl);

        double phi = phi0 + dphi;

        // Correct the phase as needed for any difference from electrical center according
        //  to the simulation mode we are using
        switch (this.enmPhsCalcMth) {
            case DESIGN:
                return phi0;

            case DYNPHASE:
                return phi;

            case DYNENERGY:
                return phi0;

            default:
                return phi0;
        }
    }

    /**
     * <p>
     * Returns the advance in RF phase for a particle drifting at velocity
     * &beta; for a distance <em>l</em>. The value is simply the time of flight
     * &Delta;<em>t</em> needed to propagate the distance <em>l</em> times the
     * angular frequency &omega; &#8796 2&pi;<em>f</em>
     * of the cavity (<em>f</em> is the fundamental cavity frequency).
     * Specifically,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;&phi; = &omega;&Delta;<em>t</em> =
     * &omega;<em>l</em>/&beta;<em>c</em>
     * = (2&pi;/&beta;&lambda;)l = <em>kl</em>
     * <br/>
     * <br/>
     * where &Delta;&phi; is the change in phase due to the offset, &lambda; is
     * the wavelength of the RF, and <em>k</em> is the wave number of the
     * particle.
     *
     * @param probe probe object arriving at the gap
     *
     * @return the value &Delta;&phi; from a particle drifting at velocity
     * &beta; for distance <em>l</em>
     *
     * @author Christopher K. Allen
     * @since Nov 19, 2014
     */
    private double compDriftingPhaseAdvance(double beta, double len) {

        // speed of light
        double c = IElement.LIGHT_SPEED;
        double f = this.getFrequency();

        //the correction for the gap offset needed
        double dt = len / (beta * c);

        double dphi = DBL_2PI * f * dt;

        return dphi;
    }

    /**
     * Computes and returns the phase change due to any gap offset between the
     * geometric center and the electrical center. The phase change &Delta;&phi;
     * is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;&phi; = &omega;
     * &Delta;<em>z</em>(1/&beta;<sub><em>i</sub>c</em> -
     * 1/&beta;<sub><em>f</sub>c</em>) = (<em>k<sub>i</sub></em> -
     * <em>k<sub>f</sub></em>)&Delta;<em>z</em> ,
     * <br/>
     * <br/>
     * where &omega; is the angular frequency of the RF, &Delta;<em>z</em> is
     * the gap offset, &beta;<sub><em>i</em></sub> is the pre-gap velocity,
     * &beta;<sub><em>f</em></sub>
     * is the post-gap velocity, <em>k<sub>i</sub></em> is the pre-gap wave
     * number, and <em>k<sub>f</sub></em>
     * is the post-gap wave number. Note that the phase change &Delta;&phi; can
     * be negative if the offset <em>l</em> is toward the downstream direction.
     *
     * @param beta_i the pre-gap velocity &beta;<sub><em>i</em></sub>
     * @param beta_f the post-gap velocity &beta;<sub><em>f</em></sub>
     *
     * @return
     *
     * @since Jan 13, 2015 by Christopher K. Allen
     */
    private double compGapOffsetPhaseChange(double beta_i, double beta_f) {

        // TODO This isn't right.  Think about the offset and the total gap length
        double dl = this.getGapOffset();
        double dphi_i = this.compDriftingPhaseAdvance(beta_i, dl);
        double dphi_f = this.compDriftingPhaseAdvance(beta_f, dl);

        double dphi = dphi_i - dphi_f;

        return dphi;
    }

    /**
     * Computes the phase &phi;<sub>mid</sub> and energy
     * <em>W</em><sub>mid</sub>
     * of the probe at the middle of the gap. The returned values are given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &phi;<sub>mid</sub> = &phi;<sub>0</sub> + &delta;&phi;/2,
     * <br/>
     * &nbsp; &nbsp; <em>W</em><sub>mid</sub> = <em>W</em><sub>0</sub> +
     * &Delta;<em>W</em>/2
     * <br/>
     * <br/>
     * where &phi;<sub>0</sub> is the probe phase at the gap entrance and
     * <em>W</em><sub>0</sub> is the probe energy at the gap entrance;
     *
     * @param probe probe propagating through the gap
     *
     * @return mid-gap longitudinal phase coordinates
     * (&phi;<sub>mid</sub>,<em>W</em><sub>mid</sub>)
     *
     * @since Jan 13, 2015 by Christopher K. Allen
     *
     * @see #compGapPhaseAndEnergyImpulses(IProbe)
     */
    private EnergyVariables compMidGapPhaseAndEnergy(IProbe probe) {

        // Get the phase and energy at the entrance of the gap
        double W_i = probe.getKineticEnergy();
        double phi_i = this.compGapEntrancePhase(probe);

        EnergyVector varDelVals = this.compGapPhaseAndEnergyGain(probe);
        double d_W = varDelVals.getEnergy();
        double d_phi = varDelVals.getPhase();

        // Create the longitudinal phase variable object to return
        double W = W_i + d_W / 2.0;
        double phi = phi_i + d_phi / 2.0;

        EnergyVariables varMidGap = new EnergyVariables(phi, W);

        return varMidGap;
    }

    /**
     * <p>
     * Computes and returns the change in the probe's phase and energy due to
     * the RF gap. This action here is primarily that of deletion. If the
     * <code>useRfGapPhaseCalculation</code> flag of the probe's algorithm
     * object is set to <code>false</code>, then the phase and energy change is
     * computed with method
     * <code>{@link #compGapPhaseAndEnergyGainDirect(IProbe)}</code>. If the
     * flag is <code>true</code> then the calculation is done using
     * <code>{@link #compGapPhaseAndEnergyGainIndirect(IProbe)}</code>.
     * </p>
     * <p>
     * Also in the case where <code>useRfGapPhaseCalculation</code> =
     * <code>false</code> the phase change due to the gap offset &delta;&phi; is
     * added to the returned phase gain &Delta;&phi;. Specifically, we compute
     * &delta;&phi; =
     * <code>{@link #compGapOffsetPhaseChange(double, double)}</code> then add
     * it to &Delta;&phi;. This action is actually done in the method
     * <code>{@link #compGapPhaseAndEnergyGainDirect(IProbe)}</code> which is
     * called by this method for the aforementioned case.
     * </p>
     *
     * @param probe the probe experiencing the change in energy and phase
     *
     * @return phase and energy gain for the given probe imparted by this gap
     *
     * @since Jan 14, 2015 by Christopher K. Allen
     */
    private EnergyVector compGapPhaseAndEnergyGain(IProbe probe) throws AcceleratingRfGap.NoConvergenceException {

        // If the algorithm doesn't want to use the dynamic phase and energy
        //  gain calculations then we default to the design mode and return
        if (probe.getAlgorithm().getRfGapPhaseCalculation() == false) {
            EnergyVector varGain = this.compGapPhaseAndEnergyGainDirect(probe);

            return varGain;
        }

        // Switch on the type of calculation mode we are using
        //
        //  Maybe we have the useRfGapPhaseCalculation flag in the probe??!!
        switch (this.enmPhsCalcMth) {

            case DESIGN:
                return this.compGapPhaseAndEnergyGainDesign(probe);

            case DYNPHASE:
                return this.compGapPhaseAndEnergyGainDirect(probe);

            case DYNENERGY:
                return this.compGapPhaseAndEnergyGainIndirect(probe);

            default:
                return this.compGapPhaseAndEnergyGainDesign(probe);

        }
    }

    /**
     * This is a simple "design code" computation of the phase and energy gain.
     * We return the results of the Panofsky equation for the design phase
     * ignoring the cavity modes. There is no change in phase.
     *
     * @param probe we are computing the gap effects for this probe
     *
     * @return the increment energy due to gap using the design model
     *
     * @since Jan 14, 2015 by Christopher K. Allen
     */
    private EnergyVector compGapPhaseAndEnergyGainDesign(IProbe probe) {

        // Compute the energy gain without corrections
        double Q = Math.abs(probe.getSpeciesCharge());
        double ETL = this.getETL();
        double phi0 = this.getPhase();

        double dW = Q * ETL * Math.cos(phi0);
        double dphi = 0.0;

        EnergyVector varGain = new EnergyVector(dphi, dW);

        return varGain;
    }

    /**
     * We just return the results from the Panofsky equation but use the probe
     * phase at the electrical center of the gap (with no corrective impulse).
     * The electrical center phase is determined by a call to the method
     * <code>{@link #compGapEntrancePhase(IProbe)}</code> which accounts for
     * propagation to the gap electrical center. The Panofsky equation then
     * provides the energy gain and the method
     * <code>{@link #compGapOffsetPhaseChange(double, double)}</code> provides
     * the change in phase.
     *
     * @param probe we are computing the gap effects for this probe
     *
     * @return the increments in phase and energy due to gap using the simple
     * model
     *
     * @since Jan 14, 2015 by Christopher K. Allen
     */
    private EnergyVector compGapPhaseAndEnergyGainDirect(IProbe probe) {

        // Compute the energy gain without corrections
        double Q = Math.abs(probe.getSpeciesCharge());
        double ETL = this.getETL();
        double A = this.compCavModeFieldCoeff();
        double phi0 = this.compGapEntrancePhase(probe);

        double dW = Q * A * ETL * Math.cos(phi0);

        // Now we add in the change in phase due to any offset in the gap
        //  electrical center from the geometric center
        //  We only do this if the S() transit time factor was not used to compute
        //  the phase and energy gains
        double Er = probe.getSpeciesRestEnergy();
        double W_i = probe.getKineticEnergy();
        double W_f = W_i + dW;

        double b_i = probe.getBeta();
        double b_f = RelativisticParameterConverter.computeBetaFromEnergies(W_f, Er);

        double dphi = this.compGapOffsetPhaseChange(b_i, b_f);

        EnergyVector varGain = new EnergyVector(dphi, dW);

        return varGain;
    }

    /**
     * <p>
     * Computes and returns the longitudinal phase change &delta;&phi; energy
     * gain &Delta;<em>W</em> through the gap due to acceleration. Although this
     * is a thin lens the longitudinal phase must have a impulsive change
     * symplectically conjugate to the change &Delta;<em>W</em> in longitudinal
     * energy.
     * </p>
     * <p>
     * The calculation is done using a fixed-point search on formulas for the
     * gap phase change &delta;&phi; and energy gain &Delta;<em>W</em> which are
     * taking from Lapostolle and Weiss's <em>Formulae for Linear
     * Accelerators</em>
     * CERN PS-2000-01.
     * </p>
     * <p>
     * <h4>CKA NOTES</h4>
     * &middot; The strategy is to compute the mid-gap velocity
     * &beta;<em><sub>mid</sub></em>
     * and phase change &delta;&phi;<em><sub>mid</sub></em>. All other gap
     * parameters can be computed from these values.
     * <br/>
     * <br/>
     * &middot; We start with the phase of the probe at the entrance of the gap,
     * call it &phi;<sub>0</sub>, see
     * <code>{@link #compGapEntrancePhase(IProbe)}</code>. Then all the initial
     * parameters for the loop, i.e., &Delta;<em>W</em><sub>0</sub>,
     * &beta;<sub>0</sub>, <em>T</em>(&beta;<sub>0</sub>),
     * <em>S</em>(&beta;<sub>0</sub>), etc., are computed from that value.
     * <br/>
     * <br/>
     * &middot; The values are computed with the (maybe naive) assumption that
     * the mid-gap phase change &delta;&phi;<em><sub>mid</sub></em> is equal to
     * half the total gap phase change &delta;&phi;, or
     * &delta;&phi;<em><sub>mid</sub></em> = &delta;&phi;/2.
     * <br/>
     * <br/>
     * &middot; We also assume (perhaps more accurately) that the mid-gap energy
     * gain &Delta;<em>W</em><sub><em>mid</em></sub> is half the total energy
     * gain &Delta;<em>W</em>, or &Delta;<em>W</em><sub><em>mid</em></sub> =
     * &Delta;<em>W</em>/2.
     * <br/>
     * <br/>
     * &middot; I am avoiding the use of <code>fitTTFprime</code> and
     * <code>fitSTFPrime</code> because I do not know what values they
     * represent. That is, are they
     * <em>dT</em>(&beta;)</em>/<em>d</em>&beta; or
     * <em>dT</em>(&beta;)/<em>dk</em>?
     * </p>
     *
     * @param probe probe propagating through gap, we use its phase and energy
     * parameters
     *
     * @return the change in phase &delta;&phi; and energy &Delta; <em>W</em> of
     * the give probe through the gap
     *
     * @author Christopher K. Allen
     * @since Nov 26, 2014
     */
    private EnergyVector compGapPhaseAndEnergyGainIndirect(IProbe probe) throws AcceleratingRfGap.NoConvergenceException {

        // Initial probe parameters
        double Q = Math.abs(probe.getSpeciesCharge());
        double Er = probe.getSpeciesRestEnergy();
        double bi = probe.getBeta();
        double Wi = probe.getKineticEnergy();

        //
        //  IMPORTANT!!!!
        //
        //  TODO: We need to check that the probe is giving the correct phase here.
        //        The phase must be set with respect to the first gap 
        //
        // Compute the phase axis intercept of the asymptotic form of phase advance
        //  entering the gap, i.e., phi_0^minus
        // phase at gap "electrical" entrance
        double phi0m = this.compGapEntrancePhase(probe);

        // phase change from center correction factor for future time calculations
        // in PARMILA TTFPrime and SPrime are in [1/cm] units, we use [m]
        //  CKA - I will try to eliminate these quantities because I cannot
        //        determine that they are correct
        // Gap parameters
        double E0 = this.getE0();
        double A = this.compCavModeFieldCoeff();
        double L = this.getGapLength();
        double qAEL = Q * A * E0 * L;

        //
        // TODO CKA - I BELIEVE T and S in the XDXF files are in centimeters!!!!
        //            That is they are dT(b)/dk and dS(b)/dk NEED TO DEAL WITH THIS!!!
        // CKA - Now I believe the objective is to compute the mid-gap wave number k_mid
        //       and phase change d_phi. All other gap parameters can be computed
        //       from these values.
        // Initialize the search 
        //
        EnergyVector vecInitVals = new EnergyVector(phi0m, Wi);

        try {

            // Compute the phase variable gain in the pre-gap region
            EnergyVector vecPreGapGains = this.gapAcclMdl.computeGapGains(LOC.PREGAP, Q, Er, vecInitVals);

            double dphim = A * vecPreGapGains.getPhase();
            double dWm = A * vecPreGapGains.getEnergy();

            // Form the longitudinal phase and energy at the gap center
            double phi0 = phi0m + dphim;
            double W0 = Wi + dWm;

            EnergyVector vecMidVals = new EnergyVector(phi0, W0);

            // Compute the post-gap phase variable changes
            EnergyVector vecPostGapGains = this.gapAcclMdl.computeGapGains(LOC.POSTGAP, Q, Er, vecMidVals);

            double dphip = A * vecPostGapGains.getPhase();
            double dWp = A * vecPostGapGains.getEnergy();

            // Form the Longitudinal phase variables in the post gap region
            double phi0p = phi0 + dphip;
            double Wf = W0 + dWp;

            EnergyVector vecEndVals = new EnergyVector(phi0p, Wf);

            // Compute the total gains for the gap
            double dphi = dphim + dphip;
            double dW = dWm + dWp;

            EnergyVector vecGapGains = new EnergyVector(dphi, dW);

            double ki = DBL_2PI / (bi * IElement.LIGHT_SPEED / this.getFrequency());
            double b_mid = RelativisticParameterConverter.computeBetaFromEnergies(W0, Er);
            double k_mid = DBL_2PI / (b_mid * IElement.LIGHT_SPEED / this.getFrequency());

            double theEnergyGain = qAEL * this.spcGapFlds.Tz(k_mid) * Math.cos(phi0 + dphim);
            double deltaPhaseCorrection = Q * A * this.gapAcclMdl.computeNormWaveNumber(W0 + dWm, Er) * this.spcGapFlds.dkTz(ki) * Math.sin(phi0 + dphim);

            // TODO - Remove this old XAL reproduction stuff if we are going to production 
            EnergyVector vecCrapGains = new EnergyVector(deltaPhaseCorrection, theEnergyGain);

            return vecCrapGains;

        } catch (AcceleratingRfGap.NoConvergenceException e) {
            System.err.println("WARNING! SpectrumMapRfGap#compGapPhaseAndEnergyGain() did not converge for element " + this.getId());

            throw e;
        }
    }
}
