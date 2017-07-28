/*
 * FXMLController.java
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

package xal.app.trajectorydisplay;

/**
 * Trajectory display. 
 * @author nataliamilas
 * 06-2017
 */

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xal.ca.ConnectionException;
import xal.ca.GetException;

public class FXMLController implements Initializable {
    
    //Creates the Accelerator
    public xal.smf.Accelerator accl = xal.smf.data.XMLDataManager.acceleratorWithPath("/Users/nataliamilas/projects/openxal/site/optics/design/main.xal");
    public TrajectoryArray DisplayTraj = new TrajectoryArray();//Trajectory to be displayed on the plot
   
    //set plot update timer
    private AnimationTimer timerPlotUpdate;
    
    //Setup the Plot
    final XYChart.Series Horizontal = new XYChart.Series();          
    final XYChart.Series Vertical = new XYChart.Series();  
    final XYChart.Series Charge = new XYChart.Series();  
    
    //Setup the Plot
    final XYChart.Series HorizontalMarker = new XYChart.Series();          
    final XYChart.Series VerticalMarker = new XYChart.Series();  
    final XYChart.Series ChargeMarker = new XYChart.Series(); 
    
    private ToggleGroup groupSequence = new ToggleGroup();//Toggle for group
    
    @FXML
    private LineChart<Number, Number> plotHorizontal;
    @FXML
    private LineChart<Number, Number> plotVertical;
    @FXML
    private LineChart<Number, Number> plotCharge;
    @FXML
    private Label labelXrms;
    @FXML
    private Label labelYrms;
    
    
    //Menu settings
    @FXML
    private MenuItem menuExit;
    @FXML
    private Menu menuSequence;
    @FXML
    private NumberAxis axisHorizontal;
    @FXML
    private NumberAxis axisVertical;
    @FXML
    private NumberAxis axisCharge;
    @FXML
    private NumberAxis axisPosition;
    @FXML
    private NumberAxis axisPositionX;
    @FXML
    private NumberAxis axisPositionY;
    @FXML
    private GridPane gridpaneCursor;
    @FXML
    private Label labelCoordinates;
    @FXML
    private Label labelBPM;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Populate the Accelerator Menu with the sequences of the machine
        List<xal.smf.AcceleratorSeq> seqItem = accl.getSequences();
        int k = 0;
        for(xal.smf.AcceleratorSeq item: seqItem){ //AddSequences
            menuSequence.getItems().add(new RadioMenuItem(item.toString()));
            RadioMenuItem addedItem = (RadioMenuItem) menuSequence.getItems().get(k);
            addedItem.setToggleGroup(groupSequence);
            if(k==0){
                groupSequence.selectToggle(addedItem);
            }
            k++;
        }      
        
        menuSequence.getItems().add(new SeparatorMenuItem());
        
        List<xal.smf.AcceleratorSeqCombo> seqCombo = accl.getComboSequences();
        k++;
        for(xal.smf.AcceleratorSeqCombo item: seqCombo){ //AddCombos
            menuSequence.getItems().add(new RadioMenuItem(item.toString()));
            RadioMenuItem addedItem = (RadioMenuItem) menuSequence.getItems().get(k);
            addedItem.setToggleGroup(groupSequence);
            if(k==0){
                groupSequence.selectToggle(addedItem);
            }
            k++;
        }   
        
