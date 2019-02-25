/*
 * Copyright (C) 2018 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.extension.jels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static xal.extension.jels.TestCommon.setupOpenXALProbe;
import xal.extension.jels.model.elem.JElsElementMapping;
import xal.extension.jels.model.elem.ThickMagFieldMap;
import xal.extension.jels.smf.ESSElementFactory;
import xal.extension.jels.smf.impl.MagFieldMap;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
@RunWith(Parameterized.class)
public class MagFieldMap3DTest extends SingleElementTest {

    public MagFieldMap3DTest(SingleElementTestData data) {
        super(data);
    }

    @Parameterized.Parameters(name = "Solenoid {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 352.21e6, current = 0;

        List<Object[]> tests = new ArrayList<>();

        // 0: basic test, E=75keV		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "ThinMagFieldMap";
                probe = setupOpenXALProbe(75e3, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = solenoid(.8, 0.3);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {-2.516205e-02, +2.138554e-01, +4.407461e-02, -3.224342e-01, +0.000000e+00, +0.000000e+00,},
                    {-1.424293e+00, -3.035165e-02, +2.148755e+00, +4.066776e-02, +0.000000e+00, +0.000000e+00,},
                    {-4.407461e-02, +3.224342e-01, -2.516205e-02, +2.138554e-01, +0.000000e+00, +0.000000e+00,},
                    {-2.148755e+00, -4.066776e-02, -1.424293e+00, -3.035165e-02, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +7.998721e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00,},};

                // TW correlation matrix
                TWGamma = 1.000079934;
                TWCorrelationMatrix = new double[][]{
                    {+7.817754e-06, -4.889759e-06, +1.698966e-06, +7.649438e-07, +4.863598e-26, +4.198239e-26,},
                    {-4.889759e-06, +3.989527e-05, +7.359725e-07, -7.983632e-06, +7.634988e-26, +6.590493e-26,},
                    {+1.698966e-06, +7.359725e-07, +9.258501e-06, -4.250924e-06, -1.552141e-26, -1.339802e-26,},
                    {+7.649438e-07, -7.983632e-06, -4.250924e-06, +3.317393e-05, +1.372236e-26, +1.184509e-26,},
                    {+4.863598e-26, +7.634988e-26, -1.552141e-26, +1.372236e-26, +5.988596e-05, +3.848152e-05,},
                    {+4.198239e-26, +6.590493e-26, -1.339802e-26, +1.184509e-26, +3.848152e-05, +3.321710e-05,},};

                TMerrTolerance = 4e-3;
                CMerrTolerance = 3e-3;
            }
        }});

        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "ThickMagFieldMap";
                probe = setupOpenXALProbe(75e3, frequency, current);
                elementMapping = new JElsElementMapping() {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        removeMap("mfm");
                        putMap("mfm", ThickMagFieldMap.class);
                    }
                };
                sequence = solenoid(.8, 0.3);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {-2.516205e-02, +2.138554e-01, +4.407461e-02, -3.224342e-01, +0.000000e+00, +0.000000e+00,},
                    {-1.424293e+00, -3.035165e-02, +2.148755e+00, +4.066776e-02, +0.000000e+00, +0.000000e+00,},
                    {-4.407461e-02, +3.224342e-01, -2.516205e-02, +2.138554e-01, +0.000000e+00, +0.000000e+00,},
                    {-2.148755e+00, -4.066776e-02, -1.424293e+00, -3.035165e-02, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +7.998721e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00,},};

                // TW correlation matrix
                TWGamma = 1.000079934;
                TWCorrelationMatrix = new double[][]{
                    {+7.817754e-06, -4.889759e-06, +1.698966e-06, +7.649438e-07, +4.863598e-26, +4.198239e-26,},
                    {-4.889759e-06, +3.989527e-05, +7.359725e-07, -7.983632e-06, +7.634988e-26, +6.590493e-26,},
                    {+1.698966e-06, +7.359725e-07, +9.258501e-06, -4.250924e-06, -1.552141e-26, -1.339802e-26,},
                    {+7.649438e-07, -7.983632e-06, -4.250924e-06, +3.317393e-05, +1.372236e-26, +1.184509e-26,},
                    {+4.863598e-26, +7.634988e-26, -1.552141e-26, +1.372236e-26, +5.988596e-05, +3.848152e-05,},
                    {+4.198239e-26, +6.590493e-26, -1.339802e-26, +1.184509e-26, +3.848152e-05, +3.321710e-05,},};

                TMerrTolerance = 4e-3;
                CMerrTolerance = 3e-3;
            }
        }});

        return tests;
    }

    private static AcceleratorSeq solenoid(double length, double fieldStrength) {

        String fieldFile = "Field_Maps/3D/LEBT_SOL_LBE_IFMIF_New_Cut.bsx";
        String fieldMapPath = JElsDemo.class.getResource(fieldFile).toString();
        fieldFile = fieldFile.substring(0, fieldFile.length() - 4);
        fieldMapPath = fieldMapPath.substring(0, fieldMapPath.indexOf(fieldFile));

        MagFieldMap solenoid = ESSElementFactory.createMagFieldMap("testSolenoid", length, fieldStrength,
                fieldMapPath, fieldFile, new ApertureBucket(), null, 0., 3, 2000);

        AcceleratorSeq sequence = new AcceleratorSeq("SolenoidTest");
        sequence.addNode(solenoid);
        sequence.setLength(length);
        sequence.setPosition(0);

        return sequence;
    }

}
