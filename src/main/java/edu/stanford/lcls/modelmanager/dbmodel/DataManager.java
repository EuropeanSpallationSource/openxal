/*=====================================================================================
 * Abs: Defines DataManager. Manages formatting of model data for display onto the 
 *      screen, and upload to the database. 
 *      
 * Rem: Note that the same methods, and data variables (MachineModelDetail and
 *      MachineModelDevice, are used for both formatting for the tables on the 
 *      ModelManager table tabs, and for uploading data to the database. These
 *      are arrays of java Property, but since the property values are String (!),
 *      the data uploaded to the database are not binary Double, but in fact 
 *      Stringified equivalents. 
 * 
 *-------------------------------------------------------------------------------------
 * Mod: 18-Mar-2015, Greg White (greg@slac.stanford.edu) 
 *                   Add ability to use different db connection drivers, so ESS can
 *                   avoid using Oracle specific OCI connector, and insteal use pure
 *                   all java jdbc.
 *       1-Mar-2011, Greg White (greg@slac.stanford.edu). 
 *                   Added static class ModelFormatter, and used it
 *                   to replace the fixed point formatting to only 6 decimal places that
 *                   was used for all formatting, to floating point with 12 significant
 *                   digits. This effectively doubles the precision of data uploaded to
 *                   the database, and enables precise transfer matrix construction.
 */
package edu.stanford.lcls.modelmanager.dbmodel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.model.IElement;
import xal.model.elem.Element;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.Magnet;
import xal.smf.impl.RfCavity;
import xal.smf.impl.RfGap;
import xal.smf.impl.VDipoleCorr;
import xal.smf.impl.qualify.MagnetType;
import xal.smf.impl.qualify.NotTypeQualifier;
import xal.smf.impl.qualify.OrTypeQualifier;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.smf.proxy.RfCavityPropertyAccessor;
import xal.tools.apputils.files.RecentFileTracker;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.beam.Twiss;
import xal.tools.beam.calc.CalculationsOnBeams;
import xal.tools.math.r3.R3;
import edu.stanford.slac.Message.Message;
/**
 * 
 * DataManager for showing loading different parameters, details and other data according to model configuration.
 * 
 * @version 1.0 18 Avg 2015
 * 
 * @author unknown
 * @author Blaž Kranjc 
 */
public class DataManager {
	final static public SimpleDateFormat machineModelDateFormat = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss");
	
	private static final String DB_URL_PROPERTY_NAME="DB_CONNECTION";
	private static final String DB_USERNAME_PROPERTY_NAME = "DB_USERNAME";	
	private static final String DB_PWD_PROPERTY_NAME = "DB_PASSWORD";

	public static String url = null; // Defaults to production

	private final static String autoRunID = "RUN";	

	private static boolean useSDisplay = false;
	
	/**
	 * Get connection to the database. 
	 * @return connection to database if succeeded.
	 * @throws SQLException if connection couldn't be established.
	 */
	public static Connection getConnection() throws SQLException
	{
		Connection connection = null;
		try { 
	        url = System.getProperty(DB_URL_PROPERTY_NAME);
			if ( url != null )
			{
				String username = System.getProperty(DB_USERNAME_PROPERTY_NAME); 
				String password = System.getProperty(DB_PWD_PROPERTY_NAME);
				connection = DriverManager.getConnection(url, username, password);
			}
			else
			{
				Message.error("Unable to establish connection to database, no url given");
			}	
			
		} catch (SQLException e) {
			Message.error("Unable to establish connection to database, check driver and url" + 
					e.toString());
		}	
		return connection;
	}

	
	
