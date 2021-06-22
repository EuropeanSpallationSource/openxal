/*
 *  MPSEvent.java
 *
 *  Created on Tue Apr 13 14:42:45 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import java.util.*;

import xal.ca.ChannelTimeRecord;
import xal.tools.correlator.Correlation;


/**
 * MPSEvent
 *
 * @author    tap
 */
public class MPSEvent {
	/** mean timestamp of the correlated signal events */
	protected final Date timestamp;

	/** sorted list of correlated signal events */
	protected final List<SignalEvent> signalEvents;


	/**
	 * Constructor
	 *
	 * @param correlation  The correlated MPS trips defining an MPS event.
	 */
	public MPSEvent( Correlation<ChannelTimeRecord> correlation ) {
		timestamp = correlation.meanDate();

		Collection<String> signals = correlation.names();
		signalEvents = new ArrayList<>();

        for(String signal : signals) {
            ChannelTimeRecord record = correlation.getRecord( signal );
            SignalEvent signalEvent = new SignalEvent( signal, record.getTimestamp() );
            signalEvents.add( signalEvent );
        }

		Collections.sort( signalEvents );
	}


	/**
	 * Get the mean timestamp of the correlated signal events
	 *
	 * @return   the mean timestamp of the correlated signal events
	 */
	public Date getTimestamp() {
		return timestamp;
	}


	/**
	 * Get the sorted list of correlated signal events
	 *
	 * @return   the list of correlated signal events
	 */
	public List<SignalEvent> getSignalEvents() {
		return signalEvents;
	}


	/**
	 * Get the first signal event (suspect as the likely cause of correlated MPS
	 * trips)
	 *
	 * @return   the first signal event
	 */
	public SignalEvent getFirstSignalEvent() {
		return signalEvents.get( 0 );
	}


	/**
	 * Generate a description of this instance.
	 *
	 * @return   description of this event
	 */
        @Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append("timestamp: ").append(timestamp);
		buffer.append("signal events: ").append(signalEvents.toString());

		return buffer.toString();
	}
}

