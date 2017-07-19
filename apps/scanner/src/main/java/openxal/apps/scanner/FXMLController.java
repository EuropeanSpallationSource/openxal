package openxal.apps.scanner;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

public class FXMLController implements Initializable {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TreeTableView<?> scanTree;

    @FXML
    private TreeTableColumn<?, ?> scanTablePV;

    @FXML
    private TreeTableColumn<?, ?> scanTableInclude;

    @FXML
    private ListView<?> run_execute;

    @FXML
    private ListView<String> analyseList;

    @FXML
    private LineChart<?, ?> graph01;
    
    private static ObservableList<String> measurements;
   

    @FXML
    private void handleScanAddPV(ActionEvent event) {
        FXMLFunctions.actionScanAddPV();
        //CheckBox cb1 = new CheckBox();
        //scanColumnIncluded.getColumns();
    }

    @FXML
    private void handleScanRemovePV(ActionEvent event) {
        FXMLFunctions.actionScanRemovePV();
    }

    @FXML
    private void handleRunExecute(ActionEvent event) {
        int[][] xy = FXMLFunctions.actionExecute();
        XYChart.Series series = new XYChart.Series();
        for (int i=0;i<10;i++) {
            series.getData().add( new XYChart.Data(String.valueOf(xy[0][i]), xy[1][i]) );
        }
        series.setName("Title " + (measurements.size()+1));
        graph01.getData().clear();
        graph01.getData().add(series);
        measurements.add("Measurement " + (measurements.size()+1));
    }
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert scanTree != null : "fx:id=\"scanTree\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert scanTablePV != null : "fx:id=\"scanTablePV\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert scanTableInclude != null : "fx:id=\"scanTableInclude\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert run_execute != null : "fx:id=\"run_execute\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert analyseList != null : "fx:id=\"analyseList\" was not injected: check your FXML file 'ScannerScene.fxml'.";
        assert graph01 != null : "fx:id=\"graph01\" was not injected: check your FXML file 'ScannerScene.fxml'.";

        measurements = FXCollections.observableArrayList();
        analyseList.setItems(measurements);
        FXMLFunctions.initialize();
    }
}
