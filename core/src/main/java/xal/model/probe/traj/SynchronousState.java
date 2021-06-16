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
package xal.model.probe.traj;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.model.probe.SynchronousProbe;

/**
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class SynchronousState extends BunchProbeState<SynchronousState> {

    // Global Constants
    // element tag for Synchronouse State
    protected static final String LABEL_SYNCH = "synch";

    // attribute tag for synchronous phase
    protected static final String ATTR_SYNCH_PHASE = "synch_phase";

    // attribute tag for energy gain
    protected static final String ATTR_ENERGY_GAIN = "energy_gain";

    // Local Attributes
    // Synchronous phase
    private double m_dblPhsRf;
    // Energy gain
    private double m_dblEnergyGain;

    // Initialization
    /**
     * Default constructor. Create a new <code>SynchronousState</code> object
     * with zero state values.
     */
    public SynchronousState() {
        super();
        this.m_dblPhsRf = 0.0;
        this.m_dblEnergyGain = 0.0;
    }

    /**
     * Copy constructor for SynchronousState. Initializes the new
     * <code>SynchronousState</code> objects with the state attributes of the
     * given <code>SynchronousState</code>.
     *
     * @param stateSync initializing state
     */
    public SynchronousState(SynchronousState stateSync) {
        super(stateSync);

        this.m_dblPhsRf = stateSync.m_dblPhsRf;
        this.m_dblEnergyGain = stateSync.m_dblEnergyGain;
    }

    /**
     * Copy constructor. Create a new <code>SynchronousState</code> object and
     * initialize the state to that of the specified probe argument.
     *
     * @param probe probe containing initializing state information
     */
    public SynchronousState(SynchronousProbe probe) {
        super(probe);
        this.setSynchronousPhase(probe.getSynchronousPhase());
        this.setEnergyGain(probe.getEnergyGain());
    }

    // Property Accessors 
    /**
     * Set synchronous phase.
     *
     * @param dblPhase synchronous phase in <b>radians</b>
     */
    public void setSynchronousPhase(double dblPhase) {
        this.m_dblPhsRf = dblPhase;
    }

    /**
     * Return the synchronous phase.
     *
     * @return synchronous phase in <b>radians</b>
     */
    public double getSynchronousPhase() {
        return this.m_dblPhsRf;
    }

    /**
     * Set the energy gain for the current element.
     *
     * @param dblEnergyGain energy gain in <b>eV</b>
     */
    public void setEnergyGain(double dblEnergyGain) {
        this.m_dblEnergyGain = dblEnergyGain;
    }

    /**
     * Return the energy gain in the current element.
     *
     * @return energy gain in <b>eV</b>
     */
    public double getEnergyGain() {
        return m_dblEnergyGain;
    }

    // ProbeState Overrides
    @Override
    public SynchronousState copy() {
        return new SynchronousState(this);
    }

    @Override
    protected void addPropertiesTo(DataAdaptor daptSink) {
        super.addPropertiesTo(daptSink);

        DataAdaptor daptSync = daptSink.createChild(SynchronousState.LABEL_SYNCH);
        daptSync.setValue(SynchronousState.ATTR_SYNCH_PHASE, this.getSynchronousPhase());
        daptSync.setValue(SynchronousState.ATTR_ENERGY_GAIN, this.getEnergyGain());
    }

    @Override
    protected void readPropertiesFrom(DataAdaptor daptSrc)
            throws DataFormatException {
        super.readPropertiesFrom(daptSrc);

        DataAdaptor daptSync = daptSrc.childAdaptor(SynchronousState.LABEL_SYNCH);
        if (daptSync == null) {
            throw new DataFormatException("SynchronousState#readPropertiesFrom(): no child element = " + LABEL_SYNCH);
        }

        if (daptSync.hasAttribute(SynchronousState.ATTR_SYNCH_PHASE)) {
            this.setSynchronousPhase(daptSync.doubleValue(SynchronousState.ATTR_SYNCH_PHASE));
        }
        if (daptSync.hasAttribute(SynchronousState.ATTR_ENERGY_GAIN)) {
            this.setEnergyGain(daptSync.doubleValue(SynchronousState.ATTR_ENERGY_GAIN));
        }
    }

    // Object Overrides
    @Override
    public String toString() {
        return super.toString()
                + " synchPhase=" + getSynchronousPhase()
                + " energyGain=" + getEnergyGain();
    }

}
