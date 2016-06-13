package xal.ca;

import xal.plugin.pvaccess.PvAccessChannelFactory;
import xal.plugin.pvaccess.server.PvAccessServerChannelFactory;


/**
 * Concrete implementation of ChannelFactory that uses pvAccess.
 * 
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class ChannelFactoryPlugin {
    /**
	 * Instantiate a new ChannelFactory
	 * @return a new channel factory
	 */
    static public ChannelFactory getChannelFactoryInstance() {
        return new PvAccessChannelFactory();
    }

    /**
     * Instantiate a new ServerChannelFactory
     * 
     * @return a new serverChannel factory
     */
    static public ChannelFactory getServerChannelFactoryInstance() {
        return new PvAccessServerChannelFactory();
    }
}
