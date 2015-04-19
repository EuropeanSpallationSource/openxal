package edu.stanford.lcls.modelmanager.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import xal.extension.widgets.apputils.SimpleProbeEditor;
import xal.model.IAlgorithm;
import xal.model.probe.Probe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;
import edu.stanford.lcls.modelmanager.dbmodel.*;
import edu.stanford.lcls.modelmanager.*;
import edu.stanford.slac.Message.Message;

import java.sql.SQLException;

/**
 * QueryView is the view for querying the database for the machine models.
 * 
 * getInstance() @return the query view
 */

public class ToolBarView implements SwingConstants {
	private JToolBar toolBarView;
	private JFrame parent;
	private BrowserModel model;
	private JComboBox<String> beamlineSelector;
	private JComboBox<String> runModeSelector;
	private JComboBox<ComboItem> BPRPSelector; //Back Propagte Reference points
	private JComboBox<String> BPRPModeSelector;
	private JButton setInitTiwissButton;
	private JButton runModelButton;
	private JButton upload2DBButton;
	private JButton makeGoldButton;
	private JButton export2MADButton;
//	private JButton helpButton;
//	private JButton exitButton;
	private ModelListView modelListView;
	private Thread thread1;
	private Thread thread2;
	private int modelModesID;
	private String refID;
	private int runModelMethod;
	private boolean refMode = true;

//	private int bprpMethod;

	public ToolBarView(JFrame _parent, BrowserModel _model, final ModelListView _modelListView) {
		parent = _parent;
		model = _model;
		modelListView = _modelListView;
//		toolBarView = new JToolBar();
		toolBarView = ((ModelManagerWindow)_parent).getToolBar();
		
		
		// usually a beamline selector would be here, but we put in probe editor
		
		// TODO OPENXAL beamline selections
		String[] beamlineSelections = {};
		beamlineSelector = new JComboBox<String>(beamlineSelections);
		beamlineSelector.setToolTipText("select a beamline");
		beamlineSelector.setMaximumSize(new Dimension(180, 28));
		beamlineSelector.setEnabled(false);
		//toolBarView.add(beamlineSelector);
		
		setInitTiwissButton = new JButton("Edit Init Twiss...");
		setInitTiwissButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				SimpleProbeEditor spe = null;
				try {
					spe = new SimpleProbeEditor( parent, model.getRunModel().getProbe(), false );
					spe.setTitle("Edit Init Twiss");
					spe.setVisible(true);
				} finally {
					if (spe != null) spe.dispose();
				}
			}
		});
		setInitTiwissButton.setEnabled(false);
		toolBarView.add(setInitTiwissButton);		

		
		toolBarView.addSeparator(new Dimension(20, 10));
		String[] runModeSelections = { "Design", "Extant" };
		runModeSelector = new JComboBox<String>(runModeSelections);
		runModeSelector.setToolTipText("select to run either DESIGN or EXTANT model");
		runModeSelector.setMaximumSize(new Dimension(100, 28));
		runModeSelector.setSelectedIndex(1);
		runModelMethod = runModeSelector.getSelectedIndex();
		toolBarView.add(runModeSelector);
		
		JPanel bp = new JPanel();
		bp.setBorder(BorderFactory.createTitledBorder("Back Prop. Twiss from"));
		bp.setLayout(new BoxLayout(bp, BoxLayout.LINE_AXIS));
		
		toolBarView.addSeparator(new Dimension(10, 10));

		final ComboItem[] refPts = { new ComboItem("WS000") }; 
		BPRPSelector = new JComboBox<ComboItem>(refPts);
		BPRPSelector.setToolTipText("select Twiss back propagate reference point");
		BPRPSelector.setMaximumSize(new Dimension(105, 28));
//		refID = refPts[BPRPSelector.getSelectedIndex()].toString();
//		bprpMethod = 0;
		BPRPSelector.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
//				bprpMethod = runModeSelector.getSelectedIndex();
				refID = refPts[BPRPSelector.getSelectedIndex()].toString();
				if (model.getRunModel() != null)
					model.setModelRef(refID);
			}
		});
		BPRPSelector.setRenderer(new ComboRenderer());
		BPRPSelector.addActionListener(new ComboListener(BPRPSelector));
		
		
		BPRPModeSelector = new JComboBox<String>(new String[]{"Measured", "Design"});
		BPRPModeSelector.setToolTipText("select to use either Design or Measured Twiss at the reference point");
		BPRPModeSelector.setSelectedIndex(1);
		if (BPRPModeSelector.getSelectedIndex() != 0)
			refMode = false;
		else
			refMode = true;
		
		BPRPModeSelector.setMaximumSize(new Dimension(100, 28));
		BPRPModeSelector.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				if (BPRPModeSelector.getSelectedIndex() != 0)
					refMode = false;
				else
					refMode = true;
				
				model.setModelRefDesign(refMode);
			}
		});
		
