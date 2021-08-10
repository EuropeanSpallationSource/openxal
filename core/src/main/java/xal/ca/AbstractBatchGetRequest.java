//
// AbstractBatchGetRequest.java
// xal
//
// Created by Tom Pelaia on 4/2/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//
package xal.ca;

import xal.tools.messaging.MessageCenter;
import xal.tools.dispatch.DispatchQueue;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AbstractBatchGetRequest
 */
public abstract class AbstractBatchGetRequest<T extends ChannelRecord> implements BatchConnectionRequestListener {

    /**
     * message center for dispatching events
     */
    private final MessageCenter messageCenter;

    /**
     * proxy which forwards events to registered listeners
     */
    private final BatchGetRequestListener<T> eventProxy;

    /**
     * set of channels for which to request batch operations
     */
    private final Set<Channel> channels;

    /**
     * table of channel records keyed by channel
     */
    private final Map<Channel, T> records;

    /**
     * table of get request exceptions keyed by channel
     */
    private final Map<Channel, Exception> exceptions;

    /**
     * channels pending completion
     */
    private final Set<Channel> pendingChannels;

    /**
     * channels that are connected and pending the get request
     */
    private final Set<Channel> pendingConnectedChannels;

    /**
     * serial queue on which channels are submitted for get requests
     */
    private final DispatchQueue getRequestedProcessingQueue;

    /**
     * indicates that pending connected channels are queued for processing
     */
    private volatile boolean pendingChannelProcessingQueued;

    /**
     * object used for waiting and notification
     */
    private final Object completionLock;

    /**
     * batch request for connecting to the pending channels
     */
    private BatchConnectionRequest batchConnectionRequest;

    private static final Logger LOGGER = Logger.getLogger(AbstractBatchGetRequest.class.getName());

    /**
     * Primary Constructor
     *
     * @param channels the channels for which get requests will be handled
     */
    // No way to pass BatchGetRequestListener.class with the specified RecordType
    @SuppressWarnings("unchecked")
    protected AbstractBatchGetRequest(final Collection<Channel> channels) {
        messageCenter = new MessageCenter("BatchGetRequest");
        eventProxy = messageCenter.registerSource(this, BatchGetRequestListener.class);

        completionLock = new Object();

        records = new HashMap<>();
        exceptions = new HashMap<>();
        pendingChannels = new HashSet<>();
        pendingConnectedChannels = new HashSet<>();
        getRequestedProcessingQueue = DispatchQueue.createSerialQueue("Batch Get Request Processing");

        batchConnectionRequest = null;
        pendingChannelProcessingQueued = false;

        this.channels = new HashSet<>(channels.size());
        this.channels.addAll(channels);
    }

    /**
     * dispose of the executors
     */
    @Override
    protected void finalize() throws Throwable {
        getRequestedProcessingQueue.dispose();
        super.finalize();
    }

    /**
     * add the specified listener as a receiver of batch get request events from
     * this instance
     *
     * @param listener a receiver which will receive events
     */
    public void addBatchGetRequestListener(final BatchGetRequestListener<T> listener) {
        messageCenter.registerTarget(listener, this, BatchGetRequestListener.class);
    }

    /**
     * remove the specified listener from receiving batch get request events
     * from this instance
     *
     * @param listener receiver to remove from receiving events
     */
    public void removeBatchGetRequestListener(final BatchGetRequestListener<T> listener) {
        messageCenter.removeTarget(listener, this, BatchGetRequestListener.class);
    }

    /**
     * add a new channel to the batch request
     *
     * @param channel a channel to add to the batch request
     */
    public void addChannel(final Channel channel) {
        synchronized (channels) {
            channels.add(channel);
        }
    }

    /**
     * get the collection of channels to process
     *
     * @return a copy of the list of channels in the batch request
     */
    public Collection<Channel> getChannels() {
        return copyChannels();
    }

    /**
     * submit as a batch the get requests for each channel
     */
    public void submit() {
        final Set<Channel> channelSet = copyChannels();
        synchronized (pendingChannels) {
            pendingChannels.clear();
            pendingChannels.addAll(channelSet);
        }
        synchronized (records) {
            records.clear();
        }

        // dispose of the old batch channel connection request
        final BatchConnectionRequest oldBatchConnectionRequest = batchConnectionRequest;
        if (oldBatchConnectionRequest != null) {
            oldBatchConnectionRequest.cancel();
            oldBatchConnectionRequest.removeBatchConnectionRequestListener(this);
        }

        // determine which channels are connected and process them immediately
        final Set<Channel> unconnectedChannels = new HashSet<>();
        for (final Channel channel : channelSet) {
            if (channel.isConnected()) {
                synchronized (pendingConnectedChannels) {
                    pendingConnectedChannels.add(channel);
                }
            } else {
                unconnectedChannels.add(channel);
            }
        }
        synchronized (pendingConnectedChannels) {
            if (!pendingConnectedChannels.isEmpty()) {
                processPendingConnectedChannels();
            }
        }

        // create a fresh batch channel connection request for unconnected channels if any
        if (!unconnectedChannels.isEmpty()) {
            final BatchConnectionRequest newBatchConnectionRequest = new BatchConnectionRequest(unconnectedChannels);
            batchConnectionRequest = newBatchConnectionRequest;
            batchConnectionRequest.addBatchConnectionRequestListener(this);
            batchConnectionRequest.submit();
        }
    }

