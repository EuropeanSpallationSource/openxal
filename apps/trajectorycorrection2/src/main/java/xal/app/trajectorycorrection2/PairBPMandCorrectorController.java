/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.trajectorycorrection2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import xal.model.ModelException;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.tools.math.r3.R3;

/**
 * FXML Controller class
 *
 * @author nataliamilas
 */
public class PairBPMandCorrectorController {

    private final BooleanProperty pairChanged = new SimpleBooleanProperty();       
    private final BooleanProperty loggedIn = new SimpleBooleanProperty();       
    private final ObservableList<Pair> dataH = FXCollections.observableArrayList();
    private final ObservableList<Pair> dataV = FXCollections.observableArrayList();    
    private HashMap<xal.smf.AcceleratorNode, R3> phase= new HashMap();
    @FXML
    private TableView<Pair> tableHorizontalPairs;
    @FXML
    private TableView<Pair> tableVerticalPairs;
    @FXML
    private ComboBox<xal.smf.impl.HDipoleCorr> comboBoxHC;
    @FXML
    private ComboBox<xal.smf.impl.BPM> comboBoxBPMV;
    @FXML
    private ComboBox<xal.smf.impl.VDipoleCorr> comboBoxVC;
    @FXML
    private ComboBox<xal.smf.impl.BPM> comboBoxBPMH;
    
    public BooleanProperty loggedInProperty() {
        return loggedIn ;
    }

    public final boolean isLoggedIn() {
        return loggedInProperty().get();
    }

    public final void setLoggedIn(boolean loggedIn) {
        loggedInProperty().set(loggedIn);
    } 
    
    public BooleanProperty pairChangedProperty() {
        return pairChanged ;
    }

    public final boolean isPairChanged() {
        return pairChangedProperty().get();
    }

    public final void setPairChanged(boolean loggedIn) {
        pairChangedProperty().set(loggedIn);
    } 
    
    public void setAllVariables(xal.smf.Accelerator accl, List<xal.smf.impl.BPM> BPMList, List<xal.smf.impl.HDipoleCorr> HCList, List<xal.smf.impl.VDipoleCorr> VCList){
        RunSimulationService simulService;                              
        AcceleratorSeq iniSeq;
        AcceleratorSeq finalSeq;
                       
        BPMList.sort((bpm1,bpm2)-> Double.compare(bpm1.getPosition()+bpm1.getParent().getPosition(),bpm2.getPosition()+bpm2.getParent().getPosition()));
        HCList.sort((hc1,hc2)-> Double.compare(hc1.getPosition()+hc1.getParent().getPosition(),hc2.getPosition()+hc2.getParent().getPosition()));
        VCList.sort((vc1,vc2)-> Double.compare(vc1.getPosition()+vc1.getParent().getPosition(),vc2.getPosition()+vc2.getParent().getPosition()));
        
        comboBoxBPMH.getItems().addAll(BPMList);
        comboBoxBPMV.getItems().addAll(BPMList);
        comboBoxHC.getItems().addAll(HCList);
        comboBoxVC.getItems().addAll(VCList);
        
        //Run Simulation to get phase advance
        //setup simulation parameters
        
        if (VCList.get(0).getPosition()<HCList.get(0).getPosition()){                        
            iniSeq = VCList.get(0).getParent();            
        } else { 
            iniSeq = HCList.get(0).getParent(); 
        }
        finalSeq = BPMList.get(BPMList.size()-1).getParent();
        if(iniSeq != finalSeq){
            List<AcceleratorSeq> newCombo = new ArrayList<>();             
            for(int i=accl.getAllSeqs().indexOf(iniSeq); i<=accl.getAllSeqs().indexOf(finalSeq); i++){
                newCombo.add(accl.getAllSeqs().get(i));
            }
            AcceleratorSeqCombo Sequence = new xal.smf.AcceleratorSeqCombo("calcMatrix",newCombo);
            simulService = new RunSimulationService(Sequence); 
            simulService.setSynchronizationMode("DESIGN");
        } else {
           simulService = new RunSimulationService(iniSeq);
           simulService.setSynchronizationMode("DESIGN");
        }                     
        
        List<xal.smf.AcceleratorNode> elements = Stream.of(HCList,VCList,BPMList).flatMap(Collection::stream).collect(Collectors.toList());
        try {
            phase = simulService.runTwissSimulation(elements);
        } catch (InstantiationException | ModelException ex) {
            Logger.getLogger(PairBPMandCorrectorController.class.getName()).log(Level.SEVERE, null, ex);
        }               
                
    }
    
