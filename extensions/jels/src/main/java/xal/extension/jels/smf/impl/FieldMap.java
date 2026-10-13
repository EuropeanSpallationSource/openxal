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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
    protected HashMap<String, FieldComponent<?>> electricField = new HashMap<>();
    protected HashMap<String, FieldComponent<?>> magneticField = new HashMap<>();

    private static final int DOUBLE_BYTES = 8;
    private static final int FLOAT_BYTES = 4;
    private static final int INT_BYTES = 4;
    private static final int ASCII_CHECK_BYTES = 15;

    /**
     * Positions this close outside the field map still return the field at its
     * edge, to absorb rounding in the caller's position arithmetic.
     */
    private static final double POSITION_TOLERANCE = 1e-6;

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
    public abstract void saveFieldMap(String path, String filename) throws IOException, URISyntaxException;

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
    public double[] getFieldMapPointPositions(double start, double dblLen) {
        if (start < 0) {
            start = 0;
        }

        int i0 = (int) Math.ceil(start / getSliceLength());
        // To avoid repeated points in different slices, the last point of a
        // slice is not included, only the very last point.
        int ie = (int) Math.floor((start + dblLen) / getSliceLength());

        // The last point of the field map is added if the slice ends there.
        boolean addLastPoint = Math.abs(getLength() - (start + dblLen)) < 1e-6 && ie != numberOfPoints - 1;

        // Find the field map points included in the current slice.
        int count = (ie >= i0 ? ie - i0 + 1 : 0) + (addLastPoint ? 1 : 0);
        double[] fieldMapPointPositions = new double[count];

        int k = 0;
        if (ie >= i0) {
            for (int i = i0; i <= ie; i++) {
                fieldMapPointPositions[k++] = longitudinalPositions[i];
            }
        }
        if (addLastPoint) {
            fieldMapPointPositions[k] = getLength();
        }
        return fieldMapPointPositions;
    }

    /**
     * Whether the position is outside the field map, beyond the tolerance for
     * rounding errors.
     *
     * @param position Position along the element [m].
     */
    protected boolean isOutside(double position) {
        return position < -POSITION_TOLERANCE || position > length + POSITION_TOLERANCE;
    }

    /**
     * A 3D field component and its derivatives on the axis (x = y = 0), at a
     * given longitudinal position.
     */
    protected record AxisSample(double value, double ddx, double ddy, double ddz) {

    }

    /**
     * Interpolates a 3D field component on the axis. The value and its
     * derivatives are computed on the enclosing grid planes and linearly
     * interpolated along z. Derivatives are central differences, or one-sided
     * at the edges of the grid (e.g. maps that start on the axis).
     */
    protected static final class AxisInterpolator3D {

        private final double[][][] field;
        private final double norm;
        private final double spacingX;
        private final double spacingY;
        private final double spacingZ;
        private final int axisX;
        private final int axisY;

        public AxisInterpolator3D(FieldComponent<double[][][]> component) {
            field = component.getField();
            norm = component.getNorm();

            double minX = component.getMin()[1];
            double minY = component.getMin()[2];
            spacingX = (component.getMax()[1] - minX) / (field[0][0].length - 1);
            spacingY = (component.getMax()[2] - minY) / (field[0].length - 1);
            spacingZ = component.getMax()[0] / (field.length - 1);

            // Rounded, not truncated: -minX / spacingX can be just below the
            // integer (e.g. 6.999...), which would select the grid line next to
            // the axis.
            axisX = (int) Math.round(-minX / spacingX);
            axisY = (int) Math.round(-minY / spacingY);
        }

        /**
         * The raw field value on the axis at grid plane {@code i}, before
         * dividing by the norm.
         */
        public double rawValueOnAxis(int i) {
            return field[i][axisY][axisX];
        }

        /**
         * Difference along x around the axis on a grid plane, per grid step.
         */
        private double derivativeX(double[][] plane) {
            double[] row = plane[axisY];
            int lo = Math.max(axisX - 1, 0);
            int hi = Math.min(axisX + 1, row.length - 1);
            return (row[hi] - row[lo]) / (hi - lo);
        }

        /**
         * Difference along y around the axis on a grid plane, per grid step.
         */
        private double derivativeY(double[][] plane) {
            int lo = Math.max(axisY - 1, 0);
            int hi = Math.min(axisY + 1, plane.length - 1);
            return (plane[hi][axisX] - plane[lo][axisX]) / (hi - lo);
        }

        /**
         * Difference along z on the axis at grid plane i, per grid step.
         */
        private double nodeDerivativeZ(int i) {
            if (i == 0) {
                return field[1][axisY][axisX] - field[0][axisY][axisX];
            }
            if (i == field.length - 1) {
                return field[i][axisY][axisX] - field[i - 1][axisY][axisX];
            }
            return (field[i + 1][axisY][axisX] - field[i - 1][axisY][axisX]) / 2;
        }

        public AxisSample interpolate(double position) {
            int i = (int) Math.floor(position / spacingZ);
            if (i < 0) {
                i = 0;
            } else if (i >= field.length - 1) {
                i = field.length - 2;
            }
            double t = position / spacingZ - i;

            double[][] plane0 = field[i];
            double[][] plane1 = field[i + 1];

            double f0 = plane0[axisY][axisX];
            double f1 = plane1[axisY][axisX];
            double value = f0 + t * (f1 - f0);

            double dx0 = derivativeX(plane0);
            double dx1 = derivativeX(plane1);
            double ddx = dx0 + t * (dx1 - dx0);

            double dy0 = derivativeY(plane0);
            double dy1 = derivativeY(plane1);
            double ddy = dy0 + t * (dy1 - dy0);

            double ddz = nodeDerivativeZ(i) + t * (nodeDerivativeZ(i + 1) - nodeDerivativeZ(i));

            return new AxisSample(value / norm, ddx / (spacingX * norm), ddy / (spacingY * norm),
                    ddz / (spacingZ * norm));
        }
    }

    /**
     * ************************ File manipulation **************************
     */
    /**
     * Resolves a field map file name against the field map directory. URIs into
     * an archive (e.g. {@code jar:file:/lib.jar!/maps/}) are resolved within
     * the archive.
     *
     * @param path field map directory
     * @param name file name
     * @return URI of the file
     */
    protected static URI resolve(String path, String name) {
        try {
            URI base = new URI(path.replace(" ", "%20"));
            String relative = name.replace(" ", "%20");
            if (!base.isOpaque()) {
                return base.resolve(relative);
            }
            String spec = base.toString();
            int separator = spec.indexOf("!/");
            if (separator < 0) {
                throw new FieldMapException("Cannot resolve field map " + name + " against " + path);
            }
            URI entry = new URI(spec.substring(separator + 1)).resolve(relative);
            return new URI(spec.substring(0, separator + 1) + entry);
        } catch (URISyntaxException ex) {
            throw new FieldMapException("Invalid field map location " + path + name, ex);
        }
    }

    @FunctionalInterface
    private interface Parser<T> {

        FieldComponent<T> parse(InputStream stream) throws IOException;
    }

    /**
     * Opens a field map file and parses it with the parser matching its
     * format.
     *
     * @param binaryParser parser for binary files, or null if the format has
     * no binary variant
     * @throws FieldMapException if the file cannot be read or parsed
     */
    private static <T> FieldComponent<T> load(String path, String name, Parser<T> asciiParser, Parser<T> binaryParser) {
        URI uri = resolve(path, name);
        try (InputStream stream = new BufferedInputStream(uri.toURL().openStream())) {
            if (isAscii(stream)) {
                return asciiParser.parse(stream);
            }
            if (binaryParser == null) {
                throw new IOException("binary files are not supported for this field map type");
            }
            return binaryParser.parse(stream);
        } catch (IOException | RuntimeException ex) {
            throw new FieldMapException("Could not load field map " + uri + ": " + ex, ex);
        }
    }

    /**
     * Checks if a file is ASCII or binary from its first bytes, leaving the
     * stream at its start.
     */
    private static boolean isAscii(InputStream stream) throws IOException {
        stream.mark(ASCII_CHECK_BYTES);
        byte[] header = stream.readNBytes(ASCII_CHECK_BYTES);
        stream.reset();

        for (byte b : header) {
            int c = b & 0xFF;
            //(9)Horizontal Tab (10)Line feed  (11)Vertical tab (13)Carriage return (32)Space (126)tilde
            if (!(c == 9 || c == 10 || c == 11 || c == 13 || (c >= 32 && c <= 126))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Loads fieldComponent from a 1D fieldmap file.
     *
     * @param path path to the file
     * @param name
     * @return
     * @throws FieldMapException if the file cannot be read or parsed
     */
    protected final FieldComponent<double[]> loadFile1D(String path, String name) {
        return load(path, name, FieldMap::parseASCIIFile1D, null);
    }

    /**
     * Loads fieldComponent from a 2D fieldmap file.
     *
     * @param path path to the file
     * @param name
     * @return
     * @throws FieldMapException if the file cannot be read or parsed
     */
    protected final FieldComponent<double[][]> loadFile2D(String path, String name) {
        return load(path, name, FieldMap::parseASCIIFile2D, null);
    }

    /**
     * Loads fieldComponent from a 3D fieldmap file.
     *
     * @param path path to the file
     * @param name
     * @return
     * @throws FieldMapException if the file cannot be read or parsed
     */
    protected final FieldComponent<double[][][]> loadFile3D(String path, String name) {
        return load(path, name, FieldMap::parseASCIIFile3D, FieldMap::parseBinaryFile3D);
    }

    /**
     * Reads the values of an ASCII field map, separated by any whitespace.
     */
    private static final class AsciiReader {

        private static final Pattern WHITESPACE = Pattern.compile("\\s+");

        private final BufferedReader reader;
        private String[] tokens = new String[0];
        private int next;

        AsciiReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }

        /**
         * The fields of the next header line.
         */
        String[] line() throws IOException {
            String line = reader.readLine();
            if (line == null) {
                throw new EOFException("unexpected end of file in the header");
            }
            return WHITESPACE.split(line.trim());
        }

        double nextDouble() throws IOException {
            while (next == tokens.length) {
                String line = reader.readLine();
                if (line == null) {
                    throw new EOFException("fewer field values than declared in the header");
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (!hasWhitespace(line)) {
                    // Usual layout: one value per line.
                    return Double.parseDouble(line);
                }
                tokens = WHITESPACE.split(line);
                next = 0;
            }
            return Double.parseDouble(tokens[next++]);
        }

        private static boolean hasWhitespace(String line) {
            for (int i = 0; i < line.length(); i++) {
                if (Character.isWhitespace(line.charAt(i))) {
                    return true;
                }
            }
            return false;
        }
    }

    private static FieldComponent<double[]> parseASCIIFile1D(InputStream stream) throws IOException {
        FieldComponent<double[]> fieldComponent = new FieldComponent<>();
        AsciiReader reader = new AsciiReader(stream);

        String[] data = reader.line();
        int nPoints = Integer.parseInt(data[0]) + 1;
        fieldComponent.setMax(new double[]{Double.parseDouble(data[1])});
        fieldComponent.setNorm(Double.parseDouble(reader.line()[0]));

        double[] field = new double[nPoints];
        for (int i = 0; i < nPoints; i++) {
            field[i] = reader.nextDouble();
        }
        fieldComponent.setField(field);

        return fieldComponent;
    }

    private static FieldComponent<double[][]> parseASCIIFile2D(InputStream stream) throws IOException {
        FieldComponent<double[][]> fieldComponent = new FieldComponent<>();
        AsciiReader reader = new AsciiReader(stream);

        String[] data = reader.line();
        int nPointsZ = Integer.parseInt(data[0]) + 1;
        double lengthZ = Double.parseDouble(data[1]);

        data = reader.line();
        int nPointsR = Integer.parseInt(data[0]) + 1;
        double lengthR = Double.parseDouble(data[1]);

        fieldComponent.setMax(new double[]{lengthZ, lengthR});
        fieldComponent.setNorm(Double.parseDouble(reader.line()[0]));

        double[][] field = new double[nPointsZ][nPointsR];
        for (int i = 0; i < nPointsZ; i++) {
            for (int j = 0; j < nPointsR; j++) {
                field[i][j] = reader.nextDouble();
            }
        }
        fieldComponent.setField(field);

        return fieldComponent;
    }

    private static FieldComponent<double[][][]> parseASCIIFile3D(InputStream stream) throws IOException {
        FieldComponent<double[][][]> fieldComponent = new FieldComponent<>();
        AsciiReader reader = new AsciiReader(stream);

        String[] data = reader.line();
        int nPointsZ = Integer.parseInt(data[0]) + 1;
        double lengthZ = Double.parseDouble(data[1]);

        data = reader.line();
        int nPointsX = Integer.parseInt(data[0]) + 1;
        double minX = Double.parseDouble(data[1]);
        double maxX = Double.parseDouble(data[2]);

        data = reader.line();
        int nPointsY = Integer.parseInt(data[0]) + 1;
        double minY = Double.parseDouble(data[1]);
        double maxY = Double.parseDouble(data[2]);

        fieldComponent.setMin(new double[]{0., minX, minY});
        fieldComponent.setMax(new double[]{lengthZ, maxX, maxY});
        fieldComponent.setNorm(Double.parseDouble(reader.line()[0]));

        double[][][] field = new double[nPointsZ][nPointsY][nPointsX];
        for (int i = 0; i < nPointsZ; i++) {
            for (int j = 0; j < nPointsY; j++) {
                for (int k = 0; k < nPointsX; k++) {
                    field[i][j][k] = reader.nextDouble();
                }
            }
        }
        fieldComponent.setField(field);

        return fieldComponent;
    }

    /**
     * Reads exactly {@code length} bytes as a little-endian buffer.
     */
    private static ByteBuffer readBytes(InputStream stream, int length) throws IOException {
        byte[] bytes = stream.readNBytes(length);
        if (bytes.length != length) {
            throw new EOFException("expected " + length + " bytes, found " + bytes.length);
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    }

    private static FieldComponent<double[][][]> parseBinaryFile3D(InputStream stream) throws IOException {
        FieldComponent<double[][][]> fieldComponent = new FieldComponent<>();

        ByteBuffer header = readBytes(stream, 3 * INT_BYTES + 6 * DOUBLE_BYTES);
        int nPointsZ = header.getInt() + 1;
        double lengthZ = header.getDouble();
        int nPointsX = header.getInt() + 1;
        double minX = header.getDouble();
        double maxX = header.getDouble();
        int nPointsY = header.getInt() + 1;
        double minY = header.getDouble();
        double maxY = header.getDouble();
        double norm = header.getDouble();

        fieldComponent.setMin(new double[]{0., minX, minY});
        fieldComponent.setMax(new double[]{lengthZ, maxX, maxY});
        fieldComponent.setNorm(norm);

        FloatBuffer values = readBytes(stream,
                Math.multiplyExact(Math.multiplyExact(Math.multiplyExact(nPointsZ, nPointsY), nPointsX), FLOAT_BYTES)).asFloatBuffer();
        double[][][] field = new double[nPointsZ][nPointsY][nPointsX];
        for (int i = 0; i < nPointsZ; i++) {
            for (int j = 0; j < nPointsY; j++) {
                for (int k = 0; k < nPointsX; k++) {
                    field[i][j][k] = values.get();
                }
            }
        }
        fieldComponent.setField(field);

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
        File fieldMapfile = new File(resolve(path, name));
        fieldMapfile.getParentFile().mkdirs();
        PrintWriter pw = new PrintWriter(new FileWriter(fieldMapfile, StandardCharsets.UTF_8));

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
        File fieldMapfile = new File(resolve(path, name));
        fieldMapfile.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(fieldMapfile, StandardCharsets.UTF_8))) {
            double zmax = fieldComponent.getMax()[0];
            double rmax = fieldComponent.getMax()[1];
            double[][] field = fieldComponent.getField();
            pw.format(Locale.US, "%d %f%n%d %f%n%f%n", field.length - 1, zmax, field[0].length - 1, rmax, fieldComponent.getNorm());
            for (int i = 0; i < field.length; i++) {
                for (int j = 0; j < field[0].length; j++) {
                    pw.format(Locale.US, "%e%n", field[i][j]);
                }
            }
        }
    }

    /**
     * Saves given fieldComponent to a file for 3D fieldmaps.
     *
     * @param path path to the file
     * @throws IOException
     * @throws URISyntaxException
     */
    protected final void saveFile3D(String path, String name, FieldComponent<double[][][]> fieldComponent) throws IOException, URISyntaxException {
        File fieldMapfile = new File(resolve(path, name));
        fieldMapfile.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(fieldMapfile, StandardCharsets.UTF_8))) {

            double zmax = fieldComponent.getMax()[0];
            double xmin = fieldComponent.getMin()[1];
            double xmax = fieldComponent.getMax()[1];
            double ymin = fieldComponent.getMin()[2];
            double ymax = fieldComponent.getMax()[2];
            double[][][] field = fieldComponent.getField();
            // TraceWin header: "nz zmax", "nx xmin xmax", "ny ymin ymax", norm.
            pw.format(Locale.US, "%d %f%n%d %f %f%n%d %f %f%n%f%n", field.length - 1, zmax, field[0][0].length - 1, xmin, xmax, field[0].length - 1, ymin, ymax, fieldComponent.getNorm());
            for (int i = 0; i < field.length; i++) {
                for (int j = 0; j < field[0].length; j++) {
                    for (int k = 0; k < field[0][0].length; k++) {
                        pw.format(Locale.US, "%e%n", field[i][j][k]);
                    }
                }
            }
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
