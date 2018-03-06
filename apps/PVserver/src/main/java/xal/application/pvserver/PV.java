/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.application.pvserver;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Juan F. Esteban Müller <juanf.estebanmuller@esss.se>
 */
public class PV {

    private SimpleStringProperty name;

    private SimpleBooleanProperty read;

    private SimpleBooleanProperty write;

    private SimpleStringProperty value;

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public SimpleBooleanProperty readProperty() {
        return read;
    }

    public Boolean getRead() {
        return read.get();
    }

    public void setRead(Boolean read) {
        this.read.set(read);
    }

    public SimpleBooleanProperty writeProperty() {
        return write;
    }

    public Boolean getWrite() {
        return write.get();
    }

    public void setWrite(Boolean write) {
        this.write.set(write);
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public PV() {
        this(null, true, true, null);
    }

    public PV(String name, Boolean read, Boolean write, String value) {
        this.name = new SimpleStringProperty(name);
        this.read = new SimpleBooleanProperty(read);
        this.write = new SimpleBooleanProperty(write);
        this.value = new SimpleStringProperty(value);
    }

}
