//
//  LoggerConfiguration.java
//  xal
//
//  Created by Tom Pelaia on 8/31/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//
package xal.service.pvlogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.ResourceManager;
import xal.tools.data.DataAdaptor;
import xal.tools.database.DBConfiguration;
import xal.tools.xml.XmlDataAdaptor;

/**
 * Manage the configuration of the PV Logger
 */
public class LoggerConfiguration {

    private static final Logger LOGGER = Logger.getLogger(LoggerConfiguration.class.getName());

    /**
     * database store
     */
    protected final PersistentStore persistentStore;

    /**
     * current database connection
     */
    private Connection connection;

    /**
     * Constructor
     */
    public LoggerConfiguration(final Connection connection) {
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

        setConnection(connection);
    }

    /**
     * fetch the channel groups
     */
    public List<ChannelGroup> fetchChannelGroups() throws SQLException {
        final String[] types = persistentStore.fetchTypes(connection);
        final List<ChannelGroup> groups = new ArrayList<>();
        for (final String type : types) {
            groups.add(persistentStore.fetchChannelGroup(connection, type));
        }

        return groups;
    }

    /**
     * Publish the channel snapshots.
     *
     * @param channelNames PVs to insert
     * @param groupID Channel Group ID
     */
    public void publishChannelsToGroup(final List<String> channelNames, final String groupID) throws SQLException {
        persistentStore.insertChannels(connection, channelNames, groupID);
        connection.commit();
    }

    /**
     * Publish the group records as groups
     */
    public void publishGroupEdits(final Set<ChannelGroupRecord> groupRecords) throws SQLException {
        persistentStore.publishGroupEdits(connection, groupRecords);
        connection.commit();
    }

    /**
     * get the database connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * set the connection
     */
    public void setConnection(final Connection connection) {
        this.connection = connection;
    }

    /**
     * close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
        }

    }
}
