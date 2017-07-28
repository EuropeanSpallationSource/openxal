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

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import xal.ca.Channel;

/**
 *
 * @author yngvelevinsen
 */
public class ChannelWrapper {
    private final Channel m_channel;
    private final StringProperty m_id;
    private final StringProperty m_handle;
    private final StringProperty m_unit;
    private final StringProperty m_type;
    private final SimpleBooleanProperty isScanned;
    private final SimpleBooleanProperty isRead;
    private final SimpleStringProperty instance;
    // This is probably not the best way to do this.
    public static int instanceCount = 0;

    ChannelWrapper(Channel c) {
        m_channel = c;
        m_id = new SimpleStringProperty(this, "id");
        m_handle = new SimpleStringProperty(this, "handle");
        m_type = new SimpleStringProperty(this, "type");
        m_unit = new SimpleStringProperty(this, "unit");
        isScanned = new SimpleBooleanProperty(false);
        isRead = new SimpleBooleanProperty(false);
        instanceCount+=1;
        instance = new SimpleStringProperty("x"+instanceCount);


        m_id.set(c.getId());
        setType();

       try {
            m_unit.set(c.getUnits());
        } catch (Exception ex) { }
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
                } catch (Exception ex) {  m_type.set("w"); }

    }
    public StringProperty idProperty() {
        return m_id;
    }
    public Channel getChannel() {
        return m_channel;
    }
    public String getChannelName() {
        return m_id.getValue();
    }
    public SimpleStringProperty instanceProperty() {
        return instance;
    }
    public StringProperty handleProperty() {
        return m_handle;
    }
    public StringProperty unitProperty() {
        return m_unit;
    }
    public StringProperty typeProperty() {
        return m_type;
    }
    public boolean getIsScanned() {
        return isScanned.get();
    }
    public SimpleBooleanProperty isScannedProperty() {
        return isScanned;
    }
    public SimpleBooleanProperty isReadProperty() {
        return isRead;
    }
    public boolean getIsRead() {
        return isRead.get();
    }
}
