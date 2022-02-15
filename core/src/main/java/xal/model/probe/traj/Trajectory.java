package xal.model.probe.traj;

import xal.tools.RealNumericIndexer;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;
import xal.model.probe.Probe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the history for a probe. Saves <code>ProbeState</code> objects, each
 * of which reflects the state of the <code>Probe</code> at a particular point
 * in time. The entire set of states then forms the trajectory of the associated
 * probe through the machine model under simulation.
 *
 * @param S the <code>ProbeState</code>-derived type indicating the class of
 * objects to be stored in this container
 *
 * @author Craig McChesney
 * @author Christopher K. Allen
 * @version $id:
 *
 */
public class Trajectory<S extends ProbeState<S>> implements IArchive, Iterable<S> {

    private static final Logger LOGGER = Logger.getLogger(Trajectory.class.getName());
    /*
     * Global Constants
     */

    // 
    // Persistence
    //
    /**
     * data node tag for trajectory
     */
    public static final String TRAJ_LABEL = "trajectory";

    /**
     * data attribute tag for trajectory concrete type
     */
    private static final String TYPE_TRAJ_TAG = "type";

    /**
     * data attribute tag for probe state concrete type
     */
    private static final String TYPE_STATE_TAG = "datatype";

    /**
     * data attribute tag for time stamp
     */
    private static final String TIMESTAMP_TAG = "timestamp";

    /**
     * data attribute tag for user comment string
     */
    private static final String COMMENT_TAG = "description";


    /*
     * Local Types
     */
    /**
     * <p>
     * The follow comments are only partially correct - the implementation has
     * been modified. In particular, the keys to the map are now unique, they
     * are never modified. Thus, there is no "equivalence class" anymore. Also,
     * the key is not necessarily the string hardware identifier for the state.
     * It could be any unique string.
     * </p>
     * <p>
     * Maintains a list of probe states for every each element class. Each
     * element class is refined as the collection is built. States are placed
     * into the map using the modeling element ID as the key. If it is found
     * that that element ID is similar to an existing element ID, than the state
     * is entered into the element ID class. By "similar", we mean that one ID
     * string can be contained within another ID (or is equal to).
     * </p>
     * <p>
     * The idea is that each element ID class is then representative of probe
     * states associated with a single hardware node. See
     * {@link IdentifierEquivClass} for another explanation.
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot; I am not sure if we have to explicitly consider the empty
     * identifier string. This empty ID is a substring of all IDs.
     * <br>
     * &middot; But we use the <code>{@link TreeMap#ceilingEntry(Object)}</code>
     * for access so that may circumvent things.
     * <br>
     * &middot; The easy thing to do might be just to reject empty strings in
     * <code>{@link #putState(String, ProbeState)}</code>
     * </p>
     *
     * @author Christopher K. Allen
     * @since Aug 14, 2014
     * @version Dec 16, 2014
     */
    private class ElemStateMap {

        /**
         * Comparator defining the order of String ID keys in the tree map
         */
        private final Comparator<String> cmpKeyOrder;

        /**
         * Map of SMF node identifiers to probe states within all modeling
         * elements
         */
        private final TreeMap<String, RealNumericIndexer<S>> mapNodeToStates;

        /**
         * The last map entry to be accessed when adding a new probe state
         */
        private Map.Entry<String, RealNumericIndexer<S>> entryLast;

        /*
         * Initialization
         */
        /**
         * Creates a new uninitialized instance of <code>ElementStateMap</code>.
         *
         * @author Christopher K. Allen
         * @since Jun 5, 2013
         */
        public ElemStateMap() {

            // Create the comparator for ordering the tree map nodes according to node IDs
            //  Then create the map itself
            this.cmpKeyOrder = (strId1, strId2) -> strId1.compareTo(strId2);

            this.mapNodeToStates = new TreeMap<>(cmpKeyOrder);

            // Create a blank last map entry
            String ideEmpty = "XXX - Root Node";
            RealNumericIndexer<S> setIdEmpty = new RealNumericIndexer<>();

            this.entryLast = new AbstractMap.SimpleEntry<>(ideEmpty, setIdEmpty);
        }

