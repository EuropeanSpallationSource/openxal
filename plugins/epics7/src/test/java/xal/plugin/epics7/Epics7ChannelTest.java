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

import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import xal.ca.ChannelRecord;
import xal.ca.ChannelStatusRecord;
import xal.ca.ChannelTimeRecord;
import xal.ca.GetException;

/**
 * Tests for {@link Epics7Channel} driven by {@link TestNativeChannel}.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelTest {

    private static final double TIMEOUT = 2.0;

    private static final String DEF_PROTOCOL = "defProtocol";

    private Preferences defaults;
    private String savedProtocol;

    /**
     * {@link Epics7Channel} reads the default protocol from the user preferences. If the environment has one configured
     * (e.g. an ESS deployment defaulting to CA or PVA), a bare name would try only that protocol and break the tests
     * that assume both protocols are raced. Pin the preference to the "try both" default and restore it afterwards so
     * the tests are deterministic regardless of the machine configuration.
     */
    @Before
    public void pinDefaultProtocol() {
        defaults = xal.tools.apputils.Preferences.nodeForPackage(xal.ca.Channel.class);
        savedProtocol = defaults.get(DEF_PROTOCOL, null);
        defaults.put(DEF_PROTOCOL, Epics7Channel.C_S_DEF_PROTOCOL);
    }

    @After
    public void restoreDefaultProtocol() {
        if (savedProtocol == null) {
            defaults.remove(DEF_PROTOCOL);
        } else {
            defaults.put(DEF_PROTOCOL, savedProtocol);
        }
    }

    private Epics7Channel connect(String name, Epics7TestChannelSystem system) {
        Epics7Channel channel = new Epics7Channel(name, system);
        assertTrue("channel " + name + " did not connect", channel.connectAndWait(TIMEOUT));
        return channel;
    }

    /**
     * The native channel the {@link Epics7Channel} adopted out of the ones it raced. Reading this from the channel
     * itself is deterministic: scanning the created channels for a connected one could transiently return the losing
     * channel before it is destroyed.
     */
    private TestNativeChannel active(Epics7Channel channel) {
        NativeChannel nativeChannel = channel.getNativeChannel();
        if (nativeChannel == null) {
            throw new AssertionError("no connected native channel");
        }
        return (TestNativeChannel) nativeChannel;
    }

    @Test
    public void testGetNativeChannelStartsNull() {
        Epics7Channel channel = new Epics7Channel("Test", null);
        assertNull(channel.getNativeChannel());
    }

    @Test
    public void testConnectAndWaitTimesOutWhenGivenNoTime() {
        Epics7Channel channel = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());
        // The fake connects after CONNECTION_TIME ms, so a zero timeout cannot succeed.
        assertFalse(channel.connectAndWait(0));
    }

    @Test
    public void testConnectAndWaitSucceeds() {
        Epics7Channel channel = connect("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());
        assertTrue(channel.isConnected());
        // Connecting again on a connected channel stays connected.
        assertTrue(channel.connectAndWait(TIMEOUT));
    }

    /**
     * With no protocol prefix, both a CA and a PVA native channel are created and raced.
     */
    @Test
    public void testBareNameTriesBothProtocols() {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = new Epics7Channel("Test", system);
        channel.requestConnection();
        assertEquals(2, system.created.size());
    }

    @Test
    public void testCaPrefixTriesOnlyCa() {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = new Epics7Channel("ca://Test", system);
        channel.requestConnection();
        assertEquals(1, system.created.size());
        assertEquals(CaNativeChannel.PROTOCOL, system.created.get(0).getProtocol());
        // The protocol prefix is stripped from the native channel name.
        assertEquals("Test", system.created.get(0).getChannelName());
    }

    @Test
    public void testPvaPrefixTriesOnlyPva() {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = new Epics7Channel("pva://Test", system);
        channel.requestConnection();
        assertEquals(1, system.created.size());
        assertEquals(PvaNativeChannel.PROTOCOL, system.created.get(0).getProtocol());
        assertEquals("Test", system.created.get(0).getChannelName());
    }

    /**
     * Once one protocol wins the race, the losing native channel is destroyed.
     */
    @Test
    public void testLosingProtocolIsDestroyed() throws InterruptedException {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        connect("Test", system);

        // Give the slower native channel time to finish connecting and be dropped.
        Thread.sleep(TestNativeChannel.CONNECTION_TIME);

        long connected = system.created.stream().filter(TestNativeChannel::isConnected).count();
        long destroyed = system.created.stream().filter(TestNativeChannel::isDestroyed).count();
        assertEquals(1, connected);
        assertEquals(1, destroyed);
    }

    @Test
    public void testDisconnectDestroysNativeChannels() {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);

        channel.disconnect();

        assertFalse(channel.isConnected());
        assertNull(channel.getNativeChannel());
        assertTrue(system.created.stream().allMatch(TestNativeChannel::isDestroyed));
    }

    @Test
    public void testConnectionListenerNotified() throws InterruptedException {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = new Epics7Channel("Test", system);

        AtomicReference<Boolean> made = new AtomicReference<>();
        channel.addConnectionListener(new xal.ca.ConnectionListener() {
            @Override
            public void connectionMade(xal.ca.Channel c) {
                made.set(Boolean.TRUE);
            }

            @Override
            public void connectionDropped(xal.ca.Channel c) {
                made.set(Boolean.FALSE);
            }
        });

        channel.connectAndWait(TIMEOUT);

        // Connection notifications are dispatched asynchronously through the MessageCenter.
        for (int i = 0; i < 50 && made.get() == null; i++) {
            Thread.sleep(20);
        }
        assertEquals(Boolean.TRUE, made.get());
    }

    @Test
    public void testReadAndWriteAccessAlwaysTrue() throws Exception {
        Epics7Channel channel = connect("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());
        assertTrue(channel.readAccess());
        assertTrue(channel.writeAccess());
    }

    @Test
    public void testElementTypeAndCount() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);
        active(channel).data = TestData.valueOnly(new org.epics.pva.data.PVADoubleArray("value", 1.0, 2.0, 3.0));

        assertEquals(double[].class, channel.elementType());
        assertEquals(3, channel.elementCount());
    }

    @Test
    public void testGetRequestString() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);

        channel.get(Epics7Channel.STATUS_REQUEST);
        assertTrue(active(channel).getRequests.contains(Epics7Channel.STATUS_REQUEST));
    }

    @Test
    public void testGetRawValueRecord() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);
        active(channel).data = TestData.doubleRecord(7.0);

        ChannelRecord record = channel.getRawValueRecord();
        assertEquals(7.0, record.doubleValue(), 0.0);
    }

    @Test
    public void testGetRawStatusRecord() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);
        active(channel).data = TestData.withAlarm(new org.epics.pva.data.PVADouble("value", 1.0), 2, 5);

        ChannelStatusRecord record = channel.getRawStatusRecord();
        assertEquals(2, record.severity());
        assertEquals(5, record.status());
    }

    @Test
    public void testGetRawTimeRecord() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);
        active(channel).data = TestData.withTime(new org.epics.pva.data.PVADouble("value", 1.0), 0, 0,
                java.time.Instant.ofEpochSecond(1000));

        ChannelTimeRecord record = channel.getRawTimeRecord();
        assertEquals(1000.0, record.timeStampInSeconds(), 1e-6);
    }

    @Test
    public void testGetRawValueCallback() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);
        active(channel).data = TestData.doubleRecord(9.0);

        AtomicReference<Double> value = new AtomicReference<>();
        channel.getRawValueCallback((record, chan) -> value.set(record.doubleValue()));
        assertEquals(9.0, value.get(), 0.0);
    }

    @Test
    public void testGetUnitsAndLimits() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);
        active(channel).data = TestData.withMetadata(-10, 10, -8, 8, -6, -4, 4, 6, "mm");

        assertEquals("mm", channel.getUnits());
        assertEquals(-10.0, channel.rawLowerDisplayLimit().doubleValue(), 0.0);
        assertEquals(10.0, channel.rawUpperDisplayLimit().doubleValue(), 0.0);
        assertEquals(-8.0, channel.rawLowerControlLimit().doubleValue(), 0.0);
        assertEquals(8.0, channel.rawUpperControlLimit().doubleValue(), 0.0);
        assertEquals(-6.0, channel.rawLowerAlarmLimit().doubleValue(), 0.0);
        assertEquals(6.0, channel.rawUpperAlarmLimit().doubleValue(), 0.0);
        assertEquals(-4.0, channel.rawLowerWarningLimit().doubleValue(), 0.0);
        assertEquals(4.0, channel.rawUpperWarningLimit().doubleValue(), 0.0);
    }

    /**
     * A get that returns no display field must raise, so callers know the metadata is unavailable.
     */
    @Test
    public void testMissingDisplayFieldThrows() {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);
        active(channel).data = TestData.doubleRecord(1.0);

        assertThrows(GetException.class, channel::getUnits);
        assertThrows(GetException.class, channel::rawLowerControlLimit);
        assertThrows(GetException.class, channel::rawLowerAlarmLimit);
    }

    @Test
    public void testPutScalar() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);

        channel.putRawValCallback(3.5, null);
        assertEquals(3.5, active(channel).puts.get(0));
    }

    @Test
    public void testPutArray() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);

        double[] values = {1.0, 2.0, 3.0};
        channel.putRawValCallback(values, null);
        assertArrayEquals(values, (double[]) active(channel).puts.get(0), 0.0);
    }

    @Test
    public void testPutNotifiesListener() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);

        AtomicReference<xal.ca.Channel> completed = new AtomicReference<>();
        channel.putRawValCallback(1, completed::set);
        assertSame(channel, completed.get());
    }

    @Test
    public void testPutOfEachScalarType() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);
        TestNativeChannel nativeChannel = active(channel);

        channel.putRawValCallback("text", null);
        channel.putRawValCallback((byte) 1, null);
        channel.putRawValCallback((short) 2, null);
        channel.putRawValCallback(3, null);
        channel.putRawValCallback(4L, null);
        channel.putRawValCallback(5.0f, null);
        channel.putRawValCallback(6.0, null);

        assertEquals("text", nativeChannel.puts.get(0));
        assertEquals((byte) 1, nativeChannel.puts.get(1));
        assertEquals((short) 2, nativeChannel.puts.get(2));
        assertEquals(3, nativeChannel.puts.get(3));
        assertEquals(4L, nativeChannel.puts.get(4));
        assertEquals(5.0f, nativeChannel.puts.get(5));
        assertEquals(6.0, nativeChannel.puts.get(6));
    }

    @Test
    public void testAddMonitorValueReceivesUpdates() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);

        AtomicReference<Double> latest = new AtomicReference<>();
        channel.addMonitorValue((record, chan) -> latest.set(record.doubleValue()), xal.ca.Monitor.VALUE);

        active(channel).postUpdate(TestData.doubleRecord(11.0));
        assertEquals(11.0, latest.get(), 0.0);
    }

    @Test
    public void testClearedMonitorStopsReceiving() throws Exception {
        Epics7TestChannelSystem system = Epics7TestChannelSystem.newEpics7ChannelSystem();
        Epics7Channel channel = connect("Test", system);

        AtomicReference<Double> latest = new AtomicReference<>();
        xal.ca.Monitor monitor = channel.addMonitorValue(
                (record, chan) -> latest.set(record.doubleValue()), xal.ca.Monitor.VALUE);

        monitor.clear();
        active(channel).postUpdate(TestData.doubleRecord(11.0));
        assertNull(latest.get());
    }

    @Test
    public void testUnsupportedLimitPvMethods() {
        Epics7Channel channel = new Epics7Channel("Test", null);
        assertThrows(UnsupportedOperationException.class, channel::getOperationLimitPVs);
        assertThrows(UnsupportedOperationException.class, channel::getWarningLimitPVs);
        assertThrows(UnsupportedOperationException.class, channel::getAlarmLimitPVs);
        assertThrows(UnsupportedOperationException.class, channel::getDriveLimitPVs);
    }
}
