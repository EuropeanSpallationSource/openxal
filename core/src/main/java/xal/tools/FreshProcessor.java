//
//  FreshProcessor.java
//  xal
//
//  Created by Tom Pelaia on 5/22/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//
package xal.tools;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * process on a separate thread pending requests dropping any previous ones
 */
public class FreshProcessor {

    /**
     * pending requests waiting to be processed
     */
    private final ArrayBlockingQueue<Runnable> requestQueue;

    /**
     * indicates whether the processor should keep running
     */
    private volatile boolean keepRunning;

    private static final Logger LOGGER = Logger.getLogger(FreshProcessor.class.getName());

    /**
     * Constructor
     */
    public FreshProcessor() {
        keepRunning = true;

        requestQueue = new ArrayBlockingQueue<>(1);
        new Thread(new RequestProcessor()).start();
    }

    /**
     * Clear pending requests
     */
    synchronized public void clear() {
        requestQueue.clear();
    }

    /**
     * Stop processing pending requests
     */
    synchronized public void terminate() {
        keepRunning = false;
        post(new EmptyRequest());
    }

    /**
     * Post a new request to be processed replacing any pending request.
     *
     * @param request Runnable request to be processed
     */
    synchronized public boolean post(final Runnable request) {
        try {
            requestQueue.clear();
            requestQueue.put(request);
            return true;
        } catch (InterruptedException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            return false;
        }
    }

    /**
     * Perform post processing
     */
    protected void postProcess() throws Exception {
    }

    /**
     * Process runner task
     */
    private class RequestProcessor extends Thread {

        @Override
        public void run() {
            while (keepRunning) {
                try {
                    final Runnable request = requestQueue.take();
                    request.run();
                    postProcess();
                } catch (Exception exception) {
                    LOGGER.log(Level.SEVERE, null, exception);
                }
            }
        }
    }
}

/**
 * Empty Request used during termination
 */
class EmptyRequest implements Runnable {

    @Override
    public void run() {
    }
}
