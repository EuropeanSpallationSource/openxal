package xal.smf.impl;

import xal.ca.ChannelFactory;
import xal.smf.*;
import xal.smf.impl.qualify.*;

/**
 * Representation of a Bunch Shape Monitor
 *
 * @author tom pelaia
 */
public class BunchShapeMonitor extends AcceleratorNode {

    /**
     * device type
     */
    public static final String TYPE = "BSM";

    // static initializer
    static {
        registerType();
    }

    /* Register type for qualification */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(BunchShapeMonitor.class, TYPE);
    }

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
    public BunchShapeMonitor(final String deviceID, final ChannelFactory channelFactory) {
        super(deviceID, channelFactory);
    }

    /**
     * Constructor
     */
    public BunchShapeMonitor(final String deviceID) {
        this(deviceID, null);
    }
}
