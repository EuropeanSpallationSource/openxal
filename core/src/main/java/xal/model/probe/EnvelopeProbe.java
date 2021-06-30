/*
 * EnvelopeProbe.java
 *
 * Created on August 13, 2002, 4:20 PM
 */
package xal.model.probe;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;

/**
 * <p>
 * <code>EnvelopeProbe</code> represents the RMS beam envelopes of a beam.
 * Specifically, its primary state object is the 7&times;7 matrix of homogeneous
 * phase space moments up to, and including second order. This is the covariance
 * matrix for the beam and is represented as
 * <br>
 * <br>
 * &nbsp; <strong>&tau;</strong> &equiv;
 * &lt;<strong>z*z</strong><em><sup>T</sup></em>&gt;
 * <br>
 * <br>
 * where <strong>z</strong>=(<em>x,x',y,y',z,z'</em>,1) is the vector of
 * homogeneous phase space coordinates, and &lt; &middot; &gt; is the moment
 * operator with respect to the beam distribution. We reserve the symbol
 * <strong>&sigma;</strong> for the
 * <em>central</em>
 * covariance matrix, which is defined
 * <br>
 * <br>
 * &nbsp; <strong>&sigma;</strong> &equiv; <strong>&tau;</strong> -
 * &lt;<strong>z</strong>&gt;&lt;<strong>z</strong>&gt;<em><sup>T</sup></em>
 * <br>
 * <br>
 * Note that the centroid position = &lt;<strong>z</strong>&gt; is carried in
 * the last row and column of <strong>&tau;</strong>
 * </p>
 *
 *
 * @author Christopher K. Allen
 * @author Craig McChesney
 *
 * @since August, 2002
 * @version 4
 *
 * @see xal.model.alg.EnvelopeTrackerBase
 * @see xal.model.probe.traj.EnvelopeProbeState
 */
public class EnvelopeProbe extends BunchProbe<EnvelopeProbeState> {

    private static final Logger LOGGER = Logger.getLogger(EnvelopeProbe.class.getName());

    /*
     * Global Methods
     */
    /**
     * Probe factory convenient method - clone given probe.
     *
     * The real work here is being done in the base class <code>Probe</code> by
     * the static factory method of the same name. The current method just
     * ensure type safety.
     *
     * @param probe probe object to be cloned.
     *
     * @return a clone of the given probe argument
     */
    public static EnvelopeProbe newInstance(final EnvelopeProbe probe) {
        return (EnvelopeProbe) Probe.newProbeInitializedFrom(probe);
    }

    /*
	 * Initialization
     */
    /**
     * Default Constructor. Creates a new, empty instance of EnvelopeProbe
     */
    public EnvelopeProbe() {
        super();

        this.setResponseMatrix(PhaseMatrix.identity());
        this.setResponseMatrixNoSpaceCharge(PhaseMatrix.identity());
        this.setCurrentResponseMatrix(PhaseMatrix.identity());
        this.setCovariance(CovarianceMatrix.newIdentity());
    }

    /**
     * Copy constructor - clones the argument
     *
     * @param probe <code>EnvelopeProbe</code> object to be cloned
     */
    public EnvelopeProbe(final EnvelopeProbe probe) {
        super(probe);

        //PhaseMatrix copy constructor does a deep copy
        this.setCovariance(probe.getCovariance().clone());
        this.setResponseMatrix(probe.getResponseMatrix().clone());
        this.setResponseMatrixNoSpaceCharge(probe.getResponseMatrixNoSpaceCharge().clone());
        this.setCurrentResponseMatrix(probe.getCurrentResponseMatrix().clone());
    }

    /**
     * Create a deep copy of this probe with all state information.
     *
     * @see xal.model.probe.Probe#copy()
     *
     * @author Christopher K. Allen
     * @since Oct 23, 2013
     */
    @Override
    public EnvelopeProbe copy() {
        return new EnvelopeProbe(this);
    }

