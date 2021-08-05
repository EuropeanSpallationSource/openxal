/*
 * ParticleTracker.java
 *
 * Created on September 9, 2002, 11:16 AM
 */
package xal.model.alg;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.probe.ParticleProbe;

/**
 * Algorithm for tracking a single particle, represented by the class
 * <code>ParticleProbe</code> through a XAL modeling element, represented by an
 * object exposing the <code>IComponent</code> interface.
 *
 * @author Christopher K. Allen
 * @author Craig McChesney
 */
public class ParticleTracker extends Tracker {

    /**
     * string type identifier for this algorithm
     */
    public static final String TYPE_ID = ParticleTracker.class.getName();

    /**
     * current version of this algorithm
     */
    public static final int VERSION = 1;

    /**
     * probe type recognized by this algorithm
     */
    public static final Class<ParticleProbe> CLS_PROBE_TYPE = ParticleProbe.class;

    /*
     *  Local Attributes
     */
 /*
     * Initialization
     */
    /**
     * Creates a new instance of ParticleTracker
     */
    public ParticleTracker() {
        super(TYPE_ID, VERSION, CLS_PROBE_TYPE);
    }

    /**
     * Copy constructor for ParticleTracker
     *
     * @param sourceTracker Tracker that is being copied
     */
    public ParticleTracker(ParticleTracker sourceTracker) {
        super(sourceTracker);
    }

    /**
     * Create a deep copy of ParticleTracker
     */
    @Override
    public ParticleTracker copy() {
        return new ParticleTracker(this);
    }

    /*
     *  Tracker Abstract Protocol
     */
    /**
     * Propagates the probe through the element.
     *
     * @param iProbe probe to propagate
     * @param elem element acting on probe
     *
     * @exception ModelException invalid probe type or error in advancing probe
     */
    @Override
    public void doPropagation(IProbe iProbe, IElement elem) throws ModelException {

        if (!this.validProbe(iProbe)) {
            throw new ModelException("ParticleTracker::propagate() - cannot propagate, invalid probe type.");
        }
        ParticleProbe probe = (ParticleProbe) iProbe;

        double dblLen = elem.getLength();

        this.advanceState(probe, elem, dblLen);
        this.advanceProbe(probe, elem, dblLen);
    }

    /**
     * Advances the probe state through the element.
     *
     * @param probe probe being modified
     * @param elem element acting on probe
     * @param dblLen length of element to advance
     *
     * @exception ModelException bad element transfer matrix/corrupt probe state
     */
    protected void advanceState(ParticleProbe probe, IElement elem, double dblLen)
            throws ModelException {

        // Properties of the element
        PhaseMap mapPhi = elem.transferMap(probe, dblLen);

        // Advance state vector
        PhaseVector z0 = probe.getPhaseCoordinates();
        PhaseVector z1 = mapPhi.apply(z0);

        probe.setPhaseCoordinates(z1);

        // Advance response matrix
        PhaseMatrix matPhi = mapPhi.getFirstOrder();
        PhaseMatrix r0 = probe.getResponseMatrix();
        PhaseMatrix r1 = matPhi.times(r0);

        probe.setResponseMatrix(r1);
    }
}
