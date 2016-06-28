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
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
final public class ServiceDirectory {
	/** The default directory */
	static final private ServiceDirectory DEFAULT_DIRECTORY = new ServiceDirectory();
	
	/** thread pool */
	final private ExecutorService THREAD_POOL;
    
    /** coder for encoding and ecoding messages for remote transport */
    final private Coder MESSAGE_CODER;
	
	/** XML-RPC server used for registering services */
    private RpcServer _rpcServer;
	
	/** JmDNS instance */
	private JmDNS _bonjour;
	
	/** ServiceDirectory constructor. */
	private ServiceDirectory() throws ServiceException {
		THREAD_POOL = Executors.newCachedThreadPool();
        MESSAGE_CODER = JSONCoder.getInstance();
		
		try {
			try {
				_bonjour = JmDNS.create( InetAddress.getLocalHost() );
			}
			catch( Exception exception ) {
				final String message = "Error attempting to initialize JmDNS.  Will attempt to try loopback mode instead of networked mode.";
				Logger.getLogger("global").log( Level.WARNING, message, exception );
				System.err.println( message );
				_bonjour = JmDNS.create( InetAddress.getByName( "127.0.0.1" ) );
			}
			
			// shutdown the service directory when quitting the process
			Runtime.getRuntime().addShutdownHook( new Thread() {
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
	static public ServiceDirectory defaultDirectory() {
		return DEFAULT_DIRECTORY;
	}
	
	
	/** Shutdown bonjour and the RPC server and dispose of all resources. */
	public void dispose() {
		if ( _bonjour != null ) {
            try {
                _bonjour.close();
                _bonjour = null;
            }
            catch( IOException exception ) {
                throw new RuntimeException( "Exception closing bonjour services.", exception );
            }
		}
		if ( _rpcServer != null ) {
            try {
                _rpcServer.shutdown();
                _rpcServer = null;
            }
            catch ( IOException exception ) {
                throw new RuntimeException( "Exception closing the server socket.", exception );
            }
		}
	}
	
    /**
     * Register a local service provider.
	 * @param protocol The protocol identifying the service type.
	 * @param name The unique name of the service provider.
     * @param provider The provider which handles the service requests.
	 * @return a new service reference for successful registration and null otherwise.
     */
    public <ProtocolType> ServiceRef registerService( final Class<ProtocolType> protocol, final String name, final ProtocolType provider ) throws ServiceException {
		return registerService( protocol, name, provider, new HashMap<String,Object>() );
    }
	
    
    /**
     * Register a local service provider.
	 * @param protocol The protocol identifying the service type.
	 * @param serviceName The unique name of the service provider.
     * @param provider The provider which handles the service requests.
	 * @param properties Properties.
	 * @return a new service reference for successful registration and null otherwise.
     */
    private <ProtocolType> ServiceRef registerService( final Class<ProtocolType> protocol, final String serviceName, final ProtocolType provider, final Map<String,Object> properties ) {
		properties.put( ServiceRef.SERVICE_KEY, serviceName );
        
        final String serviceType = getDefaultType( protocol );
		
		try {
            if ( _rpcServer == null ) {
                _rpcServer = new RpcServer( MESSAGE_CODER );
                _rpcServer.start();
            }
              
			int port = _rpcServer.getPort();
			
			// add the service to the RPC Server
			_rpcServer.addHandler( serviceName, protocol, provider );
			
			// advertise the service to the world
			final String bonjourType = ServiceRef.getFullType( serviceType );
			final ServiceInfo info = ServiceInfo.create( bonjourType, serviceName, port, 0, 0, properties );
			_bonjour.registerService( info );
			return new ServiceRef( info );
		}
		catch( Exception exception ) {
			throw new ServiceException( exception, "Exception while attempting to register a service..." );
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
	private void addServiceListener( final String type, final ServiceListener listener ) throws ServiceException {
		try {
			final String bonjourType = ServiceRef.getFullType( type );
			_bonjour.addServiceListener( bonjourType, new javax.jmdns.ServiceListener() {
				
				/**
				 * A service is added.
				 * @param type the fully qualified type of the service
				 * @param name the fully qualified name of the service
				 */
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
	 * Form a valid type based on the specified protocol by replacing the protocol's name with 
	 * a valid name in which "." is replaced by "_".
	 * @param protocol The protocol for which to get a valid type
	 * @return A valid type to represent the given protocol.
	 */
	private static String getDefaultType( final Class<?> protocol ) {
		String id = protocol.getName();
		return id.replace('.', '_');
	}
	
}