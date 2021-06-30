/**
 * ISimulationResults.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 15, 2013
 */
package xal.tools.beam.calc;

import xal.model.probe.traj.ProbeState;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.math.r3.R3;

/**
 * <p>
 * Encapsulates base for any interfaces that processes simulation data.
 * Interfaces expose particle, machine, and beam parameters derived from the
 * state data produced by numerical simulation. The context of these quantities
 * should depend upon the type <code>S</code> of the probe states produced by
 * the simulation.
 * </p>
 * <p>
 * Probe types can expose multiple interfaces. In particular, for <code>S</code>
 * = <code>xal.model.probe.traj.TransferMapState</code> the quantities of this
 * interface can be regarded as machine parameters, since the beam does not
 * influence transfer map calculations in the
 * <code>xal.model.probe.TransferMapProbe</code>. However, a there are particle
 * processors and envelope processors for them. The results of the calculations
 * must be taken in the context of the data upon which they calculate.
 * <strong>See the associated Javadoc.</strong>
 * </p>
 *
 * @author Christopher K. Allen
 * @since Nov 15, 2013
 */
public interface ISimulationResults {

    /**
     * This interface defines methods for computation results that are in the
     * form of points, or locations, in phase space.
     *
     * @param <S> type of the probe state containing local simulation data
     *
     * @author Christopher K. Allen
     * @since Nov 15, 2013
     */
    public interface ISimLocResults<S> extends ISimulationResults {

        /**
         * <p>
         * Returns homogeneous phase space coordinates of something involving
         * the simulation data. The interpretation is highly dependent upon the
         * context of the data. That is, this quantity is open for
         * interpretation; we can be referring to the position of the design
         * trajectory, an offset, or the location of the beam centroid, whatever
         * "beam" means in the context. The units are meters and radians.
         * </p>
         * <h3>NOTE:</h3>
         * <p>
         * This quantity is obtuse and not well defined - PhaseCoordinates of
         * what?
         * <br>
         * &nbsp; &nbsp; &middot; Is this a centroid location?
         * <br>
         * &nbsp; &nbsp; &middot; From which starting orbit?
         * <br>
         * &nbsp; &nbsp; &middot; Not all simulation results have quantities
         * naturally associated with phase coordinates
         * </p>
         *
         * @param state simulation state where parameters are computed
         *
         * @return vector (<em>x,x',y,y',z,z'</em>,1) of phase space coordinates
         *
         */
        public PhaseVector computeCoordinatePosition(S state);

        /**
         * <p>
         * Computes the fixed orbit about which betatron oscillations occur.
         * This value is well-defined for rings but could be ambiguous for beam
         * envelope simulation, especially with regard to method
         * <code>{@link #computeCoordinatePosition(ProbeState)}</code>. The
         * returned value for some given simulation types are provided below.
         * </p>
         * <p>
         * In general the idea is that the returned coordinate
         * <strong>z</strong> in phase space <strong>P</strong><sup>6</sup>
         * &cong;
         * <strong>R</strong><sup>6</sup> &times; {1} is invariant under some
         * map
         * <strong>&phi;</strong> : <strong>P</strong><sup>6</sup> &rarr;
         * <strong>P</strong><sup>6</sup>
         * representing the dynamics of the system.
         * </p>
         * <h3>IMPORTANT NOTE</h3>
         * <p>
         * This method is provided to maintain compatibility with the previous
         * use of <code>computeFixedOrbit()</code> presented by the trajectory
         * classes for particles, beam envelopes, etc. (This method has been
         * deprecated and discontinued.) The methods responded differently
         * depending upon whether the structure producing the simulation data
         * was from a ring or a linear transport/accelerator structure. This
         * behavior has now changed, the method produces different results for
         * <em>different simulation types</em> (e.g., particle, transfer map,
         * envelope, etc.) rather than different simulation structures.
         * </p>
         * <p>
         * When the underlying data is produced by a transfer map this method
         * <em>should return the fixed orbit position</em> at the given state.
         * When the underlying data is produced by a particle then the returned
         * value <em>should be the position of the particle</em>
         * at the given state location (for its given initial position). When
         * the underlying data is from a beam envelope then this method
         * <em>should return the centroid location</em> of the beam bunch (for
         * its given initial condition).
         * </p>
         * <p>
         * You must specify the simulation processing engine for each data type
         * to use a <code>SimResultsAdaptor</code>. To reproduce the behavior of
         * the past <code>Trajectory#computeFixedOrbit(ProbeState)</code>
         * specify a <code>{@link CalculationsOnMachines}</code> simulation data
         * processor for ring lattices and a
         * <code>{@link CalculationsOnBeams}</code> simulation processor for
         * linear lattices. This configuration is accommodated in the class
         * <code>{@link SimpleSimResultsAdaptor}</code> exposing this interface.
         * </p>
         *
         * @param state simulation state where parameters are computed
         *
         * @return the reference orbit vector (<em>x,x',y,y',z,z'</em>,1) (see
         * comments)
         */
        public PhaseVector computeFixedOrbit(S state);

