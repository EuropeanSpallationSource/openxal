/*
 * RFGap.java
 *
 * Created on March 18, 2003, 12:42 AM
 */
package xal.sim.slg;

/**
 * The RF gap element (a slim element).
 *
 * @author wdklotz
 */
public class RFGap extends ThinElement {

    private static final String TYPE = "rfgap";

    /**
     * Creates a new instance of RFGap
     */
    public RFGap(double position, double len, String name) {
        super(name, position, len);
        if (len == 0.0) {
            handleAsThick = false;
        } else {
            handleAsThick = true;
        }
    }

    /**
     * Creates a new instance of RFGap
     */
    public RFGap(double position, double len) {
        this(position, len, "RFG");
    }

    /**
     * Creates a new instance of RFGap
     */
    public RFGap(double position) {
        this(position, 0.0);
    }

    /**
     * Creates a new instance of RFGap
     */
    public RFGap(Double position, Double len, String name) {
        this(position.doubleValue(), len.doubleValue(), name);
    }

    /**
     * Creates a new instance of RFGap
     */
    public RFGap(Double position, Double len) {
        this(position.doubleValue(), len.doubleValue());
    }

    /**
     * Creates a new instance of RFGap
     */
    public RFGap(Double position) {
        this(position.doubleValue());
    }

    /**
     * Return the element type.
     */
    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toCoutString() {

        String retval = "";
        double elPos = getPosition();
        double elLen = getEffLength();
        double aStart = toAbsolutePosition(getStartPosition());
        String name = getName();
        String type = getType();
        retval += "s=" + FMT.format(aStart) + " m\t" + name + "\t" + type + " p=" + FMT.format(elPos) + " leff=" + FMT.format(elLen);
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
