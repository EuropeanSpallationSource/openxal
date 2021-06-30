/*
 * JcaChannel.java
 *
 * Created on August 26, 2002, 10:02 AM
 */
package xal.plugin.jca;

import xal.ca.*;

import gov.aps.jca.CAException;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.Context;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.*;

import java.util.logging.*;
import xal.tools.apputils.Preferences;

/**
 * Objectizes the Java Channel Access (jca) library by Boucher. In particular,
 * the jca.PV object and static jca.Ca are encapsulated. The the jca.PV and
 * jca.Ca operations are collected and exposed as necessary to perform
 * rudimentary process variable puts, gets, and monitors. The user may request a
 * reference to the associated PV object to perform more complicated operations
 * as appropriate.
 *
 * @author Christopher K. Allen
 * @author Tom Pelaia
 * @version 1.1
 */
class JcaChannel extends Channel {

    private static final Logger LOGGER = Logger.getLogger(JcaChannel.class.getName());

    //  Global Variables
    /**
     * channel access initialized
     */
    protected static boolean CA_INIT;
    /**
     * channel access library lock
     */
    protected static boolean CA_LOCK;
    /**
     * channel instance reference count
     */
    protected static long CNT_REF;
    /**
     * Forte debug mode (do not initialize jca)
     */
    protected static boolean DEBUG;

    //  Constants
    /**
     * default pend IO timeout
     */
    public static final double c_dblDefTimeIO = 5.0;
    /**
     * default pend event timeout
     */
    public static final double c_dblDefTimeEvent = 0.1;

    // Property names
    private static final String DEF_TIME_IO = "c_dblDefTimeIO";
    private static final String DEF_TIME_EVENT = "c_dblDefTimeEvent";

    //  Database Request (DBR) Data Types
    public static final int STRING;
    public static final int SHORT;
    public static final int FLOAT;
    public static final int ENUM;
    public static final int BYTE;
    public static final int INT;
    public static final int DOUBLE;

    //  Local Attributes
    /**
     * JCA Channel
     */
    protected gov.aps.jca.Channel jcaChannel;

    /**
     * cache of native JCA channels JCA won't allow us to connect to more than
     * one channel for the same PV signal.
     */
    protected JcaNativeChannelCache jcaNativeChannelCache;

    /**
     * JCA Context
     */
    protected Context jcaContext;

    /**
     * indicates whether this channel ever initialized CA
     */
    protected boolean hasInitializedCa;

    /**
     * connection lock for wait and notify actions
     */
    protected Object connectionLock;

    /**
     * Class loader initialization - Set channel access initialization flag
     */
    static {
        CA_INIT = false;
        CA_LOCK = false;
        CNT_REF = 0;

        STRING = DBRType.STRING.getValue();
        SHORT = DBRType.SHORT.getValue();
        FLOAT = DBRType.FLOAT.getValue();
        ENUM = DBRType.ENUM.getValue();
        BYTE = DBRType.BYTE.getValue();
        INT = DBRType.INT.getValue();
        DOUBLE = DBRType.DOUBLE.getValue();
    }

    /**
     * JcaChannel empty constructor.
     */
    JcaChannel() {
        this(null, null, null);
    }

    /**
     * Constructor.
     *
     * @param nativeChannelCache a cache of native JCA channels
     * @param signalName EPICS PV name
     * @param jcaContext the JCA Context within which to create the channel
     */
    JcaChannel(final String signalName, final Context jcaContext, final JcaNativeChannelCache jcaNativeChannelCache) {
        super(signalName);

        // since we only load and initialize Channel Access on demand
        hasInitializedCa = false;
        this.jcaNativeChannelCache = jcaNativeChannelCache;
        this.jcaContext = jcaContext;
        jcaChannel = null;
        connectionLock = new Object();

        // Load default timeouts from preferences if available, otherwise use hardcoded values.
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(Channel.class);
        dblTmIO = defaults.getDouble(DEF_TIME_IO, c_dblDefTimeIO);
        dblTmEvt = defaults.getDouble(DEF_TIME_EVENT, c_dblDefTimeEvent);
    }

    /**
     * Channel access library lock. Locks CA library into memory once it is
     * initialized. CA library will not release until Java virtual machine
     * terminates.
     */
    public static synchronized void caLock() {
    }

    /**
     * Unlock the Channel Access library. CA library may be released if there
     * are no Channel instances in the Java virtual machine.
     */
    public static synchronized void caUnlock() {
    }

    /**
     * Set Forte debug mode. The jca shared library (jca.dll on Windows) is
     * normally loaded whenever the first Channel object is instantiated. This
     * is done with a call to jca.Ca.init() with the Channel.caAddRef() method.
     * Loading the jca shared library seems to confuse the Forte debugger. Use
     * this method to set the Channel Forte debug mode to true. When in debug
     * mode the jca shared library is never loaded. Thus, Channel objects may be
     * instatiated, but they cannot be used to connect to EPICS channel access.
     *
     * @param bDebug debug flag (on or off)
     */
    public static synchronized void setDebugMode(boolean bDebug) {
        // turning off debug mode
        if (DEBUG == true && bDebug == false) {
            // must check if any channels were instantiated
            if (CNT_REF > 0) {
                // initialize CA if so
                CA_INIT = true;
            }
        }

        DEBUG = bDebug;
    }

