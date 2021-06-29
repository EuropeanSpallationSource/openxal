//
// ChannelGroupRecord.java: Source file for 'ChannelGroupRecord'
// Project xal
//
// Created by t6p on 1/20/11
//

package xal.service.pvlogger;

import java.util.ArrayList;
import java.util.List;



/**
 * Represents the properties of a channel group which can be edited.
 * @author  tap
 */
public class ChannelGroupRecord {
	/** represented channel group */
	private final ChannelGroup channelGroup;
	
	/** default logging period (seconds) for the group */
	private double defaultLoggingPeriod;
	
	/** retention time in days (or zero for permanent retention) for snapshots associated with this group */
	private double retention;
	
	/** service ID */
	private String serviceID;
	
	/** description of the channel group */
	private String description;
	
	
	/** Constructor */
	public ChannelGroupRecord( final ChannelGroup group ) {
		channelGroup = group;
		revert();
	}
	
	
	/** convert the list of groups to a list of records */
	public static List<ChannelGroupRecord> toRecords( final List<ChannelGroup> groups ) {
		final List<ChannelGroupRecord> records = new ArrayList<>( groups.size() );
		for ( final ChannelGroup group : groups ) {
			records.add( new ChannelGroupRecord( group ) );
		}
		
		return records;
	}
	
	
	/** revert to the group settings */
	public void revert() {
		defaultLoggingPeriod = channelGroup.getDefaultLoggingPeriod();
		retention = channelGroup.getRetention();
		serviceID = channelGroup.getServiceID();
		description = channelGroup.getDescription();
	}
	
	
	/** get the represented channel group */
	public ChannelGroup getGroup() {
		return channelGroup;
	}
	
	
	/** get the service ID */
	public String getServiceID() {
		return serviceID;
	}
	
	
	/** set the service ID */
	public void setServiceID( final String serviceID ) {
		this.serviceID = serviceID;
	}
	
	
	/** get the description */
	public String getDescription() {
		return description;
	}
	
	
	/** set the description */
	public void setDescription( final String description ) {
		this.description = description;
	}
	
	
	/** get the label */
	public String getLabel() {
		return channelGroup.getLabel();
	}
	
	
	/** get the default logging period */
	public double getDefaultLoggingPeriod() {
		return defaultLoggingPeriod;
	}
	
	
	/** set the default logging period */
	public void setDefaultLoggingPeriod( final double period ) {
		defaultLoggingPeriod = period;
	}
	
	
	/** get the retention */
	public double getRetention() {
		return retention;
	}
	
	
	/** set the retention */
	public void setRetention( final double retention ) {
		this.retention = retention;
	}
}	
