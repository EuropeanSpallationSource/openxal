package xal.extension.jels.smf.impl;

import java.net.URI;
import java.net.URISyntaxException;

import xal.extension.jels.smf.attr.ESSSolFieldMapBucket;
import xal.ca.ChannelFactory;
import xal.smf.attr.AttributeBucket;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 * ESS implementation of Solenoid Field Maps.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 *
 */
public class ESSSolFieldMap extends Electromagnet {

    public static final String s_strType = "SFM";

    /*
     *  Local Attributes
     */
    protected ESSSolFieldMapBucket m_bucSolFieldMap;           // FieldMap parameters
    protected FieldProfile2D fieldProfileR; // radial component of the field profile
    protected FieldProfile2D fieldProfileZ; // longitudianl component of the field profile

    static {
        registerType();
    }

    public ESSSolFieldMap(String strId) {
        this(strId, null);
    }

    public ESSSolFieldMap(String strId, ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setSolFieldMapBucket(new ESSSolFieldMapBucket());
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
        typeManager.registerType(ESSSolFieldMap.class, s_strType);
        typeManager.registerType(ESSSolFieldMap.class, "solfieldmap");
    }

    /*
     *  Attributes
     */
    public ESSSolFieldMapBucket getSolFieldMapBucket() {
        return m_bucSolFieldMap;
    }

    public void setSolFieldMapBucket(ESSSolFieldMapBucket buc) {
        m_bucSolFieldMap = buc;
        super.addBucket(buc);
    }

    /**
     * Override AcceleratorNode implementation to check for a ESSFieldMapBucket
     */
    @Override
    public void addBucket(AttributeBucket buc) {
        if (buc.getClass().equals(ESSSolFieldMapBucket.class)) {
            setSolFieldMapBucket((ESSSolFieldMapBucket) buc);
        }

        super.addBucket(buc);
    }

    /**
     * Magnetic field intensity factor
     */
    @Override
    public double getDesignField() {
        return m_bucSolFieldMap.getXmagmax();
    }

    @Override
    public double getDfltField() {
        return m_bucSolFieldMap.getXmagmax();
    }

    /**
     * Electric field intensity factor
     */
    public void setDesignField(double dblVal) {
        m_bucSolFieldMap.setXmagmax(dblVal);
    }

    @Override
    public void setDfltField(double dblVal) {
        m_bucSolFieldMap.setXmagmax(dblVal);
    }

    @Override
    public double getEffLength() {
        return m_dblLen;
    }
    
    /**
     * FieldMap file
     */
    public String getFieldMapFile() {
        return m_bucSolFieldMap.getFieldMapFile();
    }

    public void setFieldMapFile(String strVal) {
        m_bucSolFieldMap.setFieldMapFile(strVal);
    }

    /**
     * Field profile
     */
    public FieldProfile2D getFieldProfileR() {
        return fieldProfileR;
    }

    /**
     * Field profile
     */
    public void setFieldProfileR(FieldProfile2D fieldProfileR) {
        this.fieldProfileR = fieldProfileR;
    }

    /**
     * Field profile
     */
    public FieldProfile2D getFieldProfileZ() {
        return fieldProfileZ;
    }

    /**
     * Field profile
     */
    public void setFieldProfileZ(FieldProfile2D fieldProfileZ) {
        this.fieldProfileZ = fieldProfileZ;
    }

    /**
     * Loads the field profile if necessary
     */
    @Override
    public void update(DataAdaptor adaptor) throws NumberFormatException {
        super.update(adaptor);
        try {
            URI fieldProfileRURI = new URI(((XmlDataAdaptor) adaptor).document().getDocumentURI()).resolve(getFieldMapFile() + ".bsr");
            URI fieldProfileZURI = new URI(((XmlDataAdaptor) adaptor).document().getDocumentURI()).resolve(getFieldMapFile() + ".bsz");
                        
            setFieldProfileR(FieldProfile2D.getInstance(fieldProfileRURI.toString()));
            setFieldProfileZ(FieldProfile2D.getInstance(fieldProfileZURI.toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
