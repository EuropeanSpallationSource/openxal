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
import xal.extension.jels.model.elem.ThinMagFieldMap;
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
                probe = setupOpenXALProbe(74.671738e3, frequency, current);
                elementMapping = new JElsElementMapping() {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        removeMap("mfm");
                        putMap("mfm", ThinMagFieldMap.class);
                    }
                };
                sequence = lebtsolenoid(545.4e-3, 0.3);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.133312e-01, +1.569380e-01, +1.662813e-01, +3.018654e-01, +0.000000e+00, +0.000000e+00,},
                    {-1.304882e+00, +6.835073e-02, -2.499937e+00, +1.896907e-01, +0.000000e+00, +0.000000e+00,},
                    {-1.662813e-01, -3.018654e-01, +1.133312e-01, +1.569380e-01, +0.000000e+00, +0.000000e+00,},
                    {+2.499937e+00, -1.896907e-01, -1.304882e+00, +6.835073e-02, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +5.453132e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00,},};

                // TW correlation matrix
                TWGamma = 1.000079934;
                TWCorrelationMatrix = new double[][]{
                    {+6.809041e-06, -4.414412e-06, -9.468929e-07, -2.388969e-06, +0.000000e+00, +0.000000e+00,},
                    {-4.414412e-06, +4.443702e-05, -1.935344e-06, +6.901005e-06, +0.000000e+00, +0.000000e+00,},
                    {-9.468929e-07, -1.935344e-06, +8.216058e-06, -1.327366e-06, +0.000000e+00, +0.000000e+00,},
                    {-2.388969e-06, +6.901005e-06, -1.327366e-06, +3.559489e-05, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.254002e-05, +3.009173e-05,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.009173e-05, +3.329001e-05,},};
            }
        }});

        // 1: Thick Solenoid
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "ThickSolenoid";
                probe = setupOpenXALProbe(74.671738e3, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = lebtsolenoid(545.4e-3, 0.3);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.133312e-01, +1.569380e-01, +1.662813e-01, +3.018654e-01, +0.000000e+00, +0.000000e+00,},
                    {-1.304882e+00, +6.835073e-02, -2.499937e+00, +1.896907e-01, +0.000000e+00, +0.000000e+00,},
                    {-1.662813e-01, -3.018654e-01, +1.133312e-01, +1.569380e-01, +0.000000e+00, +0.000000e+00,},
                    {+2.499937e+00, -1.896907e-01, -1.304882e+00, +6.835073e-02, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +5.453132e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00,},};

                // TW correlation matrix
                TWGamma = 1.000079934;
                TWCorrelationMatrix = new double[][]{
                    {+6.809041e-06, -4.414412e-06, -9.468929e-07, -2.388969e-06, +0.000000e+00, +0.000000e+00,},
                    {-4.414412e-06, +4.443702e-05, -1.935344e-06, +6.901005e-06, +0.000000e+00, +0.000000e+00,},
                    {-9.468929e-07, -1.935344e-06, +8.216058e-06, -1.327366e-06, +0.000000e+00, +0.000000e+00,},
                    {-2.388969e-06, +6.901005e-06, -1.327366e-06, +3.559489e-05, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.254002e-05, +3.009173e-05,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.009173e-05, +3.329001e-05,},};
            }
        }});
        return tests;
    }

    private static AcceleratorSeq lebtsolenoid(double length, double fieldStrength) {
        return lebtsolenoid(length, fieldStrength, 0., 0., 0., 0., 0.);
    }

    private static AcceleratorSeq lebtsolenoid(double length, double fieldStrength, double dx, double dy, double pitch, double yaw, double roll) {

        String fieldFile = "Field_Maps/1D/LEBT_sol_fixed.bsz";
        String fieldMapPath = JElsDemo.class.getResource(fieldFile).toString();
        fieldFile = fieldFile.substring(0, fieldFile.length() - 4);
        fieldMapPath = fieldMapPath.substring(0, fieldMapPath.indexOf(fieldFile));

        MagFieldMap solenoid = ESSElementFactory.createMagFieldMap("testSolenoid", length, fieldStrength,
                fieldMapPath, fieldFile, new ApertureBucket(), null, 0., 2, 1000);

        solenoid.setXOffset(dx);
        solenoid.setYOffset(dy);
        solenoid.setPitchAngle(pitch);
        solenoid.setYawAngle(yaw);
        solenoid.setRollAngle(roll);

        AcceleratorSeq sequence = new AcceleratorSeq("SolenoidTest");
        sequence.addNode(solenoid);
        sequence.setLength(length);
        sequence.setPosition(0);

        return sequence;
    }
}
