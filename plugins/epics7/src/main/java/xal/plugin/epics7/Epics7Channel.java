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
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvaccess.client.ChannelPutRequester;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVByte;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVFloat;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVShort;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;
import xal.ca.ChannelRecord;
import xal.ca.ChannelStatusRecord;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.IEventSinkValStatus;
import xal.ca.IEventSinkValTime;
import xal.ca.IEventSinkValue;
import xal.ca.PutException;
import xal.ca.PutListener;
import xal.tools.apputils.Preferences;

/**
 * This {@link xal.ca.Channel} implementation can connect to ChannelAccess or PV
 * Access. If the PV signal starts with 'ca://', it will only connect to CA; if
 * it starts with 'pva://', it will only connect to PVA; otherwise it tries to
 * connect to both and uses the protocol that replies first.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7Channel extends xal.ca.Channel implements ChannelRequester {

    private final Epics7ChannelSystem epics7ChannelSystem;

    //  Constants
    public static final double C_DBL_DEF_TIME_IO = 5.0;
    public static final double C_DBL_DEF_TIME_EVENT = 0.1;

    // Property names
    private static final String DEF_TIME_IO = "c_dblDefTimeIO";
    private static final String DEF_TIME_EVENT = "c_dblDefTimeEvent";

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

    private volatile Channel caChannel;
    private volatile Channel pvaChannel;
    private volatile Channel nativeChannel;

    private final Object connectionLock = new Object();

    private CountDownLatch connectionLatch;

    public Epics7Channel(String signalName, Epics7ChannelSystem epics7ChannelSystem) {
        super(signalName);

        this.epics7ChannelSystem = epics7ChannelSystem;

        // Load default timeouts from preferences if available, otherwise use hardcoded values.
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(xal.ca.Channel.class);
        m_dblTmIO = defaults.getDouble(DEF_TIME_IO, C_DBL_DEF_TIME_IO);
        m_dblTmEvt = defaults.getDouble(DEF_TIME_EVENT, C_DBL_DEF_TIME_EVENT);
    }

    protected Channel getNativeChannel() {
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
                Logger.getLogger(Epics7Channel.class
                        .getName()).log(Level.INFO, null, ex);
            }
        }
        return isConnected();
    }

    @Override
    public void requestConnection() {
        // Only request a new connection if not done previously.
        if (!isConnected() && connectionLatch == null) {
            connectionLatch = new CountDownLatch(1);

            if (!m_strId.startsWith(CA_PREFIX)) {
                synchronized (connectionLock) {
                    pvaChannel = epics7ChannelSystem.getPvaChannelProvider().createChannel(
                            m_strId.startsWith(PVA_PREFIX) ? m_strId.substring(PVA_PREFIX.length()) : m_strId, this, ChannelProvider.PRIORITY_DEFAULT);
                }
            }
            if (!m_strId.startsWith(PVA_PREFIX)) {
                synchronized (connectionLock) {
                    caChannel = epics7ChannelSystem.getCaChannelProvider().createChannel(
                            m_strId.startsWith(CA_PREFIX) ? m_strId.substring(CA_PREFIX.length()) : m_strId, this, ChannelProvider.PRIORITY_DEFAULT);
                }
            }
        }
    }

    @Override
    public void disconnect() {
        if (caChannel != null) {
            synchronized (connectionLock) {
                caChannel.destroy();
            }
        }
        if (pvaChannel != null) {
            synchronized (connectionLock) {
                pvaChannel.destroy();
            }
        }
        nativeChannel = null;
        connectionFlag = false;
    }

    //---------------- Implementing ChannelRequester abstract methods ------------------
    @Override
    public void channelStateChange(Channel chnl, Channel.ConnectionState cs) {
        Logger.getLogger(Epics7Channel.class.getName()).log(Level.FINE, "{0} provider: channel {1} changed status to {2}", new Object[]{chnl.getProvider().getProviderName(), chnl.getChannelName(), cs});

        if (cs == Channel.ConnectionState.CONNECTED) {
            // If the other channel is connected, destroy the channel that invoked this method.
            // Otherwise, use use it.
            synchronized (connectionLock) {
                if (connectionFlag) {
                    if (chnl == caChannel) {
                        caChannel = null;
                    } else if (chnl == pvaChannel) {
                        pvaChannel = null;
                    }
                    chnl.destroy();

                    return;
                }

                nativeChannel = chnl;
                connectionFlag = true;
                // Notify listeners.
                if (connectionProxy != null) {
                    connectionProxy.connectionMade(this);
                }

                // Releasing the connection latch.
                connectionLatch.countDown();
            }
        } else if (cs == Channel.ConnectionState.DISCONNECTED && chnl == nativeChannel) {
            // Notify listeners if the channel that is in used is disconnected.
            if (connectionProxy != null) {
                connectionProxy.connectionDropped(this);
            }
            connectionFlag = false;
        }
    }

    @Override
    public void channelCreated(Status status, Channel chnl) {
        Logger.getLogger(Epics7Channel.class.getName()).log(Level.FINE, "{0} provider created a channel: {1}", new Object[]{chnl.getProvider().getProviderName(), chnl.getChannelName()});
    }

    //------------------- Implementing Requester abstract methods ----------------------
    @Override
    public String getRequesterName() {
        if (nativeChannel != null) {
            return nativeChannel.getRequesterName();
        }
        return null;
    }

    @Override
    public void message(String message, MessageType mt) {
        Logger.getLogger(Epics7Channel.class.getName()).log(Level.INFO, message);
    }
    //---------------------------------------------------------------------------------

    // --------- Get properties ---------
    @Override
    public Class<?> elementType() throws ConnectionException {
        try {
            ChannelRecord record = getRawValueRecord();
            return record.getType();
        } catch (GetException ex) {
            Logger.getLogger(Epics7Channel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public int elementCount() throws ConnectionException {
        try {
            ChannelRecord record = getRawValueRecord();
            return record.getCount();
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

    private PVStructure getControl() throws ConnectionException, GetException {
        PVStructure pvStructure = get(CONTROL_FIELD);
        if (pvStructure != null) {
            PVStructure controlStructure = pvStructure.getStructureField(CONTROL_FIELD);
            return controlStructure;
        } else {
            return null;
        }
    }

    private PVStructure getDisplay() throws ConnectionException, GetException {
        PVStructure pvStructure = get(DISPLAY_FIELD);
        if (pvStructure != null) {
            PVStructure displayStructure = pvStructure.getStructureField(DISPLAY_FIELD);
            return displayStructure;
        } else {
            return null;
        }
    }

    private PVStructure getVAlueAlarm() throws ConnectionException, GetException {
        PVStructure pvStructure = get(VALUE_ALARM_FIELD);
        if (pvStructure != null) {
            PVStructure alarmValueStructure = pvStructure.getStructureField(VALUE_ALARM_FIELD);
            return alarmValueStructure;
        } else {
            return null;
        }
    }

    @Override
    public String getUnits() throws ConnectionException, GetException {
        PVStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            return displayStructure.getStringField("units").get();
        } else {
            throw new GetException("The channel didn't return a \"display\" field.");
        }
    }

    @Override
    public Number rawUpperDisplayLimit() throws ConnectionException, GetException {
        PVStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            return displayStructure.getDoubleField("limitHigh").get();
        } else {
            throw new GetException("The channel didn't return a \"display\" field.");
        }
    }

    @Override
    public Number rawLowerDisplayLimit() throws ConnectionException, GetException {
        PVStructure displayStructure = getDisplay();
        if (displayStructure != null) {
            return displayStructure.getDoubleField("limitLow").get();
        } else {
            throw new GetException("The channel didn't return a \"display\" field.");
        }
    }

    @Override
    public Number rawUpperAlarmLimit() throws ConnectionException, GetException {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            return alarmValueStructure.getDoubleField("highAlarmLimit").get();
        } else {
            throw new GetException("The channel didn't return a \"valueAlarm\" field.");
        }
    }

    @Override
    public Number rawLowerAlarmLimit() throws ConnectionException, GetException {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            return alarmValueStructure.getDoubleField("lowAlarmLimit").get();
        } else {
            throw new GetException("The channel didn't return a \"valueAlarm\" field.");
        }
    }

    @Override
    public Number rawUpperWarningLimit() throws ConnectionException, GetException {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            return alarmValueStructure.getDoubleField("highWarningLimit").get();
        } else {
            throw new GetException("The channel didn't return a \"valueAlarm\" field.");
        }
    }

    @Override
    public Number rawLowerWarningLimit() throws ConnectionException, GetException {
        PVStructure alarmValueStructure = getVAlueAlarm();
        if (alarmValueStructure != null) {
            return alarmValueStructure.getDoubleField("lowWarningLimit").get();
        } else {
            throw new GetException("The channel didn't return a \"valueAlarm\" field.");
        }
    }

    @Override
    public Number rawUpperControlLimit() throws ConnectionException, GetException {
        PVStructure controlStructure = getControl();
        if (controlStructure != null) {
            return controlStructure.getDoubleField("limitHigh").get();
        } else {
            throw new GetException("The channel didn't return a \"control\" field.");
        }
    }

    @Override
    public Number rawLowerControlLimit() throws ConnectionException, GetException {
        PVStructure controlStructure = getControl();
        if (controlStructure != null) {
            return controlStructure.getDoubleField("limitLow").get();
        } else {
            throw new GetException("The channel didn't return a \"control\" field.");
        }
    }

    // --------- Get ---------
    public PVStructure get(String request, boolean attemptConnection) throws ConnectionException, GetException {
        GetListener listener = new GetListener();

        getCallback(request, listener, attemptConnection);

        try {
            listener.await((long) (1000 * m_dblTmIO), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(Epics7Channel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return listener.getPvStructure();
    }

    public PVStructure get(String request) throws ConnectionException, GetException {
        return get(request, true);
    }

    public void getCallback(String request, final EventListener listener, boolean attemptConnection) throws ConnectionException, GetException {
        checkConnection("ChannelGet", attemptConnection);

        ChannelGetRequesterImpl channelGetRequester = new ChannelGetRequesterImpl(listener);

        CreateRequest createRequest = CreateRequest.create();
        PVStructure pvRequest = createRequest.createRequest(request);
        if (pvRequest != null) {
            nativeChannel.createChannelGet(channelGetRequester, pvRequest);
        }
    }

    public void getCallback(String request, EventListener listener) throws ConnectionException, GetException {
        getCallback(request, listener, true);
    }

    @Override
    public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
        return new Epics7ChannelRecord(get(VALUE_REQUEST), channelName());
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener) throws ConnectionException, GetException {
        getCallback(VALUE_REQUEST,
                (pvStructure) -> {
                    ChannelRecord record = new Epics7ChannelRecord(pvStructure, this.channelName());
                    listener.eventValue(record, this);
                });
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener, boolean attemptConnection) throws ConnectionException, GetException {
        getCallback(VALUE_REQUEST,
                (pvStructure) -> {
                    ChannelRecord record = new Epics7ChannelRecord(pvStructure, this.channelName());
                    listener.eventValue(record, this);
                }, attemptConnection);
    }

    @Override
    protected ChannelRecord getRawStringValueRecord() throws ConnectionException, GetException {
        return getRawValueRecord();
    }

    @Override
    public ChannelStatusRecord getRawStatusRecord() throws ConnectionException, GetException {
        return new Epics7ChannelStatusRecord(get(STATUS_REQUEST), channelName());
    }

    @Override
    protected ChannelStatusRecord getRawStringStatusRecord() throws ConnectionException, GetException {
        return getRawStatusRecord();
    }

    @Override
    public ChannelTimeRecord getRawTimeRecord() throws ConnectionException, GetException {
        return new Epics7ChannelTimeRecord(get(TIME_REQUEST), channelName());
    }

    @Override
    protected ChannelTimeRecord getRawStringTimeRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    public void getRawValueTimeCallback(IEventSinkValTime listener, boolean attemptConnection) throws ConnectionException, GetException {
        getCallback(TIME_REQUEST,
                (pvStructure) -> {
                    ChannelTimeRecord record = new Epics7ChannelTimeRecord(pvStructure, this.channelName());
                    listener.eventValue(record, this);
                });
    }

    // --------- Monitor ---------
    @Override
    public xal.ca.Monitor addMonitorValTime(IEventSinkValTime listener, int intMaskFire) throws ConnectionException, xal.ca.MonitorException {
        checkConnection("addMonitorValTime");

        return Epics7Monitor.createNewMonitor(this, TIME_REQUEST, (pvStructure) -> {
            ChannelTimeRecord record = new Epics7ChannelTimeRecord(pvStructure, this.channelName());
            listener.eventValue(record, this);
        }, intMaskFire);
    }

    @Override
    public xal.ca.Monitor addMonitorValStatus(IEventSinkValStatus listener, int intMaskFire) throws ConnectionException, xal.ca.MonitorException {
        checkConnection("addMonitorValStatus");

        return Epics7Monitor.createNewMonitor(this, STATUS_REQUEST, (pvStructure) -> {
            ChannelStatusRecord record = new Epics7ChannelStatusRecord(pvStructure, this.channelName());
            listener.eventValue(record, this);
        }, intMaskFire);
    }

    @Override
    public xal.ca.Monitor addMonitorValue(IEventSinkValue listener, int intMaskFire) throws ConnectionException, xal.ca.MonitorException {
        checkConnection("addMonitorValue");

        return Epics7Monitor.createNewMonitor(this, VALUE_REQUEST, (pvStructure) -> {
            ChannelRecord record = new Epics7ChannelRecord(pvStructure, this.channelName());
            listener.eventValue(record, this);
        }, intMaskFire);
    }

    // --------- Put ---------
    public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
        checkConnection("putRawValCallback");
        // If listener == null, wait for putDone event.
        if (listener == null) {
            listener = new PutListenerImpl();
        }

        ChannelPutRequester channelPutRequester = new ChannelPutRequesterImpl(listener, this, putListener);

        CreateRequest createRequest = CreateRequest.create();
        PVStructure pvRequest = createRequest.createRequest(VALUE_REQUEST);

        nativeChannel.createChannelPut(channelPutRequester, pvRequest);

        if (listener instanceof PutListenerImpl) {
            try {
                ((PutListenerImpl) listener).await((long) (1000 * m_dblTmIO), TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Logger.getLogger(Epics7Channel.class.getName()).log(Level.SEVERE, null, ex);
                throw new PutException("Timeout");
            }
        }
    }

    @Override
    public void putRawValCallback(String newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVString pvString = pvStructure.getStringField(Epics7Channel.VALUE_REQUEST);
            pvString.put(newVal);
        });
    }

    @Override
    public void putRawValCallback(byte newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVByte pvByte = pvStructure.getByteField(Epics7Channel.VALUE_REQUEST);
            pvByte.put(newVal);
        }
        );
    }

    @Override
    public void putRawValCallback(short newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVShort pvShort = pvStructure.getShortField(Epics7Channel.VALUE_REQUEST);
            pvShort.put(newVal);
        });
    }

    @Override
    public void putRawValCallback(int newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVInt pvInt = pvStructure.getIntField(Epics7Channel.VALUE_REQUEST);
            pvInt.put(newVal);
        });
    }

    @Override
    public void putRawValCallback(float newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVFloat pvFloat = pvStructure.getFloatField(Epics7Channel.VALUE_REQUEST);
            pvFloat.put(newVal);
        });
    }

    @Override
    public void putRawValCallback(double newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVDouble pvDouble = pvStructure.getDoubleField(Epics7Channel.VALUE_REQUEST);
            pvDouble.put(newVal);
        });
    }

    @Override
    public void putRawValCallback(String[] newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVStringArray pvStringArray = pvStructure.getSubField(PVStringArray.class, Epics7Channel.VALUE_REQUEST);
            pvStringArray.put(0, newVal.length, newVal, 0);
        });
    }

    @Override
    public void putRawValCallback(byte[] newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVByteArray pvByteArray = pvStructure.getSubField(PVByteArray.class, Epics7Channel.VALUE_REQUEST);
            pvByteArray.put(0, newVal.length, newVal, 0);
        });
    }

    @Override
    public void putRawValCallback(short[] newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVShortArray pvShortArray = pvStructure.getSubField(PVShortArray.class, Epics7Channel.VALUE_REQUEST);
            pvShortArray.put(0, newVal.length, newVal, 0);
        });
    }

    @Override
    public void putRawValCallback(int[] newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVIntArray pvIntArray = pvStructure.getSubField(PVIntArray.class, Epics7Channel.VALUE_REQUEST);
            pvIntArray.put(0, newVal.length, newVal, 0);
        });
    }

    @Override
    public void putRawValCallback(float[] newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVFloatArray pvFloatArray = pvStructure.getSubField(PVFloatArray.class, Epics7Channel.VALUE_REQUEST);
            pvFloatArray.put(0, newVal.length, newVal, 0);
        });
    }

    @Override
    public void putRawValCallback(double[] newVal, PutListener listener) throws ConnectionException, PutException {
        putRawValCallback(listener, (pvStructure) -> {
            PVDoubleArray pvDoubleArray = pvStructure.getSubField(PVDoubleArray.class, Epics7Channel.VALUE_REQUEST);
            pvDoubleArray.put(0, newVal.length, newVal, 0);
        });
    }

    //----------------------------------------------------------------------------------
    // The following methods are not implemented because they only work with the 
    // Channel Access protocol.
    @Override
    public String[] getOperationLimitPVs() {
        throw new UnsupportedOperationException("Not supported in EPICS7 (only CA).");
    }

    @Override
    public String[] getWarningLimitPVs() {
        throw new UnsupportedOperationException("Not supported in EPICS7 (only CA).");
    }

    @Override
    public String[] getAlarmLimitPVs() {
        throw new UnsupportedOperationException("Not supported in EPICS7 (only CA).");
    }

    @Override
    public String[] getDriveLimitPVs() {
        throw new UnsupportedOperationException("Not supported in EPICS7 (only CA).");
    }
    //----------------------------------------------------------------------------------

}

class ChannelGetRequesterImpl implements ChannelGetRequester {

    private final EventListener listener;

    public ChannelGetRequesterImpl(EventListener listener) {
        this.listener = listener;
    }

    @Override
    public void channelGetConnect(Status status, ChannelGet channelGet, Structure structure) {
        channelGet.lastRequest();
        channelGet.get();
    }

    @Override
    public void getDone(Status status, ChannelGet channelGet, PVStructure pvStructure, BitSet bitSet) {
        if (status.isSuccess()) {
            listener.event(pvStructure);
        } else {
            Logger.getLogger(Epics7Channel.class.getName()).log(Level.SEVERE,
                    "GetDone was not successful for {0}",
                    channelGet.getChannel().getChannelName());
        }
    }

    @Override
    public String getRequesterName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void message(String message, MessageType messageType) {
        Logger.getLogger(Epics7Channel.class.getName()).log(Level.INFO, message);
    }
}

class ChannelPutRequesterImpl implements ChannelPutRequester {

    private final PutListener listener;
    private final Epics7Channel channel;
    private final EventListener put;

    protected ChannelPutRequesterImpl(PutListener listener, Epics7Channel channel, EventListener put) {
        this.listener = listener;
        this.channel = channel;
        this.put = put;
    }

    @Override
    public void channelPutConnect(Status status, ChannelPut channelPut, Structure structure) {
        channelPut.lastRequest();
        if (status.isSuccess()) {
            PVStructure pvStructure = PVDataFactory.getPVDataCreate().createPVStructure(structure);

            put.event(pvStructure);

            BitSet bitSet = new BitSet(pvStructure.getNumberFields());
            PVField val = pvStructure.getSubField(Epics7Channel.VALUE_REQUEST);
            bitSet.set(val.getFieldOffset());
            channelPut.put(pvStructure, bitSet);
        } else {
            Logger.getLogger(Epics7Channel.class.getName()).severe("channelPutConnect failed");
        }
    }

    @Override
    public void putDone(Status status, ChannelPut channelPut) {
        listener.putCompleted(channel);
    }

    @Override
    public void getDone(Status status, ChannelPut channelPut, PVStructure pvStructure, BitSet bitSet) {
        // Not used.
    }

    @Override
    public String getRequesterName() {
        return channel.getRequesterName();
    }

    @Override
    public void message(String message, MessageType messageType) {
        Logger.getLogger(Epics7Channel.class.getName()).log(Level.INFO, message);
    }
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

    public void await(long timeout, TimeUnit unit) throws InterruptedException {
        doneSignal.await(timeout, unit);
    }
}
