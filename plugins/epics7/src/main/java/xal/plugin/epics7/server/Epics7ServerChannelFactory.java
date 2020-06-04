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
package xal.plugin.epics7.server;

import java.util.logging.Logger;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ChannelSystem;
import xal.plugin.epics7.server.Epics7ServerChannelSystem;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannelFactory extends ChannelFactory {

    // EPICS7 channel system
    private static Epics7ServerChannelSystem CHANNEL_SYSTEM;

    public Epics7ServerChannelFactory() {
        setChannelSystem();
    }

    private static void setChannelSystem() {
        if (CHANNEL_SYSTEM == null) {
            CHANNEL_SYSTEM = Epics7ServerChannelSystem.newEpics7ServerChannelSystem();
        }
    }

    @Override
    protected Channel newChannel(String signalName) {
        if (CHANNEL_SYSTEM == null) {
            return null;
        }
        if (isTest()) {
            signalName += TEST_SUFFIX;
        }
        return new Epics7ServerChannel(signalName, CHANNEL_SYSTEM);
    }

    @Override
    protected void dispose() {
        if (CHANNEL_SYSTEM != null) {
            CHANNEL_SYSTEM.dispose();
            CHANNEL_SYSTEM = null;
        }
    }

    @Override
    public void printInfo() {
       Logger.getLogger(Epics7ServerChannelFactory.class.getName()).info("Using EPICS7 Open XAL plugin.");
       CHANNEL_SYSTEM.printInfo();
    }

    @Override
    public boolean init() {
        if (CHANNEL_SYSTEM == null) {
            return false;
        }
        return CHANNEL_SYSTEM.isInitialized();
    }

    @Override
    protected ChannelSystem channelSystem() {
        return CHANNEL_SYSTEM;
    }
}
