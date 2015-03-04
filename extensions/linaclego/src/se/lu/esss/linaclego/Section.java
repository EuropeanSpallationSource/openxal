package se.lu.esss.linaclego;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.Linac.LinacDesc;


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
    protected double rfHarmonic;
    @XmlAttribute(name = "type")
    protected String type;
    
    @XmlTransient
    protected Linac parent;
    
    public List<Cell> getCells() {   
        return this.cell;
    }

    public String getId() {
        return id;
    }
    
    public Linac getParent()
    {
    	return parent;
    }

    public double getRFHarmonic() {
        return rfHarmonic;
    }

	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		for (Cell c : getCells()) c.accept(visitor);
	}

	public boolean isPeriodicLatticeSection() {
		return "periodic".equals(type);
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		this.parent = ((LinacDesc)parent).linac;
	}
}
