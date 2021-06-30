/**
 * TestTrajectory.java
 *
 * Author  : Christopher K. Allen
 * Since   : Aug 25, 2014
 */
package xal.model.probe.traj;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.IAlgorithm;
import xal.model.IComponent;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.elem.Element;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.Probe;
import xal.model.probe.TransferMapProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.test.ResourceManager;
import xal.tools.beam.PhaseVector;

/**
 * <p>
 * Class of test cases for class <code>{@link Trajectory}</code>.
 * <p/>
 * <p>
 * Use Java virtual machine command line switch
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <kbd>java -agentlib:hprof=cpu=times</kbd>
 * <br/>
 * <br/>
 * to create <code>java.hprof.TMP</code> files for profiling.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Aug 25, 2014
 */
public class TestTrajectory {

    private static final Logger LOGGER = Logger.getLogger(TestTrajectory.class.getName());

    /*
     * Global Constants
     */
    /**
     * Flag used for indicating whether to type out to stout or file
     */
    private static final boolean BOL_TYPE_STOUT = false;

    /**
     * Output file name
     */
    private static final String STR_FILENAME_OUTPUT = TestTrajectory.class.getName() + ".txt";

    /**
     * Accelerator sequence used for testing
     */
    public static final String STR_ACCL_SEQ_ID = "HEBT2";

    /**
     * Bending Dipole ID
     */
    public static final String STR_DH1_ID = "HEBT_Mag:DH11";

    /**
     * Bending Dipole ID
     */
    public static final String STR_DH2_ID = "HEBT_Mag:DH12";

    /*
     * Global Resources
     */
    /**
     * Accelerator hardware under test
     */
    private static Accelerator accelTest;

    /**
     * Accelerator sequence under test
     */
    private static AcceleratorSeq seqTest;

    /**
     * The results output file stream
     */
    private static PrintStream pstrOutput;

    /*
     * Global Attributes
     */
    /**
     * The online model scenario for the given accelerator sequence
     */
    private static Scenario modelTest;

    /**
     * Envelope probe used for simulations
     */
    private static EnvelopeProbe probeEnv;

    /**
     * Particle probe used for simulations
     */
    private static ParticleProbe probePartc;

    /**
     * Transfer map probe used for simulations
     */
    private static TransferMapProbe probeXfer;

    /*
     * Global Methods
     */
    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since Aug 25, 2014
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        if (BOL_TYPE_STOUT) {
            pstrOutput = System.out;

        } else {

            File fileOutput = ResourceManager.getOutputFile(TestTrajectory.class, STR_FILENAME_OUTPUT);

            pstrOutput = new PrintStream(fileOutput);
        }

