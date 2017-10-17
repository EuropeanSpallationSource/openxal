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
package xal.extension.jelog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author nataliamilas
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class PostEntryController implements Initializable {

    @FXML
    private TextField textAuthor;
    @FXML
    private ComboBox<String> comboBoxType;
    @FXML
    private RadioButton rbimageYes;
    @FXML
    private ToggleGroup groupImage;
    @FXML
    private RadioButton rbimageNo;
    @FXML
    private TextField textSubject;
    @FXML
    private TextArea textBody;
    @FXML
    private Button buttonCancel;
    @FXML
    private Button buttonSubmit;
    @FXML
    private ComboBox<String> comboBoxCategory;
    @FXML
    private ComboBox<String> comboBoxLogBook;

    private String elogServer = "http://elog.esss.lu.se/";

    private WritableImage snapshot = null;

    public void setAuthor(String author) {
        textAuthor.setText(author);
    }

    public void setSnapshot(WritableImage snapshot) {
        this.snapshot = snapshot;
    }

    public void setElogServer(String elogServer) {
        this.elogServer = elogServer;
        updateLogbook();
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        updateLogbook();
    }

    /**
     * Updates the logbook, categories and types lists.
     */
    private void updateLogbook() {
        List<String> logbooks = null;
        try {
            logbooks = Jelog.getLogbooks(elogServer);
        } catch (IOException ex) {
            Logger.getLogger(PostEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RuntimeException ex) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("The logbook server is not reachable!");
            alert.showAndWait();
        }

        comboBoxLogBook.setItems(FXCollections.observableArrayList(logbooks));

        if (logbooks.isEmpty()) {
            comboBoxType.setDisable(true);
            comboBoxCategory.setDisable(true);
        } else {
            comboBoxLogBook.getSelectionModel().selectFirst();
            comboBoxLogBookHandler(null);
        }
    }

    @FXML
    private void handleButtonCancel() {
        Stage stage = (Stage) buttonCancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleButtonSubmit(ActionEvent event) {
        if (textAuthor.getText().isEmpty() || textSubject.getText().isEmpty()
                || textBody.getText().isEmpty() || comboBoxCategory.getValue().equals(-1)
                || comboBoxType.getValue().equals(-1) || comboBoxLogBook.getValue().equals(-1)) {

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all the fields before submitting the new entry.");
            alert.showAndWait();
        } else {
            try {
                Jelog.submit(textAuthor.getText(), textSubject.getText(), textBody.getText(),
                        comboBoxCategory.getValue(), comboBoxType.getValue(), null, snapshot,
                        comboBoxLogBook.getValue(), elogServer);
            } catch (IOException ex) {
                Logger.getLogger(PostEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }

            Stage stage = (Stage) buttonSubmit.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Fills in the categories and types combo boxes for the selected logbook.
     * 
     * @param event 
     */
    @FXML
    private void comboBoxLogBookHandler(ActionEvent event) {
        if (!comboBoxLogBook.getSelectionModel().isEmpty()) {
            String logbook = comboBoxLogBook.getSelectionModel().getSelectedItem().replaceAll(" ", "+");
            try {
                logbook = new URL(new URL(elogServer).toExternalForm() + logbook).toString();
            } catch (MalformedURLException ex) {
                Logger.getLogger(PostEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
            String[] categories = Jelog.getCategories(logbook);
            String[] types = Jelog.getTypes(logbook);

            comboBoxCategory.setItems(FXCollections.observableArrayList(categories));
            comboBoxType.setItems(FXCollections.observableArrayList(types));

            comboBoxCategory.getSelectionModel().selectFirst();
            comboBoxType.getSelectionModel().selectFirst();

            comboBoxCategory.setDisable(false);
            comboBoxType.setDisable(false);
        }
    }
}
