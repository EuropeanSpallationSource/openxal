/*
 *
 * BCMonitor.java
 *
 * Created on March 18, 2003, 12:46 AM
 */
package xal.sim.slg;

/**
 * The beam current monitor (a slim element).
 *
 * @author wdklotz
 */
public class BCMonitor extends ThinElement {

    private static final String TYPE = "beamcurrentmonitor";

    /**
     * Creates a new instance of BCMonitor
     */
    public BCMonitor(double position, double len, String name) {
        super(name, position, len);
        // always treated as thin element
        handleAsThick = false;
    }

    /**
     * Creates a new instance of BCMonitor
     */
    public BCMonitor(double position, double len) {
        this(position, len, "BCM");
    }

    /**
     * Creates a new instance of BCMonitor
     */
    public BCMonitor(double position) {
        this(position, 0.0);
    }

    /**
     * Creates a new instance of BCMonitor
     */
    public BCMonitor(Double position, Double len, String name) {
        this(position.doubleValue(), len.doubleValue(), name);
    }

    /**
     * Creates a new instance of BCMonitor
     */
    public BCMonitor(Double position, Double len) {
        this(position.doubleValue(), len.doubleValue());
    }

    /**
     * Creates a new instance of BCMonitor
     */
    public BCMonitor(Double position) {
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
                + FMT.format(aStart)
                + " m\t"
                + name
                + "\t"
                + type
                + " p="
                + FMT.format(elPos)
                + " leff="
                + FMT.format(elLen);
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
