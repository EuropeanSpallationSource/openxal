/*
 * CorrelationTester.java
 *
 * Created on May 1, 2003, 12:03 PM
 */
package xal.tools.correlator;

/**
 * CorrelationTester is a helper class for wrapping and applying the correlation
 * filter. Many classes may reference the correlation filter, but rather than
 * having each of them monitor whether the filter has changed, correlation
 * tester holds the current filter in use and thus wraps the correlation filter.
 * Other classes use the this tester in rather than referencing the filter
 * directly.
 *
 * @author tap
 */
public class CorrelationTester<RecordType> {

    private volatile int fullCount;
    private volatile CorrelationFilter<RecordType> filter;

    /**
     * Creates a new instance of CorrelationTester
     */
    public CorrelationTester(final int fullCount, final CorrelationFilter<RecordType> aFilter) {
        this.fullCount = fullCount;
        setFilter(aFilter);
    }

    /**
     * Set the full count.
     *
     * @param newCount The number of channels to monitor and correlate.
     */
    public void setFullCount(final int newCount) {
        fullCount = newCount;
    }

    /**
     * Get the correlation filter.
     *
     * @return The correlation filter.
     */
    public CorrelationFilter<RecordType> getFilter() {
        return filter;
    }

    /**
     * Set the correlation filter.
     *
     * @param newFilter The new filter to apply to correlations.
     */
    public void setFilter(final CorrelationFilter<RecordType> newFilter) {
        if (newFilter == null) {
            filter = CorrelationFilterFactory.<RecordType>defaultFilter();
        } else {
            filter = newFilter;
        }
    }

    /**
     * Determine whether the specified correlation passes the filter's test.
     *
     * @param correlation The correlation to test.
     * @return true if the correlation passes the filter test, and false if not.
     */
    public boolean accept(final Correlation<RecordType> correlation) {
        return filter.accept(correlation, fullCount);
    }
}
