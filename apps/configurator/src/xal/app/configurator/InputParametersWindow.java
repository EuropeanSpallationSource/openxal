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
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import xal.extension.tracewinimporter.TraceWin;
import xal.tools.beam.Twiss;

/**
 * FXML Controller class
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class InputParametersWindow implements Initializable {

    @FXML
    private Button doneButton;
    @FXML
    private TextField bunchFrequency;
    @FXML
    private TextField beamCurrent;
    @FXML
    private TextField kineticEnergy;
    @FXML
    private TextField alphaX;
    @FXML
    private TextField alphaY;
    @FXML
    private TextField alphaZ;
    @FXML
    private TextField betaX;
    @FXML
    private TextField betaY;
    @FXML
    private TextField betaZ;
    @FXML
    private TextField emittanceX;
    @FXML
    private TextField emittanceY;
    @FXML
    private TextField emittanceZ;

    private TraceWin traceWin;

    public void setTraceWin(TraceWin traceWin) {
        this.traceWin = traceWin;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bunchFrequency.setText("352.21");
        beamCurrent.setText("62.5e-3");
        kineticEnergy.setText("3.6217853e6");

        alphaX.setText("-0.051805615");
        alphaY.setText("-0.051805615");
        alphaZ.setText("-0.48130325");

        betaX.setText("0.20954703");
        betaY.setText("0.37074849");
        betaZ.setText("0.9256450");

        emittanceX.setText("0.25288");
        emittanceY.setText("0.251694");
        emittanceZ.setText("0.3615731");
    }

    @FXML
    private void doneButtonHandler(ActionEvent event) {
        traceWin.setBunchFrequency(Double.parseDouble(bunchFrequency.getText()));
        traceWin.setBeamCurrent(Double.parseDouble(beamCurrent.getText()));
        traceWin.setKineticEnergy(Double.parseDouble(kineticEnergy.getText()));
        traceWin.setInitialTwiss(new Twiss[]{
            new Twiss(Double.parseDouble(alphaX.getText()),
                Double.parseDouble(betaX.getText()),
                Double.parseDouble(emittanceX.getText()) * 1e-6),
            new Twiss(Double.parseDouble(alphaY.getText()),
                Double.parseDouble(betaY.getText()),
                Double.parseDouble(emittanceY.getText()) * 1e-6),
            new Twiss(Double.parseDouble(alphaZ.getText()),
                Double.parseDouble(betaZ.getText()),
                Double.parseDouble(emittanceZ.getText()) * 1e-6)});

        Stage stage = (Stage) doneButton.getScene().getWindow();
        stage.close();
    }

}
