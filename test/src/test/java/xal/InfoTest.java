/*
 * Copyright (C) 2021 European Spallation Source ERIC.
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
package xal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;
import xal.Info;
import xal.tools.ResourceManager;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class InfoTest {

    public InfoTest() {
    }

    /**
     * Test of getLabel method, of class Info.
     */
    @Test
    public void testGetLabel() throws URISyntaxException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        URL url = ResourceManager.getResourceURL(Info.class, "info.json");
        if (url != null) {
            File infoFile = new File(new URI(url.toString()));
            if (infoFile.exists()) {
                infoFile.delete();
            }
        }
        System.out.println("getLabel");

        String expResult = "Open XAL";

        Class<?> info = Class.forName(Info.class.getName());
        Method initMethod = info.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(null, null);

        String result = Info.getLabel();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLabel method reading a json file.
     */
    @Test
    public void testGetLabelFromJson() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, URISyntaxException, IOException, IllegalArgumentException, InvocationTargetException {
        System.out.println("getLabelFromJson");

        String label = "Open XAL Test";

        URL url = ResourceManager.getResourceURL(Info.class, "");
        File parentDirectory = new File(new URI(url.toString()));
        File infoFile = new File(parentDirectory, "info.json");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(infoFile))) {
            writer.write("{\"label\": \"" + label + "\"}");
        }

        Class<?> info = Class.forName(Info.class.getName());
        Method initMethod = info.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(null, null);
        String result = Info.getLabel();
        assertEquals(label, result);
    }

    /**
     * Test of getLabel method reading a json file.
     */
    @Test
    public void testGetLabelFromEmptyJson() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, URISyntaxException, IOException, IllegalArgumentException, InvocationTargetException {
        System.out.println("getLabelFromEmptyJson");

        String label = "Open XAL";

        URL url = ResourceManager.getResourceURL(Info.class, "");
        File parentDirectory = new File(new URI(url.toString()));
        File infoFile = new File(parentDirectory, "info.json");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(infoFile))) {
            writer.write("");
        }

        Class<?> info = Class.forName(Info.class.getName());
        Method initMethod = info.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(null, null);
        String result = Info.getLabel();
        assertEquals(label, result);
    }
}
