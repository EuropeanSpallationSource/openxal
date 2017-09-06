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
package xal.app.configurator;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import xal.extension.tracewinimporter.TraceWin;

/**
 * Class to import a TraceWin input file to Open XAL format.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class TraceWinImporter {

    public static void importTW(String input, String outputDir, ChoiceBox<?> initialParametersChoiceBox) {
        TraceWin traceWin = new TraceWin();
        JavaFXLogger logger = new JavaFXLogger();
        traceWin.setLogger(logger);

        traceWin.setOutputDir(outputDir);
        traceWin.setOutputName("main");
       
        File fileInput = new File(input);
        try {
            if (fileInput.isFile()) {                
                if (initialParametersChoiceBox.getSelectionModel().getSelectedIndex()>1){
                    logger.log("Input parameters source not valid for this input type, changing to default.");
                    initialParametersChoiceBox.getSelectionModel().select(0);
                }
                traceWin.setInputFile(fileInput);

                inputParametersWindow(traceWin);
            } else if (fileInput.isDirectory()) {               
                if (initialParametersChoiceBox.getSelectionModel().getSelectedIndex()==1){
                    logger.log("Input parameters source not valid for this input type, changing to default.");
                    initialParametersChoiceBox.getSelectionModel().select(0);
                }
                traceWin.setInputDir(fileInput);
            } else if (input.startsWith("http")) {               
                if (initialParametersChoiceBox.getSelectionModel().getSelectedIndex()==1){
                    logger.log("Input parameters source not valid for this input type, changing to default.");
                    initialParametersChoiceBox.getSelectionModel().select(0);
                }
                traceWin.setInputGit(input);
            } else {
                throw new IOException();
            }
        } catch (IOException e1) {
            System.err.println("Error while trying to read input.");
            System.exit(1);
        }

        traceWin.setInitialParametersMode(initialParametersChoiceBox.getSelectionModel().getSelectedIndex());

        Task importTW = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                traceWin.importTW();
                return null;
            }
        };

        // Run the import method on a new thread to prevent the GUI from freezing
        Thread th = new Thread(importTW);
        th.start();
    }

    private static void inputParametersWindow(TraceWin traceWin) {
        FXMLLoader fxmlLoader = new FXMLLoader(FXMLController.class.getResource("/fxml/InputParametersWindow.fxml"));
        try {
            Parent root = (Parent) fxmlLoader.load();

            InputParametersWindow controller = fxmlLoader.<InputParametersWindow>getController();
            controller.setTraceWin(traceWin);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Input parameters");
            stage.setScene(new Scene(root));

            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Open a file dialog to select the TraceWin input file.
     *
     * @param scene Owner scene object, null if none
     * @return TraceWin file path
     */
    public static String openFileDialog(Scene scene) {
        String tracewinFilePath = null;

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Select TraceWin input File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TraceWin Input Files", "*.dat"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File choice = null;
        if (scene != null) {
            Window window = scene.getWindow();
            if (window != null) {
                choice = chooser.showOpenDialog(window);
            }
        }
        if (choice != null && choice.exists() && choice.isFile() && choice.canRead()) {
            tracewinFilePath = choice.getPath();
        }

        return tracewinFilePath;

    }

    /**
     * Open a file dialog to select the Open XAL SMF output file.
     *
     * @param scene
     * @param title
     * @param path
     * @return Open XAL SMF file path
     */
    public static String dirDialog(Scene scene, String title, String path) {
        String openXALFilePath = null;

        DirectoryChooser chooser = new DirectoryChooser();

        chooser.setTitle(title);

        if (path != null) {
            chooser.setInitialDirectory(new File(path));
        }

        File choice = null;

        if (scene != null) {
            Window window = scene.getWindow();
            if (window != null) {
                choice = chooser.showDialog(window);
            }
        }
        if (choice != null) {
            openXALFilePath = choice.getPath();
        }

        return openXALFilePath;
    }
}
