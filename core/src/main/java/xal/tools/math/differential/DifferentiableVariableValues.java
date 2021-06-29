//
// DifferentiableVariable.java: Source file for 'DifferentiableVariable'
// Project xal
//
// Created by Tom Pelaia II on 5/2/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.math.differential;

import java.util.Map;
import java.util.Hashtable;

/**
 * Contains the assigned values for variables
 */
public class DifferentiableVariableValues {

    /**
     * values keyed by variable
     */
    private final Map<DifferentiableVariable, Double> valueMap;

    /**
     * Constructor
     */
    public DifferentiableVariableValues() {
        valueMap = new Hashtable<>();
    }

    /**
     * Get a new instance
     */
    public static DifferentiableVariableValues getInstance() {
        return new DifferentiableVariableValues();
    }

    /**
     * unassign all variable values
     */
    public void clear() {
        valueMap.clear();
    }

    /**
     * number of assignments
     */
    public int assignmentCount() {
        return valueMap.size();
    }

    /**
     * assign the value to the variable
     */
    public void assignValue(final DifferentiableVariable variable, final double value) {
        valueMap.put(variable, value);
    }

    /**
     * unassign the value to the variable
     */
    public void unassignValue(final DifferentiableVariable variable) {
        valueMap.remove(variable);
    }

    /**
     * Determine whether a value has been assigned for the specified variable
     */
    public boolean isAssignedValue(final DifferentiableVariable variable) {
        return valueMap.containsKey(variable);
    }

    /**
     * Get the assigned value
     */
    public double getAssignedValue(final DifferentiableVariable variable) {
        return valueMap.get(variable);
    }

    /**
     * Get the value for the variable using the assigned value if it exists;
     * otherwise using the variable's default value
     */
    public double getValue(final DifferentiableVariable variable) {
        return isAssignedValue(variable) ? getAssignedValue(variable) : variable.getDefaultValue();
    }

    /**
     * Get the string representation of this mapping
     */
    @Override
    public String toString() {
        return valueMap.toString();
    }
}
