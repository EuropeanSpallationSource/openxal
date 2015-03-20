package edu.stanford.lcls.modelmanager.dbmodel;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

import edu.stanford.lcls.modelmanager.view.ModelManagerFeature;
import edu.stanford.slac.Message.Message;

public class MachineModelDetailTable {
	protected final String SCHEMA_NAME;
	protected final String TABLE_NAME;
	protected final List<String> DB_TABLE_COLUMN_NAME;
	static protected final String[] _TABLE_COLUMN_NAME = {"run_id", "element_name",
		"index_slice_chk", "zpos", "e", "alpha_x", "alpha_y", "beta_x",
		"beta_y", "psi_x", "psi_y", "eta_x", "eta_y", "etap_x", "etap_y",
		"r11", "r12", "r13", "r14", "r15", "r16", "r21", "r22", "r23",
		"r24", "r25", "r26", "r31", "r32", "r33", "r34", "r35", "r36",
		"r41", "r42", "r43", "r44", "r45", "r46", "r51", "r52", "r53",
		"r54", "r55", "r56", "r61", "r62", "r63", "r64", "r65", "r66",
		"leff", "sleff", "ordinal", "suml"};
	static final private int TABLE_SIZE = _TABLE_COLUMN_NAME.length; // 54

	/** Constructor */
	public MachineModelDetailTable(final DBTableConfiguration configuration) {
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

	protected MachineModelDetail[] fetchMachineModelDetails(JFrame parent,
			final Connection connection, final Long id) throws SQLException {
		final List<MachineModelDetail> modelDetails = new ArrayList<MachineModelDetail>();
		try {
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
				int index = columnName.indexOf(MachineModelDetail
						.getPropertyName(i));
				if(index < 0) index = 4; // "EK" to "E" mistake
				MachineModelDetail.addPropertyType(i, columnType.get(index),
						columnSize.get(index).intValue());
			}
			MachineModelDetail.addPropertyType(MachineModelDetail
					.getPropertySize() - 5, "VARCHAR2", 50); // DEVICE_TYPE
			MachineModelDetail.addPropertyType(MachineModelDetail
					.getPropertySize() - 4, "VARCHAR2", 50); // EPICS_NAME
			MachineModelDetail.addPropertyType(MachineModelDetail
					.getPropertySize() - 3, "NUMBER", 22); // P
			MachineModelDetail.addPropertyType(MachineModelDetail
					.getPropertySize() - 2, "NUMBER", 22); // Bmag_X
			MachineModelDetail.addPropertyType(MachineModelDetail
					.getPropertySize() - 1, "NUMBER", 22); // Bmag_Y
			typeResult.close();

			// get data value
			final PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM " + SCHEMA_NAME + "."
					+ TABLE_NAME + " WHERE RUNS_ID = ? ");
			queryStatement.setLong(1, id);
			final ResultSet modelResult = queryStatement.executeQuery();		
			DeviceType deviceType = new DeviceType(parent);
			MachineModelDetail machineModelDetail;
			Double E;
			Double P;
			
			while (modelResult.next()) {
				machineModelDetail = new MachineModelDetail();
				// add TableColume
				for (int i = 0; i < TABLE_SIZE; i++) {
					machineModelDetail.addPropertyValue(i, modelResult
							.getString(DB_TABLE_COLUMN_NAME.get(i)));
				}
				// add Device length
				if (machineModelDetail.getPropertyValue("SLEFF") == null) {
					machineModelDetail.addPropertyValue("SLEFF", deviceType
							.getDeviceLength(machineModelDetail.getPropertyValue(
									"ELEMENT_NAME").toString()));
				}
				// add Device Type
				machineModelDetail.addPropertyValue("DEVICE_TYPE", deviceType
						.getDeviceType(machineModelDetail.getPropertyValue(
								"ELEMENT_NAME").toString()));
				
				// add EPICS name
				machineModelDetail.addPropertyValue("EPICS_NAME", deviceType
						.getEPICSName(machineModelDetail.getPropertyValue(
								"ELEMENT_NAME").toString()));
				
				// add P
				E = Double.parseDouble(machineModelDetail.getPropertyValue("E").toString());
				P = Math.sqrt(1+2/(E/0.000511-1))*E;
				machineModelDetail.addPropertyValue("P", P.toString());
				
				// add Bmag_X & Bmag_Y
				machineModelDetail.addPropertyValue("Bmag_X", "1");
				machineModelDetail.addPropertyValue("Bmag_Y", "1");
				
				// If Device ORDINAL is null, set ZPOS to it
				if (machineModelDetail.getPropertyValue("ORDINAL") == null) {
					machineModelDetail.setPropertyValue("ORDINAL",
							machineModelDetail.getPropertyValue("ZPOS"));
				}
				
				modelDetails.add(machineModelDetail);
			}
			queryStatement.close();
			modelResult.close();
		} catch (SQLException exception) {
			Message.error("SQLException: Cannot read saved model data from database.", true);			
			exception.printStackTrace();
		}
		// Sort by the "ORDINAL"
		Collections.sort(modelDetails, new SortMachineModelDetail("ORDINAL", SortMachineModelDetail.UP));
		return modelDetails
				.toArray(new MachineModelDetail[modelDetails.size()]);
	}
}

