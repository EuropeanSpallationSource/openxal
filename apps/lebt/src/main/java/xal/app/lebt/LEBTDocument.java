/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import xal.ca.Channel;
import xal.extension.fxapplication.XalFxDocument;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 *
 * @author nataliamilas
 */
public class LEBTDocument extends XalFxDocument {
    /**
     * String indicating which model to be used in the simulation
     */
    public SimpleStringProperty model;
    /**
     * A dictionary of the created datasets..
     */
    public Map<String, double[][]> dataSets;
    /**
     * For every measurement, store the channels read for this measurement..
     */
    public Map<String, List<Channel>> allPVrb;
    /**
     * For every measurement, store the channels written to for this measurement..
     */
    public Map<String, List<Channel>> allPVw;

    public SimpleIntegerProperty numberOfMeasurements;

    public double[][] currentMeasurement;

    // The current number of measurement points done
    public int nCombosDone;

    /**
     * To calculate constraints, we need to know the short hand variable name
     * for each variable..
     */
    public ObservableList<String> constraints;
   


    // Save/restore parameters..
    private static final String LEBT_STATE = "ISrc_andLEBT_State";
    private static final String ISRC_DATA = "IonSource_data";
    private static final String LEBT_DATA = "LEBT_data";
    private static final String MODEL_DATA = "Model_Input_data";
    private XmlDataAdaptor da;
    private DataAdaptor currentMeasAdaptor;

    /**
     *  Create a new empty ScanDocument1D
     */
    public LEBTDocument(Stage stage) {
        super(stage);
        dataSets = new HashMap<>();
        allPVrb = new HashMap<>();
        allPVw = new HashMap<>();       
        constraints = FXCollections.observableArrayList("", "", "", "");
        numberOfMeasurements = new SimpleIntegerProperty(0);
        model = new SimpleStringProperty();
        model.set("LIVE");
        DEFAULT_FILENAME="Lebt.xml";
        WILDCARD_FILE_EXTENSION = "*.xml";
        HELP_PAGEID="255603173";
    }

    /**
     *  Create a new document loaded from the URL file
     *
     *@param  url  The URL of the file to load into the new document.
     */
    public LEBTDocument(URL url, Stage stage) {
        this(stage);
        if (url == null) {
                return;
        }
        Logger.getLogger(LEBTDocument.class.getName()).log(Level.FINER, "Loading {0}", url);

        setSource(new File(url.getFile()));
        readScanDocument(url);

        //super class method - will show "Save" menu active
        if (url.getProtocol().equals("jar")) {
                return;
        }
        setHasChanges(false);
    }

    /**
     *  Reads the content of the document from the specified URL.
     *
     *@param  url  Description of the Parameter
     */
    public final void readScanDocument(URL url) {

        DataAdaptor readAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
        if (readAdaptor != null) {
            System.out.println("Will read document "+url.getFile());
        }

    }
    /**
     *  Save the ScannerDocument document to the specified URL.
     *
     *  @param  url  The file URL where the data should be saved
     */
    @Override
    public void saveDocumentAs(URL url) {
        Logger.getLogger(LEBTDocument.class.getName()).log(Level.FINER, "Saving document, filename {0}", url);
        da = XmlDataAdaptor.newEmptyDocumentAdaptor();
        DataAdaptor scannerAdaptor =  da.createChild(LEBT_STATE);
        currentMeasAdaptor = null;
        scannerAdaptor.setValue("title", url.getFile());
        scannerAdaptor.setValue("date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));

        da.writeToUrl( url );
        Logger.getLogger(LEBTDocument.class.getName()).log(Level.FINEST, "Saved document");
    }

    public void saveCurrentMeas(int nmeas) {
        if (currentMeasAdaptor==null) {
            currentMeasAdaptor=da.childAdaptor(LEBT_STATE).createChild(ISRC_DATA);
        }
        currentMeasAdaptor.createChild("step").setValue("values", currentMeasurement[nmeas]);
        da.writeToUrl( source );
    };


    /**
     *  Reads the content of the document from the specified URL, and loads the information into the application.
     *
     * @param  url  The path to the XML file
     */
    public void loadDocument(URL url) {
        DataAdaptor readAdp = XmlDataAdaptor.adaptorForUrl( url, false );
        DataAdaptor scannerAdaptor =  readAdp.childAdaptor(LEBT_STATE);

        //Accelerator acc = Model.getInstance().getAccelerator();


    }
    
    /**
     *  Get the current model
     * 
     */
    public SimpleStringProperty getModel() {
        return model;
    }

     /**
     *  Set the current model
     * @param model string indication to use with Live or Design models
     */
    public void setModel(SimpleStringProperty model) {
        this.model = model;
    }

}

