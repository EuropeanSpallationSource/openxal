//
// DispatchOperation.java
// xal
//
// Created by Tom Pelaia on 4/23/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.dispatch;

import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps a operation so status can be monitored
 */
abstract class DispatchOperation<ReturnType> implements Callable<ReturnType> {

    /**
     * indicates whether the task is a barrier operation
     */
    private final boolean isBarrier;

    /**
     * flag indicating whether this operation is currently running
     */
    private volatile boolean isRunning;

    /**
     * flag indicating whether this operation is done execution
     */
    private volatile boolean isComplete;

    /**
     * listeners for events from this operation (e.g. queue and groups)
     */
    private final Set<DispatchOperationListener> eventListeners;

    /**
     * result of the operation upon successful completion
     */
    private ReturnType result;

    private static final Logger LOGGER = Logger.getLogger(DispatchOperation.class.getName());

    /**
     * Primary Constructor
     */
    protected DispatchOperation(final DispatchOperationListener delegate, final boolean isBarrier) {
        this.isBarrier = isBarrier;

        eventListeners = new HashSet<>();
        addDispatchOperationListener(delegate);

        isRunning = false;
        isComplete = false;
        result = null;
    }

    /**
     * Get a new dispatch operation that wraps the specified raw operation
     */
    public static DispatchOperation<Void> getInstance(final Runnable rawOperation, final DispatchOperationListener delegate, final boolean isBarrier) {
        return new DispatchOperationRawRunnable(rawOperation, delegate, isBarrier);
    }

    /**
     * Get a new dispatch operation that wraps the specified raw operation
     */
    public static <ReturnType> DispatchOperation<ReturnType> getInstance(final Callable<ReturnType> rawOperation, final DispatchOperationListener delegate, final boolean isBarrier) {
        return new DispatchOperationRawCallable<>(rawOperation, delegate, isBarrier);
    }

    /**
     * wait for this operation to complete
     */
    public final void waitForCompletion() {
        while (!isComplete) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException exception) {
            }
        }
    }

    /**
     * Add the event listener
     */
    public final void addDispatchOperationListener(final DispatchOperationListener listener) {
        eventListeners.add(listener);
    }

    /**
     * determine whether this operation is a barrier operation
     */
    public final boolean isBarrier() {
        return isBarrier;
    }

    /**
     * Determine whether this operation is currently running
     */
    public final boolean isRunning() {
        return isRunning;
    }

    /**
     * Determine whether this operation is done execution
     */
    public final boolean isComplete() {
        return isComplete;
    }

    /**
     * Get the result
     */
    public final ReturnType getResult() {
        return result;
    }

    /**
     * perform the operation
     */
    @Override
    public final ReturnType call() {
        try {
            isRunning = true;
            final ReturnType result = executeRawOperation();
            this.result = result;
            return result;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            isRunning = false;
            isComplete = true;

            try {
                synchronized (this) {
                    this.notifyAll();
                }
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Failed attempt to awake threads waiting on this operation to complete.", exception);
            }

            sendCompletionNotification();
        }
    }

    protected abstract ReturnType executeRawOperation() throws java.lang.Exception;

    /**
     * notify the queue and groups that the operation has completed
     */
    private void sendCompletionNotification() {
        for (final DispatchOperationListener handler : eventListeners) {
            handler.operationCompleted(this);
        }
    }
}

/**
 * Dispatch operation built to execute a raw runnable operation
 */
class DispatchOperationRawRunnable extends DispatchOperation<Void> {

    /**
     * wrapped operation
     */
    private final Runnable rawOperation;

    /**
     * Primary Constructor
     */
    public DispatchOperationRawRunnable(final Runnable rawOperation, final DispatchOperationListener delegate, final boolean isBarrier) {
        super(delegate, isBarrier);

        this.rawOperation = rawOperation;
    }

    @Override
    protected Void executeRawOperation() throws java.lang.Exception {
        rawOperation.run();
        return null;
    }
}

/**
 * Dispatch operation built to execute a raw runnable operation
 */
class DispatchOperationRawCallable<ReturnType> extends DispatchOperation<ReturnType> {

    /**
     * wrapped operation
     */
    private final Callable<ReturnType> rawOperation;

    /**
     * Primary Constructor
     */
    public DispatchOperationRawCallable(final Callable<ReturnType> rawOperation, final DispatchOperationListener delegate, final boolean isBarrier) {
        super(delegate, isBarrier);

        this.rawOperation = rawOperation;
    }

    @Override
    protected ReturnType executeRawOperation() throws java.lang.Exception {
        return rawOperation.call();
    }
}
