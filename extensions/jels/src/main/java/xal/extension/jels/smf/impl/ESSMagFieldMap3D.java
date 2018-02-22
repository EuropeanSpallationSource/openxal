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
package xal.extension.jels.smf.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.extension.jels.smf.attr.ESSMagFieldMap3DBucket;
import xal.ca.ChannelFactory;
import xal.smf.attr.AttributeBucket;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 * ESS implementation for 3D magnetic Field Maps.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 *
 */
public class ESSMagFieldMap3D extends Electromagnet {

    public static final String s_strType = "MFM";

    private static final Logger LOGGER = Logger.getLogger(ESSMagFieldMap3D.class.getName());
    
    /*
     *  Local Attributes
     */
    // FieldMap parameters
    protected ESSMagFieldMap3DBucket m_bucMagFieldMap3D;
    // x component of the field profile  
    protected MagFieldProfile3D fieldProfileX;
    // y component of the field profile
    protected MagFieldProfile3D fieldProfileY;
    // z component of the field profile
    protected MagFieldProfile3D fieldProfileZ;

    static {
        registerType();
    }

    public ESSMagFieldMap3D(String strId) {
        this(strId, null);
    }

    public ESSMagFieldMap3D(String strId, ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setMagFieldMap3DBucket(new ESSMagFieldMap3DBucket());
        // remove MagBucket bucket
        m_mapAttrs.remove(getMagBucket().getType());
    }

    @Override
    public String getType() {
        return s_strType;
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(ESSMagFieldMap3D.class, s_strType);
        typeManager.registerType(ESSMagFieldMap3D.class, "magfieldmap");
    }

    /*
     *  Attributes
     */
    public ESSMagFieldMap3DBucket getMagFieldMap3DBucket() {
        return m_bucMagFieldMap3D;
    }

    public void setMagFieldMap3DBucket(ESSMagFieldMap3DBucket buc) {
        m_bucMagFieldMap3D = buc;
        super.addBucket(buc);
    }

    /**
     * Override AcceleratorNode implementation to check for a ESSMagFieldMap3D
     */
    @Override
    public void addBucket(AttributeBucket buc) {
        if (buc.getClass().equals(ESSMagFieldMap3DBucket.class)) {
            setMagFieldMap3DBucket((ESSMagFieldMap3DBucket) buc);
        }

        super.addBucket(buc);
    }

    /**
     * Magnetic field intensity factor
     */
    @Override
    public double getDesignField() {
        return m_bucMagFieldMap3D.getXmagmax();
    }

    @Override
    public double getDfltField() {
        return m_bucMagFieldMap3D.getXmagmax();
    }

    /**
     * Electric field intensity factor
     */
    public void setDesignField(double dblVal) {
        m_bucMagFieldMap3D.setXmagmax(dblVal);
    }

    @Override
    public void setDfltField(double dblVal) {
        m_bucMagFieldMap3D.setXmagmax(dblVal);
    }

    @Override
    public double getEffLength() {
        return m_dblLen;
    }

    /**
     * FieldMap file
     */
    public String getFieldMapFile() {
        return m_bucMagFieldMap3D.getFieldMapFile();
    }

    public void setFieldMapFile(String strVal) {
        m_bucMagFieldMap3D.setFieldMapFile(strVal);
    }

    /**
     * Field profile
     */
    public MagFieldProfile3D getMagFieldProfileX() {
        return fieldProfileX;
    }

    /**
     * Field profile
     */
    public MagFieldProfile3D getMagFieldProfileY() {
        return fieldProfileY;
    }

    /**
     * Field profile
     */
    public MagFieldProfile3D getMagFieldProfileZ() {
        return fieldProfileZ;
    }

    /**
     * Field profile
     */
    public void setMagFieldProfileX(MagFieldProfile3D fieldProfileX) {
        this.fieldProfileX = fieldProfileX;
    }

    /**
     * Field profile
     */
    public void setMagFieldProfileY(MagFieldProfile3D fieldProfileY) {
        this.fieldProfileY = fieldProfileY;
    }

    /**
     * Field profile
     */
    public void setMagFieldProfileZ(MagFieldProfile3D fieldProfileZ) {
        this.fieldProfileZ = fieldProfileZ;
    }

    /**
     * Loads the field profile if necessary
     */
    @Override
    public void update(DataAdaptor adaptor) {
        super.update(adaptor);
        try {
            URI fieldProfileXURI = new URI(((XmlDataAdaptor) adaptor).document().getDocumentURI()).resolve(getFieldMapFile() + ".bsx");
            URI fieldProfileYURI = new URI(((XmlDataAdaptor) adaptor).document().getDocumentURI()).resolve(getFieldMapFile() + ".bsy");
            URI fieldProfileZURI = new URI(((XmlDataAdaptor) adaptor).document().getDocumentURI()).resolve(getFieldMapFile() + ".bsz");

            setMagFieldProfileX(MagFieldProfile3D.getInstance(fieldProfileXURI.toString()));
            setMagFieldProfileY(MagFieldProfile3D.getInstance(fieldProfileYURI.toString()));
            setMagFieldProfileZ(MagFieldProfile3D.getInstance(fieldProfileZURI.toString()));
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "An error occurred trying to update the field profile.", e);
        }
    }
}
