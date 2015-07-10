package edu.stanford.lcls.modelmanager.dbmodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import edu.stanford.lcls.modelmanager.view.ModelManagerFeature;
import edu.stanford.slac.Message.Message;

public class MachineModelDeviceTable {

	protected final String SCHEMA_NAME;
	protected final String TABLE_NAME;
	protected final List<String> DB_TABLE_COLUMN_NAME;
	static protected final String[] _TABLE_COLUMN_NAME = {"ELEMENT_NAME", "DEVICE_PROPERTY", "DEVICE_VALUE", "ZPOS"};
	static final private int TABLE_SIZE = _TABLE_COLUMN_NAME.length; // 54

	/** Constructor */
	public MachineModelDeviceTable(final DBTableConfiguration configuration) {
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

	protected MachineModelDevice[] fetchMachineModelDevices(final Connection connection, final Long id) throws SQLException {
		final List<MachineModelDevice> modelDevices = new ArrayList<MachineModelDevice>();
		try {
			// get data value
			
			/*final PreparedStatement queryStatement = connection.prepareStatement("SELECT E.\"ELEMENT_NAME\", D.\"DEVICE_PROPERTY\", D.\"DEVICE_VALUE\", E.\"ZPOS\" " +
					"FROM \"MACHINE_MODEL\".\"MODEL_DEVICES\" D, \"MACHINE_MODEL\".\"ELEMENT_MODELS\" E " +
					"WHERE D.\"RUNS_ID\" = ? AND " +
					"D.\"RUNS_ID\" = E.\"RUNS_ID\" AND " + // TODO OPENXAL outer joins
					"E.\"INDEX_SLICE_CHK\" = 0 AND " +
					"D.\"LCLS_ELEMENTS_ELEMENT_ID\" = E.\"LCLS_ELEMENTS_ELEMENT_ID\" " + // TODO OPENXAL Oracle's outer joins (+) " +
					"ORDER BY E.\"ORDINAL\", D.\"DEVICE_PROPERTY\"");*/
			final PreparedStatement queryStatement = connection.prepareStatement("SELECT D.\"ELEMENT_NAME\", D.\"DEVICE_PROPERTY\", D.\"DEVICE_VALUE\", D.\"ZPOS\" " +
					"FROM \"MACHINE_MODEL\".\"MODEL_DEVICES\" D " +
					"WHERE D.\"RUNS_ID\" = ? ");
			queryStatement.setLong(1, id);
			final ResultSet modelResult = queryStatement.executeQuery();
			MachineModelDevice machineModelDevice;
			while (modelResult.next()) {
				machineModelDevice = new MachineModelDevice();
				// add TableColume
				for (int i = 0; i < TABLE_SIZE; i++) {
					machineModelDevice.setPropertyValue(i, modelResult
							.getString(DB_TABLE_COLUMN_NAME.get(i)));
				}

				modelDevices.add(machineModelDevice);
			}
			queryStatement.close();
			modelResult.close();
		} catch (SQLException exception) {
			Message.error("SQLException: Cannot read saved model data from database.", true);			
			exception.printStackTrace();
		}
		// Sort by the "ORDINAL"
		return modelDevices
				.toArray(new MachineModelDevice[modelDevices.size()]);
	}
}


