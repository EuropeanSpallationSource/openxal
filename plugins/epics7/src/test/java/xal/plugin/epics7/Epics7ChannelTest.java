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
import static xal.plugin.epics7.Epics7ChannelStatusRecord.ALARM_FIELD_NAME;
import static xal.plugin.epics7.Epics7ChannelStatusRecord.SEVERITY_FIELD_NAME;
import static xal.plugin.epics7.Epics7ChannelStatusRecord.STATUS_FIELD_NAME;
import static xal.plugin.epics7.Epics7ChannelTimeRecord.NANOSECONDS_FIELD_NAME;
import static xal.plugin.epics7.Epics7ChannelTimeRecord.SECONDS_FIELD_NAME;
import static xal.plugin.epics7.Epics7ChannelTimeRecord.TIMESTAMP_FIELD_NAME;
import static xal.plugin.epics7.TestChannelProvider.CONNECTION_TIME;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelTest {

    private static final Logger LOGGER = Logger.getLogger(Epics7ChannelTimeRecordTest.class.getName());

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
        LOGGER.log(Level.INFO, "getNativeChannel");
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
        LOGGER.log(Level.INFO, "connectAndWait");

        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());
        //TestCA", Epics7TestChannelSystem.newEpics7ChannelSystem());
        Epics7Channel instance2 = new Epics7Channel("ca:
        //TestPVA", Epics7TestChannelSystem.newEpics7ChannelSystem());
        Epics7Channel instance3 = new Epics7Channel("pva:

        assertEquals(instance.connectAndWait(0), false);
        assertEquals(instance2.connectAndWait(0), false);
        assertEquals(instance3.connectAndWait(0), false);

        instance.disconnect();
        instance2.disconnect();
        instance3.disconnect();

        double timeout = 1.0;
        assertEquals(instance.connectAndWait(timeout), true);
        assertEquals(instance2.connectAndWait(timeout), true);
        assertEquals(instance3.connectAndWait(timeout), true);

        // Again to test with a connected channel.
        assertEquals(instance.connectAndWait(timeout), true);
        assertEquals(instance2.connectAndWait(timeout), true);
        assertEquals(instance3.connectAndWait(timeout), true);

        // Testing InterruptedException
        HandlerImpl handler = new HandlerImpl();
        Logger.getLogger(Epics7Channel.class.getName()).addHandler(handler);

        Thread thread = new Thread() {
            @Override
            public void run() {
                assertEquals(instance.connectAndWait(timeout), false);
            }
        };

        instance.disconnect();
        thread.start();
        thread.interrupt();

        // Waiting for the other thread to finish...
        try {
            Thread.sleep(CONNECTION_TIME / 2);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        assertEquals(handler.message, null);
        assertEquals(Level.INFO, handler.level);
    }

    /**
     * Test of requestConnection method, of class Epics7Channel.
     */
    @Test
    public void testRequestConnection() {
        LOGGER.log(Level.INFO, "requestConnection");

        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());
        instance.requestConnection();

        //TestCA", Epics7TestChannelSystem.newEpics7ChannelSystem());
        Epics7Channel instance2 = new Epics7Channel("ca:
        instance2.requestConnection();

        //TestPVA", Epics7TestChannelSystem.newEpics7ChannelSystem());
        Epics7Channel instance3 = new Epics7Channel("pva:
        instance3.requestConnection();

        try {
            Thread.sleep(CONNECTION_TIME + CONNECTION_TIME / 2);
        } catch (InterruptedException ex) {
            //
        }

        assertEquals(true, instance.isConnected());
        assertEquals(true, instance2.isConnected());
        assertEquals(true, instance3.isConnected());

        //Requesting connection to a connected channel
        instance.requestConnection();
        instance2.requestConnection();
        instance3.requestConnection();

        assertEquals(true, instance.isConnected());
        assertEquals(true, instance2.isConnected());
        assertEquals(true, instance3.isConnected());

    }

    /**
     * Test of disconnect method, of class Epics7Channel.
     */
    @Test
    public void testDisconnect() {
        LOGGER.log(Level.INFO, "disconnect");
        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());
        //TestCA", Epics7TestChannelSystem.newEpics7ChannelSystem());
        Epics7Channel instance2 = new Epics7Channel("ca:
        //TestPVA", Epics7TestChannelSystem.newEpics7ChannelSystem());
        Epics7Channel instance3 = new Epics7Channel("pva:

        // Testing disconnecting a disconnected channel
        instance.disconnect();
        instance2.disconnect();
        instance3.disconnect();

        // Conecting
        instance.connectAndWait();
        instance2.connectAndWait();
        instance3.connectAndWait();

        assertEquals(instance.isConnected(), true);
        assertEquals(instance2.isConnected(), true);
        assertEquals(instance3.isConnected(), true);

        // Testing disconnecting a connected channel
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
        LOGGER.log(Level.INFO, "getRequesterName");
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
        LOGGER.log(Level.INFO, "message");
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
        LOGGER.log(Level.INFO, "elementType");
        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
     * Test of elementType method, of class Epics7Channel.
     */
    @Test
    public void testElementType_GetException() throws Exception {
        LOGGER.log(Level.INFO, "elementType_GetException");

        HandlerImpl handler = new HandlerImpl();
        Logger.getLogger(Epics7Channel.class.getName()).addHandler(handler);

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
                throw new GetException();
            }
        };

        instance.elementType();

        assertEquals(handler.message, null);
        assertEquals(handler.level, Level.SEVERE);
    }

    /**
     * Test of elementCount method, of class Epics7Channel.
     */
    @Test
    public void testElementCount() throws Exception {
        LOGGER.log(Level.INFO, "elementCount");

        int expResult = 3;
        double[] array = {1.0, 2.0, 3.0};
        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
     * Test of elementCount method, of class Epics7Channel.
     */
    @Test
    public void testElementCount_GetException() throws Exception {
        LOGGER.log(Level.INFO, "elementCount_GetException");

        HandlerImpl handler = new HandlerImpl();
        Logger.getLogger(Epics7Channel.class.getName()).addHandler(handler);

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
                throw new GetException();
            }
        };

        instance.elementCount();

        assertEquals(handler.message, null);
        assertEquals(handler.level, Level.SEVERE);
    }

    /**
     * Test of readAccess method, of class Epics7Channel.
     */
    @Test
    public void testReadAccess() throws Exception {
        LOGGER.log(Level.INFO, "readAccess");
        Epics7Channel instance = new Epics7Channel("Test", null);
        assertEquals(instance.readAccess(), true);
    }

    /**
     * Test of writeAccess method, of class Epics7Channel.
     */
    @Test
    public void testWriteAccess() throws Exception {
        LOGGER.log(Level.INFO, "writeAccess");
        Epics7Channel instance = new Epics7Channel("Test", null);
        assertEquals(instance.writeAccess(), true);
    }

    /**
     * Test of getUnits method, of class Epics7Channel.
     */
    @Test
    public void testGetUnits() throws Exception {
        LOGGER.log(Level.INFO, "getUnits");
        String units = "Volt";

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "rawUpperDisplayLimit");

        double limitHigh = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "rawLowerDisplayLimit");

        double limitLow = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "rawUpperAlarmLimit");

        double highAlarmLimit = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "rawLowerAlarmLimit");

        double lowAlarmLimit = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "rawUpperWarningLimit");

        double highWarningLimit = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "rawLowerWarningLimit");

        double lowWarningLimit = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "rawUpperControlLimit");

        double limitHigh = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "rawLowerControlLimit");

        double limitLow = 1.2;
        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "get");
        String request = "";
        boolean attemptConnection = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public void getCallback(String request, final EventListener listener, boolean attemptConnection) throws ConnectionException, GetException {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                try {
                    listener.event(pvStructure);
                } catch (PutException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
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
        LOGGER.log(Level.INFO, "getCallback");
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
        LOGGER.log(Level.INFO, "getRawValueRecord");
        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "getRawValueCallback");
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public void getCallback(String request, final EventListener listener, boolean attemptConnection) throws ConnectionException, GetException {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                try {
                    listener.event(pvStructure);
                } catch (PutException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        };

        instance.getRawValueCallback((record, chan) -> methodCalled = true);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawValueCallback method, of class Epics7Channel.
     */
    @Test
    public void testGetRawValueCallback_IEventSinkValue_boolean() throws Exception {
        LOGGER.log(Level.INFO, "getRawValueCallback");
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public void getCallback(String request, final EventListener listener, boolean attemptConnection) throws ConnectionException, GetException {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                try {
                    listener.event(pvStructure);
                } catch (PutException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        };

        instance.getRawValueCallback((record, chan) -> methodCalled = true, true);

        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawStringValueRecord method, of class Epics7Channel.
     */
    @Test
    public void testGetRawStringValueRecord() throws Exception {
        LOGGER.log(Level.INFO, "getRawStringValueRecord");

        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public ChannelRecord getRawValueRecord() throws ConnectionException, GetException {
                methodCalled = true;
                return null;
            }
        };

        instance.getRawStringValueRecord();
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawStatusRecord method, of class Epics7Channel.
     */
    @Test
    public void testGetRawStatusRecord() throws Exception {
        LOGGER.log(Level.INFO, "getRawStatusRecord");

        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) throws ConnectionException, GetException {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                methodCalled = true;

                return pvStructure;
            }
        };

        instance.getRawStatusRecord();
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawStringStatusRecord method, of class Epics7Channel.
     */
    @Test
    public void testGetRawStringStatusRecord() throws Exception {
        LOGGER.log(Level.INFO, "getRawStringStatusRecord");

        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public ChannelStatusRecord getRawStatusRecord() throws ConnectionException, GetException {
                methodCalled = true;
                return null;
            }
        };

        instance.getRawStringStatusRecord();
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawTimeRecord method, of class Epics7Channel.
     */
    @Test
    public void testGetRawTimeRecord() throws Exception {
        LOGGER.log(Level.INFO, "getRawTimeRecord");

        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) throws ConnectionException, GetException {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                methodCalled = true;

                return pvStructure;
            }
        };

        instance.getRawTimeRecord();
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawStringTimeRecord method, of class Epics7Channel.
     */
    @Test
    public void testGetRawStringTimeRecord() throws Exception {
        LOGGER.log(Level.INFO, "getRawStringTimeRecord");
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) throws ConnectionException, GetException {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);

                methodCalled = true;

                return pvStructure;
            }
        };

        instance.getRawStringTimeRecord();
        assertEquals(methodCalled, true);
    }

    /**
     * Test of getRawValueTimeCallback method, of class Epics7Channel.
     */
    @Test
    public void testGetRawValueTimeCallback() throws Exception {
        LOGGER.log(Level.INFO, "getRawValueTimeCallback");

        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public void getCallback(String request, final EventListener listener, boolean attemptConnection) throws ConnectionException, GetException {
                Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvDouble, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                        + DISPLAY_FIELD + "," + CONTROL_FIELD);

                PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
                PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
                pvStructure.getStructureField(ALARM_FIELD_NAME).getIntField(STATUS_FIELD_NAME).put(0);
                pvStructure.getStructureField(ALARM_FIELD_NAME).getIntField(SEVERITY_FIELD_NAME).put(0);
                pvStructure.getStructureField(TIMESTAMP_FIELD_NAME).getLongField(SECONDS_FIELD_NAME).put(0);
                pvStructure.getStructureField(TIMESTAMP_FIELD_NAME).getIntField(NANOSECONDS_FIELD_NAME).put(0);

                try {
                    listener.event(pvStructure);
                } catch (PutException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        };

        instance.getRawValueTimeCallback((record, chan) -> methodCalled = true, true);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of addMonitorValTime method, of class Epics7Channel.
     */
    @Test
    public void testAddMonitorValTime() throws Exception {
        LOGGER.log(Level.INFO, "addMonitorValTime");
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());

        instance.addMonitorValTime((record, chan) -> methodCalled = true, 0);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of addMonitorValStatus method, of class Epics7Channel.
     */
    @Test
    public void testAddMonitorValStatus() throws Exception {
        LOGGER.log(Level.INFO, "addMonitorValStatus");
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());

        instance.addMonitorValStatus((record, chan) -> methodCalled = true, 0);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of addMonitorValue method, of class Epics7Channel.
     */
    @Test
    public void testAddMonitorValue() throws Exception {
        LOGGER.log(Level.INFO, "addMonitorValue");
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());

        instance.addMonitorValue((record, chan) -> methodCalled = true, 0);
        assertEquals(methodCalled, true);
    }

    /**
     * Test of putRawValCallback method, of class Epics7Channel.
     */
    @Test
    public void testPutRawValCallback_PutListener_EventListener() throws Exception {
        LOGGER.log(Level.INFO, "putRawValCallback");
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        methodCalled = false;

        String newVal = "";

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        byte newVal = 0;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        short newVal = 0;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        int newVal = 0;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        float newVal = 0.0F;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        double newVal = 0.0;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        String[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        byte[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        short[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        int[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        float[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "putRawValCallback");
        double[] newVal = null;
        methodCalled = false;

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
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
        LOGGER.log(Level.INFO, "getOperationLimitPVs");
        Epics7Channel instance = new Epics7Channel("Test", null);

        boolean exceptionGenerated = false;
        try {
            instance.getOperationLimitPVs();
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
        LOGGER.log(Level.INFO, "getWarningLimitPVs");
        Epics7Channel instance = new Epics7Channel("Test", null);

        boolean exceptionGenerated = false;
        try {
            instance.getWarningLimitPVs();
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
        LOGGER.log(Level.INFO, "getAlarmLimitPVs");
        Epics7Channel instance = new Epics7Channel("Test", null);

        boolean exceptionGenerated = false;
        try {
            instance.getAlarmLimitPVs();
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
        LOGGER.log(Level.INFO, "getDriveLimitPVs");
        Epics7Channel instance = new Epics7Channel("Test", null);

        boolean exceptionGenerated = false;
        try {
            instance.getDriveLimitPVs();
        } catch (Exception ex) {
            exceptionGenerated = true;
        }
        assertEquals(exceptionGenerated, true);
    }

    /**
     *
     */
    @Test
    public void getUnits_GetException() throws Exception {
        LOGGER.log(Level.INFO, "rawUpperDisplayLimit");

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) {
                return null;
            }
        };

        boolean exceptionThrown = false;
        try {
            instance.getUnits();
        } catch (GetException ex) {
            exceptionThrown = true;
        }
        assertEquals(exceptionThrown, true);
    }

    /**
     *
     */
    @Test
    public void rawUpperDisplayLimit_GetException() throws Exception {
        LOGGER.log(Level.INFO, "rawUpperDisplayLimit");

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) {
                return null;
            }
        };
        boolean exceptionThrown = false;
        try {
            instance.rawUpperDisplayLimit();
        } catch (GetException ex) {
            exceptionThrown = true;
        }
        assertEquals(exceptionThrown, true);
    }

    /**
     *
     */
    @Test
    public void rawLowerDisplayLimit_GetException() throws Exception {
        LOGGER.log(Level.INFO, "rawUpperDisplayLimit");

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) {
                return null;
            }
        };
        boolean exceptionThrown = false;
        try {
            instance.rawLowerDisplayLimit();
        } catch (GetException ex) {
            exceptionThrown = true;
        }
        assertEquals(exceptionThrown, true);
    }

    /**
     *
     */
    @Test
    public void rawUpperAlarmLimit_GetException() throws Exception {
        LOGGER.log(Level.INFO, "rawUpperDisplayLimit");

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) {
                return null;
            }
        };
        boolean exceptionThrown = false;
        try {
            instance.rawUpperAlarmLimit();
        } catch (GetException ex) {
            exceptionThrown = true;
        }
        assertEquals(exceptionThrown, true);
    }

    /**
     *
     */
    @Test
    public void rawLowerAlarmLimit_GetException() throws Exception {
        LOGGER.log(Level.INFO, "rawUpperDisplayLimit");

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) {
                return null;
            }
        };
        boolean exceptionThrown = false;
        try {
            instance.rawLowerAlarmLimit();
        } catch (GetException ex) {
            exceptionThrown = true;
        }
        assertEquals(exceptionThrown, true);
    }

    /**
     *
     */
    @Test
    public void rawUpperWarningLimit_GetException() throws Exception {
        LOGGER.log(Level.INFO, "rawUpperDisplayLimit");

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) {
                return null;
            }
        };
        boolean exceptionThrown = false;
        try {
            instance.rawUpperWarningLimit();
        } catch (GetException ex) {
            exceptionThrown = true;
        }
        assertEquals(exceptionThrown, true);
    }

    /**
     *
     */
    @Test
    public void rawLowerWarningLimit_GetException() throws Exception {
        LOGGER.log(Level.INFO, "rawUpperDisplayLimit");

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) {
                return null;
            }
        };
        boolean exceptionThrown = false;
        try {
            instance.rawLowerWarningLimit();
        } catch (GetException ex) {
            exceptionThrown = true;
        }
        assertEquals(exceptionThrown, true);
    }

    /**
     *
     */
    @Test
    public void rawUpperControlLimit_GetException() throws Exception {
        LOGGER.log(Level.INFO, "rawUpperDisplayLimit");

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) {
                return null;
            }
        };
        boolean exceptionThrown = false;
        try {
            instance.rawUpperControlLimit();
        } catch (GetException ex) {
            exceptionThrown = true;
        }
        assertEquals(exceptionThrown, true);
    }

    /**
     *
     */
    @Test
    public void rawLowerControlLimit_GetException() throws Exception {
        LOGGER.log(Level.INFO, "rawUpperDisplayLimit");

        Epics7Channel instance = new Epics7Channel("Test", null) {
            @Override
            public PVStructure get(String request) {
                return null;
            }
        };
        boolean exceptionThrown = false;
        try {
            instance.rawLowerControlLimit();
        } catch (GetException ex) {
            exceptionThrown = true;
        }
        assertEquals(exceptionThrown, true);
    }

}
