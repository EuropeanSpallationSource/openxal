/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import xal.ca.Channel;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionListener;
import xal.ca.correlator.ChannelCorrelator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.tools.correlator.Correlation;
import xal.tools.correlator.CorrelationNotice;
import xal.tools.correlator.RecordFilter;
import xal.tools.messaging.MessageCenter;

/**
 *
 * @author nataliamilas
 */
public class BeamAgent extends BeamMarker<AcceleratorNode> implements RepRateListener {
	/** default time window in seconds for correlating this Node's signal events */
	public final static double DEFAULT_CORRELATION_WINDOW = 0.1;
	
	/** default amplitude threshold below which this Node signal is filtered out */
	protected final static double DEFAULT_AMPLITUDE_THRESHOLD = 10.0;

	/** event message center */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy for posting channel events */
	final protected BeamMarkerEventListener EVENT_PROXY;
    
	/** synchronize events dispatch to a single queue */
        final private ExecutorService EVENT_QUEUE;
    
	/** indicates whether this Node is enabled for display */
	protected boolean _enabled;
		
	/** channel for property 1 */
	protected Channel _Channel1;
	
	/** channel for property 2 */
	protected Channel _Channel2;
	
	/** channel for property 3 */
	protected Channel _Channel3;

        /** map of channels handlers keyed by the corresponding Node channel number */
	protected String[] _channelHandlers;
        
	/** map of channels keyed by the corresponding Node handles */
	protected HashMap<String,Channel> _channelTable;

	/** signal correlator */
	protected ChannelCorrelator _correlator;

	/** last record */
	protected volatile BeamMarkerRecord _lastRecord;
    

	/**
	 * Primary constructor
	 * @param node               the Node to monitor
         * @param channelHandlers    the handles on the channels to monitor (up to 3 channels)
	 * @param correlationWindow  the time in seconds for resolving correlated signals
	 */
	public BeamAgent( final AcceleratorNode node, final String[] channelHandlers, final double correlationWindow ) {
		super( node );
		
		MESSAGE_CENTER = new MessageCenter();
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, BeamMarkerEventListener.class );
                EVENT_QUEUE = Executors.newSingleThreadExecutor();
        
		_lastRecord = null;
		
		_enabled = true;
                
                _channelHandlers = channelHandlers;
                
