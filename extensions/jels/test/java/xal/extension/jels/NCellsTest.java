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
 * Note: matrices were recalculated to pass the tests, but some tests need to be
 * redone as they
 */
@RunWith(Parameterized.class)
public class NCellsTest extends SingleElementTest {

    public NCellsTest(SingleElementTestData data) {
        super(data);
    }

    @Parameters(name = "NCells {index}: {0}")
    public static Collection<Object[]> tests() {
        final double frequency = 4.025e8, current = 0;

        List<Object[]> tests = new ArrayList<>();

        // NCELLS m=0
        // NCELLS 0 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0.386525 0.664594 0.423349 0.350508 0.634734 0.628339 0.249724 0.639103 0.622128 0.25257
        // 0: basic test m=0
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "basic test, m=0";
                probe = setupOpenXALProbe(78e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(4.025e8, 0, 3, 0.5, 5.27924e6, -72.9826, 31, 0,
                        0.493611, 0.488812, 12.9359, -14.4824,
                        0.386525, 0.664594, 0.423349, 0.350508, 0.634734, 0.628339, 0.249724, 0.639103, 0.622128, 0.25257);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {+1.244549e+00, +1.207575e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+4.744376e-01, +1.252649e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +1.244549e+00, +1.207575e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +4.744376e-01, +1.252649e+00, +0.000000e+00, +0.000000e+00},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.388279e-01, +7.884434e-01},
                    {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -8.872362e-01, +5.317630e-01},};
                TMerrTolerance = 2e-3;

                TWGamma = 1.08550216;
                // TW correlation matrix
                TWCorrelationMatrix = new double[][]{
                    {3.556654E-12, 3.474525E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {3.474525E-12, 3.463701E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.833495E-12, 2.573891E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.573891E-12, 2.424613E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.405578E-12, 1.086873E-13,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.086873E-13, 3.327276E-13,},};
                CMerrTolerance = 4e-2;
            }
        }});

// 		System.out.println("NCELLS m=1");
        //  NCELLS 1 3 0.5 5.34709e+06 -55.7206 31 0 0.493611 0.488812 12.9604 -14.5077 0.393562 0.672107 0.409583 0.342918 0.645929 0.612576 0.257499 0.650186 0.606429 0.259876
        // 1: basic test m=1
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "basic test, m=1";
                probe = setupOpenXALProbe(78e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(4.025e8, 1, 3, 0.5, 5.34709e+06, -55.7206, 31, 0,
                        0.493611, 0.488812, 12.9604, -14.5077,
                        0.393562, 0.672107, 0.409583, 0.342918, 0.645929, 0.612576, 0.257499, 0.650186, 0.606429, 0.259876);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.020268E+00, 5.598010E-01, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {9.774470E-02, 1.026800E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.020268E+00, 5.598010E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 9.774470E-02, 1.026800E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 9.452121E-01, 4.685073E-01,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -2.253722E-01, 9.387362E-01,},};

                // TW correlation matrix
                TWGamma = 1.0842794;
                TWCorrelationMatrix = new double[][]{
                    {8.967675E-13, 1.333832E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.333832E-12, 2.263329E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 8.322374E-13, 1.003169E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.003169E-12, 1.508284E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.212472E-12, 8.160311E-13,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 8.160311E-13, 1.074470E-12,},};
            }
        }});

