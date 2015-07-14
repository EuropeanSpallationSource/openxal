package edu.stanford.lcls.modelmanager.dbmodel;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

import xal.model.ModelException;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.tools.data.DataAdaptor;
import xal.tools.messaging.MessageCenter;
import xal.tools.xml.XmlDataAdaptor;
//import edu.stanford.lcls.xal.model.RunModel;
//import edu.stanford.lcls.xal.tools.ca.ConnectionManager;
import edu.stanford.lcls.modelmanager.view.ModelPlotData;
import edu.stanford.lcls.modelmanager.view.ModelStateView;
import edu.stanford.lcls.xal.model.RunModel;
import edu.stanford.lcls.xal.model.RunModelConfiguration;
import edu.stanford.slac.Message.Message;

/**
 * BrowserModel is the main document model.
 * 
 * @author 
 */
public class BrowserModel {


	final protected MessageCenter MESSAGE_CENTER;
	final protected BrowserModelListener EVENT_PROXY;
	final static public SimpleDateFormat machineModelDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	protected PersistentStore PERSISTENT_STORE;
	protected Connection _connection;
	protected boolean _hasConnected = false;
	protected String _user="MACHINE_MODEL";
	protected String _databaseURL;
	
	protected MachineModel[] _allMachineModels;
	protected MachineModel[] _fetchedMachineModels;
	protected MachineModel[] _goldMachineModels;
	
	protected String _referenceMachineModelID;
	protected MachineModel _referenceMachineModel;
	protected MachineModelDetail[] _referenceMachineModelDetail;
	protected MachineModelDevice[] _referenceMachineModelDevice;
	
	protected String _selectedMachineModelID;
	protected MachineModel _selectedMachineModel;
	protected MachineModelDetail[] _selectedMachineModelDetail;
	protected MachineModelDevice[] _selectedMachineModelDevice;
	
	protected MachineModel _runMachineModel;
	protected MachineModelDetail[] _runMachineModelDetail;
	protected MachineModelDevice[] _runMachineModelDevice;
	
	protected MachineModel _goldMachineModel;
	protected MachineModelDetail[] _goldMachineModelDetail;
	
	private int plotFunctionID1;
	private int plotFunctionID2;
	
	protected RunModel rm;
	
	private final String autoRunID = "RUN";
	protected List<Integer> modelModes = new ArrayList<Integer>(Arrays
			.asList(new Integer[] { 5, 53, 52, 51 }));
	protected List<String> modelModesName = new ArrayList<String>(Arrays
			.asList(new String[] { "CATHODE to Main Dump", "CATHODE to 52SL2",
					"CATHODE to 135-Mev SPECT DUMP",
					"CATHODE to GUN SPECT DUMP" }));
	protected int modelMode = 5;
	protected boolean isGold;
	protected Accelerator acc;
	
	protected boolean stateReady = false;
	protected RunState runState = RunState.NONE;
	
	public static enum RunState {
		NONE, FETCHED_DATA, RUN
	}
	
	
	/**
	 * Constructor
	 */
	public BrowserModel(Accelerator acc) {
		this.acc = acc;
		rm = new RunModel(acc);
		MESSAGE_CENTER = new MessageCenter("Browser Model");
		EVENT_PROXY = MESSAGE_CENTER.registerSource(this,
				BrowserModelListener.class);
	
		// The following object instantiation is unnecessary.
//		rm = new RunModel(modelMode);
	}
	
	public boolean getStateReady() {
		return stateReady;
	}

	/**
	 * Add a listener of model events from this model.
	 * 
	 * @param listener
	 *            the listener to add for receiving model events.
	 */
	public void addBrowserModelListener(final BrowserModelListener listener) {
		MESSAGE_CENTER.registerTarget(listener, this,
				BrowserModelListener.class);
	}

	/**
	 * Remove the listener from receiving model events from this model.
	 * 
	 * @param listener
	 *            the listener to remove from receiving model events.
	 */
	public void removeBrowserModelListener(final BrowserModelListener listener) {
		MESSAGE_CENTER.removeTarget(listener, this, BrowserModelListener.class);
	}

