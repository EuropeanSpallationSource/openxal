//
//  SnapshotGroupTable.java
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.data.DataAdaptor;



/** represent the snapshot group (type) database table */
class SnapshotGroupTable {
	/** database table name */
	protected final String tableName;
	
	/** primary key */
	protected final String primaryKey;
	
	/** group description */
	protected final String descriptionColumn;
	
	/** snapshot period column */
	protected final String periodColumn;
	
	/** snapshot retention column */
	protected final String retentionColumn;
	
	/** service ID foreign key column */
	protected final String serviceColumn;
	
	/** proxy to the table of channel - channel group relationships */
	protected SnapshotGroupChannelTable snapshotGroupChannelTable;

        private static final Logger LOGGER = Logger.getLogger(SnapshotGroupTable.class.getName());
	
	/** Constructor */
	public SnapshotGroupTable( final DataAdaptor tableAdaptor, final SnapshotGroupChannelTable groupChannelTable ) {
		this( DBTableConfiguration.getInstance( tableAdaptor ), groupChannelTable );
	}
	
	
	/** Constructor */
	public SnapshotGroupTable( final DBTableConfiguration configuration, final SnapshotGroupChannelTable groupChannelTable ) {
		snapshotGroupChannelTable = groupChannelTable;
		
		tableName = configuration.getTableName();
		
		primaryKey = configuration.getColumn( "group" );
		
		descriptionColumn = configuration.getColumn( "description" );
		periodColumn = configuration.getColumn( "period" );
		retentionColumn = configuration.getColumn( "retention" );
		serviceColumn = configuration.getColumn( "service" );
	}
	
	
	/** fetch all channel groups */
	public List<ChannelGroup> fetchChannelGroups( final Connection connection ) throws SQLException {
		final ArrayList<ChannelGroup> groups = new ArrayList<>();
		final PreparedStatement groupsQueryStatement = getGroupsQueryStatement( connection );
		final ResultSet resultSet = groupsQueryStatement.executeQuery();
		while ( resultSet.next() ) {
			final ChannelGroup group = newChannelGroup( connection, resultSet );
			groups.add( group );
		}
		resultSet.close();
		groupsQueryStatement.close();
		return groups;
	}
	
	
	/**
	 * Fetch a channel group given the specified type
	 * @param connection database connection
	 * @param type channel group type
	 * @return channel group corresponding to the specified result set record
	 */
	public ChannelGroup fetchChannelGroup( final Connection connection, final String type ) throws SQLException {
		final PreparedStatement groupQueryStatement = getGroupQueryByNameStatement( connection );
		groupQueryStatement.setString( 1, type );
		
		final ResultSet resultSet = groupQueryStatement.executeQuery();
		try {
			return resultSet.next() ? newChannelGroup( connection, resultSet ) : null;
		} finally {
			resultSet.close();
			groupQueryStatement.close();
		}
	}
	
	
	/** produce a new channel group from the specified result set */
	private ChannelGroup newChannelGroup( final Connection connection, final ResultSet resultSet ) throws SQLException {
		final String groupID = resultSet.getString(primaryKey );		// should match type
		final String description = resultSet.getString(descriptionColumn );
		final double loggingPeriod = resultSet.getDouble(periodColumn );
		final double retention = resultSet.getDouble(retentionColumn );
		final String serviceID = resultSet.getString(serviceColumn );
		
		final String[] pvArray = snapshotGroupChannelTable.fetchActivePVsByType( connection, groupID );
		
		return new ChannelGroup( groupID, serviceID, description, pvArray, loggingPeriod, retention );			
	}
	
	
	/**
	 * Fetch channel groups as an array of types
	 * @param connection database connection
	 * @return array of types corresponding to all of the channel groups
	 */
	public String[] fetchTypes( final Connection connection )  throws SQLException {
		final List<String> types = new ArrayList<>();
		final ResultSet result = getGroupsQueryStatement( connection ).executeQuery();
		while ( result.next() ) {
			types.add(result.getString(primaryKey ) );
		}
		result.close();
		return types.toArray( new String[types.size()] );		
	}
	
	
	/**
	 * Fetch the channel groups associated with the service ID as an array of types
	 * @param connection database connection
	 * @param serviceID service ID of groups to fetch
	 * @return array of types corresponding to channel groups with the specified service ID
	 */
	public String[] fetchTypes( final Connection connection, final String serviceID ) throws SQLException {
		final PreparedStatement statement = getGroupsQueryByServiceStatement( connection );
		statement.setString( 1, serviceID );
		
		final List<String> types = new ArrayList<>();
		final ResultSet result = statement.executeQuery();
		while ( result.next() ) {
			types.add(result.getString(primaryKey ) );
		}
		result.close();
		statement.close();
		return types.toArray( new String[types.size()] );		
	}
	
	
	/**
	 * Get a prepared statement to get the snapshot groups.
	 * @return the prepared statement to query for available snapshot types
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getGroupsQueryStatement( final Connection connection ) throws SQLException {
		return connection.prepareStatement("SELECT * FROM " + tableName );
	}
	
	
	/**
	 * Get a prepared statement to get the snapshot group by group name (i.e. primary key).
	 * @return the prepared statement to query for a snapshot type with the specified name
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getGroupQueryByNameStatement( final Connection connection ) throws SQLException {
		return connection.prepareStatement("SELECT * FROM " + tableName + " WHERE " + primaryKey + " = ?" );
	}
	
	
	/**
	 * Get a prepared statement to get the snapshot groups associated with a service ID.
	 * @return the prepared statement to query for available snapshot types associated with a specified service ID
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getGroupsQueryByServiceStatement( final Connection connection ) throws SQLException {
		return connection.prepareStatement("SELECT * FROM " + tableName + " WHERE " + serviceColumn + " = ?" );
	}
	
	
	
	
	/**
	 * Create a prepared statement for updating records in the snapshot group database table.
	 * @return the prepared statement for updating records
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getUpdateStatement( final Connection connection ) throws SQLException {
		return connection.prepareStatement("UPDATE " + tableName + " SET " + descriptionColumn + " = ?, " + serviceColumn + " = ?, " + periodColumn + " = ?, " + retentionColumn + " = ? where " + primaryKey + " = ?" );
	}	
	
	
	/**
	 * Insert the channels for the specified group.
	 * @param connection database connection
	 * @param channelNames PVs to insert
	 * @param groupID Channel Group ID
	 */
	public void publishGroupEdits( final Connection connection, final Set<ChannelGroupRecord> groupRecords ) throws SQLException {
		final PreparedStatement updateStatement = getUpdateStatement( connection );
		boolean needsUpdate = false;
		
		for ( final ChannelGroupRecord groupRecord : groupRecords ) {
			if ( groupRecord != null ) {
				try {
					updateStatement.setString( 1, groupRecord.getDescription() );
					updateStatement.setString( 2, groupRecord.getServiceID() );
					updateStatement.setDouble( 3, groupRecord.getDefaultLoggingPeriod() );
					updateStatement.setDouble( 4, groupRecord.getRetention() );
					updateStatement.setString( 5, groupRecord.getLabel() );
					
					updateStatement.addBatch();
					needsUpdate = true;
				}
				catch( SQLException exception ) {
					LOGGER.log(Level.SEVERE, "Exception publishing update for group:  " + groupRecord.getLabel(), exception);
				}
			}
		}
		
		if ( needsUpdate ) {
			updateStatement.executeBatch();
		}
		
		if( updateStatement != null ) {
			updateStatement.close();
		}
	}	
}
