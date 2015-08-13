package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class MachineModelDeviceTableModel extends AbstractTableModel implements	BrowserModelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final List<String> GUI_TABLE_COLUMN_NAME;
	static final protected int TABLE_SIZE = MachineModelDevice.getPropertySize();
	private boolean editable = false;
	private boolean showAdditionalParams = false;
	private String filterKeyword;
	
	protected MachineModelDevice[] _modelDevices = new MachineModelDevice[0];
	protected MachineModelDevice[] _shownModelDevices = new MachineModelDevice[0];
    

	public MachineModelDeviceTableModel() {
		GUI_TABLE_COLUMN_NAME = MachineModelDevice.getAllPropertyName();
	}

	public void setMachineModelDevice(MachineModelDevice[] modelDevices) {
		_modelDevices = modelDevices == null ? new MachineModelDevice[0] : modelDevices;
		refresh();
	}

	@Override
    public int getColumnCount() {
		return TABLE_SIZE;
	}

	@Override
    public int getRowCount() {
		return _shownModelDevices.length;
	}

	@Override
    public Object getValueAt(int rowIndex, int columnIndex) {
		MachineModelDevice modelDevice = _shownModelDevices[rowIndex];
		String type = MachineModelDevice.getPropertyType(columnIndex);
		if (modelDevice.getPropertyValue(columnIndex) != null) {
			if (type.equals("Long"))
				return Long.valueOf((String) modelDevice
						.getPropertyValue(columnIndex));
			else if (type.equals("Double"))
				return Double.valueOf((String)modelDevice.getPropertyValue(columnIndex));
			else
				return modelDevice.getPropertyValue(columnIndex);
		} else
			return null;
	}
	

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (editable && columnIndex == 2) {
			MachineModelDevice modelDevice = _shownModelDevices[rowIndex];
			Object prop = modelDevice.getPropertyValue("DEVICE_PROPERTY");
			return "B".equals(prop) || "P".equals(prop) || 	"A".equals(prop);
		}
		return false;
	}


	@Override

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		MachineModelDevice modelDevice = _shownModelDevices[rowIndex];
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

	@Override
    public String getColumnName(int columnIndex) {
		return GUI_TABLE_COLUMN_NAME.get(columnIndex);
	}

	
	@Override
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
    
    /**
     * Convenience method for refreshing devices view.
     */
    public void refresh() {
        if (showAdditionalParams && (filterKeyword == null || filterKeyword.length() < 1)) {
            _shownModelDevices = _modelDevices;
        } else {
            List<MachineModelDevice> shownDevices = new ArrayList<MachineModelDevice>();// TODO database doesen't have
                                                                                        // additional parameters
            for (MachineModelDevice device : _modelDevices) {
                if (!showAdditionalParams) {
                    switch ((String) device.getPropertyValue("DEVICE_PROPERTY")) {
                    case "Aperture size":
                    case "Misalignment x":
                    case "Misalignment y":
                    case "Misalignment z":
                    case "Misalignment yaw":
                    case "Misalignment pitch":
                    case "Misalignment roll":
                        break;
                    default:
                        if (filterKeyword == null
                                || filterKeyword.length() < 1
                                || device.getPropertyValue("ELEMENT_NAME").toString().toLowerCase()
                                        .startsWith(filterKeyword)) {
                            shownDevices.add(device);
                        } else {
                            if (filterKeyword == null
                                    || filterKeyword.length() < 1
                                    || device.getPropertyValue("ELEMENT_NAME").toString().toLowerCase()
                                            .startsWith(filterKeyword)) {
                                shownDevices.add(device);
                            }
                        }

                    }
                }
            }
            _shownModelDevices = shownDevices.toArray(new MachineModelDevice[shownDevices.size()]);
        }
        fireTableDataChanged();
    }

    /**
     * Method for switching boolean for showing additional parameters.
     */
    public void switchAdditionalParams(){
        showAdditionalParams = !showAdditionalParams;
    }
    
    /**
     * Method for receiving filter keyword.
     * @param keyword  with which we want to filter devices.
     */
    public void setFilter(String keyword){
        filterKeyword = keyword.toLowerCase();
    }
}