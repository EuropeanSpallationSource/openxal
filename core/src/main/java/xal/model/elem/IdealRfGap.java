/*
 *  IdealRfGap.java
 *
 *  Created on October 22, 2002, 1:58 PM
 */
package xal.model.elem;

import java.io.PrintWriter;

import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.sync.IRfGap;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.RfGap;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.math.fnc.poly.RealUnivariatePolynomial;

/**
 * <p>
 *
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
 * @since November 22, 2005
 */
public class IdealRfGap extends ThinElement implements IRfGap {

    /*
     *  Global Attributes
     */
    /**
     * the string type identifier for all IdealRfGap objects
     */
    public static final String TYPE = "IdealRfGap";

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
     * flag indicating that this is the leading gap of a cavity
     */
    private boolean initialGap = false;

    /**
     * the separation of the gap center from the cell center (m)
     */
    private double gapOffset = 0.;

    /**
     * These are kluge jobs for RF cavities. Very dangerous since they are class
     * variables.
     */
    private double firstGapPhaseCorr = 0.;
    /**
     * These are kluge jobs for RF cavities. Very dangerous since they are class
     * variables.
     */
    private double structurePhase = 0.;
    /**
     * These are kluge jobs for RF cavities. Very dangerous since they are class
     * variables.
     */
    private double upstreamExitTime = 0.;

    /**
     * the phase kick correction applied at the gap center [rad]
     */
    private double deltaPhaseCorrection = 0.;

    /**
     * the on axis accelerating field (V)
     */
    private double e0 = 0.;

    /**
     * the accelerating cell length
     */
    private double cellLength = 0.;

    /**
     * the energy gained in this gap (eV)
     */
    private double theEnergyGain = 0.;

    /**
     * = 0 if the gap is part of a 0 mode cavity structure (e.g. DTL) = 1 if the
     * gap is part of a pi mode cavity (e.g. CCL, Super-conducting)
     */
    private double structureMode = 0.;

    /**
     * fit of the TTF vs. beta
     */
    private RealUnivariatePolynomial ttfFit;

    /**
     * fit of the TTF-prime vs. beta
     */
    private RealUnivariatePolynomial ttfPrimeFit;

    /**
     * fit of the S factor vs. beta
     */
    private RealUnivariatePolynomial sFit;

    /**
     * fit of the S-prime vs. beta
     */
    private RealUnivariatePolynomial sPrimeFit;

    // Synchronous phase in the gap
    private double synchronousPhase;

    /**
     * Creates a new instance of IdealRfGap
     *
     * @param strId instance identifier of element
     * @param dblETL field/transit time/length factor for gap (in
     * <strong>volts</strong> )
     * @param dblPhase operating phase of gap (in <strong>radians</strong> )
     * @param dblFreq operating RF frequency of gap (in <strong>Hertz</strong> )
     */
    public IdealRfGap(String strId, double dblETL, double dblPhase, double dblFreq) {
        super(TYPE, strId);

        this.setETL(dblETL);
        this.setPhase(dblPhase);
        this.setFrequency(dblFreq);
    }

    /**
     * JavaBean constructor - creates a new uninitialized instance of IdealRfGap
     * <strong>
     * BE CAREFUL</strong>
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
     * @return the ETL product of the gap (in <strong>volts</strong> ).
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
     * <strong>radians</strong> ).
     */
    @Override
    public double getPhase() {
        return dblPhase;
    }

    /**
     * Get the operating frequency of the RF gap.
     *
     * @return frequency of RF gap (in <strong>Hertz</strong> )
     */
    @Override
    public double getFrequency() {
        return dblFreq;
    }

    /**
     * return whether this gap is the initial gap of a cavity
     *
     * @return The firstGap value
     */
    @Override
    public boolean isFirstGap() {
        return initialGap;
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
     * @param dblETL ETL product of gap (in <strong>volts</strong> ).
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
     * <strong>radians</strong> ).
     */
    @Override
    public void setPhase(double dblPhase) {
        this.dblPhase = dblPhase;
    }

