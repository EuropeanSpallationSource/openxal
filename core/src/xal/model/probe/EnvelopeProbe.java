/*
 * EnvelopeProbe.java
 *
 * Created on August 13, 2002, 4:20 PM
 */

package xal.model.probe;

import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.EnvelopeTrajectory;
import xal.model.probe.traj.ProbeState
;
import xal.model.xml.ParsingException;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.data.DataAdaptor;

/**
 * <p>
 * <code>EnvelopeProbe</code> represents the RMS beam envelopes of a beam.
 * Specifically, its primary state object is the 7&times;7 matrix of 
 * homogeneous phase
 * space moments up to, and including second order. This is the covariance
 * matrix for the beam and is represented as
 * <br/>
 * <br/>
 * &nbsp; <b>&tau;</b> &equiv; &lt;<b>z*z</b><i><sup>T</sup></i>&gt;
 * <br/>
 * <br/>
 * where <b>z</b>=(<i>x,x',y,y',z,z'</i>,1) is the vector of homogeneous phase space
 * coordinates, and &lt; &middot; &gt; is the moment operator with respect to the beam
 * distribution.  We reserve the symbol <b>&sigma;</b> for the <em>central</em>
 * covariance matrix, which is defined
 * <br/>
 * <br/>
 * &nbsp; <b>&sigma;</b> &equiv; <b>&tau;</b> - &lt<b>z</b>&gt;&lt<b>z</b>&gt;<i><sup>T</sup></i>
 * <br/>
 * <br/>
 * Note that the centroid position = &lt;<b>z</b>&gt; is carried in the last row
 * and column of <b>&tau;</b> 
 * </p>
 * 
 * 
 * @author Christopher K. Allen
 * @author Craig McChesney
 * 
 * @since   August, 2002
 * @version 4
 * 
 * @see xal.model.alg.EnvelopeTrackerBase
 * @see xal.model.probe.traj.EnvelopeProbeState
 */

public class EnvelopeProbe extends BunchProbe {

    
    
    
    /*
     * Global Methods
     */

    
    /**
     * Probe factory convenient method - clone given probe.  
     * 
     * The real work here is being done in the base class <code>Probe</code> 
     * by the static factory method of the same name.  The current method
     * just ensure type safety.
     * 
     * @param   probe   probe object to be cloned.
     * 
     * @return      a clone of the given probe argument
     */
    public static EnvelopeProbe newInstance( final EnvelopeProbe probe ) {
        return (EnvelopeProbe) Probe.newProbeInitializedFrom(probe);
    }
    
    
    /*
	 * Local Attributes
	 */

	/** first-order response matrix from the initial state */
	private PhaseMatrix        matResp;
    
	/** first-order response matrix from the initial state (no space charge) */
	private PhaseMatrix        matRespNoSpaceCharge;
   
    
    /** first-order response matrix of current slice of element **/
    private PhaseMatrix        matPert;

	/** envelope state */
	private CovarianceMatrix  matSigam;

//    /** 
//     * <p>
//     * The twiss parameters calculated from the transfer matrix 
//     * (not calculated from the correlation matrix, except for
//     * the initialization)
//     * </p>
//     * <p>
//     * <strong>NOTES</strong>: (CKA)
//     * <br/>
//     * &middot; This attribute is redundant in the sense that all "Twiss parameter"
//     * information is contained within the correlation matrix.  The correlation
//     * matrix was intended as the primary attribute for an <code>EnvelopeProbe</code>.
//     * <br/>
//     * &middot; The dynamics of this attribute are computed from transfer matrices,
//     * however, with space charge the transfer matrices are computed using the
//     * correlation matrix.  Thus, these parameters are inconsistent in the 
//     * presence of space charge.
//     * <br/>
//     * &middot; I have made a separate Probe class, <code>TwissProbe</code> which has
//     * Twiss parameters as its primary state.
//     * </p>
//     * <em>For all these reasons I am deprecating this attribute</em>
//     * 
//     * @deprecated this is a redundant state variable
//     */
//	@Deprecated
//    private Twiss[]            arrTwiss;


    /** 
     * behavior flag - save correlation data as twiss parameters 
     * 
     * CKA NOTES:
     * - As this attribute relates to the above attributes, I am 
     * deprecating it as well.
     * 
     * @deprecated  associated with attribute <code>arrTwiss</code>
     */
	@Deprecated
    private boolean            bolSaveTwiss = false;

    

