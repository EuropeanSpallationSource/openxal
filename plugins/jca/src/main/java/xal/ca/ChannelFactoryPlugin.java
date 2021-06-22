/*
 * ChannelFactoryPlugin.java
 *
 * Created on October 18, 2013
 */

package xal.ca;

import xal.plugin.jca.JcaChannelFactory;
import xal.plugin.jca.server.JcaServerChannelFactory;


/**
 * Concrete implementation of ChannelFactory that uses JCA.
 * @author  tap
 * @author Blaz Kranjc <blaz.kranjc@cosylab.com>
 */
public class ChannelFactoryPlugin {
    /**
	 * Instantiate a new ChannelFactory
	 * @return a new channel factory
	 */
    public static ChannelFactory getChannelFactoryInstance() {
        return new JcaChannelFactory();
    }

    /**
     * Instantiate a new ServerChannelFactory
     * 
     * @return a new serverChannel factory
     */
    public static ChannelFactory getServerChannelFactoryInstance() {
        return new JcaServerChannelFactory();
    }
}
