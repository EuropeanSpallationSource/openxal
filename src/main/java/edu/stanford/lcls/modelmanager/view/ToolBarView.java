package edu.stanford.lcls.modelmanager.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.stanford.lcls.modelmanager.ModelManagerDocument;
import edu.stanford.lcls.modelmanager.ModelManagerWindow;
import edu.stanford.lcls.modelmanager.dbmodel.BrowserModel;
import edu.stanford.lcls.modelmanager.dbmodel.BrowserModel.RunState;
import edu.stanford.lcls.modelmanager.dbmodel.BrowserModelListener;
import edu.stanford.lcls.xal.model.RunModelConfiguration;
import edu.stanford.lcls.xal.model.RunModelConfigurationDesign;
import edu.stanford.lcls.xal.model.RunModelConfigurationExtant;
import edu.stanford.lcls.xal.model.RunModelConfigurationManual;
import edu.stanford.lcls.xal.model.RunModelConfigurationPVLogger;
import edu.stanford.slac.Message.Message;
import se.lu.esss.ics.jels.matcher.Matcher;
import se.lu.esss.ics.jels.matcher.MatcherDialog;
import xal.extension.widgets.apputils.SimpleProbeEditor;
import xal.model.ModelException;
import xal.service.pvlogger.apputils.browser.PVLogSnapshotChooser;

/**
 * QueryView is the view for querying the database for the machine models.
 * 
 * getInstance() @return the query view
 */

public class ToolBarView implements SwingConstants {
	private JToolBar toolBarView;
	private JFrame parent;
	private ModelManagerDocument document;
	private BrowserModel model;
	private JButton loadAccelerator;
	private JComboBox<String> beamlineSelector;
	
	/*private JComboBox<ComboItem> BPRPSelector; //Back Propagte Reference points
	private JComboBox<String> BPRPModeSelector;*/
	
	private JButton setInitTiwissButton;
	private JButton matcherButton; 
	private JButton resetInitialParametersButton;
	private JButton runWirescanner;
	
	private JComboBox<String> runModeSelector;
	private JButton editMachineParametersButton;
	private JButton resetMachineParametersButton;
	
	private JButton runModelButton;
	private JButton upload2DBButton;
	private JButton makeGoldButton;
	private JButton export2MADButton;
//	private JButton helpButton;
//	private JButton exitButton;
	private ModelListView modelListView;
	private Thread thread1;
	private Thread thread2;
	
	private PVLogSnapshotChooser plsc;
	private JDialog pvLogSelector;

//	private int bprpMethod;

