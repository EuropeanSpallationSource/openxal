/*
 * FXMLPlotPropertiesController.java
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
 * Plot properties dialog. 
 * @author nataliamilas
 * 06-2017
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * Secondary Controller that creates a window to modify the axis of a plot
 * @author nataliamilas
 * 06-2017
 */

public class PlotPropertiesController{

    private final BooleanProperty loggedIn = new SimpleBooleanProperty();
    private double yMax;
    private double yMin;
    private Boolean yAutoscale;
    private double xMax;
    private double xMin;
    private Boolean xAutoscale;
    private double cMax;
    private double cMin;
    private Boolean cAutoscale;
    private double pMax;
    private double pMin;
    private Boolean pAutoscale;
    @FXML
    private CheckBox checkBoxX;
    @FXML
    private CheckBox checkBoxY;
    @FXML
    private CheckBox checkBoxC;
    @FXML
    private CheckBox checkBoxP;
    @FXML
    private TextField textXMax;
    @FXML
    private TextField textXMin;
    @FXML
    private TextField textYMin;
    @FXML
    private TextField textYMax;
    @FXML
    private TextField textCMin;
    @FXML
    private TextField textCMax;
    @FXML
    private TextField textPMin;
    @FXML
    private TextField textPMax;
 
    public BooleanProperty loggedInProperty() {
        return loggedIn ;
    }

    public final boolean isLoggedIn() {
        return loggedInProperty().get();
    }

    public final void setLoggedIn(boolean loggedIn) {
        loggedInProperty().set(loggedIn);
    } 

    public double getcMax() {
        return cMax;
    }

    public void setcMax(double cMax) {
        this.cMax = cMax;
    }

    public double getcMin() {
        return cMin;
    }

    public void setcMin(double cMin) {
        this.cMin = cMin;
    }

    public Boolean getcAutoscale() {
        return cAutoscale;
    }

    public void setcAutoscale(Boolean cAutoscale) {
        this.cAutoscale = cAutoscale;
    }

    public double getpMax() {
        return pMax;
    }

    public void setpMax(double pMax) {
        this.pMax = pMax;
    }

    public double getpMin() {
        return pMin;
    }

    public void setpMin(double pMin) {
        this.pMin = pMin;
    }

    public Boolean getpAutoscale() {
        return pAutoscale;
    }

    public void setpAutoscale(Boolean pAutoscale) {
        this.pAutoscale = pAutoscale;
    }

    public double getyMax() {
        return yMax;
    }
    
    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

    public double getyMin() {
        return yMin;
    }

    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    public Boolean getyAutoscale() {
        return yAutoscale;
    }

    public void setyAutoscale(Boolean yAutoscale) {
        this.yAutoscale = yAutoscale;
    }

    public double getxMax() {
        return xMax;
    }

    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    public double getxMin() {
        return xMin;
    }

    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    public Boolean getxAutoscale() {
        return xAutoscale;
    }

    public void setxAutoscale(Boolean xAutoscale) {
        this.xAutoscale = xAutoscale;
    }

    @FXML
    public void handleButtonOK(){
        this.xMax = Double.parseDouble(textXMax.getText());
        this.xMin = Double.parseDouble(textXMin.getText());
        this.xAutoscale = checkBoxX.isSelected();
        this.yMax = Double.parseDouble(textYMax.getText());
        this.yMin = Double.parseDouble(textYMin.getText());
        this.yAutoscale = checkBoxY.isSelected();
        this.cMax = Double.parseDouble(textCMax.getText());
        this.cMin = Double.parseDouble(textCMin.getText());
        this.cAutoscale = checkBoxC.isSelected();
        this.pMax = Double.parseDouble(textPMax.getText());
        this.pMin = Double.parseDouble(textPMin.getText());
        this.pAutoscale = checkBoxP.isSelected();
        setLoggedIn(true);
      
    } 
    
    @FXML
    public void handleButtonCancel(){
        setLoggedIn(true);
    }
    
    public void setPlotProperties(NumberAxis axisX, NumberAxis axisY, NumberAxis axisC, NumberAxis axisPos){
        this.xAutoscale = axisX.isAutoRanging();
        this.xMax = axisX.getUpperBound();
        this.xMin = axisX.getLowerBound();
        this.yAutoscale = axisY.isAutoRanging();
        this.yMax = axisY.getUpperBound();
        this.yMin = axisY.getLowerBound();
        this.cAutoscale = axisC.isAutoRanging();
        this.cMax = axisC.getUpperBound();
        this.cMin = axisC.getLowerBound();
        this.pAutoscale = axisPos.isAutoRanging();
        this.pMax = axisPos.getUpperBound();
        this.pMin = axisPos.getLowerBound();
        
        if(this.xAutoscale){
            textXMax.setDisable(true);
            textXMin.setDisable(true);
            checkBoxX.setSelected(true);
        }
        
        if(this.yAutoscale){
            textYMax.setDisable(true);
            textYMin.setDisable(true);
            checkBoxY.setSelected(true);
        }
        
        if(this.cAutoscale){
            textCMax.setDisable(true);
            textCMin.setDisable(true);
            checkBoxC.setSelected(true);
        }
        
        if(this.pAutoscale){
            textPMax.setDisable(true);
            textPMin.setDisable(true);
            checkBoxP.setSelected(true);
        }
        
        textYMax.setText(String.valueOf(yMax));
        textYMin.setText(String.valueOf(yMin));
        textXMax.setText(String.valueOf(xMax));
        textXMin.setText(String.valueOf(xMin));
        textCMax.setText(String.valueOf(cMax));
        textCMin.setText(String.valueOf(cMin));
        textPMax.setText(String.valueOf(pMax));
        textPMin.setText(String.valueOf(pMin));
        
    }

    @FXML
    private void handleSetAutoscale(ActionEvent event) {
        
        if(checkBoxX.isSelected()){
            textXMax.setDisable(true);
            textXMin.setDisable(true);
            this.setxAutoscale(true);
        } else {
            textXMax.setDisable(false);
            textXMin.setDisable(false);
            this.setxAutoscale(false);
        }
        
        if(checkBoxY.isSelected()){
            textYMax.setDisable(true);
            textYMin.setDisable(true);
            this.setyAutoscale(true);
        } else {
            textYMax.setDisable(false);
            textYMin.setDisable(false);
            this.setyAutoscale(false);
        }
        
        if(checkBoxC.isSelected()){
            textCMax.setDisable(true);
            textCMin.setDisable(true);
            this.setyAutoscale(true);
        } else {
            textCMax.setDisable(false);
            textCMin.setDisable(false);
            this.setyAutoscale(false);
        }
        
        if(checkBoxP.isSelected()){
            textPMax.setDisable(true);
            textPMin.setDisable(true);
            this.setyAutoscale(true);
        } else {
            textPMax.setDisable(false);
            textPMin.setDisable(false);
            this.setyAutoscale(false);
        }
        
    }
    
}