        /*
         * Operations
         */
        /**
         * <p>
         * Enters the given probe state into the map using the element's
         * hardware node ID as the key. Note that this key can be changed
         * internally (clobbered) if the element ID belongs to an hardware ID
         * class already identified.
         * </p>
         * <p>
         * To improve efficiency the method checks if the state's hardware node
         * ID (of the state's associated element) is the same (i.e., same
         * equivalence class) as the last one provided in the previous call to
         * this method. A reference to the list of states for that ID is kept on
         * hand so a full map search is not used.
         * </p>
         *
         * @param strDevId hardware node ID of the modeling element associated
         * with the given probe state
         * @param state probe state to be entered into the map
         *
         * @author Christopher K. Allen
         * @since Aug 14, 2014
         */
        public void putState(String strDevId, S state) {

            // We're going to reject the empty ID
            if (strDevId.equals("")) {
                return;
            }

            // Get the Node ID class for last node accessed (needed for mapping elements)
            //      and get the position of the state within the sequence 
            //      (needed for indexing states within elements)
            double dblPos = state.getPosition();
            String strIdLast = this.entryLast.getKey();

            // The state is a member of the last equivalence class accessed
            if (this.cmpKeyOrder.compare(strIdLast, strDevId) == 0) {
                RealNumericIndexer<S> setStates = this.entryLast.getValue();

                setStates.add(dblPos, state);
                return;
            }

            // This is a new equivalence class - that is, different then the last accessed
            //      Get the list of states corresponding to the ID class
            RealNumericIndexer<S> setStates = this.mapNodeToStates.get(strDevId);

            // If there is no element list for this ID class, create one and add it to the map
            if (setStates == null) {
                this.createNewEquivClass(strDevId, state);

                return;
            }

            setStates.add(dblPos, state);

            // This will have the same equiv. class ID
            this.updateLastEntry(strDevId, setStates);
        }

        /**
         * Returns a set of position ordered probe states corresponding to the
         * given hardware node ID.
         *
         * @param strDevId model element identifier
         *
         * @return all the probe states which are associated with the given
         * hardware node ID, or <code>null</code> if there are none
         *
         * @author Christopher K. Allen
         * @since Aug 14, 2014
         */
        public List<S> getStates(String strDevId) {

            // Specific case; We get lucky, the given node ID is in the last equivalence class accessed
            String strIdLast = this.entryLast.getKey();

            if (this.cmpKeyOrder.compare(strDevId, strIdLast) == 0) {
                RealNumericIndexer<S> setStates = this.entryLast.getValue();

                return setStates.toList();
            }

            // The general case: We must get the list of states corresponding to the ID class
            RealNumericIndexer<S> setStates = this.mapNodeToStates.get(strDevId);
            if (setStates == null) {
                return new ArrayList<>();
            }

            return setStates.toList();
        }

        /**
         * Return all the states managed by this map as a list.
         *
         * @return all probe states managed by this element state map
         *
         * @author Christopher K. Allen
         * @since Aug 26, 2014
         */
        public List<S> getAllStates() {
            List<S> lstStates = new LinkedList<>();
            Collection< RealNumericIndexer<S>> setLists = this.mapNodeToStates.values();

            for (RealNumericIndexer<S> rni : setLists) {
                Iterator<S> iter = rni.iterator();
                while (iter.hasNext()) {
                    S state = iter.next();
                    lstStates.add(state);
                }
            }
            return lstStates;
        }

        /*
         * Support Methods
         */
        /**
         * Creates a new set of probe states to go into the (NodeID,{probe
         * states}) mapping. The given probe state is used as the first element
         * in the new set and the given equivalence class identifier is used to
         * key the map. The set is created and put into the map.
         *
         * @param strDevId ID of the equivalence class of probe states (usually
         * a hardware node prefix)
         * @param stateFirst The first (and only) state in the new state set
         *
         * @author Christopher K. Allen
         * @since Aug 14, 2014
         */
        private void createNewEquivClass(String strDevId, S stateFirst) {

            // Create the set of probe states (indexed by position)
            //  and add the given state to this new set
            RealNumericIndexer<S> setStates = new RealNumericIndexer<>();

            double dblPos = stateFirst.getPosition();
            setStates.add(dblPos, stateFirst);

            // Now put the equivalence class ID - probe state pair into the map 
            this.mapNodeToStates.put(strDevId, setStates);

            // Save the last list to be accessed
            this.updateLastEntry(strDevId, setStates);
        }

