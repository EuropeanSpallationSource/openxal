/*
 * Copyright (C) 2020 European Spallation Source ERIC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.extension.fxapplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javafx.application.Application;
import java.net.URL;
import java.util.Date;
import javafx.beans.property.SimpleStringProperty;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.MenuBar;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import xal.extension.application.ApplicationStatus;
import xal.extension.jelog.ElogServer;
import xal.extension.service.ServiceDirectory;
import xal.extension.service.ServiceException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.data.XMLDataManager;
import xal.tools.xml.XmlDataAdaptor.ParseException;

/**
 * The Application class handles defines the core of an application. It is often
 * the first handler of application wide events and typically forwards those
 * events to the custom application adaptor for further processing. Every
 * application has exactly one instance of this class.
 *
 * For now the FxApplication does nothing (except inheriting all from
 * Application)
 *
 * @author Yngve Levinsen <yngve.levinsen@ess.eu>
 */
public abstract class FxApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(FxApplication.class.getName());

    protected String MAIN_SCENE = "/fxml/Scene.fxml";
    protected static String CSS_STYLE = "/styles/Styles.css";
    private String STAGE_TITLE = "Demo Application";

    private enum THEME {
        DEFAULT,
        DARK
    }

    private static THEME theme = THEME.DEFAULT;

    protected XalFxDocument DOCUMENT;

    private final Date LAUNCH_TIME;

    // Set to false if this application doesn't save/load xml files
    protected boolean HAS_DOCUMENTS = true;

    // Set to false if this application doesn't need to load an accelerator
    protected boolean HAS_ACCELERATOR = true;

    // Set to false if this application doesn't need the machine sequences
    protected boolean HAS_SEQUENCE = true;

    protected MenuBar MENU_BAR;

    /**
     * **Declare static Stage**
     */
    private static Stage stage;

    /**
     * Application constructor.
     */
    protected FxApplication() {
        this(new URL[]{});
    }

    /**
     * Application constructor.
     *
     * @param urls An array of document URLs to open upon startup.
     */
    protected FxApplication(final URL[] urls) {
        super();

        LAUNCH_TIME = new Date();
    }

    private void setStage(Stage stage) {
        FxApplication.stage = stage;
    }

    public static Stage getStage() {
        return stage;
    }

    public String getApplicationName() {
        return STAGE_TITLE;
    }

    public void setApplicationName(String applicationName) {
        this.STAGE_TITLE = applicationName;
    }

    public XalFxDocument getDocument() {
        return DOCUMENT;
    }

    private static void setDefaultStyle() {
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-SemiBoldItalic.ttf").toExternalForm(), 10);
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-Black.ttf").toExternalForm(), 10);
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-BlackItalic.ttf").toExternalForm(), 10);
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-Bold.ttf").toExternalForm(), 10);
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-BoldItalic.ttf").toExternalForm(), 10);
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-ExtraLight.ttf").toExternalForm(), 10);
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-ExtraLightItalic.ttf").toExternalForm(), 10);
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-Italic.ttf").toExternalForm(), 10);
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-Light.ttf").toExternalForm(), 10);
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-LightItalic.ttf").toExternalForm(), 10);
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-Regular.ttf").toExternalForm(), 10);
        Font.loadFont(FxApplication.class.getResource("/fonts/SourceSansPro-SemiBold.ttf").toExternalForm(), 10);
    }

    /**
     * This method sets the default Style for Open XAL applications (including
     * Source Sans Pro font). It is public and static to be able to use it from
     * application that don't extend FxApplication.
     */
    public static void setOxalStyle(Scene scene) {
        theme = THEME.DEFAULT;
        setTheme(scene);
    }

    public static void setOxalDarkStyle(Scene scene) {
        theme = THEME.DARK;
        setTheme(scene);
    }

    public static void setTheme(Scene scene) {
        ObservableList<String> styleSheets = scene.getStylesheets();
        setUserAgentStylesheet(null);
        setDefaultStyle();
        styleSheets.clear();
        styleSheets.add(FxApplication.class.getResource("/styles/DefaultStyle.css").toExternalForm());
        if (theme == THEME.DARK) {
            styleSheets.add(FxApplication.class.getResource("/styles/modena_dark.css").toExternalForm());
        }
        styleSheets.add(FxApplication.class.getResource(CSS_STYLE).toExternalForm());
    }

    /**
     * This is called at the beginning of start(), after calling the setup()
     * method. It generates the menu bar (so that you can add items to MENU_BAR
     * etc after). Then start() calls beforeStart() for application specific
     * startup preparations.
     */
    private void initialize() {
        // If an application requires sequences, then force HAS_SEQUENCE=true
        if (HAS_SEQUENCE) {
            HAS_ACCELERATOR = true;
        }

        try {
            if (HAS_ACCELERATOR) {
                String acceleratorMainPath = XMLDataManager.defaultPath();
                if (acceleratorMainPath == null) {
                    acceleratorMainPath = latticeErrorDialog("Default accelerator not set", "Press OK to open file dialog to select the path to the accelerator lattice files or Cancel to close the application.");
                }
                while (!new File(acceleratorMainPath).exists()) {
                    acceleratorMainPath = latticeErrorDialog("Default accelerator lattice not found", "Press OK to open file dialog to select the path to the accelerator lattice files or Cancel to close the application.");
                }

                setAcceleratorWithPath(acceleratorMainPath);
                Logger.getLogger(FxApplication.class.getName()).log(Level.INFO, "Loading default accelerator {0}", XMLDataManager.defaultPath());
                if (DOCUMENT.getAccelerator() == null) {
                    Logger.getLogger(FxApplication.class.getName()).log(Level.INFO, "Problems loading default accelerator and no other accelerator selected.\nAborting loading of application.");
                    stop();
                }
            }

            MENU_BAR = new MenuBar();

            Menu fileMenu = new Menu("File");
            if (HAS_DOCUMENTS) {
                MenuItem newFileMenu = new MenuItem("New");
                newFileMenu.setOnAction((e) -> newFileMenuHandler());
                final MenuItem saveFileMenu = new MenuItem("Save");
                saveFileMenu.setOnAction((e) -> saveFileMenuHandler(false));
                final MenuItem saveAsFileMenu = new MenuItem("Save as..");
                saveAsFileMenu.setOnAction((e) -> saveFileMenuHandler(true));
                final MenuItem loadFileMenu = new MenuItem("Load");
                loadFileMenu.setOnAction((e) -> loadFileMenuHandler());
                fileMenu.getItems().addAll(newFileMenu, saveFileMenu, saveAsFileMenu, loadFileMenu);
            }
            final MenuItem exitMenu = new MenuItem("Exit");
            exitMenu.setOnAction((e) -> exitMenuHandler());
            fileMenu.getItems().addAll(exitMenu);

            final Menu editMenu = new Menu("Edit");

            final Menu acceleratorMenu = new Menu("Accelerator");
            final Menu sequenceMenu = new Menu("Sequence");
            final ToggleGroup groupSequence = new ToggleGroup();
            if (HAS_ACCELERATOR) {
                final MenuItem loadDefaultAcceleratorMenu = new MenuItem("Load Default Accelerator");
                loadDefaultAcceleratorMenu.setOnAction((e) -> loadDefaultAcceleratorMenuHandler());
                final MenuItem loadAcceleratorMenu = new MenuItem("Load Accelerator ...");
                loadAcceleratorMenu.setOnAction((e) -> loadAcceleratorMenuHandler());
                final MenuItem testModeMenu = new MenuItem("Enable Test Mode");
                testModeMenu.setOnAction((e) -> testModeMenuHandler(e));
                acceleratorMenu.getItems().addAll(loadDefaultAcceleratorMenu, loadAcceleratorMenu, testModeMenu);

                if (HAS_SEQUENCE && DOCUMENT.accelerator.getAccelerator() != null) {
                    buildSequenceMenu(DOCUMENT.accelerator.getAccelerator(), sequenceMenu, groupSequence);
                    acceleratorMenu.getItems().addAll(new SeparatorMenuItem(), sequenceMenu);
                }
            }

            final Menu eLogMenu = new Menu("eLog");
            final MenuItem openLogMenu = new MenuItem("Open");
            openLogMenu.setOnAction((e) -> urlMenuHandler());
            final MenuItem makePostMenu = new MenuItem("Post Screen Shot");
            makePostMenu.setOnAction((e) -> eLogMenuHandler("image"));
            final MenuItem makePostDataMenu = new MenuItem("Post Data");
            makePostDataMenu.setOnAction((e) -> eLogMenuHandler("file"));
            if (HAS_DOCUMENTS) {
                eLogMenu.getItems().addAll(openLogMenu, makePostMenu, makePostDataMenu);
            } else {
                eLogMenu.getItems().addAll(openLogMenu, makePostMenu);
            }

            final Menu viewMenu = new Menu("View");
            final MenuItem switchThemeMenu;
            if (theme == THEME.DEFAULT) {
                switchThemeMenu = new MenuItem("Set dark Theme");
            } else {
                switchThemeMenu = new MenuItem("Set default Theme");
            }
            switchThemeMenu.setOnAction((e) -> {
                if (theme == THEME.DEFAULT) {
                    setOxalDarkStyle(stage.getScene());
                    switchThemeMenu.setText("Set default Theme");
                } else {
                    theme = THEME.DEFAULT;
                    setOxalStyle(stage.getScene());
                    switchThemeMenu.setText("Set dark Theme");
                }
            });
            viewMenu.getItems().add(switchThemeMenu);

            final Menu helpMenu = new Menu("Help");
            final MenuItem docMenu = new MenuItem("Documentation");
            docMenu.setOnAction((e) -> helpMenuHandler());
            final MenuItem aboutMenu = new MenuItem("About");
            aboutMenu.setOnAction((e) -> aboutMenuHandler());
            helpMenu.getItems().addAll(docMenu, aboutMenu);

            MENU_BAR.getMenus().addAll(fileMenu, editMenu);
            if (HAS_ACCELERATOR) {
                MENU_BAR.getMenus().add(acceleratorMenu);
            }
            MENU_BAR.getMenus().addAll(eLogMenu, viewMenu, helpMenu);

            DOCUMENT.accelerator.addChangeListener((ChangeListener) (ObservableValue o, Object oldVal, Object newVal) -> {
                if (HAS_SEQUENCE && DOCUMENT.accelerator.getAccelerator() != null) {
                    DOCUMENT.sequence.set(null);
                    sequenceMenu.getItems().clear();
                    groupSequence.getToggles().clear();
                    buildSequenceMenu(DOCUMENT.accelerator.getAccelerator(), sequenceMenu, groupSequence);
                    Logger.getLogger(FxApplication.class.getName()).log(Level.INFO, "Rebuilding Sequence Menu.");
                }
            });

            DOCUMENT.sequence.addListener((ChangeListener<? super String>) (ObservableValue<? extends String> o, String oldVal, String newVal) -> {
                if (newVal == null) {
                    ((RadioMenuItem) sequenceMenu.getItems().get(0)).setSelected(true);
                } else {
                    for (Toggle item : groupSequence.getToggles()) {
                        if (((RadioMenuItem) item).getText().equals(newVal)) {
                            ((RadioMenuItem) item).setSelected(true);
                        }
                    }
                }
            });

            // display the menu bar at the top of the screen consistent with the Mac look and feel
            final String os = System.getProperty("os.name");
            if (os != null && os.startsWith("Mac")) {
                MENU_BAR.useSystemMenuBarProperty().set(true);
            }

            registerApplicationStatusService();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Exception in the initialize method.", e);
        }
    }

    /**
     * start() calls this method before initialize(). It should be used to
     * define the DOCUMENT variable.
     */
    public abstract void setup(Stage stage);

    /**
     * start() calls this method after initialize() and before showing the
     * scene. For example, here the user can modify the menu bar.
     */
    public void beforeStart(Stage stage) {
        // Default implementation does nothing.
    }

    @Override
    public final void start(Stage stage) throws IOException {
        try {
            setup(stage);
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error in the setup method of FxApplication.", e);
            throw (e);
        }

        FXMLLoader loader = null;
        try {
            initialize();

            setStage(stage);

            VBox root = new VBox();

            loader = new FXMLLoader(getClass().getResource(MAIN_SCENE));

            root.getChildren().add(MENU_BAR);
            Node applicationScene = loader.load();
            VBox.setVgrow(applicationScene, Priority.ALWAYS);
            root.getChildren().add(applicationScene);

            Scene scene = new Scene(root);

            // Set default style and application specific CSS
            setOxalStyle(scene);
            scene.getStylesheets().add(CSS_STYLE);

            stage.getProperties().put("hostServices", this.getHostServices());

            stage.setTitle(STAGE_TITLE);
            stage.setScene(scene);
            //YIL It is probably very bad to set this here but I am a stupid person.
            DOCUMENT.sourceString = new SimpleStringProperty(DOCUMENT.DEFAULT_FILENAME);
            DOCUMENT.sourceString.addListener((observable, oldValue, newValue) -> stage.setTitle(STAGE_TITLE + ": " + newValue));

            loader.<Controller>getController().setApplication(this);
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error loading the scene.", e);
            throw (e);
        }

        try {
            beforeStart(stage);
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error in the beforeStart method of FxApplication.", e);
            throw (e);
        }

        try {
            loader.<Controller>getController().beforeStart();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error in the beforeStart method of Controller.", e);
            throw (e);
        }

        stage.show();
    }

    public void buildSequenceMenu(Accelerator accelerator, Menu sequenceMenu, ToggleGroup groupSequence) {
        RadioMenuItem acceleratorItem = new RadioMenuItem("Full Accelerator");
        acceleratorItem.setToggleGroup(groupSequence);
        acceleratorItem.setOnAction((e) -> DOCUMENT.getSequenceProperty().set(null));
        sequenceMenu.getItems().addAll(acceleratorItem, new SeparatorMenuItem());

        //Populate the Sequence Menu with the sequences of the machine
        List<AcceleratorSeq> seqItem = accelerator.getSequences();

        for (AcceleratorSeq item : seqItem) { //AddSequences
            RadioMenuItem addedItem = new RadioMenuItem(item.toString());
            sequenceMenu.getItems().add(addedItem);
            addedItem.setToggleGroup(groupSequence);
            addedItem.setOnAction((e) -> selectSequenceMenuHandler(e));
        }

        sequenceMenu.getItems().add(new SeparatorMenuItem());

        List<AcceleratorSeqCombo> seqCombo = accelerator.getComboSequences();
        for (AcceleratorSeqCombo item : seqCombo) { //AddCombos
            RadioMenuItem addedItem = new RadioMenuItem(item.toString());
            sequenceMenu.getItems().add(addedItem);
            addedItem.setToggleGroup(groupSequence);
            addedItem.setOnAction((e) -> selectSequenceMenuHandler(e));
        }
        sequenceMenu.getItems().add(new SeparatorMenuItem());

        final MenuItem addCombo = new MenuItem("Add new Combo Sequence");
        addCombo.setOnAction((e) -> addComboHandler(groupSequence, e));
        sequenceMenu.getItems().add(addCombo);
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
                ServiceDirectory.defaultDirectory().registerService(ApplicationStatus.class, STAGE_TITLE, new FxApplicationStatusService(this));
                Logger.getLogger(FxApplication.class.getName()).log(Level.INFO, "Registered application services...");
            } catch (ServiceException exception) {
                System.err.println("Service registration failed due to " + exception);
                Logger.getLogger(FxApplication.class.getName()).log(Level.SEVERE, "Service registration failed due to ", exception);
            }
        } else {
            LOGGER.log(Level.CONFIG, "Application services disabled.");
        }
    }

    void showAllWindows() {
        Platform.runLater(getStage()::toFront);
    }

    /**
     * Overriding the stop method to ensure that applications close properly
     * after calling Platform.exit(). In case some application need to perform
     * some preparation before exiting, this method should be overridden.
     */
    @Override
    public void stop() {
        System.exit(0);
    }

    void quit() {
        Platform.exit();
    }

    /**
     * Get the launch time which is the time at which the Application instance
     * was instantiated.
     *
     * @return The launch time
     */
    public Date getLaunchTime() {
        return LAUNCH_TIME;
    }

    private String latticeErrorDialog(String title, String message) {
        String acceleratorMainPath = null;
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load accelerator lattice files");
            fileChooser.setInitialFileName("main.xal");

            //Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Open XAL lattice" + " (*.xal)", "*.xal");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show open file dialog
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile == null) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "No lattice file selected.");
                acceleratorMainPath = latticeErrorDialog("No lattice file selected.", message);
            } else {
                if (selectedFile.exists() && selectedFile.canRead()) {
                    acceleratorMainPath = selectedFile.getAbsolutePath();
                    XMLDataManager.setDefaultPath(acceleratorMainPath);
                } else {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not open {0}", acceleratorMainPath);
                }
            }
        } else {
            System.exit(0);
        }
        return acceleratorMainPath;
    }

    /* *****************
    * Menubar handlers *
    ********************/
    /**
     * Handles creation of new files. By default it calls the newDocument()
     * method of the XalFxDocument class, which must be implemented by
     * subclasses.
     */
    protected void newFileMenuHandler() {
        DOCUMENT.newDocument();
    }

    /**
     * Handles saving of files. It creates a saving dialog by default, and then
     * calls the saveDocument() method of the XalFxDocument class.
     */
    protected void saveFileMenuHandler(boolean saveAs) {
        if (saveAs || !DOCUMENT.sourceSetAndValid()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Application State");
            fileChooser.setInitialFileName(DOCUMENT.getDefaultFilename());

            //Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(DOCUMENT.FILETYPE_DESCRIPTION + " (" + DOCUMENT.WILDCARD_FILE_EXTENSION + ")", DOCUMENT.WILDCARD_FILE_EXTENSION);
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                DOCUMENT.setSource(selectedFile);
            } else {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Selected file is null {0}", selectedFile);
            }
        } else {
            Logger.getLogger(getClass().getName()).log(Level.FINER, "Using existing file path {0}", DOCUMENT.source);
        }
        if (DOCUMENT.sourceSetAndValid()) {
            DOCUMENT.saveDocument();
            Logger.getLogger(getClass().getName()).log(Level.FINEST, "Document saved");
        } else {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not get a good document path {0}", DOCUMENT.source);
        }
    }

    /**
     * Handles loading of files. It creates a loading dialog by default, and
     * then calls the loadDocument() method of the XalFxDocument class.
     */
    protected void loadFileMenuHandler() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Application State");
        //fileChooser.setInitialFileName(document.DEFAULT_FILENAME);

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(DOCUMENT.FILETYPE_DESCRIPTION + " (" + DOCUMENT.WILDCARD_FILE_EXTENSION + ")", DOCUMENT.WILDCARD_FILE_EXTENSION);
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "No file selected for loading");
        } else {
            if (selectedFile.exists() && selectedFile.canRead()) {
                DOCUMENT.setSource(selectedFile);
                DOCUMENT.loadDocument(DOCUMENT.source);
            } else {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not open {0}", DOCUMENT.source);
            }
        }
    }

    /**
     * Handles exit the application. Can be overriden to add cleanup routines.
     */
    protected void exitMenuHandler() {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Exit button clicked");
        Platform.exit();
    }

    protected void testModeMenuHandler(Event e) {
        DOCUMENT.testMode = !DOCUMENT.testMode;

        DOCUMENT.accelerator.setTestMode(DOCUMENT.testMode);

        if (DOCUMENT.testMode) {
            ((MenuItem) e.getSource()).setText("Disable Test Mode");
        } else {
            ((MenuItem) e.getSource()).setText("Enable Test Mode");
        }
    }

    protected void loadDefaultAcceleratorMenuHandler() {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Loading default accelerator.");
        setAcceleratorWithPath(XMLDataManager.defaultPath());
    }

    protected void setAcceleratorWithPath(String acceleratorPath) {
        try {
            XMLDataManager acceleratorXMLManager = XMLDataManager.managerWithFilePath(acceleratorPath);
            Accelerator accelerator = acceleratorXMLManager.getAccelerator();
            DOCUMENT.acceleratorXMLManager = acceleratorXMLManager;
            DOCUMENT.accelerator.setAccelerator(accelerator);
        } catch (ParseException | ClassCastException ex) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Load Accelerator Warning");
            if (ex instanceof ParseException) {
                alert.setHeaderText("Invalid file selected");
            } else if (ex instanceof ClassCastException) {
                alert.setHeaderText("File not compatible with this Open XAL version");
            }
            alert.setContentText("How to proceed?");

            ButtonType buttonTypeLoad = new ButtonType("Load Accelerator");
            ButtonType buttonTypeLoadDefault = new ButtonType("Load Default Accelerator");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeLoad, buttonTypeLoadDefault, buttonTypeCancel);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == buttonTypeLoad) {
                loadAcceleratorMenuHandler();
            } else if (result.get() == buttonTypeLoadDefault) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Loading default accelerator.");
                setAcceleratorWithPath(XMLDataManager.defaultPath());
            } else {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "No accelerator selected.");
            }
        }
    }

    protected void loadAcceleratorMenuHandler() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Accelerator");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XAL files (*.xal)", "*.xal");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show open file dialog
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Loading accelerator from file.");
            setAcceleratorWithPath(selectedFile.getAbsolutePath());
        } else {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "No accelerator selected.");
        }
    }

    protected void selectSequenceMenuHandler(Event t) {
        final RadioMenuItem getSeqName = (RadioMenuItem) t.getSource();
        DOCUMENT.setSequence(getSeqName.getText());
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Sequence Selected: {0}", DOCUMENT.getSequence());
    }

    protected void addComboHandler(ToggleGroup groupSequence, Event t) {
        Stage stage;
        Parent root;
        URL url = null;
        String sceneFile = "/xal/extension/fxapplication/CreateComboSequence.fxml";
        try {
            stage = new Stage();
            url = getClass().getResource(sceneFile);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FxApplication.class.getResource(sceneFile));
            root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Create a Combo Sequence");
            stage.initModality(Modality.APPLICATION_MODAL);
            CreateComboSequenceController loginController = loader.getController();
            loginController.setProperties(DOCUMENT.accelerator.getAccelerator());
            loginController.loggedInProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasLoggedIn, Boolean isNowLoggedIn) -> {
                if (isNowLoggedIn) {
                    if (loginController.getComboName() != null) {
                        AcceleratorSeqCombo comboSequence = new AcceleratorSeqCombo(loginController.getComboName(), loginController.getNewComboSequence());
                        MenuItem addComboMenu = (MenuItem) t.getSource();
                        RadioMenuItem addedItem = new RadioMenuItem(loginController.getComboName());
                        addedItem.setOnAction((e) -> selectSequenceMenuHandler(e));
                        addedItem.setToggleGroup(groupSequence);
                        groupSequence.selectToggle(addedItem);
                        DOCUMENT.setSequence(loginController.getComboName());
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "Sequence Selected: {0}", DOCUMENT.getSequence());
                        int index = addComboMenu.getParentMenu().getItems().size() - 2;
                        addComboMenu.getParentMenu().getItems().add(index, addedItem);
                    }
                    stage.close();
                }
            });
            stage.showAndWait();
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Exception on FXMLLoader.load()");
            LOGGER.log(Level.INFO, "  * url: " + url);
            LOGGER.log(Level.INFO, "  * " + ex);
            LOGGER.log(Level.INFO, "    ----------------------------------------\n");
        }
    }

    protected void eLogMenuHandler(String docType) {
        DOCUMENT.eLogPost(docType);
    }

    protected void helpMenuHandler() {
        DOCUMENT.help();
    }

    protected void aboutMenuHandler() {
        String aboutFile = "/About.properties";
        try {
            String oxalVersionFile = "/oxal_version.properties";
            String oxalVersion;

            Properties properties = new Properties();
            InputStream propertyStream = getClass().getResourceAsStream(oxalVersionFile);
            properties.load(propertyStream);

            try {
                oxalVersion = properties.getProperty("version");
                LOGGER.log(Level.INFO, "OXAL version = " + oxalVersion);
            } finally {
                propertyStream.close();
            }

            String applicationName;
            String version;
            String date;
            String authors;
            String organization;
            String description;

            properties = new Properties();
            propertyStream = getClass().getResourceAsStream(aboutFile);
            properties.load(propertyStream);

            try {
                applicationName = properties.getProperty("name");
                version = properties.getProperty("version");
                date = properties.getProperty("date");
                authors = properties.getProperty("authors");
                organization = properties.getProperty("organization");
                description = properties.getProperty("description");
            } finally {
                propertyStream.close();
            }

            Dialog dialog = new Dialog();
            dialog.setTitle("About");
            dialog.setHeaderText(applicationName);

            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Create the username and password labels and fields.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            grid.add(new Label("Version:"), 0, 0);
            grid.add(new Label(version), 1, 0);
            grid.add(new Label("Date:"), 0, 1);
            grid.add(new Label(date), 1, 1);
            grid.add(new Label("Author(s):"), 0, 2);
            grid.add(new Label(authors), 1, 2);
            grid.add(new Label("Organization:"), 0, 3);
            grid.add(new Label(organization), 1, 3);
            grid.add(new Label("Description:"), 0, 4);
            grid.add(new Label(description), 1, 4);
            grid.add(new Label("Developed using:"), 0, 5);
            grid.add(new Label("Open XAL FxApplication framework"), 1, 5);
            grid.add(new Label("Running on:"), 0, 6);
            grid.add(new Label("Open XAL version " + oxalVersion), 1, 6);

            dialog.getDialogPane().setContent(grid);

            dialog.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void urlMenuHandler() {
        DOCUMENT.openUrl(ElogServer.getElogURL());
    }
}
