/*
 * JcaChannelServer.java
 *
 * Created on October 21, 2013, 9:37 AM
 */

package xal.plugin.jcaserver;

import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.cas.ServerContext;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;

/**
 * Concrete implementation of ChannelServer backed by JCA.
 * 
 * @version 0.1 13 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class JcaServer{

    /** CA Server context */
    final private ServerContext CONTEXT;

    /** CA Server */
    final private DefaultServerImpl SERVER;

    /** Constructor 
     * @throws CAException exception while creating server context */
    public JcaServer() throws CAException  {
        // Create server implementation
        SERVER = new DefaultServerImpl();

        // Create a context with default configuration values.
        CONTEXT = JCALibrary.getInstance().createServerContext(JCALibrary.CHANNEL_ACCESS_SERVER_JAVA, SERVER);
    }

    /**
     * Registers a process variable for a double array and returns it (Unwrapped).
     * 
     * @param pv
     *            name of the proccessVariable
     * @param initialArray
     *            initial value.
     * @return created ServerProccessVariable
     */
    public ServerMemoryProcessVariable registerRawPV(final String pv,  Object initialArray) {
        if (!initialArray.getClass().isArray()){
            throw new IllegalArgumentException("Expectred array of primitive type");           
        }
        
        final ServerMemoryProcessVariable serverMemoryProcessVariable = new ServerMemoryProcessVariable(pv, null, initialArray);
        SERVER.registerProcessVaribale(serverMemoryProcessVariable);
        return serverMemoryProcessVariable;
    }

    public void printInfo() {
        System.out.println(CONTEXT.getVersion().getVersionString());
        CONTEXT.printInfo();
    }
    
    
}
