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

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import xal.ca.Channel;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class PutListenerImplTest {

    /**
     * Test of putCompleted method, of class PutListenerImpl.
     */
    @Test
    public void testPutCompleted() {
        System.out.println("putCompleted");
        Channel chan = null;
        PutListenerImpl instance = new PutListenerImpl();
        instance.putCompleted(chan);

        try {
            instance.await(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            fail();
        }
    }
}
