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
import org.junit.Test;
import static org.junit.Assert.*;
import xal.ca.Channel;
import xal.ca.ChannelSystem;
import xal.plugin.epics7.HandlerImpl;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannelFactoryTest {

    /**
     * Test of newChannel method, of class Epics7ServerChannelFactory.
     */
    @Test
    public void testNewChannel() {
        System.out.println("newChannel");
        Epics7ServerChannelFactory instance = new Epics7ServerChannelFactory();

        String signalName = "TestChannel";

        Channel result = instance.newChannel(signalName);
        assertEquals(signalName, result.channelName());

        instance.setTest(true);
        result = instance.newChannel(signalName);
        assertEquals(signalName + ":TEST", result.channelName());

        instance.dispose();
        result = instance.newChannel(signalName);
        assertEquals(null, result);
    }

    /**
     * Test of dispose method, of class Epics7ServerChannelFactory.
     */
    @Test
    public void testDispose() {
        System.out.println("dispose");
        Epics7ServerChannelFactory instance = new Epics7ServerChannelFactory();

        instance.dispose();
        assertEquals(false, instance.init());
        // Testing disposal of disposed system.
        instance.dispose();
    }

    /**
     * Test of printInfo method, of class Epics7ServerChannelFactory.
     */
    @Test
    public void testPrintInfo() {
        System.out.println("printInfo");
        Epics7ServerChannelFactory instance = new Epics7ServerChannelFactory();

        String message = "Using EPICS7 Open XAL plugin.";

        HandlerImpl handler = new HandlerImpl();
        Logger.getLogger(Epics7ServerChannelFactory.class.getName()).addHandler(handler);

        instance.printInfo();

        assertTrue(handler.message.startsWith(message));
        assertEquals(handler.level, Level.INFO);
    }

    /**
     * Test of init method, of class Epics7ServerChannelFactory.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        Epics7ServerChannelFactory instance = new Epics7ServerChannelFactory();
        assertEquals(true, instance.init());
        instance.dispose();
        assertEquals(false, instance.init());
    }

    /**
     * Test of channelSystem method, of class Epics7ServerChannelFactory.
     */
    @Test
    public void testChannelSystem() {
        System.out.println("channelSystem");
        Epics7ServerChannelFactory instance = new Epics7ServerChannelFactory();
        ChannelSystem result = instance.channelSystem();
        assertEquals(Epics7ServerChannelSystem.class, result.getClass());
    }
}
