/*
 * Copyright (c) 2019, Open XAL Collaboration
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package xal.app.scanner;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * A simple (stupid you say?) class which holds a string and
 * a boolean to turn the setting on/off
 *
 * @author yngvelevinsen
 */
public class SimpleOption {
    private final SimpleBooleanProperty isSelected;
    private final SimpleStringProperty description;

    public SimpleOption(String descr, boolean initialSetting) {
        description = new SimpleStringProperty(descr);
        isSelected = new SimpleBooleanProperty(initialSetting);
    }
    
    /**
     * @propertyDescription If true, the PV's return home after a scan (or when
     * a scan is stopped)
     * 
     * @return True if the setting is active/selected, false if deactivated
     */
    public SimpleBooleanProperty isSelectedProperty() {
        return isSelected;
    }
    
    /**
     * @propertyDescription If true, the PV's return home after a scan (or when
     * a scan is stopped)
     * 
     * @return True if the setting is active/selected, false if deactivated
     */
    public SimpleStringProperty descriptionProperty() {
        return description;
    }
    
    public String getDescription() {
        return description.toString();
    }
}
