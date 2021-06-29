/*
 * Bend.java
 *
 * Created on February 1, 2002, 1:25 PM
 */
package xal.smf.impl;

import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.ChannelFactory;

/**
 * Bend is used to represent a normal horizontal dipole magnet rather than a
 * corrector.
 *
 * @author tap
 */
public class Bend extends Dipole {

    /*
     *  Constants
     */
    public static final String TYPE = "DH";

    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(Bend.class, TYPE, "bend");
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
    public Bend(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Constructor
     */
    public Bend(final String strId) {
        this(strId, null);
    }

    /**
     * Get the orientation of the magnet as defined by MagnetType. The
     * orientation of all bends is Horizontal.
     *
     * @return HORIZONTAL
     */
    @Override
    public int getOrientation() {
        return HORIZONTAL;
    }

}
