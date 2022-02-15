/*
 * NumericParser.java
 *
 * Created on June 5, 2003, 2:57 PM
 */
package xal.tools.text;

/**
 * NumericParser parses a string value into an instance of a specified Number
 * subclass. It also maintains a static directory of the numeric parsers. It is
 * useful when one wants to create a generic component for entering values that
 * could be assigned dynamically a number and a numeric type (Integer, Double,
 * etc.).
 *
 * @author tap
 */
public abstract class NumericParser {

    private static NumericParser getParser(Class<? extends Number> numericType) {
        if (numericType == Double.class) {
            return new DoubleParser();
        } else if (numericType == Integer.class) {
            return new IntegerParser();
        } else if (numericType == Short.class) {
            return new ShortParser();
        } else if (numericType == Long.class) {
            return new LongParser();
        } else if (numericType == Float.class) {
            return new FloatParser();
        } else if (numericType == Byte.class) {
            return new ByteParser();
        } else {
            throw new IllegalArgumentException("Unsupported numeric type: " + numericType.getName());
        }
    }

    /**
     * Parse the string value as a number of the specified numeric type.
     *
     * @param stringValue String representation of a number
     * @param numericType The type of number to instantiate
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a
     * number
     * @throws java.lang.IllegalArgumentException if the numeric type specified
     * is unsupported
     */
    public static Number getNumericValue(final String stringValue, final Class<? extends Number> numericType) throws NumberFormatException, IllegalArgumentException {
        final NumericParser parser = getParser(numericType);
        return parser.getNumericValue(stringValue);
    }

    /**
     * Parse the string value as a number
     *
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a
     * number
     */
    public abstract Number getNumericValue(String stringValue) throws NumberFormatException;
}

/**
 * ByteParser is a concrete subclass of NumericParser that can parse a string
 * into a Number with internal storage as a byte.
 *
 * @author tap
 */
class ByteParser extends NumericParser {

    /**
     * Parse the string value as a number
     *
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a
     * number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Byte.parseByte(stringValue);
    }
}

/**
 * DoubleParser is a concrete subclass of NumericParser that can parse a string
 * into a Number with internal storage as a double.
 *
 * @author tap
 */
class DoubleParser extends NumericParser {

    /**
     * Parse the string value as a number
     *
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a
     * number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Double.parseDouble(stringValue);
    }
}

/**
 * FloatParser is a concrete subclass of NumericParser that can parse a string
 * into a Number with internal storage as a float.
 *
 * @author tap
 */
class FloatParser extends NumericParser {

    /**
     * Parse the string value as a number.
     *
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a
     * number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Float.parseFloat(stringValue);
    }
}

/**
 * IntegerParser is a concrete subclass of NumericParser that can parse a string
 * into a Number with internal storage as an int.
 *
 * @author tap
 */
class IntegerParser extends NumericParser {

    /**
     * Parse the string value as a number
     *
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a
     * number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Integer.parseInt(stringValue);
    }
}

/**
 * LongParser is a concrete subclass of NumericParser that can parse a string
 * into a Number with internal storage as a long.
 *
 * @author tap
 */
class LongParser extends NumericParser {

    /**
     * Parse the string value as a number
     *
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a
     * number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Long.parseLong(stringValue);
    }
}

/**
 * ShortParser is a concrete subclass of NumericParser that can parse a string
 * into a Number with internal storage as a short.
 *
 * @author tap
 */
class ShortParser extends NumericParser {

    /**
     * Parse the string value as a number
     *
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a
     * number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Short.parseShort(stringValue);
    }
}
