package se.lu.esss.ics.jels.smf.impl;

import xal.smf.impl.qualify.ElementTypeManager;
import xal.ca.ChannelFactory;

/**
 * The implementation of the DTLTank sequence, which derives from the
 * AcceleratorSeq class. This is a container to be used in handling Drift Tube
 * Linacs These devices have RfGaps in them, which are controlled by a single
 * RfCavity. That is, the RfCavity contains the hooks to the klystron signals,
 * which control all of the RfGaps together. As the DTLTank is also a sequence,
 * it is possible for it to contain other types of nodes, such as quads and
 * BPMs.
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class ESSDTLTank extends ESSRfCavity {

    /**
     * standard type for instances of this class
     */
    public static final String s_strType = "DTLTank";

    // static initialization
    static {
        registerType();
    }

    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes(ESSDTLTank.class, s_strType);
    }

    /**
     * Primary Constructor
     */
    public ESSDTLTank(final String strId, final ChannelFactory channelFactory, final int intReserve) {
        super(strId, channelFactory, intReserve);
    }

    /**
     * Constructor
     */
    public ESSDTLTank(final String strId, final ChannelFactory channelFactory) {
        this(strId, channelFactory, 0);
    }

    /**
     * I just added this comment - didn't do any work.
     *
     * @param strId identifier string of the DTL
     *
     * @author Christopher K. Allen
     * @since May 3, 2011
     */
    public ESSDTLTank(final String strId) {
        this(strId, 0);
    }

    /**
     * I just added this comment - didn't do any work.
     *
     * @param strId identifier string of the DTL
     * @param intReserve optional parameter for specifying memory to reserve for
     * the DTL cells if known.
     *
     * @author Christopher K. Allen
     * @since May 3, 2011
     */
    public ESSDTLTank(final String strId, int intReserve) {
        this(strId, null, intReserve);
    }

    /**
     * Support the node type
     *
     * @return
     */
    @Override
    public String getType() {
        return s_strType;
    }
;
}
