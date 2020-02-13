/*
 * Created on Oct 23, 2003
 */
package xal.smf.proxy;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.sim.scenario.Scenario;
import xal.sim.scenario.ModelInput;
import xal.smf.AcceleratorNode;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.PermanentMagnet;
import xal.smf.impl.RfCavity;
import xal.ca.*;


/**
 * @author Craig McChesney
 */
public class PrimaryPropertyAccessor {
	/** indicates the debugging status for diagnostic typeout */
	private static final boolean DEBUG = false;
	
	// key = accelerator node, value = list of inputs for that node
	private Map<AcceleratorNode,Map<String,ModelInput>> nodeInputMap = new HashMap<>();
	
	/** cache of values (excluding model inputs) for node properties keyed by node and the subsequent map is keyed by property to get the value */
	final private Map<AcceleratorNode, Map<String,Double>> PROPERTY_VALUE_CACHE;

	/** batch accessor for node properties */
	private BatchPropertyAccessor _batchAccessor;
	
	
	/** Constructor */
	public PrimaryPropertyAccessor() {
		PROPERTY_VALUE_CACHE = new HashMap<>();
		_batchAccessor = BatchPropertyAccessor.getInstance( Scenario.SYNC_MODE_DESIGN );
	}


	/** request values for the nodes and the specified sync mode */
	public void requestValuesForNodes( final Collection<AcceleratorNode> nodes, final String syncMode ) {
		final BatchPropertyAccessor batchAccessor = BatchPropertyAccessor.getInstance( syncMode );
		batchAccessor.requestValuesForNodes( nodes );
		_batchAccessor = batchAccessor;
	}

	
	/**
	 * Returns a Map of property values for the supplied node.The map's keys are the property names as defined by the node class' propertyNames
 method, values are the Double value for that property on aNode.
	 * @param objNode the AcclereatorNode whose properties to return
	 * @return a Map of node property values
	 */
	public Map<String,Double> valueMapFor( final Object objNode ) {
		if ( (objNode == null) ) {
			throw new IllegalArgumentException( "null arguments not allowed by doubleValueFor" );
		}
		if (! (objNode instanceof AcceleratorNode)) {
			throw new IllegalArgumentException( "expected instance of AcceleratorNode" );
		}

		AcceleratorNode aNode = (AcceleratorNode) objNode;
		
		PropertyAccessor nodeAccessor = getAccessorFor( aNode );
		if (nodeAccessor == null) {
			throw new IllegalArgumentException( "unknown node type: " + aNode.getClass().getName() );
		}

		final Map<String,Double> valueMap = _batchAccessor.valueMapFor( aNode );

		// cache the values
		PROPERTY_VALUE_CACHE.put( aNode, new HashMap<String,Double>( valueMap ) );		// need to copy it so we don't override the raw values

		// apply whatif settings
		addInputOverrides( aNode, valueMap );

		if (DEBUG) printValueMap( aNode, valueMap );
		
		return valueMap;
	}
	
	
	/** Use the cache rather than other sources for the value map and then apply the model inputs */
	public Map<String,Double> getWhatifValueMapFromCache( final Object objNode ) {
		if ( objNode == null ) {
			throw new IllegalArgumentException( "null arguments not allowed by doubleValueFor" );
		}
		else if ( !(objNode instanceof AcceleratorNode) ) {
			throw new IllegalArgumentException( "expected instance of AcceleratorNode" );
		}
		final AcceleratorNode aNode = (AcceleratorNode)objNode;
		final Map<String,Double> valueMap = new HashMap<String,Double>( PROPERTY_VALUE_CACHE.get( aNode ) );		// need to copy it so we don't override the raw values
		addInputOverrides( aNode, valueMap );
		return valueMap;
	}
	
	
	/**
	 * Returns a List of property names for the supplied node.
	 * @param aNode AcceleratorNode whose property names to return
	 * @return a List of property names for aNode
	 */
	public List<String> propertyNamesFor( final AcceleratorNode aNode ) {
		return _batchAccessor.propertyNamesFor( aNode );
	}


	/** get the accessor for the specified node */
	public PropertyAccessor getAccessorFor( final AcceleratorNode node ) {
		return BatchPropertyAccessor.getAccessorFor( node );
	}

	
	/**
	 * Returns true if there is an accessor for the specified node type, false otherwise.
	 * @param aNode AcceleratorNode whose type to find an accessor for
	 * @return true if there is an accessor for the supplied node, false otherwise
	 */
	public boolean hasAccessorFor(AcceleratorNode aNode) {
		return _batchAccessor.hasAccessorFor( aNode );
	}


	// Model Input Data: Node Property Overrides ===============================

	private void addInputOverrides( final AcceleratorNode aNode, final Map<String,Double> valueMap ) {
		Map<String,ModelInput> inputs = inputsForNode( aNode );
		if (inputs == null) return;
		for ( final ModelInput input : inputs.values() ) {
			final String property = input.getProperty();
			valueMap.put( property, input.getDoubleValue() );
		}
	}
	
