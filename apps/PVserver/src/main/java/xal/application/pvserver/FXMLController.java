package xal.application.pvserver;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
/**
 * 
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class FXMLController implements Initializable {

    @FXML
    private Button addPVButton;
    @FXML
    private Button removePVButton;
    @FXML
    private Button startServerButton;
    @FXML
    private TableView<PV> pvTableView;
    @FXML
    private TableColumn<PV, String> pvNameColumn;
    @FXML
    private TableColumn<PV, Boolean> pvReadColumn;
    @FXML
    private TableColumn<PV, Boolean> pvWriteColumn;
    @FXML
    private TableColumn<PV, String> pvValuecolumn;

    private Boolean serverRunning = false;
    
    
    @FXML
    private void handleAddPVButtonAction(ActionEvent event) {
        ObservableList<PV> pvs = pvTableView.getItems();
        pvs.add(new PV());
        pvTableView.setItems(pvs);
    }

    @FXML
    private void handleRemovePVButtonAction(ActionEvent event) {
        ObservableList<PV> pvs = pvTableView.getItems();
        pvs.remove(pvTableView.getSelectionModel().getSelectedCells().get(0).getRow());
        pvTableView.setItems(pvs);

        checkSelection(null);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        removePVButton.setDisable(true);
        pvTableView.setOnMouseClicked(e -> checkSelection(e));

        // 
        pvNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        pvReadColumn.setCellValueFactory(new PropertyValueFactory<>("read"));
        pvWriteColumn.setCellValueFactory(new PropertyValueFactory<>("write"));
        pvValuecolumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        // Make cells editable
        pvNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        pvReadColumn.setCellFactory(CheckBoxTableCell.forTableColumn(pvReadColumn));
        pvWriteColumn.setCellFactory(CheckBoxTableCell.forTableColumn(pvWriteColumn));
        pvValuecolumn.setCellFactory(TextFieldTableCell.forTableColumn());

        pvNameColumn.setOnEditCommit(cell -> {
            PV pv = cell.getRowValue();
            pv.setName(cell.getNewValue());
        });

        pvReadColumn.setCellValueFactory(cell -> cell.getValue().readProperty());

        pvWriteColumn.setCellValueFactory(cell -> cell.getValue().writeProperty());

        pvValuecolumn.setOnEditCommit(data -> {
            PV pv = data.getRowValue();
            pv.setValue(data.getNewValue());
        });
    }

    private void checkSelection(MouseEvent e) {
        if (!pvTableView.getSelectionModel().getSelectedCells().isEmpty()) {
            removePVButton.setDisable(false);
        } else {
            removePVButton.setDisable(true);
        }
    }

    @FXML
    private void handleStartServerButtonAction(ActionEvent event) {
        if (serverRunning){
            //stop server
            startServerButton.setText("Start server");
            serverRunning = false;
        } else {
            //start server
            startServerButton.setText("Stop server");
            serverRunning = true;
        }
    }
}
