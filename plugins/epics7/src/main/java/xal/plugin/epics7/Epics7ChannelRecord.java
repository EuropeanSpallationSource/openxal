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

    private static final String VALUE_FIELD_NAME = "value";

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
        PVField valueField = store.getSubField(fieldName);
        Type type = valueField.getField().getType();
        switch (type) {
            case scalar:
                return 1;
            case scalarArray:
                return getCountArray(valueField);
            default:
                break;
        }
        return 0;
    }

    private int getCountArray(PVField valueField) {
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
        try {
            return store.getByteField(fieldName).get();
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not byte. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return 0;
        }
    }

    @Override
    public byte byteValueAt(final int index) {
        PVByteArray byteArray = (PVByteArray) store.getScalarArrayField(fieldName, ScalarType.pvByte);
        try {
            return byteArray.get().getByte(index);
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not byte[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return 0;
        }
    }

    @Override
    public byte[] byteArray() {
        PVByteArray byteArray = (PVByteArray) store.getScalarArrayField(fieldName, ScalarType.pvByte);
        try {
            return byteArray.get().toArray(new byte[byteArray.getLength()]);
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not byte[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return new byte[]{};
        }
    }

    @Override
    public short shortValue() {
        try {
            return store.getShortField(fieldName).get();
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not short. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return 0;
        }
    }

    @Override
    public short shortValueAt(final int index) {
        PVShortArray shortArray = (PVShortArray) store.getScalarArrayField(fieldName, ScalarType.pvShort);
        try {
            return shortArray.get().getShort(index);
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not short[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return 0;
        }
    }

    @Override
    public short[] shortArray() {
        PVShortArray shortArray = (PVShortArray) store.getScalarArrayField(fieldName, ScalarType.pvShort);
        try {
            return shortArray.get().toArray(new short[shortArray.getLength()]);
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not short[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return new short[]{};
        }
    }

    @Override
    public int intValue() {
        try {
            return store.getIntField(fieldName).get();
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not int. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return 0;
        }
    }

    @Override
    public int intValueAt(final int index) {
        PVIntArray intArray = (PVIntArray) store.getScalarArrayField(fieldName, ScalarType.pvInt);
        try {
            return intArray.get().getInt(index);
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not int[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return 0;
        }
    }

    @Override
    public int[] intArray() {
        PVIntArray intArray = (PVIntArray) store.getScalarArrayField(fieldName, ScalarType.pvInt);
        try {
            return intArray.get().toArray(new int[intArray.getLength()]);
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not int[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return new int[]{};
        }
    }

    @Override
    public float floatValue() {
        try {
            return store.getFloatField(fieldName).get();
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not float. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return Float.NaN;
        }
    }

    @Override
    public float floatValueAt(final int index) {
        PVFloatArray floatArray = (PVFloatArray) store.getScalarArrayField(fieldName, ScalarType.pvFloat);
        try {
            return floatArray.get().getFloat(index);
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not float[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return Float.NaN;
        }
    }

    @Override
    public float[] floatArray() {
        PVFloatArray floatArray = (PVFloatArray) store.getScalarArrayField(fieldName, ScalarType.pvFloat);
        try {
            return floatArray.get().toArray(new float[floatArray.getLength()]);
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not float[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return new float[]{Float.NaN};
        }
    }

    @Override
    public double doubleValue() {
        try {
            return store.getDoubleField(fieldName).get();
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not double. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return Double.NaN;
        }
    }

    @Override
    public double doubleValueAt(final int index) {
        try {
            PVDoubleArray doubleArray = (PVDoubleArray) store.getScalarArrayField(fieldName, ScalarType.pvDouble);
            return doubleArray.get().getDouble(index);
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not double[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return Double.NaN;
        }
    }

    @Override
    public double[] doubleArray() {
        PVDoubleArray doubleArray = (PVDoubleArray) store.getScalarArrayField(fieldName, ScalarType.pvDouble);
        try {
            return doubleArray.get().toArray(new double[doubleArray.getLength()]);
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not double[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return new double[]{Double.NaN};
        }
    }

    @Override
    public String stringValue() {
        try {
            return store.getStringField(fieldName).get();
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not String. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return null;
        }
    }

    @Override
    public String stringValueAt(final int index) {
        PVStringArray stringArray = (PVStringArray) store.getScalarArrayField(fieldName, ScalarType.pvString);
        StringArrayData stringArrayData = new StringArrayData();
        try {
            stringArray.get(0, stringArray.getLength(), stringArrayData);
            return stringArrayData.data[index];
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not String[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return null;
        }
    }

    @Override
    public String[] stringArray() {
        PVStringArray stringArray = (PVStringArray) store.getScalarArrayField(fieldName, ScalarType.pvString);
        StringArrayData stringArrayData = new StringArrayData();
        try {
            stringArray.get(0, stringArray.getLength(), stringArrayData);
            return stringArrayData.data;
        } catch (Exception e) {
            Logger.getLogger(Epics7ChannelRecord.class.getName()).log(Level.SEVERE,
                    "Type of field {0} in {1} is not String[]. Type = {2}. Use the corresponding method.",
                    new Object[]{fieldName, channelName, getType().getCanonicalName()});
            return new String[]{};
        }
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
