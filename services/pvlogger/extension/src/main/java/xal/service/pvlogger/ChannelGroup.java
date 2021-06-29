/*
 * ChannelGroup.java
 *
 * Created on Mon Dec 08 11:39:25 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xal.ca.Channel;
import xal.ca.ConnectionListener;



/**
 * ChannelGroup is a wrapper for the PV_SET data source which defines a group of related PVs.
 *
 * @author  tap
 */
public class ChannelGroup {
	/** default logging period (seconds) for the group */
	protected final double defaultLoggingPeriod;
	
	/** retention time in days (or zero for permanent retention) for snapshots associated with this group */
	protected final double retention;
	
	/** label of the channel group */
	protected final String label;
	
	/** service ID */
	protected final String serviceId;
	
	/** description of the channel group */
	protected final String description;
	
	/** array of channel wrappers */
	protected ChannelWrapper[] channelWrappers;
	
	/** handler of channel connection events */
	protected ConnectionHandler connectionHandler;
	
	/** time of last channel connection event */
	private Date lastChannelEventTime;
	
	
	/**
	 * Primary Constructor
	 * @param label The group's label
	 * @param description A description of the group
	 * @param pvs The PVs in the group
	 * @param loggingPeriod The default logging period for the group
	 * @param retention the rentention time in days (or zero for permanent retention) for snapshots associated with this group
	 */
	public ChannelGroup( final String label, final String serviceID, final String description, final String[] pvs, final double loggingPeriod, final double retention ) {
		this.label = label;
		serviceId = serviceID;
		this.description = description;
		defaultLoggingPeriod = loggingPeriod;
		this.retention = retention;
		
		lastChannelEventTime = new Date();
		connectionHandler = new ConnectionHandler();
		wrapPVs( pvs );
	}
	
	
	/**
	 * Constructor
	 * @param label The group's label
	 * @param description A description of the group
	 * @param pvs The PVs in the group
	 * @param loggingPeriod The default logging period for the group
	 */
	public ChannelGroup( final String label, final String serviceID, final String description, final String[] pvs, final double loggingPeriod) {
		this( label, serviceID, description, pvs, loggingPeriod, 0 );
	}
	
	
	/**
	 * Constructor
	 * @param groupLabel The group's label
	 * @param pvs The PVs in the group
	 * @param loggingPeriod The default logging period for the group
	 */
	public ChannelGroup( final String groupLabel, final String serviceID, final String[] pvs, final double loggingPeriod ) {
		this( groupLabel, serviceID, "", pvs, loggingPeriod );
	}
	
	
	/**
	 * Dispose of this channel group's resources
	 */
	public void dispose() {
		for ( int index = 0 ; index < channelWrappers.length ; index++ ) {
			channelWrappers[index].removeConnectionListener(connectionHandler);
		}
	}
	
	
	/**
	 * Get the group label.
	 * @return the group label.
	 */
	public String getLabel() {
		return label;
	}
	
	
	/**
	 * Get the service ID
	 * @return the service ID
	 */
	public String getServiceID() {
		return serviceId;
	}
	
	
	/**
	 * Get a description of the group
	 * @return a description of this group
	 */
	public String getDescription() {
		return description;
	}
	
	
	/**
	 * Get the default logging period for this group
	 * @return the default logging period in seconds
	 */
	public double getDefaultLoggingPeriod() {
		return defaultLoggingPeriod;
	}
	
	
	/** get the retention in days (or 0 for permanent) of the snapshots associated with this group */
	public double getRetention() {
		return retention;
	}
	
	
	/**
	 * Create ChannelWrappers for the specified pvs.
	 * @param pvs the list of PVs to wrap
	 */
	protected void wrapPVs( final String[] pvs ) {		
		List<ChannelWrapper> wrappers = new ArrayList<>(pvs.length);
		
		for ( int index = 0 ; index < pvs.length ; index++ ) {
			ChannelWrapper wrapper = new ChannelWrapper(pvs[index]);
			wrapper.addConnectionListener(connectionHandler);
			wrappers.add(wrapper);
		}
		
		channelWrappers = wrappers.toArray( new ChannelWrapper[wrappers.size()] );
	}
	
	
	/**
	 * Request connections to the channel wrappers.
	 */
	public void requestConnections() {			
		for ( int index = 0 ; index < channelWrappers.length ; index++ ) {
			channelWrappers[index].requestConnection();
		}
		Channel.flushIO();
	}
	
	
	/**
	 * Get the channel wrappers for the channels associated with this group.
	 * @return The array of channel wrappers of this group.
	 */
	public ChannelWrapper[] getChannelWrappers() {
		return channelWrappers;
	}
	
	
	/**
	 * Get the collection of channels which we attempt to monitor
	 * @return a collection of channels corresponding to the channel wrappers
	 */
	public Collection<Channel> getChannels() {
		Set<Channel> channels = new HashSet<>(channelWrappers.length);
		
		for ( int index = 0 ; index < channelWrappers.length ; index++ ) {
			channels.add( channelWrappers[index].getChannel() );
		}
		
		return channels;
	}
	
	
	/**
	 * Get the number of channels in this group
	 * @return The number of channels in this group
	 */
	public int getChannelCount() {
		return channelWrappers.length;
	}
	
	
	/**
	 * Get the timestamp of the last channel event (e.g. channel connected/disconnected event)
	 * @return the wall clock timestamp of the last channel event
	 */
	public Date getLastChannelEventTime() {
		return lastChannelEventTime;
	}
	
	
	/**
	 * Override toString() to return the group label and a description
	 * @return the group label and description
	 */
        @Override
	public String toString() {
		return "[label: " + label + ", description: " + description + "]";  
	}
	
	
	
	/**
	 *
	 */
	protected class ConnectionHandler implements ConnectionListener {
		/**
		 * Indicates that a connection to the specified channel has been established.
		 * @param channel The channel which has been connected.
		 */
                @Override
		public void connectionMade(Channel channel) {
			lastChannelEventTime = new Date();
		}
		
		/**
		 * Indicates that a connection to the specified channel has been dropped.
		 * @param channel The channel which has been disconnected.
		 */
                @Override
		public void connectionDropped(Channel channel) {
			lastChannelEventTime = new Date();
		}
	}
}

