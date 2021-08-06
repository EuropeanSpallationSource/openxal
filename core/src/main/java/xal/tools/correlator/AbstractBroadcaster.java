/*
 * AbstractBroadcaster.java
 *
 * Created on July 25, 2002, 1:26 PM
 */
package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;

/**
 * AbstractBroadcaster is an abstract delegate for the Correlator and is
 * responsible for filtering notifications from the bin agents and
 * rebroadcasting the notifications to the CorrelationNotice listeners. Concrete
 * subclasses provide for different broadcasting behaviors. For example, a
 * broadcaster may post every correlation, passively wait for an event before
 * posting only the most recent best correlation, or maintain a buffer of the
 * best correlations with mutually exclusive channels.
 *
 * @author tap
 */
abstract class AbstractBroadcaster<T> implements BinListener<T>, StateNotice<T> {

    private final MessageCenter localCenter;

    private final MessageCenter broadcastCenter;

    protected transient int fullCount;

    protected CorrelationNotice<T> correlationProxy;

    protected CorrelationFilter<T> correlationFilter;

    /**
     * Creates a new instance of Broadcaster
     */
    @SuppressWarnings("unchecked")    // must cast proxy for Generics
    protected AbstractBroadcaster(final MessageCenter localCenter) {
        // external broadcast center  
        broadcastCenter = new MessageCenter("Correlator Broadcast");
        // internal correlator messaging
        this.localCenter = localCenter;

        fullCount = 0;

        // listen for internal bin events from all of the bins registered with the local message center
        this.localCenter.registerTarget(this, BinListener.class);

        // register to broadcast correlations
        correlationProxy = (CorrelationNotice<T>) broadcastCenter.registerSource(this, CorrelationNotice.class);
    }

    /**
     * Dispose of this delegate and all of its overhead
     */
    void dispose() {
        // stop posting correlation notices
        broadcastCenter.removeSource(this, CorrelationNotice.class);
        // stop listening for bin correlations
        localCenter.removeTarget(this, BinListener.class);
    }

    /**
     * Register the listener as a receiver of Correlation notices from this
     * correlator.
     */
    public void addCorrelationNoticeListener(final CorrelationNotice<T> listener) {
        broadcastCenter.registerTarget(listener, this, CorrelationNotice.class);
    }

    /**
     * Unregister the listener as a receiver of Correlation notices from this
     * correlator.
     */
    public void removeCorrelationNoticeListener(final CorrelationNotice<T> listener) {
        broadcastCenter.removeTarget(listener, this, CorrelationNotice.class);
    }

    /**
     * Set the full count of all channels monitored.
     *
     * @param newCount The new count of all channels being monitored.
     */
    synchronized void setFullCount(final int newCount) {
        fullCount = newCount;
    }

    /**
     * Get the full count of all channels monitored
     *
     * @return The number of channels being monitored.
     */
    int fullCount() {
        return fullCount;
    }

    /**
     * Set the correlation filter.
     *
     * @param newFilter The new filter to use to filter correlations.
     */
    synchronized void setCorrelationFilter(final CorrelationFilter<T> newFilter) {
        correlationFilter = newFilter;
    }

    /**
     * Post the correlation.
     *
     * @param correlation The correlation to post.
     */
    protected synchronized void postCorrelation(final Correlation<T> correlation) {
        correlationProxy.newCorrelation(this, correlation);
    }

    /**
     * Implement BinListener interface and handle receiving a new correlation.
     *
     * @param sender The bin agent that published the new correlation.
     * @param correlation The new correlation.
     */
    @Override
    public abstract void newCorrelation(final BinAgent<T> sender, final Correlation<T> correlation);

    /**
     * Implement BinListener interface. This method does nothing.
     *
     * @param sender The bin agent who sent this message.
     */
    @Override
    public void willReset(final BinAgent<T> sender) {
    }

    /**
     * Handle the source added event.
     *
     * @param sender The correlator to which the source has been added.
     * @param name The name identifying the new source.
     * @param newCount The new number of sources correlated.
     */
    @Override
    public void sourceAdded(final Correlator<?, T, ?> sender, final String name, final int newCount) {
        setFullCount(newCount);
    }

    /**
     * Handle the source removed event.
     *
     * @param sender The correlator from which the source has been removed.
     * @param name The name identifying the new source.
     * @param newCount The new number of sources correlated.
     */
    @Override
    public void sourceRemoved(final Correlator<?, T, ?> sender, final String name, final int newCount) {
        setFullCount(newCount);
    }

    /**
     * Handle the bin timespan changed event.
     *
     * @param sender The correlator whose timespan bin has changed.
     * @param newTimespan The new timespan used by the correlator.
     */
    @Override
    public void binTimespanChanged(final Correlator<?, T, ?> sender, final double newTimespan) {
    }

    /**
     * Handle the advance notice of the correlator stopping.
     *
     * @param sender The correlator that will stop.
     */
    @Override
    public void willStopMonitoring(final Correlator<?, T, ?> sender) {
    }

    /**
     * Handle the advance notice of the correlator starting.
     *
     * @param sender The correlator that will start.
     */
    @Override
    public void willStartMonitoring(final Correlator<?, T, ?> sender) {
    }

    /**
     * Handle the correlation filter changed event.
     *
     * @param sender The correlator whose correlation filter has changed.
     * @param newFilter The new correlation filter to use.
     */
    @Override
    public void correlationFilterChanged(final Correlator<?, T, ?> sender, final CorrelationFilter<T> newFilter) {
    }
}
