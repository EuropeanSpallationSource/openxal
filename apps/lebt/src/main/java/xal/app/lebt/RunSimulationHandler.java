/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import javafx.event.EventHandler;

/**
 *
 * @author nataliamilas
 */
public abstract class RunSimulationHandler implements EventHandler<RunSimulationEvent> {    

    public abstract void onRunEvent(Boolean param0);
    
    RunSimulationHandler (){

    }        

    @Override
    public void handle(RunSimulationEvent event) {
        event.invokeHandler(this);
    }    

}
