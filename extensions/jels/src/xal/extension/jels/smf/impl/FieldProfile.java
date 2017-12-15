/*
 * Copyright (C) 2017 European Spallation Source ERIC.
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
package xal.extension.jels.smf.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.extension.jels.tools.math.TTFIntegrator;

public class FieldProfile {

    private double length;
    private double[] field;
    private TTFIntegrator integrator;

    private static final Logger LOGGER = Logger.getLogger(FieldProfile.class.getName());

    private static Map<String, FieldProfile> instances = new HashMap<>();

    public FieldProfile(double length, double[] field) {
        this.length = length;
        this.field = field.clone();
    }

    protected FieldProfile(String path) {
        try {
            loadFile(path);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred trying to get the field profile.", e);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Fieldmap file path incorrect.", e);
        }
    }

    /**
     * Static factory method to give field profile for a specific file
     *
     * @param path path to the field map file
     * @return field profile
     */
    public static FieldProfile getInstance(String path) {
        if (instances.containsKey(path)) {
            return instances.get(path);
        }
        FieldProfile fp = new FieldProfile(path);
        instances.put(path, fp);
        return fp;
    }

    public double[] getField() {
        return field;
    }

    public double getLength() {
        return length;
    }

    public double getE0L(double frequency) {
        if (integrator == null) {
            integrator = new TTFIntegrator(length, field, frequency, false);
        }
        return integrator.getE0TL();

    }

    /**
     * ************************ File manipulation **************************
     */
    /**
     * Loads field from a file.
     *
     * @param path path to the file
     * @throws IOException
     * @throws URISyntaxException
     */
    private void loadFile(String path) throws IOException, URISyntaxException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new URL(path).openStream()));
        } catch (IOException e) {
            throw new Error("Field map " + new File(new URL(path).getFile()) + " not found");
        }

        // first line
        String line = br.readLine();
        String[] data = line.split(" ");

        int nPoints = Integer.parseInt(data[0]) + 1;
        length = Double.parseDouble(data[1]);
        field = new double[nPoints];

        br.readLine();

        int i = 0;
        while ((line = br.readLine()) != null && i < nPoints) {
            field[i++] = Double.parseDouble(line) * 1e6;
        }

        br.close();
    }

    /**
     * Saves given field to a file
     *
     * @param path path to the file
     * @throws IOException
     * @throws URISyntaxException
     */
    public void saveFile(String path) throws IOException, URISyntaxException {
        File fieldMapfile = new File(new URI(path));
        fieldMapfile.getParentFile().mkdirs();
        PrintWriter pw = new PrintWriter(new FileWriter(fieldMapfile));
        double[] field = getField();
        double zmax = getLength();
        pw.printf("%d %f%n%f%n", field.length - 1, zmax, 1.0);
        for (int i = 0; i < field.length; i++) {
            pw.printf("%f%n", field[i] * 1e-6);
        }
        pw.close();
    }

    public boolean isFirstInverted() {
        return TTFIntegrator.getSplitIntegrators(this, 0.)[0].getInverted();
    }
}
