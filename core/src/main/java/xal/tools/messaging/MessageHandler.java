/*
 * MessageHandler.java
 *
 * Created on February 5, 2002, 11:40 AM
 */
package xal.tools.messaging;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

/**
 * MessageHandler is an abstract class whose subclasses receive and forward
 * messages. It provides the foundation for handling messages.
 *
 * @author tap
 */
abstract class MessageHandler<T> implements InvocationHandler, Serializable {

    /**
     * required for Serializable
     */
    private static final long serialVersionUID = 1L;

    protected Class<T> protocol;
    protected transient Object source;
    protected transient T proxy;
    protected transient Thread[] threadPool;
    protected TargetDirectory targetDirectory;

    /**
     * Creates new MessageHandler
     */
    protected MessageHandler(final TargetDirectory newDirectory, final Class<T> newProtocol, final int threadPoolSize) {
        this(newDirectory, null, newProtocol, threadPoolSize);
    }

    /**
     * Creates new MessageHandler
     */
    protected MessageHandler(final TargetDirectory newDirectory, final Object newSource, final Class<T> newProtocol, final int threadPoolSize) {
        targetDirectory = newDirectory;
        source = newSource;
        protocol = newProtocol;
        threadPool = new Thread[threadPoolSize];
        createProxy();
    }

    /**
     * Subclasses should override this method to perform any cleanup prior to
     * removal.
     */
    public void terminate() {
    }

    /**
     * return the interface managed by this handler
     */
    public Class<T> getProtocol() {
        return protocol;
    }

    /**
     * return the source of the messages
     */
    public Object getSource() {
        return source;
    }

    /**
     * return the proxy that will forward messages to registered targets
     */
    public T getProxy() {
        return proxy;
    }

    /**
     * create the proxy for this handler to message
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void createProxy() {
        ClassLoader loader = this.getClass().getClassLoader();
        // need to suppress the rawtypes as Generics aren't supported for array creation
        Class[] protocols = new Class[]{protocol};

        proxy = (T) Proxy.newProxyInstance(loader, protocols, this);
    }

    /**
     * subclasses must override whether they support synchronous or asynchronous
     * messages
     *
     * @return true if the messaging is synchronous and false if not
     */
    public abstract boolean isSynchronous();

    /**
     * implement InvocationHandler interface
     */
    /**
     * invoke method
     */
    @Override
    public abstract Object invoke(final Object proxy, final Method method, final Object[] args);

    /**
     * get all targets associated with the source and protocol and just the
     * protocol
     */
    protected Set<T> targets() {
        final Set<T> targetSet = new HashSet<>();

        // add targets directly associated with the protocol and the target
        final Set<T> directTargets = targetDirectory.targets(source, protocol);
        targetSet.addAll(directTargets);

        // add targets associated with the protocol but no target
        final Set<T> anonymousTargets = targetDirectory.targets(null, protocol);
        targetSet.addAll(anonymousTargets);

        return targetSet;
    }
}
