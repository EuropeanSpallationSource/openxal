/*
 * RpcServer.java
 *
 * Created on July 18, 2003, 10:23 AM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.extension.service;

import xal.tools.coding.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

/**
 * RpcServer implements a server which handles remote requests against
 * registered handlers.
 *
 * @author tap
 */
public class RpcServer {

    private static final Logger LOGGER = Logger.getLogger(RpcServer.class.getName());

    /**
     * delimiter for encoding remote messages
     */
    private static final String REMOTE_MESSAGE_DELIMITER = "#";

    /**
     * socket which listens for and dispatches remote requests
     */
    private final ServerSocket serverSocket;

    /**
     * set of active sockets serving remote requests
     */
    private final Set<Socket> remoteSockets;

    /**
     * remote request handlers keyed by service name
     */
    private final Map<String, RemoteRequestHandler<?>> remoteRequestHandlers;

    /**
     * coder for encoding and decoding messages for remote transport
     */
    private final Coder messageCoder;

    /**
     * Constructor
     */
    public RpcServer(final Coder messageCoder) throws java.io.IOException {
        this.messageCoder = messageCoder;

        remoteRequestHandlers = new HashMap<>();
        serverSocket = new ServerSocket(0);
        remoteSockets = new HashSet<>();

    }

    /**
     * Get the port used by the web server.
     *
     * @return The port used by the web server.
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Get the host address used for the web server.
     *
     * @return The host address used for the web server.
     */
    public String getHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException exception) {
            final String message = "Error getting the host name of the RPC Server.";
            Logger.getLogger("global").log(Level.SEVERE, message, exception);
            LOGGER.log(Level.SEVERE, null, exception);
            return null;
        }
    }

    /**
     * start the server, listen for remote requests and dispatch them to the
     * appropriate handlers
     */
    public void start() {
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    final Socket remoteSocket = serverSocket.accept();
                    remoteSocket.setKeepAlive(true);
                    synchronized (remoteSockets) {
                        remoteSockets.add(remoteSocket);
                    }
                    processRemoteEvents(remoteSocket);
                }
            } catch (SocketException exception) {
                // server being shutdown
                LOGGER.log(Level.INFO, null, exception);
            } catch (IOException exception) {
                LOGGER.log(Level.SEVERE, null, exception);
            }
        }).start();
    }

    /**
     * shutdown the server
     */
    public void shutdown() throws IOException {
        // stop establishing new remote sockets
        serverSocket.close();

        // close the existing remote sockets
        final Set<Socket> sockets = new HashSet<>();
        synchronized (remoteSockets) {
            sockets.addAll(remoteSockets);
        }
        for (final Socket socket : sockets) {
            try {
                socket.close();
            } catch (IOException exception) {
                LOGGER.log(Level.SEVERE, null, exception);
            }
        }

        // clear the remote sockets
        synchronized (remoteSockets) {
            remoteSockets.clear();
        }
    }

    /**
     * cleanup the remote socket which has been closed
     */
    private void cleanupClosedRemoteSocket(final Socket remoteSocket) {
        synchronized (remoteSockets) {
            remoteSockets.remove(remoteSocket);
        }
    }

    /**
     * add a handler to associate with the specified service and provider
     */
    public <T> void addHandler(final String serviceName, final Class<T> protocol, final T provider) {
        final RemoteRequestHandler<T> handler = new RemoteRequestHandler<>(serviceName, protocol, provider);
        remoteRequestHandlers.put(serviceName, handler);
    }

    /**
     * remove the registered handler
     */
    public void removeHandler(final String serviceName) {
        remoteRequestHandlers.remove(serviceName);
    }

    /**
     * process remote socket events
     */
    // need to cast generic request object to Map
    @SuppressWarnings("unchecked")
    private void processRemoteEvents(final Socket remoteSocket) {
        new Thread(() -> {
            if (!remoteSocket.isClosed()) {
                // process the initial handshake
                try {
                    WebSocketIO.processRequestHandshake(remoteSocket);
                } catch (IOException exception) {
                    throw new RuntimeException("Exception handling handshake", exception);
                }
            }

            // process the messages as they arrive
            while (!remoteSocket.isClosed()) {
                String jsonRequest = null;
                try {
                    jsonRequest = WebSocketIO.readMessage(remoteSocket);
                } catch (IOException | WebSocketIO.SocketPrematurelyClosedException exception) {
                    LOGGER.log(Level.WARNING, null, exception);
                    throw new RemoteClientDroppedException("Session has been closed during read...");
                }

                try {
                    final Object requestObject = messageCoder.decode(jsonRequest);
                    if (requestObject instanceof Map) {
                        final Map<String, Object> request = (Map<String, Object>) requestObject;
                        final String message = (String) request.get("message");
                        final String[] messageParts = decodeRemoteMessage(message);
                        final String serviceName = messageParts[0];
                        final String methodName = messageParts[1];
                        final Number requestID = (Number) request.get("id");
                        final Object[] params = (Object[]) request.get("params");

                        final RemoteRequestHandler<?> handler = remoteRequestHandlers.get(serviceName);
                        final EvaluationResult result = handler.evaluateRequest(methodName, params);

                        // methods marked with the OneWay annotation return immediately and do not provide any response
                        final boolean provideResponse = !result.isOneWay();

                        if (provideResponse) {
                            final Map<String, Object> response = new HashMap<>();
                            response.put("result", result.getValue());
                            response.put("id", requestID);
                            response.put("error", result.getRuntimeExceptionWrapper());

                            final String jsonResponse = messageCoder.encode(response);
                            WebSocketIO.sendMessage(remoteSocket, jsonResponse);
                        }
                    }
                } catch (IOException | RemoteClientDroppedException exception) {
                    LOGGER.log(Level.WARNING, null, exception);
                    if (!remoteSocket.isClosed()) {
                        try {
                            remoteSocket.close();
                        } catch (IOException closeException) {
                            LOGGER.log(Level.SEVERE, null, closeException);
                        }
                    }

                    cleanupClosedRemoteSocket(remoteSocket);
                    return;
                }
            }
        }).start();
    }

    /**
     * encode the service name and method name into the remote message
     */
    static String encodeRemoteMessage(final String serviceName, final String methodName) {
        return serviceName + REMOTE_MESSAGE_DELIMITER + methodName;
    }

    /**
     * decode the service name and method name from the remote message
     */
    static String[] decodeRemoteMessage(final String message) {
        return message.split(REMOTE_MESSAGE_DELIMITER, 2);
    }
}

