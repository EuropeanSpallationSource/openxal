/*
 * Copyright (C) 2018 European Spallation Source ERIC
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
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.BPM;
import xal.smf.impl.DipoleCorr;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.VDipoleCorr;
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
    private HashMap<AcceleratorNode, R3> phase = new HashMap();
    @FXML
    private TableView<Pair> tableHorizontalPairs;
    @FXML
    private TableView<Pair> tableVerticalPairs;
    @FXML
    private ComboBox<HDipoleCorr> comboBoxHC;
    @FXML
    private ComboBox<BPM> comboBoxBPMV;
    @FXML
    private ComboBox<VDipoleCorr> comboBoxVC;
    @FXML
    private ComboBox<BPM> comboBoxBPMH;

    public BooleanProperty loggedInProperty() {
        return loggedIn;
    }

    public final boolean isLoggedIn() {
        return loggedInProperty().get();
    }

    public final void setLoggedIn(boolean loggedIn) {
        loggedInProperty().set(loggedIn);
    }

    public BooleanProperty pairChangedProperty() {
        return pairChanged;
    }

    public final boolean isPairChanged() {
        return pairChangedProperty().get();
    }

    public final void setPairChanged(boolean loggedIn) {
        pairChangedProperty().set(loggedIn);
    }

    public void setAllVariables(Accelerator accl, List<BPM> BPMList, List<HDipoleCorr> HCList, List<VDipoleCorr> VCList) {
        RunSimulationService simulService;
        AcceleratorSeq iniSeq;
        AcceleratorSeq finalSeq;

        BPMList.sort((bpm1, bpm2) -> Double.compare(bpm1.getSDisplay(), bpm2.getSDisplay()));
        HCList.sort((hc1, hc2) -> Double.compare(hc1.getSDisplay(), hc2.getSDisplay()));
        VCList.sort((vc1, vc2) -> Double.compare(vc1.getSDisplay(), vc2.getSDisplay()));

        comboBoxBPMH.getItems().addAll(BPMList);
        comboBoxBPMV.getItems().addAll(BPMList);
        comboBoxHC.getItems().addAll(HCList);
        comboBoxVC.getItems().addAll(VCList);

        //Run Simulation to get phase advance
        if (VCList.get(0).getSDisplay() < HCList.get(0).getSDisplay()) {
            iniSeq = VCList.get(0).getPrimaryAncestor();
        } else {
            iniSeq = HCList.get(0).getPrimaryAncestor();
        }
        finalSeq = BPMList.get(BPMList.size() - 1).getPrimaryAncestor();
        if (iniSeq != finalSeq) {
            List<AcceleratorSeq> newCombo = new ArrayList<>();
            for (int i = accl.getAllSeqs().indexOf(iniSeq); i <= accl.getAllSeqs().indexOf(finalSeq); i++) {
                newCombo.add(accl.getAllSeqs().get(i));
            }
            AcceleratorSeqCombo Sequence = new AcceleratorSeqCombo("calcMatrix", newCombo);
            simulService = new RunSimulationService(Sequence);
            simulService.setSynchronizationMode("DESIGN");
        } else {
            simulService = new RunSimulationService(iniSeq);
            simulService.setSynchronizationMode("DESIGN");
        }

        List<AcceleratorNode> elements = Stream.of(HCList, VCList, BPMList).flatMap(Collection::stream).collect(Collectors.toList());
        try {
            phase = simulService.runTwissSimulation(elements);
        } catch (InstantiationException | ModelException ex) {
            Logger.getLogger(PairBPMandCorrectorController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setInitialPairs(HashMap<BPM, HDipoleCorr> HC, HashMap<BPM, VDipoleCorr> VC) {

        HC.keySet().stream().forEach(bpm -> {
            dataH.add(new Pair(bpm, HC.get(bpm), (phase.get(bpm).getx() - phase.get(HC.get(bpm)).getx())));
        });

        dataH.sort((pair1, pair2) -> Double.compare(pair1.bpm.getSDisplay(), pair2.bpm.getSDisplay()));

        VC.keySet().stream().forEach(bpm -> {
            dataV.add(new Pair(bpm, VC.get(bpm), (phase.get(bpm).gety() - phase.get(VC.get(bpm)).gety())));
        });

        dataV.sort((pair1, pair2) -> Double.compare(pair1.bpm.getSDisplay(), pair2.bpm.getSDisplay()));

    }

    public HashMap<BPM, HDipoleCorr> updateHPairs() {

        HashMap<BPM, HDipoleCorr> HC = new HashMap();

        dataH.sort((pair1, pair2) -> Double.compare(pair1.bpm.getSDisplay(), pair2.bpm.getSDisplay()));

        HC.clear();
        dataH.forEach(pair -> {
            HC.put(pair.bpm, (HDipoleCorr) pair.corrector);
        });

        return HC;
    }

    public HashMap<BPM, VDipoleCorr> updateVPairs() {

        HashMap<BPM, VDipoleCorr> VC = new HashMap();

        dataV.sort((pair1, pair2) -> Double.compare(pair1.bpm.getSDisplay(), pair2.bpm.getSDisplay()));

        dataV.forEach(pair -> {
            VC.put(pair.bpm, (VDipoleCorr) pair.corrector);
        });

        return VC;
    }

    public void createGui() {

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
        PairColH.getColumns().addAll(BPMColH, CorrectorColH);

        tableHorizontalPairs.setItems(dataH);
        tableHorizontalPairs.getColumns().addAll(PairColH, PhaseColH);

        TableColumn<Pair, String> BPMColV = new TableColumn<>("BPM");
        BPMColV.setCellValueFactory(cellData -> cellData.getValue().bpmNameProperty());
        BPMColV.setPrefWidth(210);
        TableColumn<Pair, String> CorrectorColV = new TableColumn("Corrector");
        CorrectorColV.setCellValueFactory(cellData -> cellData.getValue().correctorNameProperty());
        CorrectorColV.setPrefWidth(210);
        TableColumn<Pair, String> PhaseColV = new TableColumn("Phase (1/2\u03c0)");
        PhaseColV.setCellValueFactory(cellData -> cellData.getValue().phaseNameProperty());
        PhaseColV.setPrefWidth(150);

        TableColumn PairColV = new TableColumn("Pairs");
        PairColV.getColumns().addAll(BPMColV, CorrectorColV);

        tableVerticalPairs.setItems(dataV);
        tableVerticalPairs.getColumns().addAll(PairColV, PhaseColV);

        dataH.forEach(pair -> {
            if (comboBoxBPMH.getItems().contains(pair.bpm)) {
                comboBoxBPMH.getItems().remove(pair.bpm);
            }
            if (comboBoxHC.getItems().contains((HDipoleCorr) pair.corrector)) {
                comboBoxHC.getItems().remove((HDipoleCorr) pair.corrector);
            }
        });

        dataV.forEach(pair -> {
            if (comboBoxBPMV.getItems().contains(pair.bpm)) {
                comboBoxBPMV.getItems().remove(pair.bpm);
            }
            if (comboBoxVC.getItems().contains((VDipoleCorr) pair.corrector)) {
                comboBoxVC.getItems().remove((VDipoleCorr) pair.corrector);
            }
        });

        comboBoxBPMH.setCellFactory((ListView<BPM> bpm) -> {
            ListCell cell = new ListCell<BPM>() {
                @Override
                protected void updateItem(BPM item, boolean empty) {
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

        comboBoxBPMV.setCellFactory((ListView<BPM> bpm) -> {
            ListCell cell = new ListCell<BPM>() {
                @Override
                protected void updateItem(BPM item, boolean empty) {
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

        comboBoxHC.setCellFactory((ListView<HDipoleCorr> hc) -> {
            ListCell cell = new ListCell<HDipoleCorr>() {
                @Override
                protected void updateItem(HDipoleCorr item, boolean empty) {
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

        comboBoxVC.setCellFactory((ListView<VDipoleCorr> vc) -> {
            ListCell cell = new ListCell<VDipoleCorr>() {
                @Override
                protected void updateItem(VDipoleCorr item, boolean empty) {
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

        if (pairVal != null) {
            dataH.remove(pairVal);
            comboBoxBPMH.getItems().add(pairVal.bpm);
            comboBoxHC.getItems().add((HDipoleCorr) pairVal.corrector);
            comboBoxBPMH.getItems().sort((bpm1, bpm2) -> Double.compare(bpm1.getSDisplay(), bpm2.getSDisplay()));
            comboBoxHC.getItems().sort((hc1, hc2) -> Double.compare(hc1.getSDisplay(), hc2.getSDisplay()));
        }

    }

    @FXML
    private void handleVerticalContextMenu(ActionEvent event) {

        final Pair pairVal = tableVerticalPairs.getSelectionModel().getSelectedItem();

        if (pairVal != null) {
            dataV.remove(pairVal);
            comboBoxBPMV.getItems().add(pairVal.bpm);
            comboBoxVC.getItems().add((VDipoleCorr) pairVal.corrector);
            comboBoxBPMV.getItems().sort((bpm1, bpm2) -> Double.compare(bpm1.getSDisplay(), bpm2.getSDisplay()));
            comboBoxVC.getItems().sort((vc1, vc2) -> Double.compare(vc1.getSDisplay(), vc2.getSDisplay()));
        }
    }

    @FXML
    private void handleAddPairH(ActionEvent event) {
        final HDipoleCorr hcVal = comboBoxHC.getSelectionModel().getSelectedItem();
        final BPM bpmVal = comboBoxBPMH.getSelectionModel().getSelectedItem();

        dataH.add(new Pair(bpmVal, hcVal, (phase.get(bpmVal).getx() - phase.get(hcVal).getx())));
        dataH.sort((pair1, pair2) -> Double.compare(pair1.bpm.getSDisplay(), pair2.bpm.getSDisplay()));
        comboBoxBPMH.getItems().remove(bpmVal);
        comboBoxHC.getItems().remove(hcVal);

    }

    @FXML
    private void handleAddPairV(ActionEvent event) {
        final VDipoleCorr vcVal = comboBoxVC.getSelectionModel().getSelectedItem();
        final BPM bpmVal = comboBoxBPMV.getSelectionModel().getSelectedItem();

        dataV.add(new Pair(bpmVal, vcVal, (phase.get(bpmVal).getx() - phase.get(vcVal).getx())));
        dataV.sort((pair1, pair2) -> Double.compare(pair1.bpm.getSDisplay(), pair2.bpm.getSDisplay()));
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

        private BPM bpm;
        private final StringProperty bpmName = new SimpleStringProperty();
        private DipoleCorr corrector;
        private final StringProperty correctorName = new SimpleStringProperty();
        private double phase;
        private final StringProperty phaseName = new SimpleStringProperty();

        private Pair(BPM bpm, DipoleCorr corrector, double phaseVal) {
            this.bpm = bpm;
            bpmName.set(bpm.toString());
            this.corrector = corrector;
            correctorName.set(corrector.toString());
            this.phase = phaseVal;
            phaseName.set(String.format("%.2f", phaseVal));
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

        public void setBPM(BPM bName) {
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

        public void setCorrectorName(DipoleCorr cName) {
            this.corrector = cName;
        }

        public final StringProperty phaseNameProperty() {
            return this.phaseName;
        }

        public final void setphaseName(final double phaseVal) {
            this.correctorNameProperty().set(String.format("%.4f", phaseVal));
        }

        public double getPhaseVal() {
            return phase;
        }

        public void setPhaseVal(double phaseVal) {
            phase = phaseVal;
        }

    }

}
