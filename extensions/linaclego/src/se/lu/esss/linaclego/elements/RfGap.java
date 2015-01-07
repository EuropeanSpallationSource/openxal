package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;

@XmlType(name = "rfGap")
public class RfGap extends BeamlineElement {
	@XmlTransient
	protected TransitTimeFactors ttf = new TransitTimeFactors(getParameters());
	
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
	}
	
	public double getVoltage()
	{
		return getParameters().getDoubleValue("voltsT");
	}
	
	public double getRFPhase()
	{
		return getParameters().getDoubleValue("rfPhaseDeg");
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