    /**
     * Set the operating frequency of the RF gap.
     *
     * @param dblFreq frequency of RF gap (in <strong>Hertz</strong> )
     */
    @Override
    public void setFrequency(double dblFreq) {
        this.dblFreq = dblFreq;
    }

    /**
     * Set the on accelerating field @ param E - the on axis field (V/m)
     *
     * @param e The new e0 value
     */
    @Override
    public void setE0(double e) {
        e0 = e;
    }

    /**
     * Get the on accelerating field (V/m)
     *
     * @return The e0 value
     */
    @Override
    public double getE0() {
        return e0;
    }

    /**
     * return the cell length (m)
     *
     * @return The cellLength value
     */
    public double getCellLength() {
        return cellLength;
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
        return c / f;
    }

    /**
     * Compute and return the mid-gap normalized velocity for the given probe.
     *
     * NOTE: - Because of the state-dependent nature of the energy calculations
     * (this needs to be fixed), this function will only work correctly if the
     * function energyGain() is consistent.
     *
     * @param probe probe containing energy information
     *
     * @return average or "mid-gap" velocity in units of <strong>c</strong>
     *
     * @see IdealRfGap#energyGain(IProbe)
     */
    public double betaMidGap(IProbe probe) {

        // Get probe parameters at initial energy
        double eR = probe.getSpeciesRestEnergy();
        double wI = probe.getKineticEnergy();
        double dW = this.energyGain(probe);
        double wA = wI + dW / 2.0;

        return RelativisticParameterConverter.computeBetaFromEnergies(wA, eR);
    }

    /*
     *  IElement Interface
     */
    /**
     * Returns the time taken for the probe to propagate through element.
     *
     * @param probe propagating probe
     * @return value of zero
     */
    @Override
    public double elapsedTime(IProbe probe) {

        // Initial energy parameters
        double eR = probe.getSpeciesRestEnergy();
        double wI = probe.getKineticEnergy();
        double bi = probe.getBeta();
        double dW = this.energyGain(probe);

        // Final energy parameters
        double wF = wI + dW;
        double gf = wF / eR + 1.0;
        double bf = Math.sqrt(1.0 - 1.0 / (gf * gf));

        // update the elapsed time to account for the phase correction term
        double dT = ((deltaPhaseCorrection) / (this.getFrequency() * 2.0 * Math.PI));

        //the gap offset correction
        dT = dT + gapOffset * (1. / (bi * IElement.LIGHT_SPEED) - 1. / (bf * IElement.LIGHT_SPEED));

        //the time when probe will exit this gap
        upstreamExitTime = probe.getTime() + dT + (getCellLength() / 2.) / (bf * IElement.LIGHT_SPEED);

        return dT;
    }

    /**
     * the interface method to provide the energy gain. since this calculation
     * has gotten complicated it is done in the TransferMap method and the
     * answer is returned here.
     */
    /**
     * Compute the energy gain of the RF gap for a probe including the effects
     * of calculating the phase advance.
     *
     * @param probe uses the particle species charge
     * @return energy gain for this probe (<strong>in electron-volts</strong> )
     */
    @Override
    public double energyGain(IProbe probe) {
        return theEnergyGain;
    }

