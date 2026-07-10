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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A channel system that hands out {@link TestNativeChannel}s instead of talking to real EPICS servers.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7TestChannelSystem extends Epics7ChannelSystem {

    /**
     * Every channel created, in creation order, so that tests can inspect them.
     */
    public final List<TestNativeChannel> created = new CopyOnWriteArrayList<>();

    protected static Epics7TestChannelSystem newEpics7ChannelSystem() {
        Epics7TestChannelSystem epics7ChannelSystem = new Epics7TestChannelSystem();

        epics7ChannelSystem.initialize();

        return epics7ChannelSystem;
    }

    @Override
    public NativeChannel createPvaChannel(String signalName, NativeChannel.ConnectionListener listener) {
        TestNativeChannel channel = new TestNativeChannel(PvaNativeChannel.PROTOCOL, signalName, listener);
        created.add(channel);
        return channel;
    }

    @Override
    public NativeChannel createCaChannel(String signalName, NativeChannel.ConnectionListener listener) {
        TestNativeChannel channel = new TestNativeChannel(CaNativeChannel.PROTOCOL, signalName, listener);
        created.add(channel);
        return channel;
    }

    @Override
    protected void initialize() {
        initialized = true;
    }

    @Override
    public void dispose() {
        initialized = false;
    }
}
