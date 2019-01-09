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
import org.junit.runners.Parameterized.Parameters;

import xal.extension.jels.model.elem.JElsElementMapping;
import xal.extension.jels.model.elem.ThinRfFieldMap;
import xal.extension.jels.smf.impl.FieldMap;
import xal.extension.jels.smf.impl.FieldMapFactory;
import xal.extension.jels.smf.impl.RfFieldMap;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Marker;
import xal.smf.impl.RfCavity;

@RunWith(Parameterized.class)
public class RfFieldMapTest extends SingleElementTest {

    public RfFieldMapTest(SingleElementTestData data) {
        super(data);
    }

    @Parameters(name = "FieldMap {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 352.21e6, current = 0;

        List<Object[]> tests = new ArrayList<>();

        // 0: thin rf fieldmap
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                probe = setupOpenXALProbe(89.88782e6, frequency, current,
                        new double[][]{{0., 1., 0.1},
                        {0., 1., 0.1},
                        {0., 1., 0.1}});

                elementMapping = new JElsElementMapping() {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        removeMap("rfm");
                        putMap("rfm", ThinRfFieldMap.class);
                    }
                };
//                elementMapping = JElsElementMapping.getInstance();
                sequence = fieldMap(0.988, frequency * 1e-6, 7.470983655095271, -1.10293);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073432e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330528e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073432e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330528e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.542148e-01, +7.691931e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.069545e-01, +8.706342e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.833516e-07, +2.649979e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+2.649979e-07, +2.442098e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +4.833516e-07, +2.649979e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.649979e-07, +2.442098e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.941681e-07, +1.307303e-07,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.307303e-07, +2.206415e-07,},};

                TMerrTolerance = 5e-3;
                CMerrTolerance = 5e-3;
            }
        }});

        // 1: thick rf fieldmap	
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                probe = setupOpenXALProbe(89.88782e6, frequency, current,
                        new double[][]{{0., 1., 0.1},
                        {0., 1., 0.1},
                        {0., 1., 0.1}});

                elementMapping = JElsElementMapping.getInstance();
                sequence = fieldMap(0.988, frequency * 1e-6, 7.470983655095271, -1.10293);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073432e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330528e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073432e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330528e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.542148e-01, +7.691931e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.069545e-01, +8.706342e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.833516e-07, +2.649979e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+2.649979e-07, +2.442098e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +4.833516e-07, +2.649979e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.649979e-07, +2.442098e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.941681e-07, +1.307303e-07,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.307303e-07, +2.206415e-07,},};

                TMerrTolerance = 5e-3;
                CMerrTolerance = 5e-3;
            }
        }});

        return tests;
    }

    /**
     *
     * @return sequence with fieldmap
     */
    public static AcceleratorSeq fieldMap(double length, double frequency, double cavAmp, double cavPh) {
        AcceleratorSeq sequence = new AcceleratorSeq("ThinRfFieldMapTest");

        String fieldFile = "Field_Maps/1D/Spoke_W_coupler.edz";
        String fieldMapPath = JElsDemo.class.getResource(fieldFile).toString();
        fieldFile = fieldFile.substring(0, fieldFile.length() - 4);
        fieldMapPath = fieldMapPath.substring(0, fieldMapPath.indexOf(fieldFile));

        RfFieldMap rfFieldMap = new RfFieldMap("TestFM");
        rfFieldMap.setFieldMapFile(fieldFile);

        FieldMap fieldMap = FieldMapFactory.getInstance(fieldMapPath,
                fieldFile, true, FieldMapFactory.FieldType.ELECTRIC, 1);
        rfFieldMap.setFieldMap(fieldMap);

        rfFieldMap.setLength(length);
        rfFieldMap.setPosition(0.5 * length);

        RfCavity rfCavity = new RfCavity("Cav");
        rfCavity.getRfField().setFrequency(frequency);
        rfCavity.addNode(rfFieldMap);
        rfCavity.setLength(length);
        rfCavity.setPosition(0);

        rfCavity.setDfltCavAmp(cavAmp);
        rfCavity.setDfltCavPhase(cavPh);

        sequence.addNode(rfCavity);

        sequence.setLength(length);
        
        // Adding a marker in the middle to test splitting the ThickElement in 2
        Marker marker = new Marker("Marker");
        marker.setPosition(0.5*length);
        sequence.addNode(marker);
        
        return sequence;
    }

}
