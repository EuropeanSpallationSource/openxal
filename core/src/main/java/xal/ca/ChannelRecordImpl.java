/*
 * ChannelRecord.java
 *
 * Created on June 28, 2002, 2:08 PM
 */
package xal.ca;

import xal.tools.ArrayValue;
import xal.tools.transforms.ValueTransform;

/**
 * ChannelRecord is a wrapper for the value returned by a get operation on a
 * Channel. It a provides convenience methods for getting data back as one of
 * many primitive types.
 *
 * @author tap
 */
public class ChannelRecordImpl implements ChannelRecord {

    /**
     * internal data storage
     */
    protected ArrayValue store;

    /**
     * Constructor
     *
     * @param adaptor from which to generate a record
     */
    public ChannelRecordImpl(final ValueAdaptor adaptor) {
        store = adaptor.getStore();
    }

    /**
     * Get the number of elements in the array value.
     *
     * @return The length of the array.
     */
    @Override
    public int getCount() {
        return store.getCount();
    }

    /**
     * Get the native type of the data as a Java class.
     *
     * @return The native type of the data.
     */
    @Override
    public Class<?> getType() {
        return store.getType();
    }

    /**
     * Get the data converted to a scalar byte. If the data is an array the the
     * value of the first element is converted to a byte and returned.
     *
     * @return The data as a scalar byte.
     */
    @Override
    public byte byteValue() {
        return store.byteValue();
    }

    /**
     * Get the value of the array element identified by the index and convert it
     * to a byte.
     *
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar byte.
     */
    @Override
    public byte byteValueAt(final int index) {
        return store.byteValueAt(index);
    }

    /**
     * Get the data converted to a byte array.
     *
     * @return The data as a byte array.
     */
    @Override
    public byte[] byteArray() {
        return store.byteArray();
    }

    /**
     * Get the data converted to a scalar short. If the data is an array the the
     * value of the first element is converted to a short and returned.
     *
     * @return The data as a scalar short.
     */
    @Override
    public short shortValue() {
        return store.shortValue();
    }

    /**
     * Get the value of the array element identified by the index and convert it
     * to a short.
     *
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar short.
     */
    @Override
    public short shortValueAt(final int index) {
        return store.shortValueAt(index);
    }

    /**
     * Get the data converted to a short array.
     *
     * @return The data as a short array.
     */
    @Override
    public short[] shortArray() {
        return store.shortArray();
    }

    /**
     * Get the data converted to a scalar int. If the data is an array the the
     * value of the first element is converted to a int and returned.
     *
     * @return The data as a scalar int.
     */
    @Override
    public int intValue() {
        return store.intValue();
    }

    /**
     * Get the value of the array element identified by the index and convert it
     * to a int.
     *
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar int.
     */
    @Override
    public int intValueAt(final int index) {
        return store.intValueAt(index);
    }

    /**
     * Get the data converted to a int array.
     *
     * @return The data as a int array.
     */
    @Override
    public int[] intArray() {
        return store.intArray();
    }

    /**
     * Get the data converted to a scalar long. If the data is an array the the
     * value of the first element is converted to a long and returned.
     *
     * @return The data as a scalar long.
     */
    @Override
    public long longValue() {
        return store.longValue();
    }

    /**
     * Get the value of the array element identified by the index and convert it
     * to a long.
     *
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar long.
     */
    @Override
    public long longValueAt(final int index) {
        return store.longValueAt(index);
    }

    /**
     * Get the data converted to a long array.
     *
     * @return The data as a long array.
     */
    @Override
    public long[] longArray() {
        return store.longArray();
    }

    /**
     * Get the data converted to a scalar float. If the data is an array the the
     * value of the first element is converted to a float and returned.
     *
     * @return The data as a scalar float.
     */
    @Override
    public float floatValue() {
        return store.floatValue();
    }

    /**
     * Get the value of the array element identified by the index and convert it
     * to a float.
     *
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar float.
     */
    @Override
    public float floatValueAt(final int index) {
        return store.floatValueAt(index);
    }

    /**
     * Get the data converted to a float array.
     *
     * @return The data as a float array.
     */
    @Override
    public float[] floatArray() {
        return store.floatArray();
    }

    /**
     * Get the data converted to a scalar double. If the data is an array the
     * the value of the first element is converted to a double and returned.
     *
     * @return The data as a scalar double.
     */
    @Override
    public double doubleValue() {
        return store.doubleValue();
    }

    /**
     * Get the value of the array element identified by the index and convert it
     * to a double.
     *
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar double.
     */
    @Override
    public double doubleValueAt(final int index) {
        return store.doubleValueAt(index);
    }

    /**
     * Get the data converted to a double array.
     *
     * @return The data as a double array.
     */
    @Override
    public double[] doubleArray() {
        return store.doubleArray();
    }

    /**
     * Get the data converted to a scalar string. If the data is an array the
     * the value of the first element is converted to a string and returned.
     *
     * @return The data as a scalar string.
     */
    @Override
    public String stringValue() {
        return store.stringValue();
    }

    /**
     * Get the value of the array element identified by the index and convert it
     * to a string.
     *
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar string.
     */
    @Override
    public String stringValueAt(final int index) {
        return store.stringValueAt(index);
    }

    /**
     * Get the data converted to a string array.
     *
     * @return The data as a string array.
     */
    @Override
    public String[] stringArray() {
        return store.stringArray();
    }

    /**
     * Override toString to return a representation of the data as an array.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        return "value: " + store.toString();
    }

    /**
     * Convert the store from a raw value to a processed value.
     *
     * @param transform The transform used to convert the store.
     * @return this instance as a convenience.
     */
    @Override
    public ChannelRecord applyTransform(ValueTransform transform) {
        store = transform.convertFromRaw(store);
        return this;
    }

    /**
     * Get the internal storage.
     *
     * @return The internal data storage.
     */
    @Override
    public ArrayValue arrayValue() {
        return store;
    }
}
