package xal.extension.fit.lsm;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.tools.ArrayMath;

/**
 * The Levenberg-Marquardt fitting solver.
 *
 * @author shishlo
 */
public class SolverLM implements FitSolver {

    private static final Logger LOGGER = Logger.getLogger(SolverLM.class.getName());

    private double[] a = new double[0];
    private Solution solution = new Solution();

    //Parameters of the method
    private double factor = 10.;
    private double lambdaIni = 0.001;
    private double lambdaMax = 10000.0;
    private double epsToll = 1.0E-5;

    int iterLimit = 5;
    int totalIterLimit = 30;

    /**
     * Constructor for the SolverLM object
     */
    public SolverLM() {
    }

    /**
     * Solve the fitting problem.
     *
     * @param ds The data for fitting.
     * @param iniArr The initial values of the parameters.
     * @param errIniArr The parameter values' errors.
     * @param useArr The mask Array specifying if the parameter will be used in
     * fitting.
     * @param mf The model function
     * @return The boolean value specifying success of fitting.
     */
    @Override
    public boolean solve(DataStore ds, ModelFunction mf,
            double[] iniArr, double[] errIniArr,
            boolean[] useArr) {

        int nD = ds.size();
        int count = 0;
        for (int i = 0; i < iniArr.length; i++) {
            if (useArr[i] == true) {
                count++;
            }
        }

        if (nD < count) {
            return false;
        }

        if (iniArr.length != a.length) {
            a = new double[iniArr.length];
        }

        for (int i = 0; i < iniArr.length; i++) {
            a[i] = iniArr[i];
            errIniArr[i] = 0.;
        }

        //calc. y_abs_avg
        double yAvg = 0.;
        for (int j = 0; j < nD; j++) {
            yAvg = Math.abs(ds.getY(j));
        }
        yAvg /= nD;

        solution.init(ds, mf, a, useArr);

        double chi2ini = 0.;
        double chi2new = 0.;

        double chi2Min = 0.;

        double devIni = 0.;
        double devNew = 0.;

        boolean iStop = false;
        double lambda = lambdaIni;
        double d = 0.;

        int iter = 0;
        int iterTotal = 0;

        boolean result = true;

        while (!iStop) {
            iterTotal++;

            if (lambda > lambdaMax) {
                return result;
            }

            if (iterTotal > totalIterLimit) {
                return result;
            }

            if (!solution.solve(lambda)) {
                return false;
            }

            chi2ini = solution.getChi2ini();
            chi2new = solution.getChi2new();

            if (iterTotal == 1) {
                chi2Min = Math.min(chi2ini, chi2new);
                if (chi2ini >= chi2new) {
                    solution.setParam(iniArr);
                }
                solution.setParamErr(errIniArr);
            } else {
                if (chi2new <= chi2Min) {
                    chi2Min = chi2new;
                    solution.setParam(iniArr);
                    solution.setParamErr(errIniArr);
                }
            }

            devIni = solution.getDevAvgIni();
            devNew = solution.getDevAvgNew();

            if (yAvg > 0.) {
                d = Math.abs(devIni - devNew) / yAvg;
                if (d <= epsToll) {
                    break;
                }
            }

            if (chi2ini <= chi2new) {
                lambda *= factor;
                iter = 0;
            } else {
                result = true;
                lambda /= factor;
                solution.setParam(a);
                solution.init(ds, mf, a, useArr);
                iter++;
            }
            if (iter >= iterLimit) {
                iStop = true;
            }
        }

        return result;
    }

    /**
     * Sets the lambdaFactor attribute of the SolverLM object
     *
     * @param factor The new lambdaFactor value
     */
    public void setLambdaFactor(double factor) {
        this.factor = factor;
    }

    /**
     * Gets the lambdaFactor attribute of the SolverLM object
     *
     * @return The lambdaFactor value
     */
    public double getLambdaFactor() {
        return factor;
    }

    /**
     * Sets the lambdaIni attribute of the SolverLM object
     *
     * @param lambdaIni lambdaIni new lambdaIni value
     */
    public void setLambdaIni(double lambdaIni) {
        this.lambdaIni = lambdaIni;
    }

    /**
     * Gets the lambdaIni attribute of the SolverLM object
     *
     * @return The lambdaIni value
     */
    public double getLambdaIni() {
        return lambdaIni;
    }

    /**
     * Sets the lambdaMax attribute of the SolverLM object
     *
     * @param lambdaMax The new lambdaMax value
     */
    public void setLambdaMax(double lambdaMax) {
        this.lambdaMax = lambdaMax;
    }

