//
//  FormattedNumber.java
//  xal
//
//  Created by Thomas Pelaia on 2/22/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.text;

import java.text.*;
import java.util.Comparator;

/**
 * Provides a number with a format which facilitates easy table display with
 * proper number justification.
 *
 * @author Thomas Pelaia
 * @since 2/22/06
 */
public class FormattedNumber extends Number implements Comparable<FormattedNumber> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    /**
     * default number format when none is specified
     */
    protected static final NumberFormat DEFAULT_NUMBER_FORMAT = new DecimalFormat("0.0000E0");

    /**
     * comparator which compares FormattedNumber instances according to their
     * double value
     */
    private static final Comparator<FormattedNumber> DOUBLE_VALUE_COMPARATOR = (left, right) -> Double.compare(left.doubleValue(), right.doubleValue());

    /**
     * the number
     */
    protected final Number number;

    /**
     * the format for displaying the number
     */
    protected final NumberFormat format;

    /**
     * Primary Constructor
     *
     * @param format the number format for display
     * @param value the value to represent
     */
    public FormattedNumber(final NumberFormat format, final Number value) {
        number = value;
        this.format = format;
    }

    /**
     * Constructor
     *
     * @param pattern decimal format pattern
     * @param value the value to represent
     */
    public FormattedNumber(final String pattern, final Number value) {
        this(new DecimalFormat(pattern), value);
    }

    /**
     * Constructor with the default pattern of "0.0000E###"
     *
     * @param value the value to represent
     */
    public FormattedNumber(final Number value) {
        this(DEFAULT_NUMBER_FORMAT, value);
    }

    /**
     * Constructor with the default pattern of "0.0000E###". This constructor is
     * necessary to support javax.swing.text.DefaultFormatter
     *
     * @param valueString the string to parse as a number
     */
    public FormattedNumber(final String valueString) {
        this(Double.valueOf(valueString));
    }

    /**
     * Get the format.
     *
     * @return the format
     */
    public NumberFormat getFormat() {
        return format;
    }

    /**
     * Get the value as a byte.
     *
     * @return the value as a byte
     */
    @Override
    public byte byteValue() {
        return number.byteValue();
    }

    /**
     * Get the value as a double.
     *
     * @return the value as a double
     */
    @Override
    public double doubleValue() {
        return number.doubleValue();
    }

    /**
     * Get the value as a float.
     *
     * @return the value as a float
     */
    @Override
    public float floatValue() {
        return number.floatValue();
    }

    /**
     * Get the value as an integer.
     *
     * @return the value as an integer
     */
    @Override
    public int intValue() {
        return number.intValue();
    }

    /**
     * Get the value as a short.
     *
     * @return the value as a short
     */
    @Override
    public short shortValue() {
        return number.shortValue();
    }

    /**
     * Get the value as a long.
     *
     * @return the value as a long
     */
    @Override
    public long longValue() {
        return number.longValue();
    }

    /**
     * Use the number format to provide the number's display representation.
     *
     * @return the display representation
     */
    @Override
    public String toString() {
        return format.format(number);
    }

    /**
     * compare this number with the specified other number
     */
    @Override
    public int compareTo(final FormattedNumber other) {
        return DOUBLE_VALUE_COMPARATOR.compare(this, other);
    }

    /**
     * Get a comparator which compares FormattedNumber instances by their double
     * value
     */
    static Comparator<FormattedNumber> doubleValueComparator() {
        return DOUBLE_VALUE_COMPARATOR;
    }
}
