//
//  RunningWeightedStatistics.java
//  xal
//
//  Created by Tom Pelaia on 7/6/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.statistics;


/** Calculate running statistics using weighted averaging */
public class RunningWeightedStatistics extends UnivariateStatistics {	
	/** weight for new samples */
	protected final double asymptoticWeight;
	
	/** population beyond which we should use the asymptotic weight */
	protected final int asymptoticPopulation;
	
	
	/**
	 * Constructor
	 * @param weight  asymptotic weight to apply to new samples in large populations
	 */
	public RunningWeightedStatistics( final double weight ) {
		super();
		asymptoticWeight = weight;
		asymptoticPopulation = (int)( 1.0 / weight );
	}
	
	
	/** Clear the samples  */
	public void clear() {
		population = 0;
		mean = 0;
		meanSquare = 0;
	}
	
	
	/**
	 * Get the current weight.
	 * @return the current weight
	 */
	protected final double getWeight() {
		return getWeight(population );
	}
	
	
	/**
	 * Get the weight to apply to the next new sample given the specified population.
	 * When we first start adding samples, we must trust the newest samples most and gradually shift trust to the older samples.
	 * Ultimately we will trust new samples at least at the asymptotic weight.
	 */
	protected final double getWeight( final double population ) {
		return population <= asymptoticPopulation ? 1.0D / population : asymptoticWeight;
	}
	
	
	/**
	 * Add a new sample measurement.
	 * @param value  The new sample measurement
	 */
	public void addSample( final double value ) {
		final double weight = getWeight( ++population );
		mean = weight * value + ( 1 - weight ) * mean;
		meanSquare = weight * value * value + ( 1 - weight ) * meanSquare;
	}
	
	
	/**
	 * Get the variance of the mean from the actual value.
	 * @return   the variance of the mean from the actual value
	 */
        @Override
	public double varianceOfMean() {
		return variance() * getWeight();
	}
	
	
	/**
	 * Get the variance of the mean from the actual value assuming the supporting data is a random subset of all the data.
	 * @return   the sample variance of the mean
	 */
        @Override
	public double sampleVarianceOfMean() {
		return sampleVariance() * getWeight();
	}
}
