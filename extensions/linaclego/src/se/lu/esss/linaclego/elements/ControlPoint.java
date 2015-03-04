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
    protected Parameters d = new Parameters();
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

    public ControlPoint()
    {
    	
    }
    
    public ControlPoint(String id, String type, String section, String cell, String slot, String ble)
    {
    	this.id = id;
    	this.type = type;
    	this.section = section;
    	this.cell = cell;
    	this.slot = slot;
    	this.ble = ble;
    }
    
    public Parameters getParameters() {
        return this.d;
    }

    public String getId() {
        return id;
    }

    public String getSection() {
        return section;
    }

    public String getCell() {
        return cell;
    }

    public String getSlot() {
        return slot;
    }

    public String getBle() {
        return ble;
    }
    
    public String getType() {
        return type;
    }

    public String getModel() {
        return model;
    }

    public String getDevName() {
        return devName;
    }

	public double[] getPosition() {
		
		return new double[]{d.getDoubleValue("dxmm"), d.getDoubleValue("dymm"), d.getDoubleValue("dzmm")};
	}
	
	public String getEssId()
	{
		return section+"-"+cell+"-"+slot+"-"+getId();
	}
}
