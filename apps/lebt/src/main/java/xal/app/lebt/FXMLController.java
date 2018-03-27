package xal.app.lebt;

import com.sun.javafx.charts.Legend;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;
import xal.ca.BatchGetValueRequest;
import xal.ca.Channel;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.extension.jels.smf.impl.EMU;
import xal.extension.jels.smf.impl.ESSIonSourceCoil;
import xal.extension.jels.smf.impl.ESSIonSourceMFC;
import xal.extension.jels.smf.impl.ESSIonSourceMagnetron;
import xal.extension.jels.smf.impl.Iris;
import xal.extension.jels.smf.impl.NPM;
import xal.model.ModelException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.CurrentMonitor;
import xal.tools.data.DataAdaptor;
import xal.tools.dispatch.DispatchQueue;
import xal.tools.dispatch.DispatchTimer;
import xal.tools.math.Complex;
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
    private XYChart.Series[] seriesNPMpos;
    private XYChart.Series[] seriesNPMposCyl;
    private XYChart.Series[] seriesSigmaX;
    private XYChart.Series[] seriesSigmaY;
    private XYChart.Series[] seriesSigmaR;
    private XYChart.Series[] seriesNPMsigma;
    private XYChart.Series seriesNPMsigmaCyl;
    private XYChart.Series[] seriesSigmaOffsetX;
    private XYChart.Series[] seriesSigmaOffsetY;
    private XYChart.Series[] seriesSigmaOffsetR;
    private XYChart.Series[] seriesSurroundings;
    private Object range;
    
    //defining simulation
    private SimulationRunner newRun;
    private double[][] surroundings;
    private Accelerator accelerator;
    /** timer to synch the readbacks with the setpoints and also sync the model */
    private DispatchTimer MODEL_SYNC_TIMER;

    /** model sync period in milliseconds */
    private long _modelSyncPeriod;
    
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
    //private double[] initPos;
    private double beamCurrent;
    //private double[] TwissX;
    //private double[] TwissY;
    private double spaceChargeComp;
    private double spaceChargeCompElectrode;
    
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
    @FXML private CheckBox comboBox_currentFC;
    @FXML private CheckBox comboBox_posNPM;
    @FXML private CheckBox comboBox_sigmaNPM;
    @FXML private Label label_highVoltageRB;
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
    @FXML private TextField textField_sccelectrode;
    @FXML private ToggleGroup toggleGroup_currentBI;
    @FXML private CheckBox checkBox_chopper;
    @FXML private TextField textField_chopperDelay;
    @FXML private Label label_chopperDelayRB;
    @FXML private TextField textField_chopperLength;
    @FXML private Label label_chopperLengthRB;
    @FXML private TextField textField_N2flow;
    @FXML private Label label_N2flowRB;    
    @FXML private ComboBox<InputParameters> comboBox_inputSimul;

             
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // timer to synchronize readbacks with setpoints as well as the online model
        MODEL_SYNC_TIMER = DispatchTimer.getCoalescingInstance( DispatchQueue.createSerialQueue( "" ), getOnlineModelSynchronizer() );

        // set the default model sync period to 1 second
        _modelSyncPeriod = 1000;
        
        
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
        
        seriesNPMpos = new XYChart.Series[2];
        seriesNPMsigma = new XYChart.Series[2];
        seriesNPMposCyl = new XYChart.Series[2];
        seriesNPMsigmaCyl = new XYChart.Series();
        

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
            seriesNPMpos[i] = new XYChart.Series();
            seriesNPMsigma[i] = new XYChart.Series();
            seriesNPMposCyl[i] = new XYChart.Series();
        }                             
        
        seriesSigmaX[0].setName("σx");
        seriesSigmaY[0].setName("σy");
        seriesSigmaR[0].setName("σr");
        seriesSigmaOffsetR[0].setName("σr");
        seriesSigmaOffsetX[0].setName("σx");
        seriesSigmaOffsetY[0].setName("σy");
        seriesSigmaX[1].setName("σx");
        seriesSigmaY[1].setName("σy");
        seriesSigmaR[1].setName("σr");
        seriesSigmaOffsetR[1].setName("σr");
        seriesSigmaOffsetX[1].setName("σx");
        seriesSigmaOffsetY[1].setName("σy");
        
        seriesNPMpos[0].setName("NPM_x");
        seriesNPMpos[1].setName("NPM_y");        
        seriesNPMsigma[0].setName("NPM_σx");
        seriesNPMsigma[1].setName("NPM_σy"); 
        seriesNPMposCyl[0].setName("NPM_r");
        seriesNPMposCyl[1].setName("NPM_φ");        
        seriesNPMsigmaCyl.setName("NPM_σr");
        
       //Showing surroundings
        for (int i = 0; i < surroundings[0].length ; i++) {
            seriesSurroundings[0].getData().add(new XYChart.Data(surroundings[0][i], surroundings[1][i]*1000));
            seriesSurroundings[1].getData().add(new XYChart.Data(surroundings[0][i], -surroundings[1][i]*1000));
        }       
        
        //Add surroundings        
        plot1.setAnimated(false);
        plot2.setAnimated(false);              
        plot2.getData().add(seriesSurroundings[0]);
        plot2.getData().add(seriesSurroundings[1]);  
        plot1.getStylesheets().add(this.getClass().getResource("/styles/TrajectoryPlot.css").toExternalForm());
        plot2.getStylesheets().add(this.getClass().getResource("/styles/EnvelopePlot.css").toExternalForm());                       
        
         //remove surrounding legend
        Legend legend = (Legend)plot2.lookup(".chart-legend");
        legend.getItems().remove(0, 2);
        
        //xAxis.setUpperBound(xAxis1.getUpperBound());
        //xAxis.setLowerBound(xAxis1.getLowerBound());                
        
         
        scale = 1;
        //initPos = new double[4];
        //TwissX = new double[3];
        //TwissY = new double[3];
        
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
        textField_chopperDelay.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_chopperLength.setTextFormatter(new TextFormatter<Double>(formatter3d));
        
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
        textField_sccelectrode.setTextFormatter(new TextFormatter<Double>(formatter2d));    
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
        displayValues.put(Magnetron.getChannel(ESSIonSourceMagnetron.FORWD_PRW_RB_HANDLE),label_magnetronRB);
        setValues.put(Magnetron.getChannel(ESSIonSourceMagnetron.FORWD_PRW_S_HANDLE),textField_magnetron);
        textField_magnetron.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_magnetron.getText());
                    if(val>=0 && val<=1500){
                        Magnetron.getChannel(ESSIonSourceMagnetron.FORWD_PRW_S_HANDLE).putVal(val);
                    } else {
                        textField_magnetron.setText(Double.toString(Magnetron.getChannel(ESSIonSourceMagnetron.FORWD_PRW_S_HANDLE).getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode HighVoltage = sequence.getNodeWithId("MFC");
        displayValues.put(HighVoltage.getChannel(ESSIonSourceMFC.VOLTAGE_RB_HANDLE),label_highVoltageRB);
        setValues.put(HighVoltage.getChannel(ESSIonSourceMFC.VOLTAGE_SET_HANDLE),textField_highVoltage);
        textField_highVoltage.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_highVoltage.getText());
                    if(val>=70 && val<=80){
                        HighVoltage.getChannel(ESSIonSourceMFC.VOLTAGE_SET_HANDLE).putVal(val);
                    } else {
                        textField_highVoltage.setText(Double.toString(HighVoltage.getChannel(ESSIonSourceMFC.VOLTAGE_SET_HANDLE).getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        displayValues.put(HighVoltage.getChannel(ESSIonSourceMFC.H_2_FLOW_RB_HANDLE),label_H2flowRB);
        setValues.put(HighVoltage.getChannel(ESSIonSourceMFC.H_2_FLOW_S_HANDLE),textField_H2flow);
        textField_H2flow.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_H2flow.getText());
                    if(val>=0 && val <=5){
                        HighVoltage.getChannel(ESSIonSourceMFC.H_2_FLOW_S_HANDLE).putVal(val);
                    } else {
                        textField_H2flow.setText(Double.toString(HighVoltage.getChannel(ESSIonSourceMFC.H_2_FLOW_S_HANDLE).getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode Coil1 = sequence.getNodeWithId("COIL01");
        displayValues.put(Coil1.getChannel(ESSIonSourceCoil.I_HANDLE),label_coil1RB);
        setValues.put(Coil1.getChannel(ESSIonSourceCoil.I_SET_HANDLE),textField_coil1);
        textField_coil1.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_coil1.getText());
                    if(val>=0 && val <=300){
                        Coil1.getChannel(ESSIonSourceCoil.I_SET_HANDLE).putVal(val);
                    } else {
                        textField_coil1.setText(Double.toString(Coil1.getChannel(ESSIonSourceCoil.I_SET_HANDLE).getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode Coil2 = sequence.getNodeWithId("COIL02");
        displayValues.put(Coil2.getChannel(ESSIonSourceCoil.I_HANDLE),label_coil2RB);
        setValues.put(Coil2.getChannel(ESSIonSourceCoil.I_SET_HANDLE),textField_coil2);
        textField_coil2.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_coil2.getText());
                    if(val>=0 && val <=300){
                        Coil2.getChannel(ESSIonSourceCoil.I_SET_HANDLE).putVal(val);
                    } else {
                        textField_coil2.setText(Double.toString(Coil2.getChannel(ESSIonSourceCoil.I_SET_HANDLE).getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        AcceleratorNode Coil3 = sequence.getNodeWithId("COIL03");
        displayValues.put(Coil3.getChannel(ESSIonSourceCoil.I_HANDLE),label_coil3RB);
        setValues.put(Coil3.getChannel(ESSIonSourceCoil.I_SET_HANDLE),textField_coil3);
        textField_coil3.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_coil3.getText());
                    if(val>=0 && val <=300){
                        Coil3.getChannel(ESSIonSourceCoil.I_SET_HANDLE).putVal(val);
                    } else {
                        textField_coil3.setText(Double.toString(Coil3.getChannel(ESSIonSourceCoil.I_SET_HANDLE).getValDbl()));
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
        
        AcceleratorNode IrisEquip = sequence.getNodeWithId("IRIS");
        displayValues.put(IrisEquip.getChannel(Iris.APERTURE_RB_HANDLE),label_irisApertureRB);
        setValues.put(IrisEquip.getChannel(Iris.APERTURE_SET_HANDLE),textField_irisAperture);
        textField_irisAperture.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_irisAperture.getText());
                    if(val>=0){
                        IrisEquip.getChannel(Iris.APERTURE_SET_HANDLE).putVal(val);
                    } else {
                        textField_irisAperture.setText(Double.toString(IrisEquip.getChannel(Iris.APERTURE_RB_HANDLE).getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        
        displayValues.put(IrisEquip.getChannel(Iris.OFFSET_X_RB_HANDLE),label_irisXRB);
        setValues.put(IrisEquip.getChannel(Iris.OFFSET_X_SET_HANDLE),textField_irisX);
        textField_irisX.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                try {
                    double val = Double.parseDouble(textField_irisX.getText());
                    if(val>=-50 && val<=50){
                        IrisEquip.getChannel(Iris.APERTURE_SET_HANDLE).putVal(val);
                    } else {
                        textField_irisX.setText(Double.toString(IrisEquip.getChannel(Iris.OFFSET_X_RB_HANDLE).getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        displayValues.put(IrisEquip.getChannel(Iris.OFFSET_Y_RB_HANDLE),label_irisYRB);
        setValues.put(IrisEquip.getChannel(Iris.OFFSET_Y_SET_HANDLE),textField_irisY);
        textField_irisY.focusedProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal){
                 try {
                    double val = Double.parseDouble(textField_irisY.getText());
                    if(val>=-50 && val<=50){
                        IrisEquip.getChannel(Iris.OFFSET_Y_SET_HANDLE).putVal(val);
                    } else {
                        textField_irisY.setText(Double.toString(IrisEquip.getChannel(Iris.OFFSET_Y_RB_HANDLE).getValDbl()));
                    }
                } catch (ConnectionException | PutException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        
        AcceleratorNode Chopper = sequence.getNodeWithId("CHOPPER");
       // displayValues.put(Chopper.getChannel("volR"),label_chopperVoltageRB);
      //  setValues.put(Chopper.getChannel("volS"),textField_chopperVoltage);
      //  textField_chopperVoltage.focusedProperty().addListener((obs, oldVal, newVal) ->{
      //      if(!newVal){
      //          try {
      //              Chopper.getChannel("volS").putVal(Double.parseDouble(textField_chopperVoltage.getText()));
      //          } catch (ConnectionException | PutException ex) {
      //              Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
      //          }
      //      }
      //  });        

        //AcceleratorNode Electrode = sequence.getNodeWithId("ELECTRODE");
        //displayValues.put(Electrode.getChannel("V"),null);
        //displayValues.put(Electrode.getChannel("V"),null);
        
        //define electrode properties
        checkBox_chopper.setTooltip(new Tooltip("Turns Chopper On and OFF."));
        checkBox_chopper.selectedProperty().addListener((obs, oldVal, newVal) ->{ 
            if(newVal){
                checkBox_chopper.setText("ON");  
            } else {
                checkBox_chopper.setText("OFF");                        
            }
        });
        
        //define electrode properties
        checkBox_electrode.setTooltip(new Tooltip("Turns RFQ reppeler electrode On and OFF in the simulation only."));
        checkBox_electrode.selectedProperty().addListener((obs, oldVal, newVal) ->{ 
            if(newVal){
                checkBox_electrode.setText("ON");
                electrodeStatus.setFill(Color.GREEN); 
                textField_sccelectrode.setDisable(false);
                textField_sccelectrode.setText(Double.toString(spaceChargeCompElectrode));
            } else {
                checkBox_electrode.setText("OFF");
                electrodeStatus.setFill(Color.RED);
                textField_sccelectrode.setDisable(true);
                textField_sccelectrode.setText(Double.toString(spaceChargeComp));                
            }
        });
        
        //Disgnostics equipment
        AcceleratorNode FC = sequence.getNodeWithId("FC1");
        AcceleratorNode BCM = sequence.getNodeWithId("BCM1");
        AcceleratorNode Doppler = sequence.getNodeWithId("DOPPLER");
        displayValues.put(FC.getChannel(CurrentMonitor.I_AVG_HANDLE),label_FC);
        displayValues.put(BCM.getChannel(CurrentMonitor.I_AVG_HANDLE),label_BCM);
        displayValues.put(Doppler.getChannel(CurrentMonitor.I_AVG_HANDLE),label_Doppler);
        
        //Set scale text Field
        textFieldSigmaScale.setText(Double.toString(scale));
        
        //Create listener for intial parameters
        textField_x.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        comboBox_inputSimul.getSelectionModel().getSelectedItem().setX(Double.parseDouble(textField_x.getText().trim())*1e-3);        
                    } catch(NumberFormatException ex) {
                        textField_x.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getX()));
                    }    
                }
            });
        
        textField_xp.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {                        
                        comboBox_inputSimul.getSelectionModel().getSelectedItem().setXP(Double.parseDouble(textField_xp.getText().trim())*1e-3);
                    } catch(NumberFormatException ex) {
                        textField_xp.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getXP()));
                    }    
                }
            });
        
        textField_y.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        comboBox_inputSimul.getSelectionModel().getSelectedItem().setY(Double.parseDouble(textField_y.getText().trim())*1e-3);
                    } catch(NumberFormatException ex) {
                        textField_y.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getY()));
                    }    
                }
            });
        
        textField_yp.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        comboBox_inputSimul.getSelectionModel().getSelectedItem().setYP(Double.parseDouble(textField_yp.getText().trim())*1e-3);
                    } catch(NumberFormatException ex) {
                        textField_yp.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getYP()));
                    }    
                }
            });
        
        textField_betax.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_betax.getText().trim());
                        if(val>=0){                            
                            comboBox_inputSimul.getSelectionModel().getSelectedItem().setBETAX(val);
                        } else {
                            textField_betax.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getBETAX()));
                        }
                    } catch(NumberFormatException ex) {
                        textField_betax.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getBETAX()));
                    }    
                }
            });
        
        textField_alphax.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_alphax.getText().trim());                        
                        comboBox_inputSimul.getSelectionModel().getSelectedItem().setALPHAX(val);
                    } catch(NumberFormatException ex) {
                        textField_alphax.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getALPHAX()));
                    }    
                }
            });
        
        textField_emittx.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_emittx.getText().trim());
                        if(val>=0){
                            comboBox_inputSimul.getSelectionModel().getSelectedItem().setEMITTX(val*1e-6);
                        } else {
                            textField_emittx.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getEMITTX()*1e6));
                        }
                    } catch(NumberFormatException ex) {
                        textField_emittx.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getEMITTX()*1e6));
                    }    
                }
            });
        
        textField_betay.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_betay.getText().trim());
                        if(val>=0){
                            comboBox_inputSimul.getSelectionModel().getSelectedItem().setBETAY(val);
                        } else {
                            textField_betay.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getBETAY()));
                        }
                    } catch(NumberFormatException ex) {
                        textField_betay.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getBETAY()));
                    }    
                }
            });
        
        textField_alphay.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_alphay.getText().trim());                                                
                        comboBox_inputSimul.getSelectionModel().getSelectedItem().setALPHAY(val);
                    } catch(NumberFormatException ex) {
                        textField_alphay.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getALPHAY()));
                    }    
                }
            });
        
        textField_emitty.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_emitty.getText().trim());
                        if(val>=0){                            
                            comboBox_inputSimul.getSelectionModel().getSelectedItem().setEMITTY(val*1e-6);
                        } else {
                            textField_emitty.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getEMITTY()*1e6));
                        }
                    } catch(NumberFormatException ex) {
                        textField_emitty.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getEMITTY()*1e6));
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
                        if(val>=0 && val<=1.0){
                            spaceChargeComp=Double.parseDouble(textField_scc.getText().trim());
                        } else {
                            textField_scc.setText(Double.toString(spaceChargeComp));
                        }
                    } catch(NumberFormatException ex) {
                        textField_scc.setText(Double.toString(spaceChargeComp));
                    }                    
                }
            }); 
        
        textField_sccelectrode.focusedProperty().addListener((obs, oldVal, newVal) ->{                
                if(!newVal){
                    try {
                        double val = Double.parseDouble(textField_sccelectrode.getText().trim());
                        if(val>=0 && val<=1.0){
                            spaceChargeCompElectrode=Double.parseDouble(textField_sccelectrode.getText().trim());
                        } else {
                            textField_sccelectrode.setText(Double.toString(spaceChargeCompElectrode));
                        }
                    } catch(NumberFormatException ex) {
                        textField_sccelectrode.setText(Double.toString(spaceChargeCompElectrode));
                    }                    
                }
            });  
        
        yAxis1.setAutoRanging(true);
        yAxis.setAutoRanging(true);                
        
        //Creates a batch of channels to request when updating GUI
        List<Channel> channels = new ArrayList<>();
        displayValues.keySet().forEach(channel -> channels.add(channel));
        setValues.keySet().forEach(channel -> channels.add(channel));
        //Add NPM channels
        List<NPM> npms = new ArrayList<>();
        List<EMU> emus = new ArrayList<>();
        npms = MainFunctions.mainDocument.getAccelerator().getAllNodesOfType("NPM"); 
        npms.forEach(monitor->{
            channels.add(monitor.getChannel(NPM.X_AVG_HANDLE));
            channels.add(monitor.getChannel(NPM.Y_AVG_HANDLE));
            channels.add(monitor.getChannel(NPM.X_P_AVG_HANDLE));
            channels.add(monitor.getChannel(NPM.Y_P_AVG_HANDLE));
            channels.add(monitor.getChannel(NPM.SIGMA_X_AVG_HANDLE));
            channels.add(monitor.getChannel(NPM.SIGMA_Y_AVG_HANDLE));
            channels.add(monitor.getChannel(NPM.ALPHA_X_TWISS_HANDLE));
            channels.add(monitor.getChannel(NPM.ALPHA_Y_TWISS_HANDLE));
            channels.add(monitor.getChannel(NPM.BETA_X_TWISS_HANDLE));
            channels.add(monitor.getChannel(NPM.BETA_Y_TWISS_HANDLE));
        });
        emus = MainFunctions.mainDocument.getAccelerator().getAllNodesOfType("EMU"); 
        emus.forEach(monitor->{
            channels.add(monitor.getChannel(EMU.EMITT_X_HANDLE));
            channels.add(monitor.getChannel(EMU.EMITT_Y_HANDLE));
            channels.add(monitor.getChannel(EMU.ALPHA_X_TWISS_HANDLE));
            channels.add(monitor.getChannel(EMU.ALPHA_Y_TWISS_HANDLE));
            channels.add(monitor.getChannel(EMU.BETA_X_TWISS_HANDLE));
            channels.add(monitor.getChannel(EMU.BETA_Y_TWISS_HANDLE));
        });        
        request = new BatchGetValueRequest( channels );        
        request.submitAndWait(5.0);                
        
        //Initialize arrays                        
        sigmaX = new ArrayList[2];
        sigmaY = new ArrayList[2];
        sigmaR = new ArrayList[2];
        sigmaOffsetR = new ArrayList[2];
        sigmaOffsetX = new ArrayList[2];
        sigmaOffsetY = new ArrayList[2];
        posX = new ArrayList<>();
        posY = new ArrayList<>();
        posR = new ArrayList<>();
        posPhi = new ArrayList<>();
        positions = new ArrayList<>();
        
        for(int i = 0; i < sigmaR.length;i++){
            sigmaR[i] = new ArrayList<Double>();
            sigmaX[i] = new ArrayList<Double>();
            sigmaY[i] = new ArrayList<Double>();
            sigmaOffsetR[i] = new ArrayList<Double>();
            sigmaOffsetX[i] = new ArrayList<Double>();
            sigmaOffsetY[i] = new ArrayList<Double>();
        }                         
        
        MODEL_SYNC_TIMER.setEventHandler( getOnlineModelSynchronizer() );        
      
        MainFunctions.mainDocument.getSequenceProperty().addListener((obs, oldVal, newVal) ->{ 
            
            if(!newVal.equals(null) && !newVal.matches("ISRC")){
                //get Sequence
                String sequenceName = MainFunctions.mainDocument.getSequence();        
                String Sequence = MainFunctions.mainDocument.getAccelerator().getSequences().toString();
                String ComboSequence = MainFunctions.mainDocument.getAccelerator().getComboSequences().toString();
                
                //reset input values for Simulation
                ObservableList<InputParameters> options = FXCollections.observableArrayList();
                comboBox_inputSimul.getItems().clear();
                
                //initializing simulation                            
                if (Sequence.contains(sequenceName)) {
                    newRun = new SimulationRunner(MainFunctions.mainDocument.getAccelerator().getSequence(sequenceName),MainFunctions.mainDocument.getModel().get());                                                           
                    options.add(new InputParameters(newRun.getProbe()));
                    MainFunctions.mainDocument.getAccelerator().getSequence(sequenceName).getAllNodesOfType("NPM").forEach(monitor -> options.add(new InputParameters(monitor)));
                    MainFunctions.mainDocument.getAccelerator().getSequence(sequenceName).getAllNodesOfType("EMU").forEach(monitor -> options.add(new InputParameters(monitor)));
                } else if (ComboSequence.contains(sequenceName)) {
                    newRun = new SimulationRunner(MainFunctions.mainDocument.getAccelerator().getComboSequence(sequenceName),MainFunctions.mainDocument.getModel().get());
                    options.add(new InputParameters(newRun.getProbe()));
                    MainFunctions.mainDocument.getAccelerator().getComboSequence(sequenceName).getAllNodesOfType("NPM").forEach(monitor -> options.add(new InputParameters(monitor)));
                    MainFunctions.mainDocument.getAccelerator().getComboSequence(sequenceName).getAllNodesOfType("EMU").forEach(monitor -> options.add(new InputParameters(monitor)));
                }
                
                comboBox_inputSimul.setItems(options);
                comboBox_inputSimul.getSelectionModel().select(0);                
                
                //assigning initial parameters
                getParameters();                                
                
            }                        
            
            if(MODEL_SYNC_TIMER.isSuspended()){
                MODEL_SYNC_TIMER.resume();
            } else {
                MODEL_SYNC_TIMER.startNowWithInterval( _modelSyncPeriod, 0 );
            }
            
        }); 
        
        //Set input parameter for Simulation
        comboBox_inputSimul.setCellFactory(listview -> {
            return new ListCell<InputParameters>() {
                @Override
                public void updateItem(InputParameters item, boolean empty) {
                    super.updateItem(item, empty);
                    textProperty().unbind();
                    if (item != null) {
                        setText(item.getName());                       
                    } else {
                        setText(null);                                        
                    }
                }
            };
        }
        );
        
        comboBox_inputSimul.setButtonCell(new ListCell<InputParameters>() {  
            {
                itemProperty().addListener((obs, oldValue, newValue) -> update());
                emptyProperty().addListener((obs, oldValue, newValue) -> update());                
            }
            private void update() {
                if (isEmpty() || getItem() == null) {
                    setText(null);
                } else {
                    setText(getItem().getName());
                    getItem().updateValues();                    
                    textField_x.setText(String.format("%.4f",getItem().getX()*1e03));
                    textField_xp.setText(String.format("%.4f",getItem().getXP()*1e03));
                    textField_y.setText(String.format("%.4f",getItem().getY()*1e03));
                    textField_yp.setText(String.format("%.4f",getItem().getYP()*1e03));

                    textField_alphax.setText(String.format("%.3f",getItem().getALPHAX()));
                    textField_betax.setText(String.format("%.3f",getItem().getBETAX()));
                    textField_emittx.setText(String.format("%.3f",getItem().getEMITTX()*1e06));        
                    textField_alphay.setText(String.format("%.3f",getItem().getALPHAY()));
                    textField_betay.setText(String.format("%.3f",getItem().getBETAY()));
                    textField_emitty.setText(String.format("%.3f",getItem().getEMITTY()*1e06));   
                }
            }
        });
        
        //Initializes TextField
        initTextFields();
        
        //Initializes Plots
        addTrajectorySeriesToPlot();
        addEnvelopeSeriesToPlot();
        displayPlots();
            
        
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
            RadioButton currentBI = (RadioButton) toggleGroup_currentBI.getSelectedToggle();
            String nodeBI = null;
            if (currentBI.getText().equals("Faraday Cup")){
                nodeBI = "FC1";
            } else if (currentBI.getText().equals("Beam Current Monitor")){
                nodeBI = "BCM";
            } else if (currentBI.getText().equals("Doppler")){
                nodeBI = "DOPPLER";
            }
            if(nodeBI!=null){
                Channel currentMonitor = MainFunctions.mainDocument.getAccelerator().getNode(nodeBI).getChannel(CurrentMonitor.I_AVG_HANDLE);
                if (currentMonitor.isConnected()){
                    try {                
                        double val = 1000*MainFunctions.mainDocument.getAccelerator().getNode(nodeBI).getChannel(CurrentMonitor.I_AVG_HANDLE).getValDbl();
                        if(val>0){
                            textField_bc.setText(Double.toString(val));
                            beamCurrent = val;
                        } else {
                            textField_bc.setText(Double.toString(beamCurrent));
                            comboBox_currentFC.setSelected(false);
                        }
                    } catch (ConnectionException | GetException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    comboBox_currentFC.setSelected(false);
                    textField_bc.setDisable(false);
                }
            } else {
                comboBox_currentFC.setSelected(false);
                textField_bc.setDisable(false);
            }
            
        }
        
        //Add NPM data to the chart series               
        String sequenceName = MainFunctions.mainDocument.getSequence();        
        String Sequence = MainFunctions.mainDocument.getAccelerator().getSequences().toString();
        String ComboSequence = MainFunctions.mainDocument.getAccelerator().getComboSequences().toString();
                                
        if(sequenceName!=null){
            List<NPM> npms = new ArrayList<>();
            if (Sequence.contains(sequenceName)) {
                npms = MainFunctions.mainDocument.getAccelerator().getSequence(sequenceName).getAllNodesOfType("NPM");
            } else if (ComboSequence.contains(sequenceName)) {
                npms = MainFunctions.mainDocument.getAccelerator().getComboSequence(sequenceName).getAllNodesOfType("NPM");
            } 
            //Cartesian
            seriesNPMpos[0].getData().clear();
            seriesNPMpos[1].getData().clear();
            seriesNPMsigma[0].getData().clear();
            seriesNPMsigma[1].getData().clear();
            //Cylindrical
            seriesNPMposCyl[0].getData().clear();
            seriesNPMposCyl[1].getData().clear();
            seriesNPMsigmaCyl.getData().clear();
            
            npms.forEach((monitor) -> {
                if(monitor.getChannel(NPM.X_AVG_HANDLE).isConnected() && monitor.getChannel(NPM.Y_AVG_HANDLE).isConnected()){
                    try {                    
                        seriesNPMpos[0].getData().add(new XYChart.Data(monitor.getSDisplay(),monitor.getXAvg()));
                        seriesNPMpos[1].getData().add(new XYChart.Data(monitor.getSDisplay(),monitor.getYAvg()));
                        Complex phi = new Complex(monitor.getXAvg(),monitor.getYAvg()); 
                        long scale2 = getScaleAxis(posPhi,posR);
                        seriesNPMposCyl[0].getData().add(new XYChart.Data(monitor.getSDisplay(),phi.modulus()));
                        seriesNPMposCyl[1].getData().add(new XYChart.Data(monitor.getSDisplay(),scale2*phi.phase()/Math.PI));
                    } catch (ConnectionException | GetException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

              

            if(radioButtonOffsetOff.isSelected()){
                npms.forEach((monitor) -> {
                    if(monitor.getChannel(NPM.SIGMA_X_AVG_HANDLE).isConnected() && monitor.getChannel(NPM.SIGMA_Y_AVG_HANDLE).isConnected()){
                        try {
                            seriesNPMsigma[0].getData().add(new XYChart.Data(monitor.getSDisplay(),scale*monitor.getSigmaxAvgC()*1.0e+3));
                            seriesNPMsigma[0].getData().add(new XYChart.Data(monitor.getSDisplay(),scale*monitor.getSigmaxAvgC()*-1.0e+3));
                            seriesNPMsigma[1].getData().add(new XYChart.Data(monitor.getSDisplay(),scale*monitor.getSigmayAvgC()*1.0e+3));
                            seriesNPMsigma[1].getData().add(new XYChart.Data(monitor.getSDisplay(),scale*monitor.getSigmayAvgC()*-1.0e+3));
                            seriesNPMsigmaCyl.getData().add(new XYChart.Data(monitor.getSDisplay(),scale*Math.max(monitor.getSigmaxAvgC(), monitor.getSigmayAvgC())*1.0e+3));
                            seriesNPMsigmaCyl.getData().add(new XYChart.Data(monitor.getSDisplay(),scale*Math.min(monitor.getSigmaxAvgC(), monitor.getSigmayAvgC())*-1.0e+3));
                        } catch (ConnectionException | GetException ex) {
                            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });                       
            }
            if(radioButtonOffsetOn.isSelected()){
                npms.forEach((monitor) -> {
                    if(monitor.getChannel(NPM.SIGMA_X_AVG_HANDLE).isConnected() && monitor.getChannel(NPM.SIGMA_Y_AVG_HANDLE).isConnected()){
                        try {
                            seriesNPMsigma[0].getData().add(new XYChart.Data(monitor.getSDisplay(),scale*monitor.getSigmaxAvgC()*1.0e+3+monitor.getXAvg()));
                            seriesNPMsigma[0].getData().add(new XYChart.Data(monitor.getSDisplay(),scale*monitor.getSigmaxAvgC()*-1.0e+3+monitor.getXAvg()));
                            seriesNPMsigma[1].getData().add(new XYChart.Data(monitor.getSDisplay(),scale*monitor.getSigmayAvgC()*1.0e+3+monitor.getYAvg()));
                            seriesNPMsigma[1].getData().add(new XYChart.Data(monitor.getSDisplay(),scale*monitor.getSigmayAvgC()*-1.0e+3+monitor.getYAvg()));
                            double posR = new Complex(monitor.getXAvg(),monitor.getYAvg()).modulus();
                            seriesNPMsigmaCyl.getData().add(new XYChart.Data(monitor.getSDisplay(),scale*Math.max(monitor.getSigmaxAvgC(), monitor.getSigmayAvgC())*1.0e+3+posR));
                            seriesNPMsigmaCyl.getData().add(new XYChart.Data(monitor.getSDisplay(),scale*Math.max(monitor.getSigmaxAvgC(), monitor.getSigmayAvgC())*-1.0e+3+posR));
                        } catch (ConnectionException | GetException ex) {
                            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });                
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
    
    /** get the model sync period in milliseconds
     * @return the sync period for the model timer */
    public long getModelSyncPeriod() {
        return _modelSyncPeriod;
    }

    /** update the model sync period in milliseconds
     * @param period the sync period */
    public void setModelSyncPeriod( final long period ) {
        _modelSyncPeriod = period;
        MODEL_SYNC_TIMER.startNowWithInterval( _modelSyncPeriod, 0 );
    }       
    
    /** Get a runnable that syncs the online model */
    private Runnable getOnlineModelSynchronizer() {
        return () -> {                                               
            try {   
                setParameters();
                newRun.runSimulation();
            } catch (ModelException ex ) {
                MODEL_SYNC_TIMER.suspend();
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                MODEL_SYNC_TIMER.suspend();
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                MODEL_SYNC_TIMER.suspend();
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);

            }

            //Display if successful run
            if(newRun.hasRun()) {
                retrieveData(newRun);
                Platform.runLater(
                () -> {
                  updateGUI();  
                  displayPlots();
                });                                 
                //newRun.sethasRun(false);
            }
        };
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
            npmPosHandler(new ActionEvent());
            addEnvelopeSeriesToPlot();
            npmSigHandler(new ActionEvent());
        } else {
            setLabels();
            setBounds();
        }
    }

    @FXML
    private void offsetHandler(ActionEvent event) {
        
        if (newRun.hasRun()){
            addEnvelopeSeriesToPlot();
            npmSigHandler(new ActionEvent());
        } else {
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
    private void npmPosHandler(ActionEvent event) {
       
        if(radioButtonCart.isSelected()){
            if(comboBox_posNPM.isSelected()){
                plot1.getData().add(seriesNPMpos[0]);
                plot1.getData().add(seriesNPMpos[1]);
                Legend legend = (Legend)plot1.lookup(".chart-legend");
                legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #ff0000, white;");
                legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #006ddb, white;");   
                legend.getItems().get(2).getSymbol().setStyle("-fx-background-color: #ff9999, white;");
                legend.getItems().get(3).getSymbol().setStyle("-fx-background-color: #99ccff, white;");

            } else {
                plot1.getData().remove(seriesNPMpos[0]);
                plot1.getData().remove(seriesNPMpos[1]);
                Legend legend = (Legend)plot1.lookup(".chart-legend");
                legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #ff0000, white;");
                legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #006ddb, white;");      
            }
        }
        
        if(radioButtonCyl.isSelected()){
            if(comboBox_posNPM.isSelected()){
                plot1.getData().add(seriesNPMposCyl[0]);
                plot1.getData().add(seriesNPMposCyl[1]);
                Legend legend = (Legend)plot1.lookup(".chart-legend");
                legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #006400, white;");
                legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #DAA520, white;");    
                legend.getItems().get(2).getSymbol().setStyle("-fx-background-color: #99ff99, white;");
                legend.getItems().get(3).getSymbol().setStyle("-fx-background-color: #f2dca6, white;");

            } else {
                plot1.getData().remove(seriesNPMposCyl[0]);
                plot1.getData().remove(seriesNPMposCyl[1]);
                Legend legend = (Legend)plot1.lookup(".chart-legend");
                legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #006400, white;");
                legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #DAA520, white;");         
            }
        }
        
    }    
        
    @FXML
    private void npmSigHandler(ActionEvent event) {
        
        if(radioButtonCart.isSelected()){
            if(comboBox_sigmaNPM.isSelected()){
                plot2.getData().add(seriesNPMsigma[0]);
                plot2.getData().add(seriesNPMsigma[1]);
                Legend legend = (Legend)plot2.lookup(".chart-legend");
                legend.getItems().remove(0, 4);
                legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #ff0000, white;");
                legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #006ddb, white;");            
                legend.getItems().get(2).getSymbol().setStyle("-fx-background-color: #ff9999, white;");
                legend.getItems().get(3).getSymbol().setStyle("-fx-background-color: #99ccff, white;");
            } else {
                plot2.getData().remove(seriesNPMsigma[0]);
                plot2.getData().remove(seriesNPMsigma[1]);
                Legend legend = (Legend)plot2.lookup(".chart-legend");
                int leg_size = legend.getItems().size();
                legend.getItems().remove(0, leg_size-2);                    
                legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #ff0000, white;");
                legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #006ddb, white;"); 
            }
        }
        
        if(radioButtonCyl.isSelected()){
            if(comboBox_sigmaNPM.isSelected()){
                plot2.getData().add(seriesNPMsigmaCyl);
                Legend legend = (Legend)plot2.lookup(".chart-legend");
                legend.getItems().remove(0, 3);
                legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #006400, white;");
                legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #99ff99, white;");            
            } else {
                plot2.getData().remove(seriesNPMsigmaCyl);
                Legend legend = (Legend)plot2.lookup(".chart-legend");
                int leg_size = legend.getItems().size();
                legend.getItems().remove(0, leg_size-1);  
                legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #006400, white;");
                
            }
        }
        
    } 
    
     @FXML
    private void handleMatchParameters(ActionEvent event) {
                        
        Task<Void> task;
        task = new Task<Void>(){

            @Override
            protected Void call() throws Exception {
        
                //get Sequence
                String sequenceName = MainFunctions.mainDocument.getSequence();        
                String Sequence = MainFunctions.mainDocument.getAccelerator().getSequences().toString();
                String ComboSequence = MainFunctions.mainDocument.getAccelerator().getComboSequences().toString();               
                MatchingSolver doMatch = null;

                //initializing simulation   
                if(Sequence.contains(sequenceName)){  
                    AcceleratorSeq seq = MainFunctions.mainDocument.getAccelerator().getSequence(sequenceName);
                    doMatch = new MatchingSolver(comboBox_inputSimul.getItems().get(0),comboBox_inputSimul.getSelectionModel().getSelectedItem(), seq, 0.00001);            
                } else if(ComboSequence.contains(sequenceName)){
                    AcceleratorSeqCombo seq = MainFunctions.mainDocument.getAccelerator().getComboSequence(sequenceName);
                    doMatch = new MatchingSolver(comboBox_inputSimul.getItems().get(0),comboBox_inputSimul.getSelectionModel().getSelectedItem(), seq, 0.00001);                    
                }    

                if(doMatch !=null){
                    doMatch.initSimulation(beamCurrent,spaceChargeComp,spaceChargeCompElectrode,checkBox_electrode.isSelected());
                    doMatch.solve();
                    InputParameters finalResult = doMatch.newInputValues();
                    
                    
                    Platform.runLater(
                    () -> {
                                                                           
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Matching Dialog");
                        alert.setHeaderText("Matching result summary.");

                        // Create expandable Exception.           
                        String resultText;          
                        resultText = "Positions: \n"+
                                     "(x, xp) = "+String.format("%.3f",finalResult.getX()*1e3)+","+String.format("%.3f",finalResult.getXP()*1e3)+"\n"+
                                     "(y, yp) = "+String.format("%.3f",finalResult.getY()*1e3)+","+String.format("%.3f",finalResult.getYP()*1e3)+"\n"+
                                     "Twiss Parameters: \n"+
                                     "(alphax, betax) = "+String.format("%.3f",finalResult.getALPHAX())+","+String.format("%.3f",finalResult.getBETAX())+"\n"+
                                     "(alphay, betay) = "+String.format("%.3f",finalResult.getALPHAY())+","+String.format("%.3f",finalResult.getBETAY())+"\n"+
                                     "Initial emittances: \n"+
                                     "(emittx, emitty) = "+String.format("%.3f",finalResult.getEMITTX()*1e6)+","+String.format("%.3f",finalResult.getEMITTY()*1e6);

                        Label label = new Label("New Input parameters:");

                        TextArea textArea = new TextArea(resultText);            
                        textArea.setEditable(false);
                        textArea.setWrapText(true);

                        textArea.setMaxWidth(Double.MAX_VALUE);
                        textArea.setMaxHeight(Double.MAX_VALUE);            
                        GridPane.setVgrow(textArea, Priority.ALWAYS);
                        GridPane.setHgrow(textArea, Priority.ALWAYS);

                        GridPane resultContent = new GridPane();
                        resultContent.setMaxWidth(Double.MAX_VALUE);
                        resultContent.add(label, 0, 0);
                        resultContent.add(textArea, 0, 1);            

                        // Set expandable Exception into the dialog pane.
                        alert.getDialogPane().setExpandableContent(resultContent);

                        ButtonType buttonTypeUse = new ButtonType("Use matching result");
                        ButtonType buttonTypeCancel = new ButtonType("Cancel");

                        alert.getButtonTypes().setAll(buttonTypeUse, buttonTypeCancel);

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == buttonTypeUse){
                            comboBox_inputSimul.getItems().get(0).setX(finalResult.getX());
                            comboBox_inputSimul.getItems().get(0).setXP(finalResult.getXP());
                            comboBox_inputSimul.getItems().get(0).setY(finalResult.getY());
                            comboBox_inputSimul.getItems().get(0).setYP(finalResult.getYP());
                            comboBox_inputSimul.getItems().get(0).setALPHAX(finalResult.getALPHAX());
                            comboBox_inputSimul.getItems().get(0).setBETAX(finalResult.getBETAX());
                            comboBox_inputSimul.getItems().get(0).setEMITTX(finalResult.getEMITTX());
                            comboBox_inputSimul.getItems().get(0).setALPHAY(finalResult.getALPHAY());
                            comboBox_inputSimul.getItems().get(0).setBETAY(finalResult.getBETAY());
                            comboBox_inputSimul.getItems().get(0).setEMITTY(finalResult.getEMITTY());                
                            comboBox_inputSimul.getSelectionModel().select(0);
                        } 
                    });  
                    
                    MODEL_SYNC_TIMER.resume();
                    
                }
                
                return null;
            }
        ;

        };
                    
        Thread calibrate = new Thread(task);
        calibrate.setDaemon(true); // thread will not prevent application shutdown 
        MODEL_SYNC_TIMER.suspend();
        calibrate.start();
                               
                                
        
    }
    
    //------------------------HELP METHODS------------------------------------
    
    /**
     * Gets parameters from simulation objects and assigns to text fields
     */
    private void getParameters(){
        
        double[] initPos = new double[4];
        double[] TwissX = new double[3];
        double[] TwissY = new double[3];
        
        initPos = comboBox_inputSimul.getSelectionModel().getSelectedItem().getInit();
        TwissX = comboBox_inputSimul.getSelectionModel().getSelectedItem().getTwissX();
        TwissY = comboBox_inputSimul.getSelectionModel().getSelectedItem().getTwissY();
        
        textField_x.setText(String.format("%.4f",initPos[0]*1e03));
        textField_xp.setText(String.format("%.4f",initPos[1]*1e03));
        textField_y.setText(String.format("%.4f",initPos[2]*1e03));
        textField_yp.setText(String.format("%.4f",initPos[3]*1e03));
               
        textField_alphax.setText(String.format("%.3f",TwissX[0]));
        textField_betax.setText(String.format("%.3f",TwissX[1]));
        textField_emittx.setText(String.format("%.3f",TwissX[2]*1e06));        
        textField_alphay.setText(String.format("%.3f",TwissY[0]));
        textField_betay.setText(String.format("%.3f",TwissY[1]));
        textField_emitty.setText(String.format("%.3f",TwissY[2]*1e06));         
                
        beamCurrent = newRun.getBeamCurrent();
        textField_bc.setText(Double.toString(beamCurrent));
        spaceChargeComp = newRun.getSpaceChargeCompensation();
        spaceChargeCompElectrode = newRun.getSpaceChargeCompensationElectrode();;
        textField_scc.setText(Double.toString(spaceChargeComp));
        textField_sccelectrode.setText(Double.toString(spaceChargeCompElectrode));
    }
    
    /**
     * Sets simulation parameters from text fields.
     */
    private void setParameters(){                
        
        double[] initPos = new double[4];
        double[] TwissX = new double[3];
        double[] TwissY = new double[3];
        
        initPos = comboBox_inputSimul.getSelectionModel().getSelectedItem().getInit();
        TwissX = comboBox_inputSimul.getSelectionModel().getSelectedItem().getTwissX();
        TwissY = comboBox_inputSimul.getSelectionModel().getSelectedItem().getTwissY();
        
        try{            
            newRun.setInitialBeamParameters(initPos[0],initPos[1],initPos[2],initPos[3]);        
            newRun.setBeamCurrent(beamCurrent);
            newRun.setBeamTwissX(TwissX[0],TwissX[1],TwissX[2]);
            newRun.setBeamTwissY(TwissY[0],TwissY[1],TwissY[2]);            
            newRun.setInitSimulPos(comboBox_inputSimul.getSelectionModel().getSelectedItem().getName());                
            newRun.setSpaceChargeCompensation(spaceChargeComp,spaceChargeCompElectrode);
            newRun.setElectrode(checkBox_electrode.isSelected());
            newRun.setModelSync(MainFunctions.mainDocument.getModel().get());
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
                
        yAxis.setLabel("Trajectory (mm)");                
        
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
                        
        }
        else if (radioButtonOffsetOn.isSelected()){           
            for (int i = 0; i < sigmaX[0].size(); i++){               
                seriesSigmaOffsetX[0].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaX[0],posX,scale,i)));
                seriesSigmaOffsetY[0].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaY[0],posY,scale,i)));
                seriesSigmaOffsetX[1].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaX[1],posX,scale,i)));
                seriesSigmaOffsetY[1].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaY[1],posY,scale,i)));
            }
            
        }                                          
        
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
            
            //seriesSigmaR[0].getNode().setStyle("-fx-stroke: #006400;");
            //seriesSigmaR[1].getNode().setStyle("-fx-stroke: #006400;");
        }
        else if (radioButtonOffsetOn.isSelected()){
            
            for (int i = 0; i < sigmaR[0].size(); i++){
                seriesSigmaOffsetR[0].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaR[0],posR,scale,i)));
                seriesSigmaOffsetR[1].getData().add(new XYChart.Data(positions.get(i), scaleAndOffset(sigmaR[1],posR,scale,i)));
            }                      
            
            yAxis1.setLowerBound(-90);
            yAxis1.setUpperBound(90);
            
        }                
               
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
        
        if(scalephi>scaler){
            return Math.round(scaler/scalephi)==0 ? 1 : Math.round(scaler/scalephi);
        } else {
            return Math.round(scalephi/scaler)==0 ? 1 : Math.round(scalephi/scaler);
        }
                
    }
    
    private double scaleAndOffset(ArrayList<Double> sigma, ArrayList<Double> pos, double scale, int i){
        return ((double) sigma.get(i)*scale+(double) pos.get(i));
    }
    