	/**
	 * Set the database connection to the one specified.
	 * 
	 * @param connection
	 *            the new database connection
	 * @throws ParseException
	 * @throws SQLException
	 */
	public void setDatabaseConnection(final Connection connection) throws SQLException,
			ParseException {
		_hasConnected = false;
		//_user = null;
		_databaseURL = null;
		_connection = connection;
		_hasConnected = true;
		_databaseURL = DataManager.url;
		final URL configurationURL = getClass()
				.getResource("configuration.xml");
		final DataAdaptor configurationAdaptor = XmlDataAdaptor.adaptorForUrl(
				configurationURL, false).childAdaptor("Configuration");
		final DataAdaptor persistentStoreAdaptor = configurationAdaptor
				.childAdaptor("persistentStore");
		PERSISTENT_STORE = new PersistentStore(persistentStoreAdaptor);
		
		EVENT_PROXY.modelStateChanged(this);
		fetchAllMachineModel();
	}	

	public String getDataBaseURL() {
		return _databaseURL;
	}

	public String getConnectUser() {
		return _user;
	}

	public boolean hasConnected() {
		return _hasConnected;
	}


	/**
	 * Get the array of machine model that had been fetched.
	 */
	public MachineModel[] getAllMachineModel() {
		return _allMachineModels;
	}

	public MachineModel[] getFetchedMachineModel() {
		return _fetchedMachineModels;
	}

	public MachineModel getReferenceMachineModel() {
		return _referenceMachineModel;
	}

	public MachineModel getSelectedMachineModel() {
		return _selectedMachineModel;
	}

	public MachineModel getRunMachineModel() {
		return _runMachineModel;
	}
	
	public MachineModel getGoldMachineModel() {
		return _goldMachineModel;
	}

	public MachineModel[] getGoldMachineModels() {
		return _goldMachineModels;
	}

	public MachineModelDetail[] getSelectedMachineModelDetail() {
		return _selectedMachineModelDetail;
	}

	public MachineModelDevice[] getSelectedMachineModelDevice() {
		return _selectedMachineModelDevice;
	}
	
	public MachineModelDetail[] getReferenceMachineModelDetail() {
		return _referenceMachineModelDetail;
	}
	
	public MachineModelDevice[] getReferenceMachineModelDevice() {
		return _referenceMachineModelDevice;
	}
	
	public MachineModelDevice[] getRunMachineModelDevice() {
		return _runMachineModelDevice;
	}
	
	public void setPlotFunctionID1(int plotFunctionID1) {
		this.plotFunctionID1 = plotFunctionID1;
	}

	public int getPlotFunctionID1() {
		return plotFunctionID1;
	}

	public RunModel getRunModel() {
		return rm;
	}

	public void setPlotFunctionID2(int plotFunctionID2) {
		this.plotFunctionID2 = plotFunctionID2;
	}

	public int getPlotFunctionID2() {
		return plotFunctionID2;
	}

	public int getModelMode() {
		return modelMode;
	}

	/** beamline selection */
	public void setModelMode(int _modelMode) throws SQLException {
		modelMode = _modelMode;
		filterMachineModelInMode(_allMachineModels, _goldMachineModels,
				modelMode);
	}

	public boolean isGold() {
		return isGold;
	}
	
	/**
	 * Fetch the machine models
	 */

