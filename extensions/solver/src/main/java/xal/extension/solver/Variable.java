/*
 *  Variable.java
 *
 *  Created Wednesday June 9, 2004 2:31pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;


/**
 * Variable describes a parameter that may be varied by the solver.  It specifies a name, an initial guess and upper and lower limits.
 * @author   ky6
 * @author   t6p
 */
public class Variable {
	/** the name of the variable */
	protected final String name;
	
	/** the initial value/guess assigned to the variable */
	protected double initialValue;
	
	/** the lowest value that can be assigned to the variable */
	protected double lowerLimit;
	
	/** the highest value that can be assigned to the variable */
	protected double upperLimit;
	
	
	/**
	 * Creates a new instance of Variable.
	 * @param initialValue  the initial first guess for the variable (e.g. starting point)
	 * @param name          the name to assign to the variable
	 * @param lowerLimit    the lowest value that should be assigned to the variable
	 * @param upperLimit    the highest value that should be assigned to the variable
	 */
	public Variable( String name, double initialValue, double lowerLimit, double upperLimit ) {
		this.name = name;
		this.initialValue = initialValue;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}
	
	
	/**
	 * Copy this variable but substitute the specified initial value for this variable's initial value.
	 * @param initialValue initial value to use for the new variable
	 * @return new variable with the same properties as this instance but substituting the specified initial value
	 */
	public Variable copyWithInitialValue( final double initialValue ) {
		return new Variable( name, initialValue, lowerLimit, upperLimit );
	}
	
	
	/**
	 * Get this variable's name.
	 * @return   this variable's name
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Get the initial value (i.e. initial guess).
	 * @return   the initial value
	 */
	public double getInitialValue() {
		return initialValue;
	}
	
	
	/**
	 * Get the lowest value that can be assigned to this variable.
	 * @return   the lower limit
	 */
	public double getLowerLimit() {
		return lowerLimit;
	}
	
	
	/**
	 * Get the highest value that can be assigned to this variable.
	 * @return   the upper limit
	 */
	public double getUpperLimit() {
		return upperLimit;
	}
	
	public void setInitialValue(double initialValue) {
		this.initialValue = initialValue;
	}
	
	public void setLowerLimit(double lowerLimit) {
		this.lowerLimit = lowerLimit;
	}
	
	public void setUpperLimit(double upperLimit) {
		this.upperLimit = upperLimit;
	}
	
	
	
	
	/**
	 * A string for displaying a variable. The string consist of a title, an initial value, a
	 * lower limit and an upper limit.
	 * @return   The string representation of a variable.
	 */
        @Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Variable: ").append(name).append(", ");
		buffer.append("Initial Value: ").append(initialValue).append(", ");
		buffer.append("Lower Limit: ").append(lowerLimit).append(", ");
		buffer.append("Upper Limit: ").append(upperLimit);

		return buffer.toString();
	}
}

