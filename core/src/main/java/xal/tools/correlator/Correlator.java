/*
 * Correlator.java
 *
 * Created on June 27, 2002, 8:26 AM
 */
package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.util.logging.*;

/**
 * The Correlator is the class that is used to setup monitoring of correlated
 * events. It is the sole entry point to the outside world. When correlations
 * are found, the Correlator broadcasts the correlation.
 *
 * Note that all time is in seconds unless otherwise stated.
 *
 * @author tap
 */
public abstract class Correlator<S, R, A extends SourceAgent<R>> {

    private static final Logger LOGGER = Logger.getLogger(Correlator.class.getName());

    protected MessageCenter localCenter;
    protected double binTimespan;
    protected CorrelationTester<R> correlationTester;

    // one binQueue for each channel
    private Map<String, A> sourceAgentTable;
    private StateNotice<R> stateProxy;
    /**
     * actively monitoring channels
     */
    private volatile boolean isMonitoring;
    private AbstractBroadcaster<R> broadcaster;
    private CorrelationPoster poster;

    /**
     * Creates new Correlator
     */
    protected Correlator(double aBinTimespan) {
        this(aBinTimespan, null);
    }

    /**
     * Correlator constructor
     */
    protected Correlator(final double aBinTimespan, final CorrelationFilter<R> aFilter) {
        isMonitoring = false;
        sourceAgentTable = new HashMap<>();
        correlationTester = new CorrelationTester<>(0, aFilter);

        registerEvents();

        poster = new CorrelationPoster();
        useDefaultBroadcaster();

        setBinTimespan(aBinTimespan);
    }

    /**
     * Register for notices.
     */
    // must cast StateNotice proxy to support Generics
    @SuppressWarnings("unchecked")
    protected synchronized void registerEvents() {
        // internal correlator message center
        localCenter = new MessageCenter("Internal Correlator Messaging");

        /**
         * register to broadcast changes of state
         */
        stateProxy = (StateNotice<R>) localCenter.registerSource(this, StateNotice.class);
    }

    /**
     * Register the listener as a receiver of Correlation notices from this
     * correlator.
     *
     * @param listener to register for receiving events
     */
    public void addListener(final CorrelationNotice<R> listener) {
        poster.addCorrelationNoticeListener(listener);
    }

    /**
     * Unregister the listener as a receiver of Correlation notices from this
     * correlator.
     *
     * @param listener to remove from receiving events
     */
    public void removeListener(final CorrelationNotice<R> listener) {
        poster.removeCorrelationNoticeListener(listener);
    }

    /**
     * Return the broadcaster.
     *
     * @return The broadcaster used by the correlator.
     */
    synchronized AbstractBroadcaster<R> getBroadcaster() {
        return broadcaster;
    }

    /**
     * Set the broadcaster to the specified broadcater.
     *
     * @param newBroadcaster the new broadcaster to use.
     */
    private synchronized void setBroadcaster(final AbstractBroadcaster<R> newBroadcaster) {
        if (broadcaster != null) {
            broadcaster.removeCorrelationNoticeListener(poster);
            localCenter.removeTarget(broadcaster, StateNotice.class);
            broadcaster.dispose();
        }

        broadcaster = newBroadcaster;
        broadcaster.setFullCount(numSources());
        localCenter.registerTarget(broadcaster, StateNotice.class);
        broadcaster.addCorrelationNoticeListener(poster);

        // make sure interested broadcasters get this info
        broadcaster.binTimespanChanged(this, binTimespan);
    }

    /**
     * Set the broadcaster to a default broadcaster.
     *
     * @return The new broadcaster.
     */
    synchronized DefaultBroadcaster<R> useDefaultBroadcaster() {
        if (!(broadcaster instanceof DefaultBroadcaster)) {
            setBroadcaster(new DefaultBroadcaster<>(localCenter));
        }
        return (DefaultBroadcaster<R>) broadcaster;
    }

    /**
     * Set the broadcaster to a passive broadcaster.
     *
     * @return The new broadcaster.
     */
    // enforces type internally
    @SuppressWarnings("unchecked")
    synchronized PassiveBroadcaster<R> usePassiveBroadcaster() {
        if (!(broadcaster instanceof PassiveBroadcaster)) {
            setBroadcaster(new PassiveBroadcaster<>(localCenter));
        }
        return (PassiveBroadcaster) broadcaster;
    }

