package xal.extension.fit.lsm;

import xal.tools.ArrayMath;

/**
 * The least square method solver
 *
 * @author shishlo
 */
public class SolverLSM implements FitSolver {

    private double[] a = new double[0];
    private int[] indArr = new int[0];
    private double[] errArr = new double[0];

    private double[][] ATWA = new double[0][0];

    private double[] ATWY = new double[0];

    private double[] W = new double[0];

    /**
     * Constructor for the SolverLSM object
     */
    public SolverLSM() {
    }

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
            if (useArr[i] == true) {
                na++;
            }
        }

        if (na != a.length) {
            a = new double[na];
            indArr = new int[na];
            errArr = new double[na];
            ATWA = new double[na][na];
            ATWY = new double[na];
        }

        int count = 0;
        for (int i = 0; i < iniArr.length; i++) {
            errIniArr[i] = 0.;
            if (useArr[i] == true) {
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

        if (nD != W.length) {
            W = new double[nD];
        }

        for (int i = 0; i < nD; i++) {
            W[i] = 1.0;
        }

        boolean errExist = true;

        for (int i = 0; i < nD; i++) {
            if (ds.getErrY(i) <= 0.) {
                errExist = false;
                break;
            }
        }

        if (errExist == true) {
            for (int i = 0; i < nD; i++) {
                W[i] = 1. / (ds.getErrY(i) * ds.getErrY(i));
            }
        }

        //calculation ATWY
        for (int i = 0; i < na; i++) {
            ATWY[i] = 0.;
            for (int j = 0; j < nD; j++) {
                ATWY[i] += mf.getDerivative(ds.getArrX(j), iniArr, indArr[i])
                        * W[j]
                        * (ds.getY(j) - mf.getValue(ds.getArrX(j), iniArr));
            }
        }

        //calculation ATWA
        for (int i = 0; i < na; i++) {
            for (int k = 0; k < na; k++) {
                ATWA[i][k] = 0.;
                for (int j = 0; j < nD; j++) {
                    ATWA[i][k] += mf.getDerivative(ds.getArrX(j), iniArr, indArr[i])
                            * mf.getDerivative(ds.getArrX(j), iniArr, indArr[k])
                            * W[j];
                }
            }
        }

        boolean res = ArrayMath.invertMatrix(ATWA);
        if (res != true) {
            return false;
        }

        for (int i = 0; i < na; i++) {
            for (int k = 0; k < na; k++) {
                a[i] += ATWA[i][k] * ATWY[k];
            }
        }

        for (int i = 0; i < na; i++) {
            iniArr[indArr[i]] = a[i];
            errIniArr[indArr[i]] = Math.sqrt(Math.abs(ATWA[i][i]));
        }

        if (errExist != true) {
            double y2Avg = 0.;
            double yT = 0.;
            double yA = 0.;
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
    public static void main(String args[]) {

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
            public double getDerivative(double x, double[] a, int a_index) {
                double res = 1.;
                for (int i = 0; i < a_index; i++) {
                    res *= x;
                }
                return res;
            }

        };

        int nPoints = 11;

        double[] yArr = new double[nPoints];
        double[] yErrArr = new double[nPoints];
        double[][] xArr = new double[nPoints][1];
        double z = 0.;
        for (int i = 0; i < nPoints; i++) {
            z = i + 1;
            xArr[i][0] = z;
            yArr[i] = 1.0 + z + z * z + z * z * z;
            yErrArr[i] = 1.0;
            //if(i%2 == 0) y_arr[i] += 1.0;
        }

        double[] a = new double[4];
        a[0] = 0.3;
        a[1] = 1.0;
        a[2] = 0.3;
        a[3] = 0.3;

        double[] aErr = new double[4];
        aErr[0] = 0.;
        aErr[1] = 0.;
        aErr[2] = 0.;
        aErr[3] = 0.;

        boolean[] mask = new boolean[4];
        mask[0] = true;
        mask[1] = true;
        mask[2] = true;
        mask[3] = true;

        DataStore ds = new DataStore(yArr, yErrArr, xArr);

        SolverLSM solver = new SolverLSM();

        System.out.println("======BEFORE=========");

        for (int i = 0; i < a.length; i++) {
            System.out.println("i=" + i + " a=" + a[i] + " +- " + aErr[i]);
        }
        System.out.println("======START Solver=======");

        boolean res = solver.solve(ds, mf, a, aErr, mask);

        System.out.println("sucess =" + res);

        for (int i = 0; i < a.length; i++) {
            System.out.println("i=" + i + " a=" + a[i] + " +- " + aErr[i]);
        }
        System.out.println("======STOP=======");

        a[0] = 1.;
        a[1] = 1.;
        a[2] = 1.;
        a[3] = 1.;
        System.out.println("  x        y          y_appr   ");
        for (int i = 0; i < xArr.length; i++) {
            System.out.println(" " + xArr[i][0] + "  "
                    + yArr[i] + "  "
                    + mf.getValue(xArr[i][0], a));
        }

    }

}