		if ( isAvailable() ) {
			monitorSignals( correlationWindow );
		}
	}


	/**
	 * Constructor
	 * @param node  the Node to monitor
         * @param channelHandlers  the handles on the channels to monitor (up to 3 channels)
	 */
	public BeamAgent( final AcceleratorNode node, final String[] channelHandlers) {
		this( node, channelHandlers, DEFAULT_CORRELATION_WINDOW );
	}
	
	
	/**
	 * Get the BPM ID.
	 * @return the unique BPM ID
	 */
	public String getID() {
		return NODE.getId();
	}


	/**
	 * Add the specified listener as a receiver of Node events
	 * @param listener  the listener to receive BPM events
	 */
	public void addBeamEventListener( final BeamMarkerEventListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, BeamMarkerEventListener.class );
        
        EVENT_QUEUE.submit( new Runnable() {
            public void run() {
                listener.connectionChanged( BeamAgent.this, "Channel 1", _Channel1.isConnected() );
                listener.connectionChanged( BeamAgent.this, "Channel 2", _Channel2.isConnected() );
                listener.connectionChanged( BeamAgent.this, "Channel 3", _Channel3.isConnected() );
                                
                final BeamMarkerRecord lastRecord = _lastRecord;
                if ( lastRecord != null ) {
                    listener.stateChanged( BeamAgent.this, lastRecord );
                }
            }
        });
	}


	/**
	 * Remove the specified listener from receiving BPM events
	 * @param listener  the listener to remove from receiving BPM events
	 */
	public void removeBpmEventListener( final BeamMarkerEventListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, BeamMarkerEventListener.class );
	}


	/**
	 * Get the BPM manage by this agent
	 * @return   the BPM managed by this agent
	 */
	public AcceleratorNode getNode() {
		return NODE;
	}
	
	
	/** set whether this BPM is enabled for orbit display */
	public void setEnabled( final boolean enabled ) {
		_enabled = enabled;
	}
	
	
	/** determine if this BPM is enabled for orbit display */
	public boolean isEnabled() {
		return _enabled;
	}


	/**
	 * Determine if this BPM is valid and has a good status.
	 * @return   true if this BPM has a good status and is valid; false otherwise.
	 */
	public boolean isAvailable() {
		return NODE.getStatus() && NODE.getValid();
	}


	/**
	 * Determine if this BPM's channels are all connected.
	 * @return   true if this BPM is connected and false if not.
	 */
	public boolean isConnected() {
		try {
			return isChannelConnectedIfValid( _Channel1 ) && isChannelConnectedIfValid( _Channel2 ) && isChannelConnectedIfValid( _Channel3 );
		}
		catch ( NullPointerException exception ) {
			return false;
		}
	}


	/** Check whether valid channels are connected. If the channel is invalid, then just return true since it doesn't need to be connected. */
	private boolean isChannelConnectedIfValid( final Channel channel ) {
		return channel.isValid() ? channel.isConnected() : true;
	}


	/**
	 * Determine if this BPM is available and all of its channels are connected.
	 * @return   true if this BPM is online and false if not.
	 */
	public boolean isOnline() {
		// if all the channels are connected then the BPM must be available.
		return isConnected();
	}
	
	
	/**
	 * Get the latest record.
	 * @return the latest BPM record
	 */
	public BeamMarkerRecord getLatestRecord() {
		return _lastRecord;
	}
	

	/**
	 * Get the position of the BPM relative to the start of the specified sequence.
	 * @param sequence  The sequence relative to which the BPM's position is measured
	 * @return          the position of the BPM relative to the sequence in meters
	 */
	public double getPositionIn( AcceleratorSeq sequence ) {
		return sequence.getPosition( NODE );
	}


	/**
	 * Get the string representation of the BPM.
	 * @return   the BPM's string representation
	 */
	public String toString() {
		return NODE.toString();
	}


	/** Note the channel's validity */
	static private void noteChannelValidity( final Channel channel ) {
		if ( !channel.isValid() ) {
			System.out.println( channel.channelName() + " is marked invalid. Will ignore this channel." );
		}
	}


	/**
	 * Monitor and correlated the 3 handles signals for the Node.
	 * @param binTimespan  the timespan for the correlation bin
	 */
	protected void monitorSignals( double binTimespan) {
		if ( _correlator != null ) {
			_correlator.dispose();
		}

		_correlator = new ChannelCorrelator( binTimespan );
		_channelTable = new HashMap<String,Channel>( 3 );
		_Channel1 = monitorChannel(_channelHandlers[0]);
		_Channel2 = monitorChannel(_channelHandlers[1]);
		_Channel3 = monitorChannel(_channelHandlers[2]);

		noteChannelValidity( _Channel1 );
		noteChannelValidity( _Channel2 );
		noteChannelValidity( _Channel3 );

		_correlator.addListener( new CorrelationNotice<ChannelTimeRecord>() {
			final String CHANNEL1_ID = _Channel1.getId();
			final String CHANNEL2_ID = _Channel2.getId();
			final String CHANNEL3_ID = _Channel3.getId();

			final boolean Channel1Valid = _Channel1.isValid();
			final boolean Channel2Valid = _Channel2.isValid();
			final boolean Channel3Valid = _Channel3.isValid();
			

			/**
			 * Handle the correlation event. This method gets called when a correlation was posted.
			 * @param sender       The poster of the correlation event.
			 * @param correlation  The correlation that was posted.
			 */
			public void newCorrelation( Object sender, Correlation<ChannelTimeRecord> correlation ) {
				final Date timestamp = correlation.meanDate();

				// post a BPM record if all valid channels in the BPM have an entry in the correlation, otherwise ignore this correlation
				final double[] channel_Val = new double[3];
				channel_Val[0] = getValue( _channelHandlers[0], correlation );
				if ( Channel1Valid && !correlation.isCorrelated( CHANNEL1_ID ) ) {
					return;
				}
				
				channel_Val[1] = getValue( _channelHandlers[1], correlation );
				if ( Channel2Valid && !correlation.isCorrelated( CHANNEL1_ID ) ) {
					return;
				}
				
				channel_Val[2] = getValue( _channelHandlers[2], correlation );
				if ( Channel3Valid && !correlation.isCorrelated( CHANNEL1_ID ) ) {
					return;
				}
				
				final BeamMarkerRecord record = new BeamMarkerRecord( BeamAgent.this, timestamp, _channelHandlers, channel_Val );
				EVENT_QUEUE.submit( new Runnable() {
					public void run() {
						_lastRecord = record;
						EVENT_PROXY.stateChanged( BeamAgent.this, record );
					}
				});
			}


			/**
			 * Handle the no correlation event. This method gets called when no correlation was found within some prescribed time period.
			 * @param sender  The poster of the "no correlation" event.
			 */
			public void noCorrelationCaught( Object sender ) {
				System.out.println( "No Node event." );
			}


			/**
			 * Get the value for the specified field from the correlation.
			 * @param handle       the handle of the BPM field
			 * @param correlation  the correlation with the correlated data for the BPM event
			 * @return             the correlation's BPM field value corresponding to the handle
			 */
			private double getValue( final String handle, final Correlation<ChannelTimeRecord> correlation ) {
				final Channel channel = getChannel( handle );
				if ( channel.isValid() ) {
					final String channelID = channel.getId();
					final ChannelTimeRecord record = correlation.getRecord( channelID );
					return ( record != null ) ? record.doubleValue() : Double.NaN;
				}
				else {
					return Double.NaN;
				}
			}
		} );

		_correlator.startMonitoring();
	}


	/**
	 * Connect to the channel and monitor it with the correlator.
	 * @param handle  the handle of the channel to monitor with the correlator.
	 * @param filter  the channel's record filter for the correlation.
	 * @return        the channel for which the monitor was requested
	 */
	protected Channel monitorChannel( final String handle, final RecordFilter<ChannelTimeRecord> filter ) {
		Channel channel = NODE.getChannel( handle );
		_channelTable.put( handle, channel );

		// only monitor the channel if the channel is marked valid
		if ( channel.isValid() ) {
			correlateSignal( channel, filter );

			channel.addConnectionListener(
				new ConnectionListener() {
					/**
					 * Indicates that a connection to the specified channel has been established.
					 * @param channel  The channel which has been connected.
					 */
					public void connectionMade( final Channel channel ) {
						EVENT_QUEUE.submit( new Runnable() {
							public void run() {
								_lastRecord = null;
								correlateSignal( channel, filter );
								EVENT_PROXY.connectionChanged( BeamAgent.this, handle, true );
							}
						});
					}


					/**
					 * Indicates that a connection to the specified channel has been dropped.
					 * @param channel  The channel which has been disconnected.
					 */
					public void connectionDropped( final Channel channel ) {
						EVENT_QUEUE.submit( new Runnable() {
							public void run() {
								_lastRecord = null;
								EVENT_PROXY.connectionChanged( BeamAgent.this, handle, false );
							}
						});
					}
				} );

			if ( !channel.isConnected() ) {
				channel.requestConnection();
			}
		}

		return channel;
	}


	/**
	 * Connect to the channel and monitor it with the correlator. A null record filter is used for the channel.
	 * @param handle  the handle of the channel to monitor with the correlator.
	 * @return        the channel for which the monitor was requested
	 */
	protected Channel monitorChannel( final String handle ) {
		return monitorChannel( handle, null );
	}


	/**
	 * Monitor the channel with the correlator.
	 * @param channel  the channel to monitor with the correlator.
	 * @param filter   the channel's record filter for the correlation.
	 */
	protected void correlateSignal( final Channel channel, final RecordFilter<ChannelTimeRecord> filter ) {
		if ( !_correlator.hasSource( channel.getId() ) && channel.isConnected() ) {
			_correlator.addChannel( channel, filter );
		}
	}


	/**
	 * Get this agent's channel corresponding to the specified handle.
	 * @param handle  Description of the Parameter
	 * @return        The channel value
	 */
	public Channel getChannel( final String handle ) {
		return _channelTable.get( handle );
	}


	/**
	 * Notification that the rep-rate has changed.
	 * @param monitor  The monitor announcing the new rep-rate.
	 * @param repRate  The new rep-rate.
	 */
	public void repRateChanged( RepRateMonitor monitor, double repRate ) {
		if ( _correlator != null ) {
			// if repRate is undefined or outside the expected range, revert to default otherwise
			// make the time window half of the rep-rate
			double timeWindow = ( !Double.isNaN( repRate ) && ( repRate > 0 ) && ( repRate < 10000 ) ) ? 0.5 / repRate : DEFAULT_CORRELATION_WINDOW;
			_correlator.setBinTimespan( timeWindow );
		}
	}
}