	public static MachineModelDetail[] calculateBmag(MachineModelDetail[] selectedMachineModelDetail,
			MachineModelDetail[] referenceMachineModelDetail){
			if (referenceMachineModelDetail == null) return selectedMachineModelDetail;
			
			Double alpha_X, alpha0_X, beta_X, beta0_X, gamma_X, gamma0_X,
		       alpha_Y, alpha0_Y, beta_Y, beta0_Y, gamma_Y, gamma0_Y, Bmag_X, Bmag_Y;
			for (int i = 0; i < selectedMachineModelDetail.length; i++) {
				for (int j = 0; j < referenceMachineModelDetail.length; j++){
					if(selectedMachineModelDetail[i].getPropertyValue("ELEMENT_NAME")
							.equals(referenceMachineModelDetail[j].getPropertyValue("ELEMENT_NAME")) && 
						Double.valueOf(selectedMachineModelDetail[i].getPropertyValue("ZPOS").toString()) -
						Double.valueOf(referenceMachineModelDetail[j].getPropertyValue("ZPOS").toString()) < 0.00001){
						alpha_X = Double.valueOf(selectedMachineModelDetail[i].getPropertyValue("ALPHA_X").toString());
						alpha_Y = Double.valueOf(selectedMachineModelDetail[i].getPropertyValue("ALPHA_Y").toString());
						beta_X = Double.valueOf(selectedMachineModelDetail[i].getPropertyValue("BETA_X").toString());
						beta_Y = Double.valueOf(selectedMachineModelDetail[i].getPropertyValue("BETA_Y").toString());
						gamma_X = (1 + alpha_X * alpha_X) / beta_X;
						gamma_Y = (1 + alpha_Y * alpha_Y) / beta_Y;
						alpha0_X = Double.valueOf(referenceMachineModelDetail[j].getPropertyValue("ALPHA_X").toString());
						alpha0_Y = Double.valueOf(referenceMachineModelDetail[j].getPropertyValue("ALPHA_Y").toString());
						beta0_X = Double.valueOf(referenceMachineModelDetail[j].getPropertyValue("BETA_X").toString());
						beta0_Y = Double.valueOf(referenceMachineModelDetail[j].getPropertyValue("BETA_Y").toString());
						gamma0_X = (1 + alpha0_X * alpha0_X) / beta0_X;
						gamma0_Y = (1 + alpha0_Y * alpha0_Y) / beta0_Y;
						Bmag_X = 0.5 * ( beta_X*gamma0_X - 2*alpha_X*alpha0_X + gamma_X*beta0_X);
						Bmag_Y = 0.5 * ( beta_Y*gamma0_Y - 2*alpha_Y*alpha0_Y + gamma_Y*beta0_Y);
						selectedMachineModelDetail[i].setPropertyValue("Bmag_X", Bmag_X.toString());
						selectedMachineModelDetail[i].setPropertyValue("Bmag_Y", Bmag_Y.toString());
						break;
					}
				}
			}
		return selectedMachineModelDetail;
	}
	/**
	 * Gives the Machine model to run with set model mode and some properties.
	 * @param runModelMethod mode on which to set model.
	 * @param modelMode beamline.
	 * @return configured machine model.
	 */
	public static MachineModel getRunMachineModel(int runModelMethod, String modelMode) {
		MachineModel runMachineModel = new MachineModel();
		runMachineModel.setPropertyValue("ID", autoRunID);
		runMachineModel.setPropertyValue("RUN_ELEMENT_DATE", machineModelDateFormat.format(new java.util.Date()));
		if(runModelMethod == 0){
			runMachineModel.setPropertyValue("RUN_SOURCE_CHK", "DESIGN");
		}else if(runModelMethod == 1){
			runMachineModel.setPropertyValue("RUN_SOURCE_CHK", "EXTANT");
		} else if(runModelMethod == 2){
			runMachineModel.setPropertyValue("RUN_SOURCE_CHK", "PVLOGGER");
		} else if(runModelMethod == 3){
			runMachineModel.setPropertyValue("RUN_SOURCE_CHK", "MANUAL");
		}
		runMachineModel.setPropertyValue("MODEL_MODES_ID", modelMode);
		runMachineModel.setPropertyValue("COMMENTS", "");
		runMachineModel.setPropertyValue("DATE_CREATED", machineModelDateFormat.format(new java.util.Date()));
		runMachineModel.setPropertyValue("GOLD", "RUN");
		return runMachineModel;
	}
	
