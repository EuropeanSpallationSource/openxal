package xal.extension.jels.smf.impl;

import java.util.Arrays;
import xal.ca.ChannelFactory;
import xal.extension.jels.smf.attr.DipoleCorrBucket;
import xal.smf.impl.Magnet;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.smf.impl.qualify.MagnetType;

/**
 * Class for horizontal and vertical correctors.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class DipoleCorr extends xal.smf.impl.DipoleCorr {

    /**
     * Primary Constructor
     */
    public DipoleCorr(final String strId, int orientation, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setMagBucket(new DipoleCorrBucket());
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
    public static final String TYPE = "DC";
    public static final String[] TYPE_DCH = {"dch", "horzcorr", "hcorr"};
    public static final String[] TYPE_DCV = {"dcv", "vertcorr", "vcorr"};

    // static initializer
    static {
        registerType();
    }

    /**
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(DipoleCorr.class, TYPE);
    }

    /**
     * Override to provide type signature
     */
    public String getType() {
        return TYPE;
    }

    /**
     * Sets orientation of the magnet as defined by MagnetType.
     *
     * @param orientation orientation of the magnet, either HORIZONTAL or
     * VERTICAL
     */
    public void setOrientation(int orientation) {
        ((DipoleCorrBucket) getMagBucket()).setOrientation(orientation);
    }

    /**
     * Get the orientation of the magnet as defined by MagnetType. The
     * orientation of the dipole is determined by its type: DH or DV
     *
     * @return One of HORIZONTAL or VERTICAL
     */
    @Override
    public int getOrientation() {
        return ((DipoleCorrBucket) getMagBucket()).getOrientation();
    }

    @Override
    public boolean isKindOf(String type) {
        if (getOrientation() == Magnet.HORIZONTAL) {
            return type.equalsIgnoreCase(TYPE) || Arrays.asList(TYPE_DCH).contains(type.toLowerCase()) || super.isKindOf(type);
        } else {
            return type.equalsIgnoreCase(TYPE) || Arrays.asList(TYPE_DCV).contains(type.toLowerCase()) || super.isKindOf(type);
        }
    }
}
