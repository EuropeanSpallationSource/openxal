//
// BatchConnectionRequest.java
// xal
//
// Created by Tom Pelaia on 6/14/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//
package xal.ca;

import xal.tools.messaging.*;
import xal.tools.dispatch.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BatchConnectionRequest
 */
public class BatchConnectionRequest extends java.lang.Object {

    /**
     * time (msec) to periodically watch for status when waiting for connections
     */
    private static final long WATCH_TIME = 100;

    /**
     * message center for dispatching events
     */
    private final MessageCenter messageCenter;

    /**
     * proxy which forwards events to registered listeners
     */
    private final BatchConnectionRequestListener eventProxy;

    /**
     * set of channels for which to request batch operations
     */
    private final Set<Channel> channels;

    /**
     * table of get request exceptions keyed by channel
     */
    private final Map<Channel, Exception> exceptions;

    /**
     * channels pending completion
     */
    private final Set<Channel> pendingChannels;

    /**
     * channels that have been connected
     */
    private final Set<Channel> connectedChannels;

    /**
     * channels that have been connected
     */
    private final Set<Channel> disconnectedChannels;

    /**
     * object used for waiting and notification
     */
    private final Object completionLock;

    /**
     * queue to synchronize access to resources
     */
    private final DispatchQueue resourceSyncQueue;

    /**
     * request handler
     */
    private final RequestHandler requestHandler;

    /**
     * indicates whether this request has been canceled
     */
    private volatile boolean isCanceled;

    private static final Logger LOGGER = Logger.getLogger(BatchConnectionRequest.class.getName());

    /**
     * Constructor
     *
     * @param channels for which the connections will be requested
     */
    public BatchConnectionRequest(final Collection<Channel> channels) {
        messageCenter = new MessageCenter("BatchConnectionRequest");
        eventProxy = messageCenter.registerSource(this, BatchConnectionRequestListener.class);

        completionLock = new Object();
        resourceSyncQueue = DispatchQueue.createConcurrentQueue("Resource Synchronization");

        connectedChannels = new HashSet<>();
        disconnectedChannels = new HashSet<>();
        exceptions = new HashMap<>();
        pendingChannels = new HashSet<>();

        this.channels = new HashSet<>(channels.size());
        this.channels.addAll(channels);

        requestHandler = new RequestHandler();
    }

    /**
     * dispose of the queue
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            resourceSyncQueue.dispose();
        } finally {
            super.finalize();
        }
    }

    /**
     * add the specified listener as a receiver of batch connection request
     * events from this instance
     *
     * @param listener to receive connection events
     */
    public void addBatchConnectionRequestListener(final BatchConnectionRequestListener listener) {
        messageCenter.registerTarget(listener, this, BatchConnectionRequestListener.class);
    }

    /**
     * remove the specified listener from receiving batch connection request
     * events from this instance
     *
     * @param listener to remove from receiving connection events
     */
    public void removeBatchConnectionRequestListener(final BatchConnectionRequestListener listener) {
        messageCenter.removeTarget(listener, this, BatchConnectionRequestListener.class);
    }

    /**
     * Get a copy of the channels to connect
     *
     * @return channels for which connections are requested
     */
    public Set<Channel> getChannels() {
        return Collections.unmodifiableSet(channels);
    }

    /**
     * Get the number of channels requested
     *
     * @return the number of channels for which connections are requested
     */
    public int getChannelCount() {
        return resourceSyncQueue.dispatchSync(() -> channels.size());
    }

    /**
     * Get the channels that were connected
     *
     * @return set of connected channels
     */
    public Set<Channel> getConnectedChannels() {
        return resourceSyncQueue.dispatchSync(() -> new HashSet<>(connectedChannels));
    }

    /**
     * Get the number of channels that were connected
     *
     * @return the number of channels that were connected
     */
    public int getConnectedCount() {
        return resourceSyncQueue.dispatchSync(() -> connectedChannels.size());
    }

    /**
     * Get the channels that were connected
     *
     * @return set of channels that were connected
     */
    public Set<Channel> getDisconnectedChannels() {
        return resourceSyncQueue.dispatchSync(() -> new HashSet<>(disconnectedChannels));
    }

    /**
     * Get the number of channels that were connected
     *
     * @return the number of disconnected channels
     */
    public int getDisconnectedCount() {
        return resourceSyncQueue.dispatchSync(() -> disconnectedChannels.size());
    }

    /**
     * Get the channels pending connection
     *
     * @return set of channels that are pending connection
     */
    public Set<Channel> getPendingChannels() {
        return resourceSyncQueue.dispatchSync(() -> new HashSet<>(pendingChannels));
    }

    /**
     * get the exception if any for the specified channel
     *
     * @param channel for which to get the exception if any
     * @return the exception for the specified channel or null if none
     */
    public Exception getException(final Channel channel) {
        return resourceSyncQueue.dispatchSync(() -> exceptions.get(channel));
    }

    /**
     * Get the failed channels
     *
     * @return set of channels that failed to connect
     */
    public Set<Channel> getFailedChannels() {
        return resourceSyncQueue.dispatchSync(() -> new HashSet<>(exceptions.keySet()));
    }

