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

    /**
     * Label for this version of Open XAL
     */
    private static final String LABEL;
    private static final Logger LOGGER = Logger.getLogger(Info.class.getName());

    // static initializer
    static {
        // assign the default label
        String label = "Open XAL";

        // attempt to load info properties from the "info.json" file
        LOGGER.log(Level.INFO, "Getting info resource...");
        final URL infoLocation = ResourceManager.getResourceURL(Info.class, "info.json");
        if (infoLocation != null) {
            try {
                final StringBuilder buffer = new StringBuilder();
                try (InputStream infoStream = infoLocation.openStream()) {
                    final BufferedReader infoReader = new BufferedReader(new InputStreamReader(infoStream));
                    while (true) {
                        final String nextLine = infoReader.readLine();
                        if (nextLine != null) {
                            buffer.append(nextLine);
                            buffer.append("\n");
                        } else {
                            // end of input
                            break;
                        }
                    }
                }

                @SuppressWarnings("unchecked")
                final Map<String, Object> infoMap = (Map<String, Object>) JSONCoder.defaultDecode(buffer.toString());

                label = (String) infoMap.get("label");
            } catch (IOException exception) {
                LOGGER.log(Level.SEVERE, exception, () -> "Exception attempting to load Open XAL info from: " + infoLocation);
                LOGGER.log(Level.INFO, "Will revert to default info label: {0}", label);
            }
        }

        // assign the Info propreties
        LABEL = label;
    }

    private Info() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the label for this version of Open XAL
     */
    public static String getLabel() {
        return LABEL;
    }
}
