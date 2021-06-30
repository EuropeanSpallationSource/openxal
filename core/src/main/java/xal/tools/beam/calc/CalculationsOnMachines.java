/**
 * CalculationsOnMachines.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 7, 2013
 */
package xal.tools.beam.calc;

import xal.tools.beam.calc.ISimulationResults.ISimLocResults;
import xal.tools.beam.calc.ISimulationResults.ISimEnvResults;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.PhaseMatrix.IND;
import xal.tools.math.r3.R3;
import xal.tools.math.r4.R4;
import xal.tools.math.r6.R6;

/**
 * Class for performing the calculations expressed in the
 * <code>ISimEnvResults</code> interface in the context of a particle beam
 * system without regard to the particle. That is, only properties of the
 * machine are computed, no attributes or properties of the beam are required
 * for the computations.
 *
 *
 * @author Christopher K. Allen
 * @since Nov 7, 2013
 */
public class CalculationsOnMachines extends CalculationEngine implements ISimLocResults<TransferMapState>, ISimEnvResults<TransferMapState> {

    /*
     * Global Operations
     */
    /**
     * Convenience method for computing the transfer matrix between two state
     * locations, say <em>S</em><sub>1</sub>
     * and <em>S</em><sub>2</sub>. Let <em>s</em><sub>0</sub> be the axis location
     * of the beamline entrance, <em>s</em><sub>1</sub> the location of state
     * <em>S</em><sub>1</sub>, and
     * <em>s</em><sub>2</sub> the location of state <em>S</em><sub>2</sub>. Each
     * state object <em>S<sub>n</sub></em>
     * contains the transfer matrix
     * <strong>&Phi;</strong>(<em>s<sub>n</sub></em>,<em>s</em><sub>0</sub>) which takes
     * phases coordinates at the beamline entrance to the position of state
     * <em>S<sub>n</sub></em>. The transfer matrix
     * <strong>&Phi;</strong>(<em>s</em><sub>2</sub>,<em>s</em><sub>1</sub>) taking phase
     * coordinates <strong>z</strong><sub>1</sub>
     * (and covariance matrix <strong>&sigma;</strong><sub>1</sub>) from position
     * <em>s</em><sub>1</sub> to position <em>s</em><sub>2</sub> is then given by
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>&Phi;</strong>(<em>s</em><sub>2</sub>,<em>s</em><sub>1</sub>) =
     * <strong>&Phi;</strong>(<em>s</em><sub>2</sub>,<em>s</em><sub>0</sub>)
     * <strong>&Phi;</strong>(<em>s</em><sub>1</sub>,<em>s</em><sub>0</sub>)<sup>-1</sup> ,
     * <br>
     * <br>
     * where <strong>&Phi;</strong>(<em>s</em><sub>2</sub>,<em>s</em><sub>0</sub>) is the
     * transfer matrix between the beamline entrance <em>s</em><sub>0</sub> and
     * the position <em>s</em><sub>2</sub>
     * of state <em>S</em><sub>2</sub>, and
     * <strong>&Phi;</strong>(<em>s</em><sub>1</sub>,<em>s</em><sub>0</sub>) is the transfer
     * matrix between the beamline entrance <em>s</em><sub>0</sub> and the
     * position <em>s</em><sub>1</sub>
     * of state <em>S</em><sub>1</sub>.
     *
     * @param state1 trajectory state <em>S</em><sub>1</sub> of starting location
     * <em>s</em><sub>1</sub>
     * @param state2 trajectory state <em>S</em><sub>2</sub> of final location
     * <em>s</em><sub>2</sub>
     *
     * @return transfer matrix
     * <strong>&Phi;</strong>(<em>s</em><sub>2</sub>,<em>s</em><sub>1</sub>) between locations
     * <em>s</em><sub>1</sub> and <em>s</em><sub>2</sub>
     *
     * @author Christopher K. Allen
     * @since Jun 23, 2014
     */
    public static PhaseMatrix computeTransferMatrix(TransferMapState state1, TransferMapState state2) {
        PhaseMatrix matPhi1 = state1.getTransferMap().getFirstOrder();
        PhaseMatrix matPhi2 = state2.getTransferMap().getFirstOrder();

        PhaseMatrix matPhi1inv = matPhi1.inverse();
        PhaseMatrix matPhi21 = matPhi2.times(matPhi1inv);

        return matPhi21;
    }

