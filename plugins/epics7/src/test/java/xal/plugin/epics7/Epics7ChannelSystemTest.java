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

import com.cosylab.epics.caj.CAJContext;
import com.cosylab.epics.caj.impl.CAConstants;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pvaccess.client.ChannelProvider;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelSystemTest {

    /**
     * Test of getCaChannelProvider method, of class Epics7ChannelSystem.
     */
    @Test
    public void testGetCaChannelProvider() {
        System.out.println("getCaChannelProvider");
        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        ChannelProvider expResult = null;
        ChannelProvider result = instance.getCaChannelProvider();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPvaChannelProvider method, of class Epics7ChannelSystem.
     */
    @Test
    public void testGetPvaChannelProvider() {
        System.out.println("getPvaChannelProvider");
        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        ChannelProvider expResult = null;
        ChannelProvider result = instance.getPvaChannelProvider();
        assertEquals(expResult, result);
    }

    /**
     * Test of loadJcaConfig method, of class Epics7ChannelSystem.
     */
    @Test
    public void testLoadJcaConfig() {
        System.out.println("loadJcaConfig");
        Epics7ChannelSystem instance = new Epics7ChannelSystem();

        instance.loadJcaConfig(false);
        String property = System.getProperty(CAJContext.class.getName() + ".repeater_port", null);
        assertEquals(String.valueOf(CAConstants.CA_REPEATER_PORT), property);

        instance.loadJcaConfig(true);
        property = System.getProperty(CAJContext.class.getName() + ".server_port", null);
        assertEquals(String.valueOf(CAConstants.CA_SERVER_PORT), property);
    }

    /**
     * Test of setDebugMode method, of class Epics7ChannelSystem.
     */
    @Test
    public void testSetDebugMode() {
        System.out.println("setDebugMode");
        boolean exceptionThrown = false;

        boolean debugFlag = false;
        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        try {
            instance.setDebugMode(debugFlag);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertEquals(true, exceptionThrown);
    }

    /**
     * Test of flushIO method, of class Epics7ChannelSystem.
     */
    @Test
    public void testFlushIO() {
        System.out.println("flushIO");
        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        instance.flushIO();
    }

    /**
     * Test of pendIO method, of class Epics7ChannelSystem.
     */
    @Test
    public void testPendIO() {
        System.out.println("pendIO");
        double timeout = 0.0;
        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        boolean expResult = true;
        boolean result = instance.pendIO(timeout);
        assertEquals(expResult, result);
    }

    /**
     * Test of pendEvent method, of class Epics7ChannelSystem.
     */
    @Test
    public void testPendEvent() {
        System.out.println("pendEvent");
        double timeout = 0.0;
        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        instance.pendEvent(timeout);
    }

    /**
     * Test of printInfo method, of class Epics7ChannelSystem.
     */
    @Test
    public void testPrintInfo() {
        System.out.println("printInfo");
        HandlerImpl handler = new HandlerImpl();
        Logger.getLogger(Epics7ChannelSystem.class.getName()).addHandler(handler);

        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        instance.printInfo();

        String message = "Epics7ChannelSystem: using EPICS7 Open XAL plugin.";

        assertEquals(message, handler.message);
        assertEquals(handler.level, Level.INFO);
    }
}
