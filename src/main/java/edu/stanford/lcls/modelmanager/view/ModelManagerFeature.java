package edu.stanford.lcls.modelmanager.view;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import xal.smf.Accelerator;
import edu.stanford.lcls.modelmanager.dbmodel.BrowserModel;
import edu.stanford.lcls.modelmanager.dbmodel.BrowserModelListener;
import edu.stanford.lcls.modelmanager.dbmodel.BrowserModelListener.BrowserModelAction;
import edu.stanford.slac.Message.Message;

public class ModelManagerFeature  implements SwingConstants {
	private JFrame frame;
	protected static BrowserModel model;
	private Container queryBox;
	private Container modelStateBox;
	private Container modelListBox;
	private Container modelDetailBox;
	private Container modelDeviceBox;
	private JSplitPane modelPlotBox;
	private ToolBarView toolBarView;
	private ModelListView modelListView;
//	private static Message logger = Message.getInstance();

	public ModelManagerFeature(JFrame _frame, JPanel stateBar, Accelerator acc) {
		frame =_frame;
		model = new BrowserModel(acc);
		ModelStateView modelStateView = new ModelStateView(frame, model);
		modelStateBox = ModelStateView.getInstance();
		modelStateView.start();
		modelListView = new ModelListView(frame, model);
		toolBarView = new ToolBarView(frame, model, modelListView);
		queryBox = toolBarView.getInstance();
		modelListBox = modelListView.getInstance();
		ModelDetailView modelDetailView = new ModelDetailView(frame, model);
		modelDetailBox = modelDetailView.getInstance();
		ModelDeviceView modelDeviceView = new ModelDeviceView(model);
		modelDeviceBox = modelDeviceView.getInstance();
		modelPlotBox = (JSplitPane) new ModelPlotView(frame, model,
				modelDetailView.getDataTable(), modelStateView).getInstance();

		final JTabbedPane mainDataPanel = new JTabbedPane();
		mainDataPanel.addTab("Z Plot", modelPlotBox);
		mainDataPanel.addTab("Model Details", modelDetailBox);
		mainDataPanel.addTab("Machine Parameters", modelDeviceBox);
		mainDataPanel.setToolTipTextAt(0, "Z plot panel for data plot");
		mainDataPanel.setToolTipTextAt(1, "model data table");
		mainDataPanel.setToolTipTextAt(2, "magnet and RF parameters used in the model");
		
		JTextPane loggerPane = new JTextPane();
		Message.setSwingWidget(loggerPane);
		
		JSplitPane listView = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				new JScrollPane(loggerPane), modelListBox);
		listView.setDividerLocation(30);
		listView.setOneTouchExpandable(true);
		
		JSplitPane contentView = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				 mainDataPanel, listView);
		contentView.setOneTouchExpandable(true);

		Container mainView = new Container();
		mainView.setLayout(new BorderLayout());
		mainView.add(queryBox, BorderLayout.PAGE_START);
		mainView.add(contentView, BorderLayout.CENTER);
		stateBar.add(modelStateBox);
		
		frame.getContentPane().add(mainView);
		frame.setVisible(true);
		contentView.setDividerLocation(0.7);
		modelPlotBox.setDividerLocation(modelPlotBox.getWidth() - 200);
		
		model.addBrowserModelListener(new BrowserModelListener() {
			
			@Override
			public void modelStateChanged(BrowserModel model, BrowserModelAction action) {
				if (action.equals(BrowserModelAction.RUN_DATA_FETCHED))
					mainDataPanel.setSelectedComponent(modelDeviceBox);
				else if (action.equals(BrowserModelAction.MODEL_RUN))
					mainDataPanel.setSelectedComponent(modelPlotBox);
				
			}
		});
	}
	
/*	public static Message getMessageLogger(){
		return logger;
	}
*/	
	public JSplitPane getModelPlotPane() {
		return modelPlotBox;
	}
	
	public ModelListView getModelListView() {
		return modelListView;
	}
	
	public void connectDefault(){
		modelListView.connectDefault();
	}
	
	public static BrowserModel getBrowserModel() {
		return model;
	}
}
