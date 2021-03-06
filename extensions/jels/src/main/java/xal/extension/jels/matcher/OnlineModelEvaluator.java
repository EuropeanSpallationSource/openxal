package xal.extension.jels.matcher;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.logging.Level;

import xal.extension.solver.Evaluator;
import xal.extension.solver.Objective;
import xal.extension.solver.TrialPoint;
import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.tools.beam.Twiss;

public abstract class OnlineModelEvaluator implements Evaluator {

    protected Matcher matcher;

    // Setup of initial parameters
    protected Scenario scenario;
    protected List<Objective> objectives = new ArrayList<>();

    protected List<EvaluationListener> evaluationListeners = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(OnlineModelEvaluator.class.getName());

    public OnlineModelEvaluator(Matcher matcher) {
        this.matcher = matcher;

        try {
            scenario = Scenario.newScenarioFor(matcher.getAccelerator());
        } catch (ModelException e1) {
            LOGGER.log(Level.SEVERE, "Model exception.", e1);
        }

        // Setting up synchronization mode
        scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
        try {
            scenario.resync();
        } catch (SynchronizationException e) {
            LOGGER.log(Level.SEVERE, "Synchronization Exception.", e);
        }
    }

    public void printSolution(String file, TrialPoint trial) {

        EnvelopeProbe probe = matcher.getInitialBeamParameters().getProbe(trial);
        scenario.setProbe(probe);

        try {
            scenario.run();
        } catch (ModelException e) {
            LOGGER.log(Level.SEVERE, "Model exception.", e);
        }

        try {
            Formatter f1 = new Formatter(file + ".dat", "UTF8", Locale.ENGLISH);
            Formatter f2 = new Formatter(file + ".phi.dat", "UTF8", Locale.ENGLISH);

            Trajectory<EnvelopeProbeState> trajectory = probe.getTrajectory();

            Iterator<EnvelopeProbeState> i = trajectory.stateIterator();

            double[] phix = new double[3];
            double pos0 = 0;
            double[] phil = null;
            double posl = 0;

            while (i.hasNext()) {
                EnvelopeProbeState ps = i.next();

                Twiss[] t2 = ((EnvelopeProbeState) ps).twissParameters();

                for (int k = 0; k < 3; k++) {
                    phix[k] += (ps.getPosition() - pos0) / (t2[k].getBeta());
                }
                pos0 = ps.getPosition();

                f1.format("%E %E %E %E %E %E %E %E %E%n", ps.getPosition(), ps.getGamma() - 1,
                        t2[0].getEnvelopeRadius(),
                        Math.sqrt(t2[0].getGamma() * t2[0].getEmittance()),
                        t2[1].getEnvelopeRadius(),
                        Math.sqrt(t2[1].getGamma() * t2[1].getEmittance()),
                        t2[2].getEnvelopeRadius() / ps.getGamma(),
                        Math.sqrt(t2[2].getGamma() * t2[2].getEmittance()) * ps.getGamma(),
                        Math.sqrt(t2[2].getGamma() * t2[2].getEmittance()) / ps.getGamma());

                if (ps.getElementId().toUpperCase().startsWith("LATTICE-POINT")) {
                    if (phil != null) {
                        f2.format("%E %E %E %E%n", ps.getPosition(),
                                (phix[0] - phil[0]) / (ps.getPosition() - posl) * 180. / Math.PI,
                                (phix[1] - phil[1]) / (ps.getPosition() - posl) * 180. / Math.PI,
                                (phix[2] - phil[2]) / (ps.getPosition() - posl) * 180. / Math.PI);
                    }
                    phil = phix.clone();
                    posl = ps.getPosition();
                }
                if (ps.getElementId().toUpperCase().startsWith("LATTICE-END")) {
                    phil = null;
                }
            }

            f1.close();
            f2.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Problem writing the file.", e);
        }
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public void addEvaluationListener(EvaluationListener l) {
        evaluationListeners.add(l);
    }

    protected void fireEvaluationListeners(Trajectory<EnvelopeProbeState> t) {
        for (EvaluationListener l : evaluationListeners) {
            l.onEvaluation(t);
        }
    }

    public void removeEvaluationListener(EvaluationListener l) {
        evaluationListeners.remove(l);
    }
}

interface EvaluationListener {

    void onEvaluation(Trajectory<EnvelopeProbeState> t);
}
