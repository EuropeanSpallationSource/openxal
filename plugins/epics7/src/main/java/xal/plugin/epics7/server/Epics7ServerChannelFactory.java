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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ChannelSystem;
import xal.plugin.epics7.FinishedThreadHook;
import xal.plugin.epics7.server.Epics7ServerChannelSystem;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannelFactory extends ChannelFactory {

    // EPICS7 channel system
    private static volatile Epics7ServerChannelSystem channelSystem;

    // To keep track of the threads using the Epics7ChannelSystem
    public static final List<Thread> threadList = new ArrayList<>();

    public Epics7ServerChannelFactory() {
        setChannelSystem();
    }

    private void setChannelSystem() {
        if (channelSystem == null) {
            channelSystem = Epics7ServerChannelSystem.newEpics7ServerChannelSystem();
        }
        synchronized (threadList) {
            if (!threadList.contains(Thread.currentThread())) {
                threadList.add(Thread.currentThread());
                FinishedThreadHook finishedThreadHook = new FinishedThreadHook(Thread.currentThread(), threadList, channelSystem);
                finishedThreadHook.start();
            }
        }
    }

    @Override
    protected Channel newChannel(String signalName) {
        if (channelSystem == null) {
            return null;
        }
        if (isTest()) {
            signalName += testSuffix;
        }
        return new Epics7ServerChannel(signalName, channelSystem);
    }

    @Override
    protected void dispose() {
        if (channelSystem != null) {
            channelSystem.dispose();
            channelSystem = null;
        }
    }

    @Override
    public void printInfo() {
        Logger.getLogger(Epics7ServerChannelFactory.class.getName()).info("Using EPICS7 Open XAL plugin.");
        channelSystem.printInfo();
    }

    @Override
    public boolean init() {
        if (channelSystem == null) {
            return false;
        }
        return channelSystem.isInitialized();
    }

    @Override
    protected ChannelSystem channelSystem() {
        return channelSystem;
    }
}
