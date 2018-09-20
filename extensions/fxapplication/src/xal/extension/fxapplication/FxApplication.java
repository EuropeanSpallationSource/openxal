/*
 * JavaFX Application abstract class
 *
 * Created on January 19, 2018
 */

package xal.extension.fxapplication;

import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import java.net.URL;
import java.util.Date;
import javafx.beans.property.SimpleStringProperty;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuBar;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import xal.extension.application.ApplicationStatus;
import xal.extension.service.ServiceDirectory;
import xal.extension.service.ServiceException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.data.XMLDataManager;

/**
 * The Application class handles defines the core of an application. It is often
 * the first handler of application wide events and typically forwards those
 * events to the custom application adaptor for further processing. Every
 * application has exactly one instance of this class.
 *
 * For now the FxApplication does nothing (except inheriting all from
 * Application)
 *
 * @author Yngve Levinsen <yngve.levinsen@esss.se>
 */
abstract public class FxApplication extends Application {

    protected String MAIN_SCENE = "/fxml/Scene.fxml";
    protected String CSS_STYLE = "/styles/Styles.css";
    private String STAGE_TITLE = "Demo Application";

    protected XalFxDocument DOCUMENT;

    final private Date LAUNCH_TIME;

    // Set to false if this application doesn't save/load xml files
    protected boolean HAS_DOCUMENTS = true;

    // Set to false if this application doesn't need the machine sequences
    protected boolean HAS_SEQUENCE = true;

    protected MenuBar MENU_BAR;

