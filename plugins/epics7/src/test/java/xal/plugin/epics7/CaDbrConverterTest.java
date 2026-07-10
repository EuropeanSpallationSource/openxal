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

import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.dbr.DBR_TIME_Double;
import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.Status;
import gov.aps.jca.dbr.TimeStamp;
import org.epics.pva.data.PVADouble;
import org.epics.pva.data.PVADoubleArray;
import org.epics.pva.data.PVAInt;
import org.epics.pva.data.PVAStructure;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for the Channel Access to PV Access conversion.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class CaDbrConverterTest {

    @Test
    public void testPlainScalarHasValueOnly() {
        PVAStructure structure = CaDbrConverter.toStructure(new DBR_Double(new double[]{2.5}));

        PVADouble value = structure.get("value");
        assertNotNull(value);
        assertEquals(2.5, value.get(), 0.0);
        // A plain DBR carries no alarm or timestamp.
        assertNull(structure.get("alarm"));
        assertNull(structure.get("timeStamp"));
    }

    @Test
    public void testArrayValue() {
        PVAStructure structure = CaDbrConverter.toStructure(new DBR_Double(new double[]{1.0, 2.0, 3.0}));

        PVADoubleArray value = structure.get("value");
        assertNotNull(value);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, value.get(), 0.0);
    }

    @Test
    public void testStatusFromDbr() {
        DBR_TIME_Double dbr = new DBR_TIME_Double(new double[]{1.0});
        dbr.setSeverity(Severity.MAJOR_ALARM);
        dbr.setStatus(Status.HIHI_ALARM);
        dbr.setTimeStamp(new TimeStamp(0, 0));

        PVAStructure structure = CaDbrConverter.toStructure(dbr);
        PVAStructure alarm = structure.get("alarm");
        assertNotNull(alarm);
        assertEquals(Severity.MAJOR_ALARM.getValue(), ((PVAInt) alarm.get("severity")).get());
        assertEquals(Status.HIHI_ALARM.getValue(), ((PVAInt) alarm.get("status")).get());
    }

    /**
     * Channel Access timestamps count from 1990; the converted value counts from 1970.
     */
    @Test
    public void testTimestampShiftedToUnixEpoch() {
        DBR_TIME_Double dbr = new DBR_TIME_Double(new double[]{1.0});
        dbr.setTimeStamp(new TimeStamp(100, 500));

        PVAStructure structure = CaDbrConverter.toStructure(dbr);
        PVAStructure timeStamp = structure.get("timeStamp");
        assertNotNull(timeStamp);

        long seconds = ((org.epics.pva.data.PVALong) timeStamp.get("secondsPastEpoch")).get();
        assertEquals(100 + CaDbrConverter.EPICS_EPOCH, seconds);
        assertEquals(500, ((PVAInt) timeStamp.get("nanoseconds")).get());
    }

    @Test
    public void testControlDbrHasDisplayControlAndValueAlarm() {
        DBR_CTRL_Double dbr = new DBR_CTRL_Double(new double[]{1.0});
        dbr.setLowerDispLimit(-10.0);
        dbr.setUpperDispLimit(10.0);
        dbr.setLowerCtrlLimit(-8.0);
        dbr.setUpperCtrlLimit(8.0);
        dbr.setLowerAlarmLimit(-6.0);
        dbr.setUpperAlarmLimit(6.0);
        dbr.setUnits("mm");

        PVAStructure structure = CaDbrConverter.toStructure(dbr);

        PVAStructure display = structure.get("display");
        assertNotNull(display);
        assertEquals(-10.0, ((PVADouble) display.get("limitLow")).get(), 0.0);
        assertEquals(10.0, ((PVADouble) display.get("limitHigh")).get(), 0.0);
        assertEquals("mm", ((org.epics.pva.data.PVAString) display.get("units")).get());

        PVAStructure control = structure.get("control");
        assertNotNull(control);
        assertEquals(-8.0, ((PVADouble) control.get("limitLow")).get(), 0.0);
        assertEquals(8.0, ((PVADouble) control.get("limitHigh")).get(), 0.0);

        PVAStructure valueAlarm = structure.get("valueAlarm");
        assertNotNull(valueAlarm);
        assertEquals(-6.0, ((PVADouble) valueAlarm.get("lowAlarmLimit")).get(), 0.0);
        assertEquals(6.0, ((PVADouble) valueAlarm.get("highAlarmLimit")).get(), 0.0);
    }

    /**
     * A request that names a metadata field must map to a control DBR so the server returns the limits.
     */
    @Test
    public void testRequestTypeSelection() {
        assertEquals(DBRType.CTRL_DOUBLE, CaDbrConverter.requestType(DBRType.DOUBLE, Epics7Channel.DISPLAY_FIELD));
        assertEquals(DBRType.CTRL_DOUBLE,
                CaDbrConverter.requestType(DBRType.DOUBLE, Epics7Channel.VALUE_ALARM_FIELD));
        assertEquals(DBRType.TIME_DOUBLE, CaDbrConverter.requestType(DBRType.DOUBLE, Epics7Channel.TIME_REQUEST));
        assertEquals(DBRType.STS_DOUBLE, CaDbrConverter.requestType(DBRType.DOUBLE, Epics7Channel.STATUS_REQUEST));
        assertEquals(DBRType.DOUBLE, CaDbrConverter.requestType(DBRType.DOUBLE, Epics7Channel.VALUE_REQUEST));
    }

    @Test
    public void testValueRoundTripToDbr() {
        DBR dbr = CaDbrConverter.toDBR(new PVADouble("value", 4.5));
        assertArrayEquals(new double[]{4.5}, (double[]) dbr.getValue(), 0.0);
    }

    /**
     * Channel Access has no 64 bit integer type, so a long is served as int.
     */
    @Test
    public void testLongNarrowedToInt() {
        DBR dbr = CaDbrConverter.toDBR(new org.epics.pva.data.PVALong("value", false, 7L));
        assertTrue(dbr instanceof DBR_Int);
        assertArrayEquals(new int[]{7}, (int[]) dbr.getValue());
    }

    @Test
    public void testStringRoundTrip() {
        DBR dbr = CaDbrConverter.toDBR(new org.epics.pva.data.PVAString("value", "hi"));
        assertTrue(dbr instanceof DBR_String);
        assertArrayEquals(new String[]{"hi"}, (String[]) dbr.getValue());
    }

    private static void assertTrue(boolean condition) {
        org.junit.Assert.assertTrue(condition);
    }
}
