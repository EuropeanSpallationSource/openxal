/**
 * FourierExpTransform.java
 *
 * Created      : August, 2007
 * Author       : Christopher K. Allen
 */
package xal.tools.dsp;

import JSci.maths.Complex;
import JSci.maths.matrices.ComplexSquareMatrix;
import JSci.maths.vectors.AbstractComplexVector;
import JSci.maths.vectors.ComplexVector;

/**
 * <p>
 * Class embodying the classic Discrete Fourier Transform (DFT). Although this
 * is a discrete transform, it is not a Fast Fourier Transform (FFT).
 * Specifically, we do not exploit the dyadic nature of the transform kernel. In
 * fact, we explicitly compute both the forward and inverse kernels which, in
 * the discrete version, are matrices. Thus, the transform can potentially be
 * expensive for large signal sizes (of order <em>O</em>(<em>N</em>&sup2;)). The
 * advantage here is that we may consider arbitrary signal sizes <em>N</em> > 0.
 * That is,
 * <em>N</em> does not need to be a power of 2 as with the FFT, requiring the
 * given signal to be padded accordingly.
 * </p>
 * <p>
 * The transform performed here is given by
 * <br>
 * <br>&nbsp;&nbsp; [<strong>f^</strong>] =
 * [<strong>K</strong>]&middot;[<strong>f</strong>]<br>
 * <br>
 * where [<strong>f^</strong>] is the complex vector of DFT data,
 * [<strong>K</strong>] is the complex symmetric matrix kernel, and
 * [<strong>f</strong>] is the real vector (e.i., type <code>double[]</code>) of
 * input function values. The elements
 * <em>K<sub>mn</sub></em> of the matrix kernel are given by
 * <br>
 * <br>&nbsp;&nbsp;    <em>K<sub>mn</sub></em> =
 * <em>z<sup>-mn</sup></em>/<em>N</em><sup>&frac12;</sup> <br>
 * <br>
 * where <em>N</em> is the size of the data vector [<strong>f</strong>], indices
 * <em>m, n</em>
 * range over the values 0,&hellip;,<em>N</em>-1, and <em>z</em> is the
 * generator of the transform kernel given by
 * <br>
 * <br>&nbsp;&nbsp;  <em>z</em> &equiv;
 * <em>e<sup>i</em>2<em>&pi;</em>/<em>N</em></sup>
 * <br>
 * <br>
 * The factor 1/<em>N</em><sup>&frac12;</sup> is a normalization constant;
 * specifically, the value of the <em>L</em><sub>2</sub> norm
 * ||<em>e<sup>i2&pi;n</em>/<em>N</em></sup>||.
 * </p>
 * <p>
 * The inverse transform (back to the "time" domain) is given by
 * <br>
 * <br>&nbsp;&nbsp; [<strong>f</strong>] =
 * [<strong>K</strong><sup>-1</sup>]&middot;[<strong>f^</strong>]<br>
 * <br>
 * where the elements <em>K<sub>mn</sub></em><sup>-1</sup> of the kernel are
 * given by
 * <br>
 * <br>&nbsp;&nbsp;    <em>K<sub>mn</sub></em><sup>-1</sup> =
 * <em>z<sup>mn</sup></em>/<em>N</em><sup>&frac12;</sup> <br>
 * <br>
 * Clearly [<strong>K</strong><sup>-1</sup>]&middot;[<strong>K</strong>] =
 * [<strong>I</strong>] where [<strong>I</strong>] is the
 * <em>N</em>&times;<em>N</em> identity matrix.
 * <p>
 * From the value of <em>K<sub>mn</sub></em> and <em>z</em> it can be inferred
 * that the stride in [<strong>f^</strong>] is 1/<em>T</em>, where
 * <em>T</em> is the length of the time interval over which <em>f</em> is taken.
 * Because the DFT considers both positive and negative frequency components,
 * the largest frequency we can see is &frac12;<em>N</em>/<em>T</em>,
 * corresponding to the discrete frequency <em>N</em>/2. Referring to the
 * definition of
 * <em>z</em>, the positive (discrete) frequency components cover the indices
 * <em>n</em> = 0,&hellip;,<em>floor</em>(N/2) while the negative frequency
 * components are located at the indices <em>n</em> =
 * <em>floor</em>(<em>N</em>/2)+1,&hellip;,<em>N</em>-1 (in reverse order).
 * Topologically, the positive frequencies {<em>n</em>} occur for
 * <em>z<sup>n</sup></em> on the top half-plane and the negative frequencies
 * {<em>n</em>} occur for <em>z<sup>n</sup></em> on the bottom half-plane.
 * </p>
 *
 * @author Christopher K. Allen
 *
 */
public class FourierExpTransform {

    /*
     * Local Attributes
     */
    /**
     * the expected data size
     */
    private int szData = 0;

