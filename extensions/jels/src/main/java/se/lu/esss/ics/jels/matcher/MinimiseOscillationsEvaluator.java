package se.lu.esss.ics.jels.matcher;

import java.util.List;

import xal.extension.solver.Objective;
import xal.extension.solver.SatisfactionCurve;
import xal.extension.solver.Trial;
import xal.model.ModelException;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.Twiss;

public class MinimiseOscillationsEvaluator extends OnlineModelEvaluator {	
	public MinimiseOscillationsEvaluator(Accelerator accelerator, InitialBeamParameters initialParameters) {
		super(accelerator, initialParameters);
		for (AcceleratorSeq seq : accelerator.getSequences()) {
			objectives.add(new MinimizeOscillationsObjective(seq));
			System.out.printf("%s %f %f\n",seq.getId(), seq.getPosition(), seq.getPosition()+seq.getLength());
		}
	}
	
	
	@Override
	public void evaluate(Trial trial) {	
		initialParameters.setupInitialParameters(probe, trial.getTrialPoint());
	
		try {
			scenario.run();
		} catch (ModelException e) {
			e.printStackTrace();
		}
		
		Trajectory<EnvelopeProbeState> trajectory = probe.getTrajectory();

		//System.out.println();
		for (Objective o : objectives) {
			MinimizeOscillationsObjective moo = (MinimizeOscillationsObjective)o;
			List<EnvelopeProbeState> ps = trajectory.statesInPositionRange(moo.seq.getPosition(), moo.seq.getPosition()+moo.seq.getLength());
			
			/*Twiss[] t0 = ((EnvelopeProbeState)ps[0]).twissParameters();
			Twiss[] t1 = ((EnvelopeProbeState)ps[1]).twissParameters();
			
			double minsup = 1e100, maxsup = -1e100;
			boolean foundsup = false;
			for (int i = 2; i < ps.length; i++) {
				Twiss[] t2 = ((EnvelopeProbeState)ps[i]).twissParameters();
				if ( t0[0].getEnvelopeRadius() < t1[0].getEnvelopeRadius() &&
						t1[0].getEnvelopeRadius() > t2[0].getEnvelopeRadius() ) { // we have local max
					double sup = t1[0].getEnvelopeRadius();
					if (sup < minsup) minsup = sup;
					if (sup > maxsup) maxsup = sup;
					foundsup = true;
				}
				t0=t1;
				t1=t2;
			}
			trial.setScore(moo, foundsup ? maxsup - minsup : 0);*/
			double minx = 1e100, maxx = -1e100;
			double miny = 1e100, maxy = -1e100;
			double minz = 1e100, maxz = -1e100;
			for (int i = 0; i < ps.size(); i++) {
				Twiss[] t = ((EnvelopeProbeState)ps.get(i)).twissParameters();
				if ( t[0].getEnvelopeRadius() < minx) minx = t[0].getEnvelopeRadius();
				if ( t[0].getEnvelopeRadius() > maxx) maxx = t[0].getEnvelopeRadius();
				if ( t[1].getEnvelopeRadius() < miny) miny = t[1].getEnvelopeRadius();
				if ( t[1].getEnvelopeRadius() > maxy) maxy = t[1].getEnvelopeRadius();
				if ( t[2].getEnvelopeRadius() < minz) minz = t[2].getEnvelopeRadius();
				if ( t[2].getEnvelopeRadius() > maxz) maxz = t[2].getEnvelopeRadius();
			}
			double score = 0.;
			if (Double.isNaN(minx) || Double.isNaN(maxx))
				score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope( -1e100, 0, 1000 );
			else
				score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope( minx - maxx, 0, 1000 );
			if (Double.isNaN(miny) || Double.isNaN(maxy))
				score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope( -1e100, 0, 1000 );
			else
				score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope( miny - maxy, 0, 1000 );
			if (Double.isNaN(minz) || Double.isNaN(maxz))
				score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope( -1e100, 0, 1000 );
			else
				score += SatisfactionCurve.sCurveSatisfactionWithCenterAndSlope( minz - maxz, 0, 1000 );
			trial.setScore(o, score);
			//System.out.printf("%s %f %f %f\n", moo.seq.getId(), minsup, maxsup, moo.satisfaction(maxsup - minsup));
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
			return score/3.;	
		}
	}
}
