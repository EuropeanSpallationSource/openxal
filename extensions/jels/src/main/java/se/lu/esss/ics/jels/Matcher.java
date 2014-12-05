package se.lu.esss.ics.jels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import xal.extension.solver.AlgorithmSchedule;
import xal.extension.solver.AlgorithmScheduleListener;
import xal.extension.solver.Evaluator;
import xal.extension.solver.Objective;
import xal.extension.solver.Problem;
import xal.extension.solver.SatisfactionCurve;
import xal.extension.solver.ScoreBoard;
import xal.extension.solver.SolveStopperFactory;
import xal.extension.solver.Solver;
import xal.extension.solver.Trial;
import xal.extension.solver.TrialPoint;
import xal.extension.solver.Variable;
import xal.extension.solver.market.AlgorithmStrategy;
import xal.extension.solver.solutionjudge.SolutionJudge;
import xal.extension.solver.solutionjudge.SolutionJudgeListener;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.beam.Twiss;

public class Matcher implements Evaluator {
	
	final Variable 
		ax = new Variable("ax", -0.051805615, -0.07, -0.04),
		bx = new Variable("bx", 0.20954703, 0.1, 0.4),
		ex = new Variable("ex", 0.25288, 0.1, 0.4),
		ay = new Variable("ay", -0.30984478, -0.4, -0.2),
		by = new Variable("by", 0.37074849, 0.2, 0.4),
		ey = new Variable("ey",  0.251694, 0.2, 0.3),
		az = new Variable("az", -0.48130325, -0.6,-0.4),
		bz = new Variable("bz", 0.92564505, 0.8, 1),
		ez = new Variable("ez", 0.3615731, 0.3, 0.5),
		E = new Variable("E", 3.6217853e6, 3.5e6, 3.7e6);
	final List<Variable> variables = Arrays.asList(ax,bx,ex,ay,by,ey,az,bz,ez,E);
	
	final Accelerator accelerator = loadAccelerator();
		
	EnvelopeProbe probe = setupOpenXALProbe(); // OpenXAL probe & algorithm
	//EnvelopeProbe probe = setupElsProbe(); // ELS probe & algorithm
					
	// Setup of initial parameters
	Scenario scenario; 
	
	final List<Objective> objectives = new ArrayList<>();
	
