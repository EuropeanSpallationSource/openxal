/*
 * IRfGap.java
 *
 * Created on November 4, 2002, 3:44 PM
 */

package xal.model.elem;


/**
 *  This interface defines the common properties of all RF Gap structures.
 *
 * @author  CKAllen
 */
public interface IRfGap /*extends gov.sns.xal.model.IElement*/ {
    
    
    /**
     *  Set the ETL product of the RF gap where 
     *      E is the longitudinal electric field of the gap, 
     *      T is the transit time factor of the gap,
     *      L is the length of the gap.
     *
     *  The maximum energy gain from the gap is given by qETL where q is the charge
     *  (in coulombs) of the species particle.
     *
     *  @param  dblETL  ETL product of gap (in <bold>volts</bold>).
     */
    public void setETL(double dblETL);
    
    /**
     *  Set the phase delay of the RF in gap with respect to the synchronous particle.
     *  The actual energy gain from the gap is given by qETLcos(dblPhi) where dbkPhi is 
     *  the phase delay.
     *
     *  @param  dblPhase    phase delay of the RF w.r.t. synchonouse particle (in <bold>radians</bold>).
     */
    public void setPhase(double dblPhase);
    
    /**
     *  Set the operating frequency of the RF gap.
     *
     *  @param dblFreq  frequency of RF gap (in <bold>Hertz</bold>)
     */
    public void setFrequency(double dblFreq);
    
    
    
    /**
     *  Return the ETL product of the gap, where E is the longitudinal electric field, T is the
     *  transit time factor, and L is the gap length.
     *
     *  @return     the ETL product of the gap (in <bold>volts</bold>).
     */
    public double getETL();
    
    /**
     *  Return the RF phase delay of the gap with respect to the synchonous particle.
     *
     *  @return     phase delay w.r.t. synchonous particle (in <bold>radians</bold>).
     */
    public double getPhase();
    
    /**
     *  Get the operating frequency of the RF gap.
     *
     *  @return  frequency of RF gap (in <bold>Hertz</bold>)
     */
    public double getFrequency();

    /** Set the on accelerating field
    * @ param E - the on axis field (V/m)
    */
    public void setE0(double E);
     /** Get the on accelerating field (V/m)
    */   
    public double getE0();
    
};
