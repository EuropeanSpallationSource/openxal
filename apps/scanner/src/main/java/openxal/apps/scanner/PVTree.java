/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openxal.apps.scanner;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;

/**
 * FXML Controller class
 *
 * @author yngvelevinsen
 */
public class PVTree extends SplitPane {
    public PVTree() {
        FXMLLoader fxmlLoader = new FXMLLoader(

        getClass().getResource("/widgets/PVTree.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
           fxmlLoader.load();
        } catch (IOException exception) {
           throw new RuntimeException(exception);
        }
    }
}
