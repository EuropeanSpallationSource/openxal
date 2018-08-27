/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.trajectorydisplay;

import java.io.File;
import java.net.URL;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import xal.extension.fxapplication.XalFxDocument;

/**
 *
 * @author nataliamilas
 */
public class TrajectoryDisplayDocument extends XalFxDocument {

    public SimpleBooleanProperty liveTrajectory;

    public SimpleStringProperty refTrajectoryFile;

    public SimpleStringProperty displayTrajectoryFile;

    public SimpleStringProperty saveTrajectoryFile;

    public TrajectoryArray Trajectory;

    /* CONSTRUCTOR
     * in order to access to variable from the main class
     */
    public TrajectoryDisplayDocument(Stage stage) {
        super(stage);
        DEFAULT_FILENAME = "Trajectory.xml";
        WILDCARD_FILE_EXTENSION = "*.xml";
        HELP_PAGEID = "227688944";

        liveTrajectory = new SimpleBooleanProperty();
        refTrajectoryFile = new SimpleStringProperty();
        displayTrajectoryFile = new SimpleStringProperty();
        saveTrajectoryFile = new SimpleStringProperty();

        liveTrajectory.setValue(false);
        refTrajectoryFile.setValue(null);
        displayTrajectoryFile.setValue(null);
        saveTrajectoryFile.setValue(null);
    }

    public void saveTrajectory() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Trajectory File");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showSaveDialog(null);

        saveTrajectoryFile.set(selectedFile.toString());

    }

    public void loadRefTrajectory() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Reference Trajectory File");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        refTrajectoryFile.set(selectedFile.toString());

    }

    public void getTrajectoryFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Trajectory File");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        displayTrajectoryFile.set(selectedFile.toString());

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
