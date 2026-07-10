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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.epics.pva.data.PVAStructure;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import xal.ca.Monitor;
import xal.plugin.epics7.TestData;

/**
 * Tests for {@link Epics7ServerMonitor}. Unlike the base {@link xal.plugin.epics7.Epics7Monitor}, a server monitor does
 * not subscribe to a native channel; it registers with its {@link Epics7ServerChannel} and is notified through
 * {@link Epics7ServerMonitor#post} whenever the served value changes.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerMonitorTest {

    private static Epics7ServerChannelSystem system;

    @BeforeClass
    public static void setUpClass() {
        system = Epics7ServerChannelSystem.newEpics7ServerChannelSystem();
    }

    @AfterClass
    public static void tearDownClass() {
        if (system != null) {
            system.dispose();
        }
    }

    private Epics7ServerChannel channel(String name) {
        return new Epics7ServerChannel(name, system);
    }

    /**
     * A posted value is forwarded to the listener unchanged.
     */
    @Test
    public void testPostDispatchesToListener() throws Exception {
        Epics7ServerChannel channel = channel("SrvMonPost");
        AtomicReference<PVAStructure> received = new AtomicReference<>();

        Epics7ServerMonitor monitor = Epics7ServerMonitor.createNewMonitor(channel, received::set, Monitor.VALUE);

        PVAStructure data = TestData.doubleRecord(4.0);
        monitor.post(data);

        assertSame(data, received.get());
    }

    /**
     * Creating the monitor registers it with the channel, so a later put reaches the listener.
     */
    @Test
    public void testCreateRegistersWithChannel() throws Exception {
        Epics7ServerChannel channel = channel("SrvMonRegister");
        AtomicInteger count = new AtomicInteger();

        Epics7ServerMonitor.createNewMonitor(channel, pv -> count.incrementAndGet(), Monitor.VALUE);
        channel.putRawValCallback(1.0, null);

        assertTrue(count.get() >= 1);
    }

    /**
     * After {@link Epics7ServerMonitor#clear()} the monitor is unregistered: neither a further put nor a direct post
     * reaches the listener.
     */
    @Test
    public void testClearUnregistersAndStopsEvents() throws Exception {
        Epics7ServerChannel channel = channel("SrvMonClear");
        AtomicInteger count = new AtomicInteger();

        Epics7ServerMonitor monitor = Epics7ServerMonitor.createNewMonitor(channel, pv -> count.incrementAndGet(),
                Monitor.VALUE);

        channel.putRawValCallback(1.0, null);
        int afterFirstPut = count.get();
        assertTrue(afterFirstPut >= 1);

        monitor.clear();

        // A put no longer notifies the cleared monitor...
        channel.putRawValCallback(2.0, null);
        assertEquals(afterFirstPut, count.get());

        // ...and a direct post is dropped as well.
        monitor.post(TestData.doubleRecord(3.0));
        assertEquals(afterFirstPut, count.get());
    }

    @Test
    public void testGetChannelReturnsOwner() throws Exception {
        Epics7ServerChannel channel = channel("SrvMonOwner");

        Epics7ServerMonitor monitor = Epics7ServerMonitor.createNewMonitor(channel, pv -> {
        }, Monitor.VALUE);

        assertEquals(channel, monitor.getChannel());
    }
}
