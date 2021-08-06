/*
 * PermanentMagnet.java
 *
 * Created on January 30, 2002, 2:02 PM
 */
package xal.smf.impl;

import java.util.List;
import xal.smf.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;

/**
 * PermanentMagnet is the superclass of all permanent magnet classes.
 *
 * @author tap
 */
public abstract class PermanentMagnet extends Magnet {

    // accessible properties
    private String[] readbackHandle = new String[]{};
    public final AccessibleProperty field = new AccessibleProperty("field", readbackHandle, null, this::getDesignField, cV -> getDesignField());

    // static initializer
    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(PermanentMagnet.class, "pmag", "permanentmagnet");
    }

    /**
     * Creates new PermanentMagnet
     */
    protected PermanentMagnet(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Creates new PermanentMagnet
     */
    protected PermanentMagnet(final String strId) {
        this(strId, null);
    }

    /**
     * Since this is a permanent magnet we override the inherited method to
     * advertise this characteristic.
     *
     * @return true since all PermanentMagnet instances are permanent magnets.
     */
    @Override
    public boolean isPermanent() {
        return true;
    }

    /**
     * Get the array of channels for the specified property
     */
    @Override
    public Channel[] getLivePropertyChannels(final String propertyName) {
        List<AccessibleProperty> properties = getAccessibleProperties();
        for (AccessibleProperty prop : properties) {
            if (prop.getName().equals(propertyName) && prop.hasGetters()) {
                return new Channel[0];
            }
        }
        throw new IllegalArgumentException("Unsupported PermanentMagnet live channels property: " + propertyName);
    }

    /**
     * returns the field of the magnet (T /(m^ (n-1))), n=1 for dipole, 2 for
     * quad etc.
     */
    public double getField() {

        return getDesignField();
    }

    /**
     * returns the integrated field of the magnet (T-m /(m^ (n-1))), n=1 for
     * dipole, 2 for quad etc.
     */
    public double getFieldInt() {

        return (getDesignField() * getEffLength());
    }
}