    private static Stage stage; // **Declare static Stage**

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
        this.stage = stage;
    }

    static public Stage getStage() {
        return stage;
    }

    public String getApplicationName() {
        return STAGE_TITLE;
    }

    public void setApplicationName(String applicationName) {
        this.STAGE_TITLE = applicationName;
    }

    // Call this before start() (so that you can add items to MENU_BAR etc after)
    protected void initialize() {

        Logger.getLogger(FxApplication.class.getName()).log(Level.INFO, "Loading default accelerator {0}", XMLDataManager.defaultPath());
        DOCUMENT.accelerator.setAccelerator(XMLDataManager.loadDefaultAccelerator());

        MENU_BAR = new MenuBar();

        Menu fileMenu = new Menu("File");
        if (HAS_DOCUMENTS) {
            MenuItem newFileMenu = new MenuItem("New");
            newFileMenu.setOnAction(new NewFileMenu(DOCUMENT));
            final MenuItem saveFileMenu = new MenuItem("Save");
            saveFileMenu.setOnAction(new SaveFileMenu(DOCUMENT, false));
            final MenuItem saveAsFileMenu = new MenuItem("Save as..");
            saveAsFileMenu.setOnAction(new SaveFileMenu(DOCUMENT, true));
            final MenuItem loadFileMenu = new MenuItem("Load");
            loadFileMenu.setOnAction(new LoadFileMenu(DOCUMENT));
            fileMenu.getItems().addAll(newFileMenu, saveFileMenu, saveAsFileMenu, loadFileMenu);
        }
        final MenuItem exitMenu = new MenuItem("Exit");
        exitMenu.setOnAction(new ExitMenu());
        fileMenu.getItems().addAll(exitMenu);

        final Menu editMenu = new Menu("Edit");

        final Menu acceleratorMenu = new Menu("Accelerator");
        final MenuItem loadDefaultAcceleratorMenu = new MenuItem("Load Default Accelerator");
        loadDefaultAcceleratorMenu.setOnAction(new LoadDefaultAcceleratorMenu(DOCUMENT));
        final MenuItem loadAcceleratorMenu = new MenuItem("Load Accelerator ...");
        loadAcceleratorMenu.setOnAction(new LoadAcceleratorMenu(DOCUMENT));
        acceleratorMenu.getItems().addAll(loadDefaultAcceleratorMenu, loadAcceleratorMenu);
        final Menu sequenceMenu = new Menu("Sequence");
        final ToggleGroup groupSequence = new ToggleGroup();

        if (HAS_SEQUENCE && DOCUMENT.accelerator.getAccelerator() != null) {
            buildSequenceMenu(DOCUMENT.accelerator.getAccelerator(), sequenceMenu, groupSequence);
            acceleratorMenu.getItems().addAll(new SeparatorMenuItem(), sequenceMenu);
            final MenuItem addCombo = new MenuItem("Add new Combo Sequence");
            addCombo.setOnAction(new AddCombo(DOCUMENT, groupSequence));
            sequenceMenu.getItems().add(addCombo);
        }

        final Menu eLogMenu = new Menu("eLog");
        final MenuItem openLogMenu = new MenuItem("Open");
        openLogMenu.setOnAction(new UrlMenu(DOCUMENT));
        final MenuItem makePostMenu = new MenuItem("Post");
        makePostMenu.setOnAction(new ELogMenu(DOCUMENT));
        eLogMenu.getItems().addAll(openLogMenu, makePostMenu);

        final Menu helpMenu = new Menu("Help");
        final MenuItem aboutMenu = new MenuItem("About");
        aboutMenu.setOnAction(new HelpMenu(DOCUMENT));
        helpMenu.getItems().add(aboutMenu);

        MENU_BAR.getMenus().addAll(fileMenu, editMenu, acceleratorMenu, eLogMenu, helpMenu);

        DOCUMENT.accelerator.addChangeListener((ChangeListener) (ObservableValue o, Object oldVal, Object newVal) -> {
            if (HAS_SEQUENCE && DOCUMENT.accelerator.getAccelerator() != null) {
                int menu_num = sequenceMenu.getItems().size() - 1;
                sequenceMenu.getItems().remove(0, menu_num);
                groupSequence.getToggles().clear();
                buildSequenceMenu(DOCUMENT.accelerator.getAccelerator(), sequenceMenu, groupSequence);
                Logger.getLogger(FxApplication.class.getName()).log(Level.INFO, "Rebuilding Sequence Menu.");
            }
        });

        registerApplicationStatusService();

    }

    @Override
    public void start(Stage stage) throws IOException {
        setStage(stage);

        VBox root = new VBox();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_SCENE));

        root.getChildren().add(MENU_BAR);
        root.getChildren().add(loader.load());

        Scene scene = new Scene(root);
        scene.getStylesheets().add(CSS_STYLE);

        stage.getProperties().put("hostServices", this.getHostServices());

        stage.setTitle(STAGE_TITLE);
        stage.setScene(scene);
        //YIL It is probably very bad to set this here but I am a stupid person.
        DOCUMENT.sourceString = new SimpleStringProperty(DOCUMENT.DEFAULT_FILENAME);
        DOCUMENT.sourceString.addListener((observable, oldValue, newValue) -> stage.setTitle(STAGE_TITLE + ": " + newValue));
        stage.show();
    }

    public void buildSequenceMenu(Accelerator accelerator, Menu sequenceMenu, ToggleGroup groupSequence) {
        //Populate the Sequence Menu with the sequences of the machine
        List<AcceleratorSeq> seqItem = accelerator.getSequences();
        int k = 0;

        for (AcceleratorSeq item : seqItem) { //AddSequences
            RadioMenuItem addedItem = new RadioMenuItem(item.toString());
            sequenceMenu.getItems().add(k, addedItem);
            addedItem.setToggleGroup(groupSequence);
            addedItem.setOnAction(new SelectSequenceMenu(DOCUMENT));
            k++;
        }

        sequenceMenu.getItems().add(k, new SeparatorMenuItem());

        List<AcceleratorSeqCombo> seqCombo = accelerator.getComboSequences();
        k++;
        for (AcceleratorSeqCombo item : seqCombo) { //AddCombos
            RadioMenuItem addedItem = new RadioMenuItem(item.toString());
            sequenceMenu.getItems().add(k, addedItem);
            addedItem.setToggleGroup(groupSequence);
            addedItem.setOnAction(new SelectSequenceMenu(DOCUMENT));
            k++;
        }

        sequenceMenu.getItems().add(k, new SeparatorMenuItem());

    }

    /**
     * Register the application status service so clients on the network can
     * query the status of this application instance.
     */
    final protected void registerApplicationStatusService() {
        // check to see if the startup flag has disabled application services
        Boolean shouldRegister = Boolean.valueOf(System.getProperty("registerApplicationService", "true"));

        if (shouldRegister.booleanValue()) {
            try {
                ServiceDirectory.defaultDirectory().registerService(ApplicationStatus.class, STAGE_TITLE, new FxApplicationStatusService(this));
                System.out.println("Registered application services...");
                Logger.getLogger("xal.extension.fxapplication").log(Level.INFO, "Registered application services...");
            } catch (ServiceException exception) {
                System.err.println("Service registration failed due to " + exception);
                Logger.getLogger("xal.extension.fxapplication").log(Level.SEVERE, "Service registration failed...", exception);
            }
        } else {
            Logger.getLogger("global").log(Level.CONFIG, "Application services disabled.");
            System.out.println("Application services not registerd because of startup flag...");
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

}

abstract class FileMenuItem implements EventHandler {

    protected XalFxDocument document;

    public FileMenuItem(XalFxDocument document) {
        this.document = document;
    }

    @Override
    public void handle(Event t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

class NewFileMenu extends FileMenuItem {

    public NewFileMenu(XalFxDocument document) {
        super(document);
    }

    @Override
    public void handle(Event t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

class SaveFileMenu extends FileMenuItem {

    private final boolean saveAs;

    public SaveFileMenu(XalFxDocument document, boolean saveAs) {
        super(document);
        this.saveAs = saveAs;
    }

    @Override
    public void handle(Event t) {
        if (saveAs || !document.sourceSetAndValid()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Application State");
            fileChooser.setInitialFileName(document.DEFAULT_FILENAME);

            //Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(document.FILETYPE_DESCRIPTION + " (" + document.WILDCARD_FILE_EXTENSION + ")", document.WILDCARD_FILE_EXTENSION);
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                document.setSource(selectedFile);
            } else {
                Logger.getLogger(SaveFileMenu.class.getName()).log(Level.WARNING, "Selected file is null {0}", selectedFile);
            }
        } else {
            Logger.getLogger(SaveFileMenu.class.getName()).log(Level.FINER, "Using existing file path {0}", document.source);
        }
        if (document.sourceSetAndValid()) {
            document.saveDocument();
            Logger.getLogger(SaveFileMenu.class.getName()).log(Level.FINEST, "Document saved");
        } else {
            Logger.getLogger(SaveFileMenu.class.getName()).log(Level.SEVERE, "Could not get a good document path {0}", document.source);
        }
        //saveDocumentAs( final URL url )
    }

}

class LoadFileMenu extends FileMenuItem {

    public LoadFileMenu(XalFxDocument document) {
        super(document);
    }

    @Override
    public void handle(Event t) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Application State");
        //fileChooser.setInitialFileName(document.DEFAULT_FILENAME);

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(document.FILETYPE_DESCRIPTION + " (" + document.WILDCARD_FILE_EXTENSION + ")", document.WILDCARD_FILE_EXTENSION);
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) {
            Logger.getLogger(LoadFileMenu.class.getName()).log(Level.INFO, "No file selected for loading");
        } else {
            if (selectedFile.exists() && selectedFile.canRead()) {
                document.setSource(selectedFile);
                document.loadDocument(document.source);
            } else {
                Logger.getLogger(LoadFileMenu.class.getName()).log(Level.SEVERE, "Could not open {0}", document.source);
            }
        }
    }

}

class ExitMenu implements EventHandler {

    @Override
    public void handle(Event t) {
        Logger.getLogger(ExitMenu.class.getName()).log(Level.INFO, "Exit button clicked");
        System.exit(0);
    }
}

class LoadDefaultAcceleratorMenu implements EventHandler {

    private final XalFxDocument document;

    public LoadDefaultAcceleratorMenu(XalFxDocument document) {
        this.document = document;
    }

    @Override
    public void handle(Event t) {
        Logger.getLogger(LoadDefaultAcceleratorMenu.class.getName()).log(Level.INFO, "Loading default accelerator.");
        document.accelerator.setAccelerator(XMLDataManager.loadDefaultAccelerator());
    }

}

class LoadAcceleratorMenu implements EventHandler {

    private final XalFxDocument document;

    public LoadAcceleratorMenu(XalFxDocument DOCUMENT) {
        this.document = DOCUMENT;
    }

    @Override
    public void handle(Event t) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Accelerator");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XAL files (*.xal)", "*.xal");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            Logger.getLogger(LoadAcceleratorMenu.class.getName()).log(Level.INFO, "Loading accelerator from file.");
            document.accelerator.setAccelerator(XMLDataManager.acceleratorWithPath(selectedFile.getAbsolutePath()));
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Load Accelerator Warning");
            alert.setHeaderText("Empty or invalid file selected");
            alert.setContentText("How to proceed?");

            ButtonType buttonTypeLoad = new ButtonType("Load Default Accelerator");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeLoad, buttonTypeCancel);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == buttonTypeLoad) {
                Logger.getLogger(LoadAcceleratorMenu.class.getName()).log(Level.INFO, "Loading default accelerator.");
                document.accelerator.setAccelerator(XMLDataManager.loadDefaultAccelerator());
            } else {
                Logger.getLogger(LoadAcceleratorMenu.class.getName()).log(Level.INFO, "No accelerator selected.");
                document.accelerator.setAccelerator(null);
            }
        }
    }

}

