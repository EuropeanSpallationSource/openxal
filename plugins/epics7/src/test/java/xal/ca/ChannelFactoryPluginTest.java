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
package xal.ca;

import org.junit.Test;
import static org.junit.Assert.*;
import xal.plugin.epics7.Epics7ChannelFactory;
import xal.plugin.epics7.server.Epics7ServerChannelFactory;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class ChannelFactoryPluginTest {

    /**
     * Test of getChannelFactoryInstance method, of class ChannelFactoryPlugin.
     */
    @Test
    public void testGetChannelFactoryInstance() {
        System.out.println("getChannelFactoryInstance");
        Class expResult = Epics7ChannelFactory.class;
        ChannelFactory result = ChannelFactoryPlugin.getChannelFactoryInstance();
        assertEquals(expResult, result.getClass());
    }

    /**
     * Test of getServerChannelFactoryInstance method, of class
     * ChannelFactoryPlugin.
     */
    @Test
    public void testGetServerChannelFactoryInstance() {
        System.out.println("getServerChannelFactoryInstance");
        Class expResult = Epics7ServerChannelFactory.class;
        ChannelFactory result = ChannelFactoryPlugin.getServerChannelFactoryInstance();
        assertEquals(expResult, result.getClass());
    }

}
