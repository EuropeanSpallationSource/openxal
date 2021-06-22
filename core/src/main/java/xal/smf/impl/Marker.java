package xal.smf.impl;

import xal.smf.AcceleratorNode;
import xal.smf.impl.qualify.*;
import xal.tools.data.*;
import xal.ca.ChannelFactory;


/** node representing a simple marker */
public class Marker extends AcceleratorNode {
	/** for generic marker */
    public static final String TYPE   = "marker";
    public String type   =  TYPE;
    
    public String softType   = null;
	
    /** for vacuum window */
    public static final String VIW = "VIW";
	
    /** for strip foil */
    public static final String FOIL = "Foil";
	
    /** for target */
    public static final String TARGET = "Tgt";
	
    /** for harp */
    public static final String HARP = "Harp";
	
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
    @Override
    public String getType()   { return type; }

    /** Overriden to provide type signature */
    @Override
    public String getSoftType()   { return softType; }


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
		ElementTypeManager.defaultManager().registerTypes(Marker.class, TYPE );
	}


     /**
      * Update the instance with data from the data adaptor.  Overrides the default implementation to
 	 * set the marker type since a marker type can be "Foil", "VIW" "Tgt", etc.
      * @param adaptor The data provider.
      */
    @Override
     public void update( final DataAdaptor adaptor ) {
         if (adaptor.hasAttribute("type")) {
             type = adaptor.stringValue("type");
         }
         if (adaptor.hasAttribute("softType")) {
             softType = adaptor.stringValue("softType");
             if (softType.equals("")) {
                 softType = null;
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
    @Override
     public boolean isKindOf( final String type ) {
         return type.equalsIgnoreCase(this.type ) || super.isKindOf( type );
     }
          
}
