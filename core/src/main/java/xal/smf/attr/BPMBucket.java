package xal.smf.attr;

/**
 * An attribute set for the BPM
 *
 * @author John Galambos,
 * @version 1.1
 */
public class BPMBucket extends AttributeBucket {

    /**
     * ID for serializable version
     */
    private static final long serialVersionUID = 1L;

    /*
     *  Constants
     */
    public static final String TYPE = "bpm";

    static final String[] ARR_NAMES = {"frequency",
        "length",
        "orientation"
    };


    /*
     *  Local Attributes
     */
    /**
     * the phase frequency (MHz)
     */
    private Attribute attFrequency;
    /**
     * stripline length (m)
     */
    private Attribute attLength;
    /**
     * leads come in up (1)or downstream (-1)
     */
    private Attribute attOrientation;

    /*
     *  User Interface
     */
    /**
     * Override virtual to provide type signature
     */
    public BPMBucket() {
        super();

        attFrequency = new Attribute(0.0);
        attLength = new Attribute(0.0);
        attOrientation = new Attribute(1);

        super.registerAttribute(ARR_NAMES[0], attFrequency, "Phase frequency (MHz).");
        super.registerAttribute(ARR_NAMES[1], attLength, "Stripline length (m)");
        super.registerAttribute(ARR_NAMES[2], attOrientation, "Leads come in up (1) or downstream (-1)");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String[] getAttrNames() {
        return ARR_NAMES;
    }

    /**
     * Returns the displacement offsets
     */
    public double getFrequency() {
        return attFrequency.getDouble();
    }

    public double getLength() {
        return attLength.getDouble();
    }

    public double getOrientation() {
        return attOrientation.getInteger();
    }

    public void setFrequency(double dblVal) {
        attFrequency.set(dblVal);
    }

    public void setLength(double dblVal) {
        attLength.set(dblVal);
    }

    public void setOrientation(int intVal) {
        attOrientation.set(intVal);
    }

}
