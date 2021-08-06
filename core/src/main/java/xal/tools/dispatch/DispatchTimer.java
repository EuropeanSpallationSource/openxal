//
// DispatchTimer.java
// xal
//
// Created by Tom Pelaia on 5/3/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.dispatch;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DispatchTimer
 */
public class DispatchTimer {

    private static final Logger LOGGER = Logger.getLogger(DispatchTimer.class.getName());

    /**
     * possible dispatch modes
     */
    public enum DispatchTimerMode {
        FIXED_RATE, COALESCING
    }

    /**
     * possible run states of the dispatch timer
     */
    private enum DispatchTimerRunState {
        PROCESSING, SUSPENDED, DISPOSED
    }

    /**
     * queue to which to dispatch events
     */
    private final DispatchQueue eventQueue;

    /**
     * internal queue used to schedule the timer events
     */
    private final DispatchQueue scheduleQueue;

    /**
     * event to execute when this timer fires
     */
    private Runnable eventHandler;

    /**
     * event to execute when this timer is canceled
     */
    private Runnable cancelHandler;

    /**
     * indicates whether this timer is canceled
     */
    private volatile boolean isCanceled;

    /**
     * run state of this timer
     */
    private volatile DispatchTimerRunState runState;

    /**
     * milliseconds of the interval between when the timer fires
     */
    private volatile long milliInterval;

    /**
     * nanoseconds of the interval between when the timer fires
     */
    private volatile int nanoInterval;

    /**
     * next scheduled event
     */
    private ScheduledEvent nextScheduledEvent;

    /**
     * delegate for handling the specified dispatch mode
     */
    private final DispatchTimerModeDelegate dispatchModeDelegate;

    /**
     * Primary Constructor
     */
    public DispatchTimer(final DispatchTimerMode dispatchMode, final DispatchQueue eventQueue, final Runnable eventHandler) {
        dispatchModeDelegate = getDispatchModeDelegate(dispatchMode);

        this.eventQueue = eventQueue;
        this.eventHandler = eventHandler;

        scheduleQueue = DispatchQueue.createSerialQueue("Dispatch Timer Scheduling Queue");

        runState = DispatchTimerRunState.PROCESSING;
        isCanceled = false;

        nextScheduledEvent = null;
    }

    /**
     * Constructor
     */
    public DispatchTimer(final DispatchQueue eventQueue, final Runnable eventHandler) {
        this(DispatchTimerMode.FIXED_RATE, eventQueue, eventHandler);
    }

    /**
     * Create a new fixed rate timer
     */
    public static DispatchTimer getFixedRateInstance(final DispatchQueue eventQueue, final Runnable eventHandler) {
        return new DispatchTimer(DispatchTimerMode.FIXED_RATE, eventQueue, eventHandler);
    }

    /**
     * Create a new coalescing timer
     */
    public static DispatchTimer getCoalescingInstance(final DispatchQueue eventQueue, final Runnable eventHandler) {
        return new DispatchTimer(DispatchTimerMode.COALESCING, eventQueue, eventHandler);
    }

    /**
     * Get the dispatch mode delegate for the corresponding mode enum
     */
    private DispatchTimerModeDelegate getDispatchModeDelegate(final DispatchTimerMode dispatchMode) {
        switch (dispatchMode) {
            case FIXED_RATE:
                return new DispatchTimerFixedRateDispatch();
            case COALESCING:
                return new DispatchTimerCoalescingDispatch();
            default:
                return new DispatchTimerFixedRateDispatch();
        }
    }

