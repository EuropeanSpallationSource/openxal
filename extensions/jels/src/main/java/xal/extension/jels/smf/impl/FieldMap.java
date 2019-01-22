/*
 * Copyright (C) 2019 European Spallation Source ERIC.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.extension.jels.model.elem.FieldMapPoint;

/**
 * This abstract class implements convenience method to load TraceWin-type field
 * map files. The constructor of subclasses should use the File manipulation
 * methods provided by this class to load all field components. Then they should
 * implement the getFieldAt().
 * <p>
 * To avoid having several instances for the same field map, it is recommended
 * to use the FieldMapFactory method, that will select the right FieldMap
 * subclass and make sure the field map is loaded only once.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public abstract class FieldMap {

    // Field components are stored here, the key pf the hashmap is the component (x,y,z,r).
    protected HashMap<String, FieldComponent> fieldComponents = new HashMap<>();

    protected static final Logger LOGGER = Logger.getLogger(FieldMap.class.getName());

    protected int numberOfPoints;
    protected double length;
    protected double sliceLength;
    protected double[] longitudinalPositions;

    /**
     * This method should use the fieldComponents HashMap to calculate the field
     * components at a given position.
     *
     * @param position Position along the element [m].
     * @return FieldMapPoint
     */
    public abstract FieldMapPoint getFieldAt(double position);

    /**
     * Saves the fieldmap files.
     *
     * @param path
     * @param filename
     */
    abstract public void saveFieldMap(String path, String filename) throws IOException, URISyntaxException;

    /**
     * This method returns the length of the field map.
     *
     * @return
     */
    public double getLength() {
        return length;
    }

    /**
     * This method returns the length of each slice of the field map.
     *
     * @return
     */
    public double getSliceLength() {
        return sliceLength;
    }

    /**
     * This method returns the longitudinal positions at which the field map is
     * defined.
     *
     * @return
     */
    public double[] getLongitudinalPositions() {
        return longitudinalPositions;
    }

    /**
     * This method returns the field map points within an interval. If there is
     * any field map point at the start position, it returns the point twice.
     * The reason is that the first point is used to calculate the first drift
     * space.
     *
     * @param start
     * @param dblLen
     * @return
     */
    public List<Double> getFieldMapPointPositions(double start, double dblLen) {
        // Find the field map points included in the current slice.
        List<Double> fieldMapPointPositions = new ArrayList<>();

        int i0 = (int) Math.round(start / getSliceLength());
        // To avoid repeated points in different slices, the last point of a
        // slice is not included, only the very last point.
        int ie = (int) Math.round((start + dblLen) / getSliceLength());

        if (ie >= i0) {
            for (int i = i0; i < ie; i++) {
                fieldMapPointPositions.add(longitudinalPositions[i]);
            }
        }

        // If last point of the field map is not included, add it.
        if (Math.abs(getLength() - (start + dblLen)) < 1e-6){
            fieldMapPointPositions.add(getLength());
        }
        return fieldMapPointPositions;
    }

    /**
     * ************************ File manipulation **************************
     */
    /**
     * Loads fieldComponent from a 1D fieldmap file.
     *
     * @param path path to the file
     * @param name
     * @return
     */
    protected final FieldComponent loadFile1D(String path, String name) {
        FieldComponent fieldComponent = new FieldComponent();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL(new URL(path), name).openStream()));

            // first line
            String line = br.readLine();
            String[] data = line.split(" ");

            int nPoints = Integer.parseInt(data[0]) + 1;
            double[] field = new double[nPoints];
            double length = Double.parseDouble(data[1]);
            fieldComponent.setMax(new double[]{length});

            // Read norm and not use it
            line = br.readLine();
            double norm = Double.parseDouble(line);
            fieldComponent.setNorm(norm);

            int i = 0;
            while ((line = br.readLine()) != null && i < nPoints) {
                field[i++] = Double.parseDouble(line);
            }

            br.close();

            fieldComponent.setField(field);

        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Field map " + path + " not found.", ex);
        }

        return fieldComponent;
    }

    /**
     * Loads fieldComponent from a 2D fieldmap file.
     *
     * @param path path to the file
     * @param name
     * @return
     */
    protected final FieldComponent loadFile2D(String path, String name) {
        FieldComponent fieldComponent = new FieldComponent();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL(new URL(path), name).openStream()));

            // first line
            String line = br.readLine();
            String[] data = line.split(" ");

            int nPointsZ = Integer.parseInt(data[0]) + 1;
            double lengthZ = Double.parseDouble(data[1]);

            // second line
            line = br.readLine();
            data = line.split(" ");

            int nPointsR = Integer.parseInt(data[0]) + 1;
            double lengthR = Double.parseDouble(data[1]);

            fieldComponent.setMax(new double[]{lengthZ, lengthR});

            double[][] field = new double[nPointsZ][nPointsR];

            line = br.readLine();
            double norm = Double.parseDouble(line);
            fieldComponent.setNorm(norm);

            for (int i = 0; i < nPointsZ; i++) {
                for (int j = 0; j < nPointsR; j++) {
                    line = br.readLine();
                    if (line != null) {
                        field[i][j] = Double.parseDouble(line);
                    }
                }
            }

            br.close();

            fieldComponent.setField(field);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Field map " + path + " not found.", ex);
        }

        return fieldComponent;
    }

    /**
     * Loads fieldComponent from a 3D fieldmap file.
     *
     * @param path path to the file
     * @param name
     * @return
     */
    protected final FieldComponent loadFile3D(String path, String name) {
        FieldComponent fieldComponent = new FieldComponent();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL(new URL(path), name).openStream()));

            // first line
            String line = br.readLine();
            String[] data = line.split(" ");

            int nPointsZ = Integer.parseInt(data[0]) + 1;
            double lengthZ = Double.parseDouble(data[1]);

            // second line
            line = br.readLine();
            data = line.split(" ");

            int nPointsX = Integer.parseInt(data[0]) + 1;
            double minX = Double.parseDouble(data[1]);
            double maxX = Double.parseDouble(data[2]);

            // third line
            line = br.readLine();
            data = line.split(" ");

            int nPointsY = Integer.parseInt(data[0]) + 1;
            double minY = Double.parseDouble(data[1]);
            double maxY = Double.parseDouble(data[2]);

            fieldComponent.setMin(new double[]{0., minX, minY});
            fieldComponent.setMax(new double[]{lengthZ, maxX, maxY});

            double[][][] field = new double[nPointsZ][nPointsY][nPointsX];

            line = br.readLine();
            double norm = Double.parseDouble(line);
            fieldComponent.setNorm(norm);

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

            fieldComponent.setField(field);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Field map " + path + " not found.", ex);
        }

        return fieldComponent;
    }

    /**
     * Saves given fieldComponent to a file for 1D fieldmaps.
     *
     * @param path path to the file
     * @throws IOException
     * @throws URISyntaxException
     */
    protected final void saveFile1D(String path, String name, FieldComponent<double[]> fieldComponent) throws IOException, URISyntaxException {
        File fieldMapfile = new File(new URI(path).resolve(name));
        fieldMapfile.getParentFile().mkdirs();
        PrintWriter pw = new PrintWriter(new FileWriter(fieldMapfile));

        double[] field = fieldComponent.getField();
        pw.printf("%d %f%n%f%n", field.length - 1, fieldComponent.getMax()[0], 1.0);
        for (int i = 0; i < field.length; i++) {
            pw.printf("%f%n", field[i]);
        }
        pw.close();
    }

    /**
     * Saves given fieldComponent to a file for 2D fieldmaps.
     *
     * @param path path to the file
     * @throws IOException
     * @throws URISyntaxException
     */
    protected final void saveFile2D(String path, String name, FieldComponent<double[][]> fieldComponent) throws IOException, URISyntaxException {
        File fieldMapfile = new File(new URI(path).resolve(name));
        fieldMapfile.getParentFile().mkdirs();
        PrintWriter pw = new PrintWriter(new FileWriter(fieldMapfile));

        double zmax = fieldComponent.getMax()[0];
        double rmax = fieldComponent.getMax()[1];
        double[][] field = fieldComponent.getField();
        pw.printf("%d %f%n%d %f%n%f%n", field.length - 1, zmax, field[0].length - 1, rmax, fieldComponent.getNorm());
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                pw.printf("%f%n", field[i][j]);
            }
        }
        pw.close();
    }

    /**
     * Saves given fieldComponent to a file for 3D fieldmaps.
     *
     * @param path path to the file
     * @throws IOException
     * @throws URISyntaxException
     */
    protected final void saveFile3D(String path, String name, FieldComponent<double[][][]> fieldComponent) throws IOException, URISyntaxException {
        File fieldMapfile = new File(new URI(path).resolve(name));
        fieldMapfile.getParentFile().mkdirs();
        PrintWriter pw = new PrintWriter(new FileWriter(fieldMapfile));

        double zmax = fieldComponent.getMax()[0];
        double xmin = fieldComponent.getMin()[1];
        double xmax = fieldComponent.getMax()[1];
        double ymin = fieldComponent.getMin()[2];
        double ymax = fieldComponent.getMax()[2];
        double[][][] field = fieldComponent.getField();
        pw.printf("%d %f%n%d %f %f%n%d %f %f%n%f%n", field.length - 1, zmax, field[0].length - 1, ymin, ymax, field[0][0].length - 1, xmin, xmax, fieldComponent.getNorm());
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                for (int k = 0; k < field[0][0].length; k++) {
                    pw.printf("%f%n", field[i][j][k]);
                }
            }
            pw.close();
        }
    }

    /**
     * Class to store a component of the fieldComponent.
     */
    protected static class FieldComponent<T> {

        // Minimum value for the position coordinate
        private double[] min = {0.};
        // Maximum value for the position coordinate
        private double[] max = {0.};
        // Normalization factor.
        private double norm = 0.;
        // Array containing the fieldComponent points. It can be a 1D, a 2D, or a 3D array.
        private T field;

        public FieldComponent() {
        }

        public double[] getMin() {
            return min;
        }

        public void setMin(double[] min) {
            this.min = min;
        }

        public double[] getMax() {
            return max;
        }

        public void setMax(double[] max) {
            this.max = max;
        }

        public double getNorm() {
            return norm;
        }

        public void setNorm(double norm) {
            this.norm = norm;
        }

        public T getField() {
            return field;
        }

        public void setField(T field) {
            this.field = field;
        }
    }

}
