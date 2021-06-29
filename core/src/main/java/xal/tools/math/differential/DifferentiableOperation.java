//
// DifferentiableOperation.java: Source file for 'DifferentiableOperation'
// Project xal
//
// Created by Tom Pelaia II on 4/29/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.math.differential;

import java.util.Map;
import java.util.HashMap;

/**
 * DifferentiableOperation
 */
public abstract class DifferentiableOperation {

    /**
     * precedence at addition level
     */
    public static final int ADDITION_PRECEDENCE = 0;

    /**
     * precedence at subtraction level
     */
    public static final int SUBTRACTION_PRECEDENCE = ADDITION_PRECEDENCE + 1;

    /**
     * precedence at product level
     */
    public static final int PRODUCT_PRECEDENCE = SUBTRACTION_PRECEDENCE + 1;

    /**
     * precedence at quotient level
     */
    public static final int QUOTIENT_PRECEDENCE = PRODUCT_PRECEDENCE + 1;

    /**
     * precedence at power level
     */
    public static final int POWER_PRECEDENCE = QUOTIENT_PRECEDENCE + 1;

    /**
     * precedence at symbol level
     */
    public static final int SYMBOL_PRECEDENCE = POWER_PRECEDENCE + 1;

    /**
     * get a constant operation representing the specified constant value
     */
    public static DifferentiableOperation getConstant(final double value) {
        return DifferentiableConstant.getInstance(value);
    }