	/*
	 * Initialization
	 */

	/**
	 * Default Constructor. Creates a new, empty instance of EnvelopeProbe
	 */
	public EnvelopeProbe() {
		this.matResp = PhaseMatrix.identity();
		this.matRespNoSpaceCharge = PhaseMatrix.identity();
		this.matPert  = PhaseMatrix.identity();
		this.matSigam = CovarianceMatrix.newIdentity();
	};

	/**
	 * Copy constructor - clones the argument
	 * 
	 * @param probe
	 *            <code>EnvelopeProbe</code> object to be cloned
	 */
	public EnvelopeProbe(EnvelopeProbe probe) {
		super(probe);

        //PhaseMatrix copy constructor does a deep copy
		this.setResponseMatrix(new PhaseMatrix(probe.getResponseMatrix()));
		this.setResponseMatrixNoSpaceCharge(new PhaseMatrix(probe.getResponseMatrixNoSpaceCharge()));
		this.setCurrentResponseMatrix(new PhaseMatrix(probe.getCurrentResponseMatrix()));
//		this.setBetatronPhase(new R3(probe.getBetatronPhase()));
		this.setCovariance(new CovarianceMatrix(probe.getCovariance()));
	};
    
    /**
     * Create a deep copy of this probe with all state information.
     *
     * @see xal.model.probe.Probe#copy()
     *
     * @author Christopher K. Allen
     * @since  Oct 23, 2013
     */
    @Override
    public EnvelopeProbe copy() {
        return new EnvelopeProbe( this );
    }
    
    /**
     * Set the twiss parameters for each phase plane.
     * 
     * CKA NOTES:
     * - The current method signature is misleading.  If there is
     * an beam axis offset before this method is called, then that
     * offset is preserved, but the previous correlation matrix is
     * wiped out.  Thus, even though the method signature suggests
     * there will be no offset, there can be.
     * 
     * @param twiss
     *            array of Twiss objects for H, V , long. directions
     */
    public void initFromTwiss(Twiss[] twiss) {
//        this.arrTwiss = twiss;
        PhaseVector pv = getCovariance().getMean();
        CovarianceMatrix cMat = CovarianceMatrix.buildCovariance(twiss[0],
                twiss[1], twiss[2], pv);
        this.setCovariance(cMat);
    }

    /**
     * Initialize this probe from the one specified.
     * @param probe the probe from which to initialize this one
     */
    @Override
    protected void initializeFrom( final Probe probe ) {
        super.initializeFrom( probe );
        
        applyState( probe.createProbeState() );
        createTrajectory();
    }


    /**
     * Set the Twiss parameters storage flag.
     * </p>
     * <p>
     * Changes the behavior of the save state methods.
     * By setting this flag to <code>true</code> the Twiss
     * parameter attributes will be saved <b>instead</b> of
     * the correlation matrix.  The default behavior for this class
     * is to save the correlation matrix.
     * </p>
     * <p>
     * <h4>CKA Notes:</h4>
     * - This is clearly a kluge; use this method with caution.
     * It is provided to maintain backward compatibility.
     * <br/>
     * - There is another version of code (this version) where the
     * correlation matrix is saved as three sets of Twiss parameters.
     * <br/>
     * o This can be dangerous as we have the 
     * potential to loose a lot of information.  In particular,
     * if the probe has pasted through a bend or a steering
     * magnet, the Twiss parameters do not contain enough information
     * to restart the probe.
     * </p>
     * <p>
     *  Because of all of these dangers, the method is here, but 
     *  deprecated.
     * </p>
     * 
     * @param   bolSaveTwiss    Twiss parameter save flag
     * 
     * @see Probe#save(DataAdaptor)
     * @see Probe#applyState(ProbeState)
     * 
     * @deprecated  If you want Twiss parameters, either take them from 
     *              the covariance matrix or use the <code>TwissProbe</code>
     */
    @Deprecated
    public void setSaveTwissFlag(boolean bolSaveTwiss)    {
        this.bolSaveTwiss = bolSaveTwiss;
    }
    
