//
// BoundedDifferentiableVariable.java
// 
//
// Created by Tom Pelaia on 11/14/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.math.differential;

/**
 * BoundedDifferentiableVariable
 */
public class BoundedDifferentiableVariable extends DifferentiableVariable {

    /**
     * minimum assignable value
     */
    private double lowerLimit;

    /**
     * maximum assignable value
     */
    private double upperLimit;

    /**
     * Constructor
     */
    public BoundedDifferentiableVariable(final String name, final double defaultValue, final double lowerLimit, final double upperLimit) {
        super(name, defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    /**
     * get the lower limit
     */
    public double getLowerLimit() {
        return lowerLimit;
    }

    /**
     * set the lower limit
     */
    public void setLowerLimit(final double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    /**
     * get the upper limit
     */
    public double getUpperLimit() {
        return upperLimit;
    }

    /**
     * set the upper limit
     */
    public void setUpperLimit(final double upperLimit) {
        this.upperLimit = upperLimit;
    }

    /**
     * determine whether the value is bounded within the given bounds
     */
    public boolean isBounded(final double value) {
        return value >= lowerLimit && value <= upperLimit;
    }

    /**
     * get the nearest bounded (i.e. within bounds) value to the specified value
     */
    public double getNearestBoundedValue(final double value) {
        return value < lowerLimit ? lowerLimit : value > upperLimit ? upperLimit : value;
    }
}
