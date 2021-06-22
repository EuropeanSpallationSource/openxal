package xal.smf.attr;



/**
 * A set of RF cavity attributes. Here's what's in it:
 *
 *  amp - the default field amplitude (in kV/m)
 *  phase - the default phase (deg)
 *  freq - the frequency (MHz)
 *  ampFactor - calibration factor for klystron amplitude to cavity field value (ratio)
 *  phaseOffset - calibration offset for beam - klystron phase
 *  TTFCoefs - coefficients of a 2nd order polynomial representing the transit time factor function T(betat) 
 *
 * @author  Nikolay Malitsky, Christopher K. Allen
 */


public class RfCavityBucket extends AttributeBucket {
    
	private static final long serialVersionUID = 1;
	
    /*
     *  Constants
     */
    
    public static final String  TYPE = "rfcavity"; 

    static final String[]       ARR_NAMES = {  "amp", 
                                                "phase",
                                                "freq",
                                                "ampFactor",
                                                "phaseOffset",
                                                "TTFCoefs",
                                                "TTFPrimeCoefs",
                                                "STFCoefs",
                                                "STFPrimeCoefs",
                                                "TTF_endCoefs",
                                                "TTFPrime_EndCoefs",
                                                "STF_endCoefs",
                                                "STFPrime_endCoefs",
                                                "structureMode",
                                                "qLoaded",
                                                "structureTTF"
    };
    
    
    /*
     *  Local Attributes
     */
    
    /** Default field amplitude (in MV/m) */
    private Attribute   attAmp;
    
    /** Default (design) cavity RF phase (deg) */
    private Attribute   attPhase;
    
    /** Design cavity resonant frequency (MHz) */
    private Attribute   attFreq;
    
    /** Calibration factor for klystron amplitude to cavity field value (ratio) */
    private Attribute   attAmpFactor;
    
    /** Calibration offset for beam-to-klystron phase */
    private Attribute   attPhaseOffset;
    
    /** quadratic fit coefficients for the transit time factor as a function of beta (constant, linear, quad) */
    private Attribute   attTTFCoefs;
    
    /** quadratic fit coefficients for the transit time factor prime as a function of beta (constant, linear, quad) */
    private Attribute   attTTFPrimeCoefs;
    
    /** quadratic fit coefficients for the "S transit time factor" as a function of beta (constant, linear, quad) */
    private Attribute   attSTFCoefs;
    
    /** quadratic fit coefficients for the "S transit time factor" prime as a function of beta (constant, linear, quad) */
    private Attribute   attSTFPrimeCoefs;
    
   /** quadratic fit coefficients for the transit time factor as a function of beta  for the end cells (constant, linear, quad) */     
    private Attribute   attTTFEndCoefs;
    
    /** quadratic fit coefficients for the transit time factor prime as a function of beta for the end cells  (constant, linear, quad) */
    private Attribute   attTTFPrimeEndCoefs;
    
    /** quadratic fit coefficients for the "S transit time factor" as a function of beta for the end cells  (constant, linear, quad) */
    private Attribute   attSTFEndCoefs;
    
    /** quadratic fit coefficients for the "S transit time factor" prime as a function of beta for the end cells (constant, linear, quad) */
    private Attribute   attSTFPrimeEndCoefs;
    
    /** flag for the structure type (0 or pi mode) (CKA pi mode has a value 1/2) */
    private Attribute attStructureMode;
    
    /** quality factor with all external contribution */
    private Attribute attQLoaded;
    
    /** TTF used in the real accelerator LLRF */
    private Attribute attStructureTTF;
    
