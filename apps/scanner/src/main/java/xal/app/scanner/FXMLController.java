package xal.app.scanner;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;

import xal.smf.AcceleratorNode;


public class FXMLController implements Initializable {

@FXML private TableView<TreeItem> scanTable;
@FXML private TableColumn<TreeItem, AcceleratorNode> scanColumnPV;
@FXML private TableColumn<TreeItem, CheckBox> scanColumnIncluded;
   

    @FXML
    private void handleScanAddPV(ActionEvent event) {
        System.out.println("You add a PV");
        CheckBox cb1 = new CheckBox();
        scanColumnIncluded.getColumns();
    }

    @FXML
    private void handleScanRemovePV(ActionEvent event) {
        System.out.println("You remove a PV");
    }
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        int j = 5;
    }    
}
