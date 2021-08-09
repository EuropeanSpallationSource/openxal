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
 * This class extends XalTreeView to show a list of Combo Sequences.
 *
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

    @Override
    public void setDocument(XalFxDocument document) {
        update(document.getAccelerator());
        document.getAcceleratorProperty().addChangeListener((ChangeListener<Accelerator>) (ov, oldAccelerator, newAccelerator) -> update(newAccelerator));
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
