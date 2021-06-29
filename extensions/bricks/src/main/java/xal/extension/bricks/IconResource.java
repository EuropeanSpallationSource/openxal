//
//  IconResource.java
//  xal
//
//  Created by Thomas Pelaia on 9/6/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.net.MalformedURLException;
import javax.swing.ImageIcon;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.tools.IconLib;


/** Icon identified by a URL */
public class IconResource extends ImageIcon {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(IconResource.class.getName());

	/** URL of the image for the icon */
	private final URL imageUrl;
	
	/** group of icons */
	protected final String group;
	
	/** icon name */
	protected final String iconName;
	
	
	/** Constructor */
	private IconResource( final URL imageURL, final String group, final String iconName ) {
		super( imageURL );
		
		imageUrl = imageURL;
		this.group = group;
		this.iconName = iconName;
	}
	
	
	/** create a new instance */
	static IconResource getInstance( final URL contextURL, final String group, final String iconName ) {
		try {
			final URL imageURL = group != null && !group.isEmpty() ? IconLib.getIconURL( group, iconName ) : new URL( contextURL, iconName );
			return new IconResource( imageURL, group, iconName );
		}
		catch ( MalformedURLException exception ) {
			LOGGER.log(Level.SEVERE, null, exception);
			return null;
		}
	}
	
	
	/** 
	 * Get the icon's URL 
	 * @return the icon's URL
	 */
	public URL getURL() {
		return imageUrl;
	}
	
	
	/**
	 * Get the icon's group
	 * @return the icon's group
	 */
	public String getGroup() {
		return group;
	}
	
	
	/**
	 * Get the icon's name
	 * @return the icon's name
	 */
	public String getIconName() {
		return iconName;
	}
	
	
	/**
	 * Description of this icon
	 * @return the description of this icon
	 */
    @Override
	public String toString() {
		return "group:  "  + group + ", icon name:  " + iconName;
	}
}
