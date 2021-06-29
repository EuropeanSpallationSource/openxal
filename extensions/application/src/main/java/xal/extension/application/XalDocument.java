/*
 * XalDocument.java
 *
 * Created on March 19, 2003, 11:07 AM
 */
package xal.extension.application;

import xal.extension.bricks.WindowReference;

/**
 * The base class for custom documents. Subclasses of this class need to define
 * the logic for their document. Every document has a main window and a URL
 * source that provides persistent storage.
 *
 * @author t6p
 */
public abstract class XalDocument extends XalAbstractDocument {

    // basic document instance variables
    /**
     * The main window for the document
     */
    public XalWindow mainWindow;

    /**
     * proxy for dispatching document events
     */
    private XalDocumentListener documentListenerProxy;    //

    /**
     * Constructor for new documents
     */
    public XalDocument() {
        super();
    }

    /**
     * Register this document as a source of DocumentListener events.
     */
    @Override
    public void registerEvents() {
        super.registerEvents();
        documentListenerProxy = messageCenter.registerSource(this, XalDocumentListener.class);
    }

    /**
     * Add the listener for events from this document.
     */
    public void addXalDocumentListener(final XalDocumentListener listener) {
        messageCenter.registerTarget(listener, this, XalDocumentListener.class);
    }

    /**
     * Remove the listener from event from this document.
     */
    public void removeXalDocumentListener(final XalDocumentListener listener) {
        messageCenter.removeTarget(listener, this, XalDocumentListener.class);
    }

    /**
     * Construct the main window and associate it with this document.
     */
    @Override
    void setupMainWindow() {
        makeMainWindow();
        addXalDocumentListener(mainWindow);
        mainWindow.titleChanged(this, getTitle());
    }

    /**
     * Get the window reference from the resource if any
     */
    public static WindowReference getDefaultWindowReference(final String tag, final Object... parameters) {
        return Application.getAdaptor().getDefaultWindowReference(tag, parameters);
    }

    /**
     * Set the document title.
     *
     * @param newTitle The new title for this document.
     */
    @Override
    public void setTitle(final String newTitle) {
        super.setTitle(newTitle);
        if (documentListenerProxy != null) {
            documentListenerProxy.titleChanged(this, newTitle);
        }
    }

    /**
     * Set the whether this document has changes.
     *
     * @param changeStatus Status to set whether this document has changes that
     * need saving.
     */
    @Override
    public void setHasChanges(final boolean changeStatus) {
        if (changeStatus != hasChanges()) {
            super.setHasChanges(changeStatus);
            if (documentListenerProxy != null) {
                documentListenerProxy.hasChangesChanged(this, changeStatus);
            }
        }
    }

    /**
     * This method is a request to close a document. It may be called when, for
     * example, the user selects "Close" from the File menu, or when the user
     * closes the window with the close button, or when the application quits.
     * This request starts a series of events which closes the document. Xal
     * document listeners are notified that the document will close. They may
     * perform any cleanup as necessary before the document closes. Then the
     * listeners are informed that the document has closed. The application
     * removes the document from its list of open documents and informs its
     * listeners that the document has been closed. If there are any unsaved
     * changes, the user is given an opportunity to not close the document so
     * they can save the changes.
     */
    @Override
    public boolean closeDocument() {
        if (warnUserOfUnsavedChangesWhenClosing() && hasChanges()) {
            if (!mainWindow.userPermitsCloseWithUnsavedChanges()) {
                return false;
            }
        }

        documentListenerProxy.documentWillClose(this);
        willClose();
        documentListenerProxy.documentHasClosed(this);

        freeResources();

        return true;
    }

    /**
     * Free document resources.
     */
    @Override
    public final void freeResources() {
        super.freeResources();

        documentListenerProxy = null;
        mainWindow = null;
    }

    /**
     * Get the main window for this document.
     *
     * @return The main window for this document.
     */
    public XalWindow getMainWindow() {
        return mainWindow;
    }

    /**
     * Implement the method for XalAbstractDocument.
     *
     * @return The main window for this document.
     */
    @Override
    public XalDocumentView getDocumentView() {
        return mainWindow;
    }
}
