package xal.extension.widgets.plot;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/*
 * This class is a container class for data used in the FunctionGraphsJPanel class.
 * This class interpolates y-values by using the spline interpolation.
 *
 * @version 1.0
 * @author  A. Shishlo
 */
public class CubicSplineGraphData extends BasicGraphData {

    private static final Logger LOGGER = Logger.getLogger(CubicSplineGraphData.class.getName());

    private Vector<Cubic> cY;

    /**
     * data set constructor
     */
    public CubicSplineGraphData() {
        nInterpPoints = 200;
        init(50, nInterpPoints);
    }

    /**
     * data set constructor with defined initial capacity for number of (x,y)
     * points and interpolated points
     */
    public CubicSplineGraphData(int nPoint, int nInterpPoints) {
        init(nPoint, nInterpPoints);
    }

    /**
     * returns the y-value for a certain x-value by using the spline
     * interpolation schema
     */
    @Override
    public double getValueY(double x) {
        synchronized (lockUpObj) {
            if (xyPointV.isEmpty()) {
                return (-Double.MAX_VALUE);
            }
            if (xyPointV.size() == 1) {
                return xyPointV.get(0).getY();
            }
            int i = 0;
            for (i = 0; i < xyPointV.size(); i++) {
                if (x < xyPointV.get(i).getX()) {
                    break;
                }
            }
            if (i == 0) {
                i = 1;
            }
            if (i == xyPointV.size()) {
                i = xyPointV.size() - 1;
            }
            XYpoint p1 = xyPointV.get(i - 1);
            XYpoint p2 = xyPointV.get(i);
            double u = (x - p1.getX()) / (p2.getX() - p1.getX());
            return cY.get(i - 1).eval(u);
        }
    }

    /**
     * returns the y'-value for a certain x-value by using the spline
     * interpolation schema
     */
    @Override
    public double getValueDerivativeY(double x) {
        synchronized (lockUpObj) {
            if (xyPointV.isEmpty()) {
                return (-Double.MAX_VALUE);
            }
            if (xyPointV.size() == 1) {
                return 0.0;
            }
            int i = 0;
            for (i = 0; i < xyPointV.size(); i++) {
                if (x < xyPointV.get(i).getX()) {
                    break;
                }
            }
            if (i == 0) {
                i = 1;
            }
            if (i == xyPointV.size()) {
                i = xyPointV.size() - 1;
            }
            XYpoint p1 = xyPointV.get(i - 1);
            XYpoint p2 = xyPointV.get(i);
            double u = (x - p1.getX()) / (p2.getX() - p1.getX());
            return cY.get(i - 1).evalDerivative(u) / (p2.getX() - p1.getX());
        }
    }

    /**
     * calculates the spline coefficients
     */
    @Override
    protected void calculateRepresentation() {
        int i;

        int n = this.getNumbOfPoints() - 1;
        int n1 = n + 1;
        if (n < 1) {
            return;
        }

        double[] gamma = new double[n1];
        double[] delta = new double[n1];
        double[] d = new double[n1];
        double[] z = new double[n1];

        if (cY == null) {
            cY = new Vector<>(this.getCapacity());
        }

        if (cY.size() < n1) {
            int nAddPoints = (n1 - cY.size());
            for (i = 0; i < nAddPoints; i++) {
                cY.add(new Cubic());
            }
        }

        for (i = 0; i < n1; i++) {
            z[i] = this.getY(i);
        }

        gamma[0] = 0.5;
        for (i = 1; i < n; i++) {
            gamma[i] = 1.0 / (4.0 - gamma[i - 1]);
        }
        gamma[n] = 1.0 / (2.0 - gamma[n - 1]);

        delta[0] = 3 * (z[1] - z[0]) * gamma[0];
        for (i = 1; i < n; i++) {
            delta[i] = (3 * (z[i + 1] - z[i - 1]) - delta[i - 1]) * gamma[i];
        }
        delta[n] = (3 * (z[n] - z[n - 1]) - delta[n - 1]) * gamma[n];

        d[n] = delta[n];
        for (i = n - 1; i >= 0; i--) {
            d[i] = delta[i] - gamma[i] * d[i + 1];
        }

        for (i = 0; i < n; i++) {
            cY.get(i).setCoeff(z[i], d[i], 3 * (z[i + 1] - z[i]) - 2 * d[i] - d[i + 1],
                    2 * (z[i] - z[i + 1]) + d[i] + d[i + 1]);
        }
    }

    /*
     *The inner class Cubic to keep the cubic coefficients.
     *
     * @version 1.0
     * @author  A. Shishlo
     */
    private static class Cubic {

        double a;
        double b;
        double c;
        double d;
        /* a + b*u + c*u^2 +d*u^3 */
        double e;
        double f;
        double g;

        public void setCoeff(double a, double b, double c, double d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            g = 3.0 * d;
            f = 2.0 * c;
            e = b;
        }

        /**
         * evaluate cubic
         */
        public double eval(double u) {
            return (((d * u) + c) * u + b) * u + a;
        }

        /**
         * evaluate cubic
         */
        public double evalDerivative(double u) {
            return ((g * u) + f) * u + e;
        }
    }

    /*
     *The "main" test method for debugging
     *
     * @version 1.0
     * @author  A. Shishlo
     */
    public static void main(String[] args) {
        CubicSplineGraphData spl = new CubicSplineGraphData();
        int nPoint = 20;
        double[] xV = new double[nPoint];
        double[] yV = new double[nPoint];
        LOGGER.log(Level.INFO, "Added ====As an example sin(x) has been used=======");
        LOGGER.log(Level.INFO, "Added ====x====  ====y=====");
        for (int i = 0; i < nPoint; i++) {
            xV[i] = 0.3 * i;
            yV[i] = Math.sin(xV[i]);
            spl.addPoint(xV[i], yV[i]);
            LOGGER.log(Level.INFO, "{0} {1}", new Object[]{xV[i], yV[i]});
        }

        double x;
        double y;
        double yp;
        int nGraphPoint = 50;
        LOGGER.log(Level.INFO, "==CubicSplineGraphData results========");
        LOGGER.log(Level.INFO, "====x====  ====y=====   ====derivative y====");
        double step = (spl.getMaxX() - spl.getMinX()) / nGraphPoint;

        for (int i = 0; i < nGraphPoint; i++) {
            x = spl.getMinX() + step * i + 0.5 * step;
            y = spl.getValueY(x);
            yp = spl.getValueDerivativeY(x);
            LOGGER.log(Level.INFO, "{0}  {1}  {2}", new Object[]{x, y, yp});
        }

        LOGGER.log(Level.INFO, "Stop.");
    }
}
