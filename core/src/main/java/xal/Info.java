/*
 * Info.java
 *
 * Created on September 1, 2015, 10:38 AM
 */

package xal;

import xal.tools.ResourceManager;
import xal.tools.coding.json.JSONCoder;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.*;
import java.io.*;


/**
 * Info about the current Open XAL.
 */
public class Info {
    /** Label for this version of Open XAL */
    final private static String LABEL;
    private static final Logger LOGGER = Logger.getLogger(Info.class.getName());

    // static initializer
    static {
        // assign the default label
        String label = "Open XAL";

        // attempt to load info properties from the "info.json" file
        System.out.println( "Getting info resource..." );
        final URL infoLocation = ResourceManager.getResourceURL( Info.class, "info.json" );
        if ( infoLocation != null ) {
            try {
                //System.out.println( "Attempting to load Info from URL: " + infoLocation );
                final StringBuffer buffer = new StringBuffer();
                final InputStream infoStream = infoLocation.openStream();
                final BufferedReader infoReader = new BufferedReader( new InputStreamReader( infoStream ) );
                while( true ) {
                    final String nextLine = infoReader.readLine();
                    if ( nextLine != null ) {
                        buffer.append( nextLine );
                        buffer.append( "\n" );
                    } else {
                        break;  // end of input
                    }
                }
                infoStream.close();

                //System.out.println( "Buffer: " + buffer.toString() );

                @SuppressWarnings("unchecked")
                final Map<String,Object> infoMap = (Map<String,Object>)JSONCoder.defaultDecode( buffer.toString() );

                //System.out.println( "Info map: " + infoMap );

                label = (String)infoMap.get("label");
            } catch( Exception exception ) {
                LOGGER.log(Level.SEVERE, "Exception attempting to load Open XAL info from: " + infoLocation, exception);
                LOGGER.info("Will revert to default info label: " + label );
            }
        }

        // assign the Info propreties
        LABEL = label;
    }


    /** Get the label for this version of Open XAL */
    static public String getLabel() {
        return LABEL;
    }
}