    /**
     * Check if EPICS Channel Access library has been initialized and if not, do
     * so.
     */
    private static synchronized void caAddRef() {
        // Check if Channel Access needs to be initialized
        if (!CA_INIT) {
            if (!DEBUG) {
                CA_INIT = true;
            }
        }

        CNT_REF++;
    }

    /**
     * Check if EPICS Channel Access library is still needed and if not, release
     * it.
     */
    private static synchronized void caRelease() {
        // Error check 
        if (CNT_REF == 0) {
            // inadvertant (unbalanced) call
            return;
        }
        // Check if channel access library is still needed and if not, release it
        CNT_REF--;
        if (CNT_REF > 0) {
            // still active channels
            return;
        }
        if (CA_LOCK) {
            // CA library is locked into memory
            return;
        }
        // CA library is in memory and there are no more active channels
        if (CA_INIT == true) {
            CA_INIT = false;
        }
    }

    /**
     * Check if Channel Access library can be released
     */
    @Override
    protected void finalize() throws Throwable {
        if (hasInitializedCa) {
            caRelease();
        }
        super.finalize();
    }

    /**
     * Set the channel access Pend IO timeout
     *
     * @param dblTm I/O timeout
     */
    @Override
    public void setIoTimeout(double dblTm) {
        dblTmIO = dblTm;
    }

    /**
     * Set the channel access Pend Event timeout
     *
     * @param dblTm event timeout
     */
    @Override
    public void setEventTimeout(double dblTm) {
        dblTmEvt = dblTm;
    }

    /**
     * Get the channel access Pend IO timeout
     *
     * @return I/O timeout
     */
    @Override
    public double getIoTimeout() {
        return dblTmIO;
    }

    /**
     * Get the channel access Pend Event timeout
     *
     * @return event timeout
     */
    @Override
    public double getEventTimeout() {
        return dblTmEvt;
    }

    /**
     * Initialize channel access and increment instance count
     */
    protected void initChannelAccess() {
        if (!hasInitializedCa) {
            // Increment Channel instance counter
            caAddRef();
            hasInitializedCa = true;
        }
    }

    /*
     *  Channel Access Connection
     */
    /**
     * Notify connection listeners that connection has changed
     */
    private ConnectionListener newConnectionListener() {
        return new ConnectionListener() {
            @Override
            public void connectionChanged(final ConnectionEvent event) {
                // make sure we don't post a connection event until the channel has been assigned
                synchronized (connectionLock) {
                    if (event.isConnected()) {
                        processConnectionEvent();
                    } else {
                        connectionFlag = false;
                        if (connectionProxy != null) {
                            connectionProxy.connectionDropped(JcaChannel.this);
                        }
                    }
                }
            }
        };
    }

    /**
     * Process a connection event
     */
    private void processConnectionEvent() {
        connectionFlag = true;
        proceedFromConnection();
        if (connectionProxy != null) {
            connectionProxy.connectionMade(this);
        }
    }

    /**
     * Request a new connection and wait for it no longer than the timeout.
     *
     * @param timeout seconds to wait for a connection before giving up
     * @return true if the connection was made within the timeout and false if
     * not
     */
    @Override
    public boolean connectAndWait(final double timeout) {
        if (strId == null) {
            // check whether this channel's name has been specified
            return false;
        }
        requestConnection();
        flushIO();
        if (this.isConnected()) {
            // check if we have a connection
            return true;
        }
        pendIO(timeout);
        return isConnected();
    }

    /**
     * Request that the channel be connected. Connections are made in the
     * background so this method returns immediately upon making the request.
     * The connection will be made in the future as soon as possible. A
     * connection event will be sent to registered connection listeners when the
     * connection has been established.
     */
    @Override
    public void requestConnection() {
        if (strId == null || isConnected()) {
            // determine if there is any point in attempting a connection
            return;
        }
        // initialize channel access if necessary and increment instance counter
        initChannelAccess();

        // Make connection PV 
        if (jcaChannel == null) {
            try {
                // make sure we don't post a connection event until the channel has been assigned
                synchronized (connectionLock) {
                    jcaChannel = jcaNativeChannelCache.getChannel(strId);
                    jcaChannel.addConnectionListener(newConnectionListener());
                    if (jcaChannel.getConnectionState() == gov.aps.jca.Channel.CONNECTED) {
                        processConnectionEvent();
                    }
                }
            } catch (CAException exception) {
                final String message = "Error attempting to connect to: " + strId;
                LOGGER.log(Level.SEVERE, message, exception);
            }
        }
    }

    /**
     * Attempt to connect only if this channel has never been connected in the
     * past.
     */
    private void connectIfNeverConnected() {
        if (!hasEverBeenConnected()) {
            connectAndWait();
        }
    }

    /**
     * Wait until a connection is made or the attempt to connect has timed out.
     *
     * @param timeout seconds to wait for the connection before giving up
     */
    synchronized private void waitForConnection(final double timeout) {
        if (connectionFlag) {
            // no need to wait
            return;
        }
        try {
            synchronized (connectionLock) {
                connectionLock.wait((long) (1000 * timeout));
            }
        } catch (InterruptedException exception) {
            LOGGER.log(Level.SEVERE, "Error waiting for connection to: " + strId, exception);
            LOGGER.log(Level.SEVERE, null, exception);
        }
    }

