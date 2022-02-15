package xal.extension.fit.lsm;

import java.util.*;

/**
 * The data container to use inside the solver
 *
 * @author shishlo
 */
public class DataStore {

    private int nArgs = 0;
    private Vector<double[]> recordV = new Vector<>();
    private double[] xTmpArr = new double[1];

    /**
     * Constructor for the DataStore object
     */
    public DataStore() {
    }

    /**
     * Constructor for the DataStore object with initial data
     *
     * @param y The y-values array
     * @param x The x-variables two-dimensional array
     */
    public DataStore(double[] y, double[][] x) {
        init(y, null, x);
    }

    /**
     * Constructor for the DataStore object with initial data
     *
     * @param y The y-values array
     * @param yErr The y-errors values array
     * @param x The x-variables two-dimensional array
     */
    public DataStore(double[] y, double[] yErr, double[][] x) {
        init(y, yErr, x);
    }

    /**
     * Sets the data
     *
     * @param y The y-values array
     * @param x The x-variables two-dimensional array
     * @param yErr Description of the Parameter
     */
    private void init(double[] y, double[] yErr, double[][] x) {
        nArgs = x[0].length;
        double[] arr = null;
        int nd = y.length;
        for (int i = 0; i < nd; i++) {
            arr = new double[nArgs + 2];
            arr[0] = y[i];
            arr[1] = 0.;
            if (yErr != null) {
                arr[1] = Math.abs(yErr[i]);
            }
            for (int j = 0; j < nArgs; j++) {
                arr[j + 2] = x[i][j];
            }
            recordV.add(arr);
        }
    }

    /**
     * Returns the numbers of records with (y,x_arr) pairs
     *
     * @return The numbers of records with (y,x_arr) pairs
     */
    public int size() {
        return recordV.size();
    }

    /**
     * Returns number of independent variables for this storage
     *
     * @return The size of x_arr array
     */
    public int detVarsNumber() {
        return nArgs;
    }

    /**
     * Returns y value for the record with index i
     *
     * @param i The index of the record
     * @return The y value
     */
    public double getY(int i) {
        double[] arr = recordV.get(i);
        return arr[0];
    }

    /**
     * Returns y error value for the record with index i
     *
     * @param i The index of the record
     * @return The y error value
     */
    public double getErrY(int i) {
        double[] arr = recordV.get(i);
        return arr[1];
    }

    /**
     * Returns the array with x-values for the record with index i
     *
     * @param i The index of the record
     * @return The array with x-values
     */
    public double[] getArrX(int i) {
        double[] arr = recordV.get(i);
        double[] arrX = new double[nArgs];
        System.arraycopy(arr, 2, arrX, 0, nArgs);
        return arrX;
    }

    /**
     * Removes all records
     */
    public void clear() {
        nArgs = 0;
        recordV.clear();
    }

    /**
     * Adds a record to the DataStore object
     *
     * @param y The y-value
     * @param x The independent values array
     */
    public void addRecord(double y, double[] x) {
        addRecord(y, 0., x);
    }

    /**
     * Adds a record to the DataStore object
     *
     * @param y The y-value
     * @param x The independent x value
     */
    public void addRecord(double y, double x) {
        xTmpArr[0] = x;
        addRecord(y, 0., xTmpArr);
    }

    /**
     * Adds a record to the DataStore object
     *
     * @param y The y-value
     * @param yErr The y_value error
     * @param x The independent values array
     */
    public void addRecord(double y, double yErr, double[] x) {
        if (nArgs == 0) {
            nArgs = x.length;
        }

        if (nArgs == x.length && nArgs != 0) {
            double[] arr = new double[nArgs + 2];
            arr[0] = y;
            arr[1] = Math.abs(yErr);
            for (int j = 0; j < nArgs; j++) {
                arr[j + 2] = x[j];
            }
            recordV.add(arr);
        }
    }

    /**
     * Adds a record to the DataStore object
     *
     * @param y The y-value
     * @param yErr The y_value error
     * @param x The independent x value
     */
    public void addRecord(double y, double yErr, double x) {
        xTmpArr[0] = x;
        addRecord(y, yErr, xTmpArr);
    }

}
