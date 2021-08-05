/*
 *
 * Quadrupole.java
 *
 * Created on March 17, 2003, 11:22 PM
 */
package xal.sim.slg;

/**
 * The quadrupole element (a thick element).
 *
 * @author wdklotz
 */
public class Quadrupole extends Element {

    private static final String TYPE = "quadrupole";

    /**
     * Creates a new instance of Quadrupole
     */
    public Quadrupole(double position, double len, String name) {
        super(name, position, len);
        handleAsThick = true;
    }

    /**
     * Creates a new instance of Quadrupole
     */
    public Quadrupole(Double position, Double len, String name) {
        this(position.doubleValue(), len.doubleValue(), name);
    }

    /**
     * Creates a new instance of Quadrupole
     */
    public Quadrupole(double position, double len) {
        this(position, len, "NQP");
    }

    /**
     * Creates a new instance of Quadrupole
     */
    public Quadrupole(Double position, Double len) {
        this(position.doubleValue(), len.doubleValue());
    }

    /*
     * Getter for the element type property.
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
