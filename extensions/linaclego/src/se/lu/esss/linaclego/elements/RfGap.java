package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;
import se.lu.esss.linaclego.LegoSet;

@XmlType(name = "rfGap")
public class RfGap extends BeamlineElement {
	@XmlTransient
	protected TransitTimeFactors ttf = new TransitTimeFactors(getParameters());
	
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		visitLegoMonitors(visitor);
	}
	
	public double getVoltage()
	{
		return getParameters().getDoubleValue("voltsT");
	}
	
	public String getVoltageDevName()
	{
		LegoSet s = getParameters().get("voltsT").getLegoSet();
		return s == null ? null : s.getDevName();
	}
	
	public double getRFPhase()
	{
		return getParameters().getDoubleValue("rfPhaseDeg");
	}
	
	public String getRFPhaseDevName()
	{
		LegoSet s = getParameters().get("rfPhaseDeg").getLegoSet();
		return s == null ? null : s.getDevName();
	}
	
	public int getPhaseFlag()
	{
		// TODO make an enum
		return getParameters().getIntValue("phaseFlag");
	}
	
	public double getBetaS()
	{
		return getParameters().getDoubleValue("betaS");
	}
	
	public TransitTimeFactors getTTF()
	{
		return ttf;
	}

	@Override
	public double getApertureR()
	{
		return getParameters().getDoubleValue("radApermm");
	}
}
