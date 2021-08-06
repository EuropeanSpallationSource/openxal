/*
 * Created on Oct 23, 2003
 */
package xal.smf.proxy;

import java.util.*;
import java.util.Map.Entry;
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

    private static final Logger LOGGER = Logger.getLogger(PrimaryPropertyAccessor.class.getName());

    /**
     * indicates the debugging status for diagnostic typeout
     */
    private static final boolean DEBUG = false;

    // key = accelerator node, value = list of inputs for that node
    private Map<AcceleratorNode, Map<String, ModelInput>> nodeInputMap = new HashMap<>();

    /**
     * cache of values (excluding model inputs) for node properties keyed by
     * node and the subsequent map is keyed by property to get the value
     */
    private final Map<AcceleratorNode, Map<String, Double>> propertyValueCache;

    /**
     * batch accessor for node properties
     */
    private BatchPropertyAccessor batchAccessor;

    /**
     * Constructor
     */
    public PrimaryPropertyAccessor() {
        propertyValueCache = new HashMap<>();
        batchAccessor = BatchPropertyAccessor.getInstance(Scenario.SYNC_MODE_DESIGN);
    }

    /**
     * request values for the nodes and the specified sync mode
     */
    public void requestValuesForNodes(final Collection<AcceleratorNode> nodes, final String syncMode) {
        final BatchPropertyAccessor batchAccessorInstance = BatchPropertyAccessor.getInstance(syncMode);
        batchAccessorInstance.requestValuesForNodes(nodes);
        batchAccessor = batchAccessorInstance;
    }

    /**
     * Returns a Map of property values for the supplied node.The map's keys are
     * the property names as defined by the node class' propertyNames method,
     * values are the Double value for that property on aNode.
     *
     * @param objNode the AcclereatorNode whose properties to return
     * @return a Map of node property values
     */
    public Map<String, Double> valueMapFor(final Object objNode) {
        if ((objNode == null)) {
            throw new IllegalArgumentException("null arguments not allowed by doubleValueFor");
        }
        if (!(objNode instanceof AcceleratorNode)) {
            throw new IllegalArgumentException("expected instance of AcceleratorNode");
        }

        AcceleratorNode aNode = (AcceleratorNode) objNode;

        PropertyAccessor nodeAccessor = getAccessorFor(aNode);
        if (nodeAccessor == null) {
            throw new IllegalArgumentException("unknown node type: " + aNode.getClass().getName());
        }

        final Map<String, Double> valueMap = batchAccessor.valueMapFor(aNode);

        // cache the values
        // need to copy it so we don't override the raw values
        propertyValueCache.put(aNode, new HashMap<>(valueMap));

        // apply whatif settings
        addInputOverrides(aNode, valueMap);

        if (DEBUG) {
            printValueMap(aNode, valueMap);
        }

        return valueMap;
    }

    /**
     * Use the cache rather than other sources for the value map and then apply
     * the model inputs
     */
    public Map<String, Double> getWhatifValueMapFromCache(final Object objNode) {
        if (objNode == null) {
            throw new IllegalArgumentException("null arguments not allowed by doubleValueFor");
        } else if (!(objNode instanceof AcceleratorNode)) {
            throw new IllegalArgumentException("expected instance of AcceleratorNode");
        }
        final AcceleratorNode aNode = (AcceleratorNode) objNode;
        // need to copy it so we don't override the raw values
        final Map<String, Double> valueMap = new HashMap<>(propertyValueCache.get(aNode));
        addInputOverrides(aNode, valueMap);
        return valueMap;
    }

    /**
     * get the accessor for the specified node
     */
    public PropertyAccessor getAccessorFor(final AcceleratorNode node) {
        return BatchPropertyAccessor.getAccessorFor(node);
    }

    /**
     * Returns true if there is an accessor for the specified node type, false
     * otherwise.
     *
     * @param aNode AcceleratorNode whose type to find an accessor for
     * @return true if there is an accessor for the supplied node, false
     * otherwise
     */
    public boolean hasAccessorFor(AcceleratorNode aNode) {
        return batchAccessor.hasAccessorFor(aNode);
    }

    // Model Input Data: Node Property Overrides ===============================
    private void addInputOverrides(final AcceleratorNode aNode, final Map<String, Double> valueMap) {
        Map<String, ModelInput> inputs = inputsForNode(aNode);
        if (inputs == null) {
            return;
        }
        for (final ModelInput input : inputs.values()) {
            final String property = input.getProperty();
            valueMap.put(property, input.getDoubleValue());
        }
    }

    /**
     * Sets the specified node's property to the specified value. Replaces the
     * existing value if there is one.
     *
     * @param aNode node whose property to set
     * @param property name of property to set
     * @param val double value for property
     */
    public ModelInput setModelInput(AcceleratorNode aNode, String property, double val) {
        ModelInput existingInput = getInput(aNode, property);
        if (existingInput != null) {
            existingInput.setDoubleValue(val);
            return existingInput;
        } else {
            ModelInput input = new ModelInput(aNode, property, val);
            addInput(input);
            return input;
        }
    }

    /**
     * Returns the ModelInput for the specified node's property, or null if
     * there is none.
     *
     * @param aNode node whose property to get a ModelInput for
     * @param propName name of property to get a ModelInput for
     */
    public ModelInput getInput(AcceleratorNode aNode, String propName) {
        final Map<String, ModelInput> inputs = inputsForNode(aNode);
        return inputs != null ? inputs.get(propName) : null;
    }

    protected void addInput(final ModelInput anInput) {
        final AcceleratorNode node = anInput.getAcceleratorNode();
        Map<String, ModelInput> inputs = inputsForNode(node);
        if (inputs == null) {
            inputs = new HashMap<>();
            nodeInputMap.put(node, inputs);
        }
        inputs.put(anInput.getProperty(), anInput);
    }

    public void removeInput(AcceleratorNode aNode, String property) {
        final Map<String, ModelInput> inputs = inputsForNode(aNode);
        if (inputs != null) {
            inputs.remove(property);
        }
    }

    private Map<String, ModelInput> inputsForNode(final AcceleratorNode aNode) {
        return nodeInputMap.get(aNode);
    }

    // Testing and Debugging ===================================================
    private static void printValueMap(final AcceleratorNode aNode, final Map<String, Double> values) {
        LOGGER.log(Level.INFO, "Properties for node: {0}", aNode);

        for (Entry<String, Double> entry : values.entrySet()) {
            LOGGER.log(Level.INFO, "\t{0}: {1}", new Object[]{entry.getKey(), entry.getValue()});
        }
    }
}

