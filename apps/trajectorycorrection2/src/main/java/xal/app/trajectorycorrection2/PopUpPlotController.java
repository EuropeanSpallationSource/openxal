/*
 * PopUPPlotController.java
 *
 * Created by Natalia Milas on 07.07.2017
 *
 * Copyright (c) 2017 European Spallation Source ERIC
 * Tunavägen 20
 * Lund, Sweden
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package xal.app.trajectorycorrection2;

/**
 * Secondary Controller that holds a plot that displays an orbit
 * @author nataliamilas
 * 06-2017
 */


import java.io.IOException;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author nataliamilas
 */
public class PopUpPlotController{

    private final BooleanProperty loggedIn = new SimpleBooleanProperty();
    //Setup the Plot
    //final XYChart.Series Horizontal = new XYChart.Series();          
    //final XYChart.Series Vertical = new XYChart.Series(); 
    
    final ObservableList<XYChart.Data<Double, Double>> Horizontal = FXCollections.observableArrayList();
    final ObservableList<XYChart.Data<Double, Double>> Vertical = FXCollections.observableArrayList();
    
    List<xal.smf.impl.BPM> BPMs = new ArrayList<>();;
    @FXML
    private Button buttonClose;
    @FXML
    private LineChart<Number,Number> referencePlot;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private NumberAxis xAxis;
    
    public BooleanProperty loggedInProperty() {
        return loggedIn ;
    }

    public final boolean isLoggedIn() {
        return loggedInProperty().get();
    }

    public final void setLoggedIn(boolean loggedIn) {
        loggedInProperty().set(loggedIn);
    } 

    @FXML
    public void handleButtonClose(){
        setLoggedIn(true);
      
    } 
    
    public void updatePlot(TrajectoryArray Traj){
        double valMaxY=0;
        double valMinY=0;
        double valMaxX=0;
        double valMinX=1000;

        //clear the initial values
        Horizontal.clear();
        Vertical.clear();
        
        for(xal.smf.impl.BPM item : Traj.Pos.keySet()){
            Horizontal.add(new XYChart.Data(Traj.Pos.get(item),Traj.XRef.get(item)));
            valMaxY = max(valMaxY,Traj.XRef.get(item));
            valMinY = min(valMinY,Traj.XRef.get(item));
            Vertical.add(new XYChart.Data(Traj.Pos.get(item),Traj.YRef.get(item)));
            //Vertical.setNode(new HoveredThresholdNode(Traj.YRef.get(item)-0.5,Traj.YRef.get(item)+0.5,item.toString()));
            valMaxY = max(valMaxY,Traj.YRef.get(item));
            valMinY = min(valMinY,Traj.YRef.get(item));
            BPMs.add(item);
            valMaxX = max(valMaxX,Traj.Pos.get(item));
            valMinX = min(valMinX,Traj.Pos.get(item));
            
        }
        
        for(int i=0; i<BPMs.size(); i++){
            Horizontal.get(i).setNode(new HoveredThresholdNode(0,BPMs.get(i).toString()));
            Vertical.get(i).setNode(new HoveredThresholdNode(1,BPMs.get(i).toString()));   
        }
        
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(valMinX-0.1*valMaxX);
        xAxis.setUpperBound(1.1*valMaxX);
        xAxis.setTickUnit((1.2*valMaxX-valMinX)/10);
        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(valMaxY+0.5);
        yAxis.setLowerBound(valMinY-0.5);
        yAxis.setTickUnit((valMaxY-valMinY+1)/10);
        
        referencePlot.setCursor(Cursor.CROSSHAIR);
        referencePlot.getData().add(new XYChart.Series("Horizontal",Horizontal));
        referencePlot.getData().add(new XYChart.Series("Vertical",Vertical));
        
    }
    
    @FXML
    private void handlePlotProperties(ContextMenuEvent event) {
        
        final MenuItem plotProperties = new MenuItem("Plot Properties");
        final ContextMenu menu = new ContextMenu(plotProperties);
        
        
        plotProperties.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
            
                Stage stage; 
                Parent root;
                URL    url  = null;
                String sceneFile = "/fxml/PlotProperties.fxml";
                try
                {
                    stage = new Stage();
                    url  = getClass().getResource(sceneFile);
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(MainApp.class.getResource(sceneFile));
                    root = loader.load();
                    root.getStylesheets().add("/styles/Styles.css");
                    stage.setScene(new Scene(root));
                    stage.setTitle("Plot Properties");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initOwner(buttonClose.getScene().getWindow());
                    PlotPropertiesController loginController = loader.getController();
                    loginController.setPlotProperties(xAxis.getUpperBound(),xAxis.getLowerBound(),xAxis.isAutoRanging(),yAxis.getUpperBound(),yAxis.getLowerBound(),yAxis.isAutoRanging());
                    loginController.loggedInProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasLoggedIn, Boolean isNowLoggedIn) -> {
                        if (isNowLoggedIn) {
                            xAxis.setAutoRanging(loginController.getxAutoscale());
                            xAxis.setUpperBound(loginController.getxMax());
                            xAxis.setLowerBound(loginController.getxMin());
                            yAxis.setAutoRanging(loginController.getyAutoscale());
                            yAxis.setUpperBound(loginController.getyMax());
                            yAxis.setLowerBound(loginController.getyMin());
                            yAxis.setTickUnit((loginController.getyMax()-loginController.getyMin())/10);
                            stage.close();
                        }
                    });
                    stage.showAndWait();
                }
                catch ( IOException ex )
                {
                    System.out.println( "Exception on FXMLLoader.load()" );
                    System.out.println( "  * url: " + url );
                    System.out.println( "  * " + ex );
                    System.out.println( "    ----------------------------------------\n" );
                }   
            }
        });
        
        menu.show(buttonClose.getScene().getWindow(), event.getScreenX(), event.getScreenY());
        
    }
    
    /** a node which displays a value on hover, but is otherwise empty */
    class HoveredThresholdNode extends StackPane {
        HoveredThresholdNode(int hoverplane, String name) {
            setPrefSize(15, 15);

            final Label label = createDataThresholdLabel(hoverplane,name);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
              @Override public void handle(MouseEvent mouseEvent) {
                getChildren().setAll(label);
                setCursor(Cursor.NONE);
                toFront();
              }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
              @Override public void handle(MouseEvent mouseEvent) {
                getChildren().clear();
                setCursor(Cursor.CROSSHAIR);
              }
            });
        }

        private Label createDataThresholdLabel(int hoverplane, String name) {
            final Label label = new Label(name);
            if(hoverplane == 0){
              label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            } else {
              label.getStyleClass().addAll("default-color1", "chart-line-symbol", "chart-series-line");  
            }
            label.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
            label.setTextFill(Color.BLACK);

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }
    
}
