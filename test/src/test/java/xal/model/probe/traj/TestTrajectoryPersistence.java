/**
 * TestTrajectoryPersistence.java
 *
 * Author  : Christopher K. Allen
 * Since   : Dec 29, 2015
 */
package xal.model.probe.traj;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.IAlgorithm;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.alg.ParticleTracker;
import xal.model.alg.TransferMapTracker;
import xal.model.alg.TwissTracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.Probe;
import xal.model.probe.TransferMapProbe;
import xal.model.probe.TwissProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.test.ResourceManager;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Dec 29, 2015
 */
public class TestTrajectoryPersistence {
    private static final Logger LOGGER = Logger.getLogger(TestTrajectoryPersistence.class.getName());
    
    /*
     * Global Variables
     */
    
    /** Probe state used in persistence test */
    private static Trajectory<ParticleProbeState>       trajPart;
    
    /** Probe state used in persistence test */
    private static Trajectory<TransferMapState>         trajXfer;
    
    /** Probe state used in persistence test */
    private static Trajectory<EnvelopeProbeState>       trajEnv;
    
    /** Probe state used in persistence test */
    private static Trajectory<TwissProbeState>          trajTwiss;
    

    
    /** Accelerator sequence used for testing */
//    public static final String     STR_ACCL_SEQ_ID = "HEBT2";
    public static final String     STR_ACCL_SEQ_ID = "SCLMed";
    
    
    
//    /** Output file name */
//    private static final String         STR_FILENAME_OUTPUT = "Trajectory.txt";
//
//    /** The results output file stream */
//    private static PrintStream        PSTR_OUTPUT;

    
    
    /*
     * Global Methods
     */

    /**
     * Pull in the probe state data from the model.params file.  We are going to use
     * just the initial state as the first and only state in the trajectory.
     * 
     * @throws java.lang.Exception
     *
     * @since  Dec 29, 2015,   Christopher K. Allen
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

//        File fileOutput = ResourceManager.getOutputFile(TestTrajectoryPersistence.class, STR_FILENAME_OUTPUT);
//            
//        PSTR_OUTPUT = new PrintStream(fileOutput);
//        
        try {
            Accelerator    accl  = ResourceManager.getTestAccelerator();
            AcceleratorSeq seq   = accl.getSequence(STR_ACCL_SEQ_ID);
            
            ParticleTracker algPart = AlgorithmFactory.createParticleTracker(seq);
            ParticleProbe   prbPart = ProbeFactory.createParticleProbe(seq, algPart);
            trajPart = createTrajectory(prbPart);

            TransferMapTracker  algXfer = AlgorithmFactory.createTransferMapTracker(seq);
            TransferMapProbe    prbXfer = ProbeFactory.getTransferMapProbe(seq, algXfer);
            trajXfer = createTrajectory(prbXfer);
            
            EnvTrackerAdapt algEnv = AlgorithmFactory.createEnvTrackerAdapt(seq);
            EnvelopeProbe   prbEnv = ProbeFactory.getEnvelopeProbe(seq, algEnv);
            trajEnv = createTrajectory(prbEnv);
            
            TwissTracker    algTws = AlgorithmFactory.createTwissTracker(seq);
            TwissProbe      prbTws = ProbeFactory.getTwissProbe(seq, algTws);
            trajTwiss = createTrajectory(prbTws);

        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, null, e);
            fail("Unable to create Trajectory - " + e.getMessage());
        }
    }
    
    /**
     * Forces the probe to create a trajectory object (by calling <code>Probe#initialize()</code>) then
     * adds the initial state of the probe to that trajectory object and returns it.
     *     
     * @param probe     probe object used to create the trajectory with one state
     * 
     * @return          trajectory for the given probe consisting of a single state (the initial state of probe)
     *
     * @since  Dec 29, 2015,   Christopher K. Allen
     */
    private static <S extends ProbeState<S>> Trajectory<S> createTrajectory(Probe<S> probe) {
        probe.initialize();
        
        Trajectory<S>   traj  = probe.getTrajectory();
        S               state = probe.getInitialState();
        traj.addState(state);
        
        return traj;
    }

    /**
     * @throws java.lang.Exception
     *
     * @since  Dec 29, 2015,   Christopher K. Allen
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    
    
    /*
     * Test Cases
     */
    
    /**
     *  Save the contained states of each trajectory container to a respective file.  This
     *  Test must pass in order for any other test to be successful. 
     *
     * @since  Dec 29, 2015,   Christopher K. Allen
     */
    @Test
    public final void testTrajectorySave() {
        
        this.saveTrajectory("ParticleProbeState.xml", trajPart);
        this.saveTrajectory("TransferMapState.xml", trajXfer);
        this.saveTrajectory("EnvelopeProbeState.xml", trajEnv);
        this.saveTrajectory("TwissProbeState.xml", trajTwiss);
    }
    
    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#loadFrom(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public final void testParticleTrajectoryPersistence() {
        String  strFileName = "ParticleTrajectory.xml";
        
        // Save the test trajectory
        this.saveTrajectory(strFileName, trajPart);
        
        // Recover the test trajectory from file
        Trajectory<ParticleProbeState> trjRes = this.loadTrajectory(strFileName);
        if (trjRes == null)
            fail("Trajectory<ParticleProbeState> did not work.");
        
