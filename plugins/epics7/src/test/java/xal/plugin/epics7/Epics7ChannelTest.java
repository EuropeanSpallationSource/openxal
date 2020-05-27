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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pvaccess.client.Channel;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import xal.ca.ChannelRecord;
import xal.ca.ChannelStatusRecord;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.Monitor;
import xal.ca.PutException;
import xal.ca.PutListener;
import static xal.plugin.epics7.Epics7Channel.ALARM_FIELD;
import static xal.plugin.epics7.Epics7Channel.CONTROL_FIELD;
import static xal.plugin.epics7.Epics7Channel.DISPLAY_FIELD;
import static xal.plugin.epics7.Epics7Channel.TIMESTAMP_FIELD;
import static xal.plugin.epics7.Epics7Channel.VALUE_ALARM_FIELD;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelTest {

    boolean methodCalled = false;

    public Epics7ChannelTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getNativeChannel method, of class Epics7Channel.
     */
    @Test
    public void testGetNativeChannel() {
        System.out.println("getNativeChannel");
        Epics7Channel instance = new Epics7Channel("Test", null);
        Channel expResult = null;
        Channel result = instance.getNativeChannel();
        assertEquals(expResult, result);
    }

    /**
     * Test of connectAndWait method, of class Epics7Channel.
     */
    @Test
    public void testConnectAndWait() {
        System.out.println("connectAndWait");
        double timeout = 1.0;

        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());
        Epics7Channel instance2 = new Epics7Channel("ca://TestCA", Epics7TestChannelSystem.newEpics7ChannelSystem());
        Epics7Channel instance3 = new Epics7Channel("pva://TestPVA", Epics7TestChannelSystem.newEpics7ChannelSystem());

        assertEquals(instance.connectAndWait(timeout), true);
        assertEquals(instance2.connectAndWait(timeout), true);
        assertEquals(instance3.connectAndWait(timeout), true);
    }

    /**
     * Test of requestConnection method, of class Epics7Channel.
     */
    @Test
    public void testRequestConnection() {
        System.out.println("requestConnection");
        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());
        instance.requestConnection();

        Epics7Channel instance2 = new Epics7Channel("ca://TestCA", Epics7TestChannelSystem.newEpics7ChannelSystem());
        instance2.requestConnection();

        Epics7Channel instance3 = new Epics7Channel("pva://TestPVA", Epics7TestChannelSystem.newEpics7ChannelSystem());
        instance3.requestConnection();

        try {
            Thread.sleep(20);
        } catch (InterruptedException ex) {
            //
        }

        assertEquals(instance.isConnected(), true);
        assertEquals(instance2.isConnected(), true);
        assertEquals(instance3.isConnected(), true);
    }

    /**
     * Test of disconnect method, of class Epics7Channel.
     */
    @Test
    public void testDisconnect() {
        System.out.println("disconnect");
        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());
        Epics7Channel instance2 = new Epics7Channel("ca://TestCA", Epics7TestChannelSystem.newEpics7ChannelSystem());
        Epics7Channel instance3 = new Epics7Channel("pva://TestPVA", Epics7TestChannelSystem.newEpics7ChannelSystem());

        instance.disconnect();
        instance2.disconnect();
        instance3.disconnect();

        assertEquals(instance.isConnected(), false);
        assertEquals(instance2.isConnected(), false);
        assertEquals(instance3.isConnected(), false);
    }

    /**
     * Test of getRequesterName method, of class Epics7Channel.
     */
    @Test
    public void testGetRequesterName() {
        System.out.println("getRequesterName");
        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());
        String expResult = "TestRequester";

        instance.connectAndWait(1.0);

        String result = instance.getRequesterName();
        assertEquals(expResult, result);
    }

    /**
     * Test of message method, of class Epics7Channel.
     */
    @Test
    public void testMessage() {
        System.out.println("message");
        String message = "Test message";

        Epics7Channel instance = new Epics7Channel("Test", null);

        HandlerImpl handler = new HandlerImpl();
        Logger.getLogger(Epics7Channel.class.getName()).addHandler(handler);

        instance.message(message, null);

        assertEquals(message, handler.message);
        assertEquals(handler.level, Level.INFO);
    }

    /**
     * Test of elementType method, of class Epics7Channel.
     */
    @Test
    public void testElementType() throws Exception {
        System.out.println("elementType");
        Epics7Channel instance = new Epics7Channel("Test", null) {
            public ChannelRecord getRawValueRecord() {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvBoolean, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                return new Epics7ChannelRecord(pvStructure, channelName());
            }
        };
        Class expResult = boolean.class;
        Class result = instance.elementType();
        assertEquals(expResult, result);
    }

    /**
     * Test of elementCount method, of class Epics7Channel.
     */
    @Test
    public void testElementCount() throws Exception {
        System.out.println("elementCount");

        int expResult = 3;
        double[] array = {1.0, 2.0, 3.0};
        Epics7Channel instance = new Epics7Channel("Test", null) {
            public ChannelRecord getRawValueRecord() {
                Structure structure = StandardFieldFactory.getStandardField().scalarArray(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                pvStructure.getSubField(PVDoubleArray.class, Epics7Channel.VALUE_REQUEST).put(0, expResult, array, 0);

                return new Epics7ChannelRecord(pvStructure, channelName());
            }
        };
        int result = instance.elementCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of readAccess method, of class Epics7Channel.
     */
    @Test
    public void testReadAccess() throws Exception {
        System.out.println("readAccess");
        Epics7Channel instance = new Epics7Channel("Test", null);
        assertEquals(instance.readAccess(), true);
    }

    /**
     * Test of writeAccess method, of class Epics7Channel.
     */
    @Test
    public void testWriteAccess() throws Exception {
        System.out.println("writeAccess");
        Epics7Channel instance = new Epics7Channel("Test", null);
        assertEquals(instance.writeAccess(), true);
    }

    /**
     * Test of getUnits method, of class Epics7Channel.
     */
    @Test
    public void testGetUnits() throws Exception {
        System.out.println("getUnits");
        String units = "Volt";

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                PVStructure displayStructure = pvStructure.getStructureField(DISPLAY_FIELD);

                displayStructure.getStringField("units").put(units);

                return pvStructure;
            }
        };
        assertEquals(instance.getUnits(), units);
    }

    /**
     * Test of rawUpperDisplayLimit method, of class Epics7Channel.
     */
    @Test
    public void testRawUpperDisplayLimit() throws Exception {
        System.out.println("rawUpperDisplayLimit");

        double limitHigh = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                PVStructure displayStructure = pvStructure.getStructureField(DISPLAY_FIELD);

                displayStructure.getDoubleField("limitHigh").put(limitHigh);

                return pvStructure;
            }
        };

        Number result = instance.rawUpperDisplayLimit();
        assertEquals(limitHigh, result);
    }

    /**
     * Test of rawLowerDisplayLimit method, of class Epics7Channel.
     */
    @Test
    public void testRawLowerDisplayLimit() throws Exception {
        System.out.println("rawLowerDisplayLimit");

        double limitLow = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                PVStructure displayStructure = pvStructure.getStructureField(DISPLAY_FIELD);

                displayStructure.getDoubleField("limitLow").put(limitLow);

                return pvStructure;
            }
        };

        Number result = instance.rawLowerDisplayLimit();
        assertEquals(limitLow, result);
    }

    /**
     * Test of rawUpperAlarmLimit method, of class Epics7Channel.
     */
    @Test
    public void testRawUpperAlarmLimit() throws Exception {
        System.out.println("rawUpperAlarmLimit");

        double highAlarmLimit = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD + "," + VALUE_ALARM_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                PVStructure alarmValueStructure = pvStructure.getStructureField(VALUE_ALARM_FIELD);

                alarmValueStructure.getDoubleField("highAlarmLimit").put(highAlarmLimit);

                return pvStructure;
            }
        };

        Number result = instance.rawUpperAlarmLimit();
        assertEquals(highAlarmLimit, result);
    }

    /**
     * Test of rawLowerAlarmLimit method, of class Epics7Channel.
     */
    @Test
    public void testRawLowerAlarmLimit() throws Exception {
        System.out.println("rawLowerAlarmLimit");

        double lowAlarmLimit = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD + "," + VALUE_ALARM_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                PVStructure alarmValueStructure = pvStructure.getStructureField(VALUE_ALARM_FIELD);

                alarmValueStructure.getDoubleField("lowAlarmLimit").put(lowAlarmLimit);

                return pvStructure;
            }
        };

        Number result = instance.rawLowerAlarmLimit();
        assertEquals(lowAlarmLimit, result);
    }

    /**
     * Test of rawUpperWarningLimit method, of class Epics7Channel.
     */
    @Test
    public void testRawUpperWarningLimit() throws Exception {
        System.out.println("rawUpperWarningLimit");

        double highWarningLimit = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD + "," + VALUE_ALARM_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                PVStructure alarmValueStructure = pvStructure.getStructureField(VALUE_ALARM_FIELD);

                alarmValueStructure.getDoubleField("highWarningLimit").put(highWarningLimit);

                return pvStructure;
            }
        };

        Number result = instance.rawUpperWarningLimit();
        assertEquals(highWarningLimit, result);
    }

    /**
     * Test of rawLowerWarningLimit method, of class Epics7Channel.
     */
    @Test
    public void testRawLowerWarningLimit() throws Exception {
        System.out.println("rawLowerWarningLimit");

        double lowWarningLimit = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD + "," + VALUE_ALARM_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                PVStructure alarmValueStructure = pvStructure.getStructureField(VALUE_ALARM_FIELD);

                alarmValueStructure.getDoubleField("lowWarningLimit").put(lowWarningLimit);

                return pvStructure;
            }
        };

        Number result = instance.rawLowerWarningLimit();
        assertEquals(lowWarningLimit, result);
    }

    /**
     * Test of rawUpperControlLimit method, of class Epics7Channel.
     */
    @Test
    public void testRawUpperControlLimit() throws Exception {
        System.out.println("rawUpperControlLimit");

        double limitHigh = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                PVStructure controlStructure = pvStructure.getStructureField(CONTROL_FIELD);

                controlStructure.getDoubleField("limitHigh").put(limitHigh);

                return pvStructure;
            }
        };

        Number result = instance.rawUpperControlLimit();
        assertEquals(limitHigh, result);
    }

    /**
     * Test of rawLowerControlLimit method, of class Epics7Channel.
     */
    @Test
    public void testRawLowerControlLimit() throws Exception {
        System.out.println("rawLowerControlLimit");

        double limitLow = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                PVStructure controlStructure = pvStructure.getStructureField(CONTROL_FIELD);

                controlStructure.getDoubleField("limitLow").put(limitLow);

                return pvStructure;
            }
        };
        Number result = instance.rawLowerControlLimit();
        assertEquals(limitLow, result);
    }

    /**
     * Test of get method, of class Epics7Channel.
     */
    @Test
    public void testGet_String_boolean() throws Exception {
        System.out.println("get");
        String request = "";
        boolean attemptConnection = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void getCallback(String request, final EventListener listener, boolean attemptConnection) throws ConnectionException, GetException {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                listener.event(pvStructure);
            }
        };

        Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                + DISPLAY_FIELD + "," + CONTROL_FIELD);

        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
        PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

        PVStructure result = instance.get(request, attemptConnection);
        assertEquals(pvStructure, result);
    }

    /**
     * Test of getCallback method, of class Epics7Channel.
     */
    @Test
    public void testGetCallback_3args() throws Exception {
        System.out.println("getCallback");
        String request = "";
        EventListener listener = null;
        boolean attemptConnection = true;

        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());

        instance.getCallback(request, listener, attemptConnection);
    }

    /**
     * Test of getRawValueRecord method, of class Epics7Channel.
     */
    @Test
    public void testGetRawValueRecord() throws Exception {
        System.out.println("getRawValueRecord");
        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                return pvStructure;
            }
        };

        ChannelRecord result = instance.getRawValueRecord();

        assertEquals(result.getType(), double.class);
    }

    /**
     * Test of getRawValueCallback method, of class Epics7Channel.
     */
    @Test
    public void testGetRawValueCallback_IEventSinkValue() throws Exception {
        System.out.println("getRawValueCallback");
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void getCallback(String request, final EventListener listener, boolean attemptConnection) throws ConnectionException, GetException {
                methodCalled = true;
            }
        };

        instance.getRawValueCallback(null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawValueCallback method, of class Epics7Channel.
     */
    @Test
    public void testGetRawValueCallback_IEventSinkValue_boolean() throws Exception {
        System.out.println("getRawValueCallback");
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void getCallback(String request, final EventListener listener, boolean attemptConnection) throws ConnectionException, GetException {
                methodCalled = true;
            }
        };

        instance.getRawValueCallback(null, true);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawStringValueRecord method, of class Epics7Channel.
     */
    @Test
    public void testGetRawStringValueRecord() throws Exception {
        System.out.println("getRawStringValueRecord");

        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
                methodCalled = true;
                return null;
            }
        };

        ChannelRecord result = instance.getRawStringValueRecord();
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawStatusRecord method, of class Epics7Channel.
     */
    @Test
    public void testGetRawStatusRecord() throws Exception {
        System.out.println("getRawStatusRecord");

        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) throws ConnectionException, GetException {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                methodCalled = true;

                return pvStructure;
            }
        };

        ChannelRecord result = instance.getRawStatusRecord();
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawStringStatusRecord method, of class Epics7Channel.
     */
    @Test
    public void testGetRawStringStatusRecord() throws Exception {
        System.out.println("getRawStringStatusRecord");

        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public ChannelStatusRecord getRawStatusRecord() throws ConnectionException, GetException {
                methodCalled = true;
                return null;
            }
        };

        ChannelRecord result = instance.getRawStringStatusRecord();
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawTimeRecord method, of class Epics7Channel.
     */
    @Test
    public void testGetRawTimeRecord() throws Exception {
        System.out.println("getRawTimeRecord");

        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) throws ConnectionException, GetException {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                methodCalled = true;

                return pvStructure;
            }
        };

        ChannelRecord result = instance.getRawTimeRecord();
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawStringTimeRecord method, of class Epics7Channel.
     */
    @Test
    public void testGetRawStringTimeRecord() throws Exception {
        System.out.println("getRawStringTimeRecord");
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public PVStructure get(String request) throws ConnectionException, GetException {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                methodCalled = true;

                return pvStructure;
            }
        };

        ChannelTimeRecord result = instance.getRawStringTimeRecord();
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawValueTimeCallback method, of class Epics7Channel.
     */
    @Test
    public void testGetRawValueTimeCallback() throws Exception {
        System.out.println("getRawValueTimeCallback");

        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void getCallback(String request, final EventListener listener, boolean attemptConnection) throws ConnectionException, GetException {
                methodCalled = true;
            }
        };

        instance.getRawValueTimeCallback(null, true);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of addMonitorValTime method, of class Epics7Channel.
     */
    @Test
    public void testAddMonitorValTime() throws Exception {
        System.out.println("addMonitorValTime");

        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());

        Monitor result = instance.addMonitorValTime(null, 0);
    }

    /**
     * Test of addMonitorValStatus method, of class Epics7Channel.
     */
    @Test
    public void testAddMonitorValStatus() throws Exception {
        System.out.println("addMonitorValStatus");
        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());

        Monitor result = instance.addMonitorValStatus(null, 0);
    }

    /**
     * Test of addMonitorValue method, of class Epics7Channel.
     */
    @Test
    public void testAddMonitorValue() throws Exception {
        System.out.println("addMonitorValue");
        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());

        Monitor result = instance.addMonitorValue(null, 0);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_PutListener_EventListener() throws Exception {
        System.out.println("putRawValCallback");
        methodCalled = false;
        PutListener putListener = (chan) -> {
            methodCalled = true;
        };
        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());

        instance.putRawValCallback(putListener, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_String_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        methodCalled = false;

        String newVal = "";

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_byte_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        byte newVal = 0;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_short_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        short newVal = 0;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_int_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        int newVal = 0;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_float_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        float newVal = 0.0F;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_double_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        double newVal = 0.0;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_StringArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        String[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_byteArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        byte[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_shortArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        short[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_intArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        int[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_floatArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        float[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_doubleArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        double[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            public void putRawValCallback(PutListener listener, EventListener putListener) throws ConnectionException, PutException {
                methodCalled = true;
            }
        };

        instance.putRawValCallback(newVal, null);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getOperationLimitPVs method, of class Epics7Channel.
     */
    @Test
    public void testGetOperationLimitPVs() {
        System.out.println("getOperationLimitPVs");
        Epics7Channel instance = new Epics7Channel("Test", null);

        boolean exceptionGenerated = false;
        try {
            String[] result = instance.getOperationLimitPVs();
        } catch (Exception ex) {
            exceptionGenerated = true;
        }
        assertEquals(exceptionGenerated, true);

    }

    /**
     * Test of getWarningLimitPVs method, of class Epics7Channel.
     */
    @Test
    public void testGetWarningLimitPVs() {
        System.out.println("getWarningLimitPVs");
        Epics7Channel instance = new Epics7Channel("Test", null);

        boolean exceptionGenerated = false;
        try {
            String[] result = instance.getWarningLimitPVs();
        } catch (Exception ex) {
            exceptionGenerated = true;
        }
        assertEquals(exceptionGenerated, true);

    }

    /**
     * Test of getAlarmLimitPVs method, of class Epics7Channel.
     */
    @Test
    public void testGetAlarmLimitPVs() {
        System.out.println("getAlarmLimitPVs");
        Epics7Channel instance = new Epics7Channel("Test", null);

        boolean exceptionGenerated = false;
        try {
            String[] result = instance.getAlarmLimitPVs();
        } catch (Exception ex) {
            exceptionGenerated = true;
        }
        assertEquals(exceptionGenerated, true);

    }

    /**
     * Test of getDriveLimitPVs method, of class Epics7Channel.
     */
    @Test
    public void testGetDriveLimitPVs() {
        System.out.println("getDriveLimitPVs");
        Epics7Channel instance = new Epics7Channel("Test", null);

        boolean exceptionGenerated = false;
        try {
            instance.getDriveLimitPVs();
        } catch (Exception ex) {
            exceptionGenerated = true;
        }
        assertEquals(exceptionGenerated, true);
    }
}
