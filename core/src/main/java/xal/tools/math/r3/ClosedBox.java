/*
 * ClosedBox.java
 *
 * Created on January 27, 2003, 11:05 AM
 */
package xal.tools.math.r3;

import java.io.PrintWriter;
import java.io.Serializable;
import xal.tools.math.ClosedInterval;
import xal.tools.math.MathException;

/**
 * Represents a Cartesian box in <strong>R</strong><sup>3</sup>.
 *
 * @author Christopher K. Allen
 * @since Jan 27, 2003
 */
public class ClosedBox implements Serializable {

    /*
     *  Global Constants 
     */
    /**
     * serialization version identifier
     */
    private static final long serialVersionUID = 1L;

    /*
     * Local Attributes
     */
    /**
     * first dimension extent of domain
     */
    private ClosedInterval i1;

    /**
     * second dimension extent of domain
     */
    private ClosedInterval i2;

    /**
     * third dimension extent of domain
     */
    private ClosedInterval i3;

    /**
     * Default constructor - creates an empty DomainR3 object to be initialized
     * by the user
     */
    public ClosedBox() {
    }

    /**
     * Initializing constructor - creates a new instance of DomainR3 according
     * to the given parameters.
     *
     * @param i1 interval of definition in x dimension
     * @param i2 interval of definition in y dimension
     * @param i3 interval of definition in z dimension
     */
    public ClosedBox(ClosedInterval i1, ClosedInterval i2, ClosedInterval i3) {
        this.i1 = i1;
        this.i2 = i2;
        this.i3 = i3;
    }

    /**
     * Initializing constructor - creates a new instance of DomainR3 according
     * to the given parameters.
     *
     * Not that the box is defined by the intervals [xmin,xmax] interval of
     * definition in x dimension [ymin,ymax] interval of definition in y
     * dimension [zmin,zmax] interval of definition in z dimension
     *
     * @param xmin x dimension minimum value
     * @param xmax x dimension maximum value
     * @param ymin y dimension minimum value
     * @param ymax y dimension maximum value
     * @param zmin z dimension minimum value
     * @param zmax z dimension maximum value
     *
     * @throws MathException one or more axis intervals are malformed (i.e.,
     * &alpha;<sub><em>max</em></sub> &lt; &alpha;<sub><em>min</em></sub>, where
     * &alpha; &isin; {<em>x,y,z</em>})
     *
     */
    public ClosedBox(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax) throws MathException {
        i1 = new ClosedInterval(xmin, xmax);
        i2 = new ClosedInterval(ymin, ymax);
        i3 = new ClosedInterval(zmin, zmax);
    }


    /*
     *  Grid Properties
     */
    /**
     * Get first dimension extent
     */
    public ClosedInterval get1() {
        return i1;
    }

    /**
     * Get second dimension extent
     */
    public ClosedInterval get2() {
        return i2;
    }

    /**
     * Get second dimension extent
     */
    public ClosedInterval get3() {
        return i3;
    }

    /**
     * Get the x dimension
     */
    public ClosedInterval getXDimension() {
        return i1;
    }

    /**
     * Get the y dimension
     */
    public ClosedInterval getYDimension() {
        return i2;
    }

    /**
     * Get the z dimension
     */
    public ClosedInterval getZDimension() {
        return i3;
    }

    /**
     * Get the minimum vertex
     */
    public R3 getVertexMin() {
        return new R3(i1.getMin(), i2.getMin(), i3.getMin());
    }

    /**
     * Get the maximum vertex
     */
    public R3 getVertexMax() {
        return new R3(i1.getMax(), i2.getMax(), i3.getMax());
    }

    /**
     * Determine whether point pt is an element of the domain.
     *
     * @return true if pt is in domain
     */
    public boolean membership(R3 pt) {
        return i1.membership(pt.getx()) && i2.membership(pt.gety()) && i3.membership(pt.getz());
    }

    /**
     * Determine whether or not point pt is member of the boundary of this set.
     *
     * @return true if pt is a boundary element
     */
    public boolean boundary(R3 pt) {
        return i1.isBoundary(pt.getx()) || i2.isBoundary(pt.gety()) || i3.isBoundary(pt.getz());
    }

    /**
     * Compute the centroid of the domain
     */
    public R3 centroid() {
        return new R3(i1.midpoint(), i2.midpoint(), i3.midpoint());
    }

    /**
     * Computes the diameter of the domain.
     */
    public double diameter() {
        return this.dimensions().norm2();
    }

    /**
     * Compute the volume of the domain.
     */
    public double volume() {
        return i1.measure() * i2.measure() * i3.measure();
    }

    /**
     * Compute the dimensions of the domain
     *
     * @return (lx,ly,lz)
     */
    public R3 dimensions() {
        return new R3(i1.measure(), i2.measure(), i3.measure());
    }

    /*
     *  Testing and Debugging
     */
    /**
     * Print out contents on an output stream
     *
     * @param os output stream receiving content dump
     */
    public void print(PrintWriter os) {
        i1.print(os);
        os.print("x");
        i2.print(os);
        os.print("x");
        i3.print(os);
    }

    /**
     * Print out contents on an output stream, terminate in newline character
     *
     * @param os output stream receiving content dump
     */
    public void println(PrintWriter os) {
        i1.print(os);
        os.print("x");
        i2.print(os);
        os.print("x");
        i3.println(os);
    }
}