        // Save to new file for comparison
        this.saveTrajectory(strFileName.replace(".xml", "Restored.xml"), trjRes);
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#loadFrom(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public final void testTransferMapTrajectoryPersistence() {
        String  strFileName = "TransferMapTrajectory.xml";
        
        // Save the test trajectory
        this.saveTrajectory(strFileName, trajXfer);
        
        // Recover the test trajectory from file
        Trajectory<TransferMapState> trjRes = this.loadTrajectory(strFileName);
        if (trjRes == null)
            fail("Trajectory<TransferMapState> did not work.");
        
        // Save to new file for comparison
        this.saveTrajectory(strFileName.replace(".xml", "Restored.xml"), trjRes);
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#loadFrom(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public final void testEnvelopeProbeTrajectoryPersistence() {
        String  strFileName = "EnvelopeProbeTrajectory.xml";
        
        // Save the test trajectory
        this.saveTrajectory(strFileName, trajEnv);
        
        // Recover the test trajectory from file
        Trajectory<EnvelopeProbeState> trjRes = this.loadTrajectory(strFileName);
        if (trjRes == null)
            fail("Trajectory<EnvelopeProbeState> did not work.");
        
        // Save to new file for comparison
        this.saveTrajectory(strFileName.replace(".xml", "Restored.xml"), trjRes);
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#loadFrom(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public final void testTwissProbeTrajectoryPersistence() {
        String  strFileName = "TwissProbeTrajectory.xml";
        
        // Save the test trajectory
        this.saveTrajectory(strFileName, trajTwiss);
        
        // Recover the test trajectory from file
        Trajectory<TwissProbeState> trjRes = this.loadTrajectory(strFileName);
        if (trjRes == null)
            fail("Trajectory<TwisProbeState> did not work.");
        
        // Save to new file for comparison
        this.saveTrajectory(strFileName.replace(".xml", "Restored.xml"), trjRes);
    }

    @Test
    public final void testSaveRestoreSimulation() {
        try {
            
            // Get the test accelerator and create an online model
            Accelerator    accl  = ResourceManager.getTestAccelerator();
            AcceleratorSeq seq   = accl.getSequence(STR_ACCL_SEQ_ID);
            Scenario       model = Scenario.newScenarioFor(seq);
            
            // Create an envelope probe for simulation
            IAlgorithm      alg  = AlgorithmFactory.createEnvTrackerAdapt(seq);
            EnvelopeProbe   prb  = ProbeFactory.getEnvelopeProbe(seq, alg);
            prb.initialize();

            // Initialize the model and run it
            model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
            model.setProbe( prb );
            model.resync();
            model.run();
            
            // Get the simulation results, save them, then restore them
            String      strFileName = "SimulationTrajectory.xml";
            
            Trajectory<EnvelopeProbeState>   trjSim = model.getTrajectory();
            this.saveTrajectory(strFileName, trjSim);
            Trajectory<EnvelopeProbeState>   trjRes = this.loadTrajectory(strFileName);
            this.saveTrajectory(strFileName.replace(".xml", "Restored.xml"), trjRes);
            
        } catch (ModelException | InstantiationException e) {
            LOGGER.log(Level.SEVERE, null, e);
            fail("Unable to run model and/or store/restore results");
            
        }
        
        
    }
    
    
    /*
     * Support Methods
     */
    
    /**
     * Saves the given trajectory data to XML data adaptor with the given file name (in the
     * test directory for this class).  A JUnit failure assertion is thrown if any error
     * occurs during the write.
     * 
     * @param strFileName   name of file to be written
     * @param traj          trajectory object that provides data
     * 
     * @return              <code>true</code> if successful, <code>false</code> if an exception occurred
     *
     * @since  Dec 29, 2015,   Christopher K. Allen
     */
    private <S extends ProbeState<S>> boolean saveTrajectory(String strFileName, Trajectory<S> traj)  {
        try {
            XmlDataAdaptor  daptSink = XmlDataAdaptor.newEmptyDocumentAdaptor();
            
            traj.save(daptSink);
            
            File            fileSink = ResourceManager.getOutputFile(this.getClass(), strFileName);
            daptSink.writeTo(fileSink);
            
            return true;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
            fail("Unable to write trajectory data for " + strFileName);
            return false;
        }
        
    }
    
    
    /**
     * Creates a new trajectory object by reading in the XML data in the given filename.
     * The data is loaded using an XML data adaptor object.  A JUnit failure is asserted if
     * an error occurs during the read or construction of the new trajectory object.
     *   
     * @param strFileName   name of the file containing the trajectory formatted data
     * 
     * @return              a new trajectory object of the correct type initialized with the data
     *                      in the given file, or <code>null</code> if a failure occurred.
     *
     * @since  Jan 5, 2016,   Christopher K. Allen
     */
    private <S extends ProbeState<S>> Trajectory<S> loadTrajectory(String strFileName)  {
        try {
            File            fileSrc = ResourceManager.getOutputFile(this.getClass(), strFileName);
            XmlDataAdaptor  daptSrc = XmlDataAdaptor.adaptorForFile(fileSrc, false);
            
            Trajectory<S>   trajSrc = Trajectory.loadFrom(daptSrc);
            
            return trajSrc;
            
        } catch (IllegalArgumentException | ParseException | ResourceNotFoundException | MalformedURLException e) {
            LOGGER.log(Level.SEVERE, null, e);
            fail("Unable to load trajectory data for " + strFileName);
            
            return null;
        }
        
    }

}
