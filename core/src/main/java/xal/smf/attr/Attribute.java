package xal.smf.attr;

/*
 * Attribute.java
 *
 * Created on September 10, 2001, 4:42 PM
 */
/**
 *
 * @author CKAllen
 * @version
 */
import java.util.StringTokenizer;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.tools.StringJoiner;

public final class Attribute extends Object implements Serializable {

    /**
     * ID for serializable version
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(Attribute.class.getName());

    /*
     *  Constants
     */
    private static String[] arrTypeNames = {"Unknown", // 0
        "Boolean", // 1
        "Character", // 2
        "Byte", // 3
        "Short", // 4
        "Integer", // 5
        "Long", // 6
        "Float", // 7
        "Double", // 8
        "String", // 9
        "", // 10
        "Array-Boolean", // 11
        "Array-Character", // 12
        "Array-Byte", // 13
        "Array-Short", // 14
        "Array-Integer", // 15
        "Array-Long", // 16
        "Array-Float", // 17
        "Array-Double", // 18
        "Array-String" // 19
};

    public static final int UNKNOWN = 0;
    public static final int BOOLEAN = 1;
    public static final int CHARACTER = 2;
    public static final int BYTE = 3;
    public static final int SHORT = 4;
    public static final int INTEGER = 5;
    public static final int LONG = 6;
    public static final int FLOAT = 7;
    public static final int DOUBLE = 8;
    public static final int STRING = 9;
    public static final int ARR_BOL = 11;
    public static final int ARR_CHR = 12;
    public static final int ARR_BYTE = 13;
    public static final int ARR_SHR = 14;
    public static final int ARR_INT = 15;
    public static final int ARR_LNG = 16;
    public static final int ARR_FLT = 17;
    public static final int ARR_DBL = 18;
    public static final int ARR_STR = 19;

    /*
     *  User Interface
     */
    /**
     * Create new Attribute Note that Attribute must be initially instantiated
     * to a particular type.
     */
    public Attribute(boolean val) {
        set(val);
    }

    public Attribute(int val) {
        set(val);
    }

    public Attribute(long val) {
        set(val);
    }

    public Attribute(float val) {
        set(val);
    }

    public Attribute(double val) {
        set(val);
    }

    public Attribute(String val) {
        set(val);
    }

    public Attribute(int[] arr) {
        set(arr);
    }

    public Attribute(long[] arr) {
        set(arr);
    }

    public Attribute(float[] arr) {
        set(arr);
    }

    public Attribute(double[] arr) {
        set(arr);
    }

    public Attribute(String[] arr) {
        set(arr);
    }

    //  Data Query Methods
    public int getType() {
        return intTypeId;
    }

    public String getTypeString() {
        return arrTypeNames[getType()];
    }

    public boolean isArray() {
        return (intTypeId > 10);
    }

    public Object getObject() {
        return objValue;
    }

    // Get Methods
    public boolean getBoolean() {
        return ((Boolean) objValue);
    }

    public int getInteger() {
        return ((Integer) objValue);
    }

    public long getLong() {
        return ((Long) objValue);
    }

    public float getFloat() {
        return ((Float) objValue);
    }

    public double getDouble() {
        return ((Double) objValue);
    }

    public String getString() {
        return (String) objValue;
    }

    public int[] getArrInt() {
        return (int[]) objValue;
    }

    public long[] getArrLng() {
        return (long[]) objValue;
    }

    public float[] getArrFlt() {
        return (float[]) objValue;
    }

    public double[] getArrDbl() {
        return (double[]) objValue;
    }

    public String[] getArrStr() {
        return (String[]) objValue;
    }

    //  Set Methods
    public void set(boolean newVal) {
        objValue = newVal;
        intTypeId = BOOLEAN;
    }

    public void set(int newVal) {
        objValue = newVal;
        intTypeId = INTEGER;
    }

    public void set(long newVal) {
        objValue = newVal;
        intTypeId = LONG;
    }

    public void set(float newVal) {
        objValue = newVal;
        intTypeId = FLOAT;
    }

    public void set(double newVal) {
        objValue = newVal;
        intTypeId = DOUBLE;
    }

    public void set(String newVal) {
        objValue = newVal;
        intTypeId = STRING;
    }

    public void set(int[] newArr) {
        objValue = newArr;
        intTypeId = ARR_INT;
    }

    public void set(long[] newArr) {
        objValue = newArr;
        intTypeId = ARR_LNG;
    }

    public void set(float[] newArr) {
        objValue = newArr;
        intTypeId = ARR_FLT;
    }

