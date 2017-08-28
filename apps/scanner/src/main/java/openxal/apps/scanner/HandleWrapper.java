package openxal.apps.scanner;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import static openxal.apps.scanner.ChannelWrapper.instanceCount;
import xal.ca.Channel;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.AcceleratorNode;

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

/**
 * For each handle in the EPICS table, this is needed.
 *
 * @author yngvelevinsen
 */
public class HandleWrapper {

    private final Channel m_channel;
    private final AcceleratorNode m_node;
    private final StringProperty m_id;
    private final StringProperty m_handle;
    private final StringProperty m_typeHandle;
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
    public static int instanceCount = 0;

    HandleWrapper(AcceleratorNode node, String handle) {
        m_node = node;
        m_channel = node.getChannel(handle);
        m_channel.connectAndWait();
        initialValue = new SimpleDoubleProperty(0.0);
        try {
            initialValue.set(m_channel.getRawValueRecord().doubleValue());
        } catch (ConnectionException | GetException ex) {
            Logger.getLogger(ChannelWrapper.class.getName()).log(Level.WARNING, null, ex);
        }
        m_id = new SimpleStringProperty(this, "id");
        m_typeHandle = new SimpleStringProperty(this, "typeHandle");
        m_handle = new SimpleStringProperty(this, "handle");
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

        m_id.set(m_channel.getId());
        m_handle.set(handle);
        m_typeHandle.set(node.getType()+": "+handle);
        setType();

       try {
            m_unit.set(m_channel.getUnits());
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
                } catch (Exception ex) {  m_type.set("rw"); }

    }

    // Properties...

    public StringProperty idProperty() {
        return m_id;
    }
    public SimpleStringProperty instanceProperty() {
        return instance;
    }
    public StringProperty handleProperty() {
        return m_handle;
    }
    public StringProperty typeHandleProperty() {
        return m_typeHandle;
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

    // get functions...

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
    public double[] getScanPoints() {
        if (scanPoints==null)
            updateScanRange(min.get(),max.get());
        return scanPoints;
    }
    public String getHandle() {
        return m_handle.get();
    }
    public String getTypeHandle() {
        return m_typeHandle.get();
    }
    public String getElementClass() {
        return m_node.getType();
    }

    // set functions

    public String setInstance() {
        if (instance.get().equals("x0")) {
            instanceCount+=1;
            instance.set("x"+instanceCount);
        }
        return instance.get();
    }

    // other

    private void updateScanRange(Number oldValue, Number newValue) {
        if ( newValue==null || newValue == oldValue ) {
            return;
        }
        scanPoints = new double[npoints.get()];
        for(int i=0;i<npoints.get();i++) {
            scanPoints[i] = min.get()+(max.get()-min.get())/(npoints.get()-1)*i;
        }

    }
}
