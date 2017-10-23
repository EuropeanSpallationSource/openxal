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
     * Magnetic field intensity factor
     */
    public double getXmagmax() {
        return m_attXmagmax.getDouble();
    }

    /**
     * FieldMap file
     */
    public String getFieldMapFile() {
        return m_attFieldMapFile.getString();
    }

    /**
     * Magnetic field intensity factor
     */
    public void setXmagmax(double dblVal) {
        m_attXmagmax.set(dblVal);
    }

    /**
     * FieldMap file
     */
    public void setFieldMapFile(String strVal) {
        m_attFieldMapFile.set(strVal);
    }
}