    /**
     * get an operation representing the variable
     */
    public static DifferentiableVariable getVariable(final String name, final double defaultValue) {
        return DifferentiableVariable.getInstance(name, defaultValue);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param userSubstitutions a user supplied map of the new operations keyed
     * by the current operations to be substituted
     * @return a new operation with operations substituted if the map is not
     * null and not empty otherwise just return this operation
     */
    public final DifferentiableOperation copyWithSubstitutions(final Map<DifferentiableVariable, DifferentiableOperation> userSubstitutions) {
        // copy the user supplied map as the substitution map will modify the substitutions
        final Map<DifferentiableOperation, DifferentiableOperation> substitutions = new HashMap<>(userSubstitutions);
        return substitutions != null && !substitutions.isEmpty() ? copySubstituting(substitutions) : this;
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    protected abstract DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions);

    /**
     * Construct a copy of this operation substituting the operations given in
     * the map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    protected final DifferentiableOperation copySubstitutingWithCache(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        final DifferentiableOperation cachedSubstitute = substitutions.get(this);
        if (cachedSubstitute != null) {
            return cachedSubstitute;
        } else {
            final DifferentiableOperation substitute = copySubstituting(substitutions);
            substitutions.put(this, substitute);
            return substitute;
        }
    }

    /**
     * Evaluate the operation with the default variable values
     */
    public final double evaluate() {
        return evaluate(null);
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    public double evaluate(final DifferentiableVariableValues valueMap) {
        final Map<DifferentiableOperation, Double> cache = new HashMap<>();
        return evaluate(valueMap, cache);
    }

    /**
     * get the operation precedence
     */
    protected abstract int getPrecedence();

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    protected abstract double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache);

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    protected final double evaluateWithCache(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        final Double cachedValue = cache.get(this);
        if (cachedValue != null) {
            return cachedValue;
        } else {
            final double value = evaluate(valueMap, cache);
            cache.put(this, value);
            return value;
        }
    }

    /**
     * Get the derivative with respect to the specified variable
     */
    public abstract DifferentiableOperation getDerivative(final DifferentiableVariable variable);

    /**
     * add the arguments
     */
    private static DifferentiableOperation sum(final DifferentiableOperation firstAddend, final DifferentiableOperation... addends) {
        DifferentiableOperation sum = firstAddend;
        for (final DifferentiableOperation addend : addends) {
            sum = sum.plus(addend);
        }
        return sum;
    }

    /**
     * add the arguments
     */
    public static DifferentiableOperation sum(final DifferentiableOperation... addends) {
        return sum(DifferentiableZero.getInstance(), addends);
    }

    /**
     * add the specified arguments to this
     */
    public DifferentiableOperation plus(final DifferentiableOperation... addends) {
        return sum(this, addends);
    }

    /**
     * add the addend to this operation returning the new operation
     */
    @SuppressWarnings("cast")     // cast is required to call the specific method with a negation argument
    public DifferentiableOperation plus(final DifferentiableOperation addend) {
        return addend instanceof DifferentiableZero ? this : addend instanceof DifferentiableNegation ? plus((DifferentiableNegation) addend) : DifferentiableAddition.add(this, addend);
    }

    /**
     * add the addend to this operation returning the new operation
     */
    public DifferentiableOperation plus(final double addend) {
        return plus(new DifferentiableConstant(addend));
    }

    /**
     * add the addend to this operation returning the new operation
     */
    public DifferentiableOperation plus(final DifferentiableZero addend) {
        return this;
    }

    /**
     * add the addend to this operation returning the new operation
     */
    public DifferentiableOperation plus(final DifferentiableNegation addend) {
        return minus(addend.getArgument());
    }

    /**
     * subtract the subtrahend from this operation returning the new operation
     */
    @SuppressWarnings("cast")     // cast is required to call the specific method with a negation argument
    public DifferentiableOperation minus(final DifferentiableOperation subtrahend) {
        return subtrahend instanceof DifferentiableZero ? this : subtrahend instanceof DifferentiableNegation ? minus((DifferentiableNegation) subtrahend) : DifferentiableSubtraction.subtract(this, subtrahend);
    }

    /**
     * subtract the subtrahend from this operation returning the new operation
     */
    public DifferentiableOperation minus(final double subtrahend) {
        return minus(new DifferentiableConstant(subtrahend));
    }

    /**
     * subtract the subtrahend from this operation returning the new operation
     */
    public DifferentiableOperation minus(final DifferentiableZero subtrahend) {
        return this;
    }

    /**
     * subtract the subtrahend from this operation returning the new operation
     */
    public DifferentiableOperation minus(final DifferentiableNegation subtrahend) {
        return plus(subtrahend.getArgument());
    }

    /**
     * multiply the arguments
     */
    public static DifferentiableOperation multiply(final DifferentiableOperation... multiplicands) {
        return multiply(DifferentiableOne.getInstance(), multiplicands);
    }

    /**
     * multiply the arguments
     */
    private static DifferentiableOperation multiply(final DifferentiableOperation firstMultiplicand, final DifferentiableOperation... multiplicands) {
        DifferentiableOperation product = firstMultiplicand;
        for (final DifferentiableOperation multiplicand : multiplicands) {
            product = product.times(multiplicand);
        }
        return product;
    }

    /**
     * multiply the arguments
     */
    public DifferentiableOperation times(final DifferentiableOperation... multiplicands) {
        return multiply(this, multiplicands);
    }

    /**
     * multiply the multiplicand to this operation returning the new operation
     */
    @SuppressWarnings("cast")     // cast is required to call the specific times method for zero
    public DifferentiableOperation times(final DifferentiableOperation multiplicand) {
        return multiplicand instanceof DifferentiableZero ? times((DifferentiableZero) multiplicand) : multiplicand instanceof DifferentiableOne ? this : DifferentiableMultiplication.multiply(this, multiplicand);
    }

    /**
     * multiply the multiplicand to this operation returning the new operation
     */
    public DifferentiableOperation times(final double value) {
        return times(DifferentiableConstant.getInstance(value));
    }

    /**
     * multiply the multiplicand to this operation returning the new operation
     */
    public DifferentiableOperation times(final DifferentiableZero multiplicand) {
        return DifferentiableZero.getInstance();
    }

    /**
     * multiply the multiplicand to this operation returning the new operation
     */
    public DifferentiableOperation times(final DifferentiableOne multiplicand) {
        return this;
    }

    /**
     * Divide the operation from this
     */
    @SuppressWarnings("cast")     // cast is required to call the specific method with a quotient argument
    public DifferentiableOperation over(final DifferentiableOperation divisor) {
        return divisor instanceof DifferentiableOne ? this : divisor instanceof DifferentiableDivision ? over((DifferentiableDivision) divisor) : DifferentiableDivision.divide(this, divisor);
    }

    /**
     * Divide the operation from this
     */
    public DifferentiableOperation over(final double value) {
        return over(DifferentiableConstant.getInstance(value));
    }

    /**
     * Divide the operation from this
     */
    public DifferentiableOperation over(final DifferentiableOne divisor) {
        return this;
    }

    /**
     * Divide the operation from this
     */
    public DifferentiableOperation over(final DifferentiableDivision divisor) {
        return times(divisor.reciprocal());
    }

    /**
     * multiply the operation by negative one
     */
    public DifferentiableOperation negate() {
        return DifferentiableNegation.negate(this);
    }

    /**
     * get the reciprocal of this operation
     */
    public DifferentiableOperation reciprocal() {
        return DifferentiableDivision.divide(1.0, this);
    }

    /**
     * get the absolute value of this operation
     */
    public DifferentiableOperation abs() {
        return DifferentiableAbsoluteValue.abs(this);
    }

    /**
     * get this operation raised to the specified power
     */
    public DifferentiableOperation pow(final double power) {
        return DifferentiableConstantPower.pow(this, power);
    }

    /**
     * get this operation raised to the specified power
     */
    public DifferentiableOperation pow(final DifferentiableOperation power) {
        return DifferentiablePower.pow(this, power);
    }

    /**
     * get the square root of this operation
     */
    public DifferentiableOperation sqrt() {
        return DifferentiableSquareRoot.sqrt(this);
    }

    /**
     * get the sine value of this operation
     */
    public DifferentiableOperation sin() {
        return DifferentiableSine.sin(this);
    }

    /**
     * get the cosine value of this operation
     */
    public DifferentiableOperation cos() {
        return DifferentiableCosine.cos(this);
    }

    /**
     * get the tangent value of this operation
     */
    public DifferentiableOperation tan() {
        return DifferentiableTangent.tan(this);
    }

    /**
     * get the arc sine value of this operation
     */
    public DifferentiableOperation asin() {
        return DifferentiableArcSine.asin(this);
    }

    /**
     * get the arc cosine value of this operation
     */
    public DifferentiableOperation acos() {
        return DifferentiableArcCosine.acos(this);
    }

    /**
     * get the arc tangent value of this operation
     */
    public DifferentiableOperation atan() {
        return DifferentiableArcTangent.atan(this);
    }

    /**
     * get the hyperbolic sine value of this operation
     */
    public DifferentiableOperation sinh() {
        return DifferentiableSinh.sinh(this);
    }

    /**
     * get the hyperbolic cosine value of this operation
     */
    public DifferentiableOperation cosh() {
        return DifferentiableCosh.cosh(this);
    }

    /**
     * get the hyperbolic tangent value of this operation
     */
    public DifferentiableOperation tanh() {
        return DifferentiableTanh.tanh(this);
    }

    /**
     * get the exponential of this operation
     */
    public DifferentiableOperation exp() {
        return DifferentiableExponential.exp(this);
    }

    /**
     * get the natural logarithm of this operation
     */
    public DifferentiableOperation log() {
        return DifferentiableLogarithm.log(this);
    }

    /**
     * Generate the string representation based on the precedence of the parent
     * operation
     */
    protected String toString(final int parentPrecedence) {
        return toString(parentPrecedence, false);
    }

    /**
     * Test whether this operation is equivalent to the specified operation when
     * the two operations are different instances.
     */
    protected abstract boolean isEquivalentTo(final DifferentiableOperation operation);

    /**
     * Test whether this operation is equal to or equivalent to the specified
     * operation.
     */
    public final boolean isEqualTo(final DifferentiableOperation operation) {
        return this.equals(operation) || (this.getClass().equals(operation.getClass()) && this.isEquivalentTo(operation));
    }

    /**
     * Generate the string representation based on the precedence of the parent
     * operation
     */
    protected String toString(final int parentPrecedence, final boolean closeIfEqualPrecedence) {
        final int precedence = getPrecedence();

        if (precedence < parentPrecedence || (closeIfEqualPrecedence && precedence == parentPrecedence)) {
            return "(" + toString() + ")";
        } else {
            return toString();
        }
    }
}

/**
 * constant operation
 */
class DifferentiableConstant extends DifferentiableSymbol {

    /**
     * constant value
     */
    private final double value;

    /**
     * Constructor
     */
    public DifferentiableConstant(final double value) {
        this.value = value;
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return this;
    }

