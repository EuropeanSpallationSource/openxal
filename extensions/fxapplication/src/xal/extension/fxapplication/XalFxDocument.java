/*
 * XalFxDocument.java
 *
 * Created on January 31, 2018
 */

package xal.extension.fxapplication;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;


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
}
