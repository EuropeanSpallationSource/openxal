/*
 * CorrelationFilterFactor.java
 *
 * Created on August 1, 2002, 4:54 PM
 */
package xal.tools.correlator;

/**
 *
 * @author tap
 */
public class CorrelationFilterFactory {

    public static <T> CorrelationFilter<T> defaultFilter() {
        return maxMissingFilter(0);
    }

    /**
     * accept correlations with no more than maxMissing records
     */
    public static <T> CorrelationFilter<T> maxMissingFilter(final int maxMissing) {
        return new CorrelationFilter<T>() {
            public boolean accept(final Correlation<T> correlation, final int fullCount) {
                return correlation.numRecords() >= fullCount - maxMissing;
            }
        };
    }

    /**
     * accept correlations with at least minCount records
     */
    public static <T> CorrelationFilter<T> minCountFilter(final int minCount) {
        return new CorrelationFilter<T>() {
            @Override
            public boolean accept(final Correlation<T> correlation, final int fullCount) {
                return correlation.numRecords() >= minCount;
            }
        };
    }

    /**
     * Convert a record filter to a correlation filter. This is useful when
     * stacking correlators and a correlation of one correlator is the record of
     * another.
     */
    public static <T> CorrelationFilter<T> correlationFilter(final RecordFilter<Correlation<T>> recordFilter) {
        return new CorrelationFilter<T>() {
            @Override
            public boolean accept(final Correlation<T> correlation, final int fullCount) {
                return recordFilter.accept(correlation);
            }
        };
    }

    /**
     * Creates a new instance of CorrelationFilterFactor
     */
    protected CorrelationFilterFactory() {
    }
}