    /**
     * Proceed forward since the connection has been made.
     */
    private void proceedFromConnection() {
        synchronized (connectionLock) {
            connectionLock.notify();
        }
    }

    /**
     * Terminate the network channel connection and clear all events associated
     * with process variable
     */
    @Override
    public void disconnect() {
        try {
            if (!isConnected()) {
                return;
            }

            try {
                jcaChannel.destroy();
            } catch (CAException exception) {
                LOGGER.log(Level.SEVERE, "Error disconnecting: " + strId, exception);
            }
        } finally {
            jcaChannel = null;
        }
    }

    /**
     * Determine if the channel has ever been connected regardless of its
     * present connection state.
     *
     * @return true if the channel has ever been connected and false if not.
     */
    private boolean hasEverBeenConnected() {
        final gov.aps.jca.Channel.ConnectionState state = jcaChannel.getConnectionState();
        return jcaChannel != null && (state == gov.aps.jca.Channel.CONNECTED || state == gov.aps.jca.Channel.DISCONNECTED);
    }

    /**
     * Checks if this channel has ever been connected.
     *
     * @param strFuncName name of function using connection
     */
    private void checkIfEverConnected(final String methodName) throws ConnectionException {
        if (!hasEverBeenConnected()) {
            throw new ConnectionException(this, "Channel::" + methodName + " - The channel \"" + strId + "\" must be connected at least once in the past to use this feature.");
        }
    }

    /**
     * Get state of current process variable connection
     *
     * @return EPICS channel access state code
     */
    public int state() throws ConnectionException {
        checkIfEverConnected("state()");

        return jcaChannel.getConnectionState().getValue();
    }

    /**
     * get the Java class associated with the native type of this channel
     */
    @Override
    public Class<?> elementType() throws ConnectionException {
        checkIfEverConnected("elementType()");

        return DbrValueAdaptor.elementType(getJcaType());
    }

    /**
     * Return native type of process variable associated with channel
     *
     * @return jca.DBR type code of process variable
     */
    public int nativeType() throws ConnectionException {
        checkIfEverConnected("nativeType()");

        // Get the type code
        return jcaChannel.getFieldType().getValue();
    }

    /**
     * Get the JCA field type of this channel
     *
     * @return the field type of the JCA channel
     */
    private DBRType getJcaType() throws ConnectionException {
        checkIfEverConnected("getJcaType()");

        return jcaChannel.getFieldType();
    }

    /**
     * Return size of value array associated with process variable
     *
     * @return number of values in process variable
     */
    @Override
    public int elementCount() throws ConnectionException {
        checkIfEverConnected("elementCount()");

        // Get the element count
        return jcaChannel.getElementCount();
    }

    /**
     * Determine if channel has read access to process variable
     *
     * @return true if channel has read access
     *
     * @exception ConnectionException channel not connected
     */
    @Override
    public boolean readAccess() throws ConnectionException {
        checkIfEverConnected("readAccess()");

        // Get read access
        return jcaChannel.getReadAccess();
    }

    /**
     * Determine if channel has write access to process variable
     *
     * @return true if channel has write access
     *
     * @exception ConnectionException channel not connected
     */
    @Override
    public boolean writeAccess() throws ConnectionException {
        checkIfEverConnected("writeAccess()");

        // Get write access
        return jcaChannel.getWriteAccess();
    }

    /**
     * Get the IOC host name which supports the process variable
     *
     * @return string containing network name of host
     */
    public String hostName() throws ConnectionException {
        this.checkConnection("hostName()");

        // Get the host name
        return jcaChannel.getHostName();
    }

    /**
     * Get the native value-status DBR type of this channel.
     *
     * @return The native DBR type of this channel.
     */
    protected int getStatusType() throws ConnectionException, GetException {
        connectIfNeverConnected();

        final DBRType nativeType = getJcaType();

        if (nativeType.isBYTE()) {
            return DBRType.STS_BYTE.getValue();
        } else if (nativeType.isENUM()) {
            return DBRType.STS_ENUM.getValue();
        } else if (nativeType.isSHORT()) {
            return DBRType.STS_SHORT.getValue();
        } else if (nativeType.isINT()) {
            return DBRType.STS_INT.getValue();
        } else if (nativeType.isFLOAT()) {
            return DBRType.STS_FLOAT.getValue();
        } else if (nativeType.isDOUBLE()) {
            return DBRType.STS_DOUBLE.getValue();
        } else if (nativeType.isSTRING()) {
            return DBRType.STS_STRING.getValue();
        } else {
            throw new GetException("No status type for type code: " + nativeType + " for pv: " + strId);
        }
    }

    /**
     * Get the native DBR value-status-timestamp type of this channel.
     *
     * @return The native DBR type of this channel.
     */
    protected int getTimeType() throws ConnectionException, GetException {
        final DBRType dbrType = getTimeDBRType();
        return dbrType.getValue();
    }

