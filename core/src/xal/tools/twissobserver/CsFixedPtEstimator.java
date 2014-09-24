/**
 * TwissObserver.java
 *
 *  Created : July, 2012
 *  Author  : Christopher K. Allen 
 *            Eric Dai
 */
package xal.tools.twissobserver;

import gov.sns.tools.beam.CorrelationMatrix;
import gov.sns.tools.beam.PhaseMatrix;
import gov.sns.tools.collections.LinearBuffer;
import gov.sns.xal.model.ModelException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Jama.Matrix;


/**
 * <p>
 * Computes the twiss parameters from wire scanner data.  
 * </p>
 * <p>
 * Computes the covariance matrix <b>&sigma;</b> of second moments at the given device location 
 * for the given beam current using the given RMS envelope data.  The algorithm is started
 * by using the zero-current value covariance matrix as the initialize guess
 * (see <code>{@link #computeCovarianceZeroCurrent(String, ArrayList)}</code>).  The zero-current
 * covariance matrix is passed to the method 
 * <code>{@link #computeCovarianceFiniteCurrent(String, double, CorrelationMatrix, ArrayList)}</code>.
 * </p>
 * <p>
 * A <code>{@link TransferMatrixGenerator}</code> object must be supplied
 * for the construction of one of these objects.  This is done because of
 * the variety of options that exist when creating the transfer matrix
 * generator.  It is safer to require pre-construction of the matrix
 * generator rather than offer all the opions for such generation here.
 * </p>
 * <p>
 * <h4>NOTES:</h4>
 * &middot; Bunch charge <i>Q</i> is given by beam current <i>I</i> divided by
 *          machine frequency <i>f</i>.  Specifically, <i>Q</i> = <i>I</i>/<i>f</i>.
 * <br/>
 * &middot; A <code>{@link TransferMatrixGenerator}</code> object must be supplied
 * for the construction of one of these objects.  This is done because of
 * the variety of options that exist when creating the transfer matrix
 * generator.  It is safer to require pre-construction of the matrix
 * generator rather than offer all the opions for such generation here.
 * </p>
 * 
 * @author Eric Dai
 * @author Christopher K. Allen
 * @since 7/10/2012
 *
 */
public class CsFixedPtEstimator extends CourantSnyderEstimator {

    
    /*
     * Internal Classes
     */
    
    /**
     * Convenience class for storing circular buffers of moment vector objects 
     * by their phase plane.
     *
     * @author Christopher K. Allen
     * @since  Apr 10, 2013
     *
     */
    private class MomentVectorBuffer  extends HashMap<PHASEPLANE, LinearBuffer<Matrix>> {
        
        /*
         * Global Constants
         */
        
        /** Serialization version ID @since Apr 11, 2013 */
        private static final long serialVersionUID = 1L;

        
        /*
         * Initialization
         */
        
        /**
         * Creates a new instance of <code>CovVecBuffer</code> and initiates the
         * circular buffers for each phase plane.
         *
         * @author Christopher K. Allen
         * @since  Apr 10, 2013
         */
        public MomentVectorBuffer() {
            for ( PHASEPLANE plane : PHASEPLANE.values() ) {
                int                     cntSize = plane.getCovariantBasisSize();
                LinearBuffer<Matrix>  bufVec  = new LinearBuffer<Matrix>(cntSize);
                
                this.put(plane, bufVec);
            }
        }
        
        
        /*
         * Operations
         */
        
        /**
         * Convenience method for adding a covariance vector to the set of 
         * vectors managed for the given phase plane.
         *  
         * @param plane     phase plane managed vector set
         * @param vecCov    new vector to add to managed set
         *
         * @author Christopher K. Allen
         * @since  Apr 11, 2013
         */
        public void add(PHASEPLANE plane, Matrix vecCov) {
            super.get(plane).add(vecCov);
        }
        
        /**
         * Initializes the buffer set by inserting the complete set of standard basis
         * vectors for covariance space in each phase plane.
         *
         * @author Christopher K. Allen
         * @since  Apr 10, 2013
         */
        public void initWithBasisVectors() {
            for ( PHASEPLANE plane : PHASEPLANE.values() ) {
                int                     szBasis    = plane.getCovariantBasisSize();
                LinearBuffer<Matrix>  bufCovVecs = this.get(plane);
                
                for (int i=0; i<szBasis; i++) {
                    Matrix  vecCovBasis = new Matrix(szBasis, 1);
                    vecCovBasis.set(i, 0, 1.0);
                    
                    bufCovVecs.add(vecCovBasis);
                }
            }
        }
        
