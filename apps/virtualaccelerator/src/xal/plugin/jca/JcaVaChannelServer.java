/*
 * JcaChannelServer.java
 *
 * Created on October 21, 2013, 9:37 AM
 */

package xal.plugin.jca;

import gov.aps.jca.JCALibrary;
import gov.aps.jca.cas.ServerContext;
import gov.aps.jca.dbr.DBRType;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;

/**
 * Concrete implementation of ChannelServer backed by JCA.
 * 
 * @version 0.1 13 Jul 2015
 * @author Blaž Kranjc <blaz.kranjc@cosylab.com>
 */
public class JcaVaChannelServer{

    /** CA Server context */
    final private ServerContext CONTEXT;

    /** CA Server */
    final private DefaultServerImpl SERVER;

    /** Constructor */
    public JcaVaChannelServer() throws Exception {
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
            initialArray = new Object[]{initialArray};
        }
        final ServerMemoryProcessVariable serverMemoryProcessVariable = new ServerMemoryProcessVariable(pv, null,
                DBRType.DOUBLE, initialArray);
        // TODO SET DBRType to automatically set itself (not hardcoded DOUBLE).
        SERVER.registerProcessVaribale(serverMemoryProcessVariable);
        return serverMemoryProcessVariable;
    }

    public void printInfo() {
        System.out.println(CONTEXT.getVersion().getVersionString());
        CONTEXT.printInfo();
    }
    
    
}
