/*
 *  AlgorithmSchedule.java
 *
 *  Created Wednesday June 9, 2004 2:32 pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.tools.messaging.MessageCenter;

import xal.extension.solver.algorithm.*;
import xal.extension.solver.market.*;

/**
 * AlgorithmSchedule keeps track of and executes the next algorithm based on its
 * score. Schedule also sets a trial and a stopper.
 *
 * @author ky6
 * @author t6p
 */
public class AlgorithmSchedule {

    private static final Logger LOGGER = Logger.getLogger(AlgorithmSchedule.class.getName());

    /**
     * message center for dispatching messages
     */
    private final MessageCenter messageCenter;

    /**
     * proxy which forwarding messages to registered listeners
     */
    private final AlgorithmScheduleListener eventProxy;

    /**
     * determines when to stop the trials
     */
    volatile protected Stopper stopper;

    /**
     * the problem to solve
     */
    protected Problem problem;

    /**
     * the market of algorithm runs
     */
    protected AlgorithmMarket market;

    /**
     * the solver running the schedule
     */
    protected Solver solver;

    /**
     * the maximum proposed Evaluations defined by the largest
     * minimumEvaluations of an algorithm
     */
    private int proposedEvaluations;

    /**
     * Creates a new instance of Schedule.
     *
     * @param solver The solver
     * @param market The market providing runs.
     * @param stopper The stopper which can terminate the schedule.
     */
    public AlgorithmSchedule(final Solver solver, final AlgorithmMarket market, final Stopper stopper) {
        messageCenter = new MessageCenter("Algorithm Schedule");
        eventProxy = messageCenter.registerSource(this, AlgorithmScheduleListener.class);

        this.solver = solver;
        this.market = market;

        proposedEvaluations = 1;

        setStopper(stopper);
    }

    /**
     * Reset the algorithm run stack.
     */
    public void reset() {
        market.reset();
    }

    /**
     * Add an algorithm schedule listener.
     *
     * @param aListener The listener to add.
     */
    public void addAlgorithmScheduleListener(AlgorithmScheduleListener aListener) {
        messageCenter.registerTarget(aListener, this, AlgorithmScheduleListener.class);
    }

    /**
     * Remove an algorithm schedule listener.
     *
     * @param aListener The listener to remove.
     */
    public void removeAlgorithmScheduleListener(AlgorithmScheduleListener aListener) {
        messageCenter.removeTarget(aListener, this, AlgorithmScheduleListener.class);
    }

    /**
     * Get the algorithm market.
     *
     * @return the algorithm market
     */
    public AlgorithmMarket getMarket() {
        return market;
    }

    /**
     * get the score board
     */
    public ScoreBoard getScoreBoard() {
        return solver.getScoreBoard();
    }

    /**
     * Assign a new problem.
     *
     * @param problem the new problem
     */
    public void setProblem(final Problem problem) {
        this.problem = problem;
        market.setProblem(problem);
        computeMinimumEvaluations();
    }

    /**
     * Get the stopper.
     *
     * @return the stopper
     */
    public Stopper getStopper() {
        return stopper;
    }

    /**
     * Assign a new stopper.
     *
     * @param stopper the new stopper
     */
    public void setStopper(final Stopper stopper) {
        this.stopper = stopper;
    }

    /**
     * Determine whether to continue executing the schedule.
     *
     * @return true if the stopper allows us to continue executing the schedule
     * and false if not
     */
    public boolean shouldExecute() {
        return !shouldStop();
    }

    /**
     * Allows the algorithms to check when they should stop executing their code
     */
    public boolean shouldStop() {
        return stopper.shouldStop(solver);
    }

    /**
     * Going to get the largest minimumEvaluations that a program desires This
     * will search through all the current algorithms in the pool
     */
    private void computeMinimumEvaluations() {
        final AlgorithmPool pool = solver.getAlgorithmPool();

        int newProposedEvaluations = 1;
        for (SearchAlgorithm algorithm : pool.getAlgorithms()) {
            int algorithmMinEvals = algorithm.getMinEvaluationsPerRun();
            if (algorithmMinEvals > newProposedEvaluations) {
                newProposedEvaluations = algorithmMinEvals;
            }
        }

        this.proposedEvaluations = newProposedEvaluations;
    }

    /**
     * Execute the search schedule.
     */
    public void execute() {
        try {
            if (shouldExecute()) {
                // the very first algorithm should be the InitialAlgorithm which generates a trial point from the variables' starting values
                executeRun(new InitialAlgorithm(problem));
            }

            while (shouldExecute()) {
                executeRun(market.nextAlgorithm());
            }
        } catch (RunTerminationException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
        }
    }

    /**
     * Execute a run for the specified algorithm.
     *
     * @param algorithm the algorithm to execute.
     */
    private void executeRun(final SearchAlgorithm algorithm) {
        if (algorithm != null) {
            algorithm.setProposedEvaluations(proposedEvaluations);

            eventProxy.algorithmRunWillExecute(this, algorithm, solver.getScoreBoard());
            algorithm.executeRun(this, solver.getScoreBoard());
            eventProxy.algorithmRunExecuted(this, algorithm, solver.getScoreBoard());
        }
    }

    /**
     * Evaluate the specified trial point or return null if the run has been
     * terminated.
     *
     * @param trialPoint the trial point to evaluate
     * @return a scored trial corresponding to the specified trial point
     * @throws xal.extension.solver.RunTerminationException if the run has been
     * terminated
     */
    public Trial evaluateTrialPoint(final SearchAlgorithm searchAlgorithm, final TrialPoint trialPoint) {
        if (stopper.shouldStop(solver)) {
            throw new RunTerminationException("Run terminated by the stopper.");
        } else if (searchAlgorithm.getEvaluationsLeft() < 0) {
            throw new RunTerminationException("Run terminated due to overrun of scheduled evaluations.");
        }

        final Trial trial = new Trial(problem, trialPoint, searchAlgorithm);
        score(trial);

        return trial;
    }

    /**
     * Score the trial.
     *
     * @param trial The trial to be scored.
     */
    private void score(final Trial trial) {
        final boolean isSuccessful = problem.evaluate(trial);
        if (!isSuccessful) {
            eventProxy.trialVetoed(this, trial);
        }
        solver.judge(trial);
        eventProxy.trialScored(this, trial);
    }
}
