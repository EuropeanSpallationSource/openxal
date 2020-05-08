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

import xal.plugin.epics7.*;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdatabase.PVRecord;
import org.epics.pvdatabase.pva.MonitorFactory;
import xal.ca.ConnectionException;

/**
 * Monitor implementation for Epics7 server.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerMonitor extends Epics7Monitor implements MonitorRequester {

    private Epics7ServerMonitor(EventListener listener, int intMaskEvent) throws ConnectionException {
        super(null, listener, intMaskEvent);
    }

    public static Epics7ServerMonitor createNewMonitor(PVRecord pvRecord, String request, EventListener listener, int intMaskEvent) throws ConnectionException {
        Epics7ServerMonitor monitor = new Epics7ServerMonitor(listener, intMaskEvent);

        monitor.createRequest(pvRecord, request);

        return monitor;
    }

    private void createRequest(PVRecord record, String request) {
        PVStructure structure = CreateRequest.create().createRequest(request);
        nativeMonitor = MonitorFactory.create(record, this, structure);
    }
}
