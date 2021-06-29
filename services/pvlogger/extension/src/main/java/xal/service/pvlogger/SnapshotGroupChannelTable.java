//
//  SnapshotGroupChannelTable.java
//  xal
//
//  Created by Pelaia II, Tom on 10/12/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.service.pvlogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * represent the snapshot group (type) - PV relationship database table
 */
class SnapshotGroupChannelTable {

    private static final Logger LOGGER = Logger.getLogger(SnapshotGroupChannelTable.class.getName());

    /**
     * database table name
     */
    private final String tableName;

    /**
     * Group primary key
     */
    private final String groupColumn;

    /**
     * Active indicator column
     */
    private final String activeIndicatorColumn;

    /**
     * PV primary key
     */
    private final String channelColumn;

    /**
     * Constructor
     */
    public SnapshotGroupChannelTable(final DBTableConfiguration configuration) {
        tableName = configuration.getTableName();

        groupColumn = configuration.getColumn("group");
        channelColumn = configuration.getColumn("channel");
        activeIndicatorColumn = configuration.getColumn("active");
    }

    /**
     * Fetch an array of PVs corresponding to the specified channel group
     *
     * @param connection database connection
     * @param type channel group type
     * @return array of PVs
     */
    public String[] fetchPVsByType(final Connection connection, final String type) throws SQLException {
        final PreparedStatement queryStatement = getGroupChannelQueryByGroupStatement(connection);
        queryStatement.setString(1, type);

        final List<String> pvs = new ArrayList<>();
        final ResultSet resultSet = queryStatement.executeQuery();
        while (resultSet.next()) {
            pvs.add(resultSet.getString(channelColumn));
        }
        resultSet.close();
        return pvs.toArray(new String[pvs.size()]);
    }

    /**
     * Fetch an array of active PVs corresponding to the specified channel group
     *
     * @param connection database connection
     * @param type channel group type
     * @return array of active PVs
     */
    public String[] fetchActivePVsByType(final Connection connection, final String type) throws SQLException {
        final PreparedStatement queryStatement = getActiveGroupChannelQueryByGroupStatement(connection);
        queryStatement.setString(1, type);

        final List<String> pvs = new ArrayList<>();
        final ResultSet resultSet = queryStatement.executeQuery();
        while (resultSet.next()) {
            pvs.add(resultSet.getString(channelColumn));
        }
        resultSet.close();
        return pvs.toArray(new String[pvs.size()]);
    }

    /**
     * Insert the channels for the specified group.
     *
     * @param connection database connection
     * @param channelNames PVs to insert
     * @param groupID Channel Group ID
     */
    public void insertChannels(final Connection connection, final List<String> channelNames, final String groupID) throws SQLException {
        final PreparedStatement insertStatement = getInsertStatement(connection);
        boolean needsInsert = false;

        for (final String channelName : channelNames) {
            if (channelName != null) {
                try {
                    insertStatement.setString(1, groupID);
                    insertStatement.setString(2, channelName);

                    insertStatement.addBatch();
                    needsInsert = true;
                } catch (SQLException exception) {
                    LOGGER.log(Level.SEVERE, "Exception publishing channel:  " + channelName, exception);
                }
            }
        }

        if (needsInsert) {
            insertStatement.executeBatch();
        }
        if (insertStatement != null) {
            insertStatement.close();
        }
    }

    /**
     * Get a prepared statement to get the relationships by group.
     *
     * @return the prepared statement to query for machine snapshot type-PV
     * record by type
     * @throws java.sql.SQLException if an exception occurs during a SQL
     * evaluation
     */
    protected PreparedStatement getGroupChannelQueryByGroupStatement(final Connection connection) throws SQLException {
        return connection.prepareStatement("SELECT * FROM " + tableName + " WHERE " + groupColumn + " = ?");
    }

    /**
     * Get a prepared statement to get the active relationships by group.
     *
     * @return the prepared statement to query for machine snapshot type-PV
     * record by type
     * @throws java.sql.SQLException if an exception occurs during a SQL
     * evaluation
     */
    protected PreparedStatement getActiveGroupChannelQueryByGroupStatement(final Connection connection) throws SQLException {
        return connection.prepareStatement("SELECT * FROM " + tableName + " WHERE " + groupColumn + " = ? AND " + activeIndicatorColumn + " = \'Y\'");
    }

    /**
     * Create a prepared statement for inserting new records into the channel
     * snapshot database table.
     *
     * @return the prepared statement for inserting a new channel snapshot
     * @throws java.sql.SQLException if an exception occurs during a SQL
     * evaluation
     */
    protected PreparedStatement getInsertStatement(final Connection connection) throws SQLException {
        return connection.prepareStatement("INSERT INTO " + tableName + "(" + groupColumn + ", " + channelColumn + ") VALUES (?, ?)");
    }
}
