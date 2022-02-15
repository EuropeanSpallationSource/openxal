package xal.extension.widgets.plot;

import java.awt.*;

/**
 * This class is a base abstract class for data used in the FunctionGraphsJPanel
 * class. This class contains 2D grid with values at the grid points. These
 * values will be presented as colored rectangles in the plot.
 *
 * @version 1.0
 * @author A. Shishlo
 */
public abstract class ColorSurfaceData {

    /**
     * The values on the 2D grid.
     */
    protected double[][] gridData = new double[0][0];

    protected int nX;
    protected int nY;

    protected double xStep;
    protected double yStep;

    protected double xMin;
    protected double xMax;
    protected double yMin;
    protected double yMax;
    protected double zMin;
    protected double zMax;

    private int nScreenX;
    private int nScreenY;

    private ColorGenerator colorGen = RainbowColorGenerator.getColorGenerator();

    /**
     * The data set constructor with size of the grid.
     */
    protected ColorSurfaceData(int nX, int nY) {
        this.nX = nX;
        this.nY = nY;
        gridData = new double[nX][nY];
        xMin = -0.5;
        xMax = 0.5;
        yMin = 0.5;
        yMax = 0.5;
        zMin = 0.;
        zMax = 0.;
        if (nX > 1) {
            xStep = (xMax - xMin) / (nX - 1);
        } else {
            xStep = (xMax - xMin);
        }
        if (nY > 1) {
            yStep = (yMax - yMin) / (nY - 1);
        } else {
            yStep = (yMax - yMin);
        }
        nScreenX = 30;
        nScreenY = 30;
        setZero();
    }

    /**
     * Sets the color generator.
     */
    public void setColorGenerator(ColorGenerator colorGen) {
        if (colorGen != null) {
            this.colorGen = colorGen;
        }
    }

    /**
     * Returns the color generator instance.
     */
    public ColorGenerator getColorGenerator() {
        return colorGen;
    }

    /**
     * Sets data size. It will clean the data inside.
     */
    public void setSize(int nX, int nY) {
        if (nX > gridData.length || nY > gridData[0].length) {
            gridData = new double[nX][nY];
        }
        this.nX = nX;
        this.nY = nY;
        if (nX > 1) {
            xStep = (xMax - xMin) / (nX - 1);
        } else {
            xStep = (xMax - xMin);
        }
        if (nY > 1) {
            yStep = (yMax - yMin) / (nY - 1);
        } else {
            yStep = (yMax - yMin);
        }
        zMin = 0.;
        zMax = 0.;
        setZero();
    }

    /**
     * Sets the screen resolution.
     */
    public void setScreenResolution(int nScreenX, int nScreenY) {
        this.nScreenX = nScreenX;
        this.nScreenY = nScreenY;
    }

    /**
     * Returns the horizontal screen resolution.
     */
    public int getScreenSizeX() {
        return nScreenX;
    }

    /**
     * Returns the vertical screen resolution.
     */
    public int getScreenSizeY() {
        return nScreenY;
    }

    /**
     * Returns the X size of the 2D array.
     */
    public int getSizeX() {
        return nX;
    }

    /**
     * Returns the Y size of the 2D array.
     */
    public int getSizeY() {
        return nY;
    }

    /**
     * Returns the X value of the grid for the index i.
     */
    public double getX(int i) {
        return (xMin + i * xStep);
    }

    /**
     * Returns the Y value of the grid for the index j.
     */
    public double getY(int j) {
        return (yMin + j * yStep);
    }

    /**
     * Returns the minimal X value of the grid.
     */
    public double getMinX() {
        return xMin;
    }

    /**
     * Returns the maximal X value of the grid.
     */
    public double getMaxX() {
        return xMax;
    }

    /**
     * Returns the minimal Y value of the grid.
     */
    public double getMinY() {
        return yMin;
    }

    /**
     * Returns the maximal Y value of the grid.
     */
    public double getMaxY() {
        return yMax;
    }

    /**
     * Returns the minimal Z value.
     */
    public double getMinZ() {
        return zMin;
    }

    /**
     * Returns the maximal Z value.
     */
    public double getMaxZ() {
        return zMax;
    }

    /**
     * Sets the minimal maximal X value of the grid.
     */
    public void setMinMaxX(double xMin, double xMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        if (nX > 1) {
            xStep = (xMax - xMin) / (nX - 1);
        } else {
            xStep = (xMax - xMin);
        }
    }

    /**
     * Sets the minimal maximal Y value of the grid.
     */
    public void setMinMaxY(double yMin, double yMax) {
        this.yMin = yMin;
        this.yMax = yMax;
        if (nY > 1) {
            yStep = (yMax - yMin) / (nY - 1);
        } else {
            yStep = (yMax - yMin);
        }
    }

    /**
     * Sets all values of the 2D array to 0.
     */
    public void setZero() {
        for (int i = 0; i < nX; i++) {
            for (int j = 0; j < nY; j++) {
                gridData[i][j] = 0.;
            }
        }
        zMin = 0.;
        zMax = 0.;
    }

    /**
     * Sets value of the 2D array with indexes i and j.
     */
    public void setValue(int i, int j, double value) {
        gridData[i][j] = value;
        if (zMin > gridData[i][j]) {
            zMin = gridData[i][j];
        }
        if (zMax < gridData[i][j]) {
            zMax = gridData[i][j];
        }
    }

    /**
     * Returns the value of the 2D array with indexes i and j.
     */
    public double getValue(int i, int j) {
        return gridData[i][j];
    }

    /**
     * Returns the interpolated value of the 2D array for x and y. The
     * subclasses should implement this method to provide specific interpolation
     * scheme.
     */
    public abstract double getValue(double x, double y);

    /**
     * Bins value into the 2D array for x and y with weight = value. The
     * subclasses should implement this method to provide specific interpolation
     * scheme.
     */
    public abstract void addValue(double x, double y, double value);

    /**
     * Bins value into the 2D array for x and y with weight = 1.
     */
    public void addValue(double x, double y) {
        addValue(x, y, 1.0);
    }

    /**
     * Multiplies all values of the 2D array by constant factor = value.
     */
    public void multiplyBy(double value) {
        zMin = Double.MAX_VALUE;
        zMax = -Double.MAX_VALUE;
        for (int i = 0; i < nX; i++) {
            for (int j = 0; j < nY; j++) {
                gridData[i][j] *= value;
                if (zMin > gridData[i][j]) {
                    zMin = gridData[i][j];
                }
                if (zMax < gridData[i][j]) {
                    zMax = gridData[i][j];
                }
            }
        }
    }

    /**
     * Calculates minimal and maximal Z values. In the beginning all data = 0.,
     * and if you want to get real min and max for Z you should use this method
     * first.
     */
    public void calcMaxMinZ() {
        zMin = Double.MAX_VALUE;
        zMax = -Double.MAX_VALUE;
        for (int i = 0; i < nX; i++) {
            for (int j = 0; j < nY; j++) {
                if (zMin > gridData[i][j]) {
                    zMin = gridData[i][j];
                }
                if (zMax < gridData[i][j]) {
                    zMax = gridData[i][j];
                }
            }
        }
    }

    /**
     * Returns the color for (x,y) point.
     */
    public Color getColor(double x, double y) {
        double value = getValue(x, y);
        if (zMax != zMin) {
            value = (value - zMin) / (zMax - zMin);
            if (value > 1.0) {
                value = 1.0;
            }
            if (value < 0.0) {
                value = 0.0;
            }
        } else {
            value = 0.;
        }
        return colorGen.getColor(value);
    }
}
