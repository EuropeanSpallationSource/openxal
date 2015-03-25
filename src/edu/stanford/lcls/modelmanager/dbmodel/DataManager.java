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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
// import java.text.DecimalFormat;
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
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import oracle.jdbc.pool.OracleDataSource;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import edu.stanford.slac.Message.Message;
import edu.stanford.lcls.xal.model.xml.NewTwissRMatXmlWriter;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.model.IElement;
import xal.model.ModelException;
import xal.model.elem.Element;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.Magnet;
import xal.smf.impl.RfCavity;
import xal.smf.impl.RfGap;
import xal.smf.impl.VDipoleCorr;
import xal.smf.impl.qualify.NotTypeQualifier;
import xal.smf.impl.qualify.OrTypeQualifier;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.smf.proxy.RfCavityPropertyAccessor;
import xal.tools.apputils.files.RecentFileTracker;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.beam.Twiss;

public class DataManager {
	final static public SimpleDateFormat machineModelDateFormat = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss");
	
	private static final String DB_URL_PROPERTY_NAME="DB_CONNECTION";
	private static final String DB_USERNAME_PROPERTY_NAME = "DB_USERNAME";	
	private static final String DB_PWD_PROPERTY_NAME = "DB_PASSWORD";
	// for MCCO or SLACDEV (the primary one)
	private static Connection writeConnection;
	// for MCCQA
	private static Connection writeConnection1;
	static final protected String MCCQA_URL ="jdbc:oracle:oci:/@MCCQA";
	// for SLACPROD
	private static Connection writeConnection2;
	static final protected String SLACPROD_URL ="jdbc:oracle:oci:/@SLACPROD";
	static final protected String SLACDEV_URL ="jdbc:oracle:oci:/@SLACDEV";
	static final protected String MCCO_URL = "jdbc:oracle:oci:/@MCCO";
	public static String url = getUrl(); // Defaults to production
	public static String dbusername = null;
	public static String dbpwd = null;
	
	private final static String autoRunID = "RUN";
	private static String comment = "";

	
	static ArrayList<String> deviceID = new ArrayList<String>();
	static ArrayList<String> deviceID1 = new ArrayList<String>();
	static ArrayList<String> deviceTypeID = new ArrayList<String>();

	private static boolean useSDisplay = false;
	
	public static String getUrl()
	{
		url = System.getProperty(DB_URL_PROPERTY_NAME);
		if ( url != null )
		{
			dbusername = System.getProperty(DB_USERNAME_PROPERTY_NAME);
			dbpwd    = System.getProperty(DB_PWD_PROPERTY_NAME);
		}
		else
			url = MCCO_URL;
		return url;
	}
	
