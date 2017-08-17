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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import org.gillius.jfxutils.chart.ChartZoomManager;
import xal.ca.Channel;

public class FXMLController implements Initializable {

    @FXML
    private URL location;

    @FXML
    private TableView<ChannelWrapper> scanTable;

    @FXML
    private TableColumn<ChannelWrapper, String> scanTablePV;

    @FXML
    private TableColumn<ChannelWrapper, Boolean> scanTableRead;

    @FXML
    private TableColumn<ChannelWrapper, Boolean> scanTableScan;

    @FXML
    private Tab tabConfigure;

    @FXML
    private TableView<ChannelWrapper> listOfWriteables;

    @FXML
    private TableColumn<ChannelWrapper, String> listOfWriteablesShortVar;

    @FXML
    private TableColumn<ChannelWrapper, String> listOfWriteablesPV;

    @FXML
    private TableColumn<ChannelWrapper, Double> listOfWriteablesMin;

    @FXML
    private TableColumn<ChannelWrapper, Double> listOfWriteablesMax;

    @FXML
    private TableColumn<ChannelWrapper, Integer> listOfWriteablesNpoints;

    @FXML
    private ListView<String> constraintsList;

    @FXML
    private Tab tabRun;

    @FXML
    private Button executeButton;

    @FXML
    private TextField textFieldNumMeas;

    @FXML
    private TextField textFieldTimeEstimate;

    @FXML
    private TextField textFieldCurrentMeas;

    @FXML
    private ProgressBar runProgressBar;

    @FXML
    private Tab tabDisplay;

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

    public static ObservableList<ChannelWrapper> PVlist;
    public static ObservableList<ChannelWrapper> PVscanList;

    private static ChartZoomManager zoomManager;

