/*
 * ElementaryFunctions.java
 *
 * Created on October 22, 2002, 10:10 AM
 */
package xal.tools.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <p>
 * Utility case for defining elementary mathematical functions that are not, but
 * should be, included in the <code>java.lang.Math</code> class. Several of the
 * functions in this class are implemented using the methods of
 * <code>java.lang.Math</code>.
 * </p>
 *
 * @author Christopher K. Allen
 *
 * @see java.lang.Math
 * @see java.lang.StrictMath
 */
public final class ElementaryFunction {

    /*
     *  Global Attributes
     */
    /**
     * number of Units in the Last Place (ULPs) used for bracketing
     * approximately equal values
     */
    public static final int ULPS_DEFLT_BRACKET = 100;

    /**
     * conversion between significant digits in decimal to significant digits in
     * binary log(10)/log(2)
     */
    public static final double DBL_DEC_TO_BINARY = 3.32192809488;

    /**
     * the value PI/2
     */
    public static final double PI_BY_2 = Math.PI / 2;

    /**
     * small tolerance value
     */
    public static final double EPS = 1000.0 * Double.MIN_VALUE;

    /*
     * Elementary Math
     */
    /**
     * Test if two <code>double</code> precision numbers are approximately
     * equal. This condition is checked using the the default number of Units in
     * the Last Place (ULPs) bracketing the two numbers. The default number of
     * ULPs is given in the class constant
     * <code>{@link #ULPS_DEFLT_BRACKET}</code> and current has the value
     * <code>{@value #ULPS_DEFLT_BRACKET}</code>.
     *
     * @param x double precision number
     * @param y double precision number
     *
     * @return true of <em>y</em> ~ <em>x</em>, false otherwise
     *
     * @see #approxEq(double, double, int)
     * @see #ULPS_DEFLT_BRACKET
     */
    public static boolean approxEq(double x, double y) {
        return ElementaryFunction.approxEq(x, y, ElementaryFunction.ULPS_DEFLT_BRACKET);
    }

