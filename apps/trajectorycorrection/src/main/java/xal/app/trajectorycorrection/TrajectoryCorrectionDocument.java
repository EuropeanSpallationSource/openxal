/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.trajectorycorrection;

import java.net.URL;
import javafx.stage.Stage;
import xal.extension.fxapplication.XalFxDocument;

/**
 *
 * @author nataliamilas
 */
public class TrajectoryCorrectionDocument extends XalFxDocument {

    /* CONSTRUCTOR
     * in order to access to variable from the main class
     */
    public TrajectoryCorrectionDocument(Stage stage) {
        super(stage);
        DEFAULT_FILENAME = "Trajectory.xml";
        WILDCARD_FILE_EXTENSION = "*.xml";
        HELP_PAGEID = "227688944";
    }

    @Override
    public void saveDocumentAs(URL url) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadDocument(URL url) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