    public void setInitialPairs(HashMap<xal.smf.impl.BPM,xal.smf.impl.HDipoleCorr> HC, HashMap<xal.smf.impl.BPM,xal.smf.impl.VDipoleCorr> VC){
        
        HC.keySet().stream().forEach(bpm -> {
            dataH.add(new Pair(bpm,HC.get(bpm),(phase.get(bpm).getx()-phase.get(HC.get(bpm)).getx())));            
        });
        
        dataH.sort((pair1,pair2)-> Double.compare(pair1.bpm.getPosition()+pair1.bpm.getParent().getPosition(),pair2.bpm.getPosition()+pair2.bpm.getParent().getPosition()));
                
        VC.keySet().stream().forEach(bpm -> {
            dataV.add(new Pair(bpm,VC.get(bpm),(phase.get(bpm).gety()-phase.get(VC.get(bpm)).gety())));
        });       
        
        dataV.sort((pair1,pair2)-> Double.compare(pair1.bpm.getPosition()+pair1.bpm.getParent().getPosition(),pair2.bpm.getPosition()+pair2.bpm.getParent().getPosition()));
    }
    
    public HashMap<xal.smf.impl.BPM,xal.smf.impl.HDipoleCorr> updateHPairs(){
        
        HashMap<xal.smf.impl.BPM,xal.smf.impl.HDipoleCorr> HC = new HashMap(); 
        
        dataH.sort((pair1,pair2)-> Double.compare(pair1.bpm.getPosition()+pair1.bpm.getParent().getPosition(),pair2.bpm.getPosition()+pair2.bpm.getParent().getPosition()));
        
        HC.clear();
        dataH.forEach(pair -> {
            HC.put(pair.bpm,(xal.smf.impl.HDipoleCorr) pair.corrector);            
        });
        
        return HC;
    }
    
    public HashMap<xal.smf.impl.BPM,xal.smf.impl.VDipoleCorr> updateVPairs(){
        
        HashMap<xal.smf.impl.BPM,xal.smf.impl.VDipoleCorr> VC = new HashMap();
        
        dataV.sort((pair1,pair2)-> Double.compare(pair1.bpm.getPosition()+pair1.bpm.getParent().getPosition(),pair2.bpm.getPosition()+pair2.bpm.getParent().getPosition()));
        
        dataV.forEach(pair -> {
            VC.put(pair.bpm,(xal.smf.impl.VDipoleCorr) pair.corrector);            
        });
        
        return VC;
    }
    
