package xal.app.trajectorycorrection2;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import xal.smf.impl.BPM;

/**
 * FXML Controller class
 *
 * @author nataliamilas
 */
public class VerifyResponseController{

    private final BooleanProperty loggedIn = new SimpleBooleanProperty();    
    private final List<BPM> BPMList = new ArrayList<>();
    private CorrectionMatrix Correction1to1;
    private CorrectionSVD CorrectionSVD;
    private String dataType = null;
    
    //Setup the Plot
    final XYSeriesCollection Data = new XYSeriesCollection();    
    XYSeries response = new XYSeries( "data_response" );    
        
    @FXML
    private Button buttonClose;
    @FXML
    private ComboBox<String> comboBoxCorrector;
    @FXML
    private ComboBox<String> comboBoxPlane;
    @FXML
    private AnchorPane panePlot;

    public CorrectionMatrix getCorrection1to1() {
        return Correction1to1;
    }

    public void setCorrection1to1(CorrectionMatrix Correction1to1) {
        setDataType("1to1");
        this.Correction1to1 = Correction1to1;
        //set planes
        comboBoxPlane.getItems().add("Horizontal");
        comboBoxPlane.getItems().add("Vertical");
        comboBoxPlane.setValue("Horizontal");
        
        //set BPM
        if (comboBoxPlane.getValue().equals("Horizontal")){
            Correction1to1.HC.keySet().forEach(bpm -> BPMList.add(bpm));
            BPMList.sort((bpm1,bpm2)-> Double.compare(bpm1.getSDisplay(), bpm2.getSDisplay()));
            BPMList.forEach(bpm -> comboBoxCorrector.getItems().add(bpm.toString()));
        } else if (comboBoxPlane.getValue().equals("Vertical")){
            Correction1to1.VC.keySet().forEach(bpm -> BPMList.add(bpm));
            BPMList.sort((bpm1,bpm2)-> Double.compare(bpm1.getSDisplay(), bpm2.getSDisplay()));
            BPMList.forEach(bpm -> comboBoxCorrector.getItems().add(bpm.toString()));
        }
        comboBoxCorrector.setValue(comboBoxCorrector.getItems().get(0));
                      
        updatePlot1to1(); 
        Data.addSeries(response);
        
        JFreeChart xyPlot = ChartFactory.createXYLineChart(null,"Steerer strength (T.m)", "BPM reading (mm)", Data);
        xyPlot.getLegend().setVisible(false);
        xyPlot.setBackgroundPaint(java.awt.Color.decode("#f4f4f4"));
        xyPlot.getXYPlot().getDomainAxis().setLabelFont(new Font("System", Font.PLAIN, 14));
        xyPlot.getXYPlot().getDomainAxis().setTickLabelFont(new Font("System", Font.PLAIN, 12));
        xyPlot.getXYPlot().getRangeAxis().setLabelFont(new Font("System", Font.PLAIN, 14));
        xyPlot.getXYPlot().getRangeAxis().setTickLabelFont(new Font("System", Font.PLAIN, 12));           
                        
        ChartViewer viewerPlot = new ChartViewer(xyPlot,true);
        panePlot.getChildren().add(viewerPlot);
        viewerPlot.setPrefSize(590, 345);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
        renderer.setSeriesPaint( 0 , Color.GREEN );
        final XYPlot plot = xyPlot.getXYPlot( );
        plot.setRenderer(renderer);
        plot.getRangeAxis().setLabelFont(new Font("System", Font.PLAIN, 14));
        plot.getRangeAxis().setTickLabelFont(new Font("System", Font.PLAIN, 12));
        plot.setBackgroundPaint(Color.WHITE);
        BasicStroke gridstroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1,new float[] { 1, 2 }, 10);
        plot.setDomainGridlineStroke(gridstroke);
        plot.setRangeGridlineStroke(gridstroke);                      
        
        final MenuItem plotProperties = new MenuItem("Plot Properties");
        viewerPlot.getContextMenu().getItems().add(new SeparatorMenuItem());
        viewerPlot.getContextMenu().getItems().add(plotProperties);
        
