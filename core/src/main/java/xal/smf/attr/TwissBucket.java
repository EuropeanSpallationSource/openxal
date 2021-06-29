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

    public static final String[] ARR_NAMES = {"x",
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
    public static final int iXPlane = 0;
    public static final int iYPlane = 1;
    public static final int iZPlane = 2;

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
            case iXPlane:
                return getX();
            case iYPlane:
                return getY();
            default:
                return Double.NaN;
        }
    }

    public double getAlpha(int iPlane) {
        switch (iPlane) {
            case iXPlane:
                return getAlphaX();
            case iYPlane:
                return getAlphaY();
            case iZPlane:
                return getAlphaZ();
            default:
                return Double.NaN;
        }
    }

    public double getBeta(int iPlane) {
        switch (iPlane) {
            case iXPlane:
                return getBetaX();
            case iYPlane:
                return getBetaY();
            case iZPlane:
                return getBetaZ();
            default:
                return Double.NaN;
        }
    }

    public double getEmit(int iPlane) {
        switch (iPlane) {
            case iXPlane:
                return getEmitX();
            case iYPlane:
                return getEmitY();
            case iZPlane:
                return getEmitZ();
            default:
                return Double.NaN;
        }
    }

    public double getEta(int iPlane) {
        switch (iPlane) {
            case iXPlane:
                return getEtaX();
            case iYPlane:
                return getEtaY();
            default:
                return Double.NaN;
        }
    }

    public double getEtaP(int iPlane) {
        switch (iPlane) {
            case iXPlane:
                return getEtaPx();
            case iYPlane:
                return getEtaPy();
            default:
                return Double.NaN;
        }
    }

    public double getMu(int iPlane) {
        switch (iPlane) {
            case iXPlane:
                return getMuX();
            case iYPlane:
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
            case iXPlane:
                setX(dblVal);
                break;  // tap added this break statement as it seems to be the intent
            case iYPlane:
                setY(dblVal);
                break;  // tap added this break statement as it seems to be the intent
        }
    }

    public void setAlpha(int iPlane, double dblVal) {
        switch (iPlane) {
            case iXPlane:
                setAlphaX(dblVal);
                break;  // tap added this break statement as it seems to be the intent
            case iYPlane:
                setAlphaY(dblVal);
                break;  // tap added this break statement as it seems to be the intent
            case iZPlane:
                setAlphaZ(dblVal);
                break;  // tap added this break statement as it seems to be the intent
        }
    }

    public void setBeta(int iPlane, double dblVal) {
        switch (iPlane) {
            case iXPlane:
                setBetaX(dblVal);
                break;  // tap added this break statement as it seems to be the intent
            case iYPlane:
                setBetaY(dblVal);
                break;  // tap added this break statement as it seems to be the intent
            case iZPlane:
                setBetaZ(dblVal);
                break;  // tap added this break statement as it seems to be the intent
        }
    }

    public void setEmit(int iPlane, double dblVal) {
        switch (iPlane) {
            case iXPlane:
                setEmitX(dblVal);
                break;  // tap added this break statement as it seems to be the intent
            case iYPlane:
                setEmitY(dblVal);
                break;  // tap added this break statement as it seems to be the intent
            case iZPlane:
                setEmitZ(dblVal);
                break;  // tap added this break statement as it seems to be the intent
        }
    }

    public void setEta(int iPlane, double dblVal) {
        switch (iPlane) {
            case iXPlane:
                setEtaX(dblVal);
                break;  // tap added this break statement as it seems to be the intent
            case iYPlane:
                setEtaY(dblVal);
                break;  // tap added this break statement as it seems to be the intent
        }
    }

    public void getEtaP(int iPlane, double dblVal) {
        switch (iPlane) {
            case iXPlane:
                setEtaPx(dblVal);
                break;  // tap added this break statement as it seems to be the intent
            case iYPlane:
                setEtaPy(dblVal);
                break;  // tap added this break statement as it seems to be the intent
        }
    }

    public void getMu(int iPlane, double dblVal) {
        switch (iPlane) {
            case iXPlane:
                setMuX(dblVal);
                break;  // tap added this break statement as it seems to be the intent
            case iYPlane:
                setMuY(dblVal);
                break;  // tap added this break statement as it seems to be the intent
        }
    }

}
