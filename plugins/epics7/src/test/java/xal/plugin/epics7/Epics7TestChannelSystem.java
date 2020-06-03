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

import org.epics.pvaccess.client.ChannelProvider;
import xal.plugin.epics7.Epics7ChannelSystem;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7TestChannelSystem extends Epics7ChannelSystem {

    private ChannelProvider caChannelProvider;
    private ChannelProvider pvaChannelProvider;
    private volatile boolean initialized = false;

    protected static Epics7ChannelSystem newEpics7ChannelSystem() {
        Epics7TestChannelSystem epics7ChannelSystem = new Epics7TestChannelSystem();

        epics7ChannelSystem.initialize();

        return epics7ChannelSystem;
    }

    @Override
    protected ChannelProvider getCaChannelProvider() {
        return caChannelProvider;
    }

    @Override
    protected ChannelProvider getPvaChannelProvider() {
        return pvaChannelProvider;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    protected void initialize() {
        // Try to get the channel providers.
        caChannelProvider = new TestChannelProvider();
        pvaChannelProvider = new TestChannelProvider();
        initialized = true;
    }
}