        plotProperties.setOnAction((ActionEvent event1)  -> {
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
                loginController.setPlotProperties(xyPlot.getXYPlot().getDomainAxis().getUpperBound(),xyPlot.getXYPlot().getDomainAxis().getLowerBound(),xyPlot.getXYPlot().getDomainAxis().isAutoRange(),xyPlot.getXYPlot().getRangeAxis().getUpperBound(),xyPlot.getXYPlot().getRangeAxis().getLowerBound(),xyPlot.getXYPlot().getRangeAxis().isAutoRange());
                loginController.loggedInProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasLoggedIn, Boolean isNowLoggedIn) -> {
                    if (isNowLoggedIn) {                                           
                        xyPlot.getXYPlot().getDomainAxis().setUpperBound(loginController.getxMax());
                        xyPlot.getXYPlot().getDomainAxis().setLowerBound(loginController.getxMin());
                        xyPlot.getXYPlot().getDomainAxis().setAutoRange(loginController.getxAutoscale());                        
                        xyPlot.getXYPlot().getRangeAxis().setUpperBound(loginController.getyMax());
                        xyPlot.getXYPlot().getRangeAxis().setLowerBound(loginController.getyMin()); 
                        xyPlot.getXYPlot().getRangeAxis().setAutoRange(loginController.getyAutoscale());
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
        });
    }

    public CorrectionSVD getCorrectionSVD() {
        return CorrectionSVD;
    }

    public void setCorrectionSVD(CorrectionSVD CorrectionSVD) {
        setDataType("SVD");
        this.CorrectionSVD = CorrectionSVD;
        
        //set planes
        comboBoxPlane.getItems().add("Horizontal");
        comboBoxPlane.getItems().add("Vertical");
        comboBoxPlane.setValue("Horizontal");
        
        //set correctors
        if (comboBoxPlane.getValue().equals("Horizontal")){
            CorrectionSVD.HC.forEach(hc -> comboBoxCorrector.getItems().add(hc.toString()));            
        } else if (comboBoxPlane.getValue().equals("Vertical")){
            CorrectionSVD.VC.forEach(vc -> comboBoxCorrector.getItems().add(vc.toString()));            
        }
        comboBoxCorrector.setValue(comboBoxCorrector.getItems().get(0));
                
        updatePlotSVD();
        Data.addSeries(response);
        
        JFreeChart xyPlot = ChartFactory.createXYLineChart(null,"Position (m)", "Response coefficient", Data);
        xyPlot.getLegend().setVisible(false);
        xyPlot.setBackgroundPaint(java.awt.Color.decode("#f4f4f4"));
        xyPlot.getXYPlot().getDomainAxis().setLabelFont(new Font("System", Font.PLAIN, 14));
        xyPlot.getXYPlot().getDomainAxis().setTickLabelFont(new Font("System", Font.PLAIN, 12));
        xyPlot.getXYPlot().getRangeAxis().setLabelFont(new Font("System", Font.PLAIN, 14));
        xyPlot.getXYPlot().getRangeAxis().setTickLabelFont(new Font("System", Font.PLAIN, 12));           
                        
        ChartViewer viewerPlot = new ChartViewer(xyPlot,true);
        panePlot.getChildren().add(viewerPlot);
        viewerPlot.setPrefSize(590, 345);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
        renderer.setSeriesPaint( 0 , Color.GREEN );
        final XYPlot plot = xyPlot.getXYPlot( );
        plot.setRenderer(renderer);
        plot.getRangeAxis().setLabelFont(new Font("System", Font.PLAIN, 14));
        plot.getRangeAxis().setTickLabelFont(new Font("System", Font.PLAIN, 12));
        plot.setBackgroundPaint(Color.WHITE);
        BasicStroke gridstroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1,new float[] { 1, 2 }, 10);
        plot.setDomainGridlineStroke(gridstroke);
        plot.setRangeGridlineStroke(gridstroke);  
        
