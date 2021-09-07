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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
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
 * This class extends XalTreeView to represent the Accelerator tree or a
 * sequence. It supports both normal AcceleratorSeq and AcceleratorSeqCombo
 * objects.
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
 *
 * See also {@link  xal.extension.fxapplication.widgets.XalTreeView}.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@ess.eu>
 */
public class AcceleratorTreeView extends XalTreeView<AcceleratorNode> {

    private XalFxDocument document;

    private final Map<String, CheckMenuItem> typeMap = new TreeMap<>();

    private AcceleratorSeq currentSeq;

    private boolean alwaysShowRfCavities = false;
    private boolean showAcceleratorNode = false;
    private boolean multipleSelectionFlag = false;
    private final Object lock = new Object();

    private final MenuButton filterMenu = new MenuButton();

    public AcceleratorTreeView() {
        // Top bar
        titlebox.setPadding(new Insets(5));
        HBox.setHgrow(titlebox, Priority.ALWAYS);
        filterMenu.setText("Filter");
        ObservableList<MenuItem> menuItems = filterMenu.getItems();
        MenuItem menuItemSelectAll = new MenuItem("Select All Types");
        MenuItem menuItemDeselecttAll = new MenuItem("Deselect All Types");
        menuItemSelectAll.setOnAction((e) -> this.selectAllFilters());
        menuItemDeselecttAll.setOnAction((e) -> this.deselectAllFilters());
        menuItems.add(menuItemSelectAll);
        menuItems.add(menuItemDeselecttAll);
        menuItems.add(new SeparatorMenuItem());
        titlebar.getChildren().addAll(titlebox, filterMenu);
        getChildren().add(titlebar);

        // Double-click handler to change sequence enabled by default.
        enabledDefaultDoubleClickEventHandler = true;

        getChildren().addAll(treeView, bottombar);
        VBox.setVgrow(treeView, Priority.ALWAYS);
    }

