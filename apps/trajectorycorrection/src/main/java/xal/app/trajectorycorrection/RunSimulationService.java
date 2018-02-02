/*
 * Copyright (C) 2018 European Spallation Source ERIC
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
package xal.app.trajectorycorrection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.alg.EnvelopeTracker;
import xal.model.probe.Probe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.BPM;
import xal.tools.math.r3.R3;

/**
 * prepare and run an OpenXal simulation of the machine
 *
 * @author nataliamilas
 */
public class RunSimulationService {

    private final AcceleratorSeq sequence;
    private volatile AtomicReference<String> synchronizationMode = new AtomicReference<>(Scenario.SYNC_MODE_DESIGN);

    public RunSimulationService(AcceleratorSeq sequence) {
        this.sequence = sequence;
    }

    public RunSimulationService(AcceleratorSeqCombo sequence) {
        this.sequence = sequence;
    }

    public void setSynchronizationMode(String synchronizationMode) {
        this.synchronizationMode.set(synchronizationMode);
    }

    public HashMap<BPM, Double> runTrajectorySimulation(List<BPM> bpmList, String plane) throws InstantiationException, ModelException {

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
                trajectoryFinal.put(bpm, trajectory.getStatesViaIndexer().get(trajectory.indicesForElement(bpm.toString())[0]).getCovarianceMatrix().getMean().getx());
            });
        } else if (plane.equals("Y") || plane.equals("y")) {
            bpmList.forEach(bpm -> {
                trajectoryFinal.put(bpm, trajectory.getStatesViaIndexer().get(trajectory.indicesForElement(bpm.toString())[0]).getCovarianceMatrix().getMean().gety());
            });
        } else {
            bpmList.forEach(bpm -> {
                trajectoryFinal.put(bpm, 0.0);
            });
        }

        return trajectoryFinal;

    }

    ;   
    
    public HashMap<AcceleratorNode, R3> runTwissSimulation(List<AcceleratorNode> objList) throws InstantiationException, ModelException {

        EnvelopeTracker envelopeTracker = AlgorithmFactory.createEnvelopeTracker(sequence);

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

        double betax0 = stateElement.get(0).getCovarianceMatrix().computeTwiss()[0].getBeta();
        double betay0 = stateElement.get(0).getCovarianceMatrix().computeTwiss()[1].getBeta();

        //initialize arrays
        List<Double> phi_x = new ArrayList<>();
        List<Double> phi_y = new ArrayList<>();

        //append position zero        
        double beta_v = stateElement.get(0).getBeta();
        double betax1 = stateElement.get(0).getCovarianceMatrix().computeTwiss()[0].getBeta();
        double betay1 = stateElement.get(0).getCovarianceMatrix().computeTwiss()[1].getBeta();
        //add initial condition
        phi_y.add(stateElement.get(0).getResponseMatrixNoSpaceCharge().projectR4x4().getElem(0, 1) * Math.sqrt(beta_v / (betax0 * betax1)));
        phi_x.add(stateElement.get(0).getResponseMatrixNoSpaceCharge().projectR4x4().getElem(2, 3) * Math.sqrt(beta_v / (betay0 * betay1)));

        //append other positions (remove repetitions)
        for (int i = 1; i < stateElement.size(); i++) {
            beta_v = stateElement.get(0).getBeta();
            betax1 = stateElement.get(i).getCovarianceMatrix().computeTwiss()[0].getBeta();
            betay1 = stateElement.get(i).getCovarianceMatrix().computeTwiss()[1].getBeta();
            phi_y.add(stateElement.get(i).getResponseMatrixNoSpaceCharge().projectR4x4().getElem(0, 1) * Math.sqrt(beta_v / (betax0 * betax1)));
            phi_x.add(stateElement.get(i).getResponseMatrixNoSpaceCharge().projectR4x4().getElem(2, 3) * Math.sqrt(beta_v / (betay0 * betay1)));
        }

        //normalize to the maximum value
        double max_x = Collections.max(phi_x, new Comparator<Double>() {
            @Override
            public int compare(Double x, Double y) {
                return Math.abs(x) < Math.abs(y) ? -1 : 1;
            }
        });
        double max_y = Collections.max(phi_y, new Comparator<Double>() {
            @Override
            public int compare(Double x, Double y) {
                return Math.abs(x) < Math.abs(y) ? -1 : 1;
            }
        });

        for (int i = 0; i < phi_x.size(); i++) {
            if (max_x != 0) {
                phi_x.set(i, phi_x.get(i) / max_x);
            }
            if (max_y != 0) {
                phi_y.set(i, phi_y.get(i) / max_y);
            }
        }

        // Find zero crossings
        List<Integer> i_0_x = new ArrayList<>();
        List<Integer> i_0_y = new ArrayList<>();
        i_0_x.add(0);
        i_0_y.add(0);
        for (int i = 1; i < phi_x.size(); i++) {
            if (phi_x.get(i - 1) * phi_x.get(i) < 0) {
                i_0_x.add(i);
            }
            if (phi_y.get(i - 1) * phi_y.get(i) < 0) {
                i_0_y.add(i);
            }
        }

        if (i_0_x.get(i_0_x.size() - 1) != phi_x.size() - 1) {
            i_0_x.add(phi_x.size());
        }

        if (i_0_y.get(i_0_y.size() - 1) != phi_y.size() - 1) {
            i_0_y.add(phi_y.size());
        }

        // Divide data into half periods 
        List<List<Double>> x_180 = new ArrayList<>();
        List<List<Double>> y_180 = new ArrayList<>();
        List<Double> maxx_180 = new ArrayList<>();
        List<Double> maxy_180 = new ArrayList<>();
        if (i_0_x.size() > 1) {
            for (int k = 0; k < i_0_x.size() - 1; k++) {
                maxx_180.add(Collections.max(phi_x.subList(i_0_x.get(k), i_0_x.get(k + 1)), new Comparator<Double>() {
                    @Override
                    public int compare(Double x, Double y) {
                        return Math.abs(x) < Math.abs(y) ? -1 : 1;
                    }
                }));
                x_180.add(phi_x.subList(i_0_x.get(k), i_0_x.get(k + 1)));
            }
        } else {
            maxx_180.add(Collections.max(phi_x, new Comparator<Double>() {
                @Override
                public int compare(Double x, Double y) {
                    return Math.abs(x) < Math.abs(y) ? -1 : 1;
                }
            }));
            x_180.add(phi_x);
        }

        if (i_0_y.size() > 1) {
            for (int k = 0; k < i_0_y.size() - 1; k++) {
                maxy_180.add(Collections.max(phi_y.subList(i_0_y.get(k), i_0_y.get(k + 1)), new Comparator<Double>() {
                    @Override
                    public int compare(Double x, Double y) {
                        return Math.abs(x) < Math.abs(y) ? -1 : 1;
                    }
                }));
                y_180.add(phi_y.subList(i_0_y.get(k), i_0_y.get(k + 1)));
            }
        } else {
            maxy_180.add(Collections.max(phi_y, new Comparator<Double>() {
                @Override
                public int compare(Double x, Double y) {
                    return Math.abs(x) < Math.abs(y) ? -1 : 1;
                }
            }));
            y_180.add(phi_y);
        }

        //Normalize in each half period and calculate absolute value
        x_180.forEach(subList -> {
            for (int i = 0; i < subList.size(); i++) {
                if (maxx_180.get(x_180.indexOf(subList)) != 0) {
                    subList.set(i, Math.abs(subList.get(i) / maxx_180.get(x_180.indexOf(subList))));
                }
            }
        });
        y_180.forEach(subList -> {
            for (int i = 0; i < subList.size(); i++) {
                if (maxy_180.get(y_180.indexOf(subList)) != 0) {
                    subList.set(i, Math.abs(subList.get(i) / maxy_180.get(y_180.indexOf(subList))));
                }
            }
        });

        // Phase in [2*pi], taking care the arcsin quadrant issue
        List<Integer> index = new ArrayList<>();
        List<Double> phsx = new ArrayList<>();
        List<Double> phsy = new ArrayList<>();
        List<Double> phs_k = new ArrayList<>();

        x_180.forEach(subList -> {
            subList.forEach(item -> phs_k.add(Math.asin(item) / (2.0 * Math.PI)));
            index.add(0, 0);
            for (int i = 1; i < phs_k.size(); i++) {
                if (phs_k.get(i) < phs_k.get(i - 1) && (phs_k.get(i - 1) - phs_k.get(i) > 1e-10)) {
                    index.set(0, i);
                    break;
                }
            }
            if (x_180.indexOf(subList) == (x_180.size() - 1)) {
                for (int i = index.get(0) + 1; i < phs_k.size(); i++) {
                    if (phs_k.get(i) > phs_k.get(i - 1) && (phs_k.get(i) - phs_k.get(i - 1) > 1e-10)) {
                        index.add(1, i);
                        break;
                    }
                }
            }
            if (index.size() > 1) {
                for (int i = index.get(0); i < index.get(1); i++) {
                    phs_k.set(i, 0.5 - phs_k.get(i));
                }
                for (int i = index.get(1); i < phs_k.size(); i++) {
                    phs_k.set(i, 0.5 + phs_k.get(i));
                }
            } else {
                for (int i = index.get(0); i < phs_k.size(); i++) {
                    phs_k.set(i, 0.5 - phs_k.get(i));
                }
            }
            phs_k.forEach(phase -> phsx.add(phase + 0.5 * x_180.indexOf(subList)));
            index.clear();
            phs_k.clear();
        });

        y_180.forEach(subList -> {
            subList.forEach(item -> phs_k.add(Math.asin(item) / (2 * Math.PI)));
            index.add(0, 0);
            for (int i = 1; i < phs_k.size(); i++) {
                if (phs_k.get(i) < phs_k.get(i - 1) && (phs_k.get(i - 1) - phs_k.get(i) > 1e-10)) {
                    index.set(0, i);
                    break;
                }
            }
            if (y_180.indexOf(subList) == (y_180.size() - 1)) {
                for (int i = index.get(0) + 1; i < phs_k.size(); i++) {
                    if (phs_k.get(i) > phs_k.get(i - 1) && (phs_k.get(i) - phs_k.get(i - 1) > 1e-10)) {
                        index.add(1, i);
                        break;
                    }
                }
            }
            if (index.size() > 1) {
                for (int i = index.get(0); i < index.get(1); i++) {
                    phs_k.set(i, 0.5 - phs_k.get(i));
                }
                for (int i = index.get(1); i < phs_k.size(); i++) {
                    phs_k.set(i, 0.5 + phs_k.get(i));
                }
            } else {
                for (int i = index.get(0); i < phs_k.size(); i++) {
                    phs_k.set(i, 0.5 - phs_k.get(i));
                }
            }
            phs_k.forEach(phase -> phsy.add(phase + 0.5 * y_180.indexOf(subList)));
            index.clear();
            phs_k.clear();
        });

        HashMap<AcceleratorNode, R3> betatronPhase = new HashMap();

        for (int i = 1; i < trajectory.numStates(); i++) {
            if (objList.contains(sequence.getNodeWithId(stateElement.get(i).getHardwareNodeId()))) {
                betatronPhase.put(sequence.getNodeWithId(stateElement.get(i).getHardwareNodeId()), new R3(phsx.get(i), phsy.get(i), 0.0));
                //System.out.print(stateElement.get(i).getHardwareNodeId()+":"+sequence.getNodeWithId(stateElement.get(i).getHardwareNodeId()).getSDisplay()+","+phsx.get(i)+","+phsy.get(i)+"\n");
            }
        }

        return betatronPhase;

    }
}
