/*
 * AcceleratorApplication.java
 *
 * Created on May 22, 2003, 11:13 AM
 */
package xal.extension.application.smf;

import xal.extension.application.*;
import xal.tools.apputils.files.*;
import xal.smf.data.XMLDataManager;
import xal.smf.*;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AcceleratorApplication is the subclass of Application that is required for
 * accelerator based applications.
 *
 * @author tap
 */
public class AcceleratorApplication extends FrameApplication {

    private static final Logger LOGGER = Logger.getLogger(AcceleratorApplication.class.getName());

    /**
     * file chooser for selecting an accelerator file
     */
    private JFileChooser acceleratorFileChooser;

    /**
     * Creates a new instance of AcceleratorApplication
     */
    public AcceleratorApplication(ApplicationAdaptor adaptor, URL[] urls) {
        super(adaptor, urls);
    }

    /**
     * Initialize the Application and open the documents specified by the URL
     * array. Overrides the inherited version to perform initializations that
     * should occur prior to opening files.
     *
     * @param urls An array of document URLs to open.
     */
    @Override
    protected void setup(final URL[] urls) {
        acceleratorFileChooser = new JFileChooser();
        DefaultFolderAccessory defaultFolderAccessory = new DefaultFolderAccessory(this.getClass());
        defaultFolderAccessory.applyTo(acceleratorFileChooser);
        FileFilterFactory.applyFileFilters(acceleratorFileChooser, new String[]{"xal"});
        acceleratorFileChooser.setMultiSelectionEnabled(false);

        super.setup(urls);
    }

    /**
     * Make an application commander
     */
    @Override
    protected Commander makeCommander() {
        return new AcceleratorCommander(this);
    }

    /**
     * Get the file chooser used for opening accelerator input files.
     *
     * @return The file chooser used for opening accelerator input files.
     */
    public JFileChooser getAcceleratorFileChooser() {
        return acceleratorFileChooser;
    }

    /**
     * Get the accelerator application for this session
     *
     * @return the accelerator application for this session
     */
    public static AcceleratorApplication getAcceleratorApp() {
        return (AcceleratorApplication) Application.getApp();
    }

    /**
     * Handle the launching of the application by creating the application
     * instance and performing application initialization.
     *
     * @param adaptor The custom application adaptor.
     */
    public static void launch(final ApplicationAdaptor adaptor) {
        try {
            launch(adaptor, AbstractApplicationAdaptor.getDocURLs());
        } catch (NullPointerException e) {
            LOGGER.log(Level.WARNING, null, e);
            new AcceleratorApplication(adaptor, new URL[]{});
        }
    }

    /**
     * Handle the launching of the application by creating the application
     * instance and performing application initialization.
     *
     * @param adaptor The custom application adaptor.
     * @param urls The URLs of documents to open upon launching the application
     */
    public static void launch(final ApplicationAdaptor adaptor, final URL[] urls) {
        new AcceleratorApplication(adaptor, urls);
    }

    /**
     * Create and open a new empty document of the specified type.
     *
     * @param type the type of document to create.
     */
    @Override
    protected void newDocument(final String type) {
        final AcceleratorDocument document = (AcceleratorDocument) ((ApplicationAdaptor) getAdaptor()).generateEmptyDocument(type);

        // don't need to set an accelerator if the document already has one
        if (document.getAccelerator() == null) {
            final String acceleratorPath = Boolean.getBoolean("useDefaultAccelerator") ? XMLDataManager.defaultPath() : System.getProperty("useAccelerator");
            document.applySelectedAcceleratorWithDefaultPath(acceleratorPath);
            final Accelerator accelerator = document.getAccelerator();
            if (accelerator != null) {
                final String sequenceID = System.getProperty("useSequence");
                if (sequenceID != null && sequenceID.length() > 0) {
                    document.setSelectedSequence(accelerator.findSequence(sequenceID));
                }
            }
        }
        final String selectedAcceleratorPath = document.getAcceleratorFilePath();
        if (selectedAcceleratorPath != null && selectedAcceleratorPath.length() > 0) {
            acceleratorFileChooser.setSelectedFile(new File(selectedAcceleratorPath));
        }

        produceDocument(document);
    }
}
