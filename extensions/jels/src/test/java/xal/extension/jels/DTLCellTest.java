package xal.extension.jels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import xal.extension.jels.model.elem.JElsElementMapping;
import xal.extension.jels.smf.ESSElementFactory;
import xal.extension.jels.smf.impl.ESSRfCavity;
import xal.extension.jels.smf.impl.ESSRfGap;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.ElementFactory;
import xal.smf.attr.ApertureBucket;
import xal.smf.impl.Quadrupole;

@RunWith(Parameterized.class)
public class DTLCellTest extends SingleElementTest {

    public DTLCellTest(SingleElementTestData data) {
        super(data);
    }

    @Parameters(name = "DTLCell {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 4.025e8, current = 0;

        List<Object[]> tests = new ArrayList<>();

        //		System.out.println("DTL Cell test");
        // DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
        // 0: basic test
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "basic test";
                probe = setupOpenXALProbe(2.5e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
                        148174, -35, 10, 0,
                        0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.749908e-01, +6.610770e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-3.621475e+00, +7.571430e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.077946e+00, +7.155936e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +5.560820e+00, +1.276079e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.388414e-01, +6.531479e-02},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.794280e+00, +9.164720e-01},};

                // TW correlation matrix
                TWGamma = 1.002787652;
                TWCorrelationMatrix = new double[][]{
                    {+7.849968e-07, -1.616913e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-1.616913e-06, +1.337361e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.505982e-06, +9.194360e-06, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.194360e-06, +6.133377e-05, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.249099e-06, -3.800717e-06},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.800717e-06, +8.926809e-06},};

            }
        }});

//		System.out.println("DTL Cell test");
        // DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
        // 1: basic test, E=2.5GeV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "basic test";
                probe = setupOpenXALProbe(2.5e9, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
                        148174, -35, 10, 0,
                        0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.988970e-01, +6.847455e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-9.571625e-02, +9.944901e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.001052e+00, +6.858989e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.580911e-02, +1.005461e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.999992e-01, +5.103468e-03},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.233478e-04, +9.999465e-01},};

                // TW correlation matrix
                TWGamma = 3.664587829;
                CMerrTolerance = 2e-5;
                TWCorrelationMatrix = new double[][]{
                    {+1.711433e-08, +2.607576e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+2.607576e-08, +2.466521e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.704184e-08, +3.315048e-08, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +3.315048e-08, +1.707244e-07, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.707843e-09, +5.088331e-08},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.088331e-08, +1.599327e-06},};
            }
        }});

