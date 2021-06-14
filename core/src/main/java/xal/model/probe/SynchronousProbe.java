/*
 * Created on May 28, 2004
 *
 */
package xal.model.probe;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.model.probe.traj.SynchronousState;
import xal.model.probe.traj.Trajectory;

/**
 * This class represents the behavior of the synchronous particle of a particle
 * beam bunch. Thus, its use is intended for evaluation of machine designs and
 * the variation of design trajectories with respect to machine parameters.
 *
 * @author Christopher K. Allen
 */
public class SynchronousProbe extends Probe<SynchronousState> {

    // Initialization
    public SynchronousProbe() {
        super();
        this.setRfPhase(0.0);
    }

    /**
     * @param probe
     */
    public SynchronousProbe(final SynchronousProbe probe) {
        super(probe);
        this.setRfPhase(probe.getRfPhase());
    }

    @Override
    public SynchronousProbe copy() {
        return new SynchronousProbe(this);
    }

    /**
     * Set the phase location of the synchronous particle with respect to the
     * drive RF power.
     *
     * @param dblPhase synchronous particle phase w.r.t. RF in <b>radians</b>
     */
    public void setRfPhase(double dblPhase) {
        this.stateCurrent.setRfPhase(dblPhase);
    }

    // Attribute Query
    /**
     * Return the phase of the synchronous particle with respect to any RF drive
     * power.
     *
     * @return phase location of synchronous particle in <b>radians</b>
     */
    public double getRfPhase() {
        return this.stateCurrent.getRfPhase();
    }

    // Trajectory Support
    /**
     * Creates a <code>Trajectory&lt;SynchronousState&gt;</code> object of the
     * proper type for saving the probe's history.
     *
     * @return a new, empty <code>Trajectory&lt;SynchronousState&gt;</code> for
     * saving the probe's history
     *
     * @author Jonathan M. Freed
     */
    @Override
    public Trajectory<SynchronousState> createTrajectory() {
        return new Trajectory<SynchronousState>(SynchronousState.class);
    }

    /**
     * Return a new <code>ProbeState</code> object, of the appropriate type,
     * initialized to the current state of this probe.
     *
     * @return probe state object of type <code>SynchronousState</code>
     *
     * @see xal.model.probe.Probe#createProbeState()
     */
    @Override
    public SynchronousState createProbeState() {
        return new SynchronousState(this);
    }

    /**
     * Creates a new, empty <code>SynchronousState</code>.
     *
     * @return a new, empty <code>SynchronousState</code>
     *
     * @author Jonathan M. Freed
     * @since Jul 1, 2014
     */
    @Override
    public SynchronousState createEmptyProbeState() {
        return new SynchronousState();
    }

//    /**
//     * Capture the current probe state to the <code>ProbeState</code> argument.  Note
//     * that the argument must be of the concrete type <code>SynchronousState</code>.
//     * 
//     * @param   state   <code>ProbeState</code> to receive this probe's state information
//     * 
//     * @exception IllegalArgumentException  argument is not of type <code>SynchronousState</code>
//     */   
//    @Override
//    public void applyState(SynchronousState state) {
//        if (!(state instanceof SynchronousState))
//            throw new IllegalArgumentException("invalid probe state");
//        SynchronousState    stateSync = (SynchronousState) state;
//        
//        super.applyState(state);
//        this.setBetatronPhase( stateSync.getBetatronPhase() );
//        this.setRfPhase( stateSync.getRfPhase() );
//    }

    @Override
    protected SynchronousState readStateFrom(DataAdaptor container) throws DataFormatException {
        SynchronousState state = new SynchronousState();
        state.load(container);
        return state;
    }
}
