/*
 * R3x3.java
 *
 * Created on September 16, 2003, 2:32 PM
 * Modified:
 * 
 */

package xal.tools.math.r3;


import java.util.EnumSet;

import xal.tools.math.IIndex;
import xal.tools.math.SquareMatrix;



/**
 *  <p>
 *  Represents an element of <b>R</b><sup>3&times;3</sup>, the set of real, 
 *  3&times;3 matrices.
 *  The class also contains the usual set of matrix operations and linear
 *  transforms on <b>R</b><sup>3</sup> induced by the matrix.  
 *  </p>
 *
 * @author  Christopher Allen
 *
 *  @see    xal.tools.r3.R3
 */

public class R3x3 extends SquareMatrix<R3x3> implements java.io.Serializable {
    
    
    /*
     * Internal Types
     */
    
    /**
     * Class <code>R3x3.IND</code> is an enumeration of the matrix indices
     * for the <code>R3x3</code> class.
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    public enum IND implements IIndex {
        
        /** the <i>x</i> axis index of <b>R</b><sup>3</sup> */
        X(0),

        /** the <i>y</i> axis index of <b>R</b><sup>3</sup> */
        Y(1),
        
        /** the <i>z</i> axis index of <b>R</b><sup>3</sup> */
        Z(2);

        
        /*
         * Operations
         */
        
        /**
         * Returns the value of the <code>R3x3</code> matrix index that this
         * enumeration constant represents.
         * 
         * @return      matrix index
         *
         * @see xal.tools.math.SquareMatrix.IIndex#val()
         *
         * @author Christopher K. Allen
         * @since  Oct 4, 2013
         */
        @Override
        public int val() {
            return this.index;
        }

        /*
         * Internal Attributes
         */
        
        /** the matrix index value */
        private final int       index;
        
        /*
         * Initialization
         */

