/*
 * Main.java
 *
 * Created on Fri Oct 10 14:03:52 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package edu.stanford.lcls.modelmanager;

import xal.extension.application.Application;
import xal.extension.application.ApplicationAdaptor;
import xal.extension.application.XalDocument;
import xal.extension.application.smf.AcceleratorApplication;
import xal.smf.data.XMLDataManager;

/**
 * Main is the ApplicationAdaptor for the Template application.
 *
 * @author  somebody
 */
public class ModelManagerMain extends ApplicationAdaptor {
	private static ModelManagerDocument modelManagerDocument;
    // --------- Document management -------------------------------------------
    
    /**
     * Returns no suffices, so no open/new document dialog appears
     * @return String[0]
     */
    @Override
	public String[] readableDocumentTypes() {
        return new String[0];
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    @Override
    public String[] writableDocumentTypes() {
        return new String[0];
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    @Override
    public XalDocument newEmptyDocument() {
        modelManagerDocument = new ModelManagerDocument();
        // Following line prevents application from asking to pick default accelerator
		modelManagerDocument.applySelectedAcceleratorWithDefaultPath( XMLDataManager.defaultPath() );
        return modelManagerDocument;
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    @Override
    public XalDocument newDocument(java.net.URL url) {
        modelManagerDocument = new ModelManagerDocument(url);
        return modelManagerDocument;
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "ModelManager";
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
    public ModelManagerMain() {
    }
    
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting application...");
            AcceleratorApplication.launch(new ModelManagerMain());
            modelManagerDocument.connectDefault();
            // Message.info("Application has finished launching!");
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
			System.exit(-1);
        }
    }
}

