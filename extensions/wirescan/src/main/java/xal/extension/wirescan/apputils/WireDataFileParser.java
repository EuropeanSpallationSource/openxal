/*
 * WireDataFileParser.java
 *
 * Created on January 5, 2005, 10:28 AM
 */
package xal.extension.wirescan.apputils;

import java.io.*;
import java.util.*;
import java.text.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author Paul Chu
 */
public class WireDataFileParser {

    private static final Logger LOGGER = Logger.getLogger(WireDataFileParser.class.getName());

    boolean readingRawArrays = false;
    boolean readingFitArrays = false;
    boolean zeroData = false;
    ArrayList<WireData> wires = new ArrayList<>();
    HashMap<String, WireData> wireMap = new HashMap<>();

    String name = "";
    String header = "";
    double[] xFit;
    double[] yFit;
    double[] zFit;
    double[] xRMS;
    double[] yRMS;
    double[] zRMS;
    double[] xRaw;
    double[] yRaw;
    double[] zRaw;
    double[] xFitData;
    double[] yFitData;
    double[] zFitData;
    double[] posRaw;
    double[] posFit;

    String[] xFitS = {};
    LinkedList<String> xFitL = new LinkedList<>(Arrays.asList(xFitS));
    String[] yFitS = {};
    LinkedList<String> yFitL = new LinkedList<>(Arrays.asList(yFitS));
    String[] zFitS = {};
    LinkedList<String> zFitL = new LinkedList<>(Arrays.asList(zFitS));
    String[] xRMSS = {};
    LinkedList<String> xRMSL = new LinkedList<>(Arrays.asList(xRMSS));
    String[] yRMSS = {};
    LinkedList<String> yRMSL = new LinkedList<>(Arrays.asList(yRMSS));
    String[] zRMSS = {};
    LinkedList<String> zRMSL = new LinkedList<>(Arrays.asList(zRMSS));
    String[] xRawS = {};
    LinkedList<String> xRawL = new LinkedList<>(Arrays.asList(xRawS));
    String[] yRawS = {};
    LinkedList<String> yRawL = new LinkedList<>(Arrays.asList(yRawS));
    String[] zRawS = {};
    LinkedList<String> zRawL = new LinkedList<>(Arrays.asList(zRawS));
    String[] xFitDataS = {};
    LinkedList<String> xFitDataL = new LinkedList<>(Arrays.asList(xFitDataS));
    String[] yFitDataS = {};
    LinkedList<String> yFitDataL = new LinkedList<>(Arrays.asList(yFitDataS));
    String[] zFitDataS = {};
    LinkedList<String> zFitDataL = new LinkedList<>(Arrays.asList(zFitDataS));
    String[] posRawS = {};
    LinkedList<String> posRawL = new LinkedList<>(Arrays.asList(posRawS));
    String[] posFitS = {};
    LinkedList<String> posFitL = new LinkedList<>(Arrays.asList(posFitS));

    DecimalFormat float1 = new DecimalFormat("###.####");
    DecimalFormat float2 = new DecimalFormat("#.########");
    DecimalFormat int1 = new DecimalFormat("###");

    long pvLoggerId = 0;

    /**
     * Creates a new instance of ExportToMatlab
     */
    public WireDataFileParser(File file) {
        readFile(file);
    }