    /**
	 * Set the correlation matrix for this probe (7x7 matrix in homogeneous
	 * coordinates).
	 * 
	 * @param matSigam  new phase space covariance matrix of this probe
	 * 
	 * @see xal.tools.beam.CovarianceMatrix
	 */
	public void setCovariance(CovarianceMatrix matTau) {
		this.matSigam = matTau;
	};

//	/**
//	 * Set the twiss parameters for each phase plane.
//     * 
//     * CKA NOTES:
//     * 10/06 - Changed from protected to public
//     *         Algorithms should have access to Twiss Parameters
//	 * 
//	 * @param twiss
//	 *            array of Twiss objects for H, V , long. directions
//     *            
//     * @see EnvelopeProbe#getTwiss()
//     * 
//     * @deprecated
//	 */
//	@Deprecated
//    public void setTwiss(Twiss[] twiss) {
//		this.arrTwiss = twiss;
//	}


	/**
	 * Set the first-order response matrix accumulated by the Envelope since its
	 * initial state. Note that this response includes the effects of space
	 * charge.
	 * 
	 * @param matResp
	 *            first-order response matrix in homogeneous coordinates
	 */
	public void setResponseMatrix(PhaseMatrix matResp) {
		this.matResp = matResp;
	}
	
	

	/**
	 * Set the first-order response matrix accumulated by the Envelope since its
	 * initial state. Note that this response includes the effects of space
	 * charge.
	 * 
	 * @param matResp
	 *            first-order response matrix in homogeneous coordinates
	 */
	public void setResponseMatrixNoSpaceCharge(PhaseMatrix matResp) {
		this.matRespNoSpaceCharge = matResp;
	}

    /**
     * Set the current factor of the overall response matrix.  This is the 
     * last factor post multiplied onto the response matrix. 
     * 
     * @param matRespCurr   current response matrix factor
     */
	public void setCurrentResponseMatrix(PhaseMatrix matRespCurr) {
		this.matPert = matRespCurr;
	}

    
	/*
	 * Data Query
	 */

    
    /**
     * Returns the correlation matrix for the beam in homogeneous
     * phase space coordinates.  This is the primary state object for
     * an <code>EnvelopeProbe</code> object.
     * 
     * @return  the 7x7 matrix <z*z^T> in homogeneous coordinates
     */
    public CovarianceMatrix getCovariance() {
        return matSigam;
    }
	/**
	 * Get the first-order response matrix accumulated by the Envelope since its
	 * initial state. Note that this response includes the effects of space
	 * charge.
	 * 
	 * @return first-order response matrix in homogeneous coordinates
	 */
	public PhaseMatrix getResponseMatrix() {
		return this.matResp;
	}
	/**
	 * Get the first-order response matrix accumulated by the Envelope since its
	 * initial state. Note that this response does not include the effects of space
	 * charge.
	 * 
	 * @return first-order response matrix in homogeneous coordinates
	 */
	public PhaseMatrix getResponseMatrixNoSpaceCharge() {
		return this.matRespNoSpaceCharge;
	}

    /**
     * Return the last element in the semigroup of response matrices, 
     * that is, the last matrix to be post-multiplied onto the response 
     * matrix proper.
     * 
     * @return  last factor of the response matrix
     */
	public PhaseMatrix getCurrentResponseMatrix() {
		return this.matPert;
	}


//    /** 
//     * Returns an array of Twiss objects for the probe
//     *  
//     * CKA NOTES:
//     * - This attribute is redundant in the sense that all "Twiss parameter"
//     * information is contained within the correlation matrix.  The correlation
//     * matrix was intended as the primary attribute of an <code>EnvelopeProbe</code>.
//     * 
//     * - The dynamics of this attribute are computed from transfer matrices,
//     * however, with space charge the transfer matrices are computed using the
//     * correlation matrix.  Thus these parameters are inconsistent in the 
//     * presence of space charge.
//     * 
//     * - I have made a separate Probe class, <code>TwissProbe</code> which has
//     * Twiss parameters as its primary state.
//     * 
//     * - For all these reason I am deprecating this method
//     * 
//     * @return array(twiss-H, twiss-V, twiss-L)
//     * 
//     * @deprecated
//     */
//    @Deprecated
//    public Twiss[] getTwiss() {
//        return arrTwiss;
//    }

