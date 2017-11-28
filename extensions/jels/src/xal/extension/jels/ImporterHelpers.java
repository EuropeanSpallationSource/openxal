package xal.extension.jels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.data.DataAttribute;
import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;

public class ImporterHelpers {

    public static AcceleratorSeqCombo addDefaultComboSeq(Accelerator acc) {
        List<AcceleratorSeq> seqs = acc.getSequences();

        // Remove LEBT and RFQ from default combo sequence, because they are not 
        // fully supported by Open XAL model
        if (seqs.get(1).getId().equals("RFQ")) {
            seqs = seqs.subList(2, acc.getSequences().size());
        } else if (seqs.get(0).getId().equals("LEBT")) {
            seqs = seqs.subList(1, acc.getSequences().size());
        }

        String name;
        if (seqs.size() >= 2) {
            name = seqs.get(0).getId() + "-" + seqs.get(seqs.size() - 1).getId();
        } else {
            name = "ALL";
        }

        AcceleratorSeqCombo comboSeq = new AcceleratorSeqCombo(name, seqs);
        acc.addComboSequence(comboSeq);
        return comboSeq;
    }

    // TODO remove hardcoded probe initialisation
    public static EnvelopeProbe defaultProbe() {
        EnvelopeProbe probe = setupOpenXALProbe(); // OpenXAL probe & algorithm
        //EnvelopeProbe probe = setupElsProbe(); // ELS probe & algorithm

        // Setup of initial parameters
        //setupInitialParameters(probe);
        //loadInitialParameters(probe, "mebt-initial-state.xml");		
        return probe;
    }

    private static EnvelopeProbe setupOpenXALProbe() {
        EnvelopeTracker envelopeTracker = new EnvelopeTracker();

        envelopeTracker.setRfGapPhaseCalculation(true);
        envelopeTracker.setUseSpacecharge(true);
        envelopeTracker.setEmittanceGrowth(false);
        envelopeTracker.setStepSize(0.1);
        envelopeTracker.setProbeUpdatePolicy(Tracker.UPDATE_ALWAYS);

        EnvelopeProbe envelopeProbe = new EnvelopeProbe();
        envelopeProbe.setAlgorithm(envelopeTracker);

        return envelopeProbe;
    }

    public static void setupInitialParameters(EnvelopeProbe probe, double bunchFrequency, double beamCurrent, double kineticEnergy, PhaseVector vecCent, Twiss[] initialTwiss) {
        probe.setSpeciesCharge(1);
//        probe.setSpeciesRestEnergy(9.382720813E8);    // More accurate value
        probe.setSpeciesRestEnergy(9.38272029E8); // TraceWin value
        probe.setSpeciesName("PROTON");
        probe.setKineticEnergy(kineticEnergy); //energy in eV
        probe.setPosition(0.0);
        probe.setTime(0.0);
        probe.setBeamCurrent(beamCurrent);
        probe.setBunchFrequency(bunchFrequency * 1e6);

        double beta = probe.getBeta();
        double gamma = probe.getGamma();
        double beta_gamma = beta * gamma;

        // Convert from TraceWin coordinates (z,deltap/p) to Open XAL (z,z')
        Twiss oxalTwissX = new Twiss(initialTwiss[0].getAlpha(), initialTwiss[0].getBeta(), initialTwiss[0].getEmittance() / beta_gamma);
        Twiss oxalTwissY = new Twiss(initialTwiss[1].getAlpha(), initialTwiss[1].getBeta(), initialTwiss[1].getEmittance() / beta_gamma);
        Twiss oxalTwissZ = new Twiss(initialTwiss[2].getAlpha(), initialTwiss[2].getBeta(), initialTwiss[2].getEmittance() / (beta_gamma * gamma * gamma));

//        probe.initFromTwiss(oxalTwiss);
        CovarianceMatrix matCov;
        if (vecCent != null) {
            matCov = CovarianceMatrix.buildCovariance(oxalTwissX, oxalTwissY, oxalTwissZ, vecCent);
        } else {
            matCov = CovarianceMatrix.buildCovariance(oxalTwissX, oxalTwissY, oxalTwissZ);
        }

        probe.setCovariance(matCov);
    }

    public static List<EnvelopeProbeState> simulateInitialValues(EnvelopeProbe probe, AcceleratorSeqCombo seqCombo) throws ModelException {
        Scenario scenario = Scenario.newScenarioFor(seqCombo);//, elementMapping);		
        scenario.setProbe(probe);

        // Setting up synchronization mode
        scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
        scenario.resync();

        scenario.run();

        Trajectory<EnvelopeProbeState> trajectory = probe.getTrajectory();

        List<EnvelopeProbeState> initialValues = new ArrayList<>();

        for (AcceleratorSeq seq : seqCombo.getConstituents()) {
            EnvelopeProbeState state = trajectory.stateNearestPosition(seqCombo.getPosition(seq)).copy();
            state.setElementId(seq.getId());
            initialValues.add(state);
        }

        probe.reset();
        System.gc();

        return initialValues;
    }

