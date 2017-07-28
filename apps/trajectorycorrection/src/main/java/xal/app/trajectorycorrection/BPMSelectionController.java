/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.trajectorycorrection;

import static java.lang.Double.max;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author nataliamilas
 */
public class BPMSelectionController implements Initializable {

    List<xal.smf.impl.BPM> BPMSelection = new ArrayList<>();
    List<xal.smf.AcceleratorSeq> seqItem = new ArrayList<>();
    private final BooleanProperty loggedIn = new SimpleBooleanProperty();
    private final BooleanProperty changedBPMList = new SimpleBooleanProperty();
    @FXML
    private GridPane gridPaneBPM;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        gridPaneBPM.setAlignment(Pos.CENTER);
        gridPaneBPM.setHgap(10);
        gridPaneBPM.setVgap(10);
        gridPaneBPM.setPadding(new Insets(25, 25, 25, 25)); 
       
    }
    
    public BooleanProperty loggedInProperty() {
        return loggedIn ;
    }

    public final boolean isLoggedIn() {
        return loggedInProperty().get();
    }

    public final void setLoggedIn(boolean loggedIn) {
        loggedInProperty().set(loggedIn);
    } 
    
    public BooleanProperty changedBPMListProperty() {
        return changedBPMList ;
    }
    
    public final boolean isChangedBPMList() {
        return loggedInProperty().get();
    }
    
    public final void setChangedBPMList(boolean changedBPMList) {
        changedBPMListProperty().set(changedBPMList);
    } 
    
    public boolean getChangedBPMList() {
        return changedBPMList.getValue();
    } 
    
    public List<xal.smf.impl.BPM> getBPMSelectionList(){
        return BPMSelection;
    }

    public void populateBPMGrid(xal.smf.Accelerator accl, List<xal.smf.impl.BPM> BPMs){
        int col =0;
        int row = 0;
        int maxRow = 0;
        seqItem = accl.getSequences();
        List<xal.smf.impl.BPM> BPM= accl.getAllNodesOfType("BPM");
        Text seqName;
        CheckBox checkBoxItem;
        
        for(xal.smf.AcceleratorSeq Seq: seqItem){ 
            seqName = new Text(Seq.toString());
            seqName.setFont(Font.font("System",FontWeight.BOLD,16));
            gridPaneBPM.setConstraints(seqName,col,row);
            gridPaneBPM.add(seqName,col,row);
            row++;
            checkBoxItem = new CheckBox("ALL IN");
            gridPaneBPM.setConstraints(checkBoxItem,col,row);
            gridPaneBPM.add(checkBoxItem,col,row);
            row++;
            checkBoxItem = new CheckBox("ALL OUT");
            gridPaneBPM.setConstraints(checkBoxItem,col,row);
            gridPaneBPM.add(checkBoxItem,col,row);
            BPM = accl.getSequence(Seq.toString()).getAllNodesOfType("BPM");
            for(xal.smf.impl.BPM item: BPM){
                row++;
                checkBoxItem = new CheckBox(item.toString());
                for(xal.smf.impl.BPM bpm: BPMs){
                    if(bpm == item){
                        checkBoxItem.setSelected(true);
                        BPMSelection.add(bpm);
                    }
                }
                gridPaneBPM.setConstraints(checkBoxItem,col,row);
                gridPaneBPM.add(checkBoxItem,col,row);
            }
            col++;
            maxRow = (int) max(maxRow,row);
            row = 0;
        }
        
        // Add listeners to all checkbox of the first row ("ALL" CheckBoxes)
        CheckBox selectedNode;
        for(int i=0; i<seqItem.size(); i++){
            selectedNode = (CheckBox) getNodeFromGridPane(gridPaneBPM,i,1);
            final Node seqNodeAllIn = selectedNode;
            selectedNode.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if(newValue){
                        toggleGridAllIn(seqNodeAllIn);
                    }
                }
            });
            selectedNode = (CheckBox) getNodeFromGridPane(gridPaneBPM,i,2);
            final Node seqNodeAllOut = selectedNode;
            selectedNode.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if(newValue){
                        toggleGridAllOut(seqNodeAllOut);
                    }
                }
            });
        }
        //Add button
        Button buttonSelect = new Button("Select");
        col = col-1;
        gridPaneBPM.add(buttonSelect,col,maxRow+1);
        buttonSelect.setMaxWidth(100);
        
        buttonSelect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                CheckBox selectedNode;
                BPMSelection.clear();
                for(int i=0; i<seqItem.size(); i++){
                    List<xal.smf.impl.BPM> BPM = seqItem.get(i).getAllNodesOfType("BPM");
                    for(int j=3; j<BPM.size()+3; j++){
                        selectedNode = (CheckBox) getNodeFromGridPane(gridPaneBPM,i,j);
                        if(selectedNode.isSelected()){
                            BPMSelection.add(BPM.get(j-3));
                        }
                    }
                    
                }
                setChangedBPMList(true);
                setLoggedIn(true);
            }
        });   
        
        //Add button
        Button buttonCancel = new Button("Cancel");
        col = col-1;
        gridPaneBPM.add(buttonCancel,col,maxRow+1);
        buttonCancel.setMaxWidth(100);
        
        buttonCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                setChangedBPMList(false);
                setLoggedIn(true);
            }
        });
        
    }
    
    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }
    
    private void toggleGridAllIn(Node selectedNode){
        CheckBox bpmNode;
        int index = (int) gridPaneBPM.getColumnIndex(selectedNode);
        List<xal.smf.impl.BPM> BPM = seqItem.get(index).getAllNodesOfType("BPM");
        bpmNode = (CheckBox) getNodeFromGridPane(gridPaneBPM,index,2);
        bpmNode.setSelected(false);
        for(int j=3; j<BPM.size()+3; j++){
            bpmNode = (CheckBox) getNodeFromGridPane(gridPaneBPM,index,j);
            bpmNode.setSelected(true);
        }
    }
    
    private void toggleGridAllOut(Node selectedNode){
        CheckBox bpmNode;
        int index = (int) gridPaneBPM.getColumnIndex(selectedNode);
        List<xal.smf.impl.BPM> BPM = seqItem.get(index).getAllNodesOfType("BPM");
        bpmNode = (CheckBox) getNodeFromGridPane(gridPaneBPM,index,1);
        bpmNode.setSelected(false);
        for(int j=3; j<BPM.size()+3; j++){
            bpmNode = (CheckBox) getNodeFromGridPane(gridPaneBPM,index,j);
            bpmNode.setSelected(false);
        }
    }


    @FXML
    private void handleBPMSelection(MouseEvent event) {        
        CheckBox selectedNode;
        for(int i=0; i<seqItem.size(); i++){
            selectedNode = (CheckBox) getNodeFromGridPane(gridPaneBPM,i,0);
            if(selectedNode.isSelected()){
                List<xal.smf.impl.BPM> BPM = seqItem.get(i).getAllNodesOfType("BPM");
                for(int j=1; j<=BPM.size(); j++){
                    selectedNode = (CheckBox) getNodeFromGridPane(gridPaneBPM,i,j);
                    selectedNode.setSelected(true);
                }
            }
        }
    }
    
}
