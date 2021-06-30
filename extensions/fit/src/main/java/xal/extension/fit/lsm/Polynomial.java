package xal.extension.fit.lsm;

import java.util.logging.Logger;
import java.util.logging.Level;
import xal.tools.text.ScientificNumberFormat;

/**
 * This class is for data fitting with polynomial equation.
 *
 * @author shishlo
 */
public class Polynomial {

    private static final Logger LOGGER = Logger.getLogger(Polynomial.class.getName());

    static double[] fact = new double[100];

    static {
        fact[0] = 1.0;
        for (int i = 1; i < fact.length; i++) {
            fact[i] = fact[i - 1] * i;
        }
    }

    private double[] a = new double[0];
    private double[] errArr = new double[0];
    private boolean[] mask = new boolean[0];

    private ModelFunction1D mf = null;

    private SolverLSM solver = new SolverLSM();

    private DataStore ds = new DataStore();
    private DataStore dsTmp = new DataStore();

    private double[] xTmp = new double[1];

    private ScientificNumberFormat frmt = new ScientificNumberFormat(4);

    /**
     * Creates a new instance of Polynomial
     */
    public Polynomial() {
        init();
        setOrder(1);
    }

    /**
     * Creates a new instance of Polynomial
     *
     * @param n The order of the Polynomial object
     */
    public Polynomial(int n) {
        init();
        setOrder(n);
    }

    /**
     * Description of the Method
     */
    private void init() {

        mf
                = new ModelFunction1D() {
            @Override
            public double getValue(double x, double[] a) {
                double res = 0.;
                double x_pow = 1.;
                for (int i = 0; i < a.length; i++) {
                    res += x_pow * a[i];
                    x_pow *= x;
                }
                return res;
            }

            @Override
            public double getDerivative(double x, double[] a, int indexArr) {
                double res = 1.;
                for (int i = 0; i < indexArr; i++) {
                    res *= x;
                }
                return res;
            }

        };
    }

    /**
     * Returns the parameter value
     *
     * @param index The coefficient for power "index" of the polynomial
     * @return The parameter value
     */
    public double getParameter(int index) {
        return a[index];
    }

    /**
     * Returns the parameter value error
     *
     * @param index The coefficient index for power equals to "index" in the
     * polynomial
     * @return The parameter value error
     */
    public double getParameterError(int index) {
        return errArr[index];
    }

    /**
     * Includes or excludes the parameter into fitting
     *
     * @param index The coefficient index for power equals to "index" in the
     * polynomial
     * @param fitting The boolean variable about including the coefficient into
     * the fitting
     */
    public void fitParameter(int index, boolean fitting) {
        mask[index] = fitting;
    }

    /**
     * Returns the boolean variable about including the coefficient into the
     * fitting
     *
     * @param index The coefficient index for power equals to "index" in the
     * polynomial
     * @return fitting The boolean variable about including the coefficient into
     * the fitting
     */
    public boolean fitParameter(int index) {
        return mask[index];
    }

    /**
     * Sets the parameter value
     *
     * @param val The new parameter value
     * @param index he coefficient index for power equals to "index" in the
     * polynomial
     */
    public void setParameter(int index, double val) {
        a[index] = val;
    }

    /**
     * Sets the data attribute of the Polynomial object
     *
     * @param yArr Y data array
     * @param yErrArr Y values error array
     * @param xArr The new data value
     */
    public void setData(double[] xArr,
            double[] yArr,
            double[] yErrArr) {

        ds.clear();
        setToZero();

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
     * Sets the data attribute of the Polynomial object
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
        setToZero();
    }

    /**
     * Sets all coefficients and errors to zero value
     */
    private void setToZero() {
        for (int i = 0; i < a.length; i++) {
            if (mask[i]) {
                a[i] = 0.;
            }
            errArr[i] = 0.;
        }
    }

    /**
     * Sets the order of the Polynomial object
     *
     * @param n The new order value
     */
    public void setOrder(int n) {
        a = new double[n + 1];
        errArr = new double[n + 1];
        mask = new boolean[n + 1];
        for (int i = 0; i < mask.length; i++) {
            mask[i] = true;
        }
        setToZero();
    }

