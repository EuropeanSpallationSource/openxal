package xal.app.sofia;

import com.sun.javafx.charts.Legend;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import xal.model.ModelException;

/**
 * The class handling the lebt trajectory prediction.
 * 
 * @author sofiaolsson
 */
public class FXMLController implements Initializable {
    
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
    @FXML private Button runButton;
    @FXML private TextField textField_x;
    @FXML private TextField textField_xp;
    @FXML private TextField textField_y;
    @FXML private TextField textField_yp;
    @FXML private TextField textField_sol1;
    @FXML private TextField textField_sol2;
    @FXML private TextField textField_V1;
    @FXML private TextField textField_H1;
    @FXML private TextField textField_V2;
    @FXML private TextField textField_H2;
    @FXML private TextField textField_bc;
    @FXML private RadioButton radioButtonCart;
    @FXML private RadioButton radioButtonCyl;
    private ToggleGroup coordinateGroup;
    @FXML private RadioButton radioButtonOffsetOn;
    @FXML private RadioButton radioButtonOffsetOff;
    private ToggleGroup offsetGroup;
    @FXML private NumberAxis yAxis2;
    @FXML private TextField textFieldSigmaScale;
    @FXML private Button scaleButton;
    private double scale;
             
    @Override
    public void initialize(URL url, ResourceBundle rb) {

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
    
    //------------------------HANDLE METHODS------------------------------------
    
    /**
     * Runs the simulation. Sets simulation parameters from text fields. Displays trajectory plots
     * @param event 
     */
    @FXML
    private void runButtonAction(ActionEvent event) {
        
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
    private void scaleButtonHandler(ActionEvent event) {
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
    private void handleContextMenu(ContextMenuEvent event) {
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
        textField_sol1.setText(Double.toString(newRun.getSolenoidCurrent1()));
        textField_sol2.setText(Double.toString(newRun.getSolenoidCurrent2()));
        textField_V1.setText(Double.toString(newRun.getVsteerer1Current()));
        textField_H1.setText(Double.toString(newRun.getHsteerer1Current()));
        textField_V2.setText(Double.toString(newRun.getVsteerer2Current()));
        textField_H2.setText(Double.toString(newRun.getHsteerer2Current()));
        textField_bc.setText(Double.toString(newRun.getBeamCurrent()));
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
            newRun.setSolenoidCurrent1(Double.parseDouble(textField_sol1.getText().trim()));
            newRun.setSolenoidCurrent2(Double.parseDouble(textField_sol2.getText().trim()));
            newRun.setVsteerer1Current(Double.parseDouble(textField_V1.getText().trim()));
            newRun.setHsteerer1Current(Double.parseDouble(textField_H1.getText().trim()));
            newRun.setVsteerer2Current(Double.parseDouble(textField_V2.getText().trim()));
            newRun.setHsteerer2Current(Double.parseDouble(textField_H2.getText().trim()));
            newRun.setBeamCurrent(Double.parseDouble(textField_bc.getText().trim())*0.001);
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
            yAxis.setLabel("Offset (mm) \nAngle (π rad)");
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
}

