/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

/**
 *
 * @author nataliamilas
 */
import java.util.*;

import xal.tools.text.FormattedNumber;
import xal.smf.*;
import xal.smf.AcceleratorNode;
import xal.tools.data.*;


/** record up to 3 channels at a beam marker */
public class BeamMarkerRecord<NodeType extends AcceleratorNode> {
	/** the Beam Marker */
	final protected BeamMarker<NodeType> BEAM_MARKER;
	
	/** the time when the data was taken */
	protected Date TIME_STAMP;
	
	/** the channel 1 value over the beam pulse at the marker */
	protected double channel1_VAL;
	
	/** the channel 2 value over the beam pulse at the marker */
	protected double channel2_VAL;
	
	/** the channel 3 value over the beam pulse at the marker */
	protected double channel3_VAL;
        
        /** the channel 1 handler name */
	protected String channel1_NAME;
	
	/** the channel 2 handler name */
	protected String channel2_NAME;
	
	/** the channel 3 handler name */
	protected String channel3_NAME;
	
	
	/**
	 * Primary constructor
	 * @param beamMarker    the node agent for which the record is made
	 * @param timestamp     the time of the node data
	 * @param channel1      value for channel 1
	 * @param channel2      value for channel 2
	 * @param channel3      value for channel 3
	 */
	public BeamMarkerRecord( final BeamMarker<NodeType> beamMarker, final Date timestamp, final String[] channel_name, final double[] channel_val ) {
		BEAM_MARKER = beamMarker;
		TIME_STAMP = timestamp;
		channel1_VAL = channel_val[0];
		channel1_VAL = channel_val[1];
		channel1_VAL = channel_val[2];
                channel1_NAME = channel_name[0];
		channel1_NAME = channel_name[1];
		channel1_NAME = channel_name[2];
	}
	
	
		
	/**
	 * Constructor
	 * @param record  another beam marker record
	 * @param timestamp  the time of the node data
	 * @param channel1      value for channel 1
	 * @param channel2      value for channel 2
	 */
	public BeamMarkerRecord( final BeamMarkerRecord<NodeType> record, final Date timestamp, final String[] channel_name, final double[] channel_val) {
		this( record.getBeamMarker(), timestamp, channel_name, channel_val );
	}
	
	
	/**
	 * Constructor using the current timestamp and zero for node data
	 * @param beamMarker  the node agent for which the record is made
	 */
	public BeamMarkerRecord( final BeamMarker<NodeType> beamMarker ) {
                this( beamMarker, new Date(), new String[]{"","",""}, new double[3]);
	}
	
	
	/**
	 * Calculate the difference between a primary node record and a reference node record.
	 * @param primaryRecord    The primary record
	 * @param referenceRecord  The reference record
	 * @return                 The difference between the primary record and reference record
	 */
	static public <ParamNodeType extends AcceleratorNode> BeamMarkerRecord<ParamNodeType> calcDifference( final BeamMarkerRecord<ParamNodeType> primaryRecord, final BeamMarkerRecord<ParamNodeType> referenceRecord ) {
		final BeamMarker<ParamNodeType> beamMarker = primaryRecord.getBeamMarker();
		final Date timestamp = primaryRecord.getTimestamp();
                final double[] channel_val = new double[3];
		channel_val[0] = primaryRecord.getChannel1Val()  - referenceRecord.getChannel1Val() ;
		channel_val[1] = primaryRecord.getChannel2Val()  - referenceRecord.getChannel2Val() ;
		channel_val[2] = primaryRecord.getChannel3Val()  - referenceRecord.getChannel3Val() ;
                
                final String[] channel_name = new String[3];
                channel_name[0] = primaryRecord.getChannel1Name();
                channel_name[0] = primaryRecord.getChannel2Name();
                channel_name[0] = primaryRecord.getChannel3Name();
		
		return new BeamMarkerRecord<ParamNodeType>( beamMarker, timestamp, channel_name, channel_val );
	}
	
	
	/**
	 * Get the Beam marker for which this record was generated
	 * @return   this record's beam marker
	 */
	public BeamMarker<NodeType> getBeamMarker() {
		return BEAM_MARKER;
	}
	
	
	/**
	 * Get the position of the node relative to the start of the specified sequence.
	 * @param sequence  The sequence relative to which the node's position is measured
	 * @return          the position of the node relative to the sequence in meters
	 */
	public double getPositionIn( final AcceleratorSeq sequence ) {
		return BEAM_MARKER.getPositionIn( sequence );
	}
	
	
	/**
	 * Get the unique identifier for the enclosed node
	 * @return   the ID of the record's node
	 */
	public String getNodeID() {
		return BEAM_MARKER.getID();
	}
	
	
	/**
	 * Get the timestamp of this record
	 * @return   the timestamp of this record
	 */
	public Date getTimestamp() {
		return TIME_STAMP;
	}
	
        /**
	 * Get the average value of X
	 * @return   the average value of the X field for the node
	 */
	public String getChannel1Name() {
		return channel1_NAME;
	}
	
	/**
	 * Get the average value of X
	 * @return   the average value of the X field for the node
	 */
	public double getChannel1Val() {
		return channel1_VAL;
	}
	
	
	/** Get X Average as a formatted number */
	public FormattedNumber getFormattedChannel1Val() {
		return new FormattedNumber( "0.0", getChannel1Val() );
	}
	
	/**
	 * Get the average value of X
	 * @return   the average value of the X field for the node
	 */
	public String getChannel2Name() {
		return channel2_NAME;
	}
        
	/**
	 * Get the average value of Y
	 * @return   the average value of the Y field for the node
	 */
	public double getChannel2Val() {
		return channel2_VAL;
	}
	
	
	/** Get X Average as a formatted number */
	public FormattedNumber getFormattedChannel2Val() {
		return new FormattedNumber( "0.0", getChannel2Val() );
	}
	
	/**
	 * Get the average value of X
	 * @return   the average value of the X field for the node
	 */
	public String getChannel3Name() {
		return channel3_NAME;
	}
        
	/**
	 * Get the average value of the node amplitude
	 * @return   the average value of the amplitude field for the node
	 */
	public double getChannel3Val() {
		return channel3_VAL;
	}
	
	
	/**
	 * Generate a string representation of this record
	 * @return   a string representation of this record
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( BEAM_MARKER.getID() );
		buffer.append( ", timestamp: " + TIME_STAMP );
		buffer.append( ", channel1_value: " + channel1_VAL );
		buffer.append( ", channel1_value: " + channel2_VAL );
		buffer.append( ", channel1_value: " + channel3_VAL );
		
		return buffer.toString();
	}	
}

