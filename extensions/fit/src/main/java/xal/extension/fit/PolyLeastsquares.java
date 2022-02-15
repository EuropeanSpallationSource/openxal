package xal.extension.fit;

import xal.tools.math.GenericMatrix;
import xal.tools.math.GenericVector;

/**
 *
 * @author jdg
 *
 * This is taken from M. Abdallah & J. Wang, U. Md., 1999
 *
 * LEAST SQUARES ANALYSIS
 *
 * This program will input a series of m 2-D points and create a best fit
 * polynomial equation. The governing equation is of the form: y = co + c1*x +
 * ... + ck*x^k
 *
 * The system of equations becomes: Y= A*C The least square normal equation
 * looks like: A'AC = AY; where, C = (c0, c1, ..., ck)'
 *
 * The least squares result is computed through the normal equation: A'*A*C =
 * A*Y; where the equation is represented by AC=Y. and :
 *
 * (dX, dY)- coordinate arrays for data points m- int for number of data points
 * k- int for order of poly equation mC- Matrix for the constant coefficients of
 * equation dR- correlation coefficient getValue(dx)- returns value of y
 * coordinate from equation Correlation()- returns the correlation coefficient
 * as a String Equation()- returns the characteristic equation as a String
 */
public class PolyLeastsquares {

    /**
     * number of data points
     */
    private int m;
    /**
     * order of best fit equation
     */
    private int k;

    /**
     * points to fit
     */
    private double[] dX;
    private double[] dY;

    /**
     * equation coefficient vector
     */
    private GenericVector mC;

    /**
     * constructor
     */
    public PolyLeastsquares(double[] dX, double[] dY, int k) throws PolyLeastsquaresException {

        this.dY = dY;
        this.dX = dX;

        if (dX.length != dY.length) {
            throw new PolyLeastsquaresException("X & Y not equal length");
        }

        if (dX.length <= k) {
            throw new PolyLeastsquaresException("You requested too high a polynimial order, for the number of points");
        }
        this.m = dX.length;
        this.k = k;
        init();
    }

    /**
     * Initialize various stuff
     */
    private void init() {
        double[][] dCols = new double[m][k + 1];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < k + 1; j++) {
                dCols[i][j] = Math.pow(dX[i], j);
            }
        }
        GenericMatrix mA = new GenericMatrix(dCols);
        GenericVector mY = new GenericVector(dY);

        GenericMatrix mAl = mA.transpose().times(mA);
        mC = mAl.inverse().times(mA.transpose()).times(mY);
    }

    /**
     * Calculate the predicted y value for an inputed x
     */
    public double getValue(double dX) {
        double predY = 0;

        for (int j = 0; j < k + 1; j++) {
            predY += mC.getElem(j) * Math.pow(dX, j);
        }

        return predY;
    }

    /**
     * Find the correlation coefficient for the fit
     */
    public String correlation() {
        //dR is the correlation coefficient
        double[] dYfit = new double[m];

        //to calculate dYfit:
        for (int i = 0; i < m; i++) {

            dYfit[i] = mC.getElem(0);
            for (int j = 1; j < k + 1; j++) {
                dYfit[i] += mC.getElem(j) * Math.pow(dX[i], j);
            }
        }

        //  calculate square sums:
        double dMean = dY[0] / m;
        for (int i = 1; i < m; i++) {
            dMean += dY[i] / m;
        }

        double dVarUnexplained = Math.pow(dY[0] - dYfit[0], 2);
        double dVarTotal = Math.pow(dY[0] - dMean, 2);
        for (int i = 1; i < m; i++) {
            dVarUnexplained += Math.pow(dY[i] - dYfit[i], 2);
            dVarTotal += Math.pow(dY[i] - dMean, 2);
        }

        //calculate correlation coefficient:
        double dR = Math.sqrt(1 - dVarUnexplained / dVarTotal);

        //to round it to 4 decimal places
        dR = Math.round(dR * 10000.0) / 10000.0;

        String sR = "" + dR;
        if (dR > 1) {
            sR = " -- ";
        }

        return sR;
    }

    /**
     * Return the characteristic equation as a String
     */
    public String equation() {
        String eq;

        //round the constants to two decimal places
        double dz;
        for (int i = 0; i < k + 1; i++) {
            dz = Math.round(mC.getElem(i) * 100.0) / 100.0;
            mC.setElem(i, dz);
        }

        eq = "Y = " + mC.getElem(0) + " + " + mC.getElem(1) + " X";
        for (int j = 2; j < k + 1; j++) {
            eq += " + " + mC.getElem(j) + " X^" + j;
        }

        return eq;
    }
}
