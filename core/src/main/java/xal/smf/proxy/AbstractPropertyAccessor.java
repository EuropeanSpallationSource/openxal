/*
 * Created on Oct 24, 2003
 */
package xal.smf.proxy;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.smf.AcceleratorNode;
import xal.ca.Channel;

/**
 * Access property values for a node
 *
 * @author Tom Pelaia
 */
public abstract class AbstractPropertyAccessor implements PropertyAccessor {

    private static final Logger LOGGER = Logger.getLogger(AbstractPropertyAccessor.class.getName());

    private static final double SCALE_DFLT = 1.0;

    /**
     * Get the scale factor for the specified property
     */
    protected double getPropertyScale(final String propertyName) {
        return SCALE_DFLT;
    }

    /**
     * get the map of design values keyed by property name
     */
    protected Map<String, Double> getDesignValueMap(final AcceleratorNode node, final List<String> propertyNames) {
        final Map<String, Double> valueMap = new HashMap<>();
        for (final String propertyName : propertyNames) {
            final double scale = getPropertyScale(propertyName);
            final double value = scale * node.getDesignPropertyValue(propertyName);
            valueMap.put(propertyName, value);
        }

        return valueMap;
    }

    /**
     * get the map of live values keyed by property name
     */
    protected Map<String, Double> getLiveValueMap(final AcceleratorNode node, final Map<Channel, Double> channelValues, final List<String> propertyNames) {
        // property values keyed by property name
        final Map<String, Double> valueMap = new HashMap<>();

        // loop over each property by name
        for (final String propertyName : propertyNames) {
            // get the array of channels that are required to computer the property's value
            final Channel[] propertyChannels = node.getLivePropertyChannels(propertyName);

            // array to populate with channel values for the property
            final double[] propertyChannelValues = new double[propertyChannels.length];

            // loop over each channel for the current property and populate the propertyChannelValues with the corresponding value
            for (int index = 0; index < propertyChannels.length; index++) {
                final Channel channel = propertyChannels[index];
                final Double value = channelValues.get(channel);
                if (value != null) {
                    propertyChannelValues[index] = value;
                } else {
                    // Missing property values will likely cause a SynchronizationException later if and when the property is needed, so no need to throw any exceptions here.
                    // Just print to standard error for extra diagnostics.
                    LOGGER.log(Level.WARNING, "Missing channel value for property: {0}, node: {1}, channel: {2}", new Object[]{propertyName, node.getId(), channel.channelName()});
                    // we need all of a property's channel values to compute the property value, so abandon the current property if we are missing any
                    // abandon this property and continue with the next property if any
                    break;
                }
            }

            // we only get here if all the channel values for the current property are available
            // compute the property value from the the property's channel values and populate the value map
            final double scale = getPropertyScale(propertyName);
            final double propertyValue = scale * node.getLivePropertyValue(propertyName, propertyChannelValues);
            valueMap.put(propertyName, propertyValue);
        }

        return valueMap;
    }

    /**
     * get the channels for live property access
     */
    protected Collection<Channel> getLiveChannels(final AcceleratorNode node, final List<String> propertyNames) {
        final Set<Channel> channels = new HashSet<>();

        for (final String propertyName : propertyNames) {
            final Channel[] propertyChannels = node.getLivePropertyChannels(propertyName);
            for (final Channel channel : propertyChannels) {
                channels.add(channel);
            }
        }

        return channels;
    }
}
