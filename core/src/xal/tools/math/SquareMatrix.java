/**
 * BaseMatrix.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 25, 2013
 */
package xal.tools.math;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;

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
        return this.getMatrix().numCols;
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
    	return MatrixFeatures.isSymmetric(this.getMatrix(), 1e-6);
    }


    /*
     *  Matrix Operations
     */
    
    /**
     *  Matrix determinant function.
     *
     *  @return     the determinant of this square matrix
     */
    public double det() { 
    	return CommonOps.det(this.getMatrix());
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
     *
     * @author Christopher K. Allen
     * @since  Oct 11, 2013
     */
    public <V extends BaseVector<V>> V solve(final V vecObs) {
        
        DenseMatrix64F x = new DenseMatrix64F(vecObs.getSize(), 1);
        CommonOps.solve(this.getMatrix(), vecObs.getVector(), x);
        
        return vecObs.newInstanceNoCopy(x);
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
     * @deprecated It does not make sense to copy the result vector back to the provided vector.
     * 
     * @param vecObs        the data vector on call, the solution vector upon return
     * 
     *
     * @author Christopher K. Allen
     * @since  Oct 11, 2013
     */
    @Deprecated
    public <V extends BaseVector<V>> void solveInPlace(V vecObs) {
    	V result = this.solve(vecObs);
        vecObs.setVector(result);
    }

    /*
     *  Algebraic Operations
     */
    
    /**
     * Computes trace of the matrix.
     * @return Trace of the matrix.
     */
    public double trace() {
    	return CommonOps.trace(this.getMatrix());
    }

    
    /**
     * In-place matrix element by element multiplication.
     * @deprecated Misleading name, this is not real matrix multiplication.
     *
     * @param  matMult Matrix providing the factors.
     */
    @Deprecated
    public void timesEquals(BaseMatrix<M>   matMult) {
       CommonOps.elementMult(this.getMatrix(), matMult.getMatrix());
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
        DenseMatrix64F impPhi  = matPhi.getMatrix();
        DenseMatrix64F impPhiT = impPhi.copy();
        CommonOps.transpose(impPhiT);

        DenseMatrix64F impTemp  = new DenseMatrix64F(getSize(), getSize());
        CommonOps.mult(this.getMatrix(), impPhiT, impTemp);

        DenseMatrix64F impAns  = new DenseMatrix64F(getSize(), getSize());
        CommonOps.mult(impPhi, impTemp, impAns);
        
        M   matAns = this.newInstanceNoCopy(impAns);
        
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
        DenseMatrix64F impPhi  = matPhi.getMatrix();
        DenseMatrix64F impPhiI = impPhi.copy();
        CommonOps.invert(impPhiI);

        DenseMatrix64F impTemp  = new DenseMatrix64F(getSize(), getSize());
        CommonOps.mult(this.getMatrix(), impPhiI, impTemp);

        DenseMatrix64F impAns  = new DenseMatrix64F(getSize(), getSize());
        CommonOps.mult(impPhi, impTemp, impAns);
        
        M   matAns = this.newInstanceNoCopy(impAns);
        
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
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    protected SquareMatrix(int intSize, String strTokens)    
        throws NumberFormatException {
        super(intSize, intSize, strTokens);
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
    }

    /**
     * @see BaseMatrix#BaseMatrix(Matrix)
     */
    protected SquareMatrix(DenseMatrix64F mat) {
        super(mat);
        if (mat.getNumCols() != mat.getNumRows()) {
        	throw new IllegalArgumentException("Provided matrix is not square!");
        }
    }
}
