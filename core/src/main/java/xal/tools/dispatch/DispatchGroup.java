//
// DispatchGroup.java
// xal
//
// Created by Tom Pelaia on 4/23/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.dispatch;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DispatchGroup
 */
public class DispatchGroup implements DispatchOperationListener {

    /**
     * groups that are currently entered for new operations
     */
    private static final LocalGroups LOCAL_CURRENT_GROUPS;

    private static final Logger LOGGER = Logger.getLogger(DispatchGroup.class.getName());

    /**
     * count of pending operations belonging to this group which have not yet
     * completed
     */
    private AtomicInteger pendingOperationCount = new AtomicInteger(0);

    /**
     * lock for waiting on empty
     */
    private final Object emptyWaitLock = new Object();

    // static initializer
    static {
        LOCAL_CURRENT_GROUPS = new LocalGroups();
    }

    /**
     * Get the set of current groups
     */
    protected static Set<DispatchGroup> getCurrentGroups() {
        return LOCAL_CURRENT_GROUPS.get();
    }

    /**
     * Enter this group for addition of operations on the current thread
     */
    public void enter() {
        LOCAL_CURRENT_GROUPS.add(this);
    }

    /**
     * Leave this group to remove it from addition of operations on the current
     * thread
     */
    public void leave() {
        LOCAL_CURRENT_GROUPS.remove(this);
    }

    /**
     * synonym for waitForCompletion()
     */
    public void await() {
        waitForCompletion();
    }

    /**
     * wait indefinitely for all operations to complete
     */
    public void waitForCompletion() {
        // while loop protects against accidental wake since wait is not guaranteed
        synchronized (emptyWaitLock) {
            while (pendingOperationCount.get() > 0) {
                try {
                    emptyWaitLock.wait();
                } catch (InterruptedException exception) {
                    LOGGER.log(Level.SEVERE, null, exception);
                }
            }
        }
    }

    /**
     * synonym for waitForCompletionWithTimeout( timeout )
     */
    public void await(final long timeout) {
        waitForCompletionWithTimeout(timeout);
    }

    /**
     * wait at most the timeout for all operations to complete
     *
     * @param timeout the maximum timeout in milliseconds to wait
     */
    public void waitForCompletionWithTimeout(final long timeout) {
        // maximum time until expiration
        final long maxTime = new Date().getTime() + timeout;
        // while loop protects against accidental wake since wait is not guaranteed
        while (new Date().getTime() < maxTime) {
            final long remainingTime = Math.max(0, maxTime - new Date().getTime());
            // remaining time must be strictly greater than zero otherwise the wait will wait forever until notified
            synchronized (emptyWaitLock) {
                if (pendingOperationCount.get() <= 0) {
                    break;
                }
                if (remainingTime > 0) {
                    try {
                        emptyWaitLock.wait(remainingTime);
                    } catch (InterruptedException exception) {
                        LOGGER.log(Level.WARNING, null, exception);
                    }
                }
            }
        }
    }

    /**
     * add the operation to this group and the current groups without double
     * counting this group
     */
    public <T> void addOperationToThisGroupAndCurrentGroups(final DispatchOperation<T> operation) {
        final Set<DispatchGroup> groups = new HashSet<>(getCurrentGroups());
        groups.add(this);
        addOperationToGroups(operation, groups);
    }

    /**
     * add the operation to the current groups
     */
    public static <T> void addOperationToCurrentGroups(final DispatchOperation<T> operation) {
        addOperationToGroups(operation, getCurrentGroups());
    }

    /**
     * add the operation to the specified groups
     */
    public static <T> void addOperationToGroups(final DispatchOperation<T> operation, final Set<DispatchGroup> groups) {
        if (!operation.isComplete()) {
            for (final DispatchGroup group : groups) {
                operation.addDispatchOperationListener(group);
                group.addOperation(operation);
            }
        }
    }

    /**
     * increment the pending operation count
     */
    private void incrementPendingOperationCount() {
        pendingOperationCount.incrementAndGet();
    }

    /**
     * increment the pending operation count
     */
    private void decrementPendingOperationCount() {
        pendingOperationCount.decrementAndGet();
        try {
            synchronized (emptyWaitLock) {
                if (pendingOperationCount.get() == 0) {
                    emptyWaitLock.notifyAll();
                }
            }
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed attempt to awake threads waiting on this group.", exception);
        }
    }

    /**
     * add an operation to this group
     */
    public <T> void addOperation(final DispatchOperation<T> operation) {
        if (!operation.isComplete()) {
            operation.addDispatchOperationListener(DispatchGroup.this);
            incrementPendingOperationCount();
        }
    }

    /**
     * Event indicating that an operation in this group has completed
     */
    @Override
    public <T> void operationCompleted(final DispatchOperation<T> operation) {
        decrementPendingOperationCount();
    }
}

/**
 * ThreadLocal list of groups that are currently entered and accepting new
 * operations
 */
class LocalGroups extends ThreadLocal<Set<DispatchGroup>> {

    /**
     * override the initial value to create an empty list
     */
    @Override
    protected Set<DispatchGroup> initialValue() {
        return new HashSet<>();
    }

    /**
     * add a group
     */
    protected void add(final DispatchGroup group) {
        get().add(group);
    }

    /**
     * remove a group
     */
    protected void remove(final DispatchGroup group) {
        get().remove(group);
    }
}
