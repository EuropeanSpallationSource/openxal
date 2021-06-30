/**
 * ContinuationSolution.java
 *
 * @author Christopher K. Allen
 * @since Apr 15, 2013
 */
package xal.extension.twissobserver;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.tools.math.GenericMatrix;
import xal.model.ModelException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Computes the covariance matrix at a given location which is most likely to
 * produce the provided data. That is, the covariance matrix is constructed at
 * the given device location from the data provided. The method used is a
 * continuation method where a curve of covariance matrices is constructed from
 * a known solution value, the zero-current case, to the solution value for the
 * given beam charge.
 * </p>
 * <p>
 * The iterates are computed using a continuation method as described in the
 * paper "Implementation of a Beam Envelope State Observer." Specifically, let
 * <em>n</em> be the number of independent phase planes we are considering (here
 * it is 3). Then assume a <em>smooth</em> solution curve
 * <strong>s</strong>(&middot;) : <strong>R</strong> &rarr; <strong>R</strong><sup>3<em>n</em></sup>
 * mapping bunch charge <em>q</em> to the solution <strong>&sigma;</strong> &in;
 * <strong>R</strong><sup>3<em>n</em></sup> of independent beam moments at that charge.
 * Moreover, we have <strong>&sigma;</strong>* = <strong>s</strong>(<em>q</em>*) where <em>q</em>* is
 * the bunch charge at the given profile data <strong>X</strong>.
 * </p>
 * <p>
 * In the analysis we have constructed a known function
 * <strong>G</strong> : <strong>R</strong><sup>3<em>n</em></sup> &times; <strong>R</strong> &rarr;
 * <strong>R</strong><sup>3<em>n</em></sup> such that
 * <br>
 * <br>
 * &nbsp; &nbsp; <strong>G</strong>[<strong>s</strong>(<em>q</em>),<em>q</em>] = <strong>0</strong>,
 * <br>
 * <br>
 * that is, <strong>G</strong> = 0 whenever <strong>s</strong> is the least-squares solution to the
 * problem of reconstructing the Courant-Snyder parameters for the given bunch
 * charge <em>q</em> (and given data <strong>X</strong>). The solution curve
 * <strong>s</strong>(&middot;) is constructed using continuity starting from a known
 * value
 * <strong>s</strong>(0) = <strong>&sigma;</strong><sub>0</sub>, the zero-current solution. Given
 * that the value of <strong>s</strong>
 * is known at <em>q</em>, the value of <strong>s</strong> at a small distance
 * &Delta;<em>q</em> from <em>q</em> is
 * <br>
 * <br>
 * &nbsp; &nbsp; <strong>s</strong>(<em>q</em>+&Delta;<em>q</em>) = <strong>s</strong>(<em>q</em>) +
 * [d<strong>s</strong>(<em>q</em>)/d<em>q</em>]&Delta;<em>q</em>
 * + <em>O</em>(&Delta;<em>q</em>&sup2;) .
 * <br>
 * <br>
 * Essentially this method recursively computes d<strong>s</strong>(<em>q</em>)/d<em>q</em>
 * and updates <strong>s</strong>(<em>q</em>) according to the above.
 * </p>
 * <p>
 * The value d<strong>s</strong>(<em>q</em>)/d<em>q</em> is computed by consideration of the
 * known function
 * <strong>G</strong>. We take the full derivative of the equation <strong>G</strong> = <strong>0</strong>
 * w.r.t. to <em>q</em>
 * which yields
 * <br>
 * <br>
 * &nbsp; &nbsp; d<strong>s</strong>(<em>q</em>)/d<em>q</em> =
 * [&part;<strong>G</strong>(<strong>s</strong>,<em>q</em>)/&part;<strong>s</strong>]<sup>-1</sup>
 * [&part;<strong>G</strong>(<strong>s</strong>,<em>q</em>)/&part;<em>q</em>] .
 * <br>
 * <br>
 * Once this value is computed the next value on the curve <strong>s</strong>(&middot) is
 * the vector <strong>s</strong>(<em>q</em>+&Delta;<em>q</em>) = <strong>s</strong>(<em>q</em>) +
 * [d<strong>s</strong>(<em>q</em>)/d<em>q</em>]&Delta;<em>q</em>. The partial derivatives are
 * computed numerically about the given values of <strong>s</strong> = <var>matSig0</var>
 * and <em>q</em> = <var>dblBnchChg</var> using step lengths provided by the
 * methods <code>{@link #setChargeDerivativeStepPercent(double)}</code> and
 * <code>{@link #setMomentDerivativeStepPercent(double)}</code>.
 * </p>
 *
 * <h3>NOTES:</h3>
 * &middot; Bunch charge <em>Q</em> is given by beam current <em>I</em> divided by
 * machine frequency <em>f</em>. Specifically, <em>Q</em> = <em>I</em>/<em>f</em>.
 * <br>
 * &middot; A <code>{@link TransferMatrixGenerator}</code> object must be
 * supplied for the construction of one of these objects. This is done because
 * of the variety of options that exist when creating the transfer matrix
 * generator. It is safer to require pre-construction of the matrix generator
 * rather than offer all the options for such generation here.
 * <br>
 * &middot; Bunch charge <em>Q</em> is given by beam current <em>I</em> divided by
 * machine frequency <em>f</em>. Specifically, <em>Q</em> = <em>I</em>/<em>f</em>.
 * <br>
 * &middot; The derivatives are computed by take the percentage of the current
 * value of the independent variable. This will not work well when that value is
 * near zero and the independent variable has a large domain. Taking the
 * percentage of the domain size would be a better policy if that is possible.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Apr 15, 2013
 *
 */
public class CsContinuationEstimator extends CourantSnyderEstimator {

    private static final Logger LOGGER = Logger.getLogger(CsContinuationEstimator.class.getName());

    /*
     * Global Constants
     */
    //
    // Default Numerical Parameters
    //
    /**
     * Whether or not to use an embedded fixed point search
     */
    public static final boolean BOL_FIX_PT_SRCH = true;

    /**
     * Default number of space charge steps used in continuation
     */
    public static final int CNT_CHRG_STEPS = 20;

    /**
     * Default fractional perturbation of moment vector used to compute the
     * partial of the recursion function
     */
    public static final double DBL_DEL_MMT_FRAC = 0.01;

    /**
     * Default fractional perturbation of the beam current used to compute the
     * partial of the recursion function
     */
    public static final double DBL_DEL_CURR_FRAC = 0.05;

    /*
     * Local Attributes
     */
    /**
     * Whether to use secondary search (fixed point method) embedded in this
     * method
     */
    private boolean bol2ndSrch;

    /**
     * The fixed point search algorithm embedded in this method
     */
    private CsFixedPtEstimator slnEmbed;

    /**
     * Number of beam current steps - method 2
     */
    private int cntCurSteps;

    /**
     * Derivative fractional step (0,1) for computing partials w.r.t. moments -
     * method 2
     */
    private double dblDelMmtPct;

    /**
     * Derivative fractional step (0,1) for computing partials w.r.t. beam
     * current - method 2
     */
    private double dblDelCurPct;

    /*
     * Initialization
     */
    /**
     * Creates a new instance of <code>ContinuationSolution</code>.
     *
     * @param genTransMat a pre-configured transfer matrix engine used
     * internally.
     *
     * @author Christopher K. Allen
     * @since Apr 15, 2013
     */
    public CsContinuationEstimator(TransferMatrixGenerator genTransMat) {
        this(BOL_FIX_PT_SRCH, CNT_CHRG_STEPS, DBL_DEL_MMT_FRAC, DBL_DEL_CURR_FRAC, genTransMat);
    }

    /**
     * Creates a new instance of <code>ContinuationSolution</code>.
     *
     * @param bol2ndSrch use the fixed point secondary search between beam
     * charge steps
     * @param cntChgSteps number of steps used to move (continuously) from zero
     * charge to full charge
     * @param dblDelMmtFrac the fractional perturbation in moment vector used to
     * compute the partial of the recursion function
     * @param dblDelCurFrac the fractional perturbation of the beam current used
     * to compute the recursion function partial
     * @param genTransMat a pre-configured transfer matrix engine used
     * internally.
     *
     * @author Christopher K. Allen
     * @since Apr 15, 2013
     */
    public CsContinuationEstimator(boolean bol2ndSrch, int cntChgSteps, double dblDelMmtFrac, double dblDelCurFrac, TransferMatrixGenerator genTransMat) {
        super(false, genTransMat);

        this.bol2ndSrch = bol2ndSrch;
        this.slnEmbed = new CsFixedPtEstimator(genTransMat);

        this.cntCurSteps = cntChgSteps;
        this.dblDelCurPct = dblDelCurFrac;
        this.dblDelMmtPct = dblDelMmtFrac;
    }

    /**
     * Sets whether or not to use a second, internal search method within the
     * continuation solution method (i.e., method 2). This internal search is
     * simply and application of method 1 to move the current beam charge value
     * back onto the (continuous) solution curve.
     *
     * @param bolScndSrch use the secondary search if <code>true</code>, no
     * internal search if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since Apr 5, 2013
     */
    public void setUseSecondarySearch(boolean bolScndSrch) {
        this.bol2ndSrch = bolScndSrch;
    }

    /**
     * Directly sets the maximum number of iterations allowed for the secondary
     * search algorithm.
     *
     * @param cntMaxIters maximum number of allowed secondary search iterations
     *
     * @author Christopher K. Allen
     * @since Apr 18, 2013
     */
    public void setSecondarySearchIterations(int cntMaxIters) {
        this.slnEmbed.setMaxIterations(cntMaxIters);
    }

    /**
     * <p>
     * Sets the number of steps <em>N<sub>q</sub></em> used to move from the zero
     * current solution <strong>&sigma;</strong><sub>0</sub> to the finite current
     * solution <strong>&sigma;</strong>* using the continuation method. Letting <em>q</em>*
     * denote the beam charge at solution
     * <strong>&sigma;*</strong>, then the continuation method computes the solutions
     * <strong>&sigma;</strong><sub><em>n</em></sub>
     * to the sub-problems with beam charge <em>n&Delta;q</em>
     * for each <em>n</em> = 0, 1, ..., <em>N<sub>q</sub></em>
     * where &Delta;<em>q</em> &equiv; <em>q*</em>/<em>N<sub>q</sub></em> .
     * </p>
     *
     * @param cntChgSteps number of steps used to approach the true beam charge
     * solution from the zero current solution using the continuation method
     *
     * @author Christopher K. Allen
     * @since Apr 2, 2013
     */
    public void setBeamChargeSteps(int cntChgSteps) {
        this.cntCurSteps = cntChgSteps;
    }

    /**
     * <p>
     * Sets the perturbation factor used to (numerically) compute the partial
     * derivatives with respect to the beam charge. The value of beam current is
     * increased this fractional amount of the current value when computing
     * numerical derivatives. Specifically, if perturbation factor is denoted
     * &epsilon;, then the beam charge <em>q</em> is perturbed by an amount
     * &epsilon;<em>q</em>, that is, the perturbed charged <em>q</em>' is given by
     * <br>
     * <br>
     * &nbsp; &nbsp;  <em>q</em>' = <em>q</em> + &epsilon;<em>q</em>
     * <br>
     * <br>
     * </p>
     *
     * @param dblDelChgPct a value in (0,1) indicating the fraction of the
     * current charge used as perturbation
     *
     * @author Christopher K. Allen
     * @since Nov 28, 2012
     */
    public void setChargeDerivPerturb(double dblDelChgPct) {
        this.dblDelCurPct = dblDelChgPct;
    }

    /**
     * <p>
     * Sets the perturbation used to compute (numerically) the partial
     * derivatives with respect to the initial beam moments. The value of each
     * moment is increased this fractional amount of its current value when
     * computing numerical derivatives. Specifically, if this value is denoted
     * &epsilon; and &sigma;<sub><em>i</em></sub> is the <em>i<sup>th</sup></em>
     * element of moment vector <strong>&sigma;</strong>, then the perturbed moment vector
     * <strong>&sigma;</strong>' is given by
     * <br>
     * <br>
     * &nbsp; &nbsp;  <strong>&sigma;</strong>' = <strong>&sigma;</strong> +
     * &epsilon;&sigma;<sub><em>i</em></sub><strong>e</strong><sub><em>i</em></sub> ,
     * <br>
     * <br>
     * where <strong>e</strong><sub><em>i</em></sub> is the standard basis vector for moment
     * &sigma;<em>i</em>.
     * </p>
     *
     * @param dblDelMmtPct a value in (0,1) is the fraction of the current
     * moment value used as the derivative step
     *
     * @author Christopher K. Allen
     * @since Nov 28, 2012
     */
    public void setMomentDerivPerturb(double dblDelMmtPct) {
        this.dblDelMmtPct = dblDelMmtPct;
    }

    /*
     * Operations
     */
    /**
     * <p>
     * Computes the covariance matrix at the given location which is most likely
     * to produce the given data.That is, the covariance matrix is constructed
     * at the given device location from the data provided. The method used is a
     * continuation method where a curve of covariance matrices is constructed
     * from a known solution value, the zero-current case, to the solution value
     * for the given beam charge.
     * </p>
     * <p>
     * The iterates are computed using a continuation method as described in the
     * paper "Implementation of a Beam Envelope State Observer." Specifically,
     * let <em>n</em> be the number of independent phase planes we are considering
     * (here it is 3). Then assume a <em>smooth</em> solution curve
     * <strong>s</strong>(&middot;) : <strong>R</strong> &rarr; <strong>R</strong><sup>3<em>n</em></sup>
     * mapping bunch charge <em>q</em> to the solution <strong>&sigma;</strong> &in;
     * <strong>R</strong><sup>3<em>n</em></sup> of independent beam moments at that charge.
     * Moreover, we have <strong>&sigma;</strong>* = <strong>s</strong>(<em>q</em>*) where <em>q</em>*
     * is the bunch charge at the given profile data <strong>X</strong>.
     * </p>
     * <p>
     * In the analysis we have constructed a known function
     * <strong>G</strong> : <strong>R</strong><sup>3<em>n</em></sup> &times; <strong>R</strong> &rarr;
     * <strong>R</strong><sup>3<em>n</em></sup> such that
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>G</strong>[<strong>s</strong>(<em>q</em>),<em>q</em>] = <strong>0</strong>,
     * <br>
     * <br>
     * that is, <strong>G</strong> = 0 whenever <strong>s</strong> is the least-squares solution to
     * the problem of reconstructing the Courant-Snyder parameters for the given
     * bunch charge <em>q</em> (and given data <strong>X</strong>). The solution curve
     * <strong>s</strong>(&middot;) is constructed using continuity starting from a known
     * value
     * <strong>s</strong>(0) = <strong>&sigma;</strong><sub>0</sub>, the zero-current solution.
     * Given that the value of <strong>s</strong>
     * is known at <em>q</em>, the value of <strong>s</strong> at a small distance
     * &Delta;<em>q</em> from <em>q</em> is
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>s</strong>(<em>q</em>+&Delta;<em>q</em>) = <strong>s</strong>(<em>q</em>) +
     * [d<strong>s</strong>(<em>q</em>)/d<em>q</em>]&Delta;<em>q</em>
     * + <em>O</em>(&Delta;<em>q</em>&sup2;) .
     * <br>
     * <br>
     * Essentially this method recursively computes
     * d<strong>s</strong>(<em>q</em>)/d<em>q</em> and updates <strong>s</strong>(<em>q</em>) according to
     * the above.
     * </p>
     * <p>
     * The value d<strong>s</strong>(<em>q</em>)/d<em>q</em> is computed by consideration of
     * the known function
     * <strong>G</strong>. We take the full derivative of the equation <strong>G</strong> = <strong>0</strong>
     * w.r.t. to <em>q</em>
     * which yields
     * <br>
     * <br>
     * &nbsp; &nbsp; d<strong>s</strong>(<em>q</em>)/d<em>q</em> =
     * [&part;<strong>G</strong>(<strong>s</strong>,<em>q</em>)/&part;<strong>s</strong>]<sup>-1</sup>
     * [&part;<strong>G</strong>(<strong>s</strong>,<em>q</em>)/&part;<em>q</em>] .
     * <br>
     * <br>
     * Once this value is computed the next value on the curve <strong>s</strong>(&middot)
     * is the vector <strong>s</strong>(<em>q</em>+&Delta;<em>q</em>) = <strong>s</strong>(<em>q</em>) +
     * [d<strong>s</strong>(<em>q</em>)/d<em>q</em>]&Delta;<em>q</em>. The partial derivatives
     * are computed numerically about the given values of <strong>s</strong> =
     * <var>matSig0</var> and <em>q</em> = <var>dblBnchChg</var> using step
     * lengths provided by the methods
     * <code>{@link #setChargeDerivativeStepPercent(double)}</code> and
     * <code>{@link #setMomentDerivativeStepPercent(double)}</code>.
     * </p>
     *
     * @param strRecDevId ID of the device where the reconstruction is to be
     * performed
     * @param dblBnchFreq bunch arrival frequency for the given data (in Hz)
     * @param dblBmCurr beam current (in Amperes)
     * @param arrData the profile measurement data used for the reconstruction
     *
     * @return block diagonal covariance matrix (uncoupled in the phase planes)
     * containing the second-order moments of the beam at the reconstruction
     * location
     *
     * @throws ModelException error occurred during the transfer matrix
     * computations
     *
     * @author Christopher K. Allen
     * @since Apr 2, 2013
     */
    @Override
    public CovarianceMatrix computeReconstruction(String strRecDevId, double dblBnchFreq, double dblBmCurr, ArrayList<Measurement> arrData)
            throws ModelException {
        // "Convergence" does not make sense here, unless we run the secondary search
        super.dblConvErr = Double.NaN;

        // Compute the initial values
        double dblDelI = dblBmCurr / this.cntCurSteps;
        CovarianceMatrix matSig0 = this.computeZeroCurrReconFunction(strRecDevId, arrData);

        this.matCurrSigma = matSig0;
        this.matCurrF = matSig0;

        // Initialize the iterative beam current stepping
        double dblCurrI = 0.0;

        // Compute the solution curve step by step by incrementing the beam charge
        for (int n = 1; n <= this.cntCurSteps; n++) {

            dblCurrI = n * dblDelI;

            // Compute the new covariance matrix from the current one
            CovarianceMatrix matSig1 = this.iterateNext(matSig0, strRecDevId, dblBnchFreq, dblCurrI, dblDelI, arrData);

            // Move the current solution value back onto the solution curve
            if (this.bol2ndSrch) {
                try {
                    matSig1 = this.slnEmbed.computeReconstruction(strRecDevId, dblBnchFreq, dblCurrI, matSig1, arrData);

                    super.dblConvErr = this.slnEmbed.getReconConvergenceError();

                } catch (ConvergenceException e) {
                    matSig1 = this.getReconstruction();

                }

                if (super.isDebuggingOn()) {
                    LOGGER.log(Level.INFO, "  --Finished second stage search for continuation method---------------");
                    LOGGER.log(Level.INFO, "    iterations={0}, residual error={1}, convergence error={2}\n", new Object[]{this.slnEmbed.getSolnIterations(), this.slnEmbed.getReconResidualError(), this.slnEmbed.getReconConvergenceError()});

                }
            }

            // Compute the residual error and save it
            //  If it is less than the maximum return the solution
            super.dblResErr = super.computeResidualError(matSig1, strRecDevId, arrData);

            // Record the current solution
            super.matCurrSigma = matSig1;

            //  Print out debug info
            if (super.isDebuggingOn()) {
                LOGGER.log(Level.INFO, "----Continuation Method: Charge step# {0} charge={1}, residual error={2}, converge error={3}", new Object[]{n, dblCurrI, super.getReconResidualError(), super.getReconConvergenceError()});
                LOGGER.log(Level.INFO, matSig1.toStringMatrix(fmtMatrix, 12));
                LOGGER.log(Level.INFO, "-------------------------------------------------\n");
            }

            // Reset the initial covariance matrix and do another iteration
            matSig0 = matSig1;
        }

        // We stepped through all the charge values.
        //  Report the error if in debug mode
        //  Then return the computed answer
        if (bolDebug) {
            LOGGER.log(Level.INFO, "Used {0} charge steps with final residual error {1}, and convergence error {2}", new Object[]{this.cntCurSteps, super.getReconResidualError(), super.getReconConvergenceError()});
        }

        return matSig0;
    }

    /*
     * Internal Support
     */
    /**
     * <p>
     * Computes the next iterate
     * <strong>&sigma;</strong><sub><em>i</em>+1</sub> = <strong>s</strong>(<em>q<sub>i</sub></em> +
     * &Delta;<em>q<sub>i</sub></em>) from the given moment matrix
     * <strong>&sigma;</strong><sub>0</sub> = <strong>s</strong>(<em>q<sub>i</sub></em>), given beam
     * charge <em>q<sub>i</sub></em>, and given change in beam charge
     * &Delta;<em>q<sub>i</sub></em>.
     * </p>
     * <p>
     * The iterate is computed using a continuation method as described in the
     * paper "Implementation of a Beam Envelope State Observer." Specifically,
     * let <em>n</em> be the number of independent phase planes we are considering
     * (here it is 3). Then assume a <em>smooth</em> solution curve
     * <strong>s</strong>(&middot;) : <strong>R</strong> &rarr; <strong>R</strong><sup>3<em>n</em></sup>
     * mapping bunch charge <em>q</em> to the solution <strong>&sigma;</strong> &in;
     * <strong>R</strong><sup>3<em>n</em></sup> of independent beam moments at that charge.
     * Moreover, we have <strong>&sigma;</strong>* = <strong>s</strong>(<em>q</em>*) where <em>q</em>*
     * is the bunch charge for the given profile data <strong>X</strong>.
     * </p>
     * <p>
     * In the analysis we have constructed a function
     * <strong>G</strong> : <strong>R</strong><sup>3<em>n</em></sup> &times; <strong>R</strong> &rarr;
     * <strong>R</strong><sup>3<em>n</em></sup> such that
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>G</strong>[<strong>s</strong>(<em>q</em>),<em>q</em>] = <strong>0</strong>,
     * <br>
     * <br>
     * that is, <strong>G</strong> = 0 whenever <strong>s</strong> is the least-squares solution to
     * the problem of reconstructing the Courant-Snyder parameters for the given
     * bunch charge <em>q</em> and given data <strong>X</strong>. The solution curve
     * <strong>s</strong>(&middot;) is constructed using continuity starting from a known
     * value
     * <strong>s</strong>(0) = <strong>&sigma;</strong><sub>0</sub>, the zero-current case. Given
     * that the value of <strong>s</strong>
     * is known at <em>q</em>, the value of <strong>s</strong> at a small distance
     * &Delta;<em>q</em> from <em>q</em> is
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>s</strong>(<em>q</em>+&Delta;<em>q</em>) = <strong>s</strong>(<em>q</em>) +
     * [d<strong>s</strong>(<em>q</em>)/d<em>q</em>]&Delta;<em>q</em>
     * + <em>O</em>(&Delta;<em>q</em>&sup2;) .
     * <br>
     * <br>
     * Essentially this method computes d<strong>s</strong>(<em>q</em>)/d<em>q</em>.
     * </p>
     * <p>
     * The value d<strong>s</strong>(<em>q</em>)/d<em>q</em> is computed by consideration of
     * the known function
     * <strong>G</strong>. We take the full derivative of the equation <strong>G</strong> = 0 w.r.t.
     * to <em>q</em>
     * which yields
     * <br>
     * <br>
     * &nbsp; &nbsp; d<strong>s</strong>(<em>q</em>)/d<em>q</em> =
     * [&part;<strong>G</strong>(<strong>s</strong>,<em>q</em>)/&part;<strong>s</strong>]<sup>-1</sup>
     * [&part;<strong>G</strong>(<strong>s</strong>,<em>q</em>)/&part;<em>q</em>] .
     * <br>
     * <br>
     * Once this value is computed the return value is the vector
     * <strong>s</strong>(<em>q</em>) + [d<strong>s</strong>(<em>q</em>)/d<em>q</em>]&Delta;<em>q</em>. The
     * partial derivatives are computed numerically about the given values of
     * <strong>s</strong> = <var>matSig0</var> and <em>q</em> = <var>dblBnchChg</var> using
     * step lengths provided by the methods
     * <code>{@link #setChargeDerivativeStepPercent(double)}</code> and
     * <code>{@link #setMomentDerivativeStepPercent(double)}</code>.
     * </p>
     *
     * @param matSig0 current solution iterate
     * @param strRecDevId ID of device where reconstruction is located
     * @param dblBnchFreq bunch arrival frequency (in Hz)
     * @param dblBnchChg beam charge to use in current iterate
     * @param dblDelChg increase in beam charge for returned iterate
     * @param arrData the reconstruction problem data
     *
     * @return the next solution iterate &approx;
     * <strong>s</strong>(<em>q</em>+&Delta;<em>q</em>)
     *
     * @throws ModelException Error in computing the partial derivatives using
     * the online model
     *
     * @author Christopher K. Allen
     * @since Apr 2, 2013
     */
    private CovarianceMatrix iterateNext(
            CovarianceMatrix matSig0,
            String strRecDevId,
            double dblBnchFreq,
            double dblBnchChg,
            double dblDelChg,
            ArrayList<Measurement> arrData
    )
            throws ModelException {

        // Compute the partial derivatives of the solution curve w.r.t. the beam charge
        //  at the current beam charge and store them in a map.
        Map<PHASEPLANE, GenericMatrix> mapVecDSigdq = new HashMap<>();

        for (PHASEPLANE plane : PHASEPLANE.values()) {
            int cntDim = plane.getCovariantBasisSize();
            // TODO
            GenericMatrix matId = new GenericMatrix(cntDim, cntDim);
            matId.assignIdentity();

            // Compute the moment function resolvent
            GenericMatrix matDFdSig = this.computePartialWrtMoments(plane, matSig0, strRecDevId, dblBnchFreq, dblBnchChg, arrData);
            GenericMatrix matDGdSig = matDFdSig.minus(matId);
            GenericMatrix matDGdSigInv = matDGdSig.inverse();

            // Compute the partial of the solution curve w.r.t. charge and store
            GenericMatrix vecDFdq = this.computePartialWrtCharge(plane, matSig0, strRecDevId, dblBnchFreq, dblBnchChg, arrData);
            GenericMatrix vecDSigDq = matDGdSigInv.times(vecDFdq);

            mapVecDSigdq.put(plane, vecDSigDq);
        }

        GenericMatrix vecDelSigHor = mapVecDSigdq.get(PHASEPLANE.HOR).times(dblDelChg);
        GenericMatrix vecDelSigVer = mapVecDSigdq.get(PHASEPLANE.VER).times(dblDelChg);
        GenericMatrix vecDelSigLng = mapVecDSigdq.get(PHASEPLANE.LNG).times(dblDelChg);

        CovarianceMatrix matDelSig = PHASEPLANE.constructCovariance(vecDelSigHor, vecDelSigVer, vecDelSigLng);
        CovarianceMatrix matSig1 = new CovarianceMatrix(matSig0.plus(matDelSig));

        return matSig1;
    }

    /**
     * <p>
     * Computes the partial derivative of the recursion function
     * <strong>F</strong>(<strong>&sigma;</strong>,<em>q</em>) for the given phase plane and at the
     * given value <strong>&sigma;</strong><sub>0</sub> of the covariance matrix and the
     * given beam charge
     * <em>q</em><sub>0</sub>.
     * </p>
     * <p>
     * The partial &part;<strong>F</strong>(<strong>&sigma;</strong>,<em>q</em>)/&part;<em>q</em> is
     * computed numerically by perturbing the beam charge <em>q</em> by a small
     * percentage &epsilon; then recomputing <strong>F</strong>(<strong>&sigma;</strong>,<em>q</em>)
     * and taking differences. Specifically,
     * <br>
     * <br>
     * &nbsp; &nbsp; &part;<strong>F</strong>(<strong>&sigma;</strong>,<em>q</em>)/&part;<em>q</em>
     * &approx; [ <strong>F</strong>(<strong>&sigma;</strong>,<em>q</em>+&epsilon;<em>q</em>) -
     * <strong>F</strong>(<strong>&sigma;</strong>,<em>q</em>) ] / &epsilon;q
     * <br>
     * <br>
     * where &epsilon; is the parameter provided by method
     * {@link #setChargeDerivativeStepPercent(double)}.
     * </p>
     *
     * @param plane phase plane we are using
     * @param matSig0 covariance matrix we are computing partials about
     * @param strDevId the device at the beamline location
     * @param dblBnchFreq arrival frequency of the beam bunches
     * @param dblBmCurr current beam current
     * @param arrMsmts the measurement data
     *
     * @throws ModelException Failed to generate transfer matrices due to a
     * simulation error
     *
     */
    private GenericMatrix computePartialWrtCharge(PHASEPLANE plane, CovarianceMatrix matSig0, String strRecDevId, double dblBnchFreq, double dblChg, ArrayList<Measurement> arrMsmts)
            throws ModelException {

        // Compute the current moment vector
        this.genTransMat.generateWithSpaceCharge(dblBnchFreq, dblChg, matSig0);
        GenericMatrix vecMmtsInit = this.computeReconSubFunction(plane, strRecDevId, arrMsmts);

        // Perturb the beam charge and recompute the moment vector
        double dblChgPert = (1.0 + this.dblDelCurPct) * dblChg;
        this.genTransMat.generateWithSpaceCharge(dblBnchFreq, dblChgPert, matSig0);
        GenericMatrix vecMmtsPert = this.computeReconSubFunction(plane, strRecDevId, arrMsmts);

        // Approximate the moment vector derivative by finite difference
        GenericMatrix vecDelF = vecMmtsPert.minus(vecMmtsInit);
        GenericMatrix vecDFdq = vecDelF.times(1.0 / (this.dblDelCurPct * dblChg));

        return vecDFdq;
    }

    /**
     * <p>
     * Computes the partial derivative of the recursion function
     * <strong>F</strong>(<strong>&sigma;</strong>,<em>q</em>) for the given phase plane and at the
     * given value <strong>&sigma;</strong><sub>0</sub> of the covariance matrix and the
     * given beam charge
     * <em>q</em><sub>0</sub>. The partials are computed numerically by perturbing
     * each moment (element of <strong>&sigma;</strong>) and recomputing
     * <strong>F</strong>(<strong>&sigma;</strong>,<em>q</em>) and taking differences.
     * </p>
     * <p>
     * We compute column vector &part;<strong>F</strong>/&part;&sigma;<sub><em>i</em></sub>
     * for each independent variable &sigma;<sub><em>i</em></sub> &equiv;
     * [<strong>&sigma;</strong>]<sub><em>i</em></sub>. of vector <strong>&sigma;</strong>. The
     * partials are computed numerically by perturbing
     * &sigma;<sub><em>i</em></sub> by &epsilon;<strong>e</strong><sub><em>i</em></sub> at
     * <strong>&sigma;</strong><sub>0</sub>
     * where <strong>e</strong><sub><em>i</em></sub> is the <em>i</em><sup>th</sup> covariance
     * basis matrix and &epsilon; is a percentage of the current value of
     * &sigma;<sub><em>i</em></sub>. The returned value
     * &part;<strong>F</strong>/&part;<strong>&sigma;</strong> is the augmentation
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>F</strong>/&part;<strong>&sigma;</strong> = (
     * &part;<strong>F</strong>/&part;&sigma;<sub>1</sub> |
     * &part;<strong>F</strong>/&part;&sigma;<sub>2</sub> |
     * &part;<strong>F</strong>/&part;&sigma;<sub>3</sub>
     * )
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot; This method assumes the phase planes to be independent.
     * Specifically it does not currently consider the partials of the moments
     * in one phase plane with respect to the variation of moments in a
     * different phase plane.
     * </p>
     *
     * @param plane phase plane to compute partials
     * @param matSig0 initial beam state (i.e., second-order moments) at
     * reconstruction location
     * @param strRecDevId ID of device where Courant-Snyder parameters are
     * reconstructed
     * @param dblBnchFreq beam bunch arrival frequency (in Hz)
     * @param dblChg beam bunch current<em>I</em><sub>0</sub> (in Amperes)
     * @param arrMsmts the measured profile data
     *
     * @return the partial derivative <strong>F</strong>/&part;<strong>&sigma;</strong> of the
     * recursion operator <strong>F</strong>
     *
     * @throws ModelException Failed to generate transfer matrices due to a
     * simulation error
     *
     * @author Christopher K. Allen
     * @since Apr 1, 2013
     */
    private GenericMatrix computePartialWrtMoments(PHASEPLANE plane, CovarianceMatrix matSig0, String strRecDevId, double dblBnchFreq, double dblChg, ArrayList<Measurement> arrMsmts)
            throws ModelException {

        // Extract the initial moments from the covariance matrix
        //  These values are the coordinates in the domain about which we are taking 
        //  the numerical partial derivative
        GenericMatrix vecSig0 = plane.extractCovarianceVector(matSig0);

        // Compute the current value of F(sig0,q) from the initial moments (and charge)
        //  These values are the point in the range of F that which sig0 maps to 
        this.genTransMat.generateWithSpaceCharge(dblBnchFreq, dblChg, matSig0);
        GenericMatrix vecMmtsInit = this.computeReconSubFunction(plane, strRecDevId, arrMsmts);

        // Perturb each moment and recompute the result
        //  We compute column vector DF/dSig_i for each independent variable sig_i 
        //  of vector sig.  The partials are computed numerically by perturbing sig 
        //  by del*e_i at sig0 where e_i is the ith covariance basis matrix.
        ArrayList<GenericMatrix> arrVecDelF = new ArrayList<>();

        for (int i = 0; i < plane.getCovariantBasisSize(); i++) {
            double dblMmt0 = vecSig0.getElem(i, 0);
            double dblMmtPert = this.dblDelMmtPct * dblMmt0;

            CovarianceMatrix matBasis = plane.getCovarianceBasis(i);
            PhaseMatrix matPert = matBasis.times(dblMmtPert);
            CovarianceMatrix matDelSig = new CovarianceMatrix(matSig0.plus(matPert));

            this.genTransMat.generateWithSpaceCharge(dblBnchFreq, dblChg, matDelSig);
            GenericMatrix vecMmtsPert = this.computeReconSubFunction(plane, strRecDevId, arrMsmts);
            GenericMatrix vecDelF = vecMmtsPert.minus(vecMmtsInit);
            GenericMatrix vecDFdSig = vecDelF.times(1.0 / dblMmtPert);

            arrVecDelF.add(vecDFdSig);
        }

        // Build the partial derivative matrix
        int iRowMaxF = vecMmtsInit.getRowCnt() - 1;
        int iRowMaxSig = vecSig0.getRowCnt() - 1;
        GenericMatrix matDFdSig = new GenericMatrix(iRowMaxF + 1, iRowMaxSig + 1);

        for (int j = 0; j <= iRowMaxSig; j++) {
            GenericMatrix vecDelF = arrVecDelF.get(j);

            matDFdSig.setSubMatrix(0, iRowMaxF, j, j, vecDelF.getArrayCopy());
        }

        return matDFdSig;
    }

}
