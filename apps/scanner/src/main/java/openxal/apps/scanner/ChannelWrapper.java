/*
 * Copyright (c) 2017, Open XAL Collaboration
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
package openxal.apps.scanner;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import xal.ca.Channel;
import xal.ca.ConnectionException;
import xal.ca.GetException;

/**
 *
 * @author yngvelevinsen
 */
public class ChannelWrapper {
    private final Channel m_channel;
    private final StringProperty m_id;
    private final StringProperty m_unit;
    private final StringProperty m_type;
    private final SimpleDoubleProperty min;
    private final SimpleDoubleProperty max;
    private final SimpleDoubleProperty initialValue;
    private final SimpleIntegerProperty npoints;
    private final SimpleBooleanProperty isScanned;
    private final SimpleBooleanProperty isRead;
    private final SimpleStringProperty instance;
    private double[] scanPoints;
    // This is probably not the best way to do this.
    private static int instanceCount = 0;

    public ChannelWrapper(Channel c) {
        m_channel = c;
        m_channel.connectAndWait();
        initialValue = new SimpleDoubleProperty(0.0);
        try {
            initialValue.set(c.getRawValueRecord().doubleValue());
        } catch (ConnectionException | GetException ex) {
            Logger.getLogger(ChannelWrapper.class.getName()).log(Level.WARNING, null, ex);
        }
        m_id = new SimpleStringProperty(this, "id");
        m_type = new SimpleStringProperty(this, "type");
        m_unit = new SimpleStringProperty(this, "unit");
        min = new SimpleDoubleProperty(initialValue.get()-1.0);
        max = new SimpleDoubleProperty(initialValue.get()+1.0);
        npoints = new SimpleIntegerProperty(5);

        isScanned = new SimpleBooleanProperty(false);
        isRead = new SimpleBooleanProperty(false);
        instance = new SimpleStringProperty("x0");


        updateScanRange(min.get(),max.get());

        npoints.addListener((observable, oldValue, newValue) -> updateScanRange(oldValue, newValue));
        min.addListener((observable, oldValue, newValue) -> updateScanRange(oldValue, newValue));
        max.addListener((observable, oldValue, newValue) -> updateScanRange(oldValue, newValue));
        m_id.set(c.getId());
        setType();

       try {
            m_unit.set(c.getUnits());
        } catch (ConnectionException | GetException ex) { }
    }

    private void setType() {
        try {
            if (m_channel.readAccess() && m_channel.writeAccess()) {
                 m_type.set("rw");
            } else if (m_channel.readAccess() ) {
                 m_type.set("r");
            } else if (m_channel.writeAccess() ) {
                m_type.set("w");
             }
        } catch (Exception ex) { m_type.set("w"); }
    }
    public StringProperty idProperty() {
        return m_id;
    }
    public SimpleStringProperty instanceProperty() {
        return instance;
    }
    public StringProperty unitProperty() {
        return m_unit;
    }
    public StringProperty typeProperty() {
        return m_type;
    }
    public SimpleBooleanProperty isScannedProperty() {
        return isScanned;
    }
    public SimpleBooleanProperty isReadProperty() {
        return isRead;
    }
    public SimpleDoubleProperty minProperty() {
        return min;
    }
    public SimpleDoubleProperty maxProperty() {
        return max;
    }
    public SimpleIntegerProperty npointsProperty() {
        return npoints;
    }

    public Channel getChannel() {
        return m_channel;
    }
    public String getChannelName() {
        return m_id.getValue();
    }
    public boolean getIsScanned() {
        return isScanned.get();
    }
    public boolean getIsRead() {
        return isRead.get();
    }
    public int getNpoints() {
        return npoints.get();
    }
    private void updateScanRange(Number oldValue, Number newValue) {
        if ( newValue==null || newValue == oldValue ) {
            return;
        }
        scanPoints = new double[npoints.get()];
        for(int i=0;i<npoints.get();i++) {
            scanPoints[i] = min.get()+(max.get()-min.get())/(npoints.get()-1)*i;
        }

    }

    public double[] getScanPoints() {
        if (scanPoints==null)
            updateScanRange(min.get(),max.get());
        return scanPoints.clone();
    }

    public String setInstance() {
        if (instance.get().equals("x0")) {
            instanceCount+=1;
            instance.set("x"+instanceCount);
        }
        return instance.get();
    }

    public String forceInstance(String shortName) {
        // Guessing that this counter will continue work,
        // but it is not guaranteed!
        instanceCount+=1;
        instance.set(shortName);
        return instance.get();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.m_id);
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other.getClass() == this.getClass()) {
            if (((ChannelWrapper)other).getChannelName() == null ? this.getChannelName() == null : ((ChannelWrapper)other).getChannelName().equals(this.getChannelName())) {
                return true;
            }
        }
        return false;
    }
}
