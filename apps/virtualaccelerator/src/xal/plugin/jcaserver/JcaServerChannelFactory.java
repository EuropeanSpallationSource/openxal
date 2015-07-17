package xal.plugin.jcaserver;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;

import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.cas.ServerContext;
import xal.ca.ChannelFactory;
import xal.ca.ChannelSystem;

/**
 * Concrete implementation of ChannelFactory that uses JCA.
 * 
 * @version 0.1 13 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class JcaServerChannelFactory extends ChannelFactory {
    /** JCA channel system */
    final private JcaServerChannelSystem JCA_SERVER_SYSTEM;
    
    /**
     * Channel server for creating and holding PVs.
     */
    private DefaultServerImpl CHANNEL_SERVER;
    
    /** CA Server context */
    private ServerContext CONTEXT;

     
    /** Constructor */
    public JcaServerChannelFactory() {
        JCA_SERVER_SYSTEM = new JcaServerChannelSystem();
        
        try {
	        // Create server implementation
	        CHANNEL_SERVER = new DefaultServerImpl();
	
	        // Create a context with default configuration values.
	        CONTEXT = JCALibrary.getInstance().createServerContext(JCALibrary.CHANNEL_ACCESS_SERVER_JAVA, CHANNEL_SERVER);
        } catch (CAException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a JCA server channel for the specified PV
     * 
     * @param signalName
     *            The name of the PV signal
     */
    protected xal.ca.Channel newChannel(final String signalName) {
        // Changes writable to true if name ends with any of the suffixes in {@link #WRITEABLE_CHANNEL_NAME_SUFFIXES}.
    	boolean writable = false;
        
    	// Array of suffixes, the channel can have to be writable.      
        final String[] WRITEABLE_CHANNEL_NAME_SUFFIXES = { "FldSet", "Ctl" };
        
        // We disable writing to all channels whose suffix is not in WRITEABLE_CHANNEL_NAME_SUFFIXES
        for (String suffix : WRITEABLE_CHANNEL_NAME_SUFFIXES) {
            if (signalName.endsWith(suffix)) {
                writable = true;
                break;
            }
        }
        return (xal.ca.Channel) new JcaServerChannel(signalName, CHANNEL_SERVER, writable);
    }

    /**
     * JcaSystem handles static behavior of Jca channels
     * 
     * @return the JCA channel system
     */
    protected ChannelSystem channelSystem() {
        return JCA_SERVER_SYSTEM;
    }

    /** print information about this channel factory */
    public void printInfo() {
        JCA_SERVER_SYSTEM.printInfo();
    }

    @Override
    public boolean init() {
        // nothing to initialize
        return true;
    }
 
}