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
import xal.model.IElement;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.ApertureBucket;

/**
 * Note: transfer matrices were calculated using Open XAL since there is a
 * possible bug in TraceWin print function for transfer matrices. Transfer
 * matrices will need to be recalculated after the bug is fixed. Correlation
 * matrices and gamma factors are properly calculated by TraceWin.
 */
@RunWith(Parameterized.class)
public class NCellsTest extends SingleElementTest {

    public NCellsTest(SingleElementTestData data) {
        super(data);
    }

    @Parameters(name = "NCells {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 352.21e6, current = 0;

        List<Object[]> tests = new ArrayList<>();

        // NCELLS m=0
        // NCELLS 0 3 0.1 1.e4 -30 31 0 0.5 0.5 1.0 -1.0 0.1 0.664594 0.423349 0.350508 0.634734 0.628339 0.249724 0.639103 0.622128 0.25257
        // 0: basic test m=0
        tests.add(new Object[]{new SingleElementTestData() {
            {
//                double frequency2 = 299.792e6;
//                double energy2 = 145.2e6;
                description = "basic test, m=0";
                probe = setupOpenXALProbe(3e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(frequency, 0, 3, 0.1, 1.0e4, -30, 31, 0,
                        0.5, 0.5, 1.0, -1.0, 0.1,
                        0.664594, 0.423349, 0.350508,
                        0.634734, 0.628339, 0.249724,
                        0.639103, 0.622128, 0.25257);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.000011e+00, +2.552592e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {-7.809247e-03, +9.978814e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.000011e+00, +2.552592e-01, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, -7.809247e-03, +9.978814e-01, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.997604e-01, +2.539094e-01,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.568796e-02, +1.004110e+00,},};

                TWGamma = 1.003198096;
                // TW correlation matrix
                TWCorrelationMatrix = new double[][]{
                    {+1.597331e-06, +3.274663e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+3.274663e-06, +1.101464e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +1.944977e-06, +2.687995e-06, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +2.687995e-06, +7.223819e-06, +0.000000e+00, +0.000000e+00,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.368339e-06, +3.291336e-06,},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.291336e-06, +5.384308e-06,},};
            }
        }});

// 		System.out.println("NCELLS m=1");
        //  NCELLS 1 3 0.1 1.e4 -30 31 0 0.5 0.5 1.0 -1.0 0.1 0.664594 0.423349 0.350508 0.634734 0.628339 0.249724 0.639103 0.622128 0.25257
        // 1: basic test m=1
        tests.add(
                new Object[]{new SingleElementTestData() {
                {
                    description = "basic test, m=1";
                    probe = setupOpenXALProbe(3e6, frequency, current);
                    elementMapping = JElsElementMapping.getInstance();
                    sequence = ncells(frequency, 1, 3, 0.1, 1.0e4, -30, 31, 0,
                            0.5, 0.5, 1.0, -1.0, 0.1,
                            0.664594, 0.423349, 0.350508,
                            0.634734, 0.628339, 0.249724,
                            0.639103, 0.622128, 0.25257);

                    // TW transfer matrix
                    TWTransferMatrix = new double[][]{
                        {+9.998909e-01, +1.276534e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {-2.312608e-03, +9.995383e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +9.998909e-01, +1.276534e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, -2.312608e-03, +9.995383e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.996689e-01, +1.268580e-01,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.651096e-03, +1.000646e+00,},};

                    // TW correlation matrix
                    TWGamma = 1.003199127;
                    TWCorrelationMatrix = new double[][]{
                        {+9.383769e-07, +1.872587e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+1.872587e-06, +1.105630e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.373074e-06, +1.772544e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.772544e-06, +7.257113e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.634158e-06, +2.568335e-06,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.568335e-06, +5.305173e-06,},};
                }
            }});

// 		System.out.println("NCELLS m=2");
        //  NCELLS 2 3 0.1 1.e4 -30 31 0 0.5 0.5 1.0 -1.0 0.1 0.664594 0.423349 0.350508 0.634734 0.628339 0.249724 0.639103 0.622128 0.25257
        // 2: basic test m=2
        tests.add(
                new Object[]{new SingleElementTestData() {
                {
                    description = "basic test, m=2";
                    probe = setupOpenXALProbe(3e6, frequency, current);
                    elementMapping = JElsElementMapping.getInstance();
                    sequence = ncells(frequency, 2, 3, 0.1, 1.0e4, -30, 31, 0,
                            0.5, 0.5, 1.0, -1.0, 0.1,
                            0.664594, 0.423349, 0.350508,
                            0.634734, 0.628339, 0.249724,
                            0.639103, 0.622128, 0.25257);

                    // TW transfer matrix
                    TWTransferMatrix = new double[][]{
                        {+9.999424e-01, +2.127342e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {-7.443932e-03, +9.983650e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +9.999424e-01, +2.127342e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, -7.443932e-03, +9.983650e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.999105e-01, +2.115532e-01,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.496839e-02, +1.003147e+00,},};

                    // TW correlation matrix
                    TWGamma = 1.003198063;
                    TWCorrelationMatrix = new double[][]{
                        {+1.337641e-06, +2.806720e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+2.806720e-06, +1.102565e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.728018e-06, +2.381417e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +2.381417e-06, +7.231441e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.105879e-06, +3.060400e-06,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.060400e-06, +5.371267e-06,},};
                }
            }});

        // NCELLS 0 3 0.1 1.e4 -30 31 0 0.5 0.5 1.0 -1.0 0 1 0 0 1 0 0 1 0 0
        //System.out.println("NCELLS no TTF m=0");
        // 3: no TTF, m=0
        tests.add(
                new Object[]{new SingleElementTestData() {
                {
                    description = "no TTF, m=0";
                    probe = setupOpenXALProbe(3e6, frequency, current);
                    elementMapping = JElsElementMapping.getInstance();
                    sequence = ncells(frequency, 0, 3, 0.1, 1.0e4, -30, 31, 0,
                            0.5, 0.5, 1.0, -1.0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

                    // TW transfer matrix
                    TWTransferMatrix = new double[][]{
                        {+1.000034e+00, +2.552413e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {-6.576341e-03, +9.981903e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.000034e+00, +2.552413e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, -6.576341e-03, +9.981903e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.998325e-01, +2.538817e-01,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.321309e-02, +1.003426e+00,},};

                    // TW correlation matrix
                    TWGamma = 1.003197986;
                    TWCorrelationMatrix = new double[][]{
                        {+1.597249e-06, +3.276427e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+3.276427e-06, +1.102259e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.944939e-06, +2.690268e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +2.690268e-06, +7.230365e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.368673e-06, +3.280335e-06,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.280335e-06, +5.367448e-06,},};
                }
            }});

        // NCELLS 1 3 0.1 1.e4 -30 31 0 0.5 0.5 1.0 -1.0 0 1 0 0 1 0 0 1 0 0
        //System.out.println("NCELLS no TTF m=1");
        // 4: no TTF, m=1
        tests.add(
                new Object[]{new SingleElementTestData() {
                {
                    description = "no TTF, m=1";
                    probe = setupOpenXALProbe(3e6, frequency, current);
                    elementMapping = JElsElementMapping.getInstance();
                    sequence = ncells(frequency, 1, 3, 0.1, 1.0e4, -30, 31, 0,
                            0.5, 0.5, 1.0, -1.0,
                            0, 1, 0, 0, 1, 0, 0, 1, 0, 0);

                    // TW transfer matrix
                    TWTransferMatrix = new double[][]{
                        {+1.000022e+00, +1.276561e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {-1.919347e-03, +9.995036e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.000022e+00, +1.276561e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, -1.919347e-03, +9.995036e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.997269e-01, +1.268589e-01,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.859814e-03, +1.000533e+00,},};

                    // TW correlation matrix
                    TWGamma = 1.003198834;
                    TWCorrelationMatrix = new double[][]{
                        {+9.385697e-07, +1.872886e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+1.872886e-06, +1.105590e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.373383e-06, +1.773063e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.773063e-06, +7.257275e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.634547e-06, +2.565542e-06,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.565542e-06, +5.300980e-06,},};
                }
            }});

        // NCELLS 2 3 0.1 1.e4 -30 31 0 0.5 0.5 1.0 -1.0 0 1 0 0 1 0 0 1 0 0
        // System.out.println("NCELLS no TTF m=2");
        // 5: no TTF, m=2
        tests.add(
                new Object[]{new SingleElementTestData() {
                {
                    description = "no TTF, m=2";
                    probe = setupOpenXALProbe(3e6, frequency, current);
                    elementMapping = JElsElementMapping.getInstance();
                    sequence = ncells(frequency, 2, 3, 0.1, 1.0e4, -30, 31, 0,
                            0.5, 0.5, 1.0, -1.0,
                            0, 1, 0, 0, 1, 0, 0, 1, 0, 0);

                    // TW transfer matrix
                    TWTransferMatrix = new double[][]{
                        {+9.999769e-01, +2.127191e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {-6.280302e-03, +9.985946e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +9.999769e-01, +2.127191e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, -6.280302e-03, +9.985946e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.999526e-01, +2.115363e-01,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.262878e-02, +1.002626e+00,},};

                    // TW correlation matrix
                    TWGamma = 1.003197958;
                    TWCorrelationMatrix = new double[][]{
                        {+1.337606e-06, +2.808075e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+2.808075e-06, +1.103179e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.728030e-06, +2.383304e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +2.383304e-06, +7.236725e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.106070e-06, +3.050706e-06,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.050706e-06, +5.356691e-06,},};
                }
            }});

        // NCELLS 0 3 0.1 1.e4 -30 31 0 0.5 0.5 1.0 -1.0 0 1 0 0 1 0 0 1 0 0
        //System.out.println("NCELLS no TTF m=0 spacecharge I=30mA");
        // 6: spacecharge no TTF, m=0
        tests.add(
                new Object[]{new SingleElementTestData() {
                {
                    description = "space charge no TTF, m=0";
                    probe = setupOpenXALProbe(3e6, frequency, 30e-3);
                    elementMapping = JElsElementMapping.getInstance();
                    sequence = ncells(frequency, 0, 3, 0.1, 1.0e4, -30, 31, 0,
                            0.5, 0.5, 1.0, -1.0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

                    // TW transfer matrix
                    TWTransferMatrix = new double[][]{
                        {+1.000034e+00, +2.552413e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {-6.576341e-03, +9.981903e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.000034e+00, +2.552413e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, -6.576341e-03, +9.981903e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.998325e-01, +2.538817e-01,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.321309e-02, +1.003426e+00,},};

                    // TW correlation matrix
                    TWGamma = 1.003197986;
                    TWCorrelationMatrix = new double[][]{
                        {+2.170398e-06, +5.795958e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+5.795958e-06, +1.864358e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +2.594651e-06, +5.399742e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +5.399742e-06, +1.386787e-05, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.225320e-06, +6.662686e-06,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +6.662686e-06, +1.092362e-05,},};
                    CMerrTolerance = 3e-4;
                }
            }});

        // NCELLS 0 3 0.5 1e6 -30 31 0 0.5 0.5 1.0 -1.0 0 1 0 0 1 0 0 1 0 0
        //System.out.println("NCELLS no TTF m=0 spacecharge I=30mA E=200MeV");
        // 7: spacecharge no TTF, m=0, E=200MeV
        tests.add(
                new Object[]{new SingleElementTestData() {
                {
                    description = "space charge no TTF, m=0";
                    probe = setupOpenXALProbe(200e6, frequency, 30e-3);
                    elementMapping = JElsElementMapping.getInstance();
                    sequence = ncells(frequency, 0, 3, 0.5, 1e6, -30, 31, 0,
                            0.5, 0.5, 1.0, -1.0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

                    // TW transfer matrix
                    TWTransferMatrix = new double[][]{
                        {+1.008199e+00, +1.280533e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+1.573708e-02, +1.010788e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.008199e+00, +1.280533e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.573708e-02, +1.010788e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.826029e-01, +8.596312e-01,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -4.600125e-02, +9.763666e-01,},};

                    // TW correlation matrix
                    TWGamma = 1.213576513;
                    TWCorrelationMatrix = new double[][]{
                        {+3.243616e-06, +2.554275e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+2.554275e-06, +2.040137e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +2.608858e-06, +1.931794e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.931794e-06, +1.465894e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.841879e-06, +1.600923e-06,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.600923e-06, +1.484833e-06,},};
                    CMerrTolerance = 1e-4;
                }
            }});

        // NCELLS m=0, spacecharge I=30mA, E=200MeV
        // NCELLS 0 3 0.5 1e6 -30 31 0 0.5 0.5 1.0 -1.0 0.386525 0.664594 0.423349 0.350508 0.634734 0.628339 0.249724 0.639103 0.622128 0.25257
        // 8: spacecharge, m=0, E=200MeV
        tests.add(
                new Object[]{new SingleElementTestData() {
                {
                    description = "space charge, m=0";
                    probe = setupOpenXALProbe(200e6, frequency, 30e-3);
                    elementMapping = JElsElementMapping.getInstance();
                    sequence = ncells(frequency, 0, 3, 0.5, 1e6, -30, 31, 0,
                            0.5, 0.5, 1.0, -1.0, 0.386525,
                            0.664594, 0.423349, 0.350508,
                            0.634734, 0.628339, 0.249724,
                            0.639103, 0.622128, 0.25257);

                    // TW transfer matrix
                    TWTransferMatrix = new double[][]{
                        {+1.005811e+00, +1.279964e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+1.142028e-02, +1.007987e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.005811e+00, +1.279964e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.142028e-02, +1.007987e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.872804e-01, +8.617247e-01,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.346180e-02, +9.828941e-01,},};

                    // TW correlation matrix
                    TWGamma = 1.213458702;
                    TWCorrelationMatrix = new double[][]{
                        {+3.240336e-06, +2.545802e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+2.545802e-06, +2.028884e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +2.605574e-06, +1.924489e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.924489e-06, +1.456952e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.853097e-06, +1.622035e-06,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.622035e-06, +1.512619e-06,},};
                    CMerrTolerance = 1e-4;
                }
            }});

        // NCELLS m=0, spacecharge I=30mA, E=3MeV, bunch freq=352.21MHz, RF freq=704.42MHz
        // NCELLS 0 3 0.1 1e4 -30 31 0 0.5 0.5 1.0 -1.0 0.1 0.664594 0.423349 0.350508 0.634734 0.628339 0.249724 0.639103 0.622128 0.25257
        // 9: spacecharge, m=0, E=3MeV
        tests.add(
                new Object[]{new SingleElementTestData() {
                {
                    description = "space charge, m=0";
                    probe = setupOpenXALProbe(3e6, frequency, 30e-3);
                    elementMapping = JElsElementMapping.getInstance();
                    sequence = ncells(2 * frequency, 0, 3, 0.1, 1e4, -30, 31, 0,
                            0.5, 0.5, 1.0, -1.0, 0.1,
                            0.664594, 0.423349, 0.350508,
                            0.634734, 0.628339, 0.249724,
                            0.639103, 0.622128, 0.25257);

                    // TW transfer matrix
                    TWTransferMatrix = new double[][]{
                        {+9.999684e-01, +1.276486e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {-8.961282e-03, +9.988081e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +9.999684e-01, +1.276486e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, -8.961282e-03, +9.988081e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.999103e-01, +1.269126e-01,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.802032e-02, +1.002297e+00,},};

                    // TW correlation matrix
                    TWGamma = 1.003197874;
                    TWCorrelationMatrix = new double[][]{
                        {+1.064812e-06, +2.914862e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+2.914862e-06, +1.443214e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.525203e-06, +2.991748e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +2.991748e-06, +1.034346e-05, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.850147e-06, +4.310405e-06,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.310405e-06, +8.121264e-06,},};
                    CMerrTolerance = 2e-4;
                }
            }});

        tests.add(
                new Object[]{new SingleElementTestData() {
                {
                    description = "misalignment test, m=0, dx, dy";
                    probe = setupOpenXALProbe(3e6, frequency, 0.);
                    elementMapping = JElsElementMapping.getInstance();
                    sequence = ncells(frequency, 0, 3, 0.1, 1e4, -30, 31, 0,
                            0.5, 0.5, 1.0, -1.0, 0.1,
                            0.664594, 0.423349, 0.350508,
                            0.634734, 0.628339, 0.249724,
                            0.639103, 0.622128, 0.25257,
                            .1, .2, 0, 0, 0, 0);

                    // TW transfer matrix
                    TWTransferMatrix = new double[][]{
                        {+1.000011e+00, +2.552592e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {-7.809247e-03, +9.978814e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.000011e+00, +2.552592e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, -7.809247e-03, +9.978814e-01, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.997604e-01, +2.539094e-01,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.568796e-02, +1.004110e+00,},};

                    // TW correlation matrix
                    TWGamma = 1.003198096;
                    TWCorrelationMatrix = new double[][]{
                        {+1.597331e-06, +3.274663e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+3.274663e-06, +1.101464e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +1.944977e-06, +2.687995e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +2.687995e-06, +7.223819e-06, +0.000000e+00, +0.000000e+00,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.368339e-06, +3.291336e-06,},
                        {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.291336e-06, +5.384308e-06,},};
                    TWMean = new double[]{-1.10367e-06, 0.000780925, -2.20734e-06, 0.00156185, 0.0, 0.0, 1.0};
                }
            }});
        return tests;
    }

    public static AcceleratorSeq ncells(double frequency, int m, int n, double betag, double E0T, double Phis, double R, double p,
            double kE0Ti, double kE0To, double dzi, double dzo,
            double betas, double Ts, double kTs, double k2Ts, double Ti, double kTi, double k2Ti, double To, double kTo, double k2To) {
        return ncells(frequency, m, n, betag, E0T, Phis, R, p, kE0Ti, kE0To, dzi, dzo, betas, Ts, kTs, k2Ts, Ti, kTi, k2Ti, To, kTo, k2To, 0, 0, 0, 0, 0, 0);

    }

    public static AcceleratorSeq ncells(double frequency, int m, int n, double betag, double E0T, double Phis, double R, double p,
            double kE0Ti, double kE0To, double dzi, double dzo,
            double betas, double Ts, double kTs, double k2Ts, double Ti, double kTi, double k2Ti, double To, double kTo, double k2To,
            double dx, double dy, double dz, double fx, double fy, double fz) {
        AcceleratorSeq sequence = new AcceleratorSeq("GapTest");
        AcceleratorNode[] nodes = new AcceleratorNode[n];

        double lambda = IElement.LightSpeed / frequency;
        double Lc0, Lc, Lcn;
        double amp0 = 1 + kE0Ti;
        double ampn = 1 + kE0To;
        double pos0, posn;

        if (betas != 0.0) {
            amp0 *= Ti / Ts;
            ampn *= To / Ts;
        }
        switch (m) {
            case 0:
                Lc = Lc0 = Lcn = betag * lambda;
                pos0 = 0.5 * Lc0 + dzi * 1e-3;
                posn = Lc0 + (n - 2) * Lc + 0.5 * Lcn + dzo * 1e-3;
                break;
            case 1:
                Lc = Lc0 = Lcn = 0.5 * betag * lambda;
                pos0 = 0.5 * Lc0 + dzi * 1e-3;
                posn = Lc0 + (n - 2) * Lc + 0.5 * Lcn + dzo * 1e-3;
                break;
            default:
                // m==2
                Lc0 = Lcn = 0.75 * betag * lambda;
                Lc = betag * lambda;
                pos0 = 0.25 * betag * lambda + dzi * 1e-3;
                posn = Lc0 + (n - 2) * Lc + 0.5 * betag * lambda + dzo * 1e-3;
                break;
        }

        // setup
        nodes[0] = ESSElementFactory.createESSRfGap("g0", true, amp0, new ApertureBucket(), Lc0, pos0);

        for (int i = 1; i < n - 1; i++) {
            nodes[i] = ESSElementFactory.createESSRfGap("g" + i, false, 1, new ApertureBucket(), Lc, Lc0 + (i - 0.5) * Lc);
        }

        ESSRfGap lastgap = ESSElementFactory.createESSRfGap("g" + (n - 1), false, ampn, new ApertureBucket(), Lcn, posn);
        lastgap.getRfGap().setEndCell(1);

        nodes[n - 1] = lastgap;
        ESSRfCavity cavity = ESSElementFactory.createESSRfCavity("c", Lc0 + (n - 2) * Lc + Lcn, nodes, Phis, E0T * 1e-6, frequency * 1e-6, 0);

        cavity.getAlign().setX(dx * 1e-3);
        cavity.getAlign().setY(dy * 1e-3);
        cavity.getAlign().setZ(dz * 1e-3);
        cavity.getAlign().setPitch(fx * Math.PI / 180.);
        cavity.getAlign().setYaw(fy * Math.PI / 180.);
        cavity.getAlign().setRoll(fz * Math.PI / 180.);

        // TTF
        if (betas == 0.0) {
            cavity.getRfField().setTTF_startCoefs(new double[]{});
            cavity.getRfField().setTTFCoefs(new double[]{});
            cavity.getRfField().setTTF_endCoefs(new double[]{});
        } else {
            cavity.getRfField().setTTF_startCoefs(new double[]{betas, Ti, kTi, k2Ti});
            cavity.getRfField().setTTFCoefs(new double[]{betas, Ts, kTs, k2Ts});
            cavity.getRfField().setTTF_endCoefs(new double[]{betas, To, kTo, k2To});
        }

        if (m == 1) {
            cavity.getRfField().setStructureMode(1);
        }
        sequence.addNode(cavity);
        sequence.setLength(Lc0 + (n - 2) * Lc + Lcn);

        return sequence;
    }
}
