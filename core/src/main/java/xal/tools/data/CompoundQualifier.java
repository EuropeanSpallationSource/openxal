//
//  CompoundQualifier.java
//  xal
//
//  Created by Thomas Pelaia on 5/2/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;


/** Merge multiple qualifiers to form a single qualifier */
public abstract class CompoundQualifier implements Qualifier {
	/** the default initial reserve capacity */
	protected static final int DEFAULT_RESERVE_CAPACITY = 2;
	
	/** set of qualifiers that define this compound qualifier */
	protected Qualifier[] qualifiers;
	
	/** the actual number of qualifiers that form this compound qualifier */
	protected int qualifierCount;
	
	
    /**
	 * Primary Constructor 
	 * @param reserve the initial reserve estimate for the number of qualifiers that form this compound qualifier.
	 */
    public CompoundQualifier( final int reserve ) {
		qualifierCount = 0;
        qualifiers = new Qualifier[reserve];
    }
	
	
    /** Constructor */
    public CompoundQualifier() {
        this( DEFAULT_RESERVE_CAPACITY );
    }
    
    
	/**
	 * Append a qualifier to the set of root qualifiers.
	 * @param qualifier The qualifier to append with the existing root qualifiers.
	 * @return This instance for convenience of chaining "append" operations.
	 */
    public CompoundQualifier append( final Qualifier qualifier ) {
		Qualifier[] qualifiers;
		
		if ( this.qualifiers.length <= qualifierCount ) {
			// increase the size by atleast two and roughly 10% more
			qualifiers = new Qualifier[ 2 + (int)(1.1 * this.qualifiers.length) ];
			System.arraycopy(this.qualifiers, 0, qualifiers, 0, this.qualifiers.length );			
		}
		else {
			qualifiers = this.qualifiers;
		}
		
		qualifiers[qualifierCount] = qualifier;
		this.qualifiers = qualifiers;
		++qualifierCount;
		
		return this;
    }
	
	
	/**
	 * The binary operator token.
	 * @return a token representing the binary operator.
	 */
	public abstract String binaryToken();
	
	
	/**
	 * Get a string representation of this instance.
	 * @return a string representing this compound qualifier.
	 */
        @Override
	public String toString() {
		if ( qualifierCount > 1 ) {
			final StringBuilder buffer = new StringBuilder( "(" + qualifiers[0].toString() + ")" );
			for ( int index = 1 ; index < qualifierCount ; index++ ) {
				buffer.append(" ").append(binaryToken()).append(" (");
				buffer.append(qualifiers[index]).append(")");
			}
			return buffer.toString();
		}
		else if ( qualifierCount == 1 ) {
			return qualifiers[0].toString();
		}
		else {
			return "";
		}
	}
}
