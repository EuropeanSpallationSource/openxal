/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xal.lebt;

import java.util.ArrayList;
import java.util.Arrays;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.smf.Accelerator;
import xal.tools.beam.Twiss;
import xal.sim.scenario.Scenario;
import xal.model.probe.traj.Trajectory;
import xal.smf.data.XMLDataManager;
import xal.extension.jels.smf.impl.ESSSolFieldMap;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.VDipoleCorr;

/**
 * The class handling running the simulation.
 * 
 * @author sofiaolsson
 */
public class SimulationRunner {
    
    private double[] init;
    private double contineous_bc;
    private double[] solenoidCurrents;
    private double[] correctorCurrents;
    
    private ArrayList<Double>[] sigmaX;
    private ArrayList<Double>[] sigmaY;
    private ArrayList<Double> sigmaZ;
    private ArrayList<Double>[] sigmaR;
    private ArrayList<Double>[] sigmaOffsetR;
    private ArrayList<Double>[] sigmaOffsetX;
    private ArrayList<Double>[] sigmaOffsetY;
    private ArrayList<Double> posX;
    private ArrayList<Double> posY;
    private ArrayList<Double> posR;
    private ArrayList<Double> posPhi;
    private ArrayList<Double> positions;
    
    private Accelerator accelerator;
    private AcceleratorSeq sequence;
    private EnvTrackerAdapt envelopeTracker;
    private EnvelopeProbe probe;
    private PhaseVector initial_pos;
    private Scenario model;
    
    private boolean hasRun;
    
    public static final double C_BEAM_TO_OPENXAL_B_BEAM = 22.702702702702703;
    
    public static final double SOLENOID_CURRENT_TO_PEAK_RATE = 8.15308527131783e-04; 
    public static final double HSTEERER_CURRENT_TO_PEAK_RATE = 7.166666666666667e-05;
    public static final double VSTEERER_CURRENT_TO_PEAK_RATE = 8.583333333333334e-05;
    public static final double HSTEERER_PEAK_CENTER_RATIO = 0.085395431829;
    public static final double VSTEERER_PEAK_CENTER_RATIO = 0.0804959726196;
    public static final double HSTEERER_CENTER_EDGE_RATIO = 0.481748317408;
    public static final double VSTEERER_CENTER_EDGE_RATIO = 0.479235913534;
    public static final double HSTEERER_PEAK_BL_RATIO = 0.167654873982754;
    public static final double VSTEERER_PEAK_BL_RATIO = 0.157653311589114;
    
    public static final double SPACE_CHARGE = 0.05;
    
    public static final double[] TWISSX = {-3.302987,0.39685261,1.79481296201e-05};
    public static final double[] TWISSY = {-3.2846641,0.39203602,1.82782164192e-05};
    public static final double[] TWISSZ = {0.0,109.50523,5*8.66027784872e-06};
    
    public static final double[][] SURROUNDING = {{0,0.483,0.4831,0.733,0.7331,1.0959,1.0960,1.1109,1.1110,1.3569,1.3570,1.4519,1.4520,1.7764,1.7765,1.8342,1.8343,1.9819,1.9820,2.2317,2.2320,2.3796,2.3797,2.4800},{75,75,50,50,75,75,40,40,75,75,35,35,75,75,50,50,75,75,50,50,75,75,43.78,7.1741}};
    
    //-----------------------CONSTRUCTORS-------------------------------
    
    public SimulationRunner(){
        init = new double[4];
        solenoidCurrents = new double[2];
        correctorCurrents = new double[4];
        
        Arrays.fill(init,0);
        Arrays.fill(solenoidCurrents,288.23);
        Arrays.fill(correctorCurrents,0);
        contineous_bc = 74;
              
        hasRun = false;
    }
    
    //-----------------------PUBLIC FUNCTIONS-----------------------------------
    
    //                      Set/get functions
    public void setSolenoidCurrent1(double current){solenoidCurrents[0] = current;}
    public void setSolenoidCurrent2(double current){solenoidCurrents[1] = current;}
    public void setVsteerer1Current(double current){correctorCurrents[0] = current;}
    public void setHsteerer1Current(double current){correctorCurrents[1] = current;}
    public void setVsteerer2Current(double current){correctorCurrents[2] = current;}
    public void setHsteerer2Current(double current){correctorCurrents[3] = current;}
    public void setBeamCurrent(double current){contineous_bc = current;}
    public void setInitialBeamParameters(double x,double xp, double y, double yp){init[0] = x; init[1] = xp; init[2] = y; init[3] = yp;}
    
