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

import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvdata.pv.Type;
import xal.ca.ChannelRecord;
import xal.tools.ArrayValue;
import xal.tools.transforms.ValueTransform;

/**
 * ChannelRecord implementation for Epics7. It stores the PVStructure data to
 * make it possible to use PVAccess Structures in Open XAL without making
 * significant changes to the core library.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelRecord extends ChannelRecord {

    protected PVStructure store;
    protected String fieldName;

    /**
     * Constructor
     *
     * @param pvStructure
     * @param fieldName
     */
    public Epics7ChannelRecord(PVStructure pvStructure, String fieldName) {
        super(() -> {
            return null;
        });

        store = pvStructure;
        this.fieldName = fieldName;
    }

    /**
     * Get the internal storage.
     *
     * @return The internal data storage.
     */
    public PVStructure getStore() {
        return store;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public int getCount() {
        PVField valueField = store.getSubField(fieldName);
        Type type = valueField.getField().getType();
        switch (type) {
            case scalar:
                return 1;
            case scalarArray:
                ScalarType sType = ((PVScalarArray) valueField).getScalarArray().getElementType();
                switch (sType) {
                    case pvByte:
                    case pvUByte:
                        return store.getScalarArrayField(fieldName, ScalarType.pvByte).getLength();
                    case pvDouble:
                        return store.getScalarArrayField(fieldName, ScalarType.pvDouble).getLength();
                    case pvFloat:
                        return store.getScalarArrayField(fieldName, ScalarType.pvFloat).getLength();
                    case pvInt:
                        return store.getScalarArrayField(fieldName, ScalarType.pvInt).getLength();
                    case pvUInt:
                        return store.getScalarArrayField(fieldName, ScalarType.pvUInt).getLength();
                    case pvLong:
                        return store.getScalarArrayField(fieldName, ScalarType.pvLong).getLength();
                    case pvULong:
                        return store.getScalarArrayField(fieldName, ScalarType.pvULong).getLength();
                    case pvShort:
                        return store.getScalarArrayField(fieldName, ScalarType.pvShort).getLength();
                    case pvUShort:
                        return store.getScalarArrayField(fieldName, ScalarType.pvUShort).getLength();
                    case pvString:
                        return store.getScalarArrayField(fieldName, ScalarType.pvString).getLength();
                    case pvBoolean:
                        return store.getScalarArrayField(fieldName, ScalarType.pvBoolean).getLength();
                    default:
                        break;
                }
        }
        return 0;
    }

    @Override
    public Class<?> getType() {
        PVField valueField = store.getSubField(fieldName);
        Type type = valueField.getField().getType();
        switch (type) {
            case scalar:
                return getScalarType((PVScalar) valueField);
            case scalarArray:
                return getScalarArrayType((PVScalarArray) valueField);
            default:
                break;
        }
        return null;
    }

    private Class<?> getScalarType(PVScalar pvScalar) {
        ScalarType type = pvScalar.getScalar().getScalarType();
        switch (type) {
            case pvBoolean:
                return boolean.class;
            case pvByte:
            case pvUByte:
                return byte.class;
            case pvDouble:
                return double.class;
            case pvFloat:
                return float.class;
            case pvInt:
            case pvUInt:
                return int.class;
            case pvLong:
            case pvULong:
                return long.class;
            case pvShort:
            case pvUShort:
                return short.class;
            case pvString:
                return String.class;
            default:
                break;
        }
        return null;
    }

    private Class<?> getScalarArrayType(PVScalarArray pvScalarArray) {
        ScalarType type = pvScalarArray.getScalarArray().getElementType();
        switch (type) {
            case pvByte:
            case pvUByte:
                return byte[].class;
            case pvDouble:
                return double[].class;
            case pvFloat:
                return float[].class;
            case pvInt:
            case pvUInt:
                return int[].class;
            case pvLong:
            case pvULong:
                return long[].class;
            case pvShort:
            case pvUShort:
                return short[].class;
            case pvString:
                return String[].class;
            case pvBoolean:
                return boolean[].class;
            default:
                break;
        }
        return null;
    }

    @Override
    public byte byteValue() {
        return store.getByteField(fieldName).get();
    }

    @Override
    public byte byteValueAt(final int index) {
        PVByteArray byteArray = (PVByteArray) store.getScalarArrayField(fieldName, ScalarType.pvByte);
        return byteArray.get().getByte(index);
    }

    @Override
    public byte[] byteArray() {
        PVByteArray byteArray = (PVByteArray) store.getScalarArrayField(fieldName, ScalarType.pvByte);
        return byteArray.get().toArray(new byte[byteArray.getLength()]);
    }

    @Override
    public short shortValue() {
        return store.getShortField(fieldName).get();
    }

    @Override
    public short shortValueAt(final int index) {
        PVShortArray shortArray = (PVShortArray) store.getScalarArrayField(fieldName, ScalarType.pvShort);
        return shortArray.get().getShort(index);
    }

    @Override
    public short[] shortArray() {
        PVShortArray shortArray = (PVShortArray) store.getScalarArrayField(fieldName, ScalarType.pvShort);
        return shortArray.get().toArray(new short[shortArray.getLength()]);
    }

    @Override
    public int intValue() {
        return store.getIntField(fieldName).get();
    }

    @Override
    public int intValueAt(final int index) {
        PVIntArray intArray = (PVIntArray) store.getScalarArrayField(fieldName, ScalarType.pvInt);
        return intArray.get().getInt(index);
    }

    @Override
    public int[] intArray() {
        PVIntArray intArray = (PVIntArray) store.getScalarArrayField(fieldName, ScalarType.pvInt);
        return intArray.get().toArray(new int[intArray.getLength()]);
    }

    @Override
    public float floatValue() {
        return store.getFloatField(fieldName).get();
    }

    @Override
    public float floatValueAt(final int index) {
        PVFloatArray floatArray = (PVFloatArray) store.getScalarArrayField(fieldName, ScalarType.pvFloat);
        return floatArray.get().getFloat(index);
    }

    @Override
    public float[] floatArray() {
        PVFloatArray floatArray = (PVFloatArray) store.getScalarArrayField(fieldName, ScalarType.pvFloat);
        return floatArray.get().toArray(new float[floatArray.getLength()]);
    }

    @Override
    public double doubleValue() {
        return store.getDoubleField(fieldName).get();
    }

    @Override
    public double doubleValueAt(final int index) {
        PVDoubleArray doubleArray = (PVDoubleArray) store.getScalarArrayField(fieldName, ScalarType.pvDouble);
        return doubleArray.get().getDouble(index);
    }

    @Override
    public double[] doubleArray() {
        PVDoubleArray doubleArray = (PVDoubleArray) store.getScalarArrayField(fieldName, ScalarType.pvDouble);
        return doubleArray.get().toArray(new double[doubleArray.getLength()]);
    }

    @Override
    public String stringValue() {
        return store.getStringField(fieldName).get();
    }

    @Override
    public String stringValueAt(final int index) {
        PVStringArray stringArray = (PVStringArray) store.getScalarArrayField(fieldName, ScalarType.pvString);
        StringArrayData stringArrayData = new StringArrayData();
        stringArray.get(0, stringArray.getLength(), stringArrayData);
        return stringArrayData.data[index];
    }

    @Override
    public String[] stringArray() {
        PVStringArray stringArray = (PVStringArray) store.getScalarArrayField(fieldName, ScalarType.pvString);
        StringArrayData stringArrayData = new StringArrayData();
        stringArray.get(0, stringArray.getLength(), stringArrayData);
        return stringArrayData.data;
    }

    @Override
    public String toString() {
        return "value: " + store.toString();
    }

    ChannelRecord applyTransform(final ValueTransform transform) throws Exception {
        throw new Exception("Not implemented in Epics7ChannelRecord.");
    }

    ArrayValue arrayValue() throws Exception {
        throw new Exception("Not implemented in Epics7ChannelRecord. Use Epics7ChannelRecord#getStore() instead.");
    }
}
