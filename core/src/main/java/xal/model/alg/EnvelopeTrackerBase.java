/*
 * EnvelopeTrackerBase.java
 * 
 * 
 * @author Christopher K. Allen
 * @since Feb 10, 2009
 * 
 */
package xal.model.alg;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.TraceXalUnitConverter;
import xal.tools.beam.Twiss;
import xal.tools.beam.PhaseMatrix.IND;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.em.BeamEllipsoid;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;
import xal.tools.math.BesselFunction;
import xal.tools.math.ElementaryFunction;
import xal.tools.math.EllipticIntegral;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.IdealRfGap;
import xal.model.probe.EnvelopeProbe;

/**
 * <h1>Abstract Base Class for Algorithms Propagating RMS Envelopes</h1>
 *
 * <p>
 * This class adds functionality beyond the base class <code>Tracker</code>
 * specific to tracker algorithms designed for RMS envelope beam probes, that
 * is, probes which carry the RMS statistical properties as their beam state.
 * </p>
 *
 * @author Christopher K. Allen
 *
 * @since Feb 10, 2009
 */
public abstract class EnvelopeTrackerBase extends Tracker {

    /**
     * Enumerations for supported phase planes
     *
     * @author Christopher K. Allen
     * @since Feb 19, 2009
     */
    public enum PhasePlane {

        /**
         * Either transverse phase plane: horizontal <em>x</em> or vertical
         * <em>y</em>.
         */
        TRANSVERSE(1),
        /**
         * The longitudinal phase plane <em>z</em>
         */
        LONGITUDINAL(2);

        /*
         * Local Attributes
         */
        /**
         * index value (enumeration constant)
         */
        private final int i;

        /**
         * Default enumeration constructor
         *
         * @param i Enumeration constant for phase plane
         */
        PhasePlane(int i) {
            this.i = i;
        }

        /**
         * Return the integer value of the index position
         *
         * @return Integer value of enumeration constant
         */
        public int val() {
            return i;
        }
    }

    /**
     * <h1>RF Gap Emittance Growth Models</h1>
     * <p>
     * Enumerations identifying supported emittance growth models for RF
     * accelerating gaps. (Currently this applies primarily to the longitudinal
     * situation.) at the moment.
     * </p>
     * <p>
     * Since longitudinal emittance growth mechanism is not well documented in
     * the <code>Trace3D</code> manual, it is somewhat unclear how the effect is
     * being modeled. However, it does appear to create a result that tracks the
     * <code>IMPACT</code> simulation better than the CKA method (at least for
     * one case). At this point it is unclear whether there is an error in the
     * analysis of C.K. Allen, or <code>Trace3D</code> is simply more accurate.
     * </p>
     * <p>
     * <strong>VALUES</strong>:
     * <br>
     * Currently there are two supported mechanisms for emittance growth.
     * <br>
     * <code>Trace3D</code>: use the same mechanism described in the Trace3D
     * manual.
     * <br>
     * <code>CKAllen</code>: use the mechanism described by C.K. Allen,
     * <em>et. al.</em> (see below).
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss, "Bunching of Intense Proton Beams with Six-Dimensional
     * Matching to the Linac Acceptance", CERN/MPS/LI report 73-2, Geneva,
     * Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, "Trace 3-D Documentation", LANL
     * Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth Due
     * to Phase Spread for Proton Beams in Radio Frequency Accelerating Gaps",
     * (in preparation).
     * </p>
     *
     * @author Christopher K. Allen
     * @since Feb 23, 2009
     */
    public enum EmitGrowthModel {

        /**
         * Use exact model of Trace3D
         */
        TRACE3D,
        /**
         * Uniform distribution in each (uncorrelated) phase plane
         */
        UNIFORM1D,
        /**
         * Gaussian distribution in each (uncorrelated) phase plane
         */
        GAUSSIAN1D,
        /**
         * Uniform distribution in three spatial dimensions
         */
        UNIFORM3D,
        /**
         * Gaussian distribution in three spatial dimensions
         */
        GAUSSIAN3D;

    }

    /*
     * Global Constants
     */
    /**
     * EditContext table name containing basic envelope tracking parameters
     */
    public static final String TBL_LBL_ENVBASETRACKER = "EnvelopeBaseTracker";

    /**
     * data node label for EnvelopeTracker settings
     */
    public static final String LABEL_OPTIONS = "options";

    /**
     * label for use simple tracking (no space charge)
     */
    public static final String ATTR_SCHEFF = "scheff";

    /**
     * label for use simple tracking (no space charge)
     */
    public static final String ATTR_USESPACECHARGE = "useSpacecharge";

    /**
     * label for emittance growth flag
     */
    public static final String ATTR_EMITGROWTH = "emitgrowth";

    /**
     * label for maximum step size *
     */
    public static final String ATTR_STEPSIZE = "stepsize";

    /**
     * label for use DC Beam *
     */
    public static final String ATTR_USEDCBEAM = "useDCBeam";

    /*
     * LOCAL CONSTANTS
     */
    /**
     * Conditional value where polynomial expansions are employed
     */
    private static final double SMALL_ARG = BesselFunction.SMALL_ARG;

    /**
     * upright ellipse tolerance for using expedited space charge calculation
     */
    private static final double TOLER_CORRELATION = 0.01;

    /**
     * distribution dependence factor - use that for the uniform beam
     */
    private static final double CONST_UNIFORM_BEAM = Math.pow(5.0, 1.5);

    /*
     * Local Variables
     */
    /**
     * maximum distance to advance probe before applying space charge kick
     */
    private double dblMaxStep = 0.004;

    /**
     * flag for using space charge
     */
    private boolean bolScheff = true;

    /**
     * flag for simulating emittance growth
     */
    private boolean bolEmitGrowth = false;

    /**
     * flag for simulating a DC beam
     */
    private boolean bolDCBeam = false;

    /**
     * longitudinal emittance growth model - Default is TRACE3D
     */
    private EmitGrowthModel enmEmitGrowthModel = EmitGrowthModel.TRACE3D;

    /**
     * <h2>EnvelopeTrackerBase Constructor</h2>
     * <p>
     * This should be used by child classes to pass up their class properties.
     * </p>
     *
     * @param strType string type identifier of the class
     * @param intVersion version number of class implementation
     * @param clsProbeType the class type of valid probe
     */
    public EnvelopeTrackerBase(String strType, int intVersion,
            Class<? extends IProbe> clsProbeType) {
        super(strType, intVersion, clsProbeType);
    }

    /**
     * Copy constructor for EnvelopeTracker
     *
     * @param sourceTracker Tracker that is being copied
     */
    public EnvelopeTrackerBase(EnvelopeTrackerBase sourceTracker) {
        super(sourceTracker);

        this.dblMaxStep = sourceTracker.dblMaxStep;
        this.bolScheff = sourceTracker.bolScheff;
        this.bolEmitGrowth = sourceTracker.bolEmitGrowth;
        this.bolDCBeam = sourceTracker.bolDCBeam;
        this.enmEmitGrowthModel = sourceTracker.enmEmitGrowthModel;
    }

    /**
     * Set maximum step size allowed between space charge kicks
     *
     * @param step the new maximum step size for space charge calculations
     *
     * @author Hiroyuki Sako
     */
    public void setStepSize(double step) {
        dblMaxStep = step;
    }

    /**
     * <p>
     * Method to toggle the flag to use/not use space charge calculations.
     * </p>
     *
     * @param tf the truth flag
     */
    public void setUseSpacecharge(boolean tf) {
        bolScheff = tf;
    }

    /**
     * <p>
     * Method to toggle the flag to use/not the DC beam space charge kicks.
     * </p>
     *
     * @param tf the truth flag
     */
    public void setUseDCBeam(boolean tf) {
        bolDCBeam = tf;
    }

    /**
     * <p>
     * Set the emittance growth flag. If set true then the algorithm will
     * simulate emittance growth through RF gaps.
     * </p>
     * <p>
     * <strong>NOTE</strong>: (CKA)
     * <br>
     * &middot; If set, the dynamics will no longer be consistent since the
     * response matrix and betatron phases will not reproduce the current Twiss
     * parameters.
     * </p>
     *
     * @param bolEmitGrowth set true to simulation emittance growth
     *
     * @see xal.model.elem.IdealRfGap
     * @see #setEmitGrowthModel(EmitGrowthModel)
     */
    public void setEmittanceGrowth(boolean bolEmitGrowth) {
        this.bolEmitGrowth = bolEmitGrowth;
    }

    /**
     * <h2>Set the emittance growth mechanism</h2>
     * <p>
     * Set the current mechanism for simulation emittance growth from RF
     * accelerating gaps. We can either use the same model as
     * <code>Trace3D</code> or the generalized technique described in C.K. Allen
     * <em>et. al.</em>
     * (see references below). This applies primarily to the longitudinal
     * situation since the transverse cases show very good agreement.
     * </p>
     * <p>
     * Since longitudinal emittance growth mechanism is not well documented in
     * the <code>Trace3D</code> manual, it is somewhat unclear how the effect is
     * being modeled. However, it does appear to create a result that tracks the
     * <code>IMPACT</code> simulation better than the CKA method (at least for
     * one case). At this point it is unclear whether there is an error in the
     * analysis of C.K. Allen, or <code>Trace3D</code> is simply more accurate.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * &middot; This flag only has an effect when the
     * <code>setEmittanceGrowth()</code> feature is set to <code>true</code>.
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss, "Bunching of Intense Proton Beams with Six-Dimensional
     * Matching to the Linac Acceptance", CERN/MPS/LI report 73-2, Geneva,
     * Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, "Trace 3-D Documentation", LANL
     * Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth Due
     * to Phase Spread for Proton Beams in Radio Frequency Accelerating Gaps",
     * (in preparation).
     * </p>
     *
     * @param enmModel use the <code>Trace3D</code> method or the C.K. Allen
     * <em>et. al.</em> method
     */
    public void setEmitGrowthModel(EmitGrowthModel enmModel) {
        this.enmEmitGrowthModel = enmModel;
    }

    /**
     * <p>
     * Returns the maximum element subsection length (in meters) that the probe
     * may be advanced before applying a space charge kick when space charge is
     * present.
     * </p>
     *
     * @return Maximum space charge integration step size.
     *
     */
    public double getStepSize() {
        return dblMaxStep;
    }

