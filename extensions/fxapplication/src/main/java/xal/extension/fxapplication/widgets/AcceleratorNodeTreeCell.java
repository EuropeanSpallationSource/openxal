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

import java.lang.reflect.InvocationTargetException;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import xal.smf.AcceleratorNode;

/**
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@ess.eu>
 */
public class AcceleratorNodeTreeCell extends TreeCell<AcceleratorNode> {

    private static final Logger LOGGER = Logger.getLogger(AcceleratorNodeTreeCell.class.getName());

    private final Label classNameInfoLabel = new Label();
    private final Label displayInfoLabel = new Label();
    private final HBox graphic = new HBox();
    private final HBox iconBox = new HBox();
    private final Label iconsLabel = new Label();

    AcceleratorNodeTreeCell() {
        iconBox.setPrefWidth(30);
        iconBox.setAlignment(Pos.CENTER);
        iconBox.getChildren().add(iconsLabel);
        graphic.getChildren().setAll(iconBox, classNameInfoLabel, displayInfoLabel);

        // CSS
        graphic.getStyleClass().add("tree-cell-graphic");
        displayInfoLabel.getStyleClass().add("hierarchy-readwrite-label");

        // Layout
        classNameInfoLabel.setMinWidth(Control.USE_PREF_SIZE);
        classNameInfoLabel.setStyle("-fx-font-weight: bold;");
        displayInfoLabel.setMaxWidth(Double.MAX_VALUE);
        displayInfoLabel.setAlignment(Pos.CENTER_RIGHT);

        HBox.setHgrow(displayInfoLabel, Priority.ALWAYS);
        graphic.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(AcceleratorNode item, boolean empty) {

        super.updateItem(item, empty);

        // The cell is not empty (TreeItem is not null)
        // AND the TreeItem value is not null
        if (!empty && item != null) {
            // Update Icon
            iconsLabel.setGraphic(getTreeItem().getGraphic());

            // Update Labels
            String classNameInfo = item.getId();

            classNameInfoLabel.setText(classNameInfo);
            classNameInfoLabel.setManaged(classNameInfo != null);
            classNameInfoLabel.setVisible(classNameInfo != null);

            String displayInfo = null;

            try {
                displayInfo = item.getClass().getMethod("getType").invoke(item).toString();
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                LOGGER.log(WARNING, "Getting type for {0}", classNameInfo);
            }

            displayInfoLabel.setText(displayInfo);
            displayInfoLabel.setManaged(displayInfo != null);
            displayInfoLabel.setVisible(displayInfo != null);

            setGraphic(graphic);
            setText(null);

            StackPane disclosureNode = (StackPane) getDisclosureNode();
            disclosureNode.setStyle("-fx-padding: 8 6 8 8;");
        } else {
            assert item == null;
            setGraphic(null);
            setText(null);
        }
    }
}
