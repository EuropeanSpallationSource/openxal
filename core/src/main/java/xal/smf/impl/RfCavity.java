package xal.smf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AccessibleProperty;
import xal.smf.attr.AttributeBucket;
import xal.smf.attr.RfCavityBucket;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;
import xal.tools.math.fnc.poly.RealUnivariatePolynomial;

/**
 * The implementation of the RF Cavity element. The Rf Cavity is the device that
 * is directly connected to a klystron. There are internal RF gap(s) within this
 * cavity, which are controlled by the cavity. The RfGaps are a separate class
 * of type AcceleratorNode. The beam dynamics are done in the RfGap class. Note:
 * the "knob" connections are to the klystron. The
 *
 * @author Nikolay Malitsky, Christopher K. Allen
 */
public class RfCavity extends AcceleratorSeq {

    public static final String CAV_AMP_SET_HANDLE = "cavAmpSet";
    public static final String CAV_PHASE_SET_HANDLE = "cavPhaseSet";
    public static final String CAV_AMP_AVG_HANDLE = "cavAmpAvg";
    public static final String CAV_PHASE_AVG_HANDLE = "cavPhaseAvg";
    public static final String DELTA_TRF_START_HANDLE = "deltaTRFStart";
    public static final String DELTA_TRF_END_HANDLE = "deltaTRFEnd";
    public static final String T_DELAY_HANDLE = "tDelay";
    public static final String BLANK_BEAM_HANDLE = "blankBeam";

    public final AccessibleProperty amplitude = new AccessibleProperty("amplitude", CAV_AMP_AVG_HANDLE, CAV_AMP_SET_HANDLE, this::getDfltCavAmp, designValue -> toCavAmpAvgFromCA(designValue));
    public final AccessibleProperty phase = new AccessibleProperty("phase", CAV_PHASE_AVG_HANDLE, CAV_PHASE_SET_HANDLE, this::getDfltAvgCavPhase, designValue -> toCavPhaseAvgFromCA(designValue));
    public final AccessibleProperty deltaTRFStart = new AccessibleProperty(DELTA_TRF_START_HANDLE);
    public final AccessibleProperty deltaTRFEnd = new AccessibleProperty(DELTA_TRF_END_HANDLE);
    public final AccessibleProperty tDelay = new AccessibleProperty(T_DELAY_HANDLE);
    public final AccessibleProperty blankBeam = new AccessibleProperty(BLANK_BEAM_HANDLE);

    /**
     * accelerator node type
     */
    public static final String TYPE = "RF";

    /**
     * RF Cavity parameters
     */
    protected RfCavityBucket bucRfCavity;

    /**
     * <p>
     * container of the enclosed RfGap(s) in this cavity sorted by position
     * </p>
     * <h3>NOTE:</h3>
     * <p>
     * An <code>RfCavityStruct</code> is an <code>AcceleratorSeq</code> which is
     * already an ordered list of <code>AcceleratorNode</code>s. This attribute
     * and any reliance on it seems dangerously redundant.
     * <h4>NOTE:</h4>
     * This appears to be used to process the gaps and only the gaps within this
     * cavity structure.
     * </p>
     */
    protected List<RfGap> gaps = new ArrayList<>();

    // static initializer
    static {
        registerType();
    }

    /**
     * Primary Constructor
     */
    public RfCavity(final String strId, final ChannelFactory channelFactory, final int intReserve) {
        super(strId, channelFactory, intReserve);
        setRfField(new RfCavityBucket());
    }

    /**
     * Constructor
     */
    public RfCavity(final String strId, final ChannelFactory channelFactory) {
        this(strId, channelFactory, 0);
    }

    /**
     * Constructor
     */
    public RfCavity(final String strId) {
        this(strId, 0);
    }

    /**
     * Constructor
     */
    public RfCavity(final String strId, final int intReserve) {
        this(strId, null, intReserve);
    }

    /**
     * Register accelerator node type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(RfCavity.class, TYPE);
        typeManager.registerType(RfCavity.class, "rfcavity");
    }

    /**
     * Override to provide type signature
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addNode(final AcceleratorNode newNode) {
        boolean parentResult = super.addNode(newNode);
        updateGaps();
        return parentResult;
    }

    /**
     * Update the enclosed rf gaps.
     */
    private void updateGaps() {
        final List<AcceleratorNode> nodes = getNodesOfType(RfGap.TYPE, true);
        gaps = new ArrayList<>(nodes.size());
        for (final AcceleratorNode node : nodes) {
            gaps.add((RfGap) node);
        }
        processGaps();
    }

