/*
 * NumericParser.java
 *
 * Created on June 5, 2003, 2:57 PM
 */

package xal.tools.text;

import java.util.HashMap;
import java.util.Map;

/**
 * NumericParser parses a string value into an instance of a specified 
 * Number subclass.  It also maintains a static directory of the numeric parsers.
 * It is useful when one wants to create a generic component for entering values
 * that could be assigned dynamically a number and a numeric type (Integer, Double, etc.).
 *
 * @author  tap
 */
public abstract class NumericParser {
	/** map of parsers keyed by numeric class */
    protected static final Map<Class<? extends Number>, NumericParser> PARSER_CLASS_MAP;


	// static initializer
    static {
        // create a table of parsers
        PARSER_CLASS_MAP = new HashMap<>();
        PARSER_CLASS_MAP.put( Double.class, new DoubleParser() );
        PARSER_CLASS_MAP.put( Integer.class, new IntegerParser() );
        PARSER_CLASS_MAP.put( Short.class, new ShortParser() );
        PARSER_CLASS_MAP.put( Long.class, new LongParser() );
        PARSER_CLASS_MAP.put( Float.class, new FloatParser() );
        PARSER_CLASS_MAP.put( Byte.class, new ByteParser() );
    }
    
    
    /**
     * Parse the string value as a number of the specified numeric type.
     * @param stringValue String representation of a number
     * @param numericType The type of number to instantiate
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a number
     * @throws java.lang.IllegalArgumentException if the numeric type specified is unsupported
     */
    public static Number getNumericValue( final String stringValue, final Class<? extends Number> numericType ) throws NumberFormatException, IllegalArgumentException {
        final NumericParser parser = PARSER_CLASS_MAP.get( numericType );
        try {
            return parser.getNumericValue(stringValue);
        }
        catch(NullPointerException exception) {
            throw new IllegalArgumentException("Unsupported numeric type: " + numericType.getName());
        }
    }
    
    
    /**
     * Parse the string value as a number
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a number
     */
    public abstract Number getNumericValue(String stringValue) throws NumberFormatException;
}



/**
 * ByteParser is a concrete subclass of NumericParser that can parse a string into a Number with internal storage as a byte.
 * @author  tap
 */
class ByteParser extends NumericParser {
    /**
	 * Parse the string value as a number
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Byte.parseByte(stringValue);
    }
}



/**
 * DoubleParser is a concrete subclass of NumericParser that can parse a string into a Number with internal storage as a double.
 * @author  tap
 */
class DoubleParser extends NumericParser {
    /**
     * Parse the string value as a number
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Double.parseDouble(stringValue);
    }
}



/**
 * FloatParser is a concrete subclass of NumericParser that can parse a string into a Number with internal storage as a float.
 * @author  tap
 */
class FloatParser extends NumericParser {
    /**
	 * Parse the string value as a number.
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Float.parseFloat(stringValue);
    }
}



/**
 * IntegerParser is a concrete subclass of NumericParser that can parse a string into a Number with internal storage as an int.
 * @author  tap
 */
class IntegerParser extends NumericParser {
    /**
     * Parse the string value as a number
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Integer.parseInt(stringValue);
    }
}


/**
 * LongParser is a concrete subclass of NumericParser that can parse a string into a Number with internal storage as a long.
 * @author  tap
 */
class LongParser extends NumericParser {
    /**
     * Parse the string value as a number
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Long.parseLong(stringValue);
    }
}



/**
 * ShortParser is a concrete subclass of NumericParser that can parse a string into a Number with internal storage as a short.
 * @author  tap
 */
class ShortParser extends NumericParser {
    /**
     * Parse the string value as a number
     * @param stringValue String representation of a number
     * @return numeric value of the string value
     * @throws NumberFormatException if the string cannot be parsed into a number
     */
    @Override
    public Number getNumericValue(String stringValue) throws NumberFormatException {
        return Short.parseShort(stringValue);
    }
}
