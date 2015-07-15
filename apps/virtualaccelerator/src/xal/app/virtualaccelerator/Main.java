/*
 * @(#)Main.java          0.9 02/26/2004
 *
 * Copyright (c) 2001-2004 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */
package xal.app.virtualaccelerator;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import xal.extension.application.Application;
import xal.extension.application.ApplicationAdaptor;
import xal.extension.application.XalDocument;
import xal.extension.application.smf.AcceleratorApplication;

/**
 * This is the main class and Application adapter for Virtual accelerator. It provides entry point for the program and
 * information along with some callback for other parts of application.
 * 
 * @version 0.2 13 Jul 2015
 * @author Paul Chu
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class Main extends ApplicationAdaptor {

    private URL url;
    /** Variable indicating whether or not application should default to new empty document. False by default. */
    private static boolean openNewDocument = false;
    /**
     * Variable indicating wheather or not application should start virtual accelerator after loading is done.False by
     * default.
     */
    private static boolean runOnFinishedLaunching = false;
    
    //-------------Constructors-------------
    public Main() {
        url = null;
    }

    public Main(String str) {

	    try{
            url = new URL(str);
	    }
	    catch (MalformedURLException exception) {
            System.err.println(exception);
	    }
    }

    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting application...");
            // set command-line option(s) for opening existing document(s)
            setOptions( args );
            AcceleratorApplication.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
            JOptionPane.showMessageDialog( null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
        }
    }
    
    /**
     * Loads command line options.
     * 
     * @param args
     *            arguments given by main method.
     */
    public static void setOptions(String[] args) {

        final java.util.ArrayList<String> docPaths = new java.util.ArrayList<String>();
        Properties props = System.getProperties();
        boolean chooseSequence = false;
        for (final String arg : args) {
            if (!arg.startsWith("-")) {
                if(chooseSequence){
                    props.setProperty("useSequence", arg);
                }else{
                    docPaths.add(arg);// We add any filepaths.    
                }
            } else {
                switch (arg) {
                case "-d":
                case "--default":
                    openNewDocument = true;
                    if (props.getProperty("useDefaultAccelerator") == null){
                        props.setProperty("useDefaultAccelerator", "true");    
                    }
                    if (props.getProperty("useSequence") == null){
                        props.setProperty("useSequence", "MEBT");// First sequence in default accelerator.
                    }
                    runOnFinishedLaunching = true;
                    break;
                case "-s":
                case "--sequence":
                    chooseSequence=true;
                    break;
                case "-h":
                case "--help":
                    printHelp();
                    System.exit(0);
                }

            }
        }
        if (docPaths.size() > 0) {
            docURLs = new URL[docPaths.size()];
            for (int index = 0; index < docPaths.size(); index++) {
                try {
                    docURLs[index] = new URL("file://" + docPaths.get(index));
                } catch (MalformedURLException exception) {
                    Logger.getLogger("global").log(Level.WARNING,
                            "Error setting the documents to open passed by the user.", exception);
                    System.err.println(exception);
                }
            }
        }
    }
    
    
    /** Convenient method for printing command line options help for user. */
    private static void printHelp() {
        System.out.println("Usage:virtualaccelerator [options] [files]\r\n"
                         + "        Runs Virtual Accelerator.\r\n"
                         + "  options:\r\n"
                         + "        -d,--default                    chooses default accelerator and sequence then runs it after loading is complete.\r\n"
                         + "        -s,--sequence <sequenceName>    sets sequence to use.\r\n"
                         + "        -h,--help                       print this help.\r\n"
                         + "  files:\r\n"
                         + "    path(s) to virtual accelerator[.ve] file we want to open.\r\n");
    }


    /**
     * Callback method to start virtual accelerator if {@link #runOnFinishedLaunching} is true.
     */
    @Override
    public void applicationFinishedLaunching() {
        if (runOnFinishedLaunching) {
            for(VADocument document:Application.getApp().<VADocument>getDocumentsCopy()){
                document.commander.getAction("run-va").actionPerformed(new ActionEvent(this, 0, "Run"));    
            }      
        }

    }
    
    /**
     * Callback method to destroy all servers when application is exiting.
     */
    public void applicationWillQuit() {
		try {
			final List<VADocument> documents = Application.getApp().<VADocument>getDocumentsCopy();
            for ( final VADocument document : documents ) {
				try {
					document.destroyServer();
				}
				catch( Exception exception ) {
					System.err.println( exception.getMessage() );					
				}
			}
		} 
		catch ( Exception exception ) {
			System.err.println( exception.getMessage() ); 
		}
    }
	
    public String applicationName() {
        return "Virtual Accelerator";
    }
    
    public XalDocument newDocument(java.net.URL url) {
        return new VADocument(url);
    }
    
    public XalDocument newEmptyDocument() {
        return new VADocument();
    }
    
    public String[] writableDocumentTypes() {
        return new String[] {"va"};
    }
    
    public String[] readableDocumentTypes() {
        return new String[] {"va", "xml"};
    }

    
    public boolean usesConsole() {
		return true;
    }
    
    public boolean showsWelcomeDialogAtLaunch() {
        // We don't want dialog if we already know what document are we going to open/create.
        if (openNewDocument) {
            return false;
        }
        return super.showsWelcomeDialogAtLaunch();
    }
    
}
