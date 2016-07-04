/*
 * ServiceRef.java
 *
 * Created on Mon Oct 13 10:09:59 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.service;

/**
 * ServiceRef wraps the native Rendezvous ServiceInfo and provides a reference to the service
 * that hides a direct reference to Rendezvous.
 *
 * @author  tap
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class ServiceRef {
    private final String serviceName;
    private final String hostAddress;
    private final int port;
		

	/**
	 * Create a new service reference to wrap the specified service info.
	 */
	ServiceRef(String serviceName, String hostAddress, int port) {
	    this.serviceName = serviceName;
	    this.hostAddress = hostAddress;
	    this.port = port;
	}
	

	/**
	 * Get the unique raw name of the service provider.
	 * @return The raw name of the service provider.
	 */
	public String getRawName() {
		return serviceName; 
	}

	
	/**
	 * Get the address of the remote service.
	 * @return the address of the remote service
	 */
	String getHostAddress() {
        return hostAddress;
	}

	
	/**
	 * @return port used by the remote service.
	 */
	int getPort() {
	    return port;
	}
}

