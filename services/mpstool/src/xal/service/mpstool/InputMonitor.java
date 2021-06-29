/*
 *  InputMonitor.java
 *
 *  Created on Mon Sep 13 13:12:12 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import xal.ca.*;

/**
 * InputMonitor handles callback requests for getting the latest value from the
 * wrapped channel.
 *
 * @author tap
 * @since Sep 13, 2004
 */
public class InputMonitor extends ChannelWrapper {

    /**
     * the PV's field to monitor so that we get an integer value
     */
    protected static final String PV_FIELD = ".RVAL";

    /**
     * the target value which indicates that the input is okay
     */
    protected final int OKAY_VALUE;

    /**
     * MPS PV
     */
    protected final String mpsSignal;

    /**
     * the base input signal excluding the PV field
     */
    protected final String inputSignal;

    /**
     * handle value request callbacks
     */
    protected ValueHandler valueHandler;

    /**
     * last value fetched for the signal
     */
    protected int lastValue;

    /**
     * indicates whether the value is measured or unset
     */
    protected boolean measured;

    /**
     * lock for synchronizing access to the last value
     */
    protected final Object valueLock;

    /**
     * InputMonitor constructor
     *
     * @param mpsSignal The associated MPS PV
     * @param signal The input PV
     * @param okayValue The input value that represents the good state
     */
    public InputMonitor(final String mpsSignal, final String signal, final int okayValue) {
        super(signal + PV_FIELD);

        OKAY_VALUE = okayValue;

        inputSignal = signal;
        this.mpsSignal = mpsSignal;

        measured = false;    // no values have been measured yet
        lastValue = 0;
        valueLock = new Object();

        valueHandler = new ValueHandler();
    }

    /**
     * Get the MPS PV.
     *
     * @return The MPS PV
     */
    public String getMPSPV() {
        return mpsSignal;
    }

    /**
     * The signal is different from the PV in that the signal does not include
     * the "RVAL" field.
     *
     * @return the input signal
     */
    public String getSignal() {
        return inputSignal;
    }

    /**
     * Determine if the last value request has been completed.
     *
     * @return true if the last request has been completed and false if not.
     */
    public boolean hasRequestCompleted() {
        return !Double.isNaN(lastValue);
    }

    /**
     * Get the signal's last value that was cached.
     *
     * @return the signal's last value
     */
    public double getLastValue() {
        synchronized (valueLock) {
            return lastValue;
        }
    }

    /**
     * Determine if the input is in the tripped state as of the most recent
     * request. An input is considered tripped only if the value has been
     * measured and does not equal the published okay value.
     *
     * @return true if the input has tripped and false otherwise
     */
    public boolean isInputTripped() {
        synchronized (valueLock) {
            return measured && (lastValue != OKAY_VALUE);
        }
    }

    /**
     * Determine whether the input signal value has been measured.
     *
     * @return true if the input signal value is a measure value
     */
    public boolean isMeasured() {
        return measured;
    }

    /**
     * Request the latest value for the input signal.
     */
    public void requestValueUpdate() {
        synchronized (valueLock) {
            lastValue = 0;    // clear the last value
            measured = false;    // indicate that we are awaiting a new measurement
            if (channel.isConnected()) {
                try {
                    channel.getValIntCallback(valueHandler);
                } catch (ConnectionException | GetException exception) {
                    // since the value has already been cleared we don't need to do anything
                }
                // since the value has already been cleared we don't need to do anything

            }
        }
    }

    /**
     * Dispose of resources held by this instance.
     */
    public void dispose() {
        valueHandler = null;
        measured = false;
    }

    /**
     * Handle value request callbacks.
     */
    private class ValueHandler implements IEventSinkValInt {

        /**
         * Value request callback.
         *
         * @param value the new value
         * @param channel the channel whose latest value has been returned
         */
        @Override
        public void eventValue(final int value, final Channel channel) {
            synchronized (valueLock) {
                lastValue = value;
                measured = true;
            }
        }
    }
}
