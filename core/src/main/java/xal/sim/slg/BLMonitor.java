/*
 *
 * BLMonitor.java
 *
 * Created on December 11, 2003, 01:26 PM
 */
package xal.sim.slg;

/**
 * The beam current monitor (a slim element).
 *
 * @author wdklotz
 */
public class BLMonitor extends ThinElement {

    private static final String TYPE = "beamlossmonitor";

    /**
     * Creates a new instance of BLMonitor
     */
    public BLMonitor(double position, double len, String name) {
        super(name, position, len);
        // always treated as thin element
//		if (len == 0.0) {
        handleAsThick = false;
//		} else {
//			handleAsThick = true;
//		}
    }

    /**
     * Creates a new instance of BLMonitor
     */
    public BLMonitor(double position, double len) {
        this(position, len, "BLM");
    }

    /**
     * Creates a new instance of BLMonitor
     */
    public BLMonitor(double position) {
        this(position, 0.0);
    }

    /**
     * Creates a new instance of BLMonitor
     */
    public BLMonitor(Double position, Double len, String name) {
        this(position.doubleValue(), len.doubleValue(), name);
    }

    /**
     * Creates a new instance of BLMonitor
     */
    public BLMonitor(Double position, Double len) {
        this(position.doubleValue(), len.doubleValue());
    }

    /**
     * Creates a new instance of BLMonitor
     */
    public BLMonitor(Double position) {
        this(position.doubleValue());
    }

    /**
     * Return the element type.
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Returns a printable string of this element.
     */
    @Override
    public String toCoutString() {
        String retval = "";
        double elPos = getPosition();
        double elLen = getEffLength();
        double aStart = toAbsolutePosition(getStartPosition());
        String name = getName();
        String type = getType();
        retval += "s="
                + fmt.format(aStart)
                + " m\t"
                + name
                + "\t"
                + type
                + " p="
                + fmt.format(elPos)
                + " leff="
                + fmt.format(elLen);
        return retval;
    }

    /**
     * When called with a Visitor reference the implementer can either reject to
     * be visited (empty method body) or call the Visitor by passing its own
     * object reference.
     *
     * @param v the Visitor which wants to visit this object.
     */
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

}