    /**
     * Collect all of the enclosed rf gaps for convenience
     */
    @Override
    public void update(final DataAdaptor adaptor) {
        super.update(adaptor);
        updateGaps();
    }

    /**
     * loop through the gaps in this cavity to initialize some stuff
     */
    private void processGaps() {
        Iterator<RfGap> gapIter = gaps.iterator();
        int index = 0;
        // presently the gappOffset is commented out.
        // to do it right we need the gapOffset of the 1st
        // gap in a cavity to come from an external source.
        while (gapIter.hasNext()) {
            RfGap gap = gapIter.next();
            gap.setFirstGap(index == 0);
            index += 1;
        }
    }

    /**
     * returns the bucket for the RfField of this cavity
     */
    public RfCavityBucket getRfField() {
        return bucRfCavity;
    }

    /**
     * sets the bucket for the RfField of this cavity
     */
    public void setRfField(RfCavityBucket buc) {
        bucRfCavity = buc;
        super.addBucket(buc);
    }

    /**
     * Override AcceleratorNode implementation to check for a RfCavityStruct
     * Bucket
     */
    @Override
    public void addBucket(AttributeBucket buc) {
        if (buc.getClass().equals(RfCavityBucket.class)) {
            setRfField((RfCavityBucket) buc);
        }

        super.addBucket(buc);
    }

    private Channel cavAmpSetC = null;
    private Channel cavPhaseSetC = null;
    private Channel cavAmpAvgC = null;
    private Channel cavPhaseAvgC = null;

    /**
     * get the cavity amplitude (MV) and publish this to all the gaps connected
     * to this cavity note the cavity amp [MV] = klystron amplitude * ampFactor
     * where ampFactor is a calibration factor determined experimentally
     */
    public double getCavAmpAvg() throws GetException {
        cavAmpAvgC = this.lazilyGetAndConnect(CAV_AMP_AVG_HANDLE, cavAmpAvgC);
        return toCavAmpAvgFromCA(cavAmpAvgC.getValDbl());
    }

    /**
     * Convert the raw channel access value to get the cavity amplitude in MV.
     *
     * @param rawValue the raw channel value
     * @return the cavity amplitude in MV
     */
    public double toCavAmpAvgFromCA(final double rawValue) {
        return rawValue * bucRfCavity.getAmpFactor();
    }

    /**
     * Convert the cavity amplitude to channel access.
     *
     * @param value the cavity amplitude
     * @return the channel access value
     */
    public double toCAFromCavAmpAvg(final double value) {
        return value / bucRfCavity.getAmpFactor();
    }

    /**
     * Get the cavity phase relative to the beam (deg) and publish it to all the
     * rf gaps associated with this cavity note the cavity phase = klystron
     * phase + phaseOffset where phaseOffset is a calibration factor determined
     * experimentally
     */
    public double getCavPhaseAvg() throws GetException {
        cavPhaseAvgC = this.lazilyGetAndConnect(CAV_PHASE_AVG_HANDLE, cavPhaseAvgC);
        return toCavPhaseAvgFromCA(cavPhaseAvgC.getValDbl());
    }

    /**
     * Convert the raw channel access value to get the cavity phase in degrees.
     *
     * @param rawValue the raw channel value
     * @return the cavity phase in degrees
     */
    public double toCavPhaseAvgFromCA(final double rawValue) {
        return rawValue + bucRfCavity.getPhaseOffset();
    }

    /**
     * Convert the cavity phase to channel access.
     *
     * @param value the cavity phase
     * @return the channel access value
     */
    public double toCAFromCavPhaseAvg(final double value) {
        return value - bucRfCavity.getPhaseOffset();
    }

    /**
     * @return default (design) cavity amplitude (MV)
     */
    public double getDfltCavAmp() {
        return getRfField().getAmplitude();
    }

    /**
     * @return default (design) cavity phase (deg)
     */
    public double getDfltCavPhase() {
        return getRfField().getPhase();
    }

