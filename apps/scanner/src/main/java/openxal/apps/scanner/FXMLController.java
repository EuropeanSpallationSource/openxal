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

package openxal.apps.scanner;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import org.gillius.jfxutils.chart.ChartZoomManager;
import xal.ca.Channel;

public class FXMLController implements Initializable {

    @FXML
    private URL location;

    @FXML
    private TreeTableView<?> scanTree;

    @FXML
    private TreeTableColumn<?, ?> scanTablePV;

    @FXML
    private TreeTableColumn<?, ?> scanTableInclude;

    @FXML
    private ListView<?> run_execute;

    @FXML
    private ListView<String> analyseList;

    @FXML
    private StackPane analyseGraphPane;

    @FXML
    private LineChart<Number, Number> pvReadbacksGraph;

    @FXML
    private Rectangle selectRect;

    @FXML
    private LineChart<Number, Number> pvWriteablesGraph;

    private static ObservableList<String> measurements;

    private static ChartZoomManager zoomManager;

    @FXML
    private void handleScanAddPV(ActionEvent event) {
        MainFunctions.actionScanAddPV();
        try {
            //Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("/fxml/AddPV.fxml"));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/AddPV.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Add PV");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch (IOException e) {
            System.out.println("DBG Error opening Add PV window");
        }
    }

    @FXML
    private void handleScanRemovePV(ActionEvent event) {
        MainFunctions.actionScanRemovePV();
    }

    private void plotMeasurement(String measName) {
        double [][] measurement = MainFunctions.dataSets.get(measName);
        List<Channel> pvR = MainFunctions.allPVrb.get(measName);
        List<Channel> pvW = MainFunctions.allPVw.get(measName);
        if (measurement != null) {
            for (int i=0;i<pvR.size();i++) {
                XYChart.Series<Number, Number> series = new XYChart.Series();
                for (int j=0;j<measurement.length;j++) {
                    series.getData().add( new XYChart.Data(j, measurement[j][i]) );
                }
                series.setName("PV writeable #" + (measurements.size()+1)+", "+i);
                pvWriteablesGraph.getData().add(series);
            }
            for (int i=pvW.size();i<measurement[0].length;i++) {
                XYChart.Series<Number, Number> series = new XYChart.Series();
                for (int j=0;j<measurement.length;j++) {
                    series.getData().add( new XYChart.Data(j, measurement[j][i]) );
                }
                series.setName("PV readback #" + (measurements.size()+1)+", "+i);
                pvReadbacksGraph.getData().add(series);
            }
        }
    }

    @FXML
    private void handleRunExecute(ActionEvent event) {
        double[][] measurement = MainFunctions.actionExecute();
        pvReadbacksGraph.getData().clear();
        pvWriteablesGraph.getData().clear();
        measurements.add("Measurement " + (measurements.size()+1));
        plotMeasurement("Measurement " + (measurements.size()));
        analyseList.getSelectionModel().clearAndSelect(measurements.size()-1);
        analyseList.autosize();
    }

    private void dummyBugFunction() {
        // Needed due to bug in getSelectedItems()
    }

    @FXML
    private void analyseListMouseClicked(MouseEvent event) {
        pvReadbacksGraph.getData().clear();
        pvWriteablesGraph.getData().clear();
        zoomManager.stop();
        // There is a bug in getSelectedItems() so need to call this twice..
        analyseList.getSelectionModel().getSelectedItems().forEach((meas) -> dummyBugFunction());
        analyseList.getSelectionModel().getSelectedItems().forEach((meas) -> plotMeasurement(meas));
        zoomManager.start();
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert scanTree != null : "fx:id=\"scanTree\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert scanTablePV != null : "fx:id=\"scanTablePV\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert scanTableInclude != null : "fx:id=\"scanTableInclude\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert run_execute != null : "fx:id=\"run_execute\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert analyseList != null : "fx:id=\"analyseList\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert pvReadbacksGraph != null : "fx:id=\"pvReadbacksGraph\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert selectRect != null : "fx:id=\"selectRect\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert pvWriteablesGraph != null : "fx:id=\"pvWriteablesGraph\" was not injected: check your FXML file 'ScannerScene.fxml'.";


        measurements = FXCollections.observableArrayList();
        analyseList.setItems(measurements);

        analyseList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        MainFunctions.initialize();

        // Allow zooming in the chart..
        zoomManager = new ChartZoomManager( analyseGraphPane, selectRect, pvReadbacksGraph );
        zoomManager.setMouseWheelZoomAllowed(true);
        zoomManager.setZoomDurationMillis(100);
        zoomManager.start();
    }
}
