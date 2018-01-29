package org.xal.lebt;

import com.sun.javafx.charts.Legend;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.InputMethodEvent;
import javafx.stage.FileChooser;
import xal.ca.ConnectionException;
import xal.model.ModelException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.NoSuchChannelException;
import xal.tools.URLUtil.FilePathException;

/**
 * The class handling the LEBT trajectory prediction.
 * 
 * @author sofiaolsson
 */
public class FXMLController implements Initializable {
    
    //constants
    public static final double SOLENOID_CURRENT_TO_PEAK_RATE = 8.15308527131783e-01; //(A->mT)
    public static final double SOLENOID_PEAK_RATE_TO_CURRENT = 1/8.15308527131783e-01; //(mT->A)
    public static final double HSTEERER_CURRENT_TO_PEAK_RATE = 7.166666666666667e-02; //(A->mT)
    public static final double HSTEERER_PEAK_RATE_TO_CURRENT = 1/7.166666666666667e-02;//(mT->A)
    public static final double VSTEERER_CURRENT_TO_PEAK_RATE = 8.583333333333334e-02; //(A->mT)
    public static final double VSTEERER_PEAK_RATE_TO_CURRENT = 1/8.583333333333334e-02;//(mT->A)
    
    //defining series
    private XYChart.Series seriesX;
    private XYChart.Series seriesY;
    private XYChart.Series seriesR;
    private XYChart.Series seriesPhi;
    private XYChart.Series[] seriesSigmaX;
    private XYChart.Series[] seriesSigmaY;
    private XYChart.Series[] seriesSigmaR;
    private XYChart.Series[] seriesSigmaOffsetX;
    private XYChart.Series[] seriesSigmaOffsetY;
    private XYChart.Series[] seriesSigmaOffsetR;
    private XYChart.Series[] seriesSurroundings;
    private Object range;
    
    //defining simulation
    private SimulationRunner newRun;
    private double[][] surroundings;
    private Accelerator accelerator;
    
    //defining data
    private ArrayList<Double>[] sigmaX;
    private ArrayList<Double>[] sigmaY;
    private ArrayList<Double>[] sigmaR;
    private ArrayList<Double>[] sigmaOffsetX;
    private ArrayList<Double>[] sigmaOffsetY;
    private ArrayList<Double>[] sigmaOffsetR;
    private ArrayList positions;
    private ArrayList posX;
    private ArrayList posY;
    private ArrayList posR;
    private ArrayList posPhi;
    
    @FXML private LineChart<Double, Double> plot1;
    @FXML private NumberAxis yAxis;
    @FXML private NumberAxis xAxis;
    @FXML private LineChart<Double, Double> plot2;
    @FXML private NumberAxis yAxis1;
    @FXML private NumberAxis xAxis1;  
    @FXML private TextField textField_x;
    @FXML private TextField textField_xp;
    @FXML private TextField textField_y;
    @FXML private TextField textField_yp;
    @FXML private TextField textField_emittx;
    @FXML private TextField textField_betax;
    @FXML private TextField textField_alphax;
    @FXML private TextField textField_emitty;
    @FXML private TextField textField_betay;
    @FXML private TextField textField_alphay;    
    @FXML private TextField textField_scc;
    @FXML private TextField textField_bc;
    private TextField textField_sol1;
    private TextField textField_sol2;
    private TextField textField_V1;
    private TextField textField_H1;
    private TextField textField_V2;
    private TextField textField_H2;    
    @FXML private RadioButton radioButtonCart;
    @FXML private RadioButton radioButtonCyl;
    private ToggleGroup coordinateGroup;
    @FXML private RadioButton radioButtonOffsetOn;
    @FXML private RadioButton radioButtonOffsetOff;
    private ToggleGroup offsetGroup;
    @FXML private NumberAxis yAxis2;
    @FXML private TextField textFieldSigmaScale;
    private double scale;
    @FXML private TextField textField_coil1;
    @FXML private TextField textField_coil2;
    @FXML private TextField textField_coil3;
    @FXML private Label label_coil1RB;
    @FXML private Label label_coil2RB;
    @FXML private Label label_coil3RB;
    @FXML private TextField textField_magnetron;
    @FXML private Label label_magnetronRB;    
    @FXML private TextField textField_H2flow;
    @FXML private Label label_H2flowRB;
    @FXML private TextField textField_highVoltage;
    @FXML private Label label_HighVoltageRB;
    @FXML private Label label_ISgauge1;
    @FXML private Label label_ISgauge2;
    @FXML private Label label_ISgauge3;
    @FXML private TextField textField_sol1field;
    @FXML private Label label_sol1current;
    @FXML private Label label_sol1currentRB;
    @FXML private TextField textField_sol2field;
    @FXML private Label label_sol2current;
    @FXML private Label label_sol2currentRB;
    @FXML private TextField textField_CV1field;
    @FXML private Label label_CV1current;
    @FXML private Label label_CV1currentRB;
    @FXML private TextField textField_CH1field;
    @FXML private Label label_CH1current;
    @FXML private Label label_CH1currentRB;
    @FXML private TextField textField_CV2field;
    @FXML private Label label_CV2current;
    @FXML private Label label_CV2currentRB;
    @FXML private TextField textField_CH2field;
    @FXML private Label label_CH2current;
    @FXML private Label label_CH2currentRB;
    @FXML private TextField textField_irisAperture;
    @FXML private Label label_irisApertureRB;
    @FXML private TextField textField_chopperVoltage;
    @FXML private Label label__chopperVoltageRB;
    @FXML private CheckBox comboBox_currentFC;
    @FXML private CheckBox comboBox_posNPM;
    @FXML private CheckBox comboBox_sigmaNPM;
    @FXML private CheckBox comboBox_divergenceNPM;
    @FXML private ToggleGroup displayGroup;    

             
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ActionEvent event = null;
        
