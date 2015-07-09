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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import xal.tools.beam.calc.CalculationsOnBeams;
import xal.tools.math.r3.R3;
import edu.stanford.slac.Message.Message;

public class DataManager {
	final static public SimpleDateFormat machineModelDateFormat = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss");
	
	private static final String DB_URL_PROPERTY_NAME="DB_CONNECTION";
	private static final String DB_USERNAME_PROPERTY_NAME = "DB_USERNAME";	
	private static final String DB_PWD_PROPERTY_NAME = "DB_PASSWORD";

	public static String url = null; // Defaults to production

	private final static String autoRunID = "RUN";
	private static String comment = ""; // TODO this variable should not be global!

	private static boolean useSDisplay = false;
	
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
		runMachineModel.setPropertyValue("ID", autoRunID);
		runMachineModel.setPropertyValue("RUN_ELEMENT_DATE", machineModelDateFormat.format(new java.util.Date()));
		if(runModelMethod == 0){
			runMachineModel.setPropertyValue("RUN_SOURCE_CHK", "DESIGN");
		}else if(runModelMethod == 1){
			runMachineModel.setPropertyValue("RUN_SOURCE_CHK", "EXTANT");
		} else if(runModelMethod == 2){
			runMachineModel.setPropertyValue("RUN_SOURCE_CHK", "PVLOGGER");
		}
		if(modelMode == 0)
			runMachineModel.setPropertyValue("MODEL_MODES_ID", "5");
		else
			runMachineModel.setPropertyValue("MODEL_MODES_ID", Integer.valueOf(modelMode).toString());
		runMachineModel.setPropertyValue("COMMENTS", "");
		runMachineModel.setPropertyValue("DATE_CREATED", machineModelDateFormat.format(new java.util.Date()));
		runMachineModel.setPropertyValue("GOLD", "RUN");
		runMachineModel.setPropertyValue("REF", false);
		runMachineModel.setPropertyValue("SEL", true);
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
		CalculationsOnBeams cob = new CalculationsOnBeams(trajectory);
		
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
						machineModelDetail.setPropertyValue(j, null);
					machineModelDetail.setPropertyValue("RUNS_ID", autoRunID);
					
					// trim off anything after ":"
					// I forgot why we need this. --pc
					String nodeId = node.getId();
					/*if (node.getId().contains(":")) {
						int k = node.getId().indexOf(":");
						nodeId = node.getId().substring(0, k);
					} else {
						nodeId = node.getId();
					}*/
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
							R3 betatronPhase = cob.computeBetatronPhase((EnvelopeProbeState) state);
									
							machineModelDetail.setPropertyValue("BETA_X", df.format(twiss[0].getBeta()));
							machineModelDetail.setPropertyValue("ALPHA_X", df.format(twiss[0].getAlpha()));
							machineModelDetail.setPropertyValue("BETA_Y", df.format(twiss[1].getBeta()));
							machineModelDetail.setPropertyValue("ALPHA_Y", df.format(twiss[1].getAlpha()));
							
