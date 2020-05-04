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
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;
import xal.ca.ConnectionException;

/**
 * Monitor implementation for Epics7.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7Monitor extends xal.ca.Monitor {

    private volatile Monitor nativeMonitor;

    protected Epics7Monitor(Epics7Channel channel, String request, EventListener listener, int intMaskEvent) throws ConnectionException {
        super(channel, intMaskEvent);

        MonitorRequester monitorRequester = new MonitorRequesterImpl(listener, this);
        CreateRequest createRequest = CreateRequest.create();
        PVStructure pvRequest = createRequest.createRequest(request);

        nativeMonitor = channel.getNativeChannel().createMonitor(monitorRequester, pvRequest);
    }

    @Override
    public void clear() {
        nativeMonitor.stop();
    }

    @Override
    protected void begin() throws xal.ca.MonitorException {
        // Not used. The monitor starts when the channel is connected.
    }

    class MonitorRequesterImpl implements MonitorRequester {

        private final EventListener listener;
        private final xal.ca.Monitor monitor;

        public MonitorRequesterImpl(EventListener listener, xal.ca.Monitor monitor) {
            this.listener = listener;
            this.monitor = monitor;
        }

        @Override
        public void monitorConnect(Status status, Monitor monitor, Structure structure) {
            monitor.start();
        }

        @Override
        public void monitorEvent(Monitor monitor) {
            MonitorElement element;
            while ((element = monitor.poll()) != null) {
                listener.event(element.getPVStructure());

                monitor.release(element);
            }
        }

        @Override
        public void unlisten(Monitor monitor) {
            this.monitor.clear();
        }

        @Override
        public String getRequesterName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void message(String message, MessageType messageType) {
            Logger.getLogger(Epics7Monitor.class.getName()).log(Level.INFO, message);
        }
    }
}
