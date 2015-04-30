package se.lu.esss.ics.jels.matcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Formatter;
import java.util.Locale;

import se.lu.esss.ics.jels.JElsDemo;
import xal.extension.solver.AlgorithmSchedule;
import xal.extension.solver.AlgorithmScheduleListener;
import xal.extension.solver.Problem;
import xal.extension.solver.ScoreBoard;
import xal.extension.solver.SolveStopperFactory;
import xal.extension.solver.Solver;
import xal.extension.solver.Trial;
import xal.extension.solver.Variable;
import xal.extension.solver.hint.ExcursionHint;
import xal.extension.solver.market.AlgorithmStrategy;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.xml.LatticeXmlWriter;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;

public class Matcher extends Thread {
	private MatcherConfiguration matcherConfig;
	
	/*private InitialBeamParameters initialParameters = new InitialBeamParameters();	
	private Accelerator accelerator = loadAccelerator();*/
	//private OnlineModelEvaluator evaluator = new MinimiseOscillations(accelerator, initialParameters);
	private OnlineModelEvaluator evaluator; // = new PhaseAdvEvaluator(accelerator, initialParameters);
	
	
	private static Accelerator loadAccelerator() {
		/* Loading SMF model */				
		Accelerator accelerator = XMLDataManager.acceleratorWithUrlSpec(JElsDemo.class.getResource("main.xal").toString());
				
		if (accelerator == null)
		{			
			throw new Error("Accelerator is empty. Could not load the default accelerator.");
		} 			
		return accelerator;
	}
	
	static void saveLattice(Lattice lattice, String file) {		
		lattice.setAuthor(System.getProperty("user.name", "ESS"));		
		try {
			LatticeXmlWriter.writeXml(lattice, file);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
	}
	
	public Matcher(MatcherConfiguration matcherConfig) {
		this.matcherConfig = matcherConfig;
		try {
			evaluator = matcherConfig.getCriteria().getEvaluatorClass().getConstructor(Accelerator.class, EnvelopeProbe.class, InitialBeamParameters.class)
					.newInstance(loadAccelerator(), matcherConfig.getProbe(), matcherConfig.getInitialBeamParameters());
		} catch (IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		InitialBeamParameters initialParameters = matcherConfig.getInitialBeamParameters();
		for (Variable v : initialParameters.getVariables()) {
			System.out.printf("%s: %f\n", v.getName(), v.getInitialValue());
		}
		
		Problem problem = new Problem(evaluator.getObjectives(), initialParameters.getVariables(), evaluator);
		
		Solver solver = new Solver(SolveStopperFactory.maxElapsedTimeStopper(300));

		solver.getAlgorithmSchedule().addAlgorithmScheduleListener(new AlgorithmScheduleListener() {	
			@Override
			public void trialVetoed(AlgorithmSchedule algorithmSchedule, Trial trial) {
			}
			
			@Override
			public void trialScored(AlgorithmSchedule algorithmSchedule, Trial trial) {
				System.out.printf("score: %f algo: %s\n", trial.getSatisfaction(), trial.getAlgorithm().getClass());			
			}
			
			@Override
			public void strategyWillExecute(AlgorithmSchedule schedule,
					AlgorithmStrategy strategy, ScoreBoard scoreBoard) {
			}
			
			@Override
			public void strategyExecuted(AlgorithmSchedule schedule,
					AlgorithmStrategy strategy, ScoreBoard scoreBoard) {
			}
		});
		
		evaluator.printSolution("initial-sc", problem.generateInitialTrialPoint());
		
		solver.solve(problem);
		
		evaluator.printSolution("best-fm-5min-off", solver.getScoreBoard().getBestSolution().getTrialPoint());
	
	
		try {
			Formatter f1  = new Formatter("best-fm-5min-off.txt", "UTF8", Locale.ENGLISH);
			
			for (Variable v : initialParameters.getVariables()) {
				System.out.printf("%s: %f\n", v.getName(), solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(v));
				f1.format("%s: %f\n", v.getName(), solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(v));
			}
			
			System.out.printf("best score: %f\n", solver.getScoreBoard().getBestSolution().getSatisfaction());
			f1.format("best score: %f\n", solver.getScoreBoard().getBestSolution().getSatisfaction());
			f1.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}
	
	public static EnvelopeProbe setupOpenXALProbe() {
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
		
	
	public static void main(String args[]) throws ModelException
	{
		Matcher me = new Matcher(new MatcherConfiguration(setupOpenXALProbe()));
		me.run();
	}
}
