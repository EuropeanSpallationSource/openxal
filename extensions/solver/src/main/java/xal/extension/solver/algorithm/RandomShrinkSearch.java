/*
 *  RandomShrinkSearch.java
 *
 *  Created Wednesday August 04, 2004 11:37pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver.algorithm;

import xal.extension.solver.*;
import xal.extension.solver.hint.*;
import xal.extension.solver.solutionjudge.*;

import java.util.*;


/**
 * RandomSearchAlgorithm looks for points bounded by the specified variable limits. Every
 * iteration a subset of variables are randomly chosen to be changed. The value chosen for each
 * variable is randomly selected from a uniform distribution within a window for that variable.
 * The window begins as the full limit range allowed by the variable. Whenever a better
 * solution is found, the window changes so that it is three times wider than the distance
 * between the last best point and the new best point. The window gets centered around the new
 * best points. Then the window gets clipped as necessary to satisfy the variable limits. Note
 * that the window may grow or shrink depending on the distance between successive top points.
 * As the optimal solution is approached, the window should tend to shrink around the optimal
 * point. Note that each variable has its own window and the window automatically adjusts to
 * the variable according to the progress.
 *
 * @author   t6p
 */
public class RandomShrinkSearch extends SearchAlgorithm {
	/** The current best point. */
	protected TrialPoint bestPoint;
	
	/** The active search technique (random or shrink). */
	protected Searcher searcher;

    /** keeps track of internal best Satisfaction */
    protected double bestSatisfaction;

	/** keeps track if the last run is being executed */
	protected boolean isLastEvaluation = false;

	/** Empty constructor. */
	public RandomShrinkSearch() {
	}

	
	/**
	 * Set the specified problem to solve.  Override the inherited method to look for hints.
	 * @param problem the problem to solve
	 */
        @Override
	public void setProblem( final Problem problem ) {
		super.setProblem( problem );
	}
	

	/**
	 * Get the label for this search algorithm.
	 * @return   The label for this algorithm
	 */
        @Override
	public String getLabel() {
		return "Random Shrink Search";
	}


	/** reset for searching from scratch; forget history */
        @Override
	public void reset() {
		searcher = new ComboSearcher();
		resetBestPoint();
        isLastEvaluation = false;
	}


	/** Reset the trial point's variables to their starting values.  */
	protected void resetBestPoint() {
		final List<Variable> variables = problem.getVariables();
		final MutableTrialPoint trialPoint = new MutableTrialPoint( variables.size() );
        
        for ( final Variable variable : variables ) {
			final double value = variable.getInitialValue();
			trialPoint.setValue( variable, value );
		}

		bestPoint = trialPoint.getTrialPoint();
        bestSatisfaction = 0;
	}
	
	
	/**
	 * Calculate the next few trial points.
	 * @param algorithmSchedule the algorithm run to perform the evaluation
	 */
        @Override
	public void performRun(AlgorithmSchedule algorithmSchedule) {
		try {
			isLastEvaluation = false;		// reset this flag for the new run
            int runCount = getEvaluationsLeft();
            while( runCount > 0 && !algorithmSchedule.shouldStop() ){
                if(runCount == 1){
                    isLastEvaluation = true;
                }
                evaluateTrialPoint( nextTrialPoint() );
                runCount = getEvaluationsLeft();
            }
			isLastEvaluation = false;		// clear the flag to avoid side effects
		}
		catch ( RunTerminationException exception ) {}
	}
	

	/**
	 * Calculate and get the next trial point to evaluate.
	 * @return   The next trial point to evaluate.
	 */
	public TrialPoint nextTrialPoint() {
		return searcher.nextTrialPoint();
	}
	

	/**
	 * Get the rating for this algorithm which in an integer between 0 and 10 and indicates how well this algorithm
	 * performs on global searches.
	 * @return   The global search rating for this algorithm.
	 */
        @Override
	public int globalRating() {
		return 8;
	}


