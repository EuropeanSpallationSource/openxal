/*
 * Main.java
 *
 * Created on Wed Jan 14 13:03:12 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import java.util.Date;

/**
 * Main
 *
 * @author tap
 */
public class Main {

    /**
     * The time at which the application was launched
     */
    protected static final Date LAUNCH_TIME;

    /**
     * The MPS Model
     */
    protected MPSModel model;

    /**
     * Static initializer
     */
    static {
        LAUNCH_TIME = new Date();
    }

    /**
     * Main Constructor
     */
    public Main() {
        model = new MPSModel();
    }

    /**
     * run the service by starting the logger
     */
    protected void run() {
        new MPSService(model);
    }

    /**
     * Main entry point to the service. Run the service.
     *
     * @param args The launch arguments to the service.
     */
    public static void main(String[] args) {
        new Main().run();
    }

    /**
     * Get the time when this application was launched.
     *
     * @return the time when this application was launched
     */
    public static Date getLaunchTime() {
        return LAUNCH_TIME;
    }
}
