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
package xal.ca;

import xal.plugin.epics7.Epics7ChannelFactory;
import xal.plugin.epics7.server.Epics7ServerChannelFactory;

/**
 * Implementation of ChannelFactory that uses EPICS7 to connect to IOCs using
 * the PV Access and Channel Access protocols.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class ChannelFactoryPlugin {

    private ChannelFactoryPlugin() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Instantiate a new ChannelFactory.
     *
     * @return a new channel factory
     */
    public static ChannelFactory getChannelFactoryInstance() {
        return new Epics7ChannelFactory();
    }

    /**
     * Instantiate a new ServerChannelFactory
     *
     * @return a new serverChannel factory
     */
    public static ChannelFactory getServerChannelFactoryInstance() {
        return new Epics7ServerChannelFactory();
    }
}
