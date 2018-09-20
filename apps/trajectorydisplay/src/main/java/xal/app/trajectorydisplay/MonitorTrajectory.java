/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.trajectorydisplay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import xal.smf.impl.BPM;

/**
 * Generic class for monitoring a collection of BPMs
 *
 * @author nataliamilas
 */
public class MonitorTrajectory {

    private List<BPM> BPMList;

    private final Set<ChangeListener> listeners = new HashSet<>();

    //Constructor
    public MonitorTrajectory() {
        this.BPMList = new ArrayList<BPM>();
    }

    public MonitorTrajectory(List<BPM> BPMList) {
        this();
        this.BPMList = BPMList;
    }

    public void setTrajectoryMonitor(List<BPM> BPMList) {
        synchronized (listeners) {
            List<BPM> old_BPMList = this.BPMList;
            this.BPMList = BPMList;
            listeners.forEach(listener -> listener.changed(null, old_BPMList, this.BPMList));
        }

    }

    public List<BPM> getMonitor() {
        synchronized (listeners) {
            return BPMList;
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
