/**
 * BaseMatrix.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 25, 2013
 */
package xal.tools.math;

import Jama.Matrix;

/**
 * <p>
 * Class <code>SquareMatrix</code> is the abstract base class for square matrix
 * objects supported in the XAL tools packages.
 * </p>
 * <p>
 * Currently the internal matrix operations are supported by the <tt>Jama</tt>
 * matrix package.  However, the <tt>Jama</tt> matrix package has been deemed a 
 * "proof of principle" for the Java language and scientific computing and 
 * is, thus, no longer supported.  The objective of this base class is to hide
 * the internal implementation of matrix operations from the child classes and
 * all developers using the matrix packages.  If it is determined that the <tt>Jama</tt>
 * matrix package is to be removed from XAL, the modification will be substantially
 * simplified in the current architecture.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since  Sep 25, 2013
 */
public abstract class SquareMatrix<M extends SquareMatrix<M>> extends BaseMatrix<M> {

    
    /*
     *  Local Attributes
     */

    /** size of the the square matrix */
    private final int   intSize;


    /*
     *  Assignment
     */

    /**
     * Assign this matrix to be the identity matrix.  The
     * identity matrix is the square matrix with 1's on the
     * diagonal and 0's everywhere else.
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2013
     */
    public void assignIdentity() {
        this.assignZero();
        
        for (int i=0; i<this.getSize(); i++)
            this.setElem(i, i, 1.0);
    }
    


    /*
     *  Matrix Attributes
     */

    /**
     * Returns the size of this square matrix, that is, the equal
     * number of rows and columns.
     * 
     * @return  matrix is square of this size on a side
     *
     * @author Christopher K. Allen
     * @since  Sep 25, 2013
     */
    public int  getSize() {
        return this.intSize;
    }

    /*
     * Matrix Properties
     */
    
    /**
     *  Check if matrix is symmetric.  
     * 
     *  @return true if matrix is symmetric 
     */
    public boolean isSymmetric()   {
    
        for (int i=0; i<this.getSize(); i++)
            for (int j=i; j<this.getSize(); j++) {
                if (getElem(i,j) != getElem(j,i) )
                    return false;
            }
        return true;
    }


    /*
     *  Matrix Operations
     */
    
    /**
     *  Matrix determinant function.
     *
     *  @return     the determinant of this square matrix
     */
    public double det()     { 
        return this.getMatrix().det(); 
    };
    
    /**
     * <p>
     * Solves the linear matrix-vector system without destroying the given
     * data vector.  Say the linear system can be represented algebraically
     * as
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>Ax</b> = <b>y</b> ,
     * <br>
     * <br>
     * where <b>A</b> is this matrix, <b>x</b> is the solution matrix to be
     * determined, and <b>y</b> is the data vector provided as the argument.
     * The returned value is equivalent to 
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>x</b> = <b>A</b><sup>-1</sup><b>y</b> ,
     * <br>
     * <br>
     * that is, the value of vector <b>x</b>.  
     * <p>
     * </p>
     * The vector <b>y</b> is left
     * unchanged.  However, this is somewhat expensive in that the solution
     * vector must be created through reflection and exceptions may occur.
     * For a safer implementation, but where the solution is returned within the
     * existing data vector <b>y</b> see <code>{@link #solveInPlace(BaseVector)}</code>.
     * </p>
     * <p>
     * Note that the inverse matrix
     * <b>A</b><sup>-1</sup> is never computed, the system is solved in 
     * less than <i>N</i><sup>2</sup> time.  However, if this system is to be
     * solved repeated for the same matrix <b>A</b> it may be preferable to 
     * invert this matrix and solve the multiple system with matrix multiplication.
     * </p>
     * 
     * @param vecObs        the data vector
     * 
     * @return              vector which, when multiplied by this matrix, will equal the data vector
     * 
     * @throws IllegalArgumentException     the argument has the wrong size
     *
     * @author Christopher K. Allen
     * @since  Oct 11, 2013
     */
    public <V extends BaseVector<V>> V solve(final V vecObs) throws IllegalArgumentException {
        
        // Check sizes
        if ( vecObs.getSize() != this.getSize() ) 
            throw new IllegalArgumentException(vecObs.getClass().getName() + " vector must have compatible size");
        
        // Get the implementation matrix.
        Matrix impL = this.getMatrix();
        
        // Create a Jama matrix for the observation vector 
        Matrix impObs = new Matrix(this.getSize(), 1 ,0.0);
        for (int i=0; i<this.getSize(); i++) 
            impObs.set(i,0, vecObs.getElem(i));
        
        // Solve the matrix-vector system in the Jama package
        Matrix impState = impL.solve(impObs);
        
        V   vecSoln = vecObs.newInstance();
        
        for (int i=0; i<this.getSize(); i++) {
            double dblVal = impState.get(i,  0);
            
            vecSoln.setElem(i,  dblVal);
        }
        
        return vecSoln;
 
    }