    /**
     * Set the Twiss parameters for each phase plane.
     *
     * CKA NOTES: - The current method signature is misleading. If there is an
     * beam axis offset before this method is called, then that offset is
     * preserved, but the previous correlation matrix is wiped out. Thus, even
     * though the method signature suggests there will be no offset, there can
     * be.
     *
     * @param twiss array of Twiss objects for H, V , long. directions
     */
    public void initFromTwiss(Twiss[] twiss) {
        PhaseVector pv = getCovariance().getMean();
        CovarianceMatrix cMat = CovarianceMatrix.buildCovariance(twiss[0],
                twiss[1], twiss[2], pv);
        this.setCovariance(cMat);
    }

    /**
     * Initialize this probe from the one specified.
     *
     * @param probe the probe from which to initialize this one
     *
     * @deprecated Never used
     */
    @Deprecated
    @Override
    protected void initializeFrom(final Probe<EnvelopeProbeState> probe) {
        super.initializeFrom(probe);

        applyState(probe.cloneCurrentProbeState());
        createTrajectory();
    }

    /**
     * Set the correlation matrix for this probe (7x7 matrix in homogeneous
     * coordinates).
     *
     * @param matTau new phase space covariance matrix of this probe
     *
     * @see xal.tools.beam.CovarianceMatrix
     */
    public void setCovariance(CovarianceMatrix matTau) {
        this.stateCurrent.setCovariance(matTau);
    }

    /**
     * Set the first-order response matrix accumulated by the Envelope since its
     * initial state. Note that this response includes the effects of space
     * charge.
     *
     * @param matResp first-order response matrix in homogeneous coordinates
     */
    public void setResponseMatrix(PhaseMatrix matResp) {
        this.stateCurrent.setResponseMatrix(matResp);
    }

    /**
     * Set the first-order response matrix accumulated by the Envelope since its
     * initial state. Note that this response includes the effects of space
     * charge.
     *
     * @param matResp first-order response matrix in homogeneous coordinates
     */
    public void setResponseMatrixNoSpaceCharge(PhaseMatrix matResp) {
        this.stateCurrent.setResponseMatrixNoSpaceCharge(matResp);
    }

    /**
     * Set the current factor of the overall response matrix. This is the last
     * factor post multiplied onto the response matrix.
     *
     * @param matRespCurr current response matrix factor
     */
    public void setCurrentResponseMatrix(PhaseMatrix matRespCurr) {
        this.stateCurrent.setPerturbationMatrix(matRespCurr);
    }

    /*
	 * Data Query
     */
    /**
     * Returns the correlation matrix for the beam in homogeneous phase space
     * coordinates. This is the primary state object for an
     * <code>EnvelopeProbe</code> object.
     *
     * @return the 7x7 matrix &lt;z*z^T&gt; in homogeneous coordinates
     */
    public CovarianceMatrix getCovariance() {
        return this.stateCurrent.getCovarianceMatrix();
    }

    /**
     * Get the first-order response matrix accumulated by the Envelope since its
     * initial state. Note that this response includes the effects of space
     * charge.
     *
     * @return first-order response matrix in homogeneous coordinates
     */
    public PhaseMatrix getResponseMatrix() {
        return this.stateCurrent.getResponseMatrix();
    }

    /**
     * Get the first-order response matrix accumulated by the Envelope since its
     * initial state. Note that this response does not include the effects of
     * space charge.
     *
     * @return first-order response matrix in homogeneous coordinates
     */
    public PhaseMatrix getResponseMatrixNoSpaceCharge() {
        return this.stateCurrent.getResponseMatrixNoSpaceCharge();
    }

    /**
     * Return the last element in the semigroup of response matrices, that is,
     * the last matrix to be post-multiplied onto the response matrix proper.
     *
     * @return last factor of the response matrix
     */
    public PhaseMatrix getCurrentResponseMatrix() {
        return this.stateCurrent.getPerturbationMatrix();
    }

    /*
     * Computed Parameters
     */
    /**
     * Return the covariance matrix of the distribution. Note that this can be
     * computed from the correlation matrix in homogeneous coordinates since the
     * mean values are included in that case.
     *
     * @return &lt;(z-&lt;z&gt;)*(z-&lt;z&gt;)^T&gt; = &lt;z*z^T&gt; -
     * &lt;z&gt;*&lt;z&gt;^T
     */
    public CovarianceMatrix phaseCovariance() {
        return this.stateCurrent.centralCovariance();
    }

