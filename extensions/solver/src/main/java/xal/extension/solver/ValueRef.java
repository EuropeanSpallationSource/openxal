/*
 *  ValueRef.java
 *
 *  Created Monday June 14, 2003 9:06am
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

/**
 * ValueRef class is a reference to the current value for a single variable in the current
 * trial point. Value reference provides an alternative way for models to evaluate the trial
 * points. Instead of having to copy values from the latest trial point, evaluators may
 * alternatively choose to simply get the value references up front and then use them directly
 * in the model.
 *
 * @author   ky6
 */
public class ValueRef {
	/** the current value */
	protected double value;


	/**
	 * Set the current value.
	 *
	 * @param value  The new value
	 */
	protected void setValue( final double value ) {
		this.value = value;
	}


	/**
	 * Get the current value.
	 *
	 * @return   the current value
	 */
	public double getValue() {
		return value;
	}
}

