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

package xal.app.scanner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.application.Platform;
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
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
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
    private TableColumn<ChannelWrapper, String> scanColumnPV;

    @FXML
    private TableColumn<ChannelWrapper, Boolean> scanColumnSelect;

    @FXML
    private TableView<ChannelWrapper> readTable;

    @FXML
    private TableColumn<ChannelWrapper, String> readColumnPV;

    @FXML
    private TableColumn<ChannelWrapper, Boolean> readColumnSelect;

    @FXML
    private Tab tabConfigure;

    @FXML // fx:id="delayBetweenMeas"
    private TextField delayBetweenMeas; // Value injected by FXMLLoader

    @FXML // fx:id="measPerPoint"
    private TextField measPerPoint; // Value injected by FXMLLoader

    @FXML // fx:id="measDelaySetButton"
    private Button measDelaySetButton; // Value injected by FXMLLoader

    @FXML // fx:id="nMeasPerSettingSetButton"
    private Button nMeasPerSettingSetButton; // Value injected by FXMLLoader

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
    private ProgressBar runProgressBar;

    @FXML
    private Text textFieldNumMeas;

    @FXML
    private Text textFieldTimeEstimate;

    @FXML
    private Button stopButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button restartButton;

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

    public static ObservableList<ChannelWrapper> pvScannablelist;
    public static ObservableList<ChannelWrapper> pvReadablelist;
    public static ObservableList<ChannelWrapper> PVscanList;
    public static ObservableList<ChannelWrapper> PVreadList;

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
            Logger.getLogger(FXMLController.class.getName()).log(Level.FINER, "Add PV Window opened");
        }
        catch (IOException e) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, "Error opening Add PV window", e);
        }
    }

    @FXML
    private void handleScanRemovePV(ActionEvent event) {
            readTable.getSelectionModel().getSelectedItems().forEach(cW ->
                    Logger.getLogger(FXMLController.class.getName()).log(Level.INFO, "Parameter {0} removed from readback channel list",cW.getChannelName()));
            readTable.getSelectionModel().getSelectedItems().forEach(cW -> readTable.getItems().remove(cW));
            scanTable.getSelectionModel().getSelectedItems().forEach(cW ->
                    Logger.getLogger(FXMLController.class.getName()).log(Level.INFO, "Parameter {0} removed from writeable channel list",cW.getChannelName()));
            scanTable.getSelectionModel().getSelectedItems().forEach(cW -> scanTable.getItems().remove(cW));
    }

    // Extend the the first dimension of the double array to length newLength
    private double[][] extendArray(double[][] origArray, int newLength) {
        double[][] newArray = new double[newLength][origArray[0].length];
        for (int i=0;i<origArray.length;i++) {
            for (int j=0;j<origArray[0].length;j++) {
                newArray[i][j]=origArray[i][j];
            }
        }
        return newArray;
    }

    @FXML
    void handleLoadDocument(ActionEvent event) {
        Logger.getLogger(FXMLController.class.getName()).log(Level.INFO, "Loading document..");
        try {
            // This hard-coded file name should be fixed
            MainFunctions.mainDocument.loadDocument(new File("scanner.xml").toURI().toURL());
            PVscanList.clear();
            MainFunctions.mainDocument.pvWriteables.forEach(cWrapper -> PVscanList.add(cWrapper));
            pvScannablelist.clear();
            pvReadablelist.clear();
            MainFunctions.mainDocument.pvWriteables.forEach(cWrapper -> pvScannablelist.add(cWrapper));
            MainFunctions.mainDocument.pvReadbacks.forEach(cWrapper -> pvReadablelist.add(cWrapper));

            constraintsList.setItems(MainFunctions.mainDocument.constraints);
            MainFunctions.isCombosUpdated.set(false);
            handlePreCalculate(event);
            // In case there is a half finished measurement in the file..
            if (MainFunctions.mainDocument.currentMeasurement != null) {
                MainFunctions.mainDocument.nCombosDone = MainFunctions.mainDocument.currentMeasurement.length;
                double[][] fullMeasurement = extendArray(MainFunctions.mainDocument.currentMeasurement, MainFunctions.mainDocument.combos.size());
                MainFunctions.mainDocument.currentMeasurement = fullMeasurement;
                plotMeasurement();
                restartButton.setVisible(true);
            }
            Logger.getLogger(FXMLController.class.getName()).log(Level.INFO, "Document loaded");

        } catch (MalformedURLException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Plot measurement of name measName
    private void plotMeasurement(String measName) {
        double [][] measurement = MainFunctions.mainDocument.dataSets.get(measName);
        List<Channel> pvR = MainFunctions.mainDocument.allPVrb.get(measName);
        List<Channel> pvW = MainFunctions.mainDocument.allPVw.get(measName);
        plotMeasurement(measurement, pvW, pvR);
    }

    // Plot the current (ongoing) measurement
    private void plotMeasurement() {
        List<Channel> _pvR = new ArrayList<>();
        List<Channel> _pvW = new ArrayList<>();
        MainFunctions.mainDocument.pvReadbacks.forEach(cWrap -> _pvR.add(cWrap.getChannel()));
        MainFunctions.mainDocument.pvWriteables.forEach(cWrap -> _pvW.add(cWrap.getChannel()));
        plotMeasurement(MainFunctions.mainDocument.currentMeasurement,_pvW,_pvR);
    }

    // Manually provide list of data and list of channels for the plot
    private void plotMeasurement(double [][] measurement, List<Channel> pvWriteables, List<Channel> pvReadbacks) {
        if (measurement != null) {
            for (int i=0;i<pvWriteables.size();i++) {
                XYChart.Series<Number, Number> series = new XYChart.Series();
                for (int j=0;j<measurement.length;j++) {
                    series.getData().add( new XYChart.Data(j, measurement[j][i]) );
                }
                series.setName(pvWriteables.get(i).getId());
                pvWriteablesGraph.getData().add(series);
            }
            for (int i=pvWriteables.size();i<measurement[0].length;i++) {
                XYChart.Series<Number, Number> series = new XYChart.Series();
                for (int j=0;j<measurement.length;j++) {
                    series.getData().add( new XYChart.Data(j, measurement[j][i]) );
                }
                series.setName(pvReadbacks.get(i-pvWriteables.size()).getId());
                pvReadbacksGraph.getData().add(series);
            }
        }
    }

    @FXML
    void handlePreCalculate(ActionEvent event) {
        int nPoints = MainFunctions.calculateNumMeas();
        textFieldNumMeas.setText("Number of measurement points: "+nPoints);
        textFieldTimeEstimate.setText("This will take "+MainFunctions.getTimeString(nPoints));
    }

    @FXML
    private void handleRunExecute(ActionEvent event) {
        MainFunctions.actionExecute();
        restartButton.setVisible(false);
        if (MainFunctions.mainDocument.nCombosDone == 0) {
            analyseList.getSelectionModel().clearSelection();
            analyseList.autosize();
        }
        plotMeasurement();
    }

    @FXML
    void handleRunRestart(ActionEvent event) {
        // Similar to handleRunExecute, but we set combos done back to 0
        // We do not care about showing tabDisplay (it must be available at this point)
        MainFunctions.mainDocument.nCombosDone = 0;
        MainFunctions.actionExecute();
        restartButton.setVisible(false);
        analyseList.getSelectionModel().clearSelection();
        analyseList.autosize();
        plotMeasurement();
    }

    @FXML
    private void handleRunPause(ActionEvent event) {
        MainFunctions.triggerPause();
    }

    @FXML
    private void handleRunStop(ActionEvent event) {
        MainFunctions.triggerStop();
        // This does not seem to work properly.
        restartButton.setVisible(MainFunctions.mainDocument.nCombosDone != 0);
    }

    private void dummyBugFunction() {
        // Needed due to problem with getSelectedItems()
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


    @FXML
    void setDelayEdited(KeyEvent event) {
        Logger.getLogger(FXMLController.class.getName()).log(Level.FINER, "Delay edited to {0}", delayBetweenMeas.getText());
        if((long) (Float.parseFloat(delayBetweenMeas.getText())*1000)!=MainFunctions.mainDocument.delayBetweenMeasurements)
            measDelaySetButton.setDisable(false);
        else
            measDelaySetButton.setDisable(true);
    }

    @FXML
    void setMeasPerPointEdited(KeyEvent event) {
        Logger.getLogger(FXMLController.class.getName()).log(Level.FINER, "Measurements per point edited to {0}", measPerPoint.getText());
        if(Integer.parseInt(measPerPoint.getText())!=MainFunctions.mainDocument.numberMeasurementsPerCombo)
            nMeasPerSettingSetButton.setDisable(false);
        else
            nMeasPerSettingSetButton.setDisable(true);
    }


    @FXML
    void setMeasurementDelay(ActionEvent event) {
        MainFunctions.mainDocument.delayBetweenMeasurements=(long) (Float.parseFloat(delayBetweenMeas.getText())*1000);
        Logger.getLogger(FXMLController.class.getName()).log(Level.INFO, "Measurement delay set to {0} ms", MainFunctions.mainDocument.delayBetweenMeasurements);
        // If combos are already calculated we trigger a quick refresh of the calculation
        if (MainFunctions.isCombosUpdated.getValue())
            handlePreCalculate(event);
        measDelaySetButton.setDisable(true);
    }

    @FXML
    void setNumberOfMeasPerSetting(ActionEvent event) {
        MainFunctions.mainDocument.numberMeasurementsPerCombo = Integer.parseInt(measPerPoint.getText());
        Logger.getLogger(FXMLController.class.getName()).log(Level.INFO, "Measurement per point set to {0}", MainFunctions.mainDocument.numberMeasurementsPerCombo);
        // If combos are already calculated we trigger a quick refresh of the calculation
        if (MainFunctions.isCombosUpdated.getValue())
            handlePreCalculate(event);
        nMeasPerSettingSetButton.setDisable(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert scanTable != null : "fx:id=\"scanTable\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert scanColumnPV != null : "fx:id=\"scanColumnPV\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert scanColumnSelect != null : "fx:id=\"scanColumnSelect\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert readTable != null : "fx:id=\"readTable\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert readColumnPV != null : "fx:id=\"readColumnPV\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert readColumnSelect != null : "fx:id=\"readColumnSelect\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert tabConfigure != null : "fx:id=\"tabConfigure\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert delayBetweenMeas != null : "fx:id=\"delayBetweenMeas\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert measPerPoint != null : "fx:id=\"measPerPoint\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert measDelaySetButton != null : "fx:id=\"measDelaySetButton\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert nMeasPerSettingSetButton != null : "fx:id=\"nMeasPerSettingSetButton\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert listOfWriteables != null : "fx:id=\"listOfWriteables\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert listOfWriteablesShortVar != null : "fx:id=\"listOfWriteablesShortVar\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert listOfWriteablesPV != null : "fx:id=\"listOfWriteablesPV\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert listOfWriteablesMin != null : "fx:id=\"listOfWriteablesMin\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert listOfWriteablesMax != null : "fx:id=\"listOfWriteablesMax\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert listOfWriteablesNpoints != null : "fx:id=\"listOfWriteablesNpoints\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert constraintsList != null : "fx:id=\"constraintsList\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert tabRun != null : "fx:id=\"tabRun\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert executeButton != null : "fx:id=\"executeButton\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert runProgressBar != null : "fx:id=\"runProgressBar\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert textFieldNumMeas != null : "fx:id=\"textFieldNumMeas\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert textFieldTimeEstimate != null : "fx:id=\"textFieldTimeEstimate\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert pauseButton != null : "fx:id=\"pauseButton\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert stopButton != null : "fx:id=\"stopButton\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert restartButton != null : "fx:id=\"restartButton\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert tabDisplay != null : "fx:id=\"tabDisplay\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert analyseList != null : "fx:id=\"analyseList\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert analyseGraphPane != null : "fx:id=\"analyseGraphPane\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert pvReadbacksGraph != null : "fx:id=\"pvReadbacksGraph\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert selectRect != null : "fx:id=\"selectRect\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert pvWriteablesGraph != null : "fx:id=\"pvWriteablesGraph\" was not injected: check your FXML file 'ScannerScene.fxml'.";



        initializeSelectionTables();

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


        // Initialize constraints
        constraintsList.setItems(MainFunctions.mainDocument.constraints);
        constraintsList.setCellFactory(TextFieldListCell.forListView());
        constraintsList.setOnEditCommit((ListView.EditEvent<String> t) -> {
            constraintsList.getItems().set(t.getIndex(), t.getNewValue());
            MainFunctions.mainDocument.constraints.set(t.getIndex(), t.getNewValue());
            MainFunctions.isCombosUpdated.set(false);
        });

        // Only allow to push execute when the combo list is up to date..
        executeButton.setDisable(!MainFunctions.isCombosUpdated.getValue());
        MainFunctions.isCombosUpdated.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> executeButton.setDisable(!newValue));


        MainFunctions.mainDocument.numberOfScans.addListener((observable, oldValue, newValue) -> updateAnalysisList());

        // Similarly for stop and pause buttons..
        MainFunctions.pauseTask.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                if (MainFunctions.pauseTask.get())
                    pauseButton.setText("Continue");
                else
                    pauseButton.setText("Pause");
            }
          });

        // Deal with the progress of the run, activate to pause/stop buttons, plot last measurement etc..
        MainFunctions.runProgress.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                if(MainFunctions.runProgress.getValue()<1.0 && MainFunctions.stopTask.get()==false) {
                    executeButton.setText("Running..");
                    executeButton.setDisable(true);
                    pauseButton.setVisible(true);
                    stopButton.setVisible(true);
                    runProgressBar.setProgress(MainFunctions.runProgress.getValue());
                } else {
                    executeButton.setText("Execute");
                    executeButton.setDisable(false);
                    pauseButton.setVisible(false);
                    stopButton.setVisible(false);
                    if (MainFunctions.stopTask.get()==false)
                        runProgressBar.setProgress(0.0);
                }
                pvReadbacksGraph.getData().clear();
                pvWriteablesGraph.getData().clear();
                plotMeasurement();
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

        // -- configuration text fields --
        // Set default values in the box
        delayBetweenMeas.setText(String.valueOf(0.001*MainFunctions.mainDocument.delayBetweenMeasurements));
        measPerPoint.setText(String.valueOf(MainFunctions.mainDocument.numberMeasurementsPerCombo));
        // Add ToolTips to both text fields
        Tooltip delayBetweenMeasTooltip = new Tooltip();
        delayBetweenMeasTooltip.setText("Set the delay between measurements");
        delayBetweenMeas.setTooltip(delayBetweenMeasTooltip);

        Tooltip measPerPointTooltip = new Tooltip();
        measPerPointTooltip.setText("Number of measurements at each combo");
        measPerPoint.setTooltip(measPerPointTooltip);
        // force the field to be a double only
        Pattern delayFieldPattern = Pattern.compile("\\d*|\\d+\\.\\d*");
        TextFormatter delayFieldFormatter = new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> {
            return delayFieldPattern.matcher(change.getControlNewText()).matches() ? change : null;
        });
        delayBetweenMeas.setTextFormatter(delayFieldFormatter);
        // force the field to be numeric (integer) only
        Pattern measPerFieldPattern = Pattern.compile("\\d*");
        TextFormatter measPerFieldFormatter = new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> {
            return measPerFieldPattern.matcher(change.getControlNewText()).matches() ? change : null;
        });
        measPerPoint.setTextFormatter(measPerFieldFormatter);
    }

    private void initializeSelectionTables() {
        // Initialize the list of scan variables..
        // TODO: A variable cannot be both read and written to..
        pvScannablelist = FXCollections.observableArrayList();
        pvReadablelist = FXCollections.observableArrayList();
        PVscanList = FXCollections.observableArrayList();
        scanTable.setItems(pvScannablelist);
        readTable.setItems(pvReadablelist);

        scanColumnPV.setCellValueFactory(new PropertyValueFactory<>("channelName"));
        readColumnPV.setCellValueFactory(new PropertyValueFactory<>("channelName"));

        readColumnSelect.setCellFactory(CheckBoxTableCell.forTableColumn((Integer param) -> {
            if (pvReadablelist.get(param).getIsRead()) {
                MainFunctions.actionScanAddPV(pvReadablelist.get(param), true, false);
                if (MainFunctions.checkSufficientParams()) {
                    tabConfigure.setDisable(false);
                    tabRun.setDisable(false);
                }
            } else {
                MainFunctions.actionScanRemovePV(pvReadablelist.get(param), true, false);
                if (!MainFunctions.checkSufficientParams()) {
                    tabConfigure.setDisable(true);
                    tabRun.setDisable(true);
                }
            }
            return pvReadablelist.get(param).isReadProperty();
        }));
        readColumnSelect.setCellValueFactory((CellDataFeatures<ChannelWrapper, Boolean> param) -> param.getValue().isReadProperty());

        scanColumnSelect.setCellFactory(CheckBoxTableCell.forTableColumn((Integer param) -> {
            if (pvScannablelist.get(param).getIsScanned()) {
                pvScannablelist.get(param).setInstance();
                if (MainFunctions.actionScanAddPV(pvScannablelist.get(param), false, true)) {
                    PVscanList.add(pvScannablelist.get(param));
                    MainFunctions.isCombosUpdated.set(false);
                    MainFunctions.mainDocument.setHasChanges(true);
                }
                if (MainFunctions.checkSufficientParams()) {
                    tabConfigure.setDisable(false);
                    tabRun.setDisable(false);
                }
            }
            else {
                MainFunctions.actionScanRemovePV(pvScannablelist.get(param), false, true);
                if(PVscanList.remove(pvScannablelist.get(param))) {
                    clearAllConstraints();
                    MainFunctions.isCombosUpdated.set(false);
                }
                if(PVscanList.isEmpty()) {
                    tabConfigure.setDisable(true);
                    tabRun.setDisable(true);
                }
            }
            return pvScannablelist.get(param).isScannedProperty();
        }));
        scanColumnSelect.setCellValueFactory((CellDataFeatures<ChannelWrapper, Boolean> param) -> param.getValue().isScannedProperty());

        // Ideally should be able to select multiple channels to remove, but this does not seem to work.
        //readTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //scanTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        readTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> { if (newValue!=null) scanTable.getSelectionModel().clearSelection();});
        scanTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> { if (newValue!=null) readTable.getSelectionModel().clearSelection();});
    }

    private void clearAllConstraints() {
        for(int i = 0; i<MainFunctions.mainDocument.constraints.size();i++)
            MainFunctions.mainDocument.constraints.set(i, "");
        constraintsList.setItems(MainFunctions.mainDocument.constraints);
    }

    // Whenever there is an update to the list of finished measurements, this should be called
    private void updateAnalysisList() {
        // This is typically called by a thread, so use Platform.runLater in order to avoid IllegalStateException
        Platform.runLater(
            () -> {
                measurements.clear();
                MainFunctions.mainDocument.dataSets.entrySet().forEach(dataSet -> measurements.add(dataSet.getKey()));
                if (measurements.size() >0 )
                    tabDisplay.setDisable(false);
                Logger.getLogger(FXMLController.class.getName()).log(Level.FINER, "Analysis list updated");
            }
          );
    }
}
