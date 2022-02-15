/**
 * IRealSmoothFunction.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 24, 2015
 */
package xal.tools.math.fnc;

/**
 * Interface exposing the characteristics of a real function of a real variable
 * which has derivatives.
 *
 *
 * @author Christopher K. Allen
 * @since Sep 24, 2015
 */
public interface ISmoothRealFunction extends IRealFunction {

    /**
     * <p>
     * Return the first-order derivative (<em>n</em> = 1) of the function. Thus,
     * this method is the equivalent of calling
     * <code>derivativeAt(1, dblLoc)</code>.
     * </p>
     * <p>
     * A smooth, real-valued function has at least one derivative. Thus, the
     * derivative
     * <em>f</em>'(<em>x</em>) should always exist for any class implementing
     * this interface.
     * </p>
     *
     * @param dblLoc the location <em>x</em> at which to evaluate the derivative
     *
     * @return the derivative <em>f</em>'(<em>x</em>) of the function <em>f</em>
     *
     * @since Sep 25, 2015 by Christopher K. Allen
     */
    public double derivativeAt(double dblLoc);

    /**
     * <p>
     * Compute and return the <em>n<sup>th</sup></em> derivative at the given
     * location <em>x</em>
     * within the function domain. The order argument <em>n</em>
     * must be 0 or greater where the 0<sup><em>th</em></sup>
     * derivative is simply the value of the function itself.
     * </p>
     * <p>
     * It is possible that the derivatives of a function are all zero for
     * <em>n</em> greater than a certain value. Consider a polynomial for
     * example, when <em>n</em> is greater than the degree of that polynomial.
     * </p>
     *
     * @param nOrder the order <em>n</em> of the derivative
     * @param dblLoc the location <em>x</em> at which to evaluate the derivative
     *
     * @return the derivative <em>f</em><sup>(<em>n</em>)</sup>(<em>x</em>) of
     * the function
     *
     * @throws IllegalArgumentException the derivative order must be positive.
     *
     * @since Sep 24, 2015 by Christopher K. Allen
     */
    public double derivativeAt(int nOrder, double dblLoc) throws IllegalArgumentException;

}
