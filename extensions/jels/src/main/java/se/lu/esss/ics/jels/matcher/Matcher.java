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
import xal.extension.solver.market.AlgorithmStrategy;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.xml.LatticeXmlWriter;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;
import xal.tools.annotation.AProperty.NoEdit;
import xal.tools.annotation.AProperty.Units;

public class Matcher implements Runnable {
	
	/*private InitialBeamParameters initialParameters = new InitialBeamParameters();	
	private Accelerator accelerator = loadAccelerator();*/
	//private OnlineModelEvaluator evaluator = new MinimiseOscillations(accelerator, initialParameters);
	private OnlineModelEvaluator evaluator; // = new PhaseAdvEvaluator(accelerator, initialParameters);
	
	
	private ModelEvaluatorEnum criteria = ModelEvaluatorEnum.PhaseAdvance;
	
	private double timeLimit = 1.;
	
	private InitialBeamParameters initialBeamParameters = new InitialBeamParameters();
	
	private boolean showScore;
	private boolean showSimulation;
	
	private String outputInitialRun = null;
	private String outputBestRun = null;
	
	private Accelerator accelerator;
	
	public Matcher(Accelerator accelerator) {
		this.accelerator = accelerator;
	}

	public ModelEvaluatorEnum getCriteria() {
		return criteria;
	}
	
	public void setCriteria(ModelEvaluatorEnum criteria) {
		this.criteria = criteria;
	}	
	
	@Units("min")
	public double getTimeLimit() {
		return timeLimit;
	}
	
	public void setTimeLimit(double timeLimit) {
		this.timeLimit = timeLimit;
	}
	
	public InitialBeamParameters getInitialBeamParameters() {
		return initialBeamParameters;
	}
	
	public boolean isShowScore() {
		return showScore;
	}
	
	public void setShowScore(boolean showScore) {
		this.showScore = showScore;
	}
	
	public boolean isShowSimulation() {
		return showSimulation;
	}
	
	public void setShowSimulation(boolean showSimulation) {
		this.showSimulation = showSimulation;
	}
	
	@NoEdit
	public Accelerator getAccelerator()
	{
		return accelerator;
	}
	
	
	public static Accelerator loadAccelerator() {
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
	
	public void run()
	{
		try {
			evaluator = getCriteria().getEvaluatorClass().getConstructor(Matcher.class).newInstance(this);
		} catch (IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		InitialBeamParameters initialParameters = getInitialBeamParameters();
		for (Variable v : initialParameters.getVariables()) {
			System.out.printf("%s: %f\n", v.getName(), v.getInitialValue());
		}
		
		Problem problem = new Problem(evaluator.getObjectives(), initialParameters.getVariables(), evaluator);
		
		Solver solver = new Solver(SolveStopperFactory.maxElapsedTimeStopper(timeLimit*60));

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
		
		if (outputInitialRun != null) evaluator.printSolution(outputInitialRun, problem.generateInitialTrialPoint());
		
		solver.solve(problem);
		
		if (outputBestRun != null) {
			evaluator.printSolution(outputBestRun, solver.getScoreBoard().getBestSolution().getTrialPoint());
			try {
				Formatter f1  = new Formatter(outputBestRun+".txt", "UTF8", Locale.ENGLISH);
				
				for (Variable v : initialParameters.getVariables()) {					
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
	
		// set back final values to initial
		for (Variable v : initialParameters.getVariables()) {
			System.out.printf("%s: %f\n", v.getName(), solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(v));
			v.setInitialValue(solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(v));
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
		Matcher me = new Matcher(loadAccelerator());
		me.run();
	}
}


enum ModelEvaluatorEnum 
{
	PhaseAdvance(PhaseAdvEvaluator.class),
	MinimiseOscillations(MinimiseOscillationsEvaluator.class); 
	
	
	private  Class<? extends OnlineModelEvaluator> c;
	
	ModelEvaluatorEnum(Class<? extends OnlineModelEvaluator> c) {
		this.c = c;
	}
	
	public Class<? extends OnlineModelEvaluator> getEvaluatorClass()
	{
		return c;
	}
}

