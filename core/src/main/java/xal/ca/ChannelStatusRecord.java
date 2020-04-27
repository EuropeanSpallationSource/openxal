/*
 * ChannelStatusRecord.java
 *
 * Created on June 28, 2002, 3:01 PM
 */
package xal.ca;

/**
 * ChannelStatusRecord is a wrapper for channel data that has a value along with
 * status information.
 *
 * @author tap
 */
public interface ChannelStatusRecord extends ChannelRecord {

    /**
     * Get the internal status code for this data.
     *
     * @return the status code for this data.
     */
    public int status();

    /**
     * Get the internal severity code for this data.
     *
     * @return the severity code for this data.
     */
    public int severity();

}
