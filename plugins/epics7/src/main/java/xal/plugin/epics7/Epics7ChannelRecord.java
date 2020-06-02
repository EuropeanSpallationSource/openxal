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

import org.epics.pvdata.pv.BooleanArrayData;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvdata.pv.Type;
import xal.ca.ChannelRecord;
import xal.ca.ChannelRecordImpl;
import xal.tools.ArrayValue;
import xal.tools.transforms.ValueTransform;

/**
 * ChannelRecord implementation for Epics7. It stores the PVStructure data to
 * make it possible to use PVAccess Structures in Open XAL without making
 * significant changes to the core library.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelRecord extends ChannelRecordImpl {

    protected PVStructure store;
    protected String channelName;
    protected String fieldName;

    protected static final String VALUE_FIELD_NAME = "value";

    /**
     * Constructor
     *
     * @param pvStructure
     * @param channelName
     */
    public Epics7ChannelRecord(PVStructure pvStructure, String channelName) {
        super(() -> {
            return null;
        });

        store = pvStructure;
        this.channelName = channelName;
        this.fieldName = VALUE_FIELD_NAME;
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
        if (store != null) {
            PVField valueField = store.getSubField(VALUE_FIELD_NAME);
            Type type = valueField.getField().getType();
            switch (type) {
                case scalar:
                    return 1;
                case scalarArray:
                    return getCountArray(store, valueField);
                default:
                    break;
            }
        }
        return 0;
    }

    public static int getCountArray(PVStructure structure, PVField valueField) {
        ScalarType sType = ((PVScalarArray) valueField).getScalarArray().getElementType();
        switch (sType) {
            case pvByte:
            case pvUByte:
                return structure.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvByte).getLength();
            case pvDouble:
                return structure.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvDouble).getLength();
            case pvFloat:
                return structure.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvFloat).getLength();
            case pvInt:
                return structure.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvInt).getLength();
            case pvUInt:
                return structure.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvUInt).getLength();
            case pvLong:
                return structure.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvLong).getLength();
            case pvULong:
                return structure.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvULong).getLength();
            case pvShort:
                return structure.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvShort).getLength();
            case pvUShort:
                return structure.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvUShort).getLength();
            case pvString:
                return structure.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvString).getLength();
            case pvBoolean:
                return structure.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvBoolean).getLength();
            default:
                break;
        }
        return 0;
    }

    @Override
    public Class<?> getType() {
        if (store != null) {
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

    private Object getValue() {
        if (store != null) {
            PVField valueField = store.getSubField(VALUE_FIELD_NAME);
            Type type = valueField.getField().getType();
            switch (type) {
                case scalar:
                    return getScalarValue((PVScalar) valueField);
                case scalarArray:
                    return getArrayValue((PVScalarArray) valueField);
                default:
                    break;
            }
        }
        return null;
    }

    private Object getScalarValue(PVScalar pvScalar) {
        ScalarType type = pvScalar.getScalar().getScalarType();
        switch (type) {
            case pvBoolean:
                return new boolean[]{store.getBooleanField(fieldName).get()};
            case pvByte:
            case pvUByte:
                return new byte[]{store.getByteField(fieldName).get()};
            case pvDouble:
                return new double[]{store.getDoubleField(fieldName).get()};
            case pvFloat:
                return new float[]{store.getFloatField(fieldName).get()};
            case pvInt:
            case pvUInt:
                return new int[]{store.getIntField(fieldName).get()};
            case pvLong:
            case pvULong:
                return new long[]{store.getLongField(fieldName).get()};
            case pvShort:
            case pvUShort:
                return new short[]{store.getShortField(fieldName).get()};
            case pvString:
                return new String[]{store.getStringField(fieldName).get()};
            default:
                break;
        }
        return null;
    }

    private Object getArrayValue(PVScalarArray pvScalarArray) {
        ScalarType type = pvScalarArray.getScalarArray().getElementType();
        switch (type) {
            case pvByte:
            case pvUByte:
                PVByteArray byteArray = (PVByteArray) store.getScalarArrayField(fieldName, ScalarType.pvByte);
                return byteArray.get().toArray(new byte[byteArray.getLength()]);
            case pvDouble:
                PVDoubleArray doubleArray = (PVDoubleArray) store.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvDouble);
                return doubleArray.get().toArray(new double[doubleArray.getLength()]);
            case pvFloat:
                PVFloatArray floatArray = (PVFloatArray) store.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvFloat);
                return floatArray.get().toArray(new float[floatArray.getLength()]);
            case pvInt:
            case pvUInt:
                PVIntArray intArray = (PVIntArray) store.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvInt);
                return intArray.get().toArray(new int[intArray.getLength()]);
            case pvLong:
            case pvULong:
                PVLongArray longArray = (PVLongArray) store.getScalarArrayField(fieldName, ScalarType.pvLong);
                return longArray.get().toArray(new long[longArray.getLength()]);
            case pvShort:
            case pvUShort:
                PVShortArray shortArray = (PVShortArray) store.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvShort);
                return shortArray.get().toArray(new short[shortArray.getLength()]);
            case pvString:
                PVStringArray stringArray = (PVStringArray) store.getScalarArrayField(VALUE_FIELD_NAME, ScalarType.pvString);
                StringArrayData stringArrayData = new StringArrayData();
                stringArray.get(0, stringArray.getLength(), stringArrayData);
                return stringArrayData.data;
            case pvBoolean:
                PVBooleanArray booleanArray = (PVBooleanArray) store.getScalarArrayField(fieldName, ScalarType.pvBoolean);
                BooleanArrayData booleanArrayData = new BooleanArrayData();
                booleanArray.get(0, booleanArray.getLength(), booleanArrayData);
                return booleanArrayData.data;
            default:
                break;
        }
        return null;
    }

    @Override
    public byte byteValue() {
        return ArrayValue.arrayValueFromArray(getValue()).byteValue();
    }

    @Override
    public byte byteValueAt(final int index) {
        return ArrayValue.arrayValueFromArray(getValue()).byteValueAt(index);
    }

    @Override
    public byte[] byteArray() {
        return ArrayValue.arrayValueFromArray(getValue()).byteArray();
    }

    @Override
    public short shortValue() {
        return ArrayValue.arrayValueFromArray(getValue()).shortValue();
    }

    @Override
    public short shortValueAt(final int index) {
        return ArrayValue.arrayValueFromArray(getValue()).shortValueAt(index);
    }

    @Override
    public short[] shortArray() {
        return ArrayValue.arrayValueFromArray(getValue()).shortArray();
    }

    @Override
    public int intValue() {
        return ArrayValue.arrayValueFromArray(getValue()).intValue();
    }

    @Override
    public int intValueAt(final int index) {
        return ArrayValue.arrayValueFromArray(getValue()).intValueAt(index);
    }

    @Override
    public int[] intArray() {
        return ArrayValue.arrayValueFromArray(getValue()).intArray();
    }

    @Override
    public long longValue() {
        return ArrayValue.arrayValueFromArray(getValue()).longValue();
    }

    @Override
    public long longValueAt(final int index) {
        return ArrayValue.arrayValueFromArray(getValue()).longValueAt(index);
    }

    @Override
    public long[] longArray() {
        return ArrayValue.arrayValueFromArray(getValue()).longArray();
    }

    @Override
    public float floatValue() {
        return ArrayValue.arrayValueFromArray(getValue()).floatValue();
    }

    @Override
    public float floatValueAt(final int index) {
        return ArrayValue.arrayValueFromArray(getValue()).floatValueAt(index);
    }

    @Override
    public float[] floatArray() {
        return ArrayValue.arrayValueFromArray(getValue()).floatArray();
    }

    @Override
    public double doubleValue() {
        return ArrayValue.arrayValueFromArray(getValue()).doubleValue();
    }

    @Override
    public double doubleValueAt(final int index) {
        return ArrayValue.arrayValueFromArray(getValue()).doubleValueAt(index);
    }

    @Override
    public double[] doubleArray() {
        return ArrayValue.arrayValueFromArray(getValue()).doubleArray();
    }

    @Override
    public String stringValue() {
        return ArrayValue.arrayValueFromArray(getValue()).stringValue();
    }

    @Override
    public String stringValueAt(final int index) {
        return ArrayValue.arrayValueFromArray(getValue()).stringValueAt(index);
    }

    @Override
    public String[] stringArray() {
        return ArrayValue.arrayValueFromArray(getValue()).stringArray();
    }

    @Override
    public String toString() {
        if (store != null) {
            return "value: " + store.toString();
        }
        return "";
    }

    // TODO: implement transformations.
    @Override
    public ChannelRecord applyTransform(ValueTransform transform) {
        return this;
    }

    @Override
    public ArrayValue arrayValue() {
        throw new UnsupportedOperationException("Not implemented in Epics7ChannelRecord.");
    }
}
