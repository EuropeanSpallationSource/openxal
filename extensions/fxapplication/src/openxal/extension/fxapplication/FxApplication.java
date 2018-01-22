/*
 * JavaFX Application abstract class
 *
 * Created on January 19, 2018
 */

package openxal.extension.fxapplication;


import java.io.IOException;
import javafx.application.Application;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(MAIN_SCENE));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(CSS_STYLE);

        stage.setTitle(STAGE_TITLE);
        stage.setScene(scene);
        stage.show();
    }



}
