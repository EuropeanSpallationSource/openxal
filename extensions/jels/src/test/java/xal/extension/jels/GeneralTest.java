package xal.extension.jels;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import xal.model.IElement;

import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.xml.ParsingException;
import xal.model.xml.ProbeXmlParser;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;

/**
 * Runs a battery of test located in resources folder.
 *
 * @author Ivo List <ivo.list@cosylab.com>
 */
@RunWith(Parameterized.class)
public class GeneralTest {

    /**
     * Describes Openxal, Tracewin columns/functions of the results. Sets
     * allowed error on each function.
     *
     */
    static enum Column {
        POSITION(0, 1, 0.),
        GAMA_1(1, 2, 1e-3),
        RMSX(2, 9, 1e-1),
        RMSY(3, 10, 1e-1),
        RMSZ(4, 11, 1e-1),
        CENTX(5, 3, 5e-2),
        CENTXp(6, 6, 2.5e-1),
        CENTY(7, 4, 1e-1),
        CENTYp(8, 7, 2.5e-1),
        CENTZ(9, 5, 1e-1),
        CENTdpp(10, 8, 1e-1);

        int openxal;
        int tracewin;
        double allowedError;

        private Column(int openxal, int tracewin, double allowedError) {
            this.openxal = openxal;
            this.tracewin = tracewin;
            this.allowedError = allowedError;
        }
    }

    protected Probe probe;
    protected URL tracewinData;
    protected AcceleratorSeq seq;

    public GeneralTest(Probe probe, URL tracewinData, AcceleratorSeq seq) {
        this.probe = probe;
        this.tracewinData = tracewinData;
        this.seq = seq;
    }

    @Parameters
    public static Collection<Object[]> tests() {
        List<Object[]> tests = new ArrayList<>();
        int lattice = 0;
        while (GeneralTest.class.getResource("lattice" + lattice + "/main.xal") != null) {
            int test = 0;
            AcceleratorSeq seq = loadAcceleratorSequence(GeneralTest.class.getResource("lattice" + lattice + "/main.xal").toString());
            while (GeneralTest.class.getResource("lattice" + lattice + "/probe." + test + ".xml") != null) {
                Probe probe = loadProbeFromXML(GeneralTest.class.getResource("lattice" + lattice + "/probe." + test + ".xml").toString());
                tests.add(new Object[]{probe, GeneralTest.class.getResource("lattice" + lattice + "/tracewin." + test + ".out"), seq});
                test++;
            }
            lattice++;
        }
        return tests;
    }

    /**
     * Runs a test located in the resources. Each test contains probe file with
     * initial parameters and result files from TraceWin.
     *
     * @throws IOException
     * @throws ModelException
     */
    @Test
    public void runTest() throws IOException, ModelException {
        double dataTW[][] = loadTWData(tracewinData);
        double dataOX[][] = run(probe, seq);

        //saveResults(tracewinData.getFile() + ".out", dataOX, probe.getTrajectory());
        System.out.printf("%s\t", probe.getComment());
        Column[] allCols = Column.values();
        StringBuilder message = new StringBuilder();
        boolean ok = true;
        for (int j = 1; j < allCols.length; j++) {
            double e = compare(dataOX[0], dataTW[1], dataOX[allCols[j].openxal], dataTW[allCols[j].tracewin]);
            if (e >= allCols[j].allowedError) {
                message.append(allCols[j].name()).append(" ");
                ok = false;
                System.out.printf("%s: %E %c %E\n", allCols[j].name(), e, e < allCols[j].allowedError ? '<' : '>', allCols[j].allowedError);
                System.out.printf("%s: %E %E\n", allCols[j].name(), dataOX[allCols[j].openxal][dataOX[allCols[j].openxal].length - 1], dataTW[allCols[j].tracewin][dataTW[allCols[j].tracewin].length - 1]);
                System.out.printf("%E\t", e);
            }
            //System.out.printf("%E %E\n",dataOX[allCols[j].openxal][0], dataTW[allCols[j].tracewin][0]);
        }
        System.out.println();
        assertTrue(message.append("are not within the allowed error").toString(), ok);

        dataTW = null;
        dataOX = null;
        System.gc();
    }

    protected static void saveResults(String file, double[][] data) throws FileNotFoundException {
        saveResults(file, data, null);
    }

    protected static void saveResults(String file, double[][] data, Trajectory t) throws FileNotFoundException {
        Formatter f = new Formatter(file);
        for (int i = 0; i < data[0].length; i++) {
            for (int j = 0; j < data.length; j++) {
                f.format(Locale.getDefault(), "%E\t", data[j][i]);
            }
            if (t != null && t.stateAtPosition(data[0][i]) != null) {
                f.format(Locale.getDefault(), "%s\t", t.stateAtPosition(data[0][i]).getElementId());
            }
            f.format(Locale.getDefault(), "\n");
        }
        f.close();
    }

