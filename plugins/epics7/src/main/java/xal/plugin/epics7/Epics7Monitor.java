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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pvaccess.client.Channel;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;
import xal.ca.ConnectionException;
import xal.ca.MonitorException;
import xal.ca.PutException;

/**
 * Monitor implementation for Epics7.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7Monitor extends xal.ca.Monitor implements MonitorRequester {

    protected Channel nativeChannel;
    protected Monitor nativeMonitor;
    protected final EventListener listener;

    protected boolean started = false;
    protected final Object lock = new Object();

    protected Epics7Monitor(Epics7Channel channel, EventListener listener, int intMaskEvent) throws ConnectionException {
        super(channel, intMaskEvent);
        this.listener = listener;

    }

    public static Epics7Monitor createNewMonitor(Epics7Channel channel, String request, EventListener listener, int intMaskEvent) throws MonitorException {
        Epics7Monitor monitor;
        try {
            monitor = new Epics7Monitor(channel, listener, intMaskEvent);
        } catch (ConnectionException ex) {
            throw new MonitorException("Connection Exception thrown", ex);
        }

        monitor.createRequest(channel, request);
        
        monitor.begin();

        return monitor;
    }

    private void createRequest(Epics7Channel channel, String request) {
        CreateRequest createRequest = CreateRequest.create();
        PVStructure pvRequest = createRequest.createRequest(request);

        nativeChannel = channel.getNativeChannel();
        nativeMonitor = nativeChannel.createMonitor(this, pvRequest);
    }

    @Override
    public void clear() {
        synchronized (lock) {
            started = false;
            nativeMonitor.stop();
        }
    }

    @Override
    protected void begin() {
        synchronized (lock) {
            started = true;
        }
    }

    //---------------- Implementing MonitorRequester abstract methods ------------------
    @Override
    public void monitorConnect(Status status, Monitor monitor, Structure structure) {
        synchronized (lock) {
            started = true;
        }
        monitor.start();
    }

    @Override
    public void monitorEvent(Monitor monitor) {
        // clear monitor if clear method was called.
        synchronized (lock) {
            if (!started) {
                unlisten(monitor);
                return;
            }
        }

        MonitorElement element;
        while ((element = monitor.poll()) != null) {
            try {
                listener.event(element.getPVStructure());
            } catch (PutException ex) {
                Logger.getLogger(Epics7Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            monitor.release(element);
        }
    }

    @Override
    public void unlisten(Monitor monitor) {
        monitor.destroy();
    }

    @Override
    public String getRequesterName() {
        if (nativeChannel != null) {
            return nativeChannel.getRequesterName();
        }
        return null;
    }

    @Override
    public void message(String message, MessageType messageType) {
        Logger.getLogger(Epics7Monitor.class.getName()).log(Level.INFO, message);
    }
    //---------------------------------------------------------------------------------
}