	public ToolBarView(JFrame _parent, ModelManagerDocument _document, final ModelListView _modelListView) {
		parent = _parent;
		document = _document;
		model = _document.getModel();
		modelListView = _modelListView;
//		toolBarView = new JToolBar();
		toolBarView = ((ModelManagerWindow)_parent).getToolBar();
		
		Dimension small = new Dimension(5,5);
		Dimension big = new Dimension(10,10);
		
		/*
		    beamline selection 
		 
			initial parameters:
			Edit...
			Matching...
			Reset
			Run WS... (move wirescanner configuration to a dialog box/window)
			
			Machine parameters:
			Source:
			- design
			- extant
			- pvlogger
			- selected model
			Fetch
		 */
		
		
		// Accelerator loader
		loadAccelerator = new JButton("Load");
                loadAccelerator.setToolTipText("Load Accelerator.");
                loadAccelerator.setAlignmentY(0.3f);
                loadAccelerator.setEnabled(true);
                loadAccelerator.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
		                //AcceleratorActionFactory.loadAcceleratorAction( document ).actionPerformed( null );
                                AcceleratorSelector selector = new AcceleratorSelector(parent);
                                selector.setLocationRelativeTo(null);
                                selector.setVisible(true);

				if (selector.getSelectedAccelerator() != null) {
                                    document.setAccelerator(selector.getSelectedAccelerator(), selector.getAcceleratorPath());
                                }
                        }
                });
                toolBarView.add(loadAccelerator);
                toolBarView.addSeparator(small);
		
		// beamline selection
		final JPanel bs = new JPanel();
		bs.setOpaque(false);	
		bs.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Beamline:"));
		bs.setLayout(new BoxLayout(bs, BoxLayout.LINE_AXIS));
		
		beamlineSelector = new JComboBox<String>(model.getRunModel().getBeamlines().toArray(new String[]{}));
		beamlineSelector.setToolTipText("select a beamline");
		beamlineSelector.setMaximumSize(new Dimension(180, 28));
		beamlineSelector.setEnabled(false);
		beamlineSelector.setAlignmentX(0.f);
		bs.add(beamlineSelector);
		toolBarView.add(bs);
		
		// initial parameters
		final JPanel ip = new JPanel();
		ip.setOpaque(false);
		ip.setBorder(BorderFactory.createTitledBorder("Initial parameters"));
		ip.setLayout(new BoxLayout(ip, BoxLayout.LINE_AXIS));
		
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
		ip.add(setInitTiwissButton);
		ip.add(new JToolBar.Separator(small));
		
		matcherButton = new JButton("Matching...");
		matcherButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				Matcher matcher = new Matcher(model.getRunModel().getSequence(), model.getRunModel().getProbe());
				MatcherDialog md = new MatcherDialog(parent, matcher, true);			
			}
		});
		matcherButton.setEnabled(false);
		ip.add(matcherButton);		 
		ip.add(new JToolBar.Separator(small));
		
		resetInitialParametersButton = new JButton("Reset");
		ip.add(resetInitialParametersButton);
		ip.add(new JToolBar.Separator(small));
		resetInitialParametersButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				model.getRunModel().resetProbe();
			}
		});
		
		runWirescanner = new JButton("Run WS");
		runWirescanner.setEnabled(false);
		ip.add(runWirescanner);
		ip.add(new JToolBar.Separator(small));
		
		toolBarView.add(ip);
		toolBarView.addSeparator(big);
		
		
		// machine parameters
		
		final JPanel mp = new JPanel();
		mp.setOpaque(false);
		mp.setBorder(BorderFactory.createTitledBorder("Machine parameters"));
		mp.setLayout(new BoxLayout(mp, BoxLayout.LINE_AXIS));
		
		mp.add(new JLabel("Source:"));
		mp.add(new JToolBar.Separator(small));
		
		String[] runModeSelections = { "Design", "Extant", "PVLogger", "Selected model" };
		runModeSelector = new JComboBox<String>(runModeSelections);
		runModeSelector.setToolTipText("select to run either DESIGN or EXTANT model");
		runModeSelector.setMaximumSize(new Dimension(100, 28));		
		runModeSelector.setSelectedIndex(1);
		runModeSelector.setAlignmentX(0.f);
		
		mp.add(runModeSelector);
		mp.add(new JToolBar.Separator(big));
		
		 // TODO move into a dialog box 
		/*JPanel bp = new JPanel();
		bp.setBorder(BorderFactory.createTitledBorder("Back Prop. Twiss from"));
		bp.setLayout(new BoxLayout(bp, BoxLayout.LINE_AXIS));
		
		toolBarView.addSeparator(new Dimension(10, 10));

		final ComboItem[] refPts = { new ComboItem("WS000") }; 
		BPRPSelector = new JComboBox<ComboItem>(refPts);
		BPRPSelector.setToolTipText("select Twiss back propagate reference point");
		BPRPSelector.setMaximumSize(new Dimension(105, 28));
//		refID = refPts[BPRPSelector.getSelectedIndex()].toString();
//		bprpMethod = 0;

		BPRPSelector.setRenderer(new ComboRenderer());
		BPRPSelector.addActionListener(new ComboListener(BPRPSelector));
		
		
		BPRPModeSelector = new JComboBox<String>(new String[]{"Measured", "Design"});
		BPRPModeSelector.setToolTipText("select to use either Design or Measured Twiss at the reference point");
		BPRPModeSelector.setSelectedIndex(1);
		
		BPRPModeSelector.setMaximumSize(new Dimension(100, 28));
		
//		toolBarView.add(BPRPSelector);
		bp.add(BPRPSelector);
		bp.add(BPRPModeSelector);*/

		//toolBarView.add(bp);
		// System.out.println("Use reference pt.: " + emitNode +
				// " for Twiss.");
				//Message.info("Use reference pt.: " + emitNode + " for Twiss back propagate.");
				// do Twiss back propagate
		
		toolBarView.addPropertyChangeListener( "orientation", new PropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent e) {
                int o = ((Integer)e.getNewValue()).intValue();

                if (o == JToolBar.VERTICAL) {                
                	bs.setLayout(new BoxLayout(bs, BoxLayout.PAGE_AXIS));
                	ip.setLayout(new BoxLayout(ip, BoxLayout.PAGE_AXIS));                    
                    mp.setLayout(new BoxLayout(mp, BoxLayout.PAGE_AXIS));                
                } else {
                	bs.setLayout(new BoxLayout(bs, BoxLayout.LINE_AXIS));
                	ip.setLayout(new BoxLayout(ip, BoxLayout.LINE_AXIS));                    
                    mp.setLayout(new BoxLayout(mp, BoxLayout.LINE_AXIS));                    
                }
            }
        });
		
		runModeSelector.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {
				int runModelMethod = runModeSelector.getSelectedIndex();
				//BPRPSelector.setEnabled(runModelMethod == 1);
				//BPRPModeSelector.setEnabled(runModelMethod == 1);		
				if (runModelMethod == 2) {
					// show pvlogger selector
					if (pvLogSelector == null) {
						// for PV Logger snapshot chooser
						plsc = new PVLogSnapshotChooser();
						pvLogSelector = plsc.choosePVLogId();
					} else
						pvLogSelector.setVisible(true);
				}
			}
		});
		
		
		beamlineSelector.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {
				/*if(beamlineSelector.getSelectedIndex() == 0) {
					BPRPSelector.setEnabled(true);
					for (int i=0; i< refPts.length; i++)
						((ComboItem) refPts[i]).setEnabled(true);
				}*/
				setQueryViewEnable(false);
				modelListView.setModelListViewEnable(false);
				ModelStateView.getDataBaseState();
				ModelStateView.getMachineModelState().setText(
						"Trying to find models in the selected beam line...");
				ModelStateView.getProgressBar().setString("Loading ...");
				ModelStateView.getProgressBar().setIndeterminate(true);
				Message.info("Trying to find models in the selected beam line...");
				
				thread2 = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							model.setModelMode((String)beamlineSelector.getSelectedItem());
						} catch (SQLException exception) {
							Message.error("SQLException: " + exception.getMessage());			
							exception.printStackTrace();
						}
					}
				});
				thread2.start();
			}
		});


		//mp.addSeparator(new Dimension(5, 10));
		editMachineParametersButton = new JButton("Fetch and Edit");
		editMachineParametersButton.setToolTipText("Fetch and edit machine parameters.");
		mp.add(editMachineParametersButton);
		mp.add(new JToolBar.Separator(small));
		
		resetMachineParametersButton = new JButton("Reset");
		resetMachineParametersButton.setToolTipText("Reset machine parameters.");
		mp.add(resetMachineParametersButton);
		mp.add(new JToolBar.Separator(small));
		
		
		editMachineParametersButton.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {	
				try {
					if (runModeSelector.getSelectedIndex() == 3 && model.getSelectedMachineModelDevice() == null) {
						JOptionPane.showMessageDialog(parent, "You need to select a model.", "Run model Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					model.fetchRunData(getRunModelConfiguration());		
				} catch (ModelException | SQLException e1) {
					if (runModeSelector.getSelectedIndex() == 1) {
						String message = "Couldn't connect to get extant values.";
						JOptionPane.showMessageDialog(parent, message,
								"Run Model Error! See the xterm window for more details", JOptionPane.ERROR_MESSAGE);
					}
					e1.printStackTrace();
				}
			}
		});
		
		resetMachineParametersButton.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {	
				model.resetRunData();		
			}
		});
		
		
		
		toolBarView.add(mp);
		toolBarView.addSeparator(big);		
		

		runModelButton = new JButton("Run Model");
		runModelButton.setToolTipText("run XAL online model");
		runModelButton.setAlignmentY(0.3f);
		toolBarView.add(runModelButton);
		
		toolBarView.addSeparator(small);
		upload2DBButton = new JButton("Save");
		upload2DBButton.setToolTipText("save the model data in memory to database");
		upload2DBButton.setEnabled(false);
		upload2DBButton.setAlignmentY(0.3f);
		toolBarView.add(upload2DBButton);
		
		toolBarView.addSeparator(small);
		makeGoldButton = new JButton("Make SEL Gold");
		makeGoldButton.setToolTipText("tag the selected model (in SEL column from the table) as the GOLD one");
		makeGoldButton.setAlignmentY(0.3f);
		toolBarView.add(makeGoldButton);
		makeGoldButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				thread1 = new Thread(new Runnable() {
					@Override
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
					@Override
					public void run() {
						try {
							if (model.getSelectedMachineModel() == null)
								JOptionPane
										.showMessageDialog(
												parent,
												"You need to select a machine model from SEL column first!",
												"Make Gold Error", JOptionPane.ERROR_MESSAGE);
							else if (model.getSelectedMachineModel().getPropertyValue("ID").equals("RUN"))
								JOptionPane.showMessageDialog(parent,
										"You need to save the RUN machine model first!",
										"Make Gold Error", JOptionPane.ERROR_MESSAGE);
							else {
								final String commentText = showGoldCommentDialogBox();
								
								model.makeGold(commentText);
								ModelStateView.getMachineModelState().setText("The selected machine model has been set to GOLD !");
								ModelStateView.getProgressBar().setString("Done !");
								Message.info("The selected machine model has been set to GOLD!");
							}
			
							modelListView.connectDefault();
						} catch (SQLException exception) {				
							Message.error("SQL Exception: " + exception.getMessage(), true);			
							Message.error("Gold tag operation failed!", true);			
							JOptionPane.showMessageDialog(parent, exception.getMessage(),
									"SQL Error: Gold tag operation failed!", JOptionPane.ERROR_MESSAGE);
							Logger.getLogger("global").log(Level.SEVERE,
									"SQL Error: Gold tag operation failed!", exception);
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
						ModelStateView.getProgressBar().setIndeterminate(false);
						setQueryViewEnable(true);
					}
					});
				thread1.start();
				thread2.start();
			}
		});
		
		toolBarView.addSeparator(small);
		export2MADButton = new JButton("Export to MAD");
		export2MADButton.setEnabled(false);
		//TODO to be added later for exporting to MAD file