    /**
     * Set the broadcaster to a patient broadcaster.
     *
     * @return The new broadcaster.
     */
    synchronized PatientBroadcaster<R> usePatientBroadcaster() {
        if (!(broadcaster instanceof PatientBroadcaster)) {
            setBroadcaster(new PatientBroadcaster<>(localCenter));
        }
        return (PatientBroadcaster<R>) broadcaster;
    }

    /**
     * Set the broadcaster to a verbose broadcaster.
     *
     * @return The new broadcaster.
     */
    synchronized VerboseBroadcaster<R> useVerboseBroadcaster() {
        if (!(broadcaster instanceof VerboseBroadcaster)) {
            setBroadcaster(new VerboseBroadcaster<>(localCenter));
        }
        return (VerboseBroadcaster<R>) broadcaster;
    }

    /**
     * Maximum time span allowed for events to be considered correlated
     *
     * @return the bin timespan
     */
    public double binTimespan() {
        return binTimespan;
    }

    /**
     * Set the maximum time span allowed for events to be considered correlated
     *
     * @param timespan of the bins
     */
    public void setBinTimespan(double timespan) {
        binTimespan = timespan;
        stateProxy.binTimespanChanged(this, timespan);
    }

    /**
     * Set the correlation filter to the one specified.
     *
     * @param newFilter The correlation filter to use.
     */
    public void setCorrelationFilter(final CorrelationFilter<R> newFilter) {
        correlationTester.setFilter(newFilter);
        stateProxy.correlationFilterChanged(this, correlationTester.getFilter());
    }

    /**
     * Get all of the channel agents managed by this correlator
     *
     * @return collection of source agents
     */
    protected Collection<A> getSourceAgents() {
        return sourceAgentTable.values();
    }

    /**
     * Get a channel agent by name managed by this correlator
     */
    A getSourceAgent(final String sourceName) {
        return sourceAgentTable.get(sourceName);
    }

    /**
     * Get all the names of all the sources managed by this correlator
     *
     * @return names of the sources
     */
    public synchronized Collection<String> getNamesOfSources() {
        return sourceAgentTable.keySet();
    }

    /**
     * Number of channels being managed
     *
     * @return number of sources
     */
    public int numSources() {
        return sourceAgentTable.size();
    }

    /**
     * See if we already manage this channel
     *
     * @param sourceName name of source to test
     * @return true if the named source is in this correlator and false if not
     */
    public boolean hasSource(final String sourceName) {
        return sourceAgentTable.containsKey(sourceName);
    }

    /**
     * Add a source to monitor. The name provided with each source must be
     * unique to that source. Subclasses need to wrap this method to enforce the
     * source type.
     *
     * @param source to add
     * @param sourceName name of source to add
     */
    protected void addSource(final S source, final String sourceName) {
        addSource(source, sourceName, null);
    }

    /**
     * Add a source to monitor. If we already monitor a source as determined by
     * the source name, then do nothing. The record filter is used to determine
     * whether or not to accept a reading of the specified source when the event
     * is handled. You can create your own custom filter or use a pre-built one.
     * Subclasses need to wrap this method to enforce the source type.
     *
     * @param source to add
     * @param sourceName name of source to add
     * @param recordFilter filter for the source
     */
    protected synchronized void addSource(final S source, final String sourceName, final RecordFilter<R> recordFilter) {
        if (hasSource(sourceName)) {
            return;
        }

        final A sourceAgent = newSourceAgent(source, sourceName, recordFilter);

        sourceAgentTable.put(sourceName, sourceAgent);
        sourceAgent.setBinTimespan(binTimespan);
        int numSources = numSources();
        if (isMonitoring) {
            sourceAgent.startMonitor();
        }
        stateProxy.sourceAdded(this, sourceName, numSources);
        correlationTester.setFullCount(numSources);
    }

    protected abstract A newSourceAgent(final S source, final String sourceName, final RecordFilter<R> recordFilter);