    /**
     * Submit a batch of get requests and wait for the requests to be completed
     * or timeout. Note that if this is called, within a channel access
     * callback, requests will not be processed until the callback completes, so
     * it is useless to wait. Instead, call waitForCompletion separately outside
     * of the callback.
     *
     * @param timeout the maximum time in seconds to wait for completion
     * @return true if complete or false if not
     */
    public boolean submitAndWait(final double timeout) {
        submit();
        waitForCompletion(timeout);
        Channel.pendIO(timeout);
        return isComplete();
    }

    /**
     * Synonym for waitForCompletion. Wait up to the specified timeout for
     * completion. This method should be called outside of a Channel Access
     * callback otherwise events will not be processed.
     *
     * @param timeout the maximum time in seconds to wait for completion
     * @return true if complete or false if not
     */
    public boolean await(final double timeout) {
        return waitForCompletion(timeout);
    }

    /**
     * Wait up to the specified timeout for completion. This method should be
     * called outside of a Channel Access callback otherwise events will not be
     * processed.
     *
     * @param timeout the maximum time in seconds to wait for completion
     * @return true if complete or false if not
     */
    public boolean waitForCompletion(final double timeout) {
        // timeout in milliseconds
        final long milliTimeout = (long) (1000 * timeout);
        // maximum time until expiration
        final long maxTime = new Date().getTime() + milliTimeout;
        while (!isComplete() && new Date().getTime() < maxTime) {
            final long remainingTime = Math.max(0, maxTime - new Date().getTime());
            // remaining time must be strictly greater than zero to prevent waiting forever should it be identically zero
            if (remainingTime > 0) {
                try {
                    synchronized (completionLock) {
                        completionLock.wait(remainingTime);
                    }
                } catch (InterruptedException exception) {
                    throw new BatchConnectionRuntimeException("Exception waiting for the batch get requests to be completed.", exception);
                }
            }
        }

        return isComplete();
    }

    /**
     * Request to get the data for the channel
     *
     * @param channel the channel for which to request data
     * @throws Exception when the request fails
     */
    protected abstract void requestChannelData(final Channel channel) throws ConnectionException, GetException;

