//
// DifferentiableVariable.java: Source file for 'DifferentiableVariable'
// Project xal
//
// Created by Tom Pelaia II on 5/2/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math.differential;

import java.util.Map;


/** Variable used in a differentiable operation */
public class DifferentiableVariable extends DifferentiableSymbol {
    /** name of the variable */
    private final String name;
    
    /** default value assigned to this variable */
    private double defaultValue;
    
    
    /** 
     * Constructor 
     * @param name  name of this variable
     * @param defaultValue value to use when none is supplied during evaluation
     */
    public DifferentiableVariable( final String name, final double defaultValue ) {
        this.name = name;
        setDefaultValue( defaultValue );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    @Override
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        final DifferentiableOperation substitution = substitutions.get( this );
        return substitution != null ? substitution : this;
    }
    
    
    /** Get the name of this variable */
    public String getName() {
        return name;
    }
    
    
    /** get the default value */
    public double getDefaultValue() {
        return defaultValue;
    }
    
    
    /** set the default value */
    public void setDefaultValue( final double value ) {
        defaultValue = value;
    }
    
    
    /** generate a new variable with the specified name */
    public static DifferentiableVariable getInstance( final String name, final double defaultValue ) {
        return new DifferentiableVariable( name, defaultValue );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    @Override
    public final double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return valueMap != null ? valueMap.getValue( this ) : defaultValue;
    }
    
    
    /** Get the derivative with respect to the coordinate at the specified index */
    @Override
    public final DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return variable == this ? DifferentiableOperation.getConstant( 1.0 ) : DifferentiableOperation.getConstant( 0.0 );
    }
    
    
    /** get the string representation of this variable */
    @Override
    public String toString() {
        return name;
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances. Always returns false. */
    @Override
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return false;
    }
}