	/**
	 * Sets the specified node's property to the specified value.  Replaces the
	 * existing value if there is one.
	 * 
	 * @param aNode node whose property to set
	 * @param property name of property to set
	 * @param val double value for property
	 */
	public ModelInput setModelInput(AcceleratorNode aNode, String property, double val) {
		ModelInput existingInput = getInput( aNode, property );
		if (existingInput != null) {
			existingInput.setDoubleValue(val);
			return existingInput;
		} else {
			ModelInput input = new ModelInput( aNode, property, val );
			addInput(input);
			return input;
		}
	}
	
	/**
	 * Returns the ModelInput for the specified node's property, or null if there
	 * is none.
	 * 
	 * @param aNode node whose property to get a ModelInput for
	 * @param propName name of property to get a ModelInput for
	 */
	public ModelInput getInput(AcceleratorNode aNode, String propName) {
		final Map<String,ModelInput> inputs = inputsForNode(aNode);
		return inputs != null ? inputs.get( propName ) : null;
	}
	
	protected void addInput( final ModelInput anInput ) {
		final AcceleratorNode node = anInput.getAcceleratorNode();
		Map<String,ModelInput> inputs = inputsForNode(node);
		if (inputs == null) {
			inputs = new HashMap<>();
			nodeInputMap.put( node, inputs );
		}
		inputs.put( anInput.getProperty(), anInput );		
	}
	
	
	public void removeInput(AcceleratorNode aNode, String property) {
		final Map<String,ModelInput> inputs = inputsForNode(aNode);
		if ( inputs != null ) {
			inputs.remove( property );
		}
	}
	
	
	private Map<String,ModelInput> inputsForNode( final AcceleratorNode aNode ) {
		return nodeInputMap.get( aNode );
	}
	
	
	// Testing and Debugging ===================================================
	
	private static void printValueMap( final AcceleratorNode aNode, final Map<String,Double> values ) {
		System.out.println( "Properties for node: " + aNode );

		for ( final String property : values.keySet() ) {
			Double val = values.get( property );
			System.out.println("\t" + property + ": " + val); 
		}
		
		System.out.println();
	}
}



/** Accessor for property values in batch */
abstract class BatchPropertyAccessor {
	/** map of property accessors keyed by node */
	final private static Map<Class<?>,PropertyAccessor> NODE_ACCESSORS = new HashMap<Class<?>,PropertyAccessor>();


	// static initializer
	static {
		// Accessor Registration
		registerAccessorInstance( Electromagnet.class, new ElectromagnetPropertyAccessor() );
		registerAccessorInstance( RfCavity.class, new RfCavityPropertyAccessor() );
		registerAccessorInstance( PermanentMagnet.class, new PermanentMagnetPropertyAccessor() );
	}


	/** register the property accessor for each supported node class */
	private static void registerAccessorInstance( final Class<?> nodeClass, final PropertyAccessor accessor ) {
		NODE_ACCESSORS.put( nodeClass, accessor );
	}


	/**
	 * Returns a List of property names for the supplied node.
	 * @param aNode AcceleratorNode whose property names to return
	 * @return a List of property names for aNode
	 */
	public List<String> propertyNamesFor( final AcceleratorNode aNode ) {
		if ( aNode == null )  throw new IllegalArgumentException("can't get property names for null node");
		final PropertyAccessor nodeAccessor = getAccessorFor( aNode );
		if (nodeAccessor == null)  throw new IllegalArgumentException( "unregistered node type: " + aNode.getClass().getName() );
		return nodeAccessor.propertyNames();
	}


	/** get the accessor for the specified node */
	protected static PropertyAccessor getAccessorFor( final AcceleratorNode node ) {
		for ( final Class<?> nodeClass : NODE_ACCESSORS.keySet() ) {
			if ( nodeClass.isInstance( node ) ) {
				return NODE_ACCESSORS.get( nodeClass );
			}
		}
		
		return null;
	}

	/**
	 * Returns true if there is an accessor for the specified node type, false otherwise.
	 * @param aNode AcceleratorNode whose type to find an accessor for
	 * @return true if there is an accessor for the supplied node, false otherwise
	 */
	public boolean hasAccessorFor(AcceleratorNode aNode) {
		return getAccessorFor( aNode ) != null;
	}


	/** make the request for values for the specified nodes */
	abstract public void requestValuesForNodes( final Collection<AcceleratorNode> nodes );


	/**
	 * Get a Map of property values for the supplied node keyd by property name.
	 * @param node the AcclereatorNode whose properties to return
	 * @return a Map of node property values keyed by property name
	 */
	abstract public Map<String,Double> valueMapFor( final AcceleratorNode node );
	

