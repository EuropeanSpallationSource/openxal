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

import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class FXMLController implements Initializable {

    private RBAC rbac;
    private OpticsSwitcher opticsSwitcher;

    @FXML
    private CheckBox rbacCheckBox;
    @FXML
    private ListView<String> opticsListView;
    @FXML
    private Button changeOpticsButton;
    @FXML
    private Button setDefaultButton;
    @FXML
    private TextField opticsPathTextField;
    @FXML
    private Button refreshButton;
    @FXML
    private Button revertButton;
    @FXML
    private Button importButton;
    @FXML
    private TextField inputTWTextField;
    @FXML
    private TextField outputNameTextField;
    @FXML
    private ChoiceBox<String> initialParametersChoiceBox;
    @FXML
    private Button openFileButton;
    @FXML
    private Button openDirButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize RBAC object and associated UI elements
        rbac = new RBAC(rbacCheckBox);
        rbac.updateStatus();
        rbacCheckBox.setSelected(rbac.isEnabled());

        // Initialize OpticsSwitcher object and associated UI elements
        opticsSwitcher = OpticsSwitcher.getInstance();
        if (opticsSwitcher.getOpticsLibraryPath() != null) {
            opticsPathTextField.setText(opticsSwitcher.getOpticsLibraryPath());
            refreshButtonHandler(null);
        } else {
            opticsPathTextField.setText("DEFAULT OPTICS NOT SET!");
        }

        ObservableList<String> list = initialParametersChoiceBox.getItems();
        list.add("Default");
        list.add("Manual input");
        list.add("MEBT from .ini");
        list.add("All from .ini");
        initialParametersChoiceBox.setItems(FXCollections.observableList(list));
        initialParametersChoiceBox.getSelectionModel().select(0);

    }

    @FXML
    private void rbacCheckBoxAction(ActionEvent event) {
        if (rbacCheckBox.isSelected()) {
            rbac.enable();
        } else {
            rbac.disable();
        }
        rbac.updateStatus();
    }

    @FXML
    private void changeOpticsButtonHandler(ActionEvent event) {
        String newOpticsPath;
        newOpticsPath = opticsSwitcher.setDefaultOpticsPathDialog(changeOpticsButton.getScene(),
                opticsPathTextField.getText());
        if (newOpticsPath != null) {
            opticsPathTextField.setText(newOpticsPath);
            refreshButtonHandler(event);
        }
    }

    @FXML
    private void setDefaultButtonHandler(ActionEvent event) {
        boolean isSet = opticsSwitcher.setDefaultPath(opticsListView);
        if (isSet) {
            revertButton.setDisable(false);
        }
    }

    @FXML
    private void refreshButtonHandler(ActionEvent event) {
        opticsSwitcher.refreshList(opticsListView);
        if (opticsListView.getItems().isEmpty()) {
            setDefaultButton.setDisable(true);
        } else {
            setDefaultButton.setDisable(false);
        }
    }

    @FXML
    private void opticsPathTextFieldKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            opticsSwitcher.setOpticsLibraryPath(opticsPathTextField.getText());
            refreshButtonHandler(null);
        }
    }

    @FXML
    private void revertButtonHandler(ActionEvent event) {
        opticsSwitcher.revertDefaultPath(opticsListView);

        revertButton.setDisable(true);
    }

    @FXML
    private void importHandler(ActionEvent event) {
        JavaFXLogger logger = TraceWinImporter.importTW(inputTWTextField.getText(),
                Paths.get(opticsSwitcher.getOpticsLibraryPath(), outputNameTextField.getText()).toString(),
                initialParametersChoiceBox);

        logger.getAlert().getDialogPane().getScene().getWindow().setOnHiding(event2 -> {
            refreshButtonHandler(event);
        });
    }

    @FXML
    private void openFileButtonHandler(ActionEvent event) {
        String output = TraceWinImporter.openFileDialog(openFileButton.getScene());
        if (output != null) {
            inputTWTextField.setText(output);
        }

    }

    @FXML
    private void openDirButtonHandler(ActionEvent event) {
        String output = TraceWinImporter.dirDialog(openDirButton.getScene(), "Select Open XAL SMF output dir", null);
        if (output != null) {
            inputTWTextField.setText(output);
        }
    }
}