    /**
     * Returns the flag determining whether or not space charge effects are
     * being considered during the propagation.
     *
     * @return true if space charge forces are used, false otherwise
     */
    public boolean getUseSpacecharge() {
        return this.bolScheff;
    }

    /**
     * Returns the flag determining whether or not space charge kick for DC beam
     * is being considered
     *
     * @return true if space charge forces for DC beam is used, false otherwise
     */
    public boolean getUseDCBeam() {
        return this.bolDCBeam;
    }

    @Deprecated
    public boolean getSpaceChargeFlag() {
        return getUseSpacecharge();
    }

    /**
     * Return the emittance growth flag.
     *
     * @return true if we are simulating emittance growth, false otherwise
     *
     * @see EnvelopeTracker#setEmittanceGrowth(boolean)
     */
    public boolean getEmittanceGrowth() {
        return this.bolEmitGrowth;
    }

    @Deprecated
    public boolean getEmittanceGrowthFlag() {
        return getEmittanceGrowth();
    }

    /**
     * <h2>Return the emittance growth model</h2>
     * <p>
     * Get the current mechanism for simulating emittance growth from RF
     * accelerating gaps. We are either using the that of <code>Trace3D</code>
     * or the generalized technique described in C.K. Allen <em>et. al.</em>
     * (see references below). This applies primarily to the longitudinal
     * situation since the transverse cases show very good agreement.
     * </p>
     * <p>
     * Since longitudinal emittance growth mechanism is not well documented in
     * the <code>Trace3D</code> manual, it is somewhat unclear how the effect is
     * being modeled. However, it does appear to create a result that tracks the
     * <code>IMPACT</code> simulation better than the CKA method (at least for
     * one case). At this point it is unclear whether there is an error in the
     * analysis of C.K. Allen, or <code>Trace3D</code> is simply more accurate.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * &middot; This flag only has an effect when the
     * <code>setEmittanceGrowth()</code> feature is set to <code>true</code>.
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss, "Bunching of Intense Proton Beams with Six-Dimensional
     * Matching to the Linac Acceptance", CERN/MPS/LI report 73-2, Geneva,
     * Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, "Trace 3-D Documentation", LANL
     * Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth Due
     * to Phase Spread for Proton Beams in Radio Frequency Accelerating Gaps",
     * (in preparation).
     * </p>
     *
     * @return enumeration code for particular emittance growth mechanism
     */
    public EmitGrowthModel getEmitGrowthModel() {
        return enmEmitGrowthModel;
    }

    /*
     * IArchive Interface
     */
    /**
     * Load the parameters of this <code>IAlgorithm</code> object from the table
     * data in the given <code>EditContext</code>.
     *
     * Here we load only the parameters specific to the base class. It is
     * expected that Subclasses should override this method to recover the data
     * particular to there own operation.
     *
     * @param strPrimKeyVal primary key value specifying the name of the data
     * record
     * @param ecTableData EditContext containing table data
     *
     * @see xal.tools.data.IContextAware#load(String,
     * xal.tools.data.EditContext)
     */
    @Override
    public void load(final String strPrimKeyVal, final EditContext ecTableData) throws DataFormatException {
        super.load(strPrimKeyVal, ecTableData);

        // Get the algorithm class name from the EditContext
        DataTable tblAlgorithm = ecTableData.getTable(TBL_LBL_ENVBASETRACKER);
        GenericRecord recTracker = tblAlgorithm.record(Tracker.TBL_PRIM_KEY_NAME, strPrimKeyVal);

        if (recTracker == null) {
            // just use the default record
            recTracker = tblAlgorithm.record(Tracker.TBL_PRIM_KEY_NAME, "default");
        }

        final boolean bolEmitGrw = recTracker.booleanValueForKey(ATTR_EMITGROWTH);
        final boolean bolUseSpChg = recTracker.booleanValueForKey(ATTR_SCHEFF);
        final double dblStepSize = recTracker.doubleValueForKey(ATTR_STEPSIZE);
        final boolean bolDCBeam = recTracker.booleanValueForKey(ATTR_USEDCBEAM);

        this.setEmittanceGrowth(bolEmitGrw);
        this.setStepSize(dblStepSize);
        this.setUseSpacecharge(bolUseSpChg);
        this.setUseDCBeam(bolDCBeam);
    }

    /**
     * Load the parameters of the algorithm from a data source exposing the
     * <code>IArchive</code> interface. The superclass <code>load</code> method
     * is called first, then the properties particular to
     * <code>EnvTrackerAdapt</code> are loaded.
     *
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daptArchive) {
        super.load(daptArchive);

        DataAdaptor daEnv = daptArchive.childAdaptor(LABEL_OPTIONS);
        if (daEnv != null) {
            if (daEnv.hasAttribute(ATTR_SCHEFF)) {
                this.setUseSpacecharge(daEnv.booleanValue(ATTR_SCHEFF));
                // Backward compatibility
            } else if (daEnv.hasAttribute(ATTR_USESPACECHARGE)) {
                this.setUseSpacecharge(daEnv.booleanValue(ATTR_USESPACECHARGE));
            }

            if (daEnv.hasAttribute(ATTR_EMITGROWTH)) {
                this.setEmittanceGrowth(daEnv.booleanValue(ATTR_EMITGROWTH));
            }

            if (daEnv.hasAttribute(ATTR_STEPSIZE)) {
                this.setStepSize(daEnv.doubleValue(ATTR_STEPSIZE));
            }
        }
    }

    /**
     * Save the state and settings of this algorithm to a data source exposing
     * the <code>DataAdaptor</code> interface. Subclasses should override this
     * method to store the data particular to there own operation.
     *
     * @param daptArchive data source to receive algorithm configuration
     *
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daptArchive) {
        super.save(daptArchive);

        DataAdaptor daptAlg = daptArchive.childAdaptor(NODETAG_ALG);

        DataAdaptor daptOpt = daptAlg.createChild(LABEL_OPTIONS);
        daptOpt.setValue(ATTR_SCHEFF, this.getUseSpacecharge());
        daptOpt.setValue(ATTR_STEPSIZE, this.getStepSize());
        daptOpt.setValue(ATTR_EMITGROWTH, this.getEmittanceGrowth());
        daptOpt.setValue(ATTR_USESPACECHARGE, this.getUseSpacecharge());
    }

    /**
     * <p>
     * Method to modify the transfer matrix when we are simulating emittance
     * growth. Currently, the method only considers the case of propagation
     * through an <code>IdealRfGap</code> element. If the <code>IElement</code>
     * argument is any other type of element, nothing is done.
     * </p>
     * <p>
     * The argument <code>matPhi</code> is the original transfer matrix for
     * (normal) propagation through the <code>elem</code> element.
     * </p>
     *
     * <p>
     * <strong>NOTES</strong>: (H. SAKO)
     * <br>
     * &middot; Increase emittance using same (nonlinear) procedure on the
     * second moments as in Trace3D.
     * <br>
     * <br>
     * (C.K. Allen)
     * <br>
     * &middot; The &lt;x'|x&gt; transfer matrix element is modified by the
     * formula
     * <br>
     * <br>
     * &nbsp; &lt;x'|x&gt; =
     * &lt;x'|x&gt;<em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>)
     * <br>
     * <br>
     * where &Delta;<em>&phi;</em> is the longitudial phase spread and
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) is an approximation to
     * &lt;sin(<em>&phi;</em>)&gt;.
     * <br>
     * <br>
     * &middot; Originally, the &lt;z'|z&gt; transfer matrix element is modified
     * by the formula
     * <br>
     * <br> &nbsp; &lt;z'|z&gt; = &lt;z'|z&gt;(1 -
     * &Delta;<em>&phi;</em><sup>2</sup>/12)
     * <br>
     * <br>
     * This approximation is given in the Trace3D manualThis formula is accurate
     * only for <em>d&phi;</em> &lt;&lt;. Even then, the results are
     * questionable. For a more in depth treatment of longitudinal emittance
     * growth see the reference below.
     * <br>
     * &middot; The two-term expansion for
     * <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) (see
     * {@link #compLongFourierTransform(double)}) is given as
     * <br>
     * <br>
     * &nbsp; <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) &asymp; 1 -
     * 3&Delta;<em>&phi;</em><sup>2</sup>/14
     * <br>
     * <br>
     * which does not correspond to the Trace3D manual. So I do not know where
     * they got the number.
     * </p>
     * <p>
     * <strong>Reference</strong>
     * <br>
     * C.K. Allen, Hiroyuki Sako, et. al., "Emittance Growth Due to Phase Spread
     * for Proton Beams in a Radio Frequency Accelerating Gap" (in preparation).
     * </p>
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em>
     * @param matPhi transfer matrix <strong>&Phi;</strong> for conserved
     * normalized emittance
     *
     * @return Transfer matrix &Phi; after modifying focusing term
     *
     * @throws ModelException unsupport/unknown emittance growth model
     *
     * @see #compTransFourierTransform(double)
     * @see #compLongFourierTransform(double)
     * @see EnvelopeTrackerBase#effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see EnvelopeTrackerBase#momentSine(double)
     *
     * @author Hiroyuki Sako
     * @author Christopher K. Allen
     */
    protected PhaseMatrix modTransferMatrixForEmitGrowth(double dphi, PhaseMatrix matPhi)
            throws ModelException {

        if (!this.getEmittanceGrowth()) {
            return matPhi;
        }

        // Compute auxiliary parameters
        // transverse plane Fourier transform
        double Ft;
        // longitudinal plane Fourier transform
        double Fz;

        Ft = this.compTransFourierTransform(dphi);
        Fz = this.compLongFourierTransform(dphi);

        // Modify the transfer matrix
        // thin-lens focal-length element of tranfer matrix
        double fl;

        fl = matPhi.getElem(IND.Xp, IND.X);
        matPhi.setElem(IND.Xp, IND.X, fl * Ft);

        fl = matPhi.getElem(IND.Yp, IND.Y);
        matPhi.setElem(IND.Yp, IND.Y, fl * Ft);

        fl = matPhi.getElem(IND.Zp, IND.Z);
        matPhi.setElem(IND.Zp, IND.Z, fl * Fz);

        return matPhi;
    }

