/*
 * AboutBox.java
 *
 * Created on April 1, 2003, 1:12 PM
 */

package xal.extension.application;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import java.awt.Component;


/**
 * An About Box window that displays information about the application.  There is only 
 * one about box for the entire application.  The about box displays the
 * application name, version, description, authors, organization and date.
 *
 * @author  tap
 */
class AboutBox {
	/** the about box instance */
    private static final AboutBox aboutBox;

	/** resource for the About box information */
	public static final String ABOUT_INFO_RESOURCE = "About.properties";
	
	/** information to display */
	protected final String message;
	
	/** dialog title */
	protected final String title;
    
	
    static {
		aboutBox = new AboutBox();
    }
    
    
    /** Creates a new instance of AboutBox */
    public AboutBox() {
        title = "About " + Application.getAdaptor().applicationName();
        message = generateMessage();
    }
    
    
    /**
     * The about box is enabled if and only if the aboutBox window exists.
     * @return true if the about box exists and false otherwise
     */
    static boolean isAvailable() {
        return aboutBox != null;
    }
    
    
    /**
     * Generate the HTML message that appears in the about box.
	 * @return the generated message
     */
    private String generateMessage() {
        final Map<String,String> appInfo = loadApplicationInfo();
        
        StringBuilder message = new StringBuilder( "<html>" );
        message.append( "<head>" );
        message.append( "<style type=\"text/css\">" );
        message.append( "P.title {text-align: center; font-size: large;}" );
        message.append( "P.version {text-align: center; font-size: medium;}" );
        message.append( "P.description {text-align: left; font-size:medium;}" );
        message.append( "li.author {font-size:medium;}" );
        message.append( "td.footer {text-align: center; font-size:small;}" );
        message.append( "Body.normal {background-color: silver}" );
        message.append( "</style>" );
        message.append( "</head>" );
        message.append( "<body class=normal>" );
        
        String appName = getValue( "name", appInfo );
        message.append("<P class=title>").append(appName).append("</P>");
        
        String version = getValue( "version", appInfo );
        message.append("<P class=version> <b>Version:</b> ").append(version).append("</P>");
        
        String description = getValue( "description", appInfo );
        message.append("<dl><dt><b>Description:</b></dt><dd>").append(description).append("</dd></dl>");
        
        String authorList = getValue( "authors", appInfo );
        String[] authors = Util.getTokens( authorList, "," );
        message.append( "<p> <b>Authors:</b>" );
        for ( int index = 0 ; index < authors.length ; index++ ) {
            String author = authors[index];
            message.append("<li class=author>").append(author).append("</li>");
        }
        message.append( "</p>");
        
        message.append( "<center> <table width=80%>" );
        message.append( "<tr><td class=footer> <hr> </td></tr>" );
        
        String organization = getValue( "organization", appInfo );
        message.append("<tr><td class=footer>").append(organization).append("</td></tr>");
        
        String date = getValue( "date", appInfo );
        message.append("<tr><td class=footer>").append(date).append("</td></tr>");
        
        message.append( "</table> </P>" );
        
        message.append( "</body>" );
        message.append( "</html>" );
        
        return message.toString();
    }
    
    
    /**
     * Get the information associated with the specified key from the application
     * information map.
     * @param key The key corresponding to the piece of information requested
     * @param appInfo The map containing keyed application information
     * @return The information
     */
    private String getValue( final String key, final Map<String,String> appInfo ) {
        String value = appInfo.get( key );
        if ( value == null ) {
            value = "Unspecified";
        }
        
        return value;
    }
    
    
    /**
     * Load the application information from the properties file specified by 
     * the application adaptor.  The resource bundle information is stored in 
     * a Map for convenient access.
     * @return The map containing the application information
     */
    private Map<String,String> loadApplicationInfo() {
        Map<String,String> infoMap;
        
        try {
            infoMap = Util.getPropertiesForResource( ABOUT_INFO_RESOURCE );
        }
        catch( MissingResourceException exception ) {
			final String message = "No application \"About Box\" information resource found in the application's resources directory: " + ABOUT_INFO_RESOURCE;
			Logger.getLogger("global").log( Level.WARNING, message, exception );
            System.err.println( message );
            
            // substitute with default information
            infoMap = new HashMap<>();
            infoMap.put( "name", Application.getAdaptor().getClass().getName() );
        }
        
        return infoMap;
    }
    
    
    /**
	 * Show the about box near the specified component.
     * @param component The component near which the about box should be displayed
     */
    protected void displayNear( final Component component ) {
		JOptionPane.showMessageDialog(component, message, title, JOptionPane.INFORMATION_MESSAGE );
    }
    
    
    /**
	 * Show an internal dialog about box near the specified component.
     * @param component The component near which the about box should be displayed
     */
    protected void displayInternalNear( final Component component ) {
		JOptionPane.showInternalMessageDialog(component, message, title, JOptionPane.INFORMATION_MESSAGE );
    }
    
    
    /**
     * Show the about box near the specified component.
     * @param component The component near which the about box should be displayed
     */
    public static void showNear( final Component component ) {
		aboutBox.displayNear( component );
    }
    
    
    /**
	 * Show an internal about box near the specified component.
     * @param component The component near which the about box should be displayed
     */
    public static void showInternalNear( final Component component ) {
		aboutBox.displayInternalNear( component );
    }
}
