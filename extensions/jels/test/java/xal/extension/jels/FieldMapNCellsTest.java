package xal.extension.jels;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import xal.extension.jels.model.elem.FieldMap;
import xal.extension.jels.model.elem.FieldMapExpInt;
import xal.extension.jels.model.elem.FieldMapNCells;
import xal.extension.jels.model.elem.JElsElementMapping;
import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;

public class FieldMapNCellsTest {

    @Test
    public void fieldMapNCellsTest() throws ModelException, IOException {
        Accelerator acc = XMLDataManager.acceleratorWithUrlSpec(JElsDemo.class.getResource("lattice1/main.xal").toString());

        for (int i = 0; i < 2; i++) {
            Probe probe = GeneralTest.loadProbeFromXML(JElsDemo.class.getResource("lattice1/probe." + i + ".xml").toString());
            probe.initialize();

            acc.getAccelerator().setElementMapping(new JElsElementMapping() {
                @Override
                protected void initialize() {
                    putMap("fm", FieldMap.class);
                    super.initialize();
                }
            });

            double dataOXFM[][] = GeneralTest.run(probe, acc.getComboSequence("from-mebt"));
            //GeneralTest.saveResults("resultfm"+i, dataOXFM);
            probe.reset();

            acc.getAccelerator().setElementMapping(new JElsElementMapping() {
                @Override
                protected void initialize() {
                    putMap("fm", FieldMapNCells.class);
                    super.initialize();
                }
            });
            double dataOXNC[][] = GeneralTest.run(probe, acc.getComboSequence("from-mebt"));
            //GeneralTest.saveResults("resultnc"+i, dataOXNC);
            probe.reset();

            // Test also first order integrator against the exponential one
            acc.getAccelerator().setElementMapping(new JElsElementMapping() {
                @Override
                protected void initialize() {
                    putMap("fm", FieldMapExpInt.class);
                    super.initialize();
                }
            });
            double dataOXEI[][] = GeneralTest.run(probe, acc.getComboSequence("from-mebt"));
            probe.reset();

            System.out.printf("%s\t", probe.getComment());
            StringBuilder message = new StringBuilder();
            boolean ok = true;
            for (int j = 1; j < dataOXFM.length; j++) {
                double e = GeneralTest.compare(dataOXFM[0], dataOXNC[0], dataOXFM[j], dataOXNC[j]);
                //System.out.printf("%s: %E %c %E\n",allCols[j].name(), e, e < allCols[j].allowedError ? '<' : '>', allCols[j].allowedError);
                System.out.printf("%E\t", e);
                if (e >= 0.055) {
                    message.append(j).append(" ");
                    ok = false;
                }
                
                e = GeneralTest.compare(dataOXFM[0], dataOXEI[0], dataOXFM[j], dataOXEI[j]);
                System.out.printf("%E\t", e);
                if (e >= 1e-3) {
                    message.append(j).append(" ");
                    ok = false;
                }
                //System.out.printf("%E %E\n",dataOX[allCols[j].openxal][0], dataTW[allCols[j].tracewin][0]);
            }
            System.out.println();
            assertTrue(message.append("are not within the allowed error").toString(), ok);
        }
    }
}
