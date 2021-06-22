package xal.smf;

import xal.ca.ChannelFactory;
import xal.smf.impl.*;
import xal.tools.data.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * Class factory for all AcceleratorNode objects.
 * The factory is used in parsing XML files in XDXF format.  Every AcceleratorNode has a type code (in String format) which may be used to instantiate the class.
 * @author  Nikolay Malitsky, Christopher K. Allen, Tom Pelaia
 */

public final class AcceleratorNodeFactory {
	/** channel factory from which the nodes will generate channels */
	private final ChannelFactory channelFactory;

	/** map of constructors keyed by node type */
	private Map<String,Constructor<? extends Object>> constructors = new HashMap<>();

	/** map of classes keyed by node type */
	private Map<String,Class<?>> classTable;


	/** Constructor */
	public AcceleratorNodeFactory( final ChannelFactory channelFactory ) {
		this.channelFactory = channelFactory != null ? channelFactory : ChannelFactory.defaultFactory();

		constructors = new HashMap<>();
		classTable = new HashMap<>();
	}


	/** Constructor */
	public AcceleratorNodeFactory() {
		this( null );
	}


    /**
     *  Associate the specified AcceleratorNode class with the specified node type
	 *  @param  deviceType  device type
	 *  @param  softType    software type (null indicates there is no software type)
     *  @param  nodeClass   Class class for the AcceleratorNode
     */
    public <T extends AcceleratorNode> void registerNodeClass( final String deviceType, final String softType, final Class<T> nodeClass )   {
		final String nodeType = softType != null ? deviceType + "." + softType : deviceType;
		registerNodeClass( nodeType, nodeClass );
    }


    /**
     *  Associate the specified AcceleratorNode class with the specified node type
	 *  @param  nodeType    fully qualified node type (e.g. deviceType.softType)
     *  @param  nodeClass   Class class for the AcceleratorNode
     */
    private <T extends AcceleratorNode> void registerNodeClass( final String nodeType, final Class<T> nodeClass )   {
        classTable.put( nodeType, nodeClass );

        try {
			@SuppressWarnings( "rawtypes" )
            final Constructor<T> constructor = nodeClass.getConstructor( new Class[] { String.class, ChannelFactory.class } );
            constructors.put( nodeType, constructor );
        }
		catch ( NoSuchMethodException | SecurityException exception ) {
			final String message = "AcceleratorNodeFactory: class registeration failure for type: " + nodeType;
            System.err.println( message );
			Logger.getLogger("global").log( Level.SEVERE, message, exception );
        }
    }


    /**
     * Creates the node with the specified node id and fully qualified node type.
	 * @param nodeID device ID
	 * @param nodeType fully qualified node type (e.g. deviceType.softType)
     */
    private AcceleratorNode createNode( final String nodeID, final String nodeType ) throws ClassNotFoundException {
        // Check if this node type is known; if not then substitute a generic node
        if ( !constructors.containsKey( nodeType ) ) {
			final String message = "Unknown AcceleratorNode type : \"" + nodeType + "\" for ID: " + nodeID + ".  Will substitute a GenericNode!";
            System.err.println( message );
			Logger.getLogger("global").log( Level.WARNING, message );
            final AcceleratorNode node = new GenericNode( nodeType, nodeID, channelFactory );
            classTable.put( nodeType, GenericNode.class );
            return node;
        }

        final Constructor<?> constructor = constructors.get( nodeType );
		//TODO: need to account for custom channel factory
        final Object[] args = new Object[] { nodeID, channelFactory };

        try {
            return (AcceleratorNode)constructor.newInstance( args );
        }
		catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException exception)   {
             throw new ClassNotFoundException( "Unknown AcceleratorNode type : " + nodeType );
        }
    }


    /** create an accelerator node based on a DataAdaptor */
    public AcceleratorNode createNode( final DataAdaptor adaptor ) throws ClassNotFoundException {
        final String nodeID = adaptor.stringValue( "id" );
        final String deviceType = adaptor.stringValue( "type" );
		final String softType = adaptor.hasAttribute( "softType" ) ? adaptor.stringValue( "softType" ) : null;
		final String nodeType = softType != null ? deviceType + "." + softType : deviceType;

        return createNode( nodeID, nodeType );
    }

    /**
     * Return the class table used by this node factory.
     * @return
     */
    public Map<String, Class<?>> getClassTable() {
        return classTable;
    }

    public static AcceleratorNodeFactory getDefaultFactory() {
        AcceleratorNodeFactory factory = new AcceleratorNodeFactory();

        factory.registerNodeClass("sequence", AcceleratorSeq.class);
        factory.registerNodeClass("dh", Dipole.class);
        factory.registerNodeClass("dhe", EDipole.class);
        factory.registerNodeClass("dve", EDipole.class);
        factory.registerNodeClass("QSC", Quadrupole.class);
        factory.registerNodeClass("q", Quadrupole.class);
        factory.registerNodeClass("qhe", EQuad.class);
        factory.registerNodeClass("qve", EQuad.class);
        factory.registerNodeClass("pq", PermQuadrupole.class);
        factory.registerNodeClass("S", Sextupole.class);
        factory.registerNodeClass("SOL", Solenoid.class);
        factory.registerNodeClass("rfgap", RfGap.class);
        factory.registerNodeClass("bcm", Marker.class);
        factory.registerNodeClass("dch", DipoleCorr.class);
        factory.registerNodeClass("dcv", DipoleCorr.class);
        factory.registerNodeClass("EKick", ExtractionKicker.class);
        factory.registerNodeClass("bpm", BPM.class);
        factory.registerNodeClass("bsm", BunchShapeMonitor.class);
        factory.registerNodeClass("blm", BLM.class);
        factory.registerNodeClass("ws", Marker.class);
        factory.registerNodeClass("marker", Marker.class);

        return factory;
    }
}
