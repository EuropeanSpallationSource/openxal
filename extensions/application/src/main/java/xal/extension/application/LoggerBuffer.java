/*
 *  LoggerBuffer.java
 *
 *  Created on Tue Sep 14 12:58:35 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.application;

import java.util.logging.*;
import java.util.*;

import xal.tools.messaging.MessageCenter;

/**
 * LoggerBuffer
 *
 * @author   tap
 * @since    Sep 14, 2004
 */
class LoggerBuffer extends Handler {
	/** root handler */
	protected static LoggerBuffer rootHandler;

	/** list of captured records */
	protected List<LogRecord> records;

	/** message center for dispatching messages to registered listeners */
	protected MessageCenter messageCenter;

	/** proxy which forwards events to registered listeners */
	protected LoggerBufferListener eventProxy;

	/** static constructor */
	static {
		setupRootLogger();
	}


	/** Constructor */
	public LoggerBuffer() {
		messageCenter = new MessageCenter( "Logger Buffer" );
		eventProxy = messageCenter.registerSource( this, LoggerBufferListener.class );

		records = new ArrayList<>();
	}


	/**
	 * Add the specified listener to receive LoggerBufferListener events from this instance.
	 *
	 * @param listener  The listener to register to receive events from this instance
	 */
	public void addLoggerBufferListener( final LoggerBufferListener listener ) {
		messageCenter.registerTarget( listener, this, LoggerBufferListener.class );
		synchronized( records ) {
			listener.recordsChanged( this, new ArrayList<>( records ) );
		}
	}


	/**
	 * Remove the specified listener from receiving LoggerBufferListener events from this instance.
	 *
	 * @param listener  The listener to remove from receiving events from this instance.
	 */
	public void removeLoggerBufferListener( final LoggerBufferListener listener ) {
		messageCenter.removeTarget( listener, this, LoggerBufferListener.class );
	}


	/**
	 * Initialize the root logger by creating a logger buffer handler and adding it to the root
	 * logger.
	 */
	public static void setupRootLogger() {
		if ( rootHandler == null ) {
			rootHandler = new LoggerBuffer();
			rootHandler.setLevel( Level.FINEST );
			Logger.getLogger( "" ).addHandler(rootHandler );
		}
	}


	/**
	 * Get the root handler.
	 *
	 * @return   The rootHandler value
	 */
	public static LoggerBuffer getRootHandler() {
		return rootHandler;
	}


	/** Flush the buffer. Presently does nothing. */
        @Override
	public void flush() {
	}
	
	
	/** Clear the log */
	public void clear() {
		synchronized( records ) {
			records.clear();
			eventProxy.recordsChanged( this, Collections.<LogRecord>emptyList() );
		}
	}


	/**
	 * Close the buffer. Presently does nothing.
	 *
	 * @exception SecurityException  presently doesn't get thrown
	 */
        @Override
	public void close() throws SecurityException {
	}


	/**
	 * Record the new log record.
	 *
	 * @param record  the new log record
	 */
        @Override
	public void publish( final LogRecord record ) {
		synchronized ( records ) {
			records.add( record );
			eventProxy.recordsChanged( this, new ArrayList<>(records) );
		}
	}
}

