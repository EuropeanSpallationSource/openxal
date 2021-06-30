/**
 * PolynomialFitter.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 24, 2015
 */
package xal.tools.math.fnc.poly;

import xal.tools.math.Interval;
import xal.tools.math.fnc.RealFunctionSamples;

/**
 * Class representing an uni-variate real polynomial used for the expressed
 * purpose of fitting a function over an interval. Thus, this class is a
 * polynomial because it is a polynomial fit to a given function. It is then
 * possible to specify the domain over which the fit is accurate. If this domain
 * is specified than any attempt to evaluate the polynomial outside this range
 * results in an exception.
 *
 *
 * @author Christopher K. Allen
 * @since Sep 24, 2015
 */
public class PolynomialFitter {

    /*
     * Local Attributes
     */
    /**
     * The domain of the fitting
     */
    private Interval ivlDomain;

    /*
     * Initialization
     */
    /**
     * Initializing constructor for PolynomialFitter. The given data is used to
     * create a least-squares fit up to the given polynomial order.
     *
     * @param nDegree degree of the polynomial used to fit the data
     * @param fncSmps data of function samples
     *
     * @since Sep 25, 2015 by Christopher K. Allen
     */
    public PolynomialFitter(int nDegree, RealFunctionSamples fncSmps) {
    }
}
