package edu.stanford.lcls.modelmanager.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;

import edu.stanford.lcls.modelmanager.dbmodel.BrowserModel;
import edu.stanford.lcls.modelmanager.dbmodel.BrowserModelListener;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModel;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDetail;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDevice;
import edu.stanford.slac.Message.Message;
import edu.stanford.slac.util.zplot.ZPlotPanel;

public class ModelPlotView implements SwingConstants {
	private JSplitPane modelPlotView;
	private static JPanel modelPlotBox;
	private JPanel plotSelectBox;
	private ZPlotPanel zPlotPanel;
	private static int plotFunctionID1;
	private static int plotFunctionID2;
	private static int plotSignMethod;
	private static boolean plotNodeMethod;
	private static int plotMethod;
	private boolean isGold;
	protected BrowserModel _model;
	private MachineModel _referenceMachineModel;
	private MachineModelDetail[] _referenceMachineModelDetail;
	private MachineModel _selectedMachineModel;
	private MachineModelDetail[] _selectedMachineModelDetail;

	private JList<String> plotFunctionList;
	private JRadioButton plotOriginal;
	private JRadioButton plotDifferent;
	private JRadioButton plotZPosSign;
	private JRadioButton plotMADNameSign;
	//private JRadioButton plotEPICSNameSign;
	private JCheckBox plotNode;
	private JButton refreshButton;
	private JButton SnapshotButton;

