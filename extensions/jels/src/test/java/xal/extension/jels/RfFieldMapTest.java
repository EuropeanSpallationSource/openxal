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
import xal.extension.jels.smf.ESSElementFactory;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;
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
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThinRfFieldMap";
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

                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293);

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
            }
        }});

        // 1: thick rf fieldmap	
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThickRfFieldMap";
                probe = setupOpenXALProbe(89.88782e6, frequency, current,
                        new double[][]{{0., 1., 0.1},
                        {0., 1., 0.1},
                        {0., 1., 0.1}});

                elementMapping = JElsElementMapping.getInstance();
                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293);

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
            }
        }});

        // FIELD_MAP 100 1500 66.7114 80 0 0.99941 0 1 HB_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "HB ThinRfFieldMap";
                probe = setupOpenXALProbe(570.96562e6, frequency, current,
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
                sequence = hbFieldMap(1.5, 2 * frequency * 1e-6, 0.99941, 66.7114);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.013098e+00, +1.496423e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.329909e-02, +9.926431e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.013098e+00, +1.496423e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.329909e-02, +9.926431e-01, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.653929e-01, +5.660853e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -6.881287e-02, +9.807304e-01,},};

                // TW correlation matrix
                TWGamma = 1.622840207;
                TWCorrelationMatrix = new double[][]{
                    {+2.591978e-07, +1.189681e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.189681e-07, +7.822144e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.591978e-07, +1.189681e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.189681e-07, +7.822144e-08, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.439852e-08, +1.119742e-07,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.119742e-07, +1.976687e-07,},};
            }
        }});

        // FIELD_MAP 100 1500 66.7114 80 0 0.99941 0 1 HB_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "HB ThickRfFieldMap";
                probe = setupOpenXALProbe(570.96562e6, frequency, current,
                        new double[][]{{0., 1., 0.1},
                        {0., 1., 0.1},
                        {0., 1., 0.1}});

                elementMapping = JElsElementMapping.getInstance();
                sequence = hbFieldMap(1.5, 2 * frequency * 1e-6, 0.99941, 66.7114);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.013098e+00, +1.496423e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.329909e-02, +9.926431e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.013098e+00, +1.496423e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.329909e-02, +9.926431e-01, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.653929e-01, +5.660853e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -6.881287e-02, +9.807304e-01,},};

                // TW correlation matrix
                TWGamma = 1.622840207;
                TWCorrelationMatrix = new double[][]{
                    {+2.591978e-07, +1.189681e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.189681e-07, +7.822144e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.591978e-07, +1.189681e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.189681e-07, +7.822144e-08, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.439852e-08, +1.119742e-07,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.119742e-07, +1.976687e-07,},};
            }
        }});

        // Misalignments
        // dx thin rf fieldmap
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThinRfFieldMap dx=1mm";
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

                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 1e-3, 0., 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +3.192074e-10, -3.407503e-11,},
                    {+1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +3.409934e-10, -3.624753e-11,},
                    {+0.000000e+00, +0.000000e+00, +1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +1.817597e-11, +0.000000e+00, +0.000000e+00, +8.542149e-01, +7.691930e-01,},
                    {+0.000000e+00, +2.146254e-11, +0.000000e+00, +0.000000e+00, -3.069547e-01, +8.706337e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.833522e-07, +2.649989e-07, +0.000000e+00, +0.000000e+00, -2.797349e-10, -3.225544e-11,},
                    {+2.649989e-07, +2.442115e-07, +0.000000e+00, +0.000000e+00, -6.239351e-10, -8.340346e-11,},
                    {+0.000000e+00, +0.000000e+00, +4.833518e-07, +2.649982e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.649982e-07, +2.442099e-07, +0.000000e+00, +0.000000e+00,},
                    {-2.797349e-10, -6.239351e-10, +0.000000e+00, +0.000000e+00, +2.941682e-07, +1.307303e-07,},
                    {-3.225544e-11, -8.340346e-11, +0.000000e+00, +0.000000e+00, +1.307303e-07, +2.206415e-07,},};

                TWMean = new double[]{-0.0734326, -0.133054, 0.0, 0.0, -1.01579e-06, 6.42917e-06, 1.0};
                CMerrTolerance = 2e-3;
            }
        }});

        // dx thick rf fieldmap	
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThickRfFieldMap dx=1mm";
                probe = setupOpenXALProbe(89.88782e6, frequency, current,
                        new double[][]{{0., 1., 0.1},
                        {0., 1., 0.1},
                        {0., 1., 0.1}});

                elementMapping = JElsElementMapping.getInstance();
                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 1e-3, 0., 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +3.192074e-10, -3.407503e-11,},
                    {+1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +3.409934e-10, -3.624753e-11,},
                    {+0.000000e+00, +0.000000e+00, +1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +1.817597e-11, +0.000000e+00, +0.000000e+00, +8.542149e-01, +7.691930e-01,},
                    {+0.000000e+00, +2.146254e-11, +0.000000e+00, +0.000000e+00, -3.069547e-01, +8.706337e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.833522e-07, +2.649989e-07, +0.000000e+00, +0.000000e+00, -2.797349e-10, -3.225544e-11,},
                    {+2.649989e-07, +2.442115e-07, +0.000000e+00, +0.000000e+00, -6.239351e-10, -8.340346e-11,},
                    {+0.000000e+00, +0.000000e+00, +4.833518e-07, +2.649982e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.649982e-07, +2.442099e-07, +0.000000e+00, +0.000000e+00,},
                    {-2.797349e-10, -6.239351e-10, +0.000000e+00, +0.000000e+00, +2.941682e-07, +1.307303e-07,},
                    {-3.225544e-11, -8.340346e-11, +0.000000e+00, +0.000000e+00, +1.307303e-07, +2.206415e-07,},};

                TWMean = new double[]{-0.0734326, -0.133054, 0.0, 0.0, -1.01579e-06, 6.42917e-06, 1.0};
                CMerrTolerance = 2e-3;
            }
        }});

        // dy thin rf fieldmap
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThinRfFieldMap dy=1mm";
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

                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 0., 1e-3, 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073433e+00, +1.006764e+00, +3.192074e-10, -3.407503e-11,},
                    {+0.000000e+00, +0.000000e+00, +1.330536e-01, +1.037577e+00, +3.409934e-10, -3.624753e-11,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +1.817597e-11, +8.542149e-01, +7.691930e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +2.146254e-11, -3.069547e-01, +8.706337e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.833518e-07, +2.649982e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+2.649982e-07, +2.442099e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +4.833522e-07, +2.649989e-07, -2.797349e-10, -3.225544e-11,},
                    {+0.000000e+00, +0.000000e+00, +2.649989e-07, +2.442115e-07, -6.239351e-10, -8.340346e-11,},
                    {+0.000000e+00, +0.000000e+00, -2.797349e-10, -6.239351e-10, +2.941682e-07, +1.307303e-07,},
                    {+0.000000e+00, +0.000000e+00, -3.225544e-11, -8.340346e-11, +1.307303e-07, +2.206415e-07,},};

                TWMean = new double[]{0.0, 0.0, -0.0734326, -0.133054, -1.01579e-06, 6.42917e-06, 1.0};
                CMerrTolerance = 2e-3;
            }
        }});

        // dy thick rf fieldmap	
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThickRfFieldMap dy=1mm";
                probe = setupOpenXALProbe(89.88782e6, frequency, current,
                        new double[][]{{0., 1., 0.1},
                        {0., 1., 0.1},
                        {0., 1., 0.1}});

                elementMapping = JElsElementMapping.getInstance();
                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 0., 1e-3, 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073433e+00, +1.006764e+00, +3.192074e-10, -3.407503e-11,},
                    {+0.000000e+00, +0.000000e+00, +1.330536e-01, +1.037577e+00, +3.409934e-10, -3.624753e-11,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +1.817597e-11, +8.542149e-01, +7.691930e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +2.146254e-11, -3.069547e-01, +8.706337e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.833518e-07, +2.649982e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+2.649982e-07, +2.442099e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +4.833522e-07, +2.649989e-07, -2.797349e-10, -3.225544e-11,},
                    {+0.000000e+00, +0.000000e+00, +2.649989e-07, +2.442115e-07, -6.239351e-10, -8.340346e-11,},
                    {+0.000000e+00, +0.000000e+00, -2.797349e-10, -6.239351e-10, +2.941682e-07, +1.307303e-07,},
                    {+0.000000e+00, +0.000000e+00, -3.225544e-11, -8.340346e-11, +1.307303e-07, +2.206415e-07,},};

                TWMean = new double[]{0.0, 0.0, -0.0734326, -0.133054, -1.01579e-06, 6.42917e-06, 1.0};
                CMerrTolerance = 2e-3;
            }
        }});

        // pitch thin rf fieldmap
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThinRfFieldMap pitch=1 deg";
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

                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 0., 0., 1 * Math.PI / 180., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.542149e-01, +7.691930e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.069547e-01, +8.706337e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.832862e-07, +2.648994e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+2.648994e-07, +2.441314e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +4.832866e-07, +2.648995e-07, +1.408078e-10, -1.716854e-10,},
                    {+0.000000e+00, +0.000000e+00, +2.648995e-07, +2.441326e-07, -5.080530e-10, -4.351796e-10,},
                    {+0.000000e+00, +0.000000e+00, +1.408078e-10, -5.080530e-10, +2.942552e-07, +1.308687e-07,},
                    {+0.000000e+00, +0.000000e+00, -1.716854e-10, -4.351796e-10, +1.308687e-07, +2.206937e-07,},};

                TWMean = new double[]{0.0, 0.0, 0.305933, 0.492079, -0.141031 * 0, 0.0111526 * 0, 1.0};
                CMerrTolerance = 2e-3;
            }
        }});

        // pitch thick rf fieldmap	
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThickRfFieldMap pitch = 1 deg";
                probe = setupOpenXALProbe(89.88782e6, frequency, current,
                        new double[][]{{0., 1., 0.1},
                        {0., 1., 0.1},
                        {0., 1., 0.1}});

                elementMapping = JElsElementMapping.getInstance();
                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 0., 0., 1 * Math.PI / 180., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.542149e-01, +7.691930e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.069547e-01, +8.706337e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.832862e-07, +2.648994e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+2.648994e-07, +2.441314e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +4.832866e-07, +2.648995e-07, +1.408078e-10, -1.716854e-10,},
                    {+0.000000e+00, +0.000000e+00, +2.648995e-07, +2.441326e-07, -5.080530e-10, -4.351796e-10,},
                    {+0.000000e+00, +0.000000e+00, +1.408078e-10, -5.080530e-10, +2.942552e-07, +1.308687e-07,},
                    {+0.000000e+00, +0.000000e+00, -1.716854e-10, -4.351796e-10, +1.308687e-07, +2.206937e-07,},};

                TWMean = new double[]{0.0, 0.0, 0.305933, 0.492079, -0.141031 * 0, 0.0111526 * 0, 1.0};
                CMerrTolerance = 2e-3;
            }
        }});

        // yaw thin rf fieldmap
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThinRfFieldMap yaw = 1 deg";
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
                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 0., 0., 0., 1 * Math.PI / 180., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.542149e-01, +7.691930e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.069547e-01, +8.706337e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.832866e-07, +2.648995e-07, +0.000000e+00, +0.000000e+00, +1.408078e-10, -1.716854e-10,},
                    {+2.648995e-07, +2.441326e-07, +0.000000e+00, +0.000000e+00, -5.080530e-10, -4.351796e-10,},
                    {+0.000000e+00, +0.000000e+00, +4.832862e-07, +2.648994e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.648994e-07, +2.441314e-07, +0.000000e+00, +0.000000e+00,},
                    {+1.408078e-10, -5.080530e-10, +0.000000e+00, +0.000000e+00, +2.942552e-07, +1.308687e-07,},
                    {-1.716854e-10, -4.351796e-10, +0.000000e+00, +0.000000e+00, +1.308687e-07, +2.206937e-07,},};

                TWMean = new double[]{0.305933, 0.492079, 0.0, 0.0, -0.141031 * 0, 0.0111526 * 0, 1.0};
                CMerrTolerance = 2e-3;
            }
        }});

        // yaw thick rf fieldmap	
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThickRfFieldMap yaw = 1 deg";
                probe = setupOpenXALProbe(89.88782e6, frequency, current,
                        new double[][]{{0., 1., 0.1},
                        {0., 1., 0.1},
                        {0., 1., 0.1}});

                elementMapping = JElsElementMapping.getInstance();
                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 0., 0., 0., 1 * Math.PI / 180., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.542149e-01, +7.691930e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.069547e-01, +8.706337e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.832866e-07, +2.648995e-07, +0.000000e+00, +0.000000e+00, +1.408078e-10, -1.716854e-10,},
                    {+2.648995e-07, +2.441326e-07, +0.000000e+00, +0.000000e+00, -5.080530e-10, -4.351796e-10,},
                    {+0.000000e+00, +0.000000e+00, +4.832862e-07, +2.648994e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.648994e-07, +2.441314e-07, +0.000000e+00, +0.000000e+00,},
                    {+1.408078e-10, -5.080530e-10, +0.000000e+00, +0.000000e+00, +2.942552e-07, +1.308687e-07,},
                    {-1.716854e-10, -4.351796e-10, +0.000000e+00, +0.000000e+00, +1.308687e-07, +2.206937e-07,},};

                TWMean = new double[]{0.305933, 0.492079, 0.0, 0.0, -0.141031 * 0, 0.0111526 * 0, 1.0};
                CMerrTolerance = 2e-3;
            }
        }});

        // roll thin rf fieldmap
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThinRfFieldMap roll = 1 deg";
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

                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 0., 0., 0., 0., 1 * Math.PI / 180.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073432e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330529e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073432e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330529e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.542148e-01, +7.691931e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.069545e-01, +8.706341e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.833516e-07, +2.649979e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+2.649979e-07, +2.442098e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +4.833516e-07, +2.649979e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.649979e-07, +2.442098e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.941681e-07, +1.307303e-07,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.307303e-07, +2.206415e-07,},};
            }
        }});

        // roll thick rf fieldmap	
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThickRfFieldMap roll = 1 deg";
                probe = setupOpenXALProbe(89.88782e6, frequency, current,
                        new double[][]{{0., 1., 0.1},
                        {0., 1., 0.1},
                        {0., 1., 0.1}});

                elementMapping = JElsElementMapping.getInstance();
                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 0., 0., 0., 0., 1 * Math.PI / 180.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073432e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330529e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073432e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330529e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.542148e-01, +7.691931e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.069545e-01, +8.706341e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.833516e-07, +2.649979e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+2.649979e-07, +2.442098e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +4.833516e-07, +2.649979e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.649979e-07, +2.442098e-07, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.941681e-07, +1.307303e-07,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.307303e-07, +2.206415e-07,},};

            }
        }});

        // all errors thin rf fieldmap	
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThinRfFieldMap all errors";
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
                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 1e-3, 1e-3, 1 * Math.PI / 180., 1 * Math.PI / 180., 1 * Math.PI / 180.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.542149e-01, +7.691930e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.069547e-01, +8.706337e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.832206e-07, +2.648010e-07, +1.319789e-13, +4.940851e-13, -1.394553e-10, -2.043654e-10,},
                    {+2.648010e-07, +2.440572e-07, +4.940851e-13, +4.317242e-12, -1.134176e-09, -5.202442e-10,},
                    {+1.319789e-13, +4.940851e-13, +4.832206e-07, +2.648010e-07, -1.394553e-10, -2.043654e-10,},
                    {+4.940851e-13, +4.317242e-12, +2.648010e-07, +2.440572e-07, -1.134176e-09, -5.202442e-10,},
                    {-1.394553e-10, -1.134176e-09, -1.394553e-10, -1.134176e-09, +2.943425e-07, +1.310075e-07,},
                    {-2.043654e-10, -5.202442e-10, -2.043654e-10, -5.202442e-10, +1.310075e-07, +2.207463e-07,},};

                TWMean = new double[]{0.232924, 0.360136, 0.232924, 0.360136, -0.281995*0, 0.0267418*0, 1.0};
                CMerrTolerance = 3e-3;
            }
        }});

        // all errors thick rf fieldmap	
        // FIELD_MAP 100 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "Spoke ThickRfFieldMap all errors";
                probe = setupOpenXALProbe(89.88782e6, frequency, current,
                        new double[][]{{0., 1., 0.1},
                        {0., 1., 0.1},
                        {0., 1., 0.1}});

                elementMapping = JElsElementMapping.getInstance();
                sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 1e-3, 1e-3, 1 * Math.PI / 180., 1 * Math.PI / 180., 1 * Math.PI / 180.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.073433e+00, +1.006764e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.330536e-01, +1.037577e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +8.542149e-01, +7.691930e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.069547e-01, +8.706337e-01,},};

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.832206e-07, +2.648010e-07, +1.319789e-13, +4.940851e-13, -1.394553e-10, -2.043654e-10,},
                    {+2.648010e-07, +2.440572e-07, +4.940851e-13, +4.317242e-12, -1.134176e-09, -5.202442e-10,},
                    {+1.319789e-13, +4.940851e-13, +4.832206e-07, +2.648010e-07, -1.394553e-10, -2.043654e-10,},
                    {+4.940851e-13, +4.317242e-12, +2.648010e-07, +2.440572e-07, -1.134176e-09, -5.202442e-10,},
                    {-1.394553e-10, -1.134176e-09, -1.394553e-10, -1.134176e-09, +2.943425e-07, +1.310075e-07,},
                    {-2.043654e-10, -5.202442e-10, -2.043654e-10, -5.202442e-10, +1.310075e-07, +2.207463e-07,},};

                TWMean = new double[]{0.232924, 0.360136, 0.232924, 0.360136, -0.281995*0, 0.0267418*0, 1.0};
                CMerrTolerance = 3e-3;
            }
        }});

        return tests;
    }

    /**
     *
     * @return sequence with fieldmap
     */
    public static AcceleratorSeq spokeFieldMap(double length, double frequency, double cavAmp, double cavPh) {
        return spokeFieldMap(length, frequency, cavAmp, cavPh, 0., 0., 0., 0., 0.);
    }

    public static AcceleratorSeq spokeFieldMap(double length, double frequency, double cavAmp, double cavPh, double dx, double dy, double pitch, double yaw, double roll) {
        AcceleratorSeq sequence = new AcceleratorSeq("ThinRfFieldMapTest");

        String fieldFile = "Field_Maps/1D/Spoke_W_coupler.edz";
        String fieldMapPath = JElsDemo.class.getResource(fieldFile).toString();
        fieldFile = fieldFile.substring(0, fieldFile.length() - 4);
        fieldMapPath = fieldMapPath.substring(0, fieldMapPath.indexOf(fieldFile));

        RfCavity rfCavity = ESSElementFactory.createRfFieldMap("TestFM", length, frequency, cavAmp,
                cavPh, fieldFile, fieldMapPath, new ApertureBucket(), 0, 0);


        rfCavity.setXOffset(dx);
        rfCavity.setYOffset(dy);
        rfCavity.setPitchAngle(pitch);
        rfCavity.setYawAngle(yaw);
        rfCavity.setRollAngle(roll);

        sequence.addNode(rfCavity);

        sequence.setLength(length);

        // Adding a marker in the middle to test splitting the ThickElement in 2
        Marker marker = new Marker("Marker");
        marker.setPosition(0.5 * length);
        sequence.addNode(marker);

        return sequence;
    }

    /**
     *
     * @return sequence with fieldmap
     */
    public static AcceleratorSeq hbFieldMap(double length, double frequency, double cavAmp, double cavPh) {
        AcceleratorSeq sequence = new AcceleratorSeq("ThinRfFieldMapTest");

        String fieldFile = "Field_Maps/1D/HB_W_coupler.edz";
        String fieldMapPath = JElsDemo.class.getResource(fieldFile).toString();
        fieldFile = fieldFile.substring(0, fieldFile.length() - 4);
        fieldMapPath = fieldMapPath.substring(0, fieldMapPath.indexOf(fieldFile));

        RfCavity rfCavity = ESSElementFactory.createRfFieldMap("TestFM", length, frequency, cavAmp,
                cavPh, fieldFile, fieldMapPath, new ApertureBucket(), 0, 0);

        sequence.addNode(rfCavity);

        sequence.setLength(length);

        // Adding a marker in the middle to test splitting the ThickElement in 2
        Marker marker = new Marker("Marker");
        marker.setPosition(0.5 * length);
        sequence.addNode(marker);

        return sequence;
    }
}
