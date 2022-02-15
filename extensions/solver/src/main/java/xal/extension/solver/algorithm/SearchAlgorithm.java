/*
 *  SearchAlgorithm.java
 *
 *  Created Wednesday June 9, 2004 2:35 pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver.algorithm;

import xal.tools.messaging.MessageCenter;

import xal.extension.solver.*;
import xal.extension.solver.solutionjudge.*;

import java.util.*;

/**
 * Abstract super class for an optimization search algorithm.
 *
 * @author ky6
 */
public abstract class SearchAlgorithm implements AlgorithmScheduleListener, SolutionJudgeListener {

    /**
     * comparator for sorting based on efficiency
     */
    public static final Comparator<SearchAlgorithm> EFFICIENCY_COMPARATOR = makeEfficiencyComparator();

    /**
     * the message center for dispatching messages
     */
    private final MessageCenter messageCenter;

    /**
     * the proxy for forwarding messages to registered listeners
     */
    private final SearchAlgorithmListener eventProxy;

    /**
     * the minimum evaluations that an algorithm can choose to execute
     */
    private int proposedEvaluations = 1;

    /**
     * measure of this algorithm's running efficiency in solving the problem
     */
    private double efficiency = 1.0;

    /**
     * the number of evaluations remaining to be run
     *
     * this is initialized with getRunCount at the beginning of the algorithm
     * evaluations
     */
    private int evaluationsLeft;

    /**
     * the schedule for the current algorithm
     */
    private AlgorithmSchedule schedule;

    /**
     * the problem to solve
     */
    protected Problem problem;

    /**
     * Empty constructor.
     */
    protected SearchAlgorithm() {
        messageCenter = new MessageCenter("Search Algorithm");
        eventProxy = messageCenter.registerSource(this, SearchAlgorithmListener.class);
    }

    /**
     * Assign a new problem.
     */
    public void setProblem(final Problem problem) {
        if (this.problem != problem) {
            this.problem = problem;
        }
    }

    /**
     * Reset this algorithm.
     */
    public void reset() {
    }

    /**
     * Execute an algorithm run (typically many evaluations up to those
     * proposed)
     *
     * @param schedule the schedule requesting the algorithm execution
     * @param scoreboard the scoreboard of solver status
     */
    public final void executeRun(final AlgorithmSchedule schedule, final ScoreBoard scoreboard) {
        final double initialSatisfaction = (scoreboard.getBestSolution() != null) ? scoreboard.getBestSolution().getSatisfaction() : 0.0;

        this.schedule = schedule;
        evaluationsLeft = getNearestIntegerInRange(proposedEvaluations, getMinEvaluationsPerRun(), getMaxEvaluationsPerRun());
        int initialCount = evaluationsLeft;

        // starts the algorithm
        performRun(schedule);

        // this is the amount of evaluations that were executed
        int evaluations = initialCount - evaluationsLeft;

        updateEfficiency(initialSatisfaction, scoreboard.getBestSolution().getSatisfaction(), evaluations);
    }

    /**
     * Get the nearest integer to the specified target that is within the
     * specified range.
     *
     * @param target the target that we are trying to meet
     * @param lower the minimum value of the allowed range
     * @param upper the maximum value of the allowed range
     * @return the nearest integer to the target, but within the specified range
     */
    private static int getNearestIntegerInRange(final int target, final int lower, final int upper) {
        return (target > upper) ? upper : ((target < lower) ? lower : target);
    }

    /**
     * Compute and update the efficiency of this algorithm's run.
     *
     * @param initialSatisfaction the satisfaction at the start of the run
     * @param endSatisfaction the satisfaction at the end of the run
     * @param evaluations the number of evaluations run
     */
    private void updateEfficiency(final double initialSatisfaction, final double endSatisfaction, final int evaluations) {
        if (evaluations > 0 && initialSatisfaction < 1.0) {
            // rescale the satisfaction based on what can be achieved i.e. at most the satisfaction can be 1.0 and charge for evaluations
            final double newEfficiency = (endSatisfaction - initialSatisfaction) / ((1.0 - initialSatisfaction) * evaluations);
            // weight the new efficiency against the original efficiency
            efficiency = 0.75 * Math.max(newEfficiency, 0.0) + 0.25 * efficiency;

        }
    }

    /**
     * Generate a new efficiency comparator which reverse sorts the algorithms
     * according to efficiency and falls back to algorithm label for
     * deterministic behavior should the efficiencies be degenerate (e.g. before
     * any evaluations).
     *
     * @return a new efficiency comparator
     */
    private static Comparator<SearchAlgorithm> makeEfficiencyComparator() {
        return (algorithmA, algorithmB) -> {
            final double efficiencyA = algorithmA.getEfficiency();
            final double efficiencyB = algorithmB.getEfficiency();
            return efficiencyA < efficiencyB ? 1 : (efficiencyA > efficiencyB ? -1 : algorithmA.getLabel().compareTo(algorithmB.getLabel()));
        };
    }

