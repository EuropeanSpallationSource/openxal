/*
 * SequencerBucket.java
 *
 * Created on 5/31/2002
 */
package xal.smf.attr;

/**
 *
 * A bucket to hold Information about sequences. In particular, how long the
 * sequence is, and allowed predecessor sequences.
 *
 * @author J. Galambos
 * @version 1.0
 */
public class SequenceBucket extends AttributeBucket {

    /**
     * serialization ID
     */
    private static final long serialVersionUID = 1L;

    /*
     *  Constants
     */
    public static final String TYPE = "sequence";

    static final String[] ARR_NAMES = {"predecessors"
    };

    /*
     *  Local Attributes
     */
    private Attribute attPredecessors;

    /*
     *  User Interface
     */
    /**
     * Furnish a unique type id
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Creates new SequenceBucket
     */
    public SequenceBucket() {
        super();

        // can have at most 2 predecessors
        String[] sa = new String[2];
        attPredecessors = new Attribute(sa);

        super.registerAttribute(ARR_NAMES[0], attPredecessors, "Preceding sequences (max 2).");
    }

    public String[] getPredecessors() {
        return attPredecessors.getArrStr();
    }

    public void setPredecessors(String[] sa) {
        attPredecessors.set(sa);
    }

}
