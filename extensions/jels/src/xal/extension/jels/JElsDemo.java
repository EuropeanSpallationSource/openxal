package xal.extension.jels;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.xml.LatticeXmlWriter;
import xal.model.xml.ParsingException;
import xal.model.xml.ProbeXmlParser;
import xal.model.xml.ProbeXmlWriter;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.xml.XmlDataAdaptor;

public class JElsDemo {

    private static final Logger LOGGER = Logger.getLogger(JElsDemo.class.getName());

    public static void main(String[] args) throws InstantiationException, ModelException {
        if (args.length > 0 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
            System.out.println("Usage: [-a] [combo sequence] [accelerator file main.xal] [probe file]");
            System.out.println("	-a	use adaptive tracker");
            System.out.println("	If no combo sequence is given, the first one is choosen");
            return;
        }

        Accelerator accelerator;
        AcceleratorSeq sequence;
        EnvelopeProbe probe;
        String comboSequence = null;

        boolean adaptiveTracker = false;

        int argspos = 0;

        // Option -a
        if (args.length > argspos && args[argspos].equals("-a")) {
            adaptiveTracker = true;
            argspos++;
        }

        // combo sequence
        if (args.length > argspos) {
            comboSequence = args[argspos++];
        }

        // path to accelerator
        if (args.length > argspos) {
            accelerator = loadAccelerator(args[argspos++]);
        } else {
            accelerator = loadAccelerator();
        }

        // now load the sequence
        if (comboSequence != null) {
            sequence = accelerator.getComboSequence(comboSequence);
            if (sequence == null) {
                sequence = accelerator.getSequence(comboSequence);
            }
            if (sequence == null) {
                LOGGER.severe("No appropriate sequence with name: " + comboSequence);
                return;
            }
        } else {
            sequence = accelerator.getComboSequences().get(0);
            System.out.println("Selecting combo sequence " + sequence.getId());
        }

        // path to probe
        if (args.length > argspos) {
            probe = loadProbeFromXML(args[argspos++]);
        } else {
            Tracker tracker;
            if (adaptiveTracker) {
                tracker = AlgorithmFactory.createEnvTrackerAdapt(sequence);
            } else {
                tracker = AlgorithmFactory.createEnvelopeTracker(sequence);
            }
            probe = ProbeFactory.getEnvelopeProbe(sequence, tracker);
        }

        run(sequence, probe);
    }

    public static void run(AcceleratorSeq sequence, EnvelopeProbe probe) throws ModelException {
        // Generates lattice from SMF accelerator
        Scenario scenario = Scenario.newScenarioFor(sequence);
        scenario.setProbe(probe);

        // Setting up synchronization mode
        scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
        scenario.resync();
        scenario.run();

        // Getting results
        Trajectory<EnvelopeProbeState> trajectory = probe.getTrajectory();

        EnvelopeProbeState ps = trajectory.stateAtPosition(0);
        Iterator<EnvelopeProbeState> iterState = trajectory.stateIterator();

        //BasicGraphData myDataX = new BasicGraphData();
        int i = 0;
        while (iterState.hasNext()) {
            ps = iterState.next();

            Twiss[] twiss = ps.twissParameters();

            PhaseVector mean = ps.phaseMean();

            System.out.printf("%E %E %E %E %E %E %E %E %E %E %E %E %E %E %E %E %E %E %s\n", ps.getPosition(), ps.getGamma() - 1,
                    twiss[0].getEnvelopeRadius(),
                    Math.sqrt(twiss[0].getGamma() * twiss[0].getEmittance()),
                    twiss[1].getEnvelopeRadius(),
                    Math.sqrt(twiss[1].getGamma() * twiss[1].getEmittance()),
                    twiss[2].getEnvelopeRadius() / ps.getGamma(),
                    Math.sqrt(twiss[2].getGamma() * twiss[2].getEmittance()) * ps.getGamma(),
                    Math.sqrt(twiss[2].getGamma() * twiss[2].getEmittance()) / ps.getGamma(),
                    mean.getx(),
                    mean.getxp(),
                    mean.gety(),
                    mean.getyp(),
                    mean.getz(),
                    mean.getzp(),
                    twiss[0].getBeta(),
                    twiss[1].getBeta(),
                    ps.getTime(),
                    ps.getElementId());

            i = i + 1;
        }
    }
    
    public static void loadInitialParameters(EnvelopeProbe probe, String file) {
        XmlDataAdaptor document = XmlDataAdaptor.adaptorForUrl(JElsDemo.class.getResource(file).toString(), false);
        EnvelopeProbeState state = new EnvelopeProbeState();
        state.load(document.childAdaptor("state"));
        probe.applyState(state);
    }

    public static EnvelopeProbe loadProbeFromXML(String file) {
        try {
            EnvelopeProbe probe = (EnvelopeProbe) ProbeXmlParser.parse(file);
            return probe;
        } catch (ParsingException e1) {
            LOGGER.log(Level.SEVERE, "Couldn''t load the probe from xml. {0}", e1.getMessage());
        }
        return null;
    }

    static void saveProbe(EnvelopeProbe probe, String file) {
        try {
            //probe.setSaveTwissFlag(true);
            ProbeXmlWriter.writeXml(probe, file);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Couldn''t load the probe from xml. {0}", e.getMessage());
        }
    }

    static void saveLattice(Lattice lattice, String file) {
        lattice.setAuthor(System.getProperty("user.name", "ESS"));
        try {
            LatticeXmlWriter.writeXml(lattice, file);
        } catch (IOException e1) {
            LOGGER.log(Level.SEVERE, "Couldn''t load the probe from xml. {0}", e1.getMessage());
        }
    }

    private static Accelerator loadAccelerator() {
        LOGGER.info("Loading accelerator from: " + XMLDataManager.defaultPath());
        /* Loading SMF model */
        Accelerator accelerator = XMLDataManager.loadDefaultAccelerator();

        if (accelerator == null) {
            throw new Error("Accelerator is empty. Could not load the default accelerator.");
        }
        return accelerator;
    }

    private static Accelerator loadAccelerator(String path) {
        /* Loading SMF model */
        System.out.println("Loading accelerator from: " + path);
        Accelerator accelerator = XMLDataManager.acceleratorWithUrlSpec(new File(path).toURI().toString());

        if (accelerator == null) {
            throw new Error("Accelerator is empty. Could not load the accelerator.");
        }
        return accelerator;
    }
}
