/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openxal.apps.scanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author yngvelevinsen
 */
public class FXMLFunctions {
    
    /**
     * A dictionary of the created datasets..
     */
    public static Map<String, double[][]> dataSets;
    
    public static void initialize() {
        dataSets = new HashMap<>();
    }
    
    /**
     *
     */
    public static void actionScanAddPV() {
        System.out.println("You add a PV");
    }

    public static void actionScanRemovePV() {
        System.out.println("You remove a PV");
    }

    public static double[][] actionExecute() {
        System.out.println("Executing");
        double[][] xy = new double[2][10];
        Random rnd = new Random();
        for (int i=0;i<10;i++) {
            xy[0][i]=i+1;
            xy[1][i]=rnd.nextDouble();
        }
        int measNum = dataSets.size()+1;
        dataSets.put("Measurement "+measNum, xy);
        System.out.println("There are " + dataSets.size() + " data sets now.");
        return xy;
    }
    
}