	/**
	 * Get the rating for this algorithm which in an integer between 0 and 10 and indicates how well this algorithm
	 * performs on local searches.
	 * @return   The local search rating for this algorithm.
	 */
        @Override
	public int localRating() {
		return 5;
	}


	/**
	 * Handle a message that an algorithm is available.
	 * @param source      The source of the available algorithm.
	 */
	public void algorithmAvailable( final SearchAlgorithm source ) { }


	/**
	 * Handle a message that an algorithm is not available.
	 * @param source      The source of the available algorithm.
	 */
	public void algorithmUnavailable( final SearchAlgorithm source ) { }


    /**
     * Handle a message that a trial has been scored.
     * @param schedule              Description of the Parameter
     * @param trial                 Description of the Parameter
     *
     * This is for operating this algorithm completely independently of any other algorithm
     */
        @Override
    public void trialScored( final AlgorithmSchedule schedule, final Trial trial ) {
        if(bestPoint == null){
            bestPoint = trial.getTrialPoint();
            bestSatisfaction = trial.getSatisfaction();
        }
        
        double satisfaction = trial.getSatisfaction();
		// if the source algorithm is this algorithm or the initial algorithm then update
		// the internal search window if we have a better solution (initial algorithm is always best
		// since it is run exactly once and is our starting point).
        if ( trial.getAlgorithm() == this || trial.getAlgorithm() instanceof InitialAlgorithm ) {
            if ( satisfaction >= bestSatisfaction ) {
                internalNewOptimalSolution( trial.getTrialPoint(), satisfaction );
            }

			// wait until the last evaluation to avoid being trapped in a local extremum by another algorithm
			// during the last evaluation of this algorithm, check whether we still lag the best solution significantly
			if ( isLastEvaluation && trial.getAlgorithm() == this ) {
				// check the score board for the best solution found so far by any algorithm
				// adopt that solution if that solution's satisfaction is better than this algorithm's best satisfaction by more than 25% of the possible remaining satisfaction to achieve
				final Trial bestSolution = schedule.getScoreBoard().getBestSolution();
				if ( bestSolution != null && bestSolution.getSatisfaction() > (bestSatisfaction + 0.25 * (1 - bestSatisfaction))  ) {
					searcher.shouldShift();
					internalNewOptimalSolution( bestSolution.getTrialPoint(), bestSolution.getSatisfaction() );
				}
			}
        }
    }


	/**
	 * Handle a message that a new optimal solution has been found.
	 * @param source     The source of the new optimal solution.
	 * @param solutions  The list of solutions.
	 * @param solution   The new optimal solution.
	 */
        @Override
    public void foundNewOptimalSolution( final SolutionJudge source, final List<Trial> solutions, final Trial solution ) {}


    /**
     * This is going to keep track of its own solutions. 
     * Basically it will run alongside other programs if the option is chosen, but
     * it will not changed based on their results
     */
    private void internalNewOptimalSolution( final TrialPoint newPoint, final double satisfaction ){
        bestSatisfaction = satisfaction;
        TrialPoint oldPoint = bestPoint;
        searcher.newTopSolution( oldPoint, newPoint );
        bestPoint = newPoint;
    }

    /**
     * Interface for classes that search for solutions.
     */
    protected interface Searcher {
	/** reset for searching from scratch; forget history */
        public void reset();
        
        /** turns shouldShift on */
        public void shouldShift();

        /**
         * An event indicating that a new solution has been found which is
         * better than the previous best solution according to the score given
         * by the evaluator.
         *
         * @param oldPoint The old best point.
         * @param newPoint The new best point.
         */
        public void newTopSolution(final TrialPoint oldPoint, final TrialPoint newPoint);

        /**
         * Get the next trial point.
         *
         * @return the next trial point.
         */
        public TrialPoint nextTrialPoint();
    }


