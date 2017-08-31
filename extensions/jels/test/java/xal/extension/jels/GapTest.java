package xal.extension.jels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import xal.extension.jels.model.elem.jels.JElsElementMapping;
import xal.extension.jels.smf.ESSElementFactory;
import xal.extension.jels.smf.impl.ESSRfCavity;
import xal.extension.jels.smf.impl.ESSRfGap;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;

@RunWith(Parameterized.class)
public class GapTest extends SingleElementTest {

    public GapTest(SingleElementTestData data) {
        super(data);
    }

    @Parameters(name = "Gap {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 4.025e8, current = 0;

        List<Object[]> tests = new ArrayList<>();

        // 0: basic test, E=2.5MeV		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "basic test, f=-80";
                probe = setupOpenXALProbe(2.5e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                // GAP 78019.7 -80 14.5 0 0 0 0 0 0 0
                sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0, 0, 0, 0, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.772122e+00, +9.972978e-01},};

                // TW correlation matrix
                TWGamma = 1.002678848;
                TWCorrelationMatrix = new double[][]{
                    {6.994642E-13, 1.122275E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.122275E-12, 1.353002E-11, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.134478E-12, 1.928148E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.928148E-12, 1.046063E-11, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.389279E-12, -3.938841E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -3.938841E-12, 9.095447E-12,},};

                // ELS results
                elsPosition = 0.000000E+00;
                elsSigma = new double[]{8.001089E-04, 1.018977E-03, 1.753257E-03};
                elsBeta = new double[]{2.442000E-01, 3.974000E-01, 8.628735E-01};

