//
// LeastSquareParameterFitting.java
// 
//
// Created by Tom Pelaia on 12/13/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.math;

import xal.tools.math.differential.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Least Squares parameter fitting tool which takes a differentiable operation
 * as a model.
 */
public class LeastSquareParameterFitting implements Runnable {

    /**
     * independent (sampled) variable (typically "x")
     */
    public static final DifferentiableVariable INDEPENDENT_VARIABLE;

    /**
     * dependent (measured) variable (typically "y")
     */
    private static final DifferentiableVariable DEPENDENT_VARIABLE;

    /**
     * sigma variable representing the statistical measurement error
     */
    private static final DifferentiableVariable SIGMA_VARIABLE;

    /**
     * parameters to fit
     */
    private final BoundedDifferentiableVariable[] parameters;

    /**
     * model for which to fit the data
     */
    private final DifferentiableOperation model;

    /**
     * data samples with which to fit the model
     */
    private final List<DataSample> dataSample;

    /**
     * maximum number of evaluations to perform for each run
     */
    private int maxEvaluations;

    /**
     * minimizer
     */
    private DifferentiableOperationMinimizer minimizer;

    // static initialization
    static {
        INDEPENDENT_VARIABLE = new DifferentiableVariable("x", 0.0);
        DEPENDENT_VARIABLE = new DifferentiableVariable("y", 0.0);
        SIGMA_VARIABLE = new DifferentiableVariable("\u03C3", 0.0);
    }

    /**
     * Constructor
     */
    public LeastSquareParameterFitting(final DifferentiableOperation model, final BoundedDifferentiableVariable... parameters) {
        this.model = model;
        this.parameters = parameters;
        dataSample = new ArrayList<>();

        maxEvaluations = 5;
    }

    /**
     * Add a data sample including statistical error (either all or none must
     * specify error)
     *
     * @param x independent variable value
     * @param y dependent (measured) variable value
     * @param sigma statistical measurement error
     */
    public void addSample(final double x, final double y, final double sigma) {
        dataSample.add(new DataSample(x, y, sigma));
        minimizer = null;
    }

    /**
     * Add a data sample without specifying statistical error (either all or
     * none must specify error)
     *
     * @param x independent variable value
     * @param y dependent (measured) variable value
     */
    public void addSample(final double x, final double y) {
        addSample(x, y, 1.0);
    }

    /**
     * clear samples
     */
    public void clear() {
        dataSample.clear();
        minimizer = null;
    }

    /**
     * get the maximum number of model evaluations for a run
     */
    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    /**
     * set the maximum number of model evaluations for a run
     */
    public void setMaxEvaluations(final int evaluations) {
        maxEvaluations = evaluations;
        if (minimizer != null) {
            minimizer.setMaxEvaluations(evaluations);
        }
    }

    /**
     * run the minimizer to find the best parameter fit for the model to the
     * samples
     */
    @Override
    public void run() throws java.lang.IllegalStateException {
        if (dataSample.size() < parameters.length) {
            throw new IllegalStateException("At least " + parameters.length + " samples are needed, but only " + dataSample.size() + " were supplied.");
        }

        if (minimizer == null) {
            minimizer = createMinimizer();
        }

        minimizer.run();
    }

    /**
     * set the maximum evaluations and run
     */
    public void runFor(final int evaluations) {
        setMaxEvaluations(evaluations);
        run();
    }

    /**
     * get the penalty for the best fit found
     */
    public double getFitPenalty() {
        if (minimizer != null) {
            return minimizer.getBestPenalty();
        } else {
            throw new IllegalStateException("Can't get fit penalty since the fitting has either been cleared or not run.");
        }
    }

    /**
     * get the value of the parameter for the best fit found
     */
    public double getFitValueForParameter(final BoundedDifferentiableVariable parameter) {
        if (minimizer != null) {
            return minimizer.getBestVariableValue(parameter);
        } else {
            throw new IllegalStateException("Can't get fit parameter values since the fitting has either been cleared or not run.");
        }
    }

    /**
     * Construct the minimizer
     */
    private DifferentiableOperationMinimizer createMinimizer() {
        final List<BoundedDifferentiableVariable> parameters = new ArrayList<>(this.parameters.length);
        for (final BoundedDifferentiableVariable parameter : this.parameters) {
            parameters.add(parameter);
        }

        final DifferentiableOperation penaltyOperation = createPenaltyOperation();
        final DifferentiableOperationMinimizer minimizer = new DifferentiableOperationMinimizer(penaltyOperation, parameters);
        minimizer.setMaxEvaluations(maxEvaluations);
        return minimizer;
    }

    /**
     * Construct the penalty operation
     */
    private DifferentiableOperation createPenaltyOperation() {
        // ( ( y - f(x, param) ) / sigma )^2
        final DifferentiableOperation statisticalError = DEPENDENT_VARIABLE.minus(model).over(SIGMA_VARIABLE).pow(2);

        // ( y - f(x, param) )^2
        final DifferentiableOperation basicError = DEPENDENT_VARIABLE.minus(model).pow(2);

        // initialize penalty to zero
        DifferentiableOperation penaltyOperation = DifferentiableOperation.getConstant(0.0);
        for (final DataSample sample : dataSample) {
            final Map<DifferentiableVariable, DifferentiableOperation> substitution = new HashMap<>(dataSample.size());
            substitution.put(INDEPENDENT_VARIABLE, DifferentiableOperation.getConstant(sample.x));
            substitution.put(DEPENDENT_VARIABLE, DifferentiableOperation.getConstant(sample.y));
            substitution.put(SIGMA_VARIABLE, DifferentiableOperation.getConstant(sample.sigma));
            final DifferentiableOperation errorOperation = sample.sigma == 1.0 ? basicError : statisticalError;
            penaltyOperation = penaltyOperation.plus(errorOperation.copyWithSubstitutions(substitution));
        }

        return penaltyOperation;
    }
}

/**
 * data sample
 */
class DataSample {

    /**
     * value of the independent variable
     */
    public final double x;

    /**
     * (measured) value of the dependent variable
     */
    public final double y;

    /**
     * statistical measurement error
     */
    public final double sigma;

    /**
     * Primary Constructor
     */
    public DataSample(final double x, final double y, final double sigma) {
        this.x = x;
        this.y = y;
        this.sigma = sigma;
    }

    /**
     * Constructor with non-zero constant statistical error when it is not known
     * for any sample
     */
    public DataSample(final double x, final double y) {
        this(x, y, 1.0);
    }
}
