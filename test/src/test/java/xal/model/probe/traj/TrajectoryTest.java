package xal.model.probe.traj;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * class comment
 *
 * @author Craig McChesney
 * @version $id:
 *
 * @deprecated Replaced by TestTrajectory
 */
@Deprecated
public class TrajectoryTest extends TestCase {

    /**
     * JUnit 3 test suite entry point.
     *
     * @param args command line arguments (not used)
     *
     * @author Christopher K. Allen
     * @since Apr 19, 2011
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Convenience method creating a new <code>Test</code> object initialized to
     * this class type.
     *
     * @return the object <code>new TestSuite(Trajectory
     *
     * @author Christopher K. Allen
     * @since Apr 19, 2011
     */
    public static Test suite() {
        return new TestSuite(TrajectoryTest.class);
    }

    /**
     * Test requesting states from a trajectory object.
     *
     * @author Christopher K. Allen
     * @since Apr 19, 2011
     */
    public void testTrajectoryQueries() {
    }

}