	public ModelPlotView(JFrame parent, BrowserModel model,
			final JTable dataTable, ModelStateView modelStateView) {
		_model = model;
		modelPlotBox = new JPanel();
		plotSelectBox = new JPanel(new BorderLayout());
		plotSelectBox.setBorder(BorderFactory.createEmptyBorder(2, 3, 1, 2));

		// Plot Function
		plotSelectBox.add(new JLabel("Select A Plot Function :"),
				BorderLayout.PAGE_START);
		ListData ld = new ListData();
		plotFunctionList = new JList<String>(ld);
		plotFunctionList.setSelectedIndex(1);
		Box listBox = new Box(BoxLayout.Y_AXIS);
		listBox.add(new JScrollPane(plotFunctionList));
		//listBox.add(Box.createVerticalGlue());
		plotSelectBox.add(listBox, BorderLayout.CENTER);

		// Plot Button
		Box buttonView = new Box(BoxLayout.Y_AXIS);
		plotSelectBox.add(buttonView, BorderLayout.PAGE_END);
		
		ActionListener plotListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == plotOriginal)
					plotMethod = 0;
				else if (e.getSource() == plotDifferent)
					plotMethod = 1;
				else if (e.getSource() == plotZPosSign)
					plotSignMethod = 0;
				else if (e.getSource() == plotMADNameSign)
					plotSignMethod = 1;
				//else if (e.getSource() == plotEPICSNameSign)
				//	plotSignMethod = 2;
				else if (e.getSource() == plotNode)
					plotNodeMethod = plotNode.isSelected();
				plotAction();
			}
		};		
		
		buttonView.add(Box.createVerticalStrut(5));
		JPanel plotMethodPanel = new JPanel(new GridLayout(2, 1));
		plotMethodPanel.setBorder(new TitledBorder(new LineBorder(Color.gray,1),"Comparison"));
		plotOriginal = new JRadioButton("Overlay Reference");
		plotOriginal.setToolTipText("plot individual model set(s)");
		plotOriginal.setSelected(true);
		plotDifferent = new JRadioButton("Plot Diff from Reference");
		plotDifferent.setToolTipText("plot the difference between the SEL and REF models");
		plotMethodPanel.add(plotOriginal);
		plotMethodPanel.add(plotDifferent);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(plotOriginal);
		buttonGroup.add(plotDifferent);
		plotOriginal.addActionListener(plotListener);
		plotDifferent.addActionListener(plotListener);
		buttonView.add(plotMethodPanel);
		
		buttonView.add(Box.createVerticalStrut(5));
		JPanel plotConfigurePanel = new JPanel(new GridLayout(4, 1));
		plotConfigurePanel.setBorder(new TitledBorder(new LineBorder(Color.gray,1),"Configure"));		
		plotZPosSign = new JRadioButton("Show Z Position");
		plotZPosSign.setToolTipText("show Z position at the bottom of the plot");
		plotZPosSign.setSelected(true);
		plotMADNameSign = new JRadioButton("Show Element Names");
		plotMADNameSign.setToolTipText("show element names at the bottom of the plot");
		//plotEPICSNameSign = new JRadioButton("Show EPICS Names");
		//plotEPICSNameSign.setToolTipText("show EPICS names at the bottom of the plot");
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(plotZPosSign);
		buttonGroup1.add(plotMADNameSign);
		//buttonGroup1.add(plotEPICSNameSign);
		plotNode = new JCheckBox("Show Device Points");
		plotNode.setToolTipText("show/hide symbol for each data point in the plot");
		plotConfigurePanel.add(plotZPosSign);
		plotConfigurePanel.add(plotMADNameSign);
		//plotConfigurePanel.add(plotEPICSNameSign);
		plotConfigurePanel.add(plotNode);
		plotZPosSign.addActionListener(plotListener);
		plotMADNameSign.addActionListener(plotListener);
		//plotEPICSNameSign.addActionListener(plotListener);
		plotNode.addActionListener(plotListener);
		buttonView.add(plotConfigurePanel);
		
		buttonView.add(Box.createVerticalStrut(3));
		JPanel plotButtonSet = new JPanel(new FlowLayout());
		refreshButton = new JButton("Refresh");
		SnapshotButton = new JButton("Snapshot");
		plotButtonSet.add(refreshButton);
		plotButtonSet.add(SnapshotButton);
		//buttonView.add(plotButtonSet);

		modelPlotView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				modelPlotBox, plotSelectBox);
		modelPlotView.setOneTouchExpandable(true);

		// Set default Plot Function
		plotMethod = 0;
		plotSignMethod = 0;
		int defaultSelectedRow = 1;
		modelPlotView.setResizeWeight(defaultSelectedRow);
		plotFunctionID1 = ListData.getPlotFunctionID(defaultSelectedRow, 1);
		plotFunctionID2 = ListData.getPlotFunctionID(defaultSelectedRow, 2);
		_model.setPlotFunctionID1(plotFunctionID1);
		_model.setPlotFunctionID2(plotFunctionID2);
		setPlotPanelEnable(false);
		dataTable.setDefaultRenderer(Object.class,
				new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 1L;
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						Component cell = super.getTableCellRendererComponent(
								dataTable, value, isSelected, hasFocus, row,
								column);
						if (column == plotFunctionID1
								| column == plotFunctionID2)
							cell.setBackground(Color.LIGHT_GRAY);
						else
							cell.setBackground(Color.WHITE);
						return cell;
					}
				});

		_model.addBrowserModelListener(new BrowserModelListener() {
			@Override
			public void modelStateChanged(BrowserModel model) {
				if (model.getStateReady()) {
					setPlotPanelEnable(true);
					_referenceMachineModel = model.getReferenceMachineModel();
					_referenceMachineModelDetail = model.getReferenceMachineModelDetail();
					isGold = _model.isGold();
					
					_selectedMachineModel = model.getSelectedMachineModel();
					_selectedMachineModelDetail = model.getSelectedMachineModelDetail();
					if (_selectedMachineModelDetail == null) _selectedMachineModel = null;
					plotAction();
				} else {
					setPlotPanelEnable(false);
				}
				
			}
		});

		plotFunctionList.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent event) {
						if (!event.getValueIsAdjusting()) {
							int selectedRow = plotFunctionList
									.getSelectedIndex();
							plotFunctionID1 = ListData.getPlotFunctionID(selectedRow, 1);
							plotFunctionID2 = ListData.getPlotFunctionID(selectedRow, 2);
							_model.setPlotFunctionID1(plotFunctionID1);
							_model.setPlotFunctionID2(plotFunctionID2);
							dataTable.setDefaultRenderer(Object.class,
									new DefaultTableCellRenderer() {
										private static final long serialVersionUID = 1L;
										public Component getTableCellRendererComponent(
												JTable table, Object value,
												boolean isSelected,
												boolean hasFocus, int row,
												int column) {
											Component cell = super
													.getTableCellRendererComponent(
															dataTable, value,
															isSelected,
															hasFocus, row,
															column);
											if (column == plotFunctionID1
													| column == plotFunctionID2)
												cell.setBackground(Color.LIGHT_GRAY);
											else
												cell.setBackground(Color.WHITE);
											return cell;
										}
									});
						}
						plotAction();
					}
				});

		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ModelPlotData.clearRange();
				plotAction();
			}
		});

		SnapshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelPlotView.repaint();
				try {
					zPlotPanel.doSaveAs();
				} catch (IOException e1) {
					Message.error("File IO Exception while saving snapshot: " + e1.getMessage());			
					e1.printStackTrace();
				}
			}
		});
	}
	
	public void plotAction() {
		Color refernecColor;
		if (isGold)
			refernecColor = Color.ORANGE;
		else
			refernecColor = Color.CYAN;
		ModelPlotData.getRange();
		if (_selectedMachineModel != null && _referenceMachineModel != null){
			if (plotMethod == 0){
				zPlotPanel = ModelPlotData.plotOriginal(_referenceMachineModelDetail, 
						_selectedMachineModelDetail, plotFunctionID1, plotFunctionID2, 
						plotSignMethod, plotNodeMethod, modelPlotBox, isGold);
				zPlotPanel.setTitle("Selected Model & Reference Model", Color.WHITE);
			} else if (plotMethod == 1){
				zPlotPanel = ModelPlotData.plotDifferent(_model
						.getReferenceMachineModelDetail(), _model
						.getSelectedMachineModelDetail(), plotFunctionID1,
						plotFunctionID2, plotSignMethod, plotNodeMethod, modelPlotBox);
				zPlotPanel.setTitle("Difference Between Selected & Reference Model", Color.WHITE);
			}
		} else if (_referenceMachineModel != null)	{
			zPlotPanel = ModelPlotData.plotData(_referenceMachineModelDetail,
					plotFunctionID1, plotFunctionID2, plotSignMethod, plotNodeMethod, modelPlotBox, isGold);
			zPlotPanel.setTitle("Reference Machine Model", Color.WHITE);	
		} else if (_selectedMachineModel != null){
			zPlotPanel = ModelPlotData.plotData(_selectedMachineModelDetail,
					plotFunctionID1, plotFunctionID2, plotSignMethod, plotNodeMethod, modelPlotBox, isGold);
			zPlotPanel.setTitle("Selected Machine Model", Color.WHITE);			
		}
		
		if (_selectedMachineModel != null)
			zPlotPanel.getChart().addSubtitle(new TextTitle("Selected Model ID: "+
					_selectedMachineModel.getPropertyValue("ID") +
					"    Run Source: " +
					_selectedMachineModel.getPropertyValue("RUN_SOURCE_CHK") +
					"    Created Date: " +
					_selectedMachineModel.getPropertyValue("DATE_CREATED"),
					new Font("SansSerif", Font.PLAIN, 11), Color.GREEN,
					Title.DEFAULT_POSITION, Title.DEFAULT_HORIZONTAL_ALIGNMENT,
	                Title.DEFAULT_VERTICAL_ALIGNMENT, Title.DEFAULT_PADDING));
		if (_referenceMachineModel != null)
			zPlotPanel.getChart().addSubtitle(new TextTitle("Reference Model ID: " +
					_referenceMachineModel.getPropertyValue("ID") + 
					"    Run Source: " +
					_referenceMachineModel.getPropertyValue("RUN_SOURCE_CHK") +
					"    Created Date: " + 
					_referenceMachineModel.getPropertyValue("DATE_CREATED"),
					new Font("SansSerif", Font.PLAIN, 11), refernecColor,
					Title.DEFAULT_POSITION, Title.DEFAULT_HORIZONTAL_ALIGNMENT,
	                Title.DEFAULT_VERTICAL_ALIGNMENT, Title.DEFAULT_PADDING));
		// modelPlotView.repaint(); //Repaint doesn't work!!!
		modelPlotView.setDividerLocation(modelPlotView.getDividerLocation());
	}

	public void setPlotPanelEnable(boolean enabled) {
		plotFunctionList.setEnabled(enabled);
		plotFunctionList.setFocusable(enabled);
		plotOriginal.setEnabled(enabled);
		plotDifferent.setEnabled(enabled);
		plotZPosSign.setEnabled(enabled);
		plotMADNameSign.setEnabled(enabled);
		//plotEPICSNameSign.setEnabled(enabled);
		plotNode.setEnabled(enabled);
		refreshButton.setEnabled(enabled);
		SnapshotButton.setEnabled(enabled);
	}
	
	public Container getInstance() {
		return modelPlotView;
	}

}

