/*
 * LoggerService.java
 *
 * Created on Thu Jan 15 11:22:50 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.service.pvlogger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import xal.extension.service.ServiceDirectory;
import xal.ca.Channel;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LoggerService is the implementation of LoggerPortal that responds to requests
 * from remote clients on behalf of the logger model.
 *
 * @author tap
 */
public class LoggerService implements RemoteLogging {

    private static final Logger LOGGER = Logger.getLogger(LoggerService.class.getName());

    // constants
    protected final String identity = "PV Logger";

    // model
    protected final LoggerModel model;

    /**
     * LoggerService constructor
     */
    public LoggerService(final LoggerModel model) {
        this.model = model;
        broadcast();
    }

    /**
     * Begin broadcasting the service
     */
    public void broadcast() {
        ServiceDirectory.defaultDirectory().registerService(RemoteLogging.class, identity, this);
        LOGGER.log(Level.INFO, "broadcasting...");
    }

    /**
     * Set the period between events where we take and store machine snapshots.
     *
     * @param groupType identifies the group by type
     * @param period The period in seconds between events where we take and
     * store machine snapshots.
     */
    @Override
    public void setLoggingPeriod(String groupType, double period) {
        final LoggerSession session = model.getLoggerSession(groupType);
        if (session != null) {
            session.setLoggingPeriod(period);
        }
    }

    /**
     * Get the logging period.
     *
     * @param groupType identifies the group by type
     * @return The period in seconds between events where we take and store
     * machine snapshots.
     */
    @Override
    public double getLoggingPeriod(String groupType) {
        final LoggerSession session = model.getLoggerSession(groupType);
        if (session != null) {
            return session.getLoggingPeriod();
        } else {
            return 0;
        }
    }

    /**
     * Take a snapshot and publish it.
     *
     * @param groupID ID of the group for which to take the snapshot
     * @param comment snapshot comment
     * @return machine snapshot ID or an error code (less than 0) if the attempt
     * fails
     */
    @Override
    public int takeAndPublishSnapshot(final String groupID, final String comment) {
        try {
            final LoggerSession loggerSession = model.getPVLogger().getLoggerSession(groupID);
            return loggerSession != null ? (int) loggerSession.takeAndPublishSnapshot(comment).getId() : -1;
        } catch (Exception exception) {
            return -2;
        }
    }

    /**
     * publish snapshots in the snapshot buffer
     */
    @Override
    public void publishSnapshots() {
        model.publishSnapshots();
    }

    /**
     * Determine if a logger session exists for the specified group
     *
     * @param groupID group ID of the logger session for which to look
     * @return true if a session exists for the group and false if not
     */
    @Override
    public boolean hasLoggerSession(final String groupID) {
        return model.getPVLogger().hasLoggerSession(groupID);
    }

    /**
     * Determine if the logger is presently logging
     *
     * @param groupType identifies the group by type
     * @return true if the logger is logging and false if not
     */
    @Override
    public boolean isLogging(final String groupType) {
        final LoggerSession session = model.getLoggerSession(groupType);
        if (session != null) {
            return session.isLogging();
        } else {
            return false;
        }
    }

    /**
     * reload the logger session identified by the group type
     */
    @Override
    public boolean reloadLoggerSession(final String groupType) {
        return model.reloadLoggerSession(groupType);
    }

    /**
     * Stop logging, reload groups from the database and resume logging.
     */
    @Override
    public void restartLogger() {
        model.restartLogger();
    }

    /**
     * Resume the logger logging.
     */
    @Override
    public void resumeLogging() {
        model.resumeLogging();
    }

    /**
     * Stop the logger.
     */
    @Override
    public void stopLogging() {
        model.stopLogging();
    }

    /**
     * Shutdown the process.
     *
     * @param code The shutdown code which is normally just 0.
     */
    @Override
    public void shutdown(final int code) {
        model.shutdown(code);
    }

    /**
     * Get the name of the host where the application is running.
     *
     * @return The name of the host where the application is running.
     */
    @Override
    public String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException exception) {
            return "";
        }
    }

    /**
     * Get the launch time of the service.
     *
     * @return the launch time in seconds since the Java epoch of January 1,
     * 1970.
     */
    @Override
    public Date getLaunchTime() {
        return LoggerModel.getLaunchTime();
    }

    /**
     * Get a heartbeat from the service.
     *
     * @return the time measured from the service at which the heartbeat was
     * sent
     */
    @Override
    public Date getHeartbeat() {
        return new Date();
    }

    /**
     * Get the timestamp of the last channel event (e.g. channel
     * connected/disconnected event)
     *
     * @param groupType identifies the group by type
     * @return the wall clock timestamp of the last channel event
     */
    @Override
    public Date getLastChannelEventTime(final String groupType) {
        final LoggerSession session = model.getLoggerSession(groupType);
        if (session != null) {
            return session.getChannelGroup().getLastChannelEventTime();
        } else {
            return new Date(0);
        }
    }

    /**
     * Get the timestamp of the last logger event
     *
     * @param groupType identifies the group by type
     * @return the wall clock timestamp of the last logger event
     */
    @Override
    public Date getLastLoggerEventTime(final String groupType) {
        return model.getSessionModel(groupType).getLastLoggerEventTime();
    }

    /**
     * Get the list of group types
     *
     * @return a list of the group types
     */
    @Override
    public List<String> getGroupTypes() {
        return new ArrayList<>(model.getSessionTypes());
    }

    /**
     * Get the number of channels we wish to log.
     *
     * @param groupType identifies the group by type
     * @return the number of channels we wish to log
     */
    @Override
    public int getChannelCount(final String groupType) {
        return model.getLoggerSession(groupType).getChannelGroup().getChannelCount();
    }

    /**
     * Get the list of channel info tables. Each channel info table contains the
     * PV signal name and the channel connection status.
     *
     * @param groupType identifies the group by type
     * @return The list channel info tables corresponding to the channels we
     * wish to log
     */
    @Override
    public List<Map<String, Object>> getChannels(final String groupType) {
        final LoggerSession session = model.getLoggerSession(groupType);
        if (session != null) {
            final Collection<Channel> channels = session.getChannels();
            final List<Map<String, Object>> channelInfoList = new ArrayList<>(channels.size());
            for (final Channel channel : channels) {
                final Map<String, Object> info;
                info = new HashMap<>();
                info.put(CHANNEL_SIGNAL, channel.channelName());
                info.put(CHANNEL_CONNECTED, channel.isConnected());
                channelInfoList.add(info);
            }

            return channelInfoList;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Get the timestamp of the last published snapshot
     *
     * @param groupType identifies the group by type
     * @return the timestamp of the last published snapshot
     */
    @Override
    public Date getTimestampOfLastPublishedSnapshot(String groupType) {
        MachineSnapshot snapshot = model.getSessionModel(groupType).getLastPublishedSnapshot();
        return (snapshot != null) ? snapshot.getTimestamp() : new Date(0);
    }

    /**
     * Get the textual dump of the last published snapshot
     *
     * @param groupType identifies the group by type
     * @return the textual dump of the last published snapshot or null if none
     * exists
     */
    @Override
    public String getLastPublishedSnapshotDump(String groupType) {
        Object snapshot = model.getSessionModel(groupType).getLastPublishedSnapshot();
        return (snapshot != null) ? snapshot.toString() : "";
    }
}