//		System.out.println("DTL Cell test");
        // DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
        // 2: basic test, E=200keV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "basic test";
                probe = setupOpenXALProbe(0.2e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
                        148174, -35, 10, 0,
                        0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {-4.241750e-02, +1.701181e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-7.418281e+01, -2.216842e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +4.927015e-01, +4.133921e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -3.606720e+01, -2.739446e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.928342e+00, +2.152325e-01},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.146369e+02, +5.281615e+00},};

                // TW correlation matrix
                TWGamma = 1.000115926;
                TWCorrelationMatrix = new double[][]{
                    {+1.429478e-08, +4.097482e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+4.097482e-06, +1.445445e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.159021e-06, -7.718000e-05, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -7.718000e-05, +5.302167e-03, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.075229e-04, +7.164306e-03},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +7.164306e-03, +1.669067e-01},};
                TMerrTolerance = 1e-4;
                CMerrTolerance = 3e-4;
            }
        }});

        // System.out.println("DTL Cell test only RF");
        // DTL_CEL 68.534 22.5 22.5 0.00864202 0 0 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
        // 3: only gap		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "only gap";
                probe = setupOpenXALProbe(2.5e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
                        148174, -35, 10, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.026033e+00, +6.881175e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+8.922687e-01, +1.012651e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.026033e+00, +6.881175e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.922687e-01, +1.012651e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.388414e-01, +6.531479e-02},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.794280e+00, +9.164720e-01},};

                // TW correlation matrix
                TWGamma = 1.002787652;
                TWCorrelationMatrix = new double[][]{
                    {+8.672837e-07, +2.044325e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+2.044325e-06, +1.390906e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.366517e-06, +2.619081e-06, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.619081e-06, +1.075062e-05, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.249099e-06, -3.800717e-06},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.800717e-06, +8.926809e-06},};
                CMerrTolerance = 2e-5;
            }
        }});

        // 4: only gap, E=2.5GeV 
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "only gap";
                probe = setupOpenXALProbe(2.5e9, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
                        148174, -35, 10, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.999741e-01, +6.853221e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+1.203956e-05, +9.999740e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.999741e-01, +6.853221e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.203956e-05, +9.999740e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.999992e-01, +5.103468e-03},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.233478e-04, +9.999465e-01},};

                // TW correlation matrix
                TWGamma = 3.664587829;
                CMerrTolerance = 2e-5;
                TWCorrelationMatrix = new double[][]{
                    {+1.715036e-08, +2.771183e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+2.771183e-08, +2.512650e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.698464e-08, +3.056533e-08, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +3.056533e-08, +1.649823e-07, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.707843e-09, +5.088331e-08},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.088331e-08, +1.599327e-06},};
            }
        }});

        // 5: only gap, E=200keV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "only gap";
                probe = setupOpenXALProbe(0.2e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
                        148174, -35, 10, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+2.122435e-01, +2.862845e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-5.729959e+01, -1.339885e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.122435e-01, +2.862845e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -5.729959e+01, -1.339885e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.928342e+00, +2.152325e-01},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.146369e+02, +5.281615e+00},};

                // TW correlation matrix
                TWGamma = 1.000115926;
                TWCorrelationMatrix = new double[][]{
                    {+1.687031e-07, -3.526898e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-3.526898e-05, +8.498568e-03, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.443334e-07, -5.635180e-05, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -5.635180e-05, +1.376846e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.075229e-04, +7.164306e-03},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +7.164306e-03, +1.669067e-01},};
                TMerrTolerance = 1e-4;
                CMerrTolerance = 3e-4;
            }
        }});

        // System.out.println("DTL Cell test Only RF no TTF");
        // DTL_CEL 68.534 22.5 22.5 0.00864202 0 0 148174 -35 10 0 0 0 0 0 0
        // 6: only gap no TTF
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "only gap no TTF";
                probe = setupOpenXALProbe(2.5e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
                        148174, -35, 10, 0,
                        0, 0, 0, 0, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.020163e+00, +6.881803e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+9.344604e-01, +1.020147e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.020163e+00, +6.881803e-02, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +9.344604e-01, +1.020147e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.359498e-01, +6.517930e-02,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.879136e+00, +9.125083e-01,},};

                // TW correlation matrix
                TWGamma = 1.002793835;
                TWCorrelationMatrix = new double[][]{
                    {+8.584569e-07, +2.079484e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+2.079484e-06, +1.419810e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.352105e-06, +2.670500e-06, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.670500e-06, +1.105194e-05, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.229380e-06, -4.077157e-06,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -4.077157e-06, +9.645687e-06,},};
                TMerrTolerance = 4e-5;
                CMerrTolerance = 2e-4;
            }
        }});

        // System.out.println("DTL Cell test Only RF no TTF");
        // DTL_CEL 68.534 22.5 22.5 0.00864202 0 0 148174 -35 10 0 0 0 0 0 0
        // 7: only gap no TTF
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "only gap no TTF";
                probe = setupOpenXALProbe(2.5e9, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
                        148174, -35, 10, 0,
                        0, 0, 0, 0, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.999812e-01, +6.853270e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+8.719464e-06, +9.999812e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.999812e-01, +6.853270e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.719464e-06, +9.999812e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.999994e-01, +5.103574e-03},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -2.341764e-04, +9.999613e-01},};

                // TW correlation matrix
                TWGamma = 3.664538568;
                CMerrTolerance = 2e-5;
                TWCorrelationMatrix = new double[][]{
                    {+1.715061e-08, +2.771218e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+2.771218e-08, +2.512685e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.698502e-08, +3.056569e-08, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +3.056569e-08, +1.649845e-07, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.707857e-09, +5.088473e-08},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.088473e-08, +1.599382e-06},};
            }
        }});

        // System.out.println("DTL Cell test Only RF no TTF");
        // DTL_CEL 68.534 22.5 22.5 0.00864202 0 0 148174 -35 10 0 0 0 0 0 0
        // 8: only gap no TTF
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "only gap no TTF";
                probe = setupOpenXALProbe(0.2e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
                        148174, -35, 10, 0,
                        0, 0, 0, 0, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.783789e+00, +9.139507e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+2.626371e+01, +1.783335e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.783789e+00, +9.139507e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.626371e+01, +1.783335e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -8.001782e-01, -3.813379e-04},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -5.255661e+01, -1.010888e+00},};
                TMerrTolerance = 1e-4;
                CMerrTolerance = 5e-5;
                // TW correlation matrix
                TWGamma = 1.000342512;
                TWCorrelationMatrix = new double[][]{
                    {+8.837476e-06, +1.332337e-04, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+1.332337e-04, +2.015749e-03, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.411260e-05, +2.114815e-04, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.114815e-04, +3.173543e-03, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +7.678454e-06, +5.100915e-04},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.100915e-04, +3.390161e-02},};
            }
        }});

