/*
 * Copyright (C) 2017 European Spallation Source ERIC
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
package xal.app.modelbrowser;


import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import xal.smf.AcceleratorNode;

import static java.util.logging.Level.WARNING;


/**
 * A class rendering an accelerator model node.
 *
 * @author claudiorosati
 */
public class NodeTreeCell extends TreeCell<AcceleratorNode> {

    private static final Logger LOGGER = Logger.getLogger(NodeTreeCell.class.getName());

    private final Label classNameInfoLabel = new Label();
    private final Label displayInfoLabel = new Label();
    private final HBox graphic = new HBox();
    private final Label iconsLabel = new Label();

    NodeTreeCell() {

        // We cannot dynamically update the HBox graphic children
        // in the cell.updateItem method.
        // We set once the graphic children, then we update the
        // managed property of the children depending on the cell item.
        graphic.getChildren().setAll(iconsLabel, classNameInfoLabel, displayInfoLabel);

        // CSS
        graphic.getStyleClass().add("tree-cell-graphic");
        displayInfoLabel.getStyleClass().add("hierarchy-readwrite-label");

        // Layout
        classNameInfoLabel.setMinWidth(Control.USE_PREF_SIZE);
        displayInfoLabel.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(displayInfoLabel, Priority.ALWAYS);

    }

    @Override
    protected void updateItem( AcceleratorNode item, boolean empty ) {

        super.updateItem(item, empty);

        // The cell is not empty (TreeItem is not null)
        // AND the TreeItem value is not null
        if ( !empty && item != null ) {

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
            } catch ( IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ex ) {
                LOGGER.log(WARNING, "Getting type for {0}", classNameInfo);
            }

            displayInfoLabel.setText(displayInfo);
            displayInfoLabel.setManaged(displayInfo != null);
            displayInfoLabel.setVisible(displayInfo != null);

            setGraphic(graphic);
            setText(null);

        } else {
            assert item == null;
            setGraphic(null);
            setText(null);
        }

    }

}