	/** A searcher that performs a simple random search in the entire search space.  */
	protected class RandomSearcher implements Searcher {
		/** Description of the Field */
		protected final int NUM_VARIABLES;

		/** Description of the Field */
		protected Random randomGenerator;
        
		/** Description of the Field */
		protected double changeProbabilityBase;
        
		/** Map of values keyed by variable */
		protected Map<Variable,Number> values;

        
        /** tells the ShrinkSearcher to only shift the window */
        public boolean shouldShift = false;
        
        /** turns shouldShift on */
                @Override
        public void shouldShift(){};
        

		/** Constructor  */
		public RandomSearcher() {
			NUM_VARIABLES = problem.getVariables().size();
			changeProbabilityBase = 1 / (double)NUM_VARIABLES;
			values = new HashMap<>( NUM_VARIABLES );
			randomGenerator = new Random( 0 );
		}


		/** reset for searching from scratch; forget history */
                @Override
		public void reset() { }


		/**
		 * An event indicating that a new solution has been found which is better than the previous
		 * best solution according to the score given by the evaluator.
		 *
		 * @param oldPoint  The old best point.
		 * @param newPoint  The new best point.
		 */
                @Override
		public void newTopSolution( final TrialPoint oldPoint, final TrialPoint newPoint ) { }
		

		/**
		 * Get the next trial point. Simply returns nextPoint().
		 *
		 * @return   the next trial point.
		 */
                @Override
		public TrialPoint nextTrialPoint() {
			return nextPoint();
		}


		/**
		 * Get the next trial point.
		 *
		 * @return   the next trial point.
		 */
		public TrialPoint nextPoint() {
			values.putAll( bestPoint.getValueMap() );
			return nextPoint( 1 );
		}


		/**
		 * Get the next trial point given the expected number of variables to change. Randomly pick
		 * the number of variables to vary by weighing it based on the average number we expect to
		 * vary. If no variables get selected to change then we pick the number of variables to
		 * change randomly from 1 to the total number of variables available. For each variable that
		 * was selected to change, we randomly select a value within the target domain for the
		 * variable.
		 *
		 * @param expectedNumToChange  The average number of variables that we expect to change.
		 * @return                     the next trial point.
		 */
		public TrialPoint nextPoint( int expectedNumToChange ) {
			boolean elementChanged = false;
			double changeProbability = expectedNumToChange * changeProbabilityBase;

			for ( final Variable variable : problem.getVariables() ) {
				boolean shouldChange = ( randomGenerator.nextDouble() <= changeProbability );
				if ( shouldChange ) {
					elementChanged = true;
					double value = proposeValue( variable );
					values.put( variable, new Double( value ) );
				}
			}

			if ( elementChanged ) {
				return new TrialPoint( values );
			}
			else {
				expectedNumToChange = randomGenerator.nextInt( NUM_VARIABLES ) + 1;
				return nextPoint( expectedNumToChange );
			}
		}


		/**
		 * Propose a new value for the variable by selecting a random value in the variable's search
		 * range.
		 *
		 * @param variable  the variable for which to propose a new value
		 * @return          the new value to propose for the variable
		 */
		protected double proposeValue( final Variable variable ) {
			double lowerLimit = variable.getLowerLimit();
			double upperLimit = variable.getUpperLimit();
			double rawValue = randomGenerator.nextDouble();

			return lowerLimit + rawValue * ( upperLimit - lowerLimit );
		}
	}


	/**
	 * ShrinkSearcher searches for the next trial point by adjusting the search domain per
	 * variable depending on how much a variable has changed between the best solutions found so
	 * far. As the variables converge on a solution, the search space shrinks. The search space
	 * may also increase if a variable is not converging as we get closer to an optimal solution.
	 */
	protected class ShrinkSearcher extends RandomSearcher {
		/** Description of the Field */
		protected Map<Variable,VariableWindow> variableWindows;
        
        /** tells the ShrinkSearcher to only shift the window */
        public boolean shouldShift = false;
        
