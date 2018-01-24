package xal.extension.jels.smf.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.extension.jels.smf.attr.ESSFieldMapBucket;
import xal.ca.ChannelFactory;
import xal.smf.attr.AttributeBucket;
import xal.smf.impl.RfCavity;
import xal.smf.impl.RfGap;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 * ESS implementation of Field Maps.
 *
 * @author Ivo List <ivo.list@cosylab.com>
 *
 */
public class ESSFieldMap extends RfGap {

    public static final String s_strType = "FM";

    private static final Logger LOGGER = Logger.getLogger(ESSFieldMap.class.getName());

    /*
     *  Local Attributes
     */
    // FieldMap parameters
    protected ESSFieldMapBucket m_bucFieldMap;
    // field profile     
    protected FieldProfile fieldProfile;

    static {
        registerType();
    }

    public ESSFieldMap(String strId) {
        this(strId, null);
    }

    public ESSFieldMap(String strId, ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setFieldMapBucket(new ESSFieldMapBucket());
        // remove RFGap bucket
        m_mapAttrs.remove(getRfGap().getType());
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
        typeManager.registerType(ESSFieldMap.class, s_strType);
        typeManager.registerType(ESSFieldMap.class, "fieldmap");
    }

    /*
     *  Attributes
     */
    public ESSFieldMapBucket getFieldMapBucket() {
        return m_bucFieldMap;
    }

    public final void setFieldMapBucket(ESSFieldMapBucket buc) {
        m_bucFieldMap = buc;
        super.addBucket(buc);
    }

    /**
     * Override AcceleratorNode implementation to check for a ESSFieldMapBucket
     *
     * @param buc
     */
    @Override
    public void addBucket(AttributeBucket buc) {
        if (buc.getClass().equals(ESSFieldMapBucket.class)) {
            setFieldMapBucket((ESSFieldMapBucket) buc);
        }

        super.addBucket(buc);
    }

    /**
     * @return Electric field intensity factor
     */
    public double getXelmax() {
        return m_bucFieldMap.getXelmax();
    }

    /**
     * @return Default (design) cavity RF phase (deg)
     */
    public double getPhase() {
        return m_bucFieldMap.getPhase();
    }

    /**
     * @return Design cavity resonant frequency (MHz)
     */
    public double getFrequency() {
        return m_bucFieldMap.getFrequency();
    }

    public double getAmpFactor() {
        return m_bucFieldMap.getAmpFactor();
    }

    /**
     * @return FieldMap file
     */
    public String getFieldMapFile() {
        return m_bucFieldMap.getFieldMapFile();
    }

    /**
     * @return Position + gapOffset where cavity RF phase is given (m) relative
     * to element's start
     */
    public double getPhasePosition() {
        return m_bucFieldMap.getPhasePosition() + m_bucFieldMap.getGapOffset();
    }

    /**
     * @param dblVal Position + gapOffset where cavity RF phase is given (m)
     * relative to element's start
     */
    public void setPhasePosition(double dblVal) {
        m_bucFieldMap.setPhasePosition(dblVal);
    }

    /**
     * @param dblVal Electric field intensity factor
     */
    public void setXelmax(double dblVal) {
        m_bucFieldMap.setXelmax(dblVal);
    }

    /**
     * @param dblVal Default (design) cavity RF phase (deg)
     */
    public void setPhase(double dblVal) {
        m_bucFieldMap.setPhase(dblVal);
    }

    /**
     * @param dblVal Design cavity resonant frequency (MHz)
     */
    public void setFrequency(double dblVal) {
        m_bucFieldMap.setFrequency(dblVal);
    }

    /**
     *
     * @param dblVal
     */
    public void setAmpFactor(double dblVal) {
        m_bucFieldMap.setAmpFactor(dblVal);
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
    public FieldProfile getFieldProfile() {
        return fieldProfile;
    }

    /**
     * @param fieldProfile Field profile
     */
    public void setFieldProfile(FieldProfile fieldProfile) {
        this.fieldProfile = fieldProfile;
    }

    /**
     * Loads the field profile if necessary
     *
     * @param adaptor
     */
    @Override
    public void update(DataAdaptor adaptor) {
        super.update(adaptor);
        try {
            fieldProfile = FieldProfile.getInstance(new URI(((XmlDataAdaptor) adaptor).document().getDocumentURI()).resolve(getFieldMapFile() + ".edz").toString());
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "An error occurred trying to update the field profile.", e);
        }
    }

    @Override
    public double toGapAmpFromCavityAmp(final double cavityAmp) {
        return cavityAmp * getFieldMapBucket().getAmpFactor();
    }

    @Override
    public double toGapPhaseFromCavityPhase(final double cavityPhase) {
        return cavityPhase + getFieldMapBucket().getPhaseOffset();
    }

    @Override
    public double toE0TLFromGapField(final double field) {
        final RfCavity cavity = (RfCavity) this.getParent();

        try {
            return field * getXelmax() / (fieldProfile.getE0L(cavity.getCavFreq()) / fieldProfile.getLength());
        } catch (IllegalArgumentException exception) {
            LOGGER.log(Level.INFO, "An error occurred when calculating the E0TL.", exception);
            return 0;
        }
    }
}