/**
 * Accessor for property values in batch
 */
abstract class BatchPropertyAccessor {

    /**
     * map of property accessors keyed by node
     */
    private static final Map<Class<?>, PropertyAccessor> NODE_ACCESSORS = new HashMap<>();

    // static initializer
    static {
        // Accessor Registration
        registerAccessorInstance(Electromagnet.class, new ElectromagnetPropertyAccessor());
        registerAccessorInstance(RfCavity.class, new RfCavityPropertyAccessor());
        registerAccessorInstance(PermanentMagnet.class, new PermanentMagnetPropertyAccessor());
    }

    /**
     * register the property accessor for each supported node class
     */
    private static void registerAccessorInstance(final Class<?> nodeClass, final PropertyAccessor accessor) {
        NODE_ACCESSORS.put(nodeClass, accessor);
    }

    /**
     * get the accessor for the specified node
     */
    protected static PropertyAccessor getAccessorFor(final AcceleratorNode node) {
        for (Entry<Class<?>, PropertyAccessor> entry : NODE_ACCESSORS.entrySet()) {
            if (entry.getKey().isInstance(node)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Returns true if there is an accessor for the specified node type, false
     * otherwise.
     *
     * @param aNode AcceleratorNode whose type to find an accessor for
     * @return true if there is an accessor for the supplied node, false
     * otherwise
     */
    public boolean hasAccessorFor(AcceleratorNode aNode) {
        return getAccessorFor(aNode) != null;
    }

    /**
     * make the request for values for the specified nodes
     */
    public abstract void requestValuesForNodes(final Collection<AcceleratorNode> nodes);

    /**
     * Get a Map of property values for the supplied node keys by property name.
     *
     * @param node the AcclereatorNode whose properties to return
     * @return a Map of node property values keyed by property name
     */
    public abstract Map<String, Double> valueMapFor(final AcceleratorNode node);

    /**
     * get the instance for the specified synchronization mode
     */
    static BatchPropertyAccessor getInstance(final String syncMode) {
        if (syncMode == null) {
            throw new IllegalArgumentException("Null Synchronization mode");
        } else if (syncMode.equals(Scenario.SYNC_MODE_LIVE)) {
            return new LiveBatchPropertyAccessor();
        } else if (syncMode.equals(Scenario.SYNC_MODE_DESIGN)) {
            return new DesignBatchPropertyAccessor();
        } else if (syncMode.equals(Scenario.SYNC_MODE_RF_DESIGN)) {
            return new LiveRFDesignBatchPropertyAccessor();
        } else {
            throw new IllegalArgumentException("Unknown Synchronization mode: " + syncMode);
        }
    }
}

/**
 * Accessor for property values in batch
 */
class DesignBatchPropertyAccessor extends BatchPropertyAccessor {

    /**
     * make the request for values for the specified nodes
     */
    @Override
    public void requestValuesForNodes(final Collection<AcceleratorNode> nodes) {
        // Do nothing
    }

    /**
     * Get a Map of property values for the supplied node keyd by property name.
     *
     * @param node the AcclereatorNode whose properties to return
     * @return a Map of node property values
     */
    @Override
    public Map<String, Double> valueMapFor(final AcceleratorNode node) {
        final PropertyAccessor accessor = getAccessorFor(node);
        return accessor != null ? accessor.getDesignValueMap(node) : new HashMap<>();
    }
}

/**
 * batch property accessor which is based on channels
 */
abstract class BatchChannelPropertyAccessor extends BatchPropertyAccessor {

    /**
     * channel value keyed by channel
     */
    protected Map<Channel, Double> channelValues;

    /**
     * make the request for values for the specified nodes
     */
    @Override
    public void requestValuesForNodes(final Collection<AcceleratorNode> nodes) {
        // assign an empty map at the start should something go wrong later
        channelValues = Collections.<Channel, Double>emptyMap();

        // collect all the channels from every node's properties
        final Set<Channel> channels = new HashSet<>();
        for (final AcceleratorNode node : nodes) {
            final PropertyAccessor accessor = getAccessorFor(node);
            channels.addAll(getChannels(accessor, node));
        }

        // create and submit a batch channel Get request
        final BatchGetValueRequest request = new BatchGetValueRequest(channels);
        // wait up to 5 seconds for a response
        request.submitAndWait(5.0);

        // print an overview of the request status
        if (!request.isComplete()) {
            final int requestCount = channels.size();
            final int recordCount = request.getRecordCount();
            final int exceptionCount = request.getExceptionCount();
            Logger.getLogger(BatchChannelPropertyAccessor.class.getName()).log(Level.WARNING, "Batch channel request for online model is incomplete. {0} of {1} channels succeeded. {2} channels had exceptions.", new Object[]{recordCount, requestCount, exceptionCount});
        }

        // gather values for the channels in a map keyed by channel
        final Map<Channel, Double> channelValuesMap = new HashMap<>();
        final List<String> unreadChannels = new ArrayList<>();
        for (final Channel channel : channels) {
            final ChannelRecord channelRecord = request.getRecord(channel);
            if (channelRecord != null) {
                channelValuesMap.put(channel, channelRecord.doubleValue());
            } else {
                unreadChannels.add(channel.getId());
            }
        }
        if (!unreadChannels.isEmpty()) {
            Logger.getLogger(BatchChannelPropertyAccessor.class.getName()).log(Level.WARNING, "No record for channels: {0}", unreadChannels);
        }

        this.channelValues = channelValuesMap;
    }

    /**
     * Get a Map of property values for the supplied node keyd by property name.
     *
     * @param node the AcclereatorNode whose properties to return
     * @return a Map of node property values
     */
    @Override
    public Map<String, Double> valueMapFor(final AcceleratorNode node) {
        final PropertyAccessor accessor = getAccessorFor(node);
        return getValueMap(accessor, node);
    }

    /**
     * get the channels for the specified node
     */
    protected abstract Collection<Channel> getChannels(final PropertyAccessor accessor, final AcceleratorNode node);

    /**
     * get the value map for the specified node
     */
    protected abstract Map<String, Double> getValueMap(final PropertyAccessor accessor, final AcceleratorNode node);
}

/**
 * Accessor for property values in batch
 */
class LiveBatchPropertyAccessor extends BatchChannelPropertyAccessor {

    /**
     * get the channels for the specified node
     */
    @Override
    protected Collection<Channel> getChannels(final PropertyAccessor accessor, final AcceleratorNode node) {
        return accessor.getLiveChannels(node);
    }

    /**
     * get the value map for the specified node
     */
    @Override
    protected Map<String, Double> getValueMap(final PropertyAccessor accessor, final AcceleratorNode node) {
        return accessor.getLiveValueMap(node, channelValues);
    }
}

/**
 * Accessor for property values in batch
 */
class LiveRFDesignBatchPropertyAccessor extends BatchChannelPropertyAccessor {

    /**
     * get the channels for the specified node
     */
    @Override
    protected Collection<Channel> getChannels(final PropertyAccessor accessor, final AcceleratorNode node) {
        return accessor.getLiveRFDesignChannels(node);
    }

    /**
     * get the value map for the specified node
     */
    @Override
    protected Map<String, Double> getValueMap(final PropertyAccessor accessor, final AcceleratorNode node) {
        return accessor.getLiveRFDesignValueMap(node, channelValues);
    }
}
