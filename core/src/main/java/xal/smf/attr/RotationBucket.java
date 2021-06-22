package xal.smf.attr;

/**
 * An attribute set for rotation alignment attributes (pitch, yaw, roll).
 * These alignments are offsets  in the local lattice coordinate system.
 * pitch is the rotation about x [mrad]
 * yaw is the rotation about y [mrad]
 * roll is the rotation about z [mrad]
 *
 * @author John Galambos, Christopher K. Allen
 * @version 1.1
 */


public class RotationBucket extends AttributeBucket  {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

    
    /*
     *  Constants
     */
    
    public static final String  TYPE = "rotation"; 

    static final String[]       ARR_NAMES = {  "pitch",
                                                "yaw",
                                                "roll"
                                };
    
    
    
    /*
     *  Local Attributes
     */
    
    private Attribute       attAngPitch;      // pitch angle offset
    private Attribute       attAngYaw;        // yaw angle offset
    private Attribute       attAngRoll;       // roll angle offset

    
    /** Override virtual to provide type signature */
    @Override
    public String getType()         { return TYPE; }
    
    
    
    public RotationBucket() {
        super();
        
        attAngPitch = new Attribute(0.0);
        attAngYaw   = new Attribute(0.0);
        attAngRoll  = new Attribute(0.0);
        
        super.registerAttribute(ARR_NAMES[0], attAngPitch, "Pitch angle.");
        super.registerAttribute(ARR_NAMES[1], attAngYaw, "Yaw angle.");
        super.registerAttribute(ARR_NAMES[2], attAngRoll, "Roll angle.");
    }
    

    /*
     *  Data Query
     */
    
    public double getPitch()    { return attAngPitch.getDouble(); }
    public double getYaw()      { return attAngYaw.getDouble(); }
    public double getRoll()     { return attAngRoll.getDouble(); }
    
    
    /*
     *  Data Assignment
     */
    
    public void setPitch(double dblVal) { attAngPitch.set(dblVal); }
    public void setYaw(double dblVal)   { attAngYaw.set(dblVal); }
    public void setRoll(double dblVal)  { attAngRoll.set(dblVal); }
  
}
