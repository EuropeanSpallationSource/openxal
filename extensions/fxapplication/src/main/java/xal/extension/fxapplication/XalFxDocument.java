/*
 * Copyright (C) 2020 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.extension.fxapplication;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import eu.ess.jelog.Attachment;
import xal.extension.jelog.XALPostEntryDialog;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;

/**
 * The base class for custom documents for JavaFX applications.
 *
 * @author Yngve Levinsen <yngve.levinsen@ess.eu>
 */
public abstract class XalFxDocument {

    /**
     * wildcard file extension
     */
    protected String FILETYPE_DESCRIPTION = "Any XML File";
    protected String WILDCARD_FILE_EXTENSION = "*.xml";
    protected String DEFAULT_FILENAME = "DefaultFileName.xml";

    protected SimpleStringProperty sourceString;
    /**
     * The persistent storage URL for the document
     */
    protected URL source;
    protected boolean hasChanges;
    /**
     * The accelerator XML manager object
     */
    protected XMLDataManager acceleratorXMLManager;
    /**
     * The accelerator file in use
     */
    protected AcceleratorProperty accelerator;
    /**
     * The selected Sequence/ComboSequence
     */
    protected SimpleStringProperty sequence;
    /**
     * The selected Stage for eLog Posts
     */
    protected Stage mainStage;
    private final String HELP_WIKI_BASE;
    /**
     * PageID of the Confluence page or web page with the help about the
     * application
     */
    protected String HELP_PAGEID;
    /**
     * Test mode flag, disabled by default
     */
    protected boolean testMode = false;

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
     *
     * @param changeStatus Status to set whether this document has changes that
     * need saving.
     */
    public void setHasChanges(final boolean changeStatus) {
        // Add a * after the file name in title bar in case there are changes to the file
        if (!hasChanges) {
            sourceString.set(source + "*");
        }
        hasChanges = changeStatus;
    }

    /**
     * Indicates if there are changes that need saving.
     *
     * @return Status of whether this document has changes that need saving.
     */
    public boolean hasChanges() {
        return hasChanges;
    }

    public boolean sourceSetAndValid() {
        if (source == null) {
            return false;
        }
        return true;
    }

    public void setSource(File newSource) {
        // Checking that we are allowed to write to the folder where this file is from
        if (newSource.getParentFile().canWrite()) {
            try {
                Logger.getLogger(XalFxDocument.class.getName()).log(Level.FINER, "Changing document source {0}", newSource);
                source = newSource.toURI().toURL();
                sourceString.set(newSource.toString());
            } catch (MalformedURLException ex) {
                Logger.getLogger(XalFxDocument.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Logger.getLogger(XalFxDocument.class.getName()).log(Level.WARNING, "Not possible to write to file {0}", newSource);
        }
    }

    /**
     * Subclasses need to implement this method for saving the document to a
     * URL.
     *
     * @param url The File to which this document should be saved.
     */
    public abstract void saveDocumentAs(final URL url);

    /**
     * Subclasses need to implement this method for saving the document to a
     * URL.
     *
     * @param url The URL to which this document should be saved.
     */
    public abstract void loadDocument(final URL url);

    /**
     * Subclasses need to implement this method for creating a new document.
     *
     */
    public abstract void newDocument();

    /**
     * Save this document to its persistent storage source.
     */
    public void saveDocument() {
        if (sourceSetAndValid()) {
            Logger.getLogger(XalFxDocument.class.getName()).log(Level.FINER, "Saving document using source {0}", source);
            saveDocumentAs(source);
            sourceString.set(source.toString());
        }
    }

    /**
     * Method for creating an eLog Post.
     */
    public void eLogPost(String docType) {
        Logger.getLogger(XalFxDocument.class.getName()).log(Level.FINER, "New e-log entry");
        try {
            List<Attachment> attachments = new ArrayList<>();
            if (docType.equals("image")) {
                attachments.add(new Attachment("screenshot.png", mainStage.getScene().snapshot(null)));
                XALPostEntryDialog.post(attachments, "Studies");
            } else if (docType.equals("file") && sourceSetAndValid()) {
                attachments.add(new Attachment(new File(source.getPath())));
                XALPostEntryDialog.post(attachments, "Studies");
            } else if (docType.equals("file") && !sourceSetAndValid()) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Error");
                alert.setHeaderText("No data file specified!");
                alert.setContentText("Be sure to save a data file from this application \n before posting data to the logbook.");

                alert.showAndWait();
            }
        } catch (Exception ex) {
            Logger.getLogger(XalFxDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method for redirecting to the applications web page/internal page.
     */
    public void help() {
        if (HELP_PAGEID != null && HELP_PAGEID.length() > 1) {
            openUrl(HELP_WIKI_BASE + HELP_PAGEID);
        }

    }

    public void openUrl(String url) {
        Logger.getLogger(XalFxDocument.class.getName()).log(Level.FINER, "Opening web page {0}", url);
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

    public void setAccelerator(Accelerator accelerator) {
        this.accelerator.setAccelerator(accelerator);
    }

    public AcceleratorProperty getAcceleratorProperty() {
        return accelerator;
    }

    public XMLDataManager getAcceleratorXMLManager() {
        return acceleratorXMLManager;
    }

    public void setAcceleratorXMLManager(XMLDataManager xmlDataManager) {
        this.acceleratorXMLManager = xmlDataManager;
        this.accelerator.setAccelerator(xmlDataManager.getAccelerator());
    }

    public String getFiletypeDescription() {
        return FILETYPE_DESCRIPTION;
    }

    public String getWildcardFileExtension() {
        return WILDCARD_FILE_EXTENSION;
    }

    public String getDefaultFilename() {
        return DEFAULT_FILENAME;
    }
}
