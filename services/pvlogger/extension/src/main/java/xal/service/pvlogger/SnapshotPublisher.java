//
//  MachineSnapshotLogger.java
//  xal
//
//  Created by Pelaia II, Tom on 10/16/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.pvlogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import xal.tools.data.DataAdaptor;
import xal.tools.database.ConnectionDictionary;
import xal.tools.database.DatabaseAdaptor;


/** publishes machine snapshots to the persistent store */
class SnapshotPublisher {
	/** buffer of machine snapshots pending publishing */
	final List<MachineSnapshot> SNAPSHOT_BUFFER;
	
	/** timer which signals a log operation */
	protected final Timer LOG_TIMER;
	
	/** handles timer events */
	protected TimerTask publishingTask;
	
	/** publishing period in seconds */
	protected double publishingPeriod;
	
	/** database store */
	protected final PersistentStore PERSISTENT_STORE;
	
	/** connection dictionary */
	protected ConnectionDictionary connectionDictionary;
	
	
	/** Primary Constructor */
	protected SnapshotPublisher( final DataAdaptor adaptor, final PersistentStore persistentStore, final ConnectionDictionary connectionDictionary ) {
		PERSISTENT_STORE = persistentStore;
		
		setConnectionDictionary( connectionDictionary );
		
		SNAPSHOT_BUFFER = new ArrayList<MachineSnapshot>();
		
		publishingPeriod = adaptor.doubleValue( "publishPeriod" );
		
		LOG_TIMER = new Timer();
	}
	
	
	/** set the connection dictionary */
	protected void setConnectionDictionary( final ConnectionDictionary dictionary ) {
		connectionDictionary = dictionary;
	}
	
	
	/** determine if the logger is publishing */
	public boolean isPublishing() {
		return publishingTask != null;
	}
	
	
	/** start the publishing */
	public void start() {
		if ( !isPublishing() ) {
			final long delay = toMillisecondsFromSeconds( publishingPeriod );
			publishingTask = newPublishingTask();
			LOG_TIMER.schedule( publishingTask, delay, delay );
		}
	}
	
	
	/** stop the publishing */
	public void stop() {
		if ( publishingTask != null ) {
			disposePublishingTask();
		}
	}
	
	
	/**
	 * Get the publishing period
	 * @return publishing period in seconds
	 */
	public double getPublishingPeriod() {
		return publishingPeriod;
	}
	
	
	/** 
	 * Set the publishing period 
	 * @param period publishing period in seconds
	 */
	public void setPublishingPeriod( final double period ) {
		if ( period != publishingPeriod ) {
			publishingPeriod = period;
			if ( isPublishing() ) {
				stop();
				start();
			}
		}
	}
	
	
	/** publish machine snapshots to the persistent storage */
	public void publishSnapshots() {
		publishSnapshots( connectionDictionary );
	}
	
	
	/** publish machine snapshots to the persistent storage */
	synchronized public void publishSnapshots( final ConnectionDictionary connectionDictionary ) {
		try {
			final List<MachineSnapshot> machineSnapshots = getSnapshotBufferCopy();
			if ( machineSnapshots.size() > 0 ) {
				final Connection connection = PersistentStore.connectionInstance( connectionDictionary );
				
				try {
					connection.setAutoCommit( false );				
					publishSnapshots( connection, connectionDictionary.getDatabaseAdaptor(), machineSnapshots );
				}
				finally {
					connection.close();
				}
			}
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** publish machine snapshots to the persistent storage */
	protected void publishSnapshots( final Connection connection, final DatabaseAdaptor databaseAdaptor, final List<MachineSnapshot> machineSnapshots ) throws SQLException {
		final List<MachineSnapshot> publishedSnapshots = PERSISTENT_STORE.publish( connection, databaseAdaptor, machineSnapshots );
		removeFromBuffer( publishedSnapshots );
	}
	
	
	/** convert milliseconds to seconds */
	@SuppressWarnings("unused")
    private static double toSecondsFromMilliseconds( final long milliseconds ) {
		return ((double)milliseconds) / 1000.0;
	}
	
	
	/** convert seconds to milliseconds */
	private static long toMillisecondsFromSeconds( final double seconds ) {
		return (long)( 1000 * seconds );
	}
	
	
	/** add the specified snapshots to the buffer */
	public void scheduleSnapshotPublication( final MachineSnapshot snapshot ) {
		synchronized( SNAPSHOT_BUFFER ) {
			SNAPSHOT_BUFFER.add( snapshot );
		}		
	}
	
	
	/** clear the buffer of the specified snapshots */
	protected void removeFromBuffer( final List<MachineSnapshot> snapshots ) {
		if ( snapshots != null && snapshots.size() > 0 ) {
			synchronized( SNAPSHOT_BUFFER ) {
				SNAPSHOT_BUFFER.removeAll( snapshots );
			}
		}
	}
	
	
	/** get a copy of the snapshot buffer */
	protected List<MachineSnapshot> getSnapshotBufferCopy() {
		synchronized( SNAPSHOT_BUFFER ) {
			return new ArrayList<MachineSnapshot>( SNAPSHOT_BUFFER );
		}
	}
	
	
	/** dispose of the publishing task */
	protected void disposePublishingTask() {
		if ( publishingTask != null ) {
			publishingTask.cancel();
		}
		LOG_TIMER.purge();
		publishingTask = null;
	}

	
	/** get a new timer task for periodic publishing */
	protected final TimerTask newPublishingTask() {
		return new TimerTask() {
			public final void run() {
				publishSnapshots();
			}
		};
	}
}