	/**
	 * Gives the Machine model details obtained according to runModelMethod.
	 * @param runModelMethod method for which to obtain details.
	 * @param scenario from which to obtain details.
	 * @return MachineModelDetails with set properties. 
	 */
	public static MachineModelDetail[] getRunMachineModeDetail(int runModelMethod, Scenario scenario ) {
		List<MachineModelDetail> runMachineModelDetail = new ArrayList<MachineModelDetail>();
		DeviceType deviceType = new DeviceType(scenario.getSequence());
		// DecimalFormat df = new DecimalFormat("##0.000000"); - see Mod 1-Mar-2011.
		ModelFormat df = new ModelFormat("%17.12g");
		Trajectory<EnvelopeProbeState> trajectory = scenario.getTrajectory();
		AcceleratorSeq seq = scenario.getSequence();
		OrTypeQualifier otq = new OrTypeQualifier();
		otq.or("Bnch");
		NotTypeQualifier ntq = new NotTypeQualifier(otq);
		List<AcceleratorNode> allNodes = seq.getAllNodesWithQualifier(ntq);
		// nodes without 'Bnch' for model output use
		allNodes = AcceleratorSeq.filterNodesByStatus(allNodes, true);
		
		// find out the first sequence and offset the z-position accordingly
		String firstSeqId = null;
		if (scenario.getSequence() instanceof AcceleratorSeqCombo) {
			firstSeqId = ((AcceleratorSeqCombo) scenario.getSequence())
					.getEntranceID();
		} else {
			firstSeqId = scenario.getSequence().getId();
		}
		
		Iterator<AcceleratorNode> it = allNodes.iterator();
		// order the elements
		HashMap<String, Integer> elemOrders = new HashMap<String, Integer>();
		int ordinal = 0;
		Iterator<EnvelopeProbeState> elemIter = trajectory.stateIterator();
		while (elemIter.hasNext()) {
			elemOrders.put(elemIter.next().getElementId(),	new Integer(ordinal));
			ordinal++;
		}
		CalculationsOnBeams cob = new CalculationsOnBeams(trajectory);
		
		// loop through all nodes
		while (it.hasNext()) {
			AcceleratorNode node = it.next();
			List<IElement> elems = scenario.elementsMappedTo(node);
			
			// Get effective length at node level (not the element itself).
			double node_length = 0.;
			if (node instanceof Magnet) {
				if (node instanceof HDipoleCorr
						|| node instanceof VDipoleCorr) {
					node_length = 0.0;
				} else {
					node_length = ((Magnet) node)
							.getEffLength();
				}
			} else if (node instanceof RfGap) {
				node_length = ((RfGap) node).getParent()
						.getLength();
			} else {
				node_length = node.getLength();
			}
			
			if (elems != null)
			for (int i = 0; i < elems.size(); i++) {
				// reset machineModelDetail
				MachineModelDetail machineModelDetail = new MachineModelDetail();

				machineModelDetail.setPropertyValue("RUNS_ID", autoRunID);
				
				String nodeId = node.getId();
		
				machineModelDetail.setPropertyValue("ELEMENT_NAME", nodeId);
				machineModelDetail.setPropertyValue("DEVICE_TYPE", deviceType
						.getDeviceType(machineModelDetail.getPropertyValue("ELEMENT_NAME").toString()));
				machineModelDetail.setPropertyValue("EPICS_NAME", deviceType
						.getEPICSName(machineModelDetail.getPropertyValue("ELEMENT_NAME").toString()));
				
				//double s = 0.;
				// TODO CHECK: is this still relevant?
				// treat Q30615x and Q30715x differently
				if (node.isKindOf(RfGap.s_strType)) {						
					machineModelDetail.setPropertyValue("INDEX_SLICE_CHK", i == 0 ? "0" : "2");					
				} else {
					machineModelDetail.setPropertyValue("INDEX_SLICE_CHK", String.valueOf(i));												
				}
				// get element
				Element elem = (Element) elems.get(i);

				// get element length
				machineModelDetail.setPropertyValue("LEFF", df.format(node_length));
				machineModelDetail.setPropertyValue("SLEFF", df.format(elem.getLength()));
				
				// get state for the element
			
				EnvelopeProbeState state = trajectory.stateNearestPosition(elem.getPosition()+elem.getLength()/2.);
						//.trajectoryStatesForElement(elem.getId());
				// We only output begin/middle/end for a thick device,
				// therefore, no need to go through all the states but only the
				// last state for that element.
				if (state == null) continue;
										

				// z-pos offset correction
				machineModelDetail.setPropertyValue("ZPOS", df.format(elem.getPosition()+elem.getLength()/2.));
				machineModelDetail.setPropertyValue("SUML", df.format(state.getPosition()));
				machineModelDetail.setPropertyValue("ORDINAL", elemOrders.get(state.getElementId()).toString());

				if (state instanceof EnvelopeProbeState) {
					Twiss[] twiss = state
							.twissParameters();
					R3 betatronPhase = cob.computeBetatronPhase(state);
							
					machineModelDetail.setPropertyValue("BETA_X", df.format(twiss[0].getBeta()));
					machineModelDetail.setPropertyValue("ALPHA_X", df.format(twiss[0].getAlpha()));
					machineModelDetail.setPropertyValue("BETA_Y", df.format(twiss[1].getBeta()));
					machineModelDetail.setPropertyValue("ALPHA_Y", df.format(twiss[1].getAlpha()));
					
					PhaseVector chromDispersion = cob.computeChromDispersion(state);
					machineModelDetail.setPropertyValue("ETA_X", df.format(chromDispersion.getx()));
					machineModelDetail.setPropertyValue("ETA_Y", df.format(chromDispersion.gety()));
					machineModelDetail.setPropertyValue("ETAP_X", df.format(chromDispersion.getxp()));
					machineModelDetail.setPropertyValue("ETAP_Y", df.format(chromDispersion.getyp()));
					
					machineModelDetail.setPropertyValue("PSI_X", df.format(betatronPhase.getx()));
					machineModelDetail.setPropertyValue("PSI_Y", df.format(betatronPhase.gety()));
					machineModelDetail.setPropertyValue("E", df.format(getTotalEnergyFromKinetic(state.getSpeciesRestEnergy() / 1.e9, state.getKineticEnergy() / 1.e9)));
					machineModelDetail.setPropertyValue("P", df.format(RelativisticParameterConverter.
							computeMomentumFromEnergies(state.getKineticEnergy(), state.getSpeciesRestEnergy()) / 1e9));
					machineModelDetail.setPropertyValue("Bmag_X", "1");
					machineModelDetail.setPropertyValue("Bmag_Y", "1");
					
					PhaseMatrix rMat = state
							.getResponseMatrix();
					// Set response matrix elements to machineModelDetail
					for (int row_index = 0; row_index < 7; row_index++) {
						for (int col_index = 0; col_index < 7; col_index++) {
							machineModelDetail.setPropertyValue("R" + String.valueOf(row_index+1) + String.valueOf(col_index+1),
									df.format(rMat.getElem(row_index, col_index)));
						}
					}
				}
				runMachineModelDetail.add(machineModelDetail);
			
			}
		}
		Collections.sort(runMachineModelDetail, new SortMachineModelDetail("ORDINAL", Sort.UP));
		return runMachineModelDetail.toArray(new MachineModelDetail[runMachineModelDetail.size()]);
	}
	
