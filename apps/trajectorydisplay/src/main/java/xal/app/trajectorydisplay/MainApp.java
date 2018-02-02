/*
 * Copyright (C) 2018 European Spallation Source ERIC
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
package xal.app.trajectorydisplay;

import java.io.IOException;
import static javafx.application.Application.launch;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import xal.extension.fxapplication.FxApplication;

public class MainApp extends FxApplication {

    @Override
    public void start(Stage stage) throws IOException {

        MAIN_SCENE = "/fxml/TrajectoryDisplay.fxml";
        CSS_STYLE = "/styles/Styles.css";
        STAGE_TITLE = "Trajectory Display";
        HAS_DOCUMENTS=false;

        super.initialize();
        Menu trajectoryMenu = new Menu("File");
        MenuItem tMenu = new MenuItem("Trajectory");
        tMenu.setOnAction(new TrajectoryMenu());
        trajectoryMenu.getItems().add(tMenu);
        MENU_BAR.getMenus().add(trajectoryMenu);
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

class TrajectoryMenu implements EventHandler {

    @Override
    public void handle(Event t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}