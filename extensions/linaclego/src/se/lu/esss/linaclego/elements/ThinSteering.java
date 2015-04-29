package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;
import se.lu.esss.linaclego.LegoSet;
import se.lu.esss.linaclego.Parameters.D;

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
	
	public String getXKickDevName()
	{
		LegoSet s = getParameters().get("xkick").getLegoSet();
		//if (s == null) System.err.println("Warning: there's no xkick devNames for "+getEssId());
		return s != null ? s.getDevName() : getEssId().replace('-', ':') + "X.CURR";
	}
	
	public double getYKick()
	{
		return getParameters().getDoubleValue("ykick");
	}
	
	public String getYKickDevName()
	{
		LegoSet s = getParameters().get("ykick").getLegoSet();
		//if (s == null) System.err.println("Warning: there's no ykick devNames for "+getEssId());
		return s != null ? s.getDevName() : getEssId().replace('-', ':') + "Y.CURR";
	}
	
	public int getKickType()
	{
		// TODO make enum
		return getParameters().getIntValue("kickType");
	}
}
