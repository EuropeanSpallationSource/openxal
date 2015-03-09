package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;

@XmlType(name = "thinSteering")
public class ThinSteering extends BeamlineElement {
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		visitLegoMonitors(visitor);
	}
	
	public double getXKick()
	{
		return getParameters().getDoubleValue("xkick");
	}
	
	public double getYKick()
	{
		return getParameters().getDoubleValue("ykick");
	}
	
	public int getKickType()
	{
		// TODO make enum
		return getParameters().getIntValue("kickType");
	}
}
