/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.trajectorydisplay;

import java.util.HashSet;
import java.util.Set;
import javafx.beans.value.ChangeListener;

/**
 * Generic class for monitoring Objects
 *
 * @author nataliamilas
 */
public class Monitor {

    private Object object;

    private final Set<ChangeListener> listeners = new HashSet<>();

    public void setMonitor(Object object) {
        synchronized (listeners) {
            Object old_object = this.object;
            this.object = object;
            listeners.forEach(listener -> listener.changed(null, old_object, this.object));
        }
    }

    public Object getMonitor() {
        synchronized (listeners) {
            return object;
        }
    }

    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
}