	public static double getTotalEnergyFromKinetic(double m, double k) {
		double Et = 0.;
		Et = k + m;
		return Et;
	}
	
	/**
	 * Gives MachineModelDevice obtained from scenario.
	 * @param scenario from which to obtain devices.
	 * @return MachineModelDevices with set properties.
	 */
	public static MachineModelDevice[] getRunMachineModeDevice(Scenario scenario) {
		//DecimalFormat df = new DecimalFormat("##0.000000");
		ModelFormat df = new ModelFormat("%17.12g");
		List<MachineModelDevice> runMachineModelDevice = new ArrayList<MachineModelDevice>();
		AcceleratorSeq seq = scenario.getSequence();
		Accelerator acc = seq.getAccelerator();
		List<AcceleratorNode> allNodes = seq.getAllNodes();
		Iterator<AcceleratorNode> it = allNodes.iterator();
		//Going through all accelerator nodes
		//Variables for better code readability
        String deviceValue;
        String zpos;
		while (it.hasNext()) {
			AcceleratorNode node = it.next();
			String runMode = scenario.getSynchronizationMode();
			zpos = df.format(useSDisplay ? node.getSDisplay() : acc.getPosition(node));
			// for magnets
			if (node instanceof Electromagnet) {
			    String units = "T";
			    if(MagnetType.QUAD.equals(node.getType())){
			        units = units + "/m"; 
			    }
			    else if(MagnetType.SEXT.equals(node.getType())){
                    units = units + "/m^2"; 
                }
			    else if(MagnetType.OCT.equals(node.getType())){
                    units = units + "/m^3"; 
                }//CHECK solenoid too ?
				try {
				    deviceValue = scenario.propertiesForNode(node).get(ElectromagnetPropertyAccessor.PROPERTY_FIELD).toString();
					MachineModelDevice tmp1 = new MachineModelDevice(node.getId(),"B",deviceValue,deviceValue,units,zpos);//CHECK initial value
					runMachineModelDevice.add(tmp1);
				} catch (SynchronizationException e) {
					Message.error("Model Synchronization Exception: Cannot synchronize device data for model run.", true);
					e.printStackTrace();
				}
				if (Scenario.SYNC_MODE_DESIGN.equals(runMode)) {
					
				    deviceValue = Double.toString(((Electromagnet) node).getDesignField());
					MachineModelDevice tmp2 = new MachineModelDevice(node.getId(),"BACT",deviceValue,deviceValue,units,zpos);//CHECK initial value
					runMachineModelDevice.add(tmp2);
					
					deviceValue = Double.toString(((Electromagnet) node).getDesignField());
					MachineModelDevice tmp3 = new MachineModelDevice(node.getId(),"BDES",deviceValue,deviceValue,units,zpos);//CHECK initial value
					runMachineModelDevice.add(tmp3);
				} else {
					try {
						deviceValue = Double.toString(((Electromagnet) node).getFieldReadback());
						MachineModelDevice tmp4 = new MachineModelDevice(node.getId(),"BACT",deviceValue,deviceValue,units,zpos);//CHECK initial value
						runMachineModelDevice.add(tmp4);
						
						deviceValue = Double.toString(((Electromagnet) node).getFieldSetting()); 
	                    MachineModelDevice tmp5 = new MachineModelDevice(node.getId(),"BDES",deviceValue,deviceValue,units,zpos);//CHECK initial value
						runMachineModelDevice.add(tmp5);
					} catch (ConnectionException e) {
						Message.error("Connection Exception: Cannot connect to magnet PV for " + ((Electromagnet) node).getEId(), true);
						e.printStackTrace();
					} catch (GetException e) {
						Message.error("Data Get Exception: Cannot get magnet PV vaule for " + ((Electromagnet) node).getEId(), true);
						e.printStackTrace();
					}
				}
			}
			// for RF cavities
			else if (node instanceof RfCavity) {
				try {
					deviceValue = scenario.propertiesForNode(node).get(RfCavityPropertyAccessor.PROPERTY_PHASE).toString();
					MachineModelDevice tmp6 = new MachineModelDevice(node.getId(),"P",deviceValue,deviceValue,"deg",zpos);//CHECK initial value
					runMachineModelDevice.add(tmp6);
					
					deviceValue = scenario.propertiesForNode(node).get(RfCavityPropertyAccessor.PROPERTY_AMPLITUDE).toString();
					MachineModelDevice tmp7 = new MachineModelDevice(node.getId(),"A",deviceValue,deviceValue,"kV/m",zpos);//CHECK initial value
					runMachineModelDevice.add(tmp7);
				} catch (SynchronizationException e) {
					Message.error("Model Synchronization Exception: Cannot synchronize device data for model run.", true);
					e.printStackTrace();
				}
				if (Scenario.SYNC_MODE_DESIGN.equals("DESIGN")) {
					deviceValue = Double.toString(((RfCavity) node).getDfltAvgCavPhase());
					MachineModelDevice tmp8 = new MachineModelDevice(node.getId(),"PDES",deviceValue,deviceValue,"deg",zpos);//CHECK initial value
					runMachineModelDevice.add(tmp8);

                    deviceValue = Double.toString(((RfCavity) node).getDfltCavAmp());
                    MachineModelDevice tmp9 = new MachineModelDevice(node.getId(),"ADES",deviceValue,deviceValue,"kV/m",zpos);//CHECK initial value
                    runMachineModelDevice.add(tmp9);
                }
                else{
                    // We use "design" values for both cases for now because we
                    // don't have access to live readbacks.
                    }
            }
            // Misalignments and other parameters
            deviceValue = Double.toString(node.getAper().getAperX()); 
            MachineModelDevice tmp10 = new MachineModelDevice(node.getId(),"APRX",deviceValue,deviceValue,"mm",zpos);//CHECK initial value
            runMachineModelDevice.add(tmp10);

            deviceValue = Double.toString(node.getAlign().getX()); 
            MachineModelDevice tmp11 = new MachineModelDevice(node.getId(),"MISX",deviceValue,deviceValue,"mm",zpos);//CHECK initial value
            runMachineModelDevice.add(tmp11);

            deviceValue = Double.toString(node.getAlign().getY()); 
            MachineModelDevice tmp12 = new MachineModelDevice(node.getId(),"MISY",deviceValue,deviceValue,"mm",zpos);//CHECK initial value
            runMachineModelDevice.add(tmp12);

            deviceValue = Double.toString(node.getAlign().getZ());
            MachineModelDevice tmp13 = new MachineModelDevice(node.getId(),"MISZ",deviceValue,deviceValue,"mm",zpos);//CHECK initial value
            runMachineModelDevice.add(tmp13);

            deviceValue = Double.toString(node.getAlign().getPitch());
            zpos = df.format(useSDisplay ? node.getSDisplay() : acc.getPosition(node));
            MachineModelDevice tmp14 = new MachineModelDevice(node.getId(),"ROTX",deviceValue,deviceValue,"mRad",zpos);//CHECK initial value
            runMachineModelDevice.add(tmp14);

            deviceValue = Double.toString(node.getAlign().getYaw());
            zpos = df.format(useSDisplay ? node.getSDisplay() : acc.getPosition(node));
            MachineModelDevice tmp15 = new MachineModelDevice(node.getId(),"ROTY",deviceValue,deviceValue,"mRad",zpos);//CHECK initial value
            runMachineModelDevice.add(tmp15);

            deviceValue = Double.toString(node.getAlign().getRoll());
            zpos = df.format(useSDisplay ? node.getSDisplay() : acc.getPosition(node));
            MachineModelDevice tmp16 = new MachineModelDevice(node.getId(),"ROTZ",deviceValue,deviceValue,"mRad",zpos);//CHECK initial value
            runMachineModelDevice.add(tmp16);
            
            deviceValue = String.valueOf(node.getStatus() ? 1:0);
            zpos = df.format(useSDisplay ? node.getSDisplay() : acc.getPosition(node));
            MachineModelDevice tmp17 = new MachineModelDevice(node.getId(),"ENBL",deviceValue,deviceValue,"",zpos);//CHECK initial value
            runMachineModelDevice.add(tmp17);         

        }
		return runMachineModelDevice.toArray(new MachineModelDevice[runMachineModelDevice.size()]);
	}
	
