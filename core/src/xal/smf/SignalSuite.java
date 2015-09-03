/*
 * SignalSuite.java
 *
 * Created on September 20, 2002, 1:48 PM
 */

package xal.smf;

import xal.tools.data.*;
import xal.tools.transforms.ValueTransform;

import java.util.*;


/**
 * SignalSuite represents the map of handle/signal pairs that identifies a 
 * channel and associates it with a node via the handle.
 *
 * @author  tap
 */
public class SignalSuite {
	/** map of signal entries keyed by handle */
    final private Map<String,SignalEntry> SIGNAL_MAP;        // handle-PV name table

	/** map of transforms keyed by name */
    final private Map<String,ValueTransform> TRANSFORM_MAP;     // handle-value transform table
    
    
    /** Creates a new instance of SignalSuite */
    public SignalSuite() {
        SIGNAL_MAP = new HashMap<String,SignalEntry>();
        TRANSFORM_MAP = new HashMap<String,ValueTransform>();
    }
    
    
    /**
     * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
        final List<DataAdaptor> channelAdaptors = adaptor.childAdaptors( "channel" );
        for ( final DataAdaptor channelAdaptor : channelAdaptors  ) {
            final String handle = channelAdaptor.stringValue("handle");
			
			if ( !hasHandle( handle ) ) {
				SIGNAL_MAP.put( handle, new SignalEntry() );
			}
			final SignalEntry signalEntry = SIGNAL_MAP.get( handle );
			
            final String signal = channelAdaptor.stringValue( "signal" );
			if ( signal != null )  signalEntry.setSignal( signal );
			
			if ( channelAdaptor.hasAttribute( "settable" ) ) {
				final boolean settable = channelAdaptor.booleanValue( "settable" );
				signalEntry.setSettable( settable );
			}

			if ( channelAdaptor.hasAttribute( "valid" ) ) {
				final boolean valid = channelAdaptor.booleanValue( "valid" );
				signalEntry.setValid( valid );
			}

			if ( channelAdaptor.hasAttribute( "transform" ) ) {
				final String transformKey = channelAdaptor.stringValue( "transform" );
				signalEntry.setTransformKey( transformKey );
			}
        }
        
		final List<DataAdaptor> transformAdaptors = adaptor.childAdaptors( "transform" );
		for ( final DataAdaptor transformAdaptor : transformAdaptors ) {
            final String name = transformAdaptor.stringValue( "name" );
            final ValueTransform transform = TransformFactory.getTransform( transformAdaptor );
			putTransform( name, transform );
        }
    }
    
    
    /**
     * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		final Collection<Map.Entry<String,SignalEntry>> signalMapEntries = SIGNAL_MAP.entrySet();
		for ( final Map.Entry<String,SignalEntry> entry : signalMapEntries ) {
            final DataAdaptor channelAdaptor = adaptor.createChild("channel");
            final SignalEntry signalEntry = entry.getValue();
            
            channelAdaptor.setValue( "handle", entry.getKey() );
            channelAdaptor.setValue( "signal", signalEntry.signal() );
            channelAdaptor.setValue( "settable", signalEntry.settable() );
            channelAdaptor.setValue( "valid", signalEntry.isValid() );
			if ( signalEntry.getTransformKey() != null ) {
            	channelAdaptor.setValue( "transform", signalEntry.getTransformKey() );
			}
        }
    }

	
	/**
	 * Programmatically add or replace a signal entry corresponding to the specified handle
	 * @param handle The handle referring to the signal entry
	 * @param signal PV signal associated with the handle
	 * @param transformKey Key of the signal's transformation
	 * @param settable indicates whether the channel is settable
	 * @param valid specifies whether the channel is marked valid
	 */
	public void putChannel( final String handle, final String signal, final String transformKey, final boolean settable, final boolean valid ) {
		final SignalEntry signalEntry = new SignalEntry( signal, settable, transformKey );
		signalEntry.setValid( valid );
		SIGNAL_MAP.put( handle, signalEntry );
	}


	/** 
	 * Programmatically assign a transform for the specified name
	 * @param name key for associating the transform
	 * @param transform the value transform
	 */
	public void putTransform( final String name, final ValueTransform transform ) {
		TRANSFORM_MAP.put( name, transform );
	}


