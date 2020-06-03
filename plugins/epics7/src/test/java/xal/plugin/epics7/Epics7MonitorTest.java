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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.pv.Status;
import org.junit.Test;
import static org.junit.Assert.*;
import xal.ca.ConnectionException;
import static xal.plugin.epics7.Epics7Channel.VALUE_REQUEST;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7MonitorTest {

    private boolean methodCalled = false;

    private Epics7Monitor getEpics7Monitor() throws ConnectionException {
        Epics7Channel channel = new Epics7Channel("Test", Epics7TestChannelSystem.newEpics7ChannelSystem());
        channel.connectAndWait();

        return Epics7Monitor.createNewMonitor(channel, VALUE_REQUEST,
                (pvStructure) -> methodCalled = true, 0);
    }

    /**
     * Test of createNewMonitor method, of class Epics7Monitor.
     */
    @Test
    public void testCreateNewMonitor() throws Exception {
        System.out.println("createNewMonitor");
        methodCalled = false;

        getEpics7Monitor();

        assertEquals(true, methodCalled);
    }

    /**
     * Test of clear method, of class Epics7Monitor.
     */
    @Test
    public void testClear() throws Exception {
        System.out.println("clear");
        methodCalled = false;

        Epics7Monitor instance = getEpics7Monitor();

        instance.nativeMonitor = new TestMonitor() {
            @Override
            public Status stop() {
                methodCalled = true;
                return null;
            }

        };
        instance.clear();

        assertEquals(true, methodCalled);
    }

    /**
     * Test of begin method, of class Epics7Monitor.
     */
    @Test
    public void testBegin() throws Exception {
        System.out.println("begin");
        methodCalled = false;

        Epics7Monitor instance = getEpics7Monitor();

        instance.nativeMonitor = new TestMonitor() {
            @Override
            public Status start() {
                methodCalled = true;
                return null;
            }

        };
        instance.begin();

        assertEquals(true, methodCalled);
    }

    /**
     * Test of monitorConnect method, of class Epics7Monitor.
     */
    @Test
    public void testMonitorConnect() throws ConnectionException {
        System.out.println("monitorConnect");
        methodCalled = false;

        Epics7Monitor instance = getEpics7Monitor();

        instance.nativeMonitor = new TestMonitor() {
            @Override
            public Status start() {
                methodCalled = true;
                return null;
            }

        };

        instance.monitorConnect(null, instance.nativeMonitor, null);

        assertEquals(true, methodCalled);
    }

    /**
     * Test of monitorEvent method, of class Epics7Monitor.
     */
    @Test
    public void testMonitorEvent() throws ConnectionException {
        System.out.println("monitorEvent");
        methodCalled = false;

        Epics7Monitor instance = getEpics7Monitor();

        instance.nativeMonitor = new TestMonitor() {
            @Override
            public void release(MonitorElement element) {
                methodCalled = true;
            }
        };
        instance.monitorEvent(instance.nativeMonitor);

        assertEquals(true, methodCalled);

        methodCalled = false;

        instance.nativeMonitor = new TestMonitor() {
            @Override
            public MonitorElement poll() {
                methodCalled = true;
                return null;
            }
        };

        instance.monitorEvent(instance.nativeMonitor);

        assertEquals(true, methodCalled);
    }

    /**
     * Test of unlisten method, of class Epics7Monitor.
     */
    @Test
    public void testUnlisten() throws ConnectionException {
        System.out.println("unlisten");
        methodCalled = false;

        Epics7Monitor instance = getEpics7Monitor();

        instance.nativeMonitor = new TestMonitor() {
            @Override
            public Status stop() {
                methodCalled = true;
                return null;
            }

        };
        instance.unlisten(instance.nativeMonitor);

        assertEquals(true, methodCalled);
    }

    /**
     * Test of getRequesterName method, of class Epics7Monitor.
     */
    @Test
    public void testGetRequesterName() throws ConnectionException {
        System.out.println("getRequesterName");
        String expResult = "TestRequester";

        Epics7Monitor instance = getEpics7Monitor();
        String result = instance.getRequesterName();
        System.out.println(result);
        assertEquals(expResult, result);

        instance.nativeChannel = null;
        result = instance.getRequesterName();
        assertEquals(null, result);
    }

    /**
     * Test of message method, of class Epics7Monitor.
     */
    @Test
    public void testMessage() throws ConnectionException {
        System.out.println("message");
        String message = "message";

        HandlerImpl handler = new HandlerImpl();
        Logger.getLogger(Epics7Monitor.class.getName()).addHandler(handler);

        Epics7Monitor instance = getEpics7Monitor();

        instance.message(message, null);

        assertEquals(message, handler.message);
        assertEquals(handler.level, Level.INFO);
    }

}
