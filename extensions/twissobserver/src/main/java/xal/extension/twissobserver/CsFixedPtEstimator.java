/**
 * TwissObserver.java
 *
 *  Created : July, 2012
 *  Author  : Christopher K. Allen
 *            Eric Dai
 */
package xal.extension.twissobserver;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.tools.collections.LinearBuffer;
import xal.tools.math.GenericMatrix;
import xal.tools.math.GenericSquareMatrix;
import xal.model.ModelException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * <p>
 * Computes the Twiss parameters from wire scanner data.
 * </p>
 * <p>
 * Computes the covariance matrix <strong>&sigma;</strong> of second moments at
 * the given device location for the given beam current using the given RMS
 * envelope data. The algorithm is started by using the zero-current value
 * covariance matrix as the initialize guess (see
 * <code>{@link #computeCovarianceZeroCurrent(String, ArrayList)}</code>). The
 * zero-current covariance matrix is passed to the method
 * <code>{@link #computeCovarianceFiniteCurrent(String, double, CovarianceMatrix, ArrayList)}</code>.
 * </p>
 * <p>
 * A <code>{@link TransferMatrixGenerator}</code> object must be supplied for
 * the construction of one of these objects. This is done because of the variety
 * of options that exist when creating the transfer matrix generator. It is
 * safer to require pre-construction of the matrix generator rather than offer
 * all the options for such generation here.
 * </p>
 * <h3>NOTES:</h3>
 * <p>
 * &middot; Bunch charge <em>Q</em> is given by beam current <em>I</em> divided
 * by machine frequency <em>f</em>. Specifically, <em>Q</em> =
 * <em>I</em>/<em>f</em>.
 * </p>
 *
 * @author Eric Dai
 * @author Christopher K. Allen
 * @since 7/10/2012
 *
 */
public class CsFixedPtEstimator extends CourantSnyderEstimator {

    private static final Logger LOGGER = Logger.getLogger(CsFixedPtEstimator.class.getName());

    /*
     * Interface Definitions
     */
    /**
     * Interface defining the methods to implement for a class that wishes to
     * monitor the progress of solution computation.
     *
     * @author Christopher K. Allen
     * @since Oct 2, 2014
     */
    public interface IProgressListener {

        /**
         * Notifies the listener of the residual error at the current iteration.
         * Thus, this method is fired at every iteration. Note that the tuning
         * parameter &alpha; is dynamic, its value changes throughout the
         * iteration based upon an algorithm for stabilizing the convergence.
         *
         * @param cntIter the current iteration count
         * @param dblError the residual solution error at the current iteration
         * @param dblAlpha the current tuning parameter value
         *
         * @author Christopher K. Allen
         * @since Oct 2, 2014
         */
        public void iterationUpdate(int cntIter, double dblAlpha, double dblError);

        /**
         * Signals the listener object that the search has terminated and
         * provides the covariance matrix where the search stopped. It is up to
         * the listener to check the error tolerance and iteration count to
         * determine whether or not the given covariance matrix is acceptable.
         *
         * @param matRecon the covariance matrix where the search ended.
         *
         * @author Christopher K. Allen
         * @since Oct 23, 2014
         */
        public void searchComplete(CovarianceMatrix matRecon);

    }

    /*
     * Internal Classes
     */
    /**
     * Convenience class for storing circular buffers of moment vector objects
     * by their phase plane.
     *
     * @author Christopher K. Allen
     * @since Apr 10, 2013
     *
     */
    private static class MomentVectorBuffer extends HashMap<PHASEPLANE, LinearBuffer<GenericMatrix>> {

        /*
         * Global Constants
         */
        /**
         * Serialization version ID @since Apr 11, 2013
         */
        private static final long serialVersionUID = 1L;

        /*
         * Initialization
         */
        /**
         * Creates a new instance of <code>CovVecBuffer</code> and initiates the
         * circular buffers for each phase plane.
         *
         * @author Christopher K. Allen
         * @since Apr 10, 2013
         */
        public MomentVectorBuffer() {
            for (PHASEPLANE plane : PHASEPLANE.values()) {
                int cntSize = plane.getCovariantBasisSize();
                LinearBuffer<GenericMatrix> bufVec = new LinearBuffer<>(cntSize);

                put(plane, bufVec);
            }
        }

        /*
         * Operations
         */
        /**
         * Convenience method for adding a covariance vector to the set of
         * vectors managed for the given phase plane.
         *
         * @param plane phase plane managed vector set
         * @param vecCov new vector to add to managed set
         *
         * @author Christopher K. Allen
         * @since Apr 11, 2013
         */
        public void add(PHASEPLANE plane, GenericMatrix vecCov) {
            super.get(plane).add(vecCov);
        }

        /**
         * Initializes the buffer set by inserting the complete set of standard
         * basis vectors for covariance space in each phase plane.
         *
         * @author Christopher K. Allen
         * @since Apr 10, 2013
         */
        public void initWithBasisVectors() {
            for (PHASEPLANE plane : PHASEPLANE.values()) {
                int szBasis = plane.getCovariantBasisSize();
                LinearBuffer<GenericMatrix> bufCovVecs = get(plane);

                for (int i = 0; i < szBasis; i++) {
                    GenericMatrix vecCovBasis = new GenericMatrix(szBasis, 1);
                    vecCovBasis.setElem(i, 0, 1.0);

                    bufCovVecs.add(vecCovBasis);
                }
            }
        }

        /**
         * Augments all the vectors in the buffer for the given phase plane into
         * a matrix of column vectors.
         *
         * @param buffer containing vectors that compose matrix
         *
         * @return augmented matrix composed of vectors from the given phase
         * plane buffer
         *
         * @author Christopher K. Allen
         * @since Apr 11, 2013
         */
        public GenericMatrix createAugmentedMatrix(PHASEPLANE plane) {
            LinearBuffer<GenericMatrix> bufVecs = get(plane);

            // Instantiate the augmented matrix
            int szRow = bufVecs.get(0).getRowCnt();
            int szCol = bufVecs.size();
            GenericMatrix matAug = new GenericMatrix(szRow, szCol);

            // Pack the matrix with the vectors containing herein
            int indCol = 0;
            for (GenericMatrix vecCov : bufVecs) {

                matAug.setSubMatrix(0, szRow - 1, indCol, indCol, vecCov.getArrayCopy());
                indCol++;
            }

            return matAug;
        }
    }

    /*
     * Global Constants
     */
    //
    // Default Numerical Parameters
    //
    /**
     * Maximum number of solution iterations
     */
    public static final int INT_MAX_ITER = 150;

    /**
     * Maximum residual error in solution
     */
    public static final double DBL_MAX_ERR = 1.0e-7;

    /**
     * Default value of the fixed point iteration tuning parameter
     */
    public static final double DBL_ALPHA = 0.5;

    /*
     * Local Attributes
     */
    //
    // Algorithm Tools
    //
    /**
     * Buffers containing recursion function changes - used in approximating
     * recursion function derivative
     */
    private final MomentVectorBuffer mapDeltaF;

    /**
     * Buffers containing moment vector changes - used in approximating the
     * recursion function derivative
     */
    private final MomentVectorBuffer mapDeltaSig;

    //
    // Numerical Parameters 
    //
    /**
     * Maximum number of iterations allowed
     */
    private int cntMaxIter;

    /**
     * Maximum residual error allowed
     */
    private double dblMaxError;

    /**
     * Fixed point tuning parameter - normalized distance along convex hull
     * between current iterate and next
     */
    private double dblAlpha;

    //
    // Solution Characteristics
    //
    /**
     * The last recorded iteration tuning parameter
     */
    private double dblCurrAlpha;

    /**
     * The number of iterations used to compute the last solution
     */
    private int cntCurrIters;

    //
    //  External
    //
    /**
     * List of objects waiting for progress updates
     */
    private List<IProgressListener> lstProgLsns;

    /*
     * Initialization
     */
    /**
     * Creates a new <code>TwissObserver</code> object which uses the given
     * transfer matrix generator.  <em>The new object can only be used for the
     * zero space charge case.</em>
     *
     * @param genTransMatrix
     *
     * @author Christopher K. Allen
     * @since Sep 4, 2012
     */
    public CsFixedPtEstimator(TransferMatrixGenerator genTransMatrix) {
        this(INT_MAX_ITER, DBL_MAX_ERR, DBL_ALPHA, genTransMatrix);
    }

    /**
     * Creates a new <code>TwissObserver</code> object using the given maximum
     * iteration count, maximum convergence error, and the given transfer matrix
     * generator. This constructor must be used if Twiss parameters are to be
     * computed in the presence of space charge (see
     * <code>{@link #computeCovarianceFiniteCurrent(String, double, ArrayList)}</code>).
     *
     * @param cntMaxIter maximum number of allowed search iterations
     * @param dblMaxError maximum <em>L</em><sub>2</sub> convergence error in
     * the solution
     * @param dblAlpha the default value of the fixed point iteration tuning
     * parameter
     * @param genTransMat a pre-configured transfer matrix engine used
     * internally.
     *
     * @author Christopher K. Allen
     * @author Eric Dai
     * @since Jul 20, 2012
     */
    public CsFixedPtEstimator(int cntMaxIter, double dblMaxError, double dblAlpha, TransferMatrixGenerator genTransMat) {
        super(genTransMat);

        this.cntMaxIter = cntMaxIter;
        this.dblMaxError = dblMaxError;
        this.dblAlpha = dblAlpha;

        mapDeltaF = new MomentVectorBuffer();
        mapDeltaSig = new MomentVectorBuffer();

        lstProgLsns = new LinkedList<>();
    }

    /**
     * Directly sets the maximum number of iterations allowed for the space
     * charge algorithm. The algorithm is terminated if the iteration count
     * reaches this number.
     *
     * @param cntMaxIter maximum number of allowed algorithm iterations
     *
     * @author Christopher K. Allen
     * @since Nov 13, 2012
     */
    public void setMaxIterations(int cntMaxIter) {
        this.cntMaxIter = cntMaxIter;
    }

    /**
     * Directly set the iteration tolerance for the algorithm. If the residual
     * error of the current solution and the previous solution is less than this
     * value then the algorithm is considered converged and terminates.
     *
     * @param dblMaxError the maximum error tolerance for the algorithm
     *
     * @author Christopher K. Allen
     * @since Nov 13, 2012
     */
    public void setMaxError(double dblMaxError) {
        this.dblMaxError = dblMaxError;
    }

    /**
     * The numerical tuning parameter &alpha; in the interval [0,1] which sets
     * the normalized step size taken between the current solution iterate and
     * the next solution iterate in Method 1. A value of 0 ignores the new value
     * completely while a value of 1 ignores completely the previous iterate.
     * This parameter is designed to slow down convergence for the sake of
     * stability. Let
     * <strong>F</strong>(<strong>&sigma;</strong><sub><em>i</em></sub>) be the
     * iteration map then the next solution iterate
     * <strong>&sigma;</strong><sub><em>i</em>+1</sub> is
     * <br>
     * <br>
     * &nbsp; &nbsp;  <strong>&sigma;</strong><sub><em>i</em>+1</sub> = (1 -
     * &alpha;)<strong>&sigma;</strong><sub><em>i</em></sub>
     * +
     * &alpha;<strong>F</strong>(<strong>&sigma;</strong><sub><em>i</em></sub>)
     * .
     * <br>
     * <br>
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot; A &lambda; value near 1 can cause a type of "super convergence"
     * instability.
     * </p>
     *
     * @param dblAlpha stability/convergence parameter &lambda; &in; [0,1]
     *
     * @author Christopher K. Allen
     * @since Nov 13, 2012
     */
    public void setSearchTuningValue(double dblAlpha) {
        this.dblAlpha = dblAlpha;
    }

    /*
     * Operations
     */
    /**
     * Returns the number of fixed point iterations needed to compute the last
     * computed solution.
     *
     * @return fixed point iterations used to compute the last solution
     *
     * @author Christopher K. Allen
     * @since Apr 17, 2013
     */
    public int getSolnIterations() {
        return cntCurrIters;
    }

    /**
     * Adds the current <code>IProgressListener</code> exposing object to the
     * list of objects receiving solution progress updates.
     *
     * @param lsnProg object to receive solution progress updates
     *
     * @author Christopher K. Allen
     * @since Oct 2, 2014
     */
    public void addProgressListener(IProgressListener lsnProg) {
        lstProgLsns.add(lsnProg);
    }

    /*
     * Operations
     */
    /**
     * <p>
     * Computes the covariance matrix <strong>&sigma;</strong> of second moments
     * at the given device location for the given beam current using the given
     * RMS envelope data. The algorithm is started by using the zero-current
     * value covariance matrix as the initialize guess (see
     * <code>{@link #computeCovarianceZeroCurrent(String, ArrayList)}</code>).
     * The zero-current covariance matrix is passed to the method
     * <code>{@link #computeCovarianceFiniteCurrent(String, double, CovarianceMatrix, ArrayList)}</code>.
     * </p>
     * <h3>NOTE:</h3>
     * <p>
     * &middot; Bunch charge <em>Q</em> is given by beam current <em>I</em>
     * divided by machine frequency <em>f</em>. Specifically, <em>Q</em> =
     * <em>I</em>/<em>f</em>.
     * </p>
     *
     * @param strRecDevId ID of the device where the reconstruction is to be
     * performed
     * @param dblBnchFreq bunch arrival frequency (in Hz)
     * @param dblBmCurr beam current (in A)
     * @param arrData the profile measurement data used for the reconstruction
     *
     * @return block diagonal covariance matrix (uncoupled in the phase planes)
     * containing the second-order moments of the beam at the reconstruction
     * location
     *
     * @throws ModelException error occurred during the transfer matrix
     * computations
     * @throws ConvergenceException failed to meet error tolerance after maximum
     * number of iterations
     *
     * @author Christopher K. Allen
     * @since Aug 30, 2012
     */
    @Override
    public CovarianceMatrix computeReconstruction(String strRecDevId, double dblBnchFreq, double dblBmCurr, List<Measurement> arrData)
            throws ModelException, ConvergenceException {

        // Compute the initial position, compute the solution, then return it
        CovarianceMatrix matSig0 = computeZeroCurrReconFunction(strRecDevId, arrData);
        return computeReconstruction(strRecDevId, dblBnchFreq, dblBmCurr, matSig0, arrData);
    }

    /**
     * <p>
     * Computes the covariance matrix <strong>&sigma;</strong> of second moments
     * at the given device location for the given beam current for the given RMS
     * envelope data. The covariance matrix is computed iteratively using the
     * given covariance matrix <var>matSigInit</var> as the initial guess. Thus,
     * the closer <var>matSigInit</var> is to the solution (using the Frobenius
     * norm) the faster the algorithm converges.
     * </p>
     * <h3>NOTE:</h3>
     * <p>
     * &middot; Bunch charge <em>Q</em> is given by beam current <em>I</em>
     * divided by machine frequency <em>f</em>. Specifically, <em>Q</em> =
     * <em>I</em>/<em>f</em>.
     * </p>
     *
     * @param strRecDevId ID of the device where the reconstruction is to be
     * performed
     * @param dblBnchFreq bunch arrival frequency (in Hz), typically a
     * sub-harmonic of machine frequency
     * @param dblBmCurr beam current (in Amperes)
     * @param matSigInit the initial covariance matrix (guess) used to start the
     * algorithm
     * @param arrData the profile measurement data used for the reconstruction
     *
     * @return block diagonal covariance matrix (uncoupled in the phase planes)
     * containing the second-order moments of the beam at the reconstruction
     * location
     *
     * @throws ModelException error occurred during the transfer matrix
     * computations
     * @throws ConvergenceException failed to meet error tolerance after maximum
     * number of iterations
     *
     * @author Christopher K. Allen
     * @since Aug 30, 2012
     */
    public CovarianceMatrix computeReconstruction(
            String strRecDevId,
            double dblBnchFreq,
            double dblBmCurr,
            CovarianceMatrix matSigInit,
            List<Measurement> arrData
    )
            throws ModelException, ConvergenceException {
        // Initialize the delta vector buffers
        mapDeltaF.initWithBasisVectors();
        mapDeltaSig.initWithBasisVectors();

        // Initialize the iterative search
        matCurrSigma = matSigInit;
        matCurrF = matSigInit;
        dblConvErr = Double.MAX_VALUE;
        dblResErr = Double.MAX_VALUE;

        int cntIter = 0;
        double dblErr = this.dblConvErr;
        CovarianceMatrix matSig0 = matSigInit;

        // Keep iterating unless we hit the maximum number
        while (cntIter++ < this.cntMaxIter) {

            // Compute the new covariance matrix from the current one
            CovarianceMatrix matSig1 = iterateNext(matSig0, strRecDevId, dblBnchFreq, dblBmCurr, arrData);

            // Compute the convergence error
            //  If it is less than the maximum return the solution
            super.dblConvErr = super.computeConvergenceError(matSig1, matSig0);
            super.dblResErr = super.computeResidualError(matSig1, strRecDevId, arrData);

            dblErr = super.dblConvErr;

            //  Type out debug info
            if (super.isDebuggingOn()) {
                LOGGER.log(Level.INFO, "  Iteration Method:  At iteration# {0}", cntIter);
                LOGGER.log(Level.INFO, "    alpha={0}, residual error={1}, convergence error={2}", new Object[]{this.dblCurrAlpha, this.dblResErr, this.dblConvErr});
                LOGGER.log(Level.INFO, matSig1.toStringMatrix(this.fmtMatrix, 12));
                LOGGER.log(Level.INFO, "  -------------------------------------------------\n");
            }

            this.fireProgressUpdate(cntIter, this.dblCurrAlpha, dblErr);

            // Record the iteration count
            this.cntCurrIters = cntIter;

            // If the current solution beats the error criteria we're done
            if (dblErr < this.dblMaxError) {
                return matSig1;
            }

            // Reset the initial covariance matrix and do another iteration
            matSig0 = matSig1;
        }

        // We iterated the maximum number of times and did not get below the maximum error
        //  Throw a convergence error
        throw new ConvergenceException("Iterated " + cntIter + " times with convergence error " + dblErr + ". ");
    }

    /**
     * Computes the next iterated moment in the finite-space charge second-order
     * moments calculation. The transfer matrices are generated with the given
     * initial beam states (RMS Envelopes) and beam current. The transfer
     * matrices are then used to construct the observation matrix from which the
     * initial Courant-Snyder parameters (equivalently, second-order moments)
     * can be computed.
     *
     * @param matSig0 initial beam state (i.e., second-order moments) at
     * reconstruction location
     * @param strRecDevId ID of device where Courant-Snyder parameters are
     * reconstructed
     * @param dblBnchFreq bunch arrival frequency (in Hz)
     * @param dblBmCurr beam current (in Amperes)
     * @param arrData the measured profile data.
     *
     * @return the next iterate of second-order moments at the reconstruction
     * location
     *
     * @throws ModelException Error in computing the transfer matrices using the
     * online model
     *
     * @author Christopher K. Allen
     * @since Sep 10, 2012
     */
    private CovarianceMatrix iterateNext(CovarianceMatrix matSig0, String strRecDevId, double dblBnchFreq, double dblBmCurr, List<Measurement> arrData)
            throws ModelException {

        // Compute the transfer matrices between stations 
        this.genTransMat.generateWithSpaceCharge(null, dblBnchFreq, dblBmCurr, matSig0);

        // Recurse through all the phase planes computing the new value of the recursion function
        Map<PHASEPLANE, GenericMatrix> mapFcurr = new HashMap<>();

        for (PHASEPLANE plane : PHASEPLANE.values()) {

            // Compute the new value of the recursion function 
            GenericMatrix vecFcurr = this.computeReconSubFunction(plane, strRecDevId, arrData);
            mapFcurr.put(plane, vecFcurr);
        }

        // Construct the new recursion function value as a covariance matrix 
        GenericMatrix vecMmtsHor = mapFcurr.get(PHASEPLANE.HOR);
        GenericMatrix vecMmtsVer = mapFcurr.get(PHASEPLANE.VER);
        GenericMatrix vecMmtsLng = mapFcurr.get(PHASEPLANE.LNG);

        CovarianceMatrix covF1 = PHASEPLANE.constructCovariance(vecMmtsHor, vecMmtsVer, vecMmtsLng);

        // Construct the new covariance matrix computed from the data and the observation matrix
        //  Algorithm for generating new covariance matrix from old
        double alpha = this.computeAlpha();

        PhaseMatrix matSig1 = covF1.times(alpha).plus(matSig0.times(1.0 - alpha));
        CovarianceMatrix covSig1 = new CovarianceMatrix(matSig1);

        //  Recurse through all the phase planes computing the change in the recursion function from
        //      this iteration and the last iteration, and the change in the moment vectors that 
        //      produced them.
        for (PHASEPLANE plane : PHASEPLANE.values()) {

            // Now compute the change in the recursion function value
            GenericMatrix vecFprev = plane.extractCovarianceVector(matCurrF);
            GenericMatrix vecFcurr = mapFcurr.get(plane);
            GenericMatrix vecDelF = vecFcurr.minus(vecFprev);
            mapDeltaF.add(plane, vecDelF);

            // Compute the change in the moments that produces the recursion function change
            GenericMatrix vecSigPrev = plane.extractCovarianceVector(matCurrSigma);
            GenericMatrix vecSigCurr = plane.extractCovarianceVector(covSig1);
            GenericMatrix vecDelSig = vecSigCurr.minus(vecSigPrev);
            mapDeltaSig.add(plane, vecDelSig);
        }

        // Record the current solution iterates
        dblCurrAlpha = alpha;
        super.matCurrF = covF1;
        super.matCurrSigma = covSig1;

        // Return the current solution iterate
        return covSig1;
    }

    /**
     * Computes the iteration tuning parameter from the previous changes in the
     * covariance moments and the corresponding changes in the recursion
     * function.
     *
     * @return new value of the tuning parameter that accelerates convergence
     *
     * @author Christopher K. Allen
     * @since Apr 12, 2013
     */
    private double computeAlpha() {
        double alpha = this.dblAlpha;

        for (PHASEPLANE plane : PHASEPLANE.values()) {

            // Create the augmented change in values matrices
            GenericMatrix matDelF = mapDeltaF.createAugmentedMatrix(plane);
            GenericMatrix matDelSig = mapDeltaSig.createAugmentedMatrix(plane);

            // Create an identity matrix
            int cntRows = matDelSig.getRowCnt();
            int cntCols = matDelSig.getColCnt();

            GenericMatrix matIden = new GenericMatrix(cntRows, cntCols);
            matIden.assignIdentity();

            // Check the rank of the change in sigma values matrix
            //  If not of full rank, we need to skip this plane
            int szRank = matDelSig.rank();

            if (szRank < cntRows) {
                continue;
            }

            // Compute the approximation to the partial of the recursion function
            GenericMatrix matDelSigInv = matDelSig.inverse();
            GenericMatrix matFpApprox = matDelF.times(matDelSigInv);

            // Compute the alpha value for this plane
            GenericSquareMatrix matResolv = GenericMatrix.createSquare(matIden.minus(matFpApprox));
            double dblNumer = Math.abs(matResolv.trace());
            double dblDenom = matResolv.normF();

            double dblAlphaNew = dblNumer / (dblDenom);

            if (dblAlphaNew > alpha) {
                alpha = dblAlphaNew;
            }
        }

        return alpha;
    }

    /**
     * Fires an iteration completed event to all the progress update listeners
     * while providing them will the current progress parameters.
     *
     * @param cntIter the current iteration count
     * @param dblAlpha the current value of the tuning parameter &alpha;
     * @param dblError the current solution residual error
     *
     * @author Christopher K. Allen
     * @since Oct 2, 2014
     */
    private void fireProgressUpdate(int cntIter, double dblAlpha, double dblError) {
        if (!lstProgLsns.isEmpty()) {
            for (IProgressListener lsnProg : this.lstProgLsns) {
                lsnProg.iterationUpdate(cntIter, dblAlpha, dblError);
            }
        }
    }
}
