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
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ChannelSystem;

/**
 * NOTE: previous implementations kept a cache of native channels, but that is
 * not required since ChannelFactory keeps a list of Open XAL Channels, which
 * can create only 1 native channel each. TODO: test if it works when connecting
 * to 2 different fields of the same PV.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelFactory extends ChannelFactory {

    // EPICS7 channel system
    private final Epics7ChannelSystem CHANNEL_SYSTEM;

    public Epics7ChannelFactory() {
        // Load CAJ configuration in a similar fashion as the PV Access library.
        loadJcaConfig();

        CHANNEL_SYSTEM = new Epics7ChannelSystem();
    }

    /**
     * This method does not perform any action, it only returns true if the
     * system has been initialized.
     *
     * @return
     */
    @Override
    public boolean init() {
        return CHANNEL_SYSTEM.isInitialized();
    }

    public void dispose() {
        CHANNEL_SYSTEM.dispose();
    }

    @Override
    protected Channel newChannel(String signalName) {
        return new Epics7Channel(signalName, CHANNEL_SYSTEM);
    }

    @Override
    protected ChannelSystem channelSystem() {
        return CHANNEL_SYSTEM;
    }

    @Override
    public void printInfo() {
        System.out.println("Epics7ChannelFactory: using EPICS7 Open XAL plugin.");
    }

    /**
     * This method preloads the JCA configuration in a similar way as the PV
     * Access library does. It takes the configuration from System properties,
     * property files (user or system), or from environment variables, in that
     * precedence order. It ignores the jca.use_env property.
     */
    private void loadJcaConfig() {
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

    private String getProperty(String name, String defaultValue, Properties _properties) {
        return System.getProperty(name, _properties.getProperty(name, defaultValue));
    }
}