    /**
     * Return the phase space coordinates of the centroid in homogeneous
     * coordinates
     *
     * @return &lt;z&gt; = (&lt;x&gt;, &lt;xp&gt;, &lt;y&gt;, &lt;yp&gt;,
     * &lt;z&gt;, &lt;zp&gt;, 1)^T
     */
    public PhaseVector phaseMean() {
        return this.stateCurrent.phaseMean();
    }

    /**
     * Returns the state response matrix calculated from the front face of
     * elemFrom to the back face of elemTo. This is a convenience wrapper to the
     * real method in the trajectory class
     *
     * @param elemFrom String identifying starting lattice element
     * @param elemTo String identifying ending lattice element
     *
     * @return response matrix from elemFrom to elemTo
     *
     * @see EnvelopeTrajectory#computeTransferMatrix(String, String)
     *
     * @deprecated This calculation should be done using the utility class
     * xal.tools.beam.calc.CalculationsOnMachines
     */
    @Deprecated
    public PhaseMatrix stateResponse(String elemFrom, String elemTo) {
        Trajectory<EnvelopeProbeState> trajectory = this.getTrajectory();

        // find starting index
        int[] arrIndFrom = trajectory.indicesForElement(elemFrom);

        int[] arrIndTo = trajectory.indicesForElement(elemTo);

        if (arrIndFrom.length == 0 || arrIndTo.length == 0) {
            throw new IllegalArgumentException("unknown element id");
        }

        int indFrom, indTo;
        // use last state before start element
        indTo = arrIndTo[arrIndTo.length - 1];

        EnvelopeProbeState stateTo = trajectory.stateWithIndex(indTo);
        PhaseMatrix matTo = stateTo.getResponseMatrix();

        indFrom = arrIndFrom[0] - 1;
        if (indFrom < 0) {
            // response from beginning of machine
            return matTo;
        }
        EnvelopeProbeState stateFrom = trajectory.stateWithIndex(indFrom);
        PhaseMatrix matFrom = stateFrom.getResponseMatrix();

        return matTo.times(matFrom.inverse());
    }

    /*
	 * Trajectory Support
     */
    /**
     * Creates a snapshot of the current state and returns it as a
     * <code>ProbeState</code> object of the proper type.
     *
     * @return a new <code>EnvelopeProbeState</code> encapsulating the probe's
     * current state
     */
    @Override
    public EnvelopeProbeState createProbeState() {
        return new EnvelopeProbeState(this);
    }

    /**
     * Creates a new, empty <code>EnvelopeProbeState</code>.
     *
     * @return a new, empty <code>EnvelopeProbeState</code>
     *
     * @author Jonathan M. Freed
     * @since Jul 1, 2014
     */
    @Override
    public EnvelopeProbeState createEmptyProbeState() {
        return new EnvelopeProbeState();
    }

    /**
     * Creates a <code>Trajectory&lt;EnvelopeProbeState&gt;</code> object of the
     * proper type for saving the probe's history.
     *
     * @return a new, empty <code>Trajectory&lt;EnvelopeProbeState&gt;</code>
     * for saving the probe's history
     *
     * @author Jonathan M. Freed
     */
    @Override
    public Trajectory<EnvelopeProbeState> createTrajectory() {
        return new Trajectory<>(EnvelopeProbeState.class);
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
            LOGGER.log(Level.SEVERE, "EnvelopeProbe#reset() - Unable to initialize algorithm", e);

        }
    }

    /**
     *
     * @see xal.model.probe.Probe#readStateFrom(xal.tools.data.DataAdaptor)
     *
     * @author Christopher K. Allen
     * @version Oct 31, 2013
     */
    @Override
    protected EnvelopeProbeState readStateFrom(DataAdaptor container) throws DataFormatException {
        EnvelopeProbeState state = new EnvelopeProbeState();
        state.load(container);
        return state;
    }
}
