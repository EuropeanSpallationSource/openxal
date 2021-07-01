/*
 * Interval.java
 *
 *  Created on January 24, 2003, 9:04 PM
 *  Modified:
 *      1/03:   CKA
 */
package xal.tools.math;

import java.io.PrintWriter;
import java.io.Serializable;

/**
 * <p>
 * Represents an interval on the real line. Intervals are identified by their
 * end points, which may or may not be included depending upon whether the
 * interval is open or closed. Nonetheless, we consider the left end the minimum
 * value and the right end point the maximum value and refer to them as such.
 * </p>
 * <p>
 * Issues concerning boundary points are delegated to the derived classes.
 * </p>
 *
 * @author Christopher K. Allen
 */
public class Interval implements Serializable {

    /*
     * Global Constants
     */
    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    /**
     * The entire real line
     */
    public static final Interval REAL_LINE = new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);


    /*
     * Global Operations
     */
    /**
     * Creates an interval object given the midpoint (centroid) of the interval
     * and the length of the interval (which is the Lebesgue measure). We do not
     * throw an exception assuming that both arguments are positive.
     *
     * @param dblMidpt the center location of the interval
     * @param dblLng the length of the interval
     *
     * @return new interval object with endpoints producing the given center and
     * length
     *
     * @author Christopher K. Allen
     * @since Apr 28, 2011
     */
    public static Interval createFromMidpoint(double dblMidpt, double dblLng) {

        // Compute the interval endpoints
        double dblDel = dblLng / 2.0;
        double dblMin = dblMidpt - dblDel;
        double dblMax = dblMidpt + dblDel;

        // Create the interval object and return it
        Interval I = new Interval();
        I.setMin(dblMin);
        I.setMax(dblMax);

        return I;
    }

    /**
     * Creates a new interval object according to the given endpoints. We do not
     * throw an exception here, which would normally happen if the left-hand
     * endpoint was larger than the right-hand endpoint. Instead we return a
     * <code>null</code> value in this case.
     *
     * @param dblMin the left-hand endpoint
     * @param dblMax the right-hand endpoint
     *
     * @return the interval [<strong>dblMin</strong>,<strong>dblMax</strong>]
     *
     * @author Christopher K. Allen
     * @since Apr 28, 2011
     */
    public static Interval createFromEndpoints(double dblMin, double dblMax) {

        try {
            Interval I = new Interval(dblMin, dblMax);

            return I;

        } catch (IllegalArgumentException e) {
            return null;

        }
    }

    /*
     *  Local Attributes
     */
    /**
     * minimum value
     */
    private double dblMin = 0.0;

    /**
     * maximum value
     */
    private double dblMax = 0.0;


    /*
     *  Interval Initialization
     */
    /**
     * Default constructor - creates a new instance of Interval with one point,
     * the origin.
     */
    public Interval() {
        dblMin = 0.0;
        dblMax = 0.0;
    }

    /**
     * Initializing constructor - create an interval with specified endpoints
     *
     * @param min left endpoint
     * @param max right endpoint
     *
     * @throws IllegalArgumentException <var>max</var> is smaller than
     * <var>min</var>
     */
    public Interval(double min, double max) throws IllegalArgumentException {
        if (max < min) {
            throw new IllegalArgumentException("Interval(): invalid endpoints");
        }

        dblMin = min;
        dblMax = max;
    }

    /**
     * Copy constructor - create a new open interval initialized to the
     * argument.
     *
     * @param I interval to copy
     *
     * @throws MathException malformed interval to copy
     */
    public Interval(Interval I) throws MathException {
        this(I.getMin(), I.getMax());
    }

    /**
     * Set the left end point
     *
     * @param min interval left-hand side
     */
    public void setMin(double min) {
        dblMin = min;
    }

    /**
     * Set the right end point
     *
     * @param max interval right-hand side
     */
    public void setMax(double max) {
        dblMax = max;
    }


    /*
     * Interval Properties
     */
    /**
     * Get minimum value of interval.
     *
     * @return the left end point
     */
    public double getMin() {
        return dblMin;
    }

    /**
     * Get maximum value of interval.
     *
     * @return the right end point
     */
    public double getMax() {
        return dblMax;
    }

    /**
     * Compute the interval length. This is the difference in endpoints.
     *
     * @return length of the interval
     */
    public double measure() {
        return getMax() - getMin();
    }

    /**
     * Compute the interval midpoint. This is the average of the endpoints.
     *
     * @return interval center of mass
     */
    public double midpoint() {
        return (getMax() + getMin()) / 2.0;
    }


    /*
     * Set Theory
     */
    /**
     * Is point a member of this interval <em>I</em> &sub; <em>R</em>. The test
     * is done assuming <em>I</em> is closed, so the endpoints are included.
     *
     * @param x point to be tested as a member of this interval
     *
     * @return true if x &isin; <em>I</em>, false otherwise
     */
    public boolean membership(double x) {
        return x <= getMax() && x >= getMin();
    }

    /**
     * Are intervals equal
     *
     * @param I interval object to be checked for equality
     *
     * @return true if both objects are equal as intervals
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Interval) {
            Interval I = (Interval) obj;
            return (dblMin == I.dblMin) && (dblMax == I.dblMax);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.dblMin) ^ (Double.doubleToLongBits(this.dblMin) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.dblMax) ^ (Double.doubleToLongBits(this.dblMax) >>> 32));
        return hash;
    }

    /**
     * Test if this interval represents the entire real line. Such an interval
     * has the structure
     * [<code>Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY]</code>].
     *
     * @return      <code>true</code> if this interval represents the entire real
     * line, <code>false</code> otherwise
     *
     * @since Sep 25, 2015 by Christopher K. Allen
     */
    public boolean isRealLine() {
        if (this.dblMin == Double.NEGATIVE_INFINITY && this.dblMax == Double.POSITIVE_INFINITY) {
            return true;
        }

        return false;
    }

    /**
     * Inspects for non-empty intersection with the given interval. The interval
     * is assumed closed.
     *
     * @param I interval to inspect.
     *
     * @return          <strong>true</strong> if <em>this</em> &cap; <em>I</em> &ne; {},
     * <strong>false</strong>
     * otherwise
     *
     * @since Jun 2, 2009
     * @author Christopher K. Allen
     */
    public boolean intersects(Interval I) {

        if (this.membership(I.getMin())) {
            return true;
        }

        if (this.membership(I.getMax())) {
            return true;
        }

        if (I.getMin() <= this.getMin() && I.getMax() >= this.getMax()) {
            return true;
        }

        return false;
    }

    /**
     * <h3>Contains Almost Everywhere - &sub; a.e.</h3>
     * <p>
     * Checks whether or not the given interval is a proper subset of this
     * interval, modulo the endpoints. Specifically, <var>I</var>
     * must be contained within this interval modulo the endpoints. We ignore
     * the endpoints and, thus, the condition of whether or not either interval
     * is closed or open.
     * </p>
     *
     * @param I interval to be tested
     *
     * @return     <code>true</code> if <em>I</em> &sub; <code>this</code>,
     * <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since Apr 28, 2011
     */
    public boolean containsAE(Interval I) {

        if (I.getMin() >= this.getMin() && I.getMax() <= this.getMax()) {
            return true;
        }

        return false;
    }

    /*
     * Topology
     */
    /**
     * Is point a boundary element?
     *
     * @param x point to be tested as boundary element
     *
     * @return true if x is a boundary element
     */
    public boolean isBoundary(double x) {
        return x == getMax() || x == getMin();
    }

    /**
     * <p>
     * Compute the local "vertex coordinates" of the argument <em>x</em> with
     * respect to this interval <em>I</em> &sub; <em>R</em>. These "local
     * coordinates" {&lambda;<sub>1</sub>,&lambda;<sub>2</sub>} of a point
     * <em>x</em> &isin; <em>I</em>
     * can computed by solving the following linear equation for the
     * {&lambda;<em><sub>i</sub></em>} :
     * <br/>
     * &nbsp;<table>
     * <td>
     * <table>
     * <tr>
     * <td>&lceil;</td>
     * <td><em>x</em><sub>1</sub></td>
     * <td><em>x</em><sub>2</sub></td>
     * <td>&rceil;</td>
     * <td>&nbsp;</td>
     * </tr>
     * <tr>
     * <td>&lfloor;</td>
     * <td><em>1</em></td>
     * <td><em>1</em></td>
     * <td>&rfloor;</td>
     * </tr>
     * </table>
     * </td>
     *
     * <td>
     * <table>
     * <tr>
     * <td>&lceil;</td>
     * <td><em>&lambda;</em><sub>1</sub></td>
     * <td>&rceil;</td>
     * </tr>
     * <tr>
     * <td>|</td>
     * <td><em>&lambda;</em><sub>2</sub></td>
     * <td>|</td>
     * </tr>
     * </table>
     * </td>
     *
     * <td>
     * <table>
     * <td>=</td>
     * </table>
     * </td>
     *
     * <td>
     * <table/>
     * <tr>
     * <td>&lceil;</td>
     * <td><em>x</em></td>
     * <td>&rceil;</td>
     * </tr>
     * <tr>
     * <td>&lfloor;</td>
     * <td>1</td>
     * <td>&rfloor;</td>
     * </tr>
     * </table>
     * </td>
     * </table>
     * </br>
     * where <strong>p</strong> =
     * <em>x</em> &isin; <em>R</em>.
     * </p>
     * <p>
     * <h4>NOTES:</h4>
     * &middot; After solving the above equation for the
     * {&lambda;<sub>1</sub>,&lambda;<sub>2</sub>} if there is a
     * &lambda;<sub>i</sub> such that &lambda;<sub>i</sub> &notin; [0,1], then
     * <em>x</em>
     * is not in <em>T</em>; that is, <strong>p</strong>&notin; <em>T</em>.
     * </p>
     *
     * @param x point in <em>I</em>
     *
     * @return order 2-array vertex coordinates
     * {&lambda;<sub>1</sub>,&lambda;<sub>2</sub>}
     *
     * @throws MathException argument is not a member of this set
     */
    public double[] vertexCoordinates(double x) throws MathException {

        if (!this.membership(x)) {
            String strMsg = "Interval#vertexCoordinates(): argument not a member of this set";
            throw new MathException(strMsg);
        }

        double dblDet = this.getMax() - this.getMin();
        double dblLambda1 = (this.getMax() - x) / dblDet;
        double dblLambda2 = (x - this.getMin()) / dblDet;

        double[] arrLambdas = new double[]{dblLambda1, dblLambda2};

        return arrLambdas;
    }

    /**
     * Compute and return the smallest interval containing both this interval
     * and the argument interval (i.e., the union of they are intersected).
     *
     * @param I right-hand-side argument
     * @return union of <code>this</code> and <code>I</code>
     *
     * @throws MathException empty intersection
     */
    public Interval convexHull(Interval I) throws MathException {
        double min = Math.min(this.getMin(), I.getMin());
        double max = Math.max(this.getMax(), I.getMax());

        return new Interval(min, max);
    }

    /**
     * Compute and return the largest interval contained in this interval and
     * the argument interval (i.e., the intersection). If the intersection of
     * the two intervals contains no points (i.e., it is the empty set), then a
     * <code>null</code> value is returned. Since the empty is a valid result of
     * intersection, this value should be considered in any robust
     * implementation.
     *
     * @param I interval to be intersected with <code>this</code> interval
     *
     * @return intersection of <code>this</code> and <code>I</code>, or
     * <code>null</code> if the empty set {}
     */
    public Interval intersection(Interval I) {
        double min = Math.max(this.getMin(), I.getMin());
        double max = Math.min(this.getMax(), I.getMax());

        // Check if the intersection is {}
        if (min > max) {
            return null;
        }

        try {
            return new Interval(min, max);

            // This cannot occur - already checked for it.
        } catch (IllegalArgumentException e) {
            return null;

        }
    }

    /**
     * Return the contents of the interval as a <code>String</code>.
     *
     * return string representation of interval
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[" + getMin() + "," + getMax() + "]";
    }

    /**
     * Print out contents on an output stream
     *
     * @param os output stream receiving content dump
     */
    public void print(PrintWriter os) {
        os.print(this.toString());
    }

    /**
     * Print out contents on an output stream, terminate in newline character
     *
     * @param os output stream receiving content dump
     */
    public void println(PrintWriter os) {
        os.println(this.toString());
    }

}
