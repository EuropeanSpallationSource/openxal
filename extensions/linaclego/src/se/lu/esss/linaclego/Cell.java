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

    protected List<Slot> slot;
    protected Parameters d;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "model")
    protected String model;

    public List<Slot> getSlot() {
        if (slot == null) {
            slot = new ArrayList<Slot>();
        }
        return this.slot;
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
