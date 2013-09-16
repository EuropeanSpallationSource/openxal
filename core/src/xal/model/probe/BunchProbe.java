/*
 * BunchProbe.java
 *
 * Created on November 12, 2002, 6:17 PM
 * Modifcations:
 *      11/2006 - CKA changed the primary state variables to bunch frequency Q
 *                and beam current I
 */

package xal.model.probe;


import xal.tools.math.r3.R3;

import xal.model.probe.traj.BeamTrajectory;
import xal.model.probe.traj.BunchProbeState;
import xal.model.probe.traj.ProbeState;




/**
 *  <p>
 *  Abstract base class for all probes having beam properties.  That is derived classes should
 *  represent probes with collective beam dynamics.
 *  </p>
 *  <p>
 *  <h4>Note:</h4>
 *  The bunch charge <i>Q</i> is computed from the beam current <i>I</i> and 
 *  bunch frequency <i>f</i> as 
 *  <br/>
 *  <br/>
 *  &nbsp; &nbsp;  <i>Q</i> = <i>I/f</i>
 * </p>
 * 
 * @author  Christopher K. Allen
 * @since   Nov 2, 2002
 */
public abstract class BunchProbe extends Probe {
    
    
    
    /*
     * Local Attributes
     */

    
    /** bunch frequency in Hz */
    private double  dlbFreq = 0.0;
    
    /** Beam current */
    private double  dblCurrent = 0.0;

    /** particle betatron phase with space charge */
    protected R3 vecPhsBeta;
    
//    /** Beam charge */
//    private double m_dblBmQ = 0.0;
   
    
    /*
     *  Abstract Methods
     */
    
    
//    /** 
//     *  Abstract - Returns the correlation matrix (sigma matrix) in homogeneous
//     *  phase space coordinates.
//     *
//     *  @return         <zz^T> =| <x*x>  <x*xp>  <x*y>  <x*yp>  <x*z>  <x*zp>  <x>  |
//     *                          | <xp*x> <xp*xp> <xp*y> <xp*yp> <xp*z> <xp*zp> <xp> |
//     *                            ...
//     *
//     *  @see    gov.sns.tools.beam.PhaseMatrix
//     */
//    public abstract CovarianceMatrix getCorrelation();
//    
    
    
    /*
     *  Initialization
     */
    
    
    /**
     *  Default constructor.
     * 
     *  Since BunchProbe is abstract constructor should only be calls by a derived class.
     *  Creates a new (empty) instance of BunchProbe.
     */
    protected BunchProbe()   {
        super();
        this.vecPhsBeta = R3.zero();
    }
  
    /**
     *  Copy constructor - clones the argument
     *  Since BunchProbe is abstract constructor should only be calls by a derived class.
     *
     *  @param  probe   BunchProbe object to be cloned
     */
    public BunchProbe(BunchProbe probe)   {
        super(probe);
        //Not sure what the purpose of this is
        //this.setBunchFrequency(this.getBunchFrequency());
        this.setBunchFrequency(probe.getBunchFrequency());
        this.setBeamCurrent(probe.getBeamCurrent());
        this.setBetatronPhase(new R3(probe.getBetatronPhase()));
    };        

    
    /**
     * Set the bunch arrival time frequency.
     * 
     * @param f     new bunch frequency in <b>Hz</b>
     */
    public void setBunchFrequency(double f) {
        this.dlbFreq = f;
    }
 
    /**
     *  Set the total beam current.
     * 
     * @param   I   new beam current in <bold>Amperes</bold>
     */
    public void setBeamCurrent(double I)    { 
        dblCurrent = I; 
    };
    

    /**
     * Set the betatron phase with space charge for each phase plane.
     * 
     * @param vecPhase
     *            vector (psix,psiy,psiz) of betatron phases in <b>radians </b>
     */
    public void setBetatronPhase(R3 vecPhase) {
        this.vecPhsBeta = vecPhase;
        //this.m_vecPhsBeta = new R3(vecPhase);
        // TODO - optimize the redundant copy
    }

//    /**
//     *  Set the total beam charge 
//     * 
//     *  @param  Q   beam charge in <bold>Coulombs</bold>
//     */
//    public void setBeamCharge(double Q)     { m_dblBmQ = Q; };
    

    
    
    /*
     *  Attribute Query
     */
    
    /**
     * Returns the bunch frequency, that is the frequency of 
     * the bunches need to create the beam current.
     * 
     * The bunch frequency f is computed from the beam current 
     * I and bunch charge Q as 
     *  
     *      f = I/Q
     *      
     * @return  bunch frequency in Hertz
     */
    public double getBunchFrequency()  {
        return this.dlbFreq;
    };
    
    /** 
     * Returns the total beam current 
     * 
     * @return  beam current in <b>amps</b>
     */
    public double getBeamCurrent() { 
        return dblCurrent;  
     }

    /**
     * Returns the betatron phase with space charge for all three phase planes.
     * 
     * @return vector (psix,psiy,psiz) of phases in <b>radians </b>
     */
    public R3 getBetatronPhase() {
        return this.vecPhsBeta;
    }

    
    /*
     * Computed Properties
     */
    
