/*
 * Created on Feb 19, 2004
 *
 */
package xal.tools.math.fnc.poly;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.tools.math.ElementaryFunction;
import xal.tools.math.fnc.ISmoothRealFunction;

/**
 * <p>
 * Represents a polynomial object with real coefficients over one real variable.
 * This class is meant more as an encapsulation of a polynomial function rather
 * than an algebraic object, as is implemented in the <code>JSci</code>
 * mathematical/science package.
 * </p>
 * <p>
 * Note that if the zero-argument constructor is used one is essentially left
 * with a <code>null</code> object. There will be no allocated coefficient
 * storage until the <code>{@link #setCoefArray(double[])}</code> method is
 * call. Consequently, any operations called prior to that time will throw a
 * null pointer exception.
 * </p>
 *
 * @author Christopher Allen
 * @since Feb 19, 2004
 * @version Sep 25, 2015
 */
public class RealUnivariatePolynomial implements ISmoothRealFunction {

    private static final Logger LOGGER = Logger.getLogger(RealUnivariatePolynomial.class.getName());

    /*
     *  Local Attributes
     */
    /**
     * the vector of coefficients
     */
    private double[] arrCoef = null;


    /*
      * Initialization
     */
    /**
     * Creates an empty polynomial object, the zero polynomial.
     */
    public RealUnivariatePolynomial() {
    }

    /**
     * Creates and initializes a polynomial to the specified coefficients.
     *
     * @param arrCoef
     * @return
     */
    public RealUnivariatePolynomial(double[] arrCoef) {
        this.setCoefArray(arrCoef);
    }

    /**
     * Set the entire coefficient array. The coefficient array is arranged in
     * order of ascending indeterminate order.
     *
     * @param arrCoef double array of coefficients.
     */
    public void setCoefArray(double[] arrCoef) {
        this.arrCoef = arrCoef;
    }


    /*
     *  Polynomial operations
     */
    /**
     * Return the degree of the polynomial. That is, the highest indeterminant
     * order for all the nonzero coefficients.
     */
    public int getDegree() {
        return this.getCoefs().length - 1;
    }

    /**
     * Get the specified coefficient value. The value of parameter
     * <code>iOrder</code> specifies order of the indeterminate. For example,
     * calling <code>getCoef(2)</code> would return the coefficient for the
     * indeterminate of second order.
     *
     * If the value of <code>iOrder</code> is larger than the size of the
     * coefficient array then the coefficient is assumed to have value zero.
     *
     * @param iOrder order of the indeterminate
     * @return coefficient of the specified indeterminate order
     */
    public double getCoef(int iOrder) {
        if (this.arrCoef.length == 0) {
            return 0.0;
        }
        if (iOrder >= this.arrCoef.length) {
            return 0.0;
        }

        return this.arrCoef[iOrder];
    }

    /**
     * Return the entire array of polynomial coefficients. The coefficient array
     * is arranged in order of ascending indeterminate order.
     *
     * @return the entire coefficient array
     */
    public double[] getCoefs() {
        return this.arrCoef;
    }


    /*
     * ISmoothRealFunction Interface
     */
    /**
     * Evaluate the polynomial for the specified value of the indeterminate. If
     * the coefficient vector has not been specified this method returns zero.
     *
     * @param dblVal indeterminate value to evaluate the polynomial
     *
     * @author Christopher Allen
     */
    @Override
    public double evaluateAt(double dblVal) {
        if (arrCoef == null) {
            return 0.0;
        }

        // number of coefficients
        int len = arrCoef.length;
        // accumulator
        double dblAccum = 0.0;

        for (int n = len - 1; n >= 0; n--) {
            dblAccum += this.getCoef(n) * Math.pow(dblVal, n);
        }

        return dblAccum;
    }

    /**
     * Evaluate the polynomial derivative for the specified value of the
     * indeterminate. If the coefficient vector has not been specified this
     * method returns zero. Note that the result has one less order of accuracy
     * than the underlying polynomial.
     *
     * @param dblVal indeterminate value to evaluate the polynomial
     *
     * @author Christopher Allen
     * @version Nov 26, 2014
     */
    @Override
    public double derivativeAt(double dblVal) {
        if (arrCoef == null) {
            return 0.0;
        }

        // number of coefficients
        int len = arrCoef.length;
        // the the nomial
        double dblPow = 1.0;
        // accumulator
        double dblSum = 0.0;

        for (int n = 1; n < len; n++) {
            double dblCoef = n * this.getCoef(n);

            dblSum += dblCoef * dblPow;

            dblPow *= dblVal;
        }

        return dblSum;
    }