//		System.out.println("DTL Cell test with spacecharge I=30mA");
        // DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
        // 9: space charge
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "space charge";
                probe = setupOpenXALProbe(2.5e6, frequency, 30e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
                        148174, -35, 10, 0,
                        0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.749908e-01, +6.610770e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-3.621475e+00, +7.571430e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.077946e+00, +7.155936e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +5.560820e+00, +1.276079e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.388414e-01, +6.531479e-02},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.794280e+00, +9.164720e-01},};

                // TW correlation matrix
                TWGamma = 1.002787652;
                TWCorrelationMatrix = new double[][]{
                    {+8.174030e-07, -1.233304e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-1.233304e-06, +1.150580e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.552823e-06, +1.005340e-05, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.005340e-05, +7.013171e-05, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.305220e-06, -3.031050e-06},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.031050e-06, +7.184365e-06},};

                CMerrTolerance = 4e-5;
            }
        }});

//		System.out.println("DTL Cell test with spacecharge I=30mA E=200MeV");
        // DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
        // 10: space charge, E=200MeV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "space charge";
                probe = setupOpenXALProbe(200e6, frequency, 30e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
                        148174, -35, 10, 0,
                        0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.943127e-01, +6.822478e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-4.887408e-01, +9.717297e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.005366e+00, +6.881648e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +4.937581e-01, +1.028010e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.998901e-01, +4.654681e-02},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -4.720550e-03, +9.994376e-01},};

                // TW correlation matrix
                TWGamma = 1.213328810;
                TWCorrelationMatrix = new double[][]{
                    {+8.824560e-08, +1.167685e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+1.167685e-07, +1.210896e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.414650e-07, +2.480599e-07, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.480599e-07, +1.089556e-06, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.686980e-07, +2.913010e-07},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.913010e-07, +9.564756e-07},};