    /**
     * Return the save Twiss parameters flag.  If this flag is set then
     * only the Twiss parameters are saved to a <code>DataAdaptor</code>
     * object.
     * NOTES: 
     * This can be dangerous as we have the 
     * potential to loss a lot of information.  In particular,
     * if the probe has pasted through a bend or a steering
     * magnet, the Twiss parameters do not contain enough information
     * to restart the probe. 
     * 
     * @return Twiss parameter save flag
     * 
     * @see Probe#save(DataAdaptor)
     * @see Probe#applyState(ProbeState)
     * 
     * @deprecated
     */
    @Deprecated
    public boolean getSaveTwissFlag()   {
        return this.bolSaveTwiss;
    }
    
    
    /*
     * Computed Parameters
     */
    
    /**
     *  Return the covariance matrix of the distribution.  Note that this can be computed
     *  from the correlation matrix in homogeneous coordinates since the mean values are 
     *  included in that case.
     *
     *  @return     <(z-<z>)*(z-<z>)^T> = <z*z^T> - <z>*<z>^T
     */
    public CovarianceMatrix  phaseCovariance() {
        return getCovariance().computeCentralCovariance();
    }
    
    /** 
     *  Return the phase space coordinates of the centroid in homogeneous coordinates 
     *
     *  @return         <z> = (<x>, <xp>, <y>, <yp>, <z>, <zp>, 1)^T
     */
    public PhaseVector phaseMean()  {
        return getCovariance().getMean();
    }

    
    /**
     * Returns the state response matrix calculated from the front face of
     * elemFrom to the back face of elemTo. This is a convenience wrapper to
     * the real method in the trajectory class
     * 
     * @param elemFrom  String identifying starting lattice element
     * @param elemTo    String identifying ending lattice element
     * 
     * @return      response matrix from elemFrom to elemTo
     * 
     * @see EnvelopeTrajectory#stateResponse(String, String)
     */
    public PhaseMatrix stateResponse(String elemFrom, String elemTo) {
        return ((EnvelopeTrajectory) this.getTrajectory()).stateResponse(
                elemFrom, elemTo);
    }

    
    
	/*
	 * Trajectory Support
	 */

	/**
	 * Creates a snapshot of the current state and returns it as a
	 * <code>ProbeState</code> object of the proper type.
	 * 
	 * @return a new <code>EnvelopeProbeState</code> encapsulating the probe's
	 *         current state
	 */
    @Override
	public EnvelopeProbeState createProbeState() {
		return new EnvelopeProbeState(this);
	}

	/**
	 * Creates a <code>Trajectory</code> object of the proper type for saving
	 * the probe's history.
	 * 
	 * @return a new, empty <code>EnvelopeTrajectory</code> object
	 */
    @Override
	public EnvelopeTrajectory createTrajectory() {
		return new EnvelopeTrajectory();
	}

