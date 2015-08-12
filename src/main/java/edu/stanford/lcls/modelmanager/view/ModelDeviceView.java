package edu.stanford.lcls.modelmanager.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import edu.stanford.lcls.modelmanager.dbmodel.BrowserModel;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDeviceTableModel;

public class ModelDeviceView  implements SwingConstants{
	
	private JSplitPane modelDeviceView;
	protected BrowserModel _model;
	private JTable deviceTable;


	public ModelDeviceView(BrowserModel model) {
		_model = model;

	    JPanel modelDevice= new JPanel(new GridLayout(1, 1));
	    modelDevice.setBorder(BorderFactory.createEmptyBorder(3,4,2,4));
	    Box deviceOptons = new Box(HORIZONTAL);
	    deviceOptons.add(new JLabel("Filter: "));
	    final JTextField deviceFilter = new JTextField();                    //TODO write what happens on change
	    deviceFilter.setEditable(true);
	    deviceOptons.add(deviceFilter);      
	    final JCheckBox addidionalParametersCheckBox = new JCheckBox("Show additional parrameters",false); 
	    addidionalParametersCheckBox.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                final int change = e.getStateChange();
                if (change == ItemEvent.SELECTED){
                    //TODO write what happens on change
            }else{
                //TODO write what happens on change
            } 
           }
        });
//	    AddidionalParametersCheckBox.add(Box.createGlue());
	    deviceOptons.add(addidionalParametersCheckBox);      
	    modelDevice.add(deviceOptons);	    
		
		Box tableBox = new Box(BoxLayout.Y_AXIS);
		MachineModelDeviceTableModel machineModelDeviceTableModel = new MachineModelDeviceTableModel();
		_model.addBrowserModelListener(machineModelDeviceTableModel);
		deviceTable = new JTable(machineModelDeviceTableModel);
		//Sort by Table Head
		TableSorter sorter = new TableSorter();
        sorter.setTableHeader(deviceTable.getTableHeader());
        sorter.setTableModel(machineModelDeviceTableModel);
        deviceTable.setModel(sorter);
        
        deviceTable.getSelectionModel().addListSelectionListener(
        		new ListSelectionListener() {
        			@Override
                    public void valueChanged(ListSelectionEvent event) {
						if ((!event.getValueIsAdjusting())
								& deviceTable.getSelectedRow() > -1) {
							deviceTable.setDefaultRenderer(Object.class,
									new DefaultTableCellRenderer() {
										private static final long serialVersionUID = 1L;
										@Override
                                        public Component getTableCellRendererComponent(
												JTable table, Object value,
												boolean isSelected,
												boolean hasFocus, int row,
												int column) {
											Component cell = super
													.getTableCellRendererComponent(
															deviceTable, value,
															isSelected,
															hasFocus, row,
															column);
											if (row == deviceTable.getSelectedRow())
												cell.setBackground(cell.getBackground());
											else
												cell.setBackground(Color.WHITE);
											return cell;
										}
									});
							deviceTable.repaint();
						}
					}
				});
		
		tableBox.add(deviceTable.getTableHeader());
		tableBox.add(new JScrollPane(deviceTable));

        modelDeviceView = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                modelDevice, tableBox);
        modelDeviceView.setOneTouchExpandable(true);
	}
	
	public JTable getDataTable(){
		return deviceTable;
	}

	public Container getInstance() {
		return modelDeviceView;
	}

}












