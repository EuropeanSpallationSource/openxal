/*
 * Copyright (C) 2017 European Spallation Source ERIC.
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
package xal.extension.jels.tools.math;

import xal.tools.math.fnc.poly.RealUnivariatePolynomial;

/**
 * <p>
 * Represents a model function of the form: ( x1 + x2(x0/x-1) + x3/2(x0/x-1)^2 +
 * ... + xn/(n-1)!(x0/x-1)^(n-1) ) / x1 where x0,x1,...,xn are parameters.</p>
 *
 * <p>
 * This is an encapsulation of a TTF function as is used in TraceWin. The class
 * is extended from UnivariateRealPolynomial just so it can be easily switched
 * with it. </p>
 *
 * @author Ivo List
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 *
 */
public class InverseRealPolynomial extends RealUnivariatePolynomial {

    /*
     *  Local Attributes
     */
    /**
     * the vector of coefficients
     */
    private double[] m_arrCoef = null;


    /*
      * Initialization
     */
    /**
     * Creates an empty polynomial object, the zero polynomial.
     */
    public InverseRealPolynomial() {
    }

    /**
     * Creates and initializes a polynomial to the specified coefficients.
     *
     * @param arrCoef
     */
    public InverseRealPolynomial(double[] arrCoef) {
        this.setCoefArray(arrCoef);
    }

    /**
     * Set the entire coefficient array. The coefficient array is arranged in
     * order of ascending indeterminate order.
     *
     * @param arrCoef double array of coefficients.
     */
    @Override
    public void setCoefArray(double[] arrCoef) {
        this.m_arrCoef = arrCoef.clone();
    }


    /*
     * Attribute Queries
     */
    /**
     * @return the degree of the polynomial. That is, the highest indeterminant
     * order for all the nonzero coefficients.
     */
    @Override
    public int getDegree() {
        return this.getCoefs().length - 1;
    }

    /**
     * Get the specified coefficient value.
     *
     * If the value of <code>iOrder</code> is larger than the size of the
     * coefficient array then the coefficient is assumed to have value zero.
     *
     * @param iOrder index of coefficient
     * @return coefficient
     */
    @Override
    public double getCoef(int iOrder) {
        if (this.m_arrCoef.length == 0) {
            return 0.0;
        }
        if (iOrder >= this.m_arrCoef.length) {
            return 0.0;
        }

        return this.m_arrCoef[iOrder];
    }

    /**
     * Return the entire array of coefficients.
     *
     * @return the entire coefficient array
     */
    @Override
    public double[] getCoefs() {
        return this.m_arrCoef;
    }

    /*
     *  Polynomial operations
     */
    /**
     * Evaluate the model function for the specified value: (x1 + x2(x0/x-1) +
     * x3/2(x0/x-1)^2 + ... + xn/(n-1)!(x0/x-1)^(n-1)) / x1 where x0,x1,...,xn
     * are coefficients.
     *
     * @param dblVal indeterminate value to evaluate the model function at
     * @return 
     */
    @Override
    public double evaluateAt(double dblVal) {
        if (this.m_arrCoef == null || this.m_arrCoef.length == 0) {
            return 1.0;
        }

        double x0 = m_arrCoef[0];
        if (x0 == 0.0) {
            return 1.0;
        }

        int length = this.m_arrCoef.length;      // number of coefficients
        double dblAccum = 0.0;                 // accumulator

        for (int n = length - 1; n >= 1; n--) {
            double f = 1.;
            for (int j = 2; j < n; j++) {
                f *= j;
            }
            dblAccum += this.getCoef(n) * Math.pow(x0 / dblVal - 1, n - 1) / f;
        }

        return dblAccum / m_arrCoef[1];
    }

    /**
     * Evaluate derivative of the model function for the specified value of the
     * indeterminate. If the coefficient vector has not been specified, it
     * return 0.
     *
     * (-x2(x0 x^-2) - x3(x0/x-1)(x0 x^-2) - ... - xn/(n-2)!(x0/x-1)^(n-2)(x0
     * x^-2)) / x1 where x0,x1,...,xn are coefficients.
     *
     * @param dblVal indeterminate value to evaluate the model function
     * derivative
     */
    @Override
    public double derivativeAt(double dblVal) {
        if (this.m_arrCoef == null || this.m_arrCoef.length == 0) {
            return 0.0;
        }

        double x0 = m_arrCoef[0];
        // number of coefficients
        int length = this.m_arrCoef.length;
        // accumulator
        double dblAccum = 0.0;

        for (int n = length - 1; n >= 2; n--) {
            double f = 1.;
            for (int j = 2; j < n - 1; j++) {
                f *= j;
            }
            dblAccum += this.getCoef(n) * Math.pow(x0 / dblVal - 1, n - 2) * x0 / dblVal / dblVal / f;
        }

        return -dblAccum / m_arrCoef[1];
    }

    public InverseRealPolynomial plus(InverseRealPolynomial polyAddend) {
        throw new UnsupportedOperationException();
    }

    public InverseRealPolynomial times(InverseRealPolynomial polyFac) {
        throw new UnsupportedOperationException();
    }


    /*
     * Testing and Debugging
     */
    /**
     * Construct and return a textual representation of the contents of this
     * model function as a <code>String</code> object.
     *
     * @return a String representation of the model function contents
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        int length = this.getDegree();

        String x0 = Double.toString(this.getCoef(0));
        String strPoly = "(" + Double.toString(this.getCoef(1));

        for (int n = 2; n <= length; n++) {
            strPoly += " + " + this.getCoef(n) + "(" + x0 + "/x)^" + (n - 1);
        }

        return strPoly + ")/x1";
    }

}
