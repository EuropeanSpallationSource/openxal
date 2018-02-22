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
import static xal.extension.jels.smf.ESSElementFactory.createESSMagFieldMap3D;
import xal.extension.jels.smf.impl.ESSMagFieldMap3D;
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
        final double frequency = 352.21e8, current = 0;

        List<Object[]> tests = new ArrayList<>();

        // 0: basic test, E=75keV		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                probe = setupOpenXALProbe(75e3, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = solenoid(545.4e-3, 0.3);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+3.711488e-01, +2.396342e-01, -4.938824e-01, -3.235277e-01, +0.000000e+00, -1.801775e-20,},
                    {-2.159624e+00, -4.463207e-01, +1.574749e+00, -2.718368e-01, +0.000000e+00, -4.011616e-20,},
                    {+4.938824e-01, +3.235277e-01, +3.711488e-01, +2.396342e-01, +0.000000e+00, +1.551051e-20,},
                    {-1.574749e+00, +2.718368e-01, -2.159624e+00, -4.463207e-01, +0.000000e+00, +7.232521e-21,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +5.453128e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00,},};

                // TW correlation matrix
                TWGamma = 1.000079934;
                TWCorrelationMatrix = new double[][]{
                    {+1.323485e-05, -1.581888e-05, +8.204060e-07, +1.995334e-05, -5.409689e-25, -5.984660e-25,},
                    {-1.581888e-05, +5.354963e-05, -1.477663e-05, -1.176775e-05, -1.204120e-24, -1.332100e-24,},
                    {+8.204060e-07, -1.477663e-05, +1.375869e-05, -1.194482e-05, +4.657391e-25, +5.152403e-25,},
                    {+1.995334e-05, -1.176775e-05, -1.194482e-05, +6.282459e-05, +2.176352e-25, +2.407666e-25,},
                    {-5.409689e-25, -1.204120e-24, +4.657391e-25, +2.176352e-25, +4.244679e-05, +3.002580e-05,},
                    {-5.984660e-25, -1.332100e-24, +5.152403e-25, +2.407666e-25, +3.002580e-05, +3.321710e-05,},};

                TMerrTolerance = 2e-2;
                CMerrTolerance = 2e-2;
            }
        }});

        return tests;
    }

    private static AcceleratorSeq solenoid(double length, double fieldStrength) {

        String fieldFile = "Field_Maps/3D/LEBT_SOL_LBE_IFMIF_New_Cut.bsx";
        String fieldMapPath = JElsDemo.class.getResource(fieldFile).toString();
        fieldFile = fieldFile.substring(0, fieldFile.length() - 4);
        fieldMapPath = fieldMapPath.substring(0, fieldMapPath.indexOf(fieldFile));

        ESSMagFieldMap3D solenoid = createESSMagFieldMap3D("test3DSolenoid", length, fieldStrength,
                fieldMapPath, fieldFile, new ApertureBucket(), null, 0);

        AcceleratorSeq sequence = new AcceleratorSeq("SolenoidTest");
        sequence.addNode(solenoid);
        sequence.setLength(length);
        sequence.setPosition(0);

        return sequence;
    }

}
