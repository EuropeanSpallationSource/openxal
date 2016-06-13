package xal.plugin.pvaccess.server;

import org.epics.pvdata.copy.PVCopy;
import org.epics.pvdata.copy.PVCopyFactory;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVListener;
import org.epics.pvdatabase.PVRecord;
import org.epics.pvdatabase.PVRecordField;
import org.epics.pvdatabase.PVRecordStructure;
import org.epics.pvdatabase.pva.ContextLocal;

import xal.ca.Channel;
import xal.ca.ChannelRecord;
import xal.ca.ChannelStatusRecord;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.IEventSinkValStatus;
import xal.ca.IEventSinkValTime;
import xal.ca.IEventSinkValue;
import xal.ca.Monitor;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.ca.PutListener;
import xal.plugin.pvaccess.EventSinkAdapter;
import xal.plugin.pvaccess.PvAccessChannelRecord;
import xal.plugin.pvaccess.PvAccessDataAdaptor;
import xal.ca.IServerChannel;

/**
 * Server side channel implementation that uses pva protocol.
 * 
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
class PvAccessServerChannel extends Channel implements IServerChannel {

    /** size for array PVs */
   public static final int DEFAULT_ARRAY_SIZE = 1024;

   private static final PVDatabase master = PVDatabaseFactory.getMaster();
   
   private final PVRecord record;
   
   // Names of the standard fields
   static final String VALUE_FIELD_NAME = "value";
   static final String ALARM_FIELD_NAME = "alarm";
   static final String TIMESTAMP_FIELD_NAME = "timeStamp";
   static final String DISPLAY_FIELD_NAME = "display";
   static final String CONTROL_FIELD_NAME = "control";

   private final int size;

   // Keep a reference to the context manager instance to ensure that it does not get
   // garbage collected while a channel still exists (TODO design flaw)
   @SuppressWarnings("unused") 
   private final ContextManager contextManager;

   PvAccessServerChannel( final String signalName ) {
       super(signalName);
       contextManager = ContextManager.getInstance();

       if ( signalName.length() > 0 ) {
           size = signalName.matches(".*(TBT|A)") ? DEFAULT_ARRAY_SIZE : 1;
           record = RecordFactory.createRecord(signalName, size);
           connectionFlag = true;
           master.addRecord(record);
       } else { 
           throw new IllegalArgumentException("Cannot create a channel with an empty name");
       }
   }
   
   private PVStructure getPvStructure() {
       return record.getPVRecordStructure().getPVStructure();
   }

   @Override
   public boolean connectAndWait(double timeout) {
       // We are locally connected
       return isConnected();
   }

   @Override
   public void requestConnection() {
       if (connectionProxy != null) {
           connectionProxy.connectionMade(this);
       }
   }

   @Override
   public void disconnect() {
       // Nothing to do here
   }

   @Override
   public Class<?> elementType() throws ConnectionException {
       return new PvAccessDataAdaptor(getPvStructure()).getValueType();
   }

    @Override
    public int elementCount() throws ConnectionException {
        return size;
    }

    @Deprecated
    @Override
    public boolean readAccess() throws ConnectionException {
        // PvAccess protocol does not have RW restrictions
        return true;
    }

    @Deprecated
    @Override
    public boolean writeAccess() throws ConnectionException {
        // PvAccess protocol does not have RW restrictions
        return true;
    }

    @Override
    public String getUnits() {
        return new PvAccessDataAdaptor(getPvStructure()).getUnits();
    }
    
    @Override
    public void setUnits(String units) {
        PVString unitField = getPvStructure().getStructureField(DISPLAY_FIELD_NAME).getStringField("units");
        unitField.put(units);
    }

    @Override
    public String[] getOperationLimitPVs() {
        // TODO
        return new String[0];
    }

    @Deprecated
    @Override
    public String[] getWarningLimitPVs() {
        // TODO
        return new String[0];
    }

    @Deprecated
    @Override
    public String[] getAlarmLimitPVs() {
        // TODO
        return new String[0];
    }

    @Override
    public String[] getDriveLimitPVs() {
        // TODO
        return new String[0];
    }

    private String[] constructLimitPVs(final String lowerSuffix, final String upperSuffix) {
        final String[] rangePVs = new String[2];
        rangePVs[0] = channelName() + "." + lowerSuffix;
        rangePVs[1] = channelName() + "." + upperSuffix;
        return rangePVs;
    }

    @Override
    public Number rawUpperDisplayLimit() throws ConnectionException, GetException {
        return new PvAccessDataAdaptor(getPvStructure()).getUpperDisplayLimit();
    }

    @Override
    public Number rawLowerDisplayLimit() throws ConnectionException, GetException {
        return new PvAccessDataAdaptor(getPvStructure()).getLowerDisplayLimit();
    }

    @Deprecated
    @Override
    public Number rawUpperAlarmLimit() throws ConnectionException, GetException {
        return 0;
    }

    @Deprecated
    @Override
    public Number rawLowerAlarmLimit() throws ConnectionException, GetException {
        return 0;
    }

    @Deprecated
    @Override
    public Number rawUpperWarningLimit() throws ConnectionException, GetException {
        return 0;
    }

    @Deprecated
    @Override
    public Number rawLowerWarningLimit() throws ConnectionException, GetException {
        return 0;
    }

    @Override
    public Number rawUpperControlLimit() throws ConnectionException, GetException {
        return new PvAccessDataAdaptor(getPvStructure()).getUpperControlLimit();
    }

    @Override
    public Number rawLowerControlLimit() throws ConnectionException, GetException {
        return new PvAccessDataAdaptor(getPvStructure()).getLowerControlLimit();
    }

    @Override
    public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    protected ChannelRecord getRawStringValueRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    protected ChannelStatusRecord getRawStringStatusRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    protected ChannelTimeRecord getRawStringTimeRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    public ChannelStatusRecord getRawStatusRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    public ChannelTimeRecord getRawTimeRecord() {
        return new PvAccessChannelRecord(new PvAccessDataAdaptor(getPvStructure()));
    }

    @Override
    public void getRawValueCallback(IEventSinkValue listener) throws ConnectionException, GetException {
        listener.eventValue(getRawTimeRecord(), this);
    }

    @Override
    public void getRawValueCallback(IEventSinkValue listener, boolean attemptConnection) throws ConnectionException,
            GetException {
        listener.eventValue(getRawTimeRecord(), this);
    }

    @Override
    public void getRawValueTimeCallback(IEventSinkValTime listener, boolean attemptConnection)
            throws ConnectionException, GetException {        
        listener.eventValue(getRawTimeRecord(), this);
    }

    
    @Override
    public Monitor addMonitorValTime(final IEventSinkValTime listener, final int intMaskFire) throws ConnectionException,
            MonitorException {
        
        PVListener pvListener = new PVListenerImpl(EventSinkAdapter.getAdapter(listener));
        PVCopy pvCopy = PVCopyFactory.create(getPvStructure(), getPvStructure(), "field"); // TODO mask
        record.addListener(pvListener, pvCopy);
        return new Monitor(this , intMaskFire) {
            
            @Override
            public void clear() {
                record.removeListener(pvListener, pvCopy);
            }
            
            @Override
            protected void begin() throws MonitorException {
                // Do nothing
            }
        };
    }

    @Override
    public Monitor addMonitorValStatus(final IEventSinkValStatus listener, int intMaskFire) throws ConnectionException,
            MonitorException {

        PVListener pvListener = new PVListenerImpl(EventSinkAdapter.getAdapter(listener));
        
        PVCopy pvCopy = PVCopyFactory.create(getPvStructure(), getPvStructure(), "field"); // TODO mask
        record.addListener(pvListener, pvCopy);
        return new Monitor(this , intMaskFire) {
            
            @Override
            public void clear() {
                record.removeListener(pvListener, pvCopy);
            }
            
            @Override
            protected void begin() throws MonitorException {
                // Do nothing
            }
        };
    }

    @Override
    public Monitor addMonitorValue(final IEventSinkValue listener, int intMaskFire) throws ConnectionException,
            MonitorException {

        PVListener pvListener = new PVListenerImpl(EventSinkAdapter.getAdapter(listener));
        
        PVCopy pvCopy = PVCopyFactory.create(getPvStructure(), getPvStructure(), "field"); // TODO mask
        record.addListener(pvListener, pvCopy);
        return new Monitor(this , intMaskFire) {
            
            @Override
            public void clear() {
                record.removeListener(pvListener, pvCopy);
            }
            
            @Override
            protected void begin() throws MonitorException {
                // Do nothing
            }
        };
    }

    @Override
    public void putRawValCallback(String newVal, PutListener listener) throws ConnectionException, PutException {
        throw new PutException(m_strId + " channel only supports double data!");
    }

    @Override
    public void putRawValCallback(byte newVal, PutListener listener) throws ConnectionException, PutException {
        throw new PutException(m_strId + " channel only supports double data!");
    }

    @Override
    public void putRawValCallback(short newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback((double) newVal, listener);
    }

    @Override
    public void putRawValCallback(int newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback((double) newVal, listener);
    }

    @Override
    public void putRawValCallback(float newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback((double) newVal, listener);
    }

    @Override
    public void putRawValCallback(double newVal, PutListener listener) throws ConnectionException, PutException {
        if (size == 1) {
            getPvStructure().getDoubleField("value").put(newVal);
        } else {
            PVDoubleArray valueField = (PVDoubleArray) getPvStructure().getScalarArrayField("value", ScalarType.pvDouble);
            valueField.put(0, 1, new double[]{newVal}, 0);
        }
    }

    @Override
    public void putRawValCallback(byte[] newVal, PutListener listener) throws ConnectionException, PutException {
        throw new PutException(m_strId + " channel only supports double data.");
    }

    @Override
    public void putRawValCallback(short[] newVal, PutListener listener) throws ConnectionException, PutException {
        double[] arr = new double[newVal.length];
        for (int i = 0; i < newVal.length; i++) {
            arr[i] = (double)newVal[i];
        }
        putRawValCallback(arr, listener);
    }

    @Override
    public void putRawValCallback(int[] newVal, PutListener listener) throws ConnectionException, PutException {
        double[] arr = new double[newVal.length];
        for (int i = 0; i < newVal.length; i++) {
            arr[i] = (double)newVal[i];
        }
        putRawValCallback(arr, listener);
    }

    @Override
    public void putRawValCallback(float[] newVal, PutListener listener) throws ConnectionException, PutException {
        double[] arr = new double[newVal.length];
        for (int i = 0; i < newVal.length; i++) {
            arr[i] = (double)newVal[i];
        }
        putRawValCallback(arr, listener);
    }

    @Override
    public void putRawValCallback(double[] newVal, PutListener listener) throws ConnectionException, PutException {
        if (size < newVal.length) {
            throw new PutException("The provided array for put to " + m_strId + " is too big to fit to the record.");
        }

        if (size == 1) {
            getPvStructure().getDoubleField("value").put(newVal[0]);
        } else {
            PVDoubleArray valueField = (PVDoubleArray) getPvStructure().getScalarArrayField("value", ScalarType.pvDouble);
            valueField.put(0, newVal.length, newVal, 0);
        }
    }
    
    @Deprecated
    @Override
    public void setLowerAlarmLimit(Number lowerAlarmLimit) {
        // Unsupported
    }

    @Override
    public void setLowerCtrlLimit(Number lowerCtrlLimit) {
        getPvStructure().getStructureField(CONTROL_FIELD_NAME).getDoubleField("limitLow").put((double) lowerCtrlLimit);
    }

    @Override
    public void setLowerDispLimit(Number lowerDispLimit) {
        getPvStructure().getStructureField(DISPLAY_FIELD_NAME).getDoubleField("limitLow").put((double) lowerDispLimit);
    }

    @Deprecated
    @Override
    public void setLowerWarningLimit(Number lowerWarningLimit) {
        // Unsupported
    }

    @Deprecated
    @Override
    public void setUpperAlarmLimit(Number upperAlarmLimit) {
        // Unsupported
    }

    @Override
    public void setUpperCtrlLimit(Number upperCtrlLimit) {
        getPvStructure().getStructureField(CONTROL_FIELD_NAME).getDoubleField("limitHigh").put((double) upperCtrlLimit);
    }

    @Override
    public void setUpperDispLimit(Number upperDispLimit) { 
        getPvStructure().getStructureField(DISPLAY_FIELD_NAME).getDoubleField("limitHigh").put((double) upperDispLimit);
    }

    @Deprecated
    @Override
    public void setUpperWarningLimit(Number upperWarningLimit) {
        // Unsupported
    }    
    
    @Deprecated
    @Override
    public void setSettable(boolean settable) {
        // PvAccess protocol does not have RW restrictions
    }
    
    private static class RecordFactory {

        private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        private static final StandardField standardField = StandardFieldFactory.getStandardField();
        private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

        private RecordFactory() {}

        private static PVStructure createArrayPvStructure(int size) {
            Structure top = fieldCreate.createFieldBuilder().
                    addArray("value", ScalarType.pvDouble).
                    add("alarm", standardField.alarm()).
                    add("timeStamp", standardField.timeStamp()).
                    add("display", standardField.display()).
                    add("control", standardField.control()).
                    createStructure();
            PVStructure pvStructure = pvDataCreate.createPVStructure(top);
            pvStructure.getScalarArrayField("value", ScalarType.pvDouble).setCapacity(size);
            return pvStructure;
        }

        private static PVStructure createScalarPvStructure() {
            Structure top = fieldCreate.createFieldBuilder().
                    add("value", ScalarType.pvDouble).
                    add("alarm", standardField.alarm()).
                    add("timeStamp", standardField.timeStamp()).
                    add("display", standardField.display()).
                    add("control", standardField.control()).
                    createStructure();
            return pvDataCreate.createPVStructure(top);
        }

        private static PVStructure createPvStructure(int size) {
            return size == 1 ? createScalarPvStructure() : createArrayPvStructure(size);
        }


        static PVRecord createRecord(String signalName, int size) {
            PVStructure pvStructure = createPvStructure(size);
            return new PVRecord(signalName, pvStructure);
        }

    }
    
    private class PVListenerImpl implements PVListener {
        
        private EventSinkAdapter listener;
        
        public PVListenerImpl(EventSinkAdapter listener) {
            this.listener = listener;
        }
        
        @Override
        public void unlisten(PVRecord pvRecord) {
            // Do nothing
        }
            
        @Override
        public void endGroupPut(PVRecord pvRecord) {
            // Do nothing
        }
            
        @Override
        public void dataPut(PVRecordStructure requested, PVRecordField pvRecordField) {
            // Do nothing
        }
            
        @Override
        public void dataPut(PVRecordField pvRecordField) {
            listener.eventValue(getPvStructure(), PvAccessServerChannel.this);
        }
            
        @Override
        public void beginGroupPut(PVRecord pvRecord) {
            // Do nothing
        }
    }

    /**
     * Singleton class that holds the context and destroys it on garbage collection.
     * TODO try to find a better design for this.
     */
    private static class ContextManager {

        private static class LazyContextHolder {
            private static final ContextManager INSTANCE = new ContextManager();
        }

        private static final ContextLocal context = new ContextLocal();

        private ContextManager () {
            context.start(false);
        }

        static ContextManager getInstance() {
            return LazyContextHolder.INSTANCE;
        }
        
        @Override
        protected void finalize() throws Throwable {
            context.destroy();
            super.finalize();
        }

    }

}
