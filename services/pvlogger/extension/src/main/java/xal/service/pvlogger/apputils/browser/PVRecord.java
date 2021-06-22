//
// PVRecord.java
// Open XAL
//
// Created by Pelaia II, Tom on 10/2/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.pvlogger.apputils.browser;


/** PVRecord */
public class PVRecord {
	/** process variable signal */
	private final String SIGNAL;

	/** indicates the enabled status of the signal */
	private boolean enabled;


	/** Primary Constructor */
    public PVRecord( final String signal, final boolean enabled ) {
		SIGNAL = signal;
		this.enabled = enabled;
    }


	/** Constructor */
    public PVRecord( final String signal ) {
		this( signal, true );
    }


	/** get the signal */
	public String getSignal() {
		return SIGNAL;
	}


	/** determine whether the signal is enabled */
	public boolean getEnabled() {
		return enabled;
	}


	/** set the signal enable status */
	public void setEnabled( final boolean enabled ) {
		this.enabled = enabled;
	}
}
