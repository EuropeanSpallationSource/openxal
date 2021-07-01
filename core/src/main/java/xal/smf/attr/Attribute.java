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
    public static String[] arrTypeNames = {"Unknown", // 0
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

    public static final int iUnknown = 0;
    public static final int iBoolean = 1;
    public static final int iCharacter = 2;
    public static final int iByte = 3;
    public static final int iShort = 4;
    public static final int iInteger = 5;
    public static final int iLong = 6;
    public static final int iFloat = 7;
    public static final int iDouble = 8;
    public static final int iString = 9;
    public static final int iArrBol = 11;
    public static final int iArrChr = 12;
    public static final int iArrByte = 13;
    public static final int iArrShr = 14;
    public static final int iArrInt = 15;
    public static final int iArrLng = 16;
    public static final int iArrFlt = 17;
    public static final int iArrDbl = 18;
    public static final int iArrStr = 19;

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
        intTypeId = iBoolean;
    }

    public void set(int newVal) {
        objValue = newVal;
        intTypeId = iInteger;
    }

    public void set(long newVal) {
        objValue = newVal;
        intTypeId = iLong;
    }

    public void set(float newVal) {
        objValue = newVal;
        intTypeId = iFloat;
    }

    public void set(double newVal) {
        objValue = newVal;
        intTypeId = iDouble;
    }

    public void set(String newVal) {
        objValue = newVal;
        intTypeId = iString;
    }

    public void set(int[] newArr) {
        objValue = newArr;
        intTypeId = iArrInt;
    }

    public void set(long[] newArr) {
        objValue = newArr;
        intTypeId = iArrLng;
    }

    public void set(float[] newArr) {
        objValue = newArr;
        intTypeId = iArrFlt;
    }

    public void set(double[] newArr) {
        objValue = newArr;
        intTypeId = iArrDbl;
    }

    public void set(String[] newArr) {
        objValue = newArr;
        intTypeId = iArrStr;
    }

    /**
     * Set Attribute value from string parsing
     */
    public boolean parse(String strVal) throws NumberFormatException {

        // Parse string according to type
        switch (intTypeId) {

            case iDouble:
                objValue = new Double(strVal);
                break;
            case iFloat:
                objValue = new Float(strVal);
                break;
            case iLong:
                objValue = Long.valueOf(strVal);
                break;
            case iInteger:
                objValue = Integer.valueOf(strVal);
                break;
            case iString:
                objValue = strVal;
                break;

            case iBoolean:
                objValue = Boolean.valueOf(strVal);
                break;

            case iArrDbl:
                objValue = this.parseArrDbl(strVal);
                break;
            case iArrFlt:
                objValue = this.parseArrFlt(strVal);
                break;
            case iArrLng:
                objValue = this.parseArrLng(strVal);
                break;
            case iArrInt:
                objValue = this.parseArrInt(strVal);
                break;
            case iArrStr:
                objValue = this.parseArrStr(strVal);
                break;

            default:
                return false;
        }

        return true;
    }
    ;
        


    
    /*
     *  Local Attributes
     */
                            
    private int intTypeId = iUnknown;
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

    /**
     * NOTE: Wrote these then found out they are not needed. Keep them around
     * for now
     */
    // Parsing Type Identification
    private boolean isDouble(String s) {
        if (!hasNumeric(s)) {
            // must contain numeric characters
            return false;
        }
        if (s.indexOf('F') > 0
                || // must not have 'f' or 'F' suffix
                s.indexOf('f') > 0) {
            return false;
        }

        if (s.indexOf('.') < 0
                && // must contain decimal or exponent
                s.indexOf('e') < 0
                && s.indexOf('E') < 0) {
            return false;
        }
        // must be parsable
        try {
            Double.valueOf(s);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private boolean isFloat(String s) {
        if (!hasNumeric(s)) {
            // must contain numeric characters
            return false;
        }
        if (s.indexOf('F') < 0
                && // must contain 'f' or 'F' suffix
                s.indexOf('f') < 0) {
            return false;
        }

        if (s.indexOf('.') < 0
                && // must contain decimal or exponent
                s.indexOf('e') < 0
                && s.indexOf('E') < 0) {
            return false;
        }
        // must be parsable
        try {
            Float.valueOf(s);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private boolean isLong(String s) {
        if (!hasNumeric(s)) {
            // must contain numeric characters
            return false;
        }
        if (s.indexOf('L') < 0
                && // must contain 'L' or 'l' suffix
                s.indexOf('l') < 0) {
            return false;
        }

        if (s.indexOf('.') >= 0
                || // must not contain decimal or exponent
                s.indexOf('e') >= 0
                || s.indexOf('E') >= 0) {
            return false;
        }
        // must be parsable
        try {
            Long.valueOf(s);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private boolean isInteger(String s) {
        if (!hasNumeric(s)) {
            // must contain numeric characters
            return false;
        }
        if (s.indexOf('L') > 0
                || // must not contain 'L' or 'l' suffix
                s.indexOf('l') > 0) {
            return false;
        }

        if (s.indexOf('.') >= 0
                || // must not contain decimal or exponent
                s.indexOf('e') >= 0
                || s.indexOf('E') >= 0) {
            return false;
        }
        // must be parsable
        try {
            Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    //  Parsing Auxiliary Functions
    private boolean hasLetter(String s) {
        int i, l = s.length();
        for (i = 0; i < l; ++i) {
            if (Character.getType(s.charAt(i)) == Character.LOWERCASE_LETTER
                    || Character.getType(s.charAt(i)) == Character.UPPERCASE_LETTER) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNumeric(String s) {
        int i, l = s.length();
        for (i = 0; i < l; ++i) {
            if (Character.getType(s.charAt(i)) == Character.DECIMAL_DIGIT_NUMBER) {
                return true;
            }
        }
        return false;
    }

    // method added for writing data as text - tap 3/1/2002
    public String stringValue() {
        String stringValue = "";
        StringJoiner joiner = new StringJoiner(",");

        try {
            switch (intTypeId) {
                case iArrDbl:
                    joiner.append((double[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case iArrFlt:
                    joiner.append((float[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case iArrLng:
                    joiner.append((long[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case iArrInt:
                    joiner.append((int[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case iArrStr:
                    joiner.append((String[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case iArrBol:
                    joiner.append((boolean[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case iArrChr:
                    joiner.append((char[]) objValue);
                    stringValue = joiner.toString();
                    break;
                case iArrByte:
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