    /**
     * Convenience method for computing the transfer map between two state
     * locations, say <em>S</em><sub>1</sub>
     * and <em>S</em><sub>2</sub>. Let <em>s</em><sub>0</sub> be the axis location
     * of the beamline entrance, <em>s</em><sub>1</sub> the location of state
     * <em>S</em><sub>1</sub>, and
     * <em>s</em><sub>2</sub> the location of state <em>S</em><sub>2</sub>. Each
     * state object <em>S<sub>n</sub></em>
     * contains the transfer map
     * <strong>T</strong>(<em>s<sub>n</sub></em>,<em>s</em><sub>0</sub>) which takes phases
     * coordinates at the beamline entrance to the position of state
     * <em>S<sub>n</sub></em>. The transfer map
     * <strong>T</strong>(<em>s</em><sub>2</sub>,<em>s</em><sub>1</sub>) taking phase
     * coordinates <strong>z</strong><sub>1</sub>
     * (and covariance matrix <strong>&sigma;</strong><sub>1</sub>) from position
     * <em>s</em><sub>1</sub> to position <em>s</em><sub>2</sub> is then given by
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>T</strong>(<em>s</em><sub>2</sub>,<em>s</em><sub>1</sub>) =
     * <strong>T</strong>(<em>s</em><sub>2</sub>,<em>s</em><sub>0</sub>) &#x2218;
     * <strong>T</strong>(<em>s</em><sub>1</sub>,<em>s</em><sub>0</sub>)<sup>-1</sup> ,
     * <br>
     * <br>
     * where <strong>T</strong>(<em>s</em><sub>2</sub>,<em>s</em><sub>0</sub>) is the transfer
     * map between the beamline entrance <em>s</em><sub>0</sub> and the position
     * <em>s</em><sub>2</sub>
     * of state <em>S</em><sub>2</sub>, and
     * <strong>T</strong>(<em>s</em><sub>1</sub>,<em>s</em><sub>0</sub>) is the transfer map
     * between the beamline entrance <em>s</em><sub>0</sub> and the position
     * <em>s</em><sub>1</sub>
     * of state <em>S</em><sub>1</sub>.
     *
     * @param state1 trajectory state <em>S</em><sub>1</sub> of starting location
     * <em>s</em><sub>1</sub>
     * @param state2 trajectory state <em>S</em><sub>2</sub> of final location
     * <em>s</em><sub>2</sub>
     *
     * @return transfer map <strong>T</strong>(<em>s</em><sub>2</sub>,<em>s</em><sub>1</sub>)
     * between locations <em>s</em><sub>1</sub> and <em>s</em><sub>2</sub>
     *
     * @author Christopher K. Allen
     * @since Nov 4, 2014
     */
    public static PhaseMap computeTransferMap(TransferMapState state1, TransferMapState state2) {
        PhaseMap mapPhi1 = state1.getTransferMap();
        PhaseMap mapPhi2 = state2.getTransferMap();

        PhaseMap mapPhi1inv = mapPhi1.inverse();
        PhaseMap mapPhi21 = mapPhi2.compose(mapPhi1inv);

        return mapPhi21;
    }


    /*
     * Local Attributes
     */
    /**
     * The trajectory around one turn of the ring
     */
    private final Trajectory<TransferMapState> trjSimFull;

    /**
     * The final transfer map probe state (at the end of the ring)
     */
    private final TransferMapState staFinal;

    /**
     * The transfer map (matrix) for the entire trajectory
     */
    private final PhaseMap mapPhiFull;

    /**
     * The matched beam Twiss parameters at the start of the ring
     */
    private final Twiss[] arrTwsMch;


