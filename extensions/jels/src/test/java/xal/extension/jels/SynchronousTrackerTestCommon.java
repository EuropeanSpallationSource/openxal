/*
 * Copyright (C) 2021 European Spallation Source ERIC.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.extension.jels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized;
import xal.model.ModelException;
import xal.model.alg.SynchronousTracker;
import xal.model.probe.Probe;
import xal.model.probe.SynchronousProbe;
import xal.model.probe.traj.SynchronousState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public abstract class SynchronousTrackerTestCommon extends TestCommon {

    protected SynchronousTrackerTestData data;

    private void checkTWSynchronousPhase(double TWSyncPhase, double TWEnergyGain) {
        Trajectory<SynchronousState> trajectory = probe.getTrajectory();

        double OXSynchPhase = trajectory.finalState().getSynchronousPhase();
        OXSynchPhase *= 180. / Math.PI;

        double synchPhaseRelativeError = Math.abs((OXSynchPhase - TWSyncPhase) / TWSyncPhase);
        if (synchPhaseRelativeError >= data.errTolerance) {
            System.out.printf("TW phi_s = %s\n", TWSyncPhase);
            System.out.printf("OX phi_s = %s\n", OXSynchPhase);
            System.out.printf("Error = %s, tolerance = %s\n", synchPhaseRelativeError, data.errTolerance);
        }

        Assert.assertTrue("TW Synchronous Phase", synchPhaseRelativeError < data.errTolerance);

        double OXEnergyGain = trajectory.finalState().getEnergyGain();

        // TW data is energy gain per meter [MeV/m]
        OXEnergyGain /= data.sequence.getLength() * 1e6;

        double energyGainRelativeError = Math.abs((OXEnergyGain - TWEnergyGain) / TWEnergyGain);
        if (energyGainRelativeError >= data.errTolerance) {
            System.out.printf("TW E_kick = %s\n", TWEnergyGain);
            System.out.printf("OX E_kick = %s\n", OXEnergyGain);
            System.out.printf("Error = %s, tolerance = %s\n", energyGainRelativeError, data.errTolerance);
        }

        Assert.assertTrue("TW Energy Gain", energyGainRelativeError < data.errTolerance);
    }

    public static class SynchronousTrackerTestData {

        String description;

        // input
        Probe probe;
        ElementMapping elementMapping;
        AcceleratorSeq sequence;

        // TW output
        double TWSyncPhase;
        double TWEnergyGain;

        double errTolerance = 1e-5;

        @Override
        public String toString() {
            String params = String.format(Locale.ROOT, "E=%.2E", probe.getKineticEnergy());
            if (description != null) {
                return description + ", " + params;
            }
            return params;
        }
    }

    public SynchronousTrackerTestCommon(SynchronousTrackerTestData data) {
        super(data.probe, data.elementMapping);
        this.data = data;
    }

    public static SynchronousProbe setupOpenXALProbe(double energy, double frequency) {
        // Synchronous probe and tracker
        SynchronousTracker synchronousTracker = new SynchronousTracker();
        synchronousTracker.setRfGapPhaseCalculation(true);

        SynchronousProbe probe = new SynchronousProbe();
        probe.setAlgorithm(synchronousTracker);
        probe.setSpeciesCharge(speciesCharge);
        probe.setSpeciesRestEnergy(9.38272029e8);
        probe.setKineticEnergy(energy);
        probe.setPosition(0.0);
        probe.setTime(0.0);

        probe.initialize();

        return probe;
    }

    @Test
    public void test() throws ModelException {
        System.out.printf("\nResults of %s:\n", data.toString());

        run(data.sequence);

        checkTWSynchronousPhase(data.TWSyncPhase, data.TWEnergyGain);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> probes() {
        return new ArrayList<>(0);
    }
}
