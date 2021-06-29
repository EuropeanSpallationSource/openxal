/* 
 * Sextupole.java
 */
package xal.smf.impl;

import xal.smf.impl.qualify.*;
import xal.ca.*;
import xal.tools.data.*;

/**
 * Sextupole magnet node.
 */
public class Sextupole extends Electromagnet {
    // Constants

    public static final String TYPE = "S";
    public static final String HORIZONTAL_TYPE = "SH";
    public static final String VERTICAL_TYPE = "SV";
    public static final String HORIZONTAL_SKEW_TYPE = "SSH";
    public static final String VERTICAL_SKEW_TYPE = "SSV";

    /**
     * identifies the type of sextupole (horizontal, vertical, skew)
     */
    protected String type;

    // static initializer
    static {
        registerType();
    }

    /**
     * Register type for qualification. These are the types that are common to
     * all instances. The <code>isKindOf</code> method handles the type
     * qualification specific to an instance.
     *
     * @see #isKindOf
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(Sextupole.class, TYPE, "emsext", "sext", "sextupole", MagnetType.SEXTUPOLE);
    }

    /**
     * Primary Constructor
     *
     * @param strID unique node ID
     */
    public Sextupole(final String strID, final ChannelFactory channelFactory) {
        super(strID, channelFactory);
    }

    /**
     * Constructor
     *
     * @param strID unique node ID
     */
    public Sextupole(final String strID) {
        this(strID, null);
    }

    /**
     * Override to provide the correct type signature per instance. This is
     * necessary since the Quadrupole class can represent more than one official
     * type (SH or SV).
     *
     * @return The official type consistent with the naming convention.
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Update the instance with data from the data adaptor. Overrides the
     * default implementation to set the sextupole type since a sextupole type
     * can be one of "SH", "SV", "SSH" or "SSV".
     *
     * @param adaptor The data provider.
     */
    @Override
    public void update(final DataAdaptor adaptor) {
        if (adaptor.hasAttribute("type")) {
            type = adaptor.stringValue("type");
        }
        super.update(adaptor);
    }

    /*
     * Determine whether this magnet is of the pole specified.
     * @param compPole The pole against which this magnet is being compared.
     * @return true if this magnet matches the specified pole.
     */
    @Override
    public boolean isPole(final String pole) {
        return pole.equals(MagnetType.SEXTUPOLE);
    }

    /**
     * Get the orientation of the magnet as defined by MagnetType. The
     * orientation of the sextupole is determined by its type.
     *
     * @return One of HORIZONTAL or VERTICAL
     */
    @Override
    public int getOrientation() {
        return (type.equalsIgnoreCase(HORIZONTAL_TYPE) || type.equalsIgnoreCase(HORIZONTAL_SKEW_TYPE)) ? HORIZONTAL : VERTICAL;
    }

    /**
     * Determine whether this magnet is a skew magnet.
     *
     * @return true if the magnet is skew and false otherwise.
     */
    @Override
    public boolean isSkew() {
        return type.equalsIgnoreCase(HORIZONTAL_SKEW_TYPE) || type.equalsIgnoreCase(VERTICAL_SKEW_TYPE);
    }

    /**
     * Determine if this node is of the specified type. Override the default
     * method since a quadrupole could represent either a vertical or horizontal
     * type. Must also handle inheritance checking so we must or the direct type
     * comparison with the inherited type checking.
     *
     * @param type the type against which to compare this sextupole
     * @return true if the node is a match and false otherwise.
     */
    @Override
    public boolean isKindOf(final String type) {
        return type.equalsIgnoreCase(this.type) || super.isKindOf(type);
    }

}
