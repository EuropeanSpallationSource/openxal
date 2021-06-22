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
 * Attribute set for additional magnet information about Fringe-Fields
 * factors<br>
 *
 * gap - total gap of magnet (m) entrK1 - Upstream edge face Fringe-field factor
 * (default = 0.45 for a square-edged magnet) entrK2 - Upstream edge face
 * Fringe-field factor (default = 2.80 for a square-edged magnet) exitK1 -
 * Downstream edge face Fringe-field factor (default = 0.45 for a square-edged
 * magnet) exitK2 - Downstream edge face Fringe-field factor (default = 2.80 for
 * a square-edged magnet)
 *
 * @author Ivo List
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class DipoleBucket extends xal.smf.attr.DipoleBucket {

    /**
     * ID for serializable version
     */
    private static final long serialVersionUID = 1L;

    /*
     *  Constants
     */
    private static final String[] ARR_NAMES = {
        // Total gap of magnet
        "gap",
        // entry Fringe-field factor K1
        "entrFringeFieldFactorK1",
        // entry Fringe-field factor K2
        "entrFringeFieldFactorK2",
        // exit Fringe-field factor K1
        "exitFringeFieldFactorK1",
        // exit Fringe-field factor K2
        "exitFringeFieldFactorK2",
        // Orientation of the magnet (H/V)
        "orientation"};

    public DipoleBucket() {
        super();

        attGap = new Attribute(0.0);
        attEntrFringeFieldFactorK1 = new Attribute(0.45);
        attEntrFringeFieldFactorK2 = new Attribute(2.8);
        attExitFringeFieldFactorK1 = new Attribute(0.45);
        attExitFringeFieldFactorK2 = new Attribute(2.8);
        attOrientation = new Attribute("");

        super.registerAttribute(ARR_NAMES[0], attGap, "Total gap of magnet (m).");
        super.registerAttribute(ARR_NAMES[1], attEntrFringeFieldFactorK1, "Entry Fringe-field factor K1.");
        super.registerAttribute(ARR_NAMES[2], attEntrFringeFieldFactorK2, "Entry Fringe-field factor K2.");
        super.registerAttribute(ARR_NAMES[3], attExitFringeFieldFactorK1, "Exit Fringe-field factor K1.");
        super.registerAttribute(ARR_NAMES[4], attExitFringeFieldFactorK2, "Exit Fringe-field factor K2.");
        super.registerAttribute(ARR_NAMES[5], attOrientation, "Orientation of the magnet (H/V).");
    }

    /**
     * total gap of the magnet (m)
     */
    private Attribute attGap;
    /**
     * Orientation of the magnet (H/V)
     */
    private Attribute attOrientation;
    /**
     * Upstream edge face Fringe-field factor (default = 0.45 for a square-edged
     * magnet)
     */
    private Attribute attEntrFringeFieldFactorK1;
    /**
     * Upstream edge face Fringe-field factor (default = 2.80 for a square-edged
     * magnet)
     */
    private Attribute attEntrFringeFieldFactorK2;
    /**
     * Downstream edge face Fringe-field factor (default = 0.45 for a
     * square-edged magnet)
     */
    private Attribute attExitFringeFieldFactorK1;
    /**
     * Downstream edge face Fringe-field factor (default = 2.80 for a
     * square-edged magnet)
     */
    private Attribute attExitFringeFieldFactorK2;

    /**
     * @return total gap of magnet (m)
     */
    public double getGap() {
        return attGap.getDouble();
    }

    /**
     * @param value total gap of magnet (m)
     */
    public void setGap(double value) {
        attGap.set(value);
    }

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
        switch (intVal) {
            case MagnetType.HORIZONTAL:
                attOrientation.set("horizontal");
                break;
            case MagnetType.VERTICAL:
                attOrientation.set("vertical");
                break;
            default:
                attOrientation.set("");
                break;
        }
    }

    /**
     * @return Upstream edge face Fringe-field factor (default = 0.45)
     */
    public double getEntrFringeFieldFactorK1() {
        return attEntrFringeFieldFactorK1.getDouble();
    }

    /**
     * @param value Upstream edge face Fringe-field factor (default = 0.45)
     */
    public void setEntrFringeFieldFactorK1(double value) {
        attEntrFringeFieldFactorK1.set(value);
    }

    /**
     * @return Upstream edge face Fringe-field factor (default = 2.80)
     */
    public double getEntrFringeFieldFactorK2() {
        return attEntrFringeFieldFactorK2.getDouble();
    }

    /**
     * @param value Upstream edge face Fringe-field factor (default = 2.80)
     */
    public void setEntrFringeFieldFactorK2(double value) {
        attEntrFringeFieldFactorK2.set(value);
    }

    /**
     * @return Downstream edge face Fringe-field factor (default = 0.45)
     */
    public double getExitFringeFieldFactorK1() {
        return attExitFringeFieldFactorK1.getDouble();
    }

    /**
     * @param value Downstream edge face Fringe-field factor (default = 0.45)
     */
    public void setExitFringeFieldFactorK1(double value) {
        attExitFringeFieldFactorK1.set(value);
    }

    /**
     * @return Downstream edge face Fringe-field factor (default = 2.80)
     */
    public double getExitFringeFieldFactorK2() {
        return attExitFringeFieldFactorK2.getDouble();
    }

    /**
     * @param value Downstream edge face Fringe-field factor (default = 2.80)
     */
    public void setExitFringeFieldFactorK2(double value) {
        this.attExitFringeFieldFactorK2.set(value);
    }
}
