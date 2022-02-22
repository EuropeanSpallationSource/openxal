/*
 * ClientHandler.java
 *
 * Created on July 18, 2003, 9:54 AM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.extension.service;

import xal.tools.coding.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ClientHandler handles messages sent to the proxy by forwarding them to the
 * service associated with the proxy.
 *
 * @author tap
 */
class ClientHandler<T> implements InvocationHandler {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    /**
     * protocol implemented by the remote service and dispatched through the
     * proxy
     */
    private final Class<T> serviceProtocol;

    /**
     * name of the remote service
     */
    private final String serviceName;

    /**
     * proxy which forwards invocations to the remote service
     */
    private final T proxy;

    /**
     * remote host
     */
    private final String remoteHost;

    /**
     * remote port
     */
    private final int remotePort;

    /**
     * message processors which are available
     */
    private final ConcurrentLinkedQueue<SerialRemoteMessageProcessor> messageProcessors;

    /**
     * request ID counter is incremented to provide a unique ID for each request
     */
    private AtomicInteger requestIDCounter;
    /**
     * coder for encoding and decoding messages for remote transport
     */
    private final Coder messageCoder;

    /**
     * Creates a new ClientHandler to handle service requests.
     *
     * @param host The host where the service is running.
     * @param port The port through which the service is provided.
     * @param name The name of the service.
     * @param newProtocol The interface the service provides.
     * @param messageCoder coder for encoding and decoding messages for remote
     * transport
     */
    public ClientHandler(final String host, final int port, final String name, final Class<T> newProtocol, final Coder messageCoder) {
        remoteHost = host;
        remotePort = port;
        serviceName = name;
        serviceProtocol = newProtocol;
        this.messageCoder = messageCoder;

        proxy = createProxy();

        messageProcessors = new ConcurrentLinkedQueue<>();

        requestIDCounter = new AtomicInteger(0);
    }

    /**
     * Get the next request ID and increment it
     */
    private int getNextRequestID() {
        return requestIDCounter.incrementAndGet();
    }

    /**
     * Get the interface managed by this handler.
     *
     * @return The interface managed by this handler.
     */
    public Class<?> getProtocol() {
        return serviceProtocol;
    }

    /**
     * Get the name of the remote service.
     *
     * @return The name of the remote service.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the host name of the remote service.
     *
     * @return The host name of the remote service.
     */
    public String getHost() {
        return remoteHost;
    }

    /**
     * Get the port of the remote service.
     *
     * @return The port of the remote service.
     */
    public int getPort() {
        return remotePort;
    }

    /**
     * Get the proxy that will forward requests to the remote service.
     *
     * @return The proxy that will forward requests to the remote service.
     */
    public T getProxy() {
        return proxy;
    }

    /**
     * Create the proxy for this handler to message.
     *
     * @return The proxy that will forward requests to the remote service.
     */
    // we have not choice but to cast since newProxyInstance does not support generics
    @SuppressWarnings({"unchecked", "rawtypes"})
    private T createProxy() {
        ClassLoader loader = this.getClass().getClassLoader();
        Class[] protocols = new Class[]{serviceProtocol, ServiceState.class};

        return (T) Proxy.newProxyInstance(loader, protocols, this);
    }

    /**
     * dispose of resources
     */
    public void dispose() {
        final List<SerialRemoteMessageProcessor> processors = new ArrayList<>();

        synchronized (messageProcessors) {
            processors.addAll(messageProcessors);
            messageProcessors.clear();
        }

        for (final SerialRemoteMessageProcessor processor : processors) {
            processor.dispose();
        }
    }

    /**
     * dispose of resources upon collection
     */
    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    /**
     * get the next remote message processor which is free for processing a
     * fresh message
     */
    private SerialRemoteMessageProcessor nextRemoteMessageProcessor() {
        SerialRemoteMessageProcessor processor = null;

        synchronized (messageProcessors) {
            processor = messageProcessors.poll();
        }

        if (processor != null) {
            return processor;
        } else {
            return new SerialRemoteMessageProcessor(remoteHost, remotePort, messageCoder);
        }
    }

    /**
     * recycle a message processor which is no longer in use
     */
    private void recycleRemoteMessageProcessor(final SerialRemoteMessageProcessor processor) {
        synchronized (messageProcessors) {
            messageProcessors.add(processor);
        }
    }