    public void createGui(){
        
        //configure Tables in the GUI        
        tableHorizontalPairs.setEditable(false);
        tableVerticalPairs.setEditable(false);
         
        TableColumn<Pair, String> BPMColH = new TableColumn<>("BPM");
        BPMColH.setCellValueFactory(cellData -> cellData.getValue().bpmNameProperty());
        BPMColH.setPrefWidth(210);
        TableColumn<Pair, String> CorrectorColH = new TableColumn("Corrector");
        CorrectorColH.setCellValueFactory(cellData -> cellData.getValue().correctorNameProperty());
        CorrectorColH.setPrefWidth(210);
        TableColumn<Pair, String> PhaseColH = new TableColumn("Phase (1/2\u03c0)");
        PhaseColH.setCellValueFactory(cellData -> cellData.getValue().phaseNameProperty());
        PhaseColH.setPrefWidth(150);
        
        TableColumn PairColH = new TableColumn("Pairs");
        PairColH.getColumns().addAll(BPMColH,CorrectorColH);
        
        tableHorizontalPairs.setItems(dataH);
        tableHorizontalPairs.getColumns().addAll(PairColH,PhaseColH);
        
        TableColumn<Pair, String> BPMColV = new TableColumn<>("BPM");
        BPMColV.setCellValueFactory(cellData -> cellData.getValue().bpmNameProperty());       
        BPMColV.setPrefWidth(210);
        TableColumn<Pair, String> CorrectorColV = new TableColumn("Corrector");
        CorrectorColV.setCellValueFactory(cellData -> cellData.getValue().correctorNameProperty());
        //CorrectorColV.setCellFactory(ComboBoxTableCell.forTableColumn(blockVC.toString().substring(1,blockVC.toString().length()-1).split(", ")));
        CorrectorColV.setPrefWidth(210);
        TableColumn<Pair, String> PhaseColV = new TableColumn("Phase (1/2\u03c0)");
        PhaseColV.setCellValueFactory(cellData -> cellData.getValue().phaseNameProperty());
        PhaseColV.setPrefWidth(150);
        
        TableColumn PairColV = new TableColumn("Pairs");        
        PairColV.getColumns().addAll(BPMColV,CorrectorColV);
       
        tableVerticalPairs.setItems(dataV);
        tableVerticalPairs.getColumns().addAll(PairColV,PhaseColV); 
        
        dataH.forEach(pair ->{
            if(comboBoxBPMH.getItems().contains(pair.bpm)){
                comboBoxBPMH.getItems().remove(pair.bpm);
            }
            if(comboBoxHC.getItems().contains((xal.smf.impl.HDipoleCorr) pair.corrector)){
                comboBoxHC.getItems().remove((xal.smf.impl.HDipoleCorr) pair.corrector);
            }
        });
        
        dataV.forEach(pair ->{
            if(comboBoxBPMV.getItems().contains(pair.bpm)){
                comboBoxBPMV.getItems().remove(pair.bpm);
            }
            if(comboBoxVC.getItems().contains((xal.smf.impl.VDipoleCorr) pair.corrector)){
                comboBoxVC.getItems().remove((xal.smf.impl.VDipoleCorr) pair.corrector);
            }
        });
        
        comboBoxBPMH.setCellFactory((ListView<xal.smf.impl.BPM> bpm) -> {
            ListCell cell = new ListCell<xal.smf.impl.BPM>() {
                @Override
                protected void updateItem(xal.smf.impl.BPM item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.toString());
                    }
                }
            };
            return cell;
        });
        
        comboBoxBPMV.setCellFactory((ListView<xal.smf.impl.BPM> bpm) -> {
            ListCell cell = new ListCell<xal.smf.impl.BPM>() {
                @Override
                protected void updateItem(xal.smf.impl.BPM item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.toString());
                    }
                }
            };
            return cell;
        });
        
        comboBoxHC.setCellFactory((ListView<xal.smf.impl.HDipoleCorr> hc) -> {
            ListCell cell = new ListCell<xal.smf.impl.HDipoleCorr>() {
                @Override
                protected void updateItem(xal.smf.impl.HDipoleCorr item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.toString());
                    }
                }
            };
            return cell;
        });
        
        comboBoxVC.setCellFactory((ListView<xal.smf.impl.VDipoleCorr> vc) -> {
            ListCell cell = new ListCell<xal.smf.impl.VDipoleCorr>() {
                @Override
                protected void updateItem(xal.smf.impl.VDipoleCorr item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.toString());
                    }
                }
            };
            return cell;
        });
        
        
    }

    @FXML
    private void handleHorizontalContextMenu(ActionEvent event) {
        
        final Pair pairVal = tableHorizontalPairs.getSelectionModel().getSelectedItem();
        
        if (pairVal != null){
            dataH.remove(pairVal);
            comboBoxBPMH.getItems().add(pairVal.bpm);
            comboBoxHC.getItems().add((xal.smf.impl.HDipoleCorr) pairVal.corrector);
            comboBoxBPMH.getItems().sort((bpm1,bpm2)-> Double.compare(bpm1.getPosition()+bpm1.getParent().getPosition(),bpm2.getPosition()+bpm2.getParent().getPosition()));
            comboBoxHC.getItems().sort((hc1,hc2)-> Double.compare(hc1.getPosition()+hc1.getParent().getPosition(),hc2.getPosition()+hc2.getParent().getPosition()));
        }
        
    }
    
    @FXML
    private void handleVerticalContextMenu(ActionEvent event) {
        
        final Pair pairVal = tableVerticalPairs.getSelectionModel().getSelectedItem();
                      
        if (pairVal != null){            
            dataV.remove(pairVal);     
            comboBoxBPMV.getItems().add(pairVal.bpm);
            comboBoxVC.getItems().add((xal.smf.impl.VDipoleCorr) pairVal.corrector);
            comboBoxBPMV.getItems().sort((bpm1,bpm2)-> Double.compare(bpm1.getPosition()+bpm1.getParent().getPosition(),bpm2.getPosition()+bpm2.getParent().getPosition()));
            comboBoxVC.getItems().sort((vc1,vc2)-> Double.compare(vc1.getPosition()+vc1.getParent().getPosition(),vc2.getPosition()+vc2.getParent().getPosition()));
        }
    }

    @FXML
    private void handleAddPairH(ActionEvent event) {
        final xal.smf.impl.HDipoleCorr hcVal = comboBoxHC.getSelectionModel().getSelectedItem();
        final xal.smf.impl.BPM bpmVal = comboBoxBPMH.getSelectionModel().getSelectedItem();
        
        dataH.add(new Pair(bpmVal,hcVal,(phase.get(bpmVal).getx()-phase.get(hcVal).getx())));
        dataH.sort((pair1,pair2)-> Double.compare(pair1.bpm.getPosition()+pair1.bpm.getParent().getPosition(),pair2.bpm.getPosition()+pair2.bpm.getParent().getPosition()));
        comboBoxBPMH.getItems().remove(bpmVal);
        comboBoxHC.getItems().remove(hcVal);
        
    }

    @FXML
    private void handleAddPairV(ActionEvent event) {
        final xal.smf.impl.VDipoleCorr vcVal = comboBoxVC.getSelectionModel().getSelectedItem();
        final xal.smf.impl.BPM bpmVal = comboBoxBPMV.getSelectionModel().getSelectedItem();
        
        dataV.add(new Pair(bpmVal,vcVal,(phase.get(bpmVal).getx()-phase.get(vcVal).getx())));
        dataV.sort((pair1,pair2)-> Double.compare(pair1.bpm.getPosition()+pair1.bpm.getParent().getPosition(),pair2.bpm.getPosition()+pair2.bpm.getParent().getPosition()));
        comboBoxBPMV.getItems().remove(bpmVal);
        comboBoxVC.getItems().remove(vcVal);
    }    

    @FXML
    private void handleOK(ActionEvent event) {
        setPairChanged(true);
        setLoggedIn(true);        
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        setPairChanged(false);
        setLoggedIn(true);
    }
   
    private class Pair {
        private xal.smf.impl.BPM bpm;
        private final StringProperty bpmName = new SimpleStringProperty();
        private xal.smf.impl.DipoleCorr corrector;
        private final StringProperty correctorName = new SimpleStringProperty();
        private double phase;
        private final StringProperty phaseName = new SimpleStringProperty();

        private Pair(xal.smf.impl.BPM bpm, xal.smf.impl.DipoleCorr corrector, double phaseVal) {
            this.bpm = bpm;
            bpmName.set(bpm.toString());
            this.corrector = corrector;
            correctorName.set(corrector.toString());
            this.phase = phaseVal;
            phaseName.set(String.format("%.2f",phaseVal));
        }

        public final StringProperty bpmNameProperty() {
            return this.bpmName;
        }

        public final String getbpmName() {
            return this.bpmNameProperty().get();
        }

        public final void setbpmName(final String name) {
            this.bpmNameProperty().set(name);
        }
        
        public void setBPM(xal.smf.impl.BPM bName) {
            this.bpm = bName;
        }

        public final StringProperty correctorNameProperty() {
            return this.correctorName;
        }

        public final String getcorrectorName() {
            return this.correctorNameProperty().get();
        }

        public final void setcorrectorName(final String name) {
            this.correctorNameProperty().set(name);
        }
        
        public void setCorrectorName(xal.smf.impl.DipoleCorr cName) {
            this.corrector = cName;
        }

        public final StringProperty phaseNameProperty() {
            return this.phaseName;
        }
        
        public final void setphaseName(final double phaseVal) {
            this.correctorNameProperty().set(String.format("%.4f",phaseVal));
        }
        
        public double getPhaseVal() {
            return phase;
        }
        
        public void setPhaseVal(double phaseVal) {
            phase = phaseVal;
        }

    }
       
}