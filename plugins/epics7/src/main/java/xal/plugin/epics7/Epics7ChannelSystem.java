/*
 * Copyright (C) 2020 European Spallation Source ERIC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.plugin.epics7;

import com.cosylab.epics.caj.CAJContext;
import com.cosylab.epics.caj.impl.CAConstants;
import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pva.PVASettings;
import org.epics.pva.client.PVAClient;
import xal.ca.Channel;
import xal.ca.ChannelSystem;
import xal.plugin.epics7.server.Epics7ServerChannelSystem;
import xal.tools.apputils.Preferences;

/**
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelSystem implements ChannelSystem {

    private PVAClient pvaClient;
    private Context caContext;
    protected volatile boolean initialized = false;

    private static final String ADDR_LIST = ".addr_list";
    private static final String SERVER_PORT = ".server_port";
    private static final String CA_ADDR_LIST = "EPICS_CA_ADDR_LIST";
    private static final String PVA_BROADCAST_PORT = "EPICS_PVA_BROADCAST_PORT";
    private static final String PVA_CONN_TMO = "EPICS_PVA_CONN_TMO";
    private static final String PVA_AUTO_ADDR_LIST = "EPICS_PVA_AUTO_ADDR_LIST";
    private static final String PVA_ADDR_LIST = "EPICS_PVA_ADDR_LIST";
    private static final String PVA_NAME_SERVERS = "EPICS_PVA_NAME_SERVERS";
    private static final String PVA_SERVER_PORT = "EPICS_PVA_SERVER_PORT";
    private static final String PVA_ENABLE_IPV6 = "EPICS_PVA_ENABLE_IPV6";
    private static final String PVAS_INTF_ADDR_LIST = "EPICS_PVAS_INTF_ADDR_LIST";

    /**
     * IPv4-only server interface list, used when IPv6 is disabled so that the server does not reject the IPv6 addresses
     * in core-pva's default list.
     */
    private static final String IPV4_INTF_ADDR_LIST = "0.0.0.0 224.0.0.128,1@127.0.0.1";
    private static final String CAS_BEACON_PORT = "EPICS_CAS_BEACON_PORT";
    private static final String CAS_BEACON_ADDR_LIST = "EPICS_CAS_BEACON_ADDR_LIST";
    private static final String CAS_SERVER_PORT = "EPICS_CAS_SERVER_PORT";
    private static final String CAS_ADDR_LIST = "EPICS_CAS_ADDR_LIST";
    private static final String CA_MAX_SEARCH_PERIOD = "EPICS_CA_MAX_SEARCH_PERIOD";
    private static final String CA_MAX_ARRAY_BYTES = "EPICS_CA_MAX_ARRAY_BYTES";
    private static final String CA_SERVER_PORT = "EPICS_CA_SERVER_PORT";
    private static final String CA_REPEATER_PORT = "EPICS_CA_REPEATER_PORT";
    private static final String CA_BEACON_PERIOD = "EPICS_CA_BEACON_PERIOD";
    private static final String CA_CONN_TMO = "EPICS_CA_CONN_TMO";
    private static final String CA_NAME_SERVERS = "EPICS_CA_NAME_SERVERS";
    private static final String CA_AUTO_ADDR_LIST = "EPICS_CA_AUTO_ADDR_LIST";

    protected Epics7ChannelSystem() {
    }

    protected static Epics7ChannelSystem newEpics7ChannelSystem() {
        Epics7ChannelSystem epics7ChannelSystem = new Epics7ChannelSystem();

        epics7ChannelSystem.initialize();

        return epics7ChannelSystem;
    }

    /**
     * Create a channel speaking PV Access.
     */
    public NativeChannel createPvaChannel(String signalName, NativeChannel.ConnectionListener listener) {
        return new PvaNativeChannel(pvaClient, signalName, listener);
    }

    /**
     * Create a channel speaking Channel Access.
     */
    public NativeChannel createCaChannel(String signalName, NativeChannel.ConnectionListener listener) {
        return new CaNativeChannel(caContext, signalName, listener);
    }

    protected void initialize() {
        // Must run before any core-pva class is loaded: PVASettings reads the
        // system properties we set here from a static initialiser.
        loadConfig(false);

        try {
            caContext = JCALibrary.getInstance().createContext(JCALibrary.CHANNEL_ACCESS_JAVA);
            caContext.initialize();
        } catch (CAException ex) {
            Logger.getLogger(Epics7ChannelSystem.class.getName())
                    .log(Level.SEVERE, "Channel Access context could not be created.", ex);
        }

        try {
            pvaClient = new PVAClient();
        } catch (Exception ex) {
            Logger.getLogger(Epics7ChannelSystem.class.getName())
                    .log(Level.SEVERE, "PV Access client could not be created.", ex);
        }

        // Create shutdown hook to close the resource when calling System.exit() or
        // if the process is terminated.
        Thread t = new Thread(this::dispose);
        t.setDaemon(false);
        Runtime.getRuntime().addShutdownHook(t);

        initialized = caContext != null && pvaClient != null;
    }

    /**
     * This method preloads the JCA and PVA configuration. It takes the configuration from Preferences or from
     * environment variables, in that precedence order. It ignores the jca.use_env property and the JCALibrary file.
     *
     * Note that the PV Access library used here has no equivalent of EPICS_PVA_BEACON_PERIOD or
     * EPICS_PVA_MAX_ARRAY_BYTES; those settings are ignored.
     *
     * @param isServer if server configuration has to be loaded.
     */
    public static final void loadConfig(boolean isServer) {
        // Setting jca.use_env=false to load the configuration from system properties
        // that we define now.
        System.setProperty("jca.use_env", "false");

        // core-pva enables IPv6 by default and fails hard with "IPv6 not available"
        // on hosts without it. Open XAL never exposed IPv6 PV Access configuration,
        // so default it off unless the user explicitly asks for it. This must run
        // before PVASettings' static initialiser reads the property.
        if (System.getProperty(PVA_ENABLE_IPV6) == null && System.getenv(PVA_ENABLE_IPV6) == null) {
            System.setProperty(PVA_ENABLE_IPV6, "false");
            PVASettings.EPICS_PVA_ENABLE_IPV6 = false;

            // The default server interface list references IPv6 addresses, which the
            // server rejects once IPv6 is off. Fall back to an IPv4-only list.
            if (System.getProperty(PVAS_INTF_ADDR_LIST) == null && System.getenv(PVAS_INTF_ADDR_LIST) == null) {
                System.setProperty(PVAS_INTF_ADDR_LIST, IPV4_INTF_ADDR_LIST);
                PVASettings.EPICS_PVAS_INTF_ADDR_LIST = IPV4_INTF_ADDR_LIST;
            }
        }

        // Default values
        String addressList = "";
        boolean autoAddressList = true;
        String nameServersList = "";
        float connectionTimeout = 30.0f;
        float beaconPeriod = 15.0f;
        int repeaterPort = CAConstants.CA_REPEATER_PORT;
        int serverPort = CAConstants.CA_SERVER_PORT;
        int maxArrayBytes = 16384;
        float maxSearchInterval = (float) 60.0 * 5;

        String pvaAddressList = PVASettings.EPICS_PVA_ADDR_LIST;
        boolean pvaAutoAddressList = PVASettings.EPICS_PVA_AUTO_ADDR_LIST;
        String pvaNameServers = PVASettings.EPICS_PVA_NAME_SERVERS;
        int pvaConnectionTimeout = PVASettings.EPICS_PVA_CONN_TMO;
        int pvaBroadcastPort = PVASettings.EPICS_PVA_BROADCAST_PORT;
        int pvaServerPort = PVASettings.EPICS_PVA_SERVER_PORT;

        // First try to load the configuration from environment variables.
        String tmp = System.getenv(CA_ADDR_LIST);
        if (tmp != null) {
            addressList = tmp;
        }

        tmp = System.getenv(CA_AUTO_ADDR_LIST);
        if (tmp != null) {
            autoAddressList = !"NO".equalsIgnoreCase(tmp) && !"FALSE".equalsIgnoreCase(tmp) && !"0".equals(tmp);
        }

        tmp = System.getenv(CA_NAME_SERVERS);
        if (tmp != null) {
            nameServersList = tmp;
        }

        tmp = System.getenv(CA_CONN_TMO);
        if (tmp != null) {
            connectionTimeout = Float.parseFloat(tmp);
        }

        tmp = System.getenv(CA_BEACON_PERIOD);
        if (tmp != null) {
            beaconPeriod = Float.parseFloat(tmp);
        }

        tmp = System.getenv(CA_REPEATER_PORT);
        if (tmp != null) {
            repeaterPort = Integer.parseInt(tmp);
        }

        tmp = System.getenv(CA_SERVER_PORT);
        if (tmp != null) {
            serverPort = Integer.parseInt(tmp);
        }

        tmp = System.getenv(CA_MAX_ARRAY_BYTES);
        if (tmp != null) {
            maxArrayBytes = Integer.parseInt(tmp);
        }

        tmp = System.getenv(CA_MAX_SEARCH_PERIOD);
        if (tmp != null) {
            maxSearchInterval = Float.parseFloat(tmp);
        }

        if (isServer) {
            tmp = System.getenv(CAS_ADDR_LIST);
            if (tmp != null) {
                addressList = tmp;
            }
            tmp = System.getenv(CAS_SERVER_PORT);
            if (tmp != null) {
                serverPort = Integer.parseInt(tmp);
            }
            tmp = System.getenv(CAS_BEACON_ADDR_LIST);
            if (tmp != null) {
                addressList = tmp;
            }
            tmp = System.getenv(CAS_BEACON_PORT);
            if (tmp != null) {
                repeaterPort = Integer.parseInt(tmp);
            }
        }

        // Then overwrite the values with preferences, if available.
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(Channel.class);

        addressList = defaults.get(CA_ADDR_LIST, addressList);
        autoAddressList = defaults.getBoolean(CA_AUTO_ADDR_LIST, autoAddressList);
        nameServersList = defaults.get(CA_NAME_SERVERS, nameServersList);
        connectionTimeout = defaults.getFloat(CA_CONN_TMO, connectionTimeout);
        beaconPeriod = defaults.getFloat(CA_BEACON_PERIOD, beaconPeriod);
        repeaterPort = defaults.getInt(CA_REPEATER_PORT, repeaterPort);
        serverPort = defaults.getInt(CA_SERVER_PORT, serverPort);
        maxArrayBytes = defaults.getInt(CA_MAX_ARRAY_BYTES, maxArrayBytes);
        maxSearchInterval = defaults.getFloat(CA_MAX_SEARCH_PERIOD, maxSearchInterval);

        if (isServer) {
            addressList = defaults.get(CAS_ADDR_LIST, addressList);
            serverPort = defaults.getInt(CAS_SERVER_PORT, serverPort);
            addressList = defaults.get(CAS_BEACON_ADDR_LIST, addressList);
            repeaterPort = defaults.getInt(CAS_BEACON_PORT, repeaterPort);
        }

        pvaAddressList = defaults.get(PVA_ADDR_LIST, pvaAddressList);
        pvaAutoAddressList = defaults.getBoolean(PVA_AUTO_ADDR_LIST, pvaAutoAddressList);
        pvaNameServers = defaults.get(PVA_NAME_SERVERS, pvaNameServers);
        pvaConnectionTimeout = defaults.getInt(PVA_CONN_TMO, pvaConnectionTimeout);
        pvaBroadcastPort = defaults.getInt(PVA_BROADCAST_PORT, pvaBroadcastPort);
        pvaServerPort = defaults.getInt(PVA_SERVER_PORT, pvaServerPort);

        // Finally overwrite with properties, if available.
        addressList = System.getProperty(CA_ADDR_LIST, addressList);
        autoAddressList = Boolean.parseBoolean(System.getProperty(CA_AUTO_ADDR_LIST, Boolean.toString(autoAddressList)));
        nameServersList = System.getProperty(CA_NAME_SERVERS, nameServersList);
        connectionTimeout = Float.parseFloat(System.getProperty(CA_CONN_TMO, Float.toString(connectionTimeout)));
        beaconPeriod = Float.parseFloat(System.getProperty(CA_BEACON_PERIOD, Float.toString(beaconPeriod)));
        repeaterPort = Integer.parseInt(System.getProperty(CA_REPEATER_PORT, Integer.toString(repeaterPort)));
        serverPort = Integer.parseInt(System.getProperty(CA_SERVER_PORT, Integer.toString(serverPort)));
        maxArrayBytes = Integer.parseInt(System.getProperty(CA_MAX_ARRAY_BYTES, Integer.toString(maxArrayBytes)));
        maxSearchInterval = Float.parseFloat(System.getProperty(CA_MAX_SEARCH_PERIOD, Float.toString(maxSearchInterval)));

        if (isServer) {
            addressList = System.getProperty(CAS_ADDR_LIST, addressList);
            serverPort = Integer.parseInt(System.getProperty(CAS_SERVER_PORT, Integer.toString(serverPort)));
            addressList = System.getProperty(CAS_BEACON_ADDR_LIST, addressList);
            repeaterPort = Integer.parseInt(System.getProperty(CAS_BEACON_PORT, Integer.toString(repeaterPort)));
        }

        // Finally save the configuration as properties for the caj and PV Access libraries.
        System.setProperty(CAJContext.class.getName() + ADDR_LIST, addressList);
        System.setProperty(CAJContext.class.getName() + ".auto_addr_list", Boolean.toString(autoAddressList));
        System.setProperty(CAJContext.class.getName() + ".name_servers", nameServersList);
        System.setProperty(CAJContext.class.getName() + ".connection_timeout", Float.toString(connectionTimeout));
        System.setProperty(CAJContext.class.getName() + ".beacon_period", Float.toString(beaconPeriod));
        System.setProperty(CAJContext.class.getName() + ".repeater_port", Integer.toString(repeaterPort));
        System.setProperty(CAJContext.class.getName() + SERVER_PORT, Integer.toString(serverPort));
        System.setProperty(CAJContext.class.getName() + ".max_array_bytes", Integer.toString(maxArrayBytes));
        System.setProperty(CAJContext.class.getName() + ".max_search_interval", Float.toString(maxSearchInterval));

        System.setProperty(PVA_ADDR_LIST, pvaAddressList);
        System.setProperty(PVA_AUTO_ADDR_LIST, Boolean.toString(pvaAutoAddressList));
        System.setProperty(PVA_NAME_SERVERS, pvaNameServers);
        System.setProperty(PVA_CONN_TMO, Integer.toString(pvaConnectionTimeout));
        System.setProperty(PVA_BROADCAST_PORT, Integer.toString(pvaBroadcastPort));
        System.setProperty(PVA_SERVER_PORT, Integer.toString(pvaServerPort));

        // PVASettings may already have been initialised from its static block, in
        // which case the properties above had no effect. Assign the fields too.
        PVASettings.EPICS_PVA_ADDR_LIST = pvaAddressList;
        PVASettings.EPICS_PVA_AUTO_ADDR_LIST = pvaAutoAddressList;
        PVASettings.EPICS_PVA_NAME_SERVERS = pvaNameServers;
        PVASettings.EPICS_PVA_CONN_TMO = pvaConnectionTimeout;
        PVASettings.EPICS_PVA_BROADCAST_PORT = pvaBroadcastPort;
        PVASettings.EPICS_PVA_SERVER_PORT = pvaServerPort;
    }

    @Override
    public void setDebugMode(boolean debugFlag) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void flushIO() {
        // NOP
    }

    @Override
    public boolean pendIO(double timeout) {
        return true;
    }

    @Override
    public void pendEvent(double timeout) {
        // NOP
    }

    @Override
    public void printInfo() {
        String className = this.getClass().getName();

        String message = "";

        message += CA_ADDR_LIST + " = " + System.getProperty(CAJContext.class.getName() + ADDR_LIST) + "\n";
        message += CA_AUTO_ADDR_LIST + " = " + System.getProperty(CAJContext.class.getName() + ".auto_addr_list") + "\n";
        message += CA_NAME_SERVERS + " = " + System.getProperty(CAJContext.class.getName() + ".name_servers") + "\n";
        message += CA_CONN_TMO + " = " + System.getProperty(CAJContext.class.getName() + ".connection_timeout") + "\n";
        message += CA_BEACON_PERIOD + " = " + System.getProperty(CAJContext.class.getName() + ".beacon_period") + "\n";
        message += CA_REPEATER_PORT + " = " + System.getProperty(CAJContext.class.getName() + ".repeater_port") + "\n";
        message += CA_SERVER_PORT + " = " + System.getProperty(CAJContext.class.getName() + SERVER_PORT) + "\n";
        message += CA_MAX_ARRAY_BYTES + " = " + System.getProperty(CAJContext.class.getName() + ".max_array_bytes") + "\n";
        message += CA_MAX_SEARCH_PERIOD + " = " + System.getProperty(CAJContext.class.getName() + ".max_search_interval") + "\n";

        if (className.equals(Epics7ServerChannelSystem.class.getName())) {
            message += CAS_ADDR_LIST + " = " + System.getProperty(CAJContext.class.getName() + ADDR_LIST) + "\n";
            message += CAS_SERVER_PORT + " = " + System.getProperty(CAJContext.class.getName() + SERVER_PORT) + "\n";
            message += CAS_BEACON_ADDR_LIST + " = " + System.getProperty(CAJContext.class.getName() + ADDR_LIST) + "\n";
            message += CAS_BEACON_PORT + " = " + System.getProperty(CAJContext.class.getName() + SERVER_PORT) + "\n";
        }

        message += PVA_ADDR_LIST + " = " + PVASettings.EPICS_PVA_ADDR_LIST + "\n";
        message += PVA_AUTO_ADDR_LIST + " = " + PVASettings.EPICS_PVA_AUTO_ADDR_LIST + "\n";
        message += PVA_NAME_SERVERS + " = " + PVASettings.EPICS_PVA_NAME_SERVERS + "\n";
        message += PVA_CONN_TMO + " = " + PVASettings.EPICS_PVA_CONN_TMO + "\n";
        message += PVA_BROADCAST_PORT + " = " + PVASettings.EPICS_PVA_BROADCAST_PORT + "\n";
        message += PVA_SERVER_PORT + " = " + PVASettings.EPICS_PVA_SERVER_PORT + "\n";

        Logger.getLogger(className).info(message);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void dispose() {
        if (pvaClient != null) {
            pvaClient.close();
            pvaClient = null;
        }
        if (caContext != null) {
            try {
                caContext.destroy();
            } catch (CAException | IllegalStateException ex) {
                Logger.getLogger(Epics7ChannelSystem.class.getName()).log(Level.FINE, null, ex);
            }
            caContext = null;
        }
        initialized = false;
    }
}