    /*
     * Initialization
     */
    /**
     * <p>
     * Constructor for <code>CalculationsOnMachines</code>. Accepts the
     * <code>TransferMapTrajectory</code> object and extracts the final state
     * and full trajectory transfer map. Quantities that are required for
     * subsequent machine property calculations are also computed, such as phase
     * advance through the trajectory (modulo 2&pi;), entrance position "fixed
     * orbit" (that is, the orbit that is invariant for repeated applications of
     * the linear part of the transfer map (separating the projective part), and
     * the matched envelope when treating the full transfer map as the map for a
     * periodic cell.
     * </p>
     *
     * @param datSim the simulation data for the ring, a "transfer map
     * trajectory" object
     *
     * @throws IllegalArgumentException the trajectory does not contain
     * <code>TransferMapState</code> objects
     *
     * @author Christopher K. Allen
     * @since Nov 7, 2013
     */
    public CalculationsOnMachines(Trajectory<TransferMapState> datSim) throws IllegalArgumentException {
        TransferMapState pstFinal = datSim.finalState();

        this.trjSimFull = datSim;
        this.staFinal = pstFinal;
        this.mapPhiFull = this.staFinal.getTransferMap();
        this.arrTwsMch = super.calculateMatchedTwiss(this.mapPhiFull.getFirstOrder());
    }

    /*
     * Attribute Queries
     */
    /**
     * Returns the simulation trajectory from which all the machine properties
     * are computed.
     *
     * @return simulation trajectory from which this object was initialized
     *
     * @author Christopher K. Allen
     * @since Nov 7, 2013
     */
    public Trajectory<TransferMapState> getTrajectory() {
        return this.trjSimFull;
    }

    /**
     * Returns the transfer map of the full machine lattice represented by the
     * associated simulation trajectory.
     *
     * @return the transfer map of the last state of the associated trajectory
     *
     * @author Christopher K. Allen
     * @since Nov 7, 2013
     */
    public PhaseMap getFullTransferMap() {
        return this.mapPhiFull;
    }

    /**
     * <p>
     * Returns the collection of matched Courant-Snyder parameters for each
     * phase plane. The Courant-Snyder parameters describe the matched beam
     * envelopes when treating the trajectory of transfer maps as that for a
     * periodic lattice. There the beam envelopes would have the same
     * characteristics at the beginning and end of the lattice.
     * </p>
     * <p>
     * These are computed parameters calculated once at construction time.
     * </p>
     *
     * @return the Courant-Snyder parameters of a beam needed to match it into
     * the transfer maps of the associated trajectory when treated as a periodic
     * lattice
     *
     * @author Christopher K. Allen
     * @since Nov 7, 2013
     */
    public Twiss[] getMatchedTwiss() {
        return this.arrTwsMch;
    }

    /*
     * Local Operations
     */
    /**
     * <p>
     * Returns the state response matrix calculated from the front face of
     * elemFrom to the back face of elemTo. This is a convenience wrapper to the
     * real method in the trajectory class
     * </p>
     * <p>
     * This method was moved here from EnvelopeTrajectory/EnvelopeProbe where it
     * was eliminated since <code>Trajectory</code> was genericized.
     * </p>
     *
     * @param strIdElemFrom String identifying starting lattice element
     * @param strIdElemTo String identifying ending lattice element
     *
     * @return response matrix from elemFrom to elemTo
     *
     * @see EnvelopeTrajectory#computeTransferMatrix(String, String)
     *
     */
    public PhaseMatrix computeTransferMatrix(String strIdElemFrom, String strIdElemTo) {

        Trajectory<TransferMapState> trajectory = this.getTrajectory();

        // find starting index
        int[] arrIndFrom = trajectory.indicesForElement(strIdElemFrom);

        int[] arrIndTo = trajectory.indicesForElement(strIdElemTo);

        if (arrIndFrom.length == 0 || arrIndTo.length == 0) {
            throw new IllegalArgumentException("unknown element id");
        }

        int indFrom, indTo;
        indTo = arrIndTo[arrIndTo.length - 1]; // use last state before start element

        TransferMapState stateTo = trajectory.stateWithIndex(indTo);
        PhaseMatrix matTo = stateTo.getTransferMap().getFirstOrder();

        indFrom = arrIndFrom[0] - 1;
        if (indFrom < 0) {
            return matTo; // response from beginning of machine
        }
        TransferMapState stateFrom = trajectory.stateWithIndex(indFrom);
        PhaseMatrix matFrom = stateFrom.getTransferMap().getFirstOrder();

        return matTo.times(matFrom.inverse());
    }

