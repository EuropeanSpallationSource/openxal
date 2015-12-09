package xal.tools.math;

/**
 * Concrete implementation of a BaseMatrix class for general matrices.
 * 
 * @author Blaz Kranjc
 */
public class GenericMatrix extends BaseMatrix<GenericMatrix> {

	/**
	 * Creates a matrix instance with size 1x1. Used only by parent methods.
	 */
	private GenericMatrix() {
		super(1, 1); // TODO: this breaks the library a bit as the sizes are not the same!
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
		super(arr.length, arr[0].length, arr);
	}


	/**
	 * Instance a matrix with elements provided in an array.
	 * @see BaseMatrix#BaseMatrix(int, int, double[][])
	 * 
	 * @param cntRows Number of rows.
	 * @param cntCols Number of columns.
	 * @param arr Array containing the elements of matrix.
	 */
	public GenericMatrix(int cntRows, int cntCols, double[][] arr) {
		super(cntRows, cntCols, arr);
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
	protected GenericMatrix newInstance() {
		return new GenericMatrix();
	}
	
}