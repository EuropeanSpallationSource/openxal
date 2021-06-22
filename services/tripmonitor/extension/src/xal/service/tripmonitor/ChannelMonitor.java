//
//  ChannelMonitor.java
//  xal
//
//  Created by Thomas Pelaia on 8/1/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.util.logging.*;

import xal.ca.*;
import xal.tools.messaging.MessageCenter;


/** monitor a single channel */
public class ChannelMonitor {
	/** synchronization lock */
	protected final Object EVENT_LOCK;
	
	/** event message center */
	protected final MessageCenter MESSAGE_CENTER;
	
	/** proxy for posting channel events */
	protected final ChannelEventListener EVENT_PROXY;
	
	/** PV channel */
	protected final Channel CHANNEL;
	
	/** trip filter */
	protected final TripFilter TRIP_FILTER;
	
	/** event monitor */
	protected Monitor monitor;
	
	/** last record captured */
	protected ChannelTimeRecord lastRecord;
	
	/** connection listener */
	protected ConnectionListener connectionListener;
	
	
	/** constructor */
	public ChannelMonitor( final String pv, final TripFilter tripFilter ) {
		MESSAGE_CENTER = new MessageCenter( "Channel Monitor" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, ChannelEventListener.class );
		
		EVENT_LOCK = new Object();
		CHANNEL = ChannelFactory.defaultFactory().getChannel( pv );
		TRIP_FILTER = tripFilter;
		
		lastRecord = null;
		connectionListener = null;
	}
	
	
	/**
	 * Register the listener as a receiver of channel events from the wrapped channel
	 * @param listener  The listener to receive channel events
	 */
	public void addChannelEventListener( ChannelEventListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, ChannelEventListener.class );
		if ( CHANNEL != null ) {
			boolean isConnected;
			ChannelTimeRecord lastRecord;
			
			synchronized( EVENT_LOCK ) {
				isConnected = isConnected();
				lastRecord = lastRecord;
			}
			
			listener.connectionChanged( this, isConnected );
			if ( lastRecord != null ) {
				listener.valueChanged( this, lastRecord );
			}
		}
	}
	
	
	/**
	 * Unregister the listener as a receiver of channel events from the wrapped channel
	 * @param listener  The listener to unregister from receiving channel events
	 */
	public void removeChannelEventListener( ChannelEventListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, ChannelEventListener.class );
	}
	
	
	/**
		* Get the PV for the channel being wrapped.
	 *
	 * @return   the PV
	 */
	public String getPV() {
		return CHANNEL.channelName();
	}
	
	
	/**
	 * Get the wrapped channel.
	 * @return   the wrapped channel
	 */
	public Channel getChannel() {
		return CHANNEL;
	}
	
	
	/** get a description of this channel monitor */
	public String toString() {
		return getPV();
	}
	
	
	/**
	 * Determine if the channel is connected.
	 * @return   true if the channel is connected and false if not.
	 */
	public boolean isConnected() {
		return CHANNEL.isConnected();
	}
	
	
	/**
		* Get the latest record
	 * @return the latest record
	 */
	public ChannelTimeRecord getLatestRecord() {
		synchronized( EVENT_LOCK ) {
			return lastRecord;
		}
	}
	
	
	/**
	 * Process the new monitor event
	 * @param record the new record from which to get the latest value to test for a trip
	 * @param oldRecord the old record which preceeds the new record
	 */
	protected void processMonitorEvent( final ChannelTimeRecord oldRecord, final ChannelTimeRecord record ) {
		boolean shouldPostTrip;	// indicates that a trip was found and should be posted
		int value;
		synchronized( EVENT_LOCK ) {
			value = record.intValue();
			if ( oldRecord != null ) {
				final int oldValue = oldRecord.intValue();
				shouldPostTrip = TRIP_FILTER.isTripped( oldValue, value );
			}
			else {
				return;
			}
		}
		if ( shouldPostTrip ) {
			EVENT_PROXY.handleTrip( this, new TripRecord( getPV(), record.getTimestamp(), value ) );
		}
	}
	
	
	/** clear the trip count */
	protected void clearTrips() {
		synchronized( EVENT_LOCK ) {
			lastRecord = null;
		}
	} 
	
	
	/** Request that the channel be connected. When the channel connection occurs, create a monitor. */
	public void requestConnection() {
		if ( TripMonitorManager.isVerbose() ) {
			System.out.println( "Request connection for channel:  " + CHANNEL.channelName() );
		}
		
		if ( connectionListener == null ) {
			connectionListener = new ConnectionListener() {
				/**
				 * Indicates that a connection to the specified channel has been established.
				 * @param channel  The channel which has been connected.
				 */
				public void connectionMade( final Channel channel ) {
					synchronized( EVENT_LOCK ) {
						lastRecord = null;
						if ( monitor == null ) {
							makeMonitor();
						}
					}
					TripMonitorManager.printlnIfVerbose( channel + " is connected." );
					EVENT_PROXY.connectionChanged( ChannelMonitor.this, true );
				}
				
				
				/**
				 * Indicates that a connection to the specified channel has been dropped.
				 * @param channel  The channel which has been disconnected.
				 */
				public void connectionDropped( final Channel channel ) {
					synchronized(EVENT_LOCK) {
						lastRecord = null;
					}
					TripMonitorManager.printlnIfVerbose( channel + " is disconnected." );
					EVENT_PROXY.connectionChanged( ChannelMonitor.this, false );
				}
			};
			
			CHANNEL.addConnectionListener( connectionListener );
		}
		
		if ( !CHANNEL.isConnected() ) {		// initiate a connection
			CHANNEL.requestConnection();
		}
	}
	
	
	/**
	 * Create a monitor to listen for new channel records. An instance of an internal anonymous
	 * class is the listener of the monitor events and caches the latest channel record.
	 */
	protected void makeMonitor() {
		try {
			monitor = CHANNEL.addMonitorValTime( new IEventSinkValTime() {
				/**
				* Process the monitor event by caching the latest channel record
				* @param record   Description of the Parameter
				* @param channel  Description of the Parameter
				*/
				public void eventValue( final ChannelTimeRecord record, final Channel channel ) {
					ChannelTimeRecord oldRecord;
					synchronized ( EVENT_LOCK ) {
						oldRecord = lastRecord;
						lastRecord = record;
					}
					if ( EVENT_PROXY != null ) {
						EVENT_PROXY.valueChanged( ChannelMonitor.this, record );
					}
					processMonitorEvent( oldRecord, record );
				}
			}, Monitor.VALUE );
			TripMonitorManager.printlnIfVerbose( CHANNEL.channelName() + " monitor made." );
		}
		catch ( ConnectionException exception ) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Connection exception.", exception );
			exception.printStackTrace();
		}
		catch ( MonitorException exception ) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Monitor exception.", exception );
			exception.printStackTrace();
		}
	}
	
	
	/** Dispose of the channel wrapper resources by clearing the monitor (if any) and disposing of the messaging resources. */
	public void dispose() {
		synchronized ( EVENT_LOCK ) {
			if ( connectionListener != null ) {
				CHANNEL.removeConnectionListener( connectionListener );
				connectionListener = null;
			}
			if ( monitor != null ) {
				monitor.clear();
			}
			monitor = null;
		}
	}
}
