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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.ChannelFactory;
import xal.extension.jels.smf.attr.FieldMapBucket;
import xal.smf.IFileBasedFieldMap;
import xal.smf.ISplittable;
import xal.smf.attr.AttributeBucket;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 * General magnetic Field Map element. It can be used with any type of
 * {@link xal.extension.jels.smf.impl.FieldMap} objects.
 * <p>
 * This element has to buckets:
 * <ul>
 * <li>MagBucket: contains magnetic field parameters.</li>
 * <li>FieldMapBucket: includes the filename of the field map file and
 * information about its type.</li>
 * </ul>
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 *
 */
public class MagFieldMap extends Electromagnet implements ISplittable, IFileBasedFieldMap {

    public static final String TYPE = "MFM";

    // FieldMap parameters
    protected FieldMapBucket bucFieldMap;
    // field map
    private FieldMap fieldMap;

    static {
        registerType();
    }

    public MagFieldMap(String strId) {
        this(strId, null);
    }

    public MagFieldMap(String strId, ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setFieldMapBucket(new FieldMapBucket());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerTypes(MagFieldMap.class, TYPE, "magfieldmap");
    }

    public FieldMapBucket getFieldMapBucket() {
        return bucFieldMap;
    }

    public final void setFieldMapBucket(FieldMapBucket buc) {
        bucFieldMap = buc;
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
        return bucFieldMap.getFieldMapFile();
    }

    /**
     * @param strVal FieldMap file
     */
    public void setFieldMapFile(String strVal) {
        bucFieldMap.setFieldMapFile(strVal);
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
    public void update(DataAdaptor adaptor) {
        super.update(adaptor);
        String fieldMapPath = null;

        try {
            fieldMapPath = new URI(((XmlDataAdaptor) adaptor).document().getDocumentURI()).resolve(".").toString();
        } catch (URISyntaxException ex) {
            Logger.getLogger(RfFieldMap.class.getName()).log(Level.SEVERE, null, ex);
        }

        fieldMap = FieldMapFactory.getInstance(fieldMapPath, getFieldMapFile(),
                bucFieldMap.getDynamic(), bucFieldMap.getFieldType(),
                bucFieldMap.getDimensions(), bucFieldMap.getNumberOfPoints());
    }

    @Override
    public double[] getLongitudinalPositions() {
        return fieldMap.getLongitudinalPositions();
    }

    @Override
    public double getSliceLength() {
        return fieldMap.getSliceLength();
    }

    public void setDimensions(int i) {
        bucFieldMap.setDimensions(i);
    }

    public void setDynamic(boolean b) {
        bucFieldMap.setDynamic(b);
    }

    @Override
    public void writeFieldMap(String fieldMapPath) {
        try {
            if (!new File(fieldMapPath, getFieldMapFile()).exists()) {
                fieldMap.saveFieldMap(fieldMapPath, getFieldMapFile());
            }
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(RfFieldMap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
