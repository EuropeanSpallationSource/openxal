/*
 * Copyright (c) 2017, Open XAL Collaboration
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package xal.app.scanner;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.application.Application.launch;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;

import xal.extension.fxapplication.FxApplication;

public class MainApp extends FxApplication {

    private SimpleBooleanProperty HDF5_ENABLED;

    @Override
    public void start(Stage stage) throws IOException {

        MAIN_SCENE = "/fxml/ScannerScene.fxml";
        setApplicationName("Scanner Application");
        Logger.getLogger(MainApp.class.getName()).log(Level.WARNING, "Ignoring useDefaultAccelerator: {0} .. ", Boolean.getBoolean("useDefaultAccelerator"));
        DOCUMENT = new ScannerDocument(stage);
        // Initialize some static functions (this is probably bad design..)
        MainFunctions.initialize((ScannerDocument) DOCUMENT);
        HAS_SEQUENCE = false;

        super.initialize();

        // Add specific menu items..
        HDF5_ENABLED = new SimpleBooleanProperty(false);
        HDF5_ENABLED.addListener((observable, oldValue, newValue) -> ((ScannerDocument)DOCUMENT).setUseHDF5(newValue));
        SeparatorMenuItem separator = new SeparatorMenuItem();
        CheckMenuItem useHDF5 = new CheckMenuItem("Use HDF5");
        HDF5_ENABLED.bind(useHDF5.selectedProperty());

        Menu fileMenu = MENU_BAR.getMenus().get(0);
        fileMenu.getItems().add(separator);
        fileMenu.getItems().add(useHDF5);
        MENU_BAR.getMenus().set(0, fileMenu);

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