    public double getSolenoidCurrent1(){return solenoidCurrents[0];}
    public double getSolenoidCurrent2(){return solenoidCurrents[1];}
    public double getVsteerer1Current(){return correctorCurrents[0];}
    public double getHsteerer1Current(){return correctorCurrents[1];}
    public double getVsteerer2Current(){return correctorCurrents[2];}
    public double getHsteerer2Current(){return correctorCurrents[3];}
    public double getBeamCurrent(){return contineous_bc;}
    public double[] getInitialBeamParameters(){return init;}
    public ArrayList<Double>[] getSigmaX(){return sigmaX;}
    public ArrayList<Double>[] getSigmaY(){return sigmaY;}
    public ArrayList[] getSigmaR(){return sigmaR;}
    public ArrayList[] getSigmaOffsetR(){return sigmaOffsetR;}
    public ArrayList[] getSigmaOffsetX(){return sigmaOffsetX;}
    public ArrayList[] getSigmaOffsetY(){return sigmaOffsetY;}
    public ArrayList getSigmaZ(){return sigmaZ;}
    public ArrayList getPositions(){return positions;}
    public ArrayList getPosX(){return posX;}
    public ArrayList getPosY(){return posY;}
    public ArrayList getPosR(){return posR;}
    public ArrayList getPosPhi(){return posPhi;}
    public boolean hasRun(){return hasRun;}
    
    //-----------------------CONVERTION FUNCTIONS-------------------------------
    
    /**
     * Converts solenoid current to BField
     * @param current The solenoid current (A)
     * @return B The corresponding BField
     */
    private double solCurrentToBField(double current){
        return SOLENOID_CURRENT_TO_PEAK_RATE*current;
    }
    
    /**
     * Returns an array with the B_peak fields for the horizontal steerer.
     * @param current The current of the horizontal steerer
     * @param length The length of the steerer
     * @return Bfields for the horizontal steerer, where the first element is for the center and the second for the edges
     */
    private double[] hSteererCurrentToBFields(double current,double length){
        
        double[] Bfields = new double[2];
        double B_peak, B_center;
        
        B_peak = HSTEERER_CURRENT_TO_PEAK_RATE*current;
        Bfields[0] = B_peak*HSTEERER_PEAK_CENTER_RATIO/length; //center peak
        Bfields[1] = Bfields[0]*HSTEERER_CENTER_EDGE_RATIO; //edge peaks
        
        return Bfields;
    }
    
     /**
     * Returns an array with the B_peak fields for the vertical steerer.
     * @param current The current of the vertical steerer
     * @param length The length of the steerer
     * @return Bfields for the vertical steerer, where the first element is for the center and the second for the edges
     */
    private double[] vSteererCurrentToBFields(double current, double length){
        
        double[] Bfields = new double[2];
        double B_peak, B_center;
        
        B_peak = VSTEERER_CURRENT_TO_PEAK_RATE*current;
        Bfields[0] = B_peak*VSTEERER_PEAK_CENTER_RATIO/length; //center peak
        Bfields[1] = Bfields[0]*VSTEERER_CENTER_EDGE_RATIO; //edge peaks
        
        return Bfields;
    }
        
    private double beamCurrentToOpenXAL(double cont_current){
        return cont_current*C_BEAM_TO_OPENXAL_B_BEAM*SPACE_CHARGE;
    }
            
    //----------------------SIMULATION FUNCTIONS--------------------------------
   
    /**
     * Runs the simulation
     */
    public void runSimulation() throws ModelException, InstantiationException{
        initiateOpenXAL();
        
        setCorrectorFields();
        setSolenoidFields();
        setTrackerParameters();
        setProbe();
        setCovarianceMatrix();
        runModel();
        retrieveTrajectory();
        
        hasRun = true;
    }
    
    /**
     * Connects to openxal and initiates the LEBT sequence.
     */
    private void initiateOpenXAL(){
        accelerator = XMLDataManager.acceleratorWithPath("/Users/sofiaolsson/NetBeansProjects/openxal3/apps/openxal.apps.lebt/XMLdata/main.xal");
        
        //accelerator = XMLDataManager.loadDefaultAccelerator();
        sequence = accelerator.getSequence("LEBT");
    }
    
