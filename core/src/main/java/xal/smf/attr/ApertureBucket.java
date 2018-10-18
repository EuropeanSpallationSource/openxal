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
    /** ID for serializable version */
    private static final long serialVersionUID = 2L;
    

    
    /*
     *  Constants
     */
    
    public static final int     iUnkown     = 0;
    public static final int     iEllipse    = 1;
    public static final int     iRectangle  = 2;
    public static final int     iDiamond    = 3;
    public static final int     iIrregular  = 11;
 
    
    public final static String  c_strType = "aperture"; 

    final static String[]       c_arrNames = {  "shape",
                                                "x",    // Aperture in the horizontal plane.
                                                "y",    // Aperture in the vertical plane.
                                                "pos"   // Position in the element.
                                };
    

                                
    /*
     *  Local Attributes
     */
    
    private Attribute m_attShape;
    private Attribute m_attAperX;
    private Attribute m_attAperY;
    private Attribute m_attAperPos;
    
    
    /*
     *  User Interface
     */
    
    /** Furnish a unique type id  */
    public String getType()         { return c_strType; };

    public String[] getAttrNames()  { return c_arrNames; };
    

     
    
    /** Creates new ApertureBucket */
    public ApertureBucket() {
        super();
        
        m_attShape  = new Attribute(0);
        m_attAperX  = new Attribute(new double[] {0.0});
        m_attAperY  = new Attribute(new double[] {0.0});
        m_attAperPos  = new Attribute(new double[] {0.0});
        
        super.registerAttribute(c_arrNames[0], m_attShape);
        super.registerAttribute(c_arrNames[1], m_attAperX);
        super.registerAttribute(c_arrNames[2], m_attAperY);
        super.registerAttribute(c_arrNames[3], m_attAperPos);
    };

    
    public int      getShape()  { return m_attShape.getInteger(); };
    public double[]   getAperX()  { return m_attAperX.getArrDbl(); };
    public double[]   getAperY()  { return m_attAperY.getArrDbl(); };
    public double[]   getAperPos()  { return m_attAperPos.getArrDbl(); };
    
    public void setShape(int intVal)    { m_attShape.set(intVal); };
    public void setAperX(double dblVal) { m_attAperX.set(new double[] {dblVal}); };
    public void setAperY(double dblVal) { m_attAperY.set(new double[] {dblVal}); };
    public void setAperPos(double dblVal) { m_attAperPos.set(new double[] {dblVal}); };
    
    public void setAperX(double[] dblArr) { m_attAperX.set(dblArr); };
    public void setAperY(double[] dblArr) { m_attAperY.set(dblArr); };
    public void setAperPos(double[] dblArr) { m_attAperPos.set(dblArr); };
    
};