        /**
         * Convenience method for resetting the last entry maintained by this
         * class. Saves the space of having to write out all the nested types
         * and generic information.
         *
         * @param strDevId ID of the equivalence class of probe states (usually
         * a hardware node prefix)
         * @param setStates The set of probe states belonging to the given class
         * ID
         *
         * @author Christopher K. Allen
         * @since Aug 14, 2014
         */
        private void updateLastEntry(String strDevId, RealNumericIndexer<S> setStates) {
            this.entryLast = new AbstractMap.SimpleEntry<>(strDevId, setStates);
        }
    }

    /*
     * Global Methods
     */
    // I think this will have to be rewritten once the other trajectory classes
    //   are gone since there will be no way to instantiate [newInstance()]
    /**
     * Return a new instance of the appropriately typed <code>Trajectory</code>
     * object initialized with the data contained in the given data source.
     *
     * @param daptSrc data source containing trajectory information
     *
     * @return new <code>Trajectory<code> instance initialized from the data source
     *
     * @throws DataFormatException    malformed data stare
     */
    @SuppressWarnings("unchecked")
    public static <S extends ProbeState<S>> Trajectory<S> loadFrom(DataAdaptor daptSrc) throws DataFormatException {

        // Get the trajectory node, bail out if not there
        DataAdaptor daptTraj = daptSrc.childAdaptor(Trajectory.TRAJ_LABEL);
        if (daptTraj == null) {
            DataFormatException e = new DataFormatException("Trajectory#createFrom() - DataAdaptor contains no trajectory node");
            LOGGER.log(Level.SEVERE, null, e);

            throw e;
        }

        // Need to check for the type attributes 
        //  If there are missing we have no way
        if (!daptTraj.hasAttribute(TYPE_TRAJ_TAG)) {
            DataFormatException e = new DataFormatException("Trajectory node must conatain trajectory type attribute " + TYPE_TRAJ_TAG);
            LOGGER.log(Level.SEVERE, null, e);

            throw e;
        }
        if (!daptTraj.hasAttribute(TYPE_STATE_TAG)) {
            DataFormatException e = new DataFormatException("Trajectory node must conatain probe state type attribute " + TYPE_STATE_TAG);
            LOGGER.log(Level.SEVERE, null, e);

            throw e;
        }

        // Everything looks good so far, let's get the class types and see if we can 
        //  instantiate a new trajectory object.  This is all reflection so if anything
        //  goes wrong here it's simply a crash and burn.  Otherwise return the new trajectory.
        try {
            String strTypeTraj = daptTraj.stringValue(TYPE_TRAJ_TAG);
            Class<?> clsTraj = Class.forName(strTypeTraj);
            Constructor<?> ctorTraj = clsTraj.getConstructor(Class.class);

            String strTypeState = daptTraj.stringValue(TYPE_STATE_TAG);
            Class<?> clsState = Class.forName(strTypeState);
            Object[] arrCtorArgs = {clsState};

            Trajectory<S> trjNew = (Trajectory<S>) ctorTraj.newInstance(arrCtorArgs);

            trjNew.load(daptTraj);

            return trjNew;

        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | DataFormatException e) {
            throw new DataFormatException("", e);
        }
    }

    /*
     *  Local Attributes
     */
    /**
     * any user comments regard the trajectory
     */
    private String description = "";

    /**
     * time stamp of trajectory
     */
    private Date timestamp = new Date();

    /**
     * Type class of the underlying probe state objects
     */
    private final Class<S> clsStates;

    /**
     * the history of probe states along the trajectory
     */
    private RealNumericIndexer<S> rniStateHistory;

