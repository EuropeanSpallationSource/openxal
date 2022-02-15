package xal.extension.jels;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;

@RunWith(JUnit4.class)
public class ScenarioGeneratorTest {

    private static final Logger LOGGER = Logger.getLogger(ScenarioGeneratorTest.class.getName());

    @Test
    public void doScenarioGeneratorTest() throws InstantiationException, ModelException {
        LOGGER.log(Level.INFO, "Running\n");

        Accelerator accelerator = loadAccelerator();

        for (AcceleratorSeq sequence : accelerator.getSequences()) {
            // Generates lattice from SMF accelerator
            Scenario escenario = Scenario.newScenarioFor(sequence);

            // Ensure files
            new File("temp/old").mkdirs();
            new File("temp/new").mkdirs();

            // Outputting lattice elements
            saveLattice(escenario.getLattice(), "temp/new/lattice-" + sequence.getId() + ".xml");
        }
    }

    private static void saveLattice(Lattice lattice, String file) {
        try {
            LatticeXmlWriter.writeXml(lattice, file);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
            return;
        }
    }

    private static Accelerator loadAccelerator() {
        /* Loading SMF model */
        Accelerator accelerator = null;
        try {
            accelerator = XMLDataManager.acceleratorWithUrlSpec(JElsDemo.class.getResource("main.xal").toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
        if (accelerator == null) {
            throw new Error("Accelerator is empty. Could not load the default accelerator.");
        }

        return accelerator;

        /* We can instead build lattice for the whole accelerator */
    }
}
