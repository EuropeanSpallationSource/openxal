/*
 *  Gaussian.java
 *
 *  Created on November 22, 2004, 10:59 AM
 */
package xal.extension.fit.lsm;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class is for data fitting with Gaussian function. The Gaussian function
 * form used in this class is y = pedestal+amp*exp(-(x-center)^2/(sigma^2/2.)).
 *
 * @author shishlo
 */
public class Gaussian {

    private static final Logger LOGGER = Logger.getLogger(Gaussian.class.getName());

    private double sigma = 0.5;
    private double amp = 1.;
    private double center = 0.;
    private double pedestal = 0.;

    private double sigmaErr = 0.;
    private double ampErr = 0.;
    private double centerErr = 0.;
    private double pedestalErr = 0.;

    private boolean sigmaIncl = true;
    private boolean ampIncl = true;
    private boolean centerIncl = true;
    private boolean pedestalIncl = true;

    private ModelFunction1D mf = null;

    private SolverLSM solver = new SolverLSM();

    private DataStore ds = new DataStore();

    private double[] a = new double[4];
    private double[] aErr = new double[4];

    private double[] xTmp = new double[1];

    /**
     * The "sigma" parameter
     */
    public static final String SIGMA_STR = "sigma";
    /**
     * The "amplitude" parameter
     */
    public static final String AMP_STR = "amplitude";
    /**
     * The "center" parameter
     */
    public static final String CENTER_STR = "center";
    /**
     * The "pedestal" parameter
     */
    public static final String PEDESTAL_STR = "pedestal";

    /**
     * Creates a new instance of Gaussian
     */
    public Gaussian() {
        init();
    }

