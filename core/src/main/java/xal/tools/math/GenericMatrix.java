package xal.tools.math;

/**
 * Concrete implementation of a BaseMatrix class for general matrices.
 * 
 * @author Blaz Kranjc
 */
public class GenericMatrix extends BaseMatrix<GenericMatrix> {

	/**
	 * Create a copy of provided matrix as a square matrix.
	 * @param m Generic matrix to be transformed to a square matrix 
	 */
	public static GenericSquareMatrix createSquare(BaseMatrix<?> m) {
		return new GenericSquareMatrix(m.getMatrix());
	}


	/**
	 * Instance a zero matrix of provided size.
	 * @see BaseMatrix#BaseMatrix(int, int)
	 * 
	 * @param cntRows Number of rows.
	 * @param cntCols Number of columns.
	 */
	public GenericMatrix(int cntRows, int cntCols) {
		super(cntRows, cntCols);
	}
	

	/**
	 * Instance a matrix with elements provided in an array.
	 * @see BaseMatrix#BaseMatrix(int, int, double[][])
	 * 
	 * @param arr Array containing the elements of matrix.
	 */
	public GenericMatrix(double[][] arr) {
		super(arr);
	}

	/**
	 * Deep copy of any matrix to GenericMatrix.
	 * 
	 * @param mat Matrix object.
	 */
	public <M extends BaseMatrix<M>> GenericMatrix(BaseMatrix<M> mat) {
		super(mat.getMatrix());
	}


	/**
	 * @see BaseMatrix#clone()
	 */
	@Override
	public GenericMatrix clone() {
		return new GenericMatrix(this);
	}

	/**
	 * @see BaseMatrix#newInstance()
	 */
	@Override
	protected GenericMatrix newInstance(int row, int cnt) {
		return new GenericMatrix(row, cnt);
	}

}