    public void setDfltCavAmp(double value) {
        getRfField().setAmplitude(value);
    }

    public void setDfltCavPhase(double value) {
        getRfField().setPhase(value);
    }

    /**
     * @return default (design) average cavity phase (averaged over all RF gaps
     * in the cavity)
     */
    public double getDfltAvgCavPhase() {
        return toAvgCavPhaseFromCavPhase(getDfltCavPhase());
    }

    /**
     * CKA - Never used
     *
     * @return default (design) average cavity TTF (averaged over all RF gaps in
     * the cavity)
     */
    public double getDfltAvgCavTTF() {
        double sum = 0.;
        for (final RfGap gap : gaps) {
            sum += gap.getGapTTF() * gap.bucRfGap.getAmpFactor() * Math.cos(gap.getGapDfltPhase() * Math.PI / 180.);
        }
        return sum / ((double) gaps.size() * Math.cos(getDfltAvgCavPhase() * Math.PI / 180.));
    }

    /**
     * get the length of the active RF accelerating structure in this cavity (m)
     */
    public double getRFLength() {
        double sum = 0.;
        for (final RfGap gap : gaps) {
            sum += gap.getGapLength();
        }
        return sum;
    }

    /**
     * Convert the cavity phase (phase at entrance to cavity) to average cavity
     * phase by averaging the phase over the gaps.
     *
     * @param cavityPhase the phase at the start of the cavity.
     * @return the average phase of the cavity
     */
    public double toAvgCavPhaseFromCavPhase(final double cavityPhase) {
        double sum = 0.0;
        for (final RfGap gap : gaps) {
            sum += gap.toGapPhaseFromCavityPhase(cavityPhase);
        }

        return sum / gaps.size();
    }

    /**
     * Calculate the average phase of the gaps at the center of the cavity from
     * the phase at the entrance to the cavity.
     *
     * @param cavityPhase the phase at the start of the cavity.
     * @return the average phase of the cavity
     */
    public double toCenterAvgCavPhaseFromCavPhase(final double cavityPhase) {
        final int gapCount = gaps.size();
        if (gapCount < 1) {
            return cavityPhase;
        }

        // if the gap count is even then average over the two center gaps;  if the gap 
        final int startIndex = (gapCount - 1) / 2;
        final int endIndex = 1 + gapCount / 2;

        double phaseSum = 0.0;
        double totalLength = 0.0;
        for (int index = startIndex; index < endIndex; index++) {
            final RfGap gap = this.gaps.get(index);
            final double gapLength = gap.getGapLength();
            phaseSum += gap.toGapPhaseFromCavityPhase(cavityPhase) * gapLength;
            totalLength += gapLength;
        }

        return totalLength != 0 ? phaseSum / totalLength : 0;
    }

    /**
     * Set the cavity amplitude [MV] note the cavity amp [MV] = klystron amp *
     * ampFactor where ampFactor is a calibration factor determined
     * experimentally
     */
    public void setCavAmp(double newAmp) throws PutException {
        cavAmpSetC = this.lazilyGetAndConnect(amplitude.getSetHandle(), cavAmpSetC);
        cavAmpSetC.putVal(toCAFromCavAmpAvg(newAmp));
    }

    /**
     * Set the cavity phase relative to the beam (deg) note the cavity phase =
     * klystron phase + phaseOffset where phaseOffset is a calibration factor
     * determined experimentally
     */
    public void setCavPhase(double newPhase) throws PutException {
        cavPhaseSetC = this.lazilyGetAndConnect(phase.getSetHandle(), cavPhaseSetC);
        cavPhaseSetC.putVal(toCAFromCavPhaseAvg(newPhase));
    }

    /**
     * return the present live set point for the amplitude
     */
    public double getCavAmpSetPoint() throws GetException {
        cavAmpSetC = this.lazilyGetAndConnect(amplitude.getSetHandle(), cavAmpSetC);
        return cavAmpSetC.getValDbl() * bucRfCavity.getAmpFactor();
    }

    /**
     * return the present live set point for the phase
     */
    public double getCavPhaseSetPoint() throws GetException {
        cavPhaseSetC = this.lazilyGetAndConnect(phase.getSetHandle(), cavPhaseSetC);
        return cavPhaseSetC.getValDbl();
    }