    /*
     * ISimLocResults Interface
     */
    /**
     * <p>
     * We return the projective portion of the full-turn transfer map
     * &phi;<sub><em>n</em></sub> : <strong>P</strong><sup>6</sup> &rarr;
     * <strong>P</strong><sup>6</sup>
     * where <em>n</em> is the index of the given state <em>S<sub>n</sub></em>. This
     * is the image &Delta;<strong>z</strong> of the value
     * <strong>0</strong> &in; <strong>P</strong><sup>6</sup> &cong; <strong>R</strong><sup>6</sup> &times;
     * {1}. That is the value &Delta;<strong>z</strong> =
     * &phi;<sub><em>n</em></sub>(<strong>0</strong>).
     * </p>
     * <p>
     * Recall that the transfer map &phi; is a <code>PhaseMap</code> object
     * containing a first-order component which is a linear operator on
     * projective space <strong>P</strong><sup>6</sup>. As such, this
     * <code>PhaseMatrix</code> object <strong>&Phi;</strong> is embedded in
     * <strong>R</strong><sup>7&times;7</sup>. The 7<sup><em>th</em></sup> column
     * &Delta;<strong>z</strong> of <strong>&Phi;</strong> is the column of translation operations
     * on a phase vector
     * <strong>z</strong> &in; <strong>P</strong><sup>6</sup> &sub; <strong>R</strong><sup>6</sup> &times; {1}
     * since
     * <strong>z</strong> &in; <strong>P</strong><sup>6</sup> is represented
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>z</strong> = (<em>x, x', y, y', z, z',
     * </em>1)<sup><em>T</em></sup> .
     * <br>
     * <br>
     * Thus, the action of <strong>&Phi;</strong> can be (loosely) decomposed as
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>&Phi; &sdot; z</strong> = <strong>Mz</strong> + &Delta;<strong>z</strong> ,
     * <br>
     * <br>
     * where <strong>M</strong> &in; <em>Sp</em>(6), the symplectic group. This method
     * returns the component &Delta;<strong>z</strong> of the transfer map.
     * </p>
     *
     * @param state state containing location of returned translation vector
     *
     * @return the translation vector &Delta;<strong>z</strong> of the given transfer map
     *
     * @author Christopher K. Allen
     * @since Oct 22, 2013
     */
    @Override
    public PhaseVector computeCoordinatePosition(TransferMapState state) {

        PhaseMatrix matPhi = this.calculateFullLatticeMatrixAt(state);
        R6 vecDel = matPhi.projectColumn(IND.HOM);

        PhaseVector vecTranslate = PhaseVector.embed(vecDel);

        return vecTranslate;
    }