    /**
     * generate a new constant operation for the specified constant value
     */
    public static DifferentiableConstant getInstance(final double value) {
        if (value == 0.0) {
            return DifferentiableZero.getInstance();
        } else if (value == 1.0) {
            return DifferentiableOne.getInstance();
        } else {
            return new DifferentiableConstant(value);
        }
    }

    /**
     * get the constant value
     */
    public double getValue() {
        return value;
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return value;
    }

    /**
     * Get the derivative with respect to the specified variable
     */
    @Override
    public final DifferentiableOperation getDerivative(final DifferentiableVariable variable) {
        return DifferentiableZero.getInstance();
    }

    /**
     * add the addend to this operation returning the new operation
     */
    @Override
    public DifferentiableOperation plus(final DifferentiableOperation addend) {
        return addend instanceof DifferentiableConstant ? plus((DifferentiableConstant) addend) : super.plus(addend);
    }

    /**
     * add the addend to this operation returning the new operation
     */
    public DifferentiableOperation plus(final DifferentiableConstant addend) {
        return DifferentiableConstant.getInstance(this.value + addend.value);
    }

    /**
     * add the addend to this operation returning the new operation
     */
    @Override
    public DifferentiableOperation plus(final double value) {
        return DifferentiableConstant.getInstance(this.value + value);
    }

    /**
     * perform constant subtraction
     */
    @Override
    public DifferentiableOperation minus(final DifferentiableOperation subtrahend) {
        return subtrahend instanceof DifferentiableConstant ? minus((DifferentiableConstant) subtrahend) : super.minus(subtrahend);
    }

    /**
     * perform constant subtraction
     */
    public DifferentiableOperation minus(final DifferentiableConstant subtrahend) {
        return DifferentiableConstant.getInstance(this.value - subtrahend.value);
    }

    /**
     * perform constant subtraction
     */
    @Override
    public DifferentiableOperation minus(final double subtrahend) {
        return DifferentiableConstant.getInstance(this.value - subtrahend);
    }

    /**
     * multiply the multiplicand to this operation returning the new operation
     */
    @Override
    public DifferentiableOperation times(final DifferentiableOperation multiplicand) {
        return multiplicand instanceof DifferentiableConstant ? times((DifferentiableConstant) multiplicand) : super.times(multiplicand);
    }

    /**
     * multiply the multiplicand to this operation returning the new operation
     */
    public DifferentiableOperation times(final DifferentiableConstant multiplicand) {
        return DifferentiableConstant.getInstance(this.value * multiplicand.value);
    }

    /**
     * multiply the multiplicand to this operation returning the new operation
     */
    @Override
    public DifferentiableOperation times(final double value) {
        return DifferentiableConstant.getInstance(this.value * value);
    }

    /**
     * perform constant division
     */
    @Override
    public DifferentiableOperation over(final DifferentiableOperation divisor) {
        return divisor instanceof DifferentiableConstant ? over((DifferentiableConstant) divisor) : super.over(divisor);
    }

    /**
     * perform constant division
     */
    public DifferentiableOperation over(final DifferentiableConstant divisor) {
        return DifferentiableConstant.getInstance(this.value / divisor.value);
    }

    /**
     * perform constant division
     */
    @Override
    public DifferentiableOperation over(final double divisor) {
        return DifferentiableConstant.getInstance(this.value / divisor);
    }

    /**
     * get the absolute value of this operation
     */
    @Override
    public DifferentiableOperation abs() {
        return value >= 0 ? this : getInstance(-value);
    }

    /**
     * get the absolute value of this operation
     */
    @Override
    public DifferentiableOperation negate() {
        return getInstance(-value);
    }

    /**
     * Test whether this operation is equivalent to the specified operation when
     * the two operations are different instances. Indicates whether the
     * internal values are equal.
     */
    @Override
    protected boolean isEquivalentTo(final DifferentiableOperation operation) {
        return value == ((DifferentiableConstant) operation).value;
    }

    /**
     * get the string representation
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

/**
 * constant operation representing zero
 */
class DifferentiableZero extends DifferentiableConstant {

    /**
     * singleton constant
     */
    static final DifferentiableZero ZERO_OPERATION;

    // static initializer
    static {
        ZERO_OPERATION = new DifferentiableZero();
    }

    /**
     * Constructor
     */
    private DifferentiableZero() {
        super(0.0);
    }

    /**
     * get the singleton instance
     */
    public static DifferentiableZero getInstance() {
        return ZERO_OPERATION;
    }

    /**
     * add the addend to this operation returning the new operation
     */
    @Override
    public DifferentiableOperation plus(final DifferentiableOperation addend) {
        return addend;
    }

    /**
     * subtract the operation from this
     */
    @Override
    public DifferentiableOperation minus(final DifferentiableOperation subtrahend) {
        return subtrahend.negate();
    }

    /**
     * multiply the multiplicand to this operation returning the new operation
     */
    @Override
    public DifferentiableOperation times(final DifferentiableOperation multiplicand) {
        return this;
    }

    /**
     * Divide the operation from this
     */
    @Override
    public DifferentiableOperation over(final DifferentiableOperation divisor) {
        return this;
    }

    /**
     * multiply the operation by negative one
     */
    @Override
    public DifferentiableOperation negate() {
        return this;
    }
}

/**
 * constant operation representing one
 */
class DifferentiableOne extends DifferentiableConstant {

    /**
     * singleton constant
     */
    static final DifferentiableOne ONE_OPERATION;

    // static initializer
    static {
        ONE_OPERATION = new DifferentiableOne();
    }

    /**
     * Constructor
     */
    private DifferentiableOne() {
        super(1.0);
    }

    /**
     * get the singleton instance
     */
    public static DifferentiableOne getInstance() {
        return ONE_OPERATION;
    }

    /**
     * multiply the multiplicand to this operation returning the new operation
     */
    @Override
    public DifferentiableOperation times(final DifferentiableOperation multiplicand) {
        return multiplicand;
    }
}

/**
 * Operation for adding operation addends
 */
class DifferentiableAddition extends DifferentiableOperation {

    private final DifferentiableOperation summand;
    private final DifferentiableOperation addend;