// 		System.out.println("NCELLS m=2");
        // NCELLS 2 3 0.5 5.34709e+06 -55.7206 31 0 0.493611 0.488812 12.9604 -14.5077 0.393562 0.672107 0.409583 0.342918 0.645929 0.612576 0.257499 0.650186 0.606429 0.259876
        // 2: basic test m=2
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "basic test, m=2";
                probe = setupOpenXALProbe(78e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(4.025e8, 2, 3, 0.5, 5.34709e+06, -55.7206, 31, 0,
                        0.493611, 0.488812, 12.9604, -14.5077,
                        0.393562, 0.672107, 0.409583, 0.342918, 0.645929, 0.612576, 0.257499, 0.650186, 0.606429, 0.259876);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.130726E+00, 9.611190E-01, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {3.290172E-01, 1.143624E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.130726E+00, 9.611190E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 3.290172E-01, 1.143624E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 7.159959E-01, 7.147703E-01,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -6.628140E-01, 7.027143E-01,},};

                // TW correlation matrix
                TWGamma = 1.0869501;
                TWCorrelationMatrix = new double[][]{
                    {2.316969E-12, 2.528609E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {2.528609E-12, 2.864272E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.901051E-12, 1.873160E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.873160E-12, 1.972423E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.449190E-12, 4.277394E-13,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 4.277394E-13, 5.516647E-13,},};
            }
        }});

        // NCELLS 0 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0 1 0 0 1 0 0 1 0 0
        //System.out.println("NCELLS no TTF m=0");
        // 3: no TTF, m=0
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "no TTF, m=0";
                probe = setupOpenXALProbe(78e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(4.025e8, 0, 3, 0.5, 5.27924e+06, -72.9826, 31, 0,
                        0.493611, 0.488812, 12.9359, -14.4824,
                        0, 1, 0, 0, 1, 0, 0, 1, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.257410E+00, 1.208990E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {4.883428E-01, 1.252919E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.257410E+00, 1.208990E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 4.883428E-01, 1.252919E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 5.263582E-01, 7.847883E-01,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -9.061522E-01, 5.203913E-01,},};

                // TW correlation matrix
                TWGamma = 1.0855737;
                TWCorrelationMatrix = new double[][]{
                    {3.576301E-12, 3.492566E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {3.492566E-12, 3.479751E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.854732E-12, 2.593177E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.593177E-12, 2.441401E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.371037E-12, 9.779707E-14,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 9.779707E-14, 4.641737E-13,},};
            }
        }});

        // NCELLS 1 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0 1 0 0 1 0 0 1 0 0
