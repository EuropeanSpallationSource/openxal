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

import java.util.concurrent.atomic.AtomicReference;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import xal.ca.ChannelRecord;
import xal.ca.ChannelTimeRecord;

/**
 * Tests for {@link Epics7ServerChannel}, which holds its value in memory and serves it over both protocols. The tests
 * drive the local put/get API; they do not open network connections.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannelTest {

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

    @Test
    public void testConnectsWithoutWaiting() {
        Epics7ServerChannel channel = channel("SrvConnect");
        assertTrue(channel.connectAndWait(0));
        assertTrue(channel.isConnected());
        channel.disconnect();
        assertFalse(channel.isConnected());
    }

    @Test
    public void testPrefixIsStripped() {
        Epics7ServerChannel channel = channel("ca://SrvPrefix");
        assertEquals("SrvPrefix", channel.channelName());
    }

    @Test
    public void testPutGetDoubleRoundTrip() throws Exception {
        Epics7ServerChannel channel = channel("SrvDouble");
        channel.putRawValCallback(3.5, null);

        ChannelRecord record = channel.getRawValueRecord();
        assertEquals(3.5, record.doubleValue(), 0.0);
        assertEquals(double.class, channel.elementType());
        assertEquals(1, channel.elementCount());
    }

    @Test
    public void testPutGetIntRoundTrip() throws Exception {
        Epics7ServerChannel channel = channel("SrvInt");
        channel.putRawValCallback(42, null);

        assertEquals(42, channel.getRawValueRecord().intValue());
        assertEquals(int.class, channel.elementType());
    }

    @Test
    public void testPutGetStringRoundTrip() throws Exception {
        Epics7ServerChannel channel = channel("SrvString");
        channel.putRawValCallback("hello", null);

        assertEquals("hello", channel.getRawValueRecord().stringValue());
        assertEquals(String.class, channel.elementType());
    }

    @Test
    public void testPutGetDoubleArrayRoundTrip() throws Exception {
        Epics7ServerChannel channel = channel("SrvDoubleArr");
        double[] values = {1.0, 2.0, 3.0};
        channel.putRawValCallback(values, null);

        assertArrayEquals(values, channel.getRawValueRecord().doubleArray(), 0.0);
        assertEquals(double[].class, channel.elementType());
        assertEquals(3, channel.elementCount());
    }

    /**
     * Writing a value of a different type re-creates the record with the new type.
     */
    @Test
    public void testTypeChange() throws Exception {
        Epics7ServerChannel channel = channel("SrvTypeChange");
        channel.putRawValCallback(1.0, null);
        assertEquals(double.class, channel.elementType());

        channel.putRawValCallback("now a string", null);
        assertEquals(String.class, channel.elementType());
        assertEquals("now a string", channel.getRawValueRecord().stringValue());
    }

    @Test
    public void testPutNotifiesListener() throws Exception {
        Epics7ServerChannel channel = channel("SrvNotify");
        AtomicReference<xal.ca.Channel> completed = new AtomicReference<>();

        channel.putRawValCallback(1.0, completed::set);
        assertEquals(channel, completed.get());
    }

    @Test
    public void testTimeRecordHasTimestamp() throws Exception {
        Epics7ServerChannel channel = channel("SrvTime");
        channel.putRawValCallback(1.0, null);

        ChannelTimeRecord record = channel.getRawTimeRecord();
        // The value was just stamped, so it is close to now.
        double now = System.currentTimeMillis() / 1000.0;
        assertEquals(now, record.timeStampInSeconds(), 5.0);
    }

    @Test
    public void testMonitorReceivesPuts() throws Exception {
        Epics7ServerChannel channel = channel("SrvMonitor");
        AtomicReference<Double> latest = new AtomicReference<>();

        channel.addMonitorValue((record, chan) -> latest.set(record.doubleValue()), xal.ca.Monitor.VALUE);
        channel.putRawValCallback(7.0, null);

        assertEquals(7.0, latest.get(), 0.0);
    }

    @Test
    public void testMetadataSetters() throws Exception {
        Epics7ServerChannel channel = channel("SrvMeta");
        channel.putRawValCallback(1.0, null);

        channel.setUnits("mm");
        channel.setLowerDispLimit(-10.0);
        channel.setUpperDispLimit(10.0);
        channel.setLowerCtrlLimit(-8.0);
        channel.setUpperCtrlLimit(8.0);
        channel.setLowerAlarmLimit(-6.0);
        channel.setUpperAlarmLimit(6.0);
        channel.setLowerWarningLimit(-4.0);
        channel.setUpperWarningLimit(4.0);

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
}
