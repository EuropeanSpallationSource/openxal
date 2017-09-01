package xal.extension.jels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import xal.extension.jels.model.elem.jels.JElsElementMapping;
import xal.smf.AcceleratorSeq;

@RunWith(Parameterized.class)
public class DriftTest extends SingleElementTest {

    public DriftTest(SingleElementTestData data) {
        super(data);
    }

    @Parameters(name = "Drift {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 4.025e8, current = 0;

        List<Object[]> tests = new ArrayList<>();

        // 0: basic test, E=3MeV		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                probe = setupOpenXALProbe(3e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = drift(95e-3, 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.000000e+00, +9.500000e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.000000e+00, +9.500000e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +9.439542e-02},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00},};

                // TW correlation matrix
                TWGamma = 1.00319736680;
                TWCorrelationMatrix = new double[][]{
                    {8.278731E-13, 1.513690E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.513690E-12, 1.106865E-11, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.265081E-12, 1.538791E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.538791E-12, 7.267739E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.498498E-12, 2.395727E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 2.395727E-12, 5.314588E-12,},};

                // ELS results
                elsPosition = 9.500000E-02;
                elsSigma = new double[]{9.098807E-04, 1.124765E-03, 1.864477E-03};
                elsBeta = new double[]{3.158031E-01, 4.841974E-01, 9.758203E-01};
            }
        }});

        // 1: high energy test, E=2.5GeV		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                probe = setupOpenXALProbe(2.5e9, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = drift(95e-3, 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.000000e+00, +9.500000e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.000000e+00, +9.500000e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +7.074825e-03},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00},};

                // TW correlation matrix
                TWGamma = 3.66447233145;
                TWCorrelationMatrix = new double[][]{
                    {1.879382E-14, 3.436277E-14, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {3.436277E-14, 2.512731E-13, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.871902E-14, 3.493261E-14, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 3.493261E-14, 1.649873E-13, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 7.942056E-14, 7.256693E-13,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 7.256693E-13, 2.147935E-11,},};

            }
        }});

        // 2: space charge test, I=30mA		
        tests.add(new Object[]{new SingleElementTestData() {
            {
                probe = setupOpenXALProbe2(3e6, frequency, 30e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = drift(95e-3, 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.000000e+00, +9.500000e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.000000e+00, +9.500000e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +9.439542e-02},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00},};

                // TW correlation matrix
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {+8.870559e-07, +2.185591e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+2.185591e-06, +1.313238e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.336515e-06, +2.336272e-06, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +2.336272e-06, +9.191618e-06, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.578086e-06, +3.498884e-06},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.498884e-06, +6.968244e-06}};

                CMerrTolerance = 1e-2;
            }
        }});

        // 3: space charge test, I=30mA, L = 500m
        tests.add(new Object[]{new SingleElementTestData() {
            {
                probe = setupOpenXALProbe2(3e6, frequency, 30e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = drift(500, 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.000000e+00, +5.000000e+02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.000000e+00, +5.000000e+02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +4.968180e+02},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00},};

                // TW correlation matrix
                TWGamma = 1.00319736680;
                TWCorrelationMatrix = new double[][]{
                    {7.629228E+00, 1.527372E-02, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.527372E-02, 3.057801E-05, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 6.062039E+00, 1.213665E-02, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.213665E-02, 2.429845E-05, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 4.863059E+00, 9.796572E-03,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 9.796572E-03, 1.973507E-05,},};

                CMerrTolerance = 1e-3;
            }
        }});

        // 4: space charge test, I=30mA, L = 500m, E=20GeV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                probe = setupOpenXALProbe2(20.e9, frequency, 30e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = drift(500, 0., 0.);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.000000e+00, +5.000000e+02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.000000e+00, +5.000000e+02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +1.004074e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00},};

                // TW correlation matrix
                TWGamma = 22.315273669;
                TWCorrelationMatrix = new double[][]{
                    {1.104512E-02, 2.209376E-05, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {2.209376E-05, 4.419457E-08, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 7.421412E-03, 1.484388E-05, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.484388E-05, 2.968990E-08, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 5.427981E-03, 5.404017E-03,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 5.404017E-03, 5.380167E-03,},};

                CMerrTolerance = 1e-2;
            }
        }});

        return tests;
    }

    /**
     *
     * @param L length
     * @param R aperture
     * @param Ry aperture y
     * @return sequence with drift
     */
    public static AcceleratorSeq drift(double L, double R, double Ry) {
        AcceleratorSeq sequence = new AcceleratorSeq("DriftTest");
        sequence.setLength(L);
        return sequence;
    }

}
