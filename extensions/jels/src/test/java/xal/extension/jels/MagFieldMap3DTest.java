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
import static xal.extension.jels.TestCommon.setupOpenXALProbe;
import xal.extension.jels.model.elem.JElsElementMapping;
import xal.extension.jels.model.elem.ThinMagFieldMap;
import xal.extension.jels.smf.ESSElementFactory;
import xal.extension.jels.smf.impl.MagFieldMap;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;

/**
 * This tests uses a cylindrical symmetric solenoid for the test that is
 * compared to results from an equivalent 2D solenoid field map in TraceWin.
 * This was done because TraceWin uses a quadratic fit to the field maps that
 * for 3D maps with non-monotonic longitudinal component, common in solenoids,
 * the field amplitude is distorted in the order of 1%.
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
                description = "Thin solenoid";
                probe = setupOpenXALProbe(75e3, frequency, current);
                elementMapping = new JElsElementMapping() {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        removeMap("mfm");
                        putMap("mfm", ThinMagFieldMap.class);
                    }
                };
                sequence = solenoid(.1, 2.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {-3.593230e-01, -2.238722e-02, -8.958204e-02, +5.365839e-02, +0.000000e+00, +0.000000e+00,},
                    {-1.547685e+01, -2.297360e+00, -2.585938e+01, +7.194981e-02, +0.000000e+00, +0.000000e+00,},
                    {+8.958204e-02, -5.365839e-02, -3.593230e-01, -2.238722e-02, +0.000000e+00, +0.000000e+00,},
                    {+2.585938e+01, -7.194981e-02, -1.547685e+01, -2.297360e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +9.998402e-02,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00,},};

                // TW correlation matrix
                TWGamma = 1.000079934;
                TWCorrelationMatrix = new double[][]{
                    {+7.388965e-07, +3.748138e-05, +6.778848e-08, -3.906824e-05, +0.000000e+00, +0.000000e+00,},
                    {+3.748138e-05, +5.923299e-03, +6.880694e-05, +1.155026e-03, +0.000000e+00, +0.000000e+00,},
                    {+6.778848e-08, +6.880694e-05, +1.164073e-06, +5.078568e-05, +0.000000e+00, +0.000000e+00,},
                    {-3.906824e-05, +1.155026e-03, +5.078568e-05, +4.897852e-03, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.229162e-05, +1.523327e-05,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.523327e-05, +3.321710e-05,},};

                TMerrTolerance = 2e-5;
            }
        }});

        // 0: basic test, E=75keV	
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Thick solenoid";
                probe = setupOpenXALProbe(75e3, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = solenoid(.1, 2.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {-3.593230e-01, -2.238722e-02, -8.958204e-02, +5.365839e-02, +0.000000e+00, +0.000000e+00,},
                    {-1.547685e+01, -2.297360e+00, -2.585938e+01, +7.194981e-02, +0.000000e+00, +0.000000e+00,},
                    {+8.958204e-02, -5.365839e-02, -3.593230e-01, -2.238722e-02, +0.000000e+00, +0.000000e+00,},
                    {+2.585938e+01, -7.194981e-02, -1.547685e+01, -2.297360e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +9.998402e-02,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00,},};

                // TW correlation matrix
                TWGamma = 1.000079934;
                TWCorrelationMatrix = new double[][]{
                    {+7.388965e-07, +3.748138e-05, +6.778848e-08, -3.906824e-05, +0.000000e+00, +0.000000e+00,},
                    {+3.748138e-05, +5.923299e-03, +6.880694e-05, +1.155026e-03, +0.000000e+00, +0.000000e+00,},
                    {+6.778848e-08, +6.880694e-05, +1.164073e-06, +5.078568e-05, +0.000000e+00, +0.000000e+00,},
                    {-3.906824e-05, +1.155026e-03, +5.078568e-05, +4.897852e-03, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.229162e-05, +1.523327e-05,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.523327e-05, +3.321710e-05,},};

                TMerrTolerance = 2e-5;
            }
        }});

        return tests;
    }

    private static AcceleratorSeq solenoid(double length, double fieldStrength) {

        String fieldFile = "Field_Maps/3D/solenoid_3D.bsx";
        String fieldMapPath = JElsDemo.class.getResource(fieldFile).toString();
        fieldFile = fieldFile.substring(0, fieldFile.length() - 4);
        fieldMapPath = fieldMapPath.substring(0, fieldMapPath.indexOf(fieldFile));

        MagFieldMap solenoid = ESSElementFactory.createMagFieldMap("testSolenoid", length, fieldStrength,
                fieldMapPath, fieldFile, new ApertureBucket(), null, 0., 3, 1000);

        AcceleratorSeq sequence = new AcceleratorSeq("SolenoidTest");
        sequence.addNode(solenoid);
        sequence.setLength(length);
        sequence.setPosition(0);

        return sequence;
    }
}
