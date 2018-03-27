/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.extension.solver.Problem;
import xal.extension.solver.ProblemFactory;
import xal.extension.solver.Scorer;
import xal.extension.solver.SolveStopperFactory;
import xal.extension.solver.Solver;
import xal.extension.solver.Trial;
import xal.extension.solver.Variable;
import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;

/**
 *
 * @author nataliamilas
 */
public class MatchingSolver {

    private final InputParameters iniParams;

    private final InputParameters finalParams;
    
    private final double accuracy;
    
    private final Object sequence;
    
    private final ArrayList<Variable> arrVars;
    
    private final HashMap<Variable, Double> finalVals;
    
    private final HashMap<Variable, Double> simulVals;        
    
    private SimulationRunner matchRun;
        
    MatchingSolver(InputParameters iniParams, InputParameters finalParams, Object sequence, double accuracy) {
        this.iniParams = iniParams;
        this.finalParams = finalParams;
        this.accuracy = accuracy;
        this.sequence = sequence;
        this.arrVars = new ArrayList<Variable>();
        this.finalVals = new HashMap<>();
        this.simulVals = new HashMap<>();  
    }
       
    public void setVariables(){
        
        //set Variable with initial values and objectives    
                     
        arrVars.add(new Variable("x", iniParams.getX(), -0.01, 0.01));
        finalVals.put(arrVars.get(0),finalParams.getX());        
        simulVals.put(arrVars.get(0),iniParams.getX());        
        arrVars.add(new Variable("xp", iniParams.getXP(), -0.01, 0.01)); 
        finalVals.put(arrVars.get(1),finalParams.getXP());       
        simulVals.put(arrVars.get(1),iniParams.getXP());       
        arrVars.add(new Variable("y", iniParams.getY(), -0.01, 0.01));
        finalVals.put(arrVars.get(2),finalParams.getY());        
        simulVals.put(arrVars.get(2),iniParams.getY());        
        arrVars.add(new Variable("yp", iniParams.getYP(), -0.01, 0.01));
        finalVals.put(arrVars.get(3),finalParams.getYP());
        simulVals.put(arrVars.get(3),iniParams.getYP());
                               
        arrVars.add(new Variable("alphax", iniParams.getALPHAX(), -5, 5));
        finalVals.put(arrVars.get(4),finalParams.getALPHAX());
        simulVals.put(arrVars.get(4),iniParams.getALPHAX());
        arrVars.add(new Variable("betax", iniParams.getBETAX(), 0, 20)); 
        finalVals.put(arrVars.get(5),finalParams.getBETAX());
        simulVals.put(arrVars.get(5),iniParams.getBETAX());
        arrVars.add(new Variable("emittx", iniParams.getEMITTX(), 0.05e-06, 0.3e-06));
        finalVals.put(arrVars.get(6),finalParams.getEMITTX());
        simulVals.put(arrVars.get(6),iniParams.getEMITTX());
                
        arrVars.add(new Variable("alphay", iniParams.getALPHAY(), -5, 5));
        finalVals.put(arrVars.get(7),finalParams.getALPHAY());
        simulVals.put(arrVars.get(7),iniParams.getALPHAY());
        arrVars.add(new Variable("betay", iniParams.getBETAY(), 0, 20)); 
        finalVals.put(arrVars.get(8),finalParams.getBETAY());
        simulVals.put(arrVars.get(8),iniParams.getBETAY());
        arrVars.add(new Variable("emitty", iniParams.getEMITTY(), 0.05e-06, 0.3e-06));
        finalVals.put(arrVars.get(9),finalParams.getEMITTY());
        simulVals.put(arrVars.get(9),iniParams.getEMITTY());
                                         
       
    }
    
    public void initSimulation(double beamCurrent, double spaceChargeComp, double spaceChargeCompElectrode, Boolean electrode){
        
        //initializing simulation   
        if(sequence instanceof AcceleratorSeqCombo){     
            matchRun = new SimulationRunner((AcceleratorSeqCombo) sequence, MainFunctions.mainDocument.getModel().get());                                               
        } else if(sequence instanceof AcceleratorSeq){
            matchRun = new SimulationRunner((AcceleratorSeq) sequence, MainFunctions.mainDocument.getModel().get()); 
        }           
        
        matchRun.setInitSimulPos(iniParams.getName()); 
        matchRun.setFinalSimulPos(finalParams.getName()); 
        matchRun.setBeamCurrent(beamCurrent);
        matchRun.setSpaceChargeCompensation(spaceChargeComp,spaceChargeCompElectrode);
        matchRun.setElectrode(electrode);        
        
    }    
    
    private void getSimulationValues(EnvelopeProbe probe){
        
        Trajectory trajectory = probe.getTrajectory();
        
        ArrayList<EnvelopeProbeState> stateElement = (ArrayList<EnvelopeProbeState>) trajectory.getStatesViaIndexer();                       
        
        double beta_gamma = probe.getGamma()*probe.getBeta();         
        CovarianceMatrix covmat = stateElement.get(stateElement.size()-1).getCovarianceMatrix(); 
        Twiss[] twiss = covmat.computeTwiss();
        
        simulVals.replace(arrVars.get(4),twiss[0].getAlpha());
        simulVals.replace(arrVars.get(5),twiss[0].getBeta());
        simulVals.replace(arrVars.get(6),twiss[0].getEmittance()*beta_gamma);
                
        simulVals.replace(arrVars.get(7),twiss[1].getAlpha());
        simulVals.replace(arrVars.get(8),twiss[1].getBeta());
        simulVals.replace(arrVars.get(9),twiss[1].getEmittance()*beta_gamma);                       
            
        PhaseVector iniPos = covmat.getMean();
        simulVals.replace(arrVars.get(0),iniPos.getx());        
        simulVals.replace(arrVars.get(1),iniPos.getxp());       
        simulVals.replace(arrVars.get(2),iniPos.gety());        
        simulVals.replace(arrVars.get(3),iniPos.getyp());
                                               
    }

