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

//	AcceleratorSeq myAccSeq;
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
    public HashMap<String, double[][]> getBPMMap() {
        HashMap<String, double[][]> pvMap = new HashMap<>();

        ChannelSnapshot[] css = mss.getChannelSnapshots();
//		HashMap<String, double[]> xMap = new HashMap<String, double[]>();
//		HashMap<String, double[]> yMap = new HashMap<String, double[]>();

        for (int i = 0; i < css.length; i++) {
            double[] xdata, ydata;
            if (css[i].getPV().contains("xTBT")) {
                String BPMId = css[i].getPV().substring(0, 17);
                xdata = css[i].getValue();
                double[][] data = new double[2][xdata.length];

                /*				if (!xMap.containsKey(BPMId)) {
					xMap.put(BPMId, xdata);
				}
				if (yMap.containsKey(BPMId)) {
					data[0] = xdata;
					data[1] = yMap.get(BPMId);
					pvMap.put(BPMId, data);
				}
                 */
                if (!pvMap.containsKey(BPMId)) {
                    data[0] = xdata;
                    pvMap.put(BPMId, data);
                } else {
                    System.arraycopy(xdata, 0, pvMap.get(BPMId)[0], 0, xdata.length);
                }
            }

            if (css[i].getPV().contains("yTBT")) {
                String BPMId = css[i].getPV().substring(0, 17);
                ydata = css[i].getValue();
                double[][] data = new double[2][ydata.length];

                /*				if (!yMap.containsKey(BPMId)) {
					yMap.put(BPMId, ydata);
				}
				if (xMap.containsKey(BPMId)) {
					data[0] = xMap.get(BPMId);
					data[1] = ydata;
					pvMap.put(BPMId, data);
				}
                 */
                if (!pvMap.containsKey(BPMId)) {
                    data[1] = ydata;
                    pvMap.put(BPMId, data);
                } else {
                    System.arraycopy(ydata, 0, pvMap.get(BPMId)[1], 0, ydata.length);
                }
            }
        }

        LOGGER.log(Level.INFO, "Got " + pvMap.size() + " BPMs.");

        return pvMap;
    }
}
