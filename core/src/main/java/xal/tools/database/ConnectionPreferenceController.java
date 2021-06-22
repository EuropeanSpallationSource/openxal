/*
 * ConnectionPreferenceController.java
 *
 * Created on Tue Dec 30 14:48:55 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.database;

import xal.tools.apputils.PathPreferenceSelector;

import java.util.prefs.*;


/**
 * ConnectionPreferenceController is a convenience class for displaying a PathPreferenceSelector for a database configuration.
 * @author  tap
 */
public class ConnectionPreferenceController {
	// constants
	private static final String URL_KEY;
	private static final Preferences DEFAULTS;
	private static final String SUFFIX = ".dbconfig";
	private static final String DESCRIPTION = "Database Configuration";
	
	
	static {
		URL_KEY = DBConfiguration.PREFERENCES_URL_KEY;
		DEFAULTS = DBConfiguration.getDefaults();
	}
	
	
	/** Constructor which is hidden since this class only has static methods. */
	protected ConnectionPreferenceController() {}
	
	
	/**
	 * Display the PathPreferenceSelector with the specified Frame as the owner.
	 * @param owner The owner of the PathPreferenceSelector dialog.
	 * @return true if the the user has committed changes
	 */
	public static boolean displayPathPreferenceSelector( final java.awt.Frame owner ) {
		final PathPreferenceSelector selector;
		selector = new PathPreferenceSelector( owner, DEFAULTS, URL_KEY, SUFFIX, DESCRIPTION );
		selector.setLocationRelativeTo( owner );
		selector.setVisible( true );
		return selector.hasSavedChanges();
	}
	
	
	/**
	 * Display the PathPreferenceSelector with the specified Dialog as the owner.
	 * @param owner The owner of the PathPreferenceSelector dialog.
	 * @return true if the the user has committed changes
	 */
	public static boolean displayPathPreferenceSelector( final java.awt.Dialog owner ) {
		final PathPreferenceSelector selector;
		selector = new PathPreferenceSelector( owner, DEFAULTS, URL_KEY, SUFFIX, DESCRIPTION );
		selector.setLocationRelativeTo( owner );
		selector.setVisible( true );
		return selector.hasSavedChanges();
	}
	
	
	/**
	 * Display the PathPreferenceSelector with no owner.
	 * @return true if the the user has committed changes
	 */
	public static boolean displayPathPreferenceSelector() {
		final PathPreferenceSelector selector;
		selector = new PathPreferenceSelector( DEFAULTS, URL_KEY, SUFFIX, DESCRIPTION );
		selector.setVisible( true );
		return selector.hasSavedChanges();
	}
}
