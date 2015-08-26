package edu.stanford.lcls.modelmanager.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import edu.stanford.lcls.modelmanager.dbmodel.BrowserModel;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDeviceTableModel;


/**
 * 
 * ModelDeviceView for showing list of all device parameters.
 * 
 * @version 1.0 18 Avg 2015
 * 
 * @author unknown
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class ModelDeviceView  implements SwingConstants{
	
	private JSplitPane modelDeviceView;
	protected BrowserModel _model;
	private JTable deviceTable;
	private final TableColumn initialValueColumn;

/**
 * Constructor. Creates view and configures table.
 * @param model
 */
	public ModelDeviceView(BrowserModel model) {
		_model = model;
        final MachineModelDeviceTableModel machineModelDeviceTableModel = new MachineModelDeviceTableModel(){
        	@Override
        	public void modelStateChanged(BrowserModel model, BrowserModelAction action) {
        		//Overriding modelStateChanged to show/hide initial values column
        		super.modelStateChanged(model, action);
        		if(isEditable()){//We show initial column
        			//We take all columns, that should be behind initial column, out and put them after it.
        			List<TableColumn> columns = new ArrayList <TableColumn>();
        			int i = deviceTable.getColumnCount() - 1;
        			//Taking columns out
        			while(i > 2){
        				TableColumn columnToReomve = deviceTable.getColumnModel().getColumn(i);
        				columns.add(columnToReomve);
        				deviceTable.removeColumn(columnToReomve);
        				i--;
        			}
        			deviceTable.addColumn(initialValueColumn);//Inserting initial values column
        			//Putting columns back in
        			while(!columns.isEmpty()){
        				deviceTable.addColumn(columns.remove((columns.size() - 1)));
        			}
        			
        		}else{//We hide initial column
        			deviceTable.removeColumn(initialValueColumn);
        		}
        	}
        };
        /*Filtering toolbar*/
	    final JPanel modelDevice= new JPanel(new GridLayout(1, 1));
	    modelDevice.setBorder(BorderFactory.createEmptyBorder(3,4,2,4));
	    final Box deviceOptons = new Box(HORIZONTAL);
	    /*Filter*/
	    deviceOptons.add(new JLabel("Filter: "));
	    final JTextField deviceFilter = new JTextField();
	    deviceFilter.getDocument().addDocumentListener(new DocumentListener() {
            
            @Override
            public void removeUpdate(DocumentEvent e) {callback();}
            
            @Override
            public void insertUpdate(DocumentEvent e) {callback();}
            
            @Override
            public void changedUpdate(DocumentEvent e) {callback();}
            
            private void callback(){
                String text = deviceFilter.getText();
                machineModelDeviceTableModel.setFilter(text);
                machineModelDeviceTableModel.refresh();
            }
        });
	    deviceFilter.setEditable(true);
	    deviceOptons.add(deviceFilter);
	    /*Checkbox for toggling additional parameters*/
	    final JCheckBox addidionalParametersCheckBox = new JCheckBox("Show additional parameters",false); 
        addidionalParametersCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                machineModelDeviceTableModel.switchAdditionalParams();
                machineModelDeviceTableModel.refresh();
            }
        });
        deviceOptons.add(addidionalParametersCheckBox);
        modelDevice.add(deviceOptons);
        
		/*Parameters list*/
		Box tableBox = new Box(BoxLayout.Y_AXIS);
		_model.addBrowserModelListener(machineModelDeviceTableModel);
		/*Configuring parameters table look*/
        deviceTable = new JTable(machineModelDeviceTableModel) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {//Manually setting renderer for enable parameter.
                if (column == 2 || (column == 3 && (machineModelDeviceTableModel.isEditable()))) {
                    if ("ENBL".equals(getModel().getValueAt(row, 1))) {
                        return getDefaultRenderer(Boolean.class);
                    }
                }
                return super.getCellRenderer(row, column);
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {//Manually setting editor for enable parameter.
                if (column == 2) {
                    if ("ENBL".equals(getModel().getValueAt(row, 1))) {
                        return getDefaultEditor(Boolean.class);
                    }
                }
                return super.getCellEditor(row, column);
            }
            
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {//Coloring manually changed parameters to blue for better overview. 
                Component comp = super.prepareRenderer(renderer, row, column);
                comp.setForeground(Color.BLACK);
                if(machineModelDeviceTableModel.isEditable()){//We only color if initial value is present.(It is hidden when cells aren't editable --> column 3 is something else)
                	if(column == 2 && !(getValueAt(row, 2).equals(getValueAt(row, 3)))){//if device value is different than initial value
                		comp.setForeground(Color.BLUE);
                	}
                }
                return comp;
            }
            
        };
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
								&& deviceTable.getSelectedRow() > -1) {
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
											    cell.setBackground(cell.getBackground());//CHECK: not sure if this actually does anything.
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
		initialValueColumn = deviceTable.getColumnModel().getColumn(3);
		deviceTable.getTableHeader().setReorderingAllowed(false);//We don't want user to change columns order (code is depending on correct order of columns)
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