class ListData extends AbstractListModel<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final private List<String> PLOT_FUNCTION;

	static {
		PLOT_FUNCTION = new ArrayList<String>(Arrays.asList(new String[] {
				"ALPHA X & Y", "BETA X & Y", "PSI X & Y", "ETA X & Y",
				"ETAP X & Y", "R11 & R33", "R12 & R34", "R21 & R43", 
				"R22 & R44", "ENERGY & P", "Bmag X & Y"}));
	}

	public ListData() {
	}

	public int getSize() {
		return PLOT_FUNCTION.size();
	}

	public String getElementAt(int index) {
		return PLOT_FUNCTION.get(index);
	}

	static public List<String> getPlotFunction() {
		return PLOT_FUNCTION;
	}
	
	static public int getPlotFunctionID(int rowNumber, int plotNumber){
		if ( rowNumber <= 4 ){
			return rowNumber * 2 + plotNumber + 4;
		} else if( rowNumber <= 6 ){
			return rowNumber + plotNumber * 14 - 4;
		} else if( rowNumber <= 8 ){
			return rowNumber + plotNumber * 14;
		} else if( rowNumber == 9 & plotNumber == 1){
			return 4;
		} else if( rowNumber == 9 & plotNumber == 2){
				return 57;
		} else if( rowNumber == 10 ){
				return 57 + plotNumber;
		}
		return 54;
	}
}