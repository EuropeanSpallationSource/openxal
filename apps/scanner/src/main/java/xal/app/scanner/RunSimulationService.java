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
package xal.app.scanner;


import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.Probe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.CovarianceMatrix;


/**
 * The {@link Service} handling running the simulation.
 *
 * Borrowed from ModelBrowser
 *
 * @author claudiorosati
 */
public class RunSimulationService extends Service<Void> {

    private volatile AtomicReference<String> synchronizationMode = new AtomicReference<>(Scenario.SYNC_MODE_DESIGN);
    private Scenario model;

    public RunSimulationService() {
        model = null;
    }

    public void setSynchronizationMode( String synchronizationMode ) {
        this.synchronizationMode.set(synchronizationMode);
    }

    private AcceleratorSeq getSequence() {
        Accelerator acc = Model.getInstance().getAccelerator();
        //TODO: loop over variables in MainFunctions.pvWriteables, MainFunctions.pvReadbacks and return the (combo-)sequence that includes all
        return acc.getSequences().get(0);
    }

    public Scenario getModel() {
        return model;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            @SuppressWarnings( { "ValueOfIncrementOrDecrementUsed", "CallToThreadYield" } )
            protected Void call() throws Exception {

                AcceleratorSeq sequence = getSequence();

                updateTitle(sequence.getId());

                EnvTrackerAdapt envelopeTracker = AlgorithmFactory.createEnvTrackerAdapt(sequence);

                envelopeTracker.setMaxIterations(1000);
                envelopeTracker.setAccuracyOrder(1);
                envelopeTracker.setErrorTolerance(0.001);

                Probe<?> probe = ProbeFactory.getEnvelopeProbe(sequence, envelopeTracker);
                model = Scenario.newScenarioFor(sequence);

                model.setProbe(probe);
                model.setSynchronizationMode(synchronizationMode.get());
                model.resync();
                model.run();

                // Probably stuff below here can be taken out, used in MainFunctions instead
                probe = model.getProbe();

                Trajectory<? extends ProbeState<?>> trajectory = probe.getTrajectory();
                int numStates = trajectory.numStates();
                List<? extends ProbeState<?>> stateElement = trajectory.getStatesViaIndexer();
                ObservableList<XYChart.Data<Double, Double>> sigmaXData = FXCollections.observableArrayList();
                ObservableList<XYChart.Data<Double, Double>> sigmaYData = FXCollections.observableArrayList();
                ObservableList<XYChart.Data<Double, Double>> sigmaZData = FXCollections.observableArrayList();

                for ( int i = 0; i < numStates; i++ ) {

                    ProbeState<?> pState = stateElement.get(i);

                    if ( pState instanceof EnvelopeProbeState ) {

                        EnvelopeProbeState state = (EnvelopeProbeState) pState;
                        CovarianceMatrix matrix = state.getCovarianceMatrix();
                        double position = state.getPosition();

                        sigmaXData.add(new XYChart.Data<>(position, matrix.getSigmaX()));
                        sigmaYData.add(new XYChart.Data<>(position, matrix.getSigmaY()));
                        sigmaZData.add(new XYChart.Data<>(position, matrix.getSigmaZ()));

                    }

                }

                return null;

            }
        };
    }

}