	public void fetchMachineModelInRange(final java.util.Date startTime,
			final java.util.Date endTime, final int modelMode)
			throws SQLException {
		MachineModel[] _machineModel_tmp1 = PERSISTENT_STORE
				.fetchMachineModelsInRange(_connection, startTime, endTime);
		MachineModel[] _machineModel_tmp2 = new MachineModel[_machineModel_tmp1.length
				+ _goldMachineModels.length];
		// If goldMachineModel isn't in, add it into the _machineModel
		int index = 0;
		for (int i = 0; i < _machineModel_tmp1.length; i++) {
			_machineModel_tmp2[index] = _machineModel_tmp1[i];
			index++;
		}
		for (int j = 0; j < _goldMachineModels.length; j++) {
			if (_goldMachineModels[j] != null) {
				String goldMachineModelID = (String) _goldMachineModels[j]
						.getPropertyValue("ID");
				boolean isIncludeLastDesignMachineModel = false;
				for (int i = 0; i < _machineModel_tmp1.length; i++) {
					if (_machineModel_tmp1[i].getPropertyValue("ID").equals(
							goldMachineModelID))
						isIncludeLastDesignMachineModel = true;
				}
				if (!isIncludeLastDesignMachineModel) {
					_machineModel_tmp2[index] = _goldMachineModels[j];
					index++;
				}
			}
		}
		_allMachineModels = new MachineModel[index];
		for (int i = 0; i < index; i++) {
			_allMachineModels[i] = _machineModel_tmp2[i];
		}
		filterMachineModelInMode(_allMachineModels, _goldMachineModels,
				modelMode);
	}

	public void fetchAllMachineModel() throws SQLException, ParseException {
		_allMachineModels = PERSISTENT_STORE.fetchAllMachineModels(_connection);
		_goldMachineModels = fetchGoldMachineModel(_allMachineModels);
		filterMachineModelInMode(_allMachineModels, _goldMachineModels,
				modelMode);
	}

	/**
	 * get the last design machine model
	 */
	public MachineModel[] fetchGoldMachineModel(MachineModel[] allMachineModel)
			throws ParseException, SQLException {
		List<MachineModel> goldMachineModels = new ArrayList<MachineModel>();
		for (int i = 0; i < allMachineModel.length; i++) {
			if (allMachineModel[i].getPropertyValue("GOLD").equals("PRESENT"))
				goldMachineModels.add(allMachineModel[i]);
		}
		boolean containDesignGold;
		for (int i = 0; i < modelModes.size(); i++) {
			containDesignGold = false;
			for (int j = 0; j < goldMachineModels.size(); j++) {
				if (goldMachineModels.get(j).getPropertyValue("MODEL_MODES_ID")
						.equals(String.valueOf(modelModes.get(i)))
						&& goldMachineModels.get(j).getPropertyValue(
								"RUN_SOURCE_CHK").equals("DESIGN")) {
					containDesignGold = true;
					break;
				}
			}
			// If this mode doesn't contain gold model, add the last design to
			// gold.
			if (!containDesignGold) {
				MachineModel lastDesignMachineModel = null;
				for (int j = 0; j < allMachineModel.length; j++) {
					if (!allMachineModel[j].getPropertyValue("ID").equals(
							autoRunID)
							&& allMachineModel[j].getPropertyValue(
									"RUN_SOURCE_CHK").equals("DESIGN")
							&& allMachineModel[j]
									.getPropertyValue("MODEL_MODES_ID") != null
							&& allMachineModel[j].getPropertyValue(
									"MODEL_MODES_ID").equals(
									String.valueOf(modelModes.get(i)))) {
						if (lastDesignMachineModel == null)
							lastDesignMachineModel = allMachineModel[j];
						else {
							java.util.Date d1 = machineModelDateFormat
									.parse(allMachineModel[j].getPropertyValue(
											"RUN_ELEMENT_DATE").toString());
							java.util.Date d2 = machineModelDateFormat
									.parse(lastDesignMachineModel
											.getPropertyValue(
													"RUN_ELEMENT_DATE")
											.toString());
							if (d1.after(d2))
								lastDesignMachineModel = allMachineModel[j];
						}
					}
				}
				if (lastDesignMachineModel != null)
					goldMachineModels.add(lastDesignMachineModel);
			}
		}
		return goldMachineModels.toArray(new MachineModel[goldMachineModels
				.size()]);
	}

