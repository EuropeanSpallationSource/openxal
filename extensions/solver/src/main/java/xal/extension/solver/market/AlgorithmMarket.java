/*
 *  AlgorithmMarket.java
 *
 *  Created Tuesday July 13 2004 1:14pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver.market;

import xal.tools.messaging.*;
import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.solutionjudge.*;

import java.util.*;


/**
 * AlgorithmMarket keeps track of algorithms.
 * @author   ky6
 * @author   t6p
 */
public class AlgorithmMarket implements AlgorithmScheduleListener, SolutionJudgeListener {
	/** the probability ratio for picking algorithms sorted by efficiency */
	private static final double PROBABILITY_RATIO = 0.25;
	
	/** natural logarithm of the probability ratio  */
	private static final double PROBABILITY_RATIO_LOG = Math.log( PROBABILITY_RATIO );
	
	/** the random number generator */
	private final Random randomGenerator;
	
	/** the random number generator seed for reproducibility */
	private static final long RANDOM_SEED = 12345678901234L;
	
	/** the list of algorithms in the market sorted by efficiency so the most efficient algorithms appear first */
	private List<SearchAlgorithm> algorithmsByEfficiency;
	
	/** the pool of algorithms from which to pick an algorithm */
	private AlgorithmPool algorithmPool;
	
	/** message center which dispatches events to registered listeners */
	private final MessageCenter messsageCenter;
	
	/** proxy which forwards events to registered listeners */
	private final AlgorithmMarketListener eventProxy;

	
	/**
	 * Primary Constructor
	 * @param pool        the pool of algorithms
	 */
	public AlgorithmMarket( final AlgorithmPool pool ) {
		randomGenerator = new Random( RANDOM_SEED );		

		messsageCenter = new MessageCenter("Algorithm Market");
		eventProxy = messsageCenter.registerSource( this, AlgorithmMarketListener.class );
		
		algorithmsByEfficiency = new ArrayList<>();
		setAlgorithmPool( pool );
	}


	/**
	 * Constructor
	 * @param algorithm  the only algorithm to use which also implies the single algorithm
	 */
	public AlgorithmMarket( final SearchAlgorithm algorithm ) {
		this( new AlgorithmPool( algorithm ) );
	}


	/** Constructor using the default algorithm pool and the default algorithms List. */
	public AlgorithmMarket() {
		this( new AlgorithmPool() );
	}
	
	
	/** reset the market */
	public void reset() {
		algorithmPool.reset();
		randomGenerator.setSeed( RANDOM_SEED );
	}
	
	
	/**
	 * Add a listener to receive AlgorithmMarket events.
	 * @param listener the listener to add for receiving algorithm market events
	 */
	public void addAlgorithmMarketListener( final AlgorithmMarketListener listener ) {
		messsageCenter.registerTarget( listener, this, AlgorithmMarketListener.class );
	}
	
	
	/**
	 * Remove the listener from receiving AlgorithmMarket events.
	 * @param listener the listener to remove from receiving algorithm market events
	 */
	public void removeAlgorithmMarketListener( final AlgorithmMarketListener listener ) {
		messsageCenter.removeTarget( listener, this, AlgorithmMarketListener.class );
	}
	
	
	/**
	 * Assign a new problem.
	 * @param problem the new problem
	 */
	public void setProblem( final Problem problem ) {
		algorithmPool.setProblem( problem );
	}
	


	/**
	 * Get the algorithm pool.
	 * @return   The algorithm pool.
	 */
	public AlgorithmPool getAlgorithmPool() {
		return algorithmPool;
	}


	/**
	 * Get the algorithm List.
	 * @return   The list of algorithms.
	 */
	public List<SearchAlgorithm> getAlgorithms() {
		return algorithmsByEfficiency;
	}


	/**
	 * Set the list of algorithms.
	 * @param algorithmsList  The list of algorithms.
	 */
	private void setAlgorithms( final List<SearchAlgorithm> algorithms ) {
		algorithmsByEfficiency.clear();
		algorithmsByEfficiency.addAll( algorithms );
	}


	/**
	 * Set the algorithm pool.
	 * @param pool  The algorithm pool used to set the local algorithm pool.
	 */
	public void setAlgorithmPool( final AlgorithmPool pool ) {
		final AlgorithmPool oldPool = algorithmPool;
		algorithmPool = pool;

		// add algorithms
		algorithmsByEfficiency.clear();
		algorithmsByEfficiency.addAll( pool.getAlgorithms() );

		eventProxy.poolChanged( this, oldPool, pool );
	}
	
	
	/**
	 * Get the next algorithm to execute by sorting algorithms by efficiency and then picking a algorithm randomly but weighted by
	 * the probability ratio for each successive algorithm.
	 * @return the next algorithm
	 */
	public SearchAlgorithm nextAlgorithm() {
		Collections.sort(algorithmsByEfficiency, SearchAlgorithm.EFFICIENCY_COMPARATOR );
		final int count = algorithmsByEfficiency.size();
		final int selectedIndex = (int)( Math.log( 1.0 - randomGenerator.nextDouble() * ( 1.0 - Math.pow( PROBABILITY_RATIO, count ) ) ) / PROBABILITY_RATIO_LOG );
		return algorithmsByEfficiency.get( Math.min( selectedIndex, count - 1 ) );
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
	public void algorithmRunExecuted( final AlgorithmSchedule schedule, final SearchAlgorithm algorithm, final ScoreBoard scoreBoard ) {}
	

	/**
	 * Handle a message that a trial has been scored.
	 * @param algorithmSchedule  The algorithm schedule that holds the trial scored.
	 * @param trial              The trial that was scored.
	 */
        @Override
	public void trialScored( final AlgorithmSchedule algorithmSchedule, final Trial trial ) {
		algorithmPool.trialScored( algorithmSchedule, trial );
	}


	/**
	 * Handle a message that a trial has been vetoed.
	 * @param algorithmSchedule  The algorithm schedule that holds the trial vetoed.
	 * @param trial              The trial that was vetoed.
	 */
        @Override
	public void trialVetoed( final AlgorithmSchedule algorithmSchedule, final Trial trial ) { 
		algorithmPool.trialVetoed( algorithmSchedule, trial );
	}


	/**
	 * Event indicating that a new optimal solution has been found.
	 * @param source     The source of the new optimal solution.
	 * @param solutions  The list of solutions.
	 * @param solution   The new optimal solution.
	 */
        @Override
	public void foundNewOptimalSolution( final SolutionJudge source, final List<Trial> solutions, final Trial solution ) { 
		algorithmPool.foundNewOptimalSolution( source, solutions, solution );
	}
}

