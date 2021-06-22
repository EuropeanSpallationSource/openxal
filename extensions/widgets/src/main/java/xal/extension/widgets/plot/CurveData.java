package xal.extension.widgets.plot;

import java.awt.*;

/**
 * This class is a curve data class for data used in the FunctionGraphsJPanel class.
 * This class contains a set of 2D points that will be connected on the graph's plane.
 * This class does not update the graph panel automatically. User must call the
 * method <code> refreshGraphJPanel() </code> of the  FunctionGraphsJPanel
 * in the program.
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class CurveData{

    private int nPoints = 0;
    private int nChunk = 50;

    private double [] pointsX = null;
    private double [] pointsY = null;

    private int nX, nY;

    private double xMin,xMax;
    private double yMin,yMax;

    private Color color = Color.black;

    private int lineWidth = 1;

    private BasicStroke lineStroke = new BasicStroke(1.0f);

    /**  The data set constructor.*/
    public CurveData(){
	pointsX = new double[nChunk];
	pointsY = new double[nChunk];
        xMin =  Double.MAX_VALUE;
        xMax = -Double.MAX_VALUE;
        yMin =  Double.MAX_VALUE;
        yMax = -Double.MAX_VALUE;
    }

    /**  Sets the color of the curve.*/
    public void setColor(Color color){
	if(color != null){
	    this.color = color;
	}
    }

    /**  Returns the color of the curve.*/
    public Color getColor(){
	return color;
    }

    /**  Deletes all points.*/
    public void clear(){
	nPoints = 0;
        xMin =  Double.MAX_VALUE;
        xMax = -Double.MAX_VALUE;
        yMin =  Double.MAX_VALUE;
        yMax = -Double.MAX_VALUE;
    }

    /**  Returns number of points.*/
    public int getSize(){
	return nPoints;
    }

    /**  Sets the line width.*/
    public void setLineWidth(int lineWidth){
	this.lineWidth = lineWidth;
        lineStroke = new BasicStroke((float) lineWidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);
    }

    /**  Returns the line width.*/
    public int getLineWidth(){
	return lineWidth;
    }

   /** returns the stroke for drawing.*/
    public BasicStroke getStroke(){
	return lineStroke;
    }

   /** sets the stroke for drawing.*/
    public void setStroke(BasicStroke lineStroke){
	   this.lineStroke = lineStroke;
		 this.lineWidth = (int) lineStroke.getLineWidth();
    }

    /**  Sets the points.*/
    public void setPoints(double [] x, double [] y){
	if(x.length == y.length){
	    xMin =  Double.MAX_VALUE;
	    xMax = -Double.MAX_VALUE;
	    yMin =  Double.MAX_VALUE;
	    yMax = -Double.MAX_VALUE;
	    resize(x.length);
            for(int i = 0;  i < x.length; i++ ){
		pointsX[i] = x[i];
		pointsY[i] = y[i];
		if(xMin > x[i]) xMin = x[i];
		if(yMin > y[i]) yMin = y[i];
		if(xMax < x[i]) xMax = x[i];
		if(yMax < y[i]) yMax = y[i];
	    }
	    nPoints = x.length;
	}
    }

    /**  Adds a point.*/
    public void addPoint(double x, double y){
	resize(nPoints+1);
	pointsX[nPoints] = x;
	pointsY[nPoints] = y;
	nPoints++;
	if(xMin > x) xMin = x;
	if(yMin > y) yMin = y;
	if(xMax < x) xMax = x;
	if(yMax < y) yMax = y;
    }

	/**  Finds min and max values.*/
	public void findMinMax(){
		xMin =  Double.MAX_VALUE;
	    xMax = -Double.MAX_VALUE;
	    yMin =  Double.MAX_VALUE;
	    yMax = -Double.MAX_VALUE;
		for(int i = 0;  i < nPoints; i++ ){
		if(xMin > pointsX[i]) xMin = pointsX[i];
		if(yMin > pointsY[i]) yMin = pointsY[i];
		if(xMax < pointsX[i]) xMax = pointsX[i];
		if(yMax < pointsY[i]) yMax = pointsY[i];
		}
	}


    /**  Sets a particular point with the index i.*/
    public void setPoint(int i, double x, double y){
	if(i < nPoints){
	    pointsX[i] = x;
	    pointsY[i] = y;
	    if(xMin > x) xMin = x;
	    if(yMin > y) yMin = y;
	    if(xMax < x) xMax = x;
	    if(yMax < y) yMax = y;
	}
    }

    /**  Returns the x-value for index i.*/
    public double getX(int i){
	return pointsX[i];
    }

    /**  Returns the y-value for index i.*/
    public double getY(int i){
	return pointsY[i];
    }

    /**  Returns the minimal X value. */
    public double getMinX(){
	return xMin;
    }

    /**  Returns the maximal X value. */
    public double getMaxX(){
	return xMax;
    }

    /**  Returns the minimal Y value. */
    public double getMinY(){
	return yMin;
    }

    /**  Returns the maximal Y value. */
    public double getMaxY(){
	return yMax;
    }

    private void resize(int nSize){
	if( nSize > pointsX.length){
	    double [] tmp_x = new double[nSize + nChunk];
	    double [] tmp_y = new double[nSize + nChunk];
            for(int i = 0;  i < pointsX.length; i++ ){
		tmp_x[i] = pointsX[i];
		tmp_y[i] = pointsY[i];
	    }
            pointsX = tmp_x;
            pointsY = tmp_y;
	}
    }
}