    /**
     * <p>
     * Method to compute the space charge transfer matrix for the given length,
     * probe and modeling element.
     * </p>
     *
     * <p>
     * The correlation matrix of the probe is used to determine the space charge
     * transfer matrix. The transfer matrix of the elem is computed for half the
     * distance provided, the correlation matrix is advanced this half step, and
     * the space charge matrix is computed there. Thus, the space charge matrix
     * is always computed at the center of the distance <code>dblLen</code>.
     * </p>
     *
     * NOTE:
     * <p>
     * This half-step business maintains consistency with the Trace3D algorithm.
     * Once inside the element you are essentially doing leap-frog integration,
     * which may or may not be more accurate then integration at full steps. The
     * final algorithms are both second-order accurate, simply by the way the
     * final transfer matrix is computed.
     * </p>
     *
     * @param dblLen incremental path length over which space charge is applied
     * @param probe   <code>EnvelopeProbe</code> containing correlation matrix
     * @param elem    <code>IElement</code> where probe is currently located
     *
     * @return space-charge transfer matrix for incremental distance
     *
     * @throws ModelException could not compute the transfer map for given
     * element
     *
     * @see xal.tools.beam.em.BeamEllipsoid
     */
    protected PhaseMatrix compScheffMatrix(double dblLen, EnvelopeProbe probe, IElement elem)
            throws ModelException {

        // Get probe parameters
        double gamma = probe.getGamma();
        CovarianceMatrix tau0 = probe.getCovariance();

        // Compute the space charge matrix 
        //      Compute the correlations in configuration space
        double covXX = tau0.computeCentralCovXX();
        double covYY = tau0.computeCentralCovYY();
        double covZZ = tau0.computeCentralCovZZ();

        double covXY = tau0.computeCentralCovXY();
        double covXZ = tau0.computeCentralCovXZ();
        double covYZ = tau0.computeCentralCovYZ();

        // Compute space charge matrix
        // space charge transfer matrix for dblLen
        PhaseMatrix matPhiSc;

        // Build the space charge transfer matrix in beam coordinates
        matPhiSc = PhaseMatrix.identity();

        if (getUseDCBeam()) {

            double corr = (covXY * covXY) / (covXX * covYY);
            double K = probe.beamDCPerveance();

            // beam is upright                                
            if (corr < EnvelopeTrackerBase.TOLER_CORRELATION) {

                // Compute defocusing constants in the laboratory frame
                double kx = dblLen * K / (4 * Math.sqrt(covXX) * (Math.sqrt(covXX) + Math.sqrt(covYY)));
                double ky = dblLen * K / (4 * Math.sqrt(covYY) * (Math.sqrt(covXX) + Math.sqrt(covYY)));

                matPhiSc.setElem(IND.Xp, IND.X, kx);
                matPhiSc.setElem(IND.Yp, IND.Y, ky);

                // Transform to laboratory coordinates
                PhaseVector z = tau0.getMean();
                PhaseMatrix T = PhaseMatrix.translation(z.negate());
                PhaseMatrix Ti = PhaseMatrix.translation(z);

                matPhiSc = Ti.times(matPhiSc.times(T));

            } else {

                // Beam is tilted in configuration space
                // Compute the space charge matrix in the beam frame and transform back 
                BeamEllipsoid ellipsoid = new BeamEllipsoid(gamma, tau0);
                matPhiSc = ellipsoid.computeDCScheffMatrix(dblLen, K);

            }

        } else {

            double corr = (covXY * covXY) / (covXX * covYY)
                    + (covXZ * covXZ) / (covXX * covZZ)
                    + (covYZ * covYZ) / (covYY * covZZ);
            double K = probe.beamPerveance();

            // beam is upright
            if (corr < EnvelopeTrackerBase.TOLER_CORRELATION) {
                double g_2 = gamma * gamma;

                // Compute elliptic integrals
                double RDx = EllipticIntegral.RD(covYY, g_2 * covZZ, covXX) / EnvelopeTrackerBase.CONST_UNIFORM_BEAM;
                double RDy = EllipticIntegral.RD(g_2 * covZZ, covXX, covYY) / EnvelopeTrackerBase.CONST_UNIFORM_BEAM;
                double RDz = EllipticIntegral.RD(covXX, covYY, g_2 * covZZ) / EnvelopeTrackerBase.CONST_UNIFORM_BEAM;

                // Compute defocusing constants in the laboratory frame
                double kx = gamma * dblLen * K * RDx;
                double ky = gamma * dblLen * K * RDy;
                double kz = gamma * dblLen * K * RDz;

                matPhiSc.setElem(IND.Xp, IND.X, kx);
                matPhiSc.setElem(IND.Yp, IND.Y, ky);
                matPhiSc.setElem(IND.Zp, IND.Z, kz);

                // Transform to laboratory coordinates
                PhaseVector z = tau0.getMean();
                PhaseMatrix T = PhaseMatrix.translation(z.negate());
                PhaseMatrix Ti = PhaseMatrix.translation(z);

                matPhiSc = Ti.times(matPhiSc.times(T));

                // Beam is tilted in configuration space
            } else {

                // Compute the space charge matrix in the beam frame and transform back 
                BeamEllipsoid ellipsoid = new BeamEllipsoid(gamma, tau0);
                matPhiSc = ellipsoid.computeScheffMatrix(dblLen, K);
            }
        }

        // Return the space charge matrix
        return matPhiSc;
    }

    /**
     * <h2>Emittance Growth Function for Phase Spread in RF Gap</h2>
     * <p>
     * Calculation of the emittance growth function describing the emittance
     * growth due to finite phase spread in an RF gap. (Note that the growth
     * function differs for each density distribution and for each phase plane.)
     * The particular phase plane is identified by the argument
     * <code>plane</code>, which is currently either transverse or longitudinal.
     * The density distribution is specified by the <em>emittance growth
     * model</em>
     * (see {@link #setEmitGrowthModel(EmitGrowthModel)}). We currently assume
     * the beam bunch to be axially symmetric. We denote the growth function in
     * the transverse plane as
     * <em>G<sub>t</sub></em>(<em>&phi;<sub>s</sub></em>,&Delta;<em>&phi;</em>)
     * and the growth function in the longitudinal plane as
     * <em>G<sub>z</sub></em>(<em>&phi;<sub>s</sub></em>,&Delta;<em>&phi;</em>).
     * </p>
     * <p>
     * The emittance growth calculation was originally described by M. Weiss for
     * then implemented in Trace3d. C.K. Allen,
     * <em>et. al.</em> generalized the results for arbitrary distributions and
     * the longitudinal case (see references below).
     * </p>
     * <p>
     * The emittance growth function
     * <em>G<sub>t</sub></em>(<em>&phi;<sub>s</sub></em>,&Delta;<em>&phi;</em>)
     * for the transverse plane is defined as
     * <br>
     * <br>
     * &nbsp;
     * <em>G<sub>t</sub></em>(<em>&phi;<sub>s</sub></em>,&Delta;<em>&phi;</em>)
     * &equiv; <em>S<sub>t</sub></em>(&Delta;<em>&phi;</em>) - sin<sup>2</sup>
     * <em>&phi;<sub>s</sub></em>
     * <em>T<sub>t</sub></em>(&Delta;<em>&phi;</em>)
     * <br>
     * <br>
     * where <em>&phi;<sub>s</sub></em> is the synchronous particle phase,
     * &Delta;<em>&phi;</em> is the <em>effective</em> phase spread, and
     * functions
     * <em>S<sub>t</sub></em>(&Delta;<em>&phi;</em>) and
     * <em>T<sub>t</sub></em>(&Delta;<em>&phi;</em>) are given by
     * <br>
     * <br>
     * &nbsp; <em>S<sub>t</sub></em>(&Delta;<em>&phi;</em>) &equiv; &frac12;[1 -
     * <em>F<sub>t</sub></em>(2&Delta;<em>&phi;</em>)]
     * <br>
     * <br>
     * and
     * <br>
     * <br>
     * &nbsp; <em>T<sub>t</sub></em>(&Delta;<em>&phi;</em>) &equiv;
     * <em>F<sub>t</sub></em><sup>2</sup>(&Delta;<em>&phi;</em>) -
     * <em>F<sub>t</sub></em>(2&Delta;<em>&phi;</em>)
     * <br>
     * <br>
     * respectively. There are analogous formulas for the longitudinal emittance
     * growth function
     * <em>G<sub>z</sub></em>(<em>&phi;<sub>s</sub></em>,&Delta;<em>&phi;</em>)
     * where the transverse Fourier transform
     * <em>F<sub>t</sub></em>
     * is replaced by the longitudinal Fourier transform
     * <em>F<sub>z</sub></em>. (See the methods
     * <code>{@link #compTransFourierTransform(double)}</code> and
     * <code>{@link #compLongFourierTransform(double)}</code>.)
     * </p>
     * <p>
     * The before gap and after gap transverse RMS emittances,
     * <em>&epsilon;<sub>t,i</sub></em> and
     * <em>&epsilon;<sub>t,f</sub></em>, respectively, are related by the
     * following formula:
     * <br>
     * <br>
     * &nbsp; <em>&epsilon;<sub>t,f</sub></em><sup>2</sup> =
     * <em>&eta;</em><sup>2</sup><em>&epsilon;<sub>t,i</sub></em><sup>2</sup> +
     * &Delta;<em>&epsilon;<sub>t,f</sub></em><sup>2</sup>
     * <br>
     * <br>
     * where <em>&eta;</em> is the momentum compaction due to acceleration
     * <br>
     * <br>
     * <em>&eta;</em> &equiv;
     * <em>&beta;<sub>i</sub>&gamma;<sub>i</sub></em>/<em>&beta;<sub>f</sub>&gamma;<sub>f</sub></em>
     * <br>
     * <br>
     * and &Delta;<em>&epsilon;<sub>t,f</sub></em> is the emittance increase
     * term
     * <br>
     * <br>
     * &nbsp; &Delta;<em>&epsilon;<sub>t,f</sub></em><sup>2</sup> &equiv;
     * &Delta;&lt;<em>x'<sub>f</sub></em><sup>2</sup>&gt;
     * &lt;<em>x<sub>f</sub></em><sup>2</sup>&gt;<sup>2</sup>.
     * <br>
     * <br>
     * where
     * <br>
     * <br>
     * &nbsp; &Delta;&lt;<em>x'<sub>f</sub></em><sup>2</sup>&gt; &equiv;
     * <em>k<sub>t</sub></em><sup>2</sup>
     * <em>G<sub>t</sub></em>(<em>&phi;<sub>s</sub></em>,&Delta;<em>&phi;</em>)
     * &lt;<em>x<sub>i</sub></em><sup>2</sup>&gt;.
     * <br>
     * <br>
     * and where <em>x'<sub>f</sub></em> and <em>x<sub>i</sub></em> represent
     * the after-gap divergence angle and before-gap position for
     * <em>either</em>
     * transverse phase plane, respectively. Once again there are analogous
     * formulas for the before and after gap longitudinal plane emittances
     * <em>&epsilon;<sub>z,i</sub></em> and
     * <em>&epsilon;<sub>z,f</sub></em>, respectively, with
     * <em>G<sub>t</sub></em>(<em>&phi;<sub>s</sub></em>,&Delta;<em>&phi;</em>)
     * replaced by
     * <em>G<sub>z</sub></em>(<em>&phi;<sub>s</sub></em>,&Delta;<em>&phi;</em>)
     * and
     * <em>x</em><sub>(<em>f,i</em>)</sub> replaced by
     * <em>z</em><sub>(<em>f,i</em>)</sub>.
     * </p>
     * <p>
     * <strong>NOTES</strong>: CKA
     * <br>
     * &middot; Since we are modeling the RF gap as a thin lens, only the
     * momentum (divergence angle) is modified, &lt;<em>x</em><sup>2</sup>&gt;,
     * &lt;<em>y</em><sup>2</sup>&gt;, and &lt;<em>z</em><sup>2</sup>&gt; remain
     * unaffected. Thus, &lt;<em>x<sub>f</sub></em><sup>2</sup>&gt; =
     * &lt;<em>x<sub>i</sub></em><sup>2</sup>&gt; and
     * &lt;<em>z<sub>f</sub></em><sup>2</sup>&gt; =
     * &lt;<em>z<sub>i</sub></em><sup>2</sup>&gt; and may be computed as such in
     * the above.
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss, "Bunching of Intense Proton Beams with Six-Dimensional
     * Matching to the Linac Acceptance", CERN/MPS/LI report 73-2, Geneva,
     * Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, "Trace 3-D Documentation", LANL
     * Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth Due
     * to Phase Spread for Proton Beams in Radio Frequency Accelerating Gaps",
     * (in preparation).
     * </p>
     *
     *
     * @param plane Compute the emittance growth function for this phase plane
     * @param phi_s the synchronous particle phase <em>&phi;<sub>s</sub></em>
     * in <em>radians</em>
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <em>radians</em>
     *
     * @return The value of the emittance growth function
     * <em>G<sub>t</sub></em>(<em>&phi;<sub>s</sub></em>,&Delta;<em>&phi;</em>)
     * or
     * <em>G<sub>z</sub></em>(<em>&phi;<sub>s</sub></em>,&Delta;<em>&phi;</em>)
     *
     * @throws ModelException unknown/unsupported emittance growth model, or
     * unknown/unsupported phase plane
     *
     * @author Christopher K. Allen
     * @since Feb 19, 2009
     *
     * @see EnvelopeTrackerBase#compTransFourierTransform(double)
     * @see EnvelopeTrackerBase#compLongFourierTransform(double)
     * @see EnvelopeTrackerBase#effPhaseSpread(EnvelopeProbe, IdealRfGap)
     */
    protected double compEmitGrowthFunction(PhasePlane plane, double phi_s, double dphi)
            throws ModelException {

        // Compute the Fourier transforms
        // 3D Fourier transform
        double F;
        // double angle Fourier transform
        double FdblAng;
        if (plane == PhasePlane.TRANSVERSE) {

            F = this.compTransFourierTransform(dphi);
            FdblAng = this.compTransFourierTransform(2.0 * dphi);

        } else if (plane == PhasePlane.LONGITUDINAL) {

            F = this.compLongFourierTransform(dphi);
            FdblAng = this.compLongFourierTransform(2.0 * dphi);

        } else {

            String strMsg = "";
            strMsg += "EnvelopeTrackerBase#comp3dEmitGrowthFuncUnifDistr():";
            strMsg += " Serious Error in conditional statement";
            System.err.println(strMsg);
            throw new ModelException(strMsg);
        }

        // Compute the auxiliary functions
        double sin_s = Math.sin(phi_s);
        double sin_2 = sin_s * sin_s;
        double F_2 = F * F;
        double S = 0.5 * (1. - FdblAng);
        double T = F_2 - FdblAng;

        // Compute the growth function and return it.
        double G = S - sin_2 * T;

        return G;
    }

