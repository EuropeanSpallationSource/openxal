package xal.smf.attr;

/**
 * A container class for Twiss parameter information
 *
 *
 * @author John Galambos, Christopher K. Allen
 * @version 1.1
 */
public class TwissBucket extends AttributeBucket {

    /**
     * ID for serializable version
     */
    private static final long serialVersionUID = 1L;

    /*
     *  Constants
     */
    public static final String TYPE = "twiss";

    private static final String[] ARR_NAMES = {"x",
        "y",
        "ax",
        "bx",
        "ex",
        "ay",
        "by",
        "ey",
        "az",
        "bz",
        "ez",
        "etx",
        "etpx",
        "ety",
        "etpy",
        "mux",
        "muy"
    };

    // Enumeration of the three phase plane indexes
    public static final int X_PLANE = 0;
    public static final int Y_PLANE = 1;
    public static final int Z_PLANE = 2;

    /*
     *  Local Attributes
     */
    private Attribute attX;
    private Attribute attY;

    private Attribute attAlphaX;
    private Attribute attBetaX;
    private Attribute attEmitX;

    private Attribute attAlphaY;
    private Attribute attBetaY;
    private Attribute attEmitY;

    private Attribute attAlphaZ;
    private Attribute attBetaZ;
    private Attribute attEmitZ;

    private Attribute attEtaX;
    private Attribute attEtaPx;
    private Attribute attEtaY;
    private Attribute attEtaPy;

    private Attribute attMuX;
    private Attribute attMuY;

    /*
     *  User Interface
     */
    /**
     * Override virtual to provide type signature
     */
    @Override
    public String getType() {
        return TYPE;
    }

    public TwissBucket() {
        super();
        attX = new Attribute(0.0);
        attY = new Attribute(0.0);

        attAlphaX = new Attribute(0.0);
        attBetaX = new Attribute(0.0);
        attEmitX = new Attribute(0.0);

        attAlphaY = new Attribute(0.0);
        attBetaY = new Attribute(0.0);
        attEmitY = new Attribute(0.0);

        attAlphaZ = new Attribute(0.0);
        attBetaZ = new Attribute(0.0);
        attEmitZ = new Attribute(0.0);

        attEtaX = new Attribute(0.0);
        attEtaPx = new Attribute(0.0);
        attEtaY = new Attribute(0.0);
        attEtaPy = new Attribute(0.0);

        attMuX = new Attribute(0.0);
        attMuY = new Attribute(0.0);

        super.registerAttribute(ARR_NAMES[0], attX);
        super.registerAttribute(ARR_NAMES[1], attY);

        super.registerAttribute(ARR_NAMES[2], attAlphaX);
        super.registerAttribute(ARR_NAMES[3], attBetaX);
        super.registerAttribute(ARR_NAMES[4], attEmitX);

        super.registerAttribute(ARR_NAMES[5], attAlphaY);
        super.registerAttribute(ARR_NAMES[6], attBetaY);
        super.registerAttribute(ARR_NAMES[7], attEmitY);

        super.registerAttribute(ARR_NAMES[8], attAlphaZ);
        super.registerAttribute(ARR_NAMES[9], attBetaZ);
        super.registerAttribute(ARR_NAMES[10], attEmitZ);

        super.registerAttribute(ARR_NAMES[11], attEtaX);
        super.registerAttribute(ARR_NAMES[12], attEtaPx);
        super.registerAttribute(ARR_NAMES[13], attEtaY);
        super.registerAttribute(ARR_NAMES[14], attEtaPy);

        super.registerAttribute(ARR_NAMES[15], attMuX);
        super.registerAttribute(ARR_NAMES[16], attMuY);
    }

    /*
     *  Data Query
     */
    public double getX() {
        return attX.getDouble();
    }

    public double getY() {
        return attY.getDouble();
    }

    public double getAlphaX() {
        return attAlphaX.getDouble();
    }

    public double getBetaX() {
        return attBetaX.getDouble();
    }

    public double getEmitX() {
        return attEmitX.getDouble();
    }

    public double getAlphaY() {
        return attAlphaY.getDouble();
    }

    public double getBetaY() {
        return attBetaY.getDouble();
    }

    public double getEmitY() {
        return attEmitY.getDouble();
    }

    public double getAlphaZ() {
        return attAlphaZ.getDouble();
    }

    public double getBetaZ() {
        return attBetaZ.getDouble();
    }

    public double getEmitZ() {
        return attEmitZ.getDouble();
    }

    public double getEtaX() {
        return attEtaX.getDouble();
    }

    public double getEtaPx() {
        return attEtaPx.getDouble();
    }

    public double getEtaY() {
        return attEtaY.getDouble();
    }

    public double getEtaPy() {
        return attEtaPy.getDouble();
    }

    public double getMuX() {
        return attMuX.getDouble();
    }

    public double getMuY() {
        return attMuY.getDouble();
    }

