//
//  LinearFit.java
//  xal
//
//  Created by Thomas Pelaia on 10/29/04.
//  Copyright 2004 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.fit;

import xal.tools.statistics.*;

/**
 * Fit a set of x,y data pairs to a line where
 * <code>y = slope * x + intercept</code>.
 */
public class LinearFit {

    protected final MutableUnivariateStatistics xStats;
    protected final MutableUnivariateStatistics yStats;
    protected final MutableUnivariateStatistics xxStats;
    protected final MutableUnivariateStatistics xyStats;
    protected final MutableUnivariateStatistics yyStats;

    protected boolean needsUpdate;
    protected double slope;
    protected double intercept;
    protected double correlationCoefficient;

    /**
     * Constructor
     */
    public LinearFit() {
        needsUpdate = false;
        slope = Double.NaN;
        intercept = Double.NaN;
        correlationCoefficient = Double.NaN;

        xStats = new MutableUnivariateStatistics();
        yStats = new MutableUnivariateStatistics();
        xxStats = new MutableUnivariateStatistics();
        xyStats = new MutableUnivariateStatistics();
        yyStats = new MutableUnivariateStatistics();
    }

    /**
     * Add a new x,y pair.
     */
    public synchronized void addSample(final double x, final double y) {
        xStats.addSample(x);
        yStats.addSample(y);
        xxStats.addSample(x * x);
        xyStats.addSample(x * y);
        yyStats.addSample(y * y);

        needsUpdate = true;
    }

    /**
     * Get the slope performing a fit if needed.
     *
     * @return the fitted slope
     */
    public synchronized double getSlope() {
        performFitIfNeeded();

        return slope;
    }

    /**
     * Get the intercept performing a fit if needed.
     *
     * @return the fitted intercept
     */
    public synchronized double getIntercept() {
        performFitIfNeeded();

        return intercept;
    }

    /**
     * Get the correlation coefficient.
     *
     * @return the correlation coefficient.
     */
    public synchronized double getCorrelationCoefficient() {
        performFitIfNeeded();

        return correlationCoefficient;
    }

    /**
     * Estimate the dependent variable (y) given the independent variable (x).
     *
     * @param x the independent variable
     * @return the dependent variable
     */
    public synchronized double estimateY(final double x) {
        performFitIfNeeded();

        return slope * x + intercept;
    }

    /**
     * Get the mean square error of the y value with respect to the fitted line.
     * The square root of this number gives an indication of the uncertainty in
     * y value estimates under certain assumptions about the error distribution.
     *
     * @return the mean square error of the y value with respect to the fitted
     * line
     */
    public synchronized double getMeanSquareOrdinateError() {
        performFitIfNeeded();

        final double xyMean = xyStats.mean();
        final double xxMean = xxStats.mean();
        final double yyMean = yyStats.mean();

        return yyMean - 2 * slope * xyMean + slope * slope * xxMean - intercept * intercept;
    }

    /**
     * Perform a linear fit if the the fit needs to be updated due to newly
     * added data.
     */
    synchronized protected void performFitIfNeeded() {
        if (needsUpdate) {
            performFit();
        }
    }

    /**
     * Calculate the slope and intercept.
     */
    synchronized protected void performFit() {
        final double xMean = xStats.mean();
        final double yMean = yStats.mean();
        final double xyMean = xyStats.mean();
        final double xxMean = xxStats.mean();
        final double yyMean = yyStats.mean();

        slope = (xyMean - xMean * yMean) / (xxMean - xMean * xMean);
        intercept = yMean - slope * xMean;
        correlationCoefficient = (xyMean - xMean * yMean) / Math.sqrt((xxMean - xMean * xMean) * (yyMean - yMean * yMean));

        needsUpdate = false;
    }

    /**
     * Generate a string representation of the linear equation.
     *
     * @return a string representation of the linear equation
     */
    @Override
    public synchronized String toString() {
        performFitIfNeeded();

        StringBuilder buffer = new StringBuilder();
        buffer.append("y = ").append(slope).append(" * x + ").append(intercept);
        buffer.append("\nr = ").append(correlationCoefficient);
        buffer.append("\n<x> = ").append(xStats.mean()).append(", <y> = ").append(yStats.mean());
        buffer.append("\n<xx> = ").append(xxStats.mean()).append(", <xy> = ").append(xyStats.mean()).append(", <yy> = ").append(yyStats.mean());
        return buffer.toString();
    }
}
