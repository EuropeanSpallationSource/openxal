//
//  IdealMagOctupole.java
//  xal
//
//  Created by Hiroyuki Sako on 11/30/07.
//
package xal.model.elem;

import xal.model.IProbe;
import xal.model.PropagationException;
import xal.tools.beam.PhaseMap;

/**
 * Octupole magnets element
 *
 * Created by Hiroyuki Sako on 11/30/07.
 *
 *
 */
public class IdealMagOctupole extends ThinElement {

    /**
     * type string identifier for all octupole elements
     */
    public static final String TYPE = "IdealMagOctupole";

    /**
     * identity phase map
     */
    private static final PhaseMap IDENTITY_PHASE_MAP = PhaseMap.identity();

    /**
     * Constructor
     *
     * @param strId identifier of this object
     */
    public IdealMagOctupole(final String strId) {
        super(TYPE, strId);
    }

    /**
     * Constructor
     */
    public IdealMagOctupole() {
        super(TYPE);
    }

    /**
     * Returns the time taken for the probe to propagate through element.
     *
     * @param probe propagating probe
     * @return the value zero
     */
    @Override
    public double elapsedTime(final IProbe probe) {
        return 0.0;
    }

    /**
     * Returns energy gain which is zero.
     *
     * @param probe dummy argument
     * @return returns zero
     */
    @Override
    public double energyGain(IProbe probe) {
        return 0.0;
    }

    /**
     * Returns the transfer map of this element which is the identity.
     *
     * @param probe dummy argument
     * @return the identity phase map
     * @exception PropagationException this should not occur
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws PropagationException {
        return IDENTITY_PHASE_MAP;
    }
}
