package se.lu.esss.ics.jels.matcher;

import java.io.IOException;

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
import xal.model.xml.LatticeXmlWriter;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;

public class Matcher {
	private InitialBeamParameters initialParameters = new InitialBeamParameters();	
	private Accelerator accelerator = loadAccelerator();
	//private OnlineModelEvaluator evaluator = new MinimiseOscillations(accelerator, initialParameters);
	private OnlineModelEvaluator evaluator = new PhaseAdvEvaluator(accelerator, initialParameters);
	
	
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
	

	
	public void run()
	{	
		for (Variable v : initialParameters.getVariables()) {
			System.out.printf("%s: %f\n", v.getName(), v.getInitialValue());
		}
		
		Problem problem = new Problem(evaluator.getObjectives(), initialParameters.getVariables(), evaluator);
		
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
		
		evaluator.printSolution("initial-sc-ryo", problem.generateInitialTrialPoint());
		
		solver.solve(problem);
		
		evaluator.printSolution("best-ryo-5min", solver.getScoreBoard().getBestSolution().getTrialPoint());
	
		for (Variable v : initialParameters.getVariables()) {
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
