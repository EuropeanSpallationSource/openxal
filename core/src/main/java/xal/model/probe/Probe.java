/*
 * Probe.java
 *
 * Created on August 12, 2002, 5:15 PM
 */
package xal.model.probe;

import xal.tools.annotation.AProperty.NoEdit;
import xal.tools.annotation.AProperty.Units;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;
import xal.model.IAlgorithm;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.alg.Tracker;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Provides a base class implementation of the IProbe interface that is useful
 * for most standard probe types. This class provides most of the functionality
 * necessary for the implementation of the IProbe interface,
 * <strong>except</strong> a definition and implementation of a probe "state".
 * Thus, it is up to base classes to provide and implement the particular aspect
 * of a beam the probe represents.
 * </p>
 *
 *
 * @author Christopher K. Allen
 */
public abstract class Probe<S extends ProbeState<S>> implements IProbe, IArchive {

    private static final Logger LOGGER = Logger.getLogger(Probe.class.getName());

    /*
     * Global Constants
     */
    /**
     * element tag for probe data
     */
    public static final String PROBE_LABEL = "probe";

    /**
     * attribute tag for probe type name
     */
    protected static final String TYPE_LABEL = "type";

    /**
     * attribute tag for the time stamp
     */
    protected static final String TIME_LABEL = "time";

    /**
     * element tag for comment data
     */
    protected static final String COMMENT_LABEL = "comment";

    /**
     * attribute tag for the date
     */
    private static final String DATE_LABEL = "date";

    /**
     * attribute tag for author
     */
    private static final String AUTHOR_LABEL = "author";

    /**
     * attribute tag for comment text
     */
    protected static final String TEXT_LABEL = "text";

    /**
     * data node label for algorithm data
     */
    private static final String NODETAG_ALG = "algorithm";

    /*
     * Global Methods
     */
    /**
     * Read the contents of the supplied <code>DataAdaptor</code> and return an
     * instance of the appropriate Probe species.
     *
     * @param container <code>DataAdaptor</code> to read a Probe from
     * @return a Probe for the contents of the DataAdaptor
     * @throws DataFormatException error encountered reading the DataAdaptor
     */
    public static Probe<?> readFrom(DataAdaptor container)
            throws DataFormatException {

        DataAdaptor daptProbe = container.childAdaptor(Probe.PROBE_LABEL);
        if (daptProbe == null) {
            throw new DataFormatException("Probe#readFrom() - no Probe data node.");
        }

        String type = daptProbe.stringValue(Probe.TYPE_LABEL);
        Probe<?> probe;
        try {
            Class<?> probeClass = Class.forName(type);
            probe = (Probe<?>) probeClass.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new DataFormatException(e.getMessage());
        }
        probe.load(daptProbe);
        return probe;
    }

    /**
     * <p>
     * Clone factory method. Creates a new <code>Probe</code> object, of the
     * appropriate type, initialized to the argument <code>Probe</code>.
     * </p>
     * <h3>NOTE:</h3>
     * <p>
     * There is now a reset() method that is preferable to this one. It clears
     * the probe trajHist and restores the initial state saved in the
     * initialize() method, without creating a new probe instance.
     * </p>
     *
     * @param probeInit     <code>Probe</code> object containing initial data
     *
     * @return new <code>Probe</code> object initialized to argument
     */
    public static Probe<?> newProbeInitializedFrom(final Probe<?> probeInit) {
        Class<?> pClass = probeInit.getClass();

        try {
            Constructor<?> ctorCopy = pClass.getConstructor(pClass);
            return (Probe<?>) ctorCopy.newInstance(probeInit);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Unable to intialize from " + probeInit.toString());
            return null;

        }
    }

    /*
     *  Local Attributes
     */
    //
    //  Probe Properties
    //
    /**
     * User comment associated with probe
     */
    private String strComment = "";

    /**
     * Time stamp associated with probe
     */
    private Date dateStamp = new Date();

    /**
     * Species name
     */
    private String speciesName = "";

    //
    // Probe Settings
    //
    /**
     * toggle trajHist tracking for a probe
     */
    private boolean bolTrack = true;

    /**
     * algorithm providing probe dynamics
     */
    private IAlgorithm algTracker = null;

    //
    // Probe State
    //
    /**
     * initial state of probe, set when initialize is called
     */
    private S stateInit;

