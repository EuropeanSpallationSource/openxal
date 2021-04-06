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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public abstract class FieldMap {

    // Field components are stored here, the key pf the hashmap is the component (x,y,z,r).
    protected HashMap<String, FieldComponent> electricField = new HashMap<>();
    protected HashMap<String, FieldComponent> magneticField = new HashMap<>();

    protected static final Logger LOGGER = Logger.getLogger(FieldMap.class.getName());

    protected int numberOfPoints;
    protected double length;
    protected double sliceLength;
    protected double[] longitudinalPositions;
    protected double fieldIntegral;

    // If the field couples different planes (e.g., solenoid).
    private boolean coupled = true;

    public int getNumberOfPoints() {
        return numberOfPoints;
    }

    public void setNumberOfPoints(int numberOfPoints) {
        this.numberOfPoints = numberOfPoints;
        recalculateSliceLength();
    }

    protected final void recalculateSliceLength() {
        sliceLength = length / (numberOfPoints - 1);

        longitudinalPositions = new double[numberOfPoints];
        for (int i = 0; i < numberOfPoints - 1; i++) {
            longitudinalPositions[i] = i * length / (numberOfPoints - 1);
        }
        longitudinalPositions[numberOfPoints - 1] = length;
    }

    public boolean isCoupled() {
        return coupled;
    }

    protected final void setCoupled(boolean coupled) {
        this.coupled = coupled;
    }

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

        if (start < 0) {
            start = 0;
        }

        int i0 = (int) Math.ceil(start / getSliceLength());
        // To avoid repeated points in different slices, the last point of a
        // slice is not included, only the very last point.
        int ie = (int) Math.floor((start + dblLen) / getSliceLength());

        if (ie >= i0) {
            for (int i = i0; i <= ie; i++) {
                fieldMapPointPositions.add(longitudinalPositions[i]);
            }
        }

        // If last point of the field map is not included, add it.
        if (Math.abs(getLength() - (start + dblLen)) < 1e-6 && ie != numberOfPoints - 1) {
            fieldMapPointPositions.add(getLength());
        }
        return fieldMapPointPositions;
    }

    /**
     * ************************ File manipulation **************************
     */
    /**
     * Check if a file is binary or ASCII.
     *
     * @param filename URL to the file.
     * @return True if the file is ASCII, false if binary.
     */
    private static boolean isAscii(URL fileURL) {
        boolean result = false;

        InputStream stream = null;
        try {
            stream = fileURL.openStream();

            result = true;
            for (int i = 0; i < 15; i++) {
                int c = stream.read();
                if (c == -1) {
                    break;
                }
                //(9)Horizontal Tab (10)Line feed  (11)Vertical tab (13)Carriage return (32)Space (126)tilde
                if (!(c == 9 || c == 10 || c == 11 || c == 13 || (c >= 32 && c <= 126))) {
                    result = false;
                }
            }
        } catch (IOException ex) {
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                }
            }
        }

        return result;
    }

    /**
     * Loads fieldComponent from a 1D fieldmap file.
     *
     * @param path path to the file
     * @param name
     * @return
     */
    protected final FieldComponent loadFile1D(String path, String name) {
        try {
            URL fileURL = new URL(new URL(path), name);

            if (isAscii(fileURL)) {
                return loadASCIIFile1D(fileURL);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(FieldMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Loads fieldComponent from a 2D fieldmap file.
     *
     * @param path path to the file
     * @param name
     * @return
     */
    protected final FieldComponent loadFile2D(String path, String name) {
        try {
            URL fileURL = new URL(new URL(path), name);

            if (isAscii(fileURL)) {
                return loadASCIIFile2D(fileURL);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(FieldMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Loads fieldComponent from a 3D fieldmap file.
     *
     * @param path path to the file
     * @param name
     * @return
     */
    protected final FieldComponent loadFile3D(String path, String name) {
        try {
            URL fileURL = new URL(new URL(path), name);

            if (isAscii(fileURL)) {
                return loadASCIIFile3D(fileURL);
            } else {

                return loadBinaryFile3D(fileURL);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(FieldMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private FieldComponent loadASCIIFile1D(URL fileURL) {
        FieldComponent fieldComponent = new FieldComponent();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(fileURL.openStream()));

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
            LOGGER.log(Level.INFO, "Field map " + fileURL.toString() + " not found.", ex);
        }

        return fieldComponent;
    }

    private FieldComponent loadASCIIFile2D(URL fileURL) {
        FieldComponent fieldComponent = new FieldComponent();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(fileURL.openStream()));

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
            LOGGER.log(Level.INFO, "Field map " + fileURL.toString() + " not found.", ex);
        }

        return fieldComponent;
    }

    private FieldComponent loadASCIIFile3D(URL fileURL) {
        FieldComponent fieldComponent = new FieldComponent();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(fileURL.openStream()));

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
            LOGGER.log(Level.INFO, "Field map " + fileURL.toString() + " not found.", ex);
        }

        return fieldComponent;
    }

    private FieldComponent loadBinaryFile3D(URL fileURL) {
        FieldComponent fieldComponent = new FieldComponent();

        try {
            InputStream stream = fileURL.openStream();

            int INT_BYTES = 4;
            int FLOAT_BYTES = 4;
            int DOUBLE_BYTES = 8;
            byte[] intAux = new byte[INT_BYTES];
            byte[] floatAux = new byte[FLOAT_BYTES];
            byte[] doubleAux = new byte[DOUBLE_BYTES];

            int nPointsZ = 0;
            if (stream.read(intAux, 0, INT_BYTES) == INT_BYTES) {
                nPointsZ = ByteBuffer.wrap(intAux).order(ByteOrder.LITTLE_ENDIAN).getInt() + 1;
            } else {
                throw new IOException();
            }

            double lengthZ = 0;
            if (stream.read(doubleAux, 0, DOUBLE_BYTES) == DOUBLE_BYTES) {
                lengthZ = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }

            int nPointsX = 0;
            if (stream.read(intAux, 0, INT_BYTES) == INT_BYTES) {
                nPointsX = ByteBuffer.wrap(intAux).order(ByteOrder.LITTLE_ENDIAN).getInt() + 1;
            } else {
                throw new IOException();
            }

            double minX = 0;
            if (stream.read(doubleAux, 0, DOUBLE_BYTES) == DOUBLE_BYTES) {
                minX = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }

            double maxX = 0;
            if (stream.read(doubleAux, 0, DOUBLE_BYTES) == DOUBLE_BYTES) {
                maxX = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }

            int nPointsY = 0;
            if (stream.read(intAux, 0, INT_BYTES) == INT_BYTES) {
                nPointsY = ByteBuffer.wrap(intAux).order(ByteOrder.LITTLE_ENDIAN).getInt() + 1;
            } else {
                throw new IOException();
            }

            double minY = 0;
            if (stream.read(doubleAux, 0, DOUBLE_BYTES) == DOUBLE_BYTES) {
                minY = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }

            double maxY = 0;
            if (stream.read(doubleAux, 0, DOUBLE_BYTES) == DOUBLE_BYTES) {
                maxY = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }

            double norm = 0;
            if (stream.read(doubleAux, 0, DOUBLE_BYTES) == DOUBLE_BYTES) {
                norm = ByteBuffer.wrap(doubleAux).order(ByteOrder.LITTLE_ENDIAN).getDouble();
            } else {
                throw new IOException();
            }

            fieldComponent.setMin(new double[]{0., minX, minY});
            fieldComponent.setMax(new double[]{lengthZ, maxX, maxY});

            double[][][] field = new double[nPointsZ][nPointsY][nPointsX];

            fieldComponent.setNorm(norm);

            for (int i = 0; i < nPointsZ; i++) {
                for (int j = 0; j < nPointsY; j++) {
                    for (int k = 0; k < nPointsX; k++) {
                        float fieldPoint = 0;
                        if (stream.read(floatAux, 0, FLOAT_BYTES) == FLOAT_BYTES) {
                            fieldPoint = ByteBuffer.wrap(floatAux).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        } else {
                            throw new IOException();
                        }
                        field[i][j][k] = (double) fieldPoint;
                    }
                }
            }

            stream.close();

            fieldComponent.setField(field);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Field map " + fileURL.toString() + " not found.", ex);
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
        File fieldMapfile = new File(new URL(new URL(path), name).toURI());
        fieldMapfile.getParentFile().mkdirs();
        PrintWriter pw = new PrintWriter(new FileWriter(fieldMapfile));

        double[] field = fieldComponent.getField();
        pw.format(Locale.US, "%d %f%n%f%n", field.length - 1, fieldComponent.getMax()[0], 1.0);
        for (int i = 0; i < field.length; i++) {
            pw.format(Locale.US, "%e%n", field[i]);
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
        File fieldMapfile = new File(new URL(new URL(path), name).toURI());
        fieldMapfile.getParentFile().mkdirs();
        PrintWriter pw = new PrintWriter(new FileWriter(fieldMapfile));

        double zmax = fieldComponent.getMax()[0];
        double rmax = fieldComponent.getMax()[1];
        double[][] field = fieldComponent.getField();
        pw.format(Locale.US, "%d %f%n%d %f%n%f%n", field.length - 1, zmax, field[0].length - 1, rmax, fieldComponent.getNorm());
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                pw.format(Locale.US, "%e%n", field[i][j]);
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
        File fieldMapfile = new File(new URL(new URL(path), name).toURI());
        fieldMapfile.getParentFile().mkdirs();
        PrintWriter pw = new PrintWriter(new FileWriter(fieldMapfile));

        double zmax = fieldComponent.getMax()[0];
        double xmin = fieldComponent.getMin()[1];
        double xmax = fieldComponent.getMax()[1];
        double ymin = fieldComponent.getMin()[2];
        double ymax = fieldComponent.getMax()[2];
        double[][][] field = fieldComponent.getField();
        pw.format(Locale.US, "%d %f%n%d %f %f%n%d %f %f%n%f%n", field.length - 1, zmax, field[0].length - 1, ymin, ymax, field[0][0].length - 1, xmin, xmax, fieldComponent.getNorm());
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                for (int k = 0; k < field[0][0].length; k++) {
                    pw.format(Locale.US, "%e%n", field[i][j][k]);
                }
            }
            pw.close();
        }
    }

    public double getFieldIntegral() {
        return fieldIntegral;
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
