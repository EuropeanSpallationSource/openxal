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
import gov.aps.jca.cas.ProcessVariableEventCallback;
import gov.aps.jca.dbr.DBRType;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pvdatabase.PVRecord;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class Epics7ServerChannelSystemTest {

    private static final Logger LOGGER = Logger.getLogger(Epics7ServerChannelSystemTest.class.getName());

    private boolean methodCalled = false;

    /**
     * Test of newEpics7ServerChannelSystem method, of class
     * Epics7ServerChannelSystem.
     */
    @Test
    public void testNewEpics7ServerChannelSystem() {
        LOGGER.log(Level.INFO, "newEpics7ServerChannelSystem");
        Epics7ServerChannelSystem result = Epics7ServerChannelSystem.newEpics7ServerChannelSystem();
        assertEquals(true, result.isInitialized());
    }

    /**
     * Test of addRecord method, of class Epics7ServerChannelSystem.
     */
    @Test
    public void testAddRecord() {
        LOGGER.log(Level.INFO, "addRecord");
        methodCalled = false;
        Epics7ServerChannelSystem instance = Epics7ServerChannelSystem.newEpics7ServerChannelSystem();
        instance.master = new TestPVDataBase() {
            @Override
            public boolean addRecord(PVRecord record) {
                methodCalled = true;
                return true;
            }
        };
        instance.addRecord(null);

        assertEquals(true, methodCalled);
    }

    /**
     * Test of addMemPV method, of class Epics7ServerChannelSystem.
     */
    @Test
    public void testAddMemPV() {
        LOGGER.log(Level.INFO, "addMemPV");
        methodCalled = false;
        MemoryProcessVariable memoryProcessVariable;
        memoryProcessVariable = new MemoryProcessVariable("TestName", null,
                DBRType.DOUBLE, new double[]{0}) {
            @Override
            public void setEventCallback(ProcessVariableEventCallback eventCallback) {
                methodCalled = true;
            }
        };
        Epics7ServerChannelSystem instance = Epics7ServerChannelSystem.newEpics7ServerChannelSystem();
        instance.addMemPV(memoryProcessVariable);

        assertEquals(true, methodCalled);
    }

    /**
     * Test of removeMemPV method, of class Epics7ServerChannelSystem.
     */
    @Test
    public void testRemoveMemPV() {
        LOGGER.log(Level.INFO, "removeMemPV");
        methodCalled = false;
        MemoryProcessVariable memoryProcessVariable = new MemoryProcessVariable("TestName", null,
                DBRType.DOUBLE, new double[]{0}) {
            @Override
            public void destroy() {
                methodCalled = true;
            }
        };
        Epics7ServerChannelSystem instance = Epics7ServerChannelSystem.newEpics7ServerChannelSystem();
        instance.removeMemPV(memoryProcessVariable);

        assertEquals(true, methodCalled);
    }

    /**
     * Test of removeRecord method, of class Epics7ServerChannelSystem.
     */
    @Test
    public void testRemoveRecord() {
        LOGGER.log(Level.INFO, "removeRecord");
        methodCalled = false;
        Epics7ServerChannelSystem instance = Epics7ServerChannelSystem.newEpics7ServerChannelSystem();
        instance.master = new TestPVDataBase() {
            @Override
            public boolean removeRecord(PVRecord record) {
                methodCalled = true;
                return true;
            }
        };
        instance.removeRecord(null);

        assertEquals(true, methodCalled);
    }

    /**
     * Test of initialize method, of class Epics7ServerChannelSystem.
     */
    @Test
    public void testInitialize() {
        LOGGER.log(Level.INFO, "initialize");
        Epics7ServerChannelSystem instance = Epics7ServerChannelSystem.newEpics7ServerChannelSystem();
        instance.initialize();
        assertEquals(true, instance.isInitialized());
    }

    /**
     * Test of dispose method, of class Epics7ServerChannelSystem.
     */
    @Test
    public void testDispose() {
        LOGGER.log(Level.INFO, "dispose");
        Epics7ServerChannelSystem instance = Epics7ServerChannelSystem.newEpics7ServerChannelSystem();
        instance.dispose();
        assertEquals(false, instance.isInitialized());
    }
}
