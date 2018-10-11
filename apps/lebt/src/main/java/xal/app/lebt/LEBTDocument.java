/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import java.net.URL;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import xal.extension.fxapplication.XalFxDocument;

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
        
    }

    /**
     *  Reads the content of the document from the specified URL.
     *
     *@param  url  Description of the Parameter
     */
    public final void readScanDocument(URL url) {

    }
    /**
     *  Save the ScannerDocument document to the specified URL.
     *
     *  @param  url  The file URL where the data should be saved
     */
    @Override
    public void saveDocumentAs(URL url) {
       
    }

    /**
     *  Reads the content of the document from the specified URL, and loads the information into the application.
     *
     * @param  url  The path to the XML file
     */
    public void loadDocument(URL url) {
        
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
    public void setModel(String model) {
        this.model.set(model);
    }

}

