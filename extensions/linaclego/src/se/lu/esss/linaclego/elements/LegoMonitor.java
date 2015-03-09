package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

// <legoMonitor +ble="QV-020" +cell="090" disc="PBI" +id="BPM-020" +model="ElpBpm" +section="HEBT" +slot="LWU" +type="BPM"/>

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "legoMonitor")
public class LegoMonitor {

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
    @XmlAttribute(name = "section", required = true)
    protected String section;
    @XmlAttribute(name = "disc", required = true)
    protected String disc;
    
    public LegoMonitor()
    {
    	
    }
    
    public LegoMonitor(String id, String type, String section, String cell, String slot, String ble)
    {
    	this.id = id;
    	this.type = type;
    	this.section = section;
    	this.cell = cell;
    	this.slot = slot;
    	this.ble = ble;
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
	
	public String getEssId()
	{
		return section+"-"+cell+"-"+slot+"-"+getId();
	}
}