    /**
     * Stop managing the specified source.
     *
     * @param sourceName name of source to remove
     */
    public synchronized void removeSource(final String sourceName) {
        final A sourceAgent = getSourceAgent(sourceName);
        sourceAgentTable.remove(sourceName);
        final int numSources = numSources();
        stateProxy.sourceRemoved(this, sourceName, numSources);
        correlationTester.setFullCount(numSources);
        sourceAgent.shutdown();
    }

    /**
     * Stop managing all registered sources
     */
    public synchronized void removeAllSources() {
        final Collection<String> sourceNames = new ArrayList<>(getNamesOfSources());

        for (final String sourceName : sourceNames) {
            removeSource(sourceName);
        }
    }

    /**
     * Monitor until the timeout or until a complete correlation is found
     *
     * @param timeout time to wait in seconds
     */
    // must cast broadcaster type 
    @SuppressWarnings("unchecked")
    public synchronized void pulseMonitorWithTimeout(double timeout) {
        // make sure we stop existing monitor if any (e.g. destroy other timed task)
        stopMonitoring();

        final TimedBroadcaster<R> timedBroadcaster = (broadcaster instanceof TimedBroadcaster) ? (TimedBroadcaster<R>) broadcaster : new TimedBroadcaster<>(localCenter, timeout);
        final CorrelationNotice<R> correlationListener = new CorrelationNotice<R>() {
            @Override
            public void newCorrelation(final Object sender, final Correlation<R> correlation) {
                stopMonitoring();
                timedBroadcaster.removeCorrelationNoticeListener(this);
            }

            @Override
            public void noCorrelationCaught(final Object sender) {
                stopMonitoring();
                timedBroadcaster.removeCorrelationNoticeListener(this);
            }
        };

        timedBroadcaster.addCorrelationNoticeListener(correlationListener);
        // in case timedBroadcaster was reused
        timedBroadcaster.setRepeats(false);
        // in case timedBroadcaster was reused
        timedBroadcaster.setPeriod(timeout);

        if (broadcaster != timedBroadcaster) {
            setBroadcaster(timedBroadcaster);
        }
        startMonitoring();
    }

    /**
     * Monitor and post the best partial correlation if the timeout is exceeded.
     * The timeout is rescheduled after every post.
     *
     * @param timeout time to wait in seconds
     */
    // must cast broadcaster type 
    @SuppressWarnings("unchecked")
    public synchronized void monitorWithTimeout(double timeout) {
        // make sure we stop existing monitor if any (e.g. destroy other timed task)
        stopMonitoring();

        final TimedBroadcaster<R> timedBroadcaster = (broadcaster instanceof TimedBroadcaster) ? (TimedBroadcaster<R>) broadcaster : new TimedBroadcaster<>(localCenter, timeout);
        // in case timedBroadcaster was reused
        timedBroadcaster.setPeriod(timeout);
        // in case timedBroadcaster was reused
        timedBroadcaster.setRepeats(true);

        if (broadcaster != timedBroadcaster) {
            setBroadcaster(timedBroadcaster);
        }
        startMonitoring();
    }

    /**
     * Start monitoring the managed sources.
     */
    public void startMonitoring() {
        if (!isMonitoring) {
            // broadcast the request so the listening source agents start their monitors
            stateProxy.willStartMonitoring(this);
            isMonitoring = true;
        }
    }

    /**
     * Stop monitoring the managed sources.
     */
    public void stopMonitoring() {
        if (isMonitoring) {
            // broadcast the request so the listening source agents stop their monitors
            stateProxy.willStopMonitoring(this);
            isMonitoring = false;
        }
    }

    /**
     * Determine if the correlator is running
     *
     * @return true if the correlator is running and false otherwise.
     */
    public boolean isRunning() {
        return isMonitoring;
    }

    /**
     * Dispose of the correlator and its overhead
     */
    public synchronized void dispose() {
        stopMonitoring();
        removeAllSources();
        localCenter.removeSource(this, StateNotice.class);
        localCenter.removeTarget(broadcaster, StateNotice.class);
        broadcaster.dispose();
        poster.dispose();
    }

    /**
     * <code>fetchCorrelationWithTimeout()</code> is a convenience method that
     * allows the user a simple way to fetch a correlation without handling
     * events and implementing a listener. The method spawns a fetch and blocks
     * until a correlation is retrieved or the timeout has expired. The
     * resulting correlation is returned. If no correlation was found within the
     * timeout, null is returned.
     *
     * @param aTimeout timeout for fetching a correlation
     * @return fetched correlation or null if none within the timeout
     */
    public Correlation<R> fetchCorrelationWithTimeout(final double aTimeout) {
        return new WaitingListener().listenWithTimeout(aTimeout);
    }