    /**
     * <p>
     * Get the fixed point at this state location about which betatron
     * oscillations occur.
     * </p>
     * <p>
     * Calculate the fixed point solution vector representing the closed orbit
     * at the location of this element. We first attempt to find the fixed point
     * for the full six phase space coordinates. Let <strong>&Phi;</strong> denote the
     * one-turn map for a ring. The fixed point
     * <strong>z</strong> &in; <strong>R</strong><sup>6</sup>&times;{1} in homogeneous phase space
     * coordinates is that which is invariant under <strong>&Phi;</strong>, that is,
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>&Phi;z</strong> = <strong>z</strong> .
     * <br>
     * <br>
     * This method returns that vector <strong>z</strong>.
     * </p>
     * <p>
     * Recall that the <em>homogeneous</em> transfer matrix <strong>&Phi;</strong> for the
     * ring has final row that represents the translation <strong>&Delta;</strong> of the
     * particle for the circuit around the ring. The 6&times;6 sub-matrix of
     * <strong>&Phi;</strong> represents the (linear) action of the bending magnetics and
     * quadrupoles and corresponds to the matrix <strong>T</strong> &in;
     * <strong>R</strong><sup>6&times;6</sup> (here <strong>T</strong> is linear). Thus, we can
     * write the linear operator <strong>&Phi;</strong>
     * as the augmented system
     * <br>
     * <br>
     * <pre>
     * &nbsp; &nbsp; <strong>&Phi;</strong> = |<strong>T</strong> <strong>&Delta;</strong> |,   <strong>z</strong> &equiv; |<strong>p</strong>| ,
     *         |<strong>0</strong> 1 |        |1|
     * </pre> where <strong>p</strong> is the projection of <strong>z</strong> onto the ambient
     * phase space
     * <strong>R</strong><sup>6</sup> (without homogeneous the homogeneous coordinate).
     * </p>
     * <p>
     * Putting this together we get
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>&Phi;z</strong> = <strong>Tp</strong> + <strong>&Delta;</strong> = <strong>p</strong> ,
     * <br>
     * <br>
     * to which the solution is
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>p</strong> = -(<strong>T</strong> -
     * <strong>I</strong>)<sup>-1</sup><strong>&Delta;</strong>
     * <br>
     * <br>
     * assuming it exists. The question of solution existence falls upon the
     * resolvent <strong>R</strong> &equiv; (<strong>T</strong> - <strong>I</strong>)<sup>-1</sup> of
     * <strong>T</strong>. By inspection we can see that <strong>p</strong> is defined so long as
     * the eigenvalues of <strong>T</strong> are located away from 1. In this case the
     * returned value is the augmented vector (<strong>p</strong> 1)<sup><em>T</em></sup>
     * &in; <strong>R</strong><sup>6</sup> &times; {1}.
     * </p>
     * <p>
     * When the set of eigenvectors does contain 1, we attempt to find the
     * solution for the transverse phase space. That is, we take vector <strong>p</strong>
     * &in; <strong>R</strong><sup>4</sup>
     * and <strong>T</strong> &in; <strong>R</strong><sup>4&times;4</sup> where
     * <strong>T</strong> = proj<sub>4&times;4</sub> <strong>&Phi;</strong>. The returned value is
     * then
     * <strong>z</strong> = (<strong>p</strong> 0 0 1)<sup><em>T</em></sup>.
     *
     * @param state state containing transfer map and location used in these
     * calculations
     *
     * @return fixed point solution (<em>x,x',y,y',z,z'</em>,1) for the given
     * phase matrix
     *
     * @author Christopher K. Allen
     * @since Oct 25, 2013
     */
    @Override
    public PhaseVector computeFixedOrbit(TransferMapState state) {
        PhaseMatrix matFullTrn = this.calculateFullLatticeMatrixAt(state);
        PhaseVector vecFixedPt = super.calculateFixedPoint(matFullTrn);

        return vecFixedPt;
    }

    /**
     * Computes the chromatic aberration for one pass around the ring starting
     * at the given state location, or from the entrance to state position for a
     * linear machine. The returned vector is the displacement from the closed
     * orbit caused by a unit momentum offset (&delta;<em>p</em> = 1). See the
     * documentation in
     * {@link ISimLocResults#computeChromAberration(ProbeState)} for a more
     * detailed exposition.
     *
     * @see
     * xal.tools.beam.calc.ISimLocResults#computeChromAberration(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since Nov 15, 2013
     */
    @Override
    public PhaseVector computeChromAberration(TransferMapState state) {
        double dblGamma = state.getGamma();
//        PhaseMap        mapPhi   = state.getTransferMap();
//        PhaseMap        mapPhi   = state.getStateTransferMap();
//      PhaseMatrix     matPhi   = mapPhi.getFirstOrder();
        PhaseMatrix matPhi = this.calculateFullLatticeMatrixAt(state);

        R6 vecDel = super.calculateAberration(matPhi, dblGamma);

        return PhaseVector.embed(vecDel);
    }