//                TMerrTolerance = 1e-5;
                CMerrTolerance = 2e-5;
            }
        }});

        // 11: misalignment test dx & dy, E=2.5MeV		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "misalignment dx,dy";
                probe = setupOpenXALProbe(2.5e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
                        148174, -35, 10, 0,
                        0.0805777, 0.772147, -0.386355, -0.142834, 0, 0, 1, 2, 0, 0, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.749908e-01, +6.610770e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-3.621475e+00, +7.571430e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.077946e+00, +7.155936e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +5.560820e+00, +1.276079e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.388414e-01, +6.531479e-02},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.794280e+00, +9.164720e-01},};

                // TW correlation matrix
                TWGamma = 1.002787652;
                TWCorrelationMatrix = new double[][]{
                    {+7.849968e-07, -1.616913e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-1.616913e-06, +1.337361e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.505982e-06, +9.194360e-06, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.194360e-06, +6.133377e-05, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.249099e-06, -3.800717e-06},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.800717e-06, +8.926809e-06},};

                TWMean = new double[]{0.025, 3.6, -0.015589, -11.12, 0.0, 0.0, 1.0};

                CMerrTolerance = 2e-4;
            }
        }});

        return tests;
    }

    public static AcceleratorSeq dtlcell(double frequency, double L, double Lq1, double Lq2, double g, double B1, double B2,
            double E0TL, double Phis, double R, double p,
            double betas, double Ts, double kTs, double k2Ts, double kS, double k2S) {
        return dtlcell(frequency, L, Lq1, Lq2, g, B1, B2,
                E0TL, Phis, R, p,
                betas, Ts, kTs, k2Ts, kS, k2S, 0, 0, 0, 0, 0, 0);
    }

    /**
     *
     * @param L
     * @param Lq1
     * @param Lq2
     * @param g
     * @param B1
     * @param B2
     * @param E0TL Effective gap voltage
     * @param Phis RF phase (deg) absolute or relative
     * @param R aperture
     * @param p 0: relative phase, 1: absolute phase
     * @param betas particle reduced velocity
     * @param Ts transit time factor
     * @param kTs
     * @param k2Ts
     * @param kS
     * @param k2S
     * @return
     */
    public static AcceleratorSeq dtlcell(double frequency, double L, double Lq1, double Lq2, double g, double B1, double B2,
            double E0TL, double Phis, double R, double p,
            double betas, double Ts, double kTs, double k2Ts, double kS, double k2S, double dx, double dy, double dz, double fx, double fy, double fz) {
        AcceleratorSeq sequence = new AcceleratorSeq("DTLCellTest");

        // mm -> m
        L *= 1e-3;
        Lq1 *= 1e-3;
        Lq2 *= 1e-3;
        g *= 1e-3;
        double length = g; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal

        Quadrupole quad1 = ElementFactory.createQuadrupole("quad1", Lq1, B1, new ApertureBucket(), null, Lq1 / 2.);
        Quadrupole quad2 = ElementFactory.createQuadrupole("quad2", Lq2, B2, new ApertureBucket(), null, L - Lq2 / 2.);

        ESSRfGap gap = ESSElementFactory.createESSRfGap("g", true, 1, new ApertureBucket(), length, L / 2. - g);

        ESSRfCavity dtlTank = ESSElementFactory.createESSRfCavity("d", 0, new AcceleratorNode[]{quad1, gap, quad2}, Phis, E0TL * 1e-6 / length,
                frequency * 1e-6, 0);

        dtlTank.getAlign().setX(dx * 1e-3);
        dtlTank.getAlign().setY(dy * 1e-3);
        dtlTank.getAlign().setZ(dz * 1e-3);
        dtlTank.getAlign().setPitch(fx * Math.PI / 180.);
        dtlTank.getAlign().setYaw(fy * Math.PI / 180.);
        dtlTank.getAlign().setRoll(fz * Math.PI / 180.);

        // TTF
        if (betas == 0.0) {
            dtlTank.getRfField().setTTFCoefs(new double[]{0.0});
        } else {
            dtlTank.getRfField().setTTF_startCoefs(new double[]{betas, Ts, kTs, k2Ts});
            dtlTank.getRfField().setTTFCoefs(new double[]{betas, Ts, kTs, k2Ts});
            dtlTank.getRfField().setTTF_endCoefs(new double[]{betas, Ts, kTs, k2Ts});
            dtlTank.getRfField().setSTFCoefs(new double[]{betas, 0., kS, k2S});
            dtlTank.getRfField().setSTF_startCoefs(new double[]{betas, 0., kS, k2S});
            dtlTank.getRfField().setSTF_endCoefs(new double[]{betas, 0., kS, k2S});
        }

        dtlTank.setLength(L);
        sequence.addNode(dtlTank);
        sequence.setLength(L);

        return sequence;
    }

}