    /**
     * Probe states by hardware node ID
     */
    private final ElemStateMap mapSmfIdToStates;

    /**
     * Probe states by modeling element type ID
     */
    private final ElemStateMap mapElemTypeToStates;

    /**
     * Creates a new <code>Trajectory</code> given the
     * <code>Class&lt;S&gt;</code> object of the underlying
     * <code>ProbeState</code> type, <code><strong>S</strong></code>.
     *
     *
     * @param S the template type of <code>ProbeState</code> object that the new
     * trajectory will manage
     * @param clsStates the class type the <code>ProbeState</code> derived
     * managed objects
     *
     * @since Jul 1, 2014
     */
    public Trajectory(final Class<S> clsStates) {
        this.clsStates = clsStates;

        this.rniStateHistory = new RealNumericIndexer<>();
        this.mapSmfIdToStates = new ElemStateMap();
        this.mapElemTypeToStates = new ElemStateMap();
    }

    /**
     * Set the user comment string
     *
     * @param strDescr user comment string
     */
    public void setDescription(String strDescr) {
        description = strDescr;
    }

    /**
     * Set the time stamp of the trajectory.
     *
     * @param lngTimeStamp number of milliseconds since January 1, 1970 GMT
     */
    public void setTimestamp(long lngTimeStamp) {
        timestamp = new Date(lngTimeStamp);
    }

    /**
     * Gets the class type of the probe states forming the trajectory.
     *
     * @return a <code>Class&lt;S&gt;</code> object for the class
     * <strong><code>S</code></strong>
     *
     * @author Jonathan M. Freed
     */
    public Class<S> getStateClass() {
        return this.clsStates;
    }

    /*
     ************* Trajectory Operations ************************
     */
    /**
     * Captures the specified probe's current state to a <code>ProbeState</code>
     * object then saves it to the trajectory. State goes at the tail of the
     * trajectory list.
     *
     * @param probe target probe object
     */
    public void update(Probe<S> probe) {
        S state = probe.cloneCurrentProbeState();
        addState(state);
    }

    /**
     * Save the <code>ProbeState</code> object to this trajectory. It is then
     * indexed by its position, by its hardware node source, and by its modeling
     * element type identifier.
     *
     * @param state new state addition to trajectory
     *
     * @since Dec 16, 2014 by Christopher K. Allen
     */
    public void addState(final S state) {
        double dblPos = state.getPosition();
        rniStateHistory.add(dblPos, state);

        String strElemTypeId = state.getElementTypeId();
        String strSmfNodeId = state.getHardwareNodeId();

        this.mapElemTypeToStates.putState(strElemTypeId, state);
        this.mapSmfIdToStates.putState(strSmfNodeId, state);
    }

    /**
     * <p>
     * This is the legacy method for adding states to the trajectory. See the
     * method <code>{@link #addState(ProbeState)}</code>, which should be used
     * instead as it follows the standard Java naming convention.
     * </p>
     *
     * @param state new state addition to trajectory
     *
     * @deprecated this method is replaced with
     * <code>{@link #addState(ProbeState)}</code> which is more appropriately
     * named
     */
    @Deprecated
    public void saveState(final S state) {
        this.addState(state);
    }

    /**
     * Remove the last state from the trajectory and return it.
     *
     * @return the most recent <code>ProbeState</code> in the history
     */
    public S popLastState() {
        return rniStateHistory.remove(rniStateHistory.size() - 1);
    }

    /**
     * Returns, but does not remove, the probe state at the last axial position.
     *
     * @return the probe state in this trajectory with the largest axial
     * position
     *
     * @since Dec 16, 2014 by Christopher K. Allen
     */
    public S peakLastByPosition() {
        int cntStates = this.rniStateHistory.size();
        return this.rniStateHistory.get(cntStates - 1);
    }

    /**
     * Return the last state in the trajectory with the given type.
     *
     * @param strElemTypeId class name of the desired probe state type
     *
     * @return final probe state in the trajectory of the given type
     *
     * @since Dec 29, 2015, Christopher K. Allen
     */
    public S peakLastByType(String strElemTypeId) {
        List<S> lstStatesForType = this.mapElemTypeToStates.getStates(strElemTypeId);
        int cntStates = lstStatesForType.size();

        return lstStatesForType.get(cntStates - 1);
    }

