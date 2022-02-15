package xal.extension.jels.smf.attr;

import xal.smf.attr.BPMBucket;

/**
 * An attribute set for the NPM
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@ess.eu>
 */
public class NPMBucket extends BPMBucket {

    /*
     *  Constants
     */
    public static final String TYPE = "npm";

    /**
     * Override virtual to provide type signature
     */
    public NPMBucket() {
        super();
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