    /**
     * Process the get request for a single channel
     *
     * @param channel the channel for which to process the request
     */
    protected void processRequest(final Channel channel) {
        try {
            if (channel.isConnected()) {
                requestChannelData(channel);
            } else {
                throw new ConnectionException(channel, "Exception connecting to channel " + channel.channelName() + " during batch get request.");
            }
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, null, exception);
            synchronized (exceptions) {
                Exception previousException = exceptions.put(channel, exception);
                if (previousException != null) {
                    LOGGER.log(Level.SEVERE, "More than 1 exception for the same channel.", previousException);
                }
                synchronized (pendingChannels) {
                    pendingChannels.remove(channel);
                }
            }
            eventProxy.exceptionInBatch(this, channel, exception);
            processCurrentStatus();
        }
    }

    /**
     * determine if there are any channels pending for either an exception or a
     * completed get request
     *
     * @return true if complete and false if not
     */
    public boolean isComplete() {
        synchronized (pendingChannels) {
            return pendingChannels.isEmpty();
        }
    }

    /**
     * determine if there were any exceptions
     *
     * @return true if there are any exceptions and false if not
     */
    public boolean hasExceptions() {
        synchronized (exceptions) {
            return !exceptions.isEmpty();
        }
    }

    /**
     * get the number of records
     *
     * @return the number of records
     */
    public int getRecordCount() {
        synchronized (records) {
            return records.size();
        }
    }

    /**
     * Get the number of exceptions
     *
     * @return the number of channels for which there was an exception during
     * the request
     */
    public int getExceptionCount() {
        synchronized (exceptions) {
            return exceptions.size();
        }
    }

    /**
     * Get the record if any for the specified channel
     *
     * @param channel the channel for which the record is fetched
     * @return the record for the specified channel or null if there is none
     */
    public T getRecord(final Channel channel) {
        synchronized (records) {
            return records.get(channel);
        }
    }

    /**
     * Get the exception if any for the specified channel
     *
     * @param channel the channel for which the exception is fetched
     * @return the exception for the specified channel or null if there is none
     */
    public Exception getException(final Channel channel) {
        synchronized (exceptions) {
            return exceptions.get(channel);
        }
    }

    /**
     * Get the failed channels for which exceptions were thrown during the
     * request
     *
     * @return the set of failed channels
     */
    public Set<Channel> getFailedChannels() {
        synchronized (exceptions) {
            return new HashSet<>(exceptions.keySet());
        }
    }

    /**
     * Get the channels which produced a result
     *
     * @return the set of channels each for which a record was successfully
     * fetched
     */
    public Set<Channel> getResultChannels() {
        synchronized (records) {
            return new HashSet<>(records.keySet());
        }
    }

    /**
     * copy channels to a new set
     */
    private Set<Channel> copyChannels() {
        synchronized (channels) {
            return new HashSet<>(channels);
        }
    }

    /**
     * Process the receipt of a new record event
     *
     * @param channel the channel for which the event will be processed
     * @param channelRecord the fetched record
     */
    protected void processRecordEvent(final Channel channel, final T channelRecord) {
        synchronized (records) {
            records.put(channel, channelRecord);
            synchronized (pendingChannels) {
                pendingChannels.remove(channel);
            }
        }

        eventProxy.recordReceivedInBatch(this, channel, channelRecord);
        processCurrentStatus();
    }

    /**
     * check for the current status and post notifications if necessary
     */
    protected void processCurrentStatus() {
        if (isComplete()) {
            synchronized (completionLock) {
                try {
                    completionLock.notifyAll();
                } catch (Exception exception) {
                    LOGGER.log(Level.SEVERE, "Excepting notifying", exception);
                }
            }

            // once this batch get request is complete we can cancel the batch connection request
            final BatchConnectionRequest newBatchConnectionRequest = batchConnectionRequest;
            if (newBatchConnectionRequest != null) {
                newBatchConnectionRequest.cancel();
            }

            eventProxy.batchRequestCompleted(this, getRecordCount(), getExceptionCount());
        }
    }

    /**
     * process any pending connected channels
     */
    private void processPendingConnectedChannels() {
        // flag allows pending connected channels to be accumulated so get requests can be submitted in batches
        if (!pendingChannelProcessingQueued) {
            pendingChannelProcessingQueued = true;

            getRequestedProcessingQueue.dispatchAsync(() -> {
                // yield to other threads so we can accumulate a batch of channels to process
                Thread.yield();

                pendingChannelProcessingQueued = false;

                final Set<Channel> channelSet = new HashSet<>();
                synchronized (pendingConnectedChannels) {
                    channelSet.addAll(pendingConnectedChannels);
                    pendingConnectedChannels.clear();
                }

                if (!channelSet.isEmpty()) {
                    channelSet.forEach(channel -> processRequest(channel));
                    Channel.flushIO();
                    // yield to other threads so we can accumulate a batch of channels to process 
                    Thread.yield();
                }
            });
        }
    }

    /**
     * event indicating that the batch request is complete
     */
    @Override
    public void batchConnectionRequestCompleted(final BatchConnectionRequest connectionRequest, final int connectedCount, final int disconnectedCount, final int exceptionCount) {
    }

    /**
     * event indicating that an exception has been thrown for a channel
     */
    @Override
    public void connectionExceptionInBatch(final BatchConnectionRequest connectionRequest, final Channel channel, final Exception exception) {
        synchronized (exceptions) {
            Exception previousException = exceptions.put(channel, exception);
            if (previousException != null) {
                LOGGER.log(Level.SEVERE, "More than 1 exception for the same channel.", previousException);
            }
            synchronized (pendingChannels) {
                pendingChannels.remove(channel);
            }
        }
        final ConnectionException connectionException = new ConnectionException(channel, "Exception connecting to channel " + channel.channelName() + " during batch get request.");
        eventProxy.exceptionInBatch(this, channel, connectionException);
        processCurrentStatus();
    }

    /**
     * event indicating that a connection change has occurred for a channel
     */
    @Override
    public void connectionChangeInBatch(BatchConnectionRequest connectionRequest, Channel channel, boolean connected) {
        synchronized (pendingConnectedChannels) {
            if (connected) {
                pendingConnectedChannels.add(channel);
            } else {
                pendingConnectedChannels.remove(channel);
            }
        }

        if (connected) {
            processPendingConnectedChannels();
        }
    }
}
