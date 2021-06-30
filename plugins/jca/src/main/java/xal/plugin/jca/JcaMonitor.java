/*
 * MonitorSrc.java
 *
 * Created on November 20, 2001, 10:52 AM
 */
package xal.plugin.jca;

import xal.ca.*;

import gov.aps.jca.CAException;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.dbr.*;

/**
 * Monitor implementation for JCA.
 *
 * @author Christopher K. Allen
 * @author Tom Pelaia
 * @version 1.0
 */
abstract class JcaMonitor extends Monitor implements gov.aps.jca.event.MonitorListener {

    protected int type;
    /**
     * PV object associated with channel
     */
    protected gov.aps.jca.Channel jcaChannel;
    /**
     * internal JCA monitor
     */
    protected gov.aps.jca.Monitor jcaMonitor;

    /**
     * Creates new Monitor
     *
     * @param chan Channel object to monitor
     * @param type The type of data being monitored
     * @param intMaskFire code specifying when monitor event is fired
     */
    protected JcaMonitor(final Channel chan, final int type, final int intMaskEvent) throws ConnectionException {
        super(chan, intMaskEvent);

        this.type = type;
        jcaChannel = ((JcaChannel) chan).jcaChannel;
        jcaMonitor = null;
    }

    /**
     * factory method to create a monitor which provides value, status and
     * timestamp
     */
    public static JcaMonitor newValueTimeMonitor(final JcaChannel chan, final IEventSinkValTime ifcSink, final int intMaskFire) throws ConnectionException, MonitorException {
        return JcaMonitorValTime.newMonitor(chan, ifcSink, intMaskFire);
    }

    /**
     * factory method to create a monitor which provides value and status
     */
    public static JcaMonitorValStatus newValueStatusMonitor(final JcaChannel chan, final IEventSinkValStatus ifcSink, final int intMaskFire) throws ConnectionException, MonitorException {
        return JcaMonitorValStatus.newMonitor(chan, ifcSink, intMaskFire);
    }

    /**
     * factory method to create a monitor which provides value
     */
    public static JcaMonitorValue newValueMonitor(final JcaChannel chan, final IEventSinkValue ifcSink, final int intMaskFire) throws ConnectionException, MonitorException {
        return JcaMonitorValue.newMonitor(chan, ifcSink, intMaskFire);
    }

    /**
     * Derived monitor objects must override this event hook. Derived class will
     * catch the jca.MonitorEvent, convert to appropriate data type, and forward
     * to the appropriate data sink interface (IEventSinkXxxXxx).
     */
    @Override
    public abstract void monitorChanged(MonitorEvent event);

    /**
     * Stop the monitoring of PV
     */
    @Override
    public void clear() {
        if (!bolMonitoring) {
            return;
        }

        try {
            jcaMonitor.clear();
        } catch (CAException exception) {
        }

        jcaMonitor = null;
        bolMonitoring = false;
    }

    /**
     * Start the channel monitoring
     *
     * @exception MonitorException unable to setup the channel access monitor
     */
    @Override
    protected void begin() throws MonitorException {

        try {
            final int count = jcaChannel.getElementCount();
            final DBRType dbrType = DBRType.forValue(type);
            jcaMonitor = jcaChannel.addMonitor(dbrType, count, intMaskEvent, this);
            jcaChannel.getContext().flushIO();
        } catch (CAException exception) {
            throw new MonitorException("Monitor::begin() - Incompatible types " + exception.getMessage());
        }

        bolMonitoring = true;
    }
}

/**
 * Class MonitorValTime Monitor a channel for any data type. The record returned
 * has value, status and time information.
 */
class JcaMonitorValTime extends JcaMonitor {

    /**
     * data sink for channel monitoring
     */
    private IEventSinkValTime ifcSink;