        /** turns shouldShift on */
        public void shouldShsift(){
            shouldShift = true;
        }


		/** Constructor  */
		public ShrinkSearcher() {
			reset();
		}


		/** reset for searching from scratch; forget history */
                @Override
		public void reset() {
			buildWindows();
		}


		/**
		 * Get the variable's search window for the specified variable.
		 *
		 * @param variable  the variable for which to get the search window
		 * @return          the variable's search window
		 */
		public VariableWindow getSearchWindow( final Variable variable ) {
			return variableWindows.get( variable );
		}
		
		
		/**
		 * Print variables' search windows.
		 */
		public void printVariableSearchWindows( final String message ) {
			System.out.println( "********* Printing variable search windows *********" );
			System.out.println( message );

			for ( final Variable variable : problem.getVariables() ) {
				VariableWindow window = getSearchWindow( variable );
				System.out.println( variable.getName() + " lower limit: " + window.getLowerLimit() );
				System.out.println( variable.getName() + " upper limit: " + window.getUpperLimit() );
			}
			
			System.out.println( "****************************************************" );
		}


		/**
		 * An event indicating that a new solution has been found which is better than the previous
		 * best solution according to the score given by the evaluator.
		 * @param oldPoint  The old best point.
		 * @param newPoint  The new best point.
		 */
                @Override
		public void newTopSolution( final TrialPoint oldPoint, final TrialPoint newPoint ) {
            if(shouldShift){
                shiftWindow(newPoint);
            }
            else{
                for ( final Variable variable : problem.getVariables() ) {
                    final double newValue = newPoint.getValue( variable );
                    final double oldValue = oldPoint.getValue( variable );
                    
                    if ( oldValue == newValue ) {
                        continue;
                    }
                    
                    final double lowerLimit = variable.getLowerLimit();
                    final double upperLimit = variable.getUpperLimit();
                    
                    final double change = newValue - oldValue;
                    final double lowerRange = 3 * Math.abs( change );
                    final double upperRange = 3 * Math.abs( change );
                    
                    VariableWindow window = getSearchWindow( variable );
                    window.setLowerLimit( Math.max( lowerLimit, newValue - lowerRange ) );
                    window.setUpperLimit( Math.min( upperLimit, newValue + upperRange ) );
                }
            }
            shouldShift = false;
		}
        
        public void shiftWindow(TrialPoint newPoint){
            for(final Variable variable: problem.getVariables()){
                double newValue = newPoint.getValue(variable);
                
                VariableWindow window = getSearchWindow(variable);
                double windowLower = window.getLowerLimit();
                double windowUpper = window.getUpperLimit();
                
                double windowHalfRange = (windowUpper - windowLower)/2.0;
                
                window.setLowerLimit(Math.max(variable.getLowerLimit(), newValue - windowHalfRange));
                window.setUpperLimit(Math.min(variable.getUpperLimit(), newValue + windowHalfRange));
            }
        }


		/**
		 * Build the new bounds based upon the user specified domain and the search algorithm's
		 * search space per variable. The search windows become the intersection of the user
		 * specified search domain and the algorithm's search space.
		 */
		protected void buildWindows() {
			final InitialDomain domainHint = (InitialDomain)problem.getHint( InitialDomain.TYPE );
			final InitialDelta deltaHint = (InitialDelta)problem.getHint( InitialDelta.TYPE );
			DomainHint hint;
			
			if ( deltaHint != null ) {
				hint = deltaHint;
			}
			else if ( domainHint != null ) {
				hint = domainHint;
			}
			else {
				hint = new InitialDelta();
			}
			
			final List<Variable> variables = problem.getVariables();
			variableWindows = new HashMap<>( variables.size() );
            
            for ( final Variable variable : variables ) {
				final double[] limits = hint.getRange( variable );				
				VariableWindow window = new VariableWindow( limits[DomainHint.LOWER_IND], limits[DomainHint.UPPER_IND] );
				variableWindows.put( variable, window );
			}
		}