	/**
	 * Saves model to database.
	 * @param parent parent window
	 * @param model browser model
	 * @param runMachineModel model to save
	 * @param runMachineModelDetail model detail to save
	 * @param runMachineModelDevice model device to save
	 * @return runID if successfull else null.
	 */
	public static String newUploadToDatabase(final JFrame parent,
			BrowserModel model,
			MachineModel runMachineModel,
			final MachineModelDetail[] runMachineModelDetail,
			final MachineModelDevice[] runMachineModelDevice) {
	    try {
	        
	        if(runMachineModelDetail == null){
	            throw new Exception("Model not run before trying to save. Please run model first.");
	        }
	    
		Integer runID = null;
				
		Connection writeConnection;
		
		
			writeConnection = DataManager.getConnection();
			
			ArrayList<String> elementName = new ArrayList<String>();
			ArrayList<Integer> elementID = new ArrayList<>();
			//ArrayList<Integer> elementTypeID = new ArrayList<>();
			int index;
	
			// prepare for MACHINE_MODEL.RUNS
			PreparedStatement stmt1 = null;
			
		
			writeConnection.setAutoCommit(false);
			
			stmt1 = writeConnection.prepareStatement("INSERT INTO \"MACHINE_MODEL\".\"RUNS\" (\"RUN_SOURCE_CHK\", \"COMMENTS\", \"MODEL_MODES_ID\")"
				+"	VALUES(?,?,?) RETURNING \"ID\"");
			stmt1.setString(1, (String)runMachineModel.getPropertyValue("RUN_SOURCE_CHK"));
			stmt1.setString(2, (String)runMachineModel.getPropertyValue("COMMENTS"));
			stmt1.setString(3, (String)runMachineModel.getPropertyValue("MODEL_MODES_ID"));
			
			ResultSet rs1 = stmt1.executeQuery();
			rs1.next();
			Message.info("RUN_ID = " + rs1.getInt(1));
			
			// update the RUN ID
			runID = rs1.getInt(1);
			
			PreparedStatement stmt3 = writeConnection.prepareStatement("INSERT INTO \"MACHINE_MODEL\".\"MODEL_DEVICES\" (\"RUNS_ID\", \"LCLS_ELEMENTS_ELEMENT_ID\", \"ELEMENT_NAME\", \"DEVICE_TYPES_ID\", \"DEVICE_PROPERTY\", \"DEVICE_VALUE\", \"UNITS\", \"ZPOS\") "+
			  "VALUES (?,?,?,?,?,?,?,?)");
			stmt3.setInt(1, runID);
			
			int deviceIndex = 0;
			for(int i=0; i<runMachineModelDevice.length; i++){
				index = elementName.indexOf(runMachineModelDevice[i].getPropertyValue("ELEMENT_NAME").toString());
				if(index >= 0){
					stmt3.setInt(2, elementID.get(index));					
				}else{					
					stmt3.setInt(2, deviceIndex);					
					elementName.add(runMachineModelDevice[i].getPropertyValue("ELEMENT_NAME").toString());
					elementID.add(deviceIndex);					
					deviceIndex++;
				}
				stmt3.setString(3, (String)runMachineModelDevice[i].getPropertyValue("ELEMENT_NAME"));
				stmt3.setNull(4, Types.INTEGER);			
				stmt3.setString(5, (String)runMachineModelDevice[i].getPropertyValue("DEVICE_PROPERTY"));
				stmt3.setDouble(6, Double.parseDouble((String) runMachineModelDevice[i].getPropertyValue("DEVICE_VALUE")));
				stmt3.setString(7, ((String)runMachineModelDevice[i].getPropertyValue("UNITS")));
				stmt3.setDouble(8, Double.parseDouble((String)runMachineModelDevice[i].getPropertyValue("ZPOS")));			
				stmt3.addBatch();
			}
			stmt3.executeBatch();

			PreparedStatement stmt4 = writeConnection.prepareStatement("	INSERT INTO \"MACHINE_MODEL\".\"ELEMENT_MODELS\" ( " +
    " \"RUNS_ID\", \"LCLS_ELEMENTS_ELEMENT_ID\", \"ELEMENT_NAME\", \"INDEX_SLICE_CHK\", " +
    "\"ZPOS\", \"EK\", \"ALPHA_X\", \"ALPHA_Y\", \"BETA_X\", \"BETA_Y\" , \"PSI_X\"  , \"PSI_Y\", \"ETA_X\", \"ETA_Y\", \"ETAP_X\", \"ETAP_Y\","+
    "\"R11\", \"R12\", \"R13\", \"R14\", \"R15\", \"R16\", \"R17\", " +
    "\"R21\", \"R22\", \"R23\", \"R24\", \"R25\", \"R26\", \"R27\", " +
    "\"R31\", \"R32\", \"R33\", \"R34\", \"R35\", \"R36\", \"R37\", " +
    "\"R41\", \"R42\", \"R43\", \"R44\", \"R45\", \"R46\", \"R47\", " +
    "\"R51\", \"R52\", \"R53\", \"R54\", \"R55\", \"R56\", \"R57\", " +
    "\"R61\", \"R62\", \"R63\", \"R64\", \"R65\", \"R66\", \"R67\", " +
    "\"R71\", \"R72\", \"R73\", \"R74\", \"R75\", \"R76\", \"R77\", " +
    "\"LEFF\", \"SLEFF\" , \"ORDINAL\", \"SUML\", \"DEVICE_TYPE\" ) " + 
    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			for(int i=0; i<runMachineModelDetail.length; i++){
				index = elementName.indexOf(runMachineModelDetail[i].getPropertyValue("ELEMENT_NAME").toString());
				if(index >= 0){
					stmt4.setInt(2, elementID.get(index));
				}else{
					stmt4.setNull(2, Types.INTEGER);
				}
				stmt4.setInt(1, runID);

				// Indexing for other properties
				int prop_index = 3;	

				stmt4.setString(prop_index++, (String)runMachineModelDetail[i].getPropertyValue("ELEMENT_NAME"));
				stmt4.setInt(prop_index++, Integer.parseInt((String)runMachineModelDetail[i].getPropertyValue("INDEX_SLICE_CHK")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ZPOS")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("E")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ALPHA_X")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ALPHA_Y")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("BETA_X")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("BETA_Y")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("PSI_X")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("PSI_Y")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ETA_X")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ETA_Y")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ETAP_X")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ETAP_Y")));
				for (int row_index = 0; row_index < 7; row_index++) {
					for (int col_index = 0; col_index < 7; col_index++) {
						stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue(
							"R" + String.valueOf(row_index+1) + String.valueOf(col_index+1)
							)));
					}
				}
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("LEFF")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("SLEFF")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ORDINAL")));
				stmt4.setDouble(prop_index++, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("SUML")));
				stmt4.setString(prop_index++, (String)runMachineModelDetail[i].getPropertyValue("DEVICE_TYPE"));