    /** 
     * Computes and returns the charge in each beam bunch
     * 
     * @return  beam charge in <b>coulombs</b>
     */
    public double bunchCharge() {
        if (this.getBunchFrequency() > 0.0) {
            return this.getBeamCurrent()/this.getBunchFrequency();
            
        } else {
            return 0.0;
            
        }
    }

    /** 
     * <p>
     *  Returns the generalized, three-dimensional beam perveance <i>K</i>.  
     *  This value is defined to be
     *  </p>
     *  
     *      K = (Q/4*pi*e0)*(1/gamma^3*beta^2)*(|q|/ER) 
     *  
     *  <p>
     *  where <i>Q</i> is the bunch charge, <i>e0</i> is the permittivity
     *  of free space, <i>gamma</i> is the relativitic factor, <i>beta</i> is 
     *  the normalized design velocity, <i>q</i> is the charge of the beam
     *  particles and <i>ER</i> is the rest energy of the beam partiles.
     *  </p>
     *  
     *  <p>
     *  NOTES:
     *  - The value (1/4*pi*e0) is equal to 1e-7*c^2 where <i>c</i> is the
     *  speed of light. 
     *  
     *  @return generalized beam perveance <b>Units: radians^2/meter</b>
     *  
     *  @author Christopher K. Allen
     */
    public double beamPerveance() {
        
        // Get some shorthand
        double c     = LightSpeed;
        double gamma = this.getGamma();
        double bg2   = gamma*gamma - 1.0;

        // Compute independent terms
        double  dblPermT = 1.0e-7*c*c*this.bunchCharge();
        double  dblRelaT = 1.0/(gamma*bg2);
        double  dblEnerT = Math.abs(super.getSpeciesCharge())/super.getSpeciesRestEnergy();
        
        return dblPermT*dblRelaT*dblEnerT;  
    }

    
    /*
     *  Trajectory Support
     */
    
    /**
     * Apply the contents of ProbeState to update my current state.  Subclass
     * implementations should call super.applyState to ensure superclass
     * state is applied.
     * 
     * @param state     <code>ProbeState</code> object containing new probe state data
     * 
     * @exception   IllegalArgumentException    wrong <code>ProbeState</code> subtype for this probe
     */
    @Override
    public void applyState(ProbeState state) {
        if (!(state instanceof BunchProbeState))
            throw new IllegalArgumentException("invalid probe state");
        BunchProbeState  stateBunch = (BunchProbeState)state;
        
        super.applyState(stateBunch);
        this.setBunchFrequency( stateBunch.getBunchFrequency() );
        this.setBeamCurrent( stateBunch.getBeamCurrent() );
        this.setBetatronPhase(stateBunch.getBetatronPhase());
        
//	setElapsedTime(((BunchProbeState)state).getElapsedTime());
    }

    
    @Override
    public abstract BeamTrajectory createTrajectory();
    
    @Override
    public abstract BunchProbeState createProbeState();
}





//
// Storage
//

///** 
//*  Returns the beam perveance <b>Units: radians^2/meter</b>
//*  
//*  TODO    This could be optimized (CKA)
//*/
//public double beamPerveance() {
//// double gamma = this.getGamma();
//// double bg2   = gamma*gamma - 1.0;
// 
// double  c = LightSpeed;
// double  dblPermT = 1.e-7*c*c*this.bunchCharge();
//// double  dblPermT = this.bunchCharge()/(2.0*Math.PI*Permittivity);
// double  dblRelaT = 1.0/(super.getGamma()*super.getBetaGamma()*super.getBetaGamma());
// double  dblEnerT = Math.abs(super.getSpeciesCharge())/super.getSpeciesRestEnergy();
// 
// 
// 
// return dblPermT*dblRelaT*dblEnerT;  
//}


///**
//*  Return the covariance matrix of the distribution.  Note that this can be computed
//*  from the correlation matrix in homogeneous coordinates since the mean values are 
//*  included in that case.
//*
//*  @return     <(z-<z>)*(z-<z>)^T> = <z*z^T> - <z>*<z>^T
//*/
//public CovarianceMatrix  phaseCovariance() {
// return getCorrelation().getCovariance();
//}
//
///** 
//*  Return the phase space coordinates of the centroid in homogeneous coordinates 
//*
//*  @return         <z> = (<x>, <xp>, <y>, <yp>, <z>, <zp>, 1)^T
//*/
//public PhaseVector phaseMean()  {
// return getCorrelation().getMean();
//}
//


///** return the time elapsed from the start of the probe tracking (sec) */
//public double getElapsedTime() { return elapsedTime;}
//
///** set the time elapsed from the start of the probe tracking (sec) 
//* @param time - the elapsed time (sec)
//*/
//public void setElapsedTime(double time) {elapsedTime = time; }



///** advance the time the probe has spent traveling down the beam line
//@ param the step size to advance (m)
//*/
//
//public void advanceElapsedTime(double h) {
//double deltaT;
//
//deltaT = h / (IConstants.LightSpeed * getBeta());
//setElapsedTime(getElapsedTime() + deltaT);
//}
///** update the elapsed time by a specified time increment
//* This is used in the RF gap (thin lens) kick correction.
//@ param the time amount to correct the integrated elapsed time by
//*/
//
//public void setTime(double dt) {
//setElapsedTime(getElapsedTime() + dt);
//}