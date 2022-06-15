package xal.smf.impl;

import xal.ca.ChannelFactory;
import xal.ca.GetException;
import xal.smf.AcceleratorNode;
import xal.smf.attr.AttributeBucket;
import xal.smf.attr.RfCavityBucket;
import xal.smf.attr.RfGapBucket;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.math.fnc.poly.RealUnivariatePolynomial;

/**
 * The implementation of the RF gap element.
 *
 * The RfGap class is meant to be used in connection with a set of related RF
 * gaps, such as the gaps in a DTL Tank, which are all part of a single resonant
 * cavity controlled by a single klystron. Each gap may have a fixed scale
 * factor for both the field and phase, relative to a nominal field and phase.
 *
 * @author J. Galambos
 */
public class RfGap extends AcceleratorNode {

    /*
     *  Constants
     */
    public static final String TYPE = "RG";

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(RfGap.class, TYPE, "rfgap");
    }


    /*
     *  Local Attributes
     */
    /**
     * The rf gap bucket containing the length, ampFactor, phaseFactor and TTF
     */
    protected RfGapBucket bucRfGap;

    /**
     * a flag indicating whether this gap is the first gap in a cavity string
     */
    private boolean firstCell = false;

    /**
     * Override to provide type signature
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Primary Constructor
     */
    public RfGap(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setRfGap(new RfGapBucket());
    }

    /**
     * Constructor
     */
    public RfGap(final String strId) {
        this(strId, null);
    }

    /*
     *  Attributes
     */
    public RfGapBucket getRfGap() {
        return bucRfGap;
    }

    public void setRfGap(RfGapBucket buc) {
        bucRfGap = buc;
        super.addBucket(buc);
    }

    /**
     * Override AcceleratorNode implementation to check for a RfGapBucket
     */
    @Override
    public void addBucket(AttributeBucket buc) {
        if (buc.getClass().equals(RfGapBucket.class)) {
            setRfGap((RfGapBucket) buc);
        }

        super.addBucket(buc);
    }

    /**
     * return the RF amplitude in the gap (kV/m). Note, this method should
     * probably be modified
     */
    public double getGapAmpAvg() throws GetException {
        final RfCavity rfCav = (RfCavity) getParent();
        return toGapAmpFromCavityAmp(rfCav.getCavAmpAvg());
    }

    /**
     * return the RF amplitude in the gap (kV/m)
     */
    public double getGapDfltAmp() {
        final RfCavity rfCav = (RfCavity) getParent();
        final RfCavityBucket rfCavBuc = rfCav.getRfField();
        return toGapAmpFromCavityAmp(rfCavBuc.getAmplitude());
    }

    /**
     * This includes the calibration offset factor if it has been set
     *
     * @return the RF phase in the gap (deg).
     */
    public double getGapPhaseAvg() throws GetException {
        final RfCavity rfCav = (RfCavity) getParent();
        return toGapPhaseFromCavityPhase(rfCav.getCavPhaseAvg());
    }

    /**
     * This is the product of the field * gap length * TTF
     *
     * @return the E0TL product (kV)
     */
    public double getGapE0TL() throws GetException {
        return toE0TLFromGapField(getGapAmpAvg());
    }

    /**
     * This is the product of the field * gap length * TTF
     *
     * @return the E0TL product (kV)
     */
    public double getGapDfltE0TL() {
        return toE0TLFromGapField(getGapDfltAmp());
    }

    /**
     * return the RF phase in the gap (deg)
     */
    public double getGapDfltPhase() {
        final RfCavity rfCav = (RfCavity) getParent();
        final RfCavityBucket rfCavBuc = rfCav.getRfField();
        return toGapPhaseFromCavityPhase(rfCavBuc.getPhase());
    }

    /**
     * return the RF fundamental frequency
     */
    public double getGapDfltFrequency() {
        final RfCavity rfCav = (RfCavity) getParent();
        final RfCavityBucket rfCavBuc = rfCav.getRfField();
        return rfCavBuc.getFrequency();
    }

    /**
     * Convert RF cavity amplitude to get the RF gap's amplitude.
     *
     * @param cavityAmp the RF cavity's amplitude
     * @return this RF gap's amplitude
     */
    public double toGapAmpFromCavityAmp(final double cavityAmp) {
        return cavityAmp * bucRfGap.getAmpFactor();
    }

    /**
     * Convert RF cavity phase to get the RF gap's phase.
     *
     * @param cavityPhase the RF cavity's phase
     * @return this RF gap's phase
     */
    public double toGapPhaseFromCavityPhase(final double cavityPhase) {
        return cavityPhase + bucRfGap.getPhaseFactor();
    }

    /**
     * Convert RF gap field, E0, to E0TL. This is the product of the field * gap
     * length * TTF.
     *
     * @param field the RF field in KV/m
     * @return the E0TL product (kV)
     */
    public double toE0TLFromGapField(final double field) {
        return field * bucRfGap.getLength() * bucRfGap.getTTF();
    }

    /**
     * return Rf Gap Length
     *
     * <p>
     * <h4>CKA NOTES:</h4>
     * &middot; I believe this is the length of the overall gap cell structure,
     * not just the gap itself.
     * <br/>
     * &middot; Specifically, it is the distance from one gap center to the next
     * in an accelerating structure.
     * </p>
     *
     */
    public double getGapLength() {
        return bucRfGap.getLength();
    }

    /**
     * return TTF
     */
    public double getGapTTF() {
        return bucRfGap.getTTF();
    }

    //JAMES CODE: sets the gap TTF value for the given gap
    public void setGapTTF(double gapTTFval) {
        bucRfGap.setTTF(gapTTFval);
    }

    /**
     * Set the RF amplitude in the (kV/m) should be done by the parent cavity
     * (e.g. DTL tank)
     * <br/>
     * <br/>
     * <em>Currently this method does nothing!</em>
     *
     *
     * @param cavAmp The amplitude of the first gap (kV/m)
     */
    public void setGapAmp(double cavAmp) {
        // Do nothing
    }

    /**
     * Set the RF phase in the gap (deg) should be done by the parent cavity
     * (e.g. DTL tank)
     *
     * @param cavPhase The phase of the first gap (deg)
     */
    public void setGapPhase(double cavPhase) {
        // Do nothing
    }

    // the RfGapDataSource interface methods:
    /**
     * Return a polynomial fit of the transit time factor <em>T</em>(&beta;) as
     * a function of normalized velocity &beta;.
     *
     * <p>
     * <h4>CKA NOTES:</h4>
     * &middot; It appears to me that the returned value of <em>T</em>'(&beta;)
     * is in the units of <strong>centimeters</strong>.
     * <br/>
     * &middot; The units for the transit time factor <em>T</em>(&beta;) are in
     * <strong>meters</strong>.
     * <br/>
     * &middot; This is a confusing inconsistency and hopefully we can resolve
     * this in the future.
     * <br/>
     * &middot; The modeling element <code>IdealRfGap</code> uses the magic
     * number of 0.01 as a factor in front of
     * <code>{@link #getTTFPrimeFit()}</code>.
     * </p>
     *
     * @return &nbsp; &nbsp; <em>T</em>(&beta;) &approx; <em>a</em><sub>0</sub>
     * + <em>a</em><sub>1</sub>&beta; + <em>a</em><sub>2</sub>&beta;<sup>2</sup>
     * + ...
     *
     * @version June 1, 2015
     */
    public RealUnivariatePolynomial getTTFFit() {

        double[] arrCoeffs = bucRfGap.getTCoefficients();

        // Defaults to the RF cavity transit time factor if none is 
        //  defined for this gap.
        if (arrCoeffs == null || arrCoeffs.length == 0) {
            RfCavity rfCav = (RfCavity) getParent();
            if (isEndCell()) {
                return rfCav.getTTFFitEnd();
            } else {
                return rfCav.getTTFFit();
            }
        }

        // A set of coefficients is defined for this fit.
        //  Create the fitting function and return it.
        return new RealUnivariatePolynomial(arrCoeffs);
    }

    /**
     * <p>
     * Return a polynomial fit of the transit time factor derivative
     * <em>T'</em>(&beta;) as a function of normalized velocity &beta;. Note
     * that the derivative is with respect to the wave number <em>k</em>; that
     * is,
     * <em>T</em>'(&beta) = <em>dT</em>(&beta;)/<em>dk</em>.
     * </p>
     * <p>
     * <h4>CKA NOTES:</h4>
     * &middot; It appears to me that the returned value of <em>T</em>'(&beta;)
     * is in the units of <strong>centimeters</strong>.
     * <br/>
     * &middot; The units for the transit time factor <em>T</em>(&beta;) are in
     * <strong>meters</strong>.
     * <br/>
     * &middot; This is a confusing inconsistency and hopefully we can resolve
     * this in the future.
     * <br/>
     * &middot; The modeling element <code>IdealRfGap</code> uses the magic
     * number of 0.01 as a factor in front of
     * <code>{@link #getTTFPrimeFit()}</code>.
     * <br/>
     * &middot; Equally distressing is that the code within
     * <code>IdealRfGap</code>, the modeling element for an RF gap, treats this
     * value as if it where the derivative with respect to wave number
     * <em>k</em>. That is, the returned value here is
     * &part;<em>T</em>(&beta;)/&part;<em>k</em>.
     * </p>
     *
     * @return &nbsp; &nbsp; <em>T</em>(&beta;) &approx; <em>a</em><sub>0</sub>
     * + <em>a</em><sub>1</sub>&beta; + <em>a</em><sub>2</sub>&beta;<sup>2</sup>
     * + ...
     *
     * @version June 1, 2015
     */
    public RealUnivariatePolynomial getTTFPrimeFit() {

        double[] arrCoeffs = bucRfGap.getTpCoefficients();

        // Defaults to the RF cavity transit time factor if none is 
        //  defined for this gap.
        if (arrCoeffs == null || arrCoeffs.length == 0) {
            RfCavity rfCav = (RfCavity) getParent();
            if (isEndCell()) {
                return rfCav.getTTFPrimeFitEnd();
            } else {
                return rfCav.getTTFPrimeFit();
            }
        }

        // A set of coefficients is defined for this fit.
        //  Create the fitting function and return it.
        return new RealUnivariatePolynomial(arrCoeffs);
    }

    /**
     * Return a polynomial fit of the sine transit time factor
     * <em>S</em>(&beta;) as a function of normalized velocity &beta;.
     *
     * @return &nbsp; &nbsp; <em>S</em>(&beta;) &approx; <em>b</em><sub>0</sub>
     * + <em>b</em><sub>1</sub>&beta; + <em>b</em><sub>2</sub>&beta;<sup>2</sup>
     * + ...
     *
     * @version June 1, 2015
     */
    public RealUnivariatePolynomial getSFit() {

        double[] arrCoeffs = bucRfGap.getSCoefficients();

        // Defaults to the RF cavity transit time factor if none is 
        //  defined for this gap.
        if (arrCoeffs == null || arrCoeffs.length == 0) {
            RfCavity rfCav = (RfCavity) getParent();
            if (isEndCell()) {
                return rfCav.getSTFFitEnd();
            } else {
                return rfCav.getSTFFit();
            }
        }

        // A set of coefficients is defined for this fit.
        //  Create the fitting function and return it.
        return new RealUnivariatePolynomial(arrCoeffs);
    }

    /**
     * <p>
     * Return a polynomial fit of the sine transit time factor derivative
     * <em>S'</em>(&beta;) as a function of normalized velocity &beta;. Note
     * that the derivative is with respect to the wave number <em>k</em>; that
     * is,
     * <em>S</em>'(&beta) = <em>dS</em>(&beta;)/<em>dk</em>.
     * </p>
     * <p>
     * <h4>CKA NOTES:</h4>
     * &middot; It appears to me that the returned value of <em>S</em>'(&beta;)
     * is in the units of <strong>centimeters</strong>.
     * <br/>
     * &middot; The units for the transit time factor <em>S</em>(&beta;) are in
     * <strong>meters</strong>.
     * <br/>
     * &middot; This is a confusing inconsistency and hopefully we can resolve
     * this in the future.
     * <br/>
     * &middot; The modeling element <code>IdealRfGap</code> uses the magic
     * number of 0.01 as a factor in front of
     * <code>{@link #getSTFPrimeFit()}</code>.
     * </p>
     *
     * @return &nbsp; &nbsp; <em>S</em>(&beta;) &approx; <em>b</em><sub>0</sub>
     * + <em>b</em><sub>1</sub>&beta; + <em>b</em><sub>2</sub>&beta;<sup>2</sup>
     * + ...
     *
     * @version June 1, 2015
     */
    public RealUnivariatePolynomial getSPrimeFit() {
        double[] arrCoeffs = this.bucRfGap.getSpCoefficients();

        // Defaults to the RF cavity transit time factor derivative if none is 
        //  defined for this gap.
        if (arrCoeffs == null || arrCoeffs.length == 0) {
            RfCavity rfCav = (RfCavity) getParent();
            if (isEndCell()) {
                return rfCav.getSTFPrimeFitEnd();
            } else {
                return rfCav.getSTFPrimeFit();
            }
        }

        // A set of coefficients is defined for this fit.
        //  Create the fitting function and return it.
        return new RealUnivariatePolynomial(arrCoeffs);
    }

    /**
     * @return <strong>0</strong> if the gap is part of a 0 mode cavity
     * structure (e.g. DTL) <br/>
     * <strong>1</strong> if the gap is part of a &pi; mode cavity (e.g. CCL,
     * Superconducting)
     */
    public double getStructureMode() {
        RfCavity rfCav = (RfCavity) getParent();
        return rfCav.getStructureMode();
    }

    /**
     * these may be different, for example, for a DTL cavity
     *
     * @return the offset of the gap center from the cell center (m)
     */
    public double getGapOffset() {
        return bucRfGap.getGapOffset();
    }

    /**
     * sets the flag indicating whether this is the first gap in a cavity
     */
    public void setFirstCell(boolean tf) {
        firstCell = tf;
    }

    /**
     * returns whether this is the first gap of a cavity string
     */
    public boolean isFirstCell() {
        return firstCell;
    }

    /**
     * returns whether this is the <strong>last</strong> gap of a cavity string
     */
    public boolean isEndCell() {
        return bucRfGap.getEndCell() == 1;
    }

    /**
     * Computes and returns the design value of the energy gain for this gap.
     * The energy gain is given by the Panofsky equation
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;<em>W</em> =
     * <em>q</em> <em>E</em><sub>0</sub><em>L</em> <em>T</em>(&beta;)
     * cos(&phi;<sub>0</sub>).
     *
     * @return design energy gain &Delta;<em>W</em> (eV)
     *
     * Added 10/17/02 CKA
     */
    public double getDesignEnergyGain() {
        double etl = getGapDfltE0TL();
        double phi = getGapDfltPhase();

        return etl * Math.cos(phi);
    }

}
