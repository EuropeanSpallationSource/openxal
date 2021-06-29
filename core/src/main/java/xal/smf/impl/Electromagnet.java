/*
 * Electromagnet.java
 *
 * Created on January 30, 2002, 2:00 PM
 */

package xal.smf.impl;

import xal.smf.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;
import xal.tools.data.*;

import java.util.*;


/**
 * Electromagnet is the base class representation of an electromagnet.
 *
 * @author  tap
 */
public abstract class Electromagnet extends Magnet {
    /** the node type */
    public static final String      TYPE   = "emag";
	
    // field readback handle
    public static final String FIELD_RB_HANDLE = "fieldRB";
    
    public final AccessibleProperty field = new AccessibleProperty("field", FIELD_RB_HANDLE, MagnetMainSupply.FIELD_SET_HANDLE , () -> getDesignField(), (channelValues) -> toFieldFromCA(channelValues[0]));
    
    // indicates whether to use the actual field readback or the field setting in the getField() method
    // by default use the field readback
    protected boolean useFieldReadback = true;
    
    /** the ID of this magnet's main power supply */
    protected String mainSupplyId;
        
    // static initializer
    static {
        registerType();
    }

    
    // Register types for qualification
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( Electromagnet.class, TYPE, "electromagnet" );
    }


	/** Primary Constructor */
	public Electromagnet( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
	}


    /** Constructor */
    public Electromagnet( final String strId ) {
        this( strId, null );
    }

    
    /** 
     * Update the node with data from the provided adaptor.
	 * @param adaptor The data provider
     */
    @Override
    public void update( final DataAdaptor adaptor ) throws NumberFormatException {
        super.update(adaptor);
        final DataAdaptor powerSupplyAdaptor = adaptor.childAdaptor( "ps" );
        if ( powerSupplyAdaptor != null ) {
            updatePowerSupplies( powerSupplyAdaptor );
        }
    }
    
    
    /**
     * Update data from the power supply data adaptor.  Get the main 
     * power supply information.
     * @param powerSupplyAdaptor The data provider of power supply information.
     */
    protected void updatePowerSupplies( final DataAdaptor powerSupplyAdaptor ) {
        mainSupplyId = powerSupplyAdaptor.stringValue( "main" );
    }
    
        
    /** 
     * Encode data from the node into the provided adaptor.  Overrides to provide
     * support for power supplies.
     * @param adaptor The data store
     */
    @Override
    public void write( final DataAdaptor adaptor ) {
        super.write(adaptor);
        DataAdaptor powerSupplyAdaptor = adaptor.createChild( "ps" );
        writePowerSupplies( powerSupplyAdaptor );
    }
    
    
    /**
     * Write data to the power supply data adaptor.  Put the information about
     * the main power supply into the data adaptor.
     * @param powerSupplyAdaptor The data sink for the power supply information
     */
    protected void writePowerSupplies( final DataAdaptor powerSupplyAdaptor ) {
        powerSupplyAdaptor.setValue( "main", mainSupplyId );
    }
	
	
	/**
	 * Set whether or not to use the field readback in the getField() method.
	 * @param useFieldReadback true to use the field readback and false to use the field setting.
	 */
        public void setUseFieldReadback(final boolean useFieldReadback) {
            this.useFieldReadback = useFieldReadback;
            if (useFieldReadback) {
                field.setSetHandle(MagnetMainSupply.FIELD_SET_HANDLE);
            } else {
                field.setSetHandle(FIELD_RB_HANDLE);
            }
        }

	
	/**
	 * Determines whether the field readback is used in the getField() method.
	 * @return true if the field readback is used in getField() and false if instead the field setting is used.
	 */
	public boolean useFieldReadback() {
		return useFieldReadback;
	}
    
    
    /** 
     * Get the channel handles.  Overrides the default method to add handles from
     * the main power supply.
     * @return The channel handles associated with this node
     */
    @Override
    public Collection<String> getHandles() {
        final Collection<String> handles = new HashSet<>( super.getHandles() );
		try {
			final MagnetMainSupply supply = getMainSupply();
			if ( supply != null ) {
				handles.addAll( getMainSupply().getChannelSuite().getHandles() );
			}
		}
		catch( NullPointerException exception ) {
			System.err.println("exception getting handles from the main supply \"" + getMainSupply() + "\" for electromagnet: " + getId());
			throw exception;
		}
        return handles;
    }
    
    // Overriding to not return handles from the power supply.
    @Override
    public Collection<String> getDefaultHandles() {
        List<String> defaultHandles = new ArrayList<>();

        defaultHandles.add(FIELD_RB_HANDLE);

        return defaultHandles;
    }

    /**
     * Get the readback handle corresponding to a set channel.
     *
     * @param setHandle The set handle. public String getReadbackHandle(String
     * setHandle) {
     * @return The corresponding readback handle.
     */
    @Override
    public String[] getReadbackHandles(String setHandle) {
        String[] readbackHandles = super.getReadbackHandles(setHandle);
        if (readbackHandles == null) {
            try {
                final MagnetMainSupply supply = getMainSupply();
                if (supply != null) {
                    readbackHandles = supply.getReadbackHandles(setHandle);
                }
            } catch (NullPointerException exception) {
                System.err.println("exception getting ReadbackHandle from the main supply \"" + getMainSupply() + "\" for electromagnet: " + getId());
                throw exception;
            }
        }
        return readbackHandles;
    }

    /**
     * Get the set handle corresponding to a readback channel.
     *
     * @param readbackHandle The readback handle.
     * @return The corresponding set handle.
     */
    @Override
    public String getSetHandle(String readbackHandle) {
        String setHandle = super.getSetHandle(readbackHandle);
        if (setHandle == null) {
            try {
                final MagnetMainSupply supply = getMainSupply();
                if (supply != null) {
                    setHandle = supply.getSetHandle(readbackHandle);
                }
            } catch (NullPointerException exception) {
                System.err.println("exception getting setHandle from the main supply \"" + getMainSupply() + "\" for electromagnet: " + getId());
                throw exception;
            }
        }
        return setHandle;
    }
    
    /**
     * Find the channel for the specified handle searching the main supply if necessary.
     * @param handle The handle for the channel to get.
     * @return The channel associated with this node and the specified handle or null if there is no match.
     */
    @Override
    public Channel findChannel( final String handle ) {
		final Channel channel = super.findChannel( handle );
		if ( channel != null ) {
			return channel;
		}
		else {
			final MagnetMainSupply supply = getMainSupply();
			return supply != null ? supply.findChannel( handle ) : null;
		}
    }
    
    
    /**
     * Get the main power supply for this magnet.
     * @return The main power supply for this magnet
     */
    public MagnetMainSupply getMainSupply() {
        Accelerator accelerator = getAccelerator();
        if (accelerator != null) {
            return getAccelerator().getMagnetMainSupply(mainSupplyId);
        } else {
            return null;
        }
    }

    /**
     * Set the main power supply for this magnet by id.
     * @param id Id of main power supply;
     */
    public void setMainSupplyId(String id) {
    	mainSupplyId = id;
    }
    
    @Override
    public List<AccessibleProperty> getAccessibleProperties() {
        List<AccessibleProperty> properties = new ArrayList<>();
        properties.addAll(getAccessibleProperties(null, this.getClass()));
        MagnetMainSupply supply = getMainSupply();
        if (supply != null) {
            properties.addAll(supply.getAccessibleProperties());
        }

        return properties;
    }
    
    /** 
     * Set the cycle enable state of the magnet.  If enabled, the magnet will 
     * be cycled when the field is set.
     * @param enable True to enable cycling; false to disable cycling.
     */
    public void setCycleEnable( final boolean enable ) throws ConnectionException, PutException {
        getMainSupply().setCycleEnable( enable );
    }
    
    
    /** 
     * Gets the cycle state of the magnet.  The magnet may be in one of three 
     * states: cycle is invalid (field changed in reverse direction of initial setting), 
     * cycling in progress or cycle is valid
     * @return One of CYCLE_INVALID, CYCLING or CYCLE_VALID
     */
    public int getCycleState() throws ConnectionException, GetException {
        return getMainSupply().getCycleState();
    }
    
    
    /** 
	 * Get the field in this electromagnet via ca.
	 * @return the field in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. 
	 */
    public double getField() throws ConnectionException, GetException {
        return ( useFieldReadback ) ? getFieldReadback() : getTotalFieldSetting();
    }
    
    
    /** 
	 * Get the field in this electromagnet via ca.
	 * @return the readback field in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. 
	 */
    public double getFieldReadback() throws ConnectionException, GetException {
        Channel fieldRBChannel = getAndConnectChannel( FIELD_RB_HANDLE );
        
        return toFieldFromCA( fieldRBChannel.getValDbl() );
    }
    

    /** Get the integrated field in this electromagnet 
        T-m/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. */
    public double getFieldInt() throws ConnectionException, GetException  {
        return getField() * getEffLength();
    }

    
    /** Set the main power supply field contribution in the magnet.  If cycle enable 
     * is true then the magnet is cycled before the field is set to the specified value.
     * @param newField is the new field level in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
     */  
    public void setField( final double newField ) throws ConnectionException, PutException {
        getMainSupply().setField( toCAFromField( newField ) );
    }
	
	
	/**
	 * Get the value to which the main power supply's field contribution is set.  Note that this is not the readback.
	 * @return the field setting in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 */
	public double getFieldSetting() throws ConnectionException, GetException {
        return toFieldFromCA( getMainSupply().getFieldSetting() );
	}
	
	
	/**
	 * Get the value to which the field is set including both the main supply and possible trim supply contributions.  
	 * Note that this is not the readback.
	 * @return the field setting in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 */
	public double getTotalFieldSetting() throws ConnectionException, GetException {
        return getFieldSetting();
	}
	
	
	/**
	 * Convert the raw channel access value to get the field.
	 * @param rawValue the raw channel value
	 * @return the magnetic field in T/m^(n-1)
	 */
	public final double toFieldFromCA( final double rawValue ) {
        return rawValue * getPolarity(); 
	}
	
	
	/**
	 * Convert the field value to a channel access value.
	 * @param field the magnetic field in T/m^(n-1)
	 * @return the channel access value
	 */
	public final double toCAFromField( final double field ) {
        return field * getPolarity(); 
	}
	

    /** Get the field upper settable limit of the main power supply
        in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
    */
    public double upperFieldLimit() throws ConnectionException, GetException {
		final MagnetMainSupply powerSupply = getMainSupply();
        return Math.max( toFieldFromCA( powerSupply.lowerFieldLimit() ), toFieldFromCA( powerSupply.upperFieldLimit() ) );
    }

    
    /** Get the field lower settable limit of the main power supply
       in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
    */  
    public double lowerFieldLimit() throws ConnectionException, GetException {
		final MagnetMainSupply powerSupply = getMainSupply();
        return Math.min( toFieldFromCA( powerSupply.lowerFieldLimit() ), toFieldFromCA( powerSupply.upperFieldLimit() ) );
    }
	
	
    /** Get the field upper settable limit of the main power supply in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. */
    public double upperDisplayFieldLimit() throws ConnectionException, GetException {
		final MagnetMainSupply powerSupply = getMainSupply();
        return Math.max( toFieldFromCA( powerSupply.lowerDisplayFieldLimit() ), toFieldFromCA( powerSupply.upperDisplayFieldLimit() ) );
    }
	
    
    /** Get the field lower settable limit of the main power supply in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. */
    public double lowerDisplayFieldLimit() throws ConnectionException, GetException {
		final MagnetMainSupply powerSupply = getMainSupply();
        return Math.min( toFieldFromCA( powerSupply.lowerDisplayFieldLimit() ), toFieldFromCA( powerSupply.upperDisplayFieldLimit() ) );
    }
	
	
    /** Get the field upper settable limit of the main power supply in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. */
    public double upperWarningFieldLimit() throws ConnectionException, GetException {
		final MagnetMainSupply powerSupply = getMainSupply();
        return Math.max( toFieldFromCA( powerSupply.lowerWarningFieldLimit() ), toFieldFromCA( powerSupply.upperWarningFieldLimit() ) );
    }
	
    
    /** Get the field lower settable limit of the main power supply in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. */
    public double lowerWarningFieldLimit() throws ConnectionException, GetException {
		final MagnetMainSupply powerSupply = getMainSupply();
        return Math.min( toFieldFromCA( powerSupply.lowerWarningFieldLimit() ), toFieldFromCA( powerSupply.upperWarningFieldLimit() ) );
    }
	
	
    /** Get the field upper settable limit of the main power supply in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. */
    public double upperAlarmFieldLimit() throws ConnectionException, GetException {
		final MagnetMainSupply powerSupply = getMainSupply();
        return Math.max( toFieldFromCA( powerSupply.lowerAlarmFieldLimit() ), toFieldFromCA( powerSupply.upperAlarmFieldLimit() ) );
    }
	
    
    /** Get the field lower settable limit of the main power supply in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. */
    public double lowerAlarmFieldLimit() throws ConnectionException, GetException {
		final MagnetMainSupply powerSupply = getMainSupply();
        return Math.min( toFieldFromCA( powerSupply.lowerAlarmFieldLimit() ), toFieldFromCA( powerSupply.upperAlarmFieldLimit() ) );
    }
    

    /** Get the main power supply current in this electromagnet via ca (A) */
    public double getCurrent() throws ConnectionException, GetException {
        return getMainSupply().getCurrent();
    }
    
    
    /** set the main power supply current in the magnet (A)
       @param newCurrent is the new current (A)
    */  
    public void setCurrent( final double newCurrent ) throws ConnectionException, PutException {
        getMainSupply().setCurrent( newCurrent );
    }
    

    /** get the main power supply current lower settable limit (A) */  
    public double upperCurrentLimit() throws ConnectionException, GetException {
        return getMainSupply().upperCurrentLimit();
    }
    

    /** get the main power supply current lower settable limit (A) */  
    public double lowerCurrentLimit() throws ConnectionException, GetException {
        return getMainSupply().lowerCurrentLimit();
    }
    
    
    /**
     * Since this is an electro-magnet we override the inherited method to 
     * advertise this characteristic.
     * @return false since all Electromagnet instances are not permanent magnets.
     */
    @Override
    public boolean isPermanent() {
        return false;
    }
}
