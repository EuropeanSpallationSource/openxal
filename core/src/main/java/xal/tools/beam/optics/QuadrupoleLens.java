/*
 * Created on Mar 11, 2004
 *
 */
package xal.tools.beam.optics;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.tools.math.ElementaryFunction;

/**
 * This is a utility class for computing properties of quadrupole lenses.
 *
 * @author Christopher K. Allen
 */
public class QuadrupoleLens {

    private static final Logger LOGGER = Logger.getLogger(QuadrupoleLens.class.getName());

    /**
     * Compute the characteristic transfer matrix for the focusing plane of an
     * ideal quadrupole lens with string <code>k</code> and length
     * <code>l</code>.
     *
     * @param k strength of the quadrupole lens (in
     * <strong>radians/meter</strong>)
     * @param l length of the quadrupole lens (in <strong>meters</strong>)
     *
     * @return 2x2 transfer matrix for focusing phase plane
     */
    public static double[][] transferFocPlane(double k, double l) {
        double ang = k * l;

        double[][] arrF = new double[][]{{Math.cos(ang), l * ElementaryFunction.sinc(ang)},
        {-Math.sin(ang) * k, Math.cos(ang)}
        };

        return arrF;
    }

    /**
     * Compute the characteristic transfer matrix for the defocusing plane of an
     * ideal quadrupole lens with string <code>k</code> and length
     * <code>l</code>.
     *
     * @param k strength of the quadrupole lens (in
     * <strong>radians/meter</strong>)
     * @param l length of the quadrupole lens (in <strong>meters</strong>)
     *
     * @return 2x2 transfer matrix for defocusing phase plane
     */
    public static double[][] transferDefPlane(double k, double l) {
        double ang = k * l;

        double[][] arrD = new double[][]{
            {ElementaryFunction.cosh(ang), l * ElementaryFunction.sinch(ang)},
            {ElementaryFunction.sinh(ang) * k, ElementaryFunction.cosh(ang)}
        };

        return arrD;
    }

    public static double[][] transferFocPlaneExact(double k, double l) {
        double ang = k * l;

        double[][] arrF;

        if (ang != 0) {
            arrF = new double[][]{{Math.cos(ang), l * ElementaryFunction.sinc(ang)},
            {-Math.sin(ang) * k, Math.cos(ang)}
            };
        } else {
            arrF = new double[][]{
                {1, l}, {0, 1}
            };
        }

        return arrF;
    }

    public static double[][] transferDefPlaneExact(double k, double l) {
        double ang = k * l;

        double[][] arrD;

        if (ang != 0) {
            arrD = new double[][]{
                {Math.cosh(ang), l * ElementaryFunction.sinchm(ang)},
                {Math.sinh(ang) * k, Math.cosh(ang)}
            };
        } else {
            arrD = new double[][]{
                {1, l}, {0, 1}
            };
        }

        return arrD;
    }

    /**
     * approximation valid only for thin lens (adaptive tracking should work
     * well Sako, 20 Sep 2006
     *
     * @param k strength of the quadrupole lens (in
     * <strong>radians/meter</strong>)
     * @param l length of the quadrupole lens (in <strong>meters</strong>)
     *
     * @return 2x2 transfer matrix for defocusing phase plane
     */
    public static double[][] transferFocPlaneApprox(double k, double l) {
        double[][] arrF = new double[][]{
            //   			{1, l},
            //    			{-k*k*l, 1}
            {1, l},
            {-k * k * l, -k * k * l * l + 1}
        };
        return arrF;
    }

    /**
     * approximation valid only for thin lens (adaptive tracking should work
     * well Sako, 20 Sep 2006
     *
     * @param k strength of the quadrupole lens (in
     * <strong>radians/meter</strong>)
     * @param l length of the quadrupole lens (in <strong>meters</strong>)
     *
     * @return 2x2 transfer matrix for defocusing phase plane
     */
    public static double[][] transferFocPlaneApproxSandWitch(double k, double l) {
        double[][] arrF = new double[][]{
            {1 - k * k * l * l / 2, l - k * k * l * l * l / 4},
            {-k * k * l, 1 - k * k * l * l / 2}
        };
        return arrF;
    }

    /**
     *
     * approximation valid only for thin lens (adaptive tracking should work
     * well Sako, 20 Sep 2006
     *
     * @param k strength of the quadrupole lens (in
     * <strong>radians/meter</strong>)
     * @param l length of the quadrupole lens (in <strong>meters</strong>)
     *
     * @return 2x2 transfer matrix for defocusing phase plane
     */
    public static double[][] transferDefPlaneApprox(double k, double l) {
        double[][] arrD = new double[][]{
            //    			{1, l},
            //    			{k*k*l, 1}
            {1, l},
            {k * k * l, k * k * l * l + 1}
        };
        return arrD;
    }

    /**
     *
     * approximation valid only for thin lens (adaptive tracking should work
     * well Sako, 20 Sep 2006
     *
     * @param k strength of the quadrupole lens (in
     * <strong>radians/meter</strong>)
     * @param l length of the quadrupole lens (in <strong>meters</strong>)
     *
     * @return 2x2 transfer matrix for defocusing phase plane
     */
    public static double[][] transferDefPlaneApproxSandWitch(double k, double l) {
        double[][] arrD = new double[][]{
            {1 + k * k * l * l / 2, l + k * k * l * l * l / 4},
            {k * k * l, 1 + k * k * l * l / 2}
        };
        return arrD;
    }

    /*
     * Testing and Debugging
     */
    /**
     * Driver for testing.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        double k = Math.PI / 2;
        double l = 0.5;

        double[][] arrF = transferFocPlane(k, l);
        double[][] arrD = transferDefPlane(k, l);

        LOGGER.log(Level.INFO, "F(0,0)={0}", arrF[0][0]);
        LOGGER.log(Level.INFO, "F(0,1)={0}", arrF[0][1]);
        LOGGER.log(Level.INFO, "F(1,0)={0}", arrF[1][0]);
        LOGGER.log(Level.INFO, "F(1,1)={0}", arrF[1][1]);

        LOGGER.log(Level.INFO, "D(0,0)={0}", arrD[0][0]);
        LOGGER.log(Level.INFO, "D(0,1)={0}", arrD[0][1]);
        LOGGER.log(Level.INFO, "D(1,0)={0}", arrD[1][0]);
        LOGGER.log(Level.INFO, "D(1,1)={0}", arrD[1][1]);
    }
}
