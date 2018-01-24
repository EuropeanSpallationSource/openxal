/*
 * Copyright (C) 2017 European Spallation Source ERIC.
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

package xal.app.modelbrowser;


import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static javafx.application.Application.launch;


public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    public static void main( String[] args ) {

        if ( args.length > 0 ) {
            Model.setModelFile(args[0]);
        }

        launch(args);

    }

    @Override
    public void start( Stage stage ) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));

        root.getStyleClass().add("windows-document-decoration");

        Scene scene = new Scene(root);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo-32.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo-64.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo-128.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo-256.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo-512.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo-1024.png")));

        stage.setOnCloseRequest(e -> System.exit(0));

        stage.setTitle("Open XAL Model Browser");
        stage.setScene(scene);
        stage.show();

    }

}
