/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.plugin.epics7;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
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
import xal.ca.PutListener;
import static xal.plugin.epics7.Epics7Channel.ALARM_FIELD;
import static xal.plugin.epics7.Epics7Channel.CONTROL_FIELD;
import static xal.plugin.epics7.Epics7Channel.DISPLAY_FIELD;
import static xal.plugin.epics7.Epics7Channel.TIMESTAMP_FIELD;

/**
 *
 * @author juanfestebanmuller
 */
public class ChannelPutRequesterImplTest {

    public ChannelPutRequesterImplTest() {
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

    boolean eventListenerCalled = false;
    boolean putListenerCalled = false;

    /**
     * Test of channelPutConnect method, of class ChannelPutRequesterImpl.
     */
    @Test
    public void testChannelPutConnect() {
        System.out.println("channelPutConnect");
        eventListenerCalled = false;
        Status status = StatusFactory.getStatusCreate().getStatusOK();
        ChannelPutImpl channelPut = new ChannelPutImpl();
        Structure structure = StandardFieldFactory.getStandardField().scalar(ScalarType.pvBoolean, ALARM_FIELD + "," + TIMESTAMP_FIELD + ","
                + DISPLAY_FIELD + "," + CONTROL_FIELD);
        PutListener listener = (event) -> {
        };
        Epics7Channel channel = null;
        EventListener put = (event) -> {
            eventListenerCalled = true;
        };
        ChannelPutRequesterImpl instance = new ChannelPutRequesterImpl(listener, channel, put);
        instance.channelPutConnect(status, channelPut, structure);

        assertEquals(channelPut.lastRequestCalled, true);
        assertEquals(channelPut.putCalled, true);
        assertEquals(eventListenerCalled, true);
    }

    /**
     * Test of putDone method, of class ChannelPutRequesterImpl.
     */
    @Test
    public void testPutDone() {
        System.out.println("putDone");
        putListenerCalled = false;
        Status status = null;
        ChannelPut channelPut = null;
        PutListener listener = (event) -> {
            putListenerCalled = true;
        };
        Epics7Channel channel = null;
        EventListener put = (event) -> {
        };
        ChannelPutRequesterImpl instance = new ChannelPutRequesterImpl(listener, channel, put);
        instance.putDone(status, channelPut);

        assertEquals(putListenerCalled, true);
    }

    /**
     * Test of getRequesterName method, of class ChannelPutRequesterImpl.
     */
    @Test
    public void testGetRequesterName() {
        System.out.println("getRequesterName");

        PutListener listener = (event) -> {
        };
        Epics7Channel channel = new Epics7Channel("test", null);
        EventListener put = (event) -> {
        };
        ChannelPutRequesterImpl instance = new ChannelPutRequesterImpl(listener, channel, put);
        String result = instance.getRequesterName();
        assertEquals(result, null);
    }

    /**
     * Test of message method, of class ChannelPutRequesterImpl.
     */
    @Test
    public void testMessage() {
        System.out.println("message");
        String message = "Test";
        MessageType messageType = null;

        PutListener listener = (event) -> {
        };
        Epics7Channel channel = null;
        EventListener put = (event) -> {
        };
        ChannelPutRequesterImpl instance = new ChannelPutRequesterImpl(listener, channel, put);

        HandlerImpl handler = new HandlerImpl();
        Logger.getLogger(Epics7Channel.class.getName()).addHandler(handler);
        instance.message(message, messageType);

        assertEquals(message, handler.message);
        assertEquals(handler.level, Level.INFO);
    }

    private static class ChannelPutImpl implements ChannelPut {

        public boolean putCalled = false;
        public boolean getCalled = false;
        public boolean getChannelCalled = false;
        public boolean cancelCalled = false;
        public boolean lastRequestCalled = false;
        public boolean lockCalled = false;
        public boolean unlockCalled = false;
        public boolean destroyCalled = false;

        @Override
        public void put(PVStructure pvPutStructure, BitSet bitSet) {
            putCalled = true;
        }

        @Override
        public void get() {
            getCalled = true;
        }

        @Override
        public org.epics.pvaccess.client.Channel getChannel() {
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

}