    /**
     * Returns the order of the Polynomial object
     *
     * @return The order
     */
    public int getOrder() {
        return (a.length - 1);
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
     * @param y The y value
     * @param y_err The error of the y value
     */
    public void addData(double x, double y, double y_err) {
        xTmp[0] = x;
        ds.addRecord(y, y_err, xTmp);
    }

    /**
     * It performs one step of the data fit
     *
     * @return Success or not
     */
    public boolean fit() {
        setToZero();
        boolean res = solver.solve(ds, mf, a, errArr, mask);
        return res;
    }

    /**
     * It performs one step of the data fit by using centered data. This method
     * could be more accurate in some cases, but you cannot use masks to
     * eliminate fitting of some polynomial coefficients.
     *
     * @return Success or not
     */
    public boolean fitFromCenter() {
        setToZero();
        if (a.length >= fact.length) {
            return false;
        }
        //center data and store in the temporary data container
        double xAvg = 0.;
        double yAvg = 0.;
        int nData = ds.size();
        if (nData == 0) {
            return false;
        }

        for (int i = 0; i < nData; i++) {
            xAvg = xAvg + ds.getArrX(i)[0];
            yAvg = yAvg + ds.getY(i);
        }
        xAvg = xAvg / nData;
        yAvg = yAvg / nData;

        dsTmp.clear();
        for (int i = 0; i < nData; i++) {
            dsTmp.addRecord(ds.getY(i) - yAvg, ds.getErrY(i), ds.getArrX(i)[0] - xAvg);
        }

        //prepare resulting arrays
        double[] tmpArr = new double[a.length];
        double[] errTmpArr = new double[a.length];
        boolean[] maskTmp = new boolean[a.length];
        for (int i = 0; i < a.length; i++) {
            tmpArr[i] = 0.;
            errTmpArr[i] = 0.;
            maskTmp[i] = true;
        }

        boolean res = solver.solve(dsTmp, mf, tmpArr, errTmpArr, maskTmp);

        //shift x and y back 
        if (res != false) {
            for (int j = 0; j < a.length; j++) {
                double s = 0.;
                double s2 = 0.;
                double x = 1.0;
                double cij = 0.;
                for (int i = j; i < a.length; i++) {
                    cij = fact[i] / (fact[j] * fact[i - j]);
                    s = s + tmpArr[i] * x * cij;
                    s2 = s2 + (errTmpArr[i] * x * cij) * (errTmpArr[i] * x * cij);
                    x = -x * xAvg;
                }
                a[j] = s;
                errArr[j] = Math.sqrt(s2);
            }
            a[0] = a[0] + yAvg;
        }
        return res;
    }

    /**
     * Returns the value of Polynomial function
     *
     * @param x The x-value
     * @return The polynomial function value
     */
    public double getValue(double x) {
        return mf.getValue(x, a);
    }

    /**
     * Returns the value of Polynomial function
     *
     * @param x The x-value
     * @param a The array of coefficients
     * @return The polynomial equation value
     */
    public double getValue(double x, double[] a) {
        return mf.getValue(x, a);
    }

    /**
     * Returns the array with the coefficients of the Polynomial
     *
     * @return The array with the coefficients of the Polynomial
     */
    public double[] getCoefficients() {
        double[] newArr = new double[a.length];
        System.arraycopy(a, 0, newArr, 0, a.length);
        return newArr;
    }

    /**
     * Returns the array with the errors of the coefficients of the Polynomial
     *
     * @return The array with the errors of the coefficients of the Polynomial
     */
    public double[] getCoefficientsErr() {
        double[] errNewArr = new double[errArr.length];
        System.arraycopy(errArr, 0, errNewArr, 0, errArr.length);
        return errNewArr;
    }

    /**
     * Return the characteristic equation as a String
     *
     * @param frmtLoc The format for coefficients
     * @return The characteristic equation as a String
     */
    private String equation(final ScientificNumberFormat frmtLoc) {
        String eq;
        eq = "Y = ";
        if (a.length <= 0) {
            return eq;
        }
        if (mask[0] == false && a[0] == 0.) {
        } else {
            eq += "(" + frmtLoc.format(a[0])
                    + " +- " + frmtLoc.format(errArr[0]) + ")";
        }
        for (int i = 1; i < a.length; i++) {
            if (mask[i] == false && a[i] == 0.) {
            } else {
                eq += " + x^" + i
                        + "*(" + frmtLoc.format(a[i])
                        + " +- " + frmtLoc.format(errArr[i]) + ")";
            }
        }
        return eq;
    }

    /**
     * Return the characteristic equation as a String
     *
     * @return The characteristic equation as a String
     */
    public String equation() {
        return equation(frmt);
    }

    /**
     * MAIN for debugging
     *
     * @param args The array of strings as parameters
     */
    public static void main(String args[]) {

        int n = 4;
        double[] x = new double[n];
        double[] y = new double[n];
        double[] yErr = new double[n];

        double a0 = 0.5;
        double a3 = 1.5;
        double a5 = 0.25;

        for (int i = 0; i < n; i++) {
            x[i] = i;
            y[i] = a0 + a3 * Math.pow(x[i], 3.0) + a5 * Math.pow(x[i], 5.0);
            yErr[i] = 1;
        }

        int nPoly = 6;
        Polynomial gs = new Polynomial(nPoly);

        gs.fitParameter(1, false);
        gs.fitParameter(2, false);
        gs.fitParameter(4, false);
        gs.fitParameter(6, false);

        gs.setData(x, y, yErr);
        boolean res = gs.fit();

        LOGGER.log(Level.INFO, "result = {0}", res);
        LOGGER.log(Level.INFO, "Exact val a0 = {0}", a0);
        LOGGER.log(Level.INFO, "Exact val a3 = {0}", a3);
        LOGGER.log(Level.INFO, "Exact val a5 = {0}", a5);

        for (int i = 0; i <= nPoly; i++) {
            LOGGER.log(Level.INFO, "i = {0}  a={1} err={2}", new Object[]{i, gs.getParameter(i), gs.getParameterError(i)});
        }

        LOGGER.log(Level.INFO, "eq:{0}", gs.equation());

    }

}
