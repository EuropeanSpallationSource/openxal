/**
 * CalculationsOnBeams.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 22, 2013
 */
package xal.tools.beam.calc;

import xal.tools.beam.calc.ISimulationResults.ISimLocResults;
import xal.tools.beam.calc.ISimulationResults.ISimEnvResults;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.math.r3.R3;
import xal.tools.math.r4.R4;
import xal.tools.math.r6.R6;

/**
 * Class for performing calculations on data obtained from simulating linacs and
 * beam transport systems. The class performs the calculation exposed in the
 * <code>ISimEnvResults</code> interface. They are performed in the context of a
 * linear accelerator or transport system acting upon a beam bunch, the
 * simulation data being a trajectory of <code>EnvelopeProbeStates</code>.
 *
 *
 * @author Christopher K. Allen
 * @since Oct 22, 2013
 */
public class CalculationsOnBeams extends CalculationEngine implements ISimLocResults<EnvelopeProbeState>, ISimEnvResults<EnvelopeProbeState> {

    /*
     * Local Attributes
     */
    /**
     * The trajectory around one turn of the ring
     */
    private final Trajectory<EnvelopeProbeState> trjSimul;

    /**
     * The initial envelope probe state (at the start of the simulation)
     */
    private final EnvelopeProbeState staInit;

    /**
     * The final envelope probe state (at the end of the simulation )
     */
    private final EnvelopeProbeState staFinal;

    /**
     * The response matrix for the linac (between initial state and final state)
     */
    private final PhaseMatrix matResp;

    /**
     * The betatron phase advances at the at the end of the simulation
     */
    private final R3 vecPhsAdv;

    /**
     * The fixed orbit position at the beginning of the simulation
     */
    private final PhaseVector vecFxdPt;

    /**
     * The matched beam Twiss parameters at the start of the simulation
     */
    private final Twiss[] arrTwsMch;

    /*
     * Initialization
     */
    /**
     * Constructor for <cod>CalculationsOnBeams</code>. Creates object and
     * computes all the static simulation results.
     *
     * @param datSim results for an <code>EnvelopeProbe</code> simulation
     *
     * @author Christopher K. Allen
     * @since Oct 22, 2013
     */
    public CalculationsOnBeams(Trajectory<EnvelopeProbeState> datSim) {
        trjSimul = datSim;
        staInit = datSim.initialState();
        staFinal = datSim.finalState();
        matResp = this.staFinal.getResponseMatrix();

        vecPhsAdv = super.calculatePhaseAdvPerCell(matResp);
        vecFxdPt = super.calculateFixedPoint(matResp);
        arrTwsMch = super.calculateMatchedTwiss(matResp);
    }

    /*
     * Attributes and Properties
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
    public Trajectory<EnvelopeProbeState> getTrajectory() {
        return trjSimul;
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
    public PhaseMatrix getFullResponseMatrix() {
        return matResp;
    }

    /**
     * <p>
     * Returns the betatron phase advances from the simulation beginning to end
     * (which are computed at instantiation). The returned value is the
     * calculation <code>{@link #calculatePhaseAdvPerCell(PhaseMatrix)}</code>
     * of the super class, and thus assumes simulation trajectory is that for at
     * least one period of a periodic structure.
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot; The betatron phase advances are given in the range [0,2&pi];.
     * <br>
     * </p>
     *
     * @return vector particle betatron phase advances (in radians)
     *
     * @author Christopher K. Allen
     * @since Oct 30, 2013
     */
    public R3 periodBetatronPhaseAdvance() {
        return vecPhsAdv;
    }

    /**
     * <p>
     * Returns the phase space location of the fixed orbit at the simulation
     * start (which is computed at instantiation) assuming the simulation is
     * that for at least one period of a periodic accelerating or transport
     * section. The returned value <strong>z</strong> is the result of the
     * calculation <code>{@link #calculateFixedPoint(PhaseMatrix)}</code> given
     * the full turn matrix <strong>&Phi;</strong> at the simulation exit (see
     * {@link #getFullResponseMatrix()}). It is invariant under the action of
     * <strong>&Phi;</strong>, that is, <strong>&Phi;z</strong> =
     * <strong>z</strong>.
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot; The entrance of the ring is assumed to be the location of the
     * first and last states of the solution trajectory.
     * </p>
     *
     * @return
     *
     * @author Christopher K. Allen
     * @since Oct 30, 2013
     */
    public PhaseVector periodFixedOrbitPt() {
        return vecFxdPt;
    }

