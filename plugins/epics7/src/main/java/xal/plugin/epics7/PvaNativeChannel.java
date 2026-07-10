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
import org.epics.pva.client.ClientChannelState;
import org.epics.pva.client.PVAChannel;
import org.epics.pva.client.PVAClient;
import xal.ca.GetException;
import xal.ca.MonitorException;
import xal.ca.PutException;

/**
 * {@link NativeChannel} backed by the PV Access library used by Phoebus.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class PvaNativeChannel implements NativeChannel {

    public static final String PROTOCOL = "pva";

    private static final Logger LOGGER = Logger.getLogger(PvaNativeChannel.class.getName());

    private final PVAClient client;
    private final String channelName;
    private final ConnectionListener connectionListener;

    private volatile PVAChannel channel;

    public PvaNativeChannel(PVAClient client, String channelName, ConnectionListener connectionListener) {
        this.client = client;
        this.channelName = channelName;
        this.connectionListener = connectionListener;
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public String getChannelName() {
        return channelName;
    }

    @Override
    public boolean isConnected() {
        PVAChannel current = channel;
        return current != null && current.isConnected();
    }

    @Override
    public void requestConnection() {
        if (channel == null) {
            // getChannel starts the search straight away.
            channel = client.getChannel(channelName, this::stateChanged);
        }
    }

    private void stateChanged(PVAChannel changed, ClientChannelState state) {
        LOGGER.log(Level.FINE, "pva provider: channel {0} changed status to {1}",
                new Object[]{changed.getName(), state});

        if (state == ClientChannelState.CONNECTED) {
            connectionListener.connectionChanged(this, true);
        } else if (state != ClientChannelState.INIT && state != ClientChannelState.SEARCHING
                && state != ClientChannelState.FOUND) {
            connectionListener.connectionChanged(this, false);
        }
    }

    @Override
    public void destroy() {
        PVAChannel current = channel;
        channel = null;
        if (current != null) {
            current.close();
        }
    }

    private PVAChannel connectedChannel(String operation) throws IllegalStateException {
        PVAChannel current = channel;
        if (current == null) {
            throw new IllegalStateException(operation + ": channel " + channelName + " was never connected");
        }
        return current;
    }

    @Override
    public void get(String request, EventListener listener) throws GetException {
        PVAChannel current;
        try {
            current = connectedChannel("get");
        } catch (IllegalStateException ex) {
            throw new GetException(ex.getMessage());
        }

        current.read(request).whenComplete((data, error) -> {
            if (error != null) {
                LOGGER.log(Level.SEVERE, "Get failed for " + channelName, error);
                return;
            }
            try {
                listener.event(data);
            } catch (PutException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void put(Object newValue, Runnable onComplete) throws PutException {
        PVAChannel current;
        try {
            current = connectedChannel("put");
        } catch (IllegalStateException ex) {
            throw new PutException(ex.getMessage());
        }

        try {
            current.write(false, Epics7Channel.VALUE_REQUEST, newValue).whenComplete((ignored, error) -> {
                if (error != null) {
                    LOGGER.log(Level.SEVERE, "Put failed for " + channelName, error);
                    return;
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        } catch (Exception ex) {
            throw new PutException("Put failed for " + channelName + ": " + ex.getMessage());
        }
    }

    @Override
    public NativeMonitor subscribe(String request, EventListener listener) throws MonitorException {
        PVAChannel current;
        try {
            current = connectedChannel("subscribe");
        } catch (IllegalStateException ex) {
            throw new MonitorException(ex.getMessage());
        }

        final AutoCloseable subscription;
        try {
            subscription = current.subscribe(request, (chan, changes, overruns, data) -> {
                // A null structure signals that the server cancelled the subscription.
                if (data == null) {
                    return;
                }
                try {
                    // The library reuses the structure (and its arrays) after this
                    // callback returns, but ChannelRecords keep a reference to it.
                    listener.event(data.cloneData());
                } catch (PutException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            });
        } catch (Exception ex) {
            throw new MonitorException("Could not subscribe to " + channelName, ex);
        }

        return () -> {
            try {
                subscription.close();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Could not close subscription to " + channelName, ex);
            }
        };
    }
}
