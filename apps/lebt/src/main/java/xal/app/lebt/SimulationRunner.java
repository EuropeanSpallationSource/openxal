/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import java.util.ArrayList;
import java.util.Arrays;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.smf.Accelerator;
import xal.tools.beam.Twiss;
import xal.sim.scenario.Scenario;
import xal.model.probe.traj.Trajectory;
import xal.extension.jels.smf.impl.ESSSolFieldMap;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.VDipoleCorr;

/**
 * The class handling running the simulation.
 * 
 * @author sofiaolsson
 */
public class SimulationRunner {
    
    private final double[] init;
    private double continuos_bc;
    private final double[] solenoidFields;
    private final double[] correctorFields;
    private final double beta_gamma;
    
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
    private final AcceleratorSeqCombo sequence;
    private EnvTrackerAdapt envelopeTracker;
    private EnvelopeProbe probe;
    private PhaseVector initial_pos;
    private Scenario model;
    
    private boolean hasRun;
    
    public static final double C_BEAM_TO_OPENXAL_B_BEAM = 22.702702702702703;        
    
    private double SPACE_CHARGE = 0.05;
    
    private final double[] TWISSX = {-3.302987,0.39685261,0.1223e-06};
    private final double[] TWISSY = {-3.2846641,0.39203602,0.1217e-06};
    private static final double[] TWISSZ = {0.0,109.50523,5*8.66027784872e-06};
        
    //-----------------------CONSTRUCTORS-------------------------------
    
    public SimulationRunner(Accelerator accl){
        init = new double[4];
        solenoidFields = new double[2];
        correctorFields = new double[4];
        
        Arrays.fill(init,0);
        Arrays.fill(solenoidFields,288.23);
        Arrays.fill(correctorFields,0);
        continuos_bc = 74;
        
        accelerator = accl;
        sequence = accelerator.getComboSequence("COMISSIONING");
        
        beta_gamma = 0.0126439470187;
              
        hasRun = false;
    }
    
    //-----------------------PUBLIC FUNCTIONS-----------------------------------
    
    //                      Set/get functions
    public void setSolenoid1Field(double field){solenoidFields[0] = field;}
    public void setSolenoid2Field(double field){solenoidFields[1] = field;}
    public void setVsteerer1Field(double field){correctorFields[0] = field;}
    public void setHsteerer1Field(double field){correctorFields[1] = field;}
    public void setVsteerer2Field(double field){correctorFields[2] = field;}
    public void setHsteerer2Field(double field){correctorFields[3] = field;}
    public void setBeamCurrent(double current){continuos_bc = current;}
    public void setBeamTwissX(double alpha, double beta, double emitt){TWISSX[0] = alpha;TWISSX[1] = beta;TWISSX[2] = emitt;}
    public void setBeamTwissY(double alpha, double beta, double emitt){TWISSY[0] = alpha;TWISSY[1] = beta;TWISSY[2] = emitt;}
    public void setInitialBeamParameters(double x,double xp, double y, double yp){init[0] = x; init[1] = xp; init[2] = y; init[3] = yp;}
    public void setSpaceChargeCompensation(double sc_comp){SPACE_CHARGE = sc_comp;}
    public void setAccelerator(Accelerator accl){accelerator = accl;}
    
    public double getSolenoid1Field(){return solenoidFields[0];}
    public double getSolenoid2Field(){return solenoidFields[1];}
    public double getVsteerer1Field(){return correctorFields[0];}
    public double getHsteerer1Field(){return correctorFields[1];}
    public double getVsteerer2Field(){return correctorFields[2];}
    public double getHsteerer2Field(){return correctorFields[3];}
    public double getBeamCurrent(){return continuos_bc;}
    public double getSpaceChargeCompensation(){return SPACE_CHARGE;}
    public double[] getTwissX(){return TWISSX;}
    public double[] getTwissY(){return TWISSY;}
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
        
    private double beamCurrentToOpenXAL(double cont_current){
        return cont_current*C_BEAM_TO_OPENXAL_B_BEAM*SPACE_CHARGE;
    }
            
    //----------------------SIMULATION FUNCTIONS--------------------------------
   
    /**
     * Runs the simulation
     */
    public void runSimulation(String model) throws ModelException, InstantiationException{
        
        //setCorrectorFields();
        //setSolenoidFields();
        setTrackerParameters();
        setProbe();
        setCovarianceMatrix();
        runModel(model);
        retrieveTrajectory();
        
        hasRun = true;
    }       
    
    /**
     * Sets the three lenses' field for all steerers according to given currents. Assuming equal length of each steerer element
     */
    private void setCorrectorFields(){
                
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
               
        V11.setDfltField(correctorFields[0]);
        V12.setDfltField(correctorFields[0]);
        V13.setDfltField(correctorFields[0]);
        
        H11.setDfltField(correctorFields[1]);
        H12.setDfltField(correctorFields[1]);
        H13.setDfltField(correctorFields[1]);
        
        V21.setDfltField(correctorFields[2]);
        V22.setDfltField(correctorFields[2]);
        V23.setDfltField(correctorFields[2]);
        
        H21.setDfltField(correctorFields[3]);
        H22.setDfltField(correctorFields[3]);
        H23.setDfltField(correctorFields[3]);
    }
    
    /**
     * Sets the solenoid fields according to given solenoid field
     */
    private void setSolenoidFields(){
                          
        ESSSolFieldMap sol1 = (ESSSolFieldMap) sequence.getNodeWithId("FM1:SFM");
        ESSSolFieldMap sol2 = (ESSSolFieldMap) sequence.getNodeWithId("FM2:SFM");
        
        sol1.setDfltField(solenoidFields[0]);
        sol2.setDfltField(solenoidFields[1]);
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
                
        probe.setBeamCurrent(beamCurrentToOpenXAL(continuos_bc));
        probe.setPosition(0.);
    }
    
    /**
     * Sets the initial covariance matrix
     */
    private void setCovarianceMatrix(){
        
        Twiss twissX = new Twiss(TWISSX[0],TWISSX[1],TWISSX[2]/beta_gamma);
        Twiss twissY = new Twiss(TWISSY[0],TWISSY[1],TWISSY[2]/beta_gamma);
        Twiss twissZ = new Twiss(TWISSZ[0],TWISSZ[1],TWISSZ[2]);
        
        CovarianceMatrix cov = CovarianceMatrix.buildCovariance(twissX,twissY,twissZ,initial_pos);
        probe.setCovariance(cov);
    }
    
    /**
     * Initiates model (scenario) and runs the simulation
     * @throws ModelException 
     */
    private void runModel(String modeltype) throws ModelException{
        model = Scenario.newScenarioFor(sequence);
        model.setProbe(probe);
        model.setSynchronizationMode(modeltype);
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

