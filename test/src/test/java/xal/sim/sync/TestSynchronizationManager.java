/**
 * TestSynchronizationManager.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 13, 2014
 */
package xal.sim.sync;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
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
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Quadrupole;

import xal.test.ResourceManager;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since Oct 13, 2014
 */
public class TestSynchronizationManager {

    private static final Logger LOGGER = Logger.getLogger(TestSynchronizationManager.class.getName());

    /*
     * Global Constants
     */
    /**
     * PV Logger ID of machine state when data was taken
     */
    public static final long LNG_PVLOGID = 19650065;

    /**
     * URL of the accelerator hardware description file
     */
    public static String urlAccel = ResourceManager.getTestAcceleratorURL().toString();

    /**
     * Output file location
     */
    private static String strFileOutput = TestSynchronizationManager.class.getName().replace('.', '/') + ".txt";

    /**
     * URL where we are dumping the output
     */
    public static File fileOutput = ResourceManager.getOutputFile(strFileOutput);

    /**
     * String identifier for accelerator sequence used in testing
     */
//    public static String            STR_SEQ_ID       = "HEBT1";
//    public static String            STR_SEQ_ID       = "MEBT-SCL";
    public static String seqId = "CCL";

    /*
     * Global Attributes
     */
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

    /**
     * Persistent storage for test output
     */
    private static PrintWriter wtrOutput;

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

