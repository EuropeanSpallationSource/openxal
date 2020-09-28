/*
 * Copyright (C) 2020 European Spallation Source ERIC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.extension.fxapplication;

import java.util.HashSet;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import xal.smf.Accelerator;

/**
 * Monitors changes in the accelerator
 *
 * @author Natalia Milas <natalia.milas@ess.eu>
 */
public class AcceleratorProperty {

    private Accelerator accelerator;

    private final Set<ChangeListener> listeners = new HashSet<>();

    public void setAccelerator(Accelerator accelerator) {
        synchronized (listeners) {
            Accelerator old_accelerator = this.accelerator;
            this.accelerator = accelerator;
            listeners.forEach(listener -> listener.changed(null, old_accelerator, this.accelerator));
        }

    }

    public Accelerator getAccelerator() {
        synchronized (listeners) {
            return accelerator;
        }
    }

    public void setTestMode(boolean testMode) {
        accelerator.channelSuite().getChannelFactory().setTest(testMode);
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
