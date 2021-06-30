package xal.model.probe.traj;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.Twiss;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.model.probe.EnvelopeProbe;

/**
 * Encapsulates the state of an EnvelopeProbe at a particular point in time.
 *
 * @author Craig McChesney, Christopher K. Allen
 * @version $id:
 *
 */
public class EnvelopeProbeState extends BunchProbeState<EnvelopeProbeState> {

    private static final Logger LOGGER = Logger.getLogger(EnvelopeProbeState.class.getName());

    /*
     * Global Constants
     */
    //
    // Data Persistence
    //
    /**
     * element tag for envelope data
     */
    private static final String LABEL_ENVELOPE = "envelope";

    /**
     * data node label for covariance matrix
     */
    private static final String LABEL_COV = "covariance";

    /**
     * element label for response matrix (global response from simulation start
     * to here)
     */
    private static final String LABEL_RESP = "resp";

    /**
     * data node label for the response matrix containing no space charge
     * effects
     */
    private static final String LABEL_RESP_NOSCHEFF = "resp-nosheff";

    /**
     * attribute tag for perturbation matrix (local response between states)
     */
    private static final String LABEL_PERTURB = "perturb";

    //
    // Persistence Version
    //
    /**
     * the data format version attribute
     */
    private static final String ATTR_VERSION = "ver";

    /**
     * the data format version
     */
    private static final int INT_VERSION = 2;

    //
    // Backward Compatibility
    //
    /**
     * Attribute tag for covariance matrix
     */
    private static final String ATTR_COV = "covariance";

    /**
     * This is for backward compatibility when "covariance matrix" was
     * mistakenly called "correlation matrix"
     */
    private static final String ATTR_CORR = "correlation";

    /**
     * element tag for centroid data
     */
    protected static final String LABEL_CENTROID = "centroid";

    /**
     * attribute tag for centroid value vector
     */
    private static final String VALUE_LABEL = "value";

    /**
     * These are value tags for Twiss parameters, which optionally can be used
     * to initialize the covariance matrix.
     */
    private static final String ALPHA_X_TAG = "alphaX";
    private static final String BETA_X_TAG = "betaX";
    private static final String EMIT_X_TAG = "emitX";
    private static final String ALPHA_Y_TAG = "alphaY";
    private static final String BETA_Y_TAG = "betaY";
    private static final String EMIT_Y_TAG = "emitY";
    private static final String ALPHA_Z_TAG = "alphaZ";
    private static final String BETA_Z_TAG = "betaZ";
    private static final String EMIT_Z_TAG = "emitZ";

    // 
    // Supporting State Variables
    //
    /*
     * Local Attributes
     */
    /**
     * current response matrix (Sako)
     */
    private PhaseMatrix matPert;

    /**
     * accumulated response matrix
     */
    private PhaseMatrix matResp;

    /**
     * accumulated response matrix (no space charge)
     */
    private PhaseMatrix matRespNoSpaceCharge;

    /**
     * envelope state
     */
    private CovarianceMatrix matCov;

    /*
     * Initialization
     */
    /**
     * Default constructor. Create a new, empty <code>EnvelopeProbeState</code>
     * object.
     */
    public EnvelopeProbeState() {
        super();

        this.matCov = CovarianceMatrix.newIdentity();
        this.matPert = PhaseMatrix.identity();
        this.matResp = PhaseMatrix.identity();
        this.matRespNoSpaceCharge = PhaseMatrix.identity();
    }

    /**
     * Copy constructor for EnvelopeProbeState. Initializes the new
     * <code>EnvelopeProbeState</code> objects with the state attributes of the
     * given <code>EnvelopeProbeState</code>.
     *
     * @param prsEnv initializing state
     *
     * @author Christopher K. Allen, Jonathan M. Freed
     * @since Jun 26, 2014
     */
    public EnvelopeProbeState(final EnvelopeProbeState prsEnv) {
        super(prsEnv);

        this.matCov = prsEnv.matCov.clone();
        this.matPert = prsEnv.matPert.clone();
        this.matResp = prsEnv.matResp.clone();
        this.matRespNoSpaceCharge = prsEnv.matRespNoSpaceCharge.clone();
    }

    /**
     * Initializing Constructor. Create a new <code>EnvelopeProbeState</code>
     * object and initialize it to the state of the probe argument.
     *
     * @param probe     <code>EnvelopeProbe</code> containing initializing state
     * information
     */
    public EnvelopeProbeState(final EnvelopeProbe probe) {
        super(probe);

        this.setCovariance(probe.getCovariance().clone());
        this.setResponseMatrix(probe.getResponseMatrix().clone());
        this.setResponseMatrixNoSpaceCharge(probe.getResponseMatrixNoSpaceCharge().clone());
        this.setPerturbationMatrix(probe.getCurrentResponseMatrix().clone());
    }

