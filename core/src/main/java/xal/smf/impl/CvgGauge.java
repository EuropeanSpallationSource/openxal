package xal.smf.impl;

import xal.smf.impl.qualify.*;
import xal.ca.*;

/**
 * The CvgGauge Class element. This class contains the Convectron Gauge
 * implementation. This type of vacuum gauge is for higher pressures (during
 * rough pumpdown).
 *
 * @author J. Galambos
 *
 */
public class CvgGauge extends Vacuum {

    // static initialization
    static {
        registerType();
    }

    /**
     * standard type for instances of this class
     */
    public static final String TYPE = "CVG";


    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(CvgGauge.class, TYPE);
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
    public CvgGauge(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Constructor
     */
    public CvgGauge(final String strId) {
        this(strId, null);
    }
}
