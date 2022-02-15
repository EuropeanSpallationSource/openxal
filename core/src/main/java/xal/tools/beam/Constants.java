/*
 * Constants.java
 *
 * Created on January 22, 2003, 6:08 PM
 */
package xal.tools.beam;

/**
 *
 * @author Christopher Allen
 */
public final class Constants {

    private Constants() {
    }

    /*
     *  Physical Constants
     */
    /**
     * Speed of light in a vacuum (meters/second)
     */
    public static final double LIGHT_SPEED = 299792458;

    /**
     * The unit electric charge (Farads)
     */
    public static final double UNIT_CHARGE = 1.602e-19;

    /**
     * Electric permittivity of free space (Farad/meter)
     */
    public static final double PERMITTIVITY = 8.854187817e-12;

    /**
     * Magnetic permeability of free space (Henries/meter)
     */
    public static final double PERMEABILITY = 4.0 * Math.PI * 1.0e-7;

    /**
     * Rest mass of an electron (Kilograms)
     */
    public static final double ELECTRON_MASS = 9.109e-31;

    /**
     * Rest mass of a proton (Kilograms)
     */
    public static final double PROTON_MASS = 1.6762e-27;

    /**
     * Rest energy of an electron (electron volts)
     */
    public static final double ELECTRON_ENERGY = 5.11e5;

    /**
     * Rest energy of a proton (electron volts)
     */
    public static final double PROTON_ENERGY = 9.38272e8;

    /**
     * Bohr radius of a hydrogen atom (meters)
     */
    public static final double BOHR_RADIUS = 5.29177e-11;

    /**
     * "Classical" radius of a proton: radius where electrical energy equals
     * rest energy
     */
    public static final double PROTON_RADIUS = 9.1740e-19;

}
