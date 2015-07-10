package edu.stanford.lcls.modelmanager.dbmodel;


import java.sql.SQLException;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import edu.stanford.lcls.modelmanager.view.ModelManagerFeature;
import edu.stanford.lcls.modelmanager.view.ModelStateView;
import edu.stanford.slac.Message.Message;

public class MachineModelTableModel extends AbstractTableModel implements
		BrowserModelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected BrowserModel model;
	protected final List<String> GUI_TABLE_COLUMN_NAME;
	static final protected int TABLE_SIZE = 8;
	protected MachineModel[] fetchedMachineModels;
	private MachineModel referenceMachineModel;
	private MachineModel selectedMachineModel;
	private Thread thread1;
	
	public MachineModelTableModel(MachineModel[] models) {
		GUI_TABLE_COLUMN_NAME = MachineModel.getAllPropertyName();
		fetchedMachineModels = models;
	}

	public MachineModelTableModel() {	
		this(new MachineModel[0]);
	}

	public void setMachineModels(MachineModel[] models) {
		fetchedMachineModels = models;
		fireTableDataChanged();
	}

	public MachineModel getMachineModelAt(final int row) {
		return fetchedMachineModels[row];
	}
	
	public MachineModel getMachineModelAt(final String machineModelID) {
		for(int i=0; i<fetchedMachineModels.length; i++){
			if(((String)fetchedMachineModels[i].getPropertyValue("ID")).equals(machineModelID)){
				return fetchedMachineModels[i];
			}
		}
		return null;
	}

	public int getColumnCount() {
		return TABLE_SIZE;
	}

	public int getRowCount() {
		return fetchedMachineModels.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex >= 5) columnIndex++;
		
		MachineModel modelDetail = fetchedMachineModels[rowIndex];
		String type = MachineModel.getPropertyType(columnIndex);
		
		if (modelDetail.getPropertyValue(columnIndex) != null) {
			if (type.equals("Long")) 
				return Long.valueOf((String)modelDetail.getPropertyValue(columnIndex));
			else if (type.equals("Double"))
				return Double.valueOf((String)modelDetail.getPropertyValue(columnIndex)); 
			else if (type.equals("Boolean"))
				return (Boolean)modelDetail.getPropertyValue(columnIndex); 
			else  
				return (String)modelDetail.getPropertyValue(columnIndex);
		} else
			return "";
		
	}

	public String getColumnName(int columnIndex) {
		if(columnIndex < 5)
			return GUI_TABLE_COLUMN_NAME.get(columnIndex);
		else
			return GUI_TABLE_COLUMN_NAME.get(columnIndex + 1);
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex >= 5) columnIndex++;
		
		String type = MachineModel.getPropertyType(columnIndex);
		if (type.equals("Long")) 
			return Long.class;
		else if (type.equals("Double"))
			return Double.class; 
		else if (type.equals("Boolean"))
			return Boolean.class;
		else
			return String.class;
	}
	
	public boolean isCellEditable(int row, int column) {
		if (column >= 6)
			return true;
		else
			return false;
	}
	
	public void setValueAt(Object value, int row, int column) {
		if (column == 6){
			for (int i=0;i<fetchedMachineModels.length;i++){
				fetchedMachineModels[i].setPropertyValue("REF", false);
			}
			fetchedMachineModels[row].setPropertyValue("REF", true);			
			referenceMachineModel = fetchedMachineModels[row];
		} else if (column == 7){
			fetchedMachineModels[row].setPropertyValue("SEL", value);
			if (selectedMachineModel != null){
				for (int i=0;i<fetchedMachineModels.length;i++){
					if (fetchedMachineModels[i].getPropertyValue("ID").toString()
							.equals(selectedMachineModel.getPropertyValue("ID").toString())){
						fetchedMachineModels[i].setPropertyValue("SEL", false);
						break;
						}
					}
			}
			if ((Boolean)fetchedMachineModels[row].getPropertyValue("SEL"))
				selectedMachineModel = fetchedMachineModels[row];
			else
				selectedMachineModel = null;
		}
		thread1 = new Thread(new Runnable() {
			public void run() {
				ModelStateView.getProgressBar().setString("Loading ...");
				ModelStateView.getProgressBar().setIndeterminate(true);
				fireTableDataChanged();
				try {
					model.setSelectedMachineModel(fetchedMachineModels, referenceMachineModel, selectedMachineModel);
				} catch (SQLException e) {
					Message.error("SQLException: Cannot get selected model data.", true);			
					e.printStackTrace();
				}
			}
		});
		thread1.start();
	}

	public void connectionChanged(BrowserModel model) {
		this.model = model;
	}
	
	public void machineModelFetched(BrowserModel model,
			MachineModel[] fetchedMachineModel, MachineModel referenceMachineModel,
			MachineModelDetail[] referenceMachineModelDetail,
			MachineModelDevice[] referenceMachineModelDevice){
		setMachineModels(fetchedMachineModel);
		this.referenceMachineModel = referenceMachineModel;
		this.selectedMachineModel = null;
	}

	public void modelSelected(BrowserModel model,
			MachineModel selectedMachineModel,
			MachineModelDetail[] selectedMachineModelDetail,
			MachineModelDevice[] selectedMachineModelDevice) {
		this.selectedMachineModel = selectedMachineModel;
	}
	
	public void runModel(BrowserModel model,
			MachineModel[] fetchedMachineModel,
			MachineModel runMachineModel,
			MachineModelDetail[] runMachineModelDetail,
			MachineModelDevice[] runMachineModelDevice){
		this.selectedMachineModel = runMachineModel;
		setMachineModels(fetchedMachineModel);
	}

	@Override
	public void editMachineParameters(BrowserModel browserModel,
			MachineModelDevice[] _selectedMachineModelDevice) {
	}
}