    private static void addEnvTrackerAdapt(EditContext editContext) {
        DataTable tblEnvTrackerAdapt = new DataTable("EnvTrackerAdapt", Arrays.asList(new DataAttribute[]{
            new DataAttribute("name", String.class, true),
            new DataAttribute("initstep", Double.class, false, Double.toString(0.01)),
            new DataAttribute("maxstep", Double.class, false, Double.toString(0.0)),
            new DataAttribute("maxstepdriftpmq", Double.class, false, Double.toString(0.0)),
            new DataAttribute("errortol", Double.class, false, Double.toString(1e-5)),
            new DataAttribute("slack", Double.class, false, Double.toString(0.05)),
            new DataAttribute("norm", Integer.class, false, Integer.toString(0)),
            new DataAttribute("maxiter", Integer.class, false, Integer.toString(50)),
            new DataAttribute("order", Integer.class, false, Integer.toString(2))
        }));
        GenericRecord defaultRec = new GenericRecord(tblEnvTrackerAdapt);
        defaultRec.setValueForKey("default", Tracker.TBL_PRIM_KEY_NAME);
        tblEnvTrackerAdapt.add(defaultRec);
        editContext.addTableToGroup(tblEnvTrackerAdapt, "modelparams");
    }

    public static void addHardcodedInitialParameters(Accelerator accelerator) {
        double beamCurrent = 62.5e-3;
        double bunchFrequency = 352.21;
        double kineticEnergy = 3.6217853e6;
        Twiss[] initialTwiss = new Twiss[]{new Twiss(-0.051805615, 0.20954703, 0.25288 * 1e-6),
            new Twiss(-0.30984478, 0.37074849, 0.251694 * 1e-6),
            new Twiss(-0.48130325, 0.92564505, 0.3615731 * 1e-6)};
        addInitialParameters(accelerator, beamCurrent, bunchFrequency, kineticEnergy, null, initialTwiss);
    }

    public static void addInitialParameters(Accelerator accelerator, double bunchFrequency, double beamCurrent, double kineticEnergy, PhaseVector initialCentroid, Twiss[] initialTwiss) {
        if (accelerator != null) {
            AcceleratorSeqCombo comboSeq = addDefaultComboSeq(accelerator);

            EditContext editContext = new EditContext();

            EnvelopeProbe probe = defaultProbe();
            setupInitialParameters(probe, bunchFrequency, beamCurrent, kineticEnergy, initialCentroid, initialTwiss);
            ProbeFactory.createSchema(editContext, probe);

            // Adding hardcoded EnvTrackerAdapt table, which is not created in createSchema()
            addEnvTrackerAdapt(editContext);

            // Creating schema for Centroid position
            DataTable tblCentroid = addCentroidSchema(editContext);

            accelerator.setEditContext(editContext);
            try {
                List<EnvelopeProbeState> states = simulateInitialValues(probe, comboSeq);
                ProbeFactory.storeInitialValues(editContext, states);

                if (initialCentroid != null) {
                    for (EnvelopeProbeState state : states) {
                        addCentroidToTable(state.getElementId(), state.phaseMean(), tblCentroid);
                    }
                }

            } catch (ModelException e) {
                System.err.println("Unable to simulate initial states on sequences. Only setting the first sequence.");
                List<EnvelopeProbeState> states = Arrays.asList(probe.cloneCurrentProbeState());
                states.get(0).setElementId(comboSeq.getConstituents().get(0).getId());
                ProbeFactory.storeInitialValues(editContext, states);
            }
        }
    }