        /**
         * Augments all the vectors in the buffer for the given phase plane into 
         * a matrix of column vectors.  
         * 
         * @param   buffer containing vectors that compose matrix
         *  
         * @return  augmented matrix composed of vectors from the given phase plane buffer
         *
         * @author Christopher K. Allen
         * @since  Apr 11, 2013
         */
        public Matrix   createAugmentedMatrix(PHASEPLANE plane) {
            LinearBuffer<Matrix>    bufVecs = this.get(plane);
            
            // Instantiate the augmented matrix
            int         szRow  = bufVecs.get(0).getRowDimension();
            int         szCol  = bufVecs.size();
            Matrix      matAug = new Matrix(szRow, szCol);
            
            // Pack the matrix with the vectors containing herein
            int         indCol = 0;
            for (Matrix vecCov : bufVecs) {
                
                matAug.setMatrix(0, szRow-1, indCol, indCol, vecCov);
                indCol++;
            }
            
            return matAug;
        }
    };
    

    
    /*
     * Global Constants
     */

    //
    // Default Numerical Parameters
    //
    /** Maximum number of solution iterations */
    public static final int     INT_MAX_ITER = 150;

    /** Maximum residual error in solution */
    public static final double  DBL_MAX_ERR = 1.0e-7;
    
    /** Default value of the fixed point iteration tuning parameter */
    public static final double  DBL_ALPHA = 0.5;
    
    
    
    

	/*
	 * Local Attributes
	 */
    
    //
    // Algorithm Tools
    //
    
    /** Buffers containing recursion function changes - used in approximating recursion function derivative */
    private final MomentVectorBuffer           mapDeltaF;
    
    /** Buffers containing moment vector changes - used in approximating the recursion function derivative */
    private final MomentVectorBuffer           mapDeltaSig;

    
    //
    // Numerical Parameters 
    //
    
    /** Maximum number of iterations allowed */
    private int     cntMaxIter;
    
    /** Maximum residual error allowed */
    protected double dblMaxError;
    
    
    /** Fixed point tuning parameter - normalized distance along convex hull between current iterate and next */
    private double  dblAlpha;
    
    
    
    //
    // Solution Characteristics
    //
    
    /** The last recorded iteration tuning parameter */
    private double              dblCurrAlpha;
    
	/** The number of iterations used to compute the last solution */
    private int                 cntCurrIters;
    
    
    
	/*
	 * Initialization
	 */
	
    /**
     * Creates a new <code>TwissObserver</code> object which uses the given transfer matrix
     * generator.  <em>The new object can only be used for the zero space charge case.</em>
     * @param genTransMatrix
     *
     * @author  Christopher K. Allen
     * @since   Sep 4, 2012
     */
    public CsFixedPtEstimator(TransferMatrixGenerator genTransMatrix) {
        this(INT_MAX_ITER, DBL_MAX_ERR, DBL_ALPHA, genTransMatrix);
    }
    
    /**
     * Creates a new <code>TwissObserver</code> object using the given maximum iteration count,
     * maximum convergence error, and the given transfer matrix generator.  This constructor must
     * be used if Twiss parameters are to be computed in the presence of space charge 
     * (see <code>{@link #computeCovarianceFiniteCurrent(String, double, ArrayList)}</code>).
     *
     * @param cntMaxIter    maximum number of allowed search iterations 
     * @param dblMaxError   maximum <i>L</i><sub>2</sub> convergence error in the solution
     * @param dblAlpha      the default value of the fixed point iteration tuning parameter
     * @param genTransMat   a pre-configured transfer matrix engine used internally.   
     * 
     * @author  Christopher K. Allen
     * @author  Eric Dai
     * @since   Jul 20, 2012
     */
    public CsFixedPtEstimator(int cntMaxIter, double dblMaxError, double dblAlpha, TransferMatrixGenerator genTransMat) {
        super(genTransMat);
        
        this.cntMaxIter  = cntMaxIter;
        this.dblMaxError = dblMaxError;
        this.dblAlpha    = dblAlpha;

        this.mapDeltaF   = new MomentVectorBuffer();
        this.mapDeltaSig = new MomentVectorBuffer();
    }
    
    /**
     * Directly sets the maximum number of iterations allowed for the space charge algorithm.
     * The algorithm is terminated if the iteration count reaches this number.
     * 
     * @param cntMaxIter    maximum number of allowed algorithm iterations
     *
     * @author Christopher K. Allen
     * @since  Nov 13, 2012
     */
    public void setMaxIterations(int cntMaxIter) {
        this.cntMaxIter = cntMaxIter;
    }
    
