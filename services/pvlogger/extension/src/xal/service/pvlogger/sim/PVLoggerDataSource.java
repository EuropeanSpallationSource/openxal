/*
 * @(#)PVLoggerDataSource.java          0.0 01/03/2005
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */
package xal.service.pvlogger.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xal.ca.Channel;
import xal.service.pvlogger.ChannelSnapshot;
import xal.service.pvlogger.MachineSnapshot;
import xal.service.pvlogger.PVLogger;
import xal.service.pvlogger.PvLoggerException;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Bend;
import xal.smf.impl.CurrentMonitor;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.MagnetTrimSupply;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.TrimmedQuadrupole;
import xal.smf.impl.VDipoleCorr;
import xal.smf.impl.qualify.AndTypeQualifier;
import xal.smf.impl.qualify.KindQualifier;
import xal.smf.impl.qualify.OrTypeQualifier;
import xal.smf.impl.qualify.TypeQualifier;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.tools.ArrayValue;
import xal.tools.database.ConnectionDictionary;
import xal.tools.database.ConnectionPreferenceController;
import xal.tools.transforms.ValueTransform;


/**
 * This class provides an interface for online model with PV logger data source.
 *
 * @version 0.2 1 Oct 2015
 * @author Paul Chu
 * @author Blaz Kranjc
 * 
 * TODO Things with hardcoded PV names should be redesigned
 */
public class PVLoggerDataSource {
	/** PV Logger */
	final private PVLogger PV_LOGGER;
    
	private Map<String,ChannelSnapshot> SNAPSHOT_MAP;

	private ChannelSnapshot[] CHANNEL_SNAPSHOTS;
	
	/** magnet values keyed by PV */
	private Map<String,Double> _magnetFields;
	
	/** magnet power supply values keyed by PV */
	private Map<String,Double> _magnetPowerSupplyValues;
	
	/** accelerator sequence */
	private AcceleratorSeq _sequence;
	
	/** indicates whether bend fields from the PV Logger are used in the scenario */
	private boolean _usesLoggedBendFields;
    
    
    /** Primary Constructor
     * @param id the PV Logger ID
     * @param theLogger existing, connected PV Logger to use
     */
    public PVLoggerDataSource( final long id, final PVLogger theLogger ) {
		_usesLoggedBendFields = false;
		
        if ( theLogger != null ) {
            PV_LOGGER = theLogger;
        }
        else {
            // initialize PVLogger
            ConnectionDictionary dict = PVLogger.newBrowsingConnectionDictionary();
            
            if (dict != null) {
                PV_LOGGER = new PVLogger( dict );
            } 
            else {
                ConnectionPreferenceController.displayPathPreferenceSelector();
                dict = PVLogger.newBrowsingConnectionDictionary();
                PV_LOGGER = new PVLogger( dict );
            }
        }
        
        updatePVLoggerId( id );
    }
    
    
	/**
	 * Constructor
	 * @param id the PV logger ID
	 */
	public PVLoggerDataSource( final long id ) {
        this( id, null );
	}
	
	
	/** Determine whether logged bend fields are applied in the scenario */
	public boolean getUsesLoggedBendFields() {
		return _usesLoggedBendFields;
	}
	
	
	/** Sets whether to use the logged bend fields in the scenario */
	public void setUsesLoggedBendFields( final boolean useLoggedBends ) {
		_usesLoggedBendFields = useLoggedBends;
	}
	
	
	/** close the PV Logger connection */
	public void closeConnection() {
		try {
			PV_LOGGER.closeConnection();
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}


	/**
	 * Update this data source with the data from the specified PV Logger snapshot
	 * @param id the PV logger ID
	 */
	public void updatePVLoggerId( final long id ) {
		try {
			final MachineSnapshot machineSnapshot = PV_LOGGER.fetchMachineSnapshot( id );
			CHANNEL_SNAPSHOTS = machineSnapshot.getChannelSnapshots();
			SNAPSHOT_MAP = populateChannelSnapshotTable();
			_magnetFields = getMagnetMap();
			_magnetPowerSupplyValues = getMagnetPSMap();
		}
		catch ( Exception exception ) {
			throw new RuntimeException( exception );
		}
	}

	/** populate the channel snapshot table */
	protected Map<String,ChannelSnapshot> populateChannelSnapshotTable() {
		final Map<String,ChannelSnapshot> snapshotMap = new HashMap<String,ChannelSnapshot>( CHANNEL_SNAPSHOTS.length );
		for ( final ChannelSnapshot channelSnapshot : CHANNEL_SNAPSHOTS ) {
			snapshotMap.put( channelSnapshot.getPV(), channelSnapshot );
		}
		return snapshotMap;
	}


	/** get a channel snapshot for the specified PV */
	public ChannelSnapshot getChannelSnapshot( final String pv ) {
		return SNAPSHOT_MAP.get( pv );
	}


	/** get the value for the channel snapshot corresponding to the specified PV */
	public double[] getChannelSnapshotValue( final String pv ) {
		final ChannelSnapshot snapshot = getChannelSnapshot( pv );
		return snapshot != null ? snapshot.getValue() : null;
	}
	
	
	/** Get the value map for magnets */
	public Map<String, Double> getMagnetMap() {
		final Map<String, Double> pvMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			final String snapshotPV = CHANNEL_SNAPSHOTS[i].getPV();
			// CHECK: This also matches all the power supplies
			if ( CHANNEL_SNAPSHOTS[i].getPV().contains( "Mag:" ) ) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				pvMap.put( snapshotPV, val[0] );
			}
		}

