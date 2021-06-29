package xal.smf.attr;

/**
 * An attribute set for displacement alignment attributes (x, y, z). These
 * alignments are offsets in the local lattice coordinate system. x is in the
 * horizontal direction y is in the vertical direction z is in the longitudinal
 * direction along the beam
 *
 * @author John Galambos, Christopher K. Allen
 * @version 1.0
 */
public class DisplaceBucket extends AttributeBucket {

    /**
     * ID for serializable version
     */
    private static final long serialVersionUID = 1L;

    /*
     *  Constants
     */
    public static final String TYPE = "displacement";

    static final String[] ARR_NAMES = {"x",
        "y",
        "z"
    };

    /*
     *  Local Attributes
     */
    /**
     * x plane offset
     */
    private Attribute attDspX;
    /**
     * y plane offset
     */
    private Attribute attDspY;
    /**
     * z plane offset
     */
    private Attribute attDspZ;

    /*
     *  User Interface
     */
    /**
     * Override virtual to provide type signature
     */
    @Override
    public String getType() {
        return TYPE;
    }

    public DisplaceBucket() {
        super();

        attDspX = new Attribute(0.0);
        attDspY = new Attribute(0.0);
        attDspZ = new Attribute(0.0);

        super.registerAttribute(ARR_NAMES[0], attDspX, "X plane offset [m].");
        super.registerAttribute(ARR_NAMES[1], attDspY, "Y plane offset [m].");
        super.registerAttribute(ARR_NAMES[2], attDspZ, "Z plane offset [m].");
    }

    /**
     * Returns the displacement offsets
     */
    public double getX() {
        return attDspX.getDouble();
    }

    public double getY() {
        return attDspY.getDouble();
    }

    public double getZ() {
        return attDspZ.getDouble();
    }

    public void setX(double dblVal) {
        attDspX.set(dblVal);
    }

    public void setY(double dblVal) {
        attDspY.set(dblVal);
    }

    public void setZ(double dblVal) {
        attDspZ.set(dblVal);
    }

}