    private void setParameters(Trial trial){                        
        
        matchRun.setInitialBeamParameters(trial.getTrialPoint().getValue(arrVars.get(0)),trial.getTrialPoint().getValue(arrVars.get(1)),trial.getTrialPoint().getValue(arrVars.get(2)),trial.getTrialPoint().getValue(arrVars.get(3)));        
        matchRun.setBeamTwissX(trial.getTrialPoint().getValue(arrVars.get(4)),trial.getTrialPoint().getValue(arrVars.get(5)),trial.getTrialPoint().getValue(arrVars.get(6)));
        matchRun.setBeamTwissY(trial.getTrialPoint().getValue(arrVars.get(7)),trial.getTrialPoint().getValue(arrVars.get(8)),trial.getTrialPoint().getValue(arrVars.get(9)));                    
        
    }
    
    public InputParameters newInputValues(){
        
        EnvelopeProbe finalProbe = new EnvelopeProbe();
        
        finalProbe = matchRun.getFinalProbe();
        
        double beta_gamma = finalProbe.getGamma()*finalProbe.getBeta();         
        CovarianceMatrix cov = finalProbe.getCovariance();
        Twiss[] twiss = cov.computeTwiss();
        
        Twiss twissX = new Twiss(finalVals.get(arrVars.get(4)),finalVals.get(arrVars.get(5)),finalVals.get(arrVars.get(6))/beta_gamma);
        Twiss twissY = new Twiss(finalVals.get(arrVars.get(7)),finalVals.get(arrVars.get(8)),finalVals.get(arrVars.get(9))/beta_gamma);
        Twiss twissZ = new Twiss(twiss[2].getAlpha(),twiss[2].getBeta(),twiss[2].getEmittance());
        
        PhaseVector initial_pos = new PhaseVector(Double.toString(finalVals.get(arrVars.get(0))) + "," +
               Double.toString(finalVals.get(arrVars.get(1))) + "," +
               Double.toString(finalVals.get(arrVars.get(2))) + "," +
               Double.toString(finalVals.get(arrVars.get(3))) + "," +
                                "0," +      //z
                                "0");       //z'

        cov = CovarianceMatrix.buildCovariance(twissX,twissY,twissZ,initial_pos);
        finalProbe.setCovariance(cov);        
        
        return new InputParameters(finalProbe);
       
    }
    
    public void solve()
    {                        
        
        setVariables();                
        
        Scorer scorer = new Scorer() {
            @Override
            public double score(Trial trial, List<Variable> list) {
                try {
                    //calculate using new initial trial values
                    setParameters(trial);
                    matchRun.runSimulation();
                } catch (ModelException | InstantiationException ex) {
                    Logger.getLogger(MatchingSolver.class.getName()).log(Level.SEVERE, null, ex);
                }
                getSimulationValues(matchRun.getFinalProbe());
                double score = 0;
                double diff = 0; 
                //Trajectory error
                for(int i=0; i<4; i++){                        
                    diff = 1e3*(simulVals.get(arrVars.get(i)) - finalVals.get(arrVars.get(i)));
                    score = score + diff*diff;
                }
                
                //Emittance error
                diff = (simulVals.get(arrVars.get(6)) - finalVals.get(arrVars.get(6)))/finalVals.get(arrVars.get(6));
                score = score + diff*diff;
                diff = (simulVals.get(arrVars.get(9)) - finalVals.get(arrVars.get(9)))/finalVals.get(arrVars.get(9));
                score = score + diff*diff;
                
                //Twiss Parameter error
                double Dbeta = (simulVals.get(arrVars.get(5)) - finalVals.get(arrVars.get(5)))/finalVals.get(arrVars.get(5));
                double Dalpha = (simulVals.get(arrVars.get(4)) - finalVals.get(arrVars.get(4)))-finalVals.get(arrVars.get(4))*Dbeta;
                score = score + Dbeta*Dbeta + Dalpha*Dalpha;
                Dbeta = (simulVals.get(arrVars.get(8)) - finalVals.get(arrVars.get(8)))/finalVals.get(arrVars.get(8));
                Dalpha = (simulVals.get(arrVars.get(7)) - finalVals.get(arrVars.get(7)))-finalVals.get(arrVars.get(7))*Dbeta;
                score = score + Dbeta*Dbeta + Dalpha*Dalpha;              
                
                return score;
                
            }
                
        };

        Problem problem = ProblemFactory.getInverseSquareMinimizerProblem(arrVars, scorer, 0.0001); 

        Solver solver = new Solver(SolveStopperFactory.maxEvaluationsStopper( 1000 ));

        solver.solve(problem);
        
        System.out.print(solver.getScoreBoard().getSatisfaction());
        
        arrVars.forEach(var->{
            finalVals.put(var,solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(var));
        });                

    }
}

    