    /**
     * <h2>Transverse Emittance Growth Fourier Transform</h2>
     * <p>
     * Java method for evaluating the Fourier-Bessel transform needed to compute
     * transverse emittance growth due to finite longitudinal phase spread. The
     * technique for computing emittance growth due to phase spread is described
     * in C.K. Allen, <em>et. al.</em>, "Emittance Growth Due to Phase Spread
     * for Proton Beams in Radio Frequency Accelerating Gaps." This work is a
     * generalization of that covered in the Trace3D users' manual, Appendix G,
     * which is in turn based upon the work of M. Weiss (see references below).
     * </p>
     * <p>
     * When considering only one (transverse) phase plane beams the transform
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) evaluated here is given as
     * follows:
     * <br>
     * <br>
     * &nbsp; <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) &equiv;
     * (2/<em>f</em><sub>1</sub>)
     * &int;<em>J</em><sub>0</sub>(&Delta;<em>&phi;s</em>)
     * <em>f</em>(<em>s</em><sup>2</sup>)<em>s</em><sup></sup> <em>ds</em>,
     * <br>
     * <br>
     * where <em>f</em> is the density distribution,
     * <em>J<sub>n</sub></em>(<em>s</em>) is the <em>n</em><sup>th</sup>-order
     * cylindrical Bessel function of the first kind, &Delta;<em>&phi;</em> is
     * the effective phase spread of the equivalent uniform beam,
     * <em>s</em> is the transform variable. and <em>f<sub>k</sub></em> is the
     * number
     * <br>
     * <br>
     * &nbsp; <em>f<sub>k</sub></em> &equiv; &int;
     * <em>f</em>(<em>s</em>)<em>s<sup>k</sup></em> <em>ds</em>.
     * <br>
     * <br>
     * Both integrals are taken from 0 to &infin;.
     * </p>
     * <p>
     * When considering three spatial dimensions the transform
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) evaluated here is given as
     * follows:
     * <br>
     * <br>
     * &nbsp; <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) &equiv;
     * (2/<em>f</em><sub>3/2</sub>)
     * &int;[<em>j</em><sub>0</sub>(&Delta;<em>&phi;s</em>) +
     * <em>j</em><sub>2</sub>(&Delta;<em>&phi;s</em>)]
     * <em>f</em>(<em>s</em><sup>2</sup>)<em>s</em><sup>4</sup> <em>ds</em>,
     * <br>
     * <br>
     * where <em>f</em> is the density distribution,
     * <em>j<sub>n</sub></em>(<em>s</em>) is the <em>n</em><sup>th</sup>-order
     * spherical Bessel function of the first kind, &Delta;<em>&phi;</em> is the
     * effective phase spread of the equivalent uniform beam, and
     * <em>s</em> is the transform variable. Again, both integrals are taken
     * from 0 to &infin;.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * &middot; This method actually falls out of the calculation for
     * &lt;x<sup>2</sup>sin <em>&phi;</em>(<em>z</em>)&gt;. The assumption that
     * <em>x</em> and <em>z</em> are uncorrelated yields the result
     * <br>
     * <br>
     * &nbsp; &lt;x<sup>2</sup>sin <em>&phi;</em>(<em>z</em>)&gt; =
     * &lt;x<sup>2</sup>&gt; sin <em>&phi;<sub>s</sub></em>
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>)
     * <br>
     * <br>
     * where <em>F<sub>t</sub></em>(<em>d&phi;</em>) =
     * &lt;sin(&Delta;<em>&phi;</em>)&gt; is this method, sin
     * <em>&phi;<sub>s</sub></em> is the synchronous particle phase, and
     * &Delta;<em>&phi;</em> is the <em>effective</em> phase spread of the
     * distribution.
     * <br>
     * &middot; The value of &lt;sin<sup>2</sup> <em>&phi;</em>)(<em>z</em>)&gt;
     * can also be computed from this method. The formula is
     * <br>
     * <br>
     * &nbsp; &lt;sin<sup>2</sup> <em>&phi;</em>(<em>z</em>)&gt; =
     * <em>S<sub>t</sub></em>(&Delta;<em>&phi;</em>) + sin<sup>2</sup>
     * <em>&phi;<sub>s</sub></em>
     * <em>F<sub>t</sub></em>(2&Delta;<em>&phi;</em>)
     * <br>
     * <br>
     * where
     * <br>
     * <br>
     * &nbsp; <em>S<sub>t</sub></em>(&Delta;<em>&phi;</em>) &equiv; &frac12;[1 -
     * <em>F<sub>t</sub></em>(2&Delta;<em>&phi;</em>)]
     * <br>
     * <br>
     * has analogy with sin<sup>2</sup> &Delta;<em>&phi;</em>
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss, "Bunching of Intense Proton Beams with Six-Dimensional
     * Matching to the Linac Acceptance", CERN/MPS/LI report 73-2, Geneva,
     * Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, "Trace 3-D Documentation", LANL
     * Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth Due
     * to Phase Spread for Proton Beams in Radio Frequency Accelerating Gaps",
     * (in preparation).
     * </p>
     *
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     * @return The value of transform
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) for the uniform
     * distribution.
     *
     * @throws ModelException unsupported/unknown emittance growth model
     *
     * @author Christopher K. Allen
     * @since Feb 17, 2009
     *
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double compTransFourierTransform(double dphi)
            throws ModelException {

        // the emittance growth model we are using
        EmitGrowthModel model;
        // transform value for emittance growth model
        double Ft;

        model = this.getEmitGrowthModel();
        if (model == EmitGrowthModel.TRACE3D) {

            Ft = this.fourierTransTrace3d(dphi);

        } else if (model == EmitGrowthModel.UNIFORM1D) {

            Ft = this.fourierTrans1dUniform(dphi);

        } else if (model == EmitGrowthModel.GAUSSIAN1D) {

            Ft = this.fourierTrans1dGaussian(dphi);

        } else if (model == EmitGrowthModel.UNIFORM3D) {

            Ft = this.fourierTrans3dUniform(dphi);

        } else if (model == EmitGrowthModel.GAUSSIAN3D) {

            Ft = this.fourierTrans3dGaussian(dphi);

        } else {

            String strMsg = "";
            strMsg += "EnvelopeTrackerBase#compTransFourierTransform():";
            strMsg += " Serious Error in conditional statement";
            System.err.println(strMsg);
            throw new ModelException(strMsg);

        }

        return Ft;
    }

    /**
     * <h2>Longitudinal Fourier Transform for Uniform Distribution</h2>
     * <p>
     * Java method for evaluating the Fourier-Bessel transform needed to compute
     * longitudinal emittance growth due to finite longitudinal phase spread in
     * RF accelerating gaps. For information on this effect see due to phase
     * spread is described in C.K. Allen, <em>et. al.</em>, "Emittance Growth
     * Due to Phase Spread for Proton Beams in Radio Frequency Accelerating
     * Gaps." This work is a generalization of that covered in the Trace3D
     * users' manual, Appendix G for the longitudinal direction. M. Weiss
     * treated the transverse direction (see references below).
     * </p>
     * <p>
     * When considering only one (uncorrelated) phase plane beams the transform
     * <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) evaluated here is given as
     * follows:
     * <br>
     * <br>
     * &nbsp; <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) &equiv;
     * (2/<em>f</em><sub>1</sub>) &int;
     * [<em>J</em><sub>0</sub>(&Delta;<em>&phi;s</em>) -
     * <em>J</em><sub>2</sub>(&Delta;<em>&phi;s</em>)]
     * <em>f</em>(<em>s</em><sup>2</sup>)<em>s</em><sup>3</sup> <em>ds</em>,
     * <br>
     * <br>
     * where <em>f</em> is the density distribution,
     * <em>J<sub>n</sub></em>(<em>s</em>) is the <em>n</em><sup>th</sup>-order
     * cylindrical Bessel function of the first kind, &Delta;<em>&phi;</em> is
     * the effective phase spread of the equivalent uniform beam,
     * <em>s</em> is the transform variable. and <em>f<sub>k</sub></em> is the
     * number
     * <br>
     * <br>
     * &nbsp; <em>f<sub>k</sub></em> &equiv; &int;
     * <em>f</em>(<em>s</em>)<em>s<sup>k</sup></em> <em>ds</em>.
     * <br>
     * <br>
     * Both integrals are taken from 0 to &infin;.
     * </p>
     * <p>
     * When considering three spatial dimensions the transform
     * <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) evaluated here is given as
     * follows:
     * <br>
     * <br>
     * &nbsp; <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) &equiv;
     * (2/<em>f</em><sub>3/2</sub>)
     * &int;[<em>j</em><sub>0</sub>(&Delta;<em>&phi;s</em>) -
     * 2<em>j</em><sub>2</sub>(&Delta;<em>&phi;s</em>)]
     * <em>f</em>(<em>s</em><sup>2</sup>)<em>s</em><sup>4</sup> <em>ds</em>,
     * <br>
     * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; =
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) -
     * (2/<em>f</em><sub>3/2</sub>)
     * &int;<em>j</em><sub>2</sub>(&Delta;<em>&phi;s</em>)]
     * <em>f</em>(<em>s</em><sup>2</sup>)<em>s</em><sup>4</sup> <em>ds</em>,
     * <br>
     * <br>
     * where <em>f</em> is the density distribution,
     * <em>j<sub>n</sub></em>(<em>s</em>) is the <em>n</em><sup>th</sup>-order
     * spherical Bessel function of the first kind, &Delta;<em>&phi;</em> is the
     * effective phase spread of the equivalent uniform beam, and <em>s</em> is
     * the transform variable. The number <em>f<sub>k</sub></em> is as before.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * &middot; This method falls out of the computation for
     * &lt;x<sup>2</sup>sin <em>&phi;</em>(<em>z</em>)&gt;. At least when
     * considering the longitudinal phase plane independently, it can be shown
     * that
     * <br>
     * <br>
     * &nbsp; &lt;z<sup>2</sup>sin <em>&phi;</em>(<em>z</em>)&gt; =
     * &lt;z<sup>2</sup>&gt; sin <em>&phi;<sub>s</sub></em>
     * <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>)
     * <br>
     * <br>
     * where <em>F<sub>z</sub></em>(<em>d&phi;</em>) =
     * &lt;sin(&Delta;<em>&phi;</em>)&gt; is this method, sin
     * <em>&phi;<sub>s</sub></em> is the synchronous particle phase, and
     * &Delta;<em>&phi;</em> is the <em>effective</em> phase spread of the
     * distribution.
     * <br>
     * &middot; The technique for computing longitudinal emittance growth is not
     * covered in the Trace3D manual. A two-term power series expansion for this
     * function is simply stated, but no development is presented.
     * <br>
     * &middot; The result returned by this method has a different power series
     * expansion about &Delta;<em>&phi;</em> = 0 than that presented in the
     * Trace3D manual.
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss, "Bunching of Intense Proton Beams with Six-Dimensional
     * Matching to the Linac Acceptance", CERN/MPS/LI report 73-2, Geneva,
     * Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, "Trace 3-D Documentation", LANL
     * Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth Due
     * to Phase Spread for Proton Beams in Radio Frequency Accelerating Gaps",
     * (in preparation).
     * </p>
     *
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     * @return The value of transform
     * <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) for the uniform
     * distribution.
     *
     * @throws ModelException unsupported/unknown emittance growth model
     *
     * @author Christopher K. Allen
     * @since Feb 17, 2009
     *
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see #compTransFourierTransform(double)
     * @see #compEmitGrowthFunction(PhasePlane, double, double)
     * @see xal.model.elem.IdealRfGap
     */
    protected double compLongFourierTransform(double dphi)
            throws ModelException {

        // the emittance growth model we are using
        EmitGrowthModel model;
        // transform value for emittance growth model
        double Fz;

        model = this.getEmitGrowthModel();
        if (model == EmitGrowthModel.TRACE3D) {

            Fz = this.fourierLongTrace3d(dphi);

        } else if (model == EmitGrowthModel.UNIFORM1D) {

            Fz = this.fourierLong1dUniform(dphi);

        } else if (model == EmitGrowthModel.GAUSSIAN1D) {

            Fz = this.fourierLong1dGaussian(dphi);

        } else if (model == EmitGrowthModel.UNIFORM3D) {

            Fz = this.fourierLong3dUniform(dphi);

        } else if (model == EmitGrowthModel.GAUSSIAN3D) {

            Fz = this.fourierLong3dGaussian(dphi);

        } else {

            String strMsg = "";
            strMsg += "EnvelopeTrackerBase#compLongFourierTransform():";
            strMsg += " Serious Error in conditional statement";
            System.err.println(strMsg);
            throw new ModelException(strMsg);

        }

        return Fz;
    }

