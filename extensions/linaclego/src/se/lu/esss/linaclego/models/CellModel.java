package se.lu.esss.linaclego.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.Slot;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "var",
    "slot"
})
@XmlRootElement(name = "cellModel")
public class CellModel {

    @XmlElement(required = true)
    protected List<Var> var;
    @XmlElement(required = true)
    protected List<Slot> slot;
    @XmlAttribute(name = "id", required = true)
    protected String id;

    public List<Var> getVar() {
        if (var == null) {
            var = new ArrayList<Var>();
        }
        return this.var;
    }

    public List<Slot> getSlot() {
        if (slot == null) {
            slot = new ArrayList<Slot>();
        }
        return this.slot;
    }

    public String getId() {
        return id;
    }

    
}
