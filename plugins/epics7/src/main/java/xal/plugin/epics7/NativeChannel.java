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

import org.epics.pva.data.PVAStructure;
import xal.ca.GetException;
import xal.ca.MonitorException;
import xal.ca.PutException;

/**
 * Protocol-agnostic view of a single native channel.
 *
 * The previous implementation relied on pvAccessJava's {@code ChannelProvider}, which exposed Channel Access and PV
 * Access through one common {@code Channel} interface. The PV Access library used by Phoebus ({@code org.epics.pva})
 * implements PV Access only, so that common view no longer exists and is reconstructed here.
 *
 * Both implementations deliver data as a {@link PVAStructure} shaped like a normative type, so that
 * {@link Epics7ChannelRecord} and its subclasses work unchanged for either protocol.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public interface NativeChannel {

    /**
     * Listener notified when the underlying channel connects or disconnects.
     */
    interface ConnectionListener {

        void connectionChanged(NativeChannel channel, boolean connected);
    }

    /**
     * The protocol serving this channel, used for logging.
     */
    String getProtocol();

    String getChannelName();

    boolean isConnected();

    /**
     * Start connecting. Non-blocking; the listener is notified on state changes.
     */
    void requestConnection();

    /**
     * Release all resources. The channel cannot be reused afterwards.
     */
    void destroy();

    /**
     * Read the requested fields once and deliver them to the listener.
     *
     * @param request comma separated field names, e.g. "value,alarm"
     */
    void get(String request, EventListener listener) throws GetException;

    /**
     * Write a new value to the "value" field.
     *
     * @param newValue a boxed scalar or a Java array
     */
    void put(Object newValue, Runnable onComplete) throws PutException;

    /**
     * Subscribe to the requested fields.
     *
     * The structure handed to the listener is owned by the caller and safe to retain.
     */
    NativeMonitor subscribe(String request, EventListener listener) throws MonitorException;
}