    /**
     * Get the native time DBR Type
     */
    private DBRType getTimeDBRType() throws ConnectionException, GetException {
        connectIfNeverConnected();

        final DBRType nativeType = getJcaType();

        if (nativeType.isBYTE()) {
            return DBRType.TIME_BYTE;
        } else if (nativeType.isENUM()) {
            return DBRType.TIME_ENUM;
        } else if (nativeType.isSHORT()) {
            return DBRType.TIME_SHORT;
        } else if (nativeType.isINT()) {
            return DBRType.TIME_INT;
        } else if (nativeType.isFLOAT()) {
            return DBRType.TIME_FLOAT;
        } else if (nativeType.isDOUBLE()) {
            return DBRType.TIME_DOUBLE;
        } else if (nativeType.isSTRING()) {
            return DBRType.TIME_STRING;
        } else {
            throw new GetException("No time type for type code: " + nativeType + " for pv: " + strId);
        }
    }

    /**
     * Make a new DBR for the native type of this channel.
     *
     * @return a native DBR for this channel.
     */
    DBR makeValueDBR() throws ConnectionException {
        int fieldType = nativeType();
        int elementCount = elementCount();

        // create a monitor of the correct type
        if (fieldType == DOUBLE) {
            return new DBR_Double(elementCount);
        } else if (fieldType == FLOAT) {
            return new DBR_Float(elementCount);
        } else if (fieldType == INT) {
            return new DBR_Int(elementCount);
        } else if (fieldType == SHORT) {
            return new DBR_Short(elementCount);
        } else if (fieldType == ENUM) {
            return new DBR_Enum(elementCount);
        } else if (fieldType == BYTE) {
            return new DBR_Byte(elementCount);
        } else if (fieldType == STRING) {
            return new DBR_String(elementCount);
        } else {
            return null;
        }
    }

    /**
     * Make a new value/status DBR for the native type of this channel.
     *
     * @return a native DBR for this channel.
     */
    DBR makeStatusDBR() throws ConnectionException {
        int fieldType = nativeType();
        int elementCount = elementCount();

        // create a monitor of the correct type
        if (fieldType == DOUBLE) {
            return new DBR_STS_Double(elementCount);
        } else if (fieldType == FLOAT) {
            return new DBR_STS_Float(elementCount);
        } else if (fieldType == INT) {
            return new DBR_STS_Int(elementCount);
        } else if (fieldType == SHORT) {
            return new DBR_STS_Short(elementCount);
        } else if (fieldType == ENUM) {
            return new DBR_STS_Enum(elementCount);
        } else if (fieldType == BYTE) {
            return new DBR_STS_Byte(elementCount);
        } else if (fieldType == STRING) {
            return new DBR_STS_String(elementCount);
        } else {
            return null;
        }
    }

    /**
     * Make a new value/status/timestamp DBR for the native type of this
     * channel.
     *
     * @return a native DBR for this channel.
     */
    DBR makeTimeDBR() throws ConnectionException {
        int fieldType = nativeType();
        int elementCount = elementCount();

        // create a monitor of the correct type
        if (fieldType == DOUBLE) {
            return new DBR_TIME_Double(elementCount);
        } else if (fieldType == FLOAT) {
            return new DBR_TIME_Float(elementCount);
        } else if (fieldType == INT) {
            return new DBR_TIME_Int(elementCount);
        } else if (fieldType == SHORT) {
            return new DBR_TIME_Short(elementCount);
        } else if (fieldType == ENUM) {
            return new DBR_TIME_Enum(elementCount);
        } else if (fieldType == BYTE) {
            return new DBR_TIME_Byte(elementCount);
        } else if (fieldType == STRING) {
            return new DBR_TIME_String(elementCount);
        } else {
            return null;
        }
    }

    /**
     * Convenience method which returns the units for this channel.
     */
    @Override
    public String getUnits() throws ConnectionException, GetException {
        return getCtrlInfo().getUnits();
    }

    /**
     * Get the lower and upper operation limit PVs
     *
     * @return two element array of PVs with the lower and upper limit PVs
     */
    @Override
    public String[] getOperationLimitPVs() {
        return constructLimitPVs("LOPR", "HOPR");
    }

    /**
     * Get the lower and upper warning limit PVs
     *
     * @return two element array of PVs with the lower and upper limit PVs
     */
    @Override
    public String[] getWarningLimitPVs() {
        return constructLimitPVs("LOW", "HIGH");
    }

    /**
     * Get the lower and upper alarm limit PVs
     *
     * @return two element array of PVs with the lower and upper limit PVs
     */
    @Override
    public String[] getAlarmLimitPVs() {
        return constructLimitPVs("LOLO", "HIHI");
    }

    /**
     * Get the lower and upper drive limit PVs
     *
     * @return two element array of PVs with the lower and upper limit PVs
     */
    @Override
    public String[] getDriveLimitPVs() {
        return constructLimitPVs("DRVL", "DRVH");
    }

    /**
     * Construct the lower and upper limit PVs from the lower and upper suffixes
     *
     * @return two element array of PVs with the lower and upper limit PVs
     */
    private String[] constructLimitPVs(final String lowerSuffix, final String upperSuffix) {
        final String[] rangePVs = new String[2];
        rangePVs[0] = channelName() + "." + lowerSuffix;
        rangePVs[1] = channelName() + "." + upperSuffix;
        return rangePVs;
    }

