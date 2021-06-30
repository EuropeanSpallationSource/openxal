/*
 *
 * Element.java
 *
 * Created on March 17, 2003, 1:18 PM
 */
package xal.sim.slg;

import xal.smf.AcceleratorNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * The super class of all lattice elements.
 *
 * @author wdklotz
 */
public abstract class Element implements VisitorListener, Cloneable {

    private static final Logger LOGGER = Logger.getLogger(Element.class.getName());

    /**
     * <p>
     * Indicates the component relationship of this model representation with
     * respect to its hardware counterpart. For example, does this element
     * represent the entire hardware component (value <code>WHOLE</code>) or
     * some subsection.
     * </p>
     *
     * @since Sep 2, 2009
     * @author Christopher K. Allen
     */
    public enum SECTION {

        /**
         * Component relationship unknown
         */
        UNKNOWN,
        /**
         * Element represents one point of the hardware element
         */
        POINT,
        /**
         * Element represents upstream end of hardware element
         */
        UPSTREAM,
        /**
         * Element represents downstream end of hardware element
         */
        DNSTREAM,
        /**
         * Element represents whole hardware element
         */
        WHOLE,
        /**
         * Element represent and internal portion of the hardware
         */
        INTERNAL;
    }

    /**
     * Element's corresponding hardware section
     */
    private SECTION secHware;

    /**
     * the element name
     */
    private final String name;
    /**
     * the relative position in distance units
     */
    private double position;
    /**
     * the base offset for rel. positions
     */
    private double base;
    /**
     * the length in distance units
     */
    private double len;
    /**
     * number formater
     */
    public static NumberFormat fmt;
    /**
     * the xal AcceleratorNode object
     */
    private AcceleratorNode xalNode;
    /**
     * flag used by slim elements
     */
    protected boolean handleAsThick;

    /**
     * Creates a new instance of Element
     */
    protected Element(String name, double position, double len) {
        this.secHware = SECTION.UNKNOWN;

        // number format is defined in Lattice
        fmt = Lattice.fmt;
        this.name = name;
        //always relative to base
        this.position = position;
        this.base = 0.0;
        this.len = len;
    }

