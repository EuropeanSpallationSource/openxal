/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import xal.sim.sync.SynchronizationException;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.tools.math.Complex;

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
    private boolean electrode;
    
    private ArrayList<Double>[] sigmaX;
    private ArrayList<Double>[] sigmaY;
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
    private Object sequence;
    private EnvTrackerAdapt envelopeTracker;
    private EnvelopeProbe probe;
    private PhaseVector initial_pos;
    private Scenario model;
    private String model_sync;
    
    private boolean hasRun;
    
    public static final double C_BEAM_TO_OPENXAL_B_BEAM = 22.702702702702703;        
    
    private double SPACE_CHARGE = 0.95;
    private double SPACE_CHARGE_ELECTRODE = 0.0;   
    
    private double[] TWISSX = {-3.302987,0.39685261,0.1223e-06};
    private final double[] TWISSY = {-3.2846641,0.39203602,0.1217e-06};
    private static final double[] TWISSZ = {0.0,109.50523,8.66027784872e-06};
        
    //-----------------------CONSTRUCTORS-------------------------------
    
    public SimulationRunner(Accelerator accl, AcceleratorSeq seq, String modelSync){
        
        accelerator = accl; 
        init = new double[4];
        solenoidFields = new double[2];
        correctorFields = new double[4];
        electrode = false;  
        sequence = seq;
        model_sync = modelSync;
        
        try {
            //get inital parameters from file
            envelopeTracker = AlgorithmFactory.createEnvTrackerAdapt((AcceleratorSeq) sequence);
            probe = ProbeFactory.getEnvelopeProbe(accelerator.getSequence("LEBT"), envelopeTracker);
        } catch (InstantiationException ex) {
            Logger.getLogger(SimulationRunner.class.getName()).log(Level.SEVERE, null, ex);
        }                   
        
        if (probe != null){            
            
            continuos_bc = probe.getBeamCurrent()*1000;
            beta_gamma = probe.getGamma()*probe.getBeta();
           
            Twiss[] iniTwiss = probe.getCovariance().computeTwiss();
            TWISSX[0] = iniTwiss[0].getAlpha();
            TWISSX[1] = iniTwiss[0].getBeta();
            TWISSX[2] = iniTwiss[0].getEmittance()*beta_gamma;
            
            TWISSY[0] = iniTwiss[1].getAlpha();
            TWISSY[1] = iniTwiss[1].getBeta();
            TWISSY[2] = iniTwiss[1].getEmittance()*beta_gamma;
            
            PhaseVector iniPos = probe.getCovariance().getMean();
            
            init[0] = iniPos.getx();
            init[1] = iniPos.getxp();
            init[2] = iniPos.gety();
            init[3] = iniPos.getyp();
            
            Arrays.fill(solenoidFields,288.23);
            Arrays.fill(correctorFields,0);
            
        } else {       
           
            Arrays.fill(init,0);
            Arrays.fill(solenoidFields,288.23);
            Arrays.fill(correctorFields,0);
            continuos_bc = 74;                    

            beta_gamma = 0.0126439470187;
        }
              
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
    public void setSpaceChargeCompensation(double sc_comp, double sc_compelectrode){SPACE_CHARGE = sc_comp;SPACE_CHARGE_ELECTRODE = sc_compelectrode;}
    public void setAccelerator(Accelerator accl){accelerator = accl;}
    public void setElectrode(boolean val){electrode = val;}
    public void sethasRun(boolean val){hasRun = val;}
    public void setModelSync(String model){model_sync = model;}

    
    public double getSolenoid1Field(){return solenoidFields[0];}
    public double getSolenoid2Field(){return solenoidFields[1];}
    public double getVsteerer1Field(){return correctorFields[0];}
    public double getHsteerer1Field(){return correctorFields[1];}
    public double getVsteerer2Field(){return correctorFields[2];}
    public double getHsteerer2Field(){return correctorFields[3];}
    public double getBeamCurrent(){return continuos_bc;}
    public boolean getElectrode(boolean val){return electrode;}
    public double getSpaceChargeCompensation(){return SPACE_CHARGE;}
    public double getSpaceChargeCompensationElectrode(){return SPACE_CHARGE_ELECTRODE;}
    public double[] getTwissX(){return TWISSX;}
    public double[] getTwissY(){return TWISSY;}
    public double[] getInitialBeamParameters(){return init;}    
    public ArrayList<Double>[] getSigmaX(){return sigmaX;}
    public ArrayList<Double>[] getSigmaY(){return sigmaY;}
    public ArrayList[] getSigmaR(){return sigmaR;}
    public ArrayList[] getSigmaOffsetR(){return sigmaOffsetR;}
    public ArrayList[] getSigmaOffsetX(){return sigmaOffsetX;}
    public ArrayList[] getSigmaOffsetY(){return sigmaOffsetY;}
    public ArrayList getPositions(){return positions;}
    public ArrayList getPosX(){return posX;}
    public ArrayList getPosY(){return posY;}
    public ArrayList getPosR(){return posR;}
    public ArrayList getPosPhi(){return posPhi;}
    public boolean hasRun(){return hasRun;}
    
    //-----------------------CONVERTION FUNCTIONS-------------------------------
        
    private double beamCurrentToOpenXAL(double cont_current){
        return cont_current*C_BEAM_TO_OPENXAL_B_BEAM*(1-SPACE_CHARGE)*1e-03;
    }
            
    //----------------------SIMULATION FUNCTIONS--------------------------------
   
    /**
     * Runs the simulation
     */
    public void runSimulation() throws ModelException, InstantiationException{        
        
        //initialize
        sigmaX = new ArrayList[2];
        sigmaY = new ArrayList[2];
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
               
        if(sequence instanceof AcceleratorSeqCombo){            
            AcceleratorSeqCombo seq = (AcceleratorSeqCombo) sequence;
            setTrackerParameters(seq); 
            if(electrode && seq.getAllNodes().toString().contains("ELECTRODE")){
                AcceleratorNode electrode = seq.getNodeWithId("ELECTRODE");
                setProbe(seq,0.0);
                setIniCovarianceMatrix();
                runModel(model_sync,seq,seq.getAllNodes().get(0).toString(),"ELECTRODE");
                retrieveTrajectory();
                SPACE_CHARGE = SPACE_CHARGE_ELECTRODE;
                CovarianceMatrix cov = getElectrodeCovarianceMatrix();
                setProbe(seq,electrode.getPosition());
                setCovarianceMatrix(cov);
                runModel(model_sync,seq,"ELECTRODE",seq.getAllNodes().get(seq.getNodeCount()-1).toString());
                retrieveTrajectory();
            } else {
                setProbe(seq,0.0);
                setIniCovarianceMatrix();
                runModel(model_sync,seq,seq.getAllNodes().get(0).toString(),seq.getAllNodes().get(seq.getNodeCount()-1).toString());
                retrieveTrajectory();
            }
            hasRun = true;
        } else {
            AcceleratorSeq seq = (AcceleratorSeq) sequence;            
            setTrackerParameters(seq);
            if(electrode && seq.getAllNodes().toString().contains("ELECTRODE")){
                AcceleratorNode electrode = seq.getNodeWithId("ELECTRODE");
                setProbe(seq,0.0);
                setIniCovarianceMatrix();
                runModel(model_sync,seq,seq.getAllNodes().get(0).toString(),"ELECTRODE");
                retrieveTrajectory();
                SPACE_CHARGE = SPACE_CHARGE_ELECTRODE;
                CovarianceMatrix cov = getElectrodeCovarianceMatrix();
                setProbe(seq,electrode.getPosition());
                setCovarianceMatrix(cov);
                runModel(model_sync,seq,"ELECTRODE",seq.getAllNodes().get(seq.getNodeCount()-1).toString());
                retrieveTrajectory();
            } else {
                setProbe(seq,0.0);
                setIniCovarianceMatrix();
                runModel(model_sync,seq,seq.getAllNodes().get(0).toString(),seq.getAllNodes().get(seq.getNodeCount()-1).toString());
                retrieveTrajectory();
            }
            hasRun = true;            
        }                                    
                
    }            
    
     /**
     * Sets tracker parameters
     */
    private void setTrackerParameters(AcceleratorSeq sequence) throws InstantiationException{
       
        envelopeTracker = AlgorithmFactory.createEnvTrackerAdapt(sequence);
        
        //envelopeTracker.setMaxIterations(20000);
        //envelopeTracker.setAccuracyOrder(2);
        //envelopeTracker.setErrorTolerance(0.001);
        envelopeTracker.setUseSpacecharge(true);
        //envelopeTracker.setStepSize(0.01);
        //envelopeTracker.setProbeUpdatePolicy(envelopeTracker.UPDATE_ALWAYS);
    }
    
     /**
     * Sets tracker parameters
     */
    private void setTrackerParameters(AcceleratorSeqCombo sequence) throws InstantiationException{
       
        envelopeTracker = AlgorithmFactory.createEnvTrackerAdapt(sequence);
        
        //envelopeTracker.setMaxIterations(20000);
        //envelopeTracker.setAccuracyOrder(2);
        //envelopeTracker.setErrorTolerance(0.001);
        envelopeTracker.setUseSpacecharge(true);
        //envelopeTracker.setStepSize(0.01);
        //envelopeTracker.setProbeUpdatePolicy(envelopeTracker.UPDATE_ALWAYS);
    }
    
    /**
     * Sets the probe parameters.
     */
    private void setProbe(AcceleratorSeq sequence, double ini_pos){
        
        probe = new EnvelopeProbe();
        
        probe = ProbeFactory.getEnvelopeProbe(sequence, envelopeTracker);
              
        probe.setBeamCurrent(beamCurrentToOpenXAL(continuos_bc));
        probe.setPosition(ini_pos);
    }
    
    private void setProbe(AcceleratorSeqCombo sequence, double ini_pos){
        
        probe = new EnvelopeProbe();
        
        probe = ProbeFactory.getEnvelopeProbe(sequence, envelopeTracker);
               
        probe.setBeamCurrent(beamCurrentToOpenXAL(continuos_bc));
        probe.setPosition(ini_pos);
    }
    
    /**
     * Sets the initial covariance matrix
     */
    private void setIniCovarianceMatrix(){
        
        Twiss twissX = new Twiss(TWISSX[0],TWISSX[1],TWISSX[2]/beta_gamma);
        Twiss twissY = new Twiss(TWISSY[0],TWISSY[1],TWISSY[2]/beta_gamma);
        Twiss twissZ = new Twiss(TWISSZ[0],TWISSZ[1],TWISSZ[2]);
        
        initial_pos = new PhaseVector(Double.toString(init[0]) + "," +
               Double.toString(init[1]) + "," +
               Double.toString(init[2]) + "," +
               Double.toString(init[3]) + "," +
                                "0," +      //z
                                "0");       //z'

        CovarianceMatrix cov = CovarianceMatrix.buildCovariance(twissX,twissY,twissZ,initial_pos);
        probe.setCovariance(cov);
    }
    
    private void setCovarianceMatrix(CovarianceMatrix cov){                
        probe.setCovariance(cov);
    }
    
    
    /**
     * Sets the initial covariance matrix
     */
    private CovarianceMatrix getElectrodeCovarianceMatrix(){
        
        EnvelopeProbe probeResult = (EnvelopeProbe) model.getProbe();
        
        Trajectory trajectory = probeResult.getTrajectory();
        
        ArrayList<EnvelopeProbeState> stateElement = (ArrayList<EnvelopeProbeState>) trajectory.getStatesViaIndexer();        
        CovarianceMatrix covmat;
        int[] index = trajectory.indicesForElement("ELECTRODE");
        
        covmat = stateElement.get(index[0]).getCovarianceMatrix();       
                
        return covmat;
    }
    
    /**
     * Initiates model (scenario) and runs the simulation
     * @throws ModelException 
     */
    private void runModel(String modeltype, AcceleratorSeq sequence, String startNode, String endNode) throws ModelException{
        model = Scenario.newScenarioFor(sequence);
        model.setProbe(probe);
        model.setStartNode(startNode);
        model.setStopNode(endNode);
        model.setSynchronizationMode(modeltype);
        try {
            model.resync();
        } catch (SynchronizationException ex){
            model.setSynchronizationMode("DESIGN");   
            model.resync();
        }
        model.run();
    }
    
    private void runModel(String modeltype, AcceleratorSeqCombo sequence, String startNode, String endNode) throws ModelException{
        model = Scenario.newScenarioFor(sequence);
        model.setProbe(probe);
        model.setStartNode(startNode);
        model.setStopNode(endNode);
        model.setSynchronizationMode(modeltype);
        try {
            model.resync();
        } catch (SynchronizationException ex){
            model.setSynchronizationMode("DESIGN");   
            model.resync();
        }
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
        EnvelopeProbe probeResult = (EnvelopeProbe) model.getProbe();
        
        Trajectory trajectory = probeResult.getTrajectory();
        
        ArrayList<EnvelopeProbeState> stateElement = (ArrayList<EnvelopeProbeState>) trajectory.getStatesViaIndexer();
        CovarianceMatrix covmat;  
        //double phase0;
        
        //if(posPhi.size()>1){
        //    phase0 = posPhi.get(posPhi.size()-1);
        //} else {
        //    phase0 = 0.0;
        //}
        
        //retrieve trajectory
        for(int i=0; i<trajectory.numStates(); i++){
            positions.add(stateElement.get(i).getPosition());
            covmat = stateElement.get(i).getCovarianceMatrix();

            posX.add(covmat.getMeanX() * 1.0e+3);
            posY.add(covmat.getMeanY() * 1.0e+3);
            sigmaX[0].add(covmat.getSigmaX() * 1.0e+3);
            sigmaY[0].add(covmat.getSigmaY() * 1.0e+3);
            sigmaX[1].add(covmat.getSigmaX() * -1.0e+3);
            sigmaY[1].add(covmat.getSigmaY() * -1.0e+3);
            sigmaR[0].add(Math.max(covmat.getSigmaX(),covmat.getSigmaY())* 1.0e+3);
            sigmaR[1].add(Math.max(covmat.getSigmaX(),covmat.getSigmaY())* -1.0e+3);                                    
            
            //Calculating cylindrical coordinates
            posR.add(Math.sqrt(Math.pow(covmat.getMeanX()*1.0e+3,2)+Math.pow(covmat.getMeanY()*1.0e+3,2)));
            Complex phi = new Complex(covmat.getMeanX()*1.0e+3,covmat.getMeanY()*1.0e+3);
            posPhi.add(phi.phase()/Math.PI);                                                                
                                   
            for(int k = 0; k < sigmaOffsetX.length; k++){
                sigmaOffsetX[k].add(sigmaX[k].get(i)+posX.get(i));
                sigmaOffsetY[k].add(sigmaY[k].get(i)+posY.get(i));
                sigmaOffsetR[k].add(sigmaR[k].get(i)+posR.get(i));
            }
            
        }
    }
}

