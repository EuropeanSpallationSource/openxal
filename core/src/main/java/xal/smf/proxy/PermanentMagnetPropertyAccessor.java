/*
 * Created on Oct 14, 2014
 */
package xal.smf.proxy;

import java.util.*;

import xal.smf.AcceleratorNode;
import xal.ca.Channel;

/**
 * Access property values for a permanent magnet
 *
 * @author Tom Pelaia
 */
public class PermanentMagnetPropertyAccessor extends AbstractPropertyAccessor {

    public static final String PROPERTY_FIELD = "field";

    /**
     * get the map of design values keyed by property name
     */
    @Override
    public Map<String, Double> getDesignValueMap(final AcceleratorNode node) {
        return getDesignValueMap(node, node.getProperties());
    }

    /**
     * get the map of live values keyed by property name
     */
    @Override
    public Map<String, Double> getLiveValueMap(final AcceleratorNode node, final Map<Channel, Double> channelValues) {
        return getLiveValueMap(node, channelValues, node.getProperties());
    }

    /**
     * get the channels for live property access
     */
    @Override
    public Collection<Channel> getLiveChannels(final AcceleratorNode node) {
        return getLiveChannels(node, node.getProperties());
    }

    /**
     * get the map of live RF design values keyed by property name
     */
    @Override
    public Map<String, Double> getLiveRFDesignValueMap(final AcceleratorNode node, final Map<Channel, Double> channelValues) {
        return getLiveValueMap(node, channelValues);
    }

    /**
     * get the channels for live property access with design RF
     */
    @Override
    public Collection<Channel> getLiveRFDesignChannels(final AcceleratorNode node) {
        return getLiveChannels(node);
    }

}
