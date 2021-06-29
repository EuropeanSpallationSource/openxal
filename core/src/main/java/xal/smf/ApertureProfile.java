/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.smf;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the Aperture Profile for the chamber of each section of
 * the machine.
 *
 * @author nataliamilas
 */
public class ApertureProfile {

    /**
     * map of the positions versus the apertures in horizontal and vertical and
     * the profile shape
     */
    private List<Double> profilePos;

    private List<Double> profileX;

    private List<Double> profileY;

    private List<Integer> apertureShape;

    /* 
    * Constructor
     */
    public ApertureProfile() {
        this.profilePos = new ArrayList<>();
        this.profileX = new ArrayList<>();
        this.profileY = new ArrayList<>();
        this.apertureShape = new ArrayList<>();
    }

    /* Add a single point to the list
    * @param aperX horizontal aperture in m
     */
    public void addProfilePosData(double pos) {
        profilePos.add(pos);
    }

    /* Add a single point to the list
    * @param aperX horizontal aperture in m
     */
    public void addProfileXData(double aperX) {
        profileX.add(aperX);
    }

    /* Add a single point to the list
    * @param aperY vertical aperture in m
     */
    public void addProfileYData(double aperY) {
        profileY.add(aperY);
    }

    /* Add a single point to the list
    * @param aperShape code for aperture shape in OpenXAL
     */
    public void addShapeData(int aperShape) {
        apertureShape.add(aperShape);
    }

    /* Get the full list   
    * @return list with the positionwhere there is aperture information
     */
    public List<Double> getProfilePos() {
        return profilePos;
    }

    /* Get the full list   
    * @return list with horizontal profiles values
     */
    public List<Double> getProfileX() {
        return profileX;
    }

    /* Get array of position and profile   
    * @return 2D array with [position,profileX]
     */
    public double[][] getProfileXArray() {
        double[][] profileArray = new double[2][profilePos.size()];
        for (int j = 0; j < profilePos.size(); j++) {
            profileArray[0][j] = profilePos.get(j);
            profileArray[1][j] = profileX.get(j);
        }

        return profileArray;
    }

    /* Get the full list  
    * @return list with vertical profiles values
     */
    public List<Double> getProfileY() {
        return profileY;
    }

    /* Get array of position and profile   
    * @return 2D array with [position,profileY]
     */
    public double[][] getProfileYArray() {
        double[][] profileArray = new double[2][profilePos.size()];
        for (int j = 0; j < profilePos.size(); j++) {
            profileArray[0][j] = profilePos.get(j);
            profileArray[1][j] = profileY.get(j);
        }

        return profileArray;
    }

    /* Get the full list   
    * @return list with shape value codes
     */
    public List<Integer> getProfileShape() {
        return apertureShape;
    }

    /* Get array of position and profile   
    * @return 2D array with [position,shape]
     */
    public double[][] getShapeArray() {
        double[][] profileArray = new double[2][profilePos.size()];
        for (int j = 0; j < profilePos.size(); j++) {
            profileArray[0][j] = profilePos.get(j);
            profileArray[1][j] = apertureShape.get(j);
        }

        return profileArray;
    }
}
