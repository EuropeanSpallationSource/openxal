/*
 *  AlgorithmPool.java
 *
 *  Created Thursday July 8, 2004 4:12pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import xal.extension.solver.algorithm.*;
import xal.extension.solver.solutionjudge.*;

import xal.tools.messaging.MessageCenter;

import java.util.*;

/**
 * AlgorithmPool keeps track of the available algorithms.
 *
 * @author ky6
 * @author t6p
 */
public class AlgorithmPool implements SearchAlgorithmListener, SolutionJudgeListener, AlgorithmScheduleListener {

    /**
     * The list of all algorithms
     */
    private Collection<SearchAlgorithm> algorithms;

    /**
     * The collection of algorithms available for scheduling
     */
    private Collection<SearchAlgorithm> availableAlgorithms;

    /**
     * Message center for dispatching events to registered listeners
     */
    private final MessageCenter messageCenter;

    /**
     * Proxy which forwards events to registered listeners
     */
    private final AlgorithmPoolListener eventProxy;

    /**
     * Creates a new AlgorithmPool instance Constructor that takes a list of
     * algorithms
     *
     * @param algorithms the collection of algorithms to populate the pool
     */
    public AlgorithmPool(final Collection<SearchAlgorithm> algorithms) {
        this.algorithms = new HashSet<>();
        availableAlgorithms = new HashSet<>();

        messageCenter = new MessageCenter("Algorithm Pool");
        eventProxy = messageCenter.registerSource(this, AlgorithmPoolListener.class);

        addAlgorithms(algorithms);
    }

    /**
     * Empty constructor which populates the pool of all algorithms
     */
    public AlgorithmPool() {
        this(generateDefaultAlgorithms());
    }

    /**
     * Creates a new AlgorithmPool instance Constructor that takes a list of
     * algorithms
     *
     * @param algorithm Description of the Parameter
     */
    public AlgorithmPool(final SearchAlgorithm algorithm) {
        this(Collections.<SearchAlgorithm>singletonList(algorithm));
    }

    /**
     * Get all the algorithms.
     *
     * @return The default set of algorithms
     */
    public static Collection<SearchAlgorithm> generateDefaultAlgorithms() {
        final Collection<SearchAlgorithm> allAlgorithms = new HashSet<>();

        allAlgorithms.add(new RandomSearch());
        allAlgorithms.add(new RandomShrinkSearch());
        allAlgorithms.add(new SimplexSearchAlgorithm());
        allAlgorithms.add(new DirectedStep());

        return allAlgorithms;
    }

    /**
     * Reset the algorithm pool by resetting all the algorithms.
     */
    public void reset() {
        for (final SearchAlgorithm algorithm : algorithms) {
            algorithm.reset();
        }
    }

    /**
     * Assign the problem to each algorithm in the pool.
     *
     * @param problem the problem to solve
     */
    public void setProblem(final Problem problem) {
        for (final SearchAlgorithm algorithm : algorithms) {
            algorithm.setProblem(problem);
        }
    }

    /**
     * Add an algorithm pool listener.
     *
     * @param listener The listener to add.
     */
    public void addAlgorithmPoolListener(final AlgorithmPoolListener listener) {
        messageCenter.registerTarget(listener, this, AlgorithmPoolListener.class);
    }

    /**
     * Remove a algorithm pool listener.
     *
     * @param listener The listener to remove.
     */
    public void removeAlgorithmPoolListener(final AlgorithmPoolListener listener) {
        messageCenter.removeTarget(listener, this, AlgorithmPoolListener.class);
    }

    /**
     * Set the algorithm as the sole algorithm in the pool.
     *
     * @param algorithm the algorithm to set as the only item in the pool
     */
    public void setAlgorithm(final SearchAlgorithm algorithm) {
        removeAllAlgorithms();
        addAlgorithm(algorithm);
    }

