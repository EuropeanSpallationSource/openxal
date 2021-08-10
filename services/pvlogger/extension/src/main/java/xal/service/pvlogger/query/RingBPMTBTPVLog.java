package xal.service.pvlogger.query;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.service.pvlogger.*;
import xal.tools.database.*;

public class RingBPMTBTPVLog {

    private static final Logger LOGGER = Logger.getLogger(RingBPMTBTPVLog.class.getName());

    MachineSnapshot mss;

    ChannelSnapshot[] css;

    /**
     * @param id the PV logger ID
     */
    public RingBPMTBTPVLog(long id) {
        // initialize PVLogger
        try {
            PVLogger pvLogger = null;
            final ConnectionDictionary defaultDictionary = PVLogger.newBrowsingConnectionDictionary();
            if (defaultDictionary != null && defaultDictionary.hasRequiredInfo()) {
                pvLogger = new PVLogger(defaultDictionary);
            } else {
                ConnectionPreferenceController.displayPathPreferenceSelector();
                final ConnectionDictionary dictionary = PVLogger.newBrowsingConnectionDictionary();
                if (dictionary != null && dictionary.hasRequiredInfo()) {
                    pvLogger = new PVLogger(dictionary);
                }
            }

            if (pvLogger != null) {
                mss = pvLogger.fetchMachineSnapshot(id);
                css = mss.getChannelSnapshots();
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, null, exception);
        }
    }

    /**
     *
     * Method getBPMMap. The method returns a HashMap with BPM IDs as the keys
     * and BPM turn-by-turn data as the value (2-d Double array
     * [2][TBT_data_size], [0][] is for xTBT and [1][] is for yTBT.
     *
     * @return BPM TBT data
     */
    public Map<String, double[][]> getBPMMap() {
        Map<String, double[][]> pvMap = new HashMap<>();

        ChannelSnapshot[] channelSnapshot = mss.getChannelSnapshots();

        for (ChannelSnapshot channelSnapshot1 : channelSnapshot) {
            double[] xdata;
            double[] ydata;
            if (channelSnapshot1.getPV().contains("xTBT")) {
                String bpmId = channelSnapshot1.getPV().substring(0, 17);
                xdata = channelSnapshot1.getValue();
                double[][] data = new double[2][xdata.length];
                if (!pvMap.containsKey(bpmId)) {
                    data[0] = xdata;
                    pvMap.put(bpmId, data);
                } else {
                    System.arraycopy(xdata, 0, pvMap.get(bpmId)[0], 0, xdata.length);
                }
            }
            if (channelSnapshot1.getPV().contains("yTBT")) {
                String bpmId = channelSnapshot1.getPV().substring(0, 17);
                ydata = channelSnapshot1.getValue();
                double[][] data = new double[2][ydata.length];
                if (!pvMap.containsKey(bpmId)) {
                    data[1] = ydata;
                    pvMap.put(bpmId, data);
                } else {
                    System.arraycopy(ydata, 0, pvMap.get(bpmId)[1], 0, ydata.length);
                }
            }
        }

        LOGGER.log(Level.INFO, "Got {0} BPMs.", pvMap.size());

        return pvMap;
    }
}
