package se.lu.esss.ics.jels;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xal.extension.solver.Evaluator;
import xal.extension.solver.Objective;
import xal.extension.solver.Problem;
import xal.extension.solver.SolveStopperFactory;
import xal.extension.solver.Solver;
import xal.extension.solver.Trial;
import xal.extension.solver.Variable;
import xal.extension.solver.hint.InitialDomain;
import xal.extension.solver.solutionjudge.WorstObjectiveBiasedJudge;

public class SolverDemo {

	public static void main(String args[])
	{
		final Objective linear = new Objective("linear") {
			
			@Override
			public double satisfaction(double value) {
				return 1-value;
			}
		};
		
		final Variable x = new Variable("x", 0.5, 0, 1), 
				y = new Variable("y", 0.5, 0, 1);
		
		Evaluator evaluator = new Evaluator() {
			@Override
			public void evaluate(Trial trial) {	
				double x1 = trial.getTrialPoint().getValue(x);
				double y1 = trial.getTrialPoint().getValue(y);
				double score = Math.pow(x1-0.3,2)+Math.pow(y1-0.7,2);
				trial.setScore( linear, score );
			}
		};
		
		Problem problem = new Problem(Arrays.asList(linear), Arrays.asList(x,y), evaluator);
		
		Solver solver = new Solver(SolveStopperFactory.maxElapsedTimeStopper(5));
		
		solver.solve(problem);
		
		System.out.printf("x: %f\n", solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(x));
		System.out.printf("y: %f\n", solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(y));
	}
}
