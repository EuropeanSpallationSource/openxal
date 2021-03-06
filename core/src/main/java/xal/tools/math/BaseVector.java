/*
 * Vector.java
 *
 * Created on August 15, 2002, 11:02 AM
 */

package xal.tools.math;

import java.io.PrintWriter;
import java.util.StringTokenizer;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;


/**
 * <p>
 * Class <code>Vector</code> is the abstract base class for matrix
 * objects supported in the XAL tools packages.
 * </p>
 * </p>
 *  The current implementation uses an <i>n</i>&times;1 EJML matrix to
 *  represent the underlying vector. That is, the internal representation
 *  is a column vector.
 *  </p>
 *
 * @author  Christopher K. Allen
 * @author  Blaz Kranjc
 * @since   Aug, 2002
 * @version Oct, 2013
 */

public abstract class BaseVector<V extends BaseVector<V>> implements IArchive, java.io.Serializable {
    
    
    /*
     * Global Constants
     */
    
    /** ID for serialization version */
    private static final long serialVersionUID = 1L;
    
    
    /** attribute marker for data */
    public static final String     ATTR_DATA   = "values";
    
    
    /*
     *  Local Attributes
     */

    /** internal matrix implementation */
    private DenseMatrix64F vecImpl;

    /*
     * Assignment
     */
    
    /**
     * Sets the entire vector to the values given in the Java primitive type 
     * double array.
     * 
     * <h4>NOTE!</h4>
     * TODO This is not going to work for homogeneous coordinates!
     * <br/>
     * <br/>
     * 
     * @param arrVector Java primitive array containing new vector values
     * 
     * @exception  IllegalArgumentException  the argument must have the same dimensions as this matrix
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    public void setVector(double[] arrVector) throws IllegalArgumentException {
        // Check the dimensions of the argument double array
        if (this.getSize() != arrVector.length  )
            throw new IllegalArgumentException(
                    "Dimensions of argument do not correspond to size of this vector = " 
                   + this.getSize()
                   );
        
        // Set the elements of this array to that given by the corresponding 
        //  argument entries
        //  -- I think memory access is cheaper than...
        for (int i=0; i<this.getSize(); i++) {
            double dblVal = arrVector[i];

            this.setElem(i, dblVal);
        }
        
        //  -- memory creation
//        JVector vecNew = new JVector(arrVector);
//        this.vecImpl   = vecNew;
    }
    
    /**
     * Sets the entire vector to the values given to the value of the new 
     * vector.
     * 
     * @param arrVector Java primitive array containing new vector values
     * 
     * @exception  ArrayIndexOutOfBoundsException  the argument must have the same dimensions as this matrix
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    public void setVector(V vecParent) {
        DenseMatrix64F impParent = vecParent.getVector();
        this.assignVector(impParent);
    }

    /**
     *  Parsing assignment - set the vector value
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (aka FORTRAN).
     *
     *  <h4>NOTE!</h4> 
     *  TODO This is not going to work for homogeneous coordinates!
     *  <br/>
     *  <br/>
     * 
     *  @param  strValues   token vector of SIZE<sup>2</sup> numeric values
     *
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    public void setVector(String strValues)
        throws NumberFormatException {

        // Error check the number of token strings
        StringTokenizer     tokArgs = new StringTokenizer(strValues, " ,()[]{}");
        
        if (tokArgs.countTokens() != this.getSize())
            throw new IllegalArgumentException("Vector, wrong number of token in string initializer: " + strValues);
        
        
        // Extract initial phase coordinate values
        for (int i=0; i<this.getSize(); i++) {
            String  strVal = tokArgs.nextToken();
            double  dblVal = Double.valueOf(strVal).doubleValue();

            this.setElem(i,dblVal);
        }
    }

    /** 
     * Set individual element of a vector to given value
     * 
     *  @param  intIndex  index of element
     *  @param  dblVal  new value of element
     *
     *  @exception  ArrayIndexOutOfBoundsException  iIndex is larger than the vector
     */
    public void setElem(int intIndex, double dblVal) throws ArrayIndexOutOfBoundsException {
        this.vecImpl.set(intIndex, 0, dblVal);
    };
    
