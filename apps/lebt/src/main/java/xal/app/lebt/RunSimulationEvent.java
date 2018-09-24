/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lebt;

import javafx.event.Event;
import javafx.event.EventType;

/**
 *
 * @author nataliamilas
 */
public abstract class RunSimulationEvent extends Event {
    
    public static final EventType<RunSimulationEvent> RUN_SIMULATION =
                new EventType<>(Event.ANY, "RUN_SIMULATION");

    public RunSimulationEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
    
    public abstract void invokeHandler(RunSimulationHandler handler);
    
}