    /**
     * <p>
     * Test if two <code>double</code> precision numbers are approximately
     * equal. This condition is defined with respect to the
     * <strong>U</strong>nits in
     * <strong>L</strong>ast
     * <strong>P</strong>lace (ULPs) bracketing procedure.
     * </p>
     * <p>
     * The ULP values <em>ulp<sub>x</sub></em> and <em>ulp<sub>y</sub></em> are
     * computed for each argument <em>x</em> and <em>y</em>. These values are
     * the distances between the arguments and the nearest double precision
     * number that can be represented by the IEEE 754 standard. The bracketing
     * distances &delta;<em>x</em> and &delta;<em>y</em> for <em>x</em> and
     * <em>y</em>
     * are computed as
     * <pre>
     *      &delta;<em>x</em> &trie; <em>N</em> &times; <em>ulp<sub>x</sub></em> ,
     *      &delta;<em>y</em> &trie; <em>N</em> &times; <em>ulp<sub>y</sub></em> ,
     * </pre> where <em>N</em> is the number of ULPs specified in the arguments.
     * Two intervals are defined
     * <pre>
     *      <em>I<sub>x</sub></em> &trie; [<em>x</em> &minus; &delta;<em>x</em>,<em>x</em> &plus; &delta;<em>x</em>],
     *      <em>I<sub>y</sub></em> &trie; [<em>y</em> &minus; &delta;<em>y</em>,<em>y</em> &plus; &delta;<em>y</em>].
     * </pre> If the intersection <em>I<sub>x</sub></em> &cap;
     * <em>I<sub>y</sub></em> is finite then
     * <em>x</em> and <em>y</em> are considered approximately equal.
     * </p>
     *
     * @param x double precision number
     * @param y double precision number
     * @param cntUlps number <em>N</em> of ULPs used to bracket the numbers
     *
     * @return  <code>true</code> of <em>y</em> ~ <em>x</em> within <em>N</em>
     * ULPs,
     * </code>false</code> otherwise
     */
    public static boolean approxEq(double x, double y, int cntUlps) {

        if (x == y) {
            return true;
        }

        double dx = cntUlps * Math.ulp(x);
        double dy = cntUlps * Math.ulp(y);

        if (x < y) {
            if (x + dx >= y - dy) {
                return true;
            } else {
                return false;
            }

        } else {
            if (y + dy >= x - dx) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Checks if two double precision numbers are equal up to the given number
     * of significant digits.
     *
     * @param x double precision number
     * @param y double precision number
     * @param cntDgts number <em>N</em> of significant digits to compare
     *
     * @return          <code>true</code> if the first <em>N</em> digits of <em>x</em>
     * and
     * <em>y</em> agree, <code>false</code> otherwise
     *
     * @since Dec 31, 2015, Christopher K. Allen
     */
    public static boolean significantDigitsEqs(double x, double y, int cntDgts) {

        BigDecimal bdRndX = new BigDecimal(x);
        BigDecimal bdRndY = new BigDecimal(y);

        bdRndX = bdRndX.setScale(cntDgts, RoundingMode.HALF_UP);
        bdRndY = bdRndY.setScale(cntDgts, RoundingMode.HALF_UP);

        boolean bolEq = bdRndX.equals(bdRndY);

        return bolEq;
    }

    /**
     * <p>
     * Test if two <code>double</code> precision numbers are in the same ball of
     * radius <em>r</em>.
     * </p>
     * <p>
     * <h4>NOTES CKA</h4>
     * &middot; This is really a distance function and not a topological one.
     * </p>
     *
     *
     * @param x double precision number
     * @param y double precision number
     * @param r radius defining the size of the neighborhood
     *
     * @return true of |<em>y</em> - <em>x</em>| <= <em>r</em>, false otherwise
     */
    public static boolean neighbors(double x, double y, double r) {
        double difference = x - y;

        return Math.abs(difference) <= r;
    }

    /*
     * Trigonometric Functions
     */
    /**
     * Inverse tangent function.
     *
     * This version of the arctan function is similar to the
     * <code>Math.atan2()</code> function, however it returns values in the
     * interval [-pi/2,+pi/2]. It takes the same arguments as
     * <code>Math.atan2()</code>.
     *
     * @param y argument numerator of atan(y/x)
     * @param x argument denominator
     */
    /*
     * Algebraic Functions
     */
    /**
     * Computes the factorial of the given integer. The factorial
     * <em>n</em>! of the number <em>n</em> is defined
     * <br>
     * <br>
     * &nbsp; &nbsp;  <em>n</em>! &equiv; 1 &middot; 2 &middot; &hellip; &middot;
     * (<em>n</em> - 1) &middot; <em>n</em>
     *
     * @param n integer to be "factorialized"
     *
     * @return      <em>n</em>! = factorial of argument
     *
     * @author Christopher K. Allen
     * @since Dec 9, 2011
     */
    public static final int factorial(int n) {
        if (n < 0) {
            return 0;
        }

        int intFac = 1;

        for (int i = n; i > 0; i--) {
            intFac *= i;
        }

        return intFac;
    }

    /**
     * <p>
     * Returns the value of the first argument raised to the power of the second
     * argument
     * <em>dblBase</em><sup><em>dblExpon</em></sup>. Special cases:
     * <br>
     * <br>&middot; If the second argument is positive or negative zero, then
     * the result is 1.0.
     * <br>&middot; If the second argument is 1.0, then the result is the same
     * as the first argument.
     * <br>&middot; If the second argument is NaN, then the result is NaN.
     * <br>&middot; If the first argument is NaN and the second argument is
     * nonzero, then the result is NaN.
     * <p>
     * <p>
     * This method should be used over that of
     * <code>{@link Math#pow(double, double)}</code> whenever the exponent is an
     * integer. Since the later must consider the case of non-integer exponents
     * the algorithm used there is more expensive than the simple multiplication
     * used here.
     * </p>
     *
     * @param dblBase the base of the exponential
     * @param intExpon the exponent
     *
     * @return the value <var>dblBase<sup>dblExpon</var></sup>
     *
     * @author Christopher K. Allen
     * @since Dec 9, 2011
     */
    public static final double pow(double dblBase, int intExpon) {
        double dblFac = 1.0;

        if (intExpon > 0) {
            for (int i = 0; i < intExpon; i++) {
                dblFac *= dblBase;
            }
        } else {
            for (int i = 0; i < Math.abs(intExpon); i++) {
                dblFac /= dblBase;
            }
        }

        return dblFac;
    }

    /**
     * <p>
     * Returns the value of the first argument raised to the power of the second
     * argument
     * <em>intBase</em><sup><em>intExpon</em></sup> where the base is an
     * integer. Special cases:
     * <br>
     * <br>&middot; If the second argument is positive or negative zero, then
     * the result is 1.
     * <br>&middot; If the second argument is 1, then the result is the same as
     * the first argument.
     * <br>&middot; If the second argument is NaN, then the result is NaN.
     * <br>&middot; If the first argument is NaN and the second argument is
     * nonzero, then the result is NaN.
     * <p>
     * <p>
     * This method should be used over that of
     * <code>{@link Math#pow(double, double)}</code> whenever both the base and
     * the exponent are integers. Since the later must consider the case of
     * non-integer exponents the algorithm used here is less expensive.
     * </p>
     *
     * @param intBase the base of the exponential
     * @param intExpon the exponent
     *
     * @return the value <var>intBase<sup>intExpon</var></sup>
     *
     * @author Christopher K. Allen
     * @since Dec 9, 2011
     */
    public static final long pow(int intBase, int intExpon) {
        long lngFac = 1;

        if (intExpon > 0) {
            for (int i = 0; i < intExpon; i++) {
                lngFac *= intBase;
            }
        } else {
            for (int i = 0; i < Math.abs(intExpon); i++) {
                lngFac /= intBase;
            }
        }

        return lngFac;
    }

    /*
     * Engineering Functions
     */
    /**
     * <p>
     * Implementation of the sinc function where
     * <br>
     * <br>
     * &nbsp; sinc(<em>x</em>) &equiv; sin(<em>x</em>)/<em>x</em>.
     * </p>
     * <p>
     * For small values of <em>x</em> we Taylor expand the sinc function to
     * sixth order,
     * <br>
     * <br>
     * &nbsp; sinc(x) &asymp; 1 - <em>x</em><sup>2</sup>/6 +
     * <em>x</em><sup>4</sup>/120 -
     * <em>x</em><sup>6</sup>/5040 +
     * <em>O</em>(<em>x</em><sup>8</sup>).
     * <br>
     * <br>
     * otherwise we return sin(<em>x</em>)/<em>x</em>.
     * </p>
     *
     * @param x any real number
     *
     * @return sinc(<var>x</var>) &equiv; sin(<var>x</var>)/<var>x</var>
     */
    public static double sinc(double x) {

        // avoid singularity at zero
        if (Math.abs(x) < 0.1) {
            double x2 = x * x;
            double x4 = x2 * x2;

            return 1.0 - x2 / 6.0 + x4 / 120.0 - x2 * x4 / 5040.0;
        }

        return Math.sin(x) / x;
    }

    /**
     * <p>
     * Implementation of the sinch function where
     * <br>
     * <br>
     * &nbsp; sinch(<em>x</em>) &equiv; sinh(<em>x</em>)/<em>x</em>
     * <br>
     * <br>
     * </p>
     * <p>
     * For small values of <em>x</em> we Taylor expand the hyperbolic sine
     * function to sixth order,
     * <br>
     * <br>
     *
     * &nbsp; sinch(<em>x</em>) &asymp; 1 + <em>x</em><sup>2</sup>/6 +
     * <em>x</em><sup>4</sup>/120 +
     * <em>x</em><sup>6</sup>/5040 +
     * <em>O</em>(<em>x</em><sup>8</sup>).
     * <br>
     * <br>
     * Otherwise we return sinh(<em>x</em>)/<em>x</em>.
     *
     * @param x any real number
     *
     * @return sinh(<em>x</em>)/<em>x</em>.
     */
    public static double sinch(double x) {

        if (Math.abs(x) > ElementaryFunction.EPS) {
            return sinh(x) / x;
        } else {
            // LOGGER.log(Level.INFO, "sinch, x = "+x);
        }

        double x2 = x * x;

        return 1.0 + x2 / 6.0 + x2 * x2 / 120.0 + x2 * x2 * x2 / 5040.0;
    }

    /**
     * Returns the sinch(<em>x</em><sup>2</sup>). I am not sure why this needs a
     * special implementation, but it's here. There is not special computation,
     * the result is computed directly as sinch(<em>x</em><sup>2</sup>).
     *
     * @param x any real number
     *
     * @return sinch(<em>x</em><sup>2</sup>)
     *
     * @see ElementaryFunction#sinch(double)
     */
    public static double sinchm(double x) {

        if (Math.abs(x) > ElementaryFunction.EPS) {
            return Math.sinh(x) / x;
        } else {
            //LOGGER.log(Level.INFO, "sinchm, x = "+x);
        }

        double x2 = x * x;

        return 1.0 + x2 / 6.0 + x2 * x2 / 120.0 + x2 * x2 * x2 / 5040.0;
    }


    /*
     * Hyperbolic Functions
     */
    /**
     * Hyperbolic sine function. This should really be included in Java.
     *
     * @param x any real number
     *
     * @return &frac12;(<em>e</em><sup>+<em>x</em></sup> -
     * <em>e</em><sup>-<em>x</em></sup>)
     */
    public static final double sinh(double x) {
        return 0.5 * (Math.exp(x) - Math.exp(-x));
    }

    ;
    
    /**
     * Hyperbolic cosine function.  This too.
     * 
     * @param   x   any real number
     * 
     * @return &frac12;(<em>e</em><sup>+<em>x</em></sup> + <em>e</em><sup>-<em>x</em></sup>) 
     */
    public static final double cosh(double x) {
        return 0.5 * (Math.exp(x) + Math.exp(-x));
    }

    ;
    
    /**
     * Hyperbolic tangent function.
     *  
     * @author Christopher Allen
     * 
     * @return sinh(<em>x</em>)/cosh(<em>x</em>)
     */
    public static final double tanh(double x) {
        return sinh(x) / cosh(x);
    }


    /*
     * Inverse Hyperbolic Functions
     */
    /**
     * Inverse hyperbolic sine function.
     *
     * @param x any real number
     *
     * @author Christopher K. Allen
     *
     * @return log[<em>x</em> + (<em>x</em><sup>2</sup> + 1)<sup>1/2</sup>]
     */
    public static final double asinh(double x) {
        return Math.log(x + Math.sqrt(x * x + 1.0));
    }

    /**
     * Inverse hyperbolic cosine function. Note that due to the nature of the
     * hyperbolic cosine function the argument
     * <strong>must</strong> be greater than 1.0.
     *
     * @param x a real number in the interval [1,+&infin;)
     *
     * @return log[<em>x</em> + (<em>x</em><sup>2</sup> - 1)<sup>1/2</sup>]
     *
     * @author Christopher K. Allen
     *
     * @exception IllegalArgumentException argument value is outside the domain
     * of definition
     */
    public static final double acosh(double x)
            throws IllegalArgumentException {
        if (x < 1.0) {
            throw new IllegalArgumentException("argument x=" + x + " outside interval [1,+inf)");
        }

        return Math.log(x + Math.sqrt(x * x - 1.0));
    }

    /**
     * Inverse hyperbolic tangent function. Note that do to the nature of the
     * inverse hyperbolic tangent function overflow may occur for arguments
     * close to the values -1 and 1.
     *
     * @param x a real number in the open interval (-1,1)
     *
     * @return &frac12;log[(<em>x</em> + 1)/(<em>x</em> - 1)]
     *
     * @exception IllegalArgumentException argument value is outside the domain
     * of definition
     *
     * @author Christopher K. Allen
     */
    public static final double atanh(double x)
            throws IllegalArgumentException {
        if (x >= 1.0 || x <= -1.0) {
            throw new IllegalArgumentException("argument x=" + x + " outside interval (-1,+1)");
        }

        return 0.5 * Math.log((1.0 + x) / (1.0 - x));
    }
}
