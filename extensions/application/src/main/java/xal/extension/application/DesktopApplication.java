//
//  DesktopApplication.java
//  xal
//
//  Created by Thomas Pelaia on 3/29/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.application;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import xal.Info;

/**
 * Application subclass for JDesktopPane based applications.
 */
public class DesktopApplication extends Application implements XalInternalDocumentListener {

    /**
     * desktop pane that contains the document windows.
     */
    private JFrame desktopFrame;

    /**
     * default desktop menubar
     */
    private JMenuBar defaultDesktopMenu;

    /**
     * Constructor
     *
     * @param adaptor The application adaptor used for customization.
     */
    protected DesktopApplication(final DesktopApplicationAdaptor adaptor) {
        this(adaptor, new URL[]{});
    }

    /**
     * Constructor
     *
     * @param adaptor The application adaptor used for customization.
     * @param urls An array of document URLs to open upon startup.
     */
    protected DesktopApplication(final DesktopApplicationAdaptor adaptor, final URL[] urls) {
        super(adaptor, urls);
    }

    /**
     * Initialize the Application and open the documents specified by the URL
     * array. If the URL array is empty, then create one empty document.
     *
     * @param urls An array of document URLs to open.
     */
    @Override
    protected void setup(final URL[] urls) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                createDesktopFrame();

                registerEvents();

                setupConsole();

                // Make the open/save file choosers as early as possible since JFileChooser has a known race condition bug.
                makeFileChoosers();

                // setup the application commander and load custom application commands
                commander = makeCommander();
                applicationAdaptor.customizeCommands(commander);
                setupMenuBar(commander);

                // notify the adaptor that the desktop frame will be displayed
                ((DesktopApplicationAdaptor) applicationAdaptor).applicationWillDisplayDesktopPane();

                desktopFrame.setVisible(true);
                desktopFrame.toFront();

                // notify listeners that the initial documents, if any, will be opened
                noticeProxy.applicationWillOpenInitialDocuments();

                if (urls.length > 0) {
                    for (int index = 0; index < urls.length; index++) {
                        openDocument(urls[index]);
                    }
                }

                // if multiple documents are opened then cascade them
                if (openDocuments.size() > 1) {
                    cascadeWindowsAbout(openDocuments.get(0));
                }
            });
        } catch (InterruptedException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }

        // comment out application service registration until it is developed -tap
        applicationAdaptor.applicationFinishedLaunching();
    }

    /**
     * Make an application commander
     *
     * @return the commander that loads default and custom actions.
     */
    @Override
    protected Commander makeCommander() {
        return new Commander(this);
    }

    /**
     * Setup the menubar
     */
    private void setupMenuBar(final Commander commander) {
        defaultDesktopMenu = commander.getMenubar();
        desktopFrame.setJMenuBar(defaultDesktopMenu);
    }

    /**
     * Create the top level desktop frame
     */
    private void createDesktopFrame() {
        final JDesktopPane desktop = new JDesktopPane();
        desktop.setDragMode(((DesktopApplicationAdaptor) applicationAdaptor).drawsDocumentContentOnDrag() ? JDesktopPane.LIVE_DRAG_MODE : JDesktopPane.OUTLINE_DRAG_MODE);

        desktopFrame = new JFrame(Info.getLabel() + " - " + applicationAdaptor.applicationName());
        desktopFrame.setSize(1024, 768);
        desktopFrame.setContentPane(desktop);

        desktopFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        desktopFrame.addWindowListener(newDesktopWindowHandler());
    }

    /**
     * Get the desktop pane
     */
    private JDesktopPane getDesktopPane() {
        return (JDesktopPane) desktopFrame.getContentPane();
    }

    /**
     * Get the selected internal window.
     *
     * @return the selected internal window.
     */
    XalInternalWindow getSelectedWindow() {
        final JInternalFrame selectedFrame = getDesktopPane().getSelectedFrame();
        return (selectedFrame instanceof XalInternalWindow) ? (XalInternalWindow) selectedFrame : null;
    }

    /**
     * Get the selected internal window.
     *
     * @return the selected internal window.
     */
    XalInternalDocument getSelectedDocument() {
        final XalInternalWindow selectedWindow = getSelectedWindow();
        return (selectedWindow != null) ? selectedWindow.getInternalDocument() : null;
    }

    /**
     * Create a new window listener.
     */
    private WindowListener newDesktopWindowHandler() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                quit();
            }
        };
    }

    /**
     * Add a new document to this application and if makeVisible is true, show
     * it
     *
     * @param document the document to produce
     * @param makeVisible make the document visible
     */
    @Override
    public void produceDocument(final XalAbstractDocument document, final boolean makeVisible) {
        openDocuments.add(document);
        ((XalInternalDocument) document).addXalInternalDocumentListener(this);
        document.initMainWindow();
        getDesktopPane().add((XalInternalWindow) document.getDocumentView());
        if (makeVisible) {
            document.showDocument();
        }
        noticeProxy.documentCreated(document);
    }

    /**
     * Create and open a new empty document.
     */
    @Override
    protected void newDocument() {
        newDocument("");
    }

    /**
     * Create and open a new empty document of the specified type.
     *
     * @param type the type of document to create.
     */
    @Override
    protected void newDocument(final String type) {
        final XalInternalDocument document = (XalInternalDocument) applicationAdaptor.generateEmptyDocument(type);
        produceDocument(document);
    }

    /**
     * Handle the launching of the application by creating the application
     * instance and performing application initialization.
     *
     * @param adaptor The custom application adaptor.
     * @param urls The URLs of documents to open upon launching the application
     */
    public static void launch(final DesktopApplicationAdaptor adaptor, final URL[] urls) {
        new DesktopApplication(adaptor, urls);
    }

    /**
     * Handle document title change event. Empty implementation.
     */
    @Override
    public void titleChanged(final XalInternalDocument document, final String newTitle) {
        //Do nothing
    }

    /**
     * Handle document change event. Empty implementation.
     */
    @Override
    public void hasChangesChanged(final XalInternalDocument document, final boolean newHasChangesStatus) {
        //Do nothing
    }

    /**
     * Handle document closing event. Empty implementation.
     */
    @Override
    public void documentWillClose(final XalInternalDocument document) {
        //Do nothing
    }

    /**
     * When a document has closed, the application receives this event and
     * removes the document from its open documents list. If there are no
     * documents remaining, the application quits.
     *
     * @param document The document that has closed.
     */
    @Override
    public void documentHasClosed(final XalInternalDocument document) {
        document.removeXalInternalDocumentListener(this);
        openDocuments.remove(document);
        noticeProxy.documentClosed(document);
    }

    /**
     * Handle the document activated event.
     *
     * @param document the document that has been activated.
     */
    @Override
    public void documentActivated(final XalInternalDocument document) {
        desktopFrame.setJMenuBar(document.getDesktopMenubar());
        desktopFrame.validate();
    }

    /**
     * Handle the document activated event.
     *
     * @param document the document that has been activated.
     */
    @Override
    public void documentDeactivated(final XalInternalDocument document) {
        desktopFrame.setJMenuBar(defaultDesktopMenu);
        desktopFrame.validate();
    }
}