//		toolBarView.add(BPRPSelector);
		bp.add(BPRPSelector);
		bp.add(BPRPModeSelector);

		toolBarView.add(bp);
		runModeSelector.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				runModelMethod = runModeSelector.getSelectedIndex();
				if (runModelMethod == 0) {
					BPRPSelector.setEnabled(false);
					BPRPModeSelector.setEnabled(false);
				} else {					
					BPRPSelector.setEnabled(true);
					BPRPModeSelector.setEnabled(true);
				}
			}
		});
		
		beamlineSelector.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				if(beamlineSelector.getSelectedIndex() == 0) {
					modelModesID = 5;
					BPRPSelector.setEnabled(true);
					for (int i=0; i< refPts.length; i++)
						((ComboItem) refPts[i]).setEnabled(true);
				}
				
				thread1 = new Thread(new Runnable() {
					public void run() {
						setQueryViewEnable(false);
						modelListView.setModelListViewEnable(false);
						ModelStateView.getDataBaseState();
						ModelStateView.getMachineModelState().setText(
								"Trying to find models in the selected beam line...");
						ModelStateView.getProgressBar().setString("Loading ...");
						ModelStateView.getProgressBar().setIndeterminate(true);
						Message.info("Trying to find models in the selected beam line...");
					}
				});
				thread2 = new Thread(new Runnable() {
					public void run() {
						try {
							model.setModelMode(modelModesID);
						} catch (SQLException exception) {
							Message.error("SQLException: " + exception.getMessage());			
							exception.printStackTrace();
						}
					}
				});
				thread1.start();
				thread2.start();
			}
		});

		toolBarView.addSeparator(new Dimension(10, 10));		
		
		toolBarView.addSeparator(new Dimension(5, 10));
		runModelButton = new JButton("Run Model");
		runModelButton.setToolTipText("run XAL online model");
		toolBarView.add(runModelButton);
		
		toolBarView.addSeparator(new Dimension(5, 10));
		upload2DBButton = new JButton("Save");
		upload2DBButton.setToolTipText("save the model data in memory to database");
		upload2DBButton.setEnabled(false);
		toolBarView.add(upload2DBButton);
		
		toolBarView.addSeparator(new Dimension(5, 10));
		makeGoldButton = new JButton("Make SEL Gold");
		makeGoldButton.setToolTipText("tag the selected model (in SEL column from the table) as the GOLD one");
		toolBarView.add(makeGoldButton);
		makeGoldButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				thread1 = new Thread(new Runnable() {
					public void run() {
						setQueryViewEnable(false);
						ModelStateView.getMachineModelState().setText(
								"Application is trying set the selected machine model to GOLD...");
						ModelStateView.getProgressBar().setString("Running ...");
						ModelStateView.getProgressBar().setIndeterminate(true);
						Message.info("Application is trying to set the selected machine model to GOLD...");
						}
					});
				thread2 = new Thread(new Runnable() {
					public void run() {
						try {
							model.makeGold();
							ModelStateView.getMachineModelState().setText(
							"The selected machine model has been set to GOLD !");
							ModelStateView.getProgressBar().setString("Done !");
							ModelStateView.getProgressBar().setIndeterminate(false);
							setQueryViewEnable(true);
							Message.info("The selected machine model has been set to GOLD!");
							modelListView.connectDefault();
						} catch (Exception exception) {
							exception.printStackTrace();
							JOptionPane.showMessageDialog(parent, exception.getMessage(),
									"Set Gold Model Error!", JOptionPane.ERROR_MESSAGE);
							ModelStateView.getMachineModelState().setText(
									"Application is failed to set gold model !");
							ModelStateView.getProgressBar().setString("Done !");
							ModelStateView.getProgressBar().setIndeterminate(false);
							setQueryViewEnable(true);
							Message.error("Failed to set gold model!");
						}
						}
					});
				thread1.start();
				thread2.start();
			}
		});
		
		toolBarView.addSeparator(new Dimension(5, 10));
		export2MADButton = new JButton("Export to MAD");
		export2MADButton.setEnabled(false);
		//TODO to be added later for exporting to MAD file
//		toolBarView.add(export2MADButton);
		
		toolBarView.add(Box.createGlue());
		toolBarView.addSeparator(new Dimension(10, 10));