    /**
     * current state of the probe - defines the probe
     */
    protected S stateCurrent;

    /**
     * Current probe trajHist
     */
    protected Trajectory<S> trajHist;

    /*
     *  Abstract Methods
     */
    /**
     * Require concrete implementations to override this method to create a
     * <code>Trajectory</code> object of the appropriate species.
     *
     * @return a <code>Trajectory</code> of the appropriate species for this
     * probe type
     */
    public abstract Trajectory<S> createTrajectory();

    /**
     * Captures the probe's state in a ProbeState of the appropriate species.
     *
     */
    public abstract S createProbeState();

    /**
     * Creates a new, blank <code>ProbeState</code> of the appropriate species.
     *
     * @return a new, blank <code>ProbeState</code>
     *
     * @author Jonathan M. Freed
     * @since Jul 1, 2014
     */
    public abstract S createEmptyProbeState();

    /**
     * Read the contents of the supplied <code>DataAdaptor</code> and return an
     * instance of the appropriate Trajectory species.
     *
     * @param container <code>DataAdaptor</code> to read a Trajectory from
     * @return a ProbeState for the contents of the DataAdaptor
     * @throws DataFormatException error encountered reading the DataAdaptor
     */
    protected abstract S readStateFrom(DataAdaptor container) throws DataFormatException;

    /*
     * ---------------------------------------------------------------
     */
    /**
     * Creates a deep copy of the probe
     */
    public abstract Probe<S> copy();

    /*
     *  Initialization
     */
    /**
     * Creates a new instance of Probe. Since Probe is an abstract base only
     * derived classes may call constructor.
     *
     */
    protected Probe() {

        //need to simply initialize an empty probe state
        this.stateCurrent = this.createEmptyProbeState();
        this.stateInit = this.createEmptyProbeState();

        this.trajHist = this.createTrajectory();

        this.algTracker = null;
    }

    /**
     * Creates a new instance of Probe. Since Probe is an abstract base only
     * derived classes may call constructor.
     *
     * @param ifcAlg default dynamics algorithm for probe
     */
    protected Probe(final IAlgorithm ifcAlg) {
        this();

        algTracker = ifcAlg;
    }

    /**
     * Copy constructor for Probe. This constructor does a deep copy of the
     * Probe base attributes by calling member support method
     * deepCopyProbeBase().
     *
     * @param probe Probe object to be cloned
     */
    protected Probe(final Probe<S> probe) {
        this();

        deepCopy(probe);
    }

    /**
     * Provide a user comment associated with the probe
     *
     * @param strComment user comment string
     */
    public void setComment(String strComment) {
        this.strComment = strComment;
    }

    /**
     * Sets a time stamp for the probe.
     *
     * @param dateStamp time stamp for probe
     */
    public void setTimestamp(Date dateStamp) {
        this.dateStamp = dateStamp;
    }

    /**
     * Set the species name
     *
     * @param name the species name
     */
    public void setSpeciesName(String name) {
        speciesName = name;
    }

    /**
     * Set the algorithm defining the probes dynamics through elements
     *
     * @param ifcAlg object exposing the IAlgorithm interface
     */
    public boolean setAlgorithm(IAlgorithm ifcAlg) {
        if (!ifcAlg.validProbe(this)) {
            return false;
        }

        algTracker = ifcAlg;
        return true;
    }

    /**
     * Set particle trajHist tracking for probes.
     *
     * @param bolTrack turn tracking on or off
     */
    public void setTracking(boolean bolTrack) {
        this.bolTrack = bolTrack;
    }

    /**
     * Initialize this probe from the one specified.
     *
     * @param probe the probe from which to initialize this one
     *
     * @deprecated This method is only called from child class overrides which
     * are never called themselves.
     */
    @Deprecated
    protected void initializeFrom(final Probe<S> probe) {
        final S initialState = probe.getTrajectory().initialState();
        if (initialState != null) {
            applyState(initialState);
        }

        setAlgorithm(probe.getAlgorithm());
        setTimestamp(new Date());
        setComment(probe.getComment());
    }


    /*
     * Operations
     */
    /**
     * This method returns a clone of the current state of this probe. That is,
     * the <code>ProbeState</code> object is a representation of this probe at
     * the moment this method was called.
     *
     * @return a deep copy of the current state of this probe
     *
     * @author Christopher K. Allen
     * @since Jun 26, 2014
     */
    public S cloneCurrentProbeState() {
        return this.stateCurrent.copy();
    }

