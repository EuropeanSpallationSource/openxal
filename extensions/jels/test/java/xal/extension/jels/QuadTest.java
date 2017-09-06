package xal.extension.jels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import xal.extension.jels.model.elem.jels.JElsElementMapping;
import xal.smf.AcceleratorSeq;
import xal.smf.ElementFactory;
import xal.smf.attr.ApertureBucket;
import xal.smf.impl.Quadrupole;

@RunWith(Parameterized.class)
public class QuadTest extends SingleElementTest {

    public QuadTest(SingleElementTestData data) {
        super(data);
    }

    @Parameters(name = "Quad {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 4.025e8, current = 0;

        List<Object[]> tests = new ArrayList<>();

        // 0: basic test, E=3MeV, Q=-16		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "basic, Q=-16";
                probe = setupOpenXALProbe(3e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(70., -16., 15., 0., 0., 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.160627E+00, 7.370929E-02, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {4.708429E+00, 1.160627E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 8.475378E-01, 6.640501E-02, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, -4.241844E+00, 8.475378E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00, 6.955451E-02,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00,},};

                // TW correlation matrix
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {+1.001561e-12, +5.228219e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+5.228219e-12, +3.415331e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.733876e-13, -2.953358e-12, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -2.953358e-12, +1.780296e-11, +0.000000e+00, +0.000000e+00},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.382754E-12, 2.263708E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 2.263708E-12, 5.314587E-12,},};

