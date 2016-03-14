/*
 * Main.java
 *
 * Created on March 19, 2003, 1:28 PM
 */




package xal.app.acceleratordemo;

import java.util.logging.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * Demo is a demo concrete subclass of ApplicationAdaptor.  This demo application
 * is a simple accelerator viewer that demonstrates how to build a simple
 * accelerator based application using the accelerator application framework.
 *
 * @author  t6p
 */
public class Demo extends ApplicationAdaptor {
    // --------- Document management -------------------------------------------

    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] {"txt", "text"};
    }


    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"txt", "text"};
    }


    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new DemoDocument();
    }


    /**
     * Implement this method to return an instance of my custom document
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new DemoDocument(url);
    }


    // --------- Global application management ---------------------------------


    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "DemoAcceleratorApplicaton";
    }


    // --------- Application events --------------------------------------------


    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Launching application...");
            Logger.getLogger("global").log( Level.INFO, "Launching the application..." );
            AcceleratorApplication.launch( new Demo() );
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
