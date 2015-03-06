package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;

@XmlType(name = "dtlCell")
public class DtlCell extends RfGap {
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		visitControlPoints(visitor);
	}
	
	@Override
	public double getLength()
	{
		return getParameters().getDoubleValue("cellLenmm");
	}
	
	public double getQ1Length()
	{
		return getParameters().getDoubleValue("q1Lenmm");
	}
	
	public double getQ2Length()
	{
		return getParameters().getDoubleValue("q2Lenmm");
	}
	
	public double getCellCenter()
	{
		return getParameters().getDoubleValue("cellCentermm");
	}
	
	public double getQ1FieldGradient()
	{
		return getParameters().getDoubleValue("grad1Tpm");
	}
	
	public double getQ2FieldGradient()
	{
		return getParameters().getDoubleValue("grad2Tpm");
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
