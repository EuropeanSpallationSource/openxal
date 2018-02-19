/*
 * Copyright (C) 2018 European Spallation Source ERIC.
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
import xal.smf.attr.AttributeBucket;

/**
 * A set of FieldMap attributes.
 *
 * @author Ivo List
 */
public class ESSFieldMapBucket extends AttributeBucket {

    private static final long serialVersionUID = 1;

    /*
     *  Constants
     */
 /*
     * 
     * Parameter
	 *
     */
    public static final String c_strType = "fieldmap";

    static final String[] c_arrNames = {
        "xelmax",
        "phase",
        "freq",
        "ampFactor",
        "phaseOffset",
        "fieldMapFile",
        "phasePosition",
        "gapOffset"
    };

    /*
     *  Local Attributes
     */
    /**
     * Electric field intensity factor
     */
    private Attribute m_attXelmax;

    /**
     * Default (design) cavity RF phase (deg)
     */
    private Attribute m_attPhase;

    /**
     * Design cavity resonant frequency (MHz)
     */
    private Attribute m_attFreq;

    private Attribute m_attAmpFactor;

    private Attribute m_attPhaseOffset;

    /**
     * FieldMap file
     */
    private Attribute m_attFieldMapFile;

    /**
     * Position where cavity RF phase is given (m) relative to element's start
     */
    private Attribute m_attPhasePosition;

    /**
     * Additional offset of the position where cavity RF phase is given (m)
     */
    private Attribute m_attGapOffset;

    /*
     *  User Interface
     */
    /**
     * @return Override virtual to provide type signature
     */
    @Override
    public String getType() {
        return c_strType;
    }

    @Override
    public String[] getAttrNames() {
        return c_arrNames;
    }

    public ESSFieldMapBucket() {
        super();

        m_attXelmax = new Attribute(1.);
        m_attPhase = new Attribute(0.);
        m_attFreq = new Attribute(0.);
        m_attAmpFactor = new Attribute(1.);
        m_attPhaseOffset = new Attribute(0.);
        m_attFieldMapFile = new Attribute("");
        m_attPhasePosition = new Attribute(0.);
        m_attGapOffset = new Attribute(0.);

        super.registerAttribute(c_arrNames[0], m_attXelmax);
        super.registerAttribute(c_arrNames[1], m_attPhase);
        super.registerAttribute(c_arrNames[2], m_attFreq);
        super.registerAttribute(c_arrNames[3], m_attAmpFactor);
        super.registerAttribute(c_arrNames[4], m_attPhaseOffset);
        super.registerAttribute(c_arrNames[5], m_attFieldMapFile);
        super.registerAttribute(c_arrNames[6], m_attPhasePosition);
        super.registerAttribute(c_arrNames[7], m_attGapOffset);
    }

    /**
     * @return Electric field intensity factor
     */
    public double getXelmax() {
        return m_attXelmax.getDouble();
    }

    /**
     * @return Default (design) cavity RF phase (deg)
     */
    public double getPhase() {
        return m_attPhase.getDouble();
    }

    /**
     * @return Design cavity resonant frequency (MHz)
     */
    public double getFrequency() {
        return m_attFreq.getDouble();
    }

    public double getAmpFactor() {
        return m_attAmpFactor.getDouble();
    }

    public double getPhaseOffset() {
        return m_attPhaseOffset.getDouble();
    }

    /**
     * @return FieldMap file
     */
    public String getFieldMapFile() {
        return m_attFieldMapFile.getString();
    }

    /**
     * @return Position where cavity RF phase is given (m) relative to element's start
     */
    public double getPhasePosition() {
        return m_attPhasePosition.getDouble();
    }

    /**
     * @return Additional offset of the position where cavity RF phase is given (m)
     */
    public double getGapOffset() {
        return m_attGapOffset.getDouble();
    }

    /**
     * @param dblVal Electric field intensity factor
     */
    public void setXelmax(double dblVal) {
        m_attXelmax.set(dblVal);
    }

    /**
     * @param dblVal Default (design) cavity RF phase (deg)
     */
    public void setPhase(double dblVal) {
        m_attPhase.set(dblVal);
    }

    /**
     * @param dblVal Design cavity resonant frequency (MHz)
     */
    public void setFrequency(double dblVal) {
        m_attFreq.set(dblVal);
    }

    public void setAmpFactor(double dblVal) {
        m_attAmpFactor.set(dblVal);
    }

    public void setPhaseOffset(double dblVal) {
        m_attPhaseOffset.set(dblVal);
    }

    /**
     * @param strVal AieldMap file
     */
    public void setFieldMapFile(String strVal) {
        m_attFieldMapFile.set(strVal);
    }

    /**
     * @param dblVal APosition where cavity RF phase is given (m) relative to element's start
     */
    public void setPhasePosition(double dblVal) {
        m_attPhasePosition.set(dblVal);
    }

    /**
     * @param dblVal Additional offset of the position where cavity RF phase is given (m)
     */
    public void setGapOffset(double dblVal) {
        m_attGapOffset.set(dblVal);
    }
}