    //
    //  Fourier Transforms
    //
    //
    //  Trace3D Transforms
    //
    /**
     * <h2>Transverse Fourier Transform given by Trace3D </h2>
     * <p>
     * This method returns the longitudinal Fourier-Bessel transform needed to
     * compute emittance growth from finite phase spread in an RF accelerating
     * gap. Here we return the exact same value as Trace3D. It considers a
     * uniform beam in three spatial dimensions.
     * </p>
     * <p>
     * The method computes the kluge of &lt;sin(&phi;)&gt;. In this calculation
     * we assume that the transverse and longitudinal phase planes are
     * uncorrelated and that the beam distribution is a uniform ellipsoid
     * (that's why the sinc() function pops up). Pretty restrictive - Sacherer's
     * theorem does not apply here.
     * </p>
     * <p>
     * This quantity is used when computing the transverse emittance increase in
     * an RF gap due to a finite phase spread in the beam.
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * &middot; This method is used to approximate
     * &lt;x<sup>2</sup>sin(&phi;)&gt;, which is at least third order in the
     * phase coordinates.
     * <br>
     *
     * &middot; The assumption that <em>x</em> and <em>z</em> are not correlated
     * yields the result
     * <br>
     * <br>
     * &nbsp; &lt;x<sup>2</sup>sin(&phi;)&gt; = &lt;x<sup>2</sup>&gt;
     * <em>f</em>(<em>d&phi;</em>)
     * <br>
     * <br>
     * where <em>f</em>(<em>d&phi;</em>) &equiv; &lt;sin(<em>d&phi;</em>)is this
     * method, and <em>d&phi;</em> is the "<em>phase spread</em>" of the
     * distribution. The phase spread is defined
     * <br>
     * <br>
     * &nbsp;   <em>d&phi;</em> = &lt;(<em>&phi; -
     * &phi;<sub>s</sub></em>)<sup>2</sup>&gt;<sup>1/2</sup>
     * <br>
     * <br>
     * where <em>&phi;<sub>s</sub></em> is the synchronous particle phase.
     * </p>
     * <p>
     * See K.R. Crandall and D.P. Rusthoi,
     * </p>
     * <ul><li>
     * "Trace 3-D Documentation", LANL Report LA-UR-97-887 (1997), Appendix F.
     * </li></ul>
     *
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     *
     * @return The value of transform
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) given by Trace3D which is
     * the value of &lt;sin(<em>&phi;</em>)&gt; =
     * <em>f</em>(<var>dp</var>).
     *
     * @author Christopher K. Allen
     * @since Feb 17, 2009
     *
     * @see #momentSine(double)
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierTransTrace3d(double dphi) {

        double dp_2 = dphi * dphi;

        if (dphi < 0.1) {
            // Avoid singularity at zero - Taylor expansion
            return 1.0 - dp_2 / 14.0 + dp_2 * dp_2 / 504.0;

        }

        // Use full expression
        double T = 3.0 / dp_2;

        double sinc = ElementaryFunction.sinc(dphi);
        double cos = Math.cos(dphi);

        return (5.0 * T) * (sinc * (T - 1.0) - cos * T);
    }

    /**
     * <h2>Longitudinal Fourier Transform given by Trace3D </h2>
     * <p>
     * This method returns the longitudinal Fourier-Bessel transform needed to
     * compute emittance growth from finite phase spread in an RF accelerating
     * gap. Here we return the exact same value as Trace3D. It considers a
     * uniform beam in three spatial dimensions.
     * </p>
     * <p>
     * The returned value (which is not derived or explained in the Trace3D
     * manual) is
     * <br>
     * <br>
     * &nbsp; <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) = 1 -
     * &Delta;<em>&phi;</em><sup>2</sup>/12
     * <br>
     * <br>
     * This value is taken from the Trace3D code. The manual actually quotes it
     * as 1 + &Delta;<em>&phi;</em><sup>2</sup>/12.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss, "Bunching of Intense Proton Beams with Six-Dimensional
     * Matching to the Linac Acceptance", CERN/MPS/LI report 73-2, Geneva,
     * Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, "Trace 3-D Documentation", LANL
     * Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth Due
     * to Phase Spread for Proton Beams in Radio Frequency Accelerating Gaps",
     * (in preparation).
     * </p>
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     * @return The value of transform
     * <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) given by Trace3D.
     *
     *
     * @author Christopher K. Allen
     * @since Feb 17, 2009
     *
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see #compTransFourierTransform(double)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierLongTrace3d(double dphi) {

        double Fz = 1.0 - dphi * dphi / 12.0;

        return Fz;
    }

    //
    //  Three Spatial Dimension Transforms
    //
    /**
     * <h2>Longitudinal Fourier Transform for 3D Uniform Distribution</h2>
     * <p>
     * This method return the longitudinal Fourier-Bessel transform needed to
     * compute emittance growth from finite phase spread in an RF accelerating
     * gap. Here we consider the case when the beam is uniformly distributed
     * over three spatial dimensions. For this distribution
     * <em>f</em>(<em>s</em>), we find that
     * <br>
     * <br>
     * &nbsp; <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) = 15
     * <em>j</em><sub>2</sub>(&Delta;<em>&phi;</em>)/&Delta;<em>&phi;</em><sup>2</sup>
     * - 15 <em>j</em><sub>3</sub>(&Delta;<em>&phi;</em>)/&Delta;<em>&phi;</em>
     * <br>
     * <br>
     * For small arguments we avoid any numerically singular behavior at
     * &Delta;<em>&phi;</em> = 0 by Taylor expanding. We have
     * <br>
     * <br>
     * &nbsp; <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) &asymp; 1 -
     * <em>x</em><sup>2</sup>/7 + <em>x</em><sup>4</sup>/168 -
     * <em>x</em><sup>6</sup>/8316 + 5<em>x</em><sup>8</sup>/3459456 +
     * O(<em>x</em><sup>17/2</sup>)
     * <br>
     * <br>
     * Currently we are expanding to sixth order.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     * @return The value of transform
     * <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) for the 3D uniform
     * distribution.
     *
     *
     * @author Christopher K. Allen
     * @since Feb 17, 2009
     *
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see #compTransFourierTransform(double)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierLong3dUniform(double dphi) {

        if (Math.abs(dphi) < EnvelopeTrackerBase.SMALL_ARG) {

            // Numerically unstable about dphi=0, compute expansion about dphi=0 
            double x_2 = dphi * dphi;
            double x_4 = x_2 * x_2;
            double x_6 = x_2 * x_4;

            return 1.0 - x_2 / 7.0 + x_4 / 168.0 - x_6 / 8316.0;
        }

        // Numerically stable, compute exact expresson
        double dphi_2 = dphi * dphi;
        double j2 = BesselFunction.j2(dphi);
        double j3 = BesselFunction.j3(dphi);
        double Fz = 15.0 * (j2 / dphi_2 - j3 / dphi);

        return Fz;
    }

    /**
     * <h2>Transverse Fourier Transform for 3D Uniform Distribution</h2>
     * <p>
     * This method return the transverse Fourier-Bessel transform needed to
     * compute emittance growth from finite phase spread in an RF accelerating
     * gap. Here we consider the case when the beam is uniformly distributed
     * over three spatial dimensions. For this distribution
     * <em>f</em>(<em>s</em>), we find that
     * <br>
     * <br>
     * &nbsp; <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) = 15
     * <em>j</em><sub>2</sub>(&Delta;<em>&phi;</em>)/&Delta;<em>&phi;</em><sup>2</sup>
     * <br>
     * <br>
     * For small arguments we avoid any numerically singular behavior at
     * &Delta;<em>&phi;</em> = 0 by Taylor expanding. We have
     * <br>
     * <br>
     * &nbsp; <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) &asymp; 1 -
     * <em>x</em><sup>2</sup>/14 + <em>x</em><sup>4</sup>/504 -
     * <em>x</em><sup>6</sup>/33264 + <em>x</em><sup>8</sup>/3459456 +
     * O(<em>x</em><sup>17/2</sup>)
     * <br>
     * <br>
     * Currently we are expanding to sixth order.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     * @return The value of transform
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) for the uniform
     * distribution.
     *
     *
     * @author Christopher K. Allen
     * @since Feb 17, 2009
     *
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierTrans3dUniform(double dphi) {

        if (Math.abs(dphi) < EnvelopeTrackerBase.SMALL_ARG) {

            // Numerically unstable about dphi=0, compute expansion about dphi=0 
            double x_2 = dphi * dphi;
            double x_4 = x_2 * x_2;
            double x_6 = x_2 * x_4;

            return 1.0 - x_2 / 14.0 + x_4 / 504.0 - x_6 / 33264.0;

        }

        // Numerically stable, compute exact expression
        double dphi_2 = dphi * dphi;
        double j2 = BesselFunction.j2(dphi);
        double Ft = 15.0 * j2 / dphi_2;

        return Ft;
    }

    /**
     * <h2>Transverse Fourier Transform for 3D Gaussian Distribution</h2>
     * <p>
     * This method return the transverse Fourier-Bessel transform needed to
     * compute emittance growth from finite phase spread in an RF accelerating
     * gap. Here we consider the case when the beam has a Gaussian distribution
     * over three spatial dimensions. For this distribution
     * <em>f</em>(<em>s</em>), we find that
     * <br>
     * <br>
     * &nbsp; <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) =
     * <em>e</em><sup>-&Delta;<em>&phi;</em>&circ;2/10</sup>
     * <br>
     * <br>
     * There is no need for a small argument expansion since the above
     * expression is numerically stable.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     * @return The value of transform
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) for the Gaussian
     * distribution.
     *
     *
     * @author Christopher K. Allen
     * @since Feb 17, 2009
     *
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierTrans3dGaussian(double dphi) {

        // Numerically stable, compute exact expression
        double dphi_2 = dphi * dphi;
        double Ft = Math.exp(-dphi_2 / 10.0);

        return Ft;
    }

    /**
     * <h2>Longitudinal Fourier Transform for 3D Gaussian Distribution</h2>
     * <p>
     * This method return the longitudinal Fourier-Bessel transform needed to
     * compute emittance growth from finite phase spread in an RF accelerating
     * gap. Here we consider the case when the beam has a Gaussian distribution
     * over the three spatial dimensions. For this distribution
     * <em>f</em>(<em>s</em>), we find that
     * <br>
     * <br>
     * &nbsp; <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) = (1 -
     * &Delta;<em>&phi;</em><sup>2</sup>/5)
     * <em>e</em><sup>-&Delta;<em>&phi;</em>&circ;2/10</sup>
     * <br>
     * <br>
     * There is no need for a small argument expansion since the above
     * expression is numerically stable.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     * @return The value of transform
     * <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) for the 3D Gaussian
     * distribution.
     *
     *
     * @author Christopher K. Allen
     * @since Feb 17, 2009
     *
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see #compTransFourierTransform(double)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierLong3dGaussian(double dphi) {

        // Numerically stable, compute exact expression
        double dphi_2 = dphi * dphi;
        double deriv = 1.0 - dphi_2 / 5.0;
        double Ft = Math.exp(-dphi_2 / 10.0);
        double Fz = deriv * Ft;

        return Fz;
    }

    //
    //  Single (Uncorrelated) Phase Space Distributions
    //
    /**
     * <h2>Transverse Fourier Transform for Single Phase Plane</h2>
     * <p>
     * This method return the transverse Fourier-Bessel transform needed to
     * compute emittance growth from finite phase spread in an RF accelerating
     * gap. Here we consider the case when the beam is uniformly distributed in
     * one transverse phase plane. For this distribution <em>f</em>(<em>s</em>),
     * we find that
     * <br>
     * <br>
     * &nbsp; <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) = 2
     * <em>J</em><sub>1</sub>(&Delta;<em>&phi;</em>)/&Delta;<em>&phi;</em>
     * <br>
     * <br>
     * For small arguments we avoid any numerically singular behavior at
     * &Delta;<em>&phi;</em> = 0 by Taylor expanding. We have
     * <br>
     * <br>
     * &nbsp; <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) &asymp; 1 -
     * <em>x</em><sup>2</sup>/8 + <em>x</em><sup>4</sup>/192 -
     * <em>x</em><sup>6</sup>/9216 + <em>x</em><sup>8</sup>/737280 +
     * O(<em>x</em><sup>9</sup>)
     * <br>
     * <br>
     * Currently we are expanding to sixth order.
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     * @return The value of transform
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) for the single phase plane
     * uniform distribution.
     *
     *
     * @author Christopher K. Allen
     * @since Feb 25, 2009
     *
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierTrans1dUniform(double dphi) {

        if (Math.abs(dphi) < EnvelopeTrackerBase.SMALL_ARG) {

            // Numerically unstable about dphi=0, compute expansion about dphi=0 
            double x_2 = dphi * dphi;
            double x_4 = x_2 * x_2;
            double x_6 = x_2 * x_4;

            return 1.0 - x_2 / 8.0 + x_4 / 192.0 - x_6 / 9216.0;

        }

        // Numerically stable, compute exact expression
        double J1 = BesselFunction.J1(dphi);
        double Ft = 2.0 * J1 / dphi;

        return Ft;
    }

    /**
     * <h2>Longitudinal Fourier Transform for Single Phase Plane</h2>
     * <p>
     * This method return the longitudinal Fourier-Bessel transform needed to
     * compute emittance growth from finite phase spread in an RF accelerating
     * gap. Here we consider the case when the beam is uniformly distributed in
     * one transverse phase plane. For this distribution <em>f</em>(<em>s</em>),
     * we find that
     * <br>
     * <br>
     * &nbsp; <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) = 8
     * <em>J</em><sub>2</sub>(&Delta;<em>&phi;</em>)/&Delta;<em>&phi;</em><sup>2</sup>
     * - 8 <em>J</em><sub>3</sub>(&Delta;<em>&phi;</em>)/&Delta;<em>&phi;</em>
     * <br>
     * <br>
     * For small arguments we avoid any numerically singular behavior at
     * &Delta;<em>&phi;</em> = 0 by Taylor expanding. We have
     * <br>
     * <br>
     * &nbsp; <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) &asymp; 1 -
     * <em>x</em><sup>2</sup>/4 + 5<em>x</em><sup>4</sup>/384 -
     * 7<em>x</em><sup>6</sup>/23040 + <em>x</em><sup>8</sup>/245760 +
     * O(<em>x</em><sup>9</sup>)
     * <br>
     * <br>
     * Currently we are expanding to sixth order.
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     * @return The value of transform
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) for the single phase plane
     * uniform distribution.
     *
     *
     * @author Christopher K. Allen
     * @since Feb 25, 2009
     *
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierLong1dUniform(double dphi) {

        if (Math.abs(dphi) < EnvelopeTrackerBase.SMALL_ARG) {

            // Numerically unstable about dphi=0, compute expansion about dphi=0 
            double x_2 = dphi * dphi;
            double x_4 = x_2 * x_2;
            double x_6 = x_2 * x_4;

            return 1.0 - x_2 / 4.0 + 5.0 * x_4 / 384.0 - 7.0 * x_6 / 23040.0;

        }

        // Numerically stable, compute exact expression
        double dphi_2 = dphi * dphi;
        double J2 = BesselFunction.Jn(2, dphi);
        double J3 = BesselFunction.Jn(3, dphi);
        double Fz = 8.0 * (J2 / dphi_2 - J3 / dphi);

        return Fz;
    }

    /**
     * <h2>Transverse Fourier Transform for 2D Gaussian Distribution</h2>
     * <p>
     * This method return the transverse Fourier-Bessel transform needed to
     * compute emittance growth from finite phase spread in an RF accelerating
     * gap. Here we consider the case when the beam is Gaussian distributed in
     * each (uncorrelated) phase plane. For this distribution
     * <em>f</em>(<em>s</em>), we find that
     * <br>
     * <br>
     * &nbsp; <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) =
     * <em>e</em><sup>-&Delta;<em>&phi;</em>&circ;2/8</sup>
     * <br>
     * <br>
     * There is no need for a small argument expansion since the above
     * expression is numerically stable.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     * @return The value of transform
     * <em>F<sub>t</sub></em>(&Delta;<em>&phi;</em>) for the Gaussian
     * distribution.
     *
     *
     * @author Christopher K. Allen
     * @since Feb 25, 2009
     *
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierTrans1dGaussian(double dphi) {

        // Numerically stable, compute exact expression
        double dphi_2 = dphi * dphi;
        double Ft = Math.exp(-dphi_2 / 8.0);

        return Ft;
    }

    /**
     * <h2>Longitudinal Fourier Transform for 2D Gaussian Distribution</h2>
     * <p>
     * This method return the longitudinal Fourier-Bessel transform needed to
     * compute emittance growth from finite phase spread in an RF accelerating
     * gap. Here we consider the case when the beam is Gaussian distributed in
     * each (uncorrelated) phase plane. For this distribution
     * <em>f</em>(<em>s</em>), we find that
     * <br>
     * <br>
     * &nbsp; <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) = (1 -
     * &Delta;<em>&phi;</em><sup>2</sup>/4)
     * <em>e</em><sup>-&Delta;<em>&phi;</em>&circ;2/8</sup>
     * <br>
     * <br>
     * There is no need for a small argument expansion since the above
     * expression is numerically stable.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     *
     * @param dphi effective phase spread &Delta;<em>&phi;</em> (half-width) of
     * equivalent uniform beam in <strong>radians</strong>
     *
     * @return The value of transform
     * <em>F<sub>z</sub></em>(&Delta;<em>&phi;</em>) for the Gaussian
     * distribution in single phase plane.
     *
     *
     * @author Christopher K. Allen
     * @since Feb 25, 2009
     *
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see #compTransFourierTransform(double)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierLong1dGaussian(double dphi) {

        // Numerically stable, compute exact expression
        double dphi_2 = dphi * dphi;
        double deriv = 1.0 - dphi_2 / 4.0;
        double Ft = Math.exp(-dphi_2 / 8.0);
        double Fz = deriv * Ft;

        return Fz;
    }

    /**
     * <h2>Effective Phase Spread for Equivalent Uniform Beam</h2>
     * <p>
     * Compute the longitudinal phase spread of the bunch with respect to the RF
     * in an RF gap element (based on Trace3D RfGap.f) The phase spread is
     * computed assuming a <strong>uniform</strong> distribution. The returned
     * value is then the <em>effective</em> phase spread for the equivalent
     * uniform beam. (see below).
     * </p>
     * <p>
     * In XAL, longitudinal coordinate <em>z</em> is the "phase spread", but in
     * meters. To convert to phase spread <em>d&phi;</em> in radians we have
     * <br>
     * <br>
     * &nbsp;   <em>d&phi;</em> = 2&pi;<em>z</em>/(&beta;&lambda;)
     * <br>
     * <br>
     * where &lambda; is the wavelength of the RF. To simplify matters make the
     * definition
     * <br>
     * <br>
     * &nbsp; <em>k</em> &equiv; 2&pi;/&beta;&lambda;,
     * <br>
     * <br>
     * which is the synchronous particle wave number. So, for
     * &lt;<em>d&phi;</em><sup>2</sup>&gt; we get
     * <br>
     * <br>
     * &nbsp; &lt;<em>d&phi;</em><sup>2</sup>&gt; =
     * <em>k</em><sup>2</sup>&lt;<em>z</em><sup>2</sup>&gt;.
     * <br>
     * <br>
     * Note then that &lt;<em>d&phi;</em><sup>2</sup>&gt;<sup>1/2</sup> is the
     * <em>RMS</em> phase spread.
     * </p>
     * <p>
     * I am using the mid-gap value for &beta;, that is, &beta; average. And,
     * thus,
     * <em>k</em> is also the mid-gap wave number.
     * </p>
     * <p>
     * We need to multiply &lt;z<sup>2</sup>&gt; by 5 to get the
     * "(three-dimensional) equivalent uniform beam" longitudinal semi-axis
     * (even though there is no uniform equivalent beam for emittance growth).
     * </p>
     * <p>
     * Putting this all together gives the following value for the
     * <em>effective</em> phase phase spread for the equivalent uniform beam,
     * &Delta;<em>&phi;</em>:
     * <br>
     * <br>
     * &nbsp; &Delta;<em>&phi;</em> =
     * <em>k</em>&lt;5<em>z</em><sup>2</sup>&gt;<sup>1/2</sup>.
     * <br>
     * <br>
     * The above is the value returned by this method. Note that
     * &Delta;<em>&phi;</em>
     * is also referred to as the beam <em>half-length</em> (with respect to the
     * RF phase).
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * <br>
     * &middot; Note that the RMS phase spread can also be represented as
     * <br>
     * <br>
     * &nbsp;   <em>d&phi;</em> = &lt;[<em>&phi;(s) -
     * &phi;<sub>s</sub></em>]<sup>2</sup>&gt;<sup>1/2</sup>
     * <br>
     * <br>
     * where <em>&phi;<sub>s</sub></em> is the synchronous particle phase.
     * </p>
     *
     * @param probe probe containing relativistic data
     * @param elem the RF gap modeling element creating the bunch phase spread
     *
     * @return phase spread (half width) &Delta;<em>&phi;</em> &equiv;
     * &lt;5<em>d&phi;</em><sup>2</sup>&gt;<sup>1/2</sup>
     * for this probe (<strong>radians</strong>)
     *
     * @author Hiroyuki Sako
     * @author Christopher K. Allen
     */
    protected double effPhaseSpread(EnvelopeProbe probe, IdealRfGap elem) {

        // Compute the RF wavelength
        double lambda = elem.wavelengthRF();

        // Compute the mid-gap velocity
        double beta = elem.betaMidGap(probe);

        // Compute the mid-gap wave number
        double k = (2.0 * Math.PI) / (beta * lambda);

        // Compute the longitudinal phase spread 
        double z_2 = 5.0 * probe.getCovariance().getElem(IND.Z, IND.Z);
        double dphi = k * Math.sqrt(z_2);

        return dphi;
    }

