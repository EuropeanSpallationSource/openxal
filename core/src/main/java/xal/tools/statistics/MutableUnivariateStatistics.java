/*
 *  MutableUnivariateStatistics.java
 *
 *  Created on October 7, 2002, 8:33 AM
 */
package xal.tools.statistics;

/**
 * MutableUnivariateStatistics calculates statistics of a series of measurements. The
 * statistics can be updated with each additional measurement. Only simple statistics are
 * generated (mean and standard deviation).
 *
 * @author   tap
 */
public class MutableUnivariateStatistics extends UnivariateStatistics {
	/** Constructor with no samples. */
	public MutableUnivariateStatistics() {
		this( 0, 0, 0 );
	}


	/**
	 * Copy constructor
	 *
	 * @param stats  the statistics to copy
	 */
	public MutableUnivariateStatistics( final UnivariateStatistics stats ) {
		this(stats.population, stats.mean, stats.meanSquare );
	}


	/**
	 * Constructor which scales the samples from an existing set of statistics.
	 *
	 * @param stats  the statistics against which to scale
	 * @param scale  factor which is used to scale the copied statistics
	 */
	public MutableUnivariateStatistics( final UnivariateStatistics stats, final double scale ) {
		this(stats.population, scale * stats.mean, scale * scale * stats.meanSquare );
	}


	/**
	 * Primary Constructor with a starting set of statistics.
	 *
	 * @param size           the number of samples
	 * @param average        the mean
	 * @param averageSquare  the mean square of the samples
	 */
	public MutableUnivariateStatistics( final int size, final double average, final double averageSquare ) {
		super( size, average, averageSquare );
	}


	/** Clear the samples  */
	public void clear() {
		population = 0;
		mean = 0;
		meanSquare = 0;
	}


	/**
	 *  Add a new sample measurement.
	 *
	 * @param value  The new sample measurement
	 */
	public void addSample( final double value ) {
		final double weight = 1.0D / ++population;
		mean = weight * value + ( 1 - weight ) * mean;
		meanSquare = weight * value * value + ( 1 - weight ) * meanSquare;
	}


	/**
	 *  Merge in samples from other statistics.
	 *
	 * @param stats  the statistics which should be merged into these statistics
	 */
	public void addSamples( final UnivariateStatistics stats ) {
		population += stats.population;
		final double weight = stats.population / population;

		mean = ( 1 - weight ) * mean + weight * stats.mean;
		meanSquare = ( 1 - weight ) * meanSquare + weight * stats.meanSquare;
	}


	/**
	 * Replace an old sample with a fresh sample. This is useful when updating statistics on
	 * circular buffers.
	 *
	 * @param oldValue  the sample measurement to replace
	 * @param newValue  the new sample measurement
	 */
	public void replaceSample( final double oldValue, final double newValue ) {
		final double weight = 1.0D / population;
		mean += weight * ( newValue - oldValue );
		meanSquare += weight * ( newValue * newValue - oldValue * oldValue );
	}


	/**
	 * Remove a sample from the statistics
	 *
	 * @param value  the sample measurement to remove
	 */
	public void removeSample( final double value ) {
		final double weight = 1.0D / --population;
		mean += weight * ( mean - value );
		meanSquare += weight * ( meanSquare - value * value );
	}


	/**
	 *  Modify the statistics for data of another scale.  For example, if you
	 *  took data with one set of units but want results in another.  This
	 *  method should be used with caution as it changes the scale of the
	 *  data and new data should only be added if it uses the new scale.
	 *
	 * @param scale  the amount by which to scale the existing sample measurements
	 */
	public void scaleData( final double scale ) {
		mean *= scale;
		meanSquare *= scale * scale;
	}
}