    /**
     * release resources held by this timer
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            dispose();
        } finally {
            super.finalize();
        }
    }

    /**
     * Set the event handler which is dispatched to the queue when the timer
     * fires
     */
    public void setEventHandler(final Runnable eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * Set the cancel handler which is dispatched to the queue when the timer is
     * canceled
     */
    public void setCancelHandler(final Runnable cancelHandler) {
        this.cancelHandler = cancelHandler;
    }

    /**
     * Start the timer now and set the interval between when the timer fires.
     *
     * @param milliInterval milliseconds of the interval between when the timer
     * fires
     * @param nanoInterval nanoseconds of the interval between when the timer
     * fires
     */
    public void startNowWithInterval(final long milliInterval, final int nanoInterval) {
        setStartTimeAndInterval(new Date(), milliInterval, nanoInterval);
    }

    /**
     * Set the time at which this timer starts and the interval between when the
     * timer fires.
     *
     * @param milliInterval milliseconds of the interval between when the timer
     * fires
     * @param nanoInterval nanoseconds of the interval between when the timer
     * fires
     */
    public void setStartTimeAndInterval(final Date startTime, final long milliInterval, final int nanoInterval) {
        // Since the start time is changing, we need to immediately cancel the next pending event here plus later on the schedule queue (see code below).
        cancelNextScheduledEvent();

        scheduleQueue.dispatchAsync(() -> {
            // Cancel any currently scheduled event on the schedule queue in addition to immediately (see code above)
            cancelNextScheduledEvent();

            DispatchTimer.this.milliInterval = milliInterval;
            DispatchTimer.this.nanoInterval = nanoInterval;

            // schedule an event that will execute immediately upon dispatch
            DispatchTimer.this.nextScheduledEvent = new ScheduledEvent(startTime);
            // dispatch after the start time
            scheduleQueue.dispatchAfter(startTime, nextScheduledEvent);
        });
    }

    /**
     * Schedule the next event
     */
    private void scheduleNextEvent(final ScheduledEvent nextScheduledEvent) {
        scheduleQueue.dispatchAsync(() -> {
            DispatchTimer.this.nextScheduledEvent = nextScheduledEvent;
            scheduleQueue.dispatchAsync(nextScheduledEvent);
        });
    }

    /**
     * Cancel this timer
     */
    public void cancel() {
        isCanceled = true;

        cancelNextScheduledEvent();

        if (cancelHandler != null) {
            eventQueue.dispatchAsync(cancelHandler);
        }
    }

    /**
     * Cancel the next scheduled event if any
     */
    private void cancelNextScheduledEvent() {
        if (nextScheduledEvent != null) {
            nextScheduledEvent.cancel();
        }
        this.nextScheduledEvent = null;
    }

    /**
     * Determines whether this queue is suspended (disposed implies suspended)
     */
    public boolean isSuspended() {
        // disposed states are also suspended
        return runState != DispatchTimerRunState.PROCESSING;
    }

    /**
     * suspend this timer if it is processing (do nothing if disposed or already
     * suspended)
     */
    public void suspend() {
        if (runState == DispatchTimerRunState.PROCESSING) {
            runState = DispatchTimerRunState.SUSPENDED;
        }
    }

    /**
     * resume this timer
     */
    public void resume() {
        switch (runState) {
            case SUSPENDED:
                runState = DispatchTimerRunState.PROCESSING;
                resumeScheduling();
                break;
            case DISPOSED:
                throw new RuntimeException("Cannot resume the disposed dispatch timer.");
            default:
                break;
        }
    }

    /**
     * resume scheduling events
     */
    private void resumeScheduling() {
        if (nextScheduledEvent != null) {
            nextScheduledEvent.resume();
        }
    }

    /**
     * dispose of this timer's resources
     */
    public void dispose() {
        runState = DispatchTimerRunState.DISPOSED;

        scheduleQueue.dispose();
    }

    /**
     * determine whether this timer has been disposed
     */
    public boolean isDisposed() {
        return runState == DispatchTimerRunState.DISPOSED;
    }

    /**
     * Event scheduled for execution
     */
    private class ScheduledEvent implements Runnable {

        /**
         * indicates whether the event would have fired if it had not been
         * suspended
         */
        private boolean isPastDue;

        /**
         * indicates whether this event is canceled
         */
        private volatile boolean isCanceled;

        /**
         * time at which the event should fire
         */
        private final long targetTime;

        /**
         * additional nanosecond time
         */
        private final int nanoOffset;

        /**
         * Primary Constructor
         */
        public ScheduledEvent(final long targetTime, final int nanoOffset) {
            this.targetTime = targetTime;
            this.nanoOffset = nanoOffset;

            isCanceled = false;
            isPastDue = false;
        }

        /**
         * Constructor
         */
        public ScheduledEvent(final Date targetDate, final int nanoOffset) {
            this(targetDate.getTime(), nanoOffset);
        }

        /**
         * Constructor
         */
        public ScheduledEvent(final long targetTime) {
            this(targetTime, 0);
        }

        /**
         * Constructor
         */
        public ScheduledEvent(final Date targetDate) {
            this(targetDate.getTime());
        }

        /**
         * Constructor with event scheduled immediately
         */
        public ScheduledEvent() {
            this(new Date());
        }

        /**
         * Get the target time
         */
        public long getTargetTime() {
            return targetTime;
        }

        /**
         * Get the next scheduled event relative to this one using the timer's
         * millisecond and nanosecond intervals
         */
        public ScheduledEvent nextScheduledEvent() {
            return nextScheduledEvent(milliInterval, nanoInterval);
        }

        /**
         * Get the next scheduled event relative to this one using the specified
         * delays
         */
        public ScheduledEvent nextScheduledEvent(final long milliDelay, final int nanoDelay) {
            // Calculate the new target time and nano offset. If nanos accumulate more than a millisecond, shift that amount to the milliseconds.
            final int nanoShift = nanoOffset + nanoDelay;
            final long nextTargetTime = this.targetTime + milliDelay + nanoShift / 1000000;
            final int nexrNanoOffset = nanoShift % 1000000;
            return new ScheduledEvent(nextTargetTime, nexrNanoOffset);
        }

        /**
         * Cancel this timer
         */
        public void cancel() {
            isCanceled = true;
            synchronized (scheduleQueue) {
                try {
                    scheduleQueue.notifyAll();
                } catch (Exception exception) {
                    LOGGER.log(Level.WARNING, null, exception);
                }
            }
        }

        /**
         * Resume the timer and fire the event if it is past due
         */
        public void resume() {
            if (isPastDue) {
                dispatchEventIfEnabled();
            }
        }

        /**
         * dispatch the event if timer is active
         */
        private void dispatchEventIfEnabled() {
            if (!isCanceled) {
                if (runState == DispatchTimerRunState.PROCESSING) {
                    dispatchModeDelegate.processTimerEvent(eventHandler);
                } else {
                    isPastDue = true;
                }
            }
        }

        /**
         * Executes the event
         */
        @Override
        public void run() {
            synchronized (scheduleQueue) {
                if (!isCanceled) {
                    try {
                        while (!isCanceled) {
                            // milliseconds left to wait
                            final long milliTimeout = targetTime - new Date().getTime();
                            final int nanoTimeout = nanoOffset;

                            if (milliTimeout > 0) {
                                // wait the remaining millisecond timeout
                                scheduleQueue.wait(milliTimeout, 0);
                            } else if (milliTimeout == 0 && nanoTimeout > 0) {
                                // wait the remaining nano interval
                                scheduleQueue.wait(0, nanoTimeout);
                                // assume the nano timeout was successful as we have no way to verify otherwise
                                break;
                            } else {
                                break;
                            }
                        }
                    } catch (InterruptedException exception) {
                        LOGGER.log(Level.SEVERE, null, exception);
                    } finally {
                        dispatchEventIfEnabled();
                    }
                }
            }
        }
    }

    /**
     * Make the next scheduled event
     */
    private ScheduledEvent makeNextScheduledEvent() {
        final ScheduledEvent nextEvent = nextScheduledEvent;
        return nextEvent != null ? nextEvent.nextScheduledEvent() : null;
    }

    /**
     * Delegate interface for handling the various dispatch modes
     */
    interface DispatchTimerModeDelegate {

        /**
         * process the current timer event
         */
        public void processTimerEvent(final Runnable eventHandler);
    }

    /**
     * Dispatches events at a fixed rate
     */
    private class DispatchTimerFixedRateDispatch implements DispatchTimerModeDelegate {

        /**
         * process the current timer event
         */
        @Override
        public void processTimerEvent(final Runnable eventHandler) {
            final ScheduledEvent nextEvent = makeNextScheduledEvent();

            if (eventHandler != null) {
                eventQueue.dispatchAsync(eventHandler);
            }

            if (!isCanceled && nextEvent != null) {
                scheduleNextEvent(nextEvent);
            }
        }
    }

    /**
     * Dispatches events at a fixed rate but coalesces events that are
     * concurrent thus preventing events from backing up in the queue
     */
    private class DispatchTimerCoalescingDispatch implements DispatchTimerModeDelegate {

        /**
         * process the current timer event
         */
        @Override
        public void processTimerEvent(final Runnable eventHandler) {
            final ScheduledEvent nextEvent = makeNextScheduledEvent();

            try {
                if (eventHandler != null) {
                    eventQueue.dispatchSync(eventHandler);
                }
            } finally {
                if (!isCanceled && nextEvent != null) {
                    scheduleNextEvent(nextEvent);
                }
            }
        }
    }
}
