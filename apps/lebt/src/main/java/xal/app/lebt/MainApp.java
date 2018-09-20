package xal.app.lebt;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.application.Application.launch;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import xal.extension.fxapplication.FxApplication;

public class MainApp extends FxApplication {

    @Override
    public void start(Stage stage) throws IOException {

        MAIN_SCENE = "/fxml/LEBTScene.fxml";
        CSS_STYLE = "/styles/Styles.css";
        setApplicationName("LEBT Commissioning Application");
        HAS_DOCUMENTS= false;
        HAS_SEQUENCE = true;
        DOCUMENT = new LEBTDocument(stage);

        MainFunctions.initialize((LEBTDocument)DOCUMENT);

        super.initialize();

        Menu modelMenu = new Menu("Model");
        ToggleGroup modelGroup = new ToggleGroup();
        RadioMenuItem modelDesignMenu = new RadioMenuItem("DESIGN");
        modelDesignMenu.setOnAction(new ModelMenu((LEBTDocument) DOCUMENT, modelGroup));
        modelMenu.getItems().add(modelDesignMenu );
        modelGroup.getToggles().add(modelDesignMenu);
        RadioMenuItem modelLiveMenu =new RadioMenuItem("LIVE");
        modelLiveMenu.setOnAction(new ModelMenu((LEBTDocument) DOCUMENT,modelGroup));
        modelMenu.getItems().add(modelLiveMenu);
        modelGroup.getToggles().add(modelLiveMenu);
        modelGroup.selectToggle(modelLiveMenu);
        MENU_BAR.getMenus().add(MENU_BAR.getMenus().size()-2, modelMenu);


        super.start(stage);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}

class ModelMenu implements EventHandler {

    protected LEBTDocument document;
    protected ToggleGroup modelGroup;

    ModelMenu(LEBTDocument document, ToggleGroup modelGroup){
        this.document = document;
        this.modelGroup = modelGroup;
    }

    @Override
    public void handle(Event t) {
        RadioMenuItem menu = (RadioMenuItem) modelGroup.getSelectedToggle();
        document.setModel(menu.getText());
        Logger.getLogger(LEBTDocument.class.getName()).log(Level.FINER, "Selected Model {0}",document.getModel().toString());
    }
}
