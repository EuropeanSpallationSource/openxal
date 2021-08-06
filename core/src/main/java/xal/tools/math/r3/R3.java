/*
 * R3.java
 *
 * Created on January 8, 2003, 8:52 AM
 */
package xal.tools.math.r3;

import java.io.Serializable;
import xal.tools.data.DataAdaptor;
import xal.tools.math.BaseVector;
import xal.tools.math.IIndex;

/**
 * Represents an element of R^3, the three-dimensional cartesian real space.
 *
 * @author Christopher Allen
 */
public class R3 extends BaseVector<R3> implements Serializable {

    /*
     * Internal Types
     */
    /**
     * Class <code>R3x3.IND</code> is an enumeration of the matrix indices for
     * the <code>R3x3</code> class.
     *
     * @author Christopher K. Allen
     * @since Oct 4, 2013
     */
    public enum IND implements IIndex {

        /**
         * the <em>x</em> axis index of <strong>R</strong><sup>3</sup>
         */
        X(0),
        /**
         * the <em>y</em> axis index of <strong>R</strong><sup>3</sup>
         */
        Y(1),
        /**
         * the <em>z</em> axis index of <strong>R</strong><sup>3</sup>
         */
        Z(2);

        /*
         * Operations
         */
        /**
         * Returns the value of the <code>R3x3</code> matrix index that this
         * enumeration constant represents.
         *
         * @return matrix index
         *
         * @see xal.tools.math.SquareMatrix.IIndex#val()
         *
         * @author Christopher K. Allen
         * @since Oct 4, 2013
         */
        @Override
        public int val() {
            return this.index;
        }

        /*
         * Internal Attributes
         */
        /**
         * the matrix index value
         */
        private final int index;

        /*
         * Initialization
         */
        /**
         * Constructor for IND, initializes the enumeration constant index to
         * the given value.
         *
         * @param index matrix index for this constant
         *
         * @author Christopher K. Allen
         * @since Oct 4, 2013
         */
        private IND(int index) {
            this.index = index;
        }

    }

    /*
     *  Global Constants
     */
    /**
     * serialization version identifier
     */
    private static final long serialVersionUID = 1L;

    /**
     * number of dimensions (DIM=3)
     */
    public static final int INT_SIZE = 3;

    /*
     *  Global Methods
     */
    /**
     * Create a new instance of the zero vector.
     *
     * @return zero vector
     */
    public static R3 zero() {
        return new R3(0.0, 0.0, 0.0);
    }

    /**
     * Create a new instance of R3 with initial value determined by the
     * formatted string argument. The string should be formatted as
     *
     * "(x,y,z)"
     *
     * where x, y, z are floating point representations.
     *
     * @param strTokens six-token string representing values phase coordinates
     *
     * @return 3-vector built from the given string representation
     *
     * @exception IllegalArgumentException wrong number of tokens in argument
     * (must be 6)
     * @exception NumberFormatException bad numeric value, unparseable
     */
    public static R3 parse(String strTokens)
            throws NumberFormatException, IllegalArgumentException {
        return new R3(strTokens);
    }

    /*
     *  Initialization
     */
    /**
     * Creates a new instance of R3, the zero element.
     */
    public R3() {
        super(INT_SIZE);
    }

    /**
     * Creates a new instance of R3 initialized to arguments.
     *
     * @param x1 first coordinate value
     * @param x2 first coordinate value
     * @param x3 first coordinate value
     */
    public R3(double x1, double x2, double x3) {
        super(new double[]{x1, x2, x3});
    }

    /**
     * Creates a new instance of R3 initialized to argument. If the initializing
     * array has length greater than 3 the first three values are taken, if it
     * has length less than three then the remain coordinates of the new
     * <code>R3</code> object are zero.
     *
     * @param arrVals double array of initializing values
     *
     * @throws IllegalArgumentException the argument must have the same length
     * as this vector
     */
    public R3(double[] arrVals) throws IllegalArgumentException {
        super(arrVals);

        if (arrVals.length != INT_SIZE) {
            throw new IllegalArgumentException("Argument has wrong dimensions " + arrVals);
        }
    }

    /**
     * Creates a new instance of R3 initialized to argument.
     *
     * @param vecPt deep copies this value
     */
    public R3(R3 vecPt) {
        super(vecPt);
    }

