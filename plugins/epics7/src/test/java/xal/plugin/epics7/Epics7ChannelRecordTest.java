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

import org.epics.pva.data.PVABool;
import org.epics.pva.data.PVABoolArray;
import org.epics.pva.data.PVAByte;
import org.epics.pva.data.PVAByteArray;
import org.epics.pva.data.PVADouble;
import org.epics.pva.data.PVADoubleArray;
import org.epics.pva.data.PVAFloat;
import org.epics.pva.data.PVAFloatArray;
import org.epics.pva.data.PVAInt;
import org.epics.pva.data.PVAIntArray;
import org.epics.pva.data.PVALong;
import org.epics.pva.data.PVALongArray;
import org.epics.pva.data.PVAShort;
import org.epics.pva.data.PVAShortArray;
import org.epics.pva.data.PVAString;
import org.epics.pva.data.PVAStringArray;
import org.epics.pva.data.PVAStructure;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static xal.plugin.epics7.TestData.VALUE;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelRecordTest {

    private static Epics7ChannelRecord record(org.epics.pva.data.PVAData value) {
        return new Epics7ChannelRecord(TestData.valueOnly(value));
    }

    @Test
    public void testGetStoreAndFieldName() {
        PVAStructure structure = TestData.doubleRecord(1.5);
        Epics7ChannelRecord instance = new Epics7ChannelRecord(structure);

        assertSame(structure, instance.getStore());
        assertEquals(VALUE, instance.getFieldName());
    }

    @Test
    public void testNullStore() {
        Epics7ChannelRecord instance = new Epics7ChannelRecord(null);

        assertEquals(0, instance.getCount());
        assertNull(instance.getType());
        assertEquals("", instance.toString());
    }

    @Test
    public void testScalarCountIsOne() {
        assertEquals(1, record(new PVADouble(VALUE, 3.0)).getCount());
        assertEquals(1, record(new PVAString(VALUE, "x")).getCount());
        assertEquals(1, record(new PVABool(VALUE, true)).getCount());
    }

    @Test
    public void testArrayCountIsLength() {
        assertEquals(3, record(new PVADoubleArray(VALUE, 1.0, 2.0, 3.0)).getCount());
        assertEquals(2, record(new PVAIntArray(VALUE, false, 1, 2)).getCount());
        assertEquals(4, record(new PVAStringArray(VALUE, "a", "b", "c", "d")).getCount());
        assertEquals(0, record(new PVADoubleArray(VALUE)).getCount());
    }

    @Test
    public void testScalarTypes() {
        assertEquals(boolean.class, record(new PVABool(VALUE, true)).getType());
        assertEquals(byte.class, record(new PVAByte(VALUE, false, (byte) 1)).getType());
        assertEquals(short.class, record(new PVAShort(VALUE, false, (short) 1)).getType());
        assertEquals(int.class, record(new PVAInt(VALUE, 1)).getType());
        assertEquals(long.class, record(new PVALong(VALUE, false, 1L)).getType());
        assertEquals(float.class, record(new PVAFloat(VALUE, 1f)).getType());
        assertEquals(double.class, record(new PVADouble(VALUE, 1.0)).getType());
        assertEquals(String.class, record(new PVAString(VALUE, "x")).getType());
    }

    @Test
    public void testArrayTypes() {
        assertEquals(boolean[].class, record(new PVABoolArray(VALUE, true)).getType());
        assertEquals(byte[].class, record(new PVAByteArray(VALUE, false, (byte) 1)).getType());
        assertEquals(short[].class, record(new PVAShortArray(VALUE, false, (short) 1)).getType());
        assertEquals(int[].class, record(new PVAIntArray(VALUE, false, 1)).getType());
        assertEquals(long[].class, record(new PVALongArray(VALUE, false, 1L)).getType());
        assertEquals(float[].class, record(new PVAFloatArray(VALUE, 1f)).getType());
        assertEquals(double[].class, record(new PVADoubleArray(VALUE, 1.0)).getType());
        assertEquals(String[].class, record(new PVAStringArray(VALUE, "x")).getType());
    }

    /**
     * Unsigned types map onto the same Java type as their signed counterparts.
     */
    @Test
    public void testUnsignedTypes() {
        assertEquals(byte.class, record(new PVAByte(VALUE, true, (byte) 1)).getType());
        assertEquals(int.class, record(new PVAInt(VALUE, true, 1)).getType());
        assertEquals(long.class, record(new PVALong(VALUE, true, 1L)).getType());
    }

    @Test
    public void testScalarValueAccessors() {
        Epics7ChannelRecord instance = record(new PVADouble(VALUE, 42.0));

        assertEquals(42, instance.byteValue());
        assertEquals(42, instance.shortValue());
        assertEquals(42, instance.intValue());
        assertEquals(42L, instance.longValue());
        assertEquals(42.0f, instance.floatValue(), 0.0f);
        assertEquals(42.0, instance.doubleValue(), 0.0);
        assertEquals("42.0", instance.stringValue());
    }

    @Test
    public void testArrayValueAccessors() {
        Epics7ChannelRecord instance = record(new PVADoubleArray(VALUE, 1.0, 2.0, 3.0));

        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, instance.doubleArray(), 0.0);
        assertArrayEquals(new int[]{1, 2, 3}, instance.intArray());
        assertArrayEquals(new long[]{1L, 2L, 3L}, instance.longArray());
        assertArrayEquals(new short[]{1, 2, 3}, instance.shortArray());
        assertArrayEquals(new byte[]{1, 2, 3}, instance.byteArray());
        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f}, instance.floatArray(), 0.0f);

        assertEquals(2.0, instance.doubleValueAt(1), 0.0);
        assertEquals(3, instance.intValueAt(2));
        assertEquals(1L, instance.longValueAt(0));
        assertEquals(2.0f, instance.floatValueAt(1), 0.0f);
        assertEquals(3, instance.shortValueAt(2));
        assertEquals(1, instance.byteValueAt(0));
    }

    @Test
    public void testStringArrayAccessors() {
        Epics7ChannelRecord instance = record(new PVAStringArray(VALUE, "a", "b"));

        assertArrayEquals(new String[]{"a", "b"}, instance.stringArray());
        assertEquals("b", instance.stringValueAt(1));
    }

    @Test
    public void testApplyTransformReturnsSameRecord() {
        Epics7ChannelRecord instance = record(new PVADouble(VALUE, 1.0));
        assertSame(instance, instance.applyTransform(null));
    }

    @Test
    public void testArrayValueIsUnsupported() {
        Epics7ChannelRecord instance = record(new PVADouble(VALUE, 1.0));
        assertThrows(UnsupportedOperationException.class, instance::arrayValue);
    }

    @Test
    public void testToString() {
        Epics7ChannelRecord instance = record(new PVADouble(VALUE, 1.0));
        org.junit.Assert.assertTrue(instance.toString().startsWith("value: "));
    }
}
