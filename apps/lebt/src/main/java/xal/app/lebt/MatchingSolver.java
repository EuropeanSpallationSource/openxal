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
    
    private final ArrayList<Variable> trajVars;
    
    private final ArrayList<Variable> twissVars;
    
    private final HashMap<Variable, Double> finalVals;
    
    private final HashMap<Variable, Double> simulVals;        
    
    private SimulationRunner matchRun;
        
    MatchingSolver(InputParameters iniParams, InputParameters finalParams, Object sequence, double accuracy) {
        this.iniParams = iniParams;
        this.finalParams = finalParams;
        this.accuracy = accuracy;
        this.sequence = sequence;
        this.trajVars = new ArrayList<Variable>();
        this.twissVars = new ArrayList<Variable>();
        this.finalVals = new HashMap<>();
        this.simulVals = new HashMap<>();  
    }
       
    public void setVariables(){
        
        //set Variable with initial values and objectives    
                     
        trajVars.add(new Variable("x", iniParams.getX(), -0.01, 0.01));
        finalVals.put(trajVars.get(0),finalParams.getX());        
        simulVals.put(trajVars.get(0),iniParams.getX());        
        trajVars.add(new Variable("xp", iniParams.getXP(), -0.01, 0.01)); 
        finalVals.put(trajVars.get(1),finalParams.getXP());       
        simulVals.put(trajVars.get(1),iniParams.getXP());       
        trajVars.add(new Variable("y", iniParams.getY(), -0.01, 0.01));
        finalVals.put(trajVars.get(2),finalParams.getY());        
        simulVals.put(trajVars.get(2),iniParams.getY());        
        trajVars.add(new Variable("yp", iniParams.getYP(), -0.01, 0.01));
        finalVals.put(trajVars.get(3),finalParams.getYP());
        simulVals.put(trajVars.get(3),iniParams.getYP());
                               
        twissVars.add(new Variable("alphax", iniParams.getALPHAX(), -5, 5));
        finalVals.put(twissVars.get(0),finalParams.getALPHAX());
        simulVals.put(twissVars.get(0),iniParams.getALPHAX());
        twissVars.add(new Variable("betax", iniParams.getBETAX(), 0, 20)); 
        finalVals.put(twissVars.get(1),finalParams.getBETAX());
        simulVals.put(twissVars.get(1),iniParams.getBETAX());
        twissVars.add(new Variable("emittx", iniParams.getEMITTX(), 0.05e-06, 0.3e-06));
        finalVals.put(twissVars.get(2),finalParams.getEMITTX());
        simulVals.put(twissVars.get(2),iniParams.getEMITTX());
                
        twissVars.add(new Variable("alphay", iniParams.getALPHAY(), -5, 5));
        finalVals.put(twissVars.get(3),finalParams.getALPHAY());
        simulVals.put(twissVars.get(3),iniParams.getALPHAY());
        twissVars.add(new Variable("betay", iniParams.getBETAY(), 0, 20)); 
        finalVals.put(twissVars.get(4),finalParams.getBETAY());
        simulVals.put(twissVars.get(4),iniParams.getBETAY());
        twissVars.add(new Variable("emitty", iniParams.getEMITTY(), 0.05e-06, 0.3e-06));
        finalVals.put(twissVars.get(5),finalParams.getEMITTY());
        simulVals.put(twissVars.get(5),iniParams.getEMITTY());
                                         
       
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
        
        simulVals.replace(twissVars.get(0),twiss[0].getAlpha());
        simulVals.replace(twissVars.get(1),twiss[0].getBeta());
        simulVals.replace(twissVars.get(2),twiss[0].getEmittance()*beta_gamma);
                
        simulVals.replace(twissVars.get(3),twiss[1].getAlpha());
        simulVals.replace(twissVars.get(4),twiss[1].getBeta());
        simulVals.replace(twissVars.get(5),twiss[1].getEmittance()*beta_gamma);                       
            
        PhaseVector iniPos = covmat.getMean();
        simulVals.replace(trajVars.get(0),iniPos.getx());        
        simulVals.replace(trajVars.get(1),iniPos.getxp());       
        simulVals.replace(trajVars.get(2),iniPos.gety());        
        simulVals.replace(trajVars.get(3),iniPos.getyp());
                                               
    }

    private void setParameters(Trial trial, int solver){                        
        //trajectory
        if (solver == 0){
            matchRun.setInitialBeamParameters(trial.getTrialPoint().getValue(trajVars.get(0)),trial.getTrialPoint().getValue(trajVars.get(1)),trial.getTrialPoint().getValue(trajVars.get(2)),trial.getTrialPoint().getValue(trajVars.get(3)));        
        }
        //twiss
        if (solver ==1){
            matchRun.setBeamTwissX(trial.getTrialPoint().getValue(twissVars.get(0)),trial.getTrialPoint().getValue(twissVars.get(1)),trial.getTrialPoint().getValue(twissVars.get(2)));
            matchRun.setBeamTwissY(trial.getTrialPoint().getValue(twissVars.get(3)),trial.getTrialPoint().getValue(twissVars.get(4)),trial.getTrialPoint().getValue(twissVars.get(5)));                    
        }
        
    }
    
    public InputParameters newInputValues(){
        
        EnvelopeProbe finalProbe = new EnvelopeProbe();
        
        finalProbe = matchRun.getFinalProbe();
        
        double beta_gamma = finalProbe.getGamma()*finalProbe.getBeta();         
        CovarianceMatrix cov = finalProbe.getCovariance();
        Twiss[] twiss = cov.computeTwiss();
        
        Twiss twissX = new Twiss(finalVals.get(twissVars.get(0)),finalVals.get(twissVars.get(1)),finalVals.get(twissVars.get(2))/beta_gamma);
        Twiss twissY = new Twiss(finalVals.get(twissVars.get(3)),finalVals.get(twissVars.get(4)),finalVals.get(twissVars.get(5))/beta_gamma);
        Twiss twissZ = new Twiss(twiss[2].getAlpha(),twiss[2].getBeta(),twiss[2].getEmittance());
        
        PhaseVector initial_pos = new PhaseVector(Double.toString(finalVals.get(trajVars.get(0))) + "," +
               Double.toString(finalVals.get(trajVars.get(1))) + "," +
               Double.toString(finalVals.get(trajVars.get(2))) + "," +
               Double.toString(finalVals.get(trajVars.get(3))) + "," +
                                "0," +      //z
                                "0");       //z'

        cov = CovarianceMatrix.buildCovariance(twissX,twissY,twissZ,initial_pos);
        finalProbe.setCovariance(cov);        
        
        return new InputParameters(finalProbe);
       
    }
    
    public void solve()
    {                        
        
        setVariables();                
        
        Scorer scorerTraj = new Scorer() {
            @Override
            public double score(Trial trial, List<Variable> list) {
                try {
                    //calculate using new initial trial values
                    setParameters(trial,0);
                    matchRun.runSimulation();
                } catch (ModelException | InstantiationException ex) {
                    Logger.getLogger(MatchingSolver.class.getName()).log(Level.SEVERE, null, ex);
                }
                getSimulationValues(matchRun.getFinalProbe());
                double score = 0;
                double diff = 0; 
                //Trajectory error
                for(int i=0; i<trajVars.size(); i++){                        
                    diff = 1e3*(simulVals.get(trajVars.get(i)) - finalVals.get(trajVars.get(i)));
                    score = score + diff*diff;
                }                                    
                
                return score;
                
            }
                
        };
        
        Scorer scorerTwiss = new Scorer() {
            @Override
            public double score(Trial trial, List<Variable> list) {
                try {
                    //calculate using new initial trial values
                    setParameters(trial,1);
                    matchRun.runSimulation();
                } catch (ModelException | InstantiationException ex) {
                    Logger.getLogger(MatchingSolver.class.getName()).log(Level.SEVERE, null, ex);
                }
                getSimulationValues(matchRun.getFinalProbe());
                double score = 0;
                double diff = 0; 
                 //Emittance error
                diff = (simulVals.get(twissVars.get(2)) - finalVals.get(twissVars.get(2)))/finalVals.get(twissVars.get(2));
                score = score + diff*diff;
                diff = (simulVals.get(twissVars.get(5)) - finalVals.get(twissVars.get(5)))/finalVals.get(twissVars.get(5));
                score = score + diff*diff;
                
                //Twiss Parameter error
                double Dbeta = (simulVals.get(twissVars.get(1)) - finalVals.get(twissVars.get(1)))/finalVals.get(twissVars.get(1));
                double Dalpha = (simulVals.get(twissVars.get(0)) - finalVals.get(twissVars.get(0)))-finalVals.get(twissVars.get(0))*Dbeta;
                score = score + 1/2*Math.sqrt(Dbeta*Dbeta + Dalpha*Dalpha);
                Dbeta = (simulVals.get(twissVars.get(4)) - finalVals.get(twissVars.get(4)))/finalVals.get(twissVars.get(4));
                Dalpha = (simulVals.get(twissVars.get(3)) - finalVals.get(twissVars.get(3)))-finalVals.get(twissVars.get(3))*Dbeta;
                score = score + 1/2*Math.sqrt(Dbeta*Dbeta + Dalpha*Dalpha);                                       
                
                return score;
                
            }
                
        };

        Problem problemTraj = ProblemFactory.getInverseSquareMinimizerProblem(trajVars, scorerTraj, 0.0001); 

        Solver solverTraj = new Solver(SolveStopperFactory.maxEvaluationsStopper(100));

        solverTraj.solve(problemTraj);
               
        trajVars.forEach(var->{
            finalVals.put(var,solverTraj.getScoreBoard().getBestSolution().getTrialPoint().getValue(var));
            simulVals.put(var,solverTraj.getScoreBoard().getBestSolution().getTrialPoint().getValue(var));
        }); 
        
        Problem problemTwiss = ProblemFactory.getInverseSquareMinimizerProblem(twissVars, scorerTwiss, 0.00001); 

        Solver solverTwiss = new Solver(SolveStopperFactory.maxEvaluationsStopper(1000));

        solverTwiss.solve(problemTwiss);
        
        twissVars.forEach(var->{
            finalVals.put(var,solverTwiss.getScoreBoard().getBestSolution().getTrialPoint().getValue(var));
        });

    }
}

    

