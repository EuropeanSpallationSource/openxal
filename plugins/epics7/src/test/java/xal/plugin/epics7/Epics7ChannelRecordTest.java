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
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUIntArray;
import org.epics.pvdata.pv.PVULongArray;
import org.epics.pvdata.pv.PVUShortArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Structure;
import org.junit.Test;
import static org.junit.Assert.*;
import xal.ca.ChannelRecord;
import static xal.plugin.epics7.Epics7Channel.ALARM_FIELD;
import static xal.plugin.epics7.Epics7Channel.CONTROL_FIELD;
import static xal.plugin.epics7.Epics7Channel.DISPLAY_FIELD;
import static xal.plugin.epics7.Epics7Channel.TIMESTAMP_FIELD;
import static xal.plugin.epics7.Epics7Channel.VALUE_ALARM_FIELD;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelRecordTest {

    private PVStructure pvStructure;
    String properties = ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
            + DISPLAY_FIELD + "," + CONTROL_FIELD;
    String propertiesVA = properties + "," + VALUE_ALARM_FIELD;

    PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

    private Epics7ChannelRecord newEpics7ChannelRecord(ScalarType scalarType) {
        Structure structure = StandardFieldFactory.getStandardField().scalar(scalarType, properties);
        pvStructure = pvDataCreate.createPVStructure(structure);
        return new Epics7ChannelRecord(pvStructure, "Test_Channel");
    }

    private Epics7ChannelRecord newEpics7ChannelRecordArray(ScalarType scalarType) {
        StandardField standardField = StandardFieldFactory.getStandardField();
        Structure structure = standardField.scalarArray(scalarType, properties);
        pvStructure = pvDataCreate.createPVStructure(structure);
        return new Epics7ChannelRecord(pvStructure, "Test_Channel");
    }

    /**
     * Test of getStore method, of class Epics7ChannelRecord.
     */
    @Test
    public void testGetStore() {
        System.out.println("getStore");

        Epics7ChannelRecord instance = newEpics7ChannelRecord(ScalarType.pvDouble);

        PVStructure result = instance.getStore();
        assertEquals(pvStructure, result);
    }

    /**
     * Test of getFieldName method, of class Epics7ChannelRecord.
     */
    @Test
    public void testGetFieldName() {
        System.out.println("getFieldName");
        Epics7ChannelRecord instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        String expResult = "value";
        String result = instance.getFieldName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCount method, of class Epics7ChannelRecord.
     */
    @Test
    public void testGetCount() {
        System.out.println("getCount");

        Epics7ChannelRecord instance = new Epics7ChannelRecord(null, "Test_Channel");

        int result = instance.getCount();
        assertEquals(0, result);

        instance = newEpics7ChannelRecord(ScalarType.pvDouble);

        result = instance.getCount();
        assertEquals(1, result);
    }

    @Test
    public void testGetCount_byteA() {
        System.out.println("getCount_byte[]");
        byte[] byteVal = {1, 3, 4};
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvByte);
        PVByteArray pvByteArray = instance.getStore().getSubField(PVByteArray.class, Epics7Channel.VALUE_REQUEST);
        pvByteArray.put(0, byteVal.length, byteVal, 0);
        int result = instance.getCount();
        assertEquals(byteVal.length, result);
    }

    @Test
    public void testGetCount_doubleA() {
        System.out.println("getCount_double[]");
        double[] doubleVal = {1.2, 3.2, 4.5};
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvDouble);
        PVDoubleArray pvDoubleArray = instance.getStore().getSubField(PVDoubleArray.class, Epics7Channel.VALUE_REQUEST);
        pvDoubleArray.put(0, doubleVal.length, doubleVal, 0);
        int result = instance.getCount();
        assertEquals(doubleVal.length, result);
    }

    @Test
    public void testGetCount_floatA() {
        System.out.println("getCount_float[]");
        float[] floatVal = {1.2F, 3.2F, 4.5F};
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvFloat);
        PVFloatArray pvFloatArray = instance.getStore().getSubField(PVFloatArray.class, Epics7Channel.VALUE_REQUEST);
        pvFloatArray.put(0, floatVal.length, floatVal, 0);
        int result = instance.getCount();
        assertEquals(floatVal.length, result);
    }

    @Test
    public void testGetCount_intA() {
        System.out.println("getCount_int[]");
        int[] intVal = {2, 5, 3};
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvInt);
        PVIntArray pvIntArray = instance.getStore().getSubField(PVIntArray.class, Epics7Channel.VALUE_REQUEST);
        pvIntArray.put(0, intVal.length, intVal, 0);
        int result = instance.getCount();
        assertEquals(intVal.length, result);
    }

    @Test
    public void testGetCount_uintA() {
        System.out.println("getCount_uint[]");
        int[] intVal = {2, 5, 3};
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvUInt);
        PVUIntArray pvUIntArray = instance.getStore().getSubField(PVUIntArray.class, Epics7Channel.VALUE_REQUEST);
        pvUIntArray.put(0, intVal.length, intVal, 0);
        int result = instance.getCount();
        assertEquals(intVal.length, result);
    }

    @Test
    public void testGetCount_longA() {
        System.out.println("getCount_long[]");
        long[] longVal = {2, 500000, 3};
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvLong);
        PVLongArray pvLongArray = instance.getStore().getSubField(PVLongArray.class, Epics7Channel.VALUE_REQUEST);
        pvLongArray.put(0, longVal.length, longVal, 0);
        int result = instance.getCount();
        assertEquals(longVal.length, result);
    }

    @Test
    public void testGetCount_ulongA() {
        System.out.println("getCount_ulong[]");
        long[] longVal = {2, 500000, 3};
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvULong);
        PVULongArray pvULongArray = instance.getStore().getSubField(PVULongArray.class, Epics7Channel.VALUE_REQUEST);
        pvULongArray.put(0, longVal.length, longVal, 0);
        int result = instance.getCount();
        assertEquals(longVal.length, result);
    }

    @Test
    public void testGetCount_shortA() {
        System.out.println("getCount_short[]");
        short[] shortVal = {2, 3};
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvShort);
        PVShortArray pvShortArray = instance.getStore().getSubField(PVShortArray.class, Epics7Channel.VALUE_REQUEST);
        pvShortArray.put(0, shortVal.length, shortVal, 0);
        int result = instance.getCount();
        assertEquals(shortVal.length, result);
    }

    @Test
    public void testGetCount_ushortA() {
        System.out.println("getCount_ushort[]");
        short[] shortVal = {2, 3};
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvUShort);
        PVUShortArray pvUShortArray = instance.getStore().getSubField(PVUShortArray.class, Epics7Channel.VALUE_REQUEST);
        pvUShortArray.put(0, shortVal.length, shortVal, 0);
        int result = instance.getCount();
        assertEquals(shortVal.length, result);
    }

    @Test
    public void testGetCount_StringA() {
        System.out.println("getCount_String[]");
        String[] stringVal = {"test1", "test2"};
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvString);
        PVStringArray pvStringArray = instance.getStore().getSubField(PVStringArray.class, Epics7Channel.VALUE_REQUEST);
        pvStringArray.put(0, stringVal.length, stringVal, 0);
        int result = instance.getCount();
        assertEquals(stringVal.length, result);
    }

    @Test
    public void testGetCount_booleanA() {
        System.out.println("getCount_boolean[]");
        boolean[] booleanVal = {true, false};
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvBoolean);
        PVBooleanArray pvBooleanArray = instance.getStore().getSubField(PVBooleanArray.class, Epics7Channel.VALUE_REQUEST);
        pvBooleanArray.put(0, booleanVal.length, booleanVal, 0);
        int result = instance.getCount();
        assertEquals(booleanVal.length, result);
    }

    /**
     * Test of getType method, of class Epics7ChannelRecord.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        Epics7ChannelRecord instance = newEpics7ChannelRecord(ScalarType.pvBoolean);
        assertEquals(boolean.class, instance.getType());

        instance = newEpics7ChannelRecord(ScalarType.pvByte);
        assertEquals(byte.class, instance.getType());

        instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        assertEquals(double.class, instance.getType());

        instance = newEpics7ChannelRecord(ScalarType.pvFloat);
        assertEquals(float.class, instance.getType());

        instance = newEpics7ChannelRecord(ScalarType.pvInt);
        assertEquals(int.class, instance.getType());

        instance = newEpics7ChannelRecord(ScalarType.pvLong);
        assertEquals(long.class, instance.getType());

        instance = newEpics7ChannelRecord(ScalarType.pvShort);
        assertEquals(short.class, instance.getType());

        instance = newEpics7ChannelRecord(ScalarType.pvString);
        assertEquals(String.class, instance.getType());

        instance = newEpics7ChannelRecordArray(ScalarType.pvBoolean);
        assertEquals(boolean[].class, instance.getType());

        instance = newEpics7ChannelRecordArray(ScalarType.pvByte);
        assertEquals(byte[].class, instance.getType());

        instance = newEpics7ChannelRecordArray(ScalarType.pvDouble);
        assertEquals(double[].class, instance.getType());

        instance = newEpics7ChannelRecordArray(ScalarType.pvFloat);
        assertEquals(float[].class, instance.getType());

        instance = newEpics7ChannelRecordArray(ScalarType.pvInt);
        assertEquals(int[].class, instance.getType());

        instance = newEpics7ChannelRecordArray(ScalarType.pvLong);
        assertEquals(long[].class, instance.getType());

        instance = newEpics7ChannelRecordArray(ScalarType.pvShort);
        assertEquals(short[].class, instance.getType());

        instance = newEpics7ChannelRecordArray(ScalarType.pvString);
        assertEquals(String[].class, instance.getType());

        instance = new Epics7ChannelRecord(null, "Test_Channel");
        assertEquals(null, instance.getType());
    }

    /**
     * Test of byteValue method, of class Epics7ChannelRecord.
     */
    @Test
    public void testByteValue() {
        System.out.println("byteValue");
        Epics7ChannelRecord instance = newEpics7ChannelRecord(ScalarType.pvByte);
        byte expResult = 3;
        instance.getStore().getByteField(Epics7Channel.VALUE_REQUEST).put(expResult);
        byte result = instance.byteValue();
        assertEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        instance.getStore().getDoubleField(Epics7Channel.VALUE_REQUEST).put(expResult);

        result = instance.byteValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of byteValueAt method, of class Epics7ChannelRecord.
     */
    @Test
    public void testByteValueAt() {
        System.out.println("byteValueAt");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvByte);
        int index = 0;
        byte expResult = 3;
        PVByteArray pvByteArray = instance.getStore().getSubField(PVByteArray.class, Epics7Channel.VALUE_REQUEST);
        pvByteArray.put(0, 1, new byte[]{expResult}, 0);
        byte result = instance.byteValueAt(index);
        assertEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        instance.getStore().getDoubleField(Epics7Channel.VALUE_REQUEST).put(expResult);

        result = instance.byteValueAt(index);
        assertEquals(expResult, result);
    }

    /**
     * Test of byteArray method, of class Epics7ChannelRecord.
     */
    @Test
    public void testByteArray() {
        System.out.println("byteArray");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvByte);
        byte[] expResult = {3, 2, 4};
        PVByteArray pvByteArray = instance.getStore().getSubField(PVByteArray.class, Epics7Channel.VALUE_REQUEST);
        pvByteArray.put(0, expResult.length, expResult, 0);
        byte[] result = instance.byteArray();
        assertArrayEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        instance.getStore().getDoubleField(Epics7Channel.VALUE_REQUEST).put(3);
        expResult = new byte[]{3};
        result = instance.byteArray();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of shortValue method, of class Epics7ChannelRecord.
     */
    @Test
    public void testShortValue() {
        System.out.println("shortValue");
        Epics7ChannelRecord instance = newEpics7ChannelRecord(ScalarType.pvShort);
        short expResult = 3;
        instance.getStore().getShortField(Epics7Channel.VALUE_REQUEST).put(expResult);
        short result = instance.shortValue();
        assertEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        expResult = 0;
        result = instance.shortValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of shortValueAt method, of class Epics7ChannelRecord.
     */
    @Test
    public void testShortValueAt() {
        System.out.println("shortValueAt");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvShort);
        int index = 0;
        short expResult = 3;
        PVShortArray pvShortArray = instance.getStore().getSubField(PVShortArray.class, Epics7Channel.VALUE_REQUEST);
        pvShortArray.put(0, 1, new short[]{expResult}, 0);
        short result = instance.shortValueAt(index);
        assertEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        instance.getStore().getDoubleField(Epics7Channel.VALUE_REQUEST).put(expResult);

        result = instance.shortValueAt(index);
        assertEquals(expResult, result);
    }

    /**
     * Test of shortArray method, of class Epics7ChannelRecord.
     */
    @Test
    public void testShortArray() {
        System.out.println("shortArray");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvShort);
        short[] expResult = {3, 2, 4};
        PVShortArray pvShortArray = instance.getStore().getSubField(PVShortArray.class, Epics7Channel.VALUE_REQUEST);
        pvShortArray.put(0, expResult.length, expResult, 0);
        short[] result = instance.shortArray();
        assertArrayEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        instance.getStore().getDoubleField(Epics7Channel.VALUE_REQUEST).put(3);
        expResult = new short[]{3};
        result = instance.shortArray();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of intValue method, of class Epics7ChannelRecord.
     */
    @Test
    public void testIntValue() {
        System.out.println("intValue");
        Epics7ChannelRecord instance = newEpics7ChannelRecord(ScalarType.pvInt);
        int expResult = 3;
        instance.getStore().getIntField(Epics7Channel.VALUE_REQUEST).put(expResult);
        int result = instance.intValue();
        assertEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        expResult = 0;
        result = instance.intValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of intValueAt method, of class Epics7ChannelRecord.
     */
    @Test
    public void testIntValueAt() {
        System.out.println("intValueAt");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvInt);
        int index = 0;
        int expResult = 3;
        PVIntArray pvIntArray = instance.getStore().getSubField(PVIntArray.class, Epics7Channel.VALUE_REQUEST);
        pvIntArray.put(0, 1, new int[]{expResult}, 0);
        int result = instance.intValueAt(index);
        assertEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        instance.getStore().getDoubleField(Epics7Channel.VALUE_REQUEST).put(expResult);

        result = instance.intValueAt(index);
        assertEquals(expResult, result);
    }

    /**
     * Test of intArray method, of class Epics7ChannelRecord.
     */
    @Test
    public void testIntArray() {
        System.out.println("intArray");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvInt);
        int[] expResult = {3, 2, 4};
        PVIntArray pvIntArray = instance.getStore().getSubField(PVIntArray.class, Epics7Channel.VALUE_REQUEST);
        pvIntArray.put(0, expResult.length, expResult, 0);
        int[] result = instance.intArray();
        assertArrayEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        instance.getStore().getDoubleField(Epics7Channel.VALUE_REQUEST).put(3);
        expResult = new int[]{3};
        result = instance.intArray();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of floatValue method, of class Epics7ChannelRecord.
     */
    @Test
    public void testFloatValue() {
        System.out.println("floatValue");
        Epics7ChannelRecord instance = newEpics7ChannelRecord(ScalarType.pvFloat);
        float expResult = 3;
        instance.getStore().getFloatField(Epics7Channel.VALUE_REQUEST).put(expResult);
        float result = instance.floatValue();
        assertEquals(expResult, result, 1e-6F);

        instance = newEpics7ChannelRecord(ScalarType.pvInt);
        instance.getStore().getIntField(Epics7Channel.VALUE_REQUEST).put((int) expResult);

        result = instance.floatValue();
        assertEquals(expResult, result, 1e-6F);
    }

    /**
     * Test of floatValueAt method, of class Epics7ChannelRecord.
     */
    @Test
    public void testFloatValueAt() {
        System.out.println("floatValueAt");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvFloat);
        int index = 0;
        float expResult = 3;
        PVFloatArray pvFloatArray = instance.getStore().getSubField(PVFloatArray.class, Epics7Channel.VALUE_REQUEST);
        pvFloatArray.put(0, 1, new float[]{expResult}, 0);
        float result = instance.floatValueAt(index);
        assertEquals(expResult, result, 1e-6F);

        instance = newEpics7ChannelRecord(ScalarType.pvInt);
        instance.getStore().getIntField(Epics7Channel.VALUE_REQUEST).put((int) expResult);

        result = instance.floatValueAt(index);
        assertEquals(expResult, result, 1e-6F);
    }

    /**
     * Test of floatArray method, of class Epics7ChannelRecord.
     */
    @Test
    public void testFloatArray() {
        System.out.println("floatArray");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvFloat);
        float[] expResult = {3, 2, 4};
        PVFloatArray pvFloatArray = instance.getStore().getSubField(PVFloatArray.class, Epics7Channel.VALUE_REQUEST);
        pvFloatArray.put(0, expResult.length, expResult, 0);
        float[] result = instance.floatArray();
        assertArrayEquals(expResult, result, 1e-6F);

        instance = newEpics7ChannelRecord(ScalarType.pvInt);
        instance.getStore().getIntField(Epics7Channel.VALUE_REQUEST).put(3);
        expResult = new float[]{3F};
        result = instance.floatArray();
        assertArrayEquals(expResult, result, 1e-6F);
    }

    /**
     * Test of doubleValue method, of class Epics7ChannelRecord.
     */
    @Test
    public void testDoubleValue() {
        System.out.println("doubleValue");
        Epics7ChannelRecord instance = newEpics7ChannelRecord(ScalarType.pvDouble);
        double expResult = 3;
        instance.getStore().getDoubleField(Epics7Channel.VALUE_REQUEST).put(expResult);
        double result = instance.doubleValue();
        assertEquals(expResult, result, 1e-6F);

        instance = newEpics7ChannelRecord(ScalarType.pvInt);
        instance.getStore().getIntField(Epics7Channel.VALUE_REQUEST).put((int) expResult);

        result = instance.doubleValue();
        assertEquals(expResult, result, 1e-6F);
    }

    /**
     * Test of doubleValueAt method, of class Epics7ChannelRecord.
     */
    @Test
    public void testDoubleValueAt() {
        System.out.println("doubleValueAt");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvDouble);
        int index = 0;
        double expResult = 3;
        PVDoubleArray pvDoubleArray = instance.getStore().getSubField(PVDoubleArray.class, Epics7Channel.VALUE_REQUEST);
        pvDoubleArray.put(0, 1, new double[]{expResult}, 0);
        double result = instance.doubleValueAt(index);
        assertEquals(expResult, result, 1e-6F);

        instance = newEpics7ChannelRecord(ScalarType.pvFloat);
        instance.getStore().getFloatField(Epics7Channel.VALUE_REQUEST).put((float) expResult);

        result = instance.doubleValueAt(index);
        assertEquals(expResult, result, 1e-6F);
    }

    /**
     * Test of doubleArray method, of class Epics7ChannelRecord.
     */
    @Test
    public void testDoubleArray() {
        System.out.println("doubleArray");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvDouble);
        double[] expResult = {3, 2, 4};
        PVDoubleArray pvDoubleArray = instance.getStore().getSubField(PVDoubleArray.class, Epics7Channel.VALUE_REQUEST);
        pvDoubleArray.put(0, expResult.length, expResult, 0);
        double[] result = instance.doubleArray();
        assertArrayEquals(expResult, result, 1e-6F);

        instance = newEpics7ChannelRecord(ScalarType.pvInt);
        instance.getStore().getIntField(Epics7Channel.VALUE_REQUEST).put(3);
        expResult = new double[]{3};

        result = instance.doubleArray();
        assertArrayEquals(expResult, result, 1e-6F);
    }

    /**
     * Test of StringValue method, of class Epics7ChannelRecord.
     */
    @Test
    public void testStringValue() {
        System.out.println("StringValue");
        Epics7ChannelRecord instance = newEpics7ChannelRecord(ScalarType.pvString);
        String expResult = "test";
        instance.getStore().getStringField(Epics7Channel.VALUE_REQUEST).put(expResult);
        String result = instance.stringValue();
        assertEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvInt);
        instance.getStore().getIntField(Epics7Channel.VALUE_REQUEST).put(3);
        expResult = "3";

        result = instance.stringValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of StringValueAt method, of class Epics7ChannelRecord.
     */
    @Test
    public void testStringValueAt() {
        System.out.println("StringValueAt");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvString);
        int index = 0;
        String expResult = "test";
        PVStringArray pvStringArray = instance.getStore().getSubField(PVStringArray.class, Epics7Channel.VALUE_REQUEST);
        pvStringArray.put(0, 1, new String[]{expResult}, 0);

        String result = instance.stringValueAt(index);
        assertEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvInt);
        instance.getStore().getIntField(Epics7Channel.VALUE_REQUEST).put(3);
        expResult = "3";

        result = instance.stringValueAt(index);
        assertEquals(expResult, result);
    }

    /**
     * Test of StringArray method, of class Epics7ChannelRecord.
     */
    @Test
    public void testStringArray() {
        System.out.println("StringArray");
        Epics7ChannelRecord instance = newEpics7ChannelRecordArray(ScalarType.pvString);
        String[] expResult = {"test1", "test2", "test3"};
        PVStringArray pvStringArray = instance.getStore().getSubField(PVStringArray.class, Epics7Channel.VALUE_REQUEST);
        pvStringArray.put(0, expResult.length, expResult, 0);
        String[] result = instance.stringArray();
        assertArrayEquals(expResult, result);

        instance = newEpics7ChannelRecord(ScalarType.pvInt);
        instance.getStore().getIntField(Epics7Channel.VALUE_REQUEST).put(3);
        expResult = new String[]{"3"};

        result = instance.stringArray();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of toString method, of class Epics7ChannelRecord.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Epics7ChannelRecord instance = new Epics7ChannelRecord(null, "Test_Channel");
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);

        instance = newEpics7ChannelRecordArray(ScalarType.pvString);

        expResult = "value: " + pvStructure.toString();
        result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of applyTransform method, of class Epics7ChannelRecord.
     */
    @Test
    public void testApplyTransform() {
        System.out.println("applyTransform");
        Epics7ChannelRecord instance = new Epics7ChannelRecord(null, "Test_Channel");
        ChannelRecord result = instance.applyTransform(null);
        assertEquals(instance, result);
    }

    /**
     * Test of arrayValue method, of class Epics7ChannelRecord.
     */
    @Test
    public void testArrayValue() {
        System.out.println("arrayValue");

        boolean exceptionThrown = false;
        Epics7ChannelRecord instance = new Epics7ChannelRecord(null, "Test_Channel");
        try {
            instance.arrayValue();
        } catch (Exception ex) {
            exceptionThrown = true;
        }
        assertEquals(true, exceptionThrown);
    }
}
