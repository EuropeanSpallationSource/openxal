//
//  DefaultFolderAccessory.java
//  xal
//
//  Created by Thomas Pelaia on 1/24/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//
package xal.tools.apputils.files;

import java.io.*;
import java.beans.*;
import java.awt.Component;
import javax.swing.*;
import java.util.prefs.Preferences;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage the default folder for an application's documents
 */
public class DefaultFolderAccessory implements PropertyChangeListener {

    /**
     * default preference ID
     */
    private static final String DEFAULT_ID = "DEFAULT_FOLDER";

    /**
     * file tracker for the default folder
     */
    protected RecentFileTracker folderTracker;

    /**
     * optional subfolder name
     */
    protected String subfolderName;

    /**
     * the active file chooser
     */
    protected JFileChooser activeFileChooser;

    private static final Logger LOGGER = Logger.getLogger(DefaultFolderAccessory.class.getName());

    /**
     * Primary Constructor
     */
    public DefaultFolderAccessory(final Preferences prefs, final String preferenceID, final String subfolderName) {
        this.subfolderName = subfolderName;
        final String prefID = (preferenceID != null) ? preferenceID : DEFAULT_ID;
        folderTracker = new RecentFileTracker(1, prefs, prefID);
    }

    /**
     * Constructor
     */
    public DefaultFolderAccessory(final Preferences prefs, final String preferenceID) {
        this(prefs, preferenceID, null);
    }

    /**
     * Constructor
     */
    public DefaultFolderAccessory(final Preferences prefs) {
        this(prefs, DEFAULT_ID);
    }

    /**
     * Constructor
     */
    public DefaultFolderAccessory(final Class<?> preferenceNode, final String preferenceID, final String subfolderName) {
        this(xal.tools.apputils.Preferences.nodeForPackage(preferenceNode), preferenceID, subfolderName);
    }

    /**
     * Constructor
     */
    public DefaultFolderAccessory(final Class<?> preferenceNode, final String preferenceID) {
        this(xal.tools.apputils.Preferences.nodeForPackage(preferenceNode), preferenceID);
    }

    /**
     * Constructor
     */
    public DefaultFolderAccessory(final Class<?> preferenceNode) {
        this(xal.tools.apputils.Preferences.nodeForPackage(preferenceNode), DEFAULT_ID);
    }

    /**
     * Determine if the default folder has been specified.
     */
    public boolean defaultFolderSpecified() {
        return folderTracker.getMostRecentFile() != null;
    }

    /**
     * Get the default folder
     */
    public File getDefaultFolder() {
        final File recentFolder = folderTracker.getMostRecentFile();

        if (recentFolder != null && recentFolder.exists()) {
            if (subfolderName != null) {
                final File defaultFolder = new File(recentFolder, subfolderName);
                if (!defaultFolder.exists()) {
                    defaultFolder.mkdir();
                }
                return defaultFolder;
            } else {
                return recentFolder;
            }
        }

        return null;
    }

    /**
     * Get the default folder URL
     */
    public URL getDefaultFolderURL() {
        try {
            final File defaultFolder = getDefaultFolder();
            return (defaultFolder != null) ? defaultFolder.toURI().toURL() : null;
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Exception getting the default document URL.", exception);
        }
    }

    /**
     * register for events from the specified file chooser
     */
    public void applyTo(final JFileChooser fileChooser) {
        fileChooser.setAccessory(new AccessoryView().getComponent());
        applyDefaultFolder(fileChooser);
        fileChooser.addPropertyChangeListener(this);
    }

    /**
     * Apply default folder to file chooser
     */
    public void applyDefaultFolder(final JFileChooser fileChooser) {
        fileChooser.setCurrentDirectory(getDefaultFolder());
    }

    /**
     * Implement the propertyChange event handler for this listener
     */
    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        activeFileChooser = (JFileChooser) event.getSource();
    }

    /**
     * view for displaying the buttons
     */
    private class AccessoryView {

        private final Box view;

        /**
         * Constructor
         */
        public AccessoryView() {
            view = new Box(BoxLayout.Y_AXIS);
            makeContents();
        }

        /**
         * get the component
         */
        public JComponent getComponent() {
            return view;
        }

        /**
         * handle the action event
         */
        protected void handleToDefaultFolderAction() throws Exception {
            if (!defaultFolderSpecified()) {
                String message = "A default folder has not been specified.  Would you like to specify one now?\n";
                if (subfolderName != null) {
                    message += "Note that you will be specifying the parent folder of " + subfolderName + ".\n";
                    message += subfolderName + " under the selected folder will hold your files.";
                }
                final int confirm = JOptionPane.showConfirmDialog(view, message, "Specify Default Folder", JOptionPane.YES_NO_OPTION);

                try {
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (!showDefaultFolderSelector()) {
                            return;
                        }
                    } else {
                        return;
                    }
                } catch (Exception exception) {
                    LOGGER.log(Level.SEVERE, null, exception);
                }
            }

            applyDefaultFolder(activeFileChooser);
        }

        /**
         * Show the selector for selecting the default folder.
         *
         * @return true if the user selected a default folder and false if not.
         */
        protected boolean showDefaultFolderSelector() throws Exception {
            final JFileChooser selector = new JFileChooser(activeFileChooser.getCurrentDirectory());
            selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            final String title = (subfolderName == null) ? "Default Folder" : "Default Parent Folder of " + subfolderName;
            selector.setDialogTitle(title);
            final int status = selector.showDialog(view, "Make Default");

            if (status == JFileChooser.APPROVE_OPTION) {
                File defaultFolder = selector.getSelectedFile();
                if (defaultFolder != null) {
                    folderTracker.cacheURL(defaultFolder.toURI().toURL());
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        /**
         * make the box view
         */
        protected void makeContents() {
            view.add(makeDefaultFolderNavigationButton());

            view.add(Box.createVerticalGlue());
        }

        /**
         * Make the button for navigating to the default folder.
         */
        protected Component makeDefaultFolderNavigationButton() {
            final JButton goButton = new JButton("Default Folder");
            goButton.setToolTipText("Navigate to the default folder.");
            goButton.addActionListener(event -> {
                try {
                    handleToDefaultFolderAction();
                } catch (Exception exception) {
                    reportException(exception);
                }
            });

            return goButton;
        }

        /**
         * report exceptions
         */
        protected void reportException(final Exception exception) {
            final String message = exception.getMessage();
            JOptionPane.showMessageDialog(view, message, "Default Folder Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