		/**
		 * Propose a new value for the variable by selecting a random value from the variable's
		 * shrunken search space.
		 * @param variable  the variable for which to propose a new value
		 * @return          the new value to propose for the variable
		 */
                @Override
		protected double proposeValue( final Variable variable ) {
			double rawValue = randomGenerator.nextDouble();
			final VariableWindow window = getSearchWindow( variable );
			double lowerLimit = window.getLowerLimit();
			double upperLimit = window.getUpperLimit();

			return lowerLimit + rawValue * (upperLimit - lowerLimit);
		}
	}


	/** Use a combination of search engines to search for the best solution.  */
	public class ComboSearcher extends RandomSearcher {
		/** Description of the Field */
		protected ShrinkSearcher shrinkSearcher;
		/** Description of the Field */
		protected RandomSearcher randomSearcher;
		/** Description of the Field */
		protected static final double SHRINK_THRESHOLD = 0.9;
        
        /** tells the ShrinkSearcher to only shift the window */
        public boolean shouldShift = false;
        
        /** turns shouldShift on */
        public void shouldShift(){
            shouldShift = true;
        }


		/** Constructor  */
		public ComboSearcher() {
			shrinkSearcher = new ShrinkSearcher();
			randomSearcher = new RandomSearcher();
		}


		/** reset for searching from scratch; forget history */
                @Override
		public void reset() {
			shrinkSearcher.reset();
		}


		/**
		 * An event indicating that a new solution has been found which is better than the previous
		 * best solution according to the score given by the evaluator.
		 *
		 * @param oldPoint  The old best point.
		 * @param newPoint  The new best point.
		 */
                @Override
		public void newTopSolution( final TrialPoint oldPoint, final TrialPoint newPoint ) {
            if(shouldShift){
                shrinkSearcher.shouldShift();
            }
            shrinkSearcher.newTopSolution( oldPoint, newPoint );
            shouldShift = false;
		}


		/**
		 * Propose a new value for the variable by picking a search engine to propose a new value.
		 *
		 * @param variable  the variable for which to propose a new value
		 * @return          the new value to propose for the variable
		 */
                @Override
		protected double proposeValue( final Variable variable ) {
			double selection = randomGenerator.nextDouble();
			if ( selection < SHRINK_THRESHOLD ) {
				return shrinkSearcher.proposeValue( variable );
			}
			else {
				return randomSearcher.proposeValue( variable );
			}
		}
	}
}



/**
 * Search window for a variable. Specifies upper and lower limits of the search domain of a
 * variable.
 */
final class VariableWindow {

	private double lowerLimit;
	private double upperLimit;


	/**
	 * Constructor
	 *
	 * @param lower  Description of the Parameter
	 * @param upper  Description of the Parameter
	 */
	public VariableWindow( double lower, double upper ) {
		lowerLimit = lower;
		upperLimit = upper;
	}


	/**
	 * Set the lower limit.
	 *
	 * @param limit  The new lowerLimit value
	 */
	public void setLowerLimit( final double limit ) {
		lowerLimit = limit;
	}


	/**
	 * Get the lower limit.
	 *
	 * @return   The lowerLimit value
	 */
	public double getLowerLimit() {
		return lowerLimit;
	}


	/**
	 * Set the upper limit.
	 *
	 * @param limit  The new upperLimit value
	 */
	public void setUpperLimit( final double limit ) {
		upperLimit = limit;
	}


	/**
	 * Get the upper limit.
	 *
	 * @return   The upperLimit value
	 */
	public double getUpperLimit() {
		return upperLimit;
	}


	/**
	 * Get a description of this variable search window.
	 *
	 * @return   Description of the Return Value
	 */
        @Override
	public String toString() {
		return "lower limit: " + lowerLimit + ", upper limit: " + upperLimit;
	}
}

