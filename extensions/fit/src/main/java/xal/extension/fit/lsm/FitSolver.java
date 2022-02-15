package xal.extension.fit.lsm;

/**
 * The interface for fitting solvers.
 *
 * @author shishlo
 */
public interface FitSolver {

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
    public boolean solve(DataStore ds, ModelFunction mf,
            double[] iniArr, double[] errIniArr,
            boolean[] useArr);

}
