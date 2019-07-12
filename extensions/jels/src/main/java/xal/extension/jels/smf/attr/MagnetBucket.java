/*
 * Copyright (C) 2019 European Spallation Source ERIC.
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
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class MagnetBucket extends xal.smf.attr.MagnetBucket {

    /**
     * ID for serializable version
     */
    private static final long serialVersionUID = 1L;

    /*
     *  Constants
     */
    private final static String[] c_arrNames = {
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

    /**
     * Override virtual to provide type signature
     *
     * @return attribute names
     */
    @Override
    public String[] getAttrNames() {
        String[] attrs = super.getAttrNames();
        String[] allAttrs = new String[attrs.length + c_arrNames.length];
        System.arraycopy(attrs, 0, allAttrs, 0, attrs.length);
        System.arraycopy(c_arrNames, 0, allAttrs, attrs.length, c_arrNames.length);
        return allAttrs;
    }

    public MagnetBucket() {
        super();

        m_attGap = new Attribute(0.0);
        m_attEntrFringeFieldFactorK1 = new Attribute(0.45);
        m_attEntrFringeFieldFactorK2 = new Attribute(2.8);
        m_attExitFringeFieldFactorK1 = new Attribute(0.45);
        m_attExitFringeFieldFactorK2 = new Attribute(2.8);
        m_attOrientation = new Attribute("");

        super.registerAttribute(c_arrNames[0], m_attGap);
        super.registerAttribute(c_arrNames[1], m_attEntrFringeFieldFactorK1);
        super.registerAttribute(c_arrNames[2], m_attEntrFringeFieldFactorK2);
        super.registerAttribute(c_arrNames[3], m_attExitFringeFieldFactorK1);
        super.registerAttribute(c_arrNames[4], m_attExitFringeFieldFactorK2);
        super.registerAttribute(c_arrNames[5], m_attOrientation);
    }

    /**
     * total gap of the magnet (m)
     */
    private Attribute m_attGap;
    /**
     * Orientation of the magnet (H/V)
     */
    private Attribute m_attOrientation;
    /**
     * Upstream edge face Fringe-field factor (default = 0.45 for a square-edged
     * magnet)
     */
    private Attribute m_attEntrFringeFieldFactorK1;
    /**
     * Upstream edge face Fringe-field factor (default = 2.80 for a square-edged
     * magnet)
     */
    private Attribute m_attEntrFringeFieldFactorK2;
    /**
     * Downstream edge face Fringe-field factor (default = 0.45 for a
     * square-edged magnet)
     */
    private Attribute m_attExitFringeFieldFactorK1;
    /**
     * Downstream edge face Fringe-field factor (default = 2.80 for a
     * square-edged magnet)
     */
    private Attribute m_attExitFringeFieldFactorK2;

    /**
     * @return total gap of magnet (m)
     */
    public double getGap() {
        return m_attGap.getDouble();
    }

    /**
     * @param value total gap of magnet (m)
     */
    public void setGap(double value) {
        m_attGap.set(value);
    }

    public int getOrientation() {
        String strFieldType = m_attOrientation.getString();
        if ("horizontal".equalsIgnoreCase(strFieldType) || "H".equalsIgnoreCase(strFieldType)) {
            return MagnetType.HORIZONTAL;
        } else if ("vertical".equalsIgnoreCase(strFieldType) || "V".equalsIgnoreCase(strFieldType)) {
            return MagnetType.VERTICAL;
        }
        return 0;
    }

    public void setOrientation(int intVal) {
        if (intVal == MagnetType.HORIZONTAL) {
            m_attOrientation.set("horizontal");
        } else if (intVal == MagnetType.VERTICAL) {
            m_attOrientation.set("vertical");
        } else {
            m_attOrientation.set("");
        }
    }

    /**
     * @return Upstream edge face Fringe-field factor (default = 0.45)
     */
    public double getEntrFringeFieldFactorK1() {
        return m_attEntrFringeFieldFactorK1.getDouble();
    }

    /**
     * @param value Upstream edge face Fringe-field factor (default = 0.45)
     */
    public void setEntrFringeFieldFactorK1(double value) {
        m_attEntrFringeFieldFactorK1.set(value);
    }

    /**
     * @return Upstream edge face Fringe-field factor (default = 2.80)
     */
    public double getEntrFringeFieldFactorK2() {
        return m_attEntrFringeFieldFactorK2.getDouble();
    }

    /**
     * @param value Upstream edge face Fringe-field factor (default = 2.80)
     */
    public void setEntrFringeFieldFactorK2(double value) {
        m_attEntrFringeFieldFactorK2.set(value);
    }

    /**
     * @return Downstream edge face Fringe-field factor (default = 0.45)
     */
    public double getExitFringeFieldFactorK1() {
        return m_attExitFringeFieldFactorK1.getDouble();
    }

    /**
     * @param value Downstream edge face Fringe-field factor (default = 0.45)
     */
    public void setExitFringeFieldFactorK1(double value) {
        m_attExitFringeFieldFactorK1.set(value);
    }

    /**
     * @return Downstream edge face Fringe-field factor (default = 2.80)
     */
    public double getExitFringeFieldFactorK2() {
        return m_attExitFringeFieldFactorK2.getDouble();
    }

    /**
     * @param value Downstream edge face Fringe-field factor (default = 2.80)
     */
    public void setExitFringeFieldFactorK2(double value) {
        this.m_attExitFringeFieldFactorK2.set(value);
    }
}
