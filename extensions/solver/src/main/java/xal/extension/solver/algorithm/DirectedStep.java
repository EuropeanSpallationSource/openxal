//
//  DirectedStep.java
//  xal
//
//  Created by Thomas Pelaia on 2/13/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.solver.algorithm;

import xal.extension.solver.*;
import xal.extension.solver.solutionjudge.*;
import xal.extension.solver.hint.*;

import java.util.*;

/**
 * Based on the acceleration-step of Forsythe and Motzkin
 */
public class DirectedStep extends SearchAlgorithm {

    /**
     * number of steps along acceleration search
     */
    final int numScaleSteps = 10;

    /**
     * domain for search steps
     */
    private ExcursionHint searchStepDomain;

    /**
     * The current best point.
     */
    private Trial bestSolution;

    /**
     * Last origin trial
     */
    private Trial lastOriginTrial;

    /**
     * Constructor
     */
    public DirectedStep() {
    }

    /**
     * reset the algorithm for searching from scratch
     */
    @Override
    public void reset() {
        final ExcursionHint excursionHint = (ExcursionHint) problem.getHint(ExcursionHint.TYPE);
        searchStepDomain = excursionHint != null ? excursionHint : ExcursionHint.getFractionalExcursionHint(0.001);
    }

    /**
     * Return the label for a search algorithm.
     *
     * @return The trial point.
     */
    @Override
    public String getLabel() {
        return "Directed Step";
    }

    /**
     * Calculate the next few trial points.
     */
    @Override
    public void performRun(final AlgorithmSchedule algorithmSchedule) {
        if (algorithmSchedule.shouldStop()) {
            return;
        }
        try {
            //LOGGER.log(Level.INFO,  "initial solution:  " + bestSolution );sx
            if (lastOriginTrial != bestSolution) {		// no point in repeating the same result
                lastOriginTrial = bestSolution;
                final Trial bestTrial = performAcceleratedSearch(bestSolution);
            }
            //LOGGER.log(Level.INFO,  "best solution:  " + bestTrial + "\n\n" );
        } catch (RunTerminationException exception) {
        }
    }

    /**
     * Perform an accelerated search.
     */
    protected Trial performAcceleratedSearch(final Trial originTrial) {
        final Trial secondTrial = performGradientAndLinearSearch(originTrial);
        //LOGGER.log(Level.INFO,  "second trial:  " + secondTrial );
        if (secondTrial != originTrial) {
            final Trial thirdTrial = performGradientAndLinearSearch(secondTrial);
            //LOGGER.log(Level.INFO,  "third trial:  " + thirdTrial );
            if (thirdTrial != secondTrial) {
                double[] vector = calculateVector(thirdTrial.getTrialPoint(), originTrial.getTrialPoint());
                return searchAlongGradient(vector, thirdTrial);
            } else {
                return secondTrial;
            }
        } else {
            return originTrial;
        }
    }

    /**
     * Perform a gradient calculation followed by a linear search along the
     * gradient.
     */
    protected Trial performGradientAndLinearSearch(final Trial originTrial) {
        final double[] gradient = calculateGradient(originTrial);
        return searchAlongGradient(gradient, originTrial);
    }

    /**
     * Calculate the gradient for the specified point
     *
     * @return the gradient at the specified point
     */
    protected double[] calculateGradient(final Trial originTrial) {
        final List<Variable> variables = problem.getVariables();
        final Map<Variable, Number> valueMap = new HashMap<>(originTrial.getTrialPoint().getValueMap());
        final double[] gradient = new double[variables.size()];
        final double originSatisfaction = getSatisfaction(originTrial);
        int index = 0;
        for (Variable variable : variables) {
            final double originValue = valueMap.get(variable).doubleValue();
            final double[] trialRange = searchStepDomain.getRange(originValue, variable);

            final double lowerValue = trialRange[0];
            valueMap.put(variable, lowerValue);
            final Trial lowerTrial = evaluateTrialPoint(new TrialPoint(valueMap));
            final double lowerSatisfaction = getSatisfaction(lowerTrial);

            final double upperValue = trialRange[1];
            valueMap.put(variable, upperValue);
            final Trial upperTrial = evaluateTrialPoint(new TrialPoint(valueMap));
            final double upperSatisfaction = getSatisfaction(upperTrial);

            gradient[index++] = (upperSatisfaction - lowerSatisfaction) / (upperValue - lowerValue);
            valueMap.put(variable, originValue);
        }

        //LOGGER.log(Level.INFO,  "Gradient:  " + ArrayTool.asString( gradient ) );
        return gradient;
    }

    /**
     * Calculate the vector from the origin point to the target point
     *
     * @return the vector from the origin point to the target point
     */
    protected double[] calculateVector(final TrialPoint originPoint, final TrialPoint targetPoint) {
        final List<Variable> variables = problem.getVariables();
        final Map<Variable, Number> valueMap = new HashMap<>(variables.size());
        final double[] vector = new double[variables.size()];
        int index = 0;
        for (Variable variable : variables) {
            final double originValue = originPoint.getValue(variable);
            final double targetValue = targetPoint.getValue(variable);
            vector[index++] = targetValue - originValue;
        }

        return vector;
    }

