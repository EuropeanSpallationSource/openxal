/*
 * Copyright (C) 2018 European Spallation Source ERIC.
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

/**
 * 3D fieldmap file reader (for solenoid)
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class MagFieldProfile3D {

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double lengthZ;
    private double[][][] field;
    private double norm;

    private static final Logger LOGGER = Logger.getLogger(MagFieldProfile3D.class.getName());

    public double getNorm() {
        return norm;
    }

    private static Map<String, MagFieldProfile3D> instances = new HashMap<>();

    public MagFieldProfile3D(double minX, double maxX, double minY, double maxY, double lengthZ, double[][][] fieldZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.lengthZ = lengthZ;
        this.field = fieldZ.clone();
    }

    protected MagFieldProfile3D(String path) {
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
    public static MagFieldProfile3D getInstance(String path) {
        if (instances.containsKey(path)) {
            return instances.get(path);
        }
        MagFieldProfile3D fp = new MagFieldProfile3D(path);
        instances.put(path, fp);
        return fp;
    }

    public double[][][] getField() {
        return field;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getLengthZ() {
        return lengthZ;
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
            LOGGER.log(Level.SEVERE, "Field map " + new File(new URL(path).getFile()) + " not found.", e);
        }

        // first line
        String line = br.readLine();
        String[] data = line.split(" ");

        int nPointsZ = Integer.parseInt(data[0]) + 1;
        lengthZ = Double.parseDouble(data[1]);

        // second line
        line = br.readLine();
        data = line.split(" ");

        int nPointsX = Integer.parseInt(data[0]) + 1;
        minX = Double.parseDouble(data[1]);
        maxX = Double.parseDouble(data[2]);

        // third line
        line = br.readLine();
        data = line.split(" ");

        int nPointsY = Integer.parseInt(data[0]) + 1;
        minY = Double.parseDouble(data[1]);
        maxY = Double.parseDouble(data[2]);

        field = new double[nPointsZ][nPointsY][nPointsX];

        line = br.readLine();
        norm = Double.parseDouble(line);

        for (int i = 0; i < nPointsZ; i++) {
            for (int j = 0; j < nPointsY; j++) {
                for (int k = 0; k < nPointsX; k++) {
                    line = br.readLine();
                    if (line != null) {
                        field[i][j][k] = Double.parseDouble(line);
                    }
                }
            }
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

        double zmax = getLengthZ();
        double xmin = getMinX();
        double ymin = getMinY();
        double xmax = getMaxX();
        double ymax = getMaxY();
        pw.printf("%d %f%n%d %f %f%n%d %f %f%n%f%n", field.length - 1, zmax, field[0].length - 1, ymin, ymax, field[0][0].length - 1, xmin, xmax, norm);
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                for (int k = 0; k < field[0][0].length; k++) {
                    pw.printf("%f%n", field[i][j][k]);
                }
            }
        }
        pw.close();
    }
}
