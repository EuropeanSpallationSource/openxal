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

import java.math.BigDecimal;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.junit.Test;
import static org.junit.Assert.*;
import xal.ca.Timestamp;
import static xal.plugin.epics7.Epics7Channel.ALARM_FIELD;
import static xal.plugin.epics7.Epics7Channel.CONTROL_FIELD;
import static xal.plugin.epics7.Epics7Channel.DISPLAY_FIELD;
import static xal.plugin.epics7.Epics7Channel.TIMESTAMP_FIELD;
import static xal.plugin.epics7.Epics7ChannelTimeRecord.NANOSECONDS_FIELD_NAME;
import static xal.plugin.epics7.Epics7ChannelTimeRecord.SECONDS_FIELD_NAME;
import static xal.plugin.epics7.Epics7ChannelTimeRecord.TIMESTAMP_FIELD_NAME;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelTimeRecordTest {

    private PVStructure pvStructure;
    String properties = ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
            + DISPLAY_FIELD + "," + CONTROL_FIELD;

    private Epics7ChannelTimeRecord newEpics7ChannelTimeRecord() {
        Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, properties);
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
        pvStructure = pvDataCreate.createPVStructure(structure);

        pvStructure.getStructureField(TIMESTAMP_FIELD_NAME).getLongField(SECONDS_FIELD_NAME).put(1);
        pvStructure.getStructureField(TIMESTAMP_FIELD_NAME).getIntField(NANOSECONDS_FIELD_NAME).put(2);

        Epics7ChannelTimeRecord epics7ChannelTimeRecord = new Epics7ChannelTimeRecord(pvStructure, "Test_Channel");
        return epics7ChannelTimeRecord;
    }

    /**
     * Test of getTimestamp method, of class Epics7ChannelTimeRecord.
     */
    @Test
    public void testGetTimestamp() {
        System.out.println("getTimestamp");
        Epics7ChannelTimeRecord instance = newEpics7ChannelTimeRecord();
        Timestamp expResult = new Timestamp(new BigDecimal(2).multiply(new BigDecimal("1e-9")).add(new BigDecimal(1)));
        Timestamp result = instance.getTimestamp();
        assertEquals(expResult, result);
    }

    /**
     * Test of timeStampInSeconds method, of class Epics7ChannelTimeRecord.
     */
    @Test
    public void testTimeStampInSeconds() {
        System.out.println("timeStampInSeconds");
        Epics7ChannelTimeRecord instance = newEpics7ChannelTimeRecord();
        double expResult = instance.getTimestamp().getSeconds();
        double result = instance.timeStampInSeconds();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of toString method, of class Epics7ChannelTimeRecord.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Epics7ChannelTimeRecord instance = newEpics7ChannelTimeRecord();
        String expResult = ", time: " + instance.getTimestamp().toString();
        String result = instance.toString().substring(instance.toString().indexOf(", time"));
        assertEquals(expResult, result);
    }
}
