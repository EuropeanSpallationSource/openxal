/*
 * SatisfactionSumJudge.java
 *
 * Created Wednesday June 30 2004 12:17pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.extension.solver.solutionjudge;

import xal.extension.solver.*;

import java.util.*;

/**
 * SatisfactionSumJudge is a solution judge that decides whether a solution
 * should be kept based on the weighted sum of all the objective's satisfaction.
 *
 * @author ky6
 * @author t6p
 */
public class SatisfactionSumJudge extends SolutionJudge {

    protected static final double DEFAULT_WEIGHT = 1.0;
    protected double bestWeightedSum;
    protected List<Trial> optimalSolutions;
    protected Map<Objective, Double> objectiveWeightMap;

    /**
     * Creates a new SatisfactionJudge instance
     */
    public SatisfactionSumJudge() {
        bestWeightedSum = 0.0;
        objectiveWeightMap = new HashMap<>();
        optimalSolutions = new ArrayList<>();
    }

    /**
     * Reset the satisfaction sum judge.
     */
    @Override
    public void reset() {
        bestWeightedSum = 0.0;
        optimalSolutions = new ArrayList<>();
        objectiveWeightMap = new HashMap<>();
    }

    /**
     * Set the weight of an objective.
     *
     * @param objective The objective to weight.
     * @param weight The weight to give the objective.
     */
    public void setWeight(final Objective objective, final double weight) {
        objectiveWeightMap.put(objective, weight);
    }

    /**
     * Get the weight of an objective.
     *
     * @return The weight of the specified objective
     */
    private double getWeight(final Objective objective) {
        final Double weight = objectiveWeightMap.get(objective);
        return (weight == null) ? DEFAULT_WEIGHT : weight;
    }

    /**
     * Get the optimal solutions.
     *
     * @return A list of solutions
     */
    @Override
    public List<Trial> getOptimalSolutions() {
        return optimalSolutions;
    }

    /**
     * Judge the trial.
     *
     * @param trial the trial to judge.
     */
    @Override
    public void judge(final Trial trial) {
        if (trial.isVetoed()) {
            trial.setSatisfaction(0.0);
        } else {
            double weightedSum = 0.0;
            double totalWeight = 0.0;

            // Calculate the overall satisfaction which is simply the weighted sum of all the score satisfactions.
            for (final Objective objective : trial.getProblem().getObjectives()) {
                final double satisfaction = trial.getSatisfaction(objective);
                final double weight = getWeight(objective);
                totalWeight += weight;
                weightedSum += satisfaction * weight;
            }
            weightedSum /= totalWeight;
            trial.setSatisfaction(weightedSum);

            if (weightedSum == bestWeightedSum) {
                optimalSolutions.add(trial);
                eventProxy.foundNewOptimalSolution(this, optimalSolutions, trial);
            } else if (weightedSum > bestWeightedSum) {
                bestWeightedSum = weightedSum;
                optimalSolutions.clear();
                optimalSolutions.add(trial);
                eventProxy.foundNewOptimalSolution(this, optimalSolutions, trial);
            }
        }
    }
}
