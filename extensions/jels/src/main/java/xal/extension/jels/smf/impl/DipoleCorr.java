package xal.extension.jels.smf.impl;

import xal.ca.ChannelFactory;
import xal.extension.jels.smf.attr.MagnetBucket;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.smf.impl.qualify.MagnetType;

/**
 * Base class for dipole correctors.
 *
 * @author Blaz Kranjc
 */
public class DipoleCorr extends xal.smf.impl.DipoleCorr {

    private final MagnetBucket m_bucESSMagnet = new MagnetBucket();

    /**
     * Primary Constructor
     */
    public DipoleCorr(final String strId, int orientation, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setMagBucket(m_bucESSMagnet);
        setOrientation(orientation);
    }

    public DipoleCorr(final String strId, int orientation) {
        this(strId, orientation, null);
    }

    public DipoleCorr(final String strId) {
        this(strId, MagnetType.HORIZONTAL, null);
    }

    public DipoleCorr(final String strId, final ChannelFactory channelFactory) {
        this(strId, MagnetType.HORIZONTAL, channelFactory);
    }

    /**
     * standard type for nodes of this class
     */
    public static final String s_strType = "DC";

    // static initializer
    static {
        registerType();
    }

    /**
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(DipoleCorr.class, s_strType);
    }

    /**
     * Override to provide type signature
     */
    public String getType() {
        return s_strType;
    }

    /**
     * Sets orientation of the magnet as defined by MagnetType.
     *
     * @param orientation orientation of the magnet, either HORIZONTAL or
     * VERTICAL
     */
    public void setOrientation(int orientation) {
        m_bucESSMagnet.setOrientation(orientation);
    }

    /**
     * Get the orientation of the magnet as defined by MagnetType. The
     * orientation of the dipole is determined by its type: DH or DV
     *
     * @return One of HORIZONTAL or VERTICAL
     */
    @Override
    public int getOrientation() {
        return m_bucESSMagnet.getOrientation();
    }
}
