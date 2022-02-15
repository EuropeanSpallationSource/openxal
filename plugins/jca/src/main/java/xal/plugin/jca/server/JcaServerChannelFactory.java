package xal.plugin.jca.server;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;

import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.cas.ServerContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.ca.ChannelFactory;
import xal.ca.ChannelSystem;

/**
 * Concrete implementation of ChannelFactory that uses JCA.
 *
 * @version 0.1 13 Jul 2015
 * @author Blaz Kranjc <blaz.kranjc@cosylab.com>
 */
public class JcaServerChannelFactory extends ChannelFactory {

    private static final Logger LOGGER = Logger.getLogger(JcaServerChannelFactory.class.getName());

    /**
     * JCA channel system
     */
    private JcaServerChannelSystem jcaServerSystem;

    /**
     * Channel server for creating and holding PVs.
     */
    private DefaultServerImpl channelServer;

    /**
     * CA Server context
     */
    private ServerContext context;

    /**
     * Constructor
     */
    public JcaServerChannelFactory() {
        try {
            // Create server implementation
            channelServer = new DefaultServerImpl();

            // Create a context with default configuration values.
            context = JCALibrary.getInstance().createServerContext(JCALibrary.CHANNEL_ACCESS_SERVER_JAVA, channelServer);
            jcaServerSystem = new JcaServerChannelSystem(context);
        } catch (CAException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    /**
     * Create a JCA server channel for the specified PV
     *
     * @param signalName The name of the PV signal
     */
    @Override
    protected xal.ca.Channel newChannel(final String signalName) {
        return (xal.ca.Channel) new JcaServerChannel(signalName, channelServer);
    }

    /**
     * JcaSystem handles static behavior of Jca channels
     *
     * @return the JCA channel system
     */
    @Override
    protected ChannelSystem channelSystem() {
        return jcaServerSystem;
    }

    /**
     * print information about this channel factory
     */
    @Override
    public void printInfo() {
        jcaServerSystem.printInfo();
    }

    @Override
    public boolean init() {
        // nothing to initialize
        return true;
    }

    @Override
    public void dispose() {
        context.dispose();
    }

}