    /*
     * ISimEnvResults Interface
     */
    /**
     * <p>
     * Computes the closed orbit Twiss parameters at this state location
     * representing the matched beam envelope when treating the trajectory as a
     * lattice of periodic cells.
     * </p>
     * <p>
     * Returns the array of twiss objects for this state for all three planes.
     * </p>
     * <p>
     * Calculates the matched Courant-Snyder parameters for the given period
     * cell transfer matrix and phase advances. When the given transfer matrix
     * is the full-turn matrix for a ring the computed Twiss parameters are the
     * matched envelopes for the ring at that point.
     * </p>
     * <p>
     * Let <strong>&Phi;</strong> denote the transfer matrix from the machine beginning to
     * the given state location (it is contained in the given state). It is
     * assumed to be the transfer matrix through at least one cell in a periodic
     * lattice. Internally, the array of phase advances
     * {&sigma;<sub><em>x</em></sub>, &sigma;<sub><em>y</em></sub>,
     * &sigma;<sub><em>x</em></sub>} are assumed to be the particle phase advances
     * through the cell for the matched solution. These are computed with the
     * method <code>{@link #calculatePhaseAdvPerCell(PhaseMatrix)}</code>.
     * </p>
     * <p>
     * The returned Courant-Snyder parameters (&alpha;, &beta;, &epsilon;) are
     * invariant under the action of the given phase matrix, that is, they are
     * matched. All that is require are &alpha; and &beta; since &epsilon;
     * specifies the size of the beam envelope. Consequently the returned
     * &epsilon; is <code>NaN</code>.
     * </p>
     * The following are the calculations used for the Courant-Snyder parameters
     * of a single phase plane:
     * <br>
     * <br>
     * &nbsp; &nbsp; &alpha; &equiv; -<em>ww'</em> = (&phi;<sub>11</sub> -
     * &phi;<sub>22</sub>)/(2 sin &sigma;) ,
     * <br>
     * <br>
     * &nbsp; &nbsp; &beta; &equiv; <em>w</em><sup>2</sup> =
     * &phi;<sub>12</sub>/sin &sigma;
     * <br>
     * <br>
     * where &phi;<sub><em>ij</em></sub> are the elements of the 2&times;2
     * diagonal blocks of
     * <strong>&Phi;</strong> corresponding the the particular phase plane, the function
     * <em>w</em>
     * is taken from Reiser, and &sigma; is the phase advance through the cell
     * for the particular phase plance.
     * </p>
     *
     * @param state state containing transfer map and location used in these
     * calculations
     *
     * @return array (twiss-H, twiss-V, twiss-L)
     *
     * @author Christopher K. Allen
     * @since Aug 14, 2013
     */
    @Override
    public Twiss[] computeTwissParameters(TransferMapState state) {

        PhaseMatrix matFullTrn = this.calculateFullLatticeMatrixAt(state);
        Twiss[] arrTwsMtch = super.calculateMatchedTwiss(matFullTrn);

        return arrTwsMtch;
    }

    /**
     * <p>
     * This is the phase advance for the given state location.
     * </p>
     * <p>
     * Compute and return the particle phase advance from the trajectory
     * beginning to the given state location.
     * </p>
     * <p>
     * Internally the method calculates the phase advances using the initial and
     * final Courant-Snyder &alpha; and &beta; values for the matched beam at
     * the trajectory beginning and the location of the given state,
     * respectively. These Courant-Snyder parameters are computed as the matched
     * beam envelope at the trajectory beginning and the given state location.
     * </p>
     * <p>
     * Let <strong>&Phi;</strong> represent the transfer matrix from the initial ring
     * location to the given state location. The computed quantity is the
     * general phase advance of the particle through the transfer matrix
     * <strong>&Phi;</strong>, and no special requirements are placed upon <strong>&Phi;</strong>
     * (e.g., periodicity). One phase advance is provided for each phase plane,
     * i.e., (&sigma;<sub><em>x</em></sub>, &sigma;<sub><em>z</em></sub>,
     * &sigma;<sub><em>z</em></sub>).
     * </p>
     * <p>
     * The definition of phase advance &sigma; is given by
     * <br>
     * <br>
     * &nbsp; &nbsp; &sigma;(<em>s</em>) &equiv; &int;<sup><em>s</em></sup>
     * [1/&beta;(<em>t</em>)]<em>dt</em> ,
     * <br>
     * <br>
     * where &beta;(<em>s</em>) is the Courant-Snyder, envelope function, and the
     * integral is taken along the interval between the initial and final
     * Courant-Snyder parameters.
     * </p>
     * <p>
     * The basic relation used to compute &sigma; is the following:
     * <br>
     * <br>
     * &nbsp; &nbsp; &sigma; = sin<sup>-1</sup>
     * &phi;<sub>12</sub>/(&beta;<sub>1</sub>&beta;<sub>2</sub>)<sup>&frac12;</sup>
     * ,
     * <br>
     * <br>
     * where &phi;<sub>12</sub> is the element of <strong>&Phi;</strong> in the upper
     * right corner of each 2&times;2 diagonal block, &beta;<sub>1</sub> is the
     * initial beta function value (provided) and &beta;<sub>2</sub> is the
     * final beta function value (provided).
     * </p>
     *
     * @param state state containing transfer map and location used in these
     * calculations
     *
     * @see calculatePhaseAdvance(PhaseMatrix, Twiss[], Twiss[])
     *
     * @author Christopher K. Allen
     * @since Aug 14, 2013
     */
    @Override
    public R3 computeBetatronPhase(TransferMapState state) {
        PhaseMatrix matFullTrn = this.calculateFullLatticeMatrixAt(state);
        Twiss[] arrTwsLoc = super.calculateMatchedTwiss(matFullTrn);

        PhaseMatrix matPhiLoc = state.getTransferMap().getFirstOrder();
        R3 vecPhsAdv = super.calculatePhaseAdvance(matPhiLoc, this.arrTwsMch, arrTwsLoc);

        return vecPhsAdv;
    }

