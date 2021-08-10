/*
 * GridR3.java
 *
 * Created on January 24, 2003, 9:25 PM
 */
package xal.tools.math.r3;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Represents a regular grid in three dimensions. Grid resolution may vary in
 * each dimension, however, no uneven mesh spacing is permitted. Consequently,
 * operations on this grid are less general but faster.
 * </p>
 * <p>
 * This class was originally meant to be used in a multi-particle probe for
 * multi-particle simulations. I don't believe it is being used for anything
 * right now.
 * </p>
 *
 * @author Christopher Allen
 * @since Jan 24, 2003
 */
public class Grid implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(Grid.class.getName());


    /*
     * Global Constants
     */
    /**
     * Serialization version number
     */
    private static final long serialVersionUID = 1L;

    /*
     *  Internal Classes
     */
    /**
     * Represents a point in the grid
     */
    static class GridPt implements Serializable {

        /**
         * Serialization version number
         */
        private static final long serialVersionUID = 1L;

        /**
         * function value at grid point
         */
        double val = 0.0;

    }

    /**
     * Represents a cell (voxel) in the grid
     */
    static class GridCell implements Serializable {

        /**
         * Serialization version number
         */
        private static final long serialVersionUID = 1L;

        /*
         *  Attributes
         */
        /**
         * the associated grid object
         */
        Grid grid;

        /**
         * coordinates of the base point
         */
        R3 base;

        /**
         * objects composing cell vertices
         */
        GridPt pt000;
        GridPt pt001;
        GridPt pt010;
        GridPt pt011;
        GridPt pt100;
        GridPt pt101;
        GridPt pt110;
        GridPt pt111;

        /*
         *  Auxiliary Variables
         */
        /**
         * stored local coordinates
         */
        double u1;
        double u2;
        double u3;

        /**
         * stored compliments of the local coordinates
         */
        double c1;
        double c2;
        double c3;

        /*
         *  Methods
         */
        /**
         * Return potentials at vertices
         */
        public double val000() {
            return pt000.val;
        }

        public double val001() {
            return pt001.val;
        }

        public double val010() {
            return pt010.val;
        }

        public double val011() {
            return pt011.val;
        }

        public double val100() {
            return pt100.val;
        }

        public double val101() {
            return pt101.val;
        }

        public double val110() {
            return pt110.val;
        }

        public double val111() {
            return pt111.val;
        }

        /**
         * Compute the local (cell) coordinates of the point pt. Each coordinate
         * has a range in the interval [0,1].
         *
         * @param pt pt in R3 (within the grid's domain of definition)
         *
         * @return local coordinate (u1,u2,u3) of the point within the cell
         */
        public R3 localCoords(R3 pt) {

            // Compute the displacement within the grid
            // grid origin coordinate
            R3 ptOrg;
            // grid resolution
            R3 vecRes;
            // displacement of pt from grid origin
            R3 vecDis;

            ptOrg = grid.getGridOrigin();
            vecRes = grid.getGridResolution();
            vecDis = pt.minus(ptOrg);

            // Compute the local coordinates
            // local coordinates
            double v1;
            double v2;
            double v3;

            v1 = vecDis.get1() % vecRes.get1();
            v2 = vecDis.get2() % vecRes.get2();
            v3 = vecDis.get3() % vecRes.get3();

            return new R3(v1, v2, v3);
        }

        /**
         * Get function value in grid cell using a linear interpolation
         *
         * @param pt point with grid cell to evaluate potential
         *
         * @return function value on grid
         */
        public double interpolateValue(R3 pt) {
            // the interpolated value
            double val;

            computeLocalCoords(pt);

            val = val000() * c1 * c2 * c3 + val001() * c1 * c2 * u3 + val010() * c1 * u2 * c3 + val011() * c1 * u2 * u3
                    + val100() * u1 * c2 * c3 + val101() * u1 * c2 * u3 + val110() * u1 * u2 * c3 + val111() * u1 * u2 * u3;

            return val;
        }

        /**
         * Compute the gradient in local coordinates of grid cell value
         *
         * @param pt point to determine the gradient
         *
         * @return the gradient (computed with local coordinates)
         */
        public R3 interpolateGradient(R3 pt) {
            // the gradient components
            double g1;
            double g2;
            double g3;

            computeLocalCoords(pt);

            g1 = (val100() - val000()) * c2 * c3 + (val101() - val001()) * c2 * u3
                    + (val110() - val010()) * u2 * c3 + (val111() - val011()) * u2 * u3;

            g2 = (val010() - val000()) * c1 * c3 + (val011() - val001()) * c1 * u3
                    + (val110() - val100()) * u1 * c3 + (val111() - val101()) * u1 * u3;

            g3 = (val001() - val000()) * c1 * c2 + (val011() - val010()) * c1 * u2
                    + (val101() - val100()) * u1 * c2 + (val111() - val110()) * u1 * u2;

            return new R3(g1, g2, g3);
        }

        /**
         * Compute the local (cell) coordinates of the point pt. Each coordinate
         * has a range in the interval [0,1].
         *
         * @param pt pt in R3 (within the grid's domain of definition)
         *
         * @return local coordinate (u1,u2,u3) of the point within the cell
         */
        protected void computeLocalCoords(R3 pt) {

            // Compute the displacement within the grid
            // grid origin coordinate
            R3 ptOrg;
            // grid resolution
            R3 vecRes;
            // displacement of pt from grid origin
            R3 vecDis;

            ptOrg = grid.getGridOrigin();
            vecRes = grid.getGridResolution();
            vecDis = pt.minus(ptOrg);

            // Compute the local coordinates
            u1 = vecDis.get1() % vecRes.get1();
            c1 = 1.0 - u1;
            u2 = vecDis.get2() % vecRes.get2();
            c2 = 1.0 - u2;
            u3 = vecDis.get3() % vecRes.get3();
            c3 = 1.0 - u3;
        }
    }

    /*
     *  Grid Attributes
     */
    /**
     * array of grid points
     */
    private GridPt[][][] arrPts;

    /**
     * array of grid cells
     */
    private GridCell[][][] arrCells;

    /**
     * discrete grid size
     */
    private Z3 vecSize;

    /**
     * physical dimensions of grid
     */
    private R3 vecDim = new R3();

    /**
     * grid resolution (L/N)
     */
    private R3 vecRes = new R3();

    /**
     * R3 coordinates of first grid point, i.e., the grid origin
     */
    private R3 ptOrg = new R3();

    /**
     * the grid domain in R3
     */
    private ClosedBox boxDom;

    /*
     *  Grid Initialization
     */
    /**
     * Allocate a new Grid
     *
     * @param n1 number of grid points in first dimension
     * @param n2 number of grid points in second dimension
     * @param n3 number of grid points in third dimension
     *
     * @exception GridException unable to allocate grid objects
     */
    public Grid(int n1, int n2, int n3) throws GridException {
        if (n1 <= 0 || n2 <= 0 || n3 <= 0) {
            throw new GridException("Grid::setGridSize() - Bad size vector.");
        }

        vecDim.setAll(0.0);
        vecRes.setAll(0.0);
        ptOrg.setAll(0.0);
        vecSize = new Z3(n1, n2, n3);

        this.allocateGrid(n1, n2, n3);
    }

    /**
     * Allocate a new instance of GridR3 and set the domain.
     *
     * @param n1 number of grid points in first dimension
     * @param n2 number of grid points in second dimension
     * @param n3 number of grid points in third dimension
     * @param boxDom descriptor of grid domain in R3
     *
     * @exception GridException invalid size vector encountered
     */
    public Grid(int n1, int n2, int n3, ClosedBox boxDom) throws GridException {
        this(n1, n2, n3);

        setGridDomain(boxDom);
    }

    /**
     * Set the domain in R3 which the grid occupies. All grid point locations
     * are determined by this descriptor.
     *
     * @param boxDom description of the grid position and size in R3
     */
    public void setGridDomain(ClosedBox boxDom) throws GridException {
        if (boxDom.volume() <= 0.0) {
            throw new GridException("Grid3D::setGridDomain() - bad domain descriptor.");
        }

        this.boxDom = boxDom;
        vecDim = boxDom.dimensions();
        ptOrg = boxDom.getVertexMin();

        vecRes.setx(vecDim.getx() / (vecSize.geti() - 1));
        vecRes.sety(vecDim.gety() / (vecSize.getj() - 1));
        vecRes.setz(vecDim.getz() / (vecSize.getk() - 1));
    }

    /**
     * Set the function value at grid point given by index
     *
     * @param i first dimension index of grid point
     * @param j second dimension index of grid point
     * @param k third dimension index of grid point
     * @param dblVal value of the function at grid point (i,j,k)
     */
    public void setPtValue(int i, int j, int k, double dblVal) {
        GridPt pt = this.getGridPt(i, j, k);

        pt.val = dblVal;
    }

    /*
     *  Grid Properties
     */
    /**
     * Get the size of the supporting object array.
     *
     * @return vector of array dimensions (nx, ny, nz)
     */
    public Z3 getGridSize() {
        return vecSize;
    }

    /**
     * Get the domain of the grid in R3.
     *
     * @return a ClosedBox object describing the domain of definition for this
     * grid
     */
    public ClosedBox getGridDomain() {
        return boxDom;
    }

    /**
     * Get grid resolution.
     *
     * @return vector (dx,dy,dz) of spacing between grid points
     */
    public R3 getGridResolution() {
        return vecRes;
    }

    /**
     * Get the coordinates of the grid origin, i.e., the first grid vertex.
     *
     * @return (x,y,z) coordinates of grid origin
     */
    public R3 getGridOrigin() {
        return ptOrg;
    }

    /*
     *  Grid Queries
     */
    /**
     * Get the function value at the grid point indexed by (i,j,k)
     *
     * @param i index of the first grid dimension
     * @param j index of the second grid dimension
     * @param k index of the third grid dimension
     *
     * @return function value stored at grid point (i,j,k)
     */
    public double getPtValue(int i, int j, int k) {
        return getGridPt(i, j, k).val;
    }

    /**
     * Determine whether a point is an element of the domain of definition for
     * this grid.
     *
     * @param pt point in R3 to check membership
     *
     * @return true if pt is in domain of definition
     */
    public boolean membershipGrid(R3 pt) {
        return getGridDomain().membership(pt);
    }

    /**
     * Compute the base vertex index of grid cell containing this point. The
     * base vertex is the vertex of the grid cell with smallest l1 norm. Grid
     * cells are assigned by this index.
     *
     * @param pt pt in R3 (within the grid's domain of definition)
     *
     * @return index of grid cell owning pt
     */
    public Z3 compCellIndex(R3 pt) {
        // indices of the base vertex
        int i;
        int j;
        int k;
        // displacement of pt from grid origin
        R3 vecDis;

        vecDis = pt.minus(this.getGridOrigin());

        i = (int) Math.floor(vecDis.get1() / this.getGridResolution().get1());
        j = (int) Math.floor(vecDis.get2() / this.getGridResolution().get2());
        k = (int) Math.floor(vecDis.get3() / this.getGridResolution().get3());

        return new Z3(i, j, k);
    }

    /**
     * Return the coordinates in R3 of the grid point at index (i,j,k)
     *
     * @param i x-dimension index of grid point
     * @param j y-dimension index of grid point
     * @param k k-dimension index of grid point
     *
     * @return (x,y,z) coordinate of grid point at (i,j,k)
     */
    public R3 compPtCoords(int i, int j, int k) {
        // displacements from grid origin
        double dx;
        double dy;
        double dz;

        dx = (i * this.getGridResolution().getx());
        dy = (j * this.getGridResolution().gety());
        dz = (k * this.getGridResolution().getz());

        R3 vecDis = new R3(dx, dy, dz);

        return ptOrg.plus(vecDis);
    }

    /**
     * Return the coordinates in R3 of the grid point at index (i,j,k)
     *
     * @param vecIndex (i,j,k) index vector of grid point
     *
     * @return (x,y,z) coordinate of grid point at (i,j,k)
     */
    public R3 compPtCoords(Z3 vecIndex) {
        return this.compPtCoords(vecIndex.geti(), vecIndex.getj(), vecIndex.getk());
    }

    /**
     * Return the grid cell containing this point.
     *
     * @param pt pt in R3 (within the grid's domain of definition)
     *
     * @return grid cell object contain pt
     */
    public GridCell compCellContaining(R3 pt) {
        // indices of the base vertex
        int i;
        int j;
        int k;
        // displacement of pt from grid origin
        R3 vecDis;

        vecDis = pt.minus(this.getGridOrigin());

        i = (int) Math.floor(vecDis.get1() / this.getGridResolution().get1());
        j = (int) Math.floor(vecDis.get2() / this.getGridResolution().get2());
        k = (int) Math.floor(vecDis.get3() / this.getGridResolution().get3());

        return this.getGridCell(i, j, k);
    }

    /*
     *  Function Interpolation
     */
    /**
     * Compute and return the interpolated function value at the point pt
     *
     * @param pt interpolation point
     *
     * @return interpolated function value
     */
    public double interpolateValue(R3 pt) {
        GridCell cell = this.compCellContaining(pt);

        return cell.interpolateValue(pt);
    }

    /**
     * Compute and return the interpolated function gradient at the point pt.
     * The gradient is taken with respect to the local coordinates of a grid
     * cell. Thus, the true gradient must be scaled by the grid resolution and
     * any other differential element particular to the coordinate system being
     * used.
     *
     * @param pt interpolation point
     *
     * @return gradient (w.r.t. local cell coords) of function
     */
    public R3 interpolateGradient(R3 pt) {
        GridCell cell = this.compCellContaining(pt);

        return cell.interpolateGradient(pt);
    }

    /*
     *  Testing and Debugging
     */
    /**
     * Print out grid parameters on an output stream
     *
     * @param os output stream to receive text description of grid
     */
    public void print(PrintWriter os) {
        os.println("GRID PARAMETERS");
        os.print("Grid size               : ");
        this.getGridSize().println(os);
        os.print("Grid resolution         : ");
        this.getGridResolution().println(os);
        os.print("Grid origin coordinates : ");
        this.getGridOrigin().println(os);
        os.print("Grid domain             : ");
        this.getGridDomain().println(os);
    }

    /**
     * Testing engine
     */
    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "3.5%Math.PI={0}", 3.5 % Math.PI);
    }

    /*
     *  Derived Class Support
     */
    /**
     * Return the grid point object located at grid point index.
     *
     * @param i 1st dimension grid point index
     * @param j 2nd dimension grid point index
     * @param k 3rd dimension grid point index
     *
     * @return object located at grid point (i,j,k)
     */
    protected GridPt getGridPt(int i, int j, int k) {
        return arrPts[i][j][k];
    }

    /**
     * Return the grid point object located at grid point index.
     *
     * @param vecInd vector index (i,j,k) of grid point
     *
     * @return object located at grid point (i,j,k)
     */
    protected GridPt getGridPt(Z3 vecInd) {
        return getGridPt(vecInd.geti(), vecInd.getj(), vecInd.getk());
    }

    /**
     * Return the grid cell object located at grid point index.
     *
     * @param i 1st dimension grid cell index
     * @param j 2nd dimension grid cell index
     * @param k 3rd dimension grid cell index
     *
     * @return object located at grid cell (i,j,k)
     */
    protected GridCell getGridCell(int i, int j, int k) {
        return arrCells[i][j][k];
    }

    /**
     * Return the grid cell object located at grid cell index.
     *
     * @param vecInd vector index (i,j,k) of grid cell
     *
     * @return object located at grid cell (i,j,k)
     */
    protected GridCell getGridCell(Z3 vecInd) {
        return getGridCell(vecInd.geti(), vecInd.getj(), vecInd.getk());
    }

    /*
     *  Internal Support
     */
    /**
     * Build the grid data structure. - allocate the grid points and the grid
     * cells - set the base point and vertices of the grid cells
     *
     * @param vecSize (n1,n2,n3) dimensions of the grid (grid points)
     *
     * @exception GridException derived class failed to allocate GridPts and
     * GridCells
     */
    private void allocateGrid(int n1, int n2, int n3) throws GridException {
        // Allocate grid objects
        arrPts = this.allocatePts(n1, n2, n3);
        arrCells = this.allocateCells(n1 - 1, n2 - 1, n3 - 1);

        // Configure grid cells
        // loop control variables for each dimension
        int i;
        int j;
        int k;

        for (i = 0; i < n1 - 1; i++) {
            for (j = 0; j < n2 - 1; j++) {
                for (k = 0; k < n3 - 1; k++) {
                    GridCell cell = getGridCell(i, j, k);

                    cell.grid = this;
                    cell.base = this.compPtCoords(i, j, k);

                    cell.pt000 = getGridPt(i, j, k);
                    cell.pt001 = getGridPt(i, j, k + 1);
                    cell.pt010 = getGridPt(i, j + 1, k);
                    cell.pt011 = getGridPt(i, j + 1, k + 1);
                    cell.pt100 = getGridPt(i + 1, j, k);
                    cell.pt101 = getGridPt(i + 1, j, k + 1);
                    cell.pt110 = getGridPt(i + 1, j + 1, k);
                    cell.pt111 = getGridPt(i + 1, j + 1, k + 1);
                }
            }
        }
    }

    /**
     * Allocates the array of grid point objects. Derived classes may create
     * grid point objects derived from GridPt to perform specific tasks.
     *
     * Derived classes must override this method if the GridPt class also
     * sub-classed. The array of appropriate child class of GridPt should be
     * returned.
     *
     * @param n1, n2, n3 dimensions of the array (n1, n2, n3)
     *
     * @return array of objects living at grid points
     */
    protected GridPt[][][] allocatePts(int n1, int n2, int n3) {
        return new GridPt[n1][n2][n3];
    }

    /**
     * Allocates the array of grid cell objects. Derived classes may create grid
     * cell objects derived from GridCell to perform specific tasks.
     *
     * Derived classes must override this method if the GridCell class also
     * subclassed. The array of appropriate child class of GridCell should be
     * returned.
     *
     * @param n1, n2, n3 dimensions of the array (nx, ny, nz)
     *
     * @return array of objects composing grid cells
     */
    protected GridCell[][][] allocateCells(int n1, int n2, int n3) {
        return new GridCell[n1][n2][n3];
    }
}
