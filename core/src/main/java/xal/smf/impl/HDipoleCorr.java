package xal.smf.impl;

import xal.ca.ChannelFactory;
import xal.smf.impl.qualify.ElementTypeManager;


/** 
 * The implementation of the Horizontal Dipole corrector element.
 * 
 * @author  J. Galambos (jdg@ornl.gov)
 * @author  Blaz Kranjc
 */

public class HDipoleCorr extends DipoleCorr {
	/** standard type for nodes of this class */
    public static final String TYPE   = "DCH";
  

	// static initialization
    static {
        registerType();
    }

    
    /**
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( HDipoleCorr.class, TYPE, "horzcorr", "hcorr" );
    }

    
    /** Override to provide type signature */
    @Override
    public String getType()   { return TYPE; }


	/** Primary Constructor */
	public HDipoleCorr( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
	}


	/** Constructor */
    public HDipoleCorr( final String strId )     {
        this( strId, null );
    }

    
    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of all horizontal correctors is HORIZONTAL.
     * @return HORIZONTAL
     */
    @Override
    public int getOrientation() {
        return HORIZONTAL;
    }
    
}