    /**
     * read in saved wire scan data file
     */
    public ArrayList<WireData> readFile(File file) {
        int lineNumber = 0;

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));

            String line;

            while ((line = in.readLine()) != null) {
                // get rid of "\n"
                line.replaceAll("\n", "");
                lineNumber++;
                String[] tokens = line.split("\\s+");
                int nValues = tokens.length;
                if (nValues == 0) {
                    continue;
                }
                if (nValues == 4) {
                    double num1;
                    double num2;
                    double num3;
                    try {
                        num1 = Double.parseDouble(tokens[0]);
                        num2 = Double.parseDouble(tokens[1]);
                        num3 = Double.parseDouble(tokens[2]);

                        if (num1 == 0. && num2 == 0. && num3 == 0.) {
                            zeroData = true;
                        } else {
                            zeroData = false;
                        }

                    } catch (NumberFormatException e) {
                        num1 = 1.;
                    }
                }

                String firstName = tokens[0];
                if (firstName.startsWith("start")) {
                    header = line;
                } else if (firstName.indexOf("WS") > 0) {
                    if (lineNumber > 3) {
                        dumpData();
                    }
                    name = tokens[0];
                    readingRawArrays = false;
                    readingFitArrays = false;
                } else if (firstName.equals("")) {
                    readingRawArrays = false;
                    readingFitArrays = false;
                } else if (firstName.startsWith("Area")) {
                    xFitL.add(tokens[1]);
                    xRMSL.add(tokens[2]);
                    yFitL.add(tokens[3]);
                    yRMSL.add(tokens[4]);
                    zFitL.add(tokens[5]);
                    zRMSL.add(tokens[6]);
                } else if (firstName.startsWith("Ampl")) {
                    xFitL.add(tokens[1]);
                    xRMSL.add(tokens[2]);
                    yFitL.add(tokens[3]);
                    yRMSL.add(tokens[4]);
                    zFitL.add(tokens[5]);
                    zRMSL.add(tokens[6]);
                } else if (firstName.startsWith("Mean")) {
                    xFitL.add(tokens[1]);
                    xRMSL.add(tokens[2]);
                    yFitL.add(tokens[3]);
                    yRMSL.add(tokens[4]);
                    zFitL.add(tokens[5]);
                    zRMSL.add(tokens[6]);
                } else if (firstName.startsWith("Sigma")) {
                    xFitL.add(tokens[1]);
                    xRMSL.add(tokens[2]);
                    yFitL.add(tokens[3]);
                    yRMSL.add(tokens[4]);
                    zFitL.add(tokens[5]);
                    zRMSL.add(tokens[6]);
                } else if (firstName.startsWith("Offset")) {
                    xFitL.add(tokens[1]);
                    xRMSL.add(tokens[2]);
                    yFitL.add(tokens[3]);
                    yRMSL.add(tokens[4]);
                    zFitL.add(tokens[5]);
                    zRMSL.add(tokens[6]);
                } else if (firstName.startsWith("Slope")) {
                    xFitL.add(tokens[1]);
                    xRMSL.add(tokens[2]);
                    yFitL.add(tokens[3]);
                    yRMSL.add(tokens[4]);
                    zFitL.add(tokens[5]);
                    zRMSL.add(tokens[6]);
                } else if (firstName.equals("Position") && tokens[2].equals("Raw")) {
                    readingRawArrays = true;
                } else if (firstName.equals("Position") && tokens[2].equals("Fit")) {
                    readingFitArrays = true;
                } else if (firstName.startsWith("---")) {
                } else if (firstName.equals("PVLoggerID")) {
                    String pvLoggerIdS = tokens[2];
                    pvLoggerId = Integer.parseInt(pvLoggerIdS);
                    LOGGER.log(Level.INFO, "PV logger Id = " + pvLoggerId);
                } else if (readingRawArrays && !zeroData) {
                    posRawL.add(tokens[0]);
                    xRawL.add(tokens[1]);
                    yRawL.add(tokens[2]);
                    zRawL.add(tokens[3]);
                } else if (readingFitArrays && !zeroData) {
                    posFitL.add(tokens[0]);
                    xFitDataL.add(tokens[1]);
                    yFitDataL.add(tokens[2]);
                    zFitDataL.add(tokens[3]);
                }

            }

            in.close();
            dumpData();

        } catch (FileNotFoundException e) {
            LOGGER.log(Level.INFO, "Cannot find file: {0}", file.getPath());
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "File reading error: {0}", file.getPath());
        }

        // return an ArrayList with <WireData> in it
        return wires;
    }

    private void dumpData() {
        xFitS = xFitL.toArray(new String[0]);
        yFitS = yFitL.toArray(new String[0]);
        zFitS = zFitL.toArray(new String[0]);
        xRMSS = xRMSL.toArray(new String[0]);
        yRMSS = yRMSL.toArray(new String[0]);
        zRMSS = zRMSL.toArray(new String[0]);
        posRawS = posRawL.toArray(new String[0]);
        xRawS = xRawL.toArray(new String[0]);
        yRawS = yRawL.toArray(new String[0]);
        zRawS = zRawL.toArray(new String[0]);
        posFitS = posFitL.toArray(new String[0]);
        xFitDataS = xFitDataL.toArray(new String[0]);
        yFitDataS = yFitDataL.toArray(new String[0]);
        zFitDataS = zFitDataL.toArray(new String[0]);

        // convert String to double
        xFit = new double[xFitS.length];
        yFit = new double[xFitS.length];
        zFit = new double[xFitS.length];
        xRMS = new double[xFitS.length];
        yRMS = new double[xFitS.length];
        zRMS = new double[xFitS.length];
        for (int i = 0; i < xFitS.length; i++) {
            xFit[i] = Double.parseDouble(xFitS[i]);
            yFit[i] = Double.parseDouble(yFitS[i]);
            zFit[i] = Double.parseDouble(zFitS[i]);
            xRMS[i] = Double.parseDouble(xRMSS[i]);
            yRMS[i] = Double.parseDouble(yRMSS[i]);
            zRMS[i] = Double.parseDouble(zRMSS[i]);
        }

        posRaw = new double[posRawS.length];
        xRaw = new double[posRawS.length];
        yRaw = new double[posRawS.length];
        zRaw = new double[posRawS.length];
        for (int i = 0; i < posRawS.length; i++) {
            posRaw[i] = Double.parseDouble(posRawS[i]);
            xRaw[i] = Double.parseDouble(xRawS[i]);
            yRaw[i] = Double.parseDouble(yRawS[i]);
            zRaw[i] = Double.parseDouble(zRawS[i]);
        }

        posFit = new double[posFitS.length];
        xFitData = new double[posFitS.length];
        yFitData = new double[posFitS.length];
        zFitData = new double[posFitS.length];
        for (int i = 0; i < posFitS.length; i++) {
            posFit[i] = Double.parseDouble(posFitS[i]);
            xFitData[i] = Double.parseDouble(xFitDataS[i]);
            yFitData[i] = Double.parseDouble(yFitDataS[i]);
            zFitData[i] = Double.parseDouble(zFitDataS[i]);
        }

        WireData ws = new WireData(name);
        ws.setData(xFit, yFit, zFit, xRMS, yRMS, zRMS, xRaw, yRaw, zRaw,
                xFitData, yFitData, zFitData, posRaw, posFit);

        wires.add(ws);
        wireMap.put(name, ws);
        resetData();
    }

    public HashMap<String, WireData> getWireMap() {
        return wireMap;
    }

    public String getHeader() {
        return header;
    }

    public long getPVLoggerId() {
        if (pvLoggerId < 1) {
            LOGGER.log(Level.INFO, "Invalid PV Logger ID!");
        }
        return pvLoggerId;
    }

    private void resetData() {
        xFitL.clear();
        yFitL.clear();
        zFitL.clear();
        xRMSL.clear();
        yRMSL.clear();
        zRMSL.clear();
        xRawL.clear();
        yRawL.clear();
        zRawL.clear();
        xFitDataL.clear();
        yFitDataL.clear();
        zFitDataL.clear();
        posRawL.clear();
        posFitL.clear();

    }

}
