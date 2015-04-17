package edu.stanford.lcls.modelmanager.dbmodel;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import static edu.stanford.lcls.modelmanager.dbmodel.DataManager.escape;

public class MachineModelTable {
	/** database table name */
	protected final String SCHEMA_NAME;
	protected final String TABLE_NAME;
	protected final List<String> DB_TABLE_COLUMN_NAME;
	static protected final String[] _TABLE_COLUMN_NAME = { "id",
			"run_element_date", "run_source_chk","model_modes_id",  "comments", "date_created"};
	static final private int TABLE_SIZE = 6;

	/** Constructor */
	public MachineModelTable(final DBTableConfiguration configuration) {
		TABLE_NAME = configuration.getTableName();
		SCHEMA_NAME = configuration.getSchemaName();
		DB_TABLE_COLUMN_NAME = new ArrayList<String>(TABLE_SIZE);
		for (int i = 0; i < TABLE_SIZE; i++) {
			DB_TABLE_COLUMN_NAME.add(i, configuration
					.getColumn(_TABLE_COLUMN_NAME[i]));
		}
	}

	/**
	 * Fetch the machine models within the specified time range.
	 */

	protected MachineModel[] fetchMachineModelsInRange(
			final Connection connection, final java.util.Date startTime,
			final java.util.Date endTime) throws SQLException {

		// get data type
		fetchMachineModelsDataType(connection);
		
		List<MachineModel> models = new ArrayList<MachineModel>();
		PreparedStatement queryStatement = getQueryByTimerangeStatement(connection);
		queryStatement.setTimestamp(1, new Timestamp(startTime.getTime()));
		queryStatement.setTimestamp(2, new Timestamp(endTime.getTime()));
		ResultSet modelResult = queryStatement.executeQuery();
		MachineModel machineModel;
		while (modelResult.next()) {
			machineModel = new MachineModel();
			for (int i = 0; i < TABLE_SIZE; i++) {
				machineModel.addPropertyValue(i, modelResult
						.getString(DB_TABLE_COLUMN_NAME.get(i)));
			}
			machineModel.addPropertyValue(6, "");
			machineModel.addPropertyValue(7, false);
			machineModel.addPropertyValue(8, false);
			models.add(machineModel);
		}
		queryStatement.close();
		modelResult.close();
		models = setGold(connection, models);
		return models.toArray(new MachineModel[models.size()]);
	}
	
	/**
	 * Fetch all the machine models.
	 */

	protected MachineModel[] fetchAllMachineModels(final Connection connection) throws SQLException {

		// get data type
		fetchMachineModelsDataType(connection);

		// get data value
		List<MachineModel> models = new ArrayList<MachineModel>();
		PreparedStatement queryStatement = getQueryByAllTimeStatement(connection);
		ResultSet modelResult = queryStatement.executeQuery();
		MachineModel machineModel;
		while (modelResult.next()) {
			machineModel = new MachineModel();
			for (int i = 0; i < TABLE_SIZE; i++) {
				machineModel.addPropertyValue(i, modelResult
						.getString(DB_TABLE_COLUMN_NAME.get(i)));
			}
			machineModel.addPropertyValue(6, "");
			machineModel.addPropertyValue(7, false);
			machineModel.addPropertyValue(8, false);
			models.add(machineModel);
		}
		queryStatement.close();
		modelResult.close();
		models = setGold(connection, models);
		return models.toArray(new MachineModel[models.size()]);
	}
	
	protected void fetchMachineModelsDataType(final Connection connection) throws SQLException {
		// get data type
		DatabaseMetaData metaData = connection.getMetaData();
		ResultSet typeResult = metaData.getColumns(null, SCHEMA_NAME,
				TABLE_NAME, null);
		ArrayList<String> columnName = new ArrayList<String>();
		ArrayList<String> columnType = new ArrayList<String>();
		ArrayList<Integer> columnSize = new ArrayList<Integer>();
		while (typeResult.next()) {
			columnName.add(typeResult.getString("COLUMN_NAME"));
			columnType.add(typeResult.getString("TYPE_NAME"));
			columnSize.add(typeResult.getInt("COLUMN_SIZE"));
		}
		for (int i = 0; i < TABLE_SIZE; i++) {
			int index = columnName.indexOf(MachineModel.getPropertyName(i));
			MachineModel.addPropertyType(i, columnType.get(index), columnSize
					.get(index).intValue());
		}
		MachineModel.addPropertyType(6, "VARCHAR2", 10);
		MachineModel.addPropertyType(7, "Boolean", 1);
		MachineModel.addPropertyType(8, "Boolean", 1);
		typeResult.close();
	}
	
	protected List<MachineModel> setGold (final Connection connection, List<MachineModel> models)throws SQLException{
		PreparedStatement queryStatement = connection.prepareStatement("SELECT " + escape("RUN_ID")+", "+escape("GOLD_STATUS_NO_CSS")+" FROM " + escape("MACHINE_MODEL") + "." + escape("V_GOLD_REPORT") );
		ResultSet modelResult = queryStatement.executeQuery();
		while (modelResult.next()) {
			for (int i=0; i<models.size(); i++){
				if(models.get(i).getPropertyValue("ID").equals(modelResult.getString("RUN_ID"))){
					if(modelResult.getString("GOLD_STATUS_NO_CSS") != null && 
							modelResult.getString("GOLD_STATUS_NO_CSS").equals("PRESENT"))
						models.get(i).setPropertyValue("GOLD", "PRESENT");
					else if(!models.get(i).getPropertyValue("GOLD").equals("PRESENT"))
						models.get(i).setPropertyValue("GOLD", "PREVIOUS");
					break;
				}
			}
		}
		return models;
	}

	protected PreparedStatement getQueryByTimerangeStatement(
			final Connection connection) throws SQLException {
		return connection.prepareStatement("SELECT * FROM " + escape(SCHEMA_NAME) + "."
				+ escape(TABLE_NAME) + " WHERE " + escape(DB_TABLE_COLUMN_NAME.get(1))
				+ " > ? AND " + escape(DB_TABLE_COLUMN_NAME.get(1)) + " < ?");
	}
	

	protected PreparedStatement getQueryByAllTimeStatement(
			final Connection connection) throws SQLException {
		return connection.prepareStatement("SELECT * FROM " + escape(SCHEMA_NAME) + "."
				+ escape(TABLE_NAME) );
	}
}