        /**
         * Constructor for IND, initializes the enumeration constant
         * index to the given value.
         *
         * @param index     matrix index for this constant
         *
         * @author Christopher K. Allen
         * @since  Oct 4, 2013
         */
        private IND(int index) {
            this.index = index;
        }

    }
    
    /** 
     * Enumeration for the element positions of an 
     * <code>R3x3</code> matrix element.  Also provides some 
     * convenience functions for accessing these <code>R3x3</code> 
     * elements.
     * 
     * @author  Christopher K. Allen
     */
    public enum POS  {
        
        /*
         * Enumeration Constants
         */
        XY (0,1), 
        XZ (0,2), 
        YZ (1,2),
        XX (0,0),
        YY (1,1),
        ZZ (2,2),
        YX (1,0),
        ZX (2,0),
        ZY (2,1);
                    
        
        /*
         * Local Attributes
         */
        
        /** row index */
        private final int i;
        
        /** column index */
        private final int j;
        
        
        /*
         * Initialization
         */
        
        
        /** 
         * Default enumeration constructor 
         */
        POS(int i, int j)  {
            this.i = i;
            this.j = j;
        }
        
        /** return the row index of the matrix position */
        public int row()    { return i; };

        /** return the column index of the matrix position */
        public int col()    { return j; };

        
        /** 
         * Return the <code>Position</code> object representing the 
         * transpose element of this position.
         * 
         * NOTE:
         * The current implementation is slow.
         * 
         * @return  the transpose position of the current position
         */ 
        public POS transpose() { 
            int i = this.col();
            int j = this.row();
            
            for (POS pos : POS.values()) {
                if (pos.row() == i && pos.col() == j)
                    return pos;
            }

            return null;
        };
        
        
        
        /*
         * Enumerating Positions
         */
        
        /** 
         *  Returns the set of all element positions above the matrix 
         *  diagonal.
         *  
         *  @return     set of upper triangle matrix positions
         */
        public static EnumSet<POS> getUpperTriangle() { return EnumSet.of(XY, XZ, YZ); };
        
        /**
         * Return the set of all matrix element positions along the 
         * diagonal.
         * 
         * @return      set of diagonal element positions
         */
        public static EnumSet<POS> getDiagonal()      { return EnumSet.of(XX, YY, ZZ); };
        
        /**
         * Return the set of all element positions below the matrix
         * diagonal.
         * 
         * @return      set of lower triangle matrix positions
         */
        public static EnumSet<POS> getLowerTriangle() { return EnumSet.of(YX, ZX, ZY); };
        
        /**
         * Return the set of all off-diagonal matrix positions.
         * 
         * @return      set of off diagonal positions, both upper and lower.
         */
        public static EnumSet<POS> getOffDiagonal() { return EnumSet.complementOf(POS.getDiagonal()); };
        
        
        
        /*
         * Matrix Element Accessing
         */
        
        /** 
         * Return the matrix element value for this position
         * 
         * @param   matTarget   target matrix
         * @return              element value for this position
         */
        public double   getValue(R3x3 matTarget)    { return matTarget.getElem(row(),col()); };

        /**
         * Get the diagonal element in the same row as this element position.
         * 
         * @param matTarget     target matrix
         * @return              row diagonal element value
         */
        public double   getRowDiag(R3x3 matTarget)  { return matTarget.getElem(row(),row()); };
        
        /**
         * Get the diagonal element in the same column as this element position.
         * 
         * @param matTarget     target matrix
         * @return              column diagonal element value
         */
        public double   getColDiag(R3x3 matTarget)  { return matTarget.getElem(col(),col()); };
        
        
        /*
         * Matrix Element Assignment 
         */
        
        /** 
         * Set matrix element value for this position
         * 
         * @param   matTarget   target matrix
         * @param   s           new value for matrix element
         */
        public void setValue(R3x3 matTarget, double s)    { matTarget.setElem(row(),col(), s); };

        /**
         * Set the diagonal element in the same row as this element position.
         * 
         * @param matTarget     target matrix
         * @param s             new value for matrix element
         */
        public void setRowDiag(R3x3 matTarget, double s)  { matTarget.setElem(row(),row(), s); };

        /**
         * Set the diagonal element in the same column as this element position.
         * 
         * @param matTarget     target matrix
         * @param s             new value for matrix element
         */
        public void setColDiag(R3x3 matTarget, double s)  { matTarget.setElem(col(),col(), s); };
    };

    

    
    /*
     *  Global Constants
     */
     
     /** serialization version identifier */
    private static final long serialVersionUID = 1L;

     /** number of dimensions (DIM=3) */
     public static final int    INT_SIZE = 3;
     
    
    
    /*
     *  Global Methods
     */
    
    /**
     *  Create a new instance of a zero matrix.
     *
     *  @return         matrix whose elements are all zero
     */
    public static R3x3  newZero()   {
        R3x3    matZero = new R3x3();
        matZero.assignZero();
        
        return matZero;
    }
    
    /**
     *  Create a new instance of the identity matrix
     *
     *  @return         identity matrix object
     */
    public static R3x3  newIdentity()   {
        R3x3    matIden = new R3x3();
        matIden.assignIdentity();
        
        return matIden;
    }
    
    /**
     * Create and return the generator element of SO(3) which is
     * a counter-clockwise rotation about the x axis.
     * 
     * @param   rotation angle in radians
     * 
     * @return  x-plane counter-clockwise rotation matrix 
     */
    public static R3x3  newRotationX(double dblAng)    {
        double  sin   = Math.sin(dblAng);
        double  cos   = Math.cos(dblAng);
        R3x3    matRx = R3x3.newIdentity();

        matRx.setElem(POS.YY,  cos);
        matRx.setElem(POS.YZ, -sin);
        matRx.setElem(POS.ZY,  sin);
        matRx.setElem(POS.ZZ,  cos);
        
        return matRx;
    }
    
    /**
     * Create and return the generator element of SO(3) which is
     * a counter-clockwise rotation about the y axis.
     * 
     * @param   rotation angle in radians
     * 
     * @return  y-plane counter-clockwise rotation matrix 
     */
    public static R3x3  newRotationY(double dblAng)    {
        double  sin   = Math.sin(dblAng);
        double  cos   = Math.cos(dblAng);
        R3x3    matRy = R3x3.newIdentity();

        matRy.setElem(POS.XX,  cos);
        matRy.setElem(POS.XZ,  sin);
        matRy.setElem(POS.ZX, -sin);
        matRy.setElem(POS.ZZ,  cos);
        
        return matRy;
    }
    
    /**
     * Create and return the generator element of SO(3) which is
     * a counter-clockwise rotation about the z axis.
     * 
     * @param   rotation angle in radians
     * 
     * @return  z-plane counter-clockwise rotation matrix 
     */
    public static R3x3  newRotationZ(double dblAng)    {
        double  sin   = Math.sin(dblAng);
        double  cos   = Math.cos(dblAng);
        R3x3    matRz = R3x3.newIdentity();

        matRz.setElem(POS.XX,  cos);
        matRz.setElem(POS.XY, -sin);
        matRz.setElem(POS.YX,  sin);
        matRz.setElem(POS.YY,  cos);
        
        return matRz;
    }
    
    /**
     * Create a deep copy of the given <code>R3x3</code> matrix object.  The returned 
     * object is completely decoupled from the original.
     * 
     * @param   matTarget   matrix to be copied
     * @return              a deep copy of the argument object
     */
    public static R3x3  copy(R3x3 matTarget)    {
    	return matTarget.copy();
    }
    
    /**  
     *  Create a R3x3 instance and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  strTokens   token vector of 3x3=9 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    public static R3x3 parse(String strTokens)    
        throws IllegalArgumentException, NumberFormatException
    {
        return new R3x3(strTokens);
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
     * @since  Jul 3, 2014
     */
    @Override
    public R3x3 clone() {
        return new R3x3(this);
    }

    
    /*
     * Initialization
     */
    
    /** 
     *  Creates a new instance of R3x3 initialized to zero.
     */
    public R3x3() {
        super(INT_SIZE);
    }
    
    /**
     *  Copy Constructor - create a deep copy of the target matrix.
     *
     *  @param  matInit     initial value
     */
    public R3x3(R3x3 matInit) {
        super(matInit);
    }
    
    /**
     * Initializing constructor for class <code>R3x3</code>.  The values of the
     * new matrix are set to the given Java primitive type array (the array
     * itself is unchanged).  The dimensions of the argument must be
     * 3&times;3.
     *
     * @param   arrValues   initial values for the new matrix object
     * 
     * @exception  ArrayIndexOutOfBoundsException  the argument must have the dimensions 3&times;3
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    public R3x3(double[][] arrValues) throws ArrayIndexOutOfBoundsException {
        super(INT_SIZE, arrValues);
    }
    
    /**
     *  Parsing Constructor - create a R3x3 instance and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  strTokens   token vector of 3x3=9 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    public R3x3(String strTokens) throws IllegalArgumentException, NumberFormatException {
        super(INT_SIZE, strTokens);
    }
    


    /*
     *  Assignment
     */
    
    /**
     *  Element assignment - assigns matrix element to the specified value
     *
     *  @param  i       row index
     *  @param  j       column index
     *  @parm   s       new matrix element value
     *
     *  @exception  ArrayIndexOutOfBoundsException  index must be in {0,1,2}
     */
    public void setElem(IND i, IND j, double s) throws ArrayIndexOutOfBoundsException {
        super.setElem(i, j, s);
    }
    
    /**
     * Set the element specified by the position in the argument to the
     * new value in the second argument.
     * 
     * @param   pos     matrix position
     * @param   val     new element value
     */
    public void setElem(POS pos, double val)   {
        super.setElem(pos.row(),pos.col(), val);
    }
    
    
    /*
     *  Matrix Properties
     */

    /**
     *  Return matrix element value.  Get matrix element value at specified 
     *  position.
     *
     *  @param  i       row index
     *  @param  j       column index
     */
    public double getElem(POS pos)   {
        return super.getElem(pos.row(), pos.col());
    }
    
    /**
     *  Nondestructive Matrix-Vector multiplication.
     *
     *  @return     this*vec
     */
    public R3  times(R3 vec)  {
        double x = getElem(0,0)*vec.getx() + getElem(0,1)*vec.gety() + getElem(0,2)*vec.getz();
        double y = getElem(1,0)*vec.getx() + getElem(1,1)*vec.gety() + getElem(1,2)*vec.getz();
        double z = getElem(2,0)*vec.getx() + getElem(2,1)*vec.gety() + getElem(2,2)*vec.getz();
     
        return new R3(x, y, z);
    }

	/**
     * Handles object creation required by the base class. 
     *
	 * @see xal.tools.math.BaseMatrix#newInstance()
	 * 
	 * @author Ivo List
	 * @author Christopher K. Allen
	 * @since  Jun 17, 2014
	 */
	@Override
	protected R3x3 newInstance(int row, int cnt) {
		return new R3x3();
	}
}
