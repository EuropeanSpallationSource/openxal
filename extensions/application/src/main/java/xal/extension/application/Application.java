/*
 * Application.java
 *
 * Created on March 17, 2003, 3:48 PM
 */
package xal.extension.application;

import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RepaintManager;

import xal.extension.application.platform.MacAdaptor;
import xal.extension.application.rbac.AuthenticationPane;
import xal.extension.application.rbac.RBACPlugin;
import xal.extension.service.ServiceDirectory;
import xal.extension.service.ServiceException;
import xal.rbac.AccessDeniedException;
import xal.rbac.Credentials;
import xal.rbac.RBACException;
import xal.rbac.RBACLogin;
import xal.rbac.RBACSubject;
import xal.tools.URLReference;
import xal.tools.apputils.ApplicationSupport;
import xal.tools.apputils.files.FileFilterFactory;
import xal.tools.apputils.files.RecentFileTracker;
import xal.tools.messaging.MessageCenter;

/**
 * The Application class handles defines the core of an application. It is often
 * the first handler of application wide events and typically forwards those
 * events to the custom application adaptor for further processing. Every
 * application has exactly one instance of this class. The static method
 * <code>getApp()</code> provides access to that instance. Every application has
 * one custom application adaptor. The adaptor acts as a delegate for handling
 * events specific to the custom application. The Application, however, handles
 * events common to all multi-document applications.
 *
 * @author t6p
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public abstract class Application {
    // public static constants for confirmation dialogs

    public static final int YES_OPTION = JOptionPane.YES_OPTION;
    public static final int NO_OPTION = JOptionPane.NO_OPTION;

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    // private constants
    private final Date LAUNCH_TIME;

    // static variables
    private static Application application;

    // instance variables
    /**
     * custom application adaptor
     */
    protected AbstractApplicationAdaptor applicationAdaptor;
    /**
     * list of open documents
     */
    protected List<XalAbstractDocument> openDocuments;
    protected Commander commander;

    /**
     * file chooser for open
     */
    private JFileChooser openFileChooser;
    /**
     * file chooser for save
     */
    private JFileChooser saveFileChooser;
    /**
     * cache and retrieve recently accessed files
     */
    private RecentFileTracker recentFileTracker;

    // messaging instance variables
    /**
     * local message center
     */
    private MessageCenter messageCenter;
    /**
     * proxy for broadcasting ApplicationListener events
     */
    protected ApplicationListener noticeProxy;

    /**
     * location of the next document to open
     */
    private Point nextDocumentOpenLocation;

    /**
     * template folder
     */
    private File templateFolder;

    /**
     * default folder
     */
    private File defaultDocumentFolder;


    /* RBAC service */
    private RBACLogin rbacLogin;
    private RBACSubject rbacSubject;
    private boolean useRBACLogin;

    /**
     * static initializer
     */
    static {
        LoggerBuffer.setupRootLogger();

        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("mac os x")) {
            MacAdaptor.initialize();
        }

        loadUserProperties();
        setupDoubleBufferingMode();
    }

    /**
     * Application constructor.
     *
     * @param adaptor The application adaptor used for customization.
     */
    protected Application(final AbstractApplicationAdaptor adaptor) {
        this(adaptor, new URL[]{});
    }

    /**
     * Application constructor.
     *
     * @param adaptor The application adaptor used for customization.
     * @param urls An array of document URLs to open upon startup.
     */
    protected Application(final AbstractApplicationAdaptor adaptor, final URL[] urls) {
        nextDocumentOpenLocation = new Point(0, 0);

        LAUNCH_TIME = new Date();

        applicationAdaptor = adaptor;
        openDocuments = new LinkedList<>();

        // assign the global application instance before the setup since it is referenced there (among other places).
        Application.application = this;

        while (true) {
            if (authenticateWithRBAC()) {
                if (authorizeWithRBAC("Run")) {
                    break;
                } else {
                    final int option = JOptionPane.showConfirmDialog(getActiveWindow(),
                            "No authorisation. Would you like to login with another user?",
                            "No authorisation", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        try {
                            rbacSubject.logout();
                        } catch (RBACException e) {
                            JOptionPane.showMessageDialog(getActiveWindow(), e.getMessage(), "Error while trying to logout", JOptionPane.ERROR_MESSAGE);
                        }
                        rbacSubject = null;
                    } else {
                        quit();
                    }
                }
            }
        }

        setup(urls);
    }

    /**
     * Convention method for authenticating user.
     *
     * <p>
     * If RBACPlugin couldn't be loaded the application will not use RBAC !!!
     *
     * @return true if authentication successful, false if not.
     */
    private boolean authenticateWithRBAC() {
        //RBAC authentication
        try {
            rbacLogin = RBACLogin.newRBACLogin();
        } catch (RuntimeException e) {
            System.err.println("RBAC plugin not found. Continuing without RBAC.");
            return true;
        }

        if (rbacSubject == null) {
            try {
                // Try to use the local token.
                rbacSubject = rbacLogin.authenticate(null, null);
                LOGGER.log(Level.INFO, "Already logged in.");
                if (rbacSubject != null) {
                    return true;
                }
            } catch (AccessDeniedException | RBACException e) {
                // Fall to authentication pane
            }
        }

        try {
            LOGGER.log(Level.INFO, "Starting authentication.");

            final Credentials credentials = AuthenticationPane.getCredentials();
            if (credentials == null) {
                // User pressed cancel
                LOGGER.log(Level.INFO, "Exiting...");
                quit();
                return false;
            }
            rbacSubject = rbacLogin.authenticate(credentials.getUsername(), credentials.getPassword(), credentials.getPreferredRole(), credentials.getIP());
            System.out.printf("Authentication successful with username %s.\n", credentials.getUsername());
            return (rbacSubject != null);
        } catch (AccessDeniedException e) {
            System.err.printf("Access denied during authentication: %s\n", e.getMessage());
            JOptionPane.showMessageDialog(getActiveWindow(), e.getMessage(), "Access denied", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (RBACException e) {
            System.err.printf("Error while trying to authenticate: %s\n", e.getMessage());
            JOptionPane.showMessageDialog(getActiveWindow(), e.getMessage(), "Error while trying to authenticate", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Authenticates and authorizes user. If successful returns true.
     *
     * <p>
     * Asks for given permission on resource : "Xal" + application name without
     * spaces. Eg. for Virtual Accelerator this is XalVirtualAccelerator.
     * </p>
     *
     * @param permission for which to authenticate.
     *
     * @return true if authorization was successful, else false.
     */
    public boolean authorizeWithRBAC(final String permission) {
        //RBAC authorization
        if (rbacSubject != null) {
            try {
                String appName = getAdaptor().applicationName().replace(" ", "");
                String resource = "Xal" + appName.substring(0, 1).toUpperCase() + appName.substring(1);
                System.out.printf("Starting authorization for resource %s, permission %s.\n", resource, permission);
                if (rbacSubject.hasPermission(resource, permission)) {
                    LOGGER.log(Level.INFO, "Authorization successful. Proceeding...");
                    return true;
                } else {
                    System.err.printf("No authorisation for resource %s, permission %s.\n", resource, permission);
                    return false;
                }
            } catch (RBACException e) {
                System.err.printf("Error while trying to authorize: %s.\n", e.getMessage());
                JOptionPane.showMessageDialog(getActiveWindow(), e.getMessage(), "Error while trying to authorize", JOptionPane.ERROR_MESSAGE);
                return false;
            } catch (AccessDeniedException e) {
                System.err.printf("Access denied during authorisation: %s\n", e.getMessage());
                JOptionPane.showMessageDialog(getActiveWindow(), e.getMessage(), "Access denied", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            // no RBAC module
            return true;
        }
    }

    public RBACLogin getRbacLogin() {
        return rbacLogin;
    }

    public RBACSubject getRbacSubject() {
        return rbacSubject;
    }

    /**
     * Load the user's custom properties and set them as the defaults, but do
     * not override existing properties.
     */
    private static void loadUserProperties() {
        final Preferences prefs = xal.tools.apputils.Preferences.nodeForPackage(Application.class);
        final String propertiesPath = prefs.get("UserPropertiesFile", "");

        if (propertiesPath == null || propertiesPath.isEmpty()) {
            return;
        }

        try {
            final FileInputStream propertiesStream = new FileInputStream(propertiesPath);
            final Properties defaultProperties = System.getProperties();
            // must create properties from the default properties to keep Java Web Start happy
            final Properties userProperties = new Properties(defaultProperties);
            userProperties.clear();

            userProperties.load(propertiesStream);
            propertiesStream.close();

            // don't override existing system properties since they may have been passed at the command line
            final Set<String> propertyNames = userProperties.stringPropertyNames();
            for (final String name : propertyNames) {
                if (System.getProperty(name) == null) {
                    System.setProperty(name, userProperties.getProperty(name));
                }
            }
            System.setProperties(userProperties);
            LOGGER.log(Level.INFO, "Applied user properties from file: {0}", propertiesPath);
        } catch (FileNotFoundException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            LOGGER.log(Level.WARNING, "Failed to load user properties from file: " + propertiesPath, exception);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            LOGGER.log(Level.WARNING, "Failed to load user properties from file: " + propertiesPath, exception);
        } catch (SecurityException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            LOGGER.log(Level.WARNING, "Failed to load user properties from file: " + propertiesPath, exception);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            LOGGER.log(Level.WARNING, "Failed to load user properties from file: " + propertiesPath, exception);
        }
    }

    /**
     * Check to see if the user has indicated that double buffering should be
     * disabled by having set the "DisableDoubleBuffering" property to true.
     * This may be useful for remote X display. If the the property is true then
     * disable double buffering.
     */
    private static void setupDoubleBufferingMode() {
        final boolean disableDoubleBuffering = Boolean.getBoolean("DisableDoubleBuffering");
        if (disableDoubleBuffering) {
            RepaintManager.currentManager(null).setDoubleBufferingEnabled(false);
            LOGGER.log(Level.CONFIG, "Double buffering disabled...");
        }
    }

    /**
     * Get the launch time which is the time at which the Application instance
     * was instantiated.
     *
     * @return The launch time
     */
    public Date getLaunchTime() {
        return (Date) LAUNCH_TIME.clone();
    }

    /**
     * Get the application commander that manages commands for the entire
     * application.
     *
     * @return the application commander
     */
    public Commander getCommander() {
        return commander;
    }

    /**
     * Determine whether this application can open documents
     */
    protected boolean canOpenDocuments() {
        return applicationAdaptor.canOpenDocuments();
    }

    /**
     * Indicates whether the welcome dialog should be displayed at launch
     */
    protected boolean showsWelcomeDialogAtLaunch() {
        return applicationAdaptor.showsWelcomeDialogAtLaunch();
    }

    /**
     * show the welcome dialog which offers to open a new document, template or
     * existing document.
     */
    protected void showWelcomeDialog() {
        // todo: should get the position from a Java property (e.g. passed by the launcher)
        new WelcomeController(getNextDocumentOpenLocation());
    }

    // --------- Application Initializers --------------------------------------
    /**
     * Initialize the Application and open the documents specified by the URL
     * array. If the URL array is empty, then create one empty document.
     *
     * @param urls An array of document URLs to open.
     */
    protected abstract void setup(final URL[] urls);

    /**
     * Make an application commander
     *
     * @return the commander that loads default and custom actions.
     */
    protected Commander makeCommander() {
        return new Commander(this);
    }

    /**
     * Register the application status service so clients on the network can
     * query the status of this application instance.
     */
    protected final void registerApplicationStatusService() {
        // check to see if the startup flag has disabled application services
        Boolean shouldRegister = Boolean.valueOf(System.getProperty("registerApplicationService", "true"));

        if (shouldRegister) {
            try {
                ServiceDirectory.defaultDirectory().registerService(ApplicationStatus.class, applicationAdaptor.applicationName(), new ApplicationStatusService());
                LOGGER.log(Level.INFO, "Registered application services...");
            } catch (ServiceException exception) {
                LOGGER.log(Level.SEVERE, "Service registration...", exception);
            }
        } else {
            LOGGER.log(Level.CONFIG, "Application services disabled.");
            LOGGER.log(Level.INFO, "Application services not registerd because of startup flag...");
        }
    }

    /**
     * Setup the console to capture standard output and standard error
     */
    protected void setupConsole() {
        if (applicationAdaptor.usesConsole()) {
            Console.captureOutput();
            Console.captureErr();
        }
    }

    /**
     * Get the file chooser with which the user interacts when saving a
     * document.
     *
     * @return The file chooser with which the user interacts when saving a
     * document.
     */
    public JFileChooser getSaveFileChooser() {
        return saveFileChooser;
    }

    /**
     * Set the file chooser with which the user will interact when saving a
     * document.
     *
     * @param fileChooser The file chooser with which the user will interact
     * when saving a document.
     */
    public void setSaveFileChooser(final JFileChooser fileChooser) {
        saveFileChooser = fileChooser;
        applicationAdaptor.getDefaultFolderAccessory().applyTo(saveFileChooser);
    }

    /**
     * Get the file chooser with which the user interacts when opening a
     * document.
     *
     * @return The file chooser with which the user interacts when opening a
     * document.
     */
    public JFileChooser getOpenFileChooser() {
        return openFileChooser;
    }

    /**
     * Set the file chooser with which the user will interact when opening a
     * document.
     *
     * @param fileChooser The file chooser with which the user will interact
     * when opening a document.
     */
    public void setOpenFileChooser(final JFileChooser fileChooser) {
        openFileChooser = fileChooser;
        applicationAdaptor.getDefaultFolderAccessory().applyTo(openFileChooser);
    }

    /**
     * Create a file chooser for opening and saving documents.
     */
    protected void makeFileChoosers() {
        recentFileTracker = new RecentFileTracker(10, getAdaptor().getUserPreferencesNode(), "recent_files");

        setOpenFileChooser(new JFileChooser());
        FileFilterFactory.applyFileFilters(openFileChooser, applicationAdaptor.readableDocumentTypes());
        openFileChooser.setMultiSelectionEnabled(true);

        setSaveFileChooser(new JFileChooser());
        FileFilterFactory.applyFileFilters(saveFileChooser, applicationAdaptor.writableDocumentTypes());
        saveFileChooser.setMultiSelectionEnabled(false);
    }

    /**
     * get the location of the next document to open
     */
    protected Point getNextDocumentOpenLocation() {
        return nextDocumentOpenLocation;
    }

    /**
     * Set the next document open location
     */
    protected void setNextDocumentOpenLocation(final Point location) {
        nextDocumentOpenLocation = location;
    }

    /**
     * Set the location of the next document to open based on the location of
     * the active application document window
     */
    protected void updateNextDocumentOpenLocation() {
        final Window activeWindow = getActiveWindow();
        final XalWindow selectedWindow = activeWindow instanceof XalWindow ? (XalWindow) activeWindow : null;
        // offset this window relative to the active window if any
        if (selectedWindow != null && selectedWindow.isVisible()) {
            final java.awt.Container contentPane = selectedWindow.getContentPane();
            final int offset = (int) (1.5 * (contentPane.getLocationOnScreen().y - selectedWindow.getLocationOnScreen().y));
            final Point location = new Point(selectedWindow.getLocationOnScreen());
            location.translate(offset, offset);
            setNextDocumentOpenLocation(location);
        }
    }

    /**
     * update the next location offset from document
     */
    private void updateNextDocumentOpenLocationOffsetFrom(final XalAbstractDocument document) {
        if (document instanceof XalDocument) {
            final XalWindow window = ((XalDocument) document).getMainWindow();
            if (window != null) {
                updateNextDocumentOpenLocationOffsetFrom(window);
            }
        }
    }

    /**
     * update the next location offset from window
     */
    private void updateNextDocumentOpenLocationOffsetFrom(final XalWindow window) {
        final java.awt.Container contentPane = window.getContentPane();
        final int offset = (int) (1.5 * (contentPane.getLocationOnScreen().y - window.getLocationOnScreen().y));
        final Point location = new Point(window.getLocationOnScreen());
        location.translate(offset, offset);
        setNextDocumentOpenLocation(location);
    }

    // --------- Event registration --------------------------------------------
    /**
     * Register the instance as a provider for ApplictionListener events.
     * Register the application adaptor as an ApplicationListener.
     */
    protected void registerEvents() {
        messageCenter = new MessageCenter();
        noticeProxy = messageCenter.registerSource(this, ApplicationListener.class);

        addApplicationListener(applicationAdaptor);
    }

    /**
     * Add the listener as a listener of Application events.
     *
     * @param listener Object to register as a listener of application events.
     */
    public void addApplicationListener(final ApplicationListener listener) {
        messageCenter.registerTarget(listener, this, ApplicationListener.class);
    }

    /**
     * Remove the listener from listening to Application events.
     *
     * @param listener Object to un-register as a listener of application
     * events.
     */
    public void removeApplicationListener(final ApplicationListener listener) {
        messageCenter.removeTarget(listener, this, ApplicationListener.class);
    }

    // --------- accessors -----------------------------------------------------
    /**
     * Get the unmodifiable list of all open documents.
     *
     * @return An immutable list of the open documents.
     */
    public List<XalAbstractDocument> getDocuments() {
        return Collections.unmodifiableList(openDocuments);
    }

    /**
     * Get a copy of the list of all open documents.
     *
     * @return An immutable list of the open documents.
     */
    // suppress unchecked casting to DocumentType since there is not way around it
    @SuppressWarnings("unchecked")
    public <DocumentType extends XalAbstractDocument> List<DocumentType> getDocumentsCopy() {
        final List<XalAbstractDocument> documents = getDocuments();
        final List<DocumentType> documentsCopy = new ArrayList<>(documents.size());
        for (final XalAbstractDocument document : documents) {
            documentsCopy.add((DocumentType) document);
        }

        return documentsCopy;
    }

    /**
     * Get the custom application adaptor.
     *
     * @return The custom application adaptor.
     * @see #getAdaptor
     */
    public AbstractApplicationAdaptor getApplicationAdaptor() {
        return applicationAdaptor;
    }

    // --------- File menu actions ---------------------------------------------
    /**
     * Create and open a new empty document.
     */
    protected abstract void newDocument();

    /**
     * Create and open a new empty document of the specified type.
     *
     * @param type the type of document to create.
     */
    protected abstract void newDocument(final String type);

    /**
     * Create a new document based on a user selected document
     */
    protected void newDocumentFromTemplate() {
        updateNextDocumentOpenLocation();

        final File defaultFolder = getDefaultDocumentFolder();
        final File templateFolder = getTemplateFolder();
        final File chooserFolder = templateFolder != null && templateFolder.exists() ? templateFolder : defaultFolder;
        final JFileChooser templateChooser = new JFileChooser(chooserFolder);
        FileFilterFactory.applyFileFilters(templateChooser, applicationAdaptor.readableDocumentTypes());
        templateChooser.setMultiSelectionEnabled(true);
        templateChooser.setDialogTitle("Open Template");
        templateChooser.setApproveButtonText("Open Template");
        templateChooser.setApproveButtonToolTipText("Open new copies of the selected templates");

        openDocuments(templateChooser, true, false, false);
    }

    /**
     * Show the file chooser
     */
    private int showOpenFileChooser(final JFileChooser fileChooser) {
        return fileChooser.showOpenDialog(getActiveWindow());
    }

    /**
     * Show the save file chooser.
     *
     * @return the user's option (e.g. cancel, approve, error) for the file
     * chooser.
     */
    protected int showOpenFileChooser() {
        final int status = showOpenFileChooser(openFileChooser);

        // reconcile current directory for open and save file choosers
        saveFileChooser.setCurrentDirectory(openFileChooser.getCurrentDirectory());

        return status;
    }

    /**
     * Handle the "Open" action by opening a new document.
     */
    protected void openDocument() {
        updateNextDocumentOpenLocation();
        openDocuments(openFileChooser, false, true, true);
    }

    /**
     * Handle the "Open" action by opening a new document.
     *
     * @param fileChooser the file chooser to use for file selection
     * @param copyDocument indicates whether to copy the document (e.g. as in
     * opening a template)
     * @param syncSaveChooser synchronize the current directory of the save file
     * chooser with that of the specified file chooser
     * @param trackRecent indicates whether to track the document for recent
     * activity
     */
    private void openDocuments(final JFileChooser fileChooser, final boolean copyDocument, final boolean syncSaveChooser, final boolean trackRecent) {
        final int status = showOpenFileChooser(fileChooser);

        if (syncSaveChooser) {
            saveFileChooser.setCurrentDirectory(fileChooser.getCurrentDirectory());
        }

        switch (status) {
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.APPROVE_OPTION:
                File[] fileSelections = fileChooser.getSelectedFiles();
                openFiles(fileSelections, copyDocument, trackRecent);
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }
    }

    /**
     * Support method for opening a new document with the URL specification
     *
     * @param urlSpec The URL specification of the file to open.
     */
    protected void openURL(final String urlSpec) {
        openURL(urlSpec, false, true);
    }

    /**
     * Support method for opening a new document with the URL specification
     *
     * @param urlSpec The URL specification of the file to open.
     * @param copySource indicates whether to make a fresh copy of the source
     * (e.g. as if opening from a template)
     * @param trackRecent indicates whether to track the document for recent
     * activity
     */
    private void openURL(final String urlSpec, final boolean copySource, final boolean trackRecent) {
        try {
            final URL url = new URL(urlSpec);
            openDocument(url, copySource, trackRecent);
        } catch (MalformedURLException exception) {
            LOGGER.log(Level.WARNING, "Error opening URL: " + urlSpec, exception);
            displayError(exception);
        }
    }

    /**
     * Support method for opening a new document given a file.
     *
     * @param file The file to open.
     * @see #openFiles
     */
    protected void openFile(final File file) {
        openFile(file, false, true);
    }

    /**
     * Support method for opening a new document given a file.
     *
     * @param file The file to open.
     * @param copySource indicates whether to make a fresh copy of the source
     * (e.g. as if opening from a template)
     * @param trackRecent indicates whether to track the document for recent
     * activity
     * @see #openFiles
     */
    private void openFile(final File file, final boolean copySource, final boolean trackRecent) {
        try {
            final URL url = file.toURI().toURL();
            openDocument(url, copySource, trackRecent);
        } catch (MalformedURLException exception) {
            LOGGER.log(Level.WARNING, "Error opening file: " + file, exception);
            displayError(exception);
        }
    }

    /**
     * Support method for opening an array of files.
     *
     * @param files The files to open.
     * @see #openDocument()
     */
    protected void openFiles(final File[] files) {
        openFiles(files, false, true);
    }

    /**
     * Support method for opening an array of files.
     *
     * @param files The files to open.
     * @param copySource indicates whether to make a fresh copy of the source
     * (e.g. as if opening from a template)
     * @param trackRecent indicates whether to track the document for recent
     * activity
     * @see #openDocument()
     */
    private void openFiles(final File[] files, final boolean copySource, final boolean trackRecent) {
        for (int index = 0; index < files.length; index++) {
            File file = files[index];
            openFile(file, copySource, trackRecent);
        }
    }

    /**
     * Support method for opening a document with the specified URL.
     *
     * @param url The URL of the file to open.
     * @see #openURL
     * @see #openFile
     */
    public void openDocument(final URL url) {
        openDocument(url, false, true);
    }

    /**
     * Support method for opening a document with the specified URL.
     *
     * @param url The URL of the file to open.
     * @param copyDocument indicates whether to make a new independent copy of
     * the document at the specified URL (e.g. as if opening from a template)
     * @param trackRecent indicates whether to track the document for recent
     * activity
     * @see #openURL
     * @see #openFile
     */
    private void openDocument(final URL url, final boolean copyDocument, final boolean trackRecent) {
        try {
            XalAbstractDocument document = applicationAdaptor.generateDocument(url);
            if (copyDocument) {
                // mark the document as independent form the source URL (e.g. opened from template)
                document.setSource(null);
            }
            produceDocument(document);
            if (trackRecent && !URLReference.isRootedIn(getTemplateFolderURL(), url)) {
                // never track files under the template folder regardless of the flag
                registerRecentURL(url);
            }
            updateNextDocumentOpenLocationOffsetFrom(document);
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Error opening document: " + url, exception);
            displayError("Open Failed!", "Open failed due to an internal exception!", exception);
        }
    }

    /**
     * Handle the "Close" action by closing the specified document.
     *
     * @param document The document to close.
     */
    protected void closeDocument(final XalAbstractDocument document) {
        document.closeDocument();
    }

    /**
     * Handle the "Close All" action by closing all open documents and opening a
     * new empty document.
     */
    protected void closeAllDocuments() {
        final LinkedList<XalAbstractDocument> docList = new LinkedList<>(openDocuments);

        for (final XalAbstractDocument document : docList) {
            closeDocument(document);
        }
    }

    /**
     * Show the save file chooser.
     *
     * @return the user's option (e.g. cancel, approve, error) for the file
     * chooser.
     */
    protected int showSaveFileChooser(final XalAbstractDocument document) {
        final int status = saveFileChooser.showSaveDialog((Container) document.getDocumentView());

        // reconcile current directory between open and save file choosers
        openFileChooser.setCurrentDirectory(saveFileChooser.getCurrentDirectory());

        return status;
    }

    /**
     * Handle the "Save" action by saving the specified document. If the
     * document has a an existing file source, the document is saved to that
     * source. Otherwise, the user is shown a dialog box to select a file
     * location to which the document will be saved.
     *
     * @param document The document to save.
     */
    protected void saveDocument(final XalAbstractDocument document) {
        if (!document.hasChanges()) {
            document.displayWarning("Nothing Saved!", "This document reports no changes to save.");
            return;
        }

        if (document.getSource() != null) {
            document.saveDocument();
            saveDocumentVersion(document);
        } else {
            saveAsDocument(document);
        }
    }

    /**
     * present the user with a dialog box to open a version of the specified
     * document
     */
    protected void openDocumentVersion(final XalAbstractDocument document) {
        updateNextDocumentOpenLocation();

        final FileVersionInfo versionInfo = getSourceVersionInfo(document);
        if (versionInfo != null) {
            final File currentFolder = versionInfo.getCurrentFolder();
            final File latestFolder = currentFolder.exists() ? currentFolder : currentFolder.getParentFile();
            if (latestFolder.exists()) {
                // present a file chooser and open the document selected by the user
                final JFileChooser versionChooser = new JFileChooser(latestFolder);
                FileFilterFactory.applyFileFilters(versionChooser, applicationAdaptor.readableDocumentTypes());
                versionChooser.setMultiSelectionEnabled(true);
                openDocuments(versionChooser, false, false, false);
            } else {
                document.displayWarning("Can't open version", "No versions exist for this document.");
            }
        } else {
            document.displayWarning("Can't open version", "Nothing to open.");
        }
    }

    /**
     * get the source file version info for the document
     */
    private FileVersionInfo getSourceVersionInfo(final XalAbstractDocument document) {
        try {
            final Date now = new Date();
            final URL sourceURL = document.getSource();
            if (sourceURL != null) {
                final File sourceFile = new File(sourceURL.toURI());
                final File defaultFolder = getDefaultDocumentFolder();
                if (defaultFolder != null) {
                    return new FileVersionInfo(sourceFile, defaultFolder, now);
                } else {
                    System.err.println("Can't get source version info because no default documents directory has been specified!");
                    return null;
                }
            } else {
                return null;
            }
        } catch (URISyntaxException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            throw new RuntimeException("Exception generating source version info for document.", exception);
        }
    }

    /**
     * save a copy of the document to the versions folder:
     * documents/appname/.versions/basename/year/basename_@timestamp.extension
     */
    private void saveDocumentVersion(final XalAbstractDocument document) {
        try {
            final FileVersionInfo versionInfo = getSourceVersionInfo(document);
            if (versionInfo != null) {
                final File yearFolder = versionInfo.getCurrentFolder();
                if (yearFolder != null && (yearFolder.exists() || yearFolder.mkdirs())) {
                    final String baseName = versionInfo.getBaseName();
                    final String targetName = baseName.replaceFirst("\\.", new java.text.SimpleDateFormat("_@yyyyMMdd'T'HHmmss@").format(versionInfo.getTimestamp()) + ".");;
                    final File targetFile = new File(yearFolder, targetName);
                    targetFile.createNewFile();
                    copyFile(versionInfo.getSourceFile(), targetFile);
                    targetFile.setReadOnly();
                }
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Exception saving document version...", exception);
        }
    }

    /**
     * Handle the "Save As" action by saving the specified document to the
     * location chosen by the user. Displays a dialog box to allow the user to
     * select a location.
     *
     * @param document The document to save.
     */
    protected void saveAsDocument(final XalAbstractDocument document) {
        final String defaultName = document.getFileNameForSaving();
        final File defaultFolder = saveFileChooser.getCurrentDirectory();
        final File defaultFile = new File(defaultFolder, defaultName);

        saveFileChooser.setSelectedFile(defaultFile);
        final int status = showSaveFileChooser(document);

        switch (status) {
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.APPROVE_OPTION:
                File fileSelection = saveFileChooser.getSelectedFile();
                if (fileSelection.exists()) {
                    int confirm = document.displayConfirmDialog("Overwrite Confirmation", "The selected file:  " + fileSelection + " already exists! \n Overwrite selection?");
                    if (confirm == NO_OPTION) {
                        saveAsDocument(document);	// offer a new selection
                        return;
                    }
                }
                saveDocumentToFile(document, fileSelection);
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }
    }

    /**
     * Handle the "Save All" action by saving all open documents.
     */
    protected void saveAllDocuments() {
        for (final XalAbstractDocument document : openDocuments) {
            saveDocument(document);
        }
    }

    /**
     * Support method for saving a document to a file.
     *
     * @param document The document to save.
     * @param file The file to which the document will be saved.
     */
    protected void saveDocumentToFile(final XalAbstractDocument document, final File file) {
        try {
            final URL url = file.toURI().toURL();
            document.saveDocumentAs(url);
            document.setSource(url);
            if (!URLReference.isRootedIn(getTemplateFolderURL(), url)) {
                registerRecentURL(url);
            }
            saveDocumentVersion(document);
        } catch (MalformedURLException exception) {
            LOGGER.log(Level.WARNING, "Failed to save document to file: " + file, exception);
            LOGGER.log(Level.SEVERE, null, exception);
            document.displayError("Save Error", "Error attempting to save the document.", exception);
        }
    }

    /**
     * Handle the "Revert To Saved" action by reverting the specified document
     * to that of its source file.
     *
     * @param document The document to revert.
     */
    protected void revertToSaved(final XalAbstractDocument document) {
        // don't revert if there are no changes
        if (!document.hasChanges()) {
            document.displayWarning("No revert!", "This document reports no changes from the original.");
            return;
        }

        URL source = document.getSource();

        if (source == null) {
            document.displayWarning("No revert!", "There is no source to revert to.");
            return;
        }

        if (document.closeDocument()) {
            openDocument(source);
        }
    }

    /**
     * Handle the "Quit" action by quitting the application.
     */
    public void quit() {
        boolean warnUnsavedChanges = false;
        final List<XalAbstractDocument> documents = getDocuments();
        for (final XalAbstractDocument document : documents) {
            warnUnsavedChanges |= (document.warnUserOfUnsavedChangesWhenClosing() && document.hasChanges());
        }

        if (warnUnsavedChanges) {
            try {
                int status = JOptionPane.showConfirmDialog(getActiveWindow(),
                        "Some documents have unsaved changes.  Continue Quitting?", "Unsaved Changes",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (status == JOptionPane.NO_OPTION) {
                    return;
                }
            } catch (HeadlessException exception) {
                LOGGER.log(Level.SEVERE, "Exception while quitting the application.", exception);
            }
        }

        rbacLogout();

        if (noticeProxy != null) {
            noticeProxy.applicationWillQuit();
        }

        System.exit(0);
    }

    /**
     * Prompts a logout dialog for the RBAC user to logout and logs out the user
     * if <code>YES_OPTION</code> was selected. Otherwise it does nothing.
     *
     * @return true if user was logged out or if user is not logged in, false
     * otherwise
     */
    private boolean rbacLogout() {
        useRBACLogin = RBACPlugin.useRBACLogin();

        if (rbacSubject != null && useRBACLogin) {
            final int option = JOptionPane.showConfirmDialog(getActiveWindow(), "Would you like to logout?", "Logout",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                try {
                    rbacSubject.logout();
                } catch (RBACException e) {
                    JOptionPane.showMessageDialog(getActiveWindow(), e.getMessage(), "Error while trying to logout",
                            JOptionPane.ERROR_MESSAGE);
                }
                rbacSubject = null;
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Changes the user
     */
    public void changeRBACUser() {
        boolean isLogout = rbacLogout();
        if (!isLogout) {
            // The user did not logout
            return;
        }
        while (true) {
            if (authenticateWithRBAC()) {
                if (authorizeWithRBAC("Run")) {
                    break;
                } else {
                    final int option = JOptionPane.showConfirmDialog(getActiveWindow(),
                            "No authorisation. Would you like to login with another user?", "No authorisation",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        try {
                            rbacSubject.logout();
                        } catch (RBACException e) {
                            JOptionPane.showMessageDialog(getActiveWindow(), e.getMessage(),
                                    "Error while trying to logout", JOptionPane.ERROR_MESSAGE);
                        }
                        rbacSubject = null;
                    } else {
                        quit();
                    }
                }
            }
        }
    }

    // --------- Window menu actions -------------------------------------------
    /**
     * Handle the "Cascade Windows" action by cascading all document windows
     * about the target document.
     *
     * @param targetDocument The document about whose window all document
     * windows should cascade
     */
    protected void cascadeWindowsAbout(final XalAbstractDocument targetDocument) {
        final Point windowOrigin = targetDocument.getDocumentView().getLocation();
        final List<XalAbstractDocument> documents = getDocuments();
        for (final XalAbstractDocument document : documents) {
            final XalDocumentView window = document.getDocumentView();
            try {	// iconified windows will throw exceptions
                window.setVisible(true);
                final java.awt.Container contentPane = window.getContentPane();
                final int offset = window.isVisible() ? (int) (1.5 * (contentPane.getLocationOnScreen().y - window.getLocationOnScreen().y)) : 50;

                // must do this so we can force the window to move ???
                window.setVisible(false);
                window.setLocation(windowOrigin);
                // restore the window to visible
                window.setVisible(true);
                document.showDocument();
                // prepare for next window
                windowOrigin.translate(offset, offset);
            } catch (Exception exception) {
            }
        }
    }

    /**
     * Handle the "Show All" action by showing all main windows corresponding to
     * the open documents. The windows are brought to the front and un-collapsed
     * as necessary.
     */
    protected void showAllWindows() {
        for (final XalAbstractDocument document : openDocuments) {
            document.showDocument();
        }
    }

    /**
     * Handle the "Hide All" action by hiding all main windows corresponding to
     * the open documents.
     */
    protected void hideAllWindows() {
        // hide the console
        Console.hide();

        for (final XalAbstractDocument document : openDocuments) {
            document.hideDocument();
        }
    }

    /**
     * show the about box
     */
    public static void showAboutBox() {
        AboutBox.showNear(getActiveWindow());
    }

    // --------- Manage History ------------------------------------------------
    /**
     * Register the URL of a document that has recently been opened or saved.
     * These URLs appear in the "Open Recent" submenu of the File menu. These
     * items get saved in the user's preferences for this application as
     * identified by the custom application adaptor class.
     *
     * @param url The URL to register.
     */
    void registerRecentURL(final URL url) {
        // if the url is from inside a jar file then don't cache it
        if (url.getProtocol().equalsIgnoreCase("jar")) {
            return;
        }

        recentFileTracker.cacheURL(url);
    }

    /**
     * Get the array of URLs corresponding to recently opened or saved
     * documents. Fetch the recent items from the list saved in the user's
     * preferences for this application.
     *
     * @return The array of recent URLs.
     */
    String[] getRecentURLSpecs() {
        return recentFileTracker.getRecentURLSpecs();
    }

    /**
     * Get the most recently visited folder saved in the user's preferences for
     * this application.
     *
     * @return The most recently visited folder.
     */
    private File getRecentFolder() {
        return recentFileTracker.getRecentFolder();
    }

    /**
     * Handle the "Clear" event associated with the list of recent items. Clear
     * the list of URLs corresponding to the recently opened or saved documents.
     * Clear the list in the user's preferences for this application.
     */
    void clearRecentItems() {
        recentFileTracker.clearCache();
    }

    /**
     * Get this application's template folder creating it if possible and
     * necessary
     */
    private File getTemplateFolder() {
        if (templateFolder == null) {
            final File defaultFolder = getDefaultDocumentFolder();
            final File templateFolder = defaultFolder != null && defaultFolder.exists() ? new File(defaultFolder, "Templates") : null;

            // attempt to make the template folder if it doesn't already exist but the default folder does
            if (templateFolder != null && !templateFolder.exists()) {
                if (defaultFolder.canWrite()) {
                    templateFolder.mkdir();
                }
            }

            this.templateFolder = templateFolder;
        }

        return templateFolder;
    }

    /**
     * Get the URL to this application's template folder creating it if possible
     * and necessary
     */
    private URL getTemplateFolderURL() {
        final File templateFolder = getTemplateFolder();
        try {
            return templateFolder != null ? templateFolder.toURI().toURL() : null;
        } catch (MalformedURLException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            throw new RuntimeException("Exception getting the template URL", exception);
        }
    }

    /**
     * Get the default document folder.
     *
     * @return the default folder for documents or null if none has been set.
     */
    public File getDefaultDocumentFolder() {
        if (defaultDocumentFolder == null) {
            defaultDocumentFolder = applicationAdaptor.getDefaultDocumentFolder();
        }

        return defaultDocumentFolder;
    }

    /**
     * Get the default document folder as a URL.
     *
     * @return the default folder for documents as a URL or null if none has
     * been set.
     */
    public URL getDefaultDocumentFolderURL() {
        return applicationAdaptor.getDefaultDocumentFolderURL();
    }

    // --------- Application Management ----------------------------------------
    /**
     * Handle the launching of the application by creating the application
     * instance and performing application initialization.
     *
     * @param adaptor The custom application adaptor.
     */
    public static void launch(final AbstractApplicationAdaptor adaptor) {
        try {
            // get the document URLs passed at the command line
            final URL[] docURLs = AbstractApplicationAdaptor.getDocURLs();
            if (docURLs.length > 0) {
                launch(adaptor, docURLs);
            }
        } catch (NullPointerException exception) {
            launch(adaptor, new URL[]{});
        }
    }

    /**
     * Handle the launching of the application by creating the application
     * instance and performing application initialization.
     *
     * @param adaptor The custom application adaptor.
     * @param urls The URLs of documents to open upon launching the application
     */
    public static void launch(final AbstractApplicationAdaptor adaptor, final URL[] urls) {
        adaptor.launchApplication(urls);
    }

    /**
     * Convenience method for getting the custom application adaptor. There is
     * only one such adaptor for the entire application.
     *
     * @return The custom application adaptor.
     * @see #getApplicationAdaptor
     */
    public static AbstractApplicationAdaptor getAdaptor() {
        return application.getApplicationAdaptor();
    }

    /**
     * Get the application instance. There is only one application instance per
     * application.
     *
     * @return The application instance.
     */
    public static Application getApp() {
        return application;
    }

    /**
     * Get the active window which is in focus for this application. It is
     * typically a good window relative to which you can place application
     * warning dialog boxes.
     *
     * @return The active window
     */
    public static Window getActiveWindow() {
        return ApplicationSupport.getActiveWindow();
    }

    /**
     * Add a new document to this application and show it
     *
     * @param document the document to produce
     */
    public void produceDocument(final XalAbstractDocument document) {
        produceDocument(document, true);
    }

    /**
     * Add a new document to this application and if makeVisible is true, show
     * it
     *
     * @param document the document to produce
     * @param makeVisible make the document visible
     */
    public abstract void produceDocument(final XalAbstractDocument document, final boolean makeVisible);

    //------------------- Convenience methods -----------------------------------
    /**
     * Copy the source file to the target file
     */
    private static void copyFile(final File sourceFile, final File targetFile) {
        try {
            final FileChannel sourceChannel = new FileInputStream(sourceFile).getChannel();
            final FileChannel targetChannel = new FileOutputStream(targetFile).getChannel();

            sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);

            sourceChannel.close();
            targetChannel.close();
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            throw new RuntimeException("Exception attempting to copy the source file to the target file.", exception);
        }
    }

    /**
     * Display a confirmation dialog with a title and message
     *
     * @param title The title of the dialog
     * @param message The message to display
     * @return YES_OPTION or NO_OPTION
     */
    public static int displayConfirmDialog(final String title, final String message) {
        return ApplicationSupport.displayConfirmDialog(title, message);
    }

    /**
     * Display a warning dialog box with information about the exception.
     *
     * @param exception The exception about which the warning dialog is
     * displayed.
     */
    public static void displayWarning(final Exception exception) {
        ApplicationSupport.displayWarning(exception);
    }

    /**
     * Display a warning dialog box.
     *
     * @param title Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    public static void displayWarning(final String title, final String message) {
        ApplicationSupport.displayWarning(title, message);
    }

    /**
     * Display a warning dialog box with information about the exception. This
     * method allows clarification about the consequences of the exception (e.g.
     * "Save Failed:").
     *
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the
     * exception message.
     * @param exception The exception about which the warning dialog is
     * displayed.
     */
    public static void displayWarning(final String title, final String prefix, final Exception exception) {
        ApplicationSupport.displayWarning(title, prefix, exception);
    }

    /**
     * Display an error dialog box.
     *
     * @param title Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    public static void displayError(final String title, final String message) {
        ApplicationSupport.displayError(title, message);
    }

    /**
     * Display an error dialog box with information about the exception.
     *
     * @param exception The exception about which the warning dialog is
     * displayed.
     */
    public static void displayError(final Exception exception) {
        ApplicationSupport.displayError(exception);
    }

    /**
     * Display an error dialog box with information about the exception. This
     * method allows clarification about the consequences of the exception (e.g.
     * "Save Failed:").
     *
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the
     * exception messasge.
     * @param exception The exception about which the warning dialog is
     * displayed.
     */
    public static void displayError(final String title, final String prefix, final Exception exception) {
        ApplicationSupport.displayError(title, prefix, exception);
    }

    /**
     * Display an error dialog box with information about the exception. This
     * method allows clarification about the consequences of the exception (e.g.
     * "Save Failed:").
     *
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the
     * exception messasge.
     * @param exception The exception about which the warning dialog is
     * displayed.
     */
    public static void displayApplicationError(final String title, final String prefix, final Exception exception) {
        displayError(title, prefix, exception);
    }

    /**
     * manage the welcome dialog
     */
    private class WelcomeController {

        /**
         * Open a new empty document
         */
        private static final int NEW_MODE = 0;

        /**
         * Open a document for read/write
         */
        private static final int DOCUMENT_MODE = NEW_MODE + 1;

        /**
         * Create a new document based on a template
         */
        private static final int TEMPLATE_MODE = DOCUMENT_MODE + 1;

        /**
         * Open a recently viewed document
         */
        private static final int RECENT_MODE = TEMPLATE_MODE + 1;

        /**
         * DOCUMENT_CHOOSER for the display
         */
        private final JFileChooser documentChooser;

        /**
         * indicates the mode for which to open a document
         */
        private int openMode;

        /**
         * Constructor
         */
        private WelcomeController(final Point location) {
            final Box accessory = new Box(BoxLayout.Y_AXIS);

            final JButton newButton = new JButton("New Empty");
            newButton.setToolTipText("Create a new, empty document");

            final JButton openButton = new JButton("Documents...");
            openButton.setToolTipText("Display documents to open for editing");

            final JButton templateButton = new JButton("Templates...");
            templateButton.setToolTipText("Display documents for which to open new copies");

            final JButton recentButton = new JButton("Recent...");
            recentButton.setToolTipText("Display recently opened documents to open for editing");

            accessory.add(newButton);
            accessory.add(openButton);
            accessory.add(templateButton);
            accessory.add(recentButton);

            final URLReference[] recentURLReferences = getValidRecentURLReferences();
            if (recentURLReferences == null || recentURLReferences.length == 0) {
                recentButton.setEnabled(false);
            }

            documentChooser = new WelcomeFileChooser(location);
            documentChooser.setAccessory(accessory);
            documentChooser.setMultiSelectionEnabled(true);
            documentChooser.setDialogTitle("Select " + getAdaptor().applicationName() + " documents to open");
            FileFilterFactory.applyFileFilters(documentChooser, applicationAdaptor.readableDocumentTypes());

            final File templateFolder = getTemplateFolder();
            final File documentFolder = getDefaultDocumentFolder();

            setOpenMode(TEMPLATE_MODE);

            if (templateFolder != null && templateFolder.exists() && templateFolder.isDirectory() && templateFolder.list().length > 0) {
                documentChooser.setCurrentDirectory(templateFolder);
                setOpenMode(TEMPLATE_MODE);
            } else if (documentFolder != null && documentFolder.exists() && documentFolder.isDirectory()) {
                documentChooser.setCurrentDirectory(documentFolder);
                setOpenMode(DOCUMENT_MODE);
            } else {
                setOpenMode(DOCUMENT_MODE);
            }

            newButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent event) {
                    setOpenMode(NEW_MODE);
                    documentChooser.approveSelection();
                }
            });

            openButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent event) {
                    documentChooser.setCurrentDirectory(documentFolder);
                    setOpenMode(DOCUMENT_MODE);
                }
            });

            templateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent event) {
                    documentChooser.setCurrentDirectory(templateFolder);
                    setOpenMode(TEMPLATE_MODE);
                }
            });

            recentButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent event) {
                    final URLReference selection = (URLReference) JOptionPane.showInputDialog(documentChooser, "Open the selected document", "Recent Documents", JOptionPane.PLAIN_MESSAGE, null, recentURLReferences, null);
                    if (selection != null) {
                        setOpenMode(RECENT_MODE);
                        documentChooser.approveSelection();
                        openURL(selection.getFullURLSpec());
                    }
                }
            });

            int status = documentChooser.showOpenDialog(null);

            switch (status) {
                case JFileChooser.CANCEL_OPTION:
                    System.exit(0);
                    break;
                case JFileChooser.APPROVE_OPTION:
                    processSelections(documentChooser.getSelectedFiles());
                    break;
                default:
                    newDocument();
                    break;
            }
        }

        /**
         * Set the open mode
         */
        private void setOpenMode(final int mode) {
            openMode = mode;

            switch (mode) {
                case TEMPLATE_MODE:
                    documentChooser.setApproveButtonText("Open Template");
                    documentChooser.setApproveButtonToolTipText("Open new copies of the selected templates");
                    break;
                case DOCUMENT_MODE:
                    documentChooser.setApproveButtonText("Open");
                    documentChooser.setApproveButtonToolTipText("Open the documents for editing");
                    break;
                default:
                    break;
            }
        }

        /**
         * perform the operation indicated by the mode
         */
        private void processSelections(final File[] selections) {
            if (openMode != RECENT_MODE && (selections == null || selections.length == 0)) {
                newDocument();
                return;
            }

            switch (openMode) {
                case NEW_MODE:
                    newDocument();
                    break;
                case TEMPLATE_MODE:
                    // open templates
                    openFiles(selections, true, false);
                    break;
                case DOCUMENT_MODE:
                    // open documents
                    openFiles(selections);
                    break;
                default:
                    break;
            }
        }

        /**
         * get those recent URL specs which are valid
         */
        private URLReference[] getValidRecentURLReferences() {
            return URLReference.getValidReferences(getDefaultDocumentFolderURL(), getRecentURLSpecs());
        }
    }

    /**
     * custom file chooser for the welcome window
     */
    private class WelcomeFileChooser extends JFileChooser {

        /**
         * serialization ID
         */
        private static final long serialVersionUID = 1L;

        /**
         * initial location for the dialog
         */
        final Point INITIAL_LOCATION;

        /**
         * constructor
         */
        public WelcomeFileChooser(final Point location) {
            super();

            INITIAL_LOCATION = location;
        }

        /**
         * Create the dialog
         */
        @Override
        protected JDialog createDialog(final java.awt.Component parent) throws java.awt.HeadlessException {
            final JDialog dialog = super.createDialog(parent);
            dialog.setLocation(INITIAL_LOCATION);

            dialog.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentMoved(final java.awt.event.ComponentEvent event) {
                    setNextDocumentOpenLocation(dialog.getLocationOnScreen());
                }
            });

            return dialog;
        }
    }
}

