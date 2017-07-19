/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openxal.apps.scanner;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author yngvelevinsen
 */
public class FXMLFunctions {
    
    /**
     * A list of the created datasets..
     */
    public static ArrayList<int[][]> dataSets;
    
    public static void initialize() {
        dataSets = new ArrayList<>();
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

    public static int[][] actionExecute() {
        System.out.println("Executing");
        int[][] xy = new int[2][10];
        Random rnd = new Random();
        for (int i=0;i<10;i++) {
            xy[0][i]=i+1;
            xy[1][i]=rnd.nextInt();
        }
        dataSets.add(xy);
        System.out.println("There are " + dataSets.size() + " data sets now.");
        return xy;
    }
    
}
