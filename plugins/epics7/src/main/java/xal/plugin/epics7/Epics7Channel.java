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
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
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
import xal.ca.Monitor;
import xal.ca.MonitorException;
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

    private final Epics7ChannelSystem CHANNEL_SYSTEM;

    //  Constants
    public static final double C_DBL_DEF_TIME_IO = 5.0;       // default pend IO timeout
    public static final double C_DBL_DEF_TIME_EVENT = 0.1;    // default pend event timeout

    // Property names
    private static final String DEF_TIME_IO = "c_dblDefTimeIO";
    private static final String DEF_TIME_EVENT = "c_dblDefTimeEvent";

    // Request can contain the following fields: value, alarm, timeStamp, display, control
    static final String VALUE_REQUEST = "value";
    static final String STATUS_REQUEST = "value,alarm";
    static final String TIME_REQUEST = "value,alarm,timeStamp";

    private static final String CA_PREFIX = "ca://";
    private static final String PVA_PREFIX = "pva://";

    private volatile Channel caChannel;
    private volatile Channel pvaChannel;
    private volatile Channel nativeChannel;

    private final Object connectionLock = new Object();

    private CountDownLatch connectionLatch;

    Epics7Channel(String signalName, Epics7ChannelSystem CHANNEL_SYSTEM) {
        super(signalName);

        this.CHANNEL_SYSTEM = CHANNEL_SYSTEM;

        // Load default timeouts from preferences if available, otherwise use hardcoded values.
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(xal.ca.Channel.class);
        m_dblTmIO = defaults.getDouble(DEF_TIME_IO, C_DBL_DEF_TIME_IO);
        m_dblTmEvt = defaults.getDouble(DEF_TIME_EVENT, C_DBL_DEF_TIME_EVENT);
    }

    @Override
    public boolean connectAndWait(double timeout) {
        requestConnection();

        // If not connected, wait for timeout.
        if (!isConnected()) {
            try {
                connectionLatch.await((long) timeout, TimeUnit.SECONDS);
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
                    pvaChannel = CHANNEL_SYSTEM.getPvaChannelProvider().createChannel(
                            m_strId.startsWith(PVA_PREFIX) ? m_strId.substring(PVA_PREFIX.length()) : m_strId, this, ChannelProvider.PRIORITY_DEFAULT);
                }
            }
            if (!m_strId.startsWith(PVA_PREFIX)) {
                synchronized (connectionLock) {
                    caChannel = CHANNEL_SYSTEM.getCaChannelProvider().createChannel(
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
        if (cs == Channel.ConnectionState.CONNECTED) {
            // If the other channel is connected, destroy the channel that invoked this method.
            // Otherwise, use use it.
            synchronized (connectionLock) {
                if (chnl == caChannel) {
                    if (connectionFlag) {
                        if (caChannel != null) {
                            caChannel.destroy();
                        }
                        return;
                    } else {
                        nativeChannel = caChannel;
                    }
                } else if (chnl == pvaChannel) {
                    if (connectionFlag) {
                        if (pvaChannel != null) {
                            pvaChannel.destroy();
                        }
                        return;
                    } else {
                        nativeChannel = pvaChannel;
                    }
                } else {
                    throw new RuntimeException();
                }
            }
            // Notify listeners.
            if (connectionProxy != null) {
                connectionProxy.connectionMade(this);
            }

            connectionFlag = true;

            // Releasing the connection latch.
            connectionLatch.countDown();
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
        Logger.getLogger(Epics7Channel.class.getName()).log(Level.INFO, "{0} provider created a channel: {1}", new Object[]{chnl.getProvider().getProviderName(), chnl.getChannelName()});
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

    @Override
    public Class<?> elementType() throws ConnectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int elementCount() throws ConnectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean readAccess() throws ConnectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean writeAccess() throws ConnectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getUnits() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getOperationLimitPVs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getWarningLimitPVs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getAlarmLimitPVs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getDriveLimitPVs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Number rawUpperDisplayLimit() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Number rawLowerDisplayLimit() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Number rawUpperAlarmLimit() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Number rawLowerAlarmLimit() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Number rawUpperWarningLimit() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Number rawLowerWarningLimit() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Number rawUpperControlLimit() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Number rawLowerControlLimit() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private ChannelRecord get(String request, boolean attemptConnection) throws ConnectionException, GetException {
        GetListener listener = new GetListener();

        getCallback(request, listener, attemptConnection);

        try {
            listener.await((long) m_dblTmIO, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(Epics7Channel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return listener.getRecord();
    }

    private void getCallback(String request, IEventSinkValue listener, boolean attemptConnection) throws ConnectionException, GetException {
        if (attemptConnection) {
            connectAndWait();
        }
        if (isConnected()) {
            ChannelGetRequesterImpl channelGetRequester = new ChannelGetRequesterImpl(this, request,
                    (record, channel) -> {
                        listener.eventValue(record, channel);
                    });

            CreateRequest createRequest = CreateRequest.create();
            PVStructure pvRequest = createRequest.createRequest(request);
            if (pvRequest != null) {
                nativeChannel.createChannelGet(channelGetRequester, pvRequest);
            }
        }
    }

    @Override
    public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
        return get(VALUE_REQUEST, true);
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener) throws ConnectionException, GetException {
        getCallback(VALUE_REQUEST, listener, true);
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener, boolean attemptConnection) throws ConnectionException, GetException {
        getCallback(VALUE_REQUEST,
                (record, channel) -> {
                    listener.eventValue(record, channel);
                }, attemptConnection);
    }

    @Override
    protected ChannelRecord getRawStringValueRecord() throws ConnectionException, GetException {
        return getRawValueRecord();
    }

    @Override
    public ChannelStatusRecord getRawStatusRecord() throws ConnectionException, GetException {
        return (ChannelStatusRecord) get(STATUS_REQUEST, true);
    }

    @Override
    protected ChannelStatusRecord getRawStringStatusRecord() throws ConnectionException, GetException {
        return getRawStatusRecord();
    }

    @Override
    public ChannelTimeRecord getRawTimeRecord() throws ConnectionException, GetException {
        return (ChannelTimeRecord) get(TIME_REQUEST, true);
    }

    @Override
    protected ChannelTimeRecord getRawStringTimeRecord() throws ConnectionException, GetException {
        return getRawTimeRecord();
    }

    @Override
    public void getRawValueTimeCallback(IEventSinkValTime listener, boolean attemptConnection) throws ConnectionException, GetException {
        getCallback(TIME_REQUEST,
                (record, channel) -> {
                    listener.eventValue((ChannelTimeRecord) record, channel);
                }, true);
    }

    @Override
    public Monitor addMonitorValTime(IEventSinkValTime listener, int intMaskFire) throws ConnectionException, MonitorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Monitor addMonitorValStatus(IEventSinkValStatus listener, int intMaskFire) throws ConnectionException, MonitorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Monitor addMonitorValue(IEventSinkValue listener, int intMaskFire) throws ConnectionException, MonitorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putRawValCallback(String newVal, PutListener listener) throws ConnectionException, PutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putRawValCallback(byte newVal, PutListener listener) throws ConnectionException, PutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putRawValCallback(short newVal, PutListener listener) throws ConnectionException, PutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putRawValCallback(int newVal, PutListener listener) throws ConnectionException, PutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putRawValCallback(float newVal, PutListener listener) throws ConnectionException, PutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putRawValCallback(double newVal, PutListener listener) throws ConnectionException, PutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putRawValCallback(byte[] newVal, PutListener listener) throws ConnectionException, PutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putRawValCallback(short[] newVal, PutListener listener) throws ConnectionException, PutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putRawValCallback(int[] newVal, PutListener listener) throws ConnectionException, PutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putRawValCallback(float[] newVal, PutListener listener) throws ConnectionException, PutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putRawValCallback(double[] newVal, PutListener listener) throws ConnectionException, PutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

class GetListener implements IEventSinkValue {

    private CountDownLatch doneSignal;
    private ChannelRecord record;

    public GetListener() {
        this.doneSignal = new CountDownLatch(1);
    }

    public ChannelRecord getRecord() {
        return record;
    }

    @Override
    public void eventValue(ChannelRecord record, xal.ca.Channel channel) {
        this.record = record;
        doneSignal.countDown();
    }

    public void await(long timeout, TimeUnit unit) throws InterruptedException {
        doneSignal.await(timeout, unit);
    }
}

class ChannelGetRequesterImpl implements ChannelGetRequester {

    private final EventListener listener;
    private final xal.ca.Channel channel;
    private final String request;

    public ChannelGetRequesterImpl(xal.ca.Channel channel, String request, EventListener listener) {
        this.channel = channel;
        this.request = request;
        this.listener = listener;
    }

    @Override
    public void channelGetConnect(Status status, ChannelGet channelGet, Structure structure) {
        channelGet.get();
    }

    @Override
    public void getDone(Status status, ChannelGet channelGet, PVStructure pvStructure, BitSet bitSet) {
        ChannelRecord record = null;
        switch (request) {
            case Epics7Channel.VALUE_REQUEST:
                record = new Epics7ChannelRecord(pvStructure, channel.channelName());
                break;
            case Epics7Channel.STATUS_REQUEST:
                record = new Epics7ChannelStatusRecord(pvStructure, channel.channelName());
                break;
            case Epics7Channel.TIME_REQUEST:
                record = new Epics7ChannelTimeRecord(pvStructure, channel.channelName());
                break;
            default:
                break;
        }

        listener.event(record, channel);
    }

    @Override
    public String getRequesterName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void message(String message, MessageType messageType) {
        Logger.getLogger(Epics7Channel.class.getName()).log(Level.INFO, message);
    }
}

interface EventListener {

    public void event(ChannelRecord record, xal.ca.Channel channel);
}