							PhaseVector chromDispersion = cob.computeChromDispersion((EnvelopeProbeState)state);
							machineModelDetail.setPropertyValue("ETA_X", df.format(chromDispersion.getx()));
							machineModelDetail.setPropertyValue("ETA_Y", df.format(chromDispersion.gety()));
							machineModelDetail.setPropertyValue("ETAP_X", df.format(chromDispersion.getxp()));
							machineModelDetail.setPropertyValue("ETAP_Y", df.format(chromDispersion.getyp()));
							
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
					tmp1.setPropertyValue("ELEMENT_NAME", node.getId());
					tmp1.setPropertyValue("DEVICE_PROPERTY", "B");
					tmp1.setPropertyValue("DEVICE_VALUE", scenario.propertiesForNode(node)
									.get(ElectromagnetPropertyAccessor.PROPERTY_FIELD).toString());
					tmp1.setPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp1);
				} catch (SynchronizationException e) {
					Message.error("Model Synchronization Exception: Cannot synchronize device data for model run.", true);
					e.printStackTrace();
				}
				if (Scenario.SYNC_MODE_DESIGN.equals("DESIGN")) {
					MachineModelDevice tmp2 = new MachineModelDevice();
					tmp2.setPropertyValue("ELEMENT_NAME", node.getId());
					tmp2.setPropertyValue("DEVICE_PROPERTY", "BACT");
					tmp2.setPropertyValue("DEVICE_VALUE", Double.toString(((Electromagnet) node).getDesignField()));
					tmp2.setPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp2);
					
					MachineModelDevice tmp3 = new MachineModelDevice();
					tmp3.setPropertyValue("ELEMENT_NAME", node.getId());
					tmp3.setPropertyValue("DEVICE_PROPERTY", "BDES");
					tmp3.setPropertyValue("DEVICE_VALUE", Double.toString(((Electromagnet) node).getDesignField()));
					tmp3.setPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp3);
				} else {
					try {
						MachineModelDevice tmp4 = new MachineModelDevice();
						tmp4.setPropertyValue("ELEMENT_NAME", node.getId());
						tmp4.setPropertyValue("DEVICE_PROPERTY", "BACT");
						tmp4.setPropertyValue("DEVICE_VALUE", Double.toString(((Electromagnet) node).getFieldReadback()));
						tmp4.setPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
						runMachineModelDevice.add(tmp4);
						
						MachineModelDevice tmp5 = new MachineModelDevice();
						tmp5.setPropertyValue("ELEMENT_NAME", node.getId());
						tmp5.setPropertyValue("DEVICE_PROPERTY", "BDES");
						tmp5.setPropertyValue("DEVICE_VALUE", Double.toString(((Electromagnet) node).getFieldSetting()));
						tmp5.setPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
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
					tmp6.setPropertyValue("ELEMENT_NAME", node.getId());
					tmp6.setPropertyValue("DEVICE_PROPERTY", "P");
					tmp6.setPropertyValue("DEVICE_VALUE", scenario.propertiesForNode(node).get(
							RfCavityPropertyAccessor.PROPERTY_PHASE).toString());
					tmp6.setPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp6);
					
					MachineModelDevice tmp7 = new MachineModelDevice();
					tmp7.setPropertyValue("ELEMENT_NAME", node.getId());
					tmp7.setPropertyValue("DEVICE_PROPERTY", "A");
					tmp7.setPropertyValue("DEVICE_VALUE", scenario.propertiesForNode(node)
							.get(RfCavityPropertyAccessor.PROPERTY_AMPLITUDE).toString());
					tmp7.setPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp7);
				} catch (SynchronizationException e) {
					Message.error("Model Synchronization Exception: Cannot synchronize device data for model run.", true);
					e.printStackTrace();
				}
				if (Scenario.SYNC_MODE_DESIGN.equals("DESIGN")) {
					MachineModelDevice tmp8 = new MachineModelDevice();
					tmp8.setPropertyValue("ELEMENT_NAME", node.getId());
					tmp8.setPropertyValue("DEVICE_PROPERTY", "PDES");
					tmp8.setPropertyValue("DEVICE_VALUE", Double.toString(((RfCavity) node).getDfltAvgCavPhase()));
					tmp8.setPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
					runMachineModelDevice.add(tmp8);
					
					MachineModelDevice tmp9 = new MachineModelDevice();
					tmp9.setPropertyValue("ELEMENT_NAME", node.getId());
					tmp9.setPropertyValue("DEVICE_PROPERTY", "ADES");
					tmp9.setPropertyValue("DEVICE_VALUE", Double.toString(((RfCavity) node).getDfltCavAmp()));
					tmp9.setPropertyValue("ZPOS", df.format(useSDisplay ? node.getSDisplay() : seq.getPosition(node)));
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
	
	public static String newUploadToDatabase(final JFrame parent,
			BrowserModel model,
			MachineModel runMachineModel,
			final MachineModelDetail[] runMachineModelDetail,
			final MachineModelDevice[] runMachineModelDevice) {
		
		Integer runID = null;
				
		Connection writeConnection;
		
		try {
//			writeConnection = dialog.showConnectionDialog(DatabaseAdaptor.getInstance());
			writeConnection = DataManager.getConnection();
			
			ArrayList<String> elementName = new ArrayList<String>();
			ArrayList<Integer> elementID = new ArrayList<>();
			//ArrayList<Integer> elementTypeID = new ArrayList<>();
			int index;
			ResultSet rs;
	
			// prepare for MACHINE_MODEL.RUNS
			PreparedStatement stmt1 = null;
			
		
			writeConnection.setAutoCommit(false);
			
			stmt1 = writeConnection.prepareStatement("INSERT INTO \"MACHINE_MODEL\".\"RUNS\" (\"RUN_SOURCE_CHK\", \"COMMENTS\", \"MODEL_MODES_ID\")"
				+"	VALUES(?,?,?) RETURNING \"ID\"");
			stmt1.setString(1, (String)runMachineModel.getPropertyValue("RUN_SOURCE_CHK"));
			stmt1.setString(2, (String)runMachineModel.getPropertyValue("COMMENTS"));
			stmt1.setInt(3, Integer.parseInt((String)runMachineModel.getPropertyValue("MODEL_MODES_ID")));
			
			ResultSet rs1 = stmt1.executeQuery();
			rs1.next();
			Message.info("RUN_ID = " + rs1.getInt(1));
			
			// update the RUN ID
			runID = rs1.getInt(1);
			
			/*PreparedStatement stmt2 = writeConnection.prepareStatement("SELECT L.\"ELEMENT\", L.\"ELEMENT_ID\", D.\"ID\" " +
					"FROM \"MACHINE_MODEL\".\"DEVICE_TYPES\" D, \"LCLS_INFRASTRUCTURE\".\"LCLS_ELEMENTS\" L " +
			"WHERE L.\"KEYWORD\" = D.\"DEVICE_TYPE\"");*/
			// TODO OPENXAL
			/*PreparedStatement stmt2 = writeConnection.prepareStatement("SELECT D.\"DEVICE_TYPE\", D.\"ID\", D.\"ID\" " +
					"FROM \"MACHINE_MODEL\".\"DEVICE_TYPES\" D");
			rs = stmt2.executeQuery();
			while(rs.next()){
				elementName.add(rs.getString(1));
				elementID.add(rs.getInt(2));
				elementTypeID.add(rs.getInt(3));
			}*/

			PreparedStatement stmt3 = writeConnection.prepareStatement("INSERT INTO \"MACHINE_MODEL\".\"MODEL_DEVICES\" (\"RUNS_ID\", \"LCLS_ELEMENTS_ELEMENT_ID\", \"ELEMENT_NAME\", \"DEVICE_TYPES_ID\", \"DEVICE_PROPERTY\", \"DEVICE_VALUE\", \"ZPOS\") "+
			  "VALUES (?,?,?,?,?,?,?)");
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
				stmt3.setDouble(6, Double.parseDouble((String)runMachineModelDevice[i].getPropertyValue("DEVICE_VALUE")));
				stmt3.setDouble(7, Double.parseDouble((String)runMachineModelDevice[i].getPropertyValue("ZPOS")));			
				stmt3.addBatch();
			}
			stmt3.executeBatch();

			PreparedStatement stmt4 = writeConnection.prepareStatement("	INSERT INTO \"MACHINE_MODEL\".\"ELEMENT_MODELS\" ( " +
    " \"RUNS_ID\", \"LCLS_ELEMENTS_ELEMENT_ID\", \"ELEMENT_NAME\", \"INDEX_SLICE_CHK\", " +
    "\"ZPOS\", \"EK\", \"ALPHA_X\", \"ALPHA_Y\", \"BETA_X\", \"BETA_Y\" , \"PSI_X\"  , \"PSI_Y\", \"ETA_X\", \"ETA_Y\", \"ETAP_X\", \"ETAP_Y\","+
    "\"R11\", \"R12\", \"R13\", \"R14\", \"R15\", \"R16\", \"R21\", \"R22\", \"R23\", \"R24\", \"R25\", \"R26\" , \"R31\" , \"R32\" , \"R33\" , \"R34\" , \"R35\" , \"R36\" ,"+
    "\"R41\" , \"R42\" , \"R43\" , \"R44\" , \"R45\" , \"R46\" , \"R51\" , \"R52\" , \"R53\" , \"R54\" , \"R55\" , \"R56\" , \"R61\" , \"R62\" , \"R63\" , \"R64\" , \"R65\" , \"R66\" ,"+ 
    "\"LEFF\", \"SLEFF\" , \"ORDINAL\", \"SUML\") " + 
    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			for(int i=0; i<runMachineModelDetail.length; i++){
				index = elementName.indexOf(runMachineModelDetail[i].getPropertyValue("ELEMENT_NAME").toString());
				if(index >= 0){
					stmt4.setInt(2, elementID.get(index));
				}else{
					stmt4.setNull(2, Types.INTEGER);
				}
				stmt4.setInt(1, runID);
				
				stmt4.setString(3, (String)runMachineModelDetail[i].getPropertyValue("ELEMENT_NAME"));
				stmt4.setInt(4, Integer.parseInt((String)runMachineModelDetail[i].getPropertyValue("INDEX_SLICE_CHK")));
				stmt4.setDouble(5, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ZPOS")));
				stmt4.setDouble(6, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("E")));
				stmt4.setDouble(7, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ALPHA_X")));
				stmt4.setDouble(8, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ALPHA_Y")));
				stmt4.setDouble(9, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("BETA_X")));
				stmt4.setDouble(10, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("BETA_Y")));
				stmt4.setDouble(11, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("PSI_X")));
				stmt4.setDouble(12, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("PSI_Y")));
				stmt4.setDouble(13, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ETA_X")));
				stmt4.setDouble(14, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ETA_Y")));
				stmt4.setDouble(15, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ETAP_X")));
				stmt4.setDouble(16, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ETAP_Y")));
				stmt4.setDouble(17, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R11")));
				stmt4.setDouble(18, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R12")));
				stmt4.setDouble(19, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R13")));
				stmt4.setDouble(20, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R14")));
				stmt4.setDouble(21, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R15")));
				stmt4.setDouble(22, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R16")));
				stmt4.setDouble(23, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R21")));
				stmt4.setDouble(24, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R22")));
				stmt4.setDouble(25, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R23")));
				stmt4.setDouble(26, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R24")));
				stmt4.setDouble(27, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R25")));
				stmt4.setDouble(28, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R26")));
				stmt4.setDouble(29, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R31")));
				stmt4.setDouble(30, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R32")));
				stmt4.setDouble(31, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R33")));
				stmt4.setDouble(32, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R34")));
				stmt4.setDouble(33, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R35")));
				stmt4.setDouble(34, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R36")));
				stmt4.setDouble(35, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R41")));
				stmt4.setDouble(36, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R42")));
				stmt4.setDouble(37, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R43")));
				stmt4.setDouble(38, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R44")));
				stmt4.setDouble(39, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R45")));
				stmt4.setDouble(40, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R46")));
				stmt4.setDouble(41, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R51")));
				stmt4.setDouble(42, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R52")));
				stmt4.setDouble(43, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R53")));
				stmt4.setDouble(44, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R54")));
				stmt4.setDouble(45, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R55")));
				stmt4.setDouble(46, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R56")));
				stmt4.setDouble(47, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R61")));
				stmt4.setDouble(48, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R62")));
				stmt4.setDouble(49, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R63")));
				stmt4.setDouble(50, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R64")));
				stmt4.setDouble(51, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R65")));
				stmt4.setDouble(52, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("R66")));
				stmt4.setDouble(53, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("LEFF")));
				stmt4.setDouble(54, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("SLEFF")));
				stmt4.setDouble(55, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("ORDINAL")));
				stmt4.setDouble(56, Double.parseDouble((String)runMachineModelDetail[i].getPropertyValue("SUML")));

				stmt4.addBatch();
			}
		
			stmt4.executeBatch();
			


			writeConnection.commit();
			
			String msg = "Model data upload finished successfully";
			Message.info(msg, true);

			// automatically update the table after a model is uploaded
			try {
				model.removeRunModelFromFetchedModels(runID.toString());
			} catch (ParseException e) {
				Message.error("Cannot update MODEL RUN table!");
				e.printStackTrace();
			}

			
			writeConnection.close();
			
			return runID.toString();
		} catch (SQLException e) {
			Message.error("SQLException: failed to execute PL/SQL call on " + url, true);
			e.printStackTrace();
			
			return null;
		} catch (Exception exception) {
			JOptionPane.showMessageDialog(parent, exception.getMessage(),
					"SQL Error!  Query failed on" + url, JOptionPane.ERROR_MESSAGE);
			Logger.getLogger("global").log(Level.SEVERE,
					"Database SQL error.", exception);
			Message.error("SQLException: Query failed on " + url, true);
			
			return null;
		}
		/* catch (Exception exception) {
			JOptionPane.showMessageDialog(parent, exception.getMessage(),
					"Connection Error!  Cannot connect to " + url, JOptionPane.ERROR_MESSAGE);
			Logger.getLogger("global").log(Level.SEVERE,
					"Database connection error.  Cannot connect to " + url, exception);
			Message.error("Connection exception: Cannot connect to database" + url, true);
		}*/
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
			} catch (Exception exception) {
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


