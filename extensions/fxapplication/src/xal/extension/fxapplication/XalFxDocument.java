/*
 * XalFxDocument.java
 *
 * Created on January 31, 2018
 */

package xal.extension.fxapplication;

import java.net.URL;


/**
 * The base class for custom documents for JavaFX applications.
 *
 * @author  Yngve Levinsen <yngve.levinsen@esss.se>
 */
abstract public class XalFxDocument {
    /** wildcard file extension */
    static public final String WILDCARD_FILE_EXTENSION = "*.xml";
    /** The persistent storage URL for the document */
    protected URL source;
    protected boolean hasChanges;


    /**
     * Set the whether this document has changes.
     * @param changeStatus Status to set whether this document has changes that need saving.
     */
    public void setHasChanges( final boolean changeStatus ) {
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
        return false;
    }
    public void setSource(URL newSource) {
        // TODO check if newSource is valid..
        source = newSource;
    }

    /**
     * Subclasses need to implement this method for saving the document to a URL.
     * @param url The URL to which this document should be saved.
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
        saveDocumentAs( source );
    }
}
