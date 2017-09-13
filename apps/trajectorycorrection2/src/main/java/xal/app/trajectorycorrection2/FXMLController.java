/*
 * FXMLControler.java
 *
 * Created by Natalia Milas on 07.07.2017
 *
 * Copyright (c) 2017 European Spallation Source ERIC
 * Tunavägen 20
 * Lund, Sweden
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package xal.app.trajectorycorrection2;

/**
 * Main Controller Application
 * @author nataliamilas
 * 06-2017
 */


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;

public class FXMLController implements Initializable {
   
    //Creates the Accelerator
    public xal.smf.Accelerator accl = xal.smf.data.XMLDataManager.acceleratorWithPath("/Users/nataliamilas/projects/openxal/site/optics/design/main.xal");
    //public xal.smf.Accelerator accl = xal.smf.data.XMLDataManager.loadDefaultAccelerator();
    public TrajectoryArray DisplayTraj = new TrajectoryArray();//Trajectory to be displayed on the plot
    public Map<String,CorrectionBlock> CorrectionElements = new HashMap<String,CorrectionBlock>();  //List of defined Blocks
    public Map<String,CorrectionBlock> CorrectionElementsSelected = new HashMap<String,CorrectionBlock>();  //List selected blocks (can be used in correction)
    public CorrectionMatrix CorrectTraj;//Stores values for the trajectory correction part (1-to-1 correction)
    public List<CorrectionSVD> CorrectSVDMatrix;//Stores values for the trajectory correction part (SVD correction)
    private boolean progressSVD = false;//progress bar
    private final HashMap<String,String> refTrajData = new HashMap();// holds info about reference trajectories
    private final ObjectProperty<ListCell<String>> dragSource = new SimpleObjectProperty<>();
    private ObservableList<String> selected = FXCollections.observableArrayList();
    private ObservableList<String> defined = FXCollections.observableArrayList();
    
    @FXML
    private MenuItem exitMenu;
    @FXML
    private ToggleGroup group1;
    @FXML
    private ToggleGroup groupSVD;
    @FXML
    private RadioButton radioButtonHor;
    @FXML
    private RadioButton radioButtonVer;
    @FXML
    private RadioButton radioButtonHorVer;
    @FXML
    private Button buttonPairBPMCorrector;
    @FXML
    private Button buttonMeasureResponse1to1;
    @FXML
    private Button buttonCorrect1to1;
    @FXML
    private TextField textFieldCorrFactor1to1;
    @FXML
    private RadioButton radioButtonHorSVD;
    @FXML
    private RadioButton radioButtonVerSVD;
    @FXML
    private RadioButton radioButtonHorVerSVD;
    @FXML
    private Button buttonMeasureResponseSVD;
    @FXML
    private TextField textFieldCorrFactorSVD;
    @FXML
    private TextField textFieldSingularValCut;
    @FXML
    private Button buttonCorrectSVD;
    @FXML
    private Button buttonCalcCorrectSVD;
    @FXML
    private TextArea textArea1to1;
    @FXML
    private TextArea textAreaSVD;
    @FXML
    private ProgressBar progressBarCorrection;
    @FXML
    private Label labelProgressCorrection;
    @FXML
    private MenuItem menuItemLoad;
    @FXML
    private MenuItem menuItemSave;
    @FXML
    private MenuItem menuItemDelete;
    @FXML
    private MenuItem menuItemPlot;
    @FXML
    private Button buttonPlotRefTraj;
    @FXML
    private ComboBox<String> comboBoxRefTrajectory;
    @FXML
    private ListView<String> listViewBlockDefinition;
    @FXML
    private ListView<String> listViewBlockSelection;
   
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        //Set elements not visible at start
        labelProgressCorrection.setVisible(false);
        progressBarCorrection.setVisible(false);
        buttonCorrect1to1.setDisable(true); 
        buttonMeasureResponse1to1.setDisable(true);
        
        //Load reference and zero trajectories
        refTrajData.put("Zero","/Users/nataliamilas/NetBeansProjects/TrajectoryCorrection/ZeroTrajectory.csv");
        refTrajData.put("Golden","/Users/nataliamilas/NetBeansProjects/TrajectoryCorrection/GoldenTrajectory.csv");
        
        //Fill the initial BPMList (first sequece by desfault)
        List<xal.smf.AcceleratorSeq> seqItem = accl.getSequences();
        
