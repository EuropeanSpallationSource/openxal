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
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ChannelSystemTest {

    private static final Logger LOGGER = Logger.getLogger(Epics7ChannelSystemTest.class.getName());

    /**
     * The Channel Access configuration is written to CAJ system properties.
     */
    @Test
    public void testLoadConfigClient() {
        LOGGER.log(Level.INFO, "loadConfig client");
        Epics7ChannelSystem.loadConfig(false);

        String property = System.getProperty(CAJContext.class.getName() + ".repeater_port", null);
        assertEquals(String.valueOf(CAConstants.CA_REPEATER_PORT), property);
    }

    @Test
    public void testLoadConfigServer() {
        LOGGER.log(Level.INFO, "loadConfig server");
        Epics7ChannelSystem.loadConfig(true);

        String property = System.getProperty(CAJContext.class.getName() + ".server_port", null);
        assertEquals(String.valueOf(CAConstants.CA_SERVER_PORT), property);
    }

    /**
     * The PV Access configuration is applied to PVASettings.
     */
    @Test
    public void testLoadConfigSetsPvaSettings() {
        LOGGER.log(Level.INFO, "loadConfig pva");
        Epics7ChannelSystem.loadConfig(false);

        // The default broadcast port must survive loadConfig.
        assertEquals(5076, org.epics.pva.PVASettings.EPICS_PVA_BROADCAST_PORT);
    }

    @Test
    public void testSetDebugModeUnsupported() {
        LOGGER.log(Level.INFO, "setDebugMode");
        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        assertThrows(UnsupportedOperationException.class, () -> instance.setDebugMode(false));
    }

    @Test
    public void testFlushIO() {
        LOGGER.log(Level.INFO, "flushIO");
        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        instance.flushIO();
    }

    @Test
    public void testPendIO() {
        LOGGER.log(Level.INFO, "pendIO");
        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        assertTrue(instance.pendIO(0.0));
    }

    @Test
    public void testPendEvent() {
        LOGGER.log(Level.INFO, "pendEvent");
        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        instance.pendEvent(0.0);
    }

    @Test
    public void testPrintInfo() {
        LOGGER.log(Level.INFO, "printInfo");
        HandlerImpl handler = new HandlerImpl();
        Logger.getLogger(Epics7ChannelSystem.class.getName()).addHandler(handler);

        Epics7ChannelSystem instance = new Epics7ChannelSystem();
        Epics7ChannelSystem.loadConfig(false);
        instance.printInfo();

        assertTrue(handler.message.startsWith("EPICS_CA_ADDR_LIST = "));
        assertEquals(Level.INFO, handler.level);
    }
}
