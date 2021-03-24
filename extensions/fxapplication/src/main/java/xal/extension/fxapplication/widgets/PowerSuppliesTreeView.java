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
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import xal.extension.fxapplication.XalFxDocument;
import xal.smf.Accelerator;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.MagnetPowerSupply;

/**
 * This class extends TreeView to add buttons on top to add/remove elements.
 *
 * The widget can be used independently or coupled to the application document.
 * <p>
 * In the first case, use the {@link update(Accelerator accelerator) update}
 * method to set the accelerator that will be shown.
 * <p>
 * For integration with the application document, use the
 * {@link setDocument(XalFxDocument document) setDocument} method. It will
 * update the tree every time either the accelerator is changed.
 * <p>
 * One can also define listener for single and double click events.
 * <p>
 * @author Juan F. Esteban Müller <juanf.estebanmuller@ess.eu>
 */
public class PowerSuppliesTreeView extends VBox {

    private final TreeView powerSuppliesTreeView = new TreeView();

    private final HBox titlebar = new HBox();
    private final HBox titlebox = new HBox();

    private final HBox bottombar = new HBox();

    public PowerSuppliesTreeView() {
        // Top bar
        titlebox.setPadding(new Insets(5));
        Label titleLabel = new Label("Power Supplies");
        titlebox.getChildren().add(titleLabel);
        titlebar.getChildren().add(titlebox);

        // TreeView
        powerSuppliesTreeView.setCellFactory(p -> new PowerSupplyTreeCell());

        getChildren().addAll(titlebar, powerSuppliesTreeView, bottombar);
        VBox.setVgrow(powerSuppliesTreeView, Priority.ALWAYS);
    }

    public HBox getTitlebar() {
        return titlebar;
    }

    public HBox getBottombar() {
        return bottombar;
    }

    /**
     * Returns the property to
     *
     * @return
     */
    public ReadOnlyObjectProperty<TreeItem<MagnetPowerSupply>> selectedItemProperty() {
        return powerSuppliesTreeView.getSelectionModel().selectedItemProperty();
    }

    /**
     * This method uses the accelerator property to update the tree every time
     * the accelerator or the sequence is changed, and vice versa.
     * <p>
     * Use this method for full integration with the document. If the TreeView
     * is expected to be decoupled from the document, then use the update
     * method.
     */
    public void setDocument(XalFxDocument document) {
        update(document.getAccelerator());
        document.getAcceleratorProperty().addChangeListener((ChangeListener<Accelerator>) (ov, oldAccelerator, newAccelerator) -> {
            update(newAccelerator);
        });
    }

    public void update(Accelerator accelerator) {
        TreeItem<MagnetPowerSupply> rootNode = new TreeItem<>(null, null);
        rootNode.setExpanded(true);
        powerSuppliesTreeView.setRoot(rootNode);
        powerSuppliesTreeView.setShowRoot(false);

        ImageView icon;
        TreeItem<MagnetPowerSupply> seqNodeItem;
        for (MagnetMainSupply powerSupply : accelerator.getMagnetMainSupplies()) {
            URL iconPath = getClass().getResource("icons/32/" + AcceleratorNodeIcon.getIcon(powerSupply.getClass()));
            if (iconPath == null) {
                iconPath = getClass().getResource("icons/32/PS.png");
            }
            icon = new ImageView(iconPath.toExternalForm());
            seqNodeItem = new TreeItem<>(powerSupply, icon);
            rootNode.getChildren().add(seqNodeItem);
        }

        Logger.getLogger(getClass().getName()).fine("Updating power supplies treeview.");
    }

    /**
     * Returns the AcceleratorNode of the selected item.
     *
     * @return The AcceleratorNode or null if none selected.
     */
    public MagnetPowerSupply getSelectedNode() {
        MultipleSelectionModel<TreeItem<MagnetPowerSupply>> selectionModel = powerSuppliesTreeView.getSelectionModel();
        TreeItem<MagnetPowerSupply> selectedItem = selectionModel.getSelectedItem();
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
        MultipleSelectionModel<TreeItem<MagnetPowerSupply>> selectionModel = powerSuppliesTreeView.getSelectionModel();
        TreeItem<MagnetPowerSupply> selectedItem = selectionModel.getSelectedItem();
        if (selectedItem != null) {
            return selectedItem.getValue().getId();
        } else {
            return null;
        }
    }

    /**
     * Clear the selection.
     */
    public void clearSelection() {
        powerSuppliesTreeView.getSelectionModel().clearSelection();
    }

    /**
     * Select the node with the given node ID, if found on the TreeView. It
     * automatically expand all parent node and scroll to make the selected node
     * visible, if needed.
     *
     * @param nodeId The element's node ID.
     * @return True if the element has been found.
     */
    public boolean selectElement(String nodeId) {
        return selectElement(powerSuppliesTreeView.getRoot(), nodeId);
    }

    private boolean selectElement(TreeItem<MagnetPowerSupply> parentNode, String nodeId) {
        for (TreeItem<MagnetPowerSupply> treeItem : parentNode.getChildren()) {
            if (nodeId.equals(treeItem.getValue().getId())) {
                // Expand all parent items.
                for (TreeItem parent = treeItem; parent.getParent() != null; parent = parent.getParent()) {
                    parent.getParent().setExpanded(true);
                }
                // Select the element.
                powerSuppliesTreeView.getSelectionModel().select(treeItem);
                // Scroll to the item if not visible.
                int selectedIndex = powerSuppliesTreeView.getSelectionModel().getSelectedIndex();
                ObservableList<Node> childrenUnmodifiable = powerSuppliesTreeView.getChildrenUnmodifiable();
                VirtualFlow get = (VirtualFlow) childrenUnmodifiable.get(0);
                if (selectedIndex >= get.getLastVisibleCell().getIndex() || selectedIndex <= get.getFirstVisibleCell().getIndex()) {
                    powerSuppliesTreeView.scrollTo(selectedIndex);
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
}
