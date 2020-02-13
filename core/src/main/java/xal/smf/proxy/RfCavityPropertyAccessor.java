/*
 * Created on Mar 17, 2004
 */
package xal.smf.proxy;

import java.util.*;

import xal.smf.AcceleratorNode;
import xal.smf.impl.RfCavity;
import xal.ca.Channel;

/**
 * @author Craig McChesney
 * @author Tom Pelaia
 */
public class RfCavityPropertyAccessor extends AbstractPropertyAccessor {
	
	// Constants ===============================================================	
	
	// Property Names	
	public static final String PROPERTY_PHASE = RfCavity.Property.PHASE.name();
	public static final String PROPERTY_AMPLITUDE = RfCavity.Property.AMPLITUDE.name();
	
	// Class Variables =========================================================
	final private static List<String> PROPERTY_NAMES;

	
	// Class Initialization ===================================================
	
	static {
		PROPERTY_NAMES = new ArrayList<String>();
		PROPERTY_NAMES.add( PROPERTY_PHASE );
		PROPERTY_NAMES.add( PROPERTY_AMPLITUDE );
	}

        /**
         * Convert voltage from MV to V and phase from deg to rad.
         * @param propertyName
         * @return 
         */
    @Override
    protected double getPropertyScale(String propertyName) {
        if (propertyName.equals(PROPERTY_PHASE)) {
            return Math.PI/180.;
        } else if (propertyName.equals(PROPERTY_AMPLITUDE)) {
            return 1.0e6;
        }
        return 1;

    }


	/** get the map of design values keyed by property name */
         @Override
	public Map<String,Double> getDesignValueMap( final AcceleratorNode node ) {
		return getDesignValueMap( node, PROPERTY_NAMES );
	}


	/** get the map of live values keyed by property name */
         @Override
	public Map<String,Double> getLiveValueMap( final AcceleratorNode node, final Map<Channel,Double> channelValues ) {
		return getLiveValueMap( node, channelValues, PROPERTY_NAMES );
	}


	/** get the map of live RF design values keyed by property name */
         @Override
	public Map<String,Double> getLiveRFDesignValueMap( final AcceleratorNode node, final Map<Channel,Double> channelValues ) {
		return getDesignValueMap( node );
	}


	/** get the channels for live property access */
         @Override
	public Collection<Channel> getLiveChannels( final AcceleratorNode node ) {
		return getLiveChannels( node, PROPERTY_NAMES );
	}


	/** get the channels for live property access with design RF */
         @Override
	public Collection<Channel> getLiveRFDesignChannels( final AcceleratorNode node ) {
		return Collections.<Channel>emptySet();
	}


	/** get the list of property names */
         @Override
	public List<String> propertyNames() {
		return new ArrayList<>( PROPERTY_NAMES );
	}

}
