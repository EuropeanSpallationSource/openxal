//
//  LatentProcessor.java
//  xal
//
//  Created by Tom Pelaia on 5/21/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//
package xal.tools;

/**
 * Process events with latency and replace any pending requests with the latest
 * request
 */
public class LatentProcessor extends FreshProcessor {

    /**
     * millisecond portion of latency
     */
    private final long latencyMilliseconds;

    /**
     * nanosecond portion of latency
     */
    private final int latencyNanoseconds;

    /**
     * Constructor
     *
     * @param latency Latency in seconds between successive processing of
     * requests. The latency should be positive.
     */
    public LatentProcessor(final double latency) {
        if (latency < 0.0) {
            throw new RuntimeException("Latency must be greater than or equal to zero seconds. The supplied latency was: " + latency);
        }

        // convert from seconds to milliseconds
        double dblLatencyMilliseconds = 1000.0 * latency;
        // millisecond portion of latency
        latencyMilliseconds = (long) (dblLatencyMilliseconds);

        // get the nanosecond remainder
        double remainderNanos = 1.0e6 * (dblLatencyMilliseconds - latencyMilliseconds);
        // nanosecond portion of latency rounded up
        latencyNanoseconds = (int) (remainderNanos + 0.5);
    }

    /**
     * Get the latency
     *
     * @return latency in seconds
     */
    public double getLatency() {
        return 1.0e-3 * latencyMilliseconds + 1.0e-9 * latencyNanoseconds;
    }

    /**
     * Perform post processing
     */
    @Override
    protected void postProcess() throws Exception {
        Thread.sleep(latencyMilliseconds, latencyNanoseconds);
    }
}
