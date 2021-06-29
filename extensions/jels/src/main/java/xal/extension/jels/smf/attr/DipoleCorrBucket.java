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
package xal.extension.jels.smf.attr;

import xal.smf.attr.Attribute;
import xal.smf.impl.qualify.MagnetType;

/**
 * Attribute set for additional magnet information about dipole correctors
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class DipoleCorrBucket extends xal.smf.attr.DipoleCorrBucket {

    /**
     * ID for serializable version
     */
    private static final long serialVersionUID = 1L;

    /*
     *  Constants
     */
    private static final String[] ARR_NAMES = {
        // Orientation of the magnet (H/V)
        "orientation"};

    public DipoleCorrBucket() {
        super();

        attOrientation = new Attribute("");

        super.registerAttribute(ARR_NAMES[0], attOrientation, "Orientation of the magnet (H/V).");
    }

    /**
     * Orientation of the magnet (H/V)
     */
    private Attribute attOrientation;

    public int getOrientation() {
        String strFieldType = attOrientation.getString();
        if ("horizontal".equalsIgnoreCase(strFieldType) || "H".equalsIgnoreCase(strFieldType)) {
            return MagnetType.HORIZONTAL;
        } else if ("vertical".equalsIgnoreCase(strFieldType) || "V".equalsIgnoreCase(strFieldType)) {
            return MagnetType.VERTICAL;
        }
        return 0;
    }

    public void setOrientation(int intVal) {
        if (intVal == MagnetType.HORIZONTAL) {
            attOrientation.set("horizontal");
        } else if (intVal == MagnetType.VERTICAL) {
            attOrientation.set("vertical");
        } else {
            attOrientation.set("");
        }
    }
}
