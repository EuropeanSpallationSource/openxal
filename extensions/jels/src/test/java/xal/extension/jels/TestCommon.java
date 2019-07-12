package xal.extension.jels;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.runners.Parameterized.Parameters;

import xal.extension.jels.model.elem.JElsElementMapping;
import xal.model.IComponent;
import xal.model.IElement;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.Probe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.ElementMapping;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.xml.XmlDataAdaptor;

public abstract class TestCommon {

    protected static double SpeciesCharge = 1;
    protected Probe probe;
    protected ElementMapping elementMapping;
    protected Scenario scenario;
    protected double initialEnergy;

    public TestCommon(Probe probe, ElementMapping elementMapping) {
        probe.reset();
        this.initialEnergy = probe.getKineticEnergy();
        this.probe = probe;
        this.elementMapping = elementMapping;
    }

    @Parameters
    public static Collection<Object[]> probes() {
        double energy = 2.5e9, frequency = 4.025e8, current = 0;
        return Arrays.asList(new Object[][]{
            {setupOpenXALProbe(energy, frequency, current), JElsElementMapping.getInstance()},});
    }

    public static EnvelopeProbe setupOpenXALProbe(double energy, double frequency, double current) {
        return setupOpenXALProbe(energy, frequency, current,
                new double[][]{{-0.1763, 0.2442, 0.2098},
                {-0.3247, 0.3974, 0.2091},
                {-0.5283, 0.8684, 0.2851}});
    }

    public static EnvelopeProbe setupOpenXALProbe(double energy, double frequency, double current, double twiss[][]) {
        // Envelope probe and tracker
        EnvelopeTracker envelopeTracker = new EnvelopeTracker();
        envelopeTracker.setRfGapPhaseCalculation(true);
        envelopeTracker.setUseSpacecharge(true);
        envelopeTracker.setEmittanceGrowth(false);
        envelopeTracker.setStepSize(0.004);
        envelopeTracker.setProbeUpdatePolicy(Tracker.UPDATE_EXIT);

        EnvelopeProbe envelopeProbe = new EnvelopeProbe();
        envelopeProbe.setAlgorithm(envelopeTracker);
        envelopeProbe.setSpeciesCharge(SpeciesCharge);
        envelopeProbe.setSpeciesRestEnergy(9.38272029e8);
        envelopeProbe.setKineticEnergy(energy);
        envelopeProbe.setPosition(0.0);
        envelopeProbe.setTime(0.0);

        /*
		number of particles = 1000
		beam current in A = 0
		Duty Cycle in %= 4
		normalized horizontal emittance in m*rad= 0.2098e-6
		normalized vertical emittance in m*rad = 0.2091e-6
		normalized longitudinal emittance in m*rad = 0.2851e-6
		kinetic energy in MeV = 3
		alfa x = -0.1763
		beta x in m/rad = 0.2442
		alfa y = -0.3247
		beta y in m/rad = 0.3974
		alfa z = -0.5283
		beta z in m/rad = 0.8684
         */
        double beta = envelopeProbe.getBeta();
        double gamma = envelopeProbe.getGamma();
        double beta_gamma = beta * gamma;

        envelopeProbe.initFromTwiss(new Twiss[]{new Twiss(twiss[0][0], twiss[0][1], twiss[0][2] * 1e-6 / beta_gamma),
            new Twiss(twiss[1][0], twiss[1][1], twiss[1][2] * 1e-6 / beta_gamma),
            new Twiss(twiss[2][0], twiss[2][1], twiss[2][2] * 1e-6 / beta_gamma / gamma / gamma)});
        envelopeProbe.setBeamCurrent(current);
        envelopeProbe.setBunchFrequency(frequency);//frequency

        /*CovarianceMatrix cov = ((EnvelopeProbe)envelopeProbe).getCovariance().computeCovariance();
		cov.setElem(4, 4, cov.getElem(4,4)/Math.pow(envelopeProbe.getGamma(),2));
		cov.setElem(5, 5, cov.getElem(5,5)*Math.pow(envelopeProbe.getGamma(),2));
		for (int i=0; i<6; i++) {
			System.out.println();
			for (int j=0; j<6; j++)
				System.out.printf("%E ",cov.getElem(i,j));
		}*/
        envelopeProbe.initialize();

        return envelopeProbe;
    }

