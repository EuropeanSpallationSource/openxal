/*
 * Main.java
 *
 * Created on Fri Oct 10 14:03:52 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.acceleratorapplicationtemplate;

import java.util.logging.Level;
import java.util.logging.Logger;

import xal.extension.application.*;
import xal.extension.application.smf.AcceleratorApplication;

/**
 * Main is the ApplicationAdaptor for the Template application.
 *
 * @author  somebody
 */




public class Main extends ApplicationAdaptor {
    // --------- Document management -------------------------------------------

    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] {"txt", "dat"};
    }


    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"txt", "dat"};
    }


    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new TemplateDocument();
    }


    /**
     * Implement this method to return an instance of my custom document
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new TemplateDocument(url);
    }


    // --------- Global application management ---------------------------------


    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Template";
    }


    /**
     * Specifies whether I want to send standard output and error to the console.
     * I don't need to override the superclass adaptor to return true (the default), but
     * it is sometimes convenient to disable the console while debugging.
     * @return Name of my application.
     */
    public boolean usesConsole() {
        String usesConsoleProperty = System.getProperty("usesConsole");
        if ( usesConsoleProperty != null ) {
            return Boolean.valueOf(usesConsoleProperty).booleanValue();
        }
        else {
            return true;
        }
    }


    // --------- Application events --------------------------------------------

    /**
     * Capture the application launched event and print it.  This is an optional
     * hook that can be used to do something useful at the end of the application launch.
     */
    public void applicationFinishedLaunching() {
        System.out.println("Application has finished launching!");
    }


    /**
     * Constructor
     */
    public Main() {
    }


    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting application...");
            Logger.getLogger("global").log( Level.INFO, "Launching the application..." );
            AcceleratorApplication.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            Logger.getLogger("global").log( Level.SEVERE, "Error launching the application." , exception );
            exception.printStackTrace();
            Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
            System.exit(-1);
        }
    }
}

