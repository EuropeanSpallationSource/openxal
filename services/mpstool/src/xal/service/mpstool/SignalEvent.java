/*
 *  SignalEvent.java
 *
 *  Created on Tue Apr 13 14:41:04 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import xal.ca.Timestamp;

/**
 * SignalEvent holds one event for a signal.
 *
 * @author tap
 */
public class SignalEvent implements Comparable<Object> {

    /**
     * the event's timestamp
     */
    protected final Timestamp timestamp;

    /**
     * the PV name
     */
    protected final String signal;

    /**
     * Constructor
     *
     * @param signal The PV signal
     * @param timestamp The timestamp of the event
     */
    public SignalEvent(String signal, Timestamp timestamp) {
        this.timestamp = timestamp;
        this.signal = signal;
    }

    /**
     * Get the signal
     *
     * @return the signal
     */
    public String getSignal() {
        return signal;
    }

    /**
     * Get the timestamp of the signal event
     *
     * @return the timestamp of the signal event
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * compare timestamp of other SignalEvent instance with this one's timestamp
     *
     * @param other the SignalEvent instance against which to compare this one
     * @return -1 if this is earlier than the specified record or +1 if it is
     * later or the same
     */
    @Override
    public int compareTo(Object other) {
        Timestamp otherTimestamp = ((SignalEvent) other).timestamp;
        // Do not provide 0 val for the case when they are ==
        return (timestamp.compareTo(otherTimestamp) < 0) ? -1 : 1;
    }

    /**
     * Generate a string description of this instance.
     *
     * @return description of this instance
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("signal: ").append(signal);
        buffer.append(", timestamp: ").append(timestamp);

        return buffer.toString();
    }
}
