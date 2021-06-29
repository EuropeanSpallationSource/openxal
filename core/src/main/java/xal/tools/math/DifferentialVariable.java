/*
* DifferentialVariable.java
*
* Created by t6p on 11/8/2010
*
* Copyright (c) 2010 Spallation Neutron Source
* Oak Ridge National Laboratory
* Oak Ridge, TN 37830
 */
package xal.tools.math;

/**
 * Represents an immutable value and its differentials relative to independent
 * variables and performs math operations on them in support of error
 * propagation.
 *
 * @author t6p
 */
public class DifferentialVariable {

    /**
     * representation of the constant zero
     */
    public static final DifferentialVariable ZERO = newConstant(0.0);

    /**
     * representation of the constant one
     */
    public static final DifferentialVariable ONE = newConstant(1.0);

    /**
     * offset of first independent variable for this differential relative to
     * the array of all independent variables
     */
    private final int offset;

    /**
     * differentials
     */
    private final double[] derivatives;

    /**
     * value of the variable
     */
    private final double value;

    /**
     * Primary Constructor
     *
     * @param value the value of the variable
     * @param offset offset of this differential's first independent variable
     * relative to all independent variables
     * @param derivatives array of derivatives beginning with this
     * differential's first independent variable
     */
    public DifferentialVariable(final double value, final int offset, final double... derivatives) {
        this.offset = offset;
        this.value = value;
        this.derivatives = derivatives;
    }

    /**
     * Constructor with zero offset
     *
     * @param value the value of the variable
     * @param derivatives array of derivatives beginning with the first
     * independent variable
     */
    public static DifferentialVariable getInstance(final double value, final double... derivatives) {
        return new DifferentialVariable(value, 0, derivatives);
    }

    /**
     * get a constant value
     */
    public static DifferentialVariable newConstant(final double value) {
        return new DifferentialVariable(value, 0, 0.0);
    }

    /**
     * get the value
     */
    public double getValue() {
        return value;
    }

    /**
     * get the derivative for the independent variable at the specified index
     */
    public double getDerivative(final int index) {
        final int localIndex = index - offset;
        return localIndex < 0 ? 0.0 : localIndex < derivatives.length ? derivatives[localIndex] : 0.0;
    }

    /**
     * calculate the variance for this variable given a common variance for each
     * independent variable
     */
    public double varianceWithSignalVariance(final double commonVariance) {
        double sumSquareDerivatives = 0.0;
        for (final double derivative : derivatives) {
            sumSquareDerivatives += derivative * derivative;
        }

        return commonVariance * sumSquareDerivatives;
    }

    /**
     * calculate the variance for this variable given variances for all
     * independent variables
     */
    public double varianceWithSignalVariances(final double... variances) {
        return varianceWithSignalVariances(0, variances);
    }

    /**
     * calculate the variance for this variable given the independent variables
     * starting at the specified offset among all independent variables
     */
    public double varianceWithSignalVariances(final int offset, final double... variances) {
        double variance = 0.0;
        int vindex = offset - offset;
        for (int index = offset; index < derivatives.length; index++) {
            final double derivative = getDerivative(index);
            variance += derivative * derivative * variances[vindex];
            ++vindex;
        }

        return variance;
    }

    /**
     * perform the addition operation between a variable and a scalar value
     */
    public static DifferentialVariable add(final DifferentialVariable variable, final double value) {
        return new DifferentialVariable(variable.value + value, variable.offset, variable.derivatives);
    }

    /**
     * perform the addition operation between a variable and a scalar value
     */
    public static DifferentialVariable add(final double value, final DifferentialVariable variable) {
        return DifferentialVariable.add(variable, value);
    }

    /**
     * perform the addition operation between two variables
     */
    public static DifferentialVariable add(final DifferentialVariable... addends) {
        double value = 0.0;
        int offset = Integer.MAX_VALUE;
        int maxIndex = 0;
        for (final DifferentialVariable addend : addends) {
            value += addend.value;
            if (addend.offset < offset) {
                offset = addend.offset;
            }
            final int topAddendIndex = addend.offset + addend.derivatives.length - 1;   // index of the addend's last independent variable
            if (topAddendIndex > maxIndex) {
                maxIndex = topAddendIndex;
            }
        }

        final double[] derivatives = new double[maxIndex - offset + 1];

        for (int index = offset; index <= maxIndex; index++) {
            double derivative = 0.0;
            for (final DifferentialVariable addend : addends) {
                derivative += addend.getDerivative(index);
            }
            derivatives[index - offset] = derivative;
        }

        return new DifferentialVariable(value, offset, derivatives);
    }

    /**
     * subtract the scalar value from the variable
     */
    public static DifferentialVariable subtract(final DifferentialVariable variable, final double subtrahend) {
        return DifferentialVariable.add(variable, -subtrahend);
    }

