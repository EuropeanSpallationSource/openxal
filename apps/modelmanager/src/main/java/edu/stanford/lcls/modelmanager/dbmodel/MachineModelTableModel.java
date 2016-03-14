package edu.stanford.lcls.modelmanager.dbmodel;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import edu.stanford.lcls.modelmanager.dbmodel.BrowserModelListener.BrowserModelAction;
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
	protected List<MachineModel> fetchedMachineModels = new ArrayList<>(0);
	private MachineModel referenceMachineModel;
	private MachineModel selectedMachineModel;
	private Thread thread1;
	
	public MachineModelTableModel(BrowserModel model) {	
		this.model = model;
		GUI_TABLE_COLUMN_NAME = MachineModel.getAllPropertyName();
	}

	public void setMachineModels(List<MachineModel> models) {
		fetchedMachineModels = models;
		fireTableDataChanged();
	}

	public MachineModel getMachineModelAt(final int row) {
		return fetchedMachineModels.get(row);
	}
	
	public int getColumnCount() {
		return TABLE_SIZE;
	}

	public int getRowCount() {
		return fetchedMachineModels.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex >= 5) columnIndex++;
		
		MachineModel modelDetail = fetchedMachineModels.get(rowIndex);
		
		if (columnIndex == 7) {
			return modelDetail == referenceMachineModel;
		} else if (columnIndex == 8) {
			return modelDetail == selectedMachineModel;
		}
		
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
		if (columnIndex >= 5) columnIndex++;
		
		if(columnIndex < 7)
			return GUI_TABLE_COLUMN_NAME.get(columnIndex);
		else if (columnIndex == 7)
			return "REF";
		else if (columnIndex == 8)
			return "SEL";
		return null;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex >= 5) columnIndex++;
		
		if (columnIndex > 6) return Boolean.class;
		
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
	
	public void setValueAt(Object value, int row, final int column) {
		if (column == 6){			
			referenceMachineModel = fetchedMachineModels.get(row);
		} else if (column == 7){
			if ((Boolean)value)
				selectedMachineModel = fetchedMachineModels.get(row);
			else
				selectedMachineModel = null;
		}
		thread1 = new Thread(new Runnable() {
			public void run() {
				ModelStateView.getProgressBar().setString("Loading ...");
				ModelStateView.getProgressBar().setIndeterminate(true);
				fireTableDataChanged();
				try {
					model.setSelectedMachineModel(referenceMachineModel, selectedMachineModel);
				} catch (SQLException e) {
					Message.error("SQLException: Cannot get selected model data.", true);			
					e.printStackTrace();
				}
			}
		});
		thread1.start();
	}
	
	@Override
	public void modelStateChanged(BrowserModel model, BrowserModelAction action){
		if (model.getStateReady()) {
			setMachineModels(model.getFetchedMachineModel());
			this.referenceMachineModel = model.getReferenceMachineModel();
			this.selectedMachineModel = model.getSelectedMachineModel();
		} else {
			setMachineModels(new ArrayList<MachineModel>(0));
			this.referenceMachineModel = null;
			this.selectedMachineModel = null;
		}
	}
}
