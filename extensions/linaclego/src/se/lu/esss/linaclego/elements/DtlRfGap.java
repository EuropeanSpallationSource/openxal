package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;
import se.lu.esss.linaclego.LegoSet;
import se.lu.esss.linaclego.Parameters.D;

@XmlType(name = "dtlRfGap")
public class DtlRfGap extends RfGap {
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		visitLegoMonitors(visitor);
	}
	
	@Override
	public double getLength()
	{
		return getParameters().getDoubleValue("length");
	}
	
	public double getVoltageMult()
	{
		return getParameters().getDoubleValue("voltMult");
	}
	
	public double getRFPhaseAdd()
	{
		return getParameters().getDoubleValue("phaseOffDeg");
	}
	
	@Override
	public String getVoltageDevName()
	{
		LegoSet s = getParameters().get("voltMult").getLegoSet();
		if (s == null) System.err.println("Warning: there's no voltMult devNames for "+getEssId()+ " "+getClass());
		return s != null ? s.getDevName() : getEssId().replace('-', ':') + ".GRAD";
	}
	
	@Override
	public String getRFPhaseDevName()
	{
		LegoSet s = getParameters().get("phaseOffDeg") != null ? getParameters().get("phaseOffDeg").getLegoSet() : null;
		if (s == null) System.err.println("Warning: there's no phaseOffDeg devNames for "+getEssId()+ " "+getClass());
		return s != null ? s.getDevName() : getEssId().replace('-', ':') + ".PHAS";
	}
}