    // Set actions on mouse double-click
    @Override
    protected void doubleClickEventHandler(MouseEvent event) {
        if (document != null && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            TreeItem<AcceleratorNode> selectedItem = (TreeItem<AcceleratorNode>) getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getValue() instanceof AcceleratorSeq
                    && !selectedItem.getValue().getId().equals(document.getSequence())
                    && document.getAccelerator().getSequences().contains((AcceleratorSeq) selectedItem.getValue())) {
                String seqName = selectedItem.getValue().getId();
                document.getSequenceProperty().setValue(seqName);
            }
        }
    }

    @Override
    public void setDocument(XalFxDocument document) {
        this.document = document;
        update(document.getAccelerator());
        document.getAcceleratorProperty().addChangeListener((ov, t, t1) -> {
            // Clearing the typeMap so that filter is reseted to all selected.
            typeMap.clear();
            update(document.getAccelerator());
        });
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

    public boolean isShowAcceleratorNode() {
        return showAcceleratorNode;
    }

    public void setShowAcceleratorNode(boolean showAcceleratorNode) {
        this.showAcceleratorNode = showAcceleratorNode;
        treeView.setShowRoot(showAcceleratorNode);
    }

    public boolean isAlwaysShowRfCavities() {
        return alwaysShowRfCavities;
    }

    public void setAlwaysShowRfCavities(boolean alwaysShowRfCavities) {
        this.alwaysShowRfCavities = alwaysShowRfCavities;
        updateTreeViewKeepSelection();
    }

    @Override
    public TreeItem<AcceleratorNode> addElement(AcceleratorNode node) {
        AcceleratorNode parent = node.getParent();
        if (parent instanceof Accelerator) {
            return addElement(node, treeView.getRoot());
        } else {
            TreeItem<AcceleratorNode> parentItem = findElement(parent.getId());
            return addElement(node, parentItem);
        }
    }

    @Override
    protected void addItem(TreeItem<AcceleratorNode> item, TreeItem<AcceleratorNode> parentItem) {
        AcceleratorNode node = item.getValue();
        if (!(node instanceof AcceleratorSeq) && typeMap.get(node.getType()) == null) {
            addTypeMenuItem(node.getType());
        }
        if (node instanceof RfCavity) {
            // Add RfCavity node if it is selected in the filter or if it has children visible.
            if (alwaysShowRfCavities || typeMap.get(node.getType()).isSelected() || !item.getChildren().isEmpty()) {
                parentItem.getChildren().add(item);
            }
        } else if (node instanceof AcceleratorSeq) {
            // Sequences are always shown.
            parentItem.getChildren().add(item);
        } else {
            if (typeMap.get(node.getType()).isSelected()) {
                parentItem.getChildren().add(item);
            }
        }
    }

    /**
     * Recursive method to add sequences and child nodes to the TreeView. It
     * also supports combo sequences.
     *
     * @param parentSeq
     * @param parentItem
     */
    private void addSequence(AcceleratorSeq parentSeq, TreeItem<AcceleratorNode> parentItem) {
        if (parentSeq instanceof AcceleratorSeqCombo) {
            for (AcceleratorSeq seq : ((AcceleratorSeqCombo) parentSeq).getConstituents()) {
                TreeItem<AcceleratorNode> item = newItem(seq);
                addSequence(seq, item);
                addItem(item, parentItem);
            }
        } else {
            for (AcceleratorNode node : parentSeq.getNodes()) {
                TreeItem<AcceleratorNode> item = newItem(node);
                // Add children nodes recursively for sequenes.
                if (node instanceof AcceleratorSeq) {
                    addSequence((AcceleratorSeq) node, item);
                }
                addItem(item, parentItem);
            }
        }
    }

    public void replaceElement(AcceleratorNode nodeBefore, AcceleratorNode nodeAfter) {
        if (nodeBefore != nodeAfter) {
            TreeItem<AcceleratorNode> item = findElement(nodeBefore.getId());
            item.setValue(nodeAfter);
            item.setGraphic(getIcon(nodeAfter));
        }
    }

    @Override
    public void update(Accelerator accelerator) {
        update((AcceleratorSeq) accelerator);
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

    protected void updateTreeViewKeepSelection() {
        // Check if there any selected item
        String selectedNodeId = getSelectedNodeId();

        updateTreeView();

        // Select the same item that was selected before calling this method, if available.
        if (selectedNodeId != null) {
            selectElement(selectedNodeId);
        }

        Logger.getLogger(getClass().getName()).fine("Updating accelerator treeview.");
    }
    
    protected void updateTreeView() {
        ImageView icon = new ImageView(getClass().getResource("icons/32/SEQ.png").toExternalForm());
        TreeItem<AcceleratorNode> rootNode = new TreeItem<>(currentSeq, icon);
        rootNode.setExpanded(true);
        treeView.setRoot(rootNode);
        if (currentSeq instanceof Accelerator) {
            // Hide root node when showing full accelerator if flag disabled (default)
            treeView.setShowRoot(showAcceleratorNode);
            titlebox.getChildren().clear();
            Label acceleratorName = new Label(((Accelerator) currentSeq).getSystemId());
            acceleratorName.setStyle("-fx-font-weight: bold;");
            titlebox.getChildren().add(acceleratorName);
        } else {
            treeView.setShowRoot(true);
            titlebox.getChildren().clear();
            Label acceleratorName = new Label(currentSeq.getAccelerator().getSystemId());
            acceleratorName.setStyle("-fx-font-weight: bold;");
            // When the accelerator name is clicked, the sequence property is
            // set to null and updateTreeView() is triggered.
            acceleratorName.setOnMouseReleased(e -> document.getSequenceProperty().setValue(null));
            Label separator = new Label();
            separator.getStyleClass().add("triangle-shape");
            Label sequenceName = new Label(currentSeq.getId());
            titlebox.getChildren().addAll(acceleratorName, separator, sequenceName);
        }
        addSequence(currentSeq, rootNode);

        Logger.getLogger(getClass().getName()).fine("Updating accelerator treeview.");
    }

    /**
     * Hides the filter menu. Then the application must make sure to define the
     * right filter.
     */
    public void hideFilterMenu() {
        titlebar.getChildren().remove(filterMenu);
    }

    /**
     * Shows the filter menu.
     */
    public void showFilterMenu() {
        if (!titlebar.getChildren().contains(filterMenu)) {
            titlebar.getChildren().add(filterMenu);
        }
    }

    /**
     * Convenience method to select all filters. The Tree is updated only once.
     */
    public void selectAllFilters() {
        synchronized (lock) {
            multipleSelectionFlag = true;
        }
        typeMap.values().forEach(item -> item.setSelected(true));

        updateTreeViewKeepSelection();
        synchronized (lock) {
            multipleSelectionFlag = false;
        }
    }

    /**
     * Convenience method to deselect all filters. The Tree is updated only
     * once.
     */
    public void deselectAllFilters() {
        synchronized (lock) {
            multipleSelectionFlag = true;
        }
        typeMap.values().forEach(item -> item.setSelected(false));

        updateTreeViewKeepSelection();
        synchronized (lock) {
            multipleSelectionFlag = false;
        }
    }

    /**
     * Get the list of element types that are selected in the filter.
     *
     * @return An array of Strings with the selected types.
     */
    public String[] getSelectedFilters() {
        List<String> filters = new ArrayList<>();
        typeMap.keySet().forEach(item -> {
            if (typeMap.get(item).isSelected()) {
                filters.add(item);
            }
        });
        String[] filtersArray = new String[filters.size()];

        return filters.toArray(filtersArray);
    }

    /**
     *
     * @return true if all filters are selected.
     */
    public boolean areAllFiltersSelected() {
        boolean allFiltersSelectedFlag = true;

        for (String item : typeMap.keySet()) {
            if (!typeMap.get(item).isSelected()) {
                allFiltersSelectedFlag = false;
            }
        }

        return allFiltersSelectedFlag;
    }

    /**
     * Convenience method to select some filters.The Tree is updated only once.
     *
     * @param unselectOthers flag to disable the filters not passed as
     * arguments.
     * @param elementTypes Strings with element types to be selected.
     */
    public void selectFilters(String[] elementTypes, boolean unselectOthers) {
        synchronized (lock) {
            multipleSelectionFlag = true;
        }

        for (String type : typeMap.keySet()) {
            if (unselectOthers) {
                typeMap.get(type).setSelected(false);
            }
            for (String elementType : elementTypes) {
                if (type.equals(elementType)) {
                    typeMap.get(type).setSelected(true);
                }
            }
        }

        updateTreeViewKeepSelection();
        synchronized (lock) {
            multipleSelectionFlag = false;
        }
    }

    /**
     * Convenience method to select some filters.The Tree is updated only once.
     *
     * @param elementTypes Strings with element types to be selected.
     */
    public void selectFilters(String[] elementTypes) {
        selectFilters(elementTypes, false);
    }

    /**
     * Convenience method to deselect some filters.The Tree is updated only
     * once.
     *
     * @param elementTypes Strings with element types to be unselected.
     */
    public void deselectFilters(String[] elementTypes) {
        synchronized (lock) {
            multipleSelectionFlag = true;
        }

        for (String elementType : elementTypes) {
            for (String type : typeMap.keySet()) {
                if (type.equals(elementType)) {
                    typeMap.get(type).setSelected(false);
                }
            }
        }
        updateTreeViewKeepSelection();
        synchronized (lock) {
            multipleSelectionFlag = false;
        }
    }

    private void updateFilterMenu() {
        filterMenu.getItems().remove(3, filterMenu.getItems().size());

        boolean allFiltersSelectedFlag = areAllFiltersSelected();
        String[] filters = getSelectedFilters();

        typeMap.clear();

        currentSeq.getAllNodes().stream().map(n -> n.getType()).distinct().sorted().forEachOrdered(t -> {
            if (!t.equals("sequence")) {
                addTypeMenuItem(t);
            }
        });

        if (!allFiltersSelectedFlag) {
            this.selectFilters(filters, true);
        }
    }

    private void addTypeMenuItem(String type) {
        final CheckMenuItem menuItem = new CheckMenuItem(type);

        menuItem.setSelected(true);
        menuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Update the AcceleratorTreeView only if one element is selected.
            // If a multiple selection is being done, the update must be done manually.
            synchronized (lock) {
                if (!multipleSelectionFlag) {
                    updateTreeViewKeepSelection();
                }
            }
        });

        typeMap.put(type, menuItem);
        filterMenu.getItems().add(menuItem);
    }

    @Override
    protected String getId(TreeItem<AcceleratorNode> selectedItem) {
        return selectedItem.getValue().getId();
    }
}
