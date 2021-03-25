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

import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import xal.extension.fxapplication.XalFxDocument;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeqCombo;

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
public class ComboSequencesTreeView extends XalTreeView<AcceleratorNode> {

    public ComboSequencesTreeView() {
        // Top bar
        titlebox.setPadding(new Insets(5));
        Label titleLabel = new Label("Combo sequences");
        titlebox.getChildren().add(titleLabel);
        titlebar.getChildren().add(titlebox);

        // TreeView
        treeView.setCellFactory(p -> new AcceleratorNodeTreeCell());

        getChildren().addAll(titlebar, treeView, bottombar);
        VBox.setVgrow(treeView, Priority.ALWAYS);
    }

    /**
     * This method uses the accelerator property to update the tree every time
     * the accelerator or the sequence is changed, and vice versa.
     * <p>
     * Use this method for full integration with the document. If the TreeView
     * is expected to be decoupled from the document, then use the update
     * method.
     */
    @Override
    public void setDocument(XalFxDocument document) {
        update(document.getAccelerator());
        document.getAcceleratorProperty().addChangeListener((ChangeListener<Accelerator>) (ov, oldAccelerator, newAccelerator) -> {
            update(newAccelerator);
        });
    }

    @Override
    public void update(Accelerator accelerator) {
        TreeItem<AcceleratorNode> rootNode = new TreeItem<>(null, null);
        rootNode.setExpanded(true);
        treeView.setRoot(rootNode);
        treeView.setShowRoot(false);

        ImageView icon;
        TreeItem<AcceleratorNode> seqNodeItem;
        for (AcceleratorSeqCombo seq : accelerator.getComboSequences()) {
            icon = new ImageView(getClass().getResource("icons/32/SEQ.png").toExternalForm());
            seqNodeItem = new TreeItem<>(seq, icon);
            rootNode.getChildren().add(seqNodeItem);
        }

        Logger.getLogger(getClass().getName()).fine("Updating combo sequences treeview.");
    }

    @Override
    public TreeItem<AcceleratorNode> addElement(AcceleratorNode combo) {
        return addElement(combo, treeView.getRoot());
    }

    @Override
    protected String getId(TreeItem<AcceleratorNode> selectedItem) {
        return selectedItem.getValue().getId();
    }
}