    /** 
     * Set individual element of a vector to given value.  The index is assumed
     * to be an enumeration exposing the <code>IIndex</code> interface.  That interface
     * interface belongs to the <code>BaseMatrix<M></code> namespace.  In this manner
     * matrix indices can be used to set vector component values.
     * 
     *  @param  iIndex  index of element taken from the interface <code>IIndex</code> of class <code>BaseMatrix</code>
     *  @param  dblVal  new value of element
     *
     *  @exception  ArrayIndexOutOfBoundsException  iIndex is larger than the vector
     */
    public void setElem(IIndex iIndex, double dblVal) throws ArrayIndexOutOfBoundsException {
        this.setElem(iIndex.val(), dblVal);
    };
    
   
    
    /*
     * Vector properties
     */
    
    /**
     *  Get size of Vector (number of elements)
     *  
     *  @return     vector length
     */
    public int getSize()    {
        return this.vecImpl.numRows;
    };
    
    /** 
     * Get individual element of a vector at specified index
     * 
     *  @param  iIndex  data source providing index of element 
     *  
     *  @return         value of element at given index
     *  
     *  @exception  ArrayIndexOutOfBoundsException  iIndex is larger than vector size
     */
    public double getElem(int iIndex) throws ArrayIndexOutOfBoundsException {
        return this.vecImpl.get(iIndex, 0);
    };
    

    /** 
     * <p>
     * Returns the vector component at the position indicated by the
     * given index in the <code>IIndex</code> interface.  That 
     * interface belongs to the <code>BaseMatrix<M></code> namespace.  
     * In this way matrix indices can be used to get vector component values.
     * </p>
     * <p>
     * <h3>NOTES</h3>
     * &middot; It is expected that the
     * object exposing the <code>IIndex</code> interface is an enumeration
     * class restricting the number of possible index values.
     * <br/>
     * &middot; Consequently we do not declare a thrown exception assuming
     * that that enumeration class eliminates the possibility of an out of
     * bounds error.
     * </p>
     *  
     * @param iIndex    source containing the vector index
     * 
     * @return          value of the matrix element at the given row and column
     *
     *
     * @exception  ArrayIndexOutOfBoundsException  iIndex is larger than the vector
     */
    public double getElem(IIndex iIndex) throws ArrayIndexOutOfBoundsException {
        return this.getElem( iIndex.val() );
    };
    
    /**
     * Returns a copy of the internal Java array containing
     * the vector elements.  The array dimensions are given by
     * the size of this matrix, available from 
     * <code>{@link #getSize()}</code>.  The returned array is
     * a copy of this vector thus manipulation with not affect
     * the parent object.  
     * 
     * @return  copied array of vector values
     *
     * @author Christopher K. Allen
     * @since  Sep 25, 2013
     */
    public double[] getArrayCopy() {
    	double[] copy = new double[this.getSize()];
    	System.arraycopy(this.vecImpl.data, 0, copy, 0, this.getSize());
        return copy;
    }

    
    /*
     *  Object method overrides
     */
    
    /**
     * Base classes must override the clone operation in order to 
     * make deep copies of the current object.  This operation cannot
     * be done without the exact type.
     *
     * @see java.lang.Object#clone()
     *
     * @author Jonathan M. Freed
     * @since  Jul 3, 2014
     */
    @Override
    public abstract V   clone();