/**
 * Handles remote requests
 */
class RemoteRequestHandler<T> {

    /**
     * primitive type wrappers keyed by type
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_WRAPPERS;

    /**
     * protocol of available methods
     */
    private final Class<T> protocol;

    /**
     * object to message
     */
    private final T provider;

    /**
     * cache of methods keyed by their signature
     */
    private final Map<String, Method> methodCache;

    // static initializer
    static {
        PRIMITIVE_TYPE_WRAPPERS = populatePrimitiveTypeWrappers();
    }

    private static final Logger LOGGER = Logger.getLogger(RemoteRequestHandler.class.getName());

    /**
     * Constructor
     */
    public RemoteRequestHandler(final String serviceName, final Class<T> protocol, final T provider) {
        this.protocol = protocol;
        this.provider = provider;
        methodCache = new HashMap<>();
    }

    /**
     * populate the table of primitive type wrappers
     */
    private static Map<Class<?>, Class<?>> populatePrimitiveTypeWrappers() {
        final Map<Class<?>, Class<?>> table = new HashMap<>();

        table.put(Integer.TYPE, Integer.class);
        table.put(Long.TYPE, Long.class);
        table.put(Short.TYPE, Short.class);
        table.put(Byte.TYPE, Byte.class);
        table.put(Character.TYPE, Character.class);
        table.put(Float.TYPE, Float.class);
        table.put(Double.TYPE, Double.class);
        table.put(Boolean.TYPE, Boolean.class);

        return table;
    }

    /**
     * Evaluate the request
     */
    public EvaluationResult evaluateRequest(final String methodName, final Object[] methodParams) {
        final Class<?>[] methodParamTypes = new Class<?>[methodParams.length];
        for (int index = 0; index < methodParams.length; index++) {
            final Object param = methodParams[index];
            methodParamTypes[index] = param != null ? param.getClass() : null;
        }

        final Method method = getMethod(methodName, methodParamTypes);
        final boolean isOneWay = method.isAnnotationPresent(OneWay.class);

        try {
            final Object value = method.invoke(provider, methodParams);
            return new EvaluationResult(value, isOneWay);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            return new EvaluationResult(null, isOneWay, exception.getCause());
        }
    }

