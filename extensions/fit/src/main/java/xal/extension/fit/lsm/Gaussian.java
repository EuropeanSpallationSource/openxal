/*
 *  Gaussian.java
 *
 *  Created on November 22, 2004, 10:59 AM
 */
package xal.extension.fit.lsm;

/**
 * This class is for data fitting with Gaussian function. The Gaussian function
 * form used in this class is y = pedestal+amp*exp(-(x-center)^2/(sigma^2/2.)).
 *
 * @author shishlo
 */
public class Gaussian {

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
    public static String SIGMA = "sigma";
    /**
     * The "amplitude" parameter
     */
    public static String AMP = "amplitude";
    /**
     * The "center" parameter
     */
    public static String CENTER = "center";
    /**
     * The "pedestal" parameter
     */
    public static String PEDESTAL = "pedestal";

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

        mf
                = new ModelFunction1D() {

            @Override
            public double getValue(double x, double[] a) {
                if (a.length != 4) {
                    return 0.;
                }

                double res = a[3] + a[1] * Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0]));

                return res;
            }

            @Override
            public double getDerivative(double x, double[] a, int a_index) {
                double res = 0.;
                if (a.length != 4) {
                    return 0.;
                }
                switch (a_index) {
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
        if (key.equals(SIGMA)) {
            return sigma;
        } else if (key.equals(AMP)) {
            return amp;
        } else if (key.equals(CENTER)) {
            return center;
        } else if (key.equals(PEDESTAL)) {
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
        if (key.equals(SIGMA)) {
            return sigmaErr;
        } else if (key.equals(AMP)) {
            return ampErr;
        } else if (key.equals(CENTER)) {
            return centerErr;
        } else if (key.equals(PEDESTAL)) {
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
        if (key.equals(SIGMA)) {
            sigmaIncl = incl;
        } else if (key.equals(AMP)) {
            ampIncl = incl;
        } else if (key.equals(CENTER)) {
            centerIncl = incl;
        } else if (key.equals(PEDESTAL)) {
            pedestalIncl = incl;
        }
    }

    /**
     * Returns the boolean variable about including variable into the fitting
     *
     * @param key The parameter name
     */
    public boolean fitParameter(String key) {
        if (key.equals(SIGMA)) {
            return sigmaIncl;
        } else if (key.equals(AMP)) {
            return ampIncl;
        } else if (key.equals(CENTER)) {
            return centerIncl;
        } else if (key.equals(PEDESTAL)) {
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
        if (key.equals(SIGMA)) {
            sigma = val;
        } else if (key.equals(AMP)) {
            amp = val;
        } else if (key.equals(CENTER)) {
            center = val;
        } else if (key.equals(PEDESTAL)) {
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
        double y = 0.;
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
        int n_cross = 0;
        double xMin = Double.MAX_VALUE;
        double xMax = -Double.MAX_VALUE;
        for (int i = 1; i < n; i++) {
            if ((yLevel - ds.getY(i - 1)) * (yLevel - ds.getY(i)) <= 0.) {
                n_cross++;
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

        boolean res = fit();
        return res;
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
    public static void main(String args[]) {

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

        double x = 0.;
        double err_level = 0.05;

        for (int i = 0; i < n; i++) {
            x = xMin + step * i;
            xArr[i] = x;
            yArr[i] = p + a * Math.exp(-(x - c) * (x - c) / (2. * s * s));
            yArr[i] = yArr[i] * (1.0 + err_level * 2.0 * (Math.random() - 0.5));
        }

        Gaussian gs = new Gaussian();

        gs.setData(xArr, yArr);

        gs.setParameter(Gaussian.SIGMA, s * 1.5);
        gs.setParameter(Gaussian.AMP, a * 0.9);
        gs.setParameter(Gaussian.CENTER, c * 1.2);
        gs.setParameter(Gaussian.PEDESTAL, p * 0.9);

        gs.fitParameter(Gaussian.SIGMA, true);
        gs.fitParameter(Gaussian.AMP, true);
        gs.fitParameter(Gaussian.CENTER, true);
        gs.fitParameter(Gaussian.PEDESTAL, true);

        System.out.println("================START================");
        System.out.println("data error level [%]= " + err_level * 100);
        System.out.println("Main ini: s = " + s);
        System.out.println("Main ini: a = " + a);
        System.out.println("Main ini: c = " + c);
        System.out.println("Main ini: p = " + p);

        int n_iter = 8;

        boolean res = false;

        res = gs.guessAndFit();

        for (int j = 0; j < n_iter; j++) {
            System.out.println("Main: iteration =" + j + "  res = " + res);
            System.out.println("Main: s = " + gs.getParameter(Gaussian.SIGMA) + " +- " + gs.getParameterError(Gaussian.SIGMA));
            System.out.println("Main: a = " + gs.getParameter(Gaussian.AMP) + " +- " + gs.getParameterError(Gaussian.AMP));
            System.out.println("Main: c = " + gs.getParameter(Gaussian.CENTER) + " +- " + gs.getParameterError(Gaussian.CENTER));
            System.out.println("Main: p = " + gs.getParameter(Gaussian.PEDESTAL) + " +- " + gs.getParameterError(Gaussian.PEDESTAL));
            res = gs.fit();
        }

        for (int i = 0; i < n; i++) {
            x = xMin + step * i;
            System.out.println("i=" + i + " x=" + x + " y_ini=" + yArr[i] + " model=" + gs.getValue(x));
        }

        n_iter = 100;
        java.util.Date start = new java.util.Date();
        for (int j = 0; j < n_iter; j++) {
            res = gs.fit();
        }
        java.util.Date stop = new java.util.Date();
        double time = (stop.getTime() - start.getTime()) / 1000.;
        time /= n_iter;
        System.out.println("time for one step [sec] =" + time);

    }

}
