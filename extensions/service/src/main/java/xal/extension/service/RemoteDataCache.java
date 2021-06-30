//
// RemoteDataCache.java
// Open XAL
//
// Created by Pelaia II, Tom on 10/1/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.service;

import java.util.concurrent.Callable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.UpdateListener;
import xal.tools.dispatch.DispatchQueue;

/**
 * RemoteDataCache is a utility for managing calls to remote services to avoid
 * deadlock if a service is down.
 */
public class RemoteDataCache<DataType> {

    private static final Logger LOGGER = Logger.getLogger(RemoteDataCache.class.getName());

    /**
     * remote operation to perform
     */
    private final Callable<DataType> REMOTE_OPERATION;

    /**
     * latest data that has been cached
     */
    protected volatile RemoteData<DataType> cachedData;

    /**
     * indicates whether the remote service is connected
     */
    private volatile boolean isConnected;

    /**
     * indicates whether a fetch is pending
     */
    private volatile boolean isFetchPending;

    /**
     * optional handler of the update event
     */
    private UpdateListener updateListener;

    /**
     * Constructor
     */
    public RemoteDataCache(final Callable<DataType> remoteOperation) {
        this(remoteOperation, null);
    }

    /**
     * Primary Constructor
     */
    public RemoteDataCache(final Callable<DataType> remoteOperation, final UpdateListener updateHandler) {
        REMOTE_OPERATION = remoteOperation;

        updateListener = updateHandler;

        isFetchPending = false;
        cachedData = null;
        // assume connected until proven otherwise
        isConnected = true;
    }

    /**
     * set the update handler which is called when the cache has been updated
     */
    public void setUpdateListener(final UpdateListener handler) {
        updateListener = handler;
    }

    /**
     * get the update handler
     */
    public UpdateListener getUpdateListener() {
        return updateListener;
    }

    /**
     * Refresh the cache with a fresh call to the remote unless a fetch is
     * already pending
     */
    public void refresh() {
        // fetch new data only if a fetch is not currently in progress
        if (!isFetchPending) {
            fetchData();
        }
    }

    /**
     * Get the timestamp of the last fetch
     */
    public Date getTimestamp() {
        final RemoteData<DataType> newCachedData = this.cachedData;
        return newCachedData != null ? newCachedData.getTimestamp() : null;
    }

    /**
     * Fetch the value and cache it for future requests
     */
    public DataType getValue() {
        final RemoteData<DataType> newCachedData = this.cachedData;

        if (newCachedData == null) {
            refresh();
        }

        return newCachedData != null ? newCachedData.getValue() : null;
    }

    /**
     * determine whether the remote service is connected
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Fetch the data from the remote service
     */
    private void fetchData() {
        isFetchPending = true;

        DispatchQueue.getGlobalDefaultPriorityQueue().dispatchAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final DataType result = REMOTE_OPERATION.call();
                    cachedData = new RemoteData<>(result);
                } catch (RemoteServiceDroppedException exception) {
                    cachedData = null;
                    isConnected = false;
                } catch (Exception exception) {
                    LOGGER.log(Level.SEVERE, null, exception);
                    cachedData = null;
                } finally {
                    isFetchPending = false;

                    // if there is an update listener, notify it of the updated value
                    final UpdateListener updateHandler = updateListener;
                    if (updateHandler != null) {
                        updateHandler.observedUpdate(RemoteDataCache.this);
                    }
                }
            }
        });
    }
}

/**
 * data from a remote fetch
 */
class RemoteData<DataType> {

    /**
     * latest data that has been cached
     */
    private final DataType value;

    /**
     * time of the last fetch from which the expiration should be measured
     */
    private final Date fetchTimestamp;

    /**
     * Primary Constructor
     */
    public RemoteData(final DataType value, final Date timestamp) {
        this.value = value;
        fetchTimestamp = timestamp;
    }

    /**
     * Constructor
     */
    public RemoteData(final DataType value) {
        this(value, new Date());
    }

    /**
     * get the value
     */
    public DataType getValue() {
        return value;
    }

    /**
     * get the timestamp
     */
    public Date getTimestamp() {
        return fetchTimestamp;
    }

    /**
     * get string representation
     */
    @Override
    public String toString() {
        return "Cached value: " + value + ", timestamp: " + fetchTimestamp;
    }
}