    /*
     * Base Class Interface
     */
    /**
     * Implements the cloning operation required by the base class
     * <code>ProbeState</code>.
     *
     * @see xal.model.probe.traj.ProbeState#copy()
     *
     * @author Christopher K. Allen
     * @since Jun 27, 2014
     */
    @Override
    public EnvelopeProbeState copy() {
        return new EnvelopeProbeState(this);
    }

    /*
     * Attribute Setters
     */
    /**
     * Set the first-order response matrix of the current element slice
     *
     * @param matPerturb first-order response matrix in homogeneous coordinates
     */
    public void setPerturbationMatrix(PhaseMatrix matPerturb) {
        this.matPert = matPerturb;
    }

    /**
     * Set the first-order response matrix accumulated by the Envelope since its
     * initial state. Note that this response includes the effects of space
     * charge.
     *
     * @param matResp first-order response matrix in homogeneous coordinates
     */
    public void setResponseMatrix(PhaseMatrix matResp) {
        this.matResp = matResp;
    }

    /**
     * Set the first-order response matrix accumulated by the Envelope since its
     * initial state. Note that this response does not include the effects of
     * space charge.
     *
     * @param matResp first-order response matrix in homogeneous coordinates
     */
    public void setResponseMatrixNoSpaceCharge(PhaseMatrix matResp) {
        this.matRespNoSpaceCharge = matResp;
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
        matCov = matTau;
    }

    /*
     * Attribute Queries
     */
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
     * initial state. Note that this response does not include the effects of
     * space charge.
     *
     * @return first-order response matrix in homogeneous coordinates
     */
    public PhaseMatrix getResponseMatrixNoSpaceCharge() {
        return this.matRespNoSpaceCharge;
    }

    /**
     * Get the first-order response matrix of current element slice
     *
     * @return first-order response matrix in homogeneous coordinates
     */
    public PhaseMatrix getPerturbationMatrix() {
        return this.matPert;
    }

    /**
     * Returns the correlation matrix of this state in homogeneous phase space
     * coordinates. This is the primary state attribute for
     * <code>EnvelopeProbe</code> objects.
     *
     * @return 7x7 matrix &lt;zz^T&gt; in homogeneous coordinates
     */
    public CovarianceMatrix getCovarianceMatrix() {
        return matCov;
    }

    /*
     * Computed Properties
     */
    /**
     * Convenience Method: Returns the covariance matrix of this state in
     * homogeneous phase space coordinates. This value is computed directly from
     * the correlation matrix.
     *
     * @return &lt;<strong>zz</strong><sup><em>T</em></sup>&gt; -
     * &lt;<strong>z</strong>&gt;&lt;<strong>z</strong>&gt;<sup><em>T</em></sup>
     *
     * @see xal.tools.beam.CovarianceMatrix#computeCentralCovariance()
     */
    public CovarianceMatrix centralCovariance() {
        return getCovarianceMatrix().computeCentralCovariance();
    }

    /**
     * Convenience Method: Returns the rms emittances for this state as
     * determined by the <strong>correlation matrix</strong>. This value is
     * computed directly from the correlation matrix and is independent of the
     * <code>twissParams</code> local attribute.
     *
     * @return array
     * (&epsilon;<sub>x</sub>,&epsilon;<sub>y</sub>,&epsilon;<sub>z</sub>) of
     * rms emittances
     */
    public double[] rmsEmittances() {
        return getCovarianceMatrix().computeRmsEmittances();
    }

    /**
     * <p>
     * Return the twiss parameters for this state calculated from the covariance
     * matrix.
     * </p>
     * <h3>CKA Notes:</h3>
     * <p>
     * - Use this method with caution. The returned information is incomplete,
     * it is taken only from the three 2&times;2 diagonal blocks of the
     * correlation matrix and, therefore, does not contain the full state of the
     * beam. In general, you cannot restart the beam with the returned
     * parameters, for example, in the case of bends, offsets, dipoles, etc.
     * </p>
     *
     * @return twiss parameters computed from diagonal blocks of the correlation
     * matrix
     */
    public Twiss[] twissParameters() {
        return getCovarianceMatrix().computeTwiss();
    }

