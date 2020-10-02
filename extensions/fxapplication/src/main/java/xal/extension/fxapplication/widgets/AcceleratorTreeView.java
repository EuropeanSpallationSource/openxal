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
package xal.extension.fxapplication.widgets;

import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import xal.extension.fxapplication.XalFxDocument;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.RfCavity;

/**
 * This class extends TreeView to add buttons on top to add/remove elements.
 *
 * The widget can be used independently or coupled to the application document.
 * <p>
 * In the first case, use the
 * {@link update(AcceleratorSeq acceleratorSeq) update} method to set the
 * accelerator sequence that will be shown.
 * <p>
 * For integration with the application document, use the
 * {@link setDocument(XalFxDocument document) setDocument} method. It will
 * update the tree every time either the accelerator or the sequence is changed.
 * Double clicking a sequence results in setting the sequence in the document.
 * <p>
 * One can also define listener for single and double click events. (TODO)
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@ess.eu>
 */
public class AcceleratorTreeView extends VBox {

    private XalFxDocument document;

    private final TreeView acceleratorTreeView = new TreeView();
    HBox titlebox = new HBox();
    MenuButton filterMenu = new MenuButton();

    private final Map<String, CheckMenuItem> typeMap = new TreeMap<>();
    private AcceleratorSeq currentSeq;