    /**
     * <p>
     * Solves the linear matrix-vector system and returns the solution in
     * the given data vector.  Say the linear system can be represented 
     * algebraically as
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>Ax</b> = <b>y</b> ,
     * <br>
     * <br>
     * where <b>A</b> is this matrix, <b>x</b> is the solution matrix to be
     * determined, and <b>y</b> is the data vector provided as the argument.
     * The returned value is equivalent to 
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>x</b> = <b>A</b><sup>-1</sup><b>y</b> ,
     * <br>
     * <br>
     * that is, the value of vector <b>y</b>.  
     * </p>
     * <p> 
     * The value of <b>x</b> is returned within the argument vector.  Thus,
     * the argument cannot be immutable.
     * <p>
     * Note that the inverse matrix
     * <b>A</b><sup>-1</sup> is never computed, the system is solved in 
     * less than <i>N</i><sup>2</sup> time.  However, if this system is to be
     * solved repeated for the same matrix <b>A</b> it may be preferable to 
     * invert this matrix and solve the multiple system with matrix multiplication.
     * </p>
     * 
     * @param vecObs        the data vector on call, the solution vector upon return
     * 
     * @throws IllegalArgumentException     the argument has the wrong size
     *
     * @author Christopher K. Allen
     * @since  Oct 11, 2013
     */
    public <V extends BaseVector<V>> void solveInPlace(V vecObs) throws IllegalArgumentException {
        
        // Check sizes
        if ( vecObs.getSize() != this.getSize() ) 
            throw new IllegalArgumentException(vecObs.getClass().getName() + " vector must have compatible size");
        
        // Get the implementation matrix.
        Matrix impL = this.getMatrix();
        
        // Create a Jama matrix for the observation vector 
        Matrix impObs = new Matrix(this.getSize(), 1 ,0.0);
        for (int i=0; i<this.getSize(); i++) 
            impObs.set(i,0, vecObs.getElem(i));
        
        // Solve the matrix-vector system in the Jama package
        Matrix impState = impL.solve(impObs);
        
        for (int i=0; i<this.getSize(); i++) {
            double dblVal = impState.get(i,  0);
            
            vecObs.setElem(i,  dblVal);
        }
    }

    /*
     *  Algebraic Operations
     */

    /**
     *  @see BaseMatrix#times(BaseMatrix)
     *  Product of square matrices always produce a square matrix.
     */
    @SuppressWarnings("unchecked")
    public M times(M matRight) {
        return (M)super.times(matRight); // Product of square matrices is always a square matrix
    }

    /**
     * @see BaseMatrix#times(BaseVector)
     * Square matrix preserves the size of the vector
     */
    @SuppressWarnings("unchecked")
    public <V extends BaseVector<V>> V times(V vecFac) throws IllegalArgumentException {
    	return (V)super.times(vecFac); // Product of square matrix and vector returns the same size of vector
    }
    
    /**
     * In-place matrix element by element multiplication.
     * @deprecated Misleading name, this is not real matrix multiplication.
     *
     * @param  matMult Matrix providing the factors.
     */
    @Deprecated
    public void timesEquals(BaseMatrix<M>   matMult) {
        BaseMatrix<M> matBase = matMult;
        
        this.getMatrix().arrayTimesEquals( matBase.getMatrix() );
    }
    
    
    /**
     *  <p>
     *  Function for transpose conjugation of this matrix by the argument matrix.  
     *  This method is non-destructive, returning a new matrix.
     *  </p>
     *  <p>
     *  Denote by <b>&sigma;</b><sub>0</sub> this matrix object, and denote 
     *  the argument matrix as <b>&Phi;</b>.  Then the returned matrix,
     *  <b>&sigma;</b><sub>1</sub> is given by
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>&sigma;</b><sub>1</sub> = <b>&Phi;</b><b>&sigma;</b><sub>0</sub><b>&Phi;</b><sup><i>T</i></sup>
     *  <br>
     *  <br> 
     *  </p>
     *
     *  @param  matPhi      conjugating matrix <b>&Phi;</b> (typically a transfer matrix)
     *
     *  @return             matPhi*this*matPhi^T, or <code>null</code> if an error occurred
     */
    public M    conjugateTrans(M matPhi) {
        Matrix impPhi  = ((BaseMatrix<M>)matPhi).getMatrix();
        Matrix impPhiT = impPhi.transpose();
        Matrix impAns  = impPhi.times( this.getMatrix().times( impPhiT) );
        
        M   matAns = this.newInstance(impAns);
        
        return matAns;
    };
    
