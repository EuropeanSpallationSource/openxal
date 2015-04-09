package edu.stanford.lcls.modelmanager.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import edu.stanford.lcls.modelmanager.dbmodel.BrowserModel;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDeviceTableModel;

public class ModelDeviceView  implements SwingConstants{
	
	private JPanel modelDeviceView;
	protected BrowserModel _model;
	private JTable deviceTable;

	public ModelDeviceView(BrowserModel model) {
		_model = model;
		modelDeviceView = new JPanel(new BorderLayout());
		modelDeviceView.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
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
        			public void valueChanged(ListSelectionEvent event) {
						if ((!event.getValueIsAdjusting())
								& deviceTable.getSelectedRow() > -1) {
							deviceTable.setDefaultRenderer(Object.class,
									new DefaultTableCellRenderer() {
										private static final long serialVersionUID = 1L;
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
		modelDeviceView.add(tableBox, BorderLayout.CENTER);
	}
	
	public JTable getDataTable(){
		return deviceTable;
	}

	public Container getInstance() {
		return modelDeviceView;
	}

}
