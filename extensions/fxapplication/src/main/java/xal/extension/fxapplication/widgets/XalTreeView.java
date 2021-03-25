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
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public abstract class XalTreeView<T> extends VBox {

    protected final TreeView treeView = new TreeView();
    protected final HBox titlebar = new HBox();
    protected final HBox titlebox = new HBox();
    protected final HBox bottombar = new HBox();

    protected EventHandler<MouseEvent> doubleClickEH;

    public XalTreeView() {
    }

    public HBox getTitlebar() {
        return titlebar;
    }

    public HBox getBottombar() {
        return bottombar;
    }

    public void addClickEventHandler(EventHandler<MouseEvent> eventHandler) {
        treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
    }

    public void removeClickEventHandler(EventHandler<MouseEvent> eventHandler) {
        treeView.removeEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
    }

    public void enableDefaultClickEventHandler() {
        treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickEH);
    }

    public void disableDefaultClickEventHandler() {
        treeView.removeEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickEH);
    }

    /**
     * Returns the property to
     *
     * @return
     */
    public ReadOnlyObjectProperty<TreeItem<T>> selectedItemProperty() {
        return treeView.getSelectionModel().selectedItemProperty();
    }

    public abstract void setDocument(XalFxDocument document);
    
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
        TreeItem<T> item = new TreeItem<>(node, getIcon(node));
        return item;
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
     * Returns the AcceleratorNode of the selected item.
     *
     * @return The AcceleratorNode or null if none selected.
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
     * Returns the AcceleratorNode of the selected item.
     *
     * @return The AcceleratorNode or null if none selected.
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
