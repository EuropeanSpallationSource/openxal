package se.lu.esss.linaclego;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Implements reading of legosets.
 * 
 * &lt;legoSet devName="DTL1-TNK:RFS-KLY-010.GRAD" tf0="0" tf1="176192" type="double" unit="unit"/&gt;
*/
public class LegoSet {
	@XmlAttribute(name = "devName")
	protected String devName;
	
	@XmlAttribute(name = "tf0")
	protected double tf0;
	
	@XmlAttribute(name = "tf1") 
	protected double tf1;
	
	@XmlAttribute(name = "type")
	protected String type;
	
	@XmlAttribute(name = "unit")
	protected String unit;

	public String getDevName() {
		return devName;
	}
}
