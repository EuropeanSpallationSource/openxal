//
//  PVLogger.java
//  xal
//
//  Created by Pelaia II, Tom on 10/18/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.service.pvlogger;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.ResourceManager;
import xal.tools.data.DataAdaptor;
import xal.tools.database.ConnectionDictionary;
import xal.tools.database.DBConfiguration;
import xal.tools.xml.XmlDataAdaptor;

/**
 * Provides a public interface to the PV Logger package
 */
public class PVLogger {

    private static final Logger LOGGER = Logger.getLogger(PVLogger.class.getName());

    /**
     * database store
     */
    protected final PersistentStore persistentStore;

    /**
     * snapshot publisher
     */
    protected final SnapshotPublisher snapshotPublisher;

    /**
     * connection dictionary
     */
    protected ConnectionDictionary connectionDictionary;

    /**
     * logger sessions keyed by channel group ID
     */
    protected final Map<String, LoggerSession> loggerSessions;

    /**
     * current database connection
     */
    protected Connection connection;

    /**
     * Primary Constructor
     */
    public PVLogger(final ConnectionDictionary connectionDictionary) {
        loggerSessions = new HashMap<>();

        URL configurationURL = null;
        DBConfiguration dbConfig = DBConfiguration.getInstance();
        if (dbConfig != null) {
            configurationURL = dbConfig.getSchemaURL("pvlogger");
        }
        if (configurationURL == null) {
            configurationURL = ResourceManager.getResourceURL(getClass(), "configuration.xml");
        }
        final DataAdaptor configurationAdaptor = XmlDataAdaptor.adaptorForUrl(configurationURL, false).childAdaptor("Configuration");

        final DataAdaptor persistentStoreAdaptor = configurationAdaptor.childAdaptor("persistentStore");
        persistentStore = new PersistentStore(persistentStoreAdaptor);

        final DataAdaptor publisherAdaptor = configurationAdaptor.childAdaptor("publisher");
        snapshotPublisher = new SnapshotPublisher(publisherAdaptor, persistentStore, connectionDictionary);

        setConnectionDictionary(connectionDictionary);
    }

    /**
     * Constructor
     */
    public PVLogger() {
        this(newLoggingConnectionDictionary());
    }

    /**
     * get an instance for browsing the PV Logger data
     */
    public static PVLogger getBrowsingInstance() {
        final ConnectionDictionary dictionary = newBrowsingConnectionDictionary();
        return dictionary != null ? new PVLogger(dictionary) : null;
    }

    /**
     * get an instance for logging PV data to the database
     */
    public static PVLogger getLoggingInstance() {
        return new PVLogger();
    }

    /**
     * generate a new connection dictionary appropriate for logging
     */
    public static ConnectionDictionary newLoggingConnectionDictionary() {
        return ConnectionDictionary.getInstance("pvlogger");
    }

    /**
     * generate a new connection dictionary appropriate for browsing logged data
     */
    public static ConnectionDictionary newBrowsingConnectionDictionary() {
        // use the reports account if available, otherwise use the default account
        return ConnectionDictionary.getPreferredInstance("pvlogger-reports", "reports");
    }

    /**
     * get the connection dictionary
     */
    public ConnectionDictionary getConnectionDictionary() {
        return connectionDictionary;
    }

    /**
     * set the connection dictionary
     */
    public void setConnectionDictionary(final ConnectionDictionary dictionary) {
        connectionDictionary = dictionary;
        snapshotPublisher.setConnectionDictionary(dictionary);
    }

    /**
     * Get the logger session with the specified groupID
     *
     * @param groupID the channel group ID
     * @return the logger session with the specified group ID or null if none
     * exists
     */
    public LoggerSession getLoggerSession(final String groupID) {
        synchronized (loggerSessions) {
            return loggerSessions.get(groupID);
        }
    }

    /**
     * Get all logger sessions managed by this PV Logger
     *
     * @return the collection of logger sessions
     */
    public Collection<LoggerSession> getLoggerSessions() {
        synchronized (loggerSessions) {
            return loggerSessions.values();
        }
    }

    /**
     * remove all logger sessions
     */
    public void removeAllLoggerSessions() {
        synchronized (loggerSessions) {
            for (final LoggerSession session : new HashSet<>(getLoggerSessions())) {
                removeLoggerSession(session.getChannelGroup().getLabel());
            }
        }
    }

