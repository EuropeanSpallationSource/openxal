/*
 * ChannelAgent.java
 *
 * Created on June 27, 2002, 8:46 AM
 */
package xal.ca.correlator;

import xal.tools.correlator.*;
import xal.tools.messaging.MessageCenter;
import xal.ca.*;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * ChannelAgent manages a single channel. It performs any setup, monitors the
 * channel and it manages a circular buffer of bin agents that gather correlated
 * events.
 *
 * @author tap
 */
public class ChannelAgent extends SourceAgent<ChannelTimeRecord> {

    private Channel channel;
    private Monitor monitor;
    private volatile boolean enabled;
    private volatile boolean activeFlag;
    private EventHandler eventHandler;
    private ConnectionListener connectionHandler;
    private static final Logger LOGGER = Logger.getLogger(ChannelAgent.class.getName());

    /**
     * Creates new ChannelAgent
     *
     * @param localCenter local shared message center
     * @param newChannel channel to monitor
     * @param newName name
     * @param recordFilter filter for records
     * @param tester correlation tester
     */
    public ChannelAgent(final MessageCenter localCenter, final Channel newChannel, final String newName, final RecordFilter<ChannelTimeRecord> recordFilter, final CorrelationTester<ChannelTimeRecord> tester) {
        super(localCenter, newName, recordFilter, tester);
        channel = newChannel;
        monitor = null;
    }

    /**
     * Setup the event handler to use the specified record filter to filter
     * monitor events for this channel.
     *
     * @param recordFilter The filter to use for this channel.
     */
    @Override
    protected void setupEventHandler(final RecordFilter<ChannelTimeRecord> recordFilter) {
        activeFlag = false;

        if (recordFilter == null) {
            eventHandler = new EventHandler();
        } else {
            eventHandler = new FilteredEventHandler(recordFilter);
        }
    }

    /**
     * Determine if the channel is enabled for correlations.
     *
     * @return true if the channel is enabled for correlations.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Determine if the channel is actively being monitored.
     *
     * @return true if the channel is being monitored and false otherwise.
     */
    public boolean isActive() {
        return activeFlag;
    }

    /**
     * Start monitoring the channel.
     *
     * @return true if the channel is successfully being monitored and false
     * otherwise.
     */
    @Override
    public boolean startMonitor() {
        enabled = true;
        if (connectionHandler == null) {
            connectionHandler = new ConnectionHandler();
            channel.addConnectionListener(connectionHandler);
        }

        // try to connect the channel
        activeFlag = false;
        if (!channel.isConnected()) {
            channel.requestConnection();
            Channel.flushIO();
        } else {
            makeMonitor();
        }

        return activeFlag;
    }

    /**
     * Stop monitoring the channel
     */
    @Override
    public void stopMonitor() {
        enabled = false;
        if (connectionHandler != null) {
            channel.removeConnectionListener(connectionHandler);
            connectionHandler = null;
        }
        if (monitor != null) {
            monitor.clear();
            monitor = null;
        }
        activeFlag = false;
    }

    /**
     * Create a monitor to listen for new channel records.
     */
    protected synchronized void makeMonitor() {
        try {
            if (enabled && channel.isConnected()) {
                if (monitor == null) {
                    monitor = channel.addMonitorValTime(eventHandler, Monitor.VALUE);
                }
                activeFlag = true;
            }
        } catch (MonitorException exception) {
            LOGGER.log(Level.WARNING, "Monitoring exception caught, turning off active flag", exception);
            activeFlag = false;
        }
    }

    /**
     * Handle connection changes for the channel
     */
    private class ConnectionHandler implements ConnectionListener {

        /**
         * Make a monitor when the channel is connected.
         *
         * @param channel The channel which has been connected.
         */
        @Override
        public void connectionMade(final Channel channel) {
            makeMonitor();
        }

        /**
         * Indicates that a connection to the specified channel has been
         * dropped.
         *
         * @param channel The channel which has been disconnected.
         */
        @Override
        public void connectionDropped(final Channel channel) {
            activeFlag = false;
        }
    }

    /**
     * Handle the monitor events
     */
    protected class EventHandler implements IEventSinkValTime {

        /**
         * Implement IEventSinkValTime interface
         *
         * Handle the monitor events for this channel. When the monitor fires,
         * recycle the oldest bin. Clear all memory of events and assign the
         * timestamp of the record to be the timestamp for the bin. Broadcast
         * the event within the correlation world so that bins of all channel
         * agents (not just this one) are notified of the event.
         */
        @Override
        public synchronized void eventValue(final ChannelTimeRecord channelRecord, final Channel channel) {
            if (!activeFlag) {
                return;
            }

            double timestamp = channelRecord.getTimestamp().getSeconds();
            postEvent(channelRecord, timestamp);
        }
    }

    /**
     * Handle the Monitor events and filter the events according to the supplied
     * <code>ChannelRecordFilter</code>.
     */
    protected class FilteredEventHandler extends EventHandler {

        RecordFilter<ChannelTimeRecord> filter;

        public FilteredEventHandler(final RecordFilter<ChannelTimeRecord> newFilter) {
            filter = newFilter;
        }

        @Override
        public synchronized void eventValue(final ChannelTimeRecord channelRecord, final Channel channel) {
            /**
             * Handle only those events accepted by the filter
             */
            if (filter.accept(channelRecord)) {
                super.eventValue(channelRecord, channel);
            }
        }
    }
}