    /**
     * Routine to calculate the energy gain along with the phase advance. A
     * method that is called once by transferMatrix to calculate the energy
     * gain. This prevents energy gain calculation from being repeated many
     * times. Importantly it provides a workaround from the eneryGain being
     * calculated after the upstreamExitPhase is updated elsewhere
     *
     * @param probe The Parameter
     */
    private void compEnergyGain(IProbe probe) {
        double eL = getE0() * getCellLength();

        // Initial energy parameters
        double eR = probe.getSpeciesRestEnergy();
        double bi = probe.getBeta();
        double wI = probe.getKineticEnergy();

        double phi0;

        double arrivalTime = probe.getTime();

        //the correction for the gap offset needed
        arrivalTime = arrivalTime + gapOffset / (bi * IElement.LIGHT_SPEED);

        // get phase at the gap center:
        if (!isFirstGap()) {
            phi0 = 2. * Math.PI * arrivalTime * getFrequency() - firstGapPhaseCorr;
            double driftTime = probe.getTime() - ((getCellLength() / 2.) / (bi * IElement.LIGHT_SPEED) + upstreamExitTime);
            int nLabmda = (int) Math.round(2 * structureMode * driftTime * getFrequency());
            structurePhase = structurePhase + Math.PI * nLabmda;
            phi0 = phi0 + structurePhase;
            setPhase(phi0);
            // for first gap use input for phase at the gap center
        } else {
            structurePhase = 0.;
            firstGapPhaseCorr = 2. * Math.PI * arrivalTime * getFrequency() - getPhase();
            phi0 = getPhase();
        }

        double q = Math.abs(probe.getSpeciesCharge());
        theEnergyGain = q * eL * Math.cos(phi0) * ttfFit.evaluateAt(bi);

        structurePhase = structurePhase - (2 - structureMode) * Math.PI;
        structurePhase = Math.IEEEremainder(structurePhase, (2. * Math.PI));

        //phase change from center correction factor for future time calculations
        //in PARMILA TTFPrime and SPrime are in [1/cm] units, we use [m]
        deltaPhaseCorrection = 0;
        double ttf = ttfFit.evaluateAt(bi);
        double ttfPrime = 0.01 * ttfPrimeFit.evaluateAt(bi);
        double stf = sFit.evaluateAt(bi);
        double stfPrime = 0.01 * sPrimeFit.evaluateAt(bi);
        double freq = getFrequency();
        double dEGap = q * eL * (ttf * Math.cos(phi0) + stf * Math.sin(phi0)) / 2.0;
        double bGap0 = Math.sqrt(1. - eR * eR / ((eR + wI + dEGap) * (eR + wI + dEGap)));
        double kGap0 = 2 * Math.PI * freq / (bGap0 * IElement.LIGHT_SPEED);
        double gammaGap = Math.sqrt(1. / (1. - bGap0 * bGap0));
        double bGap = bGap0;
        double kGap = kGap0;
        double dltPhi = (q * eL / (eR * gammaGap * gammaGap * gammaGap * bGap * bGap)) * kGap * (ttfPrime * Math.sin(phi0) - stfPrime * Math.cos(phi0)) / 2.0;
        for (int i = 0; i < 3; i++) {
            bGap = Math.sqrt(1. - eR * eR / ((eR + wI + dEGap) * (eR + wI + dEGap)));
            kGap = 2 * Math.PI * freq / (bGap * IElement.LIGHT_SPEED);
            gammaGap = Math.sqrt(1. / (1. - bGap * bGap));
            dEGap = q * eL * ((ttf + ttfPrime * (kGap - kGap0)) * Math.cos(phi0 + dltPhi) + (stf + stfPrime * (kGap - kGap0)) * Math.sin(phi0 + dltPhi)) / 2.0;
            dltPhi = (q * eL / (eR * gammaGap * gammaGap * gammaGap * bGap * bGap)) * kGap * (ttfPrime * Math.sin(phi0 + dltPhi) - stfPrime * Math.cos(phi0 + dltPhi)) / 2.0;
        }
        //the energy gaine and phase are known
        //now we calculate the total energy gain and phase
        theEnergyGain = q * eL * ((ttf + ttfPrime * (kGap - kGap0)) * Math.cos(phi0 + dltPhi));
        deltaPhaseCorrection = (q * eL / (eR * gammaGap * gammaGap * gammaGap * bGap * bGap)) * kGap * (ttfPrime * Math.sin(phi0 + dltPhi));

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
        double eR = probe.getSpeciesRestEnergy();
        double wI = probe.getKineticEnergy();
        double bi = probe.getBeta();
        double gi = probe.getGamma();

        // Determine the current energy gain and focusing constants for the gap
        // the following section is to calculate the phase of the beam at each gap, rather than use hardwired phases.
        // update the energy gain first:
        if (probe.getAlgorithm().getRfGapPhaseCalculation()) {
            compEnergyGain(probe);
        } else {
            simpleEnergyGain(probe);
        }

        double dW = this.energyGain(probe);

        double kz = this.compLongFocusing(probe);
        double kt = this.compTransFocusing(probe);

        // Compute final energy parameters
        double wF = wI + dW;
        double gf = wF / eR + 1.0;
        double bf = Math.sqrt(1.0 - 1.0 / (gf * gf));

        // Compute average energy parameters
        // Compute component block matrices then full transfer matrix
        double[][] arrTranX = new double[][]{{1.0, 0.0}, {kt / (bf * gf), bi * gi / (bf * gf)}};
        double[][] arrTranY = new double[][]{{1.0, 0.0}, {kt / (bf * gf), bi * gi / (bf * gf)}};

        // CKA - Corrected 7/14/2010
        //  Additional factor gbar^2 in the longitudinal focusing term 
        double[][] arrLong = new double[][]{{1.0, 0.0}, {kz / (bf * gf * gf * gf), gi * gi * gi * bi / (gf * gf * gf * bf)}};

        PhaseMatrix matPhi = new PhaseMatrix();

        matPhi.setElem(6, 6, 1.0);
        matPhi.setSubMatrix(0, 1, 0, 1, arrTranX);
        matPhi.setSubMatrix(2, 3, 2, 3, arrTranY);
        matPhi.setSubMatrix(4, 5, 4, 5, arrLong);

        // Do the phase update if this is desired:
        // do it here to resuse the bi, bf, etc. factors
        // Jan 2019 - Natalia Milas
        // apply alignment and rotation errors
        matPhi = applyErrors(matPhi, 0.0);

        return new PhaseMap(matPhi);
    }


