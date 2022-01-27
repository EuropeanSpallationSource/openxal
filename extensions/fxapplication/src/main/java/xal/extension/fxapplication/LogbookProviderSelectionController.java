package xal.extension.fxapplication;

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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import xal.extension.logbook.Logbook;
import xal.extension.logbook.LogbookProvider;

/**
 * FXML Controller class
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class LogbookProviderSelectionController implements Initializable {

    @FXML
    private ChoiceBox<String> logbookProvidersCB;
    @FXML
    private Button okB;

    private LogbookProvider provider;
    private List<LogbookProvider> logbookProviders;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logbookProviders = Logbook.getLogbookProviders();
        ObservableList<String> logbooks = FXCollections.observableList(new ArrayList<>());
        for (LogbookProvider provider : logbookProviders) {
            logbooks.add(provider.getClass().getSimpleName());
        }
        logbookProvidersCB.setItems(logbooks);
        logbookProvidersCB.getSelectionModel().selectFirst();
    }

    @FXML
    private void okBAction(ActionEvent event) {
        provider = logbookProviders.get(logbookProvidersCB.getSelectionModel().getSelectedIndex());
        ((Stage) okB.getScene().getWindow()).close();
    }

    public LogbookProvider getProvider() {
        return provider;
    }
}