class SelectSequenceMenu implements EventHandler {

    protected final XalFxDocument document;

    /*
    * CONTRUCTOR
     */
    public SelectSequenceMenu(XalFxDocument DOCUMENT) {
        this.document = DOCUMENT;
    }

    @Override
    public void handle(Event t) {
        final RadioMenuItem getSeqName = (RadioMenuItem) t.getSource();
        document.setSequence(getSeqName.getText());
        Logger.getLogger(FxApplication.class.getName()).log(Level.INFO, "Sequence Selected: {0}", document.getSequence());
    }
}

class AddCombo implements EventHandler {

    protected final XalFxDocument document;
    protected final ToggleGroup groupSequence;

    /*
    * CONTRUCTOR
     */
    public AddCombo(XalFxDocument DOCUMENT, ToggleGroup groupSequence) {
        this.document = DOCUMENT;
        this.groupSequence = groupSequence;
    }

    @Override
    public void handle(Event t) {

        Stage stage;
        Parent root;
        URL url = null;
        String sceneFile = "/xal/extension/fxapplication/resources/CreateComboSequence.fxml";
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
            loginController.setProperties(document.accelerator.getAccelerator());
            loginController.loggedInProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasLoggedIn, Boolean isNowLoggedIn) -> {
                if (isNowLoggedIn) {
                    if (loginController.getComboName() != null) {
                        AcceleratorSeqCombo comboSequence = new AcceleratorSeqCombo(loginController.getComboName(), loginController.getNewComboSequence());
                        MenuItem addComboMenu = (MenuItem) t.getSource();
                        RadioMenuItem addedItem = new RadioMenuItem(loginController.getComboName());
                        addedItem.setOnAction(new SelectSequenceMenu(document));
                        addedItem.setToggleGroup(groupSequence);
                        groupSequence.selectToggle(addedItem);
                        document.setSequence(loginController.getComboName());
                        Logger.getLogger(AddCombo.class.getName()).log(Level.INFO, "Sequence Selected: {0}", document.getSequence());
                        int index = addComboMenu.getParentMenu().getItems().size() - 2;
                        addComboMenu.getParentMenu().getItems().add(index, addedItem);
                    }
                    stage.close();
                }
            });
            stage.showAndWait();
        } catch (IOException ex) {
            System.out.println("Exception on FXMLLoader.load()");
            System.out.println("  * url: " + url);
            System.out.println("  * " + ex);
            System.out.println("    ----------------------------------------\n");
        }
    }

}

class ELogMenu implements EventHandler {

    protected XalFxDocument document;

    public ELogMenu(XalFxDocument document) {
        this.document = document;
    }

    @Override
    public void handle(Event t) {
        document.eLogPost();
    }
}

class HelpMenu implements EventHandler {

    protected XalFxDocument document;

    public HelpMenu(XalFxDocument document) {
        this.document = document;
    }

    @Override
    public void handle(Event t) {
        document.help();
    }
}

class UrlMenu implements EventHandler {

    protected XalFxDocument document;

    public UrlMenu(XalFxDocument document) {
        this.document = document;
    }

    @Override
    public void handle(Event t) {
        document.openUrl(xal.extension.jelog.ElogServer.getElogURL());
    }
}
