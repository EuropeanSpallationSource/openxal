package se.lu.esss.ics.jels.smf.impl;

import se.lu.esss.ics.jels.smf.attr.ESSFieldMapBucket;
import xal.smf.AcceleratorNode;
import xal.smf.attr.AttributeBucket;
import xal.smf.impl.qualify.ElementTypeManager;

public class ESSFieldMap extends AcceleratorNode {
	public static final String      s_strType = "FM";
	
    /*
     *  Local Attributes
     */

    protected ESSFieldMapBucket           m_bucFieldMap;           // FieldMap parameters
    
    static {
        registerType();
    }
    
    public ESSFieldMap(String strId) {
		super(strId);
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

    public double   getXelmax()  { return m_bucFieldMap.getXelmax(); }
    public double   getPhase()      { return m_bucFieldMap.getPhase(); }
    public double   getFrequency()  { return m_bucFieldMap.getFrequency(); }
    public String   getFieldMapFile()  { return m_bucFieldMap.getFieldMapFile(); }
    
    public void setXelmax(double dblVal)  { m_bucFieldMap.setXelmax(dblVal); }
    public void setPhase(double dblVal)      { m_bucFieldMap.setPhase(dblVal); }
    public void setFrequency(double dblVal)  { m_bucFieldMap.setFrequency(dblVal); }
    public void setFieldMapFile(String strVal)  { m_bucFieldMap.setFieldMapFile(strVal); }
}
