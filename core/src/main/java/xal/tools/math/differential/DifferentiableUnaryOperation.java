//
// DifferentiableUnaryOperation.java: Source file for 'DifferentiableUnaryOperation'
// Project xal
//
// Created by Tom Pelaia II on 5/3/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math.differential;


/** DifferentiableUnaryOperation */
abstract public class DifferentiableUnaryOperation extends DifferentiableSymbol {
    /** argument on which the operation is performed */
    protected final DifferentiableOperation argument;
    
    
    /** Constructor */
    protected DifferentiableUnaryOperation( final DifferentiableOperation argument ) {
        this.argument = argument;
    }
    
    
    /** get the argument to negate */
    protected DifferentiableOperation getArgument() {
        return argument;
    }
    
    
    /** Get the label for the operation */
    abstract public String getLabel();
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    abstract public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable );
    
    
    /** Get the derivative with respect to the specified variable applying the chain rule for the argument */
    @Override
    public final DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return argument.getDerivative( variable ).times( getDirectDerivative( variable ) );
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances. Returns true if the arguments match. */
    @Override
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return argument.isEqualTo(((DifferentiableUnaryOperation)operation).argument );
    }
    
    
    /** get the string representation */
    @Override
    public String toString() {
        return getLabel() + "(" + argument + ")";
    }
}