    /**
     * Check if this suite manages the specified PV signal.
     * @param signal The PV signal name for which to check.
     * @return true if this suite manages the specified signal and false otherwise.
     */
    boolean hasSignal( final String signal ) {
		for ( final SignalEntry entry : SIGNAL_MAP.values() ) {
            if ( entry.signal().equals( signal ) )  return true;
		}
        
        return false;
    }
    
    
    /** 
     * Get the PV signal associated with the handle.
     * @param handle The handle for which to get the associated PV signal.
     * @return The signal associated with the specified handle.
     */
    public String getSignal( final String handle ) {
        final SignalEntry signalEntry = getSignalEntry( handle );
		return signalEntry != null ? signalEntry.signal() : null;
    }
    
    
    /** 
     * Get all of the handles within this suite.
     * @return The handles managed by this suite.
     */
    public Collection<String> getHandles() {
        return SIGNAL_MAP.keySet();
    }
    
    
    /** 
     * Check if the signal suite manages the handle.
     * @param handle The handle for which to check availability.
     * @return true if the handle is available and false otherwise.
     */
    public boolean hasHandle( final String handle ) {
        return SIGNAL_MAP.containsKey( handle );
    }
    
    
    /**
     * Check if the signal entry associated with the specified handle has an 
     * associated value transform.
     * @param handle The handle to check for an associated transform.
     * @return true if the handle has an associated value transform and false otherwise.
     */
    public boolean hasTransform( final String handle ) {
        final SignalEntry signalEntry = getSignalEntry( handle );
		return signalEntry != null ? ( signalEntry.getTransformKey() != null ) : false;
    }
    
    
    /**
     * Get the transform associated with the specified handle.
     * @param handle The handle for which to get the transform.
     * @return The transform for the specified handle.
     */
    public ValueTransform getTransform(String handle) {
        final SignalEntry signalEntry = getSignalEntry( handle );
		return signalEntry != null ? TRANSFORM_MAP.get( signalEntry.getTransformKey() ) : null;
    }


    /**
     * Determine whether the handle's corresponding PV is valid.
     * @param handle The handle for which to get the validity.
     * @return validity state of the PV or false if there is no entry for the handle
     */
    public boolean isValid( final String handle ) {
        final SignalEntry signalEntry = getSignalEntry( handle );
		return signalEntry != null ? signalEntry.isValid() : false;
    }


    /**
     * Get the signal entry for the handle.
     * @param handle The handle for which to get the entry.
     * @return signal entry for the handle or null if there is none
     */
	private SignalEntry getSignalEntry( final String handle ) {
		return SIGNAL_MAP.get( handle );
	}
}



/** 
 * Entry in the signal map corresponding to a handle.  In the signal map
 * the key is a handle and the value is an instance of SignalEntry.
 */
class SignalEntry {
	private String _signal;			// the PV signal name
    private boolean _settable;		// whether the PV is settable
	private boolean _valid;			// whether the channel is marked valid
    private String _transformkey;   // Name of the transform if any
	
	
    /** Primary Constructor */
    public SignalEntry( final String signal, final boolean settable, final String transformKey ) {
        _signal = signal;
        _settable = settable;
        _transformkey = transformKey;
		_valid = true;
    }
	
	
    /** Constructor */
    public SignalEntry() {
		this( null, false, null );
    }
	

    /**
     * Get whether the PV is settable or not.
     * @return true if the PV is settable and false otherwise.
     */
    public boolean settable() {
        return _settable;
    }
	
	
	/** set the settable property */
	public void setSettable( final boolean isSettable ) {
		_settable = isSettable;
	}


	/** get the valid status of the PV */
	public boolean isValid() {
		return _valid;
	}


	/** mark the valid status of the PV */
	public void setValid( final boolean isValid ) {
		_valid = isValid;
	}


    /**
     * Get the PV signal name.
     * @return The PV signal name.
     */
    public String signal() {
        return _signal;
    }
	
	
	/** set the signal */
	public void setSignal( final String signal ) {
		_signal = signal;
	}


    /**
     * Get the name of the associated transform used
     * @return the name of the associated transform
     */
    public String getTransformKey() {
        return _transformkey;
    }
	
	
	/** set the transform key */
	public void setTransformKey( final String transformKey ) {
		_transformkey = transformKey;
	}
}