        //BPM annotation
        XYTextAnnotation annotation = new XYTextAnnotation("",0.0,0.0);
        plot.addAnnotation(annotation);
        
        ChartCanvas canvasPlot = viewerPlot.getCanvas();

        canvasPlot.addChartMouseListener(new ChartMouseListenerFX(){
        //viewerCombinedPlot.addChartMouseListener(new ChartMouseListenerFX(){
            @Override
            public void chartMouseClicked(ChartMouseEventFX cmefx) {
                Rectangle2D dataArea = canvasPlot.getRenderingInfo().getPlotInfo().getDataArea();                
                double plotXvalue = plot.getDomainAxis().java2DToValue(cmefx.getTrigger().getX(),dataArea,plot.getDomainAxisEdge());

                //crosshair markers visible
                plot.setDomainCrosshairVisible(true);
                plot.setRangeCrosshairVisible(false); 
                plot.setDomainCrosshairPaint(Color.BLACK);
                plot.setDomainCrosshairStroke(new BasicStroke(1.0f));
                
                //Find the closest data point
                List<BPM> BPMList = new ArrayList<>();
                CorrectionSVD.BPM.forEach(bpm -> BPMList.add(bpm));                

                double dist = Math.abs(plotXvalue-(BPMList.get(0).getSDisplay()));
                BPM closestBPM=BPMList.get(0);
                int index = 0;

                for(BPM bpm: BPMList){
                    if(dist > Math.abs(plotXvalue-(bpm.getSDisplay()))){
                        dist = Math.abs(plotXvalue-(bpm.getSDisplay()));
                        closestBPM = bpm;
                        index++;
                    }
                }
                plot.setDomainCrosshairValue(closestBPM.getSDisplay());
                double annotationX = 0.9*plot.getDomainAxis().getUpperBound();
                double annotationY = 0.9*plot.getRangeAxis().getUpperBound();                 
                annotation.setText(closestBPM.toString());
                annotation.setX(annotationX);
                annotation.setY(annotationY);
                annotation.setOutlineVisible(false);
                annotation.setFont(new Font("System", Font.BOLD, 12));
                annotation.setPaint(Color.black);
                annotation.setBackgroundPaint(Color.WHITE);
                
            }

            @Override
            public void chartMouseMoved(ChartMouseEventFX cmefx) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        });
        
        
        final MenuItem plotProperties = new MenuItem("Plot Properties");
        viewerPlot.getContextMenu().getItems().add(new SeparatorMenuItem());
        viewerPlot.getContextMenu().getItems().add(plotProperties);
        
