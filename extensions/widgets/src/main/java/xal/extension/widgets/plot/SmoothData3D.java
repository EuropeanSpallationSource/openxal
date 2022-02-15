package xal.extension.widgets.plot;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class is a data class for data used in the FunctionGraphsJPanel class.
 * This class contains 2D grid with values at the grid points. These values will
 * be presented as colored rectangles in the plot. It uses 9-points smooth
 * interpolation to calculate z-value between grid points.
 *
 * @version 1.0
 * @author A. Shishlo
 */
public class SmoothData3D extends ColorSurfaceData {

    private static final Logger LOGGER = Logger.getLogger(SmoothData3D.class.getName());

    /**
     * The data set constructor with size of the grid.
     */
    public SmoothData3D(int nX, int nY) {
        super(nX, nY);
    }

    /**
     * Returns the interpolated value of the 2D array for x and y.
     */
    @Override
    public double getValue(double x, double y) {
        if (x < xMin || y < yMin || x > xMax || y > yMax) {
            return zMin;
        }

        int i = (int) ((x - xMin) / xStep + 0.5);
        int j = (int) ((y - yMin) / yStep + 0.5);

        if (i < 1) {
            i = 1;
        }
        if (i > (nX - 2)) {
            i = nX - 2;
        }
        if (j < 1) {
            j = 1;
        }
        if (j > (nY - 2)) {
            j = nY - 2;
        }

        double fracX = (x - xMin - i * xStep) / xStep;
        double fracY = (y - yMin - j * yStep) / yStep;

        double wxm = 0.5 * (0.5 - fracX) * (0.5 - fracX);
        double wxp = 0.5 * (0.5 + fracX) * (0.5 + fracX);
        double wx0 = 0.75 - fracX * fracX;

        double wym = 0.5 * (0.5 - fracY) * (0.5 - fracY);
        double wyp = 0.5 * (0.5 + fracY) * (0.5 + fracY);
        double wy0 = 0.75 - fracY * fracY;

        double vm = wxm * gridData[i - 1][j - 1] + wx0 * gridData[i][j - 1] + wxp * gridData[i + 1][j - 1];
        double v0 = wxm * gridData[i - 1][j] + wx0 * gridData[i][j] + wxp * gridData[i + 1][j];
        double vp = wxm * gridData[i - 1][j + 1] + wx0 * gridData[i][j + 1] + wxp * gridData[i + 1][j + 1];

        return wym * vm + wy0 * v0 + wyp * vp;
    }

    /**
     * Bins value into the 2D array for x and y with weight = value.
     */
    @Override
    public void addValue(double x, double y, double value) {
        int i = (int) ((x - xMin) / xStep + 0.5);
        int j = (int) ((y - yMin) / yStep + 0.5);

        if (i < 1) {
            i = 1;
        }
        if (i > (nX - 2)) {
            i = nX - 2;
        }
        if (j < 1) {
            j = 1;
        }
        if (j > (nY - 2)) {
            j = nY - 2;
        }

        double fracX = (x - xMin - i * xStep) / xStep;
        double fracY = (y - yMin - j * yStep) / yStep;

        double wxm = 0.5 * (0.5 - fracX) * (0.5 - fracX);
        double wx0 = 0.75 - fracX * fracX;
        double wxp = 0.5 * (0.5 + fracX) * (0.5 + fracX);
        double wym = 0.5 * (0.5 - fracY) * (0.5 - fracY);
        double wy0 = 0.75 - fracY * fracY;
        double wyp = 0.5 * (0.5 + fracY) * (0.5 + fracY);

        double tmp = wym * value;
        gridData[i - 1][j - 1] += wxm * tmp;
        gridData[i][j - 1] += wx0 * tmp;
        gridData[i + 1][j - 1] += wxp * tmp;
        tmp = wy0 * value;
        gridData[i - 1][j] += wxm * tmp;
        gridData[i][j] += wx0 * tmp;
        gridData[i + 1][j] += wxp * tmp;
        tmp = wyp * value;
        gridData[i - 1][j + 1] += wxm * tmp;
        gridData[i][j + 1] += wx0 * tmp;
        gridData[i + 1][j + 1] += wxp * tmp;

        for (int ii = -1; ii < 2; ii++) {
            for (int jj = -1; jj < 2; jj++) {
                if (zMin > gridData[i + ii][j + jj]) {
                    zMin = gridData[i + ii][j + jj];
                }
                if (zMax < gridData[i + ii][j + jj]) {
                    zMax = gridData[i + ii][j + jj];
                }
            }
        }

    }

    /**
     * The test method of this class.
     */
    public static void main(String[] args) {
        int nx = 200;
        int ny = 300;

        ColorSurfaceData data = new SmoothData3D(nx, ny);

        double minX = -2;
        double minY = -3;
        double maxX = 2;
        double maxY = 3;
        double stepX = (maxX - minX) / (nx - 1);
        double stepY = (maxY - minY) / (ny - 1);

        data.setMinMaxX(minX, maxX);
        data.setMinMaxY(minY, maxY);

        double x;
        double y;
        double v;

        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                x = minX + i * stepX;
                y = minY + j * stepY;
                v = Math.exp(-(x * x + y * y));
                data.setValue(i, j, v);
            }
        }

        double maxDev = 0;
        int iMax = 0;
        int jMax = 0;

        for (int i = 6; i < nx - 6; i++) {
            for (int j = 6; j < ny - 6; j++) {
                x = i * stepX + minX + 0.5 * stepX;
                y = j * stepY + minY + 0.5 * stepY;
                v = Math.abs(Math.exp(-(x * x + y * y)) - data.getValue(x, y)) / Math.exp(-(x * x + y * y));
                if (maxDev < v) {
                    maxDev = v;
                    iMax = i;
                    jMax = j;
                }
            }
        }

        LOGGER.log(Level.INFO, "max dev [%] = {0}", maxDev * 100);

        double vCalc;
        x = iMax * stepX + minX + 0.5 * stepX;
        y = jMax * stepY + minY + 0.5 * stepY;
        v = Math.exp(-(x * x + y * y));
        vCalc = data.getValue(x, y);
        LOGGER.log(Level.INFO, "stepX = {0}", stepX);
        LOGGER.log(Level.INFO, "stepY = {0}", stepY);
        LOGGER.log(Level.INFO, "i_max = {0}", iMax);
        LOGGER.log(Level.INFO, "j_max = {0}", jMax);
        LOGGER.log(Level.INFO, "x = {0}", x);
        LOGGER.log(Level.INFO, "y = {0}", y);
        LOGGER.log(Level.INFO, "v = {0}", v);
        LOGGER.log(Level.INFO, "v_calc = {0}", vCalc);
    }
}
