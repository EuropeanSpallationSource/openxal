package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class MachineModelDeviceTableModel extends AbstractTableModel implements
		BrowserModelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final List<String> GUI_TABLE_COLUMN_NAME;
	static final protected int TABLE_SIZE = MachineModelDevice.getPropertySize();

	protected MachineModelDevice[] _modelDevices;
	private boolean editable;

	public MachineModelDeviceTableModel(MachineModelDevice[] modelDevices) {
		GUI_TABLE_COLUMN_NAME = MachineModelDevice.getAllPropertyName();
		_modelDevices = modelDevices;
	}

	public MachineModelDeviceTableModel() {
		this(new MachineModelDevice[0]);
	}

	public void setMachineModelDevice(MachineModelDevice[] modelDevices) {
		_modelDevices = modelDevices;
		fireTableDataChanged();
	}

	public int getColumnCount() {
		return TABLE_SIZE;
	}

	public int getRowCount() {
		return _modelDevices.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		MachineModelDevice modelDevice = _modelDevices[rowIndex];
		String type = MachineModelDevice.getPropertyType(columnIndex);
		if (modelDevice.getPropertyValue(columnIndex) != null) {
			if (type.equals("Long"))
				return Long.valueOf((String) modelDevice
						.getPropertyValue(columnIndex));
			else if (type.equals("Double"))
				return Double.valueOf((String)modelDevice.getPropertyValue(columnIndex));
			else
				return (String) modelDevice.getPropertyValue(columnIndex);
		} else
			return null;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		String type = MachineModelDevice.getPropertyType(columnIndex);
		if (type.equals("Long")) 
			return Long.class;
		else if (type.equals("Double"))
			return Double.class; 
		else 
			return String.class;
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
		setMachineModelDevice(referenceMachineModelDevice);
		editable = false;
	}

	public void modelSelected(BrowserModel model,
			MachineModel selectedMachineModel,
			MachineModelDetail[] selectedMachineModelDetail,
			MachineModelDevice[] selectedMachineModelDevice) {
		setMachineModelDevice(selectedMachineModelDevice);
		editable = false;
	}
	
	public void runModel(BrowserModel model,
			MachineModel[] fetchedMachineModel,
			MachineModel runMachineModel,
			MachineModelDetail[] runMachineModelDetail,
			MachineModelDevice[] runMachineModelDevice){
		setMachineModelDevice(runMachineModelDevice);
		editable = false;
	}

	@Override
	public void editMachineParameters(BrowserModel browserModel,
			MachineModelDevice[] _selectedMachineModelDevice) {
		setMachineModelDevice(_selectedMachineModelDevice);
		editable = true;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return editable && (columnIndex == 2);
	}

}