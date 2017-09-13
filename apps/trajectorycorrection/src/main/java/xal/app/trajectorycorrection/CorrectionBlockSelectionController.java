/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.trajectorycorrection;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author nataliamilas
 */
public class CorrectionBlockSelectionController implements Initializable {

    private xal.smf.Accelerator accelerator;
    private Map<String,CorrectionBlock> blocks = new HashMap<String,CorrectionBlock>(); //List of defined Blocks
    private Map<String,CorrectionBlock> blocksSelected = new HashMap<String,CorrectionBlock>(); //List selected blocks (can be used in correction)
    private ObservableList<String> selected = FXCollections.observableArrayList();
    private ObservableList<String> defined = FXCollections.observableArrayList();
    private final BooleanProperty loggedIn = new SimpleBooleanProperty();
    private final BooleanProperty selectionList = new SimpleBooleanProperty();
    private final ObjectProperty<ListCell<String>> dragSource = new SimpleObjectProperty<>();
    @FXML
    private ListView<String> listViewBlockDefinition = new ListView<String>();
    @FXML
    private ListView<String> listViewBlockSelection = new ListView<String>();


    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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
    
    public BooleanProperty changedSelectionListProperty() {
        return selectionList ;
    }
    
    public final boolean isChangedSelectionList() {
        return changedSelectionListProperty().get();
    }
    
    public final void setChangedSelectionList(boolean selectionList) {
        changedSelectionListProperty().set(selectionList);
    } 
    
    public boolean getChangedSelectionList() {
        return selectionList.getValue();
    } 
    
    public void initiateElements(xal.smf.Accelerator accl,List<CorrectionBlock> inputblocks, List<CorrectionBlock> inputblocksSelected){
        this.accelerator = accl;
        for(CorrectionBlock item: inputblocks){
            this.blocks.put(item.getBlockName(), item);
            this.defined.add(item.getBlockName());
        }
        listViewBlockDefinition.setItems(this.defined);
        listViewBlockDefinition.setCellFactory(TextFieldListCell.forListView());
        listViewBlockDefinition.setEditable(true);
        listViewBlockDefinition.setOnEditCommit(new EventHandler<ListView.EditEvent<String>>() {
            @Override
            public void handle(ListView.EditEvent<String> t) {
                    String oldName = listViewBlockDefinition.getItems().get(t.getIndex());
                    listViewBlockDefinition.getItems().set(t.getIndex(), t.getNewValue());
                    defined.set(t.getIndex(), t.getNewValue());
                    blocks.put(t.getNewValue(), blocks.remove(oldName));
                    blocks.get(t.getNewValue()).setBlockName(t.getNewValue());
            }

        });
        
                
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

        
        for(CorrectionBlock item: inputblocksSelected){
            this.blocksSelected.put(item.getBlockName(), item);
            this.selected.add(item.getBlockName());
        }
        listViewBlockSelection.setItems(this.selected);
    }
   
    public List<CorrectionBlock> getDefinedBlocks(){
        List<CorrectionBlock> returnList = new ArrayList<>();
        blocks.keySet().forEach((string) -> returnList.add(blocks.get(string)));
        return returnList;
    }
    
    public List<CorrectionBlock> getSelectedBlocks(){
        List<CorrectionBlock> returnList = new ArrayList<>();
        selected.forEach((string) -> returnList.add(blocksSelected.get(string)));
        return returnList;
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
                                blocks.put(newBlock.getBlockName(),newBlock);
                                defined.add(newBlock.getBlockName());
                            }        
                            stage.close();
                        }
                    });
                    loginController.populateElementGrid(accelerator);
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
                        Logger.getLogger(CorrectionBlockSelectionController.class.getName()).log(Level.SEVERE, null, ex1);
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
                                blocks.get(listViewBlockDefinition.getSelectionModel().getSelectedItem()).setBlockBPM(loginController.getBPMSelectionList());
                                blocks.get(listViewBlockDefinition.getSelectionModel().getSelectedItem()).setBlockHC(loginController.getHCSelectionList());
                                blocks.get(listViewBlockDefinition.getSelectionModel().getSelectedItem()).setBlockVC(loginController.getVCSelectionList());
                            }
                            stage.close();
                        }
                    });
                    loginController.populateElementGrid(accelerator);
                    loginController.enterElementstoEdit(blocks.get(listViewBlockDefinition.getSelectionModel().getSelectedItem()));
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
                        Logger.getLogger(CorrectionBlockSelectionController.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        });
        
        deleteBlock.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                int index = listViewBlockDefinition.getSelectionModel().getSelectedIndex();
                String removeName = listViewBlockDefinition.getSelectionModel().getSelectedItem();
                if(removeName != null){
                    blocks.remove(removeName);
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
            blocks.put(removeName,blocksSelected.get(removeName));
            defined.add(removeName);
            //defined.sorted();
            blocksSelected.remove(removeName);
            selected.remove(removeName);
            //selected.sorted();
            listViewBlockSelection.getSelectionModel().clearSelection();
        }
    }
    
    @FXML
    private void handleSelectedBlock(ActionEvent event) {
        String includeName = listViewBlockDefinition.getSelectionModel().getSelectedItem();
        if(includeName != null){            
            blocksSelected.put(includeName, blocks.get(includeName));
            selected.add(includeName);
            //selected.sorted();
            blocks.remove(includeName);
            defined.remove(includeName);
            //defined.sorted();
            listViewBlockDefinition.getSelectionModel().clearSelection();
        }
   
    }

    @FXML
    private void handleButtonSelect(ActionEvent event) {
        setChangedSelectionList(true);
        setLoggedIn(true);
    }

    @FXML
    private void handleButtonCancel(ActionEvent event) {
        setChangedSelectionList(false);
        setLoggedIn(true);
    }
    
}
