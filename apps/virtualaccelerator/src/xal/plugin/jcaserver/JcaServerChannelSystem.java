/*
 * JcaSystem.java
 *
 * Created on August 27, 2002, 2:38 PM
 */

package xal.plugin.jcaserver;

import gov.aps.jca.cas.ServerContext;
import xal.ca.ChannelSystem;

/**
 * JcaServerChannelSystem is the same as JcaSystem
 * 
 * @version 0.1 13 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
class JcaServerChannelSystem extends ChannelSystem {

    /** Java Channel Access Context */
    private ServerContext JCA_CONTEXT;

 
    /** Constructor */
    public JcaServerChannelSystem(ServerContext JCA_CONTEXT) {
        this.JCA_CONTEXT = JCA_CONTEXT;
    }

    @Override
    public void setDebugMode(boolean debugFlag) {
        // not used
    }

    @Override
    public void flushIO() {
    }

    @Override
    public boolean pendIO(double timeout) {
        return true;
    }

    @Override
    public void pendEvent(double timeout) {
    }

    @Override
    public void printInfo() {
        JCA_CONTEXT.printInfo();
    }
}