    /**
     * <p>
     * Calculates the fixed point (closed orbit) in transverse phase space at
     * the given state location in the presence of dispersion.
     * </p>
     * <p>
     * Let the full-turn map a the state location be denoted <strong>&Phi;</strong>. The
     * transverse plane dispersion vector <strong>&Delta;</strong> is defined
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>&Delta;</strong><sub><em>t</em></sub> &equiv;
     * -(1/&gamma;<sup>2</sup>)[d<em>x</em>/d<em>z'</em>, d<em>x'</em>/d<em>z'</em>,
     * d<em>y</em>/d<em>z'</em>, d<em>y'</em>/d<em>z'</em>]<sup><em>T</em></sup> .
     * <br>
     * <br>
     * It can be identified as the first 4 entries of the 6<sup><em>th</em></sup>
     * column in the transfer matrix <strong>&Phi;</strong>. The above vector quantifies
     * the change in the transverse particle phase coordinate position versus
     * the change in particle momentum. The factor -(1/&gamma;<sup>2</sup>) is
     * needed to convert from longitudinal divergence angle <em>z'</em> used by
     * XAL to momentum &delta;<em>p</em> &equiv; &Delta;<em>p</em>/<em>p</em> used in
     * the dispersion definition. Specifically,
     * <br>
     * <br>
     * &nbsp; &nbsp; &delta;<em>p</em> &equiv; &Delta;<em>p</em>/<em>p</em> =
     * &gamma;<sup>2</sup><em>z</em>'
     * <br>
     * <br>
     * As such, the above vector can be better described
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>&Delta;</strong><sub><em>t</em></sub> &equiv;
     * [&Delta;<em>x</em>/&delta;<em>p</em>, &Delta;<em>x'</em>/&delta;<em>p</em>,
     * &Delta;<em>y</em>/&delta;<em>p</em>,
     * &Delta;<em>y'</em>/&delta;<em>p</em>]<sup><em>T</em></sup>
     * <br>
     * <br>
     * explicitly describing the change in transverse phase coordinate for
     * fractional change in momentum &delta;<em>p</em>.
     * </p>
     * <p>
     * Since we are only concerned with transverse phase space coordinates, we
     * restrict ourselves to the 4&times;4 upper diagonal block of <strong>&Phi;</strong>,
     * which we denote take <strong>T</strong>. That is, <strong>T</strong> = &pi; &sdot;
     * <strong>&Phi;</strong>
     * where &pi; : <strong>R</strong><sup>6&times;6</sup> &rarr;
     * <strong>R</strong><sup>4&times;4</sup> is the projection operator.
     * </p>
     * <p>
     * This method finds that point <strong>z</strong><sub><em>t</em></sub> &equiv;
     * (<em>x<sub>t</sub></em>, <em>x'<sub>t</sub></em>, <em>y<sub>t</sub></em>,
     * <em>y'<sub>t</sub></em>) in transvse phase space that is invariant under
     * the action of the ring for a given momentum spread &delta;<em>p</em>. That
     * is, the particle ends up in the same location each revolution. With a
     * finite momentum spread of &delta;<em>p</em> &gt; 0 we require this require
     * that
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>Tz</strong><sub><em>t</em></sub> +
     * &delta;<em>p</em><strong>&Delta;</strong><sub><em>t</em></sub> =
     * <strong>z</strong><sub><em>t</em></sub> ,
     * <br>
     * <br>
     * which can be written
     * <br>
     * <br>
     * &nbsp; <strong>z</strong><sub><em>t</em></sub> = &delta;<em>p</em>(<strong>T</strong> -
     * <strong>I</strong>)<sup>-1</sup><strong>&Delta;</strong><sub><em>t</em></sub> ,
     * <br>
     * <br>
     * where <strong>I</strong> is the identity matrix. Dividing both sides by
     * &delta;<em>p</em> yields the final result
     * <br>
     * <br>
     * &nbsp; <strong>z</strong><sub>0</sub> &equiv;
     * <strong>z</strong><sub><em>t</em></sub>/&delta;<em>p</em> = (<strong>T</strong> -
     * <strong>I</strong>)<sup>-1</sup><strong>&Delta;</strong><sub><em>t</em></sub> ,
     * <br>
     * <br>
     * which is the returned value of this method. It is normalized by
     * &delta;<em>p</em> so that we can compute the closed orbit for any given
     * momentum spread.
     * </p>
     *
     * @param state we are calculating the dispersion at this state location
     *
     * @return The closed orbit fixed point <strong>z</strong><sub>0</sub> for finite
     * dispersion, normalized by momentum spread. Returned as an array
     * [<em>x</em><sub>0</sub>,<em>x'</em><sub>0</sub>,<em>y</em><sub>0</sub>,<em>y'</em><sub>0</sub>]/&delta;<em>p</em>
     *
     * @see
     * xal.tools.beam.calc.ISimEnvResults#computeChromDispersion(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since Nov 8, 2013
     */
    @Override
    public PhaseVector computeChromDispersion(TransferMapState state) {
        PhaseMatrix matFullTn = this.calculateFullLatticeMatrixAt(state);
        double dblGamma = state.getGamma();

//        double[]    arrDisp   = super.calculateDispersion(matFullTn, dblGamma);
//        R4          vecDispR4 = new R4(arrDisp);
        R4 vecDispR4 = super.calculateDispersion(matFullTn, dblGamma);
        PhaseVector vecDisp = PhaseVector.embed(vecDispR4);

        return vecDisp;
    }