    /**
     * <p>
     * Create a new instance of R3 with specified initial value specified by the
     * formatted string argument.
     * </p>
     * <p>
     * The string should be formatted as
     * <br>
     * <br>
     * "(x,y,z)"
     * <br>
     * <br>
     * where x, y, z are floating point representations.
     * </p>
     *
     * @param strTokens token string representing values phase coordinates
     *
     * @exception IllegalArgumentException wrong number of tokens in argument
     * (must be 6 or 7)
     * @exception NumberFormatException bad numeric value, unparseable
     */
    public R3(String strTokens) throws NumberFormatException, IllegalArgumentException {
        super(INT_SIZE, strTokens);
    }

    /**
     * Initializing constructor for <code>R3</code>. Initial values are taken
     * from the data source provided. The values are parsed from a numeric
     * string and identified by the tag <code>BaseVector#ATTR_Data</code>.
     *
     * @param daSource interface to data source containing initialization data
     *
     * @author Christopher K. Allen
     * @since Nov 5, 2013
     */
    public R3(DataAdaptor daSource) {
        super(INT_SIZE, daSource);
    }

    /**
     * Performs a deep copy operation.
     *
     * @return cloned R3 object
     */
    public R3 copy() {
        return new R3(this);
    }

    /**
     * Set index to value.
     *
     * @param i element index 0<=i<=2
     * @param val new element value
     *
     * @throws ArrayIndexOutOfBoundsException the index <var>i</var> was greater
     * than 2
     */
    public void set(int i, double val) throws ArrayIndexOutOfBoundsException {
        super.setElem(i, val);
    }

    /**
     * Set first coordinate value.
     *
     * @param x1 first coordinate of 3-vector
     */
    public void set1(double x1) {
        super.setElem(IND.X, x1);
    }

    /**
     * Set second coordinate value.
     *
     * @param x2 second coordinate of 3-vector
     */
    public void set2(double x2) {
        super.setElem(IND.Y, x2);
    }

    /**
     * Set third coordinate value.
     *
     * @param x3 third coordinate of 3-vector
     */
    public void set3(double x3) {
        super.setElem(IND.Z, x3);
    }

    /**
     * Set first coordinate value.
     *
     * @param x first coordinate of 3-vector
     */
    public void setx(double x) {
        super.setElem(IND.X, x);
    }

    /**
     * Set second coordinate value.
     *
     * @param y second coordinate of 3-vector
     */
    public void sety(double y) {
        super.setElem(IND.Y, y);
    }

    /**
     * Set third coordinate value.
     *
     * @param z first coordinate of 3-vector
     */
    public void setz(double z) {
        super.setElem(IND.Z, z);
    }

    /**
     * Set all coordinates to value
     *
     * @param s new value of all vector coordinates
     */
    public void setAll(double s) {
        for (IND i : IND.values()) {
            super.setElem(i, s);
        }
    }

    /*
     *  Properties
     */
    /**
     * Get all the vector values as a 3-array.
     *
     * @return the array {<em>x</em><sub>1</sub>, <em>x</em><sub>2</sub>,
     * <em>x</em><sub>3</sub>}.
     */
    public double[] toArray() {
        return super.getArrayCopy();
    }

    /**
     * Return first coordinate value.
     *
     * @return the first vector coordinate
     */
    public double get1() {
        return super.getElem(IND.X);
    }

    /**
     * Return second coordinate value.
     *
     * @return the second coordinate vector
     */
    public double get2() {
        return super.getElem(IND.Y);
    }

    /**
     * Return third coordinate value.
     *
     * @return the third coordinate vector
     */
    public double get3() {
        return super.getElem(IND.Z);
    }

    /**
     * Return first coordinate value.
     *
     * @return the first coordinate vector
     */
    public double getx() {
        return super.getElem(IND.X);
    }

    /**
     * Return second coordinate value.
     *
     * @return the second coordinate vector
     */
    public double gety() {
        return super.getElem(IND.Y);
    }

    /**
     * Return third coordinate value.
     *
     * @return the third coordinate vector
     */
    public double getz() {
        return super.getElem(IND.Z);
    }

