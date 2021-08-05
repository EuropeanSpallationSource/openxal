/*
 *
 * HSteerer.java
 *
 * Created on March 18, 2003, 1:01 AM
 */
package xal.sim.slg;

/**
 * The horizontal steerer element (a thin element).
 *
 * @author wdklotz
 */
public class HSteerer extends ThinElement {

    private static final String TYPE = "hsteerer";

    /**
     * Creates a new instance of HSteerer
     */
    public HSteerer(double position, double len, String name) {
        super(name, position, 0.0);
        handleAsThick = false;
    }

    /**
     * Creates a new instance of HSteerer
     */
    public HSteerer(double position, double len) {
        this(position, len, "DCH");
    }

    /**
     * Creates a new instance of HSteerer
     */
    public HSteerer(double position) {
        this(position, 0.0);
    }

    /**
     * Creates a new instance of HSteerer
     */
    public HSteerer(Double position, Double len, String name) {
        this(position.doubleValue(), len.doubleValue(), name);
    }

    /**
     * Creates a new instance of HSteerer
     */
    public HSteerer(Double position, Double len) {
        this(position.doubleValue(), len.doubleValue());
    }

    /**
     * Creates a new instance of HSteerer
     */
    public HSteerer(Double position) {
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
