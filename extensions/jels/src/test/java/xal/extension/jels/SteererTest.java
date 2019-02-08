package xal.extension.jels;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import xal.extension.jels.smf.ESSElementFactory;

import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;
import xal.smf.ElementFactory;
import xal.smf.attr.ApertureBucket;
import xal.smf.impl.DipoleCorr;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.qualify.MagnetType;

@RunWith(Parameterized.class)
public class SteererTest extends TestCommon {

    public SteererTest(Probe probe, ElementMapping elementMapping) {
        super(probe, elementMapping);
    }

    private static double errTolerance = 1e-4;

    @Test
    public void doQuadTest() throws InstantiationException, ModelException {
        probe.reset();
        System.out.println("QUAD 70 -16 15 0 0 0 0 0");
        //QUAD 70 -16 15 0 0 0 0 0		
        AcceleratorSeq sequence = quad_steerer(70., -16., 15., 10, -20);

        run(sequence);

        //printResults();
        if (initialEnergy == 3e6) {
//            checkELSResults(7.000000E-02, new double[]{1.000780E-03, 9.345521E-04, 1.833376E-03},
//                    new double[]{3.820541E-01, 3.342766E-01, 9.435362E-01}, errTolerance);

            checkTWTransferMatrix(new double[][]{
                {+1.160625e+00, +7.370925e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                {+4.708370e+00, +1.160625e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, +8.475396e-01, +6.640505e-02, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, -4.241796e+00, +8.475396e-01, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +6.955452e-02},
                {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00},}, errTolerance);

            checkTWResults(1.003197291, new double[][]{
                {+1.001561e-06, +5.228219e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                {+5.228219e-06, +3.415331e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, +8.733876e-07, -2.953358e-06, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, -2.953358e-06, +1.780296e-05, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.361266e-06, +2.249328e-06},
                {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +2.249328e-06, +5.280827e-06}

            }, errTolerance);
        }
        if (initialEnergy == 2.5e9) {
            checkTWTransferMatrix(new double[][]{
                {+1.003555e+00, +7.008293e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                {+1.016284e-01, +1.003555e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, +9.964493e-01, +6.991713e-02, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, -1.013880e-01, +9.964493e-01, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00, +5.213029e-03},
                {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.000000e+00},}, errTolerance);

            checkTWResults(3.664409209, new double[][]{
                {+1.734644e-08, +2.979657e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                {+2.979657e-08, +2.553578e-07, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, +2.689426e-08, +2.809946e-08, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, +2.809946e-08, +1.601717e-07, +0.000000e+00, +0.000000e+00},
                {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.719022e-09, +5.106308e-08},
                {+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.106308e-08, +1.599526e-06},},
                    new double[]{4.4423, 126.998, 2.21983, 63.3862, 0, 0}, errTolerance);
            /*      dx  = 4.4423 mm
					 dx' = 126.998 mrad
					 dy  = 2.21983 mm
					 dy' = 63.3862 mrad*/
        }
    }

    /**
     *
     * @param L length
     * @param G field
     * @param R aperture
     * @param Bx field for the vertical corrector
     * @param By field for the horizontal corrector
     * @return
     */
    public AcceleratorSeq quad_steerer(double L, double G, double R, double Bx, double By) {
        ApertureBucket aper = new ApertureBucket();
        aper.setAperX(R * 1e-3);
        aper.setAperY(R * 1e-3);
        aper.setShape(ApertureBucket.iRectangle);
        AcceleratorSeq sequence = new AcceleratorSeq("QuadTest");
        Quadrupole quad = ElementFactory.createQuadrupole("quad", L * 1e-3, G * Math.signum(probe.getSpeciesCharge()), aper,
                null, L / 2. * 1e-3);
        sequence.addNode(quad);

        DipoleCorr vcorr = ESSElementFactory.createESSCorrector("VC", MagnetType.VERTICAL, L * 1e-3, new ApertureBucket(), null, L / 2. * 1e-3);
        // Setting the field
        vcorr.setDfltField(-Bx);
        sequence.addNode(vcorr);

        DipoleCorr hcorr = ESSElementFactory.createESSCorrector("HC", MagnetType.HORIZONTAL, L * 1e-3, new ApertureBucket(), null, L / 2. * 1e-3);
        // Setting the field
        hcorr.setDfltField(By);
        sequence.addNode(hcorr);

        sequence.setLength(L * 1e-3);
        return sequence;
    }
}