    /**
     * Applies the properties of the state that is passed in to the current
     * state of the probe.
     *
     * @param state - the state to apply to the probe
     *
     * @author Jonathan M. Freed
     * @since Jul 9, 2014
     */
    /**
     * Apply the contents of ProbeState to update my current state. Subclass
     * implementations should call super.applyState to ensure superclass state
     * is applied.
     *
     * @param state     <code>ProbeState</code> object containing new probe state
     * data
     */
    public void applyState(S state) {
        this.stateCurrent = state.copy();
    }

    /**
     * <p>
     * Resets the probe to the saved initial state, if there is one and clears
     * the trajHist.
     * </p>
     * <p>
     * <h4>CKA NOTES:</h4>
     * These notes concern the refactoring of the probe component in order to
     * tighten the typing.
     * <br/>
     * <br/>
     * &middot; The new behavior should be the same as before, only the
     * implementation differs. The current state is set to a clone of the
     * (previously saved) initial state. &middot; The trajectory is cleared.
     * </p>
     */
    public void reset() {
        if (stateInit != null) {
            this.stateCurrent = stateInit.copy();
        }
        this.trajHist = this.createTrajectory();
    }

    /*
     *  Data Query
     */
    /**
     * Returns the comment string associated with the probe
     *
     * @return comment string
     */
    public String getComment() {
        return strComment;
    }

    /**
     * Returns the time stamp of the probe.
     *
     * @return time stamp
     */
    @NoEdit    // editors should not access this property
    public Date getTimestamp() {
        return dateStamp;
    }

    /**
     * Returns the momentum
     *
     * @return particle momentum
     */
    public double getMomentum() {
        return this.stateCurrent.getMomentum();
    }

    /**
     * Returns the current state object maintained by this probe. The state
     * object contains all the defining state information at the current
     * simulation trajectory location.
     *
     * @return the current state of this probe
     *
     * @author Christopher K. Allen
     * @since Nov 18, 2014
     */
    public S getCurrentState() {
        return this.stateCurrent;
    }

    /**
     * Returns the initial state of this probe. This is the state with which the
     * probe begins the simulation. Whenever the <code>{@link #reset()}</code>
     * command is called the current state of the simulation is set to this
     * state.
     *
     * @return the current starting state for this probe
     *
     * @since Dec 29, 2015, Christopher K. Allen
     */
    public S getInitialState() {
        return this.stateInit;
    }

    /**
     * Get the state history of the probe.
     *
     * @return Trajectory object of the proper sub-type for the probe type
     */
    @NoEdit // editors should not access this property
    @Override
    public Trajectory<S> getTrajectory() {
        return trajHist;
    }


    /*
     *  IProbe Interface
     */
    /**
     * <p>
     * Returns the longitudinal phase of this probe with respect to the RF
     * phase. Typically used to account for phase delay/advance in cavities
     * incurred due to finite propagation time. For example
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &phi; &#8796; &phi;<sub>0</sub> - &Delta;&phi;
     * <br/>
     * <br/>
     * where &Delta;&phi; = 2&pi;<em>f</em>&Delta;<i/>t</em> is the phase delay
     * due to elapsed time &Delta;<em>t</em>, <em>f</em> is the cavity resonant
     * frequency, and &phi;<sub>0</sub> is the operating phase of the cavity
     * (w.r.t. the synchronous particle).
     * </p>
     *
     * @return the probe phase &phi; with respect to the machine RF frequency
     *
     * @author Christopher K. Allen
     * @since Nov 17, 2014
     */
    @Override
    public double getLongitinalPhase() {
        return this.getCurrentState().getLongitudinalPhase();
    }

    /**
     * returns the species name
     *
     * @return species name
     */
    @Override
    public String getSpeciesName() {
        return speciesName;
    }

    /**
     * Returns the charge of probe's particle species
     *
     * @return particle species charge (<strong>Coulombs</strong>)
     */
    @Override
    public double getSpeciesCharge() {
        return this.stateCurrent.getSpeciesCharge();
    }

    /**
     * Returns the rest energy of particle species
     *
     * @return particle species rest energy (<strong>electron-volts</strong>)
     */
    @Units("eV")
    @Override
    public double getSpeciesRestEnergy() {
        return this.stateCurrent.getSpeciesRestEnergy();
    }