    /**
     * generator of transform kernel
     */
    private Complex cpxZ = null;

    /**
     * the forward transform kernel
     */
    private ComplexSquareMatrix matKerFwd = null;

    /**
     * the inverse transform kernel
     */
    private ComplexSquareMatrix matKerInv = null;

    /*
     * Initialization
     */
    /**
     * Create a new sine transform object for transforming data vectors of size
     * <var>szData</var>. During construction the
     * <var>szData</var>&times;<var>szData</var> transform matrix kernel is
     * built. Upon completion the returned transform object is able to transform
     * any <code>double[]</code> object of the appropriate size.
     *
     * @param szData
     *
     * @see FourierSineTransform#transform(double[])
     */
    public FourierExpTransform(int szData) {
        this.initTransform(szData);
    }

    /*
     * Attribute Query
     */
    /**
     * Return the expected size of the data, which is also the dimensions of the
     * kernel.
     *
     * @return size of the transform vectors (that is, the value <em>N</em>)
     */
    public int getDataSize() {
        return this.szData;
    }

    /**
     * <p>
     * Return the exponential transform generator. All the inverse transform
     * kernel elements are multiples of the this value while all forward
     * transform kernel elements are multiples of the inverse of this value.
     * </p>
     * <p>
     * Note that <em>z</em> lies on the unit circle of the complex plane.
     * </p>
     *
     * @return the transform generator
     */
    public Complex getKernelGenerator() {
        return this.cpxZ;
    }

    /*
     * Operations
     */
    /**
     * Compute and return the value of the frequency stride for this transform
     * given the total time period over which the data is taken.
     *
     * @param dblPeriod total length of the data window
     *
     * @return frequency interval (stride) between transformed data points
     */
    public double compFreqStrideFromPeriod(double dblPeriod) {
        return 1.0 / dblPeriod;
    }

    /**
     * Compute and return the value of the frequency stride for this transform
     * given the time stride (time interval between data points).
     *
     * @param dblDelta time interval between data points
     *
     * @return frequency interval (stride) between transformed data points
     */
    public double compFreqStrideFromInterval(double dblDelta) {
        int N = this.getDataSize();
        double T = dblDelta * N;

        return this.compFreqStrideFromPeriod(T);
    }

    /**
     * <p>
     * Compute and return the Fourier exponential transform of the given
     * function.
     * </p>
     * <p>
     * The returned values are ordered so that the lowest frequency components
     * come first. That is, the components are indexed according to their
     * discrete frequency. Note also that the zero-frequency component of a sine
     * transform is identically zero, as is the <em>N<sup>th</sup></em>
     * component. Thus, the first and last values will always be zero.
     * </p>
     *
     * @param arrFunc vector array of function values (zero values on either
     * end)
     *
     * @return vector array of transformed value
     *
     * @throws IllegalArgumentException invalid function dimension
     */
    public AbstractComplexVector transform(final double[] arrFunc) throws IllegalArgumentException {
        int szArr = arrFunc.length;

        // Check the dimensions
        if (szArr != this.getDataSize()) {
            throw new IllegalArgumentException(
                    "FourierExpTransform#transform():"
                    + " array size != "
                    + this.getDataSize()
            );
        }

        // Perform the transform
        double[] arrZero = this.createZeroFunction(this.getDataSize());
        ComplexVector vecFunc = new ComplexVector(arrFunc, arrZero);
        AbstractComplexVector vecTrans = this.matKerFwd.multiply(vecFunc);

        return vecTrans;
    }

    /**
     * <p>
     * Compute and return the Fourier exponential transform of the given
     * function.
     * </p>
     * <p>
     * The returned values are ordered so that the lowest frequency components
     * come first. That is, the components are indexed according to their
     * discrete frequency. Note also that the zero-frequency component of a sine
     * transform is identically zero, as is the <em>N<sup>th</sup></em>
     * component. Thus, the first and last values will always be zero.
     * </p>
     *
     * @param vecTrans vector array of inverse transform values (zero values on
     * either end)
     *
     * @return vector array of transformed value
     *
     * @throws IllegalArgumentException invalid function dimension
     */
    public double[] inverse(final AbstractComplexVector vecTrans)
            throws IllegalArgumentException {
        int szArr = vecTrans.dimension();

        // Check the dimensions
        if (szArr != this.getDataSize()) {
            throw new IllegalArgumentException(
                    "FourierExpTransform#inverse():"
                    + " array size != "
                    + this.getDataSize()
            );
        }

        // Perform the transform
        AbstractComplexVector vecFunc = this.matKerInv.multiply(vecTrans);

        // Unpack result and return it
        double[] arrFunc = new double[szArr];
        for (int index = 0; index < szArr; index++) {
            arrFunc[index] = vecFunc.getRealComponent(index);
        }

        return arrFunc;
    }

