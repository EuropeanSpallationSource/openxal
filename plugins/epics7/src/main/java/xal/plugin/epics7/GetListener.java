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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.epics.pvdata.pv.PVStructure;

/**
 * Implementation of EventListener that creates a latch and has an await method
 * to get the record synchronously.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
class GetListener implements EventListener {

    private final CountDownLatch doneSignal;
    private PVStructure pvStructure;

    public GetListener() {
        this.doneSignal = new CountDownLatch(1);
    }

    public PVStructure getPvStructure() {
        return pvStructure;
    }

    @Override
    public void event(PVStructure pvStructure) {
        this.pvStructure = pvStructure;
        doneSignal.countDown();
    }

    public void await(long timeout, TimeUnit unit) throws InterruptedException {
        doneSignal.await(timeout, unit);
    }
}
