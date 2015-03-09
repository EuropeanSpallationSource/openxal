package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;

@XmlType(name = "dtlRfGap")
public class DtlRfGap extends RfGap {
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		visitControlPoints(visitor);
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
		return getParameters().getDoubleValue("rfPhaseAdd");
	}
}
