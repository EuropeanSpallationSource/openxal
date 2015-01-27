package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;

@XmlType(name = "monitor")
public class Monitor extends BeamlineElement {
	public String getMonitorType()
	{
		return getParameters().get("monitorType").getValue();
	}
	
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
	}
}
