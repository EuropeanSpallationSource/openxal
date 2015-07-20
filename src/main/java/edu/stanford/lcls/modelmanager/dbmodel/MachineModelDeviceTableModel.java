package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import edu.stanford.lcls.modelmanager.dbmodel.BrowserModelListener.BrowserModelAction;

public class MachineModelDeviceTableModel extends AbstractTableModel implements	BrowserModelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final List<String> GUI_TABLE_COLUMN_NAME;
	static final protected int TABLE_SIZE = MachineModelDevice.getPropertySize();
	private boolean editable = false;
	
	protected MachineModelDevice[] _modelDevices = new MachineModelDevice[0];

	public MachineModelDeviceTableModel() {
		GUI_TABLE_COLUMN_NAME = MachineModelDevice.getAllPropertyName();
	}

	public void setMachineModelDevice(MachineModelDevice[] modelDevices) {
		_modelDevices = modelDevices == null ? new MachineModelDevice[0] : modelDevices;
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
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (editable && columnIndex == 2) {
			MachineModelDevice modelDevice = _modelDevices[rowIndex];
			Object prop = modelDevice.getPropertyValue("DEVICE_PROPERTY");
			return "B".equals(prop) || "P".equals(prop) || 	"A".equals(prop);
		}
		return false;
	}


	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		MachineModelDevice modelDevice = _modelDevices[rowIndex];
		modelDevice.setPropertyValue(columnIndex, aValue.toString());
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

	
	public void modelStateChanged(BrowserModel model, BrowserModelAction action) {
		editable = false;
		if (model.getStateReady()) {
			MachineModelDevice[] machineModelDevices = model.getSelectedMachineModelDevice();
			if (machineModelDevices != null) {
				if (model.getSelectedMachineModel() == model.getRunMachineModel() && model.getRunState().equals(BrowserModel.RunState.FETCHED_DATA)) {
					editable = true;
				}
			} else {
				machineModelDevices = model.getReferenceMachineModelDevice();
			}
			setMachineModelDevice(machineModelDevices);
		
		} else {
			setMachineModelDevice(new MachineModelDevice[0]);
		}
	}
	
	
}