    /**
     * Description of the Method
     */
    private void init() {

        mf = new ModelFunction1D() {

            @Override
            public double getValue(double x, double[] a) {
                if (a.length != 4) {
                    return 0.;
                }

                return a[3] + a[1] * Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0]));
            }

            @Override
            public double getDerivative(double x, double[] a, int aIndex) {
                double res = 0.;
                if (a.length != 4) {
                    return 0.;
                }
                switch (aIndex) {
                    case 0:
                        res = a[1] * (x - a[2]) * (x - a[2]) * Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0])) / (a[0] * a[0] * a[0]);
                        break;
                    case 1:
                        res = Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0]));
                        break;
                    case 2:
                        res = a[1] * (x - a[2]) * Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0])) / (a[0] * a[0]);
                        break;
                    case 3:
                        res = 1.0;
                        break;
                    default:
                        break;
                }

                return res;
            }

        };
    }

    /**
     * Sets parameters array from all parameters
     */
    private void updateParams() {
        a[0] = sigma;
        a[1] = amp;
        a[2] = center;
        a[3] = pedestal;
    }

    /**
     * Returns the parameter value
     *
     * @param key The parameter name
     * @return The parameter value
     */
    public double getParameter(String key) {
        if (key.equals(SIGMA_STR)) {
            return sigma;
        } else if (key.equals(AMP_STR)) {
            return amp;
        } else if (key.equals(CENTER_STR)) {
            return center;
        } else if (key.equals(PEDESTAL_STR)) {
            return pedestal;
        }
        return 0.;
    }

    /**
     * Returns the parameter value error
     *
     * @param key The parameter name
     * @return The parameter value error
     */
    public double getParameterError(String key) {
        if (key.equals(SIGMA_STR)) {
            return sigmaErr;
        } else if (key.equals(AMP_STR)) {
            return ampErr;
        } else if (key.equals(CENTER_STR)) {
            return centerErr;
        } else if (key.equals(PEDESTAL_STR)) {
            return pedestalErr;
        }
        return 0.;
    }

    /**
     * Includes or excludes the parameter into fitting
     *
     * @param key The parameter name
     * @param incl The boolean variable about including variable into the
     * fitting
     */
    public void fitParameter(String key, boolean incl) {
        if (key.equals(SIGMA_STR)) {
            sigmaIncl = incl;
        } else if (key.equals(AMP_STR)) {
            ampIncl = incl;
        } else if (key.equals(CENTER_STR)) {
            centerIncl = incl;
        } else if (key.equals(PEDESTAL_STR)) {
            pedestalIncl = incl;
        }
    }

    /**
     * Returns the boolean variable about including variable into the fitting
     *
     * @param key The parameter name
     */
    public boolean fitParameter(String key) {
        if (key.equals(SIGMA_STR)) {
            return sigmaIncl;
        } else if (key.equals(AMP_STR)) {
            return ampIncl;
        } else if (key.equals(CENTER_STR)) {
            return centerIncl;
        } else if (key.equals(PEDESTAL_STR)) {
            return pedestalIncl;
        }
        return false;
    }

    /**
     * Sets the parameter value
     *
     * @param key The parameter name
     * @param val The new parameter value
     */
    public void setParameter(String key, double val) {
        if (key.equals(SIGMA_STR)) {
            sigma = val;
        } else if (key.equals(AMP_STR)) {
            amp = val;
        } else if (key.equals(CENTER_STR)) {
            center = val;
        } else if (key.equals(PEDESTAL_STR)) {
            pedestal = val;
        }
        updateParams();
    }

    /**
     * Sets the data attribute of the Gaussian object
     *
     * @param yArr Y data array
     * @param yErrArr Y values error array
     * @param xArr The new data value
     */
    public void setData(double[] xArr,
            double[] yArr,
            double[] yErrArr) {

        ds.clear();

        if (xArr.length != yArr.length) {
            return;
        }

        double[] x = new double[1];

        for (int i = 0; i < xArr.length; i++) {
            x[0] = xArr[i];
            if (yErrArr != null) {
                ds.addRecord(yArr[i], yErrArr[i], x);
            } else {
                ds.addRecord(yArr[i], x);
            }
        }
    }

    /**
     * Sets the data attribute of the Gaussian object
     *
     * @param yArr Y data array
     * @param xArr The new data value
     */
    public void setData(double[] xArr,
            double[] yArr) {
        setData(xArr, yArr, null);
    }

    /**
     * Removes all internal data
     */
    public void clear() {
        ds.clear();
    }

    /**
     * Adds a data point to the internal data
     *
     * @param x The x value
     * @param y The y value
     */
    public void addData(double x, double y) {
        xTmp[0] = x;
        ds.addRecord(y, xTmp);
    }

    /**
     * Adds a data point to the internal data
     *
     * @param x The x value
     * @param y The y valu
     * @param yErr The error of the y value
     */
    public void addData(double x, double y, double yErr) {
        xTmp[0] = x;
        ds.addRecord(y, yErr, xTmp);
    }

    /**
     * perform the data fit
     *
     * @param iteration The number of iterations
     * @return Success or not
     */
    public boolean fit(int iteration) {
        for (int i = 0; i < iteration; i++) {
            if (!fit()) {
                return false;
            }
        }
        return true;
    }

    /**
     * perform one step of the data fit
     *
     * @return Success or not
     */
    public boolean fit() {

        boolean[] mask = new boolean[4];
        mask[0] = sigmaIncl;
        mask[1] = ampIncl;
        mask[2] = centerIncl;
        mask[3] = pedestalIncl;

        updateParams();

        aErr[0] = 0.;
        aErr[1] = 0.;
        aErr[2] = 0.;
        aErr[3] = 0.;

        boolean res = solver.solve(ds, mf, a, aErr, mask);

        if (res) {
            sigma = a[0];
            amp = a[1];
            center = a[2];
            pedestal = a[3];

            sigmaErr = aErr[0];
            ampErr = aErr[1];
            centerErr = aErr[2];
            pedestalErr = aErr[3];
        }

        return res;
    }

    /**
     * Perform the several iterations of the data fit with guessing the initial
     * values of parameters
     *
     * @param iteration The number of iterations
     * @return Success or not
     */
    public boolean guessAndFit(int iteration) {

        if (!guessAndFit()) {
            return false;
        }

        for (int i = 1; i < iteration; i++) {
            if (!fit()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the parameters of Gaussian with initial values defined from raw
     * data
     *
     * @return The true is the initial parameters have been defined successfully
     */
    public boolean guessAndFit() {
        int n = ds.size();
        double yMin = Double.MAX_VALUE;
        double yMax = -Double.MAX_VALUE;
        double y;
        for (int i = 0; i < n; i++) {
            y = ds.getY(i);
            if (y > yMax) {
                yMax = y;
            }
            if (y < yMin) {
                yMin = y;
            }
        }
        if (yMin > yMax) {
            return false;
        }
        double yLevel = 0.607 * (yMax - yMin) + yMin;

        double xMin = Double.MAX_VALUE;
        double xMax = -Double.MAX_VALUE;
        for (int i = 1; i < n; i++) {
            if ((yLevel - ds.getY(i - 1)) * (yLevel - ds.getY(i)) <= 0.) {
                if (xMin > ds.getArrX(i)[0]) {
                    xMin = ds.getArrX(i)[0];
                }
                if (xMax < ds.getArrX(i)[0]) {
                    xMax = ds.getArrX(i)[0];
                }
            }
        }
        if (xMax <= xMin) {
            return false;
        }

        sigma = Math.abs(xMin - xMax) / 2.0;
        center = (xMin + xMax) / 2.0;
        pedestal = Math.min(Math.abs(yMin), Math.abs(yMax));
        amp = (yMax - yMin);

        return fit();
    }

    /**
     * Returns the value of Gaussian function
     *
     * @param x The x-value
     * @return The Gauss function value
     */
    public double getValue(double x) {
        return mf.getValue(x, a);
    }

    /**
     * MAIN for debugging
     *
     * @param args The array of strings as parameters
     */
    public static void main(String[] args) {

        double p = 0.0;
        double a = 1.5;
        double c = 0.4;
        double s = 0.2;

        int n = 10;
        double xMin = c - 3 * s;
        double xMax = c + 3 * s;
        double step = (xMax - xMin) / (n - 1);

        double[] xArr = new double[n];
        double[] yArr = new double[n];

        double x;
        double errLevel = 0.05;

        for (int i = 0; i < n; i++) {
            x = xMin + step * i;
            xArr[i] = x;
            yArr[i] = p + a * Math.exp(-(x - c) * (x - c) / (2. * s * s));
            yArr[i] = yArr[i] * (1.0 + errLevel * 2.0 * (Math.random() - 0.5));
        }

        Gaussian gs = new Gaussian();

        gs.setData(xArr, yArr);

        gs.setParameter(Gaussian.SIGMA_STR, s * 1.5);
        gs.setParameter(Gaussian.AMP_STR, a * 0.9);
        gs.setParameter(Gaussian.CENTER_STR, c * 1.2);
        gs.setParameter(Gaussian.PEDESTAL_STR, p * 0.9);

        gs.fitParameter(Gaussian.SIGMA_STR, true);
        gs.fitParameter(Gaussian.AMP_STR, true);
        gs.fitParameter(Gaussian.CENTER_STR, true);
        gs.fitParameter(Gaussian.PEDESTAL_STR, true);

        LOGGER.log(Level.INFO, "================START================");
        LOGGER.log(Level.INFO, "data error level [%]= {0}", errLevel * 100);
        LOGGER.log(Level.INFO, "Main ini: s = {0}", s);
        LOGGER.log(Level.INFO, "Main ini: a = {0}", a);
        LOGGER.log(Level.INFO, "Main ini: c = {0}", c);
        LOGGER.log(Level.INFO, "Main ini: p = {0}", p);

        int nIter = 8;

        boolean res = gs.guessAndFit();

        for (int j = 0; j < nIter; j++) {
            LOGGER.log(Level.INFO, "Main: iteration = {0}  res = {1}", new Object[]{j, res});
            LOGGER.log(Level.INFO, "Main: s = {0} +- {1}", new Object[]{gs.getParameter(Gaussian.SIGMA_STR), gs.getParameterError(Gaussian.SIGMA_STR)});
            LOGGER.log(Level.INFO, "Main: a = {0} +- {1}", new Object[]{gs.getParameter(Gaussian.AMP_STR), gs.getParameterError(Gaussian.AMP_STR)});
            LOGGER.log(Level.INFO, "Main: c = {0} +- {1}", new Object[]{gs.getParameter(Gaussian.CENTER_STR), gs.getParameterError(Gaussian.CENTER_STR)});
            LOGGER.log(Level.INFO, "Main: p = {0} +- {1}", new Object[]{gs.getParameter(Gaussian.PEDESTAL_STR), gs.getParameterError(Gaussian.PEDESTAL_STR)});
            res = gs.fit();
        }

        for (int i = 0; i < n; i++) {
            x = xMin + step * i;
            LOGGER.log(Level.INFO, "i={0} x={1} y_ini={2} model={3}", new Object[]{i, x, yArr[i], gs.getValue(x)});
        }

        nIter = 100;
        java.util.Date start = new java.util.Date();
        for (int j = 0; j < nIter; j++) {
            gs.fit();
        }
        java.util.Date stop = new java.util.Date();
        double time = (stop.getTime() - start.getTime()) / 1000.;
        time /= nIter;
        LOGGER.log(Level.INFO, "time for one step [sec] ={0}", time);
    }
}
