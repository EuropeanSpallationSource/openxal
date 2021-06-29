//
// ScanChannelMonitor.java
// xal
//
// Created by Pelaia II, Tom on 1/10/13
// Copyright 2013 ORNL. All rights reserved.
//

package xal.extension.scan;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.*;


/** ScanChannelMonitor */
public class ScanChannelMonitor {
        private static final Logger LOGGER = Logger.getLogger(ScanChannelMonitor.class.getName());
    
	/** synchronization lock */
	private final Object syncLock;

	/** channel to monitor */
	private final Channel channel;

	/** handler of events */
	private final EventHandler eventHandler;

	/** channel event monitor */
	private Monitor monitor;

	/** indicates whether this wrapper is viable or disposed */
	volatile private boolean viable;

	/** indicates whether the monitor is allowed */
	volatile private boolean allowsMonitor;

	/** handler of the monitor events */
	volatile private ScanChannelMonitorDelegate delegate;

	/** latest record captured */
	volatile private ChannelTimeRecord latestRecord;


	/** Constructor with null delegate */
	public ScanChannelMonitor( final Channel channel ) {
		this( channel, null );
	}


	/** Constructor */
	public ScanChannelMonitor( final Channel channel, final ScanChannelMonitorDelegate delegate ) {
		this( channel, delegate, true );
	}


	/** Primary Constructor */
	public ScanChannelMonitor( final Channel channel, final ScanChannelMonitorDelegate delegate, final boolean requestEvents ) {
		syncLock = new Object();
		eventHandler = new EventHandler();

		viable = true;

		allowsMonitor = requestEvents;

		latestRecord = null;
		monitor = null;
		this.delegate = delegate;

		if ( channel == null )	throw new IllegalArgumentException( "Channel Wrapper cannot be assigned a null channel" );

		this.channel = channel;

		if ( requestEvents ) {
			start();
		}
	}


	/** Get the channel */
	public Channel getChannel() {
		return channel;
	}


	/** set the delegate */
	public void setDelegate( final ScanChannelMonitorDelegate delegate ) {
		this.delegate = delegate;
	}


	/** determine whether the channel is connected */
	public boolean isConnected() {
		return channel.isConnected();
	}


	/** determine whether the channel is valid (has a record and is connected) */
	public boolean isValid() {
		return latestRecord != null && channel.isConnected();
	}


	/** Get the latest record */
	public ChannelTimeRecord getLatestRecord() {
		return latestRecord;
	}


	/** stop the monitor */
	public void stop() {
		synchronized( syncLock	) {
			channel.removeConnectionListener( eventHandler );
			allowsMonitor = false;

			if ( monitor != null )  monitor.clear();
                        monitor = null;
		}
	}


	/** start the monitor */
	public void start() {
		synchronized( syncLock	) {
			allowsMonitor = true;
			channel.addConnectionListener( eventHandler );
			channel.requestConnection();
		}
	}


	/** start the monitor */
	public void createMonitor() {
		synchronized( syncLock	) {
			if ( allowsMonitor && viable && monitor == null ) {
				try {
					monitor = channel.addMonitorValTime( eventHandler, Monitor.VALUE );
					Channel.flushIO();
				}
				catch( ConnectionException | MonitorException exception ) {
					LOGGER.log(Level.SEVERE, "Exception creating monitor for channel: " + channel.getId(), exception);
				}
			}
		}
	}


	/** Dispose of the channel */
	public void dispose() {
		synchronized( syncLock ) {
			viable = false;
			delegate = null;

			stop();
		}
	}


	/** process events */
	private class EventHandler implements ConnectionListener, IEventSinkValTime {
		/**
		 * Indicates that a connection to the specified channel has been established.
		 * @param channel The channel which has been connected.
		 */
                @Override
		public void connectionMade( final Channel channel ) {
			createMonitor();

			if ( delegate != null ) {
				delegate.channelStateChanged( ScanChannelMonitor.this, latestRecord != null );
			}
		}


		/**
		 * Indicates that a connection to the specified channel has been dropped.
		 * @param channel The channel which has been disconnected.
		 */
                @Override
		public void connectionDropped( final Channel channel ) {
			if ( delegate != null ) {
				delegate.channelStateChanged( ScanChannelMonitor.this, false );
			}
		}


		/** Handle monitor event */
                @Override
		public void eventValue( final ChannelTimeRecord record, final Channel channel ) {
			latestRecord = record;
			//System.out.println( "Captured record: " + record );

			if ( delegate != null ) {
				delegate.channelRecordUpdate( ScanChannelMonitor.this, record );		// forward the event to the delegate
			}
		}
	}
}
