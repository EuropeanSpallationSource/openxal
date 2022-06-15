/*
 *  IdealRfGap.java
 *
 *  Created on October 22, 2002, 1:58 PM
 */
package xal.extension.jels.model.elem;

import java.io.PrintWriter;
import xal.extension.jels.smf.impl.ESSRfGap;

import xal.extension.jels.tools.math.InverseRealPolynomial;
import xal.extension.jels.tools.math.MeanFieldPolynomial;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ThinElement;
import xal.model.elem.sync.IRfCavityCell;
import xal.model.elem.sync.IRfGap;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

/**
 * Represents the action of an ideal RF gap. Gap is modeled as a thin element
 * whose accelerating action is given by the Panofsky formula. </p>
 * <p>
 *
 * The gap provides acceleration to the propagation probe as well as
 * longitudinal focusing and radial defocusing. These mechanisms are implemented
 * according to that provided by an ideal gap where the effects can be described
 * analytically. </p>
 *
 * @author Christopher K. Allen
 * @created November 22, 2005
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class IdealRfGap extends ThinElement implements IRfGap, IRfCavityCell {

    /*
     *  Global Constants
     */
    /**
     * the string type identifier for all IdealRfGap objects
     */
    public static final String TYPE = "JELS.IdealRfGap";

    /*
     *  Defining Attributes
     */

    /**
     * ETL product of gap
     */
    private double dblETL = 0.0;

    /**
     * phase delay of gap w.r.t. the synchronous particle
     */
    private double dblPhase = 0.0;

    /**
     * operating frequency of the gap
     */
    private double dblFreq = 0.0;

    /**
     * the on axis accelerating field (V)
     */
    private double e0 = 0.;

    /**
     * the accelerating cell length
     */
    private double cellLength = 0.;

    /**
     * = 0 if the gap is part of a 0 mode cavity structure (e.g. DTL) = 1 if the
     * gap is part of a pi mode cavity (e.g. CCL, Superconducting)
     */
    private double structureMode = 0;

    /**
     * fit of the TTF vs. beta
     */
    protected InverseRealPolynomial ttfFit;

    /**
     * the energy gained in this gap (eV)
     */
    private double energyGain;

    /**
     * the phase kick correction applied at the gap center [rad]
     */
    private double deltaPhi;

    /**
     * flag indicating that this gap is in the leading cell of an RF cavity
     */
    private boolean bolStartCell = false;

    /**
     * flag indicating that this gap is in the end cell of an RF cavity
     */
    private boolean bolEndCell = false;

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

    private double dblAmpFactor;
    private double dblPhaseFactor;
    private double synchronousPhase;
    private double longitudinalPhaseReference;

    /*
     * Initialization
     */
    /**
     * Creates a new instance of IdealRfGap
     *
     * @param strId instance identifier of element
     * @param dblETL field/transit time/length factor for gap (in
     * <strong>volts</strong>)
     * @param dblPhase operating phase of gap (in <strong>radians</strong>)
     * @param dblFreq operating RF frequency of gap (in <strong>Hertz</strong>)
     */
    public IdealRfGap(String strId, double dblETL, double dblPhase, double dblFreq) {
        super(TYPE, strId);

        this.setETL(dblETL);
        this.setPhase(dblPhase);
        this.setFrequency(dblFreq);
    }

    /**
     * JavaBean constructor - creates a new uninitialized instance of IdealRfGap
     *
     * <strong>BE CAREFUL</strong>
     */
    public IdealRfGap() {
        super(TYPE);
    }

    /*
     *  IRfGap Interface
     */
    /**
     * Return the ETL product of the gap, where E is the longitudinal electric
     * field, T is the transit time factor, and L is the gap length.
     *
     * @return the ETL product of the gap (in <bold>volts</bold>).
     */
    @Override
    public double getETL() {
        return dblETL;
    }

    /**
     * Return the RF phase delay of the gap with respect to the synchronous
     * particle.
     *
     * @return phase delay w.r.t. synchronous particle (in
     * <bold>radians</bold>).
     */
    @Override
    public double getPhase() {
        return dblPhase;
    }

    /**
     * Get the operating frequency of the RF gap.
     *
     * @return frequency of RF gap (in <bold>Hertz</bold>)
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
     * The maximum energy gain from the gap is given by qETL where q is the
     * charge (in coulombs) of the species particle.
     *
     * @param dblETL ETL product of gap (in <bold>volts</bold>).
     */
    @Override
    public void setETL(double dblETL) {
        this.dblETL = dblETL;
    }

    /**
     * Set the phase delay of the RF in the cavity with respect to the
     * synchronous particle. The actual energy gain from the gap is given by
     * qETLcos(dblPhi) where dbkPhi is the phase delay.
     *
     * @param cavPhase phase delay of the RF w.r.t. synchronous particle (in
     * <bold>radians</bold>).
     */
    @Override
    public void setPhase(double cavPhase) {
        dblPhase = cavPhase;
    }

    /**
     * Set the operating frequency of the RF gap.
     *
     * @param dblFreq frequency of RF gap (in <bold>Hertz</bold>)
     */
    @Override
    public void setFrequency(double dblFreq) {
        this.dblFreq = dblFreq;
    }

    /**
     * Set the on accelerating field
     *
     * @param cavAmp- cavity amplitude (V)
     */
    @Override
    public void setE0(double cavAmp) {
        e0 = cavAmp;
    }

    /**
     * Get the on accelerating field (V/m)
     */
    @Override
    public double getE0() {
        return e0;
    }

    /**
     * return the cell length (m)
     */
    public double getCellLength() {
        return cellLength;
    }

    /*
     *  IElement Interface
     */
    /**
     * Returns the time taken for the probe to propagate through element.
     *
     * @param probe propagating probe
     *
     * @return value of zero
     */
    @Override
    public double elapsedTime(IProbe probe) {
        return deltaPhi / (getFrequency() * 2.0 * Math.PI);
    }

    /**
     * Compute the energy gain of the RF gap for a probe including the effects
     * of calculating the phase advance.
     *
     *
     * @return energy gain for this probe (<strong>in electron-volts</strong>)
     */
    @Override
    public double energyGain(IProbe probe) {
        return energyGain;
    }

    protected double computeBetaFromGamma(double gamma) {
        return Math.sqrt(Math.pow(gamma, 2) - 1.0) / gamma;
    }

    /**
     * <p>
     * Compute the transfer map for an ideal RF gap. </p <p>
     * New transfer matrix with same definitions of <em>k<sub>r</sub></em> and
     * <em>k<sub>z</sub></em>
     * from Trace3D manual, but correctly considering XAL and trace3d
     * longitudinal phase. transformation
     * </p>
     * <p>
     * Modified on 21 Jul 06 Sako (consistency checked with Trace3D).
     * </p>
     * <p>
     * Modified on 15 Aug 17 Juan F. Esteban Müller to correct synchronous phase
     * calculation
     * </p>
     *
     * @param probe compute transfer map using parameters from this probe
     *
     * @return transfer map for the probe
     * <map smf="fm" model="xal.extension.jels.model.elem.FieldMapNCells"/>
     * @exception ModelException this should not occur
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws ModelException {
        PhaseMatrix matPhi = new PhaseMatrix();
        double lambda = LIGHT_SPEED / getFrequency();
        
        double phiS = getPhase() + probe.getLongitinalPhase() - getLongitudinalPhaseReference();
        phiS += structureMode * Math.PI * indCell;

        // Applying phase offset
        phiS += dblPhaseFactor;

        if (getE0() == 0) {
            matPhi = PhaseMatrix.identity();
            deltaPhi = 0.0;
            energyGain = 0.0;
        } else {
            double mass = probe.getSpeciesRestEnergy();
            double gammaStart = probe.getGamma();
            double betaStart = probe.getBeta();

            double kx;
            double ky;
            double kxy;
            double kz;

            double symplecticityFactor;

            double e0tl = getE0() * getCellLength();
            // Applying amplitude relative error
            e0tl *= dblAmpFactor;

            double gammaMiddle = gammaStart + e0tl / mass * Math.cos(phiS) / 2;
            double betaMiddle = computeBetaFromGamma(gammaMiddle);

            double e0tlScaled = e0tl * ttfFit.evaluateAt(betaMiddle);

            energyGain = e0tlScaled * Math.cos(phiS);

            double gammaEnd = gammaStart + energyGain / mass;
            double betaEnd = computeBetaFromGamma(gammaEnd);
            double gammaAvg = (gammaEnd + gammaStart) / 2;
            double betaAvg = computeBetaFromGamma(gammaAvg);

            if (ttfFit.getCoef(0) != 0) {
                double kToverT = -betaMiddle * ttfFit.derivativeAt(betaMiddle) / ttfFit.evaluateAt(betaMiddle);
                deltaPhi = e0tlScaled / mass * Math.sin(phiS) / (Math.pow(gammaAvg, 3) * Math.pow(betaAvg, 2)) * (kToverT);
                kx = 1 - e0tlScaled / (2 * mass) * Math.cos(phiS) / (Math.pow(betaAvg, 2) * Math.pow(gammaAvg, 3)) * (Math.pow(gammaAvg, 2) + kToverT);
                ky = 1 - e0tlScaled / (2 * mass) * Math.cos(phiS) / (Math.pow(betaAvg, 2) * Math.pow(gammaAvg, 3)) * (Math.pow(gammaAvg, 2) - kToverT);
            } else {
                kx = 1 - e0tlScaled / (2 * mass) * Math.cos(phiS) / (Math.pow(betaAvg, 2) * gammaAvg);
                ky = kx;
            }

            kxy = -Math.PI * e0tlScaled / mass * Math.sin(phiS) / (Math.pow(gammaAvg * betaAvg, 2) * lambda);
            kz = 2 * Math.PI * e0tlScaled / mass * Math.sin(phiS) / (Math.pow(betaAvg, 2) * lambda);

            symplecticityFactor = Math.sqrt((betaStart * gammaStart) / (betaEnd * gammaEnd * kx * ky));

            matPhi.setElem(0, 0, kx * symplecticityFactor);
            matPhi.setElem(1, 0, kxy / (betaEnd * gammaEnd));
            matPhi.setElem(1, 1, ky * symplecticityFactor);

            matPhi.setElem(2, 2, kx * symplecticityFactor);
            matPhi.setElem(3, 2, kxy / (betaEnd * gammaEnd));
            matPhi.setElem(3, 3, ky * symplecticityFactor);

            matPhi.setElem(4, 4, 1);
            matPhi.setElem(5, 4, kz / (betaEnd * Math.pow(gammaEnd, 3)));
            matPhi.setElem(5, 5, (betaStart * Math.pow(gammaStart, 3)) / (betaEnd * Math.pow(gammaEnd, 3)));
        }

        matPhi.setElem(6, 6, 1);

        // Jan 2019 - Natalia Milas
        // apply alignment and rotation errors
        matPhi = applyErrors(matPhi, 0.0);

        return new PhaseMap(matPhi);
    }

    /*
     *  Testing and Debugging
     */
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
    }

    /**
     * Conversion method to be provided by the user
     *
     * @param element the SMF node to convert
     */
    @Override
    public void initializeFrom(LatticeElement element) {
        super.initializeFrom(element);

        ESSRfGap rfgap = (ESSRfGap) element.getHardwareNode();

        // Initialize from source values
        bolStartCell = rfgap.isFirstCell();
        cellLength = rfgap.getGapLength();

        if (rfgap.getTTFPrimeFit().getCoef(0) != 0) {
            ttfFit = new MeanFieldPolynomial(rfgap.getTTFFit(), rfgap.getTTFPrimeFit());
        } else {
            ttfFit = rfgap.getTTFFit();
        }
        structureMode = rfgap.getStructureMode();

        dblETL = rfgap.getGapDfltE0TL() * 1e6;
        dblFreq = rfgap.getGapDfltFrequency() * 1e6;
        dblPhase = rfgap.getGapDfltPhase() * Math.PI / 180.;
        e0 = rfgap.getGapDfltAmp() * 1e6;

        dblAmpFactor = rfgap.getRfGap().getAmpFactor();
        dblPhaseFactor = rfgap.getRfGap().getPhaseFactor();
    }

    public void setCellLength(double cellLength) {
        this.cellLength = cellLength;
    }

    public double getStructureMode() {
        return structureMode;
    }

    public void setStructureMode(double structureMode) {
        this.structureMode = structureMode;
    }

    public void setTTFFit(InverseRealPolynomial ttfFit) {
        this.ttfFit = ttfFit;
    }

    @Override
    protected double longitudinalPhaseAdvance(IProbe probe) {
        return deltaPhi;
    }

    /*
     * Attribute Query
     */
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
     * defined in terms of the fractional phase advance between cells, with
     * respect to &pi;. To make this explicit
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
     * @since Nov 20, 2014
     */
    @Override
    public double getCavityModeConstant() {
        return dblCavModeConst;
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
        return bolEndCell;
    }

    /**
     *
     * @see xal.model.elem.sync.IRfCavityCell#isFirstCell()
     *
     * @since Jan 23, 2015 by Christopher K. Allen
     */
    @Override
    public boolean isFirstCell() {
        return bolStartCell;
    }

    @Override
    public void computeSynchronousPhaseAndEnergyGain(IProbe probe) {
        synchronousPhase = getPhase() + probe.getLongitinalPhase() - getLongitudinalPhaseReference();
        synchronousPhase += structureMode * Math.PI * indCell;

        // Applying phase offset
        synchronousPhase += dblPhaseFactor;

        if (getE0() == 0) {
            energyGain = 0.0;
        } else {
            double mass = probe.getSpeciesRestEnergy();
            double gammaStart = probe.getGamma();

            double e0tl = getE0() * getCellLength();
            // Applying amplitude relative error
            e0tl *= dblAmpFactor;

            double gammaMiddle = gammaStart + e0tl / mass * Math.cos(synchronousPhase) / 2;
            double betaMiddle = computeBetaFromGamma(gammaMiddle);

            double e0tlScaled = e0tl * ttfFit.evaluateAt(betaMiddle);
            // Compute energy gain to be able to calculate the synchronous phase
            // of a cavity consisting of several cells.
            energyGain = e0tlScaled * Math.cos(synchronousPhase);
        }
    }

    @Override
    public double getSynchronousPhase() {
        return synchronousPhase;
    }

    @Override
    public double getEnergyGain() {
        return energyGain;
    }

    @Override
    public void setLongitudinalPhaseReference(double longitudinalPhaseReference) {
        this.longitudinalPhaseReference = longitudinalPhaseReference;
    }

    @Override
    public double getLongitudinalPhaseReference() {
        return longitudinalPhaseReference;
    }
}