    /**
     * Create a new, initialized <code>Element</code> object.
     *
     * @param secHware element's hardware subsection
     * @param name string identifier for the element
     * @param position position of the element within the lattice
     * @param len length of the element
     *
     * @since Sep 2, 2009
     * @author Christopher K. Allen
     */
    protected Element(SECTION secHware, String name, double position, double len) {
        this(name, position, len);

        this.secHware = secHware;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * The XAL AcceleratorNode property.
     */
    public void setAcceleratorNode(AcceleratorNode node) {
        xalNode = node;
    }

    /**
     * The XAL AcceleratorNode property.
     */
    public AcceleratorNode getAcceleratorNode() {
        return xalNode;
    }

    /**
     * Set the hardware section that this element represents.
     *
     * @param secHware enumeration of possible hardware subsections
     *
     * @since Sep 2, 2009
     * @author Christopher K. Allen
     */
    public void setHardwareSection(SECTION secHware) {
        this.secHware = secHware;
    }

    /**
     * Returns the hardware subsection that this element represents.
     *
     * @return the model element's corresponding hardware subsection
     *
     * @since Sep 2, 2009
     * @author Christopher K. Allen
     */
    public SECTION getHardwareSection() {
        return this.secHware;
    }

    /**
     * Return the element type.
     */
    public abstract String getType();

    /**
     * Is this really a thick element?
     *
     * @return true if element is to be treated as a thick element, i.e.
     * appended to the lattice in phase 1 of lattice generation; false if
     * element is treated as a thin element; i.e. inserted in the lattice in
     * phase 2 of lattice generation.
     */
    public boolean isThick() {
        return handleAsThick;
    }

    /**
     * Return the upstream start position of this element.
     */
    public double getStartPosition() {
        double pos = position - len * 0.5;
        if (pos < 0.) {
            pos = 0.;
        }
        return pos;
    }

    /**
     * Return the downstream end position of this element
     */
    public double getEndPosition() {
        return position + len * 0.5;
    }

    /**
     * Return the center position of this element
     */
    public double getPosition() {
        return position;
    }

    /**
     * Convert to absolute position
     */
    public double toAbsolutePosition(double position) {
        return base + position;
    }

    /**
     * Return the length of this element in distance units.
     */
    public double getLength() {
        return len;
    }

    /**
     * Return the name of this element.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the base for relative positions.
     */
    public double getBase() {
        return base;
    }

    private double[] getSlicePositions(double cutPos) {
        //calculate length and position of sliced parts.
        double upLen = cutPos - getStartPosition();
        if (Math.abs(upLen) < Lattice.EPS) {
            upLen = 0.0;
        }
        double dnLen = getLength() - upLen;
        if (Math.abs(dnLen) < Lattice.EPS) {
            dnLen = 0.0;
        }
        double upPos = getStartPosition() + upLen * 0.5;
        double dnPos = getEndPosition() - dnLen * 0.5;
        double[] retval = {upPos, upLen, dnPos, dnLen};
        return retval;
    }

    /**
     * Set the base for relative positions.
     */
    public void setBase(double base) {
        this.base = base;
    }

    /**
     * Set the element's center position.
     */
    public void setPosition(double position) {
        this.position = position;
    }

    /**
     * Set the element length.
     */
    public void setLength(double length) {
        this.len = length;
    }

    @SuppressWarnings("rawtypes")		// arrays don't support generics
    protected List<Element> split(Element insert) throws LatticeError {
        //The slice (and replace) operation. The thick element (this)
        //is cut into an upstream and a downstream part and then element 'insert'
        //is inserted (with limiting markers) into the lattice.
        final ArrayList<Element> retval = new ArrayList<>();
        Object[] args = new Object[3];
        Element upstream = null;
        Element downstream = null;

        double cutPos = insert.getPosition();
        double[] positions = getSlicePositions(cutPos);
        //consistyency check: any negative length ?
        double negLen = 0.f;
        boolean error = false;
        if (positions[1] < -Lattice.EPS) {
            negLen = positions[1];
            error = true;
        } else if (positions[3] < -Lattice.EPS) {
            negLen = positions[3];
            error = true;
        }
        if (error) {
            //ooops! negative length: severe error ...
            String message = "negative length when splitting: " + getName() + ": pos= " + getPosition() + ", len= " + getLength();
            message += ": calculated length= " + negLen;
            message += "\n\t while inserting: " + insert.getName() + ": pos= " + insert.getPosition() + ", len= " + insert.getLength();
            throw new LatticeError(message);
        }

        try {
            Class[] params = new Class[3];
            params[0] = Class.forName("java.lang.Double");
            params[1] = Class.forName("java.lang.Double");
            params[2] = Class.forName("java.lang.String");
            Constructor<?> constructor = this.getClass().getConstructor(params);

            args[0] = positions[0];
            args[1] = positions[1];
            args[2] = getName();

            upstream = (Element) constructor.newInstance(args);
            upstream.setAcceleratorNode(this.xalNode);

            args[0] = positions[2];
            args[1] = positions[3];
            args[2] = getName();

            downstream = (Element) constructor.newInstance(args);
            downstream.setAcceleratorNode(this.xalNode);

            // Assign the modeling element relationship to the hardware
            switch (this.getHardwareSection()) {
                case POINT:
                    upstream.setHardwareSection(SECTION.POINT);
                    downstream.setHardwareSection(SECTION.POINT);
                    break;

                case WHOLE:
                    upstream.setHardwareSection(SECTION.UPSTREAM);
                    downstream.setHardwareSection(SECTION.DNSTREAM);
                    break;

                case UPSTREAM:
                    upstream.setHardwareSection(SECTION.UPSTREAM);
                    downstream.setHardwareSection(SECTION.INTERNAL);
                    break;

                case DNSTREAM:
                    upstream.setHardwareSection(SECTION.INTERNAL);
                    downstream.setHardwareSection(SECTION.DNSTREAM);
                    break;

                case INTERNAL:
                    upstream.setHardwareSection(SECTION.INTERNAL);
                    downstream.setHardwareSection(SECTION.INTERNAL);
                    break;

                default:
                    upstream.setHardwareSection(SECTION.UNKNOWN);
                    downstream.setHardwareSection(SECTION.UNKNOWN);
            }

        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException exptn) {
            LOGGER.log(Level.SEVERE, null, exptn);
            System.exit(-1);
        }

        Element marker = new Marker(cutPos);

        if (Math.abs(upstream.getLength()) < Lattice.EPS) {
            retval.add(insert);
            retval.add(marker);
            retval.add(this);
        } else if (Math.abs(downstream.getLength()) < Lattice.EPS) {
            retval.add(this);
            retval.add(marker);
            retval.add(insert);
        } else {
            retval.add(upstream);
            retval.add(marker);
            retval.add(insert);
            retval.add(marker);
            retval.add(downstream);
        }

        return retval;
    }

    /**
     * Returns a printable string of this element.
     */
    public String toCoutString() {
        String retval = "";
        double elPos = getPosition();
        double elLen = getLength();
        double aStart = toAbsolutePosition(getStartPosition());
        String name = getName();
        String type = getType();
        retval += "s=" + fmt.format(aStart) + " m\t" + name + "\t" + type + " p=" + fmt.format(elPos) + " l=" + fmt.format(elLen);
        return retval;
    }

    public String getFam() {
        return "THICK";
    }

    /**
     * Return a version string wo the cvs keyword (i.e. $Id$).
     */
    public static String version() {
        String st = "";
        char[] woId = new char[st.length()];
        int srcBegin = 5;
        int srcEnd = st.length() - 6;
        int srccnt = srcEnd - srcBegin;
        if (srccnt <= 0) {
            return "";
        }
        st.getChars(srcBegin, srcEnd, woId, 0);
        return new String(woId, 0, srccnt);
    }

    /**
     * When called with a Visitor reference the implementer can either reject to
     * be visited (empty method body) or call the Visitor by passing its own
     * object reference.
     *
     * @param v the Visitor which wants to visit this object.
     */
    @Override
    public abstract void accept(Visitor v);

}