    /* 
     * ************* Trajectory Data ************************
     */
    /**
     * Return the user comment associated with this trajectory.
     *
     * @return user comment (meta-data)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Return the time stamp of the trajectory object
     *
     * @return trajectory time stamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Return the number of states in the trajectory.
     *
     * @return the number of states
     */
    public int numStates() {
        return rniStateHistory.size();
    }


    /*
     * *************** Trajectory States ******************
     */
    /**
     * Return an Iterator over the iterator's states.
     */
    public Iterator<S> stateIterator() {
        return rniStateHistory.iterator();
    }

    /**
     * Returns the probe's initial state or null if there is none.
     *
     * @return the probe's initial state or null
     */
    public S initialState() {
        return stateWithIndex(0);
    }

    /**
     * Returns the probe's final state or null if there is none.
     *
     * @return the probe's final state or null
     */
    public S finalState() {
        return stateWithIndex(numStates() - 1);
    }

    /**
     * Get the list of all states in this trajectory managed by the state map.
     *
     * @return a new list of this trajectory's states
     */
    public List<S> getStatesViaStateMap() {
        return this.mapSmfIdToStates.getAllStates();
    }

    /**
     * Returns a list of all the states in this trajectory managed by the state
     * numeric (position) indexer.
     *
     * @return
     *
     * @author Christopher K. Allen
     * @since Aug 26, 2014
     */
    public List<S> getStatesViaIndexer() {
        return this.rniStateHistory.toList();
    }

    /**
     * Creates and returns a "sub-trajectory" object built from the contiguous
     * state objects of this trajectory between the start node
     * <code>strSmfNodeId1</code> and the stop node <code>strSmfNodeId2</code>.
     * The returned trajectory contains references to the same states contained
     * in this trajectory, <em>they are not duplicates</em>. So any
     * modifications made on the returned object will be reflected here. Also,
     * it is important to note that the returned sub-trajectory
     * <em>excludes</em> all states belonging to the stop hardware node
     * <code>strSmfNodeId2</code>. That is, the returned value contains states
     * from, and including, node 1 up to, but not including, state 2. If you
     * wish to include the states of both hardware nodes see
     * <code>{@link #subTrajectoryInclusive(String, String)}</code>.
     *
     * @param strSmfNodeId1 hardware node ID defining the first state object in
     * sub-trajectory
     * @param strSmfNodeId2 hardware node ID defining the last state object in
     * sub-trajectory
     *
     * @return sub-trajectory of this trajectory defined by the above hardware
     * nodes
     *
     * @author Christopher K. Allen
     * @since Nov 14, 2014
     *
     * @see #subTrajectoryInclusive(String, String)
     */
    public Trajectory<S> subTrajectory(String strSmfNodeId1, String strSmfNodeId2) {
        // The returned sub-trajectory
        Trajectory<S> trjSub = new Trajectory<>(this.clsStates);

        // For every state in this trajectory...
        for (S state : this) {
            String strStateId = state.getHardwareNodeId();

            // Look for the first state
            if (strStateId.equals(strSmfNodeId1)) {
                // Check for the stop state.  
                if (strStateId.equals(strSmfNodeId2)) {
                    break;
                }

                trjSub.addState(state);
            }
        }

        return trjSub;
    }