        //Defines the MEBT correction block
        CorrectionBlock correctionMEBT = new CorrectionBlock();
        correctionMEBT.setBlockBPM(seqItem.get(0).getAllNodesOfType("BPM"));
        correctionMEBT.setBlockHC(seqItem.get(0).getAllNodesOfType("DCH"));
        correctionMEBT.setBlockVC(seqItem.get(0).getAllNodesOfType("DCV"));
        correctionMEBT.setBlockName("blockMEBT");
        CorrectionElements.put("blockMEBT",correctionMEBT);
        
        //Defines the DTL correction block
        correctionMEBT = new CorrectionBlock();
        correctionMEBT.setBlockBPM(seqItem.get(1).getAllNodesOfType("BPM"));
        correctionMEBT.setBlockHC(seqItem.get(1).getAllNodesOfType("DCH"));
        correctionMEBT.setBlockVC(seqItem.get(1).getAllNodesOfType("DCV"));
        correctionMEBT.setBlockName("blockDTL");
        CorrectionElements.put("blockDTL",correctionMEBT);
        
        //Defines the SPK correction block
        correctionMEBT = new CorrectionBlock();
        correctionMEBT.setBlockBPM(seqItem.get(2).getAllNodesOfType("BPM"));
        correctionMEBT.setBlockHC(seqItem.get(2).getAllNodesOfType("DCH"));
        correctionMEBT.setBlockVC(seqItem.get(2).getAllNodesOfType("DCV"));
        correctionMEBT.setBlockName("blockSPK");
        CorrectionElements.put("blockSPK",correctionMEBT);
         
        //Populate list with some blocks
        for(String item: CorrectionElements.keySet()){
            defined.add(CorrectionElements.get(item).getBlockName());
        }
        listViewBlockDefinition.setItems(defined);
        listViewBlockDefinition.setCellFactory(TextFieldListCell.forListView());
        listViewBlockDefinition.setEditable(true);
        listViewBlockDefinition.setOnEditCommit(new EventHandler<ListView.EditEvent<String>>() {
            @Override
            public void handle(ListView.EditEvent<String> t) {
                    String oldName = listViewBlockDefinition.getItems().get(t.getIndex());
                    listViewBlockDefinition.getItems().set(t.getIndex(), t.getNewValue());
                    defined.set(t.getIndex(), t.getNewValue());
                    CorrectionElements.put(t.getNewValue(), CorrectionElements.remove(oldName));
                    CorrectionElements.get(t.getNewValue()).setBlockName(t.getNewValue());
            }

        });
        
        listViewBlockSelection.setItems(selected);
                
        listViewBlockSelection.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>(){
                 @Override
                 public void updateItem(String item , boolean empty) {
                     super.updateItem(item, empty);
                     setText(item);
                 }
            };

            cell.setOnDragDetected(event -> {
                if (! cell.isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(cell.getItem());
                    db.setContent(cc);
                    dragSource.set(cell);
                }
                event.consume();
            });

            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell &&
                        event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            cell.setOnDragEntered((DragEvent event) -> {
                if (event.getGestureSource() != cell &&
                        event.getDragboard().hasString()) {
                    cell.setOpacity(0.3);
                }
            });

            cell.setOnDragExited((DragEvent event) -> {
                if (event.getGestureSource() != cell &&
                        event.getDragboard().hasString()) {
                    cell.setOpacity(1.0);
                }
            });
            
            cell.setOnDragDone(DragEvent::consume);

