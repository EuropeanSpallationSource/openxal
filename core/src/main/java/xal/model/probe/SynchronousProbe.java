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
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class SynchronousProbe extends BunchProbe<SynchronousState> {

    // Initialization
    public SynchronousProbe() {
        super();
        this.setSynchronousPhase(0.0);
        this.setEnergyGain(0.0);
    }

    /**
     * @param probe
     */
    public SynchronousProbe(final SynchronousProbe probe) {
        super(probe);
        this.setSynchronousPhase(probe.getSynchronousPhase());
        this.setEnergyGain(probe.getEnergyGain());
    }

    @Override
    public SynchronousProbe copy() {
        return new SynchronousProbe(this);
    }

    /**
     * Set the synchronous phase.
     *
     * @param dblPhase synchronous phase in <b>radians</b>
     */
    public void setSynchronousPhase(double dblPhase) {
        this.stateCurrent.setSynchronousPhase(dblPhase);
    }

    // Attribute Query
    /**
     * Return the synchronous phase.
     *
     * @return synchronous phase in <b>radians</b>
     */
    public double getSynchronousPhase() {
        return this.stateCurrent.getSynchronousPhase();
    }

    /**
     * Set the energy gain for the current element.
     *
     * @param dblEnergyGain energy gain in <b>eV</b>
     */
    public void setEnergyGain(double dblEnergyGain) {
        this.stateCurrent.setEnergyGain(dblEnergyGain);
    }

    /**
     * Return the energy gain in the current element.
     *
     * @return energy gain in <b>eV</b>
     */
    public double getEnergyGain() {
        return this.stateCurrent.getEnergyGain();
    }

    // Trajectory Support
    /**
     * Creates a <code>Trajectory&lt;SynchronousState&gt;</code> object of the
     * proper type for saving the probe's history.
     *
     * @return a new, empty <code>Trajectory&lt;SynchronousState&gt;</code> for
     * saving the probe's history
     */
    @Override
    public Trajectory<SynchronousState> createTrajectory() {
        return new Trajectory<>(SynchronousState.class);
    }

    /**
     * Return a new <code>ProbeState</code> object, of the appropriate type,
     * initialized to the current state of this probe.
     *
     * @return probe state object of type <code>SynchronousState</code>
     */
    @Override
    public SynchronousState createProbeState() {
        return new SynchronousState(this);
    }

    /**
     * Creates a new, empty <code>SynchronousState</code>.
     *
     * @return a new, empty <code>SynchronousState</code>
     */
    @Override
    public SynchronousState createEmptyProbeState() {
        return new SynchronousState();
    }

    @Override
    protected SynchronousState readStateFrom(DataAdaptor container) throws DataFormatException {
        SynchronousState state = new SynchronousState();
        state.load(container);
        return state;
    }
}
