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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * FXML Controller class
 *
 * @author nataliamilas
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class PostEntryController implements Initializable {

    @FXML
    private TextArea textBody;
    @FXML
    private Button buttonCancel;
    @FXML
    private Button buttonSubmit;
    @FXML
    private ComboBox<String> comboBoxLogBook;

    private String elogServer = "https://logbook.esss.lu.se/";

    private String logbookGroup = null;

    private WritableImage[] snapshots = null;
    @FXML
    private GridPane gridPane;
    @FXML
    private ComboBox<String> comboBoxLogBookGroup;

    private ArrayList<Region> gridControls = null;

    private String author = null;

    private String userName = null;

    private String userPasswordHash = null;

    private Attachment[] attachments = null;
    @FXML
    private Button switchUserButton;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPassword(char[] userPassword) {
        this.userPasswordHash = Sha256.sha256(userPassword, 5000).substring(4);
    }

    public void setUserPasswordHash(String userPasswordHash) {
        this.userPasswordHash = userPasswordHash;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setSnapshots(WritableImage[] snapshots) {
        this.snapshots = snapshots;
    }

    public void setAttachments(Attachment[] attachments) {
        this.attachments = attachments;
    }

    public void setElogServer(String elogServer) {
        this.elogServer = elogServer.endsWith("/") ? elogServer : elogServer + '/';
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
        try {
            Jelog.setTrustAllCerts();
        } catch (Exception ex) {
            Logger.getLogger(PostEntryController.class.getName()).log(Level.SEVERE, null, ex);
        }

        gridControls = new ArrayList();

        updateLogbookGroups();
    }

    public boolean login() throws IOException, Exception {
        AuthenticationPaneFX pane = new AuthenticationPaneFX();
        Dialog dlg = pane.createDialog(null);

        String logbook = comboBoxLogBook.getSelectionModel().getSelectedItem();
        String[] credentials = Jelog.retrieveUsernameAndPassword(new URL(new URL(elogServer), logbook).toString());
        while (credentials[0] == null) {
            Optional<Pair<String, char[]>> result = dlg.showAndWait();
            if (result.isPresent()) {
                Jelog.login(result.get().getKey(), result.get().getValue(), true, elogServer);

                credentials = Jelog.retrieveUsernameAndPassword(new URL(new URL(elogServer), logbook).toString());
            } else {
                return false;
            }
        }

        setAuthor(credentials[0]);
        setUserName(credentials[1]);
        setUserPasswordHash(credentials[2]);

        updateLogbook();

        return true;
    }

    /**
     * Updates the logbook, categories and types lists.
     */
    private void updateLogbookGroups() {
        List<String> logbookgroups = null;
        try {
            logbookgroups = Jelog.getLogbooks(elogServer);
        } catch (IOException ex) {
            Logger.getLogger(PostEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RuntimeException ex) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("The logbook server is not reachable!");
            alert.showAndWait();
        }

        comboBoxLogBookGroup.setItems(FXCollections.observableArrayList(logbookgroups));

        if (logbookgroups.isEmpty()) {
            comboBoxLogBook.setDisable(true);
        } else {
            comboBoxLogBook.setDisable(false);
            comboBoxLogBookGroup.getSelectionModel().selectFirst();
            comboBoxLogBookGroupHandler(null);
        }
    }

    /**
     * Updates the logbook, categories and types lists.
     */
    private void updateLogbook() {
        List<String> logbooks = null;
        try {
            logbooks = Jelog.getLogbooks(elogServer + logbookGroup);
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

        if (!logbooks.isEmpty()) {
            comboBoxLogBook.getSelectionModel().selectFirst();
            comboBoxLogBookHandler(null);
        }
    }

    @FXML
    private void handleButtonCancel() {
        Stage stage = (Stage) buttonCancel.getScene().getWindow();
        stage.close();
    }

    private String getCheckedItems(TilePane pane) {
        List<String> checkedItems = new ArrayList<>();

        for (Node checkBox : pane.getChildren()) {
            if (((CheckBox) checkBox).isSelected()) {
                checkedItems.add(((CheckBox) checkBox).getText());
            }
        }

        String items = "";
        for (String item : checkedItems) {
            items = items.concat(item + " | ");
        }

        if (!items.isEmpty()) {
            items = items.substring(0, items.length() - 3);
        }

        return items.trim();
    }

    @FXML
    private void handleButtonSubmit(ActionEvent event) {

        boolean allrequired = true;
        HashMap<String, String> fields = new HashMap();

        for (int i = 4; i < gridPane.getChildren().size(); i += 2) {
            Label label = (Label) gridPane.getChildren().get(i);

            String option = null;

            if (gridPane.getChildren().get(i + 1).getClass() == TextField.class) {
                option = ((TextField) gridPane.getChildren().get(i + 1)).getText();
            } else if (gridPane.getChildren().get(i + 1).getClass() == ComboBox.class) {
                option = ((ComboBox<String>) gridPane.getChildren().get(i + 1)).getSelectionModel().getSelectedItem().replaceAll(" ", "+");
            } else if (gridPane.getChildren().get(i + 1).getClass() == TilePane.class) {
                option = getCheckedItems((TilePane) gridPane.getChildren().get(i + 1));
            }

            fields.put(label.getText(), option);

            if (label.getTextFill() == ((Paint) Color.RED) && option.isEmpty() || textBody.getText().isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please fill all the required fields (red) and text before submitting the new entry.");
                alert.showAndWait();
                allrequired = false;
                break;
            }
        }

        if (allrequired) {
            try {
                Jelog.submit(fields, textBody.getText(), null, snapshots, attachments,
                        comboBoxLogBook.getValue(), elogServer, userName, userPasswordHash);
            } catch (IOException ex) {
                Logger.getLogger(PostEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }

            Stage stage = (Stage) buttonSubmit.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Fills in the logbooks available for the selected logbook group.
     *
     * @param event
     */
    @FXML
    private void comboBoxLogBookGroupHandler(ActionEvent event) {
        if (!comboBoxLogBookGroup.getSelectionModel().isEmpty()) {
            logbookGroup = comboBoxLogBookGroup.getSelectionModel().getSelectedItem().replaceAll(" ", "+");

            updateLogbook();
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

            HashMap<String, LogbookAttribute> attributesMap = Jelog.getLogbookAttributes(elogServer, logbookGroup, logbook);

            cleanGridPane();
            updateGridPane(attributesMap);
        }
    }

    private void addNewTextField(String attribute, List<Label> labels, List<Region> controls) {
        Label label = new Label(attribute);
        TextField textField = new TextField();
        labels.add(label);
        controls.add(textField);
    }

    private void addNewComboBox(String attribute, List<Label> labels, List<Region> controls, String[] items) {
        Label label = new Label(attribute);
        ComboBox comboBox = new ComboBox<>();
        comboBox.getItems().addAll(Arrays.asList(items));
        comboBox.getSelectionModel().selectFirst();
        labels.add(label);
        controls.add(comboBox);
    }

    private void addNewCheckBox(String attribute, List<Label> labels, List<Region> controls, String[] items) {
        Label label = new Label(attribute);
        TilePane tilePane = new TilePane();
        tilePane.setTileAlignment(Pos.CENTER_LEFT);

        for (String item : items) {
            CheckBox checkBox = new CheckBox(item);
            gridControls.add(checkBox);
            tilePane.getChildren().add(checkBox);
        }

        if (tilePane.getChildren().size() == 1) {
            ((CheckBox) tilePane.getChildren().get(0)).setSelected(true);
        }
        labels.add(label);
        controls.add(tilePane);
    }

    /**
     * Updates the grid pane with all fields
     *
     * @param attributesMap
     */
    private void updateGridPane(HashMap<String, LogbookAttribute> attributesMap) {
        List<Label> labels = new ArrayList();
        List<Region> controls = new ArrayList();
        for (String attribute : attributesMap.keySet()) {
            if (attributesMap.get(attribute).getOptions() == null) {
                addNewTextField(attribute, labels, controls);
            } else if (!attributesMap.get(attribute).isMultioption()) {
                addNewComboBox(attribute, labels, controls, attributesMap.get(attribute).getOptions());
            } else if (attributesMap.get(attribute).isMultioption()) {
                addNewCheckBox(attribute, labels, controls, attributesMap.get(attribute).getOptions());
            }
            if (attributesMap.get(attribute).isRequired()) {
                labels.get(labels.size() - 1).setTextFill(Color.RED);
            }

            if (attributesMap.get(attribute).isLocked()) {
                controls.get(controls.size() - 1).setDisable(true);
                if (controls.get(controls.size() - 1).getClass().isInstance(TilePane.class)) {
                    for (Node control : controls.get(controls.size() - 1).getChildrenUnmodifiable()) {
                        control.setDisable(true);
                    }
                }
            }

            if (attribute.equals("Author")) {
                ((TextField) controls.get(controls.size() - 1)).setText(author);
            }
        }

        // Update gridPane with all new controls
        for (int i = 0; i < labels.size(); i++) {
            gridPane.add(labels.get(i), 0, 2 + i);
            gridPane.add(controls.get(i), 1, 2 + i);
            gridControls.add(labels.get(i));
            gridControls.add(controls.get(i));
        }
    }

    // Cleans up the previous controls
    private void cleanGridPane() {
        for (Region control : gridControls) {
            gridPane.getChildren().remove(control);
        }
    }

    @FXML
    private void handleSwitchUserButton(ActionEvent event) throws Exception {
        Jelog.logout();

        boolean loginResult = login();

        if (!loginResult) {
            handleButtonCancel();
        }
    }
}
