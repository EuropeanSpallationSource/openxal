package xal.smf.impl;

import xal.ca.*;
import xal.smf.impl.qualify.*;


/** 
 * The implementation of the Neutron Detector  class. 
 * ND's are a subclass of loss monitors that detect neutron losses. 
 * A separate class is provided for these since they may need to
 * be grabbed separately.
 * 
 * @author  J. Galambos (jdg@ornl.gov)
 */
public class NeutronDetector extends BLM {
	/** standard type for instances of this class */
    public static final String TYPE   = "ND";
  

	// static initialization
    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(NeutronDetector.class, TYPE);
    }


    /** Override to provide type signature */
    @Override
    public String getType()   { return TYPE; }
  

	/** Constructor */
    public NeutronDetector( final String strId, final ChannelFactory channelFactory ) {
        super( strId, channelFactory );
        
    }


	/** Constructor */
	public NeutronDetector( final String strId ) {
		this( strId, null );

	}
}