    /**
     * Stop the logger session with the specified group ID and remove it from
     * the PV Logger
     *
     * @param groupID group ID of the logger session to remove
     */
    public void removeLoggerSession(final String groupID) {
        synchronized (loggerSessions) {
            final LoggerSession session = getLoggerSession(groupID);
            if (session != null) {
                session.setEnabled(false);
                loggerSessions.remove(groupID);
            }
        }
    }

    /**
     * Determine if a logger session exists for the specified group
     *
     * @param groupID group ID of the logger session for which to look
     * @return true if a session exists for the group and false if not
     */
    public boolean hasLoggerSession(final String groupID) {
        synchronized (loggerSessions) {
            return loggerSessions.containsKey(groupID);
        }
    }

    /**
     * Request enabled logger sessions for the specified service
     *
     * @param serviceID service name
     * @return the list of logger sessions
     */
    public List<LoggerSession> requestEnabledLoggerSessionsForService(final String serviceID) throws SQLException {
        final String[] types = fetchTypes(serviceID);
        final List<LoggerSession> sessions = new ArrayList<>(types.length);
        final Connection dbConnection = getDatabaseConnection();
        for (final String groupID : types) {
            final ChannelGroup group = persistentStore.fetchChannelGroup(dbConnection, groupID);
            if (group.getDefaultLoggingPeriod() > 0) {
                sessions.add(requestLoggerSession(groupID));
            }
        }

        return sessions;
    }

    /**
     * Request logger sessions for the specified service
     *
     * @param serviceID service name
     * @return the list of logger sessions
     */
    public List<LoggerSession> requestLoggerSessionsForService(final String serviceID) throws SQLException {
        final String[] types = fetchTypes(serviceID);
        final List<LoggerSession> sessions = new ArrayList<>(types.length);
        for (final String groupID : types) {
            sessions.add(requestLoggerSession(groupID));
        }

        return sessions;
    }

    /**
     * If a logger session already exists for the channel group, get it
     * otherwise create a new one
     *
     * @param groupID the name of the channel group
     * @return an existing logger session if one exists otherwise a new logger
     * session or null if one could not be created
     */
    public LoggerSession requestLoggerSession(final String groupID) throws SQLException {
        synchronized (loggerSessions) {
            if (loggerSessions.containsKey(groupID)) {
                return getLoggerSession(groupID);
            }

            final Connection dbConnection = getDatabaseConnection();
            if (dbConnection == null) {
                return null;
            }
            final ChannelGroup group = persistentStore.fetchChannelGroup(dbConnection, groupID);
            if (group != null) {
                final LoggerSession session = new LoggerSession(group, snapshotPublisher);
                loggerSessions.put(groupID, session);
                return session;
            } else {
                return null;
            }
        }
    }

    /**
     * Reload the logger session for the specified channel group
     *
     * @param groupID the name of the channel group
     * @return the corresponding logger session or null if a corresponding
     * logger session cannot be found or generated
     */
    public LoggerSession reloadLoggerSession(final String groupID) throws SQLException {
        synchronized (loggerSessions) {
            if (loggerSessions.containsKey(groupID)) {
                final Connection dbConnection = getDatabaseConnection();
                if (dbConnection == null) {
                    return null;
                }
                final ChannelGroup group = persistentStore.fetchChannelGroup(dbConnection, groupID);
                final LoggerSession session = getLoggerSession(groupID);
                session.setChannelGroup(group);
                return session;
            } else {
                return requestLoggerSession(groupID);
            }
        }
    }

    /**
     * determine if the snapshot publisher is publishing snapshots periodically
     */
    public boolean isPublishing() {
        return snapshotPublisher.isPublishing();
    }

    /**
     * start logging sessions and publishing snapshots
     */
    public void start() {
        snapshotPublisher.start();

        synchronized (loggerSessions) {
            final Collection<LoggerSession> sessions = loggerSessions.values();
            for (final LoggerSession session : sessions) {
                if (!session.isLogging()) {
                    session.startLogging();
                }
            }
        }
    }

    /**
     * restart logging sessions and publishing snapshots
     */
    public void restart() {
        snapshotPublisher.start();

        synchronized (loggerSessions) {
            final Collection<LoggerSession> sessions = loggerSessions.values();
            for (final LoggerSession session : sessions) {
                if (!session.isLogging()) {
                    session.resumeLogging();
                }
            }
        }
    }

    /**
     * stop logging sessions and publishing snapshots but publish any scheduled
     * snapshots
     */
    public void stop() {
        snapshotPublisher.stop();
        synchronized (loggerSessions) {
            final Collection<LoggerSession> sessions = loggerSessions.values();
            for (final LoggerSession session : sessions) {
                session.stopLogging();
            }
        }
        snapshotPublisher.publishSnapshots();
    }