    // create a mew monitor
    protected JcaMonitorValTime(final Channel chan, final int type, final IEventSinkValTime ifcSink, final int intMaskFire) throws ConnectionException, MonitorException {
        super(chan, type, intMaskFire);
        this.ifcSink = ifcSink;

        super.begin();
    }

    // capture an event, wrap the dbr into a channel record and notify the sink
    @Override
    public void monitorChanged(final MonitorEvent evt) {
        final DBR dbr = evt.getDBR();
        if (dbr != null) {
            synchronized (dbr) {
                final TimeAdaptor adaptor = new DbrTimeAdaptor(dbr);
                postTimeRecord(ifcSink, adaptor);
            }
        }
    }

    // convenient way to create a new monitor
    public static JcaMonitorValTime newMonitor(final JcaChannel chan, final IEventSinkValTime ifcSink, final int intMaskFire) throws ConnectionException, MonitorException {
        try {
            int type = chan.getTimeType();
            return new JcaMonitorValTime(chan, type, ifcSink, intMaskFire);
        } catch (GetException exception) {
            throw new MonitorException("Error creating a new monitor: " + exception.getMessage());
        }
    }
}

/**
 * Class MonitorValStatus Monitor a channel for any data type. The record
 * returned has value, status and time information.
 */
class JcaMonitorValStatus extends JcaMonitor {

    /**
     * data sink for channel monitoring
     */
    private IEventSinkValStatus ifcSink;

    // create a mew monitor
    protected JcaMonitorValStatus(final Channel chan, final int type, final IEventSinkValStatus ifcSink, final int intMaskFire) throws ConnectionException, MonitorException {
        super(chan, type, intMaskFire);
        this.ifcSink = ifcSink;

        super.begin();
    }

    // capture an event, wrap the dbr into a channel record and notify the sink
    @Override
    public void monitorChanged(final MonitorEvent evt) {
        final DBR dbr = evt.getDBR();
        if (dbr != null) {
            synchronized (dbr) {
                final StatusAdaptor adaptor = new DbrStatusAdaptor(dbr);
                postStatusRecord(ifcSink, adaptor);
            }
        }
    }

    // convenient way to create a new monitor
    public static JcaMonitorValStatus newMonitor(final JcaChannel chan, final IEventSinkValStatus ifcSink, final int intMaskFire) throws ConnectionException, MonitorException {
        try {
            int type = chan.getTimeType();
            return new JcaMonitorValStatus(chan, type, ifcSink, intMaskFire);
        } catch (GetException exception) {
            throw new MonitorException("Error creating a new monitor: " + exception.getMessage());
        }
    }
}

/**
 * Class MonitorValTime Monitor a channel for any data type. The record returned
 * has value, status and time information.
 */
class JcaMonitorValue extends JcaMonitor {

    /**
     * data sink for channel monitoring
     */
    private final IEventSinkValue ifcSink;

    // create a mew monitor
    protected JcaMonitorValue(final Channel chan, final int type, final IEventSinkValue ifcSink, final int intMaskFire)
            throws ConnectionException, MonitorException {
        super(chan, type, intMaskFire);
        this.ifcSink = ifcSink;

        super.begin();
    }

    // capture an event, wrap the dbr into a channel record and notify the sink
    @Override
    public void monitorChanged(final MonitorEvent evt) {
        final DBR dbr = evt.getDBR();
        if (dbr != null) {
            synchronized (dbr) {
                final ValueAdaptor adaptor = new DbrValueAdaptor(dbr);
                postValueRecord(ifcSink, adaptor);
            }
        }
    }

    // convenient way to create a new monitor
    public static JcaMonitorValue newMonitor(final JcaChannel chan, final IEventSinkValue ifcSink, final int intMaskFire) throws ConnectionException, MonitorException {
        try {
            int type = chan.getTimeType();
            return new JcaMonitorValue(chan, type, ifcSink, intMaskFire);
        } catch (GetException exception) {
            throw new MonitorException("Error creating a new monitor: " + exception.getMessage());
        }
    }
}