        this.handleLoadAccelerator(event);
        
        //initializing toggle groups
        coordinateGroup = new ToggleGroup();
        offsetGroup = new ToggleGroup();
        
        radioButtonCart.setToggleGroup(coordinateGroup);
        radioButtonCart.setSelected(true);
        radioButtonCyl.setToggleGroup(coordinateGroup);
        
        radioButtonOffsetOff.setToggleGroup(offsetGroup);
        radioButtonOffsetOn.setToggleGroup(offsetGroup);
        radioButtonOffsetOn.setSelected(true);
        
        //initializing simulation
        newRun = new SimulationRunner();
        surroundings = SimulationRunner.SURROUNDING;
        
        //assigning labels
        getParameters();
        
        //initializing series
        seriesX = new XYChart.Series();
        seriesY = new XYChart.Series();
        seriesR = new XYChart.Series();
        seriesPhi = new XYChart.Series();
        seriesSigmaX = new XYChart.Series[2];
        seriesSigmaY = new XYChart.Series[2];
        seriesSigmaR = new XYChart.Series[2];
        seriesSigmaOffsetX = new XYChart.Series[2];
        seriesSigmaOffsetY = new XYChart.Series[2];
        seriesSigmaOffsetR = new XYChart.Series[2];
        seriesSurroundings = new XYChart.Series[2];

        seriesX.setName("x");
        seriesY.setName("y");
        seriesR.setName("r");
        seriesPhi.setName("φ");
        
        
        for(int i = 0; i<seriesSurroundings.length;i++){
            seriesSigmaX[i] = new XYChart.Series();
            seriesSigmaY[i] = new XYChart.Series();
            seriesSigmaR[i] = new XYChart.Series();
            seriesSigmaOffsetX[i] = new XYChart.Series();
            seriesSigmaOffsetY[i] = new XYChart.Series();
            seriesSigmaOffsetR[i] = new XYChart.Series();
            seriesSurroundings[i] = new XYChart.Series();
        }
              
        //Add surroundings
        plot2.getData().add(seriesSurroundings[0]);
        plot2.getData().add(seriesSurroundings[1]);
        
        //remove surrounding legend
        Legend legend = (Legend)plot2.lookup(".chart-legend");
        legend.getItems().remove(0, 2);
        
             
        plot1.setAnimated(false);
        plot2.setAnimated(false);
        plot1.setCreateSymbols(false);
        plot2.setCreateSymbols(false);
        //plot1.getData().add(seriesSurroundings);
        
        
       //Showing surroundings
        for (int i = 0; i < surroundings[0].length ; i++) {
            seriesSurroundings[0].getData().add(new XYChart.Data(surroundings[0][i], surroundings[1][i]));
            seriesSurroundings[1].getData().add(new XYChart.Data(surroundings[0][i], -surroundings[1][i]));
        }
        seriesSurroundings[0].getNode().setStyle("-fx-stroke: #000000;");
        seriesSurroundings[1].getNode().setStyle("-fx-stroke: #000000;");
        
