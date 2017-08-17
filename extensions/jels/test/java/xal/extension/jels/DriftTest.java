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
                TWGamma = 1.003197291;
                TWCorrelationMatrix = new double[][]{
                    {8.278731E-13, 1.513690E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.513690E-12, 1.106865E-11, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.265081E-12, 1.538791E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.538791E-12, 7.267739E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.476234E-12, 2.380480E-12,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 2.380480E-12, 5.280765E-12,},};

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
                TWGamma = 3.6644723;
                TWCorrelationMatrix = new double[][]{
                    {1.879382E-14, 3.436277E-14, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {3.436277E-14, 2.512731E-13, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.871902E-14, 3.493261E-14, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 3.493261E-14, 1.649873E-13, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 5.914391E-15, 5.404006E-14,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 5.404006E-14, 1.599551E-12,},};

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
                TWGamma = 1.0031974;
                TWCorrelationMatrix = new double[][]{
                    {+7.632001e+00, +1.527927e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+1.527927e-02, +3.058910e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +6.063843e+00, +1.214025e-02, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.214025e-02, +2.430565e-05, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.830985e+00, +9.731960e-03},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.731960e-03, +1.960492e-05},};

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
                    {+1.106619e-02, +2.213592e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+2.213592e-05, +4.427891e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +7.429984e-03, +1.486103e-05, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.486103e-05, +2.972421e-08, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.090264e-05, +1.085451e-05},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.085451e-05, +1.080661e-05},};

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
