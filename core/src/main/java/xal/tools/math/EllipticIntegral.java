package xal.tools.math;

import java.util.logging.Logger;
import java.util.logging.Level;

/*
 *
 * $Id: EllipticIntegral.java,v 1.1 2006/08/17 05:08:10 cvs Exp $
 *
 * Numerical Integrals
 *
 *
 */
/**
 * Utility class for numerical computation of elliptic integrals.
 *
 * @author Christopher K. Allen
 * @since Aug 17, 2006
 */
public class EllipticIntegral {

    private static final Logger LOGGER = Logger.getLogger(EllipticIntegral.class.getName());

    /**
     * Compute and return the Carlson Elliptic integral RD(x,y,z).
     *
     * This is the "degenerate form" of Carlson's Elliptic integral of the third
     * kind, a special case of his elliptic integral RJ(x,y,z,p) where p == z.
     *
     * The definition of this function is
     *
     * RD(x,y,z) := (3/2)Integral_dt{ 1.0/( (t+x)^1/2*(t+y)^1/2*(t+z)^3/2 ) }
     *
     * where the integral is from zero to infinity.
     *
     * @param x real number > 0
     * @param y real number > 0
     * @param z real number > 0
     *
     * @return the value of RD(x,y,z)
     */
    public static double RD(double x, double y, double z) {
        final double errTol = 0.05;
        final double tiny = 1.0e-25;
        final double big = 4.5e21;
        final double c1 = (3.0 / 14.0);
        final double c2 = (1.0 / 6.0);
        final double c3 = (9.0 / 22.0);
        final double c4 = (3.0 / 26.0);
        final double c5 = (0.25 * c3);
        final double c6 = (1.5 * c4);

        double alamb, ave, delx, dely, delz, ea, eb, ec, ed, ee, fac, sqrtx, sqrty, sqrtz, sum, xt, yt, zt;

        if (Math.min(x, y) < 0.0 || Math.min(x + y, z) < tiny || Math.max(Math.max(x, y), z) > big) {
            return 0.0;
        }

        xt = x;
        yt = y;
        zt = z;
        sum = 0.0;
        fac = 1.0;
        do {
            sqrtx = Math.sqrt(xt);
            sqrty = Math.sqrt(yt);
            sqrtz = Math.sqrt(zt);
            alamb = sqrtx * (sqrty + sqrtz) + sqrty * sqrtz;
            sum += fac / (sqrtz * (zt + alamb));
            fac = 0.25 * fac;
            xt = 0.25 * (xt + alamb);
            yt = 0.25 * (yt + alamb);
            zt = 0.25 * (zt + alamb);
            ave = 0.2 * (xt + yt + 3.0 * zt);
            delx = (ave - xt) / ave;
            dely = (ave - yt) / ave;
            delz = (ave - zt) / ave;
        } while (Math.max(Math.max(Math.abs(delx), Math.abs(dely)), Math.abs(delz)) > errTol);

        ea = delx * dely;
        eb = delz * delz;
        ec = ea - eb;
        ed = ea - 6.0 * eb;
        ee = ed + ec + ec;
        return 3.0 * sum + fac * (1.0 + ed * (-c1 + c5 * ed - c6 * delz * ee)
                + delz * (c2 * ee + delz * (-c3 * ec + delz * c4 * ea))) / (ave * Math.sqrt(ave));
    }

    /**
     * Axis-symmetric Carlson elliptic integral RD(r,r,z)
     *
     * Compute and return the Carlson elliptic integral for the "axis-symmetric"
     * situation where x = y = r. In this case there is an analytic formula for
     * the integral involving inverse cosines and inverse hyperbolic cosines.
     * The value of this function is actually computed by a call to the
     * <code>EllipticIntegral.formFactorD()</code> function.
     *
     * The definition for symmetricRDz(r,z) is given by the following
     *
     * RDz(r,z) := (3/2)Integral_dt{ 1.0/( (t+r)*(t+z)^3/2 ) }
     *
     * = (3/ (r*z^1/2))*formFactorD( (z/r)^1/2 )
     *
     * where the integral is from zero to infinity.
     *
     * NOTE: Depending upon the algorithm used to compute the
     * <code>Math.acos()</code> and <code>ElementaryFunction.acosh()</code>
     * functions it may actually be faster to use the function
     * <code>EllipticIntegral.RD(r,r,z)</code>.
     *
     * @param r real number > 0 (i.e., radius)
     * @param z real number > 0 (i.e., length)
     *
     * @return value of RD(r,r,z)
     *
     * @throws IllegalArgumentException argument less than zero
     *
     * @see EllipticIntegral#RD(double, double, double)
     */
    public static double symmetricRDz(double r, double z)
            throws IllegalArgumentException {
        if (r < 0.0 || z < 0.0) {
            throw new IllegalArgumentException("EllipticIntegral#RDz(r,z) - argument less than zero");
        }

        double s = Math.sqrt(z / r);
        double d = r * Math.sqrt(z);

        return (3 / d) * EllipticIntegral.formFactorD(s);
    }

