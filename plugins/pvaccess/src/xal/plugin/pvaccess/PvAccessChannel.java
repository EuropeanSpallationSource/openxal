package xal.plugin.pvaccess;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvaccess.client.ChannelPutRequester;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.util.logging.LoggingUtils;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

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

/**
 * Implementation of {@link Channel} class that uses pvAccess library.
 * pvAccess library can be used to connect with pvAccess or ChannelAccess protocol.
 * 
 * This implementation creates a connection to both protocols and only keeps the first 
 * one that is connected. This means that <b>if a pva channel and ca channel with same name
 * exist on the network the behavior of this class is non-deterministic</b>.
 * 
 * To keep compatibility with jca plugin, where the PV.FLD is a valid notation, the channel name
 * is parsed and everything after the first dot is used as a field (eg. \<pv\>.HIHI or 
 * \<pv\>.valueAlarm.highAlarmLimit). Only alarm fields are supported as they are the only ones used in apps.
 * 
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
class PvAccessChannel extends Channel {
    
    // Default timeout parameters
    private static final double DEFAULT_IO_TIMEOUT = 5.0;
    private static final double DEFAULT_EVENT_TIMEOUT = 0.1;
    
    // Names of the standard fields
    static final String VALUE_FIELD_NAME = "value";
    static final String ALARM_FIELD_NAME = "alarm";
    static final String ALARM_LIMIT_FIELD_NAME = "valueAlarm";
    static final String TIMESTAMP_FIELD_NAME = "timeStamp";
    static final String DISPLAY_FIELD_NAME = "display";
    static final String CONTROL_FIELD_NAME = "control";
    
    // Channel implementation that is used by this class
    private volatile org.epics.pvaccess.client.Channel channel;
    
    // Latch used to notify the connection to the channel
    private volatile CountDownLatch connectionLatch;
    
    // Lock for connection related stuff
    private final Object connectionLock = new Object();

    private final String defaultField;
    private final PVStructure pvRequest;

    private static final Logger LOGGER = Logger.getLogger(PvAccessChannel.class.getName());
    
    static {
        LOGGER.setLevel(Level.WARNING);
    }

    // Keep a reference to the context manager instance to ensure that it does not get
    // garbage collected while a channel still exists (TODO bad design)
    @SuppressWarnings("unused") 
    private final ProvidersManager manager = ProvidersManager.getInstance();
    
    /**
     * Constructor.
     * 
     * @param signalName Name of the PV
     */
    PvAccessChannel(String signalName) {
        super(parseChannelName(signalName));

        defaultField = parseDefaultFieldFromName(signalName);
        
        String requestString = defaultField.equals(VALUE_FIELD_NAME) ? "" : defaultField;
        pvRequest = CreateRequest.create().createRequest("field("+requestString+")");

        m_dblTmIO = DEFAULT_IO_TIMEOUT;
        m_dblTmEvt = DEFAULT_EVENT_TIMEOUT;
        connectionFlag = false;
    }
    
    private static String parseChannelName(String signalName) {
        int indexOfDot = signalName.indexOf(".");
        if (indexOfDot >= 0) {
            return signalName.substring(0, indexOfDot);
        }
        return signalName;
    }

    private static String parseDefaultFieldFromName(String signalName) {
        int indexOfDot = signalName.indexOf(".");
        if (indexOfDot >= 0) {
            return FieldNameConverter.getFieldName(signalName.substring(indexOfDot + 1));
        }
        return VALUE_FIELD_NAME;
    }

    /**
     * @return Logger object of PvAccessChannel.
     */
    static Logger getLogger() {
        return LOGGER;
    }
    
    /**
     * @return defaultField value
     */
    String getDefaultField() {
        return defaultField;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        if (channel != null && channel.getConnectionState() == ConnectionState.CONNECTED) {
            return true;
        }
        return false;
    };
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void requestConnection() {
        connectAndWait();
    }


    /**
     * Handle the channel connected event.
     */
    private void handleConnection(org.epics.pvaccess.client.Channel connectedChannel) {
        synchronized (connectionLock) {
            connectionLatch.countDown();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void checkConnection() throws ConnectionException {
        checkConnection(true);
    }

    /**
     * Check if the channel is connected.
     * The connection attempt procedure may be started if the channel is not yet connected.
     * @param attemptConnection If true the function will try to attempt connection to the channel if not connected
     * @throws ConnectionException Thrown if connection failed
     */
    private void checkConnection(boolean attemptConnection) throws ConnectionException {
        if ( !isConnected() ) {
            if ( attemptConnection ) {
                connectAndWait();
                checkConnection(false);
            }
            else {
                throw new ConnectionException( this, "The channel " + m_strId + " must be connected for this operation." );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean connectAndWait(double timeout) {
        if ( m_strId == null || isConnected()) { 
            return false;
        }

        reset();

        synchronized (connectionLock) {
            connectionLatch = new CountDownLatch(1);
        
            org.epics.pvaccess.client.Channel pvaChannel = ChannelProviderRegistryFactory.getChannelProviderRegistry().
                    createProvider(org.epics.pvaccess.ClientFactory.PROVIDER_NAME).createChannel(m_strId,
                            new PvAccessChannel.ChannelRequesterImpl(), ChannelProvider.PRIORITY_DEFAULT);
            channel = pvaChannel;
        }

        try {
            if (connectionLatch.await((long) timeout, TimeUnit.SECONDS)) {
                connectionFlag = true;
                if (connectionProxy != null) {
                    connectionProxy.connectionMade(PvAccessChannel.this);
                }
                return true;
            }
        } catch (InterruptedException e) {
            // This should not happen, but if it does the connection was not established
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        reset();
    }

    /**
     * Resets the channel to the unconnected state.
     */
    private void reset() {
        synchronized (connectionLock) {
            connectionFlag = false;

            if (channel != null) {
                channel.destroy();
                channel = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> elementType() throws ConnectionException {
        try {
            return getChannelRecord().getElementType();
        } catch (GetException e) {
            throw new ConnectionException(this, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int elementCount() throws ConnectionException {
        try {
            return getChannelRecord().getElementCount();
        } catch (GetException e) {
            throw new ConnectionException(this, e.getMessage());
        }
    }

    /**
     * PvAccess does not support access restrictions yet.
     */
    @Deprecated
    @Override
    public boolean readAccess() throws ConnectionException {
        return true;
    }

    /**
     * PvAccess does not support access restrictions yet.
     */
    @Deprecated
    @Override
    public boolean writeAccess() throws ConnectionException {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUnits() throws ConnectionException, GetException {
        return getChannelRecord().getUnits();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getOperationLimitPVs() {
        return constructLimitPVs( "LOPR", "HOPR" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getWarningLimitPVs() {
        return constructLimitPVs( "LOW", "HIGH" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getAlarmLimitPVs() {
        return constructLimitPVs( "LOLO", "HIHI" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDriveLimitPVs() {
        return constructLimitPVs( "DRVL", "DRVH" );
    }

    /** 
     * Construct the lower and upper limit PVs from the lower and upper suffixes
     * @return two element array of PVs with the lower and upper limit PVs 
     */
    private String[] constructLimitPVs( final String lowerSuffix, final String upperSuffix ) {
        final String[] rangePVs = new String[2];
        rangePVs[0] = channelName() + "." + lowerSuffix;
        rangePVs[1] = channelName() + "." + upperSuffix;
        return rangePVs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number rawUpperDisplayLimit() throws ConnectionException, GetException {
        return getChannelRecord().getUpperDisplayLimit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number rawLowerDisplayLimit() throws ConnectionException, GetException {
        return getChannelRecord().getLowerDisplayLimit();
    }

    /**
     * PvAccess default structure does not provide info on alarm limits.
     */
    @Override
    public Number rawUpperAlarmLimit() throws ConnectionException, GetException {
        return getChannelRecord().getUpperAlarmLimit();
    }

    /**
     * PvAccess default structure does not provide info on alarm limits.
     */
    @Deprecated
    @Override
    public Number rawLowerAlarmLimit() throws ConnectionException, GetException {
        return getChannelRecord().getLowerAlarmLimit();
    }

    /**
     * PvAccess default structure does not provide info on warning limits.
     */
    @Override
    public Number rawUpperWarningLimit() throws ConnectionException, GetException {
        return getChannelRecord().getUpperWarningLimit();
    }

    /**
     * PvAccess default structure does not provide info on warning limits.
     */
    @Deprecated
    @Override
    public Number rawLowerWarningLimit() throws ConnectionException, GetException {
        return getChannelRecord().getLowerWarningLimit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number rawUpperControlLimit() throws ConnectionException, GetException {
        return getChannelRecord().getUpperControlLimit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number rawLowerControlLimit() throws ConnectionException, GetException {
        return getChannelRecord().getLowerControlLimit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ChannelRecord getRawStringValueRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ChannelStatusRecord getRawStringStatusRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ChannelTimeRecord getRawStringTimeRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChannelStatusRecord getRawStatusRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChannelTimeRecord getRawTimeRecord() throws ConnectionException, GetException {
        return getChannelRecord();
    }

    /**
     * Synchronous get from the channel.
     * A timeout is used for limiting the connecting time.
     * @return Channel record that contains information on the channel state.
     * @throws ConnectionException Thrown when connection to the channel failed.
     * @throws GetException Thrown when get operation failed.
     */
    private PvAccessChannelRecord getChannelRecord() throws ConnectionException, GetException {
        CountDownLatch latch = new CountDownLatch(1);
        EventSinkValTimeWithLatch listener = new EventSinkValTimeWithLatch(latch);

        getRawValueTimeCallback(listener, true);
        try {
            if (latch.await((long) m_dblTmIO, TimeUnit.SECONDS)) {
                return listener.getRecord();
            }
        } catch (InterruptedException e) {
            throw new GetException("Concurrency error" + e.getMessage());
        }
        throw new GetException("Timeout on get operation. No data recieved in " + m_dblTmIO + " seconds.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void getRawValueCallback(IEventSinkValue listener) throws ConnectionException, GetException {
        getRawValueCallback(listener, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void getRawValueCallback(IEventSinkValue listener, boolean attemptConnection)
            throws ConnectionException, GetException {
        checkConnection(attemptConnection);

        ChannelGetRequester requester = new PvAccessChannel.ChannelGetRequesterImpl(
                EventSinkAdapter.getAdapter(listener, getDefaultField()));
        channel.createChannelGet(requester, pvRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getRawValueTimeCallback(IEventSinkValTime listener, boolean attemptConnection)
            throws ConnectionException, GetException {
        checkConnection(attemptConnection);

        ChannelGetRequester requester = new PvAccessChannel.ChannelGetRequesterImpl(
                EventSinkAdapter.getAdapter(listener, getDefaultField()));
        channel.createChannelGet(requester, pvRequest);
    }

    /**
     * {@inheritDoc}
     */
    private void putRawValCallback(final Object newVal, final String type, final PutListener listener) 
            throws ConnectionException, PutException {
        checkConnection();
        
        ChannelPutRequester requester = new PvAccessChannel.ChannelPutRequesterImpl(newVal, type, listener);
        channel.createChannelPut(requester, pvRequest);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void putRawValCallback(String newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(newVal, String.class.getName(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putRawValCallback(byte newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(newVal, byte.class.getName(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putRawValCallback(short newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(newVal, short.class.getName(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putRawValCallback(int newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(newVal, int.class.getName(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putRawValCallback(float newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(newVal, float.class.getName(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putRawValCallback(double newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(newVal, double.class.getName(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putRawValCallback(byte[] newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(newVal, byte[].class.getName(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putRawValCallback(short[] newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(newVal, short[].class.getName(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putRawValCallback(int[] newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(newVal, int[].class.getName(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putRawValCallback(float[] newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(newVal, float[].class.getName(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putRawValCallback(double[] newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(newVal, double[].class.getName(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Monitor addMonitorValTime(IEventSinkValTime listener, int intMaskFire)
            throws ConnectionException, MonitorException {
        return addMonitor(EventSinkAdapter.getAdapter(listener, getDefaultField()), intMaskFire);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Monitor addMonitorValStatus(IEventSinkValStatus listener, int intMaskFire)
            throws ConnectionException, MonitorException {
        return addMonitor(EventSinkAdapter.getAdapter(listener, getDefaultField()), intMaskFire);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Monitor addMonitorValue(IEventSinkValue listener, int intMaskFire)
            throws ConnectionException, MonitorException {
        return addMonitor(EventSinkAdapter.getAdapter(listener, getDefaultField()), intMaskFire);
    }
    
    private Monitor addMonitor(EventSinkAdapter listener, int intMaskFire) throws ConnectionException {
        MonitorRequester monitorRequester = new PvAccessMonitorRequesterImpl(listener, this, intMaskFire, defaultField);

        channel.createMonitor(monitorRequester, pvRequest);
        return (Monitor) monitorRequester;
    }
        
    /**
     * ChannelRequesterImplementation that sends the connection established events to the 
     * PvAccessChannel class on connection.
     */
    private class ChannelRequesterImpl implements ChannelRequester {
        
        /** Constructor */
        public ChannelRequesterImpl() {}

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
        public void message(String message, MessageType messageType) {
            PvAccessChannel.LOGGER.log(LoggingUtils.toLevel(messageType), message);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void channelCreated(Status status, org.epics.pvaccess.client.Channel connectedChannel) {
            PvAccessChannel.LOGGER.info("Channel '" + connectedChannel.getChannelName() + " created with status: " + status + ".");
        }

        /**
         * {@inheritDoc}
         * 
         * This implementation raises an event on the outer class, when the channel is connected.
         */
        @Override
        public void channelStateChange(org.epics.pvaccess.client.Channel connectedChannel, ConnectionState newState) {
            PvAccessChannel.LOGGER.info("State of channel " + connectedChannel.getChannelName() + "." + defaultField + " changed to " + newState.toString() + ".");

            if (newState == ConnectionState.CONNECTED) {
                PvAccessChannel.this.handleConnection(connectedChannel);
            }
        }
    }
    
    /**
     * ChannelGetRequester implementation.
     */
    private class ChannelGetRequesterImpl implements ChannelGetRequester {

        EventSinkAdapter listener;
        
        /**
         * Constructor
         * @param listener An object on which the events are raised
         */
        ChannelGetRequesterImpl(EventSinkAdapter listener) {
            this.listener = listener;
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
            PvAccessChannel.LOGGER.log(LoggingUtils.toLevel(type), message);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void channelGetConnect(Status status, ChannelGet channelGet, Structure structure) {
            if (status.isSuccess()) {
                channelGet.lastRequest();
                channelGet.get();
            }
            else
                PvAccessChannel.LOGGER.warning("Error while connecting channel " + PvAccessChannel.this.channelName() + " for get operation.");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void getDone(Status status, ChannelGet channelGet, PVStructure pvStructure, BitSet changedBitSet) {
            if (status.isSuccess()) {
                listener.eventValue(pvStructure, PvAccessChannel.this);
            } 
            else 
                PvAccessChannel.LOGGER.warning("Error while getting the value from " + PvAccessChannel.this.channelName() + ".");
        }
        
    }
    
    /**
     * ChannelPutRequester implementation.
     */
    private class ChannelPutRequesterImpl implements ChannelPutRequester {
        
        private final PutListener listener;
        private final Object value;
        private final String pvType;

        /**
         * Constructor for the ChannelPutRequester.
         * @param value Object to be put on the channel
         * @param type Name of the type of the object to put
         * @param listener Listener object on which events are raised when operation is completed
         */
        ChannelPutRequesterImpl(Object value, String type, PutListener listener) {
            this.value = value;
            this.pvType = type;
            this.listener = listener;
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
            PvAccessChannel.LOGGER.log(LoggingUtils.toLevel(type), message);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void channelPutConnect(Status status, ChannelPut channelPut, Structure structure) {
            channelPut.lastRequest();

            if (status.isSuccess()) {
                PVStructure pvStructure = PVDataFactory.getPVDataCreate().createPVStructure(structure);
                BitSet bitSet = new BitSet(pvStructure.getNumberFields());

                PVField val = pvStructure.getSubField(PvAccessChannel.this.getDefaultField());
                try {
                    PvAccessPutUtils.put(val, value, pvType);
                } catch (PutException e) {
                    PvAccessChannel.LOGGER.warning("Error on put to the " + PvAccessChannel.this.channelName() +
                            ": " + e.getMessage());
                    
                }
                bitSet.set(val.getFieldOffset());
                channelPut.put(pvStructure, bitSet);
            } else {
                PvAccessChannel.LOGGER.warning("Error while connecting channel " + PvAccessChannel.this.channelName() +
                        " for put operation.");
            }
            
        }

        /**
         * This method is not used in this implementation.
         */
        @Override
        public void getDone(Status status, ChannelPut channelPut, PVStructure pvStructure, BitSet bitSet) {
            // Not used.
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void putDone(Status status, ChannelPut channelPut) {
            if (status.isSuccess()) {
                listener.putCompleted(PvAccessChannel.this);
            } else {
                PvAccessChannel.LOGGER.warning("Error while puting a value to channel " + PvAccessChannel.this.channelName() + ".");
            }
        }
        
    }

    /**
     * Singleton class that holds the providers and destroys it on garbage collection.
     * TODO try to find a better design for this.
     */
    private static class ProvidersManager {

        private static class LazyHolder {
            private static final ProvidersManager INSTANCE = new ProvidersManager();
        }

        private ProvidersManager () {
            org.epics.pvaccess.ClientFactory.start();
        }

        static ProvidersManager getInstance() {
            return LazyHolder.INSTANCE;
        }
        
        @Override
        protected void finalize() throws Throwable {
            org.epics.pvaccess.ClientFactory.stop();
            super.finalize();
        }

    }

}
