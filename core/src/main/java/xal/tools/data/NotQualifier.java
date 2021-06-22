//
//  NotKeyValueQualifier.java
//  xal
//
//  Created by Thomas Pelaia on 5/2/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;


/** Qualifier which negates another qualifier's matching. */
public class NotQualifier implements Qualifier {
	/** the qualifier whose matching is negated. */
	private Qualifier qualifier;
	
	
	/**
	 * Constructor
	 * @param qualifier The qualifier to negate.
	 */
	public NotQualifier( final Qualifier qualifier ) {
		this.qualifier = qualifier;
	}
	
	
	/** 
	 * Determine if the specified object is not a match to the enclosed qualifier.
	 * @param object the object to test for matching
	 * @return true if the object does not match the enclosed qualifier's criteria and false if it does match.
	 */
        @Override
	public boolean matches( final Object object ) {
		return !qualifier.matches( object );
	}
	
	
	/**
	 * Get a string representation of this instance.
	 * @return "!" followed by this instance's associated qualifier.
	 */
        @Override
	public String toString() {
		return "!(" + qualifier + ")";
	}
}
