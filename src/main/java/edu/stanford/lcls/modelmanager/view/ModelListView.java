package edu.stanford.lcls.modelmanager.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import edu.stanford.lcls.modelmanager.dbmodel.BrowserModel;
import edu.stanford.lcls.modelmanager.dbmodel.BrowserModelListener;
import edu.stanford.lcls.modelmanager.dbmodel.DataManager;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModel;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDetail;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelDevice;
import edu.stanford.lcls.modelmanager.dbmodel.MachineModelTableModel;
import edu.stanford.slac.Message.Message;

public class ModelListView {
	private JPanel modelListView;
	private JFrame parent;
	protected BrowserModel model;
	private JSpinner fromSpinner;
	private JSpinner toSpinner;
	private Calendar toDate;
	private JCheckBox setEndTime;
	private JButton findButton;
	private JButton refreshDBButton;
	private JTable modelTable;
	private String referenceMachineModelID;
	private String selectedMachineModelID;
	private Thread thread1;
	private Thread thread2;
	private Connection connection;
	final SpinnerDateModel fromDateModel = new SpinnerDateModel();
	
	public ModelListView(final JFrame parent, final BrowserModel model) {
		this.parent = parent;
		this.model = model;
		modelListView = new JPanel(new BorderLayout());
		modelListView.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 0));

		JToolBar buttonView = new JToolBar();
		buttonView.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
		modelListView.add(buttonView, BorderLayout.PAGE_START);
		buttonView.add(new JLabel("Saved Models:"));
		buttonView.add(Box.createGlue());
		
		buttonView.addSeparator(new Dimension(10, 10));
		buttonView.add(new JLabel("From:"));
		buttonView.addSeparator(new Dimension(5, 10));
		// go back 1 month
		Calendar fromDate = Calendar.getInstance();
		fromDate.add(Calendar.MONTH, -1);
		fromDateModel.setValue(fromDate.getTime());
		fromSpinner = new JSpinner(fromDateModel);
		fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner,
				"MMM dd, yyyy HH:mm:ss"));
		fromSpinner.setMaximumSize(new Dimension(200, 25));
		buttonView.add(fromSpinner);
		buttonView.addSeparator(new Dimension(10, 10));
		buttonView.add(new JLabel("To:"));
		buttonView.addSeparator(new Dimension(5, 10));
		final JLabel now = new JLabel(" Now ");
		buttonView.add(now);
		final SpinnerDateModel toDateModel = new SpinnerDateModel();
		// look ahead an hour
		toDate = Calendar.getInstance();
		toDateModel.setValue(toDate.getTime());
		toSpinner = new JSpinner(toDateModel);
		toSpinner.setEditor(new JSpinner.DateEditor(toSpinner,
				"MMM dd, yyyy HH:mm:ss"));
		toSpinner.setMaximumSize(new Dimension(200, 25));
		toSpinner.setVisible(false);
		buttonView.add(toSpinner);
		
		buttonView.addSeparator(new Dimension(10, 10));
		setEndTime = new JCheckBox("Set End Time");
		setEndTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				now.setVisible(!setEndTime.isSelected());
				toDate = Calendar.getInstance();
				toDateModel.setValue(toDate.getTime());
				toSpinner.setVisible(setEndTime.isSelected());
				toSpinner.setEnabled(setEndTime.isSelected());
			}
		});
		buttonView.add(setEndTime);
		
		buttonView.addSeparator(new Dimension(10, 10));
		findButton = new JButton("Find");
		findButton.setToolTipText("search for models within the specified time range");
		buttonView.add(findButton);
		findButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				thread1 = new Thread(new Runnable() {
					public void run() {
						setModelListViewEnable(false);
						//modelListView.setModelListViewEnable(false);
						ModelStateView.getDataBaseState();
						ModelStateView.getMachineModelState().setText(
								"Trying to find models in the specified time range...");
						ModelStateView.getProgressBar().setString("Loading ...");
						ModelStateView.getProgressBar().setIndeterminate(true);
						Message.info("Application is querying the database for available models within the specified time range.");
					}
				});
				thread2 = new Thread(new Runnable() {
					public void run() {
						Date startDate = fromDateModel.getDate();
						Date endDate;
						if (setEndTime.isSelected())
							endDate = toDateModel.getDate();
						else
							endDate = Calendar.getInstance().getTime();
						findMachineModelInRange(startDate, endDate, model.getModelMode());
					}
				});
				thread1.start();
				thread2.start();
				
				// for test purpose
				//model.removeRunModelFromFetchedModels();
			}
		});
		
		buttonView.addSeparator(new Dimension(5, 10));
		refreshDBButton = new JButton("Restore to Default");
		refreshDBButton.setToolTipText("reset the application to default state");
		buttonView.add(refreshDBButton);
		refreshDBButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				thread1 = new Thread(new Runnable() {
					public void run() {
						setModelListViewEnable(false);
						//modelListView.setModelListViewEnable(false);
						ModelStateView.getDataBaseState();
						ModelStateView.getMachineModelState().setText(
								"Restoring the default view of the application...");
						ModelStateView.getProgressBar().setString("Loading ...");
						ModelStateView.getProgressBar().setIndeterminate(true);
						Message.info("Restoring the default view of the application...");
					}
				});
				thread2 = new Thread(new Runnable() {
					public void run() {
						connectDefault();
						ModelStateView.getMachineModelState().setText(
						"Default view of the application restored.");
						Message.info("Default view of the application restored.");
					}
				});
				thread1.start();
				thread2.start();
			}
		});		
		
		Box tableView = new Box(BoxLayout.Y_AXIS);
		modelListView.add(tableView, BorderLayout.CENTER);
		final MachineModelTableModel machineModelTableModel = new MachineModelTableModel();
		model.addBrowserModelListener(machineModelTableModel);
		modelTable = new JTable(machineModelTableModel);

		// Sort by Table Head
		TableSorter sorter = new TableSorter();
		sorter.setTableHeader(modelTable.getTableHeader());
		sorter.setTableModel(machineModelTableModel);
		modelTable.setModel(sorter);

		for (int columnIndex = 0; columnIndex < modelTable.getColumnCount(); columnIndex++) {
			TableColumn tableColumn = modelTable.getColumnModel().getColumn(
					columnIndex);
			switch (columnIndex) {
			case 0:
				tableColumn.setMinWidth(30);
				tableColumn.setMaxWidth(50);
				tableColumn.setPreferredWidth(40);
				break;
			case 1:
				tableColumn.setMinWidth(80);
				tableColumn.setMaxWidth(180);
				tableColumn.setPreferredWidth(100);
				break;
			case 2:
				tableColumn.setMinWidth(50);
				tableColumn.setMaxWidth(60);
				tableColumn.setPreferredWidth(55);
				break;
			case 3:
				tableColumn.setMinWidth(15);
				tableColumn.setMaxWidth(30);
				tableColumn.setPreferredWidth(20);
				break;
			case 4:
				tableColumn.setMinWidth(50);
				tableColumn.setMaxWidth(2000);
				tableColumn.setPreferredWidth(80);
				break;
			case 5:
				tableColumn.setMinWidth(50);
				tableColumn.setMaxWidth(80);
				tableColumn.setPreferredWidth(60);
				break;
			case 6:
				tableColumn.setMinWidth(20);
				tableColumn.setMaxWidth(40);
				tableColumn.setPreferredWidth(30);
				break;
			case 7:
				tableColumn.setMinWidth(20);
				tableColumn.setMaxWidth(40);
				tableColumn.setPreferredWidth(30);
				break;
			}
		}

		modelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableView.add(modelTable.getTableHeader());
		tableView.add(new JScrollPane(modelTable));
		setModelListViewEnable(false);

		modelTable.getTableHeader().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				modelTable.setDefaultRenderer(Object.class,
						new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 1L;
					public Component getTableCellRendererComponent(
							JTable table, Object value,	boolean isSelected,
							boolean hasFocus, int row, int column) {
						Component cell = super
						.getTableCellRendererComponent(
								modelTable, value,
								isSelected,
								hasFocus, row,
								column);
						if (modelTable.getValueAt(row, 0).toString()
								.equals(selectedMachineModelID))
							cell.setBackground(Color.GREEN);
						else if(modelTable.getValueAt(row, 5).toString()
								.equals("PRESENT"))
							cell.setBackground(Color.ORANGE);
						else if (modelTable.getValueAt(row, 0).toString()
								.equals(referenceMachineModelID))
							cell.setBackground(Color.CYAN);
						else
							cell.setBackground(Color.WHITE);
						return cell;
						}
					});
				modelTable.repaint();
			}
		});


		

		model.addBrowserModelListener(new BrowserModelListener() {
			public void connectionChanged(BrowserModel model) {
			}
			public void machineModelFetched(BrowserModel model,
					MachineModel[] fetchedMachineModel, MachineModel referenceMachineModel,
					MachineModelDetail[] referenceMachineModelDetail,
					MachineModelDevice[] referenceMachineModelDevice) {
				referenceMachineModelID = referenceMachineModel.getPropertyValue("ID").toString();
				modelTable.setDefaultRenderer(Object.class,
						new DefaultTableCellRenderer() {
							private static final long serialVersionUID = 1L;
							public Component getTableCellRendererComponent(
									JTable table, Object value,
									boolean isSelected, boolean hasFocus,
									int row, int column) {
								Component cell = super
										.getTableCellRendererComponent(
												modelTable, value, isSelected,
												hasFocus, row, column);
								if(modelTable.getValueAt(row, 5).toString()
										.equals("PRESENT"))
									cell.setBackground(Color.ORANGE);
								else if (modelTable.getValueAt(row, 0).toString()
										.equals(referenceMachineModelID))
									cell.setBackground(Color.CYAN);
								else
									cell.setBackground(Color.WHITE);
								return cell;
								}
						});
				setModelListViewEnable(true);
				modelTable.repaint();
			}
			public void modelSelected(BrowserModel model,
					MachineModel selectedMachineModel,
					MachineModelDetail[] machineModelDetail,
					MachineModelDevice[] machineModelDevice) {
				referenceMachineModelID = model.getReferenceMachineModel().getPropertyValue("ID").toString();
				selectedMachineModelID = selectedMachineModel.getPropertyValue("ID").toString();				
				modelTable.setDefaultRenderer(Object.class,
						new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 1L;
					public Component getTableCellRendererComponent(
							JTable table, Object value,	boolean isSelected,
							boolean hasFocus, int row, int column) {
						Component cell = super
						.getTableCellRendererComponent(
								modelTable, value,
								isSelected,
								hasFocus, row,
								column);
						if (modelTable.getValueAt(row, 0).toString()
								.equals(selectedMachineModelID))
							cell.setBackground(Color.GREEN);
						else if(modelTable.getValueAt(row, 5).toString()
								.equals("PRESENT"))
							cell.setBackground(Color.ORANGE);
						else if (modelTable.getValueAt(row, 0).toString()
								.equals(referenceMachineModelID))
							cell.setBackground(Color.CYAN);						
						else
							cell.setBackground(Color.WHITE);
						return cell;
						}
					});
				modelTable.repaint();
			}
			public void runModel(BrowserModel model,
					MachineModel[] fetchedMachineModel,
					MachineModel runMachineModel,
					MachineModelDetail[] runMachineModelDetail,
					MachineModelDevice[] runMachineModelDevice){
				selectedMachineModelID = runMachineModel.getPropertyValue("ID").toString();				
				modelTable.setDefaultRenderer(Object.class,
						new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 1L;
					public Component getTableCellRendererComponent(
							JTable table, Object value,	boolean isSelected,
							boolean hasFocus, int row, int column) {
						Component cell = super.getTableCellRendererComponent(
								modelTable, value,
								isSelected,
								hasFocus, row,
								column);
						if (modelTable.getValueAt(row, 0).toString()
								.equals(selectedMachineModelID))
							cell.setBackground(Color.GREEN);
						else if(modelTable.getValueAt(row, 5).toString()
								.equals("PRESENT"))
							cell.setBackground(Color.ORANGE);
						else if (modelTable.getValueAt(row, 0).toString()
								.equals(referenceMachineModelID))
							cell.setBackground(Color.CYAN);
						else
							cell.setBackground(Color.WHITE);
						return cell;
						}
					});
				modelTable.repaint();
			}
		});
	}

	public void setModelListViewEnable(boolean enabled) {
		fromSpinner.setEnabled(enabled);
		toSpinner.setEnabled(enabled && setEndTime.isSelected());
		setEndTime.setEnabled(enabled);
		findButton.setEnabled(enabled);
		refreshDBButton.setEnabled(enabled);
		modelTable.setEnabled(enabled);
		modelTable.setVisible(enabled);
	}
	
	public void findAllMachineModel() {
		try {
			model.fetchAllMachineModel();
		} catch (Exception exception) {
			Message.error("Database Exception: " + exception.getMessage());			
			displayError("Database Exception", "Exception fetching snapshots:",
					exception);
		}
	}

	public void findMachineModelInRange(Date startDate, Date endDate, int modelModesID) {
		try {
			model.fetchMachineModelInRange(startDate, endDate, modelModesID);
		} catch (Exception exception) {
			displayError("Database Exception", "Exception fetching MachineModels:",
					exception);
			ModelStateView.getMachineModelState().setText("Failed to find any machine models in the specified time range.");
			ModelStateView.getProgressBar().setString("Connection failed!");
			ModelStateView.getProgressBar().setIndeterminate(false);
			setModelListViewEnable(true);
			Message.error("Failed to find any machine models in the specified time range.");
		}
		setModelListViewEnable(true);
	}

	public void requestUserConnection() {
		
		connection = null;
		
		thread1 = new Thread(new Runnable() {
			public void run() {
				setModelListViewEnable(false);
				ModelStateView.getDataBaseState().setText("Trying to connect to the Database...");
				ModelStateView.getMachineModelState().setText("Application is fetching machine models...");
				ModelStateView.getProgressBar().setString("Waiting ...");
				ModelStateView.getProgressBar().setIndeterminate(true);
				Message.info("Trying to connect to the selected Database and load machine models...");
				}
			});
		thread2 = new Thread(new Runnable() {
			public void run() {		
		
		try {
			connection = DataManager.getConnection();
			// connection = DriverManager.getConnection(DataManager.getUrl());		
		} catch (Exception exception) {
	//		Message.error("Exception: " + exception.getMessage());			
			ModelStateView.getProgressBar().setIndeterminate(false);
			ModelStateView.getProgressBar().setString("Connection failed!");
			JOptionPane.showMessageDialog(parent, exception.getMessage(),
					"Connection Error!", JOptionPane.ERROR_MESSAGE);
			Logger.getLogger("global").log(Level.SEVERE,
					"Database connection error.", exception);
			// exception.printStackTrace();
			setModelListViewEnable(true);
		}
		// ModelStateView.getMachineModelState().setText("Application is fetching machine models... ");
		// Message.info("Application is fetching machine models...");
		if (connection != null) {
			try {
				model.setDatabaseConnection(connection);
				//modelStateView.getProgressBar().setString("Loading Success !");
			} catch (Exception exception) {
				//modelStateView.getProgressBar().setIndeterminate(false);
				//modelStateView.getProgressBar().setString("Loading failed !");
				Message.error("SQL Exception: " + exception.getMessage());			
				JOptionPane.showMessageDialog(parent, exception.getMessage(),
						"SQL Error!", JOptionPane.ERROR_MESSAGE);
				Logger.getLogger("global").log(Level.SEVERE,
						"Database SQL error.", exception);
				exception.printStackTrace();
			}
		} else{
			ModelStateView.getDataBaseState().setText("Connection change cancelled.");
			ModelStateView.getMachineModelState().setText("Connection change cancelled.");
			ModelStateView.getProgressBar().setString("Connection change cancelled.");
			Message.info("Connection change cancelled.");
			setModelListViewEnable(true);
		}
		ModelStateView.getProgressBar().setIndeterminate(false);
			}
		});
		thread1.start();
		thread2.start();
	}

	public void connectDefault() {
		requestUserConnection();		
		if (connection != null) {
			try {
				model.setDatabaseConnection(connection);
			} catch (SQLException exception) {
				Message.error("SQLException: " + exception.getMessage());			
				JOptionPane.showMessageDialog(parent, exception.getMessage(),
						"SQL Error!", JOptionPane.ERROR_MESSAGE);
				Logger.getLogger("global").log(Level.SEVERE,
						"Database SQL error.", exception);
				exception.printStackTrace();
			} catch (ParseException exception) {
				Message.error("Parse Exception: " + exception.getMessage());			
				JOptionPane.showMessageDialog(parent, exception.getMessage(),
						"Date Format Error!", JOptionPane.ERROR_MESSAGE);
				Logger.getLogger("global").log(Level.SEVERE,
						"Machine Model Date Format SQL error.", exception);
				exception.printStackTrace();
				}
			}
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

	public Container getInstance() {
		return modelListView;
	}
	
	public SpinnerDateModel getFromDateModel() {
		return fromDateModel;
	}
	
	public BrowserModel getBrowserModel() {
		return model;
	}
	
	public JTable getModelTable() {
		return modelTable;
	}

}