        groupSequence.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
        public void changed(ObservableValue<? extends Toggle> ov,Toggle old_toggle, Toggle new_toggle) {
            if(old_toggle != new_toggle){
                //turn off plot update timer
                timerPlotUpdate.stop();        

                gridpaneCursor.setVisible(false);

                HorizontalMarker.getData().clear();
                VerticalMarker.getData().clear();
                ChargeMarker.getData().clear();

                //turn on timer
                timerPlotUpdate.start();
            }
      }
    });
        
        //reads a new trajectory
        try {
            RadioMenuItem getSeqName = (RadioMenuItem) groupSequence.getSelectedToggle();
            DisplayTraj.readTrajectory(accl, getSeqName.getText());
        } catch (ConnectionException | GetException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Create legend names
        String nameHor ="x_rms = "+String.format("%.3f",DisplayTraj.getXrms())+" mm";
        String nameVer ="y_rms = "+String.format("%.3f",DisplayTraj.getYrms())+" mm";
        
        //Initializes the chart and chartData
        labelXrms.setText(nameHor);
        labelYrms.setText(nameVer);
    
        // update the data series
        updateDataset(DisplayTraj);
        
        double minVal = 1000;
        double maxVal = 0;
        for(xal.smf.impl.BPM item: DisplayTraj.Pos.keySet()){
            minVal = min(minVal,DisplayTraj.Pos.get(item));
            maxVal = max(maxVal,DisplayTraj.Pos.get(item));
        }
        
        axisPosition.setAutoRanging(false);
        axisPositionX.setAutoRanging(false);
        axisPositionY.setAutoRanging(false);
        axisPosition.setLowerBound(round(90*minVal)/100.0);
        axisPositionX.setLowerBound(round(90*minVal)/100.0);
        axisPositionY.setLowerBound(round(90*minVal)/100.0);
        axisPosition.setUpperBound(round(105*maxVal)/100.0);
        axisPositionX.setUpperBound(round(105*maxVal)/100.0);
        axisPositionY.setUpperBound(round(105*maxVal)/100.0);
        axisPosition.setTickUnit(round(105*maxVal-90*minVal)/1000.0);
        axisPositionX.setTickUnit(round(105*maxVal-90*minVal)/1000.0);
        axisPositionY.setTickUnit(round(105*maxVal-90*minVal)/1000.0);

        plotHorizontal.setAnimated(false);
        plotVertical.setAnimated(false);
        plotCharge.setAnimated(false);
        
        //puts the trajectories on the chart
        plotHorizontal.getData().add(Horizontal);
        plotVertical.getData().add(Vertical);
        plotCharge.getData().add(Charge);  
        plotHorizontal.getData().add(HorizontalMarker);
        plotVertical.getData().add(VerticalMarker);
        plotCharge.getData().add(ChargeMarker); 
        
        //remove legend from plots
        plotHorizontal.setLegendVisible(false);
        plotVertical.setLegendVisible(false);
        plotCharge.setLegendVisible(false);
        
        timerPlotUpdate = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    RadioMenuItem getSeqName = (RadioMenuItem) groupSequence.getSelectedToggle();
                    DisplayTraj.readTrajectory(accl, getSeqName.getText());
                } catch (ConnectionException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
                //updates the dataset
                updateDataset(DisplayTraj);
                
                if(!axisPosition.autoRangingProperty().get()){
                    double minVal = 1000;
                    double maxVal = 0;
                    for(xal.smf.impl.BPM item: DisplayTraj.Pos.keySet()){
                        minVal = min(minVal,DisplayTraj.Pos.get(item));
                        maxVal = max(maxVal,DisplayTraj.Pos.get(item));
                    }

                    axisPosition.setLowerBound(round(90*minVal)/100.0);
                    axisPositionX.setLowerBound(round(90*minVal)/100.0);
                    axisPositionY.setLowerBound(round(90*minVal)/100.0);
                    axisPosition.setUpperBound(round(105*maxVal)/100.0);
                    axisPositionX.setUpperBound(round(105*maxVal)/100.0);
                    axisPositionY.setUpperBound(round(105*maxVal)/100.0);
                    axisPosition.setTickUnit(round(105*maxVal-90*minVal)/1000.0);
                    axisPositionX.setTickUnit(round(105*maxVal-90*minVal)/1000.0);
                    axisPositionY.setTickUnit(round(105*maxVal-90*minVal)/1000.0);
                }
                
            }
                
        };
        
        timerPlotUpdate.start();  
            
    }   
    
    @FXML
    private void handleExitMenu(ActionEvent event) {
        System.exit(0);
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
                    //root.getStylesheets().add("/styles/Styles.css");
                    stage.setScene(new Scene(root));
                    stage.setTitle("Plot Properties");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initOwner(labelXrms.getScene().getWindow());
                    PlotPropertiesController loginController = loader.getController();
                    loginController.setPlotProperties(axisHorizontal, axisVertical, axisCharge, axisPosition);
                    loginController.loggedInProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasLoggedIn, Boolean isNowLoggedIn) -> {
                        if (isNowLoggedIn) {
                            axisHorizontal.setAutoRanging(loginController.getxAutoscale());
                            axisHorizontal.setUpperBound(loginController.getxMax());
                            axisHorizontal.setLowerBound(loginController.getxMin());
                            axisVertical.setTickUnit((loginController.getxMax()-loginController.getxMin())/10);
                            axisVertical.setAutoRanging(loginController.getyAutoscale());
                            axisVertical.setUpperBound(loginController.getyMax());
                            axisVertical.setLowerBound(loginController.getyMin());
                            axisVertical.setTickUnit((loginController.getyMax()-loginController.getyMin())/10);
                            axisCharge.setAutoRanging(loginController.getcAutoscale());
                            axisCharge.setUpperBound(loginController.getcMax());
                            axisCharge.setLowerBound(loginController.getcMin());
                            axisCharge.setTickUnit((loginController.getcMax()-loginController.getcMin())/10);
                            axisPosition.setAutoRanging(loginController.getpAutoscale());
                            axisPosition.setUpperBound(loginController.getpMax());
                            axisPosition.setLowerBound(loginController.getpMin());
                            axisPosition.setTickUnit((loginController.getpMax()-loginController.getpMin())/10);
                            axisPositionX.setAutoRanging(loginController.getpAutoscale());
                            axisPositionX.setUpperBound(loginController.getpMax());
                            axisPositionX.setLowerBound(loginController.getpMin());
                            axisPositionX.setTickUnit((loginController.getpMax()-loginController.getpMin())/10);
                            axisPositionY.setAutoRanging(loginController.getpAutoscale());
                            axisPositionY.setUpperBound(loginController.getpMax());
                            axisPositionY.setLowerBound(loginController.getpMin());
                            axisPositionY.setTickUnit((loginController.getpMax()-loginController.getpMin())/10);
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
        
        menu.show(labelXrms.getScene().getWindow(), event.getScreenX(), event.getScreenY());
      
    }
    
    @FXML
    private void handleSelectBPM(MouseEvent event) {
        Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
        double x = axisPosition.sceneToLocal(mouseSceneCoords).getX();
        double pos = (double) axisPosition.getValueForDisplay(x);
        
        //Find the closest data point 
        RadioMenuItem getSeqName = (RadioMenuItem) groupSequence.getSelectedToggle();
        List<xal.smf.impl.BPM> BPM = new ArrayList<>();
        if (accl.getSequences().toString().contains(getSeqName.getText())){
            BPM = accl.getSequence(getSeqName.getText()).getAllNodesOfType("BPM");
        } else if (accl.getComboSequences().toString().contains(getSeqName.getText())) {
            BPM = accl.getComboSequence(getSeqName.getText()).getAllNodesOfType("BPM");
        } else {
            BPM = accl.getAllNodesOfType("BPM");
        }
            
        double dist = Math.abs(pos-(BPM.get(0).getPosition()+BPM.get(0).getParent().getPosition()));
        xal.smf.impl.BPM closestBPM=BPM.get(0);
        int index = 0;
                
        for(xal.smf.impl.BPM bpm: BPM){
            if(dist > Math.abs(pos-(bpm.getPosition()+bpm.getParent().getPosition()))){
                dist = Math.abs(pos-(bpm.getPosition()+bpm.getParent().getPosition()));
                closestBPM = bpm;
                index++;
            } 
        }
        
        gridpaneCursor.setVisible(true);
        labelBPM.setText(closestBPM.toString());
        try {
            labelCoordinates.setText(String.format("x = %.2f mm; y = %.2f mm; c = %.1f",closestBPM.getXAvg(),closestBPM.getYAvg(),closestBPM.getAmpAvg()));
        } catch (ConnectionException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GetException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        HorizontalMarker.getData().clear();
        VerticalMarker.getData().clear();
        ChargeMarker.getData().clear();
        
        HorizontalMarker.getData().add(new XYChart.Data((closestBPM.getPosition()+closestBPM.getParent().getPosition()),0.9*axisHorizontal.getUpperBound()));
        HorizontalMarker.getData().add(new XYChart.Data((closestBPM.getPosition()+closestBPM.getParent().getPosition()),0.9*axisHorizontal.getLowerBound()));
        
        VerticalMarker.getData().add(new XYChart.Data((closestBPM.getPosition()+closestBPM.getParent().getPosition()),0.9*axisVertical.getUpperBound()));
        VerticalMarker.getData().add(new XYChart.Data((closestBPM.getPosition()+closestBPM.getParent().getPosition()),0.9*axisVertical.getLowerBound()));
        
        ChargeMarker.getData().add(new XYChart.Data((closestBPM.getPosition()+closestBPM.getParent().getPosition()),0.9*axisCharge.getUpperBound()));
        ChargeMarker.getData().add(new XYChart.Data((closestBPM.getPosition()+closestBPM.getParent().getPosition()),0.9*axisCharge.getLowerBound()));
                
    }
    
    public void updateDataset(TrajectoryArray Traj){

        //clear the initial values
        Horizontal.getData().clear();
        Vertical.getData().clear();
        Charge.getData().clear();

        for(xal.smf.impl.BPM item : Traj.Pos.keySet()){
            Horizontal.getData().add(new XYChart.Data(Traj.Pos.get(item),Traj.XDiff.get(item)));
            Vertical.getData().add(new XYChart.Data(Traj.Pos.get(item),Traj.YDiff.get(item)));
            try {
                Charge.getData().add(new XYChart.Data(Traj.Pos.get(item),item.getAmpAvg()));
            } catch (ConnectionException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GetException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //Create label values
        String nameHor ="x_rms = "+String.format("%.3f",Traj.getXrms())+" mm";
        String nameVer ="y_rms = "+String.format("%.3f",Traj.getYrms())+" mm";
                
        labelXrms.setText(nameHor);
        labelYrms.setText(nameVer);
        
    }
    
}
