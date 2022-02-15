//
//  XalInternalWindow.java
//  xal
//
//  Created by Thomas Pelaia on 3/29/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//
package xal.extension.application;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import xal.tools.apputils.ImageCaptureManager;

/**
 * The base class for custom windows that are the main windows for documents.
 * Subclasses need to define their custom views.
 *
 * @author t6p
 */
public abstract class XalInternalWindow extends JInternalFrame implements XalDocumentView, XalInternalDocumentListener {

    /**
     * serial version ID required for Serializable
     */
    static final long serialVersionUID = 1L;

    // public static constants for confirmation dialogs
    public static final int YES_OPTION = JOptionPane.YES_OPTION;
    public static final int NO_OPTION = JOptionPane.NO_OPTION;

    private static final Logger LOGGER = Logger.getLogger(XalInternalWindow.class.getName());

    //------------- instance variables -----------------------------------------
    /**
     * The toolbar associated with this window
     */
    private JToolBar toolBar;

    /**
     * The document corresponding to this main window
     */
    protected XalInternalDocument document;

    /**
     * Creates a new instance of WindowAdaptor
     */
    protected XalInternalWindow(final XalInternalDocument aDocument) {
        super("", true, true, true, true);

        positionWindow();
        registerEvents();
        document = aDocument;
        makeFrame();
    }

    /**
     * position this window relative to the currently active window
     */
    private void positionWindow() {
        final XalInternalWindow selectedWindow = ((DesktopApplication) Application.getApp()).getSelectedWindow();
        // offset this window relative to the active window if any
        if (selectedWindow != null && selectedWindow.isVisible() && !selectedWindow.isIcon()) {
            final java.awt.Container contentPane = selectedWindow.getContentPane();
            final int offset = ((int) 1.5 * (contentPane.getLocationOnScreen().y - selectedWindow.getLocationOnScreen().y));
            final Point location = new Point(selectedWindow.getLocation());
            location.translate(offset, offset);
            setLocation(location);
        }
    }

    /**
     * Register the event handlers
     */
    public void registerEvents() {
    }

    /**
     * Get the internal document
     *
     * @return this window's internal document
     */
    XalInternalDocument getInternalDocument() {
        return document;
    }

    /**
     * Make the frame and populate the menubar and toolbar.
     */
    public void makeFrame() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        makeLayout();

        Commander commander = makeCommander();
        customizeCommands(commander);
        document.customizeCommands(commander);

        setJMenuBar(commander.getMenubar());

