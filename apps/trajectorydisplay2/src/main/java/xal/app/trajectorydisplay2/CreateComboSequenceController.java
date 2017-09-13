/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.trajectorydisplay2;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;

/**
 * FXML Controller class
 *
 * @author nataliamilas
 */
public class CreateComboSequenceController{

    private final BooleanProperty loggedIn = new SimpleBooleanProperty();
    private final StringProperty comboName = new SimpleStringProperty();
    private List<AcceleratorSeq> newComboSequence;
    private List<AcceleratorSeq> sequence;

    @FXML
    private ListView<String> listStart;
    @FXML
    private ListView<String> listEnd;
    @FXML
    private Button buttonOK;
    @FXML
    private Button buttonCancel;
    
    public BooleanProperty loggedInProperty() {
        return loggedIn ;
    }

    public final boolean isLoggedIn() {
        return loggedInProperty().get();
    }

    public final void setLoggedIn(boolean loggedIn) {
        loggedInProperty().set(loggedIn);
    } 
    
    public StringProperty newComboNameProperty() {
        return comboName;
    }

    public final String getComboName() {
        return newComboNameProperty().get();
    }

    public final void setComboName(String comboName) {
        newComboNameProperty().set(comboName);
    } 
    
    public List<AcceleratorSeq>  getNewComboSequence() {
        return newComboSequence;
    }

    public void setNewComboSequence(List<AcceleratorSeq>  newComboSequence) {
        this.newComboSequence = newComboSequence;
    }
    
    public void setProperties(xal.smf.Accelerator accl){
        
        sequence = accl.getSequences();
        ObservableList<String> items = FXCollections.observableArrayList();
        
        for(AcceleratorSeq seqItem: sequence){
            items.add(seqItem.toString());
        }
        
        listStart.setItems(items);
        listEnd.setItems(items);

    }

    @FXML
    private void handleOkButton(ActionEvent event) {
        
        int startSeq = listStart.getSelectionModel().getSelectedIndex();
        int endSeq = listEnd.getSelectionModel().getSelectedIndex();
        List<AcceleratorSeq> newCombo = new ArrayList<>();
        
        for(int i=startSeq; i<=endSeq; i++){
           newCombo.add(sequence.get(i));
        }
        
        setNewComboSequence(newCombo);
        setComboName(listStart.getSelectionModel().getSelectedItem()+"-"+listEnd.getSelectionModel().getSelectedItem()); 
        setLoggedIn(true);
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        setLoggedIn(true);
    }
}
