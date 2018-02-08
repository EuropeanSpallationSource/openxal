/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.trajectorydisplay;

import java.net.URL;
import javafx.beans.property.SimpleBooleanProperty;
import xal.extension.fxapplication.XalFxDocument;

/**
 *
 * @author nataliamilas
 */
public class TrajectoryDisplayDocument extends XalFxDocument{
    
    public SimpleBooleanProperty liveTrajectory;
    
    public TrajectoryArray Trajectory;  

    /* CONSTRUCTOR 
     * in order to access to variable from the main class
    */

    public TrajectoryDisplayDocument() {
        super();
    }
    
    public void saveTrajectory(){
       
    }
                
    @Override
    public void saveDocumentAs(URL url) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadDocument(URL url) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }   

   
}