                // ELS results
                elsPosition = 7.000000E-02;
                elsSigma = new double[]{1.000780E-03, 9.345521E-04, 1.833376E-03};
                elsBeta = new double[]{3.820541E-01, 3.342766E-01, 9.435362E-01};
            }
        }});

        // 1: high energy test, E=2.5GeV, Q=-16
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "high E, Q=-16";
                probe = setupOpenXALProbe(2.5e9, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(70., -16., 15., 0., 0., 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.003555e+00, +7.008293e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+1.016284e-01, +1.003555e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +9.964493e-01, +6.991713e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -1.013880e-01, +9.964493e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +5.213029e-03},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00},};

                // TW correlation matrix
                TWGamma = 3.6644723;
                TWCorrelationMatrix = new double[][]{
                    {1.734612E-14, 2.979602E-14, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {2.979602E-14, 2.553530E-13, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.689375E-14, 2.809892E-14, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.809892E-14, 1.601687E-13, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 7.679300E-14, 6.856805E-13,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 6.856805E-13, 2.147935E-11,},};
            }
        }});

        // 2: basic test, E=3MeV, Q=16
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "basic, Q=16";
                probe = setupOpenXALProbe(3e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(70., 16., 15., 0., 0., 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {8.475378E-01, 6.640501E-02, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {-4.241844E+00, 8.475378E-01, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.160627E+00, 7.370929E-02, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 4.708429E+00, 1.160627E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00, 6.955451E-02,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00,},};

                // TW correlation matrix
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {+5.606844e-13, -1.476716e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-1.476716e-12, +1.614641e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.583302e-12, +7.733000e-12, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +7.733000e-12, +4.208032e-11, +0.000000e+00, +0.000000e+00},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.382754E-12, 2.263708E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 2.263708E-12, 5.314587E-12,},};

                // ELS results
                elsPosition = 7.000000E-02;
                elsSigma = new double[]{7.487886E-04, 1.258293E-03, 1.833376E-03};
                elsBeta = new double[]{2.138779E-01, 6.059861E-01, 9.435362E-01};
            }
        }});

        // 3: high energy test, E=2.5GeV, Q=16
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "high E, Q=16";
                probe = setupOpenXALProbe(2.5e9, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(70., 16., 15., 0., 0., 0., 0., 0.);
                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+9.964493e-01, +6.991713e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {-1.013880e-01, +9.964493e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.003555e+00, +7.008293e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.016284e-01, +1.003555e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +5.213029e-03},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00},};

                // TW correlation matrix
                TWGamma = 3.6644723;
                TWCorrelationMatrix = new double[][]{
                    {1.711984E-14, 2.638078E-14, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {2.638078E-14, 2.475213E-13, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.725810E-14, 3.354104E-14, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 3.354104E-14, 1.703343E-13, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 7.679300E-14, 6.856805E-13,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 6.856805E-13, 2.147935E-11,},};
            }
        }});

        // 4: bigger beta variation test, E=3MeV, Q=-16		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "bigger twiss, Q=-16";
                probe = setupOpenXALProbe(3e6, frequency, current,
                        new double[][]{{-0.1763, 0.3, 0.2098},
                        {-0.3247, 0.4, 0.2091},
                        {-0.5283, 0.8, 0.2851}});
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(70., -16., 15., 0., 0., 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.160627E+00, 7.370929E-02, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {4.708429E+00, 1.160627E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 8.475378E-01, 6.640501E-02, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, -4.241844E+00, 8.475378E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00, 6.955451E-02,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00,},};

                // TW correlation matrix
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {+1.187423e-06, +5.851465e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+5.851465e-06, +3.462287e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.780590e-07, -2.980439e-06, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -2.980439e-06, +1.789126e-05, +0.000000e+00, +0.000000e+00},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.141286E-06, 2.295313E-06,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 2.295313E-06, 5.768984E-06,},};
            }
        }});

        // 5: space charge test, E=3MeV, Q=-16		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "space charge, Q=-16";
                probe = setupOpenXALProbe2(3e6, frequency, 30.0e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(70., -16., 15., 0., 0., 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.160627E+00, 7.370929E-02, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {4.708429E+00, 1.160627E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 8.475378E-01, 6.640501E-02, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, -4.241844E+00, 8.475378E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00, 6.955451E-02,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00,},};

                // TW correlation matrix
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {1.038836E-06, 5.857502E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {5.857502E-06, 3.964296E-05, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 9.058370E-07, -2.566342E-06, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, -2.566342E-06, 1.480676E-05, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.439074E-06, 3.076790E-06,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.076790E-06, 6.490179E-06,},};

                CMerrTolerance = 1e-4;
            }
        }});

        // 6: space charge test, L=5m
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "space charge, L=5m, Q=-16";
                probe = setupOpenXALProbe2(3e6, frequency, 30.0e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(5000., -16., 15., 0., 0., 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.133009E+17, 1.417609E+16, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {9.055457E+17, 1.133009E+17, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, -6.381267E-01, 9.633300E-02, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, -6.153595E+00, -6.381267E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00, 4.968179E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00,},};

                // TW correlation matrix
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {+1.000000e+25, +6.819195e+27, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+6.819195e+27, +1.000000e+25, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.508888e-07, +2.454528e-06, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.454528e-06, +5.122298e-05, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.359821e-04, +6.339603e-05},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +6.339603e-05, +1.199989e-05},};
                CMerrTolerance = 1e3;
            }
        }});

        // 7: space charge test big E, L=5m
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "space charge big E, L=5m, Q=-16";
                probe = setupOpenXALProbe2(20e9, frequency, 30.0e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(5000., -16., 15., 0., 0., 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+5.526161e+00, +1.134948e+01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+2.602626e+00, +5.526161e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -7.335668e-01, +1.419210e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -3.254486e-01, -7.335668e-01, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +1.004074e-02},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00},};

                // TW correlation matrix
                TWGamma = 22.315779;
                TWCorrelationMatrix = new double[][]{
                    {5.421886E-06, 2.637317E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {2.637317E-06, 1.282862E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 4.862275E-08, -2.622650E-08, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, -2.622650E-08, 1.595557E-08, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 5.496255E-07, 5.027160E-05,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 5.027160E-05, 4.671895E-03,},};

                CMerrTolerance = 1e-5;
            }
        }});

        // 8: misalignment test dx, E=3MeV, Q=-16		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "mislignment dx";
                probe = setupOpenXALProbe(3e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(70., -16., 15., 0., 0., 0., 0., 0., 1., 0., 0., 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.160627E+00, 7.370929E-02, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {4.708429E+00, 1.160627E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 8.475378E-01, 6.640501E-02, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, -4.241844E+00, 8.475378E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00, 6.955451E-02,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00,},};

                // TW correlation matrix
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {+1.001561e-12, +5.228219e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+5.228219e-12, +3.415331e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.733876e-13, -2.953358e-12, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -2.953358e-12, +1.780296e-11, +0.000000e+00, +0.000000e+00},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.382754E-12, 2.263708E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 2.263708E-12, 5.314587E-12,},};
                TWMean = new double[]{
                    -0.160625, -4.70837, 0, 0, -0.000248268, 0
                };

                // ELS results
                elsPosition = 7.000000E-02;
                elsSigma = new double[]{1.000780E-03, 9.345521E-04, 1.833376E-03};
                elsBeta = new double[]{3.820541E-01, 3.342766E-01, 9.435362E-01};
            }
        }});

        // 9: misalignment test dy, E=3MeV, Q=-16		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "mislignment dy";
                probe = setupOpenXALProbe(3e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(70., -16., 15., 0., 0., 0., 0., 0., 0., 1., 0., 0., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.160627E+00, 7.370929E-02, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {4.708429E+00, 1.160627E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 8.475378E-01, 6.640501E-02, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, -4.241844E+00, 8.475378E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00, 6.955451E-02,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00,},};

                // TW correlation matrix
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {+1.001561e-12, +5.228219e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+5.228219e-12, +3.415331e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.733876e-13, -2.953358e-12, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -2.953358e-12, +1.780296e-11, +0.000000e+00, +0.000000e+00},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.382754E-12, 2.263708E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 2.263708E-12, 5.314587E-12,},};
                TWMean = new double[]{
                    0, 0, 0.15246, 4.2418, -0.000219041, 0
                };
            }
        }});

        // 10: rotation test fz
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "rotation fz";
                probe = setupOpenXALProbe(3e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(70., -16., 15., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 1.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.160532E+00, 7.370707E-02, 5.463327E-03, 1.274578E-04, 0.000000E+00, 0.000000E+00,},
                    {4.705703E+00, 1.160532E+00, 1.561800E-01, 5.463327E-03, 0.000000E+00, 0.000000E+00,},
                    {5.463327E-03, 1.274578E-04, 8.476332E-01, 6.640724E-02, 0.000000E+00, 0.000000E+00,},
                    {1.561800E-01, 5.463327E-03, -4.239118E+00, 8.476332E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00, 6.955451E-02,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00,},};

                // TW correlation matrix
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {+1.001439e-12, +5.226533e-12, +9.686578e-15, +1.089479e-13, +0.000000e+00, +0.000000e+00},
                    {+5.226533e-12, +3.415808e-11, +1.741208e-13, +7.520650e-14, +0.000000e+00, +0.000000e+00},
                    {+9.686578e-15, +1.741208e-13, +8.735915e-13, -2.950460e-12, +0.000000e+00, +0.000000e+00},
                    {+1.089479e-13, +7.520650e-14, -2.950460e-12, +1.780010e-11, +0.000000e+00, +0.000000e+00},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.382754E-12, 2.263708E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 2.263708E-12, 5.314587E-12,},};
            }
        }});

        // 11: rotation test x, E=3MeV, Q=-16		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "rotation fx";
                probe = setupOpenXALProbe(3e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(70., -16., 15., 0., 0., 0., 0., 0., 0., 0., 0., 1., 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.160627E+00, 7.370929E-02, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {4.708429E+00, 1.160627E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 8.475378E-01, 6.640501E-02, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, -4.241844E+00, 8.475378E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00, 6.955451E-02,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00,},};

                // TW correlation matrix
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {+1.001561e-12, +5.228219e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+5.228219e-12, +3.415331e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +8.733876e-13, -2.953358e-12, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, -2.953358e-12, +1.780296e-11, +0.000000e+00, +0.000000e+00},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.382754E-12, 2.263708E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 2.263708E-12, 5.314587E-12,},};
                TWMean = new double[]{
                    0.0333817, 0.0727444, 0, 0, -9.3271e-06, 0
                };
            }
        }});

        // 12: misalignment & rotation test, E=3MeV, Q=-16		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "misalignment & rotation";
                probe = setupOpenXALProbe(3e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = quad(70., -16., 15., 0., 0., 0., 0., 0., 1., 2., 0., 3., 4., 5.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.158249E+00, 7.365381E-02, 2.718368E-02, 6.341873E-04, 0.000000E+00, 0.000000E+00,},
                    {4.640442E+00, 1.158249E+00, 7.770993E-01, 2.718368E-02, 0.000000E+00, 0.000000E+00,},
                    {2.718368E-02, 6.341873E-04, 8.499161E-01, 6.646050E-02, 0.000000E+00, 0.000000E+00,},
                    {7.770993E-01, 2.718368E-02, -4.173856E+00, 8.499161E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00, 6.955451E-02,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.000000E+00,},};

                // TW correlation matrix
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {+9.985181e-13, +5.186108e-12, +4.822285e-14, +5.428292e-13, +0.000000e+00, +0.000000e+00},
                    {+5.186108e-12, +3.427046e-11, +8.671076e-13, +3.955276e-13, +0.000000e+00, +0.000000e+00},
                    {+4.822285e-14, +8.671076e-13, +8.784742e-13, -2.881004e-12, +0.000000e+00, +0.000000e+00},
                    {+5.428292e-13, +3.955276e-13, -2.881004e-12, +1.773345e-11, +0.000000e+00, +0.000000e+00},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.382754E-12, 2.263708E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 2.263708E-12, 5.314587E-12,},};
                TWMean = new double[]{
                    -0.0917743, -5.97537, 0.169973, 7.85048, -0.000587091, 0
                };
                CMerrTolerance = 1e-4;
            }
        }});

        return tests;
    }

    /**
     *
     * @param L length
     * @param G field
     * @param R aperture
     * @param Phi skew angle
     * @param G3 sextupole gradient (T/m^2)
     * @param G4 octupole gradient (T/m^2)
     * @param G5 decapole gradient (T/m^2)
     * @param G6 dodecapole gradient (T/m^2)
     * @return
     */
    public static AcceleratorSeq quad(double L, double G, double R, double Phi, double G3, double G4, double G5, double G6) {
        return quad(L, G, R, Phi, G3, G4, G5, G6, 0, 0, 0, 0, 0, 0);
    }

    /**
     *
     * @param L length
     * @param G field
     * @param R aperture
     * @param Phi skew angle
     * @param G3 sextupole gradient (T/m^2)
     * @param G4 octupole gradient (T/m^2)
     * @param G5 decapole gradient (T/m^2)
     * @param G6 dodecapole gradient (T/m^2)
     * @param dx displacement in x
     * @param dy displacement in y
     * @param dz displacement in z
     * @param fx rotation in x
     * @param fy rotation in y
     * @param fz rotation in z
     * @return
     */
    public static AcceleratorSeq quad(double L, double G, double R, double Phi, double G3, double G4, double G5, double G6, double dx, double dy, double dz, double fx, double fy, double fz) {
        AcceleratorSeq sequence = new AcceleratorSeq("QuadTest");

        ApertureBucket aper = new ApertureBucket();
        aper.setAperX(R * 1e-3);
        aper.setAperY(R * 1e-3);
        aper.setShape(ApertureBucket.iRectangle);
        Quadrupole quad = ElementFactory.createQuadrupole("quad", L * 1e-3, G, aper, null, L * 1e-3 * 0.5);

        quad.getAlign().setX(dx * 1e-3);
        quad.getAlign().setY(dy * 1e-3);
        quad.getAlign().setZ(dz * 1e-3);
        quad.getAlign().setPitch(fx * Math.PI / 180.);
        quad.getAlign().setYaw(fy * Math.PI / 180.);
        quad.getAlign().setRoll(fz * Math.PI / 180.);

        sequence.addNode(quad);
        sequence.setLength(L * 1e-3);
        return sequence;
    }
}
