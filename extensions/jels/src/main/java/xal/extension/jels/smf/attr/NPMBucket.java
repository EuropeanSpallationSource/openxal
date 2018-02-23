package xal.extension.jels.smf.attr;

import xal.smf.attr.BPMBucket;

/**
 * An attribute set for the NPM
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class NPMBucket extends BPMBucket {

    /*
     *  Constants
     */
    public final static String c_strType = "npm";

    /**
     * Override virtual to provide type signature
     */
    public NPMBucket() {
        super();
    }

    public String getType() {
        return c_strType;
    }

}
