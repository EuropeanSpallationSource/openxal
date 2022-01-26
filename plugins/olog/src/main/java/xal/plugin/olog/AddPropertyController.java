package xal.plugin.olog;

/*
 * Copyright (C) 2022 European Spallation Source ERIC.
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
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

/**
 * FXML Controller class for the property selection dialog.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class AddPropertyController implements Initializable {

    @FXML
    private ChoiceBox<String> propertiesCB;
    @FXML
    private Button cancelButton;
    @FXML
    private Button addButton;

    private String selectedProperty = "";
    @FXML
    private ButtonBar buttonbar;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        propertiesCB.prefWidthProperty().bind(buttonbar.widthProperty());
    }

    public String getSelectedProperty() {
        return selectedProperty;
    }

    @FXML
    private void cancelButtonAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void addButtonAction(ActionEvent event) {
        selectedProperty = propertiesCB.getSelectionModel().getSelectedItem();

        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
    }

    void setProperties(Set<String> keySet) {
        propertiesCB.getItems().setAll(keySet);
        propertiesCB.getSelectionModel().selectFirst();
    }

}