    /**
     * Convenience Method: Return the phase space coordinates of the centroid in
     * homogeneous coordinates. This value is taken from the correlation matrix.
     *
     * @return &lt;z&gt; = (&lt;x&gt;, &lt;xp&gt;, &lt;y&gt;, &lt;yp&gt;,
     * &lt;z&gt;, &lt;zp&gt;, 1)^T
     *
     * @see xal.tools.beam.CovarianceMatrix#getMean()
     */
    public PhaseVector phaseMean() {
        return getCovarianceMatrix().getMean();
    }

    /**
     * <p>
     * Save the state values particular to <code>EnvelopeProbeState</code>
     * objects to the data sink. In particular we save only the data in the 2x2
     * diagonal blocks of the correlation matrix, and as Twiss parameters.
     * </p>
     * <h3>CKA NOTE:</h3>
     * <p>
     * - <strong>Be careful</strong> when using this method! It is here as a
     * convenience only! It saves the <code>EnvelopeProbeState</code>
     * information in the save format as the load()/save() methods do, but you
     * cannot restore an <code>EnvelopeProbe</code> object from these data.
     * </p>
     *
     * @param daSink data sink represented by <code>DataAdaptor</code> interface
     */
    public void saveStateAsTwiss(DataAdaptor daSink) {
        DataAdaptor stateNode = daSink.createChild(STATE_LABEL);
        stateNode.setValue(TYPE_LABEL, getClass().getName());
        stateNode.setValue("id", this.getElementId());

        super.addPropertiesTo(stateNode);

        DataAdaptor envNode = stateNode.createChild(EnvelopeProbeState.LABEL_ENVELOPE);

        Twiss[] arrTwiss = this.twissParameters();

        envNode.setValue(EnvelopeProbeState.ALPHA_X_TAG, arrTwiss[0].getAlpha());
        envNode.setValue(EnvelopeProbeState.BETA_X_TAG, arrTwiss[0].getBeta());
        envNode.setValue(EnvelopeProbeState.EMIT_X_TAG, arrTwiss[0].getEmittance());
        envNode.setValue(EnvelopeProbeState.ALPHA_Y_TAG, arrTwiss[1].getAlpha());
        envNode.setValue(EnvelopeProbeState.BETA_Y_TAG, arrTwiss[1].getBeta());
        envNode.setValue(EnvelopeProbeState.EMIT_Y_TAG, arrTwiss[1].getEmittance());
        envNode.setValue(EnvelopeProbeState.ALPHA_Z_TAG, arrTwiss[2].getAlpha());
        envNode.setValue(EnvelopeProbeState.BETA_Z_TAG, arrTwiss[2].getBeta());
        envNode.setValue(EnvelopeProbeState.EMIT_Z_TAG, arrTwiss[2].getEmittance());
    }

    /*
     * ProbeState Overrides
     */
    /**
     * Save the state values particular to <code>EnvelopeProbeState</code>
     * objects to the data sink.
     *
     * @param container data sink represented by <code>DataAdaptor</code>
     * interface
     */
    @Override
    protected void addPropertiesTo(DataAdaptor container) {
        super.addPropertiesTo(container);

        DataAdaptor nodeEnv = container.createChild(LABEL_ENVELOPE);
        nodeEnv.setValue(ATTR_VERSION, INT_VERSION);

        DataAdaptor nodeCov = nodeEnv.createChild(LABEL_COV);
        this.getCovarianceMatrix().save(nodeCov);

        DataAdaptor nodeResp = nodeEnv.createChild(LABEL_RESP);
        this.getResponseMatrix().save(nodeResp);

        DataAdaptor nodeRespNoScheff = nodeEnv.createChild(LABEL_RESP_NOSCHEFF);
        this.getResponseMatrixNoSpaceCharge().save(nodeRespNoScheff);

        DataAdaptor nodePert = nodeEnv.createChild(LABEL_PERTURB);
        this.getPerturbationMatrix().save(nodePert);
    }

