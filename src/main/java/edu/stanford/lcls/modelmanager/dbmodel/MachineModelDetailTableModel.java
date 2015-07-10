package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class MachineModelDetailTableModel extends AbstractTableModel implements
		BrowserModelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final List<String> GUI_TABLE_COLUMN_NAME;
	static final protected int TABLE_SIZE = MachineModelDetail.getPropertySize();

	protected MachineModelDetail[] _modelDetails;

	public MachineModelDetailTableModel(MachineModelDetail[] modelDetails) {
		GUI_TABLE_COLUMN_NAME = MachineModelDetail.getAllPropertyName();
		_modelDetails = modelDetails;
	}

	public MachineModelDetailTableModel() {
		this(new MachineModelDetail[0]);
	}

	public void setMachineModelDetails(MachineModelDetail[] modelDetails) {
		_modelDetails = modelDetails;
		fireTableDataChanged();
	}

	public int getColumnCount() {
		return TABLE_SIZE;
	}

	public int getRowCount() {
		return _modelDetails.length;
	}
	

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		String type = MachineModelDetail.getPropertyType(columnIndex);
		if (type.equals("Long")) 
			return Long.class;
		else if (type.equals("Double"))
			return Double.class; 
		else 
			return String.class;
	}
	

	public Object getValueAt(int rowIndex, int columnIndex) {
		MachineModelDetail modelDetail = _modelDetails[rowIndex];
		String type = MachineModelDetail.getPropertyType(columnIndex);
		if (modelDetail.getPropertyValue(columnIndex) != null) {
			if (type.equals("Long")) 
				return Long.valueOf((String)modelDetail.getPropertyValue(columnIndex));
			else if (type.equals("Double"))
				return Double.valueOf((String)modelDetail.getPropertyValue(columnIndex)); 
			else 
				return (String)modelDetail.getPropertyValue(columnIndex);
		} else
			return "";
	}

	public String getColumnName(int columnIndex) {
		return GUI_TABLE_COLUMN_NAME.get(columnIndex);
	}

	public void connectionChanged(BrowserModel model) {
	}
	
	public void machineModelFetched(BrowserModel model,
			MachineModel[] fetchedMachineModel, MachineModel referenceMachineModel,
			MachineModelDetail[] referenceMachineModelDetail,
			MachineModelDevice[] referenceMachineModelDevice) {
		setMachineModelDetails(referenceMachineModelDetail);
	}

	public void modelSelected(BrowserModel model,
			MachineModel selectedMachineModel,
			MachineModelDetail[] selectedMachineModelDetail,
			MachineModelDevice[] selectedMachineModelDevice) {
		setMachineModelDetails(selectedMachineModelDetail);
	}
	
	public void runModel(BrowserModel model,
			MachineModel[] fetchedMachineModel,
			MachineModel runMachineModel,
			MachineModelDetail[] runMachineModelDetail,
			MachineModelDevice[] runMachineModelDevice){
		setMachineModelDetails(runMachineModelDetail);
	}
}
