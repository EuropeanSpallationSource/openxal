/*
 *  ScoreBoard.java
 *
 *  Created Wednesday June 9, 2004 2:32pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import xal.tools.messaging.MessageCenter;

import xal.extension.solver.solutionjudge.SolutionJudgeListener;
import xal.extension.solver.solutionjudge.SolutionJudge;
import xal.extension.solver.algorithm.SearchAlgorithm;

import java.util.*;

/**
 * Scoreboard maintains the status of the solver including the clock and the best solution
 * found so far.
 *
 * @author   ky6
 * @author	t6p
 */
public final class ScoreBoard implements AlgorithmScheduleListener, SolutionJudgeListener {
	/** center for broadcasting events */
	private final MessageCenter messageCenter;
	
	/** proxy which forwards events to registered listeners */
	private final ScoreBoardListener eventProxy;
	
	/** time when the solver started */
	private Date startTime;
	
	/** the best solution found */
	private Trial bestSolution;
	
	/** the solution judge */
	private SolutionJudge solutionJudge;
	
	/** the number of evaluations performed */
	private int evaluations;
	
	/** number of algorithm run executions (note that one run can correspond to many evaluations) */
	private int algorithmRunExecutions;
	
	/** the number of evaluations that have been vetoed */
	private int vetoes;
	
	/** the number of times an optimal solution was found */
	private int optimalSolutionsFound;

    /** HashMap for containing the algorithms and the amount of evaluations that each one completes */
    private Map<String, Integer> evaluationsLog = new HashMap<>();

    /** this is for recording the efficiency of the algorithms over a set amount of evaluations */
    private EfficiencyLogger efficiencyLogger = null;
    
	/**
	 * Constructor
	 * @param solutionJudge  the solution judge
	 */
	public ScoreBoard( final SolutionJudge solutionJudge ) {
		messageCenter = new MessageCenter( "Scoreboard" );
		eventProxy = messageCenter.registerSource( this, ScoreBoardListener.class );
		
		setSolutionJudge( solutionJudge );
		reset();
	}
	
	
	/**
	 * Add the specified listener as a receiver of ScoreBoard events from this instance. 
	 */
	public void addScoreBoardListener( final ScoreBoardListener listener ) {
		messageCenter.registerTarget( listener, this, ScoreBoardListener.class );
	}
	
	
	/**
	 * Remove the specified listener from receiving ScoreBoard events from this instance. 
	 */
	public void removeScoreBoardListener( final ScoreBoardListener listener ) {
		messageCenter.removeTarget( listener, this, ScoreBoardListener.class );
	}


	/** Reset the start time and the number of evaluations.  */
	public void reset() {
		solutionJudge.reset();
		startTime = new Date();
		evaluations = 0;
		algorithmRunExecutions = 0;
		vetoes = 0;
		optimalSolutionsFound = 0;
        bestSolution = null;
	}


	/**
	 * Set the solution judge.
	 * @param solutionJudge   The new solutionJudge value
	 */
	public void setSolutionJudge( SolutionJudge solutionJudge ) {
		if ( this.solutionJudge != null ) {
			this.solutionJudge.removeSolutionJudgeListener( this );
		}

		this.solutionJudge = solutionJudge;
		if ( solutionJudge != null ) {
			solutionJudge.addSolutionJudgeListener( this );
		}
	}


	/**
	 * Get the solution judge.
	 * @return   The solution judge.
	 */
	public SolutionJudge getSolutionJudge() {
		return solutionJudge;
	}
    
    /**
     * Get the satisfaction of the best trial point
     */
    public double getSatisfaction(){
        return bestSolution.getSatisfaction();
    }
	
	/**
	 * Get the number of algorithm executions
	 * @return number of algorithm executions
	 */
	public int getAlgorithmExecutions() {
		return algorithmRunExecutions;
	}


	/**
	 * Get the number of vetoes.
	 * @return   The number of vetoes made.
	 */
	public int getVetoes() {
		return vetoes;
	}


	/**
	 * Get the number of optimal solutions found.
	 * @return   The number of optimal solutions found.
	 */
	public int getOptimalSolutionsFound() {
		return optimalSolutionsFound;
	}


	/**
	 * Get the elapsed time.
	 * @return   elapsed time in seconds.
	 */
	public double getElapsedTime() {
		Date currentTime = new Date();
		long elapsedTime = currentTime.getTime() - startTime.getTime();
		return ( (double)( elapsedTime ) ) / 1000;
	}
	
	
	/**
	 * Judge the specified trial.
	 * @param trial the trial to judge
	 */
	public void judge( final Trial trial ) {
		solutionJudge.judge( trial );
	}


