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

import xal.extension.jels.smf.impl.FieldMapFactory.FieldType;
import xal.smf.attr.Attribute;
import xal.smf.attr.AttributeBucket;

/**
 * A set of FieldMap attributes. Valid for any type of field map. Specific
 * parameters must go in a separate attribute bucket.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class FieldMapBucket extends AttributeBucket {

    private static final long serialVersionUID = 1;

    public static final String c_strType = "fieldmap";

    static final String[] c_arrNames = {
        "fieldMapFile",
        "dynamic",
        "fieldType",
        "dimensions"
    };

    /**
     * FieldMap file
     */
    private Attribute m_attFieldMapFile;

    /**
     * Boolean describing if the field is static (false) or dynamic (true).
     */
    private Attribute m_attDynamic;
    /**
     * String specifying whether it is electric or magnetic.
     */
    private Attribute m_attFieldType;
    /**
     * Number of dimensions of the field map (1, 2, or 3).
     */
    private Attribute m_attDimensions;

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

    public FieldMapBucket() {
        super();

        m_attFieldMapFile = new Attribute("");
        m_attDynamic = new Attribute(false);
        m_attFieldType = new Attribute("");
        m_attDimensions = new Attribute(0);

        super.registerAttribute(c_arrNames[0], m_attFieldMapFile);
        super.registerAttribute(c_arrNames[1], m_attDynamic);
        super.registerAttribute(c_arrNames[2], m_attFieldType);
        super.registerAttribute(c_arrNames[3], m_attDimensions);
    }

    public String getFieldMapFile() {
        return m_attFieldMapFile.getString();
    }

    public void setFieldMapFile(String strVal) {
        m_attFieldMapFile.set(strVal);
    }

    public Boolean getDynamic() {
        return m_attDynamic.getBoolean();
    }

    public void setDynamic(Boolean strVal) {
        m_attDynamic.set(strVal);
    }

    public FieldType getFieldType() {
        String strFieldType = m_attFieldType.getString();
        if ("electric".equals(strFieldType) || "Electric".equals(strFieldType) || "e".equals(strFieldType) || "E".equals(strFieldType)) {
            return FieldType.ELECTRIC;
        } else if ("magnetic".equals(strFieldType) || "Magnetic".equals(strFieldType) || "b".equals(strFieldType) || "B".equals(strFieldType)) {
            return FieldType.MAGNETIC;
        }
        return null;
    }

    public void setFieldType(FieldType ftVal) {
        if (ftVal == FieldType.ELECTRIC) {
            m_attFieldType.set("electric");
        } else if (ftVal == FieldType.MAGNETIC) {
            m_attFieldType.set("magnetic");
        }
    }

    public int getDimensions() {
        return m_attDimensions.getInteger();
    }

    public void setDimensions(int intVal) {
        m_attDimensions.set(intVal);
    }
}