    /*
     *  User Interface
     */
    
    
    /** Override virtual to provide type signature */
        @Override
    public String getType() { return TYPE; }
       
    
    
    
    public RfCavityBucket() {
        super();
        
        attAmp = new Attribute( 0. );
        attPhase = new Attribute(0. );
        attFreq = new Attribute(0. );
        attAmpFactor = new Attribute(1.);
        attPhaseOffset = new Attribute(0. );
        attTTFCoefs = new Attribute(new double[] {});
        attTTFPrimeCoefs = new Attribute(new double[] {});
        attSTFCoefs = new Attribute(new double[] {});
        attSTFPrimeCoefs = new Attribute(new double[] {});
        attTTFEndCoefs = new Attribute(new double[] {});
        attTTFPrimeEndCoefs = new Attribute(new double[] {});
        attSTFEndCoefs = new Attribute(new double[] {});
        attSTFPrimeEndCoefs = new Attribute(new double[] {});
        attStructureMode = new Attribute(0. );
        attQLoaded = new Attribute(0. );
        attStructureTTF = new Attribute(1. );
	
        super.registerAttribute(ARR_NAMES[0], attAmp, "Default field amplitude (in MV).");
        super.registerAttribute(ARR_NAMES[1], attPhase, "Default (design) cavity RF phase (deg).");
        super.registerAttribute(ARR_NAMES[2], attFreq, "Design cavity resonant frequency (MHz).");
        super.registerAttribute(ARR_NAMES[3], attAmpFactor, "Calibration factor for klystron amplitude to cavity field value (ratio).");
        super.registerAttribute(ARR_NAMES[4], attPhaseOffset, "Calibration offset for beam-to-klystron phase.");
        super.registerAttribute(ARR_NAMES[5], attTTFCoefs, "Quadratic fit coefficients for the transit time factor as a function of beta (constant, linear, quad).");	
        super.registerAttribute(ARR_NAMES[6], attTTFPrimeCoefs, "Quadratic fit coefficients for the transit time factor prime as a function of beta (constant, linear, quad).");
        super.registerAttribute(ARR_NAMES[7], attSTFCoefs, "Quadratic fit coefficients for the \"S transit time factor\" as a function of beta (constant, linear, quad).");	
        super.registerAttribute(ARR_NAMES[8], attSTFPrimeCoefs, "Quadratic fit coefficients for the \"S transit time factor\" prime as a function of beta (constant, linear, quad).");		
        super.registerAttribute(ARR_NAMES[9], attTTFEndCoefs, "Quadratic fit coefficients for the transit time factor as a function of beta for the end cells (constant, linear, quad).");	
        super.registerAttribute(ARR_NAMES[10], attTTFPrimeEndCoefs, "Quadratic fit coefficients for the transit time factor prime as a function of beta for the end cells (constant, linear, quad).");
        super.registerAttribute(ARR_NAMES[11], attSTFEndCoefs, "Quadratic fit coefficients for the \"S transit time factor\" as a function of beta for the end cells (constant, linear, quad).");	
        super.registerAttribute(ARR_NAMES[12], attSTFPrimeEndCoefs, "Quadratic fit coefficients for the \"S transit time factor\" prime as a function of beta for the end cells (constant, linear, quad).");
        super.registerAttribute(ARR_NAMES[13], attStructureMode, "Flag for the structure type (0 or pi mode) (CKA pi mode has a value 1/2).");
        super.registerAttribute(ARR_NAMES[14], attQLoaded, "Quality factor with all external contribution.");
        super.registerAttribute(ARR_NAMES[15], attStructureTTF, "TTF used in the real accelerator LLRF.");
    }
    
     
    public double   getAmplitude()  { return attAmp.getDouble(); }
    public double   getPhase()      { return attPhase.getDouble(); }
    public double   getFrequency()  { return attFreq.getDouble(); }
    public double   getAmpFactor()  { return attAmpFactor.getDouble(); }
    public double   getPhaseOffset(){ return attPhaseOffset.getDouble(); }
    public double []   getTTFCoefs(){ return attTTFCoefs.getArrDbl(); }
    public double []   getTTFPrimeCoefs(){ return attTTFPrimeCoefs.getArrDbl(); }
    public double []   getSTFCoefs(){ return attSTFCoefs.getArrDbl(); }
    public double []   getSTFPrimeCoefs(){ return attSTFPrimeCoefs.getArrDbl(); }  
    public double []   getTTFEndCoefs(){ return attTTFEndCoefs.getArrDbl(); }
    public double []   getTTFPrimeEndCoefs(){ return attTTFPrimeEndCoefs.getArrDbl(); }
    public double []   getSTFEndCoefs(){ return attSTFEndCoefs.getArrDbl(); }
    public double []   getSTFPrimeEndCoefs(){ return attSTFPrimeEndCoefs.getArrDbl(); } 
    public double getStructureMode() { return attStructureMode.getDouble();} 
    public double getQLoaded() {return attQLoaded.getDouble();}
    public double getStructureTTF() {return attStructureTTF.getDouble();}

    public void setAmplitude(double dblVal)  { attAmp.set(dblVal); }
    public void setPhase(double dblVal)      { attPhase.set(dblVal); }
    public void setFrequency(double dblVal)  { attFreq.set(dblVal); }
    public void setAmpFactor(double dblVal)  { attAmpFactor.set(dblVal); }
    public void setPhaseOffset(double dblVal){ attPhaseOffset.set(dblVal); }
    public void setTTFCoefs(double [] arrVal){ attTTFCoefs.set(arrVal); }
    public void setTTFPrimeCoefs(double [] arrVal){ attTTFPrimeCoefs.set(arrVal); }  
    public void setSTFCoefs(double [] arrVal){ attSTFCoefs.set(arrVal); }
    public void setSTFPrimeCoefs(double [] arrVal){ attSTFPrimeCoefs.set(arrVal);}
    public void setTTFEndCoefs(double [] arrVal){ attTTFEndCoefs.set(arrVal); }
    public void setTTFPrimeEndCoefs(double [] arrVal){ attTTFPrimeEndCoefs.set(arrVal); }  
    public void setSTFEndCoefs(double [] arrVal){ attSTFEndCoefs.set(arrVal); }
    public void setSTFPrimeEndCoefs(double [] arrVal){ attSTFPrimeEndCoefs.set(arrVal); }
    public void setStructureMode(double dblVal)  { attStructureMode.set(dblVal); }
    public void setQLoaded(double dblVal) { attQLoaded.set(dblVal); }
    public void setStructureTTF(double dblVal) { attStructureTTF.set(dblVal); }
}
