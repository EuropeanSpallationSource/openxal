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

import org.epics.pvaccess.client.AccessRights;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelArray;
import org.epics.pvaccess.client.ChannelArrayRequester;
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.client.ChannelProcess;
import org.epics.pvaccess.client.ChannelProcessRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvaccess.client.ChannelPutGet;
import org.epics.pvaccess.client.ChannelPutGetRequester;
import org.epics.pvaccess.client.ChannelPutRequester;
import org.epics.pvaccess.client.ChannelRPC;
import org.epics.pvaccess.client.ChannelRPCRequester;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.GetFieldRequester;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class TestChannel implements Channel {

    private final ChannelProvider provider;
    private final String name;

    public TestChannel(String name, ChannelProvider provider) {
        this.name = name;
        this.provider = provider;
    }

    @Override
    public ChannelProvider getProvider() {
        return provider;
    }

    @Override
    public String getRemoteAddress() {
        return "";
    }

    @Override
    public Channel.ConnectionState getConnectionState() {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void destroy() {
        //
    }

    @Override
    public String getChannelName() {
        return name;
    }

    @Override
    public ChannelRequester getChannelRequester() {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isConnected() {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getField(GetFieldRequester requester, String subField) {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AccessRights getAccessRights(PVField pvField) {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ChannelProcess createChannelProcess(ChannelProcessRequester channelProcessRequester, PVStructure pvRequest) {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ChannelGet createChannelGet(ChannelGetRequester channelGetRequester, PVStructure pvRequest) {
        return null;
    }

    @Override
    public ChannelPut createChannelPut(ChannelPutRequester channelPutRequester, PVStructure pvRequest) {
        channelPutRequester.putDone(null, null);
        return null;
    }

    @Override
    public ChannelPutGet createChannelPutGet(ChannelPutGetRequester channelPutGetRequester, PVStructure pvRequest) {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ChannelRPC createChannelRPC(ChannelRPCRequester channelRPCRequester, PVStructure pvRequest) {
        return null;
    }

    @Override
    public Monitor createMonitor(MonitorRequester monitorRequester, PVStructure pvRequest) {
        Monitor monitor = new TestMonitor();
        monitorRequester.monitorConnect(StatusFactory.getStatusCreate().getStatusOK(), monitor, pvRequest.getStructure());
        monitorRequester.monitorEvent(monitor);
        return monitor;
    }

    @Override
    public ChannelArray createChannelArray(ChannelArrayRequester channelArrayRequester, PVStructure pvRequest) {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRequesterName() {
        return "TestRequester";
    }

    @Override
    public void message(String message, MessageType messageType) {
        //
    }
}
