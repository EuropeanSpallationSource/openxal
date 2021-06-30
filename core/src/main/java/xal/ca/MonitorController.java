//
//  MonitorController.java
//  xal
//
//  Created by Thomas Pelaia on 4/25/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//
package xal.ca;

import xal.tools.messaging.MessageCenter;

import java.util.logging.*;

/**
 * Creates a monitor for a channel when the channel is connected and dispatches
 * monitor and channel connection events to registered listeners.
 *
 * @author t6p
 */
public class MonitorController {

    /**
     * the monitor mask to use when initializing the monitor (Monitor.VALUE,
     * Monitor.LOG, Monitor.ALARM)
     */
    protected final int monitorMask;

    /**
     * synchronization lock
     */
    protected final Object eventLock;

    /**
     * event message center
     */
    protected MessageCenter messageCenter;

    /**
     * proxy for posting channel events
     */
    protected MonitorEventListener eventProxy;

    /**
     * The channel to wrap
     */
    protected Channel channel;

    /**
     * The monitor for the channel
     */
    protected Monitor monitor;

    /**
     * last record captured
     */
    protected ChannelTimeRecord lastRecord;

    /**
     * connection listener
     */
    protected ConnectionListener connectionListener;

    private static final Logger LOGGER = Logger.getLogger(MonitorController.class.getName());

    /**
     * Primary constructor.
     *
     * @param channel The channel to wrap.
     * @param monitorMask The monitor mask to apply when instantiating the
     * monitor.
     */
    public MonitorController(final Channel channel, final int monitorMask) {
        this.monitorMask = monitorMask;
        eventLock = new Object();
        this.channel = channel;
        messageCenter = new MessageCenter();
        eventProxy = messageCenter.registerSource(this, MonitorEventListener.class);
        lastRecord = null;
    }

    /**
     * Constructor using the default monitor mask (Monitor.VALUE).
     *
     * @param channel The channel to wrap.
     */
    public MonitorController(final Channel channel) {
        this(channel, Monitor.VALUE);
    }

    /**
     * Constructor creating a channel from the specified PV and applying the
     * specified monitor mask.
     *
     * @param pv The PV for which to create a channel.
     * @param monitorMask The monitor mask to apply when instantiating the
     * monitor.
     */
    public MonitorController(final String pv, final int monitorMask) {
        this(ChannelFactory.defaultFactory().getChannel(pv), monitorMask);
    }

    /**
     * Constructor creating a channel from the specified PV and using the
     * default monitor mask (Monitor.VALUE).
     *
     * @param pv The PV for which to create a channel.
     */
    public MonitorController(final String pv) {
        this(pv, Monitor.VALUE);
    }

    /**
     * Register the listener as a receiver of channel events from this
     * controller.
     *
     * @param listener The listener to receive channel events
     */
    public void addMonitorEventListener(final MonitorEventListener listener) {
        synchronized (eventLock) {
            messageCenter.registerTarget(listener, this, MonitorEventListener.class);

            // immediately notify the new listener of the current status
            if (channel != null) {
                listener.connectionChanged(channel, isConnected());
                if (lastRecord != null) {
                    listener.valueChanged(channel, lastRecord);
                }
            }
        }
    }

    /**
     * Remove the listener as a receiver of channel events from this controller.
     *
     * @param listener The listener to remove from receiving channel events
     */
    public void removeMonitorEventListener(MonitorEventListener listener) {
        messageCenter.removeTarget(listener, this, MonitorEventListener.class);
    }

    /**
     * Get the PV for the controlled channel.
     *
     * @return the PV
     */
    public String getPV() {
        return channel.channelName();
    }

    /**
     * Get this instance's controlled channel.
     *
     * @return the wrapped channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Determine if the channel is connected.
     *
     * @return true if the channel is connected and false if not.
     */
    public boolean isConnected() {
        return channel.isConnected();
    }

    /**
     * Get the latest channel record.
     *
     * @return the latest channel record or null if none has been published or
     * the channel is not connected.
     */
    public ChannelTimeRecord getLatestRecord() {
        synchronized (eventLock) {
            return lastRecord;
        }
    }

    /**
     * Request that the channel be connected. When the channel connection
     * occurs, create a monitor.
     */
    public void requestMonitor() {
        if (connectionListener == null) {
            connectionListener = new ConnectionListener() {
                /**
                 * Indicates that a connection to the specified channel has been
                 * established.
                 *
                 * @param channel The channel which has been connected.
                 */
                @Override
                public void connectionMade(Channel channel) {
                    synchronized (eventLock) {
                        // clear the last record
                        lastRecord = null;

                        if (monitor == null) {
                            // create a new monitor if one doesn't already exist
                            makeMonitor();
                        }

                        // notify listeners about the new connection
                        eventProxy.connectionChanged(channel, true);
                    }
                }

                /**
                 * Indicates that a connection to the specified channel has been
                 * dropped.
                 *
                 * @param channel The channel which has been disconnected.
                 */
                @Override
                public void connectionDropped(Channel channel) {
                    synchronized (eventLock) {
                        // clear the last record
                        lastRecord = null;
                        // notify listeners about the dropped connection
                        eventProxy.connectionChanged(channel, false);
                    }
                }
            };

            // listen for connection events
            channel.addConnectionListener(connectionListener);
        }

        // request a new connection if the channel is not already connected
        if (!channel.isConnected() && channel.isValid()) {
            channel.requestConnection();
        }
    }

    /**
     * Create a monitor to listen for new channel records. An instance of an
     * internal, anonymous class is the listener of the monitor events and
     * caches the latest channel record.
     */
    protected void makeMonitor() {
        try {
            monitor = channel.addMonitorValTime(new IEventSinkValTime() {
                /**
                 * Handle the monitor event by caching the latest channel
                 * record.
                 *
                 * @param record the monitor's posted data for the channel
                 * @param channel the channel whose monitor has fired
                 */
                @Override
                public void eventValue(final ChannelTimeRecord record, final Channel channel) {
                    synchronized (eventLock) {
                        // update the latest record
                        lastRecord = record;
                        if (eventProxy != null) {
                            //  notify listeners about the new data
                            eventProxy.valueChanged(channel, record);
                        }
                    }
                }
            }, monitorMask);
        } catch (ConnectionException exception) {
            LOGGER.log(Level.SEVERE, "Connection exception.", exception);
        } catch (MonitorException exception) {
            LOGGER.log(Level.SEVERE, "Monitor exception.", exception);
        }
    }

    /**
     * Dispose of the channel wrapper resources by clearing the monitor (if any)
     * and disposing of of the messaging resources.
     */
    public void dispose() {
        synchronized (eventLock) {
            if (connectionListener != null) {
                channel.removeConnectionListener(connectionListener);
                connectionListener = null;
            }

            if (monitor != null) {
                monitor.clear();
            }

            eventProxy = null;
            messageCenter = null;
            monitor = null;
            channel = null;
        }
    }
}
