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

/**
 * 2D fieldmap file reader (for solenoid)
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class FieldProfile2D {

    private double lengthR;
    private double lengthZ;
    private double[][] field;
    private double norm;

    public double getNorm() {
        return norm;
    }

    private static Map<String, FieldProfile2D> instances = new HashMap<>();

    public FieldProfile2D(double lengthR, double lengthZ, double[][] fieldZ) {
        this.lengthR = lengthR;
        this.lengthZ = lengthZ;
        this.field = fieldZ;
    }

    protected FieldProfile2D(String path) {
        try {
            loadFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Static factory method to give field profile for a specific file
     *
     * @param path path to the field map file
     * @return field profile
     */
    public static FieldProfile2D getInstance(String path) {
        if (instances.containsKey(path)) {
            return instances.get(path);
        }
        FieldProfile2D fp = new FieldProfile2D(path);
        instances.put(path, fp);
        return fp;
    }

    public double[][] getField() {
        return field;
    }

    public double getLengthR() {
        return lengthR;
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
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new URL(path).openStream()));
        } catch (IOException e) {
            throw new Error("Field map " + new File(new URL(path).getFile()) + " not found");
        }

        // first line
        String line = br.readLine();
        String[] data = line.split(" ");

        int Nz = Integer.parseInt(data[0]) + 1;
        lengthZ = Double.parseDouble(data[1]);

        // second line
        line = br.readLine();
        data = line.split(" ");

        int Nr = Integer.parseInt(data[0]) + 1;
        lengthR = Double.parseDouble(data[1]);

        field = new double[Nz][Nr];

        line = br.readLine();
        norm = Double.parseDouble(line);

        for (int i = 0; i < Nz; i++) {
            for (int j = 0; j < Nr; j++) {
                line = br.readLine();
                if (line != null) {
                    field[i][j] = Double.parseDouble(line);
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
        double rmax = getLengthR();
        pw.printf("%d %f\n%d %f\n%f\n", field.length - 1, zmax, field[0].length - 1, rmax, norm);
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                pw.printf("%f\n", field[i][j]);
            }
        }
        pw.close();
    }
}
