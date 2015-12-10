/**
 * 
 */
package xal.tools.math;

import org.ejml.data.DenseMatrix64F;

/**
 * Concrete implementation of Square matrix of arbitrary size
 * @author Blaz Kranjc
 */
public class GenericSquareMatrix extends SquareMatrix<GenericSquareMatrix> {

	/**
	 * @see SquareMatrix#SquareMatrix(int)
	 */
	public GenericSquareMatrix(int intSize) {
		super(intSize);
	}

	/**
	 * @see SquareMatrix#SquareMatrix(SquareMatrix)
	 */
	public GenericSquareMatrix(GenericSquareMatrix matParent) {
		super(matParent);
	}

	/**
	 * @see SquareMatrix#SquareMatrix(int, String)
	 */
	public GenericSquareMatrix(int intSize, String strTokens) throws NumberFormatException {
		super(intSize, strTokens);
	}

	/**
	 * @see SquareMatrix#SquareMatrix(int, double[][])
	 */
	public GenericSquareMatrix(int intSize, double[][] arrVals) throws ArrayIndexOutOfBoundsException {
		super(intSize, arrVals);
	}

	/**
	 * @see SquareMatrix#SquareMatrix(DenseMatrix64F)
	 */
	public GenericSquareMatrix(DenseMatrix64F mat) {
		super(mat);
	}

	/** 
	 * @see xal.tools.math.BaseMatrix#clone()
	 */
	@Override
	public GenericSquareMatrix clone() {
		return new GenericSquareMatrix(this);
	}

	/**
	 * @see xal.tools.math.BaseMatrix#newInstance(int, int)
	 */
	@Override
	protected GenericSquareMatrix newInstance(int row, int col) {
		if (row != col) {
			throw new IllegalArgumentException("Cannot create nonsquare matrix as square matrix.");
		}
		return new GenericSquareMatrix(row);
	}

}
