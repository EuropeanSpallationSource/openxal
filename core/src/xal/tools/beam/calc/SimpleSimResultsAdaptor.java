/**
 * SimpleSimResultsAdaptor.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 12, 2013
 */
package xal.tools.beam.calc;

import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;

/**
 * <p>
 * This class reduces the general operation of the base class
 * <code>SimResultsAdaptor</code> to the specific use of the calculation
 * engine <code>{@link CalculationsOnRings}</code> for simulation data
 * of type <code>TransferMapTrajectory</code>, and use of calculation
 * engine <code>{@link CalculationsOnBeams}</code> for simulation data
 * of type <code>EnvelopeTrajectory</code>.
 * Thus, probes states of either type <code>TransferMapState</code>
 * or <code>EnvelopeProbeState</code> may be passed to the interface,
 * according to which trajectory type was passed to the constructor.
 * </p>
 * <p>
 * Again, note that this adaptor will not recognize any simulation data other
 * that the type <code>TransferMapTrajectory</code> and
 * <code>EnvelopeTrajectory</code>.
 * </p>
 * <h3>NOTE:</h3>
 * <p>
 * - Calculations for the <code>ParticleProbeTrajectory</code> have been added.  The
 * calculation engine is <code>CalculationsOnParticles</code>.  Note that only the
 * methods of interface <code>ISimLocResults</code> will be recognized.  Methods of
 * interface <code>ISimEnvResults</code> will results in an exception.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Nov 7, 2013
 *
 * @see SimResultsAdaptor
 * @see CalculationsOnRings
 * @see CalculationsOnBeams
 */
public class SimpleSimResultsAdaptor extends SimResultsAdaptor {


    /*
     * Local Attributes
     */


    /*
     * Initialization
     */

    /**
     * Constructor for <code>SimpleSimResultsAdaptor</code>.  We create an internal
     * machine calculation engine based upon the type of the given simulation
     * trajectory.
     *
     * @param trajectory  simulation data that is going to be processed
     *
     * @throws IllegalArgumentException the simulation data is of an unknown type
     *
     * @author Christopher K. Allen
     * @since  Nov 7, 2013
     */
	public SimpleSimResultsAdaptor(Trajectory<?> trajectory) throws IllegalArgumentException {
        super();


        Class<?> clsTrajState = trajectory.getStateClass();

        if ( clsTrajState.equals(TransferMapState.class) ) {
            @SuppressWarnings("unchecked")
            CalculationsOnRings calRings  = new CalculationsOnRings((Trajectory<TransferMapState>)trajectory);
            super.registerCalcEngine(TransferMapState.class, calRings);;

        } else if (clsTrajState.equals(EnvelopeProbeState.class)) {
            @SuppressWarnings("unchecked")
            CalculationsOnBeams calBeams = new CalculationsOnBeams((Trajectory<EnvelopeProbeState>)trajectory);
            super.registerCalcEngine(EnvelopeProbeState.class, calBeams);

        } else if (clsTrajState.equals(ParticleProbeState.class)) {
            @SuppressWarnings("unchecked")
            CalculationsOnParticles calPart = new CalculationsOnParticles((Trajectory<ParticleProbeState>)trajectory);
            super.registerCalcEngine(ParticleProbeState.class, calPart);

        } else {

            throw new IllegalArgumentException("Unknown simulation data type " + trajectory.getClass().getName());
        }

    }

}
