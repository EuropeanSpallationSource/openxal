/*
 *  SolutionJudge.java
 *
 *  Created Tuesday June 29, 2004 12:17pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver.solutionjudge;

import xal.tools.messaging.MessageCenter;

import xal.extension.solver.Trial;

import java.util.*;

/**
 * SolutionJudge decides whether the latest scored solution is an optimal solution. In many
 * cases only one solution can be the optimal solution at any time. In other cases, there may
 * be a surface of optimal solutions at any time.
 *
 * @author   ky6
 * @author t6p
 */
public abstract class SolutionJudge {
	/** message center for dispatching events to registered listeners */
	protected MessageCenter messageCenter;
	
	/** proxy which forwards events to registered listeners */
	protected SolutionJudgeListener eventProxy;
	
	
	/** Creates a new instance of SolutionJudge */
	public SolutionJudge() {
		messageCenter = new MessageCenter( "Solution Judge" );
		eventProxy = messageCenter.registerSource( this, SolutionJudgeListener.class );
		reset();
	}


	/**
	 * Get the default solution judge.
	 * @return   the worst objective biased judge
	 */
	public static SolutionJudge getInstance() {
		return new WorstObjectiveBiasedJudge();
	}


	/** Reset the solution judge.  */
	public abstract void reset();


	/**
	 * Add a solution judge listener.
	 * @param aListener  The listener to add.
	 */
	public void addSolutionJudgeListener( SolutionJudgeListener aListener ) {
		messageCenter.registerTarget( aListener, this, SolutionJudgeListener.class );
	}


	/**
	 * Remove a solution judge listener.
	 * @param aListener  The listener to remove.
	 */
	public void removeSolutionJudgeListener( SolutionJudgeListener aListener ) {
		messageCenter.removeTarget( aListener, this, SolutionJudgeListener.class );
	}


	/**
	 * Get the optimal solutions.
	 * @return   A list of solutions.
	 */
	public abstract List<Trial> getOptimalSolutions();


	/**
	 * Judge the trial.
	 * @param trial  The trial to update the solution judge with.
	 */
	public abstract void judge( Trial trial );
}

