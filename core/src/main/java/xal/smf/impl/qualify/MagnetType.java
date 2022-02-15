/*
 * MagnetType.java
 *
 * Created on January 23, 2002, 3:49 PM
 */
package xal.smf.impl.qualify;

/**
 *
 * @author tap
 */
public interface MagnetType extends ElementType {

    /* poles */
    // orientation constants
    public static final int NO_ORIENTATION = 0;
    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;

    // magnet type constants
    public static final String DIPOLE = "D";

    public static final String QUADRUPOLE = "Q";
    public static final String QUAD = QUADRUPOLE;

    public static final String SEXTUPOLE = "S";
    public static final String SEXT = SEXTUPOLE;

    public static final String OCTUPOLE = "Oct";
    public static final String OCT = OCTUPOLE;

    public static final String SOLENOID = "SOL";
    public static final String SOL = SOLENOID;

    public static final String[] poles = {DIPOLE, QUADRUPOLE, SEXTUPOLE, OCTUPOLE};

    // public methods
    public boolean isPole(String compPole);

    public int getOrientation();

    public boolean isHorizontal();

    public boolean isVertical();

    public boolean isSkew();

    public boolean isPermanent();

    public boolean isCorrector();
}
