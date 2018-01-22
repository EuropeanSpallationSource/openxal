/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.jels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static xal.extension.jels.TestCommon.setupOpenXALProbe;
import xal.extension.jels.model.elem.JElsElementMapping;
import static xal.extension.jels.smf.ESSElementFactory.createESSSolFieldMap;
import xal.extension.jels.smf.impl.ESSSolFieldMap;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
@RunWith(Parameterized.class)
public class SolFieldMapTest extends SingleElementTest {

    public SolFieldMapTest(SingleElementTestData data) {
        super(data);
    }

    @Parameterized.Parameters(name = "Solenoid {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 352.21e8, current = 0;

        List<Object[]> tests = new ArrayList<>();

        // 0: basic test, E=3MeV		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                probe = setupOpenXALProbe(75e3, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = solenoid(545.4e-3, 0.3);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.186199e-01, +1.579852e-01, +1.732125e-01, +3.022185e-01, +0.000000e+00, +0.000000e+00},
                    {-1.306736e+00, +6.647930e-02, -2.487399e+00, +1.878450e-01, +0.000000e+00, +0.000000e+00},
                    {-1.732125e-01, -3.022185e-01, +1.186199e-01, +1.579852e-01, +0.000000e+00, +0.000000e+00},
                    {+2.487399e+00, -1.878450e-01, -1.306736e+00, +6.647930e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +5.453128e-01},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00},};

                // TW correlation matrix
                TWGamma = 1.000079934;
                TWCorrelationMatrix = new double[][]{
                    {+5.750827e-06, +3.169803e-06, -1.620668e-06, +1.700632e-06, +0.000000e+00, +0.000000e+00},
                    {+3.169803e-06, +5.401210e-05, +2.041198e-06, +1.130697e-05, +0.000000e+00, +0.000000e+00},
                    {-1.620668e-06, +2.041198e-06, +7.866685e-06, +6.577584e-07, +0.000000e+00, +0.000000e+00},
                    {+1.700632e-06, +1.130697e-05, +6.577584e-07, +3.717891e-05, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.244679e-05, +3.002580e-05},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.002580e-05, +3.321710e-05}};

                TMerrTolerance = 2e-2;
                CMerrTolerance = 3e-1;
            }
        }});

        return tests;
    }

    private static AcceleratorSeq solenoid(double length, double fieldStrength) {

        String fieldFile = "Field_Maps/1D/sef2_545_4mm.bsz";
        String fieldMapPath = JElsDemo.class.getResource(fieldFile).toString();
        fieldFile = fieldFile.substring(0, fieldFile.length() - 4);
        fieldMapPath = fieldMapPath.substring(0, fieldMapPath.indexOf(fieldFile));

        ESSSolFieldMap solenoid = createESSSolFieldMap("testSolenoid", length, fieldStrength,
                fieldMapPath, fieldFile, new ApertureBucket(), null, 0);

        AcceleratorSeq sequence = new AcceleratorSeq("SolenoidTest");
        sequence.addNode(solenoid);
        sequence.setLength(length);
        sequence.setPosition(0);

        return sequence;
    }

}
