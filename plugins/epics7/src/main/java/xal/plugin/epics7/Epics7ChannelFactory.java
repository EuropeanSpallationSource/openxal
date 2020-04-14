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

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ChannelSystem;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelFactory extends ChannelFactory {

    // EPICS7 channel system
    private final Epics7ChannelSystem CHANNEL_SYSTEM;

    public Epics7ChannelFactory() {
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

    @Override
    protected Channel newChannel(String signalName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ChannelSystem channelSystem() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void printInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
