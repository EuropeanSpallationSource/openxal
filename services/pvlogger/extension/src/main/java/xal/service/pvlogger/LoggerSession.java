/*
 * LoggerSession.java
 *
 * Created on Thu Dec 04 09:01:51 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.service.pvlogger;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.ca.Channel;
import xal.ca.ChannelTimeRecord;
import xal.tools.messaging.MessageCenter;

/**
 * LoggerSession manages a session of logging machine state. One can create an
 * instance to log the current machine state either on demand or periodically.
 *
 * @author tap
 */
public class LoggerSession {

    private static final Logger LOGGER = Logger.getLogger(LoggerSession.class.getName());

    /**
     * initial timer delay
     */
    protected static final int INITIAL_DELAY = 1000;

    /**
     * default logging period in seconds
     */
    protected static final double DEFAULT_LOGGING_PERIOD = 5.0;

    /**
     * publisher of snapshots to the persistent store
     */
    protected final SnapshotPublisher snapshotPublisher;

    /**
     * latest snapshot taken which may or may not have been published
     */
    protected volatile MachineSnapshot latestMachineSnapshot;

    // messaging
    protected final MessageCenter messageCenter;
    protected final LoggerChangeListener eventProxy;

    // state variables
    protected ChannelGroup group;
    protected final Timer logTimer;
    protected TimerTask logTask;
    /**
     * logging period in seconds
     */
    protected double loggingPeriod;
    protected boolean enabled;

    /**
     * LoggerSession constructor
     *
     * @param group Group of channels to log.
     * @param publisher The snapshot publisher.
     */
    public LoggerSession(final ChannelGroup group, final SnapshotPublisher publisher) {
        messageCenter = new MessageCenter("PV Logger");
        eventProxy = messageCenter.registerSource(this, LoggerChangeListener.class);

        snapshotPublisher = publisher;

        logTimer = new Timer();

        enabled = false;
        setChannelGroup(group);
    }

    /**
     * Add a logger change listener to receive logger change events.
     *
     * @param listener The listener of the logger change events.
     */
    public void addLoggerChangeListener(final LoggerChangeListener listener) {
        messageCenter.registerTarget(listener, this, LoggerChangeListener.class);
    }

    /**
     * Remove a logger change listener from receiving logger change events.
     *
     * @param listener The listener of the logger change events.
     */
    public void removeLoggerChangeListener(final LoggerChangeListener listener) {
        messageCenter.removeTarget(listener, this, LoggerChangeListener.class);
    }

    /**
     * Resume periodic logging with the most recent settings if enabled.
     */
    public void resumeLogging() {
        if (isEnabled()) {
            disposeLoggingTask();
            logTask = newLoggingTask();
            final long delay = (long) (loggingPeriod * 1000);
            logTimer.schedule(logTask, delay, delay);
            eventProxy.stateChanged(this, LoggerChangeListener.LOGGING_CHANGED);
        }
    }

    /**
     * Start periodically logging machine state to the persistent storage.
     */
    public void startLogging() {
        final String message = "Start logging \"" + group.getLabel() + "\" with period " + getLoggingPeriod() + " seconds";
        LOGGER.log(Level.INFO, message);
        LOGGER.log(Level.INFO, message);
        resumeLogging();
    }

    /**
     * Start periodically logging machine state to the persistent storage.
     *
     * @param period The period in seconds between events where we take and
     * store machine snapshots.
     */
    public void startLogging(final double period) {
        setLoggingPeriod(period);
        resumeLogging();
    }

    /**
     * Stop the periodic machine state logging.
     */
    public void stopLogging() {
        if (logTask != null) {
            disposeLoggingTask();
            eventProxy.stateChanged(this, LoggerChangeListener.LOGGING_CHANGED);
        }
    }

    /**
     * Reveal whether the logger is scheduled to run periodically
     *
     * @return true if the logger is scheduled to run periodically or false
     * otherwise
     */
    public boolean isLogging() {
        return logTask != null;
    }

    /**
     * Set the period between events where we take and store machine snapshots.
     *
     * @param period The period in seconds between events where we take and
     * store machine snapshots.
     */
    public void setLoggingPeriod(final double period) {
        setEnabled(period > 0);
        boolean isRunning = isLogging();
        if (period != loggingPeriod) {
            loggingPeriod = period;
            if (isRunning && logTask != null) {
                disposeLoggingTask();
                resumeLogging();
            }
            eventProxy.stateChanged(this, LoggerChangeListener.LOGGING_PERIOD_CHANGED);
        }
    }

    /**
     * Get the loggin period.
     *
     * @return The period in seconds between events where we take and store
     * machine snapshots.
     */
    public double getLoggingPeriod() {
        return loggingPeriod;
    }