//		helpButton = new JButton("Help", IconLib.getIcon( IconGroup.GENERAL, "Help24.gif" ));
//		// use the framework one
//		//		toolBarView.add(helpButton);
//		toolBarView.addSeparator(new Dimension(10, 10));
//		
//		exitButton = new JButton("Exit");
////		toolBarView.add(exitButton);
//		exitButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				System.exit(0);
//			}
//		});
		
		setQueryViewEnable(false);
		
		runModelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				thread1 = new Thread(new Runnable() {
					public void run() {
						setQueryViewEnable(false);
						ModelStateView.getMachineModelState().setText(
								"Application is running " + runModeSelector.getSelectedItem() + " XAL machine model...");
						ModelStateView.getProgressBar().setString("Running ...");
						ModelStateView.getProgressBar().setIndeterminate(true);
						Message.info("Application is running " + runModeSelector.getSelectedItem() + " XAL machine model...");
						}
					});
				thread2 = new Thread(new Runnable() {
					public void run() {
						try {
							// model.setModelRef(refID);
							
							boolean useDesignRefInd = false;
							if (BPRPModeSelector.getSelectedIndex() == 0)
								useDesignRefInd = false;
							else
								useDesignRefInd = true;
							
							model.runModel(runModelMethod, refID, useDesignRefInd);
							ModelStateView.getMachineModelState().setText(
							"The new XAL machine model's ID is \"RUN\" !");
							ModelStateView.getProgressBar().setString("Done !");
							ModelStateView.getProgressBar().setIndeterminate(false);
							setQueryViewEnable(true);
							Message.info("The new XAL machine model's ID is \"RUN\" !");
						} catch (Exception exception) {
							exception.printStackTrace();
                            String message = exception.getMessage();
                            if(exception.getCause() != null) { 
                                message = message + " -- " + exception.getCause().getMessage();
                            }
							JOptionPane.showMessageDialog(parent, message,
									"Run Model Error! See the xterm window for more details", JOptionPane.ERROR_MESSAGE);
							ModelStateView.getMachineModelState().setText(
									"Application is failed to run model !");
							ModelStateView.getProgressBar().setString("Done !");
							ModelStateView.getProgressBar().setIndeterminate(false);
							setQueryViewEnable(true);
							Message.error("Application failed to run model !");
						}
						}
					});
				thread1.start();
				thread2.start();
			}
		});

		export2MADButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				thread1 = new Thread(new Runnable() {
					public void run() {
						runModelButton.setEnabled(false);
						ModelStateView.getMachineModelState().setText(
						"Application is exporting the model data to XML file...");
						ModelStateView.getProgressBar().setString("Exporting ...");
						ModelStateView.getProgressBar().setIndeterminate(true);
						Message.info("Exporting the model data to XML files...");
					}
				});
				thread2 = new Thread(new Runnable() {
					public void run() {
						model.exportToXML(parent);
						ModelStateView.getMachineModelState().setText(
						"Application has finished to export the model data !");
						ModelStateView.getProgressBar().setString("Done ...");
						ModelStateView.getProgressBar().setIndeterminate(false);
						runModelButton.setEnabled(true);
						Message.info("Exporting model data finished!");
					}
				});
				thread1.start();
				thread2.start();
			}
		});

		upload2DBButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				thread1 = new Thread(new Runnable() {
					public void run() {
						System.out.println("Save start!");
						upload2DBButton.setEnabled(false);
						runModelButton.setEnabled(false);
						ModelStateView.getMachineModelState().setText(
						"Application is uploading the model data to database...");
						ModelStateView.getProgressBar().setString("Uploading ...");
						ModelStateView.getProgressBar().setIndeterminate(true);
						Message.info("Uploading model RUN data to database...");;
					}
				});
				thread2 = new Thread(new Runnable() {
					public void run() {
						String uploadID= model.uploadToDatabase(parent);
						long sleepBegin = System.currentTimeMillis();
						long sleepEnd = 0;
						while (!DataManager.getElementModelsDB_done()) {
							if (DataManager.status == 0) {
								Message.error("Application failed to correctly upload the model data !");
								break;
							}
							try {
								if (sleepEnd < 120000 && DataManager.status == 2) {
									Message.debug("Sleeping before Run Button Enabled!");
									Thread.sleep(1000); //sleep for 2mins max
									sleepEnd = System.currentTimeMillis() - sleepBegin;
									Message.debug("Sleep Time: " + sleepEnd);
								}
								else {
									Message.debug("Timed out!");
									DataManager.status = 0;
									break;
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if (DataManager.status == 1) {
							ModelStateView.getMachineModelState().setText(
							"Application has finished uploading the model data !");
						}
						ModelStateView.getProgressBar().setString("Done ...");
						ModelStateView.getProgressBar().setIndeterminate(false);
						upload2DBButton.setEnabled(uploadID == null);
						runModelButton.setEnabled(true);
						System.out.println("Save finished");
						Message.info("Uploading model data finished!");
						// no need to call AIDA name update from this app
						// DataManager.updateAIDA();
						}
				});
				thread1.start();
				thread2.start();
			}
		});
		
		model.addBrowserModelListener(new BrowserModelListener() {
			public void connectionChanged(BrowserModel model) {
				
			}
			public void machineModelFetched(BrowserModel model,
					MachineModel[] fetchedMachineModel, MachineModel referenceMachineModel,
					MachineModelDetail[] referenceMachineModelDetail,
					MachineModelDevice[] referenceMachineModelDevice) {
				setQueryViewEnable(true);
			}
			public void modelSelected(BrowserModel model,
					MachineModel selectedMachineModel,
					MachineModelDetail[] selectedMachineModelDetail,
					MachineModelDevice[] selectedMachineModelDevice) {
			}
			public void runModel(BrowserModel model,
					MachineModel[] fetchedMachineModel,
					MachineModel runMachineModel,
					MachineModelDetail[] runMachineModelDetail,
					MachineModelDevice[] runMachineModelDevice){
			}
		});
		
		// This is a trick to move the "Help" button from the first to the right place.
		Component comp1 = toolBarView.getComponentAtIndex(0);
		Component comp2 = toolBarView.getComponentAtIndex(1);
		toolBarView.remove(comp1);
		toolBarView.remove(comp2);
		
		toolBarView.add(comp1);
		toolBarView.add(comp2);
	}

	public Container getInstance() {
		return toolBarView;
	}
	
	public void setQueryViewEnable(boolean enabled) {
		if (!((String)runModeSelector.getSelectedItem()).equals("Design")) {
			BPRPModeSelector.setEnabled(enabled);
			BPRPSelector.setEnabled(enabled);
		} else {
			BPRPModeSelector.setEnabled(false);
			BPRPSelector.setEnabled(false);			
		}
		beamlineSelector.setEnabled(enabled);
		runModeSelector.setEnabled(enabled);
		setInitTiwissButton.setEnabled(enabled);
		runModelButton.setEnabled(enabled);
		upload2DBButton.setEnabled(enabled && !model.isRunMachineModelNull());
		makeGoldButton.setEnabled(enabled);
		//export2MADButton.setEnabled(enabled && !model.isRunMachineModelNull());
//		helpButton.setEnabled(enabled);
	}


	public void displayError(final String aTitle, final String prefix,
			final Exception exception) {
		Toolkit.getDefaultToolkit().beep();
		String message = prefix + "\n" + "Exception: "
				+ exception.getClass().getName() + "\n"
				+ exception.getMessage();
		JOptionPane.showMessageDialog(parent, message, aTitle,
				JOptionPane.ERROR_MESSAGE);
	}

	
	@SuppressWarnings("serial")
	class ComboRenderer extends JLabel implements ListCellRenderer<ComboItem> {

		public ComboRenderer() {
			setOpaque(true);
			setBorder(new EmptyBorder(1, 1, 1, 1));
		}

		public Component getListCellRendererComponent( JList<? extends ComboItem> list, 
				ComboItem value, int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			} 
			if (! value.isEnabled()) {
				setBackground(list.getBackground());
				setForeground(UIManager.getColor("Label.disabledForeground"));
			}
			setFont(list.getFont());
			setText((value == null) ? "" : value.toString());
			return this;
		}  
	}

	class ComboListener implements ActionListener {
		JComboBox<ComboItem> combo;
		Object currentItem;

		ComboListener(JComboBox<ComboItem> combo) {
			this.combo  = combo;
			combo.setSelectedIndex(0);
			currentItem = combo.getSelectedItem();
		}

		public void actionPerformed(ActionEvent e) {
			Object tempItem = combo.getSelectedItem();
			if (! ((CanEnable)tempItem).isEnabled()) {
				combo.setSelectedItem(currentItem);
			} else {
				currentItem = tempItem;
			}
		}
	}

	class ComboItem implements CanEnable {
		Object  obj;
		boolean isEnable;

		ComboItem(Object obj,boolean isEnable) {
			this.obj      = obj;
			this.isEnable = isEnable;
		}

		ComboItem(Object obj) {
			this(obj, true);
		}

		public boolean isEnabled() {
			return isEnable;
		}

		public void setEnabled(boolean isEnable) {
			this.isEnable = isEnable;
		}

		public String toString() {
			return obj.toString();
		}
	}

	public interface CanEnable {

		public void setEnabled(boolean isEnable);
		public boolean isEnabled();

	}

}
