/*
 * Copyright (C) 2020 European Spallation Source ERIC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.plugin.epics7;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pva.data.PVADouble;
import org.epics.pva.data.PVAString;
import org.epics.pva.data.PVAStructure;
import xal.ca.ChannelRecord;
import xal.ca.ChannelStatusRecord;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.IEventSinkValStatus;
import xal.ca.IEventSinkValTime;
import xal.ca.IEventSinkValue;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.ca.PutListener;
import xal.tools.apputils.Preferences;

/**
 * This {@link xal.ca.Channel} implementation can connect to ChannelAccess or PV Access. If the PV signal starts with
 * 'ca://', it will only connect to CA; if it starts with 'pva://', it will only connect to PVA; otherwise it tries to
 * connect to both and uses the protocol that replies first.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7Channel extends xal.ca.Channel implements NativeChannel.ConnectionListener {

    private final Epics7ChannelSystem epics7ChannelSystem;

    //  Constants
    public static final double C_DBL_DEF_TIME_IO = 5.0;
    public static final double C_DBL_DEF_TIME_EVENT = 0.1;
    public static final String C_S_DEF_PROTOCOL = "NONE";

    // Property names
    private static final String DEF_TIME_IO = "c_dblDefTimeIO";
    private static final String DEF_TIME_EVENT = "c_dblDefTimeEvent";
    private static final String DEF_PROTOCOL = "defProtocol";

    // Fields
    public static final String VALUE_FIELD = "value";
    public static final String ALARM_FIELD = "alarm";
    public static final String DISPLAY_FIELD = "display";
    public static final String VALUE_ALARM_FIELD = "valueAlarm";
    public static final String CONTROL_FIELD = "control";
    public static final String TIMESTAMP_FIELD = "timeStamp";

    // Request can contain the following fields: value, alarm, timeStamp, display, control, valueAlarm
    public static final String VALUE_REQUEST = VALUE_FIELD;
    public static final String STATUS_REQUEST = VALUE_FIELD + "," + ALARM_FIELD;
    public static final String TIME_REQUEST = STATUS_REQUEST + "," + TIMESTAMP_FIELD;

    private static final String CA_PREFIX = "ca://";
    private static final String PVA_PREFIX = "pva://";

    private static final String NO_DISPLAY_FIELD_EXC = "The channel didn't return a \"display\" field.";
    private static final String NO_CONTROL_FIELD_EXC = "The channel didn't return a \"control\" field.";
    private static final String NO_VALUE_ALARM_FIELD_EXC = "The channel didn't return a \"valueAlarm\" field.";
    private static final String UNSUPPORTED_EXC = "Not supported in EPICS7 (only CA).";
    protected static final String CONNECTION_EXC = "Connection Exception thrown";

    private volatile NativeChannel caChannel;
    private volatile NativeChannel pvaChannel;
    private volatile NativeChannel nativeChannel;
    private final String defaultProtocol;

    private final Object connectionLock = new Object();

    private CountDownLatch connectionLatch;

    public Epics7Channel(String signalName, Epics7ChannelSystem epics7ChannelSystem) {
        super(signalName);

        this.epics7ChannelSystem = epics7ChannelSystem;

        // Load default timeouts from preferences if available, otherwise use hardcoded values.
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(xal.ca.Channel.class);
        dblTmIO = defaults.getDouble(DEF_TIME_IO, C_DBL_DEF_TIME_IO);
        dblTmEvt = defaults.getDouble(DEF_TIME_EVENT, C_DBL_DEF_TIME_EVENT);
        defaultProtocol = defaults.get(DEF_PROTOCOL, C_S_DEF_PROTOCOL);
    }

    protected NativeChannel getNativeChannel() {
        return nativeChannel;
    }

    @Override
    public boolean connectAndWait(double timeout) {
        requestConnection();

        // If not connected, wait for timeout.
        if (!isConnected()) {
            try {
                connectionLatch.await((long) (1000 * timeout), TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Logger.getLogger(Epics7Channel.class.getName()).log(Level.INFO, null, ex);
                Thread.currentThread().interrupt();
            }
        }
        return isConnected();
    }

    @Override
    public void requestConnection() {
        // Only request a new connection if not done previously.
        if (!isConnected() && connectionLatch == null) {
            synchronized (connectionLock) {
                connectionLatch = new CountDownLatch(1);

                // prefix is pva:// or else the default protocol is PVA or None
                if (strId.startsWith(PVA_PREFIX) || (!strId.startsWith(CA_PREFIX) && !defaultProtocol.equals("CA"))) {
                    pvaChannel = epics7ChannelSystem.createPvaChannel(
                            strId.startsWith(PVA_PREFIX) ? strId.substring(PVA_PREFIX.length()) : strId, this);
                    pvaChannel.requestConnection();
                }
                // prefix is ca:// or else the default protocol is CA or None
                if (strId.startsWith(CA_PREFIX) || (!strId.startsWith(PVA_PREFIX) && !defaultProtocol.equals("PVA"))) {
                    caChannel = epics7ChannelSystem.createCaChannel(
                            strId.startsWith(CA_PREFIX) ? strId.substring(CA_PREFIX.length()) : strId, this);
                    caChannel.requestConnection();
                }
            }
        }
    }

    @Override
    public void disconnect() {
        synchronized (connectionLock) {
            if (caChannel != null) {
                caChannel.destroy();
            }
            if (pvaChannel != null) {
                pvaChannel.destroy();
            }
            nativeChannel = null;
            connectionFlag = false;
            connectionLatch = null;
        }
    }

    //------------- Implementing NativeChannel.ConnectionListener --------------
    /**
     * When no protocol is requested explicitly both are tried, and the first one to connect wins. The loser is
     * destroyed, so that only one native channel is ever used.
     */
    @Override
    public void connectionChanged(NativeChannel channel, boolean connected) {
        if (connected) {
            synchronized (connectionLock) {
                // This is in case the disconnect method is called after a connection is requested and before the
                // connection is made.
                if (connectionLatch == null) {
                    return;
                }

                if (connectionFlag) {
                    // The other protocol got there first; drop this one.
                    if (channel == caChannel) {
                        caChannel = null;
                    } else if (channel == pvaChannel) {
                        pvaChannel = null;
                    }
                    channel.destroy();

                    return;
                }

                nativeChannel = channel;
                connectionFlag = true;

                // Releasing the connection latch.
                connectionLatch.countDown();
            }

            // Notify listeners.
            if (connectionProxy != null) {
                connectionProxy.connectionMade(this);
            }
        } else if (channel == nativeChannel) {
            connectionFlag = false;
            // Notify listeners if the channel that is in use is disconnected.
            if (connectionProxy != null) {
                connectionProxy.connectionDropped(this);
            }
        }
    }
    //---------------------------------------------------------------------------------

    // --------- Get properties ---------
    @Override
    public Class<?> elementType() {
        try {
            ChannelRecord channelRecord = getRawValueRecord();
            return channelRecord.getType();
        } catch (GetException ex) {
            Logger.getLogger(Epics7Channel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public int elementCount() throws ConnectionException {
        try {
            ChannelRecord channelRecord = getRawValueRecord();
            return channelRecord.getCount();
        } catch (GetException ex) {
            Logger.getLogger(Epics7Channel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    // TODO
    @Override
    public boolean readAccess() throws ConnectionException {
        return true;
    }

    // TODO
    @Override
    public boolean writeAccess() throws ConnectionException {
        return true;
    }

    protected PVAStructure getControl() throws GetException {
        PVAStructure pvStructure = get(CONTROL_FIELD);
        if (pvStructure != null) {
            return pvStructure.get(CONTROL_FIELD);
        } else {
            return null;
        }
    }

    protected PVAStructure getDisplay() throws GetException {
        PVAStructure pvStructure = get(DISPLAY_FIELD);
        if (pvStructure != null) {
            return pvStructure.get(DISPLAY_FIELD);
        } else {
            return null;
        }
    }

    protected PVAStructure getVAlueAlarm() throws GetException {
        PVAStructure pvStructure = get(VALUE_ALARM_FIELD);
        if (pvStructure != null) {
            return pvStructure.get(VALUE_ALARM_FIELD);
        } else {
            return null;
        }
    }

    /**
     * Read a double member of a metadata sub-structure.
     */
    private static Number doubleField(PVAStructure structure, String name) {
        PVADouble field = structure.get(name);
        return field == null ? null : field.get();
    }

    @Override
    public String getUnits() throws GetException {
        PVAStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            PVAString units = displayStructure.get("units");
            return units == null ? "" : units.get();
        } else {
            throw new GetException(NO_DISPLAY_FIELD_EXC);
        }
    }

    @Override
    public Number rawUpperDisplayLimit() throws GetException {
        PVAStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            return doubleField(displayStructure, "limitHigh");
        } else {
            throw new GetException(NO_DISPLAY_FIELD_EXC);
        }
    }

    @Override
    public Number rawLowerDisplayLimit() throws GetException {
        PVAStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            return doubleField(displayStructure, "limitLow");
        } else {
            throw new GetException(NO_DISPLAY_FIELD_EXC);
        }
    }

    @Override
    public Number rawUpperAlarmLimit() throws GetException {
        PVAStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            return doubleField(alarmValueStructure, "highAlarmLimit");
        } else {
            throw new GetException(NO_VALUE_ALARM_FIELD_EXC);
        }
    }

    @Override
    public Number rawLowerAlarmLimit() throws GetException {
        PVAStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            return doubleField(alarmValueStructure, "lowAlarmLimit");
        } else {
            throw new GetException(NO_VALUE_ALARM_FIELD_EXC);
        }
    }

    @Override
    public Number rawUpperWarningLimit() throws GetException {
        PVAStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            return doubleField(alarmValueStructure, "highWarningLimit");
        } else {
            throw new GetException(NO_VALUE_ALARM_FIELD_EXC);
        }
    }

    @Override
    public Number rawLowerWarningLimit() throws GetException {
        PVAStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            return doubleField(alarmValueStructure, "lowWarningLimit");
        } else {
            throw new GetException(NO_VALUE_ALARM_FIELD_EXC);
        }
    }

    @Override
    public Number rawUpperControlLimit() throws GetException {
        PVAStructure controlStructure = getControl();
        if (controlStructure != null) {
            return doubleField(controlStructure, "limitHigh");
        } else {
            throw new GetException(NO_CONTROL_FIELD_EXC);
        }
    }

    @Override
    public Number rawLowerControlLimit() throws GetException {
        PVAStructure controlStructure = getControl();
        if (controlStructure != null) {
            return doubleField(controlStructure, "limitLow");
        } else {
            throw new GetException(NO_CONTROL_FIELD_EXC);
        }
    }

    // --------- Get ---------
    public PVAStructure get(String request, boolean attemptConnection) throws GetException {
        GetListener listener = new GetListener();

        getCallback(request, listener, attemptConnection);

        try {
            boolean noTimeout = listener.await((long) (1000 * dblTmIO), TimeUnit.MILLISECONDS);
            if (!noTimeout) {
                throw new GetException("Get timeout");
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Epics7Channel.class.getName()).log(Level.SEVERE, null, ex);
            Thread.currentThread().interrupt();
            throw new GetException("Get interrupted");
        }

        return listener.getPvStructure();
    }

    public PVAStructure get(String request) throws GetException {
        return get(request, true);
    }

    public void getCallback(String request, final EventListener listener, boolean attemptConnection)
            throws GetException {
        try {
            checkConnection("ChannelGet", attemptConnection);
        } catch (ConnectionException ex) {
            Logger.getLogger(Epics7Channel.class.getName()).log(Level.SEVERE, null, ex);
            throw new GetException("Connection Exception thrown.");
        }

        nativeChannel.get(request, listener);
    }

    public void getCallback(String request, EventListener listener) throws GetException {
        getCallback(request, listener, true);
    }

    @Override
    public ChannelRecord getRawValueRecord() throws GetException {
        return new Epics7ChannelRecord(get(VALUE_REQUEST));
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener) throws GetException {
        getCallback(VALUE_REQUEST,
                pvStructure -> {
                    ChannelRecord channelRecord = new Epics7ChannelRecord(pvStructure);
                    listener.eventValue(channelRecord, this);
                });
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener, boolean attemptConnection) throws GetException {
        getCallback(VALUE_REQUEST,
                pvStructure -> {
                    ChannelRecord channelRecord = new Epics7ChannelRecord(pvStructure);
                    listener.eventValue(channelRecord, this);
                }, attemptConnection);
    }

    @Override
    protected ChannelRecord getRawStringValueRecord() throws GetException {
        return getRawValueRecord();
    }

    @Override
    public ChannelStatusRecord getRawStatusRecord() throws GetException {
        return new Epics7ChannelStatusRecord(get(STATUS_REQUEST));
    }

    @Override
    protected ChannelStatusRecord getRawStringStatusRecord() throws GetException {
        return getRawStatusRecord();
    }

    @Override
    public ChannelTimeRecord getRawTimeRecord() throws GetException {
        return new Epics7ChannelTimeRecord(get(TIME_REQUEST));
    }

    @Override
    protected ChannelTimeRecord getRawStringTimeRecord() throws GetException {
        return getRawTimeRecord();
    }

    @Override
    public void getRawValueTimeCallback(IEventSinkValTime listener, boolean attemptConnection) throws GetException {
        getCallback(TIME_REQUEST,
                pvStructure -> {
                    ChannelTimeRecord channelRecord = new Epics7ChannelTimeRecord(pvStructure);
                    listener.eventValue(channelRecord, this);
                });
    }

    // --------- Monitor ---------
    @Override
    public xal.ca.Monitor addMonitorValTime(IEventSinkValTime listener, int intMaskFire) throws MonitorException {
        try {
            checkConnection("addMonitorValTime");
        } catch (ConnectionException ex) {
            throw new MonitorException(CONNECTION_EXC, ex);
        }

        return Epics7Monitor.createNewMonitor(this, TIME_REQUEST, pvStructure -> {
            ChannelTimeRecord channelRecord = new Epics7ChannelTimeRecord(pvStructure);
            listener.eventValue(channelRecord, this);
        }, intMaskFire);
    }

    @Override
    public xal.ca.Monitor addMonitorValStatus(IEventSinkValStatus listener, int intMaskFire) throws MonitorException {
        try {
            checkConnection("addMonitorValStatus");
        } catch (ConnectionException ex) {
            throw new MonitorException(CONNECTION_EXC, ex);
        }

        return Epics7Monitor.createNewMonitor(this, STATUS_REQUEST, pvStructure -> {
            ChannelStatusRecord channelRecord = new Epics7ChannelStatusRecord(pvStructure);
            listener.eventValue(channelRecord, this);
        }, intMaskFire);
    }

    @Override
    public xal.ca.Monitor addMonitorValue(IEventSinkValue listener, int intMaskFire) throws MonitorException {
        try {
            checkConnection("addMonitorValue");
        } catch (ConnectionException ex) {
            throw new MonitorException(CONNECTION_EXC, ex);
        }

        return Epics7Monitor.createNewMonitor(this, VALUE_REQUEST, pvStructure -> {
            ChannelRecord channelRecord = new Epics7ChannelRecord(pvStructure);
            listener.eventValue(channelRecord, this);
        }, intMaskFire);
    }

    // --------- Put ---------
    /**
     * Write a value to the channel's "value" field.
     *
     * When no listener is supplied the call blocks until the server acknowledges the write, or until the IO timeout
     * elapses.
     *
     * @param newValue a boxed scalar or a Java array
     */
    protected void putRawValCallback(Object newValue, PutListener listener) throws PutException {
        try {
            checkConnection("putRawValCallback");
        } catch (ConnectionException ex) {
            throw new PutException(CONNECTION_EXC, ex);
        }

        // If listener == null, wait for the put to complete.
        PutListenerImpl blocking = listener == null ? new PutListenerImpl() : null;
        PutListener effective = listener == null ? blocking : listener;

        nativeChannel.put(newValue, () -> effective.putCompleted(this));

        if (blocking != null) {
            try {
                if (!blocking.await((long) (1000 * dblTmIO), TimeUnit.MILLISECONDS)) {
                    throw new PutException("Put timeout");
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Epics7Channel.class.getName()).log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
                throw new PutException("Put interrupted");
            }
        }
    }

    @Override
    public void putRawValCallback(String newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(byte newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(short newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(int newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(long newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(float newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(double newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(String[] newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(byte[] newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(short[] newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(int[] newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(long[] newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(float[] newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    @Override
    public void putRawValCallback(double[] newVal, PutListener listener) throws PutException {
        putRawValCallback((Object) newVal, listener);
    }

    //----------------------------------------------------------------------------------
    // The following methods are not implemented because they only work with the
    // Channel Access protocol.
    @Override
    public String[] getOperationLimitPVs() {
        throw new UnsupportedOperationException(UNSUPPORTED_EXC);
    }

    @Override
    public String[] getWarningLimitPVs() {
        throw new UnsupportedOperationException(UNSUPPORTED_EXC);
    }

    @Override
    public String[] getAlarmLimitPVs() {
        throw new UnsupportedOperationException(UNSUPPORTED_EXC);
    }

    @Override
    public String[] getDriveLimitPVs() {
        throw new UnsupportedOperationException(UNSUPPORTED_EXC);

    }
    //----------------------------------------------------------------------------------

}

class PutListenerImpl implements PutListener {

    private final CountDownLatch doneSignal;

    public PutListenerImpl() {
        this.doneSignal = new CountDownLatch(1);
    }

    @Override
    public void putCompleted(xal.ca.Channel chan) {
        doneSignal.countDown();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return doneSignal.await(timeout, unit);
    }
}
