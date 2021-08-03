/*
 * BinAgent.java
 *
 * Created on June 27, 2002, 8:27 AM
 */
package xal.tools.correlator;

import xal.tools.statistics.MutableUnivariateStatistics;
import xal.tools.messaging.MessageCenter;

import java.util.*;

/**
 * BinAgent is the class that gathers correlated events. It is assigned a
 * timestamp. The agent listens for events and any event that is within the
 * timespan of timestamp, is accepted. When the number of accepted channels
 * equals the number of all channels monitored by the correlator, the bin agent
 * wraps all the gathered event records into a correlation object and notifies
 * the correlator world that it has found a correlation. The bin then resets
 * itself.
 *
 * @author tap
 */
public class BinAgent<R> implements BinUpdate<R>, StateNotice<R> {

    /**
     * time spread for current correlation
     */
    private double earliestTimestamp;
    private double latestTimestamp;
    /**
     * time statistics for current correlation
     */
    private MutableUnivariateStatistics timeStatistics;
    /**
     * time window restriction
     */
    private double timespan;
    /**
     * table of correlated records
     */
    private Map<String, R> recordTable;
    /**
     * proxy for posting bin events
     */
    private BinListener<R> binProxy;
    /**
     * internal message center for the correlator
     */
    private MessageCenter localCenter;
    private CorrelationTester<R> correlationTester;
    /**
     * true if this agent is prepared to receive events and false if not.
     */
    private boolean enabled;

    /**
     * Creates new BinAgent
     */
    public BinAgent(final MessageCenter localCenter, final CorrelationTester<R> tester) {
        enabled = false;
        correlationTester = tester;
        this.localCenter = localCenter;
        timeStatistics = new MutableUnivariateStatistics();
        recordTable = new HashMap<>();

        registerEvents();
    }

    /**
     * Register events for this bin agent
     */
    // need cast to get the proxy using Generics
    @SuppressWarnings("unchecked")    
    public synchronized void registerEvents() {
        /**
         * Register this bin agent as a poster of bin events
         */
        binProxy = (BinListener<R>) localCenter.registerSource(this, BinListener.class);
    }

    /**
     * Prepare itself for disposal.
     */
    synchronized void shutdown() {
        /**
         * Unregister this bin agent as a poster of correlation notices
         */
        localCenter.removeSource(this, BinListener.class);
    }

    /**
     * Forget all events
     */
    public synchronized void reset() {
        enabled = false;
        binProxy.willReset(this);
        recordTable.clear();
        timeStatistics = new MutableUnivariateStatistics();
    }

    /**
     * Forget all events and set the timestamp to the supplied one. This is used
     * when a bin is recycled.
     */
    public synchronized void resetWithRecord(final String name, final R record, final double timestamp) {
        reset();
        timeStatistics.addSample(timestamp);
        earliestTimestamp = timestamp;
        latestTimestamp = timestamp;
        enabled = true;
        newEvent(name, record, timestamp);
    }

    /**
     * Set the maximum time span allowed among the records collected.
     */
    public synchronized void setTimespan(double timespan) {
        this.timespan = timespan;
    }

    /**
     * record the event record and handle any complete correlation sets found
     */
    private synchronized void addRecord(final String name, final R record, final double timestamp) {
        recordTable.put(name, record);
        timeStatistics.addSample(timestamp);
        final Correlation<R> correlation = new Correlation<>(recordTable, timeStatistics);
        if (correlationTester.accept(correlation)) {
            binProxy.newCorrelation(this, correlation);
        }
    }

    /**
     * Remove the record corresponding to the specified name. This method is
     * used when a channel is removed and thus the associated record is no
     * longer relevant.
     */
    private void removeRecord(String name) {
        recordTable.remove(name);
    }

    /**
     * Implement BinUpdate interface
     */
    @Override
    public synchronized void newEvent(final String name, final R record, final double timestamp) {
        if (!enabled || recordTable.containsKey(name)) {
            return;
        }

        double earlyRange = Math.abs(earliestTimestamp - timestamp);
        double lateRange = Math.abs(latestTimestamp - timestamp);
        double range = Math.max(earlyRange, lateRange);

        if (range < timespan) {
            addRecord(name, record, timestamp);
            earliestTimestamp = Math.min(timestamp, earliestTimestamp);
            latestTimestamp = Math.max(timestamp, latestTimestamp);
        }
    }

    /**
     * Implement StateNotice interface to listen for change of state
     */
    @Override
    public synchronized void sourceAdded(final Correlator<?, R, ?> sender, final String name, final int newCount) {
        // Do nothing
    }

    /**
     * Implement StateNotice interface to listen for change of state
     */
    @Override
    public synchronized void sourceRemoved(final Correlator<?, R, ?> sender, final String name, final int newCount) {
        removeRecord(name);
    }

    /**
     * Implement StateNotice interface to listen for change of state
     */
    @Override
    public synchronized void binTimespanChanged(final Correlator<?, R, ?> sender, final double newTimespan) {
        setTimespan(newTimespan);
        double range = Math.abs(latestTimestamp - earliestTimestamp);

        // check if we are in violation of the new time span
        if (range > newTimespan) {
            // throw everything away
            reset();
        }
    }

    /**
     * Implement StateNotice interface to listen for change of state
     */
    @Override
    public void willStopMonitoring(final Correlator<?, R, ?> sender) {
        // Do nothing
    }

    /**
     * Implement StateNotice interface to listen for change of state
     */
    @Override
    public void willStartMonitoring(final Correlator<?, R, ?> sender) {
        // Do nothing
    }

    /**
     * Implement StateNotice interface to listen for change of state
     */
    @Override
    public void correlationFilterChanged(Correlator<?, R, ?> sender, CorrelationFilter<R> newFilter) {
        // Do nothing
    }
}
