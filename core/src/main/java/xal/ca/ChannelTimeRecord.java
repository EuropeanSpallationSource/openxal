/*
 * ChannelTimeRecord.java
 *
 * Created on June 28, 2002, 3:03 PM
 */
package xal.ca;

/**
 * ChannelTimeRecord is a wrapper for channel data that has a value along with
 * status information and a time stamp.
 *
 * @author tap
 */
public interface ChannelTimeRecord extends ChannelStatusRecord {

    /**
     * Get the timestamp.
     *
     * @return the timestamp
     */
    public Timestamp getTimestamp();

    /**
     * Get the time stamp in seconds since the Java epoch epoch. Some precision
     * is lost as we move away from the epoch since the double precision number
     * cannot hold the full native precision.
     *
     * @return The time stamp in seconds as a double.
     */
    public double timeStampInSeconds();
}
