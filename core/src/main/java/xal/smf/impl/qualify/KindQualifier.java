/*
 * KindQualifier.java
 *
 * Created on January 24, 2002, 10:57 AM
 */

package xal.smf.impl.qualify;

import xal.smf.*;


/**
 * KindQualifier is used to match nodes based upon the string type of nodes.
 * For example, the official naming convention specifies names and short names 
 * for various elements in the accelerator (e.g. "Q" for quadrupole, "BPM" for
 * a "beam position monitor".  The user can construct a qualifier by specifying
 * an element type by either its name or short name (usually given by the node's
 * "TYPE" property). 
 * @author  tap
 */
public class KindQualifier implements TypeQualifier {
	/** the node type against which to qualify for matches */
    final String kind;
	
	
    /** 
	 * Creates new KindQualifier 
	 * @param newKind The official element type of nodes to match (e.g. BPM.TYPE)
	 */
    public KindQualifier( final String newKind ) {
        kind = newKind;
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
	 * Determine if the specified node is a match based on this qualifier's criteria
	 * @param node The node to test
	 * @return true if the node is a match and false if not
	 */
    @Override
    public boolean match( final AcceleratorNode node ) {
        return node.isKindOf(kind );
    }
}
