package xal.smf.attr;

/**
 * Class factory for all (registered) AttributeBucket objects.
 *
 * @author Nikolay Malitsky, Christopher K. Allen
 */
import java.util.*;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AttributeBucketFactory {

    private static final Logger LOGGER = Logger.getLogger(AttributeBucketFactory.class.getName());

    /*
     *  Global Attributes
     */
    /**
     * set of all AttributeBucket derived classes
     */
    private static HashSet<AttributeBucket> setBuckTypes;
    /**
     * map of node type ids to constructors
     */
    private static HashMap<String, Constructor<?>> mapCtors;

    /**
     * Class loader invoked script - must be modified to register all
     * AttributeBucket types
     */
    static {

        registerClass(new AlignmentBucket());
        registerClass(new ApertureBucket());
        registerClass(new DisplaceBucket());
        registerClass(new MagnetBucket());
        registerClass(new RfCavityBucket());
        registerClass(new RotationBucket());
        registerClass(new TwissBucket());
        registerClass(new SequenceBucket());
        registerClass(new DipoleBucket());
        registerClass(new DipoleCorrBucket());

        buildCtorMap();
    }

    /**
     * Get set of all AccelerNode type strings
     */
    public static String[] getBucketTypes() {
        // number of node types
        int nTypes;
        // returned array of node type strings
        String[] arrTypes;

        // Allocate the string array
        nTypes = mapCtors.size();
        arrTypes = new String[nTypes];

        // Build the string array
        // index of current type
        int iType;
        final Set<String> nodeTypes = mapCtors.keySet();
        iType = 0;
        for (final String nodeType : nodeTypes) {
            arrTypes[iType++] = nodeType;
        }

        return arrTypes;
    }

    /**
     * Creates the node with the specified string id.
     */
    public static AttributeBucket create(String strType) throws ClassNotFoundException {

        // Error check
        if (!mapCtors.containsKey(strType)) {
            throw new ClassNotFoundException("Unknown AttributeBucket type : " + strType);
        }

        // Find the constructur object for the AttributeBucket and instantiate new node
        // contructor object for node type
        Constructor<?> ctor;
        // constructor arguments
        Object[] arrArgs;
        // the returned object
        AttributeBucket buck;

        ctor = mapCtors.get(strType);
        arrArgs = null;

        try {
            buck = (AttributeBucket) ctor.newInstance(arrArgs);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
            throw new ClassNotFoundException("Unknown AttributeBucket type : " + strType);
        }

        // Return new node
        return buck;
    }

    /*
     *  Internal Support
     */
    private static void registerClass(AttributeBucket objInst) {
        if (setBuckTypes == null) {
            setBuckTypes = new HashSet<>();
        }

        setBuckTypes.add(objInst);
    }

    @SuppressWarnings("rawtypes")        // generics aren't supported in arrays
    private static void buildCtorMap() {
        mapCtors = new HashMap<>();

        for (final AttributeBucket bucType : setBuckTypes) {
            try {
                Class<?> clsType = bucType.getClass();
                String strType = bucType.getType();
                Constructor<?> ctrType = clsType.getConstructor(new Class[]{});

                mapCtors.put(strType, ctrType);

            } catch (NoSuchMethodException e) {
                LOGGER.log(Level.INFO, "NoSuchMethodException: ", e);
            } catch (SecurityException e) {
                LOGGER.log(Level.INFO, "SecurityException: ", e);
            }

        }
    }

    /**
     * Hide the constructor - should never be called
     */
    private AttributeBucketFactory() {
    }
;

}
