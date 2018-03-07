package xal.app.lebt;

import com.sun.javafx.charts.Legend;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;
import xal.ca.BatchGetValueRequest;
import xal.ca.Channel;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.extension.jels.smf.impl.ESSMagFieldMap3D;
import xal.model.ModelException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 * The class handling the LEBT trajectory prediction.
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
    private Accelerator accelerator;
    private StatusAnimationTimer updateTimer;
    private SimpleBooleanProperty updateSimul;
    private SimpleBooleanProperty updatePVs;
    
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
    
    //input beam parameters
    private double[] initPos;
    private double beamCurrent;
    private double[] TwissX;
    private double[] TwissY;
    private double spaceChargeComp;
    
    //Map the live machine values
    private HashMap<Channel,Label> displayValues;
    private HashMap<Channel,TextField> setValues;
    private BatchGetValueRequest request;
    
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
    @FXML private CheckBox comboBox_currentFC;
    @FXML private CheckBox comboBox_posNPM;
    @FXML private CheckBox comboBox_sigmaNPM;
    @FXML private Label label_highVoltageRB;
    @FXML private Label label_chopperVoltageRB;
    @FXML private TextField textField_irisX;
    @FXML private TextField textField_irisY;
    @FXML private Label label_irisXRB;
    @FXML private Label label_irisYRB;
    @FXML private Label label_sol1fieldRB;
    @FXML private Label label_CV1fieldRB;
    @FXML private Label label_CH1fieldRB;
    @FXML private Label label_sol2fieldRB;
    @FXML private Label label_CV2fieldRB;
    @FXML private Label label_CH2fieldRB;
    @FXML private Label label_FC;
    @FXML private Label label_BCM;
    @FXML private Label label_Doppler;
    @FXML private CheckBox checkBox_electrode;
    @FXML private Circle electrodeStatus;

             
    @Override
    public void initialize(URL url, ResourceBundle rb) {
            
        //Initializes boolean flags
        updateSimul = new SimpleBooleanProperty();
        updatePVs = new SimpleBooleanProperty();
        updateSimul.set(false);
        updatePVs.set(false);
        
        //initializing toggle groups
        coordinateGroup = new ToggleGroup();
        offsetGroup = new ToggleGroup();
        
        radioButtonCart.setToggleGroup(coordinateGroup);
        radioButtonCart.setSelected(true);
        radioButtonCyl.setToggleGroup(coordinateGroup);
        
        radioButtonOffsetOff.setToggleGroup(offsetGroup);
        radioButtonOffsetOn.setToggleGroup(offsetGroup);
        radioButtonOffsetOff.setSelected(true);
                
        try {
            surroundings = readVacuumChamber();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }               
        
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
        
       //Showing surroundings
        for (int i = 0; i < surroundings[0].length ; i++) {
            seriesSurroundings[0].getData().add(new XYChart.Data(surroundings[0][i], surroundings[1][i]*1000));
            seriesSurroundings[1].getData().add(new XYChart.Data(surroundings[0][i], -surroundings[1][i]*1000));
        }       
        
        //Add surroundings        
        plot1.setAnimated(false);
        plot2.setAnimated(false);
        plot1.setCreateSymbols(false);
        plot2.setCreateSymbols(false);       
        plot2.getData().add(seriesSurroundings[0]);
        plot2.getData().add(seriesSurroundings[1]); 
        seriesSurroundings[0].getNode().setStyle("-fx-stroke: #000000;");
        seriesSurroundings[1].getNode().setStyle("-fx-stroke: #000000;");
        
        
         //remove surrounding legend
        Legend legend = (Legend)plot2.lookup(".chart-legend");
        legend.getItems().remove(0, 2);

         
        scale = 1;
        initPos = new double[4];
        TwissX = new double[3];
        TwissY = new double[3];
        
        //Set textField formatting
        StringConverter<Double> formatter2d;
        StringConverter<Double> formatter3d;
        StringConverter<Double> formatter4d;
        formatter4d = new StringConverter<Double>(){
            @Override
            public Double fromString(String string)
            {
               return Double.parseDouble(string);
            }

            @Override
            public String toString(Double object)
            {
               if (object == null)
                  return "0.0000";   
               return String.format("%.4f",object);
            }            
        };
        formatter3d = new StringConverter<Double>(){
            @Override
            public Double fromString(String string)
            {
               return Double.parseDouble(string);
            }

            @Override
            public String toString(Double object)
            {
               if (object == null)
                  return "0.0000";
               return String.format("%.3f",object);
            }            
        };
        formatter2d = new StringConverter<Double>(){
            @Override
            public Double fromString(String string)
            {
               return Double.parseDouble(string);
            }

            @Override
            public String toString(Double object)
            {
               if (object == null)
                  return "0.0000";
               return String.format("%.2f",object);
            }            
        };
       
                         
        
        //Ion Source
        textField_magnetron.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_coil1.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_coil2.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_coil3.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_H2flow.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_highVoltage.setTextFormatter(new TextFormatter<Double>(formatter3d));
        
        //LEBT
        textField_sol1field.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_sol2field.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_CV1field.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_CH1field.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_CV2field.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_CH2field.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_irisAperture.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_irisX.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_irisY.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_chopperVoltage.setTextFormatter(new TextFormatter<Double>(formatter3d));
        
        //input values
        textField_x.setTextFormatter(new TextFormatter<Double>(formatter4d));
        textField_xp.setTextFormatter(new TextFormatter<Double>(formatter4d));
        textField_y.setTextFormatter(new TextFormatter<Double>(formatter4d));
        textField_yp.setTextFormatter(new TextFormatter<Double>(formatter4d));
        textField_betax.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_alphax.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_emittx.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_betay.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_alphay.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_emitty.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_bc.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_scc.setTextFormatter(new TextFormatter<Double>(formatter2d));    
        textFieldSigmaScale.setTextFormatter(new TextFormatter<Double>(formatter2d));    
        
        textFieldSigmaScale.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try{
                        scale = Double.parseDouble(textFieldSigmaScale.getText().trim());
                    }
                    catch(NumberFormatException e){
                        scale = 1.0;
                    } 
                }
            });                                               
        
        //Map the channels to the label or textfield it belongs
        displayValues = new HashMap<Channel,Label>();
        setValues = new HashMap<Channel,TextField>();       
        Accelerator accl = MainFunctions.mainDocument.getAccelerator();              
               
        //Ion Source
        AcceleratorSeq sequence = accl.getSequence("ISRC");
        AcceleratorNode Magnetron = sequence.getNodeWithId("MAGNETRON");
        displayValues.put(Magnetron.getChannel("ForwdPrwRB"),label_magnetronRB);
        setValues.put(Magnetron.getChannel("ForwdPrwSet"),textField_magnetron);
        //textField_magnetron.setText(String.format("%.3f",Magnetron.getChannel("ForwdPrwRB").getValDbl()));
        textField_magnetron.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_magnetron.getText());
                    if(val>=0 && val<=1500){
                        Magnetron.getChannel("ForwdPrwSet").putVal(val);
                    } else {
                        textField_magnetron.setText(Double.toString(Magnetron.getChannel("ForwdPrwSet").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode HighVoltage = sequence.getNodeWithId("MFC");
        displayValues.put(HighVoltage.getChannel("V"),label_highVoltageRB);
        setValues.put(HighVoltage.getChannel("V_Set"),textField_highVoltage);
        //textField_highVoltage.setText(String.format("%.3f",HighVoltage.getChannel("V_Read").getValDbl()));
        textField_highVoltage.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_highVoltage.getText());
                    if(val>=70 && val<=80){
                        HighVoltage.getChannel("V_Set").putVal(val);
                    } else {
                        textField_highVoltage.setText(Double.toString(HighVoltage.getChannel("V_Set").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        displayValues.put(HighVoltage.getChannel("H2FlowRB"),label_H2flowRB);
        setValues.put(HighVoltage.getChannel("H2FlowSet"),textField_H2flow);
        //textField_H2flow.setText(String.format("%.3f",HighVoltage.getChannel("H2FLowRead").getValDbl()));
        textField_H2flow.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_H2flow.getText());
                    if(val>=0 && val <=5){
                        HighVoltage.getChannel("H2FlowSet").putVal(val);
                    } else {
                        textField_H2flow.setText(Double.toString(HighVoltage.getChannel("H2FlowSet").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode Coil1 = sequence.getNodeWithId("COIL01");
        displayValues.put(Coil1.getChannel("I"),label_coil1RB);
        setValues.put(Coil1.getChannel("I_Set"),textField_coil1);
        //textField_coil1.setText(String.format("%.3f",Coil1.getChannel("I").getValDbl()));
        textField_coil1.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_coil1.getText());
                    if(val>=0 && val <=300){
                        Coil1.getChannel("I_Set").putVal(val);
                    } else {
                        textField_coil1.setText(Double.toString(Coil1.getChannel("I_Set").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode Coil2 = sequence.getNodeWithId("COIL02");
        displayValues.put(Coil2.getChannel("I"),label_coil2RB);
        setValues.put(Coil2.getChannel("I_Set"),textField_coil2);
        //textField_coil2.setText(String.format("%.3f",Coil2.getChannel("I").getValDbl()));
        textField_coil2.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_coil2.getText());
                    if(val>=0 && val <=300){
                        Coil2.getChannel("I_Set").putVal(val);
                    } else {
                        textField_coil2.setText(Double.toString(Coil2.getChannel("I_Set").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode Coil3 = sequence.getNodeWithId("COIL03");
        displayValues.put(Coil3.getChannel("I"),label_coil3RB);
        setValues.put(Coil3.getChannel("I_Set"),textField_coil3);
        //textField_coil3.setText(String.format("%.3f",Coil3.getChannel("I").getValDbl()));
        textField_coil3.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_coil3.getText());
                    if(val>=0 && val <=300){
                        Coil3.getChannel("I_Set").putVal(val);
                    } else {
                        textField_coil3.setText(Double.toString(Coil3.getChannel("I_Set").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        //LEBT
        sequence = accl.getSequence("LEBT");
        AcceleratorNode Solenoid1 = sequence.getNodeWithId("FM1:MFM");
        displayValues.put(Solenoid1.getChannel("I"),label_sol1currentRB);
        displayValues.put(Solenoid1.getChannel("fieldRB"),label_sol1fieldRB);
        displayValues.put(Solenoid1.getChannel("I_Set"),label_sol1current);
        setValues.put(Solenoid1.getChannel("fieldSet"),textField_sol1field);
        //textField_sol1field.setText(String.format("%.3f",Solenoid1.getChannel("fieldRB").getValDbl()));
        textField_sol1field.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_sol1field.getText());
                    if(val>=0 && val<450){
                        Solenoid1.getChannel("fieldSet").putVal(val);
                    } else {
                        textField_sol1field.setText(Double.toString(Solenoid1.getChannel("fieldRB").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode  Solenoid2 = sequence.getNodeWithId("FM2:MFM");
        displayValues.put(Solenoid2.getChannel("I"),label_sol2currentRB);
        displayValues.put(Solenoid2.getChannel("I_Set"),label_sol2current);
        displayValues.put(Solenoid2.getChannel("fieldRB"),label_sol2fieldRB);
        setValues.put(Solenoid2.getChannel("fieldSet"),textField_sol2field);
        //textField_sol2field.setText(String.format("%.3f",Solenoid2.getChannel("fieldRB").getValDbl()));
        textField_sol2field.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_sol2field.getText());
                    if(val>=0 && val<450){
                        Solenoid2.getChannel("fieldSet").putVal(val);
                    } else {
                        textField_sol2field.setText(Double.toString(Solenoid2.getChannel("fieldRB").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode CV1 = sequence.getNodeWithId("ST1-VC1");
        displayValues.put(CV1.getChannel("I"),label_CV1currentRB);
        displayValues.put(CV1.getChannel("I_Set"),label_CV1current);
        displayValues.put(CV1.getChannel("fieldRB"),label_CV1fieldRB);
        setValues.put(CV1.getChannel("fieldSet"),textField_CV1field);
        //textField_CV1field.setText(String.format("%.3f",CV1.getChannel("fieldRB").getValDbl()));
        textField_CV1field.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_CV1field.getText());
                    if(val>=0){
                        CV1.getChannel("fieldSet").putVal(val);
                    } else {
                        textField_CV1field.setText(Double.toString(CV1.getChannel("fieldRB").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode CV2 = sequence.getNodeWithId("ST2-VC1");
        displayValues.put(CV2.getChannel("I"),label_CV2currentRB);
        displayValues.put(CV2.getChannel("I_Set"),label_CV2current);
        displayValues.put(CV2.getChannel("fieldRB"),label_CV2fieldRB);
        setValues.put(CV2.getChannel("fieldSet"),textField_CV2field);
        //textField_CV2field.setText(String.format("%.3f",CV2.getChannel("fieldRB").getValDbl()));
        textField_CV2field.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_CV2field.getText());
                    if(val>=0){
                        CV2.getChannel("fieldSet").putVal(val);
                    } else {
                        textField_CV2field.setText(Double.toString(CV2.getChannel("fieldRB").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode CH1 = sequence.getNodeWithId("ST1-HC1");
        displayValues.put(CH1.getChannel("I"),label_CH1currentRB);
        displayValues.put(CH1.getChannel("I_Set"),label_CH1current);
        displayValues.put(CH1.getChannel("fieldRB"),label_CH1fieldRB);
        setValues.put(CH1.getChannel("fieldSet"),textField_CH1field);
        //textField_CH1field.setText(String.format("%.3f",CH1.getChannel("fieldRB").getValDbl()));
        textField_CH1field.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_CH1field.getText());
                    if(val>=0){
                        CH1.getChannel("fieldSet").putVal(val);
                    } else {
                        textField_CH1field.setText(Double.toString(CH1.getChannel("fieldRB").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode CH2 = sequence.getNodeWithId("ST2-HC1");
        displayValues.put(CH2.getChannel("I"),label_CH2currentRB);
        displayValues.put(CH2.getChannel("I_Set"),label_CH2current);
        displayValues.put(CH2.getChannel("fieldRB"),label_CH2fieldRB);
        setValues.put(CH2.getChannel("fieldSet"),textField_CH2field);
        //textField_CH2field.setText(String.format("%.3f",CH2.getChannel("fieldRB").getValDbl()));
        textField_CH2field.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_CH2field.getText());
                    if(val>=0){
                        CH2.getChannel("fieldSet").putVal(val);
                    } else {
                        textField_CH2field.setText(Double.toString(CH2.getChannel("fieldRB").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode Iris = sequence.getNodeWithId("IRIS");
        displayValues.put(Iris.getChannel("apertureRB"),label_irisApertureRB);
        setValues.put(Iris.getChannel("apertureSet"),textField_irisAperture);
        //textField_irisAperture.setText(String.format("%.3f",Iris.getChannel("apertureRB").getValDbl()));
        textField_irisAperture.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_irisAperture.getText());
                    if(val>=0){
                        Iris.getChannel("apertureSet").putVal(val);
                    } else {
                        textField_irisAperture.setText(Double.toString(Iris.getChannel("apertureRB").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        /**
         * textField_irisAperture.textProperty().addListener((obs,old,niu)->{
         * if(old.compareTo(niu)==0){
         * double aperture = Double.parseDouble(label_irisApertureRB.getText());
         * double Xpos = Double.parseDouble(label_irisXRB.getText());
         * double Ypos = Double.parseDouble(label_irisYRB.getText());
         * updatePVs.set(true);
         * surroundings[1][188]=aperture+Xpos;
         * surroundings[1][189]=aperture+Xpos;
         * surroundings[1][190]=aperture+Xpos;
         * surroundings[1][191]=aperture+Xpos;
         * seriesSurroundings[0].getData().clear();
         * for (int i = 0; i < surroundings[0].length ; i++) {
         * seriesSurroundings[0].getData().add(new XYChart.Data(surroundings[0][i], surroundings[1][i]));
         * }
         * 
         * surroundings[1][188]=aperture-Xpos;
         * surroundings[1][189]=aperture-Xpos;
         * surroundings[1][190]=aperture-Xpos;
         * surroundings[1][191]=aperture-Xpos;
         * seriesSurroundings[1].getData().clear();
         * for (int i = 0; i < surroundings[0].length ; i++) {
         * seriesSurroundings[1].getData().add(new XYChart.Data(surroundings[0][i], -surroundings[1][i]));
         * }
         * }
         * });
         */
        
        displayValues.put(Iris.getChannel("offsetXRB"),label_irisXRB);
        setValues.put(Iris.getChannel("offsetXSet"),textField_irisX);
        //textField_irisX.setText(String.format("%.3f",Iris.getChannel("offsetXRB").getValDbl()));
        textField_irisX.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_irisX.getText());
                    if(val>=-50 && val<=50){
                        Iris.getChannel("offsetXSet").putVal(val);
                    } else {
                        textField_irisX.setText(Double.toString(Iris.getChannel("offsetXRB").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        /**
         * textField_irisX.textProperty().addListener((obs,old,niu)->{
         * if(old.compareTo(niu)==0){
         * double aperture = Double.parseDouble(label_irisApertureRB.getText());
         * double Xpos = Double.parseDouble(label_irisXRB.getText());
         * double Ypos = Double.parseDouble(label_irisYRB.getText());
         * updatePVs.set(true);
         * surroundings[1][188]=aperture+Xpos;
         * surroundings[1][189]=aperture+Xpos;
         * surroundings[1][190]=aperture+Xpos;
         * surroundings[1][191]=aperture+Xpos;
         * seriesSurroundings[0].getData().clear();
         * for (int i = 0; i < surroundings[0].length ; i++) {
         * seriesSurroundings[0].getData().add(new XYChart.Data(surroundings[0][i], surroundings[1][i]));
         * }
         * 
         * surroundings[1][188]=aperture-Xpos;
         * surroundings[1][189]=aperture-Xpos;
         * surroundings[1][190]=aperture-Xpos;
         * surroundings[1][191]=aperture-Xpos;
         * seriesSurroundings[1].getData().clear();
         * for (int i = 0; i < surroundings[0].length ; i++) {
         * seriesSurroundings[1].getData().add(new XYChart.Data(surroundings[0][i], -surroundings[1][i]));
         * }
         * }
         * });
         */
        displayValues.put(Iris.getChannel("offsetYRB"),label_irisYRB);
        setValues.put(Iris.getChannel("offsetYSet"),textField_irisY);
        //textField_irisY.setText(String.format("%.3f",Iris.getChannel("offsetYRB").getValDbl()));
        textField_irisY.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                 try {
                    double val = Double.parseDouble(textField_irisY.getText());
                    if(val>=-50 && val<=50){
                        Iris.getChannel("offsetYSet").putVal(val);
                    } else {
                        textField_irisY.setText(Double.toString(Iris.getChannel("offsetYRB").getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        /**
         * textField_irisY.textProperty().addListener((obs,old,niu)->{
         * if(old.compareTo(niu)==0){
         * double aperture = Double.parseDouble(label_irisApertureRB.getText());
         * double Xpos = Double.parseDouble(label_irisXRB.getText());
         * double Ypos = Double.parseDouble(label_irisYRB.getText());
         * updatePVs.set(true);
         * surroundings[1][188]=aperture+Xpos;
         * surroundings[1][189]=aperture+Xpos;
         * surroundings[1][190]=aperture+Xpos;
         * surroundings[1][191]=aperture+Xpos;
         * seriesSurroundings[0].getData().clear();
         * for (int i = 0; i < surroundings[0].length ; i++) {
         * seriesSurroundings[0].getData().add(new XYChart.Data(surroundings[0][i], surroundings[1][i]));
         * }
         * 
         * surroundings[1][188]=aperture-Xpos;
         * surroundings[1][189]=aperture-Xpos;
         * surroundings[1][190]=aperture-Xpos;
         * surroundings[1][191]=aperture-Xpos;
         * seriesSurroundings[1].getData().clear();
         * for (int i = 0; i < surroundings[0].length ; i++) {
         * seriesSurroundings[1].getData().add(new XYChart.Data(surroundings[0][i], -surroundings[1][i]));
         * }
         * }
         * });
         */
        
        AcceleratorNode Chopper = sequence.getNodeWithId("CHOPPER");
        displayValues.put(Chopper.getChannel("V"),label_chopperVoltageRB);
        setValues.put(Chopper.getChannel("V_Set"),textField_chopperVoltage);
        //textField_chopperVoltage.setText(String.format("%.3f",Chopper.getChannel("V").getValDbl()));
        textField_chopperVoltage.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    Chopper.getChannel("V").putVal(Double.parseDouble(textField_chopperVoltage.getText()));
                } catch (ConnectionException | PutException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode Electrode = sequence.getNodeWithId("ELECTRODE");
        //displayValues.put(Electrode.getChannel("V"),null);
        //displayValues.put(Electrode.getChannel("V"),null);
        
        //define electrode properties
        checkBox_electrode.setTooltip(new Tooltip("Turns reppeler electrode On and OFF in the simulation only."));
        checkBox_electrode.selectedProperty().addListener((obs, oldVal, newVal) ->{ 
            if(newVal){
                checkBox_electrode.setText("ON");
                electrodeStatus.setFill(Color.GREEN);
            } else {
                checkBox_electrode.setText("OFF");
                electrodeStatus.setFill(Color.RED);
            }
        });
        
        //Set scale text Field
        textFieldSigmaScale.setText(Double.toString(scale));
        
        //Create listener for intial parameters
        textField_x.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        initPos[0]=Double.parseDouble(textField_x.getText().trim())*1e-3;
                    } catch(NumberFormatException ex) {
                        textField_x.setText(Double.toString(initPos[0]));
                    }    
                }
            });
        
        textField_xp.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        initPos[1]=Double.parseDouble(textField_xp.getText().trim())*1e-3;
                    } catch(NumberFormatException ex) {
                        textField_xp.setText(Double.toString(initPos[1]));
                    }    
                }
            });
        
        textField_y.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        initPos[2]=Double.parseDouble(textField_y.getText().trim())*1e-3;
                    } catch(NumberFormatException ex) {
                        textField_y.setText(Double.toString(initPos[2]));
                    }    
                }
            });
        
        textField_yp.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        initPos[3]=Double.parseDouble(textField_yp.getText().trim())*1e-3;
                    } catch(NumberFormatException ex) {
                        textField_yp.setText(Double.toString(initPos[3]));
                    }    
                }
            });
        
        textField_betax.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_betax.getText().trim());
                        if(val>=0){
                            TwissX[1]=val;
                        } else {
                            textField_betax.setText(Double.toString(TwissX[1]));
                        }
                    } catch(NumberFormatException ex) {
                        textField_betax.setText(Double.toString(TwissX[1]));
                    }    
                }
            });
        
        textField_alphax.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_alphax.getText().trim());                        
                        TwissX[0]=val;                        
                    } catch(NumberFormatException ex) {
                        textField_alphax.setText(Double.toString(TwissX[0]));
                    }    
                }
            });
        
        textField_emittx.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_emittx.getText().trim());
                        if(val>=0){
                            TwissX[2]=val;
                        } else {
                            textField_emittx.setText(Double.toString(TwissX[2]));
                        }
                    } catch(NumberFormatException ex) {
                        textField_emittx.setText(Double.toString(TwissX[2]));
                    }    
                }
            });
        
        textField_betay.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_betay.getText().trim());
                        if(val>=0){
                            TwissY[1]=val;
                        } else {
                            textField_betay.setText(Double.toString(TwissY[1]));
                        }
                    } catch(NumberFormatException ex) {
                        textField_betay.setText(Double.toString(TwissY[1]));
                    }    
                }
            });
        
        textField_alphay.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_alphay.getText().trim());                        
                        TwissY[0]=val;                        
                    } catch(NumberFormatException ex) {
                        textField_alphay.setText(Double.toString(TwissY[0]));
                    }    
                }
            });
        
        textField_emitty.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_emitty.getText().trim());
                        if(val>=0){
                            TwissY[2]=val;
                        } else {
                            textField_emitty.setText(Double.toString(TwissY[2]));
                        }
                    } catch(NumberFormatException ex) {
                        textField_emitty.setText(Double.toString(TwissY[2]));
                    }    
                }
            });
        
        textField_bc.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){                    
                    try {
                        double val = Double.parseDouble(textField_bc.getText().trim());
                        if(val>=0){
                            beamCurrent=val;
                        } else {
                            textField_bc.setText(Double.toString(beamCurrent));
                        }
                    } catch(NumberFormatException ex) {
                        textField_bc.setText(Double.toString(beamCurrent));
                    }    
                }
            });
        
        textField_scc.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_scc.getText().trim());
                        if(val>0 && val<=1.0){
                            spaceChargeComp=Double.parseDouble(textField_scc.getText().trim());
                        } else {
                            textField_scc.setText(Double.toString(spaceChargeComp));
                        }
                    } catch(NumberFormatException ex) {
                        textField_scc.setText(Double.toString(spaceChargeComp));
                    }                    
                }
            });                        
        
        yAxis1.setAutoRanging(true);
        yAxis.setAutoRanging(true);                
        
        //Creates a batch of channels to request when updating GUI
        List<Channel> channels = new ArrayList<>();
        displayValues.keySet().forEach(channel -> channels.add(channel));
        setValues.keySet().forEach(channel -> channels.add(channel));        
        request = new BatchGetValueRequest( channels );        
        request.submitAndWait(5.0);
        
        //Initializes TextField values and update GUI
        initTextFields();
        updateGUI(); 
        
        //initializes the timer                
        updateTimer = new StatusAnimationTimer() {

                @Override
                public void handle(long now) {                  
                    updateGUI();                      
                    runSimulation();                    
                }
        };
        
        MainFunctions.mainDocument.getSequenceProperty().addListener((obs, oldVal, newVal) ->{ 
            if (updateTimer.isRunning()){
                updateTimer.stop();
            }
            if(!newVal.equals(null) && !newVal.matches("ISRC")){
                //get Sequence
                String sequenceName = MainFunctions.mainDocument.getSequence();
        
                String Sequence = MainFunctions.mainDocument.getAccelerator().getSequences().toString();
                String ComboSequence = MainFunctions.mainDocument.getAccelerator().getComboSequences().toString();
                
                //initializing simulation             
                newRun = new SimulationRunner(MainFunctions.mainDocument.getAccelerator(),MainFunctions.mainDocument.getAccelerator().getSequence("LEBT"));
                
                if (Sequence.contains(sequenceName)) {
                    newRun = new SimulationRunner(MainFunctions.mainDocument.getAccelerator(),MainFunctions.mainDocument.getAccelerator().getSequence(sequenceName));
                } else if (ComboSequence.contains(sequenceName)) {
                    newRun = new SimulationRunner(MainFunctions.mainDocument.getAccelerator(),MainFunctions.mainDocument.getAccelerator().getComboSequence(sequenceName).getPrimaryAncestor());
                }
                //assigning initial parameters
                getParameters();
                updateTimer.start();
            }
        });                   
        
    }    
    //------------------------HANDLE METHODS------------------------------------          
    
    /**
     * Update the GUI values from the Live machine
     */
    private void updateGUI(){ 
                    
        displayValues.keySet().forEach(channel ->{             
            if (channel.isConnected()){
                try {
                    displayValues.get(channel).setText(String.format("%.3f",channel.getValDbl()));
                } catch (ConnectionException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
                displayValues.get(channel).setStyle("-fx-background-color: white;");                
            } else {
                displayValues.get(channel).setStyle("-fx-background-color: magenta;");
            }
        }); 

        setValues.keySet().forEach(channel ->{             
            if (channel.isConnected()){
                setValues.get(channel).setStyle("-fx-background-color: white;");
                setValues.get(channel).setDisable(false);
            } else {
                setValues.get(channel).setStyle("-fx-background-color: magenta;");
                setValues.get(channel).setDisable(true);
            }
        }); 


        //Display Current if combo box is selected
        if (comboBox_currentFC.isSelected()){
            try {
                double val = MainFunctions.mainDocument.getAccelerator().getNode("FC1").getChannel("currentAvg").getValDbl();
                if(val>0){
                    textField_bc.setText(Double.toString(val));
                    beamCurrent = val;
                } else {
                    textField_bc.setText(Double.toString(beamCurrent));
                }
            } catch (ConnectionException | GetException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }  
    
    /**
     * Initializes the values in the textFields
     */
    private void initTextFields(){   
                              
        setValues.keySet().forEach(channel ->{             
            if (channel.isConnected()){
                try {
                    setValues.get(channel).setText(String.format("%.3f",channel.getValDbl()));
                    setValues.get(channel).setStyle("-fx-background-color: white;");
                    setValues.get(channel).setDisable(false);
                } catch (ConnectionException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                setValues.get(channel).setStyle("-fx-background-color: magenta;");
                setValues.get(channel).setDisable(true);
            }
        });                
                      
    }
    
    
    /**
     * Runs the simulation. Sets simulation parameters from text fields. Displays trajectory plots       
     */
    private void runSimulation() {
                           
        String sequenceName = MainFunctions.mainDocument.getSequence();

        String Sequence = MainFunctions.mainDocument.getAccelerator().getSequences().toString();
        String ComboSequence = MainFunctions.mainDocument.getAccelerator().getComboSequences().toString();                                                                                         

        
        try { 
            if (Sequence.contains(sequenceName)) { 
                setParameters();
                newRun.runSimulation(MainFunctions.mainDocument.getModel().get(),MainFunctions.mainDocument.getAccelerator().getSequence(sequenceName));
            } else if (ComboSequence.contains(sequenceName)) {
                setParameters();
                newRun.runSimulation(MainFunctions.mainDocument.getModel().get(),MainFunctions.mainDocument.getAccelerator().getComboSequence(sequenceName));
            } else {
                setParameters();
                newRun.runSimulation(MainFunctions.mainDocument.getModel().get(),MainFunctions.mainDocument.getAccelerator().getSequence("LEBT"));
            }
        } catch (ModelException ex) {
            updateTimer.stop();          
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            updateTimer.stop();           
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);

        } catch (Exception ex) {
            updateTimer.stop();            
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
    
    //------------------------HELP METHODS------------------------------------
    
    /**
     * Gets parameters from simulation objects and assigns to text fields
     */
    private void getParameters(){
                
        initPos = newRun.getInitialBeamParameters();
        
        textField_x.setText(String.format("%.4f",initPos[0]*1e03));
        textField_xp.setText(String.format("%.4f",initPos[1]*1e03));
        textField_y.setText(String.format("%.4f",initPos[2]*1e03));
        textField_yp.setText(String.format("%.4f",initPos[3]*1e03));
               
        TwissX = newRun.getTwissX();
        TwissY = newRun.getTwissY();        
        textField_alphax.setText(String.format("%.3f",TwissX[0]));
        textField_betax.setText(String.format("%.3f",TwissX[1]));
        textField_emittx.setText(String.format("%.3f",TwissX[2]*1e06));        
        textField_alphay.setText(String.format("%.3f",TwissY[0]));
        textField_betay.setText(String.format("%.3f",TwissY[1]));
        textField_emitty.setText(String.format("%.3f",TwissY[2]*1e06)); 
        beamCurrent = newRun.getBeamCurrent();
        textField_bc.setText(Double.toString(beamCurrent));
        spaceChargeComp = newRun.getSpaceChargeCompensation();
        textField_scc.setText(Double.toString(spaceChargeComp));
    }
    
    /**
     * Sets simulation parameters from text fields.
     */
    private void setParameters(){
        
        try{
            newRun.setInitialBeamParameters(initPos[0],initPos[1],initPos[2],initPos[3]);        
            newRun.setBeamCurrent(beamCurrent);
            newRun.setBeamTwissX(TwissX[0],TwissX[1],TwissX[2]);
            newRun.setBeamTwissY(TwissY[0],TwissY[1],TwissY[2]);
            newRun.setSpaceChargeCompensation(spaceChargeComp);
            newRun.setElectrode(checkBox_electrode.isSelected());
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
        
        //get Sequence
        String sequenceName = MainFunctions.mainDocument.getSequence();

        String Sequence = MainFunctions.mainDocument.getAccelerator().getSequences().toString();
        String ComboSequence = MainFunctions.mainDocument.getAccelerator().getComboSequences().toString();
        
        double pos_ini = 0.0;
        
        if (Sequence.contains(sequenceName)) {
            pos_ini = MainFunctions.mainDocument.getAccelerator().getSequence(sequenceName).getPosition();
        } else if (ComboSequence.contains(sequenceName)) {
            pos_ini = MainFunctions.mainDocument.getAccelerator().getComboSequence(sequenceName).getPosition();
        }
        
        
        //retrieves data
        sigmaX = newRun.getSigmaX();
        sigmaY = newRun.getSigmaY();
        sigmaR = newRun.getSigmaR();
        positions = newRun.getPositions();
        posX = newRun.getPosX();
        posY = newRun.getPosY();
        posR = newRun.getPosR();
        posPhi = newRun.getPosPhi();
                
        for(int i = 0; i < positions.size() ; i++) {
            positions.set(i, (Double) positions.get(i) + pos_ini);
        }
        
    }
    
    /**
     * Retrieves and displays trajectory plots
     * @param newRun the simulation
     */
    private double[][] readVacuumChamber() throws FileNotFoundException{
        
        double[][] vacuumChamber = null;
        DataAdaptor readAdp = null;
        URL file;
        
        try {
            file = this.getClass().getResource("/vacuum_chamber/VacuumChamber.xml");
            readAdp = XmlDataAdaptor.adaptorForUrl(file,false);
            DataAdaptor blockheader = readAdp.childAdaptor("LEBTboundaries");
            DataAdaptor blockPoints = blockheader.childAdaptor("points");
            int num= blockPoints.intValue("numpoints");
            DataAdaptor blockPosition = blockheader.childAdaptor("position");
            double[] pos=blockPosition.doubleArray("data");
            DataAdaptor blockAperture = blockheader.childAdaptor("chamber");
            double[] aperture=blockAperture.doubleArray("data");
            vacuumChamber = new double[2][num];
            for (int i = 0; i < num ; i++) {
                vacuumChamber[0][i]= pos[i];
                vacuumChamber[1][i]= aperture[i];
            }
        } catch (XmlDataAdaptor.ParseException | XmlDataAdaptor.ResourceNotFoundException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }            
                       
        return vacuumChamber;
       
    }
    
    /**
     * Checks which coordinate system radio button is selected and displays plots accordingly
     */
    private void displayPlots(){
        
        addTrajectorySeriesToPlot();
        
        yAxis.setAutoRanging(true);
        
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
        
        //yAxis1.setAutoRanging(true);       
        
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
        
        long scale2 = getScaleAxis(posPhi,posR);
        
        for (int i = 0; i < posX.size() ; i++) {
            seriesR.getData().add(new XYChart.Data(positions.get(i), posR.get(i)));
            seriesPhi.getData().add(new XYChart.Data(positions.get(i), new Double(posPhi.get(i).toString())*scale2));
        }
        
        if (scale2 != 1){
            yAxis.setLabel("Offset (mm) \nAngle (" + Double.toString((double) 1/scale2) + " * π rad)");
            //System.out.print(scale2);
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
            //if (((double) Collections.max(sigmaR[0])*scale+(double) Collections.max(posR)) > 100){
            //    yAxis1.setUpperBound(((double) Collections.max(sigmaR[0])*scale+(double) Collections.max(posR))+10);
            //}
            
            yAxis1.setLowerBound(-90);
            yAxis1.setUpperBound(90);
            
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
       
        yAxis1.setAutoRanging(false);
        yAxis1.setLowerBound(-90);
        yAxis1.setUpperBound(90);
        //yAxis1.setAutoRanging(true);
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
    
    private long getScaleAxis(ArrayList<Double> posphi, ArrayList<Double> posr){
        
        int i = 1;
        
        double maxphi = Collections.max(posphi);
        double minphi = Collections.min(posphi);
        double maxr = Collections.max(posr);
        double minr = Collections.min(posr);
        double scalephi = Math.max(Math.abs(maxphi),Math.abs(minphi));
        double scaler = Math.max(Math.abs(maxr),Math.abs(minr));
  
        return Math.round(scalephi/scaler)==0 ? 1 : Math.round(scalephi/scaler);
    }
    
    private double scaleAndOffset(ArrayList<Double> sigma, ArrayList<Double> pos, double scale, int i){
        return ((double) sigma.get(i)*scale+(double) pos.get(i));
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
