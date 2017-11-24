/*
 * Copyright (C) 2017 European Spallation Source ERIC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.app.trajectorycorrection2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.Probe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.BPM;
import xal.tools.math.r3.R3;

/**
 * prepare and run an OpenXal simulation of the machine
 * @author nataliamilas
 */
public class RunSimulationService{

    private final AcceleratorSeq sequence;
    private volatile AtomicReference<String> synchronizationMode = new AtomicReference<>(Scenario.SYNC_MODE_DESIGN);

    public RunSimulationService( AcceleratorSeq sequence ) {
        this.sequence = sequence;
    }
    
    public RunSimulationService( AcceleratorSeqCombo sequence ) {
        this.sequence = sequence;
    }

    public void setSynchronizationMode( String synchronizationMode ) {
        this.synchronizationMode.set(synchronizationMode);
    }

    public HashMap<BPM, Double> runTrajectorySimulation(List<BPM> bpmList,String plane) throws InstantiationException, ModelException{

        EnvTrackerAdapt envelopeTracker = AlgorithmFactory.createEnvTrackerAdapt(sequence);

        envelopeTracker.setMaxIterations(1000);
        envelopeTracker.setAccuracyOrder(1);
        envelopeTracker.setErrorTolerance(0.001);
        envelopeTracker.setUseSpacecharge(true);

        Probe<?> probe = ProbeFactory.getEnvelopeProbe(sequence, envelopeTracker);
        Scenario model = Scenario.newScenarioFor(sequence);

        model.setProbe(probe);
        model.setSynchronizationMode(synchronizationMode.get());
        model.resync();
        model.run();

        probe = model.getProbe();

        Trajectory<? extends EnvelopeProbeState> trajectory = (Trajectory<? extends EnvelopeProbeState>) probe.getTrajectory();
        HashMap<BPM, Double> trajectoryFinal = new HashMap();

        if (plane.equals("X") || plane.equals("x")) {
            bpmList.forEach(bpm -> {                    
                trajectoryFinal.put(bpm,trajectory.getStatesViaIndexer().get(trajectory.indicesForElement(bpm.toString())[0]).getCovarianceMatrix().getMean().getx());
            }); 
        } else if (plane.equals("Y") || plane.equals("y")){
            bpmList.forEach(bpm -> {                    
                trajectoryFinal.put(bpm,trajectory.getStatesViaIndexer().get(trajectory.indicesForElement(bpm.toString())[0]).getCovarianceMatrix().getMean().gety());
            }); 
        } else {
            bpmList.forEach(bpm -> {                    
                trajectoryFinal.put(bpm,0.0);
            }); 
        }

        return trajectoryFinal;

            
    };   
    