    /**
     * Creates and returns a "sub-trajectory" object built from the contiguous
     * state objects of this trajectory between the start node
     * <code>strSmfNodeId1</code> and the stop node <code>strSmfNodeId2</code>.
     * The returned trajectory contains references to the same states contained
     * in this trajectory, <em>they are not duplicates</em>. So any
     * modifications made on the returned object will be reflected here. Also,
     * it is important to note that the returned sub-trajectory
     * <em>includes</em> all states belonging to the stop hardware node
     * <code>strSmfNodeId2</code>. That is, the returned value contains states
     * from, and including, node 1 up to and including state 2. If you wish to
     * exclude the states of both hardware nodes see
     * <code>{@link #subTrajectory(String, String)}</code>.
     *
     * @param strSmfNodeId1 hardware node ID defining the first state object in
     * sub-trajectory
     * @param strSmfNodeId2 hardware node ID defining the last state object in
     * sub-trajectory
     *
     * @return sub-trajectory of this trajectory defined by the above hardware
     * nodes
     *
     * @author Christopher K. Allen
     * @since Nov 14, 2014
     *
     * @see #subTrajectory(String, String)
     */
    public Trajectory<S> subTrajectoryInclusive(String strSmfNodeId1, String strSmfNodeId2) {
        // The returned sub-trajectory
        Trajectory<S> trjSub = new Trajectory<>(this.clsStates);

        // For every state in this trajectory...
        for (S state : this) {
            String strStateId = state.getHardwareNodeId();

            // Look for the first state
            if (strStateId.equals(strSmfNodeId1)) {
                trjSub.addState(state);

                // Check for the stop state.  
                if (strStateId.equals(strSmfNodeId2)) {
                    break;
                }
            }
        }

        return trjSub;
    }

    /**
     * Returns the probe state at the specified position. Returns null if there
     * is no state for the specified position.
     */
    public S stateAtPosition(double pos) {
        for (S state : rniStateHistory) {
            if (state.getPosition() == pos) {
                return state;
            }
        }
        return null;
    }

    /**
     * Get the state that is closest to the specified position
     *
     * @param position the position for which to find a state
     * @return the state nearest the specified position
     */
    public S stateNearestPosition(final double position) {
        final int index = rniStateHistory.getClosestIndex(position);
        return rniStateHistory.size() > 0 ? rniStateHistory.get(index) : null;
    }

    // Does the return type need to be an array?  Otherwise an ArrayList would
    //   support generics
    /**
     * Returns the states that fall within the specified position range,
     * inclusive.
     *
     * @param low lower bound on position range
     * @param high upper bound on position range
     * @return an array of <code>ProbeState</code> objects whose position falls
     * within the specified range
     */
    public List<S> statesInPositionRange(final double low, final double high) {
        final int[] range = rniStateHistory.getIndicesWithinLocationRange(low, high);
        if (range != null) {
            final List<S> result = new ArrayList<>(range[1] - range[0] + 1);
            for (int index = range[0]; index <= range[1]; index++) {
                result.add(rniStateHistory.get(index));
            }

            return result;
        } else {

            return new LinkedList<>();
        }
    }

    /**
     * <p>
     * The old comment read
     * <br>
     * <br>
     * &nbsp; &nbsp; "Get the probe state for the specified element ID."
     * <br>
     * <br>
     * which is now inaccurate. The returned state is actual the first state for
     * the given identifier which is treated as that for an SMF hardware node.
     * The "first state" is the state with the smallest upstream position, that
     * which the probe encounters first in an forward propagation.
     *
     * @param strSmfNodeId hardware node ID of the desired state
     *
     * @return The first probe state for the given hardware node.
     */
    public S stateForElement(final String strSmfNodeId) {
        List<S> lstStates = this.statesForElement(strSmfNodeId);

        if (lstStates == null) {
            return null;
        }

        return lstStates.get(0);
    }

    /**
     * <p>
     * Revised version of state lookup method for an element ID class, which now
     * corresponds to a hardware node ID lookup. Since the lattice generator
     * creates element IDs by prefixing and suffixing hardware node IDs, the
     * actual hardware node ID must be used to get all the states corresponding
     * to a given hardware node.
     * <p>
     * The revised part comes from the fact that the states are now stored and
     * retrieved by hashing or a tree lookup, rather that by a linear search.
     * Specifically, a map of probe states for every hardware node ID is
     * maintained, along with an ordered list of states arranged according to
     * their position along the beamline (for iterations, e.g., see
     * <code>{@link#iterator()}</code>).
     * </p>
     *
     * @param strSmfNodeId identifier for the SMF hardware node
     *
     * @return all the probe states associated with the element ID class
     * containing the given element ID
     *
     * @author Christopher K. Allen
     * @since Jun 5, 2013
     */
    public List<S> statesForElement(String strSmfNodeId) {

        List<S> lstStates = this.mapSmfIdToStates.getStates(strSmfNodeId);
        if (lstStates == null) {
            return new ArrayList<>();
        }
        return lstStates;
    }

