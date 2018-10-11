package xal.ca;

import xal.plugin.pvaccess.PvAccessChannelFactory;
import xal.plugin.pvaccess.server.PvAccessServerChannelFactory;

/**
 * Concrete implementation of ChannelFactory that uses pvAccess.
 *
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class ChannelFactoryPlugin {

    private ChannelFactoryPlugin() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Instantiate a new ChannelFactory
     *
     * @return a new channel factory
     */
    public static ChannelFactory getChannelFactoryInstance() {
        return new PvAccessChannelFactory();
    }

    /**
     * Instantiate a new ServerChannelFactory
     *
     * @return a new serverChannel factory
     */
    public static ChannelFactory getServerChannelFactoryInstance() {
        return new PvAccessServerChannelFactory();
    }
}