    /**
     * Loads tracewin data from file
     *
     * @param twdata path to tracewin file
     * @return data
     * @throws IOException
     */
    protected double[][] loadTWData(URL twdata) throws IOException {
        final int TWcols = 26;
        int nlines = countLines(twdata);

        BufferedReader br = new BufferedReader(new InputStreamReader(twdata.openStream()));
        //drop headers
        for (int i = 0; i < 10; i++) {
            br.readLine();
        }

        double[][] data = new double[TWcols][nlines - 10];

        int i = 0;
        for (String line; (line = br.readLine()) != null; i++) {
            String cols[] = line.split(" ", TWcols + 1);
            for (int j = 0; j < TWcols; j++) {
                data[j][i] = Double.parseDouble(cols[j]);
            }
        }
        br.close();
        return data;
    }

    /**
     * Loads OpenXal probe with initial parameters from a file
     *
     * @param file OpenXal probe file
     * @return the probe
     */
    protected static Probe loadProbeFromXML(String file) {
        try {

            Probe probe = ProbeXmlParser.parse(file);
            return probe;
        } catch (ParsingException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * Loads accelerator with given path.
     *
     * @return the accelerator
     */
    private static AcceleratorSeq loadAcceleratorSequence(String location) {
        /* Loading SMF model */
        Accelerator accelerator = XMLDataManager.acceleratorWithUrlSpec(location);

        if (accelerator == null) {
            throw new Error("Accelerator is empty. Could not load the default accelerator.");
        }

        AcceleratorSeq acceleratorSeq = accelerator.getComboSequence("MEBT-A2T");

        return acceleratorSeq;
    }

    /**
     * Runs given sequence with the give probe
     *
     * @param probe probe
     * @return results from simulation
     * @throws ModelException
     * @throws IOException
     */
    public static double[][] run(Probe probe, AcceleratorSeq sequence) throws ModelException, IOException {
        Scenario scenario = Scenario.newScenarioFor(sequence);
        scenario.setProbe(probe);

        // Setting up synchronization mode
        scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
        scenario.resync();

        // Running simulation
        scenario.run();

        // Getting results
        Trajectory trajectory = probe.getTrajectory();

        Iterator<ProbeState> iterState = trajectory.stateIterator();

        int ns = trajectory.numStates();

        double[][] dataOX = new double[Column.values().length][ns];

        int i = 0;
        while (iterState.hasNext()) {
            EnvelopeProbeState ps = (EnvelopeProbeState) iterState.next();

            Twiss[] twiss;
            twiss = ps.twissParameters();

            double beta = ps.getBeta();
            double gamma = ps.getGamma();

            dataOX[Column.POSITION.openxal][i] = ps.getPosition();
            dataOX[Column.GAMA_1.openxal][i] = ps.getGamma() - 1.;
            dataOX[Column.RMSX.openxal][i] = twiss[0].getEnvelopeRadius() * 1e3;
            dataOX[Column.RMSY.openxal][i] = twiss[1].getEnvelopeRadius() * 1e3;
            dataOX[Column.RMSZ.openxal][i] = twiss[2].getEnvelopeRadius() * 360 * ps.getBunchFrequency() / (beta * IElement.LightSpeed);

            PhaseVector mean = ps.phaseMean();
            dataOX[Column.CENTX.openxal][i] = mean.getx() * 1e3;
            dataOX[Column.CENTXp.openxal][i] = mean.getxp() * 1e3;
            dataOX[Column.CENTY.openxal][i] = mean.gety() * 1e3;
            dataOX[Column.CENTYp.openxal][i] = mean.getyp() * 1e3;
            dataOX[Column.CENTZ.openxal][i] = -mean.getz() * 360 * ps.getBunchFrequency() / (beta * IElement.LightSpeed) * Math.sqrt(1 + Math.pow(mean.getx(), 2) / 4 + Math.pow(mean.gety(), 2) / 4);
            dataOX[Column.CENTdpp.openxal][i] = mean.getzp() * gamma * gamma * gamma * beta * beta * ps.getSpeciesRestEnergy() * 1e-6;

            i = i + 1;
        }

        probe.reset();
        return dataOX;
    }

    /**
     * A helper procedure to count the lines in a file.
     *
     * @param resource the file
     * @return number of lines
     * @throws IOException
     */
    private int countLines(URL resource) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(resource.openStream()));
        int i = 0;
        while (br.readLine() != null) {
            i++;
        }
        br.close();
        return i;
    }

    /**
     * Compares two tabulated functions.
     *
     * @param xa x values of first function
     * @param xb x values of second function
     * @param ya y values of first function
     * @param yb y values of second function
     * @return returns relative error
     */
    public static double compare(double[] xa, double[] xb, double[] ya, double yb[]) {
        double d = integrateL1sup(xa, xb, ya, yb);
        double a = integrateSup(xb, yb);
        //System.out.printf("%E %E\n", d, a);
        if (a < 1e-6) {
            return d;
        }
        return d / a;
    }

