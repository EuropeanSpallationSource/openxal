package xal.extension.jels.matcher;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.extension.solver.Objective;
import xal.extension.solver.SatisfactionCurve;
import xal.extension.solver.Trial;
import xal.model.ModelException;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.Twiss;

public class MinimiseOscillationsEvaluator extends OnlineModelEvaluator {

    private static final Logger LOGGER = Logger.getLogger(MinimiseOscillationsEvaluator.class.getName());

    public MinimiseOscillationsEvaluator(Matcher matcher) {
        super(matcher);
        for (AcceleratorSeq seq : matcher.getAccelerator().getSequences()) {
            objectives.add(new MinimizeOscillationsObjective(seq));
            System.out.printf("%s %f %f%n", seq.getId(), seq.getPosition(), seq.getPosition() + seq.getLength());
        }
    }

    @Override
    public void evaluate(Trial trial) {
        EnvelopeProbe probe = matcher.getInitialBeamParameters().getProbe(trial.getTrialPoint());

        scenario.setProbe(probe);
        try {
            scenario.run();
        } catch (ModelException e) {
            LOGGER.log(Level.SEVERE, "Model exception.", e);
        }

        Trajectory<EnvelopeProbeState> trajectory = probe.getTrajectory();

        for (Objective o : objectives) {
            MinimizeOscillationsObjective moo = (MinimizeOscillationsObjective) o;
            List<EnvelopeProbeState> ps = trajectory.statesInPositionRange(moo.seq.getPosition(), moo.seq.getPosition() + moo.seq.getLength());

            double minx = 1e100, maxx = -1e100;
            double miny = 1e100, maxy = -1e100;
            double minz = 1e100, maxz = -1e100;
            for (int i = 0; i < ps.size(); i++) {
                Twiss[] t = ((EnvelopeProbeState) ps.get(i)).twissParameters();
                if (t[0].getEnvelopeRadius() < minx) {
                    minx = t[0].getEnvelopeRadius();
                }
                if (t[0].getEnvelopeRadius() > maxx) {
                    maxx = t[0].getEnvelopeRadius();
                }
                if (t[1].getEnvelopeRadius() < miny) {
                    miny = t[1].getEnvelopeRadius();
                }
                if (t[1].getEnvelopeRadius() > maxy) {
                    maxy = t[1].getEnvelopeRadius();
                }
                if (t[2].getEnvelopeRadius() < minz) {
                    minz = t[2].getEnvelopeRadius();
                }
                if (t[2].getEnvelopeRadius() > maxz) {
                    maxz = t[2].getEnvelopeRadius();
                }
            }
            double score = 0.;
            if (Double.isNaN(minx) || Double.isNaN(maxx)) {
                score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope(-1e100, 0, 1000);
            } else {
                score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope(minx - maxx, 0, 1000);
            }
            if (Double.isNaN(miny) || Double.isNaN(maxy)) {
                score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope(-1e100, 0, 1000);
            } else {
                score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope(miny - maxy, 0, 1000);
            }
            if (Double.isNaN(minz) || Double.isNaN(maxz)) {
                score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope(-1e100, 0, 1000);
            } else {
                score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope(minz - maxz, 0, 1000);
            }
            trial.setScore(o, score);
        }
    }

    public static class MinimizeOscillationsObjective extends Objective {

        public AcceleratorSeq seq;

        public MinimizeOscillationsObjective(AcceleratorSeq seq) {
            super(seq.getId());
            this.seq = seq;
        }

        @Override
        public double satisfaction(double score) {
            return score / 3.;
        }
    }
}