    public void set(double[] newArr) {
        objValue = newArr;
        intTypeId = ARR_DBL;
    }

    public void set(String[] newArr) {
        objValue = newArr;
        intTypeId = ARR_STR;
    }

    /**
     * Set Attribute value from string parsing
     */
    public boolean parse(String strVal) throws NumberFormatException {

        // Parse string according to type
        switch (intTypeId) {

            case DOUBLE:
                objValue = new Double(strVal);
                break;
            case FLOAT:
                objValue = new Float(strVal);
                break;
            case LONG:
                objValue = Long.valueOf(strVal);
                break;
            case INTEGER:
                objValue = Integer.valueOf(strVal);
                break;
            case STRING:
                objValue = strVal;
                break;

            case BOOLEAN:
                objValue = Boolean.valueOf(strVal);
                break;

            case ARR_DBL:
                objValue = this.parseArrDbl(strVal);
                break;
            case ARR_FLT:
                objValue = this.parseArrFlt(strVal);
                break;
            case ARR_LNG:
                objValue = this.parseArrLng(strVal);
                break;
            case ARR_INT:
                objValue = this.parseArrInt(strVal);
                break;
            case ARR_STR:
                objValue = this.parseArrStr(strVal);
                break;

            default:
                return false;
        }

        return true;
    }

    /*
     *  Local Attributes
     */
    private int intTypeId = UNKNOWN;
    private Object objValue = null;

    /*
     *  Local Support Functions
     */
    // Parsing Arrays
    private double[] parseArrDbl(String strArr) throws NumberFormatException {
        // index of current element
        int iElem;
        // returned value
        double[] arr;
        // break string into array
        StringTokenizer tok;

        tok = new StringTokenizer(strArr, ",");
        arr = new double[tok.countTokens()];

        iElem = 0;
        while (tok.hasMoreTokens()) {
            arr[iElem] = Double.parseDouble(tok.nextToken());
            iElem++;
        }

        return arr;
    }

    private float[] parseArrFlt(String strArr) throws NumberFormatException {
        // index of current element
        int iElem;
        // returned value
        float[] arr;
        // break string into array
        StringTokenizer tok;

        tok = new StringTokenizer(strArr, ",");
        arr = new float[tok.countTokens()];

        iElem = 0;
        while (tok.hasMoreTokens()) {
            arr[iElem] = Float.parseFloat(tok.nextToken());
            iElem++;
        }

        return arr;
    }

    private long[] parseArrLng(String strArr) throws NumberFormatException {
        // index of current element
        int iElem;
        // returned value
        long[] arr;
        // break string into array
        StringTokenizer tok;

        tok = new StringTokenizer(strArr, ",");
        arr = new long[tok.countTokens()];

        iElem = 0;
        while (tok.hasMoreTokens()) {
            arr[iElem] = Long.parseLong(tok.nextToken());
            iElem++;
        }

        return arr;
    }

    private int[] parseArrInt(String strArr) throws NumberFormatException {
        // index of current element
        int iElem;
        // returned value
        int[] arr;
        // break string into array
        StringTokenizer tok;

        tok = new StringTokenizer(strArr, ",");
        arr = new int[tok.countTokens()];

        iElem = 0;
        while (tok.hasMoreTokens()) {
            arr[iElem] = Integer.parseInt(tok.nextToken());
            iElem++;
        }

        return arr;
    }

    private String[] parseArrStr(String strArr) throws NumberFormatException {
        // index of current element
        int iElem;
        // returned value
        String[] arr;
        // break string into array
        StringTokenizer tok;

        tok = new StringTokenizer(strArr, ",");
        arr = new String[tok.countTokens()];

        iElem = 0;
        while (tok.hasMoreTokens()) {
            arr[iElem] = tok.nextToken();
            iElem++;
        }

        return arr;
    }

    // method added for writing data as text - tap 3/1/2002
    public String stringValue() {
        String stringValue = "";
        StringJoiner joiner = new StringJoiner(",");

        try {
            switch (intTypeId) {
                case ARR_DBL:
                    joiner.append((double[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case ARR_FLT:
                    joiner.append((float[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case ARR_LNG:
                    joiner.append((long[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case ARR_INT:
                    joiner.append((int[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case ARR_STR:
                    joiner.append((String[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case ARR_BOL:
                    joiner.append((boolean[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case ARR_CHR:
                    joiner.append((char[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case ARR_BYTE:
                    joiner.append((byte[]) objValue);
                    stringValue = joiner.toString();
                    break;
                default:
                    stringValue = objValue.toString();
                    break;
            }
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, null, exception);
        }

        return stringValue;
    }
}
