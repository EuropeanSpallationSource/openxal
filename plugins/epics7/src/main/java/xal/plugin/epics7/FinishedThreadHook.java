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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class FinishedThreadHook extends Thread {

    private final Thread callerThread;
    private final Epics7ChannelSystem system;
    private final List<Thread> threadList;

    public FinishedThreadHook(Thread callerThread, List<Thread> threadList, Epics7ChannelSystem system) {
        this.callerThread = callerThread;
        this.system = system;
        this.threadList = threadList;
    }

    @Override
    public void run() {
        try {
            callerThread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(FinishedThreadHook.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            synchronized (threadList) {
                threadList.remove(callerThread);
                if (threadList.isEmpty()) {
                    system.dispose();
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "The thread that created the {0} finished, so it was disposed.", system.getClass().getName());
                }
            }
        }
    }
}
