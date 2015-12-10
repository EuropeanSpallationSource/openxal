package xal.tools.math;

/**
 * Concrete implementation of vector with arbitrary length.
 * 
 * @author Blaz Kranjc
 */
public class GenericVector extends BaseVector<GenericVector> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @see BaseVector#BaseVector(BaseVector)
	 */
	public GenericVector(GenericVector vecParent) throws UnsupportedOperationException {
		super(vecParent);
	}

	/**
	 * @see BaseVector#BaseVector(int, double[])
	 */
	public GenericVector(int intSize, double[] arrVals) throws ArrayIndexOutOfBoundsException {
		super(intSize, arrVals);
	}

	/**
	 * Creates an instance with specified size. Used only by parent methods.
	 * @param size Size of the vector.
	 */
	private GenericVector(int size) {
		super(size);
	}

	/**
	 * @see GenericVector#clone()
	 */
	@Override
	public GenericVector clone() {
		return new GenericVector(this);
	}

	/**
	 * @see GenericVector#newInstance()
	 */
	@Override
	protected GenericVector newInstance(int size) {
		return new GenericVector(size);
	}
	
}