        if (usesToolbar()) {
            toolBar = commander.getToolbar();
            getContentPane().add(toolBar, "North");
        }
    }

    /**
     * Subclasses should override this method to provide a custom Commander.
     */
    public Commander makeCommander() {
        // create a document commander off of the application commander and a document
        return new Commander(document);
    }

    /**
     * Get the toolbar associated with this window.
     *
     * @return This window's toolbar or null if none was added.
     */
    @Override
    public JToolBar getToolBar() {
        return toolBar;
    }

    /**
     * Override this method to register custom commands.
     */
    public void customizeCommands(final Commander commander) {
    }

    /**
     * Make the window layout.
     */
    private void makeLayout() {
        getContentPane().setLayout(new BorderLayout());
    }

    /**
     * Close this window. Check to see if the document has unsaved changes and
     * if so warn the user and allow them to cancel the close operation.
     */
    public void closeWindow() {
        releaseWindow();
    }

    /**
     * Capture the window content as a PNG. Present the user with a save dialog
     * box so the image can be saved to a file.
     */
    @Override
    public void captureAsImage() {
        try {
            ImageCaptureManager.defaultManager().saveSnapshot(this.getContentPane());
        } catch (AWTException | IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to capture image.", exception);
            displayWarning(exception);
        }
    }

    /**
     * Show this window. Make it visible (de-iconify if necessary) and bring it
     * to the front.
     */
    @Override
    public void showWindow() {
        try {
            // de-iconify this window
            setIcon(false);
            setVisible(true);
            toFront();
            setSelected(true);
        } catch (PropertyVetoException exception) {
            Application.displayError("Document Exception", "Exception attempting to display document.", exception);
        }
    }

    /**
     * Iconify this window.
     */
    @Override
    public void hideWindow() {
        try {
            // iconify the window            
            setIcon(true);
        } catch (PropertyVetoException exception) {
            Application.displayError("Document Exception", "Exception attempting to iconify document.", exception);
        }
    }

    /**
     * Query the user to see if it is okay to close the document given that
     * unsaved changes exist.
     *
     * @return If the user allows the document to be closed.
     */
    public boolean userPermitsCloseWithUnsavedChanges() {
        String message = "Document has unsaved changes!\nDo you still want to close the document without saving changes?";
        int status = JOptionPane.showConfirmDialog(this, message, "Close Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return status == JOptionPane.OK_OPTION;
    }

    /**
     * Dispose of this window and remove its association with the document.
     */
    public final void releaseWindow() {
        freeCustomResources();

        dispose();

        document.removeXalInternalDocumentListener(this);
        document = null;
    }

    /**
     * Dispose of custom window resources. Subclasses should override this
     * method to provide custom disposal of resources. The default
     * implementation does nothing.
     */
    public void freeCustomResources() {
    }

    /**
     * Generate the title on the title bar to reflect the document state. The
     * title displays the application name, the document title if any and an
     * asterisk if the document has unsaved changes.
     */
    public void generateWindowTitle() {
        String windowTitle = "Untitled";
        final String documentTitle = document.getTitle();

        if (documentTitle != null && !documentTitle.isEmpty()) {
            windowTitle = documentTitle;

            if (document.hasChanges()) {
                windowTitle += "*";
            }
        }

        final String theTitle = windowTitle;

        // since this method often gets called from other threads we should take care to make it thread safe
        if (SwingUtilities.isEventDispatchThread()) {
            setTitle(theTitle);
        } else {
            try {
                SwingUtilities.invokeAndWait(() -> setTitle(theTitle));
            } catch (InterruptedException | InvocationTargetException exception) {
                throw new RuntimeException("Exception updating the window title.", exception);
            }
        }
    }

    /**
     * Handle the document event indicating that the title has changed. Update
     * the title on the title bar to reflect the new document title.
     *
     * @param document The document initiating the title changed event.
     * @param documentTitle The new document title.
     */
    @Override
    public final void titleChanged(final XalInternalDocument document, final String documentTitle) {
        generateWindowTitle();
    }

    /**
     * Update the title on the title bar to reflect whether the document has
     * changes that need saving.
     *
     * @param document The document initiating the event.
     * @param newHasChangesStatus The new status identifying whether the
     * document has changes to be saved
     * @see #titleChanged
     */
    @Override
    public void hasChangesChanged(final XalInternalDocument document, final boolean newHasChangesStatus) {
        titleChanged(document, document.getTitle());
    }

    /**
     * Handle the event indicating that the document will close by closing the
     * window in response.
     */
    @Override
    public void documentWillClose(final XalInternalDocument document) {
        closeWindow();
    }

    /**
     * Handle document closed event. Does nothing.
     */
    @Override
    public void documentHasClosed(final XalInternalDocument document) {
    }

    /**
     * Handle the document activated event.
     *
     * @param document the document that has been activated.
     */
    @Override
    public void documentActivated(XalInternalDocument document) {
    }

    /**
     * Handle the document activated event.
     *
     * @param document the document that has been activated.
     */
    @Override
    public void documentDeactivated(XalInternalDocument document) {
    }

    //----------- Methods subclasses might override ----------------------------
    /**
     * Subclasses may override this method to create a toolbar.
     */
    public boolean usesToolbar() {
        return false;
    }

    //----------- Convenience methods ------------------------------------------
    /**
     * Display a confirmation dialog with a title and message
     *
     * @param title The title of the dialog
     * @param message The message to display
     * @return YES_OPTION or NO_OPTION
     */
    @Override
    public int displayConfirmDialog(final String title, final String message) {
        Toolkit.getDefaultToolkit().beep();
        return JOptionPane.showInternalConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
    }

    /**
     * Display a warning dialog box and provide an audible alert.
     *
     * @param aTitle Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    @Override
    public void displayWarning(final String aTitle, final String message) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showInternalMessageDialog(this, message, aTitle, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Display a warning dialog box showing information about an exception that
     * has been thrown and provide an audible alert.
     *
     * @param exception The exception whose description is being displayed.
     */
    @Override
    public void displayWarning(final Exception exception) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showInternalMessageDialog(this, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Display a warning dialog box with information about the exception and
     * provide an audible alert. This method allows clarification about the
     * consequences of the exception (e.g. "Save Failed:").
     *
     * @param aTitle Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the
     * exception message.
     * @param exception The exception about which the warning dialog is
     * displayed.
     */
    @Override
    public void displayWarning(final String aTitle, final String prefix, final Exception exception) {
        Toolkit.getDefaultToolkit().beep();
        String message = prefix + "\n" + "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showInternalMessageDialog(this, message, aTitle, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Display an error dialog box and provide an audible alert.
     *
     * @param aTitle Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    @Override
    public void displayError(final String aTitle, final String message) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showInternalMessageDialog(this, message, aTitle, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an error dialog box with information about the exception and
     * provide an audible alert.
     *
     * @param exception The exception about which the warning dialog is
     * displayed.
     */
    @Override
    public void displayError(final Exception exception) {
        Toolkit.getDefaultToolkit().beep();
        String message = "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showInternalMessageDialog(this, message, exception.getClass().getName(), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an error dialog box with information about the exception and
     * provide an audible alert. This method allows clarification about the
     * consequences of the exception (e.g. "Save Failed:").
     *
     * @param aTitle Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the
     * exception message.
     * @param exception The exception about which the warning dialog is
     * displayed.
     */
    @Override
    public void displayError(final String aTitle, final String prefix, final Exception exception) {
        Toolkit.getDefaultToolkit().beep();
        String message = prefix + "\n" + "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showInternalMessageDialog(this, message, aTitle, JOptionPane.ERROR_MESSAGE);
    }
}