    /**
     *
     * @see xal.tools.math.fnc.ISmoothRealFunction#derivativeAt(int, double)
     *
     * @since Sep 25, 2015 by Christopher K. Allen
     */
    @Override
    public double derivativeAt(int nOrder, double dblLoc) throws IllegalArgumentException {
        // Check if the order of the derivative is greater than the degree of the polynomial
        if (nOrder > this.getDegree()) {
            return 0.0;
        }

        // Compute the derivative by starting at the polynomial coefficient with index equal to the
        //  order of the derivative.  The monomial coefficient is the combinatoric of the coefficient
        //  degree with the derivative order
        // monomial of order k - n
        double xK = 1.0;
        // accumulator for polynomial summation
        double dblSum = 0.0;
        for (int k = 0; k <= this.getDegree(); k++) {
            if (k < nOrder) {
                continue;
            }

            double kFac = ElementaryFunction.factorial(k);
            double kmnFac = ElementaryFunction.factorial(k - nOrder);
            double cK = kFac / kmnFac;

            double aK = this.getCoef(k);
            double tK = cK * aK * xK;

            dblSum += tK;

            xK *= dblLoc;
        }

        return dblSum;
    }

    /*
     * Algebraic Operations
     */
    /**
     * Nondestructively add two polynomials. The current polynomial and the
     * argument are added according to standard definitions (i.e., the
     * coefficient array is added vectorially).
     *
     * @param polyAddend polynomial to be added to this
     *
     * @return a new polynomial object representing the sum
     */
    public RealUnivariatePolynomial plus(RealUnivariatePolynomial polyAddend) {
        int nLen = Math.max(polyAddend.getDegree(), this.getDegree()) + 1;
        double[] newArrCoef = new double[nLen];

        for (int n = 0; n < nLen; n++) {
            newArrCoef[n] = this.getCoef(n) + polyAddend.getCoef(n);
        }

        return new RealUnivariatePolynomial(newArrCoef);
    }

    /**
     * Nondestructive multiply two polynomials. The current polynomial and the
     * argument are multiplied according to standard definitions.
     *
     * @param polyFac polynomial to be multiplied by this
     * @return a new polynomial object representing the product
     */
    public RealUnivariatePolynomial times(RealUnivariatePolynomial polyFac) {
        int nLen = polyFac.getDegree() * this.getDegree() + 1;
        double[] newArrCoef = new double[nLen];
        double dblAccum;

        for (int n = 0; n < nLen; n++) {
            dblAccum = 0;

            for (int i = 0; i <= n; i++) {
                dblAccum += this.getCoef(i) * polyFac.getCoef(n - i);
            }
            newArrCoef[n] = dblAccum;
        }

        return new RealUnivariatePolynomial(newArrCoef);
    }


    /*
     * Object Overrides
     */
    /**
     * Construct and return a textual representation of the contents of this
     * polynomial as a <code>String</code> object.
     *
     * @return a String representation of the polynomial contents
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        int n = this.getDegree();

        StringBuilder bld = new StringBuilder();
        bld.append(Double.toString(this.getCoef(0)));
        for (int i = 1; i <= n; i++) {
            bld.append(" + ").append(this.getCoef(i)).append("x^").append(i);
        }

        return bld.toString();
    }

    /**
     * Testing driver
     */
    public static void main(String[] args) {
        RealUnivariatePolynomial poly1 = new RealUnivariatePolynomial(new double[]{1.0, 2.0, 3.0});
        RealUnivariatePolynomial poly2 = new RealUnivariatePolynomial(new double[]{1.1, 1.2, 1.3});

        LOGGER.log(Level.INFO, "poly1 = {0}", poly1);
        LOGGER.log(Level.INFO, "poly2 = {0}", poly2);
        LOGGER.log(Level.INFO, "poly1 + poly2 = {0}", (poly1.plus(poly2)));
        LOGGER.log(Level.INFO, "poly1 * poly2 = {0}", (poly1.times(poly2)));
        LOGGER.log(Level.INFO, "poly1(1.0) = {0}", poly1.evaluateAt(1.0));
        LOGGER.log(Level.INFO, "poly1(2.0) = {0}", poly1.evaluateAt(2.0));
        LOGGER.log(Level.INFO, "poly2(1.0) = {0}", poly2.evaluateAt(1.0));
    }
}