        plotProperties.setOnAction((ActionEvent event1)  -> {
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
                loginController.setPlotProperties(xyPlot.getXYPlot().getDomainAxis().getUpperBound(),xyPlot.getXYPlot().getDomainAxis().getLowerBound(),xyPlot.getXYPlot().getDomainAxis().isAutoRange(),xyPlot.getXYPlot().getRangeAxis().getUpperBound(),xyPlot.getXYPlot().getRangeAxis().getLowerBound(),xyPlot.getXYPlot().getRangeAxis().isAutoRange());
                loginController.loggedInProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasLoggedIn, Boolean isNowLoggedIn) -> {
                    if (isNowLoggedIn) {                                                                                                            
                        xyPlot.getXYPlot().getDomainAxis().setUpperBound(loginController.getxMax());
                        xyPlot.getXYPlot().getDomainAxis().setLowerBound(loginController.getxMin());
                        xyPlot.getXYPlot().getDomainAxis().setAutoRange(loginController.getxAutoscale());                        
                        xyPlot.getXYPlot().getRangeAxis().setUpperBound(loginController.getyMax());
                        xyPlot.getXYPlot().getRangeAxis().setLowerBound(loginController.getyMin()); 
                        xyPlot.getXYPlot().getRangeAxis().setAutoRange(loginController.getyAutoscale());
                        if (xyPlot.getXYPlot().getDomainCrosshairValue()<xyPlot.getXYPlot().getDomainAxis().getUpperBound() && xyPlot.getXYPlot().getDomainCrosshairValue()>xyPlot.getXYPlot().getDomainAxis().getLowerBound()){
                            annotation.setX(0.9*xyPlot.getXYPlot().getDomainAxis().getUpperBound());
                            annotation.setY(0.9*xyPlot.getXYPlot().getRangeAxis().getUpperBound());
                        } else {
                            annotation.setText(" ");
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
        });
                
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
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
    
    public void updatePlot1to1(){
        int correctorIndex;
        String plane;
        xal.smf.impl.BPM bpmindex;
        
        //clear the initial values
        response.clear();
        plane = comboBoxPlane.getValue();
        correctorIndex = comboBoxCorrector.getItems().indexOf(comboBoxCorrector.getValue());
        if (correctorIndex<0) {
            correctorIndex = 0;
        }
        bpmindex = BPMList.get(correctorIndex);
        
        for(int i = -20; i<21; i++){ 
            if(plane.equals("Horizontal")){
                response.add(i,Correction1to1.getHorParam().get(bpmindex)[0]+Correction1to1.getHorParam().get(bpmindex)[1]*i);                    
            } else if (plane.equals("Vertical")){
                response.add(i,Correction1to1.getVertParam().get(bpmindex)[0]+Correction1to1.getVertParam().get(bpmindex)[1]*i);                   
            }
        }
                    
    }       
    
    public void updatePlotSVD(){        
        int bpmIndex;
        int correctorIndex;
        int i=0;
        String plane;

        
        //referencePlot.getData().clear();
        plane = comboBoxPlane.getValue();
        correctorIndex = comboBoxCorrector.getItems().indexOf(comboBoxCorrector.getValue());
        if (correctorIndex<0) {
            correctorIndex = 0;
        }
        response.clear();
        
        for(xal.smf.impl.BPM item : CorrectionSVD.BPM){
            bpmIndex = CorrectionSVD.BPM.indexOf(item);
            if(plane.equals("Horizontal")){
                response.add(item.getSDisplay(),CorrectionSVD.TRMhorizontal.get(bpmIndex, correctorIndex));   
            } else if (plane.equals("Vertical")){
                response.add(item.getSDisplay(),CorrectionSVD.TRMvertical.get(bpmIndex, correctorIndex));                
            } 
        }
        
    }      

    @FXML
    private void handleRePlot(ActionEvent event) {
        if (dataType.equals("SVD")){
            updatePlotSVD();
        } else if (dataType.equals("1to1")){
            updatePlot1to1();
        }
    }

    @FXML
    private void handleChangePlane(ActionEvent event) {
        comboBoxCorrector.getItems().clear();
        BPMList.clear();
        if (comboBoxPlane.getValue().equals("Horizontal")){
            if (dataType.equals("SVD")){
                CorrectionSVD.HC.forEach(hc -> comboBoxCorrector.getItems().add(hc.toString()));
            } else if (dataType.equals("1to1")){
                Correction1to1.HC.keySet().forEach(bpm -> BPMList.add(bpm));
                BPMList.sort((bpm1,bpm2)-> Double.compare(bpm1.getSDisplay(), bpm2.getSDisplay()));
                BPMList.forEach(bpm -> comboBoxCorrector.getItems().add(bpm.toString()));
            }
        } else if (comboBoxPlane.getValue().equals("Vertical")){
            if (dataType.equals("SVD")){
                CorrectionSVD.VC.forEach(vc -> comboBoxCorrector.getItems().add(vc.toString()));
            } else if (dataType.equals("1to1")){
                Correction1to1.VC.keySet().forEach(bpm -> BPMList.add(bpm));
                BPMList.sort((bpm1,bpm2)-> Double.compare(bpm1.getSDisplay(), bpm2.getSDisplay()));
                BPMList.forEach(bpm -> comboBoxCorrector.getItems().add(bpm.toString()));
            }
        }
        comboBoxCorrector.setValue(comboBoxCorrector.getItems().get(0));        
    }

    @FXML
    private void handleCloseButton(ActionEvent event) {
        setLoggedIn(true);
    }
    
    
}
