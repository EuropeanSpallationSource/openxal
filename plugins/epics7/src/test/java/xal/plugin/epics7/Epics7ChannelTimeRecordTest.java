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

import java.time.Instant;
import org.epics.pva.data.PVADouble;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static xal.plugin.epics7.TestData.VALUE;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelTimeRecordTest {

    @Test
    public void testTimestamp() {
        Instant time = Instant.ofEpochSecond(1_600_000_000L, 123_456_789);
        Epics7ChannelTimeRecord instance = new Epics7ChannelTimeRecord(
                TestData.withTime(new PVADouble(VALUE, 1.0), 0, 0, time));

        assertEquals(1_600_000_000.123456789, instance.getTimestamp().getSeconds(), 1e-3);
        assertEquals(1_600_000_000.123456789, instance.timeStampInSeconds(), 1e-3);
    }

    /**
     * The status record's fields remain available on a time record.
     */
    @Test
    public void testInheritsStatusAndSeverity() {
        Epics7ChannelTimeRecord instance = new Epics7ChannelTimeRecord(
                TestData.withTime(new PVADouble(VALUE, 1.0), 2, 7, Instant.EPOCH));

        assertEquals(7, instance.status());
        assertEquals(2, instance.severity());
    }

    /**
     * A structure with no timeStamp field, as returned by a plain "value" request, must not blow up.
     */
    @Test
    public void testMissingTimeStampDefaultsToEpoch() {
        Epics7ChannelTimeRecord instance = new Epics7ChannelTimeRecord(TestData.doubleRecord(1.0));

        assertEquals(0.0, instance.timeStampInSeconds(), 0.0);
    }

    @Test
    public void testToStringMentionsTime() {
        Epics7ChannelTimeRecord instance = new Epics7ChannelTimeRecord(
                TestData.withTime(new PVADouble(VALUE, 1.0), 0, 0, Instant.ofEpochSecond(1000)));

        assertTrue(instance.toString().contains("time: "));
    }
}
