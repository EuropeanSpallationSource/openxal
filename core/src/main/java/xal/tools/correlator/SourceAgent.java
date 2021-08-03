/*
 * ChannelAgent.java
 *
 * Created on June 27, 2002, 8:46 AM
 */
package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;

import java.util.*;

/**
 * Generator manages a single channel. It performs any setup, monitors the
 * channel and it manages a circular buffer of bin agents that gather correlated
 * events.
 *
 * @author tap
 */
public abstract class SourceAgent<RecordType> implements StateNotice<RecordType> {

    /**
     * number of bins to store events for correlation comparison
     */
    private static final int BIN_POOL_SIZE = 10;

    /**
     * Shared Message center
     */
    private final MessageCenter messageCenter;

    /**
     * tester for correlations
     */
    private final CorrelationTester<RecordType> correlationTester;

    /**
     * unique name of this source agent
     */
    protected String name;

    /**
     * bins sorted by timestamp
     */
    private LinkedList<BinAgent<RecordType>> binAgents;

    /**
     * proxy to forward bin update events to registered listeners
     */
    protected BinUpdate<RecordType> binUpdateProxy;

    /**
     * Creates new ChannelAgent
     */
    public SourceAgent(final MessageCenter messageCenter, final String name, final RecordFilter<RecordType> recordFilter, final CorrelationTester<RecordType> tester) {
        this.name = name;
        correlationTester = tester;
        this.messageCenter = messageCenter;

        setupEventHandler(recordFilter);

        createBins();
        registerEvents();
    }

    @SuppressWarnings("unchecked")    // need cast to get the proxy using Generics 
    private void registerEvents() {
        binUpdateProxy = (BinUpdate<RecordType>) messageCenter.registerSource(this, BinUpdate.class);
        messageCenter.registerTarget(this, StateNotice.class);
    }

    private void unregisterEvents() {
        messageCenter.removeSource(this, BinUpdate.class);
        messageCenter.removeTarget(this, StateNotice.class);
    }

    /**
     * Subclasses implement this method to handle the monitoring of its sources
     * in a way specific to the particular SourceAgent subclass. When an event
     * is captured and it passes the filter test, this method should call
     * postEvent().
     *
     * @param recordFilter filter for records to accept or reject
     * @see #postEvent
     */
    protected abstract void setupEventHandler(RecordFilter<RecordType> recordFilter);

    /**
     * clear memory of all events
     */
    public void reset() {
        for (final BinAgent<RecordType> binAgent : binAgents) {
            binAgent.reset();
        }
    }

    /**
     * Set the timespan to each bin
     *
     * @param timespan for each bin
     */
    public void setBinTimespan(final double timespan) {
        final List<BinAgent<RecordType>> binAgents = new ArrayList<>();
        // need to synchronize since nextBin() modifies _binAgents
        synchronized (binAgents) {
            binAgents.addAll(binAgents);
        }

        for (final BinAgent<RecordType> binAgent : binAgents) {
            binAgent.setTimespan(timespan);
        }
    }

    /**
     * Create a pool of bins that form a circular buffer
     */
    private void createBins() {
        binAgents = new LinkedList<>();

        for (int index = 0; index < BIN_POOL_SIZE; index++) {
            createNewBin();
        }
    }

    /**
     * Create a new bin. Register each bin for events.
     */
    private void createNewBin() {
        final BinAgent<RecordType> binAgent = new BinAgent<>(messageCenter, correlationTester);

        messageCenter.registerTarget(binAgent, BinUpdate.class);
        messageCenter.registerTarget(binAgent, StateNotice.class);

        binAgents.add(binAgent);
    }

    /**
     * deallocate the bins when they are no longer needed
     */
    private void removeBins() {
        synchronized (binAgents) {
            for (final BinAgent<RecordType> binAgent : binAgents) {
                removeBin(binAgent);
            }

            binAgents.clear();
        }
    }

    /**
     * Remove a bin
     */
    private void removeBin(final BinAgent<RecordType> binAgent) {
        messageCenter.removeTarget(binAgent, BinUpdate.class);
        messageCenter.removeTarget(binAgent, StateNotice.class);

        binAgent.shutdown();
    }

    /**
     * Used when recycling bins. Cycle bins in a circular buffer.
     */
    private BinAgent<RecordType> nextBin() {
        BinAgent<RecordType> nextBin;

        synchronized (binAgents) {
            nextBin = binAgents.removeFirst();
            binAgents.addLast(nextBin);
        }

        return nextBin;
    }

    /**
     * This method is used to advertise a new event record received by the event
     * handler of the SourceAgent subclass. When an event record has passed the
     * the filter test it should be posted via this method so that other
     * stakeholders (i.e. the bin agents) can handle the event properly.
     *
     * @param record for which the event was posted
     * @param timestamp for which the event was posted
     */
    protected final void postEvent(final RecordType record, final double timestamp) {
        nextBin().resetWithRecord(name(), record, timestamp);

        // now notify bins everywhere of the new record
        binUpdateProxy.newEvent(name(), record, timestamp);
    }

    /**
     * Name of the managed source
     *
     * @return name of the managed source
     */
    public String name() {
        return name;
    }

    /**
     * Start monitoring the channel
     *
     * @return true upon success and false upon failure
     */
    public abstract boolean startMonitor();

    /**
     * Stop monitoring the channel
     */
    public abstract void stopMonitor();

    /**
     * shutdown this channel agent and remove itself
     */
    synchronized protected void shutdown() {
        stopMonitor();
        unregisterEvents();
        removeBins();
    }

    // implement StateNotice interface
    @Override
    public void sourceAdded(final Correlator<?, RecordType, ?> sender, final String name, final int newCount) {
    }

    // implement StateNotice interface
    @Override
    public void sourceRemoved(final Correlator<?, RecordType, ?> sender, final String name, final int newCount) {
    }

    // implement StateNotice interface
    @Override
    public void binTimespanChanged(final Correlator<?, RecordType, ?> sender, final double newTimespan) {
        setBinTimespan(newTimespan);
    }

    // implement StateNotice interface
    @Override
    public void willStopMonitoring(final Correlator<?, RecordType, ?> sender) {
        stopMonitor();
    }

    // implement StateNotice interface
    @Override
    public void willStartMonitoring(final Correlator<?, RecordType, ?> sender) {
        reset();
        startMonitor();
    }

    /**
     * Implement StateNotice interface to listen for change of state
     */
    @Override
    public void correlationFilterChanged(final Correlator<?, RecordType, ?> sender, final CorrelationFilter<RecordType> newFilter) {
    }
}
