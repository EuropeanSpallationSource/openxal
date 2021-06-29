package xal.extension.scan;

import xal.ca.*;
import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.awt.event.*;

/**
 * Manage the monitor events for a Process Variable
 *
 * @author shishlo
 * @author tap created October 31, 2005
 */
public class MonitoredPV {

    /**
     * default for the current value (0.0 rather than NaN for backward
     * compatibility)
     */
    private static final double DEFAULT_CURRENT_VALUE = 0.0;

    /**
     * hash of monitored PVs keyed by alias
     */
    private static final Map<String, MonitoredPV> ALIAS_PV_MAP;

    /**
     * local message center for state events
     */
    private final MessageCenter stateMessageCenter;

    /**
     * dispatches state events to registered listeners
     */
    final ActionListener stateEventDispatch;

    /**
     * local message center for value events
     */
    private final MessageCenter valueMessageCenter;

    /**
     * dispatches value events to registered listeners
     */
    final ActionListener valueEventDispatch;

    /**
     * handler of the delegate callbacks
     */
    private final MonitorDelegateHandler delegateHandler;

    private String alias = null;

    /**
     * monitor of channel to monitor
     */
    protected ScanChannelMonitor monitor;

    /**
     * market used to indicate whether the value has changed since the last
     * reset
     */
    volatile private boolean valueChanged;

    /**
     * the current value (either monitored or set)
     */
    volatile double currentValue;

    /**
     * indicates whether the latest event was successful
     */
    volatile boolean latestEventSuccessful;

    // static initializer
    static {
        ALIAS_PV_MAP = new HashMap<>();
    }

    /**
     * Constructor for the MonitoredPV
     */
    MonitoredPV() {
        stateMessageCenter = new MessageCenter("MonitoredPV State");
        stateEventDispatch = stateMessageCenter.registerSource(this, ActionListener.class);

        valueMessageCenter = new MessageCenter("MonitoredPV Value");
        valueEventDispatch = valueMessageCenter.registerSource(this, ActionListener.class);

        delegateHandler = new MonitorDelegateHandler();

        latestEventSuccessful = false;
        valueChanged = false;
        currentValue = DEFAULT_CURRENT_VALUE;
    }

    /**
     * Returns the monitoredPV attribute of the MonitoredPV class
     *
     * @param alias The Parameter
     * @return The monitoredPV value
     */
    public static MonitoredPV getMonitoredPV(final String alias) {
        if (alias == null) {
            return null;
        }
        if (ALIAS_PV_MAP.containsKey(alias)) {
            return ALIAS_PV_MAP.get(alias);
        } else {
            MonitoredPV mpv = new MonitoredPV();
            mpv.setAlias(alias);
            ALIAS_PV_MAP.put(alias, mpv);
            return mpv;
        }
    }

    /**
     * Sets the alias attribute of the MonitoredPV object
     *
     * @param alias The new alias value
     */
    private void setAlias(final String alias) {
        this.alias = alias;
    }

    /**
     * Determine if a monitored PV is associated with the specified alias
     *
     * @param alias The alias to lookup
     * @return true if a monitored PV has been assigned to the alias
     */
    static boolean hasAlias(final String alias) {
        return ALIAS_PV_MAP.containsKey(alias);
    }

    /**
     * Determine whether the value was changed since the last reset (i.e.
     * setValueChanged(false)).
     *
     * @return true if the value was changed since the last time it was reset;
     * false otherwise
     */
    public boolean valueChanged() {
        return valueChanged;
    }

    /**
     * Sets the boolean marker to the specified change state
     *
     * @param valueChanged The new value change marker state
     */
    public void setValueChanged(final boolean valueChanged) {
        this.valueChanged = valueChanged;
    }

    /**
     * Remove the monitored PV associated with the alias
     *
     * @param alias The Parameter
     */
    public static void removeMonitoredPV(final String alias) {
        if (alias == null) {
            return;
        }
        if (ALIAS_PV_MAP.containsKey(alias)) {
            MonitoredPV mpv = ALIAS_PV_MAP.get(alias);
            mpv.stopMonitor();
            ALIAS_PV_MAP.remove(alias);
        }
    }

    /**
     * Dispose of the monitored PV and remove its alias
     *
     * @param mpv the monitored PV to remove
     */
    public static void removeMonitoredPV(MonitoredPV mpv) {
        if (mpv == null) {
            return;
        }
        mpv.stopMonitor();
        removeMonitoredPV(mpv.getAlias());
    }

    /**
     * Returns the aliases attribute of the MonitoredPV class
     *
     * @return The aliases value
     */
    public static Object[] getAliases() {
        return ALIAS_PV_MAP.keySet().toArray();
    }

    /**
     * Returns the alias attribute of the MonitoredPV object
     *
     * @return The alias value
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Get the name of the channel to be monitored
     *
     * @return The channelName value
     */
    public String getChannelName() {
        final Channel channel = getChannel();
        return channel != null ? channel.channelName() : null;
    }

