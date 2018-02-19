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
 * A set of ESSSolFieldMap attributes.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class ESSSolFieldMapBucket extends AttributeBucket {

    private static final long serialVersionUID = 1;

    public final static String c_strType = "solfieldmap";

    final static String[] c_arrNames = {
        "xmagmax",
        "fieldMapFile"
    };

    /*
     *  Local Attributes
     */
    /**
     * Magnetic field intensity factor
     */
    private Attribute m_attXmagmax;

    /**
     * FieldMap file
     */
    private Attribute m_attFieldMapFile;

    /**
     * Override virtual to provide type signature
     * @return Type
     */
    @Override
    public String getType() {
        return c_strType;
    }

    @Override
    public String[] getAttrNames() {
        return c_arrNames;
    }

    public ESSSolFieldMapBucket() {
        super();

        m_attXmagmax = new Attribute(1.);
        m_attFieldMapFile = new Attribute("");

        super.registerAttribute(c_arrNames[0], m_attXmagmax);
        super.registerAttribute(c_arrNames[1], m_attFieldMapFile);
    }

    /**
     * @return Magnetic field intensity factor
     */
    public double getXmagmax() {
        return m_attXmagmax.getDouble();
    }

    /**
     * @return FieldMap file
     */
    public String getFieldMapFile() {
        return m_attFieldMapFile.getString();
    }

    /**
     * @param dblVal Magnetic field intensity factor
     */
    public void setXmagmax(double dblVal) {
        m_attXmagmax.set(dblVal);
    }

    /**
     * @param strVal FieldMap file path
     */
    public void setFieldMapFile(String strVal) {
        m_attFieldMapFile.set(strVal);
    }
}
