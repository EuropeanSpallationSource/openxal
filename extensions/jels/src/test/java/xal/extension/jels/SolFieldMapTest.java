/*
 * Copyright (C) 2019 European Spallation Source ERIC.
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
public class SolFieldMapTest extends SingleElementTest {

    public SolFieldMapTest(SingleElementTestData data) {
        super(data);
    }

    @Parameterized.Parameters(name = "Solenoid {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 352.21e6, current = 0;

        List<Object[]> tests = new ArrayList<>();

        // 0: Thin Solenoid
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "ThinSolenoid";
                probe = setupOpenXALProbe(75e3, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = solenoid(545.4e-3, 0.3);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.218586e-01, +1.580233e-01, +1.675747e-01, +3.021908e-01, +0.000000e+00, +0.000000e+00,},
                    {-1.304874e+00, +6.535132e-02, -2.487216e+00, +1.930515e-01, +0.000000e+00, +0.000000e+00,},
                    {-1.671618e-01, -3.022981e-01, +1.200345e-01, +1.579630e-01, +0.000000e+00, +0.000000e+00,},
                    {+2.486012e+00, -1.936698e-01, -1.306677e+00, +6.494965e-02, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +5.453128e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00,},};

                // TW correlation matrix
                TWGamma = 1.000079934;
                TWCorrelationMatrix = new double[][]{
                    {+6.851458e-06, -4.418405e-06, -9.493283e-07, -2.434459e-06, +0.000000e+00, +0.000000e+00,},
                    {-4.418405e-06, +4.391279e-05, -1.922774e-06, +6.897122e-06, +0.000000e+00, +0.000000e+00,},
                    {-9.493283e-07, -1.922774e-06, +8.257284e-06, -1.311169e-06, +0.000000e+00, +0.000000e+00,},
                    {-2.434459e-06, +6.897122e-06, -1.311169e-06, +3.535640e-05, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.244679e-05, +3.002580e-05,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.002580e-05, +3.321710e-05,},};

                TMerrTolerance = 4e-3;
                CMerrTolerance = 4e-3;
            }
        }});

        // 1: Thick Solenoid
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "ThickSolenoid";
                probe = setupOpenXALProbe(75e3, frequency, current);
                elementMapping = new JElsElementMapping() {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        removeMap("mfm");
                        putMap("mfm", ThickMagFieldMap.class);
                    }
                };
                sequence = solenoid(545.4e-3, 0.3);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.218586e-01, +1.580233e-01, +1.675747e-01, +3.021908e-01, +0.000000e+00, +0.000000e+00,},
                    {-1.304874e+00, +6.535132e-02, -2.487216e+00, +1.930515e-01, +0.000000e+00, +0.000000e+00,},
                    {-1.671618e-01, -3.022981e-01, +1.200345e-01, +1.579630e-01, +0.000000e+00, +0.000000e+00,},
                    {+2.486012e+00, -1.936698e-01, -1.306677e+00, +6.494965e-02, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +5.453128e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00,},};

                // TW correlation matrix
                TWGamma = 1.000079934;
                TWCorrelationMatrix = new double[][]{
                    {+6.851458e-06, -4.418405e-06, -9.493283e-07, -2.434459e-06, +0.000000e+00, +0.000000e+00,},
                    {-4.418405e-06, +4.391279e-05, -1.922774e-06, +6.897122e-06, +0.000000e+00, +0.000000e+00,},
                    {-9.493283e-07, -1.922774e-06, +8.257284e-06, -1.311169e-06, +0.000000e+00, +0.000000e+00,},
                    {-2.434459e-06, +6.897122e-06, -1.311169e-06, +3.535640e-05, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.244679e-05, +3.002580e-05,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.002580e-05, +3.321710e-05,},};

                TMerrTolerance = 4e-3;
                CMerrTolerance = 4e-3;
            }
        }});

        return tests;
    }

    private static AcceleratorSeq solenoid(double length, double fieldStrength) {

        String fieldFile = "Field_Maps/1D/sef2_545_4mm.bsz";
        String fieldMapPath = JElsDemo.class.getResource(fieldFile).toString();
        fieldFile = fieldFile.substring(0, fieldFile.length() - 4);
        fieldMapPath = fieldMapPath.substring(0, fieldMapPath.indexOf(fieldFile));

        MagFieldMap solenoid = ESSElementFactory.createMagFieldMap("testSolenoid", length, fieldStrength,
                fieldMapPath, fieldFile, new ApertureBucket(), null, 0., 2, 2000);

        AcceleratorSeq sequence = new AcceleratorSeq("SolenoidTest");
        sequence.addNode(solenoid);
        sequence.setLength(length);
        sequence.setPosition(0);

        return sequence;
    }

}
