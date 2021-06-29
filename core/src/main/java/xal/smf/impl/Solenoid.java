package xal.smf.impl;

import xal.ca.ChannelFactory;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.smf.impl.qualify.MagnetType;

public class Solenoid extends Electromagnet {

    public static final String TYPE = "SOL";

    // static initializer
    static {
        registerType();
    }

    /**
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(Solenoid.class, TYPE, MagnetType.SOLENOID);
    }

    /**
     * Primary Constructor
     *
     * @param strID the node's unique ID
     * @param channelFactory factory for generating channels
     */
    public Solenoid(final String strID, final ChannelFactory channelFactory) {
        super(strID, channelFactory);
    }

    /**
     * Constructor
     *
     * @param strID the dipole's unique ID
     */
    public Solenoid(final String strID) {
        this(strID, null);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