    /**
     * Invoke the specified method on the proxy to implement the
     * InvocationHandler interface. The method is evaluated by calling the
     * remote method using JSON-RPC.
     *
     * @param proxy The instance on which the method is invoked. This argument
     * is unused.
     * @param method The method to implement.
     * @param args The array of arguments to pass to the method.
     * @return The result of the method invocation.
     * @throws xal.extension.service.RemoteMessageException if an exception
     * occurs while invoking this remote message.
     */
    // must cast generic response object to Map
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws RemoteMessageException, RemoteServiceDroppedException {
        try {
            // test whether the remote service implements the method
            serviceProtocol.getMethod(method.getName(), method.getParameterTypes());
            return performRemoteServiceCall(method, args);
        } catch (NoSuchMethodException exception) {
            LOGGER.log(Level.OFF, null, exception);
            return performServiceStateCall(method, args);
        }
    }

    /**
     * perform the remote service call
     */
    private Object performRemoteServiceCall(final Method method, final Object[] args) throws RemoteMessageException, RemoteServiceDroppedException {
        try {
            final long requestID = getNextRequestID();
            final Object[] params = args != null ? args : new Object[0];

            final String methodName = method.getName();
            final Map<String, Object> request = new HashMap<>();
            final String message = RpcServer.encodeRemoteMessage(serviceName, methodName);
            request.put("message", message);
            request.put("params", params);
            request.put("id", requestID);
            final String jsonRequest = messageCoder.encode(request);

            // methods marked with the OneWay annotation return immediately and do not wait for a response from the service
            final boolean waitForResponse = !method.isAnnotationPresent(OneWay.class);

            // submit the request and wait for the response if expected
            // get the next available processor from the stack
            final SerialRemoteMessageProcessor processor = nextRemoteMessageProcessor();
            final PendingResult pendingResult = processor.submitRemoteRequest(jsonRequest, waitForResponse);
            if (!processor.isClosed()) {
                // push the processor back onto the stack if it is still viable
                recycleRemoteMessageProcessor(processor);
            }
            if (pendingResult != null) {
                final RuntimeException remoteException = pendingResult.getRemoteException();
                if (remoteException == null) {
                    return pendingResult.getValue();
                } else {
                    if (remoteException instanceof RemoteServiceDroppedException) {
                        // just rethrow it since it is a service connection issue and not an issue generated by the remote service itself
                        throw remoteException;
                    } else {
                        throw new RemoteMessageException("Exception thrown during execution of the remote request on the remote service.", remoteException);
                    }
                }
            } else {
                return null;
            }
        } catch (IllegalArgumentException | RemoteMessageException | RemoteServiceDroppedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new RuntimeException("Exception performing invocation for remote request.", exception);
        }
    }

    /**
     * perform the service state call on the local client handler
     */
    private Object performServiceStateCall(final Method method, final Object[] args) {
        try {
            return method.invoke(newServiceState(), args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
            throw new RuntimeException("Exception performing local service state call on proxy to remote.", exception);
        }
    }

    /**
     * Create new service state instance to implement the service state
     * interface
     */
    private ServiceState newServiceState() {
        return new ServiceState() {
            /**
             * Get the name of the remote service.
             *
             * @return The name of the remote service.
             */
            @Override
            public String getServiceName() {
                return ClientHandler.this.getServiceName();
            }

            /**
             * Get the host name of the remote service.
             *
             * @return The host name of the remote service.
             */
            @Override
            public String getServiceHost() {
                return ClientHandler.this.getHost();
            }

            /**
             * Get the port of the remote service.
             *
             * @return The port of the remote service.
             */
            @Override
            public int getServicePort() {
                return ClientHandler.this.getPort();
            }

            /**
             * dispose of this proxy's resources
             */
            @Override
            public void disposeServiceResources() {
                ClientHandler.this.dispose();
            }
        };
    }
}

/**
 * pending result
 */
class PendingResult {

    /**
     * result value
     */
    private Object value;

    /**
     * remote exception
     */
    private RuntimeException remoteException;

    /**
     * set the result's value
     */
    public void setValue(final Object value) {
        this.value = value;
    }

    /**
     * get the result's value
     */
    public Object getValue() {
        return value;
    }

    /**
     * set the error message
     */
    public void setRemoteException(final RuntimeException exception) {
        remoteException = exception;
    }

    /**
     * get the error message
     */
    public RuntimeException getRemoteException() {
        return remoteException;
    }
}

/**
 * Remote message processor that can handle serial (noncurrent) requests over
 * the same socket.
 */
class SerialRemoteMessageProcessor {

    /**
     * socket for sending and receiving remote messages
     */
    private final Socket remoteSocket;

    /**
     * coder for encoding and decoding messages for remote transport
     */
    private final Coder messageCoder;

