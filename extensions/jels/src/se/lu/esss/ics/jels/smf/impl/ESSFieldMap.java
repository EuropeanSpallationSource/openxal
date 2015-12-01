package se.lu.esss.ics.jels.smf.impl;

import java.net.URI;
import java.net.URISyntaxException;

import se.lu.esss.ics.jels.smf.attr.ESSFieldMapBucket;
import xal.ca.ChannelFactory;
import xal.smf.attr.AttributeBucket;
import xal.smf.impl.Electrostatic;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 * ESS implementation of Field Maps.
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 *
 */
public class ESSFieldMap extends Electrostatic {
	public static final String      s_strType = "FM";
	
    /*
     *  Local Attributes
     */

    protected ESSFieldMapBucket           m_bucFieldMap;           // FieldMap parameters
    protected FieldProfile	fieldProfile; // field profile
    
    static {
        registerType();
    }
    
    public ESSFieldMap(String strId) {
		this(strId, null);
	}
    
    public ESSFieldMap(String strId, ChannelFactory channelFactory) {
		super(strId, channelFactory);
		setFieldMapBucket(new ESSFieldMapBucket());
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
    
    
    public void setFieldMapBucket(ESSFieldMapBucket buc) { 
        m_bucFieldMap = buc; 
        super.addBucket(buc); 
    }
    
    
    /** Override AcceleratorNode implementation to check for a ESSFieldMapBucket */
    public void addBucket(AttributeBucket buc)  {
        if (buc.getClass().equals( ESSFieldMapBucket.class )) 
        	setFieldMapBucket((ESSFieldMapBucket)buc);

        super.addBucket(buc);
    }

    /** Electric field intensity factor */
    public double   getXelmax()  { return m_bucFieldMap.getXelmax(); }
    
    /** Default (design) cavity RF phase (deg) */
    public double   getPhase()      { return m_bucFieldMap.getPhase(); }
    
    /** Design cavity resonant frequency (MHz) */
    public double   getFrequency()  { return m_bucFieldMap.getFrequency(); }
    
    /** FieldMap file */
    public String   getFieldMapFile()  { return m_bucFieldMap.getFieldMapFile(); }
    
    /** Position + gapOffset where cavity RF phase is given (m) relative to element's start */
    public double   getPhasePosition()      { return m_bucFieldMap.getPhasePosition() + m_bucFieldMap.getGapOffset(); }
    
    /** Electric field intensity factor */
    public void setXelmax(double dblVal)  { m_bucFieldMap.setXelmax(dblVal); }
    
    /** Default (design) cavity RF phase (deg) */
    public void setPhase(double dblVal)      { m_bucFieldMap.setPhase(dblVal); }
    
    /** Design cavity resonant frequency (MHz) */
    public void setFrequency(double dblVal)  { m_bucFieldMap.setFrequency(dblVal); }
    
    /** FieldMap file */
    public void setFieldMapFile(String strVal)  { m_bucFieldMap.setFieldMapFile(strVal); }

    /** Field profile */
    public FieldProfile getFieldProfile() { return fieldProfile; }
    
    /** Field profile */
    public void setFieldProfile(FieldProfile fieldProfile) { this.fieldProfile = fieldProfile; }
    
    /**
     * Loads the field profile if necessary
     */
	@Override
	public void update(DataAdaptor adaptor) throws NumberFormatException {
		super.update(adaptor);
		try {
			fieldProfile = FieldProfile.getInstance(new URI(((XmlDataAdaptor)adaptor).document().getDocumentURI()).resolve(getFieldMapFile()+".edz").toString());			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
