package se.lu.esss.linaclego;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


//<legoSet cell="020" dataId="rfpdeg4" +devName="MBL-020CRM:RFS-PAMP-040.PHAS" section="MBL" +tf0="0" +tf1="1.0" +type="double" +unit="deg"/>

/**
 * Implements reading of legosets.
 * 
 * &lt;legoSet devName="DTL1-TNK:RFS-KLY-010.GRAD" tf0="0" tf1="176192" type="double" unit="unit"/&gt;
*/
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "legoSet")
public class LegoSet {
	@XmlAttribute(name = "section")
	protected String section;
	
	@XmlAttribute(name = "cell")
	protected String cell;
	
	@XmlAttribute(name = "dataId")
	protected String dataId;
	
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

	public String getDataId() {
		return dataId;
	}

	public String getSection() {
		return section;
	}

	public String getCell() {
		return cell;
	}
}
