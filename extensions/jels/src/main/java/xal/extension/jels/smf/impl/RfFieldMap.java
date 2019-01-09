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
package xal.extension.jels.smf.impl;

import xal.smf.ISplittable;
import xal.extension.jels.smf.attr.FieldMapBucket;
import xal.ca.ChannelFactory;
import xal.smf.attr.AttributeBucket;
import xal.smf.impl.RfGap;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 * General RF Field Map element. It can be used with any type of
 * {@link xal.extension.jels.smf.impl.FieldMap} objects.
 * <p>
 * This element has to buckets:
 * <ul>
 * <li>RFGap: contains rf parameters.</li>
 * <li>FieldMapBucket: includes the filename of the field map file and
 * information about its type.</li>
 * </ul>
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 *
 */
public class RfFieldMap extends RfGap implements ISplittable {

    public static final String s_strType = "RFM";

    /*
     *  Local Attributes
     */
    // FieldMap parameters
    protected FieldMapBucket m_bucFieldMap;
    // field map     
    private FieldMap fieldMap;

    static {
        registerType();
    }

    public RfFieldMap(String strId) {
        this(strId, null);
    }

    public RfFieldMap(String strId, ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setFieldMapBucket(new FieldMapBucket());
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
        typeManager.registerTypes(RfFieldMap.class, s_strType, "rffieldmap");
    }

    /*
     *  Attributes
     */
    public FieldMapBucket getFieldMapBucket() {
        return m_bucFieldMap;
    }

    public final void setFieldMapBucket(FieldMapBucket buc) {
        m_bucFieldMap = buc;
        super.addBucket(buc);
    }

    /**
     * Override AcceleratorNode implementation to check for a FieldMapBucket
     *
     * @param buc
     */
    @Override
    public void addBucket(AttributeBucket buc) {
        if (buc.getClass().equals(FieldMapBucket.class)) {
            setFieldMapBucket((FieldMapBucket) buc);
        }

        super.addBucket(buc);
    }

    /**
     * @return FieldMap file
     */
    public String getFieldMapFile() {
        return m_bucFieldMap.getFieldMapFile();
    }

    /**
     * @param strVal FieldMap file
     */
    public void setFieldMapFile(String strVal) {
        m_bucFieldMap.setFieldMapFile(strVal);
    }

    /**
     * @return Field profile
     */
    public FieldMap getFieldMap() {
        return fieldMap;
    }

    /**
     * @param fieldMap
     */
    public void setFieldMap(FieldMap fieldMap) {
        this.fieldMap = fieldMap;
    }

    @Override
    public double toE0TLFromGapField(final double field) {
        return field * m_bucRfGap.getLength();
    }

    /**
     * Loads the field profile if necessary
     *
     * @param adaptor
     */
    @Override
    public void update(DataAdaptor adaptor) {
        super.update(adaptor);
        fieldMap = FieldMapFactory.getInstance(((XmlDataAdaptor) adaptor).document().getDocumentURI(),
                getFieldMapFile(), m_bucFieldMap.getDynamic(),
                m_bucFieldMap.getFieldType(), m_bucFieldMap.getDimensions());
    }

    @Override
    public double[] getLongitudinalPositions() {
        return fieldMap.getLongitudinalPositions();
    }
    
    @Override
    public double getSliceLength(){
        return fieldMap.getSliceLength();
    }
}