//-------------------------PLOT FUNCTIONS---------------------------------------
    
    /**
     * Removes the series from plot and adds the relevant series.
     */
    private void addTrajectorySeriesToPlot(){
        
        plot1.getData().removeAll(seriesX,seriesY,seriesR,seriesPhi,seriesNPMpos[0],seriesNPMpos[1],seriesNPMposCyl[0],seriesNPMposCyl[1]);        
        
        if(radioButtonCart.isSelected()){
            plot1.getData().add(seriesX);       
            plot1.getData().add(seriesY);
            //Set Style
            plot1.getStylesheets().remove(0);
            plot1.getStylesheets().add(this.getClass().getResource("/styles/TrajectoryPlot.css").toExternalForm());
            //set colors
            Legend legend = (Legend)plot1.lookup(".chart-legend");
            legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #ff0000, white;");
            legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #006ddb, white;");            
        }
        else if (radioButtonCyl.isSelected()){
            plot1.getData().add(seriesR);       
            plot1.getData().add(seriesPhi);
            //set Style
            plot1.getStylesheets().remove(0);
            plot1.getStylesheets().add(this.getClass().getResource("/styles/TrajectoryPlotCyl.css").toExternalForm());
            //set colors
            Legend legend = (Legend)plot1.lookup(".chart-legend");
            legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #006400, white;");
            legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #DAA520, white;");                    
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
            seriesSigmaOffsetR[0],seriesSigmaOffsetR[1],
            seriesNPMsigma[0],seriesNPMsigma[1],seriesNPMsigmaCyl);                 
        
        if(radioButtonCart.isSelected()){
            
            if(radioButtonOffsetOn.isSelected()){
                for(int i = 0; i < seriesSigmaX.length; i++){
                    plot2.getData().add(seriesSigmaOffsetX[i]);       
                    plot2.getData().add(seriesSigmaOffsetY[i]);                    
                }                
            }
            else if (radioButtonOffsetOff.isSelected()){
                for(int i = 0; i < seriesSigmaX.length; i++){
                    plot2.getData().add(seriesSigmaX[i]);       
                    plot2.getData().add(seriesSigmaY[i]);  
                }                
            }
            
            //set Style
            plot2.getStylesheets().remove(0);
            plot2.getStylesheets().add(this.getClass().getResource("/styles/EnvelopePlot.css").toExternalForm());
            //set legend colors
            Legend legend = (Legend)plot2.lookup(".chart-legend");
            legend.getItems().remove(0, 4);
            legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #ff0000, white;");
            legend.getItems().get(1).getSymbol().setStyle("-fx-background-color: #006ddb, white;");                    

        }
        else if (radioButtonCyl.isSelected()){
            if(radioButtonOffsetOn.isSelected()){
                for(int i = 0; i < seriesSigmaX.length; i++){
                    plot2.getData().add(seriesSigmaOffsetR[i]);       
                }
            }
            else if (radioButtonOffsetOff.isSelected()){
                for(int i = 0; i < seriesSigmaX.length; i++){
                    plot2.getData().add(seriesSigmaR[i]);       
                }
            }
            //set Style
            plot2.getStylesheets().remove(0);
            plot2.getStylesheets().add(this.getClass().getResource("/styles/EnvelopePlotCyl.css").toExternalForm());
            //set legend colors
            Legend legend = (Legend)plot2.lookup(".chart-legend");
            legend.getItems().remove(0, 3);
            legend.getItems().get(0).getSymbol().setStyle("-fx-background-color: #006400, white;");
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