    /**
     * Constructor
     */
    public DifferentiableAddition(final DifferentiableOperation summand, final DifferentiableOperation addend) {
        if (summand instanceof DifferentiableConstant) {  // always put constants at the end
            this.addend = summand;
            this.summand = addend;
        } else {
            this.summand = summand;
            this.addend = addend;
        }
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableAddition(summand.copySubstitutingWithCache(substitutions), addend.copySubstitutingWithCache(substitutions));
    }

    /**
     * get the operation precedence
     */
    @Override
    protected int getPrecedence() {
        return DifferentiableOperation.ADDITION_PRECEDENCE;
    }

    /**
     * generate the addition operation
     */
    public static DifferentiableOperation add(final DifferentiableOperation summand, final DifferentiableOperation addend) {
        if (addend instanceof DifferentiableNegation) {
            return summand.minus(addend.negate());
        }

        if (summand.isEqualTo(addend.negate())) {
            return DifferentiableOperation.getConstant(0.0);
        }

        // collect all constants if any and place them at the end so constants can be coalesced into a single constant
        double constantSum = 0.0;                       // coalesce constants to this variable
        DifferentiableOperation operationSum = null;    // gather non-constant operations to this variable

        // test whether the summand is an addition operation so any constants can be collected and coalesced
        if (summand instanceof DifferentiableAddition) {
            final DifferentiableAddition summandAddition = (DifferentiableAddition) summand;
            if (summandAddition.addend instanceof DifferentiableConstant) {
                constantSum += ((DifferentiableConstant) summandAddition.addend).getValue();
                operationSum = summandAddition.summand;
            } else {
                operationSum = summand;
            }
        } else if (summand instanceof DifferentiableConstant) {
            constantSum += ((DifferentiableConstant) summand).getValue();
        } else {
            operationSum = summand;
        }

        // test whether the addend is an addition operation so any constants can be collected and coalesced
        if (addend instanceof DifferentiableAddition) {
            final DifferentiableAddition addendAddition = (DifferentiableAddition) addend;
            if (addendAddition.addend instanceof DifferentiableConstant) {
                constantSum += ((DifferentiableConstant) addendAddition.addend).getValue();
                operationSum = operationSum != null ? new DifferentiableAddition(operationSum, addendAddition.summand) : addendAddition.summand;
            } else {
                operationSum = operationSum != null ? new DifferentiableAddition(operationSum, addend) : addend;
            }
        } else if (addend instanceof DifferentiableConstant) {
            constantSum += ((DifferentiableConstant) addend).getValue();
        } else {
            operationSum = operationSum != null ? new DifferentiableAddition(operationSum, addend) : addend;
        }

        if (constantSum == 0.0) {
            return operationSum != null ? operationSum : DifferentiableOperation.getConstant(0.0);
        } else {
            final DifferentiableOperation constantSumOperation = DifferentiableOperation.getConstant(constantSum);
            return operationSum != null ? new DifferentiableAddition(operationSum, constantSumOperation) : constantSumOperation;
        }
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return summand.evaluateWithCache(valueMap, cache) + addend.evaluateWithCache(valueMap, cache);
    }

    /**
     * Get the derivative with respect to the specified variable
     */
    @Override
    public final DifferentiableOperation getDerivative(final DifferentiableVariable variable) {
        return summand.getDerivative(variable).plus(addend.getDerivative(variable));
    }

    /**
     * Test whether this operation is equivalent to the specified operation when
     * the two operations are different instances. The summands and addends must
     * be equal but the order doesn't matter.
     */
    @Override
    protected boolean isEquivalentTo(final DifferentiableOperation operation) {
        return (summand.isEqualTo(((DifferentiableAddition) operation).summand) && addend.isEqualTo(((DifferentiableAddition) operation).addend))
                || (summand.isEqualTo(((DifferentiableAddition) operation).addend) && addend.isEqualTo(((DifferentiableAddition) operation).summand));
    }

    /**
     * get the string representation
     */
    @Override
    public String toString() {
        final int precedence = getPrecedence();
        return summand.toString(precedence) + " + " + addend.toString(precedence);
    }
}

/**
 * Operation for subtracting operations
 */
class DifferentiableSubtraction extends DifferentiableOperation {

    private final DifferentiableOperation minuend;
    private final DifferentiableOperation subtrahend;

    /**
     * Constructor
     */
    public DifferentiableSubtraction(final DifferentiableOperation minuend, final DifferentiableOperation subtrahend) {
        this.minuend = minuend;
        this.subtrahend = subtrahend;
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableSubtraction(minuend.copySubstitutingWithCache(substitutions), subtrahend.copySubstitutingWithCache(substitutions));
    }

    /**
     * get the operation precedence
     */
    @Override
    protected int getPrecedence() {
        return DifferentiableOperation.SUBTRACTION_PRECEDENCE;
    }

    /**
     * generate the addition operation
     */
    public static DifferentiableOperation subtract(final DifferentiableOperation minuend, final DifferentiableOperation subtrahend) {
        return minuend.isEqualTo(subtrahend) ? DifferentiableOperation.getConstant(0.0) : new DifferentiableSubtraction(minuend, subtrahend);
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return minuend.evaluateWithCache(valueMap, cache) - subtrahend.evaluateWithCache(valueMap, cache);
    }

    /**
     * Get the derivative with respect to the specified variable
     */
    @Override
    public final DifferentiableOperation getDerivative(final DifferentiableVariable variable) {
        return minuend.getDerivative(variable).minus(subtrahend.getDerivative(variable));
    }

    /**
     * Test whether this operation is equivalent to the specified operation when
     * the two operations are different instances. Indicates whether the minuend
     * and subtrahend are equal to those of the specified operation.
     */
    @Override
    protected boolean isEquivalentTo(final DifferentiableOperation operation) {
        return minuend.isEqualTo(((DifferentiableSubtraction) operation).minuend) && subtrahend.isEqualTo(((DifferentiableSubtraction) operation).subtrahend);
    }

    /**
     * get the string representation
     */
    @Override
    public String toString() {
        final int precedence = getPrecedence();
        return minuend.toString(precedence) + " - " + subtrahend.toString(precedence, true);
    }
}

/**
 * Operation for multiplying two operations
 */
class DifferentiableMultiplication extends DifferentiableOperation {

    private final DifferentiableOperation multiplicand;
    private final DifferentiableOperation multiplier;