    /**
     * Get the number of exceptions
     *
     * @return the number of channels that had exceptions connecting
     */
    public int getExceptionCount() {
        return resourceSyncQueue.dispatchSync(exceptions::size);
    }

    /**
     * submit this batch for processing
     */
    public void submit() {
        resourceSyncQueue.dispatchBarrierSync(() -> {
            isCanceled = false;
            pendingChannels.clear();
            pendingChannels.addAll(channels);
            // assume all channels disconnected until notified otherwise
            disconnectedChannels.addAll(channels);
            connectedChannels.clear();
        });

        try {
            for (final Channel channel : channels) {
                processRequest(channel);
            }
            Channel.flushIO();
        } catch (Exception exception) {
            throw new BatchConnectionRuntimeException("Exception while submitting a batch Get request.", exception);
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
     * @return true upon completion and false if not complete
     */
    public boolean submitAndWait(final double timeout) {
        submit();
        await(timeout);
        Channel.pendIO(timeout);
        return isComplete();
    }

    /**
     * Wait up to the specified timeout for completion. This method may be
     * called many times as needed.
     *
     * @param timeout the maximum time in seconds to wait for completion
     * @return true upon completion and false if not complete
     */
    public boolean await(final double timeout) {
        // timeout in milliseconds
        final long milliTimeout = (long) (1000 * timeout);
        // maximum time until expiration
        final long maxTime = new Date().getTime() + milliTimeout;
        while (!isCanceled && !isComplete() && new Date().getTime() < maxTime) {
            final long remainingTime = Math.max(0, maxTime - new Date().getTime());
            // remaining time must be strictly greater than zero to prevent waiting forever should it be identically zero
            if (remainingTime > 0) {
                // want to watch for cancel periodically when the remaining time is long
                final long waitTime = remainingTime > WATCH_TIME ? WATCH_TIME : remainingTime;
                try {
                    synchronized (completionLock) {
                        completionLock.wait(waitTime);
                    }
                } catch (InterruptedException exception) {
                    throw new BatchConnectionRuntimeException("Exception waiting for the batch get requests to be completed.", exception);
                }
            }
        }

        return isComplete();
    }

    /**
     * Determine whether this request has been canceled
     *
     * @return true if canceled and false otherwise
     */
    public boolean isCanceled() {
        return isCanceled;
    }

    /**
     * Cancel this request to stop monitoring and dispatching events.
     */
    public void cancel() {
        isCanceled = true;
        resourceSyncQueue.dispatchBarrierSync(() -> {
            for (final Channel channel : channels) {
                channel.removeConnectionListener(requestHandler);
            }
        });
    }

    /**
     * process the get request for a single channel
     */
    private void processRequest(final Channel channel) {
        try {
            channel.addConnectionListener(requestHandler);
            channel.requestConnection();
        } catch (final Exception exception) {
            resourceSyncQueue.dispatchBarrierAsync(() -> {
                exceptions.put(channel, exception);
                pendingChannels.remove(channel);
            });

            if (!isCanceled) {
                eventProxy.connectionExceptionInBatch(this, channel, exception);
            }
            processCurrentStatus();
        }
    }

    /**
     * Determine if there are any channels pending for either an exception or a
     * completed get request
     *
     * @return true if complete and false otherwise
     */
    public boolean isComplete() {
        return resourceSyncQueue.dispatchSync(() -> pendingChannels.isEmpty());
    }

    /**
     * check for the current status and post notifications if necessary
     */
    private void processCurrentStatus() {
        if (isComplete()) {
            synchronized (completionLock) {
                try {
                    completionLock.notifyAll();
                } catch (Exception exception) {
                    LOGGER.log(Level.SEVERE, "Excepting notifying ", exception);
                }
            }

            if (!isCanceled) {
                int[] counts = resourceSyncQueue.dispatchSync(() -> new int[]{connectedChannels.size(), disconnectedChannels.size(), exceptions.size()});

                if (!isCanceled) {
                    eventProxy.batchConnectionRequestCompleted(this, counts[0], counts[1], counts[2]);
                }
            }
        }
    }

    /**
     * handle get request events
     */
    protected class RequestHandler implements ConnectionListener {

        /**
         * Indicates that a connection to the specified channel has been
         * established.
         *
         * @param channel The channel which has been connected.
         */
        @Override
        public void connectionMade(final Channel channel) {
            resourceSyncQueue.dispatchBarrierAsync(() -> {
                connectedChannels.add(channel);
                disconnectedChannels.remove(channel);
                pendingChannels.remove(channel);
            });

            if (!isCanceled) {
                eventProxy.connectionChangeInBatch(BatchConnectionRequest.this, channel, true);
            }
            processCurrentStatus();
        }

        /**
         * Indicates that a connection to the specified channel has been
         * dropped.
         *
         * @param channel The channel which has been disconnected.
         */
        @Override
        public void connectionDropped(final Channel channel) {
            resourceSyncQueue.dispatchBarrierAsync(() -> {
                disconnectedChannels.add(channel);
                connectedChannels.remove(channel);
                pendingChannels.remove(channel);
            });

            if (isCanceled) {
                eventProxy.connectionChangeInBatch(BatchConnectionRequest.this, channel, false);
            }
            processCurrentStatus();
        }
    }
}