    /**
     *  Convert the contents of the matrix to a string representation.
     *  The format is similar to that of Mathematica. Specifically,
     *  <br/>
     *  <br/>
     *      { a b c d }
     *  <br/>
     *
     *  @return     string representation of the matrix
     */
    @Override
    public String   toString()  {
        // double is 15 significant digits plus the spaces and brackets
        final int size = (this.getSize()*this.getSize() * 16) + (this.getSize()*2) + 4; 
        StringBuffer strBuf = new StringBuffer(size);

        synchronized(strBuf) { // get lock once instead of once per append
            strBuf.append("{ ");
            for (int i=0; i<this.getSize(); i++) {
                    strBuf.append(this.getElem(i));
                    strBuf.append(" ");
            }
            strBuf.append(" }");
        }

        return strBuf.toString();
    }

    /**
     * "Borrowed" implementation from AffineTransform, since it is based on
     * double attribute values.  Must implement hashCode to be consistent with
     * equals as specified by contract of hashCode in <code>Object</code>.
     * 
     * @return a hashCode for this object
     */
    @Override
    public int hashCode() {
        long bits = 0;
        for (int i=0; i<this.getSize(); i++) {
                bits = bits * 31 + Double.doubleToLongBits(getElem(i) );
        }

        return (((int) bits) ^ ((int) (bits >> 32)));
    }           

    


    /*
     * Vector Operations
     */
    
    /**
     *  Perform a deep copy of this Vector object and return it.
     *  
     *  @return     a cloned copy of this vector
     */
    public V copyVector() {
        V  vecClone = this.newInstance(this.getSize());
        ((BaseVector<V>)vecClone).assignVector( this.vecImpl );
            
        return vecClone;
    };

    /**
     *  Assign this vector to be the zero vector, specifically
     *  the vector containing all 0's. 
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2013
     */
    public void assignZero() {
    	CommonOps.fill(this.vecImpl, 0.0);
    }
    
    /**
     * Assign this matrix to be the unity vector,  the
     * with all 1's.
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2013
     */
    public void assignUnity() {
    	CommonOps.fill(this.vecImpl, 1.0);
    }
    
    /**
     * <p>
     * Projects this vector onto the smaller subspace represented by
     * the given vector.  For example, say this vector <b>v</b> is an element
     * of <b>R</b><sup><i>n</i></sup> and the given vector <b>u</b> is
     * an element of <b>R</b><sup><i>m</i></sup> where 
     * <i>m</i> &le; <i>n</i>. Then <b>v</b> decomposes as 
     * <b>v</b> = (<b>v</b><sub>1</sub> <b>v</b><sub>2</sub>) &in; 
     * <b>R</b><sup><i>m</i></sup> &times; <b>R</b><sup><i>n-m</i></sup>.
     * That component <b>v</b><sub>1</sub> that lives in the subspace
     * <b>R</b><sup><i>m</i></sup> is projected onto the given vector.
     * </p>
     * <p>
     * To make it simple, the first <i>m</i> components of this vector are
     * used to set all the values of the given vector, in respective order.
     * If the give vector is larger than this vector an exception is thrown.
     * </p>
     * 
     * @param vecSub    The vector to receive the projection of this vector (determines size)
     * 
     *
     * @author Christopher K. Allen
     * @since  Oct 18, 2013
     */
    public <U extends BaseVector<U>> void projectOnto(U vecSub) {
        
        // Check size of sub-space vector
        if (vecSub.getSize() > this.getSize())
            throw new IllegalArgumentException("Cannot project this vector onto the larger vector " + vecSub);
        
        for (int i=0; i<vecSub.getSize(); i++) {
            double  dblVal = this.getElem(i);
            
            vecSub.setElem(i, dblVal);
        }
    }
    