                ELSerrTolerance = 1e-1;
            }
        }});

        // 1: basic test, f=-80, E=2.5GeV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "basic test, f=-80";
                probe = setupOpenXALProbe(2.5e9, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                // GAP 78019.7 -80 14.5 0 0 0 0 0 0 0
                sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0, 0, 0, 0, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.999979e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+7.883365e-06, +9.999979e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.999979e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +7.883365e-06, +9.999979e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -2.117148e-04, +9.999957e-01},};

                // TW correlation matrix
                TWGamma = 3.6644868;
                TWCorrelationMatrix = new double[][]{
                    {1.453257E-14, 1.049189E-14, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.049189E-14, 2.512722E-13, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.357074E-14, 1.925891E-14, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.925891E-14, 1.649869E-13, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 7.022798E-14, 5.736946E-13,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 5.736946E-13, 2.147892E-11,},};
            }
        }});

        // 2: basic test f=-80, E=200keV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "basic test, f=-80";
                probe = setupOpenXALProbe(0.2e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                // GAP 78019.7 -80 14.5 0 0 0 0 0 0 0
                sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0, 0, 0, 0, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {9.836180E-01, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {3.672434E+01, 9.836180E-01, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 9.836180E-01, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 3.672434E+01, 9.836180E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -7.348106E+01, 9.677556E-01,},};

                // TW correlation matrix
                TWGamma = 1.000227592;
                TWCorrelationMatrix = new double[][]{
                    {2.400575E-12, 9.136092E-11, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {9.136092E-11, 3.517263E-09, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 3.893553E-12, 1.485509E-10, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.485509E-10, 5.692321E-09, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.199025E-11, -8.739941E-10,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -8.739941E-10, 6.372214E-08,},};
            }
        }});

        // 3: TTF test f=-35, E=2.5MeV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "TTF test, f=-35";
                probe = setupOpenXALProbe(2.5e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                // GAP 78019.7 -35 14.5 0 0.0805777 0.772147 -0.386355 -0.142834 0 0 
                sequence = gap(4.025e8, 78019.7, -35, 14.5, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);;

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.976056e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+4.783932e-01, +9.904352e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.976056e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +4.783932e-01, +9.904352e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -9.619538e-01, +9.880637e-01},};

                // TW correlation matrix
                TWGamma = 1.002729084;
                TWCorrelationMatrix = new double[][]{
                    {+6.980143e-13, +8.350357e-13, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+8.350357e-13, +1.253634e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.132127e-12, +1.461270e-12, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.461270e-12, +8.952107e-12, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.371330e-12, -1.205749e-12},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.205749e-12, +4.842405e-12},};

                // ELS results
                elsPosition = 0.000000E+00;
                elsSigma = new double[]{8.001089E-04, 1.018977E-03, 1.753257E-03};
                elsBeta = new double[]{2.442000E-01, 3.974000E-01, 8.628735E-01};

                ELSerrTolerance = 1e-1;
                TMerrTolerance = 1e-2;
                CMerrTolerance = 1e-2;
            }
        }});

        // 4: TTF test f=-35, E=2.5GeV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "TTF test, f=-35";
                probe = setupOpenXALProbe(2.5e9, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                // GAP 78019.7 -35 14.5 0 0.0805777 0.772147 -0.386355 -0.142834 0 0 
                sequence = gap(4.025e8, 78019.7, -35, 14.5, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);;

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.999862e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+6.339641e-06, +9.999861e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.999862e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +6.339641e-06, +9.999861e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.702604e-04, +9.999723e-01},};

                // TW correlation matrix
                TWGamma = 3.6645664;
                TWCorrelationMatrix = new double[][]{
                    {1.453223E-14, 1.049162E-14, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.049162E-14, 2.512663E-13, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.357019E-14, 1.925843E-14, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.925843E-14, 1.649830E-13, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 7.022798E-14, 5.736840E-13,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 5.736840E-13, 2.147796E-11,},};
            }
        }});

        // 5: TTF test f=-35, E=200keV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "TTF test, f=-35";
                probe = setupOpenXALProbe(0.2e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                // GAP 78019.7 -35 14.5 0 0.0805777 0.772147 -0.386355 -0.142834 0 0 
                sequence = gap(4.025e8, 78019.7, -35, 14.5, 0, 0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);;

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.506411e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-3.068165e+01, +7.955257e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.506411e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -3.068165e+01, +7.955257e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +6.138550e+01, +1.198389e+00},};

                // TW correlation matrix
                TWGamma = 1.000148426;
                TWCorrelationMatrix = new double[][]{
                    {+5.630599e-12, -1.125339e-10, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-1.125339e-10, +2.275448e-09, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.132409e-12, -1.820628e-10, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -1.820628e-10, +3.645712e-09, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.198528e-11, +7.444642e-10},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +7.444642e-10, +4.626513e-08},};

                TMerrTolerance = 1e-2;
                CMerrTolerance = 1e-2;
            }
        }});

        // 6: spacecharge test, E=2.5MeV		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "spacechage test, f=-80";
                probe = setupOpenXALProbe2(2.5e6, frequency, 30.0e-3);
                elementMapping = JElsElementMapping.getInstance();
                // GAP 78019.7 -80 14.5 0 0 0 0 0 0 0
                sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0, 0, 0, 0, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.772122e+00, +9.972978e-01},};

                // TW correlation matrix
                TWGamma = 1.002678848;
                TWCorrelationMatrix = new double[][]{
                    {6.994642E-07, 1.122275E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.122275E-06, 1.353002E-05, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.134478E-06, 1.928148E-06, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.928148E-06, 1.046063E-05, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.389279E-06, -3.938841E-06,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -3.938841E-06, 9.095447E-06,},};

            }
        }});

        // 7: misalignment dx, E=2.5MeV		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "misalignment dx";
                probe = setupOpenXALProbe(2.5e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                // GAP 78019.7 -80 14.5 0 0 0 0 0 0 0
                sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.772122e+00, +9.972978e-01},};

                // TW correlation matrix
                TWGamma = 1.002678848;
                TWCorrelationMatrix = new double[][]{
                    {6.994642E-13, 1.122275E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.122275E-12, 1.353002E-11, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.134478E-12, 1.928148E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.928148E-12, 1.046063E-11, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.389279E-12, -3.938841E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -3.938841E-12, 9.095447E-12,},};

                TWMean = new double[]{0.00135294, -0.881345, 0, 0, 0, 0};

                TMerrTolerance = 1e-2;
            }
        }});

        // 8: misalignment dy
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "misalignment dy";
                probe = setupOpenXALProbe(2.5e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                // GAP 78019.7 -80 14.5 0 0 0 0 0 0 0
                sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.772122e+00, +9.972978e-01},};

                // TW correlation matrix
                TWGamma = 1.002678848;
                TWCorrelationMatrix = new double[][]{
                    {6.994642E-13, 1.122275E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.122275E-12, 1.353002E-11, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.134478E-12, 1.928148E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.928148E-12, 1.046063E-11, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.389279E-12, -3.938841E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -3.938841E-12, 9.095447E-12,},};

                TWMean = new double[]{0, 0, 0.00135294, -0.881345, 0, 0};
            }
        }});

        // 9: rotation
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "rotation";
                probe = setupOpenXALProbe(2.5e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                // GAP 78019.7 -80 14.5 0 0 0 0 0 0 0
                sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.772122e+00, +9.972978e-01},};

                // TW correlation matrix
                TWGamma = 1.002678848;
                TWCorrelationMatrix = new double[][]{
                    {6.994642E-13, 1.122275E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.122275E-12, 1.353002E-11, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.134478E-12, 1.928148E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.928148E-12, 1.046063E-11, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.389279E-12, -3.938841E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -3.938841E-12, 9.095447E-12,},};

                TWMean = new double[]{0, 0.0236132, 0, 0, 0, 0};
            }
        }});

        // 10: mislignment & rotation
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "misalignment & rotation";
                probe = setupOpenXALProbe(2.5e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                // GAP 78019.7 -80 14.5 0 0 0 0 0 0 0
                sequence = gap(4.025e8, 78019.7, -80, 14.5, 0, 0, 0, 0, 0, 0, 0, 1., 2., 0, 3., 4.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.986471e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.813454e-01, +9.986471e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.772122e+00, +9.972978e-01},};

                // TW correlation matrix
                TWGamma = 1.002678848;
                TWCorrelationMatrix = new double[][]{
                    {6.994642E-13, 1.122275E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.122275E-12, 1.353002E-11, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.134478E-12, 1.928148E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.928148E-12, 1.046063E-11, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.389279E-12, -3.938841E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -3.938841E-12, 9.095447E-12,},};

                TWMean = new double[]{0.00135294, -0.810506, 0.00270588, -1.66824, 0, 0};
            }
        }});

        return tests;
    }

    public static AcceleratorSeq gap(double frequency, double E0TL, double Phis, double R, double p, double betas, double Ts, double kTs, double k2Ts, double kS, double k2S) {

        return gap(frequency, E0TL, Phis, R, p, betas, Ts, kTs, k2Ts, kS, k2S, 0, 0, 0, 0, 0);
    }

    /**
     *
     * @param frequency
     * @param E0TL
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
    public static AcceleratorSeq gap(double frequency, double E0TL, double Phis, double R, double p, double betas, double Ts, double kTs, double k2Ts, double kS, double k2S, double dx, double dy, double dz, double fx, double fy) {
        AcceleratorSeq sequence = new AcceleratorSeq("GapTest");

        double length = 1.0; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal

        // FIXME position == 0?
        ESSRfGap gap = ESSElementFactory.createESSRfGap("g", true, 1, new ApertureBucket(), length, 0);
        gap.getAlign().setX(dx * 1e-3);
        gap.getAlign().setY(dy * 1e-3);
        gap.getAlign().setZ(dz * 1e-3);
        gap.getAlign().setPitch(fx * Math.PI / 180);
        gap.getAlign().setYaw(fy * Math.PI / 180);
        // FIXME position == 0?
        ESSRfCavity cavity = ESSElementFactory.createESSRfCavity("c", 0, gap, Phis, E0TL * 1e-6 / length, frequency * 1e-6, 0);

        // TTF		
        if (betas == 0.0) {
            cavity.getRfField().setTTFCoefs(new double[]{});
            cavity.getRfField().setTTF_endCoefs(new double[]{});
        } else {
            cavity.getRfField().setTTFCoefs(new double[]{betas, Ts, kTs, k2Ts});
            cavity.getRfField().setTTF_startCoefs(new double[]{betas, Ts, kTs, k2Ts});
            cavity.getRfField().setTTF_endCoefs(new double[]{betas, Ts, kTs, k2Ts});
            cavity.getRfField().setSTFCoefs(new double[]{betas, 0., kS, k2S});
            cavity.getRfField().setSTF_startCoefs(new double[]{betas, 0., kS, k2S});
            cavity.getRfField().setSTF_endCoefs(new double[]{betas, 0., kS, k2S});
        }

        sequence.addNode(cavity);

        return sequence;
    }
}