    /**
     * Post correlations on behalf of the correlator.
     */
    private class CorrelationPoster implements CorrelationNotice<R> {

        private MessageCenter postCenter;
        private CorrelationNotice<R> postProxy;

        /**
         * Constructor
         */
        // must cast postProxy for Generics
        @SuppressWarnings("unchecked")
        public CorrelationPoster() {
            // external poster
            postCenter = new MessageCenter("Correlator Poster");
            postProxy = (CorrelationNotice<R>) postCenter.registerSource(this, CorrelationNotice.class);
        }

        /**
         * Dispose of this poster and all of its overhead
         */
        void dispose() {
            postCenter.removeSource(this, CorrelationNotice.class);
        }

        /**
         * Register the listener as a receiver of Correlation notices from this
         * correlator.
         */
        public void addCorrelationNoticeListener(final CorrelationNotice<R> listener) {
            postCenter.registerTarget(listener, this, CorrelationNotice.class);
        }

        /**
         * Unregister the listener as a receiver of Correlation notices from
         * this correlator.
         */
        public void removeCorrelationNoticeListener(final CorrelationNotice<R> listener) {
            postCenter.removeTarget(listener, this, CorrelationNotice.class);
        }

        /**
         * Handle the correlation event. This method gets called when a
         * correlation was posted.
         *
         * @param sender The poster of the correlation event.
         * @param correlation The correlation that was posted.
         */
        @Override
        public void newCorrelation(final Object sender, final Correlation<R> correlation) {
            postProxy.newCorrelation(Correlator.this, correlation);
        }

        /**
         * Handle the no correlation event. This method gets called when no
         * correlation was found within some prescribed time period.
         *
         * @param sender The poster of the "no correlation" event.
         */
        @Override
        public void noCorrelationCaught(final Object sender) {
            postProxy.noCorrelationCaught(Correlator.this);
        }
    }

    /**
     * <code>WaitingListener</code> is an internal class used to implement the
     * <code>fetchCorrelationWithTimeout()</code> method. It sets itself up as a
     * listener of events and waits until a correlation is found or the timeout
     * has expired.
     */
    private class WaitingListener implements CorrelationNotice<R> {

        /**
         * latest captured correlation
         */
        private transient Correlation<R> correlation;

        /**
         * Constructor
         */
        public WaitingListener() {
            correlation = null;
        }

        /**
         * Wait and listen for a correlation
         *
         * @param timeout the maximum time (seconds) to wait for a correlation
         */
        // need to cast broadcaster as TimedBroadcaster
        @SuppressWarnings("unchecked")
        public synchronized Correlation<R> listenWithTimeout(final double timeout) {
            addListener(this);
            pulseMonitorWithTimeout(timeout);

            try {
                // block until we get an event or the timeout has expired
                WaitingListener.this.wait((long) (1000 * timeout));
            } catch (InterruptedException exception) {
                LOGGER.log(Level.SEVERE, "Error while waiting for a correlation.", exception);
            } finally {
                removeListener(this);
            }
            synchronized (Correlator.this) {
                // there is a race condition with the timed broadcaster so we must make sure we get its best correlation if any before assuming there is none
                if (correlation == null && broadcaster instanceof TimedBroadcaster) {
                    final TimedBroadcaster<R> pulsedBroadcaster = (TimedBroadcaster) broadcaster;
                    correlation = pulsedBroadcaster.getBestPartialCorrelation();
                }

                return correlation;
            }
        }

        /**
         * Handle the latest captured correlation
         */
        @Override
        public synchronized void newCorrelation(final Object sender, final Correlation<R> newCorrelation) {
            synchronized (Correlator.this) {
                correlation = newCorrelation;
                WaitingListener.this.notifyAll();
            }
        }

        /**
         * No correlation was caught within the timeout
         */
        @Override
        public synchronized void noCorrelationCaught(final Object sender) {
            synchronized (Correlator.this) {
                correlation = null;
                WaitingListener.this.notifyAll();
            }
        }
    }
}
