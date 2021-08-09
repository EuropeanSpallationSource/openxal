package xal.extension.fit.lsm;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.tools.ArrayMath;

/**
 * The least square method solver
 *
 * @author shishlo
 */
public class SolverLSM implements FitSolver {

    private static final Logger LOGGER = Logger.getLogger(SolverLSM.class.getName());

    private double[] a = new double[0];
    private int[] indArr = new int[0];
    private double[] errArr = new double[0];

    private double[][] atwa = new double[0][0];

    private double[] atwy = new double[0];

    private double[] w = new double[0];

    /**
     * Solve the fitting problem.
     *
     * @param ds The data for fitting.
     * @param iniArr The initial values of the parameters.
     * @param errIniArr The parameter values' errors.
     * @param useArr The mask array specifying if the parameter will be used in
     * fitting.
     * @param mf The model function
     * @return The boolean value specifying success of fitting.
     */
    @Override
    public boolean solve(DataStore ds, ModelFunction mf,
            double[] iniArr, double[] errIniArr,
            boolean[] useArr) {

        int na = 0;
        for (int i = 0; i < iniArr.length; i++) {
            if (useArr[i]) {
                na++;
            }
        }

        if (na != a.length) {
            a = new double[na];
            indArr = new int[na];
            errArr = new double[na];
            atwa = new double[na][na];
            atwy = new double[na];
        }

        int count = 0;
        for (int i = 0; i < iniArr.length; i++) {
            errIniArr[i] = 0.;
            if (useArr[i]) {
                a[count] = iniArr[i];
                indArr[count] = i;
                errArr[count] = 0.;
                count++;
            }
        }

        int nD = ds.size();
        if (nD < na) {
            return false;
        }

        if (nD != w.length) {
            w = new double[nD];
        }

        for (int i = 0; i < nD; i++) {
            w[i] = 1.0;
        }

        boolean errExist = true;

        for (int i = 0; i < nD; i++) {
            if (ds.getErrY(i) <= 0.) {
                errExist = false;
                break;
            }
        }

        if (errExist) {
            for (int i = 0; i < nD; i++) {
                w[i] = 1. / (ds.getErrY(i) * ds.getErrY(i));
            }
        }

        //calculation ATWY
        for (int i = 0; i < na; i++) {
            atwy[i] = 0.;
            for (int j = 0; j < nD; j++) {
                atwy[i] += mf.getDerivative(ds.getArrX(j), iniArr, indArr[i])
                        * w[j]
                        * (ds.getY(j) - mf.getValue(ds.getArrX(j), iniArr));
            }
        }

        //calculation ATWA
        for (int i = 0; i < na; i++) {
            for (int k = 0; k < na; k++) {
                atwa[i][k] = 0.;
                for (int j = 0; j < nD; j++) {
                    atwa[i][k] += mf.getDerivative(ds.getArrX(j), iniArr, indArr[i])
                            * mf.getDerivative(ds.getArrX(j), iniArr, indArr[k])
                            * w[j];
                }
            }
        }

        boolean res = ArrayMath.invertMatrix(atwa);
        if (!res) {
            return false;
        }

        for (int i = 0; i < na; i++) {
            for (int k = 0; k < na; k++) {
                a[i] += atwa[i][k] * atwy[k];
            }
        }

        for (int i = 0; i < na; i++) {
            iniArr[indArr[i]] = a[i];
            errIniArr[indArr[i]] = Math.sqrt(Math.abs(atwa[i][i]));
        }

        if (!errExist) {
            double y2Avg = 0.;
            double yT;
            double yA;
            for (int j = 0; j < nD; j++) {
                yA = mf.getValue(ds.getArrX(j), iniArr);
                yT = ds.getY(j);
                y2Avg += (yA - yT) * (yA - yT);
            }
            double err = 0.;
            if (nD != na) {
                err = y2Avg / (nD - na);
            }
            err = Math.sqrt(Math.abs(err));
            for (int i = 0; i < na; i++) {
                errIniArr[indArr[i]] *= err;
            }
        }
        return true;
    }

    /**
     * MAIN for debugging
     *
     * @param args The array of strings as parameters
     */
    public static void main(String[] args) {

        ModelFunction1D mf
                = new ModelFunction1D() {

            @Override
            public double getValue(double x, double[] a) {
                double res = 0.;
                double xPow = 1.;
                for (int i = 0; i < a.length; i++) {
                    res += xPow * a[i];
                    xPow *= x;
                }
                return res;
            }

            @Override
            public double getDerivative(double x, double[] a, int aIndex) {
                double res = 1.;
                for (int i = 0; i < aIndex; i++) {
                    res *= x;
                }
                return res;
            }
        };

        int nPoints = 11;

        double[] yArr = new double[nPoints];
        double[] yErrArr = new double[nPoints];
        double[][] xArr = new double[nPoints][1];
        double z;
        for (int i = 0; i < nPoints; i++) {
            z = i + 1.;
            xArr[i][0] = z;
            yArr[i] = 1.0 + z + z * z + z * z * z;
            yErrArr[i] = 1.0;
        }

        double[] a = new double[]{0.3, 1.0, 0.3, 0.3};

        double[] aErr = new double[]{0.0, 0.0, 0.0, 0.0};

        boolean[] mask = new boolean[]{true, true, true, true};

        DataStore ds = new DataStore(yArr, yErrArr, xArr);

        SolverLSM solver = new SolverLSM();

        LOGGER.log(Level.INFO, "======BEFORE=========");

        for (int i = 0; i < a.length; i++) {
            LOGGER.log(Level.INFO, "i={0} a={1} +- {2}", new Object[]{i, a[i], aErr[i]});
        }
        LOGGER.log(Level.INFO, "======START Solver=======");

        boolean res = solver.solve(ds, mf, a, aErr, mask);

        LOGGER.log(Level.INFO, "sucess ={0}", res);

        for (int i = 0; i < a.length; i++) {
            LOGGER.log(Level.INFO, "i={0} a={1} +- {2}", new Object[]{i, a[i], aErr[i]});
        }
        LOGGER.log(Level.INFO, "======STOP=======");

        a[0] = 1.;
        a[1] = 1.;
        a[2] = 1.;
        a[3] = 1.;
        LOGGER.log(Level.INFO, "  x        y          y_appr   ");
        for (int i = 0; i < xArr.length; i++) {
            LOGGER.log(Level.INFO, " {0}  {1}  {2}", new Object[]{xArr[i][0], yArr[i], mf.getValue(xArr[i][0], a)});
        }
    }
}