    @FXML
    private void handleScanAddPV(ActionEvent event) {
        try {
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
    }

    private void plotMeasurement(String measName) {
        double [][] measurement = MainFunctions.dataSets.get(measName);
        List<Channel> pvR = MainFunctions.allPVrb.get(measName);
        List<Channel> pvW = MainFunctions.allPVw.get(measName);
        if (measurement != null) {
            for (int i=0;i<pvW.size();i++) {
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
    void handlePreCalculate(ActionEvent event) {
        int nPoints = MainFunctions.calculateCombos();
        textFieldNumMeas.setText("Number of measurement points: "+nPoints);
        textFieldTimeEstimate.setText("This will take "+MainFunctions.getTimeString(nPoints*2));
    }

    @FXML
    private void handleRunExecute(ActionEvent event) {
        MainFunctions.actionExecute();
        pvReadbacksGraph.getData().clear();
        pvWriteablesGraph.getData().clear();
        measurements.add("Measurement " + (measurements.size()+1));
        plotMeasurement("Measurement " + (measurements.size()));
        analyseList.getSelectionModel().clearAndSelect(measurements.size()-1);
        analyseList.autosize();
        tabDisplay.setDisable(false);
        textFieldCurrentMeas.setText("Finished");
    }

    private void dummyBugFunction() {
        // Needed due to bug in getSelectedItems()
    }

    @FXML
    private void analyseListMouseClicked(MouseEvent event) {
        pvReadbacksGraph.getData().clear();
        pvWriteablesGraph.getData().clear();
        zoomManager.stop();
        // There is a problem in getSelectedItems() so need to call this twice..
        analyseList.getSelectionModel().getSelectedItems().forEach((meas) -> dummyBugFunction());
        analyseList.getSelectionModel().getSelectedItems().forEach((meas) -> plotMeasurement(meas));
        zoomManager.start();
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert scanTable != null : "fx:id=\"scanTable\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert scanTablePV != null : "fx:id=\"scanTablePV\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert scanTableRead != null : "fx:id=\"scanTableRead\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert scanTableScan != null : "fx:id=\"scanTableScan\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert run_execute != null : "fx:id=\"run_execute\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert analyseList != null : "fx:id=\"analyseList\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert pvReadbacksGraph != null : "fx:id=\"pvReadbacksGraph\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert selectRect != null : "fx:id=\"selectRect\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert pvWriteablesGraph != null : "fx:id=\"pvWriteablesGraph\" was not injected: check your FXML file 'ScannerScene.fxml'.";

        // Initialize the list of scan variables..
        // TODO: A variable cannot be both read and written to..
        PVlist = FXCollections.observableArrayList();
        PVscanList = FXCollections.observableArrayList();
        scanTable.setItems(PVlist);
        scanTablePV.setCellValueFactory(new PropertyValueFactory<>("channelName"));
        scanTableRead.setCellFactory(CheckBoxTableCell.forTableColumn((Integer param) -> {
            if (PVlist.get(param).getIsRead()) {
                MainFunctions.actionScanAddPV(PVlist.get(param), "", true, false);
                if (MainFunctions.checkSufficientParams()) {
                    tabConfigure.setDisable(false);
                    tabRun.setDisable(false);
                }
            } else {
                MainFunctions.actionScanRemovePV(PVlist.get(param), true, false);
                if (!MainFunctions.checkSufficientParams()) {
                    tabConfigure.setDisable(true);
                    tabRun.setDisable(true);
                }
            }
            return PVlist.get(param).isReadProperty();
        }));
        scanTableRead.setCellValueFactory((CellDataFeatures<ChannelWrapper, Boolean> param) -> param.getValue().isReadProperty());
        scanTableScan.setCellFactory(CheckBoxTableCell.forTableColumn((Integer param) -> {
            MainFunctions.isCombosUpdated.set(false);
            if (PVlist.get(param).getIsScanned()) {
                PVlist.get(param).setInstance();
                if (MainFunctions.actionScanAddPV(PVlist.get(param), PVlist.get(param).instanceProperty().get(), false, true))
                    PVscanList.add(PVlist.get(param));
                if (MainFunctions.checkSufficientParams()) {
                    tabConfigure.setDisable(false);
                    tabRun.setDisable(false);
                }
            }
            else {
                MainFunctions.actionScanRemovePV(PVlist.get(param), false, true);
                PVscanList.remove(PVlist.get(param));
                if(PVscanList.isEmpty()) {
                    tabConfigure.setDisable(true);
                    tabRun.setDisable(true);
                }
                clearAllConstraints();
            }
            return PVlist.get(param).isScannedProperty();
        }));
        scanTableScan.setCellValueFactory((CellDataFeatures<ChannelWrapper, Boolean> param) -> param.getValue().isScannedProperty());

        // Initialize the configurations list

        // Initialize the list of measurements
        measurements = FXCollections.observableArrayList();
        analyseList.setItems(measurements);
        analyseList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        listOfWriteables.setItems(PVscanList);
        listOfWriteablesPV.setCellValueFactory(new PropertyValueFactory<>("channelName"));
        listOfWriteablesShortVar.setCellValueFactory(new PropertyValueFactory<>("instance"));
        listOfWriteablesMin.setCellValueFactory(new PropertyValueFactory<>("min"));
        listOfWriteablesMin.setCellFactory(TextFieldTableCell.<ChannelWrapper, Double>forTableColumn(new DoubleStringConverter()));
        listOfWriteablesMax.setCellValueFactory(new PropertyValueFactory<>("max"));
        listOfWriteablesMax.setCellFactory(TextFieldTableCell.<ChannelWrapper, Double>forTableColumn(new DoubleStringConverter()));
        listOfWriteablesNpoints.setCellValueFactory(new PropertyValueFactory<>("npoints"));
        listOfWriteablesNpoints.setCellFactory(TextFieldTableCell.<ChannelWrapper, Integer>forTableColumn(new IntegerStringConverter()));

        // Initialize functionality
        MainFunctions.initialize();

        // Initialize constraints
        ObservableList<String> constraints = FXCollections.observableArrayList("", "", "", "", "","", "", "", "");
        // Copy this to MainFunctions..
        constraints.forEach((constraint) -> MainFunctions.constraints.add(constraint));

        constraintsList.setItems(constraints);
        constraintsList.setCellFactory(TextFieldListCell.forListView());
        constraintsList.setOnEditCommit((ListView.EditEvent<String> t) -> {
            constraintsList.getItems().set(t.getIndex(), t.getNewValue());
            // TODO: is there not a better replace function for Lists?
            MainFunctions.constraints.add(t.getIndex(), t.getNewValue());
            MainFunctions.constraints.remove(t.getIndex()+1);
            MainFunctions.isCombosUpdated.set(false);
        });

        // Only allow to push execute when the combo list is up to date..
        executeButton.setDisable(!MainFunctions.isCombosUpdated.getValue());
        MainFunctions.isCombosUpdated.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> executeButton.setDisable(!newValue));
        // Write the current measurement point...
        MainFunctions.runProgress.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                textFieldCurrentMeas.setText("Running..");
                runProgressBar.setProgress(MainFunctions.runProgress.getValue());
            }
          });

        MainFunctions.isCombosUpdated.addListener((observable, oldValue, newValue) -> {
                    if(!newValue) {
                        textFieldNumMeas.setText("");
                        textFieldTimeEstimate.setText("");
                    }
                });

        // Allow zooming in the chart..
        zoomManager = new ChartZoomManager( analyseGraphPane, selectRect, pvReadbacksGraph );
        zoomManager.setMouseWheelZoomAllowed(true);
        zoomManager.setZoomDurationMillis(100);
        zoomManager.start();

        tabConfigure.setDisable(true);
        tabRun.setDisable(true);
        tabDisplay.setDisable(true);
    }

    private void clearAllConstraints() {
        constraintsList.getItems().setAll("");
        MainFunctions.constraints.clear();
        constraintsList.getItems().forEach((constraint) -> MainFunctions.constraints.add(constraint));
    }
}