    /**
     * Integrates absolute value of a tabulated function. Interpolation is from
     * left, i.e. f(x)=f(x_i) on [x_i,x_i+1).
     *
     * @param x x values of function
     * @param y y values of function
     * @return the integral
     */
    private static double integrateSup(double[] x, double[] y) {
        double I = 0.;
        for (int i = 0; i < x.length - 1; i++) {
            I += Math.abs(y[i]) * (x[i + 1] - x[i]);
        }
        return I;
    }

    /**
     * Integrates absolute difference of two tabulated functions. Interpolation
     * is from left, i.e. f(x)=f(x_i) on [x_i,x_i+1).
     *
     * @param xa x values of first function
     * @param xb x values of second function
     * @param ya y values of first function
     * @param yb y values of second function
     * @return value of the integral
     */
    private static double integrateL1sup(double[] xa, double[] xb, double[] ya, double yb[]) {
        if (xa.length == 0) {
            return integrateSup(xb, yb);
        }
        if (xb.length == 0) {
            return integrateSup(xa, ya);
        }

        double x0 = Math.min(xa[0], xb[0]);
        double f0 = ya[0];
        double g0 = yb[0];
        double I = 0.;

        for (int i = 0, j = 0; i < xa.length || j < xb.length;) {
            double x1, f1 = f0, g1 = g0;
            if (j >= xb.length) {
                x1 = xa[i];
                f1 = ya[i];
                i++;
            } else if (i >= xa.length) {
                x1 = xb[j];
                g1 = yb[j];
                j++;
            } else if (xa[i] < xb[j]) {
                x1 = xa[i];
                f1 = ya[i];
                i++;
            } else {
                x1 = xb[j];
                g1 = yb[j];
                j++;
            }
            I += Math.abs(f0 - g0) * (x1 - x0);
            f0 = f1;
            g0 = g1;
            x0 = x1;
        }
        return I;
    }

    /**
     * Integrates absolute difference of two tabulated functions. Interpolation
     * is linear.
     *
     * @param xa x values of first function
     * @param xb x values of second function
     * @param ya y values of first function
     * @param yb y values of second function
     * @return value of the integral
     */
    @SuppressWarnings("unused")
    private double integrateL1linear(double[] xa, double[] xb, double[] ya, double yb[]) {

        // merge the positions together
        double p[] = new double[xa.length + xb.length];
        for (int i = 0, j = 0; i < xa.length || j < xb.length;) {
            if (j >= xb.length) {
                p[i + j] = xa[i];
                i++;
            } else if (i >= xa.length) {
                p[i + j] = xb[j];
                j++;
            } else if (xa[i] < xb[j]) {
                p[i + j] = xa[i];
                i++;
            } else {
                p[i + j] = xb[j];
                j++;
            }
        }

        // interpolate
        double f[] = new double[p.length];
        for (int i = 0, k = 0; i < p.length; i++) {
            while (k < xa.length && p[i] >= xa[k]) {
                k++;
            }
            if (p[i] == xa[k - 1]) {
                f[i] = ya[k - 1];
            } else {
                // interpolate
                if (k >= xa.length) {
                    break;
                }
                double a0 = (p[i] - xa[k - 1]);
                double a1 = (xa[k] - p[i]);
                f[i] = (a0 * ya[k - 1] + a1 * ya[k]) / (a0 + a1);
            }
        }

        double g[] = new double[p.length];
        for (int i = 0, k = 0; i < p.length; i++) {
            while (k < xb.length && p[i] >= xb[k]) {
                k++;
            }
            if (p[i] == xb[k - 1]) {
                g[i] = yb[k - 1];
            } else {
                // interpolate
                if (k >= xb.length) {
                    break;
                }
                double a0 = (p[i] - xb[k - 1]);
                double a1 = (xb[k] - p[i]);
                g[i] = (a0 * yb[k - 1] + a1 * yb[k]) / (a0 + a1);
            }
        }

        // calculate L1
        double I = 0.;
        for (int i = 1; i < p.length; i++) {
            if ((f[i - 1] - g[i - 1]) * (f[i] - g[i]) < 0.) {
                //intersection
                double p0 = ((f[i] - g[i]) * p[i] - (f[i - 1] - g[i - 1]) * p[i - 1]) / (f[i] - f[i - 1] - g[i] + g[i - 1]);
                I += (p0 - p[i - 1]) * Math.abs(f[i - 1] - g[i - 1]) + (p[i] - p0) * Math.abs(f[i] - g[i]);
            } else {
                I += Math.abs((f[i - 1] - g[i - 1]) + (f[i] - g[i])) * (p[i] - p[i - 1]);
            }
        }
        I /= 2.;
        return I;
    }

}