    /**
     * <p>
     * Returns a list of all the states created by modeling elements of the
     * given type. Specifically, the method argument is the element type
     * identifier string which is found in the source code of each modeling
     * element. The ordering in the list is determined by the axial position of
     * the state.
     *
     * @param strTypeId modeling element type identifier string.
     *
     * @return all states in this trajectory created by elements of the given
     * type
     *
     * @since Dec 16, 2014 by Christopher K. Allen
     */
    public List<S> statesForElementType(String strTypeId) {
        List<S> lstStates = this.mapElemTypeToStates.getStates(strTypeId);

        if (lstStates == null) {
            return new ArrayList<>();
        }

        return lstStates;
    }

    /**
     * Returns an array of the state indices corresponding to the specified
     * element.
     *
     * <p>
     * <h4>NOTES - CKA</h4>
     * &middot; I have changed this method so the given argument is assumed to
     * be the <strong>hardware</strong> node identifier, not the modeling
     * element ID. I believe that is the original intent.
     * </p>
     *
     * @param element name of element to search for
     *
     * @return an array of integer indices corresponding to that element
     */
    public int[] indicesForElement(String element) {
        List<Integer> indices = new ArrayList<>();
        int c1 = 0;
        Iterator<S> it = stateIterator();
        while (it.hasNext()) {
            S state = it.next();
            if (state.getHardwareNodeId().equals(element)) {
                indices.add(c1);
            }
            c1++;
        }
        int[] resultArray = new int[indices.size()];
        int c2 = 0;
        for (Iterator<Integer> indIt = indices.iterator(); indIt.hasNext(); c2++) {
            resultArray[c2] = indIt.next();
        }
        return resultArray;
    }

    /**
     * Returns the state corresponding to the specified index, or null if there
     * is none.
     *
     * @param i index of state to return
     * @return state corresponding to specified index
     */
    public S stateWithIndex(int i) {
        try {
            return rniStateHistory.get(i);
        } catch (IndexOutOfBoundsException e) {
            LOGGER.log(Level.WARNING, null, e);
            return null;
        }
    }


    /*
     * Iterable Interface
     */
    /**
     * Returns an iterator over all the probe states in the trajectory. This is
     * the single method in the <code>Iterable&lt;T&gt;</code> interface which
     * facilitates the "for each" statement. States are traversed in their order
     * along the beamline.
     *
     * @return iterator for use in a <code>(T X : Container&lt;T&gt;)</code>
     * statement
     *
     * @author Christopher K. Allen
     * @since Oct 28, 2013
     */
    @Override
    public Iterator<S> iterator() {
        return this.rniStateHistory.iterator();
    }


    /*
     * IArchive Interface
     */
    /**
     * Adds a representation of this Trajectory and its state history to the
     * supplied <code>DataAdaptor</code>.
     *
     * @param container <code>DataAdaptor</code> in which to add
     * <code>Trajectory</code> data
     */
    @Override
    public void save(DataAdaptor container) {
        DataAdaptor trajNode = container.createChild(TRAJ_LABEL);
        trajNode.setValue(TYPE_TRAJ_TAG, getClass().getName());
        trajNode.setValue(TYPE_STATE_TAG, this.getStateClass().getName());
        trajNode.setValue(TIMESTAMP_TAG, (double) getTimestamp().getTime());
        if (getDescription().length() > 0) {
            trajNode.setValue(COMMENT_TAG, getDescription());
        }
        addStatesTo(trajNode);
    }

