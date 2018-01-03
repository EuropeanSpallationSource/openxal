/*
 * Copyright (C) 2017 European Spallation Source ERIC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package xal.app.trajectorydisplay2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
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
    private final ObservableList<File> refTrajData = FXCollections.observableArrayList();// holds info about reference trajectories

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

    private final ToggleGroup groupSequence = new ToggleGroup();//Toggle for group

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
    @FXML
    private AnchorPane mainAnchor1;
    @FXML
    private ComboBox<File> comboBoxRefTrajectory;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Populate the Accelerator Menu with the sequences of the machine
        List<xal.smf.AcceleratorSeq> seqItem = accl.getSequences();
        int k = 0;
        
        //remove LEBT and RFQ
        seqItem.remove(accl.getSequence("LEBT"));
        seqItem.remove(accl.getSequence("RFQ"));
        
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
                                groupSequence.selectToggle(addedItem);
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

       //Load reference and zero trajectories
        comboBoxRefTrajectory.setCellFactory((ListView<File> fileName) -> {
            ListCell cell = new ListCell<File>() {
                @Override
                protected void updateItem(File item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(item.getName());
                    }
                }
            };
            return cell;
        });    
        
        //Load reference and zero trajectories
         refTrajData.add(new File("/Users/nataliamilas/NetBeansProjects/TrajectoryCorrection/ZeroTrajectory.xml"));
        refTrajData.add(new File("/Users/nataliamilas/NetBeansProjects/TrajectoryCorrection/GoldenTrajectory.xml"));
        //populate the ComboBox element
        comboBoxRefTrajectory.setItems(refTrajData);
        comboBoxRefTrajectory.setValue(refTrajData.get(0));
        
        //read new reference trajectory file
        try {
            DisplayTraj.readReferenceTrajectoryFromFile(accl, comboBoxRefTrajectory.getSelectionModel().getSelectedItem());
        } catch (IOException ex) {
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

                double dist = Math.abs(plotXvalue-(BPM.get(0).getSDisplay()));
                xal.smf.impl.BPM closestBPM=BPM.get(0);
                int index = 0;

                for(xal.smf.impl.BPM bpm: BPM){
                    if(dist > Math.abs(plotXvalue-(bpm.getSDisplay()))){
                        dist = Math.abs(plotXvalue-(bpm.getSDisplay()));
                        closestBPM = bpm;
                        index++;
                    }
                }

                gridpaneCursor.setVisible(true);
                labelBPM.setText(closestBPM.toString());
                try {
                    labelCoordinates.setText(String.format("x = %.2f mm; y = %.2f mm; c = %.1f",DisplayTraj.XDiff.get(closestBPM),DisplayTraj.YDiff.get(closestBPM),closestBPM.getAmpAvg()));
                    chartVertical.setRangeCrosshairValue(DisplayTraj.YDiff.get(closestBPM));
                    chartCharge.setRangeCrosshairValue(closestBPM.getAmpAvg());
                    chartHorizontal.setRangeCrosshairValue(DisplayTraj.XDiff.get(closestBPM));
                    chartHorizontal.setDomainCrosshairValue(closestBPM.getSDisplay());
                    chartVertical.setDomainCrosshairValue(closestBPM.getSDisplay());
                    chartCharge.setDomainCrosshairValue(closestBPM.getSDisplay());

                } catch (ConnectionException | GetException ex) {
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
        final MenuItem makeElogEntry = new MenuItem("Make a Logbook entry");
        viewerCombinedPlot.getContextMenu().getItems().add(new SeparatorMenuItem());
        viewerCombinedPlot.getContextMenu().getItems().add(plotProperties);
        viewerCombinedPlot.getContextMenu().getItems().add(makeElogEntry);

        makeElogEntry.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Stage stage;
                Parent root;
                URL url1 = null;
                String sceneFile = "/fxml/MakeElogPost.fxml";
                try {
                    stage = new Stage();
                    url1 = FXMLController.this.getClass().getResource(sceneFile);
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(MainApp.class.getResource(sceneFile));
                    root = loader.load();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Send entry to eLog");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initOwner(labelXrms.getScene().getWindow());
                    MakeElogPostController loginController = loader.getController();
                    File imageFile = new File("plot.png");
                    ChartUtils.saveChartAsPNG(imageFile, chartCombinedPlot, (int) canvasCombinedPlot.getWidth(), (int) canvasCombinedPlot.getHeight());
                    loginController.setImagePath(imageFile);
                    loginController.loggedInProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasLoggedIn, Boolean isNowLoggedIn) -> {
                        if (isNowLoggedIn) {
                            stage.close();
                        }
                    }); stage.showAndWait();
                }catch (IOException ex) {
                    System.out.println( "Exception on FXMLLoader.load()" );
                    System.out.println("  * url: " + url1);
                    System.out.println( "  * " + ex );   
                    System.out.println( "    ----------------------------------------\n" );
                }
            }
        });
        
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
            @Override
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
                    
                    //read new reference trajectory file
                    try {
                        DisplayTraj.readReferenceTrajectoryFromFile(accl, comboBoxRefTrajectory.getSelectionModel().getSelectedItem());
                    } catch (IOException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }    

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

        Traj.Pos.keySet().stream().forEach((item) -> {
            horizontalSeries.add(Traj.Pos.get(item),Traj.XDiff.get(item));
            verticalSeries.add(Traj.Pos.get(item),Traj.YDiff.get(item));        
            //horizontalSeries.add(Traj.Pos.get(item),Traj.X.get(item));
            //verticalSeries.add(Traj.Pos.get(item),Traj.Y.get(item));   
            try {           
                chargeSeries.add(Traj.Pos.get(item),(Double) item.getAmpAvg());
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

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
                renderer.setSeriesPaint(0, new Color(255,0,0));
                renderer.setSeriesPaint(1, Color.BLACK);
                renderer.setSeriesOutlineStroke(0, new BasicStroke(2));
                renderer.setSeriesOutlineStroke(1, new BasicStroke(2));
                break;
            case 1:
                renderer.setSeriesPaint(0, new Color(0,109,219));
                renderer.setSeriesPaint(1, Color.BLACK);
                renderer.setSeriesOutlineStroke(0, new BasicStroke(2));
                renderer.setSeriesOutlineStroke(1, new BasicStroke(2));
                break;
            case 2:
                renderer.setSeriesPaint(0, Color.BLACK);
                renderer.setSeriesPaint(1, Color.BLACK);
                renderer.setSeriesOutlineStroke(0, new BasicStroke(2));
                renderer.setSeriesOutlineStroke(1, new BasicStroke(2));
                break;
            case 3:
                renderer.setSeriesPaint(1, new Color(238, 130, 238));
                renderer.setSeriesPaint(0, Color.BLACK);
                renderer.setSeriesOutlineStroke(0, new BasicStroke(2));
                renderer.setSeriesOutlineStroke(1, new BasicStroke(2));
        }
        plot.setRenderer( renderer );

        return plot;

    }
    
    @FXML
    private void handleLoadDefaultAccelerator(ActionEvent event) {
        timerPlotUpdate.stop();
        accl = xal.smf.data.XMLDataManager.loadDefaultAccelerator();
        
        int menu_num = menuSequence.getItems().size();
        
        menuSequence.getItems().remove(0, menu_num-1);
        System.out.print(menuSequence.getItems().toString());
        groupSequence.getToggles().clear();
        
        //Populate the Accelerator Menu with the sequences of the machine
        List<xal.smf.AcceleratorSeq> seqItem = accl.getSequences();
        int k = 0;
        for(xal.smf.AcceleratorSeq item: seqItem){ //AddSequences
            menuSequence.getItems().add(k,new RadioMenuItem(item.toString()));
            RadioMenuItem addedItem = (RadioMenuItem) menuSequence.getItems().get(k);
            addedItem.setToggleGroup(groupSequence);
            if(k==0){
                groupSequence.selectToggle(addedItem);
            }
            k++;
        }
        
        menuSequence.getItems().add(k,new SeparatorMenuItem());
        k++;
        List<xal.smf.AcceleratorSeqCombo> seqCombo = accl.getComboSequences();
        
        for(xal.smf.AcceleratorSeqCombo item: seqCombo){ //AddCombos
            menuSequence.getItems().add(k,new RadioMenuItem(item.toString()));
            RadioMenuItem addedItem = (RadioMenuItem) menuSequence.getItems().get(k);
            addedItem.setToggleGroup(groupSequence);
            if(k==0){
                groupSequence.selectToggle(addedItem);
            }
            k++;
        }

        menuSequence.getItems().add(k,new SeparatorMenuItem());  
        timerPlotUpdate.start();
           
    }

    @FXML
    private void handleLoadAccelerator(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Accelerator");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XAL files (*.xal)", "*.xal");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            timerPlotUpdate.stop();
            accl = xal.smf.data.XMLDataManager.acceleratorWithPath(selectedFile.getAbsolutePath());
            
            int menu_num = menuSequence.getItems().size();
        
            menuSequence.getItems().remove(0, menu_num-1);
            System.out.print(menuSequence.getItems().toString());
            groupSequence.getToggles().clear();

            //Populate the Accelerator Menu with the sequences of the machine
            List<xal.smf.AcceleratorSeq> seqItem = accl.getSequences();
            int k = 0;
            for(xal.smf.AcceleratorSeq item: seqItem){ //AddSequences
                menuSequence.getItems().add(k,new RadioMenuItem(item.toString()));
                RadioMenuItem addedItem = (RadioMenuItem) menuSequence.getItems().get(k);
                addedItem.setToggleGroup(groupSequence);
                if(k==0){
                    groupSequence.selectToggle(addedItem);
                }
                k++;
            }

            menuSequence.getItems().add(k,new SeparatorMenuItem());
            k++;
            List<xal.smf.AcceleratorSeqCombo> seqCombo = accl.getComboSequences();

            for(xal.smf.AcceleratorSeqCombo item: seqCombo){ //AddCombos
                menuSequence.getItems().add(k,new RadioMenuItem(item.toString()));
                RadioMenuItem addedItem = (RadioMenuItem) menuSequence.getItems().get(k);
                addedItem.setToggleGroup(groupSequence);
                if(k==0){
                    groupSequence.selectToggle(addedItem);
                }
                k++;
            }

            menuSequence.getItems().add(k,new SeparatorMenuItem());  
            timerPlotUpdate.start();
        }
    }

     //handles table context menu for saving a new reference
    @FXML
    public void handleTrajectoryMenuSave(ActionEvent event) throws GetException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Trajectory File");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
       
        //Show save file dialog
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            if (!refTrajData.contains(selectedFile)){                
                refTrajData.add(selectedFile);
            }            
            try {
                //Save Trajectory of the whole machine
                DisplayTraj.saveTrajectory(accl,selectedFile);
                comboBoxRefTrajectory.setValue(selectedFile);
            } catch (ConnectionException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //handles table context menu for loading a new reference orbit
    @FXML
    public void handleTrajectoryMenuLoad(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Trajectory File");

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            refTrajData.add(selectedFile);
            comboBoxRefTrajectory.setValue(selectedFile);
        }
    }

    @FXML
    private void openHelpURL(ActionEvent event) {
        final Hyperlink hyperlink = new Hyperlink("https://confluence.esss.lu.se/display/BPCRS/User+documentation+for+Trajectory+Correction+application");
        HostServices hostServices = (HostServices)this.mainAnchor1.getScene().getWindow().getProperties().get("hostServices");
        hostServices.showDocument(hyperlink.getText());
    }

    @FXML
    private void handleChooseRefTrajectory(ActionEvent event) {
        //read new reference trajectory file
        try {
            DisplayTraj.readReferenceTrajectoryFromFile(accl,comboBoxRefTrajectory.getSelectionModel().getSelectedItem());
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    

}