    /**
     * Returns the id of the current lattice element that the probe is visiting.
     *
     * @return id of current lattice element
     */
    @Override
    public String getCurrentElement() {
        return this.stateCurrent.getElementId();
    }

    /**
     *
     * @see xal.model.IProbe#getCurrentElementTypeId()
     *
     * @since Dec 16, 2014 by Christopher K. Allen
     */
    @Override
    public String getCurrentElementTypeId() {
        return this.stateCurrent.getElementTypeId();
    }

    /**
     * Returns the identifier of the hardware modeled by the element associated
     * with this state.
     *
     * @return hardware ID of the associated modeling element
     *
     * @see xal.model.IProbe#getCurrentHardwareId()
     *
     * @author Christopher K. Allen
     * @since Sep 3, 2014
     */
    @Override
    public String getCurrentHardwareId() {
        return this.stateCurrent.getHardwareNodeId();
    }

    /**
     * Returns the current beam-line position of the probe
     *
     * @return probe position (<strong>meters</strong>)
     */
    @Units("meters")
    @Override
    public double getPosition() {
        return this.stateCurrent.getPosition();
    }

    /**
     * Return the time elapsed since the probe began propagation.
     *
     * @return elapsed time in <strong>seconds</strong>
     */
    @Units("seconds")
    @Override
    public double getTime() {
        return this.stateCurrent.getTime();
    }

    /**
     * Return the kinetic energy of the probe. Depending upon the probe type,
     * this could be the actual kinetic energy of a single constituent particle,
     * the average kinetic energy of an ensemble, the design energy, etc.
     *
     * @return probe kinetic energy (<strong>electron-volts</strong>)
     */
    @Units("eV")
    @Override
    public double getKineticEnergy() {
        return this.stateCurrent.getKineticEnergy();
    }

    /**
     * Returns the probe velocity normalized to the speed of light.
     *
     * @return normalized probe velocity v/c (<strong>unitless</strong>
     */
    @Override
    public double getBeta() {
        return this.stateCurrent.getBeta();
    }

    /**
     * Returns the relativistic parameter corresponding to the probe velocity.
     * The relativistic factor is given by the formulae
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &gamma; = (<em>W</em>/<em>mc</em><sup>2</sup>) + 1 =
     * [1/(1-&beta;<sup>2</sup>)]<sup>1/2</sup>
     *
     * @return probe relativistic factor (<strong>unitless</strong>)
     */
    @Override
    public double getGamma() {
        return this.stateCurrent.getGamma();
    }

    /**
     *
     * @see xal.model.IProbe#lookupLastStateFor(java.lang.String)
     *
     * @since Dec 17, 2014 by Christopher K. Allen
     */
    @Override
    public ProbeState<?> lookupLastStateFor(String strElemTypeId) {
        Trajectory<S> trjProbe = this.getTrajectory();
        return trjProbe.peakLastByType(strElemTypeId);
    }

    /**
     * Set the current lattice element id.
     *
     * @param id element id of current lattice element
     */
    @Override
    public void setCurrentElement(String id) {
        this.stateCurrent.setElementId(id);
    }

    /**
     *
     * @see xal.model.IProbe#setCurrentElementTypeId(java.lang.String)
     *
     * @since Dec 16, 2014 by Christopher K. Allen
     */
    @Override
    public void setCurrentElementTypeId(String strTypeId) {
        this.stateCurrent.setElementTypeId(strTypeId);
    }

    /**
     * Sets the identifier of the hardware modeled by the current element.
     *
     * @see xal.model.IProbe#setCurrentHardwareId(java.lang.String)
     *
     * @author Christopher K. Allen
     * @since Sep 3, 2014
     */
    @Override
    public void setCurrentHardwareId(String strSmfId) {
        this.stateCurrent.setHardwareNodeId(strSmfId);
    }

    /**
     * Set the current position of the probe along the beamline.
     *
     * @param s new probe position (<strong>meters</strong>)
     *
     * @see #getPosition
     */
    @Override
    public void setPosition(double s) {
        this.stateCurrent.setPosition(s);
    }

