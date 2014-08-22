package se.lu.esss.linaclego;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.elements.BeamlineElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ble",
    "d"
})
@XmlRootElement(name = "slot")
public class Slot {
	protected List<BeamlineElement> ble;
    protected Parameters d;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "model")
    protected String model;

    public List<BeamlineElement> getBle() {
        if (ble == null) {
            ble = new ArrayList<BeamlineElement>();
        }
        return this.ble;
    }

    public Parameters getD() {
        if (d == null) {
            d = new Parameters();
        }
        return this.d;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String value) {
        this.model = value;
    }

}