	public static Connection getConnection() throws SQLException
	{
		Connection connection = null;
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
	        // DriverManager.setLoginTimeout(15);  // Set timeout to 15 seconds
	        //OracleDataSource ods = new OracleDataSource(); 
	        url = System.getProperty(DB_URL_PROPERTY_NAME);
			if ( url != null )
			{
				String username = System.getProperty(DB_USERNAME_PROPERTY_NAME); 
				String password = System.getProperty(DB_PWD_PROPERTY_NAME);
				/*ods.setURL(url);
				ods.setUser(username);
				ods.setPassword(password);
				connection = ods.getConnection();*/
				connection = DriverManager.getConnection(url, username, password);
			}
			else
			{
				url = MCCO_URL;
				/*ods.setURL(url);
				connection = ods.getConnection();*/
				connection = DriverManager.getConnection(url);
			}	
			
		} catch (SQLException e) {
			Message.error("Unable to establish connection to database, check driver and url" + 
					e.toString());
			// e.printStackTrace();
		}	
		return connection;
	}

	
	
	public static MachineModelDetail[] calculateBmag(MachineModelDetail[] selectedMachineModelDetail,
			MachineModelDetail[] referenceMachineModelDetail){
/*       if(selectedMachineModelDetail.length == referenceMachineModelDetail.length){
			Double alpha_X, alpha0_X, beta_X, beta0_X, gamma_X, gamma0_X,
			       alpha_Y, alpha0_Y, beta_Y, beta0_Y, gamma_Y, gamma0_Y, Bmag_X, Bmag_Y;
			for (int i = 0; i < selectedMachineModelDetail.length; i++) {
				alpha_X = Double.valueOf(selectedMachineModelDetail[i].getPropertyValue("ALPHA_X").toString());
				alpha_Y = Double.valueOf(selectedMachineModelDetail[i].getPropertyValue("ALPHA_Y").toString());
				beta_X = Double.valueOf(selectedMachineModelDetail[i].getPropertyValue("BETA_X").toString());
				beta_Y = Double.valueOf(selectedMachineModelDetail[i].getPropertyValue("BETA_Y").toString());
				gamma_X = (1 + alpha_X * alpha_X) / beta_X;
				gamma_Y = (1 + alpha_Y * alpha_Y) / beta_Y;
				alpha0_X = Double.valueOf(referenceMachineModelDetail[i].getPropertyValue("ALPHA_X").toString());
				alpha0_Y = Double.valueOf(referenceMachineModelDetail[i].getPropertyValue("ALPHA_Y").toString());
				beta0_X = Double.valueOf(referenceMachineModelDetail[i].getPropertyValue("BETA_X").toString());
				beta0_Y = Double.valueOf(referenceMachineModelDetail[i].getPropertyValue("BETA_Y").toString());
				gamma0_X = (1 + alpha0_X * alpha0_X) / beta0_X;
				gamma0_Y = (1 + alpha0_Y * alpha0_Y) / beta0_Y;
				Bmag_X = 0.5 * ( beta_X*gamma0_X - 2*alpha_X*alpha0_X + gamma_X*beta0_X);
				Bmag_Y = 0.5 * ( beta_Y*gamma0_Y - 2*alpha_Y*alpha0_Y + gamma_Y*beta0_Y);
				selectedMachineModelDetail[i].setPropertyValue("Bmag_X", Bmag_X.toString());
				selectedMachineModelDetail[i].setPropertyValue("Bmag_Y", Bmag_Y.toString());
			}
		}else{*/
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
	
	public static MachineModel getRunMachineModel(int runModelMethod, int modelMode) {
		MachineModel runMachineModel = new MachineModel();
		runMachineModel.addPropertyValue("ID", autoRunID);
		runMachineModel.addPropertyValue("RUN_ELEMENT_DATE", machineModelDateFormat.format(new java.util.Date()));
		if(runModelMethod == 0){
			runMachineModel.addPropertyValue("RUN_SOURCE_CHK", "DESIGN");
		}else if(runModelMethod == 1){
			runMachineModel.addPropertyValue("RUN_SOURCE_CHK", "EXTANT");
		}
		if(modelMode == 0)
			runMachineModel.addPropertyValue("MODEL_MODES_ID", "5");
		else
			runMachineModel.addPropertyValue("MODEL_MODES_ID", Integer.valueOf(modelMode).toString());
		runMachineModel.addPropertyValue("COMMENTS", "");
		runMachineModel.addPropertyValue("DATE_CREATED", machineModelDateFormat.format(new java.util.Date()));
		runMachineModel.addPropertyValue("GOLD", "RUN");
		runMachineModel.addPropertyValue("REF", false);
		runMachineModel.addPropertyValue("SEL", true);
		return runMachineModel;
	}
	
	
	public static MachineModelDetail[] getRunMachineModeDetail(JFrame parent, 
			int runModelMethod, Scenario scenario ) {
		List<MachineModelDetail> runMachineModelDetail = new ArrayList<MachineModelDetail>();
		DeviceType deviceType = new DeviceType(parent);
		// DecimalFormat df = new DecimalFormat("##0.000000"); - see Mod 1-Mar-2011.
		ModelFormat df = new ModelFormat("%17.12g");
		Trajectory trajectory = scenario.getTrajectory();
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
		
		// initial element ID
		String elem0 = "Begin_Of_" + firstSeqId;
		
		Iterator<AcceleratorNode> it = allNodes.iterator();
		// order the elements
		HashMap<String, Integer> elemOrders = new HashMap<String, Integer>();
		int ordinal = 0;
		Iterator<?> elemIter = scenario.getProbe().getTrajectory().stateIterator();
		while (elemIter.hasNext()) {
			elemOrders.put(((ProbeState) elemIter.next()).getElementId(),
					new Integer(ordinal));
			ordinal++;
		}
		
		// loop through all nodes
		while (it.hasNext()) {
			AcceleratorNode node = (AcceleratorNode) it.next();
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
			
			int devI = 0;
			
			if (elems != null)
			for (int i = 0; i < elems.size(); i++) {
				if (!((Element) elems.get(i)).getId().endsWith("xx")
						&& !((Element) elems.get(i)).getId().endsWith("yx")) {
					// reset machineModelDetail
					MachineModelDetail machineModelDetail = new MachineModelDetail();
					for(int j=0; j<MachineModelDetail.getPropertySize(); j++)
						machineModelDetail.addPropertyValue(j, null);
					machineModelDetail.setPropertyValue("RUNS_ID", autoRunID);
					
					// trim off anything after ":"
					// I forgot why we need this. --pc
					String nodeId = null;
					if (node.getId().contains(":")) {
						int k = node.getId().indexOf(":");
						nodeId = node.getId().substring(0, k);
					} else {
						nodeId = node.getId();
					}
					machineModelDetail.setPropertyValue("ELEMENT_NAME", nodeId);
					machineModelDetail.setPropertyValue("DEVICE_TYPE", deviceType
							.getDeviceType(machineModelDetail.getPropertyValue("ELEMENT_NAME").toString()));
					machineModelDetail.setPropertyValue("EPICS_NAME", deviceType
							.getEPICSName(machineModelDetail.getPropertyValue("ELEMENT_NAME").toString()));
					
					double s = 0.;
					// treat Q30615x and Q30715x differently
					if (nodeId.contains("Q30615") || nodeId.contains("Q30715")) {
						// TODO HARDCODED
						machineModelDetail.setPropertyValue("INDEX_SLICE_CHK", "1");
						s = useSDisplay ? node.getSDisplay() : seq.getPosition(node);
					} else if (node.isKindOf(RfGap.s_strType)) {
						double gapLength = ((RfGap) node)
								.getGapLength();
						if (i == 0) {
							machineModelDetail.setPropertyValue("INDEX_SLICE_CHK", "0");
							s = (useSDisplay ? node.getParent().getSDisplay() : seq.getPosition(node.getParent())) 
									+ node.getPosition() - gapLength / 2.;
						} else {
							machineModelDetail.setPropertyValue("INDEX_SLICE_CHK", "2");
							s = (useSDisplay ? node.getParent().getSDisplay() : seq.getPosition(node.getParent())) 
									+ node.getPosition() + gapLength / 2.;
						}
					} else {

						if (elems.size() == 1) {
							machineModelDetail.setPropertyValue("INDEX_SLICE_CHK", String.valueOf(devI));
							s = useSDisplay ? node.getSDisplay() : seq.getPosition(node);
						} else {
							machineModelDetail.setPropertyValue("INDEX_SLICE_CHK", String.valueOf(devI));
							double sLength =0.;
							double halfLength = 0.;
							if (node instanceof Magnet) {
								sLength= ((Magnet)node).getEffLength() / (elems.size()-1);
								halfLength = ((Magnet)node).getEffLength() / 2.;
							}
							else {
								sLength= node.getLength() / (elems.size() -1);
								halfLength = node.getLength() / 2.0;
							}
							
							s = (useSDisplay ? node.getSDisplay() : seq.getPosition(node)) - halfLength + i
									* sLength;
						}

						devI++;
					}
					// get element
					Element elem = (Element) elems.get(i);

					// get element length
					machineModelDetail.setPropertyValue("LEFF", df.format(node_length));
					machineModelDetail.setPropertyValue("SLEFF", df.format(elem.getLength()));

					// get state for the element
					try {
						List<? extends ProbeState<?>> states = scenario
								.trajectoryStatesForElement(elem.getId());
						// We only output begin/middle/end for a thick device,
						// therefore, no need to go through all the states but only the
						// last state for that element.
						if (states == null) continue;
						
						ProbeState state = states.get(states.size() - 1);

						// z-pos offset correction
						machineModelDetail.setPropertyValue("ZPOS", df.format(s));
						machineModelDetail.setPropertyValue("SUML", df.format(state.getPosition()));
						machineModelDetail.setPropertyValue("ORDINAL", elemOrders.get(state.getElementId()).toString());

						if (state instanceof EnvelopeProbeState) {
							Twiss[] twiss = ((EnvelopeProbeState) state)
									.twissParameters();
							PhaseVector betatronPhase = ((EnvelopeProbeState) state)
									.phaseMean(); //getBetatronPhase();
							machineModelDetail.setPropertyValue("BETA_X", df.format(twiss[0].getBeta()));
							machineModelDetail.setPropertyValue("ALPHA_X", df.format(twiss[0].getAlpha()));
							machineModelDetail.setPropertyValue("BETA_Y", df.format(twiss[1].getBeta()));
							machineModelDetail.setPropertyValue("ALPHA_Y", df.format(twiss[1].getAlpha()));
							/* TODO OPENXAL
							machineModelDetail.setPropertyValue("ETA_X", df.format(((EnvelopeProbeState) state).getChromDispersionX()));
							machineModelDetail.setPropertyValue("ETA_Y", df.format(((EnvelopeProbeState) state).getChromDispersionY()));
							machineModelDetail.setPropertyValue("ETAP_X", df.format(((EnvelopeProbeState) state).getChromDispersionSlopeX()));
							machineModelDetail.setPropertyValue("ETAP_Y", df.format(((EnvelopeProbeState) state).getChromDispersionSlopeY()));
							*/
							machineModelDetail.setPropertyValue("PSI_X", df.format(betatronPhase.getx()));
							machineModelDetail.setPropertyValue("PSI_Y", df.format(betatronPhase.gety()));
							machineModelDetail.setPropertyValue("E", df.format(getTotalEnergyFromKinetic(state.getSpeciesRestEnergy() / 1.e9,state.getKineticEnergy() / 1.e9)));
							machineModelDetail.setPropertyValue("P", df.format(RelativisticParameterConverter.
									computeMomentumFromEnergies(state.getKineticEnergy(), state.getSpeciesRestEnergy())/1e9));
							machineModelDetail.setPropertyValue("Bmag_X", "1");
							machineModelDetail.setPropertyValue("Bmag_Y", "1");
							
							PhaseMatrix rMat = ((EnvelopeProbeState) state)
									.getResponseMatrix();
							machineModelDetail.setPropertyValue("R11", df.format(rMat.getElem(0, 0)));
							machineModelDetail.setPropertyValue("R12", df.format(rMat.getElem(0, 1)));
							machineModelDetail.setPropertyValue("R13", df.format(rMat.getElem(0, 2)));
							machineModelDetail.setPropertyValue("R14", df.format(rMat.getElem(0, 3)));
							machineModelDetail.setPropertyValue("R15", df.format(rMat.getElem(0, 4)));
							machineModelDetail.setPropertyValue("R16", df.format(rMat.getElem(0, 5)));
							machineModelDetail.setPropertyValue("R21", df.format(rMat.getElem(1, 0)));
							machineModelDetail.setPropertyValue("R22", df.format(rMat.getElem(1, 1)));
							machineModelDetail.setPropertyValue("R23", df.format(rMat.getElem(1, 2)));
							machineModelDetail.setPropertyValue("R24", df.format(rMat.getElem(1, 3)));
							machineModelDetail.setPropertyValue("R25", df.format(rMat.getElem(1, 4)));
							machineModelDetail.setPropertyValue("R26", df.format(rMat.getElem(1, 5)));
							machineModelDetail.setPropertyValue("R31", df.format(rMat.getElem(2, 0)));
							machineModelDetail.setPropertyValue("R32", df.format(rMat.getElem(2, 1)));
							machineModelDetail.setPropertyValue("R33", df.format(rMat.getElem(2, 2)));
							machineModelDetail.setPropertyValue("R34", df.format(rMat.getElem(2, 3)));
							machineModelDetail.setPropertyValue("R35", df.format(rMat.getElem(2, 4)));
							machineModelDetail.setPropertyValue("R36", df.format(rMat.getElem(2, 5)));
							machineModelDetail.setPropertyValue("R41", df.format(rMat.getElem(3, 0)));
							machineModelDetail.setPropertyValue("R42", df.format(rMat.getElem(3, 1)));
							machineModelDetail.setPropertyValue("R43", df.format(rMat.getElem(3, 2)));
							machineModelDetail.setPropertyValue("R44", df.format(rMat.getElem(3, 3)));
							machineModelDetail.setPropertyValue("R45", df.format(rMat.getElem(3, 4)));
							machineModelDetail.setPropertyValue("R46", df.format(rMat.getElem(3, 5)));
							machineModelDetail.setPropertyValue("R51", df.format(rMat.getElem(4, 0)));
							machineModelDetail.setPropertyValue("R52", df.format(rMat.getElem(4, 1)));
							machineModelDetail.setPropertyValue("R53", df.format(rMat.getElem(4, 2)));
							machineModelDetail.setPropertyValue("R54", df.format(rMat.getElem(4, 3)));
							machineModelDetail.setPropertyValue("R55", df.format(rMat.getElem(4, 4)));
							machineModelDetail.setPropertyValue("R56", df.format(rMat.getElem(4, 5)));
							machineModelDetail.setPropertyValue("R61", df.format(rMat.getElem(5, 0)));
							machineModelDetail.setPropertyValue("R62", df.format(rMat.getElem(5, 1)));
							machineModelDetail.setPropertyValue("R63", df.format(rMat.getElem(5, 2)));
							machineModelDetail.setPropertyValue("R64", df.format(rMat.getElem(5, 3)));
							machineModelDetail.setPropertyValue("R65", df.format(rMat.getElem(5, 4)));
							machineModelDetail.setPropertyValue("R66", df.format(rMat.getElem(5, 5)));
						} else if (state instanceof TransferMapState) {
							PhaseMatrix transferMatrix = ((TransferMapState) state).getPartialTransferMap().getFirstOrder();
							CovarianceMatrix covMatrix = new CovarianceMatrix(transferMatrix);
							
							Twiss[] twiss = covMatrix.computeTwiss();							
							PhaseVector betatronPhase = covMatrix.getMean();
							
							machineModelDetail.setPropertyValue("BETA_X", df.format(twiss[0].getBeta()));
							machineModelDetail.setPropertyValue("ALPHA_X", df.format(twiss[0].getAlpha()));
							machineModelDetail.setPropertyValue("BETA_Y", df.format(twiss[1].getBeta()));
							machineModelDetail.setPropertyValue("ALPHA_Y", df.format(twiss[1].getAlpha()));
							/* TODO OPENXAL
							machineModelDetail.setPropertyValue("ETA_X", df.format(((TransferMapState) state).getChromDispersionX()));
							machineModelDetail.setPropertyValue("ETA_Y", df.format(((TransferMapState) state).getChromDispersionY()));
							machineModelDetail.setPropertyValue("ETAP_X", df.format(((TransferMapState) state).getChromDispersionSlopeX()));
							machineModelDetail.setPropertyValue("ETAP_Y", df.format(((TransferMapState) state).getChromDispersionSlopeY()));
							*/machineModelDetail.setPropertyValue("PSI_X", df.format(betatronPhase.getx()));
							machineModelDetail.setPropertyValue("PSI_Y", df.format(betatronPhase.gety()));
							machineModelDetail.setPropertyValue("EK", df.format(getTotalEnergyFromKinetic(state.getSpeciesRestEnergy() / 1.e9,state.getKineticEnergy() / 1.e9)));

							machineModelDetail.setPropertyValue("R11", df.format(transferMatrix.getElem(0, 0)));
							machineModelDetail.setPropertyValue("R12", df.format(transferMatrix.getElem(0, 1)));
							machineModelDetail.setPropertyValue("R13", df.format(transferMatrix.getElem(0, 2)));
							machineModelDetail.setPropertyValue("R14", df.format(transferMatrix.getElem(0, 3)));
							machineModelDetail.setPropertyValue("R15", df.format(transferMatrix.getElem(0, 4)));
							machineModelDetail.setPropertyValue("R16", df.format(transferMatrix.getElem(0, 5)));
							machineModelDetail.setPropertyValue("R21", df.format(transferMatrix.getElem(1, 0)));
							machineModelDetail.setPropertyValue("R22", df.format(transferMatrix.getElem(1, 1)));
							machineModelDetail.setPropertyValue("R23", df.format(transferMatrix.getElem(1, 2)));
							machineModelDetail.setPropertyValue("R24", df.format(transferMatrix.getElem(1, 3)));
							machineModelDetail.setPropertyValue("R25", df.format(transferMatrix.getElem(1, 4)));
							machineModelDetail.setPropertyValue("R26", df.format(transferMatrix.getElem(1, 5)));
							machineModelDetail.setPropertyValue("R31", df.format(transferMatrix.getElem(2, 0)));
							machineModelDetail.setPropertyValue("R32", df.format(transferMatrix.getElem(2, 1)));
							machineModelDetail.setPropertyValue("R33", df.format(transferMatrix.getElem(2, 2)));
							machineModelDetail.setPropertyValue("R34", df.format(transferMatrix.getElem(2, 3)));
							machineModelDetail.setPropertyValue("R35", df.format(transferMatrix.getElem(2, 4)));
							machineModelDetail.setPropertyValue("R36", df.format(transferMatrix.getElem(2, 5)));
							machineModelDetail.setPropertyValue("R41", df.format(transferMatrix.getElem(3, 0)));
							machineModelDetail.setPropertyValue("R42", df.format(transferMatrix.getElem(3, 1)));
							machineModelDetail.setPropertyValue("R43", df.format(transferMatrix.getElem(3, 2)));
							machineModelDetail.setPropertyValue("R44", df.format(transferMatrix.getElem(3, 3)));
							machineModelDetail.setPropertyValue("R45", df.format(transferMatrix.getElem(3, 4)));
							machineModelDetail.setPropertyValue("R46", df.format(transferMatrix.getElem(3, 5)));
							machineModelDetail.setPropertyValue("R51", df.format(transferMatrix.getElem(4, 0)));
							machineModelDetail.setPropertyValue("R52", df.format(transferMatrix.getElem(4, 1)));
							machineModelDetail.setPropertyValue("R53", df.format(transferMatrix.getElem(4, 2)));
							machineModelDetail.setPropertyValue("R54", df.format(transferMatrix.getElem(4, 3)));
							machineModelDetail.setPropertyValue("R55", df.format(transferMatrix.getElem(4, 4)));
							machineModelDetail.setPropertyValue("R56", df.format(transferMatrix.getElem(4, 5)));
							machineModelDetail.setPropertyValue("R61", df.format(transferMatrix.getElem(5, 0)));
							machineModelDetail.setPropertyValue("R62", df.format(transferMatrix.getElem(5, 1)));
							machineModelDetail.setPropertyValue("R63", df.format(transferMatrix.getElem(5, 2)));
							machineModelDetail.setPropertyValue("R64", df.format(transferMatrix.getElem(5, 3)));
							machineModelDetail.setPropertyValue("R65", df.format(transferMatrix.getElem(5, 4)));
							machineModelDetail.setPropertyValue("R66", df.format(transferMatrix.getElem(5, 5)));
						}
					} catch (ModelException e) {
						e.printStackTrace();
						Message.error("Model Exception: Cannot get model data possibly due to a failed model run.", true);
					}
					runMachineModelDetail.add(machineModelDetail);
				}
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
	
	public static MachineModelDevice[] getRunMachineModeDevice(Scenario scenario) {
		//DecimalFormat df = new DecimalFormat("##0.000000");
		ModelFormat df = new ModelFormat("%17.12g");
		List<MachineModelDevice> runMachineModelDevice = new ArrayList<MachineModelDevice>();
		AcceleratorSeq seq = scenario.getSequence();
		List<AcceleratorNode> allNodes = seq.getAllNodes();
		Iterator<AcceleratorNode> it = allNodes.iterator();
		while (it.hasNext()) {
			AcceleratorNode node = it.next();
			String runMode = scenario.getSynchronizationMode();
			// for magnets
			if (node.isKindOf("emag")) {
				try {
					MachineModelDevice tmp1 = new MachineModelDevice();
					tmp1.addPropertyValue("ELEMENT_NAME", node.getId());
					tmp1.addPropertyValue("DEVICE_PROPERTY", "B");
					tmp1.addPropertyValue("DEVICE_VALUE", scenario.propertiesForNode(node)
									.get(ElectromagnetPropertyAccessor.PROPERTY_FIELD).toString());
					tmp1.addPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp1);
				} catch (SynchronizationException e) {
					Message.error("Model Synchronization Exception: Cannot synchronize device data for model run.", true);
					e.printStackTrace();
				}
				if (runMode == "DESIGN") {
					MachineModelDevice tmp2 = new MachineModelDevice();
					tmp2.addPropertyValue("ELEMENT_NAME", node.getId());
					tmp2.addPropertyValue("DEVICE_PROPERTY", "BACT");
					tmp2.addPropertyValue("DEVICE_VALUE", Double.toString(((Electromagnet) node).getDesignField()));
					tmp2.addPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp2);
					
					MachineModelDevice tmp3 = new MachineModelDevice();
					tmp3.addPropertyValue("ELEMENT_NAME", node.getId());
					tmp3.addPropertyValue("DEVICE_PROPERTY", "BDES");
					tmp3.addPropertyValue("DEVICE_VALUE", Double.toString(((Electromagnet) node).getDesignField()));
					tmp3.addPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp3);
				} else {
					try {
						MachineModelDevice tmp4 = new MachineModelDevice();
						tmp4.addPropertyValue("ELEMENT_NAME", node.getId());
						tmp4.addPropertyValue("DEVICE_PROPERTY", "BACT");
						tmp4.addPropertyValue("DEVICE_VALUE", Double.toString(((Electromagnet) node).getFieldReadback()));
						tmp4.addPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
						runMachineModelDevice.add(tmp4);
						
						MachineModelDevice tmp5 = new MachineModelDevice();
						tmp5.addPropertyValue("ELEMENT_NAME", node.getId());
						tmp5.addPropertyValue("DEVICE_PROPERTY", "BDES");
						tmp5.addPropertyValue("DEVICE_VALUE", Double.toString(((Electromagnet) node).getFieldSetting()));
						tmp5.addPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
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
			else if (node.isKindOf("Bnch")) {
				try {
					MachineModelDevice tmp6 = new MachineModelDevice();
					tmp6.addPropertyValue("ELEMENT_NAME", node.getId());
					tmp6.addPropertyValue("DEVICE_PROPERTY", "P");
					tmp6.addPropertyValue("DEVICE_VALUE", scenario.propertiesForNode(node).get(
							RfCavityPropertyAccessor.PROPERTY_PHASE).toString());
					tmp6.addPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp6);
					
					MachineModelDevice tmp7 = new MachineModelDevice();
					tmp7.addPropertyValue("ELEMENT_NAME", node.getId());
					tmp7.addPropertyValue("DEVICE_PROPERTY", "A");
					tmp7.addPropertyValue("DEVICE_VALUE", scenario.propertiesForNode(node)
							.get(RfCavityPropertyAccessor.PROPERTY_AMPLITUDE).toString());
					tmp7.addPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp7);
				} catch (SynchronizationException e) {
					Message.error("Model Synchronization Exception: Cannot synchronize device data for model run.", true);
					e.printStackTrace();
				}
				if (runMode == "DESIGN") {
					MachineModelDevice tmp8 = new MachineModelDevice();
					tmp8.addPropertyValue("ELEMENT_NAME", node.getId());
					tmp8.addPropertyValue("DEVICE_PROPERTY", "PDES");
					tmp8.addPropertyValue("DEVICE_VALUE", Double.toString(((RfCavity) node).getDfltAvgCavPhase()));
					tmp8.addPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp8);
					
					MachineModelDevice tmp9 = new MachineModelDevice();
					tmp9.addPropertyValue("ELEMENT_NAME", node.getId());
					tmp9.addPropertyValue("DEVICE_PROPERTY", "ADES");
					tmp9.addPropertyValue("DEVICE_VALUE", Double.toString(((RfCavity) node).getDfltCavAmp()));
					tmp9.addPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp9);
				}
				// We use "design" values for both cases for now because we
				// don't have access to live readbacks.
				else {
				}
			}
		}
		return runMachineModelDevice.toArray(new MachineModelDevice[runMachineModelDevice.size()]);
	}
	
	static String database = null;

	static String runID = null;
	static boolean writeToModelDevicesDB_done = false;
	static boolean writeToElementModelsDB_done = false;
	
	public static boolean getElementModelsDB_done() {
		return writeToElementModelsDB_done;
	}
	
	public static int status; // 0 = failed , 1=finished , 2=pending
	
	public static String newUploadToDatabase(final JFrame parent,
			BrowserModel model,
			MachineModel runMachineModel,
			final MachineModelDetail[] runMachineModelDetail,
			final MachineModelDevice[] runMachineModelDevice) {
		
		status = 2; //status=pending
		
		try {
//			writeConnection = dialog.showConnectionDialog(DatabaseAdaptor.getInstance());
			writeConnection = DataManager.getConnection();
			
		} catch (Exception exception) {
				JOptionPane.showMessageDialog(parent, exception.getMessage(),
						"Connection Error!  Cannot connect to " + url, JOptionPane.ERROR_MESSAGE);
				Logger.getLogger("global").log(Level.SEVERE,
						"Database connection error.  Cannot connect to " + url, exception);
				Message.error("Connection exception: Cannot connect to database" + url, true);
		}

		ArrayList<String> elementName = new ArrayList<String>();
		ArrayList<String> elementID = new ArrayList<String>();
		ArrayList<String> elementTypeID = new ArrayList<String>();
		int index;
		ResultSet rs;

		// prepare for MACHINE_MODEL.RUNS
		String runs[] = new String[11];
		runs[0] = "1";
		runs[1] = null;
		runs[2] = "MACHINE_MODEL";
		runs[3] = null;
		runs[4] = runMachineModel.getPropertyValue("RUN_SOURCE_CHK").toString();
		runs[5] = null;
		runs[6] = null;
		runs[7] = null;
		runs[8] = null;
		runs[9] = runMachineModel.getPropertyValue("COMMENTS").toString();
		runs[10]= runMachineModel.getPropertyValue("MODEL_MODES_ID").toString();		
		
		// prepare for MACHINE_MODEL.MODEL_DEVICES
		String modelDevices[] = new String[runMachineModelDevice.length * 4];
		try{
			/*PreparedStatement stmt2 = writeConnection.prepareStatement("SELECT L.\"ELEMENT\", L.\"ELEMENT_ID\", D.\"ID\" " +
					"FROM \"MACHINE_MODEL\".\"DEVICE_TYPES\" D, \"LCLS_INFRASTRUCTURE\".\"LCLS_ELEMENTS\" L " +
			"WHERE L.\"KEYWORD\" = D.\"DEVICE_TYPE\"");*/
			// TODO OPENXAL
			PreparedStatement stmt2 = writeConnection.prepareStatement("SELECT D.\"DEVICE_TYPE\", D.\"ID\", D.\"ID\" " +
					"FROM \"MACHINE_MODEL\".\"DEVICE_TYPES\" D");
			rs = stmt2.executeQuery();
			while(rs.next()){
				elementName.add(rs.getString(1));
				elementID.add(rs.getString(2));
				elementTypeID.add(rs.getString(3));
			}

			// reset deviceID
			deviceID.clear();
			for(int i=0; i<runMachineModelDevice.length; i++){
				index = elementName.indexOf(runMachineModelDevice[i].getPropertyValue("ELEMENT_NAME").toString());
				if(index >= 0){
					deviceID.add(elementID.get(index));
					deviceTypeID.add(elementTypeID.get(index));
				}else{
					deviceID.add(null);
					deviceTypeID.add(null);
				}
				
				modelDevices[i*4] = deviceID.get(i);
				modelDevices[i*4+1] = deviceTypeID.get(i);
				modelDevices[i*4+2] = runMachineModelDevice[i].getPropertyValue("DEVICE_PROPERTY").toString();
				modelDevices[i*4+3] = runMachineModelDevice[i].getPropertyValue("DEVICE_VALUE").toString();				
			}
		} catch (Exception exception) {
			JOptionPane.showMessageDialog(parent, exception.getMessage(),
					"SQL Error!  Query failed on" + url, JOptionPane.ERROR_MESSAGE);
			Logger.getLogger("global").log(Level.SEVERE,
					"Database SQL error.", exception);
			Message.error("SQLException: Query failed on " + url, true);
			if (runID == null)
				return null;
		}
		// prepare for MACHINE_MODEL.ELEMENT_MODELS
		String modelDetail[] = new String[runMachineModelDetail.length*55];
//		try {
			//Upload MACHINE_MODEL.ELEMENT_MODELS
			deviceID1.clear();
			for(int i=0; i<runMachineModelDetail.length; i++){
				index = elementName.indexOf(runMachineModelDetail[i].getPropertyValue("ELEMENT_NAME").toString());
				if(index >= 0){
					deviceID1.add(elementID.get(index));
				}else{
					deviceID1.add(null);
				}
				
				modelDetail[i*55] = deviceID1.get(i);
				modelDetail[i*55+1] = runMachineModelDetail[i].getPropertyValue("ELEMENT_NAME").toString();
				modelDetail[i*55+2] = runMachineModelDetail[i].getPropertyValue("INDEX_SLICE_CHK").toString();
				modelDetail[i*55+3] = runMachineModelDetail[i].getPropertyValue("ZPOS").toString();
				modelDetail[i*55+4] = runMachineModelDetail[i].getPropertyValue("E").toString();
				modelDetail[i*55+5] = runMachineModelDetail[i].getPropertyValue("ALPHA_X").toString();
				modelDetail[i*55+6] = runMachineModelDetail[i].getPropertyValue("ALPHA_Y").toString();
				modelDetail[i*55+7] = runMachineModelDetail[i].getPropertyValue("BETA_X").toString();
				modelDetail[i*55+8] = runMachineModelDetail[i].getPropertyValue("BETA_Y").toString();
				modelDetail[i*55+9] = runMachineModelDetail[i].getPropertyValue("PSI_X").toString();
				modelDetail[i*55+10] = runMachineModelDetail[i].getPropertyValue("PSI_Y").toString();
				modelDetail[i*55+11] = runMachineModelDetail[i].getPropertyValue("ETA_X").toString();
				modelDetail[i*55+12] = runMachineModelDetail[i].getPropertyValue("ETA_Y").toString();
				modelDetail[i*55+13] = runMachineModelDetail[i].getPropertyValue("ETAP_X").toString();
				modelDetail[i*55+14] = runMachineModelDetail[i].getPropertyValue("ETAP_Y").toString();
				modelDetail[i*55+15] = runMachineModelDetail[i].getPropertyValue("R11").toString();
				modelDetail[i*55+16] = runMachineModelDetail[i].getPropertyValue("R12").toString();
				modelDetail[i*55+17] = runMachineModelDetail[i].getPropertyValue("R13").toString();
				modelDetail[i*55+18] = runMachineModelDetail[i].getPropertyValue("R14").toString();
				modelDetail[i*55+19] = runMachineModelDetail[i].getPropertyValue("R15").toString();
				modelDetail[i*55+20] = runMachineModelDetail[i].getPropertyValue("R16").toString();
				modelDetail[i*55+21] = runMachineModelDetail[i].getPropertyValue("R21").toString();
				modelDetail[i*55+22] = runMachineModelDetail[i].getPropertyValue("R22").toString();
				modelDetail[i*55+23] = runMachineModelDetail[i].getPropertyValue("R23").toString();
				modelDetail[i*55+24] = runMachineModelDetail[i].getPropertyValue("R24").toString();
				modelDetail[i*55+25] = runMachineModelDetail[i].getPropertyValue("R25").toString();
				modelDetail[i*55+26] = runMachineModelDetail[i].getPropertyValue("R26").toString();
				modelDetail[i*55+27] = runMachineModelDetail[i].getPropertyValue("R31").toString();
				modelDetail[i*55+28] = runMachineModelDetail[i].getPropertyValue("R32").toString();
				modelDetail[i*55+29] = runMachineModelDetail[i].getPropertyValue("R33").toString();
				modelDetail[i*55+30] = runMachineModelDetail[i].getPropertyValue("R34").toString();
				modelDetail[i*55+31] = runMachineModelDetail[i].getPropertyValue("R35").toString();
				modelDetail[i*55+32] = runMachineModelDetail[i].getPropertyValue("R36").toString();
				modelDetail[i*55+33] = runMachineModelDetail[i].getPropertyValue("R41").toString();
				modelDetail[i*55+34] = runMachineModelDetail[i].getPropertyValue("R42").toString();
				modelDetail[i*55+35] = runMachineModelDetail[i].getPropertyValue("R43").toString();
				modelDetail[i*55+36] = runMachineModelDetail[i].getPropertyValue("R44").toString();
				modelDetail[i*55+37] = runMachineModelDetail[i].getPropertyValue("R45").toString();
				modelDetail[i*55+38] = runMachineModelDetail[i].getPropertyValue("R46").toString();
				modelDetail[i*55+39] = runMachineModelDetail[i].getPropertyValue("R51").toString();
				modelDetail[i*55+40] = runMachineModelDetail[i].getPropertyValue("R52").toString();
				modelDetail[i*55+41] = runMachineModelDetail[i].getPropertyValue("R53").toString();
				modelDetail[i*55+42] = runMachineModelDetail[i].getPropertyValue("R54").toString();
				modelDetail[i*55+43] = runMachineModelDetail[i].getPropertyValue("R55").toString();
				modelDetail[i*55+44] = runMachineModelDetail[i].getPropertyValue("R56").toString();
				modelDetail[i*55+45] = runMachineModelDetail[i].getPropertyValue("R61").toString();
				modelDetail[i*55+46] = runMachineModelDetail[i].getPropertyValue("R62").toString();
				modelDetail[i*55+47] = runMachineModelDetail[i].getPropertyValue("R63").toString();
				modelDetail[i*55+48] = runMachineModelDetail[i].getPropertyValue("R64").toString();
				modelDetail[i*55+49] = runMachineModelDetail[i].getPropertyValue("R65").toString();
				modelDetail[i*55+50] = runMachineModelDetail[i].getPropertyValue("R66").toString();
				modelDetail[i*55+51] = runMachineModelDetail[i].getPropertyValue("LEFF").toString();
				modelDetail[i*55+52] = runMachineModelDetail[i].getPropertyValue("SLEFF").toString();
				modelDetail[i*55+53] = runMachineModelDetail[i].getPropertyValue("ORDINAL").toString();
				modelDetail[i*55+54] = runMachineModelDetail[i].getPropertyValue("SUML").toString();
			}
//		} catch (Exception exception) {
//			JOptionPane.showMessageDialog(parent, exception.getMessage(),
//					"SQL Error! ", JOptionPane.ERROR_MESSAGE);
//			Logger.getLogger("global").log(Level.SEVERE,
//					"Database SQL error.", exception);
//			Message.error("SQLException: " + exception.getMessage());
//			if (runID != null)
//				return runID;
//			else
//				return null;
//		}
		
		//invoke PL/SQL call
		CallableStatement cstmt1 = null;
		try {
			Message.info("Uploading model data to DB...");
			cstmt1 = writeConnection.prepareCall(
			"{call MACHINE_MODEL.MODEL_UPLOAD_PKG.UPLOAD_MODEL (?, ?, ?, ?, ?)}");
			cstmt1.registerOutParameter(4, java.sql.Types.INTEGER);
			cstmt1.registerOutParameter(5, java.sql.Types.VARCHAR);
			
			ArrayDescriptor runs_descriptor =
				ArrayDescriptor.createDescriptor( "RUNS_ARRAY_TYP", writeConnection );  
			ARRAY runs_array =
				new ARRAY( runs_descriptor, writeConnection, runs );
		    cstmt1.setArray(1, runs_array);

			ArrayDescriptor model_devices_descriptor =
				ArrayDescriptor.createDescriptor( "MODEL_DEVICES_ARRAY_TYP", writeConnection );  
			ARRAY model_devices_array =
				new ARRAY( model_devices_descriptor, writeConnection, modelDevices );
		    cstmt1.setArray(2, model_devices_array);
			
			ArrayDescriptor element_models_descriptor =
				ArrayDescriptor.createDescriptor( "ELEMENT_MODELS_ARRAY_TYP", writeConnection );  
			ARRAY element_models_array =
				new ARRAY( element_models_descriptor, writeConnection, modelDetail );
		    cstmt1.setArray(3, element_models_array);
			
			cstmt1.execute();
			// wait and check to see if the RUN ID is available
			boolean runIdReady = false;
			while (!runIdReady) {
				try {
					// check once every 0.5 sec
					Thread.sleep(500);
					Message.info("RUN_ID = " + Integer.toString(cstmt1.getInt(4)));
					if (Integer.toString(cstmt1.getInt(4)) != null || Integer.toString(cstmt1.getInt(4)).equals("null"))
						runIdReady = true;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// update the RUN ID
			runID = Integer.toString(cstmt1.getInt(4));
			
			String msg = cstmt1.getString(5);
			if (msg == null) {
				msg = "Model data upload finished successfully";
				Message.info(msg, true);

				// automatically update the table after a model is uploaded
				try {
					model.removeRunModelFromFetchedModels(runID);
				} catch (ParseException e) {
					Message.error("Cannot update MODEL RUN table!");
					e.printStackTrace();
				}
			}
			// if something goes wrong
			else {
				Message.error(msg, true);
			}
			
//			System.out.println("MODEL data upload finished successfully!");
			cstmt1.close();
//			Message.info("Model data uploaded to database successfully!", true);
			status = 1; // status=finished
			
			writeConnection.close();

		} catch (SQLException e) {
			status = 0; // status=failed
			Message.error("SQLException: failed to execute PL/SQL call on " + url, true);
			e.printStackTrace();
		}	

		return runID;
	}		
	
	/**
	 * Method used to upload model data to DB.
	 * @param parent
	 * @param model
	 * @param runMachineModel
	 * @param runMachineModelDetail
	 * @param runMachineModelDevice
	 * @return
	 */
	public static String uploadToDatabase(final JFrame parent,
			BrowserModel model,
			MachineModel runMachineModel,
			final MachineModelDetail[] runMachineModelDetail,
			final MachineModelDevice[] runMachineModelDevice) {
		//String runID = null;
		
		status = 2; //status=pending
		
		long start_database = System.currentTimeMillis();
		
		// TODO remove password prompt
//		PrefsConnectionDialog dialog = PrefsConnectionDialog.getInstance(parent, false, true);
		try {
//			writeConnection = dialog.showConnectionDialog(DatabaseAdaptor.getInstance());
			writeConnection = DataManager.getConnection();
			
		} catch (Exception exception) {
				JOptionPane.showMessageDialog(parent, exception.getMessage(),
						"Connection Error!  Cannot connect to " + url, JOptionPane.ERROR_MESSAGE);
				Logger.getLogger("global").log(Level.SEVERE,
						"Database connection error.  Cannot connect to " + url, exception);
				Message.error("Connection exception: Cannot connect to " + url, true);
		}
		if (writeConnection != null) {
//			database = dialog.getDatabase();
			if (url.equals(SLACDEV_URL)) {
				database = "slacDEV";
			} else {
				database = "slacPROD";
			}

			if(database.equals("slacPROD")) {
				// construct another 2 DB connections
				try {
//					writeConnection1 = DriverManager.getConnection(MCCQA_URL, dialog.getUser(), new String(dialog.getPassword()));
					writeConnection1 = DriverManager.getConnection(MCCQA_URL);

				} catch (Exception exception) {
					JOptionPane.showMessageDialog(parent, exception.getMessage(),
							"Connection Error! Cannot connect to " + MCCQA_URL, JOptionPane.ERROR_MESSAGE);
					Logger.getLogger("global").log(Level.SEVERE,
							"Database connection error.  Cannot connect to " + MCCQA_URL, exception);
					Message.error("Connection exception: Cannot connect to " + MCCQA_URL, true);
				}
				try {
					writeConnection2 = DriverManager.getConnection(SLACPROD_URL);

				} catch (Exception exception) {
					JOptionPane.showMessageDialog(parent, exception.getMessage(),
							"Connection Error!  Cannot connect to " + SLACPROD_URL, JOptionPane.ERROR_MESSAGE);
					Logger.getLogger("global").log(Level.SEVERE,
							"Database connection error.  Cannot connect to " + SLACPROD_URL, exception);
					Message.error("Connection exception: Cannot connect to " + SLACPROD_URL, true);
				}
			}
			
			ArrayList<String> elementName = new ArrayList<String>();
			ArrayList<String> elementID = new ArrayList<String>();
			ArrayList<String> elementTypeID = new ArrayList<String>();
			int index;
			ResultSet rs;

			try {
				//Upload MACHINE_MODEL.RUNS
				Statement stmt1 = writeConnection.createStatement();
//				System.out.println("INSERT INTO MACHINE_MODEL.RUNS (HARDWARE_SETTINGS_ID, " +
//						"XML_DOCS_ID,  CREATED_BY, DATE_CREATED, RUN_SOURCE_CHK, " +
//						"RUN_ELEMENT_FILENAME, RUN_ELEMENT_DATE, RUN_DEVICE_FILENAME, RUN_DEVICE_DATE, " +
//						"COMMENTS, MODEL_MODES_ID ) " +
//						"VALUES (1, NULL, 'MACHINE_MODEL', TO_DATE('" +
//						runMachineModel.getPropertyValue("RUN_ELEMENT_DATE").toString() + 
//						"','YYYY-MM-DD HH24:MI:SS'), '" + runMachineModel.getPropertyValue("RUN_SOURCE_CHK") + "', NULL, NULL, NULL, NULL, " +
//						"'" + runMachineModel.getPropertyValue("COMMENTS").toString() + "', " +
//						runMachineModel.getPropertyValue("MODEL_MODES_ID").toString() + " )" +
//						new String[]{"ID"});
				stmt1.executeUpdate("INSERT INTO MACHINE_MODEL.RUNS (HARDWARE_SETTINGS_ID, " +
						"XML_DOCS_ID,  CREATED_BY, DATE_CREATED, RUN_SOURCE_CHK, " +
						"RUN_ELEMENT_FILENAME, RUN_ELEMENT_DATE, RUN_DEVICE_FILENAME, RUN_DEVICE_DATE, " +
						"COMMENTS, MODEL_MODES_ID ) " +
						"VALUES (1, NULL, 'MACHINE_MODEL', TO_DATE('" +
						runMachineModel.getPropertyValue("RUN_ELEMENT_DATE").toString() + 
						"','YYYY-MM-DD HH24:MI:SS'), '" + runMachineModel.getPropertyValue("RUN_SOURCE_CHK") + "', NULL, NULL, NULL, NULL, " +
						"'" + runMachineModel.getPropertyValue("COMMENTS").toString() + "', " +
						runMachineModel.getPropertyValue("MODEL_MODES_ID").toString() + " )", 
						new String[]{"ID"});
				rs = stmt1.getGeneratedKeys();
				if (rs.next())
					runID = rs.getString(1);
				System.out.println("Upload MACHINE_MODEL.RUNS Table Done!");
				Message.info("Upload MACHINE_MODEL.RUNS Table Done!");
				
				// insert RUN info into another 2 DBs' RUNS tables with the RUN_ID obtained from MCCO 
				if (database.equals("slacPROD")) {
					// write to MCCQA
					Statement stmt2 = writeConnection1.createStatement();
//					System.out.println("INSERT INTO MACHINE_MODEL.RUNS (ID, HARDWARE_SETTINGS_ID, " +
//							"XML_DOCS_ID,  CREATED_BY, DATE_CREATED, RUN_SOURCE_CHK, " +
//							"RUN_ELEMENT_FILENAME, RUN_ELEMENT_DATE, RUN_DEVICE_FILENAME, RUN_DEVICE_DATE, " +
//							"COMMENTS, MODEL_MODES_ID ) " +
//							"VALUES (" + runID + ", 1, NULL, 'MACHINE_MODEL', TO_DATE('" +
//							runMachineModel.getPropertyValue("RUN_ELEMENT_DATE").toString() + 
//							"','YYYY-MM-DD HH24:MI:SS'), '" + runMachineModel.getPropertyValue("RUN_SOURCE_CHK") + "', NULL, NULL, NULL, NULL, " +
//							"'" + runMachineModel.getPropertyValue("COMMENTS").toString() + "', " +
//							runMachineModel.getPropertyValue("MODEL_MODES_ID").toString() + " )");
					stmt2.executeUpdate("INSERT INTO MACHINE_MODEL.RUNS (ID, HARDWARE_SETTINGS_ID, " +
							"XML_DOCS_ID,  CREATED_BY, DATE_CREATED, RUN_SOURCE_CHK, " +
							"RUN_ELEMENT_FILENAME, RUN_ELEMENT_DATE, RUN_DEVICE_FILENAME, RUN_DEVICE_DATE, " +
							"COMMENTS, MODEL_MODES_ID ) " +
							"VALUES (" + runID + ", 1, NULL, 'MACHINE_MODEL', TO_DATE('" +
							runMachineModel.getPropertyValue("RUN_ELEMENT_DATE").toString() + 
							"','YYYY-MM-DD HH24:MI:SS'), '" + runMachineModel.getPropertyValue("RUN_SOURCE_CHK") + "', NULL, NULL, NULL, NULL, " +
							"'" + runMachineModel.getPropertyValue("COMMENTS").toString() + "', " +
							runMachineModel.getPropertyValue("MODEL_MODES_ID").toString() + " )"
							);
					
					Statement stmt3 = writeConnection2.createStatement();
//					System.out.println("INSERT INTO MACHINE_MODEL.RUNS (ID, HARDWARE_SETTINGS_ID, " +
//							"XML_DOCS_ID,  CREATED_BY, DATE_CREATED, RUN_SOURCE_CHK, " +
//							"RUN_ELEMENT_FILENAME, RUN_ELEMENT_DATE, RUN_DEVICE_FILENAME, RUN_DEVICE_DATE, " +
//							"COMMENTS, MODEL_MODES_ID ) " +
//							"VALUES (" + runID + ", 1, NULL, 'MACHINE_MODEL', TO_DATE('" +
//							runMachineModel.getPropertyValue("RUN_ELEMENT_DATE").toString() + 
//							"','YYYY-MM-DD HH24:MI:SS'), '" + runMachineModel.getPropertyValue("RUN_SOURCE_CHK") + "', NULL, NULL, NULL, NULL, " +
//							"'" + runMachineModel.getPropertyValue("COMMENTS").toString() + "', " +
//							runMachineModel.getPropertyValue("MODEL_MODES_ID").toString() + " )");
					stmt3.executeUpdate("INSERT INTO MACHINE_MODEL.RUNS (ID, HARDWARE_SETTINGS_ID, " +
							"XML_DOCS_ID,  CREATED_BY, DATE_CREATED, RUN_SOURCE_CHK, " +
							"RUN_ELEMENT_FILENAME, RUN_ELEMENT_DATE, RUN_DEVICE_FILENAME, RUN_DEVICE_DATE, " +
							"COMMENTS, MODEL_MODES_ID ) " +
							"VALUES (" + runID + ", 1, NULL, 'MACHINE_MODEL', TO_DATE('" +
							runMachineModel.getPropertyValue("RUN_ELEMENT_DATE").toString() + 
							"','YYYY-MM-DD HH24:MI:SS'), '" + runMachineModel.getPropertyValue("RUN_SOURCE_CHK") + "', NULL, NULL, NULL, NULL, " +
							"'" + runMachineModel.getPropertyValue("COMMENTS").toString() + "', " +
							runMachineModel.getPropertyValue("MODEL_MODES_ID").toString() + " )"
							);
					stmt1.close();
					stmt2.close();
					stmt3.close();
				}
			} catch (Exception exception) {
				JOptionPane.showMessageDialog(parent, exception.getMessage(),
						"SQL Error!  Cannot execute SQL on one or more databases.", JOptionPane.ERROR_MESSAGE);
				Logger.getLogger("global").log(Level.SEVERE,
						"Database SQL error.  Cannot execute SQL on one or more databases.", exception);
				Message.error("SQLException: Cannot execute SQL on one or more databases.", true);
				if (runID == null)
					return null;
		}
			
				//Upload MACHINE_MODEL.MODEL_DEVICES
			try {
			PreparedStatement stmt2 = writeConnection.prepareStatement("SELECT L.ELEMENT, L.ELEMENT_ID, D.ID " +
						"FROM MACHINE_MODEL.DEVICE_TYPES D, LCLS_INFRASTRUCTURE.LCLS_ELEMENTS L " +
						"WHERE L.KEYWORD = D.DEVICE_TYPE");
				rs = stmt2.executeQuery();
				while(rs.next()){
					elementName.add(rs.getString(1));
					elementID.add(rs.getString(2));
					elementTypeID.add(rs.getString(3));
					}
				
				// reset deviceID
				deviceID.clear();
				for(int i=0; i<runMachineModelDevice.length; i++){
					index = elementName.indexOf(runMachineModelDevice[i].getPropertyValue("ELEMENT_NAME").toString());
					if(index >= 0){
						deviceID.add(elementID.get(index));
						deviceTypeID.add(elementTypeID.get(index));
						}else{
							deviceID.add(null);
							deviceTypeID.add(null);
							}
					}
			
				long start1 = System.currentTimeMillis();
				System.out.println("URL: " + url);
				System.out.println("writeConnection: " + writeConnection);
				writeToModelDevicesDB(writeConnection, runID, deviceID, deviceTypeID, runMachineModelDevice);
				long finish1 = System.currentTimeMillis() - start1;
				Message.debug("***** writeToModelDevicesDB_1: " + finish1 + "ms" + " *****");
								
//				PreparedStatement stmt3 = writeConnection.prepareStatement("INSERT INTO MACHINE_MODEL.MODEL_DEVICES " +
//									"(RUNS_ID, LCLS_ELEMENTS_ELEMENT_ID, DEVICE_TYPES_ID, " +
//									"DEVICE_PROPERTY, DEVICE_VALUE) " + "VALUES (?,?,?,?,?)",
//									ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				for(int i=0; i<runMachineModelDevice.length; i++){
//					stmt3.setString(1, runID);
//					stmt3.setString(2, deviceID.get(i));
//					stmt3.setString(3, deviceTypeID.get(i));
//					stmt3.setString(4, runMachineModelDevice[i].getPropertyValue("DEVICE_PROPERTY").toString());
//					stmt3.setString(5, runMachineModelDevice[i].getPropertyValue("DEVICE_VALUE").toString());
//					stmt3.addBatch();
//					}
//				stmt3.executeBatch();
				
				// populate data for another 2 DBs' MODEL_DEVICES tables
				if (database.equals("slacPROD")) {
					
					Thread t = new Thread(new Runnable() {

						public void run() {
							try {
								try {
									writeToModelDevicesDB_done = false;
									long start2 = System.currentTimeMillis();
									DataManager.writeToModelDevicesDB(writeConnection1, runID, deviceID, deviceTypeID, runMachineModelDevice);
									long finish2 = System.currentTimeMillis() - start2;
									Message.debug("***** writeToModelDevicesDB_2: " + finish2 + "ms" + " *****");
									start2 = System.currentTimeMillis();
									DataManager.writeToModelDevicesDB(writeConnection2, runID, deviceID, deviceTypeID, runMachineModelDevice);
									finish2 = System.currentTimeMillis() - start2;
									Message.debug("***** writeToModelDevicesDB_3: " + finish2 + "ms" + " *****");
									Message.debug("Finished writeToModelDevicesDB Thread");
									writeToModelDevicesDB_done = true;
									status = 1; // status=finished
								} catch (IndexOutOfBoundsException e) {
									//System.out.println("INDEX OUT OF BOUNDS EXCEPTION!!");
									Message.error("INDEX OUT OF BOUNDS EXCEPTION!");
									status = 0; // status = failed
									e.printStackTrace();
								}
								
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								//flag here if failed
							}
							
						}

					});
					t.start(); 			
				}
				
				System.out.println("Upload MACHINE_MODEL.MODEL_DEVICES Table Done!");
				Message.info("Upload MACHINE_MODEL.MODEL_DEVICES Table Done!");		
				
//				stmt1.close();
				stmt2.close();

			} catch (Exception exception) {
				JOptionPane.showMessageDialog(parent, exception.getMessage(),
						"SQL Error!  Cannot execute SQL on one or more databases.", JOptionPane.ERROR_MESSAGE);
				Logger.getLogger("global").log(Level.SEVERE,
						"Database SQL error.  Cannot execute SQL on one or more databases.", exception);
				Message.error("SQLException: Cannot execute SQL on one or more databases.", true);
				if (runID == null)
					return null;
		}

			try {
				//Upload MACHINE_MODEL.ELEMENT_MODELS
				deviceID1.clear();
				for(int i=0; i<runMachineModelDetail.length; i++){
					index = elementName.indexOf(runMachineModelDetail[i].getPropertyValue("ELEMENT_NAME").toString());
					if(index >= 0){
						deviceID1.add(elementID.get(index));
						}else{
							deviceID1.add(null);
							}
					}
			
				long start3_1 = System.currentTimeMillis();
				DataManager.writeToElementModelsDB(writeConnection, runID, deviceID1, runMachineModelDetail);
				long finish3_1 = System.currentTimeMillis() - start3_1;
				Message.debug("***** writeToElementModelsDB_1: " + finish3_1 + "ms" + " *****");
				// populate data for another 2 DBs' ELEMENT_MODELS tables
				if (database.equals("slacPROD")) {
					
					Thread t = new Thread(new Runnable() {

						public void run() {
							long sleepBegin = System.currentTimeMillis();
							long sleepEnd = 0;
							while (!writeToModelDevicesDB_done) {
								try {
									if (sleepEnd < 30000 && status != 0) {
										Message.debug("Sleeping!");
										Thread.sleep(1000); //sleep for 30s max
										sleepEnd = System.currentTimeMillis() - sleepBegin;
										Message.debug("Sleep Time: " + sleepEnd);
									}
									else {
										Message.debug("Timed out!");
										status = 0; // status=failed
										break;
									}
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							if (status == 0) { // status=failed
								JOptionPane.showMessageDialog(parent, "Could not successfully upload model data to dataBase !",
										"Upload DataBase", JOptionPane.INFORMATION_MESSAGE);
								Message.error("ERROR: could not complete subthreads");
								}
							if (writeToModelDevicesDB_done) {
								try {
									writeToElementModelsDB_done = false;
									Message.debug("Made it to thread for writeToElementModelsDB");
									long start3_2 = System.currentTimeMillis();
									DataManager.writeToElementModelsDB(writeConnection1, runID, deviceID1, runMachineModelDetail);
									long finish3 = System.currentTimeMillis() - start3_2;
									Message.debug("***** writeToElementModelsDB_2: " + finish3 + "ms" + " *****");
									long start3_3 = System.currentTimeMillis();
									DataManager.writeToElementModelsDB(writeConnection2, runID, deviceID1, runMachineModelDetail);
									finish3 = System.currentTimeMillis() - start3_3;
									Message.debug("***** writeToElementModelsDB_3: " + finish3 + "ms" + " *****");
									writeToElementModelsDB_done = true;
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} //end if
							
							
						}

					});
					t.start();
					
				}

//				PreparedStatement stmt3 = writeConnection.prepareStatement("INSERT INTO MACHINE_MODEL.ELEMENT_MODELS (RUNS_ID, " +
//						"LCLS_ELEMENTS_ELEMENT_ID, ELEMENT_NAME, INDEX_SLICE_CHK, ZPOS, EK, " +
//						"ALPHA_X, ALPHA_Y, BETA_X, BETA_Y, PSI_X, PSI_Y, ETA_X, ETA_Y, ETAP_X, ETAP_Y, " +
//						"R11, R12, R13, R14, R15, R16, R21, R22, R23, R24, R25, R26, " +
//						"R31, R32, R33, R34, R35, R36, R41, R42, R43, R44, R45, R46, " +
//						"R51, R52, R53, R54, R55, R56, R61, R62, R63, R64, R65, R66, " +
//						"LEFF, SLEFF, ORDINAL, SUML ) " +
//						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
//						"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
//						"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )", //56
//						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//				for(int i=0; i<runMachineModelDetail.length; i++){
//					stmt3.setString(1, runID);
//					stmt3.setString(2, deviceID.get(i));
//					stmt3.setString(3, runMachineModelDetail[i].getPropertyValue("ELEMENT_NAME").toString());
//					stmt3.setString(4, runMachineModelDetail[i].getPropertyValue("INDEX_SLICE_CHK").toString());
//					stmt3.setString(5, runMachineModelDetail[i].getPropertyValue("ZPOS").toString());
//					stmt3.setString(6, runMachineModelDetail[i].getPropertyValue("E").toString());
//					stmt3.setString(7, runMachineModelDetail[i].getPropertyValue("ALPHA_X").toString());
//					stmt3.setString(8, runMachineModelDetail[i].getPropertyValue("ALPHA_Y").toString());
//					stmt3.setString(9, runMachineModelDetail[i].getPropertyValue("BETA_X").toString());
//					stmt3.setString(10, runMachineModelDetail[i].getPropertyValue("BETA_Y").toString());
//					stmt3.setString(11, runMachineModelDetail[i].getPropertyValue("PSI_X").toString());
//					stmt3.setString(12, runMachineModelDetail[i].getPropertyValue("PSI_Y").toString());
//					stmt3.setString(13, runMachineModelDetail[i].getPropertyValue("ETA_X").toString());
//					stmt3.setString(14, runMachineModelDetail[i].getPropertyValue("ETA_Y").toString());
//					stmt3.setString(15, runMachineModelDetail[i].getPropertyValue("ETAP_X").toString());
//					stmt3.setString(16, runMachineModelDetail[i].getPropertyValue("ETAP_Y").toString());
//					stmt3.setString(17, runMachineModelDetail[i].getPropertyValue("R11").toString());
//					stmt3.setString(18, runMachineModelDetail[i].getPropertyValue("R12").toString());
//					stmt3.setString(19, runMachineModelDetail[i].getPropertyValue("R13").toString());
//					stmt3.setString(20, runMachineModelDetail[i].getPropertyValue("R14").toString());
//					stmt3.setString(21, runMachineModelDetail[i].getPropertyValue("R15").toString());
//					stmt3.setString(22, runMachineModelDetail[i].getPropertyValue("R16").toString());
//					stmt3.setString(23, runMachineModelDetail[i].getPropertyValue("R21").toString());
//					stmt3.setString(24, runMachineModelDetail[i].getPropertyValue("R22").toString());
//					stmt3.setString(25, runMachineModelDetail[i].getPropertyValue("R23").toString());
//					stmt3.setString(26, runMachineModelDetail[i].getPropertyValue("R24").toString());
//					stmt3.setString(27, runMachineModelDetail[i].getPropertyValue("R25").toString());
//					stmt3.setString(28, runMachineModelDetail[i].getPropertyValue("R26").toString());
//					stmt3.setString(29, runMachineModelDetail[i].getPropertyValue("R31").toString());
//					stmt3.setString(30, runMachineModelDetail[i].getPropertyValue("R32").toString());
//					stmt3.setString(31, runMachineModelDetail[i].getPropertyValue("R33").toString());
//					stmt3.setString(32, runMachineModelDetail[i].getPropertyValue("R34").toString());
//					stmt3.setString(33, runMachineModelDetail[i].getPropertyValue("R35").toString());
//					stmt3.setString(34, runMachineModelDetail[i].getPropertyValue("R36").toString());
//					stmt3.setString(35, runMachineModelDetail[i].getPropertyValue("R41").toString());
//					stmt3.setString(36, runMachineModelDetail[i].getPropertyValue("R42").toString());
//					stmt3.setString(37, runMachineModelDetail[i].getPropertyValue("R43").toString());
//					stmt3.setString(38, runMachineModelDetail[i].getPropertyValue("R44").toString());
//					stmt3.setString(39, runMachineModelDetail[i].getPropertyValue("R45").toString());
//					stmt3.setString(40, runMachineModelDetail[i].getPropertyValue("R46").toString());
//					stmt3.setString(41, runMachineModelDetail[i].getPropertyValue("R51").toString());
//					stmt3.setString(42, runMachineModelDetail[i].getPropertyValue("R52").toString());
//					stmt3.setString(43, runMachineModelDetail[i].getPropertyValue("R53").toString());
//					stmt3.setString(44, runMachineModelDetail[i].getPropertyValue("R54").toString());
//					stmt3.setString(45, runMachineModelDetail[i].getPropertyValue("R55").toString());
//					stmt3.setString(46, runMachineModelDetail[i].getPropertyValue("R56").toString());
//					stmt3.setString(47, runMachineModelDetail[i].getPropertyValue("R61").toString());
//					stmt3.setString(48, runMachineModelDetail[i].getPropertyValue("R62").toString());
//					stmt3.setString(49, runMachineModelDetail[i].getPropertyValue("R63").toString());
//					stmt3.setString(50, runMachineModelDetail[i].getPropertyValue("R64").toString());
//					stmt3.setString(51, runMachineModelDetail[i].getPropertyValue("R65").toString());
//					stmt3.setString(52, runMachineModelDetail[i].getPropertyValue("R66").toString());
//					stmt3.setString(53, runMachineModelDetail[i].getPropertyValue("LEFF").toString());
//					stmt3.setString(54, runMachineModelDetail[i].getPropertyValue("SLEFF").toString());
//					stmt3.setString(55, runMachineModelDetail[i].getPropertyValue("ORDINAL").toString());
//					stmt3.setString(56, runMachineModelDetail[i].getPropertyValue("SUML").toString());
//					stmt3.addBatch();
//					}
//				stmt3.executeBatch();
				System.out.println("Upload MACHINE_MODEL.ELEMENT_MODELS Table Done!");
				Message.info("Upload MACHINE_MODEL.ELEMENT_MODELS Table Done!");
//				stmt1.close();
//				stmt2.close();
//				stmt3.close();
				
				// automatically update the table after a model is uploaded
				model.removeRunModelFromFetchedModels(runID);

				long finish_database = System.currentTimeMillis() - start_database;
				Message.debug("***** uploadToDatabase: " + finish_database + " *****");
				
				if (status != 0) {
					JOptionPane.showMessageDialog(parent, "Successfully uploaded model data to dataBase !",
						"Upload DataBase", JOptionPane.INFORMATION_MESSAGE);
					Message.info("Application uploaded a new machine model.");
				}


				return runID;
			} catch (Exception exception) {
					JOptionPane.showMessageDialog(parent, exception.getMessage(),
							"SQL Error!  Model data upload failed.", JOptionPane.ERROR_MESSAGE);
					Logger.getLogger("global").log(Level.SEVERE,
							"Database SQL error.  Model data upload failed.", exception);
					Message.error("SQLException:  Model data upload failed.", true);
					if (runID != null)
						return runID;
					else
						return null;
			}
		} else{
			System.out.println(	"Data upload cancelled.");
			Message.info("Data upload cancelled.");
			return null;
		}
	}
	
	private static void writeToModelDevicesDB(Connection connection, 			
			String runID, ArrayList<String> deviceID, ArrayList<String> deviceTypeID,
			MachineModelDevice[] runMachineModelDevice) throws SQLException {
		
		System.out.println("INSERT INTO MACHINE_MODEL.MODEL_DEVICES " +
				"(RUNS_ID, LCLS_ELEMENTS_ELEMENT_ID, DEVICE_TYPES_ID, " +
				"DEVICE_PROPERTY, DEVICE_VALUE) " + "VALUES (?,?,?,?,?)" +
				ResultSet.TYPE_SCROLL_SENSITIVE + ResultSet.CONCUR_READ_ONLY);
		PreparedStatement stmt3 = connection.prepareStatement("INSERT INTO MACHINE_MODEL.MODEL_DEVICES " +
				"(RUNS_ID, LCLS_ELEMENTS_ELEMENT_ID, DEVICE_TYPES_ID, " +
				"DEVICE_PROPERTY, DEVICE_VALUE) " + "VALUES (?,?,?,?,?)",
				ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		for(int i=0; i<runMachineModelDevice.length; i++){
			stmt3.setString(1, runID);
			stmt3.setString(2, deviceID.get(i));
			stmt3.setString(3, deviceTypeID.get(i));
			stmt3.setString(4, runMachineModelDevice[i].getPropertyValue("DEVICE_PROPERTY").toString());
			stmt3.setString(5, runMachineModelDevice[i].getPropertyValue("DEVICE_VALUE").toString());
			stmt3.addBatch();
		}
		long start1 = System.currentTimeMillis();
		stmt3.executeBatch();
		long finish1 = System.currentTimeMillis() - start1;
		Message.debug("writeToModelDevicedDB (.executeBatch()): " + finish1 + "ms");
		stmt3.close();
	}
	
	private static void writeToElementModelsDB(Connection connection, 
			String runID, ArrayList<String> deviceID,
			MachineModelDetail[] runMachineModelDetail) throws SQLException {
		
		System.out.println("INSERT INTO MACHINE_MODEL.ELEMENT_MODELS (RUNS_ID, " +
				"LCLS_ELEMENTS_ELEMENT_ID, ELEMENT_NAME, INDEX_SLICE_CHK, ZPOS, EK, " +
				"ALPHA_X, ALPHA_Y, BETA_X, BETA_Y, PSI_X, PSI_Y, ETA_X, ETA_Y, ETAP_X, ETAP_Y, " +
				"R11, R12, R13, R14, R15, R16, R21, R22, R23, R24, R25, R26, " +
				"R31, R32, R33, R34, R35, R36, R41, R42, R43, R44, R45, R46, " +
				"R51, R52, R53, R54, R55, R56, R61, R62, R63, R64, R65, R66, " +
				"LEFF, SLEFF, ORDINAL, SUML ) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )" + //56
				ResultSet.TYPE_SCROLL_SENSITIVE + ResultSet.CONCUR_READ_ONLY);
		PreparedStatement stmt3 = connection.prepareStatement("INSERT INTO MACHINE_MODEL.ELEMENT_MODELS (RUNS_ID, " +
				"LCLS_ELEMENTS_ELEMENT_ID, ELEMENT_NAME, INDEX_SLICE_CHK, ZPOS, EK, " +
				"ALPHA_X, ALPHA_Y, BETA_X, BETA_Y, PSI_X, PSI_Y, ETA_X, ETA_Y, ETAP_X, ETAP_Y, " +
				"R11, R12, R13, R14, R15, R16, R21, R22, R23, R24, R25, R26, " +
				"R31, R32, R33, R34, R35, R36, R41, R42, R43, R44, R45, R46, " +
				"R51, R52, R53, R54, R55, R56, R61, R62, R63, R64, R65, R66, " +
				"LEFF, SLEFF, ORDINAL, SUML ) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )", //56
				ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		for(int i=0; i<runMachineModelDetail.length; i++){
			stmt3.setString(1, runID);
			stmt3.setString(2, deviceID.get(i));
			stmt3.setString(3, runMachineModelDetail[i].getPropertyValue("ELEMENT_NAME").toString());
			stmt3.setString(4, runMachineModelDetail[i].getPropertyValue("INDEX_SLICE_CHK").toString());
			stmt3.setString(5, runMachineModelDetail[i].getPropertyValue("ZPOS").toString());
			stmt3.setString(6, runMachineModelDetail[i].getPropertyValue("E").toString());
			stmt3.setString(7, runMachineModelDetail[i].getPropertyValue("ALPHA_X").toString());
			stmt3.setString(8, runMachineModelDetail[i].getPropertyValue("ALPHA_Y").toString());
			stmt3.setString(9, runMachineModelDetail[i].getPropertyValue("BETA_X").toString());
			stmt3.setString(10, runMachineModelDetail[i].getPropertyValue("BETA_Y").toString());
			stmt3.setString(11, runMachineModelDetail[i].getPropertyValue("PSI_X").toString());
			stmt3.setString(12, runMachineModelDetail[i].getPropertyValue("PSI_Y").toString());
			stmt3.setString(13, runMachineModelDetail[i].getPropertyValue("ETA_X").toString());
			stmt3.setString(14, runMachineModelDetail[i].getPropertyValue("ETA_Y").toString());
			stmt3.setString(15, runMachineModelDetail[i].getPropertyValue("ETAP_X").toString());
			stmt3.setString(16, runMachineModelDetail[i].getPropertyValue("ETAP_Y").toString());
			stmt3.setString(17, runMachineModelDetail[i].getPropertyValue("R11").toString());
			stmt3.setString(18, runMachineModelDetail[i].getPropertyValue("R12").toString());
			stmt3.setString(19, runMachineModelDetail[i].getPropertyValue("R13").toString());
			stmt3.setString(20, runMachineModelDetail[i].getPropertyValue("R14").toString());
			stmt3.setString(21, runMachineModelDetail[i].getPropertyValue("R15").toString());
			stmt3.setString(22, runMachineModelDetail[i].getPropertyValue("R16").toString());
			stmt3.setString(23, runMachineModelDetail[i].getPropertyValue("R21").toString());
			stmt3.setString(24, runMachineModelDetail[i].getPropertyValue("R22").toString());
			stmt3.setString(25, runMachineModelDetail[i].getPropertyValue("R23").toString());
			stmt3.setString(26, runMachineModelDetail[i].getPropertyValue("R24").toString());
			stmt3.setString(27, runMachineModelDetail[i].getPropertyValue("R25").toString());
			stmt3.setString(28, runMachineModelDetail[i].getPropertyValue("R26").toString());
			stmt3.setString(29, runMachineModelDetail[i].getPropertyValue("R31").toString());
			stmt3.setString(30, runMachineModelDetail[i].getPropertyValue("R32").toString());
			stmt3.setString(31, runMachineModelDetail[i].getPropertyValue("R33").toString());
			stmt3.setString(32, runMachineModelDetail[i].getPropertyValue("R34").toString());
			stmt3.setString(33, runMachineModelDetail[i].getPropertyValue("R35").toString());
			stmt3.setString(34, runMachineModelDetail[i].getPropertyValue("R36").toString());
			stmt3.setString(35, runMachineModelDetail[i].getPropertyValue("R41").toString());
			stmt3.setString(36, runMachineModelDetail[i].getPropertyValue("R42").toString());
			stmt3.setString(37, runMachineModelDetail[i].getPropertyValue("R43").toString());
			stmt3.setString(38, runMachineModelDetail[i].getPropertyValue("R44").toString());
			stmt3.setString(39, runMachineModelDetail[i].getPropertyValue("R45").toString());
			stmt3.setString(40, runMachineModelDetail[i].getPropertyValue("R46").toString());
			stmt3.setString(41, runMachineModelDetail[i].getPropertyValue("R51").toString());
			stmt3.setString(42, runMachineModelDetail[i].getPropertyValue("R52").toString());
			stmt3.setString(43, runMachineModelDetail[i].getPropertyValue("R53").toString());
			stmt3.setString(44, runMachineModelDetail[i].getPropertyValue("R54").toString());
			stmt3.setString(45, runMachineModelDetail[i].getPropertyValue("R55").toString());
			stmt3.setString(46, runMachineModelDetail[i].getPropertyValue("R56").toString());
			stmt3.setString(47, runMachineModelDetail[i].getPropertyValue("R61").toString());
			stmt3.setString(48, runMachineModelDetail[i].getPropertyValue("R62").toString());
			stmt3.setString(49, runMachineModelDetail[i].getPropertyValue("R63").toString());
			stmt3.setString(50, runMachineModelDetail[i].getPropertyValue("R64").toString());
			stmt3.setString(51, runMachineModelDetail[i].getPropertyValue("R65").toString());
			stmt3.setString(52, runMachineModelDetail[i].getPropertyValue("R66").toString());
			stmt3.setString(53, runMachineModelDetail[i].getPropertyValue("LEFF").toString());
			stmt3.setString(54, runMachineModelDetail[i].getPropertyValue("SLEFF").toString());
			stmt3.setString(55, runMachineModelDetail[i].getPropertyValue("ORDINAL").toString());
			stmt3.setString(56, runMachineModelDetail[i].getPropertyValue("SUML").toString());
			stmt3.addBatch();
			}
		long start1 = System.currentTimeMillis();
		stmt3.executeBatch();
		long finish1 = System.currentTimeMillis() - start1;
		Message.debug("writeToElementModelsDB (.executeBatch()): " + finish1 + "ms");
		stmt3.close();
	}
	
	public static void updateAIDA() {
		//Call AIDA_XALSERV_NAMES_UPDATE
		Thread thread = new Thread(new Runnable() {
			public void run() {
				if(database.equals("slacPROD")) {

					// for MCCQA, AIDA
					CallableStatement cstmt1 = null;
					try {
						Message.info("Updating AIDA XAL names on MCCQA...");
						cstmt1 = writeConnection1.prepareCall(
						"{call MACHINE_MODEL.ONLINE_MODEL_PKG.AIDA_XALSERV_NAMES_UPDATE ('AIDA', '202', '202', 'Y')}");
						cstmt1.execute();
						System.out.println("AIDA_XALSERV_NAMES_UPDATE finished successfully!");
						cstmt1.close();
						Message.info("AIDA_XALSERV_NAMES_UPDATE finished for MCCQA successfully!");
						writeConnection1.close();
					} catch (SQLException e) {
						Message.error("SQLException: AIDA_XALSERV_NAMES_UPDATE failed on MCCQA.", true);
						e.printStackTrace();
					}	
					
					// for SLACPROD, AIDAPROD
					CallableStatement cstmt = null;
					try {
						Message.info("Updating AIDA XAL names on SLACPROD/AIDAPROD...");
						cstmt = writeConnection2.prepareCall(
						"{call MACHINE_MODEL.ONLINE_MODEL_PKG.AIDA_XALSERV_NAMES_UPDATE ('AIDAPROD', '202', '202', 'Y')}");
						cstmt.execute();
						Message.info("AIDA_XALSERV_NAMES_UPDATE finished for SLACPROD/AIDAPROD successfully!");
					} catch (SQLException e) {
						Message.error("SQLException: AIDA_XALSERV_NAMES_UPDATE failed on SLACPROD/AIDAPROD.", true);
						e.printStackTrace();
					}

					// for SLACPROD, AIDADEV
					try {
						Message.info("Updating AIDA XAL names on SLACPROD/AIDADEV...");
						cstmt = writeConnection2.prepareCall(
						"{call MACHINE_MODEL.ONLINE_MODEL_PKG.AIDA_XALSERV_NAMES_UPDATE ('AIDADEV', '202', '202', 'Y')}");
						cstmt.execute();
						System.out.println("AIDA_XALSERV_NAMES_UPDATE finished successfully!");
						cstmt.close();
						Message.info("AIDA_XALSERV_NAMES_UPDATE finished for SLACPROD/AIDADEV successfully!");
						writeConnection2.close();
					} catch (SQLException e) {
						Message.error("SQLException: AIDA_XALSERV_NAMES_UPDATE failed on SLACPROD/AIDADEV.", true);
						e.printStackTrace();
					}
					

				}
			}
		});
		thread.start();
		
	}
	
	public static void exportDetailData(JFrame parent, MachineModelDetail[] selectedMachineModelDetail) {
		RecentFileTracker _savedFileTracker = new RecentFileTracker(1, parent.getClass(),
				"recent_saved_file");
		String currentDirectory = _savedFileTracker.getRecentFolderPath();
		JFileChooser fileChooser = new JFileChooser(currentDirectory);
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.getName().endsWith(".csv") || f.isDirectory()) {
					return true;
				}
				return false;
			}

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
	
	public static void makeGold(JFrame parent, MachineModel selectedMachineModel) {
		// TODO also update other DB instances, if necessary
		
//		PrefsConnectionDialog dialog = PrefsConnectionDialog.getInstance(parent, false, true);
		Connection writeConnection = null;
		
		try {
//			writeConnection = dialog.showConnectionDialog(DatabaseAdaptor
//					.getInstance());
			writeConnection = DataManager.getConnection();

			if (writeConnection != null) {
//				database = dialog.getDatabase();
				if (url.equals(SLACDEV_URL)) {
					database = "slacDEV";
				} else {
					database = "slacPROD";
				}
			}
		} catch (Exception exception) {
			Message.error("Connection Exception: Cannot connect to " + url, true);			
				JOptionPane.showMessageDialog(parent, exception.getMessage(),
						"Connection Error!  Cannot connect to " + url, JOptionPane.ERROR_MESSAGE);
				Logger.getLogger("global").log(Level.SEVERE,
						"Database connection error.  Cannot connect to " + url, exception);
		}
		if (writeConnection != null) {
			try {
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
					public void actionPerformed(ActionEvent e) {
						comment = commentText.getText();
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
//				System.out.println("A new GOLD Model tagged!");
				String msg = cstmt.getString(1);
				
				if (msg == null) {
					msg = "A new GOLD Model is tagged successfully!";
					Message.info(msg, true);
				} else {
					Message.error(msg, true);
				}

				cstmt.close();
//				Message.info("A new GOLD Model is tagged!");
				writeConnection.close();

/*				Statement stmt1 = writeConnection.createStatement();
				stmt1.executeUpdate("INSERT INTO MACHINE_MODEL.GOLD ( RUNS_ID, COMMENTS) VALUES ( " +
						selectedMachineModel.getPropertyValue("ID").toString() + ", '" +
						comment + "' )" );
				
				// if it's for production, also update the SLACPROD
				if(database.equals("slacPROD")) {
					try {
//						writeConnection1 = DriverManager.getConnection(MCCQA_URL, dialog.getUser(), new String(dialog.getPassword()));
						writeConnection1 = DriverManager.getConnection(MCCQA_URL, user, password);

						Statement stmt3 = writeConnection1.createStatement();
						stmt3.executeUpdate("INSERT INTO MACHINE_MODEL.GOLD ( RUNS_ID, COMMENTS) VALUES ( " +
								selectedMachineModel.getPropertyValue("ID").toString() + ", '" +
								comment + "' )" );
					} catch (Exception exception) {
						JOptionPane.showMessageDialog(parent, exception.getMessage(),
								"Connection Error!", JOptionPane.ERROR_MESSAGE);
						Logger.getLogger("global").log(Level.SEVERE,
								"Database connection error.", exception);
						Message.error("Connection exception: " + exception.getMessage());
					}
					try {
//						writeConnection2 = DriverManager.getConnection(SLACPROD_URL, dialog.getUser(), new String(dialog.getPassword()));
						writeConnection2 = DriverManager.getConnection(SLACPROD_URL, user, password);

						Statement stmt2 = writeConnection2.createStatement();
						stmt2.executeUpdate("INSERT INTO MACHINE_MODEL.GOLD ( RUNS_ID, COMMENTS) VALUES ( " +
								selectedMachineModel.getPropertyValue("ID").toString() + ", '" +
								comment + "' )" );
					} catch (Exception exception) {
						JOptionPane.showMessageDialog(parent, exception.getMessage(),
								"Connection Error!", JOptionPane.ERROR_MESSAGE);
						Logger.getLogger("global").log(Level.SEVERE,
								"Database connection error.", exception);
						Message.error("Connection exception: " + exception.getMessage());
					}
					
				}
*/				
			}catch (Exception exception) {
				Message.error("SQL Exception: " + exception.getMessage(), true);			
				Message.error("Gold tag operation failed!", true);			
				JOptionPane.showMessageDialog(parent, exception.getMessage(),
						"SQL Error: Gold tag operation failed!", JOptionPane.ERROR_MESSAGE);
				Logger.getLogger("global").log(Level.SEVERE,
						"SQL Error: Gold tag operation failed!", exception);
		}
		}
	}
	
	public static void exportToXML(JFrame parent, MachineModel runMachineModel,
			Scenario scenario) {
		RecentFileTracker _savedFileTracker = new RecentFileTracker(1, parent
				.getClass(), "recent_saved_file");
		String currentDirectory = _savedFileTracker.getRecentFolderPath();
		JFileChooser fileChooser = new JFileChooser(currentDirectory);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.getName().endsWith(".xml") || f.isDirectory()) {
					return true;
					}
				return false;
				}
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
	
	public static void closeDBConnection() {
		try {
			if (writeConnection != null)
				writeConnection.close();
			if (writeConnection1 != null)
				writeConnection1.close();
			if (writeConnection2 != null)
				writeConnection2.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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


