package se.lu.esss.linaclego;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cell"
})
@XmlRootElement(name = "section")
public class Section {

    @XmlElement(required = true)
    protected List<Cell> cell = new ArrayList<Cell>();
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "rfHarmonic", required = true)
    protected byte rfHarmonic;

    public List<Cell> getCells() {   
        return this.cell;
    }

    public String getId() {
        return id;
    }

    public byte getRfHarmonic() {
        return rfHarmonic;
    }

    public void setRfHarmonic(byte value) {
        this.rfHarmonic = value;
    }

	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		for (Cell c : getCells()) c.accept(visitor);
	}

}
