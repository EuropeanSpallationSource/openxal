/*
 * ChannelWrapper.java
 *
 * Created on Wed Dec 03 17:11:08 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.ConnectionListener;
import xal.ca.IEventSinkValTime;
import xal.ca.Monitor;
import xal.ca.MonitorException;



/**
 * ChannelWrapper is a wrapper for a Channel that handles connecting to the channel and setting up a monitor
 * when its channel is connected.  Cache the latest channel record that has been found by the monitor.
 *
 * @author  tap
 */
public class ChannelWrapper {
        private static final Logger LOGGER = Logger.getLogger(ChannelWrapper.class.getName());
    
	/** The channel to wrap */
	protected Channel channel;
	
	/** The monitor for the channel */
	protected Monitor monitor;
	
	/** The latest channel record found by the monitor */
	protected volatile ChannelTimeRecord record;
	
	/** The handler handles channel connection events */
	protected ConnectionHandler connectionHandler;
	
    
	/**
	 * ChannelWrapper constructor
	 * @param pv The PV for which to create a channel.
	 */
	public ChannelWrapper( final String pv ) {
		channel = ChannelFactory.defaultFactory().getChannel( pv );
		connectionHandler = new ConnectionHandler();
		channel.addConnectionListener( connectionHandler );		
	}
	
	
	/**
	 * Dispose of the channel resources:  Shutdown the monitor if a monitor is active.
	 * Remove the connection handler as a connection listener of the channel.
	 */
	public void dispose() {
		channel.removeConnectionListener( connectionHandler );
		if ( channel.isConnected() && monitor != null ) {
			monitor.clear();
			monitor = null;
		}
	}
	
	
	/**
	 * Add the specified object as a listener of channel connection events of the
	 * channel that is wrapped.
	 * @param listener the object to add as a connection listener.
	 */
	public void addConnectionListener( final ConnectionListener listener ) {
		channel.addConnectionListener( listener );
	}
	
	
	/**
	 * Remove the specified object from being a listener of channel connection events
	 * of the channel that is wrapped.
	 * @param listener the object to remove from being a connection listener
	 */
	public void removeConnectionListener( final ConnectionListener listener ) {
		channel.removeConnectionListener( listener );
	}
	 
	 
	/**
	* Request that the channel be connected.  When the channel connection occurs, create a monitor.  If
	* a channel is dropped then clear the record.
	*/
	protected void requestConnection() {
		channel.requestConnection();
	}
	
	
	/**
	* Create a monitor to listen for new channel records.  An instance of an internal anonymous class
	* is the listener of the monitor events and caches the latest channel record.
	*/
	protected void makeMonitor() {
		try {
			monitor = channel.addMonitorValTime( new IEventSinkValTime() {
				/** handle the monitor event by caching the latest channel record */
                                @Override
				public void eventValue( final ChannelTimeRecord aRecord, final Channel chan ) {
					record = isValid( aRecord ) ? aRecord : null;
				}
			}, Monitor.VALUE );
		}
		catch( ConnectionException | MonitorException exception ) {
			LOGGER.log(Level.SEVERE, null, exception);
		}
	}
	
	
	/**
	 * Validate the record.
	 * @param record the record to validate
	 * @return true if the record is valid and false if not
	 */
	private static boolean isValid( final ChannelTimeRecord record ) {
		final double[] array = record.doubleArray();
		for ( int index = 0 ; index < array.length ; index++ ) {
			if ( !isValidValue( array[index] ) ) return false;
		}
		
		return true;
	}
	
	
	/**
	 * Validate the value.
	 * @param value the value to validate
	 * @return true if the value is valid and false if not
	 */
	private static boolean isValidValue( final double value ) {
		return !Double.isNaN( value ) && !Double.isInfinite( value );
	}
	
	
	/**
	* Get the PV for the channel being wrapped.
	* @return the PV
	*/
	public String getPV() {
		return channel.channelName();
	}
	
	
	/**
	* Get the wrapped channel.
	* @return the wrapped channel
	*/
	public Channel getChannel() {
		return channel;
	}
	
	
	/**
	* Get the latest channel record cached.
	* @return the latest channel record cached.
	*/
	public ChannelTimeRecord getRecord() {
		return record;
	}
	
	
	
	/**
	 * Connection handler is a class whose instance listens for connection
	 * events of the wrapped channel.
	 */
	protected class ConnectionHandler implements ConnectionListener {
		/**
		 * Indicates that a connection to the specified channel has been established.
		 * If the monitor is null make a new monitor.
		 * @param channel The channel which has been connected.
		 */
                @Override
		public void connectionMade( final Channel channel ) {
			if ( monitor == null )  makeMonitor();
		}
		
		/**
		 * Indicates that a connection to the specified channel has been dropped.
		 * If the connection is dropped, clear the latest record.
		 * @param channel The channel which has been disconnected.
		 */
                @Override
		public void connectionDropped( final Channel channel ) {
			record = null;
		}
	}
}

