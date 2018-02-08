/*
 * JavaFX Application abstract class
 *
 * Created on January 19, 2018
 */

package xal.extension.fxapplication;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javafx.application.Application;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuBar;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


/**
 * The Application class handles defines the core of an application.  It is often
 * the first handler of application wide events and typically forwards those events
 * to the custom application adaptor for further processing.  Every application has
 * exactly one instance of this class.
 *
 * For now the FxApplication does nothing (except inheriting all from Application)
 *
 * @author Yngve Levinsen <yngve.levinsen@esss.se>
 */
abstract public class FxApplication extends Application {


    protected String MAIN_SCENE = "/fxml/Scene.fxml";
    protected String CSS_STYLE = "/styles/Styles.css";
    protected String STAGE_TITLE = "Demo Application";
    protected XalFxDocument DOCUMENT;

    // Set to false if this application doesn't save/load xml files
    protected boolean HAS_DOCUMENTS = true;

    protected MenuBar MENU_BAR;

    /**
     * Application constructor.
     * @param adaptor The application adaptor used for customization.
     */
    protected FxApplication( ) {
        this( new URL[]{} );
    }

    /**
     * Application constructor.
     * @param urls An array of document URLs to open upon startup.
     */
    protected FxApplication( final URL[] urls ) {
        super();
    }

    // Call this before start() (so that you can add items to MENU_BAR etc after)
    protected void initialize() {

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
            fileMenu.getItems().addAll( newFileMenu, saveFileMenu, saveAsFileMenu, loadFileMenu);
        }
        final MenuItem exitMenu = new MenuItem("Exit");
        exitMenu.setOnAction(new ExitMenu());
        fileMenu.getItems().addAll( exitMenu);


        final Menu editMenu = new Menu("Edit");

        final Menu accMenu = new Menu("Accelerator");

        MENU_BAR.getMenus().addAll( fileMenu, editMenu, accMenu);

    }

    @Override
    public void start(Stage stage) throws IOException {

        VBox root = new VBox();

        //final Group root = new Group();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_SCENE));

        root.getChildren().add(MENU_BAR);
        root.getChildren().add(loader.load());
        //Parent root = FXMLLoader.load(getClass().getResource(MAIN_SCENE));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(CSS_STYLE);

        stage.getProperties().put("hostServices", this.getHostServices());

        stage.setTitle(STAGE_TITLE);
        stage.setScene(scene);
        //YIL It is probably very bad to set this here but I am a stupid person.
        DOCUMENT.sourceString = new SimpleStringProperty(DOCUMENT.DEFAULT_FILENAME);
        DOCUMENT.sourceString.addListener((observable, oldValue, newValue) -> stage.setTitle(STAGE_TITLE+": "+newValue));
        stage.show();
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
    private boolean saveAs;

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
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(document.FILETYPE_DESCRIPTION+" ("+document.WILDCARD_FILE_EXTENSION+")", document.WILDCARD_FILE_EXTENSION);
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                document.setSource(selectedFile);
            } else {
                Logger.getLogger(SaveFileMenu.class.getName()).log(Level.WARNING, "Selected file is null {0}",selectedFile);
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
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(document.FILETYPE_DESCRIPTION+" ("+document.WILDCARD_FILE_EXTENSION+")", document.WILDCARD_FILE_EXTENSION);
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile.exists() && selectedFile.canRead()) {
            document.setSource(selectedFile);
            document.loadDocument(document.source);
        }
        else
            Logger.getLogger(LoadFileMenu.class.getName()).log(Level.SEVERE, "Could not open {0}", document.source);
    }

}

class ExitMenu implements EventHandler {

    @Override
    public void handle(Event t) {
        Logger.getLogger(ExitMenu.class.getName()).log(Level.INFO, "Exit button clicked");
        System.exit(0);
    }

}