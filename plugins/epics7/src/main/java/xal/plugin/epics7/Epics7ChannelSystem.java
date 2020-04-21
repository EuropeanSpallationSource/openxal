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

import java.util.logging.Logger;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import xal.ca.ChannelSystem;

/**
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelSystem extends ChannelSystem {

    private final ChannelProvider caChannelProvider;
    private final ChannelProvider pvaChannelProvider;
    private volatile boolean initialized = false;

    protected ChannelProvider getCaChannelProvider() {
        return caChannelProvider;
    }

    protected ChannelProvider getPvaChannelProvider() {
        return pvaChannelProvider;
    }

    protected Epics7ChannelSystem() {
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

    @Override
    public void setDebugMode(boolean debugFlag) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void flushIO() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean pendIO(double timeout) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pendEvent(double timeout) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void printInfo() {
        System.out.println("Epics7ChannelSystem: using EPICS7 Open XAL plugin.");
    }

    protected boolean isInitialized() {
        return initialized;
    }

    public void dispose() {
        org.epics.ca.ClientFactory.stop();
        org.epics.pvaccess.ClientFactory.stop();
    }
}