//		   NCELLS 1 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0 1 0 0 1 0 0 1 0 0
        //System.out.println("NCELLS no TTF m=1");
        // 4: no TTF, m=1
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "no TTF, m=1";
                probe = setupOpenXALProbe(78e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(4.025e8, 1, 3, 0.5, 5.27924e+06, -72.9826, 31, 0,
                        0.493611, 0.488812, 12.9359, -14.4824,
                        0, 1, 0, 0, 1, 0, 0, 1, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.030491E+00, 5.613739E-01, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.162076E-01, 1.030042E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.030491E+00, 5.613739E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.162076E-01, 1.030042E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 9.348819E-01, 4.683763E-01,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -2.666860E-01, 9.319993E-01,},};

                // TW correlation matrix
                TWGamma = 1.0837394;
                TWCorrelationMatrix = new double[][]{
                    {9.044082E-13, 1.345775E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.345775E-12, 2.281448E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 8.412852E-13, 1.015943E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.015943E-12, 1.524704E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.196532E-12, 7.748882E-13,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 7.748882E-13, 1.037650E-12,},};
            }
        }});

        // NCELLS 2 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0 1 0 0 1 0 0 1 0 0
        // System.out.println("NCELLS no TTF m=2");
        // 5: no TTF, m=2
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "no TTF, m=2";
                probe = setupOpenXALProbe(78e6, frequency, current);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(4.025e8, 2, 3, 0.5, 5.27924e+06, -72.9826, 31, 0,
                        0.493611, 0.488812, 12.9359, -14.4824,
                        0, 1, 0, 0, 1, 0, 0, 1, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.172099E+00, 9.733193E-01, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {3.930089E-01, 1.169109E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.172099E+00, 9.733193E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 3.930089E-01, 1.169109E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 6.690411E-01, 7.089752E-01,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -7.676433E-01, 6.629793E-01,},};

                // TW correlation matrix
                TWGamma = 1.0851165;
                TWCorrelationMatrix = new double[][]{
                    {2.388548E-12, 2.634775E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {2.634775E-12, 3.010214E-12, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.970705E-12, 1.968436E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.968436E-12, 2.091177E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.367244E-12, 3.045204E-13,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 3.045204E-13, 5.288544E-13,},};
            }
        }});

        // NCELLS 0 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0 1 0 0 1 0 0 1 0 0
        //System.out.println("NCELLS no TTF m=0 spacecharge I=30mA");
        // 6: spacecharge no TTF, m=0
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "space charge no TTF, m=0";
                probe = setupOpenXALProbe2(78e6, frequency, 30e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(4.025e8, 0, 3, 0.5, 5.27924e+06, -72.9826, 31, 0,
                        0.493611, 0.488812, 12.9359, -14.4824,
                        0, 1, 0, 0, 1, 0, 0, 1, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.257410E+00, 1.208990E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {4.883428E-01, 1.252919E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.257410E+00, 1.208990E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 4.883428E-01, 1.252919E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 5.263582E-01, 7.847883E-01,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -9.061522E-01, 5.203913E-01,},};

                // TW correlation matrix
                TWGamma = 1.08557373726;
                TWCorrelationMatrix = new double[][]{
                    {4.947803E-06, 5.093684E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {5.093684E-06, 5.293710E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 4.157164E-06, 4.063692E-06, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 4.063692E-06, 4.031250E-06, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 2.007642E-06, 4.754141E-07,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 4.754141E-07, 4.248036E-07,},};
                CMerrTolerance = 1e-4;
            }
        }});

        // NCELLS 0 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0 1 0 0 1 0 0 1 0 0
        //System.out.println("NCELLS no TTF m=0 spacecharge I=30mA E=200MeV");
        // 7: spacecharge no TTF, m=0, E=200MeV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "space charge no TTF, m=0";
                probe = setupOpenXALProbe2(200e6, frequency, 30e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(4.025e8, 0, 3, 0.5, 5.27924e+06, -72.9826, 31, 0,
                        0.493611, 0.488812, 12.9359, -14.4824,
                        0, 1, 0, 0, 1, 0, 0, 1, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.054934E+00, 1.136001E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {1.043597E-01, 1.054399E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.054934E+00, 1.136001E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.043597E-01, 1.054399E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 8.868437E-01, 7.255494E-01,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -2.921490E-01, 8.815571E-01,},};

                // TW correlation matrix
                TWGamma = 1.2156000;
                TWCorrelationMatrix = new double[][]{
                    {2.399757E-06, 2.179862E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {2.179862E-06, 2.018513E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.954075E-06, 1.650279E-06, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.650279E-06, 1.440555E-06, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.737803E-06, 1.350780E-06,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.350780E-06, 1.262041E-06,},};
            }
        }});

        // NCELLS m=0, spacecharge I=30mA, E=200MeV
        // NCELLS 0 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0.386525 0.664594 0.423349 0.350508 0.634734 0.628339 0.249724 0.639103 0.622128 0.25257
        // 8: spacecharge, m=0, E=200MeV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "space charge, m=0";
                probe = setupOpenXALProbe2(200e6, frequency, 30e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(4.025e8, 0, 3, 0.5, 5.27924e+06, -72.9826, 31, 0,
                        0.493611, 0.488812, 12.9359, -14.4824,
                        0.386525, 0.664594, 0.423349, 0.350508, 0.634734, 0.628339, 0.249724, 0.639103, 0.622128, 0.25257);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.038071E+00, 1.131049E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {7.445500E-02, 1.040152E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.038071E+00, 1.131049E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 7.445500E-02, 1.040152E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 9.187774E-01, 7.344914E-01,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -2.116106E-01, 9.143813E-01,},};

                // TW correlation matrix
                TWGamma = 1.2149029;
                TWCorrelationMatrix = new double[][]{
                    {2.375471E-06, 2.137272E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {2.137272E-06, 1.961887E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.930635E-06, 1.612455E-06, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.612455E-06, 1.394291E-06, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.806527E-06, 1.478310E-06,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.478310E-06, 1.414474E-06,},};
                TMerrTolerance = 1e-4;
                CMerrTolerance = 2e-4;
            }
        }});

        // NCELLS m=0, spacecharge I=30mA, E=3MeV, bunch freq=352.21MHz, RF freq=704.42MHz
        // NCELLS 0 3 0.5 5.27924e+06 -72.9826 31 0 0.493611 0.488812 12.9359 -14.4824 0.386525 0.664594 0.423349 0.350508 0.634734 0.628339 0.249724 0.639103 0.622128 0.25257
        // 9: spacecharge, m=0, E=3MeV
        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "space charge, m=0";
                probe = setupOpenXALProbe2(78e6, 352.21e6, 30e-3);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(704.42e6, 0, 3, 0.5, 5.27924e+06, -72.9826, 31, 0,
                        0.493611, 0.488812, 12.9359, -14.4824,
                        0.386525, 0.664594, 0.423349, 0.350508, 0.634734, 0.628339, 0.249724, 0.639103, 0.622128, 0.25257);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.138623E+00, 6.685086E-01, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {4.645476E-01, 1.143645E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.138623E+00, 6.685086E-01, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 4.645476E-01, 1.143645E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 7.255458E-01, 4.876514E-01,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -9.615804E-01, 7.204380E-01,},};

                // TW correlation matrix
                TWGamma = 1.0844864;
                TWCorrelationMatrix = new double[][]{
                    {1.650154E-06, 2.634045E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {2.634045E-06, 4.356034E-06, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.541750E-06, 2.207691E-06, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 2.207691E-06, 3.322307E-06, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.231034E-06, 4.863037E-07,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 4.863037E-07, 7.081241E-07,},};
            }
        }});

        tests.add(new Object[]{new SingleElementTestData() {
            {
                description = "misalignment test, m=0, dx, dy";
                probe = setupOpenXALProbe(78e6, frequency, 0.);
                elementMapping = JElsElementMapping.getInstance();
                sequence = ncells(4.025e8, 0, 3, 0.5, 5.27924e+06, -72.9826, 31, 0,
                        0.493611, 0.488812, 12.9359, -14.4824,
                        0.386525, 0.664594, 0.423349, 0.350508, 0.634734, 0.628339, 0.249724, 0.639103, 0.622128, 0.25257, .1, .2, 0, 0, 0, 0);

                // TW transfer matrix
                TWTransferMatrix = new double[][]{
                    {1.243353E+00, 1.206697E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {4.722683E-01, 1.250967E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 1.243353E+00, 1.206697E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 4.722683E-01, 1.250967E+00, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 5.398179E-01, 7.889158E-01,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, -8.838401E-01, 5.339459E-01,},};

                // TW correlation matrix
                TWGamma = 1.0854972;
                TWCorrelationMatrix = new double[][]{
                    {3.556654E-12, 3.474525E-12, -3.846390E-23, 3.308722E-24, 0.000000E+00, 0.000000E+00,},
                    {3.474525E-12, 3.463701E-12, 3.308722E-24, -1.075335E-23, 0.000000E+00, 0.000000E+00,},
                    {-3.846390E-23, 3.308722E-24, 2.833495E-12, 2.573891E-12, 0.000000E+00, 0.000000E+00,},
                    {3.308722E-24, -1.075335E-23, 2.573891E-12, 2.424613E-12, 0.000000E+00, 0.000000E+00,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.405578E-12, 1.086873E-13,},
                    {0.000000E+00, 0.000000E+00, 0.000000E+00, 0.000000E+00, 1.086873E-13, 3.327276E-13,},};
                TWMean = new double[]{-2.4335318458711187E-2, -4.722682926617922E-2, -4.8670636917422374E-2, -9.445365853235844E-2, 0.0, 0.0, 1.0};
                CMerrTolerance = 4e-2;
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
        double amp0, ampn;
        double pos0, posn;

        amp0 = (1 + kE0Ti) * (Ti / Ts);
        ampn = (1 + kE0To) * (To / Ts);
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
                //m==2
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