    /**
     * Search along the gradient from the origin point.
     */
    protected Trial searchAlongGradient(final double[] gradient, final Trial originTrial) {
        final List<Variable> variables = problem.getVariables();

        final TrialPoint originPoint = originTrial.getTrialPoint();
        double bestSatisfaction = getSatisfaction(originTrial);
        double minScale = 0.0;
        double maxScale = Double.MAX_VALUE;
        for (int index = 0; index < gradient.length; index++) {
            final Variable variable = variables.get(index);
            final double limit = gradient[index] > 0 ? variable.getUpperLimit() : variable.getLowerLimit();
            if (gradient[index] != 0.0 && !Double.isNaN(gradient[index])) {
                maxScale = Math.min(maxScale, (limit - originPoint.getValue(variable)) / gradient[index]);
            }
        }
        //LOGGER.log(Level.INFO,  "max scale:  " + maxScale );

        final QuadraticMaximumFinder finder = new QuadraticMaximumFinder();
        Trial bestTrial = originTrial;
        double bestScale = 0.0;
        for (int sindex = 0; sindex < numScaleSteps; sindex++) {
            if (minScale != bestScale) {
                final double scale = (minScale + 7 * bestScale) / 8;
                final TrialPoint trialPoint = trialPointAlongGradient(gradient, originPoint, scale, variables);
                final Trial trial = evaluateTrialPoint(trialPoint);
                final double satisfaction = getSatisfaction(trial);
                //LOGGER.log(Level.INFO,  "scale:  " + scale + ", trialPoint:  " + trialPoint + ", satisfaction:  " + satisfaction );
                finder.add(scale, satisfaction);
                if (satisfaction > bestSatisfaction) {
                    maxScale = bestScale;
                    bestScale = scale;
                    bestSatisfaction = satisfaction;
                    bestTrial = trial;
                } else {
                    minScale = scale;
                }
            }
            if (maxScale != bestScale) {
                final double scale = (maxScale + 7 * bestScale) / 8;
                final TrialPoint trialPoint = trialPointAlongGradient(gradient, originPoint, scale, variables);
                final Trial trial = evaluateTrialPoint(trialPoint);
                final double satisfaction = getSatisfaction(trial);
                finder.add(scale, satisfaction);
                //LOGGER.log(Level.INFO,  "scale:  " + scale + ", trialPoint:  " + trialPoint + ", satisfaction:  " + satisfaction );
                if (satisfaction > bestSatisfaction) {
                    minScale = bestScale;
                    bestScale = scale;
                    bestSatisfaction = satisfaction;
                    bestTrial = trial;
                } else {
                    maxScale = scale;
                }
            }
            if (finder.hasMaximum()) {
                final double scale = finder.getOptimalX();
                //LOGGER.log(Level.INFO,  "Finder maximum:  " + scale + ", minScale:  " + minScale + ", maxScale:  " + maxScale );
                if (scale < maxScale && scale > minScale) {
                    final TrialPoint trialPoint = trialPointAlongGradient(gradient, originPoint, scale, variables);
                    final Trial trial = evaluateTrialPoint(trialPoint);
                    final double satisfaction = getSatisfaction(trial);
                    if (satisfaction > bestSatisfaction) {
                        //LOGGER.log(Level.INFO,  "Got a better point with the quadratic fit:  " + satisfaction );
                        if (scale > bestScale) {
                            minScale = bestScale;
                        } else {
                            maxScale = bestScale;
                        }
                        bestScale = scale;
                        bestSatisfaction = satisfaction;
                        bestTrial = trial;
                    }
                }
            }
        }

        return bestTrial;
    }

    /**
     * Get a new trial point along the gradient.
     */
    protected static TrialPoint trialPointAlongGradient(final double[] gradient, final TrialPoint originPoint, final double scale, List<Variable> variables) {
        final Map<Variable, Number> valueMap = new HashMap<>(variables.size());
        for (int index = 0; index < gradient.length; index++) {
            final Variable variable = variables.get(index);

            // gradient component can be NaN if the corresponding variable's lower and upper limits match (e.g. when the variable isn't really variable)
            final double delta = Double.isNaN(gradient[index]) ? 0.0 : gradient[index] * scale;
            final double value = originPoint.getValue(variable) + delta;
            final double lowerLimit = variable.getLowerLimit();
            final double upperLimit = variable.getUpperLimit();
            final double trialValue = value < lowerLimit ? lowerLimit : value > upperLimit ? upperLimit : value;
            valueMap.put(variable, trialValue);
        }

        return new TrialPoint(valueMap);
    }

    /**
     * Get the satisfaction converting NaN to 0.0 if necessary
     */
    private static double getSatisfaction(final Trial trial) {
        final double rawSatisfaction = trial.getSatisfaction();
        return Double.isNaN(rawSatisfaction) ? 0.0 : rawSatisfaction;
    }