    /**
     * Sets the three lenses' field for all steerers according to given currents. Assuming equal length of each steerer element
     */
    private void setCorrectorFields(){
        
        double[] V1_Bpeak, H1_Bpeak, V2_Bpeak, H2_Bpeak;
        
        VDipoleCorr V11 = (VDipoleCorr) sequence.getNodeWithId("ST1-VC1");
        VDipoleCorr V12 = (VDipoleCorr) sequence.getNodeWithId("ST1-VC2");
        VDipoleCorr V13 = (VDipoleCorr) sequence.getNodeWithId("ST1-VC3");
        
        HDipoleCorr H11 = (HDipoleCorr) sequence.getNodeWithId("ST1-HC1");
        HDipoleCorr H12 = (HDipoleCorr) sequence.getNodeWithId("ST1-HC2");
        HDipoleCorr H13 = (HDipoleCorr) sequence.getNodeWithId("ST1-HC3");
        
        VDipoleCorr V21 = (VDipoleCorr) sequence.getNodeWithId("ST2-VC1");
        VDipoleCorr V22 = (VDipoleCorr) sequence.getNodeWithId("ST2-VC2");
        VDipoleCorr V23 = (VDipoleCorr) sequence.getNodeWithId("ST2-VC3");
        
        HDipoleCorr H21 = (HDipoleCorr) sequence.getNodeWithId("ST2-HC1");
        HDipoleCorr H22 = (HDipoleCorr) sequence.getNodeWithId("ST2-HC2");
        HDipoleCorr H23 = (HDipoleCorr) sequence.getNodeWithId("ST2-HC3");
        
        V1_Bpeak = vSteererCurrentToBFields(correctorCurrents[0],V12.getLength());
        H1_Bpeak = hSteererCurrentToBFields(correctorCurrents[1],H12.getLength());
        V2_Bpeak = vSteererCurrentToBFields(correctorCurrents[2],V22.getLength());
        H2_Bpeak = hSteererCurrentToBFields(correctorCurrents[3],H22.getLength());
                
        V11.setDfltField(V1_Bpeak[1]);
        V12.setDfltField(V1_Bpeak[0]);
        V13.setDfltField(V1_Bpeak[1]);
        
        H11.setDfltField(H1_Bpeak[1]);
        H12.setDfltField(H1_Bpeak[0]);
        H13.setDfltField(H1_Bpeak[1]);
        
        V21.setDfltField(V2_Bpeak[1]);
        V22.setDfltField(V2_Bpeak[0]);
        V23.setDfltField(V2_Bpeak[1]);
        
        H21.setDfltField(H2_Bpeak[1]);
        H22.setDfltField(H2_Bpeak[0]);
        H23.setDfltField(H2_Bpeak[1]);
    }
    
    /**
     * Sets the solenoid fields according to given solenoid current
     */
    private void setSolenoidFields(){
        
        double sol1_Bpeak, sol2_Bpeak;
        
        sol1_Bpeak = solCurrentToBField(solenoidCurrents[0]);
        sol2_Bpeak = solCurrentToBField(solenoidCurrents[1]);
          
        ESSSolFieldMap sol1 = (ESSSolFieldMap) sequence.getNodeWithId("FM1:SFM");
        ESSSolFieldMap sol2 = (ESSSolFieldMap) sequence.getNodeWithId("FM2:SFM");
        
        sol1.setDfltField(sol1_Bpeak);
        sol2.setDfltField(sol2_Bpeak);
    }
    
    /**
     * Sets tracker parameters
     */
    private void setTrackerParameters() throws InstantiationException{
       
        envelopeTracker = AlgorithmFactory.createEnvTrackerAdapt(sequence);
        
        envelopeTracker.setMaxIterations(20000);
        envelopeTracker.setAccuracyOrder(1);
        envelopeTracker.setErrorTolerance(0.001);
        envelopeTracker.setUseSpacecharge(true);
        envelopeTracker.setStepSize(0.01);
        envelopeTracker.setProbeUpdatePolicy(envelopeTracker.UPDATE_ALWAYS);
    }
    
    /**
     * Sets the probe parameters.
     */
    private void setProbe(){
        
        probe = ProbeFactory.getEnvelopeProbe(sequence, envelopeTracker);
        
        initial_pos = new PhaseVector(Double.toString(init[0]) + "," +
                Double.toString(init[1]) + "," +
                Double.toString(init[2]) + "," +
                Double.toString(init[3]) + "," +
                                 "0," +      //z
                                 "0");        //z'
                
        probe.setBeamCurrent(beamCurrentToOpenXAL(contineous_bc));
        probe.setPosition(0.);
    }
    