    /**
     * Load the current <code>Trajectory</code> object with the state history
     * information in the <code>DataAdaptor</code> object.
     *
     * @param daptSrc   <code>DataAdaptor</code> from which state history is
     * extracted
     *
     * @exception DataFormatException mal-formated data in
     * <code>DataAdaptor</code>
     */
    @Override
    public void load(DataAdaptor daptSrc) throws DataFormatException {
        DataAdaptor daptTraj = daptSrc;

        if (daptTraj.hasAttribute(TIMESTAMP_TAG)) {
            long lngTime = (long) daptTraj.doubleValue(TIMESTAMP_TAG);
            setTimestamp(lngTime);
        }
        if (daptTraj.hasAttribute(COMMENT_TAG)) {
            setDescription(daptTraj.stringValue(COMMENT_TAG));
        }

        try {
            readStatesFrom(daptTraj);
        } catch (DataFormatException e) {
            throw new DataFormatException("Exception loading from adaptor: ", e);

        }
    }


    /*
     * Object Overrides
     */
    /**
     * Store a textual representation of the trajectory to a string
     *
     * @return trajectory contents in string form
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Trajectory: ").append(getClass().getName()).append("\n");
        buf.append("Time: ").append(getTimestamp()).append("\n");
        buf.append("Description: ").append(getDescription()).append("\n");
        buf.append("States: ").append(rniStateHistory.size()).append("\n");
        Iterator<S> it = stateIterator();
        while (it.hasNext()) {
            buf.append(it.next().toString()).append("\n");
        }
        return buf.toString();
    }

    // Support Methods ========================================================
    /**
     * Iterates over child nodes, asking the concrete Trajectory subclass to
     * create a <code>ProbeState</code> of the appropriate species, initialized
     * from the contents of the supplied <code>DataAdaptor</code>
     *
     * @param data source containing the child state nodes
     */
    @SuppressWarnings("unchecked")
    private void readStatesFrom(DataAdaptor daptSrc) throws DataFormatException {

        Iterator<? extends DataAdaptor> childNodes = daptSrc.childAdaptors().iterator();
        while (childNodes.hasNext()) {
            DataAdaptor childNode = childNodes.next();

            if (!childNode.name().equals(ProbeState.STATE_LABEL)) {
                throw new DataFormatException(
                        "Expected state element, got: " + childNode.name());
            }

            //*********************************************************
            try {
                String strStateType = childNode.stringValue(ProbeState.TYPE_LABEL);

                Class<?> clsProbeState = Class.forName(strStateType);
                S probeState = (S) clsProbeState.getDeclaredConstructor().newInstance();
                probeState.load(childNode);

                addState(probeState);

            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | DataFormatException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
                throw new DataFormatException("", e);
            }

        }
    }

    /**
     * Save the current trajectory information in the proper trajectory format
     * to the target <code>DataAdaptor</code> object.
     *
     * @param container     <code>DataAdaptor</code> to receive trajectory history
     */
    private void addStatesTo(DataAdaptor container) {
        Iterator<S> it = stateIterator();
        while (it.hasNext()) {
            S ps = it.next();
            ps.save(container);
        }
    }

}

/**
 * This class realized equivalences classes of hardware identifier strings.
 * Since modeling elements for hardware devices could have many difference
 * names, it is necessary to identify all these names into a single equivalence
 * class for the hardware ID.
 *
 * @author Christopher K. Allen
 * @since Nov 13, 2014
 *
 * @deprecated This class is no longer needed since the probe state
 * implementation now incorporates a hardware ID attribute.
 */
@Deprecated
class IdEquivClass implements Comparator<IdEquivClass> {

    /*
     * Local Attributes
     */
    /**
     * The root Id
     */
    private String strIdRoot;

    /*
     * Initialization
     */
    public IdEquivClass(String strIdRoot) {
        this.strIdRoot = strIdRoot;
    }

    public IdEquivClass(int cntSzRoot, String strMemberId) {
        this.strIdRoot = strMemberId.substring(0, cntSzRoot);
    }

    public boolean isMember(String strId) {
        return strId.matches(this.strIdRoot);
    }

    /*
     * Comparator Interface
     */
    /**
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     *
     * @author Christopher K. Allen
     * @since Aug 28, 2014
     */
    @Override
    public int compare(IdEquivClass id1, IdEquivClass id2) {
        return 0;
    }
}
