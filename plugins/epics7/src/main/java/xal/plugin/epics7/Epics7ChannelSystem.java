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
import java.util.Properties;
import java.util.logging.Logger;
import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import xal.ca.Channel;
import xal.ca.ChannelSystem;
import xal.tools.apputils.Preferences;

/**
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelSystem extends ChannelSystem {

    private ChannelProvider caChannelProvider;
    private ChannelProvider pvaChannelProvider;
    protected volatile boolean initialized = false;

    protected ChannelProvider getCaChannelProvider() {
        return caChannelProvider;
    }

    protected ChannelProvider getPvaChannelProvider() {
        return pvaChannelProvider;
    }

    protected Epics7ChannelSystem() {
    }

    protected static Epics7ChannelSystem newEpics7ChannelSystem() {
        Epics7ChannelSystem epics7ChannelSystem = new Epics7ChannelSystem();

        epics7ChannelSystem.initialize();

        return epics7ChannelSystem;
    }

    protected void initialize() {
        // Load CAJ configuration in a similar fashion as the PV Access library.
        loadConfig(false);

        // Initialising channel providers for both EPICS protocols.
        org.epics.ca.ClientFactory.start();
        org.epics.pvaccess.ClientFactory.start();

        // Create shutdown hook to close the resource when calling System.exit() or 
        // if the process is terminated.
        // TODO: check whether this is needed, e.g., monitors are stopped without this?
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                dispose();
            }
        });
        t.setDaemon(false);
        Runtime.getRuntime().addShutdownHook(t);

        // Try to get the channel providers.
        caChannelProvider = ChannelProviderRegistryFactory.getChannelProviderRegistry().getProvider("ca");
        pvaChannelProvider = ChannelProviderRegistryFactory.getChannelProviderRegistry().getProvider("pva");

        if (caChannelProvider == null || pvaChannelProvider == null) {
            if (caChannelProvider == null) {
                Logger.getLogger(Epics7ChannelSystem.class.getName(), "Channel Access provider could not be created.");
            }
            if (pvaChannelProvider == null) {
                Logger.getLogger(Epics7ChannelSystem.class.getName(), "PV Access provider could not be created.");
            }
        } else {
            initialized = true;
        }
    }

    private String getProperty(String name, String defaultValue, Properties properties) {
        return System.getProperty(name, properties.getProperty(name, defaultValue));
    }

    /**
     * This method preloads the JCA and PVA configuration. It takes the
     * configuration from Preferences or from environment variables, in that
     * precedence order. It ignores the jca.use_env property and the JCALibrary
     * file.
     *
     * @param isServer if server configuration has to be loaded.
     */
    public static final void loadConfig(boolean isServer) {
        // Setting jca.use_env=false to load the configuration from system properties
        // that we define now.
        System.setProperty("jca.use_env", "false");

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

        String pvaAddressList = "";
        boolean pvaAutoAddressList = true;
        float pvaConnectionTimeout = 30.0f;
        float pvaBeaconPeriod = 15.0f;
        int pvaBroadcastPort = PVAConstants.PVA_BROADCAST_PORT;
        int pvaReceiveBufferSize = PVAConstants.MAX_TCP_RECV;

        // First try to load the configuration from environment variables.
        String tmp = System.getenv("EPICS_CA_ADDR_LIST");
        if (tmp != null) {
            addressList = tmp;
        }

        tmp = System.getenv("EPICS_CA_AUTO_ADDR_LIST");
        if (tmp != null) {
            autoAddressList = !tmp.equalsIgnoreCase("NO") && !tmp.equalsIgnoreCase("FALSE") && !tmp.equals("0");
        }

        tmp = System.getenv("EPICS_CA_NAME_SERVERS");
        if (tmp != null) {
            nameServersList = tmp;
        }

        tmp = System.getenv("EPICS_CA_CONN_TMO");
        if (tmp != null) {
            connectionTimeout = Float.parseFloat(tmp);
        }

        tmp = System.getenv("EPICS_CA_BEACON_PERIOD");
        if (tmp != null) {
            beaconPeriod = Float.parseFloat(tmp);
        }

        tmp = System.getenv("EPICS_CA_REPEATER_PORT");
        if (tmp != null) {
            repeaterPort = Integer.parseInt(tmp);
        }

        tmp = System.getenv("EPICS_CA_SERVER_PORT");
        if (tmp != null) {
            serverPort = Integer.parseInt(tmp);
        }

        tmp = System.getenv("EPICS_CA_MAX_ARRAY_BYTES");
        if (tmp != null) {
            maxArrayBytes = Integer.parseInt(tmp);
        }

        tmp = System.getenv("EPICS_CA_MAX_SEARCH_PERIOD");
        if (tmp != null) {
            maxSearchInterval = Float.parseFloat(tmp);
        }

        if (isServer) {
            tmp = System.getenv("EPICS_CAS_ADDR_LIST");
            if (tmp != null) {
                addressList = tmp;
            }
            tmp = System.getenv("EPICS_CAS_SERVER_PORT");
            if (tmp != null) {
                serverPort = Integer.parseInt(tmp);
            }
            tmp = System.getenv("EPICS_CAS_BEACON_ADDR_LIST");
            if (tmp != null) {
                addressList = tmp;
            }
            tmp = System.getenv("EPICS_CAS_BEACON_PORT");
            if (tmp != null) {
                repeaterPort = Integer.parseInt(tmp);
            }
        }

        tmp = System.getenv("EPICS_PVA_ADDR_LIST");
        if (tmp != null) {
            pvaAddressList = tmp;
        }

        tmp = System.getenv("EPICS_PVA_AUTO_ADDR_LIST");
        if (tmp != null) {
            pvaAutoAddressList = Boolean.parseBoolean(tmp);
        }

        tmp = System.getenv("EPICS_PVA_CONN_TMO");
        if (tmp != null) {
            pvaConnectionTimeout = Float.parseFloat(tmp);
        }

        tmp = System.getenv("EPICS_PVA_BEACON_PERIOD");
        if (tmp != null) {
            pvaBeaconPeriod = Float.parseFloat(tmp);
        }

        tmp = System.getenv("EPICS_PVA_BROADCAST_PORT");
        if (tmp != null) {
            pvaBroadcastPort = Integer.parseInt(tmp);
        }

        tmp = System.getenv("EPICS_PVA_MAX_ARRAY_BYTES");
        if (tmp != null) {
            pvaReceiveBufferSize = Integer.parseInt(tmp);
        }

        // Then overwrite the values with preferences, if available.
        java.util.prefs.Preferences defaults = Preferences.nodeForPackage(Channel.class);

        addressList = defaults.get("EPICS_CA_ADDR_LIST", addressList);
        autoAddressList = defaults.getBoolean("EPICS_CA_AUTO_ADDR_LIST", autoAddressList);
        nameServersList = defaults.get("EPICS_CA_NAME_SERVERS", nameServersList);
        connectionTimeout = defaults.getFloat("EPICS_CA_CONN_TMO", connectionTimeout);
        beaconPeriod = defaults.getFloat("EPICS_CA_BEACON_PERIOD", beaconPeriod);
        repeaterPort = defaults.getInt("EPICS_CA_REPEATER_PORT", repeaterPort);
        serverPort = defaults.getInt("EPICS_CA_SERVER_PORT", serverPort);
        maxArrayBytes = defaults.getInt("EPICS_CA_MAX_ARRAY_BYTES", maxArrayBytes);
        maxSearchInterval = defaults.getFloat("EPICS_CA_MAX_SEARCH_PERIOD", maxSearchInterval);

        if (isServer) {
            addressList = defaults.get("EPICS_CAS_ADDR_LIST", addressList);
            serverPort = defaults.getInt("EPICS_CAS_SERVER_PORT", serverPort);
            addressList = defaults.get("EPICS_CAS_BEACON_ADDR_LIST", addressList);
            repeaterPort = defaults.getInt("EPICS_CAS_BEACON_PORT", repeaterPort);
        }

        pvaAddressList = defaults.get("EPICS_PVA_ADDR_LIST", pvaAddressList);
        pvaAutoAddressList = defaults.getBoolean("EPICS_PVA_AUTO_ADDR_LIST", pvaAutoAddressList);
        pvaConnectionTimeout = defaults.getFloat("EPICS_PVA_CONN_TMO", pvaConnectionTimeout);
        pvaBeaconPeriod = defaults.getFloat("EPICS_PVA_BEACON_PERIOD", pvaBeaconPeriod);
        pvaBroadcastPort = defaults.getInt("EPICS_PVA_BROADCAST_PORT", pvaBroadcastPort);
        pvaReceiveBufferSize = defaults.getInt("EPICS_PVA_MAX_ARRAY_BYTES", pvaReceiveBufferSize);

        // Finally overwrite with properties, if available.
        addressList = System.getProperty("EPICS_CA_ADDR_LIST", addressList);
        autoAddressList = Boolean.parseBoolean(System.getProperty("EPICS_CA_AUTO_ADDR_LIST", Boolean.toString(autoAddressList)));
        nameServersList = System.getProperty("EPICS_CA_NAME_SERVERS", nameServersList);
        connectionTimeout = Float.parseFloat(System.getProperty("EPICS_CA_CONN_TMO", Float.toString(connectionTimeout)));
        beaconPeriod = Float.parseFloat(System.getProperty("EPICS_CA_BEACON_PERIOD", Float.toString(beaconPeriod)));
        repeaterPort = Integer.parseInt(System.getProperty("EPICS_CA_REPEATER_PORT", Integer.toString(repeaterPort)));
        serverPort = Integer.parseInt(System.getProperty("EPICS_CA_SERVER_PORT", Integer.toString(serverPort)));
        maxArrayBytes = Integer.parseInt(System.getProperty("EPICS_CA_MAX_ARRAY_BYTES", Integer.toString(maxArrayBytes)));
        maxSearchInterval = Float.parseFloat(System.getProperty("EPICS_CA_MAX_SEARCH_PERIOD", Float.toString(maxSearchInterval)));

        if (isServer) {
            addressList = System.getProperty("EPICS_CAS_ADDR_LIST", addressList);
            serverPort = Integer.parseInt(System.getProperty("EPICS_CAS_SERVER_PORT", Integer.toString(serverPort)));
            addressList = System.getProperty("EPICS_CAS_BEACON_ADDR_LIST", addressList);
            repeaterPort = Integer.parseInt(System.getProperty("EPICS_CAS_BEACON_PORT", Integer.toString(repeaterPort)));
        }

        pvaAddressList = System.getProperty("EPICS_PVA_ADDR_LIST", pvaAddressList);
        pvaAutoAddressList = Boolean.parseBoolean(System.getProperty("EPICS_PVA_AUTO_ADDR_LIST", Boolean.toString(pvaAutoAddressList)));
        pvaConnectionTimeout = Float.parseFloat(System.getProperty("EPICS_PVA_CONN_TMO", Float.toString(pvaConnectionTimeout)));
        pvaBeaconPeriod = Float.parseFloat(System.getProperty("EPICS_PVA_BEACON_PERIOD", Float.toString(pvaBeaconPeriod)));
        pvaBroadcastPort = Integer.parseInt(System.getProperty("EPICS_PVA_BROADCAST_PORT", Integer.toString(pvaBroadcastPort)));
        pvaReceiveBufferSize = Integer.parseInt(System.getProperty("EPICS_PVA_MAX_ARRAY_BYTES", Integer.toString(pvaReceiveBufferSize)));

        // Finally save the configuration as properties for the caj and pvaccess libraries.
        System.setProperty(CAJContext.class.getName() + ".addr_list", addressList);
        System.setProperty(CAJContext.class.getName() + ".auto_addr_list", Boolean.toString(autoAddressList));
        System.setProperty(CAJContext.class.getName() + ".name_servers", nameServersList);
        System.setProperty(CAJContext.class.getName() + ".connection_timeout", Float.toString(connectionTimeout));
        System.setProperty(CAJContext.class.getName() + ".beacon_period", Float.toString(beaconPeriod));
        System.setProperty(CAJContext.class.getName() + ".repeater_port", Integer.toString(repeaterPort));
        System.setProperty(CAJContext.class.getName() + ".server_port", Integer.toString(serverPort));
        System.setProperty(CAJContext.class.getName() + ".max_array_bytes", Integer.toString(maxArrayBytes));
        System.setProperty(CAJContext.class.getName() + ".max_search_interval", Float.toString(maxSearchInterval));

        System.setProperty("EPICS_PVA_ADDR_LIST", pvaAddressList);
        System.setProperty("EPICS_PVA_AUTO_ADDR_LIST", Boolean.toString(pvaAutoAddressList));
        System.setProperty("EPICS_PVA_CONN_TMO", Float.toString(pvaConnectionTimeout));
        System.setProperty("EPICS_PVA_BEACON_PERIOD", Float.toString(pvaBeaconPeriod));
        System.setProperty("EPICS_PVA_BROADCAST_PORT", Integer.toString(pvaBroadcastPort));
        System.setProperty("EPICS_PVA_MAX_ARRAY_BYTES", Integer.toString(pvaReceiveBufferSize));
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
        Logger.getLogger(Epics7ChannelSystem.class.getName()).info("Epics7ChannelSystem: using EPICS7 Open XAL plugin.");
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void dispose() {
        org.epics.ca.ClientFactory.stop();
        org.epics.pvaccess.ClientFactory.stop();
        initialized = false;
    }
}