    /**
     * Constructor
     */
    public DifferentiableMultiplication(final DifferentiableOperation multiplicand, final DifferentiableOperation multiplier) {
        if (multiplier instanceof DifferentiableConstant) {   // always put constants at the front
            this.multiplicand = multiplier;
            this.multiplier = multiplicand;
        } else {
            this.multiplicand = multiplicand;
            this.multiplier = multiplier;
        }
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableMultiplication(multiplicand.copySubstitutingWithCache(substitutions), multiplier.copySubstitutingWithCache(substitutions));
    }

    /**
     * get the operation precedence
     */
    @Override
    protected int getPrecedence() {
        return DifferentiableOperation.PRODUCT_PRECEDENCE;
    }

    /**
     * generate the multiplication operation
     */
    public static DifferentiableOperation multiply(final DifferentiableOperation multiplicand, final DifferentiableOperation multiplier) {
        // collect all constants if any and place them at the front so constants can be coalesced into a single constant

        double constantProduct = 1.0;                       // coalesce constants to this variable
        DifferentiableOperation operationProduct = null;    // gather non-constant operations to this variable

        // test whether the multiplicand is a multiplication operation so any constants can be collected and coalesced
        if (multiplicand instanceof DifferentiableMultiplication) {
            final DifferentiableMultiplication multiplicandProduct = (DifferentiableMultiplication) multiplicand;
            if (multiplicandProduct.multiplicand instanceof DifferentiableConstant) {
                constantProduct *= ((DifferentiableConstant) multiplicandProduct.multiplicand).getValue();
                operationProduct = multiplicandProduct.multiplier;
            } else {
                operationProduct = multiplicand;
            }
        } else if (multiplicand instanceof DifferentiableConstant) {
            constantProduct *= ((DifferentiableConstant) multiplicand).getValue();
        } else {
            operationProduct = multiplicand;
        }

        // test whether the multiplier is a multiplication operation so any constants can be collected and coalesced
        if (multiplier instanceof DifferentiableMultiplication) {
            final DifferentiableMultiplication multiplierProduct = (DifferentiableMultiplication) multiplier;
            if (multiplierProduct.multiplicand instanceof DifferentiableConstant) {
                constantProduct *= ((DifferentiableConstant) multiplierProduct.multiplicand).getValue();
                operationProduct = operationProduct != null ? new DifferentiableMultiplication(operationProduct, multiplierProduct.multiplier) : multiplierProduct.multiplier;
            } else {
                operationProduct = operationProduct != null ? new DifferentiableMultiplication(operationProduct, multiplier) : multiplier;
            }
        } else if (multiplier instanceof DifferentiableConstant) {
            constantProduct *= ((DifferentiableConstant) multiplier).getValue();
        } else {
            operationProduct = operationProduct != null ? new DifferentiableMultiplication(operationProduct, multiplier) : multiplier;
        }

        if (constantProduct == 0.0) {
            return DifferentiableOperation.getConstant(0.0);
        } else if (constantProduct == 1.0) {
            return operationProduct != null ? operationProduct : DifferentiableOperation.getConstant(1.0);
        } else {
            final DifferentiableOperation constantProductOperation = DifferentiableOperation.getConstant(constantProduct);
            return operationProduct != null ? new DifferentiableMultiplication(constantProductOperation, operationProduct) : constantProductOperation;
        }
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return multiplicand.evaluateWithCache(valueMap, cache) * multiplier.evaluateWithCache(valueMap, cache);
    }

    /**
     * Get the derivative with respect to the specified variable
     */
    @Override
    public final DifferentiableOperation getDerivative(final DifferentiableVariable variable) {
        return multiplicand.getDerivative(variable).times(multiplier).plus(multiplicand.times(multiplier.getDerivative(variable)));
    }

    /**
     * Test whether this operation is equivalent to the specified operation when
     * the two operations are different instances. Indicates whether the
     * multiplicands and multipliers match those of the specified operation.
     */
    @Override
    protected boolean isEquivalentTo(final DifferentiableOperation operation) {
        return (multiplicand.isEqualTo(((DifferentiableMultiplication) operation).multiplicand) && multiplier.isEqualTo(((DifferentiableMultiplication) operation).multiplier))
                || (multiplicand.isEqualTo(((DifferentiableMultiplication) operation).multiplier) && multiplier.isEqualTo(((DifferentiableMultiplication) operation).multiplicand));
    }

    /**
     * get the string representation
     */
    @Override
    public String toString() {
        final int precedence = getPrecedence();
        return multiplicand.toString(precedence) + " * " + multiplier.toString(precedence);
    }
}

/**
 * Operation for dividing two operations
 */
class DifferentiableDivision extends DifferentiableOperation {

    private final DifferentiableOperation dividend;
    private final DifferentiableOperation divisor;

    /**
     * Constructor
     */
    public DifferentiableDivision(final DifferentiableOperation dividend, final DifferentiableOperation divisor) {
        this.dividend = dividend;
        this.divisor = divisor;
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableDivision(dividend.copySubstitutingWithCache(substitutions), divisor.copySubstitutingWithCache(substitutions));
    }

    /**
     * get the operation precedence
     */
    @Override
    protected int getPrecedence() {
        return DifferentiableOperation.QUOTIENT_PRECEDENCE;
    }

    /**
     * generate the division operation
     */
    public static DifferentiableOperation divide(final DifferentiableOperation dividend, final DifferentiableOperation divisor) {
        return dividend.isEqualTo(divisor) ? DifferentiableOperation.getConstant(1.0) : new DifferentiableDivision(dividend, divisor);
    }

    /**
     * generate the division operation
     */
    public static DifferentiableOperation divide(final double dividend, final DifferentiableOperation divisor) {
        return new DifferentiableDivision(getConstant(dividend), divisor);
    }

    /**
     * get the reciprocal operation
     */
    @Override
    public DifferentiableOperation reciprocal() {
        return divisor.over(dividend);
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return dividend.evaluateWithCache(valueMap, cache) / divisor.evaluateWithCache(valueMap, cache);
    }

    /**
     * Get the derivative with respect to the specified variable
     */
    @Override
    public final DifferentiableOperation getDerivative(final DifferentiableVariable variable) {
        return dividend.getDerivative(variable).minus(divisor.getDerivative(variable).times(dividend).over(divisor)).over(divisor);
    }