    /**
     * Sets the initial covariance matrix
     */
    private void setCovarianceMatrix(){
        
        Twiss twissX = new Twiss(TWISSX[0],TWISSX[1],TWISSX[2]);
        Twiss twissY = new Twiss(TWISSY[0],TWISSY[1],TWISSY[2]);
        Twiss twissZ = new Twiss(TWISSZ[0],TWISSZ[1],TWISSZ[2]);
        
        CovarianceMatrix cov = CovarianceMatrix.buildCovariance(twissX,twissY,twissZ,initial_pos);
        probe.setCovariance(cov);
    }
    
    /**
     * Initiates model (scenario) and runs the simulation
     * @throws ModelException 
     */
    private void runModel() throws ModelException{
        model = Scenario.newScenarioFor(sequence);
        model.setProbe(probe);
        model.setSynchronizationMode("DESIGN");
        model.resync();
        model.run();
    }
    
    /**
     * Retrieves the trajectory from model and adds position and envelope to arraylists. 
     * Both Cartesian and cylindrical coordinates are calculated
     * The envelopes are creates as an array of arraylists, with positive and negative values as the different elements. 
     * SigmaR is calculated to be the max value of sigma X and Y.
     * Unit is mm and rad*pi.
     */
    private void retrieveTrajectory(){
        probe = (EnvelopeProbe) model.getProbe();
        
        Trajectory trajectory = probe.getTrajectory();
        
        ArrayList<EnvelopeProbeState> stateElement = (ArrayList<EnvelopeProbeState>) trajectory.getStatesViaIndexer();
        CovarianceMatrix covmat;
        
        //initialize
        sigmaX = new ArrayList[2];
        sigmaY = new ArrayList[2];
        sigmaZ = new ArrayList<Double>();
        sigmaR = new ArrayList[2];
        sigmaOffsetR = new ArrayList[2];
        sigmaOffsetX = new ArrayList[2];
        sigmaOffsetY = new ArrayList[2];
        posX = new ArrayList<Double>();
        posY = new ArrayList<Double>();
        posR = new ArrayList<Double>();
        posPhi = new ArrayList<Double>();
        positions = new ArrayList<Double>();
        
        for(int i = 0; i < sigmaR.length;i++){
            sigmaR[i] = new ArrayList<Double>();
            sigmaX[i] = new ArrayList<Double>();
            sigmaY[i] = new ArrayList<Double>();
            sigmaOffsetR[i] = new ArrayList<Double>();
            sigmaOffsetX[i] = new ArrayList<Double>();
            sigmaOffsetY[i] = new ArrayList<Double>();
        }
        
        //retrieve trajectory
        for(int i=0; i<trajectory.numStates(); i++){
            positions.add(stateElement.get(i).getPosition());
            covmat = stateElement.get(i).getCovarianceMatrix();

            posX.add(covmat.getMeanX() * 1.0e+3);
            posY.add(covmat.getMeanY() * 1.0e+3);
            sigmaX[0].add(covmat.getSigmaX() * 1.0e+3);
            sigmaY[0].add(covmat.getSigmaY() * 1.0e+3);
            sigmaZ.add(covmat.getSigmaZ());
            
            //Calculating cylindrical coordinates
            posR.add(Math.sqrt(Math.pow(posX.get(i),2)+Math.pow(posY.get(i),2)));
            
            if ((posX.get(i) == 0.0) && (posY.get(i) == 0.0)){
                posPhi.add(0.0);}
            else if (posX.get(i) == 0.0){
                double phi = Math.asin(posY.get(i)/posR.get(i));
                posPhi.add(phi/Math.PI);}
            else if (posX.get(i) > 0.0){
                double phi = Math.atan(posY.get(i)/posX.get(i));
                posPhi.add(phi/Math.PI);}
            else if (posY.get(i) >= 0){
                double phi = Math.atan(posY.get(i)/posX.get(i))+Math.PI;
                posPhi.add(phi/Math.PI);}
            else{
                double phi = Math.atan(posY.get(i)/posX.get(i))-Math.PI;
                posPhi.add(phi/Math.PI);}
            
            sigmaR[0].add(Math.max(sigmaX[0].get(i),sigmaY[0].get(i)));
            
            sigmaX[1].add(-sigmaX[0].get(i));
            sigmaY[1].add(-sigmaY[0].get(i));
            sigmaR[1].add(-sigmaR[0].get(i));
            
            
            for(int k = 0; k < sigmaOffsetX.length; k++){
                sigmaOffsetX[k].add(sigmaX[k].get(i)+posX.get(i));
                sigmaOffsetY[k].add(sigmaY[k].get(i)+posY.get(i));
                sigmaOffsetR[k].add(sigmaR[k].get(i)+posR.get(i));
            }
            
        }
    }
}