	/**
	 * Apply the contents of ProbeState to update my current state. The argument
	 * supplying the new state should be of concrete type
	 * <code>EnvelopeProbeState</code>.
	 * 
	 * @param state
	 *            <code>ProbeState</code> object containing new probe state
	 *            data
	 * 
	 * @exception IllegalArgumentException
	 *                wrong <code>ProbeState</code> subtype for this probe
	 */
    @SuppressWarnings("deprecation")
    @Override
	public void applyState(ProbeState state) {
		if (!(state instanceof EnvelopeProbeState))
			throw new IllegalArgumentException("invalid probe state");
		EnvelopeProbeState stateEnv = (EnvelopeProbeState) state;

		super.applyState(stateEnv);
		this.setCovariance(stateEnv.getCovarianceMatrix());
		this.setResponseMatrix(stateEnv.getResponseMatrix());
		this.setResponseMatrixNoSpaceCharge(stateEnv.getResponseMatrixNoSpaceCharge());
        this.setCurrentResponseMatrix(stateEnv.getPerturbationMatrix());
		//obsolete this.setTwiss(stateEnv.getTwiss());
//        this.setTwiss(stateEnv.twissParameters());
        this.setSaveTwissFlag(stateEnv.getSaveTwissFlag());
	}

    
    /**
     * Resets the probe to the saved initial state, if there is one and clears
     * the Trajectory.
     */
    @Override
    public void reset() {
        super.reset();
        if (getAlgorithm() instanceof EnvTrackerAdapt)
            try {
                getAlgorithm().initialize();
                
            } catch (ModelException e) {
                System.err.println("EnvelopeProbe#reset() - Unable to initialize algorithm");
                e.printStackTrace();
                
            }
    }
    
//    /**
//     * Save the state values particular to <code>EnvelopeProbe</code> objects
//     * to the data sink.  In particular we save only the data in the 2x2 diagonal
//     * blocks of the correlation matrix, and as Twiss parameters.
//     * 
//     * CKA NOTE:
//     * - <b>Be careful</b> when using this method!  It is here as a convenience.  It
//     * saves the <code>EnvelopeProbe</code> information in the save format as
//     * the load()/save() methods do, but you cannot restore an <code>EnvelopeProbe</code>
//     * object from these data.
//     * 
//     *  @param  daSink   data sink represented by <code>DataAdaptor</code> interface
//     */
//    public void saveStateAsTwiss(DataAdaptor daSink) {
//        EnvelopeProbeState state = this.createProbeState();
//        
//        state.saveStateAsTwiss(daSink);
//    }
    
    
//    /**
//     *  Save the contents of a probe to a data archive represented by a 
//     *  <code>DataAdaptor</code> interface.
//     *
//     *  @param  daSink      data archive to receive probe information
//     */
//    public void saveAsTwiss(DataAdaptor daSink) {
//        
//        DataAdaptor daProbe = daSink.createChild(Probe.PROBE_LABEL);
//        
//        // Save the probe type information and time stamp
//        DateFormat  frmDate = DateFormat.getDateTimeInstance();
//
//        if (this.getTimestamp() == null)
//            this.setTimestamp(new Date());
//        
//        daProbe.setValue(Probe.TYPE_LABEL, this.getClass().getName());
//        daProbe.setValue(Probe.TIME_LABEL, frmDate.format( this.getTimestamp() ) );
//        
//        // Save the comment
//        DataAdaptor daptComm = daProbe.createChild(Probe.COMMENT_LABEL);
//        daptComm.setValue(Probe.TEXT_LABEL, this.getComment() );
//        
//        // Save the algorithm type
//        this.getAlgorithm().save(daProbe);
//                
//        // Save the probe state information
//        EnvelopeProbeState state = createProbeState();
//        state.saveStateAsTwiss(daProbe);  
//    }
        
        
//  // All this functionality was moved to the TwissProbe/TwissTracker component.
//  // EnvelopeProbes do not concern themselves with Twiss parameters
//
//    /**
//	 * Advance the twiss parameters using the present response matrix Use
//	 * formula 2.54 from S.Y. Lee's book
//     * 
//     * CKA NOTES:
//     * - I believe this method will only work correctly for a beam that is
//     * unccorrelated in the phase planes.
//     * 
//     * - This method should not really be here.  According to the 
//     * Element/Algorithm/Probe architecture the probe should not know
//     * anything about the element.  It really belongs in the Algorithm
//     * object.
//     * 
//     * - This is a somewhat redundant feature, since all the Twiss 
//     * parameters are part of the correlation matrix and the Algorithm
//     * object is normally used to propagate the correlation matrix
//     * (which is part of the probe "state").
//     *  
//	 * 
//	 * @param R    the transfer matrix of the element
//	 * @param dW   the energy gain of this element (eV)
//     * 
//     * @deprecated  This should not be public, it should not be in the probe, 
//     *              and the calculations are wrong
//	 */
//	@Deprecated
//    public void advanceTwiss(PhaseMatrix R, double dW) {
//
//		//obsolete Twiss[] twissOld = getTwiss();
//		Twiss[] twissOld = getCovariance().computeTwiss();
//		Twiss[] twissNew = new Twiss[3];
//
//        // Relativistic parameter ratios
//        double ratio;       // transverse plane emittance growth
//        double ratio2;      // longitudinal plane emittance growth
//        
//        if (dW == 0.0)  {
//            ratio  = 1.0;
//            ratio2 = 1.0;
//            
//        } else  {
//            
//            double gammaRelativisticOld = this.getGamma();
//            double betaRelativisticOld = this.getBeta();
//            double gammaRelativisticNew = this.computeGammaFromW(getKineticEnergy() + dW);
//            double betaRelativisticNew = this.computeBetaFromGamma(gammaRelativisticNew);
//            ratio = gammaRelativisticOld * betaRelativisticOld/(betaRelativisticNew * gammaRelativisticNew);
//            //sako, 21 Jul 06 to optimize speed double ratio2 = betaRelativisticOld * Math.pow(gammaRelativisticOld, 3.) / (betaRelativisticNew  * Math.pow(gammaRelativisticNew, 3.) );
//            ratio2 = betaRelativisticOld * gammaRelativisticOld*gammaRelativisticOld*gammaRelativisticOld
//            / (betaRelativisticNew  * gammaRelativisticNew*gammaRelativisticNew*gammaRelativisticNew );
//            
//        }
//        
//        
//        double alpha, beta;
//		double alphaOld, betaOld, gammaOld, emit;
//		
//		int j = 0;
//		for (int i = 0; i < 3; i++) {
//			j = 2 * i;
//			// assume constant normalized emittance
//			alphaOld = twissOld[i].getAlpha();
//			betaOld = twissOld[i].getBeta();
//			gammaOld = twissOld[i].getGamma();
//			if (i==2) 
//				emit = twissOld[i].getEmittance() * ratio2; // longitudinal plane
//			else
//				emit = twissOld[i].getEmittance() * ratio;
//
//			/* sako, 21 Jul 06 to optimize speed
//			beta = Math.pow(R.getElem(j, j), 2) * betaOld - 2.
//					* R.getElem(j, j) * R.getElem(j, j + 1) * alphaOld
//					+ Math.pow(R.getElem(j, j + 1), 2) * gammaOld;
//			alpha = -R.getElem(j, j)
//					* R.getElem(j + 1, j)
//					* betaOld
//					+ (R.getElem(j, j) * R.getElem(j + 1, j + 1) + R.getElem(j,
//							j + 1)
//							* R.getElem(j + 1, j)) * alphaOld
//					- R.getElem(j, j + 1) * R.getElem(j + 1, j + 1) * gammaOld;
//					*/
////			double Rij = R.getElem(i, j);
//			double Rjj = R.getElem(j, j);
//			double Rjjp = R.getElem(j, j+1);
//			double Rjpj = R.getElem(j+1, j);
//			double Rjpjp = R.getElem(j+1,j+1);
//			
//            // CKA - corrected 12/04/06
//			beta = Rjj*Rjj * betaOld - 2.*Rjj*Rjjp * alphaOld + Rjjp*Rjjp*gammaOld;
//			
//			alpha = -Rjj*Rjpj*betaOld + (Rjj*Rjpjp + Rjjp*Rjpj)*alphaOld
//					- Rjjp*Rjpjp*gammaOld;
//
//			twissNew[i] = new Twiss(alpha, beta, emit);
//		}
//		
//		this.setTwiss(twissNew);
//
//	}

    
//    /**
//     * hs new method to update twiss using CovarianceMatrix
//     * 
//     * CKA NOTES:
//     * This method is taking the Twiss parameters directly from
//     * the given correlation matrix.
//     * 
//     * @deprecated
//     */
//    @Deprecated
//    public void updateTwiss(CovarianceMatrix Cor) {
//        
//    	//obsolete Twiss[] twissOld = getTwiss();
//    	Twiss[] twissOld = this.getCovariance().computeTwiss();
//       	Twiss[] twissNew = new Twiss[3];   	
//    	for (int i=0;i<3;i++) {
//      		double emit = twissOld[i].getEmittance();
//    		double beta = Cor.getElem(i*2,i*2)/emit;
//    		double alpha = -Cor.getElem(i*2,i*2+1)/emit;
////    		double gamma = Cor.getElem(i*2+1,i*2+1)/emit;
//  
//    		twissNew[i] = new Twiss(alpha,beta,emit);
//    	}
// 	
//    	this.setTwiss(twissNew);
//    }
    
    /**
     *
     * @see xal.model.probe.Probe#readStateFrom(xal.tools.data.DataAdaptor)
     *
     * @author Christopher K. Allen
     * @version  Oct 31, 2013
     */
    @Override
    protected ProbeState readStateFrom(DataAdaptor container) throws ParsingException {
        EnvelopeProbeState state = new EnvelopeProbeState();
        state.load(container);
        return state;
    }
}
