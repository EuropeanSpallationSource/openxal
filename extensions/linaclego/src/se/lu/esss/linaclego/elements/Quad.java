package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;
import se.lu.esss.linaclego.LegoSet;


@XmlType(name = "quad")
public class Quad extends BeamlineElement {
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		visitLegoMonitors(visitor);
	}
	
	public double getFieldGradient()
	{
		return getParameters().getDoubleValue("g");
	}
	
	public String getDevName()
	{
		LegoSet s = getParameters().get("g").getLegoSet();
		return s != null ? s.getDevName() : getEssId() + ".CURR";
	}
}
