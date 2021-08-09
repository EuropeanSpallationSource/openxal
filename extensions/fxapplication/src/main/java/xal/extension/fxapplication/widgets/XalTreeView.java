/*
 * Copyright (C) 2021 European Spallation Source ERIC.
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
package xal.extension.fxapplication.widgets;

import java.net.URL;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import xal.extension.fxapplication.XalFxDocument;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.RfCavity;

/**
 * This class represents a VBox that contains a titlebar, a TreeView, and a
 * Bottom bar. The TreeView is supposed to contain elements of the XAL SMF.
 * <p>
 * It implements methods to add, remove, find, and select elements in the
 * TreeView.
 * <p>
 * The title bar can be used to add a title referring to the TreeView, and
 * buttons for functions like filters.
 * <p>
 * The bottom bar is intended for buttons to add/remove elements.
 * <p>
 * The widget can be used independently or coupled to the application document.
 * <p>
 * In the first case, use the {@link update(Accelerator accelerator) update}
 * method to set the accelerator that will be used to get the data.
 * <p>
 * For integration with the application document, use the
 * {@link setDocument(XalFxDocument document) setDocument} method. It will
 * update the tree every time either the accelerator is changed.
 * <p>
 * One can also define listener for single and double click events.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public abstract class XalTreeView<T> extends VBox {

    protected final TreeView treeView = new TreeView();
    protected final HBox titlebar = new HBox();
    protected final HBox titlebox = new HBox();
    protected final HBox bottombar = new HBox();

    protected EventHandler<MouseEvent> doubleClickEH;

    protected XalTreeView() {
    }

    /**
     * Get the HBox object that is defined above the TreeView.
     *
     * @return
     */
    public HBox getTitlebar() {
        return titlebar;
    }

    /**
     * Get the HBox object that is defined below the TreeView.
     *
     * @return
     */
    public HBox getBottombar() {
        return bottombar;
    }

    /**
     * Adds a new click event handler to the TreeView.
     *
     * @param eventHandler
     */
    public void addClickEventHandler(EventHandler<MouseEvent> eventHandler) {
        if (eventHandler != null) {
            treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
        }
    }

    /**
     * Removes a click event handler from the TreeView.
     *
     * @param eventHandler
     */
    public void removeClickEventHandler(EventHandler<MouseEvent> eventHandler) {
        if (eventHandler != null) {
            treeView.removeEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
        }
    }

    /**
     * Enables the default click event handler in the TreeView. The default
     * click event handler must be initialized by the subclass constructor.
     */
    public void enableDefaultClickEventHandler() {
        if (doubleClickEH != null) {
            treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickEH);
        }
    }

    /**
     * Disables the default click event handler in the TreeView.
     */
    public void disableDefaultClickEventHandler() {
        if (doubleClickEH != null) {
            treeView.removeEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickEH);
        }
    }

    /**
     * Returns the selected item property of the TreeView.
     *
     * @return
     */
    public ReadOnlyObjectProperty<TreeItem<T>> selectedItemProperty() {
        return treeView.getSelectionModel().selectedItemProperty();
    }

    /**
     * This method uses the accelerator property to update the tree every time
     * the accelerator is changed, and vice versa.
     * <p>
     * Use this method for full integration with the document. If the TreeView
     * is expected to be decoupled from the document, then use the update
     * method.
     */
    public abstract void setDocument(XalFxDocument document);

    /**
     * The method should repopulate the TreeView using the data from the
     * Accelerator object passed as an argument.
     *
     * @param accelerator
     */
    public abstract void update(Accelerator accelerator);

    public void refresh() {
        treeView.refresh();
    }

    /**
     * This method allows to add a node in the TreeView after adding it to the
     * accelerator, avoiding to reload the full treeview.
     *
     * @param node
     * @return
     */
    public abstract TreeItem<T> addElement(T node);

    protected TreeItem<T> addElement(T node, TreeItem<T> parentItem) {
        TreeItem<T> item = newItem(node);
        addItem(item, parentItem);
        refresh();

        return item;
    }

    protected TreeItem<T> newItem(T node) {
        return new TreeItem<>(node, getIcon(node));
    }

    protected void addItem(TreeItem<T> item, TreeItem<T> parentItem) {
        parentItem.getChildren().add(item);
    }

    protected ImageView getIcon(T node) {
        URL iconPath = getClass().getResource("icons/32/" + AcceleratorNodeIcon.getIcon(node.getClass()));
        // Icon by default when missing.
        if (node instanceof RfCavity && iconPath == null) {
            iconPath = getClass().getResource("icons/32/CAVM.png");
        } else if (node instanceof AcceleratorSeq && iconPath == null) {
            iconPath = getClass().getResource("icons/32/SEQ.png");
        } else if (iconPath == null) {
            iconPath = getClass().getResource("icons/32/BBX.png");
        }
        return new ImageView(iconPath.toExternalForm());
    }

    /**
     * Returns the TreeItem object corresponding to the selected item.
     *
     * @return The TreeItem or null if none selected.
     */
    public TreeItem<T> getSelectedItem() {
        MultipleSelectionModel<TreeItem<T>> selectionModel = treeView.getSelectionModel();
        TreeItem<T> selectedItem = selectionModel.getSelectedItem();
        if (selectedItem != null) {
            return selectedItem;
        } else {
            return null;
        }
    }

    /**
     * Returns the object represented by the selected TreeItem.
     *
     * @return The object or null if none selected.
     */
    public T getSelectedNode() {
        TreeItem<T> selectedItem = getSelectedItem();
        if (selectedItem != null) {
            return selectedItem.getValue();
        } else {
            return null;
        }
    }

    /**
     * Returns the node ID of the selected item.
     *
     * @return The node ID or null if none selected.
     */
    public String getSelectedNodeId() {
        MultipleSelectionModel<TreeItem<T>> selectionModel = treeView.getSelectionModel();
        TreeItem<T> selectedItem = selectionModel.getSelectedItem();
        if (selectedItem != null) {
            return getId(selectedItem);
        } else {
            return null;
        }
    }

    /**
     * This method should be implemented by subclasses to get the node ID
     * corresponding to a TreeItem object.
     *
     * @param selectedItem
     * @return
     */
    protected abstract String getId(TreeItem<T> selectedItem);

    /**
     * Clear the selection.
     */
    public void clearSelection() {
        treeView.getSelectionModel().clearSelection();
    }

    /**
     * Select the node with the given node ID, if found on the TreeView. It
     * automatically expand all parent node and scroll to make the selected node
     * visible, if needed.
     * <p>
     * If called right after the TreeView is updated, make sure it is called
     * using Platform.runLater() to make sure it is executed after the TreeView
     * is updated.
     *
     * @param nodeId The element's node ID.
     * @return True if the element has been found.
     */
    public boolean selectElement(String nodeId) {
        return selectElement(treeView.getRoot(), nodeId);
    }

    protected boolean selectElement(TreeItem<T> parentNode, String nodeId) {
        for (TreeItem<T> treeItem : parentNode.getChildren()) {
            if (nodeId.equals(getId(treeItem))) {
                // Expand all parent items.
                for (TreeItem parent = treeItem; parent.getParent() != null; parent = parent.getParent()) {
                    parent.getParent().setExpanded(true);
                }
                // Select the element.
                treeView.getSelectionModel().select(treeItem);
                // Scroll to the item if not visible.
                int selectedIndex = treeView.getSelectionModel().getSelectedIndex();
                ObservableList<Node> childrenUnmodifiable = treeView.getChildrenUnmodifiable();
                VirtualFlow get = (VirtualFlow) childrenUnmodifiable.get(0);
                if (selectedIndex >= get.getLastVisibleCell().getIndex() || selectedIndex <= get.getFirstVisibleCell().getIndex()) {
                    treeView.scrollTo(selectedIndex);
                }
                return true;
            }
            // Check also the children recursively.
            if (selectElement(treeItem, nodeId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the TreeItem element with the following node ID.
     *
     * @param nodeId The element's node ID.
     * @return TreeItem of the element with the given node ID or null if not
     * found.
     */
    public TreeItem<T> findElement(String nodeId) {
        return findElement(treeView.getRoot(), nodeId);
    }

    protected TreeItem<T> findElement(TreeItem<T> parentNode, String nodeId) {
        for (TreeItem<T> treeItem : parentNode.getChildren()) {
            if (nodeId.equals(getId(treeItem))) {
                return treeItem;
            }
            // Check also the children recursively.
            TreeItem<T> child = findElement(treeItem, nodeId);
            if (child != null) {
                return child;
            }
        }
        return null;
    }
}