	/**
	 * Send a message that a trial has been scored.
	 * @param algorithmSchedule  The algorithm schedule that holds the trial scored.
	 * @param trial              The trial that was scored.
	 */
        @Override
	public void trialScored( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {
		++evaluations;
        eventProxy.trialScored( this, trial );
        
        SearchAlgorithm algorithm = trial.getAlgorithm();
        String label = algorithm.getLabel();
        if(evaluationsLog.containsKey(label)){
            Integer newEvaluations = evaluationsLog.get(label);
            newEvaluations ++;
            evaluationsLog.put(label, newEvaluations);
        }
        else{
            evaluationsLog.put(label, 1);
        }
        
        if( efficiencyLogger != null )  efficiencyLogger.record(trial);
	}


	/**
	 * Send a message that a trial has been vetoed.
	 * @param algorithmSchedule  The algorithm schedule that holds the trial vetoed.
	 * @param trial              The trial that was vetoed.
	 */
        @Override
	public void trialVetoed( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {
		++vetoes;
		eventProxy.trialVetoed( this, trial );
	}
	
	
	/**
	 * Handle an event where a new algorithm run stack will start.
	 * @param schedule the schedule posting the event
	 * @param algorithm the algorithm which will execute
	 * @param scoreBoard the scoreboard
	 */
        @Override
	public void algorithmRunWillExecute( final AlgorithmSchedule schedule, final SearchAlgorithm algorithm, final ScoreBoard scoreBoard ) {}
	
	
	/**
	 * Handle an event where a new algorithm run stack has completed.
	 * @param schedule the schedule posting the event
	 * @param algorithm the algorithm that has executed
	 * @param scoreBoard the scoreboard
	 */
        @Override
	public void algorithmRunExecuted( final AlgorithmSchedule schedule, final SearchAlgorithm algorithm, final ScoreBoard scoreBoard ) {
		++algorithmRunExecutions;
	}
	

	/**
	 * Send a message that a new optimal solution has been found.
	 * @param source     The source of the new optimal solution.
	 * @param solutions  The list of solutions.
	 * @param solution   Description of the Parameter
	 */
        @Override
	public void foundNewOptimalSolution( final SolutionJudge source, final List<Trial> solutions, final Trial solution ) {
		++optimalSolutionsFound;
		bestSolution = solution;
		eventProxy.newOptimalSolution( this, solution );
	}


	/**
	 * Get the new solution.
	 * @return   The new solution.
	 */
	public Trial getBestSolution() {
		return bestSolution;
	}


	/**
	 * A string for displaying the ScoreBoard.
	 * @return   The string representation of the ScoreBoard.
	 */
        @Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append( "\n\tScoreBoard\n***********************************\n" );
		buffer.append("Elapsed Time:  ").append(getElapsedTime()).append(" seconds\n");
		buffer.append("Evaluations:  ").append(getEvaluations()).append("\n");
		buffer.append("Vetoes:  ").append(getVetoes()).append("\n");
		buffer.append("Optimal Solutions Found:  ").append(getOptimalSolutionsFound()).append("\n");
		buffer.append("Number of Existing Optimal Solutions:  ").append(solutionJudge.getOptimalSolutions().size()).append("\n");
		buffer.append("Overall Satisfaction:  ").append(bestSolution.getSatisfaction()).append("\n");

		buffer.append( "Optimal Solutions: \n" );
		
		final Iterator<Trial> solutionIter = solutionJudge.getOptimalSolutions().iterator();
		int count = 0;
		while ( solutionIter.hasNext() && count < 3 ) {
			count++;
			buffer.append( "-------------------------------------\n" );
			final Trial optimalSolution = solutionIter.next();
			
			final Problem problem = optimalSolution.getProblem();
			for ( final Variable variable : problem.getVariables() ) {
				double value = optimalSolution.getTrialPoint().getValue( variable );
				buffer.append("Variable: ").append(variable.getName()).append(" = ").append(value).append("\n");
			}
			
			for ( final Objective objective : problem.getObjectives() ) {
				Score score = optimalSolution.getScore( objective );
				double value = score.getValue();
				double satisfaction = score.getSatisfaction();
				buffer.append("Objective: ").append(objective.getName());
				buffer.append(" = ").append(value);
				buffer.append(", satisfaction = ").append(satisfaction).append("\n");
			}
		}

		return buffer.toString();
	}
    
    
    /**
     * Get the number of evaluations.
     * @return   The number of evaluations.
     */
    public int getEvaluations() {
        return evaluations;
    }
    
    /**
     * get a copy of the evaluations for each algorithm executed
     */
    public Map<String, Integer> getEvaluationsLog() {
        return new HashMap<>( evaluationsLog );
    }
    
    
    
    
    /**
     * class for recording the efficiency for each algorithm averaged over a set amount of time
     * Also records the averaged distribution of evaluations among algorithms
     *
     * The record method is based on evaluations. Default is to average and record every 1000 evaluations
     */
    private class EfficiencyLogger{
        protected int evaluationsStep; // average and record information every (_evaluationsStep) number of evaluaitons
        protected Map<String, double[]> currentData = new HashMap<>(); // Algorithm name, dataArray
        //double[] dataArray is [0] = Evaluaitons, [1] initial satisfaction, [2] final satisfaction
        
        protected int pendingEvaluations; // just keeps tracks of how total evaluations have occured since last print statment
        
        /*
         * Just initializes the parameters
         */
        public EfficiencyLogger() {
            pendingEvaluations = 0;
            evaluationsStep = 1000;
            
        }
        
        /*
         * Controlls the stepsize over which to integrate and average
         */
        public void setEvaluationsStep(int evaluationsStep){
            if(evaluationsStep > 0){
                this.evaluationsStep = evaluationsStep;
            }
        }
        
        /*
         * Records the evaluation for an algorithm, keeping track of the efficiency. Basically just shows if there is any improvement by that algorithm
         */
        public void record(Trial trial){
            pendingEvaluations ++;
            
            String thisLabel = trial.getAlgorithm().getLabel();
            if("Initial Algorithm".equals(thisLabel)){
                System.out.printf("%15s %5s, %6s\n","### Total Evals || ", "Evaluations || ", "Efficiency ###"); // displaying the format of what is later printed
            }
            
            double[] emptyArray = new double[] {0,0,0};
            double[] dataArray = new double[3];
            
            // if there is no data for that particular algorithm, add algorithm to list and initialize points on dataArray
            if(!currentData.containsKey(thisLabel) || Arrays.equals(currentData.get(thisLabel), emptyArray)){
                dataArray[0] = 1;
                dataArray[1] = trial.getSatisfaction();
                dataArray[2] = trial.getSatisfaction();
                currentData.put(thisLabel, dataArray);

            }
            // update data to existing algorithm in map
            else{
                dataArray = currentData.get(thisLabel);
                dataArray[0] ++;
                double oldSatisfaction = dataArray[2];
                double newSatisfaction = trial.getSatisfaction();
                if(newSatisfaction > oldSatisfaction){
                    dataArray[2] = trial.getSatisfaction();
                }
                
                currentData.put(thisLabel, dataArray);
            }
            
            // if evaluation limit reached, save, print, and reset
            if(pendingEvaluations >= evaluationsStep){
                
                String[] labels = currentData.keySet().toArray(new String[0]);
                System.out.printf("%6d ", getEvaluations());
                
                int algorithmIndx = 0; // keeps track of how many algorithms have been printed on one line, limit to only 4
                for(String label: labels){
                    
                    dataArray = currentData.get(label);
                    double finalSatisfaction = dataArray[2];
                    double initialSatisfaction = dataArray[1];
                    
                    dataArray[2] = finalSatisfaction;
                    currentData.put(label, dataArray);
                    
                    algorithmIndx ++;
                    if(algorithmIndx > 5){
                        System.out.printf("\n%6s", "");
                        algorithmIndx = 0;
                    }
                    
                    double evaluations = dataArray[0];
                    double efficiency = (finalSatisfaction - initialSatisfaction)/((1.0 - initialSatisfaction)*evaluations);
                    
                    String shortLabel;
                    if(label.length() > 13) {
                        shortLabel = label.substring(0,13);
                    }
                    else{
                        shortLabel = label;
                    }
                    
                    if(evaluations == 0){
                        efficiency = 0;
                    }
                    
                    System.out.printf("%s %5.0f, %8.5f || ", shortLabel, evaluations, efficiency);
                    
                    currentData.put(label, emptyArray);
                }
                
                System.out.printf("\n");
                
                
                // once data is recorded and printed, reset current data values
                pendingEvaluations = 0;
            }
        }
    }


    /**
     * Turns efficiencyLogger on with parameters
     */
    public void recordEfficiency( final int evaluationsStep ){
		efficiencyLogger = new EfficiencyLogger();
        efficiencyLogger.setEvaluationsStep( evaluationsStep );
    }

}

