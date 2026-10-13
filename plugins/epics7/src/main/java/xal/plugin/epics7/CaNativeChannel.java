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

import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.CAStatus;
import gov.aps.jca.dbr.DBRType;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.GetException;
import xal.ca.MonitorException;
import xal.ca.PutException;

/**
 * {@link NativeChannel} speaking the Channel Access protocol through JCA/CAJ, as Phoebus does in core-pv-ca. Values are
 * converted to {@link org.epics.pva.data.PVAStructure} by {@link CaDbrConverter}, so callers cannot tell the two
 * protocols apart.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class CaNativeChannel implements NativeChannel {

    public static final String PROTOCOL = "ca";

    private static final Logger LOGGER = Logger.getLogger(CaNativeChannel.class.getName());

    /**
     * Event mask used when the caller passes none. {@link xal.ca.Monitor} masks use the same bits as Channel Access.
     */
    private static final int DEFAULT_MONITOR_MASK = gov.aps.jca.Monitor.VALUE | gov.aps.jca.Monitor.ALARM;

    private final Context context;
    private final String channelName;
    private final ConnectionListener connectionListener;

    private volatile gov.aps.jca.Channel channel;

    public CaNativeChannel(Context context, String channelName, ConnectionListener connectionListener) {
        this.context = context;
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
        gov.aps.jca.Channel current = channel;
        return current != null
                && current.getConnectionState() == gov.aps.jca.Channel.ConnectionState.CONNECTED;
    }

    @Override
    public void requestConnection() {
        if (channel != null) {
            return;
        }
        try {
            channel = context.createChannel(channelName, event -> {
                LOGGER.log(Level.FINE, "ca provider: channel {0} changed status to {1}",
                        new Object[]{channelName, event.isConnected() ? "CONNECTED" : "DISCONNECTED"});
                connectionListener.connectionChanged(this, event.isConnected());
            });
            context.flushIO();
        } catch (CAException ex) {
            LOGGER.log(Level.SEVERE, "Could not create CA channel " + channelName, ex);
        }
    }

    @Override
    public void destroy() {
        gov.aps.jca.Channel current = channel;
        channel = null;
        if (current != null) {
            try {
                current.destroy();
            } catch (CAException | IllegalStateException ex) {
                LOGGER.log(Level.FINE, "Could not destroy CA channel " + channelName, ex);
            }
        }
    }

    private gov.aps.jca.Channel connectedChannel(String operation) throws IllegalStateException {
        gov.aps.jca.Channel current = channel;
        if (current == null) {
            throw new IllegalStateException(operation + ": channel " + channelName + " was never connected");
        }
        return current;
    }

    @Override
    public void get(String request, EventListener listener, Consumer<String> onFailure) throws GetException {
        gov.aps.jca.Channel current;
        try {
            current = connectedChannel("get");
        } catch (IllegalStateException ex) {
            throw new GetException(ex.getMessage());
        }

        DBRType type = CaDbrConverter.requestType(current.getFieldType(), request);
        try {
            current.get(type, current.getElementCount(), event -> {
                if (event.getDBR() == null || !event.getStatus().isSuccessful()) {
                    onFailure.accept("Get failed for " + channelName + ": " + event.getStatus());
                    return;
                }
                try {
                    listener.event(CaDbrConverter.toStructure(event.getDBR()));
                } catch (PutException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            });
            context.flushIO();
        } catch (CAException | IllegalStateException ex) {
            throw new GetException("Get failed for " + channelName + ": " + ex.getMessage());
        }
    }

    @Override
    public void put(Object newValue, Runnable onComplete, Consumer<String> onFailure) throws PutException {
        gov.aps.jca.Channel current;
        try {
            current = connectedChannel("put");
        } catch (IllegalStateException ex) {
            throw new PutException(ex.getMessage());
        }

        gov.aps.jca.event.PutListener putListener = event -> {
            CAStatus status = event.getStatus();
            if (status != null && !status.isSuccessful()) {
                onFailure.accept("Put failed for " + channelName + ": " + status);
            } else if (onComplete != null) {
                onComplete.run();
            }
        };

        try {
            write(current, newValue, putListener);
            context.flushIO();
        } catch (CAException | IllegalStateException ex) {
            throw new PutException("Put failed for " + channelName + ": " + ex.getMessage());
        }
    }

    /**
     * Channel Access has no 64 bit integer type, so longs are narrowed to int, as the previous implementation did.
     */
    private static void write(gov.aps.jca.Channel channel, Object value, gov.aps.jca.event.PutListener listener)
            throws CAException {
        if (value instanceof Double) {
            channel.put((Double) value, listener);
        } else if (value instanceof Float) {
            channel.put((Float) value, listener);
        } else if (value instanceof Integer) {
            channel.put((Integer) value, listener);
        } else if (value instanceof Long) {
            channel.put((int) (long) (Long) value, listener);
        } else if (value instanceof Short) {
            channel.put((Short) value, listener);
        } else if (value instanceof Byte) {
            channel.put((Byte) value, listener);
        } else if (value instanceof String) {
            channel.put((String) value, listener);
        } else if (value instanceof double[]) {
            channel.put((double[]) value, listener);
        } else if (value instanceof float[]) {
            channel.put((float[]) value, listener);
        } else if (value instanceof int[]) {
            channel.put((int[]) value, listener);
        } else if (value instanceof long[]) {
            long[] longs = (long[]) value;
            int[] ints = new int[longs.length];
            for (int i = 0; i < longs.length; i++) {
                ints[i] = (int) longs[i];
            }
            channel.put(ints, listener);
        } else if (value instanceof short[]) {
            channel.put((short[]) value, listener);
        } else if (value instanceof byte[]) {
            channel.put((byte[]) value, listener);
        } else if (value instanceof String[]) {
            channel.put((String[]) value, listener);
        } else {
            throw new IllegalArgumentException("Cannot write " + value + " over Channel Access");
        }
    }

    @Override
    public NativeMonitor subscribe(String request, int mask, EventListener listener) throws MonitorException {
        gov.aps.jca.Channel current;
        try {
            current = connectedChannel("subscribe");
        } catch (IllegalStateException ex) {
            throw new MonitorException(ex.getMessage());
        }

        DBRType type = CaDbrConverter.requestType(current.getFieldType(), request);
        gov.aps.jca.event.MonitorListener monitorListener = event -> {
            if (event.getDBR() == null) {
                return;
            }
            try {
                listener.event(CaDbrConverter.toStructure(event.getDBR()));
            } catch (PutException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        };

        final gov.aps.jca.Monitor monitor;
        try {
            monitor = current.addMonitor(type, current.getElementCount(), mask == 0 ? DEFAULT_MONITOR_MASK : mask,
                    monitorListener);
            context.flushIO();
        } catch (CAException | IllegalStateException ex) {
            throw new MonitorException("Could not subscribe to " + channelName, ex);
        }

        return () -> {
            try {
                monitor.clear();
                context.flushIO();
            } catch (CAException | IllegalStateException ex) {
                LOGGER.log(Level.WARNING, "Could not clear CA monitor on " + channelName, ex);
            }
        };
    }
}