    /**
     * Set the current time since the probe began propagating
     *
     * @param dblTime new probe current time in <strong>seconds</strong>
     *
     * @author jdg
     */
    @NoEdit    // editors should not edit this property
    @Override
    public void setTime(double dblTime) {
        this.stateCurrent.setTime(dblTime);
    }

    /**
     * <p>
     * Set the longitudinal phase of this probe with respect to the RF phase.
     * Typically used to account for phase delay/advance in cavities incurred
     * due to finite propagation time. For example
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &phi; &#8796; &phi;<sub>0</sub> - &Delta;&phi;
     * <br/>
     * <br/>
     * where &Delta;&phi; = 2&pi;<em>f</em>&Delta;<i/>t</em> is the phase delay
     * due to elapsed time &Delta;<em>t</em>, <em>f</em> is the cavity resonant
     * frequency, and &phi;<sub>0</sub> is the operating phase of the cavity
     * (w.r.t. the synchronous particle).
     * </p>
     *
     * @param dblPhsLng the phase delay &Delta;&phi; incurred from probe
     * propagate between RF cavities
     *
     * @author Christopher K. Allen
     * @since Nov 17, 2014
     */
    @Override
    public void setLongitudinalPhase(double dblPhsLng) {
        this.stateCurrent.setLongitudinalPhase(dblPhsLng);
    }

    /**
     * Set the current kinetic energy of the probe.
     *
     * @param w new probe kinetic energy (<strong>electron-volts</strong>)
     *
     * @see #getKineticEnergy
     */
    @Override
    public void setKineticEnergy(double w) {
        this.stateCurrent.setKineticEnergy(w);
    }

    /**
     * Set the charge of the particle species in the beam
     *
     * @param q species particle charge in units of positive electron charge
     */
    @Override
    public void setSpeciesCharge(double q) {
        this.stateCurrent.setSpeciesCharge(q);
    }

    /**
     * Set the rest energy of a single particle in the beam
     *
     * @param eR particle rest energy (<strong>electron-volts</strong>)
     */
    @Override
    public void setSpeciesRestEnergy(double eR) {
        this.stateCurrent.setSpeciesRestEnergy(eR);
    }

    /**
     * <p>
     * Initializes the probe, resetting state as necessary.
     * </p>
     * <h3>CKA NOTES:</h3>
     * <p>
     * These notes concern the refactoring of the probe component in order to
     * tighten the typing.
     * <br>
     * <br>
     * &middot; In order to make this method compatible with the previous
     * behavior it must set the saved "initial state" to the current probe
     * state. The previous incarnation assigned the new initial state by calling
     * the {@link #createProbeState()} method to which created a new probe state
     * representing the current state of the probe.
     * <br>
     * &middot; The trajectory is cleared, that is, there is no longer any
     * history in the probe
     * <br>
     * &middot; Thus, <kbd>initialize()</kbd> is really a poor choice, since all
     * that is done is
     * <br>
     * <br>
     * &nbsp; &nbsp; - The initial state is reset to the current state
     * <br>
     * &nbsp; &nbsp; - The trajectory is cleared
     * </p>
     *
     */
    @Override
    public void initialize() {
        this.stateInit = this.cloneCurrentProbeState();

        this.trajHist = this.createTrajectory();
    }

    /**
     * Save the probe state into trajHist.
     */
    @Override
    public void update() throws ModelException {

        if (!bolTrack) {
            return;
        }

        this.getTrajectory().update(this);
    }

    /**
     * Subclasses should override this method to perform any required post
     * processing upon completion of algorithm processing. This method
     * implementation does nothing.
     *
     * @deprecated This method is called in several places I don't think it ever
     * is implemented to do anything.
     */
    @Deprecated
    @Override
    public void performPostProcessing() {
    }

    /**
     * Return the algorithm defining the probes dynamics.
     *
     * @return interface to probe dynamics
     */
    @Override
    public IAlgorithm getAlgorithm() {
        return algTracker;
    }

    /**
     * Return the archiving interface for this object.
     *
     * @see xal.tools.data.IArchive
     */
    @NoEdit // hide this property so it doesn't appear in editors
    @Override
    public IArchive getArchive() {
        return this;
    }

