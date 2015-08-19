package edu.stanford.lcls.modelmanager.dbmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;


/**
 * 
 * Machine model device table model for configuring table..
 * 
 * @version 1.0 18 Avg 2015
 * 
 * @author unknown
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class MachineModelDeviceTableModel extends AbstractTableModel implements	BrowserModelListener {

	private static final long serialVersionUID = 1L;
	protected final List<String> GUI_TABLE_COLUMN_NAME;
	static final protected int TABLE_SIZE = MachineModelDevice.getPropertySize();
	private boolean editable = false;
	private boolean showAdditionalParams = false;
	private String filterKeyword;
	public final static List<String> additionalParameters = Arrays.asList("APRX","MISX","MISY","MISZ","ROTX","ROTY","ROTZ","ENBL");
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
			    if("ENBL".equals((modelDevice.getPropertyValue(1))) && (columnIndex == 2 || columnIndex == 3 ))
			        return (Double.valueOf((String)modelDevice.getPropertyValue(columnIndex)) == 1);//Boolean
			    else
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
			return "B".equals(prop) || "P".equals(prop) || 	"A".equals(prop) || additionalParameters.contains(prop);
		}
		return false;
	}


	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		MachineModelDevice modelDevice = _shownModelDevices[rowIndex];
		if("ENBL".equals((modelDevice.getPropertyValue(1)))  &&  columnIndex == 2){
		    modelDevice.setPropertyValue(columnIndex, String.valueOf(((boolean) aValue) ? 1:0));
		}else{
            modelDevice.setPropertyValue(columnIndex, aValue.toString());
		}
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
            List<MachineModelDevice> shownDevices = new ArrayList<MachineModelDevice>();
            for (MachineModelDevice device : _modelDevices) {
                if (!showAdditionalParams) {
                    if (additionalParameters.contains(device.getPropertyValue("DEVICE_PROPERTY"))) {//if parameter is one of the additional we do not add it
                        continue;
                    }
                }
                if (filterKeyword != null && filterKeyword.length() > 0) {
                    if (!device.getPropertyValue("ELEMENT_NAME").toString().toLowerCase().startsWith(filterKeyword)) {
                        continue
                        ;
                    }
                }
                shownDevices.add(device);
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