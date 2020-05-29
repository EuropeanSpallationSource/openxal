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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7IntegrationTest {

    private static final ChannelFactory channelFactoryServer = ChannelFactory.newServerFactory();
    private static final ChannelFactory channelFactoryClient = ChannelFactory.defaultFactory();

    public Epics7IntegrationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("EPICS_CA_AUTO_ADDR_LIST", "NO");
        System.setProperty("EPICS_PVA_AUTO_ADDR_LIST", "NO");
        System.setProperty("EPICS_CA_ADDR_LIST", "255.255.255.255");
        System.setProperty("EPICS_PVA_ADDR_LIST", "255.255.255.255");
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

    public void test(String channelName, Object value) throws ConnectionException, PutException, GetException {
        Channel channelServer = channelFactoryServer.getChannel(channelName);

        if (value instanceof Float) {
            channelServer.putVal((float) value);
        } else if (value instanceof Double) {
            channelServer.putVal((double) value);
        } else if (value instanceof Byte) {
            channelServer.putVal((byte) value);
        } else if (value instanceof Short) {
            channelServer.putVal((short) value);
        } else if (value instanceof Integer) {
            channelServer.putVal((int) value);
        } else if (value instanceof String) {
            channelServer.putVal((String) value);
        } else if (value instanceof Float[]) {
            channelServer.putVal((float[]) value);
        } else if (value instanceof Double[]) {
            channelServer.putVal((double[]) value);
        } else if (value instanceof Byte[]) {
            channelServer.putVal((byte[]) value);
        } else if (value instanceof Short[]) {
            channelServer.putVal((short[]) value);
        } else if (value instanceof Integer[]) {
            channelServer.putVal((int[]) value);
        } else if (value instanceof String[]) {
            channelServer.putVal((String[]) value);
        }

        Channel caChannel = channelFactoryClient.getChannel("ca://" + channelName);
        Channel pvaChannel = channelFactoryClient.getChannel("pva://" + channelName);

        // Reducing timeouts to shorten the tests in case they fail
        caChannel.setIoTimeout(0.2);
        pvaChannel.setIoTimeout(0.2);

        if (value instanceof Float) {
            Assert.assertEquals((float) value, caChannel.getValFlt(), 1e-6);
            Assert.assertEquals((float) value, pvaChannel.getValFlt(), 1e-6);
        } else {
            Assert.assertNotEquals(value, caChannel.getValFlt());
            Assert.assertNotEquals(value, pvaChannel.getValFlt());
        }

        if (value instanceof Double) {
            Assert.assertEquals((double) value, caChannel.getValDbl(), 1e-13);
            Assert.assertEquals((double) value, pvaChannel.getValDbl(), 1e-13);
        } else {
            Assert.assertNotEquals(value, caChannel.getValDbl());
            Assert.assertNotEquals(value, pvaChannel.getValDbl());
        }

        if (value instanceof Byte) {
            Assert.assertEquals((byte) value, caChannel.getValByte());
            Assert.assertEquals((byte) value, pvaChannel.getValByte());
        } else {
            Assert.assertNotEquals(value, caChannel.getValByte());
            Assert.assertNotEquals(value, pvaChannel.getValByte());
        }

        if (value instanceof Short) {
            Assert.assertEquals((short) value, caChannel.getValShort());
            Assert.assertEquals((short) value, pvaChannel.getValShort());
        } else {
            Assert.assertNotEquals(value, caChannel.getValShort());
            Assert.assertNotEquals(value, pvaChannel.getValShort());
        }

        if (value instanceof Integer) {
            Assert.assertEquals((int) value, caChannel.getValInt());
            Assert.assertEquals((int) value, pvaChannel.getValInt());
        } else {
            Assert.assertNotEquals(value, caChannel.getValInt());
            Assert.assertNotEquals(value, pvaChannel.getValInt());
        }

        if (value instanceof String) {
            Assert.assertEquals((String) value, caChannel.getValString());
            Assert.assertEquals((String) value, pvaChannel.getValString());
        } else {
            Assert.assertNotEquals(value, caChannel.getValString());
            Assert.assertNotEquals(value, pvaChannel.getValString());
        }

        if (value instanceof Float[]) {
            Assert.assertArrayEquals((float[]) value, caChannel.getArrFlt(), (float) 1e-6);
            Assert.assertArrayEquals((float[]) value, pvaChannel.getArrFlt(), (float) 1e-6);
        }

        if (value instanceof Double[]) {
            Assert.assertArrayEquals((double[]) value, caChannel.getArrDbl(), 1e-13);
            Assert.assertArrayEquals((double[]) value, pvaChannel.getArrDbl(), 1e-13);
        }

        if (value instanceof Byte[]) {
            Assert.assertArrayEquals((byte[]) value, caChannel.getArrByte());
            Assert.assertArrayEquals((byte[]) value, pvaChannel.getArrByte());
        }

        if (value instanceof Short[]) {
            Assert.assertArrayEquals((short[]) value, caChannel.getArrShort());
            Assert.assertArrayEquals((short[]) value, pvaChannel.getArrShort());
        }

        if (value instanceof Integer[]) {
            Assert.assertArrayEquals((int[]) value, caChannel.getArrInt());
            Assert.assertArrayEquals((int[]) value, pvaChannel.getArrInt());
        }

        if (value instanceof String[]) {
            Assert.assertArrayEquals((String[]) value, caChannel.getArrString());
            Assert.assertArrayEquals((String[]) value, pvaChannel.getArrString());
        }
    }

    @Test
    public void testFloat() throws ConnectionException, PutException, GetException {
        test("Test:Channel_float", (float) 5.6);
    }

    @Test
    public void testDouble() throws ConnectionException, PutException, GetException {
        test("Test:Channel_double", (double) 5.6);
    }

    @Test
    public void testByte() throws ConnectionException, PutException, GetException {
        test("Test:Channel_byte", (byte) 1);
    }

    @Test
    public void testShort() throws ConnectionException, PutException, GetException {
        test("Test:Channel_short", (short) 2);
    }

    @Test
    public void testInt() throws ConnectionException, PutException, GetException {
        test("Test:Channel_int", (int) 3);
    }

    @Test
    public void testString() throws ConnectionException, PutException, GetException {
        test("Test:Channel_String", "Test string");
    }

    @Test
    public void testFloatArray() throws ConnectionException, PutException, GetException {
        test("Test:Channel_floatArray", new float[]{(float) 1.2, (float) 5.6});
    }

    @Test
    public void testDoubleArray() throws ConnectionException, PutException, GetException {
        test("Test:Channel_doubleArray", new double[]{1.2, 5.6});
    }

    @Test
    public void testByteArray() throws ConnectionException, PutException, GetException {
        test("Test:Channel_byteArray", new byte[]{1, 0});
    }

    @Test
    public void testShortArray() throws ConnectionException, PutException, GetException {
        test("Test:Channel_shortArray", new short[]{2, 1, 0});
    }

    @Test
    public void testIntArray() throws ConnectionException, PutException, GetException {
        test("Test:Channel_intArray", new int[]{3, 2, 1, 0});
    }

    @Test
    public void testStringArray() throws ConnectionException, PutException, GetException {
        test("Test:Channel_StringArray", new String[]{"Test string", "Second String"});
    }
}
