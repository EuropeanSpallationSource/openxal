/**
 * Signal.java
 *
 * Author  : Christopher K. Allen
 * Since   : Mar 13, 2014
 */
package xal.smf.impl.profile;

import xal.smf.scada.AScada;
import xal.smf.scada.BadStructException;
import xal.smf.scada.ScadaAnnotationException;
import xal.smf.scada.ScadaFieldDescriptor;
import xal.smf.scada.ScadaRecord;
import xal.tools.data.DataAdaptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xal.smf.impl.WireHarp;

/**
 * <p>
 * Data structure for storing all the properties of a beam profile signal. This
 * includes the signal values, the location of the value within the beam pipe,
 * and the number of samples. Noise characteristics may also be present.
 * </p>
 * <p>
 * This structure is equipped for direct acquisition of <code>WireHarp</code>
 * device profile data measurements. The data structure is derived from the
 * <code>ScadaStruct</code> base class in order to use that data I/O mechanism
 * already in place.
 * </p>
 * <p>
 * This type is intended to serve as attributes for a larger data structure
 * containing the beam profiles in multiple transverse planes. That aggregating
 * structure is expected to set up the necessary connections for data
 * acquisition use by the base class <code>ScadaRecord</code>, namely by
 * instantiating instances with the constructor
 * <code>Signal(List<ScadaFieldDescriptor>)</code> (protected access). It is
 * still possible to use a <code>Signal</code> object directly by annotating it
 * with the <code>{@link AScada.Record}</code> annotation.
 * </p>
 *
 * <p>
 * <strong>Ported from XAL on Jul 15, 2014.</strong><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 30, 2013
 */
public class Signal extends ScadaRecord {

    private static final Logger LOGGER = Logger.getLogger(Signal.class.getName());

    /*
     * Global Constants
     */
    /**
     * <p>
     * Field names of the <code>{@link Signal}</code> class. These are needed
     * for construction of PV field descriptors for the classes aggregating
     * <code>{@link Signal}</code> and the <code>{@link ASignalSet.ASet}</code>
     * annotation which identifies them.
     * </p>
     *
     * @author Christopher K. Allen
     * @since Feb 4, 2013
     *
     */
    public enum FIELD {

        /**
         * This is the enumeration constant for the
         * <code>{@link Signal#cnt}</code> field.
         */
        CNT("cnt", "typeCnt", "hndCntRb"),
        /**
         * This is the enumeration constant for the
         * <code>{@link Signal#pos}</code> field.
         */
        POS("pos", "typePos", "hndPosRb"),
        /**
         * This is the enumeration constant for the
         * <code>{@link Signal#val}</code> field.
         */
        VAL("val", "typeVal", "hndValRb"),
        /**
         * This is the enumeration constant for the
         * <code>{@link Signal#navg}</code> field.
         */
        NAVG("navg", "typeNseAvg", "hndNseAvgRb"),
        /**
         * This is the enumeration constant for the
         * <code>{@link Signal#nvar}</code> field.
         */
        NVAR("nvar", "typeNseVar", "hndNseVarRb");

        /*
         * Operations
         */
        /**
         * <p>
         * Creates a new SCADA (PV) field descriptor for the signal field
         * represented by this enumeration constant. The data to create the
         * field descriptor is taken from the provided annotation.
         * </p>
         * <h3>NOTE:</h3>
         * <p>
         * &middot; If the annotation field corresponding to this enumeration is
         * set to the empty string <kbd>""</kbd>, the method will return a
         * <code>null</code> value. Since it is not necessary for all handles to
         * be specified this is not necessarily a failure condition.
         * </p>
         *
         * @param annSig the annotation from which we are extracting the
         * parameters
         *
         * @return A new SCADA field descriptor for the the field of this
         * enumeration (from given signal annotation), or <code>null</code> if
         * the given annotation does not contain the PV signal names
         *
         * @throws ScadaAnnotationException general error with argument
         * <var>annSig</var>, ill defined
         *
         * @author Christopher K. Allen
         * @since Feb 6, 2013
         */
        public ScadaFieldDescriptor createDescriptor(ASignal annSig) throws ScadaAnnotationException {
            try {
                String strFldNm = this.strFldName;
                Class<?> clsFldTyp = (Class<?>) this.fldAnnTyp.invoke(annSig);
                String strFldHnd = (String) this.fldAnnVal.invoke(annSig);

                if (strFldHnd.equals("")) {
                    return null;
                }

                return new ScadaFieldDescriptor(strFldNm, clsFldTyp, strFldHnd);
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                throw new ScadaAnnotationException("Bad annotation " + annSig.getClass());

            }

        }