    /**
     * Recover the state values particular to <code>EnvelopeProbeState</code>
     * objects from the data source.
     *
     * @param container data source represented by a <code>DataAdaptor</code>
     * interface
     *
     * @exception DataFormatException state information in data source is
     * malformatted
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor container)
            throws DataFormatException {
        super.readPropertiesFrom(container);

        DataAdaptor nodeEnv = container.childAdaptor(LABEL_ENVELOPE);
        if (nodeEnv == null) {
            throw new DataFormatException("EnvelopeProbeState#readPropertiesFrom(): no child element = " + LABEL_ENVELOPE);
        }

        // Read the version number.  We don't do anything with it since there was no version
        //  attribute before version 2.  But it's here if necessary in the future.
        @SuppressWarnings("unused")
        int intVersion = 0;
        if (nodeEnv.hasAttribute(ATTR_VERSION)) {
            intVersion = nodeEnv.intValue(ATTR_VERSION);
        }

        // This is when the Twiss parameters were stored within the envelope node as an attribute
        //  It is possible that the centroid of the envelope was stored with it
        if (nodeEnv.hasAttribute(ALPHA_X_TAG)) {
            Twiss[] twiss = new Twiss[3];
            twiss[0] = new Twiss(nodeEnv.doubleValue(ALPHA_X_TAG),
                    nodeEnv.doubleValue(BETA_X_TAG),
                    nodeEnv.doubleValue(EMIT_X_TAG));
            twiss[1] = new Twiss(nodeEnv.doubleValue(ALPHA_Y_TAG),
                    nodeEnv.doubleValue(BETA_Y_TAG),
                    nodeEnv.doubleValue(EMIT_Y_TAG));
            twiss[2] = new Twiss(nodeEnv.doubleValue(ALPHA_Z_TAG),
                    nodeEnv.doubleValue(BETA_Z_TAG),
                    nodeEnv.doubleValue(EMIT_Z_TAG));

            DataAdaptor parNode = container.childAdaptor(LABEL_CENTROID);
            // if there is no centroid info we are done
            if (parNode == null) {
                this.setCovariance(CovarianceMatrix.buildCovariance(twiss[0], twiss[1], twiss[2]));

                // if there is centroid info get it then build the matrix 
            } else {
                if (parNode.hasAttribute(EnvelopeProbeState.VALUE_LABEL)) {
                    String strCent = parNode.stringValue(VALUE_LABEL);
                    PhaseVector vecCent = new PhaseVector(strCent);

                    this.setCovariance(CovarianceMatrix.buildCovariance(twiss[0], twiss[1], twiss[2], vecCent));
                }
            }

            // This is when the covariance matrix was stored as an attribute of the envelope node
        } else if (nodeEnv.hasAttribute(ATTR_COV)) {
            String strMatVal = nodeEnv.stringValue(ATTR_COV);
            CovarianceMatrix matChi = new CovarianceMatrix(strMatVal);
            this.setCovariance(matChi);

            // There were two different attribute tags for the same thing need to look for both 
            // Included for backward compatibility when using old attr label
        } else if (nodeEnv.hasAttribute(ATTR_CORR)) {
            String strMatVal = nodeEnv.stringValue(EnvelopeProbeState.ATTR_CORR);
            CovarianceMatrix matChi = new CovarianceMatrix(strMatVal);
            this.setCovariance(matChi);

        }

        // Read the state data in the current version
        try {

            DataAdaptor nodeCov = nodeEnv.childAdaptor(LABEL_COV);
            if (nodeCov != null) {
                CovarianceMatrix matCov = CovarianceMatrix.loadFrom(nodeCov);
                this.setCovariance(matCov);
            }

            DataAdaptor nodeResp = nodeEnv.childAdaptor(LABEL_RESP);
            if (nodeResp != null) {
                PhaseMatrix matResp = PhaseMatrix.loadFrom(nodeResp);
                this.setResponseMatrix(matResp);
            }

            DataAdaptor nodeRespNoscheff = nodeEnv.childAdaptor(LABEL_RESP_NOSCHEFF);
            if (nodeRespNoscheff != null) {
                PhaseMatrix matResp = PhaseMatrix.loadFrom(nodeRespNoscheff);
                this.setResponseMatrixNoSpaceCharge(matResp);
            }

            DataAdaptor nodePert = nodeEnv.childAdaptor(LABEL_PERTURB);
            if (nodePert != null) {
                PhaseMatrix matPert = PhaseMatrix.loadFrom(nodePert);
                this.setPerturbationMatrix(matPert);
            }

        } catch (DataFormatException e) {
            LOGGER.log(Level.SEVERE, null, e);
            throw new DataFormatException("The source data was corrupted - " + e.getMessage());

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, null, e);
            throw new DataFormatException("The provided covariance matrix was asymmetric - " + e.getMessage());

        }
    }

    /*
     * Object Overrides
     */
    /**
     * Write out state information to a string.
     *
     * @return text version of internal state data
     */
    @Override
    public String toString() {
        return super.toString() + " covariance: " + getCovarianceMatrix().toString()
                + ", response: " + this.getResponseMatrix().toString();
    }
}
