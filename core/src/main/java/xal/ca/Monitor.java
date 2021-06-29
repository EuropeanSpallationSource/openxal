/*
 * MonitorSrc.java
 *
 * Created on November 20, 2001, 10:52 AM
 */

package xal.ca;


/**
 * Monitor
 *
 * @author  Christopher K. Allen
 * @author Tom Pelaia
 * @version 1.0
 */
public abstract class Monitor {
    /** The monitor is triggered when the PV value change. */
    public static final int VALUE             = 1;
  
    /** The monitor is triggered when the PV log value change. */
    public static final int LOG               = 2;
  
    /** The monitor is triggered when the PV alarm state change. */
    public static final int ALARM             = 4;
    
    
    protected boolean             bolMonitoring;  // monitoring flag
    protected int                 intMaskEvent;   // event mask for firing monitor    
    protected Channel             xalChan;        // Channel to monitor
    
    
    /** 
     *  Creates new Monitor
     *  @param  chan        Channel object to monitor
     *  @param  intMaskEvent code specifying when monitor event is fired
     *
     *  @exception  ConnectionException     Channel is not connected
     */
    protected Monitor(Channel chan, int intMaskEvent) throws ConnectionException {
        bolMonitoring = false;
        this.intMaskEvent = intMaskEvent;        
        xalChan = chan;
    }

    
    /**
     *  Stop the monitoring of PV
     */
    public abstract void clear();
    
    
    /**
     *  Return the associated Channel object
     *  @return channel being monitored
     */
    public Channel getChannel() {
        return xalChan;
    }

    
    /**
     *  Start the channel monitoring
     *
     *  @exception  MonitorException    unable to setup the channel access monitor
     */
    protected abstract void begin() throws MonitorException;
    
    
    /**
     * Make sure monitoring is shut down before destruction
     * @throws Throwable upon failure
     */
    @Override
    protected void finalize() throws Throwable  {
		try {
			clear();
		}
		finally {
			super.finalize();
		}
    }
    
    
    /**
     * Post the value record to the listener.
     * @param listener The object receiving the monitor record.
     * @param adaptor The adaptor to the internal data record.
     */
    protected final void postValueRecord(IEventSinkValue listener, ValueAdaptor adaptor) {
        ChannelRecord record = new ChannelRecordImpl(adaptor);
        record.applyTransform( xalChan.getValueTransform() );
        listener.eventValue(record, xalChan);
    }
    
    
    /**
     * Post the value-status record to the listener.
     * @param listener The object receiving the monitor record.
     * @param adaptor The adaptor to the internal data record.
     */
    protected final void postStatusRecord(IEventSinkValStatus listener, StatusAdaptor adaptor) {
        ChannelStatusRecord record = new ChannelStatusRecordImpl(adaptor);
        record.applyTransform( xalChan.getValueTransform() );
        listener.eventValue(record, xalChan);
    }
    
    
    /**
     * Post the value-status-timestamp record to the listener.
     * @param listener The object receiving the monitor record.
     * @param adaptor The adaptor to the internal data record.
     */
    protected final void postTimeRecord(IEventSinkValTime listener, TimeAdaptor adaptor) {
        ChannelTimeRecord record = new ChannelTimeRecordImpl(adaptor);
        record.applyTransform( xalChan.getValueTransform() );
        listener.eventValue(record, xalChan);
    }
}

