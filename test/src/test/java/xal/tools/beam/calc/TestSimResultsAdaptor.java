/**
 * TestSimResultsAdaptor.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 19, 2013
 */
package xal.tools.beam.calc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import xal.model.ModelException;

import xal.model.alg.EnvTrackerAdapt;
import xal.model.alg.ParticleTracker;
import xal.model.alg.TransferMapTracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.TransferMapProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.TransferMapState;
import xal.model.probe.traj.Trajectory;
import xal.test.ResourceManager;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.math.r3.R3;

/**
 * Test cases for the <code>SimResultsAdaptor</code> class.
 *
 * @author Christopher K. Allen
 * @since Nov 19, 2013
 */
public class TestSimResultsAdaptor {

    private static final Logger LOGGER = Logger.getLogger(TestSimResultsAdaptor.class.getName());

    /*
     * Global Constants
     */
    /**
     * Output file location
     */
    private static String STR_OUTPUT = TestSimResultsAdaptor.class.getName().replace('.', '/') + ".txt";

    /**
     * String identifier for accelerator sequence used in testing
     */
    private static String STR_SEQ_ID = "Ring";


    /*
     * Global Attributes 
     */
    /**
     * The file where we send the testing output
     */
    private static FileWriter owtrOutput;

    /**
     * Accelerator object used for testing
     */
    private static Accelerator accelTest;

    /**
     * Accelerator sequence used for testing
     */
    private static AcceleratorSeq seqTest;

    /**
     * Accelerator sequence (online) model for testing
     */
    private static Scenario modelTest;

    /**
     * Envelope probe for model testing
     */
    private static EnvelopeProbe probeEnvTest;

    /**
     * Particle probe for model testing
     */
    private static ParticleProbe probePartlTest;

    /**
     * Transfer map probe for model testing
     */
    private static TransferMapProbe probeXferTest;

    /*
     * Global Methods
     */
    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since Jul 16, 2012
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

//        ResourceManager.clearAllFileLocations();
        try {

            File fileOutput = ResourceManager.getOutputFile(STR_OUTPUT);
            owtrOutput = new FileWriter(fileOutput);

            accelTest = ResourceManager.getTestAccelerator();
            seqTest = accelTest.findSequence(STR_SEQ_ID);
            modelTest = Scenario.newScenarioFor(seqTest);
            modelTest.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);

