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
import javafx.scene.layout.StackPane;
import xal.smf.impl.MagnetPowerSupply;

/**
 * An ElementTreeCell that shows a MagnetPowerSupply using an icon and the node
 * ID of the power supply.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@ess.eu>
 */
class PowerSupplyTreeCell extends ElementTreeCell<MagnetPowerSupply> {

    @Override
    protected void updateItem(MagnetPowerSupply item, boolean empty) {

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
                LOGGER.log(WARNING, "Getting type for {0}", new Object[]{classNameInfo, ex});
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
