package xal.smf.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;

/**
 * The abstract Magnet Class element. This class contains elements common to all
 * magnets in an accelerator.
 *
 * @author J. Galambos
 *
 */
public abstract class Magnet extends AcceleratorNode implements MagnetType {

    private static final Logger LOGGER = Logger.getLogger(Magnet.class.getName());

    // static initialization
    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(Magnet.class, "magnet");
    }

    /**
     * The effective magnetic length (m)
     */
    public double leff;

    /**
     * The container for the magnet information
     */
    protected MagnetBucket bucMagnet;

    /**
     * Primary Constructor
     */
    public Magnet(final String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
        setMagBucket(new MagnetBucket());
    }

    /**
     * Constructor
     */
    public Magnet(final String strId) {
        this(strId, ChannelFactory.defaultFactory());
    }

    /**
     *
     * @return the attribute bucket containing the machine multipole fields
     */
    public MagnetBucket getMagBucket() {
        return bucMagnet;
    }

    ;

 
   /**
     *  
     * Set the attribute bucket containing the machine magnet info
     */

    public void setMagBucket(MagnetBucket buc) {
        if (bucMagnet != null) {
            mapAttrs.remove(bucMagnet.getType(), bucMagnet);
        }
        bucMagnet = buc;
        super.addBucket(buc);
    }

    /**
     *
     * Override AcceleratorNode implementation to check for a MultipoleBucket
     */
    @Override
    public void addBucket(AttributeBucket buc) {

        if (buc.getClass().equals(MagnetBucket.class)) {
            setMagBucket((MagnetBucket) buc);
        }

        super.addBucket(buc);
    }

    /**
     * Override the inherited method to be true since all magnets are of the
     * magnet type.
     *
     * @return true
     */
    @Override
    public boolean isMagnet() {
        return true;
    }

    /**
     * Test if the magnet is of the specified pole type. MagnetType defines the
     * list of accepted pole types.
     *
     * @param compPole Comparison pole which should be one of MagnetType.poles
     * @return true if the magnet is of the specified pole.
     */
    @Override
    public boolean isPole(String compPole) {
        return false;
    }

    /**
     * Get the orientation of the magnet as defined by MagnetType.
     *
     * @return One of HORIZONTAL, VERTICAL or NO_ORIENTATION
     */
    @Override
    public int getOrientation() {
        return NO_ORIENTATION;
    }

    /**
     * Determine whether this magnet is oriented horizontally.
     *
     * @return true if this magnet is oriented horizontally; false otherwise.
     */
    @Override
    public final boolean isHorizontal() {
        return getOrientation() == HORIZONTAL;
    }

    /**
     * Determine whether this magnet is oriented vertically.
     *
     * @return true if this magnet is oriented vertically; false otherwise.
     */
    @Override
    public final boolean isVertical() {
        return getOrientation() == VERTICAL;
    }

    /**
     * Determine whether this magnet is a skew magnet.
     *
     * @return true if the magnet is skew and false otherwise.
     */
    @Override
    public boolean isSkew() {
        return false;
    }

    /**
     * Get whether this magnet is a permanent magnet or an electromagnet.
     *
     * @return true if the magnet is permanent and false otherwise.
     */
    @Override
    public boolean isPermanent() {
        return false;
    }

    /**
     * Determine whether this magnet is a corrector.
     *
     * @return true if this magnet is a corrector.
     */
    @Override
    public boolean isCorrector() {
        return false;
    }

    /**
     * get the design field for the magnet (T for dipole, T/m for quad, etc.)
     */
    public double getDesignField() {
        return bucMagnet.getDfltField();
    }

    /**
     * get the effective magnetic length (m)
     */
    public double getEffLength() {
        return bucMagnet.getEffLength();
    }

    /**
     * get the default magnetic field
     */
    public double getDfltField() {
        return bucMagnet.getDfltField();
    }

    /**
     * get magnet polarity
     */
    public double getPolarity() {
        try {
            return bucMagnet.getPolarity();
        } catch (Exception e) {
            LOGGER.log(Level.INFO, " Polarity not set on {0}, for stability sake, using + field", this.getId());
            return 1;
        }
    }

    /**
     * get Normal fields
     */
    public double[] getNormField() {
        return bucMagnet.getNormField();
    }

    /**
     * get tangential fields
     */
    public double[] getTangField() {
        return bucMagnet.getTangField();
    }

    /**
     * Method setDfltField
     *
     * @param field the default magnetic field to be changed to.
     */
    public void setDfltField(double field) {
        bucMagnet.setDfltField(field);
    }

}
