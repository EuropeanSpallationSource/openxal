package xal.plugin.pvaccess;

import org.epics.pvdata.pv.*;

import xal.ca.PutException;

/**
 * Utility class to ease the put of different data types to channels.
 * 
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
enum PvAccessPutUtils {
    STRING(String.class.getName()) {
        @Override
        void putImpl(PVField field, Object stringValue) {
            ((PVString) field).put((String) stringValue);
        }
    },
    FLOAT(float.class.getName()) {
        @Override
        void putImpl(PVField field, Object floatValue) {
            ((PVFloat) field).put((float) floatValue);
        }
    },
    FLOAT_ARRAY(float[].class.getName()) {
        @Override
        void putImpl(PVField field, Object floatValue) {
            float[] arr = (float[]) floatValue;
            ((PVFloatArray) field).put(0, arr.length, arr, 0);
        }
    },
    DOUBLE(double.class.getName()) {
        @Override
        void putImpl(PVField field, Object doubleValue) {
            ((PVDouble) field).put((double) doubleValue);
        }
    },
    DOUBLE_ARRAY(double[].class.getName()) {
        @Override
        void putImpl(PVField field, Object doubleValue) {
            double[] arr = (double[]) doubleValue;
            ((PVDoubleArray) field).put(0, arr.length, arr, 0);
        }
    },
    SHORT(short.class.getName()) {
        @Override
        void putImpl(PVField field, Object shortValue) {
            ((PVShort) field).put((short) shortValue);
        }
    },
    SHORT_ARRAY(short[].class.getName()) {
        @Override
        void putImpl(PVField field, Object shortValue) {
            short[] arr = (short[]) shortValue;
            ((PVShortArray) field).put(0, arr.length, arr, 0);
        }
    },
    INT(int.class.getName()) {
        @Override
        void putImpl(PVField field, Object intValue) {
            ((PVInt) field).put((int) intValue);
        }
    },
    INT_ARRAY(int.class.getName()){
        @Override
        void putImpl(PVField field, Object intValue) {
            int[] arr = (int[]) intValue;
            ((PVIntArray) field).put(0, arr.length, arr, 0);
        }
    },
    BYTE(byte.class.getName()) {
        @Override
        void putImpl(PVField field, Object byteValue) {
            ((PVByte) field).put((byte) byteValue);
        }
    },
    BYTE_ARRAY(byte.class.getName()){
        @Override
        void putImpl(PVField field, Object byteValue) {
            byte[] arr = (byte[]) byteValue;
            ((PVByteArray) field).put(0, arr.length, arr, 0);
        }
    };
    
    private final String pvType;
    
    private PvAccessPutUtils(String pvType) {
        this.pvType = pvType;
    }
    
    /**
     * Provides the correct class implementation based on type.
     * @param type Name of the type.
     * @return class implementation based on the type
     */
    private static PvAccessPutUtils getPvPutObject(String type) {
        for (PvAccessPutUtils t : values()) {
            if (t.pvType.equals(type)) {
                return t;
            }
        }
        return null;
    }
    
    /**
     * Helper method for put a value to the PVField object.
     * @param field PVField object to which the put is done
     * @param o Object to write to the field
     * @param type Name of the type of the object
     * @throws PutException 
     */
    static void put(PVField field, Object o, String type) throws PutException {
        PvAccessPutUtils t = getPvPutObject(type);
        if (t != null) {
            t.putImpl(field, o);
        } else {
            throw new PutException("Put of type " + type + " is not supported!");
        }
    }

    /**
     * Put implementation for a type.
     * @param field PVField object to which the put is done
     * @param o Object to write to the field
     */
    abstract void putImpl(PVField field, Object o);
}
