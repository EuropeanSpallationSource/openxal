package edu.stanford.lcls.modelmanager.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

import edu.stanford.lcls.modelmanager.dbmodel.*;

public class ModelDetailView implements SwingConstants {
	private JSplitPane modelDetailView;
	protected BrowserModel model;
	private JTable detailTable;

	public ModelDetailView(final JFrame parent, final BrowserModel model) {
		this.model = model;
		JPanel modelDetail = new JPanel(new GridLayout(2, 1));
		modelDetail.setBorder(BorderFactory.createEmptyBorder(3,4,2,4));
		Box modelTitle = new Box(HORIZONTAL);
		modelTitle.add(new JLabel("Model Info :"));
		modelTitle.add(Box.createHorizontalStrut(10));
		final JTextField modelName = new JTextField();
		modelName.setEditable(false);
		modelName.setBorder(null);
		modelTitle.add(modelName);
		modelTitle.add(Box.createHorizontalStrut(20));
		modelTitle.add(new JLabel("Created Date :"));
		modelTitle.add(Box.createHorizontalStrut(10));
		final JTextField modelCreatDate = new JTextField();
		modelCreatDate.setEditable(false);
		modelCreatDate.setBorder(null);
		modelTitle.add(modelCreatDate);
		modelTitle.add(Box.createGlue());
		modelDetail.add(modelTitle);
		Box modelComment = new Box(HORIZONTAL);
		modelComment.add(new JLabel("Comment :"));
		modelComment.add(Box.createHorizontalStrut(10));
		final JTextField commentText = new JTextField();
		commentText.setEditable(true);
		//commentText.setBorder(null);
		modelComment.add(commentText);
		modelComment.add(Box.createHorizontalStrut(10));
		JButton export2CSVButton = new JButton("Export CSV File");
		modelComment.add(export2CSVButton);
		export2CSVButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				model.exportDetailData(parent);
			}
		});
		modelDetail.add(modelComment);

		Box tableBox = new Box(BoxLayout.Y_AXIS);
		MachineModelDetailTableModel machineModelDetailTableModel = new MachineModelDetailTableModel();
		model.addBrowserModelListener(machineModelDetailTableModel);
		detailTable = new JTable(machineModelDetailTableModel);
		detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//Sort by Table Head
		TableSorter sorter = new TableSorter();
        sorter.setTableHeader(detailTable.getTableHeader());
        sorter.setTableModel(machineModelDetailTableModel);
        detailTable.setModel(sorter);
        
        detailTable.getSelectionModel().addListSelectionListener(
        		new ListSelectionListener() {
        			public void valueChanged(ListSelectionEvent event) {
						if ((!event.getValueIsAdjusting())
								& detailTable.getSelectedRow() > -1) {
							detailTable.setDefaultRenderer(Object.class,
									new DefaultTableCellRenderer() {
										private static final long serialVersionUID = 1L;
										public Component getTableCellRendererComponent(
												JTable table, Object value,
												boolean isSelected,
												boolean hasFocus, int row,
												int column) {
											Component cell = super
													.getTableCellRendererComponent(
															detailTable, value,
															isSelected,
															hasFocus, row,
															column);
											if (row == detailTable.getSelectedRow())
												cell.setBackground(cell.getBackground());
											else if (column == model.getPlotFunctionID1()
													| column == model.getPlotFunctionID2())
												cell.setBackground(Color.LIGHT_GRAY);
											else
												cell.setBackground(Color.WHITE);
											return cell;
										}
									});
							detailTable.repaint();
						}
					}
				});
		
		tableBox.add(detailTable.getTableHeader());
		tableBox.add(new JScrollPane(detailTable));

		commentText.addActionListener(new ActionListener() { 	
			public void actionPerformed(ActionEvent e) {
				if (commentText.isEditable())
					model.getRunMachineModel().setPropertyValue("COMMENTS", commentText.getText());
			}					
		});
		
		model.addBrowserModelListener(new BrowserModelListener() {
			@Override
			public void modelStateChanged(BrowserModel model) {					
				commentText.setEditable(false);
				MachineModel selectedMachineModel = model.getSelectedMachineModel();
				MachineModelDetail[] selectedMachineModelDetail = model.getSelectedMachineModelDetail();
				String selectedModelType = "selected";
				
				
				if (selectedMachineModel != null && selectedMachineModelDetail != null) {
					if (selectedMachineModel == model.getRunMachineModel()) {
						selectedModelType = "run";
						commentText.setEditable(true);
					}
				} else {
					selectedMachineModel = model.getReferenceMachineModel();
					selectedMachineModelDetail = model.getReferenceMachineModelDetail();
					selectedModelType = "reference";
				}
				
				if (selectedMachineModel != null) {
					commentText.setText((String)selectedMachineModel.getPropertyValue("COMMENTS"));
					String title = "The "+ selectedModelType +" machine model has "
									+ selectedMachineModelDetail.length + " records logged.";
					modelName.setText(title);
					modelCreatDate.setText((String)selectedMachineModel.getPropertyValue("DATE_CREATED"));			
				}
			}
		});

		modelDetailView = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				modelDetail, tableBox);
		modelDetailView.setOneTouchExpandable(true);
	}
	
	public JTable getDataTable(){
		return detailTable;
	}

	public Container getInstance() {
		return modelDetailView;
	}
}
