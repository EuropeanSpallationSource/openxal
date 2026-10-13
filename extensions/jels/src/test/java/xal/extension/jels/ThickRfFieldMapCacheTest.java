package xal.extension.jels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import xal.extension.jels.model.elem.JElsElementMapping;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.RfCavity;
import xal.smf.proxy.RfCavityPropertyAccessor;

/**
 * ThickRfFieldMap memoizes its phase drift and energy gain computation. Changing the cavity settings and running the
 * same scenario again with the same probe must give the same result as a freshly generated scenario, not the result
 * computed for the old settings.
 *
 * Without space charge the tracker propagates each element in a single step, so a new run starts with exactly the
 * probe state the cache was last filled with; that is the case that used to return stale results.
 */
public class ThickRfFieldMapCacheTest {

    private static final double FREQUENCY = 352.21e6;
    private static final double ENERGY = 89.88782e6;
    private static final double AMPLITUDE = 0.974792;
    private static final double PHASE = -1.10293;

    private static final double TOLERANCE = 1e-12;

    private final AcceleratorSeq sequence = RfFieldMapTest.spokeFieldMap(0.988, FREQUENCY * 1e-6, AMPLITUDE, PHASE);
    private final RfCavity cavity = (RfCavity) sequence.getNodeWithId("TestFM");

    private Scenario newScenario(boolean spaceCharge) throws ModelException {
        EnvelopeProbe probe = TestCommon.setupOpenXALProbe(ENERGY, FREQUENCY, 0.0,
                new double[][]{{0., 1., 0.1}, {0., 1., 0.1}, {0., 1., 0.1}});
        ((EnvelopeTracker) probe.getAlgorithm()).setUseSpacecharge(spaceCharge);

        Scenario scenario = Scenario.newScenarioFor(sequence, JElsElementMapping.getInstance());
        scenario.setProbe(probe);
        scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
        return scenario;
    }

    private static double run(Scenario scenario) throws ModelException {
        scenario.resetProbe();
        scenario.resync();
        scenario.run();
        return ((EnvelopeProbe) scenario.getProbe()).getKineticEnergy();
    }

    /**
     * Run the scenario once with the design settings, change one cavity property, and run it again. The second run
     * must match a new scenario with the same property set.
     */
    private void checkPropertyChange(String property, double value, boolean spaceCharge) throws ModelException {
        Scenario scenario = newScenario(spaceCharge);
        double designEnergy = run(scenario);

        scenario.setModelInput(cavity, property, value);
        double energy = run(scenario);

        Scenario reference = newScenario(spaceCharge);
        reference.setModelInput(cavity, property, value);
        double expected = run(reference);

        assertNotEquals("The property change should affect the result", designEnergy, expected, 1e3);
        assertEquals(expected, energy, TOLERANCE * expected);
    }

    @Test
    public void testPhaseChange() throws ModelException {
        checkPropertyChange(RfCavityPropertyAccessor.PROPERTY_PHASE, cavity.getDfltCavPhase() + 30.0, false);
    }

    @Test
    public void testAmplitudeChange() throws ModelException {
        checkPropertyChange(RfCavityPropertyAccessor.PROPERTY_AMPLITUDE, 0.5 * cavity.getDfltCavAmp(), false);
    }

    @Test
    public void testPhaseChangeSpaceCharge() throws ModelException {
        checkPropertyChange(RfCavityPropertyAccessor.PROPERTY_PHASE, cavity.getDfltCavPhase() + 30.0, true);
    }

    @Test
    public void testAmplitudeChangeSpaceCharge() throws ModelException {
        checkPropertyChange(RfCavityPropertyAccessor.PROPERTY_AMPLITUDE, 0.5 * cavity.getDfltCavAmp(), true);
    }
}