    /**
     * <p>
     * Compute the phase spread of the bunch for a probe (based on Trace3D
     * RfGap.f)
     * </p>
     * <h3>CKA Notes:</h3>
     * <p>
     * - This method needs to be optimized now that I understand what it is
     * doing. In XAL, longitudinal coordinate <em>z</em> is the "phase spread",
     * but in meters. To convert to phase spread <em>&delta;&phi;</em> in
     * radians we have
     * <br>
     * <br>
     * &nbsp; &nbsp; &delta;&phi; = 2&pi;<em>z</em>/(&beta;&lambda;) ,
     * <br>
     * <br>
     * where &lambda; is the wavelength of the RF. So, for
     * &lt;&delta;&phi;<sup>2</sup>&gt; we get
     * <br>
     * <br>
     * &nbsp; &nbsp; &lt;&delta;&phi;<sup>2</sup>&gt; =
     * &lt;<em>z</em><sup>2</sup>&gt;2&pi;<em>f</em>
     *                                                /(&beta;<em>c</em>) ,
     * <br>
     * <br>
     * where <em>f</em> is the RF frequency of the gap and c is the speed of
     * light.
     * <br>
     * <br>
     * - For the optional computation <strong>phaseSpreadT3d</strong> (which
     * apparently is not used) I am not sure what is happening, or why
     * &lt;y'y'&gt; is significant?
     * </p>
     *
     * @param probe we are computing the phase spread for this probe at the
     * current <code>IdealRfGap</code> condition
     * @param gap the RF gap modeling element creating the bunch phase spread
     *
     * @return phase spread (half width) for this probe
     * (<strong>radian</strong>)
     *
     * @author Hiroyuki Sako
     * @author Christopher K. Allen
     * @version Nov 6, 2013
     */
    protected double phaseSpread(EnvelopeProbe probe, IdealRfGap gap) {

        // The answer
        double dblPhaseSpreadCalc = 0.0;

        //sako
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();
        double Wbar = Wi + gap.energyGain(probe) / 2.0;

        //def
        TraceXalUnitConverter t3dxal = TraceXalUnitConverter.newConverter(gap.getFrequency(), Er, Wi);

        Twiss[] twiss = probe.getCovariance().computeTwiss();
        Twiss t3dtwissz = t3dxal.xalToTraceLongitudinal(twiss[2]);

        double emitz = t3dtwissz.getEmittance();
        double betaz = t3dtwissz.getBeta();

        //radian
        dblPhaseSpreadCalc = Math.sqrt(emitz * betaz) * 2 * Math.PI / 360;
        //betaaverage is  not there!!! is it ok?

        //sako for test. Try to use average energy to calculate dphiav
        boolean phaseSpreadT3d = false;
        if (phaseSpreadT3d) {

            double gbar = Wbar / Er + 1.0;
            double bbar = Math.sqrt(1.0 - 1.0 / (gbar * gbar));
            double clight = IProbe.LIGHT_SPEED;
            double freq = gap.getFrequency();
            double wavel = clight / freq;

            //this need to be convert to t3d unit
            CovarianceMatrix matCorXAL = probe.getCovariance();

            double sigma55 = matCorXAL.getElem(4, 4);
            double dphit3d = 2. * Math.PI * Math.sqrt(sigma55) / (bbar * wavel);

            //temp
            dblPhaseSpreadCalc = dphit3d;
        }

        return dblPhaseSpreadCalc;
    }

