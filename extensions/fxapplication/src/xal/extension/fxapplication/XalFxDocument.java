/*
* XalFxDocument.java
*
* Created on January 31, 2018
*/

package xal.extension.fxapplication;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import xal.extension.jelog.PostEntryDialog;
import xal.smf.Accelerator;


/**
* The base class for custom documents for JavaFX applications.
*
* @author  Yngve Levinsen <yngve.levinsen@esss.se>
*/
abstract public class XalFxDocument {
    /** wildcard file extension */
    protected String FILETYPE_DESCRIPTION = "Any XML File";
    protected String WILDCARD_FILE_EXTENSION = "*.xml";
    protected String DEFAULT_FILENAME = "DefaultFileName.xml";
    protected SimpleStringProperty sourceString;
    /** The persistent storage URL for the document */
    protected URL source;
    protected boolean hasChanges;
    /** The accelerator file in use */
    protected AcceleratorProperty accelerator;
    /** The selected Sequence/ComboSequence */
    protected SimpleStringProperty sequence;
    /** The selected Stage for eLog Posts */
    protected Stage mainStage;
    private final String HELP_WIKI_BASE;
    /** PageID of the Confluence page or web page with the help about the application */
    protected String HELP_PAGEID;

    public XalFxDocument() {
        this.HELP_WIKI_BASE = "https://confluence.esss.lu.se/pages/viewpage.action?pageId=";
        this.accelerator = new AcceleratorProperty();
        this.sequence = new SimpleStringProperty();
    }

    public XalFxDocument(Stage stage) {
        this();
        this.mainStage = stage;
    }

    /**
     * Set the whether this document has changes.
     * @param changeStatus Status to set whether this document has changes that need saving.
     */
    public void setHasChanges( final boolean changeStatus ) {
        // Add a * after the file name in title bar in case there are changes to the file
        if (!hasChanges)
            sourceString.set(source+"*");
        hasChanges = changeStatus;
    }

    /**
    * Indicates if there are changes that need saving.
    * @return Status of whether this document has changes that need saving.
    */
    public boolean hasChanges() {
        return hasChanges;
    }

    public boolean sourceSetAndValid() {
        if (source==null)
            return false;
        return true;
    }

    public void setSource(File newSource) {
        // Checking that we are allowed to write to the folder where this file is from
        if (newSource.getParentFile().canWrite()) {
            try {
                Logger.getLogger(XalFxDocument.class.getName()).log(Level.FINER, "Changing document source {0}",newSource);
                source = newSource.toURI().toURL();
                sourceString.set(newSource.toString());
            } catch (MalformedURLException ex) {
                Logger.getLogger(XalFxDocument.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Logger.getLogger(XalFxDocument.class.getName()).log(Level.WARNING, "Not possible to write to file {0}",newSource);
        }
    }

    /**
     * Subclasses need to implement this method for saving the document to a URL.
     * @param url The File to which this document should be saved.
     */
    abstract public void saveDocumentAs( final URL url );

    /**
     * Subclasses need to implement this method for saving the document to a URL.
     * @param url The URL to which this document should be saved.
     */
    abstract public void loadDocument( final URL url );

    /**
     * Save this document to its persistent storage source.
     */
    public void saveDocument() {
        if ( sourceSetAndValid() ) {
            Logger.getLogger(XalFxDocument.class.getName()).log(Level.FINER, "Saving document using source {0}",source);
            saveDocumentAs( source );
        }
    }

    /**
    * Method for creating an eLog Post.
    */
    public void eLogPost(){
        Logger.getLogger(XalFxDocument.class.getName()).log(Level.FINER, "New e-log entry");
        try {
            WritableImage[] snapshots = new WritableImage[1];
            snapshots[0] = mainStage.getScene().snapshot(null);
            PostEntryDialog.post(snapshots, "Studies");
        } catch (Exception ex) {
            Logger.getLogger(XalFxDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
    * Method for redirecting to the applications web page/internal page.
    */
    public void help(){
        if (HELP_PAGEID.length()>1){
            openUrl(HELP_WIKI_BASE+HELP_PAGEID);
        }

    }

    public void openUrl(String url ) {
            Logger.getLogger(XalFxDocument.class.getName()).log(Level.FINER, "Opening web page {0}",url);
            HostServices hostServices = (HostServices) mainStage.getProperties().get("hostServices");
            hostServices.showDocument(url);
    }

    public String getSequence() {
        return sequence.get();
    }
    
    public SimpleStringProperty getSequenceProperty() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence.set(sequence);
    }

    public Stage getStage() {
        return mainStage;
    }

    public void setStage(Stage mainStage) {
        this.mainStage = mainStage;
    }
    
     public Accelerator getAccelerator() {
        return accelerator.getAccelerator();
    }


}

class AcceleratorProperty {

    private Accelerator accelerator;

    private final Set<ChangeListener> listeners = new HashSet<>();

    public void setAccelerator(Accelerator accelerator) {
        synchronized( listeners ){
          Accelerator old_accelerator = this.accelerator;
          this.accelerator = accelerator;
          listeners.forEach(listener -> listener.changed(null, old_accelerator, this.accelerator));
        }

    }

    public Accelerator getAccelerator() {
        synchronized( listeners ){
         return accelerator;
        }
    }

    public void addChangeListener(ChangeListener listener){
        synchronized( listeners ){
          listeners.add(listener);
        }
    };

    public void removeChangeListener(ChangeListener listener){
        synchronized( listeners ){
          listeners.remove(listener);
        }
    };

}