/**
 * container of file versions info
 */
class FileVersionInfo {

    private final String BASE_NAME;
    private final File CURRENT_FOLDER;
    private final Date TIMESTAMP;
    private final File SOURCE_FILE;

    /**
     * Constructor
     */
    public FileVersionInfo(final File sourceFile, final File defaultFolder, final Date timestamp) {
        TIMESTAMP = timestamp;
        SOURCE_FILE = sourceFile;

        if (sourceFile.exists() && sourceFile.canRead()) {
            BASE_NAME = getBaseNameStrippingTimestamp(sourceFile);
            if (defaultFolder != null && defaultFolder.exists()) {
                final File versionsFolder = new File(defaultFolder, ".versions");
                final File baseFolder = new File(versionsFolder, BASE_NAME);

                final String yearString = new java.text.SimpleDateFormat("yyyy").format(timestamp);
                CURRENT_FOLDER = new File(baseFolder, yearString);
            } else {
                CURRENT_FOLDER = null;
            }
        } else {
            BASE_NAME = null;
            CURRENT_FOLDER = null;
        }
    }

    /**
     * get the source file
     */
    public File getSourceFile() {
        return SOURCE_FILE;
    }

    /**
     * versions base name for the source file
     */
    public String getBaseName() {
        return BASE_NAME;
    }

    /**
     * current folder to hold the latest version
     */
    public File getCurrentFolder() {
        return CURRENT_FOLDER;
    }

    /**
     * get the timestamp
     */
    public Date getTimestamp() {
        return TIMESTAMP;
    }

    /**
     * Get the base name of a file stripping timestamp tags if any
     */
    private String getBaseNameStrippingTimestamp(final File file) {
        return file.getName().replaceFirst("_@.*@", "");
    }
}
