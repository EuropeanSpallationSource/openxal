/**
 * DigitalDifferentiator.java
 *
 * Created      : September, 2007
 * Author       : Christopher K. Allen
 */
package xal.tools.dsp;

/**
 * <p>
 * Convenience class implementing a simple 1<sup>st</sup> order digital
 * differentiator. The response <em>y<sub>n</sub></em> of this filter to an input
 * <em>x<sub>n</sub></em>
 * is given by
 * <br>
 * <br>&nbsp;&nbsp;  <em>y<sub>n</sub></em> = <em>x<sub>n</sub></em> -
 * <em>x<sub>n</em>-1</sub><br>
 * <br>
 * Thus, the transfer function <em>H</em>(<em>z</em>) is given by
 * <br>
 * <br>&nbsp;&nbsp;  <em>H</em>(<em>z</em>) = 1 - <em>z</em><sup>-1</sup><br>
 * <br>
 * where <em>z</em> is the Z-transform variable.
 * </p>
 * <p>
 * The differentiator is initialized so that the initial input
 * <em>x</em><sub>-1</sub>
 * is zero. Thus, the first output from this filter is the first input. This
 * value may be changed with a call to
 * {@link DigitalIntegrator#setInputCoefficient(int, double)}.
 * </p>
 *
 * @author Christopher K. Allen
 *
 * @see xal.tools.dsp.LtiDigitalFilter
 */
public class DigitalDifferentiator extends LtiDigitalFilter {

    /*
     * Initialization
     */
    /**
     * Create a new <code>DigitalDifferentiator</code> object with zero initial
     * response.
     *
     * @see DigitalDifferentiator#setInitialResponse(double)
     */
    public DigitalDifferentiator() {
        this(0.0);
    }

    /**
     * Create a new <code>DigitalDifferentiator</code> object with the given
     * initial response.
     *
     * @param dblVal initial response of the differentiator
     */
    public DigitalDifferentiator(double dblVal) {
        super(1);
        super.setInputCoefficient(0, 1.0);
        super.setInputCoefficient(1, -1.0);
        super.setOutputCoefficient(1, 0.0);
        this.setInitialResponse(dblVal);
    }

    /**
     * Set the initial response of the differentiator to the given value. Note
     * also the the filter is reset.
     *
     * @param dblVal initial response of the differentiator
     *
     * @see LtiDigitalFilter#reset()
     */
    public void setInitialResponse(double dblVal) {
        this.reset();
        this.response(dblVal);
    }

}
