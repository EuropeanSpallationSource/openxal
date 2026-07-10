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
import org.epics.pva.data.PVAStructure;
import xal.ca.ConnectionException;
import xal.ca.MonitorException;
import xal.ca.PutException;

/**
 * Monitor implementation for Epics7.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7Monitor extends xal.ca.Monitor {

    protected NativeMonitor nativeMonitor;
    protected final EventListener listener;

    /**
     * Set from the start, so that an update arriving between subscribing and {@link #begin()} is not lost.
     */
    protected boolean started = true;
    protected final Object lock = new Object();

    protected Epics7Monitor(Epics7Channel channel, EventListener listener, int intMaskEvent) throws ConnectionException {
        super(channel, intMaskEvent);
        this.listener = listener;
    }

    public static Epics7Monitor createNewMonitor(Epics7Channel channel, String request, EventListener listener,
            int intMaskEvent) throws MonitorException {
        Epics7Monitor monitor;
        try {
            monitor = new Epics7Monitor(channel, listener, intMaskEvent);
        } catch (ConnectionException ex) {
            throw new MonitorException("Connection Exception thrown", ex);
        }

        monitor.nativeMonitor = channel.getNativeChannel().subscribe(request, monitor::dispatch);

        monitor.begin();

        return monitor;
    }

    /**
     * Drop updates that arrive after {@link #clear()}; the native library may still deliver a few.
     */
    protected void dispatch(PVAStructure pvStructure) {
        synchronized (lock) {
            if (!started) {
                return;
            }
        }
        try {
            listener.event(pvStructure);
        } catch (PutException ex) {
            Logger.getLogger(Epics7Monitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            started = false;
        }
        if (nativeMonitor != null) {
            nativeMonitor.close();
            nativeMonitor = null;
        }
    }

    @Override
    protected void begin() {
        synchronized (lock) {
            started = true;
        }
    }
}
