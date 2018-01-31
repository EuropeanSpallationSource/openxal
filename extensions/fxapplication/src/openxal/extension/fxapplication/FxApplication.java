/*
 * JavaFX Application abstract class
 *
 * Created on January 19, 2018
 */

package openxal.extension.fxapplication;


import java.io.IOException;
import javafx.application.Application;
import java.net.URL;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuBar;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
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
            newFileMenu.setOnAction(new NewFileMenu());
            final MenuItem saveFileMenu = new MenuItem("Save");
            saveFileMenu.setOnAction(new SaveFileMenu(false));
            final MenuItem saveAsFileMenu = new MenuItem("Save as..");
            saveAsFileMenu.setOnAction(new SaveFileMenu(true));
            final MenuItem loadFileMenu = new MenuItem("Load");
            loadFileMenu.setOnAction(new LoadFileMenu());
            fileMenu.getItems().addAll( newFileMenu, saveFileMenu, saveAsFileMenu, loadFileMenu);
        }

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
        stage.show();
    }

}
class NewFileMenu implements EventHandler {

    @Override
    public void handle(Event t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
class SaveFileMenu implements EventHandler {
    private boolean saveAs;
    public SaveFileMenu(boolean saveAs) {
        this.saveAs = saveAs;
    }
    @Override
    public void handle(Event t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

class LoadFileMenu implements EventHandler {

    @Override
    public void handle(Event t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}