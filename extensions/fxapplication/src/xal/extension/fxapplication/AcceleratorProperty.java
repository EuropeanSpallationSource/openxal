/*
* AcceleratorProperty.java
*
* Created on June 30, 2018
*/

package xal.extension.fxapplication;

import java.util.HashSet;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import xal.smf.Accelerator;

/**
 * Monitors changes in the accelerator
 * 
 * @author Natalia Milas <natalia.milas@esss.se>
 */
public class AcceleratorProperty {

    private Accelerator accelerator;

    private final Set<ChangeListener> listeners = new HashSet<>();

    public void setAccelerator(Accelerator accelerator) {
        synchronized( listeners ){
          Accelerator old_accelerator = this.accelerator;
          this.accelerator = accelerator;
          listeners.forEach(listener -> listener.changed(null, old_accelerator, this.accelerator));
        }
    }    

    public Accelerator getAccelerator() {
        synchronized( listeners ){
         return accelerator;
        }
    }

    public void addChangeListener(ChangeListener listener){
        synchronized( listeners ){
          listeners.add(listener);
        }
    };

    public void removeChangeListener(ChangeListener listener){
        synchronized( listeners ){
          listeners.remove(listener);
        }
    };

}