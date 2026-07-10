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

import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;
import gov.aps.jca.dbr.DBRType;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannelSystemTest {

    private static final Logger LOGGER = Logger.getLogger(Epics7ServerChannelSystemTest.class.getName());

    private static Epics7ServerChannelSystem system;

    @BeforeClass
    public static void setUpClass() {
        system = Epics7ServerChannelSystem.newEpics7ServerChannelSystem();
    }

    @AfterClass
    public static void tearDownClass() {
        if (system != null) {
            system.dispose();
        }
    }

    @Test
    public void testInitialized() {
        LOGGER.log(Level.INFO, "initialized");
        assertTrue(system.isInitialized());
    }

    @Test
    public void testPvaServerAvailable() {
        LOGGER.log(Level.INFO, "getPvaServer");
        assertNotNull(system.getPvaServer());
    }

    @Test
    public void testAddAndRemoveMemPV() {
        LOGGER.log(Level.INFO, "add/remove MemPV");
        MemoryProcessVariable pv = new MemoryProcessVariable("TestAddRemove", null, DBRType.DOUBLE, new double[]{0.0});

        system.addMemPV(pv);
        // Registration installs an event dispatcher as the callback.
        assertNotNull(pv.getEventCallback());

        system.removeMemPV(pv);
    }
}
