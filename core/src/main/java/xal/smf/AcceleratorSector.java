/*
 * AcceleratorSector.java
 *
 * Created on August 27, 2002, 11:23 AM
 */
package xal.smf;

/**
 * @author CKAllen
 */
public final class AcceleratorSector extends AcceleratorSeq {

    /*
     *  Architecture Requirements
     */
    /**
     * Charge of beam in this sequence (+-1)
     */
    protected double dblBeamCharge;

    /**
     * particle species charge to mass ratio
     */
    protected double dblQ2M;

    /**
     * particle species rest energy
     */
    protected double dblEr;

    /*
     *  Beam Parameters
     */
    /**
     * Return the signum of design particle species charge
     */
    public double getChargeSignum() {
        return dblBeamCharge;
    }

    /**
     * Return the charge to mass ratio of the design particle species
     */
    public double getCharge2Mass() {
        return dblQ2M;
    }

    /**
     * Return the rest energy of the design particle species
     */
    public double getRestEnergy() {
        return dblEr;
    }

    /**
     * Set the charge to mass ratio of the design particle
     */
    public void setCharge2Mass(double dblQ2M) {
        this.dblQ2M = dblQ2M;
    }

    /**
     * Set the charge sign of the design particle @param dblSgn {-1,+1}
     */
    public void setChargeSignum(double dblSgn) {
        dblBeamCharge = dblSgn;
    }

    /**
     * Set the rest energy of the design particle species
     */
    public void setRestEnergy(double dblEr) {
        this.dblEr = dblEr;
    }

    /**
     * Creates a new instance of AcceleratorSector
     */
    public AcceleratorSector(String strId) {
        super(strId);
    }

    /**
     * Creates a new instance of AcceleratorSector
     */
    public AcceleratorSector(String strId, int intReserve) {
        super(strId, intReserve);
    }

    /**
     * Adds node to the Sector at the tail. Sector become the owner of the node.
     * CKA 08.02
     *
     * @param node node to be appended to Sector
     * @return true if successfully add, false if node already is owned by
     * Sector
     *
     */
    @Override
    public boolean addNode(AcceleratorNode node) {
        return super.addNode(node);
    }

    public AcceleratorSeq concatenate(AcceleratorSector sec) {
        // new name
        String newStrId = this.getId() + ":" + sec.getId();
        // concatenated sequence
        AcceleratorSeq seqNew = new AcceleratorSeq(newStrId);

        return seqNew;
    }

}
