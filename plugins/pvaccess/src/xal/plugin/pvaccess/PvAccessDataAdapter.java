package xal.plugin.pvaccess;

import java.math.BigDecimal;
import java.util.Arrays;

import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.pv.*;

import xal.ca.TimeAdaptor;
import xal.tools.ArrayValue;

/**
 * Adaptor for data received from the channel.
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class PvAccessDataAdapter implements TimeAdaptor {
    
    private final PVStructure structure;
    private final String defaultField;
    	    
    /**
     * Constructor 
     * @param pvStructure Data received from the channel
     */
    public PvAccessDataAdapter(PVStructure pvStructure) {
        this(pvStructure, PvAccessChannel.VALUE_FIELD_NAME);
    }

    /**
     * Constructor 
     * @param pvStructure Data received from the channel
     * @param defaultField Default field to return as value field
     */
    public PvAccessDataAdapter(PVStructure pvStructure, String defaultField) {
        this.structure = pvStructure;
        this.defaultField = defaultField;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getTimestamp() {
        PVStructure timestampStructure = structure.getStructureField(PvAccessChannel.TIMESTAMP_FIELD_NAME);
        if (timestampStructure != null) {
            PVLong secondsPv = timestampStructure.getLongField("secondsPastEpoch");
            if (secondsPv != null)
                return new BigDecimal(secondsPv.get());
        }
        return BigDecimal.ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int status() {
        PVStructure alarmStructure = structure.getStructureField(PvAccessChannel.ALARM_FIELD_NAME);
        if (alarmStructure != null) {
            PVInt statusPv = alarmStructure.getIntField("status");
            if (statusPv != null)
                return statusPv.get();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int severity() {
        PVStructure alarmStructure = structure.getStructureField(PvAccessChannel.ALARM_FIELD_NAME);
        if (alarmStructure != null) {
            PVInt statusPv = alarmStructure.getIntField("severity");
            if (statusPv != null)
                return statusPv.get();
        }
        return 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayValue getStore() {
        PVField valueField = structure.getSubField(defaultField);
        if (valueField != null) {
            return ArrayValue.arrayValueFromArray(getValueArray(valueField));
        }
        // If value is not found return an empty ArrayValue
        return ArrayValue.arrayValueFromArray(new Object[]{});
    }
    
    private Object getValueArray(PVField valueField) {
        Type type = valueField.getField().getType();
        switch (type) {
        case scalar:
            return getScalarValueArray((PVScalar) valueField);
        case scalarArray:
            return getScalarArrayValueArray((PVScalarArray) valueField);
        default:
            break; 
        }
        return null;
    }

    private Object getScalarValueArray(PVScalar valueField) {
        ScalarType type = valueField.getScalar().getScalarType();
        Convert convert = ConvertFactory.getConvert();
        switch (type) {
        case pvBoolean:
            return new boolean[]{((PVBoolean) valueField).get()};
        case pvByte:
        case pvUByte:
            return new byte[]{convert.toByte(valueField)};
        case pvDouble:
            return new double[]{convert.toDouble(valueField)};
        case pvFloat:
            return new float[]{convert.toFloat(valueField)};
        case pvInt:
        case pvUInt:
            return new int[]{convert.toInt(valueField)};
        case pvLong:
        case pvULong:
            return new long[]{convert.toLong(valueField)};
        case pvShort:
        case pvUShort:
            return new short[]{convert.toShort(valueField)};
        case pvString:
            return new String[]{convert.toString(valueField)};
        default:
            break;
        }
        return null;
    }

    private Object getScalarArrayValueArray(PVScalarArray valueField) {
        ScalarType type = valueField.getScalarArray().getElementType();
        Convert convert = ConvertFactory.getConvert();
        int length = getElementCount();
        Object arr = null;
        switch (type) {
        case pvByte:
        case pvUByte:
            arr = new byte[length];
            convert.toByteArray(valueField, 0, length, (byte[])arr, 0);
            break;
        case pvDouble:
            arr = new double[length];
            convert.toDoubleArray(valueField, 0, length, (double[])arr, 0);
            break;
        case pvFloat:
            arr = new float[length];
            convert.toFloatArray(valueField, 0, length, (float[])arr, 0);
            break;
        case pvInt:
        case pvUInt:
            arr = new int[length];
            convert.toIntArray(valueField, 0, length, (int[])arr, 0);
            break;
        case pvLong:
        case pvULong:
            arr = new long[length];
            convert.toLongArray(valueField, 0, length, (long[])arr, 0);
            break;
        case pvShort:
        case pvUShort:
            arr = new short[length];
            convert.toShortArray(valueField, 0, length, (short[])arr, 0);
            break;
        case pvString:
            arr = new String[length];
            convert.toStringArray(valueField, 0, length, (String[])arr, 0);
            break;
        case pvBoolean:
            BooleanArrayData dataArr = new BooleanArrayData();
            ((PVBooleanArray) valueField).get(0, length, dataArr);
            arr = Arrays.copyOf(dataArr.data, length);
            break;
        default:
            break;
        }
        return arr;
    }


    /**
     * @return Number of elements in value field
     */
    public int getElementCount() {
        PVField valueField = structure.getSubField(PvAccessChannel.VALUE_FIELD_NAME);
        if (valueField == null) {
            return 0;
        }

        Type type = valueField.getField().getType();
        switch (type) {
        case scalar:
            return 1;
        case scalarArray:
            return ((PVScalarArray) valueField).getLength();
        default:
            return 0;
        }
    }
    
    /**
     * @return Type of the data in the value field.
     */
    public Class<?> getValueType() {
        PVField valueField = structure.getSubField(PvAccessChannel.VALUE_FIELD_NAME);
        Type type = valueField.getField().getType();
        ScalarType scalarType;
        switch (type) {
        case scalar:
            scalarType = ((PVScalar) valueField).getScalar().getScalarType();
            break;
        case scalarArray:
            scalarType = ((PVScalarArray) valueField).getScalarArray().getElementType();
            break;
        default:
            return null;
        }
        return getJavaPrimitiveType(scalarType);
    }

    private Class<?> getJavaPrimitiveType(ScalarType type) {
        switch (type) {
        case pvByte:
        case pvUByte:
            return Byte.TYPE;
        case pvDouble:
            return Double.TYPE;
        case pvFloat:
            return Float.TYPE;
        case pvInt:
        case pvUInt:
            return Integer.TYPE;
        case pvShort:
        case pvUShort:
            return Short.TYPE;
        case pvBoolean:
            return Boolean.TYPE;
        case pvLong:
        case pvULong:
            return Long.TYPE;
        case pvString:
            return String.class;
        default:
            break;
        }
        return null;
    }

    /**
     * @return Units of the data in the value field
     */
    public String getUnits() {
        PVStructure displayStructure = structure.getStructureField(PvAccessChannel.DISPLAY_FIELD_NAME);
        if (displayStructure == null) {
            return "";
        }
        PVString unitField = displayStructure.getStringField("units");
        if (unitField == null) {
            return "";
        }
        return unitField.get();
    }

    /**
     * @return Upper display limit
     */
    public Number getUpperDisplayLimit() {
        PVStructure displayStructure = structure.getStructureField(PvAccessChannel.DISPLAY_FIELD_NAME);
        Convert convert = ConvertFactory.getConvert();
        if (displayStructure == null) {
            return Double.NaN;
        }
        PVField upperLimField = displayStructure.getSubField("limitHigh");
        if (upperLimField == null) {
            return Double.NaN;
        }
        try {
            return convert.toDouble((PVScalar) upperLimField);
        } catch (ClassCastException e) {
            return Double.NaN;
        }
    }

    /**
     * @return Lower display limit
     */
    public Number getLowerDisplayLimit() {
        PVStructure displayStructure = structure.getStructureField(PvAccessChannel.DISPLAY_FIELD_NAME);
        Convert convert = ConvertFactory.getConvert();
        if (displayStructure == null) {
            return Double.NaN;
        }
        PVField lowerLimField = displayStructure.getSubField("limitLow");
        if (lowerLimField == null) {
            return Double.NaN;
        }
        try {
            return convert.toDouble((PVScalar) lowerLimField);
        } catch (ClassCastException e) {
            return Double.NaN;
        }
    }

    /**
     * @return Upper control limit 
     */
    public Number getUpperControlLimit() {
        PVStructure controlStructure = structure.getStructureField(PvAccessChannel.CONTROL_FIELD_NAME);
        Convert convert = ConvertFactory.getConvert();
        if (controlStructure == null) {
            return Double.NaN;
        }
        PVField upperLimField = controlStructure.getSubField("limitHigh");
        if (upperLimField == null) {
            return Double.NaN;
        }
        try {
            return convert.toDouble((PVScalar) upperLimField);
        } catch (ClassCastException e) {
            return Double.NaN;
        }
    }

    /**
     * @return Lower control limit 
     */
    public Number getLowerControlLimit() {
        PVStructure controlStructure = structure.getStructureField(PvAccessChannel.CONTROL_FIELD_NAME);
        Convert convert = ConvertFactory.getConvert();
        if (controlStructure == null) {
            return Double.NaN;
        }
        PVField lowerLimField = controlStructure.getSubField("limitLow");
        if (lowerLimField == null) {
            return Double.NaN;
        }
        try {
            return convert.toDouble((PVScalar) lowerLimField);
        } catch (ClassCastException e) {
            return Double.NaN;
        }
    }
}