    /**
     * Get the method either from the cache or find and cache it if necessary
     */
    private Method getMethod(final String methodName, final Class<?>[] parameterTypes) {
        final String methodSignature = getMethodSignature(methodName, parameterTypes);

        Method method = methodCache.get(methodSignature);
        if (method == null) {
            method = findMethod(methodName, parameterTypes);
            methodCache.put(methodSignature, method);
        }
        return method;
    }

    /**
     * Get the method signature for the specified method name and parameter
     * types
     */
    private static String getMethodSignature(final String methodName, final Class<?>[] parameterTypes) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(methodName);
        for (final Class<?> parameterType : parameterTypes) {
            final String parameterTypeID = parameterType != null ? parameterType.getName() : "";
            buffer.append(":");
            buffer.append(parameterTypeID);
        }
        return buffer.toString();
    }

    /**
     * Find the best method in the protocol that matches the method name and
     * parameters
     */
    private Method findMethod(final String methodName, final Class<?>[] parameterTypes) {
        try {
            return protocol.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException exception) {
            try {
                final Method[] methods = protocol.getMethods();

                int bestScore = 0;
                Method bestMethod = null;
                for (final Method method : methods) {
                    final int score = matchScore(method, methodName, parameterTypes);
                    if (score > bestScore) {
                        bestScore = score;
                        bestMethod = method;
                    }
                }

                if (bestMethod != null) {
                    return bestMethod;
                } else {
                    throw new RuntimeException("No matching method found for <" + methodName + "" + parameterTypes + ">", exception);
                }
            } catch (RuntimeException searchException) {
                throw new RuntimeException("Exception evaluating the remote request with the request handler.", searchException);
            }
        }
    }

    /**
     * Score the match between the method and the specified method name and
     * parameter types. Higher scores are better and zero means no match.
     */
    private static int matchScore(final Method method, final String methodName, final Class<?>[] parameterTypes) {
        int score = 0;

        final Class<?>[] methodParamTypes = method.getParameterTypes();

        if (method.getName().equals(methodName) && methodParamTypes.length == parameterTypes.length) {
            // credit for matching the name and parameters length
            score += 1;
        } else {
            // no match
            return 0;
        }

        // test each parameter type for consistency
        for (int index = 0; index < methodParamTypes.length; index++) {
            final Class<?> methodParamType = methodParamTypes[index];
            final Class<?> parameterType = parameterTypes[index];

            if (methodParamType.isPrimitive()) {
                if (parameterType == null) {
                    // no match since a primitive cannot be null
                    return 0;
                } else if (PRIMITIVE_TYPE_WRAPPERS.get(methodParamType).equals(parameterType)) {
                    // primitive type's corresponding wrapper matches parameter type
                    score += 1;
                } else {
                    // no match since the primitive must be mapped to its corresponding wrapper class
                    return 0;
                }
            } else if (methodParamType.equals(parameterType)) {
                // bonus for exact match
                score += 2;
            } else if (parameterType.isAssignableFrom(methodParamType)) {
                // types are consistent
                score += 1;
            } else {
                // no match for this parameter
                return 0;
            }
        }

        return score;
    }
}

/**
 * result of evaluating the requested method
 */
class EvaluationResult {

    /**
     * result of the method evaluation
     */
    private final Object value;

    /**
     * indicates whether the method is one way (no response to remote caller)
     */
    private final boolean isOneWay;

    /**
     * exception
     */
    private final Throwable exception;

    /**
     * Constructor
     */
    public EvaluationResult(final Object value, final boolean isOneWay) {
        this(value, isOneWay, null);
    }

    /**
     * Constructor
     */
    public EvaluationResult(final Object value, final boolean isOneWay, final Throwable exception) {
        this.value = value;
        this.isOneWay = isOneWay;
        this.exception = exception;
    }

    /**
     * determine whether the call is one way
     */
    public boolean isOneWay() {
        return isOneWay;
    }

    /**
     * get the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * get the exception
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * wrap the raw exception as runtime exception
     */
    public RuntimeException getRuntimeExceptionWrapper() {
        if (exception != null) {
            final RuntimeException wrapper = new RuntimeException(exception);
            wrapper.setStackTrace(exception.getStackTrace());
            return wrapper;
        } else {
            return null;
        }
    }
}

/**
 * indicates that a remote client connection has been dropped
 */
class RemoteClientDroppedException extends RuntimeException {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    public RemoteClientDroppedException(final String message) {
        super(message);
    }
}