        try {
//            ACCEL_TEST   = XMLDataManager.acceleratorWithUrlSpec(STRL_URL_ACCEL);
            accelTest = ResourceManager.getTestAccelerator();
            seqTest = accelTest.findSequence(seqId);
            modelTest = Scenario.newScenarioFor(seqTest);

            // Create and initialize the envelope probe
            EnvTrackerAdapt algEnv = AlgorithmFactory.createEnvTrackerAdapt(seqTest);
            probeEnvTest = ProbeFactory.getEnvelopeProbe(seqTest, algEnv);

            // Create and initialize the particle probe
            ParticleTracker algPrt = AlgorithmFactory.createParticleTracker(seqTest);
            probePartlTest = ProbeFactory.createParticleProbe(seqTest, algPrt);

            // Create and initialize transfer map probe
            TransferMapTracker algXfer = AlgorithmFactory.createTransferMapTracker(seqTest);
            probeXferTest = ProbeFactory.getTransferMapProbe(seqTest, algXfer);

            wtrOutput = new PrintWriter(fileOutput);

        } catch (FileNotFoundException | InstantiationException | ModelException e) {
            System.err.println("Unable to instantiate TransferMatrixObject");

        }
    }

    /**
     * Closes the output file stream.
     *
     * @throws Exception
     *
     * @author Christopher K. Allen
     * @since Jan 7, 2014
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        wtrOutput.close();
    }

    /*
     * Tests
     */
    /**
     * Run the online model for an envelope probe.
     *
     * @throws ModelException general synchronization or simulation error ?
     *
     * @author Christopher K. Allen
     * @since Jul 20, 2012
     */
    @Test
    public void testRunEnvelopeModel() throws ModelException {

        probeEnvTest.reset();
        modelTest.setProbe(probeEnvTest);
        modelTest.resync();
        modelTest.run();

        Trajectory<EnvelopeProbeState> trjData = modelTest.getTrajectory();

        this.saveSimData(trjData);
    }

    /**
     * Run the online model for an envelope probe.
     *
     * @throws ModelException general synchronization or simulation error ?
     *
     * @author Christopher K. Allen
     * @since Jul 20, 2012
     */
    @Test
    public void testRunEnvelopeModelWithRfGapCalc() throws ModelException {

        probeEnvTest.reset();
        probeEnvTest.getAlgorithm().setRfGapPhaseCalculation(true);
        modelTest.setProbe(probeEnvTest);
        modelTest.resync();
        modelTest.run();

        Trajectory<EnvelopeProbeState> trjData = modelTest.getTrajectory();

        this.saveSimData(trjData);
//        this.printSimData(trjData);
    }

    /**
     * Test method for {@link xal.sim.sync.SynchronizationManager#resync()}.
     *
     */
    @Test
    public final void testResync() {
        try {
            probeEnvTest.reset();

            modelTest.setProbe(probeEnvTest);
            modelTest.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
            modelTest.resync();

            modelTest.run();

            Trajectory<EnvelopeProbeState> trjData = modelTest.getTrajectory();
            this.saveSimData(trjData);

            List<AcceleratorNode> lstSmfQuads = seqTest.getNodesOfType("q", true);
            Quadrupole smfQuad1 = (Quadrupole) lstSmfQuads.get(0);
            double dblFldOld = smfQuad1.getDesignField();
            double dblFldNew = dblFldOld * 1.1;

//            LOGGER.log(Level.INFO, "Changing " + smfQuad1.getId() + " design field from " + dblFldOld + " to " + dblFldNew);
            Map<String, Double> mapPrpToValOld = modelTest.propertiesForNode(smfQuad1);
//            LOGGER.log(Level.INFO, "Old property map for " + smfQuad1.getId() + ": " + mapPrpToValOld.toString());

            smfQuad1.setDfltField(dblFldNew);
            modelTest.resync();
            Map<String, Double> mapPrpToValNew = modelTest.propertiesForNode(smfQuad1);
//            LOGGER.log(Level.INFO, "New property map for " + smfQuad1.getId() + ": " + mapPrpToValNew.toString());

            probeEnvTest.reset();
            modelTest.run();

            trjData = modelTest.getTrajectory();
            this.saveSimData(trjData);

        } catch (SynchronizationException e) {
            LOGGER.log(Level.SEVERE, null, e);

            fail("Synchronziation exception " + e.getMessage());

        } catch (ModelException e) {
            LOGGER.log(Level.SEVERE, null, e);

            fail("Online model run exception " + e.getMessage());
        }

    }

    /**
     * Test method for
     * {@link xal.sim.sync.SynchronizationManager#setModelInput(xal.smf.AcceleratorNode, java.lang.String, double)}.
     */
    @Test
    public final void testSetModelInput() {
    }

    /*
     * Support Methods
     */
    /**
     * Write the current simulation data to disk.
     *
     * @author Christopher K. Allen
     * @since Jan 7, 2014
     */
    private <S extends ProbeState<S>> void saveSimData(Trajectory<S> trjData) {

        // Write out header line
        String strSimType = modelTest.getProbe().getClass().getName();
        wtrOutput.println("DATA FOR SIMULATION WITH " + strSimType);
        wtrOutput.println("  RF Gap Phases " + modelTest.getProbe().getAlgorithm().getRfGapPhaseCalculation());

        // Write out the simulation data
//        Trajectory<?> trjData = MODEL_TEST.getTrajectory();
        for (S state : trjData) {
            wtrOutput.println(state);
        }

        // Buffer for the next write
        wtrOutput.println();
        wtrOutput.flush();
    }

    /**
     * Prints the simulation data to stdout
     *
     * @author Christopher K. Allen
     * @since Jan 7, 2014
     */
    private <S extends ProbeState<S>> void printSimData(Trajectory<S> trjData) {

        // Print out the kinetic energy profile to stdout
        LOGGER.log(Level.INFO, "DATA FOR SIMULATION WITH {0}", modelTest.getProbe().getClass().getName());
        LOGGER.log(Level.INFO, "  RF Gap Phases {0}", modelTest.getProbe().getAlgorithm().getRfGapPhaseCalculation());
//        Trajectory<?> trjData = MODEL_TEST.getTrajectory();

        for (S state : trjData) {

            String strId = state.getElementId();
            double dblW = state.getKineticEnergy();

            LOGGER.log(Level.INFO, "{0}: W = {1}", new Object[]{strId, dblW});
        }
    }
}