    /**
     * Moved from <code>IdealRfGap</code>.
     */
    protected double correctTransFocusingPhaseSpread(EnvelopeProbe probe, IdealRfGap gap) {
        double dphi = this.phaseSpread(probe, gap);
        double cor = 1.;
        cor = 1 - dphi * dphi / 14;
        //      if (dphi != 0) {
        if (dphi > 0.1) {
            cor = 15 / dphi / dphi * (3 / dphi / dphi * (Math.sin(dphi) / dphi - Math.cos(dphi)) - Math.sin(dphi) / dphi);
        }
        //      }
        return cor;
    }

    /**
     * new implementation by sako, 7 Aug 06, to do trans/long simultaneously
     * used in EnvTrackerAdapt, EnvelopeTracker
     *
     * @param probe envelope probe object (something with emittance and moments)
     * @param gap the RF gap modeling element creating the bunch phase spread
     */
    protected double[] correctSigmaPhaseSpread(EnvelopeProbe probe, IdealRfGap gap) {

        double[] dfac = new double[2];

        double dfacT = 0d;
        double dfacL = 0d;

        double phi = gap.getPhase();
        double dphi = this.phaseSpread(probe, gap);

        double tdp = 2 * dphi;
        double sintdp = Math.sin(tdp);

        double f2t = 1 - tdp * tdp / 14;
        if (tdp > 0.1) {
            //APPENDIX F (Trace3D manual)
            f2t = 3 * (sintdp / tdp - Math.cos(tdp)) / tdp / tdp;
            f2t = 15 * (f2t - sintdp / tdp) / tdp / tdp;
        }
        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);
        double G1 = 0.5 * (1 + (sinphi * sinphi - cosphi * cosphi) * f2t);
        double Q = probe.getSpeciesCharge();
        //harmic number
        double h = 1;
        double m = probe.getSpeciesRestEnergy();
        double w = probe.getKineticEnergy();
        double dw = gap.energyGain(probe);
        double wa = w + dw / 2;
        double betagammaa = Math.sqrt(wa / m * (2 + wa / m));