    /**
     * subtract the variable from the scalar value
     */
    public static DifferentialVariable subtract(final double value, final DifferentialVariable variable) {
        return DifferentialVariable.add(variable.negate(), value);
    }

    /**
     * subtract the subtrahend variable from the minuend variable
     */
    public static DifferentialVariable subtract(final DifferentialVariable minuend, final DifferentialVariable subtrahend) {
        return DifferentialVariable.add(minuend, subtrahend.negate());
    }

    /**
     * perform the multiplication operation between a variable and a scalar
     * value
     */
    public static DifferentialVariable multiply(final DifferentialVariable variable, final double value) {
        final double[] derivatives = new double[variable.derivatives.length];
        for (int index = 0; index < derivatives.length; index++) {
            derivatives[index] = value * variable.derivatives[index];
        }
        return new DifferentialVariable(variable.value * value, variable.offset, derivatives);
    }

    /**
     * perform the multiplication operation between a variable and a scalar
     * value
     */
    public static DifferentialVariable multiply(final double value, final DifferentialVariable variable) {
        return DifferentialVariable.multiply(variable, value);
    }

    /**
     * multiply the variables
     */
    public static DifferentialVariable multiply(final DifferentialVariable... multiplicands) {
        double value = 1.0;
        int offset = Integer.MAX_VALUE;
        int maxIndex = 0;
        final double[] sensitivities = new double[multiplicands.length];
        for (int termIndex = 0; termIndex < multiplicands.length; termIndex++) {
            final DifferentialVariable multiplicand = multiplicands[termIndex];
            value *= multiplicand.value;
            if (multiplicand.offset < offset) {
                offset = multiplicand.offset;
            }
            final int topMultiplicandIndex = multiplicand.offset + multiplicand.derivatives.length - 1;   // index of the multiplicand's last independent variable
            if (topMultiplicandIndex > maxIndex) {
                maxIndex = topMultiplicandIndex;
            }

            // special care must be taken if the value is zero since the differential is multiplied by the product of all other values
            if (multiplicand.value == 0.0) {
                double sensitivity = 1.0;
                for (int otherIndex = 0; otherIndex < multiplicands.length; otherIndex++) {
                    if (otherIndex != termIndex) {
                        sensitivity *= multiplicands[otherIndex].value;
                    }
                }
                sensitivities[termIndex] = sensitivity;
            }
        }

        final double[] derivatives = new double[maxIndex - offset + 1];

        for (int index = offset; index <= maxIndex; index++) {
            double derivative = 0.0;
            for (int termIndex = 0; termIndex < multiplicands.length; termIndex++) {
                final DifferentialVariable multiplicand = multiplicands[termIndex];
                if (multiplicand.value != 0.0) {
                    derivative += value * multiplicand.getDerivative(index) / multiplicand.value;
                } else {
                    derivative += sensitivities[termIndex] * multiplicand.getDerivative(index);
                }
            }
            derivatives[index - offset] = derivative;
        }

        return new DifferentialVariable(value, offset, derivatives);
    }

    /**
     * divide the variable by the scalar value
     */
    public static DifferentialVariable divide(final DifferentialVariable variable, final double value) {
        return DifferentialVariable.multiply(variable, 1 / value);
    }

    /**
     * divide the scalar value by the variable
     */
    public static DifferentialVariable divide(final double value, final DifferentialVariable variable) {
        return DifferentialVariable.multiply(variable.reciprocal(), value);
    }

    /**
     * divide the dividend by the divisor
     */
    public static DifferentialVariable divide(final DifferentialVariable dividend, final DifferentialVariable divisor) {
        return DifferentialVariable.multiply(dividend, divisor.reciprocal());
    }

    /**
     * negate the variable
     */
    public DifferentialVariable negate() {
        final double[] derivatives = new double[this.derivatives.length];
        for (int index = 0; index < derivatives.length; index++) {
            derivatives[index] = -derivatives[index];
        }
        return new DifferentialVariable(-this.value, this.offset, derivatives);
    }

    /**
     * calculate and return the reciprocal of this variable
     */
    public DifferentialVariable reciprocal() {
        final double value = 1.0 / this.value;
        final double inverseFactor = -value * value;   // f = 1/u -> df = - du / u^2
        final double[] derivatives = new double[this.derivatives.length];
        for (int index = 0; index < derivatives.length; index++) {
            derivatives[index] = inverseFactor * this.derivatives[index];
        }
        return new DifferentialVariable(value, this.offset, derivatives);
    }

    /**
     * add this variable to the specified addend
     */
    public DifferentialVariable plus(final double addend) {
        return DifferentialVariable.add(this, addend);
    }

    /**
     * add this variable to the specified addend
     */
    public DifferentialVariable plus(final DifferentialVariable addend) {
        return DifferentialVariable.add(this, addend);
    }

    /**
     * subtract the specified subtrahend from this variable
     */
    public DifferentialVariable minus(final double subtrahend) {
        return DifferentialVariable.subtract(this, subtrahend);
    }

