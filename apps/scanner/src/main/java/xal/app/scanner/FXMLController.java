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
import java.net.URL;
import java.nio.file.Files;
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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class FXMLController implements Initializable {

    @FXML
    private URL location;

    @FXML
    private TableView<ChannelWrapper> pvTable;

    @FXML
    private TableColumn<ChannelWrapper, String> pvNameColumn;

    @FXML
    private TableColumn<ChannelWrapper, Boolean> scanColumnSelect;

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
    private TextField commandToExecute;

    @FXML
    private CheckBox commandActivated;

    @FXML
    private TableView<ChannelWrapper> listOfWriteables;

    @FXML
    private TableColumn<ChannelWrapper, String> listOfWriteablesShortVar;

    @FXML
    private TableColumn<ChannelWrapper, String> listOfWriteablesPV;

    @FXML
    private TableColumn<ChannelWrapper, String> listOfWriteablesUnit;

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
    private Text textFieldWarnSave;

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
    private TableView<SimpleOption> expertOptions;

    @FXML
    private TableColumn<SimpleOption, Boolean> expertOptionDesc;

    @FXML
    private TableColumn<SimpleOption, Boolean> expertOptionSelected;

    @FXML
    private ListView<String> analyseList;

    @FXML
    private Plot plotArea;

    private static ObservableList<String> measurements;

    public static ObservableList<ChannelWrapper> PVscanList;

    public static ObservableList<SimpleOption> runOptions;

    private static final Logger logger = Logger.getLogger(FXMLController.class.getName());

    @FXML
    private void handleScanAddPV(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/AddPV.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Add PV");
            stage.setScene(new Scene(root));
            stage.show();
            logger.log(Level.FINER, "Add PV Window opened");
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Error opening Add PV window", e);
        }
    }

    @FXML
    private void handleScanRemovePV(ActionEvent event) {
            pvTable.getSelectionModel().getSelectedItems().forEach(cW -> {
                    logger.log(Level.INFO, "Parameter {0} removed from channel list",cW.getChannelName());
                    pvTable.getItems().remove(cW);
                    listOfWriteables.getItems().remove(cW);
            });
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
    void handlePreCalculate(ActionEvent event) {
        int nPoints = MainFunctions.calculateNumMeas();
        // Trigger update of Execute button
        MainFunctions.isCombosUpdated.set(true);
        int nDone = MainFunctions.mainDocument.nCombosDone;
        textFieldNumMeas.setText("Number of measurement points: "+nPoints);
        if (nDone==0) {
            textFieldTimeEstimate.setText("This will take "+MainFunctions.getTimeString(nPoints));
        } else {
            textFieldTimeEstimate.setText((nPoints-nDone) +" remaining, this will take "+MainFunctions.getTimeString(nPoints-nDone));
        }
        if (!MainFunctions.mainDocument.sourceSetAndValid())
            textFieldWarnSave.setText("Remember to save document first!");
    }

    @FXML
    private void handleRunExecute(ActionEvent event) {
        MainFunctions.actionExecute();
        restartButton.setVisible(false);
        if (MainFunctions.mainDocument.nCombosDone == 0) {
            analyseList.getSelectionModel().clearSelection();
            analyseList.autosize();
        }
        plotArea.plotMeasurement();
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
        plotArea.plotMeasurement();
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

    @FXML
    private void handleInspectCommand(ActionEvent event) {
        // Check that the command written down is valid,
        // and if it is, allow to activate it
        String command = commandToExecute.getText().split("\\s+")[0];
        File filepath = new File(command);
        if (filepath.canExecute()) {
            commandActivated.setDisable(false);
            MainFunctions.mainDocument.commandToExecute.set(commandToExecute.getText());
        } else {
            commandActivated.setDisable(true);
            MainFunctions.mainDocument.commandToExecute.set("");
            String[] paths = System.getenv("PATH").split(":");
            for (String path: paths) {
                filepath = new File(path + "/" + command);
                if (filepath.canExecute()) {
                    commandActivated.setDisable(false);
                    break;
                }
            }

        }
        if (commandActivated.isDisabled())
            commandActivated.setSelected(false);

    }

    @FXML
    private void handleActivateCommand(ActionEvent event) {
        if (commandActivated.isSelected())
            MainFunctions.mainDocument.isCommandActive.set(true);
        else
            MainFunctions.mainDocument.isCommandActive.set(false);
    }

    private void dummyBugFunction() {
        // Needed due to problem with getSelectedItems()
    }

    @FXML
    private void analyseListMouseClicked(MouseEvent event) {
        plotArea.clear();
        // There is a problem in getSelectedItems() so need to call this twice..
        analyseList.getSelectionModel().getSelectedItems().forEach((meas) -> dummyBugFunction());
        analyseList.getSelectionModel().getSelectedItems().forEach((meas) -> plotArea.plotMeasurement(meas));
        plotArea.startZoom();
    }


    @FXML
    void setDelayEdited(KeyEvent event) {
        if (!delayBetweenMeas.getText().equals("")) {
            logger.log(Level.FINER, "Delay edited to {0}", delayBetweenMeas.getText());
            if((long) (Float.parseFloat(delayBetweenMeas.getText())*1000)!=MainFunctions.mainDocument.delayBetweenMeasurements.get())
                measDelaySetButton.setDisable(false);
            else
                measDelaySetButton.setDisable(true);
        }
    }

    @FXML
    void setMeasPerPointEdited(KeyEvent event) {
        if (!measPerPoint.getText().equals("")) {
            logger.log(Level.FINER, "Measurements per point edited to {0}", measPerPoint.getText());
            if(Integer.parseInt(measPerPoint.getText())!=MainFunctions.mainDocument.numberMeasurementsPerCombo.get())
                nMeasPerSettingSetButton.setDisable(false);
            else
                nMeasPerSettingSetButton.setDisable(true);
        }
    }


    @FXML
    void setMeasurementDelay(ActionEvent event) {
        MainFunctions.mainDocument.delayBetweenMeasurements.set((long) (Float.parseFloat(delayBetweenMeas.getText())*1000));
        logger.log(Level.INFO, "Measurement delay set to {0} ms", MainFunctions.mainDocument.delayBetweenMeasurements.get());
        // If combos are already calculated we trigger a quick refresh of the calculation
        if (MainFunctions.isCombosUpdated.getValue())
            handlePreCalculate(event);
        measDelaySetButton.setDisable(true);
    }

    @FXML
    void setNumberOfMeasPerSetting(ActionEvent event) {
        MainFunctions.mainDocument.numberMeasurementsPerCombo.set(Integer.parseInt(measPerPoint.getText()));
        logger.log(Level.INFO, "Measurements per point set to {0}", MainFunctions.mainDocument.numberMeasurementsPerCombo.get());
        // If combos are already calculated we trigger a quick refresh of the calculation
        if (MainFunctions.isCombosUpdated.getValue())
            handlePreCalculate(event);
        nMeasPerSettingSetButton.setDisable(true);
    }

    void assertObject(Object obj, String name) {
        assert obj != null : "fx:id=\""+name+"\" was not injected: check your FXML file 'ScannerScene.fxml'.";
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        assertObject(pvTable, "pvTable");
        assertObject(pvNameColumn, "pvNameColumn");
        assertObject(scanColumnSelect, "scanColumnSelect");
        assertObject(readColumnSelect, "readColumnSelect");

        assertObject(tabConfigure, "tabConfigure");
        assertObject(delayBetweenMeas, "delayBetweenMeas");
        assertObject(measPerPoint, "measPerPoint");
        assertObject(measDelaySetButton, "measDelaySetButton");
        assertObject(nMeasPerSettingSetButton, "nMeasPerSettingSetButton");
        assertObject(commandToExecute, "commandToExecute");
        assertObject(commandActivated, "commandActivated");
        assertObject(listOfWriteables, "listOfWriteables");
        assertObject(listOfWriteablesShortVar, "listOfWriteablesShortVar");
        assertObject(listOfWriteablesPV, "listOfWriteablesPV");
        assertObject(listOfWriteablesUnit, "listOfWriteablesUnit");
        assertObject(listOfWriteablesMin, "listOfWriteablesMin");
        assertObject(listOfWriteablesMax, "listOfWriteablesMax");
        assertObject(listOfWriteablesNpoints, "listOfWriteablesNpoints");
        assertObject(constraintsList, "constraintsList");

        assertObject(tabRun, "tabRun");
        assertObject(executeButton, "");
        assertObject(runProgressBar, "runProgressBar");
        assertObject(textFieldNumMeas, "textFieldNumMeas");
        assertObject(textFieldTimeEstimate, "textFieldTimeEstimate");
        assertObject(pauseButton, "pauseButton");
        assertObject(stopButton, "stopButton");
        assertObject(restartButton, "restartButton");
        assertObject(expertOptions, "expertOptions");
        assertObject(expertOptionDesc, "expertOptionDesc");
        assertObject(expertOptionSelected, "expertOptionSelected");

        assertObject(tabDisplay, "tabDisplay");
        assertObject(analyseList, "analyseList");
        assertObject(plotArea, "plotArea");



        initializeSelectionTables();
        initializeExpertOptions();

        // Initialize the configurations list

        // Initialize the list of measurements
        measurements = FXCollections.observableArrayList();
        analyseList.setItems(measurements);
        analyseList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        listOfWriteables.setItems(PVscanList);
        listOfWriteablesPV.setCellValueFactory(new PropertyValueFactory<>("channelName"));
        listOfWriteablesShortVar.setCellValueFactory(new PropertyValueFactory<>("instance"));
        listOfWriteablesUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
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
        MainFunctions.isCombosUpdated.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            boolean executeAllowed = newValue;
            logger.log(Level.FINEST, "Execute allowed: {0}", new Object[]{executeAllowed});
            executeButton.setDisable(!executeAllowed);
            });


        MainFunctions.mainDocument.numberOfScans.addListener((observable, oldValue, newValue) -> updateAnalysisList());

        // Similarly for stop and pause buttons..
        MainFunctions.pauseTask.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                if ((Boolean) newVal) {
                    pauseButton.setText("Continue");
                    restartButton.setVisible(false);
                } else {
                    pauseButton.setText("Pause");
                }
            }
          });

        MainFunctions.stopTask.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                if ((Boolean) newVal) {
                    restartButton.setVisible(true);
                } else {
                    restartButton.setVisible(false);

                }
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
                    if(restartButton.isVisible())
                        executeButton.setText("Continue");
                    else
                        executeButton.setText("Execute");
                    executeButton.setDisable(false);
                    pauseButton.setVisible(false);
                    stopButton.setVisible(false);
                    if (MainFunctions.stopTask.get()==false)
                        runProgressBar.setProgress(0.0);
                }
                plotArea.clear();
                plotArea.plotMeasurement();
            }
          });


        MainFunctions.isCombosUpdated.addListener((observable, oldValue, newValue) -> {
                    if(!newValue) {
                        textFieldNumMeas.setText("");
                        textFieldTimeEstimate.setText("");
                        textFieldWarnSave.setText("");
                    }
                });


        MainFunctions.mainDocument.delayBetweenMeasurements.addListener((observable, oldValue, newValue) -> {
                    delayBetweenMeas.setText(String.valueOf(0.001*newValue.longValue()));
                });
        delayBetweenMeas.setText(String.valueOf(0.001*MainFunctions.mainDocument.delayBetweenMeasurements.get()));


        MainFunctions.mainDocument.numberMeasurementsPerCombo.addListener((observable, oldValue, newValue) -> {
                    measPerPoint.setText(newValue.toString());
                });
        measPerPoint.setText(String.valueOf(MainFunctions.mainDocument.numberMeasurementsPerCombo.get()));


        MainFunctions.mainDocument.currentMeasurementWasLoaded.addListener((observable, oldValue, newValue) -> {
                    // When this property goes from true -> false it means we should prep the continuation
                    if (oldValue && !newValue) {
                        MainFunctions.triggerStop();
                        restartButton.setVisible(MainFunctions.mainDocument.nCombosDone != 0);
                        executeButton.setText("Continue");
                        // Should also trigger a plot here!
                    }
                });


        tabConfigure.setDisable(true);
        tabRun.setDisable(true);
        tabDisplay.setDisable(true);

        // -- configuration text fields --
        // Set default values in the box
        //delayBetweenMeas.setText(String.valueOf(0.001*MainFunctions.mainDocument.delayBetweenMeasurements.get()));
        //measPerPoint.setText(String.valueOf(MainFunctions.mainDocument.numberMeasurementsPerCombo));
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
        PVscanList = FXCollections.observableArrayList();
        pvTable.setItems(MainFunctions.mainDocument.pvChannels);

        pvNameColumn.setCellValueFactory(new PropertyValueFactory<>("channelName"));

        readColumnSelect.setCellFactory(CheckBoxTableCell.forTableColumn((Integer param) -> {
            logger.log(Level.FINEST, "Selection trigger for {0}", MainFunctions.mainDocument.pvChannels.get(param));
            if (MainFunctions.mainDocument.pvChannels.get(param).getIsRead()) {
                MainFunctions.actionAddPV(MainFunctions.mainDocument.pvChannels.get(param), true, false);
                checkSufficientParams();
            } else {
                MainFunctions.actionRemovePV(MainFunctions.mainDocument.pvChannels.get(param), true, false);
                checkSufficientParams();
            }
            return MainFunctions.mainDocument.pvChannels.get(param).isReadProperty();
        }));
        readColumnSelect.setCellValueFactory((CellDataFeatures<ChannelWrapper, Boolean> param) -> param.getValue().isReadProperty());

        scanColumnSelect.setCellFactory(CheckBoxTableCell.forTableColumn((Integer param) -> {
            logger.log(Level.FINEST, "Selection trigger for {0}", MainFunctions.mainDocument.pvChannels.get(param));
            if (MainFunctions.mainDocument.pvChannels.get(param).getIsScanned()) {
                logger.log(Level.FINEST, "Will scan {0}",MainFunctions.mainDocument.pvChannels.get(param).getChannel().channelName());
                MainFunctions.mainDocument.pvChannels.get(param).setInstance();
                if (MainFunctions.actionAddPV(MainFunctions.mainDocument.pvChannels.get(param), false, true)) {
                    if (!PVscanList.contains(MainFunctions.mainDocument.pvChannels.get(param))) {
                        PVscanList.add(MainFunctions.mainDocument.pvChannels.get(param));
                        PVscanList.sort(ChannelWrapper::compareTo);
                        MainFunctions.isCombosUpdated.set(false);
                        MainFunctions.mainDocument.setHasChanges(true);
                    }
                }
                checkSufficientParams();
            }
            else {
                logger.log(Level.FINEST, "Will remove {0}",MainFunctions.mainDocument.pvChannels.get(param).getChannel().channelName());
                //MainFunctions.actionRemovePV(MainFunctions.mainDocument.pvWriteables.get(param), false, true);
                if(PVscanList.removeAll(MainFunctions.mainDocument.pvChannels.get(param))) {
                    clearAllConstraints();
                    MainFunctions.isCombosUpdated.set(false);
                }
                checkSufficientParams();
            }
            return MainFunctions.mainDocument.pvChannels.get(param).isScannedProperty();
        }));
        scanColumnSelect.setCellValueFactory((CellDataFeatures<ChannelWrapper, Boolean> param) -> param.getValue().isScannedProperty());

        // Ideally should be able to select multiple channels to remove, but this does not seem to work.
        //readTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //scanTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Why did I need this again??
        pvTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> { if (newValue!=null) pvTable.getSelectionModel().clearSelection();});

    }

    private void initializeExpertOptions() {
        // Initialize extra options for the run
        runOptions = FXCollections.observableArrayList();

        // Add an option to not return back to original settings after scan (default is to return back)
        SimpleOption returnHome = new SimpleOption("Return to initial setting after scan", true);
        returnHome.isSelectedProperty().addListener((observable, oldValue, newValue) -> {
            MainFunctions.returnHome.set(newValue);
        });
        runOptions.add(returnHome);

        // Include the initial settings of each PV in the scan (as first and last measurement point)
        SimpleOption includeInitial = new SimpleOption("Scan initial settings as first&last point", true);
        includeInitial.isSelectedProperty().addListener((observable, oldValue, newValue) -> {
            MainFunctions.mainDocument.includeInitialSettings.set(newValue);
            MainFunctions.isCombosUpdated.set(false);
        });
        runOptions.add(includeInitial);


        expertOptions.setItems(runOptions);
        expertOptionDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        expertOptionSelected.setCellFactory(CheckBoxTableCell.forTableColumn((Integer param) -> {
            SimpleOption thisOption = expertOptions.getItems().get(param);
            logger.log(Level.FINER, "Selected to flip \"{0}\", is now {1}", new Object[]{thisOption.getDescription().toString(), thisOption.isSelectedProperty().getValue()} );

            return thisOption.isSelectedProperty();
        }));
        expertOptionSelected.setCellValueFactory((CellDataFeatures<SimpleOption, Boolean> param) -> param.getValue().isSelectedProperty());
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
                MainFunctions.mainDocument.getDataSets().entrySet().forEach(dataSet -> measurements.add(dataSet.getKey()));
                if (measurements.size() >0 )
                    tabDisplay.setDisable(false);
                logger.log(Level.FINER, "Analysis list updated");
            }
          );
    }

    /**
     * Check if we have selected enough parameters to do a scan
     *
     * @return true if we have at least one parameter listed to scan
     */
    private boolean checkSufficientParams() {
        boolean sufficient = !(PVscanList.isEmpty());
        if ( sufficient ) {
            tabConfigure.setDisable(false);
            tabRun.setDisable(false);
        } else {
            tabConfigure.setDisable(true);
            tabRun.setDisable(true);
        }
        return sufficient;
    }
}
