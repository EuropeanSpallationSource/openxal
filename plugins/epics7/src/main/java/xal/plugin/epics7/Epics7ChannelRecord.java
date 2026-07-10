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
import org.epics.pva.data.PVAData;
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
import xal.ca.ChannelRecord;
import xal.ca.ChannelRecordImpl;
import xal.tools.ArrayValue;
import xal.tools.transforms.ValueTransform;

/**
 * ChannelRecord implementation for Epics7. It stores the PVAStructure data to make it possible to use PV Access
 * structures in Open XAL without making significant changes to the core library.
 *
 * Channel Access values are converted to the same structure form by {@link CaDbrConverter}, so this class does not need
 * to know which protocol produced the data.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelRecord extends ChannelRecordImpl {

    protected PVAStructure pvStructureStore;
    protected String fieldName;

    protected static final String VALUE_FIELD_NAME = "value";

    /**
     * Constructor
     *
     * @param pvStructure
     */
    public Epics7ChannelRecord(PVAStructure pvStructure) {
        super(() -> null);

        pvStructureStore = pvStructure;
        this.fieldName = VALUE_FIELD_NAME;
    }

    /**
     * Get the internal storage.
     *
     * @return The internal data storage.
     */
    public PVAStructure getStore() {
        return pvStructureStore;
    }

    public String getFieldName() {
        return fieldName;
    }

    private PVAData valueField() {
        return pvStructureStore == null ? null : pvStructureStore.get(fieldName);
    }

    @Override
    public int getCount() {
        return getCountArray(valueField());
    }

    /**
     * The number of elements held by a value field: 1 for a scalar, the array length otherwise.
     */
    public static int getCountArray(PVAData valueField) {
        if (valueField == null) {
            return 0;
        }
        if (valueField instanceof PVAByteArray) {
            return ((PVAByteArray) valueField).get().length;
        }
        if (valueField instanceof PVAShortArray) {
            return ((PVAShortArray) valueField).get().length;
        }
        if (valueField instanceof PVAIntArray) {
            return ((PVAIntArray) valueField).get().length;
        }
        if (valueField instanceof PVALongArray) {
            return ((PVALongArray) valueField).get().length;
        }
        if (valueField instanceof PVAFloatArray) {
            return ((PVAFloatArray) valueField).get().length;
        }
        if (valueField instanceof PVADoubleArray) {
            return ((PVADoubleArray) valueField).get().length;
        }
        if (valueField instanceof PVAStringArray) {
            return ((PVAStringArray) valueField).get().length;
        }
        if (valueField instanceof PVABoolArray) {
            return ((PVABoolArray) valueField).get().length;
        }
        // Any scalar.
        return 1;
    }

    @Override
    public Class<?> getType() {
        PVAData valueField = valueField();
        if (valueField == null) {
            return null;
        }
        if (valueField instanceof PVABool) {
            return boolean.class;
        }
        if (valueField instanceof PVAByte) {
            return byte.class;
        }
        if (valueField instanceof PVAShort) {
            return short.class;
        }
        if (valueField instanceof PVAInt) {
            return int.class;
        }
        if (valueField instanceof PVALong) {
            return long.class;
        }
        if (valueField instanceof PVAFloat) {
            return float.class;
        }
        if (valueField instanceof PVADouble) {
            return double.class;
        }
        if (valueField instanceof PVAString) {
            return String.class;
        }
        if (valueField instanceof PVABoolArray) {
            return boolean[].class;
        }
        if (valueField instanceof PVAByteArray) {
            return byte[].class;
        }
        if (valueField instanceof PVAShortArray) {
            return short[].class;
        }
        if (valueField instanceof PVAIntArray) {
            return int[].class;
        }
        if (valueField instanceof PVALongArray) {
            return long[].class;
        }
        if (valueField instanceof PVAFloatArray) {
            return float[].class;
        }
        if (valueField instanceof PVADoubleArray) {
            return double[].class;
        }
        if (valueField instanceof PVAStringArray) {
            return String[].class;
        }
        return null;
    }

    /**
     * The value as a Java array, which is what {@link ArrayValue} expects. Scalars become single element arrays.
     */
    private Object getValue() {
        PVAData valueField = valueField();
        if (valueField == null) {
            return null;
        }
        if (valueField instanceof PVABool) {
            return new boolean[]{((PVABool) valueField).get()};
        }
        if (valueField instanceof PVAByte) {
            return new byte[]{((PVAByte) valueField).get()};
        }
        if (valueField instanceof PVAShort) {
            return new short[]{((PVAShort) valueField).get()};
        }
        if (valueField instanceof PVAInt) {
            return new int[]{((PVAInt) valueField).get()};
        }
        if (valueField instanceof PVALong) {
            return new long[]{((PVALong) valueField).get()};
        }
        if (valueField instanceof PVAFloat) {
            return new float[]{((PVAFloat) valueField).get()};
        }
        if (valueField instanceof PVADouble) {
            return new double[]{((PVADouble) valueField).get()};
        }
        if (valueField instanceof PVAString) {
            return new String[]{((PVAString) valueField).get()};
        }
        if (valueField instanceof PVABoolArray) {
            return ((PVABoolArray) valueField).get();
        }
        if (valueField instanceof PVAByteArray) {
            return ((PVAByteArray) valueField).get();
        }
        if (valueField instanceof PVAShortArray) {
            return ((PVAShortArray) valueField).get();
        }
        if (valueField instanceof PVAIntArray) {
            return ((PVAIntArray) valueField).get();
        }
        if (valueField instanceof PVALongArray) {
            return ((PVALongArray) valueField).get();
        }
        if (valueField instanceof PVAFloatArray) {
            return ((PVAFloatArray) valueField).get();
        }
        if (valueField instanceof PVADoubleArray) {
            return ((PVADoubleArray) valueField).get();
        }
        if (valueField instanceof PVAStringArray) {
            return ((PVAStringArray) valueField).get();
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
        if (pvStructureStore != null) {
            return "value: " + pvStructureStore.toString();
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
