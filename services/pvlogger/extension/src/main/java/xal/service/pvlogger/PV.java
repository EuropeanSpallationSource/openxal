/*
 * PV.java
 *
 * Created on Fri Dec 12 15:02:32 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.service.pvlogger;

/**
 * PV
 *
 * @author tap
 */
public class PV {

    protected long id;
    protected String address;

    /**
     * Constructor
     *
     * @param id the unique identifier
     * @param address the PV address
     */
    public PV(long id, String address) {
        this.id = id;
        this.address = address;
    }

    /**
     * Get the id
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Get the PV address
     *
     * @return the PV address
     */
    String getAddress() {
        return address;
    }
}