    /**
     * <p>
     * Compute and return the discrete power spectrum for the given function.
     * The power spectrum is the square of the frequency spectrum and,
     * therefore, is always real and positive.
     * </p>
     * <p>
     * The returned values are ordered so that the lowest frequency components
     * are located at the end points. Specifically, due to the nature of the
     * discrete Fourier transform the spectrum has the topology of the circle.
     * The frequency <em>N</em> - 1 is actually the negative frequency -1. Thus,
     * the negative frequency -<em>n</n> is located at index
     * <em>N</em> - <em>n</em>. The largest frequency is at index <em>N/2</em>.
     * </p>
     *
     * @param arrFunc discrete function
     *
     * @return discrete power spectrum of given function
     *
     * @throws IllegalArgumentException invalid function dimension
     */
    public double[] powerSpectrum(final double[] arrFunc) throws IllegalArgumentException {
        AbstractComplexVector vecSpec = this.transform(arrFunc);

        double[] arrSpec = new double[this.getDataSize()];
        for (int index = 0; index < this.getDataSize(); index++) {
            Complex cpxVal = vecSpec.getComponent(index);
            arrSpec[index] = cpxVal.modSqr();
        }

        return arrSpec;
    }

    /*
     * Debugging
     */
    /**
     * Write out contents to string.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String strBuf = "";

        strBuf += "Forward transform kernel\n";
        strBuf += this.matKerFwd.toString();
        strBuf += "\nInverse transform kernel\n";
        strBuf += this.matKerInv.toString();
        strBuf += "\nProduct of forward kernel and inverse kernel\n";
        strBuf += (this.matKerInv.multiply(this.matKerFwd)).toString();

        return strBuf;
    }

    /*
     * Support Methods
     */
    /**
     * Computes and stores the transform kernel.
     *
     * @param szData dimensions of the tranform kernel.
     */
    private void initTransform(int szData) {

        // vector/matrix dimensions 
        final int N = szData;

        // Compute the z transform generator
        Double h = 2.0 * Math.PI / N;
        Complex z = new Complex(Math.cos(h), Math.sin(h));

        // Create matrix kernel and compute element values
        // normalization constant
        final double c = Math.sqrt(1.0 / N);
        ComplexSquareMatrix Kf = new ComplexSquareMatrix(N);
        ComplexSquareMatrix Ki = new ComplexSquareMatrix(N);

        for (int m = 0; m < N; m++) {
            for (int n = m; n < N; n++) {
                double cos = c * Math.cos(h * m * n);
                double sin = c * Math.sin(h * m * n);

                Kf.setElement(m, n, cos, -sin);
                Kf.setElement(n, m, cos, -sin);

                Ki.setElement(m, n, cos, sin);
                Ki.setElement(n, m, cos, sin);
            }
        }

        this.szData = N;
        this.cpxZ = z;
        this.matKerFwd = Kf;
        this.matKerInv = Ki;
    }

    /**
     * Computes and stores the transform kernel.
     *
     * @param szData dimensions of the transform kernel.
     */
    private void initTransform_old(int szData) {

        // vector/matrix dimensions 
        final int N = szData;

        // Compute the z transform generator
        Double h = 2.0 * Math.PI / N;
        Complex zf = new Complex(Math.cos(h), -Math.sin(h));
        Complex zi = new Complex(Math.cos(h), Math.sin(h));

        // Create matrix kernel and compute element values
        // normalization constant
        final double c = Math.sqrt(1.0 / N);
        ComplexSquareMatrix Kf = new ComplexSquareMatrix(N);
        ComplexSquareMatrix Ki = new ComplexSquareMatrix(N);

        // matrix kernel element values
        Complex kf, ki;
        Complex zfm = Complex.ONE;
        Complex zim = Complex.ONE;
        for (int m = 0; m < N; m++) {

            kf = zfm.multiply(c);
            ki = zim.multiply(c);

            for (int n = m; n < N; n++) {
                Kf.setElement(m, n, kf);
                Kf.setElement(n, m, kf);

                Ki.setElement(m, n, ki);
                Ki.setElement(n, m, ki);

                kf = kf.multiply(zfm);
                ki = ki.multiply(zim);
            }

            zfm = zfm.multiply(zf);
            zim = zim.multiply(zi);
        }

        this.szData = N;
        this.cpxZ = zi;
        this.matKerFwd = Kf;
        this.matKerInv = Ki;
    }

    /**
     * Create and return a discrete function of the given size and with value
     * zero.
     *
     * @param szVec size of the returned function
     *
     * @return the zero function
     */
    private double[] createZeroFunction(int szVec) {
        double[] arrFunc = new double[szVec];

        for (int index = 0; index < szData; index++) {
            arrFunc[index] = 0.0;
        }

        return arrFunc;
    }

}
