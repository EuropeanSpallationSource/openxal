package xal.smf.attr;

/**
 * Class factory for all (registered) AttributeBucket objects.
 *
 * @author  Nikolay Malitsky, Christopher K. Allen
 */

import  java.util.*;
import  java.lang.reflect.*;


public final class AttributeBucketFactory {


    /*
     *  Global Attributes
     */

    private static HashSet<AttributeBucket>                  setBuckTypes;     // set of all AttributeBucket derived classes
    private static HashMap<String,Constructor<?>>                  mapCtors;         // map of node type ids to constructors



    /** Class loader invoked script - must be modified to register all AttributeBucket types */
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




    /** Get set of all AccelerNode type strings */
    public static String[] getBucketTypes() {
        int                 nTypes;     // number of node types
        String[]            arrTypes;   // returned array of node type strings


        // Allocate the string array
        nTypes   = mapCtors.size();
        arrTypes = new String[nTypes];


        // Build the string array
        int                 iType;      // index of current type
        final Set<String> nodeTypes = mapCtors.keySet();
        iType    = 0;
		for ( final String nodeType : nodeTypes ) {
            arrTypes[iType++] = nodeType;
        }

        return arrTypes;
    }



    /** Creates the node with the specified string id. */
    public static AttributeBucket create(String strType) throws ClassNotFoundException {

        // Error check
        if (!mapCtors.containsKey(strType))
            throw new ClassNotFoundException("Unknown AttributeBucket type : " + strType);


        // Find the constructur object for the AttributeBucket and instantiate new node
        Constructor<?>         ctor;       // contructor object for node type
        Object[]            arrArgs;    // constructor arguments
        AttributeBucket     buck;       // the returned object

        ctor    = mapCtors.get(strType);
        arrArgs = null;

        try {
            buck    = (AttributeBucket)ctor.newInstance(arrArgs);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e)   {
             throw new ClassNotFoundException("Unknown AttributeBucket type : " + strType);
        }


        // Return new node
        return buck;
    }



    /*
     *  Internal Support
     */

    private static void registerClass(AttributeBucket objInst)   {
        if (setBuckTypes == null)
            setBuckTypes = new HashSet<>();

        setBuckTypes.add(objInst);
    }


	@SuppressWarnings( "rawtypes" )		// generics aren't supported in arrays
    private static void buildCtorMap()  {
        mapCtors = new HashMap<>();

		for ( final AttributeBucket bucType : setBuckTypes ) {
            try {
                Class<?>           clsType = bucType.getClass();
                String          strType = bucType.getType();
                Constructor<?>     ctrType = clsType.getConstructor(new Class[] { });

                mapCtors.put(strType, ctrType);

           } catch (NoSuchMethodException e) {
                System.out.println("NoSuchMethodException: " + e.getMessage());
           } catch (SecurityException e)    {
               System.out.println("SecurityException: " + e.getMessage());
           }

        }
    }

    /** Hide the constructor - should never be called */
    private AttributeBucketFactory() {};


}