	public Matcher()
	{
		try {
			scenario = Scenario.newScenarioFor(accelerator);
		} catch (ModelException e1) {
			e1.printStackTrace();
		}	
		
		scenario.setProbe(probe);			
		
		// Setting up synchronization mode
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		try {
			scenario.resync();
		} catch (SynchronizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	private static Accelerator loadAccelerator() {
		/* Loading SMF model */				
		Accelerator accelerator = XMLDataManager.acceleratorWithUrlSpec(JElsDemo.class.getResource("main.xal").toString());
				
		if (accelerator == null)
		{			
			throw new Error("Accelerator is empty. Could not load the default accelerator.");
		} 			
		return accelerator;
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
		
	private EnvelopeProbe setupOpenXALProbe() {
		EnvelopeTracker envelopeTracker = new EnvelopeTracker();			
		envelopeTracker.setRfGapPhaseCalculation(true);
		envelopeTracker.setUseSpacecharge(true);
		envelopeTracker.setEmittanceGrowth(false);
		envelopeTracker.setStepSize(0.1);
		envelopeTracker.setProbeUpdatePolicy(Tracker.UPDATE_EXIT);
		
		EnvelopeProbe envelopeProbe = new EnvelopeProbe();
		envelopeProbe.setAlgorithm(envelopeTracker);		
		
		return envelopeProbe;
	}
		
	@Override
	public void evaluate(Trial trial) {	
		probe.reset();
		setupInitialParameters(probe, trial.getTrialPoint());
	
		try {
			scenario.run();
		} catch (ModelException e) {
			e.printStackTrace();
		}
		
		Trajectory trajectory = probe.getTrajectory();

		//System.out.println();
		for (Objective o : objectives) {
			MinimizeOscillationsObjective moo = (MinimizeOscillationsObjective)o;
			ProbeState[] ps = trajectory.statesInPositionRange(moo.seq.getPosition(), moo.seq.getPosition()+moo.seq.getLength());
			
			Twiss[] t0 = ((EnvelopeProbeState)ps[0]).twissParameters();
			Twiss[] t1 = ((EnvelopeProbeState)ps[1]).twissParameters();
			
			/*double minsup = 1e100, maxsup = -1e100;
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
			for (int i = 0; i < ps.length; i++) {
				Twiss[] t = ((EnvelopeProbeState)ps[i]).twissParameters();
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
	
	public void printSolution(String file, TrialPoint trial) {	
		probe.reset();
		setupInitialParameters(probe, trial);
	
		try {
			scenario.run();
		} catch (ModelException e) {
			e.printStackTrace();
		}
		
		try {
			Formatter f1  = new Formatter(file+".dat", "UTF8", Locale.ENGLISH);
			Formatter f2  = new Formatter(file+".max.dat", "UTF8", Locale.ENGLISH);
			
			Trajectory trajectory = probe.getTrajectory();

			//System.out.println();
			Iterator<ProbeState> i = trajectory.stateIterator();
			Twiss[] t0 = ((EnvelopeProbeState)i.next()).twissParameters();
			Twiss[] t1 = ((EnvelopeProbeState)i.next()).twissParameters();
			
			while (i.hasNext()) {
				ProbeState ps = i.next();
				
				Twiss[] t2 = ((EnvelopeProbeState)ps).twissParameters();
				if ( t0[0].getEnvelopeRadius() < t1[0].getEnvelopeRadius() &&
						t1[0].getEnvelopeRadius() > t2[0].getEnvelopeRadius() ) { // we have local max
				    f2.format("%E %E %E %E %E %E %E %E %E\n", ps.getPosition(), ps.getGamma()-1, 		
								t2[0].getEnvelopeRadius(),
								Math.sqrt(t2[0].getGamma()*t2[0].getEmittance()),
								t2[1].getEnvelopeRadius(),
								Math.sqrt(t2[1].getGamma()*t2[1].getEmittance()),
								t2[2].getEnvelopeRadius()/ps.getGamma(),
								Math.sqrt(t2[2].getGamma()*t2[2].getEmittance())*ps.getGamma(),
								Math.sqrt(t2[2].getGamma()*t2[2].getEmittance())/ps.getGamma());			
				}
				t0=t1;
				t1=t2;
				
			    f1.format("%E %E %E %E %E %E %E %E %E\n", ps.getPosition(), ps.getGamma()-1, 		
							t2[0].getEnvelopeRadius(),
							Math.sqrt(t2[0].getGamma()*t2[0].getEmittance()),
							t2[1].getEnvelopeRadius(),
							Math.sqrt(t2[1].getGamma()*t2[1].getEmittance()),
							t2[2].getEnvelopeRadius()/ps.getGamma(),
							Math.sqrt(t2[2].getGamma()*t2[2].getEmittance())*ps.getGamma(),
							Math.sqrt(t2[2].getGamma()*t2[2].getEmittance())/ps.getGamma());
			}
			
			f1.close();
			f2.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	
	
	private void setupInitialParameters(EnvelopeProbe probe2,
			TrialPoint trialPoint) {
		double Ax = trialPoint.getValue(ax),
				Bx = trialPoint.getValue(bx),
				Ex = trialPoint.getValue(ex),
				Ay = trialPoint.getValue(ay),
				By = trialPoint.getValue(by),
				Ey = trialPoint.getValue(ey),
				Az = trialPoint.getValue(az),
				Bz = trialPoint.getValue(bz),
				Ez = trialPoint.getValue(ez),
				e0 = trialPoint.getValue(E);
		
		probe.setSpeciesCharge(1);
		probe.setSpeciesRestEnergy(9.3827202900E8);
		//elsProbe.setSpeciesRestEnergy(9.38272013e8);	
		probe.setKineticEnergy(e0);//energy
		probe.setPosition(0.0);
		probe.setTime(0.0);		
				
		double beta_gamma = probe.getBeta() * probe.getGamma();
	
		
		probe.initFromTwiss(new Twiss[]{new Twiss(Ax,Bx,Ex*1e-6 / beta_gamma),
										  new Twiss(Ay,By,Ey*1e-6 / beta_gamma),
										  new Twiss(Az,Bz,Ez*1e-6 / beta_gamma)});
		probe.setBeamCurrent(62.5e-3);
		probe.setBunchFrequency(352.21e6); 	
		
	}

	
	public void run()
	{	
		for (Variable v : variables) {
			System.out.printf("%s: %f\n", v.getName(), v.getInitialValue());
		}
		
		for (AcceleratorSeq seq : accelerator.getSequences()) {
			objectives.add(new MinimizeOscillationsObjective(seq));
			System.out.printf("%s %f %f\n",seq.getId(), seq.getPosition(), seq.getPosition()+seq.getLength());
		}
		
		Problem problem = new Problem(objectives, variables, this);
		
		Solver solver = new Solver(SolveStopperFactory.maxElapsedTimeStopper(300));

		solver.getAlgorithmSchedule().addAlgorithmScheduleListener(new AlgorithmScheduleListener() {
			
			@Override
			public void trialVetoed(AlgorithmSchedule algorithmSchedule, Trial trial) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void trialScored(AlgorithmSchedule algorithmSchedule, Trial trial) {
				System.out.printf("score: %f\n", trial.getSatisfaction());
				
			}
			
			@Override
			public void strategyWillExecute(AlgorithmSchedule schedule,
					AlgorithmStrategy strategy, ScoreBoard scoreBoard) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void strategyExecuted(AlgorithmSchedule schedule,
					AlgorithmStrategy strategy, ScoreBoard scoreBoard) {
				// TODO Auto-generated method stub
				
			}
		});
		
		printSolution("initial-sc-ryo", problem.generateInitialTrialPoint());
		
		solver.solve(problem);
		
		printSolution("best-ryo-5min", solver.getScoreBoard().getBestSolution().getTrialPoint());
	
		for (Variable v : variables) {
			System.out.printf("%s: %f\n", v.getName(), solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(v));
		}
		System.out.printf("best score: %f\n", solver.getScoreBoard().getBestSolution().getSatisfaction());		
	}
	
	public static void main(String args[]) throws ModelException
	{
		Matcher me = new Matcher();
		me.run();
	}
}