    /**
     * Convenience method which returns the upper display limit.
     */
    @Override
    public Number rawUpperDisplayLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getUpperDispLimit();
    }

    /**
     * Convenience method which returns the lower display limit.
     */
    @Override
    public Number rawLowerDisplayLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getLowerDispLimit();
    }

    /**
     * Convenience method which returns the upper alarm limit.
     */
    @Override
    public Number rawUpperAlarmLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getUpperAlarmLimit();
    }

    /**
     * Convenience method which returns the lower alarm limit.
     */
    @Override
    public Number rawLowerAlarmLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getLowerAlarmLimit();
    }

    /**
     * Convenience method which returns the upper warning limit.
     */
    @Override
    public Number rawUpperWarningLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getUpperWarningLimit();
    }

    /**
     * Convenience method which returns the lower warning limit.
     */
    @Override
    public Number rawLowerWarningLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getLowerWarningLimit();
    }

    /**
     * Convenience method which returns the upper control limit.
     */
    @Override
    public Number rawUpperControlLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getUpperCtrlLimit();
    }

    /**
     * Convenience method which returns the lower control limit.
     */
    @Override
    public Number rawLowerControlLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getLowerCtrlLimit();
    }

    /**
     * Returns a DBR_CTRL instance of the appropriate for this channel. The
     * DBR_CTRL record contains valuable information about the channel such as
     * the units and upper and lower limits for alarm, display, warning and
     * control. All of these items are returned as a DBData instance. Examples:      <code>
     *      String units = channel.getCtrlInfo().units();
     *      double upperDisplayLimit = channel.getCtrlInfo().upperDispLimit().doubleValue();
     * </code>
     */
    protected CTRL getCtrlInfo() throws ConnectionException, GetException {
        connectIfNeverConnected();

        int pvType = nativeType();
        int controlType = -1;

        final DBRType nativeType = getJcaType();

        if (nativeType.isBYTE()) {
            controlType = DBRType.CTRL_BYTE.getValue();
        } else if (nativeType.isENUM()) {
            // there appears to be no ENUM control record
            throw new GetException("No control record for ENUM type for pv: " + strId);
        } else if (nativeType.isSHORT()) {
            controlType = DBRType.CTRL_SHORT.getValue();
        } else if (nativeType.isINT()) {
            controlType = DBRType.CTRL_INT.getValue();
        } else if (nativeType.isFLOAT()) {
            controlType = DBRType.CTRL_FLOAT.getValue();
        } else if (nativeType.isDOUBLE()) {
            controlType = DBRType.CTRL_DOUBLE.getValue();
        } else {
            String message = "No control record for type code: " + nativeType + " for pv: " + strId;
            throw new GetException(message);
        }

        CTRL dbr = (CTRL) getVal(controlType);

        return dbr;
    }

    /**
     * This convenience method returns a data value object for a general data
     * type. It always attempts a connect before fetching data. For primitive
     * data types, it returns an array of values.
     */
    public Object getValue() throws ConnectionException, GetException {
        if (!connectAndWait()) {
            throw new ConnectionException();
        }

        int dataType = nativeType();
        return getVal(dataType).getValue();
    }

    /**
     * Get a <code>ChannelRecord</code> representing the fetched record for the
     * specified type.
     *
     * @return the channel record
     */
    @Override
    protected ChannelRecord getRawStringValueRecord() throws ConnectionException, GetException {
        return getRawValueRecord(STRING);
    }

    /**
     * Get a <code>ChannelRecord</code> representing the fetched record for the
     * specified type.
     *
     * @param pvType the type of PV to fetch
     * @return the channel record
     */
    protected ChannelRecord getRawValueRecord(final int pvType) throws ConnectionException, GetException {
        connectAndWait();
        DBR dbr = this.getVal(pvType);
        ChannelRecord record;

        synchronized (dbr) {
            ValueAdaptor adaptor = new DbrValueAdaptor(dbr);
            record = new ChannelRecordImpl(adaptor);
        }

        return record;
    }

    /**
     * Return a <code>ChannelRecord</code> representing the fetched record for
     * the native type of this channel. This is a convenient way to get the
     * value of the PV.
     */
    @Override
    public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
        connectAndWait();
        return getRawValueRecord(nativeType());
    }

    /**
     * Get a <code>ChannelStatusRecord</code> representing the fetched record
     * for the specified type.
     *
     * @return the channel record
     */
    @Override
    protected ChannelStatusRecord getRawStringStatusRecord() throws ConnectionException, GetException {
        return getRawStatusRecord(DBRType.STS_STRING.getValue());
    }

    /**
     * Get a <code>ChannelStatusRecord</code> representing the fetched record
     * for the specified type of this channel.
     */
    protected ChannelStatusRecord getRawStatusRecord(final int type) throws ConnectionException, GetException {
        connectAndWait();
        DBR dbr = this.getVal(type);
        ChannelStatusRecord record;

        synchronized (dbr) {
            StatusAdaptor adaptor = new DbrStatusAdaptor(dbr);
            record = new ChannelStatusRecordImpl(adaptor);
        }

        return record;
    }

    /**
     * Get a <code>ChannelStatusRecord</code> representing the fetched record
     * for the native type of this channel.
     */
    @Override
    public ChannelStatusRecord getRawStatusRecord() throws ConnectionException, GetException {
        connectAndWait();
        return getRawStatusRecord(getStatusType());
    }

    /**
     * Get a <code>ChannelTimeRecord</code> representing the fetched record for
     * the specified type.
     *
     * @return the channel record
     */
    @Override
    protected ChannelTimeRecord getRawStringTimeRecord() throws ConnectionException, GetException {
        return getRawTimeRecord(DBRType.TIME_STRING.getValue());
    }

    /**
     * Return a <code>ChannelTimeRecord</code> representing the fetched record
     * for the specified type of this channel.
     */
    public ChannelTimeRecord getRawTimeRecord(final int type) throws ConnectionException, GetException {
        connectAndWait();
        DBR dbr = this.getVal(type);
        ChannelTimeRecord record;

        synchronized (dbr) {
            TimeAdaptor adaptor = new DbrTimeAdaptor(dbr);
            record = new ChannelTimeRecordImpl(adaptor);
        }

        return record;
    }

    /**
     * Return a <code>ChannelTimeRecord</code> representing the fetched record
     * for the native type of this channel.
     */
    @Override
    public ChannelTimeRecord getRawTimeRecord() throws ConnectionException, GetException {
        connectAndWait();
        return getRawTimeRecord(getTimeType());
    }

    /**
     * Gets the value of a PV as a database request object
     *
     * @param type DBR type code of returned object
     * @return DBR object containing PV value
     */
    private DBR getVal(final int type) throws ConnectionException, GetException {
        this.checkConnection("getVal()");
        int count = this.elementCount();

        return this.getVal(type, count);
    }

    /**
     * Return process variable values in specific type and number
     *
     * @param type DBR type code of returned value
     * @param count number of values to return
     * @return DBR containing process variable values
     * @exception ConnectionException channel not connected
     * @exception GetException channel access get failure
     */
    private DBR getVal(final int type, final int count) throws ConnectionException, GetException {
        this.checkConnection("getVal()");

        try {
            DBR dbr = jcaChannel.get(DBRType.forValue(type), count);
            flushGetIO();
            return dbr;
        } catch (CAException exception) {
            LOGGER.log(Level.WARNING, "Error getting value from: " + strId, exception);
            throw new RuntimeException(exception);
        }
    }

    /**
     * Get value of process variable asynchronously with a "get monitor" object.
     * Value of process variable is sent to the event listener indicated by
     * IEventSinkValue.
     *
     * @param listener Listener of the callback event.
     * @throws xal.ca.ConnectionException channel is not connected
     * @exception xal.ca.GetException general channel access failure
     */
    @Override
    public void getRawValueCallback(final IEventSinkValue listener) throws ConnectionException, GetException {
        getRawValueCallback(listener, true);
    }

    /**
     * Get value of process variable asynchronously with a "get monitor" object.
     * Value of process variable is sent to the event listener indicated by
     * IEventSinkValue.
     *
     * @param listener Listener of the callback event.
     * @throws xal.ca.ConnectionException channel is not connected
     * @exception xal.ca.GetException general channel access failure
     */
    @Override
    public void getRawValueCallback(final IEventSinkValue listener, final boolean attemptConnection) throws ConnectionException, GetException {
        checkConnection("getValueCallback()", attemptConnection);

        new Getback(this, listener);
        if (listener == null) {
            flushGetIO();
        }
    }

    /**
     * Submit a non-blocking Get request with callback
     */
    @Override
    public void getRawValueTimeCallback(final IEventSinkValTime listener, final boolean attemptConnection) throws ConnectionException, GetException {
        checkConnection("getRawValueTimeCallback()", attemptConnection);

        try {
            final DBRType timeDBRType = getTimeDBRType();

            jcaChannel.get(timeDBRType, elementCount(), new gov.aps.jca.event.GetListener() {
                @Override
                public void getCompleted(final gov.aps.jca.event.GetEvent event) {
                    final DbrTimeAdaptor adaptor = new DbrTimeAdaptor(event.getDBR());
                    listener.eventValue(new ChannelTimeRecordImpl(adaptor), JcaChannel.this);
                }
            });
        } catch (gov.aps.jca.CAException exception) {
            throw new RuntimeException("Exception getting the time DBR type for " + strId, exception);
        }
    }

    /**
     * Asynchronously put a raw value to the channel process variable. Fire the
     * specified callback when put is complete.
     *
     * @param newVal value sent to process variable
     * @param listener The receiver of the callback event
     * @throws xal.ca.ConnectionException channel is not connected
     * @throws xal.ca.PutException general put failure
     */
    @Override
    public void putRawValCallback(final String newVal, final xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection("putValCallback()");

        try {
            jcaChannel.put(newVal, new PutNotifier(this, listener));
            if (listener == null) {
                flushPutIO();
            }
        } catch (CAException exception) {
            Logger.getLogger("global").log(Level.WARNING, "Error putting value to: " + strId, exception);
            throw new PutException("JcaChannel.putValCallback(): " + exception.getMessage());
        }
    }

    /**
     * Asynchronously put a raw value to the channel process variable. Fire the
     * specified callback when put is complete.
     *
     * @param newVal value sent to process variable
     * @param listener The receiver of the callback event
     * @throws xal.ca.ConnectionException channel is not connected
     * @throws xal.ca.PutException general put failure
     */
    @Override
    public void putRawValCallback(final byte newVal, final xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection("putValCallback()");

        try {
            jcaChannel.put(newVal, new PutNotifier(this, listener));
            if (listener == null) {
                flushPutIO();
            }
        } catch (CAException exception) {
            LOGGER.log(Level.WARNING, "Error putting value to: " + strId, exception);
            throw new PutException("JcaChannel.putValCallback(): " + exception.getMessage());
        }
    }

    /**
     * Asynchronously put a raw value to the channel process variable. Fire the
     * specified callback when put is complete.
     *
     * @param newVal value sent to process variable
     * @param listener The receiver of the callback event
     * @throws xal.ca.ConnectionException channel is not connected
     * @throws xal.ca.PutException general put failure
     */
    @Override
    public void putRawValCallback(final short newVal, final xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection("putValCallback()");

        try {
            jcaChannel.put(newVal, new PutNotifier(this, listener));
            if (listener == null) {
                flushPutIO();
            }
        } catch (CAException exception) {
            LOGGER.log(Level.WARNING, "Error putting value to: " + strId, exception);
            throw new PutException("JcaChannel.putValCallback(): " + exception.getMessage());
        }
    }

    /**
     * Asynchronously put a raw value to the channel process variable. Fire the
     * specified callback when put is complete.
     *
     * @param newVal value sent to process variable
     * @param listener The receiver of the callback event
     * @throws xal.ca.ConnectionException channel is not connected
     * @throws xal.ca.PutException general put failure
     */
    @Override
    public void putRawValCallback(final int newVal, final xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection("putValCallback()");

        try {
            jcaChannel.put(newVal, new PutNotifier(this, listener));
            if (listener == null) {
                flushPutIO();
            }
        } catch (CAException exception) {
            LOGGER.log(Level.WARNING, "Error putting value to: " + strId, exception);
            throw new PutException("JcaChannel.putValCallback(): " + exception.getMessage());
        }
    }

    /**
     * Asynchronously put a raw value to the channel process variable. Fire the
     * specified callback when put is complete.
     *
     * @param newVal value sent to process variable
     * @param listener The receiver of the callback event
     * @throws xal.ca.ConnectionException channel is not connected
     * @throws xal.ca.PutException general put failure
     */
    @Override
    public void putRawValCallback(final float newVal, final xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection("putValCallback()");

        try {
            jcaChannel.put(newVal, new PutNotifier(this, listener));
            if (listener == null) {
                flushPutIO();
            }
        } catch (CAException exception) {
            LOGGER.log(Level.WARNING, "Error putting value to: " + strId, exception);
            throw new PutException("JcaChannel.putValCallback(): " + exception.getMessage());
        }
    }

    /**
     * Asynchronously put a raw value to the channel process variable. Fire the
     * specified callback when put is complete.
     *
     * @param newVal value sent to process variable
     * @param listener The receiver of the callback event
     * @throws xal.ca.ConnectionException channel is not connected
     * @throws xal.ca.PutException general put failure
     */
    @Override
    public void putRawValCallback(double newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection("putValCallback()");

        try {
            jcaChannel.put(newVal, new PutNotifier(this, listener));
            if (listener == null) {
                flushPutIO();
            }
        } catch (CAException exception) {
            LOGGER.log(Level.WARNING, "Error putting value to: " + strId, exception);
            throw new PutException("JcaChannel.putValCallback(): " + exception.getMessage());
        }
    }

    /**
     * Asynchronously put a raw value to the channel process variable. Fire the
     * specified callback when put is complete.
     *
     * @param newVal value sent to process variable
     * @param listener The receiver of the callback event
     * @throws xal.ca.ConnectionException channel is not connected
     * @throws xal.ca.PutException general put failure
     */
    @Override
    public void putRawValCallback(byte[] newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection("putValCallback()");

        try {
            jcaChannel.put(newVal, new PutNotifier(this, listener));
            if (listener == null) {
                flushPutIO();
            }
        } catch (CAException exception) {
            LOGGER.log(Level.WARNING, "Error putting value to: " + strId, exception);
            throw new PutException("JcaChannel.putValCallback(): Incompatible types - " + exception.getMessage());
        }
    }

    /**
     * Asynchronously put a raw value to the channel process variable. Fire the
     * specified callback when put is complete.
     *
     * @param newVal value sent to process variable
     * @param listener The receiver of the callback event
     * @throws xal.ca.ConnectionException channel is not connected
     * @throws xal.ca.PutException general put failure
     */
    @Override
    public void putRawValCallback(short[] newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection("putValCallback()");

        try {
            jcaChannel.put(newVal, new PutNotifier(this, listener));
            if (listener == null) {
                flushPutIO();
            }
        } catch (CAException exception) {
            LOGGER.log(Level.WARNING, "Error putting value to: " + strId, exception);
            throw new PutException("JcaChannel.putValCallback(): Incompatible types - " + exception.getMessage());
        }
    }

    /**
     * Asynchronously put a raw value to the channel process variable. Fire the
     * specified callback when put is complete.
     *
     * @param newVal value sent to process variable
     * @param listener The receiver of the callback event
     * @throws xal.ca.ConnectionException channel is not connected
     * @throws xal.ca.PutException general put failure
     */
    @Override
    public void putRawValCallback(int[] newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection("putValCallback()");

        try {
            jcaChannel.put(newVal, new PutNotifier(this, listener));
            if (listener == null) {
                flushPutIO();
            }
        } catch (CAException exception) {
            LOGGER.log(Level.WARNING, "Error putting value to: " + strId, exception);
            throw new PutException("JcaChannel.putValCallback(): Incompatible types - " + exception.getMessage());
        }
    }

    /**
     * Asynchronously put a raw value to the channel process variable. Fire the
     * specified callback when put is complete.
     *
     * @param newVal value sent to process variable
     * @param listener The receiver of the callback event
     * @throws xal.ca.ConnectionException channel is not connected
     * @throws xal.ca.PutException general put failure
     */
    @Override
    public void putRawValCallback(float[] newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection("putValCallback()");

        try {
            jcaChannel.put(newVal, new PutNotifier(this, listener));
            if (listener == null) {
                flushPutIO();
            }
        } catch (CAException exception) {
            LOGGER.log(Level.WARNING, "Error putting value to: " + strId, exception);
            throw new PutException("JcaChannel.putValCallback(): Incompatible types - " + exception.getMessage());
        }
    }

    /**
     * Asynchronously put a raw value to the channel process variable. Fire the
     * specified callback when put is complete.
     *
     * @param newVal value sent to process variable
     * @param listener The receiver of the callback event
     * @throws xal.ca.ConnectionException channel is not connected
     * @throws xal.ca.PutException general put failure
     */
    @Override
    public void putRawValCallback(double[] newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection("putValCallback()");

        try {
            jcaChannel.put(newVal, new PutNotifier(this, listener));
            if (listener == null) {
                flushPutIO();
            }
        } catch (CAException exception) {
            LOGGER.log(Level.WARNING, "Error putting value to: " + strId, exception);
            throw new PutException("JcaChannel.putValCallback(): Incompatible types - " + exception.getMessage());
        }

    }

    /**
     * Setup a monitor on this channel
     *
     * @param ifcSink interface to data sink
     * @param intMaskFire code specifying when the monitor is fired or'ed
     * combination of {Monitor.VALUE, Monitor.LOG, Monitor.ALARM}
     * @return MonitorSrc object associated with this event
     */
    @Override
    public xal.ca.Monitor addMonitorValTime(IEventSinkValTime ifcSink, int intMaskFire) throws ConnectionException, MonitorException {
        this.checkConnection("addMonitorValTime()");
        return JcaMonitor.newValueTimeMonitor(this, ifcSink, intMaskFire);
    }

    /**
     * Setup a monitor on this channel
     *
     * @param ifcSink interface to data sink
     * @param intMaskFire code specifying when the monitor is fired or'ed
     * combination of {Monitor.VALUE, Monitor.LOG, Monitor.ALARM}
     * @return MonitorSrc object associated with this event
     */
    @Override
    public xal.ca.Monitor addMonitorValStatus(IEventSinkValStatus ifcSink, int intMaskFire) throws ConnectionException, MonitorException {
        this.checkConnection("addMonitorValStatus()");
        return JcaMonitor.newValueStatusMonitor(this, ifcSink, intMaskFire);
    }

    /**
     * Setup a monitor on this channel
     *
     * @param ifcSink interface to data sink
     * @param intMaskFire code specifying when the monitor is fired or'ed
     * combination of {Monitor.VALUE, Monitor.LOG, Monitor.ALARM}
     * @return MonitorSrc object associated with this event
     */
    @Override
    public xal.ca.Monitor addMonitorValue(IEventSinkValue ifcSink, int intMaskFire) throws ConnectionException, MonitorException {
        this.checkConnection("addMonitorValue()");
        return JcaMonitor.newValueMonitor(this, ifcSink, intMaskFire);
    }

    /**
     * Flushes the channel access request buffer for events
     */
    private void flushEvent() {
        try {
            jcaContext.pendEvent(dblTmEvt);
        } catch (CAException exception) {
            LOGGER.log(Level.SEVERE, "Error flushing the channel access request buffer.", exception);
            LOGGER.log(Level.SEVERE, null, exception);
        }
    }

    /**
     * Flushes the channel access request buffer for get operations
     *
     * @exception GetException a pendIO time out occurred
     */
    private void flushGetIO() throws GetException {
        try {
            jcaContext.pendIO(dblTmIO);
        } catch (CAException | TimeoutException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            LOGGER.log(Level.SEVERE, "Error flushing the channel access GET I/O buffer.", exception);
            throw new GetException("JcaChannel.flushGetIO() - channel access time out occurred");
        }
    }

    /**
     * Flushes the channel access request buffer for put operations
     *
     * @exception PutException a pendIO time out occurred
     */
    private void flushPutIO() throws PutException {
        try {
            jcaContext.pendIO(dblTmIO);
        } catch (CAException | TimeoutException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            LOGGER.log(Level.SEVERE, "Error flushing the channel access PUT I/O buffer.", exception);
            throw new PutException("JcaChannel.flushPutIO() - channel access time out occurred");
        }
    }

    @Override
    public void putRawValCallback(long newVal, PutListener listener) throws ConnectionException, PutException {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putRawValCallback(String[] newVal, PutListener listener) throws ConnectionException, PutException {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putRawValCallback(long[] newVal, PutListener listener) throws ConnectionException, PutException {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