        scale = 1;
    }    
    
    @FXML
    private void handleLoadDefaultAccelerator(ActionEvent event) {
       
        accelerator = xal.smf.data.XMLDataManager.loadDefaultAccelerator();        
           
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
            try{
                accelerator = xal.smf.data.XMLDataManager.acceleratorWithPath(selectedFile.getAbsolutePath());           
            } catch (FilePathException e) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Load Accelerator");
                alert.setHeaderText("Exception for bad file path specification.");
                alert.setContentText("Choose your option.");

                ButtonType buttonDefault = new ButtonType("Load Default");            
                ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(buttonDefault, buttonTypeCancel);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonDefault){
                    accelerator = xal.smf.data.XMLDataManager.loadDefaultAccelerator();               
                }       
            }
        } else {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Load Accelerator");
            alert.setHeaderText("No file was selected.");
            alert.setContentText("Choose your option.");

            ButtonType buttonDefault = new ButtonType("Load Default");            
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonDefault, buttonTypeCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonDefault){
                accelerator = xal.smf.data.XMLDataManager.loadDefaultAccelerator();               
            }       
        }
    }

    
    //------------------------HANDLE METHODS------------------------------------
    
    /**
     * Runs the simulation. Sets simulation parameters from text fields. Displays trajectory plots
     * @param event 
     */
    private void runSimulation(ActionEvent event) {
        
        //Re-initializing simulation
        newRun = new SimulationRunner();
        setParameters();
        
        try {
            newRun.runSimulation();
            
        } catch (ModelException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Modelexception in simulation");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            
        } catch (InstantiationException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Instanitiation exception in simulation");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            
        } catch (Exception ex) {
            ex.getStackTrace();
            
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Unknown error in simulation");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        
        //Display if successful run
        if(newRun.hasRun()) {
            displayData(newRun);
        }
    }
    
    @FXML
    private void handleGetCurrentfromFC(ActionEvent event) {
        if(comboBox_currentFC.isSelected()){
            textField_bc.setDisable(true);
            AcceleratorNode faradayCup;
            faradayCup = accelerator.getSequence("LEBT").getAllNodesOfType("CurrentMonitor").get(0);
            //faradayCup.getAndConnectChannel("Avg_current").addMonitorValue(listener, 0);
        } else {
            textField_bc.setDisable(false);
        }
    }
    
    @FXML
    private void coordinateHandler(ActionEvent event) {
       
        if (newRun.hasRun()){
            addTrajectorySeriesToPlot();
            addEnvelopeSeriesToPlot();
            displayPlots();
        }
        else{
            setLabels();
            setBounds();
        }
    }

    @FXML
    private void offsetHandler(ActionEvent event) {
        
        if (newRun.hasRun()){
            addEnvelopeSeriesToPlot();
            displayEnvelope();}
        else{
            setLabels();
            setBounds();
        }
    }
    
    @FXML
    private void scaleButtonHandler(InputMethodEvent event) {    
        try{
            scale = Double.parseDouble(textFieldSigmaScale.getText().trim());
        }
        catch(NumberFormatException e){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("All inputs must be numbers");
            alert.setContentText("Check inputs and try again");
            alert.showAndWait();
        }
        
        if (newRun.hasRun()){
            displayEnvelope();
        }
    }    
    
     @FXML
    private void handleSimulationRunNow(InputMethodEvent event) {
    }

    @FXML
    private void handleUpdateIrisPlot(InputMethodEvent event) {
    }
   
     @FXML
    private void handleUpdateChopperPlot(InputMethodEvent event) {
    }
    
    //------------------------HELP METHODS------------------------------------
    
    /**
     * Gets parameters from simulation objects and assigns to text fields
     */
    private void getParameters(){
        
        double[] init = newRun.getInitialBeamParameters();
        
        textField_x.setText(Double.toString(init[0]));
        textField_xp.setText(Double.toString(init[1]));
        textField_y.setText(Double.toString(init[2]));
        textField_yp.setText(Double.toString(init[3]));
        
        double[] TwissX = newRun.getTwissX();
        double[] TwissY = newRun.getTwissY();        
        textField_alphax.setText(Double.toString(TwissX[0]));
        textField_betax.setText(Double.toString(TwissX[1]));
        textField_emittx.setText(Double.toString(TwissX[2]));        
        textField_alphay.setText(Double.toString(TwissY[0]));
        textField_betay.setText(Double.toString(TwissY[1]));
        textField_emitty.setText(Double.toString(TwissY[2]));
                      
        textField_sol1.setText(Double.toString(newRun.getSolenoid1Field()));
        textField_sol2.setText(Double.toString(newRun.getSolenoid2Field()));
        textField_V1.setText(Double.toString(newRun.getVsteerer1Field()));
        textField_H1.setText(Double.toString(newRun.getHsteerer1Field()));
        textField_V2.setText(Double.toString(newRun.getVsteerer2Field()));
        textField_H2.setText(Double.toString(newRun.getHsteerer2Field()));
        textField_bc.setText(Double.toString(newRun.getBeamCurrent()));
        textField_scc.setText(Double.toString(newRun.getSpaceChargeCompensation()));
    }
    
    /**
     * Sets simulation parameters from text fields.
     */
    private void setParameters(){
        
        try{
            newRun.setInitialBeamParameters(
                    Double.parseDouble((textField_x.getText().trim()))*0.001,
                    Double.parseDouble((textField_xp.getText().trim()))*0.001,
                    Double.parseDouble((textField_y.getText().trim()))*0.001,
                    Double.parseDouble((textField_yp.getText().trim()))*0.001);
            newRun.setSolenoid1Field(Double.parseDouble(textField_sol1.getText().trim())*0.001);
            newRun.setSolenoid2Field(Double.parseDouble(textField_sol2.getText().trim())*0.001);
            newRun.setVsteerer1Field(Double.parseDouble(textField_V1.getText().trim())*0.001);
            newRun.setHsteerer1Field(Double.parseDouble(textField_H1.getText().trim())*0.001);
            newRun.setVsteerer2Field(Double.parseDouble(textField_V2.getText().trim())*0.001);
            newRun.setHsteerer2Field(Double.parseDouble(textField_H2.getText().trim())*0.001);
            newRun.setBeamCurrent(Double.parseDouble(textField_bc.getText().trim())*0.001);
            newRun.setBeamTwissX(Double.parseDouble(textField_alphax.getText().trim()), Double.parseDouble(textField_betax.getText().trim()), Double.parseDouble(textField_emittx.getText().trim())*0.000001);
            newRun.setBeamTwissY(Double.parseDouble(textField_alphay.getText().trim()), Double.parseDouble(textField_betay.getText().trim()), Double.parseDouble(textField_emitty.getText().trim())*0.000001);
            newRun.setSpaceChargeCompensation(Double.parseDouble(textField_scc.getText().trim()));
        }
        catch(NumberFormatException e){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("All inputs must be numbers");
            alert.setContentText("Check inputs and try again");
            alert.showAndWait();
        }
    }
    
    /**
     * Retrieves data from a run simulation and displays plots. 
     * @param newRun The run simulation
     */
    private void displayData(SimulationRunner newRun){
        retrieveData(newRun);
        displayPlots();
    }
    
    /**
     * Retrieves and displays trajectory plots
     * @param newRun the simulation
     */
    private void retrieveData(SimulationRunner newRun){
        
        //retrieves data
        sigmaX = newRun.getSigmaX();
        sigmaY = newRun.getSigmaY();
        sigmaR = newRun.getSigmaR();
        positions = newRun.getPositions();
        posX = newRun.getPosX();
        posY = newRun.getPosY();
        posR = newRun.getPosR();
        posPhi = newRun.getPosPhi();
    }
    
    /**
     * Checks which coordinate system radio button is selected and displays plots accordingly
     */
    private void displayPlots(){
        
        addTrajectorySeriesToPlot();
        
        if (radioButtonCart.isSelected()){
            displayCartTrajectory();
        }
        else if (radioButtonCyl.isSelected()){
            displayCylTrajectory();
        }
        
        displayEnvelope();
    }
    
    /**
     * Checks which coordinate system radio button is selected and displays envelope accordingly
     */
    private void displayEnvelope(){
        
        addEnvelopeSeriesToPlot();
        
        yAxis1.setAutoRanging(true);
        
        if (radioButtonCart.isSelected()){
            displayCartEnvelope();
        }
        else if (radioButtonCyl.isSelected()){
            displayCylEnvelope();
        }
    }
    
    /**
     * Displays the trajectory in Cartesian coordinates
     */
    private void displayCartTrajectory(){
        
        clearTrajectorySeries();
        
        for (int i = 0; i < posX.size() ; i++) {
            seriesX.getData().add(new XYChart.Data(positions.get(i), posX.get(i)));
            seriesY.getData().add(new XYChart.Data(positions.get(i), posY.get(i)));
        }
        
        yAxis.setLabel("Offset (mm)");
        
        //set colors
        Legend legend = (Legend)plot1.lookup(".chart-legend");
        legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #ff0000, white;");
        legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #006ddb, white;");
        seriesX.getNode().setStyle("-fx-stroke: #ff0000;");
        seriesY.getNode().setStyle("-fx-stroke: #006ddb;");
    }
    
    /**
     * Displays the trajectory in cylindrical coordinates
     */
    private void displayCylTrajectory(){
        
        clearTrajectorySeries();
        
        int scale2 = getScaleAxis(posPhi,posR);
        
        for (int i = 0; i < posX.size() ; i++) {
            seriesR.getData().add(new XYChart.Data(positions.get(i), posR.get(i)));
            seriesPhi.getData().add(new XYChart.Data(positions.get(i), new Double(posPhi.get(i).toString())*scale2));
        }
        
        if (scale2 != 1){
            yAxis.setLabel("Offset (mm) \nAngle (" + Double.toString((double) 1/scale2) + " * π rad)");
            System.out.print(scale2);
        }
        else{
            yAxis.setLabel("Offset (mm) \nAngle (π rad)");
        }
        
        //set colors
        Legend legend = (Legend)plot1.lookup(".chart-legend");
        legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #006400, white;");
        legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #DAA520, white;");        
        seriesR.getNode().setStyle("-fx-stroke: #006400;");
        seriesPhi.getNode().setStyle("-fx-stroke: #DAA520;");
    }
    
    /**
     * Checks which offset radio button is selected and displays envelope accordingly in Cartesian coordinates
     */
    private void displayCartEnvelope(){
        
        clearEnvelopeSeries();
        
        setCartBounds();
        
        if (radioButtonOffsetOff.isSelected()){
            
            for (int i = 0; i < sigmaX[0].size(); i++){
                seriesSigmaX[0].getData().add(new XYChart.Data(positions.get(i), ((double) sigmaX[0].get(i))*scale));
                seriesSigmaY[0].getData().add(new XYChart.Data(positions.get(i), ((double) sigmaY[0].get(i))*scale));
                seriesSigmaX[1].getData().add(new XYChart.Data(positions.get(i), ((double) sigmaX[1].get(i))*scale));
                seriesSigmaY[1].getData().add(new XYChart.Data(positions.get(i), ((double) sigmaY[1].get(i))*scale));
            }
            
            //set colors
            
            
            seriesSigmaX[0].getNode().setStyle("-fx-stroke: #ff0000;");
            seriesSigmaY[0].getNode().setStyle("-fx-stroke: #006ddb;");
            seriesSigmaX[1].getNode().setStyle("-fx-stroke: #ff0000;");
            seriesSigmaY[1].getNode().setStyle("-fx-stroke: #006ddb;");
            
        }
        else if (radioButtonOffsetOn.isSelected()){           
            for (int i = 0; i < sigmaX[0].size(); i++){               
                seriesSigmaOffsetX[0].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaX[0],posX,scale,i)));
                seriesSigmaOffsetY[0].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaY[0],posY,scale,i)));
                seriesSigmaOffsetX[1].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaX[1],posX,scale,i)));
                seriesSigmaOffsetY[1].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaY[1],posY,scale,i)));
            }
            
            //set colors
            seriesSigmaOffsetX[0].getNode().setStyle("-fx-stroke: #ff0000;");
            seriesSigmaOffsetY[0].getNode().setStyle("-fx-stroke: #006ddb;");
            seriesSigmaOffsetX[1].getNode().setStyle("-fx-stroke: #ff0000;");
            seriesSigmaOffsetY[1].getNode().setStyle("-fx-stroke: #006ddb;");
        }
        
        //set legend colors
        Legend legend = (Legend)plot2.lookup(".chart-legend");
        legend.getItems().remove(0, 4);
        legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #ff0000, white;");
        legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #006ddb, white;");
    }
    
    /**
     * Checks which offset radio button is selected and displays envelope accordingly in cylindrical coordinates
     */
    private void displayCylEnvelope(){
        
        clearEnvelopeSeries();
        
        setCylBounds();
        
        if (radioButtonOffsetOff.isSelected()){
            
            for (int i = 0; i < sigmaX[0].size(); i++){
                seriesSigmaR[0].getData().add(new XYChart.Data(positions.get(i), ((double) sigmaR[0].get(i))*scale));
                seriesSigmaR[1].getData().add(new XYChart.Data(positions.get(i), ((double) sigmaR[1].get(i))*scale));
                
            }
            if (Collections.max(sigmaR[0]) > 100){
                yAxis1.setUpperBound(Collections.max(sigmaR[0])+10);
            }
            
            seriesSigmaR[0].getNode().setStyle("-fx-stroke: #006400;");
            seriesSigmaR[1].getNode().setStyle("-fx-stroke: #006400;");
        }
        else if (radioButtonOffsetOn.isSelected()){
            
            for (int i = 0; i < sigmaR[0].size(); i++){
                seriesSigmaOffsetR[0].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaR[0],posR,scale,i)));
                seriesSigmaOffsetR[1].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaR[1],posR,scale,i)));
            }
            
            //Test for new upper bound
            if (((double) Collections.max(sigmaR[0])*scale+(double) Collections.max(posR)) > 100){
                yAxis1.setUpperBound(((double) Collections.max(sigmaR[0])*scale+(double) Collections.max(posR))+10);
            }
            
            seriesSigmaOffsetR[0].getNode().setStyle("-fx-stroke: #006400;");
            seriesSigmaOffsetR[1].getNode().setStyle("-fx-stroke: #006400;");
        }
        
        //set legend colors
        Legend legend = (Legend)plot2.lookup(".chart-legend");
        legend.getItems().remove(0, 3);
        legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #006400, white;");
    }
    
    /**
     * Determines which surrounding to show
     */
    private void setBounds(){
        if (radioButtonCart.isSelected()){
            setCartBounds();
        }
        else if (radioButtonCyl.isSelected()){
            setCylBounds();
        }
    }
    
    /**
     * Displays the surroundings in both positive and negative range.
    */
    private void setCartBounds(){
        
        yAxis1.setAutoRanging(true);
    }
    
    /**
     * Displays the surroundings in only positive range.
     */
    private void setCylBounds(){
        
        yAxis1.setAutoRanging(false);
        yAxis1.setLowerBound(0.0);
        yAxis1.setUpperBound(90);
        
    }
    
    private void setLabels(){
        if (radioButtonCart.isSelected()){
            yAxis.setLabel("Offset (mm)");
        }
        else if (radioButtonCyl.isSelected()){
            yAxis.setLabel("Offset (mm) \n Angle (π rad)");
        }
    }
    
    private int getScaleAxis(ArrayList<Double> posphi, ArrayList<Double> posr){
        
        int i = 1;
        
        double maxphi = Collections.max(posphi);
        double maxr = Collections.max(posr);
        
        if (maxphi != 0){
            while (i*maxphi <= maxr){
                i=i*10;
            }
        }
        
        return i;
    }
    
    private double scaleAndOffset(ArrayList<Double> sigma, ArrayList<Double> pos, double scale, int i){
        return ((double) sigma.get(i))*scale+(double) pos.get(i);
    }
    
