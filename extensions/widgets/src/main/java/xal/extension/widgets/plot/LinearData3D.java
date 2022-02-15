package xal.extension.widgets.plot;

/**
 * This class is a data class for data used in the FunctionGraphsJPanel class.
 * This class contains 2D grid with values at the grid points. These values will
 * be presented as colored rectangles in the plot. It uses 4-points linear
 * interpolation to calculate z-value between grid points.
 *
 * @version 1.0
 * @author A. Shishlo
 */
public class LinearData3D extends ColorSurfaceData {

    /**
     * The data set constructor with size of the grid.
     */
    public LinearData3D(int nX, int nY) {
        super(nX, nY);
    }

    /**
     * Returns the interpolated value of the 2D array for x and y.
     */
    @Override
    public double getValue(double x, double y) {
        int i;
        int j;
        double fracX;
        double fracY;
        double wxm;
        double wxp;
        double wym;
        double wyp;
        double vm;
        double vp;

        if (x < xMin || y < yMin || x > xMax || y > yMax) {
            return zMin;
        }

        i = (int) ((x - xMin) / xStep + 0.5);
        j = (int) ((y - yMin) / yStep + 0.5);

        fracX = (x - xMin - i * xStep) / xStep;
        fracY = (y - yMin - j * yStep) / yStep;

        if (fracX < 0.) {
            i = i - 1;
        }

        if (fracY < 0.) {
            j = j - 1;
        }

        if (i < 0) {
            i = 0;
        }
        if (i > (nX - 2)) {
            i = nX - 2;
        }
        if (j < 0) {
            j = 0;
        }
        if (j > (nY - 2)) {
            j = nY - 2;
        }

        fracX = (x - xMin - i * xStep) / xStep;
        fracY = (y - yMin - j * yStep) / yStep;

        wxm = 1.0 - fracX;
        wxp = fracX;

        wym = 1.0 - fracY;
        wyp = fracY;

        vm = wxm * gridData[i][j] + wxp * gridData[i + 1][j];
        vp = wxm * gridData[i][j + 1] + wxp * gridData[i + 1][j + 1];

        return wym * vm + wyp * vp;
    }

    /**
     * Bins value into the 2D array for x and y with weight = value.
     */
    @Override
    public void addValue(double x, double y, double value) {
        int i;
        int j;
        double fracX;
        double fracY;
        double wxm;
        double wxp;
        double wym;
        double wyp;
        double tmp;

        i = (int) ((x - xMin) / xStep + 0.5);
        j = (int) ((y - yMin) / yStep + 0.5);

        fracX = (x - xMin - i * xStep) / xStep;
        fracY = (y - yMin - j * yStep) / yStep;

        if (fracX < 0.) {
            i = i - 1;
        }

        if (fracY < 0.) {
            j = j - 1;
        }

        if (i < 0) {
            i = 0;
        }
        if (i > (nX - 2)) {
            i = nX - 2;
        }
        if (j < 0) {
            j = 0;
        }
        if (j > (nY - 2)) {
            j = nY - 2;
        }

        fracX = (x - xMin - i * xStep) / xStep;
        fracY = (y - yMin - j * yStep) / yStep;

        wxm = 1.0 - fracX;
        wxp = fracX;

        wym = 1.0 - fracY;
        wyp = fracY;

        tmp = wym * value;
        gridData[i][j] += wxm * tmp;
        gridData[i + 1][j] += wxp * tmp;
        tmp = wyp * value;
        gridData[i][j + 1] += wxm * tmp;
        gridData[i + 1][j + 1] += wxp * tmp;

        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                if (zMin > gridData[i + ii][j + jj]) {
                    zMin = gridData[i + ii][j + jj];
                }
                if (zMax < gridData[i + ii][j + jj]) {
                    zMax = gridData[i + ii][j + jj];
                }
            }
        }
    }
}
