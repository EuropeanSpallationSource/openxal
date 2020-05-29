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
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import xal.ca.ChannelSystem;

/**
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelSystem extends ChannelSystem {

    private ChannelProvider caChannelProvider;
    private ChannelProvider pvaChannelProvider;
    private volatile boolean initialized = false;

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
        loadJcaConfig(false);

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

        if (caChannelProvider == null) {
            Logger.getLogger(Epics7ChannelSystem.class.getName(), "Channel Access provider could not be created.");
        } else if (pvaChannelProvider == null) {
            Logger.getLogger(Epics7ChannelSystem.class.getName(), "PV Access provider could not be created.");
        } else {
            initialized = true;
        }
    }

    private String getProperty(String name, String defaultValue, Properties properties) {
        return System.getProperty(name, properties.getProperty(name, defaultValue));
    }

    /**
     * This method preloads the JCA configuration in a similar way as the PV
     * Access library does.It takes the configuration from System properties,
     * property files (user or system), or from environment variables, in that
     * precedence order. It ignores the jca.use_env property.
     *
     * @param isServer if server configuration has to be loaded.
     */
    protected final void loadJcaConfig(boolean isServer) {
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

        // Then overwrite the values with property files or system properties, if available.
        Properties _defaultProperties = new Properties();
        Properties _properties = new Properties(_defaultProperties);
        String fileSep = System.getProperty("file.separator");
        String path = null;
        try {
            // system's properties
            path = System.getProperty("java.home") + fileSep + "lib" + fileSep
                    + "JCALibrary.properties";
            _defaultProperties.load(new FileInputStream(path));
        } catch (Throwable systemEx) {
        }

        try {
            // properties
            path = System.getProperty("gov.aps.jca.JCALibrary.properties", null);
            if (path == null) {
                path = System.getProperty("user.home") + fileSep + ".JCALibrary" + fileSep
                        + "JCALibrary.properties";
            }
            _properties.load(new FileInputStream(path));
        } catch (Throwable userEx) {
        }

        // load CAJ specific configuration
        addressList = getProperty(CAJContext.class.getName() + ".addr_list", addressList, _properties);
        autoAddressList = Boolean.valueOf(getProperty(CAJContext.class.getName() + ".auto_addr_list", String.valueOf(autoAddressList), _properties));
        nameServersList = getProperty(CAJContext.class.getName() + ".name_servers", nameServersList, _properties);
        connectionTimeout = Float.parseFloat(getProperty(CAJContext.class.getName() + ".connection_timeout", String.valueOf(connectionTimeout), _properties));
        beaconPeriod = Float.parseFloat(getProperty(CAJContext.class.getName() + ".beacon_period", String.valueOf(beaconPeriod), _properties));
        repeaterPort = Integer.parseInt(getProperty(CAJContext.class.getName() + ".repeater_port", String.valueOf(repeaterPort), _properties));
        serverPort = Integer.parseInt(getProperty(CAJContext.class.getName() + ".server_port", String.valueOf(serverPort), _properties));
        maxArrayBytes = Integer.parseInt(getProperty(CAJContext.class.getName() + ".max_array_bytes", String.valueOf(maxArrayBytes), _properties));
        maxSearchInterval = Float.parseFloat(getProperty(CAJContext.class.getName() + ".max_search_interval", String.valueOf(maxSearchInterval), _properties));

        // loa configuration from properties with the same names as the environment variables.
        addressList = System.getProperty("EPICS_CA_ADDR_LIST", addressList);
        autoAddressList = Boolean.valueOf(System.getProperty("EPICS_CA_AUTO_ADDR_LIST", String.valueOf(autoAddressList)));
        nameServersList = System.getProperty("EPICS_CA_NAME_SERVERS", nameServersList);
        connectionTimeout = Float.parseFloat(System.getProperty("EPICS_CA_CONN_TMO", String.valueOf(connectionTimeout)));
        beaconPeriod = Float.parseFloat(System.getProperty("EPICS_CA_BEACON_PERIOD", String.valueOf(beaconPeriod)));
        repeaterPort = Integer.parseInt(System.getProperty("EPICS_CA_REPEATER_PORT", String.valueOf(repeaterPort)));
        serverPort = Integer.parseInt(System.getProperty("EPICS_CA_SERVER_PORT", String.valueOf(serverPort)));
        maxArrayBytes = Integer.parseInt(System.getProperty("EPICS_CA_MAX_ARRAY_BYTES", String.valueOf(maxArrayBytes)));
        maxSearchInterval = Float.parseFloat(System.getProperty("EPICS_CA_MAX_SEARCH_PERIOD", String.valueOf(maxSearchInterval)));

        if (isServer) {
            addressList = System.getProperty("EPICS_CAS_ADDR_LIST", addressList);
            serverPort = Integer.parseInt(System.getProperty("EPICS_CAS_SERVER_PORT", String.valueOf(serverPort)));
            addressList = System.getProperty("EPICS_CAS_BEACON_ADDR_LIST", addressList);
            repeaterPort = Integer.parseInt(System.getProperty("EPICS_CAS_BEACON_PORT", String.valueOf(repeaterPort)));
        }

        // Finally save the configuration in System properties.   
        System.setProperty(CAJContext.class.getName() + ".addr_list", addressList);
        System.setProperty(CAJContext.class.getName() + ".auto_addr_list", Boolean.toString(autoAddressList));
        System.setProperty(CAJContext.class.getName() + ".name_servers", nameServersList);
        System.setProperty(CAJContext.class.getName() + ".connection_timeout", Float.toString(connectionTimeout));
        System.setProperty(CAJContext.class.getName() + ".beacon_period", Float.toString(beaconPeriod));
        System.setProperty(CAJContext.class.getName() + ".repeater_port", Integer.toString(repeaterPort));
        System.setProperty(CAJContext.class.getName() + ".server_port", Integer.toString(serverPort));
        System.setProperty(CAJContext.class.getName() + ".max_array_bytes", Integer.toString(maxArrayBytes));
        System.setProperty(CAJContext.class.getName() + ".max_search_interval", Float.toString(maxSearchInterval));
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
    }
}