    // Object Overrides ========================================================
    /*
     *  IArchive Interface
     */
    /**
     * Load the contents of a probe from an data archive represented by a
     * <code>DataAdaptor</code> interface.
     *
     * @param daptSource data archive containing probe info
     *
     * @exception DataFormatException bad probe type, missing child data node,
     * or bad number format
     */
    @Override
    public void load(DataAdaptor daptSource) throws DataFormatException {

        // Make sure we have the correct data source
        String strDataName = daptSource.name();
        if (!strDataName.equals(Probe.PROBE_LABEL)) {
            throw new DataFormatException("Probe#load() - not a probe data source " + strDataName);
        }
        DataAdaptor daptProbe = daptSource;

        // Check for proper probe type 
        String strType = daptProbe.stringValue(Probe.TYPE_LABEL);
        String strClass = this.getClass().getName();
        if (!strType.equals(strClass)) {
            throw new DataFormatException("Probe#load() - data adaptor has wrong probe type: " + strType);
        }

        // Get time stamp
        DateFormat frmDate = new SimpleDateFormat("d.M.yyyy HH:mm:ss",
                Locale.ENGLISH);

        if (daptProbe.hasAttribute(Probe.TIME_LABEL)) 
            try {
            String strTStamp = daptProbe.stringValue(TIME_LABEL);
            this.setTimestamp(frmDate.parse(strTStamp));
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
        // Load any comments
        DataAdaptor daptComm = daptProbe.childAdaptor(Probe.COMMENT_LABEL);
        if (daptComm != null) {
            String strAuth = daptComm.stringValue(Probe.AUTHOR_LABEL);
            String strDate = daptComm.stringValue(Probe.DATE_LABEL);
            String strText = daptComm.stringValue(Probe.TEXT_LABEL);

            String strComm = strAuth + "|" + strDate + "|" + strText;

            this.setComment(strComm);
        }

        // Load the probe algorithm
        DataAdaptor daptAlg = daptProbe.childAdaptor(Probe.NODETAG_ALG);
        if (daptAlg == null) {
            throw new DataFormatException("Probe#load() - no algorithm data");
        }

        IAlgorithm ifcAlg = Tracker.newInstance(daptAlg);
        this.setAlgorithm(ifcAlg);

        // Load state data
        DataAdaptor daptState = daptProbe.childAdaptor(ProbeState.STATE_LABEL);
        if (daptState == null) {
            throw new DataFormatException("Probe#load() - no state data");
        }
        S state;
        try {
            state = readStateFrom(daptState);
        } catch (DataFormatException e) {
            throw new DataFormatException("Probe#load() - exception parsing state element");
        }
        this.applyState(state);
    }

    /**
     * Save the contents of a probe to a data archive represented by a
     * <code>DataAdaptor</code> interface.
     *
     * @param daSink data archive to receive probe information
     */
    @Override
    public void save(DataAdaptor daSink) {

        DataAdaptor daProbe = daSink.createChild(Probe.PROBE_LABEL);

        // Save the probe type information and time stamp
        DateFormat frmDate = new SimpleDateFormat("d.M.yyyy HH:mm:ss",
                Locale.ENGLISH);

        if (this.getTimestamp() == null) {
            this.setTimestamp(new Date());
        }

        daProbe.setValue(Probe.TYPE_LABEL, this.getClass().getName());
        daProbe.setValue(Probe.TIME_LABEL, frmDate.format(this.getTimestamp()));

        // Save the comment
        DataAdaptor daptComm = daProbe.createChild(Probe.COMMENT_LABEL);
        daptComm.setValue(Probe.TEXT_LABEL, this.getComment());

        // Save the algorithm type
        this.getAlgorithm().save(daProbe);

        // Save the probe state information
        S state = createProbeState();
        state.save(daProbe);
    }

    /*
     *  Support Functions
     */
    /**
     * This is a convenience function for derived classes when implementing
     * their deepCopy() method. This functions deep copies all attributes in the
     * Probe base class. The derived classes need only then deep copy all
     * attributes particular to their type (e.g., state, etc.).
     *
     * @param probe probe object whose Probe base is to be deep copied into this
     */
    protected void deepCopy(Probe<S> probe) {

        // Copy all the Probe base attributes by copying the current ProbeState        
        this.stateCurrent = probe.stateCurrent.copy();

        this.bolTrack = probe.bolTrack;

        // Copy the algorithm object if we have one
        this.algTracker = null;
        final IAlgorithm algorithm = probe.getAlgorithm();
        if (algorithm != null) {
            this.setAlgorithm(algorithm.copy());
        }
    }
}
