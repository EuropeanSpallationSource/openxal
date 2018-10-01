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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import xal.ca.BatchConnectionRequest;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.extension.jels.smf.impl.Chopper;
import xal.extension.jels.smf.impl.Doppler;
import xal.extension.jels.smf.impl.EMU;
import xal.extension.jels.smf.impl.ESSIonSourceCoil;
import xal.extension.jels.smf.impl.ESSIonSourceMFC;
import xal.extension.jels.smf.impl.ESSIonSourceMagnetron;
import xal.extension.jels.smf.impl.Iris;
import xal.extension.jels.smf.impl.NPM;
import xal.extension.jels.smf.impl.RepellerElectrode;
import xal.extension.jels.smf.impl.SpaceChargeCompensation;
import xal.model.ModelException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.CurrentMonitor;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.MagnetPowerSupply;
import xal.smf.impl.VDipoleCorr;
import xal.tools.data.DataAdaptor;
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

    //input beam parameters
    private double beamCurrent;   
    private double spaceChargeComp;
    private double spaceChargeCompElectrode;

    //Map the live machine values
    private HashMap<Channel,Object> displayValues;
    private HashMap<Channel,TextField> setValues;
    private BatchConnectionRequest request;
    private final ChannelMonitor monitor = new ChannelMonitor();        

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
    @FXML private Label label_Doppler;
    @FXML private CheckBox checkBox_electrode;
    @FXML private Circle electrodeStatus;
    @FXML private TextField textField_sccelectrode;
    @FXML private ToggleGroup toggleGroup_currentBI;
    @FXML private TextField textField_chopperDelay;
    @FXML private Label label_chopperDelayRB;
    @FXML private TextField textField_chopperLength;
    @FXML private Label label_chopperLengthRB;
    @FXML private TextField textField_N2flow;
    @FXML private Label label_N2flowRB;
    @FXML private ComboBox<InputParameters> comboBox_inputSimul;
    @FXML private Circle chopperStatus;    
    @FXML private RadioButton rb_CurrentMeasurement1;
    @FXML private RadioButton rb_CurrentMeasurement2;
    @FXML private Label label_CurrentMeasurement1;
    @FXML private Label label_CurrentMeasurement2;
    @FXML private AnchorPane mainPane;
    @FXML private TabPane mainTabPane;
    @FXML private Label label_transmission;   
    @FXML
    private Label label_CV1pol;
    @FXML
    private Label label_CH1pol;
    @FXML
    private Label label_CV2pol;
    @FXML
    private Label label_CH2pol;


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //Check if the accelerator file contains the LEBT and Ion Source sequences
        if(MainFunctions.mainDocument.getAccelerator().findSequence("LEBT")==null || MainFunctions.mainDocument.getAccelerator().findSequence("ISRC")==null){
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Accelerator file has no LEBT and/or Ion Source sequence.");
            alert.setContentText("Check inputs and try again");
            alert.showAndWait();
            Logger.getLogger(FXMLController.class.getName()).log(Level.FINER, "Accelerator file has no LEBT and/or Ion Source sequence.");
            System.exit(0);
        }
                       
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

        scale = 1;
        
        //Set textField formatting
        StringConverter<Double> formatter2d;
        StringConverter<Double> formatter3d;
        StringConverter<Double> formatter4d;
        StringConverter<Double> scientific3d;
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
                  return "0.000";
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
                  return "0.00";
               return String.format("%.2f",object);
            }
        };
        scientific3d = new StringConverter<Double>(){
            @Override
            public Double fromString(String string)
            {
               return Double.parseDouble(string);
            }

            @Override
            public String toString(Double object)
            {
               if (object == null)
                  return "0.000";
               return String.format("%2.3e",object);
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
        textField_sol1field.setTextFormatter(new TextFormatter<Double>(scientific3d));
        textField_sol2field.setTextFormatter(new TextFormatter<Double>(scientific3d));
        textField_CV1field.setTextFormatter(new TextFormatter<Double>(scientific3d));
        textField_CH1field.setTextFormatter(new TextFormatter<Double>(scientific3d));
        textField_CV2field.setTextFormatter(new TextFormatter<Double>(scientific3d));
        textField_CH2field.setTextFormatter(new TextFormatter<Double>(scientific3d));
        textField_irisAperture.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_irisX.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_irisY.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_chopperDelay.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_chopperLength.setTextFormatter(new TextFormatter<Double>(formatter3d));
        textField_N2flow.setTextFormatter(new TextFormatter<Double>(formatter3d));

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
                    textFieldSigmaScale.fireEvent(new RunEvent(true));
                }
            });

        //Map the channels to the label or textfield it belongs
        displayValues = new HashMap<Channel,Object>();
        setValues = new HashMap<Channel,TextField>();
        
        //Define Channel signals attached to textFields and displayFields
        setupChannelSignals();
        setupInitialConditions();
        
        yAxis1.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        
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
                
        MainFunctions.mainDocument.getSequenceProperty().addListener((obs, oldVal, newVal) ->{

            if(newVal != null && !newVal.matches("ISRC")){
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
                    MainFunctions.mainDocument.getAccelerator().getSequence(sequenceName).getAllNodesOfType("NPM").forEach(mon -> options.add(new InputParameters(mon)));
                    MainFunctions.mainDocument.getAccelerator().getSequence(sequenceName).getAllNodesOfType("EMU").forEach(mon -> options.add(new InputParameters(mon)));
                } else if (ComboSequence.contains(sequenceName)) {
                    newRun = new SimulationRunner(MainFunctions.mainDocument.getAccelerator().getComboSequence(sequenceName),MainFunctions.mainDocument.getModel().get());
                    options.add(new InputParameters(newRun.getProbe()));
                    MainFunctions.mainDocument.getAccelerator().getComboSequence(sequenceName).getAllNodesOfType("NPM").forEach(mon -> options.add(new InputParameters(mon)));
                    MainFunctions.mainDocument.getAccelerator().getComboSequence(sequenceName).getAllNodesOfType("EMU").forEach(mon -> options.add(new InputParameters(mon)));
                }

                comboBox_inputSimul.setItems(options);
                comboBox_inputSimul.getSelectionModel().select(0);
                
                //Initializes TextField 
                setConnectAndMonitor();
                initBIElements();   
                initDisplayFields();

                //assigning initial parameters
                getParameters();
                
                //Initializes Plots
                UpdateSimulation();           

                mainPane.addEventHandler(RunEvent.RUN_SIMULATION, new RunSimulationHandler(){
                    @Override
                    public void onRunEvent(Boolean param0) {
                        if(param0){UpdateSimulation();}
                    }                
                });   
                
                mainTabPane.setDisable(false);                
                setTempFldRB();
                

            }
                                               
        });
        
        MainFunctions.mainDocument.getAcceleratorProperty().addChangeListener((obs, oldVal, newVal) ->{
                
            //Check if the accelerator file contains the LEBT and Ion Source sequences
            if(MainFunctions.mainDocument.getAccelerator().findSequence("LEBT")==null || MainFunctions.mainDocument.getAccelerator().findSequence("ISRC")==null){
                 Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("Accelerator file has no LEBT and/or Ion Source sequence.");
                alert.setContentText("Check inputs and try again");
                alert.showAndWait();
                Logger.getLogger(FXMLController.class.getName()).log(Level.FINER, "Accelerator file has no LEBT and/or Ion Source sequence.");
                System.exit(0);
            }
            //Define Channel signals attached to textFields and displayFields
            setupChannelSignals();
            setupInitialConditions();
                            
            //Initializes TextField 
            setConnectAndMonitor();
            initBIElements();   
            initDisplayFields();    
            mainTabPane.setDisable(true);

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

        //Disable text field in case Model type chages to Design
        MainFunctions.mainDocument.getModel().addListener((ObservableValue<? extends String> obs, String oldVal, String newVal) ->{

            if(newVal.matches("DESIGN")){
                setValues.forEach((channel,textField)->{
                    textField.setDisable(true);
                });
                textField_sol1field.setDisable(false);
                textField_sol2field.setDisable(false);
                textField_CV1field.setDisable(false);
                textField_CV2field.setDisable(false);
                textField_CH1field.setDisable(false);
                textField_CH2field.setDisable(false);
                //MODEL_SYNC_TIMER.resume();
            } else if (newVal.matches("LIVE")){
                //set the magnets values as the readbacks from the channels
                Button applyButton = new Button("Apply Selection");
                Button cancelButton = new Button("Keep Old Fields");

                HBox hbox = new HBox(8);
                hbox.setPadding(new Insets(5, 10, 10, 5));
                hbox.setAlignment(Pos.CENTER_RIGHT);
                hbox.getChildren().addAll(applyButton,cancelButton);

                TableView<Magnet> table = new TableView<Magnet>();
                TableColumn<Magnet,Boolean> booleanColumn = new TableColumn<Magnet, Boolean>("Set?");
                TableColumn<Magnet,String> elementColumn = new TableColumn<Magnet, String>("Element");
                TableColumn<Magnet,String> newFieldColumn = new TableColumn<Magnet, String>("New Field");
                TableColumn<Magnet,String> oldFieldColumn = new TableColumn<Magnet, String>("Old Field");

                ObservableList<Magnet> inputMagnets = FXCollections.observableArrayList();

                inputMagnets.add(new Magnet("LEBT-010:BMD-Sol-01",textField_sol1field.getText(),label_sol1fieldRB.getText(),false));
                inputMagnets.add(new Magnet("LEBT-010:BMD-Sol-02",textField_sol2field.getText(),label_sol2fieldRB.getText(),false));
                inputMagnets.add(new Magnet("LEBT-010:BMD-CV-01:1",textField_CV1field.getText(),label_CV1fieldRB.getText(),false));
                inputMagnets.add(new Magnet("LEBT-010:BMD-CH-01:1",textField_CH1field.getText(),label_CH1fieldRB.getText(),false));
                inputMagnets.add(new Magnet("LEBT-010:BMD-CV-02:1",textField_CV2field.getText(),label_CV2fieldRB.getText(),false));
                inputMagnets.add(new Magnet("LEBT-010:BMD-CH-02:1",textField_CH2field.getText(),label_CH2fieldRB.getText(),false));

                elementColumn.setCellValueFactory(new PropertyValueFactory<>("magnetName"));
                newFieldColumn.setCellValueFactory(new PropertyValueFactory<>("newField"));
                oldFieldColumn.setCellValueFactory(new PropertyValueFactory<>("oldField"));
                booleanColumn.setCellValueFactory( new PropertyValueFactory<>( "selected" ));
                booleanColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

                table.setItems(inputMagnets);
                table.setEditable(true);
                table.getColumns().addAll(booleanColumn,elementColumn,newFieldColumn,oldFieldColumn);

                BorderPane secondaryLayout = new BorderPane();
                secondaryLayout.setTop(new Label("Choose magnets to restore new fields to machine:"));
                secondaryLayout.setCenter(table);
                secondaryLayout.setBottom(hbox);

                Scene secondScene = new Scene(secondaryLayout, 375, 258);
                Stage newWindow = new Stage();
                newWindow.setTitle("Magnets Settings");
                newWindow.setScene(secondScene);

                applyButton.setOnMouseClicked((MouseEvent event) -> {
                    inputMagnets.forEach(mag->{
                        AcceleratorNode node = MainFunctions.mainDocument.getAccelerator().getNode(mag.getMagnetName());
                        if(mag.selectedProperty().get()){
                            double val = Double.parseDouble(mag.getNewField());
                            try {
                                node.getChannel("fieldSet").putVal(val);
                            } catch (ConnectionException | PutException ex) {
                                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            setValues.get(node.getChannel("fieldSet")).setText(mag.getOldField());
                        }
                    });
                    newWindow.close();
                });

                cancelButton.setOnMouseClicked((MouseEvent event) -> {
                    textField_sol1field.setText(label_sol1fieldRB.getText());
                    textField_sol2field.setText(label_sol2fieldRB.getText());
                    textField_CV1field.setText(label_CV1fieldRB.getText());
                    textField_CH1field.setText(label_CH1fieldRB.getText());
                    textField_CV2field.setText(label_CV2fieldRB.getText());
                    textField_CH2field.setText(label_CH2fieldRB.getText());
                    newWindow.close();
                });

                setValues.forEach((channel,textField)->{
                    textField.setDisable(false);
                });

                newWindow.show();
                UpdateSimulation();
            }

        });      

        //Lock main Pane before a Sequence is chosen
        mainTabPane.setDisable(true);                
        
        //Initializes Plots
        addTrajectorySeriesToPlot();
        addEnvelopeSeriesToPlot();
        displayPlots();                  
                
        
        //set the magnets Readback display as change listener for changes -> run Model
        label_sol1fieldRB.textProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal.equals(oldVal)){
                UpdateSimulation();
            }
        });
        
        label_sol2fieldRB.textProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal.equals(oldVal)){
                UpdateSimulation();
            }
        });
        label_CV1fieldRB.textProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal.equals(oldVal)){
                UpdateSimulation();
            }
        });
        label_CV2fieldRB.textProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal.equals(oldVal)){
                UpdateSimulation();
            }
        });
        label_CH1fieldRB.textProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal.equals(oldVal)){
                UpdateSimulation();
            }
        });
        label_CH2fieldRB.textProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal.equals(oldVal)){
                UpdateSimulation();
            }
        });
                
    }
    
    private void setTempFldRB(){
        //Connect the Current RB values with the fileds
        
        label_CV1currentRB.textProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal.equals(oldVal)){
                label_CV1fieldRB.setText(String.format("%2.3e", 8.5833e-05*Double.parseDouble(label_CV1currentRB.getText())));
            }
        });
        label_CV2currentRB.textProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal.equals(oldVal)){
                label_CV2fieldRB.setText(String.format("%2.3e", 8.5833e-05*Double.parseDouble(label_CV2currentRB.getText())));
            }
        });
        label_CH1currentRB.textProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal.equals(oldVal)){
                label_CH1fieldRB.setText(String.format("%2.3e", 7.1667e-05*Double.parseDouble(label_CH1currentRB.getText())));
            }
        });
        label_CH2currentRB.textProperty().addListener((obs, oldVal, newVal) ->{
            if(!newVal.equals(oldVal)){
                label_CH2fieldRB.setText(String.format("%2.3e", 7.1667e-05*Double.parseDouble(label_CH2currentRB.getText())));
            }
        });                
    }
    
    //------------------------INIT METHODS -------------------------------------
    
    private void setConnectAndMonitor(){
        
        //Initializes TextField 
        monitor.disconnectAndClearAll();        
        //Creates a batch of channels to request when updating GUI
        HashMap<Channel,Object> inputChannels = new HashMap<>();    
        setValues.keySet().forEach(channel -> inputChannels.put(channel,setValues.get(channel)));        
        
        //Creates a batch of channels to request when updating GUI
        displayValues.keySet().forEach(channel -> inputChannels.put(channel,displayValues.get(channel))); 
        
        monitor.connectAndMonitor(inputChannels);
    }
    
    
    private void setupChannelSignals(){
        Accelerator accl = MainFunctions.mainDocument.getAccelerator();
        AcceleratorSeq sequence;
        ChannelFactory CHANNEL_FACTORY = ChannelFactory.defaultFactory();

        //Ion Source
        if(accl.findSequence("ISRC") != null){
            sequence = accl.getSequence("ISRC");            
            if(sequence.getNodesOfType("ISM").size()>0){
                AcceleratorNode Magnetron = sequence.getNodesOfType("ISM").get(0);
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
            } else {
                label_magnetronRB.setDisable(true);
                textField_magnetron.setDisable(true);
            }    
            
            if(sequence.getNodesOfType("ISMFC").size()>0){
                AcceleratorNode HighVoltage = sequence.getNodesOfType("ISMFC").get(0);
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
            } else {
                label_highVoltageRB.setDisable(true);
                textField_highVoltage.setDisable(true);
                label_H2flowRB.setDisable(true);
                textField_H2flow.setDisable(true);
            }          
             
            if(sequence.getNodesOfType("ISC").size()>0){
                AcceleratorNode Coil1 = sequence.getNodesOfType("ISC").get(0);
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
            } else {                
                label_coil2RB.setDisable(true);
                textField_coil2.setDisable(true);                
            }       
            
            if(sequence.getNodesOfType("ISC").size()>1){
                AcceleratorNode Coil2 = sequence.getNodesOfType("ISC").get(1);
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
            } else {                
                label_coil2RB.setDisable(true);
                textField_coil2.setDisable(true);                
            }    
            
            if(sequence.getNodesOfType("ISC").size()>2){
                AcceleratorNode Coil3 = sequence.getNodesOfType("ISC").get(2);
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
            } else {                
                label_coil3RB.setDisable(true);
                textField_coil3.setDisable(true);
            }    
        }    
            
        //LEBT
        if(accl.findSequence("LEBT") != null){
            sequence = accl.getSequence("LEBT");
        
            if(sequence.getNodesOfType("SFM").size()>1 || sequence.getNodesOfType("MFM").size()>1){
                AcceleratorNode Solenoid1 = sequence.getNodeWithId("LEBT-010:BMD-Sol-01");
                displayValues.put(Solenoid1.getChannel(MagnetPowerSupply.CURRENT_RB_HANDLE),label_sol1currentRB);
                displayValues.put(Solenoid1.getChannel("fieldRB"),label_sol1fieldRB);                              
                displayValues.put(Solenoid1.getChannel(MagnetPowerSupply.CURRENT_SET_HANDLE),label_sol1current);                 
                setValues.put(Solenoid1.getChannel(MagnetMainSupply.FIELD_SET_HANDLE),textField_sol1field);
                textField_sol1field.focusedProperty().addListener((obs, oldVal, newVal) ->{
                    if(!newVal){
                        if(MainFunctions.mainDocument.getModel().get().matches("LIVE")){
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
                        } else {
                            textField_sol1field.fireEvent(new RunEvent(true));
                        }
                    }
                });
                AcceleratorNode  Solenoid2 = sequence.getNodeWithId("LEBT-010:BMD-Sol-02");
                displayValues.put(Solenoid2.getChannel(MagnetPowerSupply.CURRENT_RB_HANDLE),label_sol2currentRB);
                displayValues.put(Solenoid2.getChannel(MagnetPowerSupply.CURRENT_SET_HANDLE),label_sol2current);
                displayValues.put(Solenoid2.getChannel("fieldRB"),label_sol2fieldRB);               
                setValues.put(Solenoid2.getChannel(MagnetMainSupply.FIELD_SET_HANDLE),textField_sol2field);
                textField_sol2field.focusedProperty().addListener((obs, oldVal, newVal) ->{
                    if(!newVal){ 
                        if(MainFunctions.mainDocument.getModel().get().matches("LIVE")){
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
                        } else {
                            textField_sol2field.fireEvent(new RunEvent(true));
                        }
                    }
                });
            } else {                
                label_sol1currentRB.setDisable(true);
                label_sol1fieldRB.setDisable(true);
                label_sol1current.setDisable(true);
                textField_sol1field.setDisable(true);
                label_sol2currentRB.setDisable(true);
                label_sol2fieldRB.setDisable(true);
                label_sol2current.setDisable(true);
                textField_sol2field.setDisable(true);
            }    
            
            if(sequence.getNodesOfType("DCV").size()>3){
                AcceleratorNode CV1 = sequence.getNodesOfType("DCV").get(0);
                displayValues.put(CV1.getChannel(MagnetPowerSupply.CURRENT_RB_HANDLE),label_CV1currentRB);
                displayValues.put(CV1.getChannel(MagnetPowerSupply.CURRENT_SET_HANDLE),label_CV1current);
                //displayValues.put(CV1.getChannel(VDipoleCorr.FIELD_RB_HANDLE),label_CV1fieldRB);                
                displayValues.put(CHANNEL_FACTORY.getChannel("LEBT-010:BMD-CV-01:PolR"),label_CV1pol);
                setValues.put(CV1.getChannel(MagnetMainSupply.FIELD_SET_HANDLE),textField_CV1field);
                textField_CV1field.focusedProperty().addListener((obs, oldVal, newVal) ->{
                    if(!newVal){ 
                        if(MainFunctions.mainDocument.getModel().get().matches("LIVE")){
                            try {
                                double val = Double.parseDouble(textField_CV1field.getText());
                                if(val<0.09 && val>-0.09){
                                    //CV1.getChannel("fieldSet").putVal(val);
                                    setCorrector("LEBT-010:BMD-CV-01",val);
                                } else {
                                    textField_CV1field.setText(Double.toString(CV1.getChannel(VDipoleCorr.FIELD_RB_HANDLE).getValDbl()));
                                }
                            } catch (ConnectionException | GetException ex) {
                                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            textField_CV1field.fireEvent(new RunEvent(true));
                        }
                    }
                });
                AcceleratorNode CV2 = sequence.getNodesOfType("DCV").get(3);
                displayValues.put(CV2.getChannel(MagnetPowerSupply.CURRENT_RB_HANDLE),label_CV2currentRB);
                displayValues.put(CV2.getChannel(MagnetPowerSupply.CURRENT_SET_HANDLE),label_CV2current);
                //displayValues.put(CV2.getChannel(VDipoleCorr.FIELD_RB_HANDLE),label_CV2fieldRB);               
                displayValues.put(CHANNEL_FACTORY.getChannel("LEBT-010:BMD-CV-02:PolR"),label_CV2pol);
                setValues.put(CV2.getChannel(MagnetMainSupply.FIELD_SET_HANDLE),textField_CV2field);
                textField_CV2field.focusedProperty().addListener((obs, oldVal, newVal) ->{
                    if(!newVal){ 
                        if(MainFunctions.mainDocument.getModel().get().matches("LIVE")){
                            try {
                                double val = Double.parseDouble(textField_CV2field.getText());
                                if(val<0.09 && val>-0.09){
                                    //CV2.getChannel("fieldSet").putVal(val);
                                    setCorrector("LEBT-010:BMD-CV-02",val);
                                } else {
                                    textField_CV2field.setText(Double.toString(CV2.getChannel(VDipoleCorr.FIELD_RB_HANDLE).getValDbl()));
                                }
                            } catch (ConnectionException | GetException ex) {
                                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            textField_CV2field.fireEvent(new RunEvent(true));
                        }
                    }
                });
            } else {                
                label_CV1currentRB.setDisable(true);
                label_CV1current.setDisable(true);
                label_CV1fieldRB.setDisable(true);
                textField_CV1field.setDisable(true);
                label_CV2currentRB.setDisable(true);
                label_CV2current.setDisable(true);
                label_CV2fieldRB.setDisable(true);
                textField_CV2field.setDisable(true);
            }    
            
            if(sequence.getNodesOfType("DCH").size()>3){
                AcceleratorNode CH1 = sequence.getNodesOfType("DCH").get(0);
                displayValues.put(CH1.getChannel(MagnetPowerSupply.CURRENT_RB_HANDLE),label_CH1currentRB);
                displayValues.put(CH1.getChannel(MagnetPowerSupply.CURRENT_SET_HANDLE),label_CH1current);
                //displayValues.put(CH1.getChannel(HDipoleCorr.FIELD_RB_HANDLE),label_CH1fieldRB);               
                displayValues.put(CHANNEL_FACTORY.getChannel("LEBT-010:BMD-CH-01:PolR"),label_CH1pol);
                setValues.put(CH1.getChannel(MagnetMainSupply.FIELD_SET_HANDLE),textField_CH1field);
                textField_CH1field.focusedProperty().addListener((obs, oldVal, newVal) ->{
                    if(!newVal){ 
                        if(MainFunctions.mainDocument.getModel().get().matches("LIVE")){
                            try {
                                double val = Double.parseDouble(textField_CH1field.getText());
                                if(val<0.12 && val>-0.12){
                                    //CH1.getChannel("fieldSet").putVal(val);
                                    setCorrector("LEBT-010:BMD-CH-01",val);
                                } else {
                                    textField_CH1field.setText(Double.toString(CH1.getChannel(HDipoleCorr.FIELD_RB_HANDLE).getValDbl()));
                                }
                            } catch (ConnectionException | GetException ex) {
                                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            textField_CH1field.fireEvent(new RunEvent(true));
                        }
                    }
                });
                AcceleratorNode CH2 = sequence.getNodesOfType("DCH").get(3);
                displayValues.put(CH2.getChannel(MagnetPowerSupply.CURRENT_RB_HANDLE),label_CH2currentRB);
                displayValues.put(CH2.getChannel(MagnetPowerSupply.CURRENT_SET_HANDLE),label_CH2current);
                //displayValues.put(CH2.getChannel(HDipoleCorr.FIELD_RB_HANDLE),label_CH2fieldRB);              
                displayValues.put(CHANNEL_FACTORY.getChannel("LEBT-010:BMD-CH-02:PolR"),label_CH2pol);
                setValues.put(CH2.getChannel(MagnetMainSupply.FIELD_SET_HANDLE),textField_CH2field);
                textField_CH2field.focusedProperty().addListener((obs, oldVal, newVal) ->{
                    if(!newVal){ 
                        if(MainFunctions.mainDocument.getModel().get().matches("LIVE")){
                            try {
                                double val = Double.parseDouble(textField_CH2field.getText());
                                if(val<0.12 && val>-0.12){
                                    //CH2.getChannel("fieldSet").putVal(val);
                                    setCorrector("LEBT-010:BMD-CH-02",val);
                                } else {
                                    textField_CH2field.setText(Double.toString(CH2.getChannel(HDipoleCorr.FIELD_RB_HANDLE).getValDbl()));
                                }
                            } catch (ConnectionException | GetException ex) {
                                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            textField_CH2field.fireEvent(new RunEvent(true));
                        }
                    }
                });
            } else {                
                label_CH1currentRB.setDisable(true);
                label_CH1current.setDisable(true);
                label_CH1fieldRB.setDisable(true);
                textField_CH1field.setDisable(true);
                label_CH2currentRB.setDisable(true);
                label_CH2current.setDisable(true);
                label_CH2fieldRB.setDisable(true);
                textField_CH2field.setDisable(true);
            }    

            if(sequence.getNodesOfType("IRIS").size()>0){
                AcceleratorNode IrisEquip = sequence.getNodesOfType("IRIS").get(0);
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
            } else {                
                label_irisApertureRB.setDisable(true);
                textField_irisAperture.setDisable(true);
                label_irisXRB.setDisable(true);
                textField_irisX.setDisable(true);
                label_irisYRB.setDisable(true);
                textField_irisY.setDisable(true);                
            }    

            if(sequence.getNodesOfType("CHP").size()>0){
                AcceleratorNode Chop = sequence.getNodesOfType("CHP").get(0);
                displayValues.put(Chop.getChannel(Chopper.DELAY_RB_HANDLE),label_chopperDelayRB);
                setValues.put(Chop.getChannel(Chopper.DELAY_SET_HANDLE),textField_chopperDelay);
                displayValues.put(Chop.getChannel(Chopper.LENGTH_RB_HANDLE),label_chopperLengthRB);
                setValues.put(Chop.getChannel(Chopper.LENGTH_SET_HANDLE),textField_chopperLength);
                displayValues.put(Chop.getChannel(Chopper.STATUS_RB_HANDLE),chopperStatus);                
                textField_chopperDelay.focusedProperty().addListener((obs, oldVal, newVal) ->{
                    if(!newVal){
                        try {
                            double val = Double.parseDouble(textField_chopperDelay.getText());
                            if(val > 0){
                                Chop.getChannel(Chopper.DELAY_SET_HANDLE).putVal(val);
                            } else {
                                textField_chopperDelay.setText(Double.toString(Chop.getChannel(Chopper.DELAY_RB_HANDLE).getValDbl()));
                            }
                        } catch (ConnectionException | PutException | GetException ex) {
                            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                textField_chopperLength.focusedProperty().addListener((obs, oldVal, newVal) ->{
                    if(!newVal){
                        try {
                            double val = Double.parseDouble(textField_chopperLength.getText());
                            if(val > 0){
                                Chop.getChannel(Chopper.LENGTH_SET_HANDLE).putVal(val);
                            } else {
                                textField_chopperLength.setText(Double.toString(Chop.getChannel(Chopper.LENGTH_RB_HANDLE).getValDbl()));
                            }
                        } catch (ConnectionException | PutException | GetException ex) {
                            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            } else {                
                label_chopperDelayRB.setDisable(true);
                textField_chopperDelay.setDisable(true);
                label_chopperLengthRB.setDisable(true);
                textField_chopperLength.setDisable(true); 
                chopperStatus.setFill(Color.GRAY);
            }    

            if(sequence.getNodesOfType("REP").size()>0){
                AcceleratorNode Electrode = sequence.getNodesOfType("REP").get(0);
                displayValues.put(Electrode.getChannel(RepellerElectrode.STATUS_RB_HANDLE),electrodeStatus);
                electrodeStatus.setFill(Color.GRAY);
            } 

            if(sequence.getNodesOfType("SCC").size()>0){
                AcceleratorNode n2FlowSCC = sequence.getNodesOfType("SCC").get(0);
                displayValues.put(n2FlowSCC.getChannel(SpaceChargeCompensation.N2FLOW_RB_HANDLE),label_N2flowRB);
                setValues.put(n2FlowSCC.getChannel(SpaceChargeCompensation.N2FLOW_SET_HANDLE),textField_N2flow);
                textField_N2flow.focusedProperty().addListener((obs, oldVal, newVal) ->{
                    if(!newVal){
                        try {
                            double val = Double.parseDouble(textField_N2flow.getText());
                            if(val > 0){
                                n2FlowSCC.getChannel(SpaceChargeCompensation.N2FLOW_SET_HANDLE).putVal(val);
                            } else {
                                textField_N2flow.setText(Double.toString(n2FlowSCC.getChannel(SpaceChargeCompensation.N2FLOW_SET_HANDLE).getValDbl()));
                            }
                        } catch (ConnectionException | PutException | GetException ex) {
                            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            } else {
                label_N2flowRB.setDisable(true);
                textField_N2flow.setDisable(true);
            }

            //define electrode properties
            checkBox_electrode.setTooltip(new Tooltip("Turns RFQ reppeler electrode On and OFF in the simulation only."));
            checkBox_electrode.selectedProperty().addListener((obs, oldVal, newVal) ->{
                if(newVal){
                    checkBox_electrode.setText("ON");
                    textField_sccelectrode.setDisable(false);
                    textField_sccelectrode.setText(Double.toString(spaceChargeCompElectrode));                    
                } else {
                    checkBox_electrode.setText("OFF");
                    textField_sccelectrode.setDisable(true);
                    textField_sccelectrode.setText(Double.toString(spaceChargeComp));
                }
                checkBox_electrode.fireEvent(new RunEvent(true));
            });
            
            if(sequence.getNodesOfType("DPL").size()>0){
                AcceleratorNode DopplerElem = sequence.getNodesOfType("DPL").get(0);
                displayValues.put(DopplerElem.getChannel(Doppler.FRACTION_H_R_HANDLE),label_Doppler);        
            } else {
                label_Doppler.setDisable(true);
            }

            //Disgnostics equipment
            if(accl.getAllNodesOfType("BCM").size()>0){
                List<AcceleratorNode> BCM = accl.getAllNodesOfType("BCM");                
                displayValues.put(BCM.get(0).getChannel(CurrentMonitor.I_AVG_HANDLE),label_CurrentMeasurement1);
                rb_CurrentMeasurement1.setText(BCM.get(0).toString());
                if(BCM.size()>1){
                    displayValues.put(BCM.get(1).getChannel(CurrentMonitor.I_AVG_HANDLE),label_CurrentMeasurement2);
                    rb_CurrentMeasurement2.setText(BCM.get(1).toString());
                } else {
                    label_CurrentMeasurement2.setDisable(true);
                    rb_CurrentMeasurement2.setDisable(true);
                }
            } else {
                label_CurrentMeasurement1.setDisable(true);
                rb_CurrentMeasurement1.setDisable(true);
                label_CurrentMeasurement2.setDisable(true);
                rb_CurrentMeasurement2.setDisable(true);
            } 
        }
    }    
    
    private void setupInitialConditions(){
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
                    textField_x.fireEvent(new RunEvent(true));
                }
            });

        textField_xp.focusedProperty().addListener((obs, oldVal, newVal) ->{
                if(!newVal){
                    try {
                        comboBox_inputSimul.getSelectionModel().getSelectedItem().setXP(Double.parseDouble(textField_xp.getText().trim())*1e-3);
                    } catch(NumberFormatException ex) {
                        textField_xp.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getXP()));
                    }
                    textField_xp.fireEvent(new RunEvent(true));
                }
            });

        textField_y.focusedProperty().addListener((obs, oldVal, newVal) ->{
                if(!newVal){
                    try {
                        comboBox_inputSimul.getSelectionModel().getSelectedItem().setY(Double.parseDouble(textField_y.getText().trim())*1e-3);
                    } catch(NumberFormatException ex) {
                        textField_y.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getY()));
                    }
                    textField_y.fireEvent(new RunEvent(true));
                }
            });

        textField_yp.focusedProperty().addListener((obs, oldVal, newVal) ->{
                if(!newVal){
                    try {
                        comboBox_inputSimul.getSelectionModel().getSelectedItem().setYP(Double.parseDouble(textField_yp.getText().trim())*1e-3);
                    } catch(NumberFormatException ex) {
                        textField_yp.setText(Double.toString(comboBox_inputSimul.getSelectionModel().getSelectedItem().getYP()));
                    }
                    textField_yp.fireEvent(new RunEvent(true));
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
                    textField_betax.fireEvent(new RunEvent(true));
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
                    textField_alphax.fireEvent(new RunEvent(true));
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
                    textField_emittx.fireEvent(new RunEvent(true));
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
                    textField_betay.fireEvent(new RunEvent(true));
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
                    textField_alphax.fireEvent(new RunEvent(true));
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
                    textField_emitty.fireEvent(new RunEvent(true));
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
                    textField_bc.fireEvent(new RunEvent(true));
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
                    textField_scc.fireEvent(new RunEvent(true));
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
                    textField_sccelectrode.fireEvent(new RunEvent(true));
                }
            });
    }
    
    /**
     * Initializes the values in the displayFields and TextFields
     */
    private void initDisplayFields(){                                                      
        
        setValues.keySet().forEach(channel ->{
            if (setValues.get(channel)!=null){
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
            }
        });          
        
        displayValues.keySet().forEach(channel ->{
            if (displayValues.get(channel) instanceof Label){
                if (channel.isConnected()){
                    try {
                        ((Label) displayValues.get(channel)).setText(String.format("%.3f",channel.getValDbl()));
                        ((Label) displayValues.get(channel)).setStyle("-fx-background-color: white;");
                        ((Label) displayValues.get(channel)).setDisable(false);
                    } catch (ConnectionException | GetException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    ((Label) displayValues.get(channel)).setStyle("-fx-background-color: magenta;");
                    ((Label) displayValues.get(channel)).setDisable(true);
                }
            } if (displayValues.get(channel) instanceof Circle){
                try {
                int val = (int) Math.round(channel.getValDbl());
                    switch (val) {
                        case 1:
                            ((Circle) displayValues.get(channel)).setFill(Color.GREEN);
                            break;
                        case 0:
                            ((Circle) displayValues.get(channel)).setFill(Color.RED);
                            break;
                        default:
                            ((Circle) displayValues.get(channel)).setFill(Color.GRAY);
                            break;
                    }
                } catch (ConnectionException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });                
        
    }    
    
    /**
     * Initializes diagnostics elements
     */
    private void initBIElements(){
        //Creates a batch of channels to request when updating GUI
        List<Channel> channels = new ArrayList<>();        
        //Add NPM channels
        List<NPM> npms = new ArrayList<>();
        List<EMU> emus = new ArrayList<>();
        npms = MainFunctions.mainDocument.getAccelerator().getAllNodesOfType("NPM");
        npms.forEach(mon->{
            channels.add(mon.getChannel(NPM.X_AVG_HANDLE));
            channels.add(mon.getChannel(NPM.Y_AVG_HANDLE));
            channels.add(mon.getChannel(NPM.X_P_AVG_HANDLE));
            channels.add(mon.getChannel(NPM.Y_P_AVG_HANDLE));
            channels.add(mon.getChannel(NPM.SIGMA_X_AVG_HANDLE));
            channels.add(mon.getChannel(NPM.SIGMA_Y_AVG_HANDLE));
            channels.add(mon.getChannel(NPM.ALPHA_X_TWISS_HANDLE));
            channels.add(mon.getChannel(NPM.ALPHA_Y_TWISS_HANDLE));
            channels.add(mon.getChannel(NPM.BETA_X_TWISS_HANDLE));
            channels.add(mon.getChannel(NPM.BETA_Y_TWISS_HANDLE));
        });
        emus = MainFunctions.mainDocument.getAccelerator().getAllNodesOfType("EMU");
        emus.forEach(mon->{
            channels.add(mon.getChannel(EMU.EMITT_X_HANDLE));
            channels.add(mon.getChannel(EMU.EMITT_Y_HANDLE));
            channels.add(mon.getChannel(EMU.ALPHA_X_TWISS_HANDLE));
            channels.add(mon.getChannel(EMU.ALPHA_Y_TWISS_HANDLE));
            channels.add(mon.getChannel(EMU.BETA_X_TWISS_HANDLE));
            channels.add(mon.getChannel(EMU.BETA_Y_TWISS_HANDLE));
        });
        request = new BatchConnectionRequest( channels );
        request.submitAndWait(5.0);
        
        comboBox_currentFC.selectedProperty().addListener((obs, oldVal, newVal) ->{
                if(comboBox_currentFC.isSelected()){
                    RadioButton currentBI = (RadioButton) toggleGroup_currentBI.getSelectedToggle();
                    String nodeBI = currentBI.getText();
                    Channel currentMonitor = MainFunctions.mainDocument.getAccelerator().getNode(nodeBI).getChannel(CurrentMonitor.I_AVG_HANDLE);
                    textField_bc.textProperty().bind(((Label)displayValues.get(currentMonitor)).textProperty());
                    textField_bc.setDisable(true);
                } else {
                    textField_bc.textProperty().unbind();
                    textField_bc.setDisable(false);
                }
            });

    }                   
    
    //------------------------HANDLE METHODS------------------------------------

    /**
     * Update the GUI values from the Live machine
     */
    private void updateGUI(){
      
        //Display Current if combo box is selected
        /**if (comboBox_currentFC.isSelected()){
            RadioButton currentBI = (RadioButton) toggleGroup_currentBI.getSelectedToggle();
            String nodeBI = currentBI.getText();
            if(nodeBI!=null){
                Channel currentMonitor = MainFunctions.mainDocument.getAccelerator().getNode(nodeBI).getChannel(CurrentMonitor.I_AVG_HANDLE);
                if (currentMonitor.isConnected()){
                    try {
                        double val = MainFunctions.mainDocument.getAccelerator().getNode(nodeBI).getChannel(CurrentMonitor.I_AVG_HANDLE).getValDbl();
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

        }**/

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

            npms.forEach((mon) -> {
                if(mon.getChannel(NPM.X_AVG_HANDLE).isConnected() && mon.getChannel(NPM.Y_AVG_HANDLE).isConnected()){
                    try {
                        seriesNPMpos[0].getData().add(new XYChart.Data(mon.getSDisplay(),mon.getXAvg()));
                        seriesNPMpos[1].getData().add(new XYChart.Data(mon.getSDisplay(),mon.getYAvg()));
                        Complex phi = new Complex(mon.getXAvg(),mon.getYAvg());
                        long scale2 = getScaleAxis(posPhi,posR);
                        seriesNPMposCyl[0].getData().add(new XYChart.Data(mon.getSDisplay(),phi.modulus()));
                        seriesNPMposCyl[1].getData().add(new XYChart.Data(mon.getSDisplay(),scale2*phi.phase()/Math.PI));
                    } catch (ConnectionException | GetException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });



            if(radioButtonOffsetOff.isSelected()){
                npms.forEach((NPM mon) -> {
                    if(mon.getChannel(NPM.SIGMA_X_AVG_HANDLE).isConnected() && mon.getChannel(NPM.SIGMA_Y_AVG_HANDLE).isConnected()){
                        try {
                            seriesNPMsigma[0].getData().add(new XYChart.Data(mon.getSDisplay(),scale*mon.getXSigmaAvg()*1.0e+3));
                            seriesNPMsigma[0].getData().add(new XYChart.Data(mon.getSDisplay(),scale*mon.getXSigmaAvg()*-1.0e+3));
                            seriesNPMsigma[1].getData().add(new XYChart.Data(mon.getSDisplay(),scale*mon.getYSigmaAvg()*1.0e+3));
                            seriesNPMsigma[1].getData().add(new XYChart.Data(mon.getSDisplay(),scale*mon.getYSigmaAvg()*-1.0e+3));
                            seriesNPMsigmaCyl.getData().add(new XYChart.Data(mon.getSDisplay(),scale*Math.max(mon.getXSigmaAvg(), mon.getYSigmaAvg())*1.0e+3));
                            seriesNPMsigmaCyl.getData().add(new XYChart.Data(mon.getSDisplay(),scale*Math.min(mon.getXSigmaAvg(), mon.getYSigmaAvg())*-1.0e+3));
                        } catch (ConnectionException | GetException ex) {
                            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
            if(radioButtonOffsetOn.isSelected()){
                npms.forEach((NPM mon) -> {
                    if (mon.getChannel(NPM.SIGMA_X_AVG_HANDLE).isConnected() && mon.getChannel(NPM.SIGMA_Y_AVG_HANDLE).isConnected()) {
                        try {
                            seriesNPMsigma[0].getData().add(new XYChart.Data(mon.getSDisplay(),scale*mon.getXSigmaAvg()*1.0e+3+mon.getXAvg()));
                            seriesNPMsigma[0].getData().add(new XYChart.Data(mon.getSDisplay(),scale*mon.getXSigmaAvg()*-1.0e+3+mon.getXAvg()));
                            seriesNPMsigma[1].getData().add(new XYChart.Data(mon.getSDisplay(),scale*mon.getYSigmaAvg()*1.0e+3+mon.getYAvg()));
                            seriesNPMsigma[1].getData().add(new XYChart.Data(mon.getSDisplay(),scale*mon.getYSigmaAvg()*-1.0e+3+mon.getYAvg()));
                            double posR1 = new Complex(mon.getXAvg(),mon.getYAvg()).modulus();
                            seriesNPMsigmaCyl.getData().add(new XYChart.Data(mon.getSDisplay(), scale*Math.max(mon.getXSigmaAvg(), mon.getYSigmaAvg())*1.0e+3 + posR1));
                            seriesNPMsigmaCyl.getData().add(new XYChart.Data(mon.getSDisplay(), scale*Math.max(mon.getXSigmaAvg(), mon.getYSigmaAvg())*-1.0e+3 + posR1));
                        }catch (ConnectionException | GetException ex) {
                            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
        }

    }
    
    private void UpdateSimulation(){

        setParameters();
        try {
            newRun.runSimulation();
        } catch (ModelException | InstantiationException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Display if successful run
        if(newRun.hasRun()) {
            retrieveData(newRun);            
            updateGUI();
            displayPlots();
        }
        
    }

    @FXML
    private void handleGetCurrentfromFC(ActionEvent event) {
        if(comboBox_currentFC.isSelected() && !comboBox_currentFC.isDisabled()){
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
        displayPlots();
    }

    @FXML
    private void offsetHandler(ActionEvent event) {

        if (newRun.hasRun()){
            addEnvelopeSeriesToPlot();
            npmSigHandler(new ActionEvent());
            label_transmission.setText(String.format("%.3f",newRun.getTransmission(radioButtonOffsetOn.isSelected())*100));
        } else {
            setLabels();
            setBounds();
        }
        displayPlots();  
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

                    //MODEL_SYNC_TIMER.resume();

                }

                return null;
            }
        ;

        };

        Thread calibrate = new Thread(task);
        calibrate.setDaemon(true); // thread will not prevent application shutdown
        //MODEL_SYNC_TIMER.suspend();
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
        spaceChargeCompElectrode = newRun.getSpaceChargeCompensationElectrode();
        textField_scc.setText(Double.toString(spaceChargeComp));
        textField_sccelectrode.setText(Double.toString(spaceChargeCompElectrode));
    }

    /**
     * Sets simulation parameters from text fields.
     */
    private void setParameters(){

        double[] initPos = {0.0,0.0,0.0,0.0};
        double[] TwissX = {0.0,0.0,0.0};
        double[] TwissY = {0.0,0.0,0.0};
        
        if(comboBox_inputSimul.getSelectionModel().getSelectedItem() != null){
            initPos = comboBox_inputSimul.getSelectionModel().getSelectedItem().getInit();
            TwissX = comboBox_inputSimul.getSelectionModel().getSelectedItem().getTwissX();
            TwissY = comboBox_inputSimul.getSelectionModel().getSelectedItem().getTwissY();
        } 

        try{
            newRun.setInitialBeamParameters(initPos[0],initPos[1],initPos[2],initPos[3]);
            newRun.setBeamCurrent(Double.parseDouble(textField_bc.getText()));
            newRun.setBeamTwissX(TwissX[0],TwissX[1],TwissX[2]);
            newRun.setBeamTwissY(TwissY[0],TwissY[1],TwissY[2]);
            newRun.setInitSimulPos(comboBox_inputSimul.getSelectionModel().getSelectedItem().getName());
            newRun.setSpaceChargeCompensation(spaceChargeComp,spaceChargeCompElectrode);
            newRun.setElectrode(checkBox_electrode.isSelected());
            newRun.setModelSync(MainFunctions.mainDocument.getModel().get());

            //Set fields: if model changes from Live to DESIGN the defaults fileds will be up-to-date
            newRun.setSolenoid1Field(Double.parseDouble(textField_sol1field.getText()));
            newRun.setSolenoid2Field(Double.parseDouble(textField_sol2field.getText()));
            newRun.setVsteerer1Field(Double.parseDouble(textField_CV1field.getText()));
            newRun.setVsteerer2Field(Double.parseDouble(textField_CV2field.getText()));
            newRun.setHsteerer1Field(Double.parseDouble(textField_CH1field.getText()));
            newRun.setHsteerer2Field(Double.parseDouble(textField_CH2field.getText()));

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
        
        label_transmission.setText(String.format("%.3f",newRun.getTransmission(radioButtonOffsetOn.isSelected())*100));
        
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
    
    private void setCorrector(String signalCorr, double val){
       Task<Void> task;
        task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
        
        
                double valOld = 0.0;
                int pol= 0;
                ChannelFactory CHANNEL_FACTORY = ChannelFactory.defaultFactory();
                try {
                    //Get the current value set in the corrector
                    valOld = CHANNEL_FACTORY.getChannel(signalCorr+":FldS").getValDbl();
                    switch (CHANNEL_FACTORY.getChannel(signalCorr+":PolR").getValInt()){
                        case 0:
                            pol = -1;
                        case 1:
                            pol = 1;
                    }
                    if(pol*valOld*val >= 0){
                        try {
                            CHANNEL_FACTORY.getChannel(signalCorr+":FldS").putVal(val);
                        } catch (PutException ex) {
                            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                         try {
                            CHANNEL_FACTORY.getChannel(signalCorr+":FldS").putVal(0.0);
                        } catch (PutException ex) {
                            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Thread.sleep(2000);
                        try {
                            if(val>0){
                                CHANNEL_FACTORY.getChannel(signalCorr+":PolPosCmd").putVal(1);
                            } else if (val<0){
                                CHANNEL_FACTORY.getChannel(signalCorr+":PolNegCmd").putVal(0);
                            }
                        } catch (PutException ex) {
                            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Thread.sleep(2000);
                        try {
                            CHANNEL_FACTORY.getChannel(signalCorr+":FldS").putVal(val);
                        } catch (PutException ex) {
                            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }                
                    }
                } catch (ConnectionException | GetException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                } 
                return null;
            };    

        };
       
        Thread correct = new Thread(task);
        correct.setDaemon(true); // thread will not prevent application shutdown
        correct.start();

    }

    public class Magnet{

        private String magnetName;
        private String newField;
        private String oldField;
        private final BooleanProperty selected = new SimpleBooleanProperty();

        public Magnet(String magnetName, String newField, String oldField, boolean selected) {
            this.magnetName = magnetName;
            this.newField = newField;
            this.oldField = oldField;
            this.selected.set(selected);
        }

        public BooleanProperty selectedProperty() { return selected; }

        public String getMagnetName() {
            return magnetName;
        }

        public void setMagnetName(String magnetName) {
            this.magnetName = magnetName;
        }

        public String getNewField() {
            return newField;
        }

        public void setNewField(String newField) {
            this.newField = newField;
        }

        public String getOldField() {
            return oldField;
        }

        public void setOldField(String oldField) {
            this.oldField = oldField;
        }

    }

}
