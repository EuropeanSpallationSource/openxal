/*
 * Copyright (C) 2017 European Spallation Source ERIC
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
package xal.app.trajectorycorrection;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.rbac.AccessDeniedException;
import xal.rbac.Credentials;
import xal.rbac.RBACException;
import xal.rbac.RBACLogin;
import xal.rbac.RBACSubject;
import xal.smf.Accelerator;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

public class FXMLController implements Initializable {

    //Menu
    @FXML
    private MenuItem exitMenu;
    @FXML
    private MenuItem menuItemLoad;
    @FXML
    private MenuItem menuItemSave;
    @FXML
    private MenuItem menuItemDelete;
    @FXML
    private MenuItem menuItemPlot;

    //radio buttons & groups   
    @FXML
    private ToggleGroup groupCorrection;
    @FXML
    private RadioButton radiobuttonCorrectSVD;
    @FXML
    private RadioButton radiobuttonCorrect1to1;
    @FXML
    private ToggleGroup groupCorrectionPlane;
    @FXML
    private RadioButton radioButtonVertical;
    @FXML
    private RadioButton radioButtonHorizontal;
    @FXML
    private RadioButton radioButtonHorVer;

    @FXML
    private Button buttonMeasureResponse1to1;
    @FXML
    private Button buttonMeasureResponseSVD;
    @FXML
    private Button buttonCalcCorrectSVD;
    @FXML
    private Button buttonPlotRefTraj;
    @FXML
    private Button buttonSVDfromModel;
    @FXML
    private Button buttonVerifyMatrixSVD;
    @FXML
    private Button button1to1fromModel;
    @FXML
    private Button buttonVerifyResponse1to1;
    @FXML
    private Button buttonCorrectTrajectory;

    //Text field & labels
    @FXML
    private TextField textFieldSingularValCut;
    @FXML
    private TextArea textArea1to1;
    @FXML
    private TextArea textAreaSVD;
    @FXML
    private Label labelProgressCorrection;
    @FXML
    private TextField textFieldCorrectionFactor;

    //progess bar
    @FXML
    private ProgressBar progressBarCorrection;

    //others
    @FXML
    private ComboBox<URL> comboBoxRefTrajectory;
    @FXML
    private ListView<CorrectionBlock> listViewBlockDefinition;
    @FXML
    private ListView<CorrectionBlock> listViewBlockSelection;

    //Variables
    public Accelerator accl = xal.smf.data.XMLDataManager.loadDefaultAccelerator();
    public TrajectoryArray DisplayTraj = new TrajectoryArray();//Trajectory to be displayed on the plot
    public ObservableList<CorrectionBlock> CorrectionElements = FXCollections.observableArrayList();  //List of defined Blocks
    public ObservableList<CorrectionBlock> CorrectionElementsSelected = FXCollections.observableArrayList();  //List selected blocks (can be used in correction)
    private final ObservableList<URL> refTrajData = FXCollections.observableArrayList();// holds info about reference trajectories   
    private double trajectoryLimit = 0.5;
    private double steererLimit = 0.002;
    private boolean abortFlag = false;
    @FXML
    private ToggleGroup groupModel;
    @FXML
    private AnchorPane mainAnchor1;
    @FXML
    private ToolBar bottomBar;
    @FXML
    private Label labelExpert;
    @FXML
    private Button buttonAbort;
    @FXML
    private Button buttonCheckPair;
    @FXML
    private Tab tabSVD;
    @FXML
    private Tab tab1to1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //initialize and connect BPMs
        DisplayTraj.initBPMs(accl);
        
        //initalize horizontal correctors 
        accl.getAllNodesOfType("DCH").parallelStream().forEach(hc -> {
            if (!(hc.getChannel("fieldSet").isConnected())){
                hc.getChannel("fieldSet").connectAndWait(1.0);
            } 
        });
        
        //initalize vertical correctors 
        accl.getAllNodesOfType("DCV").parallelStream().forEach(hv -> {
            if (!(hv.getChannel("fieldSet").isConnected())){
                hv.getChannel("fieldSet").connectAndWait(1.0);
            } 
        });
        
        //Set elements not visible at start
        labelProgressCorrection.setVisible(false);
        progressBarCorrection.setVisible(false);

        //set css id for listview
        listViewBlockDefinition.setId("listBlockDefinition");
        listViewBlockSelection.setId("listBlockSelection");

        //Sort elements
        CorrectionElements.sort((ele1, ele2) -> Double.compare(ele1.getBlockBPM().get(0).getSDisplay(), ele2.getBlockBPM().get(0).getSDisplay()));
        CorrectionElementsSelected.sort((ele1, ele2) -> Double.compare(ele1.getBlockBPM().get(0).getSDisplay(), ele2.getBlockBPM().get(0).getSDisplay()));

        listViewBlockDefinition.setItems(CorrectionElements);
        listViewBlockDefinition.setCellFactory(listview -> {
            return new ListCell<CorrectionBlock>() {
                @Override
                public void updateItem(CorrectionBlock item, boolean empty) {
                    super.updateItem(item, empty);
                    textProperty().unbind();
                    if (item != null) {
                        textProperty().bind(item.blockNameProperty());
                    } else {
                        setText(null);
                    }
                }
            };
        }
        );

        listViewBlockDefinition.setEditable(true);

        listViewBlockDefinition.setOnEditCommit((ListView.EditEvent<CorrectionBlock> t) -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("A Block with this name already exists!");
            CorrectionElements.forEach(block -> {
                if (block.getblockName().equals(t.getNewValue().getblockName())) {
                    alert.show();
                }
            });
            CorrectionElementsSelected.forEach(block -> {
                if (block.getblockName().equals(t.getNewValue().getblockName())) {
                    alert.show();
                }
            });
        });

        listViewBlockSelection.setItems(CorrectionElementsSelected);
        listViewBlockSelection.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                if (newSelection.isOkSVD()) {
                    tabSVD.setGraphic(new ImageView(this.getClass().getResource("/pictures/ok.png").toString()));
                } else {
                    tabSVD.setGraphic(new ImageView(this.getClass().getResource("/pictures/error.png").toString()));
                }
                if (newSelection.isOk1to1()) {
                    tab1to1.setGraphic(new ImageView(this.getClass().getResource("/pictures/ok.png").toString()));
                } else {
                    tab1to1.setGraphic(new ImageView(this.getClass().getResource("/pictures/error.png").toString()));
                }
            }
        });

        //tab1to1.contentProperty().addListener(listener);
        //listViewBlockSelection.setOnMouseClicked(new EventHandler<MouseEvent>() {
        //    @Override
        //    public void handle(MouseEvent event) {
        //        CorrectionBlock item = listViewBlockSelection.getSelectionModel().getSelectedItem();                
        //        if (item.isOkSVD()){
        //           tabSVD.setGraphic(new ImageView(this.getClass().getResource("/pictures/ok.png").toString()));
        //        } else {
        //           tabSVD.setGraphic(new ImageView(this.getClass().getResource("/pictures/error.png").toString())); 
        //        }
        //        if (item.isOk1to1()){
        //           tab1to1.setGraphic(new ImageView(this.getClass().getResource("/pictures/ok.png").toString()));
        //        } else {
        //           tab1to1.setGraphic(new ImageView(this.getClass().getResource("/pictures/error.png").toString())); 
        //        }
        //    }               
        //});      
        listViewBlockSelection.setCellFactory(lv -> new ListCell<CorrectionBlock>() {
            @Override
            protected void updateItem(CorrectionBlock item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getblockName());
                    if (item.isOkSVD() && item.isOk1to1()) {
                        setGraphic(new ImageView(this.getClass().getResource("/pictures/ok.png").toString()));
                    } else {
                        setGraphic(new ImageView(this.getClass().getResource("/pictures/error.png").toString()));
                    }
                }
            }
        });

        listViewBlockSelection.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends CorrectionBlock> observable, CorrectionBlock oldValue, CorrectionBlock newValue) -> {
            List<xal.smf.impl.BPM> BPMList = new ArrayList<>();
            int maxBPM = 0;
            BPMList.addAll(accl.getAllNodesOfType("BPM"));
            try {
                DisplayTraj.readTrajectory(BPMList);
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (CorrectionElementsSelected.contains(newValue)) {
                if (newValue.isOk1to1()) {
                    buttonCheckPair.setDisable(false);
                    //buttonMeasureResponse1to1.setDisable(false);
                    //button1to1fromModel.setDisable(false);
                    buttonVerifyResponse1to1.setDisable(false);
                } else {
                    buttonCheckPair.setDisable(false);
                    //buttonMeasureResponse1to1.setDisable(true);
                    //button1to1fromModel.setDisable(true);
                    buttonVerifyResponse1to1.setDisable(true);
                }
                if (newValue.isOkSVD()) {
                    buttonSVDfromModel.setDisable(false);
                    buttonVerifyMatrixSVD.setDisable(false);
                    buttonCalcCorrectSVD.setDisable(false);
                    if (Math.abs(DisplayTraj.getXmax()) < trajectoryLimit && Math.abs(DisplayTraj.getYmax()) < trajectoryLimit) {
                        buttonMeasureResponseSVD.setDisable(true);
                    } else {
                        buttonMeasureResponseSVD.setDisable(false);
                    }
                } else {
                    if (Math.abs(DisplayTraj.getXmax()) < trajectoryLimit && Math.abs(DisplayTraj.getYmax()) < trajectoryLimit) {
                        buttonMeasureResponseSVD.setDisable(true);
                    } else {
                        buttonMeasureResponseSVD.setDisable(false);
                    }
                    buttonSVDfromModel.setDisable(false);
                    buttonVerifyMatrixSVD.setDisable(true);
                    buttonCalcCorrectSVD.setDisable(true);
                }
            } else {
                buttonCheckPair.setDisable(true);
                //buttonMeasureResponse1to1.setDisable(true);
                //button1to1fromModel.setDisable(true);
                //buttonVerifyResponse1to1.setDisable(true);
                buttonMeasureResponseSVD.setDisable(true);
                buttonSVDfromModel.setDisable(true);
                buttonVerifyMatrixSVD.setDisable(true);
                buttonCalcCorrectSVD.setDisable(true);
            }
        });

        //Load reference and zero trajectories
        comboBoxRefTrajectory.setCellFactory((ListView<URL> fileName) -> {
            ListCell cell = new ListCell<URL>() {
                @Override
                protected void updateItem(URL item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(new File(item.getFile()).getName());
                    }
                }
            };
            return cell;
        });

        //Load reference and zero trajectories
        refTrajData.add(this.getClass().getResource("/zerotrajectory/ZeroTrajectory.xml"));

        //populate the ComboBox element
        comboBoxRefTrajectory.setItems(refTrajData);
        comboBoxRefTrajectory.setValue(refTrajData.get(0));

        //read new reference trajectory file
        try {
            DisplayTraj.readReferenceTrajectory(accl, comboBoxRefTrajectory.getSelectionModel().getSelectedItem());
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //load configuration file (config.xml)
    }

    //handles table context menu for saving a new reference
    @FXML
    public void handleTrajectoryMenuSave(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Trajectory File");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showSaveDialog(null);
        URL urlselectedfile = null;
        try {
            urlselectedfile = selectedFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (urlselectedfile  != null) {
            if (!refTrajData.contains(urlselectedfile)) {
                refTrajData.add(urlselectedfile);                
            }
            try {
                //Save Trajectory of the whole machine
                DisplayTraj.saveTrajectory(accl, urlselectedfile);
                comboBoxRefTrajectory.setValue(urlselectedfile);
            } catch (ConnectionException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GetException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //handles table context menu for loading a new reference orbit
    @FXML
    public void handleTrajectoryMenuLoad(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Trajectory File");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                refTrajData.add(selectedFile.toURI().toURL());
                comboBoxRefTrajectory.setValue(selectedFile.toURI().toURL());
            } catch (MalformedURLException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //handles table context menu for deleting a entry (doesn;t allow deleting the zero orbit)
    @FXML
    public void handleTrajectoryMenuDelete(ActionEvent event) {
        if (refTrajData.get(0).equals(comboBoxRefTrajectory.getValue())) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("You cannot delete the Zero file. Please select another one.");
            alert.show();
        } else {
            refTrajData.remove(comboBoxRefTrajectory.getValue());
            comboBoxRefTrajectory.setValue(refTrajData.get(0));
        }
    }

    @FXML
    private void handleMenuExit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleTrajectoryShow(ActionEvent event) throws IOException {
        Stage stage;
        Parent root;
        URL url = null;
        String sceneFile = "/fxml/PopUpPlot.fxml";
        try {
            stage = new Stage();
            url = getClass().getResource(sceneFile);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(sceneFile));
            root = loader.load();
            root.getStylesheets().add("/styles/Styles.css");
            stage.setScene(new Scene(root));
            stage.setTitle("View Reference Trajectory");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(comboBoxRefTrajectory.getScene().getWindow());
            PopUpPlotController loginController = loader.getController();
            //setup a BPM list to show
            final List<xal.smf.impl.BPM> BPMList = new ArrayList<>();
            BPMList.addAll(accl.getAllNodesOfType("BPM"));
            try {
                DisplayTraj.readTrajectory(BPMList);
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            //read new reference trajectory file
            try {
                DisplayTraj.readReferenceTrajectory(accl, comboBoxRefTrajectory.getSelectionModel().getSelectedItem());
            } catch (IOException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            loginController.updatePlot(DisplayTraj);
            loginController.loggedInProperty().addListener((obs, wasLoggedIn, isNowLoggedIn) -> {
                if (isNowLoggedIn) {
                    stage.close();
                }
            });
            stage.showAndWait();
        } catch (IOException ex) {
            System.out.println("Exception on FXMLLoader.load()");
            System.out.println("  * url: " + url);
            System.out.println("  * " + ex);
            System.out.println("    ----------------------------------------\n");
            throw ex;
        }

    }

    @FXML
    private void handleCheckPairs(ActionEvent event) {
        int blockIndex = listViewBlockSelection.getSelectionModel().getSelectedIndex();

        if (listViewBlockSelection.getSelectionModel().getSelectedItem() != null) {
            CorrectionElementsSelected.get(blockIndex).check1to1CorrectionPairs(listViewBlockDefinition.getScene().getWindow(), accl);
            //print pairs to text area
            textArea1to1.clear();
            textArea1to1.setText("Horizontal pairs: \n");
            CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getHC().keySet().stream().forEach(bpm -> textArea1to1.setText(textArea1to1.getText() + bpm.toString() + " : " + CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getHC().get(bpm).toString() + "\n"));
            textArea1to1.setText(textArea1to1.getText() + "Vertical pairs: \n");
            CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getVC().keySet().stream().forEach(bpm -> textArea1to1.setText(textArea1to1.getText() + bpm.toString() + " : " + CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getVC().get(bpm).toString() + "\n"));
        }
        if (CorrectionElementsSelected.get(blockIndex).isOk1to1()) {
            //button1to1fromModel.setDisable(false);
            //buttonMeasureResponse1to1.setDisable(false);
            buttonVerifyResponse1to1.setDisable(false);
            tab1to1.setGraphic(new ImageView(getClass().getResource("/pictures/ok.png").toString()));
        } else {
            //button1to1fromModel.setDisable(false);
            //buttonMeasureResponse1to1.setDisable(false);
            buttonVerifyResponse1to1.setDisable(true);
            tab1to1.setGraphic(new ImageView(getClass().getResource("/pictures/error.png").toString()));

        }
        listViewBlockSelection.refresh();

    }

    @FXML
    private void handleMeasureResponse1to1(ActionEvent event) {

        TextInputDialog dialog = new TextInputDialog("0.002");
        dialog.setTitle("Set Field Variation");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Corrector strength (T.m) step:");
        Optional<String> result = dialog.showAndWait();
        double resultval = 0.002;
        if (result.isPresent()) {
            resultval = Double.parseDouble(result.get());
            if (resultval <= 0.0 || resultval > 0.01) {
                resultval = 0.002;
            }
        }
        int blockIndex = listViewBlockSelection.getSelectionModel().getSelectedIndex();

        final double Dk = resultval;
        Task<Void> task;
        task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                int progress = 0;
                int total = CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getHC().size() + CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getVC().size();
                //make horizontal calibration for each BPM
                for (xal.smf.impl.BPM bpm : CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getHC().keySet()) {
                    CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getHCalibration(bpm, Dk);
                    //update progressbar
                    progress++;
                    updateProgress(progress, total);
                }

                //make vertical calibration for each BPM
                for (xal.smf.impl.BPM bpm : CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getVC().keySet()) {
                    CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getVCalibration(bpm, Dk);
                    //update progressbar
                    progress++;
                    updateProgress(progress, total);
                }

                CorrectionElementsSelected.get(blockIndex).setOk1to1(true);
                //when the scan finishes set the label and progress bar to zero
                labelProgressCorrection.setVisible(false);
                progressBarCorrection.setVisible(false);
                updateProgress(0, total);
                textArea1to1.setText("RESPONSE MEASUREMENT: Finished!");
                listViewBlockSelection.refresh();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        tab1to1.setGraphic(new ImageView(getClass().getResource("/pictures/ok.png").toString()));
                    }
                });
                //buttonCheckPair.setDisable(false);
                //buttonMeasureResponse1to1.setDisable(false);
                //button1to1fromModel.setDisable(false);
                buttonVerifyResponse1to1.setDisable(false);

                return null;
            }
        ;

        };
 
        Thread calibrate = new Thread(task);
        calibrate.setDaemon(true); // thread will not prevent application shutdown  
        if (result.isPresent()) {
            labelProgressCorrection.setVisible(true);
            progressBarCorrection.setVisible(true);
            labelProgressCorrection.setText("Aquiring BPM responses");
            progressBarCorrection.progressProperty().bind(task.progressProperty());
            calibrate.start();
        }

    }

    private void correct1to1() {

        Task<Void> task;
        task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                double DeltaK = 0.0;
                double val = 0.0;
                double correctFactor = Double.parseDouble(textFieldCorrectionFactor.getText()) / 100;
                int total = 0;
                int step = 0;
                CorrectionMatrix CorrectTraj = new CorrectionMatrix();

                for (CorrectionBlock block : CorrectionElementsSelected) {
                    CorrectTraj = block.getCorrection1to1();

                    if (radioButtonHorizontal.isSelected()) {
                        total += CorrectTraj.getHC().size();
                    } else if (radioButtonVertical.isSelected()) {
                        total += CorrectTraj.getVC().size();
                    } else if (radioButtonHorVer.isSelected()) {
                        total += CorrectTraj.getVC().size() + CorrectTraj.getHC().size();
                    }
                }

                for (CorrectionBlock block : CorrectionElementsSelected) {
                    CorrectTraj = block.getCorrection1to1();

                    List<xal.smf.impl.BPM> BPMList = block.getBlockBPM();

                    //reads ref trajectory
                    DisplayTraj.readReferenceTrajectory(accl, comboBoxRefTrajectory.getSelectionModel().getSelectedItem());
                    labelProgressCorrection.setText("Correcting Trajectory: " + block.getblockName());
                    //correct trajectory
                    for (xal.smf.impl.BPM item : BPMList) {
                        if (radioButtonHorizontal.isSelected() || radioButtonHorVer.isSelected()) {
                            if (CorrectTraj.HC.containsKey(item) && !abortFlag) {
                                val = CorrectTraj.HC.get(item).getField();
                                DeltaK = correctFactor * CorrectTraj.calcHCorrection(item, DisplayTraj.XRef.get(item));
                                val = val + DeltaK;
                                CorrectTraj.HC.get(item).setField(val);
                                Thread.sleep(2000);
                                step++;
                                updateProgress(step, total);
                            }
                        }
                        if (radioButtonHorVer.isSelected() || radioButtonVertical.isSelected()) {
                            if (CorrectTraj.VC.containsKey(item) && !abortFlag) {
                                val = CorrectTraj.VC.get(item).getField();
                                DeltaK = correctFactor * CorrectTraj.calcVCorrection(item, DisplayTraj.YRef.get(item));
                                val = val + DeltaK;
                                CorrectTraj.VC.get(item).setField(val);
                                Thread.sleep(2000);
                                step++;
                                updateProgress(step, total);
                            }
                        }
                    }
                }
                //when the scan finishes set the label and progress bar to zero
                labelProgressCorrection.setVisible(false);
                progressBarCorrection.setVisible(false);
                updateProgress(0, total);

                return null;
            }
        ;

        };
 
        labelProgressCorrection.setVisible(true);
        progressBarCorrection.setVisible(true);
        labelProgressCorrection.setText("Correcting Trajectory");
        progressBarCorrection.progressProperty().bind(task.progressProperty());
        Thread correct = new Thread(task);
        correct.setDaemon(true); // thread will not prevent application shutdown  
        correct.start();

    }

    @FXML
    private void handleMeasureResponseSVD(ActionEvent event) {

        int blockIndex = listViewBlockSelection.getSelectionModel().getSelectedIndex();

        if (!listViewBlockSelection.getSelectionModel().getSelectedItem().getblockName().isEmpty()) {

            TextInputDialog dialog = new TextInputDialog("0.002");
            dialog.setTitle("Set Field Variation");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter Corrector strength (T.m) step:");
            Optional<String> result = dialog.showAndWait();
            double resultval = 0.002;
            if (result.isPresent()) {
                resultval = Double.parseDouble(result.get());
                if (resultval <= 0.0 || resultval > 0.01) {
                    resultval = 0.002;
                }
            }

            final double Dk = resultval;

            //print knobs
            textAreaSVD.clear();

            //CorrectionElementsSelected.get(blockName).initializeSVDCorrection(accl);
            textAreaSVD.setText(textAreaSVD.getText() + CorrectionElementsSelected.get(blockIndex).getblockName() + "\n");
            textAreaSVD.setText(textAreaSVD.getText() + "Horizontal Correctors: \n");
            CorrectionElementsSelected.get(blockIndex).getBlockHC().forEach(hcorr -> textAreaSVD.setText(textAreaSVD.getText() + hcorr.toString() + "; "));
            textAreaSVD.setText(textAreaSVD.getText() + "\n");
            textAreaSVD.setText(textAreaSVD.getText() + "Vertical correctors: \n");
            CorrectionElementsSelected.get(blockIndex).getBlockVC().forEach(vcorr -> textAreaSVD.setText(textAreaSVD.getText() + vcorr.toString() + "; "));
            textAreaSVD.setText(textAreaSVD.getText() + "\n");

            Task<Void> task;
            task = new Task<Void>() {

                @Override
                protected Void call() throws Exception {

                    int progress = 0;
                    CorrectionElementsSelected.get(blockIndex).getCorrectionSVD().measureTRMHorizontal(Dk);
                    updateProgress(1, 2);
                    CorrectionElementsSelected.get(blockIndex).getCorrectionSVD().measureTRMVertical(Dk);
                    CorrectionElementsSelected.get(blockIndex).setOkSVD(true);
                    progress++;
                    updateProgress(2, 2);

                    //when the scan finishes set the label and progress bar to zero
                    labelProgressCorrection.setVisible(false);
                    progressBarCorrection.setVisible(false);
                    updateProgress(0, 2);
                    textAreaSVD.setText("RESPONSE MEASUREMENT: Finished!");
                    buttonVerifyMatrixSVD.setDisable(false);
                    buttonCalcCorrectSVD.setDisable(false);
                    listViewBlockSelection.refresh();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            tabSVD.setGraphic(new ImageView(getClass().getResource("/pictures/ok.png").toString()));
                        }
                    });
                    try {
                        DisplayTraj.readTrajectory(accl.getAllNodesOfType("BPM"));
                    } catch (ConnectionException | GetException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    buttonSVDfromModel.setDisable(false);
                    buttonVerifyMatrixSVD.setDisable(false);
                    buttonCalcCorrectSVD.setDisable(false);
                    if (Math.abs(DisplayTraj.getXmax()) < trajectoryLimit && Math.abs(DisplayTraj.getYmax()) < trajectoryLimit) {
                        buttonMeasureResponseSVD.setDisable(true);
                    } else {
                        buttonMeasureResponseSVD.setDisable(false);
                    }

                    return null;
                }
            ;

            };

            Thread calibrate = new Thread(task);
            calibrate.setDaemon(true); // thread will not prevent application shutdown  
            progressBarCorrection.progressProperty().bind(task.progressProperty());
            if (result.isPresent()) {
                labelProgressCorrection.setVisible(true);
                progressBarCorrection.setVisible(true);
                labelProgressCorrection.setText("Measuring response matrix...");
                calibrate.start();
            }
        } else {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Please select a correction block in the Selected column to measure.");
            alert.show();
        }

    }

    @FXML
    private void handleCalculateCorrectionSVD(ActionEvent event) throws IOException {

        double[] kickH;
        double[] kickV;
        double[] singVal;
        double svCut;
        int total = 0;
        double correctionFactor;
        int i = 0;

        correctionFactor = Double.parseDouble(textFieldCorrectionFactor.getText()) / 100;

        textAreaSVD.clear();

        for (CorrectionBlock block : CorrectionElementsSelected) {
            CorrectionSVD matrix = new CorrectionSVD();
            matrix = block.getCorrectionSVD();
            //reads a new trajectory
            try {
                DisplayTraj.readTrajectory(matrix.BPM);
                DisplayTraj.readReferenceTrajectory(accl, comboBoxRefTrajectory.getSelectionModel().getSelectedItem());
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            kickH = matrix.calculateHCorrection(DisplayTraj);
            kickV = matrix.calculateVCorrection(DisplayTraj);

            textAreaSVD.setText(textAreaSVD.getText() + "Horizontal Corrector strenghts \n");
            i = 0;
            for (xal.smf.impl.HDipoleCorr hcorr : matrix.HC) {
                textAreaSVD.setText(textAreaSVD.getText() + hcorr.toString() + " : " + String.format("%.5f", correctionFactor * kickH[i]) + "\n");
                i++;
            }
            textAreaSVD.setText(textAreaSVD.getText() + "Vertical Corrector strenghts \n");
            i = 0;
            for (xal.smf.impl.VDipoleCorr vcorr : matrix.VC) {
                textAreaSVD.setText(textAreaSVD.getText() + vcorr.toString() + " : " + String.format("%.5f", correctionFactor * kickV[i]) + "\n");
                i++;
            }

            singVal = matrix.getSigularValuesH();
            textAreaSVD.setText(textAreaSVD.getText() + "Horizontal Singular Values \n");
            for (int j = 0; j < singVal.length; j++) {
                textAreaSVD.setText(textAreaSVD.getText() + String.format("%.3f", singVal[j]) + ";");
            }
            textAreaSVD.setText(textAreaSVD.getText() + "\n");
            for (int j = 0; j < singVal.length; j++) {
                if (singVal[j] > matrix.getCutSVD()) {
                    total += 1;
                }
            }
            textAreaSVD.setText(textAreaSVD.getText() + "Number of horizontal singular values used: " + total + "\n");
            total = 0;
            singVal = matrix.getSigularValuesV();
            textAreaSVD.setText(textAreaSVD.getText() + "Vertical Singular Values \n");
            for (int j = 0; j < singVal.length; j++) {
                textAreaSVD.setText(textAreaSVD.getText() + String.format("%.3f", singVal[j]) + ";");
            }
            textAreaSVD.setText(textAreaSVD.getText() + "\n");
            for (int j = 0; j < singVal.length; j++) {
                if (singVal[j] > matrix.getCutSVD()) {
                    total += 1;
                }
            }
            textAreaSVD.setText(textAreaSVD.getText() + "Number of vertical singular values used: " + total + "\n");

        }

    }

    private void correctSVD() {

        double[] kickH;
        double[] kickV;
        double svCut;
        double correctionFactor;
        List<xal.smf.impl.HDipoleCorr> HC = new ArrayList<>();
        List<xal.smf.impl.VDipoleCorr> VC = new ArrayList<>();
        int i = 0;
        int j = 0;

        correctionFactor = Double.parseDouble(textFieldCorrectionFactor.getText()) / 100;

        labelProgressCorrection.setVisible(true);
        progressBarCorrection.setVisible(true);

        for (CorrectionBlock block : CorrectionElementsSelected) {
            CorrectionSVD matrix = new CorrectionSVD();
            matrix = block.getCorrectionSVD();
            labelProgressCorrection.setText("Correcting Trajectory: " + block.getblockName());
            //reads a new trajectory
            try {
                DisplayTraj.readTrajectory(matrix.BPM);
                try {
                    DisplayTraj.readReferenceTrajectory(accl, comboBoxRefTrajectory.getValue());
                } catch (IOException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            kickH = matrix.calculateHCorrection(DisplayTraj);
            kickV = matrix.calculateVCorrection(DisplayTraj);

            //Check steerer values
            double maxkickH = Math.abs(Arrays.stream(kickH).map(n -> Math.abs(n)).max().orElse(0.0));
            double maxkickV = Math.abs(Arrays.stream(kickV).map(n -> Math.abs(n)).max().orElse(0.0));

            if (maxkickH < steererLimit && maxkickV < steererLimit) {
                double aux = 0;
                i = 0;
                if (radioButtonHorizontal.isSelected() || radioButtonHorVer.isSelected()) {
                    for (xal.smf.impl.HDipoleCorr hcorr : matrix.HC) {
                        if (!abortFlag) {
                            try {
                                aux = hcorr.getField();
                            } catch (ConnectionException | GetException ex) {
                                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            aux = aux + correctionFactor * kickH[i];
                            i++;
                            try {
                                hcorr.setField(aux);
                            } catch (ConnectionException | PutException ex) {
                                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
                j = 0;
                if (radioButtonVertical.isSelected() || radioButtonHorVer.isSelected()) {
                    for (xal.smf.impl.VDipoleCorr vcorr : matrix.VC) {
                        if (!abortFlag) {
                            try {
                                aux = vcorr.getField();
                            } catch (ConnectionException | GetException ex) {
                                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            aux = aux + correctionFactor * kickV[j];
                            j++;
                            try {
                                vcorr.setField(aux);
                            } catch (ConnectionException | PutException ex) {
                                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
            progressBarCorrection.setProgress((CorrectionElementsSelected.indexOf(block) + 1) / CorrectionElementsSelected.size());
        }
        labelProgressCorrection.setVisible(false);
        progressBarCorrection.setVisible(false);
        progressBarCorrection.setProgress(0);

    }

    @FXML
    private void handleContextMenu(ContextMenuEvent event) {

        final MenuItem addBlock = new MenuItem("Add Block");
        final MenuItem loadBlock = new MenuItem("Load Block");
        final MenuItem editBlock = new MenuItem("Edit Selected Block");
        final MenuItem deleteBlock = new MenuItem("Remove Selected Block");
        final MenuItem saveBlock = new MenuItem("Save Selected Block");
        final ContextMenu menu;
        final CorrectionBlock blockItem = listViewBlockDefinition.getSelectionModel().getSelectedItem();

        if (blockItem == null) {
            menu = new ContextMenu(addBlock, loadBlock);
        } else {
            menu = new ContextMenu(addBlock, loadBlock, new SeparatorMenuItem(), editBlock, deleteBlock, saveBlock);
        }

        addBlock.setOnAction((ActionEvent event1) -> {
            Stage stage;
            Parent root;
            URL url = null;
            String sceneFile = "/fxml/CorrectionElementSelection.fxml";
            try {
                stage = new Stage();
                url = getClass().getResource(sceneFile);
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(MainApp.class.getResource(sceneFile));
                root = loader.load();
                root.getStylesheets().add("/styles/Styles.css");
                stage.setScene(new Scene(root));
                stage.setTitle("Elements for Correction: Block Definition");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(listViewBlockDefinition.getScene().getWindow());
                CorrectionElementSelectionController loginController = loader.getController();
                loginController.loggedInProperty().addListener((obs, wasLoggedIn, isNowLoggedIn) -> {
                    if (isNowLoggedIn) {
                        if (loginController.getChangedSelectionList()) {
                            CorrectionBlock newBlock = new CorrectionBlock();
                            newBlock.setBlockBPM(loginController.getBPMSelectionList());
                            newBlock.setBlockHC(loginController.getHCSelectionList());
                            newBlock.setBlockVC(loginController.getVCSelectionList());
                            newBlock.setOk1to1(false);
                            newBlock.setOkSVD(false);
                            CorrectionElements.forEach(block -> {
                                if (block.getblockName().equals(newBlock.getblockName())) {
                                    newBlock.setblockName(newBlock.getblockName() + "_new");
                                }

                            });
                            CorrectionElementsSelected.forEach(block -> {
                                if (block.getblockName().equals(newBlock.getblockName())) {
                                    newBlock.setblockName(newBlock.getblockName() + "_new");
                                }

                            });
                            newBlock.initialize1to1Correction(accl);
                            newBlock.initializeSVDCorrection(accl);
                            CorrectionElements.add(newBlock);
                            CorrectionElements.sort((ele1, ele2) -> Double.compare(ele1.getBlockBPM().get(0).getSDisplay(), ele2.getBlockBPM().get(0).getSDisplay()));
                        }
                        stage.close();
                    }
                });
                loginController.populateElementGrid(accl);
                stage.showAndWait();
            } catch (IOException ex) {
                System.out.println("Exception on FXMLLoader.load()");
                System.out.println("  * url: " + url);
                System.out.println("  * " + ex);
                System.out.println("    ----------------------------------------\n");
                try {
                    throw ex;
                } catch (IOException ex1) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        });

        editBlock.setOnAction((ActionEvent event1) -> {
            Stage stage;
            Parent root;
            URL url = null;
            String sceneFile = "/fxml/CorrectionElementSelection.fxml";
            try {
                stage = new Stage();
                url = getClass().getResource(sceneFile);
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(MainApp.class.getResource(sceneFile));
                root = loader.load();
                root.getStylesheets().add("/styles/Styles.css");
                stage.setScene(new Scene(root));
                stage.setTitle("Elements for Correction: Block Definition");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(listViewBlockDefinition.getScene().getWindow());
                CorrectionElementSelectionController loginController = loader.getController();
                loginController.populateElementGrid(accl);
                loginController.enterElementstoEdit(CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedIndex()));
                loginController.loggedInProperty().addListener((obs, wasLoggedIn, isNowLoggedIn) -> {
                    if (isNowLoggedIn) {
                        if (loginController.getChangedSelectionList()) {
                            CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedIndex()).setBlockBPM(loginController.getBPMSelectionList());
                            CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedIndex()).setBlockHC(loginController.getHCSelectionList());
                            CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedIndex()).setBlockVC(loginController.getVCSelectionList());
                            CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedIndex()).setOk1to1(false);
                            CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedIndex()).setOkSVD(false);
                            CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedIndex()).initialize1to1Correction(accl);
                            CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedIndex()).initializeSVDCorrection(accl);
                            CorrectionElements.sort((ele1, ele2) -> Double.compare(ele1.getBlockBPM().get(0).getSDisplay(), ele2.getBlockBPM().get(0).getSDisplay()));
                        }
                        stage.close();
                    }
                });
                stage.showAndWait();
            } catch (IOException ex) {
                System.out.println("Exception on FXMLLoader.load()");
                System.out.println("  * url: " + url);
                System.out.println("  * " + ex);
                System.out.println("    ----------------------------------------\n");
                try {
                    throw ex;
                } catch (IOException ex1) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        });

        deleteBlock.setOnAction((ActionEvent event1) -> {
            int index = listViewBlockDefinition.getSelectionModel().getSelectedIndex();
            CorrectionBlock removeName = listViewBlockDefinition.getSelectionModel().getSelectedItem();
            if (removeName != null) {
                CorrectionElements.remove(removeName);
                listViewBlockDefinition.getSelectionModel().clearSelection();
            }
        });

        loadBlock.setOnAction((ActionEvent event1) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load new correction block");

            //Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                CorrectionBlock newBlock = new CorrectionBlock();
                newBlock.loadBlock(selectedFile.getAbsoluteFile(), accl);
                CorrectionElements.forEach(block -> {
                    if (block.getblockName().equals(newBlock.getblockName())) {
                        newBlock.setblockName(newBlock.getblockName() + "_new");
                    }

                });
                CorrectionElementsSelected.forEach(block -> {
                    if (block.getblockName().equals(newBlock.getblockName())) {
                        newBlock.setblockName(newBlock.getblockName() + "_new");
                    }

                });
                CorrectionElements.add(newBlock);
                CorrectionElements.sort((ele1, ele2) -> Double.compare(ele1.getBlockBPM().get(0).getSDisplay(), ele2.getBlockBPM().get(0).getSDisplay()));
            }
        });

        saveBlock.setOnAction((ActionEvent event1) -> {
            CorrectionBlock newBlock = new CorrectionBlock();
            newBlock = listViewBlockDefinition.getSelectionModel().getSelectedItem();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save correction block");
            fileChooser.setInitialFileName(newBlock.getblockName());

            //Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
                DataAdaptor trajectoryAdaptor = da.createChild("CorrectionData");
                trajectoryAdaptor.setValue("title", selectedFile.getAbsolutePath());
                trajectoryAdaptor.setValue("date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
                newBlock.saveBlock(trajectoryAdaptor);
                try {
                    da.writeTo(selectedFile.getAbsoluteFile());
                } catch (IOException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        menu.show(listViewBlockDefinition.getScene().getWindow(), event.getScreenX(), event.getScreenY());

    }

    @FXML
    private void handleRemoveSelectedBlock(ActionEvent event) {
        CorrectionBlock removeName = listViewBlockSelection.getSelectionModel().getSelectedItem();
        if (removeName != null) {
            CorrectionElements.add(removeName);
            CorrectionElements.sort((ele1, ele2) -> Double.compare(ele1.getBlockBPM().get(0).getSDisplay(), ele2.getBlockBPM().get(0).getSDisplay()));
            CorrectionElementsSelected.remove(removeName);
            listViewBlockSelection.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void handleSelectedBlock(ActionEvent event) {
        CorrectionBlock includeName = listViewBlockDefinition.getSelectionModel().getSelectedItem();
        if (includeName != null) {
            CorrectionElementsSelected.add(includeName);
            CorrectionElementsSelected.sort((ele1, ele2) -> Double.compare(ele1.getBlockBPM().get(0).getSDisplay(), ele2.getBlockBPM().get(0).getSDisplay()));
            CorrectionElements.remove(includeName);
            listViewBlockDefinition.getSelectionModel().clearSelection();
        }

    }

    @FXML
    private void handleChooseRefTrajectory(ActionEvent event) {
        //read new reference trajectory file
        try {
            DisplayTraj.readReferenceTrajectory(accl, comboBoxRefTrajectory.getSelectionModel().getSelectedItem());
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleCorrectTrajectory(ActionEvent event) {
        boolean correctSVDFlag = true;
        boolean correct1to1Flag = true;

        //enable Abort button
        //buttonAbort.setDisable(false);
        //check trajectory
        final List<xal.smf.impl.BPM> BPMList = new ArrayList<>();
        final int maxBPM = 0;
        BPMList.addAll(accl.getAllNodesOfType("BPM"));
        try {
            DisplayTraj.readTrajectory(BPMList);
        } catch (ConnectionException | GetException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (CorrectionBlock blockName : CorrectionElementsSelected) {
            if (!blockName.isOkSVD() || Math.abs(DisplayTraj.getXmax()) > trajectoryLimit || Math.abs(DisplayTraj.getYmax()) > trajectoryLimit) {
                System.out.print(blockName.getblockName() + ":" + !blockName.isOkSVD() + " " + Math.abs(DisplayTraj.getXmax()) + " " + Math.abs(DisplayTraj.getYmax()));
                correctSVDFlag = false;
            }
            if (!blockName.isOk1to1()) {
                correct1to1Flag = false;
            }
        }

        if (radiobuttonCorrectSVD.isSelected() && correctSVDFlag) {
            correctSVD();
        } else if (radiobuttonCorrect1to1.isSelected() && correct1to1Flag) {
            correct1to1();
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning Dialog");
            alert.setHeaderText(null);
            alert.setGraphic(new ImageView(this.getClass().getResource("/pictures/warning.png").toString()));
            alert.setContentText("One (or more) reponse matrix is not ready for correction or the trajectory limit is exceeded! Please check the block status and trajectory before correction.");
            alert.show();
        }

        //disable Abort button
        //buttonAbort.setDisable(true);
        abortFlag = false;

    }

    @FXML
    private void handleMeasureResponseSVDfromModel(ActionEvent event) {
        CorrectionBlock blockName = listViewBlockSelection.getSelectionModel().getSelectedItem();

        if (blockName != null) {

            TextInputDialog dialog = new TextInputDialog("0.002");
            dialog.setTitle("Set Field Variation");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter Corrector strength (T.m) step:");
            Optional<String> result = dialog.showAndWait();
            double resultval = 0.002;
            if (result.isPresent()) {
                resultval = Double.parseDouble(result.get());
                if (resultval <= 0.0 || resultval > 0.01) {
                    resultval = 0.002;
                }
            }

            final double Dk = resultval;

            //print knobs
            textAreaSVD.clear();

            //CorrectionElementsSelected.get(blockName).initializeSVDCorrection(accl);
            textAreaSVD.setText(textAreaSVD.getText() + blockName.getblockName() + "\n");
            textAreaSVD.setText(textAreaSVD.getText() + "Horizontal Correctors: \n");
            blockName.getBlockHC().forEach(hcorr -> textAreaSVD.setText(textAreaSVD.getText() + hcorr.toString() + "; "));
            textAreaSVD.setText(textAreaSVD.getText() + "\n");
            textAreaSVD.setText(textAreaSVD.getText() + "Vertical correctors: \n");
            blockName.getBlockVC().forEach(vcorr -> textAreaSVD.setText(textAreaSVD.getText() + vcorr.toString() + "; "));
            textAreaSVD.setText(textAreaSVD.getText() + "\n");

            Task<Void> task;
            task = new Task<Void>() {

                @Override
                protected Void call() throws Exception {

                    int progress = 0;
                    RadioMenuItem modelSync = (RadioMenuItem) groupModel.getSelectedToggle();
                    String synchronizationMode = null;
                    if (modelSync != null){
                        synchronizationMode = modelSync.getText().substring(6);
                    } else {
                        synchronizationMode = "DESIGN";
                    }
                    blockName.getCorrectionSVD().calculateTRMHorizontal(Dk,synchronizationMode);
                    updateProgress(1, 2);
                    blockName.getCorrectionSVD().calculateTRMVertical(Dk,synchronizationMode);
                    blockName.setOkSVD(true);
                    progress++;
                    updateProgress(2, 2);

                    //when the scan finishes set the label and progress bar to zero
                    labelProgressCorrection.setVisible(false);
                    progressBarCorrection.setVisible(false);
                    updateProgress(0, 2);
                    textAreaSVD.setText("RESPONSE CALCULATION: Finished!");
                    try {
                        DisplayTraj.readTrajectory(accl.getAllNodesOfType("BPM"));
                    } catch (ConnectionException | GetException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    listViewBlockSelection.refresh();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            tabSVD.setGraphic(new ImageView(getClass().getResource("/pictures/ok.png").toString()));
                        }
                    });
                    buttonSVDfromModel.setDisable(false);
                    buttonVerifyMatrixSVD.setDisable(false);
                    buttonCalcCorrectSVD.setDisable(false);
                    if (Math.abs(DisplayTraj.getXmax()) < trajectoryLimit && Math.abs(DisplayTraj.getYmax()) < trajectoryLimit) {
                        buttonMeasureResponseSVD.setDisable(true);
                    } else {
                        buttonMeasureResponseSVD.setDisable(false);
                    }

                    return null;
                }
            ;

            };

            Thread calibrate = new Thread(task);
            calibrate.setDaemon(true); // thread will not prevent application shutdown  
            progressBarCorrection.progressProperty().bind(task.progressProperty());
            if (result.isPresent()) {
                labelProgressCorrection.setVisible(true);
                progressBarCorrection.setVisible(true);
                labelProgressCorrection.setText("Calculating response matrix...");
                calibrate.start();
            }
        } else {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Please select a correction block in the Selected column to measure.");
            alert.show();
        }

    }

    @FXML
    private void handleMeasureResponse1to1fromModel(ActionEvent event) {

        TextInputDialog dialog = new TextInputDialog("0.002");
        dialog.setTitle("Set Field Variation");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Corrector strength (T.m) step:");
        Optional<String> result = dialog.showAndWait();
        double resultval = 0.002;
        if (result.isPresent()) {
            resultval = Double.parseDouble(result.get());
            if (resultval <= 0.0 || resultval > 0.01) {
                resultval = 0.002;
            }
        }
        int blockIndex = listViewBlockSelection.getSelectionModel().getSelectedIndex();

        final double Dk = resultval;
        Task<Void> task;
        task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                int progress = 0;
                int total = CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getHC().size() + CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getVC().size();
                RadioMenuItem modelSync = (RadioMenuItem) groupModel.getSelectedToggle();
                String synchronizationMode = null;
                if (modelSync != null){
                    synchronizationMode = modelSync.getText().substring(6);
                } else {
                    synchronizationMode = "DESIGN";
                }
                //make horizontal calibration for each BPM
                for (xal.smf.impl.BPM bpm : CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getHC().keySet()) {
                    CorrectionElementsSelected.get(blockIndex).getCorrection1to1().simulHCalibration(bpm, Dk, synchronizationMode);
                    //update progressbar
                    progress++;
                    updateProgress(progress, total);
                }

                //make vertical calibration for each BPM
                for (xal.smf.impl.BPM bpm : CorrectionElementsSelected.get(blockIndex).getCorrection1to1().getVC().keySet()) {
                    CorrectionElementsSelected.get(blockIndex).getCorrection1to1().simulVCalibration(bpm, Dk, synchronizationMode);
                    //update progressbar
                    progress++;
                    updateProgress(progress, total);
                }

                CorrectionElementsSelected.get(blockIndex).setOk1to1(true);

                //when the scan finishes set the label and progress bar to zero
                labelProgressCorrection.setVisible(false);
                progressBarCorrection.setVisible(false);
                updateProgress(0, total);
                textArea1to1.setText("RESPONSE CALCULATION: Finished!");
                listViewBlockSelection.refresh();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        tab1to1.setGraphic(new ImageView(getClass().getResource("/pictures/ok.png").toString()));
                    }
                });
                //buttonCheckPair.setDisable(false);
                //buttonMeasureResponse1to1.setDisable(false);
                //button1to1fromModel.setDisable(false);
                buttonVerifyResponse1to1.setDisable(false);

                return null;
            }
        ;

        };
 
        Thread calibrate = new Thread(task);
        calibrate.setDaemon(true); // thread will not prevent application shutdown  
        if (result.isPresent()) {
            labelProgressCorrection.setVisible(true);
            progressBarCorrection.setVisible(true);
            labelProgressCorrection.setText("Aquiring BPM responses");
            progressBarCorrection.progressProperty().bind(task.progressProperty());
            calibrate.start();
        }

    }

    @FXML
    private void handleVerifySVDResponse(ActionEvent event) {
        Stage stage;
        Parent root;
        URL url = null;
        String sceneFile = "/fxml/PlotVerifyResponse.fxml";
        CorrectionBlock blockName = listViewBlockSelection.getSelectionModel().getSelectedItem();

        try {
            stage = new Stage();
            url = getClass().getResource(sceneFile);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(sceneFile));
            root = loader.load();
            root.getStylesheets().add("/styles/Styles.css");
            stage.setScene(new Scene(root));
            stage.setTitle("Verify SVD reponse for each corrector");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(comboBoxRefTrajectory.getScene().getWindow());
            VerifyResponseController loginController = loader.getController();
            loginController.setCorrectionSVD(blockName.getCorrectionSVD());
            loginController.loggedInProperty().addListener((obs, wasLoggedIn, isNowLoggedIn) -> {
                if (isNowLoggedIn) {
                    stage.close();
                }
            });
            stage.showAndWait();
        } catch (IOException ex) {
            System.out.println("Exception on FXMLLoader.load()");
            System.out.println("  * url: " + url);
            System.out.println("  * " + ex);
            System.out.println("    ----------------------------------------\n");
            //throw ex;
        }
    }

    @FXML
    private void handleVerify1to1Response(ActionEvent event) {
        Stage stage;
        Parent root;
        URL url = null;
        String sceneFile = "/fxml/PlotVerifyResponse.fxml";
        CorrectionBlock blockName = listViewBlockSelection.getSelectionModel().getSelectedItem();

        try {
            stage = new Stage();
            url = getClass().getResource(sceneFile);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(sceneFile));
            root = loader.load();
            root.getStylesheets().add("/styles/Styles.css");
            stage.setScene(new Scene(root));
            stage.setTitle("Verify BPM response");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(comboBoxRefTrajectory.getScene().getWindow());
            VerifyResponseController loginController = loader.getController();
            loginController.setCorrection1to1(blockName.getCorrection1to1());
            loginController.loggedInProperty().addListener((obs, wasLoggedIn, isNowLoggedIn) -> {
                if (isNowLoggedIn) {
                    stage.close();
                }
            });
            stage.showAndWait();
        } catch (IOException ex) {
            System.out.println("Exception on FXMLLoader.load()");
            System.out.println("  * url: " + url);
            System.out.println("  * " + ex);
            System.out.println("    ----------------------------------------\n");
            //throw ex;
        }
    }

    @FXML
    private void handleSetSVDCutValue(ActionEvent event) {
        CorrectionBlock selectedName = listViewBlockSelection.getSelectionModel().getSelectedItem();
        if (selectedName != null) {
            selectedName.getCorrectionSVD().setCutSVD(Double.parseDouble(textFieldSingularValCut.getText()));
        }
    }

    @FXML
    private void handleSetTrajectoryLimit(ActionEvent event) {
        /* RBAC service */
        boolean userData = authenticateWithRBAC();

        if (userData) {
            TextInputDialog dialog = new TextInputDialog(String.format("%.4f", trajectoryLimit));
            dialog.setTitle("Set Trajecotry Maximum Value for SVD");
            dialog.setHeaderText("WARNING! This parameter is for expert use only.");
            dialog.setGraphic(new ImageView(this.getClass().getResource("/pictures/warning.png").toString()));
            dialog.setContentText("Maximum trajectory displacement (mm):");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> trajectoryLimit = Double.parseDouble(name));
            bottomBar.setStyle("-fx-background-color: #ff0000;");
            labelExpert.setVisible(true);
            labelExpert.setText("Trajectory Limit Modified");
        }
    }

    @FXML
    private void handleSetSteererLimit(ActionEvent event) {
        /* RBAC service */
        boolean userData = authenticateWithRBAC();

        if (userData) {
            TextInputDialog dialog = new TextInputDialog(String.format("%.4f", steererLimit));
            dialog.setTitle("Set Steerer Maximum Value");
            dialog.setHeaderText("WARNING! This parameter is for expert use only.");
            dialog.setContentText("Maximum steerer strength (T.m) :");
            dialog.setGraphic(new ImageView(this.getClass().getResource("/pictures/warning.png").toString()));
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> steererLimit = Double.parseDouble(name));
            bottomBar.setStyle("-fx-background-color: #ff0000;");
            labelExpert.setVisible(true);
            labelExpert.setText("Trajectory Limit Modified");
        }
    }

    /**
     * Convention method for authenticating user.
     *
     * <p>
     * If RBACPlugin couldn't be loaded the application will not use RBAC !!!
     *
     * @return true if authentication successful, false if not.
     */
    private boolean authenticateWithRBAC() {

        RBACLogin rbacLogin;
        RBACSubject rbacSubject;
        //RBAC authentication
        try {
            rbacLogin = RBACLogin.newRBACLogin();
        } catch (RuntimeException e) {
            System.err.println("RBAC plugin not found. Continuing without RBAC.");
            return true;
        }

        try {
            // Try to use the local token.
            rbacSubject = rbacLogin.authenticate(null, null);
            System.out.println("Already logged in.");
            if (rbacSubject != null) {
                return true;
            }
        } catch (AccessDeniedException | RBACException e) {
            // Fall to authentication pane
        }

        try {
            System.out.println("Starting authentication.");

            final Credentials credentials = AuthenticationPaneFX.getCredentials();
            if (credentials == null) {
                // User pressed cancel
                System.out.println("Exiting...");
                return false;
            }
            rbacSubject = rbacLogin.authenticate(credentials.getUsername(), credentials.getPassword(), credentials.getPreferredRole(), credentials.getIP());
            System.out.printf("Authentication successful with username %s.\n", credentials.getUsername());
            return (rbacSubject != null);
        } catch (AccessDeniedException e) {
            System.err.printf("Access denied during authentication: %s\n", e.getMessage());
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Access denied");
            alert.setHeaderText("Access denied during authentication");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return false;
        } catch (RBACException e) {
            System.err.printf("Error while trying to authenticate: %s\n", e.getMessage());
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Access denied");
            alert.setHeaderText("Error while trying to authenticate");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return false;
        }
    }

    @FXML
    private void handleOpenTrajDisplayApp(ActionEvent event) {
        try {
            Process processCompile = Runtime.getRuntime().exec("java -jar /Users/nataliamilas/projects/openxal/dist/target/openxal-1.1.2-SNAPSHOT-dist/openxal-1.1.2-SNAPSHOT/apps/openxal.apps.trajectorydisplay2-1.1.2-SNAPSHOT.jar");
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleLoadDefaultAccelerator(ActionEvent event) {
        accl = xal.smf.data.XMLDataManager.loadDefaultAccelerator();
    }

    @FXML
    private void handleLoadAccelerator(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Accelerator");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XAL files (*.xal)", "*.xal");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            accl = xal.smf.data.XMLDataManager.acceleratorWithPath(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void openHelpURL(ActionEvent event) {
        final Hyperlink hyperlink = new Hyperlink("https://confluence.esss.lu.se/display/BPCRS/User+documentation+for+Trajectory+Correction+application");
        HostServices hostServices = (HostServices) this.mainAnchor1.getScene().getWindow().getProperties().get("hostServices");
        hostServices.showDocument(hyperlink.getText());
    }

    @FXML
    private void handleAbortRequest(ActionEvent event) {
        textArea1to1.setText("Correction aborted by the user!");
        textAreaSVD.setText("Correction aborted by the user!");
        abortFlag = true;
    }

    @FXML
    private void handleSaveAppState(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Application State");
        fileChooser.setInitialFileName("TrajectoryCorrectionAppState");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
            DataAdaptor appstateAdaptor = da.createChild("ApplicationState");
            appstateAdaptor.setValue("title", selectedFile.getAbsolutePath());
            appstateAdaptor.setValue("date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
            DataAdaptor generalAdaptor = appstateAdaptor.createChild("GeneralData");
            generalAdaptor.setValue("Trajectory_Limit", trajectoryLimit);
            generalAdaptor.setValue("Steerer_Limit", steererLimit);
            DataAdaptor refTrajectoryAdaptor = appstateAdaptor.createChild("ReferenceTrajectoryData");
            refTrajData.forEach(file -> {
                if (!new File(file.toString()).getName().contains("Zero")) {
                    try {
                        DisplayTraj.saveTrajectory(accl, file, refTrajectoryAdaptor);
                    } catch (ConnectionException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (GetException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            DataAdaptor blockAdaptor = appstateAdaptor.createChild("CorrectionBlockData");
            CorrectionElements.forEach(block -> block.saveBlock(blockAdaptor));
            CorrectionElementsSelected.forEach(block -> block.saveBlock(blockAdaptor));
            try {
                da.writeTo(selectedFile.getAbsoluteFile());
            } catch (IOException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @FXML
    private void handleLoadAppState(ActionEvent event) {
        DataAdaptor readAdp = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Application State");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        CorrectionElements.clear();
        CorrectionElementsSelected.clear();
        refTrajData.forEach(file -> {
            if (!new File(file.toString()).getName().contains("Zero")) {
                refTrajData.remove(file);
            }
        });

        //Show save file dialog
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                readAdp = XmlDataAdaptor.adaptorForFile(selectedFile, false);
                DataAdaptor headerAdaptor = readAdp.childAdaptor("ApplicationState");
                DataAdaptor generalAdaptor = headerAdaptor.childAdaptor("GeneralData");
                trajectoryLimit = generalAdaptor.doubleValue("Trajectory_Limit");
                steererLimit = generalAdaptor.doubleValue("Steerer_Limit");
                XmlDataAdaptor trajectoryAdaptor = (XmlDataAdaptor) headerAdaptor.childAdaptor("ReferenceTrajectoryData");
                trajectoryAdaptor.childAdaptors().forEach(trajFileAdaptor -> {
                    try {
                        refTrajData.add(new URL(trajFileAdaptor.stringValue("title")));
                    }
                    catch (MalformedURLException ex){
                        File file = new File(trajFileAdaptor.stringValue("title"));
                        if (file.exists()) {
                            try {
                                refTrajData.add(file.toURI().toURL());
                            } catch (MalformedURLException ex1) {
                                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                        else {
                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Error Dialog");
                            alert.setHeaderText(null);
                            alert.setContentText("Reference trajectory file doesn't exist.");
                        }
                    }                    
                });
                XmlDataAdaptor blockAdaptor = (XmlDataAdaptor) headerAdaptor.childAdaptor("CorrectionBlockData");
                blockAdaptor.childAdaptors().forEach(chilblockAdaptor -> {
                    CorrectionBlock newBlock = new CorrectionBlock();
                    newBlock.loadBlock(chilblockAdaptor, accl);
                    CorrectionElements.add(newBlock);
                });
                CorrectionElements.sort((ele1, ele2) -> Double.compare(ele1.getBlockBPM().get(0).getSDisplay(), ele2.getBlockBPM().get(0).getSDisplay()));
            } catch (MalformedURLException | XmlDataAdaptor.ParseException | XmlDataAdaptor.ResourceNotFoundException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