    /**
     * publish any scheduled snapshots remaining in the queue
     */
    public void publishSnapshots() {
        snapshotPublisher.publishSnapshots();
    }

    /**
     * Get the publishing period
     *
     * @return publishing period in seconds
     */
    public double getPublishingPeriod() {
        return snapshotPublisher.getPublishingPeriod();
    }

    /**
     * Set the publishing period
     *
     * @param period publishing period in seconds
     */
    public void setPublishingPeriod(final double period) {
        snapshotPublisher.setPublishingPeriod(period);
    }

    /**
     * Fetch the machine snapshot corresponding to the specified snasphot ID
     *
     * @param snapshotID machine snaspshot ID
     * @return machine snapshot corresponding to the specified ID
     */
    public MachineSnapshot fetchMachineSnapshot(final long snapshotID) throws SQLException {
        final Connection dbConnection = getDatabaseConnection();
        return persistentStore.fetchMachineSnapshot(dbConnection, snapshotID);
    }

    /**
     * Fetch the machine snapshots within the specified time range. If the type
     * is not null, then restrict the machine snapshots to those of the
     * specified type. The machine snapshots do not include the channel
     * snapshots. A complete snapshot can be obtained using the
     * fetchMachineSnapshot(id) method.
     *
     * @param type The type of machine snapshots to fetch or null for no
     * restriction
     * @param startTime The start time of the time range
     * @param endTime The end time of the time range
     * @return An array of machine snapshots meeting the specified criteria
     */
    public MachineSnapshot[] fetchMachineSnapshotsInRange(final String type, final Date startTime, final Date endTime) throws SQLException {
        final Connection dbConnection = getDatabaseConnection();
        return persistentStore.fetchMachineSnapshotsInRange(dbConnection, type, startTime, endTime);
    }

    /**
     * Fetch the channel snapshots from the data source and populate the machine
     * snapshot
     *
     * @param machineSnapshot The machine snapshot for which to fetch the
     * channel snapshots and load them
     * @return the machineSnapshot which is the same as the parameter returned
     * for convenience
     */
    public MachineSnapshot loadChannelSnapshotsInto(final MachineSnapshot machineSnapshot) throws SQLException {
        final Connection dbConnection = getDatabaseConnection();
        return persistentStore.loadChannelSnapshotsInto(dbConnection, machineSnapshot);
    }

    /**
     * Fetch channel groups as an array of types
     *
     * @return array of types corresponding to all of the channel groups
     */
    public String[] fetchTypes() throws SQLException {
        final Connection dbConnection = getDatabaseConnection();
        return persistentStore.fetchTypes(dbConnection);
    }

    /**
     * Fetch the channel groups associated with the service ID as an array of
     * types
     *
     * @param serviceID service ID of groups to fetch
     * @return array of types corresponding to channel groups with the specified
     * service ID
     */
    public String[] fetchTypes(final String serviceID) throws SQLException {
        final Connection dbConnection = getDatabaseConnection();
        return persistentStore.fetchTypes(dbConnection, serviceID);
    }

    /**
     * Get the channel group corresponding to the specified type.
     *
     * @param type channel group type
     */
    public ChannelGroup getChannelGroup(final String type) throws SQLException {
        final Connection dbConnection = getDatabaseConnection();
        return persistentStore.getChannelGroup(dbConnection, type);
    }

    /**
     * get the current database connection creating it if necessary
     */
    protected Connection getDatabaseConnection() {
        if (connection == null || !testConnection(connection)) {
            closeConnection();
            connection = getNewDatabaseConnection();
        }

        return connection;
    }

    /**
     * make a new database connection
     */
    protected Connection getNewDatabaseConnection() {
        try {
            Connection con = connectionDictionary.hasRequiredInfo() ? PersistentStore.connectionInstance(connectionDictionary) : null;
            LOGGER.log(Level.INFO, "Connection is {}", con == null ? "null" : con.toString());
            return con;
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            return null;
        }
    }

    /**
     * close the database connection if a connection exists and set the
     * connection to null
     */
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
        } finally {
            connection = null;
        }
    }

    /**
     * Test whether the connection is good
     *
     * @param connection the connection to test
     * @return true if the connection is good and false if not
     */
    protected static boolean testConnection(final Connection connection) {
        try {
            return !connection.isClosed();
        } catch (SQLException exception) {
            return false;
        }
    }

    /**
     * sql connections should be closed manually
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            closeConnection();
        } finally {
            super.finalize();
        }
    }
}
