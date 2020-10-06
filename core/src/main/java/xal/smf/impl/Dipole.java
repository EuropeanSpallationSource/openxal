package xal.smf.impl;

import xal.smf.attr.DipoleBucket;
import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;


/**
 * The implementation of the Dipole element. This class contains
 * the basic members and methods of main dipoles. Note that
 * there are other classes (e.g. dipoleCorr) that extend this class.
 *
 * @author  J. Galambos (jdg@ornl.gov)
 */

abstract public class Dipole extends Electromagnet {
	/** type for this device */
    public static final String s_strType = MagnetType.DIPOLE;

    // static initializer
    static {
        registerType();
    }

    
    /** Register type for qualification */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( Dipole.class, s_strType, "dipole" );
    }


	/** Primary Constructor */
	public Dipole( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
                setMagBucket(new DipoleBucket());
    }

    
    /**
     * Constructor
     * @param strID the dipole's unique ID
     */
    public Dipole( final String strID )     { 
        this( strID, null );
        
    }


	/** get the type */
    public String getType() {
        return s_strType;
    }


    /**
     * Determine if this magnet has the specified pole
     * @param pole the pole against which to compare this magnet's pole
     */
    public boolean isPole( final String pole ) {
        return pole.equals( MagnetType.DIPOLE );
    }

    
    /** returns design bend angle of the dipole (deg) */
    public double getBendAngle() {
        return ((DipoleBucket)getMagBucket()).getBendAngle();
    }
    
    /**
     * Get the dipole bend magnet bending angle.
     */
    public double getDfltBendAngle() {
        return ((DipoleBucket)getMagBucket()).getBendAngle();
    }
    
    /** returns design path length in meters */
    public double getDfltPathLength() {
        return ((DipoleBucket)getMagBucket()).getPathLength();
    }
    
    /** returns dipole rotation angle for entrance pole face (deg) */
    public double getEntrRotAngle() {
        return ((DipoleBucket)getMagBucket()).getDipoleEntrRotAngle();
    }
    
    /** returns dipole rotation angle for exit pole face (deg) */
    public double getExitRotAngle() {
        return ((DipoleBucket)getMagBucket()).getDipoleExitRotAngle();
    }
    
    /** returns quadrupole component for bend dipole */
    public double getQuadComponent() {
        return ((DipoleBucket)getMagBucket()).getDipoleQuadComponent();
    }
}
