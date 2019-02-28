/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.extension.jels.smf.impl.ESSMagFieldMap3D;
import xal.extension.jels.smf.impl.ESSSolFieldMap;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.sim.scenario.Scenario;
import xal.model.probe.traj.Trajectory;
import xal.sim.sync.SynchronizationException;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.Solenoid;
import xal.smf.impl.VDipoleCorr;
import xal.tools.math.Complex;

/**
 * The class handling running the simulation.
 *
 * @author sofiaolsson
 */
public class SimulationRunner {

    private final double[] init;
    private double continuos_bc;
    private double transmission;
    private double transmission_offset;
    private final double[] solenoidFields;
    private final double[] correctorVFields;
    private final double[] correctorHFields;
    private HashMap<Double,Double> vacuumChamber;
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

    private Object sequence;
    private EnvelopeTracker envelopeTracker;
    private EnvelopeProbe probe;
    private PhaseVector initial_pos;
    private Scenario model;
    private String model_sync;
    private String ini_pos_simul;
    private String final_pos_simul;

    private boolean hasRun;

    public static final double C_BEAM_TO_OPENXAL_B_BEAM = 35;

    private double SPACE_CHARGE = 0.95;
    private double SPACE_CHARGE_ELECTRODE = 0.0;

    private double[] TWISSX = {-3.302987,0.39685261,0.1223e-06};
    private final double[] TWISSY = {-3.2846641,0.39203602,0.1217e-06};
    private static final double[] TWISSZ = {0.0,110,10e-05};

    //-----------------------CONSTRUCTORS-------------------------------

    public SimulationRunner(Object seq, String modelSync){

        init = new double[4];
        solenoidFields = new double[2];
        correctorVFields = new double[2];
        correctorHFields = new double[2];
        vacuumChamber = new HashMap<>();
        electrode = false;
        sequence = seq;
        model_sync = modelSync;
        transmission = 1;
        transmission_offset = 1;
        final_pos_simul = "";
        readVacuumChamber(vacuumChamber);        

        try {
            //get inital parameters from file
            envelopeTracker = AlgorithmFactory.createEnvelopeTracker((AcceleratorSeq) sequence);
            probe = ProbeFactory.getEnvelopeProbe((AcceleratorSeq) sequence, envelopeTracker);
            probe.setCurrentElement(((AcceleratorSeq) sequence).getNodeAt(0).toString());
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
            Arrays.fill(correctorVFields,0);
            Arrays.fill(correctorHFields,0);

        } else {

            Arrays.fill(init,0);
            Arrays.fill(solenoidFields,288.23);
            Arrays.fill(correctorVFields,0);
            Arrays.fill(correctorHFields,0);
            continuos_bc = 74;

            beta_gamma = 0.0126439470187;
        }

        hasRun = false;
    }

    //-----------------------PUBLIC FUNCTIONS-----------------------------------

    //                      Set/get functions
    public void setSolenoid1Field(double field){solenoidFields[0] = field;}
    public void setSolenoid2Field(double field){solenoidFields[1] = field;}
    public void setVsteerer1Field(double field){correctorVFields[0] = field;}
    public void setHsteerer1Field(double field){correctorHFields[0] = field;}
    public void setVsteerer2Field(double field){correctorVFields[1] = field;}
    public void setHsteerer2Field(double field){correctorHFields[1] = field;}
    public void setBeamCurrent(double current){continuos_bc = current;}
    public void setBeamTwissX(double alpha, double beta, double emitt){TWISSX[0] = alpha;TWISSX[1] = beta;TWISSX[2] = emitt;}
    public void setBeamTwissY(double alpha, double beta, double emitt){TWISSY[0] = alpha;TWISSY[1] = beta;TWISSY[2] = emitt;}
    public void setInitialBeamParameters(double x,double xp, double y, double yp){init[0] = x; init[1] = xp; init[2] = y; init[3] = yp;}
    public void setSpaceChargeCompensation(double sc_comp, double sc_compelectrode){SPACE_CHARGE = sc_comp;SPACE_CHARGE_ELECTRODE = sc_compelectrode;}
    public void setElectrode(boolean val){electrode = val;}
    public void sethasRun(boolean val){hasRun = val;}
    public void setModelSync(String model){model_sync = model;}
    public void setInitSimulPos(String nodeName){ini_pos_simul = nodeName;}
    public void setFinalSimulPos(String nodeName){final_pos_simul = nodeName;}


