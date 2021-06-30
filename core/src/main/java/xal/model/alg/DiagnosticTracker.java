package xal.model.alg;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.probe.DiagnosticProbe;

/**
 * Simple algorithm for testing the model framework.
 *
 * @author Craig McChesney
 * @version $id:
 *
 */
public class DiagnosticTracker extends Tracker {

    private static final Logger LOGGER = Logger.getLogger(DiagnosticTracker.class.getName());

    /**
     * string type identifier for this algorithm
     */
    public static final String TYPE_ID = DiagnosticTracker.class.getName();

    /**
     * current version of this algorithm
     */
    public static final int VERSION = 1;

    /**
     * probe type recognized by this algorithm
     */
    public static final Class<DiagnosticProbe> CLS_PROBE_TYPE = DiagnosticProbe.class;

    /*
     * Initialization
     */
    /**
     * Creates a new instance of ParticleTracker
     */
    public DiagnosticTracker() {
        super(TYPE_ID, VERSION, CLS_PROBE_TYPE);
    }

    /**
     * Copy constructor for DiagnosticTracker
     *
     * @param sourceTracker Tracker that is being copied
     */
    public DiagnosticTracker(DiagnosticTracker sourceTracker) {
        super(sourceTracker);
    }

    /**
     * Create a deep copy of DiagnosticTracker
     */
    @Override
    public DiagnosticTracker copy() {
        return new DiagnosticTracker(this);
    }

    // ************* Tracker abstract protocol
    /**
     * Propagates the probe through the element.
     *
     * @param probe probe to propagate
     * @param elem element acting on probe
     *
     * @exception ModelException invalid probe type or error in advancing probe
     */
    @Override
    public void doPropagation(IProbe probe, IElement elem) throws ModelException {

        int nSteps = this.compStepCount(elem);
        double dblStepSize = elem.getLength() / nSteps;
        for (int i = 0; i < nSteps; i++) {
            this.advanceState(probe, elem, dblStepSize);
            this.advanceProbe(probe, elem, dblStepSize);
            probe.update();
        }
    }

    /**
     * Returns the number of sections to break the specified element in to for
     * propagation. Always one for a diagnostic probe.
     *
     * @param elem Element currently acting on probe
     *
     * @return one
     */
    protected int compStepCount(IElement elem) {
        return 1;
    }

    /**
     * Advance the supplied probe through a subsection of the specified length
     * in the specified element.
     *
     * @param probe Probe being acted on by element
     * @param elem Element acting on probe
     * @param dblLen length of element subsection to advance probe through
     */
    protected void advanceState(IProbe probe, IElement elem, double dblLen)
            throws ModelException {
        LOGGER.log(Level.INFO, "probe visiting: {0}", elem.getId());
        ((DiagnosticProbe) probe).incrementElementsVisited();
    }

}
