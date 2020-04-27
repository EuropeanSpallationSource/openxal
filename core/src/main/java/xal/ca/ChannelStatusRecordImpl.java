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
public class ChannelStatusRecordImpl extends ChannelRecordImpl implements ChannelStatusRecord {

    protected int status;
    protected int severity;

    /**
     * Creates new ChannelStatusRecord
     *
     * @param adaptor from which to create the record
     */
    public ChannelStatusRecordImpl(StatusAdaptor adaptor) {
        super(adaptor);
        status = adaptor.status();
        severity = adaptor.severity();
    }

    /**
     * Get the internal status code for this data.
     *
     * @return the status code for this data.
     */
    @Override
    public int status() {
        return status;
    }

    /**
     * Get the internal severity code for this data.
     *
     * @return the severity code for this data.
     */
    @Override
    public int severity() {
        return severity;
    }

    /**
     * Override the inherited method to return a description of this object.
     *
     * @return A description of this object.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(super.toString());
        buffer.append(", status: ").append(status);
        buffer.append(", severity: ").append(severity);

        return buffer.toString();
    }
}