    /**
     * Get the channel to be monitored
     *
     * @return The channel value
     */
    public Channel getChannel() {
        return monitor != null ? monitor.getChannel() : null;
    }

    /**
     * Sets the channel to monitor
     *
     * @param channel the new channel to monitor
     * @param delegate to handle monitor callbacks
     * @param requestEvents request channel events
     */
    private void setChannel(final Channel channel, final boolean requestEvents) {
        final ScanChannelMonitor oldMonitor = monitor;
        if (oldMonitor != null) {
            oldMonitor.dispose();
        }

        currentValue = DEFAULT_CURRENT_VALUE;
        if (channel != null) {
            monitor = new ScanChannelMonitor(channel, delegateHandler, requestEvents);
        }
    }

    /**
     * Sets the channel to monitor
     *
     * @param channel the new channel to monitor
     */
    public void setChannel(final Channel channel) {
        setChannel(channel, true);
    }

    /**
     * Sets the channelQuietly attribute of the MonitoredPV object
     *
     * @param channel The new channelQuietly value
     */
    public void setChannelQuietly(final Channel channel) {
        setChannel(channel, false);
    }

    /**
     * Set the name of the channel to monitor
     *
     * @param channelName The new channelName value
     */
    public void setChannelName(final String channelName) {
        if (channelName != null && !channelName.isEmpty()) {
            final Channel channel = ChannelFactory.defaultFactory().getChannel(channelName);
            setChannel(channel);
        } else {
            setChannel(null);
        }
    }

    /**
     * Sets the name of the channel to monitor without posting events
     *
     * @param chanName The new channelNameQuietly value
     */
    public void setChannelNameQuietly(final String chanName) {
        final Channel channel = ChannelFactory.defaultFactory().getChannel(chanName);
        setChannelQuietly(channel);
    }

    /**
     * Get the latest value of the monitored channel
     *
     * @return The value value
     */
    public double getValue() {
        return currentValue;
    }

    /**
     * Determine whether the channel is good meaning that it is connected, has
     * posted a monitored value and the latest event was successful
     *
     * @return channel status
     */
    public boolean isGood() {
        return monitor != null && monitor.isValid() && latestEventSuccessful;
    }

    /**
     * Add a listener for state change events from this monitored PV
     *
     * @param actionListener listener of state change events
     */
    public void addStateListener(final ActionListener actionListener) {
        stateMessageCenter.registerTarget(actionListener, this, ActionListener.class);
    }

    /**
     * Remove the listener from getting state change events from this monitored
     * PV
     *
     * @param actionListener the listener to remove
     */
    public void removeStateListener(final ActionListener actionListener) {
        stateMessageCenter.removeTarget(actionListener, this, ActionListener.class);
    }

    /**
     * Add a listener for value change events from this monitored PV
     *
     * @param actionListener The listener to add for value change events
     */
    public void addValueListener(final ActionListener actionListener) {
        valueMessageCenter.registerTarget(actionListener, this, ActionListener.class);
    }

    /**
     * Remove the listener from getting value change events from this monitored
     * PV
     *
     * @param actionListener the listener to remove
     */
    public void removeValueListener(final ActionListener actionListener) {
        valueMessageCenter.removeTarget(actionListener, this, ActionListener.class);
    }

    /**
     * Make an event for the given record and channel
     */
    ActionEvent makeEvent(final ChannelTimeRecord record, final Channel channel) {
        return new MonitoredPVEvent(this, record, channel);
    }

    /**
     * Stop the monitor
     */
    public void stopMonitor() {
        if (monitor != null) {
            monitor.stop();
        }
    }

    /**
     * Start the monitor
     */
    public void startMonitor() {
        if (monitor != null) {
            monitor.start();
        }
    }

    /**
     * Handle the delegate callbacks
     */
    private class MonitorDelegateHandler implements ScanChannelMonitorDelegate {

        /**
         * Callback for channel state events
         */
        @Override
        public void channelStateChanged(final ScanChannelMonitor monitor, final boolean valid) {
            final ActionEvent stateChangedAction = makeEvent(monitor.getLatestRecord(), monitor.getChannel());
            stateEventDispatch.actionPerformed(stateChangedAction);
        }

        /**
         * Callback for channel monitor events
         */
        @Override
        public void channelRecordUpdate(final ScanChannelMonitor monitor, final ChannelTimeRecord record) {
            final double value = record.doubleValue();
            MonitoredPV.this.currentValue = value;
            MonitoredPV.this.valueChanged = true;
            MonitoredPV.this.latestEventSuccessful = true;
            final ActionEvent valueChangedAction = makeEvent(record, monitor.getChannel());
            valueEventDispatch.actionPerformed(valueChangedAction);
        }
    }
}