        double wf = w + dw;
        double betagammaf = Math.sqrt(wf / m * (2 + wf / m));

        double clight = IProbe.LIGHT_SPEED;
        double freq = gap.getFrequency();
        double lambda = clight / freq;

        //Kx'
        double cay = h * Math.abs(Q) * Math.PI * gap.getETL() / (m * betagammaa * betagammaa * betagammaf * lambda);
        double f1 = 1 - dphi * dphi / 14;
        if (dphi > 0.1) {
            f1 = 15 / dphi / dphi * (3 / dphi / dphi * (Math.sin(dphi) / dphi - Math.cos(dphi)) - Math.sin(dphi) / dphi);
        }
        dfacT = cay * cay * (G1 - sinphi * sinphi * f1 * f1);

        //longitudinal
        double f2l = 1 - tdp * tdp / 14;
        if (tdp > 0.1) {
            f2l = 3 * (sintdp / tdp - Math.cos(tdp)) / tdp / tdp;
            f2l = 15 * (f2l - sintdp / tdp) / tdp / tdp;
        }

        //this is best
        double cayz = 2 * cay;
        double cayp = cayz * cayz * dphi * dphi;
        dfacL = cayp * (0.125 * cosphi * cosphi + (1. / 576.) * dphi * dphi * sinphi * sinphi);

        dfac[0] = dfacT;
        dfac[1] = dfacL;

        return dfac;
    }

    /**
     * <p>
     * Calculation of emittance increase due to phase spread based on
     * calculations in Trace3d (RfGap.f)
     * </p>
     * <p>
     * Used in EnvTrackerAdapt, EnvelopeTracker
     * </p>
     * <h3>CKA Notes:</h3>
     * <p>
     * - I think this should go in the <strong>Algorithm</strong> class. It
     * expects an <code>EnvelopeProbe</code> - element objects should really not
     * be concerned with the type of probe.
     * </p>
     *
     * @param probe envelope probe object (something with emittance and moments)
     * @param gap the RF gap modeling element creating the bunch phase spread
     *
     * @return the change in emittance after going through this element
     */
    public double correctTransSigmaPhaseSpread(EnvelopeProbe probe, IdealRfGap gap) {

        double dfac = 1;

        double phi = gap.getPhase();
        double dphi = this.phaseSpread(probe, gap);
        double f1 = correctTransFocusingPhaseSpread(probe, gap);
        double tdp = 2 * dphi;
        double f2 = 1 - tdp * tdp / 14;
        if (tdp > 0.1) {
            double sintdp = Math.sin(tdp);
            //APPENDIX F (Trace3D manual)
            f2 = 3 * (sintdp / tdp - Math.cos(tdp)) / tdp / tdp;
            f2 = 15 * (f2 - sintdp / tdp) / tdp / tdp;
        }
        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);
        double G1 = 0.5 * (1 + (sinphi * sinphi - cosphi * cosphi) * f2);
        double Q = probe.getSpeciesCharge();
        //harmic number
        double h = 1;
        double m = probe.getSpeciesRestEnergy();
        double w = probe.getKineticEnergy();
        double dw = gap.energyGain(probe);
        double wa = w + dw / 2;
        double betagammaa = Math.sqrt(wa / m * (2 + wa / m));

        double wf = w + dw;
        double betagammaf = Math.sqrt(wf / m * (2 + wf / m));

        double clight = IProbe.LIGHT_SPEED;
        double freq = gap.getFrequency();
        double lambda = clight / freq;

        //Kx'
        double cay = Math.abs(Q) * h * Math.PI * gap.getETL() / (m * betagammaa * betagammaa * betagammaf * lambda);

        dfac = cay * cay * (G1 - sinphi * sinphi * f1 * f1);

        return dfac;
    }

    /**
     * <p>
     * Calculation of emittance increase due to phase spread based on
     * calculations in Trace3d (RfGap.f)
     * </p>
     * <p>
     * used in EnvTrackerAdapt, EnvelopeTracker
     * </p>
     * <h3>CKA Notes:</h3>
     * <p>
     * - I think this should go in the <strong>Algorithm</strong> class. It
     * expects an <code>EnvelopeProbe</code> - element objects should really not
     * be concerned with the type of probe.
     * </p>
     *
     * @param probe envelope-type probe (something with emittance and moments)
     * @param gap the RF gap modeling element creating the bunch phase spread
     *
     * @return the increase in longitudinal emittance due to finite phase spread
     */
    public double correctLongSigmaPhaseSpread(EnvelopeProbe probe, IdealRfGap gap) {

        double dfac = 1;

        double phi = gap.getPhase();
        double dphi = this.phaseSpread(probe, gap);
        double tdp = 2 * dphi;

        double f2 = 1 - tdp * tdp / 14;
        if (tdp > 0.1) {
            double sintdp = Math.sin(tdp);
            f2 = 3 * (sintdp / tdp - Math.cos(tdp)) / tdp / tdp;
            f2 = 15 * (f2 - sintdp / tdp) / tdp / tdp;
        }
        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);

        double Q = probe.getSpeciesCharge();
        //harmic number
        double h = 1;
        double m = probe.getSpeciesRestEnergy();
        double w = probe.getKineticEnergy();
        double dw = gap.energyGain(probe);
        double wa = w + dw / 2;
        double gammaa = (wa + m) / m;
        double betagammaa = Math.sqrt(wa / m * (2 + wa / m));
        double clight = IProbe.LIGHT_SPEED;
        double freq = gap.getFrequency();
        double lambda = clight / freq;

        double wf = w + dw;
        double betagammaf = Math.sqrt(wf / m * (2 + wf / m));

        //21 jul 06
        double cay = h * Math.PI * gap.getETL() * Math.abs(Q) / (m * betagammaa * betagammaa * betagammaf * lambda);

        double cayz = 2 * cay * gammaa * gammaa;
        double cayp = cayz * cayz * dphi * dphi;
        dfac = cayp * (0.125 * cosphi * cosphi + (1. / 576.) * dphi * dphi * sinphi * sinphi);

        return dfac;
    }
}