	public MachineModel getGoldMachineModel(MachineModel[] goldMachineModels,
			int modelMode, String runSource) throws SQLException {
		if (modelMode == 0)
			modelMode = 5;
		MachineModel goldModel = null;
		for (int i = 0; i < goldMachineModels.length; i++) {
			if (goldMachineModels[i].getPropertyValue("MODEL_MODES_ID").equals(
					String.valueOf(modelMode))
					&& goldMachineModels[i].getPropertyValue("RUN_SOURCE_CHK")
							.equals(runSource))
				goldModel = goldMachineModels[i];
		}
		if (goldModel == null && runSource.equals("EXTANT")) {
			for (int i = 0; i < goldMachineModels.length; i++) {
				if (goldMachineModels[i].getPropertyValue("MODEL_MODES_ID")
						.equals(String.valueOf(modelMode))
						&& goldMachineModels[i].getPropertyValue(
								"RUN_SOURCE_CHK").equals("DESIGN"))
					goldModel = goldMachineModels[i];
			}
		}
		return goldModel;
	}

	/**
	 * filter machine models
	 */
	public void filterMachineModelInMode(MachineModel[] allMachineModels,
			MachineModel[] goldMachineModels, int modelMode)
			throws SQLException {
		// add run if run is not null
		if (_runMachineModel != null) {
			MachineModel[] tmp = allMachineModels;
			allMachineModels = new MachineModel[tmp.length + 1];
			for (int i = 0; i < tmp.length; i++) {
				allMachineModels[i] = tmp[i];
			}
			allMachineModels[tmp.length] = _runMachineModel;
		}
		for (int i = 0; i < allMachineModels.length; i++) {
			allMachineModels[i].setPropertyValue("REF", false);
			allMachineModels[i].setPropertyValue("SEL", false);
		}
		MachineModel[] fetchedMachineModels;
		if (modelMode == 0) {
			fetchedMachineModels = allMachineModels;
		} else {
			MachineModel[] tmp = new MachineModel[allMachineModels.length];
			int index = 0;
			for (int i = 0; i < allMachineModels.length; i++) {
				if (allMachineModels[i].getPropertyValue("MODEL_MODES_ID") != null) {
					if ((Integer.valueOf((String) allMachineModels[i]
							.getPropertyValue("MODEL_MODES_ID"))).intValue() == modelMode) {
						tmp[index] = allMachineModels[i];
						index++;
					}
				}
			}
			fetchedMachineModels = new MachineModel[index];
			for (int i = 0; i < index; i++) {
				fetchedMachineModels[i] = tmp[i];
			}

		}
		ArrayList<MachineModel> machineModelSort = new ArrayList<MachineModel>(
				Arrays.asList(fetchedMachineModels));
		Collections.sort(machineModelSort, new Sort(Sort.DOWM));
		machineModelSort.toArray(fetchedMachineModels);

		_fetchedMachineModels = fetchedMachineModels;
		_goldMachineModel = getGoldMachineModel(goldMachineModels, modelMode,
				"DESIGN");
		setGoldModel(_goldMachineModel);
		_referenceMachineModel = getGoldMachineModel(goldMachineModels,
				modelMode, "EXTANT");
		_referenceMachineModel.setPropertyValue("REF", true);
		isGold = _referenceMachineModel.getPropertyValue("GOLD").equals(
				"PRESENT");
		_selectedMachineModel = null;
		_selectedMachineModelDetail = null;
		_selectedMachineModelDevice = null;
		setReferenceModel(_referenceMachineModel);
		ModelPlotData.clearRange();
		
		stateReady = true;
		EVENT_PROXY.modelStateChanged(this);
	}