        /*
         * Local Attributes and Initialization
         */
        /**
         * Name of the field represented by this constant
         */
        private String strFldName;

        /**
         * Element in annotation <code>ADaqHarp</code> containing the channel
         * handle
         */
        private Method fldAnnVal;

        /**
         * Element in annotation that identifies the type of the channel value
         */
        private Method fldAnnTyp;

        /*
         * Initialization
         */
        /**
         * Creates a new instance of FIELD and initializes the field names of
         * <code>{@link WireHarp.Signal}</code> and
         * <code>{@link WireHarp.ASignal}</code> that the constant represents.
         *
         * @param strFldName name of the field in the
         * <code>{@link WireHarp.Signal}</code> structure this constant
         * represents
         * @param strChanTyp field name of annotation
         * <code>{@link WireHarp.ASignal}</code> which identifies the data type
         * of the channel value
         * @param strChanHnd field name of annotation
         * <code>{@link WireHarp.ASignal}</code>containing the metal-data to be
         * assigned to the <code>Signal</code> field.
         *
         * @author Christopher K. Allen
         * @since Feb 4, 2013
         */
        private FIELD(final String strFldName, String strChanTyp, String strChanHnd) {

            // Create an error message in case this operation fails
            String strErrMsg = "Instantiation error while extracting meta data from annotation " + ASignal.class;

            // Initialize the attributes of the enumeration
            try {
                this.strFldName = strFldName;
                this.fldAnnTyp = ASignal.class.getMethod(strChanTyp);
                this.fldAnnVal = ASignal.class.getMethod(strChanHnd);

            } catch (NoSuchMethodException | SecurityException e) {
                LOGGER.log(Level.SEVERE, strErrMsg, e);
            }
        }
    }

    /*
     * Global Operations
     */
    /**
     * Creates and returns a new <code>Signal</code> object containing no data
     * and incapable of any data acquisition. This type of object is primarily
     * used for data processing.
     *
     * @return an empty, unattached signal object
     *
     * @author Christopher K. Allen
     * @since Apr 19, 2012
     */
    public static Signal createBlankSignal() {
        return new Signal();
    }

    /**
     * <p>
     * Creates a new instance of the <code>Signal</code> class which is
     * connected to a given device whose signal fields are described in the
     * given annotation. When created here, the <code>Signal</code> object
     * becomes a fully functional <code>{@link ScadaRecord}</code> and can
     * automatically acquire data from the connected device.
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot; The returned <code>Signal</code> object may not have all fields
     * connected. Only those fields for which the given annotation has non-empty
     * values will be connected. This could cause complications for signals
     * requiring such fields and should be checked.
     * </p>
     *
     * @param annSig annotation class containing the meta-data describing the
     * signal connections
     *
     * @return operational <code>ScadaRecord</code> class capable of data
     * acquisition
     *
     * @throws ScadaAnnotationException field in the annotation is not
     * accessible, does not exist, or enumeration <code>Signal.FIELD</code> is
     * corrupt
     * @throws BadStructException a required field of the given annotation is
     * empty
     *
     * @author Christopher K. Allen
     * @since Feb 7, 2013
     */
    public static Signal createConnectedSignal(ASignal annSig) throws BadStructException, ScadaAnnotationException {
        List<ScadaFieldDescriptor> lstDscr = new LinkedList<>();

        for (Signal.FIELD enmFld : FIELD.values()) {

            ScadaFieldDescriptor sfdSigFld = enmFld.createDescriptor(annSig);

            if (sfdSigFld != null) {
                lstDscr.add(sfdSigFld);
            }
        }

        return new Signal(lstDscr);
    }

