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
abstract class DispatchOperation<T> implements Callable<T> {

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
    private T result;

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
    public static <T> DispatchOperation<T> getInstance(final Callable<T> rawOperation, final DispatchOperationListener delegate, final boolean isBarrier) {
        return new DispatchOperationRawCallable<>(rawOperation, delegate, isBarrier);
    }

    /**
     * wait for this operation to complete
     */
    public final void waitForCompletion() {
        synchronized (this) {
            while (!isComplete) {
                try {
                    this.wait();
                } catch (InterruptedException exception) {
                    LOGGER.log(Level.SEVERE, null, exception);
                }
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
    public final T getResult() {
        return result;
    }

    /**
     * perform the operation
     */
    @Override
    public final T call() {
        try {
            isRunning = true;
            result = executeRawOperation();
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

    protected abstract T executeRawOperation() throws Exception;

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
    protected Void executeRawOperation() throws Exception {
        rawOperation.run();
        return null;
    }
}

/**
 * Dispatch operation built to execute a raw runnable operation
 */
class DispatchOperationRawCallable<T> extends DispatchOperation<T> {
    /**
     * wrapped operation
     */
    private final Callable<T> rawOperation;

    /**
     * Primary Constructor
     */
    public DispatchOperationRawCallable(final Callable<T> rawOperation, final DispatchOperationListener delegate, final boolean isBarrier) {
        super(delegate, isBarrier);

        this.rawOperation = rawOperation;
    }

    @Override
    protected T executeRawOperation() throws Exception {
        return rawOperation.call();
    }
}
