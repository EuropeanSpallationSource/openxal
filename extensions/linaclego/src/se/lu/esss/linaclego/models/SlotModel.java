package se.lu.esss.linaclego.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.elements.BeamlineElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "var",
    "ble"
})
@XmlRootElement(name = "slotModel")
public class SlotModel {

    protected List<Var> var;
    @XmlElement(required = true)
    protected List<BeamlineElement> ble;
    @XmlAttribute(name = "id", required = true)
    protected String id;

    public List<Var> getVar() {
        if (var == null) {
            var = new ArrayList<Var>();
        }
        return this.var;
    }

    public List<BeamlineElement> getBle() {
        if (ble == null) {
            ble = new ArrayList<BeamlineElement>();
        }
        return this.ble;
    }

    public String getId() {
        return id;
    }
}
