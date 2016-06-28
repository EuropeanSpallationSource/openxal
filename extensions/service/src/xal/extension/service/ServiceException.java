/*
 * ServiceException.java
 *
 * Created on Thu Jul 08 14:58:17 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.service;


/**
 * ServiceException
 *
 * @author  tap
 * @since Jul 08, 2004
 */
class ServiceException extends RuntimeException {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/**
	 * Primary constructor
	 */
	ServiceException(Throwable cause, String message) {
		super(message, cause);
	}
	
}

