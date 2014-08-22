package se.lu.esss.linaclego;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "slot",
    "d"
})
@XmlRootElement(name = "cell")
public class Cell {

    protected List<Slot> slot = new ArrayList<Slot>();
    protected Parameters d = new Parameters();
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "model")
    protected String model;

    public List<Slot> getSlots() {
        return this.slot;
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