    private static final Logger LOGGER = Logger.getLogger(SerialRemoteMessageProcessor.class.getName());

    /**
     * Creates a new ClientHandler to handle service requests.
     *
     * @param host The host where the service is running.
     * @param port The port through which the service is provided.
     * @param messageCoder coder for encoding and decoding messages for remote
     * transport
     */
    public SerialRemoteMessageProcessor(final String host, final int port, final Coder messageCoder) {
        this.messageCoder = messageCoder;

        remoteSocket = makeRemoteSocket(host, port);

        try {
            WebSocketIO.performHandshake(remoteSocket);
        } catch (IOException | WebSocketIO.SocketPrematurelyClosedException exception) {
            throw new RuntimeException("Exception creating new remote socket.", exception);
        }
    }

    /**
     * make a new remote socket
     */
    private static Socket makeRemoteSocket(final String host, final int port) {
        try {
            final Socket remoteSocket = new Socket(host, port);
            remoteSocket.setKeepAlive(true);
            return remoteSocket;
        } catch (UnknownHostException exception) {
            throw new RemoteServiceDroppedException("Attempt to open a socket to an unknown host.", exception);
        } catch (IOException exception) {
            throw new RemoteServiceDroppedException("IO Exception attempting to open a new socket.", exception);
        } catch (Exception exception) {
            throw new RemoteServiceDroppedException("Exceptiong attempting to establish a new remote socket.", exception);
        }
    }

    /**
     * determine whether the socket is closed
     */
    public boolean isClosed() {
        return remoteSocket.isClosed();
    }

    /**
     * dispose of resources
     */
    public void dispose() {
        if (!remoteSocket.isClosed()) {
            try {
                remoteSocket.close();
            } catch (IOException exception) {
                throw new RuntimeException("Excepting closing remote client socket.", exception);
            }
        }
    }

    /**
     * dispose of resources upon collection
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
     * process the remote response
     */
    // no way to know response Object type at compile time
    @SuppressWarnings("unchecked")
    private void processRemoteResponse(final PendingResult pendingResult) throws java.io.IOException {
        try {
            final String jsonResponse = WebSocketIO.readMessage(remoteSocket);
            if (jsonResponse != null) {
                final Object responseObject = messageCoder.decode(jsonResponse);
                if (responseObject instanceof Map) {
                    final Map<String, Object> response = (Map<String, Object>) responseObject;
                    final Object result = response.get("result");
                    final RuntimeException remoteException = (RuntimeException) response.get("error");

                    pendingResult.setValue(result);
                    pendingResult.setRemoteException(remoteException);
                }
            }
        } catch (WebSocketIO.SocketPrematurelyClosedException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            cleanupClosedSocket(pendingResult, new RemoteServiceDroppedException("The remote socket has closed while reading the remote response..."));
        }
    }

    /**
     * cleanup after discovering the socket has closed
     */
    private void cleanupClosedSocket(final PendingResult pendingResult, final Exception exception) {
        // encapsulate the exception in a runtime exception if necessary since that is what gets passed back to the calling method
        final RuntimeException resultException = exception instanceof RuntimeException ? (RuntimeException) exception : new RuntimeException(exception);

        // assign the exception to the pending result
        if (pendingResult != null) {
            pendingResult.setRemoteException(resultException);
        }
    }

    /**
     * Submit the remote request
     */
    public PendingResult submitRemoteRequest(final String jsonRequest, final boolean hasResponse) {
        try {
            WebSocketIO.sendMessage(remoteSocket, jsonRequest);

            if (hasResponse) {
                final PendingResult pendingResult = new PendingResult();

                try {
                    processRemoteResponse(pendingResult);
                    return pendingResult;
                } catch (IOException exception) {
                    LOGGER.log(Level.SEVERE, null, exception);
                    return pendingResult;
                } finally {
                    // if the socket closes, cleanup the connection resources and forward the exception to the client
                    if (remoteSocket.isClosed()) {
                        cleanupClosedSocket(pendingResult, new RemoteServiceDroppedException("The remote socket has closed while processing the remote response..."));
                    }
                }
            } else {
                return null;
            }
        } catch (SocketException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            if (!remoteSocket.isClosed()) {
                try {
                    remoteSocket.close();
                } catch (IOException closeException) {
                    LOGGER.log(Level.INFO, null, closeException);
                }
            }
            if (hasResponse) {
                final PendingResult pendingResult = new PendingResult();
                cleanupClosedSocket(new PendingResult(), new RemoteServiceDroppedException("The remote socket has closed while processing the remote response..."));
                return pendingResult;
            } else {
                return null;
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            return null;
        }
    }
}
