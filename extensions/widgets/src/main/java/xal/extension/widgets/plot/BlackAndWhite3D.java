package xal.extension.widgets.plot;

/**
 * This class is a data class for data used in the FunctionGraphsJPanel class.
 * This class contains 2D grid with values at the grid points. These values
 * will be presented as colored rectangles in the plot. It does not use
 * interpolation to calculate z-value between grid points. Z-value can be 0 or
 * non zero only. 
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class BlackAndWhite3D extends ColorSurfaceData{

    /**  The data set constructor with size of the grid.*/   
    public BlackAndWhite3D(int nX, int nY){
	super(nX,nY);
    }

    /**  Returns the value of the 2D array for x and y. */
    @Override
    public double getValue(double x, double y){

	int i,j;

	if(x < xMin || y < yMin || x > xMax || y > yMax){
	    return zMin;
	}

        i = (int) ((x-xMin)/xStep + 0.5);
        j = (int) ((y-yMin)/yStep + 0.5);
	        
        if( i < 0) i = 0;
        if( i > (nX-1)) i = nX-1;
        if( j < 0) j = 0;
        if( j > (nY-1)) j = nY-1;

        return gridData[i][j];
    }

    /**  Bins value into the 2D array for x and y with weight = value.
     *   Here the value does not matter. The value on the grid will be
     *   1.
     */
    @Override
    public void addValue(double x, double y, double value){
	int i,j;

        i = (int) ((x-xMin)/xStep + 0.5);
        j = (int) ((y-yMin)/yStep + 0.5);

        if( i < 0) i = 0;
        if( i > (nX-1)) i = nX-1;
        if( j < 0) j = 0;
        if( j > (nY-1)) j = nY-1;

	gridData[i][j] = 1.0;
	if(zMin > gridData[i][j]) zMin = gridData[i][j];
	if(zMax < gridData[i][j]) zMax = gridData[i][j];
    }
}
