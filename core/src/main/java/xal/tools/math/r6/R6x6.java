/**
 * R6x6.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 15, 2013
 */
package xal.tools.math.r6;

import xal.tools.math.IIndex;
import xal.tools.math.SquareMatrix;
import xal.tools.math.r3.R3x3;
import xal.tools.math.r3.R3x3.POS;

/**
 * <p>
 * Represents an element of <strong>R</strong><sup>6&times;6</sup>, the set of
 * real, 6&times;6 matrices. The class also contains the usual set of matrix
 * operations and linear transforms on <strong>R</strong><sup>6</sup> that are
 * represented by these matrices.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Oct 15, 2013
 */
public class R6x6 extends SquareMatrix<R6x6> {

    /*
     * Internal Types
     */
    /**
     * Enumeration for the element position indices of a homogeneous phase space
     * objects. Extends the <code>PhaseIndex</code> class by adding the
     * homogeneous element position <code>HOM</code>.
     *
     * @author Christopher K. Allen
     * @since Oct, 2013
     */
    public enum IND implements IIndex {

        /*
         * Enumeration Constants
         */
        /**
         * Index of the 1st coordinate
         */
        X1(0),
        /**
         * Index of the 2nd coordinate
         */
        X2(1),
        /**
         * Index of the 3rd coordinate
         */
        X3(2),
        /**
         * Index of the 4th coordinate
         */
        X4(3),
        /**
         * Index of the 5th coordinate
         */
        X5(4),
        /**
         * Index of the 6th coordinate
         */
        X6(5);

        /*
         * IIndex Interface
         */
        /**
         * Return the integer value of the index position
         */
        @Override
        public int val() {
            return i;
        }
        ;

        
        /*
         * Local Attributes
         */
        
        /** index value */
        private final int i;

        /*
         * Initialization
         */
        /**
         * Default enumeration constructor
         */
        IND(int i) {
            this.i = i;
        }
    }

    /*
     * Global Constants
     */
    /**
     * Serialization identifier
     */
    private static final long serialVersionUID = 1L;

    /**
     * number of dimensions
     */
    public static final int INT_SIZE = 6;

    /*
     *  Global Methods
     */
    /**
     * Create a new instance of a zero matrix.
     *
     * @return zero vector
     */
    public static R6x6 newZero() {
        R6x6 matZero = new R6x6();
        matZero.assignZero();
        return matZero;
    }

    /**
     * Create a new identity matrix
     *
     * @return 6&times;6 real identity matrix
     */
    public static R6x6 newIdentity() {
        R6x6 matIden = new R6x6();
        matIden.assignIdentity();
        return matIden;
    }

    /**
     * <p>
     * Compute the rotation matrix in phase space that is essentially the
     * Cartesian product of the given rotation matrix in <em>SO</em>(3). That
     * is, if the given argument is the rotation <strong>O</strong>, the
     * returned matrix, denoted <strong>M</strong>, is the <strong>M</strong> =
     * <strong>O</strong>&times;<strong>O</strong>&times;<strong>I</strong>
     * embedding into homogeneous phase space
     * <strong>R</strong><sup>6&times;6</sup>&times;{1}. Thus,
     * <strong>M</strong> &in; <em>SO</em>(6) &sub;
     * <strong>R</strong><sup>6&times;6</sup>&times;{1}.
     * </p>
     * <p>
     * Viewing phase-space as a 6D manifold built as the tangent bundle over
     * <strong>R</strong><sup>3</sup> configuration space, then the fibers of 3D
     * configuration space at a point (<em>x,y,z</em>) are represented by the
     * Cartesian planes (<em>x',y',z'</em>). The returned phase matrix rotates
     * these fibers in the same manner as their base point (<em>x,y,z</em>).
     * </p>
     * <p>
     * This is a convenience method to build the above rotation matrix in
     * <em>SO</em>(7).
     * </p>
     *
     * @param matSO3 a rotation matrix in three dimensions, i.e., a member of
     * <em>SO</em>(3) &sub; <strong>R</strong><sup>3&times;3</sup>
     *
     * @return rotation matrix in <em>S0</em>(7) which is direct product of
     * rotations in <em>S0</em>(3)
     */
    public static R6x6 rotationProduct(R3x3 matSO3) {

        // Populate the phase rotation matrix
        R6x6 matSO6 = R6x6.newIdentity();

        // indices into the SO(7) matrix
        int m, n;
        // matSO3 matrix element
        double val;

        for (POS pos : POS.values()) {
            m = 2 * pos.row();
            n = 2 * pos.col();

            val = matSO3.getElem(pos);

            // configuration space
            matSO6.setElem(m, n, val);
            // momentum space
            matSO6.setElem(m + 1, n + 1, val);
        }
        return matSO6;
    }

