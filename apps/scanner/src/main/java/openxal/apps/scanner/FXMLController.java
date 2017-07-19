package openxal.apps.scanner;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import org.gillius.jfxutils.chart.ChartZoomManager;

public class FXMLController implements Initializable {

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
    private StackPane analyseGraphPane;

    @FXML
    private LineChart<Number, Number> graph01;

    @FXML
    private Rectangle selectRect;

    private static ObservableList<String> measurements;

    private static ChartZoomManager zoomManager;

    @FXML
    private void handleScanAddPV(ActionEvent event) {
        FXMLFunctions.actionScanAddPV();
        try {
            //Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("/fxml/AddPV.fxml"));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/AddPV.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Add PV");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch (IOException e) {
            System.out.println("Error opening Add PV window");
        }
    }

    @FXML
    private void handleScanRemovePV(ActionEvent event) {
        FXMLFunctions.actionScanRemovePV();
    }

    private void plotLine(double [][] xy) {
        if (xy != null) {
            XYChart.Series<Number, Number> series = new XYChart.Series();
            for (int i=0;i<10;i++) {
                series.getData().add( new XYChart.Data(xy[0][i], xy[1][i]) );
            }
            series.setName("Title " + (measurements.size()+1));
            graph01.getData().add(series);
        }
    }

    @FXML
    private void handleRunExecute(ActionEvent event) {
        double[][] xy = FXMLFunctions.actionExecute();
        graph01.getData().clear();
        plotLine(xy);
        measurements.add("Measurement " + (measurements.size()+1));
        analyseList.getSelectionModel().clearAndSelect(measurements.size()-1);
        analyseList.autosize();
    }

    private void dummyBugFunction() {
        // Needed due to bug in getSelectedItems()
    }

    @FXML
    private void analyseListMouseClicked(MouseEvent event) {
        graph01.getData().clear();
        zoomManager.stop();
        // There is a bug in getSelectedItems() so need to call this twice..
        analyseList.getSelectionModel().getSelectedItems().forEach((meas) -> dummyBugFunction());
        analyseList.getSelectionModel().getSelectedItems().forEach((meas) -> plotLine(FXMLFunctions.dataSets.get(meas)));
        zoomManager.start();
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

        analyseList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        FXMLFunctions.initialize();

        // Allow zooming in the chart..
        zoomManager = new ChartZoomManager( analyseGraphPane, selectRect, graph01 );
        zoomManager.setMouseWheelZoomAllowed(true);
        zoomManager.setZoomDurationMillis(100);
        zoomManager.start();
    }
}
