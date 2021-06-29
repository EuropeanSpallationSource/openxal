//
//  AbstractApplicationAdaptor.java
//  xal
//
//  Created by Thomas Pelaia on 3/29/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.application;

import xal.tools.ResourceManager;
import xal.tools.apputils.files.*;
import xal.extension.bricks.WindowReference;

import java.util.logging.*;
import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * AbstractApplicationAdaptor is the abstract superclass of the desktop and
 * frame based application adaptors. It contains hooks for handling application
 * events. It also provides application wide information about the application.
 *
 * @author t6p
 */
public abstract class AbstractApplicationAdaptor implements ApplicationListener {

    /**
     * wildcard file extension
     */
    public static final String WILDCARD_FILE_EXTENSION = FileFilterFactory.WILDCARD_FILE_EXTENSION;

    /**
     * name for the gui bricks resource which may or may not exist
     */
    public static final String GUI_BRICKS_RESOURCE = "gui.bricks";

    /**
     * location of the resources directory
     */
    private ApplicationResourceManager resourceManager;

    /**
     * accessory for this application's default document folder
     */
    private DefaultFolderAccessory defaultFolderAccessory = null;

    private static final Logger LOGGER = Logger.getLogger(AbstractApplicationAdaptor.class.getName());

    /**
     * Constructor
     */
    public AbstractApplicationAdaptor() {
        // resources are located using the default resource manager
        setResourcesLocation(null);
    }

    /**
     * Get the document's default folder accessory (needed at startup for some
     * applications prior to the Application instance)
     *
     * @return the default folder accessory
     */
    final DefaultFolderAccessory getDefaultFolderAccessory() {
        // lazily instantiate the accessory for the default document folder for this application
        // It is necessary for this to be lazy (not in the adaptor's constructor) to support script based applications
        // as they (e.g. JRuby) don't call the overriden methods via the super construtor within the inherited constructor.
        if (defaultFolderAccessory == null) {
            defaultFolderAccessory = new DefaultFolderAccessory(XalDocument.class, null, applicationName());
        }
        return defaultFolderAccessory;
    }

    /**
     * Get the default document folder.
     *
     * @return the default folder for documents or null if none has been set.
     */
    public final File getDefaultDocumentFolder() {
        return getDefaultFolderAccessory().getDefaultFolder();
    }

    /**
     * Get the default document folder as a URL.
     *
     * @return the default folder for documents as a URL or null if none has
     * been set.
     */
    public final URL getDefaultDocumentFolderURL() {
        return getDefaultFolderAccessory().getDefaultFolderURL();
    }

    /**
     * Launch the application with the specified document URLs.
     *
     * @param urls The document URLs to open upon launching the application.
     */
    abstract void launchApplication(final URL[] urls);

    // --------- Document management -------------------------------------------
    /**
     * The URLs to open existing document(s) in the command-line.
     */
    public static URL[] docURLs;

    /**
     * Subclasses should implement this method to return the array of file
     * suffixes identifying the files that can be read by the application.
     *
     * @return An array of file suffixes corresponding to readable files
     */
    public abstract String[] readableDocumentTypes();

    /**
     * Subclasses should implement this method to return the array of file
     * suffixes identifying the files that can be written by the application.
     *
     * @return An array of file suffixes corresponding to writable files
     */
    public abstract String[] writableDocumentTypes();

    /**
     * Determine whether this application can open documents
     */
    public final boolean canOpenDocuments() {
        final String[] documentTypes = readableDocumentTypes();
        return documentTypes != null && documentTypes.length > 0;
    }

    /**
     * Indicates whether the welcome dialog should be displayed at launch. By
     * default, this returns true if the application can open documents.
     */
    public boolean showsWelcomeDialogAtLaunch() {
        return canOpenDocuments();
    }

    /**
     * Generate a new empty document of the specified type.
     *
     * @param type the type of document to create.
     * @return an instance of the custom subclass of XalAbstractDocument
     */
    public abstract XalAbstractDocument generateEmptyDocument(final String type);

    /**
     * Generate a document from the specified URL.
     *
     * @return an instance of the custom subclass of XalAbstractDocument
     */
    abstract XalAbstractDocument generateDocument(URL url);