    /**
     *  <p>
     *  Function for inverse conjugation of this matrix by the argument matrix.  
     *  This method is non-destructive, return a new matrix.
     *  </p>
     *  <p>
     *  Denote by <b>&sigma;</b><sub>0</sub> this matrix object, and denote 
     *  the argument matrix as <b>&Phi;</b>.  Then the returned matrix,
     *  <b>&sigma;</b><sub>1</sub> is given by
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>&sigma;</b><sub>1</sub> = <b>&Phi;</b><b>&sigma;</b><sub>0</sub><b>&Phi;</b><sup><i>-1</i></sup>
     *  <br>
     *  <br> 
     *  </p>
     *
     *  @param  matPhi      conjugating matrix <b>&Phi;</b> (typically a transfer matrix)
     *
     *  @return             matPhi*this*matPhi<sup>-1</sup>
     */
    public M conjugateInv(M matPhi) {  
        Matrix impPhi = ((BaseMatrix<M>)matPhi).getMatrix();
        Matrix impInv = impPhi.inverse();
        Matrix impAns = impPhi.times( this.getMatrix().times( impInv) );
        
        M   matAns = this.newInstance(impAns);
        
        return matAns;
    };
    

    /*
     * Child Class Support
     */

    /**
     * Constructor for SquareMatrix.
     * Creates a new, uninitialized instance of a square matrix with the given
     * matrix dimensions. The matrix contains all zeros.
     *
     * @param intSize   size of this square matrix
     * 
     * @throws UnsupportedOperationException  child class has not defined a public, zero-argument constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 14, 2013
     */
    protected SquareMatrix(final int intSize) throws UnsupportedOperationException {
        super(intSize, intSize);
        
        this.intSize = intSize;
    }

    /**
     * Copy constructor for <code>SquareMatrix</code>.  Creates a deep
     * copy of the given object.  The dimensions are set and the 
     * internal array is cloned. 
     *
     * @param matParent     the matrix to be cloned
     *
     * @throws UnsupportedOperationException  base class has not defined a public, zero-argument constructor
     *  
     * @author Christopher K. Allen
     * @since  Sep 25, 2013
     */
    protected SquareMatrix(M matParent) throws UnsupportedOperationException {
        super(matParent);
        
        this.intSize = matParent.getSize();
    }
    
    /**
     *  Parsing Constructor - creates an instance of the child class and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  intSize     the matrix size of this object
     *  @param  strTokens   token vector of getSize()^2 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    protected SquareMatrix(int intSize, String strTokens)    
        throws IllegalArgumentException, NumberFormatException
    {
        super(intSize, intSize, strTokens);
        
        this.intSize = intSize;
    }
    
    /**
     * <p>
     * Initializing constructor for bases class <code>SquareMatrix</code>.  
     * Sets the entire matrix to the values given in the Java primitive type 
     * double array. The argument itself remains unchanged. 
     * </p>
     * <p>
     * The dimensions of the given Java double array must be 
     * consistent with the size of the matrix.  Thus, if the arguments are
     * inconsistent, an exception is thrown.
     * </p>
     * 
     * @param cntRows     the matrix row size of this object
     * @param cntCols     the matrix column size of this object
     * @param arrMatrix   Java primitive array containing new matrix values
     * 
     * @exception  ArrayIndexOutOfBoundsException  the argument must have the same dimensions as this matrix
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    protected SquareMatrix(int intSize, double[][] arrVals) throws ArrayIndexOutOfBoundsException {
        super(intSize, intSize, arrVals);
        this.intSize = intSize;
    }

    /**
     * @see BaseMatrix#BaseMatrix(Matrix)
     * 
     * @throws IllegalArgumentException if provided matrix is not square.
     */
    protected SquareMatrix(Matrix mat) {
        super(mat);
        if (mat.getRowDimension() != mat.getColumnDimension()) {
        	throw new IllegalArgumentException("Provided matrix is not square!");
        }
        this.intSize = mat.getRowDimension();
    }
}