		return pvMap;
	}
	
	
	/** Get the value map for magnet power supplies */
	public Map<String, Double> getMagnetPSMap() {
		final Map<String, Double> pvMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().contains("Mag:PS_Q")) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				pvMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return pvMap;
	}

	
	/** Get the value map for horizontal BPM signals */
	public Map<String, Double> getBPMXMap() {
		final Map<String, Double> bpmXMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().contains(":xAvg")) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmXMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmXMap;
	}

	
	/** Get the value map for vertical BPM signals */
	public Map<String, Double> getBPMYMap() {
		Map<String, Double> bpmYMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().contains(":yAvg")) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmYMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmYMap;
	}

	
	/** Get the value map for BPM amplitude */
	public Map<String, Double> getBPMAmpMap() {
		final Map<String, Double> bpmYMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().contains(":amplitudeAvg")) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmYMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmYMap;
	}
	
	
	/** Get the value map for BPM phase */
	public Map<String, Double> getBPMPhaseMap() {
		final Map<String, Double> bpmYMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().contains(":phaseAvg")) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmYMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmYMap;
	}
	
	
	/**
	 * Get all magnets that are in the specified sequence
	 * TODO Misnamed method, returns all the magnets in the sequence not just the logged ones.
	 * @param sequence Accelerator sequence to get magnets from
	 * @return List of magnets in the sequence
	 */
	private List<Electromagnet> getLoggedMagnets( final AcceleratorSeq sequence ) {
		// include quadrupoles, dipole correctors and optionally bends
		final OrTypeQualifier magnetQualifier = OrTypeQualifier.qualifierForKinds( Quadrupole.s_strType, HDipoleCorr.s_strType, VDipoleCorr.s_strType );
		if ( _usesLoggedBendFields )  magnetQualifier.or( Bend.s_strType );	// optionally include bends
		
		// filter magnets for those that are strictly electromagnets with good status
		final TypeQualifier electromagnetQualifier = AndTypeQualifier.qualifierWithQualifiers( magnetQualifier, new KindQualifier( Electromagnet.s_strType ) ).andStatus( true );
		final List<AcceleratorNode> magnetNodes = sequence.getNodesWithQualifier( electromagnetQualifier );
		final List<Electromagnet> magnets = new ArrayList<Electromagnet>( magnetNodes.size() );
		for ( final AcceleratorNode magnetNode : magnetNodes ) {
			magnets.add( (Electromagnet)magnetNode );
		}
		
		return magnets;
	}
	
	
	/** Remove this data source from the specified scenario */
	public void removeModelSourceFromScenario( final AcceleratorSeq sequence, final Scenario scenario ) {
		final List<Electromagnet> magnets = getLoggedMagnets( sequence );
		for ( final Electromagnet magnet : magnets ) {
			scenario.removeModelInput( magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD );
		}
		
		try {
			scenario.resync();
		} 
		catch ( SynchronizationException exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** 
	 * PV Logger logs raw values, but optics includes channel transforms that need to be applied. 
	 * @param rawValue raw channel value
	 * @return physical value
	 */
	static private double toPhysicalValue( final Channel channel, final double rawValue ) {
		final ValueTransform transform = channel.getValueTransform();
		return transform != null ? transform.convertFromRaw( ArrayValue.doubleStore( rawValue ) ).doubleValue() : rawValue;
	}
	
	
	/** 
	 * PV Logger logs raw values, but optics includes channel transforms that need to be applied and conversion to field (e.g. polarity scaling). 
	 * @param rawValue raw channel value
	 * @return field
	 */
	static private double toFieldFromRaw( final Electromagnet magnet, final Channel channel, final double rawValue ) {
		final double transformedValue = toPhysicalValue( channel, rawValue );
		return magnet.toFieldFromCA( transformedValue );
	}


	/**
	 * Get the magnet's field from the PV Logger Snapshot
	 * Throw an exception if the field is not found in the snapshot.
	 * @param magnet The magnet to check the snapshot for
	 * @return field of the magnet from the snapshot
	 * @throws PvLoggerException if the field for the magnet is not in the snapshot
	 */
	public double getLoggedField( final Electromagnet magnet ) throws PvLoggerException {
		double totalField = 0.0;

		// use field readback
		if ( magnet.useFieldReadback() ) {
			// System.out.println("Quad " + magnet.getId() + " use fieldReadback");
			final Channel channel = magnet.getChannel( Electromagnet.FIELD_RB_HANDLE );
			final String pvName = channel.channelName();
			if ( _magnetFields.containsKey( pvName ) ) {
				final double rawValue = _magnetFields.get( pvName );
				// take into account of proper transform
				totalField = toFieldFromRaw( magnet, channel, rawValue );
			}
			else {		// If there is no magnet field readback, use corresponding power supply field readback, instead.
				final Channel mainSupplyReadbackChannel = magnet.getMainSupply().getChannel( MagnetMainSupply.FIELD_RB_HANDLE );
				final String mainSupplyReadbackPV = mainSupplyReadbackChannel.channelName();
				if ( _magnetPowerSupplyValues.containsKey( mainSupplyReadbackPV ) ) {
					final double rawValue = _magnetPowerSupplyValues.get( mainSupplyReadbackPV );
					// take into account of proper transform
					totalField = toFieldFromRaw( magnet, mainSupplyReadbackChannel, rawValue );
				}
				else {		// if no power supply readback, use power supply fieldSet
					final Channel mainSupplySetpointChannel = magnet.getMainSupply().getChannel( MagnetMainSupply.FIELD_SET_HANDLE );
					final String mainSupplySetpointPV = mainSupplySetpointChannel.channelName();
					if ( _magnetPowerSupplyValues.containsKey( mainSupplySetpointPV ) ) {
						final double rawValue = _magnetPowerSupplyValues.get( mainSupplySetpointPV );
						// take into account of proper transform
						totalField = toFieldFromRaw( magnet, mainSupplySetpointChannel, rawValue );
					}
					else {
						System.out.println( "No logged field for " + magnet.getId() + " after trying: " + pvName + ", " + mainSupplyReadbackPV + ", " + mainSupplySetpointPV  );
						throw new PvLoggerException("No logged field for magnet " + magnet.getId());
					}
				}
			}
		}
		else {		// use field set, we need to handle magnets with trim power supplies here. However, if no readback, we have to use field readback
			// for main power supply
			final Channel chan = magnet.getMainSupply().getChannel( MagnetMainSupply.FIELD_SET_HANDLE );
			final String fieldSetPV = chan.channelName();
			if ( _magnetFields.containsKey( fieldSetPV ) ) {
				final double rawValue = _magnetFields.get( fieldSetPV );
				// take into account of proper transform
				totalField = toFieldFromRaw( magnet, chan, rawValue );
				// for trim power supply (check if it has trim first)
				if ( magnet instanceof TrimmedQuadrupole ) {
					final Channel trimFieldChannel = ((TrimmedQuadrupole) magnet).getTrimSupply().getChannel( MagnetTrimSupply.FIELD_SET_HANDLE );
					final String trimFieldPV = trimFieldChannel.channelName();
					if ( _magnetFields.containsKey( trimFieldPV ) ) {
						final double trimVal = _magnetFields.get( trimFieldPV );
						// take into account of proper transform
						final double trimField = toFieldFromRaw( magnet, trimFieldChannel, trimVal );

						// todo: this logic needs to move to the TrimmedQuadrupole class
						// handle shunt PS differently
						if ( trimFieldPV.contains( "ShntC" ) ) {
							final double shuntField = Math.abs( trimField );	// shunt is unipolar
							// shunt always opposes the main field
							totalField = totalField * trimField > 0 ? totalField - shuntField : totalField + shuntField;
						}
						else {
							totalField += trimField;
						}
					}
				}
			}
			// use readback, if no field settings
			else {
				final Channel readbackChannel = magnet.getChannel( Electromagnet.FIELD_RB_HANDLE );
				final String fieldReadbackPV = readbackChannel.channelName();
				if ( _magnetFields.containsKey( fieldReadbackPV ) ) {
					final double rawValue = _magnetFields.get( fieldReadbackPV );
					totalField = toFieldFromRaw( magnet, readbackChannel, rawValue );
				}
				else {
					throw new PvLoggerException("No logged field for magnet " + magnet.getId());
				}
			}
		}

		return totalField;
	}


	/**
	 * set the model lattice with PV logger data source
	 * @param sequence accelerator sequence
	 * @param scenario Model Scenario object that will be changed
	 */
	public void setModelSource( final AcceleratorSeq sequence, final Scenario scenario ) {
		_sequence = sequence;
		
		final List<Electromagnet> magnets = getLoggedMagnets( sequence );
		for ( final Electromagnet magnet : magnets) {
			try {
				final double field = getLoggedField( magnet );
				scenario.setModelInput( magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD, field );
			} catch (PvLoggerException e) {
				continue;
			}
		}

		try {
			scenario.resync();
		} catch (SynchronizationException e) {
			System.out.println(e);
		}
	}

	public void setAccelSequence(AcceleratorSeq seq) {
		_sequence = seq;
	}

	/**
	 * get the beam current in mA, we use the first available BCM in the
	 * sequence. If the first in the sequence is not available, use MEBT BCM02.
	 * If it's also not available, then default to 20mA
	 *
	 * @return beam current
	 */
	public double getBeamCurrent() {
		double current = 20.;
		List<AcceleratorNode> bcms = _sequence.getAllNodesOfType("BCM");
		List<AcceleratorNode> allBCMs = AcceleratorSeq.filterNodesByStatus(bcms, true);

		if (_sequence.getAllNodesOfType("BCM").size() > 0) {
			String firstBCM = ((CurrentMonitor) allBCMs.get(0)).getId();
			for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
				if (CHANNEL_SNAPSHOTS[i].getPV().contains(firstBCM)
						&& CHANNEL_SNAPSHOTS[i].getPV().contains(":currentMax")) {
					current = CHANNEL_SNAPSHOTS[i].getValue()[0];
					return current;
				} else if (CHANNEL_SNAPSHOTS[i].getPV().equals("MEBT_Diag:BCM02:currentMax")) {
					current = CHANNEL_SNAPSHOTS[i].getValue()[0];
					return current;
				}
			}
		}

		return current;
	}

	/**
	 * get the beam current in mA, use the BCM specified here. If it's not
	 * available, use 20mA as default
	 *
	 * @param bcm
	 *            the BCM you want the beam current reading from
	 * @return beam current
	 */
	public double getBeamCurrent(String bcm) {
		double current = 20;
		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().contains(bcm)
					&& CHANNEL_SNAPSHOTS[i].getPV().contains(":currentMax")) {
				current = CHANNEL_SNAPSHOTS[i].getValue()[0];
				return current;
			}
		}
		return current;
	}

	/**
	 * get all the channel snapshots.
	 *
	 * @return channel snapshots in array
	 */
	public ChannelSnapshot[] getChannelSnapshots() {
		return CHANNEL_SNAPSHOTS;
	}
}
