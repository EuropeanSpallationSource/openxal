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

import org.junit.Test;
import static org.junit.Assert.*;
import xal.ca.ChannelRecord;
import xal.ca.ChannelStatusRecord;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.Monitor;
import xal.ca.Timestamp;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannelTest {

    private boolean methodCalled = false;

    /**
     * Test of connectAndWait method, of class Epics7ServerChannel.
     */
    @Test
    public void testConnectAndWait() {
        System.out.println("connectAndWait");
        double timeout = 1.0;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        boolean result = instance.connectAndWait(timeout);
        assertEquals(true, result);
    }

    /**
     * Test of requestConnection method, of class Epics7ServerChannel.
     */
    @Test
    public void testRequestConnection() {
        System.out.println("requestConnection");
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.requestConnection();
        assertEquals(true, instance.isConnected());
    }

    /**
     * Test of disconnect method, of class Epics7ServerChannel.
     */
    @Test
    public void testDisconnect() {
        System.out.println("disconnect");
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.disconnect();
        assertEquals(false, instance.isConnected());
    }

    /**
     * Test of elementCount method, of class Epics7ServerChannel.
     */
    @Test
    public void testElementCount() throws Exception {
        System.out.println("elementCount");
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());

        int expResult = 1;
        assertEquals(expResult, instance.elementCount());

        String valString = "value";
        instance.putVal(valString);
        assertEquals(expResult, instance.elementCount());

        byte valbyte = 1;
        instance.putVal(valbyte);
        assertEquals(expResult, instance.elementCount());

        short valshort = 1;
        instance.putVal(valshort);
        assertEquals(expResult, instance.elementCount());

        int valint = 1;
        instance.putVal(valint);
        assertEquals(expResult, instance.elementCount());

        long vallong = 1;
        instance.putVal(vallong);
        assertEquals(expResult, instance.elementCount());

        float valfloat = 1;
        instance.putVal(valfloat);
        assertEquals(expResult, instance.elementCount());

        double valdouble = 1;
        instance.putVal(valdouble);
        assertEquals(expResult, instance.elementCount());

        expResult = 2;

        String[] valStringA = {"value1", "value2"};
        instance.putVal(valStringA);
        assertEquals(expResult, instance.elementCount());

        byte[] valbyteA = {1, 2};
        instance.putVal(valbyteA);
        assertEquals(expResult, instance.elementCount());

        short[] valshortA = {1, 2};
        instance.putVal(valshortA);
        assertEquals(expResult, instance.elementCount());

        int[] valintA = {1, 2};
        instance.putVal(valintA);
        assertEquals(expResult, instance.elementCount());

        long[] vallongA = {1, 2};
        instance.putVal(vallongA);
        assertEquals(expResult, instance.elementCount());

        float[] valfloatA = {1, 2};
        instance.putVal(valfloatA);
        assertEquals(expResult, instance.elementCount());

        double[] valdoubleA = {1, 2};
        instance.putVal(valdoubleA);
        assertEquals(expResult, instance.elementCount());
    }

    /**
     * Test of getUnits method, of class Epics7ServerChannel.
     */
    @Test
    public void testGetUnits() {
        System.out.println("getUnits");
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        String expResult = "Volt";
        instance.getDisplay().getStringField("units").put(expResult);
        String result = instance.getUnits();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRawValueRecord method, of class Epics7ServerChannel.
     */
    @Test
    public void testGetRawValueRecord() throws Exception {
        System.out.println("getRawValueRecord");
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        ChannelRecord result = instance.getRawValueRecord();
        assertEquals(double.class, result.getType());
    }

    /**
     * Test of getRawStatusRecord method, of class Epics7ServerChannel.
     */
    @Test
    public void testGetRawStatusRecord() throws Exception {
        System.out.println("getRawStatusRecord");
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        ChannelStatusRecord result = instance.getRawStatusRecord();
        assertEquals(0, result.status());
    }

    /**
     * Test of getRawTimeRecord method, of class Epics7ServerChannel.
     */
    @Test
    public void testGetRawTimeRecord() throws Exception {
        System.out.println("getRawTimeRecord");
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        ChannelTimeRecord result = instance.getRawTimeRecord();
        assertEquals(Timestamp.class, result.getTimestamp().getClass());
    }

    /**
     * Test of getRawValueCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testGetRawValueCallback_IEventSinkValue() throws Exception {
        System.out.println("getRawValueCallback");
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.getRawValueCallback((record, chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of getRawValueCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testGetRawValueCallback_IEventSinkValue_boolean() throws Exception {
        System.out.println("getRawValueCallback");

        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.getRawValueCallback((record, chan) -> methodCalled = true, true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of getRawValueTimeCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testGetRawValueTimeCallback() throws Exception {
        System.out.println("getRawValueTimeCallback");
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.getRawValueTimeCallback((record, chan) -> methodCalled = true, true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of addMonitorValTime method, of class Epics7ServerChannel.
     */
    @Test
    public void testAddMonitorValTime() throws Exception {
        System.out.println("addMonitorValTime");

        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;

        Monitor result = instance.addMonitorValTime((record, chan) -> methodCalled = true, 0);
        instance.putVal("test");
        assertEquals(true, methodCalled);
    }

    /**
     * Test of addMonitorValStatus method, of class Epics7ServerChannel.
     */
    @Test
    public void testAddMonitorValStatus() throws Exception {
        System.out.println("addMonitorValStatus");

        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;

        Monitor result = instance.addMonitorValStatus((record, chan) -> methodCalled = true, 0);
        instance.putVal("test");
        assertEquals(true, methodCalled);
    }

    /**
     * Test of addMonitorValue method, of class Epics7ServerChannel.
     */
    @Test
    public void testAddMonitorValue() throws Exception {
        System.out.println("addMonitorValue");

        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;

        Monitor result = instance.addMonitorValue((record, chan) -> methodCalled = true, 0);
        instance.putVal("test");
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_String_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        String newVal = "Test";
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_byte_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        byte newVal = 1;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_short_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        short newVal = 1;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_int_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        int newVal = 1;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_long_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        long newVal = 1;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_float_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        float newVal = 1F;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_double_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        double newVal = 1.0;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        methodCalled = false;
        instance.putRawValCallback("test", (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_StringArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        String[] newVal = {"test1", "test2"};
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_byteArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        byte[] newVal = {1, 2};
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_shortArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        short[] newVal = {1, 2};
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_intArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        int[] newVal = {1, 2};
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_longArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        long[] newVal = {1, 2};
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_floatArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        float[] newVal = {1F, 2F};
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of putRawValCallback method, of class Epics7ServerChannel.
     */
    @Test
    public void testPutRawValCallback_doubleArr_PutListener() throws Exception {
        System.out.println("putRawValCallback");
        double[] newVal = {1, 2};
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);

        // Trying second if condition
        methodCalled = false;
        instance.putRawValCallback(newVal, (chan) -> methodCalled = true);
        assertEquals(true, methodCalled);
    }

    /**
     * Test of setUnits method, of class Epics7ServerChannel.
     */
    @Test
    public void testSetUnits() {
        System.out.println("setUnits");
        String units = "Volt";
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.setUnits(units);
        assertEquals(units, instance.getUnits());
    }

    /**
     * Test of setLowerDispLimit method, of class Epics7ServerChannel.
     */
    @Test
    public void testSetLowerDispLimit() throws ConnectionException, GetException {
        System.out.println("setLowerDispLimit");
        Number lowerLimit = 1.0;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.setLowerDispLimit(lowerLimit);
        assertEquals(lowerLimit, instance.rawLowerDisplayLimit());
    }

    /**
     * Test of setUpperDispLimit method, of class Epics7ServerChannel.
     */
    @Test
    public void testSetUpperDispLimit() throws ConnectionException, GetException {
        System.out.println("setUpperDispLimit");
        Number upperLimit = 1.0;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.setUpperDispLimit(upperLimit);
        assertEquals(upperLimit, instance.rawUpperDisplayLimit());
    }

    /**
     * Test of setLowerAlarmLimit method, of class Epics7ServerChannel.
     */
    @Test
    public void testSetLowerAlarmLimit() throws ConnectionException, GetException {
        System.out.println("setLowerAlarmLimit");
        Number lowerLimit = 1.0;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.setLowerAlarmLimit(lowerLimit);
        assertEquals(lowerLimit, instance.rawLowerAlarmLimit());
    }

    /**
     * Test of setUpperAlarmLimit method, of class Epics7ServerChannel.
     */
    @Test
    public void testSetUpperAlarmLimit() throws ConnectionException, GetException {
        System.out.println("setUpperAlarmLimit");
        Number upperLimit = 1.0;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.setUpperAlarmLimit(upperLimit);
        assertEquals(upperLimit, instance.rawUpperAlarmLimit());
    }

    /**
     * Test of setLowerWarningLimit method, of class Epics7ServerChannel.
     */
    @Test
    public void testSetLowerWarningLimit() throws ConnectionException, GetException {
        System.out.println("setLowerWarningLimit");
        Number lowerLimit = 1.0;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.setLowerWarningLimit(lowerLimit);
        assertEquals(lowerLimit, instance.rawLowerWarningLimit());
    }

    /**
     * Test of setUpperWarningLimit method, of class Epics7ServerChannel.
     */
    @Test
    public void testSetUpperWarningLimit() throws ConnectionException, GetException {
        System.out.println("setUpperWarningLimit");
        Number upperLimit = 1.0;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.setUpperWarningLimit(upperLimit);
        assertEquals(upperLimit, instance.rawUpperWarningLimit());
    }

    /**
     * Test of setLowerCtrlLimit method, of class Epics7ServerChannel.
     */
    @Test
    public void testSetLowerCtrlLimit() throws ConnectionException, GetException {
        System.out.println("setLowerCtrlLimit");
        Number lowerLimit = 1.0;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.setLowerCtrlLimit(lowerLimit);
        assertEquals(lowerLimit, instance.rawLowerControlLimit());
    }

    /**
     * Test of setUpperCtrlLimit method, of class Epics7ServerChannel.
     */
    @Test
    public void testSetUpperCtrlLimit() throws ConnectionException, GetException {
        System.out.println("setUpperCtrlLimit");
        Number upperLimit = 1.0;
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.setUpperCtrlLimit(upperLimit);
        assertEquals(upperLimit, instance.rawUpperControlLimit());
    }

    /**
     * Test of setSettable method, of class Epics7ServerChannel.
     */
    @Test
    public void testSetSettable() {
        System.out.println("setSettable");
        Epics7ServerChannel instance = new Epics7ServerChannel("Test", Epics7ServerChannelSystem.newEpics7ServerChannelSystem());
        instance.setSettable(true);
    }
}
