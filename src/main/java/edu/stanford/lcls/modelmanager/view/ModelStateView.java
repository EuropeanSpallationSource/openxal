package edu.stanford.lcls.modelmanager.view;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.stanford.lcls.modelmanager.dbmodel.BrowserModel;
import edu.stanford.lcls.modelmanager.dbmodel.BrowserModelListener;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModel;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDetail;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDevice;

public class ModelStateView extends Thread implements SwingConstants {
	protected BrowserModel _model;
	private static Box modelStateView;
	private static JTextField dataBaseState;
	private static JTextField machineModelState;
	private static JProgressBar progressBar;

	public ModelStateView(JFrame parent, BrowserModel model) {
		_model = model;
		modelStateView = new Box(BoxLayout.X_AXIS);
		dataBaseState = new JTextField();
		machineModelState = new JTextField();
		progressBar = new JProgressBar();
		
		modelStateView.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLoweredBevelBorder(), BorderFactory
						.createEmptyBorder(3, 6, 2, 4)));

		modelStateView.add(new JLabel("Database Access Status:"));
		modelStateView.add(Box.createHorizontalStrut(10));
		dataBaseState.setEditable(false);
		dataBaseState.setBorder(null);
		modelStateView.add(dataBaseState);

		modelStateView.add(Box.createHorizontalStrut(10));
		modelStateView.add(new JSeparator(VERTICAL));
		modelStateView.add(Box.createHorizontalStrut(20));
		modelStateView.add(new JLabel("Model Query Status:"));
		modelStateView.add(Box.createHorizontalStrut(10));
		machineModelState.setEditable(false);
		machineModelState.setBorder(null);
		modelStateView.add(machineModelState);
		
		modelStateView.add(Box.createHorizontalStrut(100));
		progressBar.setPreferredSize(new Dimension(160, 0)); // length of ProgressBar
		progressBar.setStringPainted(true);
		modelStateView.add(progressBar);
	}
	
	public void run(){
		dataBaseState.setText("Trying to connect to the default Database...");
		machineModelState.setText("Loading all the machine models...");		
		progressBar.setIndeterminate(true);
		progressBar.setString("Conecting...");
		
		_model.addBrowserModelListener(new BrowserModelListener() {
			@Override
			public void modelStateChanged(BrowserModel model) {
				if (!model.getStateReady()) {
					int lastColonIndex = _model.getDataBaseURL().lastIndexOf(":");
					dataBaseState.setText("User \"" + _model.getConnectUser()
							+ "\" connected to \""
							+ _model.getDataBaseURL().substring(lastColonIndex + 1)
							+ "\" Database.");
					machineModelState.setText("Retrieving machine models from database ...");
					progressBar.setString("Loading ...");
				} else {
					if (model.getSelectedMachineModel() != null) {
						
						machineModelState.setText("You can now plot or export the running machine model data...");
						progressBar.setIndeterminate(false);
						progressBar.setString("Running Success !");
					}
					machineModelState.setText("Find " + _model.getFetchedMachineModel().size()
							+ " machine models in the database!");
					progressBar.setIndeterminate(false);
					progressBar.setString("Loading successful!");					
				}
				
			}
		});
		modelStateView.repaint();
	}
	
	public static JTextField getDataBaseState(){
		return dataBaseState;
	}
	
	public static JTextField getMachineModelState(){
		return machineModelState;
	}
	
	public static JProgressBar getProgressBar(){
		return progressBar;
	}

	public static Container getInstance() {
		return modelStateView;
	}
	
}