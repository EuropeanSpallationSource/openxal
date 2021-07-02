package xal.extension.widgets.plot;

import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * This class is a container class for data used in the FunctionGraphsJPanel class.
 * This class can produce synchronous unwrapped phase graphics data.
 * It means +-2*PI will be added to the Y-value of the external graph data container
 * to provide smooth variation of phase. 
 *
 * @version 1.0
 * @author  A. Shishlo
 */
public class UnwrappedGeneratorGraphData extends BasicGraphData {

    private static final Logger LOGGER = Logger.getLogger(UnwrappedGeneratorGraphData.class.getName());

    private final Object extUnwrappedDataLock = new Object();

    protected BasicGraphData extUnwrappedData = null;

    /**
     * data set constructor
     */
    public UnwrappedGeneratorGraphData() {
        nInterpPoints = 200;
        init(50, nInterpPoints);
    }

    /**
     * data set constructor with defined initial capacity for number of (x,y)
     * points and interpolated points
     */
    public UnwrappedGeneratorGraphData(int nPoint, int nInterpPoints) {
        init(nPoint, nInterpPoints);
    }

    /**
     * set the external data container for unwrapped data
     */
    public void setExtUnwrappedContainer(BasicGraphData extUnwrappedData) {
        this.extUnwrappedData = extUnwrappedData;
        extUnwrappedData.removeAllPoints();
    }

    /**
     * add (x,y, error of y) point to the data set and an unwrapped data point
     * to the external container
     */
    @Override
    public void addPoint(double x, double y, double err) {
        super.addPoint(x, y, err);
        double yUnwrapped = y;
        if (extUnwrappedData != null) {
            synchronized (extUnwrappedDataLock) {
                int nP = extUnwrappedData.getNumbOfPoints();
                if (nP != 0) {
                    double yIn = extUnwrappedData.getY(nP - 1);
                    if (x >= extUnwrappedData.getMaxX()) {
                        yUnwrapped = unwrap(y, yIn);
                    } else if (x <= extUnwrappedData.getMinX()) {
                        yIn = extUnwrappedData.getY(0);
                        yUnwrapped = unwrap(y, yIn);
                    } else {
                        int i = 0;
                        while (i < nP && x > extUnwrappedData.getX(i)) {
                            i++;
                        }
                        yUnwrapped = unwrap(y, extUnwrappedData.getY(i));
                    }
                }
            }
            extUnwrappedData.addPoint(x, yUnwrapped, err);
        }
    }

    /**
     * this method finds +-2*PI to produce the nearest points
     */
    protected double unwrap(double y, double yIn) {
        if (y == yIn) {
            return y;
        }
        int n = 0;
        double diff = yIn - y;
        double diffMin = Math.abs(diff);
        double sign = diff / diffMin;
        int nCurr = n + 1;
        double diff_min_curr = Math.abs(y + sign * nCurr * 360. - yIn);
        while (diff_min_curr < diffMin) {
            n = nCurr;
            diffMin = Math.abs(y + sign * n * 360. - yIn);
            nCurr++;
            diff_min_curr = Math.abs(y + sign * nCurr * 360. - yIn);
        }
        return (y + sign * n * 360.);
    }

    /**
     * remove all points from the data set
     */
    @Override
    public void removeAllPoints() {
        super.removeAllPoints();
        if (extUnwrappedData != null) {
            extUnwrappedData.removeAllPoints();
        }
    }

    //----------------------------------------
    //MAIN test method for debugging
    //----------------------------------------
    public static void main(String args[]) {
        BasicGraphData unwrupped = new BasicGraphData();
        UnwrappedGeneratorGraphData generator = new UnwrappedGeneratorGraphData();
        generator.setExtUnwrappedContainer(unwrupped);

        generator.addPoint(0., 20., 0.0);
        generator.addPoint(1., 10., 0.0);
        generator.addPoint(2., 5., 0.0);

        generator.addPoint(3., 355., 0.0);
        generator.addPoint(4., 340., 0.0);
        generator.addPoint(5., 320., 0.0);

        generator.addPoint(2.5, 359., 0.0);

        generator.addPoint(-1.0, 30., 0.0);

        generator.addPoint(-2.0, -320., 0.0);

        generator.addPoint(-3.0, -680., 0.0);

        generator.addPoint(6., 360., 0.0);
        generator.addPoint(7., 380., 0.0);
        generator.addPoint(8., 450., 0.0);

        if (generator.getNumbOfPoints() != unwrupped.getNumbOfPoints()) {
            LOGGER.log(Level.INFO, "Nunber of points are different!");
            LOGGER.log(Level.INFO, "Stop.");
            System.exit(0);
        }

        for (int i = 0; i < generator.getNumbOfPoints(); i++) {
            LOGGER.log(Level.INFO, "i={0} gen x,y = {1} {2} wrap x,y = {3} {4}", new Object[]{i, generator.getX(i), generator.getY(i), unwrupped.getX(i), unwrupped.getY(i)});
        }

    }
}