    public static void addAllInitialParameters(Accelerator accelerator, List<Double> bunchFrequency, List<Double> beamCurrent, List<Double> kineticEnergy, List<PhaseVector> initialCentroid, List<Twiss[]> initialTwiss) {
        if (accelerator != null) {
            AcceleratorSeqCombo comboSeq = addDefaultComboSeq(accelerator);

            EditContext editContext = new EditContext();

            EnvelopeProbe probe = defaultProbe();

            // TODO: beam current taken from MEBT. In future, each sequence should have its own current
            setupInitialParameters(probe, bunchFrequency.get(0), beamCurrent.get(2), kineticEnergy.get(0), initialCentroid.get(0), initialTwiss.get(0));

            ProbeFactory.createSchema(editContext, probe);

            // Adding hardcoded EnvTrackerAdapt table, which is not created in createSchema()
            addEnvTrackerAdapt(editContext);
            int i = 0;
            double beta;
            double gamma;
            double beta_gamma;
            Twiss[] auxTwiss;

            // Creating schema for Centroid position
            DataTable tblCentroid = addCentroidSchema(editContext);

            DataTable tblTwiss = editContext.getTable("twiss");
            DataTable tblLocation = editContext.getTable("location");
            GenericRecord record = null;
            for (AcceleratorSeq seq : accelerator.getSequences()) {

                gamma = 1 + kineticEnergy.get(i) / probe.getSpeciesRestEnergy();
                beta = Math.sqrt(1 - 1 / gamma / gamma);
                beta_gamma = beta * gamma;

                auxTwiss = initialTwiss.get(i);
                // Convert from TraceWin coordinates (z,deltap/p) to Open XAL (z,z')
                Twiss[] oxalTwiss = new Twiss[]{
                    new Twiss(auxTwiss[0].getAlpha(), auxTwiss[0].getBeta(), auxTwiss[0].getEmittance() / beta_gamma),
                    new Twiss(auxTwiss[1].getAlpha(), auxTwiss[1].getBeta(), auxTwiss[1].getEmittance() / beta_gamma),
                    new Twiss(auxTwiss[2].getAlpha(), auxTwiss[2].getBeta(), auxTwiss[2].getEmittance() / (beta_gamma * gamma * gamma))};

                addTwissToTable(seq.getId(), oxalTwiss, tblTwiss);

                // Adding centroid coordinates
                addCentroidToTable(seq.getId(), initialCentroid.get(i), tblCentroid);

                record = new GenericRecord(tblLocation);
                record.setValueForKey(seq.getId(), "name");
                record.setValueForKey(kineticEnergy.get(i), "W");
                tblLocation.add(record);

                i++;
            }

            accelerator.setEditContext(editContext);
        }
    }

    /*
     * Adds a centroid table to the context
     */
    private static DataTable addCentroidSchema(EditContext editContext) {
        DataTable tblCentroid = new DataTable("CentroidCoordinates", Arrays.asList(new DataAttribute[]{
            new DataAttribute("name", String.class, true),
            new DataAttribute("coordinates", String.class, true)
        }));

        editContext.addTableToGroup(tblCentroid, "modelparams");
        
        return tblCentroid;
    }

    private static void addTwissToTable(String seq, Twiss[] twiss, DataTable tblTwiss) {
        for (int i = 0; i < 3; i++) {
            String axis = new String[]{"x", "y", "z"}[i];
            GenericRecord record = new GenericRecord(tblTwiss);
            record.setValueForKey(seq, "name");
            record.setValueForKey(axis, "coordinate");
            record.setValueForKey(twiss[i].getAlpha(), "alpha");
            record.setValueForKey(twiss[i].getBeta(), "beta");
            record.setValueForKey(twiss[i].getEmittance(), "emittance");
            tblTwiss.add(record);
        }

    }

    /*
     * Adds a centroid record for a given sequence to the "CentroidCoordinates" table
     */
    private static void addCentroidToTable(String seqName, PhaseVector initialCentroid, DataTable tblCentroid) {
        GenericRecord record = new GenericRecord(tblCentroid);
        record.setValueForKey(seqName, "name");
        record.setValueForKey("(" + initialCentroid.printString() + ")", "coordinates");
        tblCentroid.add(record);
    }

    /**
     * Cleans up XML OpenXal produces
     *
     * @param parent node to clean
     */
    public static void xmlCleanup(Node parent) {
        NodeList children = parent.getChildNodes();
        NamedNodeMap attrs = parent.getAttributes();
        if (attrs != null) {
            // unneeded attributes 
            if (attrs.getNamedItem("pid") != null) {
                attrs.removeNamedItem("pid");
            }
            if (attrs.getNamedItem("status") != null) {
                attrs.removeNamedItem("status");
            }
            if (attrs.getNamedItem("eid") != null) {
                attrs.removeNamedItem("eid");
            }

            // remove type="sequence" on sequences - import doesn't work otherwise
            if ("sequence".equals(parent.getNodeName()) && attrs.getNamedItem("type") != null
                    && "sequence".equals(attrs.getNamedItem("type").getNodeValue())) {
                attrs.removeNamedItem("type");
            }

            if ("xdxf".equals(parent.getNodeName())) {
                attrs.removeNamedItem("s");
                attrs.removeNamedItem("id");
                attrs.removeNamedItem("len");
                attrs.removeNamedItem("pos");
                attrs.removeNamedItem("type");
            }
        }

        for (int i = 0; i < children.getLength();) {
            Node child = children.item(i);
            attrs = child.getAttributes();

            if ("align".equals(child.getNodeName()) || "twiss".equals(child.getNodeName())) // remove twiss and align - not needed
            {
                parent.removeChild(child);
            } else if ("channelsuite".equals(child.getNodeName()) && !child.hasChildNodes()) {
                parent.removeChild(child);
            } else if ("aperture".equals(child.getNodeName()) && "0.0".equals(attrs.getNamedItem("x").getNodeValue())) // remove empty apertures
            {
                parent.removeChild(child);
            } else {
                xmlCleanup(child);
                // remove empty attributes
                if ("attributes".equals(child.getNodeName()) && child.getChildNodes().getLength() == 0) {
                    parent.removeChild(child);
                } else {
                    i++;
                }
            }
        }
    }
}