    /**
     * <p>
     * Create a new <code>R6x6</code> instance and initialize it according to a
     * token string of element values.
     * </p>
     * <p>
     * The token string argument is assumed to be one-dimensional and packed by
     * column (ala FORTRAN).
     * </p>
     *
     * @param strTokens token vector of 6x6=36 numeric values
     *
     * @return real matrix with elements initialized by the given numeric tokens
     *
     * @exception IllegalArgumentException wrong number of string tokens
     * @exception NumberFormatException bad number format, unparseable
     */
    public static R6x6 parse(String strTokens)
            throws IllegalArgumentException, NumberFormatException {
        return new R6x6(strTokens);
    }

    /*
     * Object Overrides
     */
    /**
     * Creates and returns a deep copy of this matrix.
     *
     * @see xal.tools.math.BaseMatrix#clone()
     *
     * @author Christopher K. Allen
     * @since Jul 3, 2014
     */
    @Override
    public R6x6 clone() {
        return new R6x6(this);
    }

    /*
     * Initialization
     */
    /**
     * Zero argument constructor for R6x6. Returns a matrix of zeros.
     *
     * @throws UnsupportedOperationException only thrown in the absence of this
     * constructor
     *
     * @author Christopher K. Allen
     * @since Oct 15, 2013
     */
    public R6x6() throws UnsupportedOperationException {
        super(INT_SIZE);
    }

    /**
     * Initializing constructor for <code>R6x6</code>. The matrix elements are
     * set to those in the given Java native array, which must be 6&times;6
     * dimensional.
     *
     * @param arrVals initial values for new matrix
     *
     * @throws ArrayIndexOutOfBoundsException the given native array is not
     * 6&times;6 dimensional
     *
     * @author Christopher K. Allen
     * @since Oct 15, 2013
     */
    public R6x6(double[][] arrVals) throws ArrayIndexOutOfBoundsException {
        super(arrVals);
    }

    /**
     * <p>
     * Parsing Constructor - creates an instance of the child class and
     * initialize it according to a token string of element values.
     * </p>
     * <p>
     * The token string argument is assumed to be one-dimensional and packed by
     * column (ala FORTRAN).
     * </p>
     *
     * @param strTokens token vector of getSize()^2 numeric values
     *
     * @exception IllegalArgumentException wrong number of string tokens
     * @exception NumberFormatException bad number format, unparseable
     *
     * @author Christopher K. Allen
     * @since Oct 15, 2013
     */
    public R6x6(String strTokens) throws IllegalArgumentException, NumberFormatException {
        super(INT_SIZE, strTokens);
    }

    /**
     * Copy constructor for <code>R6x6</code>. Creates a deep copy of the given
     * object. The dimensions are set and the internal array is cloned.
     *
     * @param matParent the matrix to be cloned
     *
     * @throws UnsupportedOperationException base class has not defined a
     * public, zero-argument constructor
     *
     * @author Christopher K. Allen
     * @since Oct 15, 2013
     */
    public R6x6(R6x6 matParent) throws UnsupportedOperationException {
        super(matParent);
    }

    /**
     * Handles object creation required by the base class.
     *
     * @see xal.tools.math.BaseMatrix#newInstance()
     *
     * @author Ivo List
     * @author Christopher K. Allen
     * @since Jun 17, 2014
     */
    @Override
    protected R6x6 newInstance(int row, int cnt) {
        return new R6x6();
    }
}
