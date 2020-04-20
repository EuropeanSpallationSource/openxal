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
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.Status;
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
 * This {@link xal.ca.Channel} implementation can connect to ChannelAccess or PV Access. If the
 * PV signal starts with 'ca://', it will only connect to CA; if it starts with
 * 'pva://', it will only connect to PVA; otherwise it tries to connect to both
 * and uses the protocol that replies first.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7Channel extends xal.ca.Channel implements ChannelRequester {

    private final Epics7ChannelSystem CHANNEL_SYSTEM;

    private volatile Channel caChannel;
    private volatile Channel pvaChannel;
    private volatile Channel nativeChannel;

    private final Object connectionLock = new Object();

    private CountDownLatch connectionLatch;

    private static final String CA_PREFIX = "ca://";
    private static final String PVA_PREFIX = "pva://";

    Epics7Channel(String signalName, Epics7ChannelSystem CHANNEL_SYSTEM) {
        super(signalName);

        this.CHANNEL_SYSTEM = CHANNEL_SYSTEM;
    }

    @Override
    public boolean connectAndWait(double timeout) {
        if (connectionLatch == null || connectionLatch.getCount() == 0) {
            requestConnection();

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

    //--------------- Implementing Channel Requester abstract methods ------------------
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
    public String getRequesterName() {
        if (nativeChannel != null) {
            return nativeChannel.getRequesterName();
        }
        return null;
    }

    @Override
    public void channelCreated(Status status, Channel chnl) {
        Logger.getLogger(Epics7Channel.class.getName()).log(Level.INFO, "{0} provider created a channel: {1}", new Object[]{chnl.getProvider().getProviderName(), chnl.getChannelName()});
    }

    @Override
    public void message(String message, MessageType mt) {
        Logger.getLogger(Epics7Channel.class.getName()).log(Level.INFO, message);
//        Logger.getLogger(Epics7Channel.class.getName()).log(Level.INFO, "Channel {0} received the following message:\n{1}", new Object[]{nativeChannel.getChannelName(), message});
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

    @Override
    public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ChannelRecord getRawStringValueRecord() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ChannelStatusRecord getRawStringStatusRecord() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ChannelTimeRecord getRawStringTimeRecord() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChannelStatusRecord getRawStatusRecord() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChannelTimeRecord getRawTimeRecord() throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener) throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void getRawValueCallback(IEventSinkValue listener, boolean attemptConnection) throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getRawValueTimeCallback(IEventSinkValTime listener, boolean attemptConnection) throws ConnectionException, GetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
