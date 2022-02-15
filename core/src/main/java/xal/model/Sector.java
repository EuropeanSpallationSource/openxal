/*
 * Created on May 20, 2004
 *
 */
package xal.model;

import xal.model.elem.ElementSeq;

/**
 * Represents a logical sector of beamline. This is essentially a semantic
 * concept since it simply extends the <code>ElementSeq</code> base class.
 *
 * @author Christopher K. Allen
 * @since May 20, 2004
 */
public class Sector extends ElementSeq {

    /*
     *  Global Constants
     */
    /**
     * the string type identifier for all Sector objects
     */
    public static final String TYPE = "Sector";

    /*
     * Initialization
     */
    /**
     * Default constructor. The <code>Sector</code> object is empty and has no
     * string identifier.
     */
    public Sector() {
        super(TYPE);
    }

    /**
     * Create new <code>Sector</code> object and initialize the string
     * identifier.
     *
     * @param strId string identifier of this sector
     */
    public Sector(String strId) {
        super(TYPE, strId);
    }

    /**
     * Create new <code>Sector</code> object specifying the amount of storage to
     * reserve for the direct child components. (If not specified a default
     * value is used.)
     *
     * @param strId string identifier of this sector
     * @param szReserve number of storage positions to reserve for children
     */
    public Sector(String strId, int szReserve) {
        super(TYPE, strId, szReserve);
    }
}