    public static void saveLattice(Lattice lattice, String file) {
        try {
            LatticeXmlWriter.writeXml(lattice, file);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void saveSequence(AcceleratorSeq sequence, String file) {
        XmlDataAdaptor da = XmlDataAdaptor.newDocumentAdaptor(sequence, null);
        try {
            da.writeTo(new File(file));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void run(AcceleratorSeq sequence) throws ModelException {
        // Generates lattice from SMF accelerator
        //Scenario scenario = Scenario.newScenarioFor(sequence);
        //Scenario scenario = Scenario.newAndImprovedScenarioFor(sequence);
        try {
            scenario = Scenario.newScenarioFor(sequence, elementMapping);

            // Outputting lattice elements
            //new File("temp/").mkdirs();
            //saveLattice(scenario.getLattice(), "temp/lattice.xml");
            scenario.setProbe(probe);
            scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
            scenario.resync();

            scenario.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        // Prints transfer matrices
//        printTransferMatrices(scenario);
    }

    public void printTransferMatrices(Scenario scenario) throws ModelException {
        Iterator<IComponent> it = scenario.getLattice().globalIterator();
        PhaseMap pm = PhaseMap.identity();
        while (it.hasNext()) {
            IComponent comp = it.next();
            if (comp instanceof IElement) {
                IElement el = (IElement) comp;
                System.out.println(el.transferMap(probe, el.getLength()).getFirstOrder().toStringMatrix());
                pm = pm.compose(el.transferMap(probe, el.getLength()));
                if (el instanceof xal.model.elem.IdealRfGap) {
                    xal.model.elem.IdealRfGap gap = (xal.model.elem.IdealRfGap) el;
                    System.out.printf("gap phase=%f E0TL=%E\n", gap.getPhase() * 180. / Math.PI, gap.getETL());
                }
                if (el instanceof xal.extension.jels.model.elem.IdealRfGap) {
                    xal.extension.jels.model.elem.IdealRfGap gap = (xal.extension.jels.model.elem.IdealRfGap) el;
                    System.out.printf("gap phase=%f E0TL=%E\n", Math.IEEEremainder(gap.getPhase() * 180. / Math.PI, 360), gap.getETL());
                }
            }
        }
        PrintWriter pw = new PrintWriter(System.out);
        pm.getFirstOrder().print(pw);
        pw.flush();
    }

    public void checkTWTransferMatrix(double T[][], double errTolerance) throws ModelException {
        Trajectory<EnvelopeProbeState> trajectory = probe.getTrajectory();

        Iterator<EnvelopeProbeState> it = trajectory.stateIterator();

        Probe<EnvelopeProbeState> probe = this.probe.copy();

        PhaseMap pm = PhaseMap.identity();
        pm = new PhaseMap(trajectory.finalState().getResponseMatrixNoSpaceCharge());

        ROpenXal2TW(trajectory.initialState().getGamma(), trajectory.finalState().getGamma(), pm);

        double T77[][] = new double[7][7];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                T77[i][j] = T[i][j];
            }
        }
        PhaseMatrix pm77 = new PhaseMatrix(T77);
        double n = pm.getFirstOrder().minus(pm77).norm2() / pm77.norm2();
        //pm.getFirstOrder().minus(new PhaseMatrix(new Matrix(T77))).print();

        System.out.printf("TW transfer matrix diff: %E\n", n);
        if (n >= errTolerance) {
            for (int i = 0; i < 6; i++) {
                System.out.printf("{");
                for (int j = 0; j < 6; j++) {
                    System.out.printf("%E,\t", pm.getFirstOrder().getElem(i, j));
                }
                System.out.printf("},\n");
            }
            System.out.printf("};\nTW transfer matrix: \n");
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    System.out.printf("%E ", pm77.getElem(i, j));
                }
                System.out.printf("\n");
            }
        }

        Assert.assertTrue("TW transfer matrix", n < errTolerance);
    }

    private void ROpenXal2TW(double gamma_start, double gamma_end, PhaseMap pm) {
        PhaseMatrix r = pm.getFirstOrder();

        for (int i = 0; i < 6; i++) {
            r.setElem(i, 5, r.getElem(i, 5) / gamma_start / gamma_start);
            r.setElem(5, i, r.getElem(5, i) * gamma_end * gamma_end);
        }
        pm.setLinearPart(r);
    }

    private double tr(double x, double y) {
        //return Math.signum(x-y)*Math.pow(10, (int)Math.log10(Math.abs((x-y)/x)));
        return (x - y) / y;
    }

    protected void checkTWResults(double gammaTw, double[][] centCovTw66, double errTolerance) {
        checkTWResults(gammaTw, centCovTw66, new double[]{0., 0., 0., 0., 0., 0.}, errTolerance);
    }

    protected void checkTWResults(double gammaTw, double[][] centCovTw66, double[] meanTw6, double errTolerance) {

        System.out.printf("TW gamma diff: %.2g\n", tr(probe.getGamma(), gammaTw));
        System.out.printf("OX gamma: %.12g\n", probe.getGamma());
        Assert.assertTrue("TW gamma", tr(probe.getGamma(), gammaTw) < errTolerance);

        // transform cov
        for (int i = 0; i < 6; i++) {
            centCovTw66[i][5] /= gammaTw * gammaTw;
            centCovTw66[5][i] /= gammaTw * gammaTw;
            meanTw6[i] *= 1e-3;
        }

        double centCovTw77[][] = new double[7][7];
        double meanTw7[] = new double[7];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                centCovTw77[i][j] = centCovTw66[i][j];
            }
        }
        for (int i = 0; i < 6; i++) {
            meanTw7[i] = meanTw6[i];
        }
        meanTw7[6] = 1.0;