	public void setSelectedMachineModel(MachineModel[] fetchedMachineModels,
			MachineModel referenceMachineModel,
			MachineModel selectedMachineModel) throws SQLException {
		isGold = referenceMachineModel.getPropertyValue("GOLD").equals(
				"PRESENT");
		if (_selectedMachineModel != null && selectedMachineModel == null) {
			_fetchedMachineModels = fetchedMachineModels;
			_selectedMachineModel = null;
			_selectedMachineModelDetail = null;
			_selectedMachineModelDevice = null;
			EVENT_PROXY.modelStateChanged(this);
			return;
		} else if (_selectedMachineModel == null
				&& selectedMachineModel != null) {
			_fetchedMachineModels = fetchedMachineModels;
			setSelectedModel(selectedMachineModel);
			EVENT_PROXY.modelStateChanged(this);
			return;
		} else if (_selectedMachineModel != null
				&& selectedMachineModel != null
				&& !(_selectedMachineModel.getPropertyValue("ID")
						.equals(selectedMachineModel.getPropertyValue("ID")))) {
			_fetchedMachineModels = fetchedMachineModels;
			setSelectedModel(selectedMachineModel);
			EVENT_PROXY.modelStateChanged(this);
			return;
		} else if (!(_referenceMachineModel.getPropertyValue("ID")
				.equals(referenceMachineModel.getPropertyValue("ID")))
				&& _selectedMachineModel == null) {
			_fetchedMachineModels = fetchedMachineModels;
			setReferenceModel(referenceMachineModel);
			EVENT_PROXY.modelStateChanged(this);
			return;
		} else if (!(_referenceMachineModel.getPropertyValue("ID")
				.equals(referenceMachineModel.getPropertyValue("ID")))
				&& _selectedMachineModel != null) {
			_fetchedMachineModels = fetchedMachineModels;
			setReferenceModel(referenceMachineModel);
			setSelectedModel(_selectedMachineModel);
			EVENT_PROXY.modelStateChanged(this);
			return;
		} else {
			ModelStateView.getProgressBar().setString("Loading Success !");
			ModelStateView.getProgressBar().setIndeterminate(false);
		}
	}

	public void setGoldModel(MachineModel goldMachineModel)
			throws SQLException {
		_goldMachineModel = goldMachineModel;
		_goldMachineModelDetail = PERSISTENT_STORE.fetchMachineModelDetails(acc, _connection,
				Long.valueOf((String) _goldMachineModel.getPropertyValue("ID")));
	}

	public void setReferenceModel(MachineModel referenceMachineModel) throws SQLException {
		_referenceMachineModel = referenceMachineModel;
		if (referenceMachineModel.getPropertyValue("ID").toString().equals(
				autoRunID)) {
			_referenceMachineModelDetail = _runMachineModelDetail;
			_referenceMachineModelDevice = _runMachineModelDevice;
		} else {
			_referenceMachineModelDetail = PERSISTENT_STORE.fetchMachineModelDetails(acc, _connection,
					Long.valueOf((String) _referenceMachineModel.getPropertyValue("ID")));
			_referenceMachineModelDevice = PERSISTENT_STORE.fetchMachineModelDevices(_connection, 
							Long.valueOf((String) _referenceMachineModel.getPropertyValue("ID")));
		}
		_referenceMachineModelDetail = DataManager.calculateBmag(
				_referenceMachineModelDetail, _goldMachineModelDetail);

	}

	public void setSelectedModel(MachineModel selectedMachineModel) throws SQLException {
		if (selectedMachineModel == null)
			return;
		if (_selectedMachineModel == null
				|| !_selectedMachineModel.getPropertyValue("ID").equals(
						selectedMachineModel.getPropertyValue("ID"))
				|| selectedMachineModel.getPropertyValue("ID")
						.equals(autoRunID)) {
			_selectedMachineModel = selectedMachineModel;

			if (selectedMachineModel.getPropertyValue("ID").equals(autoRunID)) {
				_selectedMachineModelDetail = _runMachineModelDetail;
				_selectedMachineModelDevice = _runMachineModelDevice;
			} else {
				_selectedMachineModelDetail = PERSISTENT_STORE.fetchMachineModelDetails(acc, _connection,
						Long.valueOf((String) selectedMachineModel.getPropertyValue("ID")));
				_selectedMachineModelDevice = PERSISTENT_STORE.fetchMachineModelDevices(_connection, 
						Long.valueOf((String) selectedMachineModel.getPropertyValue("ID")));
				// Correct some models' ZPOS
				String startElementName = _selectedMachineModelDetail[0]
						.getPropertyValue("ELEMENT_NAME").toString();
				Double startElementZPos = Double
						.valueOf(_selectedMachineModelDetail[0]
								.getPropertyValue("ZPOS").toString());
				for (int i = 0; i < _referenceMachineModelDetail.length; i++) {
					if (_referenceMachineModelDetail[i].getPropertyValue(
							"ELEMENT_NAME").toString().equals(startElementName)) {
						Double startElementRealZPos = Double
								.valueOf(_referenceMachineModelDetail[i]
										.getPropertyValue("ZPOS").toString());
						if (!startElementRealZPos.equals(startElementZPos)) {
							Double offSet = startElementRealZPos
									- startElementZPos;
							for (int j = 0; j < _selectedMachineModelDetail.length; j++) {
								Double newElementZPos = Double
										.valueOf(_selectedMachineModelDetail[j]
												.getPropertyValue("ZPOS")
												.toString())
										+ offSet;
								_selectedMachineModelDetail[j]
										.setPropertyValue("ZPOS",
												newElementZPos.toString());
							}
						}
						break;
					}
				}
			}
		}
		if (_selectedMachineModelDetail != null)
			_selectedMachineModelDetail = DataManager.calculateBmag(_selectedMachineModelDetail, _goldMachineModelDetail);
	}
	