	/** get the instance for the specified synchronization mode */
	static BatchPropertyAccessor getInstance( final String syncMode ) {
		if ( syncMode == null ) {
			throw new IllegalArgumentException( "Null Synchronization mode" );
		}
		else if ( syncMode.equals( Scenario.SYNC_MODE_LIVE ) ) {
			return new LiveBatchPropertyAccessor();
		}
		else if ( syncMode.equals( Scenario.SYNC_MODE_DESIGN ) ) {
			return new DesignBatchPropertyAccessor();
		}
		else if ( syncMode.equals( Scenario.SYNC_MODE_RF_DESIGN ) ) {
			return new LiveRFDesignBatchPropertyAccessor();
		}
		else {
			throw new IllegalArgumentException( "Unknown Synchronization mode: " + syncMode );
		}
	}
}



/** Accessor for property values in batch */
class DesignBatchPropertyAccessor extends BatchPropertyAccessor {
	/** make the request for values for the specified nodes */
        @Override
	public void requestValuesForNodes( final Collection<AcceleratorNode> nodes ) {}


	/**
	 * Get a Map of property values for the supplied node keyd by property name.
	 * @param node the AcclereatorNode whose properties to return
	 * @return a Map of node property values
	 */
        @Override
	public Map<String,Double> valueMapFor( final AcceleratorNode node ) {
		final PropertyAccessor accessor = getAccessorFor( node );
		return accessor.getDesignValueMap( node );
	}
}



/** batch property accessor which is based on channels */
abstract class BatchChannelPropertyAccessor extends BatchPropertyAccessor {
	/** channel value keyed by channel */
	protected Map<Channel,Double> _channelValues;


	/** make the request for values for the specified nodes */
         @Override
	public void requestValuesForNodes( final Collection<AcceleratorNode> nodes ) {
		// assign an empty map at the start should something go wrong later
		_channelValues = Collections.<Channel,Double>emptyMap();

		// collect all the channels from every node's properties
		final Set<Channel> channels = new HashSet<>();
		for ( final AcceleratorNode node : nodes ) {
			final PropertyAccessor accessor = getAccessorFor( node );
			channels.addAll( getChannels( accessor, node ) );
		}

		// create and submit a batch channel Get request
		final BatchGetValueRequest request = new BatchGetValueRequest( channels );
		request.submitAndWait( 5.0 );	// wait up to 5 seconds for a response

		// print an overview of the request status
		if ( !request.isComplete() ) {
			final int requestCount = channels.size();
			final int recordCount = request.getRecordCount();
			final int exceptionCount = request.getExceptionCount();
			Logger.getLogger(BatchChannelPropertyAccessor.class.getName()).log(Level.WARNING, "Batch channel request for online model is incomplete. {0} of {1} channels succeeded. {2} channels had exceptions.", new Object[]{recordCount, requestCount, exceptionCount});
		}

		// gather values for the channels in a map keyed by channel
		final Map<Channel,Double> channelValues = new HashMap<>();
		final List<String> unreadChannels = new ArrayList<>();
		for ( final Channel channel : channels ) {
			final ChannelRecord record = request.getRecord( channel );
			if ( record != null ) {
				channelValues.put( channel, record.doubleValue() );
			}
			else {
                                unreadChannels.add(channel.getId());	
			}
		}
                 if (!unreadChannels.isEmpty()){
                          Logger.getLogger(BatchChannelPropertyAccessor.class.getName()).log(Level.WARNING, "No record for channels: {0}", unreadChannels);
                 }

		_channelValues = channelValues;
	}


	/**
	 * Get a Map of property values for the supplied node keyd by property name.
	 * @param node the AcclereatorNode whose properties to return
	 * @return a Map of node property values
	 */
         @Override
	public Map<String,Double> valueMapFor( final AcceleratorNode node ) {
		final PropertyAccessor accessor = getAccessorFor( node );
		return getValueMap( accessor, node );
	}


	/** get the channels for the specified node */
	protected abstract Collection<Channel> getChannels( final PropertyAccessor accessor, final AcceleratorNode node );


	/** get the value map for the specified node */
	protected abstract Map<String,Double> getValueMap( final PropertyAccessor accessor, final AcceleratorNode node );
}



/** Accessor for property values in batch */
class LiveBatchPropertyAccessor extends BatchChannelPropertyAccessor {
	/** get the channels for the specified node */
        @Override
	protected Collection<Channel> getChannels( final PropertyAccessor accessor, final AcceleratorNode node ) {
		return accessor.getLiveChannels( node );
	}


	/** get the value map for the specified node */
        @Override
	protected Map<String,Double> getValueMap( final PropertyAccessor accessor, final AcceleratorNode node ) {
		return accessor.getLiveValueMap( node, _channelValues );
	}
}



/** Accessor for property values in batch */
class LiveRFDesignBatchPropertyAccessor extends BatchChannelPropertyAccessor {
	/** get the channels for the specified node */
        @Override
	protected Collection<Channel> getChannels( final PropertyAccessor accessor, final AcceleratorNode node ) {
		return accessor.getLiveRFDesignChannels( node );
	}


	/** get the value map for the specified node */
        @Override
	protected Map<String,Double> getValueMap( final PropertyAccessor accessor, final AcceleratorNode node ) {
		return accessor.getLiveRFDesignValueMap( node, _channelValues );
	}
}
