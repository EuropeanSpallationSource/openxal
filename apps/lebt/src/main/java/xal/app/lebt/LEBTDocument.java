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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
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
        
    public Map<String,Double> valuesLEBT;
    
    public Map<String,Double> valuesISRC;
    
    public Map<String,Double> inputModel;
    
    // Save/restore parameters..
    private static final String LEBT_STATE = "ISrcandLEBT_State";
    private static final String ISRC_DATA = "IonSource_data";
    private static final String LEBT_DATA = "LEBT_data";
    private static final String MODEL_DATA = "Model_Input_data";
    private XmlDataAdaptor da;
    private DataAdaptor currentMeasAdaptor;

    /**
     *  Create a new empty LEBT Document
     */
    public LEBTDocument(Stage stage) {
        super(stage);      
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
     *@param stage
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
        DataAdaptor headerAdaptor =  da.createChild(LEBT_STATE);
        headerAdaptor.setValue("title", url.getFile());
        headerAdaptor.setValue("date", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));        
        DataAdaptor ionsourceAdaptor =  headerAdaptor.createChild(ISRC_DATA);
        valuesISRC.keySet().forEach(val -> {
            ionsourceAdaptor.createChild(val).setValue("value",valuesISRC.get(val));
        });
        DataAdaptor lebtAdaptor =  headerAdaptor.createChild(LEBT_DATA);
        valuesLEBT.keySet().forEach(val -> {
            lebtAdaptor.createChild(val).setValue("value",valuesLEBT.get(val));
        });
        DataAdaptor modelAdaptor =  headerAdaptor.createChild(MODEL_DATA);
        inputModel.keySet().forEach(val -> {
            modelAdaptor.createChild(val).setValue("value",inputModel.get(val));
        });
        
        da.writeToUrl( url );
        Logger.getLogger(LEBTDocument.class.getName()).log(Level.FINEST, "Saved document");
    }

    /**
     *  Reads the content of the document from the specified URL, and loads the information into the application.
     *
     * @param  url  The path to the XML file
     */
    public void loadDocument(URL url) {
        DataAdaptor readAdp = XmlDataAdaptor.adaptorForUrl( url, false );
        DataAdaptor headerAdaptor =  readAdp.childAdaptor(LEBT_STATE);
               
        DataAdaptor ionsourceAdaptor = headerAdaptor.childAdaptor(ISRC_DATA);
        valuesISRC.keySet().forEach(val -> {
            ionsourceAdaptor.childAdaptor(val).doubleValue("value");            
        });
        DataAdaptor lebtAdaptor = headerAdaptor.childAdaptor(LEBT_DATA);
        valuesLEBT.keySet().forEach(val -> {
            lebtAdaptor.childAdaptor(val).doubleValue("value");
        });
        DataAdaptor modelAdaptor = headerAdaptor.childAdaptor(MODEL_DATA);
        inputModel.keySet().forEach(val -> {
            modelAdaptor.childAdaptor(val).doubleValue("value");
        });
        
       
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