    public double getSolenoid1Field(){return solenoidFields[0];}
    public double getSolenoid2Field(){return solenoidFields[1];}
    public double getVsteerer1Field(){return correctorVFields[0];}
    public double getHsteerer1Field(){return correctorHFields[0];}
    public double getVsteerer2Field(){return correctorVFields[1];}
    public double getHsteerer2Field(){return correctorHFields[1];}
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
    public double getTransmission(boolean hasOffset){ if(hasOffset){
                                                        return transmission_offset;
                                                    }else{ 
                                                        return transmission;
                                                    }}
    public EnvelopeProbe getProbe(){return probe;}
    public EnvelopeProbe getFinalProbe(){return (EnvelopeProbe) model.getProbe();}
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
        //Initialize transmission
        transmission_offset = 1.0;
        transmission = 1.0;

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
            AcceleratorNode start_simul = seq.getNodeWithId(ini_pos_simul);
            AcceleratorNode start_seq = seq.getAllNodes().get(0);
            AcceleratorNode end_simul = seq.getAllNodes().get(seq.getNodeCount()-1);
            if(model_sync.matches("DESIGN")){setMagnetFields(seq);}
            if(final_pos_simul!=""){
                end_simul =  seq.getNodeWithId(final_pos_simul);
            }
            AcceleratorNode electrode_node = null;
            if (seq.getAllNodes().toString().contains("LEBT-010:BMD-Rep-01")){
                electrode_node = seq.getNodeWithId("LEBT-010:BMD-Rep-01");
                if (electrode_node.getSDisplay()<start_simul.getSDisplay()){
                    electrode = false;
                }
                if (electrode_node.getSDisplay()>end_simul.getSDisplay()){
                    electrode = false;
                }
            } else {
                electrode = false;
            }
            setTrackerParameters(seq);
            if(electrode){
                setProbe(seq,start_simul.getSDisplay());
                setIniCovarianceMatrix();
                runModel(model_sync,seq,ini_pos_simul,electrode_node.toString());
                retrieveTrajectory();
                SPACE_CHARGE = SPACE_CHARGE_ELECTRODE;
                CovarianceMatrix cov = getElectrodeCovarianceMatrix();
                setProbe(seq,electrode_node.getSDisplay());
                setCovarianceMatrix(cov);
                runModel(model_sync,seq,electrode_node.toString(),end_simul.toString());
                retrieveTrajectory();
            } else {
                setProbe(seq,start_simul.getSDisplay());
                setIniCovarianceMatrix();
                runModel(model_sync,seq,ini_pos_simul,end_simul.toString());
                retrieveTrajectory();
            }
            hasRun = true;
        } else {
            AcceleratorSeq seq = (AcceleratorSeq) sequence;
            AcceleratorNode start_simul = seq.getNodeWithId(ini_pos_simul);
            AcceleratorNode start_seq = seq.getAllNodes().get(0);
            AcceleratorNode end_simul = seq.getAllNodes().get(seq.getNodeCount()-1);
            if(model_sync.matches("DESIGN")){setMagnetFields(seq);}
            if(final_pos_simul!=""){
                end_simul =  seq.getNodeWithId(final_pos_simul);
            }
            AcceleratorNode electrode_node = null;
            if (seq.getAllNodes().toString().contains("LEBT-010:BMD-Rep-01")){
                electrode_node = seq.getNodeWithId("LEBT-010:BMD-Rep-01");
                if (electrode_node.getSDisplay()<start_simul.getSDisplay()){
                    electrode = false;
                }
                if (electrode_node.getSDisplay()>end_simul.getSDisplay()){
                    electrode = false;
                }
            } else {
                electrode = false;
            }
            setTrackerParameters(seq);
            if(electrode){
                setProbe(seq,start_simul.getSDisplay());
                setIniCovarianceMatrix();
                runModel(model_sync,seq,ini_pos_simul,electrode_node.toString());
                retrieveTrajectory();
                SPACE_CHARGE = SPACE_CHARGE_ELECTRODE;
                CovarianceMatrix cov = getElectrodeCovarianceMatrix();
                setProbe(seq,electrode_node.getSDisplay());
                setCovarianceMatrix(cov);
                runModel(model_sync,seq,electrode_node.toString(),end_simul.toString());
                retrieveTrajectory();
            } else {
                setProbe(seq,start_simul.getSDisplay());
                setIniCovarianceMatrix();
                runModel(model_sync,seq,ini_pos_simul,end_simul.toString());
                retrieveTrajectory();
            }
            hasRun = true;
        }

    }
    
     /**
     * Sets tracker parameters
     */
    private void setTrackerParameters(AcceleratorSeq sequence) throws InstantiationException{

        envelopeTracker = AlgorithmFactory.createEnvelopeTracker(sequence);

        envelopeTracker.setUseSpacecharge(true);
        
        envelopeTracker.setStepSize(0.02);

    }

     /**
     * Sets tracker parameters
     */
    private void setTrackerParameters(AcceleratorSeqCombo sequence) throws InstantiationException{

        envelopeTracker = AlgorithmFactory.createEnvelopeTracker(sequence);

        envelopeTracker.setUseSpacecharge(true);
        
        envelopeTracker.setStepSize(0.02);

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
     * Gets the initial covariance matrix
     */
    private CovarianceMatrix getElectrodeCovarianceMatrix(){

        EnvelopeProbe probeResult = (EnvelopeProbe) model.getProbe();

        Trajectory trajectory = probeResult.getTrajectory();

        ArrayList<EnvelopeProbeState> stateElement = (ArrayList<EnvelopeProbeState>) trajectory.getStatesViaIndexer();
        CovarianceMatrix covmat;
        int[] index = trajectory.indicesForElement("LEBT-010:BMD-Rep-01");

        covmat = stateElement.get(index[0]).getCovarianceMatrix();

        return covmat;
    }
    
    /**
     * Sets magnets
     */
    private void setMagnetFields(AcceleratorSeq sequence){
        
        AcceleratorNode solenoid1 = sequence.getNodeWithId("LEBT-010:BMD-Sol-01");
        AcceleratorNode solenoid2 = sequence.getNodeWithId("LEBT-010:BMD-Sol-02");
        List<VDipoleCorr> CV = sequence.getNodesOfType("DCV");
        List<HDipoleCorr> CH = sequence.getNodesOfType("DCH");
        
        if(solenoid1 instanceof ESSSolFieldMap){
            ((ESSSolFieldMap)solenoid1).setDfltField(solenoidFields[0]);
        } else if (solenoid1 instanceof ESSMagFieldMap3D){
            ((ESSMagFieldMap3D)solenoid1).setDfltField(solenoidFields[0]);
        } else if (solenoid1 instanceof Solenoid){
            ((Solenoid)solenoid1).setDfltField(solenoidFields[0]);
        }
        
        if(solenoid2 instanceof ESSSolFieldMap){
            ((ESSSolFieldMap)solenoid2).setDfltField(solenoidFields[0]);
        } else if (solenoid2 instanceof ESSMagFieldMap3D){
            ((ESSMagFieldMap3D)solenoid2).setDfltField(solenoidFields[0]);
        } else if (solenoid2 instanceof Solenoid){
            ((Solenoid)solenoid2).setDfltField(solenoidFields[0]);
        }
        
        for(int i=0; i<2; i++){
            CV.get(i).setDfltField(correctorVFields[i]);
            CH.get(i).setDfltField(correctorHFields[i]);                        
        }        
    }
    
    /**
     * Sets magnets
     */
    private void setMagnetFields(AcceleratorSeqCombo sequence){
        
        AcceleratorNode solenoid1 = sequence.getNodeWithId("LEBT-010:BMD-Sol-01");
        AcceleratorNode solenoid2 = sequence.getNodeWithId("LEBT-010:BMD-Sol-02");
        List<VDipoleCorr> CV = sequence.getNodesOfType("DCV");
        List<HDipoleCorr> CH = sequence.getNodesOfType("DCH");
        
        if(solenoid1 instanceof ESSSolFieldMap){
            ((ESSSolFieldMap)solenoid1).setDfltField(solenoidFields[0]);
        } else if (solenoid1 instanceof ESSMagFieldMap3D){
            ((ESSMagFieldMap3D)solenoid1).setDfltField(solenoidFields[0]);
        } else if (solenoid1 instanceof Solenoid){
            ((Solenoid)solenoid1).setDfltField(solenoidFields[0]);
        }
        
        if(solenoid2 instanceof ESSSolFieldMap){
            ((ESSSolFieldMap)solenoid2).setDfltField(solenoidFields[1]);
        } else if (solenoid2 instanceof ESSMagFieldMap3D){
            ((ESSMagFieldMap3D)solenoid2).setDfltField(solenoidFields[1]);
        } else if (solenoid2 instanceof Solenoid){
            ((Solenoid)solenoid2).setDfltField(solenoidFields[1]);
        }
        
        for(int i=0; i<2; i++){
            CV.get(i).setDfltField(correctorVFields[i]);
            CH.get(i).setDfltField(correctorHFields[i]);                        
        }           
        
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
        double aperture=0.0;        
        
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
            Complex phi = new Complex(covmat.getMeanX()*1.0e+3,covmat.getMeanY()*1.0e+3);
            posR.add(phi.modulus());
            if(phi.phase()!=0){          
                if(phi.phase()>0){
                    posPhi.add(Math.PI - phi.phase());
                } else {
                    posPhi.add(Math.PI + phi.phase());    
                } 
            } else {
                posPhi.add(phi.phase());
            }

            for(int k = 0; k < sigmaOffsetX.length; k++){
                sigmaOffsetX[k].add(sigmaX[k].get(i)+posX.get(i));
                sigmaOffsetY[k].add(sigmaY[k].get(i)+posY.get(i));
                sigmaOffsetR[k].add(sigmaR[k].get(i)+posR.get(i));
            }
                      
            aperture = getAperture(stateElement.get(i).getPosition());
            if(Double.isFinite(aperture)){
                double x0 = (aperture-covmat.getMeanX())/(Math.sqrt(2.0)*covmat.getSigmaX());
                double x1 = (-aperture-covmat.getMeanX())/(Math.sqrt(2.0)*covmat.getSigmaX());
                double y0 = (aperture-covmat.getMeanY())/(Math.sqrt(2.0)*covmat.getSigmaY());
                double y1 = (-aperture-covmat.getMeanY())/(Math.sqrt(2.0)*covmat.getSigmaY());                   
                transmission_offset = Math.min(transmission_offset,(0.25*Math.abs(erf(x0)-erf(x1))*Math.abs(erf(y0)-erf(y1))));

                x0 = aperture/(Math.sqrt(2.0)*covmat.getSigmaX());
                y0 = aperture/(Math.sqrt(2.0)*covmat.getSigmaY());
                transmission = Math.min(transmission,(erf(x0)*erf(y0)));
            }
            
        }                        
        
    }    
    
    private double erf(double x){
        return Math.signum(x)*Math.sqrt((1-Math.exp(-1.0*x*x)))*(1+0.1749*Math.exp(-1.0*x*x)-0.0481*Math.exp(-2.0*x*x));
    }
    
    private double getAperture(double pos){
        double posArray = vacuumChamber.keySet().stream().min(Comparator.comparingDouble(val -> Math.abs(val - pos))).orElseThrow(() -> new NoSuchElementException("No value present"));
        if(Double.isFinite(posArray)){
            return vacuumChamber.get(posArray);
        } else {
            return Double.NaN;
        }
    }    
    
    /**
     * Retrieves and displays trajectory plots
     * @param newRun the simulation
     */
    private void readVacuumChamber(HashMap<Double,Double> vacuumChamber) {
        
        double[][] profile;
                        
        if(sequence != null){            
            vacuumChamber.clear();
            if(sequence instanceof AcceleratorSeqCombo){
                profile = ((AcceleratorSeqCombo) sequence).getAperProfile().getProfileXArray();
                for (int i = 0; i < profile[0].length ; i++) {
                    vacuumChamber.put(profile[0][i], profile[1][i]);
                }  
            } else if(sequence instanceof AcceleratorSeq){
                profile = ((AcceleratorSeq) sequence).getAperProfile().getProfileXArray();
                for (int i = 0; i < profile[0].length ; i++) {
                    vacuumChamber.put(profile[0][i], profile[1][i]);
                }  
            }
        }    

    }
   
    
}