    // --------- Global application management ---------------------------------
    /**
     * Subclasses must implement this method to return the name of their
     * application.
     *
     * @return The name of the application
     */
    public abstract String applicationName();

    /**
     * Get the node for this application's preferences
     */
    public final Preferences getUserPreferencesNode() {
        if (this.getClass().getName().startsWith("xal.app.")) {   // standard Java based Open XAL application
            return xal.tools.apputils.Preferences.userNodeForPackage(this.getClass());
        } else {        // class is not from XAL so probably a script (e.g. jruby)
            final String scriptID = applicationName().toLowerCase().replaceAll(" ", "_").replaceAll("\\/", "-");
            return xal.tools.apputils.Preferences.userNodeForPackage(AbstractApplicationAdaptor.class).node("/xal/script/" + scriptID);
        }
    }

    /**
     * Send output to console or terminal.
     *
     * Identifies whether the application sends standard output and standard
     * error to the application's console or whether it should simply go to the
     * terminal from which the application was launched. The default is to
     * return true thus indicating that the console should be used. If
     * environment variable XAL_USE_CONSOLE is set to false, then output is sent
     * to terminal instead. Individual applications may implement a different
     * solution.
     *
     * @return Whether the application's console should capture standard output
     * and error
     */
    public boolean usesConsole() {
        String usesConsoleProperty = System.getProperty("XAL_USE_CONSOLE");
        if (usesConsoleProperty != null) {
            return Boolean.valueOf(usesConsoleProperty);
        }
        return true;
    }

    /**
     * Override this method to register custom application commands.
     *
     * @param commander The commander with which to register commands.
     * @see Commander#registerAction(Action)
     */
    public void customizeCommands(Commander commander) {
    }

    /**
     * Define some flags for launching the application, such as pre-load a
     * default accelerator. todo: this code needs to be reviewed to determine
     * its value and logic
     */
    public static void setOptions(String[] args) {
        if (args.length > 0) {

            final ArrayList<String> docPaths = new ArrayList<>();
            for (final String arg : args) {
                if (!arg.startsWith("-")) {
                    docPaths.add(arg);
                }
            }
            if (docPaths.size() > 0) {
                docURLs = new URL[docPaths.size()];
                for (int index = 0; index < docPaths.size(); index++) {
                    try {
                        docURLs[index] = new URL("file://" + docPaths.get(index));
                    } catch (MalformedURLException exception) {
                        LOGGER.log(Level.WARNING, "Error setting the documents to open passed by the user.", exception);
                    }
                }
            }
        }
    }

    /**
     * Get the document URLs.
     *
     * @return document URLs
     */
    public static URL[] getDocURLs() {
        return docURLs;
    }

    /**
     * Get the window reference from the resource if any
     */
    public WindowReference getDefaultWindowReference(final String tag, final Object... parameters) {
        final URL url = getResourceURL(GUI_BRICKS_RESOURCE);
        return new WindowReference(url, tag, parameters);
    }

    // --------- Application events --------------------------------------------
    /**
     * Event indicating that the application will open any initial documents.
     * These documents may include a new empty document if appropriate or any
     * documents passed at the command line. Subclasses may override this method
     * to handle this event if needed.
     */
    @Override
    public void applicationWillOpenInitialDocuments() {
    }

    /**
     * Subclasses may override this method to provide custom handling upon
     * completion of the application having launched.
     *
     * The default implementation prints a simple info to logger
     *
     */
    public void applicationFinishedLaunching() {
        LOGGER.log(Level.INFO, "Application{0} finished launching.", applicationName());
    }

    /**
     * Implement ApplicationListener. Subclasses may implement this method to
     * handle a document closed event at the application level. The default
     * implementation does nothing.
     */
    @Override
    public void documentClosed(final XalAbstractDocument document) {
    }

    /**
     * Implement ApplicationListener. Subclasses may implement this method to
     * handle a document created event at the application level. The default
     * implementation does nothing.
     */
    @Override
    public void documentCreated(final XalAbstractDocument document) {
    }

    /**
     * Implement ApplicationListener. Subclasses may implement this method to
     * handle an "application will quit" event at the application level. The
     * default implementation does nothing.
     */
    @Override
    public void applicationWillQuit() {
    }