    /**
     * Directly set the iteration tolerance for the algorithm.  If the residual error of the
     * current solution and the previous solution is less than this value then the algorithm
     * is considered converged and terminates.
     * 
     * @param dblMaxError   the maximum error tolerance for the algorithm
     *
     * @author Christopher K. Allen
     * @since  Nov 13, 2012
     */
    public void setMaxError(double dblMaxError) {
        this.dblMaxError = dblMaxError;
    }
    
    /**
     * The numerical tuning parameter &alpha; in the interval [0,1] which sets the normalized 
     * step size taken between the current solution iterate and the next
     * solution iterate in Method 1.  A value of 0 ignores the new value completely while a value of 1 
     * ignores completely the previous iterate.  This parameter is designed to slow down convergence 
     * for the sake of stability.  Let <b>F</b>(<b>&sigma;</b><sub><i>i</i></sub>) be the iteration
     * map then the next solution iterate <b>&sigma;</b><sub><i>i</i>+1</sub> is
     * <br/>
     * <br/>
     * &nbsp; &nbsp;  <b>&sigma;</b><sub><i>i</i>+1</sub> = (1 - &alpha;)<b>&sigma;</b><sub><i>i</i></sub> 
     *                                                    + &alpha;<b>F</b>(<b>&sigma;</b><sub><i>i</i></sub>) . 
     * <br/>
     * <br/>
     * </p>
     * <p>
     * <h4>NOTES:</h4>
     * &middot; A &lambda; value near 1 can cause a type of "super convergence" instability.  
     * </p>
     *
     * @param dblAlpha     stability/convergence parameter &lambda; &in; [0,1]
     *
     * @author Christopher K. Allen
     * @since  Nov 13, 2012
     */
    public void setSearchTuningValue(double dblAlpha) {
        this.dblAlpha = dblAlpha;
    }
    
    
    /*
     * Queries
     */
    
    /**
     * Returns the number of fixed point iterations needed to compute the last computed
     * solution.
     * 
     * @return  fixed point iterations used to compute the last solution
     *
     * @author Christopher K. Allen
     * @since  Apr 17, 2013
     */
    public int  getSolnIterations() {
        return this.cntCurrIters;
    }
    
    
    /*
     * Operations
     */
    
    /**
     * <p>
     * Computes the covariance matrix <b>&sigma;</b> of second moments at the given device location 
     * for the given beam current using the given RMS envelope data.  The algorithm is started
     * by using the zero-current value covariance matrix as the initialize guess
     * (see <code>{@link #computeCovarianceZeroCurrent(String, ArrayList)}</code>).  The zero-current
     * covariance matrix is passed to the method 
     * <code>{@link #computeCovarianceFiniteCurrent(String, double, CorrelationMatrix, ArrayList)}</code>.
     * </p>
     * <p>
     * <h4>NOTE:</h4>
     * &middot; Bunch charge <i>Q</i> is given by beam current <i>I</i> divided by
     *          machine frequency <i>f</i>.  Specifically, <i>Q</i> = <i>I</i>/<i>f</i>.
     * </p> 
     *
     * @param strRecDevId   ID of the device where the reconstruction is to be performed
     * @param dblBnchChg    amount of bunch charge in Coulombs
     * @param arrData       the profile measurement data used for the reconstruction
     *  
     * @return  block diagonal covariance matrix (uncoupled in the phase planes) containing the second-order
     *          moments of the beam at the reconstruction location
     *
     * @throws ModelException   error occurred during the transfer matrix computations
     * @throws ConvergenceException     failed to meet error tolerance after maximum number of iterations
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2012
     */
    public CorrelationMatrix computeReconstruction(String strRecDevId, double dblBnchChg, ArrayList<Measurement> arrData)
        throws ModelException, ConvergenceException
    {
        
        // Compute the initial position, compute the solution, then return it
        CorrelationMatrix   matSig0 = this.computeZeroCurrReconFunction(strRecDevId, arrData);
        CorrelationMatrix   matSig1 = this.computeReconstruction(strRecDevId, dblBnchChg, matSig0, arrData);
        
        return matSig1;
    }
    