    /**
     * Test whether this operation is equivalent to the specified operation when
     * the two operations are different instances. Indicates whether the
     * dividend and divers match those of the specified operation.
     */
    @Override
    protected boolean isEquivalentTo(final DifferentiableOperation operation) {
        return dividend.isEqualTo(((DifferentiableDivision) operation).dividend) && divisor.isEqualTo(((DifferentiableDivision) operation).divisor);
    }

    /**
     * get the string representation
     */
    @Override
    public String toString() {
        final int precedence = getPrecedence();
        return dividend.toString(precedence) + " / " + divisor.toString(precedence, true);
    }
}

/**
 * Operation for getting the negative of an operation
 */
class DifferentiableNegation extends DifferentiableOperation {

    private final DifferentiableOperation argument;

    /**
     * Constructor
     */
    public DifferentiableNegation(final DifferentiableOperation argument) {
        this.argument = argument;
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableNegation(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * get the operation precedence
     */
    @Override
    protected int getPrecedence() {
        return DifferentiableOperation.PRODUCT_PRECEDENCE;
    }

    /**
     * get the argument to negate
     */
    DifferentiableOperation getArgument() {
        return argument;
    }

    /**
     * generate the division operation
     */
    public static DifferentiableOperation negate(final DifferentiableOperation argument) {
        return new DifferentiableNegation(argument);
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return -argument.evaluateWithCache(valueMap, cache);
    }

    /**
     * Get the derivative with respect to the specified variable
     */
    @Override
    public final DifferentiableOperation getDerivative(final DifferentiableVariable variable) {
        return argument.getDerivative(variable).negate();
    }

    /**
     * get the negation value of this operation
     */
    @Override
    public DifferentiableOperation negate() {
        return argument;
    }

    /**
     * get the absolute value of this operation
     */
    @Override
    public DifferentiableOperation abs() {
        return argument.abs();
    }

    /**
     * get the cosine of this operation
     */
    @Override
    public DifferentiableOperation cos() {
        return argument.cos();
    }

    /**
     * Test whether this operation is equivalent to the specified operation when
     * the two operations are different instances.
     */
    @Override
    protected boolean isEquivalentTo(final DifferentiableOperation operation) {
        return argument.isEqualTo(((DifferentiableNegation) operation).argument);
    }

    /**
     * get the string representation
     */
    @Override
    public String toString() {
        final int precedence = getPrecedence();
        return "-" + argument.toString(precedence);
    }
}

/**
 * Operation for getting the absolute value of an operation
 */
class DifferentiableAbsoluteValue extends DifferentiableSymbol {

    private final DifferentiableOperation argument;

    /**
     * Constructor
     */
    public DifferentiableAbsoluteValue(final DifferentiableOperation argument) {
        this.argument = argument;
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableAbsoluteValue(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * get the argument to negate
     */
    DifferentiableOperation getArgument() {
        return argument;
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation abs(final DifferentiableOperation argument) {
        return new DifferentiableAbsoluteValue(argument);
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.abs(argument.evaluateWithCache(valueMap, cache));
    }

    /**
     * Get the derivative with respect to the specified variable
     */
    @Override
    public final DifferentiableOperation getDerivative(final DifferentiableVariable variable) {
        return argument.getDerivative(variable).times(argument).over(argument.abs());
    }

    /**
     * get the absolute value of this operation
     */
    @Override
    public DifferentiableOperation abs() {
        return this;
    }

    /**
     * Test whether this operation is equivalent to the specified operation when
     * the two operations are different instances.
     */
    @Override
    protected boolean isEquivalentTo(final DifferentiableOperation operation) {
        return argument.isEqualTo(((DifferentiableAbsoluteValue) operation).argument);
    }

    /**
     * get the string representation
     */
    @Override
    public String toString() {
        return "|" + argument + "|";
    }
}

/**
 * Operation for getting the sine of an operation
 */
class DifferentiableSine extends DifferentiableUnaryOperation {

    /**
     * Constructor
     */
    public DifferentiableSine(final DifferentiableOperation argument) {
        super(argument);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableSine(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation sin(final DifferentiableOperation argument) {
        return new DifferentiableSine(argument);
    }

    /**
     * Get the label for the operation
     */
    @Override
    public final String getLabel() {
        return "sin";
    }

    /**
     * Get the derivative for just this operation without regard for the chain
     * rule
     */
    @Override
    public final DifferentiableOperation getDirectDerivative(final DifferentiableVariable variable) {
        return argument.cos();
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.sin(argument.evaluateWithCache(valueMap, cache));
    }
}

/**
 * Operation for getting the cosine of an operation
 */
class DifferentiableCosine extends DifferentiableUnaryOperation {

    /**
     * Constructor
     */
    public DifferentiableCosine(final DifferentiableOperation argument) {
        super(argument);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableCosine(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation cos(final DifferentiableOperation argument) {
        return argument instanceof DifferentiableAbsoluteValue ? cos((DifferentiableAbsoluteValue) argument) : argument instanceof DifferentiableNegation ? cos((DifferentiableNegation) argument) : new DifferentiableCosine(argument);
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation cos(final DifferentiableAbsoluteValue argument) {
        return new DifferentiableCosine(argument.getArgument());
    }

    /**
     * generate the division operation
     */
    public static DifferentiableOperation cos(final DifferentiableNegation argument) {
        return new DifferentiableCosine(argument.getArgument());
    }

    /**
     * Get the label for the operation
     */
    @Override
    public final String getLabel() {
        return "cos";
    }

    /**
     * Get the derivative for just this operation without regard for the chain
     * rule
     */
    @Override
    public final DifferentiableOperation getDirectDerivative(final DifferentiableVariable variable) {
        return argument.sin().negate();
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.cos(argument.evaluateWithCache(valueMap, cache));
    }
}

/**
 * Operation for getting the tangent of an operation
 */
class DifferentiableTangent extends DifferentiableUnaryOperation {

    /**
     * Constructor
     */
    public DifferentiableTangent(final DifferentiableOperation argument) {
        super(argument);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableTangent(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation tan(final DifferentiableOperation argument) {
        return new DifferentiableTangent(argument);
    }

    /**
     * Get the label for the operation
     */
    @Override
    public final String getLabel() {
        return "tan";
    }

    /**
     * Get the derivative for just this operation without regard for the chain
     * rule
     */
    @Override
    public final DifferentiableOperation getDirectDerivative(final DifferentiableVariable variable) {
        return argument.cos().pow(-2.0);
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.tan(argument.evaluateWithCache(valueMap, cache));
    }
}

/**
 * Operation for getting the arc sine of an operation
 */
class DifferentiableArcSine extends DifferentiableUnaryOperation {

    /**
     * Constructor
     */
    public DifferentiableArcSine(final DifferentiableOperation argument) {
        super(argument);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableArcSine(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation asin(final DifferentiableOperation argument) {
        return new DifferentiableArcSine(argument);
    }

    /**
     * Get the label for the operation
     */
    @Override
    public final String getLabel() {
        return "asin";
    }

    /**
     * Get the derivative for just this operation without regard for the chain
     * rule
     */
    @Override
    public final DifferentiableOperation getDirectDerivative(final DifferentiableVariable variable) {
        return DifferentiableOne.getInstance().minus(argument.pow(2)).sqrt().reciprocal();
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.asin(argument.evaluateWithCache(valueMap, cache));
    }
}

/**
 * Operation for getting the arc cosine of an operation
 */
class DifferentiableArcCosine extends DifferentiableUnaryOperation {

    /**
     * Constructor
     */
    public DifferentiableArcCosine(final DifferentiableOperation argument) {
        super(argument);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableArcCosine(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation acos(final DifferentiableOperation argument) {
        return new DifferentiableArcCosine(argument);
    }

    /**
     * Get the label for the operation
     */
    @Override
    public final String getLabel() {
        return "acos";
    }

    /**
     * Get the derivative for just this operation without regard for the chain
     * rule
     */
    @Override
    public final DifferentiableOperation getDirectDerivative(final DifferentiableVariable variable) {
        return DifferentiableConstant.getInstance(-1.0).over(DifferentiableOne.getInstance().minus(argument.pow(2)).sqrt());
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.acos(argument.evaluateWithCache(valueMap, cache));
    }
}

/**
 * Operation for getting the arc tangent of an operation
 */
class DifferentiableArcTangent extends DifferentiableUnaryOperation {

    /**
     * Constructor
     */
    public DifferentiableArcTangent(final DifferentiableOperation argument) {
        super(argument);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableArcTangent(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation atan(final DifferentiableOperation argument) {
        return new DifferentiableArcTangent(argument);
    }

    /**
     * Get the label for the operation
     */
    @Override
    public final String getLabel() {
        return "atan";
    }

    /**
     * Get the derivative for just this operation without regard for the chain
     * rule
     */
    @Override
    public final DifferentiableOperation getDirectDerivative(final DifferentiableVariable variable) {
        return DifferentiableOne.getInstance().plus(argument.pow(2)).reciprocal();
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.atan(argument.evaluateWithCache(valueMap, cache));
    }
}

/**
 * Operation for getting the hyperbolic sine of an operation
 */
class DifferentiableSinh extends DifferentiableUnaryOperation {

    /**
     * Constructor
     */
    public DifferentiableSinh(final DifferentiableOperation argument) {
        super(argument);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableSinh(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation sinh(final DifferentiableOperation argument) {
        return new DifferentiableSinh(argument);
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.sinh(argument.evaluateWithCache(valueMap, cache));
    }

    /**
     * Get the label for the operation
     */
    @Override
    public final String getLabel() {
        return "sinh";
    }

    /**
     * Get the derivative for just this operation without regard for the chain
     * rule
     */
    @Override
    public final DifferentiableOperation getDirectDerivative(final DifferentiableVariable variable) {
        return argument.cosh();
    }
}

/**
 * Operation for getting the hyperbolic cosine of an operation
 */
class DifferentiableCosh extends DifferentiableUnaryOperation {

    /**
     * Constructor
     */
    public DifferentiableCosh(final DifferentiableOperation argument) {
        super(argument);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableCosh(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation cosh(final DifferentiableOperation argument) {
        return argument instanceof DifferentiableAbsoluteValue ? cosh((DifferentiableAbsoluteValue) argument) : argument instanceof DifferentiableNegation ? cosh((DifferentiableNegation) argument) : new DifferentiableCosh(argument);
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation cosh(final DifferentiableAbsoluteValue argument) {
        return new DifferentiableCosh(argument.getArgument());
    }

    /**
     * generate the division operation
     */
    public static DifferentiableOperation cosh(final DifferentiableNegation argument) {
        return new DifferentiableCosh(argument.getArgument());
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.cosh(argument.evaluateWithCache(valueMap, cache));
    }

    /**
     * Get the label for the operation
     */
    @Override
    public final String getLabel() {
        return "cosh";
    }

    /**
     * Get the derivative for just this operation without regard for the chain
     * rule
     */
    @Override
    public final DifferentiableOperation getDirectDerivative(final DifferentiableVariable variable) {
        return argument.sinh();
    }
}

/**
 * Operation for getting the hyperbolic tangent of an operation
 */
class DifferentiableTanh extends DifferentiableUnaryOperation {

    /**
     * Constructor
     */
    public DifferentiableTanh(final DifferentiableOperation argument) {
        super(argument);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableTanh(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation tanh(final DifferentiableOperation argument) {
        return new DifferentiableTanh(argument);
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.tanh(argument.evaluateWithCache(valueMap, cache));
    }

    /**
     * Get the label for the operation
     */
    @Override
    public final String getLabel() {
        return "tanh";
    }

    /**
     * Get the derivative for just this operation without regard for the chain
     * rule
     */
    @Override
    public final DifferentiableOperation getDirectDerivative(final DifferentiableVariable variable) {
        return argument.cosh().pow(-2.0);
    }
}

/**
 * Operation for raising the argument to a constant power
 */
class DifferentiableConstantPower extends DifferentiableSymbol {

    /**
     * argument to raise to the power
     */
    protected final DifferentiableOperation argument;

    /**
     * power to which to raise the argument
     */
    private final double POWER;

    /**
     * Constructor
     */
    public DifferentiableConstantPower(final DifferentiableOperation argument, final double power) {
        this.argument = argument;
        POWER = power;
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableConstantPower(argument.copySubstitutingWithCache(substitutions), POWER);
    }

    /**
     * get the operation precedence
     */
    @Override
    protected int getPrecedence() {
        return DifferentiableOperation.POWER_PRECEDENCE;
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation pow(final DifferentiableOperation argument, final double power) {
        return power == 0.0 ? DifferentiableOne.getInstance() : power == 1.0 ? argument : power == 0.5 ? DifferentiableSquareRoot.sqrt(argument) : new DifferentiableConstantPower(argument, power);
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation pow(final DifferentiableOperation argument, final DifferentiableConstant power) {
        return DifferentiableConstantPower.pow(argument, power.getValue());
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.pow(argument.evaluateWithCache(valueMap, cache), POWER);
    }

    /**
     * Get the derivative with respect to the specified variable
     */
    @Override
    public final DifferentiableOperation getDerivative(final DifferentiableVariable variable) {
        return new DifferentiableConstant(POWER).times(argument.getDerivative(variable)).times(DifferentiableConstantPower.pow(argument, POWER - 1.0));
    }

    /**
     * Test whether this operation is equivalent to the specified operation when
     * the two operations are different instances.
     */
    @Override
    protected boolean isEquivalentTo(final DifferentiableOperation operation) {
        return argument.isEqualTo(((DifferentiableConstantPower) operation).argument) && POWER == ((DifferentiableConstantPower) operation).POWER;
    }

    /**
     * get the string representation
     */
    @Override
    public String toString() {
        return argument.toString(getPrecedence()) + " ^ " + POWER;
    }
}

/**
 * Operation for raising the argument to an arbitrary power
 */
class DifferentiablePower extends DifferentiableSymbol {

    /**
     * argument to raise to the power
     */
    protected final DifferentiableOperation argument;

    /**
     * power to which to raise the argument
     */
    protected final DifferentiableOperation power;

    /**
     * Constructor
     */
    public DifferentiablePower(final DifferentiableOperation argument, final DifferentiableOperation power) {
        this.argument = argument;
        this.power = power;
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiablePower(argument.copySubstitutingWithCache(substitutions), power.copySubstitutingWithCache(substitutions));
    }

    /**
     * get the operation precedence
     */
    @Override
    protected int getPrecedence() {
        return DifferentiableOperation.POWER_PRECEDENCE;
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation pow(final DifferentiableOperation argument, final DifferentiableOperation power) {
        return power instanceof DifferentiableConstant ? pow(argument, (DifferentiableConstant) power) : new DifferentiablePower(argument, power);
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation pow(final DifferentiableOperation argument, final double power) {
        return DifferentiableConstantPower.pow(argument, power);
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation pow(final DifferentiableOperation argument, final DifferentiableConstant power) {
        return DifferentiableConstantPower.pow(argument, power.getValue());
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.pow(argument.evaluateWithCache(valueMap, cache), power.evaluateWithCache(valueMap, cache));
    }

    /**
     * Get the derivative with respect to the specified variable
     */
    @Override
    public final DifferentiableOperation getDerivative(final DifferentiableVariable variable) {
        return this.times(power.getDerivative(variable).times(argument.log()).plus(power.times(argument.getDerivative(variable)).over(argument)));
    }

    /**
     * Test whether this operation is equivalent to the specified operation when
     * the two operations are different instances.
     */
    @Override
    protected boolean isEquivalentTo(final DifferentiableOperation operation) {
        return argument.isEqualTo(((DifferentiablePower) operation).argument) && power == ((DifferentiablePower) operation).power;
    }

    /**
     * get the string representation
     */
    @Override
    public String toString() {
        final int precedence = getPrecedence();
        return argument.toString(precedence) + " ^ " + power.toString(precedence, true);
    }
}

/**
 * Operation for taking the square root
 */
class DifferentiableSquareRoot extends DifferentiableConstantPower {

    /**
     * Constructor
     */
    public DifferentiableSquareRoot(final DifferentiableOperation argument) {
        super(argument, 0.5);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableSquareRoot(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation sqrt(final DifferentiableOperation argument) {
        return new DifferentiableSquareRoot(argument);
    }

    /**
     * get the string representation
     */
    @Override
    public String toString() {
        return "sqrt(" + argument + ")";
    }
}

/**
 * Operation for performing the exponential
 */
class DifferentiableExponential extends DifferentiableUnaryOperation {

    /**
     * Constructor
     */
    public DifferentiableExponential(final DifferentiableOperation argument) {
        super(argument);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableExponential(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation exp(final DifferentiableOperation argument) {
        return new DifferentiableExponential(argument);
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.exp(argument.evaluateWithCache(valueMap, cache));
    }

    /**
     * Get the label for the operation
     */
    @Override
    public final String getLabel() {
        return "exp";
    }

    /**
     * Get the derivative for just this operation without regard for the chain
     * rule
     */
    @Override
    public final DifferentiableOperation getDirectDerivative(final DifferentiableVariable variable) {
        return this;
    }
}

/**
 * Operation for performing the logarithm
 */
class DifferentiableLogarithm extends DifferentiableUnaryOperation {

    /**
     * Constructor
     */
    public DifferentiableLogarithm(final DifferentiableOperation argument) {
        super(argument);
    }

    /**
     * Construct a copy this operation substituting the operations given in the
     * map.
     *
     * @param substitutions map of the new operations keyed by the current
     * operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting(final Map<DifferentiableOperation, DifferentiableOperation> substitutions) {
        return new DifferentiableLogarithm(argument.copySubstitutingWithCache(substitutions));
    }

    /**
     * generate the operation
     */
    public static DifferentiableOperation log(final DifferentiableOperation argument) {
        return new DifferentiableLogarithm(argument);
    }

    /**
     * Evaluate the operation for the specified variable values using the
     * default value if this variable is not specified in the map
     */
    @Override
    public double evaluate(final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation, Double> cache) {
        return Math.log(argument.evaluateWithCache(valueMap, cache));
    }

    /**
     * Get the label for the operation
     */
    @Override
    public final String getLabel() {
        return "ln";
    }

    /**
     * Get the derivative for just this operation without regard for the chain
     * rule
     */
    @Override
    public final DifferentiableOperation getDirectDerivative(final DifferentiableVariable variable) {
        return argument.reciprocal();
    }
}