    /**
     * <p>
     * Embeds this vector into the larger super-space represented by
     * the given vector.  For example, say this vector <b>v</b> is an element
     * of <b>R</b><sup><i>m</i></sup> and the given vector <b>u</b> is
     * an element of <b>R</b><sup><i>n</i></sup> where 
     * <i>m</i> &le; <i>n</i>. Then <b>u</b> decomposes as 
     * <b>u</b> = (<b>u</b><sub>1</sub> <b>u</b><sub>2</sub>) &in; 
     * <b>R</b><sup><i>m</i></sup> &times; <b>R</b><sup><i>n-m</i></sup>.
     * This vector <b>v</b> is embedded as that component <b>u</b><sub>1</sub> 
     * that lives in the sub-space
     * <b>R</b><sup><i>m</i></sup> &sub;<b>R</b><sup><i>m</i></sup> &times; <b>R</b><sup><i>n-m</i></sup>.
     * </p>
     * <p>
     * To make it simple, the first <i>m</i> components of the given vector are
     * set to the components of this vector, in respective order.
     * If the give vector is smaller than this vector an exception is thrown.
     * </p>
     * 
     * @param vecSup    The vector to receive the embedding of this vector 
     * 
     *
     * @author Christopher K. Allen
     * @since  Oct 18, 2013
     */
    public <U extends BaseVector<U>> void embedIn(U vecSup) {
        
        // Check the size of the super-space vector
        if ( vecSup.getSize() < this.getSize() ) 
            throw new IllegalArgumentException("Cannot embed this vector into a smaller vector " + vecSup);
        
        for (int i=0; i<this.getSize(); i++) {
            double dblVal = this.getElem(i);
            
            vecSup.setElem(i, dblVal);
        }
    }

    /**
     * Checks if the given vector is algebraically equivalent to this
     * vector.  That is, it is equal in size and element values.
     * 
     * @param vecTest   vector under equivalency test
     * 
     * @return          <code>true</code> if the argument is equivalent to this vector,
     *                  <code>false</code> if otherwise
     *
     * @author Christopher K. Allen
     * @since  Oct 1, 2013
     */
    public boolean isEquivalentTo(V vecTest) {
        if ( !this.getClass().equals(vecTest.getClass()) )
            return false;
        return MatrixFeatures.isEquals(vecTest.getVector(), this.vecImpl, 1e-6);
    }


    /*
     * Algebraic Operations
     */
    
    /** 
     * Element by element negation.  A new object is returned and the
     * current one is unmodified.
     * 
     * @return     antipodal vector of the current object
     * 
     * @author Christopher K. Allen
     * @since  Oct 10, 2013
     */
    public V negate() {
        return this.times(-1.);
    }
    
    /**
     * In place element-by-element negation of this vector.
     *
     * @author Christopher K. Allen
     * @since  Oct 10, 2013
     */
    public void negateEquals() {
    	CommonOps.changeSign(this.vecImpl);
    }
    
    /**
     *  Vector in-place addition. Add the given vector to this vector which
     *  then takes on the summed value.
     *
     *  @param  vecAdd     Vector to add to this vector (addend)
     *
     */
    public void plusEquals(V vecAdd) {
    	CommonOps.addEquals(this.vecImpl, vecAdd.getVector());
    };
    
    /**
     *  Vector addition without destruction
     *
     *  @param  vecAdd     vector added to this one (addend)
     *  
     *  @return            sum of this vector and given vector, 
     *
     */
    public V plus(V vecAdd){
    	V result = newInstance(this.getSize());
        CommonOps.add(this.vecImpl, vecAdd.getVector(), result.getVector());

        return result;
    };
    
    /**
     *  Vector in-place subtraction. Subtracts the given vector from this vector which
     *  then takes the new value.
     *
     *  @param  vecSub     Vector to subtract from this vector (subtrahend)
     *
     */
    public void minusEquals(V vecSub) {
    	CommonOps.subtractEquals(this.vecImpl, vecSub.getVector());
    };
    
    /**
     *  Vector subtraction without destruction
     *
     *  @param  vecSub     vector subtracted from this one (subtrahend)
     *  
     *  @return            difference of this vector and the given vector, 
     *
     */
    public V minus(V vecSub) {
    	V result = newInstance(this.getSize());
        CommonOps.subtract(this.vecImpl, vecSub.getVector(), result.getVector());

        return result;
    }
    
