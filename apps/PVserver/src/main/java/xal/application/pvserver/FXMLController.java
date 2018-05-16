package xal.application.pvserver;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

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
    private TableColumn<PV, Boolean> pvWritableColumn;
    @FXML
    private TableColumn<PV, String> pvValuecolumn;

    private Boolean serverRunning = false;

    @FXML
    private void handleAddPVButtonAction(ActionEvent event) {
        // Create a dialog to input a PV.
        Dialog<PV> dialog = new Dialog<>();
        dialog.setTitle("New PV");
        dialog.setHeaderText("PV");

        // Set the button types.
        ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Create the PV properties labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField pvName = new TextField();
        pvName.setPromptText("PV name");
        CheckBox pvWrittable = new CheckBox();
        TextField pvValue = new TextField();
        pvValue.setPromptText("Value");

        grid.add(new Label("PV name:"), 0, 0);
        grid.add(pvName, 1, 0);
        grid.add(new Label("Writtable:"), 0, 1);
        grid.add(pvWrittable, 1, 1);
        grid.add(new Label("Value:"), 0, 2);
        grid.add(pvValue, 1, 2);

        // Enable/Disable ok button depending on whether all fields are filled.
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        // Enable OK button only when all fields are filled.
        pvName.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || pvValue.getText().trim().isEmpty());
        });
        pvValue.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || pvName.getText().trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the PV name field by default.
        Platform.runLater(() -> pvName.requestFocus());

        // Convert the result to a new PV when the OK button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new PV(pvName.getText(), pvWrittable.isSelected(), pvValue.getText());
            }
            return null;
        });

        Optional<PV> result = dialog.showAndWait();

        result.ifPresent(pv -> {
            ObservableList<PV> pvs = pvTableView.getItems();
            pvs.add(pv);
            pvTableView.setItems(pvs);
            if (startServerButton.isDisabled()) {
                startServerButton.setDisable(false);
            }
        });
    }

    @FXML
    private void handleRemovePVButtonAction(ActionEvent event) {
        ObservableList<PV> pvs = pvTableView.getItems();
        int selectedPV = pvTableView.getSelectionModel().getSelectedCells().get(0).getRow();
        pvs.remove(selectedPV);
        pvTableView.setItems(pvs);

        checkSelection(null);
        if (pvs.isEmpty()) {
            startServerButton.setDisable(true);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        removePVButton.setDisable(true);
        pvTableView.setOnMouseClicked(e -> checkSelection(e));

        // 
        pvNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        pvWritableColumn.setCellValueFactory(new PropertyValueFactory<>("writable"));
        pvValuecolumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        // Make cells editable
        pvNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        pvWritableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(pvWritableColumn));
        pvValuecolumn.setCellFactory(TextFieldTableCell.forTableColumn());

        pvNameColumn.setOnEditCommit(cell -> {
            PV pv = cell.getRowValue();
            pv.setName(cell.getNewValue());
            if (serverRunning) {
                PVServer.getInstance().updatePVs();
            }
        });

        pvWritableColumn.setCellValueFactory(cell -> cell.getValue().writableProperty());

        pvWritableColumn.setOnEditCommit(data -> {
            if (serverRunning) {
                PVServer.getInstance().updatePVs();
            }
        });

        pvValuecolumn.setOnEditCommit(data -> {
            PV pv = data.getRowValue();
            pv.setValue(data.getNewValue());
            if (serverRunning) {
                PVServer.getInstance().updatePVs();
            }
        });

        PVServer pvServer = PVServer.getInstance();
        pvServer.setPvs(pvTableView.getItems());

        startServerButton.setDisable(true);
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
        if (serverRunning) {
            //stop server
            PVServer pvServer = PVServer.getInstance();
            pvServer.stopServer();
            startServerButton.setText("Start server");
            serverRunning = false;
            addPVButton.setDisable(false);
            checkSelection(null);
        } else {
            //start server
            PVServer pvServer = PVServer.getInstance();
            pvServer.startServer();
            startServerButton.setText("Stop server");
            serverRunning = true;
            addPVButton.setDisable(true);
            removePVButton.setDisable(true);

            // TODO: disable name column when server is running.
        }
    }
}
