package xal.smf.attr;


/**
 * Attribute set for magnet information <br>
 *
 * len - is the effective magnetic length [m]<br>
 * dfltMagField is the default field value (T for dipole, T/m for quad, etc.)<br>
 * polarity - is the polarity flag. 1 means positive current = positive field.
 *     -1 means positive current = negative field<br>
 * multFieldNorm - is an array of the normal direction multipole components 
 *   element n is the n'th pole field level over the primary field
 *   where n= 0 for dipole, n=1 for quad, ...<br>
 * multFieldSkew - is the same as multFieldNorm, but is for the skew direction<br>
 *
 * @author  Nikolay Malitsky 
 * @author  John Galambos
 * @author  Christopher K. Allen
 * @author  Paul C. Chu
 */
public class MagnetBucket extends AttributeBucket {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

    
    /*
     *  Constants
     */
    
    private static final String     TYPE = "magnet"; 

    private static final String[]   ARR_NAMES = {  "len",      // effective length
                                                    "dfltMagFld", // default field value
                                                    "polarity", // default polarity value
                                                    "multFieldNorm",      // normal field components
                                                    "multFieldSkew",       // skew field components
                                    };
    
    
     
    
    /** Override virtual to provide type signature */
    @Override
    public String getType()         { return TYPE; }
        
    public MagnetBucket() {
        super();
        
        attLenEff  = new Attribute(0.0);
        attFldDflt = new Attribute(0.0 );
        attPolarity = new Attribute(1.0 );
        attFldNorm = new Attribute(new double[] {} );
        attFldTang = new Attribute(new double[] {} );
	
        super.registerAttribute(ARR_NAMES[0], attLenEff, "Effective length (m).");
        super.registerAttribute(ARR_NAMES[1], attFldDflt, "Design field strength (T/m^(n-1)), n=1 for dipole, 2 for quad...");
        super.registerAttribute(ARR_NAMES[2], attPolarity, "Magnet polarity ( 1 or -1).");
        super.registerAttribute(ARR_NAMES[3], attFldNorm, "Normal field multipole coefficients.");
        super.registerAttribute(ARR_NAMES[4], attFldTang, "Skew field multipole coefficients.");
    }
    
     
    /** return the magnetic length (in m) */
    public double   getEffLength()  { return attLenEff.getDouble(); }
    /** return the design magnetic field strength (in Tesla) */
    public double   getDfltField()  { return attFldDflt.getDouble(); }
    /** return the magnet polarity ( 1 or -1) */
    public double   getPolarity()   { return attPolarity.getDouble(); }
    public double[] getNormField()  { return attFldNorm.getArrDbl(); }
    public double[] getTangField()  { return attFldTang.getArrDbl(); }
    
    /** set the magnetic length (in m) 
     * @param dblVal magnetic length in meters
     */
    public void setEffLength(double dblVal)     { attLenEff.set(dblVal); }
    /** set the magnet polarity 
     * @param dblVal magnet polarity (1 or -1)
     */
    public void setPolarity(double dblVal)      { attPolarity.set(dblVal); }
    public void setNormField(double[] arrVal)   { attFldNorm.set(arrVal); }
    public void setTangField(double[] arrVal)   { attFldTang.set(arrVal); }
    public void setDfltField(double dblVal)   { attFldDflt.set(dblVal); }
    /** set the dipole rotation angle for entrance pole face (in degrees) 
     * @param dblVal dipole rotation angle for entrance pole face in degrees
     */
    
    /*
     *  Local Attributes
     */
    
    /** effective magnetic length (m) */
    private Attribute       attLenEff;            
    /**  design field strength (T/m^(n-1)), n=1 for dipole, 2 for quad,... */
    private Attribute       attFldDflt;           
    /** polarity */
    private Attribute       attPolarity;           
    /**  normal field multipole coefficients */
    private Attribute       attFldNorm;           
    /** tangential field multipole coefficients */
    private Attribute       attFldTang;

}