    /**
     * <p>
     * Computes the covariance matrix <b>&sigma;</b> of second moments at the given device location 
     * for the given beam current for the given RMS envelope data.  
     * The covariance matrix is 
     * computed iteratively using the given covariance matrix <var>matSigInit</var> as the 
     * initial guess.  Thus, the closer <var>matSigInit</var> is to the solution (using the
     * Frobenius norm) the faster the algorithm converges. 
     * </p>
     * <p>
     * <h4>NOTE:</h4>
     * &middot; Bunch charge <i>Q</i> is given by beam current <i>I</i> divided by
     *          machine frequency <i>f</i>.  Specifically, <i>Q</i> = <i>I</i>/<i>f</i>.
     * </p> 
     *
     * @param strRecDevId   ID of the device where the reconstruction is to be performed
     * @param dblBnchChg    amount of bunch charge in Coulombs
     * @param matSigInit    the initial covariance matrix (guess) used to start the algorithm 
     * @param arrData       the profile measurement data used for the reconstruction
     *  
     * @return  block diagonal covariance matrix (uncoupled in the phase planes) containing the second-order
     *          moments of the beam at the reconstruction location
     *
     * @throws ModelException   error occurred during the transfer matrix computations
     * @throws ConvergenceException     failed to meet error tolerance after maximum number of iterations
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2012
     */
    public CorrelationMatrix computeReconstruction(
            String strRecDevId, 
            double dblBnchChg, 
            CorrelationMatrix matSigInit, 
            ArrayList<Measurement> arrData
            )
        throws ModelException, ConvergenceException
    {
        // Initialize the delta vector buffers
        this.mapDeltaF.initWithBasisVectors();
        this.mapDeltaSig.initWithBasisVectors();
        
        // Initialize the iterative search
        this.matCurrSigma = matSigInit;
        this.matCurrF     = matSigInit;
        this.dblConvErr   = Double.MAX_VALUE;
        this.dblResErr    = Double.MAX_VALUE;

        int                 cntIter = 0;
        double              dblErr  = this.dblConvErr;
        CorrelationMatrix   matSig0 = matSigInit;

        
        // Keep iterating unless we hit the maximum number
        while (cntIter++ < this.cntMaxIter) {

            // Compute the new covariance matrix from the current one
            CorrelationMatrix   matSig1 = this.iterateNext(matSig0, strRecDevId, dblBnchChg, arrData);

            // Compute the convergence error
            //  If it is less than the maximum return the solution
            super.dblConvErr  = super.computeConvergenceError(matSig1, matSig0);
            super.dblResErr   = super.computeResidualError(matSig1, strRecDevId, arrData);
            
            dblErr = super.dblConvErr;
            
            //  Print out debug info
            if (super.isDebuggingOn()) {
                System.out.println("  Iteration Method:  At iteration# " + cntIter ); 
                System.out.println("    alpha=" + this.dblCurrAlpha + ", residual error=" + this.dblResErr + ", convergence error=" + this.dblConvErr);
                matSig1.print(this.fmtMatrix, 12);
                System.out.println("  -------------------------------------------------\n");
            }

            // Record the iteration count
            this.cntCurrIters = cntIter;
            
            // If the current solution beats the error criteria we're done
            if (dblErr < this.dblMaxError)
                return matSig1;
            
            // Reset the initial covariance matrix and do another iteration
            matSig0 = matSig1;
        }
        
        // We iterated the maximum number of times and did not get below the maximum error
        //  Throw a convergence error
        throw new ConvergenceException("Iterated " + cntIter + " times with convergence error " + dblErr + ". ");
    }
    
