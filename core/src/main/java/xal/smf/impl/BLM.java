package xal.smf.impl;

import xal.ca.*;
import xal.smf.*;
import xal.smf.impl.qualify.*;

/**
 * The implementation of the BLM class. This class contains the methods members,
 * attributes, and signal sets pertinent to modeling Beam Loss monitors.
 *
 * @author J. Galambos (jdg@ornl.gov)
 */
public class BLM extends AcceleratorNode {

    /*
     *  Constants
     */
    public static final String TYPE = "BLM";

    // BLM channel handles
    /**
     * BLMs official avg channel handle
     */
    public static final String LOSS_AVG_HANDLE = "lossAvg";
    public final AccessibleProperty lossAvg = new AccessibleProperty(LOSS_AVG_HANDLE, LOSS_AVG_HANDLE);
    private Channel lossAvgC = null;

    /**
     * BLMs official integrated channel handle
     */
    public static final String LOSS_INT_HANDLE = "lossInt";
    public final AccessibleProperty lossInt = new AccessibleProperty(LOSS_INT_HANDLE, LOSS_INT_HANDLE);
    private Channel lossIntC = null;

    /**
     * BLM official tAvgLen channel handle
     */
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
        ElementTypeManager.defaultManager().registerTypes(BLM.class, TYPE);
    }


    /*
     *  Local Attributes
     */
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
    public BLM(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Constructor
     */
    public BLM(final String strId) {
        this(strId, null);
    }


    /*
     *  Process variable Gets
     */
    /**
     * returns average loss
     */
    public double getLossAvg() throws GetException {
        lossAvgC = lazilyGetAndConnect(LOSS_AVG_HANDLE, lossAvgC);
        return lossAvgC.getValDbl();
    }

    /**
     * returns integrated loss
     */
    public double getLossInt() throws GetException {
        lossIntC = lazilyGetAndConnect(LOSS_INT_HANDLE, lossIntC);
        return lossIntC.getValDbl();
    }

    /**
     * returns length of the averaged period (micro-sec)
     */
    public double getTAvgLen() throws GetException {
        tAvgLenC = lazilyGetAndConnect(T_AVG_LEN_HANDLE, tAvgLenC);
        return tAvgLenC.getValDbl();
    }
}