    //
    // Data Fields
    //
    /**
     * Positions of the sample points in signal
     */
    private double[] pos = {0.0};

    /**
     * Signal value at the sample location
     */
    private double[] val = {0.0};

    /**
     * Number of sample points in the signal
     */
    private int cnt = 0;

    /**
     * mean value of the noise amplitude
     */
    private double navg = 0.0;

    /**
     * standard deviation (or variance) of the noise amplitude
     */
    private double nvar = 0.0;

    /*
     * Initialization
     */
    /**
     * Creates a new, uninitialized instance of <code>Signal</code> which is not
     * connected to any XAL channels.
     *
     * @throws BadStructException no SCADA fields (@AScada.Field) were found in
     * data structure
     *
     * @author Christopher K. Allen
     * @since Feb 7, 2013
     */
    public Signal() throws BadStructException {
        super();
    }

    /**
     * Creates a new instance of Signal and initializes the SCADA operations
     * with the given field descriptors.
     *
     * @param lstFldDscr array of field descriptors used for communication with
     * hardware
     *
     * @throws BadStructException no SCADA fields were found in argument
     *
     * @author Christopher K. Allen
     * @since Feb 1, 2013
     */
    protected Signal(List<ScadaFieldDescriptor> lstFldDscr) throws BadStructException {
        super(lstFldDscr);

    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public double[] getPos() {
        return pos;
    }

    public void setPos(double[] pos) {
        this.pos = pos;
    }

    public double[] getVal() {
        return val;
    }

    public void setVal(double[] val) {
        this.val = val;
    }

    /*
     * Operations
     */
    /**
     * <p>
     * Performs an averaging operation with the given <code>Signal</code> object
     * using the given averaging factor. The quantities within this data
     * structure are averaged in place with that of the given signal structure.
     * Letting &lambda; denote the provided averaging factor, which is in the
     * interval [0,1], the new values of this signal, say <em>v'</em> are given
     * by the formula
     * <br>
     * <br>
     * &nbsp; &nbsp; <em>v'</em> = &lambda;<em>u</em> + (1 - &lambda;)<em>v</em>
     * <br>
     * <br>
     * where <em>v</em> is the previous value of <em>v'</em> and <em>u</em> is
     * the new value of <em>v</em> in <code>sigAcq</code>.
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot; Nothing is done to the position values in the signal, they are
     * unchanged of current writing.
     * <br>
     * &middot; The standard deviation is weighted vectorally.
     * </p>
     *
     * @param sigAcq signal <em>u</em> to average into this one <em>v</em>
     * @param dblWtFac weighting factor &lambda; &in; [0,1] for argument
     * <em>u</em>
     *
     * @throws IllegalArgumentException the provided signal is not the same size
     * as this signal
     *
     * @author Christopher K. Allen
     * @since May 1, 2014
     */
    public void average(Signal sigAcq, double dblWtFac) throws IllegalArgumentException {

        if (sigAcq.cnt != this.cnt) {
            throw new IllegalArgumentException("Signal objects must be of same size");
        }

        for (int index = 0; index < this.cnt; index++) {
            this.val[index] = sigAcq.val[index] * dblWtFac + this.val[index] * (1.0 - dblWtFac);
        }

        this.navg = sigAcq.navg * dblWtFac + this.navg * (1 - dblWtFac);
        this.nvar = Math.sqrt(sigAcq.nvar * sigAcq.nvar * dblWtFac + this.nvar * this.nvar * (1 - dblWtFac));
    }

    /*
     * DataListener Interface
     */
    /**
     * Returns a (unique) string identifier that identifies the persistent data
     * format that belongs to this <code>Signal</code> class.
     *
     * @see xal.tools.data.DataListener#dataLabel()
     *
     * @author Christopher K. Allen
     * @since Jan 30, 2013
     */
    @Override
    public String dataLabel() {
        return this.getClass().getCanonicalName();
    }

    /**
     *
     * @see xal.smf.scada.ScadaRecord#update(xal.tools.data.DataAdaptor)
     *
     * @author Christopher K. Allen
     * @since Oct 14, 2014
     */
    @Override
    public void update(DataAdaptor daptSrc) throws MissingResourceException, BadStructException {

        // New format - Get the data adaptor node corresponding to this signal from the 
        //  provided parent node
        String strLabel = this.dataLabel();
        DataAdaptor daptSgnl = daptSrc.childAdaptor(strLabel);

        // this is XAL version
        if (daptSgnl == null) {
            strLabel = "gov.sns." + strLabel;
            daptSgnl = daptSrc.childAdaptor(strLabel);
        }

        // we were given the data node itself
        if (daptSgnl == null) {
            daptSgnl = daptSrc;
        }

        // Check if we have the old attribute tags
        if (daptSgnl.hasAttribute("sgnl_cnt")) {
            this.updateWithOldFormat(daptSgnl);

            return;
        }

        super.update(daptSgnl);
    }

    /*
     * Object Overrides
     */
    /**
     * Write out the contents of this signal.
     *
     * @return a representation of the signal as a string of (pos,val) pairs
     *
     * @since Mar 12, 2010
     * @author Christopher K. Allen
     *
     * @see xal.smf.scada.ScadaRecord#toString()
     */
    @Override
    public String toString() {
        if (pos == null) {
            return "";
        }

        // Create the buffer and add the noise figures
        StringBuilder bufSig = new StringBuilder();

        bufSig.append("#samples=").append(this.cnt);
        bufSig.append(", noise: mean=").append(this.navg);
        bufSig.append(", var=").append(this.nvar);
        bufSig.append("; ");

        // Add the signal position-value pairs
        int index = 0;
        for (double dblPos : this.pos) {
            double dblVal = this.val[index++];

            bufSig.append("(");
            bufSig.append(dblPos);
            bufSig.append(",");
            bufSig.append(dblVal);
            bufSig.append(") ");
        }

        return bufSig.toString();
    }

    /**
     * Creates a deep copy of this object. All arrays and field descriptors are
     * duplicated.
     *
     * @since Apr 19, 2012
     * @author Christopher K. Allen
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Signal sigCopy = (Signal) super.clone();

        sigCopy.navg = this.navg;
        sigCopy.nvar = this.nvar;
        sigCopy.cnt = this.cnt;
        sigCopy.pos = this.pos.clone();
        sigCopy.val = this.val.clone();

        return sigCopy;
    }

    /*
     * Support Methods
     */
    /**
     * Upate the attribute values from the given data source assume the data
     * source is formatted using the first version.
     *
     * @param daptSrc data source containing attribute values
     *
     * @author Christopher K. Allen
     * @since Oct 15, 2014
     */
    private void updateWithOldFormat(DataAdaptor daptSrc) {

        // We have the old format, need to do this piece meal.
        this.cnt = daptSrc.intValue("sgnl_cnt");
        this.pos = daptSrc.doubleArray("sgnl_pos");
        this.val = daptSrc.doubleArray("sgnl_val");

        if (daptSrc.hasAttribute("noise_avg")) {
            this.navg = daptSrc.doubleValue("noise_avg");
        }
        if (daptSrc.hasAttribute("noise_var")) {
            this.nvar = daptSrc.doubleValue("noise_var");
        }
    }
}