    /**
     * Gets the lambdaMax attribute of the SolverLM object
     *
     * @return The lambdaMax value
     */
    public double getLambdaMax() {
        return lambdaMax;
    }

    /**
     * Sets the toll attribute of the SolverLM object
     *
     * @param epsToll The new toll value
     */
    public void setToll(double epsToll) {
        this.epsToll = epsToll;
    }

    /**
     * Gets the toll attribute of the SolverLM object
     *
     * @return The toll value
     */
    public double getToll() {
        return epsToll;
    }

    /**
     * Sets the iterLimit attribute of the SolverLM object
     *
     * @param totalIterLimit The new iterLimit value
     */
    public void setIterLimit(int totalIterLimit) {
        this.totalIterLimit = totalIterLimit;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public int getIterLimit() {
        return totalIterLimit;
    }

    /**
     * MAIN for debugging
     *
     * @param args The Array of strings as parameters
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
            public double getDerivative(double x, double[] a, int indexArr) {
                double res = 1.;
                for (int i = 0; i < indexArr; i++) {
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
        }

        double[] fitArr = new double[4];
        fitArr[0] = 0.3;
        fitArr[1] = 1.0;
        fitArr[2] = 0.3;
        fitArr[3] = 0.3;

        double[] fitErrArr = new double[4];
        fitErrArr[0] = 0.;
        fitErrArr[1] = 0.;
        fitErrArr[2] = 0.;
        fitErrArr[3] = 0.;

        boolean[] mask = new boolean[4];
        mask[0] = true;
        mask[1] = true;
        mask[2] = true;
        mask[3] = true;

        DataStore ds = new DataStore(yArr, yErrArr, xArr);

        SolverLM solver = new SolverLM();

        LOGGER.log(Level.INFO, "======BEFORE=========");

        for (int i = 0; i < fitArr.length; i++) {
            LOGGER.log(Level.INFO, "i={0} a={1} +- {2}", new Object[]{i, fitArr[i], fitErrArr[i]});
        }
        LOGGER.log(Level.INFO, "======START Solver=======");

        boolean res = solver.solve(ds, mf, fitArr, fitErrArr, mask);

        LOGGER.log(Level.INFO, "sucess ={0}", res);

        for (int i = 0; i < fitArr.length; i++) {
            LOGGER.log(Level.INFO, "i={0} a={1} +- {2}", new Object[]{i, fitArr[i], fitErrArr[i]});
        }
        LOGGER.log(Level.INFO, "======STOP=======");

        LOGGER.log(Level.INFO, "  x        y          y_appr   ");
        for (int i = 0; i < xArr.length; i++) {
            LOGGER.log(Level.INFO, " {0}  {1}  {2}", new Object[]{xArr[i][0], yArr[i], mf.getValue(xArr[i][0], fitArr)});
        }
        LOGGER.log(Level.INFO, "============");

    }

    /**
     * Auxiliary inner class.
     *
     * @author shishlo
     */
    static class Solution {

        private double[] iniArr = new double[0];
        private double[] newArr = new double[0];

        private double[] a = new double[0];
        private int[] indArr = new int[0];
        private double[] errArr = new double[0];

        private double[][] ATWAIni = new double[0][0];
        private double[][] ATWA = new double[0][0];

        private double[] ATWY = new double[0];

        private double[] W = new double[0];

        private DataStore ds = null;

        //Array for (y_exp - y_theory) Array
        private double[] dltArr = new double[0];

        private double chi2Ini = 0.;
        private double chi2New = 0.;

        private double devAvgIni = 0.;
        private double devAvgNew = 0.;

        private ModelFunction mf = null;

        private boolean errExist = false;

        /**
         * Constructor for the Solution object
         */
        Solution() {
        }

        /**
         * Initialize solution
         *
         * @param dsIn The data store
         * @param mfIn The model function
         * @param iniArrIn The initial parameters
         * @param useArr The boolean mask on parameters to use in fitting
         */
        void init(DataStore dsIn,
                ModelFunction mfIn,
                double[] iniArrIn,
                boolean[] useArr) {
            ds = dsIn;
            mf = mfIn;

            if (iniArr.length != iniArrIn.length) {
                iniArr = new double[iniArrIn.length];
                newArr = new double[iniArrIn.length];
            }

            for (int i = 0; i < iniArr.length; i++) {
                iniArr[i] = iniArrIn[i];
                newArr[i] = iniArrIn[i];
            }

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
                ATWAIni = new double[na][na];
                ATWY = new double[na];
            }

            int count = 0;
            for (int i = 0; i < iniArr.length; i++) {
                if (useArr[i] == true) {
                    a[count] = iniArr[i];
                    indArr[count] = i;
                    errArr[count] = 0.;
                    count++;
                }
            }

            int nD = ds.size();
            if (nD != W.length) {
                W = new double[nD];
                dltArr = new double[nD];
            }

            for (int i = 0; i < nD; i++) {
                W[i] = 1.0;
            }

            errExist = true;

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
            chi2Ini = 0.;
            for (int j = 0; j < nD; j++) {
                dltArr[j] = ds.getY(j) - mf.getValue(ds.getArrX(j), iniArr);
                chi2Ini += dltArr[j] * dltArr[j] / W[j];
            }

            devAvgIni = 0.;
            for (int j = 0; j < nD; j++) {
                devAvgIni += Math.abs(dltArr[j]);
            }
            devAvgIni /= nD;

            for (int i = 0; i < na; i++) {
                ATWY[i] = 0.;
                for (int j = 0; j < nD; j++) {
                    ATWY[i] += mf.getDerivative(ds.getArrX(j), iniArr, indArr[i])
                            * W[j] * dltArr[j];
                }
            }

            //calculation ATWA
            for (int i = 0; i < na; i++) {
                for (int k = 0; k < na; k++) {
                    ATWAIni[i][k] = 0.;
                    for (int j = 0; j < nD; j++) {
                        ATWAIni[i][k] += mf.getDerivative(ds.getArrX(j), iniArr, indArr[i])
                                * mf.getDerivative(ds.getArrX(j), iniArr, indArr[k])
                                * W[j];
                    }
                }
            }

        }

        /**
         * Description of the Method
         *
         * @param lambda Description of the Parameter
         * @return Description of the Return Value
         */
        boolean solve(double lambda) {
            int na = a.length;
            int nD = ds.size();

            for (int i = 0; i < na; i++) {
                for (int k = 0; k < na; k++) {
                    ATWA[i][k] = ATWAIni[i][k];
                    if (i == k) {
                        ATWA[i][k] += lambda;
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
                newArr[indArr[i]] = a[i];
            }

            chi2New = 0.;
            for (int j = 0; j < nD; j++) {
                dltArr[j] = ds.getY(j) - mf.getValue(ds.getArrX(j), newArr);
                chi2New += dltArr[j] * dltArr[j] / W[j];
            }

            devAvgNew = 0.;
            for (int j = 0; j < nD; j++) {
                devAvgNew += Math.abs(dltArr[j]);
            }
            devAvgNew /= nD;

            return true;
        }

        /**
         * Returns average deviation from initial data
         *
         * @return The deviation value
         */
        double getDevAvgIni() {
            return devAvgIni;
        }

        /**
         * Returns average deviation from initial data
         *
         * @return The deviation value
         */
        double getDevAvgNew() {
            return devAvgNew;
        }

        /**
         * Gets the chi2ini attribute of the Solution object
         *
         * @return The chi2ini value
         */
        double getChi2ini() {
            return chi2Ini;
        }

        /**
         * Gets the chi2new attribute of the Solution object
         *
         * @return The chi2new value
         */
        double getChi2new() {
            return chi2New;
        }

        /**
         * Sets the new values of parameters to the external Array.
         *
         * @param iniArrIn The external Array.
         */
        void setParam(double[] iniArrIn) {
            int na = a.length;

            for (int i = 0; i < iniArrIn.length; i++) {
                iniArrIn[i] = newArr[i];
            }
        }

        /**
         * Sets the new errors of parameters to the external Array.
         *
         * @param iniArrIn The new Array of errors values
         */
        void setParamErr(double[] iniArrIn) {
            int na = a.length;
            int nD = ds.size();

            for (int i = 0; i < iniArrIn.length; i++) {
                iniArrIn[i] = 0.;
            }

            if (nD <= (na - 1)) {
                return;
            }

            for (int i = 0; i < na; i++) {
                for (int k = 0; k < na; k++) {
                    ATWA[i][k] = ATWAIni[i][k];
                }
            }

            boolean res = ArrayMath.invertMatrix(ATWA);

            if (!res) {
                return;
            }

            if (errExist) {
                for (int i = 0; i < na; i++) {
                    iniArrIn[indArr[i]] = Math.sqrt(Math.abs(ATWA[i][i]));
                }
            } else {
                double coeff = Math.sqrt(getChi2new() / (nD - na));
                for (int i = 0; i < na; i++) {
                    iniArrIn[indArr[i]] = coeff * Math.sqrt(Math.abs(ATWA[i][i]));
                }
            }

        }

    }

}
