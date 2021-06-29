//
//  DomainHint.java
//  xal
//
//  Created by Thomas Pelaia on 4/18/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.extension.solver.hint;

import xal.extension.solver.*;



/** A hint that indicates a variable domain. */
public abstract class DomainHint extends Hint {
	public static final int LOWER_IND = 0;
	public static final int UPPER_IND = 1;
	
	
	/** Constructor */
	public DomainHint( final String label ) {
		super( label );
	}
	
	
	/** Determine if there is an entry for the variable */
	public abstract boolean hasVariable( final Variable variable );	
		
	
	/** Get the domain for the specified variable. */
	public abstract double[] getRange( final Variable variable );
}
