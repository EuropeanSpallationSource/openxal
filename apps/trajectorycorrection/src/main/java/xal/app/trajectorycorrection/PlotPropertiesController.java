/*
 * PlotPropertiesController.java
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
package xal.app.trajectorycorrection;

/**
 * Secondary Controller that creates a window to modify the axis of a plot
 * @author nataliamilas
 * 06-2017
 */


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author nataliamilas
 */
public class PlotPropertiesController{
    
    private final BooleanProperty loggedIn = new SimpleBooleanProperty();
    private double yMax;
    private double yMin;
    private Boolean yAutoscale;
    private double xMax;
    private double xMin;
    private Boolean xAutoscale;
    @FXML
    private CheckBox checkBoxX;
    @FXML
    private TextField textXMax;
    @FXML
    private TextField textXMin;
    @FXML
    private TextField textYMin;
    @FXML
    private TextField textYMax;
    @FXML
    private CheckBox checkBoxY;
    
    
    public BooleanProperty loggedInProperty() {
        return loggedIn ;
    }

    public final boolean isLoggedIn() {
        return loggedInProperty().get();
    }

    public final void setLoggedIn(boolean loggedIn) {
        loggedInProperty().set(loggedIn);
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
        setLoggedIn(true);
      
    } 
    
    public void setPlotProperties(double xMax, double xMin, boolean xAuto, double yMax, double yMin, boolean yAuto){
        this.xAutoscale = xAuto;
        this.xMax = xMax;
        this.xMin = xMin;
        this.yAutoscale = yAuto;
        this.yMax = yMax;
        this.yMin = yMin;
        
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
        
        textYMax.setText(String.valueOf(yMax));
        textYMin.setText(String.valueOf(yMin));
        textXMax.setText(String.valueOf(xMax));
        textXMin.setText(String.valueOf(xMin));
        
    }

    @FXML
    private void handleSetAutoscale(MouseEvent event) {
        
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
        
    }
    
}