    public AcceleratorTreeView() {
        // Top bar
        HBox hbox = new HBox();
        titlebox.setPadding(new Insets(5));
        HBox.setHgrow(titlebox, Priority.ALWAYS);
        filterMenu.setText("Filter");
        ObservableList<MenuItem> menuItems = filterMenu.getItems();
        MenuItem menuItemSelectAll = new MenuItem("Select All Types");
        MenuItem menuItemUnselectAll = new MenuItem("Unselect All Types");
        menuItemSelectAll.setOnAction((e) -> typeMap.values().forEach(item -> item.setSelected(true)));
        menuItemUnselectAll.setOnAction((e) -> typeMap.values().forEach(item -> item.setSelected(false)));
        menuItems.add(menuItemSelectAll);
        menuItems.add(menuItemUnselectAll);
        menuItems.add(new SeparatorMenuItem());
        hbox.getChildren().addAll(titlebox, filterMenu);
        getChildren().add(hbox);

        // TreeView
        acceleratorTreeView.setCellFactory(p -> new AcceleratorNodeTreeCell());

        // Set actions on mouse double-click
        EventHandler<MouseEvent> eh = (MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                TreeItem<AcceleratorNode> selectedItem = (TreeItem<AcceleratorNode>) acceleratorTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getValue() instanceof AcceleratorSeq
                        && !selectedItem.getValue().getId().equals(document.getSequence())
                        && document.getAccelerator().getSequences().contains((AcceleratorSeq) selectedItem.getValue())) {
                    String seqName = selectedItem.getValue().getId();
                    document.getSequenceProperty().setValue(seqName);
                }
            }
        };
        acceleratorTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED, eh);

        getChildren().add(acceleratorTreeView);
        VBox.setVgrow(acceleratorTreeView, Priority.ALWAYS);
    }

    /**
     * Returns the property to 
     * 
     * @return 
     */
    public ReadOnlyObjectProperty<TreeItem<AcceleratorNode>> selectedItemProperty() {
        return acceleratorTreeView.getSelectionModel().selectedItemProperty();
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
        this.document = document;
        update(document.getAccelerator());

        document.getAcceleratorProperty().addChangeListener((ov, t, t1) -> update(document.getAccelerator()));
        document.getSequenceProperty().addListener((ov, t, t1) -> {
            Accelerator accelerator = document.getAccelerator();
            AcceleratorSeq seq = accelerator.getSequence(document.getSequence());
            if (seq == null) {
                seq = accelerator.getComboSequence(document.getSequence());
            }
            if (seq == null) {
                update(document.getAccelerator());
            } else {
                update(seq);
            }
        });

    }

    /**
     * Update the TreeView with a new accelerator sequence.
     *
     * @param acceleratorSeq
     */
    public void update(AcceleratorSeq acceleratorSeq) {
        currentSeq = acceleratorSeq;
        updateFilterMenu();
        updateTreeView();
    }

    private void updateTreeView() {
        ImageView icon = new ImageView(getClass().getResource("icons/32/SEQ.png").toExternalForm());
        TreeItem<AcceleratorNode> rootNode = new TreeItem<>(currentSeq, icon);
        rootNode.setExpanded(true);
        acceleratorTreeView.setRoot(rootNode);
        // Hide root node when showing full accelerator.
        if (currentSeq instanceof Accelerator) {
            acceleratorTreeView.setShowRoot(false);
            titlebox.getChildren().clear();
            Label acceleratorName = new Label(((Accelerator) currentSeq).getSystemId());
            acceleratorName.setStyle("-fx-font-weight: bold;");
            titlebox.getChildren().add(acceleratorName);
        } else {
            acceleratorTreeView.setShowRoot(true);
            titlebox.getChildren().clear();
            Label acceleratorName = new Label(currentSeq.getAccelerator().getSystemId());
            acceleratorName.setStyle("-fx-font-weight: bold;");
            acceleratorName.setOnMouseReleased((e) -> update(document.getAccelerator()));
            Label separator = new Label();
            separator.getStyleClass().add("triangle-shape");
            Label sequenceName = new Label(currentSeq.getId());
            titlebox.getChildren().addAll(acceleratorName, separator, sequenceName);
        }

        addSequence(currentSeq, rootNode);

        Logger.getLogger(getClass().getName()).fine("Updating accelerator treeview.");
    }

    /**
     * Recursive method to add sequences and child nodes to the TreeView.
     *
     * @param parentSeq
     * @param parentNode
     */
    private void addSequence(AcceleratorSeq parentSeq, TreeItem<AcceleratorNode> parentNode) {
        TreeItem<AcceleratorNode> acceleratorNodeItem;
        if (parentSeq instanceof AcceleratorSeqCombo) {
            for (AcceleratorSeq seq : ((AcceleratorSeqCombo) parentSeq).getConstituents()) {
                ImageView icon = new ImageView(getClass().getResource("icons/32/SEQ.png").toExternalForm());
                acceleratorNodeItem = new TreeItem<>(seq, icon);
                addSequence(seq, acceleratorNodeItem);
                parentNode.getChildren().add(acceleratorNodeItem);
            }
        } else {
            for (AcceleratorNode node : parentSeq.getNodes()) {
                if (node instanceof AcceleratorSeq) {
                    URL iconPath = getClass().getResource("icons/32/SEQ.png");
                    if (node instanceof RfCavity) {
                        iconPath = getClass().getResource("icons/32/CAVM.png");
                    }
                    ImageView icon = new ImageView(iconPath.toExternalForm());
                    acceleratorNodeItem = new TreeItem<>(node, icon);
                    addSequence((AcceleratorSeq) node, acceleratorNodeItem);
                    parentNode.getChildren().add(acceleratorNodeItem);
                } else {
                    if (typeMap.get(node.getType()).isSelected()) {
                        URL iconPath = getClass().getResource("icons/32/" + AcceleratorNodeIcon.getIcon(node.getType()));
                        if (iconPath == null) {
                            iconPath = getClass().getResource("icons/32/BBX.png");
                        }
                        ImageView icon = new ImageView(iconPath.toExternalForm());
                        acceleratorNodeItem = new TreeItem<>(node, icon);
                        parentNode.getChildren().add(acceleratorNodeItem);
                    }

                }
            }
        }
    }

    private void updateFilterMenu() {
        filterMenu.getItems().remove(3, filterMenu.getItems().size() - 1);

        currentSeq.getAllNodes().stream().map(n -> n.getType()).distinct().sorted().forEachOrdered(t -> {
            if (!t.equals("sequence")) {
                addTypeMenuItem(t);
            }
        });
    }

    private void addTypeMenuItem(String type) {
        final CheckMenuItem menuItem = new CheckMenuItem(type);

        menuItem.setSelected(true);
        menuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateTreeView();
        });

        typeMap.put(type, menuItem);
        filterMenu.getItems().add(menuItem);
    }

    /**
     * Returns the node ID of the selected item.
     *
     * @return The node ID or null if none selected.
     */
    public String getSelectedNodeId() {
        MultipleSelectionModel<TreeItem<AcceleratorNode>> selectionModel = acceleratorTreeView.getSelectionModel();
        TreeItem<AcceleratorNode> selectedItem = selectionModel.getSelectedItem();
        if (selectedItem != null) {
            return selectedItem.getValue().getId();
        } else {
            return null;
        }
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
        return selectElement(acceleratorTreeView.getRoot(), nodeId);
    }

    private boolean selectElement(TreeItem<AcceleratorNode> parentNode, String nodeId) {
        for (TreeItem<AcceleratorNode> treeItem : parentNode.getChildren()) {
            if (nodeId.equals(treeItem.getValue().getId())) {
                // Expand all parent items.
                for (TreeItem parent = treeItem; parent.getParent() != null; parent = parent.getParent()) {
                    parent.getParent().setExpanded(true);
                }
                // Select the element.
                acceleratorTreeView.getSelectionModel().select(treeItem);
                // Scroll to the item if not visible.
                int selectedIndex = acceleratorTreeView.getSelectionModel().getSelectedIndex();
                ObservableList<Node> childrenUnmodifiable = getChildrenUnmodifiable();
                VirtualFlow get = (VirtualFlow) childrenUnmodifiable.get(0);
                if (selectedIndex >= get.getLastVisibleCell().getIndex() || selectedIndex <= get.getFirstVisibleCell().getIndex()) {
                    acceleratorTreeView.scrollTo(selectedIndex);
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