    /** 
     *  Scalar multiplication
     *
     *  @param  s   scalar value
     *
     *  @return     result of scalar multiplication
     */
    public V times(double s)   {
    	V result = newInstance(this.getSize());
        CommonOps.scale(s, this.vecImpl, result.getVector());

        return result;
    }
    

    /** 
     *  In place scalar multiplication
     *  
     *  @param  s   scalar
     */
    public void timesEquals(double s)   {
    	CommonOps.scale(s, this.vecImpl);
    };
    

    /**
     *  Vector inner product.
     *
     *  Computes the inner product of of this vector with the given
     *  vector.
     *  
     *  @param  v     second vector
     *
     *  @return         inner product of this vector and argument
     */
    public double   innerProd(V v) {
        return CommonOps.dot(this.vecImpl, v.getVector());
    }
    
    /** 
     *  Vector left multiplication, or covariant operation of matrix
     *  on this vector (post-multiply vector by matrix).
     *
     *  @param  mat     matrix operator
     *
     *  @return         result of vector-matrix product
     *
     */
    public <M extends SquareMatrix<M>> V leftMultiply(M mat) {
        
        // Check sizes
        if (this.getSize() != mat.getSize())
            throw new IllegalArgumentException("matrix and vector must be of compatible dimensions");
        
        // Perform covariant multiplication
        V result = newInstance(this.getSize());
        for (int j=0; j<this.getSize(); j++) {
            
            double  dblSum = 0.0;
            for (int i=0; i<this.getSize(); i++) {
                
                dblSum += mat.getElem(i, j)*this.getElem(i);
            }
            
            result.getVector().set(j, dblSum);
        }
        
        return result;
    };
    
    /** 
     *  Vector right multiplication, or contra-variant operation of the
     *  matrix on this vector (pre-multiply vector by matrix).
     *
     *  @param  mat     matrix operator
     *
     *  @return         result of vector-matrix product
     *
     */
    public <M extends SquareMatrix<M>> V rightMultiply(M mat) {
        // Check sizes
        if (this.getSize() != mat.getSize())
            throw new IllegalArgumentException("matrix and vector must be of compatible dimensions");
        
        // Perform contra-variant multiplication    
        V result = newInstance(this.getSize());
        for (int i=0; i<this.getSize(); i++) {
            
            double  dblSum = 0.0;
            for (int j=0; j<this.getSize(); j++) {
                
                dblSum += mat.getElem(i, j)*this.getElem(i);
            }
            
            result.getVector().set(i, dblSum);
        }
        
        return result;
    };
    
    
    /*
     *  Topological Operations
     */
    
    /**
     *  Return the <i>l</i><sub>1</sub> norm of the vector.
     *
     *  @return     ||z||<sub>1</sub> = &Sigma;<sub><i>i</i></sub> |<i>z<sub>i</sub></i>|
     */
    public double   norm1()     { 
        int         i;          // loop control
        double      dblSum;     // running sum
        
        dblSum = 0.0;
        for (i=0; i<getSize(); i++) 
            dblSum += Math.abs( this.getElem(i) );
        
        return dblSum;
    };
    
    /**
     *  Return the <i>l</i><sub>2</sub> norm of the vector.
     *
     *  @return     ||z||<sub>2</sub> = [ &Sigma;<sub><i>i</i></sub> <i>z<sub>i</sub></i><sup>2</sup> ]<sup>1/2</sup>
     */
    public double   norm2()     { 
        int         i;          // loop control
        double      dblSum;     // running sum
        
        dblSum = 0.0;
        for (i=0; i<this.getSize(); i++) 
            dblSum += this.getElem(i)*this.getElem(i);
        
        return dblSum;
    }
    
