package xal.smf.attr;

/**
 * An attribute set for alignment attributes (x, y, z, pitch, yaw, roll). These
 * alignments are offsets in the local lattice coordinate system. x is in the
 * horizontal direction [mm] y is in the vertical direction [mm] z is in the
 * longitudinal direction along the beam [mm] pitch is the rotation about x
 * [mrad] yaw is the rotation about y [mrad] roll is the rotation about z [mrad]
 *
 * @author John Galambos, Nikolay Malitsky, Christopher K. Allen
 * @version 1.1
 */
public class AlignmentBucket extends AttributeBucket {

    /**
     * ID for serializable version
     */
    private static final long serialVersionUID = 1L;

    /*
     *  Constants
     */
    public static final String TYPE = "align";

    static final String[] ARR_NAMES = {"x",
        "y",
        "z",
        "pitch",
        "yaw",
        "roll"
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

    /**
     * pitch angle offset
     */
    private Attribute attAngPitch;
    /**
     * yaw angle offset
     */
    private Attribute attAngYaw;
    /**
     * roll angle offset
     */
    private Attribute attAngRoll;

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

    @Override
    public String[] getAttrNames() {
        return ARR_NAMES;
    }

    public AlignmentBucket() {
        super();

        attDspX = new Attribute(0.0);
        attDspY = new Attribute(0.0);
        attDspZ = new Attribute(0.0);

        attAngPitch = new Attribute(0.0);
        attAngYaw = new Attribute(0.0);
        attAngRoll = new Attribute(0.0);

        super.registerAttribute(ARR_NAMES[0], attDspX, "X plane offset (m).");
        super.registerAttribute(ARR_NAMES[1], attDspY, "Y plane offset (m).");
        super.registerAttribute(ARR_NAMES[2], attDspZ, "Z plane offset (m).");

        super.registerAttribute(ARR_NAMES[3], attAngPitch, "Pitch angle (deg).");
        super.registerAttribute(ARR_NAMES[4], attAngYaw, "Yaw angle (deg).");
        super.registerAttribute(ARR_NAMES[5], attAngRoll, "Roll angle (deg).");
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

    /**
     * Returns the offset angles
     */
    public double getPitch() {
        return attAngPitch.getDouble();
    }

    public double getYaw() {
        return attAngYaw.getDouble();
    }

    public double getRoll() {
        return attAngRoll.getDouble();
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

    public void setPitch(double dblVal) {
        attAngPitch.set(dblVal);
    }

    public void setYaw(double dblVal) {
        attAngYaw.set(dblVal);
    }

    public void setRoll(double dblVal) {
        attAngRoll.set(dblVal);
    }

}
