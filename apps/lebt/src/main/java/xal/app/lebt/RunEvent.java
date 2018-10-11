/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import javafx.event.EventType;
import static xal.app.lebt.RunSimulationEvent.RUN_SIMULATION;

/**
 *
 * @author nataliamilas
 */
public class RunEvent extends RunSimulationEvent {

    public static final EventType<RunSimulationEvent> CUSTOM_EVENT_TYPE = new EventType(RUN_SIMULATION, "Run full Simulation");

    private final Boolean run;

    RunEvent(Boolean run) {
        super(CUSTOM_EVENT_TYPE);
        this.run = run;
    }    

    @Override
    public void invokeHandler(RunSimulationHandler handler) {
        handler.onRunEvent(run);
    }

}