            // Create and initialize the particle probe
            ParticleTracker algPart = AlgorithmFactory.createParticleTracker(seqTest);
            probePartlTest = ProbeFactory.createParticleProbe(seqTest, algPart);
            probePartlTest.reset();
            modelTest.setProbe(probePartlTest);
            modelTest.resync();
            modelTest.run();

//            LOGGER.log(Level.INFO, "\nParticleProbe Trajectory");
//            Trajectory<ParticleProbeState> trjPart = (Trajectory<ParticleProbeState>) MODEL_TEST.getTrajectory();
//            LOGGER.log(Level.INFO, trjPart);
            // Create and initialize transfer map probe
            TransferMapTracker algXferMap = AlgorithmFactory.createTransferMapTracker(seqTest);
            probeXferTest = ProbeFactory.getTransferMapProbe(seqTest, algXferMap);
            probeXferTest.reset();
            modelTest.setProbe(probeXferTest);
            modelTest.resync();
            modelTest.run();

//            LOGGER.log(Level.INFO, "\nTransferMap Trajectory");
//            Trajectory<TransferMapState> trjTrnsMap = (Trajectory<TransferMapState>) MODEL_TEST.getTrajectory();
//            LOGGER.log(Level.INFO, trjTrnsMap);
            // Create and initialize the envelope probe
            EnvTrackerAdapt algEnv = AlgorithmFactory.createEnvTrackerAdapt(seqTest);
            probeEnvTest = ProbeFactory.getEnvelopeProbe(seqTest, algEnv);
            probeEnvTest.reset();
            modelTest.setProbe(probeEnvTest);
            modelTest.resync();
            modelTest.run();

//            LOGGER.log(Level.INFO, "\nEnvelopeProbe Trajectory");
//            Trajectory<EnvelopeProbeState> trjEnv = (Trajectory<EnvelopeProbeState>) MODEL_TEST.getTrajectory();
//            LOGGER.log(Level.INFO, trjEnv);
        } catch (IOException | InstantiationException | ModelException e) {
            LOGGER.log(Level.SEVERE, "Unable to initial the static test resources", e);
            Assert.fail();
        }
    }

    /**
     *
     *
     * @author Christopher K. Allen
     * @since Nov 9, 2011
     */
    @AfterClass
    public static void commonCleanup() throws IOException {
        owtrOutput.flush();
        owtrOutput.close();
    }

    /*
    * Local Attributes
     */
    /**
     * Calculation engine for particle parameters using particle probe states
     */
    private CalculationsOnParticles calPartPart;

    /**
     * Calculation engine for machine parameters using transfer map states
     */
    private CalculationsOnMachines calXferMach;

    /**
     * Calculation engine for ring parameters using transfer map states
     */
    private CalculationsOnRings calXferRing;

    /**
     * Calculation engine for beam parameters using envelope probe states
     */
    private CalculationsOnBeams calEnvBeam;

    /**
     * the simulation adaptor
     */
    private SimResultsAdaptor cmpSimResults;

    /**
     *
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since May 3, 2011
     */
    @Before
    public void setUp() throws Exception {
        this.calPartPart = new CalculationsOnParticles(probePartlTest.getTrajectory());
        this.calXferMach = new CalculationsOnMachines(probeXferTest.getTrajectory());
        this.calXferRing = new CalculationsOnRings(probeXferTest.getTrajectory());
        this.calEnvBeam = new CalculationsOnBeams(probeEnvTest.getTrajectory());

        this.cmpSimResults = new SimResultsAdaptor();

        this.cmpSimResults.registerCalcEngine(ParticleProbeState.class, this.calPartPart);
        this.cmpSimResults.registerCalcEngine(TransferMapState.class, this.calXferMach);
        this.cmpSimResults.registerCalcEngine(EnvelopeProbeState.class, this.calEnvBeam);
    }

    /*
    * Tests
     */
    /**
     * Test method for
     * {@link xal.tools.beam.calc.SimResultsAdaptor#registerCalcEngine(java.lang.Class, xal.tools.beam.calc.ISimulationResults)}.
     */
    @Test
    public void testRegisterCalcEngine() {
        this.cmpSimResults.registerCalcEngine(ParticleProbeState.class, this.calPartPart);
        this.cmpSimResults.registerCalcEngine(TransferMapState.class, this.calXferMach);
        this.cmpSimResults.registerCalcEngine(EnvelopeProbeState.class, this.calEnvBeam);

        this.cmpSimResults.registerCalcEngine(TransferMapState.class, this.calXferRing);
    }

    /**
     * Test method for
     * {@link xal.tools.beam.calc.SimResultsAdaptor#computeCoordinatePosition(xal.model.probe.traj.ProbeState)}.
     *
     * @throws IOException
     */
    @Test
    public void testComputeCoordinatePosition() throws IOException {

        // Do computations on the particle trajectory
        owtrOutput.write("\nParticleTrajectory: computeCordinatePosition");
        owtrOutput.write("\n");
        Trajectory<ParticleProbeState> trjPart = probePartlTest.getTrajectory();
        for (ParticleProbeState state : trjPart) {
            PhaseVector vecPos = this.cmpSimResults.computeCoordinatePosition(state);

            owtrOutput.write(state.getElementId() + ": " + vecPos.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");

        // Do computations on the transfer map trajectory
        owtrOutput.write("\nTransferMapTrajectory: computeCoordinatePosition");
        owtrOutput.write("\n");
        Trajectory<TransferMapState> trjXfer = probeXferTest.getTrajectory();
        for (TransferMapState state : trjXfer) {
            PhaseVector vecPos = this.cmpSimResults.computeCoordinatePosition(state);

            owtrOutput.write(state.getElementId() + ": " + vecPos.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");

        // Do computations on the envelope trajectory
        owtrOutput.write("\nEnvelopeTrajectory: computeCoordinatePosition");
        owtrOutput.write("\n");
        Trajectory<EnvelopeProbeState> trjEnv = probeEnvTest.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            PhaseVector vecPos = this.cmpSimResults.computeCoordinatePosition(state);

            owtrOutput.write(state.getElementId() + ": " + vecPos.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");
    }

    /**
     * Test method for
     * {@link xal.tools.beam.calc.SimResultsAdaptor#computeFixedOrbit(xal.model.probe.traj.ProbeState)}.
     *
     * @throws IOException
     */
    @Test
    public void testComputeFixedOrbit() throws IOException {

        // Do computations on the particle trajectory
        owtrOutput.write("\nParticleTrajectory: computeFixedOrbit");
        owtrOutput.write("\n");
        Trajectory<ParticleProbeState> trjPart = probePartlTest.getTrajectory();
        for (ParticleProbeState state : trjPart) {
            PhaseVector vecPos = this.cmpSimResults.computeFixedOrbit(state);

            owtrOutput.write(state.getElementId() + ": " + vecPos.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");

        // Do computations on the transfer map trajectory
        owtrOutput.write("\nTransferMapTrajectory: computeFixedOrbit");
        owtrOutput.write("\n");
        Trajectory<TransferMapState> trjXfer = probeXferTest.getTrajectory();
        for (TransferMapState state : trjXfer) {
            PhaseVector vecPos = this.cmpSimResults.computeFixedOrbit(state);

            owtrOutput.write(state.getElementId() + ": " + vecPos.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");

        // Do computations on the envelope trajectory
        owtrOutput.write("\nEnvelopeTrajectory: computeFixedOrbit");
        owtrOutput.write("\n");
        Trajectory<EnvelopeProbeState> trjEnv = probeEnvTest.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            PhaseVector vecPos = this.cmpSimResults.computeFixedOrbit(state);

            owtrOutput.write(state.getElementId() + ": " + vecPos.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");
    }

    /**
     * Test method for
     * {@link xal.tools.beam.calc.SimResultsAdaptor#computeChromAberration(xal.model.probe.traj.ProbeState)}.
     */
    @Test
    public void testComputeChromaticAberration() throws IOException {

        // Do computations on the particle trajectory
        owtrOutput.write("\nParticleTrajectory: computeChromAberration");
        owtrOutput.write("\n");
        Trajectory<ParticleProbeState> trjPart = probePartlTest.getTrajectory();
        for (ParticleProbeState state : trjPart) {
            PhaseVector vecPos = this.cmpSimResults.computeChromAberration(state);

            owtrOutput.write(state.getElementId() + ": " + vecPos.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");

        // Do computations on the transfer map trajectory
        owtrOutput.write("\nTransferMapTrajectory: computeChromAberration");
        owtrOutput.write("\n");
        Trajectory<TransferMapState> trjXfer = probeXferTest.getTrajectory();
        for (TransferMapState state : trjXfer) {
            PhaseVector vecPos = this.cmpSimResults.computeChromAberration(state);

            owtrOutput.write(state.getElementId() + ": " + vecPos.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");

        // Do computations on the envelope trajectory
        owtrOutput.write("\nEnvelopeTrajectory: computeChromAberration");
        owtrOutput.write("\n");
        Trajectory<EnvelopeProbeState> trjEnv = probeEnvTest.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            PhaseVector vecPos = this.cmpSimResults.computeChromAberration(state);

            owtrOutput.write(state.getElementId() + ": " + vecPos.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");
    }

    /**
     * Test method for
     * {@link xal.tools.beam.calc.SimResultsAdaptor#computeTwissParameters(xal.model.probe.traj.ProbeState)}.
     */
    @Test
    public void testComputeTwissParameters() throws IOException {

        // Do computations on the transfer map trajectory
        owtrOutput.write("\nTransferMapTrajectory: computeTwissParameters");
        owtrOutput.write("\n");
        Trajectory<TransferMapState> trjXfer = probeXferTest.getTrajectory();
        for (TransferMapState state : trjXfer) {
            Twiss[] arrTwiss = this.cmpSimResults.computeTwissParameters(state);
            Twiss3D t3dMach = new Twiss3D(arrTwiss);

            owtrOutput.write(state.getElementId() + ": " + t3dMach.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");

        // Do computations on the EnvelopeTrajectory
        owtrOutput.write("\nEnvelopeTrajectory: computeTwissParameters");
        Trajectory<EnvelopeProbeState> trjEnv = probeEnvTest.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            Twiss[] arrTwiss = this.cmpSimResults.computeTwissParameters(state);
            Twiss3D t3dBeam = new Twiss3D(arrTwiss);

            owtrOutput.write(state.getElementId() + ": " + t3dBeam.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");
    }

    /**
     * Test method for
     * {@link xal.tools.beam.calc.SimResultsAdaptor#computeBetatronPhase(xal.model.probe.traj.ProbeState)}.
     */
    @Test
    public void testComputeBetatronPhase() throws IOException {

        // Do computations on the transfer map trajectory
        owtrOutput.write("\nTransferMapTrajectory: computeBetatronPhase");
        owtrOutput.write("\n");
        Trajectory<TransferMapState> trjXfer = probeXferTest.getTrajectory();
        for (TransferMapState state : trjXfer) {
            R3 vecPhase = this.cmpSimResults.computeBetatronPhase(state);

            owtrOutput.write(state.getElementId() + ": " + vecPhase.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");

        // Do computations on the EnvelopeTrajectory
        owtrOutput.write("\nEnvelopeTrajectory: computeBetatronPhase");
        Trajectory<EnvelopeProbeState> trjEnv = probeEnvTest.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            R3 vecPhase = this.cmpSimResults.computeBetatronPhase(state);

            owtrOutput.write(state.getElementId() + ": " + vecPhase.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");
    }

    /**
     * Test method for
     * {@link xal.tools.beam.calc.SimResultsAdaptor#computeChromDispersion(xal.model.probe.traj.ProbeState)}.
     */
    @Test
    public void testComputeChromDispersion() throws IOException {

        // Do computations on the transfer map trajectory
        owtrOutput.write("\nTransferMapTrajectory: computeChromDispersion");
        owtrOutput.write("\n");
        Trajectory<TransferMapState> trjXfer = probeXferTest.getTrajectory();
        for (TransferMapState state : trjXfer) {
            PhaseVector vecPhase = this.cmpSimResults.computeChromDispersion(state);

            owtrOutput.write(state.getElementId() + ": " + vecPhase.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");

        // Do computations on the EnvelopeTrajectory
        owtrOutput.write("\nEnvelopeTrajectory: computeChromDispersion");
        Trajectory<EnvelopeProbeState> trjEnv = probeEnvTest.getTrajectory();
        for (EnvelopeProbeState state : trjEnv) {
            PhaseVector vecPhase = this.cmpSimResults.computeChromDispersion(state);

            owtrOutput.write(state.getElementId() + ": " + vecPhase.toString());
            owtrOutput.write("\n");
        }
        owtrOutput.write("\n");
    }

}
