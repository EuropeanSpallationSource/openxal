/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.application.pvserver;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import xal.ca.Channel;

/**
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class PV {

    private SimpleStringProperty name;

    private SimpleBooleanProperty writable;

    private SimpleStringProperty value;
    
    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public SimpleBooleanProperty writableProperty() {
        return writable;
    }

    public Boolean getWritable() {
        return writable.get();
    }

    public void setWrite(Boolean writable) {
        this.writable.set(writable);
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public PV() {
        this(null, true, null);
    }

    public PV(String name, Boolean writable, String value) {
        this.name = new SimpleStringProperty(name);
        this.writable = new SimpleBooleanProperty(writable);
        this.value = new SimpleStringProperty(value);
    }

}