    /*
     *  Support Methods
     */
    /**
     * Compute the energy gain of the RF gap for a probe assuming a fixed
     * default phase at the gap center.
     *
     * @param probe uses the particle species charge
     * @return energy gain for this probe (<strong>in electron-volts</strong> )
     */
    public double simpleEnergyGain(IProbe probe) {
        double etl = this.getETL();
        double q = Math.abs(probe.getSpeciesCharge());
        double phi = this.getPhase();
        theEnergyGain = q * etl * Math.cos(phi);

        return theEnergyGain;
    }

    /**
     * Get the transverse focusing constant for a particular probe. The focusing
     * constant is used in the construction of the transfer matrix for the RF
     * gap. A gap provides longitudinal focusing and transverse defocusing as
     * well as a gain in beam energy. This focusing constant describes the
     * effect in the transverse direction, which is defocusing and, therefore,
     * negative.
     * <p>
     *
     * The value represents the thin lens focusing constant for an ideal RF gap
     * (this is the inverse of the focal length). To compute the focusing action
     * for the lens we must include beam energy, which is changing through the
     * gap. We use the value of beta for which the beam has received half the
     * total energy gain.
     *
     * @param probe beam energy and particle charge are taken from the probe
     * @return (de)focusing constant (<strong>in radians/meter</strong> )
     */
    public double compTransFocusing(IProbe probe) {

        double c = IElement.LIGHT_SPEED;

        double q = Math.abs(probe.getSpeciesCharge());
        double eR = probe.getSpeciesRestEnergy();
        double wI = probe.getKineticEnergy();

        double wBar = wI + this.energyGain(probe) / 2.0;
        double gbar = wBar / eR + 1.0;
        double bbar = Math.sqrt(1.0 - 1.0 / (gbar * gbar));
        double bgbar = bbar * gbar;

        double etl = this.getETL();
        double phi = this.getPhase();
        double f = this.getFrequency();

        return Math.PI * q * etl * f * Math.sin(-phi) / (c * eR * bgbar * bgbar);
    }