    /**
     * Add existing algorithms to the algorithm list.
     *
     * @param algorithms The feature to be added to the Algorithms attribute
     */
    public void addAlgorithms(final Collection<SearchAlgorithm> algorithms) {
        for (final SearchAlgorithm algorithm : algorithms) {
            addAlgorithm(algorithm);
        }
    }

    /**
     * Add an algorithm to the pool.
     *
     * @param algorithm The feature to be added to the Algorithm attribute
     */
    public void addAlgorithm(final SearchAlgorithm algorithm) {
        algorithms.add(algorithm);
        algorithm.addSearchAlgorithmListener(this);
        eventProxy.algorithmAdded(this, algorithm);
    }

    /**
     * Remove an algorithm from the pool.
     *
     * @param algorithm the algorithm to remove from the pool
     */
    public void removeAlgorithm(final SearchAlgorithm algorithm) {
        algorithm.removeSearchAlgorithmListener(this);
        algorithms.remove(algorithm);
        availableAlgorithms.remove(algorithm);
        eventProxy.algorithmRemoved(this, algorithm);
    }

    /**
     * Remove the specified algorithms.
     *
     * @param algorithms the algorithms to remove
     */
    public void removeAlgorithms(final Collection<SearchAlgorithm> algorithms) {
        for (final SearchAlgorithm algorithm : algorithms) {
            removeAlgorithm(algorithm);
        }
    }

    /**
     * Remove all algorithms.
     */
    public void removeAllAlgorithms() {
        removeAlgorithms(new HashSet<>(algorithms));
    }

    /**
     * Get a copy of the algorithms.
     *
     * @return The list of algorithms.
     */
    public Collection<SearchAlgorithm> getAlgorithms() {
        return new HashSet<>(algorithms);
    }

    /**
     * Get the available algorithms.
     *
     * @return The list of available algorithm.
     */
    public Collection<SearchAlgorithm> getAvailableAlgorithms() {
        return new HashSet<>(availableAlgorithms);
    }

    /**
     * Send a message that a trial has been scored.
     *
     * @param algorithmSchedule The algorithm schedule that holds the trial
     * scored.
     * @param trial The trial that was scored.
     */
    @Override
    public void trialScored(final AlgorithmSchedule algorithmSchedule, final Trial trial) {
        for (final SearchAlgorithm algorithm : getAlgorithms()) {
            algorithm.trialScored(algorithmSchedule, trial);
        }
    }

    /**
     * Send a message that a trial has been vetoed.
     *
     * @param algorithmSchedule The algorithm schedule that holds the trial
     * vetoed.
     * @param trial The trial that was vetoed.
     */
    @Override
    public void trialVetoed(final AlgorithmSchedule algorithmSchedule, final Trial trial) {
        for (final SearchAlgorithm algorithm : getAlgorithms()) {
            algorithm.trialVetoed(algorithmSchedule, trial);
        }
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
     * Send a message that an algorithm is available. An algorithm is available
     * if it has all the data it needs to propose a new trial.
     *
     * @param source The source of the available algorithm.
     */
    @Override
    public void algorithmAvailable(final SearchAlgorithm source) {
        availableAlgorithms.add(source);
        eventProxy.algorithmAvailable(this, source);
    }

    /**
     * Send a message that an algorithm is not available.
     *
     * @param source The source of the available algorithm.
     */
    @Override
    public void algorithmUnavailable(SearchAlgorithm source) {
        availableAlgorithms.remove(source);
        eventProxy.algorithmUnavailable(this, source);
    }

    /**
     * Event indicating that a new optimal solution has been found.
     *
     * @param source The source of the new optimal solution.
     * @param solutions The list of solutions.
     * @param solution The new optimal solution.
     */
    @Override
    public void foundNewOptimalSolution(final SolutionJudge source, final List<Trial> solutions, final Trial solution) {
        for (final SearchAlgorithm algorithm : algorithms) {
            algorithm.foundNewOptimalSolution(source, solutions, solution);
        }
    }
}
