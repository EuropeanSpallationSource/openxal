package se.lu.esss.ics.jels.smf.attr;

import xal.smf.attr.Attribute;
import xal.smf.attr.AttributeBucket;



/**
 * A set of FieldMap attributes.
 *
 * @author  Ivo List
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
    
    public final static String  c_strType = "fieldmap"; 

    final static String[]       c_arrNames = { 
    	"xelmax", 
        "phase",
        "freq",                                              
        //"ampFactor",
        //"phaseOffset",
        //"structureMode",
        //"qLoaded",
        //"structureTTF"  	
    	"fieldMapFile"
    };
    
    
    /*
     *  Local Attributes
     */
    
    
    /** Electric field intensity factor */
    private Attribute   m_attXelmax;
    
    /** Default (design) cavity RF phase (deg) */
    private Attribute   m_attPhase;
    
    /** Design cavity resonant frequency (MHz) */
    private Attribute   m_attFreq;
    
    /** FieldMap file */
    private Attribute   m_attFieldMapFile;
    
    
    /*
     *  User Interface
     */
    
    
    /** Override virtual to provide type signature */
    public String getType() { return c_strType; };
    
    public String[] getAttrNames()  { return c_arrNames; };
    
    public ESSFieldMapBucket() {
        super();
        
        m_attXelmax = new Attribute( 0. );
        m_attPhase = new Attribute(0. );
        m_attFreq = new Attribute(0. );
        m_attFieldMapFile = new Attribute("");
	
        super.registerAttribute(c_arrNames[0], m_attXelmax);
        super.registerAttribute(c_arrNames[1], m_attPhase);
        super.registerAttribute(c_arrNames[2], m_attFreq);
        super.registerAttribute(c_arrNames[3], m_attFieldMapFile);
    }
    
    /** Electric field intensity factor */ 
    public double   getXelmax()  { return m_attXelmax.getDouble(); }
    
    /** Default (design) cavity RF phase (deg) */
    public double   getPhase()      { return m_attPhase.getDouble(); }
    
    /** Design cavity resonant frequency (MHz) */
    public double   getFrequency()  { return m_attFreq.getDouble(); }
    
    /** FieldMap file */
    public String   getFieldMapFile()  { return m_attFieldMapFile.getString(); }
    
    
    /** Electric field intensity factor */
    public void setXelmax(double dblVal)  { m_attXelmax.set(dblVal); }
    
    /** Default (design) cavity RF phase (deg) */
    public void setPhase(double dblVal)      { m_attPhase.set(dblVal); }
    
    /** Design cavity resonant frequency (MHz) */
    public void setFrequency(double dblVal)  { m_attFreq.set(dblVal); }
    
    /** FieldMap file */
    public void setFieldMapFile(String strVal)  { m_attFieldMapFile.set(strVal); }
}
