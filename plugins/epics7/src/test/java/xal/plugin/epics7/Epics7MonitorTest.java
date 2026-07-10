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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link Epics7Monitor} driven by {@link TestNativeChannel}.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7MonitorTest {

    private static final double TIMEOUT = 2.0;

    private Epics7Channel connect(Epics7TestChannelSystem system) {
        Epics7Channel channel = new Epics7Channel("Test", system);
        assertTrue(channel.connectAndWait(TIMEOUT));
        return channel;
    }

    private TestNativeChannel active(Epics7TestChannelSystem system) {
        return system.created.stream().filter(TestNativeChannel::isConnected).findFirst().orElseThrow(AssertionError::new);
    }

    @Test
    public void testSubscribesWithRequest() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect(system);

        Epics7Monitor.createNewMonitor(channel, Epics7Channel.VALUE_REQUEST, pv -> {
        }, xal.ca.Monitor.VALUE);

        assertTrue(active(system).subscribeRequests.contains(Epics7Channel.VALUE_REQUEST));
    }

    @Test
    public void testEventsDeliveredWhileStarted() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect(system);

        AtomicInteger count = new AtomicInteger();
        AtomicReference<org.epics.pva.data.PVAStructure> last = new AtomicReference<>();
        Epics7Monitor.createNewMonitor(channel, Epics7Channel.VALUE_REQUEST, pv -> {
            count.incrementAndGet();
            last.set(pv);
        }, xal.ca.Monitor.VALUE);

        active(system).postUpdate(TestData.doubleRecord(4.0));
        active(system).postUpdate(TestData.doubleRecord(5.0));

        assertEquals(2, count.get());
        assertEquals(5.0, ((org.epics.pva.data.PVADouble) last.get().get("value")).get(), 0.0);
    }

    @Test
    public void testClearStopsEventsAndClosesSubscription() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect(system);

        AtomicInteger count = new AtomicInteger();
        Epics7Monitor monitor = Epics7Monitor.createNewMonitor(channel, Epics7Channel.VALUE_REQUEST,
                pv -> count.incrementAndGet(), xal.ca.Monitor.VALUE);

        monitor.clear();

        // The subscription was closed with the native channel.
        assertTrue(active(system).subscribers.isEmpty());

        // Even a late event does not reach a cleared monitor.
        monitor.dispatch(TestData.doubleRecord(1.0));
        assertEquals(0, count.get());
    }

    @Test
    public void testGetChannelReturnsOwner() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect(system);

        Epics7Monitor monitor = Epics7Monitor.createNewMonitor(channel, Epics7Channel.VALUE_REQUEST, pv -> {
        }, xal.ca.Monitor.VALUE);

        assertEquals(channel, monitor.getChannel());
    }
}
