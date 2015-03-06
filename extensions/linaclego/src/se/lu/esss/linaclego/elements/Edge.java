package se.lu.esss.linaclego.elements;

import javax.xml.bind.annotation.XmlType;

import se.lu.esss.linaclego.BLEVisitor;

@XmlType(name = "edge")
public class Edge extends BeamlineElement {
	@Override
	public void accept(BLEVisitor visitor) {
		visitor.visit(this);
		visitControlPoints(visitor);
	}
	
	public double getPoleFaceRotationAngle()
	{
		return getParameters().getDoubleValue("poleFaceAngleDeg");
	}
	
	public double getCurvatureRadius()
	{
		return getParameters().getDoubleValue("radOfCurvmm");
	}
	
	public double getGap()
	{
		return getParameters().getDoubleValue("gapmm");
	}
	
	public double getK1()
	{
		return getParameters().getDoubleValue("K1");
	}
	
	public double getK2()
	{
		return getParameters().getDoubleValue("K2");
	}
	
	@Override
	public double getApertureR()
	{
		return getParameters().getDoubleValue("aperRadmm");
	}
	
	public int getHVFlag()
	{
		// TODO make enum
		return getParameters().getIntValue("HVflag");
	}
}
