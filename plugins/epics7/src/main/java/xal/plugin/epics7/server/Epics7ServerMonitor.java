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
package xal.plugin.epics7.server;

import org.epics.pva.data.PVAStructure;
import xal.ca.ConnectionException;
import xal.ca.MonitorException;
import xal.plugin.epics7.Epics7Monitor;
import xal.plugin.epics7.EventListener;

/**
 * Monitor implementation for Epics7 server channels.
 *
 * A served channel holds its value in memory rather than fetching it from a remote server, so this monitor does not
 * subscribe to anything. It registers with its {@link Epics7ServerChannel}, which notifies it whenever the value
 * changes, whether the change came from a local put, a Channel Access client or a PV Access client.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerMonitor extends Epics7Monitor {

    private final Epics7ServerChannel serverChannel;

    private Epics7ServerMonitor(Epics7ServerChannel channel, EventListener listener, int intMaskEvent)
            throws ConnectionException {
        super(channel, listener, intMaskEvent);
        this.serverChannel = channel;
    }

    public static Epics7ServerMonitor createNewMonitor(Epics7ServerChannel channel, EventListener listener,
            int intMaskEvent) throws MonitorException {
        Epics7ServerMonitor monitor;
        try {
            monitor = new Epics7ServerMonitor(channel, listener, intMaskEvent);
        } catch (ConnectionException ex) {
            throw new MonitorException("Connection Exception thrown", ex);
        }

        channel.addMonitor(monitor);

        monitor.begin();

        return monitor;
    }

    /**
     * Called by the channel when its value changed.
     */
    void post(PVAStructure pvStructure) {
        dispatch(pvStructure);
    }

    @Override
    public void clear() {
        synchronized (lock) {
            started = false;
        }
        serverChannel.removeMonitor(this);
    }
}
