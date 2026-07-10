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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.epics.pva.data.PVAStructure;
import xal.ca.PutException;

/**
 * In-memory {@link NativeChannel} used to drive {@link Epics7Channel} without touching the network.
 *
 * Connection is reported asynchronously after {@link #CONNECTION_TIME} milliseconds, so that tests can exercise the
 * connect race between the two protocols.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class TestNativeChannel implements NativeChannel {

    public static final int CONNECTION_TIME = 100;

    private final String protocol;
    private final String channelName;
    private final ConnectionListener connectionListener;

    private volatile boolean connected = false;
    private volatile boolean destroyed = false;

    /**
     * Data handed to get and subscribe callbacks. Tests replace it to serve other shapes.
     */
    public volatile PVAStructure data = TestData.doubleRecord(0.0);

    /**
     * Requests seen by {@link #get}, and values seen by {@link #put}.
     */
    public final List<String> getRequests = new CopyOnWriteArrayList<>();
    public final List<String> subscribeRequests = new CopyOnWriteArrayList<>();
    public final List<Object> puts = new CopyOnWriteArrayList<>();

    /**
     * Listeners registered by {@link #subscribe}, so tests can push updates.
     */
    public final List<EventListener> subscribers = new CopyOnWriteArrayList<>();

    public TestNativeChannel(String protocol, String channelName, ConnectionListener connectionListener) {
        this.protocol = protocol;
        this.channelName = channelName;
        this.connectionListener = connectionListener;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getChannelName() {
        return channelName;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void requestConnection() {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(CONNECTION_TIME);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
            if (destroyed) {
                return;
            }
            connected = true;
            connectionListener.connectionChanged(this, true);
        });
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void destroy() {
        destroyed = true;
        connected = false;
    }

    @Override
    public void get(String request, EventListener listener) {
        getRequests.add(request);
        try {
            listener.event(data);
        } catch (PutException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void put(Object newValue, Runnable onComplete) {
        puts.add(newValue);
        if (onComplete != null) {
            onComplete.run();
        }
    }

    @Override
    public NativeMonitor subscribe(String request, EventListener listener) {
        subscribeRequests.add(request);
        subscribers.add(listener);
        return () -> subscribers.remove(listener);
    }

    /**
     * Push an update to every subscriber, as a real server would.
     */
    public void postUpdate(PVAStructure structure) throws PutException {
        for (EventListener listener : new ArrayList<>(subscribers)) {
            listener.event(structure);
        }
    }
}
