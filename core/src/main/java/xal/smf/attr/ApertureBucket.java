/*
 * ApertureBucket.java
 *
 * Created on September 18, 2001, 1:24 PM
 */
package xal.smf.attr;

/**
 * The aperture bucket defines the inner geometry of the elements.
 * <p>
 * For elements with constant aperture, set a single value for x and y.
 * <p>
 * For elements with more complicated geometry, use the array "pos" to define
 * the apertures "x" and "y" (also arrays of the same length) at different
 * points in the element. The first and last points should correspond to the
 * ends of the element.
 *
 * @author Christopher K. Allen
 * @version 1.0
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 * @version 2.0
 */
public class ApertureBucket extends AttributeBucket {

    /**
     * ID for serializable version
     */
    private static final long serialVersionUID = 2L;

    /*
     *  Constants
     */
    public static final int iUnkown = 0;
    public static final int iEllipse = 1;
    public static final int iRectangle = 2;
    public static final int iDiamond = 3;
    public static final int iIrregular = 11;

    public static final String TYPE = "aperture";

    static final String[] ARR_NAMES = {"shape",
        "x", // Aperture in the horizontal plane.
        "y", // Aperture in the vertical plane.
        "pos" // Position in the element.
};

    /*
     *  Local Attributes
     */
    private Attribute attShape;
    private Attribute attAperX;
    private Attribute attAperY;
    private Attribute attAperPos;

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

    @Override
    public String[] getAttrNames() {
        return ARR_NAMES;
    }

    /**
     * Creates new ApertureBucket
     */
    public ApertureBucket() {
        super();

        attShape = new Attribute(0);
        attAperX = new Attribute(new double[]{0.0});
        attAperY = new Attribute(new double[]{0.0});
        attAperPos = new Attribute(new double[]{0.0});

        super.registerAttribute(ARR_NAMES[0], attShape, "Aperture shape. 0=unknown, 1=ellipse, 2=rectangle, 3=diamond, 11=irregular.");
        super.registerAttribute(ARR_NAMES[1], attAperX, "Aperture in the horizontal plane.");
        super.registerAttribute(ARR_NAMES[2], attAperY, "Aperture in the vertical plane.");
        super.registerAttribute(ARR_NAMES[3], attAperPos, "Position in the element.");
    }

    public int getShape() {
        return attShape.getInteger();
    }

    public double[] getAperX() {
        return attAperX.getArrDbl();
    }

    public double[] getAperY() {
        return attAperY.getArrDbl();
    }

    public double[] getAperPos() {
        return attAperPos.getArrDbl();
    }

    public void setShape(int intVal) {
        attShape.set(intVal);
    }

    public void setAperX(double dblVal) {
        attAperX.set(new double[]{dblVal});
    }

    public void setAperY(double dblVal) {
        attAperY.set(new double[]{dblVal});
    }

    public void setAperPos(double dblVal) {
        attAperPos.set(new double[]{dblVal});
    }

    public void setAperX(double[] dblArr) {
        attAperX.set(dblArr);
    }

    public void setAperY(double[] dblArr) {
        attAperY.set(dblArr);
    }

    public void setAperPos(double[] dblArr) {
        attAperPos.set(dblArr);
    }

}