//-------------------------PLOT FUNCTIONS---------------------------------------
    
    /**
     * Removes the series from plot and adds the relevant series.
     */
    private void addTrajectorySeriesToPlot(){
        
        plot1.getData().removeAll(seriesX,seriesY,seriesR,seriesPhi);
        
        if(radioButtonCart.isSelected()){
            plot1.getData().add(seriesX);       
            plot1.getData().add(seriesY); 

        }
        else if (radioButtonCyl.isSelected()){
            plot1.getData().add(seriesR);       
            plot1.getData().add(seriesPhi); 
        }
    }
    
    /**
     * Removes series from plot and adds the relevant series.
     */
    private void addEnvelopeSeriesToPlot(){
        
        plot2.getData().removeAll(seriesSigmaX[0],seriesSigmaX[1],
            seriesSigmaY[0],seriesSigmaY[1],
            seriesSigmaR[0],seriesSigmaR[1],
            seriesSigmaOffsetX[0],seriesSigmaOffsetX[1],
            seriesSigmaOffsetY[0],seriesSigmaOffsetY[1],
            seriesSigmaOffsetR[0],seriesSigmaOffsetR[1]);
        
        if(radioButtonCart.isSelected()){
            
            if(radioButtonOffsetOn.isSelected()){
                for(int i = 0; i < seriesSigmaX.length; i++){
                    plot2.getData().add(seriesSigmaOffsetX[i]);       
                    plot2.getData().add(seriesSigmaOffsetY[i]);  
                }
                seriesSigmaOffsetX[1].setName("σ_x");
                seriesSigmaOffsetY[1].setName("σ_y");
            }
            else if (radioButtonOffsetOff.isSelected()){
                for(int i = 0; i < seriesSigmaX.length; i++){
                    plot2.getData().add(seriesSigmaX[i]);       
                    plot2.getData().add(seriesSigmaY[i]);  
                }
                seriesSigmaX[1].setName("σ_x");
                seriesSigmaY[1].setName("σ_y");
            }

        }
        else if (radioButtonCyl.isSelected()){
            if(radioButtonOffsetOn.isSelected()){
                for(int i = 0; i < seriesSigmaX.length; i++){
                    plot2.getData().add(seriesSigmaOffsetR[i]);       
                }
                seriesSigmaOffsetR[1].setName("σ_r");
            }
            else if (radioButtonOffsetOff.isSelected()){
                for(int i = 0; i < seriesSigmaX.length; i++){
                    plot2.getData().add(seriesSigmaR[i]);       
                }
                seriesSigmaR[1].setName("σ_r");
            }
        }
    }

    //-------------------------CLEAR METHODS------------------------------------
    
    /**
     * Clears plot1 of all data
     */
    private void clearTrajectorySeries(){
        seriesX.getData().clear();
        seriesY.getData().clear();
        seriesR.getData().clear();
        seriesPhi.getData().clear();
    }
    
    /**
     * Clears plot2 of all data
     */
    private void clearEnvelopeSeries(){
        for (int i = 0; i < seriesSigmaX.length; i++){
            seriesSigmaX[i].getData().clear();
            seriesSigmaY[i].getData().clear();
            seriesSigmaR[i].getData().clear();
            seriesSigmaOffsetX[i].getData().clear();
            seriesSigmaOffsetY[i].getData().clear();
            seriesSigmaOffsetR[i].getData().clear();
        }
    }

 //-----------------------CONVERTION FUNCTIONS-------------------------------
    
    /**
     * Converts solenoid current to BField
     * @param current The solenoid current (A)
     * @return B The corresponding BField (mT)
     */
    private double solCurrentToBField(double current){
        return SOLENOID_CURRENT_TO_PEAK_RATE*current;
    }
    
    /**
     * Converts solenoid BField to current 
     * @param BField of the solenoid (mT)
     * @return current in the solenoid (A)
     */
    private double solBFieldtoCurrent(double BField){
        return SOLENOID_PEAK_RATE_TO_CURRENT*BField;
    }
    
    /**
     * Returns an B field from Current for the horizontal steerer.
     * @param current The current of the horizontal steerer (A)
     * @return B field for the horizontal steerer (mT)
     */
    private double hSteererCurrentToBFields(double current){
               
        return HSTEERER_CURRENT_TO_PEAK_RATE*current;
    }
    
     /**
     * Returns an B field from Current for the Vertical steerer
     * @param current The current of the vertical steerer (A)
     * @return B field for the vertical steerer (mT)
     */
    private double vSteererCurrentToBFields(double current){
        
        return VSTEERER_CURRENT_TO_PEAK_RATE*current;
    }
    
    /**
     * Returns an B field from Current for the horizontal steerer.
     * @param BField field for the horizontal steerer (mT)
     * @return current The current of the horizontal steerer (A)
     */
    private double hSteererBFieldtoCurrent(double BField){
               
        return HSTEERER_PEAK_RATE_TO_CURRENT*BField;
    }
    
     /**
     * Returns an B field from Current for the Vertical steerer
     * @param BField field for the vertical steerer (mT) (A)
     */
    private double vSteererBFieldtoCurrent(double BField){
        
        return VSTEERER_PEAK_RATE_TO_CURRENT*BField;
    }
    
}