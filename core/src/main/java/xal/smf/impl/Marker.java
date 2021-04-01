package xal.smf.impl;

import xal.smf.AcceleratorNode;
import xal.smf.impl.qualify.*;
import xal.tools.data.*;
import xal.ca.ChannelFactory;


/** node representing a simple marker */
public class Marker extends AcceleratorNode {
	/** for generaic marker */
    public static String s_strType   = "marker";
    
    public static String s_strSoftType   = null;
	
    /** for vacuum window */
    public static final String VIW = "VIW";
	
    /** for strip foil */
    public static final String Foil = "Foil";
	
    /** for target */
    public static final String Tgt = "Tgt";
	
    /** for harp */
    public static final String Harp = "Harp";
	
    /** Chumps */
    public static final String CHUMPS = "ChMPS";
	
	/** Laser Stripper */
	public static final String LASER_STRIPPER = "LStrp";
	


	/**
	 * Primary Constructor
	 * @param strID the unique node identifier
	 * @param channelFactory factory for generating channels
	 */
	public Marker( final String strID, final ChannelFactory channelFactory ) {
		super( strID, channelFactory );
	}


    /**
     * Constructor
	 * @param strID the unique node identifier
     */
    public Marker( final String strID ) {
        this( strID, null );
    }


    /** Overriden to provide type signature */
    public String getType()   { return s_strType; }

    /** Overriden to provide type signature */
    public String getSoftType()   { return s_strSoftType; }


	// static initializer
	static {
		registerType();
	}


	/**
	 * Register type for qualification.  These are the types that are common to all instances.
	 * The <code>isKindOf</code> method handles the type qualification specific to an instance.
	 * @see #isKindOf
	 */
	private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( Marker.class, s_strType );
	}


     /**
      * Update the instance with data from the data adaptor.  Overrides the default implementation to
 	 * set the marker type since a marker type can be "Foil", "VIW" "Tgt", etc.
      * @param adaptor The data provider.
      */
     public void update( final DataAdaptor adaptor ) {
         if (adaptor.hasAttribute("type")) {
             s_strType = adaptor.stringValue("type");
         }
         if (adaptor.hasAttribute("softType")) {
             s_strSoftType = adaptor.stringValue("softType");
             if (s_strSoftType.equals("")) {
                 s_strSoftType = null;
             }
         }
         super.update(adaptor);
     }
     
    /** 
      * Determine if this node is of the specified type.  Override the inherited
      * method since the types of generic nodes are not associated with the 
      * class unlike typical nodes.
      * @param type The type to compare against.
      * @return true if the node is a match and false otherwise.
      */
     public boolean isKindOf( final String type ) {
         return type.equalsIgnoreCase( s_strType ) || super.isKindOf( type );
     }
          
}
