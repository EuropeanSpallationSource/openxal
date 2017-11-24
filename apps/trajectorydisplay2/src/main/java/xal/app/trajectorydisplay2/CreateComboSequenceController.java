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
package xal.app.trajectorydisplay2;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import xal.smf.AcceleratorSeq;

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

        sequence.forEach((seqItem) -> {
            items.add(seqItem.toString());
        });

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