    public double getCentroid(int iPlane) {
        switch (iPlane) {
            case X_PLANE:
                return getX();
            case Y_PLANE:
                return getY();
            default:
                return Double.NaN;
        }
    }

    public double getAlpha(int iPlane) {
        switch (iPlane) {
            case X_PLANE:
                return getAlphaX();
            case Y_PLANE:
                return getAlphaY();
            case Z_PLANE:
                return getAlphaZ();
            default:
                return Double.NaN;
        }
    }

    public double getBeta(int iPlane) {
        switch (iPlane) {
            case X_PLANE:
                return getBetaX();
            case Y_PLANE:
                return getBetaY();
            case Z_PLANE:
                return getBetaZ();
            default:
                return Double.NaN;
        }
    }

    public double getEmit(int iPlane) {
        switch (iPlane) {
            case X_PLANE:
                return getEmitX();
            case Y_PLANE:
                return getEmitY();
            case Z_PLANE:
                return getEmitZ();
            default:
                return Double.NaN;
        }
    }

    public double getEta(int iPlane) {
        switch (iPlane) {
            case X_PLANE:
                return getEtaX();
            case Y_PLANE:
                return getEtaY();
            default:
                return Double.NaN;
        }
    }

    public double getEtaP(int iPlane) {
        switch (iPlane) {
            case X_PLANE:
                return getEtaPx();
            case Y_PLANE:
                return getEtaPy();
            default:
                return Double.NaN;
        }
    }

    public double getMu(int iPlane) {
        switch (iPlane) {
            case X_PLANE:
                return getMuX();
            case Y_PLANE:
                return getMuY();
            default:
                return Double.NaN;
        }
    }

    /*
     *  Data Assignment
     */
    public void setX(double dblVal) {
        attX.set(dblVal);
    }

    public void setY(double dblVal) {
        attY.set(dblVal);
    }

    public void setAlphaX(double dblVal) {
        attAlphaX.set(dblVal);
    }

    public void setBetaX(double dblVal) {
        attBetaX.set(dblVal);
    }

    public void setEmitX(double dblVal) {
        attEmitX.set(dblVal);
    }

    public void setAlphaY(double dblVal) {
        attAlphaY.set(dblVal);
    }

    public void setBetaY(double dblVal) {
        attBetaY.set(dblVal);
    }

    public void setEmitY(double dblVal) {
        attEmitY.set(dblVal);
    }

    public void setAlphaZ(double dblVal) {
        attAlphaZ.set(dblVal);
    }

    public void setBetaZ(double dblVal) {
        attBetaZ.set(dblVal);
    }

    public void setEmitZ(double dblVal) {
        attEmitZ.set(dblVal);
    }

    public void setEtaX(double dblVal) {
        attEtaX.set(dblVal);
    }

    public void setEtaPx(double dblVal) {
        attEtaPx.set(dblVal);
    }

    public void setEtaY(double dblVal) {
        attEtaY.set(dblVal);
    }

    public void setEtaPy(double dblVal) {
        attEtaPy.set(dblVal);
    }

    public void setMuX(double dblVal) {
        attMuX.set(dblVal);
    }

    public void setMuY(double dblVal) {
        attMuY.set(dblVal);
    }

    public void setCentroid(int iPlane, double dblVal) {
        switch (iPlane) {
            case X_PLANE:
                setX(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            case Y_PLANE:
                setY(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            default:
                break;
        }
    }

    public void setAlpha(int iPlane, double dblVal) {
        switch (iPlane) {
            case X_PLANE:
                setAlphaX(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            case Y_PLANE:
                setAlphaY(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            case Z_PLANE:
                setAlphaZ(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            default:
                break;
        }
    }

    public void setBeta(int iPlane, double dblVal) {
        switch (iPlane) {
            case X_PLANE:
                setBetaX(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            case Y_PLANE:
                setBetaY(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            case Z_PLANE:
                setBetaZ(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            default:
                break;
        }
    }

    public void setEmit(int iPlane, double dblVal) {
        switch (iPlane) {
            case X_PLANE:
                setEmitX(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            case Y_PLANE:
                setEmitY(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            case Z_PLANE:
                setEmitZ(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            default:
                break;
        }
    }

    public void setEta(int iPlane, double dblVal) {
        switch (iPlane) {
            case X_PLANE:
                setEtaX(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            case Y_PLANE:
                setEtaY(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            default:
                break;
        }
    }

    public void getEtaP(int iPlane, double dblVal) {
        switch (iPlane) {
            case X_PLANE:
                setEtaPx(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            case Y_PLANE:
                setEtaPy(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            default:
                break;
        }
    }

    public void getMu(int iPlane, double dblVal) {
        switch (iPlane) {
            case X_PLANE:
                setMuX(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            case Y_PLANE:
                setMuY(dblVal);
                // tap added this break statement as it seems to be the intent
                break;
            default:
                break;
        }
    }

}