    /**
     * Axis-symmetric Carlson elliptic integral RD(z,r,r) (permuted arguments)
     *
     * Compute and return the Carlson elliptic integral for the "axis-symmetric"
     * situation where x = y = r. In this case there is an analytic formula for
     * the integral involving inverse cosines and inverse hyperbolic cosines.
     * The value of this function is actually computed by a call to the
     * <code>EllipticIntegral.formFactorD()</code> function.
     *
     * The definition for symmetricRDr(z,r) is given by the following
     *
     * RDr(r,z) := (3/2)Integral_dt{ 1.0/( (t+z)^1/2*(t+r)^2 ) }
     *
     * = (1/2)*(3/(r*z^1/2) - *RD(r,r,z)
     *
     * where the integral is from zero to infinity.
     *
     * NOTE: Depending upon the algorithm used to compute the
     * <code>Math.acos()</code> and <code>ElementaryFunction.acosh()</code>
     * functions it may actually be faster to use the function
     * <code>EllipticIntegral.RD(r,r,z)</code>.
     *
     * @param r real number > 0 (i.e., radius)
     * @param z real number > 0 (i.e., length)
     *
     * @return value of RD(z,r,r)
     *
     * @throws IllegalArgumentException argument less than zero
     *
     * @see EllipticIntegral#RD(double, double, double)
     * @see EllipticIntegral#symmetricRDz(double, double)
     */
    public static double symmetricRDr(double r, double z)
            throws IllegalArgumentException {
        if (r < 0.0 || z < 0.0) {
            throw new IllegalArgumentException("EllipticIntegral#RDr(r,z) - argument less than zero");
        }

        double d = r * Math.sqrt(z);

        return 0.5 * ((3.0 / d) - EllipticIntegral.symmetricRDz(r, z));
    }

    /**
     * Evaluates the value of the "form factor" which is an analytic means for
     * approximating the Carlson elliptic integral RD(x,y,z). I believe this
     * notion was introduced by Lapostalle in the early 1970s.
     *
     * I have the definition of the form factor f(s) as
     *
     * f(s) := (s/2)Integral_dt{1/( (t+1)*(t+s^2)^3/2 ) }
     *
     * where the integral is from zero to infinity.
     *
     * @param s a real number in the interval (0,inf)
     *
     * @return value of the form factor at s
     *
     * @author Christopher K. Allen
     */
    public static double formFactorD(double s) {
        // radius for Taylor expansion around s=1
        final double epsilon = 1.0e-6;
        // left endpoint for Taylor epansion evaluation
        final double s1 = 1.0 - epsilon;
        // right endpoint for Taylor epansion evaluation
        final double s2 = 1.0 + epsilon;

        // value of function at s=1
        final double valAtOne = 1.0 / 3.0;
        // first derivative of function at s=1
        final double D1AtOne = -4.0 / 15.0;
        // second derivative of function at s=1
        final double D2AtOne = 12.0 / 35.0;

        if (s < s1) {
            double den = 1.0 - s * s;
            double rad = Math.sqrt(den);
            double acos = Math.acos(s);

            return (1.0 - s * acos / rad) / den;

        } else if (s < s2) {
            double ds = s - 1.0;

            return valAtOne + D1AtOne * ds + 0.5 * D2AtOne * ds * ds;

        } else {
            double den = 1.0 - s * s;
            double rad = Math.sqrt(-den);
            double acsh = ElementaryFunction.acosh(s);

            return (1.0 - s * acsh / rad) / den;
        }
    }

    /*
     * Testing and Debugging
     */
    /**
     * Testing Driver
     */
    public static void main(String[] args) {

        double x = 5.55984e-7;
        double y = 4.35084e-7;
        double z = 3.12004e-6;

        LOGGER.log(Level.INFO, "{0}", RD(y / x, z / x, 1));
        LOGGER.log(Level.INFO, "{0}", RD(y / x, z / y, 1));
        LOGGER.log(Level.INFO, "{0}", RD(x / z, y / z, 1));

        LOGGER.log(Level.INFO, "R_d(.5,.5,1.0) = {0}", RD(.5, .5, 1.0));
        LOGGER.log(Level.INFO, "R_d(.5,1.0,1.0) = {0}", RD(.5, 1.0, 1.0));
        LOGGER.log(Level.INFO, "R_d(.5,1.5,1.0) = {0}", RD(.5, 1.5, 1.0));
        LOGGER.log(Level.INFO, "R_d(1.0,1.0,1.0) = {0}", RD(1.0, 1.0, 1.0));
        LOGGER.log(Level.INFO, "R_d(1.0,1.5,1.0) = {0}", RD(1.0, 1.5, 1.0));
        LOGGER.log(Level.INFO, "R_d(1.5,1.5,1.0) = {0}", RD(1.5, 1.5, 1.0));
        LOGGER.log(Level.INFO, "R_d(2.0,2.0,2.0) = {0}", RD(2.0, 2.0, 2.0));

        for (int i = 0; i < 20; i++) {
            double s = 0.1 * i;

            LOGGER.log(Level.INFO, "formFactor({0})={1}", new Object[]{s, formFactorD(s)});
        }
    }
}