    /**
     * Computes the next iterated moment in the finite-space charge second-order
     * moments calculation.  The transfer matrices are generated with the given initial 
     * beam states (RMS Envelopes) and beam current.  The transfer matrices are then used
     * to construct the observation matrix from which the initial Courant-Snyder parameters
     * (equivalently, second-order moments) can be computed. 
     *
     * @param matSig0       initial beam state (i.e., second-order moments) at reconstruction location
     * @param strRecDevId   ID of device where Courant-Snyder parameters are reconstructed
     * @param dblBnchChg    beam bunch charge (in Coulombs)
     * @param arrData       the measured profile data.
     * 
     * @return              the next iterate of second-order moments at the reconstruction location
     * 
     * @throws ModelException   Error in computing the transfer matrices using the online model
     *
     * @author Christopher K. Allen
     * @since  Sep 10, 2012
     */
    private CorrelationMatrix    iterateNext(CorrelationMatrix matSig0, String strRecDevId, double dblBnchChg, ArrayList<Measurement> arrData) 
        throws ModelException 
    {
        
        // Compute the transfer matrices between stations 
        this.genTransMat.generateWithSpaceCharge(null, dblBnchChg, matSig0);

        // Recurse through all the phase planes computing the new value of the recursion function
        Map<PHASEPLANE, Matrix> mapFcurr = new HashMap<PHASEPLANE, Matrix>();
        
        for ( PHASEPLANE plane : PHASEPLANE.values() ) {

            // Compute the new value of the recursion function 
            Matrix  vecFcurr = this.computeReconSubFunction(plane, strRecDevId, arrData);
            mapFcurr.put(plane, vecFcurr);
        }
        
        // Construct the new recursion function value as a covariance matrix 
        Matrix  vecMmtsHor = mapFcurr.get(PHASEPLANE.HOR);
        Matrix  vecMmtsVer = mapFcurr.get(PHASEPLANE.VER);
        Matrix  vecMmtsLng = mapFcurr.get(PHASEPLANE.LNG);
        
        CorrelationMatrix   covF1 = PHASEPLANE.constructCovariance(vecMmtsHor, vecMmtsVer, vecMmtsLng);
        
        // Construct the new covariance matrix computed from the data and the observation matrix
        //  Algorithm for generating new covariance matrix from old
        double              dblAlpha = this.computeAlpha();
        
        PhaseMatrix         matSig1  = covF1.times(dblAlpha).plus( matSig0.times( 1.0 - dblAlpha ) ); 
        CorrelationMatrix   covSig1  = new CorrelationMatrix( matSig1 );

        //  Recurse through all the phase planes computing the change in the recursion function from
        //      this iteration and the last iteration, and the change in the moment vectors that 
        //      produced them.
        for ( PHASEPLANE plane : PHASEPLANE.values() ) {
            
            // Now compute the change in the recursion function value
            Matrix  vecFprev = plane.extractCovarianceVector(this.matCurrF);
            Matrix  vecFcurr = mapFcurr.get(plane);
            Matrix  vecDelF  = vecFcurr.minus(vecFprev);
            this.mapDeltaF.add(plane, vecDelF);
            
            // Compute the change in the moments that produces the recursion function change
            Matrix  vecSigPrev = plane.extractCovarianceVector(this.matCurrSigma);
            Matrix  vecSigCurr = plane.extractCovarianceVector(covSig1);
            Matrix  vecDelSig  = vecSigCurr.minus(vecSigPrev);
            this.mapDeltaSig.add(plane, vecDelSig);
        }
        
        // Record the current solution iterates
        this.dblCurrAlpha = dblAlpha;
        super.matCurrF     = covF1;
        super.matCurrSigma = covSig1;
        
        // Return the current solution iterate
        return covSig1;
    }
    
    /**
     * Computes the iteration tuning parameter from the previous changes in the covariance
     * moments and the correpsonding changes in the recursion function.
     * 
     * @return  new value of the tuning parameter that accelerates convergence
     *
     * @author Christopher K. Allen
     * @since  Apr 12, 2013
     */
    private double  computeAlpha() {
        double      dblAlpha = this.dblAlpha;
        
        for (PHASEPLANE plane : PHASEPLANE.values()) {
            
            // Create the augmented change in values matrices
            Matrix  matDelF   = this.mapDeltaF.createAugmentedMatrix(plane);
            Matrix  matDelSig = this.mapDeltaSig.createAugmentedMatrix(plane);
            
            // Create an identity matrix
            int     cntRows = matDelSig.getRowDimension();
            int     cntCols = matDelSig.getColumnDimension();
            
            Matrix  matIden = Matrix.identity(cntRows, cntCols);
            
            // Check the rank of the change in sigma values matrix
            //  If not of full rank, we need to skip this plane
            int     szRank  = matDelSig.rank();
            
            if (szRank < cntRows) 
                continue;
            
            // Compute the approximation to the partial of the recusion function
            Matrix  matDelSigInv = matDelSig.inverse();
            Matrix  matFpApprox  = matDelF.times(matDelSigInv);
            
            // Compute the alpha value for this plane
            Matrix  matResolv = matIden.minus(matFpApprox);
            double  dblNumer  = Math.abs( matResolv.trace() );
            double  dblDenom  = matResolv.normF();
            
            double  dblAlphaNew = dblNumer/(dblDenom);
            
            if (dblAlphaNew > dblAlpha)
                dblAlpha = dblAlphaNew;
            
//            if (this.bolDebug) {
//                System.out.println("\n  PHASEPLANE=" + plane + ", dblNumer=" + dblNumer + ", dblDenom=" + dblDenom);
//                System.out.println("  ----Delta F---");
//                matDelF.print(this.fmtMatrix, 10);
//                System.out.println("\n  ----Delta Sigma---");
//                matDelSig.print(this.fmtMatrix, 10);
//                System.out.println("\n  ----F' approx---- ");
//                matFpApprox.print(this.fmtMatrix, 10);
//                System.out.println("\n  ----Resolve(F)--- ");
//                matResolv.print(this.fmtMatrix, 10);
//            }
        }
        
        return dblAlpha;
//        return this.dblAlpha;
    }
    
}
