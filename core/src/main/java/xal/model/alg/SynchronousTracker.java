/*
 * Copyright (c) 2021, Open XAL Collaboration
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package xal.model.alg;

import xal.model.IComponent;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.sync.IRfCavity;
import xal.model.elem.sync.IRfGap;
import xal.model.probe.SynchronousProbe;

/**
 * Algorithm for propagating a <code>SynchronousProbe</code> object through any
 * modeling element that exposes the <code>IComponent</code> interface. It
 * computes the synchronous phase at each cavity and cavity gaps, as well as the
 * energy gain.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 *
 */
public class SynchronousTracker extends Tracker {

    //  Global Constants
    // string type identifier for this algorithm
    public static final String TYPE_ID = SynchronousTracker.class.getName();

    // current version of this algorithm
    public static final int VERSION = 1;

    // probe type recognized by this algorithm
    public static final Class<SynchronousProbe> CLS_PROBE_TYPE = SynchronousProbe.class;

    /**
     * Default constructor for a <code>SynchronousTracker</code> objects. These
     * objects have no internal state information.
     *
     */
    public SynchronousTracker() {
        super(TYPE_ID, VERSION, CLS_PROBE_TYPE);
    }

    /**
     * Copy constructor for SynchronousTracker
     *
     * @param sourceTracker Tracker that is being copied
     */
    public SynchronousTracker(SynchronousTracker sourceTracker) {
        super(sourceTracker);
    }

    /**
     * Creates a deep copy of SynchronousTracker
     */
    @Override
    public SynchronousTracker copy() {
        return new SynchronousTracker(this);
    }

    // Tracker Protocol
    /**
     * Perform the actual probe propagation through the the modeling element.
     *
     * @param probe interface to <code>SynchronousProbe</code> to be advanced
     * @param elem interface to modeling element through which to advance probe
     *
     * @throws ModelException error during propagation
     *
     * @see xal.model.alg.Tracker#doPropagation(xal.model.IProbe,
     * xal.model.IElement)
     */
    @Override
    public void doPropagation(IProbe probe, IElement elem)
            throws ModelException {
        double elemPos = this.getElemPosition();
        double elemLen = elem.getLength();
        double propLen = elemLen - elemPos;

        this.advanceState(probe, elem, propLen);
        this.advanceProbe(probe, elem, propLen);
    }

    /**
     * This method was included to deal with RfCavitie objects
     *
     * @param probe
     * @param elem
     * @throws ModelException
     */
    public void propagate(IProbe probe, IComponent elem) throws ModelException {
        probe.setCurrentElement(elem.getId());
        probe.setCurrentElementTypeId(elem.getType());
        probe.setCurrentHardwareId(elem.getHardwareNodeId());

        if (elem instanceof IRfCavity) {
            IRfCavity cavity = (IRfCavity) elem;
            SynchronousProbe syncProbe = (SynchronousProbe) probe;
            cavity.computeSynchronousPhaseAndEnergyGain();
            syncProbe.setSynchronousPhase(cavity.getSynchronousPhase());
            syncProbe.setEnergyGain(cavity.getEnergyGain());
        }

        if (this.getProbeUpdatePolicy() == Tracker.UPDATE_ALWAYS) {
            probe.update();
        }
    }

    protected void advanceState(IProbe ifcProbe, IElement elem, double dblLen) {
        SynchronousProbe probe = (SynchronousProbe) ifcProbe;

        if (elem instanceof IRfGap) {
            IRfGap gap = (IRfGap) elem;
            gap.computeSynchronousPhaseAndEnergyGain(probe);
            probe.setSynchronousPhase(gap.getSynchronousPhase());
            probe.setEnergyGain(gap.getEnergyGain());
        }
    }
}
