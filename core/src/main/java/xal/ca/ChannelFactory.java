/*
 * ChannelFactory.java
 *
 * Created on August 26, 2002, 1:24 PM
 */
package xal.ca;

import java.lang.reflect.InvocationTargetException;
import xal.tools.transforms.ValueTransform;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ChannelFactory is a factory for generating channels.
 *
 * @author tap
 */
public abstract class ChannelFactory {

    /**
     * default channel factory instance
     */
    private static ChannelFactory defaultFactory;

    private static final List<ChannelFactory> FACTORY_LIST = new ArrayList<>();

    private boolean test = false;

    protected String testSuffix = ":TEST";

    /**
     * map of channels keyed by signal name
     */
    private final Map<String, Channel> channelMap;

    static {
        defaultFactory = newFactory();
    }

    /**
     * Creates a new instance of ChannelFactory
     */
    protected ChannelFactory() {
        channelMap = new HashMap<>();
    }

    /**
     * Initialize the channel system
     *
     * @return true if the initialization was successful and false if not
     */
    public abstract boolean init();

    protected abstract void dispose();

    public void destroy() {
        dispose();
        synchronized (FACTORY_LIST) {
            if (this == defaultFactory) {
                ChannelFactory.defaultFactory = null;
            }
            FACTORY_LIST.remove(this);
        }
    }

    /**
     * Dispose all channel systems
     */
    public static void disposeAll() {
        synchronized (FACTORY_LIST) {
            for (ChannelFactory channelFactory : FACTORY_LIST) {
                channelFactory.dispose();
            }
            FACTORY_LIST.clear();
        }
        defaultFactory = null;
    }

    /**
     * Get a channel associated with the signal name. If the channel is already
     * in our map, then return it, otherwise create a new one and add it to our
     * channel map.
     *
     * @param signalName The PV signal name of the channel
     * @return The channel corresponding to the signal name
     */
    public Channel getChannel(final String signalName) {
        if ("".equals(signalName)) {
            return null;
        }
        Channel channel;
        synchronized (channelMap) {
            if (!channelMap.containsKey(signalName)) {
                channel = newChannel(signalName);
                channelMap.put(signalName, channel);
            } else {
                channel = channelMap.get(signalName);
            }
        }

        return channel;
    }

    /**
     * Get a channel associated with the signal name and transform. If the
     * channel is already in our map, then return it, otherwise create a new one
     * and add it to our channel map.
     *
     * @param signalName The PV signal name of the channel
     * @param transform The channel's value transform
     * @return The channel corresponding to the signal name
     */
    public Channel getChannel(final String signalName, final ValueTransform transform) {
        if ("".equals(signalName)) {
            return null;
        }
        final String channelID = Channel.generateId(signalName, transform);
        synchronized (channelMap) {
            if (!channelMap.containsKey(channelID)) {
                final Channel channel = newChannel(signalName, transform);
                channelMap.put(channelID, channel);
                return channel;
            } else {
                return channelMap.get(channelID);
            }
        }
    }

    /**
     * Create a concrete channel which makes an appropriate low level channel
     *
     * @param signalName PV for which to create a new channel
     * @return a new channel for the specified signal name
     */
    protected abstract Channel newChannel(final String signalName);

    /**
     * Create a new channel for the given signal name and set its value
     * transform.
     *
     * @param signalName The PV signal name
     * @param transform The value transform to use in the channel
     * @return The new channel
     */
    protected Channel newChannel(String signalName, ValueTransform transform) {
        Channel channel = newChannel(signalName);
        channel.setValueTransform(transform);
        return channel;
    }

    /**
     * Get the default factory which determines the low level channel
     * implementation
     *
     * @return The default channel factory
     */
    public static ChannelFactory defaultFactory() {
        synchronized (FACTORY_LIST) {
            if (defaultFactory == null) {
                defaultFactory = newFactory();
            }
        }
        return defaultFactory;
    }

    /**
     * Get the associated channel system from the channel factory
     * implementation.
     *
     * @return The channel system
     */
    protected abstract ChannelSystem channelSystem();

    /**
     * get the default system which handles static behavior of Channels
     *
     * @return the channel system associated with the default channel factory
     */
    static ChannelSystem defaultSystem() {
        synchronized (FACTORY_LIST) {
            if (defaultFactory == null) {
                defaultFactory();
            }
        }
        return defaultFactory.channelSystem();
    }

    /**
     * Instantiate a new ChannelFactory
     *
     * @return a new channel factory
     */
    protected static ChannelFactory newFactory() {
        try {
            // effectively returns ChannelFactoryPlugin.getChannelFactoryInstance()
            final Class<?> pluginClass = Class.forName("xal.ca.ChannelFactoryPlugin");
            final Method creatorMethod = pluginClass.getMethod("getChannelFactoryInstance");
            ChannelFactory channelFactory = (ChannelFactory) creatorMethod.invoke(null);
            synchronized (FACTORY_LIST) {
                FACTORY_LIST.add(channelFactory);
            }
            return channelFactory;
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException exception) {
            throw new RuntimeException("Failed to load the ChannelFactoryPlugin: " + exception.getMessage());
        }
    }

    /**
     * Instantiate a new server ChannelFactory
     *
     * @return a new server channel factory
     */
    public static ChannelFactory newServerFactory() {
        try {
            // effectively returns ChannelFactoryPlugin.getServerChannelFactoryInstance()
            final Class<?> pluginClass = Class.forName("xal.ca.ChannelFactoryPlugin");
            final Method creatorMethod = pluginClass.getMethod("getServerChannelFactoryInstance");
            ChannelFactory channelFactory = (ChannelFactory) creatorMethod.invoke(null);
            synchronized (FACTORY_LIST) {
                FACTORY_LIST.add(channelFactory);
            }
            return channelFactory;
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException exception) {
            throw new RuntimeException("Failed to load the ChannelFactoryPlugin: " + exception.getMessage());
        }
    }

    /**
     * Print information about this factory
     */
    public abstract void printInfo();

    /**
     * Sets the test flag. If the test flag is on, the factory will add a suffix
     * to the signal names to distinguish from the original signals.
     *
     * This is a useful feature to test applications, which can run a server
     * with the test flag and the client will connect to the test channels.
     *
     * @param test
     */
    public void setTest(boolean test) {
        this.test = test;
        if (test) {
            for (Channel channel : channelMap.values()) {
                channel.setChannelName(channel.channelName() + testSuffix);
                channel.disconnect();
                channel.requestConnection();
            }
        } else {
            for (Entry<String, Channel> entry : channelMap.entrySet()) {
                String channelName = entry.getKey();
                Channel channel = entry.getValue();
                channel.setChannelName(channelName);
                channel.disconnect();
                channel.requestConnection();
            }
        }
    }

    public boolean isTest() {
        return test;
    }

    /**
     * This method allows to define a suffix for all PVs when the test mode is
     * enabled. By default, the suffix is ":TEST".
     *
     * @param suffix
     */
    public void setTestSuffix(String suffix) {
        this.testSuffix = suffix;
    }

    public String getTestSuffix() {
        return this.testSuffix;
    }
}
