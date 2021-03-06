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
    public static final String s_strType = "JELS.IdealRfGap";

    /*
	 *  Defining Attributes
     */
    /**
     * flag indicating that this is the leading gap of a cavity
     */
    private boolean initialGap = false;

    /**
     * ETL product of gap
     */
    private double m_dblETL = 0.0;

    /**
     * phase delay of gap w.r.t. the synchronous particle
     */
    private double m_dblPhase = 0.0;

    /**
     * operating frequency of the gap
     */
    private double m_dblFreq = 0.0;

    /**
     * the separation of the gap center from the cell center (m)
     */
    private double gapOffset = 0.;

    /**
     * the on axis accelerating field (V)
     */
    private double E0 = 0.;

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
    protected InverseRealPolynomial TTFFit;

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
    
    private double m_dblAmpFactor;
    private double m_dblPhaseFactor;

    /*
     * Initialization
     */
    /**
     * Creates a new instance of IdealRfGap
     *
     * @param strId instance identifier of element
     * @param dblETL field/transit time/length factor for gap (in <b>volts</b>)
     * @param dblPhase operating phase of gap (in <b>radians</b>)
     * @param dblFreq operating RF frequency of gap (in <b>Hertz</b>)
     */
    public IdealRfGap(String strId, double dblETL, double dblPhase, double dblFreq) {
        super(s_strType, strId);

        this.setETL(dblETL);
        this.setPhase(dblPhase);
        this.setFrequency(dblFreq);
    }

    /**
     * JavaBean constructor - creates a new uninitialized instance of IdealRfGap
     *
     * <b>BE CAREFUL</b>
     */
    public IdealRfGap() {
        super(s_strType);
    }

    /**
     * return whether this gap is the initial gap of a cavity
     */
    @Override
    public boolean isFirstGap() {
        return initialGap;
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
        return m_dblETL;
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
        return m_dblPhase;
    }

    /**
     * Get the operating frequency of the RF gap.
     *
     * @return frequency of RF gap (in <bold>Hertz</bold>)
     */
    @Override
    public double getFrequency() {
        return m_dblFreq;
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
        m_dblETL = dblETL;
    }

    /**
     * Set the phase delay of the RF in the cavity with respect to the synchronous
     * particle. The actual energy gain from the gap is given by qETLcos(dblPhi)
     * where dbkPhi is the phase delay.
     *
     * @param cavPhase phase delay of the RF w.r.t. synchronous particle (in
     * <bold>radians</bold>).
     */
    @Override
    public void setPhase(double cavPhase) {
        m_dblPhase = cavPhase + m_dblPhaseFactor;
    }

    /**
     * Set the operating frequency of the RF gap.
     *
     * @param dblFreq frequency of RF gap (in <bold>Hertz</bold>)
     */
    @Override
    public void setFrequency(double dblFreq) {
        m_dblFreq = dblFreq;
    }

    /**
     * Set the on accelerating field
     *
     * @param cavAmp- cavity amplitude (V)
     */
    @Override
    public void setE0(double cavAmp) {
        E0 = cavAmp * m_dblAmpFactor;
    }

    /**
     * Get the on accelerating field (V/m)
     */
    @Override
    public double getE0() {
        return E0;
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
     * @return energy gain for this probe (<b>in electron-volts</b>)
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
     * New transfer matrix with same definitions of <i>k<sub>r</sub></i> and
     * <i>k<sub>z</sub></i>
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
        double lambda = LightSpeed / getFrequency();

        double phiS;
        if (isFirstGap() || !probe.getAlgorithm().getRfGapPhaseCalculation()) {
            phiS = getPhase();
            phiS += structureMode * Math.PI * indCell;
        } else {
            phiS = probe.getLongitinalPhase();
            phiS += structureMode * Math.PI * indCell;
        }

        if (getE0() == 0) {
            matPhi = PhaseMatrix.identity();
        } else {
            double mass = probe.getSpeciesRestEnergy();
            double gammaStart = probe.getGamma();
            double betaStart = probe.getBeta();

            double gammaEnd;
            double betaEnd;
            double gammaAvg;
            double betaAvg;
            double gammaMiddle;
            double betaMiddle;

            double kx;
            double ky;
            double kxy;
            double kz;

            double symplecticityFactor;

            double E0TL;
            double E0TL_scaled;

            if (TTFFit.getCoef(0) != 0) {
                E0TL = getE0() * getCellLength();
                gammaMiddle = gammaStart + E0TL / mass * Math.cos(phiS) / 2;
                betaMiddle = computeBetaFromGamma(gammaMiddle);

                E0TL_scaled = E0TL * TTFFit.evaluateAt(betaMiddle);
                double kToverT = -betaMiddle * TTFFit.derivativeAt(betaMiddle) / TTFFit.evaluateAt(betaMiddle);

                energyGain = E0TL_scaled * Math.cos(phiS);
                gammaEnd = gammaStart + energyGain / mass;
                betaEnd = computeBetaFromGamma(gammaEnd);
                gammaAvg = (gammaEnd + gammaStart) / 2;
                betaAvg = computeBetaFromGamma(gammaAvg);

                deltaPhi = E0TL_scaled / mass * Math.sin(phiS) / (Math.pow(gammaAvg, 3) * Math.pow(betaAvg, 2)) * (kToverT);
                kx = 1 - E0TL_scaled / (2 * mass) * Math.cos(phiS) / (Math.pow(betaAvg, 2) * Math.pow(gammaAvg, 3)) * (Math.pow(gammaAvg, 2) + kToverT);
                ky = 1 - E0TL_scaled / (2 * mass) * Math.cos(phiS) / (Math.pow(betaAvg, 2) * Math.pow(gammaAvg, 3)) * (Math.pow(gammaAvg, 2) - kToverT);
            } else {
                E0TL = getE0() * getCellLength();
                gammaMiddle = gammaStart + E0TL / mass * Math.cos(phiS) / 2;
                betaMiddle = computeBetaFromGamma(gammaMiddle);
                
                E0TL_scaled = E0TL * TTFFit.evaluateAt(betaMiddle);
                
                energyGain = E0TL_scaled * Math.cos(phiS);
                gammaEnd = gammaStart + energyGain / mass;
                betaEnd = computeBetaFromGamma(gammaEnd);

                gammaAvg = (gammaEnd + gammaStart) / 2;
                betaAvg = computeBetaFromGamma(gammaAvg);

                kx = 1 - E0TL_scaled / (2 * mass) * Math.cos(phiS) / (Math.pow(betaAvg, 2) * gammaAvg);
                ky = kx;
            }

            kxy = -Math.PI * E0TL_scaled / mass * Math.sin(phiS) / (Math.pow(gammaAvg * betaAvg, 2) * lambda);
            kz = 2 * Math.PI * E0TL_scaled / mass * Math.sin(phiS) / (Math.pow(betaAvg, 2) * lambda);

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
        //matPhi = applyErrors(matPhi,cellLength);
        
        // Jan 2019 - Natalia Milas
        // apply alignment and rotation errors
        matPhi = applyErrors(matPhi,0.0);

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
        initialGap = rfgap.isFirstGap();
        cellLength = rfgap.getGapLength();
        gapOffset = rfgap.getGapOffset();

        if (rfgap.getTTFPrimeFit().getCoef(0) != 0) {
            TTFFit = new MeanFieldPolynomial(rfgap.getTTFFit(), rfgap.getTTFPrimeFit());
        } else {
            TTFFit = rfgap.getTTFFit();
        }
        structureMode = rfgap.getStructureMode();
        
        m_dblETL = rfgap.getGapDfltE0TL()*1e6;
        m_dblFreq = rfgap.getGapDfltFrequency()*1e6;
        m_dblPhase = rfgap.getGapDfltPhase()*Math.PI/180.;
        E0 = rfgap.getGapDfltAmp()*1e6;
               
        m_dblAmpFactor = rfgap.getRfGap().getAmpFactor();
        m_dblPhaseFactor = rfgap.getRfGap().getPhaseFactor();
    }

    public void setFirstGap(boolean initialGap) {
        this.initialGap = initialGap;
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

    public void setTTFFit(InverseRealPolynomial TTFFit) {
        this.TTFFit = TTFFit;
    }

    @Override
    protected double longitudinalPhaseAdvance(IProbe probe) {
        // WORKAROUND to set the initial phase
        if (isFirstGap()) {
            double phi0 = this.getPhase();
            double phi = probe.getLongitinalPhase();
            return deltaPhi - phi + phi0;
        }
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
     * Returns the structure mode <b>number</b> <i>q</i> for the cavity in which
     * this gap belongs. Here the structure mode number is defined in terms of
     * the fractional phase advance between cells, with respect to &pi;. To make
     * this explicit
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>q</i> = 0 &nbsp; &nbsp; &rAarr; 0 mode
     * <br/>
     * &nbsp; &nbsp; <i>q</i> = 1/2 &rArr; &pi;/2 mode
     * <br/>
     * &nbsp; &nbsp; <i>q</i> = 1 &nbsp; &nbsp; &rAarr; &pi; mode
     * <br/>
     * <br/>
     * Thus, a cavity mode constant of <i>q</i> = 1/2 indicates a &pi;/2 phase
     * advance between adjacent cells and a corresponding cell amplitude
     * function <i>A<sub>n</sub></i> of
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>A<sub>n</sub></i> = cos(<i>nq</i>&pi;)
     * <br/>
     * <br/>
     * where <i>n</i> is the index of the cell within the coupled cavity.
     * </p>
     *
     * @return the cavity mode constant for the cell containing this gap
     *
     * @see <i>RF Linear Accelerators</i>, Thomas P. Wangler (Wiley, 2008).
     *
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
}
