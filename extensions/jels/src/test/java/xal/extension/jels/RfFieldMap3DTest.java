/*
 * Copyright (C) 2020 European Spallation Source ERIC.
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
public class RfFieldMap3DTest extends SingleElementTest {

    public RfFieldMap3DTest(SingleElementTestData data) {
        super(data);
    }

    @Parameters(name = "FieldMap {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 352.21e6, current = 0;

        List<Object[]> tests = new ArrayList<>();

        // 0: thin rf fieldmap
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
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
                    {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                    {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                    {-5.114998e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631934e-04,},
                    {-1.255348e-11, -7.025058e-12, +8.367997e-02, +1.015104e+00, -6.007247e-04, -4.028365e-04,},
                    {-8.165523e-11, -4.019615e-10, -1.018540e-04, -4.108881e-05, +8.542148e-01, +7.691930e-01,},
                    {-2.920958e-10, -5.615127e-10, -3.278075e-04, -1.428403e-04, -3.069549e-01, +8.706336e-01,},};

                TMerrTolerance = 5e-3;

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579941e-15, -9.222121e-16, +2.037486e-16,},
                    {+2.424339e-07, +2.297722e-07, -2.990553e-14, -7.169757e-15, -4.458243e-16, +5.316791e-16,},
                    {-2.424338e-14, -2.990553e-14, +4.664586e-07, +2.453212e-07, -1.065802e-10, -1.320951e-10,},
                    {-6.579941e-15, -7.169757e-15, +2.453212e-07, +2.315271e-07, -1.896178e-10, -9.819711e-11,},
                    {-9.222121e-16, -4.458243e-16, -1.065802e-10, -1.896178e-10, +2.941681e-07, +1.307301e-07,},
                    {+2.037486e-16, +5.316791e-16, -1.320951e-10, -9.819711e-11, +1.307301e-07, +2.206413e-07,},};
                CMerrTolerance = 4e-3;

                TWMean = new double[]{4.35558e-07, -3.95732e-07, 0.0230655, 0.0580919, 0, 0.0, 1.0};
            }
        }});

        // 1: thick rf fieldmap    
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
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
                    {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                    {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                    {-5.114998e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631934e-04,},
                    {-1.255348e-11, -7.025058e-12, +8.367997e-02, +1.015104e+00, -6.007247e-04, -4.028365e-04,},
                    {-8.165523e-11, -4.019615e-10, -1.018540e-04, -4.108881e-05, +8.542148e-01, +7.691930e-01,},
                    {-2.920958e-10, -5.615127e-10, -3.278075e-04, -1.428403e-04, -3.069549e-01, +8.706336e-01,},};

                TMerrTolerance = 5e-3;

                // TW correlation matrix
                TWGamma = 1.099608191;
                TWCorrelationMatrix = new double[][]{
                    {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579941e-15, -9.222121e-16, +2.037486e-16,},
                    {+2.424339e-07, +2.297722e-07, -2.990553e-14, -7.169757e-15, -4.458243e-16, +5.316791e-16,},
                    {-2.424338e-14, -2.990553e-14, +4.664586e-07, +2.453212e-07, -1.065802e-10, -1.320951e-10,},
                    {-6.579941e-15, -7.169757e-15, +2.453212e-07, +2.315271e-07, -1.896178e-10, -9.819711e-11,},
                    {-9.222121e-16, -4.458243e-16, -1.065802e-10, -1.896178e-10, +2.941681e-07, +1.307301e-07,},
                    {+2.037486e-16, +5.316791e-16, -1.320951e-10, -9.819711e-11, +1.307301e-07, +2.206413e-07,},};
                CMerrTolerance = 4e-3;

                TWMean = new double[]{4.35558e-07, -3.95732e-07, 0.0230655, 0.0580919, 0, 0.0, 1.0};
            }
        }});

        // FIELD_MAP 700 1500 66.7114 80 0 0.99941 0 1 HB_W_coupler
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
                    {+1.045293e+00, +1.507789e+00, +7.643171e-05, +1.065057e-04, -1.151461e-05, -1.826782e-06,},
                    {+3.392562e-02, +9.919681e-01, +1.169886e-04, +9.967506e-05, -1.434986e-05, -2.924793e-06,},
                    {+5.115451e-06, -2.520392e-05, +1.045270e+00, +1.507763e+00, -2.642966e-06, -2.377456e-06,},
                    {-1.795401e-05, -3.156820e-05, +3.387943e-02, +9.919233e-01, -2.083447e-06, -5.891639e-06,},
                    {+8.734454e-05, +1.834809e-05, -1.728085e-04, -1.209754e-04, +9.652971e-01, +5.660600e-01,},
                    {+1.326605e-04, -2.760003e-05, -6.770107e-04, -5.494081e-04, -6.920716e-02, +9.805994e-01,},};

                TMerrTolerance = 5e-3;

                // TW correlation matrix
                TWGamma = 1.622838927;
                TWCorrelationMatrix = new double[][]{
                    {+2.671679e-07, +1.215281e-07, +1.649504e-11, +3.323227e-12, +8.887024e-12, +7.351101e-12,},
                    {+1.215281e-07, +7.819243e-08, +1.966361e-11, +5.628180e-12, +9.123164e-13, -2.384985e-12,},
                    {+1.649504e-11, +1.966361e-11, +2.671578e-07, +1.215168e-07, -2.916897e-11, -1.223897e-10,},
                    {+3.323227e-12, +5.628180e-12, +1.215168e-07, +7.818512e-08, -1.073582e-11, -4.625749e-11,},
                    {+8.887024e-12, +9.123164e-13, -2.916897e-11, -1.073582e-11, +9.438697e-08, +1.119424e-07,},
                    {+7.351101e-12, -2.384985e-12, -1.223897e-10, -4.625749e-11, +1.119424e-07, +1.976176e-07,},};

                CMerrTolerance = 4e-3;

                TWMean = new double[]{-0.00084514, -0.001325249, 0.001733826, 0.00512628, 0, 0.0, 1.0};
            }
        }});

        // FIELD_MAP 700 1500 66.7114 80 0 0.99941 0 1 HB_W_coupler
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
                    {+1.045293e+00, +1.507789e+00, +7.643171e-05, +1.065057e-04, -1.151461e-05, -1.826782e-06,},
                    {+3.392562e-02, +9.919681e-01, +1.169886e-04, +9.967506e-05, -1.434986e-05, -2.924793e-06,},
                    {+5.115451e-06, -2.520392e-05, +1.045270e+00, +1.507763e+00, -2.642966e-06, -2.377456e-06,},
                    {-1.795401e-05, -3.156820e-05, +3.387943e-02, +9.919233e-01, -2.083447e-06, -5.891639e-06,},
                    {+8.734454e-05, +1.834809e-05, -1.728085e-04, -1.209754e-04, +9.652971e-01, +5.660600e-01,},
                    {+1.326605e-04, -2.760003e-05, -6.770107e-04, -5.494081e-04, -6.920716e-02, +9.805994e-01,},};

                TMerrTolerance = 5e-3;

                // TW correlation matrix
                TWGamma = 1.622838927;
                TWCorrelationMatrix = new double[][]{
                    {+2.671679e-07, +1.215281e-07, +1.649504e-11, +3.323227e-12, +8.887024e-12, +7.351101e-12,},
                    {+1.215281e-07, +7.819243e-08, +1.966361e-11, +5.628180e-12, +9.123164e-13, -2.384985e-12,},
                    {+1.649504e-11, +1.966361e-11, +2.671578e-07, +1.215168e-07, -2.916897e-11, -1.223897e-10,},
                    {+3.323227e-12, +5.628180e-12, +1.215168e-07, +7.818512e-08, -1.073582e-11, -4.625749e-11,},
                    {+8.887024e-12, +9.123164e-13, -2.916897e-11, -1.073582e-11, +9.438697e-08, +1.119424e-07,},
                    {+7.351101e-12, -2.384985e-12, -1.223897e-10, -4.625749e-11, +1.119424e-07, +1.976176e-07,},};

                CMerrTolerance = 4e-3;

                TWMean = new double[]{-0.00084514, -0.001325249, 0.001733826, 0.00512628, 0, 0.0, 1.0};
            }
        }});

        // Misalignments. The TWMean of these cases are Open XAL reference
        // values, not TraceWin output.
        // dx thin rf fieldmap
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
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
                    {+1.043408e+00, +9.962923e-01, -8.254994e-08, -2.237290e-08, -5.404315e-09, +3.463997e-10,},
                    {+7.718170e-02, +1.013040e+00, -1.068719e-07, -2.357833e-08, -4.330108e-09, +1.719982e-09,},
                    {-5.194005e-12, -2.799673e-12, +1.048105e+00, +9.970722e-01, -2.587122e-04, -1.660458e-04,},
                    {-1.273131e-11, -7.115205e-12, +8.521975e-02, +1.016205e+00, -6.176166e-04, -4.103946e-04,},
                    {-8.169377e-11, -4.062588e-10, -1.059592e-04, -4.344169e-05, +8.518828e-01, +7.685190e-01,},
                    {-2.916890e-10, -5.643835e-10, -3.390063e-04, -1.495676e-04, -3.128610e-01, +8.682883e-01,},};

                TMerrTolerance = 6e-3;

                // TW correlation matrix
                TWGamma = 1.099608194;
                TWCorrelationMatrix = new double[][]{
                    {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                    {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                    {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                    {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                    {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                    {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                CMerrTolerance = 4e-3;

                TWMean = new double[]{-4.263182033907097E-2, -7.549985275920756E-2, 2.3067660522322647E-2, 5.8095654649420125E-2, 1.0908281966141456E-7, 2.536441970173061E-7, 1.0};
            }
        }});

        // dx thick rf fieldmap    
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
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
                    {+1.043408e+00, +9.962923e-01, -8.254994e-08, -2.237290e-08, -5.404315e-09, +3.463997e-10,},
                    {+7.718170e-02, +1.013040e+00, -1.068719e-07, -2.357833e-08, -4.330108e-09, +1.719982e-09,},
                    {-5.194005e-12, -2.799673e-12, +1.048105e+00, +9.970722e-01, -2.587122e-04, -1.660458e-04,},
                    {-1.273131e-11, -7.115205e-12, +8.521975e-02, +1.016205e+00, -6.176166e-04, -4.103946e-04,},
                    {-8.169377e-11, -4.062588e-10, -1.059592e-04, -4.344169e-05, +8.518828e-01, +7.685190e-01,},
                    {-2.916890e-10, -5.643835e-10, -3.390063e-04, -1.495676e-04, -3.128610e-01, +8.682883e-01,},};

                TMerrTolerance = 6e-3;
                // TW correlation matrix
                TWGamma = 1.099608194;
                TWCorrelationMatrix = new double[][]{
                    {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                    {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                    {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                    {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                    {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                    {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                CMerrTolerance = 4e-3;

                TWMean = new double[]{-4.263092798550068E-2, -7.549940245122849E-2, 2.307324918011715E-2, 5.811053463891983E-2, 1.0915872161289423E-7, 2.5381853362474726E-7, 1.0};
            }
        }});

        // dy thin rf fieldmap
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
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
                    {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                    {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                    {-5.114997e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631935e-04,},
                    {-1.255348e-11, -7.025057e-12, +8.367998e-02, +1.015104e+00, -6.007248e-04, -4.028365e-04,},
                    {-8.165505e-11, -4.019613e-10, -1.018541e-04, -4.108882e-05, +8.542148e-01, +7.691930e-01,},
                    {-2.920948e-10, -5.615119e-10, -3.278075e-04, -1.428403e-04, -3.069550e-01, +8.706336e-01,},};

                TMerrTolerance = 5e-3;

                // TW correlation matrix
                TWGamma = 1.099608194;
                TWCorrelationMatrix = new double[][]{
                    {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                    {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                    {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                    {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                    {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                    {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                TWMean = new double[]{4.775813465699849E-8, 8.457743390830891E-8, -2.431680608965371E-2, -2.557769637748055E-2, 1.2979927857846061E-4, 3.301437871063093E-4, 1.0};

                CMerrTolerance = 4e-3;
            }
        }});

        // dy thick rf fieldmap    
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(
                new Object[]{new SingleElementTestData() {
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
                        {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                        {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                        {-5.114997e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631935e-04,},
                        {-1.255348e-11, -7.025057e-12, +8.367998e-02, +1.015104e+00, -6.007248e-04, -4.028365e-04,},
                        {-8.165505e-11, -4.019613e-10, -1.018541e-04, -4.108882e-05, +8.542148e-01, +7.691930e-01,},
                        {-2.920948e-10, -5.615119e-10, -3.278075e-04, -1.428403e-04, -3.069550e-01, +8.706336e-01,},};

                    TMerrTolerance = 5e-3;

                    // TW correlation matrix
                    TWGamma = 1.099608194;
                    TWCorrelationMatrix = new double[][]{
                        {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                        {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                        {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                        {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                        {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                        {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                    TWMean = new double[]{4.7757135061769476E-8, 8.457692819889786E-8, -2.43130239904825E-2, -2.556755726441862E-2, 1.2982134984741874E-4, 3.302135316771891E-4, 1.0};
                    CMerrTolerance = 4e-3;
                }
            }
                }
        );

        // pitch thin rf fieldmap
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
        tests.add(
                new Object[]{new SingleElementTestData() {
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
                        {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                        {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                        {-5.114997e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631935e-04,},
                        {-1.255348e-11, -7.025057e-12, +8.367998e-02, +1.015104e+00, -6.007248e-04, -4.028365e-04,},
                        {-8.165505e-11, -4.019613e-10, -1.018541e-04, -4.108882e-05, +8.542148e-01, +7.691930e-01,},
                        {-2.920948e-10, -5.615119e-10, -3.278075e-04, -1.428403e-04, -3.069550e-01, +8.706336e-01,},};

                    TMerrTolerance = 5e-3;

                    // TW correlation matrix
                    TWGamma = 1.099608194;
                    TWCorrelationMatrix = new double[][]{
                        {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                        {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                        {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                        {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                        {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                        {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                    TWMean = new double[]{4.776155757996921E-8, 8.45767892548258E-8, 2.8231570002863643E-1, 5.15816759281188E-1, -1.5504075049024287E-4, -2.507207744252523E-4, 1.0};
                    CMerrTolerance = 4e-3;
                }
            }
                }
        );

        // pitch thick rf fieldmap    
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(
                new Object[]{new SingleElementTestData() {
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
                        {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                        {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                        {-5.114997e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631935e-04,},
                        {-1.255348e-11, -7.025057e-12, +8.367998e-02, +1.015104e+00, -6.007248e-04, -4.028365e-04,},
                        {-8.165505e-11, -4.019613e-10, -1.018541e-04, -4.108882e-05, +8.542148e-01, +7.691930e-01,},
                        {-2.920948e-10, -5.615119e-10, -3.278075e-04, -1.428403e-04, -3.069550e-01, +8.706336e-01,},};

                    TMerrTolerance = 5e-3;

                    // TW correlation matrix
                    TWGamma = 1.099608194;
                    TWCorrelationMatrix = new double[][]{
                        {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                        {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                        {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                        {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                        {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                        {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                    TWMean = new double[]{4.776055790012332E-8, 8.45762820461472E-8, 2.8237075250966076E-1, 5.159370684607757E-1, -1.550676896291517E-4, -2.507589254483068E-4, 1.0};
                    CMerrTolerance = 4e-3;
                }
            }});

        // yaw thin rf fieldmap
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
        tests.add(
                new Object[]{new SingleElementTestData() {
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
                    sequence = spokeFieldMap(0.988, frequency * 1e-6, 0.974792, -1.10293, 0., 0., 0., 1 * Math.PI / 180., 0.);

                    // TW transfer matrix
                    TWTransferMatrix = new double[][]{
                        {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                        {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                        {-5.114997e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631935e-04,},
                        {-1.255348e-11, -7.025057e-12, +8.367998e-02, +1.015104e+00, -6.007248e-04, -4.028365e-04,},
                        {-8.165505e-11, -4.019613e-10, -1.018541e-04, -4.108882e-05, +8.542148e-01, +7.691930e-01,},
                        {-2.920948e-10, -5.615119e-10, -3.278075e-04, -1.428403e-04, -3.069550e-01, +8.706336e-01,},};

                    TMerrTolerance = 5e-3;

                    // TW correlation matrix
                    TWGamma = 1.099608194;
                    TWCorrelationMatrix = new double[][]{
                        {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                        {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                        {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                        {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                        {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                        {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                    TWMean = new double[]{2.3215408264656323E-1, 4.438454916228223E-1, 2.3067660522408412E-2, 5.809565464967826E-2, 1.0841475620519949E-7, 2.5247973492489643E-7, 1.0};
                    CMerrTolerance = 4e-3;
                }
            }});

        // yaw thick rf fieldmap    
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(
                new Object[]{new SingleElementTestData() {
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
                        {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                        {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                        {-5.114997e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631935e-04,},
                        {-1.255348e-11, -7.025057e-12, +8.367998e-02, +1.015104e+00, -6.007248e-04, -4.028365e-04,},
                        {-8.165505e-11, -4.019613e-10, -1.018541e-04, -4.108882e-05, +8.542148e-01, +7.691930e-01,},
                        {-2.920948e-10, -5.615119e-10, -3.278075e-04, -1.428403e-04, -3.069550e-01, +8.706336e-01,},};

                    TMerrTolerance = 5e-3;

                    // TW correlation matrix
                    TWGamma = 1.099608194;
                    TWCorrelationMatrix = new double[][]{
                        {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                        {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                        {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                        {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                        {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                        {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                    TWMean = new double[]{2.3219575968136805E-1, 4.4394809267581137E-1, 2.3073249180202957E-2, 5.8110534639178056E-2, 1.0849067564187438E-7, 2.5265408176809097E-7, 1.0};
                    CMerrTolerance = 4e-3;
                }
            }});

        // roll thin rf fieldmap
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler # 7.470983655095271 amplitude
        tests.add(
                new Object[]{new SingleElementTestData() {
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
                        {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                        {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                        {-5.114997e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631935e-04,},
                        {-1.255348e-11, -7.025057e-12, +8.367998e-02, +1.015104e+00, -6.007248e-04, -4.028365e-04,},
                        {-8.165505e-11, -4.019613e-10, -1.018541e-04, -4.108882e-05, +8.542148e-01, +7.691930e-01,},
                        {-2.920948e-10, -5.615119e-10, -3.278075e-04, -1.428403e-04, -3.069550e-01, +8.706336e-01,},};

                    TMerrTolerance = 5e-3;

                    // TW correlation matrix
                    TWGamma = 1.099608194;
                    TWCorrelationMatrix = new double[][]{
                        {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                        {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                        {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                        {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                        {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                        {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                    CMerrTolerance = 5e-3;

                    TWMean = new double[]{-4.025384263768199E-4, -1.0138243899857845E-3, 2.3064148039558977E-2, 5.8086807876299336E-2, 1.0908406531112326E-7, 2.5364680264375276E-7, 1.0};
                }
            }});

        // roll thick rf fieldmap    
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(
                new Object[]{new SingleElementTestData() {
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
                        {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                        {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                        {-5.114997e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631935e-04,},
                        {-1.255348e-11, -7.025057e-12, +8.367998e-02, +1.015104e+00, -6.007248e-04, -4.028365e-04,},
                        {-8.165505e-11, -4.019613e-10, -1.018541e-04, -4.108882e-05, +8.542148e-01, +7.691930e-01,},
                        {-2.920948e-10, -5.615119e-10, -3.278075e-04, -1.428403e-04, -3.069550e-01, +8.706336e-01,},};

                    TMerrTolerance = 5e-3;

                    // TW correlation matrix
                    TWGamma = 1.099608194;
                    TWCorrelationMatrix = new double[][]{
                        {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                        {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                        {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                        {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                        {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                        {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                    CMerrTolerance = 5e-3;

                    TWMean = new double[]{-4.0263596290187936E-4, -1.0140840821119194E-3, 2.306973584615658E-2, 5.810168559949613E-2, 1.0915996724795424E-7, 2.538211392375586E-7, 1.0};
                }
            }});

        // all errors thin rf fieldmap    
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(
                new Object[]{new SingleElementTestData() {
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
                        {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                        {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                        {-5.114997e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631935e-04,},
                        {-1.255348e-11, -7.025057e-12, +8.367998e-02, +1.015104e+00, -6.007248e-04, -4.028365e-04,},
                        {-8.165505e-11, -4.019613e-10, -1.018541e-04, -4.108882e-05, +8.542148e-01, +7.691930e-01,},
                        {-2.920948e-10, -5.615119e-10, -3.278075e-04, -1.428403e-04, -3.069550e-01, +8.706336e-01,},};

                    TMerrTolerance = 5e-3;

                    // TW correlation matrix
                    TWGamma = 1.099608194;
                    TWCorrelationMatrix = new double[][]{
                        {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                        {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                        {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                        {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                        {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                        {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                    TWMean = new double[]{1.8873658130512053E-1, 3.672338786467712E-1, 2.345310641283648E-1, 4.3203332122179294E-1, -2.4903028520248092E-5, 7.777888751975937E-5, 1.0};
                    CMerrTolerance = 5e-3;
                }
            }});

        // all errors thick rf fieldmap    
        // FIELD_MAP 700 988 -1.10293 28 0 0.974792 0 0 Spoke_W_coupler
        tests.add(
                new Object[]{new SingleElementTestData() {
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
                        {+1.042640e+00, +9.957581e-01, -8.256669e-08, -2.222150e-08, -5.547380e-09, +3.140651e-10,},
                        {+7.551721e-02, +1.011866e+00, -1.061573e-07, -2.289102e-08, -4.469734e-09, +1.714060e-09,},
                        {-5.114997e-12, -2.760815e-12, +1.047387e+00, +9.965514e-01, -2.519213e-04, -1.631935e-04,},
                        {-1.255348e-11, -7.025057e-12, +8.367998e-02, +1.015104e+00, -6.007248e-04, -4.028365e-04,},
                        {-8.165505e-11, -4.019613e-10, -1.018541e-04, -4.108882e-05, +8.542148e-01, +7.691930e-01,},
                        {-2.920948e-10, -5.615119e-10, -3.278075e-04, -1.428403e-04, -3.069550e-01, +8.706336e-01,},};

                    TMerrTolerance = 5e-3;

                    // TW correlation matrix
                    TWGamma = 1.099608194;
                    TWCorrelationMatrix = new double[][]{
                        {+4.638917e-07, +2.424339e-07, -2.424338e-14, -6.579942e-15, -9.222119e-16, +2.037490e-16,},
                        {+2.424339e-07, +2.297722e-07, -2.990554e-14, -7.169759e-15, -4.458243e-16, +5.316792e-16,},
                        {-2.424338e-14, -2.990554e-14, +4.664586e-07, +2.453212e-07, -1.065803e-10, -1.320952e-10,},
                        {-6.579942e-15, -7.169759e-15, +2.453212e-07, +2.315271e-07, -1.896179e-10, -9.819712e-11,},
                        {-9.222119e-16, -4.458243e-16, -1.065803e-10, -1.896179e-10, +2.941681e-07, +1.307301e-07,},
                        {+2.037490e-16, +5.316792e-16, -1.320952e-10, -9.819712e-11, +1.307301e-07, +2.206413e-07,},};

                    TWMean = new double[]{1.8877896592642573E-1, 3.6733671083126285E-1, 2.3458421886765796E-1, 4.321489292128113E-1, -2.490788390441391E-5, 7.780975668442526E-5, 1.0};
                    CMerrTolerance = 5e-3;
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
        AcceleratorSeq sequence = new AcceleratorSeq("RfFieldMapTest");

        String fieldFile = "Field_Maps/3D/Spoke_W_coupler.edz";
        String fieldMapPath = JElsDemo.class
                .getResource(fieldFile).toString();
        fieldFile = fieldFile.substring(0, fieldFile.length() - 4);
        fieldMapPath = fieldMapPath.substring(0, fieldMapPath.indexOf(fieldFile));

        RfCavity rfCavity = ESSElementFactory.createRfFieldMap("TestFM", length, frequency, cavAmp,
                cavPh, fieldFile, fieldMapPath, new ApertureBucket(), 0, 3, 0);

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
        AcceleratorSeq sequence = new AcceleratorSeq("RfFieldMapTest");

        String fieldFile = "Field_Maps/3D/HB_W_coupler.edz";
        String fieldMapPath = JElsDemo.class
                .getResource(fieldFile).toString();
        fieldFile = fieldFile.substring(0, fieldFile.length() - 4);
        fieldMapPath = fieldMapPath.substring(0, fieldMapPath.indexOf(fieldFile));

        RfCavity rfCavity = ESSElementFactory.createRfFieldMap("TestFM", length, frequency, cavAmp,
                cavPh, fieldFile, fieldMapPath, new ApertureBucket(), 0, 3, 0);

        sequence.addNode(rfCavity);

        sequence.setLength(length);

        // Adding a marker in the middle to test splitting the ThickElement in 2
        Marker marker = new Marker("Marker");
        marker.setPosition(0.5 * length);
        sequence.addNode(marker);

        return sequence;
    }
}
