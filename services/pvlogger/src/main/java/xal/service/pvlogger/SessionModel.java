/*
 * SessionModel.java
 *
 * Created on Tue Jun 01 11:02:19 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger;

import java.util.*;


/**
 * SessionModel manages a single logger session.
 *
 * @author  tap
 * @since Jun 01, 2004
 */
public class SessionModel {
	/** handler of logger change events */
	private final LoggerEventHandler LOGGER_EVENT_HANDLER;
	
	/** Logger Session */
	protected LoggerSession loggerSession;
	
	/** Last snapshot published */
	protected MachineSnapshot lastPublishedSnapshot;
		
	/** wall clock time of the last MPS event */
	private Date lastLoggerEventTime;

	
	/** Constructor */
	public SessionModel( final LoggerSession loggerSession  ) {
		LOGGER_EVENT_HANDLER = new LoggerEventHandler();
		
		lastLoggerEventTime = new Date();
		
		setLoggerSession( loggerSession );
	}
	
	
	/**
	 * Get the logger session
	 * @return the logger session
	 */
	public LoggerSession getLoggerSession() {
		return loggerSession;
	}
	
	
	/** set the logger session */
	public void setLoggerSession( final LoggerSession loggerSession ) {
		if ( loggerSession != null ) {
			loggerSession.removeLoggerChangeListener( LOGGER_EVENT_HANDLER );
		}
		
		this.loggerSession = loggerSession;
		
		loggerSession.addLoggerChangeListener( LOGGER_EVENT_HANDLER );
	}
	
	
	/**
	 * Get the channel group type that is managed by this session model.
	 * @return the label of the channel group managed by this model
	 */
	public String getChannelGroupType() {
		return loggerSession.getChannelGroup().getLabel();
	}
	
	
	/**
	 * Get the most recently published machine snapshot.
	 * @return The most recently published machine snapshot.
	 */
	public MachineSnapshot getLastPublishedSnapshot() {
		return lastPublishedSnapshot;
	}
	
	
	/**
	 * Get the timestamp of the last logger event
	 * @return the wall clock timestamp of the last logger event
	 */
	public Date getLastLoggerEventTime() {
		return lastLoggerEventTime;
	}
	
	
	
	/** handle logger change events */
	protected class LoggerEventHandler extends LoggerChangeAdapter {
		public void snapshotTaken( final LoggerSession logger, final MachineSnapshot snapshot ) {
			lastPublishedSnapshot = snapshot;
			lastLoggerEventTime = new Date();
		}		
	}
}

