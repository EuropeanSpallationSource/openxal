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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.server.ServerContext;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;
import org.epics.pvdatabase.pva.ChannelProviderLocalFactory;
import xal.plugin.epics7.Epics7ChannelSystem;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannelSystem extends Epics7ChannelSystem {

    private ChannelProvider channelProvider;
    private PVDatabase master;
    private ServerContext context;

    public static Epics7ServerChannelSystem newEpics7ServerChannelSystem() {
        Epics7ServerChannelSystem epics7ServerChannelSystem = new Epics7ServerChannelSystem();

        epics7ServerChannelSystem.initialize();

        return epics7ServerChannelSystem;
    }

    /**
     * Once a record is created by the ChannelFactory, it must be added to the
     * master database to be able to serve the PV.
     *
     * @param record
     */
    public synchronized void addRecord(PVRecord record) {
        master.addRecord(record);
    }

    /**
     * To remove a record from the master database.
     *
     * @param record
     */
    public synchronized void removeRecord(PVRecord record) {
        master.removeRecord(record);
    }

    @Override
    protected void initialize() {
        loadJcaConfig(true);

        channelProvider = ChannelProviderLocalFactory.getChannelProviderLocal();
        master = PVDatabaseFactory.getMaster();
        try {
            context = ServerContextImpl.startPVAServer(channelProvider.getProviderName(), 0, true, null);
        } catch (PVAException ex) {
            Logger.getLogger(Epics7ServerChannelSystem.class.getName()).log(Level.SEVERE, null, ex);
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                dispose();
            }
        });
        t.setDaemon(false);
        Runtime.getRuntime().addShutdownHook(t);
    }

    @Override
    public void dispose() {
        try {
            context.destroy();
        } catch (PVAException | IllegalStateException ex) {
            Logger.getLogger(Epics7ServerChannelSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        master.destroy();
        channelProvider.destroy();
    }
}
