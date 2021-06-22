//
//  QualifierFactory.java
//  xal
//
//  Created by Thomas Pelaia on 10/26/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.smf.impl.qualify;

import xal.smf.*;


/** Factory to create qualifiers. */
public class QualifierFactory {
	protected static TypeQualifier goodStatusQualifier;
	protected static TypeQualifier badStatusQualifier;
	
	
	/** Protected constructor */
	protected QualifierFactory() {}
	
	
	/** 
	 * Get a qualifier for testing a node for the specified status 
	 * @param statusFilter the status against which to qualify nodes
	 * @return the status qualifier
	 */
	public static TypeQualifier getStatusQualifier( final boolean statusFilter ) {
		populateStatusQualifiers();
		return statusFilter ? goodStatusQualifier : badStatusQualifier;
	}
	
	
	/**
	 * Get a qualifier for testing whether a node's software type matches the specified software type
	 * @param softType software type for comparison
	 */
	public static TypeQualifier getSoftTypeQualifier( final String softType ) {
		return new TypeQualifier() {
                        @Override
			public boolean match( final AcceleratorNode node ) {
                final String nodeSoftType = node.getSoftType();
                // if neither soft type is null, then compare strings for equality for best reliability otherwise compare pointers
                return nodeSoftType != null && softType != null ? nodeSoftType.equals( softType ) : nodeSoftType == softType;
			}
		};
	}
	
	
	/**
	 * Get an qualifier for the specified node status and type.
	 * @param nodeStatus the node status
	 * @param type the node type
	 * @return a qualifier restricted to both the status and type specified
	 */
	public static TypeQualifier qualifierWithStatusAndType( final boolean nodeStatus, final String type ) {
		return AndTypeQualifier.qualifierWithStatusAndType( nodeStatus, type );
	}
	
	
	/**
	 * Get a qualifier that matches for any of the specified node types and the specified node status.
	 * @param nodeStatus the status of the nodes to match
	 * @param kinds the array of node types
	 * @return a qualifier that matches for any of the given node types
	 */
	public static TypeQualifier qualifierWithStatusAndTypes( final boolean nodeStatus, final String ... kinds ) {
		return new AndTypeQualifier().andStatus( nodeStatus ).and( OrTypeQualifier.qualifierForKinds( kinds ) );
	}
	
	
	/**
	 * Get a qualifier that matches for any of the specified qualifiers and the specified node status.
	 * @param nodeStatus the status of the nodes to match
	 * @param qualifiers the array of node qualifiers
	 * @return a qualifier that matches for any of the given qualifiers
	 */
	public static TypeQualifier qualifierForQualifiers( final boolean nodeStatus, final TypeQualifier ... qualifiers ) {
		return new AndTypeQualifier().andStatus( nodeStatus ).and( OrTypeQualifier.qualifierForQualifiers( qualifiers ) );
	}
	
	
	/** populate status qualifiers */
	protected static void populateStatusQualifiers() {
		if ( goodStatusQualifier == null ) {
			goodStatusQualifier = new TypeQualifier() {
                                @Override
				public boolean match( final AcceleratorNode node ) {
					return node.getStatus();
				}
			};
			
			badStatusQualifier = new TypeQualifier() {
                                @Override
				public boolean match( final AcceleratorNode node ) {
					return !node.getStatus();
				}
			};
		}
	}
}