    /**
     *  Return the <i>l</i><sub>&infin; norm of the vector.
     *
     *  @return     ||<i>z</i>||<sub>&infin;</sub> = sup<sub><i>i</i></sub> |<i>z<sub>i</sub></i>|
     */
    public double   normInf()     { 
        int         i;          // loop control
        double      dblMax;     // running maximum
        
        dblMax = 0.0;
        for (i=0; i<this.getSize(); i++) 
            if (Math.abs( this.getElem(i) ) > dblMax ) 
                dblMax = Math.abs( this.getElem(i) );
        
        return dblMax;
    }
    
    
    
    
    
    /*
     * IArchive Interface
     */    

    /**
     * Save the value of this vector to disk.
     * 
     * @param daptArchive   interface to data sink 
     * 
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    public void save(DataAdaptor daptArchive) {
        daptArchive.setValue(ATTR_DATA, this.toString());
    }

    /**
     * Restore the value of the this vector from the
     * contents of a data archive.
     * 
     * @param daptArchive   interface to data source
     * 
     * @throws DataFormatException      malformed data
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    public void load(DataAdaptor daptArchive) throws DataFormatException {
        if ( daptArchive.hasAttribute(ATTR_DATA) )  {
            String  strValues = daptArchive.stringValue(ATTR_DATA);
            this.setVector(strValues);         
        }
    }
    
    

    /*
     * Debugging
     */

    /**
     * Print the vector contents to standard output.
     */
    public void print() {
        System.out.print( this.toString() );
    }

    
    /**
     *  Print the vector contents to an output stream,
     *  does not add new line.
     *
     *  @param  os      output stream object 
     */
    public void print(PrintWriter os)   {

        // Create vector string
        int     indLast = this.getSize() - 1;
        String  strVec = "(";

        for (int i=0; i<indLast; i++)
            strVec = strVec + this.getElem(i) + ",";
        strVec = strVec + this.getElem(indLast) + ")";

        // Send to output stream
        os.print(strVec);
    };
            
    /**
     *  Print the vector contents to an output stream, 
     *  add new line character.
     *
     *  @param  os      output stream object 
     */
    public void println(PrintWriter os)   {

        // Create vector string
        int     indLast = this.getSize() - 1;
        String  strVec = "(";

        for (int i=0; i<indLast; i++)
            strVec = strVec + this.getElem(i) + ",";
        strVec = strVec + this.getElem(indLast) + ")";

        // Send to output stream
        os.println(strVec);
    };
    
    
    /*
     * Child Class Support
     */
    
    /** 
     * Creates a new, uninitialized instance of a vector with the given
     * size. The vector contains all zeros.
     *  
     * @param  intSize     the vector size of this object
     *  
     * @throws UnsupportedOperationException  base class has not defined a public, zero-argument constructor
     */
    protected BaseVector(int intSize) {
    	this.vecImpl = new DenseMatrix64F(intSize, 1);
    }

    /**
     * Copy constructor for <code>Vector</code>.  Creates a deep
     * copy of the given object.  The dimensions are set and the 
     * internal array is cloned. 
     *
     * @param vecParent     the vector to be cloned
     *
     * @throws UnsupportedOperationException  base class has not defined a public, zero-argument constructor
     *  
     * @author Christopher K. Allen
     * @since  Sep 25, 2013
     */
    protected BaseVector(V vecParent) throws UnsupportedOperationException {
        this.assignVector(vecParent.getVector());
    }
    
