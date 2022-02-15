//
//  MonitorHandler.java
//  xal
//
//  Created by Thomas Pelaia on 2/22/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//
package xal.ca;

import xal.tools.messaging.MessageCenter;

import java.util.logging.*;

/**
 * Connect to, monitor and cache a channel's monitor events.
 */
public class MonitorCache {

    /**
     * Message center for dispatching monitor events to registered listeners.
     */
    protected final MessageCenter messageCenter;

    /**
     * Proxy which forwards monitor events to registered listeners.
     */
    protected final IEventSinkValTime eventProxy;

    /**
     * the channel to wrap
     */
    protected final Channel channel;

    /**
     * listener to handle connection events
     */
    protected final ConnectionListener connectionHandler;

    /**
     * listener to handle monitor events
     */
    protected final IEventSinkValTime monitorEventHandler;

    /**
     * a channel monitor
     */
    protected Monitor monitor;

    /**
     * latest monitor event
     */
    protected ChannelTimeRecord latestRecord;

    /**
     * Constructor
     *
     * @param channel to monitor
     */
    public MonitorCache(final Channel channel) {
        this.channel = channel;
        monitor = null;
        latestRecord = null;

        messageCenter = new MessageCenter("Monitor Event Cache");
        eventProxy = messageCenter.registerSource(this, IEventSinkValTime.class);

        monitorEventHandler = new MonitorEventHandler();
        connectionHandler = new ConnectionEventHandler();
        channel.addConnectionListener(connectionHandler);
    }

    /**
     * Dispose of this wrapper's resources.
     */
    public void dispose() {
        if (channel != null) {
            channel.removeConnectionListener(connectionHandler);
        }

        messageCenter.removeSource(this, IEventSinkValTime.class);

        if (monitor != null) {
            monitor.clear();
            monitor = null;
        }
    }

    /**
     * Register the listener to receive IEventSinkValTime events from the
     * monitor
     *
     * @param listener to receive events
     */
    public void addMonitorListener(final IEventSinkValTime listener) {
        messageCenter.registerTarget(listener, this, IEventSinkValTime.class);
    }

    /**
     * Remove the listener from receiving IEventSinkValTime events from this
     * monitor
     *
     * @param listener to remove from receiving events
     */
    public void removeMonitorListener(final IEventSinkValTime listener) {
        messageCenter.removeTarget(listener, this, IEventSinkValTime.class);
    }

    /**
     * Request a connection and start the monitor upon connection.
     */
    public void requestMonitor() {
        channel.requestConnection();
    }

    /**
     * Get the channel.
     *
     * @return wrapped channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Determine if the channel is connected.
     *
     * @return true if the channel is connected and false if not
     */
    public boolean isConnected() {
        return channel.isConnected();
    }

    /**
     * Get the latest record.
     *
     * @return latest record
     */
    public ChannelTimeRecord getLatestRecord() {
        return latestRecord;
    }

    /**
     * Handle monitor events
     */
    protected class MonitorEventHandler implements IEventSinkValTime {

        /**
         * Handle the monitor event.
         */
        @Override
        public void eventValue(final ChannelTimeRecord channelRecord, final Channel channel) {
            latestRecord = channelRecord;
            eventProxy.eventValue(channelRecord, channel);
        }
    }

    /**
     * Handle connection events
     */
    protected class ConnectionEventHandler implements ConnectionListener {

        /**
         * Indicates that a connection to the specified channel has been
         * established.
         *
         * @param channel The channel which has been connected.
         */
        @Override
        public void connectionMade(final Channel channel) {
            if (monitor == null) {
                try {
                    monitor = channel.addMonitorValTime(monitorEventHandler, Monitor.VALUE);
                } catch (MonitorException exception) {
                    Logger.getLogger("global").log(Level.SEVERE, "Exception attempting to make a monitor.", exception);
                }
            }
        }

        /**
         * Indicates that a connection to the specified channel has been
         * dropped.
         *
         * @param channel The channel which has been disconnected.
         */
        @Override
        public void connectionDropped(final Channel channel) {
            latestRecord = null;
        }
    }
}
