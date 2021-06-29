//
//  WorstObjectiveBiasedJudge.java
//  xal
//
//  Created by Thomas Pelaia on 6/15/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.solver.solutionjudge;

import xal.extension.solver.*;

import java.util.*;

/**
 * This judge weights the least satisfied objective most and each subsequent one
 * half less than the prior one. Each objective satisfaction must be based on a
 * range of 0 to 1.
 *
 * @author t6p
 */
public class WorstObjectiveBiasedJudge extends SolutionJudge {

    /**
     * the bias weight
     */
    protected final double biasWeight;

    /**
     * the current best satisfaction
     */
    protected double bestSatisfaction;

    /**
     * used to normalize the total satisfaction to a scale of 0 to 1
     */
    protected double totalWeight;

    /**
     * the current list of the most optimal solutions
     */
    protected List<Trial> optimalSolutions;

    /**
     * Constructor
     */
    public WorstObjectiveBiasedJudge() {
        this(0.25);
    }

    /**
     * Constructor
     */
    public WorstObjectiveBiasedJudge(final double biasWeight) {
        this.biasWeight = biasWeight;
        bestSatisfaction = 0.0;
        totalWeight = 0.0;
        optimalSolutions = new ArrayList<>();
    }

    /**
     * Reset the satisfaction sum judge.
     */
    @Override
    public void reset() {
        bestSatisfaction = 0.0;
        totalWeight = 0.0;
        optimalSolutions = new ArrayList<>();
    }

    /**
     * Get the optimal solutions.
     *
     * @return a list of solutions
     */
    @Override
    public List<Trial> getOptimalSolutions() {
        return optimalSolutions;
    }

    /**
     * Judge the trial.
     *
     * @param trial The trial with which to update the solution judge.
     */
    @Override
    public void judge(final Trial trial) {
        if (trial.isVetoed()) {
            trial.setSatisfaction(0.0);
        } else {
            final List<Objective> objectives = trial.getProblem().getObjectives();
            final int numObjectives = objectives.size();
            final List<Double> satisfactions = new ArrayList<>(numObjectives);

            // collect the list of each satisfaction
            for (final Objective objective : objectives) {
                final double satisfaction = trial.getSatisfaction(objective);
                satisfactions.add(satisfaction);
            }
            Collections.sort(satisfactions);		// sort satisfactions from worst to best

            // weight each satisfaction with most weight for the worst satisfaction and exponentially decreasing from there
            double weightedSum = 0.0;
            double weight = 1.0;
            final Iterator<Double> satisfactionIter = satisfactions.iterator();
            while (satisfactionIter.hasNext()) {
                final double satisfaction = satisfactionIter.next();
                weightedSum += weight * satisfaction;
                weight *= biasWeight;	// weight the worst satisfactions most
            }

            // make sure we do this at least once and then cache it
            if (totalWeight == 0.0) {
                totalWeight = (1.0 - Math.pow(biasWeight, numObjectives)) / (1.0 - biasWeight);
            }

            // generate the overall satisfaction which is scaled from 0 to 1
            final double totalSatisfaction = weightedSum / totalWeight;
            trial.setSatisfaction(totalSatisfaction);

            if (totalSatisfaction == bestSatisfaction) {
                optimalSolutions.add(trial);
                eventProxy.foundNewOptimalSolution(this, optimalSolutions, trial);
            } else if (totalSatisfaction > bestSatisfaction) {
                bestSatisfaction = totalSatisfaction;
                optimalSolutions.clear();
                optimalSolutions.add(trial);
                eventProxy.foundNewOptimalSolution(this, optimalSolutions, trial);
            }
        }
    }
}
