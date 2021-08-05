/*
 * EnvelopeTrackerPmq.java
 *
 *  Created on November, 2006
 *  Modified:
 *
 */
package xal.model.alg;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.IdealDrift;
import xal.model.elem.IdealMagSectorDipole2;
import xal.model.elem.IdealPermMagQuad;
import xal.model.probe.EnvelopeProbe;

/**
 * <p>
 * This class is a super class of the <code>EnvelopeTracker</code> class meant
 * to handle the special case of <code>IdealPermMagQuad</code> elements. There
 * really is not much extra to do here, just look for the exception element and
 * adjust the number of integration steps accordingly. This algorithm also
 * treats <code>IdealMagSectorDipole2</code> object in a special fashion.
 * </p>
 * <p>
 * See the <code>EnvelopeTracker</code> class for a complete description of the
 * functionality
 * </p>
 *
 * @see xal.model.alg.EnvelopeTracker
 *
 * @author Christopher K. Allen
 */
public class EnvelopeTrackerPmqDipole extends EnvelopeTracker {

    private static final Logger LOGGER = Logger.getLogger(EnvelopeTrackerPmqDipole.class.getName());

    /*
     * Global Constants
     */
 /*
     *  Global Attributes
     */
    /**
     * string type identifier for algorithm
     */
    public static final String TYPE_ID = EnvelopeTrackerPmqDipole.class.getName();

    /**
     * current algorithm version
     */
    public static final int VERSION = 1;

    /**
     * probe type recognized by this algorithm
     */
    public static final Class<EnvelopeProbe> CLS_PROBE_TYPE = EnvelopeProbe.class;

    /*
     *  Local Attributes
     */
 /*
     * Initialization
     */
    /**
     * Creates a new instance of EnvelopeTracker
     */
    public EnvelopeTrackerPmqDipole() {
        super(TYPE_ID, VERSION, CLS_PROBE_TYPE);
    }

    /**
     * Copy constructor for EnvelopeTrackerPmqDipole
     *
     * @param sourceTracker Tracker that is being copied
     */
    public EnvelopeTrackerPmqDipole(EnvelopeTrackerPmqDipole sourceTracker) {
        super(sourceTracker);
    }

    /**
     * Creates a deep copy of EnvelopeTrackerPmqDipole
     */
    @Override
    public EnvelopeTrackerPmqDipole copy() {
        return new EnvelopeTrackerPmqDipole(this);
    }

    /*
     *  Tracker Abstract Methods
     */
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

        int nSteps;
        double dblSize;

        //sako
        double elemPos = this.getElemPosition();
        double elemLen = elem.getLength();
        double propLen = elemLen - elemPos;

        if (propLen < 0) {
            LOGGER.log(Level.WARNING, "doPropagation, elemPos, elemLen = {0} {1}", new Object[]{elemPos, elemLen});
            return;
        }

        if (elem instanceof IdealPermMagQuad || elem instanceof IdealMagSectorDipole2) {
            nSteps = (int) Math.max(Math.ceil(propLen / getStepSize()), 1);

        } else if (elem instanceof IdealDrift) {
            nSteps = (int) Math.max(Math.ceil(propLen / getStepSize()), 1);

        } else if (this.getUseSpacecharge()) {
            nSteps = (int) Math.max(Math.ceil(propLen / getStepSize()), 1);

        } else {
            nSteps = 1;

        }

        dblSize = propLen / nSteps;
        for (int i = 0; i < nSteps; i++) {
            this.advanceState(probe, elem, dblSize);
            this.advanceProbe(probe, elem, dblSize);
        }
    }

}