    /**
     * subtract the specified subtrahend from this variable
     */
    public DifferentialVariable minus(final DifferentialVariable subtrahend) {
        return DifferentialVariable.subtract(this, subtrahend);
    }

    /**
     * multiply this variable by the specified multiplicand
     */
    public DifferentialVariable times(final double multiplicand) {
        return DifferentialVariable.multiply(this, multiplicand);
    }

    /**
     * multiply this variable by the specified multiplicand
     */
    public DifferentialVariable times(final DifferentialVariable multiplicand) {
        return DifferentialVariable.multiply(this, multiplicand);
    }

    /**
     * divide this variable by the specified divisor
     */
    public DifferentialVariable over(final double divisor) {
        return DifferentialVariable.divide(this, divisor);
    }

    /**
     * divide this variable by the specified divisor
     */
    public DifferentialVariable over(final DifferentialVariable divisor) {
        return DifferentialVariable.divide(this, divisor);
    }

    /**
     * Perform a unary math operation with the resulting value and the
     * differential factor from the chain rule
     */
    private final DifferentialVariable unaryOperation(final double value, final double differentialFactor) {
        final double[] derivatives = new double[this.derivatives.length];
        for (int index = 0; index < this.derivatives.length; index++) {
            derivatives[index] = differentialFactor * this.derivatives[index];
        }
        return new DifferentialVariable(value, this.offset, derivatives);
    }

    /**
     * raise this variable to the specified power
     */
    public final DifferentialVariable pow(final double power) {
        final double value = Math.pow(this.value, power);
        final double differentialFactor = power * Math.pow(this.value, power - 1.0);
        return unaryOperation(value, differentialFactor);
    }

    /**
     * get the square root of this variable
     */
    public final DifferentialVariable sqrt() {
        return this.pow(0.5);
    }

    /**
     * get the absolute value of this variable
     */
    public final DifferentialVariable abs() {
        final double value = Math.abs(this.value);
        final double differentialFactor = this.value / value;
        return unaryOperation(value, differentialFactor);
    }

    /**
     * get the natural logarithm (base e) of this variable
     */
    public final DifferentialVariable log() {
        final double value = Math.log(this.value);
        final double differentialFactor = 1.0 / this.value;
        return unaryOperation(value, differentialFactor);
    }

    /**
     * get the exponential (base e) of this variable
     */
    public final DifferentialVariable exp() {
        final double value = Math.exp(this.value);
        return unaryOperation(value, value);
    }

    /**
     * get the sine of this variable
     */
    public final DifferentialVariable sin() {
        final double value = Math.sin(this.value);
        final double differentialFactor = Math.cos(this.value);
        return unaryOperation(value, differentialFactor);
    }

    /**
     * get the cosine of this variable
     */
    public final DifferentialVariable cos() {
        final double value = Math.cos(this.value);
        final double differentialFactor = -Math.sin(this.value);
        return unaryOperation(value, differentialFactor);
    }

    /**
     * get the tangent of this variable
     */
    public final DifferentialVariable tan() {
        final double value = Math.tan(this.value);
        final double secant = 1.0 / Math.cos(this.value);
        final double differentialFactor = secant * secant;
        return unaryOperation(value, differentialFactor);
    }

    /**
     * get the arc sine of this variable
     */
    public final DifferentialVariable asin() {
        final double value = Math.asin(this.value);
        final double differentialFactor = 1.0 / Math.sqrt(1.0 - this.value * this.value);
        return unaryOperation(value, differentialFactor);
    }

    /**
     * get the arc cosine of this variable
     */
    public final DifferentialVariable acos() {
        final double value = Math.acos(this.value);
        final double differentialFactor = -1.0 / Math.sqrt(1.0 - this.value * this.value);
        return unaryOperation(value, differentialFactor);
    }

    /**
     * get the arc tangent of this variable
     */
    public final DifferentialVariable atan() {
        final double value = Math.atan(this.value);
        final double differentialFactor = 1.0 / (1.0 + this.value * this.value);
        return unaryOperation(value, differentialFactor);
    }

    /**
     * get the hyperbolic sine of this variable
     */
    public final DifferentialVariable sinh() {
        final double value = Math.sinh(this.value);
        final double differentialFactor = Math.cosh(this.value);
        return unaryOperation(value, differentialFactor);
    }

    /**
     * get the hyperbolic cosine of this variable
     */
    public final DifferentialVariable cosh() {
        final double value = Math.cosh(this.value);
        final double differentialFactor = Math.sinh(this.value);
        return unaryOperation(value, differentialFactor);
    }

    /**
     * get the hyperbolic tangent of this variable
     */
    public final DifferentialVariable tanh() {
        final double value = Math.tanh(this.value);
        final double sech = 1.0 / Math.cosh(this.value);
        final double differentialFactor = sech * sech;
        return unaryOperation(value, differentialFactor);
    }
}