    /*
     * Support Methods
     */
    /**
     * <p>
     * Calculates and returns the full lattice matrix for the machine at the
     * given state location. Let <em>S<sub>n</sub></em> be the given state object
     * at location <em>s<sub>n</sub></em>, and let <strong>T</strong><sub><em>n</em></sub> be
     * the transfer matrix between locations <em>s</em><sub>0</sub> and
     * <em>s<sub>n</sub></em> , where <em>s</em><sub>0</sub> is the location of the
     * full transfer matrix
     * <strong>&Phi;</strong><sub>0</sub> for this machine (end to end). Then the full
     * turn matrix
     * <strong>&Phi;</strong><sub><em>n</em></sub> for the machine at location
     * <em>s<sub>n</sub></em>
     * is given by
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>&Phi;</strong><sub><em>n</em></sub> =
     * <strong>T</strong><sub><em>n</em></sub> &sdot; <strong>&Phi;</strong><sub>0</sub>
     * &sdot; <strong>T</strong><sub><em>n</em></sub><sup>-1</sup> .
     * <br>
     * <br>
     * That is, we conjugate the full transfer map for this machine by the
     * transfer map for the given state.
     * </p>
     *
     * @param state state object <em>S<sub>n</sub></em> for location
     * <em>s<sub>n</sub></em>
     * containing transfer matrix <strong>T</strong><sub><em>n</em></sub>
     *
     * @return the full-turn map <strong>&Phi;</strong><sub><em>n</em></sub> at the location
     * <em>s<sub>n</sub></em> of the given state
     *
     * @author Christopher K. Allen
     * @since Oct 28, 2013
     */
    protected PhaseMatrix calculateFullLatticeMatrixAt(TransferMapState state) {
        PhaseMap mapPhiState = state.getTransferMap();
        PhaseMatrix matPhiState = mapPhiState.getFirstOrder();
        PhaseMatrix matPhiStInv = matPhiState.inverse();

        PhaseMatrix matPhiFull = this.mapPhiFull.getFirstOrder();
        PhaseMatrix matFullTnLoc;

        matFullTnLoc = matPhiFull.times(matPhiStInv);
        matFullTnLoc = matPhiState.times(matFullTnLoc);

        return matFullTnLoc;
    }

}
