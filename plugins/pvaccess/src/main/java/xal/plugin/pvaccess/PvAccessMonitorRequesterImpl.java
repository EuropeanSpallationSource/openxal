package xal.plugin.pvaccess;

import org.epics.pvaccess.util.logging.LoggingUtils;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.misc.BitSet;

import xal.ca.Channel;
import xal.ca.ConnectionException;
import xal.ca.Monitor;
import xal.ca.MonitorException;

/**
 * MonitorRequester implementation.
 *
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class PvAccessMonitorRequesterImpl extends Monitor implements MonitorRequester {

    private final String defaultField;
    private final EventSinkAdapter listener;
    private org.epics.pvdata.monitor.Monitor monitor;
    private boolean isFirst;

    public PvAccessMonitorRequesterImpl(EventSinkAdapter listener, Channel channel, int maskEvent, String defaultField) throws ConnectionException {
        super(channel, maskEvent);
        this.listener = listener;
        this.defaultField = defaultField;
        isFirst = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequesterName() {
        return getClass().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(String message, MessageType type) {
        PvAccessChannel.getLogger().log(LoggingUtils.toLevel(type), message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void monitorConnect(Status status, org.epics.pvdata.monitor.Monitor connectedMonitor, Structure structure) {
        monitor = connectedMonitor;
        connectedMonitor.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void monitorEvent(org.epics.pvdata.monitor.Monitor connectedMonitor) {
        MonitorElement element;
        while ((element = connectedMonitor.poll()) != null) {
            PVStructure pvs = element.getPVStructure();

            BitSet changed = element.getChangedBitSet();

            boolean isValueChanged = false;
            boolean isAlarmChanged = false;

            PVField valueField = pvs.getSubField(defaultField);
            if (valueField != null) {
                isValueChanged = changed.get(valueField.getFieldOffset());
            }
            PVField alarmField = pvs.getSubField(PvAccessChannel.ALARM_FIELD_NAME);
            if (alarmField != null) {
                isAlarmChanged = changed.get(alarmField.getFieldOffset());
            }

            if (isFirst
                    || ((m_intMaskEvent & VALUE) > 0 && isValueChanged)
                    || ((m_intMaskEvent & ALARM) > 0 && isAlarmChanged)) {
                if (isFirst) {
                    isFirst = false;
                }
                listener.eventValue(pvs, getChannel());
            }

            monitor.release(element);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unlisten(org.epics.pvdata.monitor.Monitor connectedMonitor) {
        connectedMonitor.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        if (monitor != null) {
            unlisten(monitor);
        }
    }

    /**
     * This method is not used as the monitor should begin automatically, based
     * on the current usage of the interface in OpenXal.
     *
     * @deprecated
     */
    @Deprecated
    @Override
    protected void begin() throws MonitorException {
    }

}
