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

import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.junit.Test;
import static org.junit.Assert.*;
import static xal.plugin.epics7.Epics7Channel.ALARM_FIELD;
import static xal.plugin.epics7.Epics7Channel.CONTROL_FIELD;
import static xal.plugin.epics7.Epics7Channel.DISPLAY_FIELD;
import static xal.plugin.epics7.Epics7Channel.TIMESTAMP_FIELD;
import static xal.plugin.epics7.Epics7ChannelStatusRecord.ALARM_FIELD_NAME;
import static xal.plugin.epics7.Epics7ChannelStatusRecord.SEVERITY_FIELD_NAME;
import static xal.plugin.epics7.Epics7ChannelStatusRecord.STATUS_FIELD_NAME;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelStatusRecordTest {

    private PVStructure pvStructure;
    String properties = ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
            + DISPLAY_FIELD + "," + CONTROL_FIELD;

    private Epics7ChannelStatusRecord newEpics7ChannelStatusRecord() {
        Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, properties);
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
        pvStructure = pvDataCreate.createPVStructure(structure);

        pvStructure.getStructureField(ALARM_FIELD_NAME).getIntField(STATUS_FIELD_NAME).put(1);
        pvStructure.getStructureField(ALARM_FIELD_NAME).getIntField(SEVERITY_FIELD_NAME).put(2);

        Epics7ChannelStatusRecord epics7ChannelStatusRecord = new Epics7ChannelStatusRecord(pvStructure, "Test_Channel");
        return epics7ChannelStatusRecord;
    }

    /**
     * Test of status method, of class Epics7ChannelStatusRecord.
     */
    @Test
    public void testStatus() {
        System.out.println("status");
        Epics7ChannelStatusRecord instance = newEpics7ChannelStatusRecord();
        int expResult = 1;
        int result = instance.status();
        assertEquals(expResult, result);
    }

    /**
     * Test of severity method, of class Epics7ChannelStatusRecord.
     */
    @Test
    public void testSeverity() {
        System.out.println("severity");
        Epics7ChannelStatusRecord instance = newEpics7ChannelStatusRecord();
        int expResult = 2;
        int result = instance.severity();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class Epics7ChannelStatusRecord.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Epics7ChannelStatusRecord instance = newEpics7ChannelStatusRecord();
        String expResult = ", status: 1, severity: 2";
        String result = instance.toString().substring(instance.toString().indexOf(','));
        assertEquals(expResult, result);
    }
}
