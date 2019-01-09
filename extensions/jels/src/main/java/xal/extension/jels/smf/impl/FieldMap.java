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
import java.util.HashMap;
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

    /**
     * This method should use the fieldComponents HashMap to calculate the field
     * components at a given position.
     *
     * @param position Position along the element [m].
     * @return FieldMapPoint
     */
    public abstract FieldMapPoint getFieldAt(double position);

    /**
     * This method returns the longitudinal positions at which the field map is
     * defined.
     *
     * @return
     */
    public abstract double[] getLongitudinalPositions();

    /**
     * This method returns the length of the field map.
     *
     * @return
     */
    public abstract double getLength();

    /**
     * This method returns the length of each slice of the field map.
     *
     * @return
     */
    public abstract double getSliceLength();

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
            fieldComponent.setMax(length);

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
     * Saves given fieldComponent to a file for 1D fieldmaps.
     *
     * @param path path to the file
     * @throws IOException
     * @throws URISyntaxException
     */
    protected final void saveFile1D(String path, String name, FieldComponent<double[]> fieldComponent) throws IOException, URISyntaxException {
        File fieldMapfile = new File(new URI(path));
        fieldMapfile.getParentFile().mkdirs();
        PrintWriter pw = new PrintWriter(new FileWriter(fieldMapfile));

        double[] field = fieldComponent.getField();
        pw.printf("%d %f%n%f%n", field.length - 1, fieldComponent.getMax(), 1.0);
        for (int i = 0; i < field.length; i++) {
            pw.printf("%f%n", field[i]);
        }
        pw.close();
    }

    /**
     * Class to store a component of the fieldComponent.
     */
    protected static class FieldComponent<T> {

        // Minimum value for the position coordinate
        private double min = 0.;
        // Maximum value for the position coordinate
        private double max = 0.;
        // Normalization factor.
        private double norm = 0.;
        // Array containing the fieldComponent points. It can be a 1D, a 2D, or a 3D array.
        private T field;

        public FieldComponent() {
        }

        public double getMin() {
            return min;
        }

        public void setMin(double min) {
            this.min = min;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
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