    public HashMap<xal.smf.AcceleratorNode, R3> runTwissSimulation(List<xal.smf.AcceleratorNode> objList) throws InstantiationException, ModelException{
               
        EnvTrackerAdapt envelopeTracker = AlgorithmFactory.createEnvTrackerAdapt(sequence);

        envelopeTracker.setMaxIterations(1000);
        envelopeTracker.setAccuracyOrder(1);
        envelopeTracker.setErrorTolerance(0.001);
        envelopeTracker.setUseSpacecharge(false);

        Probe<?> probe = ProbeFactory.getEnvelopeProbe(sequence, envelopeTracker);
        Scenario model = Scenario.newScenarioFor(sequence);
        
        model.setProbe(probe);
        model.setSynchronizationMode(synchronizationMode.get());
        model.resync();
        model.run();

        probe = model.getProbe();
        

        Trajectory<? extends EnvelopeProbeState> trajectory = (Trajectory<? extends EnvelopeProbeState>) probe.getTrajectory();
        List<? extends EnvelopeProbeState> stateElement = trajectory.getStatesViaIndexer();
        
        
        double betax0=stateElement.get(0).getCovarianceMatrix().computeTwiss()[0].getBeta();
        double betay0=stateElement.get(0).getCovarianceMatrix().computeTwiss()[1].getBeta();
        
        //initialize arrays
        List<Double> phi_x = new ArrayList<>();
        List<Double> phi_y = new ArrayList<>();
        List<Double> position = new ArrayList<>();

        //append position zero        
        double gamma = stateElement.get(0).getGamma();
        double betax1=stateElement.get(0).getCovarianceMatrix().computeTwiss()[0].getBeta();
        double betay1=stateElement.get(0).getCovarianceMatrix().computeTwiss()[1].getBeta();
        //add initial condition
        position.add(stateElement.get(0).getPosition());
        phi_y.add(stateElement.get(0).getResponseMatrixNoSpaceCharge().projectR4x4().getElem(0,1)*Math.sqrt(Math.sqrt(gamma*gamma-1)/(betax0*betax1)));
        phi_x.add(stateElement.get(0).getResponseMatrixNoSpaceCharge().projectR4x4().getElem(2,3)*Math.sqrt(Math.sqrt(gamma*gamma-1)/(betay0*betay1)));

        //append other positions (remove repetitions)
        for(int i=1; i<stateElement.size();i++){
            //position.add(stateElement.get(i).getPosition());
            gamma = stateElement.get(i).getGamma();
            betax1=stateElement.get(i).getCovarianceMatrix().computeTwiss()[0].getBeta();
            betay1=stateElement.get(i).getCovarianceMatrix().computeTwiss()[1].getBeta();
            phi_y.add(stateElement.get(i).getResponseMatrixNoSpaceCharge().projectR4x4().getElem(0,1)*Math.sqrt(Math.sqrt(gamma*gamma-1)/(betax0*betax1)));
            phi_x.add(stateElement.get(i).getResponseMatrixNoSpaceCharge().projectR4x4().getElem(2,3)*Math.sqrt(Math.sqrt(gamma*gamma-1)/(betay0*betay1)));  
            System.out.print(stateElement.get(i).getResponseMatrixNoSpaceCharge().projectR4x4()+"\n");
        }      
        
        //normalize to the maximum value
        //double max_x = phi_x.stream().max(Comparator.naturalOrder()).get();
        //double max_y = phi_y.stream().max(Comparator.naturalOrder()).get();
        
        //for(int i=0; i<phi_x.size(); i++){
        //    phi_x.set(i, phi_x.get(i)/max_x);
        //    phi_y.set(i, phi_y.get(i)/max_y);
        //}

        // Find zero crossings
        List<Integer> i_0_x = new ArrayList<>();
        List<Integer> i_0_y = new ArrayList<>();
        i_0_x.add(0);
        i_0_y.add(0);
        for(int i=1; i<phi_x.size(); i++){
            if (phi_x.get(i-1)*phi_x.get(i)<0) {
                i_0_x.add(i);
            }
            if (phi_y.get(i-1)*phi_y.get(i)<0) {
                i_0_y.add(i);
            }
        }              
        
        // Divide data into half periods (this part depends on the last period) 
        List<List<Double>> x_180 = new ArrayList<>();
        List<List<Double>> y_180 = new ArrayList<>();
        if(i_0_x.size()>1){
            for(int k=0; k<i_0_x.size(); k++){
                x_180.add(phi_x.subList(i_0_x.get(k),i_0_x.get(k+1)));
            }
        } else {
            x_180.add(phi_x);
        }
        
        if(i_0_y.size()>1){
            for(int k=0; k<i_0_y.size(); k++){
               y_180.add(phi_y.subList(i_0_y.get(k),i_0_y.get(k+1)));
            }
        } else {
            y_180.add(phi_y);
        }
     
        //Normalize in each half period
        //x_180.forEach(subList ->{           
        //    subList.stream().max(Comparator.naturalOrder()).get();            
        //    for(int i=0; i<subList.size(); i++){
        //        subList.set(i, subList.get(i)/max_x);
        //    }
        //});
        //y_180.forEach(subList ->{           
        //    subList.stream().max(Comparator.naturalOrder()).get();            
        //    for(int i=0; i<subList.size(); i++){
        //        subList.set(i, subList.get(i)/max_y);
        //    }
        //});

        // Phase in [2*pi], taking care the arcsin quadrant issue
        List<Integer> index = new ArrayList<>();
        List<Double> phsx = new ArrayList<>();
        List<Double> phsy = new ArrayList<>();
        List<Double> phs_k = new ArrayList<>();              
        
        x_180.forEach(subList -> {            
            subList.forEach(item -> phs_k.add(Math.asin(item)/(2*Math.PI)));
            for(int i=1; i<phs_k.size(); i++){
                if (phs_k.get(i)<phs_k.get(i-1) && (phs_k.get(i-1)-phs_k.get(i)>1e-10)){
                    index.add(0, i);  
                    break;
                } else {
                    index.add(0, 0); 
                }
            }
            if (x_180.indexOf(subList)==(x_180.size()-1)){
                for(int i=index.get(0)+1; i<phs_k.size(); i++){
                    if (phs_k.get(i)>phs_k.get(i-1) && (phs_k.get(i)-phs_k.get(i-1)>1e-10)){
                        index.add(1, i);
                        break;
                    } else {
                        index.add(1, phs_k.size());
                    }
                }
            }
            if (index.size()>1){
                for(int i=index.get(0); i<index.get(1); i++){
                    phs_k.set(i, 0.5-phs_k.get(i));
                }
                for(int i=index.get(1); i<phs_k.size(); i++){
                    phs_k.set(i, 0.5+phs_k.get(i));
                }
            } else {
                for(int i=index.get(0); i<phs_k.size(); i++){
                    phs_k.set(i, 0.5-phs_k.get(i));
                }
            }
            phs_k.forEach(phase -> phsx.add(phase));
            index.clear();
            phs_k.clear();
        });

        y_180.forEach(subList -> {            
            subList.forEach(item -> phs_k.add(Math.asin(item)/(2*Math.PI)));
            for(int i=1; i<phs_k.size(); i++){
                if (phs_k.get(i)<phs_k.get(i-1) && (phs_k.get(i-1)-phs_k.get(i)>1e-10)){
                    index.add(0, i);  
                    break;
                } else {
                    index.add(0, 0); 
                }
            }
            if (y_180.indexOf(subList)==(y_180.size()-1)){
                for(int i=index.get(0)+1; i<phs_k.size(); i++){
                    if (phs_k.get(i)>phs_k.get(i-1) && (phs_k.get(i)-phs_k.get(i-1)>1e-10)){
                        index.add(1, i);
                        break;
                    } else {
                        index.add(1, phs_k.size());
                    }
                }
            }
            if (index.size()>1){
                for(int i=index.get(0); i<index.get(1); i++){
                    phs_k.set(i, 0.5-phs_k.get(i));
                }
                for(int i=index.get(1); i<phs_k.size(); i++){
                    phs_k.set(i, 0.5+phs_k.get(i));
                }
            } else {
                for(int i=index.get(0); i<phs_k.size(); i++){
                    phs_k.set(i, 0.5-phs_k.get(i));
                }
            }
            phs_k.forEach(phase -> phsy.add(phase));
            index.clear();
            phs_k.clear();
        });
               
        HashMap<xal.smf.AcceleratorNode, R3> betatronPhase = new HashMap();                
        xal.smf.AcceleratorNode nodeId;
        
        System.out.print(phsx+"\n");
        System.out.print(phsy+"\n");
        
        for(int i=1; i<trajectory.numStates();i++){
            if (objList.contains(sequence.getNodeWithId(stateElement.get(i).getHardwareNodeId()))){
                betatronPhase.put(sequence.getNodeWithId(stateElement.get(i).getHardwareNodeId()),new R3(phsx.get(i),phsy.get(i),0.0));
            }
        };
        
        System.out.print(betatronPhase);
        
        return betatronPhase;

            
    };   

}

