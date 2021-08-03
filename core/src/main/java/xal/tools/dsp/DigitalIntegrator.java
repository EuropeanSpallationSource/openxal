/**
 *  DigitalIntegrator.java
 *
 *  Created     : September, 2007
 *  Author      : Christopher K. Allen
 */
package xal.tools.dsp;

/**
 * <p>
 * Convenience class implementing a simple 1<sup>st</sup> order digital
 * integrator. The response <em>y<sub>n</sub></em> of this filter to an input
 * <em>x<sub>n</sub></em> is given by <br>
 * <br>
 * &nbsp;&nbsp; <em>y<sub>n</sub></em> = <em>y<sub>n</em>-1</sub> +
 * <em>x<sub>n</sub></em><br>
 * <br>
 * Thus, the transfer function <em>H</em>(<em>z</em>) is given by <br>
 * <br>
 * &nbsp;&nbsp; <em>H</em>(<em>z</em>) = 1/(1 - <em>z</em><sup>-1</sup>)<br>
 * <br>
 * where <em>z</em> is the Z-transform variable. Note that the integrator is
 * unstable for zero frequency corresponding to <em>z</em> = 1, which is
 * expected for integration.
 * </p>
 * <p>
 * The integrator is initialized so that the constant of integration
 * (<em>y</em><sub>-1</sub>) is zero. This value may be changed with a call to
 * {@link DigitalIntegrator#setConstantOfIntegration(double)}.
 * </p>
 *
 * @author Christopher K. Allen
 *
 * @see xal.tools.dsp.LtiDigitalFilter
 */
public class DigitalIntegrator extends LtiDigitalFilter {

    /*
     * Initialization
     */
    /**
     * Create a new <code>DigitalIntegrator</code> object with zero constant of
     * integration.
     */
    public DigitalIntegrator() {
        this(0.0);
    }

    /**
     * Create a new <code>DigitalIntegrator</code> object with the given
     * constant of integration.
     *
     * @param dblConst constant of integration
     *
     * @see DigitalIntegrator#setConstantOfIntegration(double)
     */
    public DigitalIntegrator(double dblConst) {
        super(1);
        super.setInputCoefficient(0, 1.0);
        super.setInputCoefficient(1, 0.0);
        super.setOutputCoefficient(1, -1.0);

        this.setConstantOfIntegration(dblConst);
    }

    /**
     * Sets the constant of integration equal to the given value. Note also that
     * the filter is reset.
     *
     * @param dblConst constant of integration
     *
     * @see LtiDigitalFilter#reset()
     */
    public void setConstantOfIntegration(double dblConst) {
        this.reset();
        this.response(dblConst);
    }
}