        /**
         * Compute and return the aberration at the given state location due to
         * energy spread. The returned value <strong>&Delta;</strong> is the
         * vector
         * <br>
         * <br>
         * &nbsp; &nbsp; <strong>&Delta;</strong> &equiv; (&Delta;<em>x</em>,
         * &Delta;<em>x'</em>, &Delta;<em>y</em>, &Delta;<em>y'</em>, 0, 0, 1)
         * <br>
         * <br>
         * where, when multiplied by momentum spread &delta; &equiv;
         * &Delta;<em>p</em>/<em>p</em> yields the change in fixed orbit
         * position. That is <strong>z</strong> = <strong>z</strong><sub>0</sub>
         * + &delta;<strong>&Delta;</strong>.
         *
         * @param state simulation state where parameters are computed
         *
         * @return the vector <strong>&Delta;</strong> of dispersion
         * coefficients
         *
         * @author Christopher K. Allen
         * @since Nov 8, 2013
         */
        public PhaseVector computeChromAberration(S state);
    }

    /**
     * <p>
     * Processes simulation data concerned with the beam properties.
     * </p>
     * <p>
     * For example, when <code>S</code> =
     * <code>xal.model.probe.traj.EnvelopeProbeState</code> then the interface
     * quantities are essentially beam parameters since the beam dynamics figure
     * into the states of <code>xal.model.probe.EnvelopeProbe</code>.
     * </p>
     *
     * @param <S> type of the probe state containing local simulation data
     *
     * @author Christopher K. Allen
     * @author Thomas Pelaia
     * @since 2/9/05
     * @version Nov 7, 2013
     */
    public interface ISimEnvResults<S> extends ISimulationResults {


        /*
         * Interface Methods
         */
        /**
         * Returns the array of twiss objects for this state for all three
         * planes at the location of the given simulation state. These could be
         * the matched beam Twiss parameters for a periodic structure or the
         * dynamics Twiss parameters of a mismatched or otherwise evolving beam.
         *
         * @param state simulation state where Twiss parameters are computed
         *
         * @return array (twiss-H, twiss-V, twiss-L)
         */
        public Twiss[] computeTwissParameters(S state);

        /**
         * Get the betatron phase values at the given state location for all
         * three phase planes.
         *
         * @param state simulation state where parameters are computed
         *
         * @return vector (&psi;<sub><em>x</em></sub>,
         * &psi;<sub><em>y</em></sub>, &psi;<sub><em>x</em></sub>) of phases in
         * radians
         */
        public R3 computeBetatronPhase(S state);