            cell.setOnDragDropped(event -> {               
                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    int draggedIdx = selected.indexOf(db.getString());
                    int thisIdx = selected.indexOf(cell.getItem());

                    selected.set(draggedIdx, cell.getItem());
                    if(thisIdx > selected.size()){
                        selected.set(selected.size(), db.getString());
                    } else {
                        selected.set(thisIdx, db.getString());
                    }                    
                    listViewBlockSelection.setItems(selected);

                    success = true;
                }
                event.setDropCompleted(success);

                event.consume();
                
            });

            return cell ;
        });

        //populate the ComboBox element
        refTrajData.keySet().forEach(item -> comboBoxRefTrajectory.getItems().add(item));
        comboBoxRefTrajectory.setValue("Zero");
        
        
        //read new reference trajectory file
        try {
            DisplayTraj.readReferenceTrajectoryFromFile(accl, accl.getAllNodesOfType("BPM"), (String) refTrajData.get(comboBoxRefTrajectory.getValue()));
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }        
        
    }

       
    //handles table context menu for saving a new reference
    @FXML
    public void handleTrajectoryMenuSave(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Trajectory File");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CVS files (*.cvs)", "*.cvs");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            refTrajData.put(selectedFile.getName(),selectedFile.getAbsolutePath());
            comboBoxRefTrajectory.getItems().add(selectedFile.getName());
            //Save Trajecotry of the whole machine
            DisplayTraj.saveTrajectory(accl,selectedFile.getAbsolutePath());
        }
    }
    
    //handles table context menu for loading a new reference orbit
    @FXML
    public void handleTrajectoryMenuLoad(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Trajectory File");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            refTrajData.put(selectedFile.getName(),selectedFile.getAbsolutePath());
            comboBoxRefTrajectory.getItems().add(selectedFile.getName());
        }
    }

    //handles table context menu for deleting a entry (doesn;t allow deleting the zero orbit)
    @FXML
    public void handleTrajectoryMenuDelete(ActionEvent event) {
        if ("Zero".equals(comboBoxRefTrajectory.getValue())) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("You have to select one reference trajecotry file. If you the actual trajectory please select the Zero file.");
            alert.show();
        } else {
            refTrajData.remove(comboBoxRefTrajectory.getValue());
            comboBoxRefTrajectory.getItems().remove(0);
            comboBoxRefTrajectory.setValue("Zero"); 
        }
    }
                  
    @FXML
    private void handleMenuExit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleTrajectoryShow(ActionEvent event) throws IOException{
        Stage stage; 
        Parent root;
        URL    url  = null;
        String sceneFile = "/fxml/PopUpPlot.fxml";
        try
        {
            stage = new Stage();
            url  = getClass().getResource(sceneFile);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(sceneFile));
            root = loader.load();
            root.getStylesheets().add("/styles/Styles.css");
            stage.setScene(new Scene(root));
            stage.setTitle("View Reference Trajectory");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(comboBoxRefTrajectory.getScene().getWindow());
            PopUpPlotController loginController = loader.getController();
            loginController.loggedInProperty().addListener((obs, wasLoggedIn, isNowLoggedIn) -> {
                if (isNowLoggedIn) {
                    stage.close();
                }
            });
            //setup a BPM list to show
            final List<xal.smf.impl.BPM> BPMList = new ArrayList<>();
            if(CorrectionElementsSelected.size()>0){
                CorrectionElementsSelected.keySet().forEach(item -> BPMList.addAll(CorrectionElementsSelected.get(item).getBlockBPM()));
            } else {
                BPMList.addAll(accl.getAllNodesOfType("BPM"));
            }
            try {
                DisplayTraj.readBPMListTrajectory(BPMList);
            } catch (ConnectionException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GetException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            //read new reference trajectory file
            try {
                DisplayTraj.readReferenceTrajectoryFromFile(accl, BPMList, (String) refTrajData.get(comboBoxRefTrajectory.getValue()));
            } catch (IOException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            loginController.updatePlot(DisplayTraj);
            stage.showAndWait();
        }
        catch ( IOException ex )
        {
            System.out.println( "Exception on FXMLLoader.load()" );
            System.out.println( "  * url: " + url );
            System.out.println( "  * " + ex );
            System.out.println( "    ----------------------------------------\n" );
            throw ex;
        }
        
    }  
    
    @FXML
    private void handlePairBPMCorrector(ActionEvent event) {
        List<xal.smf.impl.BPM> BPMList = new ArrayList<>();
        List<xal.smf.impl.HDipoleCorr> HCList = new ArrayList<>();
        List<xal.smf.impl.VDipoleCorr> VCList = new ArrayList<>();
                
        for(String item: CorrectionElementsSelected.keySet()){ 
             BPMList.addAll(CorrectionElementsSelected.get(item).getBlockBPM());
             HCList.addAll(CorrectionElementsSelected.get(item).getBlockHC());
             VCList.addAll(CorrectionElementsSelected.get(item).getBlockVC());
        }
        
        // Create pairs of BPMs and Corrector for the 1-to-1 correction scheme
        CorrectTraj = new CorrectionMatrix();
        try {
            CorrectTraj.getPairs(accl,BPMList,HCList,VCList);
        } catch (ConnectionException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GetException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //print pairs to text area
        textArea1to1.clear();
        textArea1to1.setText("Horizontal pairs: \n");
        for(xal.smf.impl.BPM item : BPMList){
            for(xal.smf.impl.BPM bpm : CorrectTraj.HC.keySet()){
                if(item == bpm){
                    textArea1to1.setText(textArea1to1.getText()+bpm.toString()+" : "+CorrectTraj.HC.get(bpm).toString()+"\n");   
                }
            }
        }
        textArea1to1.setText(textArea1to1.getText()+"Vertical pairs: \n");
        for(xal.smf.impl.BPM item : BPMList){
            for(xal.smf.impl.BPM bpm : CorrectTraj.VC.keySet()){
                if(item == bpm){
                    textArea1to1.setText(textArea1to1.getText()+bpm.toString()+" : "+CorrectTraj.VC.get(bpm).toString()+"\n");     
                }
            }
        }

        buttonMeasureResponse1to1.setDisable(false);
        
    }
    
    @FXML
    private void handleMeasureResponse1to1(ActionEvent event) {
        
        TextInputDialog dialog = new TextInputDialog("0.002");
        dialog.setTitle("Set Field Variation");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Corrector strength (T.m) step:");
        Optional<String> result = dialog.showAndWait();
        double resultval=0.002;
        if (result.isPresent()){
            resultval = Double.parseDouble(result.get());
            if ( resultval<= 0.0 || resultval > 0.01){
               resultval=0.002;
            }   
        }    
 
        final double Dk = resultval;
        Task<Void> task;
        task = new Task<Void>() {
            
            @Override
            protected Void call() throws Exception {
                
                int progress = 0;
                int total = CorrectTraj.HC.size() + CorrectTraj.VC.size();
                //make horizontal calibration for each BPM
                for(xal.smf.impl.BPM bpm : CorrectTraj.HC.keySet()){
                    CorrectTraj.getHCalibration(bpm, Dk);
                    //update progressbar
                    progress++;
                    updateProgress(progress,total);
                    System.out.print("Measure: "+bpm.toString()+" and "+CorrectTraj.HC.get(bpm).toString()+"\n");
                }
                
                //make vertical calibration for each BPM
                for(xal.smf.impl.BPM bpm : CorrectTraj.VC.keySet()){
                    CorrectTraj.getVCalibration(bpm, Dk);
                    //update progressbar
                    progress++;
                    updateProgress(progress,total);
                    System.out.print("Measure: "+bpm.toString()+" and "+CorrectTraj.VC.get(bpm).toString()+"\n");
                }
                
                //Enable CORRECT button
                buttonCorrect1to1.setDisable(false); 
                
                //when the scan finishes set the label and progress bar to zero
                labelProgressCorrection.setVisible(false);
                progressBarCorrection.setVisible(false);
                updateProgress(0,total);
                
                return null;
            };
            
        };
 
        Thread calibrate = new Thread(task);
        calibrate.setDaemon(true); // thread will not prevent application shutdown  
        if (result.isPresent()){
            labelProgressCorrection.setVisible(true);
            progressBarCorrection.setVisible(true);
            labelProgressCorrection.setText("Aquiring BPM responses");
            progressBarCorrection.progressProperty().bind(task.progressProperty());
            calibrate.start();
        }
        
    }
    
    @FXML
    private void handleButtonCorrect1to1(ActionEvent event) {
        
        Task<Void> task;
        task = new Task<Void>() {
            
            @Override
            protected Void call() throws Exception {
               
                double DeltaK = 0.0;
                double val = 0.0;
                double correctFactor = Double.parseDouble(textFieldCorrFactor1to1.getText())/100;
                int total = 0;
                int step = 0;
                
                if (radioButtonHor.isSelected()){
                    total = CorrectTraj.HC.size();
                } else if (radioButtonVer.isSelected()){
                    total = CorrectTraj.VC.size();
                } else if (radioButtonHorVer.isSelected()){
                    total = CorrectTraj.VC.size()+CorrectTraj.HC.size();
                }
                
                List<xal.smf.impl.BPM> BPMList = new ArrayList<>();
                for(String item: CorrectionElementsSelected.keySet()){ 
                    BPMList.addAll(CorrectionElementsSelected.get(item).getBlockBPM());
                }
                                
                //correct trajectory
                for(xal.smf.impl.BPM item : BPMList){
                    if(radioButtonHor.isSelected() || radioButtonHorVer.isSelected()){
                        if(CorrectTraj.HC.containsKey(item)){
                            val = CorrectTraj.HC.get(item).getField();                        
                            DeltaK = correctFactor*CorrectTraj.calcHCorrection(item,DisplayTraj.XRef.get(item));
                            val = val + DeltaK;
                            CorrectTraj.HC.get(item).setField(val);
                            Thread.sleep(2000);
                            step++;
                            updateProgress(step,total);
                        }
                    }
                    if(radioButtonHorVer.isSelected() || radioButtonVer.isSelected()){
                        if(CorrectTraj.VC.containsKey(item)){
                            val = CorrectTraj.VC.get(item).getField();                        
                            DeltaK = correctFactor*CorrectTraj.calcVCorrection(item,DisplayTraj.YRef.get(item));
                            val = val + DeltaK;
                            CorrectTraj.VC.get(item).setField(val);
                            Thread.sleep(2000);
                            step++;
                            updateProgress(step,total);
                        }
                    }
                }
                
                //when the scan finishes set the label and progress bar to zero
                labelProgressCorrection.setVisible(false);
                progressBarCorrection.setVisible(false);
                updateProgress(0,total);
                
                return null;
            };
            
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
        
        TextInputDialog dialog = new TextInputDialog("0.002");
        dialog.setTitle("Set Field Variation");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Corrector strength (T.m) step:");
        Optional<String> result = dialog.showAndWait();
        double resultval=0.002;
        if (result.isPresent()){
            resultval = Double.parseDouble(result.get());
            if ( resultval<= 0.0 || resultval > 0.01){
               resultval=0.002;
            }   
        }    
 
        final double Dk = resultval;
        
        //print knobs
        textAreaSVD.clear();
        
        
        CorrectSVDMatrix = new ArrayList<>();
        int i =0;
        for(String item: CorrectionElementsSelected.keySet()){ 
            CorrectSVDMatrix.add(new CorrectionSVD()); 
            CorrectSVDMatrix.get(i).defineKnobs(accl,CorrectionElementsSelected.get(item).getBlockBPM(),CorrectionElementsSelected.get(item).getBlockHC(),CorrectionElementsSelected.get(item).getBlockVC());
            textAreaSVD.setText(textAreaSVD.getText()+CorrectionElementsSelected.get(item).getBlockName()+"\n");
            textAreaSVD.setText(textAreaSVD.getText()+"Horizontal Correctors: \n");
            for(xal.smf.impl.HDipoleCorr hcorr : CorrectSVDMatrix.get(i).HC){
                textAreaSVD.setText(textAreaSVD.getText()+hcorr.toString()+"; ");               
            }
            textAreaSVD.setText(textAreaSVD.getText()+"\n");
            textAreaSVD.setText(textAreaSVD.getText()+"Vertical correctors: \n");
            for(xal.smf.impl.VDipoleCorr vcorr : CorrectSVDMatrix.get(i).VC){
                textAreaSVD.setText(textAreaSVD.getText()+vcorr.toString()+"; ");     
            }
            textAreaSVD.setText(textAreaSVD.getText()+"\n");
            i++;
        }
        
        Task<Void> task;
        task = new Task<Void>() {
            
            @Override
            protected Void call() throws Exception {
                
                progressSVD = true;
                int progress = 0;
                
                for(CorrectionSVD item: CorrectSVDMatrix){ 
                    item.measureTRMHorizontal(Dk);
                    progress++;
                    updateProgress(progress,CorrectSVDMatrix.size()*2);
                    item.measureTRMVertical(Dk);
                    progress++;
                    updateProgress(progress,CorrectSVDMatrix.size()*2);
                }
                
                //Enable SVD calculation button
                buttonCalcCorrectSVD.setDisable(false); 
                
                //when the scan finishes set the label and progress bar to zero
                labelProgressCorrection.setVisible(false);
                progressBarCorrection.setVisible(false);
                updateProgress(0,CorrectSVDMatrix.size()*2);
                
                progressSVD = false;
                
                return null;
            };
            
        };
 
        Thread calibrate = new Thread(task);
        calibrate.setDaemon(true); // thread will not prevent application shutdown  
        progressBarCorrection.progressProperty().bind(task.progressProperty());
        if (result.isPresent()){
            labelProgressCorrection.setVisible(true);
            progressBarCorrection.setVisible(true);
            labelProgressCorrection.setText("Measuring response matrix...");
            calibrate.start();
        }
        
    }
    
    @FXML
    private void handleCalculateCorrectionSVD(ActionEvent event) throws IOException {
        
        double[] kickH;
        double[] kickV;
        double[] singVal;
        double svCut;
        double correctionFactor;
        int i = 0;
        
        svCut = Double.parseDouble(textFieldSingularValCut.getText());       
        
        textAreaSVD.clear();
        
        for(CorrectionSVD matrix: CorrectSVDMatrix){
            //reads a new trajectory
            try {
                DisplayTraj.readBPMListTrajectory(matrix.BPM);
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            kickH = matrix.calculateHCorrection(DisplayTraj, svCut);
            kickV = matrix.calculateVCorrection(DisplayTraj, svCut);

            textAreaSVD.setText(textAreaSVD.getText()+"Horizontal Corrector strenghts \n");
            i = 0;
            for(xal.smf.impl.HDipoleCorr hcorr: matrix.HC){
                textAreaSVD.setText(textAreaSVD.getText()+hcorr.toString()+" : "+String.format("%.3f",kickH[i])+"\n");
                i++;
            }
            textAreaSVD.setText(textAreaSVD.getText()+"Vertical Corrector strenghts \n");
            i = 0;
            for(xal.smf.impl.VDipoleCorr vcorr: matrix.VC){
                textAreaSVD.setText(textAreaSVD.getText()+vcorr.toString()+" : "+String.format("%.3f",kickV[i])+"\n");
                i++;
            }

            singVal = matrix.getSigularValuesH();
            textAreaSVD.setText(textAreaSVD.getText()+"Horizontal Singular Values \n");
            for(int j = 0; j<singVal.length; j++){
                textAreaSVD.setText(textAreaSVD.getText()+String.format("%.3f", singVal[j])+"\n");
            }
            singVal = matrix.getSigularValuesV();
            textAreaSVD.setText(textAreaSVD.getText()+"Vertical Singular Values \n");
            for(int j = 0; j<singVal.length; j++){
                textAreaSVD.setText(textAreaSVD.getText()+String.format("%.3f", singVal[j])+"\n");
            }
        }
        
        //Enable SVD correction button
        buttonCorrectSVD.setDisable(false);
        
    }
    
    @FXML
    private void handleCorrectSVD(ActionEvent event) {
        
        double[] kickH;
        double[] kickV;
        double svCut;
        double correctionFactor;
        List<xal.smf.impl.HDipoleCorr> HC = new ArrayList<>();
        List<xal.smf.impl.VDipoleCorr> VC = new ArrayList<>();
        int i = 0;
        int j = 0;
        
        svCut = Double.parseDouble(textFieldSingularValCut.getText());
        correctionFactor = Double.parseDouble(textFieldCorrFactorSVD.getText())/100;
        
        for(CorrectionSVD matrix: CorrectSVDMatrix){
            //reads a new trajectory
            try {
                DisplayTraj.readBPMListTrajectory(matrix.BPM);
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            kickH = matrix.calculateHCorrection(DisplayTraj, svCut);
            kickV = matrix.calculateVCorrection(DisplayTraj, svCut);

        
            double aux = 0;
            i = 0;
            if(radioButtonHorSVD.isSelected() || radioButtonHorVerSVD.isSelected()){
                for(xal.smf.impl.HDipoleCorr hcorr: matrix.HC){
                    try {
                        aux = hcorr.getField();
                    } catch (ConnectionException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (GetException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    aux = aux + correctionFactor*kickH[i];
                    i++;
                    try {
                        hcorr.setField(aux);
                    } catch (ConnectionException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (PutException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            j = 0;
            if(radioButtonVerSVD.isSelected() || radioButtonHorVerSVD.isSelected()){
                for(xal.smf.impl.VDipoleCorr vcorr: matrix.VC){
                    try {
                        aux = vcorr.getField();
                    } catch (ConnectionException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (GetException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    aux = aux + correctionFactor*kickV[j];
                    j++;
                    try {
                        vcorr.setField(aux);
                    } catch (ConnectionException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (PutException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        
    }

    @FXML
    private void handleContextMenu(ContextMenuEvent event) {
        
        final MenuItem addBlock = new MenuItem("Add Block");
        final MenuItem editBlock = new MenuItem("Edit Selected Block");
        final MenuItem deleteBlock = new MenuItem("Remove Selected Block");
        final ContextMenu menu = new ContextMenu(addBlock, new SeparatorMenuItem(), editBlock, deleteBlock); 
 
        addBlock.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                Stage stage; 
                Parent root;
                URL    url  = null;
                String sceneFile = "/fxml/CorrectionElementSelection.fxml";
                try
                {
                    stage = new Stage();
                    url  = getClass().getResource(sceneFile);
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
                            if(loginController.getChangedSelectionList()){
                                CorrectionBlock newBlock = new CorrectionBlock();
                                newBlock.setBlockBPM(loginController.getBPMSelectionList());
                                newBlock.setBlockHC(loginController.getHCSelectionList());
                                newBlock.setBlockVC(loginController.getVCSelectionList());
                                for(int i=0; i<(defined.size()+selected.size()); i++){
                                    if(!selected.contains("newBlock"+i) && !defined.contains("newBlock"+i)){
                                        newBlock.setBlockName("newBlock"+i);
                                        break;
                                    }
                                }
                                CorrectionElements.put(newBlock.getBlockName(),newBlock);
                                defined.add(newBlock.getBlockName());
                            }        
                            stage.close();
                        }
                    });
                    loginController.populateElementGrid(accl);
                    stage.showAndWait();
                }
                catch ( IOException ex )
                {
                    System.out.println( "Exception on FXMLLoader.load()" );
                    System.out.println( "  * url: " + url );
                    System.out.println( "  * " + ex );
                    System.out.println( "    ----------------------------------------\n" );
                    try {
                        throw ex;
                    } catch (IOException ex1) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        });
        
        editBlock.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
            Stage stage; 
                Parent root;
                URL    url  = null;
                String sceneFile = "/fxml/CorrectionElementSelection.fxml";
                try
                {
                    stage = new Stage();
                    url  = getClass().getResource(sceneFile);
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
                            if(loginController.getChangedSelectionList()){
                                CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedItem()).setBlockBPM(loginController.getBPMSelectionList());
                                CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedItem()).setBlockHC(loginController.getHCSelectionList());
                                CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedItem()).setBlockVC(loginController.getVCSelectionList());
                            }
                            stage.close();
                        }
                    });
                    loginController.populateElementGrid(accl);
                    loginController.enterElementstoEdit(CorrectionElements.get(listViewBlockDefinition.getSelectionModel().getSelectedItem()));
                    stage.showAndWait();
                }
                catch ( IOException ex )
                {
                    System.out.println( "Exception on FXMLLoader.load()" );
                    System.out.println( "  * url: " + url );
                    System.out.println( "  * " + ex );
                    System.out.println( "    ----------------------------------------\n" );
                    try {
                        throw ex;
                    } catch (IOException ex1) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        });
        
        deleteBlock.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                int index = listViewBlockDefinition.getSelectionModel().getSelectedIndex();
                String removeName = listViewBlockDefinition.getSelectionModel().getSelectedItem();
                if(removeName != null){
                    CorrectionElements.remove(removeName);
                    defined.remove(removeName);
                    //defined.sorted();
                    listViewBlockDefinition.getItems().remove(removeName);
                    listViewBlockDefinition.getSelectionModel().clearSelection();
                }
               
            }
        });
        
        
        menu.show(listViewBlockDefinition.getScene().getWindow(), event.getScreenX(), event.getScreenY());
        
        
    }

    @FXML
    private void handleRemoveSelectedBlock(ActionEvent event) {
        String removeName = listViewBlockSelection.getSelectionModel().getSelectedItem();
        if(removeName != null){
            CorrectionElements.put(removeName,CorrectionElementsSelected.get(removeName));
            defined.add(removeName);
            //defined.sorted();
            CorrectionElementsSelected.remove(removeName);
            selected.remove(removeName);
            //selected.sorted();
            listViewBlockSelection.getSelectionModel().clearSelection();
        }
    }
    
    @FXML
    private void handleSelectedBlock(ActionEvent event) {
        String includeName = listViewBlockDefinition.getSelectionModel().getSelectedItem();
        if(includeName != null){            
            CorrectionElementsSelected.put(includeName, CorrectionElements.get(includeName));
            selected.add(includeName);
            //selected.sorted();
            CorrectionElements.remove(includeName);
            defined.remove(includeName);
            //defined.sorted();
            listViewBlockDefinition.getSelectionModel().clearSelection();
        }
   
    }

  
}
