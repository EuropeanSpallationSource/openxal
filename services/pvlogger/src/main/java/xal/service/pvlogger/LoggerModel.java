/*
 * LoggerModel.java
 *
 * Created on Wed Jan 14 14:44:39 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.service.pvlogger;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LoggerModel is the main model for the pvlogger service. It manages the logger
 * sessions.
 *
 * @author tap
 */
public class LoggerModel {

    private static final Logger LOGGER = Logger.getLogger(LoggerModel.class.getName());

    /**
     * The time when this process was launched in seconds since the Java epoch
     */
    private static final Date LAUNCH_TIME;

    /**
     * PV Logger
     */
    private final PVLogger pvLogger;

    /**
     * session models keyed by group ID
     */
    private final Map<String, SessionModel> sessionModels;

    /**
     * ID of the service to log
     */
    private final String serviceId;

    /**
     * static initialization
     */
    static {
        LAUNCH_TIME = new Date();
    }

    /**
     * LoggerModel constructor
     */
    public LoggerModel() {
        sessionModels = new HashMap<>();
        serviceId = System.getProperty("serviceID", "PHYSICS");

        pvLogger = new PVLogger();

        reloadGroups();
    }

    /**
     * Start logging
     */
    public void startLogging() {
        pvLogger.start();
    }

    /**
     * Restart the logger. Stop logging, reload groups from the database and
     * resume logging.
     */
    public void restartLogger() {
        stopLogging();
        reloadGroups();
        startLogging();
    }

    /**
     * Reload the channel groups from the persistent store
     */
    public void reloadGroups() {
        sessionModels.clear();

        pvLogger.removeAllLoggerSessions();

        try {
            final List<LoggerSession> loggerSessions = pvLogger.requestEnabledLoggerSessionsForService(serviceId);
            for (final LoggerSession session : loggerSessions) {
                final String groupType = session.getChannelGroup().getLabel();
                sessionModels.put(groupType, new SessionModel(session));
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
        }
    }

    /**
     * reload the group identified by the group ID
     */
    public boolean reloadLoggerSession(final String groupID) {
        try {
            pvLogger.reloadLoggerSession(groupID);
            return true;
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            return false;
        }
    }

    /**
     * Fetch the channel group types from the state store
     *
     * @return an array of channel group types in the state store
     */
    protected String[] fetchChannelGroupTypes() {
        try {
            return pvLogger.fetchTypes(serviceId);
        } catch (SQLException exception) {
            return new String[0];
        }
    }

    /**
     * Get the session model identified by the specified group type
     *
     * @param groupType the identifier of the group
     * @return the session model for the specified type or null if there is no
     * match
     */
    public SessionModel getSessionModel(final String groupType) {
        return sessionModels.get(groupType);
    }

    /**
     * Get the logger session identified by the specified group type
     *
     * @param groupType the identifier of the group
     * @return the logger session for the specified type or null if there is no
     * match
     */
    public LoggerSession getLoggerSession(final String groupType) {
        SessionModel sessionModel = getSessionModel(groupType);
        return sessionModel != null ? sessionModel.getLoggerSession() : null;
    }

    /**
     * Get the PV Logger
     *
     * @return the PV Logger
     */
    public PVLogger getPVLogger() {
        return pvLogger;
    }

    /**
     * Get the list of session types
     *
     * @return the list of session types
     */
    public Collection<String> getSessionTypes() {
        return sessionModels.keySet();
    }

    /**
     * publish snapshots in the buffer
     */
    public void publishSnapshots() {
        pvLogger.publishSnapshots();
    }

    /**
     * Resume logging.
     */
    public void resumeLogging() {
        pvLogger.restart();
    }

    /**
     * Stop logging
     */
    public void stopLogging() {
        pvLogger.stop();
    }

    /**
     * Shutdown the service
     *
     * @param code The shutdown code which is normally just 0.
     */
    public void shutdown(final int code) {
        System.exit(code);
    }

    /**
     * Get the launch time of the service.
     *
     * @return the launch time in seconds since the Java epoch of January 1,
     * 1970.
     */
    public static Date getLaunchTime() {
        return LAUNCH_TIME;
    }
}