        /**
         * <p>
         * Calculates the fixed point (closed orbit) in transverse phase space
         * at the given state <em>S<sub>n</sub></em> location
         * <em>s<sub>n</sub></em>
         * in the presence of dispersion.
         * </p>
         * <p>
         * Let the full-turn map a the state location be denoted
         * <strong>&Phi;</strong><sub><em>n</em></sub> (or the transfer matrix
         * from entrance to location <em>s<sub>n</sub></em> for a linac). The
         * transverse plane dispersion vector <strong>&Delta;</strong> is
         * defined
         * <br>
         * <br>
         * &nbsp; &nbsp; <strong>&Delta;</strong><sub><em>t</em></sub> &equiv;
         * -(1/&gamma;<sup>2</sup>)[d<em>x</em>/d<em>z'</em>,
         * d<em>x'</em>/d<em>z'</em>, d<em>y</em>/d<em>z'</em>,
         * d<em>y'</em>/d<em>z'</em>]<sup><em>T</em></sup> .
         * <br>
         * <br>
         * It can be identified as the first 4 entries of the
         * 6<sup><em>th</em></sup>
         * column in the transfer matrix
         * <strong>&Phi;</strong></strong><sub><em>n</em></sub>. The above
         * vector quantifies the change in the transverse particle phase
         * coordinate position versus the change in particle momentum. The
         * factor -(1/&gamma;<sup>2</sup>) is needed to convert from
         * longitudinal divergence angle <em>z'</em> used by XAL to momentum
         * &delta;<em>p</em> &equiv; &Delta;<em>p</em>/<em>p</em> used in the
         * dispersion definition. Specifically,
         * <br>
         * <br>
         * &nbsp; &nbsp; &delta;<em>p</em> &equiv; &Delta;<em>p</em>/<em>p</em>
         * = &gamma;<sup>2</sup><em>z</em>'
         * <br>
         * <br>
         * As such, the above vector can be better described
         * <br>
         * <br>
         * &nbsp; &nbsp; <strong>&Delta;</strong><sub><em>t</em></sub> &equiv;
         * [&Delta;<em>x</em>/&delta;<em>p</em>,
         * &Delta;<em>x'</em>/&delta;<em>p</em>,
         * &Delta;<em>y</em>/&delta;<em>p</em>,
         * &Delta;<em>y'</em>/&delta;<em>p</em>]<sup><em>T</em></sup>
         * <br>
         * <br>
         * explicitly describing the change in transverse phase coordinate for
         * fractional change in momentum &delta;<em>p</em>.
         * </p>
         * <p>
         * Since we are only concerned with transverse phase space coordinates,
         * we restrict ourselves to the 4&times;4 upper diagonal block of
         * <strong>&Phi;</strong></strong><sub><em>n</em></sub>, which we denote
         * take
         * <strong>T</strong></strong><sub><em>n</em></sub>. That is,
         * <strong>T</strong></strong><sub><em>n</em></sub> = &pi; &sdot;
         * <strong>&Phi;</strong></strong><sub><em>n</em></sub>
         * where &pi; : <strong>R</strong><sup>6&times;6</sup> &rarr;
         * <strong>R</strong><sup>4&times;4</sup> is the projection operator.
         * </p>
         * <p>
         * This method finds that point <strong>z</strong><sub><em>t</em></sub>
         * &equiv; (<em>x<sub>t</sub></em>, <em>x'<sub>t</sub></em>,
         * <em>y<sub>t</sub></em>,
         * <em>y'<sub>t</sub></em>) in transvse phase space that is invariant
         * under the action of the ring for a given momentum spread
         * &delta;<em>p</em>. That is, the particle ends up in the same location
         * each revolution. With a finite momentum spread of &delta;<em>p</em>
         * &gt; 0 we require this require that
         * <br>
         * <br>
         * &nbsp; &nbsp;
         * <strong>T</strong><sub><em>n</em><strong></sub>z</strong><sub><em>t</em></sub>
         * + &delta;<em>p</em><strong>&Delta;</strong><sub><em>t</em></sub> =
         * <strong>z</strong><sub><em>t</em></sub> ,
         * <br>
         * <br>
         * which can be written
         * <br>
         * <br>
         * &nbsp; <strong>z</strong><sub><em>t</em></sub> =
         * &delta;<em>p</em>(<strong>T</strong></strong><sub><em>n</em></sub> -
         * <strong>I</strong>)<sup>-1</sup><strong>&Delta;</strong><sub><em>t</em></sub>
         * ,
         * <br>
         * <br>
         * where <strong>I</strong> is the identity matrix. Dividing both sides
         * by &delta;<em>p</em> yields the final result
         * <br>
         * <br>
         * &nbsp; <strong>z</strong><sub>0</sub> &equiv;
         * <strong>z</strong><sub><em>t</em></sub>/&delta;<em>p</em> =
         * (<strong>T</strong></strong><sub><em>n</em></sub> -
         * <strong>I</strong>)<sup>-1</sup><strong>&Delta;</strong><sub><em>t</em></sub>
         * ,
         * <br>
         * <br>
         * which is the returned value of this method. It is normalized by
         * &delta;<em>p</em> so that we can compute the closed orbit for any
         * given momentum spread.
         * </p>
         *
         * @param state we are calculating the dispersion at this state location
         *
         * @return The closed orbit fixed point <strong>z</strong><sub>0</sub>
         * for finite dispersion, normalized by momentum spread. Returned as an
         * array
         * [<em>x</em><sub>0</sub>,<em>x'</em><sub>0</sub>,<em>y</em><sub>0</sub>,<em>y'</em><sub>0</sub>]/&delta;<em>p</em>
         *
         * @author Christopher K. Allen
         * @since Nov 8, 2013
         */
        public PhaseVector computeChromDispersion(S state);

    }

}