    /**
     * Evaluate the given trial point.
     *
     * @param trialPoint the trial point to evaluate
     * @return the scored trial
     */
    public Trial evaluateTrialPoint(final TrialPoint trialPoint) {
        --evaluationsLeft;
        return schedule.evaluateTrialPoint(this, trialPoint);
    }

    /**
     * Get the label for this search algorithm.
     *
     * @return The label for this algorithm
     */
    public abstract String getLabel();

    /**
     * Calculate the next few trial points.
     */
    public abstract void performRun(AlgorithmSchedule algorithmSchedule);

    /**
     * get the amount of evaluations left to run for this algorithm
     */
    public int getEvaluationsLeft() {
        return evaluationsLeft;
    }

    /**
     * Get the minimum number of evaluations per run. Subclasses may want to
     * override this method.
     *
     * @return the minimum number of evaluation per run.
     */
    public int getMinEvaluationsPerRun() {
        return 1;
    }

    /**
     * Get the maximum number of evaluations per run. Subclasses may want to
     * override this method.
     *
     * @return the maximum number of evaluation per run.
     */
    public int getMaxEvaluationsPerRun() {
        return Integer.MAX_VALUE;
    }

    /**
     * Sets that proposed minimum evaluations for an algorithm
     */
    public void setProposedEvaluations(int proposedEvaluations) {
        this.proposedEvaluations = proposedEvaluations;
    }

    /**
     * Get this algorithm's efficiency for improving satisfaction.
     *
     * @return the efficiency
     */
    public final double getEfficiency() {
        return efficiency;
    }

    /**
     * Get the rating for this algorithm which in an integer between 0 and 10
     * and indicates how well this algorithm performs on global searches.
     *
     * @return The global search rating for this algorithm.
     */
    abstract int globalRating();

    /**
     * Get the rating for this algorithm which in an integer between 0 and 10
     * and indicates how well this algorithm performs on local searches.
     *
     * @return The local search rating for this algorithm.
     */
    abstract int localRating();

    /**
     * Add a search algorithm listener.
     *
     * @param listener The listener to add.
     */
    public void addSearchAlgorithmListener(final SearchAlgorithmListener listener) {
        messageCenter.registerTarget(listener, this, SearchAlgorithmListener.class);

        // immediately post whether this algorithm is available
        if (getMinEvaluationsPerRun() > 0) {
            listener.algorithmAvailable(this);
        } else {
            listener.algorithmUnavailable(this);
        }
    }

    /**
     * Remove a search algorithm listener.
     *
     * @param listener The listener to remove.
     */
    public void removeSearchAlgorithmListener(final SearchAlgorithmListener listener) {
        messageCenter.removeTarget(listener, this, SearchAlgorithmListener.class);
    }

    /**
     * Handle a message that a trial has been scored.
     *
     * @param schedule Description of the Parameter
     * @param trial Description of the Parameter
     */
    @Override
    public void trialScored(final AlgorithmSchedule schedule, Trial trial) {
        // Do nothing
    }

    /**
     * Handle a message that a trial has been vetoed.
     *
     * @param schedule Description of the Parameter
     * @param trial Description of the Parameter
     */
    @Override
    public void trialVetoed(final AlgorithmSchedule schedule, final Trial trial) {
        // Do nothing
    }

    /**
     * Handle an event where a new algorithm run stack will start.
     *
     * @param schedule the schedule posting the event
     * @param algorithm the algorithm which will execute
     * @param scoreBoard the scoreboard
     */
    @Override
    public void algorithmRunWillExecute(final AlgorithmSchedule schedule, final SearchAlgorithm algorithm, final ScoreBoard scoreBoard) {
        // Do nothing
    }

    /**
     * Handle an event where a new algorithm run stack has completed.
     *
     * @param schedule the schedule posting the event
     * @param algorithm the algorithm that has executed
     * @param scoreBoard the scoreboard
     */
    @Override
    public void algorithmRunExecuted(final AlgorithmSchedule schedule, final SearchAlgorithm algorithm, final ScoreBoard scoreBoard) {
        // Do nothing
    }

    /**
     * Send a message that a new optimal solution has been found.
     *
     * @param source The source of the new optimal solution.
     * @param solutions The list of solutions.
     * @param solution The new optimal solution.
     */
    @Override
    public void foundNewOptimalSolution(final SolutionJudge source, final List<Trial> solutions, final Trial solution) {
        // Do nothing
    }
}
