/*
 * R2x2.java
 *
 * Created on September 16, 2003, 2:32 PM
 * Modified: September 30, 2013
 * 
 */

package xal.tools.math.r2;


import xal.tools.math.IIndex;
import xal.tools.math.SquareMatrix;


/**
 *  <p>
 *  Represents an element of R2x2, the set of real 3x3 matrices.
 *  The class a set of the usual matrix operations and linear
 *  transforms on R3 represented by the matrix.  
 *  </p>
 *
 * @author  Christopher Allen
 *
 *  @see    Jama.Matrix
 *  @see    xal.tools.r3.R3
 */

public class R2x2 extends SquareMatrix<R2x2> implements java.io.Serializable {
    
    /**
     * Enumeration of the allowed index positions for objects of type
     * <code>R2x2</code>.
     *
     *
     * @author Christopher K. Allen
     * @since  Oct 2, 2013
     */
    public enum IND implements IIndex {
    	
    	/** represents the phase coordinate index */
    	X(0),
    	
    	/** represents the phase angle index */
    	P(1);

		/**
		 * Returns the numerical value of this index enumeration constant.
		 * 
		 * @return	numerical index value
		 *
		 * @see xal.tools.math.SquareMatrix.IIndex#val()
		 *
		 * @author Christopher K. Allen
		 * @since  Sep 27, 2013
		 */
		@Override
		public int val() {
			return this.intVal;
		}

		
		/*
		 * Local Attributes
		 */
		
		/** The numerical index value */
		private final int		intVal;
		

		/*
         * Initialization
         */
        
        /** 
         * Default enumeration constructor 
         */
		private IND(int intVal) {
			this.intVal = intVal;
		}
    }
    
    
    /*
     *  Global Constants
     */
     
     /** serialization version identifier */
    private static final long serialVersionUID = 1L;

     
     /** Matrix size */
     public static final int    INT_SIZE = 2;
     
    
    
    /*
     *  Global Methods
     */
    
    /**
     *  Create and return a new instance of a zero matrix.
     *
     *  @return         a matrix with all zero elements
     */
    public static R2x2  newZero()   {
        R2x2    matZero = new R2x2();
        matZero.assignZero();
        
        return matZero;
    }
    
    /**
     *  Create and return a new identity matrix
     *
     *  @return         identity matrix object
     */
    public static R2x2  newIdentity()   {
        R2x2    matIden = new R2x2();
        matIden.assignIdentity();
        
        return  matIden; 
    }
    
    public static R2x2 newSymplectic() {
        R2x2    matJ = new R2x2();
        matJ.assignZero();
        matJ.setElem(IND.X, IND.P, +1.0);
        matJ.setElem(IND.P, IND.X, -1.0);
        
        return matJ;
    }
    
    /**
     * Create and return the generator element of SO(2) which is
     * a counter-clockwise rotation.
     * 
     * @param   rotation angle in radians
     * 
     * @return  x-plane counter-clockwise rotation matrix 
     */
    public static R2x2  newRotation(double dblAng)    {
        double  sin   = Math.sin(dblAng);
        double  cos   = Math.cos(dblAng);
        R2x2    matRx = R2x2.newIdentity();

        matRx.setElem(IND.X,  IND.X,  +cos);
        matRx.setElem(IND.X,  IND.P, -sin);
        matRx.setElem(IND.P, IND.X,  +sin);
        matRx.setElem(IND.P, IND.P, +cos);
        
        return matRx;
    }
    
    /**
     * Create a deep copy of the given <code>R2x2</code> matrix object.  The returned 
     * object shares no references with the argument.
     * 
     * @param   matTarget   matrix to be copied
     * 
     * @return              a deep copy of the argument object
     * 
     */
    public static R2x2  clone(R2x2 matTarget) {
    	return matTarget.copy();
    }
    
    /**
     *  Create a R2x2 instance and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  strTokens   token vector of 2x2=4 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    public static R2x2 parse(String strTokens)    
        throws IllegalArgumentException, NumberFormatException
    {
        return new R2x2(strTokens);
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
    public R2x2 clone() {
        return new R2x2(this);
    }
    
    
    /*
     * Initialization
     */
    
    /** 
     *  Creates a new instance of R2x2 initialized to zero.
     */
    public R2x2() {
    	super(INT_SIZE);
    }
    
    /**
	 *  Copy Constructor - create a deep copy of the given matrix.
	 *
	 *  @param  matParent     initial value
	 */
	public R2x2(R2x2 matParent) {
		super(matParent);
	}
    
	/**
	 *  Parsing Constructor - create a R2x2 instance and initialize it
	 *  according to a token string of element values.  
	 *
	 *  The token string argument is assumed to be one-dimensional and packed by
	 *  column (ala FORTRAN).
	 *
	 *  @param  strTokens   token vector of 2x2=4 numeric values
	 *
	 *  @exception  IllegalArgumentException    wrong number of token strings
	 *  @exception  NumberFormatException       bad number format, unparseable
	 */
	public R2x2(String strTokens) throws IllegalArgumentException, NumberFormatException {
	    super(INT_SIZE, strTokens);
	}


    /*
     *  Assignment
     */
    
	/**
     * Set the element the given indices to the new value.
     * 
     * @param   iRow	matrix row location
     * @param	iCol	matrix column index
     * 
     * @param   val     matrix element at given row and column will be set to this value
     */
    public void setElem(IND iRow, IND iCol, double dblVal)   {
        super.setElem(iRow, iCol, dblVal);
    }
    
    
    
    /*
     *  Matrix Properties
     */

    /**
     *  Return matrix element value.  Get matrix element value at specified 
     *  position.
     *
     *  @param  iRow       row index
     *  @param  iCol       column index
     * 
     * @return			the matrix element at the position specified by the indices.
     */
    public double getElem(IND iRow, IND iCol) 
    {
        return super.getElem(iRow, iCol);
    }
    
    /**
     *  Non-destructive Matrix-Vector multiplication. Specifically, the
     *  vector <b>y</b> given by
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>y</b> = <b>Ax</b>
     *  <br>
     *  <br>
     *  where <b>A</b> is this matrix and <b>x</b> is the given vector.
     *  
     *  @param vec	the vector factor <b>x</bx>
     *
     *  @return     the matrix-vector product of this matrix with the given vector
     */
    public R2  times(R2 vec)  {
        
        double x = getElem(0,0)*vec.getx() + getElem(0,1)*vec.gety();
        double y = getElem(1,0)*vec.getx() + getElem(1,1)*vec.gety();
     
        return new R2(x, y);
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
	protected R2x2 newInstance(int row, int cnt) {
		return new R2x2();
	}

    
    
}