//		toolBarView.add(export2MADButton);
		
		toolBarView.add(Box.createGlue());
		toolBarView.addSeparator(big);
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
			@Override
			public void actionPerformed(ActionEvent event) {
				if (runModeSelector.getSelectedIndex() == 3 && model.getSelectedMachineModelDevice() == null) {
					JOptionPane.showMessageDialog(parent, "You need to select a model.", "Run model Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				thread1 = new Thread(new Runnable() {
					@Override
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
					@Override
					public void run() {
						RunModelConfiguration config = null;
						try {
							// if prefetched data
							if (model.getRunState().equals(BrowserModel.RunState.FETCHED_DATA)) {
								config = new RunModelConfigurationManual(model.getRunMachineModelDevice());
							} else {
								config = getRunModelConfiguration();
							}
							
							model.runModel(config);
							
							ModelStateView.getMachineModelState().setText("The new XAL machine model's ID is \"RUN\" !");
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
                            if(config instanceof RunModelConfigurationExtant) { 
                                message = "Couldn't connect to get extant values.";
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
			@Override
			public void actionPerformed(ActionEvent event) {
				thread1 = new Thread(new Runnable() {
					@Override
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
					@Override
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
			@Override
			public void actionPerformed(ActionEvent event) {
				thread2 = new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println("Save start!");
						upload2DBButton.setEnabled(false);
						runModelButton.setEnabled(false);
						ModelStateView.getMachineModelState().setText(
						"Application is uploading the model data to database...");
						ModelStateView.getProgressBar().setString("Uploading ...");
						ModelStateView.getProgressBar().setIndeterminate(true);
						Message.info("Uploading model RUN data to database...");;
						
						String uploadID= model.uploadToDatabase(parent);
						
						if (uploadID == null) {
							Message.error("Application failed to correctly upload the model data !");
						} else {
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
				thread2.start();
			}
		});
		
		model.addBrowserModelListener(new BrowserModelListener() {
			@Override
			public void modelStateChanged(BrowserModel model, BrowserModelAction action) {
				if (model.getStateReady()) setQueryViewEnable(true);		
				if (action.equals(BrowserModelAction.ACC_LOAD)) {
					beamlineSelector.setModel(new DefaultComboBoxModel(
						model.getRunModel().getBeamlines().toArray(new String[]{})));
				}
			}
		});
		
		// This is a trick to move the "Help" button from the first to the right place.
		Component comp1 = toolBarView.getComponentAtIndex(0);
		Component comp2 = toolBarView.getComponentAtIndex(1);
		toolBarView.remove(comp1);
		toolBarView.remove(comp2);
		
		((JButton)comp1).setAlignmentY(0.3f);
		((JButton)comp2).setAlignmentY(0.3f);
		toolBarView.add(comp1);
		toolBarView.addSeparator(small);
		toolBarView.add(comp2);
	}

	public Container getInstance() {
		return toolBarView;
	}
	
	public void setQueryViewEnable(boolean enabled) {
		/*if (!((String)runModeSelector.getSelectedItem()).equals("Design")) {
			BPRPModeSelector.setEnabled(enabled);
			BPRPSelector.setEnabled(enabled);
		} else {
			BPRPModeSelector.setEnabled(false);
			BPRPSelector.setEnabled(false);			
		}*/
		beamlineSelector.setEnabled(enabled);
		
		setInitTiwissButton.setEnabled(enabled);
		matcherButton.setEnabled(enabled);
		resetInitialParametersButton.setEnabled(enabled);
		runWirescanner.setEnabled(false);
		
		runModeSelector.setEnabled(enabled && !model.getRunState().equals(RunState.FETCHED_DATA));
		editMachineParametersButton.setEnabled(enabled && !model.getRunState().equals(RunState.FETCHED_DATA));
		resetMachineParametersButton.setEnabled(enabled && model.getRunState().equals(RunState.FETCHED_DATA));
		
		runModelButton.setEnabled(enabled);
		upload2DBButton.setEnabled(enabled && model.getRunMachineModel()!=null);
		makeGoldButton.setEnabled(enabled && model.getSelectedMachineModel() != null);
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

	
	private String showGoldCommentDialogBox() {
		final JDialog goldComment = new JDialog(parent, "Gold Machine Model Comment", true);
		goldComment.getContentPane().add(new JLabel("Enter the Comment:"), BorderLayout.NORTH);
		final JTextField commentText = new JTextField();
		commentText.setPreferredSize(new Dimension(500, 26));
		goldComment.getContentPane().add(commentText, BorderLayout.CENTER);
		JPanel buttonPane = new JPanel();
		JButton commitComment = new JButton("OK");
		buttonPane.add(commitComment);
		goldComment.getContentPane().add(buttonPane, BorderLayout.SOUTH);
		commitComment.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				goldComment.dispose();
			}
		});
		goldComment.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		goldComment.pack();
		Dimension parentSize = parent.getSize();
		Dimension dialogSize = goldComment.getSize();
		Point p = parent.getLocation();
		goldComment.setLocation(p.x + parentSize.width / 2 - dialogSize.width/2, p.y + parentSize.height / 2 - dialogSize.height/2);
		goldComment.setVisible(true);
		return commentText.getText();
	}
	
	private int showFetchMachineConfigDialogBox() {
		final JDialog fetchMachineConfig = new JDialog(parent, "Fetch machine parameters for manual configuration", true);
		fetchMachineConfig.getContentPane().add(new JLabel("Select the source of machine parameters:"), BorderLayout.NORTH);
		final JComboBox<String> dataSource = new JComboBox<>(new String[]{"Design", "Extant", "PVLogger", "Selected model"});
		
		fetchMachineConfig.getContentPane().add(dataSource, BorderLayout.CENTER);
		JPanel buttonPane = new JPanel();
		JButton fetchButton = new JButton("Fetch");
		buttonPane.add(fetchButton);
		fetchMachineConfig.getContentPane().add(buttonPane, BorderLayout.SOUTH);
		fetchButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				fetchMachineConfig.dispose();
			}
		});
		fetchMachineConfig.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		fetchMachineConfig.pack();
		Dimension parentSize = parent.getSize();
		Dimension dialogSize = fetchMachineConfig.getSize();
		Point p = parent.getLocation();
		fetchMachineConfig.setLocation(p.x + parentSize.width / 2 - dialogSize.width/2, p.y + parentSize.height / 2 - dialogSize.height/2);
		fetchMachineConfig.setVisible(true);
		return dataSource.getSelectedIndex();
	}	

	private RunModelConfiguration getRunModelConfiguration() {
		RunModelConfiguration config;
		if (runModeSelector.getSelectedIndex() == 1) {
			config = new RunModelConfigurationExtant();			
		} else if (runModeSelector.getSelectedIndex() == 2) {
			config = new RunModelConfigurationPVLogger(plsc.getPVLogId());								
		} else if (runModeSelector.getSelectedIndex() == 3) { 
			config = new RunModelConfigurationManual(model.getSelectedMachineModelDevice());
		} else {
			config = new RunModelConfigurationDesign();					
		}
		return config;
	}
	
	@SuppressWarnings("serial")
	class ComboRenderer extends JLabel implements ListCellRenderer<ComboItem> {

		public ComboRenderer() {
			setOpaque(true);
			setBorder(new EmptyBorder(1, 1, 1, 1));
		}

		@Override
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

		@Override
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

		@Override
		public boolean isEnabled() {
			return isEnable;
		}

		@Override
		public void setEnabled(boolean isEnable) {
			this.isEnable = isEnable;
		}

		@Override
		public String toString() {
			return obj.toString();
		}
	}

	public interface CanEnable {

		public void setEnabled(boolean isEnable);
		public boolean isEnabled();

	}

}