        try {
            accelTest = ResourceManager.getTestAccelerator();
            seqTest = accelTest.getSequence(STR_ACCL_SEQ_ID);
            modelTest = Scenario.newScenarioFor(seqTest);

            IAlgorithm algor = AlgorithmFactory.createEnvTrackerAdapt(seqTest);
            probeEnv = ProbeFactory.getEnvelopeProbe(seqTest, algor);
            probeEnv.initialize();

            algor = AlgorithmFactory.createParticleTracker(seqTest);
            probePartc = ProbeFactory.createParticleProbe(seqTest, algor);
            probePartc.initialize();

            algor = AlgorithmFactory.createTransferMapTracker(seqTest);
            probeXfer = ProbeFactory.getTransferMapProbe(seqTest, algor);
            probeXfer.initialize();

        } catch (ModelException | InstantiationException e) {
            fail("Unable to create Scenario");
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since Aug 25, 2014
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /*
     * Test Cases
     */
    /**
     * Prints out all the element in the online model.
     *
     * @throws ModelException
     *
     * @author Christopher K. Allen
     * @since Aug 26, 2014
     */
    @Test
    public final void TestModel() throws ModelException {
        Lattice latTest = modelTest.getLattice();
        Iterator<IComponent> itrCmps = latTest.globalIterator();

        int index = 0;
        pstrOutput.println();
        pstrOutput.println("ELEMENTS contained in MODEL");
        while (itrCmps.hasNext()) {
            IComponent cmp = itrCmps.next();
            if (cmp instanceof Element) {
                pstrOutput.println("  " + index + " " + (Element) cmp);
            } else {
                pstrOutput.println("  " + index + " " + cmp.getId());
            }
            index++;
        }
    }

    /**
     * Iterates through all the states in the trajectory using a for each
     * construct.
     *
     * @author Christopher K. Allen
     * @since Sep 5, 2014
     */
    @Test
    public final void testStateIterator() {
        Trajectory<ParticleProbeState> trjPartc = this.runModel(probePartc);

        pstrOutput.println();
        pstrOutput.println("STATES retrieved iteratation using the Iterable<> interface");
        int index = 0;
        for (ParticleProbeState state : trjPartc) {
            pstrOutput.println("  " + index
                    + " " + state.getElementId()
                    + " from " + state.getHardwareNodeId()
                    + " at position " + state.getPosition()
            );
            index++;
        }
    }

    /**
     * Test method for
     * {@link xal.model.probe.traj.Trajectory#stateForElement(java.lang.String)}.
     */
    @Test
    public final void testStateForElement() {
        Trajectory<TransferMapState> trjXfer = this.runModel(probeXfer);

        pstrOutput.println();
        pstrOutput.println("SINGLE STATE for " + STR_DH1_ID);
        TransferMapState state1 = trjXfer.stateForElement(STR_DH1_ID);
        pstrOutput.println("  " + state1.getElementId() + " at position " + state1.getPosition());

        pstrOutput.println();
        pstrOutput.println("SINGLE STATE for " + STR_DH2_ID);
        TransferMapState state2 = trjXfer.stateForElement(STR_DH2_ID);
        pstrOutput.println("  " + state2.getElementId() + " at position " + state2.getPosition());
    }

    /**
     * Test method for
     * {@link xal.model.probe.traj.Trajectory#statesForElement(java.lang.String)}.
     */
    @Test
    public final void testStatesForElement() {
        Trajectory<TransferMapState> trjXfer = this.runModel(probeXfer);

        pstrOutput.println();
        pstrOutput.println("STATES for " + STR_DH1_ID);
        for (TransferMapState state : trjXfer.statesForElement(STR_DH1_ID)) {
            pstrOutput.println("  " + state.getElementId() + " at position " + state.getPosition());
        }

        pstrOutput.println();
        pstrOutput.println("STATES for " + STR_DH2_ID);
        for (TransferMapState state : trjXfer.statesForElement(STR_DH2_ID)) {
            pstrOutput.println("  " + state.getElementId() + " at position " + state.getPosition());
        }

    }

    /**
     * Test method for
     * {@link xal.model.probe.traj.Trajectory#stateAtPosition(double)}.
     */
    @Test
    public final void testStateAtPosition() {
    }

    /**
     * Prints out all the states in the trajectory to standard out as retrieved
     * by the internal numeric indexer.
     *
     * @author Christopher K. Allen
     * @since Aug 26, 2014
     */
    @Test
    public final void testGetStateViaIndexer() {
        Trajectory<TransferMapState> trjXfer = this.runModel(probeXfer);

        List<TransferMapState> lstStates = trjXfer.getStatesViaIndexer();
        pstrOutput.println();
        pstrOutput.println("STATES retrieved by the INDEXER");
        int index = 0;
        for (TransferMapState state : lstStates) {
            pstrOutput.println("  " + index
                    + " " + state.getElementId()
                    + " from " + state.getHardwareNodeId()
                    + " at position " + state.getPosition()
            );
            index++;
        }
    }

    /**
     * Prints out all the states in the trajectory to standard out as retrieved
     * by the node ID to state map.
     *
     * @author Christopher K. Allen
     * @since Aug 26, 2014
     */
    @Test
    public final void testGetStateViaMap() {
        Trajectory<TransferMapState> trjXfer = this.runModel(probeXfer);

        List<TransferMapState> lstStates = trjXfer.getStatesViaStateMap();
        pstrOutput.println();
        pstrOutput.println("STATES retrieved by the STATE MAP");
        int index = 0;
        for (TransferMapState state : lstStates) {
            pstrOutput.println("  " + index + " " + state.getElementId() + " at position " + state.getPosition());
            index++;
        }
    }

    /**
     * Test method for
     * {@link xal.model.probe.traj.Trajectory#statesForElement(java.lang.String)}.
     */
    @Test
    public final void testStatesForElement_OLD() {
        Trajectory<TransferMapState> trjXfer = this.runModel(probeXfer);

        List<TransferMapState> lstStates = trjXfer.statesForElement(STR_DH1_ID);
        pstrOutput.println();
        pstrOutput.println("STATES for " + STR_DH1_ID);
        for (TransferMapState state : lstStates) {
            pstrOutput.println("  " + state.getElementId() + " at position " + state.getPosition());
        }
    }

    /**
     * Test the sub-trajectories methods of the <code>Trajectory</code> class.
     *
     * @author Christopher K. Allen
     * @since Nov 17, 2014
     */
    @Test
    public final void testSubTrajectory() {
        Trajectory<TransferMapState> trjXfer = this.runModel(probeXfer);

        Trajectory<TransferMapState> trjSubEx = trjXfer.subTrajectory(STR_DH1_ID, STR_DH2_ID);
        pstrOutput.println();
        pstrOutput.println("SUBTRAJECTORY (EXCLUSIVE): STATES between " + STR_DH1_ID + " and " + STR_DH2_ID);
        for (TransferMapState state : trjSubEx) {
            pstrOutput.println("  " + state.getElementId() + " at position " + state.getPosition());
        }

        Trajectory<TransferMapState> trjSubIn = trjXfer.subTrajectoryInclusive(STR_DH1_ID, STR_DH2_ID);
        pstrOutput.println();
        pstrOutput.println("SUBTRAJECTORY (INCLUSIVE): STATES between " + STR_DH1_ID + " and " + STR_DH2_ID);
        for (TransferMapState state : trjSubIn) {
            pstrOutput.println("  " + state.getElementId() + " at position " + state.getPosition());
        }
    }

    /**
     * Test method for
     * {@link xal.model.probe.traj.Trajectory#statesForElement(java.lang.String)}.
     */
    @Test
    public final void testParticleProbe() {
        probePartc.setPhaseCoordinates(new PhaseVector(0.001, 0, 0, 0, 0, 0));
        probePartc.initialize();

        Trajectory<ParticleProbeState> trjPartc = this.runModel(probePartc);

        pstrOutput.println();
        pstrOutput.println("PARTICLE PROBE STATES");
        for (ParticleProbeState state : trjPartc.getStatesViaIndexer()) {
            pstrOutput.println("  " + state.getElementId() + " at position " + state.getPosition() + ": z = " + state.getPhaseCoordinates());
        }

    }

    /**
     * Test method for
     * {@link xal.model.probe.traj.Trajectory#statesForElement(java.lang.String)}.
     */
    @Test
    public final void testEnvelopeProbe() {

        Trajectory<EnvelopeProbeState> trjEnv = this.runModel(probeEnv);

        pstrOutput.println();
        pstrOutput.println("ENVELOPE PROBE STATES");
        for (EnvelopeProbeState state : trjEnv.getStatesViaIndexer()) {
            pstrOutput.println("  " + state.getElementId() + " at position " + state.getPosition() + ": sigma = " + state.getCovarianceMatrix());
        }

    }

    /*
     * Support Methods
     */
    /**
     * Runs the global online model for the testing class on the given probe
     * object. The results are returned in an untyped <code>Trajectory<?></code>
     * object.
     *
     * @param prbTest The probe to be simulated
     *
     * @return simulation data for the given probe
     *
     * @author Christopher K. Allen
     * @since Aug 25, 2014
     */
    private <S extends ProbeState<S>> Trajectory<S> runModel(Probe<S> prbTest) {

        try {
            prbTest.reset();
            modelTest.setProbe(prbTest);
            modelTest.resync();
            modelTest.run();

            Trajectory<S> trjTest = modelTest.getTrajectory();

            return trjTest;

        } catch (SynchronizationException e) {
            LOGGER.log(Level.SEVERE, null, e);
            fail("Unable to synchronize model values");

        } catch (ModelException e) {
            LOGGER.log(Level.SEVERE, null, e);
            fail("Error running the online model");
        }

        return null;
    }
}
