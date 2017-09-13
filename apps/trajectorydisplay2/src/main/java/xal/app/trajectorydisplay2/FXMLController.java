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

package xal.app.trajectorydisplay2;

/**
 * Trajectory display. 
 * @author nataliamilas
 * 06-2017
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.AcceleratorSeqCombo;


public class FXMLController implements Initializable {
    
    //Creates the Accelerator
    //public xal.smf.Accelerator accl = xal.smf.data.XMLDataManager.acceleratorWithPath("/Users/nataliamilas/projects/openxal/site/optics/design/main.xal");
    public xal.smf.Accelerator accl = xal.smf.data.XMLDataManager.loadDefaultAccelerator();
    public TrajectoryArray DisplayTraj = new TrajectoryArray();//Trajectory to be displayed on the plot
   
    //set plot update timer
    private AnimationTimer timerPlotUpdate;
    
    //Setup the Plot
    final XYSeriesCollection Horizontal = new XYSeriesCollection();          
    final XYSeriesCollection Vertical = new XYSeriesCollection( );  
    final XYSeriesCollection Charge = new XYSeriesCollection( );
    XYSeries horizontalSeries = new XYSeries( "horizontal" );    
    XYSeries verticalSeries = new XYSeries( "vertical" );    
    XYSeries chargeSeries = new XYSeries( "charge" );    
    
    //Setup the Plot
    final XYChart.Series HorizontalMarker = new XYChart.Series();          
    final XYChart.Series VerticalMarker = new XYChart.Series();  
    final XYChart.Series ChargeMarker = new XYChart.Series(); 
    
    private ToggleGroup groupSequence = new ToggleGroup();//Toggle for group
    
    @FXML
    private Label labelXrms;
    @FXML
    private Label labelYrms;
    @FXML
    private GridPane gridpaneCursor;
    @FXML
    private Label labelCoordinates;
    @FXML
    private Label labelBPM;
    @FXML
    private AnchorPane anchorPaneChart;
    
    //Menu settings
    @FXML
    private MenuItem menuExit;
    @FXML
    private Menu menuSequence;
    
    
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
        gridpaneCursor.setVisible(false);
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
        
        menuSequence.getItems().add(new SeparatorMenuItem());
        
        MenuItem addCombo = new MenuItem("Add new Combo Sequence");
        menuSequence.getItems().add(addCombo);
        
        addCombo.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
            
                Stage stage; 
                Parent root;
                URL    url  = null;
                String sceneFile = "/fxml/CreateComboSequence.fxml";
                try
                {
                    stage = new Stage();
                    url  = getClass().getResource(sceneFile);
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(MainApp.class.getResource(sceneFile));
                    root = loader.load();
                    //root.getStylesheets().add("/styles/Styles.css");
                    stage.setScene(new Scene(root));
                    stage.setTitle("Create a Combo Sequence");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initOwner(labelXrms.getScene().getWindow());
                    CreateComboSequenceController loginController = loader.getController();
                    loginController.setProperties(accl);
                    loginController.loggedInProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasLoggedIn, Boolean isNowLoggedIn) -> {
                        if (isNowLoggedIn) {
                            if(loginController.getComboName()!=null){
                                AcceleratorSeqCombo comboSequence = new AcceleratorSeqCombo(loginController.getComboName(),loginController.getNewComboSequence());
                                accl.addComboSequence(comboSequence);
                                int index = menuSequence.getItems().size()-2;
                                RadioMenuItem addedItem = new RadioMenuItem(loginController.getComboName());
                                addedItem.setToggleGroup(groupSequence);
                                menuSequence.getItems().add(index, addedItem);
                            }
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
        
        Horizontal.addSeries(horizontalSeries);
        Vertical.addSeries(verticalSeries);
        Charge.addSeries(chargeSeries);
             
        //Create the charts
        XYPlot chartHorizontal = createPlot(Horizontal, "X Position (mm)", 0); 
        XYPlot chartVertical = createPlot(Vertical, "Y Position (mm)", 1); 
        XYPlot chartCharge = createPlot(Charge,"Charge (a.u.)", 2); 
        
        final CombinedDomainXYPlot combinedXYplot = new CombinedDomainXYPlot(new NumberAxis("Position (m)"));
        combinedXYplot.setGap(10.0);
        // add the subplots...
        combinedXYplot.add(chartHorizontal, 1);
        combinedXYplot.add(chartVertical, 1);
        combinedXYplot.add(chartCharge, 1);
        combinedXYplot.setOrientation(PlotOrientation.VERTICAL);
        combinedXYplot.getDomainAxis().setLabelFont(new Font("System", Font.PLAIN, 14));
        combinedXYplot.getDomainAxis().setTickLabelFont(new Font("System", Font.PLAIN, 12));
        combinedXYplot.setDomainGridlinesVisible(true);
        combinedXYplot.setDomainPannable(false);
        combinedXYplot.setDomainZeroBaselineVisible(false);
        NumberAxis xAxis = (NumberAxis) combinedXYplot.getDomainAxis();
        xAxis.setAutoRangeIncludesZero(false);
                  
        JFreeChart chartCombinedPlot = new JFreeChart(null, new Font("Arial", Font.BOLD, 18), combinedXYplot, true);
        chartCombinedPlot.getLegend().setVisible(false);
        chartCombinedPlot.setBackgroundPaint(Color.decode("#f4f4f4"));
        ChartViewer viewerCombinedPlot = new ChartViewer(chartCombinedPlot,true);        
        ChartCanvas canvasCombinedPlot = viewerCombinedPlot.getCanvas(); 
        
        canvasCombinedPlot.addChartMouseListener(new ChartMouseListenerFX(){
        //viewerCombinedPlot.addChartMouseListener(new ChartMouseListenerFX(){    
            @Override
            public void chartMouseClicked(ChartMouseEventFX cmefx) {
                Rectangle2D dataArea = canvasCombinedPlot.getRenderingInfo().getPlotInfo().getDataArea();
                //Rectangle2D dataArea = viewerCombinedPlot.getRenderingInfo().getPlotInfo().getDataArea();
                CombinedDomainXYPlot plot = (CombinedDomainXYPlot) cmefx.getChart().getPlot();
                double plotXvalue = plot.getDomainAxis().java2DToValue(cmefx.getTrigger().getX(),dataArea,plot.getDomainAxisEdge());
                                
                //crosshair markers visible
                chartHorizontal.setDomainCrosshairVisible(true);
                chartHorizontal.setRangeCrosshairVisible(true);
                chartVertical.setDomainCrosshairVisible(true);
                chartVertical.setRangeCrosshairVisible(true);
                chartCharge.setDomainCrosshairVisible(true);
                chartCharge.setRangeCrosshairVisible(true);
               
                gridpaneCursor.setVisible(true);
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

                double dist = Math.abs(plotXvalue-(BPM.get(0).getPosition()+BPM.get(0).getParent().getPosition()));
                xal.smf.impl.BPM closestBPM=BPM.get(0);
                int index = 0;

                for(xal.smf.impl.BPM bpm: BPM){
                    if(dist > Math.abs(plotXvalue-(bpm.getPosition()+bpm.getParent().getPosition()))){
                        dist = Math.abs(plotXvalue-(bpm.getPosition()+bpm.getParent().getPosition()));
                        closestBPM = bpm;
                        index++;
                    } 
                }
     
                gridpaneCursor.setVisible(true);
                labelBPM.setText(closestBPM.toString());
                try {
                    labelCoordinates.setText(String.format("x = %.2f mm; y = %.2f mm; c = %.1f",closestBPM.getXAvg(),closestBPM.getYAvg(),closestBPM.getAmpAvg()));
                    chartVertical.setRangeCrosshairValue(closestBPM.getYAvg());
                    chartCharge.setRangeCrosshairValue(closestBPM.getAmpAvg());
                    chartHorizontal.setRangeCrosshairValue(closestBPM.getXAvg()); 
                    chartHorizontal.setDomainCrosshairValue(closestBPM.getPosition()+closestBPM.getParent().getPosition());
                    chartVertical.setDomainCrosshairValue(closestBPM.getPosition()+closestBPM.getParent().getPosition());
                    chartCharge.setDomainCrosshairValue(closestBPM.getPosition()+closestBPM.getParent().getPosition());

                } catch (ConnectionException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }

            @Override
            public void chartMouseMoved(ChartMouseEventFX cmefx) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        });
        
        //Add to the Scene and bind canvas size to anchor pane size. 
        anchorPaneChart.getChildren().add(viewerCombinedPlot);
        viewerCombinedPlot.setPrefSize(1130,680);
        
        
        final MenuItem plotProperties = new MenuItem("Plot Properties");
        viewerCombinedPlot.getContextMenu().getItems().add(new SeparatorMenuItem());
        viewerCombinedPlot.getContextMenu().getItems().add(plotProperties);
        
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
                    loginController.setPlotProperties(combinedXYplot);
                    loginController.loggedInProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasLoggedIn, Boolean isNowLoggedIn) -> {
                        if (isNowLoggedIn) {
                            XYPlot plot = (XYPlot) combinedXYplot.getSubplots().get(0);
                            plot.getRangeAxis().setUpperBound(loginController.getxMax());
                            plot.getRangeAxis().setLowerBound(loginController.getxMin());
                            plot.getRangeAxis().setAutoRange(loginController.getxAutoscale());
                            plot = (XYPlot) combinedXYplot.getSubplots().get(1);
                            plot.getRangeAxis().setUpperBound(loginController.getyMax());
                            plot.getRangeAxis().setLowerBound(loginController.getyMin());
                            plot.getRangeAxis().setAutoRange(loginController.getyAutoscale());
                            plot = (XYPlot) combinedXYplot.getSubplots().get(2);
                            plot.getRangeAxis().setUpperBound(loginController.getcMax());
                            plot.getRangeAxis().setLowerBound(loginController.getcMin());
                            plot.getRangeAxis().setAutoRange(loginController.getcAutoscale());
                            combinedXYplot.getDomainAxis().setUpperBound(loginController.getpMax());
                            combinedXYplot.getDomainAxis().setLowerBound(loginController.getpMin());
                            combinedXYplot.getDomainAxis().setAutoRange(loginController.getpAutoscale());
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
        
        groupSequence.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,Toggle old_toggle, Toggle new_toggle) {
                if(old_toggle != new_toggle){
                    //turn off plot update timer
                    timerPlotUpdate.stop();        

                    gridpaneCursor.setVisible(false);

                    HorizontalMarker.getData().clear();
                    VerticalMarker.getData().clear();
                    ChargeMarker.getData().clear();
                    
                    //clear the initial values
                    horizontalSeries.clear();
                    verticalSeries.clear();
                    chargeSeries.clear();
                    
                    //remove crosshair markers
                    chartHorizontal.setDomainCrosshairVisible(false);
                    chartHorizontal.setRangeCrosshairVisible(false);
                    chartVertical.setDomainCrosshairVisible(false);
                    chartVertical.setRangeCrosshairVisible(false);
                    chartCharge.setDomainCrosshairVisible(false);
                    chartCharge.setRangeCrosshairVisible(false);
                    combinedXYplot.getDomainAxis().setAutoRange(true);

                    //turn on timer
                    timerPlotUpdate.start();
                }
            }
        });
        
        
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
                
            }
                
        };
        
        timerPlotUpdate.start();  
            
    }   
    
    @FXML
    private void handleExitMenu(ActionEvent event) {
        System.exit(0);
    }
          
    public void updateDataset(TrajectoryArray Traj){

        horizontalSeries.clear();
        verticalSeries.clear();
        chargeSeries.clear();
        
        for(xal.smf.impl.BPM item : Traj.Pos.keySet()){
            horizontalSeries.add(Traj.Pos.get(item),Traj.XDiff.get(item));
            verticalSeries.add(Traj.Pos.get(item),Traj.YDiff.get(item));
            try {
                chargeSeries.add(Traj.Pos.get(item),(Double) item.getAmpAvg());
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
 
    private static XYPlot createPlot(XYDataset dataset, String yLabel, int chartSeries) {

        XYPlot plot = new XYPlot();
        
        String fontName = "System";
        NumberAxis rangeAxisy = new NumberAxis(yLabel);
        plot.setRangeAxis(rangeAxisy);
        plot.setDataset(dataset);
        plot.getRangeAxis().setLabelFont(new Font(fontName, Font.PLAIN, 14));
        plot.getRangeAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainCrosshairVisible(false);
        plot.setDomainCrosshairPaint(Color.BLACK);
        plot.setDomainCrosshairStroke(new BasicStroke(1.0f));
        plot.setDomainCrosshairLockedOnData(true);
        plot.setRangeCrosshairVisible(false);
        plot.setRangeCrosshairPaint(Color.BLACK);
        plot.setRangeCrosshairStroke(new BasicStroke(1.0f));
        BasicStroke gridstroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1,new float[] { 1, 2 }, 10);
        plot.setDomainGridlineStroke(gridstroke);
        plot.setRangeGridlineStroke(gridstroke);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
        switch (chartSeries){
            case 0:
                renderer.setSeriesPaint(0, Color.RED);
                renderer.setSeriesPaint(1, Color.BLACK);
                break;
            case 1:
                renderer.setSeriesPaint(0, Color.BLUE);
                renderer.setSeriesPaint(1, Color.BLACK);
                break;
            case 2:
                renderer.setSeriesPaint(0, Color.GREEN);
                renderer.setSeriesPaint(1, Color.BLACK);
                break;
            case 3:
                renderer.setSeriesPaint(0, Color.BLACK);
                renderer.setSeriesPaint(0, Color.BLACK);
        }
        plot.setRenderer( renderer );

        return plot;

    }
    
}


