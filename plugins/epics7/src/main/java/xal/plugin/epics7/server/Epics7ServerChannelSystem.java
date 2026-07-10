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

import com.cosylab.epics.caj.cas.ProcessVariableEventDispatcher;
import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;
import gov.aps.jca.CAException;
import gov.aps.jca.JCALibrary;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pva.server.PVAServer;
import xal.plugin.epics7.Epics7ChannelSystem;

/**
 * Serves channels over both EPICS protocols: PV Access through {@link PVAServer} and Channel Access through the CAJ
 * channel access server.
 *
 * The previous implementation served PV Access from a pvDatabase {@code PVRecord} registry. The PV Access library used
 * by Phoebus has no pvDatabase, so PVs are registered directly with the server as {@code ServerPV}s, each owned by an
 * {@link Epics7ServerChannel}.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannelSystem extends Epics7ChannelSystem {

    private PVAServer pvaServer;
    private gov.aps.jca.cas.ServerContext caContext;
    private DefaultServerImpl channelServer;

    public static Epics7ServerChannelSystem newEpics7ServerChannelSystem() {
        Epics7ServerChannelSystem epics7ServerChannelSystem = new Epics7ServerChannelSystem();

        epics7ServerChannelSystem.initialize();

        return epics7ServerChannelSystem;
    }

    /**
     * The PV Access server that {@link Epics7ServerChannel} registers its PVs with.
     */
    public PVAServer getPvaServer() {
        return pvaServer;
    }

    public synchronized void addMemPV(MemoryProcessVariable memoryProcessVariable) {
        channelServer.registerProcessVariable(memoryProcessVariable);
        channelServer.registerProcessVariable(memoryProcessVariable.getName() + ".VAL", memoryProcessVariable);

        ProcessVariableEventDispatcher processVariableEventDispatcher = new ProcessVariableEventDispatcher(memoryProcessVariable);
        memoryProcessVariable.setEventCallback(processVariableEventDispatcher);
    }

    synchronized void removeMemPV(MemoryProcessVariable memoryProcessVariable) {
        channelServer.unregisterProcessVariable(memoryProcessVariable.getName());
        channelServer.unregisterProcessVariable(memoryProcessVariable.getName() + ".VAL");
        memoryProcessVariable.destroy();
    }

    @Override
    protected void initialize() {
        loadConfig(true);

        try {
            channelServer = new DefaultServerImpl();
            caContext = JCALibrary.getInstance().createServerContext(JCALibrary.CHANNEL_ACCESS_SERVER_JAVA, channelServer);
        } catch (CAException ex) {
            Logger.getLogger(Epics7ServerChannelSystem.class.getName())
                    .log(Level.SEVERE, "Channel Access server context could not be created.", ex);
        }

        try {
            pvaServer = new PVAServer();
        } catch (Exception ex) {
            Logger.getLogger(Epics7ServerChannelSystem.class.getName())
                    .log(Level.SEVERE, "PV Access server could not be created.", ex);
        }

        Thread t = new Thread(this::dispose);
        t.setDaemon(false);
        Runtime.getRuntime().addShutdownHook(t);

        initialized = caContext != null && pvaServer != null;
    }

    // Disposing the contexts and clearing all the records.
    @Override
    public void dispose() {
        if (pvaServer != null) {
            pvaServer.close();
            pvaServer = null;
        }
        if (caContext != null) {
            try {
                caContext.destroy();
            } catch (CAException | IllegalStateException ex) {
                Logger.getLogger(Epics7ServerChannelSystem.class.getName()).log(Level.FINE, null, ex);
            }
            caContext = null;
        }
        initialized = false;
    }
}
