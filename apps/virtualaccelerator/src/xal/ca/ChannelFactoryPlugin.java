package xal.ca;

import xal.plugin.jca.JcaChannelFactory;

/**
 * Concrete implementation of ChannelFactory that uses JCA.
 * 
 * @version 0.2 13 Jul 2015
 * @author tap
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class ChannelFactoryPlugin {
    /**
     * Instantiate a new ChannelFactory
     * 
     * @return a new serverChannel factory
     */
    static public ChannelFactory getChannelFactoryInstance() {
        return new JcaChannelFactory();
    }
}