    /**
     * Get the minimum number of evaluations per run. Subclasses may want to
     * override this method.
     *
     * @return the minimum number of evaluation per run.
     */
    @Override
    public int getMinEvaluationsPerRun() {
        int minEvals = problem != null ? 4 * problem.getVariables().size() + 3 * 2 * numScaleSteps : 0;
        return minEvals;
    }

    /**
     * Returns the global rating which in an integer between 0 and 10.
     *
     * @return The global rating for this algorithm.
     */
    @Override
    public int globalRating() {
        return 5;
    }

    /**
     * Returns the local rating which is an integer between 0 and 10.
     *
     * @return The local rating for this algorithm.
     */
    @Override
    public int localRating() {
        return 5;
    }

    /**
     * Handle a message that an algorithm is available.
     *
     * @param source The source of the available algorithm.
     */
    public void algorithmAvailable(SearchAlgorithm source) {
    }

    /**
     * Handle a message that an algorithm is not available.
     *
     * @param source The source of the available algorithm.
     */
    public void algorithmUnavailable(SearchAlgorithm source) {
    }

    /**
     * Handle a message that a trial has been scored.
     *
     * @param trial The trial that was scored.
     * @param schedule the schedule providing this event
     */
    @Override
    public void trialScored(AlgorithmSchedule schedule, Trial trial) {
    }

    /**
     * Handle a message that a trial has been vetoed.
     *
     * @param trial The trial that was vetoed.
     * @param schedule the schedule providing this event
     */
    @Override
    public void trialVetoed(AlgorithmSchedule schedule, Trial trial) {
    }

    /**
     * Handle a message that a new optimal solution has been found.
     *
     * @param source The source of the new optimal solution.
     * @param solutions The list of solutions.
     * @param solution The new optimal solution.
     */
    @Override
    public void foundNewOptimalSolution(SolutionJudge source, List<Trial> solutions, Trial solution) {
        bestSolution = solution;
    }

    /**
     * Locate the maximum for a quadratic specified by: y = ax^2 + bx + c
     */
    private class QuadraticMaximumFinder {

        protected final List<Sample> samples;
        protected double curvature;
        protected double slope;
        protected boolean needsUpdate;

        /**
         * Constructor
         */
        public QuadraticMaximumFinder() {
            samples = new ArrayList<>(3);
            needsUpdate = true;
        }

        /**
         * add a sample
         */
        public final void add(final double x, final double y) {
            add(new Sample(x, y));
        }

        /**
         * add a sample
         */
        private void add(final Sample sample) {
            samples.add(sample);
            if (samples.size() > 3) {
                samples.remove(0);
            }
            needsUpdate = true;
        }

        /**
         * perform fit
         */
        private void performFit() {
            if (samples.size() != 3) {
                curvature = Double.NaN;
                slope = Double.NaN;
            } else {
                final double x0 = samples.get(0).getX();
                final double x1 = samples.get(1).getX();
                final double x2 = samples.get(2).getX();
                final double y0 = samples.get(0).getY();
                final double y1 = samples.get(1).getY();
                final double y2 = samples.get(2).getY();

                final double dx01 = x0 - x1;
                final double dx21 = x2 - x1;
                final double dy01 = y0 - y1;
                final double dy21 = y2 - y1;

                curvature = (dy21 * dx01 - dy01 * dx21) / ((x2 * x2 - x1 * x1) * dx01 - (x0 * x0 - x1 * x1) * dx21);
                slope = (y2 - y1 - curvature * (x2 * x2 - x1 * x1)) / dx21;

                //				LOGGER.log(Level.INFO,  "Calculating curvature for samples:  " + samples );
                //				LOGGER.log(Level.INFO,  "Curvature:  " + curvature + ", slope:  " + slope );
            }

            needsUpdate = false;
        }

        /**
         * perform fit if necessary
         */
        private void performFitIfNeeded() {
            if (needsUpdate) {
                performFit();
            }
        }

        /**
         * determine if the equation has a maximum
         */
        public boolean hasMaximum() {
            performFitIfNeeded();

            return curvature < 0.0;
        }

        /**
         * Calculate the optimal value of X which provides a maximum of the
         * quadratic
         */
        public double getOptimalX() {
            performFitIfNeeded();
            return -slope / (2.0 * curvature);
        }

        /**
         * Evaluation sample
         */
        private class Sample {

            protected final double x;
            protected final double y;

            /**
             * Constructor
             */
            public Sample(final double x, final double y) {
                this.x = x;
                this.y = y;
            }

            /**
             * get x
             */
            public final double getX() {
                return this.x;
            }

            /**
             * get y
             */
            public final double getY() {
                return this.y;
            }

            /**
             * description of this sample
             */
            @Override
            public final String toString() {
                return "x: " + this.x + ", y: " + this.y;
            }
        }
    }
}