				stmt4.addBatch();
			}
		
			stmt4.executeBatch();
			


			writeConnection.commit();
			
			String msg = "Model data upload finished successfully";
			Message.info(msg, true);
	
			writeConnection.close();
			
			return runID.toString();
		} catch (SQLException e) {
			Message.error("SQLException: failed to execute PL/SQL call on " + url, true);
			e.printStackTrace();
			
			return null;
		} catch (Exception exception) {
			JOptionPane.showMessageDialog(parent, exception.getMessage(),
					"SQL Error!  Query failed on " + url, JOptionPane.ERROR_MESSAGE);
			Logger.getLogger("global").log(Level.SEVERE,
					"Database SQL error.", exception);
			Message.error("SQLException: Query failed on " + url, true);
			
			return null;
		}
	}		
	
	/**
	 * Export model detail to csv.
	 * @param parent parent window
	 * @param selectedMachineModelDetail machine model detail to export.
	 */
	public static void exportDetailData(JFrame parent, MachineModelDetail[] selectedMachineModelDetail) {
		RecentFileTracker _savedFileTracker = new RecentFileTracker(1, parent.getClass(),
				"recent_saved_file");
		String currentDirectory = _savedFileTracker.getRecentFolderPath();
		JFileChooser fileChooser = new JFileChooser(currentDirectory);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
            public boolean accept(File f) {
				if (f.getName().endsWith(".csv") || f.isDirectory()) {
					return true;
				}
				return false;
			}

			@Override
            public String getDescription() {
				return "CSV (*.csv)";
			}
		});
		int status = fileChooser.showSaveDialog(parent);
		if (status == JFileChooser.APPROVE_OPTION) {
			_savedFileTracker.cacheURL(fileChooser.getSelectedFile());
			File file = fileChooser.getSelectedFile();
			if (!file.getName().endsWith(".csv")) {
				file = new File(file.getPath() + ".csv");
			}
			// write data to the selected file
			try {
				FileWriter fileWriter = new FileWriter(file);
				// write table column title
				for (int i = 0; i < MachineModelDetail.getPropertySize(); i++)
					fileWriter.write(MachineModelDetail.getPropertyName(i)
							+ ", ");
				fileWriter.write("\n");
				// write table column value
				for (int i = 0; i < selectedMachineModelDetail.length; i++) {
					for (int j = 0; j < MachineModelDetail.getPropertySize(); j++)
						fileWriter.write((String) selectedMachineModelDetail[i]
								.getPropertyValue(j)
								+ ", ");
					fileWriter.write("\n");
				}
				fileWriter.close();
			} catch (IOException ie) {
				Message.error("Cannot open file " + file.getName() + "for writing");
				JOptionPane.showMessageDialog(parent, "Cannot open the file"
						+ file.getName() + "for writing", "Warning!",
						JOptionPane.PLAIN_MESSAGE);
				parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		}
	}
	
	/**
	 * Tag selected model as gold
	 * @param comment to add to tag
	 * @param selectedMachineModel model to tag as gold
	 * @throws SQLException if tagging couldn't be sent to database.
	 */
	public static void makeGold(String comment, MachineModel selectedMachineModel) throws SQLException {
		// TODO also update other DB instances, if necessary
		
		Connection writeConnection = null;
		
		writeConnection = DataManager.getConnection();
		
		if (writeConnection != null) {			
			// use PL/SQL for GOLD
			CallableStatement cstmt = null;
			
			// TODO OPENXAL there's no packages in pgsql MACHINE_MODEL.MODEL_UPLOAD_PKG.GOLD_THIS_MODEL
			cstmt = writeConnection.prepareCall(
			"{call \"MACHINE_MODEL\".\"GOLD_THIS_MODEL\" ('" 
					+ selectedMachineModel.getPropertyValue("ID").toString() 
					+ "', '" + comment + "', ?)}");
			Message.info("Run " + selectedMachineModel.getPropertyValue("ID").toString()
					+ " is set to GOLD", true);
			
			cstmt.registerOutParameter(1, java.sql.Types.VARCHAR);

			cstmt.execute();
			String msg = cstmt.getString(1);
			
			if (msg == null) {
				msg = "A new GOLD Model is tagged successfully!";
				Message.info(msg, true);
			} else {
				Message.error(msg, true);
			}

			cstmt.close();
			writeConnection.close();					
		}
	}
	/**
	 * Export machine model to xml.
	 * @param parent parent window
	 * @param runMachineModel machine model to export
	 * @param scenario
	 */
	public static void exportToXML(JFrame parent, MachineModel runMachineModel,
			Scenario scenario) {
		RecentFileTracker _savedFileTracker = new RecentFileTracker(1, parent
				.getClass(), "recent_saved_file");
		String currentDirectory = _savedFileTracker.getRecentFolderPath();
		JFileChooser fileChooser = new JFileChooser(currentDirectory);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
            public boolean accept(File f) {
				if (f.getName().endsWith(".xml") || f.isDirectory()) {
					return true;
					}
				return false;
				}
			@Override
            public String getDescription() {
				return "XML (*.xml)";
				}
			});
		int status = fileChooser.showSaveDialog(parent);
		if (status == JFileChooser.APPROVE_OPTION) {
			_savedFileTracker.cacheURL(fileChooser.getSelectedFile());
			File file = fileChooser.getSelectedFile();
			try {
				java.util.Date runDate = machineModelDateFormat.parse(
						runMachineModel.getPropertyValue("RUN_ELEMENT_DATE").toString());
				SimpleDateFormat fileDateFormat = new SimpleDateFormat(
						"yyyy-MM-dd_HH-mm-ss");
				String elemFile = file.getPath() + "/xalElements_" + fileDateFormat.format(runDate) + ".xml";
				String dvcFile = file.getPath() + "/xalDevices_"  + fileDateFormat.format(runDate) + ".xml";
				/* TODO OPENXAL depends on XAL
			    NewTwissRMatXmlWriter.writeXml(scenario, elemFile, dvcFile);
			} catch (IOException ie) {
				Message.error("Cannot open file " + file.getName() + "for writing");
				JOptionPane.showMessageDialog(parent, "Cannot open the file"
						+ file.getName() + "for writing", "Warning!",
						JOptionPane.PLAIN_MESSAGE);
				parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			*/} catch (ParseException e) {
				Message.error("XML Parse Exception: " + e.getMessage());			
				e.printStackTrace();
			}
		}
	}
		
	public static String escape(String s)
	{
		return "\"" + s + "\"";
	}
}

/**
 * ModelFormat is a utility class for formatting model parameters to floating 
 * point strings suitable for printing or uploading to the database.
 * 
 * It's actually not ideal, since we are still using the same formatting for display, as
 * for upload to the db. However, using this specially written class is better than
 * the mechanism it replaces, which was formatting as Fixed Point strings with only
 * 6 decimal places - using java util class DecimalFormat. This replacement instead
 * formats using java util Formatter (in which String.format is implemented), for 
 * Floating Point, and hence replaces decimal place specification with 
 * significant digits specification.
 *  
 * @author Greg White (greg@slac.stanford.edu), 1-Mar-2011.
 *
 */
class ModelFormat
{
	String m_format = null;
	
	public ModelFormat(String format)
	{
		m_format=format;
	}
	
	public String format(Double d)
	{
		return String.format(Locale.ROOT,m_format,d);
	}
}


