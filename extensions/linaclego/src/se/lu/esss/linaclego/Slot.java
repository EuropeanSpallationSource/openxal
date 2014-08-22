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
	protected List<BeamlineElement> ble = new ArrayList<BeamlineElement>();
    protected Parameters d = new Parameters();
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "model")
    protected String model;

    public List<BeamlineElement> getBeamlineElements() {
        return this.ble;
    }

    public Parameters getParameters() {
        return this.d;
    }

    public String getId() {
        return id;
    }

    public String getModel() {
        return model;
    }
}
