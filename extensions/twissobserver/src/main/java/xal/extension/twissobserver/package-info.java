/**
 * package-info.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 24, 2014
 */
/**
 * <p>
 * The twissobserver package contains tools for computing the Courant-Snyder
 * parameters
 * <strong>&sigma;</strong> at a hardware location along the beamline from the RMS beam
 * sizes downstream of that that location. Rather than running a model of the
 * beamline and adjusting the initial conditions of the beam (i.e., the
 * Courant-Snyder parameters) until a chi-squared fit of the model results to
 * the data is achieved, the tools in this package create an automorphic map
 * <strong>F</strong> from the Courant-Snyder parameter domain to itself. This is direct
 * approach to the problem rather than the "weak" solution found by the fitting
 * technique where the Courant-Snyder parameters minimizing a functional. Here
 * the map is iterated to the fixed point of <strong>F</strong>, the fixed point
 * <strong>&sigma;</strong> = <strong>F&sigma;</strong> being the solution of Courant-Snyder
 * parameters.
 * </p>
 * <p>
 * Let <strong>T</strong> : <em>CS</em> &rarr; <em>D</em> be the map that takes Courant-Snyder
 * parameters to the set of RMS beam sizes along the beamline. The map <strong>T</strong>
 * is computed numerically by as a cascade of transfer maps along the beamline
 * from the reconstruction location to each of the data locations. We assume
 * there are at least as many data points as there are Courant-Snyder parameters
 * so that |<em>CS</em>| &le; |<em>D</em>|. Then the least-squares solution
 * <strong>&sigma;</strong> to the reconstruction problem is
 * <br>
 * <br>
 * &nbsp; &nbsp; <strong>&sigma;</strong> =
 * (<strong>T</strong><sup>*</sup><strong>T</strong>)<sup>-1</sup><strong>T</strong><sup>*</sup><strong>d</strong> ,
 * <br>
 * <br>
 * where <strong>T</strong><sup>*</sup> is the adjoint of <strong>T</strong> and <strong>d</strong> is the
 * vector of RMS beam sizes (i.e., the data). When no space charge is present
 * <strong>T</strong> is a linear map and this equation may be solved directly.
 * </p>
 * <p>
 * When no space charge is present, indicated by <em>I</em> &gt> 0 where <em>I</em>
 * is the generalized beam current, the map <t> now depends upon the initial
 * Courant-Snyder parameters, that is
 * <strong>T</strong> = <strong>T</strong>(<strong>&sigma;</strong>). For any given beam current <em>I</em> we
 * define the map
 * <strong>F</strong> : <em>CS</em> &rarr; <em>CS</em> as
 * <br>
 * <br>
 * &nbsp; &nbsp; <strong>F</strong>(<strong>&sigma;</strong>,<em>I</em>) &#8796;
 * [<strong>T</strong><sup>*</sup><strong>(<strong>&sigma;</strong>,<em>I</em>)T</strong>(<strong>&sigma;</strong>,<em>I</em>)]<sup>-1</sup><strong>T</strong><sup>*</sup>(<strong>&sigma;</strong>,<em>I</em>)<strong>d</strong>
 * <br>
 * <br>
 * The fixed point <strong>&sigma;</strong>(<em>I</em>) of <strong>F</strong> is the set of
 * Courant-Snyder parameters for the beam current <em>I</em>.
 * </p>
 * <p>
 * One particularly simply way of solving this problem is to pick an initial set
 * of Courant-Snyder parameters <strong>&sigma;</strong><sub>0</sub>(<em>I</em>) and start
 * iterating the above equation. If <strong>&sigma;</strong><sub>0</sub>(<em>I</em>) is
 * within the region of contraction for the fixed point of
 * <strong>F</strong> then the iteration converges to the solution. We can improve the
 * convergence properties by forming an outside loop where <em>I</em> is increased
 * from 0 to its target values. That is, <strong>F</strong> is iterated to its fixed point
 * for each increment of beam current, and that fixed point is used to
 * initialize the next iteration.
 * <p>
 * </p>
 * Another more sophisticated technique is a continuation method exploiting the
 * smoothness of
 * <strong>F</strong>(&middot;, &middot;). Starting with
 * <strong>F</strong>[<strong>&sigma;</strong>(<em>I</em>), <em>I</em>] = <strong>&sigma;</strong>(<em>I</em>) we
 * take the total derivative with respect to <em>I</em> yielding
 * <br>
 * <br>
 * &nbsp; &nbsp; &part;<sub>I</sub><strong>&sigma;</strong>(<em>I</em>) = [<strong>Id</strong> -
 * &part;<sub><strong>&sigma;</strong></sub><strong>F</strong>(<strong>&sigma;</strong>,<em>I</em>)]<sup>-1</sup>&part;<strong>F</strong>(<strong>&sigma;</strong>,<em>I</em>)
 * ,
 * <br>
 * <br>
 * where <strong>Id</strong> is the identity map. Starting from <strong>&sigma;</strong><sub>0</sub>
 * the solution for <em>I</em> = 0, which can be computed exactly, we move along
 * the curves
 * <br>
 * <br>
 * &nbsp; &nbsp; <strong>&sigma;</strong><sub><em>n</em>+1</sub> &#8796;
 * <strong>&sigma;</strong>(<em>I<sub>n</sub></em> + &Delta;<em>I</em>) =
 * &part;<sub><em>I</em></sub><strong>&sigma;</strong><em><sub>n</sub></em>&Delta;<em>I</em> +
 * <em>O</em>(&Delta;<em>I</em><sup>2</sup>) .
 * <br>
 * <br>
 * The derivatives are recomputed at each step and a brief fixed point iteration
 * is executed to move
 * <strong>&sigma;</strong><sub><em>n</em></sub> back onto the solution curve
 * <strong>&sigma;</strong>(&middot;) since the above linear extrapolation cannot account
 * for curvature.
 * </p>
 * <p>
 * </p>
 * There are three classes which perform the Courant-Snyder parameter
 * reconstructions from RMS beam size data using the techniques described above.
 * The other classes in the package are support classes for those classes.
 * <br>
 * <br>
 * &nbsp; &nbsp; <code><strong>CsZeroCurrentEstimator</strong></code> - This class is used
 * for estimating the Courant-Snyder parameters whenever space charge effects
 * are negligible. As described above, in this case it is a direct calculation
 * and very fast. The class should always be used for such a case.
 * <br>
 * <br>
 * &nbsp; &nbsp; <code><strong>CsFixedPointEstimator</strong></code> - This class
 * estimates Courant-Snyder parameters using the fixed point iteration method,
 * with space charge. At current this class performs very well; its convergence
 * properties are good and it is fast.
 * <br>
 * <br>
 * &nbsp; &nbsp; <code><strong>CsContinuationEstimator
 * <br>
 * <br>
 * If one has the RMS bunch lengths for the longitudinal direction then they can
 * be used in the transverse Courant-Snyder calculations. However, as is usually
 * the case, if these values are unknown then the class
 * <code>BunchLengthSimulator</code> can be used to estimate them via
 * simulation.
 * </p>
 * <p>
 * The measurement data is packaged in the class <code>Measurement</code>. Thus,
 * the RMS sizes, the beamline locations from which they come, and the desired
 * reconstruction location will fill out the attributes of this class. Class
 * instances are then passed to the above reconstruction engines to compute the
 * Courant-Snyder values.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 24, 2014
 */
package xal.extension.twissobserver;
