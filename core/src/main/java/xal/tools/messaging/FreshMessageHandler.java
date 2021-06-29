//
//  FreshMessageHandler.java
//  xal
//
//  Created by Tom Pelaia on 5/22/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.messaging;

import java.io.Serializable;
import xal.tools.FreshProcessor;

import java.util.logging.*;
import java.lang.reflect.*;

/**
 * Asynchronous Message Handler which posts only the most recent pending event
 * and drops earlier ones.
 */
class FreshMessageHandler<T> extends MessageHandler<T> implements Serializable {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FreshMessageHandler.class.getName());

    /**
     * event processor which processes the most recent pending event on of a
     * single thread
     */
    private final FreshProcessor eventProcessor;

    /**
     * Creates new AsynchronousMessageHandler
     */
    public FreshMessageHandler(final TargetDirectory directory, final Class<T> newInterface, final int threadPoolSize) {
        this(directory, null, newInterface, threadPoolSize);
    }

    /**
     * Creates new AsynchronousMessageHandler
     */
    public FreshMessageHandler(final TargetDirectory directory, final Object source, final Class<T> newInterface, final int threadPoolSize) {
        super(directory, source, newInterface, threadPoolSize);

        eventProcessor = new FreshProcessor();
    }

    /**
     * Subclasses should override this method to perform any cleanup prior to
     * removal.
     */
    @Override
    public void terminate() {
        eventProcessor.terminate();
    }

    /**
     * Implement InvocationHandler interface to invoke the specified method with
     * the supplied arguments
     *
     * @param proxy merely provides identification and is not used here
     * @param method method to invoke on the targets
     * @param args arguments supplied to the method
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        method.setAccessible(true);     // allow access to private, protected, default access methods
        final Invoker invoker = new Invoker(method, args);
        eventProcessor.post(invoker);

        return null;
    }

    /**
     * identifies the message handler as asynchronous
     *
     * @return false since this handler is asynchronous
     */
    @Override
    public final boolean isSynchronous() {
        return false;
    }

    /**
     * Helper class for executing the invoke method in a thread
     */
    private class Invoker implements Runnable {

        private final Method method;
        private final Object[] args;

        /**
         * Constructor
         */
        public Invoker(final Method newMethod, final Object[] newArgs) {
            method = newMethod;
            args = newArgs;
        }

        /**
         * forward messages to the targets
         */
        @Override
        public void run() {
            try {
                for (final Object target : targets()) {
                    method.invoke(target, args);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                final String message = "Error invoking method: " + method + " for protocol " + protocol + " for source " + source;
                LOGGER.log(Level.SEVERE, message, exception);
            }
        }
    }
}
