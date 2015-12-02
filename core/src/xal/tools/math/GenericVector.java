package xal.tools.math;

/**
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
	 * Creates an instance with size 1. Used only by parent methods.
	 */
	private GenericVector() {
		super(1);
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
	protected GenericVector newInstance() {
		return new GenericVector();
	}
	
}