    /**
     *  <p>
     *  Parsing Constructor - creates an instance of the child class and initialize it
     *  according to a token string of element values.
     *  </p>  
     *  <p>
     *  The token string argument is assumed to be one-dimensional and delimited
     *  by any of the characters <tt>" ,()[]{}"</tt>  Repeated, contiguous delimiters 
     *  are parsed together.  This conditions allows a variety of parseable string
     *  representations. For example,
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; { 1, 2, 3, 4 }
     *  <br/>
     *  <br/>
     *  and
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; [1 2 3 4]
     *  <br/>
     *  <br/>
     *  would parse to the same homogeneous vector (1, 2, 3, 4 | 1).
     *  </p>
     *
     *  @param  intSize     the matrix size of this object
     *  @param  strTokens   token vector of getSize() numeric values
     *
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    protected BaseVector(int intSize, String strTokens)    
        throws NumberFormatException {
        this(intSize);
        
        // Error check the number of token strings
        StringTokenizer     tokArgs = new StringTokenizer(strTokens, " ,()[]{}");
        
        if (tokArgs.countTokens() != this.getSize())
            throw new IllegalArgumentException("Vector, wrong number of token in string initializer: " + strTokens);
        
        
        // Extract initial phase coordinate values
        for (int i=0; i<this.getSize(); i++) {
            String  strVal = tokArgs.nextToken();
            double  dblVal = Double.valueOf(strVal).doubleValue();

            this.setElem(i,dblVal);
        }
    }
    
    /**
     * <p>
     * Initializing constructor for bases class <code>BaseVector</code>.  
     * Sets the entire matrix to the values given in the Java primitive type 
     * double array. The argument itself remains unchanged. 
     * </p>
     * <p>
     * The dimensions of the new vector will be the length of the given Java double array. 
     * </p>
     * 
     * @param arrMatrix   Java primitive array containing new vector values
     * 
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    protected BaseVector(double[] arrVals) {
//        this(intSize);
//        this.setVector(arrVals);;
        this(arrVals.length);
        this.setVector(arrVals);;
    }
    
    /**
     * Initializing constructor for <code>BaseVector</code>.  The vector values
     * are taken from the data source provided.
     *
     * @param intSize       size of this vector
     * @param daSource      data source containing the initial values of this vector
     *
     * @author Christopher K. Allen
     * @since  Nov 5, 2013
     */
    protected BaseVector(int intSize, DataAdaptor daSource) {
        this(intSize);
        this.load(daSource);
    }

    
    /*
     * Internal Support
     */

    /**
     *  Return the internal matrix representation.
     *
     *  @return     the internal implementation matrix object
     */
    protected DenseMatrix64F getVector()   { 
        return this.vecImpl; 
    }
    
    /**
     * Sets the internal matrix implementation to that given in the argument. This
     * is a deep copy operation.  No references are passed.  The assignment is 
     * made by cloning the given vector and assigning to the internal vector
     * representation encapsulated by this class.
     * 
     * @param vecValue  internal implementation of matrix values
     *
     * @author Christopher K. Allen
     * @since  Oct 1, 2013
     */
    private void assignVector(DenseMatrix64F vecValue) {
    	if (vecValue.numCols > 1) {
    		throw new IllegalArgumentException("Provided object has more than one column.");
    	}
    	this.vecImpl = vecValue.copy();
    }

    /**
     * Creates a new, uninitialized instance of this vector type.
     * 
     * @return  uninitialized vector object of type <code>V</code>
     * @author Christopher K. Allen
     * @since  Oct 1, 2013
     */
    protected abstract V newInstance(int size);

    
    /**
     * Creates a new instance of this vector type with the given Java
     * array as the internal representation.
     * 
     * @param arrVecInt     new vector's guts
     * 
     * @return              new instance of this vector type with the internal representation
     *
     * @since  Jul 24, 2015   by Christopher K. Allen
     */
    protected abstract V newInstance(double[] arrVecInt);
    
    /**
     * Creates a new instance of this vector type initialized to the given
     * implementation matrix.
     * 
     * @param   vecInit implementation vector containing initialization values    
     * 
     * @return          initialized vector object of type <code>V</code>
     * @author Christopher K. Allen
     * @since  Oct 1, 2013
     */
    protected V newInstance(DenseMatrix64F vecInit) {
        V vecNewInst = this.newInstance(vecInit.numRows);
        ((BaseVector<V>)vecNewInst).assignVector(vecInit);
        return vecNewInst;
    }

}