        CovarianceMatrix centCovOx = ((EnvelopeProbe) probe).getCovariance().computeCentralCovariance();
        PhaseVector meanOx = ((EnvelopeProbe) probe).getCovariance().getMean();
        PhaseMatrix centCovTw = new PhaseMatrix(centCovTw77);
        PhaseVector meanTw = new PhaseVector(meanTw7);

        PrintWriter pw = new PrintWriter(System.out);
        /*centCovOx.print(pw);
		meanOx.print(pw);
		meanOx.minus(meanTw).print(pw);
		pw.flush();*/
        //centCovOx.print(pw);
        //	centCovTw.print(pw);
        pw.flush();
        double n = centCovOx.minus(centCovTw).norm2() / centCovTw.norm2();
        double n2 = meanOx.minus(meanTw).norm2();
        // Dividing by the Open XAL value because the TW value is set to zero by default.
        if (meanOx.norm2() > 0.) {
            n2 /= meanOx.norm2();
        }
        double aux;
        if (n >= errTolerance) {
            for (int i = 0; i < 6; i++) {
                System.out.printf("{");
                for (int j = 0; j < 6; j++) {
                    aux = 1;
                    if (j == 5) {
                        aux *= gammaTw * gammaTw;
                    }
                    if (i == 5) {
                        aux *= gammaTw * gammaTw;
                    }

                    System.out.printf("%E,\t", aux * centCovOx.getElem(i, j));
                }
                System.out.printf("},\n");
            }
            System.out.printf("};\nTW covariance matrix: \n");
            for (int i = 0; i < 6; i++) {
                System.out.printf("{");
                for (int j = 0; j < 6; j++) {
                    aux = 1;
                    if (j == 5) {
                        aux *= gammaTw * gammaTw;
                    }
                    if (i == 5) {
                        aux *= gammaTw * gammaTw;
                    }
                    System.out.printf("%E ", aux * centCovTw.getElem(i, j));
                }
                System.out.printf("},\n");
            }
        }

        if (n2 >= errTolerance) {
            System.out.printf("TW mean = %s\n", meanTw.toString());
            System.out.printf("OX mean = %s\n", meanOx.toString());
        }
        //pm.getFirstOrder().minus(new PhaseMatrix(new Matrix(T77))).print();
        System.out.printf("TW cov matrix diff: %E\n", n);
        System.out.printf("TW mean diff: %E\n", n2);
        Assert.assertTrue("TW cov matrix", n < errTolerance);
        Assert.assertTrue("TW mean", n2 < errTolerance);
    }

}
