package xal.sim.slg;

public class EQuad extends Element {

    private static final String TYPE = "EQuad";

    /**
     * Creates a new instance of Electrostatic Quadrupole
     */
    public EQuad(double position, double len, String name) {
        super(name, position, len);
        handleAsThick = true;
    }

    /**
     * Creates a new instance of Electrostatic Quadrupole
     */
    public EQuad(Double position, Double len, String name) {
        this(position.doubleValue(), len.doubleValue(), name);
    }

    /**
     * Creates a new instance of Electrostatic Quadrupole
     */
    public EQuad(double position, double len) {
        this(position, len, "NQP");
    }

    /**
     * Creates a new instance of Electrostatic Quadrupole
     */
    public EQuad(Double position, Double len) {
        this(position.doubleValue(), len.doubleValue());
    }

    /*
     * Getter for the element type property.
     */
    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

}
