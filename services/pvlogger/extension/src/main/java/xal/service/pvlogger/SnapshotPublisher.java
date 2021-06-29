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
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.data.DataAdaptor;
import xal.tools.database.ConnectionDictionary;
import xal.tools.database.DatabaseAdaptor;


/** publishes machine snapshots to the persistent store */
class SnapshotPublisher {
	/** buffer of machine snapshots pending publishing */
	final List<MachineSnapshot> snapshotBuffer;
	
	/** timer which signals a log operation */
	protected final Timer logTimer;
	
	/** handles timer events */
	protected TimerTask publishingTask;
	
	/** publishing period in seconds */
	protected double publishingPeriod;
	
	/** database store */
	protected final PersistentStore persistentStore;
	
	/** connection dictionary */
	protected ConnectionDictionary connectionDictionary;
        
        private static final Logger LOGGER = Logger.getLogger(SnapshotPublisher.class.getName());
	
	
	/** Primary Constructor */
	protected SnapshotPublisher( final DataAdaptor adaptor, final PersistentStore persistentStore, final ConnectionDictionary connectionDictionary ) {
		this.persistentStore = persistentStore;
		
		setConnectionDictionary( connectionDictionary );
		
		snapshotBuffer = new ArrayList<>();
		
		publishingPeriod = adaptor.doubleValue( "publishPeriod" );
		
		logTimer = new Timer();
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
			logTimer.schedule( publishingTask, delay, delay );
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
			LOGGER.log(Level.SEVERE, null, exception);
		}
	}
	
	
	/** publish machine snapshots to the persistent storage */
	protected void publishSnapshots( final Connection connection, final DatabaseAdaptor databaseAdaptor, final List<MachineSnapshot> machineSnapshots ) throws SQLException {
		final List<MachineSnapshot> publishedSnapshots = persistentStore.publish( connection, databaseAdaptor, machineSnapshots );
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
		synchronized( snapshotBuffer ) {
			snapshotBuffer.add( snapshot );
		}		
	}
	
	
	/** clear the buffer of the specified snapshots */
	protected void removeFromBuffer( final List<MachineSnapshot> snapshots ) {
		if ( snapshots != null && snapshots.size() > 0 ) {
			synchronized( snapshotBuffer ) {
				snapshotBuffer.removeAll( snapshots );
			}
		}
	}
	
	
	/** get a copy of the snapshot buffer */
	protected List<MachineSnapshot> getSnapshotBufferCopy() {
		synchronized( snapshotBuffer ) {
			return new ArrayList<>( snapshotBuffer );
		}
	}
	
	
	/** dispose of the publishing task */
	protected void disposePublishingTask() {
		if ( publishingTask != null ) {
			publishingTask.cancel();
		}
		logTimer.purge();
		publishingTask = null;
	}

	
	/** get a new timer task for periodic publishing */
	protected final TimerTask newPublishingTask() {
		return new TimerTask() {
                        @Override
			public final void run() {
				publishSnapshots();
			}
		};
	}
}
