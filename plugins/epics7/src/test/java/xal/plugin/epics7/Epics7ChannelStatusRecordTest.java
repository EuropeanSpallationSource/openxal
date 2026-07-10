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

import org.epics.pva.data.PVADouble;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static xal.plugin.epics7.TestData.VALUE;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelStatusRecordTest {

    @Test
    public void testStatusAndSeverity() {
        Epics7ChannelStatusRecord instance = new Epics7ChannelStatusRecord(
                TestData.withAlarm(new PVADouble(VALUE, 1.0), 2, 5));

        assertEquals(5, instance.status());
        assertEquals(2, instance.severity());
    }

    /**
     * A structure with no alarm field, as returned by a plain "value" request, must not blow up.
     */
    @Test
    public void testMissingAlarmFieldDefaultsToZero() {
        Epics7ChannelStatusRecord instance = new Epics7ChannelStatusRecord(TestData.doubleRecord(1.0));

        assertEquals(0, instance.status());
        assertEquals(0, instance.severity());
    }

    @Test
    public void testToStringMentionsStatusAndSeverity() {
        Epics7ChannelStatusRecord instance = new Epics7ChannelStatusRecord(
                TestData.withAlarm(new PVADouble(VALUE, 1.0), 1, 3));

        String text = instance.toString();
        assertTrue(text.contains("status: 3"));
        assertTrue(text.contains("severity: 1"));
    }
}