    // --------- Application resources -----------------------------------------
    /**
     * Subclasses can set the location of the resources directory. This is
     * really used for script based applications that don't reside in a jar
     * file.
     *
     * @param resourcesDirectory normal file system directory specifying the
     * location of the resources directory
     */
    private void setResourcesDirectory(final File resourcesDirectory) {
        try {
            setResourcesLocation(resourcesDirectory.toURI().toURL());
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Bad URL to the application resource specified with the directory: " + resourcesDirectory, exception);
        }
    }

    /**
     * Convenience method to set the location of the resources directory by
     * specifying the parent directory of resources. The resources directory is
     * assumed to be named "resources". This is really used for script based
     * applications that don't reside in a jar file.
     *
     * @param resourcesParentDirectory normal file system directory specifying
     * the location of the parent directory of the resources
     */
    public void setResourcesParentDirectory(final File resourcesParentDirectory) {
        if (resourcesParentDirectory != null) {
            setResourcesDirectory(new File(resourcesParentDirectory, "resources"));
        } else {
            setResourcesLocation(null);
        }
    }

    /**
     * Convenience method to set the location of the resources directory by
     * specifying the parent directory of resources. The resources directory is
     * assumed to be named "resources". This is really used for script based
     * applications that don't reside in a jar file.
     *
     * @param resourcesParentDirectoryPath full file system directory path
     * specifying the location of the parent directory of the resources
     */
    public void setResourcesParentDirectoryWithPath(final String resourcesParentDirectoryPath) {
        if (resourcesParentDirectoryPath != null) {
            setResourcesParentDirectory(new File(resourcesParentDirectoryPath));
        } else {
            setResourcesLocation(null);
        }
    }

    /**
     * Subclasses can set the location of the resources directory. Setting it to
     * null will use the default resource manager.
     */
    public void setResourcesLocation(final URL resourcesLocation) {
        if (resourcesLocation != null) {
            resourceManager = new LocationApplicationResourceManager(resourcesLocation);
        } else {
            resourceManager = ApplicationResourceManager.getDefaultInstance();
        }
    }

    /**
     * Get the URL to the specified resource residing within the resources
     * directory.
     *
     * @param resourceSpec specification of the resource relative to the
     * resources URL
     * @return the full URL to the specified resource
     */
    public URL getResourceURL(final String resourceSpec) {
        return resourceManager.getResourceURL(this, resourceSpec);
    }
}

/**
 * abstract resource manager for applications
 */
abstract class ApplicationResourceManager {

    /**
     * get the named resource for the specified application
     */
    public abstract URL getResourceURL(final AbstractApplicationAdaptor adaptor, final String resourceSpec);

    /**
     * get the singleton instance
     */
    public static DefaultApplicationResourceManager getDefaultInstance() {
        return DefaultApplicationResourceManager.getInstance();
    }
}

/**
 * resource manager for applications that uses the default resource manager
 */
class DefaultApplicationResourceManager extends ApplicationResourceManager {

    /**
     * singleton resource manager
     */
    private static final DefaultApplicationResourceManager DEFAULT_RESOURCE_MANAGER;

    // static initializer
    static {
        DEFAULT_RESOURCE_MANAGER = new DefaultApplicationResourceManager();
    }

    /**
     * get the singleton instance
     */
    public static DefaultApplicationResourceManager getInstance() {
        return DEFAULT_RESOURCE_MANAGER;
    }

    /**
     * get the named resource for the specified application
     */
    @Override
    public URL getResourceURL(final AbstractApplicationAdaptor adaptor, final String resourceSpec) {
        return ResourceManager.getResourceURL(adaptor.getClass(), resourceSpec);
    }
}

/**
 * resource manager for applications that uses a specific location to search for
 * resources (suitable for script based applications)
 */
class LocationApplicationResourceManager extends ApplicationResourceManager {

    /**
     * location of the resources directory
     */
    private final URL resourcesLocation;

    /**
     * Constructor
     */
    public LocationApplicationResourceManager(final URL resourcesLocation) {
        this.resourcesLocation = resourcesLocation;
    }

    /**
     * get the named resource for the specified application
     */
    @Override
    public URL getResourceURL(final AbstractApplicationAdaptor adaptor, final String resourceSpec) {
        try {
            return new URL(resourcesLocation, resourceSpec);
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Bad URL to the application resource: " + resourceSpec, exception);
        }
    }
}
