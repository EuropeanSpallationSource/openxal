/*
 * ServiceDirectory.java
 *
 * Created on Tue Aug 26 10:35:45 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.service;

import xal.tools.coding.json.JSONCoder;
import xal.tools.coding.*;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;


/**
 * ServiceDirectory is a local point of access for registering and looking up services on a network.
 * It wraps the standard Bonjour mechanism to provide a simple way to register and lookup services
 * by using a Java interface as a service name.  XML-RPC is the communication protocol used for messaging.
 * Both Bonjour and XML-RPC are accepted protocols implemented in multiple languages.
 * @author  tap
 */
public final class ServiceDirectory {
	/** The default directory */
	private static final ServiceDirectory DEFAULT_DIRECTORY;
	
	/** thread pool */
	private final ExecutorService THREAD_POOL;
    
    /** coder for encoding and encoding messages for remote transport */
    private final Coder MESSAGE_CODER;
	
	/** XML-RPC server used for registering services */
    private RpcServer rpcServer;
	
	/** JmDNS instance */
	private JmDNS bonjour;
	
	/** flag indicating bonjour is in loopback mode (i.e. workstation is disconnected from network) */
	private boolean isLoopback; 
	
	/** Table mapping ServiceListener to the corresponding bonjour service listener */
	protected Map<ServiceListener, BonjourServiceListenerInfo> listenerMap;
	
	
	// static initializer
	static {
		DEFAULT_DIRECTORY = new ServiceDirectory();
	}
	
	
	/** ServiceDirectory constructor. */
	public ServiceDirectory() throws ServiceException {
		THREAD_POOL = Executors.newCachedThreadPool();
        MESSAGE_CODER = JSONCoder.getInstance();
		
		listenerMap = new Hashtable<>();
		
		try {
			try {
				bonjour = JmDNS.create( InetAddress.getLocalHost() );
				isLoopback = false;
			}
			catch( Exception exception ) {
				final String message = "Error attempting to initialize JmDNS.  Will attempt to try loopback mode instead of networked mode.";
				Logger.getLogger("global").log( Level.WARNING, message, exception );
				System.err.println( message );
				isLoopback = true;
				bonjour = JmDNS.create( InetAddress.getByName( "127.0.0.1" ) );
			}
			
			// shutdown the service directory when quitting the process
			Runtime.getRuntime().addShutdownHook( new Thread() {
                                @Override
				public void run() {
					System.out.println( "Shutting down services for this process..." );
					ServiceDirectory.this.dispose();
				}
			});
		}
		catch( Exception exception ) {
			final String message = "JmDNS initialization failed.  Services are disabled.";
			Logger.getLogger("global").log( Level.SEVERE, message, exception );
			System.err.println( message);
			exception.printStackTrace();
		}
	}
	
	
	/**
	 * Get the default ServiceDirectory instance.
	 * @return The default ServiceDirectory instance.
	 */
	public static ServiceDirectory defaultDirectory() {
		return DEFAULT_DIRECTORY;
	}
	
	
	/** Shutdown bonjour and the RPC server and dispose of all resources. */
	public void dispose() {
		listenerMap.clear();
		if ( bonjour != null ) {
            try {
                bonjour.close();
                bonjour = null;
            }
            catch( IOException exception ) {
                throw new RuntimeException( "Exception closing bonjour services.", exception );
            }
		}
		if ( rpcServer != null ) {
            try {
                rpcServer.shutdown();
                rpcServer = null;
            }
            catch ( IOException exception ) {
                throw new RuntimeException( "Exception closing the server socket.", exception );
            }
		}
	}
	
	
	/**
	 * Check if service support is active.  If initialization fails, then services will not be active.
	 * @return true if services are available
	 */
	public boolean isActive() {
		return bonjour != null && rpcServer != null;
	}
	
	
	/**
	 * Determine if bonjour is running in loopback mode which would indicate that the computer is isolated from the network.  This flag is meaningful only if the service directory is active.
	 * @return true if the service is running in loopback mode and false if it is on a network
	 */
	public boolean isLoopback() {
		return isLoopback;
	}
    
    
    /** Get a list of standard data types which are supported for coding and decoding */
    public List<String> getStandardCodingTypes() {
        return JSONCoder.getStandardTypes();
    }
    
    
    /** Get a list of all data types which are supported for coding and decoding */
    public List<String> getSupportedCodingTypes() {
        return MESSAGE_CODER.getSupportedTypes();
    }
    
    
    /** 
     * Register the custom type and its associated adaptor to use for encoding and decoding objects of the custom type
     * @param type type to identify and process for encoding and decoding
     * @param adaptor translator between the custom type and representation constructs
     */
    public <CustomType,RepresentationType> void registerCodingType( final Class<CustomType> type, final ConversionAdaptor<CustomType,RepresentationType> adaptor ) {
        MESSAGE_CODER.registerType( type, adaptor );
    }

	
    /**
     * Register a local service provider.
	 * @param protocol The protocol identifying the service type.
	 * @param name The unique name of the service provider.
     * @param provider The provider which handles the service requests.
	 * @return a new service reference for successful registration and null otherwise.
     */
    public <ProtocolType> ServiceRef registerService( final Class<ProtocolType> protocol, final String name, final ProtocolType provider ) throws ServiceException {
		return registerService( protocol, name, provider, new HashMap<>() );
    }
	
    
    /**
     * Register a local service provider.
	 * @param protocol The protocol identifying the service type.
	 * @param serviceName The unique name of the service provider.
     * @param provider The provider which handles the service requests.
	 * @param properties Properties.
	 * @return a new service reference for successful registration and null otherwise.
     */
    public <ProtocolType> ServiceRef registerService( final Class<ProtocolType> protocol, final String serviceName, final ProtocolType provider, final Map<String,Object> properties ) {
		properties.put( ServiceRef.SERVICE_KEY, serviceName );
        
        final String serviceType = getDefaultType( protocol );
		
		try {
            if ( rpcServer == null ) {
                rpcServer = new RpcServer( MESSAGE_CODER );
                rpcServer.start();
            }
              
			int port = rpcServer.getPort();
			
			// add the service to the RPC Server
			rpcServer.addHandler( serviceName, protocol, provider );
			
			// advertise the service to the world
			final String bonjourType = ServiceRef.getFullType( serviceType );
			final ServiceInfo info = ServiceInfo.create( bonjourType, serviceName, port, 0, 0, properties );
			bonjour.registerService( info );
			return new ServiceRef( info );
		}
		catch( Exception exception ) {
			throw new ServiceException( exception, "Exception while attempting to register a service..." );
		}
    }
	
    
    /**
     * Unregister a local service.
	 * @param serviceRef The service reference whose service should be shutdown.
	 * @return true if the service has been unregistered and false otherwise.
     */
    public boolean unregisterService( final ServiceRef serviceRef ) throws ServiceException {
		try {
			bonjour.unregisterService( serviceRef.getServiceInfo() );
			rpcServer.removeHandler( serviceRef.getServiceName() );
			return true;
		}
		catch(Exception exception) {
			throw new ServiceException(exception, "Exception while unregistering a service...");
		}
	}
	
	
	/**
	 * Get a proxy to the service with the given service reference and protocol. 
	 * @param protocol  The protocol implemented by the service.
	 * @param serviceRef The service reference.
	 * @return A proxy implementing the specified protocol for the specified service reference 
	 */
	public <T> T getProxy( final Class<T> protocol, final ServiceRef serviceRef ) {
        final ServiceInfo info = serviceRef.getServiceInfo();
        final String hostAddress = serviceRef.getHostAddress();		
		return new ClientHandler<T>( hostAddress, info.getPort(), serviceRef.getServiceName(), protocol, MESSAGE_CODER ).getProxy();
	}
	
	
	/**
	 * Lookup the service given the fully qualified service type and the fully qualified service name
	 * @param type The fully qualified service type
	 * @param name The fully qualified service name
	 * @return the matching service reference or null if no match is found
	 */
	public ServiceRef lookupService( final String type, final String name ) {
		try {
			ServiceInfo info = bonjour.getServiceInfo( type, name );
			return ( info != null ) ? new ServiceRef( info ) : null;
		}
		catch(Exception exception) {
			throw new ServiceException( exception, "Exception while looking up a service..." );
		}
	}
	
	
	/**
	 * Lookup the service given the fully qualified service type and the fully qualified service name and block until a match is found or the specified timeout has expired.
	 * @param type The fully qualified service type
	 * @param name The fully qualified service name
	 * @param timeout The timeout in milliseconds to block until a match is found
	 * @return the matching service reference or null if no match is found
	 */
	public ServiceRef lookupService( final String type, final String name, final int timeout ) throws ServiceException {
		try {
			final ServiceInfo info = bonjour.getServiceInfo( type, name, timeout );
			return (info != null) ? new ServiceRef(info) : null;
		}
		catch( Exception exception ) {
			throw new ServiceException( exception, "Exception while looking up a service..." );
		}
	}
	
	
	/**
	 * Convenience method for making a request to find service providers of a specific service type and waiting
	 * a specified amount of time to find those services.  It is more preferable, however, to use the "addServiceListener()" 
	 * method instead so as to monitor the availability of services.  Convenience method used when the type is derived from the protocol's name.
	 * @param protocol The protocol identifying the service type for which to find providers.
	 * @param timeout  Time to block in milliseconds while waiting for services to be discovered.
	 * @return An array of services which were found within the specified timeout
	 * @see #addServiceListener
	 */
	public ServiceRef[] findServicesWithType( final Class<?> protocol, final long timeout ) throws ServiceException {
		return findServicesWithType( getDefaultType( protocol ), timeout );
	}
	
	
	/**
	 * Convenience method for making a request to find service providers of a specific service type and waiting a specified amount of time to find those services.  
	 * It is more preferable, however, to use the "addServiceListener()" method instead so as to monitor the availability of services.
	 * @param serviceType  The type of service to find.
	 * @param timeout  Time to block in milliseconds while waiting for services to be discovered.
	 * @return An array of services which were found within the specified timeout
	 * @see #addServiceListener
	 */
	public ServiceRef[] findServicesWithType( final String serviceType, final long timeout ) throws ServiceException {
		final Map<String,ServiceRef> serviceTable = new Hashtable<>();
		
		final ServiceListener listener = new ServiceListener() {
                        @Override
			public void serviceAdded( final ServiceDirectory directory, final ServiceRef serviceRef ) {
				serviceTable.put( serviceRef.getRawName(), serviceRef );
			}
			
                        @Override
			public void serviceRemoved( final ServiceDirectory directory, final String type, final String name ) {
				serviceTable.remove( name );
			}
		};
		
		try {
			addServiceListener( serviceType, listener );
			Thread.sleep( timeout );
			removeServiceListener( listener );
			
			ServiceRef[] services = new ServiceRef[serviceTable.size()];
			Set<Map.Entry<String,ServiceRef>> serviceEntries = serviceTable.entrySet();
			Iterator<Map.Entry<String,ServiceRef>> entryIter = serviceEntries.iterator();
			int index = 0;
			while ( entryIter.hasNext() ) {
				Map.Entry<String,ServiceRef> entry = entryIter.next();
				services[index] = entry.getValue();
				++index;
			}
			
			return services;
		}
		catch( InterruptedException exception ) {
			removeServiceListener( listener );
			Logger.getLogger("global").log( Level.SEVERE, "Error attempting to find services for service type: " + serviceType, exception );
			System.err.println( exception );
		}
		
		return new ServiceRef[0];
	}
	
	
	/**
	 * Add a listener for addition and removal of service providers.  Convenience method used when the type is derived from the protocol's name.
	 * @param protocol The protocol identifying the service type.
	 * @param listener  The receiver of service availability events.
	 */
	public void addServiceListener( final Class<?> protocol, final ServiceListener listener) throws ServiceException {
		addServiceListener( getDefaultType( protocol ), listener );
	}
	
	
	/**
	 * Add a listener for addition and removal of service providers.
	 * @param type  The type of service provided.
	 * @param listener  The receiver of service availability events.
	 */
	public void addServiceListener( final String type, final ServiceListener listener ) throws ServiceException {
		try {
			final String bonjourType = ServiceRef.getFullType( type );
			bonjour.addServiceListener( bonjourType, new javax.jmdns.ServiceListener() {
				{
					listenerMap.put( listener, new BonjourServiceListenerInfo( bonjourType, this ) );
				}
				
				
				/**
				 * A service is added.
				 * @param type the fully qualified type of the service
				 * @param name the fully qualified name of the service
				 */
                                @Override
				public void serviceAdded( final ServiceEvent event ) {
					System.out.println( "Service added: " + event.getName() );
					THREAD_POOL.execute( new Runnable() {
						public void run() {
							event.getDNS().requestServiceInfo( event.getType(), event.getName() );		
						}
					});
				}
				
				
				/**
				 * A service is removed.
				 * @param type the fully qualified type of the service
				 * @param name the fully qualified name of the service
				 */
                                @Override
				public void serviceRemoved( final ServiceEvent event ) {
					System.out.println( "Service removed: " + event.getName() );
					final String type = event.getType();
					listener.serviceRemoved( ServiceDirectory.this, ServiceRef.getBaseType( type ), event.getName() );
				}
				
				
				/**
				 * A service is resolved. Its details are now available in the ServiceInfo record.
				 * @param type the fully qualified type of the service
				 * @param name the fully qualified name of the service
				 * @param info the service info record, or null if the service could be be resolved
				 */
                                @Override
				public void serviceResolved( final ServiceEvent event ) {
					final ServiceInfo info  = event.getInfo();
					final ServiceRef serviceRef = new ServiceRef( info );
					listener.serviceAdded( ServiceDirectory.this, serviceRef );
				}
			});
		}
		catch(Exception exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Error attempting to add a service listener of service type: " + type, exception );
			throw new ServiceException(exception, "Exception while trying to add a service listener...");
		}
	}
	
	
	/**
	 * Remove a listener of service availability events.
	 * @param listener The listener of service availability events.
	 */
	public void removeServiceListener( final ServiceListener listener ) {
		final BonjourServiceListenerInfo info = listenerMap.get( listener );
		final javax.jmdns.ServiceListener bonjourListener = info.LISTENER;
		if ( bonjourListener != null ) {
			bonjour.removeServiceListener( info.TYPE, bonjourListener );
		}
	}
	
	
	/**
	 * Form a valid type based on the specified protocol by replacing the protocol's name with 
	 * a valid name in which "." is replaced by "_".
	 * @param protocol The protocol for which to get a valid type
	 * @return A valid type to represent the given protocol.
	 */
	protected static String getDefaultType( final Class<?> protocol ) {
		String id = protocol.getName();
		return id.replace('.', '_');
	}
	
	
	/**
	 * Get the remote host for the service
	 * @param proxy the proxy to the service at the remote host
	 * @return a string representation of the remote host
	 */
	public static String getHost( final Object proxy ) {
		return getClientHandler( proxy ).getHost();
	}
	
	
	/**
	 * Get the remote port at which the service is available.
	 * @param proxy the proxy to the service at the remote host
	 * @return the remote port at which the service is available
	 */
	public static int getPort( final Object proxy ) {
		return getClientHandler( proxy ).getPort();
	}
	
	
	/**
	 * Get the service name for the remote service
	 * @param proxy the proxy to the remote service
	 * @return The name of the remote service
	 */
	public static String getServiceName( final Object proxy ) {
		return getClientHandler( proxy ).getServiceName();
	}
	
	
	/**
	 * Get the protocol of the remote proxy
	 * @param proxy the proxy to the remote service
	 * @return the interface implemented by the proxy
	 */
	public static Class<?> getProtocol( final Object proxy ) {
		return getClientHandler( proxy ).getProtocol();
	}
	
	
	/**
	 * Convenience method for getting the ClientHandler for the given proxy
	 * @param proxy the proxy for which we seek its client handler
	 * @return the client handler for the proxy
	 */
	protected static ClientHandler<?> getClientHandler( final Object proxy ) {
		return (ClientHandler)Proxy.getInvocationHandler( proxy );
	}
	
	
	
	/** service listener information for Bonjour */
	protected class BonjourServiceListenerInfo {
		/** JmDNS type */
		public final String TYPE;
		
		/** JmDNS service listener */
		public final javax.jmdns.ServiceListener LISTENER;
		
		
		/** Constructor */
		public BonjourServiceListenerInfo( final String type, final javax.jmdns.ServiceListener listener ) {
			TYPE = type;
			LISTENER = listener;
		}
	}
}

