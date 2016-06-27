package xal.plugin.pvaccess;

import org.epics.pvdata.pv.PVStructure;

import xal.ca.Channel;
import xal.ca.IEventSinkValStatus;
import xal.ca.IEventSinkValTime;
import xal.ca.IEventSinkValue;

/**
 * Adapter for IEventSinkValue interface family.
 * As the interfaces are disjoint this is a way to create a common interface to all of them.
 * 
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 *
 */
public abstract class EventSinkAdapter {
    
    private final String defaultField;

    private EventSinkAdapter(String defaultField) {
        this.defaultField = defaultField;
    }

    /**
     * Create an adapter for IEventSinkValue listener.
     * @param listener Object that listens to events
     * @return Adapter for listener
     */
    public static EventSinkAdapter getAdapter(IEventSinkValue listener, String defaultFieldName) {
        return new EventSinkAdapterValue(listener, defaultFieldName);
    }

    /**
     * Create an adapter for IEventSinkValStatus listener.
     * @param listener Object that listens to events
     * @return Adapter for listener
     */
    public static EventSinkAdapter getAdapter(IEventSinkValStatus listener, String defaultFieldName) {
        return new EventSinkAdapterStatus(listener, defaultFieldName);
    }

    /**
     * Create an adapter for IEventSinkValTime listener.
     * @param listener Object that listens to events
     * @return Adapter for listener
     */
    public static EventSinkAdapter getAdapter(IEventSinkValTime listener, String defaultFieldName) {
        return new EventSinkAdapterTime(listener, defaultFieldName);
    }
    
    /**
     * Creates a PvAccessChannelRecord that contains data from the pvStructure.
     * @param pvStructure data from the channel
     * @return A PvAccessChannelRecord containing the data
     */
    PvAccessChannelRecord getRecord(PVStructure pvStructure) {
        return new PvAccessChannelRecord(new PvAccessDataAdapter(pvStructure, defaultField));
    }

    /**
     * A method that sends event to the adapted listener.
     * @param pvStructure Data to send to listener
     * @param c Channel from which the data was received
     */
    public abstract void eventValue(PVStructure pvStructure, Channel c);
    
    private static class EventSinkAdapterValue extends EventSinkAdapter {
        
        private IEventSinkValue listener;
        
        private EventSinkAdapterValue(IEventSinkValue listener, String defaultFieldName) {
            super(defaultFieldName);
            this.listener = listener;
        }

        @Override
        public void eventValue(PVStructure pvStructure, Channel c) {
            listener.eventValue(getRecord(pvStructure), c);
        }
        
    }

    private static class EventSinkAdapterStatus extends EventSinkAdapter {
        
        private IEventSinkValStatus listener;
        
        private EventSinkAdapterStatus(IEventSinkValStatus listener, String defaultFieldName) {
            super(defaultFieldName);
            this.listener = listener;
        }

        @Override
        public void eventValue(PVStructure pvStructure, Channel c) {
            listener.eventValue(getRecord(pvStructure), c);
        }
        
    }

    private static class EventSinkAdapterTime extends EventSinkAdapter {
        
        private IEventSinkValTime listener;
        
        private EventSinkAdapterTime(IEventSinkValTime listener, String defaultFieldName) {
            super(defaultFieldName);
            this.listener = listener;
        }

        @Override
        public void eventValue(PVStructure pvStructure, Channel c) {
            listener.eventValue(getRecord(pvStructure), c);
        }
        
    }
}
