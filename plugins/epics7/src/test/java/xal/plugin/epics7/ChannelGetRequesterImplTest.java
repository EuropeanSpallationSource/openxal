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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static xal.plugin.epics7.Epics7Channel.ALARM_FIELD;
import static xal.plugin.epics7.Epics7Channel.CONTROL_FIELD;
import static xal.plugin.epics7.Epics7Channel.DISPLAY_FIELD;
import static xal.plugin.epics7.Epics7Channel.TIMESTAMP_FIELD;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class ChannelGetRequesterImplTest {

    public ChannelGetRequesterImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private class ChannelGetTestImpl implements ChannelGet {

        public boolean getCalled = false;
        public boolean getChannelCalled = false;
        public boolean cancelCalled = false;
        public boolean lastRequestCalled = false;
        public boolean lockCalled = false;
        public boolean unlockCalled = false;
        public boolean destroyCalled = false;

        @Override
        public void get() {
            getCalled = true;
        }

        @Override
        public Channel getChannel() {
            getChannelCalled = true;
            return null;
        }

        @Override
        public void cancel() {
            cancelCalled = true;
        }

        @Override
        public void lastRequest() {
            lastRequestCalled = true;
        }

        @Override
        public void lock() {
            lockCalled = true;
        }

        @Override
        public void unlock() {
            unlockCalled = true;
        }

        @Override
        public void destroy() {
            destroyCalled = true;
        }
    }

    private class HandlerImpl extends Handler {

        public Level level;
        public String message;

        @Override
        public void publish(LogRecord record) {
            level = record.getLevel();
            message = record.getMessage();
        }

        @Override
        public void flush() {
            //
        }

        @Override
        public void close() throws SecurityException {
            //
        }
    };

    /**
     * Test of channelGetConnect method, of class ChannelGetRequesterImpl.
     */
    @Test
    public void testChannelGetConnect() {
        System.out.println("channelGetConnect");
        Status status = null;
        ChannelGetTestImpl channelGet = new ChannelGetTestImpl();
        Structure structure = null;
        EventListener listener = (event) -> {
        };
        ChannelGetRequesterImpl instance = new ChannelGetRequesterImpl(listener);
        instance.channelGetConnect(status, channelGet, structure);
        assertEquals(channelGet.lastRequestCalled, true);
        assertEquals(channelGet.getCalled, true);
    }

    /**
     * Test of getDone method, of class ChannelGetRequesterImpl.
     */
    @Test
    public void testGetDone() {
        System.out.println("getDone");

        Status status = StatusFactory.getStatusCreate().getStatusOK();
        ChannelGetTestImpl channelGet = new ChannelGetTestImpl();
        Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvBoolean, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                + DISPLAY_FIELD + "," + CONTROL_FIELD);

        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
        PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
        BitSet bitSet = null;
        EventListener listener = (pvS) -> {
            assertEquals(pvS, pvStructure);
        };
        ChannelGetRequesterImpl instance = new ChannelGetRequesterImpl(listener);

        instance.getDone(status, channelGet, pvStructure, bitSet);
    }

    /**
     * Test of getRequesterName method, of class ChannelGetRequesterImpl.
     */
    @Test
    public void testGetRequesterName() {
        System.out.println("getRequesterName");
        EventListener listener = (event) -> {
        };
        ChannelGetRequesterImpl instance = new ChannelGetRequesterImpl(listener);
        boolean exceptionGenerated = false;
        try {
            String result = instance.getRequesterName();
        } catch (Exception ex) {
            exceptionGenerated = true;
        }
        assertEquals(exceptionGenerated, true);
    }

    /**
     * Test of message method, of class ChannelGetRequesterImpl.
     */
    @Test
    public void testMessage() {
        System.out.println("message");
        String message = "Test";
        MessageType messageType = null;
        EventListener listener = (event) -> {
        };
        ChannelGetRequesterImpl instance = new ChannelGetRequesterImpl(listener);
        HandlerImpl handler = new HandlerImpl();
        Logger.getLogger(Epics7Channel.class.getName()).addHandler(handler);
        instance.message(message, messageType);

        assertEquals(message, handler.message);
        assertEquals(handler.level, Level.INFO);
    }

}
