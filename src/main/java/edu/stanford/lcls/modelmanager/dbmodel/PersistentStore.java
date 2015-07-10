package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.*;

import javax.swing.JFrame;

import xal.smf.AcceleratorSeq;
import xal.tools.data.DataAdaptor;

/** contains information about the persistent storage */
class PersistentStore {
	
	/** machine model table */
	final protected MachineModelTable MACHINE_MODEL_TABLE;
	final protected MachineModelDetailTable MACHINE_MODEL_DETAIL_TABLE;
	final protected MachineModelDeviceTable MACHINE_MODEL_DEVICE_TABLE;
	
	/** Constructor */
	public PersistentStore( final DataAdaptor storeAdaptor ) {
		final Map<String,DBTableConfiguration> tableConfigurations = loadTableConfigurations( storeAdaptor );
		MACHINE_MODEL_TABLE = new MachineModelTable(tableConfigurations.get( "MachineModel" ) );
		MACHINE_MODEL_DETAIL_TABLE =  new MachineModelDetailTable(tableConfigurations.get( "MachineModelDetail" ) );
		MACHINE_MODEL_DEVICE_TABLE =  new MachineModelDeviceTable(tableConfigurations.get( "MachineModelDevice" ) );
	}
	
	/** get the table configurations from the configuration */
	static private Map<String,DBTableConfiguration> loadTableConfigurations( final DataAdaptor storeAdaptor ) {
		final List<DataAdaptor> tableAdaptors = storeAdaptor.childAdaptors( "dbtable" );
		final Map<String,DBTableConfiguration> tableConfigurations = new HashMap<String,DBTableConfiguration>(2);
		for ( final DataAdaptor tableAdaptor : tableAdaptors ) {
			final String entity = tableAdaptor.stringValue( "entity" );
			tableConfigurations.put( entity, new DBTableConfiguration( tableAdaptor ) );
		}		
		return tableConfigurations;
	}	

	
	public MachineModel[] fetchMachineModelsInRange( final Connection connection, final java.util.Date startTime, final java.util.Date endTime ) throws SQLException {
		return MACHINE_MODEL_TABLE.fetchMachineModelsInRange( connection, startTime, endTime );
	}
	
	public MachineModel[] fetchAllMachineModels(final Connection connection) throws SQLException {
		return MACHINE_MODEL_TABLE.fetchAllMachineModels( connection );
	}

	public MachineModelDetail[] fetchMachineModelDetails(AcceleratorSeq acc, final Connection connection, final Long id) throws SQLException {
		return MACHINE_MODEL_DETAIL_TABLE.fetchMachineModelDetails(acc, connection, id);
	}

	public MachineModelDevice[] fetchMachineModelDevices(final Connection connection, final Long id) throws SQLException {
		return MACHINE_MODEL_DEVICE_TABLE.fetchMachineModelDevices(connection, id);
	}
}
