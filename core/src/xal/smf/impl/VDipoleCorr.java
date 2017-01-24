package xal.smf.impl;

import xal.ca.ChannelFactory;
import xal.smf.impl.qualify.ElementTypeManager;


/** 
 * The implementation of the Vertical Dipole corrector element.
 * 
 * @author  J. Galambos (jdg@ornl.gov)
 * @author  Blaz Kranjc
 */

public class VDipoleCorr extends DipoleCorr {
	/** standard type for nodes of this class */
    public static final String s_strType   = "DCV";
  

	// static initialization
    static {
        registerType();
    }

    
    /**
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( VDipoleCorr.class, s_strType, "vertcorr", "vcorr" );
    }

    
    /** Override to provide type signature */
    public String getType()   { return s_strType; }


	/** Constructor */
	public VDipoleCorr( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
	}


	/** Constructor */
    public VDipoleCorr( final String strId )     {
        this( strId, null );
    }
    

    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of all vertical correctors is VERTICAL.
     * @return VERTICAL
     */
    public int getOrientation() {
        return VERTICAL;
    }
    
}
