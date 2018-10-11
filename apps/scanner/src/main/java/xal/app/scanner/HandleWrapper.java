package xal.app.scanner;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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

    HandleWrapper(AcceleratorNode node, String handle) {
        m_node = node;
        m_channel = node.getChannel(handle);
        m_id = new SimpleStringProperty(this, "id");
        m_typeHandle = new SimpleStringProperty(this, "typeHandle");
        m_handle = new SimpleStringProperty(this, "handle");
        m_type = new SimpleStringProperty(this, "type");
        m_unit = new SimpleStringProperty(this, "unit");

        m_id.set(m_channel.getId());
        m_handle.set(handle);
        m_typeHandle.set(node.getType()+": "+handle);

       Logger.getLogger(HandleWrapper.class.getName()).log(Level.FINEST, "New object {0}", m_handle);
    }

    /**
     * Call this function after channel has been connected to update attributes for the handle
     *
     * @return true if all successful
     */
    public boolean updateAfterConnected() {
        return updateUnits() && setType();
    }

    /**
     * Update the units by calling this function after the channel is connected.
     * @return true if the units were successfully retrieved from the channel, otherwise false
     */
    private boolean updateUnits() {
            if (m_channel.isConnected())
                try {
                    m_unit.set(m_channel.getUnits());
                    return true;
                } catch (ConnectionException | GetException ex) {
                    Logger.getLogger(HandleWrapper.class.getName()).log(Level.WARNING, "Connection unit problem for {0}", getChannelName());
                }
            return false;
    }

    /**
     * Set the channel type (r/w or only r)
     * Requires that the channel is already connected.
     * @return true if channel type was discovered
     */
    private boolean setType() {
        try {
            if (m_channel.isConnected()) {
            if (m_channel.readAccess() && m_channel.writeAccess()) {
                m_type.set("rw");
                return true;
            } else if (m_channel.readAccess() ) {
                m_type.set("r");
                return true;
            } else if (m_channel.writeAccess() ) {
                m_type.set("w");
                return true;
            }
            } else {
              m_type.set("-");
            }
        } catch (Exception ex) {
            Logger.getLogger(HandleWrapper.class.getName()).log(Level.WARNING, "Connection r/w problem for {0}", getChannelName());
            m_type.set("rw");
        }
        return false;
    }

    // Properties...

    public StringProperty idProperty() {
        return m_id;
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

    // get functions...

    public Channel getChannel() {
        return m_channel;
    }
    public String getChannelName() {
        return m_id.getValue();
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
    public boolean isConnected() {
        return m_channel.isConnected();
    }
}