	public void runModel(RunModelConfiguration config) throws SQLException, ModelException {
		// beamline selection
		rm.setModelMode(modelMode);

		rm.run(config);
		
		Scenario scenario = rm.getScenario();
		_runMachineModel = DataManager.getRunMachineModel(config.getRunModelMethod(),
				modelMode); // add runMachineMode to _fetchedMachineModels
		_runMachineModelDetail = DataManager.getRunMachineModeDetail(config.getRunModelMethod(), scenario);
		_runMachineModelDevice = DataManager.getRunMachineModeDevice(scenario);
		createRunModelComment(_runMachineModel, _runMachineModelDetail);

		addRunModelToFetchedModels(_runMachineModel);
		setSelectedModel(_runMachineModel);
		runState = RunState.RUN;
		EVENT_PROXY.modelStateChanged(this);
	}

	public void createRunModelComment(MachineModel runMachineModel,
			MachineModelDetail[] runMachineModelDetail) {
		if (modelMode == 0) {
			runMachineModel.setPropertyValue("COMMENTS", "Run "
					+ runMachineModel.getPropertyValue("RUN_SOURCE_CHK")
					+ " Model on "
					+ modelModesName.get(modelModes.indexOf(5))
					+ " beam line. And the Energy is "
					+ runMachineModelDetail[runMachineModelDetail.length - 1]
							.getPropertyValue("E") + " GeV.");
		} else {
			runMachineModel.setPropertyValue("COMMENTS", "Run "
					+ runMachineModel.getPropertyValue("RUN_SOURCE_CHK")
					+ " Model on "
					+ modelModesName.get(modelModes.indexOf(modelMode))
					+ " beam line. And the Energy is "
					+ runMachineModelDetail[runMachineModelDetail.length - 1]
							.getPropertyValue("E") + " GeV.");
		}
	}

	public void addRunModelToFetchedModels(MachineModel runMachineModel) {
		for (int i = 0; i < _fetchedMachineModels.length; i++) {
			_fetchedMachineModels[i].setPropertyValue("SEL", Boolean.FALSE);
		}
		MachineModel[] tmp = _fetchedMachineModels;
		if (_fetchedMachineModels[0].getPropertyValue("ID").toString().equals(
				autoRunID))
			_fetchedMachineModels[0] = runMachineModel;
		else {
			_fetchedMachineModels = new MachineModel[tmp.length + 1];
			for (int i = 0; i < tmp.length; i++) {
				_fetchedMachineModels[i] = tmp[i];
			}
			_fetchedMachineModels[tmp.length] = runMachineModel;
		}
		ArrayList<MachineModel> machineModelSort = new ArrayList<MachineModel>(
				Arrays.asList(_fetchedMachineModels));
		Collections.sort(machineModelSort, new Sort(Sort.DOWM));
		machineModelSort.toArray(_fetchedMachineModels);
	}