    /**
     * Get the longitudinal focusing constant for a particular probe. The
     * focusing constant is used in the construction of the transfer matrix for
     * the RF gap. A gap provides longitudinal focusing and transverse
     * defocusing as well as a gain in beam energy. This focusing constant
     * describes the effect in the longitudinal direction, which is focusing
     * and, therefore, positive.
     * <p>
     *
     * The value represents the thin lens focusing constant for an ideal RF gap
     * (this is the inverse of the focal length). To compute the focusing action
     * for the lens we must include beam energy, which is changing through the
     * gap. We use the value of beta for which the beam has received half the
     * total energy gain.
     *
     * @param probe beam energy and particle charge are taken from the probe
     * @return (de)focusing constant (<strong>in radians/meter</strong> )
     */
    public double compLongFocusing(IProbe probe) {

        double eR = probe.getSpeciesRestEnergy();
        double wI = probe.getKineticEnergy();

        double wBar = wI + this.energyGain(probe) / 2.0;
        double gbar = wBar / eR + 1.0;

        double kr = this.compTransFocusing(probe);

        return -2.0 * kr * gbar * gbar;
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
        os.println("  Axial field E0     : " + this.getE0());
    }

    /**
     * Conversion method to be provided by the user
     *
     * @param element the SMF node to convert
     */
    @Override
    public void initializeFrom(LatticeElement element) {
        super.initializeFrom(element);
        RfGap rfgap = (RfGap) element.getHardwareNode();

        // Initialize from source values
        initialGap = rfgap.isFirstGap();
        cellLength = rfgap.getGapLength();
        gapOffset = rfgap.getGapOffset();
        ttfPrimeFit = rfgap.getTTFPrimeFit();
        ttfFit = rfgap.getTTFFit();
        sPrimeFit = rfgap.getSPrimeFit();
        sFit = rfgap.getSFit();
        structureMode = rfgap.getStructureMode();
        dblETL = rfgap.getGapDfltE0TL() * 1e6;
        dblFreq = rfgap.getGapDfltFrequency() * 1e6;
        dblPhase = (rfgap.getGapDfltPhase() + rfgap.getRfGap().getPhaseFactor()) * Math.PI / 180.;
        e0 = rfgap.getGapDfltAmp() * 1e6 * rfgap.getRfGap().getAmpFactor();
    }

    @Override
    public void computeSynchronousPhaseAndEnergyGain(IProbe probe) {
        // Compute energy gain to be able to calculate the synchronous phase of a cavity consisting of several cells.
        if (probe.getAlgorithm().getRfGapPhaseCalculation()) {
            compEnergyGain(probe);
        } else {
            simpleEnergyGain(probe);
        }

        double bi = probe.getBeta();
        synchronousPhase = 0.;
        double arrivalTime = probe.getTime();

        //the correction for the gap offset needed
        arrivalTime = arrivalTime + gapOffset / (bi * IElement.LIGHT_SPEED);

        // get phase at the gap center:
        if (!isFirstGap()) {
            synchronousPhase = 2. * Math.PI * arrivalTime * getFrequency() - firstGapPhaseCorr;
            double driftTime = probe.getTime() - ((getCellLength() / 2.) / (bi * IElement.LIGHT_SPEED) + upstreamExitTime);
            int nLabmda = (int) Math.round(2 * structureMode * driftTime * getFrequency());
            structurePhase = structurePhase + Math.PI * nLabmda;
            synchronousPhase = synchronousPhase + structurePhase;
            setPhase(synchronousPhase);
            // for first gap use input for phase at the gap center
        } else {
            structurePhase = 0.;
            firstGapPhaseCorr = 2. * Math.PI * arrivalTime * getFrequency() - getPhase();
            synchronousPhase = getPhase();
        }
    }

    @Override
    public double getSynchronousPhase() {
        return synchronousPhase;
    }

    @Override
    public double getEnergyGain() {
        return theEnergyGain;
    }
}
