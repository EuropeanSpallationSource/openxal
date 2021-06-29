//
//  ChannelSnapshotTable.java
//  xal
//
//  Created by Pelaia II, Tom on 10/12/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.service.pvlogger;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.database.DatabaseAdaptor;
import xal.tools.database.DatabaseException;

/**
 * represent the channel snapshot database table
 */
class ChannelSnapshotTable {

    private static final Logger LOGGER = Logger.getLogger(ChannelSnapshotTable.class.getName());

    /**
     * database table name
     */
    protected final String tableName;

    /**
     * time stamp column
     */
    protected final String timestampColumn;

    /**
     * machine snapshot primary key
     */
    protected final String machineSnapshotColumn;

    /**
     * PV primary key
     */
    protected final String pvColumn;

    /**
     * value column
     */
    protected final String valueColumn;

    /**
     * status column
     */
    protected final String statusColumn;

    /**
     * severity column
     */
    protected final String severityColumn;

    /**
     * value array type (value holds an array of doubles)
     */
    protected final String valueArrayType;

    /**
     * Constructor
     */
    public ChannelSnapshotTable(final DBTableConfiguration configuration) {
        tableName = configuration.getTableName();

        machineSnapshotColumn = configuration.getColumn("machineSnapshot");
        pvColumn = configuration.getColumn("pv");

        timestampColumn = configuration.getColumn("timestamp");
        valueColumn = configuration.getColumn("value");
        statusColumn = configuration.getColumn("status");
        severityColumn = configuration.getColumn("severity");

        valueArrayType = configuration.getDataType("valueArray");
    }

    /**
     * Insert the channel snapshots.
     *
     * @param connection database connection
     * @param channelSnapshots channel snapshots to insert
     * @param machineSnapshotID machine snapshot ID
     */
    public void insert(final Connection connection, final DatabaseAdaptor databaseAdaptor, final ChannelSnapshot[] channelSnapshots, final long machineSnapshotID) throws SQLException {
        final PreparedStatement insertStatement = getInsertStatement(connection);
        boolean needsInsert = false;

        for (final ChannelSnapshot channelSnapshot : channelSnapshots) {
            if (channelSnapshot != null) {
                final Timestamp timeStamp = channelSnapshot.getTimestamp().getSQLTimestamp();
                try {
                    final Array valueArray = databaseAdaptor.getArray(valueArrayType, connection, channelSnapshot.getValue());
                    insertStatement.setLong(1, machineSnapshotID);
                    insertStatement.setString(2, channelSnapshot.getPV());
                    insertStatement.setTimestamp(3, timeStamp);

                    insertStatement.setArray(4, valueArray);

                    insertStatement.setInt(5, channelSnapshot.getStatus());
                    insertStatement.setInt(6, channelSnapshot.getSeverity());

                    insertStatement.addBatch();
                    needsInsert = true;
                } catch (SQLException | DatabaseException exception) {
                    System.err.println("Exception publishing channel snapshot:  " + channelSnapshot);
                    LOGGER.log(Level.SEVERE, null, exception);
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
     * Fetch the channel snapshots associated with a machine snapshot given by
     * the machine snapshot's unique identifier.
     *
     * @param connection database connection
     * @param machineSnapshotID machine snapshot primary key
     * @return The channel snapshots associated with the machine snapshop
     */
    public ChannelSnapshot[] fetchChannelSnapshotsForMachineSnapshotID(final Connection connection, final long machineSnapshotID) throws SQLException {
        final List<ChannelSnapshot> snapshots = new ArrayList<>();

        final PreparedStatement snapshotQuery = getQueryByMachineSnapshotStatement(connection);
        snapshotQuery.setLong(1, machineSnapshotID);

        final ResultSet resultSet = snapshotQuery.executeQuery();
        while (resultSet.next()) {
            final String pv = resultSet.getString(pvColumn);
            final Timestamp timestamp = resultSet.getTimestamp(timestampColumn);
            final Number[] bigValue = (Number[]) resultSet.getArray(valueColumn).getArray();
            final double[] value = toDoubleArray(bigValue);
            final short status = resultSet.getShort(statusColumn);
            final short severity = resultSet.getShort(severityColumn);
            snapshots.add(new ChannelSnapshot(pv, value, status, severity, new xal.ca.Timestamp(timestamp)));
        }
        if (snapshotQuery != null) {
            snapshotQuery.close();
        }
        resultSet.close();
        return snapshots.toArray(new ChannelSnapshot[snapshots.size()]);
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
        return connection.prepareStatement("INSERT INTO " + tableName + "(" + machineSnapshotColumn + ", " + pvColumn + ", " + timestampColumn + ", " + valueColumn + ", " + statusColumn + ", " + severityColumn + ") VALUES (?, ?, ?, ?, ?, ?)");
    }

    /**
     * Create a prepared statement to query for channel snapshot records
     * corresponding to a machine snapshot.
     *
     * @return the prepared statement to query for channel snapshots by machine
     * snapshot
     * @throws java.sql.SQLException if an exception occurs during a SQL
     * evaluation
     */
    protected PreparedStatement getQueryByMachineSnapshotStatement(final Connection connection) throws SQLException {
        return connection.prepareStatement("SELECT * FROM " + tableName + " WHERE " + machineSnapshotColumn + " = ?");
    }

    /**
     * Convert an array of numbers to an array of double values.
     *
     * @param numbers array of numbers to convert
     * @return array of double values corresponding to the input array of
     * numbers.
     */
    protected static double[] toDoubleArray(final Number[] numbers) {
        final double[] array = new double[numbers.length];

        for (int index = 0; index < numbers.length; index++) {
            array[index] = numbers[index].doubleValue();
        }

        return array;
    }
}