    /**
     * Determine whether the beam is blanked
     *
     * @return true if the beam is blanked and false if not
     */
    public boolean getBlankBeam() throws GetException {
        final Channel blankBeamChannel = getAndConnectChannel(BLANK_BEAM_HANDLE);
        return blankBeamChannel != null ? (blankBeamChannel.getValEnum() == 1) : false;
    }

    /**
     * Blank the beam
     *
     * @param mode true to blank the beam and false for continuous on
     */
    public void setBlankBeam(final boolean mode) throws PutException {
        final Channel blankBeamChannel = getAndConnectChannel(blankBeam.getSetHandle());
        if (blankBeamChannel != null) {
            blankBeamChannel.putVal(mode ? 1 : 0);
        } else {
            throw new RuntimeException("Attempting to blank the beam for " + getId() + " but no channel can be found.");
        }
    }

    /**
     * method to return the gaps associated with this cavity
     */
    public Collection<RfGap> getGaps() {
        return gaps;
    }

    /**
     * method to return the gaps associated with this cavity as a List
     */
    public List<RfGap> getGapsAsList() {
        return gaps;
    }

    /**
     * Set the design phase
     *
     * @param phase new design phase (deg)
     */
    public void updateDesignPhase(double phase) {
        RfCavityBucket rfCavBuc = this.getRfField();
        rfCavBuc.setPhase(phase);
    }

    /**
     * method to set the design amplitude
     *
     * @param amp new design amplitude (MV)
     */
    public void updateDesignAmp(double amp) {
        RfCavityBucket rfCavBuc = this.getRfField();
        rfCavBuc.setAmplitude(amp);
    }

    /**
     * return a polynomial fit of the transit time factor as a function of beta
     */
    public RealUnivariatePolynomial getTTFFit() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new RealUnivariatePolynomial(rfCavBuc.getTTFCoefs());
    }

    /**
     * return a polynomial fit of the transit time factor prime as a function of
     * beta
     */
    public RealUnivariatePolynomial getTTFPrimeFit() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new RealUnivariatePolynomial(rfCavBuc.getTTFPrimeCoefs());
    }

    /**
     * return a polynomial fit of the "S" transit time factor as a function of
     * beta
     */
    public RealUnivariatePolynomial getSTFFit() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new RealUnivariatePolynomial(rfCavBuc.getSTFCoefs());
    }

    /**
     * return a polynomial fit of the "S" transit time factor prime as a
     * function of beta
     */
    public RealUnivariatePolynomial getSTFPrimeFit() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new RealUnivariatePolynomial(rfCavBuc.getSTFPrimeCoefs());
    }

    /**
     * return a polynomial fit of the transit time factor for end cells as a
     * function of beta
     */
    public RealUnivariatePolynomial getTTFFitEnd() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new RealUnivariatePolynomial(rfCavBuc.getTTFEndCoefs());
    }

    /**
     * return a polynomial fit of the transit time factor prime for end cells as
     * a function of beta
     */
    public RealUnivariatePolynomial getTTFPrimeFitEnd() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new RealUnivariatePolynomial(rfCavBuc.getTTFPrimeEndCoefs());
    }

    /**
     * return a polynomial fit of the "S" transit time factor for end cells as a
     * function of beta
     */
    public RealUnivariatePolynomial getSTFFitEnd() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new RealUnivariatePolynomial(rfCavBuc.getSTFEndCoefs());
    }

    /**
     * return a polynomial fit of the "S" transit time factor prime for end
     * cells as a function of beta
     */
    public RealUnivariatePolynomial getSTFPrimeFitEnd() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return new RealUnivariatePolynomial(rfCavBuc.getSTFPrimeEndCoefs());
    }

    /**
     * returns 0 if the gap is part of a 0 mode cavity structure (e.g. DTL)
     * returns 1 if the gap is part of a pi mode cavity (e.g. CCL,
     * Superconducting)
     */
    public double getStructureMode() {
        RfCavityBucket rfCavBuc = this.getRfField();
        return rfCavBuc.getStructureMode();
    }

    /**
     * Get RF cavity frequency.
     *
     * @return RF cavity frequency (MHz)
     */
    public double getCavFreq() {
        return this.getRfField().getFrequency();
    }
}
