//-*-jde-*-
package edu.stanford.slac.meme.service.optics;

import java.util.logging.Logger;

import edu.stanford.slac.meme.support.sys.MemeConstants;

/**
 * Entity defines how to get the "device name" part and "property" part from an MEME EPICS V4 PV.
 * 
 * @author Greg White sometime
 * @author Greg White 03/03/2015 upcase entity parts so using code can assume 1 case.
 */
class Entity {
    // Acquire the logging interface
    private static final Logger logger = Logger.getLogger(Entity.class.getName());
    private String m_instance = null;
    private String m_attribute = null;

    Entity(final String pvname) {
        logger.fine("Entity constructor received pvame:\"" + pvname + "\"");
        parse(pvname);
    }

    Entity(final String pvname, final Boolean instanceOnly) {
        logger.fine("Entity instanceOnly constructor received pvame:\"" + pvname + "\"");
        if (instanceOnly == true) {
            if (pvname.indexOf(MemeConstants.SEPARATOR) >= 0) {
                throw new IllegalArgumentException(String.format(MemeConstants.UNEXPECTEDSEP, pvname, MemeConstants.SEPARATOR));
            } else {
                m_instance = pvname;
                m_instance.toUpperCase();
            }
        } else
            parse(pvname);
    }

    /**
     * Parses a pvname into an instance part, which should identify a thing, and a attribute part, which should name a
     * property of the thing. For example, in the SLAC process variable naming convention, given the pv name
     * XCOR:LI23:343[:|//]BDES, the "XCOR:LI23:343" identifies a thing, a corrector magent, and "BDES" names an
     * attribute of the corrector - its desired B-field energization. Of course the real world interpretation of these
     * name parts is not something that can be enforced.
     */
    private void parse(final String pvname) {
        // Look for last index of ":". If does not occur
        // then the attribute part has not been given.
    	final int separatorcharindex = pvname.lastIndexOf(MemeConstants.SEPARATOR);
        if (separatorcharindex == 0) {
            m_instance = pvname.toUpperCase();
            m_attribute = null;
        } else {
            m_instance = pvname.substring(0, separatorcharindex).toUpperCase();
            m_attribute = pvname.substring(separatorcharindex + 1).toUpperCase();
        }
    }

    protected String instance() {
        return m_instance;
    }

    protected String attribute() {
        return m_attribute;
    }
}
