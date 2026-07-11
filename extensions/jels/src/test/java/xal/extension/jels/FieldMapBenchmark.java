/*
 * Copyright (C) 2026 European Spallation Source ERIC.
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

import java.util.Arrays;

import org.junit.Assume;
import org.junit.Test;

import xal.model.probe.Probe;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;

/**
 * Lightweight, repeatable timing harness for the field-map-heavy simulation
 * path. It runs the same MEBT-A2T combo sequence used by {@link GeneralTest}
 * (dense RF field maps) many times and reports min/median/mean run times.
 * <p>
 * The {@link Scenario} is built once and each timed iteration only re-runs
 * {@code resync()}+{@code run()} (with a fresh probe), so the measurement
 * reflects the simulation cost rather than the one-off lattice generation, as
 * in a typical optimization loop where settings change and the model re-runs.
 * <p>
 * It is guarded by the {@code jels.benchmark} system property so it does not
 * slow the normal {@code mvn verify}. Run it explicitly with:
 * <pre>
 *   mvn -pl extensions/jels test -Dtest=FieldMapBenchmark -Djels.benchmark=true
 * </pre>
 * or from the command line via {@link #main(String[])}.
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class FieldMapBenchmark {

    private static final String LATTICE = "lattice0/main.xal";
    private static final String PROBE = "lattice0/probe.0.xml";
    private static final String COMBO_SEQUENCE = "MEBT-A2T";

    private static final int WARMUP_RUNS = 3;
    private static final int TIMED_RUNS = 20;

    @Test
    public void benchmark() throws Exception {
        Assume.assumeTrue("Set -Djels.benchmark=true to run the benchmark.",
                Boolean.getBoolean("jels.benchmark"));
        run(WARMUP_RUNS, TIMED_RUNS);
    }

    public static void main(String[] args) throws Exception {
        int warmup = args.length > 0 ? Integer.parseInt(args[0]) : WARMUP_RUNS;
        int timed = args.length > 1 ? Integer.parseInt(args[1]) : TIMED_RUNS;
        run(warmup, timed);
    }

    private static AcceleratorSeq loadSequence() {
        String location = FieldMapBenchmark.class.getResource(LATTICE).toString();
        Accelerator accelerator = XMLDataManager.acceleratorWithUrlSpec(location);
        if (accelerator == null) {
            throw new Error("Could not load the accelerator from " + location);
        }
        return accelerator.getComboSequence(COMBO_SEQUENCE);
    }

    private static void run(int warmupRuns, int timedRuns) throws Exception {
        AcceleratorSeq sequence = loadSequence();
        String probeUrl = FieldMapBenchmark.class.getResource(PROBE).toString();

        // Build the scenario once; each iteration only re-runs the simulation.
        Scenario scenario = Scenario.newScenarioFor(sequence);
        scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);

        System.out.printf("FieldMapBenchmark: sequence=%s warmup=%d timed=%d%n",
                COMBO_SEQUENCE, warmupRuns, timedRuns);

        for (int i = 0; i < warmupRuns; i++) {
            simulate(scenario, GeneralTest.loadProbeFromXML(probeUrl));
        }

        double[] millis = new double[timedRuns];
        for (int i = 0; i < timedRuns; i++) {
            Probe probe = GeneralTest.loadProbeFromXML(probeUrl);
            long t0 = System.nanoTime();
            simulate(scenario, probe);
            millis[i] = (System.nanoTime() - t0) / 1e6;
        }

        Arrays.sort(millis);
        double min = millis[0];
        double median = millis[timedRuns / 2];
        double mean = Arrays.stream(millis).average().orElse(Double.NaN);
        System.out.printf("FieldMapBenchmark results (ms): min=%.1f median=%.1f mean=%.1f max=%.1f%n",
                min, median, mean, millis[timedRuns - 1]);
    }

    private static void simulate(Scenario scenario, Probe probe) throws Exception {
        scenario.setProbe(probe);
        scenario.resync();
        scenario.run();
    }
}
