package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.Parameters;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "d"
})
@XmlRootElement(name = "cnpt")
public class ControlPoint {

    @XmlElement(name="d", required = true)
    protected Parameters d;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "ble", required = true)
    protected String ble;
    @XmlAttribute(name = "cell", required = true)
    protected String cell;
    @XmlAttribute(name = "slot", required = true)
    protected String slot;
    @XmlAttribute(name = "type", required = true)
    protected String type;
    @XmlAttribute(name = "model", required = true)
    protected String model;
    @XmlAttribute(name = "devName", required = true)
    protected String devName;
    @XmlAttribute(name = "section", required = true)
    protected String section;

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

    public String getBle() {
        return ble;
    }

    public void setBle(String value) {
        this.ble = value;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String value) {
        this.cell = value;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String value) {
        this.slot = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String value) {
        this.model = value;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String value) {
        this.devName = value;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String value) {
        this.section = value;
    }

}