    /*
     *  Object method overrides
     */
    /**
     * Creates and returns a deep copy of <strong>this</strong> vector.
     *
     * @see xal.tools.math.BaseVector#clone()
     *
     * @author Jonathan M. Freed
     * @since Jul 3, 2014
     */
    @Override
    public R3 clone() {
        return new R3(this);
    }

    /**
     * Convert the vector contents to a string.
     *
     * @return vector value as a string (<em>x, y, z</em>)
     */
    @Override
    public String toString() {
        // Create vector string
        return "(" + getElem(IND.X) + "," + getElem(IND.Y) + "," + getElem(IND.Z) + ")";
    }


    /*
     *  Coordinate Transforms
     */
    /**
     * Apply coordinate transform from cartesian to cylindrical coordinates.
     *
     * @return polar coordinates (r,phi,z) of this cartesian point
     */
    public R3 cartesian2Cylindrical() {
        double x1 = get1();
        double x2 = get2();
        double x3 = get3();

        double r = Math.sqrt(x1 * x1 + x2 * x2);
        double a = Math.atan2(x2, x1);
        double z = x3;

        return new R3(r, a, z);
    }

    /**
     * Apply coordinate transform from cartesian to spherical coordinates.
     *
     * @return polar coordinates (r, theta, phi) of this cartesian point
     */
    public R3 cartesian2Spherical() {
        double x1 = get1();
        double x2 = get2();
        double x3 = get3();

        double r2 = x1 * x1 + x2 * x2;
        double rho = Math.sqrt(r2);
        double r = Math.sqrt(r2 + x3 * x3);
        double theta = Math.atan2(x3, rho);
        double phi = Math.atan2(x2, x1);

        return new R3(r, theta, phi);
    }

    /**
     * Apply coordinate transform from cylindrical to cartesian coordinates
     *
     * @return cartesian coordinates (x,y,z) of this cylindrical point
     */
    public R3 cylindrical2Cartesian() {
        double x1 = get1();
        double x2 = get2();
        double x3 = get3();

        double x = x1 * Math.cos(x2);
        double y = x1 * Math.sin(x2);
        double z = x3;

        return new R3(x, y, z);
    }

    /**
     * Apply coordinate tranform from spherical to cartesian coordinates
     *
     * @return cartesian coordinates (x,y,z) of this sphereical point
     */
    public R3 spherical2Cartesian() {
        double x1 = get1();
        double x2 = get2();
        double x3 = get3();

        double r = x1 * Math.cos(x2);
        double x = r * Math.cos(x3);
        double y = r * Math.sin(x3);
        double z = x1 * Math.sin(x2);

        return new R3(x, y, z);
    }

    /*
     *  Algebraic Methods
     */
    /**
     * Vector multiplication using three-dimensional cross product.
     *
     * @param r second (right) operand in cross-product (this is first operand)
     *
     * @return result of vector cross product in three space
     */
    public R3 times(R3 r) {
        double x1 = get1();
        double x2 = get2();
        double x3 = get3();

        return new R3(x2 * r.get3() - x3 * r.get2(), x3 * r.get1() - x1 * r.get3(), x1 * r.get2() - x2 * r.get1());
    }

    /*
     *  Geometric Methods
     */
    /**
     * Returns the vector of squared elements.
     *
     * @return the vector (<em>x</em><sub>1</sub><sup>2</sup>,
     * <em>x</em><sub>2</sub><sup>2</sup>,
     * <em>x</em><sub>3</sub><sup>2</sup>)
     *
     * @author Christopher K. Allen
     * @since Aug 25, 2011
     */
    public R3 squared() {
        double x1 = get1();
        double x2 = get2();
        double x3 = get3();

        return new R3(x1 * x1, x2 * x2, x3 * x3);
    }

    /**
     * Handles object creation required by the base class.
     *
     * @see xal.tools.math.BaseVector#newInstance()
     *
     * @author Ivo List
     * @author Christopher K. Allen
     * @since Jun 17, 2014
     */
    @Override
    protected R3 newInstance(int size) {
        return new R3();
    }

    /**
     *
     * @see xal.tools.math.BaseVector#newInstance(double[])
     *
     * @since Jul 24, 2015 by Christopher K. Allen
     */
    @Override
    protected R3 newInstance(double[] arrVecInt) {
        return new R3(arrVecInt);
    }
}