    /**
     * Determine whether this logger session is enabled
     *
     * @return true if this logger session is enabled and false if not
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set whether this session should be enabled
     *
     * @param enable true to enable this session and false to disable it
     */
    protected void setEnabled(final boolean enable) {
        this.enabled = enable;
        if (isLogging()) {
            stopLogging();
        }
        eventProxy.stateChanged(this, LoggerChangeListener.ENABLE_CHANGED);
    }

    /**
     * Get the active channel group for this session
     *
     * @return the channel group
     */
    public ChannelGroup getChannelGroup() {
        return group;
    }

    /**
     * Set the channel group for this logger session
     *
     * @param group the new channel group for this logger session
     */
    public void setChannelGroup(final ChannelGroup group) {
        final boolean shouldLog = isLogging();
        if (shouldLog) {
            stopLogging();
        }

        final ChannelGroup oldGroup = this.group;
        if (oldGroup != null && group != oldGroup) {
            oldGroup.dispose();
        } else if (group != null) {
            group.requestConnections();
            setLoggingPeriod(group.getDefaultLoggingPeriod());
        }

        if (group == null) {
            setLoggingPeriod(DEFAULT_LOGGING_PERIOD);
        }

        this.group = group;
        if (shouldLog) {
            resumeLogging();
        }
        eventProxy.stateChanged(this, LoggerChangeListener.GROUP_CHANGED);
    }

    /**
     * Get the channels which we are attempting to monitor and log
     *
     * @return a collection of the channels we wish to monitor and log
     */
    public Collection<Channel> getChannels() {
        return group.getChannels();
    }

    /**
     * Get the latest machine snapshot which may or may not have been published
     *
     * @return the latest machine snapshot
     */
    public MachineSnapshot getLatestMachineSnapshot() {
        return latestMachineSnapshot;
    }

    /**
     * Take a snapshot and publish it immediately
     *
     * @return the published snapshot
     */
    public final MachineSnapshot takeAndPublishSnapshot() {
        return takeAndPublishSnapshot("");
    }

    /**
     * Take a snapshot and publish it immediately
     *
     * @param comment machine snapshot comment
     * @return the published snapshot
     */
    public final MachineSnapshot takeAndPublishSnapshot(final String comment) {
        final MachineSnapshot machineSnapshot = takeSnapshot();
        machineSnapshot.setComment(comment);
        publishSnapshot(machineSnapshot);
        return machineSnapshot;
    }

    /**
     * Take a snapshot and schedule it for publication
     *
     * @return the scheduled snapshot
     */
    protected final MachineSnapshot takeAndScheduleSnapshotForPublication() {
        final MachineSnapshot machineSnapshot = takeSnapshot();
        snapshotPublisher.scheduleSnapshotPublication(machineSnapshot);
        return machineSnapshot;
    }

    /**
     * Take a snapshot of the current machine state.
     *
     * @return A snapshot of the current machine state.
     */
    public final MachineSnapshot takeSnapshot() {
        final ChannelWrapper[] channelWrappers = group.getChannelWrappers();
        MachineSnapshot machineSnapshot = new MachineSnapshot(channelWrappers.length);
        machineSnapshot.setType(group.getLabel());
        for (int index = 0; index < channelWrappers.length; index++) {
            ChannelWrapper channelWrapper = channelWrappers[index];
            if (channelWrapper != null) {
                ChannelTimeRecord channelRecord = channelWrapper.getRecord();
                if (channelRecord != null) {
                    ChannelSnapshot snapshot = new ChannelSnapshot(channelWrapper.getPV(), channelRecord);
                    machineSnapshot.setChannelSnapshot(index, snapshot);
                }
            }
        }

        latestMachineSnapshot = machineSnapshot;
        eventProxy.snapshotTaken(this, machineSnapshot);
        return machineSnapshot;
    }

    /**
     * Publish the machine snapshot to the persistent storage.
     *
     * @param machineSnapshot The machine snapshot to publish.
     */
    public final void publishSnapshot(final MachineSnapshot machineSnapshot) {
        snapshotPublisher.scheduleSnapshotPublication(machineSnapshot);
        snapshotPublisher.publishSnapshots();
        eventProxy.snapshotPublished(this, machineSnapshot);
    }

    /**
     * dispose of the logging task
     */
    protected void disposeLoggingTask() {
        if (logTask != null) {
            logTask.cancel();
        }
        logTimer.purge();
        logTask = null;
    }

    /**
     * get a new timer task for periodic logging
     */
    protected final TimerTask newLoggingTask() {
        return new TimerTask() {
            @Override
            public final void run() {
                // must catch exceptions to avoid the timer stopping
                try {
                    final MachineSnapshot machineSnapshot = takeSnapshot();
                    snapshotPublisher.scheduleSnapshotPublication(machineSnapshot);
                } catch (Exception exception) {
                    LOGGER.log(Level.WARNING, "Error publishing snapshot: ", exception);
                }
            }
        };
    }
}
