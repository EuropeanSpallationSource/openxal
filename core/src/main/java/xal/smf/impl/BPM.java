package xal.smf.impl;

import xal.ca.*;
import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;

/**
 * The implementation of the BPM class. This class contains the methods members,
 * attributes, and signal sets pertinent to modeling Beam Position monitors.
 *
 * @author J. Galambos (jdg@ornl.gov)
 */
public class BPM extends AcceleratorNode {

    /*
     *  Constants
     */
    public static final String TYPE = "BPM";

    /**
     * The container for the bpm information
     *
     */
    protected BPMBucket bpmBucket;

    // BPM channel handles
    // BPMs official xAvg channel handle
    public static final String X_AVG_HANDLE = "xAvg";
    public final AccessibleProperty xAvg = new AccessibleProperty(X_AVG_HANDLE, X_AVG_HANDLE);
    private Channel xAvgC = null;

    // BPMs official yAvg channel handle
    public static final String Y_AVG_HANDLE = "yAvg";
    public final AccessibleProperty yAvg = new AccessibleProperty(Y_AVG_HANDLE, Y_AVG_HANDLE);
    private Channel yAvgC = null;

    // BPMs official ampAvg channel handle
    public static final String AMP_AVG_HANDLE = "amplitudeAvg";
    public final AccessibleProperty amplitudeAvg = new AccessibleProperty(AMP_AVG_HANDLE, AMP_AVG_HANDLE);
    private Channel ampAvgC = null;

    // BPMs official phaseAvg channel handle
    public static final String PHASE_AVG_HANDLE = "phaseAvg";
    public final AccessibleProperty phaseAvg = new AccessibleProperty(PHASE_AVG_HANDLE, PHASE_AVG_HANDLE);
    private Channel phaseAvgC = null;

    // BPMs official x turn-by-turn channel handle
    public static final String X_TBT_HANDLE = "xTBT";
    public final AccessibleProperty xTBT = new AccessibleProperty(X_TBT_HANDLE, X_TBT_HANDLE);
    private Channel xTBTC = null;

    // BPMs official y turn-by-turn channel handle
    public static final String Y_TBT_HANDLE = "yTBT";
    public final AccessibleProperty yTBT = new AccessibleProperty(Y_TBT_HANDLE, Y_TBT_HANDLE);
    private Channel yTBTC = null;

    // BPMs official amplitude turn-by-turn channel handle
    public static final String AMP_TBT_HANDLE = "ampTBT";
    public final AccessibleProperty ampTBT = new AccessibleProperty(AMP_TBT_HANDLE, AMP_TBT_HANDLE);
    private Channel ampTBTC = null;

    // BPMs official phase turn-by-turn channel handle
    public static final String PHASE_TBT_HANDLE = "phaseTBT";
    public final AccessibleProperty phaseTBT = new AccessibleProperty(PHASE_TBT_HANDLE, PHASE_TBT_HANDLE);
    private Channel phaseTBTC = null;

    // BPM official tAvgLen channel handle
    public static final String T_AVG_LEN_HANDLE = "tAvgLen";
    public final AccessibleProperty tAvgLen = new AccessibleProperty(T_AVG_LEN_HANDLE, T_AVG_LEN_HANDLE);
    private Channel tAvgLenC = null;

    static {
        registerType();
    }


    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(BPM.class, TYPE);
    }

    /**
     * Override to provide type signature
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Constructor
     */
    public BPM(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setBPMBucket(new BPMBucket());
    }

    /**
     * Constructor
     */
    public BPM(final String strId) {
        this(strId, null);
    }

    /**
     * return the BPM Bucket
     */
    public BPMBucket getBPMBucket() {
        return bpmBucket;
    }

    /**
     * Set the attribute bucket containing the bpm info
     */
    public void setBPMBucket(BPMBucket buc) {
        bpmBucket = buc;
        super.addBucket(buc);
    }

    /**
     *
     * Override AcceleratorNode implementation to check for a BPMBucket
     */
    @Override
    public void addBucket(AttributeBucket buc) {

        if (buc.getClass().equals(BPMBucket.class)) {
            setBPMBucket((BPMBucket) buc);
        }
        super.addBucket(buc);
    }

    /*
     *  Process variable Gets
     */
    /**
     * returns average X position over macropulse (mm) accounting for alignment
     */
    public double getXAvg() throws GetException {
        xAvgC = lazilyGetAndConnect(X_AVG_HANDLE, xAvgC);
        return xAvgC.getValDbl();
    }

    /**
     * returns average Y position over macropulse (mm) accounting for alignment
     */
    public double getYAvg() throws GetException {
        yAvgC = lazilyGetAndConnect(Y_AVG_HANDLE, yAvgC);
        return yAvgC.getValDbl();
    }

    /**
     * returns average bpm Amplitude signal over macropulse (au)
     */
    public double getAmpAvg() throws GetException {
        ampAvgC = lazilyGetAndConnect(AMP_AVG_HANDLE, ampAvgC);
        return ampAvgC.getValDbl();
    }

    /**
     * returns average bpm phase signal over macropulse (au)
     */
    public double getPhaseAvg() throws GetException {
        phaseAvgC = lazilyGetAndConnect(PHASE_AVG_HANDLE, phaseAvgC);
        return phaseAvgC.getValDbl();
    }

    /**
     * returns bpm x turn-by-turn array
     */
    public double[] getXTBT() throws GetException {
        xTBTC = lazilyGetAndConnect(X_TBT_HANDLE, xTBTC);
        return xTBTC.getArrDbl();
    }

    /**
     * returns bpm y turn-by-turn array
     */
    public double[] getYTBT() throws GetException {
        yTBTC = lazilyGetAndConnect(Y_TBT_HANDLE, yTBTC);
        return yTBTC.getArrDbl();
    }

    /**
     * returns bpm amplitude turn-by-turn array
     */
    public double[] getAmpTBT() throws GetException {
        ampTBTC = lazilyGetAndConnect(AMP_TBT_HANDLE, ampTBTC);
        return ampTBTC.getArrDbl();
    }

    /**
     * returns bpm phase turn-by-turn array
     */
    public double[] getPhaseTBT() throws GetException {
        phaseTBTC = lazilyGetAndConnect(PHASE_TBT_HANDLE, phaseTBTC);
        return phaseTBTC.getArrDbl();
    }

    /**
     * returns length of the averaged period (micro-sec)
     */
    public double getTAvgLen() throws GetException {
        tAvgLenC = lazilyGetAndConnect(T_AVG_LEN_HANDLE, tAvgLenC);
        return tAvgLenC.getValDbl();
    }
}