    /**
     * <p>
     * Returns the matched Courant-Snyder parameters at the entrance of the
     * simulation assuming the simulation is for at least one period of a
     * periodic structure. These are the "envelopes" taken from the "closed
     * envelope" solution under the assume the linac is a periodic transport.
     * </p>
     * <p>
     * Note that emittance &epsilon; is the parameter used to describe the
     * extend of the actual beam (rather than the normalized size &beta;), or
     * "acceptance". Thus it cannot be computed here and <code>NaN</code> is
     * returned instead.
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot; The entrance of the simulation is assumed to be the location of
     * the first and last states of the solution trajectory.
     * </p>
     *
     * @return array of Twiss parameter sets (&alpha;, &beta;, NaN)
     *
     * @author Christopher K. Allen
     * @since Oct 30, 2013
     */
    public Twiss[] periodMatchedTwiss() {
        return arrTwsMch;
    }

    /*
     * Local Operations
     */
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
     */
    public PhaseMatrix computeTransferMatrix(String elemFrom, String elemTo) {

        Trajectory<EnvelopeProbeState> trajectory = this.getTrajectory();

        // find starting index
        int[] arrIndFrom = trajectory.indicesForElement(elemFrom);

        int[] arrIndTo = trajectory.indicesForElement(elemTo);

        if (arrIndFrom.length == 0 || arrIndTo.length == 0) {
            throw new IllegalArgumentException("unknown element id");
        }

        int indFrom;
        int indTo;
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
     * ISimLocResults Interface
     */
    /**
     * Returns the centroid location of the beam envelope. This quantity is
     * taken from the <code>CovarianceMatrix</code> state object. Since the
     * state quantities are expressed in homogeneous coordinates the final row
     * and column of the covariance matrix are interpreted as the centroid
     * vector of the beam bunch.
     *
     * @see
     * xal.tools.beam.calc.ISimEnvResults#computeCoordinatePosition(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since Nov 7, 2013
     */
    @Override
    public PhaseVector computeCoordinatePosition(EnvelopeProbeState state) {
        CovarianceMatrix matSigLoc = state.getCovarianceMatrix();
        return matSigLoc.getMean();
    }

    /**
     * <h3>IMPORTANT NOTE</h3>
     * <p>
     * This method has been modified so it returns exactly the same value as
     * {@link #computeCoordinatePosition(ParticleProbeState)}. This modification
     * is maintain compatibility with the previous use of
     * <code>computeFixedOrbit()</code> presented by the trajectory classes for
     * particles, beam envelopes, etc. They responded differently depending upon
     * whether the structure producing the simulation data was from a ring or a
     * linear transport/accelerator structure.
     * <br>
     * <br>
     * Thus, <em>ignore all commenting below!</em>
     * </p>
     *
     * <p>
     * Consider first the point in phase space that is invariant under repeated
     * application of the response matrix <strong>&Phi;</strong> for the entire
     * beamline or ring. This is under the condition that we decompose
     * <strong>&Phi;</strong> into its homogeneous and non-homogeneous
     * components. A particle entering the linac at that location exits at the
     * same location.
     * </p>
     * <p>
     * To compute this linac fixed point, recall that the <em>homogeneous</em>
     * response matrix <strong>&Phi;</strong> for the beamline (or full-turn
     * matrix for a ring) has final row that represents the translation
     * <strong>&Delta;</strong> of the particle under the action of
     * <strong>&Phi;</strong>. The 6&times;6 sub-matrix of
     * <strong>&Phi;</strong> represents the (linear) action of the bending
     * magnetics and quadrupoles and corresponds to the matrix
     * <strong>T</strong> &in;
     * <strong>R</strong><sup>6&times;6</sup> (here <strong>T</strong> is
     * linear). Thus, we can write the linear operator <strong>&Phi;</strong>
     * as the augmented system
     * <br>
     * <br>
     * <pre>
     * &nbsp; &nbsp; <strong>&Phi;</strong> = |<strong>T</strong> <strong>&Delta;</strong> |,   <strong>z</strong> &equiv; |<strong>p</strong>| ,
     *         |<strong>0</strong> 1 |        |1|
     * </pre> where <strong>p</strong> is the projection of <strong>z</strong>
     * into the embedded phase space
     * <strong>R</strong><sup>6</sup> (without homogeneous coordinate).
     * coordinates).
     * </p>
     * <p>
     * Putting this together we get
     * <br>
     * <br>
     * &nbsp; &nbsp; <strong>&Phi;z</strong> = <strong>Tp</strong> +
     * <strong>&Delta;</strong> = <strong>p</strong> ,
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
     * resolvent <strong>R</strong> &equiv; (<strong>T</strong> -
     * <strong>I</strong>)<sup>-1</sup> of
     * <strong>T</strong>. By inspection we can see that <strong>p</strong> is
     * defined so long as the eigenvalues of <strong>T</strong> are located away
     * from 1. In this case the returned value is the augmented vector
     * (<strong>p</strong> 1)<sup><em>T</em></sup>
     * &in; <strong>R</strong><sup>6</sup> &times; {1}.
     * </p>
     * <p>
     * When the set of eigenvectors does contain 1, we attempt to find the
     * solution for the transverse phase space. That is, we take vector
     * <strong>p</strong>
     * &in; <strong>R</strong><sup>4</sup>
     * and <strong>T</strong> &in; <strong>R</strong><sup>4&times;4</sup> where
     * <strong>T</strong> = proj<sub>4&times;4</sub> <strong>&Phi;</strong>. The
     * solution value is then
     * <strong>z</strong> = (<strong>p</strong> 0 0 1)<sup><em>T</em></sup>.
     * </p>
     * <p>
     * Once we have the fixed point <strong>z</strong><sub>0</sub> for the linac
     * we compute the trajectory of the fixed point at the location of the given
     * probe state. To do so, we multiply
     * <strong>z</strong><sub>0</sub> by the response matrix
     * <strong>&Phi;</strong><sub><em>n</em></sub> for the given probe state.
     * That is, we propagate the fixed point of the linac from the linac
     * entrance to the location of the given phase state.
     * </p>
     *
     * @return The quantity
     * <strong>&Phi;</strong><sub><em>n</em></sub>&sdot;<strong>z</strong><sub>0</sub>,
     * the linac fixed point <strong>z</strong><sub>0</sub> propagated to the
     * state location
     * <em>s<sub>n</sub></em>
     *
     * @see
     * xal.tools.beam.calc.ISimEnvResults#computeFixedOrbit(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since Nov 7, 2013
     */
    @Override
    public PhaseVector computeFixedOrbit(EnvelopeProbeState state) {
        return computeCoordinatePosition(state);
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
    public PhaseVector computeChromAberration(EnvelopeProbeState state) {
        double dblGamma = state.getGamma();
        PhaseMatrix mat = state.getResponseMatrix();
        R6 vecDel = super.calculateAberration(mat, dblGamma);

        return PhaseVector.embed(vecDel);
    }

    /*
     * ISimEnvResults Interface
     */
    /**
     * Returns the Courant-Snyder parameters of the beam envelope at the
     * location of the given probe state. These values are computed from the
     * primary state object of an <code>EnvelopeProbe</code> the <em>covariance
     * matrix</em> <strong>&sigma;</strong>. Only the 2&times;2 diagonal blocks
     * of
     * <strong>&sigma;</strong> are used for Courant-Snyder parameter
     * calculations (for each phase plane), thus, any phase plane coupling is
     * lost.
     *
     * @see
     * xal.tools.beam.calc.ISimEnvResults#computeTwissParameters(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since Nov 7, 2013
     */
    @Override
    public Twiss[] computeTwissParameters(EnvelopeProbeState state) {
        CovarianceMatrix matSigma = state.getCovarianceMatrix();
        return matSigma.computeTwiss();
    }

    /**
     * <p>
     * Computes and returns the "betatron phase" of a beam particle within the
     * simulated envelope at the given state location. This quantity is the
     * phase advance between the beginning of the simulation and the given state
     * location. The calculation proceeds by computing the Courant-Snyder
     * parameters &alpha; and &beta; of the envelope at the entrance to the
     * linac and at the given state location using the covariance matrix
     * <strong>&sigma;</strong>(<em>s</em>) of the simulation. The given state
     * also contains the response matrix <strong>&Phi;</strong>(<em>s</em>)
     * between the entrance to the linac and the current state location
     * <em>s</em>. This matrix is used as the transfer matrix mapping particle
     * phase coordinates between the linac entrance and the current state
     * location.
     * </p>
     * <p>
     * The definition of phase advance &psi; is given by
     * <br>
     * <br>
     * &nbsp; &nbsp; &psi;(<em>s</em>) &equiv; &int;<sup><em>s</em></sup>
     * [1/&beta;(<em>t</em>)]<em>dt</em> ,
     * <br>
     * <br>
     * where &beta;(<em>s</em>) is the Courant-Snyder, envelope function, and
     * the integral is taken along the interval between the initial and final
     * Courant-Snyder parameters.
     * </p>
     * <p>
     * The basic relation used to compute &psi; is the following:
     * <br>
     * <br>
     * &nbsp; &nbsp; &psi; = sin<sup>-1</sup>
     * &phi;<sub>12</sub>/(&beta;<sub>1</sub>&beta;<sub>2</sub>)<sup>&frac12;</sup>
     * ,
     * <br>
     * <br>
     * where &phi;<sub>12</sub> is the element of <strong>&Phi;</strong> in the
     * upper right corner of each 2&times;2 diagonal block, &beta;<sub>1</sub>
     * is the initial beta function value (provided) and &beta;<sub>2</sub> is
     * the final beta function value (provided).
     * </p>
     *
     * @see
     * xal.tools.beam.calc.ISimEnvResults#computeBetatronPhase(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since Nov 7, 2013
     */
    @Override
    public R3 computeBetatronPhase(EnvelopeProbeState state) {
        CovarianceMatrix matSigInit = this.staInit.getCovarianceMatrix();
        Twiss[] arrTwsInit = matSigInit.computeTwiss();

        CovarianceMatrix matSigLoc = state.getCovarianceMatrix();
        Twiss[] arrTwsLoc = matSigLoc.computeTwiss();

        PhaseMatrix matPhiLoc = state.getResponseMatrix();

        return super.calculatePhaseAdvance(matPhiLoc, arrTwsInit, arrTwsLoc);
    }

    /**
     * <p>
     * Convenience function for returning the chromatic dispersion coefficients
     * as defined by D.C. Carey in "The Optics of Charged Particle Beams".
     * </p>
     * Computes the chromatic aberration for one pass around the ring starting
     * at the given state location, or from the entrance to state position for a
     * linear machine. The returned vector is the displacement from the closed
     * orbit caused by a unit momentum offset (&delta;<em>p</em> = 1). See the
     * documentation in
     * {@link ISimLocResults#computeChromAberration(ProbeState)} for a more
     * detailed exposition.
     * <h3>NOTE:</h3>
     * <p>
     * - Reference text D.C. Carey, "The Optics of Charged Particle Beams"
     * </p>
     *
     * @return vector of chromatic dispersion coefficients in
     * <strong>meters/radian</strong>
     *
     * @see
     * xal.tools.beam.calc.ISimEnvResults#computeChromDispersion(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since Nov 8, 2013
     */
    @Override
    public PhaseVector computeChromDispersion(EnvelopeProbeState state) {
        double dblGamma = state.getGamma();
        PhaseMatrix mat = state.getResponseMatrix();

        R4 vecDisp = super.calculateDispersion(mat, dblGamma);

        return PhaseVector.embed(vecDisp);
    }
}