	public void removeRunModelFromFetchedModels(String uploadID) {
		//add upload machine model to all machine models.
		_runMachineModel.setPropertyValue("ID", uploadID);
		_runMachineModel.setPropertyValue("GOLD", "");
		MachineModel[] tmp = _allMachineModels;
		_allMachineModels = new MachineModel[tmp.length + 1];
		for (int i = 0; i < tmp.length; i++) {
			_allMachineModels[i] = tmp[i];
		}
		_allMachineModels[tmp.length] = _runMachineModel;
		
		//refresh model list
		for (int i = 0; i < _fetchedMachineModels.length; i++) {
			if (_fetchedMachineModels[i].getPropertyValue("ID").toString().equals(
					autoRunID)){
				_fetchedMachineModels[i].setPropertyValue("ID", uploadID);
				_fetchedMachineModels[i].setPropertyValue("GOLD", "");
				break;
			}
		}
		
		//if select machine model is upload model, refresh the run id.
		if (_selectedMachineModel != null && _selectedMachineModel
				.getPropertyValue("ID").toString().equals(autoRunID)) {
			_selectedMachineModel = _runMachineModel;
			for (int i = 0; i < _selectedMachineModelDetail.length; i++) {
				_selectedMachineModelDetail[i].setPropertyValue("RUNS_ID", uploadID);
			}
		}
		
		_runMachineModel = null;
		_runMachineModelDetail = null;
		_runMachineModelDevice = null;
		
		EVENT_PROXY.modelStateChanged(this);
	}

	public void exportToXML(final JFrame parent) {
		DataManager.exportToXML(parent, _runMachineModel, rm.getScenario());
	}

	public String uploadToDatabase(final JFrame parent) {
//		return DataManager.uploadToDatabase(parent, this, _runMachineModel,
//				_runMachineModelDetail, _runMachineModelDevice);
		String runID = DataManager.newUploadToDatabase(parent, this, _runMachineModel,
				_runMachineModelDetail, _runMachineModelDevice);
		// automatically update the table after a model is uploaded
		removeRunModelFromFetchedModels(runID);
		runState = RunState.NONE;
		return runID;
	}

	public void exportDetailData(JFrame parent) {
		if (_selectedMachineModelDetail == null)
			DataManager.exportDetailData(parent, _referenceMachineModelDetail);
		else
			DataManager.exportDetailData(parent, _selectedMachineModelDetail);
	}

	public void makeGold(String comment) throws SQLException {
		DataManager.makeGold(comment, _selectedMachineModel);
	}
	
	public void closeDBConnection() {
		try {
			_connection.close();
		} catch (SQLException e) {
			Message.error("Unable to close connection to database. "+
					e.getMessage());
		}
	}

	public void fetchRunData(RunModelConfiguration config) throws SQLException, ModelException {
		Scenario scenario = rm.getScenario();
		config.initialize(scenario);
		
		_runMachineModel = DataManager.getRunMachineModel(config.getRunModelMethod(), modelMode); // add runMachineMode to _fetchedMachineModels
		
		_runMachineModelDetail = null;
		// switch fetch Machine model
		_runMachineModelDevice = DataManager.getRunMachineModeDevice(scenario);
		_runMachineModel.setPropertyValue("COMMENTS", "Fetched machine parameters.");
		_runMachineModel.setPropertyValue("RUN_SOURCE_CHK", "PRERUN");
		addRunModelToFetchedModels(_runMachineModel);
		setSelectedModel(_runMachineModel);
		
		runState = RunState.FETCHED_DATA;
		EVENT_PROXY.modelStateChanged(this);
	}
	
	public RunState getRunState()
	{
		return runState;
	}

	public void resetRunData() {
		if (_selectedMachineModel == _runMachineModel) {
			_selectedMachineModel = null;
			_selectedMachineModelDetail = null;
			_selectedMachineModelDevice = null;
		}

		if (_fetchedMachineModels[0].getPropertyValue("ID").toString().equals(
				autoRunID)) {
			MachineModel[] tmp = _fetchedMachineModels;
			_fetchedMachineModels = new MachineModel[tmp.length-1];
			
			for (int i = 0; i < _fetchedMachineModels.length; i++) {
				_fetchedMachineModels[i] = tmp[i+1];
			}
		}
		
		_runMachineModel = null;
		_runMachineModelDetail = null;
		_runMachineModelDevice = null;		
		
		runState = RunState.NONE;
		EVENT_PROXY.modelStateChanged(this);
	}